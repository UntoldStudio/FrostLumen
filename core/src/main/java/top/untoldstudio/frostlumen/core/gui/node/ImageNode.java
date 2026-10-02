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

import top.untoldstudio.frostlumen.core.data.ImageAlignment;
import top.untoldstudio.frostlumen.core.data.RGBA;
import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.gui.GuiNode;
import top.untoldstudio.frostlumen.core.render.GuiRender;
import top.untoldstudio.frostlumen.core.texture.Texture;
import top.untoldstudio.frostlumen.core.tool.MathTool;

import java.util.HashSet;
import java.util.Set;

public abstract class ImageNode<T extends ImageNode<T>> extends GuiNode<T> {
    private final Set<ImageRenderDescription> selfRenderDescriptions = new HashSet<>();

    @Override
    protected void operationPosition(GuiNode<?> parentFrame, int parentRealPositionX, int parentRealPositionY) {
        super.operationPosition(parentFrame, parentRealPositionX, parentRealPositionY);
        for (ImageRenderDescription renderDescription : selfRenderDescriptions) {
            renderDescription.operationImageAlignment();
        }
    }

    public class ImageRenderDescription {
        private Texture texture;
        private RGBA color = RGBA.WHITE;
        private ImageAlignment imageAlignment = ImageAlignment.STRETCH;
        private int imagePositionX;
        private int imagePositionY;
        private int imagePositionMaxX;
        private int imagePositionMaxY;

        public boolean canRender() {
            return texture != null;
        }

        public void render(GuiRender render) {
            render.enableScissor(realPositionX, realPositionY, realSizeX, realSizeY, angle);
            render.drawTexture(texture, imagePositionX, imagePositionY, imagePositionMaxX, imagePositionMaxY, angle, color.red(), color.green(), color.blue(), color.alpha());
            render.disableScissor();
        }

        /**
         * {@link top.untoldstudio.frostlumen.core.render.IResourceManager:getIResourceManagerFromThreadLocal()}
         */
        public ImageRenderDescription setTexture(Texture texture) {
            this.texture = texture;
            return this;
        }
        public ImageRenderDescription setColor(RGBA color) {
            this.color = color;
            return this;
        }
        public ImageRenderDescription setImageAlignment(ImageAlignment imageAlignment) {
            this.imageAlignment = imageAlignment;
            operationImageAlignment();
            return this;
        }

        public Texture getTexture() {
            return texture;
        }
        public RGBA getColor() {
            return color;
        }
        public ImageAlignment getImageAlignment() {
            return imageAlignment;
        }

        public T getNode() {
            return self;
        }

        public void operationImageAlignment() {
            if (texture == null) return;
            switch (imageAlignment) {
                case STRETCH -> {
                    imagePositionX = realPositionX;
                    imagePositionY = realPositionY;
                    imagePositionMaxX = realPositionMaxX;
                    imagePositionMaxY = realPositionMaxY;
                }
                case FIT -> {
                    int width = texture.width();
                    int height = texture.height();

                    double scaleX = (double) realSizeX / width;
                    double scaleY = (double) realSizeY / height;
                    double scale = Math.min(scaleX, scaleY);

                    int newWidth = MathTool.round(width * scale);
                    int newHeight = MathTool.round(height * scale);

                    imagePositionX = realPositionX + (realSizeX - newWidth) / 2;
                    imagePositionY = realPositionY + (realSizeY - newHeight) / 2;
                    imagePositionMaxX = imagePositionX + newWidth;
                    imagePositionMaxY = imagePositionY + newHeight;
                }
                case FILL -> {
                    int width = texture.width();
                    int height = texture.height();

                    double scaleX = (double) realSizeX / width;
                    double scaleY = (double) realSizeY / height;
                    double scale = Math.max(scaleX, scaleY);

                    int newWidth = MathTool.round(width * scale);
                    int newHeight = MathTool.round(height * scale);

                    imagePositionX = realPositionX + (realSizeX - newWidth) / 2;
                    imagePositionY = realPositionY + (realSizeY - newHeight) / 2;
                    imagePositionMaxX = imagePositionX + newWidth;
                    imagePositionMaxY = imagePositionY + newHeight;
                }
            }
        }

        public ImageRenderDescription() {
            selfRenderDescriptions.add(this);
        }
    }

    public ImageNode(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
