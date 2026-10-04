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
package top.untoldstudio.frostlumen.core.gui.node;

import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.gui.GuiNode;
import top.untoldstudio.frostlumen.core.gui.ParentNode;
import top.untoldstudio.frostlumen.core.render.GuiRender;
import top.untoldstudio.frostlumen.core.texture.Texture;

public class ImageLabel extends ImageNode<ImageLabel> {
    private final ImageRenderDescription imageRenderDescription;
    private boolean drawBackground = true;

    @Override
    public void render(GuiRender render, long delta) {
        if (drawBackground) super.drawDefaultFrameBackground(render);
        imageRenderDescription.render(render);
    }

    public ImageLabel setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
        return this;
    }

    public boolean isDrawBackground() {
        return drawBackground;
    }

    public ImageRenderDescription getImageRenderDescription() {
        return imageRenderDescription;
    }

    @Override
    public void operationPosition(ParentNode<?> parentFrame, int parentRealPositionX, int parentRealPositionY) {
        super.operationPosition(parentFrame, parentRealPositionX, parentRealPositionY);
        operationImageAlignment();
    }

    private void operationImageAlignment() {
        imageRenderDescription.operationImageAlignment();
    }

    public ImageLabel(Texture texture, ScaleOffset position, ScaleOffset size) {
        super(position, size);
        this.imageRenderDescription = new ImageRenderDescription().setTexture(texture);
    }
}
