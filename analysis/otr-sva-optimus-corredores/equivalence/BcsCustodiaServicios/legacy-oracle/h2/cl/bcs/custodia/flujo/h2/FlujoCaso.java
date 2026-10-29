package cl.bcs.custodia.flujo.h2;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Runs ONE golden case of the ConsultarFlujoCustodia slice against a subject and returns the dump (the content of expected.json). SINGLE SOURCE for
 * the legacy oracle (legacy-oracle/runner/FlujoOracleRunner.java) and the JUnit harness (copied byte for byte by legacy-oracle/sync.py), so the
 * legacy and the new implementation are measured by the same code on the same H2 database.
 *
 * <p>Per case: a fresh in-memory H2 (MODE=LEGACY; the JVM must run with user.timezone=UTC and user.language=en), schema-flujo.sql +
 * schema-session.sql + seed/base.sql + the case seed.sql, committed, then autocommit OFF; the subject receives the connection wrapped by
 * {@link HanaH2} and the case script {@link FlujoGuion}. Dump: raw output text (key order, BigDecimal scale), exception (class; message only for a
 * plain java.lang.Exception), the rows written to / deleted from / left in CUS.FlujoCustodia_MatrizUso (random IdTrabajo masked), the sequences
 * requested and the ValorizarInstrumento requests with their scripted answers. Nothing else varies between runs (no clock, no timing is dumped).
 */
public final class FlujoCaso {

    public static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    public static final String MATRIZ = "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso";
    public static final String MARCA_ID_TRABAJO = "<IdTrabajo: Math.random, D-CUS-09>";
    private static final AtomicInteger DB = new AtomicInteger();

    /** the thing under test, seen through the JavaCall entry point: call(Connection, JsonObject) */
    @FunctionalInterface
    public interface Sujeto {
        JsonObject call(Connection con, JsonObject entrada, FlujoGuion guion) throws Exception;
    }

    private FlujoCaso() {
    }

