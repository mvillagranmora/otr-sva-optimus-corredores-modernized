package cl.bcs.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;

/**
 * ORACLE SHIM of legacy cl.bcs.utils.SQL (BcsUtils/src/cl/bcs/utils/SQL.java).
 * Reproduces the semantics the three services depend on:
 *  - comment stripping regex (SQL.java:501 / :556)
 *  - ArrayList expansion; an empty list becomes a single NULL parameter, i.e. IN (NULL) (SQL.java:505-523)
 *  - result mapping by column label onto public fields (SQL.java:213-240, lenient HANA mapper)
 *  - batch: fields by parameter name, BigDecimal.setScale(16, HALF_DOWN), null -> setNull (SQL.java:580-596)
 * Numbers are converted to the field type because H2 and HANA differ on SUM()/CASE result types.
 */
public class SQL {
    private static final String COMMENTS = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)";

    public <T> List<T> query(Connection conn, String sql, HashMap<String, Object> params, Class<T> type) throws SQLException {
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
                        Field f = field(type, md.getColumnLabel(c));
                        if (f != null) f.set(obj, convert(rs.getObject(c), f.getType()));
                    }
                    result.add(obj);
                }
            }
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        }
        return result;
    }

    public <T> int[] batch(Connection conn, String sql, List<T> params, Class<T> type) throws SQLException {
        String s = sql.replaceAll(COMMENTS, "");
        List<String> names = new ArrayList<>();
        String jdbcSql = parse(s, names);
        try (PreparedStatement ps = conn.prepareStatement(jdbcSql)) {
            for (T p : params) {
                for (int k = 0; k < names.size(); k++) {
                    Field f = p.getClass().getField(names.get(k));
                    Object v = f.get(p);
                    if (v == null) {
                        ps.setNull(k + 1, Types.NULL);
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
            throw e;
        } catch (Exception e) {
            throw new SQLException("Map Object failed", e);
        }
    }

    private static Field field(Class<?> t, String col) {
        for (String n : new String[]{col, col.toLowerCase(), col.toUpperCase()}) {
            try {
                return t.getField(n);
            } catch (NoSuchFieldException ignored) {
                // try next spelling
            }
        }
        return null;
    }

    private static Object convert(Object v, Class<?> t) {
        if (v == null) return null;
        if (v instanceof Number n) {
            if (t == Long.class) return n.longValue();
            if (t == Integer.class) return n.intValue();
            if (t == Short.class) return n.shortValue();
            if (t == BigDecimal.class) return new BigDecimal(n.toString());
            if (t == String.class) return n.toString();
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
