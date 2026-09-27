package top.untoldstudio.frostlumen.core.gui;

import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.event.FrameBufferSizeChangeEvent;
import top.untoldstudio.frostlumen.core.render.GuiRender;

public final class NodeRoot extends ParentNode<NodeRoot> {
    private final Window window;
    private final GuiRender render;

    @Override
    public void dispatchFrameBufferSizeChangeEvent(FrameBufferSizeChangeEvent event) {
        realSizeX = event.newWidth();
        realSizeY = event.newHeight();
        realPositionMaxX = event.newWidth();
        realPositionMaxY = event.newHeight();
        super.dispatchFrameBufferSizeChangeEvent(event);
    }

    public void render() {
        render.beginFrame();
        dispatchRender(render);
        render.submitBuffer();
        render.endFrame();
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
