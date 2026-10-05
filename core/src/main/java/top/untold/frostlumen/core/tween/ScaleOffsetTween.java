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

import top.untold.frostlumen.core.data.ScaleOffset;

import java.util.function.Consumer;

// --8<-- [start:classDefinition]
public class ScaleOffsetTween extends ParentTween {
    private final DoubleTween xScaleTween;
    private final IntTween xOffsetTween;
    private final DoubleTween yScaleTween;
    private final IntTween yOffsetTween;

    public ScaleOffset getCurrentValue() {
        return new ScaleOffset(xScaleTween.getCurrentValue(), xOffsetTween.getCurrentValue(), yScaleTween.getCurrentValue(), yOffsetTween.getCurrentValue());
    }

    public void registerAutoCallOnSetter(Consumer<ScaleOffset> consumer) {
        registerOnUpdateCallback(() -> consumer.accept(getCurrentValue()));
    }

    public ScaleOffsetTween(ScaleOffset start, ScaleOffset end, long time, TweenFunction function, boolean removeOnComplete) {
        super(removeOnComplete);
        xScaleTween = new DoubleTween(start.xScale(), end.xScale(), time, function, false);
        xOffsetTween = new IntTween(start.xOffset(), end.xOffset(), time, function, false);
        yScaleTween = new DoubleTween(start.yScale(), end.yScale(), time, function, false);
        yOffsetTween = new IntTween(start.yOffset(), end.yOffset(), time, function, false);

        addChildren(xScaleTween, xOffsetTween, yScaleTween, yOffsetTween);
    }
}
// --8<-- [end:classDefinition]