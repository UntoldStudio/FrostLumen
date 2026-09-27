package top.untoldstudio.frostlumen.core.event;

import top.untoldstudio.frostlumen.core.data.InputAction;
import top.untoldstudio.frostlumen.core.data.InputModifiers;
import top.untoldstudio.frostlumen.core.data.Key;

public class KeyEvent extends CancelableEvent {
    private final Key key;
    private final InputAction inputAction;
    private final InputModifiers inputModifiers;

    public KeyEvent(Key key, InputAction action, InputModifiers modifiers) {
        this.key = key;
        this.inputAction = action;
        this.inputModifiers = modifiers;
    }

    public Key getKey() {
        return key;
    }
    public InputAction getInputAction() {
        return inputAction;
    }
    public InputModifiers getInputModifiers() {
        return inputModifiers;
    }
}
