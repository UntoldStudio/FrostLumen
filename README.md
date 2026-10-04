# FrostLumen
[![](https://jitpack.io/v/UntoldStudio/FrostLumen.svg)](https://jitpack.io/#UntoldStudio/FrostLumen)
![fastutil](https://img.shields.io/badge/fastutil-8.5.19-purple)
![LWJGL](https://img.shields.io/badge/LWJGL-3.3.3-red)
![Log4j](https://img.shields.io/badge/Log4j-2.24.1-brown)

FrostLumen is an embedded UI library. You only need to call one rendering callback and pass in one window handle per frame. The core module is the core module; the neoforge module is the Minecraft Mod binding and transitively depends on core; the example module is a standalone application example.

This library currently does not support multiple windows.

Note: This library requires the host to provide LWJGL, JOML, and FastUtil dependencies.

To use this library, add the following to your build script:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.untoldstudio.frostlumen:core:${version}")
}
```

If you're using it through NeoForge, depend on the neoforge module, which transitively pulls in core:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.untoldstudio.frostlumen:neoforge:${version}")
}
```

Documentation is available [here](https://frostlumen.untold.top/).

If you encounter any bugs, you can open an issue.

This project is licensed under Apache 2.0.