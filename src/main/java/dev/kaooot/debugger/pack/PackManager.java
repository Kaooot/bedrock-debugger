package dev.kaooot.debugger.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.Getter;
import org.cloudburstmc.protocol.bedrock.data.PackType;
import dev.kaooot.debugger.BedrockDebuggerProxy;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class PackManager {

    @Getter
    private final Set<ServerPack> packs = new ObjectOpenHashSet<>();
    @Getter
    private final Map<UUID, ServerPack> packIdCache = new Object2ObjectOpenHashMap<>();

    public ByteBuf getChunkFromPack(ServerPack pack, int offset, int length) {
        if (pack.getSize() - offset > length) {
            pack.setChunk(new byte[length]);
        } else {
            pack.setChunk(new byte[(int) (pack.getSize() - offset)]);
        }
        try (final FileInputStream inputStream = new FileInputStream(pack.getFile())) {
            inputStream.skip(offset);
            inputStream.read(pack.getChunk());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Unpooled.wrappedBuffer(pack.getChunk());
    }

    public void loadPacks(BedrockDebuggerProxy proxy) {
        final File packPath = new File(
            System.getProperty("user.dir") + "/resource_packs"
        );

        if (!packPath.exists()) {
            packPath.mkdirs();
        }

        if (!packPath.isDirectory()) {
            return;
        }

        for (final File file : Objects.requireNonNull(packPath.listFiles())) {
            if (!file.isDirectory()) {
                final String fileEnding = file.getName()
                    .substring(file.getName().lastIndexOf(".") + 1);

                boolean scripting = false;

                if (fileEnding.equalsIgnoreCase("zip") || fileEnding.equalsIgnoreCase("mcpack")) {
                    try (final ZipFile zipFile = new ZipFile(file)) {
                        final String manifestFileName = "manifest.json";
                        ZipEntry manifestEntry = zipFile.getEntry(manifestFileName);

                        if (zipFile.getEntry("scripts") != null) {
                            scripting = true;
                        }

                        // due to the mcpack file extension
                        if (manifestEntry == null) {
                            manifestEntry = zipFile.stream()
                                .filter(zipEntry -> !zipEntry.isDirectory() &&
                                    zipEntry.getName().toLowerCase().endsWith(manifestFileName))
                                .filter(zipEntry -> {
                                    final File zipEntryFile = new File(zipEntry.getName());

                                    if (!zipEntryFile.getName()
                                        .equalsIgnoreCase(manifestFileName)) {
                                        return false;
                                    }

                                    return zipEntryFile.getParent() == null ||
                                        zipEntryFile.getParentFile().getParent() == null;
                                }).findFirst().orElseThrow(() -> new IllegalArgumentException(
                                    "The " + manifestFileName + " file could not be found"));
                        }

                        final JsonObject manifest = new JsonParser()
                            .parse(new InputStreamReader(zipFile.getInputStream(manifestEntry),
                                StandardCharsets.UTF_8)).getAsJsonObject();

                        if (!this.isManifestValid(manifest)) {
                            throw new IllegalArgumentException(
                                "The " + manifestFileName + " file is invalid");
                        }

                        final JsonObject manifestHeader = manifest.getAsJsonObject("header");
                        final String packName = manifestHeader.get("name").getAsString();
                        final String packId = manifestHeader.get("uuid").getAsString();
                        final JsonArray packVersionArray =
                            manifestHeader.get("version").getAsJsonArray();
                        final String packVersion = packVersionArray.toString()
                            .replace("[", "").replace("]", "")
                            .replaceAll(",", ".");
                        final int packSize = Math.toIntExact(file.length());
                        final byte[] packHash = MessageDigest.getInstance("SHA-256")
                            .digest(java.nio.file.Files.readAllBytes(file.toPath()));

                        proxy.getLogger().info("The pack {} has been loaded from {}/{}", packName,
                            file.getParent(), file.getName());

                        final ServerPack pack = new ServerPack(file, packName,
                            UUID.fromString(packId), packVersion, packSize, packHash,
                            PackType.RESOURCES, scripting);

                        this.packs.add(pack);
                        this.packIdCache.putIfAbsent(pack.getId(), pack);
                    } catch (final IOException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean isManifestValid(final JsonObject manifest) {
        if (manifest.has("format_version") && manifest.has("header") &&
            manifest.has("modules")) {
            final JsonObject manifestHeader = manifest.get("header").getAsJsonObject();

            if (manifestHeader.has("name") &&
                manifestHeader.has("uuid") && manifestHeader.has("version")) {
                final JsonArray headerVersion = manifestHeader.getAsJsonArray("version");
                return headerVersion.size() == 3;
            }
        }
        return false;
    }
}