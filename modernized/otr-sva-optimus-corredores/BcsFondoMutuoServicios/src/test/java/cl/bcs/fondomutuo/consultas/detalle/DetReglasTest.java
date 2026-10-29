package cl.bcs.fondomutuo.consultas.detalle;

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

import cl.bcs.fondomutuo.consultas.detalle.CasoDet.Perfil;

/**
 * Rule cards of {@code ConsultarDetalleSolicitud} (RULES_FONDOMUTUO_F3_A.md 8.6, FM-DET-01..04; defects D-F3A-24, D-F3A-25, D-NOR-9 and the D-CUO-* of the valuation; matrix
 * DET-C01..C04), stated with LITERAL values. The expected values are the ones the unmodified legacy (DET + ConsultarSolicitudAporteRescateUI + CalcularMontoNumeroCuota)
 * produced (they equal the recorded goldens: this class and {@link DetEquivalenciaTest} must agree; if the legacy is re-recorded and a literal changes, the legacy won: update the
 * literal and say so). Each test runs the NEW code on the H2 of its case (slug of {@code cases/FMM-DET-nnn_slug}) and names the card or defect it pins.
 * Universe: see {@code cases_det_lib.py} / {@code cases_det_a.py} (default solicitud: APO, ING, by amount 150000 CLP, FM001A, portfolio 500; clock 2026-03-02; the price of
 * EntregarPreciosTasas is 1500.1234; the quota decimals of FM001A are 4).
 */
class DetReglasTest {

    private static final String VACIA = "{\"P_OUT_RESULTADO\":[],\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[]}";

    // slug -> "Monto|NumeroCuotas|ValorCuota" (text of the numbers) of the first row of P_OUT_RESULTADO
        private static final Map<String, String> VALORIZACION = Map.ofEntries(
            Map.entry("ing-por-monto", "150000|99.9918|1500.1234"),
            Map.entry("ing-por-cuotas", "15751|10.5000|1500.1234"),
            Map.entry("ing-rescate-total", "38253|25.5000|1500.1234"),
            Map.entry("ing-por-porcentaje", "500000|333.3059|1500.1234"),
            Map.entry("estado-ing-valorizada", "123456|82.2972|1500.1234"),
            Map.entry("estado-pen-valorizada", "123456|82.2972|1500.1234"),
            Map.entry("ing-moneda-distinta-diferido", "135000000|89992.5966|1500.1234"),
            Map.entry("ing-sin-respuesta-de-paridad", "135000000|89992.5966|1500.1234"),
            Map.entry("ing-moneda-eur-por-paridad", "151500000|100991.6918|1500.1234"),
            Map.entry("ing-moneda-eur-sin-paridad", "0|0.00000|1500.1234"),
            Map.entry("ing-fondo-en-dolares", "1578.95|50.13|31.5"),
            Map.entry("ing-fondo-en-dolares-rescate", "1595.74|50.66|31.5"),
            Map.entry("ing-usa-dolar-observado-s", "142650000|95092.1771|1500.1234"),
            Map.entry("ing-sin-serie-en-valor-cuota", "150000|99.9918|1500.1234"),
            Map.entry("ing-con-id-de-solicitud-grande", "150000|99.9918|1500.1234"),
            Map.entry("valorizacion-llama-a-los-colaboradores", "150000|99.9918|1500.1234"),
            Map.entry("dos-solicitudes-solo-la-pedida", "150000|99.9918|1500.1234"),
            Map.entry("estado-CER-sin-valorizar", "123456.00000000|7.00000000|1400.50000000"),
            Map.entry("estado-ing-sin-valorizar", "123456.00000000|7.00000000|1400.50000000"),
            Map.entry("monto-decimales-de-la-tabla", "123456.12345678|88.12345678|1400.50000000")
    );

