package cl.bcs.canalagente.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Verifier round 20261031a. The text of the SQLException of a failed INSERT / SELECT must be the LEGACY text WHOLE (cl.bcs.utils.SQL.rethrow: cause message, " Query: ", the SQL,
 * " Parameters: ", Arrays.deepToString of the rows). For the batch INSERT every row is the legacy POJO CartolaDeMovimiento printed by Utils.printJson (all its fields in order, the
 * nulls, dates yyyy-MM-dd HH:mm:ss.SSS, HTML escaping on) and a null row prints as "null". The approved equivalence test compares only the CLASS of a SQLException, so it never fixed
 * this text (it was "CartolaJson$Movimiento@6d4d66d2", a different hash in every run). Here the full text is compared with the golden recorded from the real legacy: seven development
 * goldens (CAN-CAL-017, CAN-LOG-043, 051, 052, 053, 055, 056) and the fresh goldens CAN-FRG-025 (special characters) and CAN-FRG-026 (repeated and null ids), copied as test resources.
 * The only text masked is the variable name of the JVM helpful-NullPointerException inside "Map Object failed" (legacy compiled without -g says &lt;local12&gt;): the same mask the
 * verifier's compare.py declares; the "Parameters:" text that follows is NOT masked.
 */
class CanalMensajeParametersTest {

    private static final String HELPFUL_NPE = "(?<= because \")[^\"\\n]*(?=\" is null)";

    static String normalizar(String excepcion) {
        return excepcion.replaceAll(HELPFUL_NPE, "VAR");
    }

    static JsonObject recurso(String id, String nombre) {
        try (InputStream in = CanalMensajeParametersTest.class.getResourceAsStream("/canal-fresco/" + id + "/" + nombre)) {
            if (in == null) throw new IllegalStateException("missing test resource canal-fresco/" + id + "/" + nombre);
            return JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static String semilla(String id) {
        try (InputStream in = CanalMensajeParametersTest.class.getResourceAsStream("/canal-fresco/" + id + "/seed.sql")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static String idFresco(String prefijo) {
        try {
            java.nio.file.Path dir = java.nio.file.Paths.get(CanalMensajeParametersTest.class.getResource("/canal-fresco").toURI());
            try (Stream<java.nio.file.Path> s = java.nio.file.Files.list(dir)) {
                List<String> l = s.map(p -> p.getFileName().toString()).filter(n -> n.startsWith(prefijo)).toList();
                assertEquals(1, l.size(), prefijo);
                return l.get(0);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static Stream<Arguments> casos() throws Exception {
        List<String> dev = List.of("CAN-CAL-017", "CAN-LOG-043", "CAN-LOG-051", "CAN-LOG-052", "CAN-LOG-053", "CAN-LOG-055", "CAN-LOG-056");
        Stream<Arguments> a = dev.stream().map(f -> {
            String id = CasoCanal.id(f);
            return Arguments.of(id, CasoCanal.golden(id).get("excepcion").getAsString(), null);
        });
        Stream<Arguments> b = Stream.of("CAN-FRG-025", "CAN-FRG-026").map(f -> {
            String id = idFresco(f);
            return Arguments.of(id, recurso(id, "expected.json").get("excepcion").getAsString(), id);
        });
        return Stream.concat(a, b);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @DisplayName("the SQLException text (Query: ... Parameters: ...) is the legacy text whole")
    void elMensajeCompletoEsElDelLegacy(String id, String golden, String fresco) throws Exception {
        JsonObject obtenido = fresco == null ? CasoCanal.corrida(id) : CasoCanal.ejecutar(recurso(id, "case.json"), semilla(id), null);
        assertTrue(obtenido.has("excepcion"), id + ": the unit must throw");
        String texto = obtenido.get("excepcion").getAsString();
        assertFalse(texto.contains("$Movimiento@"), id + ": Parameters must not carry Object.toString of the POJO: " + texto);
        assertEquals(normalizar(golden), normalizar(texto), id + ": full exception text");
    }

    @Test
    @DisplayName("CAN-LOG-043: the legacy prints the null element of the cartola as null after the JSON of the first row")
    void elElementoNuloSeImprimeComoNull() throws Exception {
        String texto = CasoCanal.corrida(CasoCanal.id("CAN-LOG-043")).get("excepcion").getAsString();
        assertTrue(texto.endsWith("\"MontoRetencionGananciaCap\":null}, null]"), texto);
        assertTrue(texto.contains("Parameters: [{\"IdCargoAbonoMovimiento\":1,\"FechaSistema\":\"2026-02-10 00:00:00.000\",\"FechaIngresoRegistro\":null,\"IdCuentaInversion\":21151,"), texto);
    }

    @Test
    @DisplayName("CAN-FRG-025: Gson of the platform escapes the HTML characters (' < > & =) and keeps the accents, in the order of the legacy POJO")
    void escapeHtmlYAcentos() throws Exception {
        String id = idFresco("CAN-FRG-025");
        String texto = CasoCanal.ejecutar(recurso(id, "case.json"), semilla(id), null).get("excepcion").getAsString();
        assertTrue(texto.contains("\"NombreCliente\":\"O\\u0027Higgins \\u003c\\u0026\\u003e \\u003d \\\"x\\\" \u00f1\",\"IdPortafolio\":555,"), texto);
        assertTrue(texto.contains("\"Cargo\":0,\"Abono\":1500.5,\"SaldoSimulado\":10000,\"IdMatrizAgrupador\":0,\"EsADC\":\"N\""), texto);
    }
}
