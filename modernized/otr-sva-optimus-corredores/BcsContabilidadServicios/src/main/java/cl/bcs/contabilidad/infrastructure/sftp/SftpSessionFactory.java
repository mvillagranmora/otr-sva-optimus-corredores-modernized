package cl.bcs.contabilidad.infrastructure.sftp;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.Objects;

/**
 * Builds JSch sessions that always verify the server's host key (approved deviation).
 *
 * <p>Legacy: {@code SFTPConnector.connect(username, password, host, port)} with password authentication, a
 * 500 000 ms timeout and {@code StrictHostKeyChecking=no}. Everything is kept except the last setting: sessions use
 * {@code StrictHostKeyChecking=yes} with a read-only {@link KnownHostsVerifier} loaded from
 * {@link SftpSettings#knownHosts()}, so a missing known_hosts file, an unknown server or a changed key fails with
 * {@link SftpHostKeyException}.
 */
public final class SftpSessionFactory {

    /** Legacy {@code session.setTimeout(500000)}. */
    static final int TIMEOUT_MS = 500_000;

    private final SftpSettings settings;

    public SftpSessionFactory(SftpSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    /**
     * An unconnected session configured for strict host-key checking.
     *
     * @throws SftpHostKeyException when the known_hosts file cannot be used
     * @throws SftpTransferException when JSch rejects the settings
     */
    public Session crear() throws SftpTransferException {
        KnownHostsVerifier verificador = KnownHostsVerifier.load(settings.knownHosts());
        try {
            JSch jsch = new JSch();
            jsch.setHostKeyRepository(verificador);
            Session session = jsch.getSession(settings.username(), settings.host(), settings.port());
            session.setHostKeyRepository(verificador);
            session.setPassword(settings.password());
            session.setTimeout(TIMEOUT_MS);
            session.setConfig("StrictHostKeyChecking", "yes");
            session.setConfig("HashKnownHosts", "no");
            return session;
        } catch (JSchException e) {
            throw new SftpTransferException("Cannot configure SFTP session for " + destino() + ": "
                    + e.getMessage(), e);
        }
    }

    /**
     * A connected session.
     *
     * @throws SftpHostKeyException when the server's key is not in known_hosts or does not match it
     * @throws SftpTransferException for any other connection failure
     */
    public Session conectar() throws SftpTransferException {
        Session session = crear();
        try {
            session.connect();
            return session;
        } catch (JSchException e) {
            session.disconnect();
            if (session.getHostKeyRepository() instanceof KnownHostsVerifier v && v.rechazo().isPresent()) {
                throw new SftpHostKeyException("SFTP server rejected: " + v.rechazo().get(), e);
            }
            throw new SftpTransferException("Cannot connect to SFTP server " + destino() + ": " + e.getMessage(), e);
        }
    }

    private String destino() {
        return settings.username() + "@" + settings.host() + ":" + settings.port();
    }
}
