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
package top.untold.frostlumen.core.data;

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
