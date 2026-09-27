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
package top.untoldstudio.rimeui.neoforge;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.untoldstudio.frostlumen.core.data.RGBA;
import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.data.ThicknessPosition;
import top.untoldstudio.frostlumen.core.gui.Window;
import top.untoldstudio.frostlumen.core.gui.node.Frame;
import top.untoldstudio.frostlumen.core.render.RenderProviderType;

public class ModEventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientSetup(FMLClientSetupEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            Window window = Window.from(minecraft.getWindow().handle(), RenderProviderType.OPENGL);
            Frame frame = new Frame(ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5))
                    .setAnchor(0.5, 0.5).setBackgroundColor(RGBA.BLUE)//.setBackgroundCornerRadius(100)
                    .setBackgroundBorderThickness(10).setBackgroundBorderThicknessPosition(ThicknessPosition.INSIDE).setBackgroundCornerRadius(50).setBackgroundBorderColor(RGBA.RED.withAlpha(100))
                    ;
            window.getNodeRoot().addChildren(frame);
        });
    }
}
