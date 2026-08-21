package dev.kaooot.debugger.util;

import lombok.Value;
import org.cloudburstmc.protocol.common.util.Preconditions;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class BedrockGameVersion {

    int major;
    int minor;
    int patch;
    int revision;

    public static BedrockGameVersion from(String version) {
        Preconditions.checkArgument(version.contains("."));
        final String[] array = version.split("\\.");
        final int length = array.length;
        Preconditions.checkArgument(length >= 3);
        final int major = Integer.parseInt(array[0]);
        final int minor = Integer.parseInt(array[1]);
        final int patch = Integer.parseInt(
            array[2].contains("-") ? array[2].split("-")[0] : array[2]
        );
        int revision = -1;
        if (length > 3) {
            if (array[3].contains("-")) {
                revision = Integer.parseInt(array[3].split("beta.")[1]);
            } else {
                revision = Integer.parseInt(array[3]);
            }
        }
        return new BedrockGameVersion(major, minor, patch, revision);
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor + "." + this.patch +
            (this.revision == -1 ? "" : ("." + this.revision));
    }
}