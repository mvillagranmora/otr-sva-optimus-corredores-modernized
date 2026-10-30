package cl.bcs.replica.kit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Sql {
    private Sql() {
    }

    public static String recurso(String nombre) {
        try (InputStream in = Sql.class.getClassLoader().getResourceAsStream("cl/bcs/replica/sql/" + nombre)) {
            if (in == null) throw new IllegalStateException("can't load resource:" + nombre);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean exec(Connection conn, String sql) throws SQLException {
        if (conn == null) throw new SQLException("Null connection");
        if (sql == null) throw new SQLException("Null SQL statement");
        try (Statement st = conn.createStatement()) {
            return st.execute(sql);
        }
    }

    public static void batch(Connection conn, String sql, List<Map<String, Object>> filas) throws SQLException {
        String sinComentarios = sql.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)", "");
        Map<String, List<Integer>> indices = new LinkedHashMap<>();
        String parseado = parsear(sinComentarios, indices);
        try (PreparedStatement ps = conn.prepareStatement(parseado)) {
            try {
                for (Map<String, Object> fila : filas) {
                    for (Map.Entry<String, List<Integer>> e : indices.entrySet()) {
                        Object v = fila.get(e.getKey());
                        for (int i : e.getValue()) {
                            if (v == null) ps.setNull(i, Types.VARCHAR);
                            else ps.setObject(i, v);
                        }
                    }
                    ps.addBatch();
                }
                if (indices.isEmpty() && filas.isEmpty()) ps.addBatch();
                ps.executeBatch();
            } catch (SQLException cause) {
                String m = cause.getMessage();
                if (cause.getCause() != null) m = m + " " + cause.getCause().getMessage();
                SQLException e = new SQLException(m + " Query: " + sql + " Parameters: " + Arrays.deepToString(filas.toArray()), cause.getSQLState(), cause.getErrorCode());
                e.setNextException(cause);
                throw e;
            }
        }
    }

    static String parsear(String query, Map<String, List<Integer>> indices) {
        StringBuilder out = new StringBuilder(query.length());
        boolean simple = false;
        boolean doble = false;
        int n = 1;
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            if (simple) {
                if (c == '\'') simple = false;
            } else if (doble) {
                if (c == '"') doble = false;
            } else if (c == '\'') {
                simple = true;
            } else if (c == '"') {
                doble = true;
            } else if (c == ':' && i + 1 < query.length() && Character.isJavaIdentifierStart(query.charAt(i + 1))) {
                int j = i + 2;
                while (j < query.length() && Character.isJavaIdentifierPart(query.charAt(j))) j++;
                String nombre = query.substring(i + 1, j);
                c = '?';
                i += nombre.length();
                indices.computeIfAbsent(nombre, k -> new ArrayList<>()).add(n++);
            }
            out.append(c);
        }
        return out.toString();
    }

    public static Map<String, Object> fila(Object... pares) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < pares.length; i += 2) m.put((String) pares[i], pares[i + 1]);
        return m;
    }
}
