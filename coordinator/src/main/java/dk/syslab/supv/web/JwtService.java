package dk.syslab.supv.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class JwtService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    PrivateKey privateKey;
    PublicKey publicKey;

    // HOW TO GENERATE KEYS using OpenSSL
    // 1. generate key pair
    // openssl genrsa -out jwt.key 2048
    // 2. generate public key
    // openssl rsa -in jwt.key -pubout -out jwt.pub
    // 3. convert private key to PKCS8
    // openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in jwt.key -out jwt.pkcs8
    // 4. Java can read the PKCS8 private key with PKCS8EncodedKeySpec and the X509 public key with X509EncodedKeySpec
    // So you will need both, then use KeyFactory.getInstance("RSA") and generatePrivate / generatePublic (remember to decode base64)

    // basically the private key should be on the authentication server / access control server
    // and the public key can be distributed to all sorts of applications
    // further, the server should publish the public key on a public URL, so the private key can be cycled as needed, see JWK, JWKS

    public JwtService(Environment env) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String prvBase64 = env.getRequiredProperty("jwt.private.key");
        String pubBase64 = env.getRequiredProperty("jwt.public.key");

        KeyFactory kf = KeyFactory.getInstance("RSA");
        byte[] bytePrvKey = Base64.getDecoder().decode(prvBase64);
        byte[] bytePubKey = Base64.getDecoder().decode(pubBase64);

        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(bytePrvKey);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(bytePubKey);

        privateKey = kf.generatePrivate(pkcs8EncodedKeySpec);
        publicKey = kf.generatePublic(x509EncodedKeySpec);
    }


    public Key getSigningKey() {
        return privateKey;
    }

    public Key getValidationKey() {
        return publicKey;
    }
}
