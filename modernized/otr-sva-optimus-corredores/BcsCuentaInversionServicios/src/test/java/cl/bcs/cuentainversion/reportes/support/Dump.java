package cl.bcs.cuentainversion.reportes.support;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Table snapshots shared by the legacy oracle (which records them) and the modern tests (which recompute them), so both
 * sides serialise database state the same way.
 *
 * <p>Rules (documented in the README):
 * <ul>
 *   <li>Every BASE TABLE of schema PUBLIC is dumped, rows ordered by all columns. Temporary tables are not dumped:
 *       how a staging area is implemented is not behaviour.</li>
 *   <li>Numbers by value (scale is an H2 column property, not behaviour); dates as yyyy-MM-dd; timestamps as
 *       yyyy-MM-dd HH:mm:ss; a timestamp whose date is today (local or UTC) becomes {@code <NOW>} because the legacy SQL
 *       stamps CURRENT_UTCTIMESTAMP.</li>
 *   <li>Columns in {@link #MASCARA_ALEATORIA} (IdTrabajo: legacy uses Math.random()) become {@code <random>}.</li>
 *   <li>The recorded result is only the tables that CHANGED between the seeded state and the end of the call.</li>
 * </ul>
 */
public final class Dump {

    public static final Set<String> MASCARA_ALEATORIA = Set.of("IdTrabajo");

    private Dump() {
    }

    public static JsonObject snapshot(Connection c) throws Exception {
        JsonObject out = new JsonObject();
        List<String> tablas = new ArrayList<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' "
                     + "AND TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_NAME")) {
            while (rs.next()) {
                tablas.add(rs.getString(1));
            }
        }
        for (String t : tablas) {
            out.add(t, tabla(c, t));
        }
        return out;
    }

    private static JsonArray tabla(Connection c, String t) throws Exception {
        JsonArray rows = new JsonArray();
        java.util.List<JsonObject> lista = new java.util.ArrayList<>();
        try (Statement st = c.createStatement()) {
            int n;
            try (ResultSet probe = st.executeQuery("SELECT * FROM \"" + t + "\" WHERE 1 = 0")) {
                n = probe.getMetaData().getColumnCount();
            }
            StringBuilder ord = new StringBuilder();
            for (int i = 1; i <= n; i++) {
                ord.append(i > 1 ? "," : "").append(i);
            }
            try (ResultSet rs = st.executeQuery("SELECT * FROM \"" + t + "\" ORDER BY " + ord)) {
                ResultSetMetaData md = rs.getMetaData();
                while (rs.next()) {
                    JsonObject row = new JsonObject();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        String col = md.getColumnLabel(i);
                        int tipo = md.getColumnType(i);
                        Object v;
                        if (tipo == java.sql.Types.DATE) {
                            v = rs.getObject(i, java.time.LocalDate.class);
                        } else if (tipo == java.sql.Types.TIMESTAMP) {
                            v = rs.getObject(i, java.time.LocalDateTime.class);
                        } else {
                            v = rs.getObject(i);
                        }
                        row.add(col, valor(col, v));
                    }
                    lista.add(row);
                }
            }
        }
        // order by the MASKED content: random work-table ids (IdTrabajo) must not decide the order of the rows
        lista.sort(java.util.Comparator.comparing(JsonObject::toString));
        lista.forEach(rows::add);
        return rows;
    }

    static JsonElement valor(String col, Object v) {
        if (v == null) {
            return JsonNull.INSTANCE;
        }
        if (MASCARA_ALEATORIA.contains(col)) {
            return new JsonPrimitive("<random>");
        }
        if (v instanceof Number n) {
            return new JsonPrimitive(new BigDecimal(new BigDecimal(n.toString()).stripTrailingZeros().toPlainString()));
        }
        if (v instanceof byte[] bytes) {
            return new JsonPrimitive("0x" + java.util.HexFormat.of().formatHex(bytes));
        }
        if (v instanceof Boolean b) {
            return new JsonPrimitive(b);
        }
        if (v instanceof LocalDate d) {
            return new JsonPrimitive(d.toString());
        }
        if (v instanceof java.time.LocalDateTime ts) {
            LocalDate d = ts.toLocalDate();
            if (d.equals(LocalDate.now()) || d.equals(LocalDate.now(ZoneOffset.UTC))) {
                return new JsonPrimitive("<NOW>");
            }
            String s = ts.toString().replace('T', ' ');
            return new JsonPrimitive(s.length() == 16 ? s + ":00" : s.length() > 19 ? s.substring(0, 19) : s);
        }
        return new JsonPrimitive(v.toString());
    }

    /** Tables whose rows differ between two snapshots, with their rows in {@code after}. */
    public static JsonObject cambios(JsonObject before, JsonObject after) {
        JsonObject out = new JsonObject();
        for (String t : after.keySet()) {
            JsonElement b = before.get(t);
            if (b == null || !b.equals(after.get(t))) {
                out.add(t, after.get(t));
            }
        }
        return out;
    }
}
