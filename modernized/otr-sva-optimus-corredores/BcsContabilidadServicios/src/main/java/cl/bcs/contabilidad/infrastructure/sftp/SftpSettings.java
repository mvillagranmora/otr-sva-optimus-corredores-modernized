package cl.bcs.contabilidad.infrastructure.sftp;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Connection settings of the interface SFTP server (CONT-A-33, CONT-A-34).
 *
 * <p>Legacy: {@code CreaInterfazDiaria.java:202} read USERNAME, PASSWORD, HOST and PORT from the external file
 * {@code CreaInterfazSIGA.properties}, without defaults. They now come only from the environment, still without
 * defaults. {@code SFTP_KNOWN_HOSTS} is new: the legacy disabled host-key checking
 * ({@code StrictHostKeyChecking=no}); the server key is now verified against that OpenSSH known_hosts file
 * (approved deviation). {@link #toString()} never shows the password.
 */
public record SftpSettings(String host, int port, String username, String password, Path knownHosts) {

    public static final String ENV_HOST = "SFTP_HOST";
    public static final String ENV_PORT = "SFTP_PORT";
    public static final String ENV_USER = "SFTP_USER";
    public static final String ENV_CLAVE = "SFTP_PASSWORD";
    public static final String ENV_KNOWN_HOSTS = "SFTP_KNOWN_HOSTS";

    public SftpSettings {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        Objects.requireNonNull(knownHosts, "knownHosts");
    }

    /** Reads the process environment. */
    public static SftpSettings fromEnvironment() {
        return fromEnvironment(System.getenv());
    }

    /**
     * Reads {@code SFTP_HOST}, {@code SFTP_PORT}, {@code SFTP_USER}, {@code SFTP_PASSWORD} and
     * {@code SFTP_KNOWN_HOSTS}. Every one is required.
     *
     * @throws SftpConfigurationException naming the first missing or invalid variable (never its value)
     */
    public static SftpSettings fromEnvironment(Map<String, String> env) {
        String host = requerida(env, ENV_HOST);
        int port = puerto(requerida(env, ENV_PORT));
        String user = requerida(env, ENV_USER);
        String clave = requerida(env, ENV_CLAVE);
        Path knownHosts;
        try {
            knownHosts = Path.of(requerida(env, ENV_KNOWN_HOSTS));
        } catch (java.nio.file.InvalidPathException e) {
            throw new SftpConfigurationException(ENV_KNOWN_HOSTS + " is not a valid path");
        }
        return new SftpSettings(host, port, user, clave, knownHosts);
    }

    private static String requerida(Map<String, String> env, String nombre) {
        String valor = env.get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new SftpConfigurationException("Missing required environment variable " + nombre);
        }
        return valor.trim();
    }

    private static int puerto(String valor) {
        try {
            int p = Integer.parseInt(valor);
            if (p < 1 || p > 65535) {
                throw new SftpConfigurationException(ENV_PORT + " must be between 1 and 65535");
            }
            return p;
        } catch (NumberFormatException e) {
            throw new SftpConfigurationException(ENV_PORT + " is not a number");
        }
    }

    @Override
    public String toString() {
        return "SftpSettings[host=" + host + ", port=" + port + ", username=" + username
                + ", knownHosts=" + knownHosts + ", password=<redacted>]";
    }
}
