package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Modernized BcsJobCertificacionMasivaSolicitudes.
 *
 * Legacy: cl.bcs.fondomutuo.BcsJobCertificacionMasivaSolicitudes
 * Parses multi-value parameters and calls certification services.
 *
 * Business rules implemented (pure logic, tested in CertificacionMasivaSolicitudesJobTest):
 *   B-FMJ-04  esMasivo="S" → uses CertificarPorFiltrosSolicitudAporteRescate with filter arrays.
 *   B-FMJ-05  esMasivo≠"S" → uses CertificarSolicitudAporteRescate with individual solicitud IDs.
 *   B-FMJ-06  Parameters IdInternoFiltro, IdEmisor, etc. split by ";" into parallel arrays.
 *   B-FMJ-07  Empty filter/solicitud list → no-op.
 *
 * D-FMJ-10: main() removed.
 * D-FMJ-11: BcsJob replaced with @Service.
 */
@Service
public class CertificacionMasivaSolicitudesJob {

    private static final Logger log = LoggerFactory.getLogger(CertificacionMasivaSolicitudesJob.class);
    public static final String ID_TIPO_PROGRAMA = "CERTIFICACIONMASIVASOLICITUDESFFMM";

    private final FondoMutuoServicioPort servicioPort;

    public CertificacionMasivaSolicitudesJob(FondoMutuoServicioPort servicioPort) {
        this.servicioPort = servicioPort;
    }

    /**
     * Builds the list of filter maps from parallel semicolon-delimited parameter arrays.
     *
     * B-FMJ-06: Each parameter is a semicolon-delimited string; position i across all
     * parameters forms one filter record.
     *
     * @param idInternoFiltro         semicolon-delimited IdInternoFiltro values
     * @param idEmisor                semicolon-delimited IdEmisor values
     * @param idMoneda                semicolon-delimited IdMoneda values
     * @param idSerieFondo            semicolon-delimited IdSerieFondo values
     * @param nacional                semicolon-delimited Nacional values
     * @param tipoSolicitud           semicolon-delimited TipoSolicitud values
     * @param idTipoMovimientoFondoMutuo semicolon-delimited IdTipoMovimientoFondoMutuo values
     * @param moneyMarket             semicolon-delimited MoneyMarket values
     * @param idCarga                 semicolon-delimited IdCarga values
     * @return ordered list of filter records (one map per filter row)
     */
    public static List<Map<String, Object>> parseFiltros(
            String idInternoFiltro, String idEmisor, String idMoneda, String idSerieFondo,
            String nacional, String tipoSolicitud, String idTipoMovimientoFondoMutuo,
            String moneyMarket, String idCarga) {

        String[] filtros    = idInternoFiltro.split(";");
        String[] emisores   = idEmisor.split(";");
        String[] monedas    = idMoneda.split(";");
        String[] series     = idSerieFondo.split(";");
        String[] nacionales = nacional.split(";");
        String[] tipos      = tipoSolicitud.split(";");
        String[] movFM      = idTipoMovimientoFondoMutuo.split(";");
        String[] mm         = moneyMarket.split(";");
        String[] cargas     = idCarga.split(";");

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < filtros.length; i++) {
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("IdEmisor",                    safeGet(emisores, i));
            f.put("IdMoneda",                    safeGet(monedas, i));
            f.put("IdSerieFondo",                safeGet(series, i));
            f.put("Nacional",                    safeGet(nacionales, i));
            f.put("TipoSolicitud",               safeGet(tipos, i));
            f.put("IdTipoMovimientoFondoMutuo",  safeGet(movFM, i));
            f.put("MoneyMarket",                 safeGet(mm, i));
            f.put("IdCarga",                     Long.parseLong(safeGet(cargas, i)));
            result.add(f);
        }
        return result;
    }

    /**
     * Parses semicolon-delimited solicitud IDs into a list of Longs.
     * B-FMJ-05 support.
     */
    public static List<Long> parseIdSolicitudes(String idSolicitudes) {
        if (idSolicitudes == null || idSolicitudes.isBlank()) return List.of();
        return Arrays.stream(idSolicitudes.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private static String safeGet(String[] arr, int idx) {
        return idx < arr.length ? arr[idx] : "";
    }

    /**
     * Executes the certificacion masiva job.
     *
     * @param idEmpresa              company ID (required)
     * @param idUsuario              user ID (required)
     * @param simularOperacion       "S" or "N"
     * @param usaReglasNegocio       "S" or "N"
     * @param fechaCertificacion     certification date
     * @param esMasivo               "S" → use filter-based certification; else → individual IDs
     * @param idInternoFiltro        filter IDs (semicolon-delimited; used when esMasivo="S")
     * @param idEmisor               emitter filter values
     * @param idMoneda               currency filter values
     * @param idSerieFondo           fund series filter values
     * @param nacional               nacional filter values
     * @param tipoSolicitud          solicitud type filter values
     * @param idTipoMovimientoFondoMutuo movement type filter values
     * @param moneyMarket            money market filter values
     * @param idCarga                load ID filter values
     * @param idSolicitudes          individual solicitud IDs (used when esMasivo≠"S")
     * @return service result map
     */
    public Map<String, Object> ejecutar(
            Long idEmpresa, Long idUsuario,
            String simularOperacion, String usaReglasNegocio, String fechaCertificacion,
            String esMasivo,
            String idInternoFiltro, String idEmisor, String idMoneda, String idSerieFondo,
            String nacional, String tipoSolicitud, String idTipoMovimientoFondoMutuo,
            String moneyMarket, String idCarga,
            String idSolicitudes) {

        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");
        Objects.requireNonNull(esMasivo, "esMasivo is required");

        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);

        if ("S".equalsIgnoreCase(esMasivo)) {
            // B-FMJ-04: filter-based certification
            List<Map<String, Object>> filtros = parseFiltros(
                    idInternoFiltro, idEmisor, idMoneda, idSerieFondo,
                    nacional, tipoSolicitud, idTipoMovimientoFondoMutuo, moneyMarket, idCarga);

            // B-FMJ-07: empty → no-op
            if (filtros.isEmpty()) {
                log.info("*{}* Sin filtros para procesar", ID_TIPO_PROGRAMA);
                return Map.of("P_OUT_ERRORESCONTADOR", 0L);
            }

            Map<String, Object> resultado = servicioPort.certificarPorFiltros(
                    idEmpresa, idUsuario, "FMM", simularOperacion, usaReglasNegocio,
                    fechaCertificacion, filtros);
            log.info("*{}* Fin de Job (masivo)", ID_TIPO_PROGRAMA);
            return resultado;

        } else {
            // B-FMJ-05: individual solicitud certification
            List<Long> ids = parseIdSolicitudes(idSolicitudes);

            // B-FMJ-07: empty → no-op
            if (ids.isEmpty()) {
                log.info("*{}* Sin solicitudes para certificar", ID_TIPO_PROGRAMA);
                return Map.of("P_OUT_ERRORESCONTADOR", 0L);
            }

            Map<String, Object> resultado = servicioPort.certificarSolicitudes(
                    idEmpresa, idUsuario, "FMM", simularOperacion, usaReglasNegocio,
                    fechaCertificacion, ids);
            log.info("*{}* Fin de Job (individual)", ID_TIPO_PROGRAMA);
            return resultado;
        }
    }
}
