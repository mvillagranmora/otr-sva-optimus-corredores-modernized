package cl.bcs.utils.security;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Dual-algorithm password encoder for zero-downtime canary migration.
 *
 * RULE-030 FIX: legacy system used unsalted SHA-256 inconsistently between
 * login and change-password paths. This encoder unifies both paths and
 * enables progressive migration to BCrypt without DB schema changes.
 *
 * Canary strategy (rehash-enabled: false):
 *   - Accepts SHA-256 (legacy) AND BCrypt (new) — BD shared with TomEE
 *   - Never writes to BD on login → TomEE continues reading SHA-256 unchanged
 *
 * Post-cutover (rehash-enabled: true):
 *   - Spring Security calls upgradeEncoding() → true for SHA-256 hashes
 *   - Re-hashes to BCrypt on each successful login (single-row UPDATE)
 *   - Migration completes naturally as users log in
 *
 * Hash detection (no BD prefix required):
 *   - BCrypt:  starts with "$2a$" or "$2b$" or "$2y$"
 *   - SHA-256: 64-char lowercase hex
 */
@Component
public class HybridPasswordEncoder implements PasswordEncoder {

    private static final int BCRYPT_COST = 12;

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(BCRYPT_COST);

    @Value("${security.password.rehash-enabled:false}")
    private boolean rehashEnabled;

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        if (isBcrypt(encodedPassword)) {
            return bcrypt.matches(rawPassword, encodedPassword);
        }
        // Legacy: unsalted SHA-256
        String sha256 = DigestUtils.sha256Hex(rawPassword.toString());
        return sha256.equalsIgnoreCase(encodedPassword);
    }

    /**
     * Returns true only when canary is complete (TomEE off) AND the stored
     * hash is still SHA-256 — Spring Security re-hashes on the next successful login.
     */
    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return rehashEnabled && !isBcrypt(encodedPassword);
    }

    static boolean isBcrypt(String hash) {
        return hash != null && (hash.startsWith("$2a$")
                             || hash.startsWith("$2b$")
                             || hash.startsWith("$2y$"));
    }
}
