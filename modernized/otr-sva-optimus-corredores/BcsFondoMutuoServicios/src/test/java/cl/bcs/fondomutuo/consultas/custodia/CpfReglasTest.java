package cl.bcs.fondomutuo.consultas.custodia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.custodia.CasoCpf.Perfil;

/**
 * Rule cards of {@code ConsultarCustodiaPortafolioFondoMutuo} (RULES_FONDOMUTUO_F3_A.md 8.2, FM-CPF-01..14; defects D-F3A-03..08 and D-F3A-27, D-NOR-9; matrix CPF-C01..C11), stated with
 * LITERAL values. The expected values are the ones the unmodified legacy (CPF + CalcularMontoNumeroCuota + ObtenerDiaSiguiente, all real) produced (they equal the recorded goldens: this
 * class and {@link CpfEquivalenciaTest} must agree; if the legacy is re-recorded and a literal changes, the legacy won: update the literal and say so). Each test runs the NEW code on the
 * H2 of its case (slug of {@code cases-cpf/FMM-CPF-nnn_slug}) and names the card or defect it pins.
 * Universe: see {@code cases_cpf_lib.py} / {@code cases_cpf_a.py} (clients 100 / 101; portfolios 500, 501 APV, 502, 503; series FM001A .. FM006E with instruments 900 .. 905; system currency
 * CLP with 0 decimals and rounding 'R'; clock and FechaSistema 2026-03-02; the price of EntregarPreciosTasas is 1500.1234; CalcularParidad answers 1 per quota; default solicitud: APO, ING,
 * by units 10, FM001A, client 100, portfolio 500, regime GEN).
 */
class CpfReglasTest {

    private static JsonObject dump(String slug) throws Exception {
        return CasoCpf.ejecutar(CasoCpf.idPorSlug(slug), Perfil.ABIERTA);
    }

    private static JsonArray filas(JsonObject d) {
        assertFalse(d.has("excepcion"), "raised " + d.get("excepcion"));
        return d.getAsJsonObject("salida").getAsJsonArray("P_OUT_TBL");
    }

    private static JsonArray filas(String slug) throws Exception {
        return filas(dump(slug));
    }

    private static JsonObject fila(String slug) throws Exception {
        JsonArray f = filas(slug);
        assertEquals(1, f.size(), slug + " rows");
        return f.get(0).getAsJsonObject();
    }

    private static String v(JsonObject fila, String k) {
        JsonElement e = fila.get(k);
        return e == null ? "<absent>" : e.isJsonNull() ? "<null>" : e.getAsString();
    }

    private static String campos(JsonObject fila, String... ks) {
        List<String> l = new ArrayList<>();
        for (String k : ks) l.add(v(fila, k));
        return String.join("|", l);
    }

    private static String excepcion(String slug) throws Exception {
        JsonObject d = dump(slug);
        assertTrue(d.has("excepcion"), slug + " did not raise");
        assertEquals("ROLLBACK_HTTP500", d.get("decisionServlet").getAsString());
        return d.get("excepcion").getAsString();
    }

    private static List<String> colaboradores(JsonObject d) {
        List<String> l = new ArrayList<>();
        for (JsonElement c : d.getAsJsonArray("colaboradores")) l.add(c.getAsJsonObject().get("colaborador").getAsString());
        return l;
    }

    /** The n-th call (0 based) to the collaborator, its first argument. */
    private static JsonObject llamada(JsonObject d, String colaborador, int n) {
        int i = 0;
        for (JsonElement c : d.getAsJsonArray("colaboradores")) {
            JsonObject o = c.getAsJsonObject();
            if (o.get("colaborador").getAsString().equals(colaborador) && i++ == n) return o.getAsJsonArray("argumentos").get(0).getAsJsonObject();
        }
        throw new AssertionError("no call " + n + " to " + colaborador + " in " + colaboradores(d));
    }

    /** The IdRegistro of every row of the n-th EntregarPreciosTasas call. */
    private static List<Long> idsDePrecios(JsonObject d, int n) {
        List<Long> l = new ArrayList<>();
        for (JsonElement x : llamada(d, "EntregarPreciosTasas", n).getAsJsonArray("p_DatosInstrumentos")) l.add(x.getAsJsonObject().get("IdRegistro").getAsLong());
        return l;
    }

    /** The "Fecha" of every entry of the n-th CalcularParidad call. */
    private static List<String> fechasDeParidad(JsonObject d, int n) {
        List<String> l = new ArrayList<>();
        for (JsonElement x : llamada(d, "CalcularParidad", n).getAsJsonArray("p_CalculosEntrada")) l.add(x.getAsJsonObject().get("Fecha").getAsString());
        return l;
    }

    private static List<String> series(String slug) throws Exception {
        return new ArrayList<>(new TreeSet<>(seriesEnOrden(slug)));
    }

    private static List<String> seriesEnOrden(String slug) throws Exception {
        List<String> l = new ArrayList<>();
        for (JsonElement f : filas(slug)) l.add(v(f.getAsJsonObject(), "IdSerieFondo"));
        return l;
    }

    // ---- FM-CPF-01 contract
    @Test
    @DisplayName("FM-CPF-01 CPF-C01 D-F3A-27: la salida solo trae P_OUT_TBL (sin contador ni mensajes) y el anfitrion hace ROLLBACK en TODA respuesta; la custodia recibe los defaults del pojo")
    void fmCpf01_soloTblYRollback() throws Exception {
        JsonObject d = dump("custodia-sola");
        assertEquals("[P_OUT_TBL]", d.getAsJsonObject("salida").keySet().toString());
        assertEquals("ROLLBACK", d.get("decisionServlet").getAsString());
        assertEquals("{\"p_AgrupaPorBoveda\":\"N\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"2026-03-02\",\"p_IdBoveda\":\"\",\"p_IdCliente\":0,\"p_IdEmisor\":null,\"p_IdEmpresa\":1,"
                + "\"p_IdFamiliaFondo\":\"\",\"p_IdInstrumento\":0,\"p_IdModulo\":\"FMM\",\"p_IdPortafolio\":0,\"p_IdProductoAgrupador\":null,\"p_IdUsuario\":7,\"p_TipoFechaConsulta\":\"LIQ\"}",
                CpfEquivalenciaTest.canon(llamada(d, "ConsultarCustodiaPortafolioFFMM", 0)).toString());
        assertEquals(List.of("ConsultarCustodiaPortafolioFFMM", "EntregarPreciosTasas", "CalcularParidad", "ConsultarEmpresa", "CalcularParidad"), colaboradores(d));
    }

    @Test
    @DisplayName("D-F3A-27: ninguna respuesta con filas lleva P_OUT_ERRORESCONTADOR (el anfitrion la trata como error: ROLLBACK y auditoria ERROR)")
    void dF3A27_ningunaRespuestaTraeContador() throws Exception {
        for (String slug : new String[] { "sin-nada", "transito-sin-custodia", "rescate-total-con-custodia-y-aporte", "orden-seis-saldos", "garantia-distinta-de-cero", "mlt-s-sin-regimenes" }) {
            JsonObject d = dump(slug);
            assertFalse(d.getAsJsonObject("salida").has("P_OUT_ERRORESCONTADOR"), slug);
            assertFalse(d.getAsJsonObject("salida").has("P_OUT_MENSAJES"), slug);
            assertEquals("ROLLBACK", d.get("decisionServlet").getAsString(), slug);
        }
    }

