package cl.bcs.plataforma.login.bootstrap;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cl.bcs.plataforma.login.dialecto.SentenciasSql;
import cl.bcs.plataforma.login.ProcedimientoAlmacenado;
import cl.bcs.plataforma.login.jdbc.SqlNombrado;
import cl.bcs.plataforma.login.sesion.SesionDatos;
import cl.bcs.plataforma.login.sesion.SesionDatos.SesionData;

/**
 * The bootstrap of the web application as a JSON API (T-20, T-21, T-21b; option B: the UI is outside the core). The legacy {@code BootstrapServlet}
 * assembled {@code login.html} and {@code home.html} and embedded, as JavaScript constants, the data the AngularJS application needs. Here the same data
 * is answered as one JSON object whose keys ARE the names of those constants ({@code BCS_DICCIONARIO}, {@code BCS_SESION}, {@code BCS_ACCESOS}...,
 * {@code bcs_feriados}, {@code bcs_formato_cantidad}...): a loader of the UI defines each key as a variable of the page before Angular starts, so the
 * application cannot tell the difference. Values keep the shape of the legacy ones; the UI-owned parts (HTML, libraries, menu and window files,
 * localisation script, Datadog RUM script) are not served here.
 *
 * <p>Contract {@value #CONTRATO} version {@value #VERSION}: a key that begins with an underscore ({@code _contrato}) is metadata, not a variable. The
 * login answer carries, besides the constants, {@code empresas} (the legacy {@code <::EMPRESAS::>}) and {@code logo} ({@code <::LOGO_CORREDOR::>}); the
 * home answer carries {@code BCS_USER_MENU} as the CODE of the user's menu (the legacy wrote it as the name of a JavaScript variable of the menu file,
 * T-21c).
 */
@Service
public class BootstrapServicio {

    public static final String CONTRATO = "optimus-bootstrap";
    public static final int VERSION = 1;

    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    /** Configuration parameters of other modules the home may read (legacy {@code getConfiguracionAsJson}); every parameter of the PRODUCTO module too. */
    static final List<String> PARAMETROS_PERMITIDOS = Arrays.asList("APV.Habilitado", "TOTALREFACTURACION", "URLWIKIOPT", "TasaPrestamoIgualTasaEmprestito",
            "REFAC_MASIVA", "Desabilitar_OD_AFP", "EVT.TIEMPO_PULL_NOTIFICACIONES", "REFACTURAR_BATCH_DESDE", "CalculoPrecioPunta", "ParametroPrecioMercado",
            "ActivaGeneraSpot");

    public static class Diccionario {
        public String Codigo;
        public String Descripcion;
        public String Abreviado;
    }

    public static class Empresa {
        public Long IdEmpresa;
        public String Nombre;
    }

    public static class Imagen {
        public String NombreImagen;
        public byte[] Binario;
    }

    public static class DiaFeriado {
        public Long IdFeriado;
        public String Tipo;
        public String Fecha;
        public Integer Dia;
        public String DescripcionDia;
        public Integer AgnoCarga;
        public Long IdPais;
    }

    public static class Parametro {
        public Long IdEmpresa;
        public String Modulo;
        public String CodigoParametro;
        public String Valor;
    }

    public static class TipoOperacionInstrumento {
        public String IdTipoInstrumento;
        public String IdTipoOperacion;
        public String AccionOrden;
        public String IdTipoProducto;
        public String EsVentaCorta;
        public String EsPutEsCall;
    }

    public static class Preferencia {
        public String Llave;
        public String Valor;
    }

    public static class ConfiguracionFiltro {
        public Long IdConfiguracionFiltroInterfaz;
        public String Controlador;
        public String TipoObjeto;
        public String NombreObjeto;
        public String IdEstado;
        public String Filtros;
    }

    public static class UsuarioAcceso {
        public String IdElementoHtml;
        public String EsVisibleMenu;
        public String Existe;
        public Long IdUsuario;
        public Long IdAcceso;
        public String NombreAcceso;
        public String TipoAcceso;
        public Long Padre;
        public Integer Orden;
        public Long IdServicio;
    }

