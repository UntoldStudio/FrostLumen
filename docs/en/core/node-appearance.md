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

If this value is false, the entire background will not be rendered.

If you do not see a background, and the node has the drawBackground property, first consider setting it to true.

## BackgroundBlurStrength

Type: double

Default value: 0

Range: 0.1-1 recommended; the actual value will be clamped to 0-2 on the renderer side.

When enabled, Gaussian blur will be applied to all pixels in the background, and the blur region rotates with the node's angle.

If Alpha=255, it will have no effect no matter what value is set, because an opaque node already directly covers the pixels behind it, so blurring the pixels behind it is meaningless.

Example:

```java
node.setBackgroundBlurStrength(0.7);
```

## BackgroundColor

Type: RGBA

Default value: RGBA.WHITE

The background color.

Example:

```java
node.setBackgroundColor(new RGBA(210, 230, 255, 90));
```

## BackgroundBorder

The BackgroundBorder property is actually composed of 6 independent components of different types.

The four of type int:
`backgroundLeftBorderThickness`
`backgroundRightBorderThickness`
`backgroundTopBorderThickness`
`backgroundBottomBorderThickness`

(The four values above must be >= 0; otherwise, unexpected behavior may occur. Their default values are all 0, and the unit is pixels.)

(If you do not want to call the setters for these four one by one, you can use `setBackgroundBorderThickness(int thickness)` to set all four at once.)

The one of type RGBA:

`backgroundBorderColor`

(Default value is RGBA.BLACK)

The one of type ThicknessPosition:

`backgroundBorderThicknessPosition`

(Default value is OUTSIDE)

The four int values correspond to the border thickness of the four sides of the background, the RGBA value determines the border color, and the ThicknessPosition determines where the border is.

ThicknessPosition has three values:

OUTSIDE (rendered outside the background)

INSIDE (rendered inside the background)

CENTER (rendered at the center of the background border)

The above values are all borders, so even if the node rotates, they will stay attached to the background.

Example: Set a blue border with a thickness of 5 pixels, rendered inside the background, using the chained API.

```java
node.setBackgroundBorderThickness(5)
    .setBackgroundBorderColor(RGBA.BLUE)
    .setBackgroundBorderThicknessPosition(ThicknessPosition.INSIDE);
```

## CornerRadius

It consists of 4 independent components of type int:

`backgroundLeftTopCornerRadius`
`backgroundRightTopCornerRadius`
`backgroundLeftBottomCornerRadius`
`backgroundRightBottomCornerRadius`

(They must be >= 0; otherwise, unexpected behavior may occur. Their default values are all 0, and the unit is pixels for the corner radius.)

(If you do not want to call the setters one by one, you can use `setBackgroundCornerRadius(int radius)` to set all four at once.)

!!! warning

    Hit testing does not take rounded corners into account.

Example: Set the top-left corner radius to 5, the bottom-right to 20, the top-right to 30, and the bottom-left to 0, using the chained API.

```java
node.setBackgroundLeftTopCornerRadius(5)
    .setBackgroundRightTopCornerRadius(30)
    .setBackgroundLeftBottomCornerRadius(0)
    .setBackgroundRightBottomCornerRadius(20);
```