    @Test
    @DisplayName("FM-CPF-01 sin-nada: sin solicitudes ni custodia => P_OUT_TBL vacio; CalcularMontoNumeroCuota corre con 0 series (sin llamar a nadie) pero la empresa SI se consulta (D-F3A-07)")
    void fmCpf01_sinNada() throws Exception {
        JsonObject d = dump("sin-nada");
        assertEquals("{\"P_OUT_TBL\":[]}", d.get("salidaCruda").getAsString());
        assertEquals(List.of("ConsultarCustodiaPortafolioFFMM", "ConsultarEmpresa", "CalcularParidad"), colaboradores(d));
        assertEquals(0, llamada(d, "CalcularParidad", 0).getAsJsonArray("p_CalculosEntrada").size());
    }

    @Test
    @DisplayName("FM-CPF-01 / D-NOR-9 DESVIO-KIT-1: empresa ausente, nula o sin configuracion => NullPointerException del legacy o ParametroNoEncontradoException del kit (decision del usuario: se mantiene la del kit)")
    void fmCpf01_empresaNulaOSinConfiguracion() throws Exception {
        for (String slug : new String[] { "empresa-ausente", "empresa-nula", "empresa-sin-configuracion" }) {
            String e = excepcion(slug);
            assertTrue(e.startsWith("NullPointerException:") || (e.startsWith("Exception: Parametro ") && e.endsWith(" no encontrado")), slug + " => " + e);
        }
    }

    @Test
    @DisplayName("FM-CPF-01: p_IdEmpresa '1' (texto) se convierte a 1; 'abc' y p_IdCliente 1.5 => JsonSyntaxException (HTTP 500)")
    void fmCpf01_entradaDeTipoIncorrecto() throws Exception {
        assertEquals(1, filas("empresa-como-texto").size());
        assertTrue(excepcion("empresa-no-numerica").startsWith("JsonSyntaxException:"));
        assertTrue(excepcion("cliente-decimal").startsWith("JsonSyntaxException:"));
    }

    @Test
    @DisplayName("FM-CPF-01: solo p_IdEmpresa => usuario y modulo nulos viajan nulos a la custodia (no hay RevisarRequisitosBasicos)")
    void fmCpf01_soloEmpresa() throws Exception {
        JsonObject d = dump("entrada-solo-empresa");
        assertEquals(1, filas(d).size());
        JsonObject c = llamada(d, "ConsultarCustodiaPortafolioFFMM", 0);
        assertTrue(c.get("p_IdUsuario").isJsonNull());
        assertTrue(c.get("p_IdModulo").isJsonNull());
    }

    @Test
    @DisplayName("FM-CPF-01: los nulos explicitos pisan los defaults del pojo: p_IdCliente nulo => el SQL no devuelve solicitudes; p_IdFamiliaFondo nulo => tampoco; p_IdEmisor '' filtra; los nulos de producto y emisor = sin filtro")
    void fmCpf01_nulosExplicitos() throws Exception {
        JsonObject d = dump("cliente-nulo-explicito");
        assertEquals("0|0", campos(filas(d).get(0).getAsJsonObject(), "CantidadTransSolicitudesAporte", "MontoTransSolicitudesAporte"));
        assertTrue(llamada(d, "ConsultarCustodiaPortafolioFFMM", 0).get("p_IdCliente").isJsonNull());
        d = dump("familia-nula-explicita");
        assertEquals("0|0", campos(filas(d).get(0).getAsJsonObject(), "CantidadTransSolicitudesAporte", "MontoTransSolicitudesAporte"));
        assertTrue(llamada(d, "ConsultarCustodiaPortafolioFFMM", 0).get("p_IdFamiliaFondo").isJsonNull());
        assertEquals(0, filas("emisor-vacio").size());
        assertEquals("10.0000", v(fila("filtros-nulos-explicitos"), "CantidadTransSolicitudesAporte"));
        assertEquals(0, filas("cliente-negativo").size());
        assertEquals(0, filas("instrumento-inexistente").size());
    }

    // ---- FM-CPF-02 date of the query
    @Test
    @DisplayName("FM-CPF-02 CPF-C08: p_FechaConsulta '0001-01-01' o nula => PLA.FechaSistema; otra fecha se usa tal cual: llega a la custodia, a los precios y a la paridad")
    void fmCpf02_fechaDeConsulta() throws Exception {
        Map<String, String> esperado = Map.of("fecha-defecto", "2026-03-02", "fecha-nula", "2026-03-02", "fecha-explicita", "2026-02-27", "fecha-sistema-distinta", "2026-01-15");
        for (Map.Entry<String, String> e : esperado.entrySet()) {
            JsonObject d = dump(e.getKey());
            assertEquals(e.getValue(), llamada(d, "ConsultarCustodiaPortafolioFFMM", 0).get("p_FechaConsulta").getAsString(), e.getKey());
            assertEquals(e.getValue() + " 00:00:00.000", llamada(d, "EntregarPreciosTasas", 0).getAsJsonArray("p_DatosInstrumentos").get(0).getAsJsonObject().get("FechaConsulta").getAsString(), e.getKey());
            assertEquals(List.of(e.getValue()), fechasDeParidad(d, 1), e.getKey());
        }
        assertEquals("2026-02-27 10:00:00", llamada(dump("fecha-con-hora"), "ConsultarCustodiaPortafolioFFMM", 0).get("p_FechaConsulta").getAsString());
    }

    @Test
    @DisplayName("FM-CPF-02: una fecha sin formato ('' o 'hoy') no se valida: llega a la valorizacion y falla con DateTimeParseException; sin PLA.FechaSistema falla aun con fecha explicita (se lee primero)")
    void fmCpf02_fechaInvalidaYParametro() throws Exception {
        assertTrue(excepcion("fecha-vacia").startsWith("DateTimeParseException:"));
        assertTrue(excepcion("fecha-texto-libre").startsWith("DateTimeParseException:"));
        assertEquals("Exception: Parametro PLA.FechaSistema en modulo PLATAFORMA no encontrado", excepcion("fecha-explicita-sin-fecha-sistema"));
        assertEquals(0, filas("fecha-explicita-sin-valorizar").size());
    }

