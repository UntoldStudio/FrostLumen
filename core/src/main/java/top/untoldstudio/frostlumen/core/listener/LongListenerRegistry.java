package top.untoldstudio.frostlumen.core.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

public class LongListenerRegistry {
    private final List<LongConsumer> listeners = new ArrayList<>();

    public void register(LongConsumer listener) {
        listeners.add(listener);
    }
    public void unregister(LongConsumer listener) {
        listeners.remove(listener);
    }

    public void trigger(long value) {
        for (LongConsumer listener : listeners) {
            listener.accept(value);
        }
    }
}
