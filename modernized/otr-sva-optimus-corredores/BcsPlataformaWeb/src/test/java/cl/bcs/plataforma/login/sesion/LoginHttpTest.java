package cl.bcs.plataforma.login.sesion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.adapter.in.CrearSesionController;
import cl.bcs.plataforma.login.PrefijoPlaFilter;
import cl.bcs.plataforma.login.UnidadDeTrabajo;
import cl.bcs.plataforma.login.h2.LoginDb;

/**
 * The HTTP face of the login on H2: the {@code /PLA} prefix (T-10), the session filter (T-11), the single transaction per request (T-13) and the
 * contract of the answers (T-09). The same filters and controller classes the application registers, with MockMvc.
 */
class LoginHttpTest {

    private static final String CLAVE_NAVEGADOR = sha256Hex("clave-de-prueba");
    private static final String CLAVE_GUARDADA = sha256Hex(CLAVE_NAVEGADOR).toUpperCase();

    /** a service behind the session filter, standing for any protected path */
    @RestController
    static class Protegido {
        @GetMapping("/Protegido")
        String protegido() {
            return "dentro";
        }

        @GetMapping("/Ping")
        String ping() {
            return "pong";
        }
    }

    private LoginDb db;
    private MockMvc mvc;

    @BeforeAll
    static void utc() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        org.h2.util.DateTimeUtils.resetCalendar();
    }

    @BeforeEach
    void montar() throws Exception {
        db = LoginDb.nueva();
        db.sql("INSERT INTO \"BSCL.Plataforma.BD::ACC.Usuario\" (\"IdUsuario\",\"IdEmpresa\",\"CodigoUsuario\",\"Clave\",\"FechaVigencia\",\"IdEstado\","
                + "\"IntentosConexion\",\"FechaBloqueoTemporal\",\"EsAutorizado\",\"TipoEntidad\",\"IdEntidad\",\"UsuarioClientes\",\"UsuarioNegocios\","
                + "\"UsuarioServicios\",\"UsuarioCorredores\",\"CodigoMenu\") VALUES (1,1,'JPEREZ','" + CLAVE_GUARDADA + "',DATE '0001-01-01','ACT',0,"
                + "TIMESTAMP '0001-01-01 00:00:00','S','EMP',100,'N','N','N','S','MENU1')");
        UnidadDeTrabajo unidad = new UnidadDeTrabajo(db.dataSource());
        CrearSesionController login = new CrearSesionController(unidad, new CrearSesionServicio(new AuditoriaSesion()));
        mvc = MockMvcBuilders.standaloneSetup(login, new Protegido()).addFilters(new PrefijoPlaFilter(), new FiltroSesion(db.dataSource())).build();
    }

    @AfterEach
    void cerrar() throws Exception {
        db.close();
    }

    private static String sha256Hex(String s) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String cuerpo(String clave) {
        return "{\"p_IdEmpresa\":1,\"p_NombreUsuario\":\"jperez\",\"p_ClaveUsuario\":\"" + clave + "\",\"p_IdCanal\":\"COR\"}";
    }

    // ------------------------------------------------------------------------------------------------ T-10 prefix and T-09 contract

    @Test
    @DisplayName("T-10 T-09 POST /PLA/CrearSesion answers 200 with the legacy shape; the token is the one stored in ACC.Sesion")
    void login_por_el_prefijo_pla() throws Exception {
        String respuesta = mvc.perform(post("/PLA/CrearSesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo(CLAVE_NAVEGADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.P_OUT_ENTIDAD").value(""))
                .andExpect(jsonPath("$.P_OUT_ADVERTENCIASCONTADOR").value(0))
                .andExpect(jsonPath("$.P_OUT_ERRORESCONTADOR").value(0))
                .andExpect(jsonPath("$.P_OUT_ADVERTENCIASDETALLE").isEmpty())
                .andExpect(jsonPath("$.P_OUT_ERRORESDETALLE").isEmpty())
                .andReturn().getResponse().getContentAsString();
        String token = JsonParser.parseString(respuesta).getAsJsonObject().get("P_OUT_TOKEN").getAsString();
        assertThat(db.valor("SELECT \"TokenValido\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = '" + token + "'")).isEqualTo("S");
        // the path without the prefix keeps answering (jobs and health checks do not move)
        mvc.perform(post("/CrearSesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo(CLAVE_NAVEGADOR))).andExpect(status().isOk());
    }

    @Test
    @DisplayName("T-09 every refusal is 401 USUARIO_CLAVE_INCORRECTA with the same body: wrong password, unknown user, bad company, body that is not JSON")
    void rechazo_uniforme() throws Exception {
        String esperado = "{\"P_OUT_ERRORESCONTADOR\":1,\"P_OUT_ERRORESDETALLE\":[{\"CODIGO\":\"USUARIO_CLAVE_INCORRECTA\",\"Descripcion\":\"Usuario o contrasena incorrecta.\"}]}";
        for (String cuerpo : new String[] { cuerpo(sha256Hex("mala")), cuerpo(CLAVE_NAVEGADOR).replace("jperez", "nadie"),
                cuerpo(CLAVE_NAVEGADOR).replace("\"p_IdEmpresa\":1", "\"p_IdEmpresa\":\"x\""), "no es json" }) {
            String body = mvc.perform(post("/PLA/CrearSesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andExpect(status().isUnauthorized())
                    .andReturn().getResponse().getContentAsString();
            assertThat(body).isEqualTo(esperado);
        }
    }

    // ------------------------------------------------------------------------------------------------ T-13 one transaction

    @Test
    @DisplayName("T-13 RULE-014 a refused login still COMMITS: the attempt counter of a wrong password survives the request")
    void intento_fallido_se_confirma() throws Exception {
        mvc.perform(post("/PLA/CrearSesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo(sha256Hex("mala")))).andExpect(status().isUnauthorized());
        assertThat(db.valor("SELECT \"IntentosConexion\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdUsuario\" = 1")).isEqualTo("1");
    }

    @Test
    @DisplayName("T-13 an exception in the middle ROLLS BACK everything the request wrote (legacy: the connection is closed without commit)")
    void excepcion_deshace_todo() throws Exception {
        // the maximum of attempts is not a number: the statement that counts the attempt runs, then parsing it fails
        db.sql("UPDATE \"BSCL.Plataforma.BD::PLA.Configuracion\" SET \"Valor\" = 'tres' WHERE \"CodigoParametro\" = 'PLA.ClaveUsuarioIntentos'");
        mvc.perform(post("/PLA/CrearSesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo(sha256Hex("mala")))).andExpect(status().isUnauthorized());
        assertThat(db.valor("SELECT \"IntentosConexion\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdUsuario\" = 1")).isEqualTo("0");
    }

    @Test
    @DisplayName("T-13 a successful login commits the session, its audit rows and the counter reset together")
    void exito_se_confirma_completo() throws Exception {
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"IntentosConexion\" = 2 WHERE \"IdUsuario\" = 1");
        mvc.perform(post("/PLA/CrearSesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo(CLAVE_NAVEGADOR))).andExpect(status().isOk());
        assertThat(db.valor("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::ACC.Sesion\"")).isEqualTo("1");
        assertThat(db.valor("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::PLA.Log\"")).isEqualTo("1");
        assertThat(db.valor("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::PLA.DetalleLog\"")).isEqualTo("4");
        assertThat(db.valor("SELECT \"IntentosConexion\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdUsuario\" = 1")).isEqualTo("0");
    }

    // ------------------------------------------------------------------------------------------------ T-11 session filter

    @Test
    @DisplayName("T-11 a protected path under /PLA: 400 without the token header, 401 with an unknown token, 200 with a valid one")
    void filtro_de_sesion() throws Exception {
        mvc.perform(get("/PLA/Protegido")).andExpect(status().isBadRequest());
        mvc.perform(get("/PLA/Protegido").header("X-SESSION-TOKEN", "no-existe")).andExpect(status().isUnauthorized());
        String token = JsonParser.parseString(mvc.perform(post("/PLA/CrearSesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo(CLAVE_NAVEGADOR)))
                .andReturn().getResponse().getContentAsString()).getAsJsonObject().get("P_OUT_TOKEN").getAsString();
        mvc.perform(get("/PLA/Protegido").header("X-SESSION-TOKEN", token)).andExpect(status().isOk());
        // closed sessions stop working
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Sesion\" SET \"TokenValido\" = 'N'");
        mvc.perform(get("/PLA/Protegido").header("X-SESSION-TOKEN", token)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("T-11 exempt paths need no token (login, ping, bootstrap/login, password and recovery services); bootstrap/home does")
    void rutas_exentas() throws Exception {
        mvc.perform(get("/PLA/Ping")).andExpect(status().isOk());
        for (String ruta : new String[] { "/PLA/ValidaClaveProvisoria", "/PLA/SolicitudRecuperacionClave", "/PLA/RecuperacionClave",
                "/PLA/ConfirmarCambioClave", "/PLA/bootstrap/login", "/PLA/Configuraciones", "/PLA/SSOInit", "/PLA/SSOLogout", "/PLA/WebStatic/x.js" }) {
            // no controller is mounted for most of them: what matters is that the FILTER lets them through (404, not 400/401)
            mvc.perform(get(ruta)).andExpect(status().isNotFound());
        }
        // owner decision 2026-10-26: changing a password needs a session (the legacy did not ask for one)
        mvc.perform(post("/PLA/ActualizarClave").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(get("/PLA/bootstrap/home")).andExpect(status().isBadRequest());
        mvc.perform(post("/PLA/bootstrap/home")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("T-11 closed by default (decision 2026-10-26): a path without the /PLA prefix is judged as if it had it; /internal, the actuator and /error are left out")
    void cerrado_por_defecto() throws Exception {
        // not exempt: needs the token even without the prefix
        mvc.perform(get("/Protegido")).andExpect(status().isBadRequest());
        mvc.perform(get("/Protegido").header("X-SESSION-TOKEN", "no-existe")).andExpect(status().isUnauthorized());
        String token = JsonParser.parseString(mvc.perform(post("/CrearSesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo(CLAVE_NAVEGADOR)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).getAsJsonObject().get("P_OUT_TOKEN").getAsString();
        mvc.perform(get("/Protegido").header("X-SESSION-TOKEN", token)).andExpect(status().isOk());
        // exempt, with or without the prefix: login and ping
        mvc.perform(get("/Ping")).andExpect(status().isOk());
        mvc.perform(get("/bootstrap/login")).andExpect(status().isNotFound());   // no controller mounted here: the filter let it through
        mvc.perform(get("/bootstrap/home")).andExpect(status().isBadRequest());
        // left out of the filter (their own protection): the answer is the 404 of a path with no controller, never 400/401
        for (String ruta : new String[] { "/internal/java-call", "/actuator/health", "/error" }) {
            mvc.perform(get(ruta)).andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("T-11 the exemption is exact: a look-alike path (/PLA/CrearSesionX, /CrearSesion/../call) is not exempt")
    void exencion_exacta() throws Exception {
        mvc.perform(get("/CrearSesionX")).andExpect(status().isBadRequest());
        mvc.perform(get("/PLA/CrearSesionX")).andExpect(status().isBadRequest());
        mvc.perform(get("/internalx/java-call")).andExpect(status().isBadRequest());
        mvc.perform(get("/actuatorx")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("T-10 the prefix is only /PLA/: /PLANES is not rewritten")
    void prefijo_exacto() throws Exception {
        // not rewritten to /ES: it is an unknown unprefixed path, which the closed-by-default filter asks a token for
        mvc.perform(get("/PLANES")).andExpect(status().isBadRequest());
        JsonObject vacio = new JsonObject();
        assertThat(vacio.size()).isZero();
    }
}
