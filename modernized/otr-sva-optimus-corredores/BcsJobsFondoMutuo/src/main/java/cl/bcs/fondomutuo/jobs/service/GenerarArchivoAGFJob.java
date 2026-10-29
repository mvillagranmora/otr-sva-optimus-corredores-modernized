package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import cl.bcs.fondomutuo.jobs.port.out.HanaFondoMutuoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Modernized GenerarArchivo (generaarchivooperacionesagf).
 *
 * Legacy: cl.bcs.fondomutuo.generaarchivooperacionesagf.GenerarArchivo
 * Calls GeneracionArchivoAGF service, then updates programa output path.
 *
 * D-FMJ-14: Ruta obtained from HanaFondoMutuoPort (config: PLA.RutaBaseArchivos).
 * D-FMJ-15: GeneracionArchivoAGF call via FondoMutuoServicioPort.
 */
@Service
public class GenerarArchivoAGFJob {

    private static final Logger log = LoggerFactory.getLogger(GenerarArchivoAGFJob.class);
    public static final String ID_TIPO_PROGRAMA = "GENERARARCHIVOAGFFFMM";

    private final HanaFondoMutuoPort     hanaPort;
    private final FondoMutuoServicioPort servicioPort;

    public GenerarArchivoAGFJob(HanaFondoMutuoPort hanaPort, FondoMutuoServicioPort servicioPort) {
        this.hanaPort    = hanaPort;
        this.servicioPort = servicioPort;
    }

    /**
     * Generates the AGF operations file.
     *
     * @param jobId       program ID for output path update (required)
     * @param idEmpresa   company ID (required)
     * @param idUsuario   user ID (required)
     * @param idModulo    module ID
     * @param fechaDesde  start date
     * @param fechaHasta  end date
     * @param idAmbiente  environment identifier
     * @return service result map
     */
    public Map<String, Object> ejecutar(Long jobId, Long idEmpresa, Long idUsuario,
            String idModulo, String fechaDesde, String fechaHasta, String idAmbiente) {
        Objects.requireNonNull(jobId, "jobId is required");
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");

        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);

        String ruta = hanaPort.consultarParametroConfiguracion(idEmpresa, "PLA.RutaBaseArchivos");

        Map<String, Object> resultado = servicioPort.generarArchivoAGF(
                idEmpresa, idUsuario, idModulo, fechaDesde, fechaHasta, idAmbiente, ruta);

        String nombreArchivo = (String) resultado.get("p_out_NombreArchivo");
        if (nombreArchivo != null) {
            hanaPort.actualizarRutaSalidaPrograma(jobId, nombreArchivo);
        }

        log.info("*{}* Fin de Job — archivo: {}", ID_TIPO_PROGRAMA, nombreArchivo);
        return resultado;
    }
}