    public static JsonObject leer(Path p) throws Exception {
        return JsonParser.parseString(new String(Files.readAllBytes(p), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    /**
     * @param base     directory holding schema-flujo.sql, schema-session.sql and seed/base.sql
     * @param caso     directory of the case (case.json, optional seed.sql)
     * @param sujeto   legacy or new implementation
     * @param proveniencia text stored in "_provenance" (not compared)
     */
    public static JsonObject ejecutar(Path base, Path caso, Sujeto sujeto, String proveniencia) throws Exception {
        JsonObject def = leer(caso.resolve("case.json"));
        String url = "jdbc:h2:mem:flujocus" + DB.incrementAndGet() + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        JsonObject res = new JsonObject();
        res.addProperty("_provenance", proveniencia);
        res.addProperty("caso", caso.getFileName().toString());
        res.add("reglas", def.has("reglas") ? def.get("reglas") : new JsonArray());
        try (Connection raw = DriverManager.getConnection(url, "sa", "")) {
            for (String f : new String[] { "schema-flujo.sql", "schema-session.sql", "seed/base.sql" }) runScript(raw, base.resolve(f));
            if (Files.exists(caso.resolve("seed.sql"))) runScript(raw, caso.resolve("seed.sql"));
            raw.commit();
            raw.setAutoCommit(false);
            Connection con = HanaH2.wrap(raw);
            FlujoGuion guion = new FlujoGuion(def.has("colaboradores") ? def.getAsJsonObject("colaboradores") : null);
            JsonObject entrada = def.getAsJsonObject("entrada").deepCopy();
            try {
                JsonObject out = sujeto.call(con, entrada, guion);
                res.addProperty("salidaCruda", out == null ? null : out.toString());
                res.add("excepcion", JsonNull.INSTANCE);
                res.add("salida", out == null ? JsonNull.INSTANCE : out);
            } catch (Throwable t) {
                res.add("salidaCruda", JsonNull.INSTANCE);
                JsonObject ex = new JsonObject();
                ex.addProperty("clase", t.getClass().getName());
                if (t.getClass() == Exception.class) ex.addProperty("mensaje", t.getMessage());
                res.add("excepcion", ex);
                res.add("salida", JsonNull.INSTANCE);
            }
            res.add("matrizUso", matrizUso(raw));
            res.add("secuenciasSolicitadas", guion.secuencias());
            res.add("colaboradores", guion.llamadas());
            res.addProperty("llamadaSinGuion", guion.huboLlamadaSinGuion());
            raw.rollback();
        }
        return res;
    }

    /** the dump without the fields that legitimately differ between subjects (provenance) */
    public static JsonObject comparable(JsonObject dump) {
        JsonObject c = dump.deepCopy();
        c.remove("_provenance");
        return c;
    }

    static JsonObject matrizUso(Connection raw) throws Exception {
        JsonObject m = new JsonObject();
        List<String> ins = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Set<String> idsDel = new HashSet<>();
        int del = 0;
        for (JsonElement e : filas(raw, "SELECT * FROM \"ORACLE.MatrizUsoAuditoria\" ORDER BY \"Orden\"")) {
            JsonObject r = e.getAsJsonObject();
            String op = r.get("Operacion").getAsString();
            String id = String.valueOf(r.get("IdTrabajo"));
            r.remove("Orden");
            r.remove("Operacion");
            r.addProperty("IdTrabajo", MARCA_ID_TRABAJO);
            if (op.equals("I")) {
                ins.add(r.toString());
                ids.add(id);
            } else {
                del++;
                idsDel.add(id);
            }
        }
        java.util.Collections.sort(ins);   // insertion order follows HashMap iteration (D-CUS-05): not part of the contract of a work table
        JsonArray a = new JsonArray();
        for (String s : ins) a.add(JsonParser.parseString(s));
        m.add("insertadas", a);
        m.addProperty("idTrabajoDistintos", ids.size());
        m.addProperty("eliminadas", del);
        m.addProperty("eliminadasConElMismoIdTrabajo", ids.containsAll(idsDel) && idsDel.containsAll(ids));
        List<String> rest = new ArrayList<>();
        for (JsonElement e : filas(raw, "SELECT * FROM \"" + MATRIZ + "\"")) {
            JsonObject r = e.getAsJsonObject();
            r.addProperty("IdTrabajo", MARCA_ID_TRABAJO);
            rest.add(r.toString());
        }
        java.util.Collections.sort(rest);
        JsonArray b = new JsonArray();
        for (String s : rest) b.add(JsonParser.parseString(s));
        m.add("restantes", b);
        return m;
    }

    /** rows rendered as in the FondoMutuo oracle: numbers normalised, dates as text, NULL as JSON null */
    static JsonArray filas(Connection conn, String sql) throws Exception {
        JsonArray rows = new JsonArray();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                JsonObject row = new JsonObject();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    Object v = rs.getObject(i);
                    String col = md.getColumnLabel(i);
                    if (v == null) row.add(col, JsonNull.INSTANCE);
                    else if (v instanceof Number) row.add(col, new JsonPrimitive(new BigDecimal(new BigDecimal(v.toString()).stripTrailingZeros().toPlainString())));
                    else if (v instanceof java.util.Date || v instanceof java.time.temporal.Temporal) row.addProperty(col, rs.getString(i));
                    else row.addProperty(col, v.toString());
                }
                rows.add(row);
            }
        }
        return rows;
    }

    public static void runScript(Connection conn, Path file) throws Exception {
        List<String> clean = new ArrayList<>();
        for (String line : new String(Files.readAllBytes(file), StandardCharsets.UTF_8).split("\n")) {
            if (!line.strip().startsWith("--")) clean.add(line.replaceAll(";\\s+--[^']*$", ";"));
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : String.join("\n", clean).split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }
}
