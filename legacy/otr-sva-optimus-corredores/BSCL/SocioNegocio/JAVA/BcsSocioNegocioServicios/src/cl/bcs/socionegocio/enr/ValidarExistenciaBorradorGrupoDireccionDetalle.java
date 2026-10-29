package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.GrupoDireccionDetalle;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidarExistenciaBorradorGrupoDireccionDetalle implements JavaCall {
    static final Logger logger = LogManager.getLogger(ValidarExistenciaBorradorGrupoDireccionDetalle.class);

    private final String SQL_SELECT_GRUPO_DIRECCION_DETALLE = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarExistenciaBorradorGrupoDireccionDetalle_SelectGrupoDireccionDetalle.sql");

    public static class EntradaValidarExistenciaBorradorGrupoDireccionDetalle extends BcsPojo {
        public Long p_IdEmpresa;
        public List<GrupoDireccionDetalle> p_GrupoDireccionDetalles;

        public EntradaValidarExistenciaBorradorGrupoDireccionDetalle() {
            this.p_GrupoDireccionDetalles = new ArrayList<>();
        }
    }

    public static class SalidaValidarExistenciaBorradorGrupoDireccionDetalle extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaBorradorGrupoDireccionDetalle() {
            this.validaciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaValidarExistenciaBorradorGrupoDireccionDetalle logica(Connection con, EntradaValidarExistenciaBorradorGrupoDireccionDetalle entrada) throws Exception {
        logger.info("INICIO::ValidarExistenciaBorradorGrupoDireccionDetalle");
        long inicio = System.currentTimeMillis();

        SalidaValidarExistenciaBorradorGrupoDireccionDetalle out = new SalidaValidarExistenciaBorradorGrupoDireccionDetalle();
        out.validaciones = validarExistenciaGrupoDireccionDetalles(con, entrada.p_GrupoDireccionDetalles, entrada.p_IdEmpresa);

        long fin = System.currentTimeMillis();
        logger.info("FIN::ValidarExistenciaBorradorGrupoDireccionDetalle", fin - inicio);

        return out;
    }

    private List<Validacion> validarExistenciaGrupoDireccionDetalles(Connection con, List<GrupoDireccionDetalle> grupoDireccionDetalles, Long idEmpresa) throws Exception {
        logger.debug("INICIO::ValidarExistenciaBorradorGrupoDireccionDetalle.validarExistenciaGrupoDireccionDetalles");
        long inicio = System.currentTimeMillis();

        List<Long> idsGruposDirecciones = grupoDireccionDetalles
                .parallelStream()
                .map(grupoDireccion -> grupoDireccion.IdGrupoDireccionDetalle)
                .collect(Collectors.toList());

        HashMap<String, Object> parametros = new HashMap<>(1);
        parametros.put("IdsGrupoDireccionDetalles", idsGruposDirecciones);

        SQL sql = new SQL();
        List<GrupoDireccionDetalle> grupoDireccionDetallesExistentes = sql.query(con, SQL_SELECT_GRUPO_DIRECCION_DETALLE, parametros, GrupoDireccionDetalle.class);
        Map<Long, GrupoDireccionDetalle> grupoDireccionDetallesPorId = grupoDireccionDetallesExistentes
                .parallelStream()
                .collect(Collectors.toMap(detalle -> detalle.IdGrupoDireccionDetalle, detalle -> detalle));

        List<Validacion> validaciones = new ArrayList<>();

        for (GrupoDireccionDetalle detalle : grupoDireccionDetalles) {
            if (grupoDireccionDetallesPorId.get(detalle.IdGrupoDireccionDetalle) == null) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;

                HashMap<String, String> datosMensajes = new HashMap<>(1);
                datosMensajes.put("GrupoDireccionDetalle", "ID: " + detalle.IdGrupoDireccionDetalle);

                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_GRUPO_DIRECCION_DETALLE_NO_EXISTE", datosMensajes, con, idEmpresa);
                validaciones.add(validacion);
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarExistenciaBorradorGrupoDireccionDetalle.validarExistenciaGrupoDireccionDetalles", fin - inicio);

        return validaciones;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":1,\"p_UsaReglasNegocio\":1,\"p_CodigoFormulario\":1,\"p_IdTipoCanal\":1,\"p_GrupoDireccionDetalles\":[{\"IdGrupoDireccionDetalle\":4},{\"IdGrupoDireccionDetalle\":-2}]}";

        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaValidarExistenciaBorradorGrupoDireccionDetalle entrada = gson.fromJson(jsonEntrada, EntradaValidarExistenciaBorradorGrupoDireccionDetalle.class);

            ValidarExistenciaBorradorGrupoDireccionDetalle validador = new ValidarExistenciaBorradorGrupoDireccionDetalle();
            try {
                SalidaValidarExistenciaBorradorGrupoDireccionDetalle salida = validador.logica(con, entrada);
                System.out.println(salida);
            } catch (Exception innerException) {
                innerException.printStackTrace();
                con.rollback();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
