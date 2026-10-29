package cl.bcs.apv.familia_c;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.familia_c.support.Caso;
import cl.bcs.apv.familia_c.support.Verifica;

/**
 * Self-check of the family C harness (not an equivalence test: excluded from the dual-execution dump by its name).
 * (1) Inventory: a missing, empty or hand-edited golden FAILS here instead of silently shrinking the suite.
 * (2) The comparator CAN fail: it accepts the golden's own output and rejects an altered output, an altered table, an altered collaborator trace and
 * an altered sequence trace, so a green equivalence test means something.
 */
class FamiliaCSelfCheckTest {

    private static final Path CASOS = Path.of("src/test/resources/apv-c/cases");
    /** Case whose legacy run changed nothing, called no collaborator and requested no sequence (the origin portfolio does not exist): its golden equals the seeded state. */
    private static final String SIN_CAMBIOS = "APV-C-360_portafolio-inexistente-no-hace-nada";

    private static List<Path> directorios() throws IOException {
        try (Stream<Path> s = Files.list(CASOS)) {
            return s.filter(Files::isDirectory).sorted().toList();
        }
    }

    @Test
    void cadaCasoTieneEntradaYGoldenGrabadoPorElOraculo() throws IOException {
        List<String> problemas = new ArrayList<>();
        for (Path d : directorios()) {
            Path c = d.resolve("case.json");
            Path e = d.resolve("expected.json");
            if (!Files.exists(c)) {
                problemas.add(d.getFileName() + ": missing case.json");
            }
            if (!Files.exists(e)) {
                problemas.add(d.getFileName() + ": missing expected.json");
                continue;
            }
            JsonObject g = JsonParser.parseString(Files.readString(e, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!g.has("_provenance") || !g.get("_provenance").getAsString().contains("Recorded by legacy-oracle")) {
                problemas.add(d.getFileName() + ": golden without oracle provenance (hand written?)");
            }
            if (!g.has("salida") && !g.has("excepcion")) {
                problemas.add(d.getFileName() + ": neither salida nor excepcion");
            }
        }
        assertThat(problemas).isEmpty();
        assertThat(directorios()).as("recorded cases (family C records 365 at delivery; fewer means goldens were deleted)").hasSizeGreaterThanOrEqualTo(365);
    }

    @Test
    void losIdentificadoresAPVCSonUnicosYTienenForma() throws IOException {
        Pattern p = Pattern.compile("^APV-C-(\\d{2,3})_.+");
        List<Integer> ids = new ArrayList<>();
        for (Path d : directorios()) {
            var m = p.matcher(d.getFileName().toString());
            assertThat(m.matches()).as(d.getFileName().toString()).isTrue();
            ids.add(Integer.parseInt(m.group(1)));
        }
        assertThat(ids).doesNotHaveDuplicates();
        assertThat(ids).contains(1, 50, 140, 200, 300, 340, 360, 470);
    }

    @Test
    void cadaCasoGrabadoLoReferenciaUnTestCompilado() throws IOException {
        StringBuilder fuentes = new StringBuilder();
        try (Stream<Path> s = Files.list(Path.of("src/test/java/cl/bcs/apv/familia_c"))) {
            for (Path f : s.filter(x -> x.toString().endsWith("Test.java")).toList()) {
                fuentes.append(Files.readString(f, StandardCharsets.UTF_8));
            }
        }
        String texto = fuentes.toString();
        List<String> sinTest = new ArrayList<>();
        for (Path d : directorios()) {
            if (!texto.contains("\"" + d.getFileName() + "\"")) {
                sinTest.add(d.getFileName().toString());
            }
        }
        assertThat(sinTest).as("recorded cases no test refers to").isEmpty();
    }

    @Test
    void losGoldensNoLlevanFechasDelDiaDeGrabacionNiCredenciales() throws IOException {
        // fixtures only use 2025-04-01 (fixed clock), 2018..2025 literals, 0001-01-01 and 2099-12-31; a date in 2026..2098 would be a run-day value
        Pattern diaDeEjecucion = Pattern.compile("\\b20(2[6-9]|[3-8]\\d|9[0-8])-\\d{2}");
        Pattern credencial = Pattern.compile("(?i)(password|passwd|secret|token|api[_-]?key|jdbc:|bearer )");
        List<String> problemas = new ArrayList<>();
        for (Path d : directorios()) {
            for (String archivo : List.of("case.json", "expected.json", "seed.sql")) {
                Path f = d.resolve(archivo);
                if (!Files.exists(f)) {
                    continue;
                }
                String t = Files.readString(f, StandardCharsets.UTF_8);
                if (archivo.equals("expected.json") && diaDeEjecucion.matcher(t).find()) {
                    problemas.add(d.getFileName() + "/" + archivo + ": a date that looks like the day it was recorded (mask the column with \"enmascarar\")");
                }
                if (archivo.equals("expected.json") && Pattern.compile("[$A-Za-z]@[0-9a-f]{4,8}\\b").matcher(t).find()) {
                    problemas.add(d.getFileName() + "/" + archivo + ": a raw identity hash code (Dump.sinHash must write @<hash>)");
                }
                if (credencial.matcher(t.replace("excepcion", "")).find()) {
                    problemas.add(d.getFileName() + "/" + archivo + ": credential-looking text");
                }
            }
        }
        assertThat(problemas).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------ the comparator can fail

    private static Verifica.Ejecucion conLaSalidaDelGolden(Caso caso) {
        return new Verifica.Ejecucion(caso.esperado().get("salida").deepCopy(), null);
    }

    @Test
    void elComparadorAceptaLaSalidaDelPropioGolden() {
        Caso caso = Caso.cargar(SIN_CAMBIOS);
        Verifica.comparar(caso, conLaSalidaDelGolden(caso), List.of(), List.of());
    }

    @Test
    void elComparadorRechazaUnaSalidaAlterada() {
        Caso caso = Caso.cargar(SIN_CAMBIOS);
        JsonObject alterada = caso.esperado().getAsJsonObject("salida").deepCopy();
        alterada.addProperty("P_OUT_ERRORESCONTADOR", 7);
        assertThatThrownBy(() -> Verifica.comparar(caso, new Verifica.Ejecucion(alterada, null), List.of(), List.of())).isInstanceOf(AssertionError.class)
                .hasMessageContaining("salida");
    }

    @Test
    void elComparadorRechazaUnaExcepcionDondeElLegacyRespondio() {
        Caso caso = Caso.cargar(SIN_CAMBIOS);
        assertThatThrownBy(() -> Verifica.comparar(caso, new Verifica.Ejecucion(null, new IllegalStateException("x")), List.of(), List.of())).isInstanceOf(AssertionError.class);
    }

    @Test
    void elComparadorRechazaUnaTablaAlterada() throws SQLException {
        Caso caso = Caso.cargar(SIN_CAMBIOS);
        try (Statement st = caso.conexion().createStatement()) {
            st.executeUpdate("UPDATE \"BSCL.Portafolio.BD::POR.Autorizado\" SET \"IdEstado\" = 'XXX'");
        }
        assertThatThrownBy(() -> Verifica.comparar(caso, conLaSalidaDelGolden(caso), List.of(), List.of())).isInstanceOf(AssertionError.class)
                .hasMessageContaining("rows of");
    }

    @Test
    void elComparadorRechazaUnaTrazaDeColaboradoresAlterada() {
        Caso caso = Caso.cargar(SIN_CAMBIOS);
        JsonObject llamada = new JsonObject();
        llamada.addProperty("servicio", "AgregarLog");
        llamada.add("entrada", new JsonObject());
        assertThatThrownBy(() -> Verifica.comparar(caso, conLaSalidaDelGolden(caso), List.of(llamada), List.of())).isInstanceOf(AssertionError.class)
                .hasMessageContaining("collaborator");
    }

    @Test
    void elComparadorRechazaUnaSecuenciaPedidaDeMas() {
        Caso caso = Caso.cargar(SIN_CAMBIOS);
        assertThatThrownBy(() -> Verifica.comparar(caso, conLaSalidaDelGolden(caso), List.of(), List.of("BSCL.APV.BD.Secuencias::X"))).isInstanceOf(AssertionError.class)
                .hasMessageContaining("sequences");
    }

    private static Throwable excepcionDeNombre(String simple) throws ReflectiveOperationException {
        for (String paquete : List.of("java.lang.", "java.sql.", "java.util.", "java.io.", "java.text.", "java.time.format.", "com.google.gson.")) {
            try {
                Class<?> k = Class.forName(paquete + simple);
                try {
                    return (Throwable) k.getConstructor(String.class).newInstance("replay");
                } catch (NoSuchMethodException sinConstructorDeTexto) {
                    try {
                        return (Throwable) k.getConstructor(String.class, int.class).newInstance("replay", 0); // java.text.ParseException
                    } catch (NoSuchMethodException sinEntero) {
                        return (Throwable) k.getConstructor(String.class, CharSequence.class, int.class).newInstance("replay", "x", 0); // DateTimeParseException
                    }
                }
            } catch (ClassNotFoundException ignored) {
                // next package
            }
        }
        throw new IllegalStateException("cannot replay exception " + simple);
    }

    /**
     * Fidelity of the modern fixture: for every case whose legacy run left the database exactly as seeded, the freshly built modern fixture dumps the same
     * tables as the oracle did, so replaying the golden's own output / exception, collaborator trace and sequences must be ACCEPTED by the comparator. This
     * proves the seeding, the dump, the masks and the canonical form agree between the oracle and the modern harness (the cases that write cannot be replayed
     * without the legacy, they are covered by the real use cases in Step 2).
     */
    @Test
    void todosLosCasosQueNoEscribenAceptanSuPropioGolden() throws Exception {
        int repetidos = 0;
        List<String> fallos = new ArrayList<>();
        for (String nombre : Caso.todos()) {
            Caso caso = Caso.cargar(nombre);
            JsonObject esperado = caso.esperado();
            if (!cl.bcs.apv.familia_c.support.Canon.texto(caso.tablas()).equals(cl.bcs.apv.familia_c.support.Canon.texto(esperado.getAsJsonObject("tablas")))) {
                continue; // the legacy wrote rows
            }
            Verifica.Ejecucion r = esperado.has("excepcion")
                    ? new Verifica.Ejecucion(null, excepcionDeNombre(esperado.get("excepcion").getAsString().split(":")[0]))
                    : new Verifica.Ejecucion(esperado.get("salida").deepCopy(), null);
            List<JsonObject> traza = new ArrayList<>();
            esperado.getAsJsonArray("colaboradores").forEach(e -> traza.add(e.getAsJsonObject().deepCopy()));
            List<String> secuencias = new ArrayList<>();
            for (JsonElement e : esperado.getAsJsonArray("secuenciasSolicitadas")) {
                String v = e.getAsString();
                int i = v.lastIndexOf('#');
                for (int k = 0; k < Integer.parseInt(v.substring(i + 1)); k++) {
                    secuencias.add(v.substring(0, i));
                }
            }
            try {
                Verifica.comparar(caso, r, traza, secuencias);
                repetidos++;
            } catch (AssertionError e) {
                fallos.add(nombre + ": " + e.getMessage().lines().findFirst().orElse(""));
            }
        }
        System.out.println("golden replays accepted by the comparator: " + repetidos);
        assertThat(fallos).isEmpty();
        assertThat(repetidos).as("cases that leave the database untouched").isGreaterThan(0);
    }

    @Test
    void unGoldenOCasoFaltanteFallaEnLugarDeSaltarse() {
        assertThatThrownBy(() -> Caso.cargar("APV-C-000_no-existe")).isInstanceOf(AssertionError.class).hasMessageContaining("missing test fixture");
    }

    @Test
    @SuppressWarnings("unused")
    void laSalidaDelGoldenEsUnObjetoJson() {
        JsonElement e = Caso.cargar(SIN_CAMBIOS).esperado().get("salida");
        assertThat(e.isJsonObject()).isTrue();
    }
}
