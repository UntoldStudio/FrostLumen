package top.untoldstudio.frostlumen.core.gui.node;

import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.gui.GuiNode;
import top.untoldstudio.frostlumen.core.render.GuiRender;
import top.untoldstudio.frostlumen.core.texture.Texture;

public class ImageLabel extends ImageNode<ImageLabel> {
    private final ImageRenderDescription imageRenderDescription;
    private boolean drawBackground = true;

    @Override
    public void render(GuiRender render, long delta) {
        if (drawBackground) super.drawDefaultFrameBackground(render);
        imageRenderDescription.render(render);
    }

    public ImageLabel setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
        return this;
    }

    public boolean isDrawBackground() {
        return drawBackground;
    }

    public ImageRenderDescription getImageRenderDescription() {
        return imageRenderDescription;
    }

    @Override
    public void operationPosition(GuiNode<?> parentFrame, int parentRealPositionX, int parentRealPositionY) {
        super.operationPosition(parentFrame, parentRealPositionX, parentRealPositionY);
        operationImageAlignment();
    }

    private void operationImageAlignment() {
        imageRenderDescription.operationImageAlignment();
    }

    public ImageLabel(Texture texture, ScaleOffset position, ScaleOffset size) {
        super(position, size);
        this.imageRenderDescription = new ImageRenderDescription().setTexture(texture);
    }
}
