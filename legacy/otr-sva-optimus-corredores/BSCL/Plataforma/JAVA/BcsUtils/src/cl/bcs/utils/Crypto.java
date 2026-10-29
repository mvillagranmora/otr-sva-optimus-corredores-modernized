package cl.bcs.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {	

    private static final String ALGORITHM = "AES";
    private static final String ENCODING = "UTF-8";
    private static final String TRANSFORMATION = "AES/CBC/NoPadding";
    private static final String PROVIDER = "SunJCE";
    
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String IV = "AAAAAAAAAAAAAAAA";
    private static final String NULLS = "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0";

    public static void main(String []args) throws Exception {
    	System.out.println(Crypto.encrypt("hR#eEp8i8%", CryptoUtils.CRYPTO_KEY));
    }
    
    public static String decrypt(String cipherText,
                                 String encryptionKey)
            throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(ENCODING), ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV.getBytes(ENCODING)));
            return unpadd(new String(cipher.doFinal(bytesFromHex(cipherText)), ENCODING));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException |
                UnsupportedEncodingException | IllegalBlockSizeException |
                BadPaddingException | NoSuchAlgorithmException |
                NoSuchProviderException | NoSuchPaddingException ex) {
            throw new Exception("Error al desencriptar", ex);
        }
    }

    public static String encrypt(String plainText,
                                 String encryptionKey)
            throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(ENCODING), ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV.getBytes(ENCODING)));
            return bytesToHex(cipher.doFinal(padd(plainText).getBytes(ENCODING)));
        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                NoSuchPaddingException | InvalidKeyException |
                InvalidAlgorithmParameterException | IllegalBlockSizeException |
                BadPaddingException | UnsupportedEncodingException ex) {
            throw new Exception("Error al encriptar", ex);
        }
    }

    private static byte[] bytesFromHex(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static String padd(String s) {
        String r = s;
        int len = r.length();
        int n = ((len + 15) / 16) * 16;
        r += NULLS.substring(0, n - len);
        return r;
    }

    private static String unpadd(String s) {
        int x = s.indexOf('\0');
        if (x < 0) {
            return s;
        } else {
            return s.substring(0, x);
        }
    }
    
}

