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
package top.untold.frostlumen.core.render;

import top.untold.frostlumen.core.data.NiceSliceType;
import top.untold.frostlumen.core.font.Font;
import top.untold.frostlumen.core.texture.Texture;

public interface ResourceManager {
    ThreadLocal<ResourceManager> THREAD_LOCAL = new ThreadLocal<>();

    Texture loadTexture(String path);
    Texture loadTexture(String path, boolean isLinear);
    Texture loadTexture(byte[] data);
    Texture loadTexture(byte[] data, boolean isLinear);
    Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, int left, int right, int top, int bottom);
    Texture loadNiceSliceTexture(String path, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom);
    Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, int left, int right, int top, int bottom);
    Texture loadNiceSliceTexture(byte[] data, NiceSliceType type, boolean stretchInner, boolean isLinear, int left, int right, int top, int bottom);
    Font loadFont(String path);

    static ResourceManager getIResourceManagerFromThreadLocal() {
        return THREAD_LOCAL.get();
    }
}
