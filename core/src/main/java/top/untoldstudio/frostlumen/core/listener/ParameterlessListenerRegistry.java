package top.untoldstudio.frostlumen.core.listener;

import java.util.ArrayList;
import java.util.List;

public class ParameterlessListenerRegistry extends ListenerRegistry {
    private final List<Runnable> listeners = new ArrayList<>();

    public void register(Runnable listener) {
        listeners.add(listener);
    }
    public void unregister(Runnable listener) {
        listeners.remove(listener);
    }

    public void trigger() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}