    static final String SQL_DICCIONARIO = SentenciasSql.hana("BootstrapServicio.SQL_DICCIONARIO");
    static final String SQL_EMPRESAS = SentenciasSql.hana("BootstrapServicio.SQL_EMPRESAS");
    static final String SQL_IMAGEN = SentenciasSql.hana("BootstrapServicio.SQL_IMAGEN");
    static final String SQL_FERIADOS = SentenciasSql.hana("BootstrapServicio.SQL_FERIADOS");
    static final String SQL_TIPO_OPERACION = SentenciasSql.hana("BootstrapServicio.SQL_TIPO_OPERACION");
    static final String SQL_PREFERENCIAS = SentenciasSql.hana("BootstrapServicio.SQL_PREFERENCIAS");
    static final String SQL_PREFERENCIAS_GRILLA = SentenciasSql.hana("BootstrapServicio.SQL_PREFERENCIAS_GRILLA");
    static final String SQL_FILTRO_COMBO = SentenciasSql.hana("BootstrapServicio.SQL_FILTRO_COMBO");
    static final String SQL_ACCESOS = SentenciasSql.hana("BootstrapServicio.SQL_ACCESOS");

    /** Legacy {@code Utils.getConfiguraciones}: the configuration tables of the 12 modules for one company (UNION: duplicate rows collapse). */
    static final String SQL_CONFIGURACIONES = SentenciasSql.hana("BootstrapServicio.SQL_CONFIGURACIONES");

    static final String SP_FORMATOS = SentenciasSql.hana("BootstrapServicio.SP_FORMATOS");
    static final String SP_USUARIO_ADMIN = SentenciasSql.hana("BootstrapServicio.SP_USUARIO_ADMIN");
    static final String SP_OBTENER_CONFIGURACION = SentenciasSql.hana("BootstrapServicio.SP_OBTENER_CONFIGURACION");
    static final String SP_FECHAS = SentenciasSql.hana("BootstrapServicio.SP_FECHAS");

    /** The statements the login page reads: all their objects are checked before the first one runs. */
    /** The statements the home page needs, the session ones included: checked before the first one runs, so a 503 never follows a write. */
    private static final List<String> SENTENCIAS_DEL_HOME = List.of("SesionDatos.QUERY_USUARIO", "SesionDatos.QUERY_PARAMETROS",
            "SesionDatos.QUERY_EMPRESA", "SesionDatos.QUERY_VALORES_DEFECTO", "SesionDatos.UPDATE_SESION", "SesionDatos.UPDATE_USUARIO",
            "BootstrapServicio.SQL_PREFERENCIAS", "BootstrapServicio.SQL_PREFERENCIAS_GRILLA", "BootstrapServicio.SQL_DICCIONARIO",
            "BootstrapServicio.SQL_FERIADOS", "BootstrapServicio.SQL_TIPO_OPERACION", "BootstrapServicio.SQL_FILTRO_COMBO",
            "BootstrapServicio.SQL_ACCESOS", "BootstrapServicio.SQL_CONFIGURACIONES");

    private static final List<String> SENTENCIAS_DEL_LOGIN = List.of("BootstrapServicio.SQL_CONFIGURACIONES", "BootstrapServicio.SQL_DICCIONARIO",
            "BootstrapServicio.SQL_EMPRESAS", "BootstrapServicio.SQL_IMAGEN");

    private final SesionDatos sesionDatos;
    private final ProcedimientoAlmacenado procedimientos;
    private final SentenciasSql sentencias;

    /** HANA statements: what the module has always sent. */
    public BootstrapServicio(SesionDatos sesionDatos, ProcedimientoAlmacenado procedimientos) {
        this(sesionDatos, procedimientos, SentenciasSql.predeterminadas());
    }

    @Autowired
    public BootstrapServicio(SesionDatos sesionDatos, ProcedimientoAlmacenado procedimientos, SentenciasSql sentencias) {
        this.sesionDatos = sesionDatos;
        this.procedimientos = procedimientos;
        this.sentencias = sentencias;
    }

    /** The result of the home bootstrap: the data, or a refusal that carries only the codes of the legacy messages. */
    public record Home(boolean valida, JsonObject datos) {
    }

    // ------------------------------------------------------------------------------------------------ GET /PLA/bootstrap/login (T-20)

