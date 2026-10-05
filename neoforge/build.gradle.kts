plugins {
    id("java-library")
    id("idea")
    id("net.neoforged.moddev") version "2.0.144"
    id("maven-publish")
}

repositories {
    mavenLocal()
}

base {
    archivesName = "frostlumen-neoforge"
}

evaluationDependsOn(":core")

neoForge {
    version = property("neo_version") as String

    runs {
        register("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", property("id") as String)
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        create("${property("id")}") {
            sourceSet(sourceSets.main.get())
            sourceSet(project(":core").extensions
                .getByType<JavaPluginExtension>()
                .sourceSets["main"])
            sourceSet(project(":minecraft").extensions
                .getByType<JavaPluginExtension>()
                .sourceSets["main"])
        }
    }
}

sourceSets.main {
    resources.srcDir("src/generated/resources")
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":minecraft"))
}

tasks.named<Jar>("jar") {
    from(project(":core").sourceSets.main.get().output)
    from(project(":minecraft").sourceSets.main.get().output)
}

val minecraftVersion = project.property("minecraft_version") as String

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    description = "生成模组数据"
    val replaceProperties = mapOf(
        "minecraft_version" to (minecraftVersion),
        "minecraft_version_range" to (project.property("minecraft_version_range") as String),
        "neo_version" to (project.property("neo_version") as String),
        "neo_version_range" to (project.property("neo_version_range") as String),
        "loader_version_range" to (project.property("loader_version_range") as String),
        "mod_id" to (project.property("id") as String),
        "mod_name" to (project.property("id") as String),
        "mod_license" to (project.property("license") as String),
        "mod_version" to (project.property("version") as String),
        "mod_authors" to (project.property("mod_authors") as String),
        "mod_description" to (project.property("mod_description") as String)
    )
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}

sourceSets.main {
    resources.srcDir(generateModMetadata)
}

neoForge.ideSyncTask(generateModMetadata)

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "neoforge-build${minecraftVersion}"
        }
    }
}