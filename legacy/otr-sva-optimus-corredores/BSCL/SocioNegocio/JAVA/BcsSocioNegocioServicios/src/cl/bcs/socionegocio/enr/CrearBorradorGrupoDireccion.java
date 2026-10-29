package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.GrupoDireccion;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CrearBorradorGrupoDireccion implements JavaCall {

    static final Logger logger = LogManager.getLogger(CrearBorradorGrupoDireccion.class);

    private final String SQL_INSERT_GRUPO_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorGrupoDireccion_InsertGrupoDireccion.sql");

    public static class EntradaCrearBorradorGrupoDireccion extends EntradaAdministrarBorrador {
        public List<GrupoDireccion> p_GrupoDirecciones;

        public EntradaCrearBorradorGrupoDireccion() {
            this.p_GrupoDirecciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaCrearBorradorGrupoDireccion jin = gson.fromJson(
                in.toString(),
                EntradaCrearBorradorGrupoDireccion.class
        );
        SalidaAdministrarBorrador out = logica(conn, jin);

        JsonParser parser = new JsonParser();
        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    public SalidaAdministrarBorrador logica(Connection con,
                                            EntradaCrearBorradorGrupoDireccion entrada) throws Exception {
        logger.info("INICIO::CrearBorradorGrupoDireccion");
        long inicioServicio = System.currentTimeMillis();

        SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

        out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_GrupoDirecciones, GrupoDireccion.class, out);
        out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_GrupoDirecciones, "GrupoDireccion", out);

        if (out.P_OUT_ERRORESCONTADOR > 0) {
            return out;
        }

        List<GrupoDireccion> gruposDirecciones = obtenerGruposDirecciones(con, entrada.p_GrupoDirecciones);

        insertarGruposDirecciones(con, gruposDirecciones);

        out.P_OUT_REFID = obtenerIdsInsertados(gruposDirecciones);

        long finServicio = System.currentTimeMillis();
        logger.info("FIN::CrearBorradorGrupoDireccion", finServicio - inicioServicio);

        return out;
    }

    private List<GrupoDireccion> obtenerGruposDirecciones(Connection con,
                                                          List<GrupoDireccion> borradoresGruposDirecciones) throws SQLException {
        logger.trace("INICIO::CrearBorradorGrupoDireccion.obtenerGruposDirecciones");
        long inicio = System.currentTimeMillis();

        List<Long> secuencia = Utils.getSecuencia(con, "BSCL.SocioNegocios.BD.Secuencias::BorradorGrupoDireccion", Long.valueOf(borradoresGruposDirecciones.size()));

        AtomicInteger index = new AtomicInteger();
        List<GrupoDireccion> gruposDirecciones = borradoresGruposDirecciones
                .parallelStream()
                .map(borradorGrupoDireccion -> {
                    Long idGrupoDireccion = secuencia.get(index.getAndIncrement());

                    return obtenerEntidadGrupoDireccion(borradorGrupoDireccion, idGrupoDireccion);
                })
                .collect(Collectors.toList());
        ;

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearBorradorGrupoDireccion.obtenerGruposDirecciones", fin - inicio);

        return gruposDirecciones;
    }

    private GrupoDireccion obtenerEntidadGrupoDireccion(GrupoDireccion borradorGrupoDireccion, Long idGrupoDireccion) {
        logger.info("INICIO::CrearBorradorGrupoDireccion.obtenerEntidadGrupoDireccion");
        long inicio = System.currentTimeMillis();

        GrupoDireccion grupoDireccion = new GrupoDireccion();
        grupoDireccion.IdGrupoDireccion = idGrupoDireccion;
        grupoDireccion.IdEnrolamiento = borradorGrupoDireccion.IdEnrolamiento;
        grupoDireccion.IdInterno = borradorGrupoDireccion.IdInterno;
        grupoDireccion.Grupo = borradorGrupoDireccion.Grupo;
        grupoDireccion.Comentario = borradorGrupoDireccion.Comentario;
        grupoDireccion.IdGrupoDireccionPersona = borradorGrupoDireccion.IdGrupoDireccionPersona;
        
        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearBorradorGrupoDireccion.obtenerEntidadGrupoDireccion", fin - inicio);

        return grupoDireccion;
    }

    private void insertarGruposDirecciones(Connection con, List<GrupoDireccion> gruposDirecciones) throws SQLException {
        logger.debug("INICIO::CrearBorradorGrupoDireccion.insertarGruposDirecciones");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(con, SQL_INSERT_GRUPO_DIRECCION_ELECTRONICA, gruposDirecciones, GrupoDireccion.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::CrearBorradorGrupoDireccion.insertarGruposDirecciones", fin - inicio);
    }

    private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<GrupoDireccion> gruposDirecciones) {
        logger.trace("INICIO::CrearBorradorGrupoDireccion.obtenerIdsInsertados");
        long inicio = System.currentTimeMillis();

        List<ResultadoAdministrarBorradorEntidad> idsInsertados = gruposDirecciones
                .parallelStream()
                .map(grupoDireccion -> {
                    ResultadoAdministrarBorradorEntidad resultado = new ResultadoAdministrarBorradorEntidad();
                    resultado.IdEntidad = grupoDireccion.IdGrupoDireccion;
                    resultado.IdInterno = grupoDireccion.IdInterno;

                    return resultado;
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearBorradorGrupoDireccion.obtenerIdsInsertados", fin - inicio);

        return idsInsertados;
    }

    public static void main(String[] args) {
        try (Connection con = PlataformaUtils.getDESAConnection()) {
            String gruposDireccionesJson = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":1,\"p_UsaReglasNegocio\":1,\"p_CodigoFormulario\":1,\"p_IdTipoCanal\":1,\"p_GrupoDirecciones\":[{\"IdEnrolamiento\":1,\"IdGrupoDireccion\":1,\"IdInterno\":1,\"Grupo\":\"Grupo de prueba main\",\"Comentario\":\"Prueba main\"},{\"IdEnrolamiento\":2,\"IdGrupoDireccion\":2,\"IdInterno\":2,\"Grupo\":\"Grupo 2 de prueba main\",\"Comentario\":\"Prueba 2 main\"}]}";

            Gson gson = Utils.getGsonBuilder();
            EntradaCrearBorradorGrupoDireccion entrada = gson.fromJson(gruposDireccionesJson, EntradaCrearBorradorGrupoDireccion.class);

            CrearBorradorGrupoDireccion crearBorradorGrupoDireccion = new CrearBorradorGrupoDireccion();

            try {
                SalidaAdministrarBorrador salida = crearBorradorGrupoDireccion.logica(con, entrada);
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
