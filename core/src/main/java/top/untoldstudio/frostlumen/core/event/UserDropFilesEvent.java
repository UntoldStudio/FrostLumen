package top.untoldstudio.frostlumen.core.event;

import java.nio.file.Path;

public class UserDropFilesEvent extends CancelableEvent {
    private final Path[] filePaths;

    public UserDropFilesEvent(Path[] filePaths) {
        this.filePaths = filePaths;
    }

    public Path[] getFilePaths() {
        return filePaths;
    }
}
