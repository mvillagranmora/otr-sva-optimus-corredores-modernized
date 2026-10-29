import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import cl.bcs.custodia.flujo.h2.FlujoCaso;
import cl.bcs.custodia.flujo.h2.FlujoGuion;
import cl.bcs.oracle.Doubles;

/**
 * Golden recorder of the slice cl.bcs.custodia.ConsultarFlujoCustodia (JavaCall). Java 11 syntax on purpose (the legacy builds with JDK 11).
 *
 * <pre>
 * java FlujoOracleRunner &lt;baseDir&gt; [caseNamePrefix]      baseDir = equivalence/BcsCustodiaServicios
 *   -Doracle.repeat=N   run every case N times and compare the dumps (instability report, D-CUS-06); the golden is written only when all N agree
 *   -Doracle.out=DIR    write DIR/&lt;case&gt;.json instead of cases/&lt;case&gt;/expected.json (re-recording check, mutation check)
 * </pre>
 * Per case: shared {@link FlujoCaso} (fresh H2, schema + seeds, dialect bridge, MatrizUso witness, dump), subject = the UNMODIFIED legacy class:
 * {@code new cl.bcs.custodia.ConsultarFlujoCustodia().call(con, json)} exactly as the CallServlet does (new instance per call). Static caches of
 * the Plataforma kit (ConfiguracionSistema, Diccionario, Fecha offset) are reset per case: one case = one request in a fresh JVM. A case that
 * reaches the ValorizarInstrumento double without a scripted answer is REFUSED (no golden written, exit code 3).
 */
public class FlujoOracleRunner {

    public static void main(String[] args) throws Exception {
        Path base = Paths.get(args[0]);
        String prefix = args.length > 1 ? args[1] : "";
        int repeat = Integer.getInteger("oracle.repeat", 1);
        String outDir = System.getProperty("oracle.out");
        List<Path> dirs;
        try (Stream<Path> s = Files.list(base.resolve("cases"))) {
            dirs = s.filter(Files::isDirectory).sorted().collect(Collectors.toList());
        }
        int n = 0;
        List<String> inestables = new ArrayList<>();
        List<String> rechazados = new ArrayList<>();
        for (Path dir : dirs) {
            if (!dir.getFileName().toString().startsWith(prefix) || !Files.exists(dir.resolve("case.json"))) continue;
            String primero = null;
            boolean estable = true;
            JsonObject dump = null;
            for (int k = 0; k < repeat; k++) {
                dump = FlujoCaso.ejecutar(base, dir, FlujoOracleRunner::legacy,
                        "Recorded by legacy-oracle/runner/FlujoOracleRunner.java executing the UNMODIFIED legacy cl.bcs.custodia.ConsultarFlujoCustodia.call"
                        + " (real cl.bcs.utils.SQL and Plataforma kit) on H2 MODE=LEGACY, JVM zone UTC, locale en; ValorizarInstrumento = scripted double."
                        + " Do not edit by hand; re-record with legacy-oracle/record.sh.");
                String t = FlujoCaso.PRETTY.toJson(FlujoCaso.comparable(dump));
                if (primero == null) primero = t;
                else if (!primero.equals(t)) estable = false;
            }
            String id = dir.getFileName().toString();
            if (dump.get("llamadaSinGuion").getAsBoolean()) {
                rechazados.add(id);
                System.out.println("!! REFUSED (unscripted collaborator call) " + id);
                continue;
            }
            if (!estable) {
                inestables.add(id);
                System.out.println("!! UNSTABLE over " + repeat + " runs: " + id + " (golden NOT written)");
                continue;
            }
            byte[] text = (FlujoCaso.PRETTY.toJson(dump) + "\n").getBytes(StandardCharsets.UTF_8);
            if (outDir != null) {
                Files.createDirectories(Paths.get(outDir));
                Files.write(Paths.get(outDir).resolve(id + ".json"), text);
            } else {
                Files.write(dir.resolve("expected.json"), text);
            }
            System.out.println("== " + id + (dump.get("excepcion").isJsonNull() ? "" : "  [exception " + dump.getAsJsonObject("excepcion").get("clase").getAsString() + "]"));
            n++;
        }
        if (outDir == null && prefix.isEmpty()) grabarMapaItems(base);
        System.out.println("goldens recorded from the legacy: " + n + (repeat > 1 ? " (each stable over " + repeat + " runs)" : ""));
        System.out.println("unstable: " + inestables);
        System.out.println("refused: " + rechazados);
        if (n == 0) System.exit(2);
        if (!rechazados.isEmpty() || !inestables.isEmpty()) System.exit(3);
    }

    /**
     * unit golden of CUS-FLU-13: the item -> source map of the legacy, read by reflection from the private field valoresItemCalculoHash of a fresh
     * instance (filled by the private setItemCalculoHash()), written sorted by item to unit-goldens/item-fuente.json
     */
    static void grabarMapaItems(Path base) throws Exception {
        java.lang.reflect.Field f = cl.bcs.custodia.ConsultarFlujoCustodia.class.getDeclaredField("valoresItemCalculoHash");
        f.setAccessible(true);
        java.util.Map<?, ?> m = (java.util.Map<?, ?>) f.get(new cl.bcs.custodia.ConsultarFlujoCustodia());
        java.util.TreeMap<String, String> orden = new java.util.TreeMap<>();
        for (java.util.Map.Entry<?, ?> e : m.entrySet()) orden.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
        JsonObject o = new JsonObject();
        o.addProperty("_provenance", "Read by FlujoOracleRunner from the private field valoresItemCalculoHash of the UNMODIFIED legacy "
                + "cl.bcs.custodia.ConsultarFlujoCustodia (setItemCalculoHash, ConsultarFlujoCustodia.java:2218-2279). Do not edit by hand.");
        o.addProperty("items", orden.size());
        o.addProperty("fuentes", new java.util.TreeSet<>(orden.values()).size());
        JsonObject mapa = new JsonObject();
        for (java.util.Map.Entry<String, String> e : orden.entrySet()) mapa.addProperty(e.getKey(), e.getValue());
        o.add("mapa", mapa);
        Files.createDirectories(base.resolve("unit-goldens"));
        Files.write(base.resolve("unit-goldens").resolve("item-fuente.json"), (FlujoCaso.PRETTY.toJson(o) + '\n').getBytes(StandardCharsets.UTF_8));
        System.out.println("unit golden unit-goldens/item-fuente.json: " + orden.size() + " items");
    }

    static JsonObject legacy(java.sql.Connection con, JsonObject entrada, FlujoGuion guion) throws Exception {
        resetStatics();
        Doubles.use(guion);
        return new cl.bcs.custodia.ConsultarFlujoCustodia().call(con, entrada);
    }

    /** the legacy keeps static caches: every case starts from the state of a fresh JVM */
    static void resetStatics() throws Exception {
        Class<?> d = Class.forName("cl.bcs.plataforma.pla.utils.Diccionario");
        java.lang.reflect.Field f = d.getDeclaredField("diccionariosCargados");
        f.setAccessible(true);
        ((java.util.List<?>) f.get(null)).clear();
        java.lang.reflect.Field r = d.getDeclaredField("registros");
        r.setAccessible(true);
        r.set(null, null);
        cl.bcs.utils.Fecha.setLocalOffSet(0);
        cl.bcs.plataforma.pla.utils.ConfiguracionSistema.limpiarParametros();
    }
}
