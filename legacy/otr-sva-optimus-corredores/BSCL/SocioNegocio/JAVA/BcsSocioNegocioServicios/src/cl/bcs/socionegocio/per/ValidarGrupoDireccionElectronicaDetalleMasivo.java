package cl.bcs.socionegocio.per;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica;
import cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica.Detalle;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidarGrupoDireccionElectronicaDetalleMasivo implements JavaCall {

    static final Logger logger = LogManager.getLogger(ValidarGrupoDireccionElectronicaDetalleMasivo.class);

    public final String SQL_SELECT_GRUPO_DIRECCION_ELECTRONICA_DETALLE_DIRECCION_ASIGNADA = loadResourceToString("cl/bcs/socionegocio/per/sql/SelectGrupoDireccionElectronicaDetalleMasivo_SelectGrupoDireccionElectronicaDetalleDireccionAsignada.sql");

    public static class EntradaValidarGrupoDireccionElectronicaDetalleMasivo extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;

        public List<Detalle> p_Detalles;

        public EntradaValidarGrupoDireccionElectronicaDetalleMasivo() {
            p_Detalles = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public List<Validacion> logica(Connection conn, EntradaValidarGrupoDireccionElectronicaDetalleMasivo entrada) throws Exception {
        logger.debug("INICIO::ValidarGrupoDireccionElectronicaDetalleMasivo");
        long inicio = System.currentTimeMillis();

        List<Validacion> validaciones = new ArrayList<>();

        validaciones.addAll(validarDireccionesDuplicadas(conn, entrada));
        validaciones.addAll(validarDireccionesAsignadas(conn, entrada));

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarGrupoDireccionElectronicaDetalleMasivo", fin - inicio);

        return validaciones;
    }

    private List<Validacion> validarDireccionesDuplicadas(Connection conn, EntradaValidarGrupoDireccionElectronicaDetalleMasivo entrada) throws Exception {
        logger.debug("INICIO::ValidarGrupoDireccionElectronicaDetalleMasivo.validarDireccionesDuplicadas");
        long inicio = System.currentTimeMillis();

        List<Validacion> validaciones = new ArrayList<>();

        Map<Long, List<Detalle>> detallesPorIdGrupo = entrada.p_Detalles.stream().collect(Collectors.groupingBy(detalle -> detalle.IdGrupoDireccion));
        Map<Long, Map<Long, Long>> repeticionesDireccionPorIdGrupo = new HashMap<>(detallesPorIdGrupo.size());

        detallesPorIdGrupo.forEach((idGrupo, detallesGrupo) -> {
            Map<Long, Long> repeticionesDireccionPorIdDireccion = detallesGrupo.stream().collect(Collectors.groupingBy(detalle -> detalle.IdDireccionElectronica, Collectors.counting()));
            repeticionesDireccionPorIdGrupo.put(idGrupo, repeticionesDireccionPorIdDireccion);
        });

        for (GrupoDireccionElectronica.Detalle detalle : entrada.p_Detalles) {
            boolean direccionDuplicadaEnGrupo = repeticionesDireccionPorIdGrupo.get(detalle.IdGrupoDireccion).get(detalle.IdDireccionElectronica) > 1;

            if (direccionDuplicadaEnGrupo) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                validacion.MensajeError = new MensajeSalida(detalle.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_DIRECCION_ELECTRONICA_DETALLE_ENTRANTE_DUPLICADA", new HashMap<>(), conn, entrada.p_IdEmpresa);

                validaciones.add(validacion);
            }
        }


        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarGrupoDireccionElectronicaDetalleMasivo.validarDireccionesDuplicadas", fin - inicio);

        return validaciones;
    }

    private List<Validacion> validarDireccionesAsignadas(Connection conn, EntradaValidarGrupoDireccionElectronicaDetalleMasivo entrada) throws Exception {
        logger.debug("INICIO::ValidarGrupoDireccionElectronicaDetalleMasivo.validarDireccionesAsignadas");
        long inicio = System.currentTimeMillis();

        List<Validacion> validaciones = new ArrayList<>();

        Set<Long> idsGrupoDireccion = new HashSet<>();
        Set<Long> idsDireccionElectronica = new HashSet<>();

        for (GrupoDireccionElectronica.Detalle detalle : entrada.p_Detalles) {
            idsGrupoDireccion.add(detalle.IdGrupoDireccion);
            idsDireccionElectronica.add(detalle.IdDireccionElectronica);
        }

        HashMap<String, Object> parametros = new HashMap<>(4);
        parametros.put("GrupoDireccionIds", new ArrayList<>(idsGrupoDireccion));
        parametros.put("DireccionElectronicaIds", new ArrayList<>(idsDireccionElectronica));

        SQL sql = new SQL();
        List<GrupoDireccionElectronica.Detalle> detallesDireccionesAsignadas = sql.query(conn, SQL_SELECT_GRUPO_DIRECCION_ELECTRONICA_DETALLE_DIRECCION_ASIGNADA, parametros, GrupoDireccionElectronica.Detalle.class);

        Map<Long, Set<Long>> idsDireccionAsignadasPorIdGrupo = detallesDireccionesAsignadas.stream().collect(Collectors.groupingBy(
                detalle -> detalle.IdGrupoDireccion,
                Collectors.mapping(detalle -> detalle.IdDireccionElectronica, Collectors.toSet())
        ));

        for (GrupoDireccionElectronica.Detalle detalle : entrada.p_Detalles) {
            Set<Long> idsDireccionEnGrupo = idsDireccionAsignadasPorIdGrupo.get(detalle.IdGrupoDireccion);
            boolean direccionEstaAsignada = idsDireccionEnGrupo != null && idsDireccionEnGrupo.contains(detalle.IdDireccionElectronica);

            if (direccionEstaAsignada) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                validacion.MensajeError = new MensajeSalida(detalle.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_DIRECCION_ELECTRONICA_DETALLE_EXISTE", new HashMap<>(), conn, entrada.p_IdEmpresa);

                validaciones.add(validacion);
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarGrupoDireccionElectronicaDetalleMasivo.validarDireccionesAsignadas", fin - inicio);

        return validaciones;
    }
}
