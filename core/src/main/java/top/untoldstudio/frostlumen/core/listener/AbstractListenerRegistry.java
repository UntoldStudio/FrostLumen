package top.untoldstudio.frostlumen.core.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractListenerRegistry<T> {
    protected final List<T> listeners = new ArrayList<>();

    public void register(T listener){
        listeners.add(listener);
    }
    public void unregister(T listener) {
        listeners.remove(listener);
    }

    protected void forListeners(Consumer<T> consumer){
        for (T listener : listeners) {
            consumer.accept(listener);
        }
    }
}
