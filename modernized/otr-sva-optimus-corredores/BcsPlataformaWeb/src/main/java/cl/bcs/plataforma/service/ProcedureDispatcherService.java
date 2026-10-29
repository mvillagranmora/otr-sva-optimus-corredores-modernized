package cl.bcs.plataforma.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dispatches P-type services — calls HANA stored procedures via JDBC.
 * Introspects SYS.PROCEDURE_PARAMETERS once per procedure name (cached).
 * Source: CallServlet StoredProcedure.call() pattern.
 */
@Service
public class ProcedureDispatcherService implements cl.bcs.plataforma.login.ProcedimientoAlmacenado {

    private static final Logger log = LoggerFactory.getLogger(ProcedureDispatcherService.class);

    private static final String SQL_PARAMS =
        "SELECT PARAMETER_NAME, DATA_TYPE_NAME, PARAMETER_TYPE, POSITION" +
        " FROM SYS.PROCEDURE_PARAMETERS" +
        " WHERE SCHEMA_NAME = 'BSCL' AND PROCEDURE_NAME = :procName" +
        " ORDER BY POSITION";

    private record SpParam(String name, String dataType, String paramType, int position) {
        boolean isIn()    { return "IN".equals(paramType) || "INOUT".equals(paramType); }
        boolean isOut()   { return "OUT".equals(paramType) || "INOUT".equals(paramType); }
        boolean isTable() { return "TABLE".equalsIgnoreCase(dataType) || "TABLE_TYPE".equalsIgnoreCase(dataType); }   // HANA QA reports TABLE_TYPE
    }

    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate jdbc;
    private final ConcurrentHashMap<String, List<SpParam>> paramCache = new ConcurrentHashMap<>();

    public ProcedureDispatcherService(DataSource dataSource, NamedParameterJdbcTemplate jdbc) {
        this.dataSource = dataSource;
        this.jdbc = jdbc;
    }

