package cl.bcs.contabilidad.informe;

import cl.bcs.contabilidad.domain.model.clienterelacionado.Comprobante;
import cl.bcs.contabilidad.domain.model.clienterelacionado.CondicionConcepto;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ConceptoNormativoConfig;
import cl.bcs.contabilidad.domain.model.clienterelacionado.OperacionClienteRelacionado;
import cl.bcs.contabilidad.domain.model.clienterelacionado.OperacionDolar;
import cl.bcs.contabilidad.domain.model.clienterelacionado.PactoTermino;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ResultadoConsultaClienteRelacionado;
import cl.bcs.contabilidad.domain.model.clienterelacionado.TotalConcepto;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ValorCondicion;
import cl.bcs.contabilidad.domain.service.clienterelacionado.ConceptosClienteRelacionadoCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static cl.bcs.contabilidad.informe.ClienteRelacionadoFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of the in-memory part of legacy
 * {@code ConsultarOperacionesClienteRelacionado.logica()} (lines 173-350) and
 * {@code obtenerMontoDesdeComprobante()} (lines 362-418).
 *
 * <p>Legacy file: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/
 * src/cl/bcs/contabilidad/ConsultarOperacionesClienteRelacionado.java (abbreviated "COCR" below).
 *
 * <p>Target: {@code ConceptosClienteRelacionadoCalculator.calcular(monedaSistema, conceptos, fuentes)}.
 */
class ConceptosClienteRelacionadoCalculatorTest {

    private static ResultadoConsultaClienteRelacionado calcular(String moneda, List<ConceptoNormativoConfig> conceptos,
                                                                FuentesB fuentes) {
        return ConceptosClienteRelacionadoCalculator.calcular(moneda, conceptos, fuentes.build());
    }

    private static TotalConcepto total(ResultadoConsultaClienteRelacionado r, String concepto) {
        return r.conceptos().stream().filter(c -> concepto.equals(c.concepto())).findFirst()
            .orElseThrow(() -> new AssertionError("concepto ausente: " + concepto + " en " + r.conceptos()));
    }

    private static void assertTotal(TotalConcepto t, String cantidad, String monto) {
        assertThat(t.cantidad()).as("Cantidad de " + t.concepto()).isEqualByComparingTo(cantidad);
        assertThat(t.monto()).as("Monto de " + t.concepto()).isEqualByComparingTo(monto);
    }

    // ------------------------------------------------------------------ CONT-D-10

    @Test
    @DisplayName("CONT-D-10: concepto sin condiciones (lista vacía o null) aparece con todo en 0 aunque haya operaciones")
    void contD10_conceptoSinCondiciones_seInformaEnCero() {
        // COCR:189-203 -> acumulado en 0 y 'continue' (no se agrega como regla)
        var r = calcular("CLP",
            List.of(concepto(1, "Vacio"), new ConceptoNormativoConfig(2L, "Nulo", null)),
            fuentes().comprobantes(comprobante(10).operado("5000").build()));

        assertThat(r.conceptos()).hasSize(2);
        for (String n : List.of("Vacio", "Nulo")) {
            TotalConcepto t = total(r, n);
            assertTotal(t, "0", "0");
            assertThat(t.resultado()).isEqualByComparingTo("0");
            assertThat(t.activos()).isEqualByComparingTo("0");
            assertThat(t.pasivos()).isEqualByComparingTo("0");
        }
    }

    // ------------------------------------------------------------------ CONT-D-11

    @Test
    @DisplayName("CONT-D-11: una operación cuenta sólo si cumple TODOS los grupos (TipoInstrumento, TipoDocumento, CompraVenta)")
    void contD11_debeCumplirTodosLosGrupos() {
        // COCR:230-255 -> cada grupo se compara con op.IdTipoInstrumento / op.TipoDocumento / op.CompraVenta
        var c = concepto(1, "Compras RV",
            cond("TipoInstrumento", "ACN"), cond("TipoDocumento", "FAC"), cond("CompraVenta", "C"));
        var r = calcular("CLP", List.of(c), fuentes().comprobantes(
            comprobante(1).tipoInstrumento("ACN").tipoDocumento("FAC").compraVenta("C").operado("1000").build(),
            comprobante(2).tipoInstrumento("ACN").tipoDocumento("FAC").compraVenta("V").operado("2000").build(),
            comprobante(3).tipoInstrumento("ACN").tipoDocumento("FAE").compraVenta("C").operado("4000").build(),
            comprobante(4).tipoInstrumento("CFI").tipoDocumento("FAC").compraVenta("C").operado("8000").build()));

        assertTotal(total(r, "Compras RV"), "1", "1000");
    }

