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

import top.untold.frostlumen.core.data.ScaleOffset;
import top.untold.frostlumen.core.font.Font;
import top.untold.frostlumen.core.gui.ParentNode;
import top.untold.frostlumen.core.render.GuiRender;

public class TextLabel extends TextNode<TextLabel> implements HasDrawBackground<TextLabel> {
    private final TextRenderDescription renderDescription = new TextRenderDescription();
    private boolean drawBackground = false;

    @Override
    protected void init(GuiRender render) {
        renderDescription.init(render);
    }

    public TextRenderDescription description() {
        return renderDescription;
    }

    @Override
    protected void render(GuiRender render, double delta) {
        if (drawBackground) background.render(render);
        renderDescription.render(render);
    }
    @Override
    public TextLabel setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
        return this;
    }

    @Override
    public void operationPosition(ParentNode<?> parentFrame, int parentRealPositionX, int parentRealPositionY) {
        super.operationPosition(parentFrame, parentRealPositionX, parentRealPositionY);
        renderDescription.operationTextRenderPosition();
    }

    @Override
    public boolean isDrawBackground() {
        return drawBackground;
    }

    public TextLabel(String text, ScaleOffset position, ScaleOffset size) {
        this(text, null, position, size);
    }
    public TextLabel(String text, Font font, ScaleOffset position, ScaleOffset size) {
        super(position, size);
        this.renderDescription.setText(text).setFont(font);
    }
}
