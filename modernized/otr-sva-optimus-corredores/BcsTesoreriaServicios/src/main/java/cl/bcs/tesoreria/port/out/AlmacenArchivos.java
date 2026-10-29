package cl.bcs.tesoreria.port.out;

import java.io.IOException;

/**
 * file-output seam for the classes that write files (CargaMasivaIngresoEgresoDAPFFMM writes a CSV below the folder configured in
 * TESORERIA/...; legacy opens a FileWriter). {@code rutaRelativa} is relative to the configured base folder; the golden "archivos" holds name and content.
 */
public interface AlmacenArchivos {
    void escribir(String rutaRelativa, byte[] contenido) throws IOException;
}
