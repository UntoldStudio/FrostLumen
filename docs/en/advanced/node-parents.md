# Intermediate-Layer Node Base Classes

This chapter will explain several intermediate-layer abstract classes for nodes. They provide convenient APIs/classes in certain aspects for you to use.

## ImageNode

ImageNode is the abstract base class for image nodes. It inherits from GuiNode. Internally, it defines `public class ImageRenderDescription` (non-static). This type has all the data needed to render an image. All methods return itself, and the `getNode` method returns the outer class.

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

## TextNode

TextNode is the abstract base class for text nodes. It inherits from GuiNode. Internally, it defines

`public abstract class AbstractTextRenderDescription`

`public class TextRenderDescription`

`public class CanWrappedTextRenderDescription`

AbstractTextRenderDescription is a non-static abstract base class. It holds all the data needed to render text, such as the font and the alignment used relative to the node.

TextRenderDescription only inherits from AbstractTextRenderDescription, fills in TextRenderDescription for the generic parameter, and does not add any additional behavior.

CanWrappedTextRenderDescription adds a `wrap` field, along with the `setWrap` and `isWrap` methods, used to control automatic line wrapping.

If the node you want to create needs to render text, you can inherit from TextNode.

!!! warning

    CanWrappedTextRenderDescription automatically wraps lines according to `\n` and the node width and the width of each character, while TextRenderDescription does not wrap even when it encounters `\n`, and will render it as notdef.

If you want to render them, call `render(GuiRender render)`.

!!! info

    Because AbstractTextRenderDescription's `getNode()` method directly returns the outer class, you only need to provide methods that return a RenderDescription.

!!! info

    TextLabel uses CanWrappedTextRenderDescription, while the input box of TextBox uses TextRenderDescription.

## Others

You may be wondering: I cannot inherit from both TextNode and ImageNode, so how do I create a node that has both Text and Image?

The answer is: if you really need to render both at the same time, we recommend using a composite node approach.

For example, use a Frame as a container, with a TextLabel and an ImageLabel as child nodes.

We strongly do not recommend inheriting from GuiNode merely for the purpose of managing child nodes.