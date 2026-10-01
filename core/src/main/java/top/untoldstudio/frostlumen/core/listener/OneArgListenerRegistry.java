package top.untoldstudio.frostlumen.core.listener;

import java.util.function.Consumer;

public class OneArgListenerRegistry<T> extends AbstractListenerRegistry<Consumer<T>> {
    public void trigger(T value) {
        forListeners(listener -> listener.accept(value));
    }
}
