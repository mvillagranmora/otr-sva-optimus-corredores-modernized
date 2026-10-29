package cl.bcs.plataforma.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dispatches Q-type services — executes OData-style SELECTs against HANA views.
 * Source: CallServlet + OdataImpSql.QueryJsonObject() pattern.
 *
 * OData params supported: $select, $filter, $orderby, $top, $skip, $inlinecount=allpages.
 * $filter operators: eq, ne, lt, le, gt, ge, and, or,
 *                    startswith(f,'v'), endswith(f,'v'), substringof('v',f).
 */
@Service
public class QueryDispatcherService {

    private final DataSource dataSource;

    public QueryDispatcherService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public record QueryResult(JsonArray rows, long totalCount) {}

    /**
     * Executes the OData query against HANA view BSCL."<rutaServicio>".
     *
     * @param rutaServicio  PLA.Servicio.RutaServicio — HANA view name (without schema)
     * @param select        OData $select (comma-separated columns, or null for *)
     * @param filter        OData $filter expression (or null)
     * @param orderby       OData $orderby (or null)
     * @param top           OData $top (0 = no limit)
     * @param skip          OData $skip (0 = no offset)
     * @param inlineCount   true → also return total count before paging
     */
    public QueryResult dispatch(String rutaServicio,
                                String select, String filter, String orderby,
                                long top, long skip, boolean inlineCount) throws Exception {
        String viewRef = "BSCL.\"" + rutaServicio + "\"";

        String whereSql = filter != null && !filter.isBlank() ? translateFilter(filter) : null;

        long totalCount = 0L;
        if (inlineCount) {
            String countSql = "SELECT COUNT(*) FROM " + viewRef +
                (whereSql != null ? " WHERE " + whereSql : "");
            try (Connection conn = dataSource.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(countSql)) {
                if (rs.next()) totalCount = rs.getLong(1);
            }
        }

        String cols = (select != null && !select.isBlank()) ? quoteColumns(select) : "*";
        StringBuilder sb = new StringBuilder("SELECT ");
        if (top > 0 && skip == 0) sb.append("TOP ").append(top).append(' ');
        sb.append(cols).append(" FROM ").append(viewRef);
        if (whereSql != null) sb.append(" WHERE ").append(whereSql);
        if (orderby != null && !orderby.isBlank()) sb.append(" ORDER BY ").append(translateOrderBy(orderby));
        if (top > 0 && skip > 0) sb.append(" LIMIT ").append(top).append(" OFFSET ").append(skip);
        else if (skip > 0)        sb.append(" OFFSET ").append(skip);

        JsonArray rows = new JsonArray();
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sb.toString())) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols2 = meta.getColumnCount();
            while (rs.next()) {
                JsonObject row = new JsonObject();
                for (int i = 1; i <= cols2; i++) {
                    String colName = meta.getColumnLabel(i);
                    String val = rs.getString(i);
                    if (val == null) row.addProperty(colName, (String) null);
                    else row.addProperty(colName, val);
                }
                rows.add(row);
            }
        }
        return new QueryResult(rows, inlineCount ? totalCount : rows.size());
    }

    // ─── OData filter → HANA SQL WHERE ───────────────────────────────────────

    // Handles: eq ne lt le gt ge and or not, startswith endswith substringof
    static String translateFilter(String odata) {
        if (odata == null || odata.isBlank()) return null;
        String s = odata.trim();
        s = translateFunctions(s);
        s = translateComparisons(s);
        s = translateLogical(s);
        return s;
    }

    private static final Pattern STARTSWITH = Pattern.compile(
        "startswith\\(([^,]+),\\s*'([^']*)'\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ENDSWITH = Pattern.compile(
        "endswith\\(([^,]+),\\s*'([^']*)'\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUBSTRINGOF = Pattern.compile(
        "substringof\\('([^']*)',\\s*([^)]+)\\)", Pattern.CASE_INSENSITIVE);

    private static String translateFunctions(String s) {
        Matcher m;
        m = STARTSWITH.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, quoteCol(m.group(1).trim()) + " LIKE '" + m.group(2) + "%'");
        }
        m.appendTail(sb);
        s = sb.toString();

        m = ENDSWITH.matcher(s);
        sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, quoteCol(m.group(1).trim()) + " LIKE '%" + m.group(2) + "'");
        }
        m.appendTail(sb);
        s = sb.toString();

        m = SUBSTRINGOF.matcher(s);
        sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, quoteCol(m.group(2).trim()) + " LIKE '%" + m.group(1) + "%'");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static final Pattern CMP = Pattern.compile(
        "(\\w+)\\s+(eq|ne|lt|le|gt|ge)\\s+('(?:[^']*)'|[\\d.]+|true|false|null)",
        Pattern.CASE_INSENSITIVE);

    private static String translateComparisons(String s) {
        Matcher m = CMP.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String col = quoteCol(m.group(1));
            String op  = switch (m.group(2).toLowerCase()) {
                case "eq" -> "=";
                case "ne" -> "<>";
                case "lt" -> "<";
                case "le" -> "<=";
                case "gt" -> ">";
                case "ge" -> ">=";
                default   -> "=";
            };
            String val = m.group(3);
            if ("null".equalsIgnoreCase(val)) {
                m.appendReplacement(sb, col + " IS NULL");
            } else {
                m.appendReplacement(sb, col + " " + op + " " + val);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String translateLogical(String s) {
        return s.replaceAll("(?i)\\band\\b", "AND")
                .replaceAll("(?i)\\bor\\b",  "OR")
                .replaceAll("(?i)\\bnot\\b", "NOT");
    }

    private static String quoteCol(String col) {
        return "\"" + col.trim() + "\"";
    }

    private static String quoteColumns(String select) {
        String[] parts = select.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(quoteCol(parts[i]));
        }
        return sb.toString();
    }

    private static String translateOrderBy(String orderby) {
        // "field asc,field2 desc" → "\"field\" ASC,\"field2\" DESC"
        String[] parts = orderby.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(',');
            String[] tokens = parts[i].trim().split("\\s+");
            sb.append(quoteCol(tokens[0]));
            if (tokens.length > 1) sb.append(' ').append(tokens[1].toUpperCase());
        }
        return sb.toString();
    }
}
