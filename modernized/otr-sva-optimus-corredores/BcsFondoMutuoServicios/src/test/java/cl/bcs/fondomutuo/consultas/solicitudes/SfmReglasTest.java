package cl.bcs.fondomutuo.consultas.solicitudes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.solicitudes.CasoSfm.Perfil;

/**
 * Rule cards of {@code ConsultarSolicitudesFFMM} (SFM) and {@code ConsultarSolicitudesFFMMWS} (WS) (RULES_FONDOMUTUO_F3_A.md 8.3, FM-SFM-01..08; defects D-F3A-08, 09, 10, 25,
 * 28 and D-NOR-9 / D-NOR-10 of section 9; matrix SFM-C01..C08, WS-C01..C03), stated with LITERAL values. The expected values are the ones the unmodified legacy produced (they
 * equal the recorded goldens: this class and {@link SfmEquivalenciaTest} must agree; if the legacy is re-recorded and a literal changes, the legacy won: update the literal and
 * say so). Each test runs the NEW code on the H2 of its case (slug of {@code cases/FMM-SFM-nnn_slug}; the WS twin of scenario {@code x} is {@code ws-x}) and names the card or
 * defect it pins. Universe: see {@code cases_sfm_lib.py} (stand-in table of the view, default row: client 100, APO, INGRESADO, 2026-03-02 13:00:00 UTC, offset -10800).
 */
class SfmReglasTest {

    // slug of an SFM scenario -> NumSolicitud of the rows of P_OUT_SOLICITUDES, in output order (the WS twin must give the same)
        private static final Map<String, String> FILTROS = Map.ofEntries(
            Map.entry("filtro-cliente", "2"),
            Map.entry("filtro-cliente-cero", "1,2,3,4"),
            Map.entry("filtro-cliente-negativo", ""),
            Map.entry("filtro-tipo-solicitud", "2,3"),
            Map.entry("filtro-tipo-solicitud-minuscula", ""),
            Map.entry("filtro-origen", "2,3"),
            Map.entry("filtro-folio-exacto", "1"),
            Map.entry("filtro-tipo-ahorro", "2,4"),
            Map.entry("filtro-tipo-cuenta", "2,3"),
            Map.entry("filtro-serie-fondo", "2,4"),
            Map.entry("filtro-fecha-desde-inclusive", "2,3,4"),
            Map.entry("filtro-fecha-hasta-inclusive", "1,2,3"),
            Map.entry("filtro-rango-de-fechas", "2,3"),
            Map.entry("filtro-rango-invertido", ""),
            Map.entry("filtro-fecha-sin-ceros", ""),
            Map.entry("filtro-fecha-con-hora", "1,2,3"),
            Map.entry("filtro-fecha-basura", ""),
            Map.entry("filtros-todos-a-la-vez", "3"),
            Map.entry("filtros-contradictorios", ""),
            Map.entry("filtro-espacio-no-se-recorta", "1"),
            Map.entry("filtro-vacio-no-filtra", "1,2,3"),
            Map.entry("filtro-folio-con-nulos", "1"),
            Map.entry("tipo-movimiento-siempre-filtra", "1,5,6,8"),
            Map.entry("adc-s", "1"),
            Map.entry("adc-n", "1,2,3,4,5"),
            Map.entry("adc-minuscula", "1,2,3,4,5"),
            Map.entry("adc-si", "1,2,3,4,5"),
            Map.entry("adc-vacio", "1,2,3,4,5"),
            Map.entry("adc-espacio", "1,2,3,4,5"),
            Map.entry("no-automaticas-s", "1"),
            Map.entry("no-automaticas-n", "1,2,3,4,5"),
            Map.entry("no-automaticas-minuscula", "1,2,3,4,5"),
            Map.entry("no-automaticas-x", "1,2,3,4,5"),
            Map.entry("adc-y-no-automaticas-juntos", "1"),
            Map.entry("p-estado-terminado", "2"),
            Map.entry("p-estado-terminado-minuscula", ""),
            Map.entry("p-estado-anulado", "3"),
            Map.entry("p-estado-ingresado", "1"),
            Map.entry("p-estado-en-proceso", "4"),
            Map.entry("p-estado-desconocido", ""),
            Map.entry("p-estado-vacio-quita-los-sin-estado", "1,2,3,4"),
            Map.entry("p-estado-un-espacio", ""),
            Map.entry("p-estado-con-espacio-final", ""),
            Map.entry("p-estado-y-cliente", "1,3")
    );

    // slug -> Estado of the rows in output order, and the NumSolicitud of those rows
        private static final Map<String, String> ESTADOS = Map.ofEntries(
            Map.entry("estado-tres-ingresadas", "INGRESADO,INGRESADO,INGRESADO"),
            Map.entry("estado-en-proceso-y-terminado", "EN PROCESO,EN PROCESO"),
            Map.entry("estado-terminado-y-anulado-se-descarta", ""),
            Map.entry("estado-ingresado-y-terminado-se-descarta", ""),
            Map.entry("estado-anulada-sola", "ANULADO"),
            Map.entry("estado-dos-anuladas", "ANULADO,ANULADO"),
            Map.entry("estado-terminada-sola", "TERMINADO"),
            Map.entry("estado-dos-terminadas", "TERMINADO,TERMINADO"),
            Map.entry("estado-ingresada-y-en-proceso", "EN PROCESO,EN PROCESO"),
            Map.entry("estado-en-proceso-solo", "EN PROCESO"),
            Map.entry("estado-en-proceso-y-anulada", "EN PROCESO,EN PROCESO"),
            Map.entry("estado-anulada-e-ingresada-se-descarta", ""),
            Map.entry("estado-desconocido", ""),
            Map.entry("estado-detalle-nulo", ""),
            Map.entry("estado-minusculas", ""),
            Map.entry("estado-ingresada-y-desconocida", ""),
            Map.entry("estado-en-proceso-y-desconocida", "EN PROCESO,EN PROCESO"),
            Map.entry("estado-con-espacio", ""),
            Map.entry("asocia-por-num-solicitud-sin-folio", "EN PROCESO,EN PROCESO,EN PROCESO"),
            Map.entry("folio-cero-no-asocia", "TERMINADO,ANULADO"),
            Map.entry("asocia-por-folio-contrato-distinta-solicitud", "EN PROCESO,EN PROCESO"),
            Map.entry("dos-grupos-independientes", "TERMINADO"),
            Map.entry("encadenamiento-no-transitivo", "EN PROCESO,EN PROCESO"),
            Map.entry("fila-unica-folio-contrato-nulo-no-rev", "INGRESADO"),
            Map.entry("rev-folio-relacionado-ingresadas", "INGRESADO,INGRESADO"),
            Map.entry("rev-terminada-y-en-proceso", "EN PROCESO,EN PROCESO"),
            Map.entry("rev-mixta-se-descarta", ""),
            Map.entry("rev-asocia-por-num-solicitud", "EN PROCESO,EN PROCESO"),
            Map.entry("rev-folio-relacionado-cero-no-asocia", "TERMINADO,ANULADO"),
            Map.entry("rev-y-aporte-no-se-mezclan", "TERMINADO,ANULADO"),
            Map.entry("rev-minuscula-es-normal", "INGRESADO"),
            Map.entry("rev-montos-origen-nulos-no-importan", "INGRESADO")
    );

