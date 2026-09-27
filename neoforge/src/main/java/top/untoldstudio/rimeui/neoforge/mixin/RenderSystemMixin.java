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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.untoldstudio.frostlumen.core.gui.NodeRoot;
import top.untoldstudio.frostlumen.core.gui.Window;
import top.untoldstudio.frostlumen.core.render.GuiRender;

@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {
    @WrapOperation(
            method = "flipFrame",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V")
    )
    private static void beforeSwap(long windowHandle, Operation<Void> original) {
        if (Window.get(windowHandle) != null) {
            NodeRoot root = Window.get(windowHandle).getNodeRoot();
            GuiRender render = root.getRender();
            render.saveContext();
            root.render();
            render.restoreContext();
        }
        original.call(windowHandle);
    }
}