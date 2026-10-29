package cl.bcs.contabilidad.infrastructure.sftp;

/**
 * An SFTP setting is missing or invalid (CONT-A-33). The message names the variable, never its value.
 *
 * <p>Legacy: {@code Integer.parseInt(properties.getProperty("PORT"))} and JSch failed with unrelated errors.
 */
public class SftpConfigurationException extends IllegalStateException {

    public SftpConfigurationException(String message) {
        super(message);
    }
}
