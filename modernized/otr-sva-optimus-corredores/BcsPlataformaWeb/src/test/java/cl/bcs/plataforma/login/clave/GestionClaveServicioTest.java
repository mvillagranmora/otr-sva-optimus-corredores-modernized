package cl.bcs.plataforma.login.clave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Statement;
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

import cl.bcs.plataforma.adapter.in.PasswordController;
import cl.bcs.plataforma.login.PrefijoPlaFilter;
import cl.bcs.plataforma.login.UnidadDeTrabajo;
import cl.bcs.plataforma.login.h2.LoginDb;
import cl.bcs.plataforma.login.h2.ProcedimientosGuionados;
import cl.bcs.plataforma.login.sesion.AuditoriaSesion;
import cl.bcs.plataforma.login.sesion.CrearSesionServicio;
import cl.bcs.plataforma.login.sesion.FiltroSesion;

/**
 * Password services of the login on H2 (T-12, T-24..T-27) through the same controller the application registers. The stored procedures are scripted
 * ({@link ProcedimientosGuionados}). Rules: RULE-029..040 of BUSINESS_RULES.md; RULE-032 (digits) and RULE-040 (attempts) are the reviewed ones.
 */
class GestionClaveServicioTest {

    private LoginDb db;
    private ProcedimientosGuionados sp;
    private MockMvc mvc;

