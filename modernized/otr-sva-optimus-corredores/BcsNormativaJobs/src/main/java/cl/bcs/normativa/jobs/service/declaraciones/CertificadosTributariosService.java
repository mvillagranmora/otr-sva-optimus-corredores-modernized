package cl.bcs.normativa.jobs.service.declaraciones;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;
import cl.bcs.normativa.jobs.port.out.NormativaHanaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Modernized BcsJobGenerarCertificadosTributarios / BcsJobGrabarCertificadoCliente
 * / BcsJobCertificadosClienteMasivo.
 *
 * Legacy: calls GenerarCertificadoXxMasivo.logica(conn, entrada)
 *   for cert types 9, 17, 21, 24, 36, 43, 71
 *
 * B-NJB-CERT-01: CertificadosTributarios generates tax certificates for clients
 * B-NJB-CERT-02: Supports cert types 9 (int), 17 (acciones), 21 (renta), 24, 36, 43, 71
 * B-NJB-CERT-03: Parameters: IdEmpresa, IdUsuario, AnoTributario, IdCliente (or "TODOS")
 *
 * HANA dependency: GenerarCertificadoXxMasivo calls HANA SPs.
 * Integration tests: @Disabled("requires HANA").
 */
@Service
public class CertificadosTributariosService {

    private static final Logger log = LoggerFactory.getLogger(CertificadosTributariosService.class);

    private final NormativaHanaPort hanaPort;

    public CertificadosTributariosService(NormativaHanaPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    /**
     * Generates tax certificates for clients.
     */
    public ResultadoJob generarCertificados(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        validarParametros(parametros);
        log.info("GenerarCertificadosTributarios: jobId={} IdEmpresa={}", parametros.jobId(),
                parametros.requerido("IdEmpresa"));
        return hanaPort.ejecutarGeneracion(parametros);
    }

    /**
     * Validates CertificadosTributarios parameters.
     * Pure logic — testable without Spring or HANA.
     * Required: IdEmpresa, IdUsuario, AnoTributario
     */
    public static void validarParametros(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        parametros.requeridoLong("IdEmpresa");
        parametros.requeridoLong("IdUsuario");
        parametros.requerido("AnoTributario");
    }

    /**
     * Validates that AnoTributario is a 4-digit year.
     * Pure logic — testable without Spring or HANA.
     *
     * @param anoTributario year string from parameters
     * @throws IllegalArgumentException if not a valid 4-digit year
     */
    public static int parsearAnoTributario(String anoTributario) {
        Objects.requireNonNull(anoTributario, "anoTributario must not be null");
        try {
            int ano = Integer.parseInt(anoTributario.trim());
            if (ano < 1900 || ano > 2100) {
                throw new IllegalArgumentException(
                        "AnoTributario fuera de rango [1900,2100]: " + anoTributario);
            }
            return ano;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("AnoTributario no es numérico: " + anoTributario, e);
        }
    }
}
