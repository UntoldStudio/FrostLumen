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
package top.untoldstudio.frostlumen.example;

import org.lwjgl.opengl.GL;
import top.untoldstudio.frostlumen.core.data.ImageAlignment;
import top.untoldstudio.frostlumen.core.data.RGBA;
import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.gui.Window;
import top.untoldstudio.frostlumen.core.gui.node.Frame;
import top.untoldstudio.frostlumen.core.gui.node.ImageButton;
import top.untoldstudio.frostlumen.core.render.IResourceManager;
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

        IResourceManager resourceManager = IResourceManager.getIResourceManagerFromThreadLocal();
        ImageButton button = new ImageButton(resourceManager.loadTexture("/oiiaioiiai-blue.jpg"), ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(1.5, 1.5))
                .normal().setImageAlignment(ImageAlignment.FILL).getNode()
                .onHover().setTexture(resourceManager.loadTexture("/oiiaioiiai-red.jpg")).getNode()
                .onClick().setTexture(resourceManager.loadTexture("/oiiaioiiai-green.jpg")).getNode()
                .setAnchor(0.5, 0.5)
                .setDrawBackground(true)
                .setBackgroundCornerRadius(10)
                ;
        Frame frame1 = new Frame(ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5))
                .setAnchor(0.5, 0.5).setBackgroundColor(new RGBA(210, 230, 255, 90))
                .setClipChildren(true)
                .setAngle(20f)
                .setBackgroundCornerRadius(10)
                ;
        Frame frame2 = new Frame(ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5))
                .setAnchor(0.5, 0.5)
                .setBackgroundColor(new RGBA(210, 230, 255, 90))
                .setBackgroundBlurStrength(1f)
                .setBackgroundCornerRadius(10)
                ;
        Frame frame3 = new Frame(ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.3, 0.3))
                .setAnchor(0.5, 0.5)
                .setBackgroundColor(new RGBA(210, 230, 255, 90))
                .setBackgroundBlurStrength(1f)
                .setBackgroundCornerRadius(10)
                .setAngle(100)
                ;
        frame1.addChild(button);
        window.getNodeRoot().addChildren(frame1, frame2, frame3);

        long previousTime = System.nanoTime();
        int frameCount = 0;

        glfwSwapInterval(1);

        while (!glfwWindowShouldClose(windowHandle)) {
            glfwPollEvents();
            glClearColor(0.1f, 0.1f, 0.1f, 0.1f);
            glClear(GL_COLOR_BUFFER_BIT);
            window.getNodeRoot().render();

            glfwSwapBuffers(windowHandle);

            frameCount++;
            long currentTime = System.nanoTime();
            long elapsed = currentTime - previousTime;
            if (elapsed >= 1_000_000_000L) {
                double seconds = elapsed / 1_000_000_000.0;
                System.out.printf("FPS: %.2f%n", frameCount / seconds);
                frameCount = 0;
                previousTime = currentTime;
            }
        }

        window.close();
    }
}
