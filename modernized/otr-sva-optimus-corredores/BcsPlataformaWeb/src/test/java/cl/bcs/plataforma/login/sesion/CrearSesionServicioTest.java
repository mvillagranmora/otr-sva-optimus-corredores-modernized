package cl.bcs.plataforma.login.sesion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.util.Base64;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.login.h2.LoginDb;

/**
 * Characterization of the login (legacy {@code CrearSesion.crearSesionUsuario}) on H2 with the legacy SQL. Every test names the rule it pins
 * (RULE-010..022 of BUSINESS_RULES.md) and the task of TODO_MVP.md. The browser sends {@code SHA-256(clave)} in hex; the stored value is the SHA-256
 * of that text in upper case.
 */
class CrearSesionServicioTest {

    private static final String CLAVE_NAVEGADOR = sha256Hex("clave-de-prueba");        // what the browser sends
    private static final String CLAVE_GUARDADA = sha256Hex(CLAVE_NAVEGADOR).toUpperCase(); // what ACC.Usuario.Clave holds

    private LoginDb db;
    private CrearSesionServicio servicio;
    private Connection con;

    @BeforeAll
    static void utc() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        org.h2.util.DateTimeUtils.resetCalendar();
    }

    @BeforeEach
    void abrir() throws Exception {
        db = LoginDb.nueva();
        con = db.con();
        servicio = new CrearSesionServicio(new AuditoriaSesion());
        // an employee user of channel COR, no attempts, no block, no expiry
        usuario(1, "JPEREZ", "EMP", 100, "S", "N", "N", "N", "ACT", "P_NO");
    }

    @AfterEach
    void cerrar() throws Exception {
        db.close();
    }

    // ------------------------------------------------------------------------------------------------ helpers

    private void usuario(long id, String codigo, String entidad, long idEntidad, String corredores, String negocios, String servicios, String clientes,
            String estado, String autorizadoMarca) throws Exception {
        String autorizado = autorizadoMarca.equals("P_SI") ? "P" : "S";
        db.sql("INSERT INTO \"BSCL.Plataforma.BD::ACC.Usuario\" (\"IdUsuario\",\"IdEmpresa\",\"CodigoUsuario\",\"Clave\",\"FechaVigencia\",\"IdEstado\","
                + "\"IntentosConexion\",\"FechaBloqueoTemporal\",\"EsAutorizado\",\"TipoEntidad\",\"IdEntidad\",\"UsuarioClientes\",\"UsuarioNegocios\","
                + "\"UsuarioServicios\",\"UsuarioCorredores\",\"CodigoMenu\") VALUES (" + id + ",1,'" + codigo + "','" + CLAVE_GUARDADA + "',DATE '0001-01-01','"
                + estado + "',0,TIMESTAMP '0001-01-01 00:00:00','" + autorizado + "','" + entidad + "'," + idEntidad + ",'" + clientes + "','" + negocios + "','"
                + servicios + "','" + corredores + "','MENU1')");
    }

    private CrearSesionServicio.Resultado entrar(String usuario, String clave, String canal) throws Exception {
        CrearSesionServicio.Resultado r = servicio.crearSesionUsuario(con, 1L, usuario, clave, canal);
        con.commit();
        return r;
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

    private String intentos(long idUsuario) throws Exception {
        return db.valor("SELECT \"IntentosConexion\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdUsuario\" = " + idUsuario);
    }

    // ------------------------------------------------------------------------------------------------ the happy path and the answer

    @Test
    @DisplayName("RULE-013 correct password (SHA-256 of the text the browser sent, upper case) opens a session for the COR channel")
    void clave_correcta_abre_sesion() throws Exception {
        CrearSesionServicio.Resultado r = entrar("jperez", CLAVE_NAVEGADOR, "COR");   // the user name is matched in upper case
        assertThat(r.entidad).isEqualTo("EMP");
        assertThat(r.token).isNotBlank();
        assertThat(db.valor("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"TokenValido\" = 'S' AND \"IdUsuario\" = 1 AND \"IdCanal\" = 'COR'"))
                .isEqualTo("1");
    }

    @Test
    @DisplayName("RULE-021 T-07 token = Base64(idEmpresa:usuario:timestamp + UUID), the user as the client wrote it")
    void formato_del_token() throws Exception {
        CrearSesionServicio.Resultado r = entrar("jperez", CLAVE_NAVEGADOR, "COR");
        String claro = new String(Base64.getDecoder().decode(r.token), StandardCharsets.UTF_8);
        assertThat(claro).matches("1:jperez:\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}(\\.\\d+)?[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("RULE-013 a wrong password is refused with the same answer as an unknown user")
    void clave_incorrecta_y_usuario_inexistente_igual_respuesta() throws Exception {
        CrearSesionServicio.Resultado mala = entrar("JPEREZ", sha256Hex("otra"), "COR");
        CrearSesionServicio.Resultado fantasma = entrar("NOEXISTE", CLAVE_NAVEGADOR, "COR");
        assertThat(mala.entidad).isEmpty();
        assertThat(fantasma.entidad).isEmpty();
        assertThat(mala.token).isEqualTo(fantasma.token).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------ eligibility of the user

    @Test
    @DisplayName("RULE-010 a permanently blocked user (BLO) cannot log in")
    void usuario_BLO() throws Exception {
        usuario(2, "BLOQUEADO", "EMP", 100, "S", "N", "N", "N", "BLO", "P_NO");
        assertThat(entrar("BLOQUEADO", CLAVE_NAVEGADOR, "COR").entidad).isEmpty();
    }

    @Test
    @DisplayName("RULE-011 only EsAutorizado = P blocks; any other value lets the user in")
    void autorizacion_pendiente() throws Exception {
        usuario(2, "PENDIENTE", "EMP", 100, "S", "N", "N", "N", "ACT", "P_SI");
        assertThat(entrar("PENDIENTE", CLAVE_NAVEGADOR, "COR").entidad).isEmpty();
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"EsAutorizado\" = 'X' WHERE \"IdUsuario\" = 2");
        assertThat(entrar("PENDIENTE", CLAVE_NAVEGADOR, "COR").entidad).isEqualTo("EMP");
    }

    @Test
    @DisplayName("RULE-012 an expired account (FechaVigencia before the system date) is refused; 0001-01-01 means no expiry; the system date itself is valid")
    void cuenta_vencida() throws Exception {
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"FechaVigencia\" = DATE '2026-03-01' WHERE \"IdUsuario\" = 1");
        assertThat(entrar("JPEREZ", CLAVE_NAVEGADOR, "COR").entidad).isEmpty();
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"FechaVigencia\" = DATE '2026-03-02' WHERE \"IdUsuario\" = 1");
        assertThat(entrar("JPEREZ", CLAVE_NAVEGADOR, "COR").entidad).isEqualTo("EMP");
    }

    // ------------------------------------------------------------------------------------------------ attempts and temporary block

    @Test
    @DisplayName("RULE-014 a wrong password adds one attempt (confirmed by the commit)")
    void intento_fallido_suma() throws Exception {
        entrar("JPEREZ", sha256Hex("mala"), "COR");
        assertThat(intentos(1)).isEqualTo("1");
        entrar("JPEREZ", sha256Hex("mala"), "COR");
        assertThat(intentos(1)).isEqualTo("2");
    }

    @Test
    @DisplayName("RULE-015 at the maximum of attempts the user is blocked for PLA.ClaveUsuarioTiempoBloqueo minutes and the counter returns to 0")
    void bloqueo_temporal_al_llegar_al_maximo() throws Exception {
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"IntentosConexion\" = 2 WHERE \"IdUsuario\" = 1");   // maximum is 3
        entrar("JPEREZ", sha256Hex("mala"), "COR");
        assertThat(intentos(1)).isEqualTo("0");
        // blocked for 15 minutes: between 14 and 16 minutes ahead of the database clock
        assertThat(db.valor("SELECT DATEDIFF('MINUTE', LOCALTIMESTAMP, \"FechaBloqueoTemporal\") FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdUsuario\" = 1"))
                .isIn("14", "15");
        // while blocked not even the right password opens a session
        assertThat(entrar("JPEREZ", CLAVE_NAVEGADOR, "COR").entidad).isEmpty();
    }

    @Test
    @DisplayName("RULE-016 a successful login returns the attempt counter to 0")
    void exito_reinicia_intentos() throws Exception {
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"IntentosConexion\" = 2 WHERE \"IdUsuario\" = 1");
        assertThat(entrar("JPEREZ", CLAVE_NAVEGADOR, "COR").entidad).isEqualTo("EMP");
        assertThat(intentos(1)).isEqualTo("0");
    }

    // ------------------------------------------------------------------------------------------------ channel / entity

    @Test
    @DisplayName("RULE-017 an employee needs the flag of the channel: COR, AGE and WS each need their own")
    void matriz_canal_empleado() throws Exception {
        assertThat(entrar("JPEREZ", CLAVE_NAVEGADOR, "COR").entidad).isEqualTo("EMP");
        assertThat(entrar("JPEREZ", CLAVE_NAVEGADOR, "AGE").entidad).isEmpty();
        assertThat(entrar("JPEREZ", CLAVE_NAVEGADOR, "WS").entidad).isEmpty();
        assertThat(entrar("JPEREZ", CLAVE_NAVEGADOR, "CLI").entidad).isEmpty();
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"UsuarioNegocios\" = 'S' WHERE \"IdUsuario\" = 1");
        assertThat(entrar("JPEREZ", CLAVE_NAVEGADOR, "AGE").entidad).isEqualTo("EMP");
    }

    @Test
    @DisplayName("RULE-017 a person logs in only through CLI; a client through WS or CLI")
    void matriz_canal_persona_y_cliente() throws Exception {
        usuario(2, "PERSONA", "PER", 200, "N", "N", "N", "S", "ACT", "P_NO");
        usuario(3, "CLIENTE", "CLI", 300, "N", "N", "S", "S", "ACT", "P_NO");
        assertThat(entrar("PERSONA", CLAVE_NAVEGADOR, "CLI").entidad).isEqualTo("PER");
        assertThat(entrar("PERSONA", CLAVE_NAVEGADOR, "COR").entidad).isEmpty();
        assertThat(entrar("CLIENTE", CLAVE_NAVEGADOR, "WS").entidad).isEqualTo("CLI");
        assertThat(entrar("CLIENTE", CLAVE_NAVEGADOR, "CLI").entidad).isEqualTo("CLI");
        assertThat(entrar("CLIENTE", CLAVE_NAVEGADOR, "COR").entidad).isEmpty();
    }

    @Test
    @DisplayName("RULE-018 (reviewed wrong): a BLOCKED client cannot log in, as employees and persons cannot")
    void cliente_BLO_no_entra() throws Exception {
        usuario(3, "CLIENTE", "CLI", 300, "N", "N", "S", "S", "ACT", "P_NO");
        db.sql("UPDATE \"BSCL.SocioNegocio.BD::CLI.Cliente\" SET \"IdEstado\" = 'BLO' WHERE \"IdCliente\" = 300");
        assertThat(entrar("CLIENTE", CLAVE_NAVEGADOR, "CLI").entidad).isEmpty();
    }

    @Test
    @DisplayName("RULE-019 T-03 the entity must be active AND enrolled: a blocked employee or one not yet enrolled is refused, also for a person")
    void entidad_activa_y_enrolada() throws Exception {
        db.sql("UPDATE \"BSCL.SocioNegocio.BD::EMP.Empleado\" SET \"IdEstado\" = 'BLO' WHERE \"IdEmpleado\" = 100");
        assertThat(entrar("JPEREZ", CLAVE_NAVEGADOR, "COR").entidad).isEmpty();
        db.sql("UPDATE \"BSCL.SocioNegocio.BD::EMP.Empleado\" SET \"IdEstado\" = 'ACT', \"EsEnrolado\" = 'N' WHERE \"IdEmpleado\" = 100");
        assertThat(entrar("JPEREZ", CLAVE_NAVEGADOR, "COR").entidad).isEmpty();
        usuario(2, "PERSONA", "PER", 200, "N", "N", "N", "S", "ACT", "P_NO");
        db.sql("UPDATE \"BSCL.SocioNegocio.BD::PER.Persona\" SET \"IdEstado\" = 'BLO' WHERE \"IdPersona\" = 200");
        assertThat(entrar("PERSONA", CLAVE_NAVEGADOR, "CLI").entidad).isEmpty();
        // the entity row is missing altogether
        usuario(4, "HUERFANO", "EMP", 999, "S", "N", "N", "N", "ACT", "P_NO");
        assertThat(entrar("HUERFANO", CLAVE_NAVEGADOR, "COR").entidad).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------ the session itself

    @Test
    @DisplayName("RULE-022 T-04 with PLA.SesionUnicaUsuario = S and the channel in PLA.CanalesSesionUnica a new login closes the previous session of the user and channel")
    void sesion_unica() throws Exception {
        db.sql("INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.SesionUnicaUsuario', 'S')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.CanalesSesionUnica', 'COR;AGE')");
        String primero = entrar("JPEREZ", CLAVE_NAVEGADOR, "COR").token;
        String segundo = entrar("JPEREZ", CLAVE_NAVEGADOR, "COR").token;
        assertThat(db.valor("SELECT \"TokenValido\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = '" + primero + "'")).isEqualTo("N");
        assertThat(db.valor("SELECT \"TokenValido\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = '" + segundo + "'")).isEqualTo("S");
    }

    @Test
    @DisplayName("RULE-022 without the switch (or with another channel) the previous sessions stay valid")
    void sin_sesion_unica_conviven() throws Exception {
        String primero = entrar("JPEREZ", CLAVE_NAVEGADOR, "COR").token;
        entrar("JPEREZ", CLAVE_NAVEGADOR, "COR");
        assertThat(db.valor("SELECT \"TokenValido\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = '" + primero + "'")).isEqualTo("S");
        db.sql("INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.SesionUnicaUsuario', 'S')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.CanalesSesionUnica', 'AGE')");   // COR is not in the list
        entrar("JPEREZ", CLAVE_NAVEGADOR, "COR");
        assertThat(db.valor("SELECT \"TokenValido\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = '" + primero + "'")).isEqualTo("S");
    }

    @Test
    @DisplayName("RULE-020 T-05 the WS channel reuses its valid token while it is not idle; an idle one is invalidated and a new one issued")
    void ws_reutiliza_token() throws Exception {
        usuario(5, "SERVICIO", "EMP", 100, "N", "N", "S", "N", "ACT", "P_NO");
        String primero = entrar("SERVICIO", CLAVE_NAVEGADOR, "WS").token;
        String reutilizado = entrar("SERVICIO", CLAVE_NAVEGADOR, "WS").token;
        assertThat(reutilizado).isEqualTo(primero);
        assertThat(db.valor("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdUsuario\" = 5")).isEqualTo("1");
        // idle for more than PLA.IdleMaxTimeWS (60 000 ms)
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Sesion\" SET \"FechaUltimoUso\" = DATEADD('SECOND', -120, LOCALTIMESTAMP) WHERE \"IdUsuario\" = 5");
        String nuevo = entrar("SERVICIO", CLAVE_NAVEGADOR, "WS").token;
        assertThat(nuevo).isNotEqualTo(primero);
        assertThat(db.valor("SELECT \"TokenValido\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = '" + primero + "'")).isEqualTo("N");
        assertThat(db.valor("SELECT \"TokenValido\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = '" + nuevo + "'")).isEqualTo("S");
    }

    @Test
    @DisplayName("T-06 a new session leaves one PLA.Log row (INFORMATIVO, PLA, SESION) and four PLA.DetalleLog rows, with their authentication codes; a reused WS token leaves none")
    void auditoria_de_sesion() throws Exception {
        entrar("JPEREZ", CLAVE_NAVEGADOR, "COR");
        assertThat(db.filas("SELECT \"IdTipoLog\",\"IdModulo\",\"IdTipoRegistro\",\"IdUsuario\",\"IdEmpresa\",\"PeriodoLog\",\"FechaLog\" FROM \"BSCL.Plataforma.BD::PLA.Log\""))
                .containsExactly("INFORMATIVO|PLA|SESION|1|1|2026-03|2026-03-02");
        assertThat(db.filas("SELECT \"SubRegistro\",\"CampoRegistro\",\"ValorNuevo\" FROM \"BSCL.Plataforma.BD::PLA.DetalleLog\" ORDER BY \"IdDetalleLog\""))
                .containsExactly("SESION|FechaIngreso|2026-03-02", "SESION|ACCION|INICIO SESION EXITOSA", "SESION|CANAL|COR", "SESION|TokenValido|S");
        assertThat(db.valor("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::PLA.Log\" WHERE \"CodigoAutenticacion\" IS NULL OR LENGTH(\"CodigoAutenticacion\") <> 64")).isEqualTo("0");
        usuario(5, "SERVICIO", "EMP", 100, "N", "N", "S", "N", "ACT", "P_NO");
        entrar("SERVICIO", CLAVE_NAVEGADOR, "WS");
        long antes = Long.parseLong(db.valor("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::PLA.Log\""));
        entrar("SERVICIO", CLAVE_NAVEGADOR, "WS");   // reuse
        assertThat(Long.parseLong(db.valor("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::PLA.Log\""))).isEqualTo(antes);
    }

    @Test
    @DisplayName("RULE-012/RULE-010 faithful to the legacy: a user row with no block date is a NullPointerException (the controller answers 401 and rolls back)")
    void fecha_bloqueo_nula_es_npe_como_el_legacy() throws Exception {
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"FechaBloqueoTemporal\" = NULL WHERE \"IdUsuario\" = 1");
        assertThatThrownBy(() -> servicio.crearSesionUsuario(con, 1L, "JPEREZ", CLAVE_NAVEGADOR, "COR")).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("no company: refused without touching the database")
    void sin_empresa() throws Exception {
        assertThat(servicio.crearSesionUsuario(con, null, "JPEREZ", CLAVE_NAVEGADOR, "COR").entidad).isEmpty();
    }

    @Test
    @DisplayName("T-09 the hash of the password is the SHA-256 of the text, hex, upper case")
    void hash_de_la_clave() {
        assertThat(CrearSesionServicio.sha256Mayuscula("abc")).isEqualTo("BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD");
        assertThat(List.of(CLAVE_GUARDADA)).allMatch(h -> h.equals(h.toUpperCase()) && h.length() == 64);
    }
}
