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
