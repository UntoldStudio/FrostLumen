package top.untoldstudio.frostlumen.core.event;

public class MouseMoveEvent extends CancelableEvent {
    private final double x;
    private final double y;
    private final double deltaX;
    private final double deltaY;

    public MouseMoveEvent(double x, double y, double deltaX, double deltaY) {
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getDeltaX() {
        return deltaX;
    }
    public double getDeltaY() {
        return deltaY;
    }
}
