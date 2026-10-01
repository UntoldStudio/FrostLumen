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
package top.untoldstudio.frostlumen.core.data;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

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

    private static final Int2ObjectMap<MouseButton> MAP = new Int2ObjectOpenHashMap<>();

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
