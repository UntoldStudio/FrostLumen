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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TextNode<T extends TextNode<T>> extends GuiNode<T> {
    private final Set<AbstractTextRenderDescription<?>> selfRenderDescriptions = new HashSet<>();

    @Override
    protected void onRelayout() {
        for (AbstractTextRenderDescription<?> renderDescription : selfRenderDescriptions) {
            renderDescription.operationTextRenderPosition();
        }
    }

    public abstract class AbstractTextRenderDescription<R extends AbstractTextRenderDescription<R>> {
        @SuppressWarnings("unchecked")
        protected final R self = (R) this;
        protected int textRenderPositionX;
        protected int textRenderPositionY;
        protected Font font;
        protected int fontSize = 14;
        protected String text = "";
        protected double italicDegrees = 0;
        protected int boldStrength = 0;
        protected HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
        protected VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
        protected RGBA color = RGBA.BLACK;

        public void render(GuiRender render) {
            if (font == null) return;
            render.drawString(text, font, textRenderPositionX, textRenderPositionY, realAngle, fontSize, italicDegrees, boldStrength, color.red(), color.green(), color.blue(), color.alpha());
        }

        protected void init(GuiRender render) {
            if (font != null) return;
            font = Font.loadDefaultFont(render);
            operationTextRenderPosition();
        }

        public T getNode() {
            return TextNode.this.self;
        }

        public R setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
            this.horizontalAlignment = horizontalAlignment;
            operationTextRenderPosition();
            return self;
        }
        public R setVerticalAlignment(VerticalAlignment verticalAlignment) {
            this.verticalAlignment = verticalAlignment;
            operationTextRenderPosition();
            return self;
        }

        public R setFontSize(int fontSize) {
            this.fontSize = fontSize;
            operationTextRenderPosition();
            return self;
        }
        public R setText(String text) {
            this.text = text;
            operationTextRenderPosition();
            return self;
        }
        public R setFont(Font font) {
            this.font = font;
            operationTextRenderPosition();
            return self;
        }
        public R setColor(RGBA color) {
            this.color = color;
            return self;
        }
        public R setItalicDegrees(double italicDegrees) {
            this.italicDegrees = italicDegrees;
            return self;
        }
        public R setBoldStrength(int boldStrength) {
            this.boldStrength = boldStrength;
            return self;
        }

        protected void operationTextRenderPosition() {
            if (font == null) return;

            int textWidth = font.getStringWidth(text, fontSize);
            int textHeight = font.getStringHeight(fontSize);

            operationAlignment(textWidth, textHeight);
        }

        protected void operationAlignment(int textWidth, int textHeight) {
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

        protected AbstractTextRenderDescription() {
            selfRenderDescriptions.add(this);
        }
    }
    public class TextRenderDescription extends AbstractTextRenderDescription<TextRenderDescription> {
    }

    public class CanWrappedTextRenderDescription extends AbstractTextRenderDescription<CanWrappedTextRenderDescription> {
        protected final List<Line> lines = new ArrayList<>();
        protected boolean wrap = false;

        public CanWrappedTextRenderDescription setWrap(boolean wrap) {
            boolean shouldOperation = wrap && !this.wrap;
            this.wrap = wrap;
            if (shouldOperation) operationTextRenderPosition();
            return self;
        }
        public boolean isWrap() {
            return wrap;
        }

        @Override
        public void render(GuiRender render) {
            if (font == null) return;
            for (Line line : lines) {
                line.render(render);
            }
        }

        @Override
        public void operationTextRenderPosition() {
            if (font == null) return;

            int height = font.getStringHeight(fontSize);
            int sizeX = realSizeX;

            int[] charSizes = font.getStringWidths(text, fontSize);
            int currentChar = 0;

            int sizeOneLine = Integer.MIN_VALUE;

            lines.clear();

            while (currentChar < charSizes.length) {
                Line line = new Line();

                int startChar = currentChar;
                int currentX = 0;

                boolean isWrapChar = false;

                while (currentChar < charSizes.length) {
                    char c = text.charAt(currentChar);

                    if (c == '\n') {
                        isWrapChar = true;
                        break;
                    }

                    int size = charSizes[currentChar];

                    if (wrap && currentX > 0 && currentX + size > sizeX) {
                        break;
                    }

                    currentX += size;
                    currentChar++;

                }

                line.text = text.substring(startChar, currentChar);

                lines.add(line);

                sizeOneLine = Math.max(currentX, sizeOneLine);

                if (isWrapChar) {
                    currentChar++;
                }
            }

            if (lines.isEmpty()) return;

            int textHeight = lines.size() * height;

            textRenderPositionY = switch (verticalAlignment) {
                case TOP -> realPositionY;
                case CENTER -> realPositionY + (realSizeY - textHeight) / 2;
                case BOTTOM -> realPositionMaxY - textHeight;
            };

            int centerX = realPositionX + realSizeX / 2;
            int centerY = textRenderPositionY + textHeight / 2;

            for (int i = 0; i < lines.size(); i++) {
                Line line = lines.get(i);

                int lineWidth = font.getStringWidth(line.text, fontSize);
                line.renderPositionX = switch (horizontalAlignment) {
                    case LEFT -> realPositionX;
                    case CENTER -> realPositionX + (realSizeX - lineWidth) / 2;
                    case RIGHT -> realPositionMaxX - lineWidth;
                };

                if (i == 0) {
                    super.textRenderPositionX = line.renderPositionX;
                }

                line.renderPositionY = textRenderPositionY + (i * height);

                line.centerX = centerX;
                line.centerY = centerY;
            }
        }

        public class Line {
            String text;
            int renderPositionX;
            int renderPositionY;
            int centerX;
            int centerY;

            void render(GuiRender render) {
                render.drawString(text, font, renderPositionX, renderPositionY, centerX, centerY, realAngle, fontSize, italicDegrees, boldStrength, color.red(), color.green(), color.blue(), color.alpha());
            }
        }
    }

    public TextNode(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
