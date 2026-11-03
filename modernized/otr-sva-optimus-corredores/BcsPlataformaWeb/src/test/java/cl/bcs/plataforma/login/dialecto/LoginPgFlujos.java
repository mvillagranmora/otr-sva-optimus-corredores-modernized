package cl.bcs.plataforma.login.dialecto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.adapter.in.BootstrapController;
import cl.bcs.plataforma.adapter.in.CrearSesionController;
import cl.bcs.plataforma.login.PrefijoPlaFilter;
import cl.bcs.plataforma.login.UnidadDeTrabajo;
import cl.bcs.plataforma.login.h2.LoginDb;
import cl.bcs.plataforma.login.h2.ProcedimientosGuionados;
import cl.bcs.plataforma.login.sesion.AuditoriaSesion;
import cl.bcs.plataforma.login.sesion.CrearSesionServicio;
import cl.bcs.plataforma.login.sesion.FiltroSesion;
import cl.bcs.plataforma.login.sesion.SesionDatos;
import cl.bcs.plataforma.login.bootstrap.BootstrapServicio;
import cl.bcs.plataforma.service.ProcedureDispatcherService;

/**
 * Captures what the CURRENT login code sends to JDBC: the texts of the constants of the six classes (reflection) and, flow by flow, every SQL text
 * the services hand to the connection (a recording proxy placed in front of the H2 bridge, so it sees the text BEFORE any H2 rewrite). The goldens
 * under {@code src/test/resources/loginpg/golden-hana/} were recorded with {@link #capturarConstantes()} and {@link #capturarFlujos()} on the code
 * before the dialect layer existed; in HANA mode the production code must keep sending the same texts.
 * Fake data only (users JPEREZ, passwords derived from the literal "clave-de-prueba").
 */
public final class LoginPgFlujos {

    private LoginPgFlujos() {
    }

    // ------------------------------------------------------------------------------------------------ constants by reflection

    public static Map<String, String> capturarConstantes() throws Exception {
        Map<String, String> m = new LinkedHashMap<>();
        for (String nombre : LoginPgRecursos.CLASES) {
            Class<?> c = Class.forName(nombre);
            List<String> ids = new ArrayList<>();
            Map<String, String> locales = new LinkedHashMap<>();
            for (Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (Modifier.isStatic(mod) && Modifier.isFinal(mod) && f.getType() == String.class && LoginPgRecursos.CONSTANTE_SQL.matcher(f.getName()).matches()) {
                    f.setAccessible(true);
                    locales.put(c.getSimpleName() + "." + f.getName(), (String) f.get(null));
                }
            }
            ids.addAll(locales.keySet());
            ids.sort(String::compareTo);
            for (String id : ids) m.put(id, locales.get(id));
        }
        return m;
    }

    // ------------------------------------------------------------------------------------------------ recording proxies

    /** a DataSource whose connections record the SQL texts handed to prepareStatement / prepareCall / Statement.execute* / addBatch */
    static DataSource grabador(DataSource real, List<String> destino) {
        return (DataSource) Proxy.newProxyInstance(LoginPgFlujos.class.getClassLoader(), new Class<?>[] { DataSource.class }, (p, m, a) -> {
            if (m.getName().equals("getConnection")) {
                return conexionGrabadora((Connection) m.invoke(real, a), destino);
            }
            return m.invoke(real, a);
        });
    }