    private static final Map<String, String> ESTADOS_NUM = Map.ofEntries(
            Map.entry("estado-tres-ingresadas", "1,2,3"),
            Map.entry("estado-en-proceso-y-terminado", "1,2"),
            Map.entry("estado-terminado-y-anulado-se-descarta", ""),
            Map.entry("estado-ingresado-y-terminado-se-descarta", ""),
            Map.entry("estado-anulada-sola", "1"),
            Map.entry("estado-dos-anuladas", "1,2"),
            Map.entry("estado-terminada-sola", "1"),
            Map.entry("estado-dos-terminadas", "1,2"),
            Map.entry("estado-ingresada-y-en-proceso", "1,2"),
            Map.entry("estado-en-proceso-solo", "1"),
            Map.entry("estado-en-proceso-y-anulada", "1,2"),
            Map.entry("estado-anulada-e-ingresada-se-descarta", ""),
            Map.entry("estado-desconocido", ""),
            Map.entry("estado-detalle-nulo", ""),
            Map.entry("estado-minusculas", ""),
            Map.entry("estado-ingresada-y-desconocida", ""),
            Map.entry("estado-en-proceso-y-desconocida", "1,2"),
            Map.entry("estado-con-espacio", ""),
            Map.entry("asocia-por-num-solicitud-sin-folio", "7,7,7"),
            Map.entry("folio-cero-no-asocia", "1,2"),
            Map.entry("asocia-por-folio-contrato-distinta-solicitud", "1,2"),
            Map.entry("dos-grupos-independientes", "3"),
            Map.entry("encadenamiento-no-transitivo", "2,2"),
            Map.entry("fila-unica-folio-contrato-nulo-no-rev", "1"),
            Map.entry("rev-folio-relacionado-ingresadas", "1,2"),
            Map.entry("rev-terminada-y-en-proceso", "1,2"),
            Map.entry("rev-mixta-se-descarta", ""),
            Map.entry("rev-asocia-por-num-solicitud", "7,7"),
            Map.entry("rev-folio-relacionado-cero-no-asocia", "1,2"),
            Map.entry("rev-y-aporte-no-se-mezclan", "1,2"),
            Map.entry("rev-minuscula-es-normal", "1"),
            Map.entry("rev-montos-origen-nulos-no-importan", "1")
    );

