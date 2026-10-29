import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * READ-ONLY: catalog (reinversion lock table, sequences, IdCarga default) and AGGREGATE counts of the last 24 months (no personal data), authorised in PLAN_FONDOMUTUO_F2.md section 12 (D-F2-7). A fixed whitelist of SELECT statements over SYS.* catalog views;
 * the connection is read-only and rolled back; no INSERT/UPDATE/DELETE/DDL exists in this program.
 * Credentials come from the local .env keys qa01.saphana.* and are never printed or written.
 * usage: java -cp ngdbc.jar HanaFondoMutuoF2b.java <envFile> <outDir>
 */
public class HanaFondoMutuoF2b {
    static final String T = "\"BSCL\".\"BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate\"";
    static final LinkedHashMap<String, String> QUERIES = new LinkedHashMap<>();
    static {
        QUERIES.put("f2b_columnas_bloqueo_reinversion", "SELECT TABLE_NAME, COLUMN_NAME, POSITION, DATA_TYPE_NAME, LENGTH, SCALE, IS_NULLABLE, DEFAULT_VALUE FROM SYS.TABLE_COLUMNS WHERE SCHEMA_NAME = 'BSCL' AND SUBSTR_AFTER(TABLE_NAME, '::') = 'FMM.ProcesarReinversionRescate_BloqueoReinversion' ORDER BY POSITION");
        QUERIES.put("f2b_tipo_bloqueo_reinversion", "SELECT TABLE_NAME, TABLE_TYPE, IS_TEMPORARY, TEMPORARY_TABLE_TYPE FROM SYS.TABLES WHERE SCHEMA_NAME = 'BSCL' AND SUBSTR_AFTER(TABLE_NAME, '::') = 'FMM.ProcesarReinversionRescate_BloqueoReinversion'");
        QUERIES.put("f2b_secuencias", "SELECT SEQUENCE_NAME, START_NUMBER, MIN_VALUE, MAX_VALUE, INCREMENT_BY, IS_CYCLED FROM SYS.SEQUENCES WHERE SCHEMA_NAME = 'BSCL' AND (SEQUENCE_NAME LIKE '%IdAgrupador%' OR SEQUENCE_NAME LIKE '%ReinversionSerieFondo%' OR SEQUENCE_NAME LIKE '%Agrupador%') ORDER BY SEQUENCE_NAME");
        QUERIES.put("f2b_default_idcarga", "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE_NAME, IS_NULLABLE, DEFAULT_VALUE FROM SYS.TABLE_COLUMNS WHERE SCHEMA_NAME = 'BSCL' AND SUBSTR_AFTER(TABLE_NAME, '::') IN ('FMM.SolicitudAporteRescate', 'FMM.ReinversionSerieFondoDestino') AND COLUMN_NAME = 'IdCarga'");
        QUERIES.put("f2b_reinv_medio_pago_24m", "SELECT \"IdTipoMovimientoFondoMutuo\", \"IdMedioDePago\", COUNT(*) AS \"Solicitudes\" FROM " + T + " WHERE \"IdTipoMovimientoFondoMutuo\" IN ('APOREINV', 'RESREINV') AND \"FechaIngresoRegistro\" >= ADD_MONTHS(CURRENT_DATE, -24) GROUP BY \"IdTipoMovimientoFondoMutuo\", \"IdMedioDePago\" ORDER BY 1, 2");
        QUERIES.put("f2b_ingresoegreso_spot_24m", "SELECT \"IdTipoMovimientoFondoMutuo\", CASE WHEN \"IdIngresoEgreso\" IS NULL THEN 'N' ELSE 'S' END AS \"ConIngresoEgreso\", CASE WHEN \"IdOperacionSpot\" IS NULL THEN 'N' ELSE 'S' END AS \"ConOperacionSpot\", COUNT(*) AS \"Solicitudes\" FROM " + T + " WHERE \"FechaIngresoRegistro\" >= ADD_MONTHS(CURRENT_DATE, -24) GROUP BY \"IdTipoMovimientoFondoMutuo\", CASE WHEN \"IdIngresoEgreso\" IS NULL THEN 'N' ELSE 'S' END, CASE WHEN \"IdOperacionSpot\" IS NULL THEN 'N' ELSE 'S' END ORDER BY 1, 2, 3");
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
