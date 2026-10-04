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
import top.untold.frostlumen.core.listener.OneArgListenerRegistry;
import top.untold.frostlumen.core.render.GuiRender;
import top.untold.frostlumen.core.texture.Texture;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ImageButton extends ImageNode<ImageButton> {
    private final OneArgListenerRegistry<MouseButtonEvent> mouseButtonEventListenerRegistry = new OneArgListenerRegistry<>();
    private final Set<MouseButton> canTriggerMouseButtons = new HashSet<>();
    private final ImageNode<ImageButton>.ImageRenderDescription normal;
    private final ImageNode<ImageButton>.ImageRenderDescription onHover = new ImageRenderDescription();
    private final ImageNode<ImageButton>.ImageRenderDescription onClick = new ImageRenderDescription();
    private boolean drawBackground = false;

    public ImageButton addCanTriggerMouseButton(MouseButton button) {
        canTriggerMouseButtons.add(button);
        return this;
    }
    public ImageButton removeCanTriggerMouseButton(MouseButton button) {
        canTriggerMouseButtons.remove(button);
        return this;
    }

    @Override
    protected void render(GuiRender render, long delta) {
        if (drawBackground) super.drawDefaultFrameBackground(render);
        if (!Collections.disjoint(canTriggerMouseButtons, currentMouseClickButtons)) {
            if (onClick.canRender()) {
                onClick.render(render);
            } else {
                normal.render(render);
            }
            render.setCursorShape(CursorShape.HAND);
        } else if (mouseInNode) {
            if (onHover.canRender()) {
                onHover.render(render);
            } else {
                normal.render(render);
            }
            render.setCursorShape(CursorShape.HAND);
        } else {
            normal.render(render);
        }
    }

    public ImageButton registerMouseButtonEventListener(Consumer<MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.register(callback);
        return this;
    }
    public ImageButton unregisterMouseButtonEventListener(Consumer<MouseButtonEvent> callback) {
        mouseButtonEventListenerRegistry.unregister(callback);
        return this;
    }

    @Override
    protected void onMouseButtonEvent(MouseButtonEvent event) {
        if (!Collections.disjoint(canTriggerMouseButtons, currentMouseClickButtons)) {
            mouseButtonEventListenerRegistry.trigger(event);
        }
    }

    public boolean isDrawBackground() {
        return drawBackground;
    }
    public ImageNode<ImageButton>.ImageRenderDescription normal() {
        return normal;
    }
    public ImageNode<ImageButton>.ImageRenderDescription onHover() {
        return onHover;
    }
    public ImageNode<ImageButton>.ImageRenderDescription onClick() {
        return onClick;
    }

    public ImageButton setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
        return this;
    }

    public ImageButton(Texture normal, ScaleOffset position, ScaleOffset size) {
        super(position, size);
        this.normal = new ImageNode<ImageButton>.ImageRenderDescription().setTexture(normal);
        canTriggerMouseButtons.add(MouseButton.LEFT);
    }
}
