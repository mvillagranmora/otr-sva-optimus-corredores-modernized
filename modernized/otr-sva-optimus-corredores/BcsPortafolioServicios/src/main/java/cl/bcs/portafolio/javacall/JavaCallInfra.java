package cl.bcs.portafolio.javacall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Shared utilities for J-type JavaCall migration.
 * Replaces cl.bcs.utils.SQL for query/batch operations.
 */
public final class JavaCallInfra {

    private static final Logger log = LoggerFactory.getLogger(JavaCallInfra.class);

    private JavaCallInfra() {}

    // ─── MensajeSalida ───────────────────────────────────────────────────────

    public static final class MensajeSalida {
        public Long id;
        public String tipo;
        public String codigo;
        public String descripcion;

        public static final String OK          = "OK";
        public static final String ERROR       = "ERROR";
        public static final String ADVERTENCIA = "ADVERTENCIA";
        public static final String INFORMACION = "INFORMACION";

        public MensajeSalida(Long id, String tipo, String codigo, String descripcion) {
            this.id          = id;
            this.tipo        = tipo;
            this.codigo      = codigo;
            this.descripcion = descripcion;
        }

        public static MensajeSalida ok(String codigo) {
            return new MensajeSalida(0L, OK, codigo, codigo);
        }

        public static MensajeSalida ok(String codigo, String descripcion) {
            return new MensajeSalida(0L, OK, codigo, descripcion);
        }

        public static MensajeSalida error(long id, String codigo, String descripcion) {
            return new MensajeSalida(id, ERROR, codigo, descripcion);
        }

        public static MensajeSalida error(String codigo, String descripcion) {
            return new MensajeSalida(0L, ERROR, codigo, descripcion);
        }

        public static MensajeSalida error(String codigo) {
            return new MensajeSalida(0L, ERROR, codigo, codigo);
        }

        public static long countErrors(List<MensajeSalida> mensajes) {
            return mensajes == null ? 0L : mensajes.stream()
                    .filter(m -> ERROR.equals(m.tipo)).count();
        }
    }

    // ─── Query ───────────────────────────────────────────────────────────────

    public static <T> List<T> query(Connection conn, String sql,
                                    Map<String, Object> params,
                                    RowMapper<T> mapper) {
        var jdbc = jdbc(conn);
        var src  = new MapSqlParameterSource(params);
        return jdbc.query(stripSemicolon(sql), src, mapper);
    }

    // ─── Batch ───────────────────────────────────────────────────────────────

    public static void batch(Connection conn, String sql, List<?> items) {
        if (items.isEmpty()) return;
        var jdbc = jdbc(conn);
        SqlParameterSource[] sources = items.stream()
                .map(JavaCallInfra::fieldSource)
                .toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate(stripSemicolon(sql), sources);
    }

    public static void batch(Connection conn, String sql, SqlParameterSource[] sources) {
        if (sources.length == 0) return;
        jdbc(conn).batchUpdate(stripSemicolon(sql), sources);
    }

    // ─── Resource loading ────────────────────────────────────────────────────

    public static String loadSql(String classpathPath) {
        try {
            var resource = new ClassPathResource(classpathPath);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("SQL file not found on classpath: " + classpathPath, e);
        }
    }

    // ─── PublicFieldRowMapper ─────────────────────────────────────────────────

    public static <T> RowMapper<T> fieldMapper(Class<T> type) {
        return (rs, rowNum) -> mapFields(rs, type);
    }

    private static <T> T mapFields(ResultSet rs, Class<T> type) throws SQLException {
        try {
            T obj = type.getDeclaredConstructor().newInstance();
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                String label = meta.getColumnLabel(i);
                try {
                    Field f = type.getField(label);
                    Object val = rs.getObject(i);
                    if (val != null) {
                        f.set(obj, coerce(val, f.getType()));
                    }
                } catch (NoSuchFieldException ignored) {
                    // column not mapped to a field — skip
                }
            }
            return obj;
        } catch (ReflectiveOperationException e) {
            throw new SQLException("Row mapping failed for " + type.getSimpleName(), e);
        }
    }

    private static Object coerce(Object val, Class<?> target) {
        if (val == null || target.isInstance(val)) return val;
        if (target == Long.class   || target == long.class)   return ((Number) val).longValue();
        if (target == Integer.class || target == int.class)   return ((Number) val).intValue();
        if (target == Double.class  || target == double.class) return ((Number) val).doubleValue();
        if (target == Boolean.class || target == boolean.class) {
            if (val instanceof Boolean b) return b;
            String s = val.toString();
            return "1".equals(s) || "true".equalsIgnoreCase(s) || "S".equalsIgnoreCase(s);
        }
        if (target == String.class) return val.toString();
        return val;
    }

    // ─── Internals ───────────────────────────────────────────────────────────

    private static NamedParameterJdbcTemplate jdbc(Connection conn) {
        return new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
    }

    private static MapSqlParameterSource fieldSource(Object item) {
        var src = new MapSqlParameterSource();
        for (Field f : item.getClass().getFields()) {
            try {
                src.addValue(f.getName(), f.get(item));
            } catch (IllegalAccessException ignored) {}
        }
        return src;
    }

    private static String stripSemicolon(String sql) {
        String s = sql.strip();
        return s.endsWith(";") ? s.substring(0, s.length() - 1) : s;
    }
}
