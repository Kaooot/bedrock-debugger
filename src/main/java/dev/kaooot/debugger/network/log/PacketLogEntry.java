package dev.kaooot.debugger.network.log;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.data.PacketRecipient;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Getter
@RequiredArgsConstructor
public final class PacketLogEntry {

    private final long sequence;
    private final long timestampMillis;
    private final PacketRecipient direction;
    private final PacketRecipient definitionRecipient;
    private final int packetId;
    private final String name;
    private final byte[] payload;
    private final String packetString;

    private String hexDump;
    private String binaryDump;

    public int size() {
        return this.payload.length;
    }

    public boolean isServerbound() {
        return this.direction.equals(PacketRecipient.SERVER);
    }

    public String getHexDump() {
        if (this.hexDump == null) {
            this.hexDump = this.payload.length == 0 ? "[NO DATA]" :
                ByteBufUtil.prettyHexDump(Unpooled.wrappedBuffer(this.payload));
        }
        return this.hexDump;
    }

    public String getBinaryDump() {
        if (this.binaryDump == null) {
            this.binaryDump = this.payload.length == 0 ? "[NO DATA]" : this.buildBinaryDump();
        }
        return this.binaryDump;
    }

    private String buildBinaryDump() {
        final StringBuilder builder = new StringBuilder();
        for (int offset = 0; offset < this.payload.length; offset += 8) {
            builder.append(String.format("%08X  ", offset));
            final StringBuilder ascii = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                final int index = offset + i;
                if (index < this.payload.length) {
                    final int value = this.payload[index] & 0xFF;
                    builder.append(
                        String.format("%8s ", Integer.toBinaryString(value)).replace(' ', '0')
                    );
                    ascii.append(value >= 0x20 && value < 0x7F ? (char) value : '.');
                } else {
                    builder.append("         ");
                    ascii.append(' ');
                }
            }
            builder.append(" |").append(ascii).append('|');
            if (offset + 8 < this.payload.length) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }
}