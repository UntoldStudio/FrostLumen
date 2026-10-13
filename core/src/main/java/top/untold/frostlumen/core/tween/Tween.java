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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Tween {
    protected final List<Runnable> onCompleteCallbackList = new CopyOnWriteArrayList<>();
    protected final List<Runnable> onUpdateCallbackList = new CopyOnWriteArrayList<>();
    protected final boolean removeOnComplete;
    protected int remainingRepeatCount = 0;
    protected boolean alwaysRepeat = false;
    protected boolean playing;

    public void setRemainingRepeatCount(int remainingRepeatCount) {
        this.remainingRepeatCount = remainingRepeatCount;
    }
    public void setAlwaysRepeat(boolean alwaysRepeat) {
        this.alwaysRepeat = alwaysRepeat;
    }
    public boolean isAlwaysRepeat() {
        return alwaysRepeat;
    }
    public int getRemainingRepeatCount() {
        return remainingRepeatCount;
    }

    public void onComplete(Runnable runnable) {
        onCompleteCallbackList.add(runnable);
    }
    public void removeOnComplete(Runnable runnable) {
        onCompleteCallbackList.remove(runnable);
    }

    public void onUpdate(Runnable runnable) {
        onUpdateCallbackList.add(runnable);
    }
    public void removeOnUpdate(Runnable runnable) {
        onUpdateCallbackList.remove(runnable);
    }

    protected void triggerOnCompleteCallback() {
        for (Runnable runnable : onCompleteCallbackList) {
            runnable.run();
        }
    }
    protected void triggerOnUpdateCallback() {
        for (Runnable runnable : onUpdateCallbackList) {
            runnable.run();
        }
    }

    public final void play() {
        reset();
        resume();
    }

    public final void step(double delta) {
        if (!playing) return;
        stepTween(delta);
        if (isCompleted()) {
            if (alwaysRepeat) {
                reset();
            } else if (remainingRepeatCount > 0) {
                reset();
                remainingRepeatCount--;
            }
        }
    }

    protected abstract void stepTween(double delta);

    public final void resume() {
        if (isCompleted()) return;
        playing = true;
        resumeTween();
    }
    public final void pause() {
        playing = false;
        pauseTween();
    }

    protected void pauseTween() {}
    protected void resumeTween() {}

    public void reset() {
        resetTween();
    }

    protected abstract void resetTween();

    public final boolean isPlaying() {
        return playing;
    }
    public final boolean isRemoveOnComplete() {
        return removeOnComplete;
    }

    public abstract boolean isCompleted();

    protected Tween(boolean removeOnComplete) {
        this.removeOnComplete = removeOnComplete;
    }
}
