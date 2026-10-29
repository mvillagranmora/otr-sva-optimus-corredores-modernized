package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Modernized BcsJobCargaOnlineAGF.
 *
 * Legacy: cl.bcs.fondomutuo.BcsJobCargaOnlineAGF
 * Reads solicitudes pending for AGF from HANA, inserts into a secondary
 * SQL Server database (TARGET_URL_CARGA_ONLINE), then updates AGF status.
 *
 * D-FMJ-08: Cross-database write (HANA → SQL Server) deferred to Phase 3
 *   via FondoMutuoServicioPort. Phase 2 stub throws UnsupportedOperationException.
 * D-FMJ-09: TARGET_* connection parameters moved to application configuration.
 */
@Service
public class CargaOnlineAGFJob {

    private static final Logger log = LoggerFactory.getLogger(CargaOnlineAGFJob.class);
    public static final String ID_TIPO_PROGRAMA = "CARGAONLINEAGFFFMM";

    private static final Long ESTADO_PROCESADO = 2L; // ConstantesFondoMutuo.EstadoProcesado

    private final FondoMutuoServicioPort servicioPort;

    public CargaOnlineAGFJob(FondoMutuoServicioPort servicioPort) {
        this.servicioPort = servicioPort;
    }

    /**
     * Reads solicitudes for AGF, inserts them into the target database,
     * then marks them as processed in HANA.
     *
     * @param idEmpresa company ID (required)
     * @param idUsuario user ID (required)
     * @return list of solicitudes processed
     */
    public List<Map<String, Object>> ejecutar(Long idEmpresa, Long idUsuario) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");

        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);

        // 1. Query HANA for solicitudes pending for AGF
        List<Map<String, Object>> solicitudes = servicioPort.consultarSolicitudesParaAGF(
                idEmpresa, idUsuario, "FMM");

        if (solicitudes.isEmpty()) {
            log.info("*{}* No hay solicitudes para procesar", ID_TIPO_PROGRAMA);
            return solicitudes;
        }

        // 2. Extract IDs of inserted solicitudes (cross-DB insert done in port implementation)
        List<Long> idsInsertados = solicitudes.stream()
                .map(s -> (Long) s.get("IdInterfazAgf"))
                .toList();

        // 3. Update status to Procesado in HANA
        servicioPort.cambiarEstadoSolicitudesAGF(idEmpresa, idsInsertados, ESTADO_PROCESADO, "Sin observacion");

        log.info("*{}* Fin de Job — {} solicitudes procesadas", ID_TIPO_PROGRAMA, solicitudes.size());
        return solicitudes;
    }
}
