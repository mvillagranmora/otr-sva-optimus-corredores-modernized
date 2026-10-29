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

public class ModificarBorradorGrupoDireccion implements JavaCall {

    static final Logger logger = LogManager.getLogger(ModificarBorradorDireccionFisica.class);

    private final String SQL_UPDATE_GRUPO_DIRECCION = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorGrupoDireccion_UpdateGrupoDireccion.sql");

    public static class EntradaModificarBorradorGrupoDireccion extends EntradaAdministrarBorrador {
        public List<GrupoDireccion> p_GruposDirecciones;

        public EntradaModificarBorradorGrupoDireccion() {
            this.p_GruposDirecciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaModificarBorradorGrupoDireccion jin = gson.fromJson(in.toString(), EntradaModificarBorradorGrupoDireccion.class);
        SalidaAdministrarBorrador out = logica(conn, jin);

        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

        return dataout;
    }

    public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorGrupoDireccion entrada) throws Exception {
        logger.info("INICIO::ModificarBorradorGrupoDireccion");
        long inicioServicio = System.currentTimeMillis();
        SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

        out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_GruposDirecciones, GrupoDireccion.class, out);
        out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_GruposDirecciones, "GrupoDireccion", out);

        if (out.P_OUT_ERRORESCONTADOR > 0) {
            return out;
        }

        ValidarExistenciaBorradorGrupoDireccion validarExistenciaBorradorGrupoDireccion = new ValidarExistenciaBorradorGrupoDireccion();
        EntradaValidarExistenciaBorradorGrupoDireccion entradaValidarExistenciaBorradorGrupoDireccion = new EntradaValidarExistenciaBorradorGrupoDireccion();
        entradaValidarExistenciaBorradorGrupoDireccion.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaValidarExistenciaBorradorGrupoDireccion.p_GruposDirecciones = entrada.p_GruposDirecciones;

        List<Validacion> validaciones = validarExistenciaBorradorGrupoDireccion.logica(con, entradaValidarExistenciaBorradorGrupoDireccion).validaciones;
        out.agregarValidacionesSalida(validaciones);

        if (out.P_OUT_ERRORESCONTADOR > 0) {
            return out;
        }

        actualizarGrupoDirecciones(con, entrada.p_GruposDirecciones);

        out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_GruposDirecciones);

        long finServicio = System.currentTimeMillis();
        logger.info("FIN::ModificarBorradorGrupoDireccion", finServicio - inicioServicio);

        return out;
    }

    private void actualizarGrupoDirecciones(Connection con, List<GrupoDireccion> gruposDirecciones) throws Exception {
        logger.debug("INICIO::ModificarBorradorGrupoDireccion.actualizarGrupoDirecciones");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(con, SQL_UPDATE_GRUPO_DIRECCION, gruposDirecciones, GrupoDireccion.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ModificarBorradorGrupoDireccion.actualizarGrupoDirecciones", fin - inicio);
    }

    private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<GrupoDireccion> gruposDirecciones) {
        logger.trace("INICIO::ModificarBorradorGrupoDireccion.obtenerIdsActualizados");
        long inicio = System.currentTimeMillis();

        List<ResultadoAdministrarBorradorEntidad> idsInsertados = gruposDirecciones
                .parallelStream()
                .map(grupoDireccion -> {
                    ResultadoAdministrarBorradorEntidad resultado = new ResultadoAdministrarBorradorEntidad();
                    resultado.IdInterno = grupoDireccion.IdInterno;
                    resultado.IdEntidad = grupoDireccion.IdGrupoDireccion;

                    return resultado;
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::ModificarBorradorGrupoDireccion.obtenerIdsActualizados", fin - inicio);

        return idsInsertados;
    }

    public static void main(String[] args) {
        try (Connection con = PlataformaUtils.getDESAConnection()) {
            String direccionesExistentesJsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":1,\"p_UsaReglasNegocio\":1,\"p_CodigoFormulario\":1,\"p_IdTipoCanal\":1,\"p_GruposDirecciones\":[{\"IdEnrolamiento\":1,\"IdGrupoDireccion\":6,\"IdInterno\":1,\"Grupo\":\"Grupo de prueba MODIFICAR main\",\"Comentario\":\"Prueba MODIFICAR main\"},{\"IdEnrolamiento\":2,\"IdGrupoDireccion\":5,\"IdInterno\":2,\"Grupo\":\"Grupo 2 de prueba MODIFICAR main\",\"Comentario\":\"Prueba 2 main\"}]}";

            Gson gson = Utils.getGsonBuilder();
            EntradaModificarBorradorGrupoDireccion entrada = gson.fromJson(direccionesExistentesJsonRequest, EntradaModificarBorradorGrupoDireccion.class);

            ModificarBorradorGrupoDireccion modificarBorradorGrupoDireccion = new ModificarBorradorGrupoDireccion();
            try {
                SalidaAdministrarBorrador salida = modificarBorradorGrupoDireccion.logica(con, entrada);

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
