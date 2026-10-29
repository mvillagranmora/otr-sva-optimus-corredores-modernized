package cl.bcs.cuentainversion.transaccional.support;

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
        return tabla(conn, spec.get("nombre").getAsString(), spec.get("orden").getAsString(), excluir(spec), columnas(spec));
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

    public static JsonArray tabla(Connection conn, String table, String orderBy, Set<String> exclude) throws SQLException {
        return tabla(conn, table, orderBy, exclude, "*");
    }

    public static JsonArray tabla(Connection conn, String table, String orderBy, Set<String> exclude, String columnas) throws SQLException {
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
                        row.addProperty(col, rs.getString(i));
                    } else {
                        row.addProperty(col, v.toString());
                    }
                }
                rows.add(row);
            }
        }
        return rows;
    }
}
