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
import top.untoldstudio.frostlumen.core.gui.NodeRoot;
import top.untoldstudio.frostlumen.core.gui.Window;
import top.untoldstudio.frostlumen.core.gui.node.Frame;
import top.untoldstudio.frostlumen.core.gui.node.ImageButton;
import top.untoldstudio.frostlumen.core.render.ResourceManager;
import top.untoldstudio.frostlumen.core.render.RenderProviderType;
import top.untoldstudio.frostlumen.core.tween.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL32.*;

public class Main {
    public static void main(String[] args) {
        if (!glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW");
        }

        long windowHandle = glfwCreateWindow(800, 600, "Test", 0, 0);

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        Window window = Window.from(windowHandle, RenderProviderType.OPENGL);

        ResourceManager resourceManager = ResourceManager.getIResourceManagerFromThreadLocal();
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
        Frame frame2 = new Frame(ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.3, 0.3))
                .setAnchor(0.5, 0.5)
                .setBackgroundColor(new RGBA(210, 230, 255, 0))
                .setBackgroundBlurStrength(0.7)
                .setBackgroundCornerRadius(10)
                .setAngle(100)
                ;

        CustomTweenFunction tweenFunction = new CustomTweenFunction(TweenFunctions.LINEAR)
                .setSegment(0, 0.2, TweenFunctions.BOUNCE_IN_OUT, child1 -> {
                    child1.setSegment(0, 0.8, TweenFunctions.QUINT_IN_OUT, childChild1 -> {
                        childChild1.setSegment(0, 0.5, TweenFunctions.QUINT_IN_OUT, childChildChild1 -> {
                        });
                    })
                            .setSegment(0.8, 1, child1, childChild2 -> {
                            })
                    ;
                })
                .setSegment(0.2, 0.5, TweenFunctions.SINE_IN_OUT, child2 -> {
                })
                .setSegment(0.5, 0.7, TweenFunctions.LINEAR, child3 -> {
                })
                .setSegment(0.7, 1, TweenFunctions.BOUNCE_OUT, child4 -> {
                })
                ;

        ScaleOffsetTween scaleOffsetTween = new ScaleOffsetTween(ScaleOffset.ZERO, ScaleOffset.fromScale(1, 1), 1000, tweenFunction, false);
        ScaleOffsetTween scaleOffsetTween1 = new ScaleOffsetTween(ScaleOffset.fromScale(1, 1), ScaleOffset.ZERO, 1000, tweenFunction, false);
        scaleOffsetTween.registerOnCompleteCallback(scaleOffsetTween1::play);
        scaleOffsetTween1.registerOnCompleteCallback(scaleOffsetTween::play);

        frame1.addChild(button);

        NodeRoot root = window.getNodeRoot();
        root.addChildren(frame1, frame2);
        TweenScheduler tweenScheduler = root.getTweenScheduler();
        tweenScheduler.registerTween(scaleOffsetTween);
        tweenScheduler.registerTween(scaleOffsetTween1);
        scaleOffsetTween.registerAutoCallOnSetter(frame1::setPosition);
        scaleOffsetTween1.registerAutoCallOnSetter(frame1::setPosition);
        scaleOffsetTween.play();

        long previousTime = System.nanoTime();
        int frameCount = 0;

        glfwSwapInterval(0);

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
