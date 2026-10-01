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
package top.untoldstudio.frostlumen.core.render;

import top.untoldstudio.frostlumen.core.font.Font;
import top.untoldstudio.frostlumen.core.texture.Texture;

public interface IResourceManager {
    ThreadLocal<IResourceManager> THREAD_LOCAL = new ThreadLocal<>();

    Texture loadTexture(String path);
    Texture loadNiceSliceTexture(String path, int left, int right, int top, int bottom);
    Font loadFont(String path);

    static IResourceManager getIResourceManagerFromThreadLocal() {
        return THREAD_LOCAL.get();
    }
}
