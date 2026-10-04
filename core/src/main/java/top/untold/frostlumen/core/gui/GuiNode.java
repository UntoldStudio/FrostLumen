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
package top.untold.frostlumen.core.gui;

import top.untold.frostlumen.core.data.*;
import top.untold.frostlumen.core.event.*;
import top.untold.frostlumen.core.listener.OneArgListenerRegistry;
import top.untold.frostlumen.core.tool.MathTool;
import top.untold.frostlumen.core.render.GuiRender;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract non-sealed class GuiNode<T extends GuiNode<T>> extends ParentNode<T> {
    private final OneArgListenerRegistry<MouseMoveEvent> mouseEnterListenerRegistry = new OneArgListenerRegistry<>();
    private final OneArgListenerRegistry<MouseMoveEvent> mouseLeaveListenerRegistry = new OneArgListenerRegistry<>();
    protected NodeRoot root;
    protected ParentNode<?> parent;
    protected int zIndex = 0;
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
    protected double backgroundBlurStrength = 0;

    public T registerMouseEnterListener(Consumer<MouseMoveEvent> listener) {
        mouseEnterListenerRegistry.register(listener);
        return self;
    }
    public T unregisterMouseEnterListener(Consumer<MouseMoveEvent> listener) {
        mouseEnterListenerRegistry.unregister(listener);
        return self;
    }
    public T registerMouseLeaveListener(Consumer<MouseMoveEvent> listener) {
        mouseLeaveListenerRegistry.register(listener);
        return self;
    }
    public T unregisterMouseLeaveListener(Consumer<MouseMoveEvent> listener) {
        mouseLeaveListenerRegistry.unregister(listener);
        return self;
    }

    protected void drawDefaultFrameBackground(GuiRender render) {
        if (backgroundBlurStrength != 0 && backgroundColor.alpha() != 255) {
            boolean hasRoundedCorner = backgroundLeftTopCornerRadius != 0 || backgroundRightTopCornerRadius != 0 || backgroundLeftBottomCornerRadius != 0 || backgroundRightBottomCornerRadius != 0;
            if (hasRoundedCorner) {
                render.enableScissor(realPositionX, realPositionY, realSizeX, realSizeY, realAngle, backgroundLeftTopCornerRadius, backgroundRightTopCornerRadius, backgroundLeftBottomCornerRadius, backgroundRightBottomCornerRadius);
                render.blurRegion(realPositionX, realPositionY, realSizeX, realSizeY, realAngle, backgroundBlurStrength);
                render.disableScissor();
            } else {
                render.blurRegion(realPositionX, realPositionY, realSizeX, realSizeY, realAngle, backgroundBlurStrength);
            }
        }

        if (backgroundLeftBorderThickness == 0 && backgroundRightBorderThickness == 0 &&
                backgroundTopBorderThickness == 0 && backgroundBottomBorderThickness == 0 &&
                backgroundLeftTopCornerRadius == 0 && backgroundRightTopCornerRadius == 0 &&
                backgroundLeftBottomCornerRadius == 0 && backgroundRightBottomCornerRadius == 0
        ) {
            render.drawRectangle(realPositionX, realPositionY, realPositionMaxX, realPositionMaxY, realAngle, backgroundColor.red(), backgroundColor.green(), backgroundColor.blue(), backgroundColor.alpha());
        } else {
            render.drawShape(realPositionX, realPositionY, realPositionMaxX, realPositionMaxY, realAngle,
                    backgroundColor.red(), backgroundColor.green(), backgroundColor.blue(), backgroundColor.alpha(),
                    backgroundLeftTopCornerRadius, backgroundRightTopCornerRadius,
                    backgroundLeftBottomCornerRadius, backgroundRightBottomCornerRadius,
                    backgroundLeftBorderThickness, backgroundRightBorderThickness,
                    backgroundTopBorderThickness, backgroundBottomBorderThickness,
                    backgroundBorderColor.red(), backgroundBorderColor.green(),
                    backgroundBorderColor.blue(), backgroundBorderColor.alpha(),
                    backgroundBorderThicknessPosition);
        }
    }

    protected void operationPosition(){
        if (getParent() != null && getParent() instanceof ParentNode<?> node){
            operationPosition(node, node.realPositionX, node.realPositionY);
        } else {
            operationPosition(null, 0, 0);
        }
    }
    protected void operationPosition(ParentNode<?> parentFrame, int parentRealPositionX, int parentRealPositionY) {
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

        realAngle = (parentFrame == null ? 0 : parentFrame.realAngle) + angle;

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
            render.enableScissor(realPositionX, realPositionY, realSizeX, realSizeY, realAngle, backgroundLeftTopCornerRadius, backgroundRightTopCornerRadius, backgroundLeftBottomCornerRadius, backgroundRightBottomCornerRadius);
            render(render, delta);
            super.dispatchRender(render, delta);
            render.disableScissor();
        } else {
            render(render, delta);
            super.dispatchRender(render, delta);
        }
    }
    @Override
    public void dispatchKeyEvent(KeyEvent event) {
        super.dispatchKeyEvent(event);
        if (event.isCancel()) return;
        onKeyEvent(event);
    }
    public void dispatchMouseButtonEvent(MouseButtonEvent event) {
        if (event.getAction() == InputAction.PRESS && mouseInNode) {
            currentMouseClickButtons.add(event.getButton());
        } else if (event.getAction() == InputAction.RELEASE) {
            currentMouseClickButtons.remove(event.getButton());
        }
        super.dispatchMouseButtonEvent(event);
        if (event.isCancel()) return;
        onMouseButtonEvent(event);
    }
    @Override
    public void dispatchMouseMoveEvent(MouseMoveEvent event) {
        boolean current = isMouseInNode(root);
        if (current != mouseInNode) {
            if (current) {
                mouseEnterListenerRegistry.trigger(event);
            } else {
                mouseLeaveListenerRegistry.trigger(event);
            }

            mouseInNode = current;
        }
        super.dispatchMouseMoveEvent(event);
        if (event.isCancel()) return;
        onMouseMoveEvent(event);
    }
    @Override
    public void dispatchMouseScrollEvent(MouseScrollEvent event) {
        super.dispatchMouseScrollEvent(event);
        if (event.isCancel()) return;
        onMouseScrollEvent(event);
    }
    @Override
    public void dispatchUserInputTextEvent(UserInputTextEvent event) {
        super.dispatchUserInputTextEvent(event);
        if (event.isCancel()) return;
        onUserInputTextEvent(event);
    }
    @Override
    public void dispatchUserDropFilesEvent(UserDropFilesEvent event) {
        super.dispatchUserDropFilesEvent(event);
        if (event.isCancel()) return;
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
        if (event.isCancel()) return;
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
        setChildRootToThisRoot(child);
        super.addChild(child);
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

    public T setBackgroundBlurStrength(double strength) {
        this.backgroundBlurStrength = strength;
        return self;
    }
    public T setZIndex(int zIndex) {
        this.zIndex = zIndex;
        if (parent != null) {
            parent.sortChildren();
        }
        return self;
    }
    public T setAngle(float angle) {
        this.angle = angle;
        operationPosition();
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
        operationPosition();
        return self;
    }
    public T setYAnchor(double yAnchor) {
        this.yAnchor = yAnchor;
        operationPosition();
        return self;
    }
    public T setAnchor(double xAnchor, double yAnchor) {
        this.xAnchor = xAnchor;
        this.yAnchor = yAnchor;
        operationPosition();
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
    public int getZIndex() {
        return zIndex;
    }
    public double getBackgroundBlurStrength() {
        return backgroundBlurStrength;
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
        if (root == null) return false;

        double mouseX = root.getWindow().getMouseX();
        double mouseY = root.getWindow().getMouseY();

        if (this.realAngle == 0 || this.realAngle == 180) {
            return root.getWindow().isMouseInRange(realPositionX, realPositionY, realPositionMaxX, realPositionMaxY);
        }

        if (isPointNotInNodeRect(this, mouseX, mouseY)) {
            return false;
        }

        GuiNode<?> ancestor = parent instanceof GuiNode<?> parentGuiNode ? parentGuiNode : null;
        while (ancestor != null) {
            if (ancestor.clipChildren && isPointNotInNodeRect(ancestor, mouseX, mouseY)) {
                return false;
            }
            ancestor = ancestor.parent instanceof GuiNode<?> nextAncestor ? nextAncestor : null;
        }
        return true;
    }

    private static boolean isPointNotInNodeRect(GuiNode<?> node, double mouseX, double mouseY) {
        int centerX = (node.realPositionX + node.realPositionMaxX) / 2;
        int centerY = (node.realPositionY + node.realPositionMaxY) / 2;

        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double radians = Math.toRadians(-node.realAngle);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double localX = centerX + dx * cosine - dy * sine;
        double localY = centerY + dx * sine + dy * cosine;
        return !(localX >= node.realPositionX) || !(localX <= node.realPositionMaxX) || !(localY >= node.realPositionY) || !(localY <= node.realPositionMaxY);
    }

    public GuiNode(ScaleOffset position, ScaleOffset size) {
        this.position = position;
        this.size = size;
    }
}
