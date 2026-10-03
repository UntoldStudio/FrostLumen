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

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import org.lwjgl.CLongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_Matrix;
import org.lwjgl.util.freetype.FT_Vector;
import top.untoldstudio.frostlumen.core.data.CursorMode;
import top.untoldstudio.frostlumen.core.data.CursorShape;
import top.untoldstudio.frostlumen.core.data.NiceSliceType;
import top.untoldstudio.frostlumen.core.data.ThicknessPosition;
import top.untoldstudio.frostlumen.core.exception.RenderException;
import top.untoldstudio.frostlumen.core.exception.ResourceException;
import top.untoldstudio.frostlumen.core.font.Font;
import top.untoldstudio.frostlumen.core.texture.Texture;
import top.untoldstudio.frostlumen.core.tool.GCCleanable;
import top.untoldstudio.frostlumen.core.tool.LruCacheMap;
import top.untoldstudio.frostlumen.core.tool.MathTool;
import top.untoldstudio.frostlumen.core.tool.ResourceReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.util.freetype.FreeType.*;
import static org.lwjgl.stb.STBImage.*;

public abstract class GuiRender implements IResourceManager {
    protected static final ThreadLocal<FT_Vector> vector = ThreadLocal.withInitial(() -> {
        FT_Vector ftVector = FT_Vector.malloc();
        GCCleanable.getGlobalCleaner().register(Thread.currentThread(), ftVector::free);
        return ftVector;
    });
    protected final Map<Font, Int2FloatMap> ascenderCache = new HashMap<>();
    protected final Map<Double, FT_Matrix> italicDegreesCache = new LruCacheMap<>(128, (key, value) -> value.free());
    protected final long windowHandle;
    protected final Map<String, Font> fontMap = new HashMap<>();
    protected long ftLibrary;
    protected long cursorShapeInThisFrame;
    protected int cursorModeInThisFrame;
    protected final Int2LongMap cursorShapeMap = new Int2LongOpenHashMap();
    protected final Deque<ScissorState> scissorStateDeque = new ArrayDeque<>();
    protected record ScissorState(
            float centerX, float centerY,
            float halfWidth, float halfHeight,
            float cosineOfAngle, float sineOfAngle,
            float cornerTopLeft, float cornerTopRight,
            float cornerBottomLeft, float cornerBottomRight
    ) {}

    public void drawRectangle(int minX, int minY, int maxX, int maxY, float angle, int red, int green, int blue, int alpha) {
        drawRectangle(minX, minY, maxX, maxY, angle, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha);
    }

    public void drawRectangle(int minX, int minY, int maxX, int maxY, float angle,
                              int aRed, int aGreen, int aBlue, int aAlpha,
                              int bRed, int bGreen, int bBlue, int bAlpha,
                              int cRed, int cGreen, int cBlue, int cAlpha,
                              int dRed, int dGreen, int dBlue, int dAlpha
    ) {
        int centerX = (minX + maxX) / 2;
        int centerY = (minY + maxY) / 2;

        drawTriangle(minX, minY, maxX, minY, minX, maxY, centerX, centerY, angle, aRed, aGreen, aBlue, aAlpha, bRed, bGreen, bBlue, bAlpha, cRed, cGreen, cBlue, cAlpha);
        drawTriangle(maxX, minY, minX, maxY, maxX, maxY, centerX, centerY, angle, bRed, bGreen, bBlue, bAlpha, cRed, cGreen, cBlue, cAlpha, dRed, dGreen, dBlue, dAlpha);
    }

    public void drawShape(int minX, int minY, int maxX, int maxY, float angle, int red, int green, int blue, int alpha,
                          int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii,
                          int aBorderThickness, int bBorderThickness, int cBorderThickness, int dBorderThickness,
                          int borderRed, int borderGreen, int borderBlue, int borderAlpha,
                          ThicknessPosition thicknessPosition
    ) {
        drawShape(minX, minY, maxX, maxY, angle, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha,
                aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii, aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness,
                borderRed, borderGreen, borderBlue, borderAlpha, thicknessPosition
        );
    }

    public void drawTriangle(int ax, int ay, int bx, int by, int cx, int cy, float angle,
                             int aRed, int aGreen, int aBlue, int aAlpha,
                             int bRed, int bGreen, int bBlue, int bAlpha,
                             int cRed, int cGreen, int cBlue, int cAlpha) {
        int centerX = (ax + bx + cx) / 3;
        int centerY = (ay + by + cy) / 3;

        drawTriangle(ax, ay, bx, by, cx, cy, centerX, centerY, angle, aRed, aGreen, aBlue, aAlpha, bRed, bGreen, bBlue, bAlpha, cRed, cGreen, cBlue, cAlpha);
    }
    public abstract void drawTriangle(int ax, int ay, int bx, int by, int cx, int cy, int centerX, int centerY, float angle,
                                      int aRed, int aGreen, int aBlue, int aAlpha,
                                      int bRed, int bGreen, int bBlue, int bAlpha,
                                      int cRed, int cGreen, int cBlue, int cAlpha
    );

