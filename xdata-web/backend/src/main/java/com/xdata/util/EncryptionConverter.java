package com.xdata.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
@Component
@Slf4j
public class EncryptionConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String DEFAULT_SECRET = "xdata-default-secret-key-123";
    
    private static String secret;

    @Value("${xdata.encryption.key:xdata-default-secret-key-123}")
    public void setSecret(String secret) {
        EncryptionConverter.secret = secret;
        log.info("Encryption secret has been initialized.");
    }

    private String getSecret() {
        return secret != null ? secret : DEFAULT_SECRET;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            SecretKeySpec key = new SecretKeySpec(getFixedKey(getSecret()), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e) {
            log.error("Error encrypting attribute: {}", e.getMessage());
            return attribute;
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            SecretKeySpec key = new SecretKeySpec(getFixedKey(getSecret()), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(decrypted);
        } catch (Exception e) {
            // If decryption fails, it might be plain text or encrypted with a different key
            // log.trace("Decryption failed for value, assuming plain text");
            return dbData;
        }
    }

    private byte[] getFixedKey(String s) {
        byte[] key = new byte[16];
        byte[] bytes = s.getBytes();
        System.arraycopy(bytes, 0, key, 0, Math.min(bytes.length, 16));
        return key;
    }
}