    private static JsonObject dump(String slug) throws Exception {
        return CasoSfm.ejecutar(CasoSfm.idPorSlug(slug), Perfil.ABIERTA);
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

    private static JsonArray filas(JsonObject salida) {
        return salida.getAsJsonArray("P_OUT_SOLICITUDES");
    }

    private static String campo(JsonObject salida, int i, String k) {
        JsonElement e = filas(salida).get(i).getAsJsonObject().get(k);
        return e == null ? "<absent>" : e.getAsString();
    }

    private static String nums(JsonObject salida) {
        List<String> l = new ArrayList<>();
        for (JsonElement r : filas(salida)) l.add(r.getAsJsonObject().get("NumSolicitud").getAsString());
        return String.join(",", l);
    }

    private static String estados(JsonObject salida) {
        List<String> l = new ArrayList<>();
        for (JsonElement r : filas(salida)) l.add(r.getAsJsonObject().get("Estado").getAsString());
        return String.join(",", l);
    }

    private static String montos(JsonObject salida, int i) {
        return String.join("|", campo(salida, i, "MontoCuotasComprometidas"), campo(salida, i, "MontoPesosComprometidos"), campo(salida, i, "MontoCuotasDisponibles"),
                campo(salida, i, "MontoPesosDisponibles"));
    }

    // ---- FM-SFM-01 contract
    @Test
    @DisplayName("FM-SFM-01 SFM-C01 D-F3A-25: SFM sin filtros: {P_OUT_ERRORESCONTADOR 0, P_OUT_MENSAJES [], P_OUT_SOLICITUDES}; 73 campos por fila con Estado despues de DetSolicitud; new Gson() plano: IdSerieFondoOrigen nulo no se escribe")
    void fmSfm01_contratoSfm() throws Exception {
        JsonObject d = dump("defecto-tres-solicitudes");
        JsonObject s = d.getAsJsonObject("salida");
        assertEquals(List.of("P_OUT_ERRORESCONTADOR", "P_OUT_MENSAJES", "P_OUT_SOLICITUDES"), new ArrayList<>(s.keySet()));
        assertEquals(0L, s.get("P_OUT_ERRORESCONTADOR").getAsLong());
        assertEquals(0, s.getAsJsonArray("P_OUT_MENSAJES").size());
        assertEquals("COMMIT", d.get("decisionServlet").getAsString());
        List<String> claves = new ArrayList<>(filas(s).get(0).getAsJsonObject().keySet());
        assertEquals(73, claves.size());
        assertEquals("IdCliente", claves.get(0));
        assertEquals("DetSolicitud", claves.get(5));
        assertEquals("Estado", claves.get(6));
        assertEquals("EstadoDetalle", claves.get(7));
        assertEquals("FechaIngreso", claves.get(40));
        assertEquals("FechaFinalReinversion", claves.get(72));
        assertFalse(claves.contains("IdSerieFondoOrigen"));
        assertFalse(claves.contains("IdComprobante"));
        assertFalse(claves.contains("FechaIngresoLocal"));
        assertEquals("1,2,3", nums(s));
        assertEquals("INGRESADO,TERMINADO,ANULADO", estados(s));
        assertFalse(d.get("salidaCruda").getAsString().contains("null"), "plain Gson: null fields are not written");
    }

    @Test
    @DisplayName("FM-SFM-01 / FM-SFM-07(b) WS-C01: el WS devuelve los mismos campos mas IdComprobante y FechaIngresoLocal al final (75 campos); IdComprobante 0 sin Tesoreria")
    void fmSfm01_contratoWs() throws Exception {
        JsonObject s = salida("ws-defecto-tres-solicitudes");
        List<String> claves = new ArrayList<>(filas(s).get(0).getAsJsonObject().keySet());
        assertEquals(75, claves.size());
        assertEquals(List.of("IdContrato", "FechaFinalReinversion", "IdComprobante", "FechaIngresoLocal"), claves.subList(71, 75));
        assertEquals("0", campo(s, 0, "IdComprobante"));
        assertEquals("1,2,3", nums(s));
        assertEquals("INGRESADO,TERMINADO,ANULADO", estados(s));
    }

    @Test
    @DisplayName("FM-SFM-01: una vista vacia da P_OUT_SOLICITUDES vacia con contador 0 y commit (SFM y WS)")
    void fmSfm01_vistaVacia() throws Exception {
        for (String slug : new String[] { "vista-vacia", "ws-vista-vacia" }) {
            JsonObject d = dump(slug);
            assertEquals("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"P_OUT_SOLICITUDES\":[]}", d.get("salidaCruda").getAsString(), slug);
            assertEquals("COMMIT", d.get("decisionServlet").getAsString(), slug);
        }
    }

    @Test
    @DisplayName("FM-SFM-01: los BigDecimal salen con el texto de su escala ('0E-8', '1E-8', '1693.85619000'); fechas de la vista como texto tal cual")
    void fmSfm01_textoDeLosNumeros() throws Exception {
        for (String slug : new String[] { "tipos-numericos-y-fechas-en-texto", "ws-tipos-numericos-y-fechas-en-texto" }) {
            JsonObject s = salida(slug);
            assertEquals("123456789.12345678", campo(s, 0, "MontoPesosOrigen"), slug);
            assertEquals("1E-8", campo(s, 0, "MontoCuotasOrigen"), slug);
            assertEquals("1693.85619000", campo(s, 0, "ValorCuotaOrigen"), slug);
            assertEquals("0.50000000", campo(s, 0, "MontoCuotasComision"), slug);
            assertEquals("33.33333333", campo(s, 0, "PorcentajeOrigen"), slug);
            assertEquals("0E-8", campo(s, 0, "PorcentajeDestino"), slug);
            assertEquals("0E-8", campo(s, 0, "MontoPesosDestino"), slug);
            assertEquals("", campo(s, 0, "FechaLiquidacion"), slug);
            assertEquals("2026-03-04", campo(s, 0, "FechaCertificacion"), slug);
        }
    }

    @Test
    @DisplayName("FM-SFM-07(e) D-F3A-10: FechaIngresoLocal viaja con el formato por defecto de Gson 'MMM d, yyyy, h:mm:ss a' con espacio fino U+202F (JDK 20+: la salida depende de la JVM)")
    void fmSfm07e_fechaIngresoLocalConFormatoGson() throws Exception {
        JsonObject s = salida("ws-fecha-ingreso-local-viaja-con-formato-gson");
        assertEquals("Mar 10, 2026, 3:05:07 PM", campo(s, 0, "FechaIngresoLocal"));
        assertEquals("2026-03-10 15:05:07", campo(s, 0, "FechaIngreso"));
        assertEquals("15:05:07", campo(s, 0, "HoraSolicitud"));
        assertEquals("Mar 2, 2026, 10:00:00 AM", campo(salida("ws-defecto-tres-solicitudes"), 0, "FechaIngresoLocal"));
    }

    @Test
    @DisplayName("FM-SFM-01 D-F3A-28: \"IdSerieFondo\" as \"IdSerieFondoOrigen\" con el mapeo de queryParallel sobre H2: IdSerieFondo recibe la serie de la vista (FM002B) e IdSerieFondoOrigen queda nulo (no se escribe)")
    void fmSfm01_aliasDeLaSerieOrigen() throws Exception {
        for (String slug : new String[] { "serie-origen-alias-queryparallel", "ws-serie-origen-alias-queryparallel" }) {
            JsonObject s = salida(slug);
            assertEquals("FM002B", campo(s, 0, "IdSerieFondo"), slug);
            assertEquals("<absent>", campo(s, 0, "IdSerieFondoOrigen"), slug);
            assertEquals("FM001A", campo(s, 1, "IdSerieFondo"), slug);
            assertEquals("FM003C", campo(s, 1, "IdSerieFondoDestino"), slug);
            assertEquals("FM001A", campo(s, 0, "SerieFondoOrigen"), slug);
        }
    }

    @Test
    @DisplayName("FM-SFM-06 D-F3A-28: sin ORDER BY la salida sigue el orden de la consulta: filas insertadas 5,1,4,2,3 salen 5,1,4,2,3")
    void fmSfm06_ordenDeLaConsulta() throws Exception {
        assertEquals("5,1,4,2,3", nums(salida("orden-del-sql-sin-order-by")));
        assertEquals("5,1,4,2,3", nums(salida("ws-orden-del-sql-sin-order-by")));
    }

    @Test
    @DisplayName("FM-SFM-01: es de solo lectura: ni tablas locales de sesion, ni commits autonomos, ni secuencias, ni colaboradores; las tablas vuelven intactas")
    void fmSfm01_soloLectura() throws Exception {
        for (String slug : new String[] { "defecto-tres-solicitudes", "ws-defecto-tres-solicitudes", "ws-tes-dos-detalles-duplican-la-fila" }) {
            JsonObject d = dump(slug);
            assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop").size(), slug);
            assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("commitsAutonomos").size(), slug);
            assertEquals(0, d.getAsJsonArray("secuenciasSolicitadas").size(), slug);
            assertEquals(0, d.getAsJsonArray("colaboradores").size(), slug);
            assertEquals(3 - (slug.contains("tes-") ? 2 : 0), d.getAsJsonObject("tablas").getAsJsonArray("BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesFFMM").size(), slug);
        }
    }

    @Test
    @DisplayName("FM-SFM-01 SFM-C08 D-F3A-25: un filtro con null explicito => NullPointerException (isEmpty / equals sobre null), tambien p_Estado (despues de leer y calcular)")
    void fmSfm01_camposNulosSonNpe() throws Exception {
        for (String campo : new String[] { "idcliente", "tiposolicitud", "origen", "folio", "tipoahorro", "tipocuenta", "idseriefondo", "fechadesde", "fechahasta", "estado" }) {
            assertTrue(excepcion("campo-nulo-" + campo).startsWith("NullPointerException"), "SFM " + campo);
            assertTrue(excepcion("ws-campo-nulo-" + campo).startsWith("NullPointerException"), "WS " + campo);
        }
    }

    @Test
    @DisplayName("FM-SFM-01: p_IdCliente 'abc' => JsonSyntaxException; '100' como texto lo coacciona Gson a Long y un campo desconocido se ignora")
    void fmSfm01_entradaJson() throws Exception {
        assertTrue(excepcion("cliente-no-numerico").startsWith("JsonSyntaxException"));
        assertTrue(excepcion("ws-cliente-no-numerico").startsWith("JsonSyntaxException"));
        assertEquals("1,3", nums(salida("campos-extra-y-cliente-texto")));
        assertEquals("1,3", nums(salida("ws-campos-extra-y-cliente-texto")));
    }

