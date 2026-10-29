import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * Probe (authorised by the user for QA, 2026-10-22): does a DDL on a SESSION-LOCAL temporary table implicitly COMMIT the
 * writes made earlier in the same transaction on HANA?
 * ONLY session-local temporary tables named #fmprobe_* are created, written and dropped, on one connection, which is
 * closed at the end (HANA drops local temporary tables with the session). No persistent object and no business table
 * is touched. Every statement is checked against a closed allow-list before it runs.
 * Credentials come from the local .env keys qa01.saphana.* and are never printed or written.
 * usage: java -cp ngdbc.jar HanaProbarDdlCommit.java <envFile>
 */
public class HanaProbarDdlCommit {
    static final String[] PERMITIDAS = {
        "CREATE LOCAL TEMPORARY TABLE #fmprobe_", "INSERT INTO #fmprobe_", "DROP TABLE #fmprobe_", "SELECT COUNT(*) FROM #fmprobe_"
    };

    static void ejecutar(Connection cn, String sql) throws SQLException {
        boolean ok = false;
        for (String p : PERMITIDAS) ok |= sql.startsWith(p);
        if (!ok) throw new IllegalStateException("statement not allowed");
        try (Statement st = cn.createStatement()) {
            st.execute(sql);
        }
    }

    static int contar(Connection cn, String tabla) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tabla;
        if (!sql.startsWith(PERMITIDAS[3])) throw new IllegalStateException("statement not allowed");
        try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void main(String[] a) throws Exception {
        Map<String, String> env = new HashMap<>();
        for (String l : Files.readAllLines(Paths.get(a[0]), StandardCharsets.UTF_8)) {
            int i = l.indexOf('=');
            if (i > 0 && !l.startsWith("#")) env.put(l.substring(0, i).trim(), l.substring(i + 1).trim());
        }
        Properties p = new Properties();
        p.setProperty("user", env.get("qa01.saphana.dbuser"));
        p.setProperty("password", env.get("qa01.saphana.dbpass"));
        for (String kv : env.getOrDefault("qa01.saphana.dbprop", "").split(";")) {
            int i = kv.indexOf('=');
            if (i > 0) p.setProperty(kv.substring(0, i).trim(), kv.substring(i + 1).trim());
        }
        Class.forName(env.getOrDefault("qa01.saphana.dbdrv", "com.sap.db.jdbc.Driver"));
        try (Connection cn = DriverManager.getConnection(env.get("qa01.saphana.dbURL"), p)) {
            cn.setAutoCommit(false);
            ejecutar(cn, "CREATE LOCAL TEMPORARY TABLE #fmprobe_a (id INTEGER)");
            cn.commit();
            // Case 1: uncommitted INSERT, then a CREATE LOCAL TEMPORARY TABLE, then ROLLBACK
            ejecutar(cn, "INSERT INTO #fmprobe_a VALUES (1)");
            ejecutar(cn, "CREATE LOCAL TEMPORARY TABLE #fmprobe_b (id INTEGER)");
            cn.rollback();
            int caso1 = contar(cn, "#fmprobe_a");
            cn.rollback();
            // Case 2: uncommitted INSERT, then a DROP TABLE of a local temporary table, then ROLLBACK
            ejecutar(cn, "INSERT INTO #fmprobe_a VALUES (2)");
            ejecutar(cn, "DROP TABLE #fmprobe_b");
            cn.rollback();
            int caso2 = contar(cn, "#fmprobe_a");
            cn.rollback();
            ejecutar(cn, "DROP TABLE #fmprobe_a");
            cn.commit();
            System.out.println("case1 rows surviving rollback after CREATE LOCAL TEMPORARY TABLE: " + caso1
                + (caso1 > 0 ? "  => DDL COMMITS implicitly" : "  => no implicit commit"));
            System.out.println("case2 rows surviving rollback after DROP TABLE (local temporary): " + caso2
                + " (total, includes case 1 rows)" + (caso2 > caso1 ? "  => DDL COMMITS implicitly" : "  => no implicit commit"));
        }
    }
}
