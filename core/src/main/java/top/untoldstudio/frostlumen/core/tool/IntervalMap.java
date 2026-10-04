/*
 * Copyright 2026 Untold Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.untoldstudio.frostlumen.core.tool;

import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

public final class IntervalMap<V> {

    public record Range<V>(double start, double end, V value) {}

    private final Double2ObjectRBTreeMap<Range<V>> map = new Double2ObjectRBTreeMap<>();

    /**
     * 返回小于等于指定值的最大 key, 不存在时返回 NaN
     */
    private double floorKey(double key) {
        if (map.containsKey(key)) return key;
        Double2ObjectSortedMap<Range<V>> head = map.headMap(key);
        return head.isEmpty() ? Double.NaN : head.lastDoubleKey();
    }

    /**
     * 返回严格大于指定值的最小 key, 不存在时返回 NaN
     */
    private double higherKey(double key) {
        Double2ObjectSortedMap<Range<V>> tail = map.tailMap(key);
        for (Double2ObjectMap.Entry<Range<V>> entry : tail.double2ObjectEntrySet()) {
            if (entry.getDoubleKey() > key) {
                return entry.getDoubleKey();
            }
        }
        return Double.NaN;
    }

    /**
     * 放入区间, 与已有区间重叠时覆盖（删除重叠的旧区间）
     */
    public void put(double start, double end, V value) {
        put(start, end, value, true);
    }

    /**
     * 放入区间
     *
     * @param overwrite 为 true 时删除所有重叠的旧区间, 为 false 时抛出异常
     */
    public void put(double start, double end, V value, boolean overwrite) {
        if (Double.isNaN(start) || Double.isNaN(end)) {
            throw new IllegalArgumentException("start/end must not be NaN");
        }
        if (start > end) {
            throw new IllegalArgumentException("start > end: " + start + " > " + end);
        }

        if (overwrite) {
            DoubleArrayList toRemove = new DoubleArrayList();

            double leftKey = floorKey(start);
            if (!Double.isNaN(leftKey) && map.get(leftKey).end() > start) {
                toRemove.add(leftKey);
            }

            for (Double2ObjectMap.Entry<Range<V>> entry : map.tailMap(start).double2ObjectEntrySet()) {
                double key = entry.getDoubleKey();
                if (key <= start) continue;
                if (key >= end) break;
                toRemove.add(key);
            }

            for (int i = 0; i < toRemove.size(); i++) {
                map.remove(toRemove.getDouble(i));
            }
        } else {
            double leftKey = floorKey(start);
            if (!Double.isNaN(leftKey)) {
                Range<V> leftRange = map.get(leftKey);
                if (leftRange.end() > start) {
                    throw new IllegalStateException("Interval [" + start + ", " + end + "] overlaps " + leftRange);
                }
            }

            double rightKey = higherKey(start);
            if (!Double.isNaN(rightKey)) {
                Range<V> rightRange = map.get(rightKey);
                if (rightRange.start() < end) {
                    throw new IllegalStateException("Interval [" + start + ", " + end + "] overlaps " + rightRange);
                }
            }
        }

        map.put(start, new Range<>(start, end, value));
    }

    /**
     * 命中时返回所在区间, 未命中时返回 null
     */
    public Range<V> getRange(double key) {
        if (Double.isNaN(key)) return null;
        double floor = floorKey(key);
        if (Double.isNaN(floor)) return null;
        Range<V> range = map.get(floor);
        return key <= range.end() ? range : null;
    }

    public V get(double key) {
        Range<V> range = getRange(key);
        return range != null ? range.value() : null;
    }

    public V getOrDefault(double key, V defaultValue) {
        V value = get(key);
        return value != null ? value : defaultValue;
    }

    public boolean containsKey(double key) {
        return getRange(key) != null;
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void clear() {
        map.clear();
    }
}