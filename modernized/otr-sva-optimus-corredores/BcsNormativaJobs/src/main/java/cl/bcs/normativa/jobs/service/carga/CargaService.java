package cl.bcs.normativa.jobs.service.carga;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;
import cl.bcs.normativa.jobs.port.out.NormativaHanaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Modernized BcsJobCarga — Carga de datos normativos.
 *
 * Legacy: BcsJobCarga → ProcesoCarga.procesar()
 *
 * B-NJB-CAR-01: Loads DJ data into HANA normativa tables.
 *
 * HANA dependency: requires live HANA connection.
 * Integration tests: @Disabled("requires HANA").
 */
@Service
public class CargaService {

    private static final Logger log = LoggerFactory.getLogger(CargaService.class);

    private final NormativaHanaPort hanaPort;

    public CargaService(NormativaHanaPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    /**
     * Executes the carga process with the given job parameters.
     */
    public ResultadoJob ejecutar(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        log.info("Proceso de carga activado: jobId={}", parametros.jobId());
        ResultadoJob resultado = hanaPort.ejecutarCarga(parametros);
        log.info("Proceso de carga finalizado: exitoso={}", resultado.exitoso());
        return resultado;
    }

    /**
     * Validates that the required parameters for carga are present.
     * Pure logic — testable without Spring or HANA.
     */
    public static void validarParametros(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        parametros.requerido("idDocumento");
        parametros.requerido("idUsuario");
        parametros.requerido("schema");
    }
}
