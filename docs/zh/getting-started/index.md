# 首次使用

本章节将会演示初始化库,嵌入渲染循环并且在屏幕上显示一个全屏的白色矩形

本UI库没有自己的渲染循环, 它通常直接嵌入宿主程序中.

当你创建了一个窗口并持有窗口句柄的时候,你可以这么做以初始化库:

```java
Window window = Window.from(windowHandle, RenderProviderType.OPENGL); //目前我们只有OpenGL后端
```

!!! warning

    本库暂时不支持多窗口

它内部将会:

注册GLFW回调
初始化渲染后端

!!! warning

    你应该在渲染线程调用`Window.from()`

    并且不要在`Window.from()`之后注册任何GLFW回调, 你的回调应该在Window.from之前注册

初始化Window后,你可以获取GUI节点根:

```java
NodeRoot root = window.getNodeRoot();
```

然后,你可以在你的渲染循环里:

```java
//在渲染UI之前的代码...
root.render();
//在渲染UI之后的代码,例如glfwSwapBuffers()...
```

这样,UI库就可以正常执行渲染了

现在,我们将在屏幕上渲染一个覆盖全屏的长方形:

```java
ScaleOffset position = ScaleOffset.ZERO; //位置在左上角
ScaleOffset size = ScaleOffset.fromScale(1, 1); //占满父节点,这里父节点是NodeRoot,占满父节点就是占满全屏
Frame frame = new Frame(position, size); //新建Frame对象
root.addChild(frame); //将Frame挂到节点根
```

ScaleOffset可以在核心概念章节查看详细用法

!!! warning

    上列代码不要在渲染循环中每次都新建,因为我们是保留模式UI库,这会导致每帧都有一个新Frame挂到节点树上,性能严重降低

如果没有任何问题的话,你应该在屏幕上看到一个覆盖全屏的白色长方形