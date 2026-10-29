package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.TransactionTimedOutException;

/**
 * Named-parameter SQL executor with the SEMANTICS of legacy {@code cl.bcs.utils.SQL.query} + {@code NamedParameterStatement}
 * ({@code BcsUtils}), written to run the legacy SQL text unchanged on the connection of the unit of work (no pool, no state):
 * <ul>
 *   <li>comments {@code /* *}{@code / , // and --} are removed with the legacy regular expression before anything else;</li>
 *   <li>a {@code List} parameter expands the textual {@code :name} into {@code :name_0,:name_1,...}; an EMPTY list into one NULL parameter
 *       ({@code IN (:x)} matches nothing) - the legacy plain-text replace, prefix collisions included;</li>
 *   <li>{@code :name} is recognised outside single/double quotes (the {@code "SCHEMA::TABLE"} of HANA objects is therefore safe); a parameter that is
 *       in the map but not in the SQL fails ({@code Parameter not found}) as in the legacy; a {@code null} value is {@code setNull(VARCHAR)};</li>
 *   <li>rows are mapped to the PUBLIC FIELDS of the pojo by exact column label (case sensitive, no conversion: a type mismatch fails, as the legacy
 *       {@code Field.set} did); columns without a field are ignored.</li>
 * </ul>
 * The trailing {@code ;} of the SQL files is sent as is (the legacy did). Only {@link Connection} is used: the caller owns the transaction.
 */
public final class SqlNombrado {

