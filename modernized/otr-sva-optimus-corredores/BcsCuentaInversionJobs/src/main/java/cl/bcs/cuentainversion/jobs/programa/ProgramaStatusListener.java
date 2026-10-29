package cl.bcs.cuentainversion.jobs.programa;

import cl.bcs.cuentainversion.jobs.runtime.JobStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * The real {@link JobStatusListener} (job pattern H4): at the end of a run it writes the legacy final state of the program in its own
 * transaction ({@link ProgramaRepositorio}), after the job transaction has already committed or rolled back.
 *
 * <p>Legacy: {@code BcsJobListener.status} -> {@code TransactionManager.updateProgramm} (BcsJob.java:151,158; SB:150-173; TM:212-257).
 * <ul>
 *   <li>{@code status(id, false, "")} (a commit AND a rollback of the job, legacy defect CIJ-DEF-01 kept): TER, {@code Resultado} =
 *       "Finalizado correctamente.";</li>
 *   <li>{@code status(id, true, msg)} (the job threw): ERR, {@code Resultado} = "Terminado con errores:" + msg.</li>
 * </ul>
 * Both set {@code fechaFin} and {@code Agenda.UltimaFechaDeEjecucion}; {@code Resultado} is cut to 1999 characters when longer than 2000.
 * A failure writing the state propagates to {@code JobRunner}, which logs it and keeps the run result (review M8): the program then stays
 * ASI / Bloqueo 'S', which no automatic retry takes (see TRANSFORMATION_NOTES, limitation about crashes).
 */
public class ProgramaStatusListener implements JobStatusListener {

    private static final Logger log = LoggerFactory.getLogger(ProgramaStatusListener.class);

    private final ProgramaRepositorio repositorio;

    public ProgramaStatusListener(ProgramaRepositorio repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio, "repositorio");
    }

    @Override
    public void status(long idPrograma, boolean error, String mensaje) {
        boolean existe = error
                ? repositorio.error(idPrograma, TextoResultado.conErrores(mensaje))
                : repositorio.terminar(idPrograma, ProgramaRepositorio.RESULTADO_OK);
        if (existe) {
            log.info("Program #{} final state: {}", idPrograma, error ? "ERR" : "TER");
        } else {
            log.warn("Program #{} has no row in BAT.Programa: its final state was not written", idPrograma);
        }
    }

    /**
     * Marks the program as "requires review" (ERR, Intento = MaximoIntentos, Bloqueo 'S', prefixed Resultado): the manager does not retry it
     * by itself. For the later step in which a collaborator already committed outside the job transaction and the job then fails.
     */
    public void marcarRequiereRevision(long idPrograma, String motivo) {
        if (!repositorio.requiereRevision(idPrograma, motivo)) {
            log.warn("Program #{} has no row in BAT.Programa: it could not be marked as requiring review", idPrograma);
        }
    }
}
