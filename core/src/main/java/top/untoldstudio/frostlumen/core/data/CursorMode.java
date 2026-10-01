package top.untoldstudio.frostlumen.core.data;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import static org.lwjgl.glfw.GLFW.*;

public enum CursorMode {
    NORMAL(GLFW_CURSOR_NORMAL),
    HIDDEN(GLFW_CURSOR_HIDDEN),
    DISABLED(GLFW_CURSOR_DISABLED),
    UNKNOWN(-1);

    private static final Int2ObjectMap<CursorMode> MAP = new Int2ObjectOpenHashMap<>();
    private final int glfwValue;

    static {
        for (CursorMode cursorMode : CursorMode.values()) {
            MAP.put(cursorMode.glfwValue, cursorMode);
        }
    }

    CursorMode(int glfwValue){
        this.glfwValue = glfwValue;
    }
    public int getGLFWValue(){
        return glfwValue;
    }
    public static CursorMode fromGLFWValue(int glfwValue){
        return MAP.getOrDefault(glfwValue, UNKNOWN);
    }
}
