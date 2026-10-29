package cl.bcs.plataforma.enc;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BorradorResponderEncuesta implements JavaCall {
    static final Logger logger = LogManager.getLogger(BorradorResponderEncuesta.class);

    private final String SQL_SELECT_DATOS_INVERSIONISTA = loadResourceToString("cl/bcs/plataforma/enc/sql/BorradorResponderEncuesta_SelectDatosInversionista.sql");
    private final String SQL_UPDATE_CATEGORIA_INVERSIONISTA = loadResourceToString("cl/bcs/plataforma/enc/sql/BorradorResponderEncuesta_UpdateCategoriaInversionista.sql");
    private final String SQL_UPDATE_RESPONDIO_ENCUESTA = loadResourceToString("cl/bcs/plataforma/enc/sql/BorradorResponderEncuesta_UpdateRespondioEncuesta.sql");

    public static class DatosInversionista extends BcsPojo {
        public Long IdEnrolamiento;
        public String IdCategoriaInversionista;
        public String IdPerfilComercial;
    }

    public static class EntradaBorradorResponderEncuesta extends BcsPojo {
        public String p_IdModulo;
        public String p_IdCanal;
        public Long p_IdUsuario;
        public Long p_IdEmpresa;
        public Long p_IdDefinicionEncuesta;
        public Long p_IdEnrolamiento;
        public String p_RespondeEncuesta;
        public List<ResponderEncuesta.RespuestaEncuesta_Entrada> p_Respuestas;

        public EntradaBorradorResponderEncuesta() {
            p_Respuestas = new ArrayList<>();
        }
    }

    public static class SalidaBorradorResponderEncuesta extends BcsPojo {
        public String IdCategoriaInversionista;
        public String IdPerfilComercial;
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;

        public SalidaBorradorResponderEncuesta() {
            P_OUT_MENSAJES = new ArrayList<>();
            P_OUT_ERRORESCONTADOR = 0L;
        }

        public void agregarValidaciones(List<Validacion> validacionesActualizar) {
            for (Validacion validacion : validacionesActualizar) {
                agregarValidacion(validacion);
            }
        }

        private void agregarValidacion(Validacion validacion) {
            if (validacion.EsError) {
                P_OUT_ERRORESCONTADOR += 1L;
                P_OUT_MENSAJES.add(validacion.MensajeError);
            }
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaBorradorResponderEncuesta entrada = gson.fromJson(in, EntradaBorradorResponderEncuesta.class);
        SalidaBorradorResponderEncuesta salida = logica(conn, entrada);
        JsonParser jsonParser = new JsonParser();

        return jsonParser.parse(gson.toJson(salida)).getAsJsonObject();
    }

    private SalidaBorradorResponderEncuesta logica(Connection conn, EntradaBorradorResponderEncuesta entrada) throws Exception {
        logger.info("INICIO::BorradorResponderEncuesta");
        long inicio = System.currentTimeMillis();

        SalidaBorradorResponderEncuesta salida = new SalidaBorradorResponderEncuesta();

        boolean respondeEncuesta = "S".equals(entrada.p_RespondeEncuesta);
        if (respondeEncuesta) {
            ObtenerResultadoEncuesta obtenerResultadoEncuesta = new ObtenerResultadoEncuesta();
            ObtenerResultadoEncuesta.ObtenerResultadoEncuesta_Entrada entradaObtenerResultadoEncuesta = new ObtenerResultadoEncuesta.ObtenerResultadoEncuesta_Entrada();
            entradaObtenerResultadoEncuesta.p_IdDefinicionEncuesta = entrada.p_IdDefinicionEncuesta;
            entradaObtenerResultadoEncuesta.p_IdEmpresa = entrada.p_IdDefinicionEncuesta;
            entradaObtenerResultadoEncuesta.p_IdCanal = entrada.p_IdCanal;
            entradaObtenerResultadoEncuesta.p_IdModulo = entrada.p_IdModulo;
            entradaObtenerResultadoEncuesta.p_IdUsuario = entrada.p_IdUsuario;
            entradaObtenerResultadoEncuesta.p_Respuestas = entrada.p_Respuestas;
            ObtenerResultadoEncuesta.ObtenerResultadoEncuesta_Salida salidaObtenerResultadoEncuesta = obtenerResultadoEncuesta.logica(conn, entradaObtenerResultadoEncuesta);
            salida.P_OUT_ERRORESCONTADOR += salidaObtenerResultadoEncuesta.P_OUT_ERRORESCONTADOR;
            salida.P_OUT_MENSAJES.addAll(salidaObtenerResultadoEncuesta.P_OUT_MENSAJES);
            if (salida.P_OUT_ERRORESCONTADOR > 0) {
                return salida;
            }

            DatosInversionista datosInversionista = consultarDatosInversionista(conn, entrada, salidaObtenerResultadoEncuesta.IdCategoriaInversionista);
            if (datosInversionista == null) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;

                HashMap<String, String> parametros = new HashMap<>(2);
                parametros.put("CategoriaInversionista", salidaObtenerResultadoEncuesta.IdCategoriaInversionista);
                validacion.MensajeError = new MensajeSalida(entrada.p_IdEnrolamiento, MensajeSalida.MENSAJE_ERROR, "UI_CATEGORIA_INVERSIONISTA_SIN_PERFIL_COMERCIAL_POR_DEFECTO", parametros, conn, entrada.p_IdEmpresa);

                salida.agregarValidacion(validacion);

                return salida;
            }

            List<Validacion> validacionesActualizar = actualizarCategoriaInversionistaEnrolamiento(conn, datosInversionista);
            salida.agregarValidaciones(validacionesActualizar);
            if (salida.P_OUT_ERRORESCONTADOR > 0) {
                return salida;
            }

            salida.IdCategoriaInversionista = datosInversionista.IdCategoriaInversionista;
            salida.IdPerfilComercial = datosInversionista.IdPerfilComercial;
        }

        actualizarRespondioEncuesta(conn, entrada.p_IdEnrolamiento);

        long fin = System.currentTimeMillis();
        logger.info("FIN::BorradorResponderEncuesta", fin - inicio);

        return salida;
    }

    private List<Validacion> actualizarCategoriaInversionistaEnrolamiento(Connection conn, DatosInversionista datosInversionista) throws Exception {
        logger.debug("INICIO::BorradorResponderEncuesta.actualizarCategoriaInversionistaEnrolamiento");
        long inicio = System.currentTimeMillis();

        List<Validacion> validaciones = new ArrayList<>();

        SQL sql = new SQL();
        sql.batch(conn, SQL_UPDATE_CATEGORIA_INVERSIONISTA, Collections.singletonList(datosInversionista), DatosInversionista.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::BorradorResponderEncuesta.actualizarCategoriaInversionistaEnrolamiento", fin - inicio);

        return validaciones;
    }

    private void actualizarRespondioEncuesta(Connection conn, Long idEnrolamiento) throws SQLException {
        logger.debug("INICIO::BorradorResponderEncuesta.actualizarCategoriaInversionistaEnrolamiento");
        long inicio = System.currentTimeMillis();

        DatosInversionista datosInversionista = new DatosInversionista();
        datosInversionista.IdEnrolamiento = idEnrolamiento;

        SQL sql = new SQL();
        sql.batch(conn, SQL_UPDATE_RESPONDIO_ENCUESTA, Collections.singletonList(datosInversionista), DatosInversionista.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::BorradorResponderEncuesta.actualizarCategoriaInversionistaEnrolamiento", fin - inicio);
    }

    private DatosInversionista consultarDatosInversionista(Connection conn, EntradaBorradorResponderEncuesta entrada, String idCategoriaInversionista) throws SQLException {
        logger.debug("INICIO::BorradorResponderEncuesta.consultarDatosInversionista");
        long inicio = System.currentTimeMillis();

        HashMap<String, Object> parametros = new HashMap<>(8);
        parametros.put("IdEnrolamiento", entrada.p_IdEnrolamiento);
        parametros.put("IdCategoriaInversionista", idCategoriaInversionista);
        parametros.put("IdEmpresa", entrada.p_IdEmpresa);

        SQL sql = new SQL();
        DatosInversionista datosInversionista = sql.query(conn, SQL_SELECT_DATOS_INVERSIONISTA, parametros, DatosInversionista.class).stream().findFirst().orElse(null);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::BorradorResponderEncuesta.consultarDatosInversionista", fin - inicio);

        return datosInversionista;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String jsonRequest = "{\"p_RespondeEncuesta\": \"S\", \"p_IdCanal\":\"OPT\",\"p_IdEmpresa\":1,\"p_IdDefinicionEncuesta\":32,\"p_IdModulo\":\"SOC\",\"p_IdUsuario\":4,\"p_IdEnrolamiento\":375,\"p_Respuestas\":[{\"IdDefinicionPregunta\":63,\"IdDefinicionRespuesta\":105},{\"IdDefinicionPregunta\":64,\"IdDefinicionRespuesta\":109},{\"IdDefinicionPregunta\":65,\"IdDefinicionRespuesta\":112},{\"IdDefinicionPregunta\":66,\"IdDefinicionRespuesta\":116},{\"IdDefinicionPregunta\":67,\"IdDefinicionRespuesta\":121},{\"IdDefinicionPregunta\":68,\"IdDefinicionRespuesta\":125},{\"IdDefinicionPregunta\":69,\"IdDefinicionRespuesta\":129},{\"IdDefinicionPregunta\":70,\"IdDefinicionRespuesta\":132},{\"IdDefinicionPregunta\":71,\"IdDefinicionRespuesta\":136},{\"IdDefinicionPregunta\":72,\"IdDefinicionRespuesta\":141},{\"IdDefinicionPregunta\":73,\"IdDefinicionRespuesta\":146},{\"IdDefinicionPregunta\":74,\"IdDefinicionRespuesta\":150},{\"IdDefinicionPregunta\":75,\"IdDefinicionRespuesta\":154},{\"IdDefinicionPregunta\":76,\"IdDefinicionRespuesta\":159},{\"IdDefinicionPregunta\":77,\"IdDefinicionRespuesta\":163}]}";

        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaBorradorResponderEncuesta entrada = gson.fromJson(jsonRequest, EntradaBorradorResponderEncuesta.class);

            System.out.println(entrada);

            BorradorResponderEncuesta borradorResponderEncuesta = new BorradorResponderEncuesta();
            try {
                SalidaBorradorResponderEncuesta salida = borradorResponderEncuesta.logica(conn, entrada);
                System.out.println(salida);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.rollback();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
