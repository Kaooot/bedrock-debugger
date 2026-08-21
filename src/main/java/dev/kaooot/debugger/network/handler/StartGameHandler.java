package dev.kaooot.debugger.network.handler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.reflect.Field;
import java.util.List;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.data.ServerBlockProperty;
import org.cloudburstmc.protocol.bedrock.data.SyncedPlayerMovementSettings;
import org.cloudburstmc.protocol.bedrock.data.payload.experiment.ExperimentToggle;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.player.ServerPlayer;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class StartGameHandler implements PacketHandler<StartGamePacket> {

    @Override
    public PacketSignal handle(StartGamePacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().setActorId(packet.getEntityID());
        proxy.getPlayer().setRuntimeId(packet.getRuntimeID());
        proxy.getPlayer().setGameType(packet.getGameType());
        proxy.getPlayer().updateDimension(packet.getSettings().getSpawnSettings().getDimension());

        proxy.getPlayer().setBlockNetworkIdsAreHashes(packet.isBlockNetworkIdsAreHashes());
        proxy.getBlockPaletteManager().setUsedBlocksList();

        final ServerPlayer serverPlayer = new ServerPlayer(
            packet.getEntityID(),
            packet.getRuntimeID(),
            proxy
        );
        proxy.getPlayers().add(serverPlayer);

        try {
            final Field field = proxy.getPlayer().getClass().getDeclaredField("serverPlayer");
            field.setAccessible(true);
            field.set(proxy.getPlayer(), serverPlayer);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);

        if (settingsConfig.isPrintDebugInfo()) {
            this.printDebugInfo(proxy, packet);
        }

        final List<NbtMap> list = new ObjectArrayList<>();

        for (final ServerBlockProperty blockProperty : packet.getBlockProperties()) {
            list.add(NbtMap.builder()
                .putString("name", blockProperty.getName())
                .putCompound("properties", blockProperty.getProperties())
                .build()
            );
        }

        proxy.getPlayer().setServerBlockProperties(NbtMap.builder()
            .putList("properties", NbtType.COMPOUND, list)
            .build()
        );

        proxy.getPlayer().getExperiments().addAll(packet.getSettings().getExperiments()
            .getToggles()
            .stream()
            .map(ExperimentToggle::getName)
            .toList());
        proxy.getDebugScreenInfo().startTicking();
        proxy.setTransferring(false);

        if (settingsConfig.isForceEnablePersonaSkins() &&
            packet.getSettings().isPersonaDisabled()) {
            packet.getSettings().setPersonaDisabled(false);
        }
        if (settingsConfig.isForceDisableServerAuthBlockBreaking()) {
            packet.setMovementSettings(
                new SyncedPlayerMovementSettings(
                    null,
                    packet.getMovementSettings().getRewindHistorySize(),
                    false
                )
            );
        }
        proxy.getServer().sendPacket(packet);
        return PacketSignal.HANDLED;
    }

    private void printDebugInfo(BedrockDebuggerProxy proxy, StartGamePacket packet) {
        proxy.getLogger().debug("--- Server Authority Info ---");
        proxy.getLogger().debug("ItemStackNetManager: {}",
            packet.isEnableItemStackNetManager() ? "enabled" : "disabled"
        );
        proxy.getLogger().debug(
            "RewindHistorySize: {}",
            packet.getMovementSettings().getRewindHistorySize()
        );
        proxy.getLogger().debug("ServerAuthBlockBreaking: {}",
            packet.getMovementSettings().isServerAuthoritativeBlockBreaking() ? "enabled" :
                "disabled"
        );
        proxy.getLogger().debug("------------------------");

        if (!packet.getServerVersion().isEmpty() && packet.getServerVersion() != null) {
            proxy.getLogger().debug("Server version telemetry data: {}", packet.getServerVersion());
        }
    }
}