    // ---- parameters (all mandatory)
    @Test
    @DisplayName("D-NOR-9 FM-CPF-03/04/08/13: cada parametro es obligatorio: sin el => Exception 'Parametro X en modulo Y no encontrado' (texto comparado)")
    void dNor9_parametrosObligatorios() throws Exception {
        Map<String, String> esperado = Map.of(
                "sin-parametro-ws-solicitudes", "Exception: Parametro WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS en modulo FONDOMUTUO no encontrado",
                "sin-moneda-sistema", "Exception: Parametro PLA.MonedaSistema en modulo PLATAFORMA no encontrado",
                "sin-usa-dolar-observado", "Exception: Parametro UsaDolarObservado en modulo FONDOMUTUO no encontrado",
                "sin-pais-sistema", "Exception: Parametro PLA.PaisSistema en modulo PLATAFORMA no encontrado",
                "sin-calcula-valor-cuota-dia-siguiente", "Exception: Parametro PLA.CalculaValorCuotaDiaSiguiente en modulo PLATAFORMA no encontrado");
        for (Map.Entry<String, String> e : esperado.entrySet()) assertEquals(e.getValue(), excepcion(e.getKey()), e.getKey());
    }

    // ---- FM-CPF-03 universe of the solicitudes in transit
    @Test
    @DisplayName("FM-CPF-03: universo de solicitudes en transito (estados ING/PEN, tipos APO/RES, empresa, filtros de cliente, portafolio, instrumento, producto agrupador, emisor y familia); series que salen")
    void fmCpf03_universoYFiltros() throws Exception {
        Map<String, List<String>> esperado = Map.ofEntries(
                Map.entry("transito-sin-custodia", List.of("FM001A")),
                Map.entry("estados-ing-y-pen", List.of("FM001A", "FM002B")),
                Map.entry("tipos-apo-res-otros-no", List.of("FM001A", "FM002B")),
                Map.entry("empresa-ajena-no-sale", List.of("FM001A")),
                Map.entry("filtro-cliente", List.of("FM002B")),
                Map.entry("filtro-portafolio", List.of("FM002B")),
                Map.entry("filtro-instrumento", List.of("FM002B")),
                Map.entry("filtro-producto-agrupador", List.of("FM002B")),
                Map.entry("filtro-producto-agrupador-cero", List.of("FM001A")),
                Map.entry("filtro-emisor", List.of("FM002B")),
                Map.entry("filtro-familia", List.of("FM002B")),
                Map.entry("filtros-combinados-sin-filas", List.of()),
                Map.entry("sin-solicitudes-que-cumplan", List.of()));
        for (Map.Entry<String, List<String>> e : esperado.entrySet()) assertEquals(e.getValue(), series(e.getKey()), e.getKey());
        assertEquals("501", v(filas("filtro-portafolio").get(0).getAsJsonObject(), "IdPortafolio"));
    }

    @Test
    @DisplayName("FM-CPF-03 CPF-C10 QD-F3A-02: filtro de solicitudes automaticas (parametro exactamente 'S'): solo IngresoAutomatico 'N' o regimen BF; 's' y 'SI' no filtran")
    void fmCpf03_solicitudesAutomaticas() throws Exception {
        assertEquals(List.of("FM001A", "FM002B", "FM003U", "FM005D"), series("automaticas-sin-filtro"));
        assertEquals(List.of("FM002B", "FM003U"), series("automaticas-con-filtro"));
        assertEquals(List.of("FM001A", "FM002B", "FM003U", "FM005D"), series("automaticas-filtro-minuscula"));
        assertEquals(List.of("FM001A", "FM002B", "FM003U", "FM005D"), series("automaticas-filtro-valor-raro"));
    }

    @Test
    @DisplayName("FM-CPF-03/04: aporte por monto con cuotas 5 y Spot origen (no MOD): CantidadMontoAporte = Spot.MontoMonedaSecundaria 777,5; con Spot MOD o sin Spot = MontoMonedaSistema 199000")
    void fmCpf03_montoSpotDelAporte() throws Exception {
        assertEquals("133.3224|777.50000000", campos(fila("aporte-con-spot-origen"), "CantidadTransSolicitudesAporte", "MontoTransSolicitudesAporte"));
        assertEquals("133.3224|199000.00000000", campos(fila("aporte-con-spot-modificado"), "CantidadTransSolicitudesAporte", "MontoTransSolicitudesAporte"));
        assertEquals("133.3224|199000.00000000", campos(fila("aporte-sin-spot"), "CantidadTransSolicitudesAporte", "MontoTransSolicitudesAporte"));
    }

    @Test
    @DisplayName("FM-CPF-03/08: datos del portafolio de la solicitud: APV 501 (matriz 7, producto 3, ADC S), 502 (matriz NOAPV 9, ADC S), 503 sin matriz (0, N, producto nulo), 999 inexistente (todo nulo, matriz 0); regimen sin descripcion = ''")
    void fmCpf03_datosDelPortafolio() throws Exception {
        String k = "CodigoPortafolio,IdSocioDeNegocio,DocumentoIdentidad,RazonSocial,Producto,IdMatrizAgrupador,EsADC,RegimenTributario";
        String[] ks = k.split(",");
        assertEquals("APV-B|1100|11111111-1|Cliente Ficticio Uno|Producto APV tres|7|S|Regimen General", campos(fila("portafolio-apv"), ks));
        assertEquals("PF502|1101|22222222-2|Cliente Ficticio Dos|<null>|9|S|Regimen General", campos(fila("portafolio-noapv-adc"), ks));
        assertEquals("PF503|1101|22222222-2|Cliente Ficticio Dos|<null>|0|N|Regimen General", campos(fila("portafolio-sin-matriz"), ks));
        assertEquals("<null>|<null>|<null>|<null>|<null>|0|N|Regimen General", campos(fila("portafolio-inexistente"), ks));
        assertEquals("PF500|1100|11111111-1|Cliente Ficticio Uno|<null>|8|N|", campos(fila("regimen-sin-descripcion"), ks));
        assertEquals("COR|", campos(fila("transito-sin-custodia"), "IdBoveda", "Relacion"));
    }

    @Test
    @DisplayName("FM-CPF-03/04: una garantia (INGGTA) no se excluye del transito y su peticion de precios lleva IdUso GARANTIA")
    void fmCpf03_garantiaEnTransito() throws Exception {
        JsonObject d = dump("garantia-en-transito");
        assertEquals("GARANTIA", llamada(d, "EntregarPreciosTasas", 0).getAsJsonArray("p_DatosInstrumentos").get(0).getAsJsonObject().get("IdUso").getAsString());
        assertEquals("CARTERA", llamada(d, "EntregarPreciosTasas", 1).getAsJsonArray("p_DatosInstrumentos").get(0).getAsJsonObject().get("IdUso").getAsString());
    }

