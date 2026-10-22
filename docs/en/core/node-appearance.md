# Node Appearance

This chapter will explain the common properties that every GuiNode has.

## ClipChildren

Type: boolean

Default: false

When enabled, all children will be restricted to render only within the parent's border. ClipChildren takes the node's corner radius into account and will rotate the clipping region along with the node's angle.

When enabled, hit testing will also consider this factor along the parent chain. In other words, as long as the player's mouse is not actually over a visible part of the node, hit testing will return false.

Example:

```java
node.setClipChildren(true);
```

## DrawBackground

Type: boolean

Default: varies; some nodes are true, some are false

Most nodes have this property. If a node has this property, it must have a corresponding setter.

Generally, nodes that have this property implement the HasDrawBackground interface.

If this value is false, the entire background will not be rendered.

If you do not see a background and the node has a drawBackground property, first consider setting it to true.

## ZIndex

Type: int

Default: 0

As the name suggests, it determines the order in which a node is rendered and receives event dispatch under the same parent. The larger the number, the higher it renders and the earlier it receives events.

Example: Set node's ZIndex to 100

```java
node.setZIndex(100);
```

## Background

background is a field that every node has, but if the subclass does not expose the ability to draw a background, setting it has no effect.
Background configuration items are accessed through the `background()` method of the GuiNode base class. You can return to the node via the background configuration's `getNode()` method.

Example: Configure the background

```java
Frame frame = new Frame(ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5))
        .background()
        .setColor(new RGBA(210, 230, 255, 90))
        .setCornerRadius(10)
        .getNode();
```

### BackgroundBlurStrength

Type: double

Default: 0

Range: 0.1-1 recommended; the actual value will be clamped to 0-2 on the renderer side

When enabled, Gaussian blur will be applied to all pixels of the background, and the region will rotate along with the node's angle.

If Alpha=255, no matter what value is set, it will have no effect, because an opaque node already directly covers the area, and blurring the pixels behind it is meaningless.

Example:

```java
background.setBlurStrength(0.7);
```

### BackgroundColor

Type: RGBA

Default: RGBA.WHITE

The background color.

Example:

```java
background.setColor(new RGBA(210, 230, 255, 90));
```

### BackgroundBorder

The BackgroundBorder property actually consists of 6 independent components of different types.

Of type int:
`leftBorderThickness`
`rightBorderThickness`
`topBorderThickness`
`bottomBorderThickness`

(The four values above must be >=0; otherwise, unexpected behavior may occur. The default values are all 0, and the unit is pixels.)

(If you do not want to call the setters for these four one by one, you can use `setBorderThickness(int thickness)` to set all four at once.)

Of type RGBA:

`borderColor`

(Default: RGBA.BLACK)

Of type ThicknessPosition:

`borderThicknessPosition`

(Default: OUTSIDE)

The four int values correspond to the border thickness of the four sides of the background, the RGBA value determines the border color, and the ThicknessPosition determines where the border is drawn.

ThicknessPosition has three values:

OUTSIDE (rendered outside the background)

INSIDE (rendered inside the background)

CENTER (rendered at the center of the background's border)

The values above are borders, so even if the node rotates, they will stay tightly attached to the background.

Example: Set a blue border 5 pixels thick, rendered inside the background, using the chained API

```java
background.setBorderThickness(5)
    .setBorderColor(RGBA.BLUE)
    .setBorderThicknessPosition(ThicknessPosition.INSIDE);
```

### BackgroundCornerRadius

It consists of 4 independent components of type int:

`leftTopCornerRadius`
`rightTopCornerRadius`
`leftBottomCornerRadius`
`rightBottomCornerRadius`

(They must be >=0; otherwise, unexpected behavior may occur. The default values are all 0, and the unit is the corner radius in pixels.)

(If you do not want to call the setters one by one, you can use `setCornerRadius(int radius)` to set all four at once.)

Example: Set the top-left corner radius to 5, bottom-right to 20, top-right to 30, bottom-left to 0, using the chained API

```java
background.setLeftTopCornerRadius(5)
    .setRightTopCornerRadius(30)
    .setLeftBottomCornerRadius(0)
    .setRightBottomCornerRadius(20);
```