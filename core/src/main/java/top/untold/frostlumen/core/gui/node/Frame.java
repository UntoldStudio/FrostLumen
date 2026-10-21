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
import top.untold.frostlumen.core.event.MouseButtonEvent;
import top.untold.frostlumen.core.gui.GuiNode;
import top.untold.frostlumen.core.listener.OneArgListenerRegistry;
import top.untold.frostlumen.core.render.GuiRender;

import java.util.function.Consumer;

public class Frame extends GuiNode<Frame> implements InputBlocker<Frame> {
    private final OneArgListenerRegistry<MouseButtonEvent> mouseButtonEventListenerRegistry = new OneArgListenerRegistry<>();
    private boolean blockInput = false;

    public Frame registerMouseButtonEventListener(Consumer<MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.register(callback);
        return this;
    }

    public Frame unregisterMouseButtonEventListener(Consumer<MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.unregister(callback);
        return this;
    }

    @Override
    protected void onMouseButtonEvent(MouseButtonEvent event) {
        mouseButtonEventListenerRegistry.trigger(event);
    }

    public Frame setBlockInput(boolean blockInput) {
        this.blockInput = blockInput;
        return this;
    }

    public boolean isBlockInput() {
        return this.blockInput;
    }

    @Override
    protected void render(GuiRender render, double delta) {
        background.render(render);
    }

    public Frame(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
