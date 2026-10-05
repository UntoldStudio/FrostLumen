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

import top.untold.frostlumen.core.data.ImageAlignment;
import top.untold.frostlumen.core.data.RGBA;
import top.untold.frostlumen.core.data.ScaleOffset;
import top.untold.frostlumen.core.gui.GuiNode;
import top.untold.frostlumen.core.gui.ParentNode;
import top.untold.frostlumen.core.render.GuiRender;
import top.untold.frostlumen.core.render.ResourceManager;
import top.untold.frostlumen.core.texture.Texture;
import top.untold.frostlumen.core.tool.MathTool;

import java.util.HashSet;
import java.util.Set;

public abstract class ImageNode<T extends ImageNode<T>> extends GuiNode<T> {
    private final Set<ImageRenderDescription> selfRenderDescriptions = new HashSet<>();

    @Override
    protected void operationPosition(ParentNode<?> parentFrame, int parentRealPositionX, int parentRealPositionY) {
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
            render.enableScissor(realPositionX, realPositionY, realSizeX, realSizeY, realAngle, backgroundLeftTopCornerRadius, backgroundRightTopCornerRadius, backgroundLeftBottomCornerRadius, backgroundRightBottomCornerRadius);
            render.drawTexture(texture, imagePositionX, imagePositionY, imagePositionMaxX, imagePositionMaxY, realAngle, color.red(), color.green(), color.blue(), color.alpha());
            render.disableScissor();
        }

        /**
         * {@link ResourceManager :getResourceManagerFromThreadLocal()}
         * 如果你正在使用MC绑定你可以看看top.untoldstudio.frostlumen.neoforge.tool.MinecraftResourceAdapter
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
