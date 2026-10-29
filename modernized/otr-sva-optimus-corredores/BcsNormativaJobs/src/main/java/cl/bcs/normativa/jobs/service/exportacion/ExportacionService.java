package cl.bcs.normativa.jobs.service.exportacion;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;
import cl.bcs.normativa.jobs.port.out.NormativaHanaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Modernized BcsJobExportacion / BcsJobExportacionNCG380 — Exportación de datos.
 *
 * Legacy: BcsJobExportacion → ProcesoExportacion.procesar()
 *         BcsJobExportacionNCG380 → ProcesoExportacionServ.procesar()
 *
 * B-NJB-EXP-01: Exports DJ data to regulatory files.
 * B-NJB-EXP-02: NCG380 variant uses a separate service process.
 *
 * HANA dependency: requires live HANA connection.
 * Integration tests: @Disabled("requires HANA").
 */
@Service
public class ExportacionService {

    private static final Logger log = LoggerFactory.getLogger(ExportacionService.class);

    private final NormativaHanaPort hanaPort;

    public ExportacionService(NormativaHanaPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    /**
     * Executes the standard exportación process.
     */
    public ResultadoJob ejecutar(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        log.info("Proceso de exportacion activado: jobId={}", parametros.jobId());
        ResultadoJob resultado = hanaPort.ejecutarExportacion(parametros);
        log.info("Proceso de exportacion finalizado: exitoso={}", resultado.exitoso());
        return resultado;
    }

    /**
     * Executes the NCG380/serv exportación process.
     */
    public ResultadoJob ejecutarServ(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        log.info("Proceso de exportacion serv (NCG380) activado: jobId={}", parametros.jobId());
        ResultadoJob resultado = hanaPort.ejecutarExportacionServ(parametros);
        log.info("Proceso de exportacion serv finalizado: exitoso={}", resultado.exitoso());
        return resultado;
    }

    /**
     * Validates that the required parameters for exportacion are present.
     * Pure logic — testable without Spring or HANA.
     */
    public static void validarParametros(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        parametros.requerido("idDocumento");
        parametros.requerido("idUsuario");
        parametros.requerido("schema");
    }
}