    /** What the login page needs; no session. The company-1 configuration decides environment, offset and SSO button, as the legacy. */
    public JsonObject login(Connection con) throws Exception {
        sentencias.exigirDisponibles(SENTENCIAS_DEL_LOGIN);
        Map<String, Map<String, String>> configuracion = configuraciones(con, 1);
        Map<String, String> pla = configuracion.get("PLATAFORMA");
        JsonObject out = base();
        out.addProperty("CONF_LOCALE", "CL");
        out.add("BCS_DICCIONARIO", diccionario(con, "CL"));
        out.add("BCS_SESION", new JsonObject());
        out.add("BCS_OFFSET", numeroOTexto(pla.get("PLA.SegundosZonaHoraria")));
        out.add("BCS_VERSION", numeroOTexto("0"));
        out.add("BCS_ACCESOS", new JsonObject());
        out.add("BCS_PREFERENCIAS", new JsonObject());
        out.add("BCS_PREFERENCIAS_GRILLA", new JsonObject());
        out.addProperty("BCS_AMBIENTE", pla.get("PLA.Ambiente"));
        out.addProperty("BCS_TIMESTAMP_LOCAL", "");
        out.addProperty("BCS_MUESTRA_SSO_GOOGLE", textoComoLegacy(pla.get("PLA.MuestraSSOGoogle")));
        out.add("bcs_feriados", new JsonObject());
        out.add("BCS_OPERACION_INSTRUMENTO", new JsonObject());
        out.add("BCS_ESTADO_MODULO", new JsonObject());
        out.add("BCS_CONFIGURACION", new JsonObject());
        out.addProperty("BCS_OFFLINE", true);
        out.addProperty("BCS_USER_MENU", "BCS_FULLMENU");
        out.add("empresas", GSON.toJsonTree(empresas(con)));
        out.add("logo", logo(con));
        return out;
    }

    // ------------------------------------------------------------------------------------------------ POST /PLA/bootstrap/home (T-21)

    /**
     * What the home page needs for the session of the token. A session that is not valid (or whose user is blocked, expired, not valid or whose company
     * is badly configured) is refused; a valid one is used (one more use of the token, user connected): the caller commits it.
     */
    public Home home(Connection con, String token) throws Exception {
        sentencias.exigirDisponibles(SENTENCIAS_DEL_HOME);
        SesionData sesion = sesionDatos.getSesionData(con, token);
        if (sesion.P_OUT_ERRORESCONTADOR > 0L) {
            con.rollback();
            JsonObject rechazo = new JsonObject();
            rechazo.addProperty("P_OUT_ERRORESCONTADOR", sesion.P_OUT_ERRORESCONTADOR);
            JsonArray codigos = new JsonArray();
            for (SesionDatos.Mensaje m : sesion.P_OUT_MENSAJES) {
                JsonObject o = new JsonObject();
                o.addProperty("Codigo", m.Codigo);
                codigos.add(o);
            }
            rechazo.add("P_OUT_MENSAJES", codigos);
            return new Home(false, rechazo);
        }

        Map<String, Map<String, String>> configuracion = configuraciones(con, 1);
        Map<String, String> pla = configuracion.get("PLATAFORMA");
        String menu = sesion.CodigoMenuUsuario.toUpperCase();

        JsonObject out = base();
        out.addProperty("CONF_LOCALE", pla.get("PLA.LocalizacionSistema"));
        out.add("BCS_DICCIONARIO", diccionario(con, pla.get("PLA.LocalizacionSistema")));
        out.add("BCS_SESION", GSON.toJsonTree(sesion));
        out.add("BCS_OFFSET", numeroOTexto(pla.get("PLA.SegundosZonaHoraria")));
        out.add("BCS_VERSION", numeroOTexto(pla.get("PLA.Version")));
        out.add("BCS_ACCESOS", accesos(con, sesion.IdEmpresa, sesion.IdUsuario));
        out.add("BCS_PREFERENCIAS", clavesValor(con, sentencias.de(SQL_PREFERENCIAS), parametros("IdEmpresa", sesion.IdEmpresa, "IdUsuario", sesion.IdUsuario)));
        out.add("BCS_PREFERENCIAS_GRILLA", clavesValor(con, sentencias.de(SQL_PREFERENCIAS_GRILLA), parametros("IdEmpresa", sesion.IdEmpresa)));
        out.addProperty("BCS_AMBIENTE", pla.get("PLA.Ambiente"));
        out.addProperty("BCS_TIMESTAMP_LOCAL", fechaHoraLocal(con, sesion.IdEmpresa));
        out.addProperty("BCS_MUESTRA_SSO_GOOGLE", textoComoLegacy(null));
        out.add("bcs_feriados", feriados(con));
        out.add("BCS_OPERACION_INSTRUMENTO", tiposOperacion(con));
        out.add("BCS_ESTADO_MODULO", new JsonObject());
        out.add("BCS_CONFIGURACION", configuracionPublica(configuracion));
        out.addProperty("BCS_OFFLINE", true);
        formatos(con, out);
        out.add("bcs_filtrocombobase", filtroComboBase(con));
        out.addProperty("BCS_USER_MENU", menu.length() > 0 ? menu : "BCS_FULLMENU");
        out.add("empresas", GSON.toJsonTree(empresas(con)));
        out.add("logo", logo(con));
        return new Home(true, out);
    }

