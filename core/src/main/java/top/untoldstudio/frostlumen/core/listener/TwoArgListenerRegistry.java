package top.untoldstudio.frostlumen.core.listener;

import java.util.function.BiConsumer;

public class TwoArgListenerRegistry<T, U> extends AbstractListenerRegistry<BiConsumer<T, U>> {
    public void trigger(T value1, U value2) {
        forListeners(listener -> listener.accept(value1, value2));
    }
}
