# Intermediate Layer Node Parent Classes

This chapter will explain several intermediate-layer abstract classes for nodes. They provide convenient APIs/classes in certain aspects for you to use.

## ImageNode

ImageNode is the abstract base class for image nodes and inherits from GuiNode. It internally defines `public class ImageRenderDescription` (non-static). This type holds all the data needed to render an image. All its methods return the object itself, and it has a `getNode` method that returns the outer class.

If the node you want to make has an image, you can inherit from ImageNode.

If you want to render an ImageRenderDescription, call `render(GuiRender render)`.

If your node has multiple images to manage, you can design your API like these three methods of ImageButton: `normal`, `onHover`, and `onClick`, returning the ImageRenderDescription corresponding to that state.

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

Example: The ImageLabel class, which uses ImageRenderDescription and inherits from ImageNode

```java
--8<-- "core/src/main/java/top/untold/frostlumen/core/gui/node/ImageLabel.java:classDefinition"
```

## TextNode

TextNode is the abstract base class for text nodes and inherits from GuiNode. It internally defines:

`public abstract class AbstractTextRenderDescription`

`public class TextRenderDescription`

`public class CanWrappedTextRenderDescription`

AbstractTextRenderDescription is a non-static abstract base class that holds all the data needed for text rendering, such as the font and the alignment used relative to the node.

TextRenderDescription only inherits from AbstractTextRenderDescription, fills the generic with TextRenderDescription, and adds no extra behavior.

CanWrappedTextRenderDescription adds the `wrap` field and the `setWrap` and `isWrap` methods, used to control automatic line wrapping.

If the node you want to make needs to render text, you can inherit from TextNode.

!!! warning

    CanWrappedTextRenderDescription automatically wraps lines based on `\n` and the node width and the width of each character, while TextRenderDescription does not wrap even when it encounters `\n`, and it will be rendered as notdef.

If you want to render them, call `render(GuiRender render)`.

!!! info

    Because AbstractTextRenderDescription's `getNode()` method directly returns the outer class, you only need to provide methods that return RenderDescription.

!!! info

    TextLabel uses CanWrappedTextRenderDescription, while TextBox's input field uses TextRenderDescription.

## Other Interfaces

### InputBlocker

Indicates that the user can accept events themselves. The `AbstractInputBlocker` base class provides a default implementation.

### ButtonNode

Indicates that this node is a button.

### HasDrawBackground

Indicates that this node has a `drawBackground` field. In other words, drawing the background is optional.

## Others

You may wonder: I cannot inherit from both TextNode and ImageNode at the same time, so how do I make a node that has both Text and Image?

The answer is: if you really need to render both at the same time, we recommend using a composite node approach.

For example, use a Frame as the container, with a TextLabel and an ImageLabel as child nodes.

We strongly do not recommend inheriting from GuiNode solely for the purpose of managing child nodes.