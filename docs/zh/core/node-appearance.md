# 节点外观

本章节将会讲解每个GuiNode都会有的共用属性.

## ClipChildren

类型:boolean

默认值:false

启用后将会限制所有子项只能渲染在父项的边框内.ClipChildren会考虑节点的圆角值,且会跟随节点的角度旋转区域

启用后命中测试也会考虑父链上的该因素,换句话说只要玩家鼠标没有真正放到节点可见的地方那么命中测试就会返回false

示例:

```java
node.setClipChildren(true);
```

## DrawBackground

类型:boolean

默认值:不定,某些节点true,有些false

大部分节点都有这个属性,如果有这个属性那么该节点就必定有对应setter

如果该值为false,整个背景将不会渲染

如果你没看到背景,且节点有drawBackground属性,优先考虑将它设为true

## BackgroundBlurStrength

类型:double

默认值:0

范围:推荐0.1-1 实际值将会在渲染器侧钳制到0-2

启用后将会对背景所有像素应用高斯模糊,会跟随节点的角度旋转区域

如果Alpha=255,无论设置值为多少都将没有任何效果,因为不透明节点,本来也是直接覆盖,模糊后面的像素无意义

示例:

```java
node.setBackgroundBlurStrength(0.7);
```

## BackgroundColor

类型:RGBA

默认值:RGBA.WHITE

背景的颜色.

示例:

```java
node.setBackgroundColor(new RGBA(210, 230, 255, 90));
```

## BackgroundBorder

BackgroundBorder属性实际由6个类型不同的独立分量组成

类型为int的:
`backgroundLeftBorderThickness`
`backgroundRightBorderThickness`
`backgroundTopBorderThickness`
`backgroundBottomBorderThickness`

(以上这四个值必须>=0否则会出现非预期行为,默认值都是0,单位为像素)

(如果你不想一个个调用这四个的setter,可以使用`setBackgroundBorderThickness(int thickness)`一键设置四个)

类型为RGBA的:

`backgroundBorderColor`

(默认值为RGBA.BLACK)

类型为ThicknessPosition的:

`backgroundBorderThicknessPosition`

(默认值为OUTSIDE)

类型为int的4个分别对应背景四条边的边框厚度,类型为RGBA的决定边框的颜色,类型为ThicknessPosition的决定边框在哪

ThicknessPosition有三个值:

OUTSIDE(渲染在背景外)

INSIDE(渲染在背景内)

CENTER(渲染在背景的边框中心)

以上的值都是边框因此即使节点旋转它也会紧贴背景

示例:设置一个大小为5像素的蓝色边框,渲染在背景内,使用链式API

```java
node.setBackgroundBorderThickness(5)
    .setBackgroundBorderColor(RGBA.BLUE)
    .setBackgroundBorderThicknessPosition(ThicknessPosition.INSIDE);
```

## CornerRadius

它由4个类型为int的独立分量组成

`backgroundLeftTopCornerRadius`
`backgroundRightTopCornerRadius`
`backgroundLeftBottomCornerRadius`
`backgroundRightBottomCornerRadius`

(它们必须>=0否则会出现非预期行为,默认值都是0,单位是像素圆角半径)

(如果你不想一个个调用setter,可以使用`setBackgroundCornerRadius(int radius)`一键设置四个)

!!! warning

    命中测试不会考虑圆角

示例:设置左上角圆角半径为5,右下角为20,右上角为30,左下角为0,使用链式API

```java
node.setBackgroundLeftTopCornerRadius(5)
    .setBackgroundRightTopCornerRadius(30)
    .setBackgroundLeftBottomCornerRadius(0)
    .setBackgroundRightBottomCornerRadius(20);
```