package cl.bcs.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ORACLE SHIM of legacy cl.bcs.utils.SQL (legacy/.../BcsUtils/src/cl/bcs/utils/SQL.java). Reproduces the semantics the
 * CuentaInversion services depend on:
 * <ul>
 *   <li>comment-stripping regex: SQL.java:503 (query) and :555 (batch);</li>
 *   <li>ArrayList parameter expansion; an empty list becomes ONE NULL parameter, i.e. {@code IN (NULL)}: SQL.java:504-526;</li>
 *   <li>named parameters ({@code :name}) resolved to JDBC {@code ?} (legacy NamedParameterStatement), nulls via setNull: SQL.java:193-196;</li>
 *   <li>result mapping by column label onto PUBLIC FIELDS of the target class, lenient HANA mapper: SQL.java:213-246, 275-289
 *       (here numbers are converted to the field type because H2 and HANA differ on the Java type of SUM()/RANK()/CASE results);</li>
 *   <li>batch: parameters read from public fields by parameter name, {@code BigDecimal} fields get {@code setScale(16, HALF_DOWN)},
 *       null -> setNull; an empty parameter list with no named parameters executes the statement once: SQL.java:565-596;</li>
 *   <li>{@code exec}: plain Statement.execute: SQL.java:623-630.</li>
 * </ul>
 * {@code call} (HANA stored procedures) is NOT reproducible on H2: it is recorded as a collaborator call (see the method) and returns no outputs.
 */
