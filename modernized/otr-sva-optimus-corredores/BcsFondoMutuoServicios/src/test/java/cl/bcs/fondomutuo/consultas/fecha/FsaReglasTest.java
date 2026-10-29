package cl.bcs.fondomutuo.consultas.fecha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.fecha.CasoFsa.Perfil;

/**
 * Rule cards of {@code ConsultarFechaSolicitudAporteRescate} (RULES_FONDOMUTUO_F3_A.md 8.1, FM-FSA-01..08; defects D-F3A-01/02, D-NOR-9; matrix FSA-C01..C09),
 * stated with LITERAL values. The expected values are the ones the unmodified legacy produced (they equal the recorded goldens: this class and
 * {@link FsaEquivalenciaTest} must agree; if the legacy is re-recorded and a literal changes, the legacy won: update the literal and say so). Each test runs the
 * NEW code on the H2 of its case (slug of {@code cases/FMM-FSA-nn_slug}) and names the card or defect it pins.
 * Date order in {@link #fechas}: Emision|Digitacion|Aceptacion(curse)|ValorCuota|Certificacion|Liquidacion. Universe of the cases: see {@code seed/base-fsa.sql}
 * (series FSN01/02 national, FSX01..07 foreign, FSD01 national dollar; clock Monday 2026-03-02; Saturday/Sunday and 2026-03-05 (country 56) non working).
 */
class FsaReglasTest {

    private static final String RES_NAC = "2026-03-02|2026-03-02|2026-03-03|2026-03-04|2026-03-09|2026-03-06";
    private static final String RES_EXT_SERIE = "2026-03-02|2026-03-02|2026-03-02|2026-03-05|2026-03-10|2026-03-09";
    private static final String REINV_MISMO_EMISOR = "2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-04|2026-03-04";
    private static final String REINV_EMISORES_DISTINTOS = "2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-04|2026-03-06";

    private static JsonObject dump(String slug) throws Exception {
        return CasoFsa.ejecutar(CasoFsa.idPorSlug(slug), Perfil.ABIERTA);
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

    private static JsonArray resultado(JsonObject salida) {
        return salida.getAsJsonArray("P_OUT_RESULTADO");
    }

    private static String fechas(JsonObject salida, int i) {
        JsonObject r = resultado(salida).get(i).getAsJsonObject();
        List<String> f = new ArrayList<>();
        for (String k : new String[] { "FechaEmision", "FechaDigitacion", "FechaAceptacion", "FechaValorCuota", "FechaCertificacion", "FechaLiquidacion" }) {
            f.add(r.get(k).getAsString().substring(0, 10));
        }
        return String.join("|", f);
    }

    private static List<String> codigos(JsonObject salida) {
        List<String> c = new ArrayList<>();
        for (JsonElement m : salida.getAsJsonArray("P_OUT_MENSAJES")) c.add(m.getAsJsonObject().get("codigo").getAsString());
        return c;
    }

    // ---- FM-FSA-01 contract
    @Test
    @DisplayName("FM-FSA-01 / FSA-C01: 1 APO nacional, perfil comercial: contrato de salida, dias de aporte de la serie")
    void fmFsa01_contratoYUnAporteNacionalComercial() throws Exception {
        JsonObject d = dump("apo-nacional-comercial");
        JsonObject s = d.getAsJsonObject("salida");
        assertEquals(List.of("P_OUT_ERRORESCONTADOR", "P_OUT_MENSAJES", "P_OUT_RESULTADO"), new ArrayList<>(s.keySet()));
        assertEquals(0L, s.get("P_OUT_ERRORESCONTADOR").getAsLong());
        assertEquals(0, s.getAsJsonArray("P_OUT_MENSAJES").size());
        assertEquals(List.of("IdInterno", "IdSerieFondo", "FechaEmision", "FechaDigitacion", "FechaAceptacion", "FechaValorCuota", "FechaCertificacion", "FechaLiquidacion"),
                new ArrayList<>(resultado(s).get(0).getAsJsonObject().keySet()));
        assertEquals("2026-03-02 00:00:00.000", resultado(s).get(0).getAsJsonObject().get("FechaEmision").getAsString());
        assertEquals("FSN01", resultado(s).get(0).getAsJsonObject().get("IdSerieFondo").getAsString());
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-02|2026-03-02", fechas(s, 0));
        assertEquals("COMMIT", d.get("decisionServlet").getAsString());
    }

    @Test
    @DisplayName("FM-FSA-01: p_SimularOperacion=S y p_UsaReglasNegocio nulo no se leen (salida identica a la del caso 01)")
    void fmFsa01_simularYReglasDeNegocioSeIgnoran() throws Exception {
        assertEquals(dump("apo-nacional-comercial").get("salidaCruda").getAsString(), dump("simular-y-reglas-se-ignoran").get("salidaCruda").getAsString());
    }

    @Test
    @DisplayName("FM-FSA-01: es de solo lectura: ni tablas locales de sesion, ni commits autonomos, ni secuencias; las tablas vuelven intactas")
    void fmFsa01_soloLectura() throws Exception {
        JsonObject d = dump("apo-nacional-comercial");
        assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop").size());
        assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("commitsAutonomos").size());
        assertEquals(0, d.getAsJsonArray("secuenciasSolicitadas").size());
        assertEquals(0, d.getAsJsonObject("tablas").getAsJsonArray("BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate").size());
    }

