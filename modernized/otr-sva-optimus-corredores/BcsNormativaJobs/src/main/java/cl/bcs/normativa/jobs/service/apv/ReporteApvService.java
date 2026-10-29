package cl.bcs.normativa.jobs.service.apv;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;
import cl.bcs.normativa.jobs.port.out.NormativaHanaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Modernized BcsJobGenerarReporteAPV / BcsJobCargarDatosReporteAPV.
 *
 * Legacy:
 *   BcsJobGenerarReporteAPV → GenerarReporteAPV.logica(conn, entrada)
 *   BcsJobCargarDatosReporteAPV → CargarDatosReporteAPV.logica(conn, entrada)
 *
 * B-NJB-APV-01: GenerarReporteAPV parameters: IdEmpresa, IdUsuario, FechaCierre
 * B-NJB-APV-02: CargarDatosReporteAPV parameters: IdEmpresa, IdUsuario, FechaCierre
 *
 * HANA dependency: both services call HANA SPs.
 * Integration tests: @Disabled("requires HANA").
 */
@Service
public class ReporteApvService {

    private static final Logger log = LoggerFactory.getLogger(ReporteApvService.class);

    private final NormativaHanaPort hanaPort;

    public ReporteApvService(NormativaHanaPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    /**
     * Generates the APV report.
     */
    public ResultadoJob generarReporte(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        validarParametros(parametros);
        log.info("GenerarReporteAPV: jobId={} IdEmpresa={}", parametros.jobId(),
                parametros.requerido("IdEmpresa"));
        return hanaPort.ejecutarGeneracion(parametros);
    }

    /**
     * Loads APV report data.
     */
    public ResultadoJob cargarDatos(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        validarParametros(parametros);
        log.info("CargarDatosReporteAPV: jobId={} IdEmpresa={}", parametros.jobId(),
                parametros.requerido("IdEmpresa"));
        return hanaPort.ejecutarCarga(parametros);
    }

    /**
     * Validates APV report parameters.
     * Pure logic — testable without Spring or HANA.
     * Required: IdEmpresa, IdUsuario, FechaCierre
     */
    public static void validarParametros(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        parametros.requeridoLong("IdEmpresa");
        parametros.requeridoLong("IdUsuario");
        parametros.requerido("FechaCierre");
    }
}
