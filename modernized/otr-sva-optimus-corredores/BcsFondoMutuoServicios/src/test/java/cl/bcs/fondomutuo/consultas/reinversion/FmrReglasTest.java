package cl.bcs.fondomutuo.consultas.reinversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.reinversion.CasoFmr.Perfil;

/**
 * Rule cards of {@code ConsultarFondoMutuoReinversion} (RULES_FONDOMUTUO_F3_A.md 8.4, FM-FMR-01..04; defects D-F3A-11/25/27, D-REQ-7; matrix FMR-C01..C03),
 * stated with LITERAL values. The expected values are the ones the unmodified legacy produced (they equal the recorded goldens: this class and
 * {@link FmrEquivalenciaTest} must agree; if the legacy is re-recorded and a literal changes, the legacy won: update the literal and say so). Each test runs the
 * NEW code on the H2 of its case (slug of {@code cases/FMM-FMR-nn_slug}) and names the card or defect it pins.
 * Row text in {@link #filas}: {@code IdSerieFondo|IdRegimenFondoMutuo|MLT|ValorCuotaInstrumento|MontoPatrimonioEmisor|MontoMinimo} with the numbers as the Gson
 * text of the legacy (scale of the HANA type: {@code 1234.56780000}, {@code 0E-8}); a key that the legacy omits shows as {@code ~}.
 * Universe: {@code seed/base-fmr.sql} (series FM001A, S001..S012; see cases_fmr_lib.py).
 */
class FmrReglasTest {

    private static final String S001_18Q = "S001|18Q|S|1234.56780000|100000.00000000|1000.000000";
    private static final String S001_18T = "S001|18T|S|1234.56780000|100000.00000000|1000.000000";
    private static final String S002_18Q = "S002|18Q|S|987.65000000|250000.50000000|2500.000000";
    private static final String S004_18Q = "S004|18Q|S|10.50000000|100000.00000000|10.000000";
    private static final String S004_18T = "S004|18T|S|10.50000000|100000.00000000|10.000000";
    private static final String S005_18Q = "S005|18Q|S|500.25000000|100000.00000000|1000.000000";
    private static final String S006_18Q = "S006|18Q|S|700.50000000|100000.00000000|1000.000000";
    private static final String S007_18Q = "S007|18Q|S|300.10000000|100000.00000000|1000.000000";
    private static final String S008_18T = "S008|18T|S|150.00000000|100000.00000000|1000.000000";
    private static final String FM001A_GEN = "FM001A|GEN|N|1500.12340000|0E-8|0.000000";
    private static final String S003_57BIS = "S003|57BIS|N|0|0E-8|0.000000";
    private static final String S009_GEN = "S009|GEN|N|222.22000000|100000.00000000|1000.000000";
    private static final String S010_SIN_REGIMEN = "S010|~|N|111.11000000|100000.00000000|1000.000000";
    private static final String S011_18Q = "S011|18Q|S|50.00000000|100000.00000000|1000.000000";
    private static final String S012_NINGUNO = "S012|NINGUNO|N|80.80000000|100000.00000000|1000.000000";

    /** The 13 rows with estado ACT or no state filter, the company 1 (S007 INA is the 14th). */
    private static final List<String> ACTIVAS = List.of(FM001A_GEN, S001_18Q, S001_18T, S002_18Q, S003_57BIS, S004_18Q, S004_18T, S005_18Q, S006_18Q, S008_18T, S009_GEN,
            S010_SIN_REGIMEN, S012_NINGUNO);
    private static final List<String> TODAS_LA_EMPRESA_1 = List.of(FM001A_GEN, S001_18Q, S001_18T, S002_18Q, S003_57BIS, S004_18Q, S004_18T, S005_18Q, S006_18Q, S007_18Q,
            S008_18T, S009_GEN, S010_SIN_REGIMEN, S012_NINGUNO);

