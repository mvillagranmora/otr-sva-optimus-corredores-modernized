package cl.bcs.contabilidad.infrastructure.sftp;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Read-only host-key repository backed by an OpenSSH known_hosts file (approved deviation from
 * {@code SFTPConnector.java:47 StrictHostKeyChecking=no}).
 *
 * <p>JSch calls {@link #check} during the handshake with the server's key; with
 * {@code StrictHostKeyChecking=yes} anything other than {@link #OK} aborts the connection. This repository never
 * learns keys ({@link #add} and {@code remove} do nothing), and it remembers why it rejected a key so the adapter
 * can report a precise {@link SftpHostKeyException} instead of JSch's generic error.
 */
public final class KnownHostsVerifier implements HostKeyRepository {

    private static final int PUERTO_SSH = 22;

    private final HostKeyRepository delegate;
    private final Path origen;
    private final AtomicReference<String> rechazo = new AtomicReference<>();

    private KnownHostsVerifier(HostKeyRepository delegate, Path origen) {
        this.delegate = delegate;
        this.origen = origen;
    }

    /**
     * Loads a known_hosts file.
     *
     * @throws SftpHostKeyException when the file is missing, unreadable, malformed or holds no host key
     */
    public static KnownHostsVerifier load(Path knownHosts) throws SftpHostKeyException {
        if (knownHosts == null || !Files.isRegularFile(knownHosts)) {
            throw new SftpHostKeyException("known_hosts file not found: " + knownHosts);
        }
        JSch jsch = new JSch();
        try (InputStream in = Files.newInputStream(knownHosts)) {
            jsch.setKnownHosts(in);
        } catch (IOException | JSchException e) {
            throw new SftpHostKeyException("Cannot read known_hosts file " + knownHosts + ": " + e.getMessage(), e);
        }
        HostKeyRepository repositorio = jsch.getHostKeyRepository();
        HostKey[] claves = repositorio.getHostKey();
        if (claves == null || claves.length == 0) {
            throw new SftpHostKeyException("known_hosts file " + knownHosts + " contains no host key");
        }
        return new KnownHostsVerifier(repositorio, knownHosts);
    }

    /** The name JSch looks up: {@code host} on port 22, {@code [host]:port} otherwise (OpenSSH convention). */
    public static String hostAlias(String host, int port) {
        return port == PUERTO_SSH ? host : "[" + host + "]:" + port;
    }

    /**
     * Verifies {@code key} (an SSH public-key blob) for {@code host:port}.
     *
     * @throws SftpHostKeyException when there is no entry for the host or the entry has a different key
     */
    public void verify(String host, int port, byte[] key) throws SftpHostKeyException {
        if (check(hostAlias(host, port), key) != OK) {
            throw new SftpHostKeyException(rechazo.get());
        }
    }

    /** Why the last key was rejected, if one was. */
    public Optional<String> rechazo() {
        return Optional.ofNullable(rechazo.get());
    }

    @Override
    public int check(String host, byte[] key) {
        int resultado = key == null ? NOT_INCLUDED : delegate.check(host, key);
        if (resultado == CHANGED) {
            rechazo.set("Host key of " + host + " does not match the entry in " + origen);
        } else if (resultado != OK) {
            rechazo.set("Host " + host + " has no matching entry in " + origen);
        } else {
            rechazo.set(null);
        }
        return resultado;
    }

    /** Never learns keys: a server not listed in known_hosts stays rejected. */
    @Override
    public void add(HostKey hostkey, UserInfo ui) {
        // read-only by design
    }

    @Override
    public void remove(String host, String type) {
        // read-only by design
    }

    @Override
    public void remove(String host, String type, byte[] key) {
        // read-only by design
    }

    @Override
    public String getKnownHostsRepositoryID() {
        return origen.toString();
    }

    @Override
    public HostKey[] getHostKey() {
        return delegate.getHostKey();
    }

    @Override
    public HostKey[] getHostKey(String host, String type) {
        return delegate.getHostKey(host, type);
    }
}
