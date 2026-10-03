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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TweenScheduler {
    private final List<Tween> tweenList = new CopyOnWriteArrayList<>();

    public void step(long delta) {
        for (Tween tween : tweenList) {
            tween.step(delta);
        }
        tweenList.removeIf(tween -> tween.isCompleted() && tween.isRemoveOnComplete());
    }

    public void registerTween(Tween tween) {
        tweenList.add(tween);
    }
    public void unregisterTween(Tween tween) {
        tweenList.remove(tween);
    }
}