    // ---- FM-FSA-02 validations, in this order (FSA-C08)
    @Test
    @DisplayName("FM-FSA-02 / FSA-C08: usuario sin perfil => contador 1, PERFIL_INCORRECTO (id 0), resultado vacio; hace rollback")
    void fmFsa02_sinPerfil() throws Exception {
        JsonObject d = dump("sin-perfil");
        JsonObject s = d.getAsJsonObject("salida");
        assertEquals(1L, s.get("P_OUT_ERRORESCONTADOR").getAsLong());
        assertEquals(List.of("PERFIL_INCORRECTO"), codigos(s));
        assertEquals(0L, s.getAsJsonArray("P_OUT_MENSAJES").get(0).getAsJsonObject().get("id").getAsLong());
        assertEquals("ERROR", s.getAsJsonArray("P_OUT_MENSAJES").get(0).getAsJsonObject().get("tipo").getAsString());
        assertEquals("Perfil incorrecto", s.getAsJsonArray("P_OUT_MENSAJES").get(0).getAsJsonObject().get("descripcion").getAsString());
        assertEquals(0, resultado(s).size());
        assertEquals("ROLLBACK", d.get("decisionServlet").getAsString());
    }

    @Test
    @DisplayName("FM-FSA-02 / FSA-C08: lista vacia => contador 1, NO_SE_INGRESARON_SOLICITUDES; la clave ausente equivale a lista vacia")
    void fmFsa02_listaVacia() throws Exception {
        for (String slug : new String[] { "lista-vacia", "lista-ausente-queda-vacia" }) {
            JsonObject s = salida(slug);
            assertEquals(1L, s.get("P_OUT_ERRORESCONTADOR").getAsLong(), slug);
            assertEquals(List.of("NO_SE_INGRESARON_SOLICITUDES"), codigos(s), slug);
            assertEquals("No se ingresaron solicitudes", s.getAsJsonArray("P_OUT_MENSAJES").get(0).getAsJsonObject().get("descripcion").getAsString(), slug);
            assertEquals(0, resultado(s).size(), slug);
        }
    }

    @Test
    @DisplayName("FM-FSA-02: sin la entrada del diccionario el texto es '<codigo> sin traduccion' (kit)")
    void fmFsa02_mensajeSinTraduccion() throws Exception {
        JsonObject s = salida("lista-vacia-mensaje-sin-diccionario");
        assertEquals("NO_SE_INGRESARON_SOLICITUDES sin traduccion", s.getAsJsonArray("P_OUT_MENSAJES").get(0).getAsJsonObject().get("descripcion").getAsString());
    }

