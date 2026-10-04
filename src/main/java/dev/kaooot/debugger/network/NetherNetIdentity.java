package dev.kaooot.debugger.network;

import java.io.File;
import org.cloudburstmc.netty.util.nethernet.OperatorIdentity;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * <p>The proxy's NetherNet operator identity. It signs the SDP answers handed to connecting
 * clients and the identity assertions presented to a downstream NetherNet server. The key is
 * persisted so a dedicated server that pins it (trust on first use) keeps trusting this proxy
 * across restarts instead of re-prompting.
 *
 * @author Kaooot
 */
public final class NetherNetIdentity {

    private static final File IDENTITY_FILE = new File("data", "nethernet-identity.pem");
    private static final String DOMAIN = "Bedrock Debugger";

    private static volatile OperatorIdentity identity;

    private NetherNetIdentity() {
    }

    /**
     * The operator identity, generated and written to {@link #IDENTITY_FILE} on first use.
     */
    public static OperatorIdentity get() {
        OperatorIdentity current = identity;
        if (current != null) {
            return current;
        }

        synchronized (NetherNetIdentity.class) {
            if (identity == null) {
                try {
                    identity = OperatorIdentity.fromPemOrCreate(IDENTITY_FILE, DOMAIN);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load the NetherNet identity", e);
                }
            }
            return identity;
        }
    }
}
