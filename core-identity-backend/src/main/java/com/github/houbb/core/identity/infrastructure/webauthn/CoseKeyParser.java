package com.github.houbb.core.identity.infrastructure.webauthn;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.math.BigInteger;

/**
 * Parses COSE_Key formatted public keys (RFC 8152).
 * Supports EC2 P-256 (alg -7: ES256) and RSA (alg -257: RS256).
 */
public final class CoseKeyParser {

    private CoseKeyParser() {}

    // COSE key type constants
    private static final int KTY_OKP = 1;
    private static final int KTY_EC2 = 2;
    private static final int KTY_RSA = 3;

    // COSE key parameter labels
    private static final int LABEL_KTY = 1;
    private static final int LABEL_ALG = 3;
    private static final int LABEL_CRV = -1;
    private static final int LABEL_X = -2;
    private static final int LABEL_Y = -3;
    private static final int LABEL_N = -1;
    private static final int LABEL_E = -2;

    // EC2 curves
    private static final int CRV_P256 = 1;

    /**
     * Parse a COSE_Key encoded as a JSON string (base64url-encoded key parameters).
     * Expected format: {"1": 2, "3": -7, "-1": 1, "-2": "base64url_x", "-3": "base64url_y"}
     */
    public static PublicKey parseCoseKey(String coseKeyJson) {
        try {
            var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var map = objectMapper.readValue(coseKeyJson, java.util.Map.class);

            int kty = ((Number) map.get(String.valueOf(LABEL_KTY))).intValue();

            if (kty == KTY_EC2) {
                return parseEC2Key(map);
            } else if (kty == KTY_RSA) {
                return parseRsaKey(map);
            }
            throw new IllegalArgumentException("Unsupported COSE key type: " + kty);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse COSE key", e);
        }
    }

    private static PublicKey parseEC2Key(java.util.Map<String, Object> map) throws Exception {
        int crv = ((Number) map.get(String.valueOf(LABEL_CRV))).intValue();
        if (crv != CRV_P256) {
            throw new IllegalArgumentException("Unsupported EC curve: " + crv);
        }

        byte[] x = java.util.Base64.getUrlDecoder().decode((String) map.get(String.valueOf(LABEL_X)));
        byte[] y = java.util.Base64.getUrlDecoder().decode((String) map.get(String.valueOf(LABEL_Y)));

        BigInteger xBigInt = new BigInteger(1, x);
        BigInteger yBigInt = new BigInteger(1, y);

        ECPoint point = new ECPoint(xBigInt, yBigInt);
        var ecSpec = new java.security.spec.ECParameterSpec(
                new java.security.spec.EllipticCurve(
                        new java.security.spec.ECFieldFp(
                                new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951")),
                        new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853948"),
                        new BigInteger("41058363725152142129326129780047268409114441015993725554835256314039467401291")),
                new ECPoint(
                        new BigInteger("48439561293906451759052585252797914202762949526041747995844080717082404635286"),
                        new BigInteger("36134250956749795798585127919587881956611106672985015071877198253568414405109")),
                new BigInteger("115792089210356248762697446949407573529996955224135760342422259061068512044369"),
                1);

        ECPublicKeySpec keySpec = new ECPublicKeySpec(point, ecSpec);
        return KeyFactory.getInstance("EC").generatePublic(keySpec);
    }

    private static PublicKey parseRsaKey(java.util.Map<String, Object> map) throws Exception {
        byte[] n = java.util.Base64.getUrlDecoder().decode((String) map.get(String.valueOf(LABEL_N)));
        byte[] e = java.util.Base64.getUrlDecoder().decode((String) map.get(String.valueOf(LABEL_E)));

        BigInteger nBigInt = new BigInteger(1, n);
        BigInteger eBigInt = new BigInteger(1, e);

        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(nBigInt, eBigInt);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }
}
