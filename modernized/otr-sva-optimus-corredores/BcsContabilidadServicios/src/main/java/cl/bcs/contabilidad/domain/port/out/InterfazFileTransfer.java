package cl.bcs.contabilidad.domain.port.out;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Publishes the generated interface tree to the BPC server (CONT-A-30..32).
 *
 * <p>Legacy: {@code CreaInterfazDiaria.java:198-205} called {@code SFTPConnector.removeDir} and then
 * {@code SFTPConnector.downloadDir}, which despite its name UPLOADS the whole local base path, recursively, to
 * {@code <remote base>/<last segment of the local path>}.
 */
public interface InterfazFileTransfer {

    /** Remote parent directory used by the legacy job. */
    String REMOTE_BASE_DIRECTORY = "/home/interfaz-bpc/";

    /** Deletes the remote copy of {@code localDirectory} ({@link #remoteCopyPath}), if there is one. */
    void removeRemoteCopy(String remoteBaseDirectory, Path localDirectory) throws IOException;

    /** Uploads {@code localDirectory} and everything below it to {@link #remoteCopyPath}, overwriting files. */
    void uploadDirectory(String remoteBaseDirectory, Path localDirectory) throws IOException;

    /**
     * Remote location of the copy: the remote base followed by the last segment of the local path, joined by
     * plain concatenation as the legacy did ({@code SFTPConnector.java:212-216}).
     */
    static String remoteCopyPath(String remoteBaseDirectory, Path localDirectory) {
        Path name = localDirectory.getFileName();
        return remoteBaseDirectory + (name == null ? "" : name.toString());
    }
}
