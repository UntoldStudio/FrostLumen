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

import org.joml.Matrix4f;
import top.untoldstudio.frostlumen.core.data.ThicknessPosition;
import top.untoldstudio.frostlumen.core.exception.ResourceException;
import top.untoldstudio.frostlumen.core.gui.Window;
import top.untoldstudio.frostlumen.core.render.GuiRender;
import top.untoldstudio.frostlumen.core.render.IResourceManager;
import top.untoldstudio.frostlumen.core.tool.DirectByteBuffer;
import top.untoldstudio.frostlumen.core.tool.ResourceReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL32C.*;

public class OpenGLGuiRender extends GuiRender {
    private final Deque<RenderCommand> commands = new ArrayDeque<>();

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
        try {
            triangleVertSource = ResourceReader.readString("/shader/triangle/vert.glsl");
            triangleFragSource = ResourceReader.readString("/shader/triangle/frag.glsl");
            shapeVertSource = ResourceReader.readString("/shader/shape/vert.glsl");
            shapeFragSource = ResourceReader.readString("/shader/shape/frag.glsl");
            textureVertSource = ResourceReader.readString("/shader/texture/vert.glsl");
            textureFragSource = ResourceReader.readString("/shader/texture/frag.glsl");
        } catch (IOException e) {
            throw new ResourceException("Cannot read shader source!");
        }
        triangleShaderProgram = createProgram(triangleVertSource, triangleFragSource, Map.of(
                0, "aPos",
                1, "aColor"
        ));
        shapeShaderProgram = createProgram(shapeVertSource, shapeFragSource, Map.of(
                0, "aScreenPos",
                1, "aLocalPos",
                2, "aHalfSize",
                3, "aFillColor",
                4, "aBorderColor",
                5, "aCornerRadii",
                6, "aEdgeThickness",
                7, "aBorderPosition"
        ));
        textureShaderProgram = createProgram(textureVertSource, textureFragSource, Map.of(
                0, "aScreenPos",
                1, "aTexCoord",
                2, "aColor"
        ));

        triangleVao = glGenVertexArrays();
        triangleVbo = glGenBuffers();
        glBindVertexArray(triangleVao);
        glBindBuffer(GL_ARRAY_BUFFER, triangleVbo);
        triangleStride = 2 * Integer.BYTES + 4 * Byte.BYTES;
        int triangleOffset = 0;
        triangleVboCapacity = triangleStride * 1024;
        glBufferData(GL_ARRAY_BUFFER, triangleVboCapacity, GL_STREAM_DRAW);
        glVertexAttribIPointer(0, 2, GL_INT, triangleStride, triangleOffset);
        triangleOffset += 2 * Integer.BYTES;
        glVertexAttribPointer(1, 4, GL_UNSIGNED_BYTE, true, triangleStride, triangleOffset);
        enableVertexAttributes(1);
        triangleProjectLocation = glGetUniformLocation(triangleShaderProgram, "uProjection");

