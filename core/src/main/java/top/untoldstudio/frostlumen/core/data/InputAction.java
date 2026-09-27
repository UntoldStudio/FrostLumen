package top.untoldstudio.frostlumen.core.data;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public enum InputAction {
    PRESS(GLFW_PRESS),
    RELEASE(GLFW_RELEASE),
    REPEAT(GLFW_REPEAT),
    UNKNOWN(-1);

    private static final Map<Integer, InputAction> ACTION_MAP = new HashMap<>();
    private final int glfwValue;

    static {
        for (InputAction action : InputAction.values()) {
            ACTION_MAP.put(action.glfwValue, action);
        }
    }

    InputAction(int glfwValue){
        this.glfwValue = glfwValue;
    }
    public int getGLFWValue(){
        return glfwValue;
    }
    public static InputAction fromGLFWValue(int glfwValue){
        return ACTION_MAP.getOrDefault(glfwValue, UNKNOWN);
    }
}
