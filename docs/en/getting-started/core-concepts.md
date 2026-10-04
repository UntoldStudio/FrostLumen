# Core Concepts

This chapter will introduce several core concepts of this library.

## Basics

In this library, realXxx and xxx (where xxx is a field name) will frequently appear. Generally, realXxx is the value actually passed to the GuiRender renderer, and it is an absolute value relative to the entire window. xxx is the user's input value. For performance reasons, realXxx is generally a scalar.

This library does not use window size at all; the window size mentioned in the documentation refers to the FrameBuffer size.

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

The above 7 values are all in pixels except realAngle, which is in degrees, and the results are rounded during calculation.

NodeRoot sets `realPositionX`, `realPositionY`, and `realAngle` to zero, and sets `realSizeX`, `realSizeY`, `realPositionMaxX`, and `realPositionMaxY` to the window size.

GuiNode recalculates these 7 values when it is initialized, when the parent requires recalculation, when the parent is changed, or when position/size/angle is changed. After calculating them itself, it recursively causes its children to recalculate as well.

They all have self-referencing generics and are not builders. All setters return the self field, where self is defined as (T) this, so you can chain calls at any time.

## ScaleOffset

ScaleOffset is a highly important class in this library. It is an immutable Java record, defined as:

```java
public record ScaleOffset(double xScale, int xOffset, double yScale, int yOffset) {...}
```

The position and size types of nodes are both this.

There are 3 ways to construct it:

`new ScaleOffset(xScale, xOffset, yScale, yOffset)`

`ScaleOffset.fromScale(xScale, yScale)`

`ScaleOffset.fromOffset(xOffset, yOffset)`

And a fixed constant ZERO.

The final size of a node is determined by its fields. The algorithm is roughly:

`parent.realSize * Scale + Offset`

The final position of a node is also determined by its fields. The algorithm is roughly:

`parent.realPosition + parent.realSize * Scale + Offset`

(Note: The final position is also affected by an additional Anchor, see below)

(Note: The final size of NodeRoot is the window size, and the coordinate origin and realPosition are located at 0,0)

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

It is not a record class, but consists of two double values: xAnchor, yAnchor.

It generally consists of 2 components in the range 0-1. Exceeding this range is also legal, but may cause unexpected behavior, such as rendering outside the window.

Assuming x and y have been calculated according to the formulas above, then realPositionX and realPositionY are:

`x - this.realSizeX * xAnchor -> realPositionX`

`y - this.realSizeY * yAnchor -> realPositionY`

All GuiNode children have these public methods:

`setAnchor(double xAnchor, double yAnchor)`

`setXAnchor(double xAnchor)`

`setYAnchor(double yAnchor)`

And these protected fields:
`double xAnchor`
`double yAnchor`

For example:

```java
node.setAnchor(0, 0); // Located at the top-left corner of the shape
node.setAnchor(0.5, 0.5); // Located at the center of the shape
node.setAnchor(1, 1); // Located at the bottom-right corner of the shape
```

## angle and realAngle

They control the clockwise angle when the node is rendered.

The rotation center is actually the center of the shape, not the point where the Anchor is.

If you directly call GuiRender's method for drawing triangles, the rotation center there is the triangle's centroid.

realAngle is calculated by adding its own angle to the parent's realAngle.

All GuiNode children have the `setAngle(float angle)` method.

For example:

```java
node.setAngle(90f); // Rotate 90 degrees clockwise
```