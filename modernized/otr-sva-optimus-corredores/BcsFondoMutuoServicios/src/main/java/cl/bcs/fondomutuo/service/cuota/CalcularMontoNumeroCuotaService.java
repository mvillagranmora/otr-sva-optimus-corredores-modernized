package cl.bcs.fondomutuo.service.cuota;

import cl.bcs.fondomutuo.domain.CalcularMontoNumeroCuotaEntrada;
import cl.bcs.fondomutuo.domain.CalcularMontoNumeroCuotaEntrada.SerieSolicitud;
import cl.bcs.fondomutuo.domain.CalcularMontoNumeroCuotaSalida;
import cl.bcs.fondomutuo.domain.CalcularMontoNumeroCuotaSalida.ResultadoCuota;
import cl.bcs.fondomutuo.domain.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.port.in.CalcularMontoNumeroCuotaUseCase;
import cl.bcs.fondomutuo.port.out.HanaFondoMutuoPort;
import cl.bcs.fondomutuo.port.out.ParidadPort;
import cl.bcs.fondomutuo.port.out.ValorCuotaPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Calculates monto ↔ número de cuotas for mutual fund subscriptions/redemptions.
 *
 * Pure-logic rules extracted from:
 *   CalcularMontoNumeroCuota.java — logica() method
 *
 * B-FM-CUOTA-01: Monto → cuotas (same currency)
 *   numeroCuotas = monto / valorCuota, scale=decimalesCuota, HALF_DOWN
 *
 * B-FM-CUOTA-02: Cuotas → monto (same currency)
 *   monto = numeroCuotas * valorCuota (MathContext.DECIMAL128)
 *
 * B-FM-CUOTA-03: RESREINV with zero cuotas gets extra decimal place (HALF_UP)
 *   numeroCuotas = monto / valorCuota, scale=decimalesCuota+1, HALF_UP
 *
 * B-FM-CUOTA-04: valorCuota == ZERO → error response (ERROR_VALOR_CUOTA)
 *
 * HANA-dependent operations (prices, paridad) are @Disabled in tests.
 */
@Service
public class CalcularMontoNumeroCuotaService implements CalcularMontoNumeroCuotaUseCase {

    private final ValorCuotaPort valorCuotaPort;
    private final ParidadPort paridadPort;
    private final HanaFondoMutuoPort hanaPort;

    public CalcularMontoNumeroCuotaService(ValorCuotaPort valorCuotaPort,
                                           ParidadPort paridadPort,
                                           HanaFondoMutuoPort hanaPort) {
        this.valorCuotaPort = valorCuotaPort;
        this.paridadPort = paridadPort;
        this.hanaPort = hanaPort;
    }

    @Override
    public CalcularMontoNumeroCuotaSalida calcular(CalcularMontoNumeroCuotaEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.getIdEmpresa(), "idEmpresa");

        CalcularMontoNumeroCuotaSalida salida = new CalcularMontoNumeroCuotaSalida();
        Long idEmpresa = entrada.getIdEmpresa();

        // Collect unique series IDs
        List<String> seriesIds = entrada.getSeries().stream()
                .map(CalcularMontoNumeroCuotaEntrada.SerieSolicitud::getIdSerieFondo)
                .filter(Objects::nonNull).distinct().toList();

        // Load valor cuota data from HANA
        List<cl.bcs.fondomutuo.domain.ValorCuota> vcList = valorCuotaPort.findBySeries(seriesIds, idEmpresa);
        java.util.Map<String, cl.bcs.fondomutuo.domain.ValorCuota> vcMap = vcList.stream()
                .collect(java.util.stream.Collectors.toMap(
                        cl.bcs.fondomutuo.domain.ValorCuota::idSerieFondo, v -> v, (a, b) -> a));

        if (vcMap.isEmpty()) {
            for (CalcularMontoNumeroCuotaEntrada.SerieSolicitud serie : entrada.getSeries()) {
                CalcularMontoNumeroCuotaSalida.ResultadoCuota r = buildErrorResultado(
                        serie.getIdInterno(), serie.getIdSerieFondo());
                salida.getResultados().add(r);
            }
            return salida;
        }

