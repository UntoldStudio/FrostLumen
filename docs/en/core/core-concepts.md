# Core Concepts

This chapter will introduce several core concepts of this library.

## Basics

In this library, you will frequently see `realXxx` and `xxx` (where `xxx` is a field name). Generally, `realXxx` is the value actually passed to the `GuiRender` renderer, and it is an absolute value relative to the entire window. `xxx` is the user-provided input value. For performance reasons, `realXxx` is generally a scalar.

This library does not use window size at all. Any window size mentioned in the documentation refers to the framebuffer size.

## Inheritance Chain

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

ParentNode has these important protected fields:

`int realPositionX`

`int realPositionY`

`int realPositionMaxX`

`int realPositionMaxY`

`int realSizeX`

`int realSizeY`

`float realAngle`

Of the 7 values above, all are in pixels except `realAngle`, which is in degrees, and the result is rounded during calculation.

NodeRoot sets `realPositionX`, `realPositionY`, and `realAngle` to zero, and sets `realSizeX`, `realSizeY`, `realPositionMaxX`, and `realPositionMaxY` to the window size.

GuiNode recalculates these 7 values when it is initialized, when the parent requests recalculation, or when the parent is changed or position/size/angle is changed. After calculating them itself, it recursively asks its children to recalculate.

They all have self-referential generics and are not builders. All setters return the `self` field, where `self` is defined as `(T) this`, so you can chain calls on it at any time.

## ScaleOffset

ScaleOffset is a relatively important class in this library. It is an immutable record class, defined as:

```java
public record ScaleOffset(double xScale, int xOffset, double yScale, int yOffset) {...}
```

The type of a node's position and size is both this.

There are 3 ways to construct it:

`new ScaleOffset(xScale, xOffset, yScale, yOffset)`

`ScaleOffset.fromScale(xScale, yScale)`

`ScaleOffset.fromOffset(xOffset, yOffset)`

and a fixed constant `ZERO`.

A node's final size is determined by its fields, and the algorithm is roughly:

`parent.realSize * Scale + Offset`

A node's final position is also determined by its fields, and the algorithm is roughly:

`parent.realPosition + parent.realSize * Scale + Offset`
(Note: the final position is also affected by an additional Anchor; see below.)

(Note: NodeRoot's final size is the window size, and the coordinate origin and `realPosition` are located at `0,0`.)

All GuiNode children have these public methods:

`setPosition(ScaleOffset position)`

`setSize(ScaleOffset size)`

ScaleOffset has many methods, such as

with series:
withX
withY
withScale
withOffset
withXScale
withXOffset
...

add series:
addScale
addOffset
addX
addY
addXScale
addXOffset
...

## Anchor

A node has an Anchor property, which affects the node's final coordinates.

It is not a record class, but consists of two double values: `xAnchor`, `yAnchor`.

It generally consists of 2 components in the range 0-1. Values outside this range are also legal, but may cause unexpected behavior, such as rendering outside the window.

Assuming `x` and `y` have already been calculated according to the formulas above, then `realPositionX` and `realPositionY` are:

`x - this.realSizeX * xAnchor -> realPositionX`

`y - this.realSizeY * yAnchor -> realPositionY`

All GuiNode children have these public methods:

`setAnchor(double xAnchor, double yAnchor)`

`setXAnchor(double xAnchor)`

`setYAnchor(double yAnchor)`

and these protected fields:

`double xAnchor`
`double yAnchor`

For example:

```java
node.setAnchor(0, 0); // Located at the top-left corner of the shape
node.setAnchor(0.5, 0.5); // Located at the center of the shape
node.setAnchor(1, 1); // Located at the bottom-right corner of the shape
```

## Angle and RealAngle

They control the clockwise angle when the node is rendered.

The rotation center is actually the center of the shape, not the Anchor point.

If you directly call GuiRender's triangle drawing method, the rotation center there is the triangle's centroid.

`realAngle` is calculated from its own `angle` + the parent's `realAngle`.

All GuiNode children have the `setAngle(float angle)` method.

For example:

```java
node.setAngle(90f); // Rotate clockwise by 90 degrees
```

## RGBA

RGBA is an immutable record class, defined as:

```java
public record RGBA(int red, int green, int blue, int alpha) {...}
```

Note: The range of these four values must all be 0-255; otherwise, unexpected behavior may occur.

Alpha is roughly equivalent to opacity. A value of 0 is completely transparent, and a value of 255 is completely opaque; the range is simply changed from 0-1 to 0-255.

It represents a color.

Almost all APIs use it to represent a color, for example GuiNode's `backgroundColor`.

It has four methods: `withRed`, `withGreen`, `withBlue`, and `withAlpha`.

There are 1 ways to construct it:

`new RGBA(red, green, blue, alpha)`

or use constants defined by the RGBA class:
`WHITE`
`BLACK`
`RED`
`GREEN`
`BLUE`
`GRAY`
`TRANSPARENT`

Example: Set a node's background color to opaque orange

```java
node.setBackgroundColor(new RGBA(255, 128, 0, 255));
```