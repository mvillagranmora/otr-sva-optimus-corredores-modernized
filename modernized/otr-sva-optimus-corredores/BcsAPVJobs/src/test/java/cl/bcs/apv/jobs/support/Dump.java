package cl.bcs.apv.jobs.support;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Table dump shared by the legacy oracle runner and the modern tests, so both sides serialise rows identically:
 * numbers normalised ({@code stripTrailingZeros().toPlainString()}), everything else {@code toString()}, SQL NULL as JSON null.
 * The case declares what to dump:
 * {@code "tablas": [{"nombre": "<HANA table>", "orden": "\"Col\", ...", "excluir": ["Col"], "enmascarar": ["Col"], "columnas": [...], "expresiones": {...}}]}.
 *
 * <p>{@code enmascarar}: columns whose value is a wall-clock instant of the RUN (the legacy takes {@code System.currentTimeMillis()} in Java:
 * AsignarContratoARecaudacion, ValidarYAsignarContratosARecaudaciones): every date/timestamp text of those columns is written as {@code <TS>} on both sides
 * (the golden still shows whether the column was set). See the README, "Dates are masked, why".
 */
public final class Dump {

    private Dump() {
    }

    /** yyyy-MM-dd with an optional [ T]HH:mm:ss[.fraction]: the shape of a database date or timestamp rendered by the driver. */
    public static final Pattern TIMESTAMP = Pattern.compile("\\d{4}-\\d{2}-\\d{2}([ T]\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?)?");

    /** yyyyMMddHHmmss: the run-clock stamp of a job file name (APV/LOG_CARGA_MASIVA_PREVIRED_userId_yyyyMMddHHmmss.txt). */
    public static final Pattern MARCA_14 = Pattern.compile("\\d{14}");

    /** Masks the wall-clock instant of the RUN inside a text: a dashed date/timestamp and a 14 digit stamp both become {@code <TS>}. */
    public static String enmascararInstante(String texto) {
        return texto == null ? null : MARCA_14.matcher(TIMESTAMP.matcher(texto).replaceAll("<TS>")).replaceAll("<TS>");
    }

    /**
     * The HANA placeholder date 0001-01-01 written through java.sql.Date/Timestamp (Julian calendar before 1582) is stored by H2's proleptic
     * calendar as 0000-12-30. Both spellings are rendered as the placeholder 0001-01-01 so that a modern implementation that writes 0001-01-01
     * directly (java.time) is not reported different from the legacy for a calendar artefact (documented in the README).
     */
    /** Token that replaces the JDK "helpful NullPointerException" text. */
    public static final String NPE_TOKEN = "<NPE-MSG>";

    /**
     * JDK helpful NullPointerException text ({@code Cannot invoke "String.equals(Object)" because "<local33>.X" is null}): it names compiler locals
     * and bytecode slots of the legacy class, so it cannot be produced by the modern code and is not a contract. Same environment variance as
     * the dates. Applied to every string of the golden "salida" and, in {@link Canon}, to both sides of the comparison.
     */
    public static final Pattern NPE_MSG = Pattern.compile(
            "Cannot (?:invoke|read|load from|store to|assign field|throw exception|enter synchronized block|exit synchronized block)\\b[^\\n]*? is null");

    public static String sinMensajeNpe(String texto) {
        return texto == null ? null : NPE_MSG.matcher(texto).replaceAll(NPE_TOKEN);
    }

    public static JsonElement sinMensajeNpe(JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return e;
        }
        if (e.isJsonPrimitive()) {
            return e.getAsJsonPrimitive().isString() ? new JsonPrimitive(sinMensajeNpe(e.getAsString())) : e;
        }
        if (e.isJsonArray()) {
            JsonArray a = new JsonArray();
            e.getAsJsonArray().forEach(x -> a.add(sinMensajeNpe(x)));
            return a;
        }
        JsonObject o = new JsonObject();
        e.getAsJsonObject().entrySet().forEach(en -> o.add(en.getKey(), sinMensajeNpe(en.getValue())));
        return o;
    }

    static String placeholder(String iso) {
        return iso != null && iso.startsWith("0000-12-30") ? "0001-01-01" + iso.substring(10) : iso;
    }

    public static Set<String> excluir(JsonObject spec) {
        return lista(spec, "excluir");
    }

    public static Set<String> enmascarar(JsonObject spec) {
        return lista(spec, "enmascarar");
    }

    private static Set<String> lista(JsonObject spec, String clave) {
        Set<String> s = new HashSet<>();
        if (spec.has(clave)) {
            for (JsonElement e : spec.getAsJsonArray(clave)) {
                s.add(e.getAsString());
            }
        }
        return s;
    }

    /** Convenience: dump as declared by a case "tablas" entry. */
    public static JsonArray tabla(Connection conn, JsonObject spec) throws SQLException {
        return tabla(conn, spec.get("nombre").getAsString(), spec.get("orden").getAsString(), excluir(spec), columnas(spec), enmascarar(spec));
    }

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

    public static JsonArray tabla(Connection conn, String table, String orderBy, Set<String> exclude, String columnas, Set<String> enmascarar)
            throws SQLException {
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
                        String t = placeholder(rs.getString(i));
                        row.addProperty(col, enmascarar.contains(col) ? enmascararInstante(t) : t);
                    } else {
                        String t = v.toString();
                        row.addProperty(col, enmascarar.contains(col) ? enmascararInstante(t) : t);
                    }
                }
                rows.add(row);
            }
        }
        return rows;
    }

    /**
     * Masks, inside a collaborator call log, every string value of the listed keys (at any depth) that looks like a date or timestamp:
     * {@code "enmascararColaboradores": ["FechaCreacion", ...]} of case.json. The legacy builds those values from the wall clock
     * ({@code new java.sql.Date(System.currentTimeMillis())} in GrabarContratoSAAPV); the golden keeps the key and shows that it was set.
     */
    public static JsonElement enmascararClaves(JsonElement e, Set<String> claves) {
        if (claves.isEmpty() || e == null) {
            return e;
        }
        if (e.isJsonArray()) {
            JsonArray out = new JsonArray();
            e.getAsJsonArray().forEach(x -> out.add(enmascararClaves(x, claves)));
            return out;
        }
        if (e.isJsonObject()) {
            JsonObject out = new JsonObject();
            for (var en : e.getAsJsonObject().entrySet()) {
                JsonElement v = en.getValue();
                if (claves.contains(en.getKey()) && v.isJsonPrimitive() && v.getAsJsonPrimitive().isString()) {
                    out.addProperty(en.getKey(), TIMESTAMP.matcher(v.getAsString()).replaceAll("<TS>"));
                } else {
                    out.add(en.getKey(), enmascararClaves(v, claves));
                }
            }
            return out;
        }
        return e;
    }
}
