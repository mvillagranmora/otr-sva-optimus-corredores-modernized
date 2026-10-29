package cl.bcs.normativa.jobs.service.valorizar;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;
import cl.bcs.normativa.jobs.port.out.NormativaHanaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Modernized BcsJobValorizarNCG499 / BcsJobValorizarDeclaracion.
 *
 * Legacy:
 *   BcsJobValorizarNCG499 → ValorizarNCG499.logica(conn, entrada)
 *   BcsJobValorizarDeclaracion → ValorizarDeclaracion.logica(conn, entrada)
 *
 * B-NJB-VAL-NCG-01: NCG499 parameters: IdEmpresa, IdUsuario, IdPais, IdModulo, Fecha
 * B-NJB-VAL-DEC-01: Declaracion parameters: IdEmpresa, IdUsuario, IdModulo, Version, FuenteDatos
 *
 * HANA dependency: ValorizarNCG499 and ValorizarDeclaracion call HANA SPs.
 * Integration tests: @Disabled("requires HANA").
 */
@Service
public class ValorizarService {

    private static final Logger log = LoggerFactory.getLogger(ValorizarService.class);

    private final NormativaHanaPort hanaPort;

    public ValorizarService(NormativaHanaPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    /**
     * Executes ValorizarNCG499.
     */
    public ResultadoJob valorizarNCG499(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        validarParametrosNCG499(parametros);
        log.info("ValorizarNCG499: jobId={} IdEmpresa={}", parametros.jobId(),
                parametros.requerido("IdEmpresa"));
        return hanaPort.ejecutarGeneracion(parametros); // delegated to HANA port
    }

    /**
     * Executes ValorizarDeclaracion.
     */
    public ResultadoJob valorizarDeclaracion(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        validarParametrosDeclaracion(parametros);
        log.info("ValorizarDeclaracion: jobId={} IdEmpresa={}", parametros.jobId(),
                parametros.requerido("IdEmpresa"));
        return hanaPort.ejecutarGeneracion(parametros); // delegated to HANA port
    }

    /**
     * Validates NCG499 parameters.
     * Pure logic — testable without Spring or HANA.
     * Required: IdEmpresa, IdUsuario, IdPais, IdModulo, Fecha
     */
    public static void validarParametrosNCG499(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        parametros.requeridoLong("IdEmpresa");
        parametros.requeridoLong("IdUsuario");
        parametros.requeridoLong("IdPais");
        parametros.requerido("IdModulo");
        parametros.requerido("Fecha");
    }

    /**
     * Validates Declaracion parameters.
     * Pure logic — testable without Spring or HANA.
     * Required: IdEmpresa, IdUsuario, IdModulo, Version, FuenteDatos
     */
    public static void validarParametrosDeclaracion(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        parametros.requeridoLong("IdEmpresa");
        parametros.requeridoLong("IdUsuario");
        parametros.requerido("IdModulo");
        parametros.requeridoLong("Version");
        parametros.requerido("FuenteDatos");
    }
}
