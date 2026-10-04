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
package top.untoldstudio.frostlumen.core.data;

public record ScaleOffset(double xScale, int xOffset, double yScale, int yOffset) {
    public static final ScaleOffset ZERO = new ScaleOffset(0, 0, 0, 0);

    public ScaleOffset withScale(double xScale, double yScale){
        return new ScaleOffset(xScale, xOffset, yScale, yOffset);
    }
    public ScaleOffset withOffset(int xOffset, int yOffset){
        return new ScaleOffset(xScale, xOffset, yScale, yOffset);
    }
    public ScaleOffset withXScale(double xScale){
        return new ScaleOffset(xScale, xOffset, yScale, yOffset);
    }
    public ScaleOffset withX(double xScale, int xOffset) {
        return new ScaleOffset(xScale, xOffset, yScale, yOffset);
    }
    public ScaleOffset withY(double yScale, int yOffset) {
        return new ScaleOffset(xScale, xOffset, yScale, yOffset);
    }
    public ScaleOffset withYScale(double yScale){
        return new ScaleOffset(xScale, xOffset, yScale, yOffset);
    }
    public ScaleOffset withXOffset(int xOffset){
        return new ScaleOffset(xScale, xOffset, yScale, yOffset);
    }
    public ScaleOffset withYOffset(int yOffset){
        return new ScaleOffset(xScale, xOffset, yScale, yOffset);
    }

    public static ScaleOffset fromScale(double xScale, double yScale) {
        return new ScaleOffset(xScale, 0, yScale, 0);
    }
    public static ScaleOffset fromOffset(int xOffset, int yOffset) {
        return new ScaleOffset(0, xOffset, 0, yOffset);
    }

    public ScaleOffset add(double xScale, int xOffset, double yScale, int yOffset){
        return new ScaleOffset(this.xScale + xScale, this.xOffset + xOffset, this.yScale + yScale, this.yOffset + yOffset);
    }
    public ScaleOffset addScale(double xScale, double yScale){
        return add(xScale, 0, yScale, 0);
    }
    public ScaleOffset addXScale(double xScale){
        return addScale(xScale, 0);
    }
    public ScaleOffset addYScale(double yScale){
        return addScale(0, yScale);
    }
    public ScaleOffset addOffset(int xOffset, int yOffset){
        return add(0, xOffset, 0, yOffset);
    }
    public ScaleOffset addXOffset(int xOffset){
        return addOffset(xOffset, 0);
    }
    public ScaleOffset addYOffset(int yOffset){
        return addOffset(0, yOffset);
    }
    public ScaleOffset add(ScaleOffset other){
        return add(other.xScale, other.xOffset, other.yScale, other.yOffset);
    }
}
