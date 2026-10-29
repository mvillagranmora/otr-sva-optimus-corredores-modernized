package cl.bcs.fondomutuo.valorcuota;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.kit.ColaboradorErrorException;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.valorcuota.CasoCvc.Perfil;

/**
 * Rule cards of {@code CargarValorCuota} (RULES_FONDOMUTUO_F3_A.md 8.8, FM-CVC-01..14; defects D-F3A-16..23, D-NOR-9, D-NOR-11, D-REQ-7, D-REQ-11; matrix CVC-C01..C15), stated with
 * LITERAL values. The expected values are the ones the unmodified legacy produced (they equal the recorded goldens: this class and {@link CvcEquivalenciaTest} must agree; if the
 * legacy is re-recorded and a literal changes, the legacy won: update the literal and say so). Each test runs the NEW code on the H2 of its case (slug of
 * {@code cases/FMM-CVC-nnn_slug}) and names the card or defect it pins. Universe: {@code cases_cvc_lib.py} (company 1, user 7, system date 2026-03-02, FM001A CLP 4 decimals ISIN
 * CL0000000001, FM001B 2 decimals, FM001C 6 decimals with the SAME ISIN as FM001B, FM002A USD; the first historico id of a fresh session is 1000).
 *
 * <p>Besides the hand-written tests, {@link #resultadosPorRegla} and {@link #excepcionesPorRegla} walk the index of literals {@link CvcTablasLiterales} (generated from the goldens by
 * sync_cvc.py): for EVERY case, the states of the rows, the counter, the message codes, the exception class and the servlet decision, each line labelled with its rules.
 */
class CvcReglasTest {

    private static final String H = "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico";
    private static final String V = "BSCL.FondoMutuo.BD::FMM.ValorCuota";
    private static final String SEQ = "BSCL.FondoMutuo.BD.Secuencias::ValorCuotaHistorico";

    @AfterEach
    void limpiar() {
        SujetoCvc.logPrueba = null;
        SujetoCvc.orden = null;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------- helpers

    private static JsonObject r(String slug) throws Exception {
        return CasoCvc.ejecutar(CasoCvc.idPorSlug(slug), Perfil.ABIERTA);
    }

    private static JsonArray tabla(JsonObject r, String seccion, String t) {
        return r.getAsJsonObject(seccion).getAsJsonArray(t);
    }

    private static JsonObject fila(JsonArray t, int i) {
        return t.get(i).getAsJsonObject();
    }

    private static String s(JsonObject o, String k) {
        return o.get(k).isJsonNull() ? null : o.get(k).getAsString();
    }

    private static JsonObject res(JsonObject r, int i) {
        return r.getAsJsonObject("salida").getAsJsonArray("P_OUT_RESULTADO").get(i).getAsJsonObject();
    }

    private static List<String> codigos(JsonObject r) {
        List<String> l = new ArrayList<>();
        for (JsonElement m : r.getAsJsonObject("salida").getAsJsonArray("P_OUT_MENSAJES")) l.add(s(m.getAsJsonObject(), "codigo"));
        return l;
    }

    private static List<String> estados(JsonObject r) {
        List<String> l = new ArrayList<>();
        for (JsonElement m : r.getAsJsonObject("salida").getAsJsonArray("P_OUT_RESULTADO")) l.add(s(m.getAsJsonObject(), "DescEstado"));
        return l;
    }

    private static long contador(JsonObject r) {
        return r.getAsJsonObject("salida").get("P_OUT_ERRORESCONTADOR").getAsLong();
    }

    private static String texto(JsonObject o, String k) {
        return o.get(k).getAsBigDecimal().toPlainString();
    }

    private static void sinEscrituras(JsonObject r) {
        assertEquals(r.getAsJsonObject("tablasIniciales").toString(), r.getAsJsonObject("tablasTrasLaLlamada").toString(), "nothing was written");
        assertEquals(r.getAsJsonObject("tablasIniciales").toString(), r.getAsJsonObject("tablas").toString());
        assertEquals(0, r.getAsJsonArray("colaboradores").size(), "AgregarLog was not called");
        assertEquals(0, r.getAsJsonArray("secuenciasSolicitadas").size(), "no sequence value was taken");
    }

    // ------------------------------------------------------------------------------------------------------------ every case, by its index of literals

    static Stream<Arguments> resultadosPorRegla() {
        return CvcTablasLiterales.RESULTADOS.lines().filter(l -> !l.isBlank()).map(l -> l.split("\\|", -1)).map(p -> Arguments.of(p[0], p[1], p[2], p[3], p[4], p[5]));
    }

    static Stream<Arguments> excepcionesPorRegla() {
        return CvcTablasLiterales.EXCEPCIONES.lines().filter(l -> !l.isBlank()).map(l -> l.split("\\|", -1)).map(p -> Arguments.of(p[0], p[1], p[2], p[3]));
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("resultadosPorRegla")
    @DisplayName("FM-CVC-02/04/12: states of the rows, counter, message codes and servlet decision of every case that answers")
    void resultadosPorRegla(String slug, String reglas, String estados, String contador, String codigos, String decision) throws Exception {
        JsonObject r = r(slug);
        assertEquals(estados.isEmpty() ? List.of() : List.of(estados.split(",")), estados(r), slug + " :: states of the rows");
        assertEquals(Long.parseLong(contador), contador(r), slug + " :: P_OUT_ERRORESCONTADOR");
        assertEquals(codigos.isEmpty() ? List.of() : List.of(codigos.split(",")), codigos(r), slug + " :: message codes");
        assertEquals(decision, s(r, "decisionServlet"), slug + " :: CallServlet decision (commit only with counter 0)");
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("excepcionesPorRegla")
    @DisplayName("FM-CVC-01..14 / D-NOR-9 / D-F3A-22: the cases that END WITH AN EXCEPTION (class and HTTP 500 rollback)")
    void excepcionesPorRegla(String slug, String reglas, String clase, String decision) throws Exception {
        JsonObject r = r(slug);
        String e = s(r, "excepcion");
        if (slug.equals("empresa-sin-configuracion")) {
            assertTrue(e.startsWith("NullPointerException:") || e.startsWith("Exception: Parametro "), "DESVIO-KIT-1: legacy NPE or the kit's ParametroNoEncontradoException: " + e);
        } else {
            assertEquals(clase, e.substring(0, e.indexOf(':')), slug + " :: exception class");
        }
        assertEquals(decision, s(r, "decisionServlet"));
        assertFalse(r.has("salida"), "an exception leaves no output");
    }

    // ----------------------------------------------------------------------------------------------------------------------------------- FM-CVC-01

    @Test
    @DisplayName("FM-CVC-01: la salida tiene los campos del legacy, en su orden, con la fecha yyyy-MM-dd HH:mm:ss.SSS y la escala de cada numero")
    void fmCvc01_salidaDelLegacy() throws Exception {
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_RESULTADO\":[{\"IdSerieFondo\":\"FM001A\",\"IdMoneda\":\"CLP\",\"ISIN\":\"\",\"FechaVigencia\":\"2026-03-02 00:00:00.000\","
                + "\"ValorCuota\":1500.5,\"ValorCuotaFechaAnterior\":0,\"Observacion\":\"Carga de prueba\",\"DecimalesValorCuota\":4,\"FechaCalculo\":\"2026-03-02 00:00:00.000\","
                + "\"Estado\":\"OK\",\"DescEstado\":\"OK\",\"IdEmpresa\":1,\"EmisorValorCuota\":\"AGF1\",\"ParidadValorCuota\":1,\"ValorCuotaCalculado\":1500.5,\"VariacionValorCuota\":0}],"
                + "\"P_OUT_MENSAJES\":[]}", s(r("real-una-serie-una-fecha"), "salidaCruda"));
    }

    @Test
    @DisplayName("FM-CVC-01: p_UsaReglasNegocio no se lee, los campos desconocidos se ignoran y ISIN / REGISTRO fuera del modo ISIN no cambian nada")
    void fmCvc01_camposQueNoSeLeen() throws Exception {
        String esperada = s(r("real-una-serie-una-fecha"), "salidaCruda");
        for (String slug : List.of("reglas-de-negocio-no-se-lee", "reglas-de-negocio-ausente", "campos-desconocidos-se-ignoran")) {
            assertEquals(esperada, s(r(slug), "salidaCruda"), slug);
        }
        // outside the ISIN mode the ISIN of the row is only copied to the result; FECHACALCULO (2026-02-01) and REGISTRO are not read: the date of calculation is the system date
        JsonObject conIsin = r("contrato-con-isin-y-registro-fuera-de-modo-isin");
        assertEquals("CL0000000001", s(res(conIsin, 0), "ISIN"));
        assertEquals("2026-03-02 00:00:00.000", s(res(conIsin, 0), "FechaCalculo"));
        assertEquals("FM001A", s(res(conIsin, 0), "IdSerieFondo"));
    }

    @Test
    @DisplayName("FM-CVC-01: p_IdEmpresa y p_IdUsuario como texto numerico se convierten; un texto no numerico lanza JsonSyntaxException (HTTP 500)")
    void fmCvc01_conversionDeLaEmpresa() throws Exception {
        assertEquals(s(r("real-una-serie-una-fecha"), "salidaCruda"), s(r("empresa-como-texto-se-convierte"), "salidaCruda"));
        JsonObject mala = r("empresa-no-numerica-lanza");
        assertTrue(s(mala, "excepcion").startsWith("JsonSyntaxException:"));
        assertEquals("ROLLBACK_HTTP500", s(mala, "decisionServlet"));
        sinEscrituras(mala);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------- FM-CVC-02

    @Test
    @DisplayName("FM-CVC-02 / D-REQ-11: con errores de requisitos basicos el contador es el NUMERO de errores (no 1), se copian sus mensajes y no se calcula ni se escribe nada")
    void fmCvc02_contadorEsElNumeroDeErrores() throws Exception {
        JsonObject uno = r("usuario-inexistente");
        assertEquals(1, contador(uno));
        assertEquals(List.of("USUARIO_NO_EXISTE"), codigos(uno));
        assertEquals(0, uno.getAsJsonObject("salida").getAsJsonArray("P_OUT_RESULTADO").size());
        sinEscrituras(uno);
        JsonObject dos = r("usuario-y-modulo-invalidos-cuenta-errores");
        assertEquals(2, contador(dos));
        assertEquals(List.of("USUARIO_NO_EXISTE", "MODULO_NO_EXISTE"), codigos(dos));
        sinEscrituras(dos);
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":2,\"P_OUT_RESULTADO\":[],\"P_OUT_MENSAJES\":[{\"id\":0,\"tipo\":\"ERROR\",\"codigo\":\"USUARIO_NO_EXISTE\",\"descripcion\":\"Usuario no existe\"},"
                + "{\"id\":0,\"tipo\":\"ERROR\",\"codigo\":\"MODULO_NO_EXISTE\",\"descripcion\":\"Modulo no existe\"}]}", s(dos, "salidaCruda"));
    }

    @Test
    @DisplayName("FM-CVC-02: lista vacia => contador 1 y NO_SE_INGRESARON_VALORES_CUOTA (id 0, texto del diccionario); con errores previos el contador se PISA a 1 y se agrega el mensaje")
    void fmCvc02_listaVaciaPisaElContador() throws Exception {
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":1,\"P_OUT_RESULTADO\":[],\"P_OUT_MENSAJES\":[{\"id\":0,\"tipo\":\"ERROR\",\"codigo\":\"NO_SE_INGRESARON_VALORES_CUOTA\","
                + "\"descripcion\":\"No se ingresaron valores cuota\"}]}", s(r("lista-vacia-real"), "salidaCruda"));
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":1,\"P_OUT_RESULTADO\":[],\"P_OUT_MENSAJES\":[{\"id\":0,\"tipo\":\"ERROR\",\"codigo\":\"USUARIO_NO_EXISTE\",\"descripcion\":\"Usuario no existe\"},"
                + "{\"id\":0,\"tipo\":\"ERROR\",\"codigo\":\"MODULO_NO_EXISTE\",\"descripcion\":\"Modulo no existe\"},{\"id\":0,\"tipo\":\"ERROR\",\"codigo\":\"NO_SE_INGRESARON_VALORES_CUOTA\","
                + "\"descripcion\":\"No se ingresaron valores cuota\"}]}", s(r("requisitos-con-errores-y-lista-vacia-pisa-el-contador"), "salidaCruda"));
        for (String slug : List.of("lista-vacia-real", "lista-vacia-simulacion", "lista-ausente-toma-el-defecto-vacio", "entrada-vacia-objeto")) sinEscrituras(r(slug));
    }

