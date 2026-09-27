package top.untoldstudio.frostlumen.core.data;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public enum MouseButton {
    LEFT(GLFW_MOUSE_BUTTON_1),
    RIGHT(GLFW_MOUSE_BUTTON_2),
    MIDDLE(GLFW_MOUSE_BUTTON_3),
    BUTTON_4(GLFW_MOUSE_BUTTON_4),
    BUTTON_5(GLFW_MOUSE_BUTTON_5),
    BUTTON_6(GLFW_MOUSE_BUTTON_6),
    BUTTON_7(GLFW_MOUSE_BUTTON_7),
    BUTTON_8(GLFW_MOUSE_BUTTON_8),
    UNKNOWN(-1);

    private static final Map<Integer, MouseButton> MAP = new HashMap<Integer, MouseButton>();

    static {
        for (MouseButton button : MouseButton.values()) {
            MAP.put(button.glfwValue, button);
        }
    }

    private final int glfwValue;

    MouseButton(int glfwValue){
        this.glfwValue = glfwValue;
    }
    public int getGLFWValue(){
        return glfwValue;
    }
    public static MouseButton fromGLFWValue(int glfwValue){
        return MAP.getOrDefault(glfwValue, UNKNOWN);
    }
}
