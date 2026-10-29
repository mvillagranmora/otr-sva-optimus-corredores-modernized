package cl.bcs.contabilidad.infrastructure.sftp;

/**
 * The SFTP server's host key could not be verified: the known_hosts file is missing or unreadable, it has no
 * entry for the server, or the entry does not match the key the server presented.
 *
 * <p>Approved deviation: the legacy {@code SFTPConnector.connect} used {@code StrictHostKeyChecking=no} and
 * accepted any server.
 */
public class SftpHostKeyException extends SftpTransferException {

    public SftpHostKeyException(String message) {
        super(message);
    }

    public SftpHostKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
