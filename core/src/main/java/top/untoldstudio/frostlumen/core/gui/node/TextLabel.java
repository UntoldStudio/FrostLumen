package top.untoldstudio.frostlumen.core.gui.node;

import top.untoldstudio.frostlumen.core.data.HorizontalAlignment;
import top.untoldstudio.frostlumen.core.data.RGBA;
import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.data.VerticalAlignment;
import top.untoldstudio.frostlumen.core.font.Font;
import top.untoldstudio.frostlumen.core.gui.GuiNode;
import top.untoldstudio.frostlumen.core.render.GuiRender;

public class TextLabel extends GuiNode<TextLabel> {
    private int textRenderPositionX;
    private int textRenderPositionY;
    private boolean drawBackground = false;
    private Font font;
    private int fontSize = 14;
    private String text;
    private double italicDegrees = 0;
    private int boldStrength = 0;
    private RGBA textColor = RGBA.WHITE;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

    @Override
    protected void render(GuiRender render, long delta) {
        if (drawBackground) super.drawDefaultFrameBackground(render);
        render.drawString(text, font, textRenderPositionX, textRenderPositionY, angle, fontSize, italicDegrees, boldStrength, textColor.red(), textColor.green(), textColor.blue(), textColor.alpha());
    }

    public TextLabel setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        return this;
    }
    public TextLabel setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        return this;
    }
    public TextLabel setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
        return this;
    }
    public TextLabel setFontSize(int fontSize) {
        this.fontSize = fontSize;
        operationTextRenderPosition();
        return this;
    }
    public TextLabel setText(String text) {
        this.text = text;
        operationTextRenderPosition();
        return this;
    }
    public TextLabel setFont(Font font) {
        this.font = font;
        operationTextRenderPosition();
        return this;
    }
    public TextLabel setTextColor(RGBA textColor) {
        this.textColor = textColor;
        return this;
    }
    public TextLabel setItalicDegrees(double italicDegrees) {
        this.italicDegrees = italicDegrees;
        return this;
    }
    public TextLabel setBoldStrength(int boldStrength) {
        this.boldStrength = boldStrength;
        return this;
    }

    @Override
    public void operationPosition(GuiNode<?> parentFrame, int parentRealPositionX, int parentRealPositionY) {
        super.operationPosition(parentFrame, parentRealPositionX, parentRealPositionY);
        operationTextRenderPosition();
    }

    private void operationTextRenderPosition() {
        int textWidth = font.getStringWidth(text, fontSize);
        int textHeight = font.getStringHeight(fontSize);

        textRenderPositionX = switch (horizontalAlignment) {
            case LEFT -> realPositionX;
            case CENTER -> {
                int center = realPositionX + realSizeX / 2;
                yield center - textWidth / 2;
            }
            case RIGHT -> realPositionMaxX - textWidth;
        };
        textRenderPositionY = switch (verticalAlignment) {
            case TOP -> realPositionY;
            case CENTER -> {
                int center = realPositionY + realSizeY / 2;
                yield center - textHeight / 2;
            }
            case BOTTOM -> realPositionMaxY - textHeight;
        };
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }
    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }
    public boolean isDrawBackground() {
        return drawBackground;
    }
    public int getFontSize() {
        return fontSize;
    }
    public String getText() {
        return text;
    }
    public Font getFont() {
        return font;
    }
    public RGBA getTextColor() {
        return textColor;
    }
    public double getItalicDegrees() {
        return italicDegrees;
    }
    public int getBoldStrength() {
        return boldStrength;
    }

    public TextLabel(String text, ScaleOffset position, ScaleOffset size) {
        this(text, Font.getDefaultFont(), position, size);
    }
    public TextLabel(String text, Font font, ScaleOffset position, ScaleOffset size) {
        super(position, size);
        this.text = text;
        this.font = font;
    }
}
