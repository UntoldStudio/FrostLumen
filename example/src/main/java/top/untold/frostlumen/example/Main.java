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
import top.untold.frostlumen.core.data.*;
import top.untold.frostlumen.core.gui.NodeRoot;
import top.untold.frostlumen.core.gui.Window;
import top.untold.frostlumen.core.gui.node.*;
import top.untold.frostlumen.core.render.ResourceManager;
import top.untold.frostlumen.core.render.RenderProviderType;
import top.untold.frostlumen.core.tween.CustomTweenFunction;
import top.untold.frostlumen.core.tween.ScaleOffsetTween;
import top.untold.frostlumen.core.tween.TweenFunctions;
import top.untold.frostlumen.core.tween.TweenScheduler;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL32.*;

// --8<-- [start:classDefinition]
public class Main {
    public static void main(String[] args) {
        if (!glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW");
        }

        long windowHandle = glfwCreateWindow(800, 600, "Test", 0, 0);

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        Window window = Window.from(windowHandle, RenderProviderType.OPENGL);

        NodeRoot root = window.getNodeRoot();
        ResourceManager resourceManager = root.getResourceManager();

        ImageButton button = new ImageButton(resourceManager.loadTexture("/oiiaioiiai-blue.jpg"), ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5))
                .normal().setImageAlignment(ImageAlignment.FILL).getNode()
                .onHover().setTexture(resourceManager.loadTexture("/oiiaioiiai-red.jpg")).getNode()
                .onClick().setTexture(resourceManager.loadTexture("/oiiaioiiai-green.jpg")).getNode()
                .setAnchor(0.5, 0.5)
                .setAngle(20)
                .setDrawBackground(true)
                .background()
                .setCornerRadius(10)
                .getNode();
        Frame blurBox = new Frame(ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.3, 0.3))
                .setAnchor(0.5, 0.5)
                .setAngle(100)
                .background()
                .setColor(new RGBA(210, 230, 255, 0))
                .setBlurStrength(0.7)
                .getNode();

        CustomTweenFunction tweenFunction = new CustomTweenFunction(TweenFunctions.LINEAR)
                .setNestedSegment(0, 0.2, TweenFunctions.BOUNCE_IN_OUT, child1 -> {
                    child1.setNestedSegment(0, 0.8, TweenFunctions.QUINT_IN_OUT, childChild1 -> {
                                childChild1.setNestedSegment(0, 0.5, TweenFunctions.QUINT_IN_OUT, childChildChild1 -> {
                                });
                            })
                            .setNestedSegment(0.8, 1, child1, childChild2 -> {
                            })
                    ;
                })
                .setNestedSegment(0.2, 0.5, TweenFunctions.SINE_IN_OUT, child2 -> {
                })
                .setNestedSegment(0.5, 0.7, TweenFunctions.LINEAR, child3 -> {
                })
                .setNestedSegment(0.7, 1, TweenFunctions.BOUNCE_OUT, child4 -> {
                });

        ScaleOffsetTween scaleOffsetTween = new ScaleOffsetTween(ScaleOffset.ZERO, ScaleOffset.fromScale(1, 1), 1000, tweenFunction, false);
        ScaleOffsetTween scaleOffsetTween1 = new ScaleOffsetTween(ScaleOffset.fromScale(1, 1), ScaleOffset.ZERO, 1000, tweenFunction, false);
        scaleOffsetTween.onComplete(scaleOffsetTween1::play);
        scaleOffsetTween1.onComplete(scaleOffsetTween::play);

        HorizontalBox box = new HorizontalBox(ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5))
                .setAnchor(0.5, 0.5)
                .background()
                .setColor(RGBA.BLUE)
                .getNode();
        Frame frame = new Frame(ScaleOffset.fromScale(0, 0), ScaleOffset.fromScale(0.5, 0.5));
        Frame frame3 = new Frame(ScaleOffset.fromScale(0, 0), ScaleOffset.fromScale(0.5, 0.5)).background().setColor(RGBA.GREEN).getNode();
        TextBox textBox = new TextBox(ScaleOffset.fromScale(0, 0), ScaleOffset.fromScale(0.5, 0.5))
                .setZIndex(100)
                .setDrawBackground(true)
                .background()
                .setColor(RGBA.GRAY)
                .getNode();
        TextLabel label = new TextLabel("11451411 111111111111111111111111111111233\n333333333333\n111111111111111111111111111111111111111111111", ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5))
                .setZIndex(100)
                .setDrawBackground(true)
                .description()
                .setWrap(true)
                .setFontSize(30)
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.TOP)
                .getNode()
                .background()
                .setColor(RGBA.GRAY)
                .getNode();
        box.addChildren(frame, frame3);
        root.addChildren(box, button/*, blurBox*/, textBox, label);
        TweenScheduler tweenScheduler = root.getTweenScheduler();
        tweenScheduler.registerTween(scaleOffsetTween);
        tweenScheduler.registerTween(scaleOffsetTween1);
        scaleOffsetTween.bindSetter(button::setPosition);
        scaleOffsetTween1.bindSetter(button::setPosition);
        scaleOffsetTween.play();

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
// --8<-- [end:classDefinition]