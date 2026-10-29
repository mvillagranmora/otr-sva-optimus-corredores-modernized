package cl.bcs.normativa.jobs.service.validacion;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;
import cl.bcs.normativa.jobs.port.out.NormativaHanaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Modernized BcsJobValidacion — Validación de documentos normativos.
 *
 * Legacy: BcsJobValidacion → ProcesoValidacion.procesar()
 *
 * B-NJB-VAL-01: Executes ProcesoValidacion which validates DJ data against HANA.
 *
 * HANA dependency: requires live HANA connection.
 * Integration tests: @Disabled("requires HANA").
 */
@Service
public class ValidacionService {

    private static final Logger log = LoggerFactory.getLogger(ValidacionService.class);

    private final NormativaHanaPort hanaPort;

    public ValidacionService(NormativaHanaPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    /**
     * Executes the validación process with the given job parameters.
     */
    public ResultadoJob ejecutar(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        log.info("Proceso de validacion activado: jobId={}", parametros.jobId());
        ResultadoJob resultado = hanaPort.ejecutarValidacion(parametros);
        log.info("Proceso de validacion finalizado: exitoso={}", resultado.exitoso());
        return resultado;
    }

    /**
     * Validates that the required parameters for validacion are present.
     * Pure logic — testable without Spring or HANA.
     *
     * Required parameters: idDocumento, idUsuario, schema
     */
    public static void validarParametros(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        parametros.requerido("idDocumento");
        parametros.requerido("idUsuario");
        parametros.requerido("schema");
    }
}
