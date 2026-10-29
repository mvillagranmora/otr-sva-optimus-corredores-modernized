package cl.bcs.normativa.jobs.service.serv;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;
import cl.bcs.normativa.jobs.port.out.NormativaHanaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Modernized BcsJobProcesarServ / BcsJobProcesarPautaServ.
 *
 * Legacy: BcsJobProcesarServ calls HANA SP NOR.ProcesarTarea
 *         via CALL "BSCL.Normativa.BD.Servicios::NOR.ProcesarTarea"(?,?,?,?)
 *
 * B-NJB-SRV-01: Parameters: IdEmpresa (long), IdTipoTarea (String), IdSolicitudCircular (long)
 * B-NJB-SRV-02: SP takes (idEmpresa, idTipoTarea, idSolicitudCircular, jobId)
 * B-NJB-SRV-03: Logs pauta entries at PEN (start), PEN (executing), TER (success), ERR (error)
 *
 * HANA dependency: NOR.ProcesarTarea SP.
 * Integration tests: @Disabled("requires HANA").
 */
@Service
public class ProcesarServService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarServService.class);

    private final NormativaHanaPort hanaPort;

    public ProcesarServService(NormativaHanaPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    /**
     * Executes the NOR.ProcesarTarea stored procedure.
     */
    public ResultadoJob ejecutar(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");

        long idEmpresa = parametros.requeridoLong("IdEmpresa");
        String idTipoTarea = parametros.requerido("IdTipoTarea");
        long idSolicitudCircular = parametros.requeridoLong("IdSolicitudCircular");

        log.info("NOR.ProcesarTarea: jobId={} idEmpresa={} idTipoTarea={} idSolicitudCircular={}",
                parametros.jobId(), idEmpresa, idTipoTarea, idSolicitudCircular);

        return hanaPort.procesarTarea(idEmpresa, idTipoTarea, idSolicitudCircular, parametros.jobId());
    }

    /**
     * Validates that the required parameters for ProcesarServ are present.
     * Pure logic — testable without Spring or HANA.
     *
     * Required: IdEmpresa (long), IdTipoTarea (String), IdSolicitudCircular (long)
     */
    public static void validarParametros(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        parametros.requeridoLong("IdEmpresa");
        parametros.requerido("IdTipoTarea");
        parametros.requeridoLong("IdSolicitudCircular");
    }
}
