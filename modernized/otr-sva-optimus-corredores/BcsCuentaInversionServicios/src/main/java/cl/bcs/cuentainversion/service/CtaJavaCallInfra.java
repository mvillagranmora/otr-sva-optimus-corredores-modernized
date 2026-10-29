package cl.bcs.cuentainversion.service;

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
 * Shared JDBC utilities for J-type JavaCall migration in BcsCuentaInversionServicios.
 * Replaces legacy cl.bcs.utils.SQL for query/batch/exec operations.
 */
public final class CtaJavaCallInfra {

    private static final Logger log = LoggerFactory.getLogger(CtaJavaCallInfra.class);

    private CtaJavaCallInfra() {}

    // ─── MensajeSalida ───────────────────────────────────────────────────────

    public static final class MensajeSalida {
        public Long   id;
        public String tipo;
        public String codigo;
        public String descripcion;

        public static final String OK          = "OK";
        public static final String ERROR       = "ERROR";
        public static final String ADVERTENCIA = "ADVERTENCIA";
        public static final String INFORMACION = "INFORMACION";
        public static final String AUTORIZACION = "AUTORIZACION";

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

        public static MensajeSalida error(String codigo) {
            return new MensajeSalida(0L, ERROR, codigo, codigo);
        }

        public static MensajeSalida error(String codigo, String descripcion) {
            return new MensajeSalida(0L, ERROR, codigo, descripcion);
        }

        public static long countErrors(List<MensajeSalida> msgs) {
            return msgs == null ? 0L : msgs.stream().filter(m -> ERROR.equals(m.tipo)).count();
        }
    }

    // ─── Query ───────────────────────────────────────────────────────────────

    public static <T> List<T> query(Connection conn, String sql,
                                    Map<String, Object> params,
                                    RowMapper<T> mapper) {
        return jdbc(conn).query(strip(sql), new MapSqlParameterSource(params), mapper);
    }

    // ─── Batch ───────────────────────────────────────────────────────────────

    public static void batch(Connection conn, String sql, List<?> items) {
        if (items.isEmpty()) return;
        SqlParameterSource[] sources = items.stream()
                .map(CtaJavaCallInfra::fieldSource)
                .toArray(SqlParameterSource[]::new);
        jdbc(conn).batchUpdate(strip(sql), sources);
    }

    public static void batch(Connection conn, String sql, SqlParameterSource[] sources) {
        if (sources.length == 0) return;
        jdbc(conn).batchUpdate(strip(sql), sources);
    }

    /** Execute DML that takes no parameters (DELETE, INSERT from table, etc.). */
    public static void exec(Connection conn, String sql) {
        jdbc(conn).update(strip(sql), new MapSqlParameterSource());
    }

    /** Execute DML with named parameters. */
    public static int exec(Connection conn, String sql, Map<String, Object> params) {
        return jdbc(conn).update(strip(sql), new MapSqlParameterSource(params));
    }

    // ─── Numbers ─────────────────────────────────────────────────────────────

    /** Money columns of the schema are DECIMAL(28,16): amounts written back use 16 decimals, HALF_DOWN (legacy SQL.batch). */
    public static java.math.BigDecimal escala16(java.math.BigDecimal valor) {
        return valor == null ? null : valor.setScale(16, java.math.RoundingMode.HALF_DOWN);
    }

    // ─── System parameters ───────────────────────────────────────────────────

    /**
     * Mandatory system parameter. A missing one fails with a plain {@link Exception} carrying the legacy text, which several
     * goldens pin (ConfiguracionSistema.java:70-71).
     */
    public static String parametro(cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort configuracion, Long idEmpresa,
                                   String modulo, String nombre) throws Exception {
        String valor = configuracion.getConfiguracion(idEmpresa, nombre);
        if (valor == null) {
            throw new Exception("Parametro " + nombre + " en modulo " + modulo + " no encontrado");
        }
        return valor;
    }

    // ─── Resource loading ────────────────────────────────────────────────────

    public static String loadSql(String classpathPath) {
        try {
            var res = new ClassPathResource(classpathPath);
            return StreamUtils.copyToString(res.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("SQL file not found: " + classpathPath, e);
        }
    }

    // ─── Row mapper ──────────────────────────────────────────────────────────

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
                    if (val != null) f.set(obj, coerce(val, f.getType()));
                } catch (NoSuchFieldException ignored) {}
            }
            return obj;
        } catch (ReflectiveOperationException e) {
            throw new SQLException("Row mapping failed for " + type.getSimpleName(), e);
        }
    }

    private static Object coerce(Object val, Class<?> target) {
        if (val == null || target.isInstance(val)) return val;
        if (target == Long.class    || target == long.class)    return ((Number) val).longValue();
        if (target == Integer.class || target == int.class)     return ((Number) val).intValue();
        if (target == Double.class  || target == double.class)  return ((Number) val).doubleValue();
        if (target == java.math.BigDecimal.class && val instanceof Number n)
            return new java.math.BigDecimal(n.toString());
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

    static MapSqlParameterSource fieldSource(Object item) {
        var src = new MapSqlParameterSource();
        for (Field f : item.getClass().getFields()) {
            try { src.addValue(f.getName(), f.get(item)); }
            catch (IllegalAccessException ignored) {}
        }
        return src;
    }

    private static String strip(String sql) {
        String s = sql.strip();
        return s.endsWith(";") ? s.substring(0, s.length() - 1) : s;
    }
}