    // ---- FM-CPF-04 valuation of the solicitudes
    @Test
    @DisplayName("FM-CPF-04 CPF-C02 D-F3A-05: valorizacion de solicitudes en transito (CantidadAporte|CantidadMontoAporte|CantidadRescate|CantidadMontoRescate), literales del legacy")
    void fmCpf04_valorizacionDeSolicitudes() throws Exception {
        Map<String, String> esperado = Map.ofEntries(
                Map.entry("transito-sin-custodia", "10.0000|15001|0E-8|0E-8"),
                Map.entry("aporte-por-cuotas", "10.0000|15001|0E-8|0E-8"),
                Map.entry("aporte-por-monto-sin-cuotas", "0E-8|150000.00000000|0E-8|0E-8"),
                Map.entry("aporte-por-monto-con-cuotas", "99.9918|199000.00000000|0E-8|0E-8"),
                Map.entry("rescate-por-cuotas", "0E-8|0E-8|8.5000|12751"),
                Map.entry("rescate-por-monto-con-cuotas", "0E-8|0E-8|59.9951|90000"),
                Map.entry("cuotas-negativas-no-valorizan", "-5.00000000|100.00000000|0E-8|0E-8"),
                Map.entry("transito-rescate-valorizado", "0E-8|0E-8|8.5000|12751"));
        for (Map.Entry<String, String> e : esperado.entrySet()) {
            assertEquals(e.getValue(), campos(fila(e.getKey()), "CantidadTransSolicitudesAporte", "MontoTransSolicitudesAporte", "CantidadTransSolicitudesRescate", "MontoTransSolicitudesRescate"), e.getKey());
        }
    }

    @Test
    @DisplayName("FM-CPF-04 D-F3A-04: el IdInterno de la solicitud empieza en 2 (aporte 2, rescate -2) y la paridad usa IdInterno*4+k; las filas con cuotas <= 0 no se valorizan (cero llamadas)")
    void fmCpf04_idsYFilasSinValorizar() throws Exception {
        JsonObject d = dump("transito-sin-custodia");
        assertEquals(List.of(2L), idsDePrecios(d, 0));
        assertEquals("[10, 11]", paridadIds(d, 0).toString());
        assertEquals(List.of(1L), idsDePrecios(d, 1));
        d = dump("rescate-por-cuotas");
        assertEquals(List.of(-2L), idsDePrecios(d, 0));
        assertEquals("[-10, -11]", paridadIds(d, 0).toString());
        assertEquals(List.of(-1L), idsDePrecios(d, 2));          // the rescate in transit of the saldo 1, valued again as a saldo (FM-CPF-12)
        assertEquals(List.of("ConsultarCustodiaPortafolioFFMM", "EntregarPreciosTasas", "CalcularParidad", "ConsultarEmpresa", "CalcularParidad"), colaboradores(dump("aporte-por-monto-sin-cuotas")));
        assertEquals(List.of("ConsultarCustodiaPortafolioFFMM", "EntregarPreciosTasas", "CalcularParidad", "ConsultarEmpresa", "CalcularParidad"), colaboradores(dump("cuotas-negativas-no-valorizan")));
    }

    private static List<Long> paridadIds(JsonObject d, int n) {
        List<Long> l = new ArrayList<>();
        for (JsonElement x : llamada(d, "CalcularParidad", n).getAsJsonArray("p_CalculosEntrada")) l.add(x.getAsJsonObject().get("IdInterno").getAsLong());
        return l;
    }

    @Test
    @DisplayName("FM-CPF-04 D-F3A-05: fondo en dolares: el monto en moneda sistema del aporte sale de CalcularParidad (id*4+1 = 9: 7.200.000) y el del rescate de id -2 (-9: 2.880.000), luego -1 (-5: 2.881.000) en transito")
    void fmCpf04_fondoEnDolares() throws Exception {
        assertEquals("10.00|7200000", campos(fila("aporte-fondo-en-dolares"), "CantidadTransSolicitudesAporte", "MontoTransSolicitudesAporte"));
        assertEquals("4.00|2881000", campos(fila("rescate-fondo-en-dolares"), "CantidadTransSolicitudesRescate", "MontoTransSolicitudesRescate"));
        assertTrue(excepcion("moneda-fondo-sin-formato").startsWith("Exception: formato no encontrado moneda USD"));
    }

    @Test
    @DisplayName("FM-CPF-04 D-NOR-9: sin precios la valorizacion responde ERROR_VALORCUOTA sin IdInterno y la fila queda sin valorizar; con DOS series, dos resultados con clave nula => IllegalStateException; serie desconocida: la fila no sale")
    void fmCpf04_sinPrecio() throws Exception {
        assertEquals("10.00000000|0E-8", campos(fila("valorizacion-sin-precio"), "CantidadTransSolicitudesAporte", "MontoTransSolicitudesAporte"));
        assertTrue(excepcion("valorizacion-sin-precio-dos-series").startsWith("IllegalStateException:"));
        assertEquals(0, filas("serie-desconocida-valorizada").size());
    }

    // ---- FM-CPF-05 / 09 total rescate
    @Test
    @DisplayName("FM-CPF-05/09 CPF-C04 QD-F3A-03: rescate total: CantidadTransSolicitudesRescate = custodia 100 + aporte en transito 20 = 120 y se valoriza (MontoTransSolicitudesRescate 180015); solo custodia = 100 (150012)")
    void fmCpf05_rescateTotal() throws Exception {
        assertEquals("100|20.0000|120.0000|30002|180015", campos(fila("rescate-total-con-custodia-y-aporte"), "CantidadCustodia", "CantidadTransSolicitudesAporte", "CantidadTransSolicitudesRescate", "MontoTransSolicitudesAporte",
                "MontoTransSolicitudesRescate"));
        assertEquals("100|0|100|150012", campos(fila("rescate-total-solo-custodia"), "CantidadCustodia", "CantidadTransSolicitudesAporte", "CantidadTransSolicitudesRescate", "MontoTransSolicitudesRescate"));
        assertEquals("100|0|100|150012", campos(fila("rescate-total-apo-tambien-cuenta"), "CantidadCustodia", "CantidadTransSolicitudesAporte", "CantidadTransSolicitudesRescate", "MontoTransSolicitudesRescate"));
        assertEquals("-10|-10|-15001", campos(fila("rescate-total-custodia-negativa"), "CantidadCustodia", "CantidadTransSolicitudesRescate", "MontoTransSolicitudesRescate"));
    }

    @Test
    @DisplayName("FM-CPF-05: la marca es por portafolio_instrumento (todos los regimenes); otro portafolio, otro instrumento, estado cerrado o sin saldo => sin rescate total; el filtro automatico NO exime a BF aqui")
    void fmCpf05_marcaDeRescateTotal() throws Exception {
        JsonArray f = filas("rescate-total-aplica-a-todos-los-regimenes");
        assertEquals("BF|40|40", campos(f.get(0).getAsJsonObject(), "IdRegimenFondoMutuo", "CantidadCustodia", "CantidadTransSolicitudesRescate"));
        assertEquals("GEN|100|100", campos(f.get(1).getAsJsonObject(), "IdRegimenFondoMutuo", "CantidadCustodia", "CantidadTransSolicitudesRescate"));
        for (String slug : new String[] { "rescate-total-otro-portafolio", "rescate-total-otro-instrumento", "rescate-total-estado-cerrado", "rescate-total-filtro-automatico-sin-bf", "rescate-total-filtros-de-entrada" }) {
            for (JsonElement r : filas(slug)) assertEquals("0", v(r.getAsJsonObject(), "CantidadTransSolicitudesRescate"), slug);
        }
        assertEquals(0, filas("rescate-total-sin-saldo").size());
        assertEquals("100", v(fila("rescate-total-filtro-automatico-no-automatica"), "CantidadTransSolicitudesRescate"));
        assertEquals("100", v(fila("rescate-total-dos-solicitudes-t"), "CantidadTransSolicitudesRescate"));
    }