    // ------------------------------------------------------------------------------------------------ pieces

    /** The legacy page wrote {@code '" + value + "'}: an absent value is the TEXT null, not a JSON null. */
    private static String textoComoLegacy(String valor) {
        return valor == null ? "null" : valor;
    }

    private static JsonObject base() {
        JsonObject out = new JsonObject();
        JsonObject contrato = new JsonObject();
        contrato.addProperty("nombre", CONTRATO);
        contrato.addProperty("version", VERSION);
        out.add("_contrato", contrato);
        // a constant of the legacy page (BootstrapServlet: fixed text, not data): the login background images
        JsonArray fondo = new JsonArray();
        fondo.add("santiago-bg.jpg");
        fondo.add("santiago2-bg.jpg");
        out.add("CONF_IMAGEN_FONDO", fondo);
        return out;
    }

    /** Legacy {@code getDiccionario}: {@code Codigo} to text and {@code Codigo_ABR} to the abbreviation, for the language. */
    JsonObject diccionario(Connection con, String lenguaje) throws Exception {
        List<Diccionario> filas = SqlNombrado.consultar(con, sentencias.de(SQL_DICCIONARIO), parametros("CodLenguaje", lenguaje == null ? "CL" : lenguaje), Diccionario.class);
        JsonObject out = new JsonObject();
        for (Diccionario d : filas) {
            out.addProperty(d.Codigo, d.Descripcion);
            out.addProperty(d.Codigo + "_ABR", d.Abreviado);
        }
        return out;
    }

    List<Empresa> empresas(Connection con) throws Exception {
        return SqlNombrado.consultar(con, sentencias.de(SQL_EMPRESAS), new HashMap<>(), Empresa.class);
    }

    /** Legacy {@code getImagen}: the login logo as {@code [{NombreImagen, Binario (base64)}]}. */
    JsonArray logo(Connection con) throws Exception {
        JsonArray out = new JsonArray();
        for (Imagen img : SqlNombrado.consultar(con, sentencias.de(SQL_IMAGEN), new HashMap<>(), Imagen.class)) {
            JsonObject o = new JsonObject();
            o.addProperty("NombreImagen", img.NombreImagen);
            o.addProperty("Binario", img.Binario != null ? Base64.getEncoder().encodeToString(img.Binario) : "");
            out.add(o);
        }
        return out;
    }

    /** Legacy {@code getFeriados}: {@code IdPais -> Fecha -> {Tipo, Dia, DescripcionDia, AgnoCarga}}. */
    JsonObject feriados(Connection con) throws Exception {
        JsonObject out = new JsonObject();
        for (DiaFeriado f : SqlNombrado.consultar(con, sentencias.de(SQL_FERIADOS), new HashMap<>(), DiaFeriado.class)) {
            if (out.get(f.IdPais + "") == null) out.add(f.IdPais + "", new JsonObject());
            JsonObject pais = out.get(f.IdPais + "").getAsJsonObject();
            JsonObject fecha = new JsonObject();
            pais.add(f.Fecha, fecha);
            fecha.addProperty("Tipo", f.Tipo);
            fecha.addProperty("Dia", f.Dia);
            fecha.addProperty("DescripcionDia", f.DescripcionDia);
            fecha.addProperty("AgnoCarga", f.AgnoCarga);
        }
        return out;
    }

