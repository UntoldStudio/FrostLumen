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
import org.lwjgl.system.MemoryUtil;
import top.untold.frostlumen.core.data.*;
import top.untold.frostlumen.core.gui.NodeRoot;
import top.untold.frostlumen.core.gui.Window;
import top.untold.frostlumen.core.gui.node.*;
import top.untold.frostlumen.core.render.ResourceManager;
import top.untold.frostlumen.core.render.RenderProviderType;
import top.untold.frostlumen.core.texture.Texture;
import top.untold.frostlumen.core.tween.CustomTweenFunction;
import top.untold.frostlumen.core.tween.ScaleOffsetTween;
import top.untold.frostlumen.core.tween.TweenFunctions;
import top.untold.frostlumen.core.tween.TweenScheduler;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL32.*;

// --8<-- [start:classDefinition]
public class Main {
    private static final AtomicBoolean shouldClose = new AtomicBoolean(false);

    private static long createWindow(int width, int height, String title, Window shared) {
        return glfwCreateWindow(width, height, title, MemoryUtil.NULL, shared == null ? MemoryUtil.NULL : shared.getWindowHandle());
    }

    public static void main(String[] args) throws InterruptedException {
        if (!glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW");
        }

        long windowHandle1 = createWindow(800, 600, "Test1", null);
        glfwMakeContextCurrent(windowHandle1);
        GL.createCapabilities();
        glClearColor(0.1f, 0.1f, 0.1f, 0.1f);
        Window window1 = Window.from(windowHandle1);
        window1.init(RenderProviderType.OPENGL);
        NodeRoot root1 = window1.getNodeRoot();
        ResourceManager resourceManager1 = root1.getResourceManager();

        long windowHandle2 = createWindow(800, 600, "Test2", window1);
        glfwMakeContextCurrent(windowHandle2);
        glClearColor(0.1f, 0.1f, 0.1f, 0.1f);
        Window window2 = Window.from(windowHandle2);
        window2.init(RenderProviderType.OPENGL);
        NodeRoot root2 = window2.getNodeRoot();
        ResourceManager resourceManager2 = root2.getResourceManager();

        Texture oiiacatBlue = resourceManager1.loadTexture("/oiiaioiiai-blue.jpg");
        Texture oiiacatRed = resourceManager1.loadTexture("/oiiaioiiai-red.jpg");
        Texture oiiacatGreen =  resourceManager1.loadTexture("/oiiaioiiai-green.jpg");

        ImageButton button = new ImageButton(oiiacatBlue, ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5))
                .normal().setImageAlignment(ImageAlignment.FILL).getNode()
                .onHover().setTexture(oiiacatRed).getNode()
                .onClick().setTexture(oiiacatGreen).getNode()
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
                .setCornerRadius(10)
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
        root1.addChildren(box, button, blurBox, textBox, label);
        TweenScheduler tweenScheduler = root1.getTweenScheduler();
        tweenScheduler.registerTween(scaleOffsetTween);
        tweenScheduler.registerTween(scaleOffsetTween1);
        scaleOffsetTween.bindSetter(button::setPosition);
        scaleOffsetTween1.bindSetter(button::setPosition);
        scaleOffsetTween.play();

        ImageLabel window2ImageLabel = new ImageLabel(oiiacatGreen, ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5))
                .setAnchor(0.5, 0.5)
                ;
        TextBox window2TextBox = new TextBox(ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5))
                ;

        root2.addChildren(window2ImageLabel, window2TextBox);

        long previousTime = System.nanoTime();
        int frameCount = 0;

        Window otherWindow = runOtherWindowInOtherThread(800, 600, "Test3");
        Thread thread = otherWindow.getRenderThread();

        glfwSwapInterval(0);

        while (!shouldClose.get() && !glfwWindowShouldClose(windowHandle1) && !glfwWindowShouldClose(windowHandle2)) {
            glfwPollEvents();

            glfwMakeContextCurrent(windowHandle1);
            glClear(GL_COLOR_BUFFER_BIT);
            root1.render();
            glfwSwapBuffers(windowHandle1);

            glfwMakeContextCurrent(windowHandle2);
            glClear(GL_COLOR_BUFFER_BIT);
            root2.render();
            glfwSwapBuffers(windowHandle2);

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

        shouldClose.set(true);

        thread.join();

        resourceManager1.unloadTexture(oiiacatBlue, oiiacatRed, oiiacatGreen);

        window1.close();
        window2.close();
        otherWindow.close();

        glfwTerminate();
    }

    private static Window runOtherWindowInOtherThread(int width, int height, String title) {
        long handle = createWindow(width, height, title, null);
        Window window = Window.from(handle);
        Thread thread = new Thread(() -> {
            glfwMakeContextCurrent(handle);
            GL.createCapabilities();
            window.init(RenderProviderType.OPENGL);
            NodeRoot root = window.getNodeRoot();

            TextLabel label = new TextLabel("Hello from other thread!", ScaleOffset.fromScale(0.5, 0.5), ScaleOffset.fromScale(0.5, 0.5));
            TextBox box = new TextBox(ScaleOffset.fromScale(0, 0), ScaleOffset.fromScale(0.5, 0.5)).setZIndex(100);

            ScaleOffsetTween tween1 = new ScaleOffsetTween(ScaleOffset.ZERO, ScaleOffset.fromScale(1, 1), 10000L, TweenFunctions.LINEAR, false);
            ScaleOffsetTween tween2 = new ScaleOffsetTween(ScaleOffset.fromScale(1, 1), ScaleOffset.ZERO, 10000L, TweenFunctions.LINEAR, false);

            tween1.bindSetter(label::setPosition);
            tween2.bindSetter(label::setPosition);

            tween1.onComplete(tween2::play);
            tween2.onComplete(tween1::play);

            root.addChildren(label, box);

            root.getTweenScheduler().registerTweens(tween1, tween2);

            tween1.play();

            glClearColor(0.1f, 0.1f, 0.1f, 0.1f);

            while (!shouldClose.get()) {
                if (glfwWindowShouldClose(handle)) {
                    root.runAllTasksOnCurrentThread();
                }
                glClear(GL_COLOR_BUFFER_BIT);
                root.render();
                glfwSwapBuffers(handle);
                if (glfwWindowShouldClose(handle)) {
                    break;
                }
            }
            shouldClose.set(true);
        });
        thread.start();
        return window;
    }
}
// --8<-- [end:classDefinition]