    // ----------------------------------------------------------------------------------------------------------------------------------- FM-CVC-03

    @Test
    @DisplayName("FM-CVC-03: p_AllFunds = S (sin distinguir mayusculas) => IDSERIEFONDO es un ISIN; la fila usa la serie REAL y el ISIN queda en el resultado")
    void fmCvc03_isinConocido() throws Exception {
        for (String slug : List.of("isin-conocido-real", "isin-minuscula-s-tambien-es-modo-isin")) {
            JsonObject r = r(slug);
            assertEquals("FM001A", s(res(r, 0), "IdSerieFondo"));
            assertEquals("CL0000000001", s(res(r, 0), "ISIN"));
            assertEquals("AGF1", s(res(r, 0), "EmisorValorCuota"));
            assertEquals("FM001A", s(fila(tabla(r, "tablas", H), 0), "IdSerieFondo"));
            assertEquals("FM001A", s(fila(tabla(r, "tablas", V), 0), "IdSerieFondo"));
        }
    }

    @Test
    @DisplayName("FM-CVC-03 / D-F3A-21: un ISIN sin serie (o un id de serie en lugar del ISIN) se DESCARTA sin mensaje; sin ninguna serie la salida es vacia, contador 0 y no se escribe nada")
    void fmCvc03_isinSinSerieSeDescartaEnSilencio() throws Exception {
        for (String slug : List.of("isin-sin-serie-se-descarta-sin-mensaje", "isin-en-lugar-del-id-de-serie-se-descarta", "isin-lista-sin-ninguna-serie-no-escribe")) {
            JsonObject r = r(slug);
            assertEquals("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_RESULTADO\":[],\"P_OUT_MENSAJES\":[]}", s(r, "salidaCruda"), slug);
            assertEquals("COMMIT", s(r, "decisionServlet"));
            sinEscrituras(r);
        }
        JsonObject mixto = r("isin-sin-serie-con-otro-conocido");
        assertEquals(1, mixto.getAsJsonObject("salida").getAsJsonArray("P_OUT_RESULTADO").size());
        assertEquals(List.of("OK"), estados(mixto));
    }

    @Test
    @DisplayName("FM-CVC-03 / D-F3A-21: un ISIN en varias series genera una fila por serie con el mismo valor (ids 1000 y 1001, una sola peticion de secuencia de 2)")
    void fmCvc03_isinEnVariasSeries() throws Exception {
        JsonObject r = r("isin-en-varias-series-genera-una-fila-por-serie");
        assertEquals(List.of("OK", "OK"), estados(r));
        assertEquals("FM001B", s(res(r, 0), "IdSerieFondo"));
        assertEquals("FM001C", s(res(r, 1), "IdSerieFondo"));
        assertEquals(2, tabla(r, "tablas", V).size());
        assertEquals("1000", s(fila(tabla(r, "tablas", H), 0), "IdValorCuotaHistorico"));
        assertEquals("1001", s(fila(tabla(r, "tablas", H), 1), "IdValorCuotaHistorico"));
        assertEquals("[{\"secuencia\":\"" + SEQ + "\",\"cantidad\":2}]", r.getAsJsonArray("secuenciasSolicitadas").toString());
    }

    @Test
    @DisplayName("FM-CVC-03 / D-F3A-21 / D-NOR-9: un ISIN repetido en la entrada lanza IllegalStateException (toMap) antes de leer nada mas")
    void fmCvc03_isinRepetidoLanza() throws Exception {
        JsonObject r = r("isin-repetido-en-la-entrada-lanza");
        assertTrue(s(r, "excepcion").startsWith("IllegalStateException: Duplicate key CL0000000001"));
        assertEquals("ROLLBACK_HTTP500", s(r, "decisionServlet"));
        sinEscrituras(r);
    }

