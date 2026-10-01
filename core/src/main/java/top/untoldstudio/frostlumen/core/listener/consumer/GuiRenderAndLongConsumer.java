package top.untoldstudio.frostlumen.core.listener.consumer;

import top.untoldstudio.frostlumen.core.render.GuiRender;

@FunctionalInterface
public interface GuiRenderAndLongConsumer {
    void accept(GuiRender render, long value);
}
