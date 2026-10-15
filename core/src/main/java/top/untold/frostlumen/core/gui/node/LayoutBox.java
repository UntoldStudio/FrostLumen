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

import org.jetbrains.annotations.NotNull;
import top.untold.frostlumen.core.data.ScaleOffset;
import top.untold.frostlumen.core.gui.GuiNode;
import top.untold.frostlumen.core.gui.ParentNode;
import top.untold.frostlumen.core.render.GuiRender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class LayoutBox<T extends LayoutBox<T>> extends GuiNode<T> {
    protected List<GuiNode<?>> sortedNodeList = new ArrayList<>();
    protected int nodeStartSpacing = 0;
    protected int nodeSpacing = 0;
    protected abstract void sortNodes();

    private void sortList() {
        List<GuiNode<?>> nodes = new ArrayList<>(children);
        nodes.sort(Comparator.comparing(GuiNode<?>::getLayoutOrder));
        sortedNodeList = nodes;
        sortNodes();
    }

    @Override
    public T addChild(@NotNull GuiNode<?> child) {
        super.addChild(child);
        sortList();
        return self;
    }

    @Override
    public T removeChild(@NotNull GuiNode<?> child) {
        super.removeChild(child);
        sortNodes();
        return self;
    }

    @Override
    protected void operationPosition(ParentNode<?> parentFrame, int parentRealPositionX, int parentRealPositionY) {
        super.operationPosition(parentFrame, parentRealPositionX, parentRealPositionY);
        sortNodes();
    }

    public T setNodeStartSpacing(int nodeStartSpacing) {
        this.nodeStartSpacing = nodeStartSpacing;
        return self;
    }
    public T setNodeSpacing(int nodeSpacing) {
        this.nodeSpacing = nodeSpacing;
        return self;
    }

    public int getNodeStartSpacing() {
        return nodeStartSpacing;
    }
    public int getNodeSpacing() {
        return nodeSpacing;
    }

    @Override
    protected void render(GuiRender render, double delta) {
        background.render(render);
    }

    public LayoutBox(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
