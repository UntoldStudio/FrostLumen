# Resources

This chapter will explain how to use ResourceManager to load resources, and how to use resource objects.

## ResourceManager

All resource loading should be done through ResourceManager. Calls to load resources should be made on the rendering thread and must be after `Window.from` is called.

You can get the ResourceManager instance via the static method `ResourceManager.getResourceManagerFromThreadLocal()`.

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

To load a font, you only need to call `loadFont(String path)` to get a Font instance. Fonts are cached.

A Font instance can be passed to various nodes that need a Font, such as TextLabel.

Font is an immutable record, defined as:

```java
public record Font(FT_Face face, String path, ByteBuffer data, int id) {}
```

It has the `public int getStringWidth(String text, int fontSize)` method and the `public int getStringHeight(int fontSize)` method, allowing you to get the pixel dimensions it occupies.

The static method `getDefaultFont` returns the default font (currently Inter in this version).

### Loading Textures

The core methods for loading textures are these:

```java
Texture loadTexture(String path, boolean isLinear);
Texture loadTexture(byte[] data, boolean isLinear);
Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom);
Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom);
```

Note: **None of them cache; each call re-uploads to the GPU and returns a new Texture.**

The first function loads a normal texture. `path` indicates the path to the local texture (supports disk/jar internals). `isLinear`: pass `false` for pixel art, pass `true` for high resolution. The convenience overload defaults to `true`.
(`isLinear=true` uses linear filtering, smoothing when scaled; `false` uses nearest-neighbor, keeping pixels sharp when scaled. So pass `false` for pixel art, `true` for normal images.)

The third function loads a nine-slice texture. `path` indicates the path to the local texture (supports disk/jar internals). `NiceSliceType` has two values: `PROPORTIONAL` and `FIXED_BORDER`.

```text
PROPORTIONAL: The four corners scale proportionally to the target width versus the original image width, changing naturally with the target size.

FIXED_BORDER: The four corners keep their original pixels; only the center and edges are stretched/tiled.
```

`stretchInner` indicates whether the center of the nine-slice is tiled or stretched. `isLinear` is the same as above. `left, right, top, bottom` indicate the pixel distances from the four edges of the nine-slice to the corresponding edges of the center.

The fourth function compared to the second function, and the second compared to the first, only changes the texture acquisition path from the resource manager reading bytes itself to letting you pass bytes.

Texture is an immutable record, defined as:

```java
public record Texture(boolean isNiceSlice, NiceSliceType sliceType, boolean stretchInner, int textureId, int width, int height, int channel, String filePath, int left, int right, int top, int bottom) {...}
```

If it is not a nine-slice, `sliceType` will be `null`, `stretchInner` is unspecified, and `left, right, top, bottom` are all `-1`.

If loaded from `byte[]`, `filePath` is `null`.

Example: Construct a Button that fills the entire screen, centered on the screen. In the normal state, the texture is `oiiaioiiai-blue.jpg` with image alignment `FILL`. In the hover state, it is `oiiaioiiai-red.jpg`. In the click state, it is `oiiaioiiai-green.jpg`. It will draw Frame's default background, with a corner radius of 10 pixels.

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