        shapeVao = glGenVertexArrays();
        shapeVbo = glGenBuffers();
        glBindVertexArray(shapeVao);
        glBindBuffer(GL_ARRAY_BUFFER, shapeVbo);
        shapeStride = 10 * Integer.BYTES + 4 * Float.BYTES + 9 * Byte.BYTES + 3 * Byte.BYTES;
        int shapeOffset = 0;
        shapeVboCapacity = shapeStride * 512;
        glBufferData(GL_ARRAY_BUFFER, shapeVboCapacity, GL_STREAM_DRAW);
        glVertexAttribIPointer(0, 2, GL_INT, shapeStride, shapeOffset);
        shapeOffset += 2 * Integer.BYTES;
        glVertexAttribPointer(1, 2, GL_FLOAT, false, shapeStride, shapeOffset);
        shapeOffset += 2 * Float.BYTES;
        glVertexAttribPointer(2, 2, GL_FLOAT, false, shapeStride, shapeOffset);
        shapeOffset += 2 * Float.BYTES;
        glVertexAttribPointer(3, 4, GL_UNSIGNED_BYTE, true, shapeStride, shapeOffset);
        shapeOffset += 4 * Byte.BYTES;
        glVertexAttribPointer(4, 4, GL_UNSIGNED_BYTE, true, shapeStride, shapeOffset);
        shapeOffset += 4 * Byte.BYTES;
        glVertexAttribIPointer(5, 4, GL_INT, shapeStride, shapeOffset);
        shapeOffset += 4 * Integer.BYTES;
        glVertexAttribIPointer(6, 4, GL_INT, shapeStride, shapeOffset);
        shapeOffset += 4 * Integer.BYTES;
        glVertexAttribIPointer(7, 1, GL_INT, shapeStride, shapeOffset);
        enableVertexAttributes(7);
        shapeProjectLocation = glGetUniformLocation(shapeShaderProgram, "uProjection");

