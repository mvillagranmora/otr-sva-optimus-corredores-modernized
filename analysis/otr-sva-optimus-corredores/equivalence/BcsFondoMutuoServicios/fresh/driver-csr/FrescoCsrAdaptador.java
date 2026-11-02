package cl.bcs.fondomutuo.aportereinversion;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * verify 2026-11-02 T4-C (read-only driver, analysis side) for the FRESH inputs of CrearSolicitudAporteReinversion (CSR). For every case directory under {@code <cases>} it writes the canonical
 * text (the same sections and rules as {@code CsrEquivalenciaTest}, through {@code CsrCostura.texto}) of
 *  - the golden RECORDED from the unmodified legacy by legacy-oracle/record_csr.sh ({@code <cases>/<id>/expected.json}) into {@code <legacyOut>/<id>/out.txt}, and
 *  - the run of the NEW production code through the module's own harness ({@code CasoCsr.ejecutar}, profile ABIERTAS, every gate open) into {@code <newOut>/<id>/out.txt}.
 * Nothing is relaxed. An exception is written by CLASS (the message only for a plain {@code java.lang.Exception}), as the test does.
 *
 *   java -cp driver;test-classes;classes;deps FrescoCsrAdaptador &lt;cases&gt; &lt;legacyOut&gt; &lt;newOut&gt;
 */
public class FrescoCsrAdaptador {

    private static final String[] CLAVES = { "decisionServlet", "salidaCruda", "tablas", "colaboradores", "secuenciasSolicitadas", "sesion" };

    public static void main(String[] a) throws Exception {
        Path cases = Path.of(a[0]);
        Path legacyOut = Path.of(a[1]);
        Path newOut = Path.of(a[2]);
        List<Path> dirs = new ArrayList<>();
        try (Stream<Path> s = Files.list(cases)) {
            s.filter(d -> Files.exists(d.resolve("case.json"))).sorted().forEach(dirs::add);
        }
        int n = 0;
        for (Path d : dirs) {
            String id = d.getFileName().toString();
            Path golden = d.resolve("expected.json");
            if (!Files.exists(golden)) {
                System.out.println(id + " :: NO LEGACY GOLDEN (the oracle refused or the case is unstable): left out");
                continue;
            }
            JsonObject legacy = JsonParser.parseString(Files.readString(golden, StandardCharsets.UTF_8)).getAsJsonObject();
            java.lang.reflect.Method m = CasoCsr.class.getDeclaredMethod("ejecutar", Path.class, CasoCsr.Perfil.class, java.util.function.Consumer.class, JsonObject.class, boolean.class);
            m.setAccessible(true);
            JsonObject nuevo = (JsonObject) m.invoke(null, d, CasoCsr.Perfil.ABIERTAS, null, null, true);   // T4-C: through AporteReinversionJsonAdapter
            escribir(legacyOut.resolve(id), legacy);
            escribir(newOut.resolve(id), nuevo);
            n++;
            System.out.println(id + " :: written (decision legacy " + legacy.get("decisionServlet") + ", new " + nuevo.get("decisionServlet") + ")");
        }
        System.out.println("fresh cases written: " + n);
    }

    private static void escribir(Path dir, JsonObject r) throws Exception {
        Files.createDirectories(dir);
        StringBuilder sb = new StringBuilder();
        for (String k : CLAVES) {
            sb.append("== ").append(k).append('\n').append(CsrCostura.texto(r.get(k))).append('\n');
        }
        sb.append("== excepcion\n");
        if (r.has("excepcion")) {
            String e = r.get("excepcion").getAsString();
            String clase = e.substring(0, e.indexOf(':'));
            sb.append(clase.equals("Exception") ? e : clase).append('\n');
        } else {
            sb.append("<none>\n");
        }
        Files.writeString(dir.resolve("out.txt"), sb.toString(), StandardCharsets.UTF_8);
    }
}
