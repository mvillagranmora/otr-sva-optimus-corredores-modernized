package cl.bcs.normativa.jobs.service.costos;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;
import cl.bcs.normativa.jobs.port.out.NormativaHanaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Modernized BcsJobProcesarCostos / BcsJobProcesarCostosComplemento.
 *
 * Legacy:
 *   BcsJobProcesarCostos → ActualizarCostoUF_MLT.logica(conn, entrada)
 *   BcsJobProcesarCostosComplemento → similar complementary costs logic
 *
 * B-NJB-CST-01: ProcesarCostos parameters: IdEmpresa, IdUsuario
 * B-NJB-CST-02: Uses ActualizarCostoUF_MLT HANA service
 *
 * HANA dependency: ActualizarCostoUF_MLT calls HANA SPs.
 * Integration tests: @Disabled("requires HANA").
 */
@Service
public class ProcesarCostosService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCostosService.class);

    private final NormativaHanaPort hanaPort;

    public ProcesarCostosService(NormativaHanaPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    /**
     * Executes cost processing.
     */
    public ResultadoJob ejecutar(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        validarParametros(parametros);
        log.info("ProcesarCostos: jobId={} IdEmpresa={}", parametros.jobId(),
                parametros.requerido("IdEmpresa"));
        return hanaPort.ejecutarGeneracion(parametros);
    }

    /**
     * Validates ProcesarCostos parameters.
     * Pure logic — testable without Spring or HANA.
     * Required: IdEmpresa, IdUsuario
     */
    public static void validarParametros(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        parametros.requeridoLong("IdEmpresa");
        parametros.requeridoLong("IdUsuario");
    }
}