    // slug -> MontoRetencion text of the first row ('<absent>' = the field is not written)
        private static final Map<String, String> RETENCION = Map.ofEntries(
            Map.entry("retencion-apv-b-15", "150000"),
            Map.entry("retencion-apv-an", "150000"),
            Map.entry("retencion-apv-a-no", "<absent>"),
            Map.entry("retencion-regimen-vacio-usa-codigo-portafolio", "150000"),
            Map.entry("retencion-portafolio-apv-a-sin-regimen", "<absent>"),
            Map.entry("retencion-portafolio-no-apv", "<absent>"),
            Map.entry("retencion-medio-cuenta-inversion", "<absent>"),
            Map.entry("retencion-medio-vacio", "150000"),
            Map.entry("retencion-medio-minuscula", "150000"),
            Map.entry("retencion-con-traspaso", "<absent>"),
            Map.entry("retencion-tipo-movimiento-restrasp", "<absent>"),
            Map.entry("retencion-tipo-movimiento-minuscula", "<absent>"),
            Map.entry("retencion-aporte-no", "<absent>"),
            Map.entry("retencion-sin-parametro", "0"),
            Map.entry("retencion-porcentaje-cero", "0"),
            Map.entry("retencion-porcentaje-cien", "1000000"),
            Map.entry("retencion-porcentaje-decimal-es-cero", "0"),
            Map.entry("retencion-porcentaje-no-numerico", "0"),
            Map.entry("retencion-porcentaje-negativo", "-50000"),
            Map.entry("retencion-porcentaje-mayor-que-cien", "2500000"),
            Map.entry("retencion-redondeo-clp", "150008"),
            Map.entry("retencion-redondeo-clp-mitad", "150002"),
            Map.entry("retencion-moneda-usd", "150.08"),
            Map.entry("retencion-moneda-sin-formato", "150.0825000000"),
            Map.entry("retencion-monto-cero", "0")
    );

