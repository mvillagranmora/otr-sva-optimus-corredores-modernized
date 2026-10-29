package cl.bcs.apv.domain.port.out;

import java.io.IOException;

/**
 * File-output seam (same port as BcsTesoreriaServicios) for the APV classes that read or write files (mass loads of Previred, banco, PAC,
 * bonificacion TGR). {@code rutaRelativa} is relative to the configured base folder; the golden "archivos" holds name and content.
 */
public interface AlmacenArchivos {
    void escribir(String rutaRelativa, byte[] contenido) throws IOException;
}
