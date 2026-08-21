package dev.kaooot.debugger.api.auth.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import java.net.URI;
import java.security.KeyPair;
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;
import lombok.experimental.UtilityClass;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@UtilityClass
public class ForgeryUtil {

    /**
     * Forges the skin data
     *
     * @param keyPair       used to create the json web token
     * @param skinData      the login skin data
     * @param modifiedExtra the modified extra data values
     *
     * @return a fresh forged skin
     */
    public String forgeSkin(KeyPair keyPair, String skinData, Map<String, Object> modifiedExtra) {
        try {
            final Map<String, Object> map = JWSObject.parse(skinData).getPayload().toJSONObject();
            map.putAll(modifiedExtra);
            final JWSObject jwt = new JWSObject(new JWSHeader.Builder(JWSAlgorithm.ES384)
                .x509CertURL(URI.create(Base64.getEncoder()
                    .encodeToString(keyPair.getPublic().getEncoded())))
                .build(),
                new Payload(map)
            );
            jwt.sign(new ECDSASigner(keyPair.getPrivate(), Curve.P_384));
            return jwt.serialize();
        } catch (ParseException | JOSEException e) {
            throw new IllegalStateException("Failed to forge skin data", e);
        }
    }
}