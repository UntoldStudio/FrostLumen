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

import top.untold.frostlumen.core.data.NiceSliceType;
import top.untold.frostlumen.core.data.RGBA;
import top.untold.frostlumen.core.data.ScaleOffset;
import top.untold.frostlumen.core.event.MouseScrollEvent;
import top.untold.frostlumen.core.gui.GuiNode;
import top.untold.frostlumen.core.gui.ParentNode;
import top.untold.frostlumen.core.render.GuiRender;
import top.untold.frostlumen.core.texture.Texture;
import top.untold.frostlumen.core.tool.Maths;

public class ScrollFrame extends GuiNode<ScrollFrame> {
    private RGBA scrollBarColor = RGBA.WHITE;
    private boolean canScroll = true;
    private boolean scrollBarVisible = true;
    private double scrollScale = 2;
    private double scrollProgress = 1;
    private double scrollSpeed = 1;
    private int scrollBarXPosition;
    private int scrollBarYPosition;
    private int scrollBarWidth = 6;
    private int scrollBarYSize;
    private Texture scrollBarTexture;

    @Override
    protected void init(GuiRender render) {
        scrollBarTexture = render.loadNiceSliceTexture("/texture/default_scroll_bar.png", NiceSliceType.PROPORTIONAL, true, true, 0, 0, 20, 20);
    }

    @Override
    protected void operationPosition(ParentNode<?> parentFrame, int parentRealPositionX, int parentRealPositionY) {
        super.operationPosition(parentFrame, parentRealPositionX, parentRealPositionY);
        setScrollBarProcessState();
    }

    private void setScrollBarProcessState() {
        double yScale = 1 / scrollScale;
        scrollBarYSize = Maths.round(realSizeY * yScale);
        scrollBarXPosition = realPositionMaxX - scrollBarWidth;
        scrollBarYPosition = Maths.round((realSizeY - scrollBarYSize) * (1 - scrollProgress));
    }

    @Override
    public int getRealPositionY() {
        int base = super.getRealPositionY();
        return base - (int) ((int) ((scrollScale - 1) * realSizeY) * (1 - scrollProgress));
    }

    @Override
    protected void onMouseScrollEvent(MouseScrollEvent event) {
        if (canScroll && mouseInNode) {
            double value = event.getYDelta() / 5 * scrollSpeed / scrollScale;
            double tempProcess = scrollProgress + value;
            scrollProgress = Math.clamp(tempProcess, 0, 1);
            event.cancel();
            setScrollBarProcessState();
        }
    }

    @Override
    protected void render(GuiRender render, double delta) {
        background.render(render);
        if (scrollBarVisible && scrollBarTexture != null) {
            render.drawTexture(scrollBarTexture, scrollBarXPosition, scrollBarYPosition + realPositionY, scrollBarXPosition + scrollBarWidth, scrollBarYPosition + scrollBarYSize + realPositionY, realAngle, scrollBarColor.red(), scrollBarColor.green(), scrollBarColor.blue(), scrollBarColor.alpha());
        }
    }

    public ScrollFrame setScrollBarColor(RGBA scrollBarColor) {
        this.scrollBarColor = scrollBarColor;
        return this;
    }
    public ScrollFrame setScrollBarVisible(boolean scrollBarVisible) {
        this.scrollBarVisible = scrollBarVisible;
        return this;
    }
    public ScrollFrame setScrollBarWidth(int scrollBarWidth) {
        this.scrollBarWidth = scrollBarWidth;
        return self;
    }
    public ScrollFrame setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
        return self;
    }
    public ScrollFrame setScrollScale(double scrollScale) {
        this.scrollScale = scrollScale;
        return self;
    }
    public ScrollFrame setScrollProgress(double scrollProgress) {
        this.scrollProgress = scrollProgress;
        return self;
    }
    public ScrollFrame setScrollSpeed(int scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
        return self;
    }

    public RGBA getScrollBarColor() {
        return scrollBarColor;
    }
    public boolean isScrollBarVisible() {
        return scrollBarVisible;
    }
    public int getScrollBarWidth() {
        return scrollBarWidth;
    }
    public boolean isCanScroll() {
        return canScroll;
    }
    public double getScrollScale() {
        return scrollScale;
    }
    public double getScrollProgress() {
        return scrollProgress;
    }
    public double getScrollSpeed() {
        return scrollSpeed;
    }

    public ScrollFrame(ScaleOffset position, ScaleOffset size) {
        super(position, size);
        clipChildren = true;
    }
}
