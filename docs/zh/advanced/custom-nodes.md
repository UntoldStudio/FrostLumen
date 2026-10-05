# 自定义节点

自定义节点适用于内置节点无法表达的绘制.如果你能用现有节点组合出来,就不要自定义

本章介绍机制——怎么继承,怎么调 GuiRender,怎么响应事件.什么场景值得自定义取决于你的项目,如果某个需求足够普遍,库会提供内置节点

## 如何制作

你的自定义节点应该继承GuiNode<?>抽象类,并且泛型参数填自己的类型,如`Frame extends GuiNode<Frame>`

如果你最终决定自己制作自定义节点,那么你**必须直接调用**我们的渲染抽象层

你的类继承GuiNode后,必须重写`render(GuiRender render, long delta)`方法,其中delta单位为毫秒,当前版本我们不推荐使用delta来做动画,我们推荐使用Tween来做,Tween介绍在上一章节

你的构造函数应该至少拥有`ScaleOffset position, ScaleOffset size`并调用`super(position, size)`

你的所有setter方法应当返回this,如果你造的是抽象类那么请你定义为:`Example<T extends Example<T>> extends GuiNode<T>`并且所有setter返回T(使用父类字段self)

如果你希望绘制默认背景,请调用父类的`drawDefaultFrameBackground(GuiRender render)`方法,如果你的背景绘制是可选的,请加`drawBackground`属性(字段,setter与getter),默认值由节点类型自由决定

示例:最简单的仅仅调用父类`drawDefaultFrameBackground`的Frame类

```java
public class Frame extends GuiNode<Frame> {
    @Override
    protected void render(GuiRender render, long delta) {
        super.drawDefaultFrameBackground(render);
    }

    public Frame(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
```

## 事件

你的节点可以任意监听你想要监听的事件

onXxx是给你的自定义节点重写用的钩子,dispatchXxx(包括dispatchRender)是分发流程,一般不需要重写

如果你仅仅是为了判断鼠标是否在节点内,建议使用父类的字段`protected boolean mouseInNode`

父类还帮你维护了目前有哪些鼠标按键点击了节点,`protected Set<MouseButton> currentMouseClickButtons`,如果你想取消事件的话你可以选择重写

示例:某Button的onMouseButtonEvent方法

```java
@Override
    protected void onMouseButtonEvent(MouseButtonEvent event) {
        if (!Collections.disjoint(canTriggerMouseButtons, currentMouseClickButtons)) { //canTriggerMouseButtons是该节点自己的字段
            event.cancel();
            mouseButtonEventListenerRegistry.trigger(event);
        }
    }
```

目前有这些方法可重写,重写之后在方法体内编写你想要的任何后续处理即可

```java
//可取消事件(event.cancel()可用,调用会阻止后续分发)
protected void onKeyEvent(KeyEvent event) {}
protected void onMouseButtonEvent(MouseButtonEvent event) {}
protected void onMouseMoveEvent(MouseMoveEvent event) {}
protected void onMouseEnterWindowEvent(MouseEnterWindowEvent event) {}
protected void onMouseLeaveWindowEvent(MouseLeaveWindowEvent event) {}
protected void onMouseScrollEvent(MouseScrollEvent event) {}
protected void onUserInputTextEvent(UserInputTextEvent event) {}
protected void onUserRequestWindowCloseEvent(UserRequestWindowCloseEvent event) {} //它如果取消了不光会阻止事件分发还会让窗口不关闭
protected void onUserDropFilesEvent(UserDropFilesEvent event) {}

//不可取消事件
protected void onWindowMinimizeEvent(WindowMinimizeEvent event) {}
protected void onWindowMaximizeEvent(WindowMaximizeEvent event) {}
protected void onWindowMoveEvent(WindowMoveEvent event) {}
protected void onWindowCloseEvent(WindowCloseEvent event) {}
protected void onWindowFocusChangeEvent(WindowFocusChangeEvent event) {}
protected void onFrameBufferSizeChangeEvent(FrameBufferSizeChangeEvent event) {}
```

## GuiRender

