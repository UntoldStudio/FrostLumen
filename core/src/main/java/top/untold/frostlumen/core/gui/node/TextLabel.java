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
import top.untold.frostlumen.core.font.Font;
import top.untold.frostlumen.core.listener.TwoArgListenerRegistry;
import top.untold.frostlumen.core.render.GuiRender;

import java.util.function.BiConsumer;

public class TextLabel extends TextNode<TextLabel> implements HasDrawBackground<TextLabel>, InputBlocker<TextLabel> {
    private final CanWrappedTextRenderDescription renderDescription = new CanWrappedTextRenderDescription();
    private boolean drawBackground = true;
    private final TwoArgListenerRegistry<TextLabel, MouseButtonEvent> mouseButtonEventListenerRegistry = new TwoArgListenerRegistry<>();
    private final TwoArgListenerRegistry<TextLabel, MouseMoveEvent> mouseMoveEventListenerRegistry = new TwoArgListenerRegistry<>();
    private final TwoArgListenerRegistry<TextLabel, MouseScrollEvent> mouseScrollEventListenerRegistry = new TwoArgListenerRegistry<>();
    private final TwoArgListenerRegistry<TextLabel, KeyEvent> keyEventListenerRegistry = new TwoArgListenerRegistry<>();
    private boolean blockInput;

    public TextLabel registerMouseButtonEventListener(BiConsumer<TextLabel, MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.register(callback);
        return self;
    }
    public TextLabel registerMouseScrollEventListener(BiConsumer<TextLabel, MouseScrollEvent> callback) {
        mouseScrollEventListenerRegistry.register(callback);
        return self;
    }
    public TextLabel registerKeyEventListener(BiConsumer<TextLabel, KeyEvent> callback) {
        keyEventListenerRegistry.register(callback);
        return self;
    }
    public TextLabel registerMouseMoveEventListener(BiConsumer<TextLabel, MouseMoveEvent> callback) {
        mouseMoveEventListenerRegistry.register(callback);
        return self;
    }

    public TextLabel unregisterMouseButtonEventListener(BiConsumer<TextLabel, MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.unregister(callback);
        return self;
    }
    public TextLabel unregisterMouseScrollEventListener(BiConsumer<TextLabel, MouseScrollEvent> callback) {
        mouseScrollEventListenerRegistry.unregister(callback);
        return self;
    }
    public TextLabel unregisterMouseMoveEventListener(BiConsumer<TextLabel, MouseMoveEvent> callback) {
        mouseMoveEventListenerRegistry.unregister(callback);
        return self;
    }
    public TextLabel unregisterKeyEventListener(BiConsumer<TextLabel, KeyEvent> callback) {
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

    public TextLabel setBlockInput(boolean blockInput) {
        this.blockInput = blockInput;
        return self;
    }

    public boolean isBlockInput() {
        return this.blockInput;
    }

    public CanWrappedTextRenderDescription description() {
        return renderDescription;
    }

    @Override
    protected void render(GuiRender render, double delta) {
        if (drawBackground) background.render(render);
        renderDescription.render(render);
    }
    @Override
    public TextLabel setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
        return this;
    }

    @Override
    public boolean isDrawBackground() {
        return drawBackground;
    }

    public TextLabel(String text, ScaleOffset position, ScaleOffset size) {
        this(text, null, position, size);
    }
    public TextLabel(String text, Font font, ScaleOffset position, ScaleOffset size) {
        super(position, size);
        this.renderDescription.setText(text).setFont(font);

    }
}
