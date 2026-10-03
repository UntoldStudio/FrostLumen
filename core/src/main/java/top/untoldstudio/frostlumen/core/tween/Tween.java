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

import java.util.ArrayList;
import java.util.List;

public abstract class Tween {
    protected final List<Runnable> onCompleteCallbackList = new ArrayList<>();
    protected final List<Runnable> onUpdateCallbackList = new ArrayList<>();
    protected final boolean removeOnComplete;
    protected boolean playing;

    public void registerOnCompleteCallback(Runnable runnable) {
        onCompleteCallbackList.add(runnable);
    }
    public void unregisterOnCompleteCallback(Runnable runnable) {
        onCompleteCallbackList.remove(runnable);
    }

    public void registerOnUpdateCallback(Runnable runnable) {
        onUpdateCallbackList.add(runnable);
    }
    public void unregisterOnUpdateCallback(Runnable runnable) {
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
        playing = true;
        reset();
        playTween();
    }

    protected void playTween() {}

    public final void step(long delta) {
        if (!playing) return;
        stepTween(delta);
    }

    protected abstract void stepTween(long delta);

    public final void stop() {
        playing = false;
        stopTween();
    }

    protected void stopTween() {}

    public void reset() {
        resetTween();
    }

    protected void resetTween() {}

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
