package dk.syslab.controller.validation;

import dk.syslab.controller.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class JwtService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    PublicKey publicKey;

    // Simplified version of the JWT service found in the coordinator, this only has public key to validate tokens
    public JwtService(Configuration configuration) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pubBase64 = configuration.getRequiredProperty("jwt.public.key");
        KeyFactory kf = KeyFactory.getInstance("RSA");
        byte[] bytePubKey = Base64.getDecoder().decode(pubBase64);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(bytePubKey);
        publicKey = kf.generatePublic(x509EncodedKeySpec);
    }

    public Key getValidationKey() {
        return publicKey;
    }
}
