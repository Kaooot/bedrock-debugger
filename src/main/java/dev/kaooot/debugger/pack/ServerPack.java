package dev.kaooot.debugger.pack;

import java.io.File;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.data.PackType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Getter
@RequiredArgsConstructor
public class ServerPack {

    private final File file;
    private final String name;
    private final UUID id;
    private final String version;
    private final long size;
    private final byte[] hash;
    private final PackType type;
    private final boolean scripting;

    @Setter
    private byte[] chunk = new byte[0];
}