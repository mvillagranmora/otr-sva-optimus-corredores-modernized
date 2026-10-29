package cl.bcs.mesa.service;

import cl.bcs.mesa.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * B-MSP-*: lógica pura de la Calculadora Spot.
 * Source: BcsMesaServicios/src/cl/bcs/mesa/SPT/CalculosCalculadoraSpot.java
 *
 * Todos los métodos son public static para habilitar testing sin contexto Spring.
 */
@Service
public class CalculadoraSpotService {

    /** B-MSP-VIG-*: CalculosCalculadoraSpot.obtenerMovimientosVigentesSpot — line 1030 */
    public static List<MovimientoSpot> filtrarVigentes(List<MovimientoSpot> movimientos) {
        return movimientos.stream()
                .filter(m -> "VIG".equals(m.Estado) || "PEL".equals(m.Estado) || "PDA".equals(m.Estado))
                .collect(Collectors.toList());
    }

    /** B-MSP-INTERB-*: CalculosCalculadoraSpot.obtenerMovimientosInterbancarios — lines 1085-1087 */
    public static List<MovimientoSpot> filtrarInterbancarios(List<MovimientoSpot> vigentes) {
        List<MovimientoSpot> interb = vigentes.stream()
                .filter(m -> "INTERB".equals(m.Nemotecnico) && !"FWD".equals(m.IdTipoCanalIngreso))
                .collect(Collectors.toList());
        List<MovimientoSpot> arbint = vigentes.stream()
                .filter(m -> "ARBINT".equals(m.Nemotecnico) && !"FWD".equals(m.IdTipoCanalIngreso))
                .collect(Collectors.toList());
        List<MovimientoSpot> resultado = new ArrayList<>(interb);
        resultado.addAll(arbint);
        return resultado;
    }

    /** B-MSP-DISTRI-*: CalculosCalculadoraSpot.obtenerMovimientosDistribucion — lines 1265-1271 */
    public static List<MovimientoSpot> filtrarDistribucion(List<MovimientoSpot> vigentes) {
        List<MovimientoSpot> distri = vigentes.stream()
                .filter(m -> "DISTRI".equals(m.Nemotecnico))
                .collect(Collectors.toList());
        List<MovimientoSpot> arbdis = vigentes.stream()
                .filter(m -> "ARBDIS".equals(m.Nemotecnico))
                .collect(Collectors.toList());
        List<MovimientoSpot> resultado = new ArrayList<>(distri);
        resultado.addAll(arbdis);
        return resultado;
    }

    /** B-MSP-RES-*: CalculosCalculadoraSpot.obtenerResumenOperacionesSpot — lines 1368-1388 */
    public static SalidaResumenOperacionesSpot resumenOperaciones(List<MovimientoSpot> movimientos) {
        SalidaResumenOperacionesSpot salida = new SalidaResumenOperacionesSpot();
        movimientos.forEach(m -> {
            if ("ANU".equals(m.Estado)) salida.NumeroAnuladas++;
            if ("COMSPT".equals(m.IdTipoOperacion) && "VIG".equals(m.Estado)) salida.NumeroCompras++;
            if ("VENSPT".equals(m.IdTipoOperacion) && "VIG".equals(m.Estado)) salida.NumeroVentas++;
            if (("COMSPT".equals(m.IdTipoOperacion) || "VENSPT".equals(m.IdTipoOperacion))
                    && ("VIG".equals(m.Estado) || "PEL".equals(m.Estado)
                        || "PDA".equals(m.Estado) || "ANU".equals(m.Estado))) {
                salida.TotalOperaciones++;
            }
        });
        return salida;
    }

