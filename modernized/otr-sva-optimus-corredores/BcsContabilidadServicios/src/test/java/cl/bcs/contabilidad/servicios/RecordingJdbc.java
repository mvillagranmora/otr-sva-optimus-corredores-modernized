package cl.bcs.contabilidad.servicios;

import org.h2.tools.SimpleResultSet;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.SQLExceptionSubclassTranslator;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A REAL {@link NamedParameterJdbcTemplate} + {@link JdbcTemplate} running over a fake JDBC driver.
 *
 * <p>Everything Spring does is real: named-parameter parsing, collection expansion, "No value supplied
 * for the SQL parameter" errors, BeanPropertyRowMapper / BeanPropertySqlParameterSource semantics,
 * empty-batch short-circuit. Only the database is fake: result rows are scripted per LEGACY SQL file
 * (statements are identified by comparing their normalised text to the legacy oracle copy), a blank
 * statement fails like a real driver would (syntax error), and every statement is recorded together
 * with the values bound to each of its named parameters.
 */
final class RecordingJdbc extends NamedParameterJdbcTemplate {

    static final Object MISSING = new Object() {
        @Override public String toString() { return "<NO VALUE SUPPLIED>"; }
    };

    /** One statement the service handed to Spring. */
    record Call(String sql, String legacyFile, String kind, List<Map<String, Object>> paramSets, boolean batch) {
        Map<String, Object> params() { return paramSets.isEmpty() ? Map.of() : paramSets.get(0); }
        /** Statements that actually reach the database (Spring skips empty batches). */
        int executions() { return batch ? paramSets.size() : 1; }
        @Override public String toString() { return (legacyFile != null ? legacyFile : "<" + LegacySql.norm(sql) + ">") + " " + paramSets; }
    }

    private static final class Db {
        List<Map<String, Object>> pending = List.of();
        boolean failNext;
    }

    private final Db db;
    private final List<Call> calls = new ArrayList<>();
    private final Map<String, List<Map<String, Object>>> rowsByFile = new HashMap<>();
    private final Set<String> failing = new HashSet<>();
    private final Map<String, long[]> sequences = new LinkedHashMap<>();
    private final Map<String, String> sequenceLabels = new HashMap<>();
    private final Map<String, String> legacyNorm = new LinkedHashMap<>();
    private List<Map<String, Object>> defaultSelectRows = null;

    RecordingJdbc() { this(new Db()); }

    private RecordingJdbc(Db db) {
        super(template(db));
        this.db = db;
        for (String f : listLegacyFiles()) legacyNorm.put(f, LegacySql.norm(LegacySql.text(f)));
    }

    private static JdbcTemplate template(Db db) {
        JdbcTemplate jt = new JdbcTemplate(fakeDataSource(db));
        jt.setExceptionTranslator(new SQLExceptionSubclassTranslator());
        return jt;
    }

    // ------------------------------------------------------------------ scripting

    @SafeVarargs
    final RecordingJdbc rows(String legacyFile, Map<String, Object>... rows) {
        rowsByFile.put(legacyFile, Arrays.asList(rows));
        return this;
    }

    RecordingJdbc rows(String legacyFile, List<Map<String, Object>> rows) {
        rowsByFile.put(legacyFile, rows);
        return this;
    }

    /** Any SELECT that is not a known legacy statement returns these rows (used for the SAP OACT query). */
    @SafeVarargs
    final RecordingJdbc defaultSelectRows(Map<String, Object>... rows) {
        defaultSelectRows = Arrays.asList(rows);
        return this;
    }

    /** The statement identified as this legacy file fails with an SQLException when executed. */
    RecordingJdbc failOn(String legacyFile) {
        failing.add(legacyFile);
        return this;
    }

    /**
     * A HANA sequence. Any SELECT containing {@code "<name>".NEXTVAL} returns ids start, start+1, ...
     * One row per call, or {@code :cantidad} rows when that parameter is bound (legacy Utils.getSecuencia).
     */
    RecordingJdbc sequence(String name, long start, String columnLabel) {
        sequences.put(name, new long[]{start});
        sequenceLabels.put(name, columnLabel);
        return this;
    }

    static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    // ------------------------------------------------------------------ inspection

    List<Call> calls() { return calls; }

    List<Call> callsTo(String legacyFile) {
        return calls.stream().filter(c -> sameText(legacyFile, c.legacyFile())).collect(Collectors.toList());
    }

    Call single(String legacyFile) {
        List<Call> l = callsTo(legacyFile);
        if (l.size() != 1) throw new AssertionError("expected exactly one execution of " + legacyFile + " but got " + l.size() + "; all calls: " + calls);
        return l.get(0);
    }

