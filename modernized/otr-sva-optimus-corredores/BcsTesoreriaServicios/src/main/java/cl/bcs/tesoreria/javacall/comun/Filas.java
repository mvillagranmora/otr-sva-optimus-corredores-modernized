package cl.bcs.tesoreria.javacall.comun;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.RowMapper;

/**
 * {@link RowMapper} for the row classes of the JavaCall contract (static classes with public fields named after the SQL column labels, as the legacy
 * {@code cl.bcs.utils.SQL.query} filled them): each column is stored in the public field whose name is the column label, a column without a field is
 * ignored, a NULL stays null. Numbers are converted to the type of the field (the drivers differ on the Java type of SUM(), COUNT() or CASE
 * results), and java.sql.Date / Timestamp are interchangeable.
 */
public final class Filas {

    private static final Map<Class<?>, Map<String, Field>> CAMPOS = new ConcurrentHashMap<>();

    private Filas() {
    }

    public static <T> RowMapper<T> de(Class<T> tipo) {
        Map<String, Field> campos = CAMPOS.computeIfAbsent(tipo, Filas::camposPublicos);
        return (ResultSet rs, int fila) -> {
            T objeto = nuevo(tipo);
            ResultSetMetaData md = rs.getMetaData();
            for (int c = 1; c <= md.getColumnCount(); c++) {
                Field campo = campos.get(md.getColumnLabel(c));
                if (campo != null) {
                    try {
                        campo.set(objeto, convertir(rs.getObject(c), campo.getType()));
                    } catch (IllegalAccessException e) {
                        throw new SQLException("cannot set " + tipo.getSimpleName() + "." + campo.getName(), e);
                    }
                }
            }
            return objeto;
        };
    }

    private static Map<String, Field> camposPublicos(Class<?> tipo) {
        Map<String, Field> campos = new ConcurrentHashMap<>();
        for (Field f : tipo.getFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                campos.put(f.getName(), f);
            }
        }
        return campos;
    }

    private static <T> T nuevo(Class<T> tipo) throws SQLException {
        try {
            return tipo.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new SQLException("cannot instantiate " + tipo.getName(), e);
        }
    }

    private static Object convertir(Object v, Class<?> t) {
        if (v == null) {
            return null;
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
                return n.toString();
            }
        }
        if (v instanceof Timestamp ts && t == java.sql.Date.class) {
            return new java.sql.Date(ts.getTime());
        }
        if (v instanceof java.sql.Date d && t == Timestamp.class) {
            return new Timestamp(d.getTime());
        }
        if ((v instanceof java.sql.Date || v instanceof Timestamp) && t == String.class) {
            return v.toString();
        }
        return v;
    }
}
