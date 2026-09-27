package top.untoldstudio.frostlumen.core.event;

public abstract class CancelableEvent {
    private boolean cancel = false;

    public void cancel() {
        setCancel(true);
    }
    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
    public boolean isCancel() {
        return cancel;
    }
}
