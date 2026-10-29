package cl.bcs.contabilidad.infrastructure.persistence;

import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Explicit, column-label based row reader used by the hand-written {@link RowMapper}s.
 *
 * <p>Legacy {@code cl.bcs.utils.SQL.query} copied each column into the public field with the same label and ignored
 * absent ones, so a missing column leaves the value {@code null} here too (no exception). Labels match exactly first,
 * then case-insensitively (legacy {@code CASE_SENSITIVE == false} fallback).
 */
public final class SqlRow {

    private final ResultSet rs;
    private final Map<String, Integer> columns;

    private SqlRow(ResultSet rs) throws SQLException {
        this.rs = rs;
        ResultSetMetaData md = rs.getMetaData();
        Map<String, Integer> cols = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 1; i <= md.getColumnCount(); i++) {
            String label = md.getColumnLabel(i);
            if (label != null) cols.putIfAbsent(label, i);
        }
        this.columns = cols;
    }

    /** A {@link RowMapper} that hands each row to {@code f} as a {@link SqlRow}. */
    public static <T> RowMapper<T> mapper(Function<SqlRow, T> f) {
        return (rs, rowNum) -> f.apply(new SqlRow(rs));
    }

    public boolean has(String column) {
        return columns.containsKey(column);
    }

    public Object obj(String column) {
        Integer i = columns.get(column);
        if (i == null) return null;
        try {
            return rs.getObject(i);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read column " + column, e);
        }
    }

    public String str(String column) {
        Object v = obj(column);
        return v == null ? null : v instanceof String s ? s : v.toString();
    }

    public Long lng(String column) {
        Object v = obj(column);
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        String s = v.toString().trim();
        return s.isEmpty() ? null : new BigDecimal(s).longValue();
    }

    public Integer integer(String column) {
        Long l = lng(column);
        return l == null ? null : l.intValue();
    }

    public BigDecimal dec(String column) {
        Object v = obj(column);
        if (v == null) return null;
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Long || v instanceof Integer || v instanceof Short || v instanceof Byte)
            return BigDecimal.valueOf(((Number) v).longValue());
        if (v instanceof Number n) return new BigDecimal(n.toString());
        String s = v.toString().trim();
        return s.isEmpty() ? null : new BigDecimal(s);
    }

    @Override
    public String toString() {
        return "SqlRow" + columns.keySet().toString().toLowerCase(Locale.ROOT);
    }
}
