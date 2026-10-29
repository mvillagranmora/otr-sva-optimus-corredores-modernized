package cl.bcs.apv.infrastructure.adapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cl.bcs.apv.domain.port.out.AlmacenArchivos;

/**
 * Local-file-system implementation of {@link AlmacenArchivos}. Every path is resolved against ONE configured base directory
 * ({@code apv.archivos.base-dir}; it plays the role of the legacy platform parameter {@code PLATAFORMA/PLA.RutaBaseArchivos}) and a path
 * that escapes it (absolute, {@code ..}, a symbolic link out of the base) is rejected, so a value taken from a request or from the
 * database cannot make the service write outside the base. The destination folder must already exist (NoSuchFileException otherwise: the caller treats it as a missing folder, TES-B-183).
 */
@Component
public class AlmacenArchivosLocal implements AlmacenArchivos {

    private final Path base;

    public AlmacenArchivosLocal(@Value("${apv.archivos.base-dir:}") String baseDir) {
        // not configured: the application still starts, the first write fails (file output is disabled until a base directory is set)
        this.base = baseDir == null || baseDir.isBlank() ? null : Path.of(baseDir).toAbsolutePath().normalize();
    }

    @Override
    public void escribir(String rutaRelativa, byte[] contenido) throws IOException {
        Path destino = confinar(rutaRelativa);
        // the destination folder must already exist (legacy: a missing folder is an error, not created); re-check it is still inside the base after links are resolved
        if (!destino.getParent().toRealPath().startsWith(base.toRealPath())) {
            throw new IOException("Path escapes the base directory: " + rutaRelativa);
        }
        Files.write(destino, contenido);
    }

    /** The absolute, normalised destination of a relative path; rejects absolute paths and paths that leave the base. */
    Path confinar(String rutaRelativa) throws IOException {
        if (base == null) {
            throw new IllegalStateException("apv.archivos.base-dir is not configured");
        }
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            throw new IOException("Empty file path");
        }
        Path relativa = Path.of(rutaRelativa.replace('\\', '/'));
        if (relativa.isAbsolute() || relativa.getRoot() != null) {
            throw new IOException("Absolute paths are not accepted: " + rutaRelativa);
        }
        Path destino = base.resolve(relativa).normalize();
        if (!destino.startsWith(base) || destino.equals(base)) {
            throw new IOException("Path escapes the base directory: " + rutaRelativa);
        }
        return destino;
    }
}
