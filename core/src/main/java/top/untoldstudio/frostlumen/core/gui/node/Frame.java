package top.untoldstudio.frostlumen.core.gui.node;

import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.gui.GuiNode;
import top.untoldstudio.frostlumen.core.render.GuiRender;

public final class Frame extends GuiNode<Frame> {
    @Override
    public void render(GuiRender render) {
        super.renderDefaultFrameBackground(render);
    }

    public Frame(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