    @Test
    @DisplayName("CONT-D-11: nombre del grupo es case-insensitive; los valores configurados son case-sensitive")
    void contD11_nombreGrupoIgnoraMayusculas_valorNo() {
        // COCR:236-238 equalsIgnoreCase sobre el nombre del grupo; COCR:244 mapa.containsKey (HashMap, exacto)
        var c = concepto(1, "Mayus", cond("TIPOINSTRUMENTO", "ACN"), cond("compraventa", "C"));
        var r = calcular("CLP", List.of(c), fuentes().comprobantes(
            comprobante(1).tipoInstrumento("ACN").compraVenta("C").operado("100").build(),
            comprobante(2).tipoInstrumento("acn").compraVenta("C").operado("200").build(),
            comprobante(3).tipoInstrumento("ACN").compraVenta("c").operado("400").build()));

        assertTotal(total(r, "Mayus"), "1", "100");
    }

    // ------------------------------------------------------------------ CONT-D-12

    @Test
    @DisplayName("CONT-D-12: un grupo de condición desconocido (no TipoInstrumento/TipoDocumento/CompraVenta/TipoDato) nunca se cumple")
    void contD12_grupoDesconocido_nuncaCumple() {
        // COCR:235-253 -> valorOperacion queda null -> cumpleGrupo=false
        var c = concepto(1, "Moneda", cond("Moneda", "CLP"));
        var r = calcular("CLP", List.of(c), fuentes().comprobantes(comprobante(1).operado("100").build()));
        assertTotal(total(r, "Moneda"), "0", "0");
    }

    // ------------------------------------------------------------------ CONT-D-13

    @Test
    @DisplayName("CONT-D-13: el grupo TipoDocumento se omite para operaciones VENTER/COMTER/COMPAC/VENPAC")
    void contD13_tipoDocumentoSeOmiteParaPactosYTerminos() {
        // COCR:37 EXCLUIR_OPERACIONES; COCR:233
        var c = concepto(1, "Pactos PDBC", cond("TipoDocumento", "FAC"), cond("TipoInstrumento", "PDBC"));
        var r = calcular("CLP", List.of(c), fuentes()
            .pactos(pacto(1).tipoOperacion("VENPAC").tipoDocumento(null).tipoInstrumento("PDBC").montoInicial("5000000").build(),
                    pacto(2).tipoOperacion("COMPAC").tipoDocumento("XXX").tipoInstrumento("PDBC").montoInicial("1000000").build())
            .terminosCartera(pacto(3).tipoOperacion("COMTER").tipoDocumento("COMTER").tipoInstrumento("PDBC").montoInicial("300").build())
            // a comprobante whose operation is NOT excluded must still satisfy TipoDocumento
            .comprobantes(comprobante(4).tipoOperacion("COMACC").tipoDocumento(null).tipoInstrumento("PDBC").operado("7").build()));

        assertTotal(total(r, "Pactos PDBC"), "3", "6000300");
    }

    // ------------------------------------------------------------------ CONT-D-14

    @Test
    @DisplayName("CONT-D-14: valor con OperadorFuncion '-' resta el monto (la cantidad igual suma 1)")
    void contD14_operadorMenos_restaMonto() {
        // COCR:246-247 restar=true; COCR:279 monto.negate(); COCR:281-282 Cantidad+1
        var grupoDoc = new CondicionConcepto("TipoDocumento",
            List.of(new ValorCondicion("FAC", "+"), new ValorCondicion("NCR", "-")));
        var c = concepto(1, "Neto", grupoDoc);
        var r = calcular("CLP", List.of(c), fuentes().comprobantes(
            comprobante(1).tipoDocumento("FAC").operado("1000").build(),
            comprobante(2).tipoDocumento("NCR").operado("300").build()));

        assertTotal(total(r, "Neto"), "2", "700");
    }

    // ------------------------------------------------------------------ CONT-D-15

