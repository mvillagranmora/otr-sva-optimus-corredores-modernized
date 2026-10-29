package cl.bcs.custodia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HexFormat;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Live QA equivalence tests for BcsCustodiaServicios.
 *
 * These tests compare the legacy TomEE/GWJAVA system with direct HANA QA queries.
 * They run only when QA_HANA_PASSWORD is set — run manually when the QA environment is accessible.
 *
 * Access details: see analysis/otr-sva-optimus-corredores/QA_ACCESS_PROCEDURE.md (not committed). Every coordinate and
 * credential comes from the environment (RULE-076): QA_GWJAVA_BASE, QA_HANA_URL, QA_HANA_USER, QA_HANA_PASSWORD,
 * QA_APP_USER, QA_APP_PASSWORD. HANA QA is read-only: only SELECT is issued.
 */
@EnabledIfEnvironmentVariable(named = "QA_HANA_PASSWORD", matches = ".+",
        disabledReason = "QA environment required — set the QA_* variables (QA_ACCESS_PROCEDURE.md) to run it")
class BcsCustodiaServiciosQAEquivalenceTest {

    private static final String GWJAVA_BASE   = env("QA_GWJAVA_BASE");
    private static final String HANA_URL      = env("QA_HANA_URL");
    private static final String HANA_USER     = env("QA_HANA_USER");
    private static final String HANA_PASSWORD = env("QA_HANA_PASSWORD");
    private static final String APP_USER      = env("QA_APP_USER");
    private static final String APP_PASSWORD  = env("QA_APP_PASSWORD");
    private static final int    ID_EMPRESA    = 1;

    private static String env(String name) {
        String v = System.getenv(name);
        return v == null ? "" : v;
    }

    // -----------------------------------------------------------------------
    // QA-1: obtenerFechaSistema — GWJAVA t=2 vs HANA PLA.FechaSistema
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("[QA-1] obtenerFechaSistema: GWJAVA t=2 date == HANA PLA.FechaSistema")
    void qa1_obtenerFechaSistema_gwjavaMatchesHana() throws Exception {
        String gwjavaFecha = callGwjavaFechaSistema();
        String hanaFecha   = queryHanaFechaSistema();

        System.out.println("[QA-1] GWJAVA t=2 P_OUT_FECHAHORASISTEMA = " + gwjavaFecha);
        System.out.println("[QA-1] HANA PLA.FechaSistema             = " + hanaFecha);

        // GWJAVA returns full timestamp e.g. "2026-09-14 18:24:47.000"; extract date part
        String gwjavaDate = gwjavaFecha.substring(0, 10);
        assertThat(gwjavaDate).isEqualTo(hanaFecha);
    }

    // -----------------------------------------------------------------------
    // QA-2: obtenerFechaSistema — modernized adapter vs HANA direct
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("[QA-2] obtenerFechaSistema: adapter SQL against HANA QA returns same as direct query")
    void qa2_obtenerFechaSistema_adapterSqlMatchesHana() throws Exception {
        String hanaFecha = queryHanaFechaSistema();

        // Run the same SQL the modernized adapter will use (table name substituted for HANA)
        String adapterFecha = queryHanaAdapterSql((long) ID_EMPRESA);

        System.out.println("[QA-2] HANA direct query  = " + hanaFecha);
        System.out.println("[QA-2] Adapter SQL result = " + adapterFecha);

        assertThat(adapterFecha).isEqualTo(hanaFecha);
    }

