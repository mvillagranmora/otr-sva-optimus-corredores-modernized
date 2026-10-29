package cl.bcs.custodia.flujo.jdbc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Named-parameter SQL executor with the semantics of legacy {@code cl.bcs.utils.SQL.query / batch} + {@code NamedParameterStatement} (BcsUtils), so the
 * legacy SQL text of ConsultarFlujoCustodia runs unchanged on the connection of the caller (no pool, no state, the caller owns the transaction):
 * <ul>
 *   <li>comments {@code /* *}{@code / , // and --} are removed with the legacy regular expression first;</li>
 *   <li>a {@code List} parameter expands the textual {@code :name} into {@code :name_0,:name_1,...}; an EMPTY list into one NULL parameter;</li>
 *   <li>{@code :name} is recognised outside single/double quotes; a parameter in the map but not in the SQL fails ({@code Parameter not found}); a
 *       {@code null} value is {@code setNull(VARCHAR)};</li>
 *   <li>rows are mapped to the PUBLIC FIELDS of the pojo by exact column label, no conversion (a type mismatch fails as the legacy {@code Field.set});
 *       columns without a field are ignored;</li>
 *   <li>{@link #lote} follows {@code SQL.batch}: one set of values per element read from its public fields, BigDecimal at scale 16 HALF_DOWN.</li>
 * </ul>
 * CUS-FLU-01..36 (data access of the slice).
 */
public final class SqlNombrado {

    private static final java.util.regex.Pattern COMENTARIOS = java.util.regex.Pattern.compile("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)");

    private SqlNombrado() {
    }

    /** SELECT mapped to the public fields of {@code tipo} (legacy {@code SQL.query(con, sql, params, Class)}). */
    public static <T> List<T> consultar(Connection con, String sql, Map<String, Object> params, Class<T> tipo) throws SQLException {
        if (con == null) throw new SQLException("Null connection");
        Preparada p = preparar(sql, params);
        List<T> salida = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(p.sql)) {
            asignar(st, p);
            try (ResultSet rs = st.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                while (rs.next()) {
                    T obj = tipo.getDeclaredConstructor().newInstance();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        Field f = campo(tipo, md.getColumnLabel(i));
                        if (f != null) {
                            f.set(obj, rs.getObject(i));
                        }
                    }
                    salida.add(obj);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new SQLException(e.getMessage(), e);
        }
        return salida;
    }

    /** SELECT as rows (column label to value, in column order). */
    public static List<Map<String, Object>> filas(Connection con, String sql, Map<String, Object> params) throws SQLException {
        if (con == null) throw new SQLException("Null connection");
        Preparada p = preparar(sql, params);
        List<Map<String, Object>> salida = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(p.sql)) {
            asignar(st, p);
            try (ResultSet rs = st.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        fila.put(md.getColumnLabel(i), rs.getObject(i));
                    }
                    salida.add(fila);
                }
            }
        }
        return salida;
    }

    /**
     * Legacy {@code SQL.batch(con, sql, list, type)}: the named parameters of the SQL are read from the public fields of each element and the
     * statements run as one JDBC batch. An empty list with named parameters adds no statement (nothing is executed).
     */
    public static <T> void lote(Connection con, String sql, List<T> elementos, Class<T> tipo) throws SQLException {
        if (con == null) throw new SQLException("Null connection");
        if (elementos == null) throw new SQLException("Null parameters. If parameters aren't need, pass an empty");
        Preparada p = preparar(sql, new HashMap<>());
        try (PreparedStatement st = con.prepareStatement(p.sql)) {
            for (T elemento : elementos) {
                try {
                    T obj = tipo.cast(elemento);
                    for (Map.Entry<String, int[]> e : p.indices.entrySet()) {
                        Field f = obj.getClass().getField(e.getKey());
                        Object valor = f.get(obj);
                        for (int i : e.getValue()) {
                            if (valor == null) {
                                st.setNull(i, Types.VARCHAR);
                            } else {
                                if (f.getType().isAssignableFrom(BigDecimal.class)) {
                                    valor = ((BigDecimal) valor).setScale(16, RoundingMode.HALF_DOWN);
                                }
                                st.setObject(i, valor);
                            }
                        }
                    }
                } catch (Exception er) {
                    throw new SQLException("Map Object failed", er);
                }
                st.addBatch();
            }
            if (p.indices.isEmpty() && elementos.isEmpty()) {
                st.addBatch();
            }
            st.executeBatch();
        }
    }

    private static Field campo(Class<?> tipo, String etiqueta) {
        try {
            return tipo.getField(etiqueta);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private record Preparada(String sql, Map<String, int[]> indices, Map<String, Object> valores) {
    }

    private static Preparada preparar(String sql, Map<String, Object> params) throws SQLException {
        if (sql == null) throw new SQLException("Null SQL statement");
        if (params == null) throw new SQLException("Null Class Params. If parameters aren't need, pass an empty");
        String limpio = COMENTARIOS.matcher(sql).replaceAll("");
        Map<String, Object> valores = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : params.entrySet()) {
            if (e.getValue() instanceof List<?> lista) {
                StringBuilder nuevos = new StringBuilder();
                if (lista.isEmpty()) {
                    nuevos.append(':').append(e.getKey()).append("_0");
                    valores.put(e.getKey() + "_0", null);
                } else {
                    int n = 0;
                    for (Object o : lista) {
                        String clave = e.getKey() + "_" + n++;
                        nuevos.append(':').append(clave).append(',');
                        valores.put(clave, o);
                    }
                    nuevos.setLength(nuevos.length() - 1);
                }
                limpio = limpio.replace(":" + e.getKey(), nuevos.toString());
            } else {
                valores.put(e.getKey(), e.getValue());
            }
        }
        // NamedParameterStatement.parse
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
        return new Preparada(salida.toString(), indices, valores);
    }

    private static void asignar(PreparedStatement st, Preparada p) throws SQLException {
        for (Map.Entry<String, Object> e : p.valores.entrySet()) {
            int[] idx = p.indices.get(e.getKey());
            if (idx == null) throw new IllegalArgumentException("Parameter not found: " + e.getKey());
            for (int i : idx) {
                if (e.getValue() == null) st.setNull(i, Types.VARCHAR);
                else st.setObject(i, e.getValue());
            }
        }
    }
}