    private static final String COMENTARIOS = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?:/" + "/.*)|(?:--.*)";  // the literal is split so that the comment stripper of SinCambiosDeEsquemaTest does not cut it at the double slash

    /** Test hook (M4 contract test): sees, per query, every mapped column with the Java class the driver declares. Null in production. */
    interface ObservadorMapeo {
        void columna(Class<?> pojo, Field campo, String etiqueta, String claseColumna, int precision, int escala) throws SQLException;
    }

    static volatile ObservadorMapeo observador;

    /**
     * DataSource whose transaction deadline bounds the statements of the current thread (set by {@link ConexionUnidadDeTrabajo#usar}; null in
     * {@code usarAunVencido} and outside a unit of work: then no query timeout is applied, exactly as before).
     */
    private static final ThreadLocal<DataSource> PLAZO = new ThreadLocal<>();

    private SqlNombrado() {
    }

    /** Sets the DataSource whose remaining transaction time becomes the query timeout of the next statements; returns the previous one (to restore it). */
    static DataSource plazoDe(DataSource ds) {
        DataSource anterior = PLAZO.get();
        if (ds == null) PLAZO.remove();
        else PLAZO.set(ds);
        return anterior;
    }

    /** Remaining time of the Spring transaction as the query timeout; no-op without a deadline. A transaction already expired fails as Spring does. */
    private static void aplicarPlazo(PreparedStatement st) throws SQLException {
        aplicarPlazo(st, false);
    }

    /**
     * Same, for any statement (used by {@link ConexionAcotada}). {@code tolerarVencido}: a deadline already passed does not fail the statement but gives it the minimum
     * timeout of 1 second (the cleanup DELETE of a library, which must run so that the rows do not outlive the request).
     */
    static void aplicarPlazo(Statement st, boolean tolerarVencido) throws SQLException {
        DataSource ds = PLAZO.get();
        if (ds == null) return;
        try {
            DataSourceUtils.applyTimeout(st, ds, -1);
        } catch (TransactionTimedOutException e) {
            if (!tolerarVencido) throw e;
            st.setQueryTimeout(1);
        }
    }

    /** SELECT mapped to the public fields of {@code tipo} (legacy {@code SQL.query(con, sql, params, Class)}). */
    public static <T> List<T> consultar(Connection con, String sql, Map<String, Object> params, Class<T> tipo) throws SQLException {
        return consultar(con, sql, params, tipo, false);
    }

    /**
     * Same as {@link #consultar(Connection, String, Map, Class)} but a column is matched to the field by its UNDERLYING column NAME ({@code getColumnName}),
     * not by the label: the legacy {@code SQL.queryParallel} ({@code RowSetDynaClass}) does so, so {@code "IdSerieFondo" as "IdSerieFondoOrigen"} fills
     * {@code IdSerieFondo} (D-F3A-28). Used only by the fronts that need it; the label variant is unchanged.
     */
    public static <T> List<T> consultarPorNombreDeColumna(Connection con, String sql, Map<String, Object> params, Class<T> tipo) throws SQLException {
        return consultar(con, sql, params, tipo, true);
    }

    private static String nombre(ResultSetMetaData md, int i, boolean porNombre) throws SQLException {
        return porNombre ? md.getColumnName(i) : md.getColumnLabel(i);
    }

    private static <T> List<T> consultar(Connection con, String sql, Map<String, Object> params, Class<T> tipo, boolean porNombre) throws SQLException {
        List<T> salida = new ArrayList<>();
        ejecutar(con, sql, params, rs -> {
            ResultSetMetaData md = rs.getMetaData();
            ObservadorMapeo observa = observador;
            if (observa != null) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    Field f = campo(tipo, nombre(md, i, porNombre));
                    if (f != null) observa.columna(tipo, f, nombre(md, i, porNombre), md.getColumnClassName(i), md.getPrecision(i), md.getScale(i));
                }
            }
            try {
                while (rs.next()) {
                    T obj = tipo.getDeclaredConstructor().newInstance();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        Field f = campo(tipo, nombre(md, i, porNombre));
                        if (f != null) {
                            asignarCampo(f, obj, rs.getObject(i), tipo);
                        }
                    }
                    salida.add(obj);
                }
            } catch (ReflectiveOperationException e) {
                throw new SQLException(e.getMessage(), e);
            }
        });
        return salida;
    }

    /**
     * Legacy {@code Field.set}: no conversion. A type mismatch (a HANA column whose driver class differs from the pojo field) still fails with
     * {@link IllegalArgumentException} as the legacy, but the message now names the field, the pojo class and the two Java classes (M4). NEVER the value.
     */
    private static void asignarCampo(Field f, Object obj, Object valor, Class<?> tipo) throws IllegalAccessException {
        try {
            f.set(obj, valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Row mapping failed: field " + tipo.getName() + "." + f.getName() + " is " + f.getType().getName()
                    + " but the column returned " + (valor == null ? "null" : valor.getClass().getName()), e);
        }
    }

    /** SELECT as rows (column label -> value, in column order); for ports whose callers do not need a pojo. */
    public static List<Map<String, Object>> filas(Connection con, String sql, Map<String, Object> params) throws SQLException {
        List<Map<String, Object>> salida = new ArrayList<>();
        ejecutar(con, sql, params, rs -> {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    fila.put(md.getColumnLabel(i), rs.getObject(i));
                }
                salida.add(fila);
            }
        });
        return salida;
    }

    /** INSERT/UPDATE/DELETE (or any statement that returns no rows). @return rows affected */
    public static int actualizar(Connection con, String sql, Map<String, Object> params) throws SQLException {
        Preparada p = preparar(sql, params);
        try (PreparedStatement st = con.prepareStatement(p.sql)) {
            aplicarPlazo(st);
            asignar(st, p);
            return st.executeUpdate();
        }
    }

    private interface Lector {
        void leer(ResultSet rs) throws SQLException;
    }

    private static void ejecutar(Connection con, String sql, Map<String, Object> params, Lector lector) throws SQLException {
        if (con == null) throw new SQLException("Null connection");
        Preparada p = preparar(sql, params);
        try (PreparedStatement st = con.prepareStatement(p.sql)) {
            aplicarPlazo(st);
            asignar(st, p);
            try (ResultSet rs = st.executeQuery()) {
                lector.leer(rs);
            }
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

    static Preparada preparar(String sql, Map<String, Object> params) throws SQLException {
        if (sql == null) throw new SQLException("Null SQL statement");
        if (params == null) throw new SQLException("Null Class Params. If parameters aren't need, pass an empty");
        String limpio = sql.replaceAll(COMENTARIOS, "");
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
        Map<String, int[]> indices = new HashMap<>();
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

    /** Rows per {@code executeBatch}: the legacy {@code SQL.batch} sent them all at once; the chunks bound the memory of the driver. */
    static final int TRAMO_LOTE = 1000;

    /**
     * Legacy {@code SQL.batch}: ONE {@link PreparedStatement} for all the rows (same SQL text), every row bound before it is added, {@code addBatch} per row and
     * {@code executeBatch} per chunk of {@value #TRAMO_LOTE} rows. The remaining time of the transaction is applied before every chunk (as {@link #actualizar} does per
     * statement). A failing row surfaces as the {@link java.sql.BatchUpdateException} of the driver (a {@link SQLException}: the caller, {@code ConexionUnidadDeTrabajo},
     * composes the same sanitised message from SQLState and ErrorCode); whether the rows after it run depends on the driver, as in the legacy. Rows must not hold
     * {@code List} values (no expansion of the SQL text per row).
     * @return the sum of the update counts the driver reports (a {@code SUCCESS_NO_INFO} counts as 0)
     */
    public static int actualizarLote(Connection con, String sql, List<Map<String, Object>> filas) throws SQLException {
        if (filas.isEmpty()) return 0;
        List<Preparada> preparadas = new ArrayList<>(filas.size());
        for (Map<String, Object> fila : filas) preparadas.add(preparar(sql, fila));
        int total = 0;
        try (PreparedStatement st = con.prepareStatement(preparadas.get(0).sql)) {
            int pendientes = 0;
            for (Preparada p : preparadas) {
                asignar(st, p);
                st.addBatch();
                if (++pendientes == TRAMO_LOTE) {
                    total += ejecutarTramo(st);
                    pendientes = 0;
                }
            }
            if (pendientes > 0) total += ejecutarTramo(st);
        }
        return total;
    }

    private static int ejecutarTramo(PreparedStatement st) throws SQLException {
        aplicarPlazo(st);
        int total = 0;
        for (int n : st.executeBatch()) if (n > 0) total += n;
        return total;
    }
}
