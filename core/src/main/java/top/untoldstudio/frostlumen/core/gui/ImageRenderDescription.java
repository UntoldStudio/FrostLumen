package top.untoldstudio.frostlumen.core.gui;

import top.untoldstudio.frostlumen.core.data.ImageAlignment;
import top.untoldstudio.frostlumen.core.data.RGBA;
import top.untoldstudio.frostlumen.core.texture.Texture;

public class ImageRenderDescription {
    private Texture texture;
    private RGBA color;
    private ImageAlignment imageAlignment = ImageAlignment.STRETCH;
    private int imagePositionX;
    private int imagePositionY;
    private int imagePositionMaxX;
    private int imagePositionMaxY;

    public Texture getTexture() {
        return texture;
    }
    public RGBA getColor() {
        return color;
    }
    public ImageAlignment getImageAlignment() {
        return imageAlignment;
    }
    public int getImagePositionX() {
        return imagePositionX;
    }
    public int getImagePositionY() {
        return imagePositionY;
    }
    public int getImagePositionMaxX() {
        return imagePositionMaxX;
    }
    public int getImagePositionMaxY() {
        return imagePositionMaxY;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }
    public void setColor(RGBA color) {
        this.color = color;
    }
    public void setImageAlignment(ImageAlignment imageAlignment) {
        this.imageAlignment = imageAlignment;
    }
    public void setImagePositionX(int imagePositionX) {
        this.imagePositionX = imagePositionX;
    }
    public void setImagePositionY(int imagePositionY) {
        this.imagePositionY = imagePositionY;
    }
    public void setImagePositionMaxX(int imagePositionMaxX) {
        this.imagePositionMaxX = imagePositionMaxX;
    }
    public void setImagePositionMaxY(int imagePositionMaxY) {
        this.imagePositionMaxY = imagePositionMaxY;
    }
}
