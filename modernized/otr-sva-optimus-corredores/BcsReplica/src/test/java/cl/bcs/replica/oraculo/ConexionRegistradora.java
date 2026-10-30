package cl.bcs.replica.oraculo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * JDBC connection of the BcsReplica oracle AND of its JUnit tests (one source file, byte for byte in both places): a recording proxy over an H2 connection.
 * <ul>
 * <li>EVERY statement the code under test sends is recorded, in order, as the text the database receives (comments removed, whitespace collapsed) plus, for a batch,
 *     the parameter rows; this is the golden of the SQL of the module (the 58 statements of the legacy are pinned as text).</li>
 * <li>A statement that H2 can run is run on H2 after a mechanical dialect translation (UPSERT ... WITH PRIMARY KEY -> MERGE ... KEY, ADD_DAYS -> DATEADD, CURRENT_TIMESTAMP ->
 *     the fixed clock, the PLACEHOLDER arguments of an analytic view are dropped: the view is a stand-in table seeded by the case). A statement that HANA alone can run
 *     (CREATE/DROP TRIGGER, any view of BD.Vistas other than the Sebra and the four analytic stand-ins) is recorded as {@code NO_EJECUTABLE_H2} and has no effect on the data:
 *     the H2/HANA limit documented in ORACLE_NOTES.md.</li>
 * <li>A scripted failure ({@code fallos}: text contained in the statement, n-th occurrence, message) makes the statement throw an SQLException when it is sent.</li>
 * </ul>
 * Java 11 syntax on purpose (the oracle compiles with --release 11). Nothing here opens a connection: the caller hands in the H2 connection.
 */
public final class ConexionRegistradora {
    public static final String H2 = "H2";
    public static final String NO_EJECUTABLE = "NO_EJECUTABLE_H2";
    public static final String FALLO = "FALLO_INYECTADO";

    private static final Map<String, String> CLAVES_UPSERT = new HashMap<>();

    static {
        CLAVES_UPSERT.put("\"BSCL.Plataforma.BD::PLA.ResumenReplica\"", "\"UltimaActualizacion\", \"Nombre\", \"FechaSistema\"");
        CLAVES_UPSERT.put("\"BSCL.Replica.BD::EnvioPostPauta\"", "\"FechaSistema\", \"Tabla\", \"Fecha\"");
    }

