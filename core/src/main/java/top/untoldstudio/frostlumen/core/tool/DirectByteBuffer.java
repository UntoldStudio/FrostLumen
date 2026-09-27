package top.untoldstudio.frostlumen.core.tool;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DirectByteBuffer {
    private ByteBuffer buffer;

    public void writeFloat(float value) {
        tryExpansion(4);
        buffer.putFloat(value);
    }
    public void writeFloats(float v1, float v2, float v3, float v4) {
        writeFloat(v1);
        writeFloat(v2);
        writeFloat(v3);
        writeFloat(v4);
    }
    public void writeInt(int value) {
        tryExpansion(4);
        buffer.putInt(value);
    }
    public void writeInts(int v1, int v2) {
        writeInt(v1);
        writeInt(v2);
    }
    public void writeInts(int v1, int v2, int v3, int v4) {
        writeInt(v1);
        writeInt(v2);
        writeInt(v3);
        writeInt(v4);
    }
    public void writeByte(byte value) {
        tryExpansion(1);
        buffer.put(value);
    }
    public void writeBytes(byte v1, byte v2, byte v3, byte v4) {
        writeByte(v1);
        writeByte(v2);
        writeByte(v3);
        writeByte(v4);
    }

    public ByteBuffer getNioDirectByteBuffer() {
        buffer.flip();
        return buffer;
    }

    public void clear() {
        buffer.clear();
    }

    public void fresh(int capacity) {
        buffer = ByteBuffer.allocateDirect(capacity);
        buffer.order(ByteOrder.nativeOrder());
    }

    private void tryExpansion(int needByteLength) {
        int target = buffer.position() + needByteLength;
        if (buffer.capacity() < target) {
            ByteBuffer newBuffer = ByteBuffer.allocateDirect(target * 2);
            newBuffer.order(ByteOrder.nativeOrder());
            buffer.flip();
            newBuffer.put(buffer);
            buffer = newBuffer;
        }
    }

    public int getWrittenBytes() {
        return buffer.position();
    }

    public DirectByteBuffer(int capacity) {
        buffer = ByteBuffer.allocateDirect(capacity);
        buffer.order(ByteOrder.nativeOrder());
    }
}
