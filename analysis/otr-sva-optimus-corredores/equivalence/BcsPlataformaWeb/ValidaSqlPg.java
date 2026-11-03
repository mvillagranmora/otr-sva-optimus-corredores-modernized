import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** Solo analisis: prepara (Parse/Describe) cada sentencia de sql/postgres contra Postgres QA sin ejecutarla. Uso: java -cp jar ValidaSqlPg.java <.env> <carpeta sql/postgres>. */
public class ValidaSqlPg {

    /** :Nombre -> ? fuera de comillas, como SqlNombrado */
    static String posicional(String sql) {
        StringBuilder sb = new StringBuilder();
        boolean simple = false, doble = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (simple) { if (c == '\'') simple = false; }
            else if (doble) { if (c == '"') doble = false; }
            else if (c == '\'') simple = true;
            else if (c == '"') doble = true;
            else if (c == ':' && i + 1 < sql.length() && Character.isJavaIdentifierStart(sql.charAt(i + 1))) {
                int j = i + 2;
                while (j < sql.length() && Character.isJavaIdentifierPart(sql.charAt(j))) j++;
                sb.append('?');
                i = j - 1;
                continue;
            }
            sb.append(c);
        }
        String t = sb.toString().stripTrailing();
        return t.endsWith(";") ? t.substring(0, t.length() - 1) : t;
    }

    public static void main(String[] a) throws Exception {
        Map<String, String> e = new HashMap<>();
        for (String l : Files.readAllLines(Path.of(a[0]), StandardCharsets.UTF_8)) {
            l = l.strip();
            if (l.isEmpty() || l.startsWith("#") || !l.contains("=")) continue;
            int i = l.indexOf('=');
            e.put(l.substring(0, i).strip(), l.substring(i + 1).strip());
        }
        Properties p = new Properties();
        p.setProperty("user", e.get("qa01.postgres.dbuser"));
        p.setProperty("password", e.get("qa01.postgres.dbpass"));
        DriverManager.setLoginTimeout(25);
        List<Path> archivos;
        try (var s = Files.list(Path.of(a[1]))) { archivos = s.filter(x -> x.toString().endsWith(".sql")).sorted().toList(); }
        int ok = 0, mal = 0;
        try (Connection c = DriverManager.getConnection(e.get("qa01.postgres.dbURL"), p)) {
            c.setReadOnly(true);
            try (Statement t = c.createStatement()) { t.execute("set lock_timeout = '3s'"); t.execute("set statement_timeout = '10s'"); }
            for (Path f : archivos) {
                String id = f.getFileName().toString().replace(".sql", "");
                System.out.println("... " + id); System.out.flush();
                String sql = posicional(Files.readString(f, StandardCharsets.UTF_8));
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ParameterMetaData pm = ps.getParameterMetaData();
                    ResultSetMetaData rm = ps.getMetaData();
                    System.out.println("OK    " + id + " | parametros " + pm.getParameterCount() + " | columnas " + (rm == null ? "-" : rm.getColumnCount()));
                    ok++;
                } catch (SQLException ex) {
                    String m = String.valueOf(ex.getMessage()).replace('\n', ' ');
                    System.out.println("ERROR " + id + " | " + ex.getSQLState() + " | " + m.substring(0, Math.min(200, m.length())));
                    mal++;
                    if (c.getAutoCommit() == false) c.rollback();
                }
            }
        }
        System.out.println("RESUMEN ok=" + ok + " error=" + mal);
    }
}
