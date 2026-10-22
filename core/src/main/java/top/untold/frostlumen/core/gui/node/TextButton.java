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

import top.untold.frostlumen.core.data.CursorShape;
import top.untold.frostlumen.core.data.MouseButton;
import top.untold.frostlumen.core.data.ScaleOffset;
import top.untold.frostlumen.core.event.MouseButtonEvent;
import top.untold.frostlumen.core.font.Font;
import top.untold.frostlumen.core.listener.TwoArgListenerRegistry;
import top.untold.frostlumen.core.render.GuiRender;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class TextButton extends TextNode<TextButton> implements HasDrawBackground<TextButton>, ButtonNode<TextButton> {
    private final TwoArgListenerRegistry<TextButton, MouseButtonEvent> mouseButtonEventListenerRegistry = new TwoArgListenerRegistry<>();
    private final Set<MouseButton> canTriggerMouseButtons = new HashSet<>();
    private final CanWrappedTextRenderDescription renderDescription = new CanWrappedTextRenderDescription();
    private boolean drawBackground = true;
    private boolean changeCursorShape = true;

    public TextButton addCanTriggerMouseButton(MouseButton button) {
        canTriggerMouseButtons.add(button);
        return this;
    }
    public TextButton removeCanTriggerMouseButton(MouseButton button) {
        canTriggerMouseButtons.remove(button);
        return this;
    }

    public TextButton registerMouseButtonEventListener(BiConsumer<TextButton, MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.register(callback);
        return this;
    }
    public TextButton unregisterMouseButtonEventListener(BiConsumer<TextButton, MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.unregister(callback);
        return this;
    }

    @Override
    protected void onMouseButtonEvent(MouseButtonEvent event) {
        if (!Collections.disjoint(canTriggerMouseButtons, currentMouseClickButtons)) {
            event.cancel();
            mouseButtonEventListenerRegistry.trigger(this, event);
        }
    }

    public TextButton setChangeCursorShape(boolean changeCursorShape) {
        this.changeCursorShape = changeCursorShape;
        return this;
    }

    public boolean isChangeCursorShape() {
        return changeCursorShape;
    }

    @Override
    protected void render(GuiRender render, double delta) {
        if (drawBackground) background.render(render);
        renderDescription.render(render);
        if (mouseInNode && changeCursorShape) {
            render.setCursorShape(CursorShape.HAND);
        }
    }

    @Override
    public TextButton setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
        return this;
    }

    @Override
    public boolean isDrawBackground() {
        return drawBackground;
    }

    public CanWrappedTextRenderDescription description() {
        return renderDescription;
    }

    public TextButton(String text, ScaleOffset position, ScaleOffset size) {
        this(text, null, position, size);
    }
    public TextButton(String text, Font font, ScaleOffset position, ScaleOffset size) {
        super(position, size);
        this.renderDescription.setText(text).setFont(font);
        canTriggerMouseButtons.add(MouseButton.LEFT);
    }
}
