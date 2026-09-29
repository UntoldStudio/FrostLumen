/*
 * Copyright 2026 Untold Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.untoldstudio.frostlumen.core.render;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import top.untoldstudio.frostlumen.core.data.ThicknessPosition;
import top.untoldstudio.frostlumen.core.exception.ResourceException;
import top.untoldstudio.frostlumen.core.texture.Texture;
import top.untoldstudio.frostlumen.core.tool.ResourceReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.*;

public abstract class GuiRender implements IResourceManager {
    protected final long windowHandle;

    public void drawRectangle(int minX, int minY, int maxX, int maxY, int red, int green, int blue, int alpha) {
        drawRectangle(minX, minY, maxX, maxY, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha);
    }

    public void drawRectangle(int minX, int minY, int maxX, int maxY,
                              int aRed, int aGreen, int aBlue, int aAlpha,
                              int bRed, int bGreen, int bBlue, int bAlpha,
                              int cRed, int cGreen, int cBlue, int cAlpha,
                              int dRed, int dGreen, int dBlue, int dAlpha
    ) {
        drawTriangle(minX, minY, maxX, minY, minX, maxY, aRed, aGreen, aBlue, aAlpha, bRed, bGreen, bBlue, bAlpha, cRed, cGreen, cBlue, cAlpha);
        drawTriangle(maxX, minY, minX, maxY, maxX, maxY, bRed, bGreen, bBlue, bAlpha, cRed, cGreen, cBlue, cAlpha, dRed, dGreen, dBlue, dAlpha);
    }

    public void drawShape(int minX, int minY, int maxX, int maxY, int red, int green, int blue, int alpha,
                          int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii,
                          int aBorderThickness, int bBorderThickness, int cBorderThickness, int dBorderThickness,
                          int borderRed, int borderGreen, int borderBlue, int borderAlpha,
                          ThicknessPosition thicknessPosition
    ) {
        drawShape(minX, minY, maxX, maxY, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha,
                aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii, aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness,
                borderRed, borderGreen, borderBlue, borderAlpha, thicknessPosition
        );
    }

    public abstract void drawTriangle(int ax, int ay, int bx, int by, int cx, int cy,
                                      int aRed, int aGreen, int aBlue, int aAlpha,
                                      int bRed, int bGreen, int bBlue, int bAlpha,
                                      int cRed, int cGreen, int cBlue, int cAlpha
    );

    public abstract void drawShape(int minX, int minY, int maxX, int maxY,
                                   int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                                   int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha,
                                   int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii,
                                   int aBorderThickness, int bBorderThickness, int cBorderThickness, int dBorderThickness,
                                   int borderRed, int borderGreen, int borderBlue, int borderAlpha,
                                   ThicknessPosition position
    );

    public abstract void drawTexture(int textureId, int minX, int minY, int maxX, int maxY, float u0, float v0, float u1, float v1,
                                     int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                                     int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha
    );

    public void drawNiceSliceTexture(int textureId, int textureWidth, int textureHeight, int borderLeft, int borderRight, int borderTop, int borderBottom, int targetMinX, int targetMinY, int targetMaxX, int targetMaxY, float textureU0, float textureV0, float textureU3, float textureV3,
                                     int aRed, int aGreen, int aBlue, int aAlpha,
                                     int bRed, int bGreen, int bBlue, int bAlpha,
                                     int cRed, int cGreen, int cBlue, int cAlpha,
                                     int dRed, int dGreen, int dBlue, int dAlpha
    ) {
        int targetRectWidth = targetMaxX - targetMinX;
        int targetRectHeight = targetMaxY - targetMinY;
        float subTexturePixelWidth = (textureU3 - textureU0) * textureWidth;
        float subTexturePixelHeight = (textureV3 - textureV0) * textureHeight;

        if (targetRectWidth <= 0 || targetRectHeight <= 0 || textureWidth <= 0 || textureHeight <= 0 || subTexturePixelWidth <= 0 || subTexturePixelHeight <= 0) {
            return;
        }

        float widthScale = targetRectWidth / subTexturePixelWidth;

        int scaledBorderLeft = Math.min(Math.round(borderLeft * widthScale), targetRectWidth / 2);
        int scaledBorderRight = Math.min(Math.round(borderRight * widthScale), targetRectWidth / 2);
        int scaledBorderTop = Math.min(Math.round(borderTop * widthScale), targetRectHeight / 2);
        int scaledBorderBottom = Math.min(Math.round(borderBottom * widthScale), targetRectHeight / 2);

        int leftSliceEndX = targetMinX + scaledBorderLeft;
        int rightSliceStartX = targetMaxX - scaledBorderRight;

        int topSliceEndY = targetMinY + scaledBorderTop;
        int bottomSliceStartY = targetMaxY - scaledBorderBottom;

        float textureU1 = textureU0 + borderLeft / (float) textureWidth;
        float textureU2 = textureU3 - borderRight / (float) textureWidth;

        float textureV1 = textureV0 + borderTop / (float) textureHeight;
        float textureV2 = textureV3 - borderBottom / (float) textureHeight;

        for (int columnRegionIndex = 0; columnRegionIndex < 3; columnRegionIndex++) {
            int regionMinX;
            int regionMaxX;
            float regionTextureLeftU;
            float regionTextureRightU;
            if (columnRegionIndex == 0) {
                regionMinX = targetMinX;
                regionMaxX = leftSliceEndX;
                regionTextureLeftU = textureU0;
                regionTextureRightU = textureU1;
            } else if (columnRegionIndex == 1) {
                regionMinX = leftSliceEndX;
                regionMaxX = rightSliceStartX;
                regionTextureLeftU = textureU1;
                regionTextureRightU = textureU2;
            } else {
                regionMinX = rightSliceStartX;
                regionMaxX = targetMaxX;
                regionTextureLeftU = textureU2;
                regionTextureRightU = textureU3;
            }

            for (int rowRegionIndex = 0; rowRegionIndex < 3; rowRegionIndex++) {
                int regionMinY;
                int regionMaxY;
                float regionTextureTopV;
                float regionTextureBottomV;
                if (rowRegionIndex == 0) {
                    regionMinY = targetMinY;
                    regionMaxY = topSliceEndY;
                    regionTextureTopV = textureV0;
                    regionTextureBottomV = textureV1;
                } else if (rowRegionIndex == 1) {
                    regionMinY = topSliceEndY;
                    regionMaxY = bottomSliceStartY;
                    regionTextureTopV = textureV1;
                    regionTextureBottomV = textureV2;
                } else {
                    regionMinY = bottomSliceStartY;
                    regionMaxY = targetMaxY;
                    regionTextureTopV = textureV2;
                    regionTextureBottomV = textureV3;
                }

                float normalizedX0 = (regionMinX - targetMinX) / (float) targetRectWidth;
                float normalizedX1 = (regionMaxX - targetMinX) / (float) targetRectWidth;
                float normalizedY0 = (regionMinY - targetMinY) / (float) targetRectHeight;
                float normalizedY1 = (regionMaxY - targetMinY) / (float) targetRectHeight;

                float inverseNormalizedX0 = 1f - normalizedX0;
                float inverseNormalizedX1 = 1f - normalizedX1;
                float inverseNormalizedY0 = 1f - normalizedY0;
                float inverseNormalizedY1 = 1f - normalizedY1;

                int topLeftRed = Math.round(
                        inverseNormalizedX0 * inverseNormalizedY0 * aRed +
                                normalizedX0 * inverseNormalizedY0 * bRed +
                                inverseNormalizedX0 * normalizedY0 * cRed +
                                normalizedX0 * normalizedY0 * dRed
                );
                int topLeftGreen = Math.round(
                        inverseNormalizedX0 * inverseNormalizedY0 * aGreen +
                                normalizedX0 * inverseNormalizedY0 * bGreen +
                                inverseNormalizedX0 * normalizedY0 * cGreen +
                                normalizedX0 * normalizedY0 * dGreen
                );
                int topLeftBlue = Math.round(
                        inverseNormalizedX0 * inverseNormalizedY0 * aBlue +
                                normalizedX0 * inverseNormalizedY0 * bBlue +
                                inverseNormalizedX0 * normalizedY0 * cBlue +
                                normalizedX0 * normalizedY0 * dBlue
                );
                int topLeftAlpha = Math.round(
                        inverseNormalizedX0 * inverseNormalizedY0 * aAlpha +
                                normalizedX0 * inverseNormalizedY0 * bAlpha +
                                inverseNormalizedX0 * normalizedY0 * cAlpha +
                                normalizedX0 * normalizedY0 * dAlpha
                );

                int topRightRed = Math.round(
                        inverseNormalizedX1 * inverseNormalizedY0 * aRed +
                                normalizedX1 * inverseNormalizedY0 * bRed +
                                inverseNormalizedX1 * normalizedY0 * cRed +
                                normalizedX1 * normalizedY0 * dRed
                );
                int topRightGreen = Math.round(
                        inverseNormalizedX1 * inverseNormalizedY0 * aGreen +
                                normalizedX1 * inverseNormalizedY0 * bGreen +
                                inverseNormalizedX1 * normalizedY0 * cGreen +
                                normalizedX1 * normalizedY0 * dGreen
                );
                int topRightBlue = Math.round(
                        inverseNormalizedX1 * inverseNormalizedY0 * aBlue +
                                normalizedX1 * inverseNormalizedY0 * bBlue +
                                inverseNormalizedX1 * normalizedY0 * cBlue +
                                normalizedX1 * normalizedY0 * dBlue
                );
                int topRightAlpha = Math.round(
                        inverseNormalizedX1 * inverseNormalizedY0 * aAlpha +
                                normalizedX1 * inverseNormalizedY0 * bAlpha +
                                inverseNormalizedX1 * normalizedY0 * cAlpha +
                                normalizedX1 * normalizedY0 * dAlpha
                );

                int bottomLeftRed = Math.round(
                        inverseNormalizedX0 * inverseNormalizedY1 * aRed +
                                normalizedX0 * inverseNormalizedY1 * bRed +
                                inverseNormalizedX0 * normalizedY1 * cRed +
                                normalizedX0 * normalizedY1 * dRed
                );
                int bottomLeftGreen = Math.round(
                        inverseNormalizedX0 * inverseNormalizedY1 * aGreen +
                                normalizedX0 * inverseNormalizedY1 * bGreen +
                                inverseNormalizedX0 * normalizedY1 * cGreen +
                                normalizedX0 * normalizedY1 * dGreen
                );
                int bottomLeftBlue = Math.round(
                        inverseNormalizedX0 * inverseNormalizedY1 * aBlue +
                                normalizedX0 * inverseNormalizedY1 * bBlue +
                                inverseNormalizedX0 * normalizedY1 * cBlue +
                                normalizedX0 * normalizedY1 * dBlue
                );
                int bottomLeftAlpha = Math.round(
                        inverseNormalizedX0 * inverseNormalizedY1 * aAlpha +
                                normalizedX0 * inverseNormalizedY1 * bAlpha +
                                inverseNormalizedX0 * normalizedY1 * cAlpha +
                                normalizedX0 * normalizedY1 * dAlpha
                );

                int bottomRightRed = Math.round(
                        inverseNormalizedX1 * inverseNormalizedY1 * aRed +
                                normalizedX1 * inverseNormalizedY1 * bRed +
                                inverseNormalizedX1 * normalizedY1 * cRed +
                                normalizedX1 * normalizedY1 * dRed
                );
                int bottomRightGreen = Math.round(
                        inverseNormalizedX1 * inverseNormalizedY1 * aGreen +
                                normalizedX1 * inverseNormalizedY1 * bGreen +
                                inverseNormalizedX1 * normalizedY1 * cGreen +
                                normalizedX1 * normalizedY1 * dGreen
                );
                int bottomRightBlue = Math.round(
                        inverseNormalizedX1 * inverseNormalizedY1 * aBlue +
                                normalizedX1 * inverseNormalizedY1 * bBlue +
                                inverseNormalizedX1 * normalizedY1 * cBlue +
                                normalizedX1 * normalizedY1 * dBlue
                );
                int bottomRightAlpha = Math.round(
                        inverseNormalizedX1 * inverseNormalizedY1 * aAlpha +
                                normalizedX1 * inverseNormalizedY1 * bAlpha +
                                inverseNormalizedX1 * normalizedY1 * cAlpha +
                                normalizedX1 * normalizedY1 * dAlpha
                );

                drawTexture(textureId, regionMinX, regionMinY, regionMaxX, regionMaxY,
                        regionTextureLeftU, regionTextureTopV, regionTextureRightU, regionTextureBottomV,
                        topLeftRed, topLeftGreen, topLeftBlue, topLeftAlpha,
                        topRightRed, topRightGreen, topRightBlue, topRightAlpha,
                        bottomLeftRed, bottomLeftGreen, bottomLeftBlue, bottomLeftAlpha,
                        bottomRightRed, bottomRightGreen, bottomRightBlue, bottomRightAlpha);
            }
        }
    }

    public void drawTexture(Texture texture, int minX, int minY, int maxX, int maxY, float u0, float v0, float u1, float v1,
                            int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                            int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha
    ) {
        if (texture.isNiceSlice()) {
            drawNiceSliceTexture(texture.textureId(), texture.width(), texture.height(), texture.left(), texture.right(), texture.top(), texture.bottom(),
                    minX, minY, maxX, maxY, u0, v0, u1, v1,
                    aRed, aGreen, aBlue, aAlpha, bRed, bGreen, bBlue, bAlpha,
                    cRed, cGreen, cBlue, cAlpha, dRed, dGreen, dBlue, dAlpha
            );
        } else {
            drawTexture(texture.textureId(), minX, minY, maxX, maxY, u0, v0, u1, v1,
                    aRed, aGreen, aBlue, aAlpha, bRed, bGreen, bBlue, bAlpha,
                    cRed, cGreen, cBlue, cAlpha, dRed, dGreen, dBlue, dAlpha
            );
        }
    }
    public void drawTexture(Texture data, int minX, int minY, int maxX, int maxY, float u0, float v0, float u1, float v1, int red, int green, int blue, int alpha) {
        drawTexture(data, minX, minY, maxX, maxY, u0, v0, u1, v1, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha);
    }

    public abstract void submitBuffer();

    public void beginFrame() {
        IResourceManager.THREAD_LOCAL.set(this);
        begin();
    }

    public void endFrame() {
        end();
    }

    public abstract void enableScissor(int x, int y, int width, int height);

    public abstract void disableScissor();

    protected abstract void begin();

    public abstract void saveContext();

    protected abstract void end();

    public abstract void restoreContext();

    private Texture loadTexture(String path, boolean isNiceSlice, int left, int right, int top, int bottom) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelBuffer = stack.mallocInt(1);

            ByteBuffer pixels = stbi_load(path, widthBuffer, heightBuffer, channelBuffer, 4);
            if (pixels == null) {
                byte[] bytes = ResourceReader.readBytes(path);
                ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                pixels = stbi_load_from_memory(buffer, widthBuffer, heightBuffer, channelBuffer, 4);

                if (pixels == null) {
                    throw new ResourceException("Load texture failed:" + stbi_failure_reason());
                }
            }

            int width = widthBuffer.get();
            int height = heightBuffer.get();
            int channel = channelBuffer.get();

            int id = loadTexture(pixels, width, height);

            stbi_image_free(pixels);

            return new Texture(isNiceSlice, id, width, height, channel, path, left, right, top, bottom);
        } catch (IOException e) {
            throw new ResourceException("Load texture failed: " + e.getMessage());
        }
    }

    public Texture loadTexture(String path) {
        return loadTexture(path, false, -1, -1, -1, -1);
    }

    public Texture loadNiceSliceTexture(String path, int left, int right, int top, int bottom) {
        return loadTexture(path, true, left, right, top, bottom);
    }

    protected abstract int loadTexture(ByteBuffer data, int width, int height);


    public void setExternalSettingCursor(long handle) {
    }

    public GuiRender(long windowHandle) {
        this.windowHandle = windowHandle;
    }
}
