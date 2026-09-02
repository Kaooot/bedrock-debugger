package dev.kaooot.debugger.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.AbilitiesIndex;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.data.PlayerBlockActionData;
import org.cloudburstmc.protocol.bedrock.data.map.MapPixel;
import org.cloudburstmc.protocol.bedrock.data.payload.abilities.SerializedAbilitiesData;
import org.cloudburstmc.protocol.bedrock.data.payload.abilities.SerializedAbilitiesDataSerializedLayer;
import org.cloudburstmc.protocol.bedrock.data.payload.inventory.transaction.ItemUseOnActorActionType;
import org.cloudburstmc.protocol.bedrock.data.payload.inventory.transaction.data.InventoryTransactionDataType;
import org.cloudburstmc.protocol.bedrock.data.payload.inventory.transaction.data.ItemUseOnActorInventoryTransaction;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;
import org.cloudburstmc.protocol.bedrock.packet.MapInfoRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket;
import dev.kaooot.debugger.BedrockDebuggerProxy;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class CheatClientAuthority {

    private static final float ENHANCED_FLY_SPEED_VALUE = 0.1f;
    private static final float DEFAULT_WALK_SPEED = 0.1f;
    private static final float DEFAULT_FLY_SPEED = 0.05f;

    private final BedrockDebuggerProxy proxy;
    private final ProxiedPlayer player;

    @Getter
    private final ClientAuthoritativeSettings settings = new ClientAuthoritativeSettings();

    public boolean handleInvalidCreativeActionBypass(PlayerActionPacket packet) {
        return this.settings.isBypassInvalidCreativeDestroyAction() &&
            packet.getAction().equals(PlayerActionType.CREATIVE_DESTROY_BLOCK);
    }

    public void updateMineAbility() {
        final boolean enabled = this.settings.isForceMineAbilityEnabled();
        final UpdateAbilitiesPacket updateAbilitiesPacket = new UpdateAbilitiesPacket();
        final SerializedAbilitiesData data = this.player.getSerializedAbilitiesData();
        if (enabled) {
            for (final SerializedAbilitiesDataSerializedLayer layer : data.getLayers()) {
                layer.getAbilityValues().add(AbilitiesIndex.MINE);
            }
        }
        updateAbilitiesPacket.setData(data);
        this.proxy.getServer().sendPacket(updateAbilitiesPacket);
    }

    public boolean handleNuker(PlayerAuthInputPacket packet) {
        if (packet.getPlayerBlockActions().isEmpty()) {
            return false;
        }
        if (!this.settings.isNukerEnabled()) {
            return false;
        }
        final PlayerBlockActionData firstAction = packet.getPlayerBlockActions().getFirst();
        for (int x = -this.settings.getNukerWidth(); x <= this.settings.getNukerWidth(); x++) {
            for (int z = -this.settings.getNukerWidth(); z <= this.settings.getNukerWidth(); z++) {
                for (int y = -this.settings.getNukerHeight(); y <= this.settings.getNukerHeight();
                     y++) {
                    final Vector3i posOffset = firstAction.getBlockPosition().add(x, y, z);

                    final PlayerBlockActionData startDestroyAction = new PlayerBlockActionData();
                    startDestroyAction.setPlayerActionType(PlayerActionType.START_DESTROY_BLOCK);
                    startDestroyAction.setBlockPosition(posOffset);

                    final PlayerBlockActionData predictDestroyAction = new PlayerBlockActionData();
                    predictDestroyAction.setPlayerActionType(
                        PlayerActionType.PREDICT_DESTROY_BLOCK
                    );
                    predictDestroyAction.setBlockPosition(posOffset);

                    packet.getPlayerBlockActions().add(startDestroyAction);
                    packet.getPlayerBlockActions().add(predictDestroyAction);
                }
            }
        }
        return true;
    }

    public boolean handleClicksPerSecondOverride(InventoryTransactionPacket packet) {
        if (!this.settings.isCpsOverrideEnabled()) {
            return false;
        }
        if (!packet.getTransaction().getType()
            .equals(InventoryTransactionDataType.ITEM_USE_ON_ACTOR)) {
            return false;
        }
        final ItemUseOnActorInventoryTransaction transaction =
            (ItemUseOnActorInventoryTransaction) packet.getTransaction();
        if (!transaction.getActionType().equals(ItemUseOnActorActionType.ATTACK)) {
            return false;
        }
        for (int i = 0; i < this.settings.getClicksPerSecond(); i++) {
            this.proxy.getClient().sendPacket(packet);
        }
        return true;
    }

    public void updateEnhancedFlySpeed() {
        this.player.sendMessage(
            (this.settings.isEnhancedFlySpeedEnabled() ? "Enabled" : "Disabled") +
                " Enhanced Fly Speed"
        );

        final UpdateAbilitiesPacket updateAbilitiesPacket = new UpdateAbilitiesPacket();
        final SerializedAbilitiesData data = this.player.getSerializedAbilitiesData();
        for (final SerializedAbilitiesDataSerializedLayer layer : data.getLayers()) {
            layer.getAbilityValues().add(AbilitiesIndex.FLYING);
            layer.setFlySpeed(
                this.settings.isEnhancedFlySpeedEnabled() ? ENHANCED_FLY_SPEED_VALUE :
                    DEFAULT_FLY_SPEED
            );
        }
        updateAbilitiesPacket.setData(data);
        this.proxy.getServer().sendPacket(updateAbilitiesPacket);
    }

    public void handleEnhancedFlySpeed(UpdateAbilitiesPacket packet) {
        if (packet.getData().getTargetPlayerRawId() != this.player.getActorId()) {
            return;
        }
        this.player.setSerializedAbilitiesData(packet.getData());
        if (!this.settings.isEnhancedFlySpeedEnabled()) {
            return;
        }
        final SerializedAbilitiesData data = packet.getData();
        for (final SerializedAbilitiesDataSerializedLayer layer : data.getLayers()) {
            layer.setFlySpeed(ENHANCED_FLY_SPEED_VALUE);
        }
    }

    public void sendBadPacket() {
        final MapInfoRequestPacket packet = new MapInfoRequestPacket();
        packet.setMapUniqueID(0L);
        for (int i = 0; i < 65000; i++) {
            packet.getClientPixelsList().add(new MapPixel(0, i));
        }

        this.proxy.getClient().sendPacket(packet);
        this.proxy.getPlayer().sendMessage("Sent bad packet");
    }
}