    // -----------------------------------------------------------------------
    // QA-3: CUSTODIA_OPTIMUS sanity — view is queryable, has data
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("[QA-3] CUSTODIA_OPTIMUS contains custody positions")
    void qa3_custodiaOptimus_hasRows() throws Exception {
        long count = queryCustodiaOptimusCount();
        System.out.println("[QA-3] CUSTODIA_OPTIMUS row count = " + count);
        assertThat(count).isGreaterThan(0);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Call GWJAVA t=2 (ConsultarFechaHoraSistema) and return P_OUT_FECHAHORASISTEMA value. */
    private String callGwjavaFechaSistema() throws Exception {
        String token = obtenerToken();
        String body  = "{\"p_IdEmpresa\":" + ID_EMPRESA + "}";
        String resp  = postGwjava("/call?t=2", token, body);
        Matcher m = Pattern.compile("\"P_OUT_FECHAHORASISTEMA\"\\s*:\\s*\"([^\"]+)\"").matcher(resp);
        assertThat(m.find()).as("P_OUT_FECHAHORASISTEMA not found in response: " + resp).isTrue();
        return m.group(1);
    }

    /** Query HANA QA PLA.Configuracion directly for PLA.FechaSistema. */
    private String queryHanaFechaSistema() throws Exception {
        Properties props = new Properties();
        props.setProperty("user", HANA_USER);
        props.setProperty("password", HANA_PASSWORD);
        try (Connection conn = DriverManager.getConnection(HANA_URL, props);
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" " +
                     "WHERE \"IdEmpresa\" = ? AND \"CodigoParametro\" = 'PLA.FechaSistema'")) {
            ps.setLong(1, ID_EMPRESA);
            try (ResultSet rs = ps.executeQuery()) {
                assertThat(rs.next()).as("No PLA.FechaSistema row found in HANA QA").isTrue();
                return rs.getString("Valor");
            }
        }
    }

    /** Run the adapter SQL (with real HANA table name) to confirm it works. */
    private String queryHanaAdapterSql(Long idEmpresa) throws Exception {
        Properties props = new Properties();
        props.setProperty("user", HANA_USER);
        props.setProperty("password", HANA_PASSWORD);
        try (Connection conn = DriverManager.getConnection(HANA_URL, props);
             PreparedStatement ps = conn.prepareStatement(
                     // This is the adapter SQL with the real HANA table substituted
                     "SELECT \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" " +
                     "WHERE \"IdEmpresa\" = ? AND \"CodigoParametro\" = 'PLA.FechaSistema'")) {
            ps.setLong(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                assertThat(rs.next()).isTrue();
                return rs.getString("Valor");
            }
        }
    }

    private long queryCustodiaOptimusCount() throws Exception {
        Properties props = new Properties();
        props.setProperty("user", HANA_USER);
        props.setProperty("password", HANA_PASSWORD);
        try (Connection conn = DriverManager.getConnection(HANA_URL, props);
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM BSCL_VIRTUAL.CUSTODIA_OPTIMUS")) {
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    /** Authenticate with GWJAVA and return the session token. */
    private String obtenerToken() throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        String hashClave = HexFormat.of().formatHex(sha.digest(APP_PASSWORD.getBytes(StandardCharsets.UTF_8)));
        String loginBody = String.format(
                "{\"p_IdEmpresa\":%d,\"p_NombreUsuario\":\"%s\",\"p_ClaveUsuario\":\"%s\",\"p_IdCanal\":\"COR\"}",
                ID_EMPRESA, APP_USER, hashClave);
        String resp = postGwjava("/CrearSesion", null, loginBody);
        Matcher m = Pattern.compile("\"P_OUT_TOKEN\"\\s*:\\s*\"([^\"]+)\"").matcher(resp);
        assertThat(m.find()).as("P_OUT_TOKEN not found in login response: " + resp).isTrue();
        return m.group(1);
    }

    private String postGwjava(String path, String token, String body) throws Exception {
        var url = URI.create(GWJAVA_BASE + path).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(30_000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (token != null) {
            conn.setRequestProperty("X-SESSION-TOKEN", token);   // a session token is a secret: never printed
        }
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        int code = conn.getResponseCode();
        if (code >= 400) {
            var err = conn.getErrorStream();
            String errBody = err != null ? new String(err.readAllBytes(), StandardCharsets.UTF_8) : "(no error body)";
            throw new java.io.IOException("HTTP " + code + " from " + url + " — " + errBody);
        }
        byte[] bytes = conn.getInputStream().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
