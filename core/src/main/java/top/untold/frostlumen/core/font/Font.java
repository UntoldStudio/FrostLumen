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
package top.untold.frostlumen.core.font;

import org.lwjgl.CLongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_Size;
import org.lwjgl.util.freetype.FT_Vector;
import top.untold.frostlumen.core.render.ResourceManager;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.util.freetype.FreeType.*;

public record Font(FT_Face face, String path, ByteBuffer data, int id) {
    public static final String DEFAULT_FONT_PATH = "/inter.ttf";

    private final static AtomicInteger fontSeq = new AtomicInteger(0);

    public int getStringWidth(String text, int fontSize) {
        FT_Face face = face();
        FT_Set_Pixel_Sizes(face, 0, fontSize);
        int prevIdx = 0;
        boolean hasPrev = false;
        float width = 0;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            CLongBuffer advanceBuf = stack.mallocCLong(1);
            FT_Vector k = FT_Vector.malloc();
            try {
                for (int offset = 0; offset < text.length(); ) {
                    int codepoint = text.codePointAt(offset);
                    offset += Character.charCount(codepoint);
                    int gi = FT_Get_Char_Index(face, codepoint);
                    if (gi == 0) continue;
                    if (hasPrev) {
                        FT_Get_Kerning(face, prevIdx, gi, FT_KERNING_DEFAULT, k);
                        width += k.x() / 64.0f;
                    }
                    advanceBuf.clear();
                    FT_Get_Advance(face, gi, FT_LOAD_DEFAULT, advanceBuf);
                    width += (int) advanceBuf.get(0) / 65536.0f;
                    prevIdx = gi;
                    hasPrev = true;
                }
            } finally {
                k.free();
            }
        }
        return Math.round(width);
    }

    public int getStringHeight(int fontSize) {
        FT_Face face = face();
        FT_Set_Pixel_Sizes(face, 0, fontSize);
        FT_Size size = face.size();
        Objects.requireNonNull(size);
        long ascender = size.metrics().ascender();
        long descender = size.metrics().descender();
        return Math.round((ascender - descender) / 64.0f);
    }

    public static Font loadFontFromCurrentResourceManager(String path) {
        return ResourceManager.getIResourceManagerFromThreadLocal().loadFont(path);
    }
    public static Font getDefaultFont() {
        return loadFontFromCurrentResourceManager(DEFAULT_FONT_PATH);
    }

    public Font(FT_Face face, String path, ByteBuffer data) {
        this(face, path, data, fontSeq.getAndIncrement());
    }
}
