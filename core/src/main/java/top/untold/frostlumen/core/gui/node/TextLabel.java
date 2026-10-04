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

import top.untold.frostlumen.core.data.HorizontalAlignment;
import top.untold.frostlumen.core.data.RGBA;
import top.untold.frostlumen.core.data.ScaleOffset;
import top.untold.frostlumen.core.data.VerticalAlignment;
import top.untold.frostlumen.core.font.Font;
import top.untold.frostlumen.core.gui.GuiNode;
import top.untold.frostlumen.core.gui.ParentNode;
import top.untold.frostlumen.core.render.GuiRender;

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
    public void operationPosition(ParentNode<?> parentFrame, int parentRealPositionX, int parentRealPositionY) {
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
