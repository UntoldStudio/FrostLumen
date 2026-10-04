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

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

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
        }
    }
}

sourceSets.main {
    resources.srcDir("src/generated/resources")
}

dependencies {
    implementation(project(":core"))
}

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties = mapOf(
        "minecraft_version" to (project.property("minecraft_version") as String),
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
            artifactId = "neoforge"
        }
    }
}