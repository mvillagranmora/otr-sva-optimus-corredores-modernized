package cl.bcs.normativa.jobs.service.generacion;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;
import cl.bcs.normativa.jobs.port.out.NormativaHanaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Modernized BcsJobGeneracion — Generación de documentos normativos.
 *
 * Legacy: BcsJobGeneracion → ProcesoGeneracion.procesar()
 *
 * B-NJB-GEN-01: Executes ProcesoGeneracion which reads from HANA,
 *               generates regulatory files, updates document state.
 * B-NJB-GEN-02: DJ1 generates three file types: FX, IR, FI.
 *
 * HANA dependency: ProcesoGeneracion requires live HANA connection.
 * Integration tests: @Disabled("requires HANA").
 */
@Service
public class GeneracionService {

    private static final Logger log = LoggerFactory.getLogger(GeneracionService.class);

    private final NormativaHanaPort hanaPort;

    public GeneracionService(NormativaHanaPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    /**
     * Executes the generación process with the given job parameters.
     *
     * @param parametros job parameters from BAT.ParametrosPrograma
     * @return execution result
     */
    public ResultadoJob ejecutar(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        log.info("Proceso de generacion activado: jobId={} params={}", parametros.jobId(), parametros.parameters());
        ResultadoJob resultado = hanaPort.ejecutarGeneracion(parametros);
        log.info("Proceso de generacion finalizado: exitoso={}", resultado.exitoso());
        return resultado;
    }

    /**
     * Validates that the required parameters for generacion are present.
     * Pure logic — testable without Spring or HANA.
     *
     * Required parameters: idDocumento, idUsuario, schema
     *
     * @param parametros job parameters
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static void validarParametros(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        parametros.requerido("idDocumento");
        parametros.requerido("idUsuario");
        parametros.requerido("schema");
    }
}
