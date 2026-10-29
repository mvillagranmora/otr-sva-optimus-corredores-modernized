package cl.bcs.tesoreria.jobs.job.port;

/**
 * Legacy {@code CargaMasivaIngresoEgresoDAPFFMM.getCabeceras(con, tipoArchivoCarga, fechaEjecucion, idEstado)}: the FTP
 * file headers ({@code FTP.Cabecera}) of a load type and date in a state ("PRO" processed, "PEN" pending). The job only
 * needs to know whether the list is empty. The service BcsTesoreriaServicios keeps this query private, so this is a port
 * that Step 2 implements with the same SQL (the trace name is {@code CargaMasivaIngresoEgresoDAPFFMM.getCabeceras}).
 */
public interface CabecerasCarga {

    /** Number of headers; the legacy returns a list and the job only tests {@code isEmpty()}. */
    int cantidad(String tipoArchivoCarga, String fechaEjecucion, String idEstado) throws Exception;
}
