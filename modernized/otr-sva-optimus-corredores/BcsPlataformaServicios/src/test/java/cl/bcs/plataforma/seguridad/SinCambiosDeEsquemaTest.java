package cl.bcs.plataforma.seguridad;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Project rule: NO database change of any kind. The only SQL of ConsultarEstadoModulo is the legacy SELECT on PAU.Modulo; this test pins that the
 * adapter holds exactly one statement, that it is a SELECT, and that no DDL/DML/CALL keyword appears in the adapter or in the new
 * JavaCall package. (The 1.090 SQL resources of the skeleton belong to the stubs that are out of the context; they are not scanned here.)
 */
class SinCambiosDeEsquemaTest {

    private static final Pattern PROHIBIDAS = Pattern.compile(
            "\\b(CREATE|ALTER|DROP|TRUNCATE|GRANT|REVOKE|CALL|INSERT|UPDATE|DELETE|MERGE|UPSERT|REPLACE\\s+INTO)\\b", Pattern.CASE_INSENSITIVE);

    @Test
    void elUnicoSqlEsElSelectLegacySobrePauModulo() throws Exception {
        var f = cl.bcs.plataforma.adapter.out.jdbc.ModuloJdbcRepository.class.getDeclaredField("SQL_SELECT_MODULOEMPRESA");
        f.setAccessible(true);
        String sql = (String) f.get(null);
        assertThat(sql).isEqualTo("SELECT IFNULL(\"IdEstado\",'ABT') AS \"IdEstadoModulo\", IFNULL(\"CodigoModulo\",'') as \"NombreModulo\" "
                + "FROM \"BSCL.Plataforma.BD::PAU.Modulo\" where \"IdEmpresa\" = :IdEmpresa");
        assertThat(sql.split(":IdEmpresa", -1)).hasSize(2);
        assertThat(PROHIBIDAS.matcher(sql).find()).isFalse();
    }

    @Test
    void elAdaptadorYElPaqueteJavaCallNoTienenDdlNiDml() throws Exception {
        List<Path> fuentes;
        try (Stream<Path> s = Stream.concat(
                Files.walk(Path.of("src/main/java/cl/bcs/plataforma/adapter/in/javacall")),
                Stream.concat(Files.walk(Path.of("src/main/java/cl/bcs/plataforma/cre")),
                        Files.walk(Path.of("src/main/java/cl/bcs/plataforma/adapter/out/jdbc"))))) {
            fuentes = s.filter(p -> p.toString().endsWith("Handler.java") || p.toString().contains("ModuloJdbc")
                    || p.toString().endsWith("ConsultarEstadoModuloService.java")).toList();
        }
        assertThat(fuentes).hasSizeGreaterThanOrEqualTo(3);
        for (Path p : fuentes) {
            // only string literals can hold SQL: drop comments first
            String t = Files.readString(p, StandardCharsets.UTF_8).replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\n]*", "");
            java.util.regex.Matcher lit = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(t);
            while (lit.find()) {
                assertThat(PROHIBIDAS.matcher(lit.group(1)).find()).as(p + " literal: " + lit.group(1)).isFalse();
            }
        }
    }
}
