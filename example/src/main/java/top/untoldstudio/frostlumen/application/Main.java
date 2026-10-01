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
package top.untoldstudio.frostlumen.application;

import org.lwjgl.opengl.GL;
import top.untoldstudio.frostlumen.core.data.RGBA;
import top.untoldstudio.frostlumen.core.data.ScaleOffset;
import top.untoldstudio.frostlumen.core.gui.Window;
import top.untoldstudio.frostlumen.core.gui.node.TextLabel;
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

        TextLabel label = new TextLabel("114514", ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5))
                .setTextColor(RGBA.BLUE)
                .setDrawBackground(true)
                .setAnchor(0.5, 0.5)
                .setBackgroundCornerRadius(10)
                .setAngle(10f)
                ;
        window.getNodeRoot().addChild(label);

        while (!glfwWindowShouldClose(windowHandle)) {
            glfwPollEvents();

            glClearColor(0.1f, 0.1f, 0.1f, 0.1f);
            glClear(GL_COLOR_BUFFER_BIT);

            window.getNodeRoot().render();
            glfwSwapBuffers(windowHandle);
        }
    }
}
