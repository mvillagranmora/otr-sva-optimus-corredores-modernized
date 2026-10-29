package cl.bcs.mesa;

import cl.bcs.mesa.domain.*;
import cl.bcs.mesa.service.CalculadoraSpotService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for BcsMesaServicios.
 *
 * Source: legacy/BSCL/Mesa/JAVA/BcsMesaServicios/src/cl/bcs/mesa/SPT/CalculosCalculadoraSpot.java
 *
 * Pure JUnit 5 — no Spring context, no HANA, no DB.
 * Each test pins a specific formula or filtering rule from the legacy code.
 */
class MesaServiciosTest {

    // Helper: 8-arg overload delegates to 6-arg service method (last two args ignored by legacy)
    BreakEvenResult calcularBreakEven(BigDecimal posicionInicial, BigDecimal precioInicial,
                                      BigDecimal compras, BigDecimal promCompras,
                                      BigDecimal ventas, BigDecimal promVentas,
                                      BigDecimal ignoredA, BigDecimal ignoredB) {
        return CalculadoraSpotService.calcularBreakEven(
            posicionInicial, precioInicial, compras, promCompras, ventas, promVentas);
    }

    // =========================================================================
    // Tests — MOVIMIENTOS VIGENTES FILTER (B-MSP-VIG-*)
    // =========================================================================

    @Test void vigentes_estado_vig_incluido() {
        MovimientoSpot m = new MovimientoSpot(); m.Estado = "VIG";
        assertEquals(1, CalculadoraSpotService.filtrarVigentes(List.of(m)).size());
    }

    @Test void vigentes_estado_pel_incluido() {
        MovimientoSpot m = new MovimientoSpot(); m.Estado = "PEL";
        assertEquals(1, CalculadoraSpotService.filtrarVigentes(List.of(m)).size());
    }

    @Test void vigentes_estado_pda_incluido() {
        MovimientoSpot m = new MovimientoSpot(); m.Estado = "PDA";
        assertEquals(1, CalculadoraSpotService.filtrarVigentes(List.of(m)).size());
    }

    @Test void vigentes_estado_anu_excluido() {
        MovimientoSpot m = new MovimientoSpot(); m.Estado = "ANU";
        assertEquals(0, CalculadoraSpotService.filtrarVigentes(List.of(m)).size());
    }

    @Test void vigentes_estado_otro_excluido() {
        MovimientoSpot m = new MovimientoSpot(); m.Estado = "CAN";
        assertEquals(0, CalculadoraSpotService.filtrarVigentes(List.of(m)).size());
    }

    // =========================================================================
    // Tests — INTERBANCARIOS FILTER (B-MSP-INTERB-*)
    // =========================================================================

    @Test void interbancarios_nemotecnico_interb_canal_otc_incluido() {
        MovimientoSpot m = new MovimientoSpot();
        m.Nemotecnico = "INTERB"; m.IdTipoCanalIngreso = "OTC"; m.Estado = "VIG";
        assertEquals(1, CalculadoraSpotService.filtrarInterbancarios(List.of(m)).size());
    }

    @Test void interbancarios_nemotecnico_interb_canal_fwd_excluido() {
        MovimientoSpot m = new MovimientoSpot();
        m.Nemotecnico = "INTERB"; m.IdTipoCanalIngreso = "FWD"; m.Estado = "VIG";
        assertEquals(0, CalculadoraSpotService.filtrarInterbancarios(List.of(m)).size());
    }

    @Test void interbancarios_nemotecnico_arbint_canal_otc_incluido() {
        MovimientoSpot m = new MovimientoSpot();
        m.Nemotecnico = "ARBINT"; m.IdTipoCanalIngreso = "OTC"; m.Estado = "VIG";
        assertEquals(1, CalculadoraSpotService.filtrarInterbancarios(List.of(m)).size());
    }

