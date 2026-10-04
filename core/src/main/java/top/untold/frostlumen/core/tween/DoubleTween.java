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

import java.util.function.DoubleConsumer;

public class DoubleTween extends NumberTween {
    private final double startValue;
    private final double endValue;
    private double currentValue;

    @Override
    protected void resetTween() {
        currentValue = startValue;
    }

    public DoubleTween(double startValue, double endValue, long time, TweenFunction function, boolean removeOnComplete) {
        super(time, function, removeOnComplete);
        this.startValue = startValue;
        this.currentValue = startValue;
        this.endValue = endValue;
    }

    @Override
    protected void applyEased(double eased) {
        currentValue = startValue + (endValue - startValue) * eased;
        triggerOnUpdateCallback();
    }

    public void registerAutoCallOnSetter(DoubleConsumer consumer) {
        registerOnUpdateCallback(() -> consumer.accept(currentValue));
    }

    public double getCurrentValue() {
        return currentValue;
    }
}
