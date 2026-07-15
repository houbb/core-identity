package com.github.houbb.core.identity.infrastructure.webauthn;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

/**
 * Verifies WebAuthn signatures using ECDSA (SHA-256) or RS256 (SHA-256 with RSA).
 * Self-implemented using java.security.Signature.
 */
public final class WebAuthnSignatureVerifier {

    private WebAuthnSignatureVerifier() {}

    /**
     * Verify a WebAuthn assertion signature.
     *
     * @param publicKey     the parsed public key from the credential
     * @param algorithm     COSE algorithm identifier (-7 = ES256, -257 = RS256, -37 = PS256)
     * @param signedData    the authenticatorData || clientDataHash
     * @param signature     the raw signature bytes
     * @return true if signature is valid
     */
    public static boolean verify(PublicKey publicKey, int algorithm, byte[] signedData, byte[] signature) {
        try {
            String jcaAlgorithm = coseAlgorithmToJca(algorithm);
            Signature sig = Signature.getInstance(jcaAlgorithm);

            // PSS parameters for RSASSA-PSS (PS256)
            if (algorithm == -37) {
                PSSParameterSpec pssSpec = new PSSParameterSpec("SHA-256", "MGF1",
                        MGF1ParameterSpec.SHA256, 32, 1);
                sig.setParameter(pssSpec);
            }

            sig.initVerify(publicKey);
            sig.update(signedData);
            return sig.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException("Signature verification failed", e);
        }
    }

    /**
     * Map COSE algorithm identifier to JCA algorithm name.
     *
     * -7  = ES256 = SHA256withECDSA
     * -257 = RS256 = SHA256withRSA
     * -37 = PS256 = RSASSA-PSS (SHA-256)
     * -8  = ES384 = SHA384withECDSA
     * -258 = RS384 = SHA384withRSA
     * -259 = RS512 = SHA512withRSA
     */
    private static String coseAlgorithmToJca(int algorithm) {
        return switch (algorithm) {
            case -7  -> "SHA256withECDSA";
            case -257 -> "SHA256withRSA";
            case -37  -> "RSASSA-PSS";
            case -8   -> "SHA384withECDSA";
            case -258 -> "SHA384withRSA";
            case -259 -> "SHA512withRSA";
            default -> throw new IllegalArgumentException("Unsupported COSE algorithm: " + algorithm);
        };
    }
}
