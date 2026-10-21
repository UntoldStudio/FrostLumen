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
import top.untold.frostlumen.core.font.Font;
import top.untold.frostlumen.core.listener.OneArgListenerRegistry;
import top.untold.frostlumen.core.render.GuiRender;

import java.util.function.Consumer;

public class TextLabel extends TextNode<TextLabel> implements HasDrawBackground<TextLabel>, InputBlocker<TextLabel> {
    private final CanWrappedTextRenderDescription renderDescription = new CanWrappedTextRenderDescription();
    private final OneArgListenerRegistry<MouseButtonEvent> mouseButtonEventListenerRegistry = new OneArgListenerRegistry<>();
    private boolean drawBackground = true;
    private boolean blockInput = false;

    public TextLabel registerMouseButtonEventListener(Consumer<MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.register(callback);
        return this;
    }

    public TextLabel unregisterMouseButtonEventListener(Consumer<MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.unregister(callback);
        return this;
    }

    @Override
    protected void onMouseButtonEvent(MouseButtonEvent event) {
        mouseButtonEventListenerRegistry.trigger(event);
    }

    public TextLabel setBlockInput(boolean blockInput) {
        this.blockInput = blockInput;
        return this;
    }

    public boolean isBlockInput() {
        return this.blockInput;
    }

    @Override
    protected void init(GuiRender render) {
        renderDescription.init(render);
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
