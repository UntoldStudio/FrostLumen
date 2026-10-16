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

import top.untold.frostlumen.core.data.*;
import top.untold.frostlumen.core.event.KeyEvent;
import top.untold.frostlumen.core.event.MouseButtonEvent;
import top.untold.frostlumen.core.event.UserInputTextEvent;
import top.untold.frostlumen.core.font.Font;
import top.untold.frostlumen.core.render.GuiRender;

public class TextBox extends TextNode<TextBox> implements HasDrawBackground<TextBox> {
    private final TextRenderDescription noInput = new TextRenderDescription();
    private final BoxNormalRenderDescription normal = new BoxNormalRenderDescription();
    private boolean drawBackground = true;
    private int[] textSizes = new int[0];
    private int textHeight;
    private int cursorPosition = 0;
    private int cursorPixelPosition;
    private int cursorWidth = 1;
    private RGBA cursorColor = RGBA.WHITE;
    private boolean shouldDisplayCursor = false;
    private long cursorSwitchVisibleTime = 30;
    private long displayCursorTimer = 30;

    @Override
    public TextBox setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
        return this;
    }

    @Override
    public boolean isDrawBackground() {
        return drawBackground;
    }

    @Override
    protected void init(GuiRender render) {
        noInput.init(render);
        normal.init(render);
    }

    @Override
    protected void tick() {
        if (displayCursorTimer > 0) {
            displayCursorTimer--;
        } else {
            shouldDisplayCursor = !shouldDisplayCursor;
            displayCursorTimer = cursorSwitchVisibleTime;
        }
    }

    @Override
    protected void render(GuiRender render, double delta) {
        if (drawBackground) background.render(render);
        if (normal.getText() != null) {
            normal.render(render);
        } else {
            noInput.render(render);
        }
        if (isFocused() && shouldDisplayCursor) {
            render.drawRectangle(cursorPixelPosition, normal.textRenderPositionY, cursorPixelPosition + cursorWidth, normal.textRenderPositionY + textHeight, realAngle, cursorColor.red(), cursorColor.green(), cursorColor.blue(), cursorColor.alpha());
        }
        if (mouseInNode) {
            render.setCursorShape(CursorShape.IBEAM);
        }
    }

    @Override
    protected void onUserInputTextEvent(UserInputTextEvent event) {
        if (isFocused()) {
            event.cancel();
            insertString(event.getText());
        }
    }

    @Override
    protected void onMouseButtonEvent(MouseButtonEvent event) {
        if (event.getButton() == MouseButton.LEFT && event.getAction() == InputAction.PRESS) {
            int textMin = normal.textRenderPositionX;
            int textMax = normal.textRenderPositionX;

            for (int size : textSizes) {
                textMax += size;
            }
            double target = event.getX();
            if (target < textMin) {
                setCursorPosition(0);
            } else if (target > textMax) {
                setCursorPosition(textSizes.length);
            } else {
                int current = textMin;
                int position = 0;
                for (int size : textSizes) {
                    if (current >= target) break;
                    current += size;
                    position++;
                }
                setCursorPosition(position);
            }
            displayCursorNow();
        }
    }

    private void insertString(String text) {
        String string = normal.getText();
        normal.setText(new StringBuffer(string).insert(cursorPosition, text).toString());
        normal.operationCursorPosition();
        cursorPosition++;
        normal.operationCursorPosition();
    }

    @Override
    protected void onKeyEvent(KeyEvent event) {
        if (event.getInputAction() != InputAction.RELEASE) {
            Key key = event.getKey();
            if (key == Key.LEFT && cursorPosition > 0) {
                cursorPosition--;
                normal.operationCursorPosition();
                displayCursorTimer = cursorSwitchVisibleTime;
                shouldDisplayCursor = true;
            } else if (key == Key.RIGHT && cursorPosition < textSizes.length) {
                cursorPosition++;
                normal.operationCursorPosition();
                displayCursorTimer = cursorSwitchVisibleTime;
                shouldDisplayCursor = true;
            } else if (key == Key.BACKSPACE && textSizes.length > 0) {
                String result = new StringBuilder(normal.getText()).deleteCharAt(cursorPosition - 1).toString();
                if (cursorPosition > 0) {
                    cursorPosition--;
                }
                normal.setText(result);
            } else if (key == Key.V && event.getInputModifiers().isControlPressed()) {
                boolean isAtEnd = cursorPosition == textSizes.length;
                insertString(root.getWindow().getClipboard());
                if (isAtEnd) {
                    setCursorPosition(textSizes.length);
                }
            } else if (key == Key.ENTER) {
                setFocused(false);
            }
        }
    }

    private void displayCursorNow() {
        displayCursorTimer = cursorSwitchVisibleTime;
        shouldDisplayCursor = true;
    }

    private void setCursorPosition(int position) {
        this.cursorPosition = position;
        normal.operationCursorPosition();
    }

    public TextRenderDescription noInput() {
        return noInput;
    }

    public TextRenderDescription normal() {
        return normal;
    }

    private class BoxNormalRenderDescription extends TextRenderDescription {
        @Override
        protected void init(GuiRender render) {
            super.init(render);
            textHeight = font.getStringHeight(fontSize);
            operationCursorPosition();
        }

        @Override
        public TextRenderDescription setText(String text) {
            super.setText(text);
            if (font != null) {
                textSizes = font.getStringWidths(text, fontSize);
            } else {
                textSizes = null;
            }
            return this;
        }

        @Override
        public TextRenderDescription setFont(Font font) {
            super.setFont(font);
            textHeight = font.getStringHeight(fontSize);
            return this;
        }

        protected void operationTextRenderPosition() {
            super.operationTextRenderPosition();
            operationCursorPosition();
        }

        private void operationCursorPosition() {
            int value = textRenderPositionX;

            if (textSizes.length < cursorPosition) {
                cursorPosition = textSizes.length - 1;
            }

            for (int i = 0; i < cursorPosition; i++) {
                int size = textSizes[i];
                value += size;
            }

            cursorPixelPosition = value;
        }
    }

    public TextBox(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
