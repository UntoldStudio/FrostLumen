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

import top.untoldstudio.frostlumen.core.exception.TweenException;

public abstract class NumberTween extends Tween {
    private final long targetTime;
    private final TweenFunction function;
    private long currentTime;
    private boolean isCompleted = false;

    @Override
    public void reset() {
        isCompleted = false;
        currentTime = 0;
        super.reset();
    }

    @Override
    protected void resumeTween() {
        if (isCompleted) {
            pause();
        }
    }

    @Override
    protected void stepTween(long delta) {
        //resume但是Tween已经播放完成的时候会走这里
        if (currentTime >= targetTime) {
            playing = false;
            isCompleted = true;
            applyEased(1.0);
            triggerOnCompleteCallback();
            return;
        }

        currentTime += delta;

        double percent = Math.min((double) currentTime / targetTime, 1.0);
        double eased = function.computeEased(percent);

        applyEased(eased);

        if (currentTime >= targetTime) {
            playing = false;
            isCompleted = true;
            applyEased(1.0);
            triggerOnCompleteCallback();
        }
    }

    protected abstract void applyEased(double eased);

    @Override
    public boolean isCompleted() {
        return isCompleted;
    }

    protected NumberTween(long time, TweenFunction function, boolean removeOnFinish) {
        super(removeOnFinish);

        if (time <= 0) throw new TweenException("time <= 0");

        this.targetTime = time;
        this.function = function;
    }
}
