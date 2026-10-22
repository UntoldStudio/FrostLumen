# Custom Nodes

Custom nodes are suitable for drawing that built-in nodes cannot express. If you can compose existing nodes to achieve it, do not create a custom node.

This chapter will introduce how to inherit from `GuiNode`, how to call `GuiRender` methods, and how to respond to events. Which scenarios are worth customizing depends on your project. If a requirement is common enough, the library will provide a built-in node.

## How to Make One

Your custom node should inherit from the `GuiNode<?>` abstract class, and the generic parameter should be your own type, e.g. `Frame extends GuiNode<Frame>`.

If you ultimately decide to make your own custom node, then you **must directly call** our rendering abstraction layer.

After your class inherits `GuiNode`, it must override the `render(GuiRender render, double delta)` method, where `delta` is in milliseconds. In the current version, we do not recommend using `delta` for animation; we recommend using Tween instead. Tween is introduced in the previous chapter.

Your constructor should at least have `ScaleOffset position, ScaleOffset size` and call `super(position, size)`.

All your setter methods should return `this`. If you are making an abstract class, please define it as: `Example<T extends Example<T>> extends GuiNode<T>` and have all setters return `T` (using the parent class field `self`).

If you want to draw the default background, please call the `render` method of the parent class field `background`. If your background drawing is optional, please add a `drawBackground` property (field, setter, and getter) and implement the `HasDrawBackground` interface, filling the generic with your own type. The default value is freely determined by the node type.

Example: The simplest `Frame` class that only calls the `render` method of the parent class `background` field

```java
public class Frame extends GuiNode<Frame> {
    @Override
    protected void render(GuiRender render, double delta) {
        background.render(render);
    }

    public Frame(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
```

Example: A `DemoFrame` class with optional background drawing

```java
public class DemoFrame extends GuiNode<DemoFrame> implements HasDrawBackground<DemoFrame> {
    private boolean drawBackground = false;
    
    @Override
    public DemoFrame setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
        return self;
    }
    @Override
    public boolean isDrawBackground() {
        return drawBackground;
    }
    
    @Override
    protected void render(GuiRender render, double delta) {
        if (drawBackground) {
            background.render(render);
        }
    }

    public DemoFrame(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
```

## Events

Your node can listen to any events you want.

`onXxx` are hooks for your custom node to override. `dispatchXxx` (including `dispatchRender`) is the dispatch flow and generally does not need to be overridden.

If you only want to determine whether the mouse is inside the node, it is recommended to use the parent class field `protected boolean mouseInNode`.

The parent class also maintains for you which mouse buttons are currently clicking the node: `protected Set<MouseButton> currentMouseClickButtons`. If you want to cancel events, you can choose to override.

Example: A certain Button's `onMouseButtonEvent` method

```java
@Override
    protected void onMouseButtonEvent(MouseButtonEvent event) {
        if (!Collections.disjoint(canTriggerMouseButtons, currentMouseClickButtons)) { // canTriggerMouseButtons is the node's own field
            event.cancel();
            mouseButtonEventListenerRegistry.trigger(self, event); // self is a field inherited from ParentNode, of type T
        }
    }
```

Currently, the following methods can be overridden. After overriding, simply write any subsequent processing you want in the method body.

```java
// Cancelable events (event.cancel() is available; calling it prevents subsequent dispatch)
protected void onKeyEvent(KeyEvent event) {}
protected void onMouseButtonEvent(MouseButtonEvent event) {}
protected void onMouseMoveEvent(MouseMoveEvent event) {}
protected void onMouseEnterWindowEvent(MouseEnterWindowEvent event) {}
protected void onMouseLeaveWindowEvent(MouseLeaveWindowEvent event) {}
protected void onMouseScrollEvent(MouseScrollEvent event) {}
protected void onUserInputTextEvent(UserInputTextEvent event) {}
protected void onUserRequestWindowCloseEvent(UserRequestWindowCloseEvent event) {} // If canceled, it not only prevents event dispatch but also prevents the window from closing
protected void onUserDropFilesEvent(UserDropFilesEvent event) {}

// Non-cancelable events
protected void onWindowMinimizeEvent(WindowMinimizeEvent event) {}
protected void onWindowMaximizeEvent(WindowMaximizeEvent event) {}
protected void onWindowMoveEvent(WindowMoveEvent event) {}
protected void onWindowCloseEvent(WindowCloseEvent event) {}
protected void onWindowFocusChangeEvent(WindowFocusChangeEvent event) {}
protected void onFrameBufferSizeChangeEvent(FrameBufferSizeChangeEvent event) {}
protected void onPreeditEvent(PreeditEvent event) {} // IME preedit event
protected void onRelayout() {} // Called when layout needs to be recalculated
protected void onWindowFocusNodeChangeEvent(ParentNode<?> oldFocusNode, ParentNode<?> newFocusNode) {}
protected void tick() {} // Fixed 60 times per second; if the frame rate is below 60, it will catch up at the beginning of each render
protected void init(GuiRender render) {} // Triggered when the node first joins the node tree; at this point parent and root will absolutely not be null, and this event will not be triggered a second time
```

!!! warning

    Some parent nodes such as `TextNode`, `ImageNode`, and `LayoutBox` override `onRelayout` themselves, so please call `super.onRelayout()` once.

## GuiRender

`GuiRender` is our rendering abstraction layer. Note: **The coordinates it accepts are all absolute coordinates relative to the window.** You can directly use the parent class's `realXxx`; they are all absolute coordinates relative to the window. As of now, you can call these methods:

