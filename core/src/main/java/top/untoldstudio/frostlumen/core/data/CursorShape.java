package top.untoldstudio.frostlumen.core.data;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import static org.lwjgl.glfw.GLFW.*;

public enum CursorShape {
    ARROW(GLFW_ARROW_CURSOR),
    IBEAM(GLFW_IBEAM_CURSOR),
    CROSSHAIR(GLFW_CROSSHAIR_CURSOR),
    HAND(GLFW_HAND_CURSOR),
    HRESIZE(GLFW_HRESIZE_CURSOR),
    VRESIZE(GLFW_VRESIZE_CURSOR),
    UNKNOWN(-1);

    private static final Int2ObjectMap<CursorShape> MAP = new Int2ObjectOpenHashMap<>();
    private final int glfwValue;

    static {
        for (CursorShape cursorShape : CursorShape.values()) {
            MAP.put(cursorShape.glfwValue, cursorShape);
        }
    }

    CursorShape(int glfwValue){
        this.glfwValue = glfwValue;
    }
    public int getGLFWValue(){
        return glfwValue;
    }
    public static CursorShape fromGLFWValue(int glfwValue){
        return MAP.getOrDefault(glfwValue, UNKNOWN);
    }
}
