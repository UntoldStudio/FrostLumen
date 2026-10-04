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

public enum Key {
    A(GLFW_KEY_A),
    B(GLFW_KEY_B),
    C(GLFW_KEY_C),
    D(GLFW_KEY_D),
    E(GLFW_KEY_E),
    F(GLFW_KEY_F),
    G(GLFW_KEY_G),
    H(GLFW_KEY_H),
    I(GLFW_KEY_I),
    J(GLFW_KEY_J),
    K(GLFW_KEY_K),
    L(GLFW_KEY_L),
    M(GLFW_KEY_M),
    N(GLFW_KEY_N),
    O(GLFW_KEY_O),
    P(GLFW_KEY_P),
    Q(GLFW_KEY_Q),
    R(GLFW_KEY_R),
    S(GLFW_KEY_S),
    T(GLFW_KEY_T),
    U(GLFW_KEY_U),
    V(GLFW_KEY_V),
    W(GLFW_KEY_W),
    X(GLFW_KEY_X),
    Y(GLFW_KEY_Y),
    Z(GLFW_KEY_Z),
    MAIN_0(GLFW_KEY_0),
    MAIN_1(GLFW_KEY_1),
    MAIN_2(GLFW_KEY_2),
    MAIN_3(GLFW_KEY_3),
    MAIN_4(GLFW_KEY_4),
    MAIN_5(GLFW_KEY_5),
    MAIN_6(GLFW_KEY_6),
    MAIN_7(GLFW_KEY_7),
    MAIN_8(GLFW_KEY_8),
    MAIN_9(GLFW_KEY_9),
    KP_0(GLFW_KEY_KP_0),
    KP_1(GLFW_KEY_KP_1),
    KP_2(GLFW_KEY_KP_2),
    KP_3(GLFW_KEY_KP_3),
    KP_4(GLFW_KEY_KP_4),
    KP_5(GLFW_KEY_KP_5),
    KP_6(GLFW_KEY_KP_6),
    KP_7(GLFW_KEY_KP_7),
    KP_8(GLFW_KEY_KP_8),
    KP_9(GLFW_KEY_KP_9),
    KP_DECIMAL(GLFW_KEY_KP_DECIMAL),
    KP_DIVIDE(GLFW_KEY_KP_DIVIDE),
    KP_MULTIPLY(GLFW_KEY_KP_MULTIPLY),
    KP_SUBTRACT(GLFW_KEY_KP_SUBTRACT),
    KP_ADD(GLFW_KEY_KP_ADD),
    KP_ENTER(GLFW_KEY_KP_ENTER),
    KP_EQUAL(GLFW_KEY_KP_EQUAL),
    F1(GLFW_KEY_F1),
    F2(GLFW_KEY_F2),
    F3(GLFW_KEY_F3),
    F4(GLFW_KEY_F4),
    F5(GLFW_KEY_F5),
    F6(GLFW_KEY_F6),
    F7(GLFW_KEY_F7),
    F8(GLFW_KEY_F8),
    F9(GLFW_KEY_F9),
    F10(GLFW_KEY_F10),
    F11(GLFW_KEY_F11),
    F12(GLFW_KEY_F12),
    F13(GLFW_KEY_F13),
    F14(GLFW_KEY_F14),
    F15(GLFW_KEY_F15),
    F16(GLFW_KEY_F16),
    F17(GLFW_KEY_F17),
    F18(GLFW_KEY_F18),
    F19(GLFW_KEY_F19),
    F20(GLFW_KEY_F20),
    F21(GLFW_KEY_F21),
    F22(GLFW_KEY_F22),
    F23(GLFW_KEY_F23),
    F24(GLFW_KEY_F24),
    F25(GLFW_KEY_F25),
    APOSTROPHE(GLFW_KEY_APOSTROPHE),
    COMMA(GLFW_KEY_COMMA),
    MINUS(GLFW_KEY_MINUS),
    PERIOD(GLFW_KEY_PERIOD),
    SLASH(GLFW_KEY_SLASH),
    SEMICOLON(GLFW_KEY_SEMICOLON),
    EQUAL(GLFW_KEY_EQUAL),
    LEFT_BRACKET(GLFW_KEY_LEFT_BRACKET),
    RIGHT_BRACKET(GLFW_KEY_RIGHT_BRACKET),
    BACKSLASH(GLFW_KEY_BACKSLASH),
    GRAVE_ACCENT(GLFW_KEY_GRAVE_ACCENT),
    ESCAPE(GLFW_KEY_ESCAPE),
    ENTER(GLFW_KEY_ENTER),
    TAB(GLFW_KEY_TAB),
    BACKSPACE(GLFW_KEY_BACKSPACE),
    INSERT(GLFW_KEY_INSERT),
    DELETE(GLFW_KEY_DELETE),
    HOME(GLFW_KEY_HOME),
    END(GLFW_KEY_END),
    PAGE_UP(GLFW_KEY_PAGE_UP),
    PAGE_DOWN(GLFW_KEY_PAGE_DOWN),
    CAPS_LOCK(GLFW_KEY_CAPS_LOCK),
    SCROLL_LOCK(GLFW_KEY_SCROLL_LOCK),
    NUM_LOCK(GLFW_KEY_NUM_LOCK),
    PRINT_SCREEN(GLFW_KEY_PRINT_SCREEN),
    PAUSE(GLFW_KEY_PAUSE),
    UP(GLFW_KEY_UP),
    DOWN(GLFW_KEY_DOWN),
    LEFT(GLFW_KEY_LEFT),
    RIGHT(GLFW_KEY_RIGHT),
    LEFT_SHIFT(GLFW_KEY_LEFT_SHIFT),
    RIGHT_SHIFT(GLFW_KEY_RIGHT_SHIFT),
    LEFT_CONTROL(GLFW_KEY_LEFT_CONTROL),
    RIGHT_CONTROL(GLFW_KEY_RIGHT_CONTROL),
    LEFT_ALT(GLFW_KEY_LEFT_ALT),
    RIGHT_ALT(GLFW_KEY_RIGHT_ALT),
    LEFT_SUPER(GLFW_KEY_LEFT_SUPER),
    RIGHT_SUPER(GLFW_KEY_RIGHT_SUPER),
    SPACE(GLFW_KEY_SPACE),
    MENU(GLFW_KEY_MENU),
    WORLD_1(GLFW_KEY_WORLD_1),
    WORLD_2(GLFW_KEY_WORLD_2),
    UNKNOWN(GLFW_KEY_UNKNOWN);

    private static final Int2ObjectMap<Key> MAP = new Int2ObjectOpenHashMap<>();
    private final int glfwValue;

    static {
        for (Key key : Key.values()) {
            MAP.put(key.glfwValue, key);
        }
    }
    
    Key(int glfwValue){
        this.glfwValue = glfwValue;
    }

    public int getGLFWValue(){
        return glfwValue;
    }
    public static Key fromGLFWValue(int glfwValue){
        return MAP.getOrDefault(glfwValue, UNKNOWN);
    }
}
