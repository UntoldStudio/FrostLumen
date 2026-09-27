package top.untoldstudio.frostlumen.core.data;

import java.nio.ByteBuffer;

public record RGBA(int red, int green, int blue, int alpha) {
    public float getRedFloat(){
        return red / 255f;
    }
    public float getGreenFloat(){
        return green / 255f;
    }
    public float getBlueFloat(){
        return blue / 255f;
    }
    public float getAlphaFloat(){
        return alpha / 255f;
    }

    public RGBA withRed(int red){
        return new RGBA(red, green, blue, alpha);
    }
    public RGBA withGreen(int green){
        return new RGBA(red, green, blue, alpha);
    }
    public RGBA withBlue(int blue){
        return new RGBA(red, green, blue, alpha);
    }
    public RGBA withAlpha(int alpha){
        return new RGBA(red, green, blue, alpha);
    }
    public int toARGBInt(){
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
    public ByteBuffer writeRGBAByteBuffer(ByteBuffer buffer){
        buffer.put((byte)red);
        buffer.put((byte)green);
        buffer.put((byte)blue);
        buffer.put((byte)alpha);
        return buffer;
    }
    public static final RGBA WHITE = new RGBA(255, 255, 255, 255);
    public static final RGBA BLACK = new RGBA(0, 0, 0, 255);
    public static final RGBA RED = new RGBA(255, 0, 0, 255);
    public static final RGBA GREEN = new RGBA(0, 255, 0, 255);
    public static final RGBA BLUE = new RGBA(0, 0, 255, 255);
    public static final RGBA GRAY = new RGBA(128, 128, 128, 255);
}
