package dev.kaooot.debugger.util;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.experimental.UtilityClass;
import org.cloudburstmc.math.vector.Vector2i;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@UtilityClass
public class WindowHelper {

    private static final String WINDOW_TITLE = "Minecraft";
    private static final String WINDOW_TITLE_PREVIEW = "Minecraft Preview";

    public int findProcessId(boolean isPreview) {
        final AtomicInteger resultId = new AtomicInteger(-1);
        User32.INSTANCE.EnumWindows((hwnd, pointer) -> {
            final int bufferLength = 512;
            final char[] buffer = new char[bufferLength];
            User32.INSTANCE.GetWindowText(hwnd, buffer, bufferLength);
            final String title = Native.toString(buffer);
            if (isPreview && title.contains(WINDOW_TITLE_PREVIEW) ||
                (!isPreview && title.contains(WINDOW_TITLE))) {
                final IntByReference pid = new IntByReference();
                User32.INSTANCE.GetWindowThreadProcessId(hwnd, pid);
                resultId.set(pid.getValue());
                return false;
            }
            return true;
        }, null);
        return resultId.get();
    }

    public Vector2i getRes(WinDef.HWND hwnd) {
        final WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(hwnd, rect);
        final int width = rect.right - rect.left;
        final int height = rect.bottom - rect.top;
        return Vector2i.from(width, height);
    }

    public WinDef.HWND getHWND(boolean isPreview) {
        final WinDef.HWND hwnd = User32.INSTANCE.FindWindow(
            null,
            isPreview ? WINDOW_TITLE_PREVIEW : WINDOW_TITLE
        );
        if (hwnd == null) {
            throw new IllegalStateException("Failed to find game window");
        }
        return hwnd;
    }
}