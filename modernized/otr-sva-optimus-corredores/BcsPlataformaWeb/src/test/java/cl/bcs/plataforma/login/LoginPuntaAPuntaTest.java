package cl.bcs.plataforma.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.adapter.in.BootstrapController;
import cl.bcs.plataforma.adapter.in.CrearSesionController;
import cl.bcs.plataforma.login.bootstrap.BootstrapServicio;
import cl.bcs.plataforma.login.h2.LoginDb;
import cl.bcs.plataforma.login.h2.ProcedimientosGuionados;
import cl.bcs.plataforma.login.sesion.AuditoriaSesion;
import cl.bcs.plataforma.login.sesion.CrearSesionServicio;
import cl.bcs.plataforma.login.sesion.FiltroSesion;
import cl.bcs.plataforma.login.sesion.SesionDatos;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * G-03: the whole login journey of the MVP on H2, with no TomEE and no HANA, through the same controllers and filters the application registers
 * (the {@code /PLA} prefix as the gateway hands it over): open the login page, wrong passwords until the temporary block, the block holds even for
 * the right password, once it expires the right password opens a session, the home bootstrap with that token, a token that does not exist, no token.
 */
class LoginPuntaAPuntaTest {

    private static final String CLAVE_NAVEGADOR = sha256Hex("clave-de-prueba");
    private static final String CLAVE_GUARDADA = sha256Hex(CLAVE_NAVEGADOR).toUpperCase();
    private static final String USUARIO = "\"BSCL.Plataforma.BD::ACC.Usuario\"";

