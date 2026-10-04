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
package top.untold.frostlumen.core.event;

public class MouseScrollEvent extends CancelableEvent {
    private final double x;
    private final double y;
    private final double xDelta;
    private final double yDelta;

    public MouseScrollEvent(double x, double y, double xDelta, double yDelta) {
        this.x = x;
        this.y = y;
        this.xDelta = xDelta;
        this.yDelta = yDelta;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getXDelta() {
        return xDelta;
    }
    public double getYDelta() {
        return yDelta;
    }
}
