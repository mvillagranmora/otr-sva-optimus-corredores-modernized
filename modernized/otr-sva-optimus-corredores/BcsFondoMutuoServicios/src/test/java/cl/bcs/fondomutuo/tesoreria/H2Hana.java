package cl.bcs.fondomutuo.tesoreria;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SAP HANA -> H2 dialect bridge used by BOTH sides of the characterization tests, so that the legacy oracle and the
 * modern code execute exactly the same SQL text (the production .sql resources, unmodified) against the same H2 engine.
 *
 * <p>It wraps a JDBC {@link Connection} (dynamic proxy) and rewrites every statement the driver is asked to prepare or
 * execute. The wrapper sees the JDBC form ({@code ?} placeholders): the legacy {@code cl.bcs.utils.SQL} and Spring's
 * {@code NamedParameterJdbcTemplate} both resolve {@code :name} before calling the driver.
 *
 * <p>Rewrites (each one is a HANA construct that H2 does not parse; none changes the meaning for the seeded data):
 * <ol>
 *   <li>trailing {@code ;} is removed (HANA's JDBC driver accepts it, H2 only for single statements);</li>
 *   <li>{@code #Name} (HANA local temporary table prefix) becomes {@code TT_Name};</li>
 *   <li>{@code BSCL."X"} (HANA default schema prefix) becomes {@code "X"};</li>
 *   <li>{@code CURRENT_UTCTIMESTAMP} becomes the fixed-clock alias {@code HANA_UTCNOW()} (deterministic);</li>
 *   <li>{@code MAP(} becomes {@code DECODE(} (NULL search value matches NULL, as in HANA), {@code UCASE(} becomes {@code UPPER(};</li>
 *   <li>{@code ? AS "Alias"} (parameter as a select-list expression) becomes {@code CAST(? AS VARCHAR(100)) AS "Alias"} (H2 cannot infer the type;
 *       every such parameter in the SQL resources is a date/text);</li>
 *   <li>the alias {@code RowNum} becomes {@code RowNumero} (ROWNUM is reserved in H2 LEGACY mode);</li>
 *   <li>{@code UPDATE a SET .. FROM "t" AS a WHERE ..} (single table) becomes {@code UPDATE "t" AS a SET .. WHERE ..};</li>
 *   <li>statements that need a structural rewrite (multi-table UPDATE .. FROM .. JOIN, parameters in a select list) are
 *       replaced by the reviewed H2 equivalents in {@code tesoreria-a/h2-dialect/*.sql}, matched by the
 *       whitespace-normalised HANA source text. See {@code h2-dialect/README.txt}.</li>
 * </ol>
 * The SQL functions HANA provides (TO_NVARCHAR, TO_BIGINT, TO_DECIMAL, TO_DATE, TO_TIMESTAMP, ADD_SECONDS ...) are static
 * methods of this class registered as H2 aliases by {@code schema-tesoreria-a.sql}. They take {@code String} arguments:
 * H2 converts any SQL value (number, date, timestamp, text) to its canonical text, which is what HANA's implicit conversion does too.
 * {@code SELECT TOP n} and the {@code ||}/COALESCE/RANK/window syntax run natively in H2 MODE=LEGACY.
 */
public final class H2Hana {

    /** Fixed UTC clock returned by CURRENT_UTCTIMESTAMP; equal to the oracle shim FechaDB clock. */
    public static final Timestamp UTC_NOW = Timestamp.valueOf("2025-04-01 10:15:30");

    private static final Pattern TEMP_TABLE = Pattern.compile("#(\\w+)");
    private static final Pattern DEFAULT_SCHEMA = Pattern.compile("\\bBSCL\\.\"");
    /** CURRENT_UTCTIMESTAMP and (the server-local) CURRENT_TIMESTAMP: both read the fixed test clock (2025-04-01 10:15:30), the stand-in has no zone. */
    private static final Pattern UTC_NOW_RE = Pattern.compile("\\bCURRENT_(?:UTC)?TIMESTAMP\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MAP_RE = Pattern.compile("\\bMAP\\s*\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern UCASE_RE = Pattern.compile("\\bUCASE\\s*\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern TO_CHAR_RE = Pattern.compile("\\bTO_CHAR\\s*\\(", Pattern.CASE_INSENSITIVE);
    /**
     * HANA {@code "sequence".NEXTVAL} -> {@code HANA_NEXTVAL('sequence')}. H2's own {@code NEXT VALUE FOR} is evaluated ONCE per grouped query (every
     * group would get the same id, e.g. the comprobante ids of CrearComprobante_InsertGruposDeComprobantes.sql); HANA gives every row its own value,
     * which the alias reproduces (it asks H2 for the next value on each call).
     */
    private static final Pattern NEXTVAL_RE = Pattern.compile("\"([^\"]+)\"\\.NEXTVAL\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern UPDATE_FROM = Pattern.compile(
            "(?is)^\\s*UPDATE\\s+(\\w+)\\s+SET\\s+(.*?)\\s+FROM\\s+(\"[^\"]+\")\\s+(?:AS\\s+)?(\\w+)\\b(.*)$");
    /** a bare parameter used as a select-list expression with an alias: H2 cannot infer its type ({@code ? AS "FechaSaldo"}). */
    private static final Pattern PARAM_AS_COLUMN = Pattern.compile("(?<![\\w\"?])\\?(\\s+AS\\s+\"\\w+\")", Pattern.CASE_INSENSITIVE);
    /** ROWNUM is a pseudo-column of H2 in MODE=LEGACY; the HANA SQL uses it as a plain alias (ConsultarSaldoCuentaInversion_Select_Ultimo_Movimiento.sql). */
    private static final Pattern ROWNUM_RE = Pattern.compile("\\bRowNum\\b");
    /** HANA column store attributes of CREATE TABLE ({@code BIGINT CS_FIXED}, {@code NVARCHAR(3) CS_STRING}): no H2 equivalent, no semantics. */
    private static final Pattern CS_ATTR_RE = Pattern.compile("\\s+CS_[A-Z0-9_]+\\b");
    private static final Pattern NAMED_PARAM = Pattern.compile("(?<![:\\w]):([A-Za-z_]\\w*)");
    private static final Pattern COMMENTS = Pattern.compile("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)");

    private static Map<String, String> overrides;
    private static final Map<String, String> CACHE = new HashMap<>();

    private H2Hana() {
    }

    // ------------------------------------------------------------------------------------------------ translation

    public static synchronized String translate(String sql) {
        if (sql == null) {
            return null;
        }
        String cached = CACHE.get(sql);
        if (cached != null) {
            return cached;
        }
        String out = doTranslate(sql);
        CACHE.put(sql, out);
        return out;
    }

    private static String doTranslate(String sql) {
        String override = overrides().get(normalize(sql));
        if (override != null) {
            return override;
        }
        String s = stripTrailingSemicolon(sql.strip());
        s = TEMP_TABLE.matcher(s).replaceAll("TT_$1");
        s = DEFAULT_SCHEMA.matcher(s).replaceAll("\"");
        s = UTC_NOW_RE.matcher(s).replaceAll("HANA_UTCNOW()");
        s = MAP_RE.matcher(s).replaceAll("DECODE(");
        s = UCASE_RE.matcher(s).replaceAll("UPPER(");
        s = TO_CHAR_RE.matcher(s).replaceAll("TO_VARCHAR(");
        s = PARAM_AS_COLUMN.matcher(s).replaceAll("CAST(? AS VARCHAR(100))$1");
        s = ROWNUM_RE.matcher(s).replaceAll("RowNumero");
        s = CS_ATTR_RE.matcher(s).replaceAll("");
        s = NEXTVAL_RE.matcher(s).replaceAll("HANA_NEXTVAL('$1')");
        Matcher upd = UPDATE_FROM.matcher(s);
        if (upd.matches() && upd.group(1).equalsIgnoreCase(upd.group(4))) {
            if (upd.group(5).strip().toUpperCase().startsWith("WHERE")) {
                s = "UPDATE " + upd.group(3) + " AS " + upd.group(4) + " SET " + upd.group(2) + " " + upd.group(5);
            } else {
                s = updateFromJoin(upd.group(1), upd.group(2), upd.group(3), upd.group(5));
            }
        }
        return s;
    }

    /**
     * HANA {@code UPDATE a SET c1 = e1, .. FROM "t" a JOIN .. WHERE ..} (update with join) as an H2 MERGE: the SET expressions are evaluated in the
     * joined source query (same rows, same values: they see the pre-update row), the target row is identified by H2's {@code _ROWID_}.
     * Parameters keep their order (SET expressions first, then join/where).
     */
    static String updateFromJoin(String alias, String set, String table, String rest) {
        java.util.List<String> assignments = splitTopLevel(set);
        StringBuilder select = new StringBuilder("SELECT " + alias + "._ROWID_ AS RID__");
        StringBuilder sets = new StringBuilder();
        for (int i = 0; i < assignments.size(); i++) {
            String a = assignments.get(i);
            int eq = indexOfTopLevelEquals(a);
            String lhs = a.substring(0, eq).strip();
            String rhs = a.substring(eq + 1).strip();
            if (lhs.toUpperCase().startsWith(alias.toUpperCase() + ".")) {
                lhs = lhs.substring(alias.length() + 1);
            }
            // a bare parameter has no type in a select list: H2 converts the text on assignment (same value)
            select.append(", ").append(rhs.equals("?") ? "CAST(? AS VARCHAR(4000))" : rhs).append(" AS V").append(i).append("__");
            sets.append(i == 0 ? "" : ", ").append(lhs).append(" = S__.V").append(i).append("__");
        }
        select.append(" FROM ").append(table).append(" ").append(alias).append(" ").append(rest.strip());
        return "MERGE INTO " + table + " " + alias + " USING (" + select + ") S__ ON " + alias + "._ROWID_ = S__.RID__ WHEN MATCHED THEN UPDATE SET " + sets;
    }

    private static java.util.List<String> splitTopLevel(String s) {
        java.util.List<String> out = new java.util.ArrayList<>();
        int depth = 0;
        int start = 0;
        char quote = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (quote != 0) {
                if (c == quote) {
                    quote = 0;
                }
            } else if (c == '\'' || c == '"') {
                quote = c;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                out.add(s.substring(start, i));
                start = i + 1;
            }
        }
        out.add(s.substring(start));
        return out;
    }

    private static int indexOfTopLevelEquals(String s) {
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (quote != 0) {
                if (c == quote) {
                    quote = 0;
                }
            } else if (c == '\'' || c == '"') {
                quote = c;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == '=' && depth == 0) {
                return i;
            }
        }
        throw new IllegalStateException("no assignment in SET item: " + s);
    }

    private static String stripTrailingSemicolon(String s) {
        String t = s;
        while (t.endsWith(";")) {
            t = t.substring(0, t.length() - 1).stripTrailing();
        }
        return t;
    }

    /** Removes comments, collapses whitespace, drops a trailing ';' and rewrites :name to ?. */
    static String normalize(String sql) {
        String s = COMMENTS.matcher(sql).replaceAll("");
        s = NAMED_PARAM.matcher(s).replaceAll("?");
        s = s.replaceAll("\\s+", " ").strip();
        return stripTrailingSemicolon(s);
    }

    private static synchronized Map<String, String> overrides() {
        if (overrides != null) {
            return overrides;
        }
        Map<String, String> map = new HashMap<>();
        String index = readResource("fondomutuo-tes/h2-dialect/index.txt");
        for (String line : index.split("\\R")) {
            String name = line.strip();
            if (name.isEmpty() || name.startsWith("#")) {
                continue;
            }
            String body = readResource("fondomutuo-tes/h2-dialect/" + name);
            String[] lines = body.split("\\R", -1);
            if (!lines[0].startsWith("-- HANA-SOURCE:")) {
                throw new IllegalStateException("h2-dialect/" + name + " must start with '-- HANA-SOURCE: <classpath sql>'");
            }
            String hanaPath = lines[0].substring("-- HANA-SOURCE:".length()).strip();
            // optional: -- HANA-LITERAL: p_IdEmpresa=1  (the legacy substitutes this parameter textually before executing)
            List<String[]> literales = new ArrayList<>();
            int first = 1;
            while (first < lines.length && lines[first].startsWith("-- HANA-LITERAL:")) {
                literales.add(lines[first].substring("-- HANA-LITERAL:".length()).strip().split("=", 2));
                first++;
            }
            String hana = readResource(hanaPath);
            String h2 = String.join("\n", Arrays.asList(lines).subList(first, lines.length));
            if (!paramNames(hana).equals(paramNames(h2))) {
                throw new IllegalStateException("h2-dialect/" + name + ": the H2 variant must use the same :named parameters in the same order as "
                        + hanaPath + " -> " + paramNames(hana) + " vs " + paramNames(h2));
            }
            map.put(normalize(hana), normalize(h2));
            for (String[] kv : literales) {
                String a = hana.replaceAll(":" + kv[0] + "\\b", Matcher.quoteReplacement(kv[1]));
                String b = h2.replaceAll(":" + kv[0] + "\\b", Matcher.quoteReplacement(kv[1]));
                map.put(normalize(a), normalize(b));
            }
        }
        overrides = map;
        return overrides;
    }

    private static List<String> paramNames(String sql) {
        String s = COMMENTS.matcher(sql).replaceAll("");
        List<String> names = new ArrayList<>();
        Matcher m = NAMED_PARAM.matcher(s);
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }

    private static String readResource(String path) {
        try (InputStream in = H2Hana.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("missing classpath resource " + path + " (a missing fixture is a FAILURE, not a skip)");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // ------------------------------------------------------------------------------------------------ JDBC wrapping

    /** Wraps {@code real} so every SQL text is translated before reaching H2. */
    public static Connection wrap(Connection real) {
        InvocationHandler h = (proxy, m, a) -> {
            try {
                String n = m.getName();
                if (a != null && a.length > 0 && a[0] instanceof String s
                        && (n.equals("prepareStatement") || n.equals("prepareCall") || n.equals("nativeSQL"))) {
                    Object[] b = a.clone();
                    b[0] = translate(s);
                    return m.invoke(real, b);
                }
                if (n.equals("createStatement")) {
                    return wrapStatement((Statement) m.invoke(real, a));
                }
                return m.invoke(real, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Connection) Proxy.newProxyInstance(H2Hana.class.getClassLoader(), new Class<?>[]{Connection.class}, h);
    }

    private static Statement wrapStatement(Statement real) {
        InvocationHandler h = (proxy, m, a) -> {
            try {
                String n = m.getName();
                if (a != null && a.length > 0 && a[0] instanceof String s
                        && (n.equals("execute") || n.equals("executeUpdate") || n.equals("executeQuery")
                        || n.equals("addBatch") || n.equals("executeLargeUpdate"))) {
                    Object[] b = a.clone();
                    b[0] = translate(s);
                    return m.invoke(real, b);
                }
                return m.invoke(real, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Statement) Proxy.newProxyInstance(H2Hana.class.getClassLoader(), new Class<?>[]{Statement.class}, h);
    }

    /** Opens (and wraps) an H2 in-memory database in MODE=LEGACY. */
    public static Connection connect(String name) throws SQLException {
        return wrap(DriverManager.getConnection("jdbc:h2:mem:" + name + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", ""));
    }

    // ------------------------------------------------------------------------------------------------ SQL functions
    // (registered as H2 aliases by schema-tesoreria-a.sql; String arguments = canonical text of any SQL value)

    private static String javaFormat(String hanaFmt) {
        return hanaFmt.replace("YYYY", "yyyy").replace("DD", "dd").replace("HH24", "HH").replace("MI", "mm").replace("SS", "ss");
    }

    /** HANA format -> text for a timestamp: YYYY MM DD HH24 MI SS and FF1..FF7 (fractional digits, zero padded). */
    private static String formatTimestamp(Timestamp ts, String fmt) {
        String out = fmt;
        java.time.LocalDateTime t = ts.toLocalDateTime();
        String nanos = String.format("%09d", t.getNano());
        for (int n = 9; n >= 1; n--) {
            out = out.replace("FF" + n, nanos.substring(0, n));
        }
        out = out.replace("YYYY", String.format("%04d", t.getYear())).replace("HH24", String.format("%02d", t.getHour()))
                .replace("MI", String.format("%02d", t.getMinute())).replace("SS", String.format("%02d", t.getSecond()))
                .replace("MM", String.format("%02d", t.getMonthValue())).replace("DD", String.format("%02d", t.getDayOfMonth()));
        return out;
    }

    /** Alias target of HANA_NEXTVAL (H2 passes the current connection as first argument). */
    public static long nextVal(Connection conn, String sequence) throws SQLException {
        try (Statement st = conn.createStatement(); java.sql.ResultSet rs = st.executeQuery("SELECT NEXT VALUE FOR \"" + sequence + "\"")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /** HANA ADD_DAYS(date, n) as used by the SQL (always applied to TO_DATE(..)): a DATE. */
    public static java.sql.Date addDays(String v, Long days) {
        if (v == null || days == null) {
            return null;
        }
        return java.sql.Date.valueOf(toTimestamp(v).toLocalDateTime().toLocalDate().plusDays(days));
    }

    /**
     * Plataforma/BD/Utilitarios/FormatearNumero.sql (SQLScript function): ROUND(p, d, ROUND_HALF_UP) for 'R', ROUND(p, d, ROUND_FLOOR) for 'T',
     * NULL for any other mode (the function body assigns nothing); result DECIMAL(26,8).
     */
    public static BigDecimal formatearNumero(BigDecimal numero, Integer decimales, String redondeaTrunca) {
        if (numero == null) {
            return null;
        }
        int d = decimales == null ? 0 : decimales;
        if ("R".equals(redondeaTrunca)) {
            return numero.setScale(d, java.math.RoundingMode.HALF_UP);
        }
        if ("T".equals(redondeaTrunca)) {
            return numero.setScale(d, java.math.RoundingMode.FLOOR);
        }
        return null;
    }

    /** HANA TO_NVARCHAR(x) / TO_VARCHAR(x): the canonical text of the value. */
    public static String toNvarchar(String v) {
        return v;
    }

    /** HANA TO_NVARCHAR(date_or_timestamp, format). A text that does not look like a date is returned as is. */
    public static String toNvarchar(String v, String fmt) {
        if (v == null) {
            return null;
        }
        String s = v.strip();
        if (s.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            return formatTimestamp(toTimestamp(s), fmt);
        }
        return s;
    }

    public static String toVarchar(String v) {
        return toNvarchar(v);
    }

    public static String toVarchar(String v, String fmt) {
        return toNvarchar(v, fmt);
    }

    public static Long toBigint(String v) {
        if (v == null) {
            return null;
        }
        return new BigDecimal(v.strip()).longValue();
    }

    public static BigDecimal toDecimal(String v) {
        if (v == null) {
            return null;
        }
        return new BigDecimal(v.strip());
    }

    public static java.sql.Date toDate(String v) {
        if (v == null) {
            return null;
        }
        return java.sql.Date.valueOf(v.strip().substring(0, 10));
    }

    public static java.sql.Date toDate(String v, String fmt) {
        return toDate(v);
    }

    public static Timestamp toTimestamp(String v) {
        if (v == null) {
            return null;
        }
        String s = v.strip();
        return Timestamp.valueOf(s.length() == 10 ? s + " 00:00:00" : s);
    }

    public static Timestamp addSeconds(Timestamp ts, Long seconds) {
        if (ts == null || seconds == null) {
            return null;
        }
        return Timestamp.from(ts.toInstant().plusSeconds(seconds));
    }

    public static Timestamp utcNow() {
        return UTC_NOW;
    }
}