    @RestController
    static class Protegido {
        @GetMapping("/Protegido")
        String protegido() {
            return "dentro";
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
        ProcedimientosGuionados sp = new ProcedimientosGuionados();
        sp.responder("BSCL.Plataforma.BD.Utilitarios::ObtenerFechas", (c, in) -> ProcedimientosGuionados.salida("P_OUT_FECHAHORASISTEMALOCAL", "2026-03-02 06:30:00"));
        sp.responder("BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin", (c, in) -> ProcedimientosGuionados.salida("P_OUT_ESADMIN", "N"));
        sp.responder("BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion", (c, in) -> ProcedimientosGuionados.salida("P_OUT_VALOR", "N"));
        sp.responder("BSCL.Plataforma.BD.Servicios::ConsultarFormatos", (c, in) -> {
            JsonObject o = new JsonObject();
            o.add("P_OUT_MONEDAFORMATO", new JsonArray());
            return o;
        });
        UnidadDeTrabajo unidad = new UnidadDeTrabajo(db.dataSource());
        mvc = MockMvcBuilders.standaloneSetup(
                new CrearSesionController(unidad, new CrearSesionServicio(new AuditoriaSesion())),
                new BootstrapController(unidad, new BootstrapServicio(new SesionDatos(), sp)),
                new Protegido())
                .addFilters(new PrefijoPlaFilter(), new FiltroSesion(db.dataSource())).build();

        db.sql("INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.Ambiente', 'DES')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.SegundosZonaHoraria', '-10800')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.MuestraSSOGoogle', 'N')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.LocalizacionSistema', 'CL')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.Version', '3.2')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.ZonaHoraria', 'America/Santiago')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.MonedaSistema', 'CLP')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.BolsaDefectoSistema', 'BCS')",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" VALUES (1, 'Corredora Ficticia S.A.', 'ACT')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Empresa\" VALUES (1, 'Corredora Ficticia S.A.', 44, 100)",
                "INSERT INTO \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" VALUES (100, NULL, NULL, 777, 1)",
                "INSERT INTO \"BSCL.Plataforma.BD::INS.Corredora\" VALUES (50, 777)",
                "INSERT INTO " + USUARIO + " (\"IdUsuario\",\"IdEmpresa\",\"CodigoUsuario\",\"Clave\",\"FechaVigencia\",\"IdEstado\",\"IntentosConexion\","
                        + "\"FechaBloqueoTemporal\",\"EsAutorizado\",\"TipoEntidad\",\"IdEntidad\",\"UsuarioClientes\",\"UsuarioNegocios\",\"UsuarioServicios\","
                        + "\"UsuarioCorredores\",\"CodigoMenu\") VALUES (1,1,'JPEREZ','" + CLAVE_GUARDADA + "',DATE '0001-01-01','ACT',0,"
                        + "TIMESTAMP '0001-01-01 00:00:00','S','EMP',100,'N','N','N','S','MENU1')");
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

    private String intentar(String claveNavegador) throws Exception {
        String cuerpo = "{\"p_IdEmpresa\":1,\"p_NombreUsuario\":\"jperez\",\"p_ClaveUsuario\":\"" + claveNavegador + "\",\"p_IdCanal\":\"COR\"}";
        var r = mvc.perform(post("/PLA/CrearSesion").contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andReturn().getResponse();
        return r.getStatus() + " " + r.getContentAsString(StandardCharsets.UTF_8);
    }

    private String intentos() throws Exception {
        return db.valor("SELECT \"IntentosConexion\" FROM " + USUARIO + " WHERE \"IdUsuario\" = 1");
    }

    @Test
    @DisplayName("G-03 login journey: page, wrong passwords, temporary block, block expires, session, home, bad token, no token")
    void recorrido_completo() throws Exception {
        // 1. the login page opens without a session
        JsonObject login = JsonParser.parseString(mvc.perform(get("/PLA/bootstrap/login")).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8)).getAsJsonObject();
        assertThat(login.getAsJsonArray("empresas")).hasSize(1);

        // 2. two wrong passwords: 401 each, the counter survives the request
        assertThat(intentar(sha256Hex("mala"))).startsWith("401 ").contains("USUARIO_CLAVE_INCORRECTA");
        assertThat(intentos()).isEqualTo("1");
        assertThat(intentar(sha256Hex("mala"))).startsWith("401 ");
        assertThat(intentos()).isEqualTo("2");

        // 3. the third reaches the maximum: blocked for a while, counter back to 0
        assertThat(intentar(sha256Hex("mala"))).startsWith("401 ");
        assertThat(intentos()).isEqualTo("0");
        assertThat(db.valor("SELECT CASE WHEN \"FechaBloqueoTemporal\" > LOCALTIMESTAMP THEN 'futuro' ELSE 'pasado' END FROM " + USUARIO + " WHERE \"IdUsuario\" = 1"))
                .isEqualTo("futuro");

        // 4. the block holds for the right password too: no session is created
        assertThat(intentar(CLAVE_NAVEGADOR)).startsWith("401 ");
        assertThat(db.valor("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::ACC.Sesion\"")).isEqualTo("0");

        // 5. the block expires: the right password opens a session
        db.sql("UPDATE " + USUARIO + " SET \"FechaBloqueoTemporal\" = TIMESTAMP '2000-01-01 00:00:00' WHERE \"IdUsuario\" = 1");
        String ok = intentar(CLAVE_NAVEGADOR);
        assertThat(ok).startsWith("200 ");
        String token = JsonParser.parseString(ok.substring(4)).getAsJsonObject().get("P_OUT_TOKEN").getAsString();
        assertThat(token).isNotBlank();
        assertThat(db.valor("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"TokenValido\" = 'S'")).isEqualTo("1");

        // 6. the home bootstrap with that token
        var home = mvc.perform(post("/PLA/bootstrap/home").header("X-SESSION-TOKEN", token)).andExpect(status().isOk()).andReturn().getResponse();
        JsonObject datos = JsonParser.parseString(home.getContentAsString(StandardCharsets.UTF_8)).getAsJsonObject();
        assertThat(datos.getAsJsonObject("_contrato").get("nombre").getAsString()).isEqualTo("optimus-bootstrap");
        assertThat(datos.has("BCS_SESION")).isTrue();
        assertThat(datos.get("BCS_USER_MENU").getAsString()).isEqualTo("MENU1");

        // 7. a protected path: the token opens it, a token that does not exist gives 401, no header gives 400
        mvc.perform(get("/PLA/Protegido").header("X-SESSION-TOKEN", token)).andExpect(status().isOk());
        mvc.perform(post("/PLA/bootstrap/home").header("X-SESSION-TOKEN", "no-existe")).andExpect(status().isUnauthorized());
        mvc.perform(get("/PLA/Protegido").header("X-SESSION-TOKEN", "no-existe")).andExpect(status().isUnauthorized());
        mvc.perform(get("/PLA/Protegido")).andExpect(status().isBadRequest());
        mvc.perform(post("/PLA/bootstrap/home")).andExpect(status().isBadRequest());
    }
}
