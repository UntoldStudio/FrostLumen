plugins {
    id("maven-publish")
}

val id: String = project.property("id") as String

dependencies {
    compileOnly(rootProject.libs.bundles.log.all)

    compileOnly(platform(rootProject.libs.lwjgl.bom))
    compileOnly(rootProject.libs.bundles.lwjgl.all)

    compileOnly(rootProject.libs.joml)
}

tasks.jar {
    archiveBaseName.set("frostlumen-core")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = id
        }
    }
}