    @Test
    @DisplayName("CONT-D-15: un mismo FolioOperacion cuenta una sola vez por concepto; folio null nunca se deduplica")
    void contD15_dedupPorFolioDentroDelConcepto() {
        // COCR:44 foliosProcesados por ReglaConcepto; COCR:225 y COCR:286 (sólo si FolioOperacion != null)
        var c1 = concepto(1, "C1", cond("TipoInstrumento", "ACN", "PRT"));
        var c2 = concepto(2, "C2", cond("TipoInstrumento", "ACN", "PRT"));
        var r = calcular("CLP", List.of(c1, c2), fuentes()
            .comprobantes(comprobante(10).tipoInstrumento("ACN").operado("100").build(),
                          comprobante(10).tipoInstrumento("ACN").operado("999").build())
            .prestamos(comprobante(0).folio(null).tipoInstrumento("PRT").operado("50").build(),
                       comprobante(0).folio(null).tipoInstrumento("PRT").operado("50").build()));

        assertTotal(total(r, "C1"), "3", "200");
        assertTotal(total(r, "C2"), "3", "200"); // dedup is per concept, not global
    }

    // ------------------------------------------------------------------ CONT-D-16

    @Test
    @DisplayName("CONT-D-16: conceptos se calculan sobre comprobantes, préstamos, pactos, spots, forwards y términos-cartera; NO sobre términos")
    void contD16_fuentesDeConceptos_excluyenTerminos() {
        // COCR:183-184 Arrays.asList(comprobantes, prestamosEmprestitos, pactos, spots, forwards, terminosCartera)
        var c = concepto(1, "PDBC", cond("TipoInstrumento", "PDBC"));
        var r = calcular("CLP", List.of(c), fuentes()
            .pactos(pacto(1).tipoInstrumento("PDBC").montoInicial("100").build())
            .terminos(pacto(2).tipoOperacion("COMTER").tipoInstrumento("PDBC").montoInicial("200").build())
            .terminosCartera(pacto(3).tipoOperacion("COMTER").tipoInstrumento("PDBC").montoInicial("400").build()));

        assertTotal(total(r, "PDBC"), "2", "500");
    }

    @Test
    @DisplayName("CONT-D-16: el orden de fuentes decide qué fila gana la deduplicación (comprobante antes que término-cartera)")
    void contD16_ordenDeFuentes_comprobanteGanaDedup() {
        // COCR:184 orden: comprobantes primero, terminosCartera al final
        var c = concepto(1, "ACN", cond("TipoInstrumento", "ACN"));
        var r = calcular("CLP", List.of(c), fuentes()
            .comprobantes(comprobante(7).tipoInstrumento("ACN").operado("10").build())
            .terminosCartera(pacto(7).tipoOperacion("VENTER").tipoInstrumento("ACN").montoInicial("99").build()));

        assertTotal(total(r, "ACN"), "1", "10");
    }

    // ------------------------------------------------------------------ CONT-D-17 / 18

    @ParameterizedTest(name = "CONT-D-17: moneda sistema CLP, comprobante CLP, TipoDato {0} -> {1}")
    @CsvSource({
        "MONTO_COBRO,4", "MONTO_COMISION,8", "MONTO_DERECHO,64", "MONTO_GASTOS,16", "MONTO_IVA,32",
        "MONTO_OPERADO,1", "MONTO_TOTAL,2", "monto_comision,8",
        // *_MX keys only apply when currencies differ -> total 0 -> fallback MontoOperado (=1)
        "VALOR_GASTOS_MX,1", "MONTO_COMISION_MX,1",
        // unknown key -> ignored -> fallback MontoOperado
        "MONTO_RETENCION,1"
    })
    void contD17_tipoDatoMismaMoneda(String tipoDato, String esperado) {
        // COCR:261-262; COCR:364 mismaMoneda (equalsIgnoreCase); COCR:368-411 switch(tipo.toUpperCase()); COCR:414-416 fallback
        var c = concepto(1, "TD", cond("TipoDato", tipoDato), cond("TipoInstrumento", "ACN"));
        var r = calcular("CLP", List.of(c), fuentes().comprobantes(montosPotenciaDeDos(1, "CLP")));
        assertTotal(total(r, "TD"), "1", esperado);
    }

