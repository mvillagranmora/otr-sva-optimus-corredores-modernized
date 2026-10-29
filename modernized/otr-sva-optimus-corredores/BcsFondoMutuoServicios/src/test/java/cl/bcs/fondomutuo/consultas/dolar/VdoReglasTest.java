package cl.bcs.fondomutuo.consultas.dolar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.dolar.CasoVdo.Perfil;

/**
 * Rule cards of {@code ConsultarValorDolarFondoMutuo} (RULES_FONDOMUTUO_F3_A.md 8.7, FM-VDO-01..04; defects D-F3A-14/15/25, D-NOR-9; matrix VDO-C01..C04),
 * stated with LITERAL values. The expected values are the ones the unmodified legacy produced (they equal the recorded goldens: this class and
 * {@link VdoEquivalenciaTest} must agree; if the legacy is re-recorded and a literal changes, the legacy won: update the literal and say so). Each test runs the
 * NEW code on the H2 of its case (slug of {@code cases/FMM-VDO-nn_slug}) and names the card or defect it pins.
 * Result text in {@link #resultados}: {@code IdSerieFondo|IdTipoSolicitud|ValorParidad} ({@code ~} = key omitted by the plain Gson; the number is the legacy text:
 * table values keep the scale 8 of the HANA type, collaborator values come as the scripted JSON number). Universe: {@code seed/base-vdo.sql}
 * (V001 930.10 / 925.40, V002 0 / 0, V003 no row, V004 company 2, V005 scale 8; FM001A 900 / 890). The scripted collaborator answers are in each case.json.
 */
class VdoReglasTest {

    private static final String VACIA = "{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_RESULTADO\":[]}";