    @Test void interbancarios_nemotecnico_distri_excluido() {
        MovimientoSpot m = new MovimientoSpot();
        m.Nemotecnico = "DISTRI"; m.IdTipoCanalIngreso = "OTC"; m.Estado = "VIG";
        assertEquals(0, CalculadoraSpotService.filtrarInterbancarios(List.of(m)).size());
    }

    // =========================================================================
    // Tests — DISTRIBUCION FILTER (B-MSP-DISTRI-*)
    // =========================================================================

    @Test void distribucion_nemotecnico_distri_incluido() {
        MovimientoSpot m = new MovimientoSpot();
        m.Nemotecnico = "DISTRI"; m.Estado = "VIG";
        assertEquals(1, CalculadoraSpotService.filtrarDistribucion(List.of(m)).size());
    }

    @Test void distribucion_nemotecnico_arbdis_incluido() {
        MovimientoSpot m = new MovimientoSpot();
        m.Nemotecnico = "ARBDIS"; m.Estado = "VIG";
        assertEquals(1, CalculadoraSpotService.filtrarDistribucion(List.of(m)).size());
    }

    @Test void distribucion_nemotecnico_interb_excluido() {
        MovimientoSpot m = new MovimientoSpot();
        m.Nemotecnico = "INTERB"; m.Estado = "VIG";
        assertEquals(0, CalculadoraSpotService.filtrarDistribucion(List.of(m)).size());
    }

    // =========================================================================
    // Tests — RESUMEN OPERACIONES SPOT (B-MSP-RES-*)
    // =========================================================================

    @Test void resumen_anulada_contabilizada() {
        MovimientoSpot m = new MovimientoSpot();
        m.Estado = "ANU"; m.IdTipoOperacion = "COMSPT";
        SalidaResumenOperacionesSpot r = CalculadoraSpotService.resumenOperaciones(List.of(m));
        assertEquals(1, r.NumeroAnuladas);
        assertEquals(0, r.NumeroCompras);
        assertEquals(1, r.TotalOperaciones);
    }

    @Test void resumen_compra_vig_contabilizada() {
        MovimientoSpot m = new MovimientoSpot();
        m.Estado = "VIG"; m.IdTipoOperacion = "COMSPT";
        SalidaResumenOperacionesSpot r = CalculadoraSpotService.resumenOperaciones(List.of(m));
        assertEquals(1, r.NumeroCompras);
        assertEquals(0, r.NumeroAnuladas);
        assertEquals(1, r.TotalOperaciones);
    }

    @Test void resumen_venta_vig_contabilizada() {
        MovimientoSpot m = new MovimientoSpot();
        m.Estado = "VIG"; m.IdTipoOperacion = "VENSPT";
        SalidaResumenOperacionesSpot r = CalculadoraSpotService.resumenOperaciones(List.of(m));
        assertEquals(1, r.NumeroVentas);
        assertEquals(1, r.TotalOperaciones);
    }

    @Test void resumen_compra_pel_no_cuenta_en_compras_pero_si_total() {
        MovimientoSpot m = new MovimientoSpot();
        m.Estado = "PEL"; m.IdTipoOperacion = "COMSPT";
        SalidaResumenOperacionesSpot r = CalculadoraSpotService.resumenOperaciones(List.of(m));
        assertEquals(0, r.NumeroCompras);
        assertEquals(1, r.TotalOperaciones);
    }

    @Test void resumen_multiple_operaciones_conteo_correcto() {
        MovimientoSpot c1 = new MovimientoSpot(); c1.Estado = "VIG"; c1.IdTipoOperacion = "COMSPT";
        MovimientoSpot c2 = new MovimientoSpot(); c2.Estado = "VIG"; c2.IdTipoOperacion = "COMSPT";
        MovimientoSpot v1 = new MovimientoSpot(); v1.Estado = "VIG"; v1.IdTipoOperacion = "VENSPT";
        MovimientoSpot a1 = new MovimientoSpot(); a1.Estado = "ANU"; a1.IdTipoOperacion = "COMSPT";
        SalidaResumenOperacionesSpot r = CalculadoraSpotService.resumenOperaciones(List.of(c1, c2, v1, a1));
        assertEquals(2, r.NumeroCompras);
        assertEquals(1, r.NumeroVentas);
        assertEquals(1, r.NumeroAnuladas);
        assertEquals(4, r.TotalOperaciones);
    }

