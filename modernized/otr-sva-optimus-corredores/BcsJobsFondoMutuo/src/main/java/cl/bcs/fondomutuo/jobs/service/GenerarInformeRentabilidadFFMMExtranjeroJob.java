package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import cl.bcs.fondomutuo.jobs.port.out.HanaFondoMutuoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Modernized BcsJobGenerarInformeRentabilidadFFMMExtranjero.
 *
 * Legacy: cl.bcs.fondomutuo.BcsJobGenerarInformeRentabilidadFFMMExtranjero
 * Calls GenerarInformeRentabilidadFFMMExtranjeroMasivo (Crystal Reports),
 * then updates programa output path.
 *
 * D-FMJ-22: Crystal Reports via FondoMutuoServicioPort.
 * D-FMJ-23: Output path update via HanaFondoMutuoPort.
 */
@Service
public class GenerarInformeRentabilidadFFMMExtranjeroJob {

    private static final Logger log = LoggerFactory.getLogger(GenerarInformeRentabilidadFFMMExtranjeroJob.class);
    public static final String ID_TIPO_PROGRAMA = "GenerarInformeRentabilidadFFMMExtranjero";

    private final HanaFondoMutuoPort     hanaPort;
    private final FondoMutuoServicioPort servicioPort;

    public GenerarInformeRentabilidadFFMMExtranjeroJob(HanaFondoMutuoPort hanaPort,
            FondoMutuoServicioPort servicioPort) {
        this.hanaPort    = hanaPort;
        this.servicioPort = servicioPort;
    }

    /**
     * Generates the rentabilidad FFMM extranjero informe (Crystal Reports).
     *
     * @param jobId        program ID (required)
     * @param idEmpresa    company ID (required)
     * @param idUsuario    user ID (required)
     * @param idModulo     module ID
     * @param agnoComercial commercial year (e.g. "2020")
     * @param esMasivo     "S" = all clients; "N" = only idCliente
     * @param idCliente    client ID; 0 means all clients (only relevant when esMasivo="N")
     * @return service result map
     */
    public Map<String, Object> ejecutar(Long jobId, Long idEmpresa, Long idUsuario,
            String idModulo, String agnoComercial, String esMasivo, Long idCliente) {

        Objects.requireNonNull(jobId, "jobId is required");
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");

        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);

        // B-FMJ-14: idCliente = 0 → all clients (empty list passed to service)
        List<Long> idClientes = (idCliente != null && idCliente != 0L) ? List.of(idCliente) : List.of();

        Map<String, Object> resultado = servicioPort.generarInformeRentabilidadExtranjero(
                idEmpresa, idUsuario, idModulo, agnoComercial, esMasivo, idClientes);

        Object rutaZip = resultado.get("p_out_RutaZip");
        Long errores = ((Number) resultado.getOrDefault("P_OUT_ERRORESCONTADOR", 0L)).longValue();
        if (errores == 0L && rutaZip != null) {
            hanaPort.actualizarRutaSalidaPrograma(jobId, rutaZip.toString());
        }

        log.info("*{}* Fin de Job", ID_TIPO_PROGRAMA);
        return resultado;
    }
}