    private static JsonObject dump(String slug) throws Exception {
        return CasoFmr.ejecutar(CasoFmr.idPorSlug(slug), Perfil.ABIERTA);
    }

    private static JsonObject salida(String slug) throws Exception {
        JsonObject d = dump(slug);
        assertFalse(d.has("excepcion"), slug + " raised " + d.get("excepcion"));
        return d.getAsJsonObject("salida");
    }

    private static String excepcion(String slug) throws Exception {
        JsonObject d = dump(slug);
        assertTrue(d.has("excepcion"), slug + " did not raise");
        assertEquals("ROLLBACK_HTTP500", d.get("decisionServlet").getAsString());
        return d.get("excepcion").getAsString();
    }

    private static List<String> filas(String slug) throws Exception {
        List<String> r = new ArrayList<>();
        for (JsonElement e : salida(slug).getAsJsonArray("P_OUT_LISTA")) {
            JsonObject o = e.getAsJsonObject();
            List<String> c = new ArrayList<>();
            for (String k : new String[] { "IdSerieFondo", "IdRegimenFondoMutuo", "MLT", "ValorCuotaInstrumento", "MontoPatrimonioEmisor", "MontoMinimo" }) {
                c.add(o.has(k) ? o.get(k).getAsString() : "~");
            }
            r.add(String.join("|", c));
        }
        return r;
    }

    private static List<String> series(String slug) throws Exception {
        List<String> r = new ArrayList<>();
        for (String f : filas(slug)) r.add(f.substring(0, f.indexOf('|')) + "/" + f.split("\\|")[1]);
        return r;
    }

    // ---- FM-FMR-01 contract / FM-FMR-04 copy
    @Test
    @DisplayName("FM-FMR-01 / FM-FMR-04 FMR-C01: contrato de salida (solo P_OUT_LISTA), nueve campos en orden, copia sin transformaciones, una fila por serie y regimen")
    void fmFmr01_contratoYCopia() throws Exception {
        JsonObject d = dump("todo-comodin-estado-vacio");
        JsonObject s = d.getAsJsonObject("salida");
        assertEquals(List.of("P_OUT_LISTA"), new ArrayList<>(s.keySet()));
        assertEquals(List.of("IdSerieFondo", "NombreSerieFondo", "IdMoneda", "IdEmisor", "MLT", "IdRegimenFondoMutuo", "ValorCuotaInstrumento", "MontoPatrimonioEmisor", "MontoMinimo"),
                new ArrayList<>(s.getAsJsonArray("P_OUT_LISTA").get(1).getAsJsonObject().keySet()));
        assertEquals(TODAS_LA_EMPRESA_1, filas("todo-comodin-estado-vacio"));
        JsonObject s001 = s.getAsJsonArray("P_OUT_LISTA").get(1).getAsJsonObject();
        assertEquals("Serie A Renta Uno", s001.get("NombreSerieFondo").getAsString());
        assertEquals("CLP", s001.get("IdMoneda").getAsString());
        assertEquals("AGF1", s001.get("IdEmisor").getAsString());
    }

