package dev.kaooot.debugger.command.internal;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.AbilitiesIndex;
import org.cloudburstmc.protocol.bedrock.data.ActorBlockSyncMessageId;
import org.cloudburstmc.protocol.bedrock.data.ActorLinkType;
import org.cloudburstmc.protocol.bedrock.data.BlockChangeEntry;
import org.cloudburstmc.protocol.bedrock.data.BookEditAction;
import org.cloudburstmc.protocol.bedrock.data.BuildPlatform;
import org.cloudburstmc.protocol.bedrock.data.CameraShakeAction;
import org.cloudburstmc.protocol.bedrock.data.CameraShakeType;
import org.cloudburstmc.protocol.bedrock.data.CodeBuilderCategoryType;
import org.cloudburstmc.protocol.bedrock.data.CodeBuilderCodeStatus;
import org.cloudburstmc.protocol.bedrock.data.CodeBuilderOperationType;
import org.cloudburstmc.protocol.bedrock.data.ControlScheme;
import org.cloudburstmc.protocol.bedrock.data.DebugMarkerData;
import org.cloudburstmc.protocol.bedrock.data.Difficulty;
import org.cloudburstmc.protocol.bedrock.data.Dimension;
import org.cloudburstmc.protocol.bedrock.data.DisconnectFailReason;
import org.cloudburstmc.protocol.bedrock.data.EduSharedUriResource;
import org.cloudburstmc.protocol.bedrock.data.EducationLevelSettings;
import org.cloudburstmc.protocol.bedrock.data.GameRuleData;
import org.cloudburstmc.protocol.bedrock.data.GameType;
import org.cloudburstmc.protocol.bedrock.data.GeneratorType;
import org.cloudburstmc.protocol.bedrock.data.GraphicsMode;
import org.cloudburstmc.protocol.bedrock.data.HudElement;
import org.cloudburstmc.protocol.bedrock.data.HudVisibility;
import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.data.MovementEffectType;
import org.cloudburstmc.protocol.bedrock.data.MultiplayerSettingsPacketType;
import org.cloudburstmc.protocol.bedrock.data.ObjectiveSortOrder;
import org.cloudburstmc.protocol.bedrock.data.ParticleType;
import org.cloudburstmc.protocol.bedrock.data.PayloadType;
import org.cloudburstmc.protocol.bedrock.data.PhotoType;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.data.PlayerArmorDamageFlag;
import org.cloudburstmc.protocol.bedrock.data.PlayerPermissionLevel;
import org.cloudburstmc.protocol.bedrock.data.PlayerRespawnState;
import org.cloudburstmc.protocol.bedrock.data.PredictionType;
import org.cloudburstmc.protocol.bedrock.data.SimulationType;
import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.cloudburstmc.protocol.bedrock.data.SpawnPositionType;
import org.cloudburstmc.protocol.bedrock.data.StoreOfferRedirectType;
import org.cloudburstmc.protocol.bedrock.data.TextPacketType;
import org.cloudburstmc.protocol.bedrock.data.actor.ActorEvent;
import org.cloudburstmc.protocol.bedrock.data.actor.ActorFlags;
import org.cloudburstmc.protocol.bedrock.data.actor.ActorLink;
import org.cloudburstmc.protocol.bedrock.data.camera.AimAssistAction;
import org.cloudburstmc.protocol.bedrock.data.camera.CameraFadeInstruction;
import org.cloudburstmc.protocol.bedrock.data.camera.CameraSetInstruction;
import org.cloudburstmc.protocol.bedrock.data.camera.CameraTargetInstruction;
import org.cloudburstmc.protocol.bedrock.data.command.CommandEnumData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginType;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOutputType;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamType;
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermissionLevel;
import org.cloudburstmc.protocol.bedrock.data.command.SoftEnumUpdateType;
import org.cloudburstmc.protocol.bedrock.data.definitions.DimensionDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.FeatureDefinition;
import org.cloudburstmc.protocol.bedrock.data.ee.AgentActionType;
import org.cloudburstmc.protocol.bedrock.data.ee.LessonAction;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerEnumName;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType;
import org.cloudburstmc.protocol.bedrock.data.inventory.EnchantmentInstance;
import org.cloudburstmc.protocol.bedrock.data.inventory.FullContainerName;
import org.cloudburstmc.protocol.bedrock.data.inventory.InventoryLayout;
import org.cloudburstmc.protocol.bedrock.data.inventory.InventoryLeftTabIndex;
import org.cloudburstmc.protocol.bedrock.data.inventory.InventoryRightTabIndex;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemEnchantOption;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemEnchants;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemUseMethod;
import org.cloudburstmc.protocol.bedrock.data.inventory.LabTableReactionType;
import org.cloudburstmc.protocol.bedrock.data.map.MapPixel;
import org.cloudburstmc.protocol.bedrock.data.payload.abilities.SerializedAbilitiesData;
import org.cloudburstmc.protocol.bedrock.data.payload.abilities.SerializedAbilitiesDataSerializedLayer;
import org.cloudburstmc.protocol.bedrock.data.payload.abilities.SerializedLayer;
import org.cloudburstmc.protocol.bedrock.data.payload.attribute.AttributeData;
import org.cloudburstmc.protocol.bedrock.data.payload.attribute.AttributeModifier;
import org.cloudburstmc.protocol.bedrock.data.payload.attribute.AttributeModifierOperation;
import org.cloudburstmc.protocol.bedrock.data.payload.attribute.AttributeOperands;
import org.cloudburstmc.protocol.bedrock.data.payload.boss.BossBarColor;
import org.cloudburstmc.protocol.bedrock.data.payload.boss.BossBarOverlay;
import org.cloudburstmc.protocol.bedrock.data.payload.boss.BossEventUpdateType;
import org.cloudburstmc.protocol.bedrock.data.payload.command.BlockCommandData;
import org.cloudburstmc.protocol.bedrock.data.payload.command.CommandBlockMode;
import org.cloudburstmc.protocol.bedrock.data.payload.common.DimensionType;
import org.cloudburstmc.protocol.bedrock.data.payload.common.RedactableString;
import org.cloudburstmc.protocol.bedrock.data.payload.connection.DisconnectPacketMessages;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.PrimitiveShapeDataPayload;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ScriptPrimitiveShapeType;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.SphereDataPayload;
import org.cloudburstmc.protocol.bedrock.data.payload.structure.AnimationMode;
import org.cloudburstmc.protocol.bedrock.data.payload.structure.Mirror;
import org.cloudburstmc.protocol.bedrock.data.payload.structure.Rotation;
import org.cloudburstmc.protocol.bedrock.data.payload.structure.StructureBlockType;
import org.cloudburstmc.protocol.bedrock.data.payload.structure.StructureEditorData;
import org.cloudburstmc.protocol.bedrock.data.payload.structure.StructureRedstoneSaveMode;
import org.cloudburstmc.protocol.bedrock.data.payload.structure.StructureSettings;
import org.cloudburstmc.protocol.bedrock.data.payload.text.AuthorAndMessage;
import org.cloudburstmc.protocol.bedrock.data.structure.StructureTemplateRequestOperation;
import org.cloudburstmc.protocol.bedrock.data.structure.StructureTemplateResponseType;
import org.cloudburstmc.protocol.bedrock.packet.ActorEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.ActorPickRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.AddBehaviorTreePacket;
import org.cloudburstmc.protocol.bedrock.packet.AddItemActorPacket;
import org.cloudburstmc.protocol.bedrock.packet.AddPaintingPacket;
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket;
import org.cloudburstmc.protocol.bedrock.packet.AddVolumeEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.AgentActionEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.AgentAnimationPacket;
import org.cloudburstmc.protocol.bedrock.packet.AnimateEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket;
import org.cloudburstmc.protocol.bedrock.packet.AnvilDamagePacket;
import org.cloudburstmc.protocol.bedrock.packet.AwardAchievementPacket;
import org.cloudburstmc.protocol.bedrock.packet.BlockEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.BlockPickRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.BookEditPacket;
import org.cloudburstmc.protocol.bedrock.packet.BossEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.CameraAimAssistPacket;
import org.cloudburstmc.protocol.bedrock.packet.CameraInstructionPacket;
import org.cloudburstmc.protocol.bedrock.packet.CameraPacket;
import org.cloudburstmc.protocol.bedrock.packet.CameraShakePacket;
import org.cloudburstmc.protocol.bedrock.packet.ChangeDimensionPacket;
import org.cloudburstmc.protocol.bedrock.packet.ChangeMobPropertyPacket;
import org.cloudburstmc.protocol.bedrock.packet.ClientMovementPredictionSyncPacket;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundCloseFormPacket;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundControlSchemeSetPacket;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundDebugRendererPacket;
import org.cloudburstmc.protocol.bedrock.packet.CodeBuilderPacket;
import org.cloudburstmc.protocol.bedrock.packet.CodeBuilderSourcePacket;
import org.cloudburstmc.protocol.bedrock.packet.CommandBlockUpdatePacket;
import org.cloudburstmc.protocol.bedrock.packet.CommandOutputPacket;
import org.cloudburstmc.protocol.bedrock.packet.CompletedUsingItemPacket;
import org.cloudburstmc.protocol.bedrock.packet.ContainerRegistryCleanupPacket;
import org.cloudburstmc.protocol.bedrock.packet.ContainerSetDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.CorrectPlayerMovePredictionPacket;
import org.cloudburstmc.protocol.bedrock.packet.CreatePhotoPacket;
import org.cloudburstmc.protocol.bedrock.packet.CurrentStructureFeaturePacket;
import org.cloudburstmc.protocol.bedrock.packet.DeathInfoPacket;
import org.cloudburstmc.protocol.bedrock.packet.DebugInfoPacket;
import org.cloudburstmc.protocol.bedrock.packet.DimensionDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket;
import org.cloudburstmc.protocol.bedrock.packet.EditorNetworkPacket;
import org.cloudburstmc.protocol.bedrock.packet.EduUriResourcePacket;
import org.cloudburstmc.protocol.bedrock.packet.EducationSettingsPacket;
import org.cloudburstmc.protocol.bedrock.packet.EmotePacket;
import org.cloudburstmc.protocol.bedrock.packet.FeatureRegistryPacket;
import org.cloudburstmc.protocol.bedrock.packet.GameRulesChangedPacket;
import org.cloudburstmc.protocol.bedrock.packet.GameTestRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.GameTestResultsPacket;
import org.cloudburstmc.protocol.bedrock.packet.GuiDataPickItemPacket;
import org.cloudburstmc.protocol.bedrock.packet.InteractPacket;
import org.cloudburstmc.protocol.bedrock.packet.InventoryContentPacket;
import org.cloudburstmc.protocol.bedrock.packet.InventorySlotPacket;
import org.cloudburstmc.protocol.bedrock.packet.JigsawStructureDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.LabTablePacket;
import org.cloudburstmc.protocol.bedrock.packet.LecternUpdatePacket;
import org.cloudburstmc.protocol.bedrock.packet.LessonProgressPacket;
import org.cloudburstmc.protocol.bedrock.packet.LevelEventGenericPacket;
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.MapCreateLockedCopyPacket;
import org.cloudburstmc.protocol.bedrock.packet.MapInfoRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.MobArmorEquipmentPacket;
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket;
import org.cloudburstmc.protocol.bedrock.packet.MobEquipmentPacket;
import org.cloudburstmc.protocol.bedrock.packet.ModalFormRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.MotionPredictionHintsPacket;
import org.cloudburstmc.protocol.bedrock.packet.MoveActorAbsolutePacket;
import org.cloudburstmc.protocol.bedrock.packet.MovementEffectPacket;
import org.cloudburstmc.protocol.bedrock.packet.MultiplayerSettingsPacket;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;
import org.cloudburstmc.protocol.bedrock.packet.NpcDialoguePacket;
import org.cloudburstmc.protocol.bedrock.packet.NpcRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.OnScreenTextureAnimationPacket;
import org.cloudburstmc.protocol.bedrock.packet.OpenSignPacket;
import org.cloudburstmc.protocol.bedrock.packet.PhotoTransferPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlaySoundPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerArmorDamagePacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerEnchantOptionsPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerFogPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerHotbarPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerStartItemCooldownPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerToggleCrafterSlotRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerUpdateEntityOverridesPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerVideoCapturePacket;
import org.cloudburstmc.protocol.bedrock.packet.PositionTrackingDBClientRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.PositionTrackingDBServerBroadcastPacket;
import org.cloudburstmc.protocol.bedrock.packet.PrimitiveShapesPacket;
import org.cloudburstmc.protocol.bedrock.packet.PurchaseReceiptPacket;
import org.cloudburstmc.protocol.bedrock.packet.RefreshEntitlementsPacket;
import org.cloudburstmc.protocol.bedrock.packet.RemoveObjectivePacket;
import org.cloudburstmc.protocol.bedrock.packet.RemoveVolumeEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.RequestAbilityPacket;
import org.cloudburstmc.protocol.bedrock.packet.RequestPermissionsPacket;
import org.cloudburstmc.protocol.bedrock.packet.RespawnPacket;
import org.cloudburstmc.protocol.bedrock.packet.ScriptMessagePacket;
import org.cloudburstmc.protocol.bedrock.packet.ServerPlayerPostMovePositionPacket;
import org.cloudburstmc.protocol.bedrock.packet.ServerSettingsRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.ServerSettingsResponsePacket;
import org.cloudburstmc.protocol.bedrock.packet.ServerStatsPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetActorLinkPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetActorMotionPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetCommandsEnabledPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetDefaultGameTypePacket;
import org.cloudburstmc.protocol.bedrock.packet.SetDifficultyPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetDisplayObjectivePacket;
import org.cloudburstmc.protocol.bedrock.packet.SetHealthPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetHudPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetLastHurtByPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetPlayerGameTypePacket;
import org.cloudburstmc.protocol.bedrock.packet.SetPlayerInventoryOptionsPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetSpawnPositionPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetTimePacket;
import org.cloudburstmc.protocol.bedrock.packet.SetTitlePacket;
import org.cloudburstmc.protocol.bedrock.packet.SettingsCommandPacket;
import org.cloudburstmc.protocol.bedrock.packet.ShowCreditsPacket;
import org.cloudburstmc.protocol.bedrock.packet.ShowProfilePacket;
import org.cloudburstmc.protocol.bedrock.packet.ShowStoreOfferPacket;
import org.cloudburstmc.protocol.bedrock.packet.SimpleEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.SimulationTypePacket;
import org.cloudburstmc.protocol.bedrock.packet.SpawnExperienceOrbPacket;
import org.cloudburstmc.protocol.bedrock.packet.SpawnParticleEffectPacket;
import org.cloudburstmc.protocol.bedrock.packet.StopSoundPacket;
import org.cloudburstmc.protocol.bedrock.packet.StructureBlockUpdatePacket;
import org.cloudburstmc.protocol.bedrock.packet.StructureTemplateDataRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.StructureTemplateDataResponsePacket;
import org.cloudburstmc.protocol.bedrock.packet.SyncActorPropertyPacket;
import org.cloudburstmc.protocol.bedrock.packet.TakeItemActorPacket;
import org.cloudburstmc.protocol.bedrock.packet.TextPacket;
import org.cloudburstmc.protocol.bedrock.packet.TickingAreasLoadStatusPacket;
import org.cloudburstmc.protocol.bedrock.packet.ToastRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.TransferPacket;
import org.cloudburstmc.protocol.bedrock.packet.UnlockedRecipesPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAdventureSettingsPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAttributesPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockSyncedPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateClientInputLocksPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateClientOptionsPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateEquipPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdatePlayerGameTypePacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateSoftEnumPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateSubChunkBlocksPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateTradePacket;
import org.cloudburstmc.protocol.common.util.OptionalBoolean;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.command.Command;
import dev.kaooot.debugger.api.command.annotation.CommandEnumValue;
import dev.kaooot.debugger.api.command.annotation.Description;
import dev.kaooot.debugger.api.command.annotation.Name;
import dev.kaooot.debugger.api.command.annotation.Overloads;
import dev.kaooot.debugger.api.command.annotation.Parameter;
import dev.kaooot.debugger.api.command.annotation.Parameters;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.TestConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Name("testpackets")
@Description("test some packets.")
@Overloads({
    @Parameters(overloads = {
        @Parameter(name = "option", type = CommandParamType.ID, enumData =
        @dev.kaooot.debugger.api.command.annotation.CommandEnumData(name = "Option",
            values = {
                @CommandEnumValue(name = "client_bound"),
                @CommandEnumValue(name = "server_bound"),
                @CommandEnumValue(name = "disconnect"),
                @CommandEnumValue(name = "transfer"),
                @CommandEnumValue(name = "dim_change")
            }))
    })
})
public class TestPacketsCommand extends Command<BedrockDebuggerProxy> {

