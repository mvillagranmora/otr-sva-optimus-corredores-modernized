package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import cl.bcs.fondomutuo.jobs.port.out.HanaFondoMutuoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Modernized GenerarArchivoSolicitudAporteRescateJob.
 *
 * Legacy: cl.bcs.fondomutuo.jobs.GenerarArchivoSolicitudAporteRescateJob
 * Calls GenerarArchivoSolicitudAporteRescate service, then updates programa output path.
 *
 * D-FMJ-18: Service call behind FondoMutuoServicioPort.
 * D-FMJ-19: Output path update behind HanaFondoMutuoPort.
 */
@Service
public class GenerarArchivoSolicitudAporteRescateJob {

    private static final Logger log = LoggerFactory.getLogger(GenerarArchivoSolicitudAporteRescateJob.class);
    public static final String ID_TIPO_PROGRAMA = "GENERARARCHIVOSOLICITARESFFMM";

    private final HanaFondoMutuoPort     hanaPort;
    private final FondoMutuoServicioPort servicioPort;

    public GenerarArchivoSolicitudAporteRescateJob(HanaFondoMutuoPort hanaPort,
            FondoMutuoServicioPort servicioPort) {
        this.hanaPort    = hanaPort;
        this.servicioPort = servicioPort;
    }

    /**
     * Generates the solicitud aporte/rescate file.
     *
     * @param jobId       program ID (required)
     * @param idEmpresa   company ID (required)
     * @param idUsuario   user ID (required)
     * @param idModulo    module ID
     * @param fechaDesde  start date
     * @param fechaHasta  end date
     * @param filtros     additional filter parameters (idTipoMovimiento, estado, idEmisor, etc.)
     * @return service result map
     */
    public Map<String, Object> ejecutar(Long jobId, Long idEmpresa, Long idUsuario,
            String idModulo, String fechaDesde, String fechaHasta, Map<String, Object> filtros) {

        Objects.requireNonNull(jobId, "jobId is required");
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");
        Objects.requireNonNull(filtros, "filtros is required");

        log.info("INICIO JOB {}", ID_TIPO_PROGRAMA);

        Map<String, Object> resultado = servicioPort.generarArchivoSolicitudAporteRescate(
                idEmpresa, idUsuario, idModulo, fechaDesde, fechaHasta, filtros);

        Object rutaSalida = resultado.get("P_OUT_RUTAARCHIVO");
        if (rutaSalida != null) {
            hanaPort.actualizarRutaSalidaPrograma(jobId, rutaSalida.toString());
        }

        log.info("FIN JOB {}", ID_TIPO_PROGRAMA);
        return resultado;
    }
}
