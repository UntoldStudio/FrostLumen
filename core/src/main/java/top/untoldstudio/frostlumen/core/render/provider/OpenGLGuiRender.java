package top.untoldstudio.frostlumen.core.render.provider;

import org.joml.Matrix4f;
import top.untoldstudio.frostlumen.core.data.ThicknessPosition;
import top.untoldstudio.frostlumen.core.exception.ResourceError;
import top.untoldstudio.frostlumen.core.gui.Window;
import top.untoldstudio.frostlumen.core.render.GuiRender;
import top.untoldstudio.frostlumen.core.tool.DirectByteBuffer;
import top.untoldstudio.frostlumen.core.tool.ResourceReader;

import java.io.IOException;
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

    private final Deque<SavedGLState> savedGLStateState = new ArrayDeque<>();
    private final Deque<SavedGLState> savedGLStatePool = new ArrayDeque<>();

    public OpenGLGuiRender(long windowHandle) {
        super(windowHandle);

        saveContext();

        String triangleVertSource;
        String triangleFragSource;
        String shapeVertSource;
        String shapeFragSource;
        try {
            triangleVertSource = ResourceReader.readString("/shader/triangle/vert.glsl");
            triangleFragSource = ResourceReader.readString("/shader/triangle/frag.glsl");
            shapeVertSource = ResourceReader.readString("/shader/shape/vert.glsl");
            shapeFragSource = ResourceReader.readString("/shader/shape/frag.glsl");
        } catch (IOException e) {
            throw new ResourceError("Cannot read shader source!");
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

        triangleVao = glGenVertexArrays();
        triangleVbo = glGenBuffers();
        glBindVertexArray(triangleVao);
        glBindBuffer(GL_ARRAY_BUFFER, triangleVbo);
        triangleStride = roundUpTo4(2 * Integer.BYTES + 4 * Byte.BYTES);
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
        shapeStride = roundUpTo4(10 * Integer.BYTES + 4 * Float.BYTES + 9 * Byte.BYTES);
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

        restoreContext();
    }

    public static int roundUpTo4(int n) {
        return (n + 3) / 4 * 4;
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
            throw new ResourceError(glGetShaderInfoLog(vertShader));
        }

        int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, fragSource);
        glCompileShader(fragShader);
        if (glGetShaderi(fragShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new ResourceError(glGetShaderInfoLog(fragShader));
        }

        int program = glCreateProgram();
        glAttachShader(program, vertShader);
        glAttachShader(program, fragShader);
        for (Map.Entry<Integer, String> attributeLocation : attributeLocations.entrySet()) {
            glBindAttribLocation(program, attributeLocation.getKey(), attributeLocation.getValue());
        }

        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw new ResourceError(glGetProgramInfoLog(program));
        }

        glDeleteShader(vertShader);
        glDeleteShader(fragShader);

        return program;
    }

    @Override
    public void drawTriangle(int ax, int ay, int bx, int by, int cx, int cy,
                             int aRed, int aGreen, int aBlue, int aAlpha,
                             int bRed, int bGreen, int bBlue, int bAlpha,
                             int cRed, int cGreen, int cBlue, int cAlpha) {
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

        buffer.writeInts(ax, ay);
        buffer.writeBytes((byte) aRed, (byte) aGreen, (byte) aBlue, (byte) aAlpha);

        buffer.writeInts(bx, by);
        buffer.writeBytes((byte) bRed, (byte) bGreen, (byte) bBlue, (byte) bAlpha);

        buffer.writeInts(cx, cy);
        buffer.writeBytes((byte) cRed, (byte) cGreen, (byte) cBlue, (byte) cAlpha);

        if (shouldPush) {
            commands.push(batch);
        }
    }

    public void drawShape(int minX, int minY, int maxX, int maxY,
                          int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                          int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha,
                          int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii,
                          int aBorderThickness, int bBorderThickness, int cBorderThickness, int dBorderThickness,
                          int borderRed, int borderGreen, int borderBlue, int borderAlpha,
                          ThicknessPosition position) {
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

        buffer.writeInts(exMinX, exMinY);
        buffer.writeFloats(-exHalfWidth, -exHalfHeight, halfWidth, halfHeight);
        buffer.writeBytes((byte) aRed, (byte) aGreen, (byte) aBlue, (byte) aAlpha);
        buffer.writeBytes((byte) borderRed, (byte) borderGreen, (byte) borderBlue, (byte) borderAlpha);
        buffer.writeInts(aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii);
        buffer.writeInts(aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness);
        buffer.writeInt(positionOrder);

        buffer.writeInts(exMaxX, exMinY);
        buffer.writeFloats(exHalfWidth, -exHalfHeight, halfWidth, halfHeight);
        buffer.writeBytes((byte) bRed, (byte) bGreen, (byte) bBlue, (byte) bAlpha);
        buffer.writeBytes((byte) borderRed, (byte) borderGreen, (byte) borderBlue, (byte) borderAlpha);
        buffer.writeInts(aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii);
        buffer.writeInts(aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness);
        buffer.writeInt(positionOrder);

        buffer.writeInts(exMaxX, exMaxY);
        buffer.writeFloats(exHalfWidth, exHalfHeight, halfWidth, halfHeight);
        buffer.writeBytes((byte) dRed, (byte) dGreen, (byte) dBlue, (byte) dAlpha);
        buffer.writeBytes((byte) borderRed, (byte) borderGreen, (byte) borderBlue, (byte) borderAlpha);
        buffer.writeInts(aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii);
        buffer.writeInts(aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness);
        buffer.writeInt(positionOrder);

        buffer.writeInts(exMinX, exMaxY);
        buffer.writeFloats(-exHalfWidth, exHalfHeight, halfWidth, halfHeight);
        buffer.writeBytes((byte) cRed, (byte) cGreen, (byte) cBlue, (byte) cAlpha);
        buffer.writeBytes((byte) borderRed, (byte) borderGreen, (byte) borderBlue, (byte) borderAlpha);
        buffer.writeInts(aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii);
        buffer.writeInts(aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness);
        buffer.writeInt(positionOrder);

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

        int[] int4Array = new int[4];

        glGetIntegerv(GL_COLOR_WRITEMASK, int4Array);
        boolean[] colorWriteMask = savedGLState.colorWriteMask;
        colorWriteMask[0] = int4Array[0] == GL_TRUE;
        colorWriteMask[1] = int4Array[1] == GL_TRUE;
        colorWriteMask[2] = int4Array[2] == GL_TRUE;
        colorWriteMask[3] = int4Array[3] == GL_TRUE;
        glGetIntegerv(GL_VIEWPORT, savedGLState.viewport);

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
        glViewport(savedGLState.viewport[0], savedGLState.viewport[1], savedGLState.viewport[2], savedGLState.viewport[3]);
        glBlendFuncSeparate(savedGLState.blendSrcRgb, savedGLState.blendDstRgb, savedGLState.blendSrcAlpha, savedGLState.blendDstAlpha);
        glLogicOp(savedGLState.logicOpMode);
        glBlendEquationSeparate(savedGLState.blendEquationRgb, savedGLState.blendEquationAlpha);

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
        glScissor(x, y, width, height);
        glEnable(GL_SCISSOR_TEST);
    }
    @Override
    public void disableScissor() {
        glDisable(GL_SCISSOR_TEST);
    }

    private final Deque<TriangleBatch> triangleBatchPool = new ArrayDeque<>();
    private final Deque<ShapeBatch> shapeBatchPool = new ArrayDeque<>();

    private <T extends RenderBatch> T allocBatch(Deque<T> pool, Supplier<T> supplier) {
        if (pool.isEmpty()) return supplier.get();
        T result = pool.pop();
        result.buffer.clear();
        return result;
    }
    private TriangleBatch allocTriangleBatch() {
        return allocBatch(triangleBatchPool, () -> new TriangleBatch(new DirectByteBuffer(36)));
    }
    private ShapeBatch allocShapeBatch() {
        return allocBatch(shapeBatchPool, () -> new ShapeBatch(new DirectByteBuffer(408)));
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
    }

    private class TriangleBatch extends RenderBatch {
        @Override
        public void execute() {
            glUseProgram(triangleShaderProgram);
            glBindVertexArray(triangleVao);
            glBindBuffer(GL_ARRAY_BUFFER, triangleVbo);
            int writtenBytes = buffer.getWrittenBytes();
            triangleVboCapacity = ensureCapacity(writtenBytes, triangleVboCapacity);
            glBufferSubData(GL_ARRAY_BUFFER, 0, buffer.getNioDirectByteBuffer());
            glDrawArrays(GL_TRIANGLES, 0, writtenBytes / triangleStride);
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
            glUseProgram(shapeShaderProgram);
            glBindVertexArray(shapeVao);
            glBindBuffer(GL_ARRAY_BUFFER, shapeVbo);
            int writtenBytes = buffer.getWrittenBytes();
            shapeVboCapacity = ensureCapacity(writtenBytes, shapeVboCapacity);
            glBufferSubData(GL_ARRAY_BUFFER, 0, buffer.getNioDirectByteBuffer());
            glDrawArrays(GL_TRIANGLE_FAN, 0, writtenBytes / shapeStride);
        }

        @Override
        public void destroy() {
            shapeBatchPool.push(this);
        }

        ShapeBatch(DirectByteBuffer buffer) {
            super(buffer);
        }
    }

    private int ensureCapacity(int limit, int capacity) {
        if (limit > capacity) {
            int target = Math.max(capacity * 2, capacity + limit);
            glBufferData(GL_ARRAY_BUFFER, target, GL_STREAM_DRAW);
            return target;
        }
        return capacity;
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

        boolean blend;
        boolean depthTest;
        boolean cullFace;
        boolean scissorTest;
        boolean stencilTest;
        boolean colorLogicOp;

        int[] viewport = new int[4];
        public boolean[] colorWriteMask = new boolean[4];
    }
}
