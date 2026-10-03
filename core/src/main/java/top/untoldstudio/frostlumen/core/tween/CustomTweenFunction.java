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
package top.untoldstudio.frostlumen.core.tween;

import top.untoldstudio.frostlumen.core.tool.IntervalMap;

import java.util.Objects;
import java.util.function.Consumer;

public class CustomTweenFunction implements TweenFunction {
    private final IntervalMap<TweenFunction> tweenFunctions = new IntervalMap<>();
    private TweenFunction outOfRangeFunction;

    @Override
    public double computeEased(double percent) {
        IntervalMap.Range<TweenFunction> range = tweenFunctions.getRange(percent);
        if (range == null) {
            return outOfRangeFunction.computeEased(percent);
        }

        double start = range.start();
        double end = range.end();
        double localT = (percent - start) / (end - start);

        return start + (end - start) * range.value().computeEased(localT);
    }

    /**
     * 在当前对象上增加一段, 直接使用给定的 {@code function} 作为该段的缓动函数
     * 返回 {@code this} 以便继续链式配置
     */
    public CustomTweenFunction setSegment(double start, double end, TweenFunction function) {
        tweenFunctions.put(start, end, function);
        return this;
    }

    /**
     * 在当前对象上增加一段, 并新建一个 {@link CustomTweenFunction} 作为该段的缓动函数
     * 新对象的越界函数由 {@code nestedOutOfRangeFunction} 指定, 随后通过 {@code nestedConfigurator} 配置
     * 返回 {@code this}, 嵌套对象的配置在回调内完成
     */
    public CustomTweenFunction setSegment(double start, double end, TweenFunction nestedOutOfRangeFunction, Consumer<CustomTweenFunction> nestedConfigurator) {
        CustomTweenFunction function = new CustomTweenFunction(nestedOutOfRangeFunction);
        setSegment(start, end, function);
        nestedConfigurator.accept(function);
        return this;
    }

    public CustomTweenFunction setOutOfRangeFunction(TweenFunction outOfRangeFunction) {
        Objects.requireNonNull(outOfRangeFunction);
        this.outOfRangeFunction = outOfRangeFunction;
        return this;
    }

    public CustomTweenFunction(TweenFunction outOfRangeFunction) {
        setOutOfRangeFunction(outOfRangeFunction);
    }
}
