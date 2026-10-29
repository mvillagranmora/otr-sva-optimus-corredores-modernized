import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cl.bcs.cuentainversion.reportes.support.ArchivoDump;
import cl.bcs.cuentainversion.reportes.support.Dump;
import cl.bcs.cuentainversion.reportes.support.Mask;
import cl.bcs.oracle.Colaboradores;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import cl.bcs.utils.Utils;

/**
 * Records golden expectations by executing the UNMODIFIED legacy classes of BcsCuentaInversionServicios (package
 * cl.bcs.cuentaInversion) against H2 (MODE=LEGACY) seeded with each case, with the shims of legacy-oracle/shims/ standing
 * in for the platform runtime and for collaborators outside this suite.
 *
 * Usage: java OracleRunner <reportesResourcesDir> [caseNamePrefix]   (JDK 25; see ../../../java/.../reportes/README.md)
 * Writes <case>/expected.json:
 *   salida                    JsonObject returned by JavaCall.call(conn, entrada)   (or "excepcion": class + message)
 *   tablas                    only the tables whose rows changed (Dump.cambios), as seeded -> end of call
 *   llamadasColaboradores     calls made to the shimmed collaborators (ConsultarSaldo, CalcularParidad, ...)
 *   auditoria                 entradas sent to the platform audit service (AgregarLog)
 *   log                       lines written with cl.bcs.utils.Log
 *   secuenciasSolicitadas     "name#cantidad" per Utils.getSecuencia call
 *   archivos                  generated files (ArchivoDump), only when the case generates files
 *   pdfTextos                 texts shown in PDFs by the PDFBox shim (the PDF layout itself is not reproduced)
 * Transaction model (assumption, documented): autocommit off; commit when call() returns, rollback when it throws.
 * Nothing real is contacted: every connection value is H2 in memory, every credential-shaped value is fake.
 */
public class OracleRunner {

    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    static int dbCounter = 0;
    static Path base;

    public static void main(String[] args) throws Exception {
        // The legacy servers run in Chile; java.util.Date <-> DATE conversions depend on the JVM zone.
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Santiago"));
        java.util.Locale.setDefault(java.util.Locale.US); // number/date formats of generated files must not depend on the machine
        base = Path.of(args[0]).toAbsolutePath();
        String prefix = args.length > 1 ? args[1] : "";
        try (var dirs = Files.list(base.resolve("cases"))) {
            for (Path dir : dirs.sorted().toList()) {
                if (!Files.isDirectory(dir) || !dir.getFileName().toString().startsWith(prefix)) continue;
                System.out.println("== " + dir.getFileName());
                JsonObject expected = run(dir);
                Files.writeString(dir.resolve("expected.json"), PRETTY.toJson(expected) + "\n", StandardCharsets.UTF_8);
            }
        }
    }

