package cl.bcs.tesoreria.jobs.programa;

import java.util.Map;
import java.util.Optional;

/**
 * Port to {@code BAT.Programa} for job pattern H4 (see {@code TRANSFORMATION_NOTES.md}, "Job pattern H4").
 *
 * <p>Every method that writes COMMITS BY ITSELF, in its own connection and transaction, never the transaction of the job: the
 * legacy Job Manager did the same ({@code TransactionManager.updateProgramm} opened its own connection from the Job Manager
 * data source and called {@code con.commit()}, TransactionManager.java:212-257), and the claim must be visible to other
 * instances before the job starts, while the final state must survive a rollback of the job.
 *
 * <p>No database change is made by any implementation: only existing columns and tables of {@code BAT} are used.
 */
public interface ProgramaRepositorio {

    /** {@code Resultado} written when a program is claimed (legacy: updateProgramm(id, 0, "Programa Encolado"), JM:74). */
    String RESULTADO_ENCOLADO = "Programa Encolado";
    /** {@code Resultado} written when a run ends without error (legacy: BcsJobListener, SB:150-173). */
    String RESULTADO_OK = "Finalizado correctamente.";
    /** Prefix of {@code Resultado} for the state "requires review" (the only marker available without a column). */
    String PREFIJO_REVISION = "REQUIERE REVISION: ";

    /** The program row with its class and {@code MaximoIntentos}; empty if {@code BAT.Programa} has no such row. */
    Optional<Programa> cargarPrograma(long idPrograma);

    /**
     * The parameters of the program from the view {@code BAT.ConsultarParametrosProgramas}: {@code ValorBlob}, when present, is
     * decoded as UTF-8 and replaces {@code Valor} (TransactionManager.java:490-495); the last duplicate wins; entries whose value is
     * null are dropped (the legacy {@code getParam} gave null for them as for a missing name). A database error is NEVER swallowed
     * (the legacy returned an empty map silently, :499-501): it is a {@link ProgramaJdbcException}.
     */
    Map<String, String> parametros(long idPrograma);

    /**
     * Atomic claim: {@code Bloqueo='S'}, {@code idEstado='ASI'}, {@code Intento+1}, {@code Resultado='Programa Encolado'} only if
     * the row is still {@code Bloqueo='N'}, PEN and {@code Intento < MaximoIntentos}. Returns false when nothing was claimed
     * (row missing, or already claimed by another instance or run): the caller answers 409. Legacy claim was not atomic
     * (TransactionManager.java:68-73: no condition, no check of the affected rows).
     */
    boolean reclamar(long idPrograma);

    /** Legacy final state TER: {@code fechaFin=now}, {@code Resultado} cut as the legacy did, Agenda.UltimaFechaDeEjecucion. */
    boolean terminar(long idPrograma, String resultado);

    /** Legacy final state ERR (same columns as {@link #terminar}); the caller supplies the full text, e.g. "Terminado con errores:...". */
    boolean error(long idPrograma, String resultado);

    /**
     * State "requires review": ERR, {@code Intento = MaximoIntentos}, {@code Bloqueo='S'}, {@code Resultado} prefixed with
     * {@link #PREFIJO_REVISION}. Neither the legacy reading query (PEN only) nor the modern retry (Intento &lt; Max) takes it.
     *
     * @return false if the program row does not exist
     */
    boolean requiereRevision(long idPrograma, String motivo);
}
