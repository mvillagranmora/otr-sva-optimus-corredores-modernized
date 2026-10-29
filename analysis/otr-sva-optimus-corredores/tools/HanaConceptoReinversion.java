import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * READ-ONLY: columns of the Tesoreria concept tables and the 3 concept catalog rows APORFM/APOREI/APOFUS (no personal data); user asked to confirm which concept APOREINV uses. A fixed whitelist of SELECT statements over SYS.* catalog views;
 * the connection is read-only and rolled back; no INSERT/UPDATE/DELETE/DDL exists in this program.
 * Credentials come from the local .env keys qa01.saphana.* and are never printed or written.
 * usage: java -cp ngdbc.jar HanaConceptoReinversion.java <envFile> <outDir>
 */
public class HanaConceptoReinversion {
    static final LinkedHashMap<String, String> QUERIES = new LinkedHashMap<>();
    static {
        QUERIES.put("concepto_tes_top", "SELECT TOP 25 \"IdConcepto\", COUNT(*) AS \"Registros\" FROM \"BSCL\".\"BSCL.Tesoreria.BD::TES.IngresoEgreso\" GROUP BY \"IdConcepto\" ORDER BY 2 DESC");
        QUERIES.put("concepto_tes_total", "SELECT COUNT(*) AS \"Total\", MIN(\"FechaIngresoRegistro\") AS \"Desde\", MAX(\"FechaIngresoRegistro\") AS \"Hasta\" FROM \"BSCL\".\"BSCL.Tesoreria.BD::TES.IngresoEgreso\"");
        QUERIES.put("concepto_tes_fmm_prefijo", "SELECT \"IdConcepto\", COUNT(*) AS \"Registros\" FROM \"BSCL\".\"BSCL.Tesoreria.BD::TES.IngresoEgreso\" WHERE \"IdConcepto\" LIKE 'APO%' OR \"IdConcepto\" LIKE 'RES%' OR \"IdConcepto\" LIKE '%FM%' GROUP BY \"IdConcepto\" ORDER BY 2 DESC");
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
        Path out = Paths.get(a[1]);
        Files.createDirectories(out);
        try (Connection cn = DriverManager.getConnection(env.get("qa01.saphana.dbURL"), p)) {
            cn.setReadOnly(true);
            cn.setAutoCommit(false);
            for (Map.Entry<String, String> q : QUERIES.entrySet()) {
                if (!q.getValue().startsWith("SELECT")) throw new IllegalStateException("only SELECT is allowed");
                try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(q.getValue());
                     BufferedWriter w = Files.newBufferedWriter(out.resolve(q.getKey() + ".csv"), StandardCharsets.UTF_8)) {
                    ResultSetMetaData md = rs.getMetaData();
                    int n = md.getColumnCount();
                    StringBuilder h = new StringBuilder();
                    for (int c = 1; c <= n; c++) h.append(c > 1 ? "," : "").append(md.getColumnLabel(c));
                    w.write(h + "\n");
                    int rows = 0;
                    while (rs.next()) {
                        StringBuilder r = new StringBuilder();
                        for (int c = 1; c <= n; c++) {
                            String v = rs.getString(c);
                            if (v == null) v = "";
                            v = v.replace("\r", " ").replace("\n", " ");
                            if (v.contains(",") || v.contains("\"")) v = "\"" + v.replace("\"", "\"\"") + "\"";
                            r.append(c > 1 ? "," : "").append(v);
                        }
                        w.write(r + "\n");
                        rows++;
                    }
                    System.out.println(q.getKey() + ": " + rows + " rows");
                } catch (SQLException e) {
                    System.out.println(q.getKey() + ": ERROR " + e.getErrorCode() + " " + String.valueOf(e.getMessage()).split("\n")[0]);
                }
            }
            cn.rollback();
        }
    }
}
