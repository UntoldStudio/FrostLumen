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
