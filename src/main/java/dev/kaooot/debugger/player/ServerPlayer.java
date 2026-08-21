package dev.kaooot.debugger.player;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.cloudburstmc.math.vector.Vector3f;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.shape.DebugBox;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.util.Util;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Getter
public class ServerPlayer {

    private final long actorId;
    private final long runtimeId;
    private final BedrockDebuggerProxy proxy;

    @Setter
    private Vector3f position = Vector3f.ZERO;
    @Getter
    private final float width = 0.6f;
    @Getter
    private final float height = 1.8f;
    @Getter
    private final float eyeHeight = 1.62f;
    @Getter
    @Setter
    private String nameTag;

    private final Set<String> nodeShapeIds = new ObjectOpenHashSet<>();

    public ServerPlayer(long actorId, long runtimeId, BedrockDebuggerProxy proxy) {
        this.actorId = actorId == 0 ? runtimeId : actorId;
        this.runtimeId = runtimeId;
        this.proxy = proxy;
    }

    public void renderBounds(SettingsConfig settingsConfig) {
        final String playerBoxId = "player_box_" + this.actorId;

        final DebugBox box = new DebugBox();
        box.setId(playerBoxId);
        box.setLocation(Vector3f.from(0f, this.height / 2f, 0f));
        box.setColor(Util.rgbToColor(
                settingsConfig.getPlayerDebugRendererColorR(),
                settingsConfig.getPlayerDebugRendererColorG(),
                settingsConfig.getPlayerDebugRendererColorB()
            )
        );
        box.setBoxBound(Vector3f.from(this.width, this.height, this.width));
        box.setAttachedToEntityID(this.actorId);

        this.proxy.getDebugShapeRenderer().renderShapes(box);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ServerPlayer other)) {
            return false;
        }
        return other.actorId == this.actorId && other.runtimeId == this.runtimeId;
    }
}