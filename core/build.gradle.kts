plugins {
    id("maven-publish")
}

dependencies {
    compileOnly(platform(rootProject.libs.lwjgl.bom))
    compileOnly(rootProject.libs.bundles.lwjgl.all)

    compileOnly(rootProject.libs.joml)

    compileOnly(rootProject.libs.fastutil.core)
}

tasks.jar {
    archiveBaseName.set("frostlumen-core")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "core"
        }
    }
}