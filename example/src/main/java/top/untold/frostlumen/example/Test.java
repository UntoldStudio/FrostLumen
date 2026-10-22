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
package top.untold.frostlumen.example;

import org.lwjgl.opengl.GL;
import top.untold.frostlumen.core.data.InputAction;
import top.untold.frostlumen.core.data.ScaleOffset;
import top.untold.frostlumen.core.gui.NodeRoot;
import top.untold.frostlumen.core.gui.Window;
import top.untold.frostlumen.core.gui.node.TextButton;
import top.untold.frostlumen.core.gui.node.TextLabel;
import top.untold.frostlumen.core.render.RenderProviderType;
import top.untold.frostlumen.core.render.ResourceManager;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL32C.*;

public class Test {
    private static void buildTest(NodeRoot root, ResourceManager resourceManager, Window window) {
        TextButton button = new TextButton("Hello", ScaleOffset.ZERO, ScaleOffset.fromScale(0.5, 0.5));
        button.registerMouseButtonEventListener((textButton, event) -> {
            if (event.getAction() == InputAction.PRESS) {
                textButton.description().setText("Press");
            } else if (event.getAction() == InputAction.RELEASE) {
                textButton.description().setText("Release");
            }
        });
        root.addChild(button);
    }

    static void main(String[] args) {
        if (!glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW");
        }

        long windowHandle = glfwCreateWindow(800, 600, "Test", 0, 0);

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        Window window = Window.from(windowHandle);
        window.init(RenderProviderType.OPENGL);

        NodeRoot root = window.getNodeRoot();
        ResourceManager resourceManager = root.getResourceManager();

        buildTest(root, resourceManager, window);

        long previousTime = System.nanoTime();
        int frameCount = 0;

        glfwSwapInterval(0);

        while (!glfwWindowShouldClose(windowHandle)) {
            glfwPollEvents();
            glClearColor(0.1f, 0.1f, 0.1f, 0.1f);
            glClear(GL_COLOR_BUFFER_BIT);
            root.render();

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
