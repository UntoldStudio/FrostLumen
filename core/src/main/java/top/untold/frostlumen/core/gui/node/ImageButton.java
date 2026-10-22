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
import top.untold.frostlumen.core.listener.TwoArgListenerRegistry;
import top.untold.frostlumen.core.render.GuiRender;
import top.untold.frostlumen.core.texture.Texture;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class ImageButton extends ImageNode<ImageButton> implements HasDrawBackground<ImageButton>, ButtonNode<ImageButton> {
    private final TwoArgListenerRegistry<ImageButton, MouseButtonEvent> mouseButtonEventListenerRegistry = new TwoArgListenerRegistry<>();
    private final Set<MouseButton> canTriggerMouseButtons = new HashSet<>();
    private final ImageRenderDescription normal;
    private final ImageRenderDescription onHover = new ImageRenderDescription();
    private final ImageRenderDescription onClick = new ImageRenderDescription();
    private boolean drawBackground = false;
    private boolean changeCursorShape = true;

    public ImageButton addCanTriggerMouseButton(MouseButton button) {
        canTriggerMouseButtons.add(button);
        return this;
    }
    public ImageButton removeCanTriggerMouseButton(MouseButton button) {
        canTriggerMouseButtons.remove(button);
        return this;
    }

    @Override
    protected void render(GuiRender render, double delta) {
        if (drawBackground) background.render(render);
        if (!Collections.disjoint(canTriggerMouseButtons, currentMouseClickButtons) && mouseInNode) {
            if (onClick.canRender()) {
                onClick.render(render);
            } else {
                normal.render(render);
            }
            if (changeCursorShape) render.setCursorShape(CursorShape.HAND);
        } else if (mouseInNode) {
            if (onHover.canRender()) {
                onHover.render(render);
            } else {
                normal.render(render);
            }
            if (changeCursorShape) render.setCursorShape(CursorShape.HAND);
        } else {
            normal.render(render);
        }
    }

    public ImageButton registerMouseButtonEventListener(BiConsumer<ImageButton, MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.register(callback);
        return this;
    }
    public ImageButton unregisterMouseButtonEventListener(BiConsumer<ImageButton, MouseButtonEvent> callback) {
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

    @Override
    public boolean isDrawBackground() {
        return drawBackground;
    }

    public ImageRenderDescription normal() {
        return normal;
    }
    public ImageRenderDescription onHover() {
        return onHover;
    }
    public ImageRenderDescription onClick() {
        return onClick;
    }

    public ImageButton setChangeCursorShape(boolean changeCursorShape) {
        this.changeCursorShape = changeCursorShape;
        return this;
    }

    public boolean isChangeCursorShape() {
        return changeCursorShape;
    }

    @Override
    public ImageButton setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
        return this;
    }

    public ImageButton(Texture normal, ScaleOffset position, ScaleOffset size) {
        super(position, size);
        this.normal = new ImageRenderDescription().setTexture(normal);
        canTriggerMouseButtons.add(MouseButton.LEFT);
    }
}