    // ---- FM-CPF-06 / 07 custody and grouping
    @Test
    @DisplayName("FM-CPF-06/07/08 CPF-C03: solicitud y custodia de la misma clave => UN saldo con los datos de la custodia (bovedas DCV, relacion REL1) y las cantidades en transito; dos custodias de la clave: gana la ultima")
    void fmCpf07_unSaldoPorClave() throws Exception {
        assertEquals("DCV|REL1|100|20.0000", campos(fila("solicitud-y-custodia-misma-clave"), "IdBoveda", "Relacion", "CantidadCustodia", "CantidadTransSolicitudesAporte"));
        assertEquals("DCV|R2|60", campos(fila("custodia-la-ultima-gana"), "IdBoveda", "Relacion", "CantidadCustodia"));
        assertEquals(List.of("FM001A", "FM002B"), seriesEnOrden("custodia-y-transito-de-saldos-distintos"));
        assertEquals(List.of("FM001A/BF", "FM001A/GEN"), regimenes("regimenes-distintos-dos-saldos"));
        assertEquals(List.of("100/FM001A", "101/FM001A"), clientes("clientes-distintos-dos-saldos"));
    }

    private static List<String> regimenes(String slug) throws Exception {
        List<String> l = new ArrayList<>();
        for (JsonElement f : filas(slug)) l.add(v(f.getAsJsonObject(), "IdSerieFondo") + "/" + v(f.getAsJsonObject(), "IdRegimenFondoMutuo"));
        return l;
    }

    private static List<String> clientes(String slug) throws Exception {
        List<String> l = new ArrayList<>();
        for (JsonElement f : filas(slug)) l.add(v(f.getAsJsonObject(), "IdCliente") + "/" + v(f.getAsJsonObject(), "IdSerieFondo"));
        return l;
    }

    @Test
    @DisplayName("FM-CPF-07 D-F3A-05: dos filas del SQL del mismo saldo solo suman las cuatro cantidades (aporte y rescate, cuotas y montos) sobre la primera: 3400 / 700 sin valorizar, 23501 con una valorizada")
    void fmCpf07_mergeDeSolicitudes() throws Exception {
        assertEquals("0E-8|3400.00000000|0E-8|700.00000000", campos(fila("merge-sin-valorizar"), "CantidadTransSolicitudesAporte", "MontoTransSolicitudesAporte", "CantidadTransSolicitudesRescate", "MontoTransSolicitudesRescate"));
        assertEquals("15.9995|23501.00000000", campos(fila("merge-dos-solicitudes-mismo-saldo"), "CantidadTransSolicitudesAporte", "MontoTransSolicitudesAporte"));
        assertEquals("15.99950000|15001.00000000|2.00000000|3000", campos(fila("merge-tres-solicitudes"), "CantidadTransSolicitudesAporte", "MontoTransSolicitudesAporte", "CantidadTransSolicitudesRescate",
                "MontoTransSolicitudesRescate"));
        assertEquals("1100.00000000|2200.00000000", campos(fila("aporte-y-rescate-misma-fila-de-saldo"), "MontoTransSolicitudesAporte", "MontoTransSolicitudesRescate"));
    }

    @Test
    @DisplayName("FM-CPF-06/08 / D-NOR-9: custodia sin p_out_tbl => NullPointerException; custodia que falla => su Exception tal cual; instrumento sin serie => el saldo se omite; dos series de un instrumento => 'key field is not unique'")
    void fmCpf06_08_casosLimiteDeLaCustodiaYDeLasSeries() throws Exception {
        assertTrue(excepcion("custodia-tabla-nula").startsWith("NullPointerException:"));
        assertEquals("Exception: falla simulada de la custodia", excepcion("custodia-falla"));
        assertEquals(List.of("FM001A"), seriesEnOrden("custodia-instrumento-sin-serie"));
        assertEquals(List.of("FM001A"), seriesEnOrden("custodia-instrumento-nulo"));
        assertEquals("Exception: key field is not unique", excepcion("dos-series-un-instrumento"));
        assertTrue(excepcion("custodia-cantidad-nula").startsWith("NullPointerException:"));
        assertTrue(excepcion("custodia-cantidad-garantia-nula").startsWith("NullPointerException:"));
        assertEquals(0, filas("solicitud-sin-serie-instrumento-ajeno").size());
        assertTrue(excepcion("serie-sin-fondo").startsWith("NullPointerException:"));
        assertEquals("100|<null>", campos(fila("custodia-sin-regimen"), "IdCliente", "IdRegimenFondoMutuo"));
        assertEquals("<null>|GEN", campos(fila("custodia-cliente-nulo"), "IdCliente", "IdRegimenFondoMutuo"));
    }

    // ---- FM-CPF-08 fields
    @Test
    @DisplayName("FM-CPF-08 D-F3A-06: campos de la serie (FM002B): Nemotecnico = IdSerieFondo, NombreAdministradora = IdEmisor, moneda = moneda del fondo, documento de la administradora = el predeterminado")
    void fmCpf08_camposDeLaSerie() throws Exception {
        JsonObject f = fila("campos-de-la-serie");
        assertEquals("FM002B|FM002B|AGF2|AGF2|CLP|CLP|CLP|2|2|FAM2|N|11|Fondo Ficticio Dos|Serie B ficticia|B|S|76000002-2",
                campos(f, "IdSerieFondo", "Nemotecnico", "IdEmisor", "NombreAdministradora", "IdMoneda", "IdMonedaPrincipal", "IdMonedaFondo", "DecimalesValorCuota", "DecimalesCuota", "IdFamiliaFondo", "FondoExtranjero",
                        "Fondo", "NombreFondo", "NombreSerieFondo", "CodigoSerie", "RestringeRescate", "DocumentoIdentidadAdministradora"));
        assertEquals("FM|Fondos Mutuos|FM|Fondo Mutuo|1", campos(f, "IdClasificacionTipoInstrumento", "NombreClasificacionTipoInstrumento", "IdTipoInstrumento", "NombreTipoInstrumento", "IdEmpresa"));
        f = fila("fondo-en-dolares-extranjero");
        assertEquals("USD|S|FMX|Fondo Mutuo Extranjero|<null>|EXT1|4|2", campos(f, "IdMoneda", "FondoExtranjero", "IdTipoInstrumento", "NombreTipoInstrumento", "DocumentoIdentidadAdministradora", "NombreAdministradora",
                "DecimalesValorCuota", "DecimalesCuota"));
        assertEquals("<null>|AGF3", campos(fila("emisor-sin-documento-predeterminado"), "DocumentoIdentidadAdministradora", "NombreAdministradora"));
        assertEquals("<null>|<null>|AGF1", campos(fila("instrumento-sin-tipo"), "IdTipoInstrumento", "NombreTipoInstrumento", "IdEmisor"));
    }