    @ParameterizedTest(name = "CONT-D-18: moneda sistema CLP, comprobante USD, TipoDato {0} -> {1}")
    @CsvSource({
        "VALOR_COBRO_MX,4", "MONTO_COMISION_MX,8", "VALOR_DERECHO_MX,64", "VALOR_GASTOS_MX,16", "VALOR_IVA_MX,32",
        "VALOR_OPERADO_MX,1", "VALOR_TOTAL_MX,2",
        // local-currency keys do not apply to a foreign-currency comprobante -> fallback MontoOperado
        "MONTO_COMISION,1"
    })
    void contD18_tipoDatoDistintaMoneda(String tipoDato, String esperado) {
        // COCR:390-409 (*_MX sólo si !mismaMoneda)
        var c = concepto(1, "TD", cond("TipoDato", tipoDato), cond("TipoInstrumento", "ACN"));
        var r = calcular("CLP", List.of(c), fuentes().comprobantes(montosPotenciaDeDos(1, "USD")));
        assertTotal(total(r, "TD"), "1", esperado);
    }

    @Test
    @DisplayName("CONT-D-17: varios TipoDato se suman (comisión + IVA)")
    void contD17_variosTipoDatoSeSuman() {
        // COCR:366-412 acumula total
        var c = concepto(1, "Cobros", cond("TipoDato", "MONTO_COMISION", "MONTO_IVA"), cond("TipoInstrumento", "ACN"));
        var r = calcular("CLP", List.of(c), fuentes().comprobantes(
            comprobante(1).moneda("clp").operado("50000").comision("1000.40").impto("190.08").build()));
        assertTotal(total(r, "Cobros"), "1", "1190.48");
    }

    @Test
    @DisplayName("CONT-D-18: si la suma de TipoDato da 0 y MontoOperado es null, el monto es 0")
    void contD18_fallbackConMontoOperadoNull() {
        // COCR:414-416
        var c = concepto(1, "TD", cond("TipoDato", "MONTO_COMISION"), cond("TipoInstrumento", "ACN"));
        var r = calcular("CLP", List.of(c), fuentes().comprobantes(comprobante(1).operado(null).comision("0").build()));
        assertTotal(total(r, "TD"), "1", "0");
    }

    @Test
    @DisplayName("CONT-D-18: moneda sistema null => nunca 'misma moneda': aplican las claves *_MX")
    void contD18_monedaSistemaNull_usaClavesMx() {
        // COCR:364 monedaSistema != null && ...
        var c = concepto(1, "TD", cond("TipoDato", "VALOR_GASTOS_MX"), cond("TipoInstrumento", "ACN"));
        var r = calcular(null, List.of(c), fuentes().comprobantes(montosPotenciaDeDos(1, "CLP")));
        assertTotal(total(r, "TD"), "1", "16");
    }

    // ------------------------------------------------------------------ CONT-D-19

    @Test
    @DisplayName("CONT-D-19: la clave del grupo de montos es exactamente 'TipoDato'; 'TIPODATO' se ignora al filtrar y al sumar")
    void contD19_claveTipoDatoCaseSensitive() {
        // COCR:231 equalsIgnoreCase("TIPODATO") -> se salta en el filtro;
        // COCR:261 getOrDefault("TipoDato") (HashMap, exacto) -> no encuentra 'TIPODATO' -> fallback MontoOperado
        var c = concepto(1, "TD", cond("TIPODATO", "MONTO_COMISION"), cond("TipoInstrumento", "ACN"));
        var r = calcular("CLP", List.of(c), fuentes().comprobantes(montosPotenciaDeDos(1, "CLP")));
        assertTotal(total(r, "TD"), "1", "1");
    }

    // ------------------------------------------------------------------ CONT-D-20

    @Test
    @DisplayName("CONT-D-20: pactos/términos aportan MontoInicial (null cuenta como 0, pero suma 1 a la cantidad)")
    void contD20_pactoAportaMontoInicial() {
        // COCR:263-265
        var c = concepto(1, "P", cond("TipoInstrumento", "PDBC"));
        var r = calcular("CLP", List.of(c), fuentes().pactos(
            pacto(1).tipoInstrumento("PDBC").montoInicial("10000000").montoFinal("10025000").build(),
            pacto(2).tipoInstrumento("PDBC").montoInicial(null).build()));
        assertTotal(total(r, "P"), "2", "10000000");
    }

    // ------------------------------------------------------------------ CONT-D-21

