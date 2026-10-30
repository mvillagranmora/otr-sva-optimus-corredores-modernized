package cl.bcs.replica.oraculo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/** Dump of the tables named by a case (one source file, byte for byte in the oracle and in the JUnit tests): numbers normalised, dates as ISO text, NULL as JSON null. Java 11 syntax. */
public final class VolcadoTablas {
    private VolcadoTablas() {
    }

    /** @param tablas [{"nombre": "BSCL.Plataforma.BD::PLA.Replica.WorkStack", "orden": "\"IdStack\"", "columnas": ["IdStack", "Key"]}] ("columnas" optional: all; null tablas: nothing) */
    public static JsonObject volcar(Connection conn, JsonArray tablas) throws Exception {
        JsonObject out = new JsonObject();
        if (tablas == null) return out;
        for (JsonElement el : tablas) {
            JsonObject t = el.getAsJsonObject();
            String cols = "*";
            if (t.has("columnas")) {
                StringBuilder sb = new StringBuilder();
                for (JsonElement c : t.getAsJsonArray("columnas")) sb.append(sb.length() > 0 ? ", " : "").append('"').append(c.getAsString()).append('"');
                cols = sb.toString();
            }
            out.add(t.get("nombre").getAsString(), filas(conn, t.get("nombre").getAsString(), cols, t.get("orden").getAsString()));
        }
        return out;
    }

    static JsonArray filas(Connection conn, String tabla, String columnas, String orden) throws Exception {
        JsonArray rows = new JsonArray();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT " + columnas + " FROM \"" + tabla + "\" ORDER BY " + orden)) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                JsonObject row = new JsonObject();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    Object v = rs.getObject(i);
                    String col = md.getColumnLabel(i);
                    if (v == null) row.add(col, JsonNull.INSTANCE);
                    else if (v instanceof Number) row.add(col, new JsonPrimitive(new BigDecimal(new BigDecimal(v.toString()).stripTrailingZeros().toPlainString())));
                    else if (v instanceof java.util.Date || v instanceof java.time.temporal.TemporalAccessor) row.addProperty(col, rs.getString(i));
                    else row.addProperty(col, v.toString());
                }
                rows.add(row);
            }
        }
        return rows;
    }
}
