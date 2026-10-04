# FrostLumen
[![](https://jitpack.io/v/UntoldStudio/FrostLumen.svg)](https://jitpack.io/#UntoldStudio/FrostLumen)
![fastutil](https://img.shields.io/badge/fastutil-8.5.19-purple)
![LWJGL](https://img.shields.io/badge/LWJGL-3.3.3-red)
![Log4j](https://img.shields.io/badge/Log4j-2.24.1-brown)

FrostLumen is an embedded UI library. You only need to call one rendering callback and pass in a window handle each frame. The core module is the one you depend on. The neoforge module binds it to Minecraft, and the example module is a standalone app example.

Multiple windows aren't supported yet.

Note: the host needs to provide LWJGL, JOML, and FastUtil.

To use it, add this to your build script:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.untoldstudio.frostlumen:core:${version}")
}
```

If you're using it through NeoForge:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.untoldstudio.frostlumen:neoforge:${version}")
}
```

Docs are [here](https://frostlumen.untold.top/).

If you run into any bugs, open an issue.

Licensed under Apache 2.0.