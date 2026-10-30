package cl.bcs.canalagente.adapter.out.jdbc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Named-parameter SQL executor with the semantics of legacy {@code cl.bcs.utils.SQL} (query, batch, exec) + {@code NamedParameterStatement}, written to run the legacy
 * SQL text unchanged on the connection the caller owns (no commit, no rollback, no close):
 * <ul>
 *   <li>comments are removed with the legacy regular expression; {@code :name} is recognised outside single and double quotes (the {@code "SCHEMA::TABLE"} of HANA objects
 *       is safe); a parameter in the map that is not in the SQL fails ({@code Parameter not found}); a {@code null} value is {@code setNull(VARCHAR)};</li>
 *   <li>rows are mapped to the PUBLIC FIELDS of the pojo by exact column label (case sensitive, no conversion: a type mismatch fails as the legacy {@code Field.set} did;
 *       columns without a field are ignored); a Clob becomes a String and a Blob a byte array;</li>
 *   <li>a failure of the database is rethrown as {@link SQLException} with {@code " Query: ... Parameters: [...]"} appended, as the legacy {@code rethrow};</li>
 *   <li>the trailing {@code ;} of the SQL files is sent as is (the legacy did).</li>
 * </ul>
 * Not ported: list parameters (no SQL of this module uses them) and the PostgreSQL dialect translation (HANA only).
 */
public final class SqlNombrado {

    private static final String COMENTARIOS = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?:/" + "/.*)|(?:--.*)";

    private SqlNombrado() {
    }

    private record Preparada(String sql, Map<String, int[]> indices) {
        int[] de(String nombre) {
            int[] idx = indices.get(nombre);
            if (idx == null) {
                throw new IllegalArgumentException("Parameter not found: " + nombre);
            }
            return idx;
        }

        void asignar(PreparedStatement st, String nombre, Object valor) throws SQLException {
            for (int i : de(nombre)) {
                if (valor == null) st.setNull(i, Types.VARCHAR);
                else st.setObject(i, valor);
            }
        }
    }

    private static Preparada preparar(String sql) {
        String limpio = sql.replaceAll(COMENTARIOS, "");
        StringBuilder salida = new StringBuilder(limpio.length());
        Map<String, List<Integer>> mapa = new HashMap<>();
        boolean simple = false;
        boolean doble = false;
        int indice = 1;
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
                String nombre = limpio.substring(i + 1, j);
                c = '?';
                i += nombre.length();
                mapa.computeIfAbsent(nombre, k -> new LinkedList<>()).add(indice++);
            }
            salida.append(c);
        }
        Map<String, int[]> indices = new LinkedHashMap<>();
        mapa.forEach((k, v) -> indices.put(k, v.stream().mapToInt(Integer::intValue).toArray()));
        return new Preparada(salida.toString(), indices);
    }

    /** SELECT mapped to the public fields of {@code tipo} (legacy {@code SQL.query(con, sql, params, Class)}). */
    public static <T> List<T> consultar(Connection con, String sql, Map<String, Object> params, Class<T> tipo) throws SQLException {
        if (con == null) throw new SQLException("Null connection");
        if (sql == null) throw new SQLException("Null SQL statement");
        if (params == null) throw new SQLException("Null Class Params. If parameters aren't need, pass an empty");
        if (tipo == null) throw new SQLException("Null Class Type");
        List<T> resultado = new ArrayList<>();
        try {
            Preparada p = preparar(sql);
            try (PreparedStatement st = con.prepareStatement(p.sql())) {
                for (Map.Entry<String, Object> e : params.entrySet()) {
                    p.asignar(st, e.getKey(), e.getValue());
                }
                try (ResultSet rs = st.executeQuery()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int columnas = meta.getColumnCount();
                    while (rs.next()) {
                        T obj = tipo.getDeclaredConstructor().newInstance();
                        resultado.add(obj);
                        for (int i = 1; i <= columnas; i++) {
                            Field campo = campo(obj, meta.getColumnLabel(i));
                            if (campo != null) campo.set(obj, valor(rs.getObject(i)));
                        }
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new SQLException(e.getMessage(), e);
        } catch (SQLException e) {
            throw rethrow(e, sql, params);
        }
        return resultado;
    }

    private static Field campo(Object obj, String etiqueta) {
        try {
            return obj.getClass().getField(etiqueta);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static Object valor(Object v) throws SQLException {
        if (v instanceof Blob b) return b.length() == 0 ? new byte[0] : b.getBytes(1, (int) b.length());
        if (v instanceof Clob c) return c.length() == 0 ? "" : c.getSubString(1, (int) c.length());
        return v;
    }

    /**
     * Legacy {@code SQL.batch(con, sql, list, type)}: one statement, one {@code addBatch} per element (the parameters are read from the public field of the element that
     * has the name of the {@code :name}; a BigDecimal is rescaled to 16 decimals HALF_DOWN), one {@code executeBatch}. A null list fails; a null element or a missing field
     * fails with {@code "Map Object failed"}.
     */
    public static <T> int[] lote(Connection con, String sql, List<T> filas, Class<T> tipo) throws SQLException {
        if (con == null) throw new SQLException("Null connection");
        if (sql == null) throw new SQLException("Null SQL statement");
        if (filas == null) throw new SQLException("Null parameters. If parameters aren't need, pass an empty");
        if (tipo == null) throw new SQLException("Null Class Type.");
        try {
            Preparada p = preparar(sql);
            try (PreparedStatement st = con.prepareStatement(p.sql())) {
                String[] nombres = p.indices().keySet().toArray(new String[0]);
                for (T fila : filas) {
                    try {
                        T obj = tipo.cast(fila);
                        for (String nombre : nombres) {
                            Field campo = obj.getClass().getField(nombre);
                            Object v = campo.get(obj);
                            if (v != null && campo.getType().isAssignableFrom(BigDecimal.class)) {
                                v = ((BigDecimal) v).setScale(16, RoundingMode.HALF_DOWN);
                            }
                            p.asignar(st, nombre, v);
                        }
                    } catch (Exception er) {
                        throw new SQLException("Map Object failed", er);
                    }
                    st.addBatch();
                }
                if (nombres.length == 0 && filas.isEmpty()) {
                    st.addBatch();
                }
                return st.executeBatch();
            }
        } catch (SQLException e) {
            throw rethrow(e, sql, filas.toArray());
        }
    }

    /** Legacy {@code SQL.exec(con, sql)}: the text goes to {@code Statement.execute} as is. */
    public static boolean ejecutar(Connection con, String sql) throws SQLException {
        if (con == null) throw new SQLException("Null connection");
        if (sql == null) throw new SQLException("Null SQL statement");
        try (Statement st = con.createStatement()) {
            return st.execute(sql);
        }
    }

    private static SQLException rethrow(SQLException causa, String sql, Object params) {
        String mensaje = causa.getMessage();
        if (causa.getCause() != null) {
            mensaje = mensaje + " " + causa.getCause().getMessage();
        }
        Object[] lista = params instanceof Object[] a ? a : new Object[] { params };
        SQLException e = new SQLException(mensaje + " Query: " + sql + " Parameters: " + Arrays.deepToString(lista), causa.getSQLState(), causa.getErrorCode());
        e.setNextException(causa);
        return e;
    }
}