public class SQL {
    private static final String COMMENTS = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)";

    public SQL() {
    }

    public static class Count {
        public Long Contador;

        public Count() {
            this.Contador = -1L;
        }
    }

    public <T> List<T> query(Connection conn, String sql, HashMap<String, Object> params, Class<T> type) throws SQLException {
        if (conn == null) throw new SQLException("Null connection");
        if (sql == null) throw new SQLException("Null SQL statement");
        if (params == null) throw new SQLException("Null Class Params. If parameters aren't need, pass an empty");
        if (type == null) throw new SQLException("Null Class Type");
        HashMap<String, Object> paramsT = new HashMap<>();
        String s = sql.replaceAll(COMMENTS, "");
        for (Map.Entry<String, Object> e : params.entrySet()) {
            if (e.getValue() instanceof ArrayList<?> list) {
                int items = 0;
                StringBuilder ni = new StringBuilder();
                if (list.isEmpty()) {
                    String k = e.getKey() + "_" + items++;
                    ni.append(':').append(k);
                    paramsT.put(k, null);
                } else {
                    for (Object o : list) {
                        String k = e.getKey() + "_" + items++;
                        if (ni.length() > 0) ni.append(',');
                        ni.append(':').append(k);
                        paramsT.put(k, o);
                    }
                }
                s = s.replace(":" + e.getKey(), ni.toString());
            } else {
                paramsT.put(e.getKey(), e.getValue());
            }
        }
        List<String> names = new ArrayList<>();
        String jdbcSql = parse(s, names);
        List<T> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(jdbcSql)) {
            for (int i = 0; i < names.size(); i++) {
                Object v = paramsT.get(names.get(i));
                if (v == null) ps.setNull(i + 1, Types.NULL); else ps.setObject(i + 1, v);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                while (rs.next()) {
                    T obj = type.getDeclaredConstructor().newInstance();
                    for (int c = 1; c <= md.getColumnCount(); c++) {
                        Field f = null;
                        try {
                            f = obj.getClass().getField(md.getColumnLabel(c));
                        } catch (Exception er) {
                            f = null;
                        }
                        if (f != null) f.set(obj, convert(rs.getObject(c), f.getType()));
                    }
                    result.add(obj);
                }
            }
        } catch (SQLException e) {
            throw rethrow(e, sql);
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        }
        return result;
    }

    public <T> int[] batch(Connection conn, String sql, List<T> params, Class<T> type) throws SQLException {
        if (conn == null) throw new SQLException("Null connection");
        if (sql == null) throw new SQLException("Null SQL statement");
        if (params == null) throw new SQLException("Null parameters. If parameters aren't need, pass an empty");
        if (type == null) throw new SQLException("Null Class Type.");
        String s = sql.replaceAll(COMMENTS, "");
        List<String> names = new ArrayList<>();
        String jdbcSql = parse(s, names);
        try (PreparedStatement ps = conn.prepareStatement(jdbcSql)) {
            for (T p : params) {
                try {
                    T pObj = type.cast(p);
                    for (int k = 0; k < names.size(); k++) {
                        Field field = pObj.getClass().getField(names.get(k));
                        Object v = field.get(pObj);
                        if (v == null) {
                            ps.setNull(k + 1, Types.NULL);
                        } else {
                            if (field.getType().isAssignableFrom(BigDecimal.class)) {
                                v = ((BigDecimal) v).setScale(16, RoundingMode.HALF_DOWN);
                            }
                            ps.setObject(k + 1, v);
                        }
                    }
                } catch (Exception er) {
                    throw new SQLException("Map Object failed", er);
                }
                ps.addBatch();
            }
            if (names.isEmpty() && params.isEmpty()) ps.addBatch();
            return ps.executeBatch();
        } catch (SQLException e) {
            throw rethrow(e, sql);
        }
    }

    public final static boolean exec(Connection conn, String sql) throws SQLException {
        if (conn == null) throw new SQLException("Null connection");
        if (sql == null) throw new SQLException("Null SQL statement");
        try (Statement st = conn.createStatement()) {
            return st.execute(sql);
        }
    }

    @SuppressWarnings("rawtypes")
    public HashMap<String, Object> call(Connection conn, String sql, HashMap<String, Object> paramsIn, HashMap<String, Class> paramsOut) throws SQLException {
        // The HANA stored procedure itself is NOT reproducible on H2. What IS characterised is the Java side: the call (procedure name + scalar
        // inputs) is recorded as a collaborator call ("StoredProcedure") and the procedure's effects are whatever the case seed left in its
        // output tables; the output scalars are returned empty. A case that needs more must not use this path.
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?i)call\\s+\"([^\"]+)\"").matcher(sql);
        java.util.Map<String, Object> reg = new java.util.LinkedHashMap<>();
        reg.put("procedimiento", m.find() ? m.group(1) : sql);
        reg.put("entrada", new java.util.TreeMap<>(paramsIn));
        cl.bcs.oracle.Hooks.registrar("StoredProcedure", reg);
        HashMap<String, Object> salida = new HashMap<String, Object>();
        // Scripted OUTPUT parameters of the procedure (case.json "colaboradores": {"SalidaProcedimiento": {"<procedure name>": {"<param>": value | [rows]}}}).
        // They stand in for what HANA would return; the procedure itself is outside the proof.
        com.google.gson.JsonObject cols = cl.bcs.oracle.Hooks.colaboradores;
        if (cols != null && cols.has("SalidaProcedimiento") && reg.get("procedimiento") != null) {
            com.google.gson.JsonObject todas = cols.getAsJsonObject("SalidaProcedimiento");
            String nombre = (String) reg.get("procedimiento");
            if (todas.has(nombre)) {
                com.google.gson.Gson g = Utils.getGsonBuilder();
                com.google.gson.JsonObject guion = todas.getAsJsonObject(nombre);
                for (java.util.Map.Entry<String, Class> p : paramsOut.entrySet()) {
                    if (!guion.has(p.getKey())) continue;
                    com.google.gson.JsonElement v = guion.get(p.getKey());
                    if (v.isJsonArray()) {
                        List<Object> filas = new java.util.ArrayList<>();
                        for (com.google.gson.JsonElement f : v.getAsJsonArray()) filas.add(g.fromJson(f, p.getValue()));
                        salida.put(p.getKey(), filas);
                    } else {
                        salida.put(p.getKey(), g.fromJson(v, p.getValue()));
                    }
                }
            }
        }
        return salida;
    }

    private static SQLException rethrow(SQLException cause, String sql) {
        SQLException e = new SQLException(cause.getMessage() + " Query: " + sql, cause.getSQLState(), cause.getErrorCode());
        e.setNextException(cause);
        return e;
    }

    private static Object convert(Object v, Class<?> t) {
        if (v == null) return null;
        if (v instanceof Number n) {
            if (t == Long.class || t == long.class) return n.longValue();
            if (t == Integer.class || t == int.class) return n.intValue();
            if (t == Short.class) return n.shortValue();
            if (t == Double.class || t == double.class) return n.doubleValue();
            if (t == BigDecimal.class) return new BigDecimal(n.toString());
            if (t == String.class) return n.toString();
        }
        if (v instanceof java.sql.Timestamp ts && t == java.sql.Date.class) return new java.sql.Date(ts.getTime());
        if (v instanceof java.sql.Date d && t == java.sql.Timestamp.class) return new java.sql.Timestamp(d.getTime());
        if (v instanceof java.sql.Date d && t == String.class) return d.toString();
        if (v instanceof java.sql.Timestamp ts && t == String.class) return ts.toString();
        return v;
    }

    /** Replaces :name with ?, skipping quoted sections and '::' (legacy NamedParameterStatement). */
    static String parse(String sql, List<String> names) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < sql.length()) {
            char ch = sql.charAt(i);
            if (ch == '\'' || ch == '"') {
                int j = sql.indexOf(ch, i + 1);
                if (j < 0) j = sql.length() - 1;
                out.append(sql, i, j + 1);
                i = j + 1;
            } else if (ch == ':' && i + 1 < sql.length() && sql.charAt(i + 1) == ':') {
                out.append("::");
                i += 2;
            } else if (ch == ':' && i + 1 < sql.length() && Character.isJavaIdentifierStart(sql.charAt(i + 1))) {
                int j = i + 1;
                while (j < sql.length() && Character.isJavaIdentifierPart(sql.charAt(j))) j++;
                names.add(sql.substring(i + 1, j));
                out.append('?');
                i = j;
            } else {
                out.append(ch);
                i++;
            }
        }
        return out.toString();
    }
}
