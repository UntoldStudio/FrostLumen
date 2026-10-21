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
import top.untold.frostlumen.core.render.GuiRender;
import top.untold.frostlumen.core.texture.Texture;

// --8<-- [start:classDefinition]
public class ImageLabel extends ImageNode<ImageLabel> implements HasDrawBackground<ImageLabel> {
    private final ImageRenderDescription imageRenderDescription;
    private boolean drawBackground = true;

    @Override
    public void render(GuiRender render, double delta) {
        if (drawBackground) background.render(render);
        imageRenderDescription.render(render);
    }

    @Override
    public ImageLabel setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
        return this;
    }
    @Override
    public boolean isDrawBackground() {
        return drawBackground;
    }

    public ImageRenderDescription getImageRenderDescription() {
        return imageRenderDescription;
    }

    @Override
    public void onRelayout() {
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
// --8<-- [end:classDefinition]