    public abstract void drawShape(int minX, int minY, int maxX, int maxY, float angle,
                                   int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                                   int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha,
                                   int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii,
                                   int aBorderThickness, int bBorderThickness, int cBorderThickness, int dBorderThickness,
                                   int borderRed, int borderGreen, int borderBlue, int borderAlpha,
                                   ThicknessPosition position
    );

    public abstract void drawTexture(int textureId, int minX, int minY, int maxX, int maxY, int centerX, int centerY, float angle, float u0, float v0, float u1, float v1,
                                     int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                                     int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha
    );

    public void drawNiceSliceTexture(NiceSliceType type, int textureId, int textureWidth, int textureHeight, int borderLeft, int borderRight, int borderTop, int borderBottom, int targetMinX, int targetMinY, int targetMaxX, int targetMaxY, float angle, float textureU0, float textureV0, float textureU3, float textureV3,
                                     int aRed, int aGreen, int aBlue, int aAlpha,
                                     int bRed, int bGreen, int bBlue, int bAlpha,
                                     int cRed, int cGreen, int cBlue, int cAlpha,
                                     int dRed, int dGreen, int dBlue, int dAlpha,
                                     boolean stretchInner) {
        switch (type) {
            case PROPORTIONAL -> drawNiceSliceTextureProportional(textureId, textureWidth, textureHeight, borderLeft, borderRight, borderTop, borderBottom, targetMinX, targetMinY, targetMaxX, targetMaxY, angle, textureU0, textureV0, textureU3, textureV3, aRed, aGreen, aBlue, aAlpha, bRed, bGreen, bBlue, bAlpha, cRed, cGreen, cBlue, cAlpha, dRed, dGreen, dBlue, dAlpha, stretchInner);
            case FIXED_BORDER -> drawNiceSliceTextureFixed(textureId, textureWidth, textureHeight, borderLeft, borderRight, borderTop, borderBottom, targetMinX, targetMinY, targetMaxX, targetMaxY, angle, textureU0, textureV0, textureU3, textureV3, aRed, aGreen, aBlue, aAlpha, bRed, bGreen, bBlue, bAlpha, cRed, cGreen, cBlue, cAlpha, dRed, dGreen, dBlue, dAlpha, stretchInner);
        }
    }

    public void drawTexture(Texture texture, int minX, int minY, int maxX, int maxY, float angle, int red, int green, int blue, int alpha) {
        drawTexture(texture, minX, minY, maxX, maxY, angle, 0, 0, 1, 1,
                red, green, blue, alpha, red, green, blue, alpha,
                red, green, blue, alpha, red, green, blue, alpha
        );
    }
    public void drawTexture(Texture texture, int minX, int minY, int maxX, int maxY, float angle, float u0, float v0, float u1, float v1,
                            int aRed, int aGreen, int aBlue, int aAlpha,
                            int bRed, int bGreen, int bBlue, int bAlpha,
                            int cRed, int cGreen, int cBlue, int cAlpha,
                            int dRed, int dGreen, int dBlue, int dAlpha
    ) {
        if (texture.isNiceSlice()) {
            drawNiceSliceTexture(
                    texture.sliceType(), texture.textureId(), texture.width(), texture.height(), texture.left(), texture.right(), texture.top(), texture.bottom(), minX, minY, maxX, maxY, angle, u0, v0, u1, v1,
                    aRed, aGreen, aBlue, aAlpha,
                    bRed, bGreen, bBlue, bAlpha,
                    cRed, cGreen, cBlue, cAlpha,
                    dRed, dGreen, dBlue, dAlpha, texture.stretchInner()
            );
        } else {
            drawTexture(texture.textureId(), minX, minY, maxX, maxY, (minX + maxX) / 2, (minY + maxY) / 2, angle, u0, v0, u1, v1,
                    aRed, aGreen, aBlue, aAlpha,
                    bRed, bGreen, bBlue, bAlpha,
                    cRed, cGreen, cBlue, cAlpha,
                    dRed, dGreen, dBlue, dAlpha
            );
        }
    }

    private void drawNiceSliceTextureProportional(int textureId, int textureWidth, int textureHeight, int borderLeft, int borderRight, int borderTop, int borderBottom, int targetMinX, int targetMinY, int targetMaxX, int targetMaxY, float angle, float textureU0, float textureV0, float textureU3, float textureV3,
                                                  int aRed, int aGreen, int aBlue, int aAlpha,
                                                  int bRed, int bGreen, int bBlue, int bAlpha,
                                                  int cRed, int cGreen, int cBlue, int cAlpha,
                                                  int dRed, int dGreen, int dBlue, int dAlpha,
                                                  boolean stretchInner) {
        if (borderLeft + borderRight > textureWidth) {
            throw new RenderException("borderLeft + borderRight > textureWidth");
        }
        if (borderTop + borderBottom > textureHeight) {
            throw new RenderException("borderTop + borderBottom > textureHeight");
        }
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
            float uStart;
            float regionTextureRightU;
            if (columnRegionIndex == 0) {
                regionMinX = targetMinX;
                regionMaxX = leftSliceEndX;
                uStart = textureU0;
                regionTextureRightU = textureU1;
            } else if (columnRegionIndex == 1) {
                regionMinX = leftSliceEndX;
                regionMaxX = rightSliceStartX;
                uStart = textureU1;
                regionTextureRightU = textureU2;
            } else {
                regionMinX = rightSliceStartX;
                regionMaxX = targetMaxX;
                uStart = textureU2;
                regionTextureRightU = textureU3;
            }

            for (int rowRegionIndex = 0; rowRegionIndex < 3; rowRegionIndex++) {
                int regionMinY;
                int regionMaxY;
                float vStart;
                float regionTextureBottomV;
                if (rowRegionIndex == 0) {
                    regionMinY = targetMinY;
                    regionMaxY = topSliceEndY;
                    vStart = textureV0;
                    regionTextureBottomV = textureV1;
                } else if (rowRegionIndex == 1) {
                    regionMinY = topSliceEndY;
                    regionMaxY = bottomSliceStartY;
                    vStart = textureV1;
                    regionTextureBottomV = textureV2;
                } else {
                    regionMinY = bottomSliceStartY;
                    regionMaxY = targetMaxY;
                    vStart = textureV2;
                    regionTextureBottomV = textureV3;
                }

                if (regionMinX >= regionMaxX || regionMinY >= regionMaxY) {
                    continue;
                }

                boolean isInner = (columnRegionIndex == 1 || rowRegionIndex == 1);
                if (isInner && !stretchInner) {
                    float tileWidthPx = (regionTextureRightU - uStart) * textureWidth;
                    float tileHeightPx = (regionTextureBottomV - vStart) * textureHeight;
                    if (tileWidthPx > 0 && tileHeightPx > 0) {
                        int regionWidth = regionMaxX - regionMinX;
                        int regionHeight = regionMaxY - regionMinY;
                        for (float offsetX = 0; offsetX < regionWidth; offsetX += tileWidthPx) {
                            float drawWidth = Math.min(tileWidthPx, regionWidth - offsetX);
                            float uEnd = uStart + (drawWidth / tileWidthPx) * (regionTextureRightU - uStart);
                            int drawX = regionMinX + Math.round(offsetX);
                            int drawMaxX = drawX + Math.round(drawWidth);
                            for (float offsetY = 0; offsetY < regionHeight; offsetY += tileHeightPx) {
                                float drawHeight = Math.min(tileHeightPx, regionHeight - offsetY);
                                float vEnd = vStart + (drawHeight / tileHeightPx) * (regionTextureBottomV - vStart);
                                int drawY = regionMinY + Math.round(offsetY);
                                int drawMaxY = drawY + Math.round(drawHeight);
                                drawTexturedRegion(textureId, drawX, drawY, drawMaxX, drawMaxY,
                                        targetMinX, targetMinY, targetMaxX, targetMaxY, angle,
                                        uStart, vStart, uEnd, vEnd,
                                        aRed, aGreen, aBlue, aAlpha,
                                        bRed, bGreen, bBlue, bAlpha,
                                        cRed, cGreen, cBlue, cAlpha,
                                        dRed, dGreen, dBlue, dAlpha);
                            }
                        }
                    }
                } else {
                    drawTexturedRegion(textureId, regionMinX, regionMinY, regionMaxX, regionMaxY,
                            targetMinX, targetMinY, targetMaxX, targetMaxY, angle,
                            uStart, vStart, regionTextureRightU, regionTextureBottomV,
                            aRed, aGreen, aBlue, aAlpha,
                            bRed, bGreen, bBlue, bAlpha,
                            cRed, cGreen, cBlue, cAlpha,
                            dRed, dGreen, dBlue, dAlpha);
                }
            }
        }
    }

    private void drawNiceSliceTextureFixed(int textureId, int textureWidth, int textureHeight, int borderLeft, int borderRight, int borderTop, int borderBottom, int targetMinX, int targetMinY, int targetMaxX, int targetMaxY, float angle, float textureU0, float textureV0, float textureU3, float textureV3,
                                           int aRed, int aGreen, int aBlue, int aAlpha,
                                           int bRed, int bGreen, int bBlue, int bAlpha,
                                           int cRed, int cGreen, int cBlue, int cAlpha,
                                           int dRed, int dGreen, int dBlue, int dAlpha,
                                           boolean stretchInner) {
        if (borderLeft + borderRight > textureWidth) {
            throw new RenderException("borderLeft + borderRight > textureWidth");
        }
        if (borderTop + borderBottom > textureHeight) {
            throw new RenderException("borderTop + borderBottom > textureHeight");
        }
        int targetRectWidth = targetMaxX - targetMinX;
        int targetRectHeight = targetMaxY - targetMinY;
        if (targetRectWidth <= 0 || targetRectHeight <= 0) {
            return;
        }

        int i = Math.min(borderLeft, targetRectWidth / 2);
        int j = Math.min(borderRight, targetRectWidth / 2);
        int k = Math.min(borderTop, targetRectHeight / 2);
        int l = Math.min(borderBottom, targetRectHeight / 2);

        int midX = targetMinX + i;
        int midMaxX = targetMaxX - j;
        int midY = targetMinY + k;
        int midMaxY = targetMaxY - l;

        for (int col = 0; col < 3; col++) {
            int regionMinX;
            int regionMaxX;
            int sourceX;
            int sourceW;
            if (col == 0) {
                regionMinX = targetMinX;
                regionMaxX = midX;
                sourceX = 0;
                sourceW = i;
            } else if (col == 1) {
                regionMinX = midX;
                regionMaxX = midMaxX;
                sourceX = i;
                sourceW = textureWidth - j - i;
            } else {
                regionMinX = midMaxX;
                regionMaxX = targetMaxX;
                sourceX = textureWidth - j;
                sourceW = j;
            }
            if (regionMinX >= regionMaxX || sourceW <= 0) {
                continue;
            }

            for (int row = 0; row < 3; row++) {
                int regionMinY;
                int regionMaxY;
                int sourceY;
                int sourceH;
                if (row == 0) {
                    regionMinY = targetMinY;
                    regionMaxY = midY;
                    sourceY = 0;
                    sourceH = k;
                } else if (row == 1) {
                    regionMinY = midY;
                    regionMaxY = midMaxY;
                    sourceY = k;
                    sourceH = textureHeight - l - k;
                } else {
                    regionMinY = midMaxY;
                    regionMaxY = targetMaxY;
                    sourceY = textureHeight - l;
                    sourceH = l;
                }
                if (regionMinY >= regionMaxY || sourceH <= 0) {
                    continue;
                }

                float uStart = textureU0 + (sourceX / (float) textureWidth) * (textureU3 - textureU0);
                float uEnd = textureU0 + ((sourceX + sourceW) / (float) textureWidth) * (textureU3 - textureU0);
                float vStart = textureV0 + (sourceY / (float) textureHeight) * (textureV3 - textureV0);
                float vEnd = textureV0 + ((sourceY + sourceH) / (float) textureHeight) * (textureV3 - textureV0);

                boolean isInner = (col == 1 || row == 1);
                if (isInner && !stretchInner) {
                    for (int tileX = regionMinX; tileX < regionMaxX; tileX += sourceW) {
                        int tileMaxX = Math.min(tileX + sourceW, regionMaxX);
                        float tileUEnd = uStart + (tileMaxX - tileX) / (float) sourceW * (uEnd - uStart);
                        for (int tileY = regionMinY; tileY < regionMaxY; tileY += sourceH) {
                            int tileMaxY = Math.min(tileY + sourceH, regionMaxY);
                            float tileVEnd = vStart + (tileMaxY - tileY) / (float) sourceH * (vEnd - vStart);
                            drawTexturedRegion(textureId, tileX, tileY, tileMaxX, tileMaxY,
                                    targetMinX, targetMinY, targetMaxX, targetMaxY, angle,
                                    uStart, vStart, tileUEnd, tileVEnd,
                                    aRed, aGreen, aBlue, aAlpha,
                                    bRed, bGreen, bBlue, bAlpha,
                                    cRed, cGreen, cBlue, cAlpha,
                                    dRed, dGreen, dBlue, dAlpha);
                        }
                    }
                } else {
                    drawTexturedRegion(textureId, regionMinX, regionMinY, regionMaxX, regionMaxY,
                            targetMinX, targetMinY, targetMaxX, targetMaxY, angle,
                            uStart, vStart, uEnd, vEnd,
                            aRed, aGreen, aBlue, aAlpha,
                            bRed, bGreen, bBlue, bAlpha,
                            cRed, cGreen, cBlue, cAlpha,
                            dRed, dGreen, dBlue, dAlpha);
                }
            }
        }
    }

    private void drawTexturedRegion(int textureId, int targetMinX, int targetMinY, int targetMaxX, int targetMaxY,
                                    int globalTargetMinX, int globalTargetMinY, int globalTargetMaxX, int globalTargetMaxY,
                                    float angle, float u0, float v0, float u1, float v1,
                                    int aRed, int aGreen, int aBlue, int aAlpha,
                                    int bRed, int bGreen, int bBlue, int bAlpha,
                                    int cRed, int cGreen, int cBlue, int cAlpha,
                                    int dRed, int dGreen, int dBlue, int dAlpha) {
        int targetRectWidth = globalTargetMaxX - globalTargetMinX;
        int targetRectHeight = globalTargetMaxY - globalTargetMinY;
        int centerX = (globalTargetMinX + globalTargetMaxX) / 2;
        int centerY = (globalTargetMinY + globalTargetMaxY) / 2;

        float normalizedX0 = (targetMinX - globalTargetMinX) / (float) targetRectWidth;
        float normalizedX1 = (targetMaxX - globalTargetMinX) / (float) targetRectWidth;
        float normalizedY0 = (targetMinY - globalTargetMinY) / (float) targetRectHeight;
        float normalizedY1 = (targetMaxY - globalTargetMinY) / (float) targetRectHeight;

        float inverseNormalizedX0 = 1f - normalizedX0;
        float inverseNormalizedX1 = 1f - normalizedX1;
        float inverseNormalizedY0 = 1f - normalizedY0;
        float inverseNormalizedY1 = 1f - normalizedY1;

        int topLeftRed = MathTool.round(
                inverseNormalizedX0 * inverseNormalizedY0 * aRed +
                        normalizedX0 * inverseNormalizedY0 * bRed +
                        inverseNormalizedX0 * normalizedY0 * cRed +
                        normalizedX0 * normalizedY0 * dRed
        );
        int topLeftGreen = MathTool.round(
                inverseNormalizedX0 * inverseNormalizedY0 * aGreen +
                        normalizedX0 * inverseNormalizedY0 * bGreen +
                        inverseNormalizedX0 * normalizedY0 * cGreen +
                        normalizedX0 * normalizedY0 * dGreen
        );
        int topLeftBlue = MathTool.round(
                inverseNormalizedX0 * inverseNormalizedY0 * aBlue +
                        normalizedX0 * inverseNormalizedY0 * bBlue +
                        inverseNormalizedX0 * normalizedY0 * cBlue +
                        normalizedX0 * normalizedY0 * dBlue
        );
        int topLeftAlpha = MathTool.round(
                inverseNormalizedX0 * inverseNormalizedY0 * aAlpha +
                        normalizedX0 * inverseNormalizedY0 * bAlpha +
                        inverseNormalizedX0 * normalizedY0 * cAlpha +
                        normalizedX0 * normalizedY0 * dAlpha
        );

        int topRightRed = MathTool.round(
                inverseNormalizedX1 * inverseNormalizedY0 * aRed +
                        normalizedX1 * inverseNormalizedY0 * bRed +
                        inverseNormalizedX1 * normalizedY0 * cRed +
                        normalizedX1 * normalizedY0 * dRed
        );
        int topRightGreen = MathTool.round(
                inverseNormalizedX1 * inverseNormalizedY0 * aGreen +
                        normalizedX1 * inverseNormalizedY0 * bGreen +
                        inverseNormalizedX1 * normalizedY0 * cGreen +
                        normalizedX1 * normalizedY0 * dGreen
        );
        int topRightBlue = MathTool.round(
                inverseNormalizedX1 * inverseNormalizedY0 * aBlue +
                        normalizedX1 * inverseNormalizedY0 * bBlue +
                        inverseNormalizedX1 * normalizedY0 * cBlue +
                        normalizedX1 * normalizedY0 * dBlue
        );
        int topRightAlpha = MathTool.round(
                inverseNormalizedX1 * inverseNormalizedY0 * aAlpha +
                        normalizedX1 * inverseNormalizedY0 * bAlpha +
                        inverseNormalizedX1 * normalizedY0 * cAlpha +
                        normalizedX1 * normalizedY0 * dAlpha
        );

        int bottomLeftRed = MathTool.round(
                inverseNormalizedX0 * inverseNormalizedY1 * aRed +
                        normalizedX0 * inverseNormalizedY1 * bRed +
                        inverseNormalizedX0 * normalizedY1 * cRed +
                        normalizedX0 * normalizedY1 * dRed
        );
        int bottomLeftGreen = MathTool.round(
                inverseNormalizedX0 * inverseNormalizedY1 * aGreen +
                        normalizedX0 * inverseNormalizedY1 * bGreen +
                        inverseNormalizedX0 * normalizedY1 * cGreen +
                        normalizedX0 * normalizedY1 * dGreen
        );
        int bottomLeftBlue = MathTool.round(
                inverseNormalizedX0 * inverseNormalizedY1 * aBlue +
                        normalizedX0 * inverseNormalizedY1 * bBlue +
                        inverseNormalizedX0 * normalizedY1 * cBlue +
                        normalizedX0 * normalizedY1 * dBlue
        );
        int bottomLeftAlpha = MathTool.round(
                inverseNormalizedX0 * inverseNormalizedY1 * aAlpha +
                        normalizedX0 * inverseNormalizedY1 * bAlpha +
                        inverseNormalizedX0 * normalizedY1 * cAlpha +
                        normalizedX0 * normalizedY1 * dAlpha
        );

        int bottomRightRed = MathTool.round(
                inverseNormalizedX1 * inverseNormalizedY1 * aRed +
                        normalizedX1 * inverseNormalizedY1 * bRed +
                        inverseNormalizedX1 * normalizedY1 * cRed +
                        normalizedX1 * normalizedY1 * dRed
        );
        int bottomRightGreen = MathTool.round(
                inverseNormalizedX1 * inverseNormalizedY1 * aGreen +
                        normalizedX1 * inverseNormalizedY1 * bGreen +
                        inverseNormalizedX1 * normalizedY1 * cGreen +
                        normalizedX1 * normalizedY1 * dGreen
        );
        int bottomRightBlue = MathTool.round(
                inverseNormalizedX1 * inverseNormalizedY1 * aBlue +
                        normalizedX1 * inverseNormalizedY1 * bBlue +
                        inverseNormalizedX1 * normalizedY1 * cBlue +
                        normalizedX1 * normalizedY1 * dBlue
        );
        int bottomRightAlpha = MathTool.round(
                inverseNormalizedX1 * inverseNormalizedY1 * aAlpha +
                        normalizedX1 * inverseNormalizedY1 * bAlpha +
                        inverseNormalizedX1 * normalizedY1 * cAlpha +
                        normalizedX1 * normalizedY1 * dAlpha
        );

        drawTexture(textureId, targetMinX, targetMinY, targetMaxX, targetMaxY, centerX, centerY, angle, u0, v0, u1, v1,
                topLeftRed, topLeftGreen, topLeftBlue, topLeftAlpha,
                topRightRed, topRightGreen, topRightBlue, topRightAlpha,
                bottomLeftRed, bottomLeftGreen, bottomLeftBlue, bottomLeftAlpha,
                bottomRightRed, bottomRightGreen, bottomRightBlue, bottomRightAlpha);
    }

    public abstract void submitBuffer();

    public void beginFrame() {
        IResourceManager.THREAD_LOCAL.set(this);
        cursorShapeInThisFrame = -1;
        cursorModeInThisFrame = -1;
        begin();
    }

    public void endFrame() {
        end();
        if (cursorShapeInThisFrame != -1) {
            glfwSetCursor(windowHandle, cursorShapeInThisFrame);
        }
        if (cursorModeInThisFrame != -1) {
            glfwSetInputMode(windowHandle, GLFW_CURSOR, cursorModeInThisFrame);
        }
    }

    public void enableScissor(int x, int y, int width, int height, float angle, int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii) {
        float centerX = x + width * 0.5f;
        float centerY = y + height * 0.5f;
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;

        float radians = (float) Math.toRadians(angle);
        float cosineOfAngle = (float) Math.cos(radians);
        float sineOfAngle = (float) Math.sin(radians);

        float maxCornerRadius = Math.min(halfWidth, halfHeight);
        float cornerTopLeft = Math.clamp(aCornerRadii, 0, maxCornerRadius);
        float cornerTopRight = Math.clamp(bCornerRadii, 0, maxCornerRadius);
        float cornerBottomLeft = Math.clamp(cCornerRadii, 0, maxCornerRadius);
        float cornerBottomRight = Math.clamp(dCornerRadii, 0, maxCornerRadius);

        scissorStateDeque.push(new ScissorState(centerX, centerY, halfWidth, halfHeight,
                cosineOfAngle, sineOfAngle,
                cornerTopLeft, cornerTopRight, cornerBottomLeft, cornerBottomRight));
        pushScissorToRender();
    }

    public void disableScissor() {
        if (!scissorStateDeque.isEmpty()) {
            scissorStateDeque.pop();
        }
        pushScissorToRender();
    }

    private void pushScissorToRender() {
        if (scissorStateDeque.isEmpty()) {
            disableScissorTest();
        } else {
            enableScissorTest(scissorStateDeque.toArray(new ScissorState[0]));
        }
    }

    protected abstract void enableScissorTest(ScissorState[] scissorStates);

    protected abstract void disableScissorTest();

    public void initRender() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer buffer = stack.mallocPointer(1);
            int error = FT_Init_FreeType(buffer);
            if (error != 0) {
                throw new ResourceException("Failed to initialize FreeType library");
            }
            ftLibrary = buffer.get();
        }

        init();
    }

    protected abstract void init();

    protected abstract void begin();

    public abstract void saveContext();

    protected abstract void end();

    public abstract void restoreContext();

    @Override
    public Font loadFont(String path) {
        return fontMap.computeIfAbsent(path, fontPath -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer buffer = stack.mallocPointer(1);
                int error = FT_New_Face(ftLibrary, fontPath, 0, buffer);
                long facePointer = -1;
                ByteBuffer fontBuffer = null;

                if (error != 0) {
                    byte[] data;
                    try {
                        data = ResourceReader.readBytes(fontPath);
                    } catch (IOException e) {
                        throw new ResourceException("Cannot load font file: " + fontPath);
                    }
                    fontBuffer = MemoryUtil.memAlloc(data.length);
                    fontBuffer.put(data).flip();
                    buffer.clear();
                    error = FT_New_Memory_Face(ftLibrary, fontBuffer, 0, buffer);
                    if (error == 0) {
                        facePointer = buffer.get(0);
                    }
                }

                if (facePointer == -1) {
                    throw new ResourceException("Failed to create font face for: " + fontPath);
                }

                FT_Face face = FT_Face.create(facePointer);
                return new Font(face, fontPath, fontBuffer);
            }
        });
    }

    public void drawString(String text, Font font, int startDrawX, int startDrawY, float angle, int fontSize, double italicDegrees, int boldStrength, int red, int green, int blue, int alpha) {
        drawString(text, font, startDrawX, startDrawY, angle, fontSize, italicDegrees, boldStrength, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha);
    }
    public void drawString(String text, Font font, int startDrawX, int startDrawY, float angle,
                           int fontSize, double italicDegrees, int boldStrength,
                           int aRed, int aGreen, int aBlue, int aAlpha,
                           int bRed, int bGreen, int bBlue, int bAlpha,
                           int cRed, int cGreen, int cBlue, int cAlpha,
                           int dRed, int dGreen, int dBlue, int dAlpha
    ) {
        int centerX = (font.getStringWidth(text, fontSize) + startDrawX + startDrawX) / 2;
        int centerY = (font.getStringHeight(fontSize) + startDrawY + startDrawY) / 2;

        FT_Face face = font.face();
        int error = FT_Set_Pixel_Sizes(face, 0, fontSize);
        if (error != 0) {
            throw new ResourceException("Failed to set pixel sizes!");
        }

        float ascenderPx = ascenderCache.computeIfAbsent(font, f -> new Int2FloatOpenHashMap()).computeIfAbsent(fontSize, s -> Objects.requireNonNull(face.size()).metrics().ascender() / 64.0f);

        float penX = startDrawX;
        float penY = startDrawY + ascenderPx;

        FT_Matrix italicMatrix = null;
        if (italicDegrees != 0.0) {
            italicMatrix = italicDegreesCache.computeIfAbsent(italicDegrees, key -> {
                FT_Matrix matrix = FT_Matrix.malloc();
                matrix.xx(0x10000);
                matrix.xy((int) Math.round(Math.tan(Math.toRadians(key)) * 0x10000));
                matrix.yx(0);
                matrix.yy(0x10000);
                return matrix;
            });
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            CLongBuffer advanceBuffer = stack.mallocCLong(1);
            int cap = text.length();
            IntBuffer glyphIndices = stack.mallocInt(cap);
            FloatBuffer kerning = stack.mallocFloat(cap);
            FloatBuffer advances = stack.mallocFloat(cap);

            int glyphCount = 0;
            float totalWidth = 0.0f;
            int prevIdx = 0;
            boolean hasPrev = false;
            for (int offset = 0; offset < text.length(); ) {
                int codepoint = text.codePointAt(offset);
                offset += Character.charCount(codepoint);
                int gi = FT_Get_Char_Index(face, codepoint);
                if (gi == 0) continue;

                float kern = 0.0f;
                if (hasPrev) {
                    FT_Vector k = vector.get();
                    FT_Get_Kerning(face, prevIdx, gi, FT_KERNING_DEFAULT, k);
                    kern = k.x() / 64.0f;
                }
                advanceBuffer.clear();
                FT_Get_Advance(face, gi, FT_LOAD_DEFAULT, advanceBuffer);
                float adv = (int) advanceBuffer.get(0) / 65536.0f;

                glyphIndices.put(glyphCount, gi);
                kerning.put(glyphCount, kern);
                advances.put(glyphCount, adv);
                totalWidth += kern + adv;
                glyphCount++;

                prevIdx = gi;
                hasPrev = true;
            }

            if (glyphCount == 0) return;

            float widthCursor = 0.0f;
            for (int i = 0; i < glyphCount; i++) {
                int glyphIndex = glyphIndices.get(i);
                float kern = kerning.get(i);
                float advance = advances.get(i);

                if (i > 0) {
                    penX += kern;
                    widthCursor += kern;
                }

                float tStart = widthCursor / totalWidth;
                float tEnd = (widthCursor + advance) / totalWidth;

                int aRedNew = MathTool.round(aRed + (bRed - aRed) * tStart);
                int aGreenNew = MathTool.round(aGreen + (bGreen - aGreen) * tStart);
                int aBlueNew = MathTool.round(aBlue + (bBlue - aBlue) * tStart);
                int aAlphaNew = MathTool.round(aAlpha + (bAlpha - aAlpha) * tStart);

                int bRedNew = MathTool.round(aRed + (bRed - aRed) * tEnd);
                int bGreenNew = MathTool.round(aGreen + (bGreen - aGreen) * tEnd);
                int bBlueNew = MathTool.round(aBlue + (bBlue - aBlue) * tEnd);
                int bAlphaNew = MathTool.round(aAlpha + (bAlpha - aAlpha) * tEnd);

                int cRedNew = MathTool.round(cRed + (dRed - cRed) * tStart);
                int cGreenNew = MathTool.round(cGreen + (dGreen - cGreen) * tStart);
                int cBlueNew = MathTool.round(cBlue + (dBlue - cBlue) * tStart);
                int cAlphaNew = MathTool.round(cAlpha + (dAlpha - cAlpha) * tStart);

                int dRedNew = MathTool.round(cRed + (dRed - cRed) * tEnd);
                int dGreenNew = MathTool.round(cGreen + (dGreen - cGreen) * tEnd);
                int dBlueNew = MathTool.round(cBlue + (dBlue - cBlue) * tEnd);
                int dAlphaNew = MathTool.round(cAlpha + (dAlpha - cAlpha) * tEnd);

                penX = drawGlyph(font, glyphIndex, fontSize, italicDegrees, boldStrength, italicMatrix, penX, penY, centerX, centerY, angle,
                        aRedNew, aGreenNew, aBlueNew, aAlphaNew,
                        bRedNew, bGreenNew, bBlueNew, bAlphaNew,
                        cRedNew, cGreenNew, cBlueNew, cAlphaNew,
                        dRedNew, dGreenNew, dBlueNew, dAlphaNew
                );

                widthCursor += advance;
            }
        }
    }

    public abstract float drawGlyph(Font font, int glyphIndex, int fontSize, double italicDegrees, int boldStrength, FT_Matrix italicMatrix, float x, float y, int centerX, int centerY, float angle,
                                    int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                                    int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha
    );

    private Texture loadTexture(String path, boolean isNiceSlice, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom) {
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
                    MemoryUtil.memFree(buffer);
                    throw new ResourceException("Load texture failed:" + stbi_failure_reason());
                }

                MemoryUtil.memFree(buffer);
            }

            int width = widthBuffer.get();
            int height = heightBuffer.get();
            int channel = channelBuffer.get();

            int id = loadTexture(pixels, width, height, isLinear);

            stbi_image_free(pixels);

            return new Texture(isNiceSlice, type, stretchInner, id, width, height, channel, path, left, right, top, bottom);
        } catch (IOException e) {
            throw new ResourceException("Load texture failed: " + e.getMessage());
        }
    }
    private Texture loadTexture(byte[] bytes, boolean isNiceGrid, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelBuffer = stack.mallocInt(1);
            ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            ByteBuffer pixels = stbi_load_from_memory(buffer, widthBuffer, heightBuffer, channelBuffer, 4);
            if (pixels == null) {
                MemoryUtil.memFree(buffer);
                throw new ResourceException("Load texture failed:" + stbi_failure_reason());
            }
            int width = widthBuffer.get();
            int height = heightBuffer.get();
            int id = loadTexture(pixels, width, height, isLinear);
            MemoryUtil.memFree(buffer);
            stbi_image_free(pixels);
            return new Texture(isNiceGrid, type, stretchInner, id, width, height, channelBuffer.get(), null, left, right, top, bottom);
        }
    }

    @Override
    public Texture loadTexture(byte[] data) {
        return loadTexture(data, true);
    }
    @Override
    public Texture loadTexture(byte[] data, boolean isLinear) {
        return loadTexture(data, false, null, true, isLinear, -1, -1, -1, -1);
    }
    @Override
    public Texture loadTexture(String path) {
        return loadTexture(path, true);
    }
    @Override
    public Texture loadTexture(String path, boolean isLinear) {
        return loadTexture(path, false, null, true, isLinear, -1, -1, -1, -1);
    }

    @Override
    public Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, int left, int right, int top, int bottom) {
        return loadNiceSliceTexture(data, type, stretchInner, true, left, right, top, bottom);
    }
    @Override
    public Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom) {
        return loadTexture(data, true, type, stretchInner, isLinear, left, right, top, bottom);
    }
    @Override
    public Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, int left, int right, int top, int bottom) {
        return loadNiceSliceTexture(path, type, stretchInner, true, left, right, top, bottom);
    }
    @Override
    public Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom) {
        return loadTexture(path, true, type, stretchInner, isLinear, left, right, top, bottom);
    }

    public void setCursorShape(CursorShape cursorShapeInThisFrame) {
        this.cursorShapeInThisFrame = cursorShapeMap.get(cursorShapeInThisFrame.getGLFWValue());
    }
    public void setCursorMode(int cursorModeInThisFrame) {
        this.cursorModeInThisFrame = cursorModeInThisFrame;
    }
    public void setCursorMode(CursorMode cursorModeInThisFrame) {
        this.cursorModeInThisFrame = cursorModeInThisFrame.getGLFWValue();
    }
    public void setCursorShape(long cursorShapeInThisFrame) {
        this.cursorShapeInThisFrame = cursorShapeInThisFrame;
    }

    protected abstract int loadTexture(ByteBuffer data, int width, int height, boolean isLinear);

    public abstract void onFrameBufferSizeChange(int width, int height);

    public abstract RenderProviderType getProviderType();

    public void blurRegion(int x, int y, int width, int height, float angle, float strength) {
        if (width == 0 || height == 0 || strength == 0) return;
        strength = Math.clamp(strength, 0, 1);
        int radius = MathTool.round(strength * 100);
        blurFramebufferRegion(x, y, width, height, angle, radius);
    }
    protected abstract void blurFramebufferRegion(int x, int y, int width, int height, float angle, int radius);

    public GuiRender(long windowHandle) {
        if (IResourceManager.getIResourceManagerFromThreadLocal() == null) {
            IResourceManager.THREAD_LOCAL.set(this);
        }

        this.windowHandle = windowHandle;

        for (CursorShape shape : CursorShape.values()) {
            int glfwValue = shape.getGLFWValue();
            if (glfwValue < 0) continue;
            cursorShapeMap.put(glfwValue, glfwCreateStandardCursor(shape.getGLFWValue()));
        }
    }
}
