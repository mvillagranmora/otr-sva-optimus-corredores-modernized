package cl.bcs.normativa.jobs.service.cuadratura;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;
import cl.bcs.normativa.jobs.port.out.NormativaHanaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Modernized BcsJobCuadratura — Cuadratura de datos normativos.
 *
 * Legacy: BcsJobCuadratura → ProcesoCuadratura.procesar()
 *
 * B-NJB-CUA-01: Reconciles DJ data against HANA reference data.
 *
 * HANA dependency: requires live HANA connection.
 * Integration tests: @Disabled("requires HANA").
 */
@Service
public class CuadraturaService {

    private static final Logger log = LoggerFactory.getLogger(CuadraturaService.class);

    private final NormativaHanaPort hanaPort;

    public CuadraturaService(NormativaHanaPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    /**
     * Executes the cuadratura process with the given job parameters.
     */
    public ResultadoJob ejecutar(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        log.info("Proceso de cuadratura activado: jobId={}", parametros.jobId());
        ResultadoJob resultado = hanaPort.ejecutarCuadratura(parametros);
        log.info("Proceso de cuadratura finalizado: exitoso={}", resultado.exitoso());
        return resultado;
    }

    /**
     * Validates that the required parameters for cuadratura are present.
     * Pure logic — testable without Spring or HANA.
     */
    public static void validarParametros(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        parametros.requerido("idDocumento");
        parametros.requerido("idUsuario");
        parametros.requerido("schema");
    }
}
