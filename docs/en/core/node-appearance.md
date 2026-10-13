# Node Appearance

This chapter will explain the common properties that every GuiNode has.

## ClipChildren

Type: boolean

Default value: false

When enabled, all children will be restricted to rendering only within the parent's border. ClipChildren takes the node's corner radius into account, and the clipping region rotates with the node's angle.

When enabled, hit testing will also take this factor along the parent chain into account. In other words, as long as the player's mouse is not actually over a visible part of the node, hit testing will return false.

Example:

```java
node.setClipChildren(true);
```

## DrawBackground

Type: boolean

Default value: varies; true for some nodes, false for others.

Most nodes have this property. If a node has this property, then it must have a corresponding setter.

Nodes that have this property generally implement the `HasDrawBackground` interface.

If this value is false, the entire background will not be rendered.

If you do not see a background, and the node has the drawBackground property, first consider setting it to true.

## Background

`background` is a field that every node has, but if a subclass does not expose the ability to draw the background, setting it will have no effect.

Background configuration items are accessed through the `background()` method of the GuiNode base class. You can return to the node via the background configuration item's `getNode()` method.

Example: Configuring the background

```java
Frame frame = new Frame(ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5))
                .background()
                .setColor(new RGBA(210, 230, 255, 90))
                .setCornerRadius(10)
                .getNode();
```

### BackgroundBlurStrength

Type: double

Default value: 0

Range: 0.1-1 recommended; the actual value will be clamped to 0-2 on the renderer side.

When enabled, Gaussian blur will be applied to all pixels in the background, and the blur region rotates with the node's angle.

If Alpha=255, it will have no effect no matter what value is set, because an opaque node already directly covers the pixels behind it, so blurring the pixels behind it is meaningless.

Example:

```java
background.setBlurStrength(0.7);
```

### BackgroundColor

Type: RGBA

Default value: RGBA.WHITE

The background color.

Example:

```java
background.setColor(new RGBA(210, 230, 255, 90));
```

### BackgroundBorder

The BackgroundBorder property is actually composed of 6 independent components of different types.

The four of type int:
`leftBorderThickness`
`rightBorderThickness`
`topBorderThickness`
`bottomBorderThickness`

(The four values above must be >= 0; otherwise, unexpected behavior may occur. Their default values are all 0, and the unit is pixels.)

(If you do not want to call the setters for these four one by one, you can use `setBorderThickness(int thickness)` to set all four at once.)

The one of type RGBA:

`borderColor`

(Default value is RGBA.BLACK)

The one of type ThicknessPosition:

`borderThicknessPosition`

(Default value is OUTSIDE)

The four int values correspond to the border thickness of the four sides of the background, the RGBA value determines the border color, and the ThicknessPosition determines where the border is.

ThicknessPosition has three values:

OUTSIDE (rendered outside the background)

INSIDE (rendered inside the background)

CENTER (rendered at the center of the background border)

The above values are all borders, so even if the node rotates, they will stay attached to the background.

Example: Set a blue border with a thickness of 5 pixels, rendered inside the background, using the chained API.

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

(They must be >= 0; otherwise, unexpected behavior may occur. Their default values are all 0, and the unit is pixels for the corner radius.)

(If you do not want to call the setters one by one, you can use `setCornerRadius(int radius)` to set all four at once.)

!!! warning

    Hit testing does not take rounded corners into account.

Example: Set the top-left corner radius to 5, the bottom-right to 20, the top-right to 30, and the bottom-left to 0, using the chained API.

```java
background.setLeftTopCornerRadius(5)
    .setRightTopCornerRadius(30)
    .setLeftBottomCornerRadius(0)
    .setRightBottomCornerRadius(20);
```