    // ---- FM-SFM-02 filters (every filter; SFM and the WS twin)
    @Test
    @DisplayName("FM-SFM-02 SFM-C06 D-F3A-08: cada filtro del SQL (cliente, tipo, origen, folio exacto, ahorro, cuenta, serie, fechas de texto inclusive, espacio sin trim, "
            + "vacio sin filtro, FiltraTipoMovimiento siempre, ADC solo con 'S' exacto, no automaticas solo con 'S' exacto, p_Estado en memoria) devuelve estas NumSolicitud, igual en el WS")
    void fmSfm02_filtros() throws Exception {
        for (Map.Entry<String, String> e : FILTROS.entrySet()) {
            assertEquals(e.getValue(), nums(salida(e.getKey())), "SFM " + e.getKey());
            assertEquals(e.getValue(), nums(salida("ws-" + e.getKey())), "WS " + e.getKey());
        }
        assertTrue(FILTROS.size() >= 40, "the filter table covers at least 40 scenarios, found " + FILTROS.size());
    }

    @Test
    @DisplayName("FM-SFM-02: p_IdCliente=0 no filtra; p_IdCliente=-1 si (ninguna fila); el cliente 200 filtra a la solicitud 2")
    void fmSfm02_clienteCeroNoFiltra() throws Exception {
        assertEquals("1,2,3,4", nums(salida("filtro-cliente-cero")));
        assertEquals("", nums(salida("filtro-cliente-negativo")));
        assertEquals("2", nums(salida("filtro-cliente")));
    }

    @Test
    @DisplayName("FM-SFM-02: las fechas se comparan como TEXTO con >= y <=: '2026-3-5' deja fuera todo (3 > 0); '2026-03-10 23:59:59' incluye el 2026-03-10 (prefijo menor)")
    void fmSfm02_fechasComoTexto() throws Exception {
        assertEquals("", nums(salida("filtro-fecha-sin-ceros")));
        assertEquals("1,2,3", nums(salida("filtro-fecha-con-hora")));
        assertEquals("", nums(salida("filtro-fecha-basura")));
    }

    @Test
    @DisplayName("FM-SFM-02: FiltraTipoMovimiento=S siempre: se excluyen INGGTA, TipoMovimiento nulo, desconocido (ZZZ) y 'apoNorm' por mayusculas; salen 1, 5, 6 y 8 (APOREINV)")
    void fmSfm02_tipoMovimientoSiempreFiltra() throws Exception {
        assertEquals("1,5,6,8", nums(salida("tipo-movimiento-siempre-filtra")));
        assertEquals("1,5,6,8", nums(salida("ws-tipo-movimiento-siempre-filtra")));
    }

    @Test
    @DisplayName("FM-SFM-02: PLA.FiltroAdministracionCarteraWS='S' deja solo EsADC='N' (se pierden 'S', nulo, vacio y 'n'); WS_SOLICITUDES_..._NO_AUTOMATICAS='S' solo IngresoAutomatico='N'; con ambos en S, solo la fila N/N")
    void fmSfm02_adcYNoAutomaticas() throws Exception {
        assertEquals("1", nums(salida("adc-s")));
        assertEquals("1", nums(salida("no-automaticas-s")));
        assertEquals("1", nums(salida("adc-y-no-automaticas-juntos")));
        assertEquals("1,2,3,4,5", nums(salida("adc-minuscula")));
        assertEquals("1,2,3,4,5", nums(salida("no-automaticas-minuscula")));
    }

    @Test
    @DisplayName("FM-SFM-02: faltan los parametros PLATAFORMA/PLA.FiltroAdministracionCarteraWS y FONDOMUTUO/WS_SOLICITUDES_..._NO_AUTOMATICAS => Exception con el texto del legacy (HTTP 500)")
    void fmSfm02_parametrosObligatorios() throws Exception {
        for (String prefijo : new String[] { "", "ws-" }) {
            assertEquals("Exception: Parametro PLA.FiltroAdministracionCarteraWS en modulo PLATAFORMA no encontrado", excepcion(prefijo + "sin-parametro-adc"));
            assertEquals("Exception: Parametro WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS en modulo FONDOMUTUO no encontrado", excepcion(prefijo + "sin-parametro-no-automaticas"));
            assertEquals("Exception: Parametro PLA.SegundosZonaHoraria en modulo PLATAFORMA no encontrado", excepcion(prefijo + "sin-segundos-zona-horaria"));
        }
    }

    @Test
    @DisplayName("FM-SFM-02 D-NOR-9 DESVIO-KIT-1: empresa nula, sin configuracion o ausente => el legacy lanza NullPointerException (ConfiguracionSistema); el kit del modulo ParametroNoEncontradoException (aceptado solo aqui)")
    void fmSfm02_empresaSinConfiguracion() throws Exception {
        for (String slug : new String[] { "empresa-nula", "empresa-sin-configuracion", "empresa-ausente" }) {
            for (String prefijo : new String[] { "", "ws-" }) {
                String e = excepcion(prefijo + slug);
                assertTrue(e.startsWith("NullPointerException") || (e.startsWith("Exception: Parametro ") && e.endsWith(" no encontrado")), prefijo + slug + ": " + e);
            }
        }
    }

    // ---- FM-SFM-03 hour and entry date (SFM: seconds offset, date NOT adjusted; WS: from FechaIngresoLocal)
    @Test
    @DisplayName("FM-SFM-03 SFM-C07 D-F3A-09: offset +3600 y hora 23:30:00 => HoraSolicitud 00:30:00 y FechaIngreso '2026-03-10 00:30:00' (la fecha NO avanza); el WS usa FechaIngresoLocal: 2026-03-11 00:30")
    void fmSfm03_vueltaDeMedianocheHaciaAdelante() throws Exception {
        JsonObject s = salida("hora-23-30-mas-3600");
        assertEquals("00:30:00", campo(s, 0, "HoraSolicitud"));
        assertEquals("2026-03-10", campo(s, 0, "FechaSolicitud"));
        assertEquals("2026-03-10 00:30:00", campo(s, 0, "FechaIngreso"));
        JsonObject w = salida("ws-hora-23-30-mas-3600");
        assertEquals("00:30", campo(w, 0, "HoraSolicitud"));
        assertEquals("2026-03-11", campo(w, 0, "FechaSolicitud"));
        assertEquals("2026-03-11 00:30:00", campo(w, 0, "FechaIngreso"));
    }

