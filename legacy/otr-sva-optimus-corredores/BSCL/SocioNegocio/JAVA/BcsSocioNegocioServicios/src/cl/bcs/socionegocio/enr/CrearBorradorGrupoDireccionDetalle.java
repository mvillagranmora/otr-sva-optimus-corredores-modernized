package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.GrupoDireccionDetalle;
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

public class CrearBorradorGrupoDireccionDetalle implements JavaCall {
    static final Logger logger = LogManager.getLogger(CrearBorradorGrupoDireccionDetalle.class);

    private final String SQL_INSERT_GRUPO_DIRECCION_DETALLE = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorGrupoDireccionDetalle_InsertGrupoDireccionDetalle.sql");

    public static class EntradaCrearBorradorGrupoDireccionDetalle extends EntradaAdministrarBorrador {
        public List<GrupoDireccionDetalle> p_GrupoDireccionDetalles;

        public EntradaCrearBorradorGrupoDireccionDetalle() {
            this.p_GrupoDireccionDetalles = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson                                      gson = Utils.getGsonBuilder();
        EntradaCrearBorradorGrupoDireccionDetalle jin  = gson.fromJson(in.toString(), EntradaCrearBorradorGrupoDireccionDetalle.class);

        SalidaAdministrarBorrador out = logica(conn, jin);

        JsonParser parser = new JsonParser();

        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    public SalidaAdministrarBorrador logica(Connection conn, EntradaCrearBorradorGrupoDireccionDetalle jin) throws Exception {
        logger.info("INICIO::CrearBorradorGrupoDireccionDetalle");
        long inicio = System.currentTimeMillis();

        SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

        out = jin.validarCamposObligatoriosEntrada(conn, jin.p_GrupoDireccionDetalles, GrupoDireccionDetalle.class, out);
        out = jin.validarGrillasObligatoriasEntrada(conn, jin.p_GrupoDireccionDetalles, "GrupoDireccionDetalle", out);

        if (out.P_OUT_ERRORESCONTADOR > 0) {
            return out;
        }

        List<GrupoDireccionDetalle> grupoDireccionDetalles = obtenerGrupoDireccionDetalles(conn, jin.p_GrupoDireccionDetalles);

        insertarGrupoDireccionDetalles(conn, grupoDireccionDetalles);

        out.P_OUT_REFID = obtenerIdsInsertados(grupoDireccionDetalles);

        long fin = System.currentTimeMillis();
        logger.info("FIN::CrearBorradorGrupoDireccionDetalle", fin - inicio);

        return out;
    }

    private List<GrupoDireccionDetalle> obtenerGrupoDireccionDetalles(Connection conn,
                                                                      List<GrupoDireccionDetalle> p_grupoDireccionDetalles) throws SQLException {
        logger.trace("CrearBorradorGrupoDireccionDetalle.obtenerGrupoDireccionDetalles");
        long inicio = System.currentTimeMillis();

        List<Long> secuencia = Utils.getSecuencia(
                conn,
                "BSCL.SocioNegocios.BD.Secuencias::BorradorGrupoDireccionDetalle",
                Long.valueOf(p_grupoDireccionDetalles.size())
        );

        AtomicInteger index = new AtomicInteger();
        List<GrupoDireccionDetalle> gruposDireccionDetalles = p_grupoDireccionDetalles
                .parallelStream()
                .map(borrador -> {
                    Long idGrupoDireccionDetalle = secuencia.get(index.getAndIncrement());

                    return obtenerEntidadGrupoDireccionDetalle(borrador, idGrupoDireccionDetalle);
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("CrearBorradorGrupoDireccionDetalle.obtenerGrupoDireccionDetalles", fin - inicio);

        return gruposDireccionDetalles;
    }

    private GrupoDireccionDetalle obtenerEntidadGrupoDireccionDetalle(GrupoDireccionDetalle borrador, Long idGrupoDireccionDetalle) {
        GrupoDireccionDetalle detalle = new GrupoDireccionDetalle();
        detalle.IdGrupoDireccionDetalle = idGrupoDireccionDetalle;
        detalle.IdGrupoDireccion        = borrador.IdGrupoDireccion;
        detalle.IdDireccionElectronica  = borrador.IdDireccionElectronica;
        detalle.IdTipoDestinatario      = borrador.IdTipoDestinatario;
        detalle.IdGrupoDireccionPersona = borrador.IdGrupoDireccionPersona;
        detalle.IdGrupoDireccionDetallePersona = borrador.IdGrupoDireccionDetallePersona;
        return detalle;
    }

    private void insertarGrupoDireccionDetalles(Connection conn, List<GrupoDireccionDetalle> grupoDireccionDetalles) throws SQLException {
        logger.debug("INICIO::CrearBorradorGrupoDireccionDetalle.insertarGrupoDireccionDetalles");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_INSERT_GRUPO_DIRECCION_DETALLE, grupoDireccionDetalles, GrupoDireccionDetalle.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::CrearBorradorGrupoDireccionDetalle.insertarGrupoDireccionDetalles", fin - inicio);
    }

    private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<GrupoDireccionDetalle> grupoDireccionDetalles) {
        logger.trace("INICIO::CrearBorradorGrupoDireccionDetalle.obtenerIdsInsertados");
        long inicio = System.currentTimeMillis();

        List<ResultadoAdministrarBorradorEntidad> idsInsertados = grupoDireccionDetalles
                .parallelStream()
                .map(detalle -> {
                    ResultadoAdministrarBorradorEntidad resultado = new ResultadoAdministrarBorradorEntidad();
                    resultado.IdEntidad = detalle.IdGrupoDireccionDetalle;
                    resultado.IdInterno = detalle.IdInterno;

                    return resultado;
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearBorradorGrupoDireccionDetalle.obtenerIdsInsertados", fin - inicio);

        return idsInsertados;
    }

    public static void main(String[] args) {
        String successfulJson = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_CodigoFormulario\":\"\",\"p_IdTipoCanal\":\"OPT\",\"p_GrupoDireccionDetalles\":[{\"IdDireccionElectronica\":74,\"IdTipoDestinatario\":\"PARA\",\"IdGrupoDireccion\":36}]}";

        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson                                      gson    = Utils.getGsonBuilder();
            EntradaCrearBorradorGrupoDireccionDetalle entrada = gson.fromJson(successfulJson, EntradaCrearBorradorGrupoDireccionDetalle.class);

            CrearBorradorGrupoDireccionDetalle crearBorradorGrupoDireccionDetalle = new CrearBorradorGrupoDireccionDetalle();

            try {
                SalidaAdministrarBorrador salida = crearBorradorGrupoDireccionDetalle.logica(conn, entrada);

                System.out.println(salida);
                System.out.println("Fin metodo main");
            } catch (Exception innerException) {
                innerException.printStackTrace();
                conn.rollback();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
