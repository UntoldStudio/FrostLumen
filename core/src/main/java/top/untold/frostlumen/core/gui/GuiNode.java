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
import top.untold.frostlumen.core.tool.Maths;
import top.untold.frostlumen.core.render.GuiRender;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract non-sealed class GuiNode<T extends GuiNode<T>> extends ParentNode<T> {
    protected final OneArgListenerRegistry<MouseMoveEvent> mouseEnterListenerRegistry = new OneArgListenerRegistry<>();
    protected final OneArgListenerRegistry<MouseMoveEvent> mouseLeaveListenerRegistry = new OneArgListenerRegistry<>();
    protected final Background background = new Background();
    protected NodeRoot root;
    protected ParentNode<?> parent;
    protected int zIndex = 0;
    protected int layoutOrder = 0;
    protected boolean clipChildren = false;
    protected double xAnchor = 0;
    protected double yAnchor = 0;
    protected float angle = 0;
    protected Set<MouseButton> currentMouseClickButtons = new HashSet<>();
    protected boolean mouseInNode;
    protected boolean init = false;

    public Background background() {
        return background;
    }

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

    protected void operationPosition(){
        if (getParent() != null && getParent() instanceof ParentNode<?> node){
            operationPosition(node, node.getRealPositionX(), node.getRealPositionY());
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

            realSizeX = Maths.round(windowWidth * size.xScale()) + size.xOffset();
            realSizeY = Maths.round(windowHeight * size.yScale()) + size.yOffset();

            realPositionX = Maths.round(windowWidth * position.xScale())
                    + position.xOffset()
                    - (int) Math.round(realSizeX * xAnchor);

            realPositionY = Maths.round(windowHeight * position.yScale())
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

    public T setFocused(boolean focused) {
        if (root == null) return self;
        if (focused) {
            root.getWindow().setFocusNode(this);
        } else {
            root.getWindow().setFocusNode(null);
        }
        return self;
    }
    public boolean isFocused() {
        if (root == null) return false;
        return root.getWindow().getFocusNode() == this;
    }

    @Override
    public void dispatchRender(GuiRender render, double delta) {
        if (clipChildren) {
            render.enableScissor(realPositionX, realPositionY, realSizeX, realSizeY, realAngle, background.leftTopCornerRadius, background.rightTopCornerRadius, background.leftBottomCornerRadius, background.rightBottomCornerRadius);
            render(render, delta);
            super.dispatchRender(render, delta);
            render.disableScissor();
        } else {
            render(render, delta);
            super.dispatchRender(render, delta);
        }
    }
    @Override
    public void dispatchTickEvent() {
        tick();
        super.dispatchTickEvent();
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
            if (event.getButton() == MouseButton.LEFT) {
                root.getWindow().setFocusNode(this);
            }
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
    protected void dispatchInit(GuiRender render) {
        if (!init) {
            init = true;
            init(render);
        }
        forwardFor(node -> node.dispatchInit(render), null);
    }

    protected abstract void render(GuiRender render, double delta);
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
    protected void init(GuiRender render) {}
    protected void tick() {}

    @Override
    public T addChild(GuiNode<?> child) {
        setChildRootToThisRoot(child);
        super.addChild(child);
        return self;
    }

    private void setChildRootToThisRoot(GuiNode<?> child) {
        child.root = this.root;
        if (root != null) {
            child.dispatchInit(root.getRender());
        }
        for (GuiNode<?> node : child.children) {
            setChildRootToThisRoot(node);
        }
    }

    public T setLayoutOrder(int layoutOrder) {
        this.layoutOrder = layoutOrder;
        return self;
    }
    public T setParent(ParentNode<?> parent) {
        parent.addChild(this);
        return self;
    }
    public ParentNode<?> getParent() {
        return parent;
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
    public T setClipChildren(boolean clipChildren) {
        this.clipChildren = clipChildren;
        return self;
    }

    public boolean isClipChildren() {
        return clipChildren;
    }
    public int getLayoutOrder() {
        return layoutOrder;
    }
    public int getZIndex() {
        return zIndex;
    }
    public float getAngle() {
        return angle;
    }
    public double getXAnchor() {
        return xAnchor;
    }
    public double getYAnchor() {
        return yAnchor;
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
            if (!root.getWindow().isMouseInRange(realPositionX, realPositionY, realPositionMaxX, realPositionMaxY)) {
                return false;
            }
        } else if (isPointNotInNodeRect(this, mouseX, mouseY)) {
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

    public class Background {
        protected RGBA color = RGBA.WHITE;
        protected RGBA borderColor = RGBA.BLACK;
        protected int leftBorderThickness = 0;
        protected int rightBorderThickness = 0;
        protected int topBorderThickness = 0;
        protected int bottomBorderThickness = 0;
        protected int leftTopCornerRadius = 0;
        protected int rightTopCornerRadius = 0;
        protected int leftBottomCornerRadius = 0;
        protected int rightBottomCornerRadius = 0;
        protected ThicknessPosition borderThicknessPosition = ThicknessPosition.OUTSIDE;
        protected double blurStrength = 0;

        public void render(GuiRender render) {
            if (blurStrength != 0 && color.alpha() != 255) {
                boolean hasRoundedCorner = leftTopCornerRadius != 0 || rightTopCornerRadius != 0 || leftBottomCornerRadius != 0 || rightBottomCornerRadius != 0;
                if (hasRoundedCorner) {
                    render.enableScissor(realPositionX, realPositionY, realSizeX, realSizeY, realAngle, leftTopCornerRadius, rightTopCornerRadius, leftBottomCornerRadius, rightBottomCornerRadius);
                    render.blurRegion(realPositionX, realPositionY, realSizeX, realSizeY, realAngle, blurStrength);
                    render.disableScissor();
                } else {
                    render.blurRegion(realPositionX, realPositionY, realSizeX, realSizeY, realAngle, blurStrength);
                }
            }

            if (leftBorderThickness == 0 && rightBorderThickness == 0 &&
                    topBorderThickness == 0 && bottomBorderThickness == 0 &&
                    leftTopCornerRadius == 0 && rightTopCornerRadius == 0 &&
                    leftBottomCornerRadius == 0 && rightBottomCornerRadius == 0
            ) {
                render.drawRectangle(realPositionX, realPositionY, realPositionMaxX, realPositionMaxY, realAngle, color.red(), color.green(), color.blue(), color.alpha());
            } else {
                render.drawShape(realPositionX, realPositionY, realPositionMaxX, realPositionMaxY, realAngle,
                        color.red(), color.green(), color.blue(), color.alpha(),
                        leftTopCornerRadius, rightTopCornerRadius,
                        leftBottomCornerRadius, rightBottomCornerRadius,
                        leftBorderThickness, rightBorderThickness,
                        topBorderThickness, bottomBorderThickness,
                        borderColor.red(), borderColor.green(),
                        borderColor.blue(), borderColor.alpha(),
                        borderThicknessPosition);
            }
        }

        public Background setColor(RGBA color) {
            this.color = color;
            return this;
        }
        public Background setBorderColor(RGBA borderColor) {
            this.borderColor = borderColor;
            return this;
        }
        public Background setLeftBorderThickness(int leftBorderThickness) {
            this.leftBorderThickness = leftBorderThickness;
            return this;
        }
        public Background setRightBorderThickness(int rightBorderThickness) {
            this.rightBorderThickness = rightBorderThickness;
            return this;
        }
        public Background setTopBorderThickness(int topBorderThickness) {
            this.topBorderThickness = topBorderThickness;
            return this;
        }
        public Background setBottomBorderThickness(int bottomBorderThickness) {
            this.bottomBorderThickness = bottomBorderThickness;
            return this;
        }
        public Background setBorderThickness(int borderThickness) {
            this.leftBorderThickness = borderThickness;
            this.rightBorderThickness = borderThickness;
            this.topBorderThickness = borderThickness;
            this.bottomBorderThickness = borderThickness;
            return this;
        }

        public Background setLeftTopCornerRadius(int leftTopCornerRadius) {
            this.leftTopCornerRadius = leftTopCornerRadius;
            return this;
        }
        public Background setRightTopCornerRadius(int rightTopCornerRadius) {
            this.rightTopCornerRadius = rightTopCornerRadius;
            return this;
        }
        public Background setLeftBottomCornerRadius(int leftBottomCornerRadius) {
            this.leftBottomCornerRadius = leftBottomCornerRadius;
            return this;
        }
        public Background setRightBottomCornerRadius(int rightBottomCornerRadius) {
            this.rightBottomCornerRadius = rightBottomCornerRadius;
            return this;
        }
        public Background setCornerRadius(int cornerRadius) {
            this.leftTopCornerRadius = cornerRadius;
            this.rightTopCornerRadius = cornerRadius;
            this.leftBottomCornerRadius = cornerRadius;
            this.rightBottomCornerRadius = cornerRadius;
            return this;
        }

        public Background setBorderThicknessPosition(ThicknessPosition borderThicknessPosition) {
            this.borderThicknessPosition = borderThicknessPosition;
            return this;
        }
        public Background setBlurStrength(double blurStrength) {
            this.blurStrength = blurStrength;
            return this;
        }

        public RGBA getColor() {
            return color;
        }
        public RGBA getBorderColor() {
            return borderColor;
        }
        public int getLeftBorderThickness() {
            return leftBorderThickness;
        }
        public int getRightBorderThickness() {
            return rightBorderThickness;
        }
        public int getTopBorderThickness() {
            return topBorderThickness;
        }
        public int getBottomBorderThickness() {
            return bottomBorderThickness;
        }
        public int getLeftTopCornerRadius() {
            return leftTopCornerRadius;
        }
        public int getRightTopCornerRadius() {
            return rightTopCornerRadius;
        }
        public int getLeftBottomCornerRadius() {
            return leftBottomCornerRadius;
        }
        public int getRightBottomCornerRadius() {
            return rightBottomCornerRadius;
        }
        public ThicknessPosition getBorderThicknessPosition() {
            return borderThicknessPosition;
        }
        public double getBlurStrength() {
            return blurStrength;
        }

        public T getNode() {
            return self;
        }
    }
}
