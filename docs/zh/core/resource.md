# 资源

本章将会讲解如何使用ResourceManager加载资源,以及资源对象如何使用.

## ResourceManager

所有资源的加载都应该通过ResourceManager.加载资源的调用应该在渲染线程使用,并且必须在Window.from调用之后

你可以通过`ResourceManager.getResourceManagerFromThreadLocal()`静态方法获取ResourceManager实例.

ResourceManager定义了这些方法:

```java
Texture loadTexture(String path);
Texture loadTexture(String path, boolean isLinear);
Texture loadTexture(byte[] data);
Texture loadTexture(byte[] data, boolean isLinear);
Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, int left, int right, int top, int bottom);
Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom);
Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, int left, int right, int top, int bottom);
Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom);
Font loadFont(String path);
```

### 加载字体

加载字体仅需通过loadFont(String path)即可获取Font实例,字体有缓存

Font实例可以传给各种需要Font的节点,如TextLabel

Font是个不可变记录,定义为:

```java
public record Font(FT_Face face, String path, ByteBuffer data, int id) {}
```

它拥有`public int getStringWidth(String text, int fontSize)`方法与`public int getStringHeight(int fontSize)`方法让你获取占用的像素

静态方法`getDefaultFont`返回默认字体(当前版本为inter)

### 加载贴图

加载贴图的核心方法是这些:

```java
Texture loadTexture(String path, boolean isLinear);
Texture loadTexture(byte[] data, boolean isLinear);
Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom);
Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom);
```

注意:**他们都不会缓存,每次都会重新上传到GPU,返回新的Texture**

第一个函数加载普通纹理,path表示本地纹理的路径(支持硬盘/jar内部),isLinear:像素风传false,高分辨率传true,便捷重载默认传true
(isLinear=true使用线性过滤,缩放时平滑,false使用最近邻,缩放时保持像素锐利.所以像素画传false,普通图片传true)

第三个函数加载九宫格纹理,path表示本地纹理的路径(支持硬盘/jar内部),NiceSliceType有`PROPORTIONAL` `FIXED_BORDER`两种

```text
PROPORTIONAL:四个角按目标宽度与原图宽度的比例缩放,跟随目标尺寸自然变化

FIXED_BORDER:四个角保持原始像素,只有中心和边被拉伸/平铺
```

stretchInner表示九宫格中心是平铺还是拉伸,isLinear同上,left,right,top,bottom表示九宫格的四个边离中心的对应边的像素距离

第四个函数对比第二个函数,第二个函数对比第一个函数仅仅是把纹理获取路径从资源管理器自己读字节改为了让你传字节

Texture是一个不可变记录,定义为:

```java
public record Texture(boolean isNiceSlice, NiceSliceType sliceType, boolean stretchInner, int textureId, int width, int height, int channel, String filePath, int left, int right, int top, int bottom) {...}
```

如果它不是九宫格,sliceType将是null,stretchInner不定,left,right,top,bottom都是-1

如果从byte[]加载,filePath是null

示例:构造一个Button,占满全屏,在屏幕正中心,normal状态下贴图是oiiaioiiai-blue.jpg,且图片对齐为FILL,鼠标悬停状态下是oiiaioiiai-red.jpg,点击状态下是oiiaioiiai-green.jpg,会绘制Frame的默认背景,圆角半径为10像素

```java
ResourceManager resourceManager = ResourceManager.getResourceManagerFromThreadLocal();

ImageButton button = new ImageButton(resourceManager.loadTexture("/oiiaioiiai-blue.jpg"), ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(1.0, 1.0))
                .normal().setImageAlignment(ImageAlignment.FILL).getNode()
                .onHover().setTexture(resourceManager.loadTexture("/oiiaioiiai-red.jpg")).getNode()
                .onClick().setTexture(resourceManager.loadTexture("/oiiaioiiai-green.jpg")).getNode()
                .setAnchor(0.5, 0.5)
                .setDrawBackground(true)
                .setBackgroundCornerRadius(10);
```