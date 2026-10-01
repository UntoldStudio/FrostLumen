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
package top.untoldstudio.frostlumen.core.gui.node;

import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.gui.GuiNode;
import top.untoldstudio.frostlumen.core.render.GuiRender;

public class Frame extends GuiNode<Frame> {
    @Override
    public void render(GuiRender render, long delta) {
        super.drawDefaultFrameBackground(render);
    }

    public Frame(ScaleOffset position, ScaleOffset size) {
        super(position, size);
    }
}
