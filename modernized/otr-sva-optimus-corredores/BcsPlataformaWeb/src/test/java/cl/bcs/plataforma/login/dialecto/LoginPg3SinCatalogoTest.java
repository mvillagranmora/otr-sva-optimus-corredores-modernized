package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * A3-1 the equivalence catalog is NOT a run-time resource of the project (decision of the owner): the table (resumen_exportacion.xlsx and the CSV made from
 * it) exists only for development. Structure tests over production: the four classes of the catalog are gone, no procedure-definition class reads a CSV,
 * no {@code equivalencias} folder and no {@code .csv} anywhere under src/main/resources, and the statements are plain resources
 * ({@code sql/hana/<id>.sql}, {@code sql/postgres/<id>.sql}, {@code sql/indice.txt}) with nothing else under {@code sql/}. They fail with
 * {@code LOGIN-PG3 catalog still in production: ...} while the catalog is there and with {@code LOGIN-PG3 not implemented: ...} for what the redesign adds.
 */
class LoginPg3SinCatalogoTest {

    private static final Path JAVA = Path.of("src", "main", "java");
    private static final Path DIALECTO = JAVA.resolve("cl/bcs/plataforma/login/dialecto");
    private static final Path RECURSOS = Path.of("src", "main", "resources");

    // ------------------------------------------------------------------------------------------------ the classes

    @ParameterizedTest(name = "A3-1 the class {0} is gone from the classpath of main (the catalog classes are not in production)")
    @ValueSource(strings = { "CatalogoEquivalencias", "LectorCsv", "FichaDeColumna", "ObjetoNoDisponibleException" })
    void la_clase_no_existe(String simple) {
        if (ApiLoginPg3.existe(simple)) throw ApiLoginPg3.catalogoEnProduccion("the class " + ApiLoginPg3.PAQUETE + simple + " is still on the classpath of main");
    }

    @ParameterizedTest(name = "A3-1 the source file {0}.java is gone from src/main/java")
    @ValueSource(strings = { "CatalogoEquivalencias", "LectorCsv", "FichaDeColumna", "ObjetoNoDisponibleException" })
    void el_fuente_no_existe(String simple) {
        assertThat(DIALECTO).as("the dialect package of main").isDirectory();
        if (Files.exists(DIALECTO.resolve(simple + ".java"))) throw ApiLoginPg3.catalogoEnProduccion("src/main/java/.../dialecto/" + simple + ".java is still there");
    }

