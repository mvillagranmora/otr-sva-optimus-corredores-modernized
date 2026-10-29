package cl.bcs.cuentainversion.reportes.support;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HANA to H2 SQL adapter shared by the legacy oracle (legacy-oracle/shims/cl/bcs/utils/SQL.java) and by the
 * modern tests (the DataSource handed to the modern services is wrapped with it), so that BOTH implementations run the
 * SAME HANA SQL text on the SAME H2 engine. Without it neither side could execute the production SQL resources.
 *
 * <p>Only syntax that H2 cannot parse is rewritten. Everything else is left untouched; HANA functions are provided as
 * H2 aliases by {@link H2Functions}.
 * <ul>
 *   <li>{@code UPSERT tbl (c1,c2..) VALUES (..) WHERE c1 = :a AND c2 = :b} becomes
 *       {@code MERGE INTO tbl (c1,c2..) KEY (c1,c2) VALUES (..)}. HANA UPSERT with WHERE updates the row matching the
 *       WHERE or inserts it; the WHERE columns are the key.</li>
 *   <li>{@code CURRENT_UTCTIMESTAMP} / {@code CURRENT_UTCDATE} (HANA keywords) get call parentheses so they resolve to the
 *       H2 aliases of {@link H2Functions}.</li>
 *   <li>{@code LOCAL TEMPORARY COLUMN TABLE} (HANA column store) becomes {@code LOCAL TEMPORARY TABLE}.</li>
 *   <li>a bare {@code DECIMAL} column type (HANA: floating decimal) becomes {@code DECIMAL(34,16)}; H2 would silently use scale 0.</li>
 *   <li>{@code TO_DECIMAL(x,p,s)} becomes {@code CAST(x AS DECIMAL(p,s))}; {@code MAP(a,b,c,d)} becomes a simple CASE.</li>
 *   <li>a bare parameter in a select list ({@code :p AS "Col"}) is wrapped in CAST(.. AS VARCHAR(100)) because H2 cannot type it.</li>
 *   <li>HANA local temporary tables {@code #Name} are rewritten to the quoted identifier {@code "#Name"}.</li>
 * </ul>
 * Source of the semantics: SAP HANA SQL reference (UPSERT / REPLACE) and the SQL resources under
 * legacy/.../cuentaInversion/sql/ (ActualizarSaldosReporteAntiguedad_UpsertSaldoReporteAntiguedad.sql, ...).
 */
public final class H2Dialect {

    private static final Pattern UPSERT = Pattern.compile(
            "(?is)^\\s*UPSERT\\s+(\"[^\"]+\"|\\S+)\\s*\\((.*?)\\)\\s*VALUES\\s*\\((.*)\\)\\s*WHERE\\s+(.*?)\\s*;?\\s*$");
    private static final Pattern WHERE_KEY = Pattern.compile("(?is)(\"[^\"]+\")\\s*=\\s*(?::\\w+|\\?)");
    private static final Pattern UTC_NOW = Pattern.compile("(?i)\\b(CURRENT_UTCTIMESTAMP|CURRENT_UTCDATE)\\b(?!\\s*\\()");
    private static final Pattern BARE_DECIMAL = Pattern.compile("(?i)\\bDECIMAL\\b(?!\\s*\\()");
    private static final Pattern COLUMN_TABLE = Pattern.compile("(?i)TEMPORARY\\s+COLUMN\\s+TABLE");
    private static final Pattern TO_DECIMAL = Pattern.compile("(?i)\\bTO_DECIMAL\\s*\\(\\s*([^,()]+?)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");
    /** {@code :name AS "Col"} / {@code ? AS "Col"} in a select list: H2 cannot type a bare parameter there (HANA can). */
    private static final Pattern PARAM_COLUMN = Pattern.compile("(:\\w+|\\?)\\s+AS\\s+\"");
    private static final Pattern MAP_CALL = Pattern.compile("(?i)(?<![\\w\"])MAP\\s*\\(");
    private static final Pattern TEMP_TABLE = Pattern.compile("(?<![\"\\w])#(\\w+)");

    private H2Dialect() {
    }

    public static String translate(String sql) {
        if (sql == null) {
            return null;
        }
        String s = sql;
        Matcher m = UPSERT.matcher(s);
        if (m.matches()) {
            List<String> keys = new ArrayList<>();
            Matcher k = WHERE_KEY.matcher(m.group(4));
            while (k.find()) {
                keys.add(k.group(1));
            }
            s = "MERGE INTO " + m.group(1) + " (" + m.group(2) + ") KEY (" + String.join(", ", keys) + ") VALUES (" + m.group(3) + ")";
        }
        s = TEMP_TABLE.matcher(s).replaceAll("\"#$1\"");
        s = COLUMN_TABLE.matcher(s).replaceAll("TEMPORARY TABLE");
        s = BARE_DECIMAL.matcher(s).replaceAll("DECIMAL(34,16)");
        s = UTC_NOW.matcher(s).replaceAll("$1()");
        s = TO_DECIMAL.matcher(s).replaceAll("CAST($1 AS DECIMAL($2,$3))");
        s = rewriteMap(s);
        s = PARAM_COLUMN.matcher(s).replaceAll("CAST($1 AS VARCHAR(100)) AS \"");
        return s;
    }

    /**
     * HANA {@code MAP(value, search, result [, default])} (a simple CASE) becomes
     * {@code CASE WHEN value = search THEN result ELSE default END}. Arguments are split at the top level commas.
     */
    private static String rewriteMap(String sql) {
        Matcher m = MAP_CALL.matcher(sql);
        while (m.find()) {
            int open = m.end() - 1;
            int depth = 0;
            int i = open;
            boolean quote = false;
            List<String> args = new ArrayList<>();
            int from = open + 1;
            for (; i < sql.length(); i++) {
                char c = sql.charAt(i);
                if (c == '\'') {
                    quote = !quote;
                } else if (!quote && c == '(') {
                    depth++;
                } else if (!quote && c == ')') {
                    depth--;
                    if (depth == 0) {
                        args.add(sql.substring(from, i));
                        break;
                    }
                } else if (!quote && c == ',' && depth == 1) {
                    args.add(sql.substring(from, i));
                    from = i + 1;
                }
            }
            if (args.size() < 3 || args.size() > 4) {
                continue;
            }
            String els = args.size() == 4 ? " ELSE " + args.get(3) : "";
            String repl = "CASE WHEN " + args.get(0) + " = " + args.get(1) + " THEN " + args.get(2) + els + " END";
            return rewriteMap(sql.substring(0, m.start()) + repl + sql.substring(i + 1));
        }
        return sql;
    }
}
