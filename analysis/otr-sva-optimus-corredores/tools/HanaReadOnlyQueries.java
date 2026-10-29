import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * READ-ONLY queries against HANA QA for the Jobs H1/H4 cutover design. A fixed whitelist of SELECT statements only
 * (see QUERIES); the connection is read-only and rolled back; no INSERT/UPDATE/DELETE/DDL exists in this program.
 * Credentials come from the local .env keys qa01.saphana.* and are never printed or written.
 * usage: java -cp ngdbc.jar HanaReadOnlyQueries.java <envFile> <outDir>
 */
public class HanaReadOnlyQueries {
    static final String P = "\"BSCL\".\"BSCL.Plataforma.BD::BAT.Programa\"";
    static final String TP = "\"BSCL\".\"BSCL.Plataforma.BD::BAT.TipoPrograma\"";
    static final String AG = "\"BSCL\".\"BSCL.Plataforma.BD::BAT.Agenda\"";
    static final LinkedHashMap<String, String> QUERIES = new LinkedHashMap<>();
    static {
        QUERIES.put("q1_nodo_tipoprograma", "SELECT \"Nodo\", COUNT(*) AS \"Tipos\" FROM " + TP + " GROUP BY \"Nodo\" ORDER BY 1");
        QUERIES.put("q2_tipos_programa", "SELECT \"IdTipoPrograma\", \"Modulo\", \"NombreClasePrograma\", \"TipoClasePrograma\", \"Nodo\", \"MaximoIntentos\", \"EsPauta\", \"TipoSalida\", \"FormatoSalida\", \"TieneLog\" FROM " + TP + " ORDER BY \"Modulo\", \"IdTipoPrograma\"");
        QUERIES.put("q3_nodo_usado_por_tipo", "SELECT \"Nodo\", \"IdTipoPrograma\", COUNT(*) AS \"Programas\" FROM " + P + " GROUP BY \"Nodo\", \"IdTipoPrograma\" ORDER BY 1, 2");
        QUERIES.put("q4_estados_bloqueos", "SELECT \"idEstado\", \"Bloqueo\", COUNT(*) AS \"Programas\" FROM " + P + " GROUP BY \"idEstado\", \"Bloqueo\" ORDER BY 1, 2");
        QUERIES.put("q5_atascados_asi", "SELECT TOP 50 \"IdPrograma\", \"IdTipoPrograma\", \"Nodo\", \"Intento\", \"Bloqueo\", SUBSTRING(\"Resultado\", 1, 120) AS \"Resultado\", \"fechaEjecucion\", \"fechaFin\" FROM " + P + " WHERE \"idEstado\" = 'ASI' ORDER BY \"fechaEjecucion\" DESC");
        QUERIES.put("q6_agenda_resumen", "SELECT \"IdTipoPrograma\", \"EsDemonio\", \"habilitado\", COUNT(*) AS \"Agendas\" FROM " + AG + " GROUP BY \"IdTipoPrograma\", \"EsDemonio\", \"habilitado\" ORDER BY 1, 2, 3");
        QUERIES.put("q7_nodo_programa_vs_tipo", "SELECT P.\"Nodo\" AS \"NodoPrograma\", TP.\"Nodo\" AS \"NodoTipo\", COUNT(*) AS \"Programas\" FROM " + P + " P INNER JOIN " + TP + " TP ON P.\"IdTipoPrograma\" = TP.\"IdTipoPrograma\" GROUP BY P.\"Nodo\", TP.\"Nodo\" ORDER BY 1, 2");
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
