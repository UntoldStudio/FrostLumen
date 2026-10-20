# Multiple Windows

This chapter explains how to use multiple windows with this library.

## Basics

When using multiple windows, you should have one main loop. No matter how many threads you have, there can only be one main loop at any given time.

The main loop should be the only loop among these threads that calls `glfwPollEvents`.

## Rendering a Single Window in a Multi-Window Setup

When you render a certain window, you should at least ensure that the current context is that window. You can use `glfwMakeContextCurrent(windowHandle)` to update it.

## Single-Threaded Multi-Window

When you render multiple windows in a single thread, you should switch contexts, and it is best to have a main window that shares resources with child windows (the `share` parameter of `glfwCreateWindow`). If resources are not shared, `Texture` and `Font` cannot be shared.

For example (assuming that when one window closes, the other should also close):

```java
private static long createWindow(int width, int height, String title, Window shared) {
    return glfwCreateWindow(width, height, title, MemoryUtil.NULL, shared == null ? MemoryUtil.NULL : shared.getWindowHandle());
}

public static void main(String[] args) {
    if (!glfwInit()) {
        throw new RuntimeException("Unable to initialize GLFW");
    }

    long windowHandle1 = createWindow(800, 600, "Test1", null);
    glfwMakeContextCurrent(windowHandle1);
    GL.createCapabilities();
    glClearColor(0.1f, 0.1f, 0.1f, 0.1f);
    Window window1 = Window.from(windowHandle1);
    window1.init(RenderProviderType.OPENGL);
    NodeRoot root1 = window1.getNodeRoot();
    ResourceManager resourceManager1 = root1.getResourceManager();

    long windowHandle2 = createWindow(800, 600, "Test2", window1);
    glfwMakeContextCurrent(windowHandle2);
    glClearColor(0.1f, 0.1f, 0.1f, 0.1f);
    Window window2 = Window.from(windowHandle2);
    window2.init(RenderProviderType.OPENGL);
    NodeRoot root2 = window2.getNodeRoot();
    ResourceManager resourceManager2 = root2.getResourceManager();

    Texture oiiacatBlue = resourceManager1.loadTexture("/oiiaioiiai-blue.jpg"); // Both windows can access it

    while (!glfwWindowShouldClose(windowHandle1) && !glfwWindowShouldClose(windowHandle2)) {
        glfwPollEvents();

        glfwMakeContextCurrent(windowHandle1);
        glClear(GL_COLOR_BUFFER_BIT);
        root1.render();
        glfwSwapBuffers(windowHandle1);

        glfwMakeContextCurrent(windowHandle2);
        glClear(GL_COLOR_BUFFER_BIT);
        root2.render();
        glfwSwapBuffers(windowHandle2);
    }
    
    window1.close();
    window2.close();

    glfwTerminate();
}
```

!!! warning

    If you enable vertical sync, then each `glfwSwapBuffers` in the above code will block for one vertical sync period. Assuming your monitor's maximum frame rate is 120, the actual maximum frame rate will actually be 60.

## Multi-Threaded Multi-Window

When using multiple threads and multiple windows, do not share any resources. This is very important.

You should have at least one main window (main thread). Only it calls `glfwPollEvents`, and it should be responsible for calls to `glfwCreateWindow` and `Window.from`. `window.init` is the responsibility of that window's rendering thread.

Example of multi-threaded multi-window (assuming that when one window closes, the other should also close):

```java
public class Main {
    private static final AtomicBoolean shouldClose = new AtomicBoolean(false);

    private static long createWindow(int width, int height, String title, Window shared) {
        return glfwCreateWindow(width, height, title, MemoryUtil.NULL, shared == null ? MemoryUtil.NULL : shared.getWindowHandle());
    }

    public static void main(String[] args) {
        if (!glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW");
        }

        long windowHandle = createWindow(800, 600, "Main Thread", null);
        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();
        glClearColor(0.1f, 0.1f, 0.1f, 0.1f);
        Window window = Window.from(windowHandle);
        window.init(RenderProviderType.OPENGL);
        NodeRoot root = window.getNodeRoot();

        Window otherWindow = runOtherWindowInOtherThread(800, 600, "Other Thread");
        Thread thread = otherWindow.getRenderThread();

        while (!shouldClose.get() && !glfwWindowShouldClose(windowHandle)) {
            glfwPollEvents();

            glClear(GL_COLOR_BUFFER_BIT);
            root.render();
            glfwSwapBuffers(windowHandle);
        }

        shouldClose.set(true);

        thread.join();

        window.close();
        otherWindow.close();

        glfwTerminate();
    }

    private static Window runOtherWindowInOtherThread(int width, int height, String title) {
        long handle = createWindow(width, height, title, null);
        Window window = Window.from(handle);
        Thread thread = new Thread(() -> {
            glfwMakeContextCurrent(handle);
            GL.createCapabilities();
            window.init(RenderProviderType.OPENGL);
            NodeRoot root = window.getNodeRoot();

            glClearColor(0.1f, 0.1f, 0.1f, 0.1f);

            while (!shouldClose.get()) {
                if (glfwWindowShouldClose(handle)) {
                    root.runAllTasksOnCurrentThread(); // To ensure UserRequestWindowCloseEvent is dispatched correctly
                }
                glClear(GL_COLOR_BUFFER_BIT);
                root.render();
                glfwSwapBuffers(handle);
                if (glfwWindowShouldClose(handle)) { // If the user requested window close at the beginning and it was canceled after node dispatch, then this if block will not be entered
                    break;
                }
            }
            shouldClose.set(true);
        });
        thread.start();
        return window;
    }
}
```