    private static JsonObject dump(String slug) throws Exception {
        return CasoDet.ejecutar(CasoDet.idPorSlug(slug), Perfil.ABIERTA);
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

    private static String campo(JsonObject salida, int i, String k) {
        JsonElement e = resultado(salida).get(i).getAsJsonObject().get(k);
        return e == null ? "<absent>" : e.getAsString();
    }

    private static String valores(JsonObject salida) {
        return String.join("|", campo(salida, 0, "Monto"), campo(salida, 0, "NumeroCuotas"), campo(salida, 0, "ValorCuota"));
    }

    private static List<String> colaboradores(JsonObject dump) {
        List<String> l = new ArrayList<>();
        for (JsonElement c : dump.getAsJsonArray("colaboradores")) l.add(c.getAsJsonObject().get("colaborador").getAsString());
        return l;
    }

    // ---- FM-DET-01 contract and validation
    @Test
    @DisplayName("FM-DET-01 DET-C01: id nulo, ausente o 0 => contador 1, NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE (id 0, ERROR), resultado vacio y rollback; no valida requisitos basicos ni perfil y no llama a nadie")
    void fmDet01_idNuloOCero() throws Exception {
        String esperado = "{\"P_OUT_RESULTADO\":[],\"P_OUT_ERRORESCONTADOR\":1,\"P_OUT_MENSAJES\":[{\"id\":0,\"tipo\":\"ERROR\",\"codigo\":\"NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE\","
                + "\"descripcion\":\"No se ingresaron solicitudes\"}]}";
        for (String slug : new String[] { "id-nulo", "id-ausente", "id-cero" }) {
            JsonObject d = dump(slug);
            assertEquals(esperado, d.get("salidaCruda").getAsString(), slug);
            assertEquals("ROLLBACK", d.get("decisionServlet").getAsString(), slug);
            assertEquals(0, d.getAsJsonArray("colaboradores").size(), slug);
        }
    }

    @Test
    @DisplayName("FM-DET-01: sin la entrada del diccionario el texto es '<codigo> sin traduccion'; con p_IdEmpresa nulo y id 0 el mensaje se arma igual (sin traduccion) y no hay excepcion")
    void fmDet01_mensajeSinTraduccion() throws Exception {
        String sinTraduccion = "NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE sin traduccion";
        for (String slug : new String[] { "id-cero-sin-diccionario", "id-cero-empresa-nula" }) {
            JsonObject s = salida(slug);
            assertEquals(1L, s.get("P_OUT_ERRORESCONTADOR").getAsLong(), slug);
            assertEquals(sinTraduccion, s.getAsJsonArray("P_OUT_MENSAJES").get(0).getAsJsonObject().get("descripcion").getAsString(), slug);
        }
    }

    @Test
    @DisplayName("FM-DET-01 / FM-DET-04 DET-C04: id inexistente (999), negativo (-1) u otra empresa => P_OUT_RESULTADO vacio, contador 0, sin mensaje, commit")
    void fmDet04_idInexistente() throws Exception {
        for (String slug : new String[] { "id-inexistente", "id-negativo", "otra-empresa-no-sale" }) {
            JsonObject d = dump(slug);
            assertEquals(VACIA, d.get("salidaCruda").getAsString(), slug);
            assertEquals("COMMIT", d.get("decisionServlet").getAsString(), slug);
        }
    }

    @Test
    @DisplayName("FM-DET-01: id como texto '5' lo coacciona Gson a Long 5; 'abc' y 1.5 => JsonSyntaxException; sin RevisarRequisitosBasicos (usuario 999 y modulo XXX no se validan) y usuario/modulo nulos se ignoran")
    void fmDet01_entradaJsonYSinValidaciones() throws Exception {
        assertEquals("5", campo(salida("id-texto-numerico"), 0, "IdSolicitudAporteRescate"));
        assertTrue(excepcion("id-no-numerico").startsWith("JsonSyntaxException"));
        assertTrue(excepcion("id-decimal").startsWith("JsonSyntaxException"));
        assertEquals(1, resultado(salida("sin-requisitos-basicos")).size());
        assertEquals(1, resultado(salida("usuario-y-modulo-nulos")).size());
    }

    @Test
    @DisplayName("FM-DET-02 D-NOR-9 DESVIO-KIT-1: p_IdEmpresa nulo o sin configuracion => el legacy lanza NullPointerException (ConfiguracionSistema, aun sin filas); el kit del modulo ParametroNoEncontradoException (aceptado solo aqui)")
    void fmDet02_empresaSinConfiguracion() throws Exception {
        for (String slug : new String[] { "empresa-nula-con-id", "empresa-sin-configuracion" }) {
            String e = excepcion(slug);
            assertTrue(e.startsWith("NullPointerException") || (e.startsWith("Exception: Parametro ") && e.endsWith(" no encontrado")), slug + ": " + e);
        }
    }

    @Test
    @DisplayName("FM-DET-02: CalcularMontoNumeroCuota lee UsaDolarObservado y PLA.MonedaSistema SIEMPRE, aunque la solicitud no se valorice: si faltan => Exception con el texto del legacy")
    void fmDet02_parametrosDeLaValorizacion() throws Exception {
        assertEquals("Exception: Parametro UsaDolarObservado en modulo FONDOMUTUO no encontrado", excepcion("sin-parametro-usa-dolar-observado"));
        assertEquals("Exception: Parametro PLA.MonedaSistema en modulo PLATAFORMA no encontrado", excepcion("sin-moneda-sistema"));
    }

    // ---- FM-DET-02 delegation and valuation
    @Test
    @DisplayName("FM-DET-02 DET-C02: solo ING y PEN se valoran: CER, LIQ, ANU, PRO, 'ing', 'IN ' y '' se devuelven como la vista (Monto 123456, cuotas 7, valor cuota 1400,5) y sin llamar a colaboradores")
    void fmDet02_soloIngYPenSeValorizan() throws Exception {
        for (String slug : new String[] { "estado-CER-sin-valorizar", "estado-LIQ-sin-valorizar", "estado-ANU-sin-valorizar", "estado-PRO-sin-valorizar", "estado-ing-sin-valorizar",
                "estado-IN-espacio-sin-valorizar", "estado-vacio-sin-valorizar" }) {
            JsonObject d = dump(slug);
            assertEquals("123456.00000000|7.00000000|1400.50000000", valores(d.getAsJsonObject("salida")), slug);
            assertEquals(0, d.getAsJsonArray("colaboradores").size(), slug);
        }
    }

    @Test
    @DisplayName("FM-DET-02 D-F3A-24 DET-C02: una consulta ejecuta calculos: ING y PEN se valoran con CalcularMontoNumeroCuota (precio 1500.1234): Monto 123456 (formato CLP), cuotas 82.2972, valor cuota 1500.1234; "
            + "llama a EntregarPreciosTasas y a CalcularParidad (IdTrabajo 2)")
    void fmDet02_ingYPenValorizadas() throws Exception {
        for (String slug : new String[] { "estado-ing-valorizada", "estado-pen-valorizada" }) {
            JsonObject d = dump(slug);
            assertEquals("123456|82.2972|1500.1234", valores(d.getAsJsonObject("salida")), slug);
            assertEquals(List.of("EntregarPreciosTasas", "CalcularParidad"), colaboradores(d), slug);
        }
    }

    @Test
    @DisplayName("FM-DET-02 D-F3A-24: lo que la delegada envia a los colaboradores: EntregarPreciosTasas con el pais 56, IdRegistro = id de la solicitud, FechaConsulta = fecha valor cuota 2026-03-03, IdUso CARTERA; "
            + "CalcularParidad(2) con IdInterno 6 (id*4+2: monto en UF) y 7 (id*4+3: valor de la UF)")
    void fmDet02_argumentosAColaboradores() throws Exception {
        JsonObject d = dump("valorizacion-llama-a-los-colaboradores");
        JsonArray c = d.getAsJsonArray("colaboradores");
        assertEquals(2, c.size());
        JsonObject precios = c.get(0).getAsJsonObject().getAsJsonArray("argumentos").get(0).getAsJsonObject();
        assertEquals(56L, precios.get("p_IdPais").getAsLong());
        JsonObject reg = precios.getAsJsonArray("p_DatosInstrumentos").get(0).getAsJsonObject();
        assertEquals(1L, reg.get("IdRegistro").getAsLong());
        assertEquals("2026-03-03 00:00:00.000", reg.get("FechaConsulta").getAsString());
        assertEquals("CARTERA", reg.get("IdUso").getAsString());
        assertEquals("FM001A", reg.get("Nemotecnico").getAsString());
        assertEquals("CLP", reg.get("IdMoneda").getAsString());
        JsonObject paridad = c.get(1).getAsJsonObject().getAsJsonArray("argumentos").get(0).getAsJsonObject();
        assertEquals(2L, paridad.get("p_IdTrabajo").getAsLong());
        JsonArray calculos = paridad.getAsJsonArray("p_CalculosEntrada");
        assertEquals(2, calculos.size());
        assertEquals(6L, calculos.get(0).getAsJsonObject().get("IdInterno").getAsLong());
        assertEquals("UF", calculos.get(0).getAsJsonObject().get("IdMonedaDestino").getAsString());
        assertEquals(7L, calculos.get(1).getAsJsonObject().get("IdInterno").getAsLong());
        assertEquals("UF", calculos.get(1).getAsJsonObject().get("IdMonedaOrigen").getAsString());
    }

    @Test
    @DisplayName("FM-DET-02 D-F3A-24: con solo solicitudes no valorizables CalcularMontoNumeroCuota se ejecuta igual pero sin series no llega a EntregarPreciosTasas ni a CalcularParidad (0 llamadas)")
    void fmDet02_sinSeriesNoHayLlamadas() throws Exception {
        assertEquals(0, dump("valorizacion-se-ejecuta-aunque-no-haya-ing").getAsJsonArray("colaboradores").size());
    }

    @Test
    @DisplayName("FM-DET-02 D-CUO-*: valorizacion por monto (150000 => 99.9918 cuotas HALF_DOWN), por cuotas (10,5 => Monto 15751), rescate total 'T' (25,5 => 38253) y por porcentaje 'P' (500000 => 333.3059)")
    void fmDet02_tiposDeCalculo() throws Exception {
        for (String slug : VALORIZACION.keySet()) {
            JsonObject s = salida(slug);
            assertEquals(VALORIZACION.get(slug), valores(s), slug);
        }
        assertTrue(VALORIZACION.size() >= 18, "the valuation table covers at least 18 scenarios, found " + VALORIZACION.size());
    }

    @Test
    @DisplayName("FM-DET-02 D-CUO-1: precio 0 => ArithmeticException (division por cero); sin precios del colaborador => la solicitud queda como la vista con valor cuota 0 (TO_DECIMAL(0)); precio de otro registro => Monto y cuotas enteros y valor 0")
    void fmDet02_preciosAnomalos() throws Exception {
        assertTrue(excepcion("ing-precio-cero").startsWith("ArithmeticException"));
        assertEquals("123456.00000000|7.00000000|0E-8", valores(salida("ing-sin-precio-del-colaborador")));
        assertEquals("123456|7|0", valores(salida("ing-precio-de-otro-registro")));
    }

    @Test
    @DisplayName("FM-DET-02 D-F3A-24: serie inexistente => CalcularMontoNumeroCuota responde ERROR_VALORCUOTA CON IdInterno y la delegada lo da por valido: Monto, cuotas y valor cuota quedan NULOS (no se escriben); sin llamadas a colaboradores")
    void fmDet02_serieInexistentePisaConNulos() throws Exception {
        JsonObject d = dump("ing-serie-inexistente-pisa-con-nulos");
        JsonObject s = d.getAsJsonObject("salida");
        assertEquals("<absent>|<absent>|<absent>", valores(s));
        assertEquals("ING", campo(s, 0, "EstadoSolicitud"));
        assertEquals(0, d.getAsJsonArray("colaboradores").size());
    }

    @Test
    @DisplayName("FM-DET-02: solicitud en EUR con fondo en CLP: el monto destino sale de CalcularParidad(1) (151.500.000 => 100991.6918 cuotas) y sin respuesta de la paridad el monto es 0 y las cuotas 0.00000 (HALF_UP con un decimal mas)")
    void fmDet02_monedaDeInversionDistinta() throws Exception {
        assertEquals("151500000|100991.6918|1500.1234", valores(salida("ing-moneda-eur-por-paridad")));
        assertEquals("0|0.00000|1500.1234", valores(salida("ing-moneda-eur-sin-paridad")));
        JsonObject d = dump("ing-moneda-eur-por-paridad");
        assertEquals(List.of("EntregarPreciosTasas", "CalcularParidad", "CalcularParidad"), colaboradores(d));
        JsonObject primera = d.getAsJsonArray("colaboradores").get(1).getAsJsonObject().getAsJsonArray("argumentos").get(0).getAsJsonObject();
        assertEquals(1L, primera.get("p_IdTrabajo").getAsLong());
        assertEquals("EUR", primera.getAsJsonArray("p_CalculosEntrada").get(0).getAsJsonObject().get("IdMonedaOrigen").getAsString());
    }

    @Test
    @DisplayName("FM-DET-02: CLP con fondo CLP y solicitud en USD usa el tipo de cambio de aporte 900 de la serie (135.000.000) y no la paridad; con UsaDolarObservado='S' va por la paridad (142.650.000)")
    void fmDet02_dolarObservado() throws Exception {
        assertEquals("135000000|89992.5966|1500.1234", valores(salida("ing-moneda-distinta-diferido")));
        assertEquals("135000000|89992.5966|1500.1234", valores(salida("ing-sin-respuesta-de-paridad")));
        assertEquals("142650000|95092.1771|1500.1234", valores(salida("ing-usa-dolar-observado-s")));
    }

    @Test
    @DisplayName("FM-DET-02: fondo en USD (2 decimales de cuota, tipos de cambio 950 / 940): aporte => 1578.95 y 50.13 cuotas; rescate => 1595.74 y 50.66; precio 31.5")
    void fmDet02_fondoEnDolares() throws Exception {
        assertEquals("1578.95|50.13|31.5", valores(salida("ing-fondo-en-dolares")));
        assertEquals("1595.74|50.66|31.5", valores(salida("ing-fondo-en-dolares-rescate")));
    }

    @Test
    @DisplayName("FM-DET-02: se devuelve solo la solicitud pedida (id 2 de 3), con id de 12 digitos se conserva el Long, y el precio no se lee de FMM.ValorCuota (la tabla vacia no cambia el resultado)")
    void fmDet02_soloLaPedidaYSinLeerLaTablaDeValorCuota() throws Exception {
        JsonObject s = salida("dos-solicitudes-solo-la-pedida");
        assertEquals(1, resultado(s).size());
        assertEquals("2", campo(s, 0, "IdSolicitudAporteRescate"));
        assertEquals("123456789012", campo(salida("ing-con-id-de-solicitud-grande"), 0, "IdSolicitudAporteRescate"));
        assertEquals("150000|99.9918|1500.1234", valores(salida("ing-sin-serie-en-valor-cuota")));
    }

    @Test
    @DisplayName("FM-DET-02 D-F3A-24: ING aporte sin Spot: MontoMonedaSistema (777 de la tabla) se sobrescribe con el calculado (150000) junto con CostoMonedaLocal 150000, ParidadFechaCertificacion 1, CostoUF 3.85 y "
            + "ValorUFFechaInversion 39000; con Spot (IdOperacionSpot 300) MontoMonedaSistema queda 777.00000000; un rescate siempre se sobrescribe (15001)")
    void fmDet02_montoMonedaSistemaYValoresDerivados() throws Exception {
        JsonObject a = salida("ing-monto-moneda-sistema-aporte");
        assertEquals("150000", campo(a, 0, "MontoMonedaSistema"));
        assertEquals("150000", campo(a, 0, "CostoMonedaLocal"));
        assertEquals("1", campo(a, 0, "ParidadFechaCertificacion"));
        assertEquals("3.85", campo(a, 0, "CostoUF"));
        assertEquals("39000", campo(a, 0, "ValorUFFechaInversion"));
        JsonObject spot = salida("ing-monto-moneda-sistema-aporte-con-spot");
        assertEquals("777.00000000", campo(spot, 0, "MontoMonedaSistema"));
        assertEquals("150000", campo(spot, 0, "CostoMonedaLocal"));
        JsonObject res = salida("ing-monto-moneda-sistema-rescate");
        assertEquals("15001", campo(res, 0, "MontoMonedaSistema"));
        assertEquals("15001|10.0000|1500.1234", valores(res));
    }

    // ---- FM-DET-03 APV retention
    @Test
    @DisplayName("FM-DET-03 DET-C03: MontoRetencion = Monto * porcentaje / 100 solo si IdTipoMovimientoFondoMutuo = RESNORM, regimen APV-AN / APV-B (o el codigo del portafolio APV si el regimen esta vacio), "
            + "sin orden de traspaso y medio de pago distinto de 'CIN' exacto; porcentaje de APV.PorcentajeRetencion (Long: '12.5' y 'abc' => 0; sin parametro => 0); formato de la moneda; sin retencion el campo no se escribe")
    void fmDet03_retencion() throws Exception {
        for (Map.Entry<String, String> e : RETENCION.entrySet()) {
            assertEquals(e.getValue(), campo(salida(e.getKey()), 0, "MontoRetencion"), e.getKey());
        }
        assertTrue(RETENCION.size() >= 25, "the retention table covers at least 25 scenarios, found " + RETENCION.size());
    }

    @Test
    @DisplayName("FM-DET-03 DET-C03: rescate normal APV-B, Monto 1.000.000 y porcentaje 15 => MontoRetencion 150000 (Monto sigue con su escala de la tabla)")
    void fmDet03_ejemploDeLaTarjeta() throws Exception {
        JsonObject s = salida("retencion-apv-b-15");
        assertEquals("150000", campo(s, 0, "MontoRetencion"));
        assertEquals("1000000.00000000", campo(s, 0, "Monto"));
        assertEquals("CER", campo(s, 0, "EstadoSolicitud"));
    }

    @Test
    @DisplayName("FM-DET-03 / FM-DET-02: ING APV-B por cuotas 100 se valoriza primero (Monto 150012) y la retencion se calcula sobre ese Monto: 22502 (15 % redondeado por el formato de CLP)")
    void fmDet03_retencionSobreMontoValorizado() throws Exception {
        JsonObject s = salida("retencion-sobre-monto-valorizado");
        assertEquals("150012|100.0000|1500.1234", valores(s));
        assertEquals("22502", campo(s, 0, "MontoRetencion"));
    }

    @Test
    @DisplayName("FM-DET-03 D-F3A-24 D-NOR-9: ING APV-B con serie inexistente: la valorizacion deja Monto NULO y la retencion lanza NullPointerException (solicitud.Monto.multiply); sin catch")
    void fmDet03_montoNuloEsNpe() throws Exception {
        assertTrue(excepcion("retencion-monto-nulo-por-error-de-valorizacion").startsWith("NullPointerException"));
    }

    @Test
    @DisplayName("FM-DET-03 D-F3A-24: una orden de traspaso (IdTraspaso no nulo) evita la retencion; DOS ordenes repiten la solicitud (2 filas, ninguna con retencion)")
    void fmDet03_traspaso() throws Exception {
        assertEquals("<absent>", campo(salida("retencion-con-traspaso"), 0, "MontoRetencion"));
        JsonObject dos = salida("retencion-dos-solicitudes-por-traspaso");
        assertEquals(2, resultado(dos).size());
        assertEquals("<absent>", campo(dos, 0, "MontoRetencion"));
        assertEquals("<absent>", campo(dos, 1, "MontoRetencion"));
    }

    // ---- FM-DET-04 output mapping
    @Test
    @DisplayName("FM-DET-04 D-F3A-25: 93 campos por fila en el orden del pojo de salida (IdSolicitudAporteRescate ... IdAgrupador); nada se transforma; new Gson() plano: java.sql.Date como 'MMM d, yyyy', Timestamp como "
            + "'MMM d, yyyy, h:mm:ss a' con U+202F (JDK 20+), los nulos no se escriben")
    void fmDet04_camposYFormatos() throws Exception {
        JsonObject s = salida("campos-completos");
        List<String> claves = new ArrayList<>(resultado(s).get(0).getAsJsonObject().keySet());
        assertEquals(93, claves.size());
        assertEquals("IdSolicitudAporteRescate", claves.get(0));
        assertEquals("FechaEmision", claves.get(3));
        assertEquals("FechaIngresoRegistro", claves.get(4));
        assertEquals("Monto", claves.get(18));
        assertEquals("IdOperacionSpot", claves.get(87));
        assertEquals("IdAgrupador", claves.get(92));
        assertEquals("Mar 2, 2026", campo(s, 0, "FechaEmision"));
        assertEquals("Mar 2, 2026, 9:45:07 PM", campo(s, 0, "FechaIngresoRegistro"));
        assertEquals("Mar 4, 2026", campo(s, 0, "FechaCertificacion"));
        assertEquals("Mar 5, 2026", campo(s, 0, "FechaLiquidacion"));
        assertEquals("Certificado", campo(s, 0, "NombreEstado"));
        assertEquals("FM001", campo(s, 0, "NombreFondo"));
        assertEquals("Fondo Ficticio Uno", campo(s, 0, "NombreFondoReal"));
        assertEquals("Serie A ficticia", campo(s, 0, "NombreSerieFondo"));
        assertEquals("11111111-1", campo(s, 0, "DocumentoIdentidad"));
        assertEquals("Transferencia", campo(s, 0, "TextoPreferenciaPago"));
        assertEquals("usr07", campo(s, 0, "CodigoUsuario"));
        assertEquals("<absent>", campo(s, 0, "IdOmnibus"));
        assertEquals("<absent>", campo(s, 0, "MontoRetencion"));
    }

    @Test
    @DisplayName("FM-DET-04: fechas sin dato (DATE 0001-01-01) salen como 'Jan 3, 0001' (calendario gregoriano/juliano de java.sql.Date); fechas reales como 'Jan 5, 2026' y Timestamp 'Jan 5, 2026, 10:11:12 AM'")
    void fmDet04_fechas() throws Exception {
        assertEquals("Jan 3, 0001", campo(salida("fecha-por-defecto-0001"), 0, "FechaAnulacion"));
        JsonObject s = salida("fechas-de-la-solicitud");
        assertEquals("Jan 5, 2026", campo(s, 0, "FechaEmision"));
        assertEquals("Jan 7, 2026", campo(s, 0, "FechaCertificacion"));
        assertEquals("Jan 8, 2026", campo(s, 0, "FechaLiquidacion"));
        assertEquals("Jan 5, 2026", campo(s, 0, "FechaAceptacion"));
        assertEquals("Jan 9, 2026", campo(s, 0, "FechaAnulacion"));
        assertEquals("Jan 5, 2026, 10:11:12 AM", campo(s, 0, "FechaAceptacionRegistro"));
    }

    @Test
    @DisplayName("FM-DET-04: Spot: IdOperacionSpot 300 con su operacion (CNF / PRO / CPUSD, liquidacion 2026-03-06) => 'Confirmado', 'Procesado', 'CPUSD', 'Compra dolar', 'Mar 6, 2026'; operacion MOD o ausente => nombres '' y tipo ''")
    void fmDet04_spot() throws Exception {
        JsonObject s = salida("spot-completo");
        assertEquals("300", campo(s, 0, "IdOperacionSpot"));
        assertEquals("Confirmado", campo(s, 0, "NombreEstadoSpot"));
        assertEquals("Procesado", campo(s, 0, "NombreEstadoConfirmacionSpot"));
        assertEquals("Mar 6, 2026", campo(s, 0, "FechaLiquidacionSpot"));
        assertEquals("CPUSD", campo(s, 0, "IdTipoOperacionSpot"));
        assertEquals("Compra dolar", campo(s, 0, "NombreTipoOperacionSpot"));
        for (String slug : new String[] { "spot-estado-mod-no-se-une", "spot-sin-operacion" }) {
            JsonObject n = salida(slug);
            assertEquals("", campo(n, 0, "NombreEstadoSpot"), slug);
            assertEquals("", campo(n, 0, "NombreEstadoConfirmacionSpot"), slug);
            assertEquals("<absent>", campo(n, 0, "FechaLiquidacionSpot"), slug);
        }
        JsonObject sin = salida("campos-nulos-se-omiten");
        assertEquals("", campo(sin, 0, "NombreEstadoSpot"));
        assertEquals("", campo(sin, 0, "IdTipoOperacionSpot"));
        assertEquals("", campo(sin, 0, "NombreTipoOperacionSpot"));
    }

    @Test
    @DisplayName("FM-DET-04 D-F3A-24: dos operaciones Spot con el mismo origen duplican la solicitud (2 filas); si la solicitud esta ING el toMap de la valorizacion lanza IllegalStateException (clave repetida)")
    void fmDet04_duplicadosPorJoin() throws Exception {
        assertEquals(2, resultado(salida("spot-dos-operaciones-duplican")).size());
        assertTrue(excepcion("duplicada-por-join-ing-falla").startsWith("IllegalStateException"));
    }

    @Test
    @DisplayName("FM-DET-04: omnibus 9001 => IdOmnibus 77 (inexistente => no se escribe); IdAgrupador 55 de la solicitud; cliente o portafolio sin fila (LEFT JOIN) => datos del cliente nulos")
    void fmDet04_omnibusAgrupadorYLeftJoins() throws Exception {
        JsonObject o = salida("omnibus-y-agrupador");
        assertEquals("77", campo(o, 0, "IdOmnibus"));
        assertEquals("55", campo(o, 0, "IdAgrupador"));
        assertEquals("<absent>", campo(salida("omnibus-inexistente"), 0, "IdOmnibus"));
        JsonObject c = salida("cliente-sin-fila-en-la-vista");
        assertEquals("<absent>", campo(c, 0, "DocumentoIdentidad"));
        assertEquals("Portafolio sin cliente", campo(c, 0, "DescripcionPortafolio"));
        JsonObject p = salida("portafolio-inexistente");
        assertEquals("<absent>", campo(p, 0, "DescripcionPortafolio"));
        assertEquals("999", campo(p, 0, "IdPortafolio"));
        JsonObject f = salida("serie-sin-fondo");
        assertEquals("<absent>", campo(f, 0, "NombreFondo"));
        assertEquals("904", campo(f, 0, "IdInstrumento"));
        assertEquals("<absent>", campo(salida("tipo-movimiento-sin-fila"), 0, "DescripcionTipoMovimiento"));
        assertEquals("<absent>", campo(salida("estado-sin-fila"), 0, "NombreEstado"));
    }

    @Test
    @DisplayName("FM-DET-04: CalculoMontoCuotaUI / CalculoPorMontoCuotaUI segun CalculoPorMontoCuota: C => CUOTA / UI_NUMERO_CUOTAS, M => MONTO / UI_MONTO, T => TOTAL / UI_TOTAL, P => '' / UI_PORCENTAJE")
    void fmDet04_calculoUi() throws Exception {
        Map<String, String> esperado = Map.of("calculo-por-monto-cuota-ui", "CUOTA|UI_NUMERO_CUOTAS", "calculo-por-monto-ui", "MONTO|UI_MONTO", "calculo-por-total-ui", "TOTAL|UI_TOTAL",
                "calculo-por-porcentaje-ui", "|UI_PORCENTAJE");
        for (Map.Entry<String, String> e : esperado.entrySet()) {
            JsonObject s = salida(e.getKey());
            assertEquals(e.getValue(), campo(s, 0, "CalculoMontoCuotaUI") + "|" + campo(s, 0, "CalculoPorMontoCuotaUI"), e.getKey());
        }
        JsonObject r = salida("regimen-tributario-apv-segun-proposito");
        assertEquals("APV-B", campo(r, 0, "IdRegimenTributario"));
        assertEquals("Portafolio APV B", campo(r, 0, "DescripcionPortafolio"));
    }

    @Test
    @DisplayName("FM-DET-04: Monto, cuotas y valor cuota de una solicitud no valorizada salen con los 8 decimales de la tabla (123456.12345678 / 88.12345678 / 1400.50000000)")
    void fmDet04_decimalesDeLaTabla() throws Exception {
        assertEquals("123456.12345678|88.12345678|1400.50000000", valores(salida("monto-decimales-de-la-tabla")));
    }

    @Test
    @DisplayName("FM-DET-04: es de solo lectura: ni tablas locales de sesion, ni commits autonomos, ni secuencias; las tablas (solicitudes, valor cuota, configuracion) vuelven intactas")
    void fmDet04_soloLectura() throws Exception {
        JsonObject d = dump("determinismo-solicitud-completa");
        assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop").size());
        assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("commitsAutonomos").size());
        assertEquals(0, d.getAsJsonArray("secuenciasSolicitadas").size());
        assertEquals(2, d.getAsJsonObject("tablas").getAsJsonArray("BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate").size());
        assertEquals("150000|99.9918|1500.1234", valores(d.getAsJsonObject("salida")));
        assertEquals("COMMIT", d.get("decisionServlet").getAsString());
    }