    @Test
    @DisplayName("FM-CPF-08: sin custodia => IdBoveda 'COR', Relacion '', cantidades de custodia 0, PPPCustodia 0 e INGPrecioPromedio 0; con custodia los datos de cliente salen de la custodia; PPPCustodia nulo => 0; INGPrecioPromedio nulos se copian nulos")
    void fmCpf08_valoresPorDefectoYCopiaDeLaCustodia() throws Exception {
        assertEquals("COR||0|0|0|0|0", campos(fila("sin-custodia-datos-de-la-solicitud"), "IdBoveda", "Relacion", "CantidadCustodia", "PPPCustodia", "INGPrecioPromedioCompra", "INGMontoPrecioPromedioCompra",
                "INGMontoPrecioPromedioMonedaInstrumento"));
        assertEquals("1234.5678|1111.1111|2222.2222|3333.3333", campos(fila("custodia-sola"), "PPPCustodia", "INGPrecioPromedioCompra", "INGMontoPrecioPromedioCompra", "INGMontoPrecioPromedioMonedaInstrumento"));
        assertEquals("0", v(fila("ppp-custodia-nulo"), "PPPCustodia"));
        assertEquals("<null>|<null>|<null>", campos(fila("ing-precios-nulos"), "INGPrecioPromedioCompra", "INGMontoPrecioPromedioCompra", "INGMontoPrecioPromedioMonedaInstrumento"));
        assertEquals("5555|99999999-9|Razon de la custodia|APV-B|Producto APV tres|7|S", campos(fila("custodia-datos-de-cliente"), "IdSocioDeNegocio", "DocumentoIdentidad", "RazonSocial", "CodigoPortafolio", "Producto", "IdMatrizAgrupador", "EsADC"));
    }

    // ---- FM-CPF-10 MLT
    @Test
    @DisplayName("FM-CPF-10 CPF-C06 D-F3A-08: MLT con BuscaMLT exactamente 'S': el PRIMER regimen ACT de la serie (sin ORDER BY) con RescateConMandato o AceptaMandatoEnReinversion 'S'; sin regimenes queda NULO; otro valor => 'N'")
    void fmCpf10_mlt() throws Exception {
        Map<String, String> esperado = Map.ofEntries(
                Map.entry("mlt-no-buscado", "N"),
                Map.entry("mlt-s-rescate-con-mandato", "S"),
                Map.entry("mlt-s-acepta-mandato-reinversion", "S"),
                Map.entry("mlt-s-sin-mandato", "N"),
                Map.entry("mlt-s-sin-regimenes", "<null>"),
                Map.entry("mlt-s-solo-el-primer-regimen", "N"),
                Map.entry("mlt-s-primer-regimen-con-mandato", "S"),
                Map.entry("mlt-s-regimen-inactivo", "N"),
                Map.entry("mlt-s-solo-regimenes-inactivos", "<null>"),
                Map.entry("mlt-minuscula", "N"),
                Map.entry("mlt-nulo", "N"));
        for (Map.Entry<String, String> e : esperado.entrySet()) assertEquals(e.getValue(), v(fila(e.getKey()), "MLT"), e.getKey());
        JsonArray dos = filas("mlt-s-solo-ese-saldo");
        assertEquals("FM001A|N", campos(dos.get(0).getAsJsonObject(), "IdSerieFondo", "MLT"));
        assertEquals("FM002B|S", campos(dos.get(1).getAsJsonObject(), "IdSerieFondo", "MLT"));
        for (JsonElement r : filas("mlt-s-dos-saldos-misma-serie")) assertEquals("N", v(r.getAsJsonObject(), "MLT"));
    }

    // ---- FM-CPF-11 custody valuation
    @Test
    @DisplayName("FM-CPF-11 CPF-C01: 100 cuotas x 1500,1234: Monto 150012, ValorCuota 1500.1234, FechaValorCuota de la fuente del precio; sin precio queda 0001-01-01, Monto 0, ValorCuota 0")
    void fmCpf11_valorizacionDeLaCustodia() throws Exception {
        assertEquals("150012|1500.1234|2026-03-02|100|1", campos(fila("custodia-valorizada"), "Monto", "ValorCuota", "FechaValorCuota", "MontoMonedaSistema", "ValorCuotaMonedaSistema"));
        assertEquals("150012|2026-02-27", campos(fila("custodia-fecha-de-la-fuente"), "Monto", "FechaValorCuota"));
        assertEquals("0|0|0001-01-01|100", campos(fila("custodia-sin-precio"), "Monto", "ValorCuota", "FechaValorCuota", "MontoMonedaSistema"));
        assertTrue(excepcion("custodia-dos-series-sin-precio").startsWith("IllegalStateException:"));
    }

    @Test
    @DisplayName("FM-CPF-11 D-CUO-1: cantidades limite de la custodia: 0 => monto 0; 33,3333 => 50004; -10 => -15001; precio 0 (BigDecimal.ZERO) => salta el calculo y la paridad va vacia; precio 0.0 (escala 1) NO se salta")
    void fmCpf11_cantidadesYPreciosLimite() throws Exception {
        assertEquals("0|0", campos(fila("custodia-cantidad-cero"), "Monto", "MontoMonedaSistema"));
        assertEquals("50004|33", campos(fila("custodia-cantidad-fraccionaria"), "Monto", "MontoMonedaSistema"));
        assertEquals("-15001|-10", campos(fila("custodia-cantidad-negativa"), "Monto", "MontoMonedaSistema"));
        assertEquals("150012339999250|100000000000", campos(fila("custodia-cantidad-grande"), "Monto", "MontoMonedaSistema"));
        assertEquals("0|0", campos(fila("custodia-cantidad-minima"), "Monto", "MontoMonedaSistema"));
        JsonObject d = dump("custodia-precio-cero");
        assertEquals("0|0", campos(fila(d), "Monto", "ValorCuota"));
        assertEquals(0, llamada(d, "CalcularParidad", 0).getAsJsonArray("p_CalculosEntrada").size());
        d = dump("custodia-precio-cero-con-escala");
        assertEquals("0|0.0", campos(fila(d), "Monto", "ValorCuota"));
        assertEquals(2, llamada(d, "CalcularParidad", 0).getAsJsonArray("p_CalculosEntrada").size());
    }

    private static JsonObject fila(JsonObject d) {
        JsonArray f = filas(d);
        assertEquals(1, f.size());
        return f.get(0).getAsJsonObject();
    }

