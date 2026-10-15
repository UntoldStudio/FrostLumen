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
package top.untold.frostlumen.core.gui.node;

import top.untold.frostlumen.core.data.ScaleOffset;
import top.untold.frostlumen.core.gui.GuiNode;

public class VerticalBox extends LayoutBox<VerticalBox> {
    @Override
    public void sortNodes(){
        int currentOffset = nodeStartSpacing;

        for (GuiNode<?> node : sortedNodeList){
            node.setPosition(ScaleOffset.fromOffset(node.getPosition().getXPixelFromParentRealSize(this), currentOffset));
            currentOffset += node.getSize().getYPixelFromParentRealSize(this) + nodeSpacing;
        }
    }

    public VerticalBox(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
