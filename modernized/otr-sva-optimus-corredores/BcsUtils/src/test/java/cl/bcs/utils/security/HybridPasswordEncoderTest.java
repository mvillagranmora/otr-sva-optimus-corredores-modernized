package cl.bcs.utils.security;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

// RULE-030 FIX: dual-hash migration encoder — no DB changes, canary safe
class HybridPasswordEncoderTest {

    private HybridPasswordEncoder encoder;

    @BeforeEach
    void setup() {
        encoder = new HybridPasswordEncoder();
        ReflectionTestUtils.setField(encoder, "rehashEnabled", false);
    }

    // ── encode always produces BCrypt ─────────────────────────────────────

    @Test
    @DisplayName("encode produces BCrypt hash (new passwords always BCrypt)")
    void encodeProducesBcrypt() {
        String hash = encoder.encode("MiPassword1!");
        assertThat(HybridPasswordEncoder.isBcrypt(hash)).isTrue();
    }

    // ── matches: BCrypt path ──────────────────────────────────────────────

    @Test
    @DisplayName("matches BCrypt hash — correct password")
    void matchesBcryptCorrect() {
        String hash = new BCryptPasswordEncoder(12).encode("Secreta123!");
        assertThat(encoder.matches("Secreta123!", hash)).isTrue();
    }

    @Test
    @DisplayName("matches BCrypt hash — wrong password returns false")
    void matchesBcryptWrong() {
        String hash = new BCryptPasswordEncoder(12).encode("Secreta123!");
        assertThat(encoder.matches("OtraPassword!", hash)).isFalse();
    }

    // ── matches: SHA-256 legacy path ──────────────────────────────────────

    @Test
    @DisplayName("matches legacy SHA-256 (unsalted) — correct password")
    void matchesSha256Correct() {
        String sha256 = DigestUtils.sha256Hex("PasswordLegacy1");
        assertThat(encoder.matches("PasswordLegacy1", sha256)).isTrue();
    }

    @Test
    @DisplayName("matches legacy SHA-256 — wrong password returns false")
    void matchesSha256Wrong() {
        String sha256 = DigestUtils.sha256Hex("PasswordLegacy1");
        assertThat(encoder.matches("Diferente", sha256)).isFalse();
    }

    @Test
    @DisplayName("matches SHA-256 uppercase hex — case-insensitive comparison")
    void matchesSha256UppercaseHex() {
        String sha256upper = DigestUtils.sha256Hex("Test123").toUpperCase();
        assertThat(encoder.matches("Test123", sha256upper)).isTrue();
    }

    @Test
    @DisplayName("matches null stored hash — returns false without exception")
    void matchesNullStored() {
        assertThat(encoder.matches("cualquier", null)).isFalse();
    }

    // ── upgradeEncoding: canary phase (rehash-enabled: false) ────────────

    @Test
    @DisplayName("upgradeEncoding = false during canary even for SHA-256 hash")
    void noUpgradeWhenRehashDisabled() {
        String sha256 = DigestUtils.sha256Hex("pass");
        assertThat(encoder.upgradeEncoding(sha256)).isFalse();
    }

    @Test
    @DisplayName("upgradeEncoding = false during canary for BCrypt hash")
    void noUpgradeForBcryptWhenDisabled() {
        String bcrypt = encoder.encode("pass");
        assertThat(encoder.upgradeEncoding(bcrypt)).isFalse();
    }

    // ── upgradeEncoding: post-cutover (rehash-enabled: true) ─────────────

    @Test
    @DisplayName("upgradeEncoding = true post-cutover for SHA-256 — triggers re-hash")
    void upgradesSha256PostCutover() {
        ReflectionTestUtils.setField(encoder, "rehashEnabled", true);
        String sha256 = DigestUtils.sha256Hex("pass");
        assertThat(encoder.upgradeEncoding(sha256)).isTrue();
    }

    @Test
    @DisplayName("upgradeEncoding = false post-cutover for BCrypt — already migrated")
    void doesNotUpgradeBcryptPostCutover() {
        ReflectionTestUtils.setField(encoder, "rehashEnabled", true);
        String bcrypt = encoder.encode("pass");
        assertThat(encoder.upgradeEncoding(bcrypt)).isFalse();
    }

    // ── isBcrypt helper ───────────────────────────────────────────────────

    @Test
    @DisplayName("isBcrypt detects $2a$, $2b$, $2y$ prefixes")
    void isBcryptDetectsAllPrefixes() {
        assertThat(HybridPasswordEncoder.isBcrypt("$2a$12$abc")).isTrue();
        assertThat(HybridPasswordEncoder.isBcrypt("$2b$12$abc")).isTrue();
        assertThat(HybridPasswordEncoder.isBcrypt("$2y$12$abc")).isTrue();
        assertThat(HybridPasswordEncoder.isBcrypt("a4f3e2b1" + "0".repeat(56))).isFalse();
        assertThat(HybridPasswordEncoder.isBcrypt(null)).isFalse();
    }

    // ── canary: BD no cambia (clave: upgradeEncoding false → Spring Security no escribe)

    @Test
    @DisplayName("CANARY: SHA-256 verifica OK y no pide re-hash (BD compartida con TomEE)")
    void canarySharedDbScenario() {
        String storedInLegacyDb = DigestUtils.sha256Hex("PasswordUsuario1!");
        // Verificación funciona
        assertThat(encoder.matches("PasswordUsuario1!", storedInLegacyDb)).isTrue();
        // Spring Security NO va a re-hashear → TomEE no se rompe
        assertThat(encoder.upgradeEncoding(storedInLegacyDb)).isFalse();
    }

    @Test
    @DisplayName("POST-CUTOVER: SHA-256 verifica OK y pide re-hash (TomEE apagado)")
    void postCutoverRehashScenario() {
        ReflectionTestUtils.setField(encoder, "rehashEnabled", true);
        String storedInDb = DigestUtils.sha256Hex("PasswordUsuario1!");
        assertThat(encoder.matches("PasswordUsuario1!", storedInDb)).isTrue();
        // Spring Security re-hashea a BCrypt en el próximo UPDATE
        assertThat(encoder.upgradeEncoding(storedInDb)).isTrue();
    }
}
