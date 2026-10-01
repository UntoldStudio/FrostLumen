package top.untoldstudio.frostlumen.core.tool;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class LruCacheMap<K, V> extends LinkedHashMap<K, V> {
    private BiConsumer<K, V> onAutoRemove;
    private int maxSize;

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        if (size() > maxSize) {
            if (onAutoRemove != null) onAutoRemove.accept(eldest.getKey(), eldest.getValue());
            return true;
        }
        return false;
    }

    public int getMaxSize() {
        return maxSize;
    }
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
    public void setOnAutoRemove(BiConsumer<K, V> onAutoRemove) {
        this.onAutoRemove = onAutoRemove;
    }

    public LruCacheMap(int maxSize, BiConsumer<K, V> onRemove) {
        this(16, maxSize, onRemove);
    }
    public LruCacheMap(int maxSize) {
        this(16, maxSize, null);
    }
    public LruCacheMap(int initialCapacity, int maxSize, BiConsumer<K, V> onRemove) {
        super(initialCapacity, 0.75f, true);
        this.maxSize = maxSize;
        this.onAutoRemove = onRemove;
    }
}
