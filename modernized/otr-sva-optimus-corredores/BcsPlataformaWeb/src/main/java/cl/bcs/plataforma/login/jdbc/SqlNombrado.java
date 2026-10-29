package cl.bcs.plataforma.login.jdbc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
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
import java.util.regex.Pattern;

/**
 * Named-parameter SQL executor with the semantics of legacy {@code cl.bcs.utils.SQL.query / batch} + {@code NamedParameterStatement}, so the legacy
 * login SQL runs unchanged on the connection of the request (one transaction, owned by the caller; no pool, no state):
 * <ul>
 *   <li>comments {@code /* *}{@code / , // and --} are removed first (precompiled legacy expression);</li>
 *   <li>a {@code List} parameter expands the textual {@code :name} into {@code :name_0,:name_1,...}; an EMPTY list into one NULL parameter;</li>
 *   <li>{@code :name} is recognised outside single/double quotes (the {@code "SCHEMA::TABLE"} of HANA objects is safe); a parameter in the map but
 *       not in the SQL fails; a {@code null} value is {@code setNull(VARCHAR)};</li>
 *   <li>rows are mapped to the PUBLIC FIELDS of the pojo by exact column label; a numeric column is converted to the numeric type of the field
 *       (the legacy relied on the driver returning exactly that type, the H2 and HANA types of a column can differ in width); columns without a
 *       field are ignored;</li>
 *   <li>{@link #lote} follows {@code SQL.batch}: one set of values per element read from its public fields.</li>
 * </ul>
 * MVP login (T-13): every statement of one request goes through the same {@link Connection}.
 */
public final class SqlNombrado {

    private static final Pattern COMENTARIOS = Pattern.compile("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)");

    private SqlNombrado() {
    }

    /** SELECT mapped to the public fields of {@code tipo}. */
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
                            f.set(obj, leer(rs, i, f.getType()));
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

    /** INSERT / UPDATE / DELETE with the parameters of the map. @return rows affected */
    public static int actualizar(Connection con, String sql, Map<String, Object> params) throws SQLException {
        if (con == null) throw new SQLException("Null connection");
        Preparada p = preparar(sql, params);
        try (PreparedStatement st = con.prepareStatement(p.sql)) {
            asignar(st, p);
            return st.executeUpdate();
        }
    }

    /** Legacy {@code SQL.batch(con, sql, list, type)}: the named parameters are read from the public fields of each element. */
    public static <T> void lote(Connection con, String sql, List<T> elementos, Class<T> tipo) throws SQLException {
        if (con == null) throw new SQLException("Null connection");
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

    /**
     * The value of a column for a pojo field. Dates and timestamps are read as {@code java.time} values and turned into {@code java.sql} ones
     * with {@code valueOf}: the date a field shows is the date of the column in any driver and any calendar (a driver that converts through the
     * hybrid Julian calendar shows 0001-01-01 as 0001-01-03, and the legacy tests the text "0001-01-01").
     */
    private static Object leer(ResultSet rs, int i, Class<?> destino) throws SQLException {
        if (destino == java.sql.Date.class) {
            try {
                java.time.LocalDate d = rs.getObject(i, java.time.LocalDate.class);
                return d == null ? null : java.sql.Date.valueOf(d);
            } catch (SQLException e) {
                return rs.getDate(i);   // the HANA driver does not convert DATE to java.time: the plain read, as the legacy did
            }
        }
        if (destino == java.sql.Timestamp.class) {
            try {
                java.time.LocalDateTime t = rs.getObject(i, java.time.LocalDateTime.class);
                return t == null ? null : java.sql.Timestamp.valueOf(t);
            } catch (SQLException e) {
                return rs.getTimestamp(i);
            }
        }
        if (destino == byte[].class) {
            return rs.getBytes(i);   // HANA hands a BLOB column as a Blob object, H2 as bytes: read the bytes in both
        }
        if (destino == String.class) {
            return rs.getString(i);   // a text field takes the text form of any column (a timestamp column read by {@code getString}, as the legacy servlets did)
        }
        return convertir(rs.getObject(i), destino);
    }

    private static Object convertir(Object valor, Class<?> destino) {
        if (valor instanceof Number n && !destino.isInstance(valor)) {
            if (destino == Long.class || destino == long.class) return n.longValue();
            if (destino == Integer.class || destino == int.class) return n.intValue();
            if (destino == BigDecimal.class) return new BigDecimal(n.toString());
            if (destino == Double.class || destino == double.class) return n.doubleValue();
        }
        return valor;
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
        // a trailing ';' of a statement file is not sent (JDBC drivers differ on it)
        String texto = salida.toString().stripTrailing();
        if (texto.endsWith(";")) {
            texto = texto.substring(0, texto.length() - 1);
        }
        Map<String, int[]> indices = new LinkedHashMap<>();
        mapa.forEach((k, v) -> indices.put(k, v.stream().mapToInt(Integer::intValue).toArray()));
        return new Preparada(texto, indices, valores);
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