    private static List<Path> fuentes() throws IOException {
        try (Stream<Path> s = Files.walk(JAVA)) {
            return s.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    @DisplayName("A3-1 no production source refers to the catalog classes, to a CSV file or to the equivalencias folder (string literals and identifiers): nothing in production can read the table at run time")
    void ningun_fuente_lee_el_catalogo() throws IOException {
        Pattern identificadores = Pattern.compile("\\b(CatalogoEquivalencias|LectorCsv|FichaDeColumna|ObjetoNoDisponibleException)\\b");
        Pattern literales = Pattern.compile("\"[^\"\\n]*(\\.csv|equivalencias)[^\"\\n]*\"", Pattern.CASE_INSENSITIVE);
        List<String> hallazgos = new ArrayList<>();
        List<Path> todos = fuentes();
        assertThat(todos).as("sources scanned").hasSizeGreaterThan(50);
        for (Path f : todos) {
            String t = Files.readString(f, StandardCharsets.UTF_8);
            Matcher a = identificadores.matcher(t);
            while (a.find()) hallazgos.add(f.getFileName() + " mentions " + a.group());
            Matcher b = literales.matcher(t);
            while (b.find()) hallazgos.add(f.getFileName() + " has the literal " + b.group());
        }
        if (!hallazgos.isEmpty()) throw ApiLoginPg3.catalogoEnProduccion(String.join("; ", new TreeSet<>(hallazgos)));
    }

    // ------------------------------------------------------------------------------------------------ the resources

    @Test
    @DisplayName("A3-1 no folder named equivalencias and no .csv (nor .xlsx, .xls) anywhere under src/main/resources; the production classpath has no such resource either")
    void sin_recursos_del_catalogo() throws IOException {
        assertThat(RECURSOS).as("main resources").isDirectory();
        List<String> hallazgos = new ArrayList<>();
        try (Stream<Path> s = Files.walk(RECURSOS)) {
            for (Path p : (Iterable<Path>) s::iterator) {
                String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
                if (n.equals("equivalencias") || n.endsWith(".csv") || n.endsWith(".xlsx") || n.endsWith(".xls")) hallazgos.add(RECURSOS.relativize(p).toString());
            }
        }
        if (!hallazgos.isEmpty()) throw ApiLoginPg3.catalogoEnProduccion("resources of the table are still in src/main/resources: " + hallazgos);
        for (String r : List.of("/equivalencias/equivalencias-objetos.csv", "/equivalencias/equivalencias-columnas.csv", "/equivalencias/procedimientos-postgres.csv")) {
            if (LoginPg3SinCatalogoTest.class.getResource(r) != null) throw ApiLoginPg3.catalogoEnProduccion("the classpath still serves " + r);
        }
    }

    @Test
    @DisplayName("A3-1 under src/main/resources/sql there is nothing but hana/, postgres/ and indice.txt (no sql/comun, no other folder, no loose file)")
    void la_carpeta_sql_solo_tiene_lo_acordado() throws IOException {
        Path sql = RECURSOS.resolve("sql");
        assertThat(sql).isDirectory();
        Set<String> hijos = new TreeSet<>();
        try (Stream<Path> s = Files.list(sql)) {
            s.forEach(p -> hijos.add(p.getFileName().toString()));
        }
        assertEquals(Set.of("hana", "postgres", "indice.txt"), hijos);
    }

    @ParameterizedTest(name = "A3-3 sql/{0}/ has exactly one <id>.sql per statement of the index, no more and no fewer (the 55 ids of the golden)")
    @ValueSource(strings = { "hana", "postgres" })
    void un_archivo_por_sentencia_y_dialecto(String dialecto) throws IOException {
        Set<String> ids = new TreeSet<>(LoginPgRecursos.goldenSql().keySet());
        assertEquals(55, ids.size());
        Path dir = RECURSOS.resolve("sql").resolve(dialecto);
        assertThat(dir).as("sql/" + dialecto).isDirectory();
        Set<String> existentes = new TreeSet<>();
        try (Stream<Path> s = Files.list(dir)) {
            s.forEach(p -> existentes.add(p.getFileName().toString()));
        }
        Set<String> esperados = new TreeSet<>();
        ids.forEach(id -> esperados.add(id + ".sql"));
        Set<String> faltan = new TreeSet<>(esperados);
        faltan.removeAll(existentes);
        if (!faltan.isEmpty()) throw ApiLoginPg3.pendiente("sql/" + dialecto + " lacks " + faltan);
        Set<String> sobran = new TreeSet<>(existentes);
        sobran.removeAll(esperados);
        assertThat(sobran).as("files in sql/" + dialecto + " that are not a statement of the index").isEmpty();
    }

    @Test
    @DisplayName("A3-3 sql/indice.txt lists exactly the 55 ids of the golden, one per line, none twice")
    void el_indice_es_el_del_golden() throws IOException {
        List<String> ids = new ArrayList<>();
        for (String l : Files.readString(RECURSOS.resolve("sql/indice.txt"), StandardCharsets.UTF_8).split("\\R")) if (!l.isBlank()) ids.add(l.strip());
        assertThat(ids).hasSize(55).doesNotHaveDuplicates().containsExactlyInAnyOrderElementsOf(LoginPgRecursos.goldenSql().keySet());
    }

    // ------------------------------------------------------------------------------------------------ the procedures

    @Test
    @DisplayName("A3-5 ProcedimientosPostgres has ONE public constructor and it takes no parameters (the definitions of the eight procedures are Java code, not a file)")
    void procedimientos_con_constructor_sin_parametros() {
        Class<?> c = ApiLoginPg3.clase("ProcedimientosPostgres");
        if (c.getConstructors().length != 1 || c.getConstructors()[0].getParameterCount() != 0) {
            throw ApiLoginPg3.pendiente("ProcedimientosPostgres must have exactly one public constructor and it takes no parameters, found " + java.util.Arrays.toString(c.getConstructors()));
        }
        assertThat(cl.bcs.plataforma.login.ProcedimientoAlmacenado.class.isAssignableFrom(c)).isTrue();
    }

    @Test
    @DisplayName("A3-5 the selector ProcedimientosPorDialecto still exists (A2-2 unchanged): a ProcedimientoAlmacenado with the public constructor (SqlDialecto, hana, postgres)")
    void el_selector_sigue() {
        Class<?> c = ApiLoginPg3.clase("ProcedimientosPorDialecto");
        assertThat(cl.bcs.plataforma.login.ProcedimientoAlmacenado.class.isAssignableFrom(c)).isTrue();
        boolean hay = false;
        for (var k : c.getConstructors()) {
            hay |= k.getParameterCount() == 3 && k.getParameterTypes()[0] == SqlDialecto.class;
        }
        if (!hay) throw ApiLoginPg3.pendiente("ProcedimientosPorDialecto has no public constructor (SqlDialecto, ProcedimientoAlmacenado, ProcedimientoAlmacenado)");
    }
}
