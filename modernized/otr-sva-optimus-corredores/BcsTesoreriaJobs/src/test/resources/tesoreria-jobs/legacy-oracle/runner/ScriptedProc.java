import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.oracle.Hooks;

/**
 * Scripted HANA stored procedure: a java.sql.CallableStatement proxy that answers "colaboradores.procedimiento.resultados"
 * ([{columnas:[..], filas:[[..]]}], every value read as text) and records one "StoredProcedure" collaborator call per execute():
 * {llamada, parametros:[{tipo: long|string, valor}]}. "colaboradores.procedimiento.lanza" scripts a failure of execute().
 * No result set at all: execute() answers false (the HANA procedure returned no result set).
 */
final class ScriptedProc {

    private ScriptedProc() {
    }

    static CallableStatement crear(String llamada, Connection h2) {
        TreeMap<Integer, Object> params = new TreeMap<>();
        JsonArray res = Hooks.objeto("procedimiento").has("resultados") ? Hooks.objeto("procedimiento").getAsJsonArray("resultados") : new JsonArray();
        int[] idx = {0};
        ResultSet[] actual = {null};
        return (CallableStatement) Proxy.newProxyInstance(ScriptedProc.class.getClassLoader(), new Class<?>[]{CallableStatement.class}, (p, m, a) -> {
            switch (m.getName()) {
                case "setLong" -> { params.put((Integer) a[0], a[1]); return null; }
                case "setString" -> { params.put((Integer) a[0], a[1]); return null; }
                case "execute" -> {
                    JsonObject o = new JsonObject();
                    o.addProperty("llamada", llamada);
                    JsonArray ps = new JsonArray();
                    for (Object v : params.values()) {
                        JsonObject x = new JsonObject();
                        x.addProperty("tipo", v instanceof Long ? "long" : "string");
                        if (v instanceof Long l) x.addProperty("valor", l); else x.addProperty("valor", (String) v);
                        ps.add(x);
                    }
                    o.add("parametros", ps);
                    Hooks.registrarJson("StoredProcedure", o);
                    String lanza = Hooks.texto("procedimiento.lanza", null);
                    if (lanza != null) throw new SQLException(lanza);
                    idx[0] = 0;
                    return !res.isEmpty();
                }
                case "getResultSet" -> {
                    actual[0] = consulta(h2, res.get(idx[0]).getAsJsonObject());
                    return actual[0];
                }
                case "getMoreResults" -> { idx[0]++; return idx[0] < res.size(); }
                case "close" -> { return null; }
                default -> throw new UnsupportedOperationException("ScriptedProc." + m.getName());
            }
        });
    }

    private static ResultSet consulta(Connection h2, JsonObject rs) throws SQLException {
        JsonArray cols = rs.getAsJsonArray("columnas");
        JsonArray filas = rs.getAsJsonArray("filas");
        StringBuilder sql = new StringBuilder("SELECT * FROM (VALUES ");
        List<String> rows = new ArrayList<>();
        for (JsonElement f : filas) rows.add(fila(f.getAsJsonArray(), cols.size()));
        if (rows.isEmpty()) rows.add(fila(null, cols.size()));
        sql.append(String.join(",", rows)).append(") AS T(");
        for (int i = 0; i < cols.size(); i++) sql.append(i > 0 ? "," : "").append('"').append(cols.get(i).getAsString()).append('"');
        sql.append(")");
        if (filas.isEmpty()) sql.append(" WHERE 1 = 0");
        Statement st = h2.createStatement();
        return st.executeQuery(sql.toString());
    }

    private static String fila(JsonArray f, int n) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < n; i++) {
            JsonElement v = f == null ? null : f.get(i);
            sb.append(i > 0 ? "," : "");
            if (v == null || v.isJsonNull()) sb.append("CAST(NULL AS VARCHAR)");
            else sb.append("CAST('").append(v.getAsString().replace("'", "''")).append("' AS VARCHAR)");
        }
        return sb.append(")").toString();
    }
}
