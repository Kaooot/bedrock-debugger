package dev.kaooot.debugger.actor;

import com.google.common.base.CaseFormat;
import it.unimi.dsi.fastutil.Function;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.actor.ActorDataMap;
import org.cloudburstmc.protocol.bedrock.data.actor.ActorDataTypes;
import org.cloudburstmc.protocol.bedrock.data.actor.ActorEvent;
import org.cloudburstmc.protocol.bedrock.data.actor.ActorFlags;
import org.cloudburstmc.protocol.bedrock.data.actor.ActorLink;
import org.cloudburstmc.protocol.bedrock.packet.RemoveActorPacket;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.shape.DebugBox;
import dev.kaooot.debugger.api.shape.DebugText;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.util.Util;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@ToString
public class Actor {

    private static final Function<String, String> CONVERTER = s ->
        CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.UPPER_CAMEL).convert(s.toString());

    protected final BedrockDebuggerProxy proxy;
    @Getter
    protected final String identifier;

    @Getter
    protected final long actorId;
    @Getter
    protected final long runtimeId;

    @Setter
    @Getter
    private Vector3f position;
    @Getter
    @Setter
    private String name;
    @Getter
    private final long boundingBoxNetworkId = ThreadLocalRandom.current().nextLong();
    @Getter
    @Setter
    private ActorDataMap metadata;
    @Getter
    @Setter
    private ActorEvent lastEvent;
    @Getter
    @Setter
    private float health;
    @Getter
    @Setter
    private float maxHealth;
    @Getter
    @Setter
    private ActorLink link;

    private String prettifiedActorType;

    public Actor(BedrockDebuggerProxy proxy, String identifier, Vector3f position) {
        this.proxy = proxy;
        this.identifier = identifier;
        this.position = position;
        this.actorId = proxy.generateActorId();
        this.runtimeId = this.actorId;
    }

    public Actor(BedrockDebuggerProxy proxy, String identifier, Vector3f position, long actorId,
                 long runtimeId) {
        this.proxy = proxy;
        this.identifier = identifier;
        this.position = position;
        this.actorId = actorId;
        this.runtimeId = runtimeId;
    }

    public void spawn(Vector3f position) {
        this.spawn(position, new ActorLink[0]);
    }

    public void spawn(Vector3f position, ActorLink... links) {

    }

    public void remove() {
        final RemoveActorPacket packet = new RemoveActorPacket();
        packet.setTargetActorID(this.actorId);

        this.proxy.getServer().sendPacket(packet);
        this.proxy.getActors().removeIf(actor -> actor.getActorId() == this.actorId &&
            actor.getRuntimeId() == this.runtimeId);
    }

    public void renderBounds(SettingsConfig settingsConfig) {
        final String actorBoxId = "actor_box_" + this.actorId;
        final String actorBoxTextId = "actor_box_text_" + this.actorId;
        final String actorBoxLinkTextId = "actor_box_link_text_" + this.actorId;

        final float width = !this.metadata.containsKey(ActorDataTypes.WIDTH) ? 1f :
            this.metadata.get(ActorDataTypes.WIDTH);
        final float height = !this.metadata.containsKey(ActorDataTypes.HEIGHT) ? 1f :
            this.metadata.get(ActorDataTypes.HEIGHT);
        final float scale = this.metadata.containsKey(ActorDataTypes.SCALE) ? 1f :
            this.metadata.get(ActorDataTypes.SCALE);

        final DebugBox box = new DebugBox();
        box.setId(actorBoxId);
        box.setLocation(Vector3f.from(
            0f,
            height * scale / 2f,
            0f
        ));
        box.setColor(Util.rgbToColor(
                settingsConfig.getActorDebugRendererColorR(),
                settingsConfig.getActorDebugRendererColorG(),
                settingsConfig.getActorDebugRendererColorB()
            )
        );
        box.setBoxBound(
            Vector3f.from(
                width * scale,
                height * scale,
                width * scale
            )
        );
        box.setAttachedToEntityID(this.actorId);

        final boolean isRider = this.link != null && this.link.getTargetB() == this.actorId;

        if (this.link != null) {
            for (final Actor actor : this.proxy.getActors()) {
                if (actor.getLink() == null && (this.link.getTargetB() == actor.getActorId() ||
                    this.link.getTargetA() == actor.getActorId())) {
                    actor.setLink(this.link);
                    break;
                }
            }
        }

        final EnumSet<ActorFlags> flags = this.metadata.getOrCreateFlags();

        final DebugText text = new DebugText();
        text.setId(actorBoxTextId);
        text.setLocation(Vector3f.from(
                0f,
                box.getBoxBound().getY() + 0.5f /*+ (isRider ? 1f : 0f) + (flags.size() * 0.25f)*/,
                0f
            )
        );
        /*text.setText(this.getPrettifiedActorType() +
            "\nHealth: §c" + this.health + " §f/ §4" + this.maxHealth +
            (!flags.isEmpty() ?
                "\nFlags (" + flags.size() + "):\n\n" + String.join("\n",
                    flags.stream().map(Enum::name).toList()) : "") +
            (this.lastEvent != null ? "\nLast Event: §e" + this.lastEvent.name() : "")
        );*/
        text.setText(this.getPrettifiedActorType());
        text.setColor(Util.rgbToColor(255, 140, 110));
        text.setAttachedToEntityID(this.actorId);

        if (this.link != null) {
           /* final DebugText linkText = new DebugText();
            linkText.setId(actorBoxLinkTextId);
            linkText.setLocation(Vector3f.from(
                    0f,
                    box.getBoxBound().getY(),
                    0f
                )
            );
            linkText.setText("\nLink Type: §e" + CONVERTER.apply(this.link.getType().name()) +
                "\nIs Rider: §v" + isRider +
                "\nIs Ridden: §v" + (this.link.getTargetA() == this.actorId) +
                "\nVAV: " + this.link.getVehicleAngularVelocity());
            linkText.setScale(0.5f);

            this.proxy.getDebugShapeRenderer().renderShape(box);
            if (settingsConfig.isActorDebugRendererShowText()) {
                this.proxy.getDebugShapeRenderer().renderShapes(text, linkText);
            }*/
        } else {
            if (this.proxy.getDebugShapeRenderer().isShapeRendered(actorBoxLinkTextId)) {
                this.proxy.getDebugShapeRenderer().removeShape(actorBoxLinkTextId);
            }
            this.proxy.getDebugShapeRenderer().renderShape(box);
            if (settingsConfig.isActorDebugRendererShowText()) {
                this.proxy.getDebugShapeRenderer().renderShape(text);
            }
        }
    }

    public String getPrettifiedActorType() {
        if (this.prettifiedActorType == null) {
            this.prettifiedActorType = CONVERTER.apply(this.identifier.split(":")[1]);
        }
        return this.prettifiedActorType;
    }
}