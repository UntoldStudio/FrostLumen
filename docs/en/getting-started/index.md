# First Use

This chapter will demonstrate initializing the library, embedding it into a render loop, and displaying a full-screen white rectangle on the screen.

This UI library does not have its own render loop; it is usually embedded directly into the host program.

When you have created a window and hold its window handle, you can do this to initialize the library:

```java
Window window = Window.from(windowHandle); // Will register GLFW callbacks
window.init(RenderProviderType.OPENGL); // Will initialize the rendering backend
```

!!! warning

    You should call `Window.from` on the thread that calls `glfwPollEvent`, and call `window.init` on the rendering thread.

    Also, do not register any GLFW callbacks after `Window.from`. Your callbacks should be registered before `Window.from`.

After initializing the Window, you can get the GUI node root:

```java
NodeRoot root = window.getNodeRoot();
```

Then, in your render loop, you can:

```java
// Code before rendering the UI...
root.render();
// Code after rendering the UI, e.g. glfwSwapBuffers()...
```

This way, the UI library can perform rendering normally.

Now, we will render a rectangle covering the entire screen:

```java
ScaleOffset position = ScaleOffset.ZERO; // Position at the top-left corner
ScaleOffset size = ScaleOffset.fromScale(1, 1); // Fill the parent node; here the parent node is NodeRoot, so filling the parent node means filling the entire screen
Frame frame = new Frame(position, size); // Create a new Frame object
root.addChild(frame); // Attach the Frame to the node root
```

For detailed usage of ScaleOffset, see the Core Concepts chapter.

!!! warning

    Do not recreate the above code every time in the render loop, because we are a retained-mode UI library. This will cause a new Frame to be attached to the node tree every frame, severely degrading performance.

If there are no problems, you should see a white rectangle covering the entire screen.