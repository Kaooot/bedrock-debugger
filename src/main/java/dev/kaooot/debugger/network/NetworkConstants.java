package dev.kaooot.debugger.network;

import lombok.experimental.UtilityClass;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.v2207.Bedrock_v2207;
import org.cloudburstmc.protocol.bedrock.data.EncodingSettings;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@UtilityClass
public class NetworkConstants {

    public final BedrockCodec CODEC = Bedrock_v2207.CODEC;
    public final EncodingSettings ENCODING_SETTINGS = EncodingSettings.UNLIMITED;
}