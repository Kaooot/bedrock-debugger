package dev.kaooot.debugger.imgui;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.imgui.renderer.ImGuiRenderer;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.DoubleBuffer;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class ImGuiAdapter {

    private static final long IDLE_FRAME_SLEEP_MS = 66L;
    private static final String WINDOW_TITLE = "Bedrock Debugger Overlay";
    private static final String RELEASE_TITLE = "Minecraft";
    private static final String PREVIEW_TITLE = "Minecraft Preview";

    private final BedrockDebuggerProxy proxy;
    private ImGuiImplGlfw imGuiImplGlfw;
    private ImGuiImplGl3 imGuiImplGl3 = new ImGuiImplGl3();

    private long mainWindowHandle;
    private boolean mainRenderingToggle = false;

    private final Map<String, ImGuiWindowSizeAndPos> windows = new Object2ObjectOpenHashMap<>();

    private final DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
    private final DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);

    private boolean initialized = false;

    private CompletableFuture<Void> renderThread;

    public void init() {
        this.proxy.getLogger().debug("Initialize ImGui Adapter");
        this.renderThread = CompletableFuture.runAsync(() -> {
            this.initGlFW();
            this.initImGui();
            this.initialized = true;
            this.runRenderLoop();
            this.shutdown();
        });
    }

    public void reinit() {
        GLFW.glfwSetWindowShouldClose(this.mainWindowHandle, true);
        this.renderThread.whenComplete((unused, throwable) -> this.init());
    }

    public void stop() {
        if (!this.initialized || this.renderThread == null) {
            return;
        }
        GLFW.glfwSetWindowShouldClose(this.mainWindowHandle, true);
        try {
            this.renderThread.join();
        } catch (CompletionException | CancellationException e) {
            this.proxy.getLogger().error("Failed to stop ImGui render thread", e);
        }
    }

    public void shutdown() {
        if (!this.initialized) {
            return;
        }
        this.initialized = false;
        this.imGuiImplGl3.shutdown();
        this.imGuiImplGlfw.shutdown();

        ImGui.destroyContext();

        GLFW.glfwDestroyWindow(this.mainWindowHandle);
        GLFW.glfwTerminate();
    }

    public void toggle() {
        WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        if (hwnd == null) {
            this.focusOverlay();
            hwnd = User32.INSTANCE.GetForegroundWindow();
        }
        final long handle = Pointer.nativeValue(hwnd.getPointer());

        if (handle == this.getWindowHandle(this.getGameWindowTitle()) &&
            !this.mainRenderingToggle) {
            this.mainRenderingToggle = true;
        } else if (this.mainRenderingToggle) {
            this.mainRenderingToggle = false;
            this.focusGameWindow();
        }
    }

    public boolean isGameWindowFocused() {
        return Pointer.nativeValue(User32.INSTANCE.GetForegroundWindow().getPointer()) ==
            this.getWindowHandle(this.getGameWindowTitle());
    }

    public void trackWindow(String id) {
        this.windows.put(id,
            new ImGuiWindowSizeAndPos(ImGui.getWindowPos(), ImGui.getWindowSize()));
    }

    public void removeWindow(String id) {
        this.windows.remove(id);
    }

    @Value
    private static class ImGuiWindowSizeAndPos {
        ImVec2 windowPos;
        ImVec2 windowSize;
    }

    private void initGlFW() {
        GLFW.glfwInit();
        GLFW.glfwWindowHint(GLFW.GLFW_FLOATING, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);

        final WinDef.RECT rect = this.getGameWindowRect();
        final int width = rect.right - rect.left;
        final int height = rect.bottom - rect.top;

        this.mainWindowHandle = GLFW.glfwCreateWindow(width, height, WINDOW_TITLE, 0, 0);

        GLFW.glfwSetWindowAttrib(
            this.mainWindowHandle,
            GLFW.GLFW_DECORATED,
            GLFW.GLFW_FALSE
        );
        GLFW.glfwSetWindowPos(this.mainWindowHandle, rect.left, rect.top);
        GLFW.glfwMakeContextCurrent(this.mainWindowHandle);
        GLFW.glfwSwapInterval(1);

        GL.createCapabilities();
        GL11.glClearColor(.0f, .0f, .0f, .0f);
    }

    private void initImGui() {
        ImGui.createContext();
        ImGui.getStyle().setAlpha(1.0f);
        ImGui.getStyle().getColor(ImGuiCol.WindowBg).w = 0.0f;
        ImGui.getStyle().setWindowRounding(5.0f);
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);

        this.imGuiImplGlfw = new ImGuiImplGlfw();
        this.imGuiImplGl3 = new ImGuiImplGl3();
        this.imGuiImplGlfw.init(this.mainWindowHandle, true);
        this.imGuiImplGl3.init("#version 150");

        GLFW.glfwShowWindow(this.mainWindowHandle);
    }

    private void runRenderLoop() {
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_TRUE);
        while (!GLFW.glfwWindowShouldClose(this.mainWindowHandle)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            this.render();

            GLFW.glfwSwapBuffers(this.mainWindowHandle);
            GLFW.glfwPollEvents();

            if (!this.mainRenderingToggle) {
                try {
                    Thread.sleep(IDLE_FRAME_SLEEP_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void render() {
        if (!this.mainRenderingToggle) {
            GLFW.glfwSetWindowAttrib(
                this.mainWindowHandle,
                GLFW.GLFW_MOUSE_PASSTHROUGH,
                GLFW.GLFW_TRUE
            );
            return;
        }

        this.imGuiImplGlfw.newFrame();
        this.imGuiImplGl3.newFrame();
        ImGui.newFrame();

        this.updatePosAndSize();

        final ImGuiRendererRegistry registry = Registries.getRegistry(RegistryKey.IMGUI_RENDERER);

        for (final ImGuiRenderer renderer : registry.getValues()) {
            renderer.render(this.proxy, this);
        }

        final ImVec2 cursorPos = this.getMousePos();
        final boolean isInWindow = this.isInImGuiWindow(cursorPos);

        GLFW.glfwSetWindowAttrib(
            this.mainWindowHandle,
            GLFW.GLFW_MOUSE_PASSTHROUGH,
            isInWindow ? GLFW.GLFW_FALSE : GLFW.GLFW_TRUE
        );

        ImGui.render();

        this.imGuiImplGl3.renderDrawData(ImGui.getDrawData());
        ImGui.updatePlatformWindows();
    }

    private boolean isInImGuiWindow(ImVec2 cursorPos) {
        for (final ImGuiWindowSizeAndPos value : this.windows.values()) {
            if (cursorPos.x >= value.getWindowPos().x &&
                cursorPos.x <= (value.getWindowPos().x + value.getWindowSize().x) &&
                cursorPos.y >= value.getWindowPos().y &&
                cursorPos.y <= (value.getWindowPos().y + value.getWindowSize().y)) {
                return true;
            }
        }
        return false;
    }

    private ImVec2 getMousePos() {
        GLFW.glfwGetCursorPos(this.mainWindowHandle, this.xBuffer, this.yBuffer);
        return new ImVec2((float) this.xBuffer.get(0), (float) this.yBuffer.get(0));
    }

    private void updatePosAndSize() {
        final WinDef.RECT rect = this.getGameWindowRect();
        final int width = rect.right - rect.left;
        final int height = rect.bottom - rect.top;

        GLFW.glfwSetWindowSize(this.mainWindowHandle, width, height);
        GLFW.glfwSetWindowPos(this.mainWindowHandle, rect.left, rect.top);
    }

    private WinDef.HWND getWindow(String windowTitle) {
        final WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
        if (hwnd == null) {
            throw new IllegalStateException("Could not find window for title: " + windowTitle);
        }
        return hwnd;
    }

    private WinDef.HWND getGameWindow() {
        return this.getWindow(this.getGameWindowTitle());
    }

    private WinDef.RECT getGameWindowRect() {
        final WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(this.getGameWindow(), rect);
        return rect;
    }

    private void focusGameWindow() {
        this.focusWindow(this.getGameWindow());
    }

    private void focusOverlay() {
        this.focusWindow(this.getWindow(WINDOW_TITLE));
    }

    private void focusWindow(WinDef.HWND hwnd) {
        final boolean result = User32.INSTANCE.SetForegroundWindow(hwnd);
        if (!result) {
            throw new IllegalStateException("Failed to focus window.");
        }
    }

    private long getWindowHandle(String windowTitle) {
        return Pointer.nativeValue(this.getWindow(windowTitle).getPointer());
    }

    private String getGameWindowTitle() {
        return this.proxy.getPlayer().getLoginData().isPreview() ? PREVIEW_TITLE :
            RELEASE_TITLE;
    }
}