package cl.bcs.fondomutuo.seguridad;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * RULE-076: no legacy test main() and no credential or JDBC literal in src/main. The legacy {@code ManejadorSolicitudAporteRescate.main} and
 * {@code CrearSolicitudAporteRescateNormal.main} carry {@code PlataformaUtils.getDESAConnection()} (HANA host, user {@code desa01}) and a JSON with real ids:
 * none of that was copied. Scans the WHOLE of src/main (the ~130 stubs out of the context included). Fails (never skips) when src/main is not found.
 */
class Rule076SinCredencialesNiMainTest {

    private static final Path MAIN = Path.of("src", "main");
    private static final Pattern MAIN_METHOD = Pattern.compile("static\\s+void\\s+main\\s*\\(");
    private static final Pattern JDBC_LITERAL = Pattern.compile("jdbc:[a-z0-9]+:", Pattern.CASE_INSENSITIVE);
    private static final Pattern LEGACY_CONNECTION = Pattern.compile("getDESAConnection|getQAConnection|desa01|B0lsa123|BasicDataSource", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECRET_LITERAL = Pattern.compile(
            "(password|passwd|pwd|secret|api[_-]?key|token)[\"']?\\s*[:=,]\\s*\"[^\"$\\s]{3,}\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECRET_PROPERTY = Pattern.compile(
            "^\\s*[\\w.\\-]*(password|passwd|secret|api[_-]?key|token)[\\w.\\-]*\\s*[:=]\\s*(?!\\$\\{)\\S+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static List<Path> archivos(String... ext) throws IOException {
        assertThat(MAIN).as("RULE-076 needs src/main (run the tests from the module directory)").isDirectory();
        try (Stream<Path> s = Files.walk(MAIN)) {
            return s.filter(Files::isRegularFile).filter(p -> {
                for (String e : ext) if (p.toString().endsWith(e)) return true;
                return false;
            }).toList();
        }
    }

    private static String leer(Path p) throws IOException {
        return Files.readString(p, StandardCharsets.ISO_8859_1);
    }

    @Test
    void rule076_soloLaClaseSpringApplicationTieneMain() throws IOException {
        List<String> conMain = new ArrayList<>();
        for (Path p : archivos(".java")) if (MAIN_METHOD.matcher(leer(p)).find()) conMain.add(p.getFileName().toString());
        assertThat(conMain).containsExactly("BcsFondoMutuoApplication.java");
    }

    @Test
    void rule076_sinLiteralesJdbcNiConexionDeDesarrolloDelLegacy() throws IOException {
        List<String> culpables = new ArrayList<>();
        for (Path p : archivos(".java", ".properties", ".yml", ".yaml", ".xml", ".json", ".sql")) {
            String t = leer(p);
            if (JDBC_LITERAL.matcher(t).find() || LEGACY_CONNECTION.matcher(t).find()) culpables.add(p.toString());
        }
        assertThat(culpables).as("files with a jdbc: literal or the legacy dev connection").isEmpty();
    }

    @Test
    void rule076_sinCredencialesLiterales() throws IOException {
        List<String> culpables = new ArrayList<>();
        for (Path p : archivos(".java")) if (SECRET_LITERAL.matcher(leer(p)).find()) culpables.add(p.toString());
        for (Path p : archivos(".properties", ".yml", ".yaml")) if (SECRET_PROPERTY.matcher(leer(p)).find()) culpables.add(p.toString());
        assertThat(culpables).as("files with a hard-coded credential").isEmpty();
    }

    @Test
    void rule076_laConexionYElTokenVienenDelEntorno_yNoHayUrlsFicticias() throws IOException {
        String y = Files.readString(MAIN.resolve("resources/application.yml"));
        assertThat(y).contains("${HANA_URL}").contains("${HANA_USER}").contains("${HANA_PASSWORD}").contains("${INTERNAL_TOKEN:}").contains("${JAVACALL_URL}");
        assertThat(y).doesNotContain("localhost:820").doesNotContain("PARIDAD_URL").doesNotContain("CRYSTAL_REPORTS_URL");
    }

    @Test
    void rule076_rescateReinversionF2W1Frente2_sinElMainDelLegacyNiSusIds() throws IOException {
        // legacy CrearSolicitudRescateReinversion.main (RR:1594-1615): PlataformaUtils.getPropertiesConnection("master01", "connections.properties") and a
        // JSON with real client / portfolio ids (43003, 388037) and series: none of that was copied
        Pattern legado = Pattern.compile("getPropertiesConnection|master01|connections\\.properties|43003|388037|00240AUSD|00103AUSD");
        List<Path> f2 = archivos(".java").stream().filter(p -> p.toString().replace('\\', '/').contains("/fondomutuo/rescatereinversion/")).toList();
        assertThat(f2.stream().map(p -> p.getFileName().toString())).contains("CrearSolicitudRescateReinversionService.java", "RreArmado.java",
                "BloqueoReinversionJdbc.java", "RescateReinversionJavaCallHandler.java", "RescateReinversionDestino.java");
        for (Path p : f2) {
            String t = leer(p);
            assertThat(MAIN_METHOD.matcher(t).find() || LEGACY_CONNECTION.matcher(t).find() || JDBC_LITERAL.matcher(t).find() || legado.matcher(t).find())
                    .as(p.toString()).isFalse();
        }
    }

    @Test
    void rule076_lasClasesF2W1EstanEnElBarrido_ySinElMainDelLegacy() throws IOException {
        // F2 W1: ProcesarIngresoEgresoTesoreria legacy carries main() with PlataformaUtils.getQAConnection() and a JSON with real ids: not copied
        List<Path> f2 = archivos(".java").stream().filter(p -> p.toString().replace('\\', '/').contains("/fondomutuo/tesoreria/")).toList();
        assertThat(f2.stream().map(p -> p.getFileName().toString())).contains("ProcesarIngresoEgresoTesoreriaService.java", "TesoreriaArmado.java",
                "TesoreriaJdbc.java", "ColaboradoresTesoreria.java", "TesoreriaEnProcesoConfiguration.java");
        for (Path p : f2) {
            String t = leer(p);
            assertThat(MAIN_METHOD.matcher(t).find() || LEGACY_CONNECTION.matcher(t).find() || JDBC_LITERAL.matcher(t).find()).as(p.toString()).isFalse();
        }
    }
}
