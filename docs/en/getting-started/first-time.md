# First Use

This UI library does not have its own render loop; it is usually embedded directly into the host program.

When you have created a window and hold its window handle, you can do this to initialize the library:

```java
Window window = Window.from(windowHandle, RenderProviderType.OPENGL); // Currently we only have an OpenGL backend
```

Internally, it will:

Register GLFW callbacks
Initialize the rendering backend

!!! warning

    You should call `Window.from()` on the rendering thread.

    And do not register any GLFW callbacks after `Window.from()`**

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
ScaleOffset size = ScaleOffset.fromScale(1, 1); // Size covers the entire screen
Frame frame = new Frame(position, size); // Create a new Frame object
root.addChild(frame); // Attach the Frame to the node root
```

!!! warning

    Do not recreate the above code every time in the render loop, because we are a retained-mode UI library. This will cause a new Frame to be attached to the node tree every frame, severely degrading performance.

If there are no problems, you should see a white rectangle covering the entire screen.