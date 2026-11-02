package cl.bcs.fondomutuo.certificacion;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * verify 2026-11-02 T4-C (read-only driver, analysis side) for the FRESH inputs of CertificarSolicitudAporteRescate (T1..T5, whole runs A..J). For every case directory under {@code <cases>} it writes the
 * canonical text (the same sections and rules as {@code CerCosturaT5.compararTodo}, through {@code CerCosturaT5.txt / txtN / proyectar}) of
 *  - the golden RECORDED from the unmodified legacy by legacy-oracle/record_cer_t5.sh ({@code <cases>/<id>/expected.json}) into {@code <legacyOut>/<id>/out.txt}, and
 *  - the run of the NEW production JavaCall handler (CertificarAporteRescateJavaCallHandler, its list OPEN) over the facade through the module's own harness ({@code CasoCerT5.correr}, every gate open) into {@code <newOut>/<id>/out.txt}.
 * Nothing is relaxed. An exception is written by CLASS (the message only for a plain {@code java.lang.Exception}), as the test does.
 *
 *   java -cp driver;test-classes;classes;deps FrescoCerHandler &lt;cases&gt; &lt;legacyOut&gt; &lt;newOut&gt;
 */
public class FrescoCerHandler {

    public static void main(String[] a) throws Exception {
        Path cases = Path.of(a[0]);
        Path legacyOut = Path.of(a[1]);
        Path newOut = Path.of(a[2]);
        List<Path> dirs = new ArrayList<>();
        try (Stream<Path> s = Files.list(cases)) {
            s.filter(d -> Files.exists(d.resolve("case.json"))).sorted().forEach(dirs::add);
        }
        Method correr = CasoCerT5.class.getDeclaredMethod("correr", Path.class, CasoCerT5.Opciones.class);
        correr.setAccessible(true);
        java.util.TimeZone zona = java.util.TimeZone.getDefault();
        java.util.Locale idioma = java.util.Locale.getDefault();
        int n = 0;
        for (Path d : dirs) {
            String id = d.getFileName().toString();
            Path golden = d.resolve("expected.json");
            if (!Files.exists(golden)) {
                System.out.println(id + " :: NO LEGACY GOLDEN (the oracle refused or the case is unstable): left out");
                continue;
            }
            JsonObject legacy = JsonParser.parseString(Files.readString(golden, StandardCharsets.UTF_8)).getAsJsonObject();
            CasoCerT5.Corrida corrida;
            try {
                java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
                java.util.Locale.setDefault(java.util.Locale.ENGLISH);
                org.h2.util.DateTimeUtils.resetCalendar();
                corrida = (CasoCerT5.Corrida) correr.invoke(null, d, CasoCerT5.Opciones.abiertas().porHandler());
            } finally {
                java.util.TimeZone.setDefault(zona);
                java.util.Locale.setDefault(idioma);
                org.h2.util.DateTimeUtils.resetCalendar();
            }
            escribir(legacyOut.resolve(id), legacy);
            escribir(newOut.resolve(id), corrida.volcado);
            n++;
            System.out.println(id + " :: written (decision legacy " + legacy.get("decisionServlet") + ", new " + corrida.volcado.get("decisionServlet") + ")");
        }
        System.out.println("fresh cases written: " + n);
    }

    private static void escribir(Path dir, JsonObject r) throws Exception {
        Files.createDirectories(dir);
        StringBuilder sb = new StringBuilder();
        sb.append("== tablasIniciales\n").append(CerCosturaT5.txt(r.get("tablasIniciales"))).append('\n');
        sb.append("== fin\n");
        if (r.has("excepcion")) {
            String e = r.get("excepcion").getAsString();
            String clase = e.substring(0, e.indexOf(':'));
            sb.append(clase.equals("Exception") ? e : clase).append('\n');
        } else {
            sb.append(CerCosturaT5.txtN(r.get("salida"))).append('\n');
        }
        sb.append("== decisionServlet\n").append(r.get("decisionServlet").getAsString()).append('\n');
        sb.append("== tablasTrasLaLlamada\n").append(CerCosturaT5.txt(r.get("tablasTrasLaLlamada"))).append('\n');
        sb.append("== tablas\n").append(CerCosturaT5.txt(r.get("tablas"))).append('\n');
        sb.append("== colaboradores\n").append(CerCosturaT5.txtN(CerCosturaT5.proyectar(r.getAsJsonArray("colaboradores")))).append('\n');
        sb.append("== secuenciasSolicitadas\n").append(CerCosturaT5.txt(r.get("secuenciasSolicitadas"))).append('\n');
        sb.append("== sesion\n").append(CerCosturaT5.txt(r.get("sesion"))).append('\n');
        Files.writeString(dir.resolve("out.txt"), sb.toString(), StandardCharsets.UTF_8);
    }
}
