package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorGrupoDireccionDetalle.EntradaValidarExistenciaBorradorGrupoDireccionDetalle;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.GrupoDireccionDetalle;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EliminarBorradorGrupoDireccionDetalle implements JavaCall {

    static final Logger logger = LogManager.getLogger(EliminarBorradorGrupoDireccionDetalle.class);

    private final String SQL_DELETE_GRUPO_DIRECCION_DETALLE = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorGrupoDireccionDetalle_DeleteGrupoDireccionDetalle.sql");

    public static class EntradaEliminarBorradorGrupoDireccionDetalle extends EntradaAdministrarBorrador {
        public List<GrupoDireccionDetalle> p_GrupoDireccionDetalles;

        public EntradaEliminarBorradorGrupoDireccionDetalle() {
            this.p_GrupoDireccionDetalles = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaEliminarBorradorGrupoDireccionDetalle jin = gson.fromJson(in.toString(), EntradaEliminarBorradorGrupoDireccionDetalle.class);
        SalidaAdministrarBorrador out = logica(conn, jin);

        JsonParser parser = new JsonParser();

        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    public SalidaAdministrarBorrador logica(Connection con, EntradaEliminarBorradorGrupoDireccionDetalle entrada) throws Exception {
        logger.info("INICIO::EliminarBorradorGrupoDireccionDetalle");
        long inicioServicio = System.currentTimeMillis();
        SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

        ValidarExistenciaBorradorGrupoDireccionDetalle validarExistenciaBorradorGrupoDireccionDetalle = new ValidarExistenciaBorradorGrupoDireccionDetalle();
        EntradaValidarExistenciaBorradorGrupoDireccionDetalle entradaValidarExistenciaBorradorGrupoDireccionDetalle = new EntradaValidarExistenciaBorradorGrupoDireccionDetalle();
        entradaValidarExistenciaBorradorGrupoDireccionDetalle.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaValidarExistenciaBorradorGrupoDireccionDetalle.p_GrupoDireccionDetalles = entrada.p_GrupoDireccionDetalles;

        List<Validacion> validaciones = validarExistenciaBorradorGrupoDireccionDetalle.logica(con, entradaValidarExistenciaBorradorGrupoDireccionDetalle).validaciones;
        out.agregarValidacionesSalida(validaciones);

        if (out.P_OUT_ERRORESCONTADOR > 0) {
            return out;
        }

        eliminarGrupoDireccionDetalles(con, entrada.p_GrupoDireccionDetalles);

        out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_GrupoDireccionDetalles);

        long finServicio = System.currentTimeMillis();
        logger.info("FIN::EliminarBorradorGrupoDireccionDetalle", finServicio - inicioServicio);

        return out;
    }

    private void eliminarGrupoDireccionDetalles(Connection con, List<GrupoDireccionDetalle> detalles) throws Exception {
        logger.debug("INICIO::EliminarBorradorGrupoDireccionDetalle.eliminarGrupoDireccionDetalles");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(con, SQL_DELETE_GRUPO_DIRECCION_DETALLE, detalles, GrupoDireccionDetalle.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::EliminarBorradorGrupoDireccionDetalle.eliminarGrupoDireccionDetalles", fin - inicio);
    }

    private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<GrupoDireccionDetalle> detalles) {
        logger.trace("INICIO::EliminarBorradorGrupoDireccionDetalle.obtenerIdsActualizados");
        long inicio = System.currentTimeMillis();

        List<ResultadoAdministrarBorradorEntidad> idsInsertados = detalles
                .parallelStream()
                .map(detalle -> {
                    ResultadoAdministrarBorradorEntidad resultado = new ResultadoAdministrarBorradorEntidad();
                    resultado.IdInterno = detalle.IdInterno;
                    resultado.IdEntidad = detalle.IdGrupoDireccionDetalle;

                    return resultado;
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::EliminarBorradorGrupoDireccionDetalle.obtenerIdsActualizados", fin - inicio);

        return idsInsertados;
    }

    public static void main(String[] args) {
        String detallesExistentesJson = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":1,\"p_UsaReglasNegocio\":1,\"p_CodigoFormulario\":1,\"p_IdTipoCanal\":1,\"p_GrupoDireccionDetalles\":[{\"IdGrupoDireccionDetalle\":11},{\"IdGrupoDireccionDetalle\":10}]}";

        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaEliminarBorradorGrupoDireccionDetalle entrada = gson.fromJson(detallesExistentesJson, EntradaEliminarBorradorGrupoDireccionDetalle.class);

            EliminarBorradorGrupoDireccionDetalle EliminarBorradorGrupoDireccionDetalle = new EliminarBorradorGrupoDireccionDetalle();
            try {
                SalidaAdministrarBorrador salida = EliminarBorradorGrupoDireccionDetalle.logica(con, entrada);

                System.out.println(salida);
                System.out.println("Fin metodo main");
            } catch (Exception innerException) {
                innerException.printStackTrace();
                con.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
