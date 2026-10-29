package cl.bcs.plataforma.pla.utils;

import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenEJBEncryption {
    private static final Logger logger = LogManager.getLogger(OpenEJBEncryption.class);
    static String algorithm = "DESede"; // 3DES
    static String defaultKeyText = "default-optimus-key";

    public static Key getKeyFromText(String keyText) {
        byte[] keyBytes = new byte[24];
        byte[] textBytes = keyText.getBytes();
        System.arraycopy(textBytes, 0, keyBytes, 0, Math.min(textBytes.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, algorithm);
    }

    public static String encrypt(String plaintext, String key) {
        if (plaintext.isEmpty()) {
            logger.error("Debe especificar un texto para cifrar.");
            return plaintext;
        }
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, getKeyFromText(key));
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes); // Codificamos en Base64
        } catch (Exception e) {
            logger.error("Error al cifrar: ", e);
            return null;
        }
    }

    public static String decrypt(String encryptedText, String key) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, getKeyFromText(key));
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText); // Decodificamos desde Base64
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            logger.error("Error al descifrar: ", e);
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            String keyText = "test";
            String plaintext = "testpassword";
            String encryptedText = encrypt(plaintext, keyText);
            String decryptedText = decrypt(encryptedText, keyText);

            System.out.println("Texto original: " + plaintext);
            System.out.println("Texto encriptado: " + encryptedText);
            System.out.println("Texto desencriptado: " + decryptedText);
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }
}
