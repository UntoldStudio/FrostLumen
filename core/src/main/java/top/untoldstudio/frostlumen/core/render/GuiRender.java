package top.untoldstudio.frostlumen.core.render;

import top.untoldstudio.frostlumen.core.data.ThicknessPosition;

public abstract class GuiRender {
    protected final long windowHandle;

    public void drawRectangle(int minX, int minY, int maxX, int maxY, int red, int green, int blue, int alpha) {
        drawRectangle(minX, minY, maxX, maxY, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha);
    }
    public void drawRectangle(int minX, int minY, int maxX, int maxY,
                              int aRed, int aGreen, int aBlue, int aAlpha,
                              int bRed, int bGreen, int bBlue, int bAlpha,
                              int cRed, int cGreen, int cBlue, int cAlpha,
                              int dRed, int dGreen, int dBlue, int dAlpha
                           ) {
        drawTriangle(minX, minY, maxX, minY, minX, maxY, aRed, aGreen, aBlue, aAlpha, bRed, bGreen, bBlue, bAlpha, cRed, cGreen, cBlue, cAlpha);
        drawTriangle(maxX, minY, minX, maxY, maxX, maxY, bRed, bGreen, bBlue, bAlpha, cRed, cGreen, cBlue, cAlpha, dRed, dGreen, dBlue, dAlpha);
    }
    public void drawShape(int minX, int minY, int maxX, int maxY, int red, int green, int blue, int alpha,
                          int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii,
                          int aBorderThickness, int bBorderThickness, int cBorderThickness, int dBorderThickness,
                          int borderRed, int borderGreen, int borderBlue, int borderAlpha,
                          ThicknessPosition thicknessPosition
                          ) {
        drawShape(minX, minY, maxX, maxY, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha,
                aCornerRadii, bCornerRadii, cCornerRadii, dCornerRadii, aBorderThickness, bBorderThickness, cBorderThickness, dBorderThickness,
                borderRed, borderGreen, borderBlue, borderAlpha, thicknessPosition
                );
    }

    public abstract void drawTriangle(int ax, int ay, int bx, int by, int cx, int cy,
                                      int aRed, int aGreen, int aBlue, int aAlpha,
                                      int bRed, int bGreen, int bBlue, int bAlpha,
                                      int cRed, int cGreen, int cBlue, int cAlpha);
    public abstract void drawShape(int minX, int minY, int maxX, int maxY,
                                   int aRed, int aGreen, int aBlue, int aAlpha, int bRed, int bGreen, int bBlue, int bAlpha,
                                   int cRed, int cGreen, int cBlue, int cAlpha, int dRed, int dGreen, int dBlue, int dAlpha,
                                   int aCornerRadii, int bCornerRadii, int cCornerRadii, int dCornerRadii,
                                   int aBorderThickness, int bBorderThickness, int cBorderThickness, int dBorderThickness,
                                   int borderRed, int borderGreen, int borderBlue, int borderAlpha,
                                   ThicknessPosition position
                                   );

    public abstract void submitBuffer();
    public void beginFrame() {
        begin();
    }
    public void endFrame() {
        end();
    }

    public abstract void enableScissor(int x, int y, int width, int height);
    public abstract void disableScissor();
    protected abstract void begin();
    public abstract void saveContext();
    protected abstract void end();
    public abstract void restoreContext();

    public void setExternalSettingCursor(long handle) {
    }

    public GuiRender(long windowHandle) {
        this.windowHandle = windowHandle;
    }
}
