# Resources

This chapter explains how to use ResourceManager to load resources, and how resource objects are used.

## ResourceManager

All resource loading should be done through ResourceManager. Resource loading calls should be made on the rendering thread (other threads can use `root.runTask` to schedule them onto the rendering thread), and must be after `window.init` has been called.

You can obtain the ResourceManager instance via the `getResourceManager` method of `NodeRoot`.

ResourceManager defines these methods:

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

### Loading Fonts

Note: **None of them cache. Each call re-uploads and returns a new Font.**

To load a font, simply use loadFont(String path) to get a Font instance.

A Font instance can be passed to various nodes that need a Font, such as TextLabel.

```java
textLabel.description().setFont(font);
```

Font is an immutable record, defined as:

```java
public record Font(FT_Face face, String path, ByteBuffer data, int id) {}
```

It has a `public int getStringWidth(String text, int fontSize)` method and a `public int getStringHeight(int fontSize)` method so you can get the pixel space occupied.

The static method `loadDefaultFont(GuiRender render)` returns the default font (currently MiSans Regular in this version).

### Loading Textures

The core methods for loading textures are these:

```java
Texture loadTexture(String path, boolean isLinear);
Texture loadTexture(byte[] data, boolean isLinear);
Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom);
Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom);
```

Note: **They do not cache. Each call re-uploads to the GPU and returns a new Texture.**

The first function loads a normal texture. `path` is the path to the local texture (supports disk / inside jar). `isLinear`: pass false for pixel art, true for high-resolution images. The convenience overload defaults to true.
(isLinear=true uses linear filtering, smooth when scaling; false uses nearest-neighbor, keeping pixels sharp when scaling. So pass false for pixel art, true for normal images.)

The third function loads a nice-slice (nine-patch) texture. `path` is the path to the local texture (supports disk / inside jar). NiceSliceType has two values: `PROPORTIONAL` and `FIXED_BORDER`.

```text
PROPORTIONAL: The four corners are scaled according to the ratio of the target width to the original image width, changing naturally with the target size.

FIXED_BORDER: The four corners keep their original pixels; only the center and edges are stretched/tiled.
```

`stretchInner` indicates whether the center of the nice slice is tiled or stretched. `isLinear` is the same as above. `left`, `right`, `top`, `bottom` indicate the pixel distances from the four edges of the nice slice to the corresponding edges of the center.

The fourth function, compared to the second, and the second compared to the first, simply changes the texture acquisition path from the resource manager reading the bytes itself to letting you pass in the bytes.

Texture is an immutable record, defined as:

```java
public record Texture(boolean isNiceSlice, NiceSliceType sliceType, boolean stretchInner, int textureId, int width, int height, int channel, String filePath, int left, int right, int top, int bottom) {...}
```

If it is not a nice slice, sliceType will be null, stretchInner is unspecified, and left, right, top, bottom are all -1.

If loaded from byte[], filePath is null.

Example: Construct a Button that fills the entire screen, centered on the screen. In the normal state, the texture is oiiaioiiai-blue.jpg, and the image alignment is FILL. In the hover state, it is oiiaioiiai-red.jpg. In the click state, it is oiiaioiiai-green.jpg. It will draw Frame's default background, with a corner radius of 10 pixels.

```java
ResourceManager resourceManager = root.getResourceManager();

ImageButton button = new ImageButton(resourceManager.loadTexture("/oiiaioiiai-blue.jpg"), ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(1.0, 1.0))
                .normal().setImageAlignment(ImageAlignment.FILL).getNode()
                .onHover().setTexture(resourceManager.loadTexture("/oiiaioiiai-red.jpg")).getNode()
                .onClick().setTexture(resourceManager.loadTexture("/oiiaioiiai-green.jpg")).getNode()
                .setAnchor(0.5, 0.5)
                .setDrawBackground(true)
                .background()
                .setCornerRadius(10)
                .getNode();
```

### Unloading Resources

The methods for unloading resources are these:

```java
void unloadTexture(Texture texture);
default void unloadTexture(Texture... textures) {
    for (Texture texture : textures) {
        unloadTexture(texture);
    }
}
void unloadFont(Font font);
default void unloadFont(Font... fonts) {
    for (Font font : fonts) {
        unloadFont(font);
    }
}
```

They all free part of the memory resources. The other part depends on the underlying renderer implementation, and GPU resources also differ depending on the underlying renderer implementation.

!!! warning

    Please unload resources with the ResourceManager that loaded them. Do not use another window's ResourceManager to unload.