    @Test
    @DisplayName("FM-SFM-03 SFM-C07 D-F3A-09: offset -10800 y hora 02:15:00 => 23:15:00 con la MISMA fecha 2026-03-10 (en realidad el dia anterior); el WS muestra 2026-03-09 23:15")
    void fmSfm03_vueltaDeMedianocheHaciaAtras() throws Exception {
        JsonObject s = salida("hora-02-15-menos-10800");
        assertEquals("23:15:00", campo(s, 0, "HoraSolicitud"));
        assertEquals("2026-03-10 23:15:00", campo(s, 0, "FechaIngreso"));
        JsonObject w = salida("ws-hora-02-15-menos-10800");
        assertEquals("23:15", campo(w, 0, "HoraSolicitud"));
        assertEquals("2026-03-09", campo(w, 0, "FechaSolicitud"));
        assertEquals("2026-03-09 23:15:00", campo(w, 0, "FechaIngreso"));
    }

    @Test
    @DisplayName("FM-SFM-03: offsets de borde: 0 sin cambio, 86400 (un dia) sin cambio, 129600 (+12 h), -1 con 00:00:00 => 23:59:59, hora 00:00:00 con -10800 => 21:00:00, 23:59:59 con +1 => 00:00:00")
    void fmSfm03_offsetsDeBorde() throws Exception {
        assertEquals("09:05:07", campo(salida("offset-cero"), 0, "HoraSolicitud"));
        assertEquals("09:05:07", campo(salida("offset-un-dia"), 0, "HoraSolicitud"));
        assertEquals("21:05:07", campo(salida("offset-un-dia-y-medio"), 0, "HoraSolicitud"));
        assertEquals("23:59:59", campo(salida("offset-menos-un-segundo"), 0, "HoraSolicitud"));
        assertEquals("21:00:00", campo(salida("hora-medianoche-exacta"), 0, "HoraSolicitud"));
        assertEquals("00:00:00", campo(salida("hora-limite-23-59-59-mas-1"), 0, "HoraSolicitud"));
        assertEquals("21:00:00", campo(salida("hora-24-00-00"), 0, "HoraSolicitud"));   // '24:00:00' parses as 00:00:00 (HH is lenient at the day end)
        assertEquals("14:00:00", campo(salida("segundos-con-signo-mas"), 0, "HoraSolicitud"));   // Long.parseLong accepts '+3600'
    }

    @Test
    @DisplayName("FM-SFM-03 D-F3A-09: PLA.SegundosZonaHoraria 3000000000 se trunca con (int) a -1294967296: 13:00:00 pasa a 11:51:44; el WS lo lee y no lo usa (10:00)")
    void fmSfm03_offsetFueraDeInt() throws Exception {
        assertEquals("11:51:44", campo(salida("segundos-fuera-de-int"), 0, "HoraSolicitud"));
        assertEquals("10:00", campo(salida("ws-segundos-fuera-de-int"), 0, "HoraSolicitud"));
    }

    @Test
    @DisplayName("FM-SFM-03 D-NOR-10: PLA.SegundosZonaHoraria no numerico ('abc', '3600.5', ' 3600') => NumberFormatException, tambien en el WS (lo lee aunque no lo use)")
    void fmSfm03_offsetNoNumerico() throws Exception {
        for (String slug : new String[] { "segundos-no-numericos", "segundos-con-decimales", "segundos-con-espacio" }) {
            assertTrue(excepcion(slug).startsWith("NumberFormatException"), slug);
            assertTrue(excepcion("ws-" + slug).startsWith("NumberFormatException"), "ws-" + slug);
        }
        assertTrue(excepcion("ws-offset-invalido-se-lee-igual").startsWith("NumberFormatException"));
    }

    @Test
    @DisplayName("FM-SFM-03 SFM-C08: HoraSolicitud con milisegundos, un digito, sin segundos o vacia => DateTimeParseException; nula o FechaSolicitud nula => NullPointerException (solo SFM)")
    void fmSfm03_horasInvalidasSoloEnSfm() throws Exception {
        for (String slug : new String[] { "hora-con-milisegundos", "hora-un-digito", "hora-sin-segundos", "hora-vacia" }) {
            assertTrue(excepcion(slug).startsWith("DateTimeParseException"), slug);
            // the WS does not read HoraSolicitud: its hour is the local time of FechaIngresoLocal (10:00 by default; '9:05:00' - 3 h = 06:05 for the one digit text)
            assertEquals(slug.equals("hora-un-digito") ? "06:05" : "10:00", campo(salida("ws-" + slug), 0, "HoraSolicitud"), "ws-" + slug + " does not read HoraSolicitud");
        }
        assertTrue(excepcion("hora-nula").startsWith("NullPointerException"));
        assertTrue(excepcion("fecha-solicitud-nula").startsWith("NullPointerException"));
        assertEquals("10:00", campo(salida("ws-hora-nula"), 0, "HoraSolicitud"));
        assertEquals("2026-03-10", campo(salida("ws-fecha-solicitud-nula"), 0, "FechaSolicitud"));
    }

    @Test
    @DisplayName("FM-SFM-03: FechaIngreso = FechaSolicitud + ' ' + hora sin validar la fecha ('ayer 10:00:00'); el WS recalcula FechaSolicitud con la de FechaIngresoLocal")
    void fmSfm03_fechaIngresoConcatenaSinValidar() throws Exception {
        assertEquals("ayer 10:00:00", campo(salida("fecha-ingreso-concatena-sin-validar"), 0, "FechaIngreso"));
        assertEquals("ayer", campo(salida("fecha-ingreso-concatena-sin-validar"), 0, "FechaSolicitud"));
        assertEquals("2026-03-02 10:00:00", campo(salida("ws-fecha-ingreso-concatena-sin-validar"), 0, "FechaIngreso"));
    }

    // ---- FM-SFM-07 WS: entry date and time from FechaIngresoLocal
    @Test
    @DisplayName("FM-SFM-07(c) WS-C02: FechaIngresoLocal 2026-03-10 09:05:00 => FechaIngreso '2026-03-10 09:05:00', FechaSolicitud '2026-03-10', HoraSolicitud '09:05' (LocalTime.toString omite los segundos en cero)")
    void fmSfm07c_horaIsoDelWs() throws Exception {
        JsonObject w = salida("ws-fecha-ingreso-local-09-05-00");
        assertEquals("2026-03-10 09:05:00", campo(w, 0, "FechaIngreso"));
        assertEquals("2026-03-10", campo(w, 0, "FechaSolicitud"));
        assertEquals("09:05", campo(w, 0, "HoraSolicitud"));
        assertEquals("09:05:00", campo(salida("fecha-ingreso-local-09-05-00"), 0, "HoraSolicitud"));   // the SFM twin ignores FechaIngresoLocal: its own HoraSolicitud 12:05:00 - 3 h
    }

