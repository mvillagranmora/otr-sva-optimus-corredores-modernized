package cl.bcs.utils;

import cl.bcs.cuentainversion.reportes.support.H2Dialect;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;

/**
 * ORACLE SHIM of legacy cl.bcs.utils.SQL (legacy/.../Plataforma/JAVA/BcsUtils/src/cl/bcs/utils/SQL.java).
 * Reproduces the semantics the reporting classes depend on and nothing else:
 *  - comment stripping regex (SQL.java:498, :556)
 *  - ArrayList expansion; an empty list becomes a single NULL parameter, i.e. IN (NULL) (SQL.java:499-523)
 *  - null parameter -> setNull, others setObject (SQL.java:200-203)
 *  - result mapping by exact column label onto public fields; unmapped columns ignored (SQL.java:225-245)
 *  - batch: parameters read from public fields by name, BigDecimal.setScale(16, HALF_DOWN), null -> setNull,
 *    empty parameter list and no parameters -> one execution (SQL.java:559-596)
 *  - static exec(conn, sql) runs a statement as is (SQL.java:623-631)
 *  - errors are wrapped with the query text and parameters (SQL.java:794-814 rethrow)
 * Deviations (test infrastructure only): the SQL is passed through H2Dialect (HANA UPSERT, #temp tables), and
 * a NULL parameter is bound as a VARCHAR null (H2 cannot type an untyped NULL in SELECT ? AS col, HANA can; same rule in
 * CasoReporte for the modern side), and numeric results are converted to the declared field type because H2 and HANA differ on result types (a legacy
 * field declared Long receives a Long in HANA; H2 may answer BigDecimal for SUM/CASE).
 */
public class SQL {
    private static final String COMMENTS = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)";

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
        String jdbcSql = parse(H2Dialect.translate(s), names);
        List<T> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(jdbcSql)) {
            for (int i = 0; i < names.size(); i++) {
                Object v = paramsT.get(names.get(i));
                if (v == null) ps.setNull(i + 1, Types.VARCHAR); else ps.setObject(i + 1, v);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                while (rs.next()) {
                    T obj = type.getDeclaredConstructor().newInstance();
                    result.add(obj);
                    for (int c = 1; c <= md.getColumnCount(); c++) {
                        Field f = null;
                        try { f = obj.getClass().getField(md.getColumnLabel(c)); } catch (Exception ignored) { /* unmapped */ }
                        if (f != null) f.set(obj, convert(rs.getObject(c), f.getType()));
                    }
                }
            }
        } catch (SQLException e) {
            rethrow(e, sql, params);
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
        String jdbcSql = parse(H2Dialect.translate(s), names);
        try (PreparedStatement ps = conn.prepareStatement(jdbcSql)) {
            for (T p : params) {
                for (int k = 0; k < names.size(); k++) {
                    Object v;
                    try {
                        Field f = p.getClass().getField(names.get(k));
                        v = f.get(p);
                    } catch (Exception er) {
                        throw new SQLException("Map Object failed", er);
                    }
                    if (v == null) {
                        ps.setNull(k + 1, Types.VARCHAR);
                    } else {
                        if (v instanceof BigDecimal bd) v = bd.setScale(16, RoundingMode.HALF_DOWN);
                        ps.setObject(k + 1, v);
                    }
                }
                ps.addBatch();
            }
            if (names.isEmpty() && params.isEmpty()) ps.addBatch();
            return ps.executeBatch();
        } catch (SQLException e) {
            rethrow(e, sql, params.toArray());
            return null;
        }
    }

    /** Legacy SQL.java:623-631. */
    public static boolean exec(Connection conn, String sql) throws SQLException {
        if (conn == null) throw new SQLException("Null connection");
        if (sql == null) throw new SQLException("Null SQL statement");
        try (Statement st = conn.createStatement()) {
            return st.execute(H2Dialect.translate(sql));
        }
    }

    protected void rethrow(SQLException cause, String sql, Object... params) throws SQLException {
        String causeMessage = cause.getMessage();
        if (cause.getCause() != null) causeMessage = causeMessage + " " + cause.getCause().getMessage();
        StringBuilder msg = new StringBuilder(causeMessage);
        msg.append(" Query: ").append(sql).append(" Parameters: ");
        msg.append(params == null ? "[]" : Arrays.deepToString(params));
        SQLException e = new SQLException(msg.toString(), cause.getSQLState(), cause.getErrorCode());
        e.setNextException(cause);
        throw e;
    }

    private static Object convert(Object v, Class<?> t) {
        if (v == null) return null;
        if (v instanceof Number n) {
            if (t == Long.class) return n.longValue();
            if (t == Integer.class) return n.intValue();
            if (t == Short.class) return n.shortValue();
            if (t == Double.class) return n.doubleValue();
            if (t == BigDecimal.class) return new BigDecimal(n.toString());
            if (t == String.class) return n.toString();
        }
        if (t == java.sql.Date.class && v instanceof java.util.Date d && !(v instanceof java.sql.Date)) {
            return new java.sql.Date(d.getTime());
        }
        if (t == String.class && !(v instanceof String)) {
            return v.toString();
        }
        return v;
    }

    /** Replaces :name with ?, skipping quoted sections and '::'. */
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
