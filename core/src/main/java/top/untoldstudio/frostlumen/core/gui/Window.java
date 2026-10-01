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
package top.untoldstudio.frostlumen.core.gui;

import org.lwjgl.glfw.*;
import top.untoldstudio.frostlumen.core.data.InputAction;
import top.untoldstudio.frostlumen.core.data.InputModifiers;
import top.untoldstudio.frostlumen.core.data.Key;
import top.untoldstudio.frostlumen.core.data.MouseButton;
import top.untoldstudio.frostlumen.core.event.*;
import top.untoldstudio.frostlumen.core.render.GuiRender;
import top.untoldstudio.frostlumen.core.render.RenderProviderType;
import top.untoldstudio.frostlumen.core.render.provider.OpenGLGuiRender;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.glfw.GLFW.*;

public class Window {
    private static final Map<Long, Window> WINDOW_MAP = new ConcurrentHashMap<>();
    private final GLFWKeyCallback keyCallback;
    private final GLFWMouseButtonCallback mouseButtonCallback;
    private final GLFWCursorPosCallback mouseMoveCallback;
    private final GLFWScrollCallback mouseScrollCallback;
    private final GLFWCharCallback userInputTextCallback;
    private final GLFWCursorEnterCallback mouseEnterCallback;
    private final GLFWWindowSizeCallback windowSizeChangeCallback;
    private final GLFWFramebufferSizeCallback frameBufferSizeChangeCallback;
    private final GLFWWindowCloseCallback windowCloseCallback;
    private final GLFWWindowFocusCallback windowFocusCallback;
    private final GLFWDropCallback dropCallback;
    private final GLFWWindowIconifyCallback minimizeCallback;
    private final GLFWWindowMaximizeCallback maximizeCallback;
    private final GLFWWindowPosCallback windowMoveCallback;
    private GLFWKeyCallback oldKeyCallback;
    private GLFWMouseButtonCallback oldMouseButtonCallback;
    private GLFWCursorPosCallback oldMouseMoveCallback;
    private GLFWScrollCallback oldMouseScrollCallback;
    private GLFWCharCallback oldUserInputTextCallback;
    private GLFWCursorEnterCallback oldMouseEnterCallback;
    private GLFWWindowSizeCallback oldWindowSizeChangeCallback;
    private GLFWFramebufferSizeCallback oldFrameBufferSizeChangeCallback;
    private GLFWWindowCloseCallback oldWindowCloseCallback;
    private GLFWWindowFocusCallback oldWindowFocusCallback;
    private GLFWDropCallback oldDropCallback;
    private GLFWWindowIconifyCallback oldMinimizeCallback;
    private GLFWWindowMaximizeCallback oldMaximizeCallback;
    private GLFWWindowPosCallback oldWindowMoveCallback;

    private int windowWidth;
    private int windowHeight;
    private int frameBufferWidth;
    private int frameBufferHeight;
    private int windowX;
    private int windowY;
    private double mouseX = -1;
    private double mouseY = -1;
    private boolean mouseInWindow;
    private ParentNode<?> focusNode;
    private boolean mouseClick;
    private boolean focus;
    private boolean maximize;
    private boolean minimize;

    private final NodeRoot root;
    private final long windowHandle;

    public static Window get(long windowHandle) {
        return WINDOW_MAP.get(windowHandle);
    }
    public static Window from(long windowHandle, RenderProviderType type) {
        return WINDOW_MAP.computeIfAbsent(windowHandle, (key -> new Window(windowHandle, switch (type) {
            case OPENGL -> new OpenGLGuiRender(windowHandle);
        })));
    }

    public NodeRoot getNodeRoot() {
        return root;
    }