    @Test
    @DisplayName("FM-FMR-01 / D-F3A-27: sin P_OUT_ERRORESCONTADOR ni mensajes: el anfitrion decide ROLLBACK (la lectura no pierde datos); solo lectura: sin tablas locales, commits autonomos ni secuencias")
    void fmFmr01_sinContadorYSoloLectura() throws Exception {
        JsonObject d = dump("tablas-no-cambian");
        assertEquals("ROLLBACK", d.get("decisionServlet").getAsString());
        assertFalse(d.getAsJsonObject("salida").has("P_OUT_ERRORESCONTADOR"));
        assertFalse(d.getAsJsonObject("salida").has("P_OUT_MENSAJES"));
        assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop").size());
        assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("commitsAutonomos").size());
        assertEquals(0, d.getAsJsonArray("secuenciasSolicitadas").size());
        assertEquals(0, d.getAsJsonArray("colaboradores").size());
        assertEquals(12, d.getAsJsonObject("tablas").getAsJsonArray("BSCL.FondoMutuo.BD::FMM.ValorCuota").size());
    }

    @Test
    @DisplayName("FM-FMR-01: p_IdUsuario y p_IdModulo no se usan; claves desconocidas se ignoran; sin filas = {\"P_OUT_LISTA\":[]}")
    void fmFmr01_usuarioModuloYCamposDesconocidos() throws Exception {
        assertEquals(filas("estado-act"), filas("usuario-y-modulo-no-se-usan"));
        assertEquals(List.of(S007_18Q), filas("campos-desconocidos-se-ignoran"));
        assertEquals("{\"P_OUT_LISTA\":[]}", dump("sin-filas-no-es-error").get("salidaCruda").getAsString());
    }

    @Test
    @DisplayName("FM-FMR-01: tipos incorrectos en la entrada => JsonSyntaxException (HTTP 500); '1' y 1.0 como empresa se aceptan")
    void fmFmr01_tiposIncorrectos() throws Exception {
        assertTrue(excepcion("tipo-incorrecto-empresa-texto").startsWith("JsonSyntaxException"));
        assertTrue(excepcion("tipo-incorrecto-estado-objeto").startsWith("JsonSyntaxException"));
        assertEquals(List.of(S007_18Q), filas("empresa-como-texto-numerico"));
        assertEquals(List.of(S007_18Q), filas("empresa-decimal"));
    }

    // ---- FM-FMR-02 wildcards of the SQL
    @Test
    @DisplayName("FM-FMR-02 FMR-C01/C03: p_IdEstado ACT filtra; INA => solo S007; '' , nulo y ausente = sin filtro; la comparacion es exacta (minuscula y espacio no coinciden)")
    void fmFmr02_estado() throws Exception {
        assertEquals(ACTIVAS, filas("estado-act"));
        assertEquals(List.of(S007_18Q), filas("estado-ina"));
        assertEquals(TODAS_LA_EMPRESA_1, filas("todo-comodin-estado-vacio"));
        assertEquals(TODAS_LA_EMPRESA_1, filas("estado-nulo-es-sin-filtro"));
        assertEquals(TODAS_LA_EMPRESA_1, filas("estado-ausente-es-sin-filtro"));
        assertEquals(List.of(), filas("estado-minuscula-no-coincide"));
        assertEquals(List.of(), filas("estado-con-espacio-no-coincide"));
    }

    @Test
    @DisplayName("FM-FMR-02: filtros de moneda, fondo extranjero, familia y emisor: '-' o nulo = sin filtro; '' NO es comodin en los de '-'; comparacion exacta")
    void fmFmr02_monedaExtranjeroFamiliaEmisor() throws Exception {
        assertEquals(List.of("S004/18Q", "S004/18T", "S012/NINGUNO"), series("moneda-usd"));
        assertEquals(ACTIVAS, filas("moneda-nula-es-sin-filtro"));
        assertEquals(List.of(), filas("moneda-vacia-no-es-comodin"));
        assertEquals(List.of(), filas("moneda-inexistente"));
        assertEquals(List.of(S004_18Q, S004_18T), filas("fondo-extranjero-s"));
        assertEquals(List.of(S008_18T), filas("fondo-extranjero-x"));
        assertEquals(10, filas("fondo-extranjero-n").size());
        assertEquals(ACTIVAS, filas("fondo-extranjero-nulo"));
        assertEquals(List.of(S004_18Q, S004_18T), filas("familia-fam3"));
        assertEquals(9, filas("familia-fam1").size());
        assertEquals(ACTIVAS, filas("familia-nula"));
        assertEquals(List.of("FM001A/GEN", "S001/18Q", "S001/18T", "S002/18Q", "S005/18Q", "S010/~"), series("emisor-agf1"));
        assertEquals(List.of(S008_18T), filas("emisor-agf3-fuera-de-la-vista"));
        assertEquals(ACTIVAS, filas("emisor-nulo"));
        assertEquals(List.of(), filas("emisor-en-minuscula"));
    }

    @Test
    @DisplayName("FM-FMR-02 / FM-FMR-03: filtro de regimen: solo las filas del regimen pedido; '' no es comodin (todas salen sin regimen); S009 con 18Q INA no cuenta")
    void fmFmr02_regimen() throws Exception {
        assertEquals(List.of(S001_18Q, S002_18Q, S004_18Q, S005_18Q, S006_18Q), filas("regimen-18q"));
        assertEquals(List.of(S001_18T, S004_18T, S008_18T), filas("regimen-18t"));
        assertEquals(List.of(S003_57BIS), filas("regimen-57bis"));
        assertEquals(List.of(FM001A_GEN, S009_GEN), filas("regimen-gen"));
        assertEquals(List.of(), filas("regimen-inexistente"));
        assertEquals(ACTIVAS, filas("regimen-nulo-es-sin-filtro"));
        List<String> vacio = filas("regimen-vacio-no-es-comodin");
        assertEquals(11, vacio.size());
        assertTrue(vacio.stream().allMatch(f -> f.split("\\|")[1].equals("~") && f.split("\\|")[2].equals("N")));
        assertEquals(List.of(S001_18Q, S002_18Q, S004_18Q, S005_18Q, S006_18Q, S007_18Q), filas("regimen-ina-no-cuenta"));
    }

    @Test
    @DisplayName("FM-FMR-02 FMR-C01: la serie de origen se excluye (IdSerieFondo <> origen); '-', nulo, '' (distinto de '-'), inexistente y minuscula no excluyen nada; ejemplo de la tarjeta")
    void fmFmr02_serieDeOrigen() throws Exception {
        List<String> sinS001 = new ArrayList<>(TODAS_LA_EMPRESA_1.subList(0, 1));
        sinS001.addAll(List.of(S002_18Q, S003_57BIS, S004_18Q, S004_18T, S005_18Q, S006_18Q, S008_18T, S009_GEN, S010_SIN_REGIMEN, S012_NINGUNO));
        assertEquals(sinS001, filas("origen-excluye-la-serie"));
        assertEquals(List.of(S002_18Q, S004_18Q, S005_18Q, S006_18Q), filas("origen-con-regimen-como-el-ejemplo-de-la-tarjeta"));
        for (String slug : new String[] { "origen-nulo-no-excluye", "origen-inexistente-no-excluye", "origen-vacio-no-es-comodin" }) assertEquals(ACTIVAS.size(), filas(slug).size(), slug);
        assertEquals(9, filas("origen-en-minuscula-no-excluye").size());
    }

    @Test
    @DisplayName("FM-FMR-02 / D-F3A-25: p_IdEmpresa filtra la empresa; 99, negativa o enorme => vacia; ausente, nula explicita o {} (long primitivo = 0) => IdEmpresa = 0 => vacia (Q-F3A-9 cerrada contra HANA: el parametro BIGINT <> '' es siempre verdadero)")
    void fmFmr02_empresa() throws Exception {
        assertEquals(List.of(S011_18Q), filas("empresa-2-solo-sus-series"));
        for (String slug : new String[] { "empresa-inexistente", "p-idempresa-negativa", "p-idempresa-grande", "empresa-ausente-es-cero", "empresa-nula-explicita", "entrada-vacia" }) {
            assertEquals(List.of(), filas(slug), slug);
        }
    }

    // ---- FM-FMR-03 rows per serie and regime, MLT, values
    @Test
    @DisplayName("FM-FMR-03 D-F3A-11 FMR-C02: una fila por serie y por regimen ACT (S001 y S004 salen dos veces); serie sin regimen sale una vez con regimen ausente y MLT N; sin ORDER BY")
    void fmFmr03_unaFilaPorSerieYRegimen() throws Exception {
        assertEquals(List.of(S004_18Q, S004_18T), filas("serie-con-dos-regimenes-salen-dos-filas"));
        assertEquals(List.of(S001_18T), filas("serie-dos-regimenes-filtro-uno"));
        assertTrue(filas("serie-sin-regimen-con-comodin").contains(S010_SIN_REGIMEN));
        assertEquals(ACTIVAS, filas("serie-sin-regimen-con-comodin"));
    }

    @Test
    @DisplayName("FM-FMR-03 FMR-C02: MLT S si el regimen tiene RescateConMandato S (18Q) o AceptaMandatoEnReinversion S (18T), si no N (57BIS, GEN, NINGUNO, sin regimen)")
    void fmFmr03_mlt() throws Exception {
        assertEquals("S", filas("regimen-18q").get(0).split("\\|")[2]);
        assertEquals("S", filas("regimen-18t").get(0).split("\\|")[2]);
        assertEquals("N", filas("regimen-57bis").get(0).split("\\|")[2]);
        assertEquals("N", filas("regimen-gen").get(0).split("\\|")[2]);
        assertEquals("N", filas("serie-sin-regimen-con-comodin").get(11).split("\\|")[2]);
    }

    @Test
    @DisplayName("FM-FMR-03 / FM-FMR-04: IFNULL de la cuota (serie sin fila: 0), del patrimonio y del minimo; la escala de HANA se conserva en el texto (0E-8 para cero con escala 8)")
    void fmFmr03_valoresConIfnull() throws Exception {
        assertEquals(List.of(S003_57BIS, S008_18T), filas("valores-ifnull-cuota-y-montos"));
        assertEquals("S003|57BIS|N|0|0E-8|0.000000", filas("minimo-nulo-no-ocurre-monto-cero").get(0));
        assertEquals(List.of("S002|18Q|S|987.65000000|250000.50000000|2500.000000", S005_18Q), filas("monto-patrimonio-decimal"));
        assertEquals(List.of(S001_18T), filas("valor-cuota-escala-ocho"));
    }

    @Test
    @DisplayName("FM-FMR-03 D-REQ-7: la cuota se une por serie SIN filtro de fecha: la cuota de 2020-01-01 (1.5) se devuelve como valor de la serie")
    void fmFmr03_cuotaSinFiltroDeFecha() throws Exception {
        assertEquals(List.of("S002|18Q|S|1.50000000|250000.50000000|2500.000000", S005_18Q), filas("serie-con-cuota-historica-no-aplica-fecha"));
    }

    // ---- RULE-076
    @Test
    @DisplayName("RULE-076: ni el main() del legacy ni su serie real ni su conexion se copiaron (src/main y los fixtures de fondomutuo-fmr)")
    void rule076_sinElMainNiLosIdsDelLegacy() throws Exception {
        // legacy ConsultarFondoMutuoReinversion.main (FMR:119-141): PlataformaUtils.getBOLSAConnection() and a JSON with the real series id below
        List<String> culpables = new ArrayList<>();
        for (java.nio.file.Path raiz : List.of(java.nio.file.Path.of("src", "main"), java.nio.file.Path.of("src", "test", "resources", "fondomutuo-fmr"))) {
            assertTrue(java.nio.file.Files.isDirectory(raiz), "RULE-076 needs " + raiz + " (run the tests from the module directory)");
            try (var s = java.nio.file.Files.walk(raiz)) {
                for (java.nio.file.Path p : s.filter(java.nio.file.Files::isRegularFile).toList()) {
                    String t = java.nio.file.Files.readString(p, java.nio.charset.StandardCharsets.ISO_8859_1);
                    if (t.contains("00212NUSD") || t.contains("getBOLSAConnection") || t.contains("getDESAConnection")) culpables.add(p.toString());
                }
            }
        }
        assertEquals(List.of(), culpables);
    }
}
