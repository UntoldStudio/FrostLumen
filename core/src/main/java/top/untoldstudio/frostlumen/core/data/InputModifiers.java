package top.untoldstudio.frostlumen.core.data;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_CAPS_LOCK;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_NUM_LOCK;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_SUPER;

public record InputModifiers(boolean isShiftPressed, boolean isControlPressed, boolean isAltPressed, boolean isSuperPressed, boolean isCapsLockEnabled, boolean isNumberLockEnabled) {
    public static InputModifiers fromGLFWValue(int modifiers){
        return new InputModifiers(
                (modifiers & GLFW_MOD_SHIFT) != 0, (modifiers & GLFW_MOD_CONTROL) != 0,
                (modifiers & GLFW_MOD_ALT) != 0, (modifiers & GLFW_MOD_SUPER) != 0,
                (modifiers & GLFW_MOD_CAPS_LOCK) != 0, (modifiers & GLFW_MOD_NUM_LOCK) != 0
        );
    }
}