    // ---- RULE-076
    @Test
    @DisplayName("RULE-076: ni el main() del legacy ni sus ids reales ni su conexion se copiaron (src/main y los fixtures de fondomutuo-det)")
    void rule076_sinElMainNiLosIdsDelLegacy() throws Exception {
        // legacy ConsultarDetalleSolicitud.main: PlataformaUtils.getDESAConnection() and a JSON with a real solicitud id (the number below); the delegate main uses getDESAConnection too
        List<String> culpables = new ArrayList<>();
        for (java.nio.file.Path raiz : List.of(java.nio.file.Path.of("src", "main"), java.nio.file.Path.of("src", "test", "resources", "fondomutuo-det"))) {
            assertTrue(java.nio.file.Files.isDirectory(raiz), "RULE-076 needs " + raiz + " (run the tests from the module directory)");
            try (var s = java.nio.file.Files.walk(raiz)) {
                for (java.nio.file.Path p : s.filter(java.nio.file.Files::isRegularFile).toList()) {
                    String t = java.nio.file.Files.readString(p, java.nio.charset.StandardCharsets.ISO_8859_1);
                    if (t.contains("399131") || t.contains("getDESAConnection") || t.contains("getPRESURAConnection")) culpables.add(p.toString());
                }
            }
        }
        assertEquals(List.of(), culpables);
    }
}
