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
 * Modernized BcsJobProcesarComisionesNN.
 *
 * Legacy: cl.bcs.fondomutuo.jobs.BcsJobProcesarComisionesNN
 * Parses comma-delimited cartera IDs, calls ProcesarComisionesNN service.
 *
 * Business rules (pure):
 *   B-FMJ-15  Comisiones parameter split by "," into IdCartera values.
 *   B-FMJ-16  Empty list → no-op.
 */
@Service
public class ProcesarComisionesNNJob {

    private static final Logger log = LoggerFactory.getLogger(ProcesarComisionesNNJob.class);
    public static final String ID_TIPO_PROGRAMA = "ComisionesNN";

    private final FondoMutuoServicioPort servicioPort;

    public ProcesarComisionesNNJob(FondoMutuoServicioPort servicioPort) {
        this.servicioPort = servicioPort;
    }

    /**
     * Parses a comma-delimited string of cartera IDs.
     *
     * B-FMJ-15: Legacy uses comisiones.split(",") — commas, not semicolons.
     *
     * @param comisiones comma-delimited Long IDs
     * @return list of IdCartera values
     */
    public static List<Long> parseComisiones(String comisiones) {
        if (comisiones == null || comisiones.isBlank()) return List.of();
        return Arrays.stream(comisiones.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    /**
     * Executes the ProcesarComisionesNN job.
     *
     * @param idEmpresa   company ID (required)
     * @param idUsuario   user ID (required)
     * @param idModulo    module ID
     * @param fechaDesde  start date
     * @param fechaHasta  end date
     * @param comisiones  comma-delimited cartera IDs (required)
     * @return service result map
     */
    public Map<String, Object> ejecutar(Long idEmpresa, Long idUsuario, String idModulo,
            String fechaDesde, String fechaHasta, String comisiones) {

        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");
        Objects.requireNonNull(comisiones, "comisiones is required");

        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);

        List<Long> idCarteras = parseComisiones(comisiones);

        // B-FMJ-16: empty → no-op
        if (idCarteras.isEmpty()) {
            log.info("*{}* No se enviaron comisiones para procesar", ID_TIPO_PROGRAMA);
            return Map.of("P_OUT_ERRORESCONTADOR", 0L, "procesadas", 0);
        }

        log.info("*{}* Procesando {} comisiones", ID_TIPO_PROGRAMA, idCarteras.size());
        Map<String, Object> resultado = servicioPort.procesarComisionesNN(
                idEmpresa, idUsuario, idModulo, fechaDesde, fechaHasta, idCarteras);

        log.info("*{}* Fin de Job", ID_TIPO_PROGRAMA);
        return resultado;
    }
}
