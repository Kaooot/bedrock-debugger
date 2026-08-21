package dev.kaooot.debugger.input;

import com.sun.jna.platform.win32.User32;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.menu.ServerSettingsMenu;
import dev.kaooot.debugger.player.ProxiedPlayer;
import dev.kaooot.debugger.screen.DebugScreenRegistry;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class KeyInputListener {

    private final BedrockDebuggerProxy proxy;
    private final Int2BooleanMap pressed = new Int2BooleanOpenHashMap();
    private final List<Integer> listeningKeys = Arrays.asList(
        KeyEvent.VK_F3, KeyEvent.VK_F6, KeyEvent.VK_F7
    );

    public void init() {
        final Consumer<Integer> keyPressedConsumer = keyCode -> {
            if (KeyInputListener.this.proxy.getPlayer() == null) {
                return;
            }
            switch (keyCode) {
                case KeyEvent.VK_F3 -> {
                    if (!KeyInputListener.this.proxy.isLoadPacks()) {
                        KeyInputListener.this.proxy.getPlayer().sendMessage(
                            "§cNot available. Please enable the debug resource packs toggle."
                        );
                        return;
                    }
                    if (!KeyInputListener.this.proxy.getPlayer().isDiagnosticsEnabled()) {
                        KeyInputListener.this.proxy.getPlayer().sendMessage(
                            "§cNot available. Please enable client diagnostics in the creator" +
                                " settings and restart the game."
                        );
                        return;
                    }
                    final DebugScreenRegistry registry = Registries
                        .getRegistry(RegistryKey.DEBUG_SCREEN);
                    int nextIndex = KeyInputListener.this.proxy.getDebugScreenInfo()
                        .getSelectedIndex() + 1;
                    if (!registry.getKeys().contains(nextIndex)) {
                        KeyInputListener.this.proxy.getDebugScreenInfo()
                            .setLastElement(registry.getValue(
                                KeyInputListener.this.proxy.getDebugScreenInfo()
                                    .getSelectedIndex()
                            ).getElement());
                        nextIndex = -1;
                    }
                    KeyInputListener.this.proxy.getDebugScreenInfo()
                        .setSelectedIndex(nextIndex);
                }
                case KeyEvent.VK_F6 -> {
                    final ProxiedPlayer player = KeyInputListener.this.proxy.getPlayer();
                    player.toggleEnhancedFlySpeed();
                }
                case KeyEvent.VK_F7 -> new ServerSettingsMenu().show(KeyInputListener.this.proxy);
                default -> throw new IllegalStateException(
                    "Received key input without listener for key " + keyCode + " (" +
                        KeyEvent.getKeyText(keyCode) + ")"
                );
            }
        };
        CompletableFuture.runAsync(() -> {
            while (true) {
                for (final int keyCode : this.listeningKeys) {
                    final boolean pressed =
                        (User32.INSTANCE.GetAsyncKeyState(keyCode) & 0x8000) != 0;
                    if (pressed && !KeyInputListener.this.pressed.getOrDefault(keyCode, false)) {
                        keyPressedConsumer.accept(keyCode);
                    }
                    KeyInputListener.this.pressed.put(keyCode, pressed);
                }
            }
        });
    }
}