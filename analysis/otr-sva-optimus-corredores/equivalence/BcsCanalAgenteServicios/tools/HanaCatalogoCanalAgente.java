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
 * BcsCanalAgenteServicios: READ-ONLY catalog of the HANA QA objects the CanalAgente SQL touches (columns, primary keys, table kind, procedures, views).
 * A fixed whitelist of SELECT statements over SYS catalog views; the connection is read-only and rolled back; there is no INSERT/UPDATE/DELETE/DDL here.
 * Credentials come from the root .env keys qa01.saphana.* read in memory by this process and are never printed or written.
 * usage: java -cp ngdbc.jar HanaCatalogoCanalAgente.java <envFile> <outDir>
 */
public class HanaCatalogoCanalAgente {
    static final String TABLAS = "'BSCL.Portafolio.BD::POR.Portafolio','BSCL.SocioNegocio.BD::CLI.Cliente','BSCL.SocioNegocio.BD::PER.Persona',"
            + "'BSCL.SocioNegocio.BD::PER.DocumentoIdentidad','BSCL.Facturacion.BD::FAC.Comprobante',"
            + "'BSCL.CanalAgente.BD::ConsultarHistorialMovimientosCuentaInversion_Proceso'";
    static final LinkedHashMap<String, String> QUERIES = new LinkedHashMap<>();
    static {
        QUERIES.put("canal_columnas_tablas", "SELECT TABLE_NAME, COLUMN_NAME, POSITION, DATA_TYPE_NAME, LENGTH, SCALE, IS_NULLABLE, DEFAULT_VALUE, GENERATION_TYPE FROM SYS.TABLE_COLUMNS WHERE SCHEMA_NAME = 'BSCL' AND TABLE_NAME IN (" + TABLAS + ") ORDER BY TABLE_NAME, POSITION");
        QUERIES.put("canal_claves", "SELECT TABLE_NAME, COLUMN_NAME, POSITION FROM SYS.CONSTRAINTS WHERE SCHEMA_NAME = 'BSCL' AND IS_PRIMARY_KEY = 'TRUE' AND TABLE_NAME IN (" + TABLAS + ") ORDER BY TABLE_NAME, POSITION");
        QUERIES.put("canal_tablas", "SELECT TABLE_NAME, TABLE_TYPE, IS_COLUMN_TABLE, IS_TEMPORARY, TEMPORARY_TABLE_TYPE FROM SYS.TABLES WHERE SCHEMA_NAME = 'BSCL' AND (TABLE_NAME IN (" + TABLAS + ") OR TABLE_NAME LIKE 'BSCL.CanalAgente.BD%') ORDER BY TABLE_NAME");
        QUERIES.put("canal_commit_action", "SELECT TABLE_NAME, COMMIT_ACTION FROM SYS.TABLES WHERE SCHEMA_NAME = 'BSCL' AND TABLE_NAME LIKE 'BSCL.CanalAgente.BD%' ORDER BY TABLE_NAME");
        QUERIES.put("canal_tipos_objeto", "SELECT OBJECT_NAME, OBJECT_TYPE FROM SYS.OBJECTS WHERE SCHEMA_NAME = 'BSCL' AND (OBJECT_NAME LIKE 'BSCL.CanalAgente.%' OR OBJECT_NAME = 'BSCL.Portafolio.BD.Vistas::ConsultarPortafolio') ORDER BY OBJECT_NAME");
        QUERIES.put("canal_columnas_vistas", "SELECT VIEW_NAME, COLUMN_NAME, POSITION, DATA_TYPE_NAME, LENGTH, SCALE FROM SYS.VIEW_COLUMNS WHERE SCHEMA_NAME = 'BSCL' AND VIEW_NAME = 'BSCL.Portafolio.BD.Vistas::ConsultarPortafolio' ORDER BY POSITION");
        QUERIES.put("canal_procedimientos", "SELECT PROCEDURE_NAME FROM SYS.PROCEDURES WHERE SCHEMA_NAME = 'BSCL' AND PROCEDURE_NAME LIKE 'BSCL.CanalAgente.%' ORDER BY PROCEDURE_NAME");
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
                    System.out.println(q.getKey() + ": ERROR " + e.getErrorCode());
                }
            }
            cn.rollback();
        }
    }
}
