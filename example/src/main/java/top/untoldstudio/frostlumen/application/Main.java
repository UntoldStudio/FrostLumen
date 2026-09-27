package top.untoldstudio.frostlumen.application;

import org.lwjgl.opengl.GL;
import top.untoldstudio.frostlumen.core.data.RGBA;
import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.data.ThicknessPosition;
import top.untoldstudio.frostlumen.core.gui.Window;
import top.untoldstudio.frostlumen.core.gui.node.Frame;
import top.untoldstudio.frostlumen.core.render.RenderProviderType;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL32.*;

public class Main {
    public static void main(String[] args) {
        glfwInit();
        long windowHandle = glfwCreateWindow(800, 600, "Test", 0, 0);

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        Window window = Window.from(windowHandle, RenderProviderType.OPENGL);

        Frame frame = new Frame(ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5))
                .setAnchor(0.5, 0.5).setBackgroundColor(RGBA.BLUE)//.setBackgroundCornerRadius(100)
                .setBackgroundBorderThickness(10).setBackgroundBorderThicknessPosition(ThicknessPosition.INSIDE).setBackgroundCornerRadius(50).setBackgroundBorderColor(RGBA.RED.withAlpha(100))
                ;
        window.getNodeRoot().addChildren(frame);

        while (!glfwWindowShouldClose(windowHandle)) {
            glfwPollEvents();

            glClearColor(1f, 1f, 1f, 1f);
            glClear(GL_COLOR_BUFFER_BIT);

            window.getNodeRoot().render();
            glfwSwapBuffers(windowHandle);
        }
    }
}
