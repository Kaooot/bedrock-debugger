package dev.kaooot.debugger.util;

import com.sun.jna.platform.win32.WinDef;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.protocol.bedrock.data.GraphicsMode;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.screen.DebugScreen;
import dev.kaooot.debugger.screen.DebugScreenRegistry;
import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class DebugScreenInfo {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd'T'HH:mm:ss"
    );

    private final BedrockDebuggerProxy proxy;

    @Getter
    @Setter
    private DebugElement lastElement;

    @Getter
    private int selectedIndex;

    private final SystemInfo systemInfo = new SystemInfo();
    private final OperatingSystem operatingSystem = this.systemInfo.getOperatingSystem();
    private final OperatingSystem.OSVersionInfo versionInfo = this.operatingSystem.getVersionInfo();
    private final HardwareAbstractionLayer hardware = this.systemInfo.getHardware();

    private String graphicsCardName;
    private String osVersion;

    private int processId = -1;
    private WinDef.HWND hwnd;
    private long maxMemValueSeen = 0L;

    public void setSelectedIndex(int selectedIndex) {
        final DebugScreenRegistry registry = Registries.getRegistry(RegistryKey.DEBUG_SCREEN);
        final DebugScreen previous = registry.getValue(
            selectedIndex > 0 ? selectedIndex - 1 :
                this.selectedIndex == -1 ? 0 : this.selectedIndex
        );
        if (selectedIndex <= 0) {
            final SettingsConfig settingsConfig = Registries.
                <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
                .get(SettingsConfig.class);
            if (settingsConfig.isEnableExperimentalImGui()) {
                this.proxy.getImGuiAdapter().toggle();
            }
        }
        this.proxy.getServer().sendDebugInfos(
            previous.getElement(),
            Collections.emptyList()
        );
        this.selectedIndex = selectedIndex;
    }

    public void startTicking() {
        if (!this.proxy.isLoadPacks()) {
            this.proxy.getLogger().info(
                "Debug Screen will not be ticked because packs are disabled"
            );
            return;
        }
        this.proxy.getServer().sendDebugInfo(DebugElement.HIDE, "Toggle");
        final String defaultGraphicsCardName = this.hardware.getGraphicsCards().isEmpty() ? "" :
            this.hardware.getGraphicsCards().stream()
                .sorted(Comparator.comparingLong(GraphicsCard::getVRam))
                .toList()
                .get(this.hardware.getGraphicsCards().size() - 1)
                .getName();
        final DebugScreenRegistry registry = Registries.getRegistry(RegistryKey.DEBUG_SCREEN);
        final Int2ObjectMap<DebugScreen> screens = new Int2ObjectOpenHashMap<>();
        for (final int key : registry.getKeys()) {
            screens.put(key, registry.getValue(key));
        }
        this.graphicsCardName = defaultGraphicsCardName;
        this.osVersion = "10.0." + this.versionInfo.getBuildNumber() + "." + this.getUBR();
        final boolean isPreview = this.proxy.getPlayer().getLoginData().isPreview();
        this.processId = WindowHelper.findProcessId(isPreview);
        this.hwnd = WindowHelper.getHWND(isPreview);
        this.proxy.getScheduler().schedule((() -> {
            if (!this.proxy.getPlayer().isDiagnosticsEnabled()) {
                return;
            }
            if (this.selectedIndex == -1) {
                return;
            }
            final DebugScreen screen = screens.get(this.selectedIndex);
            screen.render(this.proxy);
        }), 2);
    }

    public String getBuildInfo(SettingsConfig settingsConfig) {
        long memoryValue = 0L;
        if (this.processId != -1) {
            final OSProcess process = this.operatingSystem.getProcess(this.processId);
            if (process != null) {
                memoryValue = process.getResidentSetSize() / 1048576;
                if (this.maxMemValueSeen < memoryValue) {
                    this.maxMemValueSeen = memoryValue;
                }
            }
        }

        final String firstLine = (!this.proxy.getPlayer().getLoginData().isPreview() ?
            "v" : "") + this.getMinecraftVersion() +
            " [RENDERDRAGON] D3D12+" +
            (this.proxy.getPlayer().getGraphicsMode().equals(GraphicsMode.ADVANCED) ? " PBR" : "")
            + " D3D_SM65 (cli-p" + this.proxy.getPlayer().getLoginData().getClientNetworkVersion() +
            ", s12345) Windows GDK Build, " + this.graphicsCardName +
            ", " +
            this.osVersion;
        final Vector2i res = WindowHelper.getRes(this.hwnd);
        final String secondLine = "FPS: " + this.proxy.getPlayer().getDiagnostics().getAvgFps() +
            ", " +
            "SrvTime:" + 0.0f +
            ", " +
            "Mem:" + memoryValue +
            ", " +
            "Max:" + this.maxMemValueSeen +
            ", " +
            "Free:" + this.systemInfo.getHardware().getMemory().getAvailable() +
            ", " +
            "Env: Production" +
            ", " +
            "GUI Scale: " +
            (((Number) Math.abs(
                (long) this.proxy.getPlayer().getLoginData().getClientChainData().get("GuiScale"))
            ).floatValue() + 1) +
            ", " +
            "Res:" + res.getX() + "x" + res.getY();

        final Instant instant = Instant.now();
        final String thirdLine = "Local=" + ZonedDateTime.ofInstant(
            instant,
            !settingsConfig.getZoneIdOverride().isEmpty() ?
                ZoneId.of(settingsConfig.getZoneIdOverride()) : ZoneId.systemDefault()
        ).format(FORMATTER) +
            ", UTC=" + ZonedDateTime.ofInstant(instant, ZoneOffset.UTC).format(FORMATTER) + "Z";
        return firstLine +
            "\n" + " ".repeat(firstLine.length() / 10) +
            secondLine +
            "\n" + " ".repeat((int) (secondLine.length() / 2.5f)) +
            thirdLine;
    }

    public String getMinecraftVersion() {
        final BedrockGameVersion version = this.proxy.getPlayer().getLoginData().getGameVersion();
        return (version.getRevision() != -1 ? "Preview " : "") +
            version.getMinor() + "." + version.getPatch() + (version.getRevision() == -1 ? "" :
            "." + version.getRevision());
    }

    private String getUBR() {
        try {
            final Process process = Runtime.getRuntime().exec("powershell.exe -Command " +
                "\"Get-ItemProperty -Path 'HKLM:\\Software\\Microsoft\\Windows NT\\CurrentVersion' | " +
                "Select-Object -ExpandProperty UBR\"");
            try (final InputStream inputStream = process.getInputStream()) {
                final String rev = new String(inputStream.readAllBytes()).trim();
                process.destroy();
                return rev;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Rev";
    }
}