    static Connection conexionGrabadora(Connection real, List<String> destino) {
        return (Connection) Proxy.newProxyInstance(LoginPgFlujos.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            String n = m.getName();
            if ((n.equals("prepareStatement") || n.equals("prepareCall") || n.equals("nativeSQL")) && a != null && a.length > 0 && a[0] instanceof String s) {
                destino.add(s);
            }
            Object r;
            try {
                r = m.invoke(real, a);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getCause();
            }
            if (n.equals("createStatement")) {
                java.sql.Statement st = (java.sql.Statement) r;
                return Proxy.newProxyInstance(LoginPgFlujos.class.getClassLoader(), new Class<?>[] { java.sql.Statement.class }, (p2, m2, a2) -> {
                    String n2 = m2.getName();
                    if ((n2.equals("execute") || n2.equals("executeUpdate") || n2.equals("executeQuery") || n2.equals("addBatch")) && a2 != null && a2.length > 0
                            && a2[0] instanceof String s) {
                        destino.add(s);
                    }
                    try {
                        return m2.invoke(st, a2);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
            }
            return r;
        });
    }

    // ------------------------------------------------------------------------------------------------ flows

    private static String sha256Hex(String s) throws Exception {
        byte[] h = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : h) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @RestController
    static class Protegido {
        @GetMapping("/Protegido")
        String protegido() {
            return "dentro";
        }
    }

    private static final String USUARIO = "INSERT INTO \"BSCL.Plataforma.BD::ACC.Usuario\" (\"IdUsuario\",\"IdEmpresa\",\"CodigoUsuario\",\"Clave\",\"FechaVigencia\",\"IdEstado\","
            + "\"IntentosConexion\",\"FechaBloqueoTemporal\",\"EsAutorizado\",\"TipoEntidad\",\"IdEntidad\",\"UsuarioClientes\",\"UsuarioNegocios\","
            + "\"UsuarioServicios\",\"UsuarioCorredores\",\"CodigoMenu\") VALUES (1,1,'JPEREZ','%s',DATE '0001-01-01','ACT',%d,"
            + "TIMESTAMP '0001-01-01 00:00:00','S','EMP',100,'N','N','N','S','MENU1')";

    private static String cuerpo(String clave) {
        return "{\"p_IdEmpresa\":1,\"p_NombreUsuario\":\"jperez\",\"p_ClaveUsuario\":\"" + clave + "\",\"p_IdCanal\":\"COR\"}";
    }

    static void utc() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        org.h2.util.DateTimeUtils.resetCalendar();
    }

    public static Map<String, List<String>> capturarFlujos() throws Exception {
        utc();
        Map<String, List<String>> r = new LinkedHashMap<>();
        r.put("login-exito-dos-veces-y-filtro", loginExito());
        r.put("login-clave-mala-hasta-bloqueo", loginBloqueo());
        r.put("clave-actualizar-y-provisoria", clave());
        r.put("bootstrap-login", bootstrap("login"));
        r.put("bootstrap-home", bootstrap("home"));
        r.put("procedimiento-llamada", llamadaProcedimiento());
        return r;
    }

    private static List<String> distintos(List<String> l) {
        return new ArrayList<>(new LinkedHashSet<>(l));
    }

    private static List<String> loginExito() throws Exception {
        String navegador = sha256Hex("clave-de-prueba");
        String guardada = sha256Hex(navegador).toUpperCase();
        try (LoginDb db = LoginDb.nueva()) {
            db.sql(String.format(USUARIO, guardada, 2));
            List<String> sql = new ArrayList<>();
            DataSource ds = grabador(db.dataSource(), sql);
            MockMvc mvc = MockMvcBuilders.standaloneSetup(new CrearSesionController(new UnidadDeTrabajo(ds), new CrearSesionServicio(new AuditoriaSesion())), new Protegido())
                    .addFilters(new PrefijoPlaFilter(), new FiltroSesion(ds)).build();
            String token = null;
            for (int i = 0; i < 2; i++) {
                String resp = mvc.perform(post("/PLA/CrearSesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo(navegador))).andReturn().getResponse().getContentAsString();
                token = JsonParser.parseString(resp).getAsJsonObject().get("P_OUT_TOKEN").getAsString();
            }
            mvc.perform(get("/PLA/Protegido").header("X-SESSION-TOKEN", token)).andReturn();
            return distintos(sql);
        }
    }

    private static List<String> loginBloqueo() throws Exception {
        String navegador = sha256Hex("clave-de-prueba");
        String guardada = sha256Hex(navegador).toUpperCase();
        try (LoginDb db = LoginDb.nueva()) {
            db.sql(String.format(USUARIO, guardada, 0));
            List<String> sql = new ArrayList<>();
            DataSource ds = grabador(db.dataSource(), sql);
            MockMvc mvc = MockMvcBuilders.standaloneSetup(new CrearSesionController(new UnidadDeTrabajo(ds), new CrearSesionServicio(new AuditoriaSesion())))
                    .addFilters(new PrefijoPlaFilter(), new FiltroSesion(ds)).build();
            for (int i = 0; i < 4; i++) {
                mvc.perform(post("/PLA/CrearSesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo(sha256Hex("mala")))).andReturn();
            }
            return distintos(sql);
        }
    }

    /** ActualizarClave with the session of the user, then ValidaClaveProvisoria with a right and a wrong provisional password */
    private static List<String> clave() throws Exception {
        try (LoginDb db = LoginDb.nueva()) {
            db.sql(String.format(USUARIO, "VIEJA", 0),
                    "INSERT INTO \"BSCL.Plataforma.BD::ACC.Sesion\" (\"IdTokenSesion\",\"IdUsuario\",\"TokenValido\",\"FechaUltimoUso\",\"FechaIngreso\",\"IdCanal\") "
                            + "VALUES ('TOKEN-JPEREZ',1,'S',TIMESTAMP '2026-03-02 08:00:00',TIMESTAMP '2026-03-02 08:00:00','COR')",
                    "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"ClaveProvisoria\" = '" + cl.bcs.plataforma.login.clave.GestionClaveServicio.encryptPassword("Tmp#9x")
                            + "', \"FechaClaveProvisoria\" = TIMESTAMP '2026-03-02 00:00:00', \"IntentosConexionClaveProvisoria\" = 1 WHERE \"IdUsuario\" = 1");
            List<String> sql = new ArrayList<>();
            DataSource ds = grabador(db.dataSource(), sql);
            MockMvc mvc = MockMvcBuilders.standaloneSetup(new cl.bcs.plataforma.adapter.in.PasswordController(new UnidadDeTrabajo(ds),
                    new cl.bcs.plataforma.login.clave.GestionClaveServicio(new ProcedimientosGuionados()))).addFilters(new PrefijoPlaFilter(), new FiltroSesion(ds)).build();
            mvc.perform(post("/PLA/ValidaClaveProvisoria").contentType(MediaType.APPLICATION_JSON).content("{\"p_CodigoUsuario\":\"JPEREZ\",\"p_ClaveProvisoria\":\"mala\"}")).andReturn();
            mvc.perform(post("/PLA/ValidaClaveProvisoria").contentType(MediaType.APPLICATION_JSON).content("{\"p_CodigoUsuario\":\"JPEREZ\",\"p_ClaveProvisoria\":\"Tmp#9x\"}")).andReturn();
            mvc.perform(post("/PLA/ActualizarClave").header("X-SESSION-TOKEN", "TOKEN-JPEREZ").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"p_CodigoUsuario\":\"JPEREZ\",\"Clave1\":\"Clave-1234\",\"Clave2\":\"Clave-1234\"}")).andReturn();
            return distintos(sql);
        }
    }

    private static final String TOKEN = "TOKEN-DE-PRUEBA";

    private static List<String> bootstrap(String cual) throws Exception {
        try (LoginDb db = LoginDb.nueva()) {
            ProcedimientosGuionados sp = new ProcedimientosGuionados();
            sp.responder("BSCL.Plataforma.BD.Utilitarios::ObtenerFechas", (c, in) -> ProcedimientosGuionados.salida("P_OUT_FECHAHORASISTEMALOCAL", "2026-03-02 06:30:00"));
            sp.responder("BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin", (c, in) -> ProcedimientosGuionados.salida("P_OUT_ESADMIN", "N"));
            sp.responder("BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion", (c, in) -> ProcedimientosGuionados.salida("P_OUT_VALOR", "N"));
            sp.responder("BSCL.Plataforma.BD.Servicios::ConsultarFormatos", (c, in) -> {
                JsonObject o = new JsonObject();
                o.add("P_OUT_MONEDAFORMATO", new JsonArray());
                return o;
            });
            db.sql(SemillaLoginPg.bootstrapHana());
            List<String> sql = new ArrayList<>();
            DataSource ds = grabador(db.dataSource(), sql);
            MockMvc mvc = MockMvcBuilders.standaloneSetup(new BootstrapController(new UnidadDeTrabajo(ds), new BootstrapServicio(new SesionDatos(), sp)))
                    .addFilters(new PrefijoPlaFilter(), new FiltroSesion(ds)).build();
            if (cual.equals("login")) {
                mvc.perform(get("/PLA/bootstrap/login")).andReturn();
            } else {
                mvc.perform(post("/PLA/bootstrap/home").header("X-SESSION-TOKEN", TOKEN)).andReturn();
            }
            return distintos(sql);
        }
    }

    /** the text of the call of a stored procedure and of the introspection query (ProcedureDispatcherService), with a scripted catalog and connection */
    private static List<String> llamadaProcedimiento() throws Exception {
        List<String> sql = new ArrayList<>();
        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(new JdbcTemplate()) {
            @Override
            public List<Map<String, Object>> queryForList(String texto, SqlParameterSource origen) {
                sql.add(texto);
                String proc = String.valueOf(origen.getValue("procName"));
                if (proc.endsWith("::Sin")) return List.of();
                return List.of(fila("P_IN_UNO", "NVARCHAR", "IN", 1), fila("P_IN_DOS", "BIGINT", "IN", 2), fila("P_OUT_TRES", "NVARCHAR", "OUT", 3),
                        fila("P_OUT_TABLA", "TABLE_TYPE", "OUT", 4));
            }
        };
        ProcedureDispatcherService svc = new ProcedureDispatcherService(null, jdbc);
        Connection con = conexionGrabadora(conexionFalsa(), sql);
        JsonObject entrada = new JsonObject();
        entrada.addProperty("P_IN_UNO", "x");
        entrada.addProperty("P_IN_DOS", 7);
        svc.llamar(con, "BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin", entrada);
        svc.llamar(con, "BSCL.Plataforma.BD.Utilitarios::Sin", new JsonObject());
        return distintos(sql);
    }

    private static Map<String, Object> fila(String nombre, String tipo, String modo, int pos) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("PARAMETER_NAME", nombre);
        f.put("DATA_TYPE_NAME", tipo);
        f.put("PARAMETER_TYPE", modo);
        f.put("POSITION", pos);
        return f;
    }

    /** a connection that only knows prepareCall: the callable statement answers every call with a neutral value */
    private static Connection conexionFalsa() {
        return (Connection) Proxy.newProxyInstance(LoginPgFlujos.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            if (m.getName().equals("prepareCall")) {
                return Proxy.newProxyInstance(LoginPgFlujos.class.getClassLoader(), new Class<?>[] { CallableStatement.class }, (p2, m2, a2) -> {
                    Class<?> t = m2.getReturnType();
                    if (t == String.class) return "x";
                    if (t == boolean.class) return false;
                    if (t == int.class) return 0;
                    if (t == long.class) return 0L;
                    return null;
                });
            }
            if (m.getName().equals("close")) return null;
            throw new UnsupportedOperationException(m.getName());
        });
    }
}
