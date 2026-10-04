import org.gradle.api.GradleException
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

plugins {
    id("java")
    id("com.diffplug.spotless") version "6.25.0"
}

val versionString: String = project.property("version") as String
val groupId: String = project.property("group_id") as String

group = groupId
version = versionString

subprojects {
    pluginManager.apply("java")
    pluginManager.apply("com.diffplug.spotless")

    spotless {
        java {
            licenseHeaderFile(rootProject.file("HEADER"))
            targetExclude("**/build/**", "**/generated/**")
        }
    }

    tasks.named("build") { dependsOn(tasks.named("spotlessApply")) }

    extra["id"] = rootProject.findProperty("id")?.toString()
    extra["version"] = rootProject.version.toString()
    extra["group_id"] = rootProject.findProperty("group_id")?.toString()

    group = groupId
    version = versionString

    repositories {
        mavenCentral()
    }
}

tasks.build {
    dependsOn(subprojects.map { it.tasks.named("build") })
}
tasks.named<JavaCompile>("compileJava") {
    enabled = false
}
tasks.jar {
    enabled = false
}
tasks.named<Jar>("jar") {
    enabled = false
}

val gitTargetBranch: String = "main"

val spotlessApplyAll = tasks.register("spotlessApplyAll") {
    group = "verification"
    description = "对所有子项目执行spotlessApply"
    dependsOn(subprojects.map { it.tasks.matching { t -> t.name == "spotlessApply" } })
}

tasks.register("pushChanges") {
    dependsOn(spotlessApplyAll)
    notCompatibleWithConfigurationCache("任务需要交互式输入并访问项目目录")
    description = "自动add,commit并推送当前分支"
    dependsOn(tasks.named("build"))
    doLast {
        val projectDir = project.rootProject.projectDir
        val status = runGit(projectDir, "git", "status", "--porcelain")
        if (status.trim().isEmpty()) {
            println("工作区没有需要提交的更改,直接推送")
        } else {
            println("当前更改的文件:")
            println(status)
            println("请输入提交信息:")
            val reader = BufferedReader(InputStreamReader(System.`in`))
            val message = reader.readLine()
            if (message == null || message.trim().isEmpty()) {
                throw GradleException("提交信息不能为空")
            }
            runGit(projectDir, "git", "add", ".")
            runGit(projectDir, "git", "commit", "-m", message)
        }
        runGit(projectDir, "git", "push", gitTargetBranch, "HEAD")
        println("推送完成")
    }
}

tasks.register("releaseVersion") {
    dependsOn(spotlessApplyAll)
    notCompatibleWithConfigurationCache("任务需要交互式输入并访问项目目录")
    description = "自动add,commit,push并创建发布标签"
    dependsOn(tasks.named("build"))
    doLast {
        val projectDir = project.rootProject.projectDir
        val tagName = project.version.toString()
        if (tagName.isEmpty() || tagName.contains("unspecified")) {
            throw GradleException("版本号无效:'${tagName}', 请设置gradle.properties中的version")
        }
        val status = runGit(projectDir, "git", "status", "--porcelain")
        if (status.trim().isEmpty()) {
            println("工作区没有需要提交的更改,直接推送")
        } else {
            println("当前更改的文件:")
            println(status)
            println("请输入提交信息:")
            val reader = BufferedReader(InputStreamReader(System.`in`))
            val message = reader.readLine()
            if (message == null || message.trim().isEmpty()) {
                throw GradleException("提交信息不能为空")
            }
            runGit(projectDir, "git", "add", ".")
            runGit(projectDir, "git", "commit", "-m", message)
        }
        runGit(projectDir, "git", "push", gitTargetBranch, "HEAD")
        val remoteTags = runGit(projectDir, "git", "ls-remote", "--tags", gitTargetBranch)
        if (remoteTags.contains("refs/tags/${tagName}")) {
            throw GradleException("远程仓库已存在标签'${tagName}', 请更新version后再试")
        }
        runGit(projectDir, "git", "tag", "-a", tagName, "-m", "Release $tagName")
        runGit(projectDir, "git", "push", gitTargetBranch, tagName)
        println("发布完成,标签${tagName}已推送")
    }
}

tasks.register<Exec>("docsServe") {
    group = "documentation"
    description = "本地预览文档"
    workingDir = rootProject.projectDir
    dependsOn(tasks.named("javadocAll"))
    val isWindows = System.getProperty("os.name").lowercase().contains("win")
    val exe = if (isWindows) ".venv/Scripts/mkdocs.exe" else ".venv/bin/mkdocs"
    commandLine(exe, "serve")
}

tasks.register<Javadoc>("javadocAll") {
    group = "documentation"
    description = "生成所有模块的统一Javadoc"

    subprojects.forEach { sub ->
        sub.pluginManager.withPlugin("java") {
            source(sub.extensions.getByType<JavaPluginExtension>().sourceSets["main"].allJava)
            classpath += sub.extensions.getByType<JavaPluginExtension>().sourceSets["main"].compileClasspath
        }
    }

    destinationDir = rootProject.file("docs/javadoc")
    isFailOnError = false

    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        charset("UTF-8")
        author(true)
        version(true)
        quiet()
        addStringOption("Xdoclint:none", "-quiet")
    }
}

fun runGit(dir: File, vararg args: String): String {
    val proc = ProcessBuilder(*args)
        .directory(dir)
        .redirectErrorStream(true)
        .apply {
            environment()["GIT_TERMINAL_PROMPT"] = "0"
            environment()["GIT_SSH_COMMAND"] = "ssh -o BatchMode=yes"
            environment()["GIT_EDITOR"] = "true"
            environment()["GIT_ASKPASS"] = "echo"
        }
        .start()

    proc.outputStream.close()

    val output = proc.inputStream.bufferedReader().use { it.readText() }
    val exit = proc.waitFor()

    if (exit != 0) {
        throw GradleException("Git command failed: ${args.joinToString(" ")}\n$output")
    }
    return output
}