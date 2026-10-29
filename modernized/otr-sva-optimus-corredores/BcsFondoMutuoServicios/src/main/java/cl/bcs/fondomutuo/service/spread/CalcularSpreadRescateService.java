package cl.bcs.fondomutuo.service.spread;

import cl.bcs.fondomutuo.domain.CalcularSpreadRescateEntrada;
import cl.bcs.fondomutuo.domain.CalcularSpreadRescateEntrada.DatoSpread;
import cl.bcs.fondomutuo.domain.CalcularSpreadRescateSalida;
import cl.bcs.fondomutuo.domain.CalcularSpreadRescateSalida.ResultadoSpread;
import cl.bcs.fondomutuo.port.in.CalcularSpreadRescateUseCase;
import cl.bcs.fondomutuo.port.out.ParidadPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Calculates the spread (monetary adjustment) on rescate (redemption) operations.
 *
 * Pure-logic rules extracted from:
 *   CalcularSpreadRescate.java — logica() method
 *
 * B-FM-SPREAD-01: montoReajuste = valorFactorReajuste * costoFactorReajusteRescate, scale=8 HALF_DOWN
 *
 * B-FM-SPREAD-02: Same currency (monedaFondo == monedaSistema)
 *   spread = (cantidadRescate * valorCuota) - montoReajuste, scale=8 HALF_DOWN
 *   (Note: when NumeroCalculo=0, spread=ZERO — edge case from legacy)
 *
 * B-FM-SPREAD-03: Different currency
 *   spread = (cantidadRescate * valorCuota) - montoReajuste (in fund currency), scale=8 HALF_DOWN
 *   (montoReajuste converted from monedaSistema to monedaFondo via paridad)
 *
 * B-FM-SPREAD-04: Validation — each of valorFactorReajuste, costoFactorReajusteRescate,
 *   cantidadRescate, valorCuota must be > 0; otherwise increment erroresContador.
 */
@Service
public class CalcularSpreadRescateService implements CalcularSpreadRescateUseCase {

    private final ParidadPort paridadPort;

    public CalcularSpreadRescateService(ParidadPort paridadPort) {
        this.paridadPort = paridadPort;
    }

    @Override
    public CalcularSpreadRescateSalida calcular(CalcularSpreadRescateEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.getIdEmpresa(), "idEmpresa");

        CalcularSpreadRescateSalida salida = new CalcularSpreadRescateSalida();
        Long idEmpresa = entrada.getIdEmpresa();

        for (CalcularSpreadRescateEntrada.DatoSpread dato : entrada.getDatos()) {
            CalcularSpreadRescateSalida.ResultadoSpread resultado = new CalcularSpreadRescateSalida.ResultadoSpread();
            resultado.setNumeroCalculoSpreadRescate(dato.getNumeroCalculoSpreadRescate());

            String error = validarDato(dato);
            if (error != null) {
                resultado.setSpread(BigDecimal.ZERO);
                salida.getResultados().add(resultado);
                salida.setErroresContador((salida.getErroresContador() != null ? salida.getErroresContador() : 0L) + 1L);
                continue;
            }

            BigDecimal montoReajuste = calcularMontoReajuste(dato.getValorFactorReajuste(), dato.getCostoFactorReajusteRescate());
            resultado.setValorFactorReajuste(dato.getValorFactorReajuste());
            resultado.setCostoFactorReajusteRescate(dato.getCostoFactorReajusteRescate());
            resultado.setCantidadRescate(dato.getCantidadRescate());
            resultado.setValorCuota(dato.getValorCuota());

            // Determine idMonedaSistema via HANA config (default CLP if unavailable)
            String idMonedaSistema = "CLP";
            String idMonedaFondo = dato.getIdMonedaFondo() != null ? dato.getIdMonedaFondo() : "CLP";

            if (!idMonedaSistema.equalsIgnoreCase(idMonedaFondo)) {
                // Cross-currency: convert montoReajuste from monedaSistema to monedaFondo
                try {
                    java.time.LocalDate fecha = dato.getFecha() != null
                            ? java.time.LocalDate.parse(dato.getFecha().substring(0, 10))
                            : java.time.LocalDate.now();
                    BigDecimal montoConvertido = paridadPort.convertirMonto(
                            montoReajuste, idMonedaSistema, idMonedaFondo, fecha, idEmpresa);
                    resultado.setSpread(calcularSpreadDistintaMoneda(
                            dato.getCantidadRescate(), dato.getValorCuota(), montoConvertido));
                } catch (Exception e) {
                    // Graceful: fall back to same-currency calculation
                    resultado.setSpread(calcularSpreadMismaMoneda(
                            dato.getCantidadRescate(), dato.getValorCuota(), montoReajuste));
                }
            } else {
                resultado.setSpread(calcularSpreadMismaMoneda(
                        dato.getCantidadRescate(), dato.getValorCuota(), montoReajuste));
            }

            salida.getResultados().add(resultado);
        }