    private final StructureSettings structureSettings =
        new StructureSettings("paletteName", true, true, true,
            Vector3i.ZERO, Vector3i.ZERO, 0L, Rotation.NONE,
            Mirror.NONE, AnimationMode.NONE, 0f, 0f, 0, Vector3f.ZERO);

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        if (!Registries.<ConfigRegistry>getRegistry(RegistryKey.CONFIG).get(TestConfig.class)
            .isPacketTesting()) {
            proxy.getPlayer().sendMessage("§cPacket testing is not enabled");
            return;
        }
        if (args.length < 1) {
            proxy.getPlayer().sendMessage("No argument provided");
            return;
        }
        if (args[0].equalsIgnoreCase("disconnect")) {
            this.testDisconnect(proxy);
            return;
        }
        if (args[0].equalsIgnoreCase("transfer")) {
            this.testTransfer(proxy);
            return;
        }
        if (args[0].equalsIgnoreCase("dim_change")) {
            this.testChangeDimension(proxy);
            return;
        }
        if (args[0].equalsIgnoreCase("client_bound")) {
            this.testText(proxy);
            this.testAddPlayer(proxy);
            this.testAddItemActor(proxy);
            this.testServerPlayerPostMovePosition(proxy);
            this.testTakeItemActor(proxy);
            this.testMoveActorAbsolute(proxy);
            this.testAddPainting(proxy);
            this.testLevelEvent(proxy);
            this.testBlockEvent(proxy);
            this.testActorEvent(proxy);
            this.testMobEffect(proxy);
            this.testSetActorLink(proxy);
            this.testSetSpawnPosition(proxy);
            this.testPlayerHotbar(proxy);
            this.testContainerSetData(proxy);
            this.testGuiDataPickItem(proxy);
            this.testSetCommandsEnabled(proxy);
            this.testSetDifficulty(proxy);
            this.testSetPlayerGameType(proxy);
            this.testSimpleEvent(proxy);
            this.testSpawnExperienceOrb(proxy);
            this.testClientboundMapItemData(proxy);
            this.testGameRulesChanged(proxy);
            this.testCamera(proxy);
            this.testBossEvent(proxy);
            this.testShowCredits(proxy);
            this.testCommandOutput(proxy);
            this.testUpdateTrade(proxy);
            this.testUpdateEquip(proxy);
            this.testPlaySound(proxy);
            this.testStopSound(proxy);
            this.testAddBehaviorTree(proxy);
            this.testShowStoreOffer(proxy);
            this.testSetLastHurtBy(proxy);
            this.testPhotoTransfer(proxy);
            this.testModalFormRequest(proxy);
            this.testServerSettingsResponse(proxy);
            this.testShowProfile(proxy);
            this.testSetDefaultGameType(proxy);
            this.testSetDisplayObjective(proxy);
            this.testLabTable(proxy);
            this.testRemoveObjective(proxy);
            this.testUpdateBlockSynced(proxy);
            this.testMoveActorDelta(proxy);
            this.testUpdateSoftEnum(proxy);
            this.testSpawnParticleEffect(proxy);
            this.testLevelEventGeneric(proxy);
            this.testOnScreenTextureAnimation(proxy);
            this.testStructureTemplateDataResponse(proxy);
            this.testEducationSettings(proxy);
            this.testCompletedUsingItem(proxy);
            this.testPlayerEnchantOptions(proxy);
            this.testPlayerArmorDamage(proxy);
            this.testUpdatePlayerGameType(proxy);
            this.testPositionTrackingDBServerBroadcastPacket(proxy);
            this.testDebugInfoPacket(proxy);
            this.testMotionPredictionHints(proxy);
            this.testAnimateEntity(proxy);
            this.testCameraShake(proxy);
            this.testPlayerFog(proxy);
            this.testCorrectPlayerMovePrediction(proxy);
            this.testClientboundDebugRenderer(proxy);
            //this.testSyncActorProperty(proxy);
            this.testAddVolumeEntity(proxy);
            this.testRemoveVolumeEntity(proxy);
            this.testSimulationType(proxy);
            this.testNpcDialogue(proxy);
            this.testEduUriResource(proxy);
            this.testCreatePhoto(proxy);
            this.testUpdateSubChunkBlocks(proxy);
            this.testPlayerStartItemCooldown(proxy);
            this.testScriptMessage(proxy);
            this.testTickingAreasLoadStatus(proxy);
            this.testDimensionData(proxy);
            this.testAgentActionEvent(proxy);
            this.testChangeMobProperty(proxy);
            this.testLessonProgress(proxy);
            this.testToastRequest(proxy);
            this.testDeathInfo(proxy);
            this.testEditorNetwork(proxy);
            this.testFeatureRegistry(proxy);
            this.testServerStats(proxy);
            this.testGameTestResults(proxy);
            this.testUpdateClientInputLocks(proxy);
            this.testUnlockedRecipes(proxy);
            this.testCameraInstruction(proxy);
            this.testNetworkStackLatency(proxy);
            this.testMobEquipment(proxy);
            this.testMobArmorEquipment(proxy);
            this.testLevelSoundEvent(proxy);
            this.testRespawn(proxy);
            this.testSetActorMotion(proxy);
            this.testSetHealth(proxy);
            this.testSetTime(proxy);
            this.testSetTitle(proxy);
            this.testUpdateAbilities(proxy);
            this.testUpdateAdventureSettings(proxy);
            this.testUpdateAttributes(proxy);
            this.testUpdateBlock(proxy);
            this.testOpenSign(proxy);
            this.testAgentAnimation(proxy);
            this.testSetHud(proxy);
            this.testAwardAchievement(proxy);
            this.testClientboundCloseForm(proxy);
            this.testJigsawStructureData(proxy);
            this.testCurrentStructureFeature(proxy);
            this.testInventorySlot(proxy);
            this.testInventoryContent(proxy);
            this.testContainerRegistryCleanup(proxy);
            this.testMovementEffect(proxy);
            this.testClientMovementPredictionSync(proxy);
            this.testPlayerVideoCapture(proxy);
            this.testPlayerUpdateEntityOverrides(proxy);
            this.testPlayerLocation(proxy);
            this.testClientboundControlSchemeSet(proxy);
            this.testPrimitiveShapes(proxy);
        }
        if (args[0].equalsIgnoreCase("server_bound")) {
            this.testInteract(proxy);
            this.testBlockPickRequest(proxy);
            this.testActorPickRequest(proxy);
            this.testPlayerAction(proxy);
            this.testAnimate(proxy);
            this.testMapInfoRequest(proxy);
            this.testCommandBlockUpdate(proxy);
            this.testStructureBlockUpdate(proxy);
            this.testPurchaseReceipt(proxy);
            this.testBookEdit(proxy);
            this.testNpcRequest(proxy);
            this.testModalFormResponse(proxy);
            this.testServerSettingsRequest(proxy);
            this.testLecternUpdate(proxy);
            this.testMapCreateLockedCopy(proxy);
            this.testStructureTemplateDataRequest(proxy);
            this.testEmotePacket(proxy);
            this.testMultiplayerSettings(proxy);
            this.testSettingsCommand(proxy);
            this.testAnvilDamage(proxy);
            this.testCodeBuilder(proxy);
            this.testPositionTrackingDBClientRequest(proxy);
            this.testCodeBuilderSource(proxy);
            this.testRequestAbility(proxy);
            this.testRequestPermissions(proxy);
            this.testGameTestRequest(proxy);
            this.testRefreshEntitlements(proxy);
            this.testPlayerToggleCrafterSlotRequest(proxy);
            this.testSetPlayerInventoryOptions(proxy);
            //this.testInventoryTransaction(proxy);
            //this.testItemStackRequest(proxy);
            this.testUpdateClientOptions(proxy);
        }
    }

    private void testDisconnect(BedrockDebuggerProxy proxy) {
        final DisconnectPacket packet = new DisconnectPacket();
        packet.setReason(DisconnectFailReason.UNKNOWN);
        packet.setMessages(new DisconnectPacketMessages("message", ""));
        proxy.getServer().sendPacket(packet);
    }

    private void testText(BedrockDebuggerProxy proxy) {
        final TextPacket packet = new TextPacket();
        packet.setMessageType(TextPacketType.CHAT);
        final AuthorAndMessage body = new AuthorAndMessage();
        body.setMessage("message");
        body.setPlayerName("Player");
        packet.setSendersXUID("");
        packet.setBody(body);
        proxy.getServer().sendPacket(packet);
    }

    private void testAddPlayer(BedrockDebuggerProxy proxy) {
        final long actorId = 21490;
        final AddPlayerPacket packet = new AddPlayerPacket();
        packet.setUuid(UUID.randomUUID());
        packet.setPlayerName("PlayerName");
        packet.setTargetActorID(actorId);
        packet.setTargetRuntimeID(actorId);
        packet.setPlatformChatId("");
        packet.setPosition(proxy.getPlayer().getPosition());
        packet.setVelocity(Vector3f.ZERO);
        packet.setRotation(proxy.getPlayer().getRotation());
        packet.setCarriedItem(ItemData.AIR);
        packet.setPlayerGameType(GameType.CREATIVE);
        packet.getAbilitiesData().setTargetPlayerRawId(actorId);
        packet.getAbilitiesData().setPlayerPermissions(PlayerPermissionLevel.MEMBER);
        packet.getAbilitiesData().setCommandPermissions(CommandPermissionLevel.ANY);
        final SerializedAbilitiesDataSerializedLayer layer =
            new SerializedAbilitiesDataSerializedLayer();
        layer.setSerializedLayer(SerializedLayer.BASE);
        layer.getAbilitiesSet().addAll(Arrays.asList(AbilitiesIndex.values()));
        layer.getAbilityValues().addAll(Arrays.asList(AbilitiesIndex.values()));
        layer.setWalkSpeed(0.01f);
        layer.setFlySpeed(0.01f);
        packet.getAbilitiesData().getLayers().add(layer);
        packet.setDeviceId("");
        packet.setBuildPlatform(BuildPlatform.GOOGLE);
        proxy.getServer().sendPacket(packet);
    }

    private void testAddItemActor(BedrockDebuggerProxy proxy) {
        final int actorId = 214911;
        final AddItemActorPacket packet = new AddItemActorPacket();
        packet.setTargetActorID(actorId);
        packet.setTargetRuntimeID(proxy.getPlayer().getActorId());
        packet.setItem(ItemData.AIR);
        packet.setPosition(proxy.getPlayer().getPosition().clone().add(3, 1, 3));
        packet.setVelocity(Vector3f.ZERO);
        proxy.getServer().sendPacket(packet);
    }

    private void testServerPlayerPostMovePosition(BedrockDebuggerProxy proxy) {
        final ServerPlayerPostMovePositionPacket packet = new ServerPlayerPostMovePositionPacket();
        packet.setPos(proxy.getPlayer().getPosition());
        proxy.getServer().sendPacket(packet);
    }

    private void testTakeItemActor(BedrockDebuggerProxy proxy) {
        final TakeItemActorPacket packet = new TakeItemActorPacket();
        packet.setItemRuntimeID(214911);
        packet.setActorRuntimeID(proxy.getPlayer().getRuntimeId());
        proxy.getServer().sendPacket(packet);
    }

    private void testMoveActorAbsolute(BedrockDebuggerProxy proxy) {
        final MoveActorAbsolutePacket packet = new MoveActorAbsolutePacket();
        packet.getMoveData().setActorRuntimeID(proxy.getPlayer().getRuntimeId());
        packet.getMoveData().setPos(proxy.getPlayer().getPosition());
        packet.getMoveData().setRotation(proxy.getPlayer().getRotation());
        packet.getMoveData().setOnGround(true);
        proxy.getServer().sendPacket(packet);
    }

    private void testAddPainting(BedrockDebuggerProxy proxy) {
        final long actorId = 214912;
        final AddPaintingPacket packet = new AddPaintingPacket();
        packet.setTargetActorID(actorId);
        packet.setTargetRuntimeID(actorId);
        packet.setPosition(proxy.getPlayer().getPosition());
        packet.setDirection(0);
        packet.setMotif("");
        proxy.getServer().sendPacket(packet);
    }

    private void testLevelEvent(BedrockDebuggerProxy proxy) {
        final LevelEventPacket packet = new LevelEventPacket();
        packet.setType(LevelEvent.SOUND_TOTEM_USED);
        packet.setPosition(proxy.getPlayer().getPosition());
        proxy.getServer().sendPacket(packet);
    }

    private void testBlockEvent(BedrockDebuggerProxy proxy) {
        final BlockEventPacket packet = new BlockEventPacket();
        packet.setBlockPosition(proxy.getPlayer().getPosition().toInt());
        proxy.getServer().sendPacket(packet);
    }

    private void testActorEvent(BedrockDebuggerProxy proxy) {
        final ActorEventPacket packet = new ActorEventPacket();
        packet.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
        packet.setType(ActorEvent.HURT);
        proxy.getServer().sendPacket(packet);
    }

    private void testMobEffect(BedrockDebuggerProxy proxy) {
        final MobEffectPacket packet = new MobEffectPacket();
        packet.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
        packet.setEvent(MobEffectPacket.Event.ADD);
        packet.setEffectID(0);
        packet.setEffectAmplifier(0);
        packet.setShowParticles(false);
        packet.setEffectDurationTicks(40);
        packet.setTick(0L);
        proxy.getServer().sendPacket(packet);
    }

    private void testSetActorLink(BedrockDebuggerProxy proxy) {
        final SetActorLinkPacket packet = new SetActorLinkPacket();
        packet.setLink(new ActorLink(proxy.getPlayer().getActorId(), 0L,
            ActorLinkType.PASSENGER, false, false, 0f));
        proxy.getServer().sendPacket(packet);
    }

    private void testSetSpawnPosition(BedrockDebuggerProxy proxy) {
        final SetSpawnPositionPacket packet = new SetSpawnPositionPacket();
        packet.setSpawnPositionType(SpawnPositionType.PLAYER_RESPAWN);
        packet.setDimensionType(DimensionType.from(Dimension.OVERWORLD));
        packet.setBlockPosition(Vector3i.ZERO);
        proxy.getServer().sendPacket(packet);
    }

    private void testPlayerHotbar(BedrockDebuggerProxy proxy) {
        final PlayerHotbarPacket packet = new PlayerHotbarPacket();
        packet.setContainerID((byte) 0);
        packet.setSelectedSlot(0);
        packet.setShouldSelectSlot(true);
        proxy.getServer().sendPacket(packet);
    }

    private void testContainerSetData(BedrockDebuggerProxy proxy) {
        final ContainerSetDataPacket packet = new ContainerSetDataPacket();
        packet.setContainerID((byte) 0);
        packet.setId(ContainerSetDataPacket.FURNACE_TICK_COUNT);
        packet.setValue(0);
        proxy.getServer().sendPacket(packet);
    }

    private void testGuiDataPickItem(BedrockDebuggerProxy proxy) {
        final GuiDataPickItemPacket packet = new GuiDataPickItemPacket();
        packet.setItemName("minecraft:air");
        packet.setItemEffectName("minecraft:air");
        packet.setSlot(0);
        proxy.getServer().sendPacket(packet);
    }

    private void testSetCommandsEnabled(BedrockDebuggerProxy proxy) {
        final SetCommandsEnabledPacket packet = new SetCommandsEnabledPacket();
        packet.setCommandsEnabled(true);
        proxy.getServer().sendPacket(packet);
    }

    private void testSetDifficulty(BedrockDebuggerProxy proxy) {
        final SetDifficultyPacket packet = new SetDifficultyPacket();
        packet.setDifficulty(Difficulty.EASY.ordinal());
        proxy.getServer().sendPacket(packet);
    }

    private void testChangeDimension(BedrockDebuggerProxy proxy) {
        final ChangeDimensionPacket packet = new ChangeDimensionPacket();
        packet.setDimension(DimensionType.from(Dimension.OVERWORLD));
        packet.setPosition(Vector3f.ZERO);
        proxy.getServer().sendPacket(packet);
    }

    private void testSetPlayerGameType(BedrockDebuggerProxy proxy) {
        final SetPlayerGameTypePacket packet = new SetPlayerGameTypePacket();
        packet.setPlayerGameType(GameType.CREATIVE);
        proxy.getServer().sendPacket(packet);
    }

    private void testSimpleEvent(BedrockDebuggerProxy proxy) {
        final SimpleEventPacket packet = new SimpleEventPacket();
        packet.setType(SimpleEventPacket.Subtype.ENABLE_COMMANDS);
        proxy.getServer().sendPacket(packet);
    }

    private void testSpawnExperienceOrb(BedrockDebuggerProxy proxy) {
        final SpawnExperienceOrbPacket packet = new SpawnExperienceOrbPacket();
        packet.setPosition(proxy.getPlayer().getPosition());
        packet.setXpValue(4000);
        proxy.getServer().sendPacket(packet);
    }

    private void testClientboundMapItemData(BedrockDebuggerProxy proxy) {
        /*final ClientboundMapItemDataPacket packet = new ClientboundMapItemDataPacket();
        packet.getTrackedEntityIds().add(proxy.getPlayer().getActorId());
        packet.setMapID(999L);
        packet.setDimension(DimensionType.from(Dimension.OVERWORLD));
        packet.setMapOrigin(Vector3i.ZERO);
        proxy.getServer().sendPacket(packet);*/
    }

    private void testGameRulesChanged(BedrockDebuggerProxy proxy) {
        final GameRulesChangedPacket packet = new GameRulesChangedPacket();
        packet.getRulesData().getRulesList().add(new GameRuleData<>("showCoordinates", true));
        proxy.getServer().sendPacket(packet);
    }

    private void testCamera(BedrockDebuggerProxy proxy) {
        final CameraPacket packet = new CameraPacket();
        packet.setCameraID(99L);
        packet.setTargetPlayerID(proxy.getPlayer().getActorId());
        proxy.getServer().sendPacket(packet);
    }

    private void testBossEvent(BedrockDebuggerProxy proxy) {
        final BossEventPacket packet = new BossEventPacket();
        packet.setTargetActorID(0);
        packet.setEventType(BossEventUpdateType.ADD);
        packet.setPlayerID(proxy.getPlayer().getActorId());
        packet.setName("Title");
        packet.setHealthPercent(100.0f);
        packet.setColor(BossBarColor.GREEN);
        packet.setOverlay(BossBarOverlay.PROGRESS);
        proxy.getServer().sendPacket(packet);

        final BossEventPacket pk = new BossEventPacket();
        pk.setTargetActorID(0);
        pk.setEventType(BossEventUpdateType.UPDATE_NAME);
        pk.setPlayerID(proxy.getPlayer().getActorId());
        pk.setColor(BossBarColor.GREEN);
        pk.setOverlay(BossBarOverlay.PROGRESS);
        pk.setName("Title");
        proxy.getServer().sendPacket(pk);
    }

    private void testShowCredits(BedrockDebuggerProxy proxy) {
        final ShowCreditsPacket packet = new ShowCreditsPacket();
        packet.setPlayerRuntimeID(proxy.getPlayer().getRuntimeId());
        packet.setCreditsState(ShowCreditsPacket.CreditsState.END_CREDITS);
        proxy.getServer().sendPacket(packet);
    }

    private void testCommandOutput(BedrockDebuggerProxy proxy) {
        final CommandOutputPacket packet = new CommandOutputPacket();
        packet.setOriginData(new CommandOriginData(CommandOriginType.PLAYER, UUID.randomUUID(),
            "", -1L));
        packet.setOutputType(CommandOutputType.ALL_OUTPUT);
        packet.setDataSet("dataSet");
        proxy.getServer().sendPacket(packet);
    }

    private void testUpdateTrade(BedrockDebuggerProxy proxy) {
        final UpdateTradePacket packet = new UpdateTradePacket();
        packet.setContainerId((byte) 0);
        packet.setType(ContainerType.CONTAINER);
        packet.setEntityUniqueId(0);
        packet.setLastTradingPlayer(proxy.getPlayer().getActorId());
        packet.setDisplayName("DisplayName");
        packet.setOffers(NbtMap.EMPTY);
        packet.setUseNewTradeScreen(true);
        proxy.getServer().sendPacket(packet);
    }

    private void testUpdateEquip(BedrockDebuggerProxy proxy) {
        final UpdateEquipPacket packet = new UpdateEquipPacket();
        packet.setContainerId((byte) 0);
        packet.setType((short) ContainerType.HORSE.ordinal());
        packet.setEntityUniqueId(0);
        packet.setTag(NbtMap.EMPTY);
        proxy.getServer().sendPacket(packet);
    }

    private void testTransfer(BedrockDebuggerProxy proxy) {
        final TransferPacket packet = new TransferPacket();
        packet.setServerAddress("127.0.0.1");
        packet.setServerPort(19132);
        proxy.getServer().sendPacket(packet);
    }

    private void testPlaySound(BedrockDebuggerProxy proxy) {
        final PlaySoundPacket packet = new PlaySoundPacket();
        packet.setName("mob.zombie.death");
        packet.setPosition(proxy.getPlayer().getPosition());
        packet.setVolume(1.0f);
        packet.setPitch(1.0f);
        proxy.getServer().sendPacket(packet);
    }

    private void testStopSound(BedrockDebuggerProxy proxy) {
        final StopSoundPacket packet = new StopSoundPacket();
        packet.setSoundName("mob.zombie.death");
        packet.setStopAllSounds(true);
        proxy.getServer().sendPacket(packet);
    }

    private void testAddBehaviorTree(BedrockDebuggerProxy proxy) {
        final AddBehaviorTreePacket packet = new AddBehaviorTreePacket();
        packet.setBehaviorTreeStructureJson("");
        proxy.getServer().sendPacket(packet);
    }

    private void testShowStoreOffer(BedrockDebuggerProxy proxy) {
        final ShowStoreOfferPacket packet = new ShowStoreOfferPacket();
        packet.setProductID(UUID.fromString("bf2d5f9a-3b1b-4f1f-9001-f54479e7cd85"));
        packet.setRedirectType(StoreOfferRedirectType.MARKETPLACE_OFFER);
        proxy.getServer().sendPacket(packet);
    }

    private void testSetLastHurtBy(BedrockDebuggerProxy proxy) {
        final SetLastHurtByPacket packet = new SetLastHurtByPacket();
        packet.setLastHurtBy(0);
        proxy.getServer().sendPacket(packet);
    }

    private void testPhotoTransfer(BedrockDebuggerProxy proxy) {
        final PhotoTransferPacket packet = new PhotoTransferPacket();
        packet.setPhotoName("Photo");
        packet.setPhotoData(new byte[0]);
        packet.setBookID("id");
        packet.setType(PhotoType.BOOK);
        packet.setSourceType(PhotoType.BOOK);
        packet.setOwnerID(proxy.getPlayer().getActorId());
        packet.setNewPhotoName("PhotoNew");
        proxy.getServer().sendPacket(packet);
    }

    private void testModalFormRequest(BedrockDebuggerProxy proxy) {
        final ModalFormRequestPacket packet = new ModalFormRequestPacket();
        packet.setFormID(0);
        packet.setFormData("{}");
        proxy.getServer().sendPacket(packet);
    }

    private void testServerSettingsResponse(BedrockDebuggerProxy proxy) {
        final ServerSettingsResponsePacket packet = new ServerSettingsResponsePacket();
        packet.setFormID(0);
        packet.setFormData("{}");
        proxy.getServer().sendPacket(packet);
    }

    private void testShowProfile(BedrockDebuggerProxy proxy) {
        final ShowProfilePacket packet = new ShowProfilePacket();
        packet.setPlayerXUID("2535412609893193");
        proxy.getServer().sendPacket(packet);
    }

    private void testSetDefaultGameType(BedrockDebuggerProxy proxy) {
        final SetDefaultGameTypePacket packet = new SetDefaultGameTypePacket();
        packet.setGameType(GameType.CREATIVE);
        proxy.getServer().sendPacket(packet);
    }

    private void testRemoveObjective(BedrockDebuggerProxy proxy) {
        final RemoveObjectivePacket packet = new RemoveObjectivePacket();
        packet.setObjectiveName("ObjectiveName");
        proxy.getServer().sendPacket(packet);
    }

    private void testSetDisplayObjective(BedrockDebuggerProxy proxy) {
        final SetDisplayObjectivePacket packet = new SetDisplayObjectivePacket();
        packet.setDisplaySlotName("DisplaySlotName");
        packet.setObjectiveName("ObjectiveName");
        packet.setObjectiveDisplayName("ObjectiveDisplayName");
        packet.setCriteriaName("criteriaName");
        packet.setSortOrder(ObjectiveSortOrder.ASCENDING);
        proxy.getServer().sendPacket(packet);
    }

    private void testLabTable(BedrockDebuggerProxy proxy) {
        final LabTablePacket packet = new LabTablePacket();
        packet.setType(LabTablePacket.Type.RESET);
        packet.setPosition(Vector3i.ZERO);
        packet.setReaction(LabTableReactionType.NONE);
        proxy.getServer().sendPacket(packet);
    }

    private void testUpdateBlockSynced(BedrockDebuggerProxy proxy) {
        final UpdateBlockSyncedPacket packet = new UpdateBlockSyncedPacket();
        packet.setUniqueActorId(proxy.getPlayer().getActorId());
        packet.setActorSyncMessage(ActorBlockSyncMessageId.NONE);
        packet.setBlockPosition(Vector3i.ZERO);
        packet.setDefinition(() -> 0);
        packet.setLayer(0);
        proxy.getServer().sendPacket(packet);
    }

    private void testMoveActorDelta(BedrockDebuggerProxy proxy) {
      /*  final MoveActorDeltaPacket packet = new MoveActorDeltaPacket();
        packet.getData().setActorRuntimeID(proxy.getPlayer().getRuntimeId());
        packet.setDeltaX(1);
        packet.setDeltaY(1);
        packet.setDeltaZ(1);
        packet.getData().setNewPositionX(proxy.getPlayer().getPosition().getX());
        packet.getData().setNewPositionY(proxy.getPlayer().getPosition().getY());
        packet.getData().setNewPositionZ(proxy.getPlayer().getPosition().getZ());
        packet.getData().setRotationX(0f);
        packet.getData().setRotationY(0f);
        packet.getData().setRotationYHead(0f);
        proxy.getServer().sendPacket(packet);*/
    }

    private void testUpdateSoftEnum(BedrockDebuggerProxy proxy) {
        final UpdateSoftEnumPacket packet = new UpdateSoftEnumPacket();
        packet.setSoftEnum(new CommandEnumData("soft", Collections.emptyMap(), true));
        packet.setUpdateType(SoftEnumUpdateType.ADD);
        proxy.getServer().sendPacket(packet);
    }

    private void testSpawnParticleEffect(BedrockDebuggerProxy proxy) {
        final SpawnParticleEffectPacket packet = new SpawnParticleEffectPacket();
        packet.setDimensionId(DimensionType.from(Dimension.OVERWORLD));
        packet.setActorId(proxy.getPlayer().getActorId());
        packet.setPosition(proxy.getPlayer().getPosition());
        packet.setEffectName("minecraft:flame");
        packet.setMolangVariables(Optional.empty());
        proxy.getServer().sendPacket(packet);
    }

    private void testLevelEventGeneric(BedrockDebuggerProxy proxy) {
        final LevelEventGenericPacket packet = new LevelEventGenericPacket();
        packet.setType(ParticleType.RED_DUST);
        packet.setTag(NbtMap.EMPTY);
        proxy.getServer().sendPacket(packet);
    }

    private void testOnScreenTextureAnimation(BedrockDebuggerProxy proxy) {
        final OnScreenTextureAnimationPacket packet = new OnScreenTextureAnimationPacket();
        packet.setEffectId(0L);
        proxy.getServer().sendPacket(packet);
    }

    private void testStructureTemplateDataResponse(BedrockDebuggerProxy proxy) {
        final StructureTemplateDataResponsePacket packet =
            new StructureTemplateDataResponsePacket();
        packet.setStructureName("minecraft:village");
        packet.setSave(false);
        packet.setStructureNBT(NbtMap.EMPTY);
        packet.setResponseType(StructureTemplateResponseType.NONE);
        proxy.getServer().sendPacket(packet);
    }

    private void testEducationSettings(BedrockDebuggerProxy proxy) {
        final EducationSettingsPacket packet = new EducationSettingsPacket();
        final EducationLevelSettings educationLevelSettings = new EducationLevelSettings();
        educationLevelSettings.setCodeBuilderDefaultURI("");
        educationLevelSettings.setCodeBuilderTitle("title");
        educationLevelSettings.setPostProcessFilter("filter");
        educationLevelSettings.setScreenshotBorderResourcePath("path");
        educationLevelSettings.setAgentCapabilities(OptionalBoolean.empty());
        educationLevelSettings.setCodeBuilderOverrideUri(Optional.empty());
        educationLevelSettings.setExternalLinkSettings(OptionalBoolean.empty());
        packet.setEducationLevelSettings(educationLevelSettings);
        proxy.getServer().sendPacket(packet);
    }

    private void testCompletedUsingItem(BedrockDebuggerProxy proxy) {
        final CompletedUsingItemPacket packet = new CompletedUsingItemPacket();
        packet.setItemId(0);
        packet.setItemUseMethod(ItemUseMethod.UNKNOWN);
        proxy.getServer().sendPacket(packet);
    }

    private void testPlayerEnchantOptions(BedrockDebuggerProxy proxy) {
        final PlayerEnchantOptionsPacket packet = new PlayerEnchantOptionsPacket();
        packet.getOptions().add(
            new ItemEnchantOption(1,
                new ItemEnchants(
                    0,
                    List.of(new EnchantmentInstance(0, 0)),
                    List.of(new EnchantmentInstance(1, 0)),
                    List.of(new EnchantmentInstance(2, 0))
                ),
                "minecraft:test_enchant",
                1337
            )
        );
        proxy.getServer().sendPacket(packet);
    }

    private void testPlayerArmorDamage(BedrockDebuggerProxy proxy) {
        final PlayerArmorDamagePacket packet = new PlayerArmorDamagePacket();
        for (int i = 0; i < 5; i++) {
            packet.getDamageForSlot()[i] = 0;
        }
        packet.getFlags().addAll(List.of(PlayerArmorDamageFlag.values()));
        proxy.getServer().sendPacket(packet);
    }

    private void testUpdatePlayerGameType(BedrockDebuggerProxy proxy) {
        final UpdatePlayerGameTypePacket packet = new UpdatePlayerGameTypePacket();
        packet.setTargetPlayer(proxy.getPlayer().getActorId());
        packet.setPlayerGameType(GameType.CREATIVE);
        proxy.getServer().sendPacket(packet);
    }

    private void testPositionTrackingDBServerBroadcastPacket(BedrockDebuggerProxy proxy) {
        final PositionTrackingDBServerBroadcastPacket packet =
            new PositionTrackingDBServerBroadcastPacket();
        packet.setAction(PositionTrackingDBServerBroadcastPacket.Action.NOT_FOUND);
        packet.setTrackingId(0);
        packet.setPositionTrackingData(NbtMap.EMPTY);
        proxy.getServer().sendPacket(packet);
    }

    private void testDebugInfoPacket(BedrockDebuggerProxy proxy) {
        final DebugInfoPacket packet = new DebugInfoPacket();
        packet.setActorId(proxy.getPlayer().getActorId());
        packet.setData("");
        proxy.getServer().sendPacket(packet);
    }

    private void testMotionPredictionHints(BedrockDebuggerProxy proxy) {
        final MotionPredictionHintsPacket packet = new MotionPredictionHintsPacket();
        packet.setRuntimeId(proxy.getPlayer().getRuntimeId());
        packet.setMotion(Vector3f.ZERO);
        packet.setOnGround(true);
        proxy.getServer().sendPacket(packet);
    }

    private void testAnimateEntity(BedrockDebuggerProxy proxy) {
        final AnimateEntityPacket packet = new AnimateEntityPacket();
        packet.setAnimation("");
        packet.setNextState("");
        packet.setStopExpression("");
        packet.setController("");
        proxy.getServer().sendPacket(packet);
    }

    private void testCameraShake(BedrockDebuggerProxy proxy) {
        final CameraShakePacket packet = new CameraShakePacket();
        packet.setShakeType(CameraShakeType.POSITIONAL);
        packet.setShakeAction(CameraShakeAction.ADD);
        proxy.getServer().sendPacket(packet);
    }

    private void testPlayerFog(BedrockDebuggerProxy proxy) {
        final PlayerFogPacket packet = new PlayerFogPacket();
        packet.getFogStack().add("test");
        proxy.getServer().sendPacket(packet);
    }

    private void testCorrectPlayerMovePrediction(BedrockDebuggerProxy proxy) {
        final CorrectPlayerMovePredictionPacket packet = new CorrectPlayerMovePredictionPacket();
        packet.setPos(proxy.getPlayer().getPosition());
        packet.setPosDelta(Vector3f.ZERO);
        packet.setOnGround(true);
        packet.setPredictionType(PredictionType.VEHICLE);
        packet.setVehicleRotation(Vector2f.ZERO);
        packet.setVehicleAngularVelocity(1.0f);
        proxy.getServer().sendPacket(packet);
    }

    private void testClientboundDebugRenderer(BedrockDebuggerProxy proxy) {
        final ClientboundDebugRendererPacket packet = new ClientboundDebugRendererPacket();
        packet.setType(PayloadType.ADD_DEBUG_MARKER_CUBE);

        final DebugMarkerData data = new DebugMarkerData();
        data.setText("debugMarkerText");
        data.setColor(Color.RED.getRGB());
        data.setPosition(Vector3f.ZERO);

        packet.setDebugMarkerData(data);
        proxy.getServer().sendPacket(packet);
    }

    private void testSyncActorProperty(BedrockDebuggerProxy proxy) {
        final SyncActorPropertyPacket packet = new SyncActorPropertyPacket();
        packet.setPropertyData(NbtMap.EMPTY);
        proxy.getServer().sendPacket(packet);
    }

    private void testAddVolumeEntity(BedrockDebuggerProxy proxy) {
        final AddVolumeEntityPacket packet = new AddVolumeEntityPacket();
        packet.setEntityNetworkId(1337);
        packet.setComponents(NbtMap.EMPTY);
        packet.setIdentifier("minecraft:test");
        packet.setInstanceName("instanceName");
        packet.setMinBounds(Vector3i.ZERO);
        packet.setMaxBounds(Vector3i.ZERO);
        packet.setDimensionType(DimensionType.from(Dimension.OVERWORLD));
        packet.setEngineVersion("0.0.0");
        proxy.getServer().sendPacket(packet);
    }

    private void testRemoveVolumeEntity(BedrockDebuggerProxy proxy) {
        final RemoveVolumeEntityPacket packet = new RemoveVolumeEntityPacket();
        packet.setEntityNetworkId(1337);
        packet.setDimensionType(DimensionType.from(Dimension.OVERWORLD));
        proxy.getServer().sendPacket(packet);
    }

    private void testSimulationType(BedrockDebuggerProxy proxy) {
        final SimulationTypePacket packet = new SimulationTypePacket();
        packet.setSimType(SimulationType.TEST);
        proxy.getServer().sendPacket(packet);
    }

    private void testNpcDialogue(BedrockDebuggerProxy proxy) {
        final NpcDialoguePacket packet = new NpcDialoguePacket();
        packet.setNpcId(0);
        packet.setActionType(NpcDialoguePacket.Action.CLOSE);
        packet.setDialogue("dialogue");
        packet.setSceneName("scene");
        packet.setNpcName("npc");
        packet.setActionJson("");
        proxy.getServer().sendPacket(packet);
    }

    private void testEduUriResource(BedrockDebuggerProxy proxy) {
        final EduUriResourcePacket packet = new EduUriResourcePacket();
        packet.setEduSharedUriResource(EduSharedUriResource.EMPTY);
        proxy.getServer().sendPacket(packet);
    }

    private void testCreatePhoto(BedrockDebuggerProxy proxy) {
        final CreatePhotoPacket packet = new CreatePhotoPacket();
        packet.setPhotoName("Photo");
        packet.setPhotoItemName("minecraft:air");
        proxy.getServer().sendPacket(packet);
    }

    private void testUpdateSubChunkBlocks(BedrockDebuggerProxy proxy) {
        final UpdateSubChunkBlocksPacket packet = new UpdateSubChunkBlocksPacket();
        packet.setSubChunkBlockPosition(Vector3i.ZERO);
        packet.getStandardBlocks().add(new BlockChangeEntry(Vector3i.ZERO, () -> 0, 0, 0L,
            ActorBlockSyncMessageId.NONE));
        packet.getExtraBlocks().add(new BlockChangeEntry(Vector3i.ZERO, () -> 0, 0, 0L,
            ActorBlockSyncMessageId.NONE));
        proxy.getServer().sendPacket(packet);
    }

    private void testPlayerStartItemCooldown(BedrockDebuggerProxy proxy) {
        final PlayerStartItemCooldownPacket packet = new PlayerStartItemCooldownPacket();
        packet.setItemCategory("minecraft:air");
        packet.setDurationTicks(20);
        proxy.getServer().sendPacket(packet);
    }

    private void testScriptMessage(BedrockDebuggerProxy proxy) {
        final ScriptMessagePacket packet = new ScriptMessagePacket();
        packet.setMessageId("messageID");
        packet.setMessageValue("messageVal");
        proxy.getServer().sendPacket(packet);
    }

    private void testTickingAreasLoadStatus(BedrockDebuggerProxy proxy) {
        final TickingAreasLoadStatusPacket packet = new TickingAreasLoadStatusPacket();
        proxy.getServer().sendPacket(packet);
    }

    private void testDimensionData(BedrockDebuggerProxy proxy) {
        final DimensionDataPacket packet = new DimensionDataPacket();
        packet.getDefinitions().add(
            new DimensionDefinition(
                "minecraft:overworld",
                320,
                -64,
                GeneratorType.LEGACY,
                DimensionType.from(Dimension.OVERWORLD),
                new UUID(0L, 0L),
                "minecraft:ocean"
            )
        );
        proxy.getServer().sendPacket(packet);
    }

    private void testAgentActionEvent(BedrockDebuggerProxy proxy) {
        final AgentActionEventPacket packet = new AgentActionEventPacket();
        packet.setRequestId("requestID");
        packet.setAction(AgentActionType.NONE);
        packet.setResponse("");
        proxy.getServer().sendPacket(packet);
    }

    private void testChangeMobProperty(BedrockDebuggerProxy proxy) {
        final ChangeMobPropertyPacket packet = new ChangeMobPropertyPacket();
        packet.setActorId(proxy.getPlayer().getActorId());
        packet.setPropertyName("property");
        packet.setStringComponentValue("s");
        proxy.getServer().sendPacket(packet);
    }

    private void testLessonProgress(BedrockDebuggerProxy proxy) {
        final LessonProgressPacket packet = new LessonProgressPacket();
        packet.setLessonAction(LessonAction.START);
        packet.setActivityId("activityID");
        proxy.getServer().sendPacket(packet);
    }

    private void testToastRequest(BedrockDebuggerProxy proxy) {
        final ToastRequestPacket packet = new ToastRequestPacket();
        packet.setTitle("title");
        packet.setContent("content");
        proxy.getServer().sendPacket(packet);
    }

    private void testDeathInfo(BedrockDebuggerProxy proxy) {
        final DeathInfoPacket packet = new DeathInfoPacket();
        packet.setDeathCauseAttackName("attackName");
        packet.getDeathCauseMessageList().add("test");
        proxy.getServer().sendPacket(packet);
    }

    private void testEditorNetwork(BedrockDebuggerProxy proxy) {
        final EditorNetworkPacket packet = new EditorNetworkPacket();
        packet.setRawVariantName("");
        packet.setRawVariantData("");
        proxy.getServer().sendPacket(packet);
    }

    private void testFeatureRegistry(BedrockDebuggerProxy proxy) {
        final FeatureRegistryPacket packet = new FeatureRegistryPacket();
        packet.getFeaturesDataList().add(new FeatureDefinition("minecraft:feature", ""));
        proxy.getServer().sendPacket(packet);
    }

    private void testServerStats(BedrockDebuggerProxy proxy) {
        final ServerStatsPacket packet = new ServerStatsPacket();
        proxy.getServer().sendPacket(packet);
    }

    private void testGameTestResults(BedrockDebuggerProxy proxy) {
        final GameTestResultsPacket packet = new GameTestResultsPacket();
        packet.setError("error");
        packet.setTestName("test");
        proxy.getServer().sendPacket(packet);
    }

    private void testUpdateClientInputLocks(BedrockDebuggerProxy proxy) {
        final UpdateClientInputLocksPacket packet = new UpdateClientInputLocksPacket();
        packet.setServerPos(proxy.getPlayer().getPosition());
        proxy.getServer().sendPacket(packet);
    }

    private void testUnlockedRecipes(BedrockDebuggerProxy proxy) {
        final UnlockedRecipesPacket packet = new UnlockedRecipesPacket();
        packet.setType(UnlockedRecipesPacket.UnlockedRecipesPacketType.REMOVE_ALL);
        packet.getUnlockedRecipesList().add("minecraft:acacia_button");
        proxy.getServer().sendPacket(packet);
    }

    private void testCameraInstruction(BedrockDebuggerProxy proxy) {
        final CameraSetInstruction setInstruction = new CameraSetInstruction();
        setInstruction.setPreset(proxy.getServer().getCameraPresetDefinitions().getDefinition(0));

        final CameraFadeInstruction fadeInstruction = new CameraFadeInstruction();
        fadeInstruction.setTime(new CameraFadeInstruction.TimeOption(1f, 1f, 1f));
        fadeInstruction.setColor(new CameraFadeInstruction.ColorOption(Color.YELLOW.getRed(),
            Color.YELLOW.getGreen(), Color.YELLOW.getBlue()));

        final CameraTargetInstruction targetInstruction = new CameraTargetInstruction();
        targetInstruction.setTargetCenterOffset(Vector3f.ZERO);
        targetInstruction.setTargetActorID(proxy.getPlayer().getActorId());

        final CameraInstructionPacket packet = new CameraInstructionPacket();
        packet.setSetInstruction(setInstruction);

        proxy.getServer().sendPacket(packet);

        /*final CameraInstructionPacket pk0 = new CameraInstructionPacket();
        pk0.setFadeInstruction(fadeInstruction);
        proxy.getServer().sendPacket(pk0);*/

        /*final CameraInstructionPacket pk0 = new CameraInstructionPacket();
        pk0.setTargetInstruction(targetInstruction);
        proxy.getServer().sendPacket(pk0);*/
    }

    private void testNetworkStackLatency(BedrockDebuggerProxy proxy) {
        final NetworkStackLatencyPacket packet = new NetworkStackLatencyPacket();
        packet.setCreationTime(0);
        packet.setFromServer(true);
        proxy.getServer().sendPacket(packet);
    }

    private void testMobEquipment(BedrockDebuggerProxy proxy) {
        final MobEquipmentPacket packet = new MobEquipmentPacket();
        packet.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
        packet.setItem(ItemData.AIR);
        proxy.getServer().sendPacket(packet);
    }

    private void testMobArmorEquipment(BedrockDebuggerProxy proxy) {
        final MobArmorEquipmentPacket packet = new MobArmorEquipmentPacket();
        packet.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
        packet.setHead(ItemData.AIR);
        packet.setTorso(ItemData.AIR);
        packet.setLegs(ItemData.AIR);
        packet.setFeet(ItemData.AIR);
        packet.setBody(ItemData.AIR);
        proxy.getServer().sendPacket(packet);
    }

    private void testLevelSoundEvent(BedrockDebuggerProxy proxy) {
        final LevelSoundEventPacket packet = new LevelSoundEventPacket();
        packet.setSoundEvent(SoundEvent.ADMIRE);
        packet.setPosition(Vector3f.ZERO);
        packet.setActorIdentifier("");
        proxy.getServer().sendPacket(packet);
    }

    private void testRespawn(BedrockDebuggerProxy proxy) {
        final RespawnPacket packet = new RespawnPacket();
        packet.setPlayerRuntimeId(proxy.getPlayer().getRuntimeId());
        packet.setPosition(proxy.getPlayer().getPosition());
        packet.setState(PlayerRespawnState.READY_TO_SPAWN);
        proxy.getServer().sendPacket(packet);
    }

    private void testSetActorMotion(BedrockDebuggerProxy proxy) {
        final SetActorMotionPacket packet = new SetActorMotionPacket();
        packet.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
        packet.setMotion(Vector3f.ZERO);
        proxy.getServer().sendPacket(packet);
    }

    private void testSetHealth(BedrockDebuggerProxy proxy) {
        final SetHealthPacket packet = new SetHealthPacket();
        packet.setHealth(20);
        proxy.getServer().sendPacket(packet);
    }

    private void testSetTime(BedrockDebuggerProxy proxy) {
        final SetTimePacket packet = new SetTimePacket();
        proxy.getServer().sendPacket(packet);
    }

    private void testSetTitle(BedrockDebuggerProxy proxy) {
        final SetTitlePacket packet = new SetTitlePacket();
        packet.setTitleType(SetTitlePacket.TitleType.TITLE);
        packet.setTitleText("text");
        packet.setFadeInTime(20);
        packet.setStayTime(20);
        packet.setFadeOutTime(20);
        packet.setXuid("");
        packet.setPlatformOnlineId("");
        proxy.getServer().sendPacket(packet);
    }

    private void testUpdateAbilities(BedrockDebuggerProxy proxy) {
        final UpdateAbilitiesPacket packet = new UpdateAbilitiesPacket();
        final SerializedAbilitiesDataSerializedLayer layer =
            new SerializedAbilitiesDataSerializedLayer();
        layer.setSerializedLayer(SerializedLayer.BASE);
        layer.getAbilitiesSet().addAll(Arrays.asList(AbilitiesIndex.values()));
        layer.getAbilityValues().addAll(Arrays.asList(AbilitiesIndex.values()));
        layer.setWalkSpeed(0.01f);
        layer.setFlySpeed(0.01f);
        packet.getData().getLayers().add(layer);
        final SerializedAbilitiesData data = new SerializedAbilitiesData();
        data.setTargetPlayerRawId(proxy.getPlayer().getActorId());
        data.setPlayerPermissions(PlayerPermissionLevel.OPERATOR);
        data.setCommandPermissions(CommandPermissionLevel.OWNER);
        data.getLayers().add(layer);

        packet.setData(data);
        proxy.getServer().sendPacket(packet);
    }

    private void testUpdateAdventureSettings(BedrockDebuggerProxy proxy) {
        final UpdateAdventureSettingsPacket packet = new UpdateAdventureSettingsPacket();
        proxy.getServer().sendPacket(packet);
    }

    private void testUpdateAttributes(BedrockDebuggerProxy proxy) {
        final UpdateAttributesPacket packet = new UpdateAttributesPacket();
        packet.setRuntimeID(proxy.getPlayer().getRuntimeId());
        packet.setAttributeList(List.of(new AttributeData("minecraft:luck", -1024, 1024, 0, 0,
            List.of(new AttributeModifier("minecraft:test", "test", 1,
                AttributeModifierOperation.OPERATION_ADDITION, AttributeOperands.OPERAND_MIN,
                true)))));
        proxy.getServer().sendPacket(packet);
    }

    private void testUpdateBlock(BedrockDebuggerProxy proxy) {
        final UpdateBlockPacket packet = new UpdateBlockPacket();
        packet.setBlockPosition(Vector3i.ZERO);
        packet.setDefinition(() -> 0);
        proxy.getServer().sendPacket(packet);
    }

    private void testOpenSign(BedrockDebuggerProxy proxy) {
        final OpenSignPacket packet = new OpenSignPacket();
        packet.setPos(Vector3i.ZERO);
        proxy.getServer().sendPacket(packet);
    }

    private void testAgentAnimation(BedrockDebuggerProxy proxy) {
        final AgentAnimationPacket packet = new AgentAnimationPacket();
        packet.setAgentAnimation((byte) 0);
        packet.setRuntimeId(proxy.getPlayer().getRuntimeId());
        proxy.getServer().sendPacket(packet);
    }

    private void testSetHud(BedrockDebuggerProxy proxy) {
        final SetHudPacket packet = new SetHudPacket();
        packet.getHudElementList().addAll(List.of(HudElement.values()));
        packet.setHudVisible(HudVisibility.RESET);
        proxy.getServer().sendPacket(packet);
    }

    private void testAwardAchievement(BedrockDebuggerProxy proxy) {
        final AwardAchievementPacket packet = new AwardAchievementPacket();
        packet.setAchievementID(0);
        proxy.getServer().sendPacket(packet);
    }

    private void testClientboundCloseForm(BedrockDebuggerProxy proxy) {
        final ClientboundCloseFormPacket packet = new ClientboundCloseFormPacket();
        proxy.getServer().sendPacket(packet);
    }

    private void testJigsawStructureData(BedrockDebuggerProxy proxy) {
        final JigsawStructureDataPacket packet = new JigsawStructureDataPacket();
        packet.setJigsawStructureDataTag(NbtMap.EMPTY);
        proxy.getServer().sendPacket(packet);
    }

    private void testCurrentStructureFeature(BedrockDebuggerProxy proxy) {
        final CurrentStructureFeaturePacket packet = new CurrentStructureFeaturePacket();
        packet.setCurrentStructureFeature("");
        proxy.getServer().sendPacket(packet);
    }

    private void testInventorySlot(BedrockDebuggerProxy proxy) {
        final InventorySlotPacket packet = new InventorySlotPacket();
        packet.setItem(ItemData.AIR);
        packet.setFullContainerName(new FullContainerName(ContainerEnumName.ANVIL_INPUT_CONTAINER
            , null));
        packet.setStorageItem(ItemData.AIR);
        proxy.getServer().sendPacket(packet);
    }

    private void testInventoryContent(BedrockDebuggerProxy proxy) {
        final InventoryContentPacket packet = new InventoryContentPacket();
        packet.getSlots().add(ItemData.AIR);
        packet.setFullContainerName(new FullContainerName(ContainerEnumName.ANVIL_INPUT_CONTAINER
            , null));
        packet.setStorageItem(ItemData.AIR);
        proxy.getServer().sendPacket(packet);
    }

    private void testInteract(BedrockDebuggerProxy proxy) {
        final InteractPacket packet = new InteractPacket();
        packet.setAction(InteractPacket.Action.INVALID);
        packet.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
        packet.setPosition(Vector3f.ZERO);
        proxy.getClient().sendPacket(packet);
    }

    private void testBlockPickRequest(BedrockDebuggerProxy proxy) {
        final BlockPickRequestPacket packet = new BlockPickRequestPacket();
        packet.setPosition(Vector3i.ZERO);
        packet.setMaxSlots(0);
        packet.setWithData(true);
        proxy.getClient().sendPacket(packet);
    }

    private void testActorPickRequest(BedrockDebuggerProxy proxy) {
        final ActorPickRequestPacket packet = new ActorPickRequestPacket();
        packet.setActorID(proxy.getPlayer().getActorId());
        packet.setMaxSlots(0);
        packet.setWithData(true);
        proxy.getClient().sendPacket(packet);
    }

    private void testPlayerAction(BedrockDebuggerProxy proxy) {
        final PlayerActionPacket packet = new PlayerActionPacket();
        packet.setPlayerRuntimeID(proxy.getPlayer().getRuntimeId());
        packet.setAction(PlayerActionType.START_ITEM_USE_ON);
        packet.setBlockPosition(proxy.getPlayer().getPosition().toInt());
        packet.setResultPos(proxy.getPlayer().getPosition().toInt());
        proxy.getClient().sendPacket(packet);
    }

    private void testAnimate(BedrockDebuggerProxy proxy) {
        final AnimatePacket packet = new AnimatePacket();
        packet.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
        packet.setAction(AnimatePacket.Action.SWING);
        proxy.getClient().sendPacket(packet);
    }

    private void testMapInfoRequest(BedrockDebuggerProxy proxy) {
        final MapInfoRequestPacket packet = new MapInfoRequestPacket();
        packet.setMapUniqueID(999L);
        packet.getClientPixelsList().add(new MapPixel(10, 1));
        proxy.getClient().sendPacket(packet);
    }

    private void testCommandBlockUpdate(BedrockDebuggerProxy proxy) {
        final CommandBlockUpdatePacket packet = new CommandBlockUpdatePacket();
        final BlockCommandData data = new BlockCommandData();
        data.setBlockPosition(Vector3i.ZERO);
        data.setCommandBlockMode(CommandBlockMode.NORMAL);
        packet.setTarget(data);
        packet.setCommand("/gamemode 1 @a");
        packet.setLastOutput("");
        packet.setName("Name");
        packet.setFilteredName("");
        proxy.getClient().sendPacket(packet);
    }

    private void testStructureBlockUpdate(BedrockDebuggerProxy proxy) {
        final StructureBlockUpdatePacket packet = new StructureBlockUpdatePacket();
        packet.setBlockPosition(Vector3i.ZERO);
        packet.setStructureData(new StructureEditorData(new RedactableString("minecraft:test", ""),
            "dataField", false, true, StructureBlockType.CORNER, this.structureSettings,
            StructureRedstoneSaveMode.SAVES_TO_DISK));
        proxy.getClient().sendPacket(packet);
    }

    private void testPurchaseReceipt(BedrockDebuggerProxy proxy) {
        final PurchaseReceiptPacket packet = new PurchaseReceiptPacket();
        packet.getPurchaseReceipts().add("{}");
        proxy.getClient().sendPacket(packet);
    }

    private void testBookEdit(BedrockDebuggerProxy proxy) {
        final BookEditAction.Finalize finalize = new BookEditAction.Finalize();
        finalize.setTitle("Title");
        finalize.setAuthor("Author");
        finalize.setXuid("");

        final BookEditPacket packet = new BookEditPacket();
        packet.setBookSlot(0);
        packet.setOperation(finalize);

        proxy.getClient().sendPacket(packet);
    }

    private void testNpcRequest(BedrockDebuggerProxy proxy) {
        final NpcRequestPacket packet = new NpcRequestPacket();
        packet.setNpcRuntimeID(0L);
        packet.setRequestType(NpcRequestPacket.RequestType.SET_NAME);
        packet.setActions("");
        packet.setSceneName("scene");
        proxy.getClient().sendPacket(packet);
    }

    private void testModalFormResponse(BedrockDebuggerProxy proxy) {
        final ModalFormRequestPacket packet = new ModalFormRequestPacket();
        packet.setFormID(0);
        packet.setFormData("");
        proxy.getClient().sendPacket(packet);
    }

    private void testServerSettingsRequest(BedrockDebuggerProxy proxy) {
        final ServerSettingsRequestPacket packet = new ServerSettingsRequestPacket();
        proxy.getClient().sendPacket(packet);
    }

    private void testLecternUpdate(BedrockDebuggerProxy proxy) {
        final LecternUpdatePacket packet = new LecternUpdatePacket();
        packet.setPositionOfLecternToUpdate(Vector3i.ZERO);
        proxy.getClient().sendPacket(packet);
    }

    private void testMapCreateLockedCopy(BedrockDebuggerProxy proxy) {
        final MapCreateLockedCopyPacket packet = new MapCreateLockedCopyPacket();
        packet.setNewMapId(0L);
        packet.setOriginalMapId(1L);
        proxy.getClient().sendPacket(packet);
    }

    private void testStructureTemplateDataRequest(BedrockDebuggerProxy proxy) {
        final StructureTemplateDataRequestPacket packet = new StructureTemplateDataRequestPacket();
        packet.setStructureName("minecraft:test");
        packet.setStructurePosition(Vector3i.ZERO);
        packet.setStructureSettings(this.structureSettings);
        packet.setRequestedOperation(StructureTemplateRequestOperation.NONE);
        proxy.getClient().sendPacket(packet);
    }

    private void testEmotePacket(BedrockDebuggerProxy proxy) {
        final EmotePacket packet = new EmotePacket();
        packet.setActorRuntimeId(proxy.getPlayer().getActorId());
        packet.setXuid("");
        packet.setPlatformId("");
        packet.setEmoteId(UUID.randomUUID().toString());
        packet.setEmoteLengthTicks(40);
        proxy.getClient().sendPacket(packet);
    }

    private void testMultiplayerSettings(BedrockDebuggerProxy proxy) {
        final MultiplayerSettingsPacket packet = new MultiplayerSettingsPacket();
        packet.setType(MultiplayerSettingsPacketType.ENABLE_MULTIPLAYER);
        proxy.getClient().sendPacket(packet);
    }

    private void testSettingsCommand(BedrockDebuggerProxy proxy) {
        final SettingsCommandPacket packet = new SettingsCommandPacket();
        packet.setCommand("/test");
        proxy.getClient().sendPacket(packet);
    }

    private void testAnvilDamage(BedrockDebuggerProxy proxy) {
        final AnvilDamagePacket packet = new AnvilDamagePacket();
        packet.setDamageAmount(1);
        packet.setBlockPosition(Vector3i.ZERO);
        proxy.getClient().sendPacket(packet);
    }

    private void testCodeBuilder(BedrockDebuggerProxy proxy) {
        final CodeBuilderPacket packet = new CodeBuilderPacket();
        packet.setUrl("https://minecraft.net");
        proxy.getClient().sendPacket(packet);
    }

    private void testPositionTrackingDBClientRequest(BedrockDebuggerProxy proxy) {
        final PositionTrackingDBClientRequestPacket packet =
            new PositionTrackingDBClientRequestPacket();
        packet.setAction(PositionTrackingDBClientRequestPacket.Action.QUERY);
        proxy.getClient().sendPacket(packet);
    }

    private void testCodeBuilderSource(BedrockDebuggerProxy proxy) {
        final CodeBuilderSourcePacket packet = new CodeBuilderSourcePacket();
        packet.setOperation(CodeBuilderOperationType.GET);
        packet.setCategory(CodeBuilderCategoryType.CODE_STATUS);
        packet.setCodeStatus(CodeBuilderCodeStatus.SUCCEEDED);
        proxy.getClient().sendPacket(packet);
    }

    private void testRequestAbility(BedrockDebuggerProxy proxy) {
        final RequestAbilityPacket packet = new RequestAbilityPacket();
        packet.setAbility(AbilitiesIndex.BUILD);
        packet.setValueType(AbilitiesIndex.Type.BOOLEAN);
        packet.setBoolValue(true);
        proxy.getClient().sendPacket(packet);
    }

    private void testRequestPermissions(BedrockDebuggerProxy proxy) {
        final RequestPermissionsPacket packet = new RequestPermissionsPacket();
        packet.setPlayerPermissionLevel(PlayerPermissionLevel.OPERATOR);
        packet.setTargetPlayerId(proxy.getPlayer().getActorId());
        proxy.getClient().sendPacket(packet);
    }

    private void testGameTestRequest(BedrockDebuggerProxy proxy) {
        final GameTestRequestPacket packet = new GameTestRequestPacket();
        packet.setTestPos(Vector3i.ZERO);
        packet.setTestName("test");
        proxy.getClient().sendPacket(packet);
    }

    private void testRefreshEntitlements(BedrockDebuggerProxy proxy) {
        final RefreshEntitlementsPacket packet = new RefreshEntitlementsPacket();
        proxy.getClient().sendPacket(packet);
    }

    private void testPlayerToggleCrafterSlotRequest(BedrockDebuggerProxy proxy) {
        final PlayerToggleCrafterSlotRequestPacket packet =
            new PlayerToggleCrafterSlotRequestPacket();
        packet.setPos(Vector3i.ZERO);
        proxy.getClient().sendPacket(packet);
    }

    private void testSetPlayerInventoryOptions(BedrockDebuggerProxy proxy) {
        final SetPlayerInventoryOptionsPacket packet = new SetPlayerInventoryOptionsPacket();
        packet.setLeftInventoryTab(InventoryLeftTabIndex.SURVIVAL);
        packet.setRightInventoryTab(InventoryRightTabIndex.ARMOR);
        packet.setLayoutInv(InventoryLayout.DEFAULT);
        packet.setLayoutCraft(InventoryLayout.DEFAULT);
        proxy.getClient().sendPacket(packet);
    }

    /*private void testInventoryTransaction(BedrockDebuggerProxy proxy) {
        final InventoryTransactionPacket packet = new InventoryTransactionPacket();
        packet.getLegacySetItemSlots()
            .add(
                new LegacySetSlot(
                    ContainerEnumName.ANVIL_INPUT_CONTAINER, new byte[]{0}
                )
            );

        final InventorySource source = new InventorySource();
        source.setSourceType(InventorySourceType.GLOBAL_INVENTORY);
        source.setContainerID(0);
        source.setBitFlags(InventorySourceFlags.NO_FLAG);

        final InventoryAction action = new InventoryAction();
        action.setSource(source);
        action.setSlot(0);
        action.setFromItem(ItemData.AIR);
        action.setToItem(ItemData.AIR);

        final InventoryTransaction transaction = new InventoryTransaction();
        transaction.getActions().add(action);

        final NormalTransactionData normalTransactionData = new NormalTransactionData();
        normalTransactionData.getActions().add(transaction);

        packet.setTransaction(normalTransactionData);

        proxy.getClient().sendPacket(packet);
    }*/

    /*private void testItemStackRequest(BedrockDebuggerProxy proxy) {
        final ItemStackRequestPacket packet = new ItemStackRequestPacket();
        final int requestId = 5131;
        final ItemStackRequestSlotData slotData = new ItemStackRequestSlotData(
            ContainerEnumName.ANVIL_INPUT_CONTAINER, 0, 0,
            new FullContainerName(ContainerEnumName.ANVIL_INPUT_CONTAINER, null));

        final AutoCraftRecipeAction autoCraftRecipeAction = new AutoCraftRecipeAction(0, 0,
            Collections.singletonList(ItemDescriptorWithCount.EMPTY), 0);
        final BeaconPaymentAction beaconPaymentAction = new BeaconPaymentAction(0, 0);
        final ConsumeAction consumeAction = new ConsumeAction(0, slotData);
        final CraftCreativeAction craftCreativeAction = new CraftCreativeAction(0, 0);
        final CraftGrindstoneAction craftGrindstoneAction = new CraftGrindstoneAction(0, 0, 0);
        final CraftLoomAction craftLoomAction = new CraftLoomAction("bo", 0);
        final CraftRecipeAction craftRecipeAction = new CraftRecipeAction(0, 0);
        final CraftRecipeOptionalAction craftRecipeOptionalAction =
            new CraftRecipeOptionalAction(0, 0);
        final CraftResultsDeprecatedAction craftResultsDeprecatedAction =
            new CraftResultsDeprecatedAction(Collections.singleton(ItemData.AIR)
                .toArray(ItemData[]::new), 0);
        final CreateAction createAction = new CreateAction(0);
        final DestroyAction destroyAction = new DestroyAction(0, slotData);
        final DropAction dropAction = new DropAction(0, slotData, false);
        final LabTableCombineAction labTableCombineAction = new LabTableCombineAction();
        final MineBlockAction mineBlockAction = new MineBlockAction(0, 0, 0);
        final PlaceAction placeAction = new PlaceAction(0, slotData, slotData);
        final SwapAction swapAction = new SwapAction(slotData, slotData);
        final TakeAction takeAction = new TakeAction(0, slotData, slotData);

        final ItemStackRequest request = new ItemStackRequest(requestId,
            Arrays.asList(autoCraftRecipeAction, beaconPaymentAction, consumeAction,
                craftCreativeAction, craftGrindstoneAction, craftLoomAction, craftRecipeAction,
                craftRecipeOptionalAction, craftResultsDeprecatedAction, createAction, destroyAction
                , dropAction, labTableCombineAction, mineBlockAction, placeAction, swapAction,
                takeAction).toArray(new ItemStackRequestAction[0]),
            new String[0], TextProcessingEventOrigin.SERVER_CHAT_PUBLIC);

        packet.getRequests().add(request);

        proxy.getClient().sendPacket(packet);
    }*/

    private void testContainerRegistryCleanup(BedrockDebuggerProxy proxy) {
        proxy.getServer().sendPacket(new ContainerRegistryCleanupPacket());
    }

    private void testCameraAimAssist(BedrockDebuggerProxy proxy) {
        final CameraAimAssistPacket packet = new CameraAimAssistPacket();
        packet.setViewAngle(Vector2f.ZERO);
        packet.setDistance(1f);
        packet.setTargetMode(CameraAimAssistPacket.TargetMode.DISTANCE);
        packet.setAction(AimAssistAction.CLEAR);
        packet.setPresetId("");
        proxy.getServer().sendPacket(packet);
    }

    private void testMovementEffect(BedrockDebuggerProxy proxy) {
        final MovementEffectPacket packet = new MovementEffectPacket();
        packet.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
        packet.setEffectType(MovementEffectType.GLIDE_BOOST);
        packet.setEffectDuration(1);
        proxy.getServer().sendPacket(packet);
    }

    private void testClientMovementPredictionSync(BedrockDebuggerProxy proxy) {
        final ClientMovementPredictionSyncPacket packet = new ClientMovementPredictionSyncPacket();
        packet.getFlags().add(ActorFlags.ON_FIRE);
        packet.setActorBoundingBox(Vector3f.from(1f, 1f, 1f));
        packet.setMovementSpeed(0.1f);
        packet.setUnderwaterMovementSpeed(0.1f);
        packet.setLavaMovementSpeed(0.1f);
        packet.setJumpStrength(1f);
        packet.setHealth(20f);
        packet.setHunger(20f);
        packet.setActorID(proxy.getPlayer().getActorId());
        proxy.getServer().sendPacket(packet);
    }

    private void testUpdateClientOptions(BedrockDebuggerProxy proxy) {
        final UpdateClientOptionsPacket packet = new UpdateClientOptionsPacket();
        packet.setGraphicsMode(GraphicsMode.FANCY);
        proxy.getClient().sendPacket(packet);
    }

    private void testPlayerVideoCapture(BedrockDebuggerProxy proxy) {
        final PlayerVideoCapturePacket packet = new PlayerVideoCapturePacket();
        packet.setAction(PlayerVideoCapturePacket.Action.START_VIDEO_CAPTURE);
        packet.setFrameRate(30);
        packet.setFilePrefix("");
        proxy.getServer().sendPacket(packet);
    }

    private void testPlayerUpdateEntityOverrides(BedrockDebuggerProxy proxy) {
        final PlayerUpdateEntityOverridesPacket packet = new PlayerUpdateEntityOverridesPacket();
        packet.setUpdateType(PlayerUpdateEntityOverridesPacket.UpdateType.CLEAR_OVERRIDES);
        packet.setTargetID(-1);
        proxy.getServer().sendPacket(packet);
    }

    private void testPlayerLocation(BedrockDebuggerProxy proxy) {
       /* final PlayerLocationPacket packet = new PlayerLocationPacket();
        packet.setType(PlayerLocationPacket.Type.PLAYER_LOCATION_COORDINATES);
        packet.setTargetActorID(-1L);
        packet.setPosition(Vector3f.ZERO);
        proxy.getServer().sendPacket(packet);*/
    }

    private void testClientboundControlSchemeSet(BedrockDebuggerProxy proxy) {
        final ClientboundControlSchemeSetPacket packet = new ClientboundControlSchemeSetPacket();
        packet.setControlScheme(ControlScheme.LOCKED_PLAYER_RELATIVE_STRAFE);
        proxy.getServer().sendPacket(packet);
    }

    private void testPrimitiveShapes(BedrockDebuggerProxy proxy) {
        final PrimitiveShapesPacket packet = new PrimitiveShapesPacket();
        final PrimitiveShapeDataPayload shapeData = new PrimitiveShapeDataPayload();
        shapeData.setNetworkId(1);
        shapeData.setShapeType(ScriptPrimitiveShapeType.CIRCLE);
        shapeData.setLocation(proxy.getPlayer().getPosition());
        shapeData.setDimension(DimensionType.from(Dimension.OVERWORLD));
        final SphereDataPayload payload = new SphereDataPayload();
        payload.setNumSegments(0);
        shapeData.setExtraShapeData(payload);
        packet.getShapes().add(shapeData);
        proxy.getServer().sendPacket(packet);
    }
}