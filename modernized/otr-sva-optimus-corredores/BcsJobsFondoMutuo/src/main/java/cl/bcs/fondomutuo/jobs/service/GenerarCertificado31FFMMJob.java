package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import cl.bcs.fondomutuo.jobs.port.out.HanaFondoMutuoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Modernized BcsJobGenerarCertificado31FFMM.
 *
 * Legacy: cl.bcs.fondomutuo.BcsJobGenerarCertificado31FFMM
 * Parses comma-delimited traspaso IDs, calls GenerarCertificado31Masivo (Crystal Reports),
 * then updates programa output path.
 *
 * D-FMJ-20: Crystal Reports via FondoMutuoServicioPort (delegates to BcsCrystalReports).
 * D-FMJ-21: Output path update via HanaFondoMutuoPort.
 */
@Service
public class GenerarCertificado31FFMMJob {

    private static final Logger log = LoggerFactory.getLogger(GenerarCertificado31FFMMJob.class);
    public static final String ID_TIPO_PROGRAMA = "GenerarCertificado31FFMM";

    private final HanaFondoMutuoPort     hanaPort;
    private final FondoMutuoServicioPort servicioPort;

    public GenerarCertificado31FFMMJob(HanaFondoMutuoPort hanaPort, FondoMutuoServicioPort servicioPort) {
        this.hanaPort    = hanaPort;
        this.servicioPort = servicioPort;
    }

    /**
     * Parses a comma-delimited string of traspaso IDs.
     * Legacy: listaTraspasosStr.split(",")
     *
     * @param listaTraspasos comma-delimited Long IDs
     * @return list of traspaso IDs
     */
    public static List<Long> parseListaTraspasos(String listaTraspasos) {
        if (listaTraspasos == null || listaTraspasos.isBlank()) return List.of();
        return Arrays.stream(listaTraspasos.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    /**
     * Generates the Certificado 31 FFMM (Crystal Reports).
     *
     * @param jobId           program ID (required)
     * @param idEmpresa       company ID (required)
     * @param idUsuario       user ID (required)
     * @param idModulo        module ID
     * @param ciudadCertificado city for the certificate
     * @param fechaCertificado  date for the certificate
     * @param listaTraspasos  comma-delimited traspaso IDs
     * @return service result map
     */
    public Map<String, Object> ejecutar(Long jobId, Long idEmpresa, Long idUsuario,
            String idModulo, String ciudadCertificado, String fechaCertificado, String listaTraspasos) {

        Objects.requireNonNull(jobId, "jobId is required");
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");

        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);

        List<Long> idTraspasos = parseListaTraspasos(listaTraspasos);

        Map<String, Object> resultado = servicioPort.generarCertificado31Masivo(
                idEmpresa, idUsuario, idModulo, ciudadCertificado, fechaCertificado, idTraspasos);

        Object rutaZip = resultado.get("p_out_RutaZip");
        Long errores = ((Number) resultado.getOrDefault("P_OUT_ERRORESCONTADOR", 0L)).longValue();
        if (errores == 0L && rutaZip != null) {
            hanaPort.actualizarRutaSalidaPrograma(jobId, rutaZip.toString());
        }

        log.info("*{}* Fin de Job — errores: {}", ID_TIPO_PROGRAMA, errores);
        return resultado;
    }
}
