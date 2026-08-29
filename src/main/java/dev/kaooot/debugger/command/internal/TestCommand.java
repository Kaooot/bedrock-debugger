package dev.kaooot.debugger.command.internal;

import com.google.common.base.CaseFormat;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.command.Command;
import dev.kaooot.debugger.api.command.annotation.CommandEnumData;
import dev.kaooot.debugger.api.command.annotation.CommandEnumValue;
import dev.kaooot.debugger.api.command.annotation.Description;
import dev.kaooot.debugger.api.command.annotation.Name;
import dev.kaooot.debugger.api.command.annotation.Overloads;
import dev.kaooot.debugger.api.command.annotation.Parameter;
import dev.kaooot.debugger.api.command.annotation.Parameters;
import dev.kaooot.debugger.api.shape.DebugCone;
import dev.kaooot.debugger.api.shape.DebugCylinder;
import dev.kaooot.debugger.api.shape.DebugEllipsoid;
import dev.kaooot.debugger.api.shape.DebugPyramid;
import dev.kaooot.debugger.api.shape.DebugShape;
import dev.kaooot.debugger.api.shape.DebugText;
import dev.kaooot.debugger.menu.ServerSettingsMenu;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.actor.ActorDataTypes;
import org.cloudburstmc.protocol.bedrock.data.actor.ActorFlags;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamType;
import org.cloudburstmc.protocol.bedrock.data.map.MapPixel;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ScriptPrimitiveShapeType;
import org.cloudburstmc.protocol.bedrock.packet.MapInfoRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetActorDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.SpawnParticleEffectPacket;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Name("test")
@Description("Test command.")
@Overloads({
    @Parameters(overloads = {
        @Parameter(name = "action", type = CommandParamType.ID, enumData = @CommandEnumData(name =
            "ActionType",
            values = {
                @CommandEnumValue(name = "settings"),
                @CommandEnumValue(name = "imgui"),
                @CommandEnumValue(name = "bad_packet"),
                @CommandEnumValue(name = "version"),
                @CommandEnumValue(name = "shapes"),
                @CommandEnumValue(name = "sleeping"),
                @CommandEnumValue(name = "sleeping_actorflag"),
            }))
    })
})
public class TestCommand extends Command<BedrockDebuggerProxy> {

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        if (args.length >= 1) {
            switch (args[0]) {
                case "settings" -> new ServerSettingsMenu().show(proxy);
                case "imgui" -> proxy.getImGuiAdapter().toggle();
                case "particle" -> {
                    final ThreadLocalRandom random = ThreadLocalRandom.current();
                    proxy.getScheduler().schedule(() -> {
                        for (int i = 0; i < 10; i++) {
                            final SpawnParticleEffectPacket packet =
                                new SpawnParticleEffectPacket();
                            packet.setDimensionId(proxy.getPlayer().getDimension());
                            packet.setActorId(proxy.getPlayer().getActorId());
                            packet.setPosition(
                                Vector3f.from(
                                    random.nextFloat(-2f, 2f),
                                    random.nextFloat(2f, 2.5f),
                                    random.nextFloat(-2f, 2f)
                                )
                            );
                            packet.setEffectName("minecraft:snowflake_particle");
                            packet.setMolangVariables(Optional.empty());

                            proxy.getServer().sendPacket(packet);
                        }
                    }, 100);
                }
                case "bad_packet" -> {
                    final MapInfoRequestPacket packet = new MapInfoRequestPacket();
                    packet.setMapUniqueID(0L);
                    for (int i = 0; i < 65000; i++) {
                        packet.getClientPixelsList().add(new MapPixel(0, i));
                    }

                    proxy.getClient().sendPacket(packet);
                    proxy.getPlayer().sendMessage("Sent bad packet");
                }
                case "version" -> {
                    proxy.getPlayer().sendMessage("Version info dump: " +
                        proxy.getPlayer().getLoginData().getGameVersion() +
                        ", Client Network Version " +
                        proxy.getPlayer().getLoginData().getClientNetworkVersion() +
                        ", Preview flag: " +
                        proxy.getPlayer().getLoginData().isPreview()
                    );
                }
                case "shapes" -> {
                    final List<DebugShape> shapes = new ObjectArrayList<>();

                    final DebugCylinder cylinder = new DebugCylinder();
                    cylinder.setId("cylinder");
                    cylinder.setLocation(proxy.getPlayer().getPosition());
                    cylinder.setDimension(proxy.getPlayer().getDimension());
                    cylinder.setRadiusX(Vector2f.ONE);
                    cylinder.setRadiusZ(Vector2f.ONE);
                    cylinder.setHeight(1f);
                    cylinder.setNumSegments(20);

                    final DebugPyramid pyramid = new DebugPyramid();
                    pyramid.setId("pyramid");
                    pyramid.setLocation(proxy.getPlayer().getPosition().clone().add(0f, 0f, 4f));
                    pyramid.setDimension(proxy.getPlayer().getDimension());
                    pyramid.setWidth(1f);
                    pyramid.setDepth(1f);
                    pyramid.setHeight(1f);

                    final DebugEllipsoid ellipsoid = new DebugEllipsoid();
                    ellipsoid.setId("ellipsoid");
                    ellipsoid.setLocation(proxy.getPlayer().getPosition().clone().add(0f, 0f, 8f));
                    ellipsoid.setDimension(proxy.getPlayer().getDimension());
                    ellipsoid.setRadii(Vector3f.from(1f, 1f, 1f));
                    ellipsoid.setSegmentsPerAxis(20);

                    final DebugCone cone = new DebugCone();
                    cone.setId("cone");
                    cone.setLocation(proxy.getPlayer().getPosition().clone().add(0f, 0f, 12f));
                    cone.setDimension(proxy.getPlayer().getDimension());
                    cone.setRadii(Vector2f.ONE);
                    cone.setHeight(1f);
                    cone.setNumSegments(4);

                    shapes.addAll(
                        List.of(
                            cylinder, pyramid, ellipsoid, cone
                        )
                    );

                    final List<DebugShape> shapesCopy = new ObjectArrayList<>(shapes);
                    for (final DebugShape shape : shapesCopy) {
                        final DebugText text = new DebugText();
                        text.setId("text-" + shape.getId());
                        text.setScale(1f);
                        text.setLocation(shape.getLocation().clone().add(0, 5f, 0f));
                        text.setDimension(shape.getDimension());
                        final StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(
                            CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.UPPER_CAMEL)
                                .convert(shape.getType().name()));
                        if (shape.getType().equals(ScriptPrimitiveShapeType.CYLINDER)) {
                            stringBuilder.append("\n")
                                .append("Radius X: ")
                                .append(cylinder.getRadiusX())
                                .append("\n")
                                .append("Radius Z: ")
                                .append(cylinder.getRadiusZ())
                                .append("\n")
                                .append("Height: ")
                                .append(cylinder.getHeight())
                                .append("\n")
                                .append("Num Segments: ")
                                .append(cylinder.getNumSegments());
                        } else if (shape.getType().equals(ScriptPrimitiveShapeType.PYRAMID)) {
                            stringBuilder.append("\n")
                                .append("Width: ")
                                .append(pyramid.getWidth())
                                .append("\n")
                                .append("Depth: ")
                                .append(pyramid.getDepth())
                                .append("\n")
                                .append("Height: ")
                                .append(pyramid.getHeight());
                        } else if (shape.getType().equals(ScriptPrimitiveShapeType.ELLIPSOID)) {
                            stringBuilder.append("\n")
                                .append("Radii: ")
                                .append(ellipsoid.getRadii())
                                .append("\n")
                                .append("Segments Per Axis: ")
                                .append(ellipsoid.getSegmentsPerAxis());
                        } else if (shape.getType().equals(ScriptPrimitiveShapeType.CONE)) {
                            stringBuilder.append("\n")
                                .append("Radii: ")
                                .append(cone.getRadii())
                                .append("\n")
                                .append("Height: ")
                                .append(cone.getHeight())
                                .append("\n")
                                .append("Num Segments: ")
                                .append(cone.getNumSegments());
                        }

                        text.setText(stringBuilder.toString());
                        shapes.add(text);
                    }

                    proxy.getDebugShapeRenderer().renderShapes(shapes.toArray(DebugShape[]::new));
                }
                case "sleeping" -> proxy.getScheduler().schedule(() -> {
                    final SetActorDataPacket packet = new SetActorDataPacket();
                    packet.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
                    packet.getActorData().putType(ActorDataTypes.PLAYER_FLAGS, (byte) 2);

                    proxy.getServer().sendPacket(packet);
                    proxy.getPlayer().sendMessage(packet.toString());
                    proxy.getScheduler().schedule(() -> {
                        final SetActorDataPacket resetPacket = new SetActorDataPacket();
                        resetPacket.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
                        resetPacket.getActorData()
                            .putType(ActorDataTypes.PLAYER_FLAGS, (byte) 0);

                        proxy.getServer().sendPacket(resetPacket);
                        proxy.getPlayer().sendMessage(resetPacket.toString());
                    }, 0, 2);
                }, 0, 40);
                case "sleeping_actorflag" -> proxy.getScheduler().schedule(() -> {
                    final SetActorDataPacket packet = new SetActorDataPacket();
                    packet.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
                    packet.getActorData().setFlag(ActorFlags.SLEEPING, true);

                    proxy.getServer().sendPacket(packet);
                    proxy.getPlayer().sendMessage(packet.toString());
                    proxy.getScheduler().schedule(() -> {
                        final SetActorDataPacket resetPacket = new SetActorDataPacket();
                        resetPacket.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
                        resetPacket.getActorData().setFlag(ActorFlags.SLEEPING, false);

                        proxy.getServer().sendPacket(resetPacket);
                        proxy.getPlayer().sendMessage(resetPacket.toString());
                    }, 0, 2);
                }, 0, 40);
            }
        }
    }
}