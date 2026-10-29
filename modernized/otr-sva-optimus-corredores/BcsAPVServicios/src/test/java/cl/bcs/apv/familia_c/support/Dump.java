package cl.bcs.apv.familia_c.support;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Table dump shared by the legacy oracle runner and the modern tests, so both sides serialise rows identically:
 * numbers normalised ({@code stripTrailingZeros().toPlainString()}), everything else {@code toString()}, SQL NULL as JSON null.
 * The case declares what to dump: {@code "tablas": [{"nombre": "<HANA table>", "orden": "\"Col\", ...", "excluir": ["Col"]}]}.
 */
public final class Dump {

    private Dump() {
    }

    private static final java.util.regex.Pattern HASH = java.util.regex.Pattern.compile("@[0-9a-f]{4,8}\\b");

    /** JDK "helpful NullPointerException" text: names compiler-dependent locals ({@code "<local11>.Campo"}), so it is not a stable contract. */
    private static final java.util.regex.Pattern NPE = java.util.regex.Pattern.compile("NullPointerException: Cannot [^\n]*? is null");

    /**
     * Identity hash codes ({@code Class$Inner@6d3af739}, from Object.toString inside a legacy exception message) vary between runs: written as
     * {@code @<hash>} in the goldens and in the comparison (both sides), so a re-record is byte-identical. The JDK helpful text of a
     * NullPointerException ("Cannot invoke ... because "<local4>.x" is null") is written as {@code <helpful message>} for the same reason.
     */
    public static String sinHash(String texto) {
        if (texto == null || Boolean.getBoolean("oracle.rawmsg")) { // -Doracle.rawmsg=true: debugging only (keeps the JDK helpful NPE text)
            return texto;
        }
        return NPE.matcher(HASH.matcher(texto).replaceAll("@<hash>")).replaceAll("NullPointerException: <helpful message>");
    }