    private static JsonObject dump(String slug) throws Exception {
        return CasoVdo.ejecutar(CasoVdo.idPorSlug(slug), Perfil.ABIERTA);
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

    private static List<String> resultados(String slug) throws Exception {
        List<String> r = new ArrayList<>();
        for (JsonElement e : salida(slug).getAsJsonArray("P_OUT_RESULTADO")) {
            JsonObject o = e.getAsJsonObject();
            r.add(txt(o, "IdSerieFondo") + "|" + txt(o, "IdTipoSolicitud") + "|" + txt(o, "ValorParidad"));
        }
        return r;
    }

    private static String txt(JsonObject o, String k) {
        return o.has(k) ? o.get(k).getAsString() : "~";
    }

    private static int llamadasAParidad(String slug) throws Exception {
        return dump(slug).getAsJsonArray("colaboradores").size();
    }

    private static JsonObject entradaAParidad(String slug) throws Exception {
        return dump(slug).getAsJsonArray("colaboradores").get(0).getAsJsonObject().getAsJsonArray("argumentos").get(0).getAsJsonObject();
    }

    // ---- FM-VDO-01 contract and parameters (VDO-C04)
    @Test
    @DisplayName("FM-VDO-01 VDO-C04: p_Series vacia (o ausente) => {P_OUT_ERRORESCONTADOR 0, P_OUT_RESULTADO []}, COMMIT, con las dos ramas y SIN llamar a CalcularParidad")
    void fmVdo01_listaVacia() throws Exception {
        for (String slug : new String[] { "lista-vacia-rama-tabla", "lista-vacia-rama-observado", "lista-ausente-es-vacia" }) {
            JsonObject d = dump(slug);
            assertEquals(VACIA, d.get("salidaCruda").getAsString(), slug);
            assertEquals("COMMIT", d.get("decisionServlet").getAsString(), slug);
            assertEquals(0, d.getAsJsonArray("colaboradores").size(), slug);
        }
    }

    @Test
    @DisplayName("FM-VDO-01 VDO-C04 D-F3A-25: p_Series nulo explicito => NullPointerException (size() sobre nulo)")
    void fmVdo01_listaNulaEsNpe() throws Exception {
        assertTrue(excepcion("lista-nula-explicita-npe").startsWith("NullPointerException"));
    }

    @Test
    @DisplayName("FM-VDO-01 VDO-C04: los parametros FONDOMUTUO/UsaDolarObservado y PLATAFORMA/PLA.MonedaSistema se leen SIEMPRE y son obligatorios, aunque p_Series este vacia")
    void fmVdo01_parametrosObligatorios() throws Exception {
        assertTrue(excepcion("parametro-observado-ausente-aun-con-lista-vacia").contains("Parametro UsaDolarObservado en modulo FONDOMUTUO no encontrado"));
        assertTrue(excepcion("moneda-sistema-ausente-aun-con-lista-vacia").contains("Parametro PLA.MonedaSistema en modulo PLATAFORMA no encontrado"));
    }

    @Test
    @DisplayName("FM-VDO-01 VDO-C04 (kit): empresa sin registros de configuracion (2) o p_IdEmpresa ausente => NullPointerException del legacy (HashMap de la empresa nulo); se acepta la clase del puerto del kit (ParametroNoEncontrado / IllegalArgument): ver KIT_NPE_DE_CONFIGURACION")
    void fmVdo01_empresaSinConfiguracion() throws Exception {
        for (String slug : new String[] { "empresa-sin-configuracion", "empresa-ausente", "tabla-empresa-2-sin-configuracion" }) {
            String e = excepcion(slug);
            assertTrue(VdoEquivalenciaTest.CLASES_DEL_KIT.contains(e.substring(0, e.indexOf(':'))), slug + " :: " + e);
        }
    }

    @Test
    @DisplayName("FM-VDO-01/04: usuario y modulo no se leen; claves desconocidas se ignoran; tipos incorrectos => JsonSyntaxException; elemento nulo en la lista => NullPointerException")
    void fmVdo01_entradaTolerante() throws Exception {
        assertEquals(List.of("V001|APO|930.10000000"), resultados("usuario-y-modulo-no-se-usan"));
        assertEquals(List.of("V001|APO|930.10000000"), resultados("campos-desconocidos-se-ignoran"));
        assertTrue(excepcion("empresa-como-texto-json-syntax").startsWith("JsonSyntaxException"));
        assertTrue(excepcion("series-como-texto-json-syntax").startsWith("JsonSyntaxException"));
        assertTrue(excepcion("elemento-nulo-en-la-lista-npe").startsWith("NullPointerException"));
    }

    // ---- FM-VDO-02 branch UsaDolarObservado = S (VDO-C01, VDO-C02)
    @Test
    @DisplayName("FM-VDO-02 VDO-C01: 3 series con la misma fecha => UNA entrada a CalcularParidad (p_IdTrabajo 1, USD -> moneda sistema, MontoOrigen 1, IdInterno 0) y las 3 reciben el MontoDestino")
    void fmVdo02_unaFechaTresSeries() throws Exception {
        assertEquals(List.of("V001|APO|940.25", "V002|RES|940.25", "V003|APO|940.25"), resultados("observado-una-fecha-tres-series"));
        assertEquals(1, llamadasAParidad("observado-una-fecha-tres-series"));
        assertEquals("{\"p_IdEmpresa\":1,\"p_IdTrabajo\":1,\"p_CalculosEntrada\":[{\"IdTrabajo\":1,\"IdInterno\":0,\"Fecha\":\"2026-03-10\",\"IdMonedaOrigen\":\"USD\","
                + "\"IdMonedaDestino\":\"CLP\",\"ValorCambioLocalModificado\":0,\"MontoOrigen\":1,\"IdUso\":\"\"}]}", entradaAParidad("observado-una-fecha-tres-series").toString());
        assertEquals("COMMIT", dump("observado-una-fecha-tres-series").get("decisionServlet").getAsString());
    }

    @Test
    @DisplayName("FM-VDO-02 VDO-C02 D-F3A-14 D-NOR-9: dos o tres fechas distintas => entradas con IdInterno 0 (la variable i no se incrementa) y List2Hash lanza 'key field is not unique' (HTTP 500)")
    void fmVdo02_variasFechasClaveNoUnica() throws Exception {
        for (String slug : new String[] { "observado-dos-fechas-clave-no-unica", "observado-tres-fechas-clave-no-unica", "observado-dos-fechas-una-sola-entrada-por-fecha",
                "observado-fecha-vacia-y-texto-libre" }) {
            assertEquals("Exception: key field is not unique", excepcion(slug).replaceFirst("^\\w*Exception", "Exception"), slug);
        }
        assertEquals(1, llamadasAParidad("observado-dos-fechas-clave-no-unica"));   // ONE call with one entry per distinct date
        assertEquals(2, entradaAParidad("observado-dos-fechas-clave-no-unica").getAsJsonArray("p_CalculosEntrada").size());
        JsonObject entrada = entradaAParidad("observado-tres-fechas-clave-no-unica");
        assertEquals(3, entrada.getAsJsonArray("p_CalculosEntrada").size());
        for (JsonElement c : entrada.getAsJsonArray("p_CalculosEntrada")) assertEquals(0L, c.getAsJsonObject().get("IdInterno").getAsLong());
    }

    @Test
    @DisplayName("FM-VDO-02 D-F3A-14: con dos fechas y UNA sola salida del colaborador (IdInterno 0) no hay clave repetida y las dos series reciben ese valor")
    void fmVdo02_dosFechasUnaSalida() throws Exception {
        assertEquals(List.of("V001|APO|940.25", "V002|RES|940.25"), resultados("observado-dos-fechas-doble-devuelve-una-salida"));
    }

    @Test
    @DisplayName("FM-VDO-02: el tipo de solicitud no cuenta en la rama observada; misma serie y fecha repetidas => una entrada; 30 series de una fecha => una entrada y 30 resultados en orden")
    void fmVdo02_tipoSeIgnoraYDeduplica() throws Exception {
        assertEquals(List.of("V001|APO|940.25", "V001|RES|940.25"), resultados("observado-tipo-solicitud-se-ignora"));
        assertEquals(1, llamadasAParidad("observado-tipo-solicitud-se-ignora"));
        assertEquals(List.of("V001|APO|940.25", "V001|APO|940.25"), resultados("observado-misma-serie-dos-tipos"));
        assertEquals(1, llamadasAParidad("observado-misma-serie-dos-tipos"));
        List<String> treinta = resultados("observado-treinta-series-una-fecha");
        assertEquals(30, treinta.size());
        assertEquals("V900|RES|940.25", treinta.get(0));
        assertEquals("V929|APO|940.25", treinta.get(29));
        assertEquals(1, llamadasAParidad("observado-treinta-series-una-fecha"));
    }

    @Test
    @DisplayName("FM-VDO-02: respuesta del colaborador vacia, con IdInterno distinto de 0 => ValorParidad 0; MontoDestino nulo => clave omitida; el contador de errores del colaborador no se lee; el valor no se redondea")
    void fmVdo02_respuestasDelColaborador() throws Exception {
        assertEquals(List.of("V001|APO|0"), resultados("observado-colaborador-devuelve-vacio"));
        assertEquals(List.of("V001|APO|0"), resultados("observado-colaborador-id-distinto-de-cero"));
        assertEquals(List.of("V001|APO|~"), resultados("observado-colaborador-monto-nulo"));
        assertEquals(List.of("V001|APO|933.3"), resultados("observado-colaborador-contador-de-errores-se-ignora"));
        assertEquals(List.of("V001|APO|940.123456789"), resultados("observado-valor-con-escala"));
    }

    @Test
    @DisplayName("FM-VDO-02: el colaborador que lanza propaga su excepcion tal cual (mensaje del colaborador, HTTP 500)")
    void fmVdo02_colaboradorFalla() throws Exception {
        assertTrue(excepcion("observado-colaborador-falla").endsWith("Paridad no disponible para la fecha"));
        assertEquals(1, llamadasAParidad("observado-colaborador-falla"));
    }

    @Test
    @DisplayName("FM-VDO-02: Fecha nula => una entrada con Fecha nula (HashMap acepta nulo) y la clave Fecha omitida en el resultado; la moneda destino es PLA.MonedaSistema (USD aqui)")
    void fmVdo02_fechaNulaYMonedaSistema() throws Exception {
        assertEquals(List.of("V001|APO|940.25", "V002|APO|940.25"), resultados("observado-fecha-nula"));
        assertTrue(entradaAParidad("observado-fecha-nula").getAsJsonArray("p_CalculosEntrada").get(0).getAsJsonObject().get("Fecha").isJsonNull());
        assertEquals("USD", entradaAParidad("observado-moneda-sistema-usd").getAsJsonArray("p_CalculosEntrada").get(0).getAsJsonObject().get("IdMonedaDestino").getAsString());
    }

    @Test
    @DisplayName("FM-VDO-02: solo 'S' exacto activa la rama observada: 's', ' S' y '' van por la tabla (el colaborador no se llama)")
    void fmVdo02_soloSExacto() throws Exception {
        assertEquals(List.of("V001|APO|930.10000000"), resultados("observado-minuscula-no-es-s"));
        assertEquals(List.of("V001|RES|925.40000000"), resultados("observado-con-espacio-no-es-s"));
        assertEquals(List.of("V001|RES|925.40000000"), resultados("observado-vacio-es-rama-tabla"));
        for (String slug : new String[] { "observado-minuscula-no-es-s", "observado-con-espacio-no-es-s", "observado-vacio-es-rama-tabla" }) assertEquals(0, llamadasAParidad(slug), slug);
    }

    // ---- FM-VDO-03 branch of the table (VDO-C03)
    @Test
    @DisplayName("FM-VDO-03 VDO-C03: APO => ValorDolarAporte (930.10), RES => ValorDolarRescate (925.40), con la escala 8 del tipo HANA; sin llamar a CalcularParidad")
    void fmVdo03_aporteYRescate() throws Exception {
        assertEquals(List.of("V001|APO|930.10000000"), resultados("tabla-aporte"));
        assertEquals(List.of("V001|RES|925.40000000"), resultados("tabla-rescate"));
        assertEquals(0, llamadasAParidad("tabla-aporte"));
        assertEquals(List.of("FM001A|APO|900.00000000", "FM001A|RES|890.00000000"), resultados("tabla-base-fm001a"));
        assertEquals(List.of("V005|APO|1000.12345678", "V005|RES|999.99999999"), resultados("tabla-escala-ocho"));
    }

    @Test
    @DisplayName("FM-VDO-03 D-F3A-15: cualquier tipo distinto de 'APO' exacto toma el dolar de rescate: '', nulo, 'apo', 'XYZ', ' APO'; el orden y las repeticiones de la entrada se conservan")
    void fmVdo03_tipoDistintoDeAporteEsRescate() throws Exception {
        assertEquals(List.of("V001||925.40000000"), resultados("tabla-tipo-vacio-es-rescate"));
        assertEquals(List.of("V001|~|925.40000000"), resultados("tabla-tipo-nulo-es-rescate"));
        assertEquals(List.of("V001|apo|925.40000000"), resultados("tabla-tipo-minuscula-es-rescate"));
        assertEquals(List.of("V001|XYZ|925.40000000"), resultados("tabla-tipo-desconocido-es-rescate"));
        assertEquals(List.of("V001| APO|925.40000000"), resultados("tabla-tipo-con-espacio-es-rescate"));
        assertEquals(List.of("V001|APO|930.10000000", "V001|RES|925.40000000", "V001||925.40000000", "V001|~|925.40000000", "V001|apo|925.40000000", "V001|APO|930.10000000"),
                resultados("tabla-todos-los-tipos-en-una-peticion"));
        assertEquals(List.of("V001|~|925.40000000"), resultados("tabla-sin-tipo-solicitud-ni-fecha"));
    }

    @Test
    @DisplayName("FM-VDO-03 D-F3A-15: la Fecha no cuenta en la rama de tabla (1999-01-01, nula o basura dan el mismo valor y se devuelven tal cual)")
    void fmVdo03_fechaSeIgnora() throws Exception {
        assertEquals(List.of("V001|APO|930.10000000", "V001|APO|930.10000000", "V001|APO|930.10000000"), resultados("tabla-fecha-se-ignora"));
        JsonObject s = salida("tabla-fecha-se-ignora");
        assertEquals("1999-01-01", s.getAsJsonArray("P_OUT_RESULTADO").get(0).getAsJsonObject().get("Fecha").getAsString());
        assertFalse(s.getAsJsonArray("P_OUT_RESULTADO").get(1).getAsJsonObject().has("Fecha"));
        assertEquals("no es fecha", s.getAsJsonArray("P_OUT_RESULTADO").get(2).getAsJsonObject().get("Fecha").getAsString());
    }

    @Test
    @DisplayName("FM-VDO-03: serie desconocida, nula, de otra empresa o sin fila de tipo de cambio => ValorParidad 0 sin error; valores 0 de la tabla salen como 0E-8")
    void fmVdo03_valorCero() throws Exception {
        assertEquals(List.of("NOEXISTE|APO|0", "V001|APO|930.10000000"), resultados("tabla-serie-desconocida-es-cero"));
        assertEquals(List.of("~|APO|0"), resultados("tabla-serie-nula"));
        assertEquals(List.of("V003|APO|0", "V003|RES|0"), resultados("tabla-serie-sin-fila-de-tipo-de-cambio"));
        assertEquals(List.of("V004|APO|0"), resultados("tabla-serie-de-otra-empresa"));
        assertEquals(List.of("V002|APO|0E-8", "V002|RES|0E-8"), resultados("tabla-valores-cero"));
        assertEquals(List.of("NOEXISTE|APO|0"), resultados("sin-validaciones-ni-perfil"));
        assertEquals(List.of("V001' OR '1'='1|APO|0"), resultados("tabla-serie-con-comilla"));
    }

    @Test
    @DisplayName("FM-VDO-03: empresa 2 con sus parametros: V004 APO 910.50 y RES 905.50; 30 series en el IN conservan orden y ceros para las desconocidas")
    void fmVdo03_empresa2Y30Series() throws Exception {
        assertEquals(List.of("V004|APO|910.50000000", "V004|RES|905.50000000"), resultados("tabla-empresa-2-con-configuracion"));
        List<String> r = resultados("tabla-treinta-series-en-in");
        assertEquals(30, r.size());
        assertEquals("V001|RES|925.40000000", r.get(0));
        assertEquals("V005|APO|1000.12345678", r.get(1));
        assertEquals("NOX02|RES|0", r.get(2));
    }

    @Test
    @DisplayName("FM-VDO-03 D-NOR-9 SINTETICO: dos filas de ValorTipoCambio para una serie (la clave de HANA lo impide) => List2Hash lanza 'key field is not unique'")
    void fmVdo03_filaRepetidaList2Hash() throws Exception {
        assertEquals("Exception: key field is not unique", excepcion("tabla-dos-filas-de-tipo-de-cambio-list2hash").replaceFirst("^\\w*Exception", "Exception"));
    }

    // ---- FM-VDO-04 what VDO does not do
    @Test
    @DisplayName("FM-VDO-04 VDO-C04: solo lectura: tablas intactas, sin tablas locales ni commits autonomos ni secuencias; sin validacion de usuario ni perfil; el contador es 0 y el servlet hace COMMIT")
    void fmVdo04_soloLectura() throws Exception {
        JsonObject d = dump("tablas-no-cambian");
        assertEquals("COMMIT", d.get("decisionServlet").getAsString());
        assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop").size());
        assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("commitsAutonomos").size());
        assertEquals(0, d.getAsJsonArray("secuenciasSolicitadas").size());
        assertEquals(List.of("V001|APO|930.10000000", "V005|RES|999.99999999"), resultados("tablas-no-cambian"));
        assertEquals("COMMIT", dump("sin-validaciones-ni-perfil").get("decisionServlet").getAsString());
        assertEquals(List.of("P_OUT_ERRORESCONTADOR", "P_OUT_RESULTADO"), new ArrayList<>(salida("sin-validaciones-ni-perfil").keySet()));
    }

