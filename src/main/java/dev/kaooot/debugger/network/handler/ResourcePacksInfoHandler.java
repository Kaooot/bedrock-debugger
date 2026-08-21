package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.data.payload.pack.PackIdVersion;
import org.cloudburstmc.protocol.bedrock.data.payload.pack.PackInfoData;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePacksInfoPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.pack.ServerPack;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ResourcePacksInfoHandler implements PacketHandler<ResourcePacksInfoPacket> {

    @Override
    public PacketSignal handle(ResourcePacksInfoPacket packet, BedrockDebuggerProxy proxy) {
        final SettingsConfig settingsConfig =
            Registries.<ConfigRegistry>getRegistry(RegistryKey.CONFIG).get(SettingsConfig.class);

        if (settingsConfig.isPrintDebugInfo()) {
            for (final PackInfoData resourcePack : packet.getResourcePacks()) {
                proxy.getLogger()
                    .debug("\"{}\": \"{}\",", resourcePack.getPackIdVersion().getPackUUID(),
                        resourcePack.getContentKey());
            }
        }
        proxy.getPlayer().setForceDisableVibrantVisuals(packet.isForceDisableVibrantVisuals());
        proxy.getPlayer().setResourcePackRequired(packet.isResourcePackRequired());
        for (final ServerPack pack : proxy.getPackManager().getPacks()) {
            final PackIdVersion packIdVersion = new PackIdVersion();
            packIdVersion.setPackUUID(pack.getId());
            packIdVersion.setPackVersion(pack.getVersion());
            final PackInfoData packInfoData = new PackInfoData();
            packInfoData.setPackIdVersion(packIdVersion);
            packInfoData.setPackSize(pack.getSize());
            packInfoData.setContentKey("");
            packInfoData.setSubpackName("");
            packInfoData.setContentIdentity("");
            packInfoData.setHasScripts(false);
            packInfoData.setAddonPack(false);
            packInfoData.setRayTracingCapable(false);
            packInfoData.setCdnUrl("");
            packet.getResourcePacks().add(packInfoData);
        }

        packet.setResourcePackRequired(true);
        proxy.getServer().sendPacketImmediately(packet);
        return PacketSignal.HANDLED;
    }
}