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
import top.untold.frostlumen.core.listener.TwoArgListenerRegistry;

import java.util.function.BiConsumer;

public abstract class AbstractInputBlocker<T extends GuiNode<T>> extends GuiNode<T> implements InputBlocker<T> {
    private final TwoArgListenerRegistry<T, MouseButtonEvent> mouseButtonEventListenerRegistry = new TwoArgListenerRegistry<>();
    private final TwoArgListenerRegistry<T, MouseMoveEvent> mouseMoveEventListenerRegistry = new TwoArgListenerRegistry<>();
    private final TwoArgListenerRegistry<T, MouseScrollEvent> mouseScrollEventListenerRegistry = new TwoArgListenerRegistry<>();
    private final TwoArgListenerRegistry<T, KeyEvent> keyEventListenerRegistry = new TwoArgListenerRegistry<>();
    private boolean blockInput;

    public T registerMouseButtonEventListener(BiConsumer<T, MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.register(callback);
        return self;
    }
    public T registerMouseScrollEventListener(BiConsumer<T, MouseScrollEvent> callback) {
        mouseScrollEventListenerRegistry.register(callback);
        return self;
    }
    public T registerKeyEventListener(BiConsumer<T, KeyEvent> callback) {
        keyEventListenerRegistry.register(callback);
        return self;
    }
    public T registerMouseMoveEventListener(BiConsumer<T, MouseMoveEvent> callback) {
        mouseMoveEventListenerRegistry.register(callback);
        return self;
    }

    public T unregisterMouseButtonEventListener(BiConsumer<T, MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.unregister(callback);
        return self;
    }
    public T unregisterMouseScrollEventListener(BiConsumer<T, MouseScrollEvent> callback) {
        mouseScrollEventListenerRegistry.unregister(callback);
        return self;
    }
    public T unregisterMouseMoveEventListener(BiConsumer<T, MouseMoveEvent> callback) {
        mouseMoveEventListenerRegistry.unregister(callback);
        return self;
    }
    public T unregisterKeyEventListener(BiConsumer<T, KeyEvent> callback) {
        keyEventListenerRegistry.unregister(callback);
        return self;
    }

    @Override
    protected void onMouseButtonEvent(MouseButtonEvent event) {
        mouseButtonEventListenerRegistry.trigger(self, event);
    }
    @Override
    protected void onMouseMoveEvent(MouseMoveEvent event) {
        mouseMoveEventListenerRegistry.trigger(self, event);
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
