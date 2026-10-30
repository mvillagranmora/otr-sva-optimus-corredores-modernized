package cl.bcs.replica.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** Readers of the dump of a run ({@link CasoReplica#ejecutar}) for the rule tests: the SQL sent, the rows of a table, the exception, the collaborator calls. */
final class VolcadoAyuda {
    static final String WS = "BSCL.Plataforma.BD::PLA.Replica.WorkStack";
    static final String RESUMEN = "BSCL.Plataforma.BD::PLA.ResumenReplica";
    static final String POST = "BSCL.Replica.BD::EnvioPostPauta";
    static final String SEF = "BSCL.CuentaInversion.BD::CTA.SaldoFechaEfectiva";
    static final String SFM = "BSCL.CuentaInversion.BD::CTA.SaldoFechaMovimiento";
    static final String HLI = "BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion";
    static final String HOP = "BSCL.Custodia.BD::HIS.CustodiaFechaOperacion";

    private VolcadoAyuda() {
    }

    /** the dump of the run of the case whose id contains every fragment */
    static JsonObject run(String... fragmentos) throws Exception {
        return CasoReplica.corrida(CasoReplica.id(fragmentos));
    }

    static JsonArray filas(JsonObject d, String tabla) {
        return d.getAsJsonObject("tablas").getAsJsonArray(tabla);
    }

    static List<String> sql(JsonObject d) {
        List<String> l = new ArrayList<>();
        for (JsonElement s : d.getAsJsonArray("sentencias")) l.add(s.getAsJsonObject().get("sql").getAsString());
        return l;
    }

    static int sentencias(JsonObject d) {
        return d.getAsJsonArray("sentencias").size();
    }

    /** the number of sentences whose text starts with {@code prefijo} (case-insensitive) */
    static long contar(JsonObject d, String prefijo) {
        return sql(d).stream().filter(s -> s.toUpperCase().startsWith(prefijo.toUpperCase())).count();
    }

    static long ejecutadas(JsonObject d, String ejecucion) {
        long n = 0;
        for (JsonElement s : d.getAsJsonArray("sentencias")) if (s.getAsJsonObject().get("ejecucion").getAsString().equals(ejecucion)) n++;
        return n;
    }

    /** "DELETE" / "INSERT" / "UPSERT" / "DROP" / "CREATE" / "batch:INSERT" ... the kind of every sentence in order */
    static List<String> tipos(JsonObject d) {
        List<String> l = new ArrayList<>();
        for (JsonElement e : d.getAsJsonArray("sentencias")) {
            JsonObject s = e.getAsJsonObject();
            String verbo = s.get("sql").getAsString().split("\\s+")[0].toUpperCase();
            l.add((s.get("tipo").getAsString().equals("batch") ? "batch:" : "") + verbo);
        }
        return l;
    }

    static String excepcion(JsonObject d) {
        return d.has("excepcion") ? d.get("excepcion").getAsString().split(":")[0] : null;
    }

    static List<String> columna(JsonArray filas, String columna) {
        List<String> l = new ArrayList<>();
        for (JsonElement f : filas) {
            JsonElement v = f.getAsJsonObject().get(columna);
            l.add(v == null || v.isJsonNull() ? null : v.getAsString());
        }
        return l;
    }

    static List<String> parametrosWs(JsonObject d) {
        return columna(filas(d, WS), "Parameters");
    }

    static List<String> tablasWs(JsonObject d) {
        return columna(filas(d, WS), "Table");
    }

    static JsonArray colaboradores(JsonObject d, String colaborador, String metodo) {
        JsonArray a = new JsonArray();
        for (JsonElement c : d.getAsJsonArray("colaboradores")) {
            JsonObject o = c.getAsJsonObject();
            if (o.get("colaborador").getAsString().equals(colaborador) && o.get("metodo").getAsString().equals(metodo)) a.add(o);
        }
        return a;
    }

    static String resumenSql(JsonObject d) {
        return sql(d).stream().map(s -> s.length() > 70 ? s.substring(0, 70) : s).collect(Collectors.joining(" | "));
    }

    static void afirmarContiene(List<String> l, String esperado, String mensaje) {
        assertTrue(l.contains(esperado), mensaje + ": " + l);
    }
}
