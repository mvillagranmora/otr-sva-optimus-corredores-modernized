package cl.bcs.contabilidad.infrastructure.sftp;

import java.io.IOException;

/**
 * The SFTP session or a transfer failed (CONT-A-31). The message never carries credentials.
 *
 * <p>Legacy: {@code JSchException}, {@code SftpException} and {@code IllegalAccessException} from
 * {@code SFTPConnector}, all only logged by {@code CreaInterfazDiaria}.
 */
public class SftpTransferException extends IOException {

    public SftpTransferException(String message) {
        super(message);
    }

    public SftpTransferException(String message, Throwable cause) {
        super(message, cause);
    }
}
