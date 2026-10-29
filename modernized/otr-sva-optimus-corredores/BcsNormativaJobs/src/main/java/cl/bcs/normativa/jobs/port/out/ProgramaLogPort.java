package cl.bcs.normativa.jobs.port.out;

/**
 * Port for updating BAT.Programa records in HANA.
 * Corresponds to legacy SQL_UPDATE_ID_PROGRAMA batches in interface jobs.
 */
public interface ProgramaLogPort {

    /**
     * Updates the output path in BAT.Programa.
     * Legacy: UPDATE "BSCL.Plataforma.BD::BAT.Programa" SET "RutaSalida" = ? WHERE "IdPrograma" = ?
     */
    void actualizarRutaSalida(Long idPrograma, String rutaSalida);

    /**
     * Updates the output path, log path, and comment in BAT.Programa.
     * Legacy: InterfacesJob_update.sql batch.
     */
    void actualizarPrograma(Long idPrograma, String rutaArchivo, String rutaLog, String comentario);
}
