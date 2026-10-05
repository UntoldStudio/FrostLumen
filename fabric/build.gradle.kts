plugins {
    id("net.fabricmc.fabric-loom") version "1.18-SNAPSHOT"
    id("maven-publish")
}

val minecraftVersion: String = project.property("minecraft_version").toString()
val loaderVersion: String = project.property("loader_version").toString()
val fabricVersion: String = project.property("fabric_version").toString()

base {
    archivesName.set("frostlumen-fabric")
}

repositories {
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":minecraft"))

    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:$loaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", minecraftVersion)
    inputs.property("loader_version", loaderVersion)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
                "version" to project.version,
                "minecraft_version" to minecraftVersion,
                "loader_version" to loaderVersion
        )
    }
}

val targetJavaVersion = 25
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withSourcesJar()
}

tasks.jar {
    from(project(":core").sourceSets.main.get().output)
    from(project(":minecraft").sourceSets.main.get().output)
    from("LICENSE") {
        rename { "${it}_${project.property("archives_base_name")}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "fabric-build${minecraftVersion}"
        }
    }
}