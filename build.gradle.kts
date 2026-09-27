import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
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
    dependsOn(tasks.named("spotlessApply"))
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
tasks.withType<Javadoc>().configureEach { enabled = false }

abstract class PushChangesTask : DefaultTask() {
    @TaskAction
    fun push() {
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
        runGit(projectDir, "git", "push", "origin", "HEAD")
        println(">>>推送完成")
    }

    private fun runGit(dir: File, vararg args: String): String {
        val proc = ProcessBuilder(*args)
            .directory(dir)
            .redirectErrorStream(true)
            .start()
        val output = proc.inputStream.bufferedReader().use { it.readText() }
        val exit = proc.waitFor()
        if (exit != 0) {
            throw RuntimeException("Git command failed: ${args.joinToString(" ")}\nError: $output")
        }
        return output
    }
}

tasks.register<PushChangesTask>("pushChanges") {
    dependsOn(tasks.named("spotlessApply"))
    notCompatibleWithConfigurationCache("任务需要交互式输入并访问项目目录")
    description = "自动add,commit并推送当前分支"
}

abstract class ReleaseVersionTask : DefaultTask() {
    @TaskAction
    fun release() {
        val projectDir = project.rootProject.projectDir
        val tagName = project.version.toString()
        if (tagName.isEmpty() || tagName.contains("unspecified")) {
            throw GradleException("版本号无效:'$tagName', 请设置gradle.properties中的version")
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
        runGit(projectDir, "git", "push", "origin", "HEAD")
        val remoteTags = runGit(projectDir, "git", "ls-remote", "--tags", "origin")
        if (remoteTags.contains("refs/tags/$tagName")) {
            throw GradleException("远程仓库已存在标签'$tagName', 请更新version后再试")
        }
        runGit(projectDir, "git", "tag", "-a", tagName, "-m", "Release $tagName")
        runGit(projectDir, "git", "push", "origin", tagName)
        println("发布完成,标签${tagName}已推送,JitPack将开始构建")
    }

    private fun runGit(dir: File, vararg args: String): String {
        val proc = ProcessBuilder(*args)
            .directory(dir)
            .redirectErrorStream(true)
            .start()
        val output = proc.inputStream.bufferedReader().use { it.readText() }
        val exit = proc.waitFor()
        if (exit != 0) {
            throw RuntimeException("Git command failed: ${args.joinToString(" ")}\nError: $output")
        }
        return output
    }
}

tasks.register<ReleaseVersionTask>("releaseVersion") {
    dependsOn(tasks.named("spotlessApply"))
    notCompatibleWithConfigurationCache("任务需要交互式输入并访问项目目录")
    description = "自动add,commit,push并创建发布标签"
}