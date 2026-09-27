package top.untoldstudio.frostlumen.core.event;

public class MouseScrollEvent extends CancelableEvent {
    private final double x;
    private final double y;
    private final double xDelta;
    private final double yDelta;

    public MouseScrollEvent(double x, double y, double xDelta, double yDelta) {
        this.x = x;
        this.y = y;
        this.xDelta = xDelta;
        this.yDelta = yDelta;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getXDelta() {
        return xDelta;
    }
    public double getYDelta() {
        return yDelta;
    }
}
