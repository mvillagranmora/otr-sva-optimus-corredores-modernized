package cl.bcs.plataforma;

import cl.bcs.plataforma.adapter.in.*;
import cl.bcs.plataforma.service.TransaccionWSService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Timestamp;
import java.util.List;

/**
 * Characterization tests for BcsPlataformaWeb.
 *
 * Methodology:
 *   - Pure-logic tests: inline the exact formula from the legacy source, assert the same output.
 *   - Integration tests that require HANA are marked @Disabled("requires HANA").
 *   - The Spring context loads but does not connect to HANA (H2 in-memory via application-test.properties).
 *
 * All HANA-specific calls (stored procedures, custom SQL functions, multi-schema UNION) are
 * verified at the unit level or skipped.
 *
 * Build:  mvn test -pl BcsPlataformaWeb
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BcsPlataformaWebApplicationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    // =========================================================================
    // B-PLA-01 — CallController stub returns 503
    // Source: CallServlet.java — @WebServlet("/call")
    // =========================================================================

    /**
     * B-PLA-01: POST /call → 503 Service Unavailable (PLA.Servicio registry not exported).
     * Source: CallController.call — throws UnsupportedOperationException
     */
    @Test
    void rule01_callPost_returns503_stubs_PlaServicio() throws Exception {
        mockMvc.perform(post("/call")
                        .contentType("application/json")
                        .content("{\"t\":1}"))
               .andExpect(status().isServiceUnavailable());
    }

    /**
     * B-PLA-01b: GET /call → 503 Service Unavailable.
     */
    @Test
    void rule01b_callGet_returns503() throws Exception {
        mockMvc.perform(get("/call").param("t", "1"))
               .andExpect(status().isServiceUnavailable());
    }

    // =========================================================================
    // B-PLA-02 — PingController
    // Source: PingServlet — @WebServlet("/Ping")
    // =========================================================================

    /**
     * B-PLA-02: GET /Ping without sleep → 200 OK with "Served at:" body.
     * Source: PingServlet.doGet — response.getWriter().append("Served at: ")
     */
    @Test
    void rule02_pingGet_returns200() throws Exception {
        mockMvc.perform(get("/Ping"))
               .andExpect(status().isOk())
               .andExpect(content().string(org.hamcrest.Matchers.containsString("Served at:")));
    }

    // =========================================================================
    // B-PLA-03 — GlobalExceptionHandler
    // Source: GlobalExceptionHandler
    // =========================================================================

    /**
     * B-PLA-03: UnsupportedOperationException → 503 JSON response.
     * Source: GlobalExceptionHandler.handleUoe
     */
    @Test
    void rule03_unsupportedOperationException_maps_to_503() throws Exception {
        mockMvc.perform(post("/call")
                        .contentType("application/json")
                        .content("{}"))
               .andExpect(status().isServiceUnavailable())
               .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    // B-PLA-04 / B-PLA-05 (resolveTipoEntidad, esFechaMinima) moved to login/sesion/CrearSesionServicioTest (RULE-010..022 on H2)

    // =========================================================================
    // B-PLA-06 — TransaccionWSService pure logic
    // Source: ProcesarIdTransaccion
    // =========================================================================

    /**
     * B-PLA-06a: Body split by "@@@" must have exactly 4 parts.
     * Source: ProcesarIdTransaccion.performTask — params.length != 4
     */
    @Test
    void rule06a_transaccion_validParams_4_parts() {
        String[] valid = "INSERT@@@TX-001@@@SERVICIO@@@2024-01-01T00:00:00".split("@@@");
        assertTrue(TransaccionWSService.isValidParams(valid),
                "B-PLA-06a: 4 parts → valid");
    }

    /**
     * B-PLA-06b: Fewer than 4 parts → invalid.
     * Source: ProcesarIdTransaccion.performTask — params.length != 4
     */
    @Test
    void rule06b_transaccion_invalidParams_3_parts() {
        String[] invalid = "INSERT@@@TX-001@@@SERVICIO".split("@@@");
        assertFalse(TransaccionWSService.isValidParams(invalid),
                "B-PLA-06b: 3 parts → invalid");
    }

    /**
     * B-PLA-06c: ID length ≤ 36 → valid.
     * Source: ProcesarIdTransaccion.MAX_LENGTH_ID = 36
     */
    @Test
    void rule06c_transaccion_idLength_36_isValid() {
        String id36 = "a".repeat(36);
        assertTrue(TransaccionWSService.isValidIdLength(id36),
                "B-PLA-06c: 36 chars → valid");
    }

    /**
     * B-PLA-06d: ID length 37 → invalid.
     * Source: ProcesarIdTransaccion — idTransaccionWS.length() > MAX_LENGTH_ID
     */
    @Test
    void rule06d_transaccion_idLength_37_isInvalid() {
        String id37 = "a".repeat(37);
        assertFalse(TransaccionWSService.isValidIdLength(id37),
                "B-PLA-06d: 37 chars → invalid");
    }

    /**
     * B-PLA-06e: INSERT is a valid action.
     * Source: ProcesarIdTransaccion.performTask — switch(action)
     */
    @Test
    void rule06e_transaccion_INSERT_isValidAction() {
        assertTrue(TransaccionWSService.isValidAction("INSERT"),
                "B-PLA-06e: INSERT → valid action");
    }

    /**
     * B-PLA-06f: DELETE is a valid action.
     */
    @Test
    void rule06f_transaccion_DELETE_isValidAction() {
        assertTrue(TransaccionWSService.isValidAction("DELETE"),
                "B-PLA-06f: DELETE → valid action");
    }

    /**
     * B-PLA-06g: UNKNOWN is not a valid action.
     */
    @Test
    void rule06g_transaccion_UNKNOWN_isInvalidAction() {
        assertFalse(TransaccionWSService.isValidAction("UNKNOWN"),
                "B-PLA-06g: UNKNOWN → invalid action");
    }

    // B-PLA-07 (passwordsMatch, okEstado, nokEstado) moved to login/clave/GestionClaveServicioTest (RULE-029..040 on H2)

    // =========================================================================
    // B-PLA-08 — AuditoriaWSController.parseTimestamp (pure logic)
    // Source: AuditoriaWS.performTask — Fecha.toTimeStamp(params[3])
    // =========================================================================

    /**
     * B-PLA-08a: Valid timestamp string → non-null Timestamp.
     */
    @Test
    void rule08a_parseTimestamp_valid() {
        Timestamp ts = AuditoriaWSController.parseTimestamp("2024-06-15T10:30:00");
        assertNotNull(ts, "B-PLA-08a: valid timestamp");
        assertTrue(ts.getTime() > 0, "B-PLA-08a: timestamp > 0");
    }

    /**
     * B-PLA-08b: Invalid timestamp string → Timestamp(0) (no exception thrown).
     * Source: AuditoriaWS — graceful degradation
     */
    @Test
    void rule08b_parseTimestamp_invalid_returnsEpoch() {
        Timestamp ts = AuditoriaWSController.parseTimestamp("not-a-date");
        assertNotNull(ts, "B-PLA-08b: invalid → Timestamp(0) not null");
        assertEquals(0L, ts.getTime(), "B-PLA-08b: invalid → epoch");
    }

    // =========================================================================
    // B-PLA-09 — FileBlobController.resolveUpdateSql (pure logic)
    // Source: FileBlobManagerFactory.getManager — entity type dispatch
    // =========================================================================

    /**
     * B-PLA-09a: ANOTACION_PORTAFOLIO → Portafolio table update SQL.
     */
    @Test
    void rule09a_resolveSql_ANOTACION_PORTAFOLIO() {
        String sql = FileBlobController.resolveUpdateSql("ANOTACION_PORTAFOLIO");
        assertNotNull(sql, "B-PLA-09a: Portafolio SQL not null");
        assertTrue(sql.contains("POR.Anotacion"),
                "B-PLA-09a: Portafolio SQL references POR.Anotacion");
    }

    /**
     * B-PLA-09b: ANOTACION_CLIENTE → SocioNegocio table update SQL.
     */
    @Test
    void rule09b_resolveSql_ANOTACION_CLIENTE() {
        String sql = FileBlobController.resolveUpdateSql("ANOTACION_CLIENTE");
        assertNotNull(sql, "B-PLA-09b: Cliente SQL not null");
        assertTrue(sql.contains("CLI.Anotacion"),
                "B-PLA-09b: Cliente SQL references CLI.Anotacion");
    }

    /**
     * B-PLA-09c: ANOTACION_RECAUDACION_APV → APV table update SQL.
     */
    @Test
    void rule09c_resolveSql_ANOTACION_RECAUDACION_APV() {
        String sql = FileBlobController.resolveUpdateSql("ANOTACION_RECAUDACION_APV");
        assertNotNull(sql, "B-PLA-09c: APV SQL not null");
        assertTrue(sql.contains("REC.Anotacion"),
                "B-PLA-09c: APV SQL references REC.Anotacion");
    }

    /**
     * B-PLA-09d: Unknown entity → null (no manager available).
     * Source: FileBlobManagerFactory.getManager — return null for unknown
     */
    @Test
    void rule09d_resolveSql_unknown_returnsNull() {
        String sql = FileBlobController.resolveUpdateSql("UNKNOWN_ENTITY");
        assertNull(sql, "B-PLA-09d: unknown entity → null");
    }

    // =========================================================================
    // B-PLA-10 — SSOController.extractEmail (pure logic)
    // Source: SSOUtils.procesar — payload.get("email") / "mail" fallback
    // =========================================================================

    /**
     * B-PLA-10a: JWT payload with "email" field → returns email.
     */
    @Test
    void rule10a_extractEmail_fromEmailField() {
        JsonObject payload = new JsonObject();
        payload.addProperty("email", "user@example.com");
        assertEquals("user@example.com", SSOController.extractEmail(payload),
                "B-PLA-10a: extracts 'email' field");
    }

    /**
     * B-PLA-10b: JWT payload with "mail" field (BofA format) → returns mail.
     * Source: SSOUtils.procesar — "mail" fallback for BofA
     */
    @Test
    void rule10b_extractEmail_fromMailField_fallback() {
        JsonObject payload = new JsonObject();
        payload.addProperty("mail", "user@bofa.com");
        assertEquals("user@bofa.com", SSOController.extractEmail(payload),
                "B-PLA-10b: fallback to 'mail' field");
    }

    /**
     * B-PLA-10c: JWT payload with neither "email" nor "mail" → IllegalArgumentException.
     */
    @Test
    void rule10c_extractEmail_noEmailField_throws() {
        JsonObject payload = new JsonObject();
        payload.addProperty("sub", "12345");
        assertThrows(IllegalArgumentException.class,
                () -> SSOController.extractEmail(payload),
                "B-PLA-10c: missing email/mail → IAE");
    }

    // B-PLA-11 (debug mode, GET /bootstrap without token) was the placeholder HTML page; the bootstrap is now JSON: see login/bootstrap/BootstrapTest

    // =========================================================================
    // B-PLA-12 — ConfiguracionController.parseKeys (pure logic)
    // Source: ConfiguracionServlet.doGet — body parse logic
    // =========================================================================

    /**
     * B-PLA-12a: Single JSON object body → 1 key.
     */
    @Test
    void rule12a_parseKeys_singleObject() throws Exception {
        String body = "{\"idEmpresa\":1,\"modulo\":\"PLATAFORMA\",\"codigoParametro\":\"PLA.Version\"}";
        var keys = ConfiguracionController.parseKeys(body);
        assertEquals(1, keys.size(), "B-PLA-12a: single object → 1 key");
        assertEquals("PLATAFORMA", keys.get(0).getModulo());
        assertEquals("PLA.Version", keys.get(0).getCodigoParametro());
    }

    /**
     * B-PLA-12b: JSON array body → multiple keys.
     */
    @Test
    void rule12b_parseKeys_array() throws Exception {
        String body = "[{\"idEmpresa\":1,\"modulo\":\"PLATAFORMA\",\"codigoParametro\":\"PLA.Version\"}," +
                       "{\"idEmpresa\":1,\"modulo\":\"APV\",\"codigoParametro\":\"APV.Habilitado\"}]";
        var keys = ConfiguracionController.parseKeys(body);
        assertEquals(2, keys.size(), "B-PLA-12b: array → 2 keys");
    }

    /**
     * B-PLA-12c: Null body → empty list.
     */
    @Test
    void rule12c_parseKeys_null() throws Exception {
        var keys = ConfiguracionController.parseKeys(null);
        assertTrue(keys.isEmpty(), "B-PLA-12c: null → empty list");
    }

    // =========================================================================
    // Integration tests — disabled (require HANA)
    // =========================================================================

    @Disabled("requires HANA — CallServlet dispatcher depends on PLA.Servicio registry")
    @Test
    void intTest_callDispatcher_withHana() throws Exception {
        mockMvc.perform(post("/call")
                        .param("t", "1")
                        .header("X-SESSION-TOKEN", "test-token")
                        .contentType("application/json")
                        .content("{}"))
               .andExpect(status().isOk());
    }

    @Disabled("requires HANA — ConfiguracionServlet UNION ALL query spans 11 schemas")
    @Test
    void intTest_configuracion_withHana() throws Exception {
        mockMvc.perform(get("/Configuraciones")
                        .contentType("application/json")
                        .content("{\"idEmpresa\":1,\"modulo\":\"PLATAFORMA\",\"codigoParametro\":\"PLA.Version\"}"))
               .andExpect(status().isOk());
    }
}
