package cl.bcs.valo2.calculadoras;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.valo2.ValorizadorBonosParams;
import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.entidades.Bono;

public final class CalculadoraMedidas extends CalculadoraBonos implements Calculadora {
    private static final Logger LOGGER = Logger.getLogger(CalculadoraMedidas.class.getName());

    public CalculadoraMedidas(
            final ValorizadorContexto contexto) {
        super(contexto);
    }

    @Override
    public void calcular(
            final ValorizadorBonosParams params,
            final Bono bono) {
        params.setTasa360(calcularTasa360(params, bono));
        params.setDuration(calcularDuration(params, bono));
        params.setConvexidad(calcularConvexidad(params, bono));
    }

    private BigDecimal calcularConvexidad(
            final ValorizadorBonosParams params,
            final Bono bono) {
        double convexidad;
        try {
            convexidad = CalculadoraFinanciera.calcularConvexidad(params.getCupones(),
                    params.calcularTasaUnitaria(), bono.calcularCantidadPagosAnuales());
            return new BigDecimal(convexidad).setScale(contexto.getConfig().getEscalaConvexidad(),
                    RoundingMode.HALF_UP);
        } catch (final Exception ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Al calcular convexidad: " + params, ex);
            }

            return null;
        }
    }

    private BigDecimal calcularDuration(
            final ValorizadorBonosParams params,
            final Bono bono) {
        double duration;
        try {
            duration = CalculadoraFinanciera.calcularDuration(params.getCupones(),
                    params.calcularTasaUnitaria(), bono.calcularCantidadPagosAnuales());
            return new BigDecimal(duration).setScale(contexto.getConfig().getEscalaDuration(),
                    RoundingMode.HALF_UP);
        } catch (final Exception ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Al calcular duration: " + params, ex);
            }

            return null;
        }
    }

    private BigDecimal calcularTasa360(
            final ValorizadorBonosParams params,
            final Bono bono) {
        if (bono.getConvencionDias().equalsIgnoreCase("ACT/365")) {
            final double tasa360 = 100
                    * (Math.pow(1 + params.calcularTasaUnitaria(), 360. / 365.) - 1);
            return new BigDecimal(tasa360).setScale(contexto.getConfig().getEscalaTasas(),
                    RoundingMode.HALF_UP);
        } else {
            return null;
        }
    }

}
