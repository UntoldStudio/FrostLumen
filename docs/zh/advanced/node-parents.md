# 中间层节点父类

本章节将会讲解几个节点的中间层抽象类,它们将提供某些方面的便捷API/类以供你使用

## ImageNode

ImageNode是图像节点的抽象基类,继承GuiNode,它内部定义了`public class ImageRenderDescription`(非static),这个类型拥有渲染一个图片所需的所有数据,所有方法返回它本身,以及getNode方法返回外部类

如果你要做的节点有图片,可以继承ImageNode

如果你要渲染ImageRenderDescription,调用`render(GuiRender render)`

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

## TextNode

TextNode是文字节点的抽象基类,继承GuiNode,它内部定义了

`public abstract class AbstractTextRenderDescription`

`public class TextRenderDescription`

`public class CanWrappedTextRenderDescription`

AbstractTextRenderDescription是非静态抽象基类,持有一个文字渲染所需的所有数据,比如字体,使用的相对于节点的对齐等

TextRenderDescription仅仅继承AbstractTextRenderDescription,泛型填TextRenderDescription,不添加额外行为

CanWrappedTextRenderDescription增加了`wrap`字段,`setWrap`与`isWrap`方法,用于控制自动换行

如果你要制作的节点要渲染文本,可以继承TextNode

!!! warning

    CanWrappedTextRenderDescription会根据\n与节点宽度,各个字符的宽度自动换行,而TextRenderDescription就算遇见\n也不换行,且会渲染为notdef

如果你要渲染它们,调用`render(GuiRender render)`

!!! info

    因为AbstractTextRenderDescription的getNode()方法直接返回外部类,因此你只需要提供返回RenderDescription的方法

!!! info

    TextLabel使用了CanWrappedTextRenderDescription,而TextBox的输入框则使用了TextRenderDescription

## 其他

你可能会疑惑,我无法同时继承TextNode和ImageNode,那我怎么制作又有Text又有Image的节点?

答案是:如果你真的要同时渲染它们,我们推荐你使用组合节点的方式

比如一个Frame当容器,有一个TextLabel和ImageLabel作为子节点

我们十分不推荐你仅仅为了管理子节点而继承GuiNode
