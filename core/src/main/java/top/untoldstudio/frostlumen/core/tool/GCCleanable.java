package top.untoldstudio.frostlumen.core.tool;

import java.lang.ref.Cleaner;

public abstract class GCCleanable {
    private static final Cleaner CLEANER = Cleaner.create();

    protected GCCleanable(Runnable onGCRecycleCallback) {
        CLEANER.register(this, onGCRecycleCallback);
    }
}