    // ---- FM-CPF-12 guarantee and rescate in transit
    @Test
    @DisplayName("FM-CPF-12 CPF-C05 D-F3A-03: garantia 5: se pide con IdInterno*-2 (-2) y se busca con IdInterno: MontoGarantia SIEMPRE 0; MontoGarantiaMonedaSistema = 5 x 1500,25 = 7501; garantia 0 no se valoriza; negativa si")
    void fmCpf12_garantia() throws Exception {
        JsonObject d = dump("garantia-distinta-de-cero");
        assertEquals("5|0|7501|1500.25|150025", campos(fila(d), "CantidadGarantia", "MontoGarantia", "MontoGarantiaMonedaSistema", "ValorCuotaMonedaSistema", "MontoMonedaSistema"));
        assertEquals(List.of(-2L), idsDePrecios(d, 1));
        assertEquals(List.of("ConsultarCustodiaPortafolioFFMM", "EntregarPreciosTasas", "CalcularParidad", "EntregarPreciosTasas", "CalcularParidad", "ConsultarEmpresa", "CalcularParidad"), colaboradores(d));
        assertEquals(List.of("ConsultarCustodiaPortafolioFFMM", "EntregarPreciosTasas", "CalcularParidad", "ConsultarEmpresa", "CalcularParidad"), colaboradores(dump("garantia-cero-no-se-valoriza")));
        assertEquals("-2|0|-3001", campos(fila("garantia-negativa"), "CantidadGarantia", "MontoGarantia", "MontoGarantiaMonedaSistema"));
        assertEquals("4|0|6001", campos(fila("garantia-con-moneda-sistema"), "CantidadGarantia", "MontoGarantia", "MontoGarantiaMonedaSistema"));
        assertEquals("2|0|1800|900", campos(fila("garantia-fondo-en-dolares"), "CantidadGarantia", "MontoGarantia", "MontoGarantiaMonedaSistema", "ValorCuotaMonedaSistema"));
        for (JsonElement r : filas("garantia-dos-saldos-solo-uno")) {
            JsonObject o = r.getAsJsonObject();
            assertEquals("FM001A".equals(v(o, "IdSerieFondo")) ? "3|0|3" : "0|0|0", campos(o, "CantidadGarantia", "MontoGarantia", "MontoGarantiaMonedaSistema"));
        }
    }

    @Test
    @DisplayName("FM-CPF-12 D-F3A-05: rescate en transito (8,5 cuotas): se valoriza con CalcularMontoNumeroCuota (id -1) y MontoTransSolicitudesRescate = MontoMonedaSistema del resultado (12751); sin precio queda lo de la valorizacion de solicitudes")
    void fmCpf12_rescateEnTransito() throws Exception {
        JsonObject d = dump("transito-rescate-valorizado");
        assertEquals("8.5000|12751", campos(fila(d), "CantidadTransSolicitudesRescate", "MontoTransSolicitudesRescate"));
        assertEquals(List.of(-2L), idsDePrecios(d, 0));
        assertEquals(List.of(1L), idsDePrecios(d, 1));
        assertEquals(List.of(-1L), idsDePrecios(d, 2));
        assertEquals("8.5000|12751", campos(fila("transito-rescate-con-custodia"), "CantidadTransSolicitudesRescate", "MontoTransSolicitudesRescate"));
        assertEquals("8.50000000|5000.00000000", campos(fila("transito-rescate-sin-precio"), "CantidadTransSolicitudesRescate", "MontoTransSolicitudesRescate"));
        // the first valuation (id -2) has a price and leaves the amount in CLP (2.880.000); the ones of the saldo (-1) and of the custody have none: the amount of the first one stays
        assertEquals("4.00|2880000", campos(fila("transito-rescate-sin-precio-en-el-saldo"), "CantidadTransSolicitudesRescate", "MontoTransSolicitudesRescate"));
    }

    // ---- FM-CPF-13 value of the quota in system currency
    @Test
    @DisplayName("FM-CPF-13 CPF-C07 QD-F3A-04: con PLA.CalculaValorCuotaDiaSiguiente exactamente 'S' la paridad usa el dia habil siguiente a FechaValorCuota (viernes 03-06 con lunes 03-09 feriado => martes 03-10); si no, la fecha de la consulta")
    void fmCpf13_diaSiguiente() throws Exception {
        assertEquals(List.of("2026-03-10"), fechasDeParidad(dump("dia-siguiente-s-viernes"), 1));
        assertEquals(List.of("2026-03-04"), fechasDeParidad(dump("dia-siguiente-s-habil"), 1));
        assertEquals(List.of("2026-03-02"), fechasDeParidad(dump("dia-siguiente-n"), 1));
        assertEquals(List.of("2026-03-02"), fechasDeParidad(dump("dia-siguiente-minuscula"), 1));
        assertEquals(List.of("2026-03-07"), fechasDeParidad(dump("dia-siguiente-s-pais-sin-feriados"), 1));
        JsonObject usd = llamada(dump("dia-siguiente-s-fondo-en-dolares"), "CalcularParidad", 1).getAsJsonArray("p_CalculosEntrada").get(0).getAsJsonObject();
        assertEquals("1|2026-03-10|USD|CLP|8", usd.get("IdInterno").getAsString() + "|" + usd.get("Fecha").getAsString() + "|" + usd.get("IdMonedaOrigen").getAsString() + "|" + usd.get("IdMonedaDestino").getAsString()
                + "|" + usd.get("MontoOrigen").getAsString());
        List<String> dos = fechasDeParidad(dump("dia-siguiente-s-dos-fechas"), 1);
        assertEquals(2, dos.size());
        assertTrue(dos.contains("2026-03-10") && dos.contains("2026-03-04"), dos.toString());
    }

    @Test
    @DisplayName("D-F3A-29 (candidato) FM-CPF-13: con 'S' y un saldo sin valorizar (FechaValorCuota 0001-01-01) la paridad usa 0001-01-03 (calendario juliano del legacy: el 0001-01-01 es sabado); el puerto de ObtenerDiaSiguiente da 0001-01-02 (proleptico)")
    void dF3A29_diaSiguienteDelAnio1() throws Exception {
        assertEquals(List.of("0001-01-03"), fechasDeParidad(dump("dia-siguiente-s-sin-valorizar"), 0));
    }

    @Test
    @DisplayName("D-F3A-07 FM-CPF-13: ConsultarEmpresa y ObtenerDiaSiguiente se ejecutan SIEMPRE, aun con el parametro 'N': si la empresa falla, falla todo; sin IdPais el resultado no cambia")
    void dF3A07_empresaSiempreSeConsulta() throws Exception {
        assertEquals("Exception: empresa no disponible", excepcion("empresa-falla-aunque-no-use-dia-siguiente"));
        assertEquals(1, colaboradores(dump("dia-siguiente-n")).stream().filter("ConsultarEmpresa"::equals).count());
        assertEquals(1, filas("empresa-sin-pais").size());
    }

    @Test
    @DisplayName("FM-CPF-13: ValorCuotaMonedaSistema = MontoDestino de la paridad (sin respuesta o MontoDestino nulo = 0); MontoMonedaSistema y MontoGarantiaMonedaSistema = cantidad x valor, con el formato de la moneda sistema (CLP: 0 decimales HALF_UP; 'T' trunca)")
    void fmCpf13_valorCuotaMonedaSistema() throws Exception {
        assertEquals("1500.25|15003", campos(fila("paridad-valor-cuota-moneda-sistema"), "ValorCuotaMonedaSistema", "MontoMonedaSistema"));
        assertEquals("1500.35|15004", campos(fila("paridad-redondeo-mitad"), "ValorCuotaMonedaSistema", "MontoMonedaSistema"));
        assertEquals("15002", v(fila("moneda-sistema-trunca"), "MontoMonedaSistema"));
        assertEquals("0|0", campos(fila("paridad-sin-respuesta"), "ValorCuotaMonedaSistema", "MontoMonedaSistema"));
        assertEquals("0|0", campos(fila("paridad-monto-destino-nulo"), "ValorCuotaMonedaSistema", "MontoMonedaSistema"));
        assertEquals("900|9000", campos(fila("fondo-en-dolares-paridad"), "ValorCuotaMonedaSistema", "MontoMonedaSistema"));
        assertEquals("15002.50|0.00", campos(fila("moneda-sistema-sin-formato"), "MontoMonedaSistema", "MontoGarantiaMonedaSistema"));
        assertEquals("paridad no disponible", excepcion("paridad-falla").substring("Exception: ".length()));
        assertTrue(excepcion("paridad-id-repetido").startsWith("IllegalStateException:"));
    }