    // =========================================================================
    // Tests — RESULTADO POSICION INICIAL (B-MSP-POS-*)
    // =========================================================================

    @Test void resultado_posicion_inicial_formula_basica() {
        PosicionInicial p = new PosicionInicial();
        p.IdMoneda = "USD";
        p.PosicionInicial = new BigDecimal("1000000");
        p.TipoCambioInicial = new BigDecimal("900");
        p.CierreIngresado = new BigDecimal("950");
        // diferencia = 950 - 900 = 50; resultado = 50 * 1000000 = 50000000
        BigDecimal resultado = CalculadoraSpotService.resultadoPosicionInicial(List.of(p), "USD");
        assertEquals(0, new BigDecimal("50000000").compareTo(resultado));
    }

    @Test void resultado_posicion_inicial_moneda_distinta_excluida() {
        PosicionInicial p = new PosicionInicial();
        p.IdMoneda = "EUR";
        p.PosicionInicial = new BigDecimal("500000");
        p.TipoCambioInicial = new BigDecimal("1050");
        p.CierreIngresado = new BigDecimal("1100");
        // Consultamos USD → posicion = 0 → resultado = 0
        BigDecimal resultado = CalculadoraSpotService.resultadoPosicionInicial(List.of(p), "USD");
        assertEquals(0, BigDecimal.ZERO.compareTo(resultado));
    }

    @Test void resultado_posicion_inicial_cierre_menor_tipo_cambio_negativo() {
        PosicionInicial p = new PosicionInicial();
        p.IdMoneda = "USD";
        p.PosicionInicial = new BigDecimal("1000000");
        p.TipoCambioInicial = new BigDecimal("950");
        p.CierreIngresado = new BigDecimal("900");
        // diferencia = 900 - 950 = -50; resultado = -50 * 1000000 = -50000000
        BigDecimal resultado = CalculadoraSpotService.resultadoPosicionInicial(List.of(p), "USD");
        assertTrue(resultado.compareTo(BigDecimal.ZERO) < 0);
    }

    @Test void posicion_inicial_moneda_suma_solo_matching() {
        PosicionInicial usd = new PosicionInicial();
        usd.IdMoneda = "USD"; usd.PosicionInicial = new BigDecimal("2000000");
        PosicionInicial eur = new PosicionInicial();
        eur.IdMoneda = "EUR"; eur.PosicionInicial = new BigDecimal("500000");
        BigDecimal resultado = CalculadoraSpotService.posicionInicialMoneda(List.of(usd, eur), "USD");
        assertEquals(0, new BigDecimal("2000000").compareTo(resultado));
    }

    // =========================================================================
    // Tests — PROMEDIO DE COMPRA/VENTA (B-MSP-PROM-*)
    // =========================================================================

    @Test void promedio_compra_formula_basica() {
        BigDecimal monto = new BigDecimal("900000000");
        BigDecimal compras = new BigDecimal("1000000");
        // 900000000 / 1000000 = 900
        BigDecimal prom = CalculadoraSpotService.promedioDeCompra(monto, compras);
        assertEquals(0, new BigDecimal("900").compareTo(prom));
    }

    @Test void promedio_compra_devuelve_cero_cuando_compras_son_cero() {
        BigDecimal prom = CalculadoraSpotService.promedioDeCompra(new BigDecimal("900000000"), BigDecimal.ZERO);
        assertEquals(0, BigDecimal.ZERO.compareTo(prom));
    }

