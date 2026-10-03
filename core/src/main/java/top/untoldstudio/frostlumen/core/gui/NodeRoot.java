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
package top.untoldstudio.frostlumen.core.gui;

import top.untoldstudio.frostlumen.core.data.CursorShape;
import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.event.FrameBufferSizeChangeEvent;
import top.untoldstudio.frostlumen.core.event.MouseMoveEvent;
import top.untoldstudio.frostlumen.core.render.GuiRender;
import top.untoldstudio.frostlumen.core.tween.TweenScheduler;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class NodeRoot extends ParentNode<NodeRoot> {
    private final Window window;
    private final GuiRender render;
    private final Deque<Runnable> tasks = new ConcurrentLinkedDeque<>();
    private final TweenScheduler tweenScheduler = new TweenScheduler();
    private long externalSettingCursor = -1;
    private boolean isLastMouseMoveEventCanceled = false;
    private long lastRenderTime;

    public void init() {
        render.initRender();
        lastRenderTime = System.currentTimeMillis();
    }

    @Override
    public void dispatchMouseMoveEvent(MouseMoveEvent event) {
        super.dispatchMouseMoveEvent(event);
        isLastMouseMoveEventCanceled = event.isCancel();
    }

    @Override
    public void dispatchFrameBufferSizeChangeEvent(FrameBufferSizeChangeEvent event) {
        realSizeX = event.newWidth();
        realSizeY = event.newHeight();
        realPositionMaxX = event.newWidth();
        realPositionMaxY = event.newHeight();
        render.onFrameBufferSizeChange(realPositionMaxX, realPositionMaxY);
        super.dispatchFrameBufferSizeChangeEvent(event);
    }

    public void render() {
        render.beginFrame();
        while (!tasks.isEmpty()) {
            tasks.poll().run();
        }

        if (externalSettingCursor != -1 && !isLastMouseMoveEventCanceled) {
            render.setCursorShape(externalSettingCursor);
        } else {
            render.setCursorShape(CursorShape.ARROW);
        }
        long now = System.currentTimeMillis();
        long delta = now - lastRenderTime;

        tweenScheduler.step(delta);

        dispatchRender(render, delta);

        render.submitBuffer();

        render.endFrame();
        lastRenderTime = now;
    }

    public void runTask(Runnable task) {
        tasks.offer(task);
    }

    @Override
    public NodeRoot addChild(GuiNode<?> child) {
        setChildRootToThis(child);
        super.addChild(child);
        return self;
    }

    private void setChildRootToThis(GuiNode<?> child) {
        child.root = this;
        for (GuiNode<?> node : child.children) {
            setChildRootToThis(node);
        }
    }

    public Window getWindow() {
        return window;
    }

    public GuiRender getRender() {
        return render;
    }

    public TweenScheduler getTweenScheduler() {
        return tweenScheduler;
    }

    public void setExternalSettingCursor(long handle) {
        this.externalSettingCursor = handle;
    }

    public NodeRoot(Window window, GuiRender render) {
        this.window = window;
        this.render = render;

        size = ScaleOffset.fromScale(1, 1);
        position = ScaleOffset.ZERO;
        realPositionX = 0;
        realPositionY = 0;
        realSizeX = window.getFrameBufferWidth();
        realSizeY = window.getFrameBufferHeight();
        realPositionMaxX = window.getFrameBufferWidth();
        realPositionMaxY = window.getFrameBufferHeight();
    }
}
