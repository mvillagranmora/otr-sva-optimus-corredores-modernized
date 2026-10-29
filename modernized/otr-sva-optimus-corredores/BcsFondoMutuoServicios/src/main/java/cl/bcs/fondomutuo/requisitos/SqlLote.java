package cl.bcs.fondomutuo.requisitos;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;

/**
 * Legacy {@code SQL.batch(con, sql, List<T>, Class<T>)} semantics on top of {@link SqlNombrado}: for each bean, every {@code :name} of the SQL is read
 * from the PUBLIC FIELD of that name; a {@code null} is {@code setNull}, a {@code BigDecimal} is rounded to scale 16 HALF_DOWN before binding (legacy
 * rule, it changes what is stored for values with more than 16 decimals), anything else is bound as is. ALL the beans are read first, then ONE prepared statement runs them as a JDBC batch
 * ({@link SqlNombrado#actualizarLote}, chunks of 1000 rows, like the legacy {@code SQL.batch}); the caller owns the transaction). An unknown field fails like the legacy ("Map Object failed").
 */
public final class SqlLote {

    private static final String COMENTARIOS = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)";

    private SqlLote() {
    }

    public static <T> int ejecutar(Connection con, String sql, List<T> filas) throws SQLException {
        Set<String> nombres = nombres(sql);
        List<Map<String, Object>> lote = new ArrayList<>(filas.size());
        for (T fila : filas) {
            Map<String, Object> params = new HashMap<>();
            try {
                for (String n : nombres) {
                    Field campo = fila.getClass().getField(n);
                    Object valor = campo.get(fila);
                    if (valor != null && campo.getType().isAssignableFrom(BigDecimal.class)) {
                        valor = ((BigDecimal) valor).setScale(16, RoundingMode.HALF_DOWN);
                    }
                    params.put(n, valor);
                }
            } catch (ReflectiveOperationException e) {
                throw new SQLException("Map Object failed", e);
            }
            lote.add(params);
        }
        return SqlNombrado.actualizarLote(con, sql, lote);
    }

    /** The {@code :name} parameters of the SQL outside quotes, in order of appearance (the legacy NamedParameterStatement parser). */
    static Set<String> nombres(String sql) {
        String limpio = sql.replaceAll(COMENTARIOS, "");
        Set<String> salida = new LinkedHashSet<>();
        boolean simple = false;
        boolean doble = false;
        for (int i = 0; i < limpio.length(); i++) {
            char c = limpio.charAt(i);
            if (simple) {
                if (c == '\'') simple = false;
            } else if (doble) {
                if (c == '"') doble = false;
            } else if (c == '\'') {
                simple = true;
            } else if (c == '"') {
                doble = true;
            } else if (c == ':' && i + 1 < limpio.length() && Character.isJavaIdentifierStart(limpio.charAt(i + 1))) {
                int j = i + 2;
                while (j < limpio.length() && Character.isJavaIdentifierPart(limpio.charAt(j))) j++;
                salida.add(limpio.substring(i + 1, j));
                i = j - 1;
            }
        }
        return salida;
    }
}
