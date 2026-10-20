# 多窗口

本章节将会讲解如何在本库使用多窗口

## 基础

使用多窗口时,你应该有一个主循环,无论你有多少个线程,同一时刻只能有一个主循环

主循环应该是当前这几个线程中唯一会调用`glfwPollEvents`的循环

## 多窗口下的单个窗口的渲染

当你渲染某窗口时,你应该至少确保ContextCurrent为当前窗口,你可以使用`glfwMakeContextCurrent(windowHandle)`来更新

## 单线程多窗口

当你单线程渲染多个窗口时,你应该做到切换上下文,并且最好拥有一个主窗口,与子窗口共享资源(`glfwCreateWindow`的share参数),如果不共享资源那么Texture和Font无法共用

例如(假设一个窗口关闭另外一个也要关闭):

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

    Texture oiiacatBlue = resourceManager1.loadTexture("/oiiaioiiai-blue.jpg"); //两个窗口都可以访问
    
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

!!! warn

    如果你开了垂直同步,那么上述代码的glfwSwapBuffers每一个都会阻塞一个垂直同步周期,假设你显示器最大帧率120,那么实际最大帧率其实是60

## 多线程多窗口

当你多线程多窗口的时候,不要share任何资源,这很重要

你应该至少有一个主窗口(主线程),只有他调用glfwPollEvents,且`glfwCreateWindow`与`Window.from`的调用都应该由他负责,`window.init`才是那个窗口的渲染线程负责

多线程多窗口示例(假设一个窗口关闭另外一个也要关闭):

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

    private static Window runOtherWindowInOtherThread(int width, int height, String title) throws InterruptedException {
        long handle = createWindow(width, height, title, null);
        Window window = Window.from(handle);
        CountDownLatch initLatch = new CountDownLatch(1); //生产环境不要这么写
        Thread thread = new Thread(() -> {
            try {
                glfwMakeContextCurrent(handle);
                GL.createCapabilities();
                window.init(RenderProviderType.OPENGL);
            } finally {
                initLatch.countDown();
            }
            NodeRoot root = window.getNodeRoot();

            glClearColor(0.1f, 0.1f, 0.1f, 0.1f);

            while (!shouldClose.get()) {
                if (glfwWindowShouldClose(handle)) {
                    root.runAllTasksOnCurrentThread(); //为了确保正确分发UserRequestWindowCloseEvent
                }
                glClear(GL_COLOR_BUFFER_BIT);
                root.render();
                glfwSwapBuffers(handle);
                if (glfwWindowShouldClose(handle)) { //如果在开头用户请求关闭窗口,节点分发后被取消,那么if块不会进入
                    break;
                }
            }
            shouldClose.set(true);
        });
        thread.start();
        initLatch.await();
        return window;
    }
}
```