    @Test
    @DisplayName("FM-CVC-03 / FM-CVC-06: en modo ISIN la fecha de calculo es FECHACALCULO de la fila (tambien en el historico, el vigente y el log); vacia, nula o mal formada lanza")
    void fmCvc03_fechaCalculoDeLaFila() throws Exception {
        JsonObject r = r("isin-fechacalculo-propia-de-la-fila");
        assertEquals("2026-02-20 00:00:00.000", s(res(r, 0), "FechaCalculo"));
        assertEquals("2026-02-20", s(fila(tabla(r, "tablas", H), 0), "FechaCarga"));
        assertEquals("2026-02-20", s(fila(tabla(r, "tablas", V), 0), "FechaCarga"));
        assertEquals("2026-02-20", s(r.getAsJsonArray("colaboradores").get(0).getAsJsonObject().getAsJsonArray("argumentos").get(0).getAsJsonObject().getAsJsonArray("p_Log").get(0).getAsJsonObject(), "FechaSistemaLog"));
        assertEquals("2026-02-20 00:00:00.000", s(res(r("isin-fechacalculo-con-hora"), 0), "FechaCalculo"));
        assertEquals("ROLLBACK_HTTP500", s(r("isin-fechacalculo-vacia-lanza"), "decisionServlet"));
        // a row with an error does NOT read FECHACALCULO: the date is the default and the text may be anything
        JsonObject err = r("isin-mensajevalidacion-error-fechacalculo-no-se-lee");
        assertEquals("0001-01-01 00:00:00.000", s(res(err, 0), "FechaCalculo"));
        assertEquals(List.of("FORMATO_INVALIDO"), estados(err));
    }

    @Test
    @DisplayName("FM-CVC-03 / D-F3A-20: en modo ISIN no se valida la moneda; un ISIN vacio coincide con todas las series sin ISIN (FM004A)")
    void fmCvc03_monedaEIsinVacio() throws Exception {
        assertEquals(List.of("OK"), estados(r("isin-moneda-distinta-no-se-valida")));
        JsonObject vacio = r("isin-vacio-coincide-con-todas-las-series-sin-isin");
        assertEquals("FM004A", s(res(vacio, 0), "IdSerieFondo"));
        assertEquals("", s(res(vacio, 0), "ISIN"));
        assertEquals("CL0000000001", s(res(r("isin-isin-del-pojo-se-pisa-con-el-de-la-serie"), 0), "ISIN"));
    }

    // ----------------------------------------------------------------------------------------------------------------------------------- FM-CVC-04

    @Test
    @DisplayName("FM-CVC-04: todas las apariciones de una pareja (serie, fecha) repetida son VALOR_CUOTA_DUPLICADO, la primera tambien; el contador cuenta cada una")
    void fmCvc04_duplicadoMarcaTodas() throws Exception {
        JsonObject r = r("duplicado-misma-serie-y-fecha-marca-las-dos");
        assertEquals(List.of("VALOR_CUOTA_DUPLICADO", "VALOR_CUOTA_DUPLICADO"), estados(r));
        assertEquals(2, contador(r));
        assertEquals("0001-01-01 00:00:00.000", s(res(r, 0), "FechaVigencia"));
        assertEquals("", s(res(r, 0), "EmisorValorCuota"));
        assertEquals(List.of("OK", "OK", "OK"), estados(r("duplicado-no-aplica-a-otra-fecha-ni-otra-serie")));
        assertEquals(List.of("VALOR_CUOTA_DUPLICADO", "VALOR_CUOTA_DUPLICADO"), estados(r("duplicado-fecha-con-hora-es-la-misma-fecha")));
    }

    @Test
    @DisplayName("FM-CVC-04: el orden de las reglas es fijo: formato > duplicado > serie > valor 0 > moneda > fecha futura")
    void fmCvc04_ordenDeLasReglas() throws Exception {
        assertEquals(List.of("FORMATO_INVALIDO", "VALOR_CUOTA_DUPLICADO"), estados(r("formato-invalido-gana-a-duplicado")));
        assertEquals(List.of("VALOR_CUOTA_DUPLICADO", "VALOR_CUOTA_DUPLICADO"), estados(r("duplicado-gana-a-serie-inexistente")));
        assertEquals(List.of("VALOR_CUOTA_INVALIDO"), estados(r("valor-cero-gana-a-moneda-incorrecta")));
        assertEquals(List.of("MONEDA_INCORRECTA"), estados(r("moneda-incorrecta-gana-a-fecha-futura")));
        assertEquals(List.of("FECHA_VALOR_CUOTA_INCORRECTA"), estados(r("fecha-futura")));
        assertEquals(List.of("OK"), estados(r("fecha-igual-a-la-del-sistema-pasa")));
        assertEquals(List.of("OK"), estados(r("fecha-con-hora-futura-del-mismo-dia")));
    }

    @Test
    @DisplayName("FM-CVC-04 / D-F3A-23: valor 0 es VALOR_CUOTA_INVALIDO (tambien 0.00000000) pero un valor NEGATIVO pasa y se escribe; valor nulo o ausente lanza NullPointerException")
    void fmCvc04_valorCeroNegativoYNulo() throws Exception {
        assertEquals(List.of("VALOR_CUOTA_INVALIDO"), estados(r("valor-cero-invalido")));
        assertEquals(List.of("VALOR_CUOTA_INVALIDO"), estados(r("valor-cero-con-escala-invalido")));
        JsonObject neg = r("valor-negativo-pasa");
        assertEquals(List.of("OK"), estados(neg));
        assertEquals("-5.25", texto(fila(tabla(neg, "tablas", V), 0), "ValorCuota"));
        assertEquals("-5.25", texto(fila(tabla(neg, "tablas", H), 1), "ValorCuota"));
        assertEquals("105.200", texto(res(neg, 0), "VariacionValorCuota"), "variation of -5.25 against 100: |(-5.25 - 100) / 100| * 100, no threshold");
        for (String slug : List.of("valor-nulo-npe", "valor-ausente-npe", "valor-nulo-en-fila-con-error-de-formato-igual-npe")) {
            assertTrue(s(r(slug), "excepcion").startsWith("NullPointerException:"), slug);
        }
    }

    @Test
    @DisplayName("FM-CVC-04 / D-F3A-20: la moneda solo se compara cuando p_AllFunds es EXACTAMENTE N (con nulo, ausente, n, X o vacio no se valida y se escribe la de la entrada)")
    void fmCvc04_monedaSoloConTodosN() throws Exception {
        assertEquals(List.of("MONEDA_INCORRECTA"), estados(r("moneda-incorrecta-con-todos-N")));
        assertEquals(List.of("MONEDA_INCORRECTA"), estados(r("moneda-distinta-en-minuscula-con-todos-N")));
        for (String slug : List.of("moneda-no-se-valida-con-todos-nulo", "moneda-no-se-valida-con-todos-ausente", "moneda-no-se-valida-con-todos-n-minuscula",
                "moneda-no-se-valida-con-todos-otro-texto", "moneda-no-se-valida-con-todos-vacio")) {
            JsonObject r = r(slug);
            assertEquals(List.of("OK"), estados(r), slug);
            assertEquals("USD", s(fila(tabla(r, "tablas", H), 0), "IdMoneda"), slug + " :: the currency of the INPUT is stored, not the one of the fund (CLP)");
            assertEquals("USD", s(fila(tabla(r, "tablas", V), 0), "IdMoneda"));
        }
        assertTrue(s(r("moneda-nula-con-todos-N-npe"), "excepcion").startsWith("NullPointerException:"));
    }

    @Test
    @DisplayName("FM-CVC-04: las fechas se leen con Fecha.toDate: 10 o 19 caracteres; vacia, con milisegundos o con otro formato lanza DateTimeParseException; nula lanza NullPointerException; incluso en filas con error")
    void fmCvc04_formatoDeFechas() throws Exception {
        assertEquals(List.of("OK"), estados(r("fecha-con-hora-de-19-caracteres")));
        for (String slug : List.of("fecha-vacia-lanza", "fecha-con-milisegundos-lanza", "fecha-dia-mes-anio-lanza", "fecha-invalida-en-fila-con-error-de-formato-igual-lanza")) {
            assertTrue(s(r(slug), "excepcion").startsWith("DateTimeParseException:"), slug);
        }
        assertTrue(s(r("fecha-nula-npe"), "excepcion").startsWith("NullPointerException:"));
        assertTrue(s(r("serie-nula-npe"), "excepcion").startsWith("NullPointerException:"));
    }

    @Test
    @DisplayName("FM-CVC-04 / D-F3A-16: VALOR_EXISTE_FECHA es inalcanzable: cargar una fecha que ya existe como vigente da OK (la llave serie_fecha nunca coincide con la busqueda por serie)")
    void dF3A16_valorExisteFechaInalcanzable() throws Exception {
        JsonObject r = r("existe-valor-en-la-fecha-no-se-detecta");
        assertEquals(List.of("OK"), estados(r));
        assertEquals(0, contador(r));
        assertEquals("1500.5", texto(fila(tabla(r, "tablas", V), 0), "ValorCuota"), "the vigente value was replaced without any warning");
    }

