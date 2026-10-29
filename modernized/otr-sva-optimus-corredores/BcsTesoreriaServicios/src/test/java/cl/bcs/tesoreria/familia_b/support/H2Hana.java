package cl.bcs.tesoreria.familia_b.support;

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
 *       replaced by the reviewed H2 equivalents in {@code tesoreria-b/h2-dialect/*.sql}, matched by the
 *       whitespace-normalised HANA source text. See {@code h2-dialect/README.txt}.</li>
 * </ol>
 * The SQL functions HANA provides (TO_NVARCHAR, TO_BIGINT, TO_DECIMAL, TO_DATE, TO_TIMESTAMP, ADD_SECONDS ...) are static
 * methods of this class registered as H2 aliases by {@code schema-tesoreria-b.sql}. They take {@code String} arguments:
 * H2 converts any SQL value (number, date, timestamp, text) to its canonical text, which is what HANA's implicit conversion does too.
 * {@code SELECT TOP n} and the {@code ||}/COALESCE/RANK/window syntax run natively in H2 MODE=LEGACY.
 */
public final class H2Hana {

    /** Fixed UTC clock returned by CURRENT_UTCTIMESTAMP; equal to the oracle shim FechaDB clock. */
    public static final Timestamp UTC_NOW = Timestamp.valueOf("2025-04-01 10:15:30");

    private static final Pattern TEMP_TABLE = Pattern.compile("#(\\w+)");
    private static final Pattern DEFAULT_SCHEMA = Pattern.compile("\\bBSCL\\.\"");
    private static final Pattern UTC_NOW_RE = Pattern.compile("\\bCURRENT_UTCTIMESTAMP\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MAP_RE = Pattern.compile("\\bMAP\\s*\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern UCASE_RE = Pattern.compile("\\bUCASE\\s*\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern UPDATE_FROM = Pattern.compile(
            "(?is)^\\s*UPDATE\\s+(\\w+)\\s+SET\\s+(.*?)\\s+FROM\\s+(\"[^\"]+\")\\s+(?:AS\\s+)?(\\w+)\\s+(WHERE\\s+.*)$");
    /** a bare parameter used as a select-list expression with an alias: H2 cannot infer its type ({@code ? AS "FechaSaldo"}). */
    private static final Pattern PARAM_AS_COLUMN = Pattern.compile("(?<![\\w\"?])\\?(\\s+AS\\s+\"\\w+\")", Pattern.CASE_INSENSITIVE);
    /** ROWNUM is a pseudo-column of H2 in MODE=LEGACY; the HANA SQL uses it as a plain alias (ConsultarSaldoCuentaInversion_Select_Ultimo_Movimiento.sql). */
    private static final Pattern ROWNUM_RE = Pattern.compile("\\bRowNum\\b");
    /** HANA {@code CAST(BINTOSTR(CAST("Col" AS BINARY)) AS VARCHAR)} is text -> bytes -> text (the identity); H2 2.x reads a text cast to BINARY as hexadecimal. */
    private static final Pattern BINTOSTR_ROUNDTRIP = Pattern.compile(
            "CAST\\s*\\(\\s*BINTOSTR\\s*\\(\\s*CAST\\s*\\(\\s*(\"[^\"]+\"|\\w+)\\s+AS\\s+BINARY\\s*\\)\\s*\\)\\s+AS\\s+VARCHAR\\s*\\)", Pattern.CASE_INSENSITIVE);
    /** HANA sequence access {@code "BSCL.x::Seq".NEXTVAL}. */
    private static final Pattern SEQ_NEXTVAL = Pattern.compile("(\"[^\"]+\")\\.NEXTVAL\\b", Pattern.CASE_INSENSITIVE);
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
        s = PARAM_AS_COLUMN.matcher(s).replaceAll("CAST(? AS VARCHAR(100))$1");
        s = ROWNUM_RE.matcher(s).replaceAll("RowNumero");
        s = BINTOSTR_ROUNDTRIP.matcher(s).replaceAll("CAST($1 AS VARCHAR)");
        s = SEQ_NEXTVAL.matcher(s).replaceAll("NEXT VALUE FOR $1");
        Matcher upd = UPDATE_FROM.matcher(s);
        if (upd.matches() && upd.group(1).equalsIgnoreCase(upd.group(4))) {
            s = "UPDATE " + upd.group(3) + " AS " + upd.group(4) + " SET " + upd.group(2) + " " + upd.group(5);
        }
        return s;
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
        String index = readResource("tesoreria-b/h2-dialect/index.txt");
        for (String line : index.split("\\R")) {
            String name = line.strip();
            if (name.isEmpty() || name.startsWith("#")) {
                continue;
            }
            String body = readResource("tesoreria-b/h2-dialect/" + name);
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
            // the legacy classpath path (oracle) or its ported copy of the modern module (sql/<same file name>, byte-identical)
            String hana = readResourceCualquiera(hanaPath, hanaPath.replace("cl/bcs/tesoreria/sql/", "sql/"));
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

    private static String readResourceCualquiera(String primero, String segundo) {
        String p = H2Hana.class.getClassLoader().getResource(primero) != null ? primero : segundo;
        return readResource(p);
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
    // (registered as H2 aliases by schema-tesoreria-b.sql; String arguments = canonical text of any SQL value)

    private static String javaFormat(String hanaFmt) {
        return hanaFmt.replace("YYYY", "yyyy").replace("DD", "dd").replace("HH24", "HH").replace("MI", "mm").replace("SS", "ss");
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
            return new java.text.SimpleDateFormat(javaFormat(fmt)).format(java.sql.Date.valueOf(s.substring(0, 10)));
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

    /** HANA ADD_DAYS(date, n): the date n days later (text in, date out). */
    public static java.sql.Date addDays(String v, Long dias) {
        if (v == null || dias == null) {
            return null;
        }
        return java.sql.Date.valueOf(java.time.LocalDate.parse(v.strip().substring(0, 10)).plusDays(dias));
    }

    /** HANA BINTOSTR(binary): the bytes read as text. */
    public static String bintoStr(byte[] b) {
        return b == null ? null : new String(b, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static Timestamp utcNow() {
        return UTC_NOW;
    }
}