    @Test void promedio_venta_formula_basica() {
        BigDecimal monto = new BigDecimal("950000000");
        BigDecimal ventas = new BigDecimal("1000000");
        BigDecimal prom = CalculadoraSpotService.promedioDeVenta(monto, ventas);
        assertEquals(0, new BigDecimal("950").compareTo(prom));
    }

    @Test void promedio_venta_devuelve_cero_cuando_ventas_son_cero() {
        BigDecimal prom = CalculadoraSpotService.promedioDeVenta(new BigDecimal("950000000"), BigDecimal.ZERO);
        assertEquals(0, BigDecimal.ZERO.compareTo(prom));
    }

    // =========================================================================
    // Tests — UTILIDAD COMPRA/VENTA INTERBANCARIO (B-MSP-UTIL-INTERB-*)
    // =========================================================================

    @Test void utilidad_compra_precio_mayor_cierre_positiva() {
        // precio=950, cierre=900, monto=1000000 → utilidad = 1000000 * (950-900) = 50000000
        BigDecimal util = CalculadoraSpotService.utilidadCompraIncrement(
                new BigDecimal("1000000"), new BigDecimal("950"), new BigDecimal("900"));
        assertEquals(0, new BigDecimal("50000000").compareTo(util));
    }

    @Test void utilidad_compra_precio_menor_cierre_negativa() {
        // precio=900, cierre=950 → utilidad negativa
        BigDecimal util = CalculadoraSpotService.utilidadCompraIncrement(
                new BigDecimal("1000000"), new BigDecimal("900"), new BigDecimal("950"));
        assertTrue(util.compareTo(BigDecimal.ZERO) < 0);
    }

    @Test void utilidad_venta_cierre_mayor_precio_positiva() {
        // cierre=960, precio=900, monto=1000000 → 1000000*(960-900) = 60000000
        BigDecimal util = CalculadoraSpotService.utilidadVentaIncrement(
                new BigDecimal("1000000"), new BigDecimal("960"), new BigDecimal("900"));
        assertEquals(0, new BigDecimal("60000000").compareTo(util));
    }

    @Test void utilidad_venta_cierre_menor_precio_negativa() {
        BigDecimal util = CalculadoraSpotService.utilidadVentaIncrement(
                new BigDecimal("1000000"), new BigDecimal("900"), new BigDecimal("960"));
        assertTrue(util.compareTo(BigDecimal.ZERO) < 0);
    }

    // =========================================================================
    // Tests — UTILIDAD COMPRA/VENTA DISTRIBUCION (B-MSP-UTIL-DISTRI-*)
    // =========================================================================

    @Test void utilidad_compra_distribucion_costo_mayor_cierre_positiva() {
        // monto=1000000, costo=950, cierre=900 → 1000000*(950-900)=50000000
        BigDecimal util = CalculadoraSpotService.utilidadCompraDistribucion(
                new BigDecimal("1000000"), new BigDecimal("950"), new BigDecimal("900"));
        assertEquals(0, new BigDecimal("50000000").compareTo(util));
    }

    @Test void utilidad_venta_distribucion_cierre_mayor_costo_positiva() {
        // monto=1000000, cierre=960, costo=900 → 1000000*(960-900)=60000000
        BigDecimal util = CalculadoraSpotService.utilidadVentaDistribucion(
                new BigDecimal("1000000"), new BigDecimal("960"), new BigDecimal("900"));
        assertEquals(0, new BigDecimal("60000000").compareTo(util));
    }

    @Test void monto_para_promedio_compra_distribucion() {
        // monto=1000000, costo=950 → 950000000
        BigDecimal monto = CalculadoraSpotService.montoParaPromedioCompraDistribucion(
                new BigDecimal("1000000"), new BigDecimal("950"));
        assertEquals(0, new BigDecimal("950000000").compareTo(monto));
    }

