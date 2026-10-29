package cl.bcs.tesoreria.familia_b.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import cl.bcs.tesoreria.port.out.AlmacenArchivos;

/**
 * Writes below the case's platform base folder ({@code PLATAFORMA/PLA.RutaBaseArchivos}, the per-case temporary directory that
 * {@link Archivos#capturar} reads back as the golden "archivos"); relative paths only, confined like the production adapter.
 */
public class AlmacenArchivosDeCaso implements AlmacenArchivos {
    private final Path base;

    public AlmacenArchivosDeCaso(Caso caso) {
        String ruta = caso.config().get("PLATAFORMA/PLA.RutaBaseArchivos");
        this.base = ruta == null ? null : Path.of(ruta).toAbsolutePath().normalize();
    }

    @Override
    public void escribir(String rutaRelativa, byte[] contenido) throws IOException {
        if (base == null) {
            throw new IOException("PLA.RutaBaseArchivos is not configured in the case");
        }
        Path destino = base.resolve(rutaRelativa).normalize();
        if (!destino.startsWith(base)) {
            throw new IOException("Path escapes the base directory: " + rutaRelativa);
        }
        // no directory is created: the legacy wrote only into an existing folder (a missing one is NoSuchFileException, TES-B-183)
        Files.write(destino, contenido);
    }
}
