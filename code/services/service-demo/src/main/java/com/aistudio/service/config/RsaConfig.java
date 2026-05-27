package com.aistudio.service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class RsaConfig {

    @Value("${rsa.private-key}")
    private String privateKeyBase64;

    @Value("${rsa.public-key}")
    private String publicKeyBase64;

    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    public String decrypt(String cipherTextBase64) {
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PrivateKey privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherTextBase64));
            return new String(decryptedBytes);
        } catch (Exception exception) {
            log.error("RSA decrypt failed", exception);
            throw new IllegalStateException("密码解密失败");
        }
    }
}