    /** Copy of {@code e} with {@link #sinHash} applied to every string value. */
    public static JsonElement sinHash(JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return e;
        }
        if (e.isJsonPrimitive()) {
            return e.getAsJsonPrimitive().isString() ? new JsonPrimitive(sinHash(e.getAsString())) : e;
        }
        if (e.isJsonArray()) {
            JsonArray a = new JsonArray();
            e.getAsJsonArray().forEach(x -> a.add(sinHash(x)));
            return a;
        }
        JsonObject o = new JsonObject();
        e.getAsJsonObject().entrySet().forEach(en -> o.add(en.getKey(), sinHash(en.getValue())));
        return o;
    }

    /** The string list case.json declares under {@code clave} (e.g. "enmascarar"), as a set; empty when absent. */
    public static Set<String> claves(JsonObject caso, String clave) {
        Set<String> s = new HashSet<>();
        if (caso.has(clave)) {
            for (JsonElement e : caso.getAsJsonArray(clave)) {
                s.add(e.getAsString());
            }
        }
        return s;
    }

    /**
     * Copy of {@code e} in which every non-null value of an object member named in {@code claves} that looks like a date or timestamp text
     * ({@link #TIMESTAMP}) is replaced by {@code <TS>} (the member stays visible, only the run-day value is hidden). Used on BOTH sides (oracle and modern
     * test) for outputs and collaborator traces that carry the date the legacy took from the system clock ({@code new Date()}).
     * A key written {@code "*Key"} masks ANY primitive value of {@code Key} as {@code <M>} (random numbers such as AgregarLog's IdTrabajo).
     */
    public static JsonElement enmascarar(JsonElement e, Set<String> claves) {
        if (claves.isEmpty() || e == null) {
            return e;
        }
        JsonElement c = e.deepCopy();
        mascarar(c, claves);
        return c;
    }

    private static void mascarar(JsonElement e, Set<String> claves) {
        if (e == null || e.isJsonNull() || e.isJsonPrimitive()) {
            return;
        }
        if (e.isJsonArray()) {
            e.getAsJsonArray().forEach(x -> mascarar(x, claves));
            return;
        }
        JsonObject o = e.getAsJsonObject();
        for (String k : new java.util.ArrayList<>(o.keySet())) {
            JsonElement v = o.get(k);
            if (claves.contains("*" + k) && v.isJsonPrimitive()) {
                // "*Key": any primitive value of that key is run-dependent (e.g. IdTrabajo = Math.random() in the legacy AgregarLog callers): written as <M>
                o.addProperty(k, "<M>");
            } else if (claves.contains(k) && v.isJsonPrimitive() && v.getAsJsonPrimitive().isString()) {
                o.addProperty(k, TIMESTAMP.matcher(v.getAsString()).replaceAll("<TS>"));
            } else {
                mascarar(v, claves);
            }
        }
    }

    public static Set<String> excluir(JsonObject spec) {
        Set<String> s = new HashSet<>();
        if (spec.has("excluir")) {
            for (JsonElement e : spec.getAsJsonArray("excluir")) {
                s.add(e.getAsString());
            }
        }
        return s;
    }

    /** Convenience: dump as declared by a case "tablas" entry ({nombre, orden, columnas?, excluir?}). */
    public static JsonArray tabla(Connection conn, JsonObject spec) throws SQLException {
        return tabla(conn, spec.get("nombre").getAsString(), spec.get("orden").getAsString(), excluir(spec), columnas(spec), enmascarar(spec));
    }

    /**
     * Columns whose value is a wall-clock timestamp taken by the DATABASE ({@code CURRENT_TIMESTAMP}, e.g. PAC.LogMandatoPAC.FechaProceso): the
     * case lists them in {@code "enmascarar"} and every timestamp-looking value of those columns is written as {@code <TS>} on both sides
     * (the golden still shows whether the column was set). Regex: {@link #TIMESTAMP}.
     */
    public static Set<String> enmascarar(JsonObject spec) {
        Set<String> s = new HashSet<>();
        if (spec.has("enmascarar")) {
            for (JsonElement e : spec.getAsJsonArray("enmascarar")) {
                s.add(e.getAsString());
            }
        }
        return s;
    }

    /** yyyy-MM[-dd[ T]HH:mm:ss[.fraction]]: the shape of a database date, timestamp or year-month period rendered by the driver. */
    public static final java.util.regex.Pattern TIMESTAMP = java.util.regex.Pattern.compile("\\d{4}-\\d{2}(-\\d{2}([ T]\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?)?)?");

    /**
     * Select list of a "tablas" entry: {@code columnas} (default all) plus {@code expresiones} ({"alias": "SQL expression"}), used to dump a
     * derived value instead of a raw one that depends on the H2/JVM (e.g. year-1 placeholder timestamps).
     */
    public static String columnas(JsonObject spec) {
        StringBuilder b = new StringBuilder();
        if (!spec.has("columnas")) {
            b.append('*');
        } else {
            for (JsonElement e : spec.getAsJsonArray("columnas")) {
                if (b.length() > 0) {
                    b.append(", ");
                }
                b.append('"').append(e.getAsString()).append('"');
            }
        }
        if (spec.has("expresiones")) {
            for (var e : spec.getAsJsonObject("expresiones").entrySet()) {
                b.append(", (").append(e.getValue().getAsString()).append(") AS \"").append(e.getKey()).append('"');
            }
        }
        return b.toString();
    }

    public static JsonArray tabla(Connection conn, String table, String orderBy, Set<String> exclude) throws SQLException {
        return tabla(conn, table, orderBy, exclude, "*");
    }

    public static JsonArray tabla(Connection conn, String table, String orderBy, Set<String> exclude, String columnas) throws SQLException {
        return tabla(conn, table, orderBy, exclude, columnas, Set.of());
    }

    public static JsonArray tabla(Connection conn, String table, String orderBy, Set<String> exclude, String columnas, Set<String> enmascarar) throws SQLException {
        JsonArray rows = new JsonArray();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT " + columnas + " FROM \"" + table + "\" ORDER BY " + orderBy)) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                JsonObject row = new JsonObject();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    String col = md.getColumnLabel(i);
                    if (exclude.contains(col)) {
                        continue;
                    }
                    Object v = rs.getObject(i);
                    if (v == null) {
                        row.add(col, JsonNull.INSTANCE);
                    } else if (v instanceof Number num) {
                        row.add(col, new JsonPrimitive(new BigDecimal(new BigDecimal(num.toString()).stripTrailingZeros().toPlainString())));
                    } else if (v instanceof java.util.Date) {
                        // ISO text from the driver: java.sql.Date#toString would shift years < 1582 (Julian calendar), e.g. 0001-01-01 -> 0001-01-03
                        row.addProperty(col, enmascarar.contains(col) ? TIMESTAMP.matcher(rs.getString(i)).replaceAll("<TS>") : rs.getString(i));
                    } else {
                        row.addProperty(col, enmascarar.contains(col) ? TIMESTAMP.matcher(v.toString()).replaceAll("<TS>") : v.toString());
                    }
                }
                rows.add(row);
            }
        }
        return rows;
    }
}
