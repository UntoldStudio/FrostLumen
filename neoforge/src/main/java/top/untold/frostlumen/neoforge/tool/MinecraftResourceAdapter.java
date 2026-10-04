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
package top.untold.frostlumen.neoforge.tool;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import top.untold.frostlumen.core.data.NiceSliceType;
import top.untold.frostlumen.core.exception.ResourceException;
import top.untold.frostlumen.core.render.ResourceManager;
import top.untold.frostlumen.core.texture.Texture;

import java.util.Optional;

public class MinecraftResourceAdapter {
    public static Texture loadItemTexture(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        ResourceLocation itemSprite = ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "item/" + itemId.getPath());
        byte[] data = SpriteRawCache.get(itemSprite);

        if (data == null) {
            ResourceLocation blockSprite = ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "block/" + itemId.getPath());
            data = SpriteRawCache.get(blockSprite);
        }

        if (data == null) {
            throw new ResourceException("Cannot load texture from resource item:" + item);
        }

        return ResourceManager.getIResourceManagerFromThreadLocal().loadTexture(data, false);
    }
    public static Texture loadTextureFromResourceLocation(ResourceLocation location) {
        boolean isNiceSlice = false;
        int left = -1;
        int right = -1;
        int top = -1;
        int bottom = -1;
        boolean stretchInner = false;

        notNiceSlice: {
            TextureAtlas atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI);
            TextureAtlasSprite sprite = atlas.getSprite(location);
            SpriteContents contents = sprite.contents();

            Optional<GuiMetadataSection> guiMetadataSection = contents.getAdditionalMetadata(GuiMetadataSection.TYPE);
            if (guiMetadataSection.isEmpty()) break notNiceSlice;
            GuiSpriteScaling scaling = guiMetadataSection.get().scaling();
            if (!(scaling instanceof GuiSpriteScaling.NineSlice(int width, int height, GuiSpriteScaling.NineSlice.Border border, boolean inner))) {
                break notNiceSlice;
            }

            left = border.left();
            right = border.right();
            top = border.top();
            bottom = border.bottom();
            stretchInner = inner;
            isNiceSlice = true;
        }

        byte[] data = SpriteRawCache.get(location);

        if (data == null) {
            throw new ResourceException("Cannot load texture from resource location:" + location);
        }

        if (isNiceSlice) {
            return ResourceManager.getIResourceManagerFromThreadLocal().loadNiceSliceTexture(data, NiceSliceType.FIXED_BORDER, stretchInner, false, left, right, top, bottom);
        } else {
            return ResourceManager.getIResourceManagerFromThreadLocal().loadTexture(data, false);
        }
    }
}
