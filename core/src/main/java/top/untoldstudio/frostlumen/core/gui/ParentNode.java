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

import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.event.*;
import top.untoldstudio.frostlumen.core.listener.GuiRenderAndLongListenerRegistry;
import top.untoldstudio.frostlumen.core.listener.consumer.GuiRenderAndLongConsumer;
import top.untoldstudio.frostlumen.core.render.GuiRender;

import java.util.*;
import java.util.function.Consumer;

public sealed abstract class ParentNode<T extends ParentNode<T>> permits NodeRoot, GuiNode {
    private final GuiRenderAndLongListenerRegistry beforeRenderListenerRegistry = new GuiRenderAndLongListenerRegistry();
    private final GuiRenderAndLongListenerRegistry afterRenderListenerRegistry = new GuiRenderAndLongListenerRegistry();

    @SuppressWarnings("unchecked")
    protected final T self = (T) this;
    protected ScaleOffset position;
    protected ScaleOffset size;
    protected int realPositionX;
    protected int realPositionY;
    protected int realPositionMaxX;
    protected int realPositionMaxY;
    protected int realSizeX;
    protected int realSizeY;
    protected float realAngle;
    protected final List<GuiNode<?>> children = new ArrayList<>();

    public ScaleOffset getPosition() {
        return position;
    }
    public ScaleOffset getSize() {
        return size;
    }
    public int getRealPositionX() {
        return realPositionX;
    }
    public int getRealPositionY() {
        return realPositionY;
    }
    public int getRealPositionMaxX() {
        return realPositionMaxX;
    }
    public int getRealPositionMaxY() {
        return realPositionMaxY;
    }
    public int getRealSizeX() {
        return realSizeX;
    }
    public int getRealSizeY() {
        return realSizeY;
    }

    public void registerBeforeRenderListener(GuiRenderAndLongConsumer consumer) {
        beforeRenderListenerRegistry.register(consumer);
    }
    public void unregisterBeforeRenderListener(GuiRenderAndLongConsumer consumer) {
        beforeRenderListenerRegistry.unregister(consumer);
    }
    public void registerAfterRenderListener(GuiRenderAndLongConsumer consumer) {
        afterRenderListenerRegistry.register(consumer);
    }
    public void unregisterAfterRenderListener(GuiRenderAndLongConsumer consumer) {
        afterRenderListenerRegistry.unregister(consumer);
    }

    public void dispatchRender(GuiRender render, long delta) {
        beforeRenderListenerRegistry.trigger(render, delta);
        forwardFor(node -> node.dispatchRender(render, delta), null);
        afterRenderListenerRegistry.trigger(render, delta);
    }
    public void dispatchKeyEvent(KeyEvent event) {
        reverseFor(node -> node.dispatchKeyEvent(event), event);
    }
    public void dispatchMouseButtonEvent(MouseButtonEvent event) {
        reverseFor(node -> node.dispatchMouseButtonEvent(event), event);
    }
    public void dispatchMouseMoveEvent(MouseMoveEvent event) {
        reverseFor(node -> node.dispatchMouseMoveEvent(event), event);
    }
    public void dispatchMouseScrollEvent(MouseScrollEvent event) {
        reverseFor(node -> node.dispatchMouseScrollEvent(event), event);
    }
    public void dispatchUserInputTextEvent(UserInputTextEvent event) {
        reverseFor(node -> node.dispatchUserInputTextEvent(event), event);
    }
    public void dispatchUserDropFilesEvent(UserDropFilesEvent event) {
        reverseFor(node -> node.dispatchUserDropFilesEvent(event), event);
    }

    public void dispatchMouseEnterWindowEvent(MouseEnterWindowEvent event) {
        forwardFor(node -> node.dispatchMouseEnterWindowEvent(event), null);
    }
    public void dispatchMouseLeaveWindowEvent(MouseLeaveWindowEvent event) {
        forwardFor(node -> node.dispatchMouseLeaveWindowEvent(event), null);
    }
    public void dispatchFrameBufferSizeChangeEvent(FrameBufferSizeChangeEvent event) {
        forwardFor(node -> node.dispatchFrameBufferSizeChangeEvent(event), null);
    }
    public void dispatchUserRequestWindowCloseEvent(UserRequestWindowCloseEvent event) {
        forwardFor(node -> node.dispatchUserRequestWindowCloseEvent(event), event);
    }
    public void dispatchWindowCloseEvent(WindowCloseEvent event) {
        forwardFor(node -> node.dispatchWindowCloseEvent(event), null);
    }
    public void dispatchWindowFocusChangeEvent(WindowFocusChangeEvent event) {
        forwardFor(node -> node.dispatchWindowFocusChangeEvent(event), null);
    }
    public void dispatchWindowMinimizeEvent(WindowMinimizeEvent event) {
        forwardFor(node -> node.dispatchWindowMinimizeEvent(event), null);
    }
    public void dispatchWindowMaximizeEvent(WindowMaximizeEvent event) {
        forwardFor(node -> node.dispatchWindowMaximizeEvent(event), null);
    }
    public void dispatchWindowMoveEvent(WindowMoveEvent event) {
        forwardFor(node -> node.dispatchWindowMoveEvent(event), null);
    }

    public T addChild(GuiNode<?> child) {
        child.parent = this;
        children.add(child);
        sortChildren();
        child.operationPosition();
        return self;
    }
    public T addChildren(GuiNode<?>... children) {
        for (GuiNode<?> child : children) {
            addChild(child);
        }
        return self;
    }
    public T removeChild(GuiNode<?> child) {
        children.remove(child);
        child.parent = null;
        return self;
    }
    public T removeChildren(GuiNode<?>... children) {
        for (GuiNode<?> child : children) {
            removeChild(child);
        }
        return self;
    }
    public List<GuiNode<?>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    protected void forwardFor(Consumer<GuiNode<?>> consumer, CancelableEvent event) {
        for (int i = 0; i < children.size(); i++) {
            consumer.accept(children.get(i));
            if (event != null && event.isCancel()) break;
        }
    }
    protected void reverseFor(Consumer<GuiNode<?>> consumer, CancelableEvent event) {
        for (int i = children.size() - 1; i >= 0; i--) {
            consumer.accept(children.get(i));
            if (event != null && event.isCancel()) break;
        }
    }
    protected void sortChildren() {
        children.sort(Comparator.comparingInt(GuiNode::getZIndex));
    }
}
