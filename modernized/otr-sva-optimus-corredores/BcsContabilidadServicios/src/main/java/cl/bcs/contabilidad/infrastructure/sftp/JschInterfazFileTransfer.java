package cl.bcs.contabilidad.infrastructure.sftp;

import cl.bcs.contabilidad.domain.port.out.InterfazFileTransfer;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * JSch implementation of {@link InterfazFileTransfer} (CONT-A-30..32).
 *
 * <p>Legacy: {@code SFTPConnector.removeDir} and {@code SFTPConnector.downloadDir} (which uploads), called from
 * {@code CreaInterfazDiaria.java:198-205}. Kept:
 * <ul>
 *   <li>removal targets {@code <remote base><last local segment>}; entries whose name starts with {@code .} are
 *       skipped; a missing remote copy or a failed deletion does not stop the run (legacy swallowed it silently,
 *       it is now logged);</li>
 *   <li>upload copies the local base path recursively into that same remote directory, creating directories that
 *       do not exist and overwriting files.</li>
 * </ul>
 * Changed: host keys are verified ({@link SftpSessionFactory}); remote paths are absolute instead of relative to
 * the session's working directory; local streams are closed. Settings are resolved on every call so the
 * application starts without SFTP variables and a missing one only fails the publication step.
 */
public final class JschInterfazFileTransfer implements InterfazFileTransfer {

    private static final Logger log = LoggerFactory.getLogger(JschInterfazFileTransfer.class);

    private final Supplier<SftpSettings> settings;

    public JschInterfazFileTransfer(Supplier<SftpSettings> settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    @Override
    public void removeRemoteCopy(String remoteBaseDirectory, Path localDirectory) throws IOException {
        String destino = InterfazFileTransfer.remoteCopyPath(remoteBaseDirectory, localDirectory);
        try (Canal canal = abrir()) {
            ChannelSftp sftp = canal.sftp();
            try {
                if (atributos(sftp, destino) == null) {
                    log.info("No remote copy to remove at {}", destino);
                } else {
                    borrar(sftp, destino);
                }
            } catch (SftpException | SftpTransferException e) {
                // legacy removeDir swallowed every failure once the channel was open
                log.warn("Remote copy {} not fully removed: {}", destino, e.getMessage());
            }
        }
    }

    @Override
    public void uploadDirectory(String remoteBaseDirectory, Path localDirectory) throws IOException {
        if (!Files.isDirectory(localDirectory)) {
            throw new NoSuchFileException(localDirectory.toString(), null, "local interfaz directory not found");
        }
        String destino = InterfazFileTransfer.remoteCopyPath(remoteBaseDirectory, localDirectory);
        try (Canal canal = abrir()) {
            subir(canal.sftp(), localDirectory, destino);
        } catch (SftpException e) {
            throw new SftpTransferException("Upload to " + destino + " failed: " + e.getMessage(), e);
        }
    }

    private void subir(ChannelSftp sftp, Path directorio, String remoto) throws SftpException, IOException {
        SftpATTRS attrs = atributos(sftp, remoto);
        if (attrs == null) {
            sftp.mkdir(remoto);
        } else if (!attrs.isDir()) {
            throw new SftpTransferException("Remote path " + remoto + " exists and is not a directory");
        }
        List<Path> hijos;
        try (Stream<Path> s = Files.list(directorio)) {
            hijos = s.sorted().toList();
        }
        for (Path hijo : hijos) {
            String remotoHijo = remoto + "/" + hijo.getFileName();
            if (Files.isDirectory(hijo)) {
                subir(sftp, hijo, remotoHijo);
            } else {
                try (InputStream in = Files.newInputStream(hijo)) {
                    sftp.put(in, remotoHijo, ChannelSftp.OVERWRITE);
                }
            }
        }
    }

    private static void borrar(ChannelSftp sftp, String remoto) throws SftpException {
        if (sftp.stat(remoto).isDir()) {
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> entradas = sftp.ls(remoto);
            for (ChannelSftp.LsEntry entrada : entradas) {
                if (!entrada.getFilename().startsWith(".")) {
                    borrar(sftp, remoto + "/" + entrada.getFilename());
                }
            }
            sftp.rmdir(remoto);
        } else {
            sftp.rm(remoto);
        }
    }

    /** Attributes of {@code remoto}, or {@code null} when it does not exist. */
    private static SftpATTRS atributos(ChannelSftp sftp, String remoto) throws SftpTransferException {
        try {
            return sftp.stat(remoto);
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return null;
            }
            throw new SftpTransferException("Cannot stat remote path " + remoto + ": " + e.getMessage(), e);
        }
    }

    private Canal abrir() throws SftpTransferException {
        Session session = new SftpSessionFactory(settings.get()).conectar();
        try {
            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
            return new Canal(session, sftp);
        } catch (JSchException e) {
            session.disconnect();
            throw new SftpTransferException("Cannot open SFTP channel: " + e.getMessage(), e);
        }
    }

    /** An open SFTP channel and its session, closed together. */
    private record Canal(Session session, ChannelSftp sftp) implements AutoCloseable {
        @Override
        public void close() {
            sftp.exit();
            session.disconnect();
        }
    }
}
