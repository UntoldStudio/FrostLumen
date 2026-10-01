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
import top.untoldstudio.frostlumen.core.gui.NodeRoot;
import top.untoldstudio.frostlumen.core.gui.Window;
import top.untoldstudio.frostlumen.core.listener.OneArgListenerRegistry;
import top.untoldstudio.frostlumen.core.render.RenderProviderType;

import java.util.function.Consumer;

public class FrostLumenModBootstrap {
    public static final FrostLumenModBootstrap INSTANCE = new FrostLumenModBootstrap();
    private final OneArgListenerRegistry<NodeRoot> bootstrapRegistry = new OneArgListenerRegistry<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientSetup(FMLClientSetupEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            Window window = Window.from(minecraft.getWindow().handle(), RenderProviderType.OPENGL);
            NodeRoot root = window.getNodeRoot();
            bootstrapRegistry.trigger(root);
        });
    }

    public void registerBootstrapConsumer(Consumer<NodeRoot> consumer) {
        bootstrapRegistry.register(consumer);
    }
    public void unregisterBootstrapConsumer(Consumer<NodeRoot> consumer) {
        bootstrapRegistry.unregister(consumer);
    }

    private FrostLumenModBootstrap() {
    }
}