    public void close() {
        glfwDestroyWindow(windowHandle);
        clear();
    }
    public void clear() {
        WindowCloseEvent closeEvent = new WindowCloseEvent();
        root.dispatchWindowCloseEvent(closeEvent);

        glfwSetKeyCallback(windowHandle, oldKeyCallback);
        glfwSetMouseButtonCallback(windowHandle, oldMouseButtonCallback);
        glfwSetCursorPosCallback(windowHandle, oldMouseMoveCallback);
        glfwSetScrollCallback(windowHandle, oldMouseScrollCallback);
        glfwSetCharCallback(windowHandle, oldUserInputTextCallback);
        glfwSetCursorEnterCallback(windowHandle, oldMouseEnterCallback);
        glfwSetWindowSizeCallback(windowHandle, oldWindowSizeChangeCallback);
        glfwSetFramebufferSizeCallback(windowHandle, oldFrameBufferSizeChangeCallback);
        glfwSetWindowCloseCallback(windowHandle, oldWindowCloseCallback);
        glfwSetWindowFocusCallback(windowHandle, oldWindowFocusCallback);
        glfwSetDropCallback(windowHandle, oldDropCallback);
        glfwSetWindowIconifyCallback(windowHandle, oldMinimizeCallback);
        glfwSetWindowMaximizeCallback(windowHandle, oldMaximizeCallback);
        glfwSetWindowPosCallback(windowHandle, oldWindowMoveCallback);

        keyCallback.free();
        mouseButtonCallback.free();
        mouseMoveCallback.free();
        mouseScrollCallback.free();
        userInputTextCallback.free();
        mouseEnterCallback.free();
        windowSizeChangeCallback.free();
        frameBufferSizeChangeCallback.free();
        windowCloseCallback.free();
        windowFocusCallback.free();
        dropCallback.free();
        minimizeCallback.free();
        maximizeCallback.free();
        windowMoveCallback.free();
    }

