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
package top.untoldstudio.frostlumen.core.gui;

import top.untoldstudio.frostlumen.core.data.*;
import top.untoldstudio.frostlumen.core.tool.MathTool;
import top.untoldstudio.frostlumen.core.event.*;
import top.untoldstudio.frostlumen.core.render.GuiRender;

import java.util.HashSet;
import java.util.Set;

public abstract non-sealed class GuiNode<T extends GuiNode<T>> extends ParentNode<T> {
    protected NodeRoot root;
    protected ParentNode<?> parent;
    protected int renderLevel = 0;
    protected RGBA backgroundColor = RGBA.WHITE;
    protected RGBA backgroundBorderColor = RGBA.BLACK;
    protected int backgroundLeftBorderThickness = 0;
    protected int backgroundRightBorderThickness = 0;
    protected int backgroundTopBorderThickness = 0;
    protected int backgroundBottomBorderThickness = 0;
    protected int backgroundLeftTopCornerRadius = 0;
    protected int backgroundRightTopCornerRadius = 0;
    protected int backgroundLeftBottomCornerRadius = 0;
    protected int backgroundRightBottomCornerRadius = 0;
    protected ThicknessPosition backgroundBorderThicknessPosition = ThicknessPosition.OUTSIDE;
    protected boolean clipChildren = false;
    protected double xAnchor = 0;
    protected double yAnchor = 0;
    protected float angle = 0;
    protected Set<MouseButton> currentMouseClickButtons = new HashSet<>();
    protected boolean mouseInNode;

    protected void drawDefaultFrameBackground(GuiRender render) {
        if (backgroundLeftBorderThickness == 0 && backgroundRightBorderThickness == 0 && backgroundTopBorderThickness == 0 && backgroundBottomBorderThickness == 0 && backgroundLeftTopCornerRadius == 0 && backgroundRightTopCornerRadius == 0 && backgroundLeftBottomCornerRadius == 0 && backgroundRightBottomCornerRadius == 0) {
            render.drawRectangle(realPositionX, realPositionY, realPositionMaxX, realPositionMaxY, angle,
                    backgroundColor.red(), backgroundColor.green(), backgroundColor.blue(), backgroundColor.alpha());
        } else {
            render.drawShape(realPositionX, realPositionY, realPositionMaxX, realPositionMaxY, angle,
                    backgroundColor.red(), backgroundColor.green(), backgroundColor.blue(), backgroundColor.alpha(),
                    backgroundLeftTopCornerRadius, backgroundRightTopCornerRadius, backgroundLeftBottomCornerRadius, backgroundRightBottomCornerRadius,
                    backgroundLeftBorderThickness, backgroundRightBorderThickness, backgroundTopBorderThickness, backgroundBottomBorderThickness,
                    backgroundBorderColor.red(), backgroundBorderColor.green(), backgroundBorderColor.blue(), backgroundBorderColor.alpha(), backgroundBorderThicknessPosition
                    );
        }
    }

    protected void operationPosition(){
        if (getParent() != null && getParent() instanceof GuiNode<?> node){
            operationPosition(node, node.realPositionX, node.realPositionY);
        } else {
            operationPosition(null, 0, 0);
        }
    }
    protected void operationPosition(GuiNode<?> parentFrame, int parentRealPositionX, int parentRealPositionY) {
        if (root == null) return;
        Window window = root.getWindow();

        if (parentFrame != null) {
            realSizeX = (int) Math.round(size.xScale() * parentFrame.realSizeX) + size.xOffset();
            realSizeY = (int) Math.round(size.yScale() * parentFrame.realSizeY) + size.yOffset();

            realPositionX = parentRealPositionX
                    + (int) Math.round(position.xScale() * parentFrame.realSizeX)
                    + position.xOffset()
                    - (int) Math.round(realSizeX * xAnchor);

            realPositionY = parentRealPositionY
                    + (int) Math.round(position.yScale() * parentFrame.realSizeY)
                    + position.yOffset()
                    - (int) Math.round(realSizeY * yAnchor);
        } else {
            int windowWidth = window.getFrameBufferWidth();
            int windowHeight = window.getFrameBufferHeight();

            realSizeX = MathTool.round(windowWidth * size.xScale()) + size.xOffset();
            realSizeY = MathTool.round(windowHeight * size.yScale()) + size.yOffset();

            realPositionX = MathTool.round(windowWidth * position.xScale())
                    + position.xOffset()
                    - (int) Math.round(realSizeX * xAnchor);

            realPositionY = MathTool.round(windowHeight * position.yScale())
                    + position.yOffset()
                    - (int) Math.round(realSizeY * yAnchor);
        }

        realPositionMaxX = realPositionX + realSizeX;
        realPositionMaxY = realPositionY + realSizeY;
        letChildrenOperationPosition();
    }
    protected void letChildrenOperationPosition(){
        for (GuiNode<?> guiNode : children){
            guiNode.operationPosition();
        }
    }

    @Override
    public void dispatchRender(GuiRender render, long delta) {
        if (clipChildren) {
            render.enableScissor(realPositionX, realPositionY, realSizeX, realSizeY);
            super.dispatchRender(render, delta);
            render(render, delta);
            render.disableScissor();
        } else {
            super.dispatchRender(render, delta);
            render(render, delta);
        }
    }
    @Override
    public void dispatchKeyEvent(KeyEvent event) {
        super.dispatchKeyEvent(event);
        onKeyEvent(event);
    }
    public void dispatchMouseButtonEvent(MouseButtonEvent event) {
        if (event.getAction() == InputAction.PRESS && mouseInNode) {
            currentMouseClickButtons.add(event.getButton());
        } else if (event.getAction() == InputAction.RELEASE) {
            currentMouseClickButtons.remove(event.getButton());
        }
        super.dispatchMouseButtonEvent(event);
        onMouseButtonEvent(event);
    }
    @Override
    public void dispatchMouseMoveEvent(MouseMoveEvent event) {
        mouseInNode = isMouseInNode(root);
        super.dispatchMouseMoveEvent(event);
        onMouseMoveEvent(event);
    }
    @Override
    public void dispatchMouseScrollEvent(MouseScrollEvent event) {
        super.dispatchMouseScrollEvent(event);
        onMouseScrollEvent(event);
    }
    @Override
    public void dispatchUserInputTextEvent(UserInputTextEvent event) {
        super.dispatchUserInputTextEvent(event);
        onUserInputTextEvent(event);
    }
    @Override
    public void dispatchUserDropFilesEvent(UserDropFilesEvent event) {
        super.dispatchUserDropFilesEvent(event);
        onUserDropFilesEvent(event);
    }

    @Override
    public void dispatchMouseEnterWindowEvent(MouseEnterWindowEvent event) {
        onMouseEnterWindowEvent(event);
        super.dispatchMouseEnterWindowEvent(event);
    }
    @Override
    public void dispatchMouseLeaveWindowEvent(MouseLeaveWindowEvent event) {
        onMouseLeaveWindowEvent(event);
        super.dispatchMouseLeaveWindowEvent(event);
    }
    @Override
    public void dispatchFrameBufferSizeChangeEvent(FrameBufferSizeChangeEvent event) {
        operationPosition();
        onFrameBufferSizeChangeEvent(event);
        super.dispatchFrameBufferSizeChangeEvent(event);
    }
    @Override
    public void dispatchUserRequestWindowCloseEvent(UserRequestWindowCloseEvent event) {
        onUserRequestWindowCloseEvent(event);
        super.dispatchUserRequestWindowCloseEvent(event);
    }
    @Override
    public void dispatchWindowCloseEvent(WindowCloseEvent event) {
        onWindowCloseEvent(event);
        super.dispatchWindowCloseEvent(event);
    }
    @Override
    public void dispatchWindowFocusChangeEvent(WindowFocusChangeEvent event) {
        onWindowFocusChangeEvent(event);
        super.dispatchWindowFocusChangeEvent(event);
    }
    @Override
    public void dispatchWindowMinimizeEvent(WindowMinimizeEvent event) {
        onWindowMinimizeEvent(event);
        super.dispatchWindowMinimizeEvent(event);
    }
    @Override
    public void dispatchWindowMaximizeEvent(WindowMaximizeEvent event) {
        onWindowMaximizeEvent(event);
        super.dispatchWindowMaximizeEvent(event);
    }
    @Override
    public void dispatchWindowMoveEvent(WindowMoveEvent event) {
        onWindowMoveEvent(event);
        super.dispatchWindowMoveEvent(event);
    }

    protected abstract void render(GuiRender render, long delta);
    protected void onKeyEvent(KeyEvent event) {}
    protected void onMouseButtonEvent(MouseButtonEvent event) {}
    protected void onMouseMoveEvent(MouseMoveEvent event) {}
    protected void onMouseEnterWindowEvent(MouseEnterWindowEvent event) {}
    protected void onMouseLeaveWindowEvent(MouseLeaveWindowEvent event) {}
    protected void onMouseScrollEvent(MouseScrollEvent event) {}
    protected void onUserInputTextEvent(UserInputTextEvent event) {}
    protected void onFrameBufferSizeChangeEvent(FrameBufferSizeChangeEvent event) {}
    protected void onUserRequestWindowCloseEvent(UserRequestWindowCloseEvent event) {}
    protected void onWindowCloseEvent(WindowCloseEvent event) {}
    protected void onWindowFocusChangeEvent(WindowFocusChangeEvent event) {}
    protected void onUserDropFilesEvent(UserDropFilesEvent event) {}
    protected void onWindowMinimizeEvent(WindowMinimizeEvent event) {}
    protected void onWindowMaximizeEvent(WindowMaximizeEvent event) {}
    protected void onWindowMoveEvent(WindowMoveEvent event) {}

    @Override
    public T addChild(GuiNode<?> child) {
        super.addChild(child);
        setChildRootToThisRoot(child);
        return self;
    }

    private void setChildRootToThisRoot(GuiNode<?> child) {
        child.root = this.root;
        for (GuiNode<?> node : child.children) {
            setChildRootToThisRoot(node);
        }
    }

    public T setParent(ParentNode<?> parent) {
        parent.addChild(this);
        return self;
    }
    public ParentNode<?> getParent() {
        return parent;
    }

    public T setRenderLevel(int renderLevel) {
        this.renderLevel = renderLevel;
        return self;
    }
    public int getRenderLevel() {
        return renderLevel;
    }

    public T setAngle(float angle) {
        this.angle = angle;
        return self;
    }
    public T setPosition(ScaleOffset position) {
        this.position = position;
        operationPosition();
        return self;
    }
    public T setSize(ScaleOffset size) {
        this.size = size;
        operationPosition();
        return self;
    }
    public T setBackgroundColor(RGBA color) {
        this.backgroundColor = color;
        return self;
    }
    public T setBackgroundBorderThicknessPosition(ThicknessPosition position) {
        this.backgroundBorderThicknessPosition = position;
        return self;
    }
    public T setXAnchor(double xAnchor) {
        this.xAnchor = xAnchor;
        return self;
    }
    public T setYAnchor(double yAnchor) {
        this.yAnchor = yAnchor;
        return self;
    }
    public T setAnchor(double xAnchor, double yAnchor) {
        this.xAnchor = xAnchor;
        this.yAnchor = yAnchor;
        return self;
    }
    public T setBackgroundCornerRadius(int radius) {
        this.backgroundLeftTopCornerRadius = radius;
        this.backgroundRightTopCornerRadius = radius;
        this.backgroundLeftBottomCornerRadius = radius;
        this.backgroundRightBottomCornerRadius = radius;
        return self;
    }
    public T setBackgroundLeftTopCornerRadius(int backgroundLeftTopCornerRadius) {
        this.backgroundLeftTopCornerRadius = backgroundLeftTopCornerRadius;
        return self;
    }
    public T setBackgroundRightTopCornerRadius(int backgroundRightTopCornerRadius) {
        this.backgroundRightTopCornerRadius = backgroundRightTopCornerRadius;
        return self;
    }
    public T setBackgroundLeftBottomCornerRadius(int backgroundLeftBottomCornerRadius) {
        this.backgroundLeftBottomCornerRadius = backgroundLeftBottomCornerRadius;
        return self;
    }
    public T setBackgroundRightBottomCornerRadius(int backgroundRightBottomCornerRadius) {
        this.backgroundRightBottomCornerRadius = backgroundRightBottomCornerRadius;
        return self;
    }
    public T setBackgroundLeftBorderThickness(int backgroundLeftBorderThickness) {
        this.backgroundLeftBorderThickness = backgroundLeftBorderThickness;
        return self;
    }
    public T setBackgroundRightBorderThickness(int backgroundRightBorderThickness) {
        this.backgroundRightBorderThickness = backgroundRightBorderThickness;
        return self;
    }
    public T setBackgroundTopBorderThickness(int backgroundTopBorderThickness) {
        this.backgroundTopBorderThickness = backgroundTopBorderThickness;
        return self;
    }
    public T setBackgroundBottomBorderThickness(int backgroundBottomBorderThickness) {
        this.backgroundBottomBorderThickness = backgroundBottomBorderThickness;
        return self;
    }
    public T setBackgroundBorderColor(RGBA color) {
        this.backgroundBorderColor = color;
        return self;
    }
    public T setBackgroundBorderThickness(int backgroundBorderThickness) {
        this.backgroundLeftBorderThickness = backgroundBorderThickness;
        this.backgroundRightBorderThickness = backgroundBorderThickness;
        this.backgroundTopBorderThickness = backgroundBorderThickness;
        this.backgroundBottomBorderThickness = backgroundBorderThickness;
        return self;
    }
    public T setClipChildren(boolean clipChildren) {
        this.clipChildren = clipChildren;
        return self;
    }

    public boolean isClipChildren() {
        return clipChildren;
    }
    public float getAngle() {
        return angle;
    }
    public RGBA getBackgroundColor() {
        return backgroundColor;
    }
    public double getXAnchor() {
        return xAnchor;
    }
    public double getYAnchor() {
        return yAnchor;
    }
    public ThicknessPosition getBackgroundBorderThicknessPosition() {
        return backgroundBorderThicknessPosition;
    }
    public int getBackgroundLeftTopCornerRadius() {
        return backgroundLeftTopCornerRadius;
    }
    public int getBackgroundRightTopCornerRadius() {
        return backgroundRightTopCornerRadius;
    }
    public int getBackgroundLeftBottomCornerRadius() {
        return backgroundLeftBottomCornerRadius;
    }
    public int getBackgroundRightBottomCornerRadius() {
        return backgroundRightBottomCornerRadius;
    }
    public int getBackgroundLeftBorderThickness() {
        return backgroundLeftBorderThickness;
    }
    public int getBackgroundRightBorderThickness() {
        return backgroundRightBorderThickness;
    }
    public int getBackgroundTopBorderThickness() {
        return backgroundTopBorderThickness;
    }
    public int getBackgroundBottomBorderThickness() {
        return backgroundBottomBorderThickness;
    }

    public boolean isMouseInNode() {
        return mouseInNode;
    }
    public Set<MouseButton> getCurrentMouseClickButtons() {
        return currentMouseClickButtons;
    }

    private boolean isMouseInNode(NodeRoot root) {
        int centerX = (realPositionX + realPositionMaxX) / 2;
        int centerY = (realPositionY + realPositionMaxY) / 2;

        double dx = root.getWindow().getMouseX() - centerX;
        double dy = root.getWindow().getMouseY() - centerY;
        double rad = Math.toRadians(-angle);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double localX = centerX + dx * cos - dy * sin;
        double localY = centerY + dx * sin + dy * cos;
        return localX >= realPositionX && localX <= realPositionMaxX && localY >= realPositionY && localY <= realPositionMaxY;
    }

    public GuiNode(ScaleOffset position, ScaleOffset size) {
        this.position = position;
        this.size = size;
    }
}
