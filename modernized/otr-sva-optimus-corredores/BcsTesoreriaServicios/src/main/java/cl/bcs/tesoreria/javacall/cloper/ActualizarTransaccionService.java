package cl.bcs.tesoreria.javacall.cloper;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.comun.Parametros;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.BcsPojo;

/**
 * Legacy {@code cl.bcs.tesoreria.cloper.ActualizarTransaccion}, behaviours TES-A-760..779: asks the CLOPER web service (the clearing house, through
 * the {@code Cloper} collaborator) for the trades of a date and stamps the chamber and the bilateral flag on the matching Optimus transactions.
 *
 * <p>The collaborator receives {@code {body, conexion: {wsdl, profile, password}}}; the password never travels through the port ({@code "<set>"}
 * only says it is configured): the production adapter reads {@code PLATAFORMA/PLA.CloperWsBSC.Password} itself. It answers
 * {@code {"valor": [rows]}}, or an empty object for the legacy {@code null} (a failed call).
 */
@Service
public class ActualizarTransaccionService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarTransaccionService.class);
    /** The legacy used a plain Gson on both sides. */
    private static final Gson GSON = new Gson();

    private static final String SQL_TRANSACCION = loadSql("sql/ActualizarTransaccion_SelectTransaccion.sql");
    private static final String SQL_ACTUALIZAR = loadSql("sql/ActualizarTransaccion_UpdateTransaccion.sql");
    private static final String SQL_CODIGO_CORREDORA = loadSql("sql/ActualizarTransaccion_SelectCodigoCorredora.sql");

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class ActualizarTransaccion_Entrada extends BcsPojo {
        public Long p_IdEmpresa;
        public String p_FechaTransaccion;
        /** May be null (query by date only), "0" or "?" (same meaning). */
        public String p_FolioTransaccion;
    }

    public static class ActualizarTransaccion_Salida extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    public static class Transaccion extends BcsPojo {
        public Long IdTransaccion;
        public String Folio;
        public String Nemotecnico;
        public BigDecimal Cantidad;
        public BigDecimal Precio;
        public BigDecimal Monto;
        /** System date the transaction was entered (the operation date without time). */
        public java.sql.Date FechaSistemaCreacion;
        public java.sql.Date FechaLiquidacion;
        public String IdCamara;
        public String EsBilateral;
    }

    public static class CodigoCorredora extends BcsPojo {
        public String NombreCorto = "";
        public Long IdCorredora = 0L;
        public String CodigoCorredora = "";
        public String IdBolsa = "";
    }

    /** One trade of the CLOPER answer (legacy {@code Cloper} data fields). */
    public static class Cloper extends BcsPojo {
        public Long bolsa;
        public String camara;
        public BigDecimal cantidad;
        public Long cod_corr_com;
        public Long cod_corr_ven;
        public Long cod_liq_com;
        public Long cod_liq_ven;
        public Long cod_ope_com;
        public Long cod_ope_ven;
        public Long dias_ven;
        public String emisor;
        public String estado;
        public String estado_liq_bil;
        public String fec_liq;
        public String fecha;
        public String fecha_pago;
        public String folio;
        public String hora;
        public String madurez;
        public String mercado;
        public String moneda;
        public BigDecimal monto;
        public BigDecimal monto_div;
        public String nemo;
        public String tipo_liq;
        public String tipo_liquida;
        public String tipo_opera;
    }

    /** Connection data of the CLOPER web service as read from the configuration (legacy {@code ParametrosConexionCloperWS}). */
    private static final class ParametrosConexion {
        String wsdl;
        String profile;
        String password;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public ActualizarTransaccionService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}: the error counter is recounted from the messages. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        ActualizarTransaccion_Entrada in = GSON.fromJson(entrada, ActualizarTransaccion_Entrada.class);
        ActualizarTransaccion_Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return GSON.toJsonTree(out).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public ActualizarTransaccion_Salida logica(ActualizarTransaccion_Entrada entrada) throws Exception {
        ActualizarTransaccion_Salida salida = new ActualizarTransaccion_Salida();
        Long idEmpresa = entrada.p_IdEmpresa;

        Map<String, Object> filtro = new HashMap<>();
        filtro.put("IdEmpresa", idEmpresa);
        filtro.put("FechaSistemaCreacion", entrada.p_FechaTransaccion);
        filtro.put("FolioTransaccion", folioConsultado(entrada));
        List<Transaccion> transacciones = jdbc.query(SQL_TRANSACCION, filtro, Filas.de(Transaccion.class));
        if (transacciones.isEmpty()) {
            // a null value map is the legacy way to ask for the untranslated text
            salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "NO_SE_ENCONTRARON_TRANSACCIONES_PARA_FECHA_INDICADA",
                    diccionario.mensaje(idEmpresa, "NO_SE_ENCONTRARON_TRANSACCIONES_PARA_FECHA_INDICADA", null)));
            return salida;
        }

        // LEGACY DEFECT kept (see README TES-A-777): the fallback "0" is dead code (the lookup never returns null), so the web service is asked
        // with an empty user when the broker has no code
        CodigoCorredora corredora = codigoCorredora();
        String codigoCorredora = corredora != null ? corredora.CodigoCorredora : "0";

        ParametrosConexion conexion = parametrosDeConexion(idEmpresa, salida);
        if (salida.P_OUT_ERRORESCONTADOR > 0L) {
            salida.P_OUT_MENSAJES.add(mensaje(idEmpresa, MensajeSalida.MENSAJE_ERROR, "PARAMETROS_CLOPER_WS_NO_ENCONTRADO"));
            return salida;
        }

        List<Cloper> palos = obtenerDataCloper(entrada, codigoCorredora, conexion);
        if (palos == null) {
            salida.P_OUT_MENSAJES.add(mensaje(idEmpresa, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_CONSULTAR_CLOPER_WS"));
            return salida;
        }
        if (palos.isEmpty()) {
            salida.P_OUT_MENSAJES.add(mensaje(idEmpresa, MensajeSalida.MENSAJE_INFORMACION, "NO_SE_ENCONTRARON_PALOS_EN_LA_CCLV_PARA_PROCESAR"));
            return salida;
        }

        List<Transaccion> aActualizar = new ArrayList<>();
        for (Transaccion t : transacciones) {
            Cloper palo = palos.stream().filter(c -> calza(t, c)).findFirst().orElse(null);
            if (palo != null) {
                t.IdCamara = palo.camara.trim();
                t.EsBilateral = esBilateral(palo) ? "S" : "N";
                aActualizar.add(t);
            }
        }
        if (aActualizar.isEmpty()) {
            salida.P_OUT_MENSAJES.add(mensaje(idEmpresa, MensajeSalida.MENSAJE_INFORMACION, "NO_SE_ENCONTRARON_CALCES_ENTRE_LAS_TRANSACCIONES_Y_LOS_PALOS_DEL_CCLV"));
            return salida;
        }
        actualizar(aActualizar, salida, idEmpresa);

        if (salida.P_OUT_ERRORESCONTADOR == 0) {
            String codigo = "TRANSACCIONES_ACTUALIZADAS_CON_CALCE_EN_LA_CCLV";
            salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, codigo,
                    diccionario.mensaje(idEmpresa, codigo, Map.of("Cantidad", String.valueOf(aActualizar.size())))));
        }
        return salida;
    }

    /** Folio "0", "?" and null all mean "every folio of the date". */
    private static boolean sinFolio(ActualizarTransaccion_Entrada entrada) {
        return entrada.p_FolioTransaccion == null || entrada.p_FolioTransaccion.equals("0") || entrada.p_FolioTransaccion.equals("?");
    }

    private static String folioConsultado(ActualizarTransaccion_Entrada entrada) {
        return sinFolio(entrada) ? "" : entrada.p_FolioTransaccion;
    }

    private CodigoCorredora codigoCorredora() {
        List<CodigoCorredora> codigos = jdbc.query(SQL_CODIGO_CORREDORA, Map.of(), Filas.de(CodigoCorredora.class));
        return codigos.isEmpty() ? new CodigoCorredora() : codigos.get(0);
    }

    /** Reads the three connection parameters; the first one missing is counted as an error (the legacy counted it, then reported it as a message). */
    private ParametrosConexion parametrosDeConexion(Long idEmpresa, ActualizarTransaccion_Salida salida) {
        ParametrosConexion p = new ParametrosConexion();
        try {
            String password = Parametros.texto(configuracion, idEmpresa, Parametros.PLATAFORMA, "PLA.CloperWsBSC.Password");
            String profile = Parametros.texto(configuracion, idEmpresa, Parametros.PLATAFORMA, "PLA.CloperWsBSC.Profile");
            String wsdl = Parametros.texto(configuracion, idEmpresa, Parametros.PLATAFORMA, "PLA.CloperWsBSC.Wsdl");
            p.password = password;
            p.profile = profile;
            p.wsdl = wsdl;
        } catch (Exception e) {
            log.error(e.getMessage());
            salida.P_OUT_ERRORESCONTADOR++;
        }
        return p;
    }

    /** The trades of the date from CLOPER, or null when the call failed (the legacy swallowed any exception of the call). */
    private List<Cloper> obtenerDataCloper(ActualizarTransaccion_Entrada entrada, String codigoCorredora, ParametrosConexion conexion) {
        try {
            JsonObject consulta = new JsonObject();
            consulta.addProperty("id", "1");
            consulta.addProperty("fecha", entrada.p_FechaTransaccion);
            consulta.addProperty("folio", sinFolio(entrada) ? "0" : entrada.p_FolioTransaccion);
            consulta.addProperty("usuario", codigoCorredora);
            JsonArray cuerpo = new JsonArray();
            cuerpo.add(consulta);

            JsonObject datosConexion = new JsonObject();
            datosConexion.addProperty("wsdl", String.valueOf(conexion.wsdl));
            datosConexion.addProperty("profile", String.valueOf(conexion.profile));
            datosConexion.addProperty("password", conexion.password == null ? "null" : "<set>");
            JsonObject peticion = new JsonObject();
            peticion.add("body", cuerpo);
            peticion.add("conexion", datosConexion);

            JsonElement valor = colaboradores.invocar("Cloper", peticion).get("valor");
            if (valor == null || valor.isJsonNull()) {
                return null;
            }
            List<Cloper> palos = new ArrayList<>();
            valor.getAsJsonArray().forEach(fila -> palos.add(GSON.fromJson(fila, Cloper.class)));
            return palos;
        } catch (UnsupportedOperationException e) {
            // NOT a Cloper answer: the CLOPER client is not available in this deployment (ColaboradoresJavaCall). It must not be swallowed as the
            // legacy "call failed" (HTTP 200 with ERROR_AL_CONSULTAR_CLOPER_WS): it escapes so the endpoint answers 503 and the router falls back to TomEE.
            log.warn("CLOPER collaborator is not available; the request is left to the TomEE fallback", e);
            throw e;
        } catch (RuntimeException e) {
            log.debug(e.getMessage());
            return null;
        }
    }

    /** A trade matches when folio, nemo, date, quantity and amount are the same. */
    private static boolean calza(Transaccion t, Cloper c) {
        return t.Folio.equalsIgnoreCase(c.folio.toString().trim()) && t.Nemotecnico.equalsIgnoreCase(c.nemo.toString().trim())
                && t.FechaSistemaCreacion.toString().equalsIgnoreCase(c.fecha.toString().trim()) && t.Cantidad.compareTo(c.cantidad) == 0
                && t.Monto.compareTo(c.monto) == 0;
    }

    /** Bilateral: liquidation type B, or type M in state RZ or E. */
    private static boolean esBilateral(Cloper c) {
        String tipo = c.tipo_liquida.toString().trim();
        String estado = c.estado.toString().trim();
        return "B".equalsIgnoreCase(tipo) || "M".equalsIgnoreCase(tipo) && ("RZ".equalsIgnoreCase(estado) || "E".equalsIgnoreCase(estado));
    }

    /** A failure of the update is reported as a message and counted, not thrown. */
    private void actualizar(List<Transaccion> transacciones, ActualizarTransaccion_Salida salida, Long idEmpresa) {
        try {
            List<Map<String, Object>> filas = new ArrayList<>();
            for (Transaccion t : transacciones) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("IdCamara", t.IdCamara);
                fila.put("EsBilateral", t.EsBilateral);
                fila.put("IdTransaccion", t.IdTransaccion);
                filas.add(fila);
            }
            Consultas.lote(jdbc, SQL_ACTUALIZAR, filas);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            salida.P_OUT_ERRORESCONTADOR++;
            salida.P_OUT_MENSAJES.add(mensaje(idEmpresa, MensajeSalida.MENSAJE_ERROR, "ERROR_ACTUALIZAR_TRANSACCION"));
        }
    }

    private MensajeSalida mensaje(Long idEmpresa, String tipo, String codigo) {
        return new MensajeSalida(0L, tipo, codigo, diccionario.mensaje(idEmpresa, codigo, new HashMap<>()));
    }
}