    /**
     * Calls the HANA SP identified by rutaServicio.
     * Returns the full OUT-parameter JSON object.
     *
     * TABLE-type IN parameters are supported: the JSON body must supply the data
     * as a JSON array under the matching parameter name. Each element is a row object
     * whose keys are the column names. The driver receives a PreparedStatement ResultSet
     * via setObject() — the HANA ngdbc driver maps this to a TABLE_IN parameter.
     */
    public JsonObject dispatch(String rutaServicio, JsonObject params,
                               long idEmpresa, long idUsuario) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            return llamar(conn, rutaServicio, params);
        }
    }

    /**
     * The same call on the connection (and so the transaction) of the caller (T-22): the login and password-recovery services need the
     * procedure to take part in their single transaction. The connection is neither committed nor closed here.
     */
    @Override
    public JsonObject llamar(Connection conn, String rutaServicio, JsonObject params) throws Exception {
        List<SpParam> spParams = paramCache.computeIfAbsent(rutaServicio, this::introspect);

        // A TABLE IN parameter reaches HANA as a local temporary table filled with the rows (what the legacy helper did): the call names the table in
        // place of the "?" of that position. The tables live in the session and are dropped when the call ends.
        Map<Integer, String> tablasTemporales = new java.util.HashMap<>();
        try {
            for (SpParam p : spParams) {
                if (p.isIn() && p.isTable()) {
                    String jsonKey = findKey(params, p.name());
                    JsonElement val = jsonKey != null ? params.get(jsonKey) : null;
                    tablasTemporales.put(p.position(), crearTablaTemporal(conn, rutaServicio, p, val));
                }
            }
            return ejecutar(conn, rutaServicio, params, spParams, tablasTemporales);
        } finally {
            for (String t : tablasTemporales.values()) {
                try (Statement st = conn.createStatement()) {
                    st.execute("DROP TABLE " + t);
                } catch (SQLException ignored) {
                    // the table disappears with the session anyway
                }
            }
        }
    }

    private JsonObject ejecutar(Connection conn, String rutaServicio, JsonObject params, List<SpParam> spParams,
                                Map<Integer, String> tablasTemporales) throws Exception {
        String callSql = buildCallSql(rutaServicio, spParams, tablasTemporales);
        Map<Integer, Integer> indice = new java.util.HashMap<>();   // position of the parameter -> index of its "?"
        int n = 0;
        for (SpParam sp : spParams) {
            if (!tablasTemporales.containsKey(sp.position())) {
                indice.put(sp.position(), ++n);
            }
        }

        try (CallableStatement cs = conn.prepareCall(callSql)) {

            // Bind IN params from request body (case-insensitive name match)
            for (SpParam p : spParams) {
                if (!p.isIn() || p.isTable()) continue;
                String jsonKey = findKey(params, p.name());
                JsonElement val = jsonKey != null ? params.get(jsonKey) : null;
                if (val == null || val.isJsonNull()) {
                    cs.setNull(indice.get(p.position()), jdbcType(p.dataType()));
                } else {
                    cs.setObject(indice.get(p.position()), coerce(val, p.dataType()), jdbcType(p.dataType()));
                }
            }

            // Register OUT params
            for (SpParam p : spParams) {
                if (p.isOut()) {
                    cs.registerOutParameter(indice.get(p.position()),
                        p.isTable() ? Types.REF_CURSOR : jdbcType(p.dataType()));   // as the legacy: a table OUT is a cursor
                }
            }

            cs.execute();

            // Collect OUT values
            // As the legacy StoredProcedure: the scalar OUT values are read from the statement and each table OUT comes as a result set, in the
            // order of the parameters (the first with getResultSet, the next ones after getMoreResults)
            JsonObject result = new JsonObject();
            for (SpParam p : spParams) {
                if (p.isOut() && !p.isTable()) {
                    result.addProperty(p.name(), cs.getString(indice.get(p.position())));
                }
            }
            boolean primera = true;
            for (SpParam p : spParams) {
                if (!p.isOut() || !p.isTable()) continue;
                if (!primera) {
                    cs.getMoreResults();
                }
                primera = false;
                ResultSet rs = cs.getResultSet();
                result.add(p.name(), rs != null ? resultSetToArray(rs) : new JsonArray());
            }
            return result;

        }
    }

    /** Column definition of a table parameter, from the catalog. */
    private record Columna(String nombre, String tipo, int largo, int escala) {
    }

    private final ConcurrentHashMap<String, List<Columna>> columnasCache = new ConcurrentHashMap<>();

    private List<Columna> columnas(String proc, String parametro) {
        return columnasCache.computeIfAbsent(proc + "|" + parametro, k -> {
            var ps = new MapSqlParameterSource().addValue("p", proc).addValue("n", parametro);
            List<Columna> out = new ArrayList<>();
            for (Map<String, Object> r : jdbc.queryForList(
                    "SELECT COLUMN_NAME, DATA_TYPE_NAME, LENGTH, SCALE FROM SYS.PROCEDURE_PARAMETER_COLUMNS"
                            + " WHERE SCHEMA_NAME = 'BSCL' AND PROCEDURE_NAME = :p AND PARAMETER_NAME = :n ORDER BY POSITION", ps)) {
                out.add(new Columna(str(r, "COLUMN_NAME"), str(r, "DATA_TYPE_NAME"),
                        r.get("LENGTH") == null ? 0 : ((Number) r.get("LENGTH")).intValue(),
                        r.get("SCALE") == null ? 0 : ((Number) r.get("SCALE")).intValue()));
            }
            return out;
        });
    }

    private static String tipoSql(Columna c) {
        return switch (c.tipo()) {
            case "NVARCHAR", "VARCHAR", "NCHAR", "CHAR", "VARBINARY" -> c.tipo() + "(" + c.largo() + ")";
            case "DECIMAL" -> "DECIMAL(" + c.largo() + "," + c.escala() + ")";
            default -> c.tipo();   // BIGINT, INTEGER, SECONDDATE, TIMESTAMP, DATE, BLOB...
        };
    }

    /**
     * Creates a local temporary table with the columns of the table parameter and fills it with the JSON rows (an array of objects keyed by column
     * name; a missing key is NULL). Returns the name of the table.
     */
    private String crearTablaTemporal(Connection conn, String proc, SpParam p, JsonElement filas) throws SQLException {
        List<Columna> cols = columnas(proc, p.name());
        if (cols.isEmpty()) {
            throw new SQLException("no columns found for the table parameter " + p.name() + " of " + proc);
        }
        String nombre = "#BCS_" + p.name().replaceAll("[^A-Za-z0-9_]", "_") + "_" + Long.toString(System.nanoTime() % 1_000_000_000L);
        StringBuilder ddl = new StringBuilder("CREATE LOCAL TEMPORARY TABLE ").append(nombre).append(" (");
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) ddl.append(", ");
            ddl.append('"').append(cols.get(i).nombre()).append("\" ").append(tipoSql(cols.get(i)));
        }
        ddl.append(')');
        try (Statement st = conn.createStatement()) {
            st.execute(ddl.toString());
        }
        JsonArray rows = (filas != null && filas.isJsonArray()) ? filas.getAsJsonArray() : new JsonArray();
        if (rows.isEmpty()) {
            return nombre;
        }
        StringBuilder ins = new StringBuilder("INSERT INTO ").append(nombre).append(" VALUES (");
        for (int i = 0; i < cols.size(); i++) {
            ins.append(i > 0 ? ",?" : "?");
        }
        ins.append(')');
        try (PreparedStatement st = conn.prepareStatement(ins.toString())) {
            for (JsonElement fila : rows) {
                JsonObject o = fila.getAsJsonObject();
                for (int i = 0; i < cols.size(); i++) {
                    String key = findKey(o, cols.get(i).nombre());
                    enlazar(st, i + 1, cols.get(i), key == null ? null : o.get(key));
                }
                st.addBatch();
            }
            st.executeBatch();
        }
        return nombre;
    }

    private static void enlazar(PreparedStatement st, int pos, Columna c, JsonElement v) throws SQLException {
        if (v == null || v.isJsonNull()) {
            st.setNull(pos, tipoJdbcColumna(c.tipo()));
            return;
        }
        String s = v.getAsString();
        switch (c.tipo()) {
            case "BIGINT" -> st.setLong(pos, Long.parseLong(s));
            case "INTEGER", "SMALLINT", "TINYINT" -> st.setInt(pos, Integer.parseInt(s));
            case "DECIMAL" -> st.setBigDecimal(pos, new java.math.BigDecimal(s));
            case "SECONDDATE", "TIMESTAMP" -> st.setTimestamp(pos, aTimestamp(s));
            case "DATE" -> st.setDate(pos, java.sql.Date.valueOf(s.length() > 10 ? s.substring(0, 10) : s));
            case "BLOB", "VARBINARY" -> st.setBytes(pos, java.util.Base64.getDecoder().decode(s));
            default -> st.setString(pos, s);
        }
    }

    private static Timestamp aTimestamp(String s) {
        String t = s.trim().replace('T', ' ');
        if (t.length() == 10) {
            t = t + " 00:00:00";
        }
        return Timestamp.valueOf(t);
    }

    private static int tipoJdbcColumna(String tipo) {
        return switch (tipo) {
            case "BIGINT" -> Types.BIGINT;
            case "INTEGER", "SMALLINT", "TINYINT" -> Types.INTEGER;
            case "DECIMAL" -> Types.DECIMAL;
            case "SECONDDATE", "TIMESTAMP" -> Types.TIMESTAMP;
            case "DATE" -> Types.DATE;
            case "BLOB", "VARBINARY" -> Types.VARBINARY;
            default -> Types.NVARCHAR;
        };
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private List<SpParam> introspect(String procName) {
        var ps = new MapSqlParameterSource().addValue("procName", procName);
        List<Map<String, Object>> rows = jdbc.queryForList(SQL_PARAMS, ps);
        List<SpParam> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            list.add(new SpParam(
                str(r, "PARAMETER_NAME"),
                str(r, "DATA_TYPE_NAME"),
                str(r, "PARAMETER_TYPE"),
                ((Number) r.get("POSITION")).intValue()
            ));
        }
        if (list.isEmpty()) {
            log.warn("No SYS.PROCEDURE_PARAMETERS found for '{}'. Check HANA XS artifact name.", procName);
        }
        return list;
    }

    private static String buildCallSql(String procName, List<SpParam> spParams, Map<Integer, String> tablasTemporales) {
        StringBuilder sb = new StringBuilder("{CALL BSCL.\"").append(procName).append("\"(");
        for (int i = 0; i < spParams.size(); i++) {
            if (i > 0) sb.append(',');
            String t = tablasTemporales.get(spParams.get(i).position());
            sb.append(t != null ? t : "?");
        }
        return sb.append(")}").toString();
    }

    private static String findKey(JsonObject obj, String paramName) {
        if (obj.has(paramName)) return paramName;
        for (String key : obj.keySet()) {
            if (key.equalsIgnoreCase(paramName)) return key;
        }
        return null;
    }

    private static Object coerce(JsonElement el, String dataType) {
        if (el.isJsonNull()) return null;
        String s = el.getAsString();
        return switch (dataType) {
            case "INTEGER", "INT"  -> Long.parseLong(s);
            case "BIGINT"          -> Long.parseLong(s);
            case "DECIMAL", "NUMERIC" -> new java.math.BigDecimal(s);
            case "BOOLEAN"         -> Boolean.parseBoolean(s);
            default                -> s;
        };
    }

    private static int jdbcType(String dataType) {
        return switch (dataType) {
            case "INTEGER", "INT"  -> Types.INTEGER;
            case "BIGINT"          -> Types.BIGINT;
            case "DECIMAL", "NUMERIC" -> Types.DECIMAL;
            case "BOOLEAN"         -> Types.BOOLEAN;
            case "DATE"            -> Types.DATE;
            case "TIMESTAMP"       -> Types.TIMESTAMP;
            case "TABLE"           -> Types.OTHER;
            default                -> Types.NVARCHAR;
        };
    }

    private static JsonArray resultSetToArray(ResultSet rs) throws SQLException {
        JsonArray arr = new JsonArray();
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        while (rs.next()) {
            JsonObject row = new JsonObject();
            for (int i = 1; i <= cols; i++) {
                String col = meta.getColumnLabel(i);
                String val = rs.getString(i);
                if (val == null) row.addProperty(col, (String) null);
                else row.addProperty(col, val);
            }
            arr.add(row);
        }
        return arr;
    }

    private static String str(Map<String, Object> row, String col) {
        Object v = row.get(col);
        return v == null ? "" : v.toString();
    }
}
