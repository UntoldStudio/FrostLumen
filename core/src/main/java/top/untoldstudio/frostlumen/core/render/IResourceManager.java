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
