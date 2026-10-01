package top.untoldstudio.frostlumen.core.font;

import org.lwjgl.CLongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_Size;
import org.lwjgl.util.freetype.FT_Vector;
import top.untoldstudio.frostlumen.core.render.IResourceManager;

import java.nio.ByteBuffer;
import java.util.Objects;

import static org.lwjgl.util.freetype.FreeType.*;

public record Font(FT_Face face, String path, ByteBuffer data, int id) {
    public static final String DEFAULT_FONT_PATH = "/inter.ttf";

    private static int fontSeq = 0;

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
        return IResourceManager.getIResourceManagerFromThreadLocal().loadFont(path);
    }
    public static Font getDefaultFont() {
        return loadFontFromCurrentResourceManager(DEFAULT_FONT_PATH);
    }

    public Font(FT_Face face, String path, ByteBuffer data) {
        this(face, path, data, fontSeq++);
    }
}
