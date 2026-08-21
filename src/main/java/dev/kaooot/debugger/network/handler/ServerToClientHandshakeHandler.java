package dev.kaooot.debugger.network.handler;

import com.nimbusds.jwt.SignedJWT;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.cloudburstmc.protocol.bedrock.packet.ClientToServerHandshakePacket;
import org.cloudburstmc.protocol.bedrock.packet.ServerToClientHandshakePacket;
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ServerToClientHandshakeHandler
    implements PacketHandler<ServerToClientHandshakePacket> {

    @Override
    public PacketSignal handle(ServerToClientHandshakePacket packet, BedrockDebuggerProxy proxy) {
        try {
            final SignedJWT jwt = SignedJWT.parse(packet.getHandshakeWebToken());
            final URI x5u = jwt.getHeader().getX509CertURL();
            final byte[] salt = Base64.getDecoder().decode(jwt.getJWTClaimsSet()
                .getStringClaim("salt"));
            final ECPublicKey publicKey = EncryptionUtils.parseKey(x5u.toASCIIString());
            final SecretKey secretKey = EncryptionUtils.getSecretKey(
                proxy.getKeyPair().getPrivate(),
                publicKey,
                salt
            );

            proxy.getClient().enableEncryption(secretKey);
            proxy.getClient().sendPacketImmediately(new ClientToServerHandshakePacket());
            proxy.getLogger().info("Encryption enabled");
            return PacketSignal.HANDLED;
        } catch (ParseException | NoSuchAlgorithmException | InvalidKeySpecException |
                 InvalidKeyException e) {
            throw new RuntimeException("Could not enable encryption on client: " + e.getMessage());
        }
    }
}