    JsonArray tiposOperacion(Connection con) throws Exception {
        JsonArray out = new JsonArray();
        for (TipoOperacionInstrumento r : SqlNombrado.consultar(con, sentencias.de(SQL_TIPO_OPERACION), new HashMap<>(), TipoOperacionInstrumento.class)) {
            JsonObject linea = new JsonObject();
            linea.addProperty("AccionOrden", r.AccionOrden);
            linea.addProperty("IdTipoInstrumento", r.IdTipoInstrumento);
            linea.addProperty("IdTipoOperacion", r.IdTipoOperacion);
            linea.addProperty("EsVentaCorta", r.EsVentaCorta);
            linea.addProperty("IdTipoProducto", r.IdTipoProducto);
            linea.addProperty("EsPutEsCall", r.EsPutEsCall);
            out.add(linea);
        }
        return out;
    }

    /** {@code Llave -> Valor} of a preferences view. */
    private JsonObject clavesValor(Connection con, String sql, Map<String, Object> params) throws Exception {
        JsonObject out = new JsonObject();
        for (Preferencia p : SqlNombrado.consultar(con, sql, params, Preferencia.class)) {
            out.addProperty(p.Llave, p.Valor);
        }
        return out;
    }

    /** Legacy {@code getFiltroComboBase}: {@code Controlador -> TipoObjeto -> NombreObjeto -> Filtros} (tabs and line breaks removed). */
    JsonObject filtroComboBase(Connection con) throws Exception {
        JsonObject out = new JsonObject();
        for (ConfiguracionFiltro f : SqlNombrado.consultar(con, sentencias.de(SQL_FILTRO_COMBO), new HashMap<>(), ConfiguracionFiltro.class)) {
            String filtros = f.Filtros != null ? f.Filtros : "";
            filtros = filtros.replace("\t", "").replace("\r", "").replace("\n", "");
            if (out.get(f.Controlador) == null) out.add(f.Controlador, new JsonObject());
            JsonObject controlador = out.get(f.Controlador).getAsJsonObject();
            if (controlador.get(f.TipoObjeto) == null) controlador.add(f.TipoObjeto, new JsonObject());
            controlador.get(f.TipoObjeto).getAsJsonObject().addProperty(f.NombreObjeto, filtros);
        }
        return out;
    }

    /** Legacy {@code getAccesos}: {@code ADMIN} and, per HTML element, whether it exists and is visible for the user. */
    JsonObject accesos(Connection con, long idEmpresa, long idUsuario) throws Exception {
        JsonObject out = new JsonObject();
        JsonObject in = new JsonObject();
        in.addProperty("p_IdEmpresa", idEmpresa);
        in.addProperty("p_IdUsuario", idUsuario);
        boolean esAdmin = "S".equals(procedimientos.llamar(con, SP_USUARIO_ADMIN, in).get("P_OUT_ESADMIN").getAsString());
        out.addProperty("ADMIN", esAdmin);

        JsonObject cfg = new JsonObject();
        cfg.addProperty("p_IdEmpresa", idEmpresa);
        cfg.addProperty("p_CodigoParametro", "PLA.ConflictoAccesoDefecto");
        String existePorDefecto = procedimientos.llamar(con, SP_OBTENER_CONFIGURACION, cfg).get("P_OUT_VALOR").getAsString();

        for (UsuarioAcceso acc : SqlNombrado.consultar(con, sentencias.de(SQL_ACCESOS), parametros("IdEmpresa", idEmpresa, "IdUsuario", idUsuario), UsuarioAcceso.class)) {
            String idHtml = acc.IdElementoHtml;
            String existeDB = acc.Existe;
            String valorFinalExiste = esAdmin ? "S" : existeDB;
            if (out.get(idHtml) == null) out.add(idHtml, new JsonObject());
            JsonObject ele = out.get(idHtml).getAsJsonObject();
            if (ele.has("Existe") && !esAdmin) {
                if (!ele.get("Existe").getAsString().equals(existeDB)) {
                    valorFinalExiste = existePorDefecto;
                }
            }
            ele.addProperty("IdElementoHtml", idHtml);
            ele.addProperty("Existe", valorFinalExiste);
            ele.addProperty("EsVisible", acc.EsVisibleMenu);
        }
        return out;
    }

