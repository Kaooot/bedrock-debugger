package dev.kaooot.debugger.api.auth.request;

import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import dev.kaooot.debugger.api.auth.util.ECDSAUtil;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public abstract class TokenAuthRequest<T> extends AuthRequest<T> {

    protected final URI xboxLiveSisuUrl = URI.create("https://sisu.xboxlive.com/authorize");
    protected final URI xboxLiveDeviceAuthUrl = URI.create(
        "https://device.auth.xboxlive.com/device/authenticate"
    );

    protected JsonObject buildProofKey(ECPublicKey publicKey) {
        final int fieldSize = publicKey.getParams().getCurve().getField().getFieldSize();
        final JsonObject proofKey = new JsonObject();
        proofKey.addProperty("alg", "ES256");
        proofKey.addProperty("crv", "P-256");
        proofKey.addProperty("kty", "EC");
        proofKey.addProperty("use", "sig");
        proofKey.addProperty("x",
            this.encodeECCoordinate(fieldSize, publicKey.getW().getAffineX())
        );
        proofKey.addProperty("y",
            this.encodeECCoordinate(fieldSize, publicKey.getW().getAffineY())
        );
        return proofKey;
    }

    protected String sign(String data, URL url, ECPrivateKey privateKey) {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             final DataOutputStream outputStream = new DataOutputStream(baos)) {
            final long timestamp = ((Instant.now().plus(ECDSAUtil.getClientTimeOffset())
                .getEpochSecond()) + 11644473600L) * 10000000L;

            outputStream.writeInt(1); // Policy Version
            outputStream.writeByte(0);
            outputStream.writeLong(timestamp);
            outputStream.writeByte(0);
            outputStream.write("POST".getBytes(StandardCharsets.UTF_8));
            outputStream.writeByte(0);
            outputStream.write((url.getPath() + (url.getQuery() != null ? url.getQuery() : ""))
                .getBytes(StandardCharsets.UTF_8));
            outputStream.writeByte(0);
            outputStream.writeByte(0);
            outputStream.write(data.getBytes(StandardCharsets.UTF_8));
            outputStream.writeByte(0);

            try (final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                 final DataOutputStream headerStream = new DataOutputStream(baos1)) {
                headerStream.writeInt(1); // Policy Version
                headerStream.writeLong(timestamp);

                try {
                    final Signature signature =
                        Signature.getInstance("SHA256withECDSAinP1363Format");
                    signature.initSign(privateKey);
                    signature.update(baos.toByteArray());

                    headerStream.write(signature.sign());
                    return Base64.getEncoder().encodeToString(baos1.toByteArray());
                } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to sign request");
    }

    private String encodeECCoordinate(int fieldSize, BigInteger coordinate) {
        final byte[] data = this.bigIntToByteArray(coordinate);
        final int dataLength = (fieldSize + 7) / 8;
        if (data.length >= dataLength) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
        }
        final byte[] paddedData = new byte[dataLength];
        System.arraycopy(data, 0, paddedData, dataLength - data.length, data.length);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(paddedData);
    }

    private byte[] bigIntToByteArray(BigInteger bigInteger) {
        int length = bigInteger.bitLength();
        length = length + 7 >> 3 << 3;
        final byte[] data = bigInteger.toByteArray();
        if (bigInteger.bitLength() % 8 != 0 && bigInteger.bitLength() / 8 + 1 == length / 8) {
            return data;
        }
        int src = 0;
        int len = data.length;
        if (bigInteger.bitLength() % 8 == 0) {
            src = 1;
            --len;
        }
        final int dest = length / 8 - len;
        final byte[] resizedData = new byte[length / 8];
        System.arraycopy(data, src, resizedData, dest, len);
        return resizedData;
    }
}