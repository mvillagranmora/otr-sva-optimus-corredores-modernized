package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import cl.bcs.fondomutuo.jobs.port.out.HanaFondoMutuoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Modernized BcsJobGenerarArchivosFFMM.
 *
 * Legacy: cl.bcs.fondomutuo.jobs.BcsJobGenerarArchivosFFMM
 * Routes to either GenerarArchivoMovimientos or GenerarArchivoParticipes
 * based on the IdArchivoFondoMutuo parameter.
 *
 * Business rules (pure, tested in GenerarArchivosFFMMJobTest):
 *   B-FMJ-11  IdArchivoFondoMutuo = "INFORME_MOV_FFMM" → GenerarArchivoMovimientos.
 *   B-FMJ-12  IdArchivoFondoMutuo = "INFORME_PARTICIPE_FFMM" → GenerarArchivoParticipes.
 *   B-FMJ-13  Any other value → IllegalArgumentException ("Tipo de archivo no válido").
 *
 * D-FMJ-16: Service calls behind FondoMutuoServicioPort.
 * D-FMJ-17: Output path update via HanaFondoMutuoPort.
 */
@Service
public class GenerarArchivosFFMMJob {

    private static final Logger log = LoggerFactory.getLogger(GenerarArchivosFFMMJob.class);
    public static final String ID_TIPO_PROGRAMA = "GENERARARCHIVOFFFMM";

    public static final String TIPO_MOVIMIENTOS = "INFORME_MOV_FFMM";
    public static final String TIPO_PARTICIPES  = "INFORME_PARTICIPE_FFMM";

    private final HanaFondoMutuoPort     hanaPort;
    private final FondoMutuoServicioPort servicioPort;

    public GenerarArchivosFFMMJob(HanaFondoMutuoPort hanaPort, FondoMutuoServicioPort servicioPort) {
        this.hanaPort    = hanaPort;
        this.servicioPort = servicioPort;
    }

    /**
     * Validates and resolves the idArchivoFondoMutuo parameter.
     *
     * B-FMJ-11/12/13: Only "INFORME_MOV_FFMM" and "INFORME_PARTICIPE_FFMM" are valid.
     *
     * @param idArchivoFondoMutuo the requested file type
     * @return the validated value (same as input)
     * @throws IllegalArgumentException if the value is not recognised
     */
    public static String validarTipoArchivo(String idArchivoFondoMutuo) {
        if (TIPO_MOVIMIENTOS.equals(idArchivoFondoMutuo) || TIPO_PARTICIPES.equals(idArchivoFondoMutuo)) {
            return idArchivoFondoMutuo;
        }
        throw new IllegalArgumentException("Tipo de archivo no válido: " + idArchivoFondoMutuo);
    }

    /**
     * Generates the requested FFMM file type.
     *
     * @param jobId               program ID (required)
     * @param idEmpresa           company ID (required)
     * @param idUsuario           user ID (required)
     * @param fechaCurse          settlement date
     * @param estadoMovimiento    movement state filter
     * @param idArchivoFondoMutuo file type: "INFORME_MOV_FFMM" or "INFORME_PARTICIPE_FFMM"
     * @param idMoneda            currency filter
     * @param fondoMutuo          fund filter
     * @param tipoSolicitud       solicitud type filter
     * @param idEmisor            emitter filter
     * @return service result map
     * @throws IllegalArgumentException if idArchivoFondoMutuo is not recognised (B-FMJ-13)
     */
    public Map<String, Object> ejecutar(Long jobId, Long idEmpresa, Long idUsuario,
            String fechaCurse, String estadoMovimiento, String idArchivoFondoMutuo,
            String idMoneda, String fondoMutuo, String tipoSolicitud, String idEmisor) {

        Objects.requireNonNull(jobId, "jobId is required");
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");
        Objects.requireNonNull(idArchivoFondoMutuo, "idArchivoFondoMutuo is required");

        log.info("INICIO JOB {} — tipo: {}", ID_TIPO_PROGRAMA, idArchivoFondoMutuo);

        // B-FMJ-13: validate type
        validarTipoArchivo(idArchivoFondoMutuo);

        Map<String, Object> resultado;

        if (TIPO_MOVIMIENTOS.equals(idArchivoFondoMutuo)) {
            // B-FMJ-11: movimientos
            resultado = servicioPort.generarArchivoMovimientos(
                    idEmpresa, idUsuario, fechaCurse, fondoMutuo, idEmisor,
                    estadoMovimiento, tipoSolicitud, idMoneda);
        } else {
            // B-FMJ-12: participes
            resultado = servicioPort.generarArchivoParticipes(
                    idEmpresa, idUsuario, "FFMM", fechaCurse, fondoMutuo, idEmisor,
                    estadoMovimiento, tipoSolicitud, idMoneda);
        }

        // Update programa output path
        Object rutaArchivo = resultado.get("P_OUT_RUTA_ARCHIVO");
        Object rutaLog     = resultado.get("P_OUT_RUTA_ARCHIVO_LOG");
        if (rutaArchivo != null) {
            hanaPort.actualizarRutaSalidaPrograma(jobId, rutaArchivo.toString());
        }

        log.info("FIN JOB {}", ID_TIPO_PROGRAMA);
        return resultado;
    }
}