    /** All bound parameter sets for this legacy statement, across calls, that actually executed. */
    List<Map<String, Object>> executedParamSets(String legacyFile) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Call c : callsTo(legacyFile)) if (c.batch()) out.addAll(c.paramSets()); else out.add(c.params());
        return out;
    }

    /** The i-th executed parameter set of a statement; a readable assertion failure if it never ran that often. */
    Map<String, Object> executedAt(String legacyFile, int i) {
        List<Map<String, Object>> sets = executedParamSets(legacyFile);
        if (sets.size() <= i) throw new AssertionError("expected at least " + (i + 1) + " execution(s) of " + legacyFile + " but got " + sets.size() + "; all calls: " + calls);
        return sets.get(i);
    }

    int executions(String legacyFile) {
        return callsTo(legacyFile).stream().mapToInt(Call::executions).sum();
    }

    List<String> executedFilesInOrder() {
        return calls.stream().filter(c -> c.executions() > 0).map(Call::legacyFile).collect(Collectors.toList());
    }

    /**
     * True when the given legacy files were executed in this relative order. Alias-aware: two legacy files with
     * identical text (e.g. AgregarMetadata_/EditarMetadata_InsertMetadataFiltros.sql) are the same statement, so
     * {@link #executedFilesInOrder()} may report either name.
     */
    boolean executedInOrder(String... legacyFiles) {
        int i = 0;
        for (String f : executedFilesInOrder()) if (i < legacyFiles.length && sameText(f, legacyFiles[i])) i++;
        return i == legacyFiles.length;
    }

    // ------------------------------------------------------------------ Spring hooks

    @Override
    protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource ps,
            Consumer<PreparedStatementCreatorFactory> customizer) {
        Call c = record(sql, List.of(ps), false);
        db.pending = resolveRows(c);
        db.failNext = failing.stream().anyMatch(f -> sameText(f, c.legacyFile()));
        return super.getPreparedStatementCreator(sql, ps, customizer);
    }

    @Override
    public int[] batchUpdate(String sql, SqlParameterSource[] batchArgs) {
        Call c = record(sql, Arrays.asList(batchArgs), true);
        db.pending = List.of();
        db.failNext = failing.stream().anyMatch(f -> sameText(f, c.legacyFile()));
        return super.batchUpdate(sql, batchArgs);
    }

    private Call record(String sql, List<SqlParameterSource> sources, boolean batch) {
        Set<String> names = LegacySql.paramNames(sql);
        List<Map<String, Object>> sets = new ArrayList<>();
        for (SqlParameterSource s : sources) {
            Map<String, Object> m = new LinkedHashMap<>();
            if (s instanceof MapSqlParameterSource ms) m.putAll(ms.getValues());
            for (String n : names) m.put(n, s.hasValue(n) ? s.getValue(n) : MISSING);
            sets.add(m);
        }
        String n = LegacySql.norm(sql);
        String kind = n.isEmpty() ? "" : n.split("[ (]")[0].toUpperCase(Locale.ROOT);
        Call c = new Call(sql, identify(n), kind, sets, batch);
        calls.add(c);
        return c;
    }

    /** Two legacy files with identical normalised text are the same statement (aliases). */
    private boolean sameText(String a, String b) {
        if (a == null || b == null) return false;
        if (a.equals(b)) return true;
        String na = legacyNorm.get(a);
        return na != null && na.equals(legacyNorm.get(b));
    }

    private String identify(String n) {
        if (n.isEmpty()) return null;
        for (var e : legacyNorm.entrySet()) if (e.getValue().equals(n)) return e.getKey();
        String best = null;
        for (var e : legacyNorm.entrySet())
            if (n.contains(e.getValue()) && (best == null || e.getValue().length() > legacyNorm.get(best).length())) best = e.getKey();
        return best;
    }

    private List<Map<String, Object>> resolveRows(Call c) {
        String n = LegacySql.norm(c.sql());
        if (n.toUpperCase(Locale.ROOT).startsWith("SELECT")) {
            for (var e : sequences.entrySet()) {
                if (n.contains("\"" + e.getKey() + "\".NEXTVAL")) {
                    Object cant = c.params().get("cantidad");
                    long count = cant instanceof Number num ? num.longValue() : 1L;
                    List<Map<String, Object>> out = new ArrayList<>();
                    for (long i = 0; i < count; i++) out.add(row(sequenceLabels.get(e.getKey()), e.getValue()[0]++));
                    return out;
                }
            }
        }
        if (c.legacyFile() == null) {
            return defaultSelectRows != null && n.toUpperCase(Locale.ROOT).startsWith("SELECT") ? defaultSelectRows : List.of();
        }
        List<Map<String, Object>> rows = rowsByFile.entrySet().stream().filter(e -> sameText(e.getKey(), c.legacyFile()))
                .map(Map.Entry::getValue).findFirst().orElse(List.of());
        String legacy = legacyNorm.get(c.legacyFile());
        if (!n.equals(legacy) && n.toUpperCase(Locale.ROOT).startsWith("SELECT COUNT(")) {
            return List.of(row("Contador", (long) rows.size())); // legacy SQL.countQuery wrapper
        }
        return rows;
    }

    // ------------------------------------------------------------------ fake JDBC driver

    private static List<String> listLegacyFiles() {
        try {
            var url = RecordingJdbc.class.getClassLoader().getResource("servicios/sql");
            if (url == null) throw new IllegalStateException("legacy oracle SQL directory missing");
            String[] names = new java.io.File(url.toURI()).list();
            return names == null ? List.of() : Arrays.stream(names).sorted().toList();
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object defaultValue(Class<?> t) {
        if (t == boolean.class) return false;
        if (t == int.class) return 0;
        if (t == long.class) return 0L;
        if (t == short.class) return (short) 0;
        if (t == byte.class) return (byte) 0;
        if (t == double.class) return 0d;
        if (t == float.class) return 0f;
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler h) {
        return (T) Proxy.newProxyInstance(RecordingJdbc.class.getClassLoader(), new Class<?>[]{type}, h);
    }

    private static DataSource fakeDataSource(Db db) {
        DatabaseMetaData md = proxy(DatabaseMetaData.class, (p, m, a) -> switch (m.getName()) {
            case "getDatabaseProductName" -> "HDB";
            case "supportsBatchUpdates" -> true;
            case "toString" -> "FakeHanaMetaData";
            default -> defaultValue(m.getReturnType());
        });
        Connection[] conHolder = new Connection[1];
        Connection con = proxy(Connection.class, (p, m, a) -> switch (m.getName()) {
            case "prepareStatement" -> {
                String sql = (String) a[0];
                if (sql == null || sql.isBlank()) throw new SQLSyntaxErrorException("SAP DBTech JDBC: [257]: sql syntax error: empty statement");
                yield preparedStatement(db, conHolder[0]);
            }
            case "getMetaData" -> md;
            case "getAutoCommit" -> true;
            case "isClosed" -> false;
            case "toString" -> "FakeHanaConnection";
            case "hashCode" -> System.identityHashCode(p);
            case "equals" -> p == a[0];
            case "unwrap" -> null;
            default -> defaultValue(m.getReturnType());
        });
        conHolder[0] = con;
        return proxy(DataSource.class, (p, m, a) -> switch (m.getName()) {
            case "getConnection" -> con;
            case "toString" -> "FakeHanaDataSource";
            case "hashCode" -> System.identityHashCode(p);
            case "equals" -> p == a[0];
            default -> defaultValue(m.getReturnType());
        });
    }

    private static PreparedStatement preparedStatement(Db db, Connection con) {
        int[] batch = {0};
        ParameterMetaData pmd = proxy(ParameterMetaData.class, (p, m, a) ->
                m.getName().equals("getParameterType") ? Types.VARCHAR : defaultValue(m.getReturnType()));
        return proxy(PreparedStatement.class, (p, m, a) -> {
            switch (m.getName()) {
                case "executeQuery": {
                    if (db.failNext) throw new SQLException("simulated database failure");
                    return resultSet(db.pending);
                }
                case "executeUpdate":
                    if (db.failNext) throw new SQLException("simulated database failure");
                    return 1;
                case "addBatch":
                    batch[0]++;
                    return null;
                case "executeBatch": {
                    if (db.failNext) throw new SQLException("simulated database failure");
                    int[] r = new int[batch[0]];
                    Arrays.fill(r, 1);
                    return r;
                }
                case "execute":
                    if (db.failNext) throw new SQLException("simulated database failure");
                    return false;
                case "getUpdateCount": return -1;
                case "getConnection": return con;
                case "getParameterMetaData": return pmd;
                case "toString": return "FakeHanaPreparedStatement";
                case "hashCode": return System.identityHashCode(p);
                case "equals": return p == a[0];
                default: return defaultValue(m.getReturnType());
            }
        });
    }

    private static ResultSet resultSet(List<Map<String, Object>> rows) {
        SimpleResultSet rs = new SimpleResultSet();
        LinkedHashMap<String, Integer> cols = new LinkedHashMap<>();
        for (Map<String, Object> r : rows)
            for (var e : r.entrySet()) {
                Integer t = cols.get(e.getKey());
                if (t == null || (t == Types.VARCHAR && e.getValue() != null && !(e.getValue() instanceof String)))
                    cols.put(e.getKey(), sqlType(e.getValue()));
            }
        for (var e : cols.entrySet()) rs.addColumn(e.getKey(), e.getValue(), 38, 10);
        for (Map<String, Object> r : rows) {
            Object[] vals = new Object[cols.size()];
            int i = 0;
            for (String c : cols.keySet()) vals[i++] = r.get(c);
            rs.addRow(vals);
        }
        return rs;
    }

    private static int sqlType(Object v) {
        if (v == null || v instanceof String) return Types.VARCHAR;
        if (v instanceof Long) return Types.BIGINT;
        if (v instanceof Integer || v instanceof Short) return Types.INTEGER;
        if (v instanceof BigDecimal) return Types.DECIMAL;
        if (v instanceof Boolean) return Types.BOOLEAN;
        if (v instanceof java.sql.Date) return Types.DATE;
        if (v instanceof Timestamp) return Types.TIMESTAMP;
        return Types.JAVA_OBJECT;
    }
}
