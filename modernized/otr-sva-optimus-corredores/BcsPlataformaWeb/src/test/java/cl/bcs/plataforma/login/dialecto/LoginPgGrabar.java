package cl.bcs.plataforma.login.dialecto;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Writes the goldens of the HANA code (src/test/resources/loginpg/golden-hana) from the CURRENT production code. It runs only when the system
 * property {@code loginpg.grabar} names the output directory (LoginPgSqlHanaTest calls it first), and must be used ONLY on code that has not been
 * given the dialect layer yet (or in HANA mode of the throwaway copy): the goldens are the oracle, regenerating them from refactored code would make
 * the test compare the code with itself. One file per statement, byte-exact, UTF-8, no trailing newline added.
 */
public final class LoginPgGrabar {

    private LoginPgGrabar() {
    }

    public static void siHaySolicitud() throws Exception {
        String dir = System.getProperty("loginpg.grabar");
        if (dir == null || dir.isBlank()) return;
        Path out = Path.of(dir);
        Files.createDirectories(out);
        Map<String, String> constantes = LoginPgFlujos.capturarConstantes();
        StringBuilder indice = new StringBuilder();
        for (Map.Entry<String, String> e : constantes.entrySet()) {
            Files.write(out.resolve(e.getKey() + ".sql"), e.getValue().getBytes(StandardCharsets.UTF_8));
            indice.append(e.getKey()).append('\n');
        }
        Files.writeString(out.resolve("INDEX.txt"), indice.toString(), StandardCharsets.UTF_8);
        JsonObject flujos = new JsonObject();
        for (Map.Entry<String, List<String>> e : LoginPgFlujos.capturarFlujos().entrySet()) {
            JsonArray a = new JsonArray();
            e.getValue().forEach(a::add);
            flujos.add(e.getKey(), a);
        }
        Files.writeString(out.resolve("enviado-por-flujo.json"), new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(flujos) + "\n",
                StandardCharsets.UTF_8);
    }
}