    // ----------------------------------------------------------------------------------------------------------------------------------- FM-CVC-05 / 06

    @Test
    @DisplayName("FM-CVC-05 / D-F3A-17: el valor que se escribe es el de la entrada con toda su escala; los decimales de la serie (4) no lo truncan (1693.85619, no 1693.8561)")
    void dF3A17_elTruncadoPorDecimalesSeDescarta() throws Exception {
        JsonObject r = r("decimales-de-la-serie-4-valor-5-decimales-no-se-trunca");
        assertEquals(4, res(r, 0).get("DecimalesValorCuota").getAsLong());
        assertEquals("1693.85619", texto(res(r, 0), "ValorCuota"));
        assertEquals("1693.85619", texto(res(r, 0), "ValorCuotaCalculado"));
        assertEquals("1693.85619", texto(fila(tabla(r, "tablas", V), 0), "ValorCuota"));
        assertEquals("1693.85619", texto(fila(tabla(r, "tablas", H), 0), "ValorCuota"));
        assertEquals("1693.9", texto(fila(tabla(r("decimales-de-la-serie-0-valor-con-decimales"), "tablas", V), 0), "ValorCuota"), "a serie with 0 decimals keeps the decimals too");
        assertEquals("1.12345679", texto(fila(tabla(r("decimales-de-la-serie-8-valor-con-nueve"), "tablas", V), 0), "ValorCuota"), "the 9th decimal is rounded by the column (scale 8), not by the class");
    }

    @Test
    @DisplayName("FM-CVC-05 / D-F3A-23: el binder del lote redondea los BigDecimal a 16 decimales HALF_DOWN antes de la columna (0.123456784999999950 -> 0.12345678, ...951 -> 0.12345679)")
    void dF3A23_binderDieciseisDecimalesHalfDown() throws Exception {
        assertEquals("0.12345678", texto(fila(tabla(r("escala-mayor-a-16-half-down-del-binder"), "tablas", V), 0), "ValorCuota"));
        assertEquals("0.12345679", texto(fila(tabla(r("escala-mayor-a-16-sin-empate-sube"), "tablas", V), 0), "ValorCuota"));
        assertEquals("10.12345679", texto(fila(tabla(r("escala-del-motor-8-redondea-el-noveno-decimal"), "tablas", V), 0), "ValorCuota"));
        assertEquals("0", texto(fila(tabla(r("valor-muy-pequeno-redondea-a-cero-en-la-columna"), "tablas", V), 0), "ValorCuota"), "1E-9 is stored as 0 and is NOT rejected");
        // the OUTPUT and the LOG keep the BigDecimal as it came (text, scale and exponent)
        assertTrue(s(r("escala-mayor-a-16-half-down-del-binder"), "salidaCruda").contains("\"ValorCuota\":0.123456784999999950"));
        assertTrue(s(r("valor-con-ceros-a-la-derecha-conserva-la-escala-en-salida-y-log"), "salidaCruda").contains("\"ValorCuota\":1500.50,"));
        assertTrue(s(r("valor-con-exponente-conserva-el-texto-en-el-log"), "salidaCruda").contains("\"ValorCuota\":1.5E+2,"));
    }

    @Test
    @DisplayName("FM-CVC-05 / FM-CVC-14: valor fuera de la columna (20 enteros), observacion nula o de 101 caracteres, moneda nula o de 4 caracteres: falla la escritura del historico (SQLException, HTTP 500, rollback)")
    void fmCvc05_valoresQueNoCaben() throws Exception {
        for (String slug : List.of("valor-demasiado-grande-fallo-de-escritura", "observacion-ausente-fallo-de-escritura", "observacion-de-101-caracteres-fallo-de-escritura",
                "moneda-nula-sin-validacion-fallo-de-escritura", "moneda-de-4-caracteres-sin-validacion-fallo-de-escritura")) {
            JsonObject r = r(slug);
            assertTrue(s(r, "excepcion").startsWith("SQLException:"), slug);
            assertEquals("ROLLBACK_HTTP500", s(r, "decisionServlet"));
            assertEquals(r.getAsJsonObject("tablasIniciales").toString(), r.getAsJsonObject("tablas").toString(), slug + " :: rolled back");
            assertEquals(0, r.getAsJsonArray("colaboradores").size(), slug + " :: the log is written only after the four writes");
            assertEquals("[{\"secuencia\":\"" + SEQ + "\",\"cantidad\":1}]", r.getAsJsonArray("secuenciasSolicitadas").toString(), slug + " :: the ids were taken before the insert failed");
        }
        assertEquals("", s(fila(tabla(r("observacion-vacia-se-escribe-vacia"), "tablas", V), 0), "Observacion"));
    }

    @Test
    @DisplayName("FM-CVC-06: FechaVigencia de una fila con error es 0001-01-01 y su emisor es el texto vacio; FechaCalculo es la fecha del sistema y no la de cierre; paridad 1")
    void fmCvc06_camposDelResultado() throws Exception {
        JsonObject r = r("fecha-vigencia-de-fila-con-error-es-0001-01-01");
        assertEquals("0001-01-01 00:00:00.000", s(res(r, 0), "FechaVigencia"));
        assertEquals("", s(res(r, 0), "EmisorValorCuota"));
        assertEquals("AGF1", s(res(r, 1), "EmisorValorCuota"));
        assertEquals("2026-03-02 00:00:00.000", s(res(r, 0), "FechaCalculo"));
        assertEquals("1", texto(res(r, 0), "ParidadValorCuota"));
        assertEquals("1", texto(res(r, 0), "ValorCuotaCalculado"), "ValorCuotaCalculado is the value (not 0) even for a row with error");
        JsonObject sistema = r("fecha-calculo-es-la-fecha-del-sistema-no-la-de-cierre");
        assertEquals("2026-03-01 00:00:00.000", s(res(sistema, 0), "FechaVigencia"));
        assertEquals("2026-03-02 00:00:00.000", s(res(sistema, 0), "FechaCalculo"));
        assertEquals("2026-03-02", s(fila(tabla(sistema, "tablas", H), 0), "FechaCarga"));
        assertEquals(List.of("OK"), estados(r("fecha-sistema-con-hora")));
    }

    // ----------------------------------------------------------------------------------------------------------------------------------- FM-CVC-07

    @Test
    @DisplayName("FM-CVC-07: variacion 105 contra 100 = 5.000; 1693.8561 contra 1690 = 0.200; baja de 100 a 95 = 5.000 (valor absoluto); salto a 1100 = 1000.000 sin umbral")
    void fmCvc07_variacion() throws Exception {
        JsonObject a = r("variacion-105-contra-100-es-5");
        assertEquals("100.00000000", texto(res(a, 0), "ValorCuotaFechaAnterior"));
        assertEquals("5.000", texto(res(a, 0), "VariacionValorCuota"));
        assertEquals("0.200", texto(res(r("variacion-1693-8561-contra-1690-es-0-200"), 0), "VariacionValorCuota"));
        assertEquals("5.000", texto(res(r("variacion-es-valor-absoluto-si-baja"), 0), "VariacionValorCuota"));
        JsonObject salto = r("variacion-sin-umbral-se-escribe-un-salto-de-mil-por-ciento");
        assertEquals("1000.000", texto(res(salto, 0), "VariacionValorCuota"));
        assertEquals(List.of("OK"), estados(salto));
        assertEquals("COMMIT", s(salto, "decisionServlet"));
    }

    @Test
    @DisplayName("FM-CVC-07: sin valor anterior, con anterior 0, con cargas no ultimas, de otro dia o de otra serie la variacion es 0 y el anterior es 0")
    void fmCvc07_sinAnterior() throws Exception {
        for (String slug : List.of("variacion-anterior-cero-es-cero", "variacion-sin-valor-anterior-es-cero", "anterior-con-solo-cargas-no-ultimas-es-cero", "anterior-de-otro-dia-no-cuenta")) {
            JsonObject r = r(slug);
            assertEquals(0, res(r, 0).get("ValorCuotaFechaAnterior").getAsBigDecimal().signum(), slug);   // 0 (no row) or 0.00000000 (a stored 0)
            assertEquals("0", texto(res(r, 0), "VariacionValorCuota"), slug);
        }
        assertEquals("100.00000000", texto(res(r("anterior-solo-cuenta-la-ultima-carga-diaria"), 0), "ValorCuotaFechaAnterior"), "only EsUltimaCargaDiaria = S counts");
        JsonObject conError = r("variacion-de-filas-con-error-es-cero");
        assertEquals("5.000", texto(res(conError, 0), "VariacionValorCuota"));
        assertEquals("SERIE_NO_EXISTE", s(res(conError, 1), "DescEstado"));
        assertEquals(0, res(conError, 1).get("ValorCuotaFechaAnterior").getAsBigDecimal().signum(), "a row with error has no previous value (no serie)");
        assertEquals("0", texto(res(conError, 1), "VariacionValorCuota"));
    }

    @Test
    @DisplayName("FM-CVC-07: el dia anterior es el dia CALENDARIO anterior (diaHabil N, sin feriados, p_IdPais -1 por defecto, 56 explicito o nulo igual), tambien al cruzar mes y anio y con fecha de 19 caracteres")
    void fmCvc07_diaAnteriorCalendario() throws Exception {
        for (String slug : List.of("pais-por-defecto-menos-uno", "pais-explicito-56-feriado-no-importa", "pais-nulo-explicito", "anterior-es-el-dia-calendario-anterior-aunque-sea-feriado",
                "anterior-cruza-mes-y-anio", "anterior-con-fecha-de-entrada-con-hora")) {
            assertEquals("100.00000000", texto(res(r(slug), 0), "ValorCuotaFechaAnterior"), slug);
        }
        assertTrue(s(r("anterior-historico-de-dos-vigentes-misma-fecha-lanza"), "excepcion").startsWith("IllegalStateException: Duplicate key FM001A"));
    }

    @Test
    @DisplayName("FM-CVC-07 / D-F3A-18: el 'dia anterior' se calcula con UN objeto reutilizado: todas las filas buscan el dia anterior de la FECHACIERRE de la ULTIMA fila")
    void dF3A18_objetoDeFechaReutilizado() throws Exception {
        // rows (FM001A 03-02, FM001B 03-01): the last one is 03-01 -> day before 02-28; FM001A has a historico at 03-01 (its real previous day) that is NOT found
        JsonObject a = r("dia-anterior-reutilizado-todas-las-filas-usan-la-ultima-fecha");
        assertEquals("0", texto(res(a, 0), "ValorCuotaFechaAnterior"));
        assertEquals("40.00000000", texto(res(a, 1), "ValorCuotaFechaAnterior"));
        assertEquals("25.000", texto(res(a, 1), "VariacionValorCuota"));
        // inverted order: the last row is FM001A 03-02 -> 03-01: now FM001A finds 100 and FM001B (historico at 02-28) finds nothing
        JsonObject b = r("dia-anterior-reutilizado-la-ultima-fila-manda-aunque-sea-la-mas-antigua");
        assertEquals("0", texto(res(b, 0), "ValorCuotaFechaAnterior"));
        assertEquals("100.00000000", texto(res(b, 1), "ValorCuotaFechaAnterior"));
        assertEquals("5.000", texto(res(b, 1), "VariacionValorCuota"));
        // the same serie twice: both rows see the previous day of the LAST one (02-27 -> 02-26: 90)
        JsonObject c = r("dia-anterior-misma-serie-dos-fechas-el-anterior-es-de-la-ultima");
        assertEquals("90.00000000", texto(res(c, 0), "ValorCuotaFechaAnterior"));
        assertEquals("90.00000000", texto(res(c, 1), "ValorCuotaFechaAnterior"));
        assertEquals("16.700", texto(res(c, 0), "VariacionValorCuota"));
        assertEquals("5.600", texto(res(c, 1), "VariacionValorCuota"));
    }

    // ----------------------------------------------------------------------------------------------------------------------------------- FM-CVC-08

    @ParameterizedTest(name = "p_SimularOperacion {0}")
    @ValueSource(strings = { "simulacion-S-tres-filas-no-escribe", "simulacion-ausente-no-escribe", "simulacion-nula-no-escribe", "simulacion-n-minuscula-no-escribe",
            "simulacion-otro-texto-no-escribe", "simulacion-vacia-no-escribe" })
    @DisplayName("FM-CVC-08: solo el texto EXACTO \"N\" escribe; con S, ausente, nulo, n, otro texto o vacio calcula, responde y no escribe, no llama al log ni pide secuencia")
    void fmCvc08_soloLaNExactaEscribe(String slug) throws Exception {
        JsonObject r = r(slug);
        assertEquals(List.of("OK", "OK", "OK"), estados(r));
        assertEquals(0, contador(r));
        assertEquals("COMMIT", s(r, "decisionServlet"));
        sinEscrituras(r);
    }

    @Test
    @DisplayName("FM-CVC-08: la simulacion con filas con error devuelve los mismos mensajes y contador que la real; el calculo de la variacion corre pero no se escribe nada")
    void fmCvc08_simulacionCalculaPeroNoEscribe() throws Exception {
        JsonObject sim = r("simulacion-S-con-filas-con-error-mismos-mensajes-que-real");
        assertEquals(List.of("OK", "SERIE_NO_EXISTE"), estados(sim));
        assertEquals(1, contador(sim));
        sinEscrituras(sim);
        JsonObject var = r("simulacion-S-calcula-variacion-y-no-escribe");
        assertEquals("5.000", texto(res(var, 0), "VariacionValorCuota"));
        assertEquals(1, tabla(var, "tablas", H).size(), "only the seeded row");
        assertEquals(0, tabla(var, "tablas", V).size());
    }

    @Test
    @DisplayName("FM-CVC-08: el orden de las escrituras es 1 actualizar ultima carga, (pedir los ids), 2 insertar historico, 3 borrar vigentes, 4 insertar vigentes, 5 log")
    void fmCvc08_ordenDeLasEscrituras() throws Exception {
        List<String> orden = new ArrayList<>();
        SujetoCvc.orden = orden;
        JsonObject r = r("real-N-tres-filas-escribe-en-orden");
        assertEquals(List.of("actualizarUltimaCarga:3", "secuencia:" + SEQ + ":3", "insertarHistorico:3:ids=[1000, 1001, 1002]", "eliminarVigentes:3", "insertarVigentes:3", "log"), orden);
        assertEquals(List.of("OK", "OK", "OK"), estados(r));
        // a failing step stops the sequence there
        orden.clear();
        r("fallo-en-el-paso-1-actualizar-ultima-carga");
        assertEquals(List.of("actualizarUltimaCarga:1"), orden);
        orden.clear();
        r("fallo-en-el-paso-2-insertar-historico");
        assertEquals(List.of("actualizarUltimaCarga:2", "secuencia:" + SEQ + ":2", "insertarHistorico:2:ids=[1000, 1001]"), orden);
        orden.clear();
        r("fallo-en-el-paso-4-insertar-vigente");
        assertEquals(List.of("actualizarUltimaCarga:2", "secuencia:" + SEQ + ":2", "insertarHistorico:2:ids=[1000, 1001]", "eliminarVigentes:2", "insertarVigentes:2"), orden);
        // no OK row: the ids are not asked, nothing is inserted and the log port is NOT called (AgregarLog returns at once with no log lines); whether the steps
        // run on empty lists is not observable
        orden.clear();
        r("log-ninguna-fila-correcta-no-llama-al-log");
        assertTrue(orden.stream().noneMatch(o -> (o.startsWith("secuencia") && !o.endsWith(":0")) || o.equals("log") || (o.startsWith("insertarHistorico") && !o.startsWith("insertarHistorico:0"))), orden.toString());
    }

    // ----------------------------------------------------------------------------------------------------------------------------------- FM-CVC-09 / 10

    @Test
    @DisplayName("FM-CVC-09: la carga previa de la misma serie, fecha y empresa pasa a N y entra la nueva con S y el id de la secuencia; las demas filas del historico no se tocan")
    void fmCvc09_historico() throws Exception {
        JsonObject r = r("historico-la-carga-previa-de-la-misma-serie-y-fecha-pasa-a-N");
        JsonArray h = tabla(r, "tablas", H);
        assertEquals(2, h.size());
        assertEquals("1", s(fila(h, 0), "IdValorCuotaHistorico"));
        assertEquals("N", s(fila(h, 0), "EsUltimaCargaDiaria"));
        assertEquals("1000", s(fila(h, 1), "IdValorCuotaHistorico"));
        assertEquals("S", s(fila(h, 1), "EsUltimaCargaDiaria"));
        assertEquals("2026-03-02 14:30:00", s(fila(h, 1), "FechaIngresoRegistro"));
        for (String slug : List.of("historico-otras-fechas-de-la-serie-quedan-S", "historico-otra-empresa-misma-serie-y-fecha-queda-S", "historico-otra-serie-misma-fecha-queda-S")) {
            JsonObject o = r(slug);
            assertEquals("S", s(fila(tabla(o, "tablas", H), 0), "EsUltimaCargaDiaria"), slug + " :: untouched");
        }
        JsonObject yaN = r("historico-carga-ya-no-ultima-no-se-toca");
        assertEquals("N", s(fila(tabla(yaN, "tablas", H), 0), "EsUltimaCargaDiaria"));
        assertEquals("N", s(fila(tabla(yaN, "tablas", H), 1), "EsUltimaCargaDiaria"));
        assertEquals("S", s(fila(tabla(yaN, "tablas", H), 2), "EsUltimaCargaDiaria"));
    }

