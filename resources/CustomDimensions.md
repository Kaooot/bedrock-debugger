# Custom Dimension Docs

For r26u2

https://learn.microsoft.com/en-us/minecraft/creator/documents/scripting/custom-dimension-api-tutorial?view=minecraft-bedrock-experimental

## Notes

* IDs for custom dimensions start at 1000
* height is limited to [-512;512]

CustomDimAddon:

* The purpose of the addon is to provide an empty custom dimension to the client's custom dimension registry.
* Create a new world with the pack and beta APIs (gtst) experiment enabled
* Optional: enable content console logging in server.properties
* Move the world and the behavior pack to the Bedrock Dedicated Server

## Important packets

The dimension types should be handled properly when implementing the server's custom dimension registry.

* AddVolumeEntityPacket
* ChangeDimensionPacket
* ClientboundAttributeLayerSyncPacket (AttributeLayerSyncPacketData types)
* ClientboundMapItemDataPacket
* DimensionDataPacket
* LegacyTelemetryEventPacket (PortalCreated and PortalUsed types)
* LevelChunkPacket
* LocatorBarPacket
* PrimitiveShapesPacket
* RemoveVolumeEntityPacket
* SetSpawnPositionPacket
* SpawnParticleEffectPacket
* StartGamePacket (LevelSettings -> SpawnSettings)
* SubChunkPacket
* SubRequestChunkPacket