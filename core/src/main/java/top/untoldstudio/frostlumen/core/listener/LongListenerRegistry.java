package top.untoldstudio.frostlumen.core.listener;

import java.util.function.LongConsumer;

public class LongListenerRegistry extends AbstractListenerRegistry<LongConsumer> {
    public void trigger(long value) {
        forListeners(consumer -> consumer.accept(value));
    }
}
