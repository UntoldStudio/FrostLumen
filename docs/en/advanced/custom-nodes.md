# Custom Nodes

Custom nodes are suitable for drawing that built-in nodes cannot express. If you can compose it using existing nodes, do not create a custom one.

This chapter will introduce how to inherit from GuiNode, how to call GuiRender's methods, and how to respond to events. Which scenarios are worth customizing depends on your project. If a requirement is common enough, the library will provide a built-in node.

## How to Create

Your custom node should inherit from the `GuiNode<?>` abstract class, and fill in its own type for the generic parameter, such as `Frame extends GuiNode<Frame>`.

If you ultimately decide to create your own custom node, then you **must directly call** our rendering abstraction layer.

After your class inherits from GuiNode, you must override the `render(GuiRender render, long delta)` method, where `delta` is in milliseconds. In the current version, we do not recommend using `delta` for animations; we recommend using Tween instead. The introduction to Tween is in the previous chapter.

Your constructor should at least have `ScaleOffset position, ScaleOffset size` and call `super(position, size)`.

All your setter methods should return `this`. If you are creating an abstract class, then please define it as: `Example<T extends Example<T>> extends GuiNode<T>` and have all setters return `T` (using the parent class field `self`).

If you want to draw the default background, please call the parent class's `drawDefaultFrameBackground(GuiRender render)` method. If your background drawing is optional, please add a `drawBackground` property (field, setter, and getter). The default value is freely determined by the node type.

Example: The simplest Frame class that only calls the parent class's `drawDefaultFrameBackground`

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

## Events

Your node can listen to any events you want.

`onXxx` are hooks for your custom node to override. `dispatchXxx` (including `dispatchRender`) is the dispatch flow and generally does not need to be overridden.

If you only want to determine whether the mouse is inside the node, it is recommended to use the parent class's field `protected boolean mouseInNode`.

The parent class also helps you maintain which mouse buttons are currently clicking the node: `protected Set<MouseButton> currentMouseClickButtons`. If you want to cancel events, you can choose to override.

Example: A Button's `onMouseButtonEvent` method

```java
@Override
    protected void onMouseButtonEvent(MouseButtonEvent event) {
        if (!Collections.disjoint(canTriggerMouseButtons, currentMouseClickButtons)) { // canTriggerMouseButtons is a field of this node
            event.cancel();
            mouseButtonEventListenerRegistry.trigger(event);
        }
    }
```

Currently, there are these methods that can be overridden. After overriding, simply write any follow-up processing you want in the method body.

```java
// Cancellable events (event.cancel() is available; calling it will prevent further dispatch)
protected void onKeyEvent(KeyEvent event) {}
protected void onMouseButtonEvent(MouseButtonEvent event) {}
protected void onMouseMoveEvent(MouseMoveEvent event) {}
protected void onMouseEnterWindowEvent(MouseEnterWindowEvent event) {}
protected void onMouseLeaveWindowEvent(MouseLeaveWindowEvent event) {}
protected void onMouseScrollEvent(MouseScrollEvent event) {}
protected void onUserInputTextEvent(UserInputTextEvent event) {}
protected void onUserRequestWindowCloseEvent(UserRequestWindowCloseEvent event) {} // If it is cancelled, it will not only prevent event dispatch but also keep the window from closing
protected void onUserDropFilesEvent(UserDropFilesEvent event) {}

// Non-cancellable events
protected void onWindowMinimizeEvent(WindowMinimizeEvent event) {}
protected void onWindowMaximizeEvent(WindowMaximizeEvent event) {}
protected void onWindowMoveEvent(WindowMoveEvent event) {}
protected void onWindowCloseEvent(WindowCloseEvent event) {}
protected void onWindowFocusChangeEvent(WindowFocusChangeEvent event) {}
protected void onFrameBufferSizeChangeEvent(FrameBufferSizeChangeEvent event) {}
```

