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
package top.untold.frostlumen.core.gui.node;

import top.untold.frostlumen.core.event.KeyEvent;
import top.untold.frostlumen.core.event.MouseButtonEvent;
import top.untold.frostlumen.core.event.MouseMoveEvent;
import top.untold.frostlumen.core.event.MouseScrollEvent;
import top.untold.frostlumen.core.gui.GuiNode;

import java.util.function.Consumer;

public interface InputBlocker<T extends GuiNode<T>> {
    T setBlockInput(boolean blockInput);
    boolean isBlockInput();

    T registerMouseButtonEventListener(Consumer<MouseButtonEvent> callback);
    T registerMouseMoveEventListener(Consumer<MouseMoveEvent> callback);
    T registerMouseScrollEventListener(Consumer<MouseScrollEvent> callback);
    T registerKeyEventListener(Consumer<KeyEvent> callback);

    T unregisterMouseButtonEventListener(Consumer<MouseButtonEvent> callback);
    T unregisterMouseMoveEventListener(Consumer<MouseMoveEvent> callback);
    T unregisterMouseScrollEventListener(Consumer<MouseScrollEvent> callback);
    T unregisterKeyEventListener(Consumer<KeyEvent> callback);
}
