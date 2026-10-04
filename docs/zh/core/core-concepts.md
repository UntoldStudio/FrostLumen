# 核心概念

本章节将会介绍本库的几个核心概念

## 基础

本库会频繁出现realXxx与xxx(xxx为字段名),realXxx一般来说才是真正传给GuiRender渲染器的值,并且是相对于整个窗口的绝对值,xxx是用户的输入值,realXxx为了性能一般均为标量

本库完全不使用窗口大小,文档提到的窗口大小均为FrameBuffer大小

## 继承链

```text
ParentNode(sealed)
    -NodeRoot(final)
    -GuiNode(non-sealed)
        -Frame
        -TextLabel
        -ImageNode
            -ImageLabel
            ...
        ...
```

ParentNode拥有这些重要的protected字段:

`int realPositionX`

`int realPositionY`

`int realPositionMaxX`

`int realPositionMaxY`

`int realSizeX`

`int realSizeY`

`float realAngle`

上列7个值除了realAngle单位为角度外均为像素,且计算时结果会取整

NodeRoot会把`realPositionX` `realPositionY` `realAngle`置零, 把`realSizeX` `realSizeY` `realPositionMaxX` `realPositionMaxY` 设置为窗口大小

GuiNode会在初始化,父项要求重算,改变父项或者更改position/size/angle的时候重新算这7个值,并且自己算完后递归子项让它们重算

它们都拥有自引用泛型,且不是builder,所有setter返回self字段,self定义为(T) this,因此你可以在任何时候链式调用它

## ScaleOffset

ScaleOffset是本库重要性较高的类.它是一个不可变记录类,几乎所有节点的构造函数都要求ScaleOffset类型的position和size,定义为:

```java
public record ScaleOffset(double xScale, int xOffset, double yScale, int yOffset) {...}
```

节点的位置和大小的类型都是它

构造方式有3种:

`new ScaleOffset(xScale, xOffset, yScale, yOffset)`

`ScaleOffset.fromScale(xScale, yScale)`

`ScaleOffset.fromOffset(xOffset, yOffset)`

以及一个固定的常量ZERO

一个节点的最终大小由他的字段决定,算法大致为:

`parent.realSize * Scale + Offset`

一个节点的最终位置也是由他的字段决定,算法大致为:

`parent.realPosition + parent.realSize * Scale + Offset`
(注意:最终位置还有额外的Anchor影响,请见下文)

(注意:NodeRoot的最终大小即为窗口大小,坐标原点和realPosition位于0,0)

所有GuiNode子项都有这些public方法:

`setPosition(ScaleOffset position)`

`setSize(ScaleOffset size)`

ScaleOffset有许多方法,比如

with系列:
withX
withY
withScale
withOffset
withXScale
withXOffset
...

add系列:
addScale
addOffset
addX
addY
addXScale
addXOffset
...

## Anchor

一个节点拥有Anchor属性,它会影响节点的最终坐标

它不是记录类,而是由两个double值组成:xAnchor, yAnchor

它一般由2个0-1的分量组成,超出该范围也是合法的,但是可能会造成非预期行为,例如渲染到了窗口之外

假设已经按照上文中的公式算出了x和y,那么realPositionX和realPositionY则是:

`x - this.realSizeX * xAnchor -> realPositionX`

`y - this.realSizeY * yAnchor -> realPositionY`

所有GuiNode的子项都有这些public方法

`setAnchor(double xAnchor, double yAnchor)`

`setXAnchor(double xAnchor)`

`setYAnchor(double yAnchor)`

以及这些protected字段
`double xAnchor`
`double yAnchor`

例如:

```java
node.setAnchor(0, 0); //位于图形左上角
node.setAnchor(0.5, 0.5); //位于图形中心
node.setAnchor(1, 1); //位于图形右下角
```

## Angle与RealAngle

它们控制节点渲染时候的顺时针角度

旋转中心实际为图形的中心而非Anchor所在点

如果你直接调用了GuiRender绘制三角形的方法,那里的旋转中心是三角形重心

realAngle是通过自己的angle+父项的realAngle算出来的

所有GuiNode的子项都有`setAngle(float angle)`方法

例如:

```java
node.setAngle(90f); //顺时针旋转90度
```

## RGBA

RGBA是一个不可变记录类,定义为:

```java
public record RGBA(int red, int green, int blue, int alpha) {...}
```

注意:这四个值的范围必须全部是0-255, 否则会出现非预期行为

Alpha大致等于不透明度,值为0则完全透明,值为255则完全不透明,只是范围从0-1换成了0-255

它代表一种颜色
API几乎全部用他表示一个颜色,例如GuiNode的backgroundColor
它有withRed,withGreen,withBlue,withAlpha这四个方法

构造方式有1种:

`new RGBA(red, green, blue, alpha)`

或者使用RGBA类定义的常量:
`WHITE`
`BLACK`
`RED`
`GREEN`
`BLUE`
`GRAY`
`TRANSPARENT`

示例:设置某node的背景色为不透明橙色

```java
node.setBackgroundColor(new RGBA(255, 128, 0, 255));
```