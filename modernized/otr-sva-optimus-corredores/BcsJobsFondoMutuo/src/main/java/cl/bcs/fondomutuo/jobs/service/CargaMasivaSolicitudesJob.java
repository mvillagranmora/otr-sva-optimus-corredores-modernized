package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Modernized BcsJobCargaMasivaSolicitudes.
 *
 * Legacy: cl.bcs.fondomutuo.BcsJobCargaMasivaSolicitudes
 * Parses semicolon-delimited transaction IDs, then calls ProcesarCargaSolicitudesAporteRescate.
 *
 * Business rules implemented (pure logic, tested in CargaMasivaSolicitudesJobTest):
 *   B-FMJ-01  Transacciones parameter split by ";" into individual IdCarga values.
 *   B-FMJ-02  Empty list → return false without calling service.
 *   B-FMJ-03  Error counter > 0 → propagate error message from salida.
 *
 * D-FMJ-06: main() removed.
 * D-FMJ-07: BcsJob replaced with @Service.
 */
@Service
public class CargaMasivaSolicitudesJob {

    private static final Logger log = LoggerFactory.getLogger(CargaMasivaSolicitudesJob.class);
    public static final String ID_TIPO_PROGRAMA = "CARGAMASIVASOLICITUDESFFMM";

    private final FondoMutuoServicioPort servicioPort;

    public CargaMasivaSolicitudesJob(FondoMutuoServicioPort servicioPort) {
        this.servicioPort = servicioPort;
    }

    /**
     * Parses the semicolon-delimited transacciones string into a list of IDs.
     *
     * B-FMJ-01: Split by ";" to get individual IdCarga values.
     *
     * @param transacciones semicolon-delimited string of Long IDs (e.g. "123;456;789")
     * @return list of Long IDs; empty list if transacciones is null or blank
     */
    public static List<Long> parseTransacciones(String transacciones) {
        if (transacciones == null || transacciones.isBlank()) {
            return List.of();
        }
        return Arrays.stream(transacciones.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    /**
     * Executes the carga masiva de solicitudes job.
     *
     * @param idEmpresa      company ID (required)
     * @param idUsuario      user ID (required)
     * @param transacciones  semicolon-delimited list of IdCarga (required)
     * @return service result map; empty map if no transactions to process
     * @throws IllegalArgumentException if no valid transaction IDs are found
     */
    public Map<String, Object> ejecutar(Long idEmpresa, Long idUsuario, String transacciones) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");
        Objects.requireNonNull(transacciones, "transacciones is required");

        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);
        log.info("*{}* idEmpresa: {}", ID_TIPO_PROGRAMA, idEmpresa);
        log.info("*{}* idUsuario: {}", ID_TIPO_PROGRAMA, idUsuario);
        log.info("*{}* transacciones: {}", ID_TIPO_PROGRAMA, transacciones);

        // B-FMJ-01: parse semicolon-delimited IDs
        List<Long> idCargas = parseTransacciones(transacciones);

        // B-FMJ-02: empty list → no-op
        if (idCargas.isEmpty()) {
            log.info("*{}* No se enviaron cargas para procesar", ID_TIPO_PROGRAMA);
            return Map.of("P_OUT_ERRORESCONTADOR", 0L, "procesadas", 0);
        }

        log.info("*{}* Procesando {} cargas", ID_TIPO_PROGRAMA, idCargas.size());
        Map<String, Object> resultado = servicioPort.procesarCargaMasivaSolicitudes(
                idEmpresa, idUsuario, "FMM", idCargas);
        log.info("*{}* Fin de Job", ID_TIPO_PROGRAMA);
        return resultado;
    }
}