    @Test
    @DisplayName("CONT-D-21: operaciones dólar aportan MontoCompra si CompraVenta='C' (ignora mayúsculas), si no MontoVenta")
    void contD21_dolarSegunCompraVenta() {
        // COCR:266-277
        var c = concepto(1, "Dolar", cond("TipoInstrumento", "SPT", "FWD"));
        var r = calcular("CLP", List.of(c), fuentes()
            .spots(dolar(1).compraVenta("C").compra("100").venta("999").build(),
                   dolar(2).compraVenta("V").compra("999").venta("50").build(),
                   dolar(3).compraVenta("c").compra("7").venta("999").build(),
                   dolar(4).compraVenta(null).compra("999").venta("3").build(),
                   dolar(5).tipoOperacion("ANCSPT").compraVenta("C").compra("70").venta("999").build(),
                   dolar(6).tipoOperacion("ANVSPT").compraVenta("V").compra("999").venta("20").build(),
                   dolar(7).compraVenta("C").compra(null).venta("999").build())
            .forwards(dolar(8).tipoInstrumento("FWD").compraVenta("V").compra("0").venta("1000").build()));

        // 100 + 50 + 7 + 3 + 70 + 20 + 0 + 1000
        assertTotal(total(r, "Dolar"), "8", "1250");
    }

    // ------------------------------------------------------------------ CONT-D-22

    @Test
    @DisplayName("CONT-D-22: 'Total' suma Cantidad y Monto de todos los conceptos; Resultado/Activos/Pasivos siempre 0")
    void contD22_totalizado() {
        // COCR:292-322 (Resultado y Saldo nunca se calculan: quedan en BigDecimal.ZERO)
        var r = calcular("CLP",
            List.of(concepto(1, "A", cond("TipoInstrumento", "ACN")),
                    concepto(2, "B", cond("TipoInstrumento", "CFI")),
                    concepto(3, "Vacio")),
            fuentes().comprobantes(
                comprobante(1).tipoInstrumento("ACN").operado("100").build(),
                comprobante(2).tipoInstrumento("CFI").operado("50.5").build()));

        TotalConcepto t = r.totalizado();
        assertThat(t.concepto()).isEqualTo("Total");
        assertThat(t.cantidad()).isEqualByComparingTo("2");
        assertThat(t.monto()).isEqualByComparingTo("150.5");
        assertThat(t.resultado()).isEqualByComparingTo("0");
        assertThat(t.activos()).isEqualByComparingTo("0");
        assertThat(t.pasivos()).isEqualByComparingTo("0");
        for (TotalConcepto c : r.conceptos()) {
            assertThat(c.resultado()).isEqualByComparingTo("0");
            assertThat(c.activos()).isEqualByComparingTo("0");
            assertThat(c.pasivos()).isEqualByComparingTo("0");
        }
    }

    @Test
    @DisplayName("CONT-D-22: sin conceptos configurados el 'Total' existe y está en 0")
    void contD22_sinConceptos_totalEnCero() {
        var r = calcular("CLP", List.of(), fuentes().comprobantes(comprobante(1).operado("100").build()));
        assertThat(r.conceptos()).isEmpty();
        assertThat(r.totalizado().concepto()).isEqualTo("Total");
        assertThat(r.totalizado().cantidad()).isEqualByComparingTo("0");
        assertThat(r.totalizado().monto()).isEqualByComparingTo("0");
    }

    // ------------------------------------------------------------------ CONT-D-23

    @Test
    @DisplayName("CONT-D-23: conceptos salen ordenados por IdConceptoNormativo ascendente (orden HashMap<Long> del legacy para ids < 16)")
    void contD23_ordenConceptosPorId() {
        // COCR:186 HashMap<Long,..>; COCR:324 new ArrayList<>(agrupados.values()). See report: legacy order is
        // only ascending for small ids (17 would come before 1); modern contract = ascending id.
        var r = calcular("CLP",
            List.of(concepto(3, "Tres", cond("TipoInstrumento", "ACN")), concepto(1, "Uno"), concepto(2, "Dos")),
            fuentes());
        assertThat(r.conceptos()).extracting(TotalConcepto::concepto).containsExactly("Uno", "Dos", "Tres");
    }

    // ------------------------------------------------------------------ CONT-D-24

    @Test
    @DisplayName("CONT-D-24: 'operaciones' = comprobantes, spots, forwards, pactos, términos (en ese orden); sin préstamos ni términos-cartera")
    void contD24_listaOperaciones_composicionYOrden() {
        // COCR:328 Arrays.asList(comprobantes, spots, forwards, pactos, terminos)
        var r = calcular("CLP", List.of(), fuentes()
            .comprobantes(comprobante(1).build(), comprobante(2).build())
            .spots(dolar(3).build())
            .forwards(dolar(4).tipoInstrumento("FWD").build())
            .pactos(pacto(5).build())
            .terminos(pacto(6).tipoOperacion("COMTER").build())
            .prestamos(comprobante(7).tipoInstrumento("PRT").build())
            .terminosCartera(pacto(8).tipoOperacion("VENTER").build()));

        assertThat(r.operaciones()).extracting(OperacionClienteRelacionado::folioOperacion)
            .containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
        assertThat(r.operaciones().get(0)).isInstanceOf(Comprobante.class);
        assertThat(r.operaciones().get(2)).isInstanceOf(OperacionDolar.class);
        assertThat(r.operaciones().get(4)).isInstanceOf(PactoTermino.class);
    }

