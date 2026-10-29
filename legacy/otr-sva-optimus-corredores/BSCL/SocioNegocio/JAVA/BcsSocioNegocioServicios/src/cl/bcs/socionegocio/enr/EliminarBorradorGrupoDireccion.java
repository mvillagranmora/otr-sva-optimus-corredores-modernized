package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.GrupoDireccion;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorGrupoDireccion.EntradaValidarExistenciaBorradorGrupoDireccion;
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

public class EliminarBorradorGrupoDireccion implements JavaCall {

    static final Logger logger = LogManager.getLogger(EliminarBorradorGrupoDireccion.class);

    private final String SQL_DELETE_GRUPO_DIRECCION = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorGrupoDireccion_DeleteGrupoDireccion.sql");
    private final String SQL_DELETE_GRUPO_DIRECCION_DETALLE_POR_ID_GRUPO_DIRECCION = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorGrupoDireccion_DeleteGrupoDireccionDetalleByGrupoDireccion.sql");

    public static class EntradaEliminarBorradorGrupoDireccion extends EntradaAdministrarBorrador {
        public List<GrupoDireccion> p_GruposDirecciones;

        public EntradaEliminarBorradorGrupoDireccion() {
            this.p_GruposDirecciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaEliminarBorradorGrupoDireccion jin = gson.fromJson(in.toString(), EntradaEliminarBorradorGrupoDireccion.class);
        SalidaAdministrarBorrador out = logica(conn, jin);

        JsonParser parser = new JsonParser();

        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    public SalidaAdministrarBorrador logica(Connection con, EntradaEliminarBorradorGrupoDireccion entrada) throws Exception {
        logger.info("INICIO::EliminarBorradorGrupoDireccion");
        long inicioServicio = System.currentTimeMillis();
        SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

        ValidarExistenciaBorradorGrupoDireccion validarExistenciaBorradorGrupoDireccion = new ValidarExistenciaBorradorGrupoDireccion();
        EntradaValidarExistenciaBorradorGrupoDireccion entradaValidarExistenciaBorradorGrupoDireccion = new EntradaValidarExistenciaBorradorGrupoDireccion();
        entradaValidarExistenciaBorradorGrupoDireccion.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaValidarExistenciaBorradorGrupoDireccion.p_GruposDirecciones = entrada.p_GruposDirecciones;

        List<Validacion> validaciones = validarExistenciaBorradorGrupoDireccion.logica(con, entradaValidarExistenciaBorradorGrupoDireccion).validaciones;
        out.agregarValidacionesSalida(validaciones);

        if (out.P_OUT_ERRORESCONTADOR > 0) {
            return out;
        }

        eliminarGruposDirecciones(con, entrada.p_GruposDirecciones);

        out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_GruposDirecciones);

        long finServicio = System.currentTimeMillis();
        logger.info("FIN::EliminarBorradorGrupoDireccion", finServicio - inicioServicio);

        return out;
    }

    private void eliminarGruposDirecciones(Connection con, List<GrupoDireccion> gruposDirecciones) throws Exception {
        logger.debug("INICIO::EliminarBorradorGrupoDireccion.eliminarGruposDirecciones");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();

        // Delete references to GruposDirecciones being removed
        sql.batch(con, SQL_DELETE_GRUPO_DIRECCION_DETALLE_POR_ID_GRUPO_DIRECCION, gruposDirecciones, GrupoDireccion.class);

        // Delete actual GruposDirecciones
        sql.batch(con, SQL_DELETE_GRUPO_DIRECCION, gruposDirecciones, GrupoDireccion.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::EliminarBorradorGrupoDireccion.eliminarGruposDirecciones", fin - inicio);
    }

    private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<GrupoDireccion> detalles) {
        logger.trace("INICIO::EliminarBorradorGrupoDireccion.obtenerIdsActualizados");
        long inicio = System.currentTimeMillis();

        List<ResultadoAdministrarBorradorEntidad> idsInsertados = detalles
                .parallelStream()
                .map(grupoDireccion -> {
                    ResultadoAdministrarBorradorEntidad resultado = new ResultadoAdministrarBorradorEntidad();
                    resultado.IdInterno = grupoDireccion.IdInterno;
                    resultado.IdEntidad = grupoDireccion.IdGrupoDireccion;

                    return resultado;
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::EliminarBorradorGrupoDireccion.obtenerIdsActualizados", fin - inicio);

        return idsInsertados;
    }

    public static void main(String[] args) {
        String gruposDireccionesExistentesJson = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":1,\"p_UsaReglasNegocio\":1,\"p_CodigoFormulario\":1,\"p_IdTipoCanal\":1,\"p_GruposDirecciones\":[{\"IdGrupoDireccion\":10}]}";

        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaEliminarBorradorGrupoDireccion entrada = gson.fromJson(gruposDireccionesExistentesJson, EntradaEliminarBorradorGrupoDireccion.class);

            EliminarBorradorGrupoDireccion EliminarBorradorGrupoDireccionDetalle = new EliminarBorradorGrupoDireccion();
            try {
                SalidaAdministrarBorrador salida = EliminarBorradorGrupoDireccionDetalle.logica(con, entrada);
                con.commit();

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
