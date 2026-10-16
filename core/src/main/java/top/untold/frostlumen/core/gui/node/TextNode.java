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

import java.util.HashSet;
import java.util.Set;

public abstract class TextNode<T extends TextNode<T>> extends GuiNode<T> {
    private final Set<TextRenderDescription> selfRenderDescriptions = new HashSet<>();

    @Override
    protected void operationPosition(ParentNode<?> parentFrame, int parentRealPositionX, int parentRealPositionY) {
        super.operationPosition(parentFrame, parentRealPositionX, parentRealPositionY);
        for (TextRenderDescription renderDescription : selfRenderDescriptions) {
            renderDescription.operationTextRenderPosition();
        }
    }

    public class TextRenderDescription {
        protected int textRenderPositionX;
        protected int textRenderPositionY;
        protected Font font;
        protected int fontSize = 14;
        protected String text = "";
        protected double italicDegrees = 0;
        protected int boldStrength = 0;
        protected HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
        protected VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
        protected RGBA color = RGBA.WHITE;

        public void render(GuiRender render) {
            if (font == null) return;
            render.drawString(text, font, textRenderPositionX, textRenderPositionY, angle, fontSize, italicDegrees, boldStrength, color.red(), color.green(), color.blue(), color.alpha());
        }

        protected void init(GuiRender render) {
            if (font != null) return;
            font = Font.loadDefaultFont(render);
        }

        public T getNode() {
            return self;
        }

        public TextRenderDescription setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
            this.horizontalAlignment = horizontalAlignment;
            return this;
        }
        public TextRenderDescription setVerticalAlignment(VerticalAlignment verticalAlignment) {
            this.verticalAlignment = verticalAlignment;
            return this;
        }

        public TextRenderDescription setFontSize(int fontSize) {
            this.fontSize = fontSize;
            operationTextRenderPosition();
            return this;
        }
        public TextRenderDescription setText(String text) {
            this.text = text;
            operationTextRenderPosition();
            return this;
        }
        public TextRenderDescription setFont(Font font) {
            this.font = font;
            operationTextRenderPosition();
            return this;
        }
        public TextRenderDescription setColor(RGBA color) {
            this.color = color;
            return this;
        }
        public TextRenderDescription setItalicDegrees(double italicDegrees) {
            this.italicDegrees = italicDegrees;
            return this;
        }
        public TextRenderDescription setBoldStrength(int boldStrength) {
            this.boldStrength = boldStrength;
            return this;
        }

        protected void operationTextRenderPosition() {
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
        public int getFontSize() {
            return fontSize;
        }
        public String getText() {
            return text;
        }
        public Font getFont() {
            return font;
        }
        public RGBA getColor() {
            return color;
        }
        public double getItalicDegrees() {
            return italicDegrees;
        }
        public int getBoldStrength() {
            return boldStrength;
        }

        protected TextRenderDescription() {
            selfRenderDescriptions.add(this);
        }
    }

    public TextNode(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
