package top.untoldstudio.frostlumen.core.listener;

import top.untoldstudio.frostlumen.core.listener.consumer.GuiRenderAndLongConsumer;
import top.untoldstudio.frostlumen.core.render.GuiRender;

public class GuiRenderAndLongListenerRegistry extends AbstractListenerRegistry<GuiRenderAndLongConsumer> {
    public void trigger(GuiRender render, long value) {
        forListeners(consumer -> consumer.accept(render, value));
    }
}
