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

import java.util.ArrayList;
import java.util.List;

public abstract class ParentTween extends Tween {
    private final List<Tween> children = new ArrayList<>();
    private boolean needUpdate = false;

    protected void addChild(Tween tween) {
        children.add(tween);
        tween.registerOnUpdateCallback(() -> needUpdate = true);
        tween.registerOnCompleteCallback(this::tryTriggerOnCompleteCallback);
    }
    protected void addChildren(Tween... tweenArray) {
        for (Tween tween : tweenArray) {
            addChild(tween);
        }
    }

    private void tryTriggerOnCompleteCallback() {
        boolean isAllFinished = true;

        for (Tween child : children) {
            if (!child.isCompleted()) {
                isAllFinished = false;
                break;
            }
        }

        if (isAllFinished) {
            playing = false;
            triggerOnCompleteCallback();
        }
    }

    @Override
    public void resetTween() {
        for (Tween child : children) {
            child.reset();
        }
    }

    @Override
    protected void stepTween(long delta) {
        for (Tween child : children) {
            child.step(delta);
        }
        if (needUpdate) {
            needUpdate = false;
            triggerOnUpdateCallback();
        }
    }

    @Override
    protected void resumeTween() {
        for (Tween tween : children) {
            tween.resume();
        }
    }

    @Override
    public boolean isCompleted() {
        for (Tween child : children) {
            if (!child.isCompleted()) return false;
        }
        return true;
    }

    @Override
    protected void pauseTween() {
        for (Tween tween : children) {
            tween.pause();
        }
    }

    protected ParentTween(boolean removeOnComplete) {
        super(removeOnComplete);
    }
}