    @BeforeAll
    static void utc() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        org.h2.util.DateTimeUtils.resetCalendar();
    }

    @BeforeEach
    void montar() throws Exception {
        db = LoginDb.nueva();
        sp = new ProcedimientosGuionados();
        UnidadDeTrabajo unidad = new UnidadDeTrabajo(db.dataSource());
        mvc = MockMvcBuilders.standaloneSetup(new PasswordController(unidad, new GestionClaveServicio(sp)))
                .addFilters(new PrefijoPlaFilter(), new FiltroSesion(db.dataSource())).build();
        // an employee user JPEREZ whose password is "Clave-1234" (what the browser sends is its SHA-256; the stored value is SHA-256 of that)
        db.sql("INSERT INTO \"BSCL.Plataforma.BD::ACC.Usuario\" (\"IdUsuario\",\"IdEmpresa\",\"CodigoUsuario\",\"Clave\",\"FechaVigencia\",\"IdEstado\","
                + "\"IntentosConexion\",\"FechaBloqueoTemporal\",\"EsAutorizado\",\"TipoEntidad\",\"IdEntidad\",\"UsuarioClientes\",\"UsuarioNegocios\","
                + "\"UsuarioServicios\",\"UsuarioCorredores\",\"DebeCambiarClave\") VALUES (1,1,'JPEREZ','VIEJA',DATE '0001-01-01','PEN',0,"
                + "TIMESTAMP '0001-01-01 00:00:00','S','EMP',100,'N','N','N','S','S')");
        db.sql("INSERT INTO \"BSCL.Plataforma.BD::ACC.Sesion\" (\"IdTokenSesion\",\"IdUsuario\",\"TokenValido\",\"FechaUltimoUso\",\"FechaIngreso\",\"IdCanal\") "
                + "VALUES ('TOKEN-JPEREZ',1,'S',TIMESTAMP '2026-03-02 08:00:00',TIMESTAMP '2026-03-02 08:00:00','COR')");
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

    private JsonObject llamar(String ruta, String cuerpo) throws Exception {
        String r = mvc.perform(post(ruta).header("X-SESSION-TOKEN", "TOKEN-JPEREZ").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return r.isEmpty() ? null : JsonParser.parseString(r).getAsJsonObject();
    }

    private String usuarioCampo(String campo) throws Exception {
        return db.valor("SELECT \"" + campo + "\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdUsuario\" = 1");
    }

    // ------------------------------------------------------------------------------------------------ ActualizarClave needs a session (owner decision)

    @Test
    @DisplayName("ActualizarClave needs a session: no token 400, an unknown or closed token 401, and the session must belong to the user whose password changes (403)")
    void actualizar_clave_exige_sesion() throws Exception {
        String cuerpo = "{\"p_CodigoUsuario\":\"JPEREZ\",\"Clave1\":\"Clave-1234\",\"Clave2\":\"Clave-1234\"}";
        String antes = usuarioCampo("Clave");
        mvc.perform(post("/PLA/ActualizarClave").contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andExpect(status().isBadRequest());
        mvc.perform(post("/PLA/ActualizarClave").header("X-SESSION-TOKEN", "no-existe").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isUnauthorized());
        // a valid session of ANOTHER user cannot change JPEREZ's password
        db.sql("INSERT INTO \"BSCL.Plataforma.BD::ACC.Usuario\" (\"IdUsuario\",\"IdEmpresa\",\"CodigoUsuario\",\"Clave\",\"FechaVigencia\",\"IdEstado\",\"IntentosConexion\","
                + "\"FechaBloqueoTemporal\",\"EsAutorizado\",\"TipoEntidad\",\"IdEntidad\") VALUES (2,1,'OTRO','X',DATE '0001-01-01','ACT',0,TIMESTAMP '0001-01-01 00:00:00','S','EMP',200)",
                "INSERT INTO \"BSCL.Plataforma.BD::ACC.Sesion\" (\"IdTokenSesion\",\"IdUsuario\",\"TokenValido\",\"FechaUltimoUso\",\"FechaIngreso\",\"IdCanal\") "
                        + "VALUES ('TOKEN-OTRO',2,'S',TIMESTAMP '2026-03-02 08:00:00',TIMESTAMP '2026-03-02 08:00:00','COR')");
        mvc.perform(post("/PLA/ActualizarClave").header("X-SESSION-TOKEN", "TOKEN-OTRO").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());
        assertThat(usuarioCampo("Clave")).isEqualTo(antes);
        // the user's own session works, in any letter case of the user code
        mvc.perform(post("/PLA/ActualizarClave").header("X-SESSION-TOKEN", "TOKEN-JPEREZ").contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo.replace("JPEREZ", "jperez"))).andExpect(status().isOk());
        assertThat(usuarioCampo("Clave")).isEqualTo(GestionClaveServicio.encryptPassword("Clave-1234"));
        // and once the session is closed it does not work any more
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Sesion\" SET \"TokenValido\" = 'N' WHERE \"IdTokenSesion\" = 'TOKEN-JPEREZ'");
        mvc.perform(post("/PLA/ActualizarClave").header("X-SESSION-TOKEN", "TOKEN-JPEREZ").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------------------------------------ ActualizarClave (T-12)

    @Test
    @DisplayName("T-12 RULE-029/037 ActualizarClave stores the DOUBLE SHA-256 (upper case), activates the user and the user can then log in with the browser hash")
    void actualizar_clave_guarda_como_el_legacy() throws Exception {
        JsonObject r = llamar("/PLA/ActualizarClave", "{\"p_CodigoUsuario\":\"jperez\",\"Clave1\":\"Clave-1234\",\"Clave2\":\"Clave-1234\"}");
        assertThat(r.get("Estado").getAsString()).isEqualTo("OK");
        assertThat(r.get("status").getAsString()).isEqualTo("Contrasena actualizada correctamente.");
        assertThat(usuarioCampo("Clave")).isEqualTo(sha256Hex(sha256Hex("Clave-1234")).toUpperCase());
        assertThat(usuarioCampo("IdEstado")).isEqualTo("ACT");
        assertThat(usuarioCampo("DebeCambiarClave")).isEqualTo("N");
        assertThat(usuarioCampo("FechaIngreso")).isEqualTo("2026-03-02");
        assertThat(usuarioCampo("UltimoCambioClave")).isEqualTo("2026-03-02");
        // the login of the same user: the browser sends SHA-256 of the typed password
        CrearSesionServicio login = new CrearSesionServicio(new AuditoriaSesion());
        var sesion = login.crearSesionUsuario(db.con(), 1L, "JPEREZ", sha256Hex("Clave-1234"), "COR");
        db.con().commit();
        assertThat(sesion.entidad).isEqualTo("EMP");
    }

    @Test
    @DisplayName("T-12 a different confirmation, an empty password and an empty body are answered NOK with the dictionary text and write nothing")
    void actualizar_clave_rechazos_simples() throws Exception {
        assertThat(llamar("/PLA/ActualizarClave", "{\"p_CodigoUsuario\":\"JPEREZ\",\"Clave1\":\"Clave-1234\",\"Clave2\":\"otra\"}").toString())
                .isEqualTo("{\"Estado\":\"NOK\",\"status\":\"La contrasena y su confirmacion no son iguales.\"}");
        assertThat(llamar("/PLA/ActualizarClave", "{\"p_CodigoUsuario\":\"JPEREZ\",\"Clave1\":\"\",\"Clave2\":\"\"}").toString())
                .isEqualTo("{\"Estado\":\"NOK\",\"status\":\"Contrasena invalida.\"}");
        assertThat(mvc.perform(post("/PLA/ActualizarClave").header("X-SESSION-TOKEN", "TOKEN-JPEREZ").contentType(MediaType.APPLICATION_JSON).content("")).andReturn().getResponse().getContentAsString())
                .isEqualTo("{\"Estado\":\"NOK\",\"status\":\"Contrasena invalida.\"}");
        assertThat(usuarioCampo("Clave")).isEqualTo("VIEJA");
    }

    @Test
    @DisplayName("T-12 RULE-031 a weak password: NOK and the status lists the heading and EVERY criterion (as the legacy), nothing is written")
    void actualizar_clave_politica() throws Exception {
        JsonObject r = llamar("/PLA/ActualizarClave", "{\"p_CodigoUsuario\":\"JPEREZ\",\"Clave1\":\"abc\",\"Clave2\":\"abc\"}");
        assertThat(r.get("Estado").getAsString()).isEqualTo("NOK");
        JsonArray status = r.getAsJsonArray("status");
        assertThat(status.get(0).getAsString()).isEqualTo("La contraseña debe contener:");
        assertThat(status.size()).isEqualTo(8);   // heading + 7 criteria (prohibited, length, valid characters, upper, lower, special, digit)
        assertThat(status.get(2).getAsString()).isEqualTo("- Mínimo 8 carácteres");
        assertThat(usuarioCampo("Clave")).isEqualTo("VIEJA");
    }

    @Test
    @DisplayName("T-12 RULE-034 a prohibited word (exact match of the whole password) is refused; RULE-032 a password without digit is refused (approved separate rule)")
    void actualizar_clave_prohibida_y_digitos() throws Exception {
        db.sql("INSERT INTO \"BSCL.Plataforma.BD::ACC.PasswordProhibidas\" VALUES ('Prohibida-1')");
        assertThat(llamar("/PLA/ActualizarClave", "{\"p_CodigoUsuario\":\"JPEREZ\",\"Clave1\":\"Prohibida-1\",\"Clave2\":\"Prohibida-1\"}").get("Estado").getAsString())
                .isEqualTo("NOK");
        assertThat(llamar("/PLA/ActualizarClave", "{\"p_CodigoUsuario\":\"JPEREZ\",\"Clave1\":\"Sin-digitos\",\"Clave2\":\"Sin-digitos\"}").get("Estado").getAsString())
                .isEqualTo("NOK");
        assertThat(llamar("/PLA/ActualizarClave", "{\"p_CodigoUsuario\":\"JPEREZ\",\"Clave1\":\"prohibida-1\",\"Clave2\":\"prohibida-1\"}").get("Estado").getAsString())
                .isEqualTo("NOK");   // fails for the missing capital, not for the word (the match is case sensitive)
    }

    @Test
    @DisplayName("T-12 when the policy cannot be read the answer is NOK with an empty status list and nothing is written")
    void actualizar_clave_politica_ilegible() throws Exception {
        db.sql("DELETE FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\" = 'ACC.numLongitud'");
        assertThat(llamar("/PLA/ActualizarClave", "{\"p_CodigoUsuario\":\"JPEREZ\",\"Clave1\":\"Clave-1234\",\"Clave2\":\"Clave-1234\"}").toString())
                .isEqualTo("{\"Estado\":\"NOK\",\"status\":[]}");
        assertThat(usuarioCampo("Clave")).isEqualTo("VIEJA");
    }

    @Test
    @DisplayName("T-12 a code the dictionary lacks is answered with an empty body (as the legacy)")
    void actualizar_clave_sin_texto_en_el_diccionario() throws Exception {
        db.sql("DELETE FROM \"BSCL.Plataforma.BD::PLA.Diccionario\" WHERE \"CodDiccionario\" = 'CONTRASENA_INVALIDA'");
        String cuerpo = mvc.perform(post("/PLA/ActualizarClave").header("X-SESSION-TOKEN", "TOKEN-JPEREZ").contentType(MediaType.APPLICATION_JSON).content("{\"p_CodigoUsuario\":\"JPEREZ\",\"Clave1\":\"\",\"Clave2\":\"\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(cuerpo).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------ ValidaClaveProvisoria (T-24)

    private void provisoria(String clara, String fecha, int intentos) throws Exception {
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"ClaveProvisoria\" = '" + GestionClaveServicio.encryptPassword(clara) + "', \"FechaClaveProvisoria\" = TIMESTAMP '"
                + fecha + " 08:00:00', \"IntentosConexionClaveProvisoria\" = " + intentos + " WHERE \"IdUsuario\" = 1");
    }

    private JsonObject validar(String clave) throws Exception {
        return llamar("/PLA/ValidaClaveProvisoria", "{\"p_CodigoUsuario\":\"jperez\",\"p_ClaveProvisoria\":\"" + clave + "\"}");
    }

    @Test
    @DisplayName("T-24 RULE-038 the right provisional password issued on the system date is valid and is erased (it works once)")
    void provisoria_valida_el_mismo_dia() throws Exception {
        provisoria("Tmp#9x", "2026-03-02", 0);
        assertThat(validar("Tmp#9x").toString()).isEqualTo("{\"Estado\":\"OK\",\"status\":\"Clave provisoria valida.\"}");
        assertThat(usuarioCampo("ClaveProvisoria")).isEmpty();
        assertThat(usuarioCampo("IntentosConexionClaveProvisoria")).isEqualTo("0");
        assertThat(validar("Tmp#9x").toString()).isEqualTo("{\"Estado\":\"NOK\",\"status\":\"Clave provisoria invalida.\"}");
    }

    @Test
    @DisplayName("T-24 RULE-038 a provisional password of another day is expired and erased, even if it is the right one")
    void provisoria_vencida() throws Exception {
        provisoria("Tmp#9x", "2026-03-01", 0);
        assertThat(validar("Tmp#9x").toString()).isEqualTo("{\"Estado\":\"NOK\",\"status\":\"Clave provisoria vencida.\"}");
        assertThat(usuarioCampo("ClaveProvisoria")).isEmpty();
    }

    @Test
    @DisplayName("T-24 a wrong provisional password counts one attempt and keeps the password")
    void provisoria_incorrecta_suma_intento() throws Exception {
        provisoria("Tmp#9x", "2026-03-02", 1);
        assertThat(validar("otra").toString()).isEqualTo("{\"Estado\":\"NOK\",\"status\":\"Clave provisoria invalida.\"}");
        assertThat(usuarioCampo("IntentosConexionClaveProvisoria")).isEqualTo("2");
        assertThat(usuarioCampo("ClaveProvisoria")).isEqualTo(GestionClaveServicio.encryptPassword("Tmp#9x"));
    }

    @Test
    @DisplayName("T-24 RULE-039/040 (reviewed wrong): with a maximum of 3 the right password is refused at exactly 3 failed attempts (the legacy allowed a 4th) and is erased")
    void provisoria_tope_de_intentos_exacto() throws Exception {
        provisoria("Tmp#9x", "2026-03-02", 2);       // maximum PLA.ClaveUsuarioIntentos = 3: two failures so far, the third try is still allowed
        assertThat(validar("Tmp#9x").get("Estado").getAsString()).isEqualTo("OK");
        provisoria("Tmp#9x", "2026-03-02", 3);       // three failures: locked
        assertThat(validar("Tmp#9x").toString()).isEqualTo("{\"Estado\":\"NOK\",\"status\":\"Supera los intentos maximos.\"}");
        assertThat(usuarioCampo("ClaveProvisoria")).isEmpty();
    }

    @Test
    @DisplayName("T-24 an unknown user is answered with an empty body (legacy), an empty body with CLAVE_PROVISORIA_INVALIDA")
    void provisoria_usuario_desconocido_y_cuerpo_vacio() throws Exception {
        assertThat(llamar("/PLA/ValidaClaveProvisoria", "{\"p_CodigoUsuario\":\"NADIE\",\"p_ClaveProvisoria\":\"x\"}")).isNull();
        assertThat(mvc.perform(post("/PLA/ValidaClaveProvisoria").contentType(MediaType.APPLICATION_JSON).content("")).andReturn().getResponse().getContentAsString())
                .isEqualTo("{\"Estado\":\"NOK\",\"status\":\"Clave provisoria invalida.\"}");
    }

    // ------------------------------------------------------------------------------------------------ SolicitudRecuperacionClave (T-25)

    @Test
    @DisplayName("T-25 the request calls the procedure with company, user in upper case and a 5-character token, and answers the masked e-mail")
    void solicitud_recuperacion_ok() throws Exception {
        sp.responderSiempre("BSCL.Plataforma.BD.Servicios::ACC.RecuperarClaveUsuario", ProcedimientosGuionados.salida("P_OUT_ERRORESCONTADOR", "0", "P_OUT_MAIL", "juan.perez@banco.cl"));
        JsonObject r = llamar("/PLA/SolicitudRecuperacionClave", "{\"p_IdEmpresa\":\"1\",\"p_Username\":\"jperez\"}");
        assertThat(r.get("statusCode").getAsString()).isEqualTo("0");
        assertThat(r.get("statusMessage").getAsString()).isEqualTo("OK");
        assertThat(r.get("recoveryTo").getAsString()).isEqualTo("j*********@b****.c*");
        JsonObject in = sp.llamadasA("BSCL.Plataforma.BD.Servicios::ACC.RecuperarClaveUsuario").get(0).entrada();
        assertThat(in.get("p_IdEmpresa").getAsLong()).isEqualTo(1L);
        assertThat(in.get("p_Username").getAsString()).isEqualTo("JPEREZ");
        assertThat(in.get("p_Token").getAsString()).hasSize(5);
    }

    @Test
    @DisplayName("T-25 a procedure error is answered 200 with its code and ERROR_RECUPERAR_CLAVE; a failure, a bad company or a bad body with code 3")
    void solicitud_recuperacion_errores() throws Exception {
        sp.responderSiempre("BSCL.Plataforma.BD.Servicios::ACC.RecuperarClaveUsuario", ProcedimientosGuionados.salida("P_OUT_ERRORESCONTADOR", "2"));
        assertThat(llamar("/PLA/SolicitudRecuperacionClave", "{\"p_IdEmpresa\":\"1\",\"p_Username\":\"jperez\"}").toString())
                .isEqualTo("{\"statusCode\":2,\"statusMessage\":\"ERROR_RECUPERAR_CLAVE\"}");
        assertThat(llamar("/PLA/SolicitudRecuperacionClave", "{\"p_IdEmpresa\":\"x\",\"p_Username\":\"jperez\"}").toString())
                .isEqualTo("{\"statusCode\":3,\"statusMessage\":\"ERROR_RECUPERAR_CLAVE\"}");
        assertThat(llamar("/PLA/SolicitudRecuperacionClave", "{}").toString()).isEqualTo("{\"statusCode\":3,\"statusMessage\":\"ERROR_RECUPERAR_CLAVE\"}");
        sp.responder("BSCL.Plataforma.BD.Servicios::ACC.RecuperarClaveUsuario", (c, in) -> {
            throw new IllegalStateException("procedure down");
        });
        assertThat(llamar("/PLA/SolicitudRecuperacionClave", "{\"p_IdEmpresa\":\"1\",\"p_Username\":\"jperez\"}").toString())
                .isEqualTo("{\"statusCode\":3,\"statusMessage\":\"ERROR_RECUPERAR_CLAVE\"}");
    }

    // ------------------------------------------------------------------------------------------------ ConfirmarCambioClave (T-27)

    @Test
    @DisplayName("T-27 the confirmation passes company, user (upper case), password and token to the procedure; success commits what the procedure wrote")
    void confirmar_ok_confirma_lo_que_escribio_el_procedimiento() throws Exception {
        sp.responder("BSCL.Plataforma.BD.Servicios::ACC.ConfirmarCambioClaveUsuario", (con, in) -> {
            try (Statement st = con.createStatement()) {
                st.execute("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"Clave\" = 'NUEVA' WHERE \"IdUsuario\" = 1");
            } catch (java.sql.SQLException e) {
                throw new IllegalStateException(e);
            }
            return ProcedimientosGuionados.salida("P_OUT_ERRORESCONTADOR", "0");
        });
        JsonObject r = llamar("/PLA/ConfirmarCambioClave", "{\"p_IdEmpresa\":\"1\",\"p_Username\":\"jperez\",\"p_Password\":\"Nueva#123\",\"p_Token\":\"Ab#x9\"}");
        assertThat(r.toString()).isEqualTo("{\"statusCode\":\"0\",\"statusMessage\":\"OK\"}");
        JsonObject in = sp.llamadasA("BSCL.Plataforma.BD.Servicios::ACC.ConfirmarCambioClaveUsuario").get(0).entrada();
        assertThat(in.get("p_Username").getAsString()).isEqualTo("JPEREZ");
        assertThat(in.get("p_Password").getAsString()).isEqualTo("Nueva#123");
        assertThat(in.get("p_Token").getAsString()).isEqualTo("Ab#x9");
        assertThat(usuarioCampo("Clave")).isEqualTo("NUEVA");
    }

    @Test
    @DisplayName("T-27 T-22 a procedure error ROLLS BACK what the procedure wrote on the connection of the request, and answers 200 ERROR_CONFIRMAR_CLAVE with its code")
    void confirmar_error_deshace() throws Exception {
        sp.responder("BSCL.Plataforma.BD.Servicios::ACC.ConfirmarCambioClaveUsuario", (con, in) -> {
            try (Statement st = con.createStatement()) {
                st.execute("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"Clave\" = 'NUEVA' WHERE \"IdUsuario\" = 1");
            } catch (java.sql.SQLException e) {
                throw new IllegalStateException(e);
            }
            return ProcedimientosGuionados.salida("P_OUT_ERRORESCONTADOR", "1");
        });
        JsonObject r = llamar("/PLA/ConfirmarCambioClave", "{\"p_IdEmpresa\":\"1\",\"p_Username\":\"jperez\",\"p_Password\":\"x\",\"p_Token\":\"t\"}");
        assertThat(r.toString()).isEqualTo("{\"statusCode\":1,\"statusMessage\":\"ERROR_CONFIRMAR_CLAVE\"}");
        assertThat(usuarioCampo("Clave")).isEqualTo("VIEJA");
    }

    @Test
    @DisplayName("T-27 GET is always the error code 3; a bad body is code 3")
    void confirmar_get_y_cuerpo_malo() throws Exception {
        assertThat(mvc.perform(get("/PLA/ConfirmarCambioClave")).andReturn().getResponse().getContentAsString())
                .isEqualTo("{\"statusCode\":3,\"statusMessage\":\"ERROR_CONFIRMAR_CLAVE\"}");
        assertThat(llamar("/PLA/ConfirmarCambioClave", "{\"p_IdEmpresa\":\"no\"}").toString()).isEqualTo("{\"statusCode\":3,\"statusMessage\":\"ERROR_CONFIRMAR_CLAVE\"}");
    }

    // ------------------------------------------------------------------------------------------------ RecuperacionClave (T-26)

    private void datosRecuperacion() throws Exception {
        db.sql("INSERT INTO \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" VALUES (10, NULL, 100, 777, 1)",
                "INSERT INTO \"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona\" VALUES (777, 5550, 'Juan Perez Soto')",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales\" VALUES (1, 'Corredora Ficticia S.A.')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Imagenes\" VALUES ('LOGO_CORREDOR', 1, '/img/logo.png')");
        sp.responder("BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento", (c, in) -> ProcedimientosGuionados.salida("P_OUT_IDEVENTO", "1"));
    }

    @Test
    @DisplayName("T-26 the OPTIMUS channel and an employee: the provisional password (PLA.LargoMaximoToken characters) is notified and stored hashed with the system date")
    void recuperacion_envia_clave_provisoria() throws Exception {
        datosRecuperacion();
        JsonObject r = llamar("/PLA/RecuperacionClave", "{\"p_CodigoUsuario\":\"jperez\",\"p_CanalSistema\":\"OPTIMUS\"}");
        assertThat(r.toString()).isEqualTo("{\"Estado\":\"OK\",\"status\":\"CLAVE ENVIADA\"}");

        JsonObject evento = sp.llamadasA("BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento").get(0).entrada();
        assertThat(evento.get("P_IDEMPRESA").getAsLong()).isEqualTo(1L);
        assertThat(evento.get("P_IDUSUARIO").getAsLong()).isEqualTo(1L);
        assertThat(evento.get("P_IDMODULO").getAsString()).isEqualTo("PLA");
        JsonObject e = evento.getAsJsonArray("P_EVENTOS").get(0).getAsJsonObject();
        assertThat(e.get("CodigoTipoEvento").getAsString()).isEqualTo("SOLICITUD_CAMBIO_CLAVE");
        assertThat(e.get("FechaCreacionSistema").getAsString()).isEqualTo("2026-03-02");
        assertThat(e.get("IdPersonaDestinatario").getAsLong()).isEqualTo(777L);
        JsonObject dir = evento.getAsJsonArray("P_IDDIRECCIONES").get(0).getAsJsonObject();
        assertThat(dir.get("TipoDestinatario").getAsString()).isEqualTo("TO");
        assertThat(dir.get("IdDireccion").getAsLong()).isEqualTo(5550L);
        String provisoria = null;
        for (var v : evento.getAsJsonArray("P_VALORES")) {
            JsonObject o = v.getAsJsonObject();
            switch (o.get("Llave").getAsString()) {
                case "Nombre" -> assertThat(o.get("Valor").getAsString()).isEqualTo("Juan Perez Soto");
                case "Corredora" -> assertThat(o.get("Valor").getAsString()).isEqualTo("Corredora Ficticia S.A.");
                case "LogoCorredora" -> assertThat(o.get("Valor").getAsString()).isEqualTo("/img/logo.png");
                case "ClaveProvisoria" -> provisoria = o.get("Valor").getAsString();
                default -> throw new AssertionError("unexpected value " + o);
            }
        }
        assertThat(provisoria).hasSize(10);
        assertThat(usuarioCampo("ClaveProvisoria")).isEqualTo(GestionClaveServicio.encryptPassword(provisoria));
        assertThat(usuarioCampo("FechaClaveProvisoria")).startsWith("2026-03-02");
        assertThat(usuarioCampo("IntentosConexionClaveProvisoria")).isEqualTo("0");
        // and the user can prove it with ValidaClaveProvisoria the same day
        assertThat(validar(provisoria).get("Estado").getAsString()).isEqualTo("OK");
    }

    @Test
    @DisplayName("T-26 a user that does not belong to the channel (employee asking as CLIENTE, unknown channel) gets 401 ERROR_RECUPERAR_CLAVE and nothing is stored")
    void recuperacion_canal_incorrecto() throws Exception {
        datosRecuperacion();
        String esperado = "{\"P_OUT_ERRORESCONTADOR\":1,\"P_OUT_ERRORESDETALLE\":[{\"CODIGO\":\"ERROR_RECUPERAR_CLAVE\",\"Descripcion\":\"Error al Recuperar Clave.\"}]}";
        for (String canal : new String[] { "CLIENTE", "OTRO", "" }) {
            String cuerpo = mvc.perform(post("/PLA/RecuperacionClave").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"p_CodigoUsuario\":\"JPEREZ\",\"p_CanalSistema\":\"" + canal + "\"}")).andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
            assertThat(cuerpo).isEqualTo(esperado);
        }
        assertThat(sp.llamadasA("BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento")).isEmpty();
        assertThat(usuarioCampo("ClaveProvisoria")).isEmpty();
    }

    @Test
    @DisplayName("RULE-041 password recovery: the channel must match the entity type (CLIENTE: CLI or PER; OPTIMUS and AGENTE: EMP); any other pair is 401 ERROR_RECUPERAR_CLAVE")
    void rule041_canal_y_tipo_de_entidad() throws Exception {
        datosRecuperacion();
        db.sql("INSERT INTO \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" VALUES (11, 100, NULL, 777, 1)");
        String[] canales = { "CLIENTE", "OPTIMUS", "AGENTE" };
        // entity type, id of the entity, and which channels are allowed for it
        Object[][] matriz = { { "EMP", 100, new boolean[] { false, true, true } }, { "CLI", 100, new boolean[] { true, false, false } },
                { "PER", 777, new boolean[] { true, false, false } } };
        for (Object[] fila : matriz) {
            db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"TipoEntidad\" = '" + fila[0] + "', \"IdEntidad\" = " + fila[1] + " WHERE \"IdUsuario\" = 1");
            boolean[] permitido = (boolean[]) fila[2];
            for (int i = 0; i < canales.length; i++) {
                var r = mvc.perform(post("/PLA/RecuperacionClave").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"p_CodigoUsuario\":\"JPEREZ\",\"p_CanalSistema\":\"" + canales[i] + "\"}")).andReturn().getResponse();
                assertThat(r.getStatus()).as("entity " + fila[0] + " channel " + canales[i]).isEqualTo(permitido[i] ? 200 : 401);
                if (!permitido[i]) {
                    assertThat(r.getContentAsString()).contains("ERROR_RECUPERAR_CLAVE");
                }
            }
        }
    }

    @Test
    @DisplayName("T-26 an unknown user, an empty body and a failing notification all give 401 and leave the user untouched")
    void recuperacion_fallos() throws Exception {
        datosRecuperacion();
        mvc.perform(post("/PLA/RecuperacionClave").contentType(MediaType.APPLICATION_JSON).content("{\"p_CodigoUsuario\":\"NADIE\",\"p_CanalSistema\":\"OPTIMUS\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/PLA/RecuperacionClave").contentType(MediaType.APPLICATION_JSON).content("")).andExpect(status().isUnauthorized());
        sp.responder("BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento", (c, in) -> {
            throw new IllegalStateException("events down");
        });
        mvc.perform(post("/PLA/RecuperacionClave").contentType(MediaType.APPLICATION_JSON).content("{\"p_CodigoUsuario\":\"JPEREZ\",\"p_CanalSistema\":\"OPTIMUS\"}"))
                .andExpect(status().isUnauthorized());
        assertThat(usuarioCampo("ClaveProvisoria")).isEmpty();
    }

    @Test
    @DisplayName("T-10 T-11 the provisional password check and the recovery services are reachable under /PLA without a session token; changing the password is not")
    void servicios_de_clave_no_piden_sesion() throws Exception {
        mvc.perform(post("/PLA/ValidaClaveProvisoria").contentType(MediaType.APPLICATION_JSON).content("")).andExpect(status().isOk());
        mvc.perform(post("/PLA/ActualizarClave").contentType(MediaType.APPLICATION_JSON).content("")).andExpect(status().isBadRequest());
        mvc.perform(post("/PLA/ActualizarClave").header("X-SESSION-TOKEN", "TOKEN-JPEREZ").contentType(MediaType.APPLICATION_JSON).content("")).andExpect(status().isOk());
    }
}
