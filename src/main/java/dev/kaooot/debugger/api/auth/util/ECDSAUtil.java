package dev.kaooot.debugger.api.auth.util;

import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECGenParameterSpec;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;
import dev.kaooot.debugger.api.auth.AuthHttpRequest;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@UtilityClass
public class ECDSAUtil {

    private KeyPairGenerator KEY_PAIR_GENERATOR;
    private Duration CLIENT_TIME_OFFSET = null;
    private final String CLIENT_ID = "0000000048183522";
    private final URI OAUTH20_CONNECT_URI =
        URI.create("https://login.live.com/oauth20_connect.srf");

    static {
        try {
            KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance("EC");
            KEY_PAIR_GENERATOR.initialize(new ECGenParameterSpec("secp256r1"));
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    public KeyPair generateKeyPair() {
        return KEY_PAIR_GENERATOR.generateKeyPair();
    }

    public synchronized Duration getClientTimeOffset() {
        if (CLIENT_TIME_OFFSET != null) {
            return CLIENT_TIME_OFFSET;
        }

        try {
            final AuthHttpRequest request = AuthHttpRequest.builder()
                .uri(OAUTH20_CONNECT_URI)
                .method(AuthHttpRequest.RequestMethod.POST)
                .contentType(AuthHttpRequest.ContentType.FORM)
                .header("client_id", CLIENT_ID)
                .header("scope", "service::user.auth.xboxlive.com::MBI_SSL")
                .header("response_type", "device_code")
                .responseHandler(() -> {
                    throw new RuntimeException("The authentication code could not be generated");
                });
            final AuthHttpRequest.Response response = request.send0();

            final Instant clientTime = Instant.now();
            final Instant serverTime = DateTimeFormatter.RFC_1123_DATE_TIME
                .parse(response.getConnection().getHeaderField("Date"), Instant::from);

            CLIENT_TIME_OFFSET = Duration.between(clientTime, serverTime);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to calculate client time offset");
        }
        return CLIENT_TIME_OFFSET;
    }
}