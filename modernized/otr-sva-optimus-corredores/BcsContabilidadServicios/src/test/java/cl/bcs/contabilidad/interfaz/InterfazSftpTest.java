package cl.bcs.contabilidad.interfaz;

import cl.bcs.contabilidad.domain.port.out.InterfazFileTransfer;
import cl.bcs.contabilidad.infrastructure.sftp.KnownHostsVerifier;
import cl.bcs.contabilidad.infrastructure.sftp.SftpHostKeyException;
import cl.bcs.contabilidad.infrastructure.sftp.SftpSessionFactory;
import cl.bcs.contabilidad.infrastructure.sftp.SftpSettings;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

/**
 * SFTP transfer contract and credential handling for the interfaz diaria.
 * Oracle: CreaInterfazDiaria.java:198-226 and SFTPConnector.java.
 *
 * <p>SECRET HANDLING: every value below is a clearly fake substitute (sftp.example.invalid, interfaz-test,
 * fake-password-0000). No host, user or password from the legacy sources or properties is reproduced.
 */
class InterfazSftpTest {

    /** Fake, same-shape credentials. Never real ones. */
    static Map<String, String> envFalso() {
        Map<String, String> env = new HashMap<>();
        env.put("SFTP_HOST", "sftp.example.invalid");
        env.put("SFTP_PORT", "2222");
        env.put("SFTP_USER", "interfaz-test");
        env.put("SFTP_PASSWORD", "fake-password-0000");
        // Approved deviation (host-key verification): known_hosts is required; fromEnvironment does not open it
        env.put("SFTP_KNOWN_HOSTS", "/etc/interfaz-test/known_hosts");
        return env;
    }

    @Test
    @DisplayName("CONT-A-32: remote copy path = /home/interfaz-bpc/ + last segment of the local base path")
    void cont_a_32_remoteCopyPathIsBaseNameUnderRemoteDir() {
        // SFTPConnector.java:212-216 sftpDirectory + new File(fileDirectory).getName(); :171 same name on upload
        assertThat(InterfazFileTransfer.remoteCopyPath("/home/interfaz-bpc/", Path.of("/data/archivos/")))
                .isEqualTo("/home/interfaz-bpc/archivos");
        assertThat(InterfazFileTransfer.remoteCopyPath("/home/interfaz-bpc/", Path.of("/opt/optimus/files")))
                .isEqualTo("/home/interfaz-bpc/files");
    }

    @Test
    @DisplayName("CONT-A-33: credentials come from the environment (SFTP_HOST/PORT/USER/PASSWORD)")
    void cont_a_33_settingsReadFromEnvironment() {
        // CreaInterfazDiaria.java:202 properties USERNAME/PASSWORD/HOST/PORT (external file) -> env vars
        SftpSettings s = SftpSettings.fromEnvironment(envFalso());
        assertThat(s.host()).isEqualTo("sftp.example.invalid");
        assertThat(s.port()).isEqualTo(2222);
        assertThat(s.username()).isEqualTo("interfaz-test");
        assertThat(s.password()).isEqualTo("fake-password-0000");
        assertThat(s.knownHosts()).isEqualTo(Path.of("/etc/interfaz-test/known_hosts"));
    }

    @ParameterizedTest(name = "missing {0}")
    @ValueSource(strings = {"SFTP_HOST", "SFTP_PORT", "SFTP_USER", "SFTP_PASSWORD", "SFTP_KNOWN_HOSTS"})
    @DisplayName("CONT-A-33: no built-in defaults: any missing variable (including PORT) is an error naming it")
    void cont_a_33_missingVariableFailsWithoutDefault(String clave) {
        // CreaInterfazDiaria.java:202 Integer.parseInt(properties.getProperty("PORT")) has no default -> fails
        Map<String, String> env = envFalso();
        env.remove(clave);
        assertThatThrownBy(() -> SftpSettings.fromEnvironment(env))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(clave)
                .message().doesNotContain("fake-password-0000");
    }