    @Test
    @DisplayName("FM-SFM-07(c): segundos distintos de cero se escriben ('09:05:07'), la fraccion tambien ('09:05:00.500', FechaIngreso sin fraccion) y medianoche es '00:00'")
    void fmSfm07c_formasDeLocalTimeToString() throws Exception {
        assertEquals("09:05:07", campo(salida("ws-fecha-ingreso-local-con-segundos"), 0, "HoraSolicitud"));
        assertEquals("09:05:00.500", campo(salida("ws-fecha-ingreso-local-con-fraccion"), 0, "HoraSolicitud"));
        assertEquals("2026-03-10 09:05:00", campo(salida("ws-fecha-ingreso-local-con-fraccion"), 0, "FechaIngreso"));
        assertEquals("00:00", campo(salida("ws-fecha-ingreso-local-medianoche"), 0, "HoraSolicitud"));
    }

    @Test
    @DisplayName("FM-SFM-07(c) D-F3A-10: FechaIngresoLocal nula => NullPointerException en el WS (el SFM no la lee); FechaSolicitud de la salida es la local (2026-03-09) aunque el filtro uso la de la vista (2026-03-10)")
    void fmSfm07c_fechaIngresoLocalNulaYOtraFecha() throws Exception {
        assertTrue(excepcion("ws-fecha-ingreso-local-nula").startsWith("NullPointerException"));
        assertEquals("1", nums(salida("fecha-ingreso-local-nula")));
        JsonObject w = salida("ws-otra-fecha-que-fecha-solicitud");
        assertEquals("1", nums(w));
        assertEquals("2026-03-09", campo(w, 0, "FechaSolicitud"));
        assertEquals("23:00", campo(w, 0, "HoraSolicitud"));
        assertEquals("2026-03-10", campo(salida("otra-fecha-que-fecha-solicitud"), 0, "FechaSolicitud"));
    }

    // ---- FM-SFM-04 global state
    @Test
    @DisplayName("FM-SFM-04 SFM-C01/C02 D-F3A-08: el estado global de cada grupo (ultimo que cumple: ANULADO todas < INGRESADO todas < TERMINADO todas < EN PROCESO alguna); "
            + "los grupos mixtos (TERMINADO+ANULADO, INGRESADO+TERMINADO, desconocidos, nulo, minusculas) quedan en '' y se DESCARTAN; igual en el WS")
    void fmSfm04_estadoGlobal() throws Exception {
        for (Map.Entry<String, String> e : ESTADOS.entrySet()) {
            assertEquals(e.getValue(), estados(salida(e.getKey())), "SFM estados " + e.getKey());
            assertEquals(ESTADOS_NUM.get(e.getKey()), nums(salida(e.getKey())), "SFM nums " + e.getKey());
            assertEquals(e.getValue(), estados(salida("ws-" + e.getKey())), "WS estados " + e.getKey());
        }
        assertTrue(ESTADOS.size() >= 30, "the state table covers at least 30 scenarios, found " + ESTADOS.size());
    }

    @Test
    @DisplayName("FM-SFM-04 D-F3A-08: TERMINADO+ANULADO e INGRESADO+TERMINADO desaparecen en silencio (resultado vacio, contador 0, sin mensaje)")
    void fmSfm04_gruposMixtosDesaparecen() throws Exception {
        for (String slug : new String[] { "estado-terminado-y-anulado-se-descarta", "estado-ingresado-y-terminado-se-descarta", "estado-anulada-e-ingresada-se-descarta", "rev-mixta-se-descarta" }) {
            JsonObject d = dump(slug);
            assertEquals("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"P_OUT_SOLICITUDES\":[]}", d.get("salidaCruda").getAsString(), slug);
        }
    }

    @Test
    @DisplayName("FM-SFM-04: asociacion por FolioContrato distinto de 0 e igual, O por NumSolicitud igual; no es transitiva: A(f1,s1) TERMINADO, B(f1,s2) ANULADO, C(f9,s2) EN PROCESO => A '' (descartada), B y C EN PROCESO")
    void fmSfm04_asociacionNoTransitiva() throws Exception {
        assertEquals("EN PROCESO,EN PROCESO", estados(salida("encadenamiento-no-transitivo")));
        assertEquals("2,2", nums(salida("encadenamiento-no-transitivo")));
        assertEquals("TERMINADO,ANULADO", estados(salida("folio-cero-no-asocia")));   // FolioContrato 0 never associates by folio
        assertEquals("EN PROCESO,EN PROCESO,EN PROCESO", estados(salida("asocia-por-num-solicitud-sin-folio")));
    }

    @Test
    @DisplayName("FM-SFM-04 SFM-C08 D-NOR-9: FolioContrato o NumSolicitud nulos en una fila no REV => NullPointerException (Long.equals); una fila REV con FolioContrato nulo NO falla; REV con FolioRelacionado nulo si")
    void fmSfm04_nulosDeAsociacion() throws Exception {
        assertTrue(excepcion("folio-contrato-nulo").startsWith("NullPointerException"));
        assertTrue(excepcion("ws-folio-contrato-nulo").startsWith("NullPointerException"));
        assertTrue(excepcion("num-solicitud-nulo").startsWith("NullPointerException"));
        assertTrue(excepcion("rev-folio-relacionado-nulo").startsWith("NullPointerException"));
        assertEquals("INGRESADO", estados(salida("fila-unica-folio-contrato-nulo-no-rev")));
    }

    // ---- FM-SFM-05 amounts
    @Test
    @DisplayName("FM-SFM-05 SFM-C03: grupo EN PROCESO (10,5 / 1050) + TERMINADO (4 / 400) + ANULADO (9 / 900): comprometidas 10,5 y 1050, disponibles 4 y 400, la ANULADO no suma "
            + "(el ejemplo de la tarjeta con INGRESADO tiene Estado '' y NO sale en la salida: los montos solo se observan si el estado no es '')")
    void fmSfm05_comprometidasYDisponibles() throws Exception {
        for (String slug : new String[] { "montos-en-proceso-terminado-anulado", "ws-montos-en-proceso-terminado-anulado" }) {
            JsonObject s = salida(slug);
            for (int i = 0; i < 3; i++) assertEquals("10.50000000|1050.00000000|4.00000000|400.00000000", montos(s, i), slug + " row " + i);
        }
    }