        for (CalcularMontoNumeroCuotaEntrada.SerieSolicitud serie : entrada.getSeries()) {
            CalcularMontoNumeroCuotaSalida.ResultadoCuota resultado = new CalcularMontoNumeroCuotaSalida.ResultadoCuota();
            resultado.setIdInterno(serie.getIdInterno());
            resultado.setTipoSolicitud(serie.getTipoSolicitud());

            cl.bcs.fondomutuo.domain.ValorCuota vc = vcMap.get(serie.getIdSerieFondo());
            if (vc == null || vc.valorCuota() == null || vc.valorCuota().compareTo(BigDecimal.ZERO) == 0) {
                resultado.setCodigoMensaje("ERROR_VALOR_CUOTA");
                resultado.setResultado("Error al obtener Valor Cuota : " + serie.getIdSerieFondo());
                salida.getResultados().add(resultado);
                continue;
            }

            BigDecimal valorCuota = vc.valorCuota();
            resultado.setValorCuota(valorCuota);
            resultado.setValorParidad(BigDecimal.ONE);

            String idMonedaFondo = serie.getIdMonedaFondo() != null ? serie.getIdMonedaFondo() : "";
            String idMonedaInversion = serie.getIdMonedaInversion() != null ? serie.getIdMonedaInversion() : "";
            boolean diferenteCurrency = !idMonedaInversion.equalsIgnoreCase(idMonedaFondo)
                    && !idMonedaInversion.isEmpty() && !idMonedaFondo.isEmpty();

            boolean porMonto = "M".equalsIgnoreCase(serie.getCalculoPorMontoCuota());
            BigDecimal monto = serie.getMonto() != null ? serie.getMonto() : BigDecimal.ZERO;
            BigDecimal numeroCuotas = serie.getNumeroCuotas();

            if (diferenteCurrency) {
                // Cross-currency: use paridad service
                try {
                    java.time.LocalDate fecha = serie.getFecha() != null
                            ? java.time.LocalDate.parse(serie.getFecha().substring(0, 10))
                            : java.time.LocalDate.now();
                    String monedaOrigen = porMonto ? idMonedaInversion : idMonedaFondo;
                    String monedaDestino = porMonto ? idMonedaFondo : idMonedaInversion;
                    BigDecimal montoOrigen = porMonto ? monto : (numeroCuotas != null
                            ? valorCuota.multiply(numeroCuotas, java.math.MathContext.DECIMAL128)
                            : BigDecimal.ZERO);

                    BigDecimal paridad = paridadPort.calcularParidad(monedaOrigen, monedaDestino, fecha, idEmpresa);
                    resultado.setValorParidad(paridad);

                    if (porMonto) {
                        BigDecimal montoDestino = paridadPort.convertirMonto(monto, idMonedaInversion, idMonedaFondo, fecha, idEmpresa);
                        if (valorCuota.compareTo(BigDecimal.ZERO) != 0) {
                            resultado.setNumeroCuotas(montoDestino.divide(valorCuota, java.math.MathContext.DECIMAL128)
                                    .setScale(2, RoundingMode.HALF_DOWN));
                        }
                        resultado.setMonto(montoDestino);
                        resultado.setMontoDestino(monto);
                    } else {
                        BigDecimal montoCuotas = numeroCuotas != null
                                ? valorCuota.multiply(numeroCuotas, java.math.MathContext.DECIMAL128) : BigDecimal.ZERO;
                        resultado.setMonto(montoCuotas);
                        resultado.setMontoDestino(paridadPort.convertirMonto(montoCuotas, idMonedaFondo, idMonedaInversion, fecha, idEmpresa));
                        resultado.setNumeroCuotas(numeroCuotas);
                    }
                } catch (Exception e) {
                    resultado.setCodigoMensaje("ERROR_PARIDAD");
                    resultado.setResultado("Error al calcular paridad: " + e.getMessage());
                    salida.getResultados().add(resultado);
                    continue;
                }
            } else {
                // Same currency — pure logic
                if (porMonto) {
                    if (valorCuota.compareTo(BigDecimal.ZERO) == 0) {
                        resultado.setCodigoMensaje("ERROR_VALOR_CUOTA");
                        resultado.setResultado("Error al obtener Valor Cuota : " + serie.getIdSerieFondo());
                        salida.getResultados().add(resultado);
                        continue;
                    }
                    resultado.setNumeroCuotas(calcularNumeroCuotasDesdeMonto(monto, valorCuota, 2));
                    resultado.setMonto(monto);
                    resultado.setMontoDestino(monto);
                } else {
                    BigDecimal nc = numeroCuotas != null ? numeroCuotas : BigDecimal.ZERO;
                    BigDecimal montoCuotas = calcularMontoDesdeCuotas(nc, valorCuota);
                    resultado.setMonto(montoCuotas);
                    resultado.setMontoDestino(montoCuotas);
                    resultado.setNumeroCuotas(nc);
                }
            }

            salida.getResultados().add(resultado);
        }