    // ------------------------------------------------------------------ CONT-D-25

    @ParameterizedTest(name = "CONT-D-25: TipoDocumento {0} => montos del comprobante salen negados en 'operaciones'")
    @ValueSource(strings = {"NCE", "NCR", "NDR", "NDE", "NEE", "NEO"})
    void contD25_documentosNegativos_comprobanteNegado(String tipoDoc) {
        // COCR:326 documentosNegativos; COCR:329-342
        var r = calcular("CLP", List.of(), fuentes().comprobantes(
            comprobante(1).tipoDocumento(tipoDoc).operado("100").total("110").cobro("10").comision("5").gasto("3")
                .impto("2").retencion(null).derecho("0.5").build()));

        Comprobante c = (Comprobante) r.operaciones().get(0);
        assertThat(c.montoOperado()).isEqualByComparingTo("-100");
        assertThat(c.montoTotal()).isEqualByComparingTo("-110");
        assertThat(c.montoCobro()).isEqualByComparingTo("-10");
        assertThat(c.totalComision()).isEqualByComparingTo("-5");
        assertThat(c.totalGasto()).isEqualByComparingTo("-3");
        assertThat(c.totalImpto()).isEqualByComparingTo("-2");
        assertThat(c.totalRetencion()).as("null se mantiene null (COCR:358-360)").isNull();
        assertThat(c.totalDerecho()).isEqualByComparingTo("-0.5");
    }

    @Test
    @DisplayName("CONT-D-25: FAC no se niega; dólar con documento negativo niega compra/venta; pacto nunca se niega")
    void contD25_otrosTiposYDocumentos() {
        // COCR:330 (TipoDocumento != null && contiene); COCR:344-348 OperacionDolar; PactoTermino sin rama
        var r = calcular("CLP", List.of(), fuentes()
            .comprobantes(comprobante(1).tipoDocumento("FAC").operado("100").build())
            .spots(dolar(2).tipoDocumento("NCE").compra("40").venta("0").build())
            .pactos(pacto(3).tipoDocumento("NCR").montoInicial("500").montoFinal("510").build()));

        assertThat(((Comprobante) r.operaciones().get(0)).montoOperado()).isEqualByComparingTo("100");
        OperacionDolar d = (OperacionDolar) r.operaciones().get(1);
        assertThat(d.montoCompra()).isEqualByComparingTo("-40");
        assertThat(d.montoVenta()).isEqualByComparingTo("0");
        PactoTermino p = (PactoTermino) r.operaciones().get(2);
        assertThat(p.montoInicial()).isEqualByComparingTo("500");
        assertThat(p.montoFinal()).isEqualByComparingTo("510");
    }

    @Test
    @DisplayName("CONT-D-25: la negación ocurre después de acumular: el concepto suma el valor positivo (ABS del SQL)")
    void contD25_negacionNoAfectaConceptos() {
        // COCR:223-290 acumula antes; COCR:328-350 niega después
        var c = concepto(1, "NC", cond("TipoDocumento", "NCR"));
        var r = calcular("CLP", List.of(c), fuentes().comprobantes(
            comprobante(1).tipoDocumento("NCR").operado("100").build()));

        assertTotal(total(r, "NC"), "1", "100");
        assertThat(((Comprobante) r.operaciones().get(0)).montoOperado()).isEqualByComparingTo("-100");
    }

    // ------------------------------------------------------------------ helpers

    /** Comprobante ACN with distinguishable amounts: operado 1, total 2, cobro 4, comision 8, gasto 16, impto 32, derecho 64, retencion 128. */
    private static Comprobante montosPotenciaDeDos(long folio, String moneda) {
        return comprobante(folio).tipoInstrumento("ACN").moneda(moneda)
            .operado("1").total("2").cobro("4").comision("8").gasto("16").impto("32").derecho("64").retencion("128")
            .build();
    }
}
