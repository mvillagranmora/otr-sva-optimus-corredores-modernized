package cl.bcs.tributario.jobs.service;

import cl.bcs.tributario.jobs.domain.CargaIpcResultado;
import cl.bcs.tributario.jobs.port.out.CargaIpcHanaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Job service for the IPC (Consumer Price Index) factor load process.
 *
 * <p>Business rules preserved from legacy {@code BcsJobCargaIPC}:</p>
 * <ul>
 *   <li>B-TRI-01: {@code idModulo}, {@code simularOperacion}, and
 *       {@code usaReglasNegocio} must be non-null.</li>
 *   <li>B-TRI-02: if {@code erroresContador > 0} the job must fail with a
 *       {@link RuntimeException} (legacy performed a rollback and re-threw).</li>
 *   <li>B-TRI-03: if {@code erroresContador == 0} the job succeeds.</li>
 *   <li>B-TRI-04: legacy default values — {@code idEmpresa=1}, {@code idUsuario=3}
 *       when absent from job parameters.</li>
 * </ul>
 *
 * <p>Bug fix vs. legacy: the legacy checked {@code sp.getObject(4)} (the
 * {@code SimularOperacion} IN parameter) instead of the errores OUT parameter 7.
 * The corrected check reads {@code erroresContador} (OUT param 7) from
 * {@link CargaIpcResultado}.</p>
 */
@Service
public class CargaIpcJobService {

    private static final Logger log = LoggerFactory.getLogger(CargaIpcJobService.class);

    /** Legacy default company ID when IdEmpresa param is absent. B-TRI-04 */
    public static final long DEFAULT_ID_EMPRESA = 1L;

    /** Legacy default user ID when IdUsuario param is absent. B-TRI-04 */
    public static final long DEFAULT_ID_USUARIO = 3L;

    private final CargaIpcHanaPort hanaPort;

    public CargaIpcJobService(CargaIpcHanaPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    /**
     * Executes the IPC load job.
     *
     * @param idEmpresa        company identifier
     * @param idUsuario        user identifier
     * @param idModulo         module identifier (must be non-null)
     * @param simularOperacion simulate-only flag (must be non-null)
     * @param usaReglasNegocio apply business rules flag (must be non-null)
     * @return the SP result when successful (erroresContador == 0)
     * @throws NullPointerException  if any required String param is null (B-TRI-01)
     * @throws RuntimeException      if erroresContador > 0 (B-TRI-02)
     */
    public CargaIpcResultado ejecutar(long idEmpresa, long idUsuario,
                                      String idModulo,
                                      String simularOperacion,
                                      String usaReglasNegocio) {
        Objects.requireNonNull(idModulo, "idModulo es requerido");
        Objects.requireNonNull(simularOperacion, "simularOperacion es requerido");
        Objects.requireNonNull(usaReglasNegocio, "usaReglasNegocio es requerido");

        log.info("*CargaIPC* Inicio de proceso — empresa={} usuario={} modulo={}",
                idEmpresa, idUsuario, idModulo);

        CargaIpcResultado resultado = hanaPort.ejecutarCargarValorFactorIpc(
                idEmpresa, idUsuario, idModulo, simularOperacion, usaReglasNegocio);

        if (!resultado.exitoso()) {
            log.error("*CargaIPC* Error controlado — erroresContador={}",
                    resultado.erroresContador());
            throw new RuntimeException(
                    "*CargaIPC* Error al procesar carga de IPC: erroresContador="
                    + resultado.erroresContador());
        }

        log.info("*CargaIPC* Fin de proceso — advertencias={}", resultado.advertenciasContador());
        return resultado;
    }

    /**
     * Parses a Long from a string, returning {@code defaultValue} when the string
     * is null or blank.
     *
     * <p>B-TRI-04: legacy used {@code Long.parseLong(this.getParam("IdEmpresa"))}
     * but fell back to hardcoded defaults when the param was not configured.</p>
     *
     * @param value        the string to parse
     * @param defaultValue fallback value
     * @return parsed long or the default
     * @throws NumberFormatException if the string is non-blank and not a valid long
     */
    public static long parseLongOrDefault(String value, long defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Long.parseLong(value);
    }
}