    /** B-MSP-POS-*: CalculosCalculadoraSpot.obtenerResultadoPosicionInicial — lines 1000-1013 */
    public static BigDecimal resultadoPosicionInicial(List<PosicionInicial> posiciones, String idMoneda) {
        return posiciones.stream().map(p -> {
            BigDecimal diferencia = p.CierreIngresado.subtract(p.TipoCambioInicial, MathContext.DECIMAL128);
            BigDecimal posicion = idMoneda.equals(p.IdMoneda) ? p.PosicionInicial : BigDecimal.ZERO;
            return diferencia.multiply(posicion, MathContext.DECIMAL128);
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** B-MSP-POS-*: CalculosCalculadoraSpot.obtenerPosicionInicialMoneda — lines 988-997 */
    public static BigDecimal posicionInicialMoneda(List<PosicionInicial> posiciones, String idMoneda) {
        return posiciones.stream()
                .map(p -> idMoneda.equals(p.IdMoneda) ? p.PosicionInicial : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * B-MSP-PROM-*: promedio de compra — lines 1244-1245.
     * Guardado: comprasPuntas == 0 → retorna 0.
     */
    public static BigDecimal promedioDeCompra(BigDecimal montoParaPromedioCompra,
            BigDecimal comprasPuntas) {
        if (comprasPuntas.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return montoParaPromedioCompra.divide(comprasPuntas, MathContext.DECIMAL128);
    }

    /** B-MSP-PROM-*: promedio de venta — guardado: ventasPuntas == 0 → retorna 0. */
    public static BigDecimal promedioDeVenta(BigDecimal montoParaPromedioVenta,
            BigDecimal ventasPuntas) {
        if (ventasPuntas.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return montoParaPromedioVenta.divide(ventasPuntas, MathContext.DECIMAL128);
    }

    /**
     * B-MSP-UTIL-INTERB-*: UtilidadCompra interbancario — line 1160.
     * UtilidadCompra += monto × (precioCalculadora − tipoCambioCierre)
     */
    public static BigDecimal utilidadCompraIncrement(BigDecimal monto, BigDecimal precioCalculadora,
            BigDecimal tipoCambioCierre) {
        return monto.multiply(
            precioCalculadora.subtract(tipoCambioCierre, MathContext.DECIMAL128),
            MathContext.DECIMAL128);
    }

    /**
     * B-MSP-UTIL-INTERB-*: UtilidadVenta interbancario — line 1180.
     * UtilidadVenta += monto × (tipoCambioCierre − precioCalculadora)
     */
    public static BigDecimal utilidadVentaIncrement(BigDecimal monto, BigDecimal tipoCambioCierre,
            BigDecimal precioCalculadora) {
        return monto.multiply(
            tipoCambioCierre.subtract(precioCalculadora, MathContext.DECIMAL128),
            MathContext.DECIMAL128);
    }

    /**
     * B-MSP-UTIL-DISTRI-*: UtilidadCompra distribución — line 1309.
     * UtilidadCompra += monto × (tipoCambioCosto − tipoCambioCierre)
     */
    public static BigDecimal utilidadCompraDistribucion(BigDecimal monto, BigDecimal tipoCambioCosto,
            BigDecimal tipoCambioCierre) {
        return monto.multiply(
            tipoCambioCosto.subtract(tipoCambioCierre, MathContext.DECIMAL128),
            MathContext.DECIMAL128);
    }

    /**
     * B-MSP-UTIL-DISTRI-*: UtilidadVenta distribución — line 1330.
     * UtilidadVenta += monto × (tipoCambioCierre − tipoCambioCosto)
     */
    public static BigDecimal utilidadVentaDistribucion(BigDecimal monto, BigDecimal tipoCambioCierre,
            BigDecimal tipoCambioCosto) {
        return monto.multiply(
            tipoCambioCierre.subtract(tipoCambioCosto, MathContext.DECIMAL128),
            MathContext.DECIMAL128);
    }

    /**
     * B-MSP-UTIL-DISTRI-*: MontoParaPromedioCompra distribución — line 1308.
     * MontoParaPromedioCompra += MontoMonedaPrincipal × TipoCambioCosto
     */
    public static BigDecimal montoParaPromedioCompraDistribucion(BigDecimal monto,
            BigDecimal tipoCambioCosto) {
        return monto.multiply(tipoCambioCosto, MathContext.DECIMAL128);
    }

    /** B-MSP-BE-*: CalculosCalculadoraSpot.CalculoBreakEven — lines 1394-1407 */
    public static BreakEvenResult calcularBreakEven(BigDecimal posicionInicial, BigDecimal precioInicial,
            BigDecimal compras, BigDecimal promCompras,
            BigDecimal ventas, BigDecimal promVentas) {
        BigDecimal equivPesosPosInicial = posicionInicial.multiply(precioInicial)
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal equivPesosCompras = compras.multiply(promCompras);
        BigDecimal equivPesosVentas  = ventas.multiply(promVentas);
        BigDecimal posicionActual = compras.subtract(ventas).add(posicionInicial)
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal equivEnPesos = equivPesosCompras.subtract(equivPesosVentas)
                .add(equivPesosPosInicial)
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal tipoCambioBreakEven =
            (posicionActual.compareTo(BigDecimal.ZERO) != 0
                && equivEnPesos.compareTo(BigDecimal.ZERO) != 0)
                ? equivEnPesos.divide(posicionActual, MathContext.DECIMAL128)
                              .setScale(4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BreakEvenResult result = new BreakEvenResult();
        result.BreakEven  = tipoCambioBreakEven;
        result.MontoTotal = equivEnPesos;
        return result;
    }

    /**
     * B-MSP-PARIDAD-*: CalculosCalculadoraSpot.obtenerParidadAUtilizarPosicionInicial — lines 977-982.
     * BIDASK: posicion ≥ 0 → ParidadBID; else → ParidadASK. Otro tipo → ParidadMercado.
     */
    public static BigDecimal paridadAUtilizar(String tipoParidad, BigDecimal posicion, Paridad paridad) {
        if (paridad == null) return BigDecimal.ZERO;
        if ("BIDASK".equals(tipoParidad)) {
            return posicion.compareTo(BigDecimal.ZERO) != -1 ? paridad.ParidadBID : paridad.ParidadASK;
        }
        return paridad.ParidadMercado;
    }

    /**
     * B-MSP-MRE-*: CalculosCalculadoraSpot.obtenerMontoResultadoEquivalente — lines 1420-1456.
     * M: monto = TipoCambioCierre / ParidadMercado; else: monto = TipoCambioCierre × ParidadMercado.
     * COMSPT: diff = tipoCambioRef − monto; VENSPT: diff = monto − tipoCambioRef.
     * final = diff × MontoMonedaPrincipalEquivalente.
     */
    public static BigDecimal montoResultadoEquivalente(MovimientoSpot m,
            BigDecimal tipoCambioADiferenciar) {
        if (m.ParidadBid.compareTo(BigDecimal.ZERO) == 0
                || m.ParidadMercado.compareTo(BigDecimal.ZERO) == 0
                || m.TipoCambioCierre.compareTo(BigDecimal.ZERO) == 0
                || m.MontoMonedaPrincipalEquivalente.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal monto;
        if ("M".equals(m.MultiplicaODivide)) {
            monto = m.ParidadMercado.compareTo(BigDecimal.ZERO) != 0
                    ? m.TipoCambioCierre.divide(m.ParidadMercado, MathContext.DECIMAL128)
                    : BigDecimal.ZERO;
        } else {
            monto = m.TipoCambioCierre.multiply(m.ParidadMercado, MathContext.DECIMAL128);
        }
        if (tipoCambioADiferenciar != null && tipoCambioADiferenciar.compareTo(BigDecimal.ZERO) != 0) {
            monto = "COMSPT".equals(m.IdTipoOperacion)
                    ? tipoCambioADiferenciar.subtract(monto, MathContext.DECIMAL128)
                    : monto.subtract(tipoCambioADiferenciar, MathContext.DECIMAL128);
        }
        return monto.multiply(m.MontoMonedaPrincipalEquivalente, MathContext.DECIMAL128);
    }
}