    // =========================================================================
    // Tests — BREAKEVEN CALCULATION (B-MSP-BE-*)
    // =========================================================================

    @Test void breakeven_posicion_y_precio_sin_operaciones() {
        // Compras=0, Ventas=0, posicion=1000000, precio=900
        BreakEvenResult r = CalculadoraSpotService.calcularBreakEven(
                new BigDecimal("1000000"), new BigDecimal("900"),
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO);
        // equivPesosPosInicial=900000000; posicionActual=1000000
        // equivEnPesos=900000000; BreakEven=900000000/1000000=900
        assertEquals(0, new BigDecimal("900.0000").compareTo(r.BreakEven));
    }

    @Test void breakeven_devuelve_cero_cuando_posicion_actual_es_cero() {
        // Compras=500000, Ventas=500000, posicionInicial=0
        BreakEvenResult r = calcularBreakEven(
                BigDecimal.ZERO, new BigDecimal("900"),
                new BigDecimal("500000"), new BigDecimal("950"),
                new BigDecimal("500000"), new BigDecimal("950"),
                BigDecimal.ZERO, BigDecimal.ZERO);
        // PosicionActual = 500000-500000+0 = 0 → BreakEven=0
        assertEquals(0, BigDecimal.ZERO.compareTo(r.BreakEven));
    }

    @Test void breakeven_monto_total_es_equiv_en_pesos() {
        BreakEvenResult r = calcularBreakEven(
                new BigDecimal("1000000"), new BigDecimal("900"),
                new BigDecimal("200000"), new BigDecimal("920"),
                new BigDecimal("100000"), new BigDecimal("950"),
                BigDecimal.ZERO, BigDecimal.ZERO);
        // equivPesos = 200000*920 - 100000*950 + 1000000*900
        //            = 184000000 - 95000000 + 900000000 = 989000000
        assertEquals(0, new BigDecimal("989000000.0000").compareTo(r.MontoTotal));
    }

    @Test void breakeven_formula_escala_4_half_up() {
        // scale=4, ROUND_HALF_UP applied to breakEven
        BreakEvenResult r = CalculadoraSpotService.calcularBreakEven(
                new BigDecimal("1000000"), new BigDecimal("900.12345"),
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO);
        // BreakEven = equivEnPesos / posicionActual; equivEnPesos scaled 4
        // equivPesosPosInicial = 900.12345 * 1000000 = 900123450 → ROUND_HALF_UP scale 4
        // posicionActual = 0+0+1000000 = 1000000.0000
        // BreakEven = equivEnPesos / 1000000
        assertEquals(4, r.BreakEven.scale());
    }

    // =========================================================================
    // Tests — PARIDAD A UTILIZAR (B-MSP-PARIDAD-*)
    // =========================================================================

    @Test void paridad_bidask_posicion_positiva_usa_bid() {
        Paridad par = new Paridad();
        par.ParidadBID = new BigDecimal("900");
        par.ParidadASK = new BigDecimal("950");
        par.ParidadMercado = new BigDecimal("925");
        BigDecimal p = CalculadoraSpotService.paridadAUtilizar("BIDASK", new BigDecimal("1000000"), par);
        assertEquals(0, new BigDecimal("900").compareTo(p));
    }

    @Test void paridad_bidask_posicion_negativa_usa_ask() {
        Paridad par = new Paridad();
        par.ParidadBID = new BigDecimal("900");
        par.ParidadASK = new BigDecimal("950");
        par.ParidadMercado = new BigDecimal("925");
        BigDecimal p = CalculadoraSpotService.paridadAUtilizar("BIDASK", new BigDecimal("-500000"), par);
        assertEquals(0, new BigDecimal("950").compareTo(p));
    }

