package cl.bcs.contabilidad.jobs.guarda;

import cl.bcs.contabilidad.domain.port.out.ProcesoBatchPort;

import java.util.List;
import java.util.Objects;

/**
 * The guard around {@link ProcesoBatchPort} (HTTP adapter {@code TomEeProcesoBatchClient}, legacy {@code InscribirProcesoBatch}, TW #17): it
 * registers a program in the platform's {@code BAT.Programa} through TomEE, which commits on its own and is not idempotent, so a repeat would
 * register a second program. No job of this module calls it today (only {@code EnviarPreComprobanteService}, which no job reaches); the decorator is
 * there so that the day one does, the call is announced, closed and never repeated silently.
 */
public class ProcesoBatchGuardado implements ProcesoBatchPort {

    private final ProcesoBatchPort delegado;
    private final ColaboradorTomEEGuardado guarda;

    public ProcesoBatchGuardado(ProcesoBatchPort delegado, ColaboradorTomEEGuardado guarda) {
        this.delegado = Objects.requireNonNull(delegado, "delegado");
        this.guarda = Objects.requireNonNull(guarda, "guarda");
    }

    @Override
    public Inscripcion inscribir(Long idEmpresa, String idModulo, Long idUsuario, String idTipoPrograma, long segundosDesfase,
                                 List<Parametro> parametros) {
        return guarda.invocar(ClasificacionColaborador.INSCRIBIR_PROCESO_BATCH,
                () -> delegado.inscribir(idEmpresa, idModulo, idUsuario, idTipoPrograma, segundosDesfase, parametros));
    }
}
