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

import top.untold.frostlumen.core.data.ScaleOffset;
import top.untold.frostlumen.core.event.KeyEvent;
import top.untold.frostlumen.core.event.MouseButtonEvent;
import top.untold.frostlumen.core.event.MouseMoveEvent;
import top.untold.frostlumen.core.event.MouseScrollEvent;
import top.untold.frostlumen.core.gui.GuiNode;
import top.untold.frostlumen.core.listener.OneArgListenerRegistry;

import java.util.function.Consumer;

public abstract class AbstractInputBlocker<T extends GuiNode<T>> extends GuiNode<T> implements InputBlocker<T> {
    private final OneArgListenerRegistry<MouseButtonEvent> mouseButtonEventListenerRegistry = new OneArgListenerRegistry<>();
    private final OneArgListenerRegistry<MouseMoveEvent> mouseMoveEventListenerRegistry = new OneArgListenerRegistry<>();
    private final OneArgListenerRegistry<MouseScrollEvent> mouseScrollEventListenerRegistry = new OneArgListenerRegistry<>();
    private final OneArgListenerRegistry<KeyEvent> keyEventListenerRegistry = new OneArgListenerRegistry<>();
    private boolean blockInput;

    public T registerMouseButtonEventListener(Consumer<MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.register(callback);
        return self;
    }
    public T registerMouseScrollEventListener(Consumer<MouseScrollEvent> callback) {
        mouseScrollEventListenerRegistry.register(callback);
        return self;
    }
    public T registerKeyEventListener(Consumer<KeyEvent> callback) {
        keyEventListenerRegistry.register(callback);
        return self;
    }
    public T registerMouseMoveEventListener(Consumer<MouseMoveEvent> callback) {
        mouseMoveEventListenerRegistry.register(callback);
        return self;
    }

    public T unregisterMouseButtonEventListener(Consumer<MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.unregister(callback);
        return self;
    }
    public T unregisterMouseScrollEventListener(Consumer<MouseScrollEvent> callback) {
        mouseScrollEventListenerRegistry.unregister(callback);
        return self;
    }
    public T unregisterMouseMoveEventListener(Consumer<MouseMoveEvent> callback) {
        mouseMoveEventListenerRegistry.unregister(callback);
        return self;
    }
    public T unregisterKeyEventListener(Consumer<KeyEvent> callback) {
        keyEventListenerRegistry.unregister(callback);
        return self;
    }

    @Override
    protected void onMouseButtonEvent(MouseButtonEvent event) {
        mouseButtonEventListenerRegistry.trigger(event);
    }
    @Override
    protected void onMouseMoveEvent(MouseMoveEvent event) {
        mouseMoveEventListenerRegistry.trigger(event);
    }

    public T setBlockInput(boolean blockInput) {
        this.blockInput = blockInput;
        return self;
    }

    public boolean isBlockInput() {
        return this.blockInput;
    }

    public AbstractInputBlocker(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