    @Test
    @DisplayName("FM-SFM-05: 3 INGRESADO (1,5 / 2,25 / 3,125; 100 / 200 / 300) => comprometidas 6,875 y 600 (escala 8) con disponibles '0' (escala 0); TERMINADO suma disponibles; ANULADO deja todo en '0'")
    void fmSfm05_sumasYEscalas() throws Exception {
        assertEquals("6.87500000|600.00000000|0|0", montos(salida("montos-ingresadas-suman-comprometidas"), 0));
        assertEquals("0|0|10.00000000|1000.00000000", montos(salida("montos-terminadas-suman-disponibles"), 1));
        assertEquals("0|0|0|0", montos(salida("montos-anuladas-no-suman"), 0));
        assertEquals("7.00000000|70.00000000|0|0", montos(salida("montos-estado-desconocido-y-nulo-comprometen"), 2));
        assertEquals("-3.00000000|-30.00000000|0|0", montos(salida("montos-negativos"), 0));
        JsonObject grupos = salida("montos-cada-solicitud-recibe-los-de-su-grupo");   // folio 555: solicitudes 1 and 2 (cuotas 1+2, pesos 1000+1000); folio 556: solicitud 3
        assertEquals("3.00000000|2000.00000000|0|0", montos(grupos, 0));
        assertEquals("3.00000000|2000.00000000|0|0", montos(grupos, 1));
        assertEquals("10.00000000|1000.00000000|0|0", montos(grupos, 2));
    }

    @Test
    @DisplayName("FM-SFM-05: DECIMAL128 (34 digitos): 123456789012345678.12345678 + 0.00000001 = 123456789012345678.12345679")
    void fmSfm05_decimal128() throws Exception {
        assertEquals("123456789012345678.12345679|123456789012345678.12345679|0|0", montos(salida("montos-precision-decimal128"), 0));
    }

    @Test
    @DisplayName("FM-SFM-05 D-NOR-9: monto nulo en una fila comprometida o disponible (BigDecimal.add(null)) => NullPointerException; en una fila ANULADO no se lee; REV usa el destino (nulo => NPE) y no el origen")
    void fmSfm05_montosNulos() throws Exception {
        for (String slug : new String[] { "montos-cuotas-nulas", "montos-pesos-nulos", "montos-nulos-en-terminada", "rev-montos-destino-nulos" }) {
            assertTrue(excepcion(slug).startsWith("NullPointerException"), slug);
            assertTrue(excepcion("ws-" + slug).startsWith("NullPointerException"), "ws-" + slug);
        }
        assertEquals("ANULADO", estados(salida("montos-nulos-en-anulada-no-fallan")));
        assertEquals("INGRESADO", estados(salida("rev-montos-origen-nulos-no-importan")));
        assertEquals("2.00000000|20.00000000|0|0", montos(salida("rev-montos-origen-nulos-no-importan"), 0));
    }

    @Test
    @DisplayName("FM-SFM-04/05 SFM-C04: REV (FolioRelacionado 900) INGRESADO x2 => INGRESADO; comprometidas suman el DESTINO (3+4 cuotas, 30+40 pesos) y no el origen (99); REV TERMINADO(5/50)+EN PROCESO(6/60) => EN PROCESO con disponibles 5/50")
    void fmSfm05_reinversionSumaElDestino() throws Exception {
        assertEquals("7.00000000|70.00000000|0|0", montos(salida("rev-folio-relacionado-ingresadas"), 0));
        assertEquals("6.00000000|60.00000000|5.00000000|50.00000000", montos(salida("rev-terminada-y-en-proceso"), 0));
        assertEquals("EN PROCESO,EN PROCESO", estados(salida("rev-asocia-por-num-solicitud")));
        assertEquals("TERMINADO,ANULADO", estados(salida("rev-y-aporte-no-se-mezclan")));   // REV (folio rel. 900) and APO (folio contrato 900) are separate groups
        assertEquals("INGRESADO", estados(salida("rev-minuscula-es-normal")));            // 'rev' is not REV: origin amounts and FolioContrato
        assertEquals("8.00000000", campo(salida("rev-minuscula-es-normal"), 0, "MontoCuotasComprometidas"));
    }

    // ---- FM-SFM-06 state filter
    @Test
    @DisplayName("FM-SFM-06 SFM-C05: p_Estado se filtra en memoria con equals (sensible a mayusculas y espacios); con p_Estado vacio se quitan solo las filas con Estado ''; "
            + "el grupo mixto (E) nunca se ve")
    void fmSfm06_filtroDeEstado() throws Exception {
        assertEquals("2", nums(salida("p-estado-terminado")));
        assertEquals("", nums(salida("p-estado-terminado-minuscula")));
        assertEquals("3", nums(salida("p-estado-anulado")));
        assertEquals("1", nums(salida("p-estado-ingresado")));
        assertEquals("4", nums(salida("p-estado-en-proceso")));
        assertEquals("", nums(salida("p-estado-desconocido")));
        assertEquals("", nums(salida("p-estado-un-espacio")));
        assertEquals("", nums(salida("p-estado-con-espacio-final")));
        assertEquals("1,2,3,4", nums(salida("p-estado-vacio-quita-los-sin-estado")));
    }

    @Test
    @DisplayName("FM-SFM-06 D-F3A-08: el estado se calcula con las filas que dejo el SQL: con p_IdCliente=200 la fila ANULADO del grupo 100 no entra y la TERMINADO queda sola (TERMINADO, no '')")
    void fmSfm06_elFiltroDelSqlCambiaElGrupo() throws Exception {
        JsonObject s = salida("filtro-sql-cambia-el-grupo");
        assertEquals("1", nums(s));
        assertEquals("TERMINADO", estados(s));
        assertEquals("1,3", nums(salida("p-estado-y-cliente")));
    }

    // ---- FM-SFM-07 / 08 WS join with Tesoreria
    @Test
    @DisplayName("FM-SFM-07(a) WS-C01: IdIngresoEgreso 5001 (RESCFM) con un detalle vigente (comprobante 7001) => IdComprobante 7001 en el WS; el SFM no trae IdComprobante")
    void fmSfm07a_idComprobante() throws Exception {
        JsonObject w = salida("ws-tes-un-detalle-de-comprobante");
        assertEquals("1", nums(w));
        assertEquals("7001", campo(w, 0, "IdComprobante"));
        assertEquals("<absent>", campo(salida("tes-un-detalle-de-comprobante"), 0, "IdComprobante"));
        assertEquals("7001", campo(salida("ws-tes-aporte-tipo-aporfm"), 0, "IdComprobante"));
    }