    private static final Pattern COMENTARIOS = Pattern.compile("/\\*.*?\\*/|--[^\\n]*", Pattern.DOTALL);
    private static final Pattern UPSERT = Pattern.compile("^\\s*UPSERT\\s+(\"[^\"]+\")\\s*\\(([^)]*)\\)\\s*VALUES\\s*\\((.*)\\)\\s*WITH\\s+PRIMARY\\s+KEY\\s*;?\\s*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern ADD_DAYS = Pattern.compile("ADD_DAYS\\(\\s*('[^']*')\\s*,\\s*(-?\\d+)\\s*\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PLACEHOLDER_ARGS = Pattern.compile("\\(\\s*PLACEHOLDER\\.[^)]*\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern VISTA_NO_MODELADA = Pattern.compile("\\.Vistas::(?!Sebra\\.|ConsultarSaldoCuentaInversionFecha(?:Efectiva|Movimiento)Analitica\"|CUS\\.ConsultarCustodiaFecha(?:Operacion|Liquidacion)Analitica\")");
    private static final Pattern AHORA = Pattern.compile("\\bCURRENT_TIMESTAMP\\b", Pattern.CASE_INSENSITIVE);

    private final Connection raw;
    private final String reloj;
    private final JsonArray sentencias = new JsonArray();
    private final List<JsonObject> fallos = new ArrayList<>();
    private final Map<JsonObject, Integer> vistos = new HashMap<>();
    private final Connection proxy;

    /**
     * @param raw    the H2 connection (autocommit off), schema already selected
     * @param fallos the "fallos" of the case: [{"contiene": "...", "ocurrencia": 1, "mensaje": "..."}] (null or empty: none)
     * @param reloj  fixed clock text "yyyy-MM-dd HH:mm:ss" that replaces CURRENT_TIMESTAMP in the SQL run on H2
     */
    public ConexionRegistradora(Connection raw, JsonArray fallos, String reloj) {
        this.raw = raw;
        this.reloj = reloj;
        if (fallos != null) for (JsonElement f : fallos) this.fallos.add(f.getAsJsonObject());
        this.proxy = (Connection) Proxy.newProxyInstance(ConexionRegistradora.class.getClassLoader(), new Class<?>[] { Connection.class }, new ManejadorConexion());
    }

    public Connection conexion() {
        return proxy;
    }

    public JsonArray sentencias() {
        return sentencias;
    }

    /** The text as the database receives it: comments out, whitespace collapsed. */
    public static String normalizar(String sql) {
        return COMENTARIOS.matcher(sql).replaceAll(" ").replaceAll("\\s+", " ").trim();
    }

    static String clasificar(String normalizado) {
        String u = normalizado.toUpperCase();
        if (u.startsWith("CREATE TRIGGER") || u.startsWith("DROP TRIGGER")) return NO_EJECUTABLE;
        if (PLACEHOLDER_ARGS.matcher(normalizado).replaceAll("").toUpperCase().contains("PLACEHOLDER.")) return NO_EJECUTABLE;
        if (VISTA_NO_MODELADA.matcher(normalizado).find()) return NO_EJECUTABLE;
        return H2;
    }

    /** Dialect translation of one statement (no ';' separators left). */
    String aH2(String sql) {
        String s = sql.trim();
        Matcher u = UPSERT.matcher(s);
        if (u.matches()) {
            String clave = CLAVES_UPSERT.get(u.group(1));
            if (clave == null) throw new IllegalStateException("UPSERT on a table without a declared key: " + u.group(1));
            s = "MERGE INTO " + u.group(1) + " (" + u.group(2) + ") KEY (" + clave + ") VALUES (" + u.group(3) + ")";
        }
        s = PLACEHOLDER_ARGS.matcher(s).replaceAll("");
        s = ADD_DAYS.matcher(s).replaceAll("DATEADD('DAY', $2, CAST($1 AS TIMESTAMP))");
        s = AHORA.matcher(s).replaceAll(Matcher.quoteReplacement("TIMESTAMP '" + reloj + "'"));
        return s;
    }

    /** Splits a text with several statements on the ';' that are outside single and double quotes. */
    static List<String> partir(String sql) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean simple = false;
        boolean doble = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\'' && !doble) simple = !simple;
            else if (c == '"' && !simple) doble = !doble;
            if (c == ';' && !simple && !doble) {
                if (!cur.toString().isBlank()) out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (!cur.toString().isBlank()) out.add(cur.toString());
        return out;
    }

    private SQLException fallo(String normalizado) {
        for (JsonObject f : fallos) {
            if (normalizado.contains(f.get("contiene").getAsString())) {
                int n = vistos.merge(f, 1, Integer::sum);
                int objetivo = f.has("ocurrencia") ? f.get("ocurrencia").getAsInt() : 1;
                if (n == objetivo) return new SQLException(f.get("mensaje").getAsString());
            }
        }
        return null;
    }

    /** A text longer than 300 characters is recorded as its first 120, its last 200, its length and its SHA-256 (the golden stays readable and the whole text stays pinned). */
    static void ponerSql(JsonObject rec, String norm) {
        if (norm.length() <= 300) {
            rec.addProperty("sql", norm);
            return;
        }
        rec.addProperty("sql", norm.substring(0, 120) + " ... " + norm.substring(norm.length() - 200));
        rec.addProperty("sqlLargo", norm.length());
        try {
            byte[] h = java.security.MessageDigest.getInstance("SHA-256").digest(norm.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            rec.addProperty("sqlSha256", sb.toString());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    static JsonElement valor(Object v) {
        if (v == null) return JsonNull.INSTANCE;
        if (v instanceof Number) return new com.google.gson.JsonPrimitive((Number) v);
        if (v instanceof CharSequence) return new com.google.gson.JsonPrimitive(v.toString());
        if (v instanceof Boolean) return new com.google.gson.JsonPrimitive((Boolean) v);
        JsonObject o = new JsonObject();
        if (v instanceof java.sql.Date || v instanceof java.time.LocalDate) o.addProperty("fecha", v.toString());
        else if (v instanceof java.util.Date) o.addProperty("fechaHora", new java.sql.Timestamp(((java.util.Date) v).getTime()).toString());
        else if (v instanceof java.time.LocalDateTime) o.addProperty("fechaHora", java.sql.Timestamp.valueOf((java.time.LocalDateTime) v).toString());
        else o.addProperty("objeto", v.toString());
        return o;
    }

    private final class ManejadorConexion implements InvocationHandler {
        @Override
        public Object invoke(Object p, Method m, Object[] a) throws Throwable {
            try {
                if (m.getName().equals("createStatement")) {
                    Statement real = (Statement) m.invoke(raw, a);
                    return Proxy.newProxyInstance(ConexionRegistradora.class.getClassLoader(), new Class<?>[] { Statement.class }, new ManejadorStatement(real));
                }
                if (m.getName().equals("prepareStatement") && a != null && a.length >= 1 && a[0] instanceof String) {
                    String sql = (String) a[0];
                    String norm = normalizar(sql);
                    PreparedStatement real = null;
                    if (clasificar(norm).equals(H2)) {
                        Object[] b = a.clone();
                        b[0] = aH2(sql);
                        real = (PreparedStatement) m.invoke(raw, b);
                    }
                    return Proxy.newProxyInstance(ConexionRegistradora.class.getClassLoader(), new Class<?>[] { PreparedStatement.class }, new ManejadorPrepared(real, norm));
                }
                return m.invoke(raw, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    private final class ManejadorStatement implements InvocationHandler {
        private final Statement real;

        ManejadorStatement(Statement real) {
            this.real = real;
        }

        @Override
        public Object invoke(Object p, Method m, Object[] a) throws Throwable {
            try {
                if (m.getName().equals("execute") && a != null && a.length == 1 && a[0] instanceof String) {
                    ejecutar((String) a[0]);
                    return Boolean.FALSE;
                }
                return m.invoke(real, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private void ejecutar(String sql) throws SQLException {
            String norm = normalizar(sql);
            JsonObject rec = new JsonObject();
            rec.addProperty("tipo", "exec");
            ponerSql(rec, norm);
            sentencias.add(rec);
            SQLException f = fallo(norm);
            if (f != null) {
                rec.addProperty("ejecucion", FALLO);
                rec.addProperty("error", f.getMessage());
                throw f;
            }
            String clase = clasificar(norm);
            rec.addProperty("ejecucion", clase);
            if (!clase.equals(H2)) return;
            JsonArray filas = new JsonArray();
            rec.add("filasAfectadas", filas);
            for (String parte : partir(COMENTARIOS.matcher(sql).replaceAll(" "))) {
                real.execute(aH2(parte));
                filas.add(real.getUpdateCount());
            }
        }
    }

    private final class ManejadorPrepared implements InvocationHandler {
        private final PreparedStatement real;
        private final String norm;
        private final Map<Integer, Object> actual = new TreeMap<>();
        private final List<Map<Integer, Object>> filas = new ArrayList<>();

        ManejadorPrepared(PreparedStatement real, String norm) {
            this.real = real;
            this.norm = norm;
        }

        @Override
        public Object invoke(Object p, Method m, Object[] a) throws Throwable {
            try {
                String n = m.getName();
                if (n.startsWith("set") && a != null && a.length >= 2 && a[0] instanceof Integer) {
                    actual.put((Integer) a[0], n.equals("setNull") ? null : a[1]);
                    if (real != null) m.invoke(real, a);
                    return null;
                }
                if (n.equals("addBatch") && (a == null || a.length == 0)) {
                    filas.add(new TreeMap<>(actual));
                    if (real != null) real.addBatch();
                    return null;
                }
                if (n.equals("executeBatch")) return ejecutarLote();
                if (n.equals("execute") || n.equals("executeUpdate")) {
                    filas.add(new TreeMap<>(actual));
                    Object r = ejecutarLote();
                    return n.equals("execute") ? (Object) Boolean.FALSE : (Object) Integer.valueOf(((int[]) r).length == 0 ? 0 : ((int[]) r)[0]);
                }
                if (real == null) {
                    if (n.equals("close") || n.equals("clearParameters") || n.equals("clearBatch")) return null;
                    throw new SQLException("statement not executable on H2 (" + n + "): " + norm);
                }
                return m.invoke(real, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private int[] ejecutarLote() throws SQLException {
            JsonObject rec = new JsonObject();
            rec.addProperty("tipo", "batch");
            ponerSql(rec, norm);
            JsonArray rows = new JsonArray();
            for (Map<Integer, Object> f : filas) {
                JsonArray r = new JsonArray();
                for (Object v : f.values()) r.add(valor(v));
                rows.add(r);
            }
            rec.add("filas", rows);
            sentencias.add(rec);
            SQLException f = fallo(norm);
            if (f != null) {
                rec.addProperty("ejecucion", FALLO);
                rec.addProperty("error", f.getMessage());
                filas.clear();
                throw f;
            }
            String clase = clasificar(norm);
            rec.addProperty("ejecucion", clase);
            int[] cuentas;
            if (real == null) {
                cuentas = new int[filas.size()];
                java.util.Arrays.fill(cuentas, -2);
            } else {
                cuentas = real.executeBatch();
                JsonArray c = new JsonArray();
                for (int x : cuentas) c.add(x);
                rec.add("filasAfectadas", c);
            }
            filas.clear();
            return cuentas;
        }
    }
}
