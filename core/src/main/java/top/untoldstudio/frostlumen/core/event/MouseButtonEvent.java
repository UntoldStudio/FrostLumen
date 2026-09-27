package top.untoldstudio.frostlumen.core.event;

import top.untoldstudio.frostlumen.core.data.InputAction;
import top.untoldstudio.frostlumen.core.data.InputModifiers;
import top.untoldstudio.frostlumen.core.data.MouseButton;

public class MouseButtonEvent extends CancelableEvent {
    private final MouseButton button;
    private final InputAction action;
    private final InputModifiers modifiers;

    public MouseButtonEvent(MouseButton button, InputAction action, InputModifiers modifiers) {
        this.button = button;
        this.action = action;
        this.modifiers = modifiers;
    }

    public MouseButton getButton() {
        return button;
    }
    public InputAction getAction() {
        return action;
    }
    public InputModifiers getModifiers() {
        return modifiers;
    }
}
