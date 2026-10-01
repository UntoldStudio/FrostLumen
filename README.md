# FrostLumen
[![](https://jitpack.io/v/UntoldStudio/FrostLumen.svg)](https://jitpack.io/#UntoldStudio/FrostLumen)
![fastutil](https://img.shields.io/badge/fastutil-8.5.19-purple)
![LWJGL](https://img.shields.io/badge/LWJGL-3.3.3-red)
![Gson](https://img.shields.io/badge/Gson-2.10.1-blue)
![Log4j](https://img.shields.io/badge/Log4j-2.24.1-brown)
![SLF4J](https://img.shields.io/badge/SLF4J-2.0.9-orange)

FrostLumen is an embedded UI library that only requires calling one rendering callback and one window handle per frame. The core module is the module you need to rely on, and the neoforge module is a Minecraft Mod bind, application Module is a Independent Editor

This library currently does not support multiple windows

Note: This library requires the host to provide dependencies for LWJGL and JOML and GSON and FastUtil

To use this library, add the following to your build script:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.UntoldStudio:FrostLumen:${version}")
}
```

Documentation is available [here](https://untoldstudio.github.io/FrostLumen).

If you encounter any bugs, feel free to open an issue!

This project is licensed under the Apache2.0