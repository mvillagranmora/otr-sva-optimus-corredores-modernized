package cl.bcs.plataforma.jobs.service.archivos;

import org.springframework.stereotype.Service;

/**
 * Service stub for CargarArchivoTasas and CargarArchivoTasasNemo jobs.
 *
 * These jobs read CSV/text files with interest rates and bulk-load them
 * into HANA via temp tables and batch UPSERT operations.
 * Full execution requires a live HANA connection.
 *
 * Legacy classes:
 *   - cl.bcs.plataforma.jobs.CargarArchivoTasasJob
 *   - cl.bcs.plataforma.jobs.CargarArchivoTasasNemoJob
 *   - cl.bcs.plataforma.jobs.GenerarArchivoTasas
 */
@Service
public class CargarArchivoTasasService {

    /**
     * Load a rate file into HANA.
     *
     * @throws UnsupportedOperationException always — requires HANA connection
     */
    public void cargarArchivoTasas(Long idEmpresa, Long idUsuario, String rutaArchivo) {
        throw new UnsupportedOperationException(
                "CargarArchivoTasasService.cargarArchivoTasas requires a live HANA connection");
    }

    /**
     * Load a Nemo-format rate file into HANA.
     *
     * @throws UnsupportedOperationException always — requires HANA connection
     */
    public void cargarArchivoTasasNemo(Long idEmpresa, Long idUsuario, String rutaArchivo) {
        throw new UnsupportedOperationException(
                "CargarArchivoTasasService.cargarArchivoTasasNemo requires a live HANA connection");
    }
}