    // ---- FM-CPF-14 order
    @Test
    @DisplayName("FM-CPF-14 CPF-C11 D-F3A-04: el orden de P_OUT_TBL es el de iteracion de un HashMap<String,..> de claves cliente_portafolio_instrumento_regimen (estable para las mismas claves); 6 claves")
    void fmCpf14_ordenDelHashMap() throws Exception {
        assertEquals(List.of("FM001A", "FM004C", "FM003U", "FM002B", "FM005D", "FM006E"), seriesEnOrden("orden-seis-saldos"));
        assertEquals(List.of("100/500/FM001A/GEN", "102/502/FM006E/18Q", "101/501/FM005D/BF", "101/501/FM002B/BF", "100/500/FM004C/GEN", "102/502/FM003U/18Q"), claves("orden-claves-mixtas"));
        assertEquals(List.of("100/500/FM001A/GEN", "100/500/FM005D/GEN", "100/500/FM002B/GEN", "100/500/FM006E/GEN"), claves("orden-insercion-solicitudes-primero"));
    }

    @Test
    @DisplayName("FM-CPF-14 D-F3A-04: con 13 saldos el HashMap se redimensiona (umbral 12) y el orden de iteracion cambia; los IdInterno internos 1..13 siguen ese orden")
    void fmCpf14_ordenTrasRedimensionar() throws Exception {
        assertEquals(List.of("100/500/FM001A/GEN", "101/500/FM006E/GEN", "101/500/FM003U/GEN", "101/500/FM001A/GEN", "100/500/FM004C/GEN", "101/500/FM004C/GEN", "100/500/FM002B/GEN", "100/500/FM006E/GEN",
                "102/503/FM001A/GEN", "101/500/FM002B/GEN", "100/500/FM003U/GEN", "101/500/FM005D/GEN", "100/500/FM005D/GEN"), claves("orden-trece-saldos-redimensiona"));
        JsonObject d = dump("orden-trece-saldos-redimensiona");
        List<Long> ids = new ArrayList<>();
        for (JsonElement x : llamada(d, "CalcularParidad", 1).getAsJsonArray("p_CalculosEntrada")) ids.add(x.getAsJsonObject().get("IdInterno").getAsLong());
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L), ids);
    }

    private static List<String> claves(String slug) throws Exception {
        List<String> l = new ArrayList<>();
        for (JsonElement f : filas(slug)) {
            JsonObject o = f.getAsJsonObject();
            l.add(v(o, "IdCliente") + "/" + v(o, "IdPortafolio") + "/" + v(o, "IdSerieFondo") + "/" + v(o, "IdRegimenFondoMutuo"));
        }
        return l;
    }

    @Test
    @DisplayName("FM-CPF-14 D-F3A-04: IdInterno de custodia 1, 2, ... en el orden del HashMap (se ven en CalcularMontoNumeroCuota de la custodia y en la paridad final); solo un saldo: aporte id 2/3 y custodia id 1")
    void fmCpf14_idsInternosDeCustodia() throws Exception {
        JsonObject d = dump("orden-seis-saldos");
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L), idsDePrecios(d, 0));
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L), paridadFinal(d));
        d = dump("solo-un-saldo-ids");
        assertEquals(2, idsDePrecios(d, 0).size());
        assertEquals(List.of(1L), idsDePrecios(d, 1));
    }

    private static List<Long> paridadFinal(JsonObject d) {
        JsonArray llamadas = d.getAsJsonArray("colaboradores");
        JsonObject ultima = llamadas.get(llamadas.size() - 1).getAsJsonObject();
        assertEquals("CalcularParidad", ultima.get("colaborador").getAsString());
        List<Long> l = new ArrayList<>();
        for (JsonElement x : ultima.getAsJsonArray("argumentos").get(0).getAsJsonObject().getAsJsonArray("p_CalculosEntrada")) l.add(x.getAsJsonObject().get("IdInterno").getAsLong());
        return l;
    }

    // ---- read only and RULE-076
    @Test
    @DisplayName("FM-CPF read only: ningun caso modifica tablas, deja tablas de sesion, commits autonomos ni secuencias")
    void soloLectura() throws Exception {
        for (String slug : new String[] { "transito-sin-custodia", "rescate-total-con-custodia-y-aporte", "orden-seis-saldos", "garantia-distinta-de-cero" }) {
            JsonObject d = dump(slug);
            JsonObject golden = CasoCpf.golden(CasoCpf.idPorSlug(slug));
            assertEquals(golden.get("tablas").toString(), d.get("tablas").toString(), slug);
            assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop").size(), slug);
            assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("commitsAutonomos").size(), slug);
            assertEquals(0, d.getAsJsonArray("secuenciasSolicitadas").size(), slug);
        }
        assertNotNull(CasoCpf.golden(CasoCpf.idPorSlug("custodia-sola")).get("tablas"));
    }

    @Test
    @DisplayName("RULE-076: ni el main() del legacy ni su conexion DESA ni el id real de cliente que trae se copiaron (src/main y los fixtures de fondomutuo-cpf)")
    void rule076_sinElMainNiLosIdsDelLegacy() throws Exception {
        // legacy ConsultarCustodiaPortafolioFondoMutuo.main (CPF:1129-1160): PlataformaUtils.getDESAConnection() and a JSON with a real client id (4538) and a date of 2024
        List<String> culpables = new ArrayList<>();
        for (java.nio.file.Path raiz : List.of(java.nio.file.Path.of("src", "main"), java.nio.file.Path.of("src", "test", "resources", "fondomutuo-cpf"))) {
            assertTrue(java.nio.file.Files.isDirectory(raiz), "RULE-076 needs " + raiz + " (run the tests from the module directory)");
            try (var s = java.nio.file.Files.walk(raiz)) {
                for (java.nio.file.Path p : s.filter(java.nio.file.Files::isRegularFile).toList()) {
                    String t = java.nio.file.Files.readString(p, java.nio.charset.StandardCharsets.ISO_8859_1);
                    if (t.contains("getDESAConnection") || t.contains("getBOLSAConnection") || t.matches("(?s).*\"p_IdCliente\"\\s*:\\s*4538.*")) culpables.add(p.toString());
                }
            }
        }
        assertEquals(List.of(), culpables);
    }
}