## GuiRender

GuiRender is our rendering abstraction layer. Note: **The coordinates it accepts are all absolute coordinates relative to the window.** You can directly use the parent class's `realXxx` values; they are all absolute coordinates relative to the window. As of now, you can call these methods:

Note: For method details, please see the Javadoc.

```java
// Almost every rendering/operation method has an angle parameter; generally pass the parent class's realAngle directly.

// Rendering methods

// Draw a rectangle with the same color at all four corners. For all methods below, min* generally directly uses the parent class's realPosition*, and max* generally directly uses the parent class's realPositionMax*
public void drawRectangle(int minX, int minY, int maxX, int maxY, float angle, int red, int green, int blue, int alpha)
// Draw a rectangle with independently specified four-corner colors (gradient)
public void drawRectangle(int minX, int minY, int maxX, int maxY, float angle,
                          int aRed, int aGreen, int aBlue, int aAlpha,
                          int bRed, int bGreen, int bBlue, int bAlpha,
                          int cRed, int cGreen, int cBlue, int cAlpha,
                          int dRed, int dGreen, int dBlue, int dAlpha
)
// Draw an advanced rectangle supporting rounded corners, borders, etc.
public void drawShape(int minX, int minY, int maxX, int maxY, float angle, int red, int green, int blue, int alpha,
                      int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii,
                      int aBorderThickness, int bBorderThickness, int cBorderThickness, int dBorderThickness,
                      int borderRed, int borderGreen, int borderBlue, int borderAlpha,
                      ThicknessPosition thicknessPosition
)
// Draw an advanced rectangle supporting rounded corners, borders, etc.; all four corners can have independent rounded corners and borders
public abstract void drawShape(int minX, int minY, int maxX, int maxY, float angle,
                               int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                               int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha,
                               int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii,
                               int aBorderThickness, int bBorderThickness, int cBorderThickness, int dBorderThickness,
                               int borderRed, int borderGreen, int borderBlue, int borderAlpha,
                               ThicknessPosition position
)
// Draw a triangle
public void drawTriangle(int ax, int ay, int bx, int by, int cx, int cy, float angle,
                         int aRed, int aGreen, int aBlue, int aAlpha,
                         int bRed, int bGreen, int bBlue, int bAlpha,
                         int cRed, int cGreen, int cBlue, int cAlpha
)
// Draw a triangle with a specified center point for rotation, etc.
public abstract void drawTriangle(int ax, int ay, int bx, int by, int cx, int cy, int centerX, int centerY, float angle,
                                  int aRed, int aGreen, int aBlue, int aAlpha,
                                  int bRed, int bGreen, int bBlue, int bAlpha,
                                  int cRed, int cGreen, int cBlue, int cAlpha
)
// Draw a texture. Generally, you should call the overload that accepts a Texture instead of this one.
public abstract void drawTexture(int textureId, int minX, int minY, int maxX, int maxY, int centerX, int centerY, float angle, float u0, float v0, float u1, float v1,
                                 int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                                 int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha
)
// Draw a nine-slice texture. Generally, you should call the overload that accepts a Texture (drawTexture) instead of this one.
public void drawNiceSliceTexture(NiceSliceType type, int textureId, int textureWidth, int textureHeight, int borderLeft, int borderRight, int borderTop, int borderBottom, int targetMinX, int targetMinY, int targetMaxX, int targetMaxY, float angle, float textureU0, float textureV0, float textureU3, float textureV3,
                                 int aRed, int aGreen, int aBlue, int aAlpha,
                                 int bRed, int bGreen, int bBlue, int bAlpha,
                                 int cRed, int cGreen, int cBlue, int cAlpha,
                                 int dRed, int dGreen, int dBlue, int dAlpha,
                                 boolean stretchInner
)
// Draw a texture. Internally decides whether to use nine-slice or normal rendering based on the texture information.
public void drawTexture(Texture texture, int minX, int minY, int maxX, int maxY, float angle, int red, int green, int blue, int alpha)
// Same as above, with additional support for independent four-corner colors (gradient)
public void drawTexture(Texture texture, int minX, int minY, int maxX, int maxY, float angle, float u0, float v0, float u1, float v1,
                        int aRed, int aGreen, int aBlue, int aAlpha,
                        int bRed, int bGreen, int bBlue, int bAlpha,
                        int cRed, int cGreen, int cBlue, int cAlpha,
                        int dRed, int dGreen, int dBlue, int dAlpha
)
// Draw a string, with support for specifying the font, etc.
public void drawString(String text, Font font, int startDrawX, int startDrawY, float angle, int fontSize, double italicDegrees, int boldStrength, int red, int green, int blue, int alpha)
// Draw a string with support for independent four-corner colors (gradient)
public void drawString(String text, Font font, int startDrawX, int startDrawY, float angle,
                       int fontSize, double italicDegrees, int boldStrength,
                       int aRed, int aGreen, int aBlue, int aAlpha,
                       int bRed, int bGreen, int bBlue, int bAlpha,
                       int cRed, int cGreen, int cBlue, int cAlpha,
                       int dRed, int dGreen, int dBlue, int dAlpha
)
    
// Operation methods

// Enable scissor. There is an internal scissor stack. You can pass in corner radius information, etc. Generally, directly use the parent class's leftTopCornerRadii, etc.
public void enableScissor(int x, int y, int width, int height, float angle, int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii)
// Disable scissor
public void disableScissor()
// Apply blur to a certain region
public void blurRegion(int x, int y, int width, int height, float angle, double strength)
// Set the cursor shape
public void setCursorShape(CursorShape cursorShapeInThisFrame)
// Set the cursor mode
public void setCursorMode(CursorMode cursorModeInThisFrame)

// Resource methods

// Load a font from disk
public Font loadFont(String path)
// Load an image from memory
public Texture loadTexture(byte[] data)
// Load an image from memory. For isLinear, pass false for pixel art, pass true for normal art or directly use the overload above.
public Texture loadTexture(byte[] data, boolean isLinear)
// Load an image from disk
public Texture loadTexture(String path)
// Load an image from disk
public Texture loadTexture(String path, boolean isLinear)
// Load a nine-slice texture from memory. stretchInner determines whether the center texture is stretched or tiled.
public Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, int left, int right, int top, int bottom)
// Load a nine-slice texture from memory
public Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom)
// Load a nine-slice texture from disk
public Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, int left, int right, int top, int bottom)
// Load a nine-slice texture from disk
public Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom)
```

!!! info

    The timing of `render()` calls is generally determined by when the host calls the `render()` method of the NodeRoot instance. As long as your parent does not change the delta parameter of render dispatch, `delta` always represents the number of milliseconds between this render and the previous render.

Example: A rectangle node that does not draw the default background and has different colors at the four corners. It is only used to demonstrate the mechanism; in practice, you can achieve this using Frame's properties.
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

ImageNode is the abstract base class for images. It inherits from GuiNode. Internally, it defines `public class ImageRenderDescription` (non-static). This type has all the data needed to render an image. All methods return itself, and the `getNode` method returns the outer class.

If the node you want to create has an image, you can inherit from ImageNode.

If you want to render an ImageRenderDescription, call `render(GuiRender render)`.

If your node has multiple images to manage, you can design your API like ImageButton's three methods: normal, onHover, onClick, returning the ImageRenderDescription corresponding to that state.

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

    Because ImageRenderDescription's `getNode()` method directly returns the outer class, you only need to provide methods that return ImageRenderDescription.

Example: The ImageLabel class that uses ImageRenderDescription and inherits from ImageNode

```java
--8<-- "core/src/main/java/top/untold/frostlumen/core/gui/node/ImageLabel.java:classDefinition"
```