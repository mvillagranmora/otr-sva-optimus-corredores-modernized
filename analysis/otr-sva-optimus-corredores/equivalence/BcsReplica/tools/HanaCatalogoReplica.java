import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * BcsReplica: READ-ONLY catalog of the HANA QA objects the Replica SQL touches (columns, primary keys, triggers, sequences).
 * A fixed whitelist of SELECT statements over SYS catalog views; the connection is read-only and rolled back; there is no INSERT/UPDATE/DELETE/DDL here.
 * Credentials come from the root .env keys qa01.saphana.* and are never printed or written.
 * usage: java -cp ngdbc.jar HanaCatalogoReplica.java <envFile> <outDir>
 */
public class HanaCatalogoReplica {
    static final String TABLAS = "'BSCL.Plataforma.BD::PLA.Replica.WorkStack','BSCL.Plataforma.BD::PLA.ResumenReplica','BSCL.Replica.BD::EnvioPostPauta',"
            + "'BSCL.CuentaInversion.BD::CTA.SaldoFechaEfectiva','BSCL.CuentaInversion.BD::CTA.SaldoFechaMovimiento',"
            + "'BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion','BSCL.Custodia.BD::HIS.CustodiaFechaOperacion',"
            + "'BSCL.Replica.BD::TBCTCIRV','BSCL.Replica.BD::TBCTCURV','BSCL.Replica.BD::TBCTCIRF','BSCL.Replica.BD::TBCTCURF','BSCL.Replica.BD::TBCTCIIF',"
            + "'BSCL.Replica.BD::TBCTCTCL','BSCL.Replica.BD::TBCTTRTP','BSCL.Replica.BD::TBCTCIFM','BSCL.Replica.BD::TBCTCUFM','BSCL.Replica.BD::TBCPMSPC',"
            + "'BSCL.Replica.BD::TBCPDTPC','BSCL.Replica.BD::TBCTGTIA','BSCL.Replica.BD::TBCTTRPT'";
    static final String VISTAS = "'BSCL.Replica.BD.Vistas::Sebra.TBCTCIRV','BSCL.Replica.BD.Vistas::Sebra.TBCTGTIA','BSCL.Replica.BD.Vistas::Sebra.TBCPMSPC'";
    static final LinkedHashMap<String, String> QUERIES = new LinkedHashMap<>();
    static {
        QUERIES.put("replica_columnas_tablas", "SELECT TABLE_NAME, COLUMN_NAME, POSITION, DATA_TYPE_NAME, LENGTH, SCALE, IS_NULLABLE, DEFAULT_VALUE, GENERATION_TYPE FROM SYS.TABLE_COLUMNS WHERE SCHEMA_NAME = 'BSCL' AND TABLE_NAME IN (" + TABLAS + ") ORDER BY TABLE_NAME, POSITION");
        QUERIES.put("replica_claves", "SELECT TABLE_NAME, COLUMN_NAME, POSITION FROM SYS.CONSTRAINTS WHERE SCHEMA_NAME = 'BSCL' AND IS_PRIMARY_KEY = 'TRUE' AND TABLE_NAME IN (" + TABLAS + ") ORDER BY TABLE_NAME, POSITION");
        QUERIES.put("replica_columnas_vistas", "SELECT VIEW_NAME, COLUMN_NAME, POSITION, DATA_TYPE_NAME, LENGTH, SCALE FROM SYS.VIEW_COLUMNS WHERE SCHEMA_NAME = 'BSCL' AND VIEW_NAME IN (" + VISTAS + ") ORDER BY VIEW_NAME, POSITION");
        QUERIES.put("replica_tipos_objeto", "SELECT OBJECT_NAME, OBJECT_TYPE FROM SYS.OBJECTS WHERE SCHEMA_NAME = 'BSCL' AND OBJECT_NAME LIKE 'BSCL.Replica.BD%' AND (OBJECT_NAME LIKE '%Sebra.%' OR OBJECT_NAME LIKE '%EnvioPostPauta%') ORDER BY OBJECT_NAME");
        QUERIES.put("replica_triggers", "SELECT TRIGGER_NAME, SUBJECT_TABLE_NAME, TRIGGER_ACTION_TIME, TRIGGER_EVENT, TRIGGER_GRANULARITY FROM SYS.TRIGGERS WHERE SCHEMA_NAME = 'BSCL' AND TRIGGER_NAME LIKE 'BSCL.Plataforma.BD::PLA.Replica.%'");
        QUERIES.put("replica_secuencia_workstack", "SELECT SEQUENCE_NAME, START_NUMBER, MIN_VALUE, MAX_VALUE, INCREMENT_BY, IS_CYCLED FROM SYS.SEQUENCES WHERE SCHEMA_NAME = 'BSCL' AND SEQUENCE_NAME LIKE '%WorkStack%'");
        QUERIES.put("replica_dump_nombres_sebra", "SELECT DISTINCT TABLE_NAME FROM SYS.TABLES WHERE SCHEMA_NAME = 'BSCL' AND TABLE_NAME LIKE 'BSCL.Replica.BD::TBC%' ORDER BY 1");
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
