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

import top.untoldstudio.frostlumen.core.data.RGBA;

import java.util.function.Consumer;

public class RGBATween extends ParentTween {
    private final IntTween redTween;
    private final IntTween greenTween;
    private final IntTween blueTween;
    private final IntTween alphaTween;

    public RGBA getCurrentValue() {
        return new RGBA(redTween.getCurrentValue(), greenTween.getCurrentValue(), blueTween.getCurrentValue(), alphaTween.getCurrentValue());
    }

    public void registerAutoCallOnSetter(Consumer<RGBA> consumer) {
        registerOnUpdateCallback(() -> consumer.accept(getCurrentValue()));
    }

    public RGBATween(RGBA start, RGBA end, long time, TweenFunction function, boolean removeOnFinish) {
        super(removeOnFinish);
        redTween = new IntTween(start.red(), end.red(), time, function, false);
        greenTween = new IntTween(start.green(), end.green(), time, function, false);
        blueTween = new IntTween(start.blue(), end.blue(), time, function, false);
        alphaTween = new IntTween(start.alpha(), end.alpha(), time, function, false);

        addChildren(redTween, greenTween, blueTween, alphaTween);
    }
}
