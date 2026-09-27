package top.untoldstudio.frostlumen.core.gui;

import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.event.*;
import top.untoldstudio.frostlumen.core.render.GuiRender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public sealed abstract class ParentNode<T extends ParentNode<T>> permits NodeRoot, GuiNode {
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

    public void dispatchRender(GuiRender render) {
        reverseFor(node -> node.dispatchRender(render));
    }
    public void dispatchKeyEvent(KeyEvent event) {
        reverseFor(node -> node.dispatchKeyEvent(event));
    }
    public void dispatchMouseButtonEvent(MouseButtonEvent event) {
        reverseFor(node -> node.dispatchMouseButtonEvent(event));
    }
    public void dispatchMouseMoveEvent(MouseMoveEvent event) {
        reverseFor(node -> node.dispatchMouseMoveEvent(event));
    }
    public void dispatchMouseScrollEvent(MouseScrollEvent event) {
        reverseFor(node -> node.dispatchMouseScrollEvent(event));
    }
    public void dispatchUserInputTextEvent(UserInputTextEvent event) {
        reverseFor(node -> node.dispatchUserInputTextEvent(event));
    }
    public void dispatchUserDropFilesEvent(UserDropFilesEvent event) {
        reverseFor(node -> node.dispatchUserDropFilesEvent(event));
    }

    public void dispatchMouseEnterWindowEvent(MouseEnterWindowEvent event) {
        for (GuiNode<?> node : children) {
            node.dispatchMouseEnterWindowEvent(event);
        }
    }
    public void dispatchMouseLeaveWindowEvent(MouseLeaveWindowEvent event) {
        for (GuiNode<?> node : children) {
            node.dispatchMouseLeaveWindowEvent(event);
        }
    }
    public void dispatchFrameBufferSizeChangeEvent(FrameBufferSizeChangeEvent event) {
        for (GuiNode<?> node : children) {
            node.dispatchFrameBufferSizeChangeEvent(event);
        }
    }
    public void dispatchUserRequestWindowCloseEvent(UserRequestWindowCloseEvent event) {
        for (GuiNode<?> node : children) {
            node.dispatchUserRequestWindowCloseEvent(event);
        }
    }
    public void dispatchWindowCloseEvent(WindowCloseEvent event) {
        for (GuiNode<?> node : children) {
            node.dispatchWindowCloseEvent(event);
        }
    }
    public void dispatchWindowFocusChangeEvent(WindowFocusChangeEvent event) {
        for (GuiNode<?> node : children) {
            node.dispatchWindowFocusChangeEvent(event);
        }
    }
    public void dispatchWindowMinimizeEvent(WindowMinimizeEvent event) {
        for (GuiNode<?> node : children) {
            node.dispatchWindowMinimizeEvent(event);
        }
    }
    public void dispatchWindowMaximizeEvent(WindowMaximizeEvent event) {
        for (GuiNode<?> node : children) {
            node.dispatchWindowMaximizeEvent(event);
        }
    }
    public void dispatchWindowMoveEvent(WindowMoveEvent event) {
        for (GuiNode<?> node : children) {
            node.dispatchWindowMoveEvent(event);
        }
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

    protected void reverseFor(Consumer<GuiNode<?>> consumer) {
        for (int i = children.size() - 1; i >= 0; i--) {
            consumer.accept(children.get(i));
        }
    }
    protected void sortChildren() {
        children.sort(Comparator.comparingInt(GuiNode::getRenderLevel));
    }
}
