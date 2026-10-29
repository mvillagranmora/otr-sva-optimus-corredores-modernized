package cl.bcs.utils;

import cl.bcs.utils.equivalencia.Golden;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Scope A (binding decision 2026-10-16): BcsUtils is a PURE common library. The legacy classes below are NOT ported
 * (the reason is in each row) and must not reappear here; the first-pass classes PasswordScanner and SessionTimeout are
 * superseded by cl.bcs.utils.clave and cl.bcs.utils.sesion (their behavior contradicted the legacy: spaces rejected,
 * invented WEB/default parameter keys). Also: the golden inventory, so a deleted fixture can never pass silently.
 */
class AlcanceLibreriaTest {

    @ParameterizedTest(name = "UTIL-90 not ported: {0} ({1})")
    @CsvSource(delimiter = '|', value = {
            "cl.bcs.utils.SQL|data access: Spring JDBC per service",
            "cl.bcs.utils.JdbcUtil|data access: Spring JDBC per service",
            "cl.bcs.utils.NamedParameterStatement|data access: NamedParameterJdbcTemplate",
            "cl.bcs.utils.NamedCallableStatement|data access: SimpleJdbcCall",
            "cl.bcs.utils.ListMapResultSet|data access (CachedRowSet)",
            "cl.bcs.utils.NuamPreparedStatement|data access",
            "cl.bcs.sql.utils.SqlBulk|data access",
            "cl.bcs.sql.utils.SqlUtil|data access (cl.bcs.sql.*)",
            "cl.bcs.sql.metadata.SqlMetadata|data access (cl.bcs.sql.*)",
            "cl.bcs.sql.generator.SqlGenerator|data access (cl.bcs.sql.*)",
            "cl.bcs.utils.ProcesaLlamadaServletCall|servlet/platform DB: enforcement of RULE-022/023/026/027 moves to Phase 5",
            "cl.bcs.utils.ServletUtils|servlet bound",
            "cl.bcs.utils.JavaView|platform DB bound",
            "cl.bcs.utils.JavaViewCache|platform DB bound",
            "cl.bcs.utils.PluginLoader|TomEE class loading",
            "cl.bcs.utils.AgregarLog|platform DB bound",
            "cl.bcs.utils.AgregarNotificacion|platform DB bound",
            "cl.bcs.utils.Locker|platform DB bound",
            "cl.bcs.utils.CallRestService|platform bound HTTP",
            "cl.bcs.utils.ConnectionUtils|connections: Spring datasource",
            "cl.bcs.utils.PropertiesUtil|TomEE configuration",
            "cl.bcs.utils.JobFileManager|platform bound (SFTP/jobs)",
            "cl.bcs.utils.ConcurrentArchiver|platform bound",
            "cl.bcs.utils.StatsD|replaced by Micrometer",
            "cl.bcs.utils.Log|replaced by SLF4J",
            "cl.bcs.utils.Crypto|RULE-077: static AES key/IV",
            "cl.bcs.utils.CryptoUtils|RULE-077: key constant",
            "cl.bcs.utils.SesionUtils|DB-bound; pure parts in cl.bcs.utils.sesion",
            "cl.bcs.utils.ValidarNuevaClave|DB-bound; pure parts in cl.bcs.utils.clave",
            "cl.bcs.utils.PasswordScanner|first pass, superseded by cl.bcs.utils.clave",
            "cl.bcs.utils.SessionTimeout|first pass, superseded by cl.bcs.utils.sesion",
            "cl.bcs.utils.JsonWebToken|dead code in the legacy (decision 5, 2026-10-16): not ported"})
    void util90_noPortado(String clase, String razon) {
        assertThatThrownBy(() -> Class.forName(clase, false, getClass().getClassLoader())).as(razon).isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    @DisplayName("UTIL-91: golden inventory: every fixture is present with at least the recorded number of cases (1895 in total; jwt removed by decision 5)")
    void util91_inventarioGolden() {
        Map<String, Integer> esperado = new LinkedHashMap<>();
        esperado.put("any/rut", 80);
        esperado.put("any/strings", 101);
        esperado.put("any/javacall", 16);
        esperado.put("any/timer", 64);
        esperado.put("any/files", 25);
        esperado.put("any/zip", 43);
        esperado.put("db/password", 198);
        esperado.put("db/session", 42);
        for (String z : new String[]{"America_Santiago", "UTC"}) {
            esperado.put(z + "/json", 65);
            esperado.put(z + "/collections", 27);
            esperado.put(z + "/dateutil", 209);
            esperado.put(z + "/fecha", 362);
        }
        int total = 0;
        for (Map.Entry<String, Integer> e : esperado.entrySet()) {
            String[] p = e.getKey().split("/");
            int n = Golden.casos(p[0], p[1]).size();
            assertThat(n).as(e.getKey()).isEqualTo(e.getValue());
            total += n;
        }
        assertThat(total).isEqualTo(1895);
        assertThat(getClass().getClassLoader().getResource("golden/PROVENANCE.md")).as("provenance note").isNotNull();
    }
}