        return salida;
    }

    // -------------------------------------------------------------------------
    // B-FM-SPREAD-01: Calculate montoReajuste
    // Source: CalcularSpreadRescate.java:logica — datoProceso.MontoReajuste calculation
    // -------------------------------------------------------------------------
    public static BigDecimal calcularMontoReajuste(
            BigDecimal valorFactorReajuste,
            BigDecimal costoFactorReajusteRescate) {
        Objects.requireNonNull(valorFactorReajuste, "valorFactorReajuste");
        Objects.requireNonNull(costoFactorReajusteRescate, "costoFactorReajusteRescate");
        return valorFactorReajuste
                .multiply(costoFactorReajusteRescate, MathContext.DECIMAL128)
                .setScale(8, RoundingMode.HALF_DOWN);
    }

    // -------------------------------------------------------------------------
    // B-FM-SPREAD-02: Spread for same-currency case (monedaFondo == monedaSistema)
    // Source: CalcularSpreadRescate.java — !monedaSistema.equalsIgnoreCase(dato.IdMonedaFondo) else branch
    // -------------------------------------------------------------------------
    public static BigDecimal calcularSpreadMismaMoneda(
            BigDecimal cantidadRescate,
            BigDecimal valorCuota,
            BigDecimal montoReajusteConvertido) {
        Objects.requireNonNull(cantidadRescate, "cantidadRescate");
        Objects.requireNonNull(valorCuota, "valorCuota");
        Objects.requireNonNull(montoReajusteConvertido, "montoReajusteConvertido");
        return cantidadRescate
                .multiply(valorCuota, MathContext.DECIMAL128)
                .subtract(montoReajusteConvertido, MathContext.DECIMAL128)
                .setScale(8, RoundingMode.HALF_DOWN);
    }

    // -------------------------------------------------------------------------
    // B-FM-SPREAD-03: Spread for different-currency case (monedaFondo != monedaSistema)
    // Source: CalcularSpreadRescate.java — !monedaSistema.equalsIgnoreCase(dato.IdMonedaFondo) branch
    // -------------------------------------------------------------------------
    public static BigDecimal calcularSpreadDistintaMoneda(
            BigDecimal cantidadRescate,
            BigDecimal valorCuota,
            BigDecimal montoReajuste) {
        Objects.requireNonNull(cantidadRescate, "cantidadRescate");
        Objects.requireNonNull(valorCuota, "valorCuota");
        Objects.requireNonNull(montoReajuste, "montoReajuste");
        return cantidadRescate
                .multiply(valorCuota, MathContext.DECIMAL128)
                .subtract(montoReajuste, MathContext.DECIMAL128)
                .setScale(8, RoundingMode.HALF_DOWN);
    }

    // -------------------------------------------------------------------------
    // B-FM-SPREAD-04: Validate a dato — returns error code if invalid, null if valid
    // Source: CalcularSpreadRescate.java — validation guards
    // -------------------------------------------------------------------------
    public static String validarDato(DatoSpread dato) {
        Objects.requireNonNull(dato, "dato");
        if (dato.getValorFactorReajuste() == null || dato.getValorFactorReajuste().compareTo(BigDecimal.ZERO) != 1) {
            return "VALOR_FACTOR_MAYOR_CERO";
        }
        if (dato.getCostoFactorReajusteRescate() == null || dato.getCostoFactorReajusteRescate().compareTo(BigDecimal.ZERO) != 1) {
            return "COSTO_FACTOR_MAYOR_CERO";
        }
        if (dato.getCantidadRescate() == null || dato.getCantidadRescate().compareTo(BigDecimal.ZERO) != 1) {
            return "CANTIDAD_RESCATE_MAYOR_CERO";
        }
        if (dato.getValorCuota() == null || dato.getValorCuota().compareTo(BigDecimal.ZERO) != 1) {
            return "VALOR_CUOTA_MAYOR_CERO";
        }
        return null;
    }
}