        return salida;
    }

    // -------------------------------------------------------------------------
    // B-FM-CUOTA-01: Monto → cuotas (same currency, no paridad)
    // Source: CalcularMontoNumeroCuota.java — else branch when diferido=N, calculoPorMontoCuota=M
    // -------------------------------------------------------------------------
    public static BigDecimal calcularNumeroCuotasDesdeMonto(
            BigDecimal monto,
            BigDecimal valorCuota,
            int decimalesCuota) {
        Objects.requireNonNull(monto, "monto");
        Objects.requireNonNull(valorCuota, "valorCuota");
        if (valorCuota.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("valorCuota cannot be zero");
        }
        return monto.divide(valorCuota, MathContext.DECIMAL128)
                    .setScale(decimalesCuota, RoundingMode.HALF_DOWN);
    }

    // -------------------------------------------------------------------------
    // B-FM-CUOTA-02: Cuotas → monto (same currency, no paridad)
    // Source: CalcularMontoNumeroCuota.java — NumeroCuotas != null branch, MontoPorCuota calc
    // -------------------------------------------------------------------------
    public static BigDecimal calcularMontoDesdeCuotas(
            BigDecimal numeroCuotas,
            BigDecimal valorCuota) {
        Objects.requireNonNull(numeroCuotas, "numeroCuotas");
        Objects.requireNonNull(valorCuota, "valorCuota");
        return valorCuota.multiply(numeroCuotas, MathContext.DECIMAL128);
    }

    // -------------------------------------------------------------------------
    // B-FM-CUOTA-03: RESREINV special case — zero cuotas → extra decimal HALF_UP
    // Source: CalcularMontoNumeroCuota.java — parallelStream idTipoMovimientoFondoMutuo = RESREINV
    // -------------------------------------------------------------------------
    public static BigDecimal calcularNumeroCuotasResreinv(
            BigDecimal monto,
            BigDecimal valorCuota,
            int decimalesCuota) {
        Objects.requireNonNull(monto, "monto");
        Objects.requireNonNull(valorCuota, "valorCuota");
        if (valorCuota.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("valorCuota cannot be zero");
        }
        return monto.divide(valorCuota, MathContext.DECIMAL128)
                    .setScale(decimalesCuota + 1, RoundingMode.HALF_UP);
    }

    // -------------------------------------------------------------------------
    // B-FM-CUOTA-04: Error result when valorCuota is zero
    // Source: CalcularMontoNumeroCuota.java — if(resultado.ValorCuota.compareTo(BigDecimal.ZERO) == 0)
    // -------------------------------------------------------------------------
    public static ResultadoCuota buildErrorResultado(Long idInterno, String idSerieFondo) {
        ResultadoCuota r = new ResultadoCuota();
        r.setIdInterno(idInterno);
        r.setCodigoMensaje("ERROR_VALOR_CUOTA");
        r.setResultado("Error al obtener Valor Cuota : " + idSerieFondo);
        return r;
    }

    // -------------------------------------------------------------------------
    // B-FM-CUOTA-05: Determine si la operación requiere conversión de moneda
    // Source: CalcularMontoNumeroCuota.java — diferido = S when idMonedaInversion != idMonedaFondo
    // -------------------------------------------------------------------------
    public static boolean requiereConversionMoneda(String idMonedaInversion, String idMonedaFondo) {
        Objects.requireNonNull(idMonedaInversion, "idMonedaInversion");
        Objects.requireNonNull(idMonedaFondo, "idMonedaFondo");
        return !idMonedaInversion.equalsIgnoreCase(idMonedaFondo);
    }

    // -------------------------------------------------------------------------
    // B-FM-CUOTA-06: Determine tipo de cambio según tipo de solicitud
    // Source: CalcularMontoNumeroCuota.java — TipoCambio = APO ? ValorDolarAporte : ValorDolarRescate
    // -------------------------------------------------------------------------
    public static BigDecimal seleccionarTipoCambio(
            String tipoSolicitud,
            BigDecimal valorDolarAporte,
            BigDecimal valorDolarRescate) {
        Objects.requireNonNull(tipoSolicitud, "tipoSolicitud");
        return ConstantesFondoMutuo.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(tipoSolicitud)
               ? valorDolarAporte
               : valorDolarRescate;
    }
}