    /**
     * Legacy {@code getFormatos}: the seven format matrices {@code uso -> moneda -> tipo de instrumento -> tipo de operacion -> {decimales, trunca/redondea}}
     * from the answer of the formats procedure; rows without {@code IdUsoMonedaFormato} are skipped.
     */
    void formatos(Connection con, JsonObject out) throws Exception {
        JsonArray filas = procedimientos.llamar(con, SP_FORMATOS, new JsonObject()).get("P_OUT_MONEDAFORMATO").getAsJsonArray();
        String[][] matrices = {
                {"bcs_formato_cantidad", "CantidadDecimales", "CantidadTrucaRedondea"},
                {"bcs_formato_precio", "PrecioDecimales", "PrecioTrucaRedondea"},
                {"bcs_formato_tasa", "TasaDecimales", "TasaTrucaRedondea"},
                {"bcs_formato_monto", "MontoDecimales", "MontoTrucaRedondea"},
                {"bcs_formato_tipoCambio", "TipoCambioDecimales", "TipoCambioTrucaRedondea"},
                {"bcs_formato_paridad", "ParidadDecimales", "ParidadTrucaRedondea"},
                {"bcs_formato_puntos", "PuntosDecimales", "PuntosTrucaRedondea"}};
        for (String[] m : matrices) {
            JsonObject matriz = new JsonObject();
            for (JsonElement e : filas) {
                JsonObject fila = e.getAsJsonObject();
                if (fila.get("IdUsoMonedaFormato") == null) continue;
                JsonObject nodo = hijo(hijo(hijo(hijo(matriz, fila.get("IdUsoMonedaFormato").getAsString()), fila.get("IdMoneda").getAsString()),
                        fila.get("IdTipoInstrumento").getAsString()), fila.get("IdTipoOperacion").getAsString());
                nodo.addProperty(m[1], fila.get(m[1]).getAsInt());
                nodo.addProperty(m[2], fila.get(m[2]).getAsString());
            }
            out.add(m[0], matriz);
        }
    }

    private static JsonObject hijo(JsonObject padre, String clave) {
        if (padre.get(clave) == null) padre.add(clave, new JsonObject());
        return padre.get(clave).getAsJsonObject();
    }

    /** Legacy {@code Utils.getConfiguraciones}: {@code Modulo -> CodigoParametro -> Valor} for the company. */
    Map<String, Map<String, String>> configuraciones(Connection con, long idEmpresa) throws Exception {
        Map<String, Map<String, String>> mapa = new HashMap<>();
        for (Parametro p : SqlNombrado.consultar(con, sentencias.de(SQL_CONFIGURACIONES), parametros("IdEmpresa", idEmpresa), Parametro.class)) {
            mapa.computeIfAbsent(p.Modulo, k -> new HashMap<>()).put(p.CodigoParametro, p.Valor);
        }
        return mapa;
    }

    /** Legacy {@code getConfiguracionAsJson}: only the allowed parameters, and every parameter of the PRODUCTO module. */
    static JsonObject configuracionPublica(Map<String, Map<String, String>> configuracion) {
        JsonObject out = new JsonObject();
        for (Map.Entry<String, Map<String, String>> modulo : configuracion.entrySet()) {
            JsonObject m = new JsonObject();
            for (Map.Entry<String, String> p : modulo.getValue().entrySet()) {
                if (PARAMETROS_PERMITIDOS.contains(p.getKey()) || "PRODUCTO".equalsIgnoreCase(modulo.getKey())) {
                    m.addProperty(p.getKey(), p.getValue());
                }
            }
            out.add(modulo.getKey(), m);
        }
        return out;
    }

    /** Legacy {@code Utils.getFechaHoraSistemaLocal}: the local date and time of the system, from the procedure {@code ObtenerFechas}. */
    String fechaHoraLocal(Connection con, long idEmpresa) throws Exception {
        JsonObject in = new JsonObject();
        in.addProperty("p_IdEmpresa", idEmpresa);
        return procedimientos.llamar(con, SP_FECHAS, in).get("P_OUT_FECHAHORASISTEMALOCAL").getAsString();
    }

    /** The legacy wrote these two values raw into JavaScript: a number when it is one, else text; absent is null. */
    private static JsonElement numeroOTexto(String valor) {
        if (valor == null) return com.google.gson.JsonNull.INSTANCE;
        try {
            return new JsonPrimitive(new java.math.BigDecimal(valor.trim()));
        } catch (NumberFormatException e) {
            return new JsonPrimitive(valor);
        }
    }

    private static Map<String, Object> parametros(Object... pares) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            m.put((String) pares[i], pares[i + 1]);
        }
        return m;
    }
}