Note: For method details, please see the Javadoc.

```java
// Almost every rendering/operation method has an angle parameter; generally pass the parent class's realAngle directly.

// Rendering methods

// Draw a rectangle with the same color at all four corners. For all min* below, generally pass the parent class's realPosition*; for max*, generally pass the parent class's realPositionMax*
public void drawRectangle(int minX, int minY, int maxX, int maxY, float angle, int red, int green, int blue, int alpha)
// Draw a rectangle with independently specified corner colors (gradient)
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
// Draw an advanced rectangle supporting rounded corners, borders, etc., with independent rounded corners and borders at all four corners
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
// Draw a texture. Generally, you should call the overload that accepts Texture rather than this one.
public abstract void drawTexture(int textureId, int minX, int minY, int maxX, int maxY, int centerX, int centerY, float angle, float u0, float v0, float u1, float v1,
                                 int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                                 int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha
)
// Draw a nice-slice texture. Generally, you should call the overload that accepts Texture (drawTexture) rather than this one.
public void drawNiceSliceTexture(NiceSliceType type, int textureId, int textureWidth, int textureHeight, int borderLeft, int borderRight, int borderTop, int borderBottom, int targetMinX, int targetMinY, int targetMaxX, int targetMaxY, float angle, float textureU0, float textureV0, float textureU3, float textureV3,
                                 int aRed, int aGreen, int aBlue, int aAlpha,
                                 int bRed, int bGreen, int bBlue, int bAlpha,
                                 int cRed, int cGreen, int cBlue, int cAlpha,
                                 int dRed, int dGreen, int dBlue, int dAlpha,
                                 boolean stretchInner
)
// Draw a texture; internally decides whether to use nice-slice or normal rendering based on texture information
public void drawTexture(Texture texture, int minX, int minY, int maxX, int maxY, float angle, int red, int green, int blue, int alpha)
// Same as above, additionally supporting independent corner colors (gradient)
public void drawTexture(Texture texture, int minX, int minY, int maxX, int maxY, float angle, float u0, float v0, float u1, float v1,
                        int aRed, int aGreen, int aBlue, int aAlpha,
                        int bRed, int bGreen, int bBlue, int bAlpha,
                        int cRed, int cGreen, int cBlue, int cAlpha,
                        int dRed, int dGreen, int dBlue, int dAlpha
)
// Draw a string, supporting specified fonts, etc.
public void drawString(String text, Font font, int startDrawX, int startDrawY, float angle, int fontSize, double italicDegrees, int boldStrength, int red, int green, int blue, int alpha)
// Draw a string, supporting a specified rotation center point
public void drawString(String text, Font font, int startDrawX, int startDrawY, int centerX, int centerY, float angle, int fontSize, double italicDegrees, int boldStrength, int red, int green, int blue, int alpha)
// Draw a string, supporting independent corner colors (gradient)
public void drawString(String text, Font font, int startDrawX, int startDrawY, float angle,
                       int fontSize, double italicDegrees, int boldStrength,
                       int aRed, int aGreen, int aBlue, int aAlpha,
                       int bRed, int bGreen, int bBlue, int bAlpha,
                       int cRed, int cGreen, int cBlue, int cAlpha,
                       int dRed, int dGreen, int dBlue, int dAlpha
)
// Draw a string, supporting independent corner colors (gradient) and a rotation center point
public void drawString(String text, Font font, int startDrawX, int startDrawY, int centerX, int centerY, float angle,
                       int fontSize, double italicDegrees, int boldStrength,
                       int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                       int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha
)
    
// Operation methods

// Enable clipping. There is an internal scissor stack, and you can pass in corner radius information, etc. Generally, directly use the parent class's leftTopCornerRadii, etc.
public void enableScissor(int x, int y, int width, int height, float angle, int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii)
// Disable clipping
public void disableScissor()
// Apply blur to a certain region
public void blurRegion(int x, int y, int width, int height, float angle, double strength)
// Set cursor shape
public void setCursorShape(CursorShape cursorShapeInThisFrame)
// Set cursor mode
public void setCursorMode(CursorMode cursorModeInThisFrame)

// Resource methods

// Load a font from disk
public Font loadFont(String path)
// Load an image from memory
public Texture loadTexture(byte[] data)
// Load an image from memory; for isLinear, pass false for pixel art, true for normal style, or directly use the overload above
public Texture loadTexture(byte[] data, boolean isLinear)
// Load an image from disk
public Texture loadTexture(String path)
// Load an image from disk
public Texture loadTexture(String path, boolean isLinear)
// Load a nice-slice texture from memory; stretchInner determines whether the center texture is stretched/tiled
public Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, int left, int right, int top, int bottom)
// Load a nice-slice texture from memory
public Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom)
// Load a nice-slice texture from disk
public Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, int left, int right, int top, int bottom)
// Load a nice-slice texture from disk
public Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom)
// Unload texture resources
public void unloadTexture(Texture texture)
// Unload font resources
public void unloadFont(Font font)
```

!!! info

    The timing of `render()` calls is generally determined by when the host calls the `render()` method of the `NodeRoot` instance. As long as your parent does not change the `delta` parameter of render dispatch, `delta` always represents the number of milliseconds between this render and the previous render.

Example: A rectangle node that does not draw the default background and has different colors at the four corners. It is only for demonstrating the mechanism; in practice, you can implement it using `Frame` properties.
```java
public class DemoNode extends GuiNode<DemoNode> {
    @Override
    protected void render(GuiRender render, double delta) {
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