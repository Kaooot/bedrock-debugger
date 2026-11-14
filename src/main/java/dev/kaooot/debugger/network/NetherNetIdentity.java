package dev.kaooot.debugger.network;

import com.nimbusds.jwt.SignedJWT;
import java.io.File;
import java.security.KeyPair;
import org.cloudburstmc.netty.util.nethernet.OperatorIdentity;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public final class NetherNetIdentity {

    private static final File IDENTITY_FILE = new File("data", "nethernet-identity.pem");
    private static final String DOMAIN = "Bedrock Debugger";

    private static volatile OperatorIdentity identity;

    private NetherNetIdentity() {
    }

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

    public static OperatorIdentity forToken(KeyPair keyPair, String signedToken) {
        try {
            final String issuer = SignedJWT.parse(signedToken).getJWTClaimsSet().getIssuer();
            return OperatorIdentity.fromToken(
                keyPair, signedToken, issuer != null ? issuer : DOMAIN
            );
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to build the NetherNet identity from the multiplayer token", e
            );
        }
    }
}