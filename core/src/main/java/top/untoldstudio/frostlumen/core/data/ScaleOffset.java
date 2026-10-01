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

import top.untoldstudio.frostlumen.core.tool.MathTool;
import top.untoldstudio.frostlumen.core.gui.ParentNode;

public record ScaleOffset(
        double xScale,
        int xOffset,
        double yScale,
        int yOffset
) {
    public static final ScaleOffset ZERO = new ScaleOffset(0, 0, 0, 0);

    public ScaleOffset withScale(double xScale, double yScale){
        return new ScaleOffset(xScale, xOffset, yScale, yOffset);
    }
    public ScaleOffset withOffset(int xOffset, int yOffset){
        return new ScaleOffset(xScale, xOffset, yScale, yOffset);
    }
    public ScaleOffset withXScale(double xScale){
        return withScale(xScale, yScale);
    }
    public ScaleOffset withYScale(double yScale){
        return withScale(xScale, yScale);
    }
    public ScaleOffset withXOffset(int xOffset){
        return withOffset(xOffset, yOffset);
    }
    public ScaleOffset withYOffset(int yOffset){
        return withOffset(xOffset, yOffset);
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
    public ScaleOffset subScale(double xScale, double yScale){
        return sub(xScale, 0, yScale, 0);
    }
    public ScaleOffset subXScale(double xScale){
        return subScale(xScale, 0);
    }
    public ScaleOffset subYScale(double yScale){
        return subScale(0, yScale);
    }
    public ScaleOffset subOffset(int xOffset, int yOffset){
        return sub(0, xOffset, 0, yOffset);
    }
    public ScaleOffset subXOffset(int xOffset){
        return subOffset(xOffset, 0);
    }
    public ScaleOffset subYOffset(int yOffset){
        return subOffset(0, yOffset);
    }
    public ScaleOffset sub(ScaleOffset other){
        return sub(other.xScale, other.xOffset, other.yScale, other.yOffset);
    }
    public ScaleOffset sub(double xScale, int xOffset, double yScale, int yOffset){
        return new ScaleOffset(this.xScale - xScale, this.xOffset - xOffset, this.yScale - yScale, this.yOffset - yOffset);
    }

    public int getRealPixelXInParent(ParentNode<?> node) {
        return MathTool.round(xOffset + xScale * node.getRealSizeX());
    }
    public int getRealPixelYInParent(ParentNode<?> node) {
        return MathTool.round(yOffset + yScale * node.getRealSizeY());
    }
}