        textureVao = glGenVertexArrays();
        textureVbo = glGenBuffers();
        glBindVertexArray(textureVao);
        glBindBuffer(GL_ARRAY_BUFFER, textureVbo);
        textureStride = 2 * Integer.BYTES + 2 * Float.BYTES + 4 * Byte.BYTES;
        int textureOffset = 0;
        textureVboCapacity = textureStride * 1024;
        glBufferData(GL_ARRAY_BUFFER, textureVboCapacity, GL_STREAM_DRAW);
        glVertexAttribIPointer(0, 2, GL_INT, textureStride, textureOffset);
        textureOffset += 2 * Integer.BYTES;
        glVertexAttribPointer(1, 2, GL_FLOAT, false, textureStride, textureOffset);
        textureOffset += 2 * Float.BYTES;
        glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, textureStride, textureOffset);
        enableVertexAttributes(2);
        textureProjectLocation = glGetUniformLocation(textureShaderProgram, "uProjection");
        textureSamplerLocation = glGetUniformLocation(textureShaderProgram, "uTexture");

        restoreContext();
    }

    private void enableVertexAttributes(int target) {
        for (int i = 0; i <= target; i++) {
            glEnableVertexAttribArray(i);
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

        glColorMask(true, true, true, true);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glDisable(GL_SCISSOR_TEST);
        glDisable(GL_STENCIL_TEST);
        glDisable(GL_COLOR_LOGIC_OP);
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
    public void drawTriangle(int ax, int ay, int bx, int by, int cx, int cy,
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
        buffer.writeBytesFromIntsWithForcedConversion(aRed, aGreen, aBlue, aAlpha);

        buffer.writeInt(bx, by);
        buffer.writeBytesFromIntsWithForcedConversion(bRed, bGreen, bBlue, bAlpha);

        buffer.writeInt(cx, cy);
        buffer.writeBytesFromIntsWithForcedConversion(cRed, cGreen, cBlue, cAlpha);

        if (shouldPush) {
            commands.push(batch);
        }
    }

    @Override
    public void drawShape(int minX, int minY, int maxX, int maxY,
                          int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                          int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha,
                          int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii,
                          int aBorderThickness, int bBorderThickness, int cBorderThickness, int dBorderThickness,
                          int borderRed, int borderGreen, int borderBlue, int borderAlpha,
                          ThicknessPosition position
    ) {
        boolean shouldPush = true;
        DirectByteBuffer buffer;
        ShapeBatch batch;
        if (commands.peek() instanceof ShapeBatch currentBatch) {
            buffer = currentBatch.buffer;
            batch = currentBatch;
            shouldPush = false;
        } else {
            batch = allocShapeBatch();
            buffer = batch.buffer;
        }

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

        buffer.writeInt(exMinX, exMinY);
        buffer.writeFloat(-exHalfWidth, -exHalfHeight, halfWidth, halfHeight);
        buffer.writeBytesFromIntsWithForcedConversion(aRed, aGreen, aBlue, aAlpha);
        buffer.writeBytesFromIntsWithForcedConversion(borderRed, borderGreen, borderBlue, borderAlpha);
        buffer.writeInt(aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii);
        buffer.writeInt(aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness);
        buffer.writeInt(positionOrder);

        buffer.writeInt(exMaxX, exMinY);
        buffer.writeFloat(exHalfWidth, -exHalfHeight, halfWidth, halfHeight);
        buffer.writeBytesFromIntsWithForcedConversion(bRed, bGreen, bBlue, bAlpha);
        buffer.writeBytesFromIntsWithForcedConversion(borderRed, borderGreen, borderBlue, borderAlpha);
        buffer.writeInt(aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii);
        buffer.writeInt(aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness);
        buffer.writeInt(positionOrder);

        buffer.writeInt(exMaxX, exMaxY);
        buffer.writeFloat(exHalfWidth, exHalfHeight, halfWidth, halfHeight);
        buffer.writeBytesFromIntsWithForcedConversion(dRed, dGreen, dBlue, dAlpha);
        buffer.writeBytesFromIntsWithForcedConversion(borderRed, borderGreen, borderBlue, borderAlpha);
        buffer.writeInt(aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii);
        buffer.writeInt(aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness);
        buffer.writeInt(positionOrder);

        buffer.writeInt(exMinX, exMaxY);
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
    public void drawTexture(int textureId, int minX, int minY, int maxX, int maxY, float u0, float v0, float u1, float v1,
                            int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                            int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha
    ) {
        boolean shouldPush = true;
        DirectByteBuffer buffer;
        TextureBatch batch;
        if (commands.peek() instanceof TextureBatch currentBatch && currentBatch.textureId == textureId) {
            buffer = currentBatch.buffer;
            batch = currentBatch;
            shouldPush = false;
        } else {
            batch = allocTextureBatch(textureId);
            buffer = batch.buffer;
        }

        buffer.writeInt(minX, minY);
        buffer.writeFloat(u0, v0);
        buffer.writeBytesFromIntsWithForcedConversion(aRed, aGreen, aBlue, aAlpha);

        buffer.writeInt(maxX, minY);
        buffer.writeFloat(u1, v0);
        buffer.writeBytesFromIntsWithForcedConversion(bRed, bGreen, bBlue, bAlpha);

        buffer.writeInt(maxX, maxY);
        buffer.writeFloat(u1, v1);
        buffer.writeBytesFromIntsWithForcedConversion(dRed, dGreen, dBlue, dAlpha);

        buffer.writeInt(minX, maxY);
        buffer.writeFloat(u0, v1);
        buffer.writeBytesFromIntsWithForcedConversion(cRed, cGreen, cBlue, cAlpha);

        if (shouldPush) {
            commands.push(batch);
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

    private final Deque<TriangleBatch> triangleBatchPool = new ArrayDeque<>();
    private final Deque<ShapeBatch> shapeBatchPool = new ArrayDeque<>();
    private final Deque<TextureBatch> textureBatchPool = new ArrayDeque<>();
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

        protected static int ensureCapacity(int limit, int capacity) {
            if (limit > capacity) {
                int target = Math.max(capacity * 2, capacity + limit);
                glBufferData(GL_ARRAY_BUFFER, target, GL_STREAM_DRAW);
                return target;
            }
            return capacity;
        }

        protected int executeBatch(int program, int vao, int vbo, int mode, int stride, int capacity) {
            glUseProgram(program);
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            int writtenBytes = buffer.getWrittenBytes();
            capacity = ensureCapacity(writtenBytes, capacity);
            glBufferSubData(GL_ARRAY_BUFFER, 0, buffer.getNioDirectByteBuffer());
            glDrawArrays(mode, 0, writtenBytes / stride);
            return capacity;
        }
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
}