GuiRender是我们的渲染抽象层.注意:**它接受的坐标都是相对于窗口的绝对坐标**,你可以直接使用父类的realXxx,它们都是相对于窗口的绝对坐标,截至目前,你可以调用这些方法:

注意:方法细节请见javadoc

```java
//几乎每个渲染/操作类方法都有angle参数,一般直接传父类的realAngle

//渲染类

//画矩形,四个角同一颜色,以下所有min*一般直接传父类的realPosition*,max*一般直接传父类的realPositionMax*
public void drawRectangle(int minX, int minY, int maxX, int maxY, float angle, int red, int green, int blue, int alpha)
//画矩形,可以指定四角颜色(渐变)
public void drawRectangle(int minX, int minY, int maxX, int maxY, float angle,
                          int aRed, int aGreen, int aBlue, int aAlpha,
                          int bRed, int bGreen, int bBlue, int bAlpha,
                          int cRed, int cGreen, int cBlue, int cAlpha,
                          int dRed, int dGreen, int dBlue, int dAlpha
)
//画高级的矩形,支持圆角,边框等
public void drawShape(int minX, int minY, int maxX, int maxY, float angle, int red, int green, int blue, int alpha,
                      int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii,
                      int aBorderThickness, int bBorderThickness, int cBorderThickness, int dBorderThickness,
                      int borderRed, int borderGreen, int borderBlue, int borderAlpha,
                      ThicknessPosition thicknessPosition
)
//画高级的矩形,支持圆角,边框等,四个角都可以独立圆角边框
public abstract void drawShape(int minX, int minY, int maxX, int maxY, float angle,
                               int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                               int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha,
                               int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii,
                               int aBorderThickness, int bBorderThickness, int cBorderThickness, int dBorderThickness,
                               int borderRed, int borderGreen, int borderBlue, int borderAlpha,
                               ThicknessPosition position
)
//画三角形
public void drawTriangle(int ax, int ay, int bx, int by, int cx, int cy, float angle,
                         int aRed, int aGreen, int aBlue, int aAlpha,
                         int bRed, int bGreen, int bBlue, int bAlpha,
                         int cRed, int cGreen, int cBlue, int cAlpha
)
//画三角形,可以指定旋转等操作的中心点
public abstract void drawTriangle(int ax, int ay, int bx, int by, int cx, int cy, int centerX, int centerY, float angle,
                                  int aRed, int aGreen, int aBlue, int aAlpha,
                                  int bRed, int bGreen, int bBlue, int bAlpha,
                                  int cRed, int cGreen, int cBlue, int cAlpha
)
//画纹理,一般来说你应该调用接受Texture的重载而非这个
public abstract void drawTexture(int textureId, int minX, int minY, int maxX, int maxY, int centerX, int centerY, float angle, float u0, float v0, float u1, float v1,
                                 int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                                 int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha
)
//画九宫格纹理,一般来说你应该调用接受Texture的重载(drawTexture)而非这个
public void drawNiceSliceTexture(NiceSliceType type, int textureId, int textureWidth, int textureHeight, int borderLeft, int borderRight, int borderTop, int borderBottom, int targetMinX, int targetMinY, int targetMaxX, int targetMaxY, float angle, float textureU0, float textureV0, float textureU3, float textureV3,
                                 int aRed, int aGreen, int aBlue, int aAlpha,
                                 int bRed, int bGreen, int bBlue, int bAlpha,
                                 int cRed, int cGreen, int cBlue, int cAlpha,
                                 int dRed, int dGreen, int dBlue, int dAlpha,
                                 boolean stretchInner
)
//画纹理,内部根据texture信息决定走九宫格/普通渲染
public void drawTexture(Texture texture, int minX, int minY, int maxX, int maxY, float angle, int red, int green, int blue, int alpha)
//同上,额外支持四个角独立颜色(渐变)
public void drawTexture(Texture texture, int minX, int minY, int maxX, int maxY, float angle, float u0, float v0, float u1, float v1,
                        int aRed, int aGreen, int aBlue, int aAlpha,
                        int bRed, int bGreen, int bBlue, int bAlpha,
                        int cRed, int cGreen, int cBlue, int cAlpha,
                        int dRed, int dGreen, int dBlue, int dAlpha
)
//画字符串,支持指定字体等
public void drawString(String text, Font font, int startDrawX, int startDrawY, float angle, int fontSize, double italicDegrees, int boldStrength, int red, int green, int blue, int alpha)
//画字符串,支持四个角独立颜色(渐变)
public void drawString(String text, Font font, int startDrawX, int startDrawY, float angle,
                       int fontSize, double italicDegrees, int boldStrength,
                       int aRed, int aGreen, int aBlue, int aAlpha,
                       int bRed, int bGreen, int bBlue, int bAlpha,
                       int cRed, int cGreen, int cBlue, int cAlpha,
                       int dRed, int dGreen, int dBlue, int dAlpha
)
    
//操作类

//开启裁剪,内部有裁剪栈,可以传入圆角信息等,一般直接用父类的leftTopCornerRadii等
public void enableScissor(int x, int y, int width, int height, float angle, int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii)
//关闭裁剪
public void disableScissor()
//对某个地区应用模糊
public void blurRegion(int x, int y, int width, int height, float angle, double strength)
//设置光标形状
public void setCursorShape(CursorShape cursorShapeInThisFrame)
//设置光标模式
public void setCursorMode(CursorMode cursorModeInThisFrame)

//资源类

//从硬盘加载字体
public Font loadFont(String path)
//加载内存里的图片
public Texture loadTexture(byte[] data)
//加载内存里的图片,isLinear,像素风传false,正常风传true或者直接用上面的重载
public Texture loadTexture(byte[] data, boolean isLinear)
//加载硬盘里的图片
public Texture loadTexture(String path)
//加载硬盘里的图片
public Texture loadTexture(String path, boolean isLinear)
//从内存加载九宫格纹理,stretchInner决定中心纹理是拉伸/平铺
public Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, int left, int right, int top, int bottom)
//从内存加载九宫格纹理
public Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom)
//从硬盘加载九宫格纹理
public Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, int left, int right, int top, int bottom)
//从硬盘加载九宫格纹理
public Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom)
```

