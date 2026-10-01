package top.untoldstudio.frostlumen.core.listener;

public class ParameterlessListenerRegistry extends AbstractListenerRegistry<Runnable> {
    public void trigger() {
        forListeners(Runnable::run);
    }
}
