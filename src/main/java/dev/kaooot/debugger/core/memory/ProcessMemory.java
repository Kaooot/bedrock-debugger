package dev.kaooot.debugger.core.memory;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD.SIZE_T;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.MEMORY_BASIC_INFORMATION;
import com.sun.jna.ptr.IntByReference;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public final class ProcessMemory implements Closeable {

    private static final int PROCESS_VM_READ = 0x0010;
    private static final int PROCESS_QUERY_INFORMATION = 0x0400;

    private static final int MEM_COMMIT = 0x1000;
    private static final int PAGE_GUARD = 0x100;
    private static final int READABLE = 0x66;

    public static final long MIN_POINTER = 0x10000L;

    private final HANDLE handle;
    private final List<Region> regions;

    private final Memory scratch = new Memory(8L << 20);
    private final IntByReference readCount = new IntByReference();
    private final byte[] pointerBytes = new byte[8];

    private ProcessMemory(final HANDLE handle) {
        this.handle = handle;
        this.regions = this.readRegions();
    }

    public static ProcessMemory open(final long pid) {
        final HANDLE handle = Kernel32.INSTANCE.OpenProcess(
            PROCESS_VM_READ | PROCESS_QUERY_INFORMATION, false, (int) pid);
        if (handle == null) {
            throw new IllegalStateException(
                "OpenProcess failed for pid " + pid + " (error " + Native.getLastError() + ")");
        }
        return new ProcessMemory(handle);
    }

    public List<Region> regions() {
        return this.regions;
    }

    private List<Region> readRegions() {
        final List<Region> result = new ArrayList<>();
        long address = 0;
        while (address < 0x7FFFFFFF0000L) {
            final MEMORY_BASIC_INFORMATION info = new MEMORY_BASIC_INFORMATION();
            final SIZE_T written = Kernel32.INSTANCE.VirtualQueryEx(
                this.handle, new Pointer(address), info, new SIZE_T(info.size()));
            if (written.longValue() == 0) {
                break;
            }
            final long base = Pointer.nativeValue(info.baseAddress);
            final long size = info.regionSize.longValue();
            if (size <= 0) {
                break;
            }
            final int state = info.state.intValue();
            final int protect = info.protect.intValue();
            final boolean readable = (protect & READABLE) != 0 && (protect & PAGE_GUARD) == 0;
            if (state == MEM_COMMIT && readable) {
                result.add(new Region(base, size));
            }
            address = base + size;
        }
        return result;
    }

    public boolean isMapped(final long address) {
        int low = 0;
        int high = this.regions.size() - 1;
        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final Region region = this.regions.get(mid);
            if (address < region.getBase()) {
                high = mid - 1;
            } else if (address >= region.end()) {
                low = mid + 1;
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean tryRead(final long address, final byte[] buffer, final int length) {
        try {
            final Memory target = length <= this.scratch.size() ? this.scratch : new Memory(length);
            final boolean ok = Kernel32.INSTANCE.ReadProcessMemory(
                this.handle, new Pointer(address), target, length, this.readCount);
            if (!ok || this.readCount.getValue() != length) {
                return false;
            }
            target.read(0, buffer, 0, length);
            return true;
        } catch (final RuntimeException exception) {
            return false;
        }
    }

    public byte[] read(final long address, final int length) {
        final byte[] buffer = new byte[length];
        return this.tryRead(address, buffer, length) ? buffer : null;
    }

    public int readClipped(final long address, final byte[] buffer, final int length) {
        int low = 0;
        int high = this.regions.size() - 1;
        int found = -1;
        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final Region region = this.regions.get(mid);
            if (address < region.getBase()) {
                high = mid - 1;
            } else if (address >= region.end()) {
                low = mid + 1;
            } else {
                found = mid;
                break;
            }
        }
        if (found < 0) {
            return 0;
        }
        long end = this.regions.get(found).end();
        for (int i = found + 1; i < this.regions.size() && this.regions.get(i).getBase() == end; i++) {
            end = this.regions.get(i).end();
        }
        final int clipped = (int) Math.min(length, end - address);
        return clipped > 0 && this.tryRead(address, buffer, clipped) ? clipped : 0;
    }

    public long readPointer(final long address) {
        return this.tryRead(address, this.pointerBytes, 8) ? getLong(this.pointerBytes, 0) : 0;
    }

    public void forEachChunk(final int windowSize, final int overlap,
                             final ChunkConsumer consumer) {
        final byte[] window = new byte[windowSize];
        for (final Region region : this.regions) {
            final long step = windowSize - overlap;
            for (long at = region.getBase(); at < region.end(); at += step) {
                final int length = (int) Math.min(windowSize, region.end() - at);
                if (length < overlap + 1) {
                    break;
                }
                if (this.tryRead(at, window, length)) {
                    consumer.accept(at, window, length);
                }
                if (length < windowSize) {
                    break;
                }
            }
        }
    }

    public static long getLong(final byte[] buffer, final int offset) {
        long value = 0;
        for (int i = 7; i >= 0; i--) {
            value = (value << 8) | (buffer[offset + i] & 0xFFL);
        }
        return value;
    }

    public static int getInt(final byte[] buffer, final int offset) {
        return (buffer[offset] & 0xFF)
            | (buffer[offset + 1] & 0xFF) << 8
            | (buffer[offset + 2] & 0xFF) << 16
            | (buffer[offset + 3] & 0xFF) << 24;
    }

    @Override
    public void close() {
        Kernel32.INSTANCE.CloseHandle(this.handle);
    }

    @Value
    public static class Region {

        long base;
        long size;

        public long end() {
            return this.base + this.size;
        }
    }

    @FunctionalInterface
    public interface ChunkConsumer {

        void accept(long base, byte[] buffer, int length);
    }
}