    @Test
    @DisplayName("FM-FSA-02: lista nula con perfil valido => NullPointerException; sin perfil el perfil se valida antes y no hay excepcion")
    void fmFsa02_listaNulaEsNpeSoloSiHayPerfil() throws Exception {
        assertTrue(excepcion("lista-nula-npe").startsWith("NullPointerException"));
        assertEquals(List.of("PERFIL_INCORRECTO"), codigos(salida("lista-nula-sin-perfil")));
    }

    @Test
    @DisplayName("FM-FSA-02(1): requisitos basicos con error => contador 1 (no 2) con todos sus mensajes, antes de leer el perfil o la lista")
    void fmFsa02_requisitosBasicosPrimero() throws Exception {
        assertEquals(List.of("USUARIO_NO_EXISTE"), codigos(salida("requisitos-usuario-no-existe")));
        assertEquals(List.of("USUARIO_NO_VIGENTE"), codigos(salida("requisitos-usuario-no-vigente")));
        assertEquals(List.of("MODULO_NO_EXISTE"), codigos(salida("requisitos-modulo-no-existe")));
        JsonObject dos = salida("requisitos-modulo-y-usuario-juntos");
        assertEquals(1L, dos.get("P_OUT_ERRORESCONTADOR").getAsLong());
        assertEquals(List.of("USUARIO_NO_EXISTE", "MODULO_NO_EXISTE"), codigos(dos));
        assertEquals(List.of("EMPRESA_NO_EXISTE", "USUARIO_NO_EXISTE"), codigos(salida("requisitos-empresa-no-existe")));
        assertEquals(List.of("EMPRESA_VACIA"), codigos(salida("requisitos-empresa-nula")));
        assertEquals(List.of("USUARIO_VACIO"), codigos(salida("requisitos-usuario-nulo")));
        assertEquals(List.of("MODULO_VACIO"), codigos(salida("requisitos-modulo-nulo")));
        assertEquals("Empresa vacia", salida("requisitos-empresa-nula").getAsJsonArray("P_OUT_MENSAJES").get(0).getAsJsonObject().get("descripcion").getAsString());
    }

    @Test
    @DisplayName("FM-FSA-02 D-REQ-8: usuario con dos perfiles: el SELECT TOP 1 sin ORDER BY elige (orden de H2 = insercion; el de HANA no esta probado)")
    void fmFsa02_dosPerfilesTop1() throws Exception {
        assertEquals(RES_EXT_SERIE, fechas(salida("usuario-con-dos-perfiles-top-1"), 0));
    }

    // ---- FM-FSA-03 / 04 series, country, collaborator
    @Test
    @DisplayName("FM-FSA-03: la misma serie repetida no duplica la lectura (el IN deduplica)")
    void fmFsa03_serieRepetidaNoDuplica() throws Exception {
        JsonObject s = salida("dos-solicitudes-misma-serie");
        assertEquals(2, resultado(s).size());
        assertEquals(REINV_MISMO_EMISOR, fechas(s, 0));
        assertEquals(REINV_MISMO_EMISOR, fechas(s, 1));
    }

    @Test
    @DisplayName("FM-FSA-03 D-NOR-9: dos filas para la misma serie => IllegalStateException de toMap (sintetico: HANA lo impediria con la clave de PLA.Moneda)")
    void fmFsa03_dosFilasPorSerieEsIllegalState() throws Exception {
        assertTrue(excepcion("serie-dos-filas-toMap-IllegalState").startsWith("IllegalStateException"));
    }

    @Test
    @DisplayName("FM-FSA-03: FondoNacionalDolar = S solo si el pais del fondo es PLA.PaisSistema y su moneda PLA.MonedaAdicional (FSD01: fecha valor cuota = curse)")
    void fmFsa03_fondoNacionalDolar() throws Exception {
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-02|2026-03-04|2026-03-03", fechas(salida("fondo-nacional-dolar-rescate"), 0));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-02|2026-03-02", fechas(salida("fondo-nacional-dolar-aporte"), 0));
        // without PLA.MonedaAdicional the LEFT JOIN is null and the flag is N: the value date goes back to the series valuta (03-03)
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-04|2026-03-03", fechas(salida("moneda-adicional-ausente"), 0));
        // PLA.PaisSistema = 840 makes the foreign series FSX01 (country 840, USD) a "national dollar" one
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-02|2026-03-10|2026-03-09", fechas(salida("pais-sistema-distinto"), 0));
    }

    @Test
    @DisplayName("FM-FSA-03: moneda del fondo sin fila en PLA.Moneda (LEFT JOIN): IdPaisMoneda nulo, el resultado no cambia")
    void fmFsa03_monedaSinFila() throws Exception {
        assertEquals(RES_EXT_SERIE, fechas(salida("serie-con-moneda-sin-fila"), 0));
    }

    @Test
    @DisplayName("FM-FSA-04 / FSA-C09: ConsultarEmpresa se consulta dos veces (FSA y CalcularFecha), con el id de la empresa; el pais no cambia el calculo si no hay calendario por pais")
    void fmFsa04_empresaDosVeces() throws Exception {
        JsonArray llamadas = dump("empresa-se-consulta-dos-veces").getAsJsonArray("colaboradores");
        assertEquals(2, llamadas.size());
        for (JsonElement l : llamadas) {
            assertEquals("ConsultarEmpresa", l.getAsJsonObject().get("colaborador").getAsString());
            assertEquals(1L, l.getAsJsonObject().getAsJsonArray("argumentos").get(0).getAsLong());
        }
        assertEquals(RES_EXT_SERIE, fechas(salida("empresa-pais-840"), 0));
    }

    @Test
    @DisplayName("FM-FSA-04: empresa sin IdPais => NullPointerException; ConsultarEmpresa que lanza Exception se propaga con su texto (HTTP 500)")
    void fmFsa04_empresaSinPaisYEmpresaQueFalla() throws Exception {
        assertTrue(excepcion("empresa-sin-pais").startsWith("NullPointerException"));
        assertEquals("Exception: empresa no disponible", excepcion("empresa-falla"));
    }

    @Test
    @DisplayName("FM-FSA-04 FSA-C09: serie inexistente con 1 solicitud => la entrada va con cuatro campos y el colaborador lanza NullPointerException (D-FEC-2)")
    void fmFsa04_serieInexistenteUnaSolicitud() throws Exception {
        assertTrue(excepcion("serie-inexistente-una-solicitud").startsWith("NullPointerException"));
        assertEquals(2, dump("serie-inexistente-una-solicitud").getAsJsonArray("colaboradores").size());   // FSA reached the collaborator
        assertTrue(excepcion("serie-nula").startsWith("NullPointerException"));
    }

    @Test
    @DisplayName("FM-FSA-04: FechaEmision vacia o con formato invalido => DateTimeParseException; nula => NullPointerException; con hora (19 caracteres) se acepta")
    void fmFsa04_fechaEmision() throws Exception {
        assertTrue(excepcion("emision-vacia").startsWith("DateTimeParseException"));
        assertTrue(excepcion("emision-formato-invalido").startsWith("DateTimeParseException"));
        assertTrue(excepcion("emision-nula").startsWith("NullPointerException"));
        assertEquals("2026-03-05|2026-03-05|2026-03-06|2026-03-09|2026-03-11|2026-03-10", fechas(salida("emision-con-hora"), 0));
        assertEquals("2026-03-05|2026-03-05|2026-03-06|2026-03-09|2026-03-11|2026-03-10", fechas(salida("emision-feriado"), 0));
        assertEquals("2026-03-07|2026-03-07|2026-03-07|2026-03-09|2026-03-07|2026-03-07", fechas(salida("emision-sabado"), 0));
    }

    // ---- FM-FSA-05 / 06 reinversion-set detection and states (FSA-C03..C06, D-F3A-01)
    @Test
    @DisplayName("FM-FSA-05/06 FSA-C03 D-F3A-01: 2 RES extranjeras del mismo emisor => retencion 2, valuta cuota 1, certificacion 2 (no los dias de la serie 5/3/6)")
    void fmFsa05_dosRescatesExtranjerosMismoEmisor() throws Exception {
        JsonObject s = salida("dos-res-extranjeras-mismo-emisor");
        assertEquals(REINV_MISMO_EMISOR, fechas(s, 0));
        assertEquals(REINV_MISMO_EMISOR, fechas(s, 1));
        assertEquals(RES_EXT_SERIE, fechas(salida("res-extranjero-unica-dias-de-la-serie"), 0));   // the same RES alone uses the series days
    }

    @Test
    @DisplayName("FM-FSA-05/06 FSA-C04: emisores distintos => retencion 4 (liquidacion 2026-03-06), tambien con 3 solicitudes si una sola es de otro emisor")
    void fmFsa05_emisoresDistintosRetencionCuatro() throws Exception {
        assertEquals(REINV_EMISORES_DISTINTOS, fechas(salida("dos-res-extranjeras-emisores-distintos"), 0));
        JsonObject tres = salida("tres-res-extranjeras-un-emisor-distinto");
        for (int i = 0; i < 3; i++) assertEquals(REINV_EMISORES_DISTINTOS, fechas(tres, i));
    }

    @Test
    @DisplayName("FM-FSA-05 FSA-C05: una serie nacional en la lista => fondosExtranjeros=N: cada solicitud usa los dias de su serie")
    void fmFsa05_unaSerieNacionalBajaLaRegla() throws Exception {
        JsonObject s = salida("dos-res-una-nacional");
        assertEquals(RES_EXT_SERIE, fechas(s, 0));
        assertEquals(RES_NAC, fechas(s, 1));
        JsonObject nac = salida("dos-res-nacionales-emisores-distintos");
        assertEquals(RES_NAC, fechas(nac, 0));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-04|2026-03-09|2026-03-06", fechas(nac, 1));
    }

    @Test
    @DisplayName("FM-FSA-05: la comparacion de emisor y de FondoExtranjero es equalsIgnoreCase ('ext1' = 'EXT1'; 'n' baja la regla, 's' no)")
    void fmFsa05_ignoraMayusculas() throws Exception {
        assertEquals(REINV_MISMO_EMISOR, fechas(salida("emisor-en-mayusculas-minusculas"), 1));
        JsonObject n = salida("fondo-extranjero-n-minuscula-baja");
        assertEquals(RES_EXT_SERIE, fechas(n, 0));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-06|2026-03-11|2026-03-10", fechas(n, 1));
    }

    @Test
    @DisplayName("FM-FSA-05 D-F3A-01 D-NOR-9: lista de 2+ con una serie ausente (primera o segunda) => NullPointerException")
    void fmFsa05_serieAusenteEnListaEsNpe() throws Exception {
        assertTrue(excepcion("serie-inexistente-dos-solicitudes").startsWith("NullPointerException"));
        assertTrue(excepcion("serie-inexistente-segunda-de-dos").startsWith("NullPointerException"));
    }

    @Test
    @DisplayName("FM-FSA-06 D-F3A-01 FSA-C06: APO+RES en una lista de 2 => el APO usa los dias de aporte; el RES la regla 2/1/2 (la lista no es una reinversion y se trata como tal)")
    void fmFsa06_aporteYRescateMezclados() throws Exception {
        JsonObject s = salida("aporte-y-rescate-extranjeros-mezclados");
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-04|2026-03-03", fechas(s, 0));
        assertEquals(REINV_MISMO_EMISOR, fechas(s, 1));
        JsonObject aportes = salida("dos-aportes-extranjeros");
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-04|2026-03-03", fechas(aportes, 0));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-04|2026-03-03", fechas(aportes, 1));
    }

    @Test
    @DisplayName("FM-FSA-06: el estado del fondo sale de EstadoAporte*/EstadoRescate* segun el tipo y el perfil (FSN02: A/C aporte, R/T rescate) y elige la fila de configuracion de fechas")
    void fmFsa06_estadoSegunTipoYPerfil() throws Exception {
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-04|2026-03-10|2026-03-06", fechas(salida("estado-rescate-comercial-R"), 0));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-04|2026-03-11|2026-03-06", fechas(salida("estado-rescate-backoffice-T"), 0));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-04|2026-03-09|2026-03-03", fechas(salida("estado-aporte-comercial-A"), 0));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-04|2026-03-12|2026-03-03", fechas(salida("estado-aporte-backoffice-C"), 0));
        JsonObject lista = salida("estado-en-lista-rescate-usa-estado-de-rescate");
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-04|2026-03-09|2026-03-03", fechas(lista, 0));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-04|2026-03-10|2026-03-06", fechas(lista, 1));
        // the profile of the user (backoffice 8 vs commercial 7) selects the state column
        assertEquals("2026-03-02|2026-03-02|2026-03-03|2026-03-04|2026-03-10|2026-03-06", fechas(salida("estado-fondo-selecciona-configuracion"), 0));
        assertEquals(RES_NAC, fechas(salida("estado-fondo-selecciona-configuracion-comercial"), 0));
    }

    @Test
    @DisplayName("FM-FSA-06 FSA-C02: RES unico nacional con perfil backoffice usa los dias de rescate de la serie")
    void fmFsa06_rescateNacionalBackofficeYComercial() throws Exception {
        assertEquals(RES_NAC, fechas(salida("res-nacional-backoffice"), 0));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-04|2026-03-09|2026-03-06", fechas(salida("res-nacional-comercial"), 0));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-04|2026-03-03", fechas(salida("apo-extranjero-backoffice"), 0));
    }

    @Test
    @DisplayName("FM-FSA-06: tipo distinto de APO (nulo, 'apo') va por la rama de rescate de FSA y el colaborador lo rechaza con Exception 'Tipo Solicitud no valida.'")
    void fmFsa06_tipoNoApoEsRescateYElColaboradorLoRechaza() throws Exception {
        assertEquals("Exception: Tipo Solicitud no valida.", excepcion("tipo-no-apo-es-rescate"));
        assertEquals("Exception: Tipo Solicitud no valida.", excepcion("tipo-minuscula-apo-es-rescate"));
    }

    // ---- FM-FSA-07 spot flag (visible in the certification date)
    @Test
    @DisplayName("FM-FSA-07 FSA-C07: 1 solicitud USD/CLP => spot S: la certificacion se adelanta a 2026-03-11 (sin spot 2026-03-04); USD/USD, pago vacio, moneda vacia, sin monedas y 'usd' => N")
    void fmFsa07_unaSolicitudPorMonedas() throws Exception {
        String conSpot = "2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-11|2026-03-12";
        String sinSpot = "2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-04|2026-03-12";
        assertEquals(conSpot, fechas(salida("spot-una-solicitud-usd-clp"), 0));
        assertEquals(sinSpot, fechas(salida("spot-una-solicitud-usd-usd"), 0));
        assertEquals(sinSpot, fechas(salida("spot-una-solicitud-pago-vacio"), 0));
        assertEquals(sinSpot, fechas(salida("spot-una-solicitud-moneda-vacia"), 0));
        assertEquals(sinSpot, fechas(salida("spot-una-solicitud-sin-monedas"), 0));
        assertEquals(sinSpot, fechas(salida("spot-mayusculas-minusculas"), 0));
        assertEquals(conSpot, fechas(salida("spot-una-solicitud-pago-nulo"), 0));   // IdMonedaPago null: equalsIgnoreCase(null) is false and "" is not null
    }

    @Test
    @DisplayName("FM-FSA-07: el nombre dice rescate pero el calculo vale tambien para un APO (la bandera sale en S; la fecha de aporte no cambia)")
    void fmFsa07_aporteTambien() throws Exception {
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-04|2026-03-03", fechas(salida("spot-aporte-tambien"), 0));
    }

    @Test
    @DisplayName("FM-FSA-07 FSA-C07: lista de 2 con IdMoneda distinta => spot S en las dos (certificacion 2026-03-12); misma IdMoneda con pago distinto => N (2026-03-11)")
    void fmFsa07_listaPorIdMoneda() throws Exception {
        JsonObject s = salida("spot-lista-monedas-distintas");
        assertEquals("2026-03-09|2026-03-09|2026-03-09|2026-03-10|2026-03-12|2026-03-13", fechas(s, 0));
        assertEquals("2026-03-09|2026-03-09|2026-03-09|2026-03-10|2026-03-12|2026-03-13", fechas(s, 1));
        JsonObject n = salida("spot-lista-misma-moneda-pago-distinto");
        assertEquals("2026-03-09|2026-03-09|2026-03-09|2026-03-10|2026-03-11|2026-03-13", fechas(n, 0));
        assertEquals("2026-03-09|2026-03-09|2026-03-09|2026-03-10|2026-03-11|2026-03-13", fechas(n, 1));
        JsonObject nula = salida("spot-lista-segunda-moneda-nula");   // equalsIgnoreCase(null) is false => "different" => S, no exception
        assertEquals("2026-03-09|2026-03-09|2026-03-09|2026-03-10|2026-03-12|2026-03-13", fechas(nula, 1));
    }

    @Test
    @DisplayName("FM-FSA-07 D-F3A-01: IdMoneda nula => NullPointerException (1 solicitud) y primera IdMoneda nula en una lista de 2 => NullPointerException")
    void fmFsa07_monedaNulaEsNpe() throws Exception {
        assertTrue(excepcion("spot-una-solicitud-moneda-nula-npe").startsWith("NullPointerException"));
        assertTrue(excepcion("spot-lista-primera-moneda-nula-npe").startsWith("NullPointerException"));
    }

    // ---- FM-FSA-08 delegation, order, ids
    @Test
    @DisplayName("FM-FSA-08: el orden de salida es el del colaborador (el de entrada, sin Switch): ids 30, 10, 20")
    void fmFsa08_ordenDeSalida() throws Exception {
        JsonObject s = salida("orden-de-salida-ids-desordenados");
        assertEquals(List.of(30L, 10L, 20L), ids(s));
        assertEquals(List.of("FSN02", "FSN01", "FSX01"), series(s));
    }

    @Test
    @DisplayName("FM-FSA-08 parallelStream: 120 solicitudes salen en el orden de entrada (ids 1000, 993, 986, ...)")
    void fmFsa08_cientoVeinteSolicitudesEnOrden() throws Exception {
        JsonObject s = salida("ciento-veinte-solicitudes-determinismo");
        List<Long> esperado = new ArrayList<>();
        for (int i = 0; i < 120; i++) esperado.add(1000L - 7L * i);
        assertEquals(esperado, ids(s));
        assertEquals(RES_NAC, fechas(s, 0));   // id 1000, FSN01, RES, 2026-03-02: the list is not all foreign => series days
    }

    @Test
    @DisplayName("FM-FSA-08 / D-NOR-9: IdInterno nulo, cero, negativo o repetido se copian tal cual a la salida (sin excepcion)")
    void fmFsa08_idInternoSeCopia() throws Exception {
        assertTrue(resultado(salida("idinterno-nulo")).get(0).getAsJsonObject().get("IdInterno").isJsonNull());
        assertEquals(List.of(0L), ids(salida("idinterno-cero")));
        assertEquals(List.of(-3L), ids(salida("idinterno-negativo")));
        assertEquals(List.of(5L, 5L), ids(salida("idinterno-repetido-dos-solicitudes")));
    }

    @Test
    @DisplayName("FM-FSA-08: sin filas activas de configuracion de fechas el colaborador falla con NullPointerException; el parametro de dia no habil money market mueve la fecha valor cuota a 2026-03-01")
    void fmFsa08_configuracionDeFechas() throws Exception {
        assertTrue(excepcion("res-sin-configuracion-de-fechas").startsWith("NullPointerException"));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-01|2026-03-02|2026-03-02", fechas(salida("parametro-dia-no-habil-money-market"), 0));
    }

    @Test
    @DisplayName("FM-FSA-01 / D-F3A-02: campos desconocidos de la solicitud y NPE de datos NOT NULL sinteticos: se ignoran / se lanzan como el legacy")
    void fmFsa01_camposDesconocidosYDatosNulosSinteticos() throws Exception {
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-02|2026-03-02", fechas(salida("p-solicitud-con-campo-desconocido"), 0));
        assertTrue(excepcion("db-nulos-emisor-npe").startsWith("NullPointerException"));
        assertTrue(excepcion("db-nulos-dias-retencion-rescate-npe").startsWith("NullPointerException"));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-02|2026-03-02", fechas(salida("db-nulos-dias-aporte-sin-npe"), 0));
    }

    @Test
    @DisplayName("FM-FSA-06 D-FEC-*: series con pais del fondo distinto del pais del emisor: FSX05 y FSX07 => NullPointerException del colaborador; FSX06 calcula")
    void fmFsa06_paisFondoDistintoDelEmisor() throws Exception {
        for (String slug : new String[] { "res-fsx05-comercial", "res-fsx05-backoffice", "res-fsx07-comercial", "res-fsx07-backoffice" }) {
            assertTrue(excepcion(slug).startsWith("NullPointerException"), slug);
        }
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-06|2026-03-11|2026-03-10", fechas(salida("res-fsx06-comercial"), 0));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-06|2026-03-11|2026-03-10", fechas(salida("res-fsx06-backoffice"), 0));
        JsonObject par = salida("res-dos-extranjeras-pais-distinto-comercial");
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-02|2026-03-04|2026-03-04", fechas(par, 0));
        assertEquals("2026-03-02|2026-03-02|2026-03-02|2026-03-03|2026-03-04|2026-03-04", fechas(par, 1));
    }

    // ---- RULE-076
    @Test
    @DisplayName("RULE-076: ni el main() del legacy ni sus ids reales ni su conexion se copiaron (src/main y los fixtures de fondomutuo-fsa)")
    void rule076_sinElMainNiLosIdsDelLegacy() throws Exception {
        // legacy ConsultarFechaSolicitudAporteRescate.main (FSA:356-373): PlataformaUtils.getBOLSAConnection() and a JSON with the real series ids below
        List<String> culpables = new ArrayList<>();
        for (java.nio.file.Path raiz : List.of(java.nio.file.Path.of("src", "main"), java.nio.file.Path.of("src", "test", "resources", "fondomutuo-fsa"))) {
            org.junit.jupiter.api.Assertions.assertTrue(java.nio.file.Files.isDirectory(raiz), "RULE-076 needs " + raiz + " (run the tests from the module directory)");
            try (var s = java.nio.file.Files.walk(raiz)) {
                for (java.nio.file.Path p : s.filter(java.nio.file.Files::isRegularFile).toList()) {
                    String t = java.nio.file.Files.readString(p, java.nio.charset.StandardCharsets.ISO_8859_1);
                    if (t.contains("00062PUSD") || t.contains("00047PUSD") || t.contains("getBOLSAConnection") || t.contains("getDESAConnection")) culpables.add(p.toString());
                }
            }
        }
        assertEquals(List.of(), culpables);
    }

    private static List<Long> ids(JsonObject salida) {
        List<Long> l = new ArrayList<>();
        for (JsonElement r : resultado(salida)) l.add(r.getAsJsonObject().get("IdInterno").getAsLong());
        return l;
    }

    private static List<String> series(JsonObject salida) {
        List<String> l = new ArrayList<>();
        for (JsonElement r : resultado(salida)) l.add(r.getAsJsonObject().get("IdSerieFondo").getAsString());
        return l;
    }
}
