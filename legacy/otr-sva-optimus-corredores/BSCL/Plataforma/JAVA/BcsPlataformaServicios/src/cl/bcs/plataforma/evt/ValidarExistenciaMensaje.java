package cl.bcs.plataforma.evt;

import cl.bcs.plataforma.evt.pojo.Mensaje;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
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

public class ValidarExistenciaMensaje implements JavaCall {

    static final Logger logger = LogManager.getLogger(ValidarExistenciaMensaje.class);

    private final String SQL_SELECT_MENSAJE = loadResourceToString("cl/bcs/plataforma/evt/sql/ComunesMensaje_SelectCountMensaje.sql");

    public static class EntradaValidarExistenciaMensaje extends BcsPojo {
        public Long p_IdEmpresa;
        public List<Mensaje> p_Mensajes;

        public EntradaValidarExistenciaMensaje() {
            this.p_Mensajes = new ArrayList<>();
        }
    }

    public static class SalidaValidarExistenciaMensaje extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaMensaje() {
            this.validaciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaValidarExistenciaMensaje logica(Connection con, EntradaValidarExistenciaMensaje entrada) throws Exception {
        logger.info("INICIO::ValidarExistenciaMensaje");
        long inicio = System.currentTimeMillis();

        SalidaValidarExistenciaMensaje out = new SalidaValidarExistenciaMensaje();
        out.validaciones = validarExistenciaMensajes(con, entrada.p_Mensajes, entrada.p_IdEmpresa);

        long fin = System.currentTimeMillis();
        logger.info("FIN::ValidarExistenciaMensaje", fin - inicio);

        return out;
    }

    private List<Validacion> validarExistenciaMensajes(Connection con, List<Mensaje> mensajes, Long idEmpresa) throws Exception {
        logger.debug("INICIO::ValidarExistenciaMensaje.validarExistenciaMensajes");
        long inicio = System.currentTimeMillis();

        List<Long> idMensaje = mensajes.parallelStream().map(mensaje -> mensaje.IdMensaje).collect(Collectors.toList());

        HashMap<String, Object> parametros = new HashMap<>(1);
        parametros.put("IdMensaje", idMensaje);

        SQL sql = new SQL();
        List<Mensaje> mensajesExistentes = sql.query(con, SQL_SELECT_MENSAJE, parametros, Mensaje.class);
        Map<Long, Mensaje> mensajesPorIdMensaje = mensajesExistentes.parallelStream().collect(Collectors.toMap(mensajesExistente -> mensajesExistente.IdMensaje, mensajesExistente -> mensajesExistente));

        List<Validacion> validaciones = new ArrayList<>();

        for (Mensaje mensaje : mensajes) {
            if (mensajesPorIdMensaje.get(mensaje.IdMensaje) == null) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                HashMap<String, String> datosMensajes = new HashMap<>(1);
                datosMensajes.put("Titulo", mensaje.Titulo);
                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_MENSAJE_NO_EXISTE", datosMensajes, con, idEmpresa);

                validaciones.add(validacion);
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarExistenciaMensaje.validarExistenciaMensajes", fin - inicio);

        return validaciones;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":1,\"p_UsaReglasNegocio\":1,\"p_CodigoFormulario\":1,\"p_IdTipoCanal\":1,\"p_Mensajes\":[{\"IdInterno\":1, \"IdMensaje\":-7, \"FechaVigenciaDesde\": \"2021-01-01\",\"FechaVigenciaHasta\":\"2022-01-01\",\"Habilitado\": \"N\",\"IdTipoEntidadDestinatario\": \"CLI\",\"IdTipoMensaje\": \"AVISO\",\"Titulo\": \"NOMBRE_DEL_AVISO\",\"MensajeCorto\": \"ESTE ES UN TEXTO\",\"MensajeLargo\": \"ESTE ES UN TEXTO MAS LARGO\",\"MuestraMensajeCorto\": \"N\",\"UrlAviso\": \"\",\"IdTipoCanal\": \"CLI\"}]}";

        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaValidarExistenciaMensaje entrada = gson.fromJson(jsonEntrada, EntradaValidarExistenciaMensaje.class);

            ValidarExistenciaMensaje validador = new ValidarExistenciaMensaje();
            try {
                SalidaValidarExistenciaMensaje salida = validador.logica(con, entrada);
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
