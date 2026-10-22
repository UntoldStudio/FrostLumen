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

import org.jetbrains.annotations.NotNull;
import top.untold.frostlumen.core.data.*;
import top.untold.frostlumen.core.event.*;
import top.untold.frostlumen.core.listener.TwoArgListenerRegistry;
import top.untold.frostlumen.core.render.GuiRender;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public abstract non-sealed class GuiNode<T extends GuiNode<T>> extends ParentNode<T> {
    protected final TwoArgListenerRegistry<T, MouseMoveEvent> mouseEnterListenerRegistry = new TwoArgListenerRegistry<>();
    protected final TwoArgListenerRegistry<T, MouseMoveEvent> mouseLeaveListenerRegistry = new TwoArgListenerRegistry<>();
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

    public T registerMouseEnterListener(BiConsumer<T, MouseMoveEvent> listener) {
        mouseEnterListenerRegistry.register(listener);
        return self;
    }

    public T unregisterMouseEnterListener(BiConsumer<T, MouseMoveEvent> listener) {
        mouseEnterListenerRegistry.unregister(listener);
        return self;
    }

    public T registerMouseLeaveListener(BiConsumer<T, MouseMoveEvent> listener) {
        mouseLeaveListenerRegistry.register(listener);
        return self;
    }

    public T unregisterMouseLeaveListener(BiConsumer<T, MouseMoveEvent> listener) {
        mouseLeaveListenerRegistry.unregister(listener);
        return self;
    }

    protected void operationPosition() {
        if (root == null || parent == null) return;

        int parentRealPositionX = parent.realPositionX;
        int parentRealPositionY = parent.realPositionY;

        realSizeX = size.getXPixelFromParentRealSize(parent);
        realSizeY = size.getYPixelFromParentRealSize(parent);

        realPositionX = parentRealPositionX + position.getXPixelFromParentRealSize(parent) - (int) Math.round(realSizeX * xAnchor);

        realPositionY = parentRealPositionY + position.getYPixelFromParentRealSize(parent) - (int) Math.round(realSizeY * yAnchor);

        realPositionMaxX = realPositionX + realSizeX;
        realPositionMaxY = realPositionY + realSizeY;

        realAngle = parent.realAngle + angle;

        onRelayout();

        letChildrenOperationPosition();

        mouseInNode = computeIsMouseInNode(root);
    }

    protected void letChildrenOperationPosition() {
        for (GuiNode<?> guiNode : children) {
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
        if (event.getAction() == InputAction.PRESS) {
            if (mouseInNode) {
                currentMouseClickButtons.add(event.getButton());
                if (event.getButton() == MouseButton.LEFT) {
                    setFocused(true);
                }
            } else {
                root.getWindow().cancelFocused(this);
            }
        }

        super.dispatchMouseButtonEvent(event);
        if (event.isCancel()) return;
        onMouseButtonEvent(event);

        if (event.getAction() == InputAction.RELEASE) {
            currentMouseClickButtons.remove(event.getButton());
        }
    }

    @Override
    public void dispatchMouseMoveEvent(MouseMoveEvent event) {
        boolean current = computeIsMouseInNode(root);

        if (current != mouseInNode) {
            if (current) {
                mouseEnterListenerRegistry.trigger(self, event);
            } else {
                mouseLeaveListenerRegistry.trigger(self, event);
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
    public void dispatchPreeditEvent(PreeditEvent event) {
        super.dispatchPreeditEvent(event);
        if (event.isCancel()) return;
        onPreeditEvent(event);
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

    @Override
    public void dispatchWindowFocusNodeChangeEvent(ParentNode<?> oldFocusNode, ParentNode<?> newFocusNode) {
        onWindowFocusNodeChangeEvent(oldFocusNode, newFocusNode);
        super.dispatchWindowFocusNodeChangeEvent(oldFocusNode, newFocusNode);
    }

    protected void dispatchInit(GuiRender render) {
        if (!init) {
            init = true;
            init(render);
        }
        forwardFor(node -> node.dispatchInit(render), null);
    }

    protected abstract void render(GuiRender render, double delta);

    protected void onKeyEvent(KeyEvent event) {
    }

    protected void onMouseButtonEvent(MouseButtonEvent event) {
    }

    protected void onMouseMoveEvent(MouseMoveEvent event) {
    }

    protected void onMouseEnterWindowEvent(MouseEnterWindowEvent event) {
    }

    protected void onMouseLeaveWindowEvent(MouseLeaveWindowEvent event) {
    }

    protected void onMouseScrollEvent(MouseScrollEvent event) {
    }

    protected void onUserInputTextEvent(UserInputTextEvent event) {
    }

    protected void onFrameBufferSizeChangeEvent(FrameBufferSizeChangeEvent event) {
    }

    protected void onUserRequestWindowCloseEvent(UserRequestWindowCloseEvent event) {
    }

    protected void onWindowCloseEvent(WindowCloseEvent event) {
    }

    protected void onWindowFocusChangeEvent(WindowFocusChangeEvent event) {
    }

    protected void onUserDropFilesEvent(UserDropFilesEvent event) {
    }

    protected void onPreeditEvent(PreeditEvent event) {
    }

    protected void onWindowMinimizeEvent(WindowMinimizeEvent event) {
    }

    protected void onWindowMaximizeEvent(WindowMaximizeEvent event) {
    }

    protected void onWindowMoveEvent(WindowMoveEvent event) {
    }

    protected void init(GuiRender render) {
    }

    protected void onRelayout() {
    }

    protected void tick() {
    }

    protected void onWindowFocusNodeChangeEvent(ParentNode<?> oldFocusNode, ParentNode<?> newFocusNode) {
    }

    @Override
    public T addChild(@NotNull GuiNode<?> child) {
        super.addChild(child);
        setChildRootToThisRoot(child);
        return self;
    }

    private void setChildRootToThisRoot(GuiNode<?> child) {
        if (child.root != null && child.root != this.root) {
            throw new IllegalStateException("Cannot move a node between different NodeRoots");
        }
        child.root = this.root;
        for (GuiNode<?> node : child.children) {
            setChildRootToThisRoot(node);
        }
        if (root != null) {
            child.operationPosition();
            child.dispatchInit(root.getRender());
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

    private boolean computeIsMouseInNode(NodeRoot root) {
        if (root == null) return false;

        double mouseX = root.getWindow().getMouseX();
        double mouseY = root.getWindow().getMouseY();

        if (isPointNotInRoundedRect(this, mouseX, mouseY)) {
            return false;
        }

        GuiNode<?> ancestor = parent instanceof GuiNode<?> parentGuiNode ? parentGuiNode : null;
        while (ancestor != null) {
            if (ancestor.clipChildren && isPointNotInRoundedRect(ancestor, mouseX, mouseY)) {
                return false;
            }
            ancestor = ancestor.parent instanceof GuiNode<?> nextAncestor ? nextAncestor : null;
        }
        return true;
    }

    private static boolean isPointNotInRoundedRect(GuiNode<?> node, double mouseX, double mouseY) {
        int centerX = (node.realPositionX + node.realPositionMaxX) / 2;
        int centerY = (node.realPositionY + node.realPositionMaxY) / 2;

        double deltaX = mouseX - centerX;
        double deltaY = mouseY - centerY;
        double radians = Math.toRadians(-node.realAngle);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);

        double localX = centerX + deltaX * cosine - deltaY * sine;
        double localY = centerY + deltaX * sine + deltaY * cosine;

        double left = node.realPositionX;
        double right = node.realPositionMaxX;
        double top = node.realPositionY;
        double bottom = node.realPositionMaxY;

        if (localX < left || localX > right || localY < top || localY > bottom) {
            return true;
        }

        int leftTopCornerRadius = node.background.getLeftTopCornerRadius();
        int rightTopCornerRadius = node.background.getRightTopCornerRadius();
        int leftBottomCornerRadius = node.background.getLeftBottomCornerRadius();
        int rightBottomCornerRadius = node.background.getRightBottomCornerRadius();

        boolean hasRoundedCorner = leftTopCornerRadius != 0 || rightTopCornerRadius != 0 || leftBottomCornerRadius != 0 || rightBottomCornerRadius != 0;
        if (!hasRoundedCorner) {
            return false;
        }

        double cornerCenterX = 0;
        double cornerCenterY = 0;
        int cornerRadius = 0;
        boolean inCornerRegion = false;

        if (localX < left + leftTopCornerRadius && localY < top + leftTopCornerRadius) {
            cornerCenterX = left + leftTopCornerRadius;
            cornerCenterY = top + leftTopCornerRadius;
            cornerRadius = leftTopCornerRadius;
            inCornerRegion = true;
        } else if (localX > right - rightTopCornerRadius && localY < top + rightTopCornerRadius) {
            cornerCenterX = right - rightTopCornerRadius;
            cornerCenterY = top + rightTopCornerRadius;
            cornerRadius = rightTopCornerRadius;
            inCornerRegion = true;
        } else if (localX < left + leftBottomCornerRadius && localY > bottom - leftBottomCornerRadius) {
            cornerCenterX = left + leftBottomCornerRadius;
            cornerCenterY = bottom - leftBottomCornerRadius;
            cornerRadius = leftBottomCornerRadius;
            inCornerRegion = true;
        } else if (localX > right - rightBottomCornerRadius && localY > bottom - rightBottomCornerRadius) {
            cornerCenterX = right - rightBottomCornerRadius;
            cornerCenterY = bottom - rightBottomCornerRadius;
            cornerRadius = rightBottomCornerRadius;
            inCornerRegion = true;
        }

        if (!inCornerRegion) {
            return false;
        }

        double distanceX = localX - cornerCenterX;
        double distanceY = localY - cornerCenterY;
        double distanceSquared = distanceX * distanceX + distanceY * distanceY;
        return distanceSquared > (double) cornerRadius * cornerRadius;
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