    @Test
    @DisplayName("FM-CVC-09: el historico guarda TODAS las filas OK (con ids consecutivos en el orden de la entrada), con la moneda de la entrada, y ninguna fila con error")
    void fmCvc09_todasLasFilasOkEnOrdenDeEntrada() throws Exception {
        JsonObject r = r("historico-ids-consecutivos-en-orden-de-entrada");
        JsonArray h = tabla(r, "tablas", H);
        assertEquals(List.of("FM001C", "FM001A", "FM001B"), List.of(s(fila(h, 0), "IdSerieFondo"), s(fila(h, 1), "IdSerieFondo"), s(fila(h, 2), "IdSerieFondo")));
        assertEquals(List.of("1000", "1001", "1002"), List.of(s(fila(h, 0), "IdValorCuotaHistorico"), s(fila(h, 1), "IdValorCuotaHistorico"), s(fila(h, 2), "IdValorCuotaHistorico")));
        assertEquals("[{\"secuencia\":\"" + SEQ + "\",\"cantidad\":3}]", r.getAsJsonArray("secuenciasSolicitadas").toString());
        JsonObject conError = r("historico-no-escribe-las-filas-con-error");
        assertEquals(2, tabla(conError, "tablasTrasLaLlamada", H).size(), "the row with VALOR_CUOTA_INVALIDO is not written");
        assertEquals(2, tabla(r("historico-dos-fechas-una-serie"), "tablas", H).size());
    }

    @Test
    @DisplayName("FM-CVC-10: de cada serie queda UNA fila vigente: la de la fecha MAS RECIENTE del lote (sin importar el orden de la entrada); el historico guarda las dos")
    void fmCvc10_vigenteEsLaMasRecienteDelLote() throws Exception {
        for (String slug : List.of("vigente-dos-fechas-de-la-serie-queda-la-mas-reciente", "vigente-dos-fechas-orden-invertido-queda-la-mas-reciente")) {
            JsonObject r = r(slug);
            assertEquals(1, tabla(r, "tablas", V).size(), slug);
            assertEquals("2026-03-02", s(fila(tabla(r, "tablas", V), 0), "FechaCierre"), slug);
            assertEquals("1500.5", texto(fila(tabla(r, "tablas", V), 0), "ValorCuota"), slug);
            assertEquals(2, tabla(r, "tablas", H).size(), slug);
        }
        JsonObject tres = r("vigente-tres-fechas-una-serie");
        assertEquals("2026-03-02", s(fila(tabla(tres, "tablas", V), 0), "FechaCierre"));
        assertEquals("3", texto(fila(tabla(tres, "tablas", V), 0), "ValorCuota"));
        JsonObject dos = r("vigente-dos-series-dos-fechas-cada-una");
        assertEquals(2, tabla(dos, "tablas", V).size());
        assertEquals("2026-03-02", s(fila(tabla(dos, "tablas", V), 0), "FechaCierre"));
        assertEquals("2026-03-02", s(fila(tabla(dos, "tablas", V), 1), "FechaCierre"));
        assertEquals(4, tabla(dos, "tablas", H).size());
    }

    @Test
    @DisplayName("FM-CVC-10 / D-F3A-19: NO compara con el vigente almacenado: cargar una fecha ATRASADA (02-27) pisa la vigente (03-02) y deja la atrasada")
    void dF3A19_fechaAtrasadaPisaLaVigente() throws Exception {
        JsonObject r = r("vigente-fecha-atrasada-pisa-la-vigente");
        assertEquals(List.of("OK"), estados(r));
        JsonArray v = tabla(r, "tablas", V);
        assertEquals(1, v.size());
        assertEquals("2026-02-27", s(fila(v, 0), "FechaCierre"));
        assertEquals("90", texto(fila(v, 0), "ValorCuota"));
        JsonObject lote = r("vigente-lote-con-fecha-atrasada-y-reciente-queda-la-reciente-del-lote");
        assertEquals("2026-03-01", s(fila(tabla(lote, "tablas", V), 0), "FechaCierre"), "the most recent of the BATCH (03-01), even if the stored one was 03-02");
        assertEquals("2026-03-02", s(fila(tabla(r("vigente-fecha-posterior-reemplaza"), "tablas", V), 0), "FechaCierre"));
    }

    @Test
    @DisplayName("FM-CVC-10: FMM.ValorCuota tiene PK (IdSerieFondo), una fila por serie: el DELETE por serie reemplaza solo la fila de esa serie; las otras series no se tocan; una fila con error no reemplaza nada")
    void fmCvc10_unaFilaPorSerie() throws Exception {
        JsonObject r = r("vigente-el-delete-por-serie-reemplaza-solo-la-fila-de-esa-serie");
        JsonArray v = tabla(r, "tablasTrasLaLlamada", V);
        assertEquals(2, v.size());
        assertEquals("FM001A", s(fila(v, 0), "IdSerieFondo"));
        assertEquals("110", texto(fila(v, 0), "ValorCuota"));
        assertEquals("2026-03-02", s(fila(v, 0), "FechaCierre"));
        assertEquals("FM001B", s(fila(v, 1), "IdSerieFondo"));
        assertEquals("5", texto(fila(v, 1), "ValorCuota"));
        assertEquals("1", s(fila(tabla(r("vigente-usa-la-empresa-de-la-peticion-no-la-de-la-serie"), "tablas", V), 0), "IdEmpresa"));
        JsonObject err = r("vigente-fila-con-error-no-reemplaza");
        assertEquals(1, tabla(err, "tablas", V).size());
        assertEquals("100", texto(fila(tabla(err, "tablas", V), 0), "ValorCuota"));
    }

    // ----------------------------------------------------------------------------------------------------------------------------------- FM-CVC-11

    private static JsonObject entradaLog(JsonObject r, int i) {
        return r.getAsJsonArray("colaboradores").get(i).getAsJsonObject().getAsJsonArray("argumentos").get(0).getAsJsonObject();
    }

    @Test
    @DisplayName("FM-CVC-11: AgregarLog recibe un log por fila OK: cabecera 159, MODIFICACION, FMM, VALOR_CUOTA_FMM, 'Cargar Valor Cuota', fecha = FechaCalculo, y un detalle (serie, 'VALOR_CUOTA_FMM <fecha>', valor)")
    void fmCvc11_contenidoDelLog() throws Exception {
        JsonObject r = r("log-una-fila-con-sus-campos-fijos");
        assertEquals(1, r.getAsJsonArray("colaboradores").size());
        assertEquals("AgregarLog", s(r.getAsJsonArray("colaboradores").get(0).getAsJsonObject(), "colaborador"));
        JsonObject e = entradaLog(r, 0);
        assertTrue(e.get("p_IdEmpresa").isJsonNull() && e.get("p_IdUsuario").isJsonNull() && e.get("p_IdModulo").isJsonNull(), "the header of the request is NOT filled (nulls are written)");
        assertEquals(0, e.get("p_IdTrabajo").getAsLong());
        JsonObject l = e.getAsJsonArray("p_Log").get(0).getAsJsonObject();
        assertEquals(159, l.get("IdCabeceraLog").getAsLong());
        assertEquals("MODIFICACION", s(l, "IdTipoLog"));
        assertEquals("FMM", s(l, "IdModulo"));
        assertEquals(7, l.get("IdUsuario").getAsLong());
        assertEquals(7, l.get("IdUsuarioAtendido").getAsLong());
        assertEquals(1, l.get("IdEmpresa").getAsLong());
        assertEquals("2026-03-02", s(l, "FechaSistemaLog"));
        assertEquals("VALOR_CUOTA_FMM", s(l, "IdTipoRegistro"));
        assertEquals("Cargar Valor Cuota", s(l, "NombreProceso"));
        assertEquals(0, l.get("IdCliente").getAsLong());
        assertEquals(0, l.get("Folio").getAsLong());
        assertEquals("", s(l, "Nemo"));
        assertEquals(0, l.get("IdTrabajo").getAsLong());
        JsonObject d = e.getAsJsonArray("p_LogDetalle").get(0).getAsJsonObject();
        assertEquals(0, d.get("IdTrabajo").getAsLong());
        assertEquals(159, d.get("IdCabeceraLog").getAsLong());
        assertEquals("FM001A", s(d, "SubRegistro"));
        assertEquals("VALOR_CUOTA_FMM 2026-03-02", s(d, "CampoRegistro"));
        assertEquals("", s(d, "ValorAntiguo"));
        assertEquals("1500.5", s(d, "ValorNuevo"));
        assertEquals("1500.50", s(entradaLog(r("valor-con-ceros-a-la-derecha-conserva-la-escala-en-salida-y-log"), 0).getAsJsonArray("p_LogDetalle").get(0).getAsJsonObject(), "ValorNuevo"), "toString() of the BigDecimal");
        assertEquals("1.5E+2", s(entradaLog(r("valor-con-exponente-conserva-el-texto-en-el-log"), 0).getAsJsonArray("p_LogDetalle").get(0).getAsJsonObject(), "ValorNuevo"));
    }

