package cl.bcs.scheduler.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.openejb.cipher.PasswordCipherFactory;
import org.apache.openejb.config.Cipher;

public class CypherEncryption {

    private static final Logger logger = LogManager.getLogger(CypherEncryption.class);
	static String cipherName = "Static3DES";
	Cipher cipher = new Cipher();
	
	public static String encrypt(String p) {
		if (p.isEmpty()) {
            logger.error("Debe especificar un texto para cifrar.");
            return p;
        }
		return new String(PasswordCipherFactory.getPasswordCipher(cipherName).encrypt(p));
	}

	public static String decrypt(String p) {
		final char[] encryptedStatic3DES = p.toCharArray();
		String s = "";
		try {
			s = PasswordCipherFactory.getPasswordCipher(cipherName).decrypt(encryptedStatic3DES);
		}catch(Exception e) {
			logger.error("String no corresponde a un ciphered string para desencriptar, retornando string..");
			s = p;
		}
		return s;
	}

}
