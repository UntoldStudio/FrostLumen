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
package top.untold.frostlumen.core.tween;

import top.untold.frostlumen.core.tool.MathTool;

import java.util.function.IntConsumer;

// --8<-- [start:classDefinition]
public class IntTween extends NumberTween {
    private final int startValue;
    private final int endValue;
    private int currentValue;

    @Override
    protected void resetTween() {
        currentValue = startValue;
    }

    public IntTween(int startValue, int endValue, long time, TweenFunction function, boolean removeOnComplete) {
        super(time, function, removeOnComplete);
        this.startValue = startValue;
        this.currentValue = startValue;
        this.endValue = endValue;
    }

    @Override
    protected void applyEased(double eased) {
        currentValue = MathTool.round(startValue + (endValue - startValue) * eased);
        triggerOnUpdateCallback();
    }

    public void registerAutoCallOnSetter(IntConsumer consumer) {
        registerOnUpdateCallback(() -> consumer.accept(currentValue));
    }

    public int getCurrentValue() {
        return currentValue;
    }
}
// --8<-- [end:classDefinition]