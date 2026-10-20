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
import top.untold.frostlumen.core.event.*;
import top.untold.frostlumen.core.font.Font;
import top.untold.frostlumen.core.gui.ParentNode;
import top.untold.frostlumen.core.listener.OneArgListenerRegistry;
import top.untold.frostlumen.core.render.GuiRender;

import java.util.function.Consumer;

public class TextBox extends TextNode<TextBox> implements HasDrawBackground<TextBox> {
    private static final RGBA DEFAULT_SELECTED_COLOR = new RGBA(33, 66, 131, 255);
    private static final RGBA DEFAULT_CURSOR_COLOR = new RGBA(10, 132, 255, 255);
    private final OneArgListenerRegistry<String> onUserInputEndListenerRegistry = new OneArgListenerRegistry<>();
    private final TextRenderDescription noInput = new TextRenderDescription();
    private final BoxNormalRenderDescription normal = new BoxNormalRenderDescription();
    private boolean drawBackground = true;
    private int[] textSizes = new int[0];
    private int preeditEnd = 0;
    private int textHeight;
    private int cursorPosition = 0;
    private int cursorPixelPosition;
    private int cursorWithoutPreeditPixelPosition;
    private int cursorWidth = 1;
    private double mouseXOnPress;
    private int selectedStart;
    private int selectedEnd;
    private int selectedPixelStart;
    private int selectedPixelEnd;
    private boolean selected;
    private RGBA selectedColor = DEFAULT_SELECTED_COLOR;
    private RGBA cursorColor = DEFAULT_CURSOR_COLOR;
    private boolean shouldDisplayCursor = false;
    private long cursorSwitchVisibleTime = 30;
    private long displayCursorTimer = 30;

    public TextBox registerOnUserInputEndListener(Consumer<String> consumer) {
        onUserInputEndListenerRegistry.register(consumer);
        return this;
    }
    public TextBox unregisterOnUserInputEndListener(Consumer<String> consumer) {
        onUserInputEndListenerRegistry.unregister(consumer);
        return this;
    }

    public TextBox setCursorWidth(int cursorWidth) {
        this.cursorWidth = cursorWidth;
        return this;
    }
    public TextBox setCursorColor(RGBA cursorColor) {
        this.cursorColor = cursorColor;
        return this;
    }
    public TextBox setSelectedColor(RGBA selectedColor) {
        this.selectedColor = selectedColor;
        return this;
    }
    public TextBox setCursorSwitchVisibleTime(long cursorSwitchVisibleTime) {
        this.cursorSwitchVisibleTime = cursorSwitchVisibleTime;
        return this;
    }

    public int getCursorWidth() {
        return cursorWidth;
    }
    public RGBA getCursorColor() {
        return cursorColor;
    }
    public RGBA getSelectedColor() {
        return selectedColor;
    }
    public long getCursorSwitchVisibleTime() {
        return cursorSwitchVisibleTime;
    }

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
        if (isFocused() && currentMouseClickButtons.contains(MouseButton.LEFT)) {
            displayCursorTimer = cursorSwitchVisibleTime;
            shouldDisplayCursor = true;
            return;
        }

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
        if (isSelected()) {
            render.drawRectangle(selectedPixelStart, normal.textRenderPositionY, selectedPixelEnd, normal.textRenderPositionY + textHeight, realAngle, selectedColor.red(), selectedColor.green(), selectedColor.blue(), selectedColor.alpha());
        }
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
    protected void onWindowFocusNodeChangeEvent(ParentNode<?> oldNode, ParentNode<?> newNode) {
        if (oldNode == this && newNode != this) {
            onUserInputEndListenerRegistry.trigger(normal.getText());
        }

        if (newNode != this && isSelected()) {
            selected = false;
            selectedStart = 0;
            selectedEnd = 0;
        }
    }

    @Override
    protected void onUserInputTextEvent(UserInputTextEvent event) {
        if (isFocused()) {
            deleteSelected();
            deleteCursorChars(preeditEnd);
            preeditEnd = 0;
            event.cancel();
            insertString(event.getText());
        }
    }