    @Test
    @DisplayName("FM-CVC-11: solo las filas OK van al log (una por fila, en el orden de la entrada); sin filas OK, o en simulacion, NO se llama al puerto (AgregarLog devuelve de inmediato con la lista vacia)")
    void fmCvc11_soloFilasOkYNuncaVacio() throws Exception {
        JsonObject mixto = r("log-solo-de-las-filas-correctas");
        assertEquals(1, mixto.getAsJsonArray("colaboradores").size());
        assertEquals(2, entradaLog(mixto, 0).getAsJsonArray("p_Log").size());
        assertEquals("FM001A", s(entradaLog(mixto, 0).getAsJsonArray("p_LogDetalle").get(0).getAsJsonObject(), "SubRegistro"));
        assertEquals("FM001B", s(entradaLog(mixto, 0).getAsJsonArray("p_LogDetalle").get(1).getAsJsonObject(), "SubRegistro"));
        assertEquals(0, r("log-ninguna-fila-correcta-no-llama-al-log").getAsJsonArray("colaboradores").size());
        assertEquals(0, r("log-en-simulacion-no-se-llama").getAsJsonArray("colaboradores").size());
        assertEquals("FM001B", s(entradaLog(r("log-modo-isin-subregistro-es-la-serie-real"), 0).getAsJsonArray("p_LogDetalle").get(0).getAsJsonObject(), "SubRegistro"));
        assertEquals("2026-02-20", s(entradaLog(r("log-fecha-del-log-es-la-fecha-de-calculo"), 0).getAsJsonArray("p_Log").get(0).getAsJsonObject(), "FechaSistemaLog"));
    }

    @Test
    @DisplayName("FM-CVC-11 / D-F3A-22 / D-NOR-11 (decision 2): si AgregarLog falla NO afecta a la carga: se traga cualquier excepcion; el contador, la salida y la escritura son los mismos")
    void fmCvc11_elFalloDelLogSeTraga() throws Exception {
        JsonObject conFallo = r("log-falla-se-traga-y-la-carga-sigue");
        assertEquals(s(r("real-una-serie-una-fecha"), "salidaCruda"), s(conFallo, "salidaCruda"));
        assertEquals("COMMIT", s(conFallo, "decisionServlet"));
        assertEquals(1, tabla(conFallo, "tablas", H).size());
        assertEquals(1, tabla(conFallo, "tablas", V).size());
        assertEquals("error: log caido", s(conFallo.getAsJsonArray("colaboradores").get(0).getAsJsonObject(), "respuesta"));
        JsonObject mixto = r("log-falla-con-lote-mixto-sigue-el-contador-de-errores");
        assertEquals(1, contador(mixto));
        assertEquals("ROLLBACK", s(mixto, "decisionServlet"));
    }

    @Test
    @DisplayName("FM-CVC-11 (decision 2): el puerto del log puede fallar de cualquier forma (RuntimeException, 503 del router, ColaboradorError, indeterminado): ninguna cambia la carga; un Error de la JVM NO se traga (el legacy atrapa Exception)")
    void fmCvc11_cualquierExcepcionDelPuertoSeTraga() throws Exception {
        String esperada = s(r("real-una-serie-una-fecha"), "salidaCruda");
        List<RuntimeException> fallos = List.of(new IllegalStateException("timeout"), new ServicioNoServidoException("ROUTER", "AgregarLog 503"),
                new ColaboradorErrorException("AgregarLog 500", new RuntimeException("x")), new NullPointerException("respuesta vacia"));
        for (RuntimeException fallo : fallos) {
            SujetoCvc.logPrueba = e -> {
                throw fallo;
            };
            JsonObject r = r("real-una-serie-una-fecha");
            assertEquals(esperada, s(r, "salidaCruda"), fallo.getClass().getSimpleName());
            assertEquals("COMMIT", s(r, "decisionServlet"), fallo.getClass().getSimpleName() + " :: a failing audit is not a failing load");
            assertEquals(1, tabla(r, "tablas", H).size());
            assertEquals(1, tabla(r, "tablas", V).size());
        }
        SujetoCvc.logPrueba = e -> {
            throw new AssertionError("fatal");
        };
        JsonObject r = r("real-una-serie-una-fecha");
        assertEquals("ROLLBACK_HTTP500", s(r, "decisionServlet"));
        assertTrue(s(r, "excepcion").startsWith("AssertionError:"));
    }

    // ----------------------------------------------------------------------------------------------------------------------------------- FM-CVC-12

    @Test
    @DisplayName("FM-CVC-12 / D-REQ-11: un mensaje de error por fila ERR (codigo = DescEstado, texto del diccionario o '<codigo> sin traduccion'); el contador es el numero de errores; las OK no generan mensaje")
    void fmCvc12_mensajesYContador() throws Exception {
        JsonObject r = r("dos-errores-contador-dos-mensajes-con-y-sin-traduccion");
        assertEquals(3, contador(r));
        assertEquals("{\"id\":0,\"tipo\":\"ERROR\",\"codigo\":\"SERIE_NO_EXISTE\",\"descripcion\":\"La serie no existe\"}", r.getAsJsonObject("salida").getAsJsonArray("P_OUT_MENSAJES").get(0).toString());
        assertEquals("{\"id\":0,\"tipo\":\"ERROR\",\"codigo\":\"FECHA_VALOR_CUOTA_INCORRECTA\",\"descripcion\":\"FECHA_VALOR_CUOTA_INCORRECTA sin traduccion\"}",
                r.getAsJsonObject("salida").getAsJsonArray("P_OUT_MENSAJES").get(2).toString());
        JsonObject mixtaRepetida = r("errores-repetidos-de-una-misma-fila-duplicada-cuentan-dos");
        assertEquals(2, contador(mixtaRepetida));
        assertEquals(List.of("VALOR_CUOTA_DUPLICADO", "VALOR_CUOTA_DUPLICADO", "OK"), estados(mixtaRepetida));
        assertEquals(0, r("todo-correcto-contador-cero-sin-mensajes").getAsJsonObject("salida").getAsJsonArray("P_OUT_MENSAJES").size());
    }

    // ----------------------------------------------------------------------------------------------------------------------------------- FM-CVC-13

