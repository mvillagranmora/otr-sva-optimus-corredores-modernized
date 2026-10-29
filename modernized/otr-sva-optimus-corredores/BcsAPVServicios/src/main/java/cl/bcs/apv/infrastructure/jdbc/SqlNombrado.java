package cl.bcs.apv.infrastructure.jdbc;

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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * JDBC kit of the APV adapters (copy of the proven BcsTesoreriaServicios kit): the three behaviours of the legacy {@code cl.bcs.utils.SQL} they rely on, on top of
 * {@link NamedParameterJdbcTemplate} (so the statements run in the caller's Spring transaction and connection).
 * <ul>
 *   <li>{@link #consultar}: named parameters; a collection parameter expands to its items and an EMPTY collection becomes one NULL item
 *       ({@code IN (NULL)}); rows are mapped by column label onto the public fields of the target class (labels without a field are
 *       ignored) or onto the components of a record; numbers are converted to the declared type (H2 and HANA differ on SUM/COUNT types);</li>
 *   <li>{@link #lote}: one batch execution per element, the parameters read from the element's public fields (or record components) by name;
 *       every {@link BigDecimal} is written with {@code setScale(16, HALF_DOWN)} (legacy {@code SQL.batch}); an empty list with a parameterless
 *       statement runs it once;</li>
 *   <li>{@link #ejecutar}: DDL / parameterless DML as written (legacy {@code SQL.exec}).</li>
 * </ul>
 * Unlike the legacy class an unbound parameter name is an error ({@link InvalidDataAccessApiUsageException}); a service that has to reproduce a
 * legacy "forgotten parameter" defect binds the name to {@code null} explicitly. Failures surface as {@link SQLException} (the type the legacy
 * threw and the goldens pin), not as Spring's {@code DataAccessException}.
 *
 * <p>SQL text lives in {@code src/main/resources/sql/<nombre>.sql} (same text, byte for byte, as the legacy {@code cl/bcs/APV/sql}: the H2 dialect bridge of the tests keys its overrides on that text); comments are
 * stripped before parsing, as the legacy class did.
 */
public final class SqlNombrado {

    private static final Pattern COMENTARIOS = Pattern.compile("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)");
    private static final String BASE = "sql/";

    private final NamedParameterJdbcTemplate jdbc;

    public SqlNombrado(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public NamedParameterJdbcTemplate jdbc() {
        return jdbc;
    }

    /** Loads {@code sql/<nombre>.sql} from the classpath (UTF-8, BOM removed). */
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

    public <T> List<T> consultar(String sql, Map<String, ?> params, Class<T> tipo) throws SQLException {
        MapSqlParameterSource ps = new MapSqlParameterSource();
        params.forEach((k, v) -> ps.addValue(k, normalizar(v)));
        try {
            return jdbc.query(sinComentarios(sql), ps, mapper(tipo));
        } catch (DataAccessException e) {
            throw sqlException(e);
        }
    }

    public <T> List<T> consultar(String sql, Class<T> tipo) throws SQLException {
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
    public <T> void lote(String sql, List<T> filas) throws SQLException {
        String limpio = sinComentarios(sql);
        try {
            if (filas.isEmpty()) {
                if (!tieneParametros(limpio)) {
                    jdbc.update(limpio, new MapSqlParameterSource());
                }
                return;
            }
            SqlParameterSource[] fuentes = filas.stream().map(CamposPublicos::new).toArray(SqlParameterSource[]::new);
            jdbc.batchUpdate(limpio, fuentes);
        } catch (DataAccessException e) {
            throw sqlException(e);
        }
    }

    /** One execution with named parameters; returns the number of affected rows. */
    public int actualizar(String sql, Map<String, ?> params) throws SQLException {
        MapSqlParameterSource ps = new MapSqlParameterSource();
        params.forEach(ps::addValue);
        try {
            return jdbc.update(sinComentarios(sql), ps);
        } catch (DataAccessException e) {
            throw sqlException(e);
        }
    }

    /** DDL / parameterless DML, run as written (legacy {@code SQL.exec}). */
    public void ejecutar(String sql) throws SQLException {
        String s = sql.strip();
        try {
            jdbc.getJdbcOperations().execute(s.endsWith(";") ? s.substring(0, s.length() - 1) : s);
        } catch (DataAccessException e) {
            throw sqlException(e);
        }
    }

    /**
     * The legacy {@code con.commit()} at the points the goldens pin (user decision 1: reproduced): commits the CURRENT connection, the one the
     * statements of this use case run on (the transaction-bound connection of the Spring transaction).
     */
    public void confirmar() throws SQLException {
        try {
            jdbc.getJdbcOperations().execute((org.springframework.jdbc.core.ConnectionCallback<Void>) con -> {
                con.commit();
                return null;
            });
        } catch (DataAccessException e) {
            throw sqlException(e);
        }
    }

    /** The legacy {@code con.rollback()}: see {@link #confirmar()}. */
    public void revertir() throws SQLException {
        try {
            jdbc.getJdbcOperations().execute((org.springframework.jdbc.core.ConnectionCallback<Void>) con -> {
                con.rollback();
                return null;
            });
        } catch (DataAccessException e) {
            throw sqlException(e);
        }
    }

    // ------------------------------------------------------------------------------------------------ internals

    static String sinComentarios(String sql) {
        return COMENTARIOS.matcher(sql).replaceAll("");
    }

    private static SQLException sqlException(DataAccessException e) {
        Throwable causa = e.getMostSpecificCause();
        if (causa instanceof SQLException sql) {
            return sql;
        }
        return new SQLException(e.getMessage(), e);
    }

    private static boolean tieneParametros(String sql) {
        return !NamedParameterUtils.buildSqlParameterList(NamedParameterUtils.parseSqlStatement(sql), new MapSqlParameterSource()).isEmpty();
    }

    /** Parameters read from the public fields of a bean (or the components of a record), by name; BigDecimal at scale 16 (legacy SQL.batch). */
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
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
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
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
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
        if (v instanceof java.sql.Timestamp ts && t == java.sql.Date.class) {
            return new java.sql.Date(ts.getTime());
        }
        if (v instanceof java.sql.Date d && t == java.sql.Timestamp.class) {
            return new java.sql.Timestamp(d.getTime());
        }
        if (t == String.class && !(v instanceof String)) {
            return v.toString();
        }
        return v;
    }
}
