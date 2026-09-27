plugins {
    id("java")
}

repositories {
    mavenCentral()
}

val lwjglVersion = rootProject.libs.versions.lwjgl.get()
val lwjglNatives = when {
    org.gradle.internal.os.OperatingSystem.current().isWindows -> "natives-windows"
    org.gradle.internal.os.OperatingSystem.current().isLinux -> "natives-linux"
    org.gradle.internal.os.OperatingSystem.current().isMacOsX -> {
        val arch = System.getProperty("os.arch")
        if (arch == "aarch64" || arch == "arm64") "natives-macos-arm64" else "natives-macos"
    }
    else -> throw GradleException("Unsupported operating system")
}

dependencies {
    implementation(project(":core"))
    implementation(rootProject.libs.bundles.log.all)

    implementation(platform(rootProject.libs.lwjgl.bom))
    implementation(rootProject.libs.bundles.lwjgl.all)

    implementation(rootProject.libs.joml)

    runtimeOnly("org.lwjgl:lwjgl:${lwjglVersion}:${lwjglNatives}")
    runtimeOnly("org.lwjgl:lwjgl-opengl:${lwjglVersion}:${lwjglNatives}")
    runtimeOnly("org.lwjgl:lwjgl-glfw:${lwjglVersion}:${lwjglNatives}")
    runtimeOnly("org.lwjgl:lwjgl-stb:${lwjglVersion}:${lwjglNatives}")
    runtimeOnly("org.lwjgl:lwjgl-freetype:${lwjglVersion}:${lwjglNatives}")
}