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
package top.untoldstudio.frostlumen.core.render.provider;

import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.*;
import top.untoldstudio.frostlumen.core.data.ThicknessPosition;
import top.untoldstudio.frostlumen.core.exception.RenderException;
import top.untoldstudio.frostlumen.core.exception.ResourceException;
import top.untoldstudio.frostlumen.core.font.Font;
import top.untoldstudio.frostlumen.core.gui.Window;
import top.untoldstudio.frostlumen.core.render.GuiRender;
import top.untoldstudio.frostlumen.core.render.IResourceManager;
import top.untoldstudio.frostlumen.core.render.RenderProviderType;
import top.untoldstudio.frostlumen.core.tool.DirectByteBuffer;
import top.untoldstudio.frostlumen.core.tool.MathTool;
import top.untoldstudio.frostlumen.core.tool.ResourceReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL32C.*;
import static org.lwjgl.util.freetype.FreeType.*;

public class OpenGLGuiRender extends GuiRender {
    private final Deque<RenderCommand> commands = new ArrayDeque<>();
    private final Long2ObjectMap<Double2ObjectOpenHashMap<GlyphInfo>> glyphCache = new Long2ObjectOpenHashMap<>();

    private final Matrix4f projectionMatrix = new Matrix4f();
    private final float[] projectionMatrixArray = new float[16];

    private final int triangleStride;
    private final int triangleProjectLocation;
    private final int triangleShaderProgram;
    private final int triangleVao;
    private final int triangleVbo;
    private int triangleVboCapacity;

    private final int shapeStride;
    private final int shapeProjectLocation;
    private final int shapeShaderProgram;
    private final int shapeVao;
    private final int shapeVbo;
    private int shapeVboCapacity;

    private final int textureStride;
    private final int textureProjectLocation;
    private final int textureSamplerLocation;
    private final int textureShaderProgram;
    private final int textureVao;
    private final int textureVbo;
    private int textureVboCapacity;

    private final int stringStride;
    private final int stringProjectLocation;
    private final int stringSamplerLocation;
    private final int stringShaderProgram;
    private final int stringVao;
    private final int stringVbo;
    private int stringVboCapacity;
    private int fontAtlasTextureId;
    private int atlasWidth;
    private int atlasHeight;
    private int atlasCursorX;
    private int atlasCursorY;
    private int atlasRowHeight;

    private static final int BLUR_DOWNSCALE = 2;
    private static final float BLUR_DOWNSCALE_INV = 1.0f / BLUR_DOWNSCALE;
    private static final int BLUR_PASSES = 5;
    private final DirectByteBuffer blurTempBuffer;
    private final int[] blurFbo = new int[2];
    private final int[] blurTexture = new int[2];
    private final int blurShaderProgram;
    private final int blurTexelSizeLocation;
    private final int blurDirectionLocation;
    private final int blurSamplerLocation;
    private final int blurUVScaleLocation;
    private final int blurRadiusLocation;
    private final int blurVao;
    private int blurTextureWidth;
    private int blurTextureHeight;

    private final Deque<SavedGLState> savedGLStateState = new ArrayDeque<>();
    private final Deque<SavedGLState> savedGLStatePool = new ArrayDeque<>();

    public OpenGLGuiRender(long windowHandle) {
        super(windowHandle);

        if (IResourceManager.getIResourceManagerFromThreadLocal() == null) {
            IResourceManager.THREAD_LOCAL.set(this);
        }

        saveContext();

        String triangleVertSource;
        String triangleFragSource;
        String shapeVertSource;
        String shapeFragSource;
        String textureVertSource;
        String textureFragSource;
        String stringVertSource;
        String stringFragSource;
        String blurVertSource;
        String blurFragSource;
        try {
            triangleVertSource = ResourceReader.readString("/shader/opengl/triangle/vert.glsl");
            triangleFragSource = ResourceReader.readString("/shader/opengl/triangle/frag.glsl");
            shapeVertSource = ResourceReader.readString("/shader/opengl/shape/vert.glsl");
            shapeFragSource = ResourceReader.readString("/shader/opengl/shape/frag.glsl");
            textureVertSource = ResourceReader.readString("/shader/opengl/texture/vert.glsl");
            textureFragSource = ResourceReader.readString("/shader/opengl/texture/frag.glsl");
            stringVertSource = ResourceReader.readString("/shader/opengl/string/vert.glsl");
            stringFragSource = ResourceReader.readString("/shader/opengl/string/frag.glsl");
            blurVertSource = ResourceReader.readString("/shader/opengl/blur/vert.glsl");
            blurFragSource = ResourceReader.readString("/shader/opengl/blur/frag.glsl");
        } catch (IOException e) {
            throw new ResourceException("Cannot read shader source!");
        }
        triangleShaderProgram = createProgram(triangleVertSource, triangleFragSource, Map.of(
                0, "aScreenPos",
                1, "aCenterPos",
                2, "aAngle",
                3, "aColor"
        ));
        shapeShaderProgram = createProgram(shapeVertSource, shapeFragSource, Map.of(
                0, "aScreenPos",
                1, "aCenterPos",
                2, "aAngle",
                3, "aLocalPos",
                4, "aHalfSize",
                5, "aFillColor",
                6, "aBorderColor",
                7, "aCornerRadii",
                8, "aEdgeThickness",
                9, "aBorderPosition"
        ));
        textureShaderProgram = createProgram(textureVertSource, textureFragSource, Map.of(
                0, "aScreenPos",
                1, "aCenterPos",
                2, "aAngle",
                3, "aTexCoord",
                4, "aColor"
        ));
        stringShaderProgram = createProgram(stringVertSource, stringFragSource, Map.of(
                0, "aScreenPos",
                1, "aCenterPos",
                2, "aAngle",
                3, "aTexCoord",
                4, "aColor"
        ));
        blurShaderProgram = createProgram(blurVertSource, blurFragSource, Map.of());

        triangleVao = glGenVertexArrays();
        triangleVbo = glGenBuffers();
        glBindVertexArray(triangleVao);
        glBindBuffer(GL_ARRAY_BUFFER, triangleVbo);
        triangleStride = 2 * Integer.BYTES + 2 * Integer.BYTES + Float.BYTES + 4 * Byte.BYTES;
        int triangleOffset = 0;
        triangleVboCapacity = triangleStride * 1024;
        glBufferData(GL_ARRAY_BUFFER, triangleVboCapacity, GL_STREAM_DRAW);
        glVertexAttribIPointer(0, 2, GL_INT, triangleStride, triangleOffset);
        triangleOffset += 2 * Integer.BYTES;
        glVertexAttribIPointer(1, 2, GL_INT, triangleStride, triangleOffset);
        triangleOffset += 2 * Integer.BYTES;
        glVertexAttribPointer(2, 1, GL_FLOAT, false, triangleStride, triangleOffset);
        triangleOffset += Float.BYTES;
        glVertexAttribPointer(3, 4, GL_UNSIGNED_BYTE, true, triangleStride, triangleOffset);
        enableVertexAttributes(3);
        triangleProjectLocation = glGetUniformLocation(triangleShaderProgram, "uProjection");

        shapeVao = glGenVertexArrays();
        shapeVbo = glGenBuffers();
        glBindVertexArray(shapeVao);
        glBindBuffer(GL_ARRAY_BUFFER, shapeVbo);
        shapeStride = 2 * Integer.BYTES + 2 * Integer.BYTES + Float.BYTES + 2 * Float.BYTES + 2 * Float.BYTES + 4 * Byte.BYTES + 4 * Byte.BYTES + 4 * Integer.BYTES + 4 * Integer.BYTES + Integer.BYTES;
        int shapeOffset = 0;
        shapeVboCapacity = shapeStride * 512;
        glBufferData(GL_ARRAY_BUFFER, shapeVboCapacity, GL_STREAM_DRAW);
        glVertexAttribIPointer(0, 2, GL_INT, shapeStride, shapeOffset);
        shapeOffset += 2 * Integer.BYTES;
        glVertexAttribIPointer(1, 2, GL_INT, shapeStride, shapeOffset);
        shapeOffset += 2 * Integer.BYTES;
        glVertexAttribPointer(2, 1, GL_FLOAT, false, shapeStride, shapeOffset);
        shapeOffset += Float.BYTES;
        glVertexAttribPointer(3, 2, GL_FLOAT, false, shapeStride, shapeOffset);
        shapeOffset += 2 * Float.BYTES;
        glVertexAttribPointer(4, 2, GL_FLOAT, false, shapeStride, shapeOffset);
        shapeOffset += 2 * Float.BYTES;
        glVertexAttribPointer(5, 4, GL_UNSIGNED_BYTE, true, shapeStride, shapeOffset);
        shapeOffset += 4 * Byte.BYTES;
        glVertexAttribPointer(6, 4, GL_UNSIGNED_BYTE, true, shapeStride, shapeOffset);
        shapeOffset += 4 * Byte.BYTES;
        glVertexAttribIPointer(7, 4, GL_INT, shapeStride, shapeOffset);
        shapeOffset += 4 * Integer.BYTES;
        glVertexAttribIPointer(8, 4, GL_INT, shapeStride, shapeOffset);
        shapeOffset += 4 * Integer.BYTES;
        glVertexAttribIPointer(9, 1, GL_INT, shapeStride, shapeOffset);
        enableVertexAttributes(9);
        shapeProjectLocation = glGetUniformLocation(shapeShaderProgram, "uProjection");

        textureVao = glGenVertexArrays();
        textureVbo = glGenBuffers();
        glBindVertexArray(textureVao);
        glBindBuffer(GL_ARRAY_BUFFER, textureVbo);
        textureStride = 2 * Integer.BYTES + 2 * Integer.BYTES + Float.BYTES + 2 * Float.BYTES + 4 * Byte.BYTES;
        int textureOffset = 0;
        textureVboCapacity = textureStride * 1024;
        glBufferData(GL_ARRAY_BUFFER, textureVboCapacity, GL_STREAM_DRAW);
        glVertexAttribIPointer(0, 2, GL_INT, textureStride, textureOffset);
        textureOffset += 2 * Integer.BYTES;
        glVertexAttribIPointer(1, 2, GL_INT, textureStride, textureOffset);
        textureOffset += 2 * Integer.BYTES;
        glVertexAttribPointer(2, 1, GL_FLOAT, false, textureStride, textureOffset);
        textureOffset += Float.BYTES;
        glVertexAttribPointer(3, 2, GL_FLOAT, false, textureStride, textureOffset);
        textureOffset += 2 * Float.BYTES;
        glVertexAttribPointer(4, 4, GL_UNSIGNED_BYTE, true, textureStride, textureOffset);
        enableVertexAttributes(4);
        textureProjectLocation = glGetUniformLocation(textureShaderProgram, "uProjection");
        textureSamplerLocation = glGetUniformLocation(textureShaderProgram, "uTexture");

        stringVao = glGenVertexArrays();
        stringVbo = glGenBuffers();
        glBindVertexArray(stringVao);
        glBindBuffer(GL_ARRAY_BUFFER, stringVbo);
        stringStride = 2 * Integer.BYTES + 2 * Integer.BYTES + Float.BYTES + 2 * Float.BYTES + 4 * Byte.BYTES;
        int stringOffset = 0;
        stringVboCapacity = stringStride * 1024;
        glBufferData(GL_ARRAY_BUFFER, stringVboCapacity, GL_STREAM_DRAW);
        glVertexAttribIPointer(0, 2, GL_INT, stringStride, stringOffset);
        stringOffset += 2 * Integer.BYTES;
        glVertexAttribIPointer(1, 2, GL_INT, stringStride, stringOffset);
        stringOffset += 2 * Integer.BYTES;
        glVertexAttribPointer(2, 1, GL_FLOAT, false, stringStride, stringOffset);
        stringOffset += Float.BYTES;
        glVertexAttribPointer(3, 2, GL_FLOAT, false, stringStride, stringOffset);
        stringOffset += 2 * Float.BYTES;
        glVertexAttribPointer(4, 4, GL_UNSIGNED_BYTE, true, stringStride, stringOffset);
        enableVertexAttributes(4);
        stringProjectLocation = glGetUniformLocation(stringShaderProgram, "uProjection");
        stringSamplerLocation = glGetUniformLocation(stringShaderProgram, "uTexture");
        fontAtlasTextureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, fontAtlasTextureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        atlasCursorX = 0;
        atlasCursorY = 0;
        atlasRowHeight = 0;
        atlasWidth = 2048;
        atlasHeight = 2048;
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, atlasWidth, atlasHeight, 0, GL_RED, GL_UNSIGNED_BYTE, (ByteBuffer) null);

        blurVao = glGenVertexArrays();
        blurTexelSizeLocation = glGetUniformLocation(blurShaderProgram, "uTexelSize");
        blurDirectionLocation = glGetUniformLocation(blurShaderProgram, "uDirection");
        blurSamplerLocation = glGetUniformLocation(blurShaderProgram, "uTexture");
        blurUVScaleLocation = glGetUniformLocation(blurShaderProgram, "uUVScale");
        blurRadiusLocation = glGetUniformLocation(blurShaderProgram, "uRadius");

        blurTempBuffer = new DirectByteBuffer(textureStride * 4);

        restoreContext();
    }

    private void enableVertexAttributes(int target) {
        for (int i = 0; i <= target; i++) {
            glEnableVertexAttribArray(i);
        }
    }

    @Override
    public void init() {
        Window window = Window.get(windowHandle);
        int w = Math.max(1, window.getFrameBufferWidth() / BLUR_DOWNSCALE);
        int h = Math.max(1, window.getFrameBufferHeight() / BLUR_DOWNSCALE);
        rebuildBlurTextures(w, h);
    }

    @Override
    public void onFrameBufferSizeChange(int width, int height) {
        glViewport(0, 0, width, height);
        int halfW = Math.max(1, width / BLUR_DOWNSCALE);
        int halfH = Math.max(1, height / BLUR_DOWNSCALE);
        if (halfW > blurTextureWidth || halfH > blurTextureHeight) {
            rebuildBlurTextures(Math.max(halfW, blurTextureWidth), Math.max(halfH, blurTextureHeight));
        }
    }

    @Override
    public void begin() {
        Window window = Window.get(windowHandle);
        int windowWidth = window.getFrameBufferWidth();
        int windowHeight = window.getFrameBufferHeight();

        glViewport(0, 0, windowWidth, windowHeight);

        projectionMatrix.setOrtho(0.0f, windowWidth, windowHeight, 0.0f, -1.0f, 1.0f);
        projectionMatrix.get(projectionMatrixArray);

        glUseProgram(triangleShaderProgram);
        glUniformMatrix4fv(triangleProjectLocation, false, projectionMatrixArray);
        glUseProgram(shapeShaderProgram);
        glUniformMatrix4fv(shapeProjectLocation, false, projectionMatrixArray);
        glUseProgram(textureShaderProgram);
        glUniformMatrix4fv(textureProjectLocation, false, projectionMatrixArray);
        glUseProgram(stringShaderProgram);
        glUniformMatrix4fv(stringProjectLocation, false, projectionMatrixArray);

        glColorMask(true, true, true, true);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glDisable(GL_SCISSOR_TEST);
        glDisable(GL_STENCIL_TEST);
        glDisable(GL_COLOR_LOGIC_OP);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void end() {
    }

    private int createProgram(String vertSource, String fragSource, Map<Integer, String> attributeLocations) {
        int vertShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertShader, vertSource);
        glCompileShader(vertShader);
        if (glGetShaderi(vertShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new ResourceException(glGetShaderInfoLog(vertShader));
        }

        int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, fragSource);
        glCompileShader(fragShader);
        if (glGetShaderi(fragShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new ResourceException(glGetShaderInfoLog(fragShader));
        }

        int program = glCreateProgram();
        glAttachShader(program, vertShader);
        glAttachShader(program, fragShader);
        for (Map.Entry<Integer, String> attributeLocation : attributeLocations.entrySet()) {
            glBindAttribLocation(program, attributeLocation.getKey(), attributeLocation.getValue());
        }

        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw new ResourceException(glGetProgramInfoLog(program));
        }

        glDeleteShader(vertShader);
        glDeleteShader(fragShader);

        return program;
    }

    @Override
    public int loadTexture(ByteBuffer pixels, int width, int height) {
        int textureId = glGenTextures();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

        return textureId;
    }

    @Override
    public void drawTriangle(int ax, int ay, int bx, int by, int cx, int cy, int centerX, int centerY, float angle,
                             int aRed, int aGreen, int aBlue, int aAlpha,
                             int bRed, int bGreen, int bBlue, int bAlpha,
                             int cRed, int cGreen, int cBlue, int cAlpha
    ) {
        boolean shouldPush = true;
        DirectByteBuffer buffer;
        TriangleBatch batch;

        if (commands.peek() instanceof TriangleBatch currentBatch) {
            buffer = currentBatch.buffer;
            batch = currentBatch;
            shouldPush = false;
        } else {
            batch = allocTriangleBatch();
            buffer = batch.buffer;
        }

        buffer.writeInt(ax, ay);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeBytesFromIntsWithForcedConversion(aRed, aGreen, aBlue, aAlpha);

        buffer.writeInt(bx, by);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeBytesFromIntsWithForcedConversion(bRed, bGreen, bBlue, bAlpha);

        buffer.writeInt(cx, cy);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeBytesFromIntsWithForcedConversion(cRed, cGreen, cBlue, cAlpha);

        if (shouldPush) {
            commands.push(batch);
        }
    }

    @Override
    public void drawShape(int minX, int minY, int maxX, int maxY, float angle,
                          int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                          int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha,
                          int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii,
                          int aBorderThickness, int bBorderThickness, int cBorderThickness, int dBorderThickness,
                          int borderRed, int borderGreen, int borderBlue, int borderAlpha,
                          ThicknessPosition position
    ) {
        boolean shouldPush = true;
        ShapeBatch batch;
        if (commands.peek() instanceof ShapeBatch currentBatch) {
            batch = currentBatch;
            shouldPush = false;
        } else {
            batch = allocShapeBatch();
        }

        DirectByteBuffer buffer = batch.buffer;

        byte positionOrder = position.getOrder();
        float halfWidth  = (maxX - minX) * 0.5f;
        float halfHeight = (maxY - minY) * 0.5f;
        int expand = Math.max(
                Math.max(aBorderThickness, bBorderThickness),
                Math.max(cBorderThickness, dBorderThickness)
        );

        if (position == ThicknessPosition.INSIDE) expand = 0;
        else if (position == ThicknessPosition.CENTER) expand /= 2;

        float ex = expand;
        float exHalfWidth = halfWidth + ex;
        float exHalfHeight = halfHeight + ex;
        int exMinX = minX - expand;
        int exMinY = minY - expand;
        int exMaxX = maxX + expand;
        int exMaxY = maxY + expand;

        int centerX = (minX + maxX) / 2;
        int centerY = (minY + maxY) / 2;

        buffer.writeInt(exMinX, exMinY);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(-exHalfWidth, -exHalfHeight, halfWidth, halfHeight);
        buffer.writeBytesFromIntsWithForcedConversion(aRed, aGreen, aBlue, aAlpha);
        buffer.writeBytesFromIntsWithForcedConversion(borderRed, borderGreen, borderBlue, borderAlpha);
        buffer.writeInt(aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii);
        buffer.writeInt(aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness);
        buffer.writeInt(positionOrder);

        buffer.writeInt(exMaxX, exMinY);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(exHalfWidth, -exHalfHeight, halfWidth, halfHeight);
        buffer.writeBytesFromIntsWithForcedConversion(bRed, bGreen, bBlue, bAlpha);
        buffer.writeBytesFromIntsWithForcedConversion(borderRed, borderGreen, borderBlue, borderAlpha);
        buffer.writeInt(aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii);
        buffer.writeInt(aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness);
        buffer.writeInt(positionOrder);

        buffer.writeInt(exMaxX, exMaxY);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(exHalfWidth, exHalfHeight, halfWidth, halfHeight);
        buffer.writeBytesFromIntsWithForcedConversion(dRed, dGreen, dBlue, dAlpha);
        buffer.writeBytesFromIntsWithForcedConversion(borderRed, borderGreen, borderBlue, borderAlpha);
        buffer.writeInt(aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii);
        buffer.writeInt(aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness);
        buffer.writeInt(positionOrder);

        buffer.writeInt(exMinX, exMaxY);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(-exHalfWidth, exHalfHeight, halfWidth, halfHeight);
        buffer.writeBytesFromIntsWithForcedConversion(cRed, cGreen, cBlue, cAlpha);
        buffer.writeBytesFromIntsWithForcedConversion(borderRed, borderGreen, borderBlue, borderAlpha);
        buffer.writeInt(aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii);
        buffer.writeInt(aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness);
        buffer.writeInt(positionOrder);

        if (shouldPush) {
            commands.push(batch);
        }
    }

    @Override
    public void drawTexture(int textureId, int minX, int minY, int maxX, int maxY, int centerX, int centerY, float angle, float u0, float v0, float u1, float v1,
                            int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                            int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha
    ) {
        boolean shouldPush = true;
        TextureBatch batch;
        if (commands.peek() instanceof TextureBatch currentBatch && currentBatch.textureId == textureId) {
            batch = currentBatch;
            shouldPush = false;
        } else {
            batch = allocTextureBatch(textureId);
        }

        DirectByteBuffer buffer = batch.buffer;

        buffer.writeInt(minX, minY);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(u0, v0);
        buffer.writeBytesFromIntsWithForcedConversion(aRed, aGreen, aBlue, aAlpha);

        buffer.writeInt(maxX, minY);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(u1, v0);
        buffer.writeBytesFromIntsWithForcedConversion(bRed, bGreen, bBlue, bAlpha);

        buffer.writeInt(maxX, maxY);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(u1, v1);
        buffer.writeBytesFromIntsWithForcedConversion(dRed, dGreen, dBlue, dAlpha);

        buffer.writeInt(minX, maxY);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(u0, v1);
        buffer.writeBytesFromIntsWithForcedConversion(cRed, cGreen, cBlue, cAlpha);

        if (shouldPush) {
            commands.push(batch);
        }
    }

    @Override
    public float drawGlyph(Font font, int glyphIndex, int fontSize, double italicDegrees, int boldStrength, FT_Matrix italicMatrix, float x, float y, int centerX, int centerY, float angle,
                           int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                           int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha
    ) {
        Double2ObjectMap<GlyphInfo> map = glyphCache.computeIfAbsent(((long) font.id() << 41) | ((long) fontSize << 28) | ((long) glyphIndex << 10) | boldStrength, key -> new Double2ObjectOpenHashMap<>());
        GlyphInfo info = map.get(italicDegrees);
        if (info == null) {
            FT_Face face = font.face();
            if (FT_Load_Glyph(face, glyphIndex, FT_LOAD_NO_BITMAP) != 0) throw new ResourceException("Failed to load glyph!");
            FT_GlyphSlot slot = face.glyph();
            assert slot != null;

            if (italicMatrix != null) {
                FT_Outline_Transform(slot.outline(), italicMatrix);
            }

            if (boldStrength > 0) {
                FT_Outline_Embolden(slot.outline(), boldStrength * 64L);
            }

            if (FT_Render_Glyph(slot, FT_RENDER_MODE_NORMAL) != 0) throw new RenderException("Failed to render glyph!");
            FT_Bitmap bitmap = slot.bitmap();
            int bitmapLeft = slot.bitmap_left();
            int bitmapTop = slot.bitmap_top();
            FT_Vector advance = slot.advance();
            float advanceX = advance.x() / 64.0f;

            if (atlasCursorX + bitmap.width() > atlasWidth) {
                atlasCursorX = 0;
                atlasCursorY += atlasRowHeight;
                atlasRowHeight = 0;
            }
            if (atlasCursorY + bitmap.rows() > atlasHeight) {
                expandAtlas();
            }

            glBindTexture(GL_TEXTURE_2D, fontAtlasTextureId);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glPixelStorei(GL_UNPACK_ROW_LENGTH, bitmap.pitch());
            glTexSubImage2D(GL_TEXTURE_2D, 0, atlasCursorX, atlasCursorY, bitmap.width(), bitmap.rows(), GL_RED, GL_UNSIGNED_BYTE, Objects.requireNonNull(bitmap.buffer(bitmap.pitch() * bitmap.rows())));
            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);

            float u0 = (float) atlasCursorX / atlasWidth;
            float u1 = (float) (atlasCursorX + bitmap.width()) / atlasWidth;
            float v0 = (float) atlasCursorY / atlasHeight;
            float v1 = (float) (atlasCursorY + bitmap.rows()) / atlasHeight;

            atlasCursorX += bitmap.width();
            if (bitmap.rows() > atlasRowHeight) atlasRowHeight = bitmap.rows();

            info = new GlyphInfo(u0, v0, u1, v1, bitmapLeft, bitmapTop, bitmap.width(), bitmap.rows(), advanceX);
            map.put(italicDegrees, info);
        }

        int glyphX = MathTool.round(x + info.bitmapLeft);
        int glyphY = MathTool.round(y - info.bitmapTop);
        int x1 = glyphX + info.bitmapWidth;
        int y1 = glyphY + info.bitmapHeight;

        float u0 = info.u0;
        float v0 = info.v0;
        float u1 = info.u1;
        float v1 = info.v1;

        boolean shouldPush = true;
        StringBatch batch;
        if (commands.peek() instanceof StringBatch currentBatch) {
            shouldPush = false;
            batch = currentBatch;
        } else {
            batch = allocStringBatch();
        }

        DirectByteBuffer buffer = batch.buffer;

        buffer.writeInt(glyphX, glyphY);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(u0, v0);
        buffer.writeBytesFromIntsWithForcedConversion(aRed, aGreen, aBlue, aAlpha);

        buffer.writeInt(x1, glyphY);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(u1, v0);
        buffer.writeBytesFromIntsWithForcedConversion(bRed, bGreen, bBlue, bAlpha);

        buffer.writeInt(glyphX, y1);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(u0, v1);
        buffer.writeBytesFromIntsWithForcedConversion(cRed, cGreen, cBlue, cAlpha);

        buffer.writeInt(x1, glyphY);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(u1, v0);
        buffer.writeBytesFromIntsWithForcedConversion(bRed, bGreen, bBlue, bAlpha);

        buffer.writeInt(glyphX, y1);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(u0, v1);
        buffer.writeBytesFromIntsWithForcedConversion(cRed, cGreen, cBlue, cAlpha);

        buffer.writeInt(x1, y1);
        buffer.writeInt(centerX, centerY);
        buffer.writeFloat(angle);
        buffer.writeFloat(u1, v1);
        buffer.writeBytesFromIntsWithForcedConversion(dRed, dGreen, dBlue, dAlpha);

        if (shouldPush) {
            commands.push(batch);
        }

        return x + info.advanceX;
    }

    @Override
    protected void blurFramebufferRegion(int x, int y, int width, int height, float angle, int radius) {
        submitBuffer();
        saveContext();

        Window window = Window.get(windowHandle);
        int windowWidth = window.getFrameBufferWidth();
        int windowHeight = window.getFrameBufferHeight();

        int halfW = Math.max(1, width / BLUR_DOWNSCALE);
        int halfH = Math.max(1, height / BLUR_DOWNSCALE);

        if (halfW > blurTextureWidth || halfH > blurTextureHeight) {
            rebuildBlurTextures(Math.max(halfW, blurTextureWidth), Math.max(halfH, blurTextureHeight));
        }

        int glY0 = windowHeight - (y + height);
        int glY1 = windowHeight - y;

        glDisable(GL_SCISSOR_TEST);

        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, blurFbo[0]);
        glBlitFramebuffer(x, glY0, x + width, glY1, 0, 0, halfW, halfH, GL_COLOR_BUFFER_BIT, GL_LINEAR);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glBindVertexArray(blurVao);
        glUseProgram(blurShaderProgram);
        glUniform2f(blurTexelSizeLocation, 1f / blurTextureWidth, 1f / blurTextureHeight);
        glUniform2f(blurUVScaleLocation, (float) halfW / blurTextureWidth, (float) halfH / blurTextureHeight);
        glActiveTexture(GL_TEXTURE0);
        glUniform1i(blurSamplerLocation, 0);

        float perPassRadius = Math.max(1.0f, radius * BLUR_DOWNSCALE_INV / BLUR_PASSES);
        glUniform1f(blurRadiusLocation, perPassRadius);

        for (int i = 0; i < BLUR_PASSES; i++) {
            glBindFramebuffer(GL_FRAMEBUFFER, blurFbo[1]);
            glViewport(0, 0, halfW, halfH);
            glUniform1i(blurDirectionLocation, 0);
            glBindTexture(GL_TEXTURE_2D, blurTexture[0]);
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

            glBindFramebuffer(GL_FRAMEBUFFER, blurFbo[0]);
            glViewport(0, 0, halfW, halfH);
            glUniform1i(blurDirectionLocation, 1);
            glBindTexture(GL_TEXTURE_2D, blurTexture[1]);
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, windowWidth, windowHeight);
        glUseProgram(textureShaderProgram);
        glUniformMatrix4fv(textureProjectLocation, false, projectionMatrixArray);

        glEnable(GL_SCISSOR_TEST);
        glScissor(x, glY0, width, height);

        glBindVertexArray(textureVao);
        glBindBuffer(GL_ARRAY_BUFFER, textureVbo);

        int centerX = x + width / 2;
        int centerY = y + height / 2;
        float uMax = (float) halfW / blurTextureWidth;
        float vMax = (float) halfH / blurTextureHeight;

        DirectByteBuffer temp = blurTempBuffer;
        temp.clear();

        temp.writeInt(x, y);
        temp.writeInt(centerX, centerY);
        temp.writeFloat(0f);
        temp.writeFloat(0f, vMax);
        temp.writeBytesFromIntsWithForcedConversion(255, 255, 255, 255);

        temp.writeInt(x + width, y);
        temp.writeInt(centerX, centerY);
        temp.writeFloat(0f);
        temp.writeFloat(uMax, vMax);
        temp.writeBytesFromIntsWithForcedConversion(255, 255, 255, 255);

        temp.writeInt(x + width, y + height);
        temp.writeInt(centerX, centerY);
        temp.writeFloat(0f);
        temp.writeFloat(uMax, 0f);
        temp.writeBytesFromIntsWithForcedConversion(255, 255, 255, 255);

        temp.writeInt(x, y + height);
        temp.writeInt(centerX, centerY);
        temp.writeFloat(0f);
        temp.writeFloat(0f, 0f);
        temp.writeBytesFromIntsWithForcedConversion(255, 255, 255, 255);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, blurTexture[0]);
        glUniform1i(textureSamplerLocation, 0);

        glBufferSubData(GL_ARRAY_BUFFER, 0, temp.getNioDirectByteBuffer());
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

        glDisable(GL_SCISSOR_TEST);

        restoreContext();
    }

    private void rebuildBlurTextures(int width, int height) {
        saveContext();

        for (int i = 0; i < 2; i++) {
            if (blurTexture[i] != 0) {
                glDeleteTextures(blurTexture[i]);
            }
            if (blurFbo[i] != 0) {
                glDeleteFramebuffers(blurFbo[i]);
            }
        }

        blurTextureWidth = width;
        blurTextureHeight = height;

        for (int i = 0; i < 2; i++) {
            blurTexture[i] = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, blurTexture[i]);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, blurTextureWidth, blurTextureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            blurFbo[i] = glGenFramebuffers();
            glBindFramebuffer(GL_FRAMEBUFFER, blurFbo[i]);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, blurTexture[i], 0);

            if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
                throw new ResourceException("Blur FBO " + i + " incomplete");
            }
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        restoreContext();
    }

    private void expandAtlas() {
        int oldWidth = atlasWidth;
        int oldHeight = atlasHeight;

        int size = oldWidth * oldHeight;

        ByteBuffer oldData = MemoryUtil.memAlloc(size);

        try {
            glBindTexture(GL_TEXTURE_2D, fontAtlasTextureId);
            glGetTexImage(GL_TEXTURE_2D, 0, GL_RED, GL_UNSIGNED_BYTE, oldData);

            int newTexId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, newTexId);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            int newWidth = oldWidth * 2;
            int newHeight = oldHeight * 2;
            glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, newWidth, newHeight, 0, GL_RED, GL_UNSIGNED_BYTE, (ByteBuffer) null);

            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, oldWidth, oldHeight, GL_RED, GL_UNSIGNED_BYTE, oldData);

            glDeleteTextures(fontAtlasTextureId);
            fontAtlasTextureId = newTexId;

            atlasWidth = newWidth;
            atlasHeight = newHeight;

            atlasCursorX = 0;
            atlasCursorY = oldHeight;
            atlasRowHeight = 0;

            for (Double2ObjectMap<GlyphInfo> map : glyphCache.values()) {
                map.clear();
            }
        } finally {
            MemoryUtil.memFree(oldData);
        }
    }

    @Override
    public void saveContext() {
        SavedGLState savedGLState = allocSavedGLState();

        savedGLState.blend = glIsEnabled(GL_BLEND);
        savedGLState.depthTest = glIsEnabled(GL_DEPTH_TEST);
        savedGLState.cullFace = glIsEnabled(GL_CULL_FACE);
        savedGLState.scissorTest = glIsEnabled(GL_SCISSOR_TEST);
        savedGLState.stencilTest = glIsEnabled(GL_STENCIL_TEST);
        savedGLState.colorLogicOp = glIsEnabled(GL_COLOR_LOGIC_OP);

        savedGLState.program = glGetInteger(GL_CURRENT_PROGRAM);
        savedGLState.frameBufferBinding = glGetInteger(GL_FRAMEBUFFER_BINDING);
        savedGLState.vertexArrayBinding = glGetInteger(GL_VERTEX_ARRAY_BINDING);
        savedGLState.arrayBufferBinding = glGetInteger(GL_ARRAY_BUFFER_BINDING);
        savedGLState.blendSrcRgb = glGetInteger(GL_BLEND_SRC_RGB);
        savedGLState.blendSrcAlpha = glGetInteger(GL_BLEND_SRC_ALPHA);
        savedGLState.blendDstRgb = glGetInteger(GL_BLEND_DST_RGB);
        savedGLState.blendDstAlpha = glGetInteger(GL_BLEND_DST_ALPHA);
        savedGLState.logicOpMode = glGetInteger(GL_LOGIC_OP_MODE);
        savedGLState.blendEquationRgb = glGetInteger(GL_BLEND_EQUATION_RGB);
        savedGLState.blendEquationAlpha = glGetInteger(GL_BLEND_EQUATION_ALPHA);
        savedGLState.activeTexture = glGetInteger(GL_ACTIVE_TEXTURE);
        savedGLState.bindTexture = glGetInteger(GL_TEXTURE_BINDING_2D);

        int[] int4Array = new int[4];

        glGetIntegerv(GL_COLOR_WRITEMASK, int4Array);
        boolean[] colorWriteMask = savedGLState.colorWriteMask;
        colorWriteMask[0] = int4Array[0] == GL_TRUE;
        colorWriteMask[1] = int4Array[1] == GL_TRUE;
        colorWriteMask[2] = int4Array[2] == GL_TRUE;
        colorWriteMask[3] = int4Array[3] == GL_TRUE;

        glGetIntegerv(GL_VIEWPORT, savedGLState.viewport);
        glGetIntegerv(GL_SCISSOR_BOX, savedGLState.scissorTestBox);

        savedGLStateState.push(savedGLState);
    }

    @Override
    public void restoreContext() {
        SavedGLState savedGLState = savedGLStateState.pop();

        enableGLState(GL_BLEND, savedGLState.blend);
        enableGLState(GL_DEPTH_TEST, savedGLState.depthTest);
        enableGLState(GL_CULL_FACE, savedGLState.cullFace);
        enableGLState(GL_SCISSOR_TEST, savedGLState.scissorTest);
        enableGLState(GL_STENCIL_TEST, savedGLState.stencilTest);
        enableGLState(GL_COLOR_LOGIC_OP, savedGLState.colorLogicOp);

        glUseProgram(savedGLState.program);
        glBindFramebuffer(GL_FRAMEBUFFER, savedGLState.frameBufferBinding);
        glBindVertexArray(savedGLState.vertexArrayBinding);
        glBindBuffer(GL_ARRAY_BUFFER, savedGLState.arrayBufferBinding);
        glColorMask(savedGLState.colorWriteMask[0], savedGLState.colorWriteMask[1], savedGLState.colorWriteMask[2], savedGLState.colorWriteMask[3]);
        glBlendFuncSeparate(savedGLState.blendSrcRgb, savedGLState.blendDstRgb, savedGLState.blendSrcAlpha, savedGLState.blendDstAlpha);
        glLogicOp(savedGLState.logicOpMode);
        glBlendEquationSeparate(savedGLState.blendEquationRgb, savedGLState.blendEquationAlpha);
        glActiveTexture(savedGLState.activeTexture);
        glBindTexture(GL_TEXTURE_2D, savedGLState.bindTexture);

        glViewport(savedGLState.viewport[0], savedGLState.viewport[1], savedGLState.viewport[2], savedGLState.viewport[3]);
        glScissor(savedGLState.scissorTestBox[0], savedGLState.scissorTestBox[1], savedGLState.scissorTestBox[2], savedGLState.scissorTestBox[3]);

        savedGLStatePool.push(savedGLState);
    }

    private SavedGLState allocSavedGLState() {
        if (savedGLStatePool.isEmpty()) return new SavedGLState();
        return savedGLStatePool.pop();
    }

    private void enableGLState(int setting, boolean value) {
        if (value) {
            glEnable(setting);
        } else {
            glDisable(setting);
        }
    }

    @Override
    public void submitBuffer() {
        Iterator<RenderCommand> iterator = commands.descendingIterator();
        while (iterator.hasNext()) {
            RenderCommand command = iterator.next();
            if (command instanceof RenderBatch batch && batch.buffer.getWrittenBytes() == 0) continue;
            command.execute();
            command.destroy();
        }
        commands.clear();
    }

    @Override
    public void enableScissor(int x, int y, int width, int height) {
        int glY = Window.get(windowHandle).getFrameBufferHeight() - y - height;

        addStateCommand(() -> {
            glScissor(x, glY, width, height);
            glEnable(GL_SCISSOR_TEST);
        });
    }
    @Override
    public void disableScissor() {
        addStateCommand(() -> glDisable(GL_SCISSOR_TEST));
    }

    @Override
    public RenderProviderType getProviderType() {
        return RenderProviderType.OPENGL;
    }

    private final Deque<TriangleBatch> triangleBatchPool = new ArrayDeque<>();
    private final Deque<ShapeBatch> shapeBatchPool = new ArrayDeque<>();
    private final Deque<TextureBatch> textureBatchPool = new ArrayDeque<>();
    private final Deque<StringBatch> stringBatchPool = new ArrayDeque<>();
    private final Deque<StateCommand> stateCommandPool = new ArrayDeque<>();

    private <T extends RenderBatch> T allocBatch(Deque<T> pool, Supplier<T> supplier) {
        if (pool.isEmpty()) return supplier.get();
        T result = pool.pop();
        result.buffer.clear();
        return result;
    }
    private TriangleBatch allocTriangleBatch() {
        return allocBatch(triangleBatchPool, () -> new TriangleBatch(new DirectByteBuffer(triangleVboCapacity * 3)));
    }
    private ShapeBatch allocShapeBatch() {
        return allocBatch(shapeBatchPool, () -> new ShapeBatch(new DirectByteBuffer(shapeVboCapacity * 4)));
    }
    private TextureBatch allocTextureBatch(int textureId) {
        TextureBatch batch = allocBatch(textureBatchPool, () -> new TextureBatch(new DirectByteBuffer(textureStride * 4), textureId));
        batch.textureId = textureId;
        return batch;
    }
    private StringBatch allocStringBatch() {
        return allocBatch(stringBatchPool, () -> new StringBatch(new DirectByteBuffer(stringStride * 4)));
    }

    private void addStateCommand(Runnable command) {
        if (stateCommandPool.isEmpty()) {
            commands.push(new StateCommand(command));
            return;
        }
        StateCommand stateCommand = stateCommandPool.pop();
        stateCommand.command = command;
        commands.push(stateCommand);
    }

    private abstract static class RenderBatch implements RenderCommand {
        DirectByteBuffer buffer;

        RenderBatch(DirectByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public abstract void execute();

        @Override
        public abstract void destroy();

        protected int executeBatch(int program, int vao, int vbo, int mode, int stride, int capacity) {
            return OpenGLGuiRender.executeBatch(buffer, program, vao, vbo, mode, stride, capacity);
        }
    }

    private static int executeBatch(DirectByteBuffer buffer, int program, int vao, int vbo, int mode, int stride, int capacity) {
        glUseProgram(program);
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        int writtenBytes = buffer.getWrittenBytes();
        capacity = ensureCapacity(writtenBytes, capacity);
        glBufferSubData(GL_ARRAY_BUFFER, 0, buffer.getNioDirectByteBuffer());
        glDrawArrays(mode, 0, writtenBytes / stride);
        return capacity;
    }
    private static int ensureCapacity(int limit, int capacity) {
        if (limit > capacity) {
            int target = Math.max(capacity * 2, capacity + limit);
            glBufferData(GL_ARRAY_BUFFER, target, GL_STREAM_DRAW);
            return target;
        }
        return capacity;
    }

    private class TriangleBatch extends RenderBatch {
        @Override
        public void execute() {
            triangleVboCapacity = executeBatch(triangleShaderProgram, triangleVao, triangleVbo, GL_TRIANGLES, triangleStride, triangleVboCapacity);
        }

        @Override
        public void destroy() {
            triangleBatchPool.push(this);
        }

        TriangleBatch(DirectByteBuffer buffer) {
            super(buffer);
        }
    }
    private class ShapeBatch extends RenderBatch {
        @Override
        public void execute() {
            shapeVboCapacity = executeBatch(shapeShaderProgram, shapeVao, shapeVbo, GL_TRIANGLE_FAN, shapeStride, shapeVboCapacity);
        }

        @Override
        public void destroy() {
            shapeBatchPool.push(this);
        }

        ShapeBatch(DirectByteBuffer buffer) {
            super(buffer);
        }
    }
    private class TextureBatch extends RenderBatch {
        int textureId;

        @Override
        public void execute() {
            glUseProgram(textureShaderProgram);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureId);
            glUniform1i(textureSamplerLocation, 0);
            textureVboCapacity = executeBatch(textureShaderProgram, textureVao, textureVbo, GL_TRIANGLE_FAN, textureStride, textureVboCapacity);
        }

        @Override
        public void destroy() {
            textureBatchPool.push(this);
        }

        TextureBatch(DirectByteBuffer buffer, int textureId) {
            super(buffer);
            this.textureId = textureId;
        }
    }

    private class StringBatch extends RenderBatch {
        @Override
        public void execute() {
            glUseProgram(stringShaderProgram);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, fontAtlasTextureId);
            glUniform1i(stringSamplerLocation, 0);
            stringVboCapacity = executeBatch(stringShaderProgram, stringVao, stringVbo, GL_TRIANGLES, stringStride, stringVboCapacity);
        }

        @Override
        public void destroy() {
            stringBatchPool.push(this);
        }

        StringBatch(DirectByteBuffer buffer) {
            super(buffer);
        }
    }

    private class StateCommand implements RenderCommand {
        Runnable command;

        @Override
        public void execute() {
            command.run();
        }

        @Override
        public void destroy() {
            stateCommandPool.push(this);
        }

        public StateCommand(Runnable command) {
            this.command = command;
        }
    }

    private interface RenderCommand {
        void execute();

        void destroy();
    }

    private static class SavedGLState {
        int program;
        int frameBufferBinding;
        int vertexArrayBinding;
        int arrayBufferBinding;
        int blendSrcRgb;
        int blendSrcAlpha;
        int blendDstRgb;
        int blendDstAlpha;
        int logicOpMode;
        int blendEquationRgb;
        int blendEquationAlpha;
        int activeTexture;
        int bindTexture;

        boolean blend;
        boolean depthTest;
        boolean cullFace;
        boolean scissorTest;
        boolean stencilTest;
        boolean colorLogicOp;

        int[] viewport = new int[4];
        int[] scissorTestBox = new int[4];
        public boolean[] colorWriteMask = new boolean[4];
    }

    private record GlyphInfo(float u0, float v0, float u1, float v1, int bitmapLeft, int bitmapTop, int bitmapWidth, int bitmapHeight, float advanceX) {
    }
}