    @Test
    @DisplayName("CONT-A-33: a non-numeric port is rejected")
    void cont_a_33_nonNumericPortRejected() {
        // CreaInterfazDiaria.java:202 Integer.parseInt -> NumberFormatException
        Map<String, String> env = envFalso();
        env.put("SFTP_PORT", "veintidos");
        assertThatThrownBy(() -> SftpSettings.fromEnvironment(env)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("CONT-A-34: SftpSettings.toString never reveals the password")
    void cont_a_34_toStringMasksPassword() {
        assertThat(SftpSettings.fromEnvironment(envFalso()).toString())
                .doesNotContain("fake-password-0000")
                .contains("sftp.example.invalid");
    }

    private static final List<Path> FUENTES_SFTP = List.of(
            Path.of("src/main/java/cl/bcs/contabilidad/infrastructure/sftp"),
            Path.of("src/main/java/cl/bcs/contabilidad/infrastructure/interfaz"),
            Path.of("src/main/java/cl/bcs/contabilidad/infrastructure/batch/CreaInterfazDiariaJob.java"));

    private static final Pattern IPV4 = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final Pattern PASSWORD_LITERAL =
            Pattern.compile("(?i)(setPassword\\s*\\(\\s*\"|password\\s*=\\s*\"[^\"]+\")");

    @Test
    @DisplayName("CONT-A-34: production SFTP/interfaz sources contain no IP literal and no inline password")
    void cont_a_34_noHardcodedCredentialsInProductionSources() throws IOException {
        // CreaInterfazDiaria.java:42-47 had a commented host/user/password block; it must not be carried over
        for (Path p : FUENTES_SFTP) {
            if (!Files.exists(p)) {
                fail("Expected production source not found (run from module root): " + p.toAbsolutePath());
            }
            try (Stream<Path> s = Files.walk(p)) {
                for (Path f : s.filter(x -> x.toString().endsWith(".java")).toList()) {
                    String src = Files.readString(f, StandardCharsets.UTF_8);
                    assertThat(IPV4.matcher(src).find()).as("IPv4 literal in " + f).isFalse();
                    assertThat(PASSWORD_LITERAL.matcher(src).find()).as("inline password in " + f).isFalse();
                }
            }
        }
        Path props = Path.of("src/main/resources");
        try (Stream<Path> s = Files.walk(props)) {
            for (Path f : s.filter(x -> x.getFileName().toString().matches("application.*\\.(properties|ya?ml)")).toList()) {
                for (String linea : Files.readAllLines(f, StandardCharsets.UTF_8)) {
                    String l = linea.trim();
                    if (l.toLowerCase().contains("sftp") && !l.startsWith("#") && l.matches(".*[=:].*")) {
                        String valor = l.split("[=:]", 2)[1].trim();
                        assertThat(valor.isEmpty() || valor.startsWith("${"))
                                .as("SFTP setting must be a placeholder in " + f + " (key: " + l.split("[=:]", 2)[0] + ")")
                                .isTrue();
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ CONT-A-37 host-key verification
    // Approved deviation: SFTPConnector.java:47 used StrictHostKeyChecking=no. The server key must now match an
    // entry of the known_hosts file named by SFTP_KNOWN_HOSTS. Keys are generated in-process; nothing connects.

    @TempDir
    Path tmp;

    private static final String HOST = "sftp.example.invalid";
    private static final int PORT = 2222;

    /** Public-key blob of a freshly generated, throw-away RSA key (stands in for a server host key). */
    static byte[] claveServidor() throws JSchException {
        return KeyPair.genKeyPair(new JSch(), KeyPair.RSA, 2048).getPublicKeyBlob();
    }

    /** One OpenSSH known_hosts line: {@code [host]:port ssh-rsa <base64>}. */
    static String lineaKnownHosts(String alias, byte[] blob) {
        return alias + " ssh-rsa " + Base64.getEncoder().encodeToString(blob) + "\n";
    }

    private Path knownHosts(String contenido) throws IOException {
        Path f = tmp.resolve("known_hosts");
        Files.writeString(f, contenido, StandardCharsets.US_ASCII);
        return f;
    }

    private SftpSettings settings(Path knownHosts) {
        Map<String, String> env = envFalso();
        env.put("SFTP_KNOWN_HOSTS", knownHosts.toString());
        return SftpSettings.fromEnvironment(env);
    }

    @Test
    @DisplayName("CONT-A-37: sessions use StrictHostKeyChecking=yes with the read-only known_hosts verifier")
    void cont_a_37_sessionRequiresStrictHostKeyChecking() throws Exception {
        // SFTPConnector.java:47 setConfig("StrictHostKeyChecking", "no") -> replaced by strict checking
        Path kh = knownHosts(lineaKnownHosts("[" + HOST + "]:" + PORT, claveServidor()));
        Session session = new SftpSessionFactory(settings(kh)).crear();
        assertThat(session.getConfig("StrictHostKeyChecking")).isEqualTo("yes");
        assertThat(session.getHostKeyRepository()).isInstanceOf(KnownHostsVerifier.class);
        assertThat(session.isConnected()).isFalse();
    }

    @Test
    @DisplayName("CONT-A-37: a missing known_hosts file is rejected with a controlled error before connecting")
    void cont_a_37_missingKnownHostsFileRejected() {
        Path ausente = tmp.resolve("no-existe/known_hosts");
        assertThatThrownBy(() -> new SftpSessionFactory(settings(ausente)).crear())
                .isInstanceOf(SftpHostKeyException.class)
                .hasMessageContaining("known_hosts")
                .message().doesNotContain("fake-password-0000");
        assertThatThrownBy(() -> KnownHostsVerifier.load(ausente)).isInstanceOf(SftpHostKeyException.class);
    }

    @Test
    @DisplayName("CONT-A-37: a known_hosts file without any host key is rejected")
    void cont_a_37_emptyKnownHostsRejected() throws Exception {
        Path kh = knownHosts("# no keys\n");
        assertThatThrownBy(() -> KnownHostsVerifier.load(kh)).isInstanceOf(SftpHostKeyException.class);
        assertThatThrownBy(() -> new SftpSessionFactory(settings(kh)).crear())
                .isInstanceOf(SftpHostKeyException.class);
    }

    @Test
    @DisplayName("CONT-A-37: the server key matching its known_hosts entry is accepted")
    void cont_a_37_matchingHostKeyAccepted() throws Exception {
        byte[] clave = claveServidor();
        KnownHostsVerifier v = KnownHostsVerifier.load(knownHosts(lineaKnownHosts("[" + HOST + "]:" + PORT, clave)));
        v.verify(HOST, PORT, clave);
        // the call JSch makes during the handshake with StrictHostKeyChecking=yes
        assertThat(v.check("[" + HOST + "]:" + PORT, clave)).isEqualTo(HostKeyRepository.OK);
        assertThat(v.rechazo()).isEmpty();
    }

    @Test
    @DisplayName("CONT-A-37: a server key different from the known_hosts entry is rejected")
    void cont_a_37_wrongHostKeyRejected() throws Exception {
        KnownHostsVerifier v = KnownHostsVerifier.load(
                knownHosts(lineaKnownHosts("[" + HOST + "]:" + PORT, claveServidor())));
        byte[] otra = claveServidor();
        assertThatThrownBy(() -> v.verify(HOST, PORT, otra))
                .isInstanceOf(SftpHostKeyException.class)
                .hasMessageContaining("does not match");
        assertThat(v.check("[" + HOST + "]:" + PORT, otra)).isEqualTo(HostKeyRepository.CHANGED);
    }

    @Test
    @DisplayName("CONT-A-37: a server absent from known_hosts is rejected, even with a known key, and never learned")
    void cont_a_37_unknownHostRejectedAndNotLearned() throws Exception {
        byte[] clave = claveServidor();
        KnownHostsVerifier v = KnownHostsVerifier.load(knownHosts(lineaKnownHosts("otro.example.invalid", clave)));
        assertThatThrownBy(() -> v.verify(HOST, PORT, clave))
                .isInstanceOf(SftpHostKeyException.class)
                .hasMessageContaining("no matching entry");
        v.verify("otro.example.invalid", 22, clave);
        v.add(new HostKey("[" + HOST + "]:" + PORT, clave), null);
        assertThat(v.check("[" + HOST + "]:" + PORT, clave)).isEqualTo(HostKeyRepository.NOT_INCLUDED);
    }

    @Test
    @DisplayName("CONT-A-37: production sources never disable host-key checking")
    void cont_a_37_noStrictHostKeyCheckingNoInProductionSources() throws IOException {
        Pattern desactivado = Pattern.compile("\"StrictHostKeyChecking\"\\s*,\\s*\"no\"");
        for (Path p : FUENTES_SFTP) {
            try (Stream<Path> s = Files.walk(p)) {
                for (Path f : s.filter(x -> x.toString().endsWith(".java")).toList()) {
                    assertThat(desactivado.matcher(Files.readString(f, StandardCharsets.UTF_8)).find())
                            .as("host-key checking disabled in " + f).isFalse();
                }
            }
        }
    }
}
