package cl.bcs.tesoreria.jobs.support;

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
 * Table dump shared by the legacy oracle runner and the modern tests, so both sides serialise rows identically: numbers
 * normalised ({@code stripTrailingZeros().toPlainString()}), everything else {@code toString()}, SQL NULL as JSON null. The
 * case declares what to dump: {@code "tablas": [{"nombre": "<HANA table>", "orden": "\"Col\", ...", "excluir": ["Col"],
 * "enmascarar": ["Col"]}]}; a masked column has every timestamp-looking value replaced by {@code <TS>} (wall-clock values).
 */
public final class Dump {

    /** yyyy-MM-dd with an optional [ T]HH:mm:ss[.fraction]. */
    public static final java.util.regex.Pattern TIMESTAMP = java.util.regex.Pattern.compile("\\d{4}-\\d{2}-\\d{2}([ T]\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?)?");

    private Dump() {
    }

    private static Set<String> lista(JsonObject spec, String k) {
        Set<String> s = new HashSet<>();
        if (spec.has(k)) {
            for (JsonElement e : spec.getAsJsonArray(k)) {
                s.add(e.getAsString());
            }
        }
        return s;
    }

    public static JsonArray tabla(Connection conn, JsonObject spec) throws SQLException {
        Set<String> exclude = lista(spec, "excluir");
        Set<String> mask = lista(spec, "enmascarar");
        JsonArray rows = new JsonArray();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM \"" + spec.get("nombre").getAsString() + "\" ORDER BY " + spec.get("orden").getAsString())) {
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
                    } else {
                        String t = v instanceof java.util.Date ? rs.getString(i) : v.toString();
                        row.addProperty(col, mask.contains(col) ? TIMESTAMP.matcher(t).replaceAll("<TS>") : t);
                    }
                }
                rows.add(row);
            }
        }
        return rows;
    }
}