    @Override
    protected void onMouseButtonEvent(MouseButtonEvent event) {
        if (!isFocused()) return;

        if (event.getButton() == MouseButton.LEFT) {
            if (event.getAction() == InputAction.PRESS) {
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
                    setCursorPosition(getCharIndexAt(target));
                }

                displayCursorNow();

                if (selected) {
                    selected = false;
                }

                mouseXOnPress = event.getX();

                event.cancel();
            }
        }
    }

    private int getCharIndexAt(double position) {
        int current = normal.textRenderPositionX;
        int index = 0;
        for (int size : textSizes) {
            if (current + size / 2.0 > position) {
                break;
            }
            current += size;
            index++;
        }
        return index;
    }

    private int getPixelPositionAt(int index) {
        int start = normal.textRenderPositionX;

        for (int i = 0; i < index; i++) {
            start += textSizes[i];
        }

        return start;
    }

    @Override
    protected void onMouseMoveEvent(MouseMoveEvent event) {
        if (!isFocused() || !currentMouseClickButtons.contains(MouseButton.LEFT)) return;

        int startChar = getCharIndexAt(mouseXOnPress);

        int endChar = getCharIndexAt(event.getX());

        int realStart = Math.min(startChar, endChar);
        int realEnd = Math.max(endChar, startChar);

        setCursorPosition(endChar);

        setSelectRange(realStart, realEnd);

        selected = true;
    }

    private void insertString(String text) {
        String string = normal.getText();
        normal.setText(new StringBuffer(string).insert(cursorPosition, text).toString());
        normal.operationCursorPosition();
        cursorPosition += text.length();
        normal.operationCursorPosition();
    }

    @Override
    protected void onKeyEvent(KeyEvent event) {
        if (!isFocused()) return;

        if (event.getInputAction() != InputAction.RELEASE) {
            Key key = event.getKey();
            if (key == Key.LEFT && cursorPosition > 0) {
                if (isSelected()) return;
                cursorPosition--;
                normal.operationCursorPosition();
                displayCursorTimer = cursorSwitchVisibleTime;
                shouldDisplayCursor = true;
            } else if (key == Key.RIGHT && cursorPosition < textSizes.length) {
                if (isSelected()) return;
                cursorPosition++;
                normal.operationCursorPosition();
                displayCursorTimer = cursorSwitchVisibleTime;
                shouldDisplayCursor = true;
            } else if (key == Key.BACKSPACE && textSizes.length > 0) {
                if (!deleteSelected()) {
                    deleteCursorChars(1);
                }
            } else if (key == Key.V && event.getInputModifiers().isControlPressed()) {
                boolean isAtEnd = cursorPosition == textSizes.length;
                deleteSelected();
                String clipboard = root.getWindow().getClipboard();
                if (clipboard != null) {
                    insertString(root.getWindow().getClipboard());
                }
                if (isAtEnd) {
                    setCursorPosition(textSizes.length);
                }
            } else if (key == Key.C && event.getInputModifiers().isControlPressed()) {
                if (!isSelected()) return;
                root.getWindow().setClipboard(normal.getText().substring(selectedStart, selectedEnd));
            } else if (key == Key.A && event.getInputModifiers().isControlPressed()) {
                selected = true;
                setSelectRange(0, textSizes.length);
            } else if (key == Key.ENTER) {
                setFocused(false);
            }

            event.cancel();
        }
    }

    private void setSelectRange(int start, int end) {
        this.selectedStart = start;
        this.selectedEnd = end;
        this.selectedPixelStart = getPixelPositionAt(start);
        this.selectedPixelEnd = getPixelPositionAt(end);
    }

    @Override
    protected void onRelayout() {
        super.onRelayout();

        if (isSelected()) {
            this.selectedPixelStart = getPixelPositionAt(selectedStart);
            this.selectedPixelEnd = getPixelPositionAt(selectedEnd);
        }

        setPreeditRegion();
    }

    @Override
    protected void onPreeditEvent(PreeditEvent event) {
        if (!isFocused()) return;

        deleteSelected();

        deleteCursorChars(preeditEnd);

        preeditEnd = 0;

        insertString(event.getPreeditText());
        preeditEnd = event.getPreeditText().length();

        normal.operationCursorPosition();

        setPreeditRegion();

        event.cancel();
    }

    private void setPreeditRegion() {
        root.getWindow().setPreeditRegion(cursorWithoutPreeditPixelPosition, normal.textRenderPositionY, cursorWidth, textHeight);
    }

    private void deleteCursorChars(int count) {
        if (cursorPosition == 0) return;
        StringBuilder builder = new StringBuilder(normal.getText());
        for (int i = 0; i < count; i++) {
            builder.deleteCharAt(cursorPosition - 1);
            cursorPosition--;
        }
        normal.setText(builder.toString());
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

    private boolean isSelected() {
        return selected && selectedStart - selectedEnd != 0;
    }

    private boolean deleteSelected() {
        if (!isSelected()) return false;
        cursorPosition = selectedEnd;
        deleteCursorChars(selectedEnd - selectedStart);
        selected = false;
        selectedStart = 0;
        selectedEnd = 0;
        return true;
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
            int value = getPixelPositionAt(cursorPosition - preeditEnd);

            if (textSizes.length < cursorPosition) {
                cursorPosition = textSizes.length - 1;
            }

            cursorWithoutPreeditPixelPosition = value;

            value = getPixelPositionAt(cursorPosition - preeditEnd);

            cursorPixelPosition = value;
        }
    }

    public TextBox(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
