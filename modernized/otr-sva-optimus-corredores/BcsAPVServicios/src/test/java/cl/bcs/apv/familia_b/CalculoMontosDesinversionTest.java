package cl.bcs.apv.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.apv.familia_b.support.Caso;
import cl.bcs.apv.familia_b.support.Equivalencia;
import cl.bcs.apv.familia_b.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.APV.CalculoMontosDesinversion} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code CalcularMontosDesinversion} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class CalculoMontosDesinversionTest {

    static final List<String> CASOS = List.of(
            "APV-B-101_sin-custodias-informa-error-con-contador-cero",
            "APV-B-102_custodia-con-monto-mercado-cero-corta-con-error",
            "APV-B-103_custodia-sin-monto-mercado-informa-error-nulo",
            "APV-B-104_moneda-de-sistema-sin-configurar-lanza-excepcion",
            "APV-B-105_fecha-de-sistema-sin-configurar-lanza-excepcion",
            "APV-B-106_indicador-de-redondeo-sin-configurar-informa-error",
            "APV-B-107_indicador-de-redondeo-desconocido-informa-error-nulo",
            "APV-B-108_precios-duplicados-lanzan-excepcion",
            "APV-B-109_precio-positivo-reemplaza-y-precio-cero-conserva",
            "APV-B-110_renta-variable-simultanea-no-pide-precio",
            "APV-B-111_fondo-mutuo-y-renta-fija-no-piden-precio",
            "APV-B-112_total-mayor-al-patrimonio-desinvierte-todo",
            "APV-B-113_total-igual-al-patrimonio-no-acumula-monto-total",
            "APV-B-114_porcentaje-redondeo-hacia-arriba-rv-falla-con-monto-clp-nulo",
            "APV-B-115_porcentaje-redondeo-hacia-abajo-fm-falla-con-monto-clp-nulo",
            "APV-B-116_porcentaje-redondeo-half-up-rv-falla-con-monto-clp-nulo",
            "APV-B-117_porcentaje-half-down-renta-variable-convierte-a-pesos",
            "APV-B-118_porcentaje-half-down-fondo-mutuo-usa-decimales-de-la-serie",
            "APV-B-119_porcentaje-half-down-fondo-mutuo-sin-serie-informa-error",
            "APV-B-120_porcentaje-half-down-fm-en-dolares-convierte-a-pesos",
            "APV-B-121_porcentaje-menor-a-uno-se-trata-como-monto-total",
            "APV-B-122_porcentaje-dos-custodias-suma-montos",
            "APV-B-123_monto-total-calcula-pct-y-pide-paridad-en-moneda-extranjera",
            "APV-B-124_monto-total-con-id-registro-duplicado-informa-error",
            "APV-B-125_monto-total-paridad-con-errores-se-ignora",
            "APV-B-126_monto-total-sin-redondeo-conocido-falla-con-monto-clp-nulo",
            "APV-B-127_traspaso-por-monto-renta-variable-calcula-cantidad-entera",
            "APV-B-128_traspaso-por-monto-fondo-mutuo-usa-decimales-de-la-serie",
            "APV-B-129_traspaso-por-uf-nunca-se-lee-y-falla-con-monto-nulo",
            "APV-B-130_traspaso-por-porcentaje-renta-variable-convierte-a-pesos",
            "APV-B-131_traspaso-por-porcentaje-fm-usa-decimales-de-la-serie",
            "APV-B-132_traspaso-sin-detalle-que-coincida-informa-error-nulo",
            "APV-B-133_traspaso-con-detalle-en-cero-informa-error-nulo",
            "APV-B-134_traspaso-sin-detalles-informa-error-nulo",
            "APV-B-135_traspaso-monto-prevalece-sobre-uf-y-porcentaje",
            "APV-B-136_traspaso-dos-custodias-suman-montos",
            "APV-B-137_traspaso-fondo-mutuo-sin-serie-informa-error-de-indice");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CalculoMontosDesinversion");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CalculoMontosDesinversion.java:568-575: no custodies: ERROR 'No hay Custodias para calcular'; the type is CALCULO (not ERROR) so call() counts 0 errors; ConsultarEmpresa is still asked (line 298)
    @Test
    void apvB101_sinCustodiasInformaErrorConContadorCero() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // CalculoMontosDesinversion.java:357-366: a custody with MontoMercadoCustodia 0 ends the calculation with an error message (counter reported as 0, type CALCULO)
    @Test
    void apvB102_custodiaConMontoMercadoCeroCortaConError() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // CalculoMontosDesinversion.java:357,560-567: MontoMercadoCustodia null: NullPointerException caught, message ERROR with the exception text
    @Test
    void apvB103_custodiaSinMontoMercadoInformaErrorNulo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // CalculoMontosDesinversion.java:296: PLATAFORMA/PLA.MonedaSistema missing: Exception before any collaborator is called
    @Test
    void apvB104_monedaDeSistemaSinConfigurarLanzaExcepcion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // CalculoMontosDesinversion.java:300-301: PLATAFORMA/PLA.FechaSistema missing: Exception after ConsultarEmpresa
    @Test
    void apvB105_fechaDeSistemaSinConfigurarLanzaExcepcion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // CalculoMontosDesinversion.java:351,560-567: APV.IndicadorRedondeoDesinversion missing: the Exception is caught inside the try and reported as ERROR text
    @Test
    void apvB106_indicadorDeRedondeoSinConfigurarInformaError() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }

    // CalculoMontosDesinversion.java:489-541,543-544: an unknown rounding indicator matches no case, MontoADesinvertirCLP stays null: NullPointerException caught (partial output: only P_OUT_PORCENTAJE)
    @Test
    void apvB107_indicadorDeRedondeoDesconocidoInformaErrorNulo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(6));
    }

    // CalculoMontosDesinversion.java:333,Utils.List2Hash: two prices with the same IdRegistro: Exception('key field is not unique') escapes (outside the try)
    @Test
    void apvB108_preciosDuplicadosLanzanExcepcion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(7));
    }

    // CalculoMontosDesinversion.java:335-342: a price > 0 replaces PrecioMercadoCustodia, a price 0 keeps the custody's own price
    @Test
    void apvB109_precioPositivoReemplazaYPrecioCeroConserva() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(8));
    }

    // CalculoMontosDesinversion.java:313: RV of type SIM is not sent to EntregarPreciosTasas
    @Test
    void apvB110_rentaVariableSimultaneaNoPidePrecio() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(9));
    }

    // CalculoMontosDesinversion.java:313: only RV custodies (not FM, not RF) are sent to EntregarPreciosTasas
    @Test
    void apvB111_fondoMutuoYRentaFijaNoPidenPrecio() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(10));
    }

    // CalculoMontosDesinversion.java:472-480,549-551: total >= patrimony: everything is divested; MontoTotal is NOT accumulated in this branch (0); percentage = total/patrimony*100 from a double
    @Test
    void apvB112_totalMayorAlPatrimonioDesinvierteTodo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(11));
    }

    // CalculoMontosDesinversion.java:472: total equal to the patrimony counts as 'everything' (>=): percentage 100, MontoTotal 0
    @Test
    void apvB113_totalIgualAlPatrimonioNoAcumulaMontoTotal() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(12));
    }

    // CalculoMontosDesinversion.java:491-500,543-544: ROUND_UP never sets MontoADesinvertirCLP: NullPointerException when accumulating, reported as ERROR
    @Test
    void apvB114_porcentajeRedondeoHaciaArribaRvFallaConMontoClpNulo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(13));
    }

    // CalculoMontosDesinversion.java:501-510: ROUND_DOWN (non RV) ends the same way
    @Test
    void apvB115_porcentajeRedondeoHaciaAbajoFmFallaConMontoClpNulo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(14));
    }

    // CalculoMontosDesinversion.java:511-520: ROUND_HALF_UP ends the same way
    @Test
    void apvB116_porcentajeRedondeoHalfUpRvFallaConMontoClpNulo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(15));
    }

    // CalculoMontosDesinversion.java:521-540: HALF_DOWN, non FM: quantity rounded to 0 decimals, amount = quantity*price, converted to CLP through CalcularParidad (CLP rounded HALF_DOWN)
    @Test
    void apvB117_porcentajeHalfDownRentaVariableConvierteAPesos() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(16));
    }

    // CalculoMontosDesinversion.java:526-533: HALF_DOWN, FM: quantity and amount use DecimalesCuota of the series (FMM.SerieDeFondo, 4 decimals) and CalcularParidad converts to CLP
    @Test
    void apvB118_porcentajeHalfDownFondoMutuoUsaDecimalesDeLaSerie() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(17));
    }

    // CalculoMontosDesinversion.java:526-530: HALF_DOWN FM whose series does not exist: valorCuotaTipoParidad.get(0) fails, IndexOutOfBoundsException caught and reported
    @Test
    void apvB119_porcentajeHalfDownFondoMutuoSinSerieInformaError() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(18));
    }

    // CalculoMontosDesinversion.java:526-533: HALF_DOWN FM in USD (3 decimals) converted to pesos by the collaborator, CLP rounded HALF_DOWN
    @Test
    void apvB120_porcentajeHalfDownFmEnDolaresConvierteAPesos() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(19));
    }

    // CalculoMontosDesinversion.java:372-377,482: a percentage below 1 has longValue()==0 so it is treated as 'amount mode' (percentage recomputed from the total) and the amounts are not converted before the switch
    @Test
    void apvB121_porcentajeMenorAUnoSeTrataComoMontoTotal() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(20));
    }

    // CalculoMontosDesinversion.java:372-376,543-544: two custodies: one CalcularParidad call per custody (HALF_DOWN) and MontoTotal is the sum of the CLP amounts
    @Test
    void apvB122_porcentajeDosCustodiasSumaMontos() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(21));
    }

    // CalculoMontosDesinversion.java:223-248,482-483: amount mode: the foreign-currency custody asks CalcularParidad to convert the share, then HALF_DOWN converts the divested amount back (3 calls)
    @Test
    void apvB123_montoTotalCalculaPctYPideParidadEnMonedaExtranjera() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(22));
    }

    // CalculoMontosDesinversion.java:240,272-284: two custodies with the same IdRegistro in a foreign currency: List2Hash fails ('key field is not unique'), caught and reported
    @Test
    void apvB124_montoTotalConIdRegistroDuplicadoInformaError() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(23));
    }

    // CalculoMontosDesinversion.java:280-282: a CalcularParidad answer with errors is ignored (empty list): MontoADesinvertirCLP 0
    @Test
    void apvB125_montoTotalParidadConErroresSeIgnora() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(24));
    }

    // CalculoMontosDesinversion.java:482-545: amount mode with ROUND_DOWN: the paridad call happens (line 483) and then the NullPointerException of line 544 is reported
    @Test
    void apvB126_montoTotalSinRedondeoConocidoFallaConMontoClpNulo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(25));
    }

    // CalculoMontosDesinversion.java:379-436: traspaso detail with MontoTraspaso > 0, not FM: amount converted by CalcularParidad, quantity = amount/price rounded to 0 decimals HALF_UP
    @Test
    void apvB127_traspasoPorMontoRentaVariableCalculaCantidadEntera() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(26));
    }

    // CalculoMontosDesinversion.java:426-430: traspaso detail by amount, FM: quantity with DecimalesCuota of the series, HALF_DOWN
    @Test
    void apvB128_traspasoPorMontoFondoMutuoUsaDecimalesDeLaSerie() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(27));
    }

    // CalculoMontosDesinversion.java:425,437: ObtenerDetalleTraspasoDesinversion.sql does not select UFTraspaso: the UF branch can never run; a detail with a NULL MontoTraspaso fails at the first comparison
    @Test
    void apvB129_traspasoPorUfNuncaSeLeeYFallaConMontoNulo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(28));
    }

    // CalculoMontosDesinversion.java:449-461: traspaso detail by percentage (MontoTraspaso 0), not FM: quantity = pct*quantity/100 (0 decimals), amount in CLP through CalcularParidad
    @Test
    void apvB130_traspasoPorPorcentajeRentaVariableConvierteAPesos() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(29));
    }

    // CalculoMontosDesinversion.java:449-454: traspaso detail by percentage, FM: DecimalesCuota of the series, CLP through CalcularParidad
    @Test
    void apvB131_traspasoPorPorcentajeFmUsaDecimalesDeLaSerie() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(30));
    }

    // CalculoMontosDesinversion.java:417-467: no detail matches the custody (regimen differs): MontoADesinvertirCLP stays null and the accumulation fails (NullPointerException reported)
    @Test
    void apvB132_traspasoSinDetalleQueCoincidaInformaErrorNulo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(31));
    }

    // CalculoMontosDesinversion.java:425,437,449: a detail with amount, UF and percentage all 0 selects no branch: same null failure
    @Test
    void apvB133_traspasoConDetalleEnCeroInformaErrorNulo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(32));
    }

    // CalculoMontosDesinversion.java:379-384,466-467: traspaso without detail rows: same null failure
    @Test
    void apvB134_traspasoSinDetallesInformaErrorNulo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(33));
    }

    // CalculoMontosDesinversion.java:425-436: MontoTraspaso > 0 takes precedence over the percentage of the same detail
    @Test
    void apvB135_traspasoMontoPrevaleceSobreUfYPorcentaje() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(34));
    }

    // CalculoMontosDesinversion.java:417-468: two custodies, each with its detail: totals are added
    @Test
    void apvB136_traspasoDosCustodiasSumanMontos() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(35));
    }

    // CalculoMontosDesinversion.java:423,430: FM custody whose series is missing in a traspaso calculation: IndexOutOfBoundsException caught
    @Test
    void apvB137_traspasoFondoMutuoSinSerieInformaErrorDeIndice() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(36));
    }
}
