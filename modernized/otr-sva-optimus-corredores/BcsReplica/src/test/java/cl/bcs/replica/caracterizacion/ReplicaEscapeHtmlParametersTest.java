package cl.bcs.replica.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Verifier round 20261031a. Gap found by the canary {@code disableHtmlEscaping()} on {@code Sql.json}: no development golden carries one of ' &lt; &gt; &amp; = in the rows of a failed
 * batch, so turning the HTML escaping of the Gson of the legacy {@code Utils.printJson} off survived with the whole suite green. The legacy Gson escapes them as the unicode escapes
 * {@code ' < > & =} (a backslash, "u" and four hex digits, in the text of the exception) and keeps the accents as they are.
 *
 * <p>The five FRESH inputs REP-FRG-001, 002, 010, 041 and 043 (recorded from the real legacy by the verifier) are copied as test resources under {@code replica-fresco/}; the literal
 * expected tail of {@code " Parameters: "} below is the text of those legacy goldens, and the whole exception message of the modernized run must equal the golden.
 * In Java source {@code "\\u0027"} is the six characters backslash-u-0-0-2-7.
 */
class ReplicaEscapeHtmlParametersTest {

    private static final String PARAMETERS = " Parameters: ";

    private static Path dir(String prefijo) throws Exception {
        Path raiz = Paths.get(ReplicaEscapeHtmlParametersTest.class.getResource("/replica-fresco").toURI());
        try (var s = Files.list(raiz)) {
            List<Path> l = s.filter(p -> p.getFileName().toString().startsWith(prefijo)).toList();
            assertEquals(1, l.size(), prefijo);
            return l.get(0);
        }
    }

    private static JsonObject leer(Path p) throws Exception {
        return JsonParser.parseString(Files.readString(p, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static String cola(String texto) {
        int i = texto.lastIndexOf(PARAMETERS);
        assertTrue(i >= 0, "no Parameters part: " + texto);
        return texto.substring(i + PARAMETERS.length());
    }

    private static void fija(String prefijo, String esperadoLiteral) throws Exception {
        Path d = dir(prefijo);
        JsonObject golden = leer(d.resolve("expected.json"));
        Path semilla = d.resolve("seed.sql");
        JsonObject obtenido = CasoReplica.ejecutar(leer(d.resolve("case.json")), Files.exists(semilla) ? Files.readString(semilla, StandardCharsets.UTF_8) : null, null);
        assertTrue(obtenido.has("excepcion"), prefijo + ": the unit must throw");
        String g = golden.get("excepcion").getAsString();
        String o = obtenido.get("excepcion").getAsString();
        assertEquals(esperadoLiteral, cola(g), prefijo + ": the literal expected tail is the legacy golden");
        assertEquals(esperadoLiteral, cola(o), prefijo + ": Parameters of the modernized exception");
        assertEquals(g, o, prefijo + ": whole exception message");
    }

    @Test
    @DisplayName("REP-FRG-001: ' = < > & \" after the date of the workstack row of CARTERARV come out escaped (\\u0027 \\u003d \\u003c \\u003e \\u0026 and a quote with backslash)")
    void carteraRvConEspeciales() throws Exception {
        fija("REP-FRG-001", "[{\"Key\":\"\",\"Table\":\"CARTERARV\",\"Action\":\"M\",\"Status\":0,\"Stamp\":null,\"Parameters\":\"tables:Custodia_CarteraRV;BCS_query:QueryReplica;Fecha:2026-03-04"
                + "\\u0027\\u003d\\u003c\\u003e\\u0026\\\"" + "z".repeat(371) + " ;serialDelete:false;truncarDestino:true;serialLaunch:false\"}]");
    }

    @Test
    @DisplayName("REP-FRG-002: the escaped characters of the PATRIMONIO row (116 escaped equal signs) (REP-FRG-002)")
    void patrimonioConEspeciales() throws Exception {
        fija("REP-FRG-002", "[{\"Key\":\"\",\"Table\":\"PATRIMONIO DIA ACTUAL\",\"Action\":\"M\",\"Status\":0,\"Stamp\":null,\"Parameters\":\"tables:CuentaInversion_PatrimonioCajaFechaLiquidacion,"
                + "CuentaInversion_PatrimonioCajaFechaOperacion,Custodia_PatrimonioConsolidadoFechaLiquidacion,Custodia_PatrimonioConsolidadoFechaOperacion,Custodia_PatrimonioCustodiaFechaLiquidacion,"
                + "Custodia_PatrimonioCustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:2026-03-04\\u003d\\u003c\\u003e\\u0026\\u0027" + "\\u003d".repeat(116)
                + ";serialDelete:false;truncarDestino:false;serialLaunch:false\"}]");
    }

    @Test
    @DisplayName("REP-FRG-010: accents stay as they are, the HTML characters are escaped (REP-FRG-010)")
    void patrimonioConTildesYEspeciales() throws Exception {
        fija("REP-FRG-010", "[{\"Key\":\"\",\"Table\":\"PATRIMONIO DIA ACTUAL\",\"Action\":\"M\",\"Status\":0,\"Stamp\":null,\"Parameters\":\"tables:CuentaInversion_PatrimonioCajaFechaLiquidacion,"
                + "CuentaInversion_PatrimonioCajaFechaOperacion,Custodia_PatrimonioConsolidadoFechaLiquidacion,Custodia_PatrimonioConsolidadoFechaOperacion,Custodia_PatrimonioCustodiaFechaLiquidacion,"
                + "Custodia_PatrimonioCustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:2026-03-04ñ\\u0027\\u003cé\\u003e\\u0026\\u003dá" + "ó".repeat(113)
                + ";serialDelete:false;truncarDestino:false;serialLaunch:false\"}]");
    }

    @Test
    @DisplayName("REP-FRG-041: post-pauta row, escapes in the table and in the date, the n-tilde stays (REP-FRG-041)")
    void postPautaFechaLargaConEspeciales() throws Exception {
        fija("REP-FRG-041", "[{\"FechaSistema\":\"2026-06-30\",\"Tabla\":\"Custodia_X\\u0027\\u003c\\u003e\\u0026\\u003d\",\"Fecha\":\"2026-07-01\\u0027\\u003c\\u003e\\u0026\\u003dñ\"}]");
    }

    @Test
    @DisplayName("REP-FRG-043: post-pauta row, table of 101 characters ending in & and < (REP-FRG-043)")
    void postPautaTablaDe101Caracteres() throws Exception {
        fija("REP-FRG-043", "[{\"FechaSistema\":\"2028-02-29\",\"Tabla\":\"" + "T".repeat(99) + "\\u0026\\u003c\",\"Fecha\":\"2028-03-01\"}]");
    }
}