    private Window(long windowHandle, GuiRender render) {
        this.windowHandle = windowHandle;

        mouseInWindow = glfwGetWindowAttrib(windowHandle, GLFW_HOVERED) == GLFW_TRUE;
        focus = glfwGetWindowAttrib(windowHandle, GLFW_FOCUSED) == GLFW_TRUE;
        int[] widthArray = new int[1];
        int[] heightArray = new int[1];
        glfwGetWindowSize(windowHandle, widthArray, heightArray);
        this.windowWidth = widthArray[0];
        this.windowHeight = heightArray[0];
        glfwGetFramebufferSize(windowHandle, widthArray, heightArray);
        frameBufferWidth = widthArray[0];
        frameBufferHeight = heightArray[0];

        root = new NodeRoot(this, render);

        focusNode = root;

        keyCallback = GLFWKeyCallback.create(((currentWindow, key, scancode, action, modifiers) -> {
            KeyEvent event = new KeyEvent(Key.fromGLFWValue(key), InputAction.fromGLFWValue(action), InputModifiers.fromGLFWValue(modifiers));
            root.dispatchKeyEvent(event);
            if (oldKeyCallback != null && !event.isCancel()) {
                oldKeyCallback.invoke(currentWindow, key, scancode, action, modifiers);
            }
        }));
        mouseButtonCallback = GLFWMouseButtonCallback.create(((currentWindow, button, action, modifiers) -> {
            MouseButtonEvent event = new MouseButtonEvent(MouseButton.fromGLFWValue(button), InputAction.fromGLFWValue(action), InputModifiers.fromGLFWValue(modifiers));
            mouseClick = event.getAction() != InputAction.RELEASE;
            root.dispatchMouseButtonEvent(event);
            if (oldMouseButtonCallback != null && !event.isCancel()) {
                oldMouseButtonCallback.invoke(currentWindow, button, action, modifiers);
            }
        }));
        mouseMoveCallback = GLFWCursorPosCallback.create((currentWindow, x, y) -> {
            x = x * (double)frameBufferWidth / windowWidth;
            y = y * (double)frameBufferHeight / windowHeight;

            double xDelta = mouseX == -1 ? 0 : x - mouseX;
            double yDelta = mouseY == -1 ? 0 : y - mouseY;
            mouseX = x;
            mouseY = y;
            MouseMoveEvent event = new MouseMoveEvent(x, y, xDelta, yDelta);
            root.dispatchMouseMoveEvent(event);
            if (oldMouseMoveCallback != null && !event.isCancel()) {
                oldMouseMoveCallback.invoke(currentWindow, x, y);
            }
        });
        mouseScrollCallback = GLFWScrollCallback.create(((currentWindow, xDelta, yDelta) -> {
            MouseScrollEvent event = new MouseScrollEvent(mouseX, mouseY, xDelta, yDelta);
            root.dispatchMouseScrollEvent(event);
            if (oldMouseScrollCallback != null && !event.isCancel()) {
                oldMouseScrollCallback.invoke(currentWindow, xDelta, yDelta);
            }
        }));
        userInputTextCallback = GLFWCharCallback.create((currentWindow, codepoint) -> {
            UserInputTextEvent event = new UserInputTextEvent(new String(Character.toChars(codepoint)));
            root.dispatchUserInputTextEvent(event);
            if (oldUserInputTextCallback != null && !event.isCancel()) {
                oldUserInputTextCallback.invoke(currentWindow, codepoint);
            }
        });
        mouseEnterCallback = GLFWCursorEnterCallback.create((currentWindow, is) -> {
            mouseInWindow = is;
            if (is) {
                root.dispatchMouseEnterWindowEvent(new MouseEnterWindowEvent());
            } else {
                root.dispatchMouseLeaveWindowEvent(new MouseLeaveWindowEvent());
            }
            mouseX = -1;
            mouseY = -1;
            if (oldMouseEnterCallback != null) {
                oldMouseEnterCallback.invoke(currentWindow, is);
            }
        });
        windowSizeChangeCallback = GLFWWindowSizeCallback.create((currentWindow, width, height) -> {
            this.windowWidth = width;
            this.windowHeight = height;
            if (oldWindowSizeChangeCallback != null) {
                oldWindowSizeChangeCallback.invoke(currentWindow, width, height);
            }
        });
        frameBufferSizeChangeCallback = GLFWFramebufferSizeCallback.create((currentWindow, width, height) -> {
            FrameBufferSizeChangeEvent event = new FrameBufferSizeChangeEvent(frameBufferWidth, frameBufferHeight, width, height);
            frameBufferWidth = width;
            frameBufferHeight = height;
            root.dispatchFrameBufferSizeChangeEvent(event);
            if (oldFrameBufferSizeChangeCallback != null) {
                oldFrameBufferSizeChangeCallback.invoke(currentWindow, width, height);
            }
        });
        windowCloseCallback = GLFWWindowCloseCallback.create((currentWindow) -> {
            UserRequestWindowCloseEvent requestEvent = new UserRequestWindowCloseEvent();
            root.dispatchUserRequestWindowCloseEvent(requestEvent);
            if (!requestEvent.isCancel()) {
                if (oldWindowCloseCallback != null) {
                    oldWindowCloseCallback.invoke(currentWindow);
                }
                if (glfwWindowShouldClose(currentWindow)) {
                    WindowCloseEvent closeEvent = new WindowCloseEvent();
                    root.dispatchWindowCloseEvent(closeEvent);
                }
            } else {
                glfwSetWindowShouldClose(currentWindow, false);
            }
        });
        windowFocusCallback = GLFWWindowFocusCallback.create((currentWindow, focus) -> {
            this.focus = focus;
            WindowFocusChangeEvent event = new WindowFocusChangeEvent(focus);
            root.dispatchWindowFocusChangeEvent(event);
            if (oldWindowFocusCallback != null) {
                oldWindowFocusCallback.invoke(currentWindow, focus);
            }
        });
        dropCallback = GLFWDropCallback.create((currentWindow, count, names) -> {
            Path[] filePaths = new Path[count];
            for (int i = 0; i < count; i++) {
                filePaths[i] = Path.of(GLFWDropCallback.getName(names, i));
            }
            UserDropFilesEvent event = new UserDropFilesEvent(filePaths);
            root.dispatchUserDropFilesEvent(event);
            if (oldDropCallback != null) {
                oldDropCallback.invoke(currentWindow, count, names);
            }
        });
        minimizeCallback = GLFWWindowIconifyCallback.create((currentWindow, is) -> {
            minimize = is;
            WindowMinimizeEvent event = new WindowMinimizeEvent();
            root.dispatchWindowMinimizeEvent(event);
            if (oldMinimizeCallback != null) {
                oldMinimizeCallback.invoke(currentWindow, is);
            }
        });
        maximizeCallback = GLFWWindowMaximizeCallback.create((currentWindow, is) -> {
            maximize = is;
            WindowMaximizeEvent event = new WindowMaximizeEvent();
            root.dispatchWindowMaximizeEvent(event);
            if (oldMaximizeCallback != null) {
                oldMaximizeCallback.invoke(currentWindow, is);
            }
        });
        windowMoveCallback = GLFWWindowPosCallback.create((currentWindow, x, y) -> {
            WindowMoveEvent event = new WindowMoveEvent(windowX, windowY, x, y);
            windowX = x;
            windowY = y;
            root.dispatchWindowMoveEvent(event);
            if (oldWindowMoveCallback != null) {
                oldWindowMoveCallback.invoke(currentWindow, windowX, windowY);
            }
        });
        oldKeyCallback = glfwSetKeyCallback(windowHandle, keyCallback);
        oldMouseButtonCallback = glfwSetMouseButtonCallback(windowHandle, mouseButtonCallback);
        oldMouseMoveCallback = glfwSetCursorPosCallback(windowHandle, mouseMoveCallback);
        oldMouseScrollCallback = glfwSetScrollCallback(windowHandle, mouseScrollCallback);
        oldUserInputTextCallback = glfwSetCharCallback(windowHandle, userInputTextCallback);
        oldMouseEnterCallback = glfwSetCursorEnterCallback(windowHandle, mouseEnterCallback);
        oldWindowSizeChangeCallback = glfwSetWindowSizeCallback(windowHandle, windowSizeChangeCallback);
        oldFrameBufferSizeChangeCallback = glfwSetFramebufferSizeCallback(windowHandle, frameBufferSizeChangeCallback);
        oldWindowCloseCallback = glfwSetWindowCloseCallback(windowHandle, windowCloseCallback);
        oldWindowFocusCallback = glfwSetWindowFocusCallback(windowHandle, windowFocusCallback);
        oldDropCallback = glfwSetDropCallback(windowHandle, dropCallback);
        oldMinimizeCallback = glfwSetWindowIconifyCallback(windowHandle, minimizeCallback);
        oldMaximizeCallback = glfwSetWindowMaximizeCallback(windowHandle, maximizeCallback);
        oldWindowMoveCallback = glfwSetWindowPosCallback(windowHandle, windowMoveCallback);
    }

    public boolean isMouseInRange(int minX, int minY, int maxX, int maxY) {
        int oldMinX = minX;
        int oldMinY = minY;

        minX = Math.min(oldMinX, maxX);
        maxX = Math.max(oldMinX, maxX);

        minY = Math.min(oldMinY, maxY);
        maxY = Math.max(oldMinY, maxY);

        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    public void setFocusNode(ParentNode<?> focus) {
        this.focusNode = focus;
    }
    public ParentNode<?> getFocusNode() {
        return focusNode;
    }

    public boolean isMouseInWindow() {
        return mouseInWindow;
    }
    public double getMouseX() {
        return mouseX;
    }
    public double getMouseY() {
        return mouseY;
    }
    public int getFrameBufferWidth() {
        return frameBufferWidth;
    }
    public int getFrameBufferHeight() {
        return frameBufferHeight;
    }
    public long getWindowHandle() {
        return windowHandle;
    }
    public boolean isFocus() {
        return focus;
    }
    public boolean isIconify() {
        return minimize;
    }
    public boolean isMaximize() {
        return maximize;
    }
    public boolean isMouseClick() {
        return mouseClick;
    }
}
