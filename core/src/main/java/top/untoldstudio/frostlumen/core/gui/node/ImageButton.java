package top.untoldstudio.frostlumen.core.gui.node;

import top.untoldstudio.frostlumen.core.data.CursorShape;
import top.untoldstudio.frostlumen.core.data.MouseButton;
import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.render.GuiRender;
import top.untoldstudio.frostlumen.core.texture.Texture;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ImageButton extends ImageNode<ImageButton> {
    private final Set<MouseButton> canTriggerMouseButtons = new HashSet<>();
    private final ImageNode<ImageButton>.ImageRenderDescription normal;
    private final ImageNode<ImageButton>.ImageRenderDescription onHover = new ImageRenderDescription();
    private final ImageNode<ImageButton>.ImageRenderDescription onClick = new ImageRenderDescription();
    private boolean drawBackground = false;

    public ImageButton addCanTriggerMouseButton(MouseButton button) {
        canTriggerMouseButtons.add(button);
        return this;
    }
    public ImageButton removeCanTriggerMouseButton(MouseButton button) {
        canTriggerMouseButtons.remove(button);
        return this;
    }

    @Override
    protected void render(GuiRender render, long delta) {
        if (drawBackground) super.drawDefaultFrameBackground(render);
        if (!Collections.disjoint(canTriggerMouseButtons, currentMouseClickButtons)) {
            if (onClick.canRender()) {
                onClick.render(render);
            } else {
                normal.render(render);
            }
            render.setCursorShape(CursorShape.HAND);
        } else if (mouseInNode) {
            if (onHover.canRender()) {
                onHover.render(render);
            } else {
                normal.render(render);
            }
            render.setCursorShape(CursorShape.HAND);
        } else {
            normal.render(render);
        }
    }

    public boolean isDrawBackground() {
        return drawBackground;
    }
    public ImageNode<ImageButton>.ImageRenderDescription getNormal() {
        return normal;
    }
    public ImageNode<ImageButton>.ImageRenderDescription getOnHover() {
        return onHover;
    }
    public ImageNode<ImageButton>.ImageRenderDescription getOnClick() {
        return onClick;
    }

    public ImageButton setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
        return this;
    }

    public ImageButton(Texture normal, ScaleOffset position, ScaleOffset size) {
        super(position, size);
        this.normal = new ImageNode<ImageButton>.ImageRenderDescription().setTexture(normal);
        canTriggerMouseButtons.add(MouseButton.LEFT);
    }
}