    @Test
    @DisplayName("FM-SFM-08 WS-C03 D-F3A-10: dos detalles vigentes => el WS devuelve la solicitud DOS veces (7001 y 7002) y comprometidas dobles (20 cuotas / 200 pesos); el SFM una sola vez (10 / 100)")
    void fmSfm08_dosDetallesDuplicanLaFila() throws Exception {
        JsonObject w = salida("ws-tes-dos-detalles-duplican-la-fila");
        assertEquals("1,1", nums(w));
        assertEquals("7001", campo(w, 0, "IdComprobante"));
        assertEquals("7002", campo(w, 1, "IdComprobante"));
        assertEquals("20.00000000|200.00000000|0|0", montos(w, 0));
        assertEquals("20.00000000|200.00000000|0|0", montos(w, 1));
        JsonObject s = salida("tes-dos-detalles-duplican-la-fila");
        assertEquals("1", nums(s));
        assertEquals("10.00000000|100.00000000|0|0", montos(s, 0));
    }

    @Test
    @DisplayName("FM-SFM-08 D-F3A-10: tres detalles => tres filas de la solicitud 1 y una de la 2; detalle repetido (dato sucio) duplica otra vez; el SFM no duplica")
    void fmSfm08_tresDetallesYRepetido() throws Exception {
        assertEquals("1,1,1,2", nums(salida("ws-tes-tres-detalles")));
        assertEquals("1,2", nums(salida("tes-tres-detalles")));
        assertEquals("1,1", nums(salida("ws-tes-detalle-origen-repetido")));
        assertEquals("1", nums(salida("tes-detalle-origen-repetido")));
    }

    @Test
    @DisplayName("FM-SFM-08: los duplicados inflan los conteos por estado: INGRESADO (x2 por el JOIN) + TERMINADO en el mismo folio => '' y desaparecen; con p_Estado=INGRESADO el WS devuelve las dos copias")
    void fmSfm08_duplicadosYEstadoGlobal() throws Exception {
        assertEquals("", nums(salida("ws-tes-duplicados-y-estado-global")));
        assertEquals("", nums(salida("tes-duplicados-y-estado-global")));
        assertEquals("1,1", nums(salida("ws-tes-duplicados-con-p-estado")));
        assertEquals("1", nums(salida("tes-duplicados-con-p-estado")));
    }

    @Test
    @DisplayName("FM-SFM-07(a): el LEFT JOIN de Tesoreria: detalle ANU excluido (queda el vigente 7002), solo anulado => IdComprobante 0, tipo de operacion ajeno, IdIngresoEgreso 0 o nulo, ingreso inexistente, "
            + "detalle sin estado o sin detalle, comprobante nulo (COALESCE) => 0 y una sola fila")
    void fmSfm07a_leftJoinDeTesoreria() throws Exception {
        JsonObject anu = salida("ws-tes-detalle-anulado-no-cuenta");
        assertEquals("1,2", nums(anu));
        assertEquals("7002", campo(anu, 0, "IdComprobante"));
        assertEquals("0", campo(anu, 1, "IdComprobante"));
        for (String slug : new String[] { "tes-tipo-operacion-distinta", "tes-ingreso-egreso-cero", "tes-ingreso-egreso-nulo", "tes-sin-ingreso-egreso", "tes-detalle-con-estado-nulo",
                "tes-origen-sin-detalle", "tes-comprobante-nulo" }) {
            JsonObject w = salida("ws-" + slug);
            assertEquals("1", nums(w), slug);
            assertEquals("0", campo(w, 0, "IdComprobante"), slug);
        }
    }

    // ---- determinism sets (the legacy runs queryParallel and every filter on parallelStream)
    @Test
    @DisplayName("FM-SFM-04/05/06 SFM-C01..C05: 20 grupos de 3 filas + 5 grupos REV: 52 solicitudes salen (40 EN PROCESO, 6 ANULADO, 3 TERMINADO, 3 INGRESADO; el resto es '' y se descarta), en el orden de la consulta")
    void fmSfm_conjuntoGrande() throws Exception {
        for (String slug : new String[] { "determinismo-sesenta-y-diez-filas", "ws-determinismo-sesenta-y-diez-filas" }) {
            JsonObject s = salida(slug);
            assertEquals(52, filas(s).size(), slug);
            assertTrue(nums(s).startsWith("1,2,3,4,5,6,7,8,9,13,14,15,16,17,"), slug + ": " + nums(s));
            assertEquals("2.00000000|30.00000000|1.00000000|10.00000000", montos(s, 0), slug);
            assertEquals("5.50000000|70.00000000|0|0", montos(s, 3), slug);
            assertEquals("EN PROCESO", campo(s, 0, "Estado"), slug);
            assertEquals("ANULADO", campo(s, 12, "Estado"), slug);
        }
        assertEquals(40, filas(salida("determinismo-con-estado")).size());
    }

    @Test
    @DisplayName("FM-SFM-04/05: un grupo de 200 filas (FolioContrato 7, cada 7ma EN PROCESO): todas EN PROCESO con comprometidas 20100 cuotas y 201000 pesos (suma exacta en cualquier orden de reduccion)")
    void fmSfm_grupoDeDoscientas() throws Exception {
        for (String slug : new String[] { "determinismo-grupo-de-doscientas", "ws-determinismo-grupo-de-doscientas" }) {
            JsonObject s = salida(slug);
            assertEquals(200, filas(s).size(), slug);
            for (int i : new int[] { 0, 99, 199 }) {
                assertEquals("EN PROCESO", campo(s, i, "Estado"), slug);
                assertEquals("20100.00000000|201000.00000000|0|0", montos(s, i), slug + " row " + i);
            }
        }
    }

    // ---- RULE-076
    @Test
    @DisplayName("RULE-076: ni el main() de los legacy ni sus ids reales ni su conexion se copiaron (src/main y los fixtures de fondomutuo-sfm)")
    void rule076_sinElMainNiLosIdsDelLegacy() throws Exception {
        // legacy ConsultarSolicitudesFFMM(WS).main: PlataformaUtils.getDESAConnection() and a JSON with a real client id (the number below)
        List<String> culpables = new ArrayList<>();
        for (java.nio.file.Path raiz : List.of(java.nio.file.Path.of("src", "main"), java.nio.file.Path.of("src", "test", "resources", "fondomutuo-sfm"))) {
            assertTrue(java.nio.file.Files.isDirectory(raiz), "RULE-076 needs " + raiz + " (run the tests from the module directory)");
            try (var s = java.nio.file.Files.walk(raiz)) {
                for (java.nio.file.Path p : s.filter(java.nio.file.Files::isRegularFile).toList()) {
                    String t = java.nio.file.Files.readString(p, java.nio.charset.StandardCharsets.ISO_8859_1);
                    if (t.contains("79296") || t.contains("getDESAConnection") || t.contains("getBOLSAConnection")) culpables.add(p.toString());
                }
            }
        }
        assertEquals(List.of(), culpables);
    }
}
