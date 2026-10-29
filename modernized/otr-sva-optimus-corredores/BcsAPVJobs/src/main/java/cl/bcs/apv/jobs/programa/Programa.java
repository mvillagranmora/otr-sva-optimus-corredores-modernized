package cl.bcs.apv.jobs.programa;

/**
 * A row of {@code BAT.Programa} joined with its {@code BAT.TipoPrograma}: what the legacy Job Manager read for a program
 * (TransactionManager.readProgramm, lines 41-47) minus the schedule window.
 *
 * @param idTipoPrograma {@code NVARCHAR(40)} in HANA (a name such as GENERARARCHIVOPAGO, never a number)
 * @param idEstado       PEN (waiting), ASI (claimed), TER (finished) or ERR (failed)
 * @param bloqueo        'S' once claimed, 'N' while it can still be taken
 * @param nombreClase    {@code TipoPrograma.NombreClasePrograma}: the legacy job class name; {@code null} if the type has no row
 * @param maximoIntentos {@code TipoPrograma.MaximoIntentos} (0 when the type has no row)
 */
public record Programa(long idPrograma, String idEstado, String idTipoPrograma, int intento, String bloqueo, String nodo,
                       long idAgenda, String nombreClase, int maximoIntentos) {

    /** The legacy reading condition for a program that may run: {@code Bloqueo='N'}, PEN and {@code Intento < MaximoIntentos}. */
    public boolean reclamable() {
        return "N".equals(bloqueo) && "PEN".equals(idEstado) && intento < maximoIntentos;
    }

    /** One line for logs and error messages (never contains parameters). */
    public String resumen() {
        return "idEstado=" + idEstado + ", Bloqueo=" + bloqueo + ", Intento=" + intento + "/" + maximoIntentos;
    }
}