    static JsonObject run(Path dir) throws Exception {
        JsonObject caso = JsonParser.parseString(Files.readString(dir.resolve("case.json"))).getAsJsonObject();
        String clase = caso.get("clase").getAsString();
        int n = ++dbCounter;
        Path work = Path.of(System.getProperty("oracle.work", System.getProperty("user.dir"))).toAbsolutePath();
        Path out = work.resolve("out" + n);
        Files.createDirectories(out);
        if (caso.has("directorios")) {
            for (JsonElement d : caso.getAsJsonArray("directorios")) Files.createDirectories(out.resolve(d.getAsString()));
        }
        String outStr = out.toString().replace('\\', '/');

        Connection conn = DriverManager.getConnection("jdbc:h2:mem:oracle" + n + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
        runScript(conn, base.resolve("schema-reportes.sql"), outStr);
        conn.setAutoCommit(false);
        runScript(conn, base.resolve("seed/base-reportes.sql"), outStr);
        if (caso.has("seeds")) {
            for (JsonElement sd : caso.getAsJsonArray("seeds")) runScript(conn, base.resolve("seed/" + sd.getAsString()), outStr);
        }
        if (Files.exists(dir.resolve("seed.sql"))) runScript(conn, dir.resolve("seed.sql"), outStr);
        conn.commit();

        resetEstatico();
        Colaboradores.reset(caso.has("colaboradores") ? caso.getAsJsonObject("colaboradores") : null);
        Utils.SECUENCIA_LLAMADAS.clear();
        AgregarLog.AUDITORIA.clear();
        Log.LINES.clear();
        PDPageContentStream.TEXTOS.clear();

        JsonObject expected = new JsonObject();
        expected.addProperty("_provenance", "Recorded by legacy-oracle/runner/OracleRunner.java executing the unmodified legacy class "
                + "cl.bcs.cuentaInversion." + clase + " on H2 MODE=LEGACY. Do not edit by hand; re-record.");

        JsonObject before = Dump.snapshot(conn);
        JsonObject entrada = JsonParser.parseString(caso.getAsJsonObject("entrada").toString().replace("${OUT}", outStr)).getAsJsonObject();
        try {
            Class<?> c = Class.forName("cl.bcs.cuentaInversion." + clase);
            JavaCall jc = (JavaCall) c.getDeclaredConstructor().newInstance();
            JsonObject salida;
            if (caso.has("invocacion")) {
                // classes whose JavaCall.call() is not implemented (returns null): invoke logica(Connection, Entrada) directly
                JsonObject inv = caso.getAsJsonObject("invocacion");
                Class<?> tipoEntrada = Class.forName("cl.bcs.cuentaInversion." + clase + "$" + inv.get("entrada").getAsString());
                Object ent = Utils.getGsonBuilder().fromJson(entrada, tipoEntrada);
                Object res = c.getMethod(inv.get("metodo").getAsString(), Connection.class, tipoEntrada).invoke(jc, conn, ent);
                salida = JsonParser.parseString(Utils.getGsonBuilder().toJson(res)).getAsJsonObject();
            } else {
                salida = jc.call(conn, entrada.deepCopy());
            }
            conn.commit();
            expected.add("salida", salida == null ? com.google.gson.JsonNull.INSTANCE : JsonParser.parseString(Mask.texto(salida.toString().replace(outStr, "${OUT}"))));
        } catch (InvocationTargetException ite) {
            conn.rollback();
            expected.addProperty("excepcion", describir(ite.getCause()));
        } catch (Throwable t) {
            conn.rollback();
            expected.addProperty("excepcion", describir(t));
        }
        expected.add("tablas", Dump.cambios(before, Dump.snapshot(conn)));
        expected.add("llamadasColaboradores", PRETTY.toJsonTree(Colaboradores.LLAMADAS));
        JsonArray aud = new JsonArray();
        for (String a : AgregarLog.AUDITORIA) aud.add(JsonParser.parseString(a));
        expected.add("auditoria", aud);
        JsonArray log = new JsonArray();
        for (String l : Log.LINES) log.add(new JsonPrimitive(Mask.texto(l.replace(outStr, "${OUT}"))));
        expected.add("log", log);
        expected.add("secuenciasSolicitadas", PRETTY.toJsonTree(Utils.SECUENCIA_LLAMADAS));
        if (!PDPageContentStream.TEXTOS.isEmpty()) {
            JsonArray pdf = new JsonArray();
            for (String t : PDPageContentStream.TEXTOS) pdf.add(new JsonPrimitive(t));
            expected.add("pdfTextos", pdf);
        }
        JsonObject archivos = ArchivoDump.directorio(out);
        if (archivos.size() > 0) expected.add("archivos", archivos);
        conn.close();
        return expected;
    }

    static String describir(Throwable t) {
        String m = String.valueOf(t.getMessage()).replaceAll("[A-Za-z]:\\\\\\S*", "<path>");
        return t.getClass().getName() + ": " + m;
    }

    /** Legacy classes keep static caches (Diccionario, ConfiguracionSistema, MonedaFormato); reset them per case. */
    static void resetEstatico() throws Exception {
        try {
            Class<?> d = Class.forName("cl.bcs.plataforma.pla.utils.Diccionario");
            var f1 = d.getDeclaredField("diccionariosCargados");
            f1.setAccessible(true);
            ((List<?>) f1.get(null)).clear();
            var f2 = d.getDeclaredField("registros");
            f2.setAccessible(true);
            f2.set(null, null);
        } catch (ClassNotFoundException ignored) {
            // not on the classpath of this run
        }
        try {
            Class<?> c = Class.forName("cl.bcs.plataforma.pla.utils.ConfiguracionSistema");
            c.getMethod("limpiarParametros").invoke(null);
        } catch (ClassNotFoundException ignored) {
            // not on the classpath of this run
        }
        try {
            Class<?> m = Class.forName("cl.bcs.plataforma.pla.utils.MonedaFormato");
            var f = m.getDeclaredField("formatos");
            f.setAccessible(true);
            f.set(null, null);
        } catch (ClassNotFoundException ignored) {
            // not on the classpath of this run
        }
    }

    static void runScript(Connection conn, Path file, String out) throws Exception {
        String sql = Files.readString(file, StandardCharsets.UTF_8).replace("${OUT}", out);
        StringBuilder clean = new StringBuilder();
        for (String line : sql.split("\n")) {
            if (line.strip().startsWith("--")) continue;
            clean.append(line).append('\n');
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : clean.toString().split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }
}
