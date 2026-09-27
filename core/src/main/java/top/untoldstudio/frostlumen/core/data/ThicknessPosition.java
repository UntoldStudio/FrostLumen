package top.untoldstudio.frostlumen.core.data;

public enum ThicknessPosition {
    INSIDE(0),
    CENTER(1),
    OUTSIDE(2);

    private final byte order;

    ThicknessPosition(int order) {
        this.order = (byte)order;
    }

    public byte getOrder() {
        return order;
    }
}