    // ---- RULE-076
    @Test
    @DisplayName("RULE-076: ni el main() del legacy ni su serie real ni su conexion se copiaron (src/main y los fixtures de fondomutuo-vdo)")
    void rule076_sinElMainNiLosIdsDelLegacy() throws Exception {
        // legacy ConsultarValorDolarFondoMutuo.main (VDO:190-212): PlataformaUtils.getDESAConnection() and a JSON with the real series id below
        List<String> culpables = new ArrayList<>();
        for (java.nio.file.Path raiz : List.of(java.nio.file.Path.of("src", "main"), java.nio.file.Path.of("src", "test", "resources", "fondomutuo-vdo"))) {
            assertTrue(java.nio.file.Files.isDirectory(raiz), "RULE-076 needs " + raiz + " (run the tests from the module directory)");
            try (var s = java.nio.file.Files.walk(raiz)) {
                for (java.nio.file.Path p : s.filter(java.nio.file.Files::isRegularFile).toList()) {
                    String t = java.nio.file.Files.readString(p, java.nio.charset.StandardCharsets.ISO_8859_1);
                    if (t.contains("FMOPTIMUS_A") || t.contains("getBOLSAConnection") || t.contains("getDESAConnection")) culpables.add(p.toString());
                }
            }
        }
        assertEquals(List.of(), culpables);
    }
}
