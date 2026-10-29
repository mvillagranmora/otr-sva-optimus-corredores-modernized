package cl.bcs.cuentainversion.reportes;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * JDBC helper of the reporting family (CTA-B). It reproduces the three behaviours of the legacy {@code cl.bcs.utils.SQL} that the
 * reporting services rely on, on top of Spring's {@link NamedParameterJdbcTemplate}:
 * <ul>
 *   <li>{@link #consultar}: named parameters, a collection parameter expands to its items and an EMPTY collection becomes a single
 *       NULL item ({@code IN (NULL)}, so the predicate never matches), the result rows are mapped by column label onto the public
 *       fields of the target class (labels without a field are ignored) or onto the components of a record;</li>
 *   <li>{@link #lote}: one batch execution per element, the parameters are read from the element's public fields by name and every
 *       {@link BigDecimal} is written with {@code setScale(16, HALF_DOWN)} (the accepted DECIMAL(28,16) assumption);</li>
 *   <li>{@link #ejecutar}: DDL/DML text run as is (no parameters).</li>
 * </ul>
 * Unlike the legacy class an unbound parameter name is an error ({@link InvalidDataAccessApiUsageException}); a service that must
 * reproduce a legacy "forgotten parameter" defect binds the name to {@code null} explicitly.
 *
 * <p>The SQL text lives in {@code resources/cl/bcs/cuentainversion/sql/<name>.sql}; comments are stripped before parsing, as the
 * legacy class did.
 */
public final class SqlReportes {

    private static final Pattern COMENTARIOS = Pattern.compile("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)");
    private static final String BASE = "cl/bcs/cuentainversion/sql/";

    private final NamedParameterJdbcTemplate jdbc;

    public SqlReportes(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SqlReportes(DataSource dataSource) {
        this(new NamedParameterJdbcTemplate(dataSource));
    }

    public NamedParameterJdbcTemplate jdbc() {
        return jdbc;
    }

    /** Loads {@code cl/bcs/cuentainversion/sql/<nombre>.sql} (UTF-8, BOM removed). */
    public static String recurso(String nombre) {
        String ruta = BASE + nombre + ".sql";
        try (InputStream in = new ClassPathResource(ruta).getInputStream()) {
            String s = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return s.startsWith("﻿") ? s.substring(1) : s;
        } catch (IOException e) {
            throw new IllegalStateException("SQL resource not found: " + ruta, e);
        }
    }

    // ------------------------------------------------------------------------------------------------ query

    public <T> List<T> consultar(String sql, Map<String, ?> params, Class<T> tipo) {
        MapSqlParameterSource ps = new MapSqlParameterSource();
        params.forEach((k, v) -> ps.addValue(k, normalizar(v)));
        return jdbc.query(sinComentarios(sql), ps, mapper(tipo));
    }

    public <T> List<T> consultar(String sql, Class<T> tipo) {
        return consultar(sql, Map.of(), tipo);
    }

    /** An empty collection is bound as a single NULL item (legacy SQL.query). */
    private static Object normalizar(Object v) {
        if (v instanceof Collection<?> c && c.isEmpty()) {
            return Collections.singletonList(null);
        }
        return v;
    }

    // ------------------------------------------------------------------------------------------------ DML

    /** Runs the statement once per element; the parameters are the element's public fields (see class comment). */
    public <T> void lote(String sql, List<T> filas) {
        String limpio = sinComentarios(sql);
        if (filas.isEmpty()) {
            if (!tieneParametros(limpio)) {
                jdbc.update(limpio, new MapSqlParameterSource());
            }
            return;
        }
        SqlParameterSource[] fuentes = filas.stream().map(CamposPublicos::new).toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate(limpio, fuentes);
    }

    /** One execution with named parameters; returns the number of affected rows. */
    public int actualizar(String sql, Map<String, ?> params) {
        MapSqlParameterSource ps = new MapSqlParameterSource();
        params.forEach(ps::addValue);
        return jdbc.update(sinComentarios(sql), ps);
    }

    /** DDL / parameterless DML, run as written (legacy {@code SQL.exec}). */
    public void ejecutar(String sql) {
        String s = sql.strip();
        jdbc.getJdbcOperations().execute(s.endsWith(";") ? s.substring(0, s.length() - 1) : s);
    }

    // ------------------------------------------------------------------------------------------------ sequences

    /**
     * {@code cantidad} values of a HANA sequence (legacy {@code Utils.getSecuencia}): an empty list when {@code cantidad} is null
     * or not positive. HANA gets the legacy {@code SERIES_GENERATE_BIGINT} query; H2 (characterization tests) cannot run it and
     * draws the values one by one.
     */
    public List<Long> secuencia(String nombre, Long cantidad) {
        List<Long> salida = new ArrayList<>();
        if (cantidad == null || cantidad <= 0) {
            return salida;
        }
        String producto = jdbc.getJdbcOperations().execute((ConnectionCallback<String>) c -> c.getMetaData().getDatabaseProductName());
        if (producto != null && producto.toUpperCase().contains("H2")) {
            for (long i = 0; i < cantidad; i++) {
                salida.add(jdbc.getJdbcOperations().queryForObject("SELECT \"" + nombre + "\".NEXTVAL", Long.class));
            }
            return salida;
        }
        String q = "SELECT \"" + nombre + "\".NEXTVAL AS \"Id\" FROM SERIES_GENERATE_BIGINT(1, 0, :cantidad)";
        jdbc.query(q, new MapSqlParameterSource("cantidad", cantidad), (rs, n) -> rs.getLong("Id")).forEach(salida::add);
        return salida;
    }

    // ------------------------------------------------------------------------------------------------ internals

    static String sinComentarios(String sql) {
        return COMENTARIOS.matcher(sql).replaceAll("");
    }

    private static boolean tieneParametros(String sql) {
        return !NamedParameterUtils.buildSqlParameterList(NamedParameterUtils.parseSqlStatement(sql), new MapSqlParameterSource()).isEmpty();
    }

    /** Parameters read from the public fields of a bean, by name; BigDecimal at scale 16 (legacy SQL.batch). */
    private static final class CamposPublicos extends AbstractSqlParameterSource {
        private final Object fila;

        CamposPublicos(Object fila) {
            this.fila = fila;
        }

        @Override
        public boolean hasValue(String nombre) {
            if (fila.getClass().isRecord()) {
                return Arrays.stream(fila.getClass().getRecordComponents()).anyMatch(c -> c.getName().equals(nombre));
            }
            try {
                fila.getClass().getField(nombre);
                return true;
            } catch (NoSuchFieldException e) {
                return false;
            }
        }

        @Override
        public Object getValue(String nombre) {
            try {
                Object v;
                if (fila.getClass().isRecord()) {
                    RecordComponent c = Arrays.stream(fila.getClass().getRecordComponents()).filter(x -> x.getName().equals(nombre)).findFirst()
                            .orElseThrow(() -> new InvalidDataAccessApiUsageException("Object field unknown: " + nombre));
                    v = c.getAccessor().invoke(fila);
                } else {
                    v = fila.getClass().getField(nombre).get(fila);
                }
                return v instanceof BigDecimal bd ? bd.setScale(16, RoundingMode.HALF_DOWN) : v;
            } catch (ReflectiveOperationException e) {
                throw new InvalidDataAccessApiUsageException("Object field unknown: " + nombre, e);
            }
        }
    }

    public static <T> RowMapper<T> mapper(Class<T> tipo) {
        return tipo.isRecord() ? new RecordMapper<>(tipo) : new CamposMapper<>(tipo);
    }

    private static final class CamposMapper<T> implements RowMapper<T> {
        private final Class<T> tipo;

        CamposMapper(Class<T> tipo) {
            this.tipo = tipo;
        }

        @Override
        public T mapRow(java.sql.ResultSet rs, int rowNum) throws SQLException {
            try {
                T obj = tipo.getDeclaredConstructor().newInstance();
                ResultSetMetaData md = rs.getMetaData();
                for (int c = 1; c <= md.getColumnCount(); c++) {
                    Field f = campo(tipo, md.getColumnLabel(c));
                    if (f != null) {
                        f.set(obj, convertir(rs.getObject(c), f.getType()));
                    }
                }
                return obj;
            } catch (ReflectiveOperationException e) {
                throw new SQLException(e.getMessage(), e);
            }
        }
    }

    private static final class RecordMapper<T> implements RowMapper<T> {
        private final Class<T> tipo;
        private final RecordComponent[] componentes;
        private final Constructor<T> constructor;

        RecordMapper(Class<T> tipo) {
            this.tipo = tipo;
            this.componentes = tipo.getRecordComponents();
            Class<?>[] tipos = new Class<?>[componentes.length];
            for (int i = 0; i < tipos.length; i++) {
                tipos[i] = componentes[i].getType();
            }
            try {
                this.constructor = tipo.getDeclaredConstructor(tipos);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public T mapRow(java.sql.ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> valores = new LinkedHashMap<>();
            ResultSetMetaData md = rs.getMetaData();
            for (int c = 1; c <= md.getColumnCount(); c++) {
                valores.put(md.getColumnLabel(c), rs.getObject(c));
            }
            Object[] args = new Object[componentes.length];
            for (int i = 0; i < args.length; i++) {
                Object v = convertir(valores.get(componentes[i].getName()), componentes[i].getType());
                args[i] = v == null && componentes[i].getType().isPrimitive() ? valorPorDefecto(componentes[i].getType()) : v;
            }
            try {
                return constructor.newInstance(args);
            } catch (ReflectiveOperationException e) {
                throw new SQLException("cannot map row onto " + tipo.getSimpleName(), e);
            }
        }
    }

    private static Field campo(Class<?> tipo, String etiqueta) {
        try {
            Field f = tipo.getField(etiqueta);
            return Modifier.isStatic(f.getModifiers()) ? null : f;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static Object valorPorDefecto(Class<?> primitivo) {
        if (primitivo == boolean.class) {
            return false;
        }
        if (primitivo == long.class) {
            return 0L;
        }
        if (primitivo == int.class) {
            return 0;
        }
        if (primitivo == double.class) {
            return 0d;
        }
        return null;
    }

    /** H2 and HANA differ on result types (a SUM may come back as BigDecimal for a Long field): convert to the declared field type. */
    static Object convertir(Object v, Class<?> t) throws SQLException {
        if (v == null) {
            return null;
        }
        if (v instanceof Clob c) {
            return c.length() == 0 ? "" : c.getSubString(1, (int) c.length());
        }
        if (v instanceof Blob b) {
            return b.length() == 0 ? new byte[0] : b.getBytes(1, (int) b.length());
        }
        if (v instanceof Number n) {
            if (t == Long.class || t == long.class) {
                return n.longValue();
            }
            if (t == Integer.class || t == int.class) {
                return n.intValue();
            }
            if (t == Short.class || t == short.class) {
                return n.shortValue();
            }
            if (t == Double.class || t == double.class) {
                return n.doubleValue();
            }
            if (t == BigDecimal.class) {
                return v instanceof BigDecimal ? v : new BigDecimal(n.toString());
            }
            if (t == String.class) {
                return v.toString();
            }
        }
        if (t == java.sql.Date.class && v instanceof java.util.Date d && !(v instanceof java.sql.Date)) {
            return new java.sql.Date(d.getTime());
        }
        if (t == String.class && !(v instanceof String)) {
            return v.toString();
        }
        return v;
    }
}
