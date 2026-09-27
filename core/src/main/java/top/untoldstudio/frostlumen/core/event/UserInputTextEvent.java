package top.untoldstudio.frostlumen.core.event;

public class UserInputTextEvent extends CancelableEvent {
    private final String text;

    public UserInputTextEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