!!! info

    render()的调用时机一般由宿主什么时候调用NodeRoot实例的render()方法来决定,只要你的父项不改渲染分发的delta参数那么delta永远代表本次渲染与上次渲染相隔的毫秒数

示例:一个矩形节点,不画默认背景,四角颜色不同,它仅用于演示机制,实际你可以用Frame的属性来实现
```java
public class DemoNode extends GuiNode<DemoNode> {
    @Override
    protected void render(GuiRender render, long delta) {
        render.drawRectangle(realPositionX, realPositionY,
                             realPositionMaxX, realPositionMaxY, realAngle,
                             255, 0, 0, 255,
                             0, 255, 0, 255,
                             0, 0, 255, 255,
                             255, 255, 0, 255
        );
    }

    public DemoNode(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
```

## ImageNode

ImageNode是图像的抽象基类,继承GuiNode,它内部定义了`public class ImageRenderDescription`(非static),这个类型拥有渲染一个图片所需的所有数据,所有方法返回它本身,以及getNode方法返回外部类

如果你要做的节点有图片,可以继承ImageNode

如果你要渲染ImageRenderDescription,调用render(GuiRender render)

如果你的节点有多个图片要管理,你可以像ImageButton的这三个方法设计你的API,normal,onHover,onClick,返回该状态对应的ImageRenderDescription

```java
public ImageRenderDescription normal() {
    return normal;
}
public ImageRenderDescription onHover() {
    return onHover;
}
public ImageRenderDescription onClick() {
    return onClick;
}
```

!!! info

    因为ImageRenderDescription的getNode()方法直接返回外部类,因此你只需要提供返回ImageRenderDescription的方法

示例:使用ImageRenderDescription并继承ImageNode的ImageLabel类

```java
--8<-- "core/src/main/java/top/untold/frostlumen/core/gui/node/ImageLabel.java:classDefinition"
```