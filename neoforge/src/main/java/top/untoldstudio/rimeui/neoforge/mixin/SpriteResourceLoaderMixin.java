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
package top.untoldstudio.rimeui.neoforge.mixin;

import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.untoldstudio.rimeui.neoforge.tool.SpriteRawCache;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Mixin(SpriteResourceLoader.class)
public interface SpriteResourceLoaderMixin {
    @Inject(method = "create", at = @At("RETURN"), cancellable = true)
    private static void captureRawPng(Set<MetadataSectionType<?>> meta, CallbackInfoReturnable<SpriteResourceLoader> callbackInfoReturnable) {
        SpriteResourceLoader original = callbackInfoReturnable.getReturnValue();

        SpriteResourceLoader wrapped = (id, resource, constructor) -> {
            try (InputStream in = resource.open()) {
                SpriteRawCache.put(id, in.readAllBytes());
            } catch (IOException ignored) {
            }
            return original.loadSprite(id, resource, constructor);
        };

        callbackInfoReturnable.setReturnValue(wrapped);
    }
}