    @Test void paridad_mercado_usa_paridad_mercado() {
        Paridad par = new Paridad();
        par.ParidadBID = new BigDecimal("900");
        par.ParidadASK = new BigDecimal("950");
        par.ParidadMercado = new BigDecimal("925");
        BigDecimal p = CalculadoraSpotService.paridadAUtilizar("MERCADO", new BigDecimal("1000000"), par);
        assertEquals(0, new BigDecimal("925").compareTo(p));
    }

    @Test void paridad_nula_devuelve_cero() {
        BigDecimal p = CalculadoraSpotService.paridadAUtilizar("BIDASK", BigDecimal.ONE, null);
        assertEquals(0, BigDecimal.ZERO.compareTo(p));
    }

    // =========================================================================
    // Tests — MONTO RESULTADO EQUIVALENTE (B-MSP-MRE-*)
    // =========================================================================

    @Test void mre_modo_m_compra_diferencia_positiva() {
        MovimientoSpot m = new MovimientoSpot();
        m.MultiplicaODivide = "M";
        m.TipoCambioCierre = new BigDecimal("900");
        m.ParidadMercado = new BigDecimal("1");     // divide → 900/1=900
        m.ParidadBid = new BigDecimal("890");       // != 0 to pass guard
        m.MontoMonedaPrincipalEquivalente = new BigDecimal("1000000");
        m.IdTipoOperacion = "COMSPT";
        BigDecimal ref = new BigDecimal("950");
        // monto=900; COMSPT → diff=950-900=50; final=50*1000000=50000000
        BigDecimal result = CalculadoraSpotService.montoResultadoEquivalente(m, ref);
        assertEquals(0, new BigDecimal("50000000").compareTo(result));
    }

    @Test void mre_guard_paridad_bid_cero_devuelve_cero() {
        MovimientoSpot m = new MovimientoSpot();
        m.ParidadBid = BigDecimal.ZERO;
        m.ParidadMercado = new BigDecimal("1");
        m.TipoCambioCierre = new BigDecimal("900");
        m.MontoMonedaPrincipalEquivalente = new BigDecimal("1000000");
        m.MultiplicaODivide = "M";
        m.IdTipoOperacion = "COMSPT";
        assertEquals(0, BigDecimal.ZERO.compareTo(
            CalculadoraSpotService.montoResultadoEquivalente(m, new BigDecimal("950"))));
    }

    @Test void mre_modo_d_multiplica_paridad_mercado() {
        MovimientoSpot m = new MovimientoSpot();
        m.MultiplicaODivide = "D";
        m.TipoCambioCierre = new BigDecimal("2");
        m.ParidadMercado = new BigDecimal("450");   // multiply → 2*450=900
        m.ParidadBid = new BigDecimal("1");         // != 0 to pass guard
        m.MontoMonedaPrincipalEquivalente = new BigDecimal("1000000");
        m.IdTipoOperacion = "VENSPT";
        BigDecimal ref = new BigDecimal("850");
        // monto=900; VENSPT → diff=900-850=50; final=50*1000000=50000000
        BigDecimal result = CalculadoraSpotService.montoResultadoEquivalente(m, ref);
        assertEquals(0, new BigDecimal("50000000").compareTo(result));
    }

    // =========================================================================
    // Canary — ensures tests don't silently pass with wrong formula polarity
    // =========================================================================

    @Test void canary_utilidad_compra_asimetrica_con_venta() {
        BigDecimal uc = CalculadoraSpotService.utilidadCompraIncrement(
            new BigDecimal("1000"), new BigDecimal("10"), new BigDecimal("9"));
        BigDecimal uv = CalculadoraSpotService.utilidadVentaIncrement(
            new BigDecimal("1000"), new BigDecimal("9"), new BigDecimal("10"));
        // UC > 0, UV < 0
        assertTrue(uc.compareTo(BigDecimal.ZERO) > 0,
            "UtilidadCompra debe ser positiva cuando precio > cierre");
        assertTrue(uv.compareTo(BigDecimal.ZERO) < 0,
            "UtilidadVenta debe ser negativa cuando cierre < precio");
    }
}