    @Test
    @DisplayName("FM-CVC-13 / D-F3A-16: repetir la misma carga no falla ni se detecta: el historico gana una fila (la primera pasa a N, la nueva es S con otro id), el vigente queda igual y hay otro log")
    void fmCvc13_repetirLaMismaCarga() throws Exception {
        JsonObject r = r("repetir-la-misma-carga-el-historico-crece-una-fila");
        assertEquals("COMMIT", s(r.getAsJsonArray("previas").get(0).getAsJsonObject(), "decisionServlet"));
        assertEquals(0, contador(r));
        JsonArray h = tabla(r, "tablas", H);
        assertEquals(2, h.size());
        assertEquals(List.of("1000", "N"), List.of(s(fila(h, 0), "IdValorCuotaHistorico"), s(fila(h, 0), "EsUltimaCargaDiaria")));
        assertEquals(List.of("1001", "S"), List.of(s(fila(h, 1), "IdValorCuotaHistorico"), s(fila(h, 1), "EsUltimaCargaDiaria")));
        assertEquals(1, tabla(r, "tablas", V).size());
        assertEquals(2, r.getAsJsonArray("colaboradores").size(), "one audit line per load");
        JsonObject tres = r("repetir-tres-veces-la-misma-carga");
        assertEquals(3, tabla(tres, "tablas", H).size());
        assertEquals(List.of("N", "N", "S"), List.of(s(fila(tabla(tres, "tablas", H), 0), "EsUltimaCargaDiaria"), s(fila(tabla(tres, "tablas", H), 1), "EsUltimaCargaDiaria"),
                s(fila(tabla(tres, "tablas", H), 2), "EsUltimaCargaDiaria")));
        JsonObject otroValor = r("repetir-con-otro-valor-la-segunda-queda-vigente");
        assertEquals("1600", texto(fila(tabla(otroValor, "tablas", V), 0), "ValorCuota"));
        sinEscrituras(r("repetir-en-simulacion-no-escribe-nunca"));
    }

    @Test
    @DisplayName("FM-CVC-13 / FM-CVC-14: tras una carga con error que el anfitrion revirtio, repetirla sin el error deja UNA fila (la primera no existe); dos fechas sucesivas ven el anterior de la primera")
    void fmCvc13_repetirTrasUnaCargaRevertida() throws Exception {
        JsonObject r = r("repetir-tras-una-carga-con-error-la-primera-se-revirtio");
        assertEquals("ROLLBACK", s(r.getAsJsonArray("previas").get(0).getAsJsonObject(), "decisionServlet"));
        assertEquals(1, tabla(r, "tablas", H).size());
        assertEquals("S", s(fila(tabla(r, "tablas", H), 0), "EsUltimaCargaDiaria"));
        assertEquals("1001", s(fila(tabla(r, "tablas", H), 0), "IdValorCuotaHistorico"), "the first id (1000) was consumed by the reverted load: sequences are not transactional");
        JsonObject sucesivas = r("dos-cargas-de-fechas-sucesivas-la-segunda-ve-el-anterior-de-la-primera");
        assertEquals("100.00000000", texto(res(sucesivas, 0), "ValorCuotaFechaAnterior"));
        assertEquals("5.000", texto(res(sucesivas, 0), "VariacionValorCuota"));
    }

    // ----------------------------------------------------------------------------------------------------------------------------------- FM-CVC-14

    @Test
    @DisplayName("FM-CVC-14 / D-F3A-22: un lote mixto ESCRIBE las filas OK antes de contar los errores (la clase no hace commit ni rollback); el anfitrion ve contador 1 y revierte TODO")
    void fmCvc14_loteMixtoEscribeAntesDeContar() throws Exception {
        JsonObject r = r("lote-mixto-escribe-antes-de-contar-y-el-anfitrion-revierte-todo");
        assertEquals(1, contador(r));
        assertEquals(List.of("OK", "OK", "SERIE_NO_EXISTE"), estados(r));
        assertEquals(3, tabla(r, "tablasTrasLaLlamada", H).size(), "seeded row (now N) + the 2 rows of the OK series written BEFORE the errors were counted");
        assertEquals("N", s(fila(tabla(r, "tablasTrasLaLlamada", H), 0), "EsUltimaCargaDiaria"));
        assertEquals(2, tabla(r, "tablasTrasLaLlamada", V).size());
        assertEquals(1, r.getAsJsonArray("colaboradores").size(), "the audit line of the OK rows was written too");
        assertEquals("ROLLBACK", s(r, "decisionServlet"));
        assertEquals(r.getAsJsonObject("tablasIniciales").toString(), r.getAsJsonObject("tablas").toString(), "rollback of the whole batch, the OK rows included");
        assertEquals("S", s(fila(tabla(r, "tablas", H), 0), "EsUltimaCargaDiaria"));
        JsonObject formato = r("lote-mixto-con-formato-invalido");
        assertEquals("ROLLBACK", s(formato, "decisionServlet"));
        assertEquals(1, tabla(formato, "tablasTrasLaLlamada", H).size());
        assertEquals(0, tabla(formato, "tablas", H).size());
    }

    @Test
    @DisplayName("FM-CVC-14: sin errores el anfitrion confirma (contador 0 = commit) y quedan escritas todas las filas OK")
    void fmCvc14_sinErroresConfirma() throws Exception {
        JsonObject r = r("lote-sin-errores-el-anfitrion-confirma");
        assertEquals("COMMIT", s(r, "decisionServlet"));
        assertEquals(3, tabla(r, "tablas", H).size());
        assertEquals(2, tabla(r, "tablas", V).size());
        assertEquals(r.getAsJsonObject("tablasTrasLaLlamada").toString(), r.getAsJsonObject("tablas").toString());
    }

    @Test
    @DisplayName("FM-CVC-14 (d) / D-NOR-11: si una escritura lanza, la excepcion sale (HTTP 500), queda lo escrito hasta ese paso en la sesion y el anfitrion revierte; el log no se llama")
    void fmCvc14_falloParcial() throws Exception {
        JsonObject p1 = r("fallo-en-el-paso-1-actualizar-ultima-carga");
        assertEquals("ROLLBACK_HTTP500", s(p1, "decisionServlet"));
        assertEquals(0, p1.getAsJsonArray("secuenciasSolicitadas").size());
        assertEquals(1, tabla(p1, "tablasTrasLaLlamada", H).size());
        JsonObject p2 = r("fallo-en-el-paso-2-insertar-historico");
        assertEquals("[{\"secuencia\":\"" + SEQ + "\",\"cantidad\":2}]", p2.getAsJsonArray("secuenciasSolicitadas").toString());
        assertEquals(2, tabla(p2, "tablasTrasLaLlamada", H).size(), "the first row of the batch is in (and the previous one is now N); the second one failed");
        assertEquals("N", s(fila(tabla(p2, "tablasTrasLaLlamada", H), 0), "EsUltimaCargaDiaria"));
        assertEquals(1, tabla(p2, "tablasTrasLaLlamada", V).size());
        JsonObject p4 = r("fallo-en-el-paso-4-insertar-vigente");
        assertEquals(1, tabla(p4, "tablasTrasLaLlamada", V).size(), "step 3 deleted FM001A and FM001B, step 4 inserted FM001A and failed on FM001B");
        assertEquals("FM001A", s(fila(tabla(p4, "tablasTrasLaLlamada", V), 0), "IdSerieFondo"));
        assertEquals(3, tabla(p4, "tablasTrasLaLlamada", H).size());
        for (JsonObject p : List.of(p1, p2, p4)) {
            assertEquals(0, p.getAsJsonArray("colaboradores").size());
            assertEquals(p.getAsJsonObject("tablasIniciales").toString(), p.getAsJsonObject("tablas").toString());
            assertTrue(s(p, "excepcion").startsWith("SQLException:"));
        }
        sinEscrituras(r("fallo-de-escritura-en-simulacion-no-ocurre"));
        assertEquals(0, r("log-y-fallo-de-escritura-el-log-no-se-llama").getAsJsonArray("colaboradores").size());
    }

    // ----------------------------------------------------------------------------------------------------------------------------------- configuration

    @Test
    @DisplayName("FM-CVC-04 / DESVIO-KIT-1: empresa sin configuracion = NullPointerException del legacy (el kit: ParametroNoEncontradoException); falta PLA.FechaSistema = Exception 'Parametro ... no encontrado'; fecha mal formada = DateTimeParseException")
    void configuracionDeLaEmpresa() throws Exception {
        String e = s(r("empresa-sin-configuracion"), "excepcion");
        assertTrue(e.startsWith("NullPointerException:") || e.startsWith("Exception: Parametro PLA.FechaSistema en modulo PLATAFORMA no encontrado"), e);
        assertEquals("Exception: Parametro PLA.FechaSistema en modulo PLATAFORMA no encontrado", s(r("empresa-con-modulo-pero-sin-fecha-de-sistema-excepcion-de-parametro"), "excepcion"));
        assertEquals("Exception: Parametro PLA.FechaSistema en modulo PLATAFORMA no encontrado", s(r("fecha-sistema-ausente-excepcion-de-parametro"), "excepcion"));
        assertTrue(s(r("fecha-sistema-mal-formada-lanza"), "excepcion").startsWith("DateTimeParseException:"));
        assertEquals(List.of("OK"), estados(r("fecha-sistema-posterior-permite-fecha-futura-de-base")));
    }
}
