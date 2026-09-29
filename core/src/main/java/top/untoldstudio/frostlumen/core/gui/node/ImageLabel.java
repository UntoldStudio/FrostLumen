package top.untoldstudio.frostlumen.core.gui.node;

import top.untoldstudio.frostlumen.core.MathTool;
import top.untoldstudio.frostlumen.core.data.ImageAlignment;
import top.untoldstudio.frostlumen.core.data.RGBA;
import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.gui.GuiNode;
import top.untoldstudio.frostlumen.core.render.GuiRender;
import top.untoldstudio.frostlumen.core.texture.Texture;

public class ImageLabel extends GuiNode<ImageLabel> {
    private Texture texture;
    private boolean drawBackground = true;
    private RGBA textureColor = RGBA.WHITE;
    private ImageAlignment imageAlignment = ImageAlignment.STRETCH;
    private int imagePositionX;
    private int imagePositionY;
    private int imagePositionMaxX;
    private int imagePositionMaxY;

    @Override
    public void render(GuiRender render, long delta) {
        if (drawBackground) super.drawDefaultFrameBackground(render);
        render.enableScissor(realPositionX, realPositionY, realSizeX, realSizeY);
        render.drawTexture(texture, imagePositionX, imagePositionY, imagePositionMaxX, imagePositionMaxY, 0, 0, 1, 1, textureColor.red(), textureColor.green(), textureColor.blue(), textureColor.alpha());
        render.disableScissor();
    }

    public ImageLabel setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
        return this;
    }

    public ImageLabel setImageAlignment(ImageAlignment imageAlignment) {
        this.imageAlignment = imageAlignment;
        operationImageAlignment();
        return this;
    }
    /**
     * {@link top.untoldstudio.frostlumen.core.render.IResourceManager:getIResourceManagerFromThreadLocal()}
     */
    public ImageLabel setTexture(Texture texture) {
        this.texture = texture;
        operationImageAlignment();
        return this;
    }
    public ImageLabel setTextureColor(RGBA textureColor) {
        this.textureColor = textureColor;
        return this;
    }
    public Texture getTexture() {
        return texture;
    }
    public RGBA getTextureColor() {
        return textureColor;
    }
    public boolean isDrawBackground() {
        return drawBackground;
    }
    public ImageAlignment getImageAlignment() {
        return imageAlignment;
    }

    @Override
    public void operationPosition(GuiNode<?> parentFrame, int parentRealPositionX, int parentRealPositionY) {
        super.operationPosition(parentFrame, parentRealPositionX, parentRealPositionY);
        operationImageAlignment();
    }

    private void operationImageAlignment() {
        switch (imageAlignment) {
            case STRETCH -> {
                imagePositionX = realPositionX;
                imagePositionY = realPositionY;
                imagePositionMaxX = realPositionMaxX;
                imagePositionMaxY = realPositionMaxY;
            }
            case FIT -> {
                int width = texture.width();
                int height = texture.height();

                double scaleX = (double) realSizeX / width;
                double scaleY = (double) realSizeY / height;
                double scale = Math.min(scaleX, scaleY);

                int newWidth = MathTool.round(width * scale);
                int newHeight = MathTool.round(height * scale);

                imagePositionX = realPositionX + (realSizeX - newWidth) / 2;
                imagePositionY = realPositionY + (realSizeY - newHeight) / 2;
                imagePositionMaxX = imagePositionX + newWidth;
                imagePositionMaxY = imagePositionY + newHeight;
            }
            case FILL -> {
                int width = texture.width();
                int height = texture.height();

                double scaleX = (double) realSizeX / width;
                double scaleY = (double) realSizeY / height;
                double scale = Math.max(scaleX, scaleY);

                int newWidth = MathTool.round(width * scale);
                int newHeight = MathTool.round(height * scale);

                imagePositionX = realPositionX + (realSizeX - newWidth) / 2;
                imagePositionY = realPositionY + (realSizeY - newHeight) / 2;
                imagePositionMaxX = imagePositionX + newWidth;
                imagePositionMaxY = imagePositionY + newHeight;
            }
        }
    }

    public ImageLabel(Texture texture, ScaleOffset position, ScaleOffset size) {
        super(position, size);
        this.texture = texture;
    }
}
