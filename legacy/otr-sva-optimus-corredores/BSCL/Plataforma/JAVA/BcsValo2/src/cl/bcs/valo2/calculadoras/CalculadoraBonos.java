package cl.bcs.valo2.calculadoras;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.valo2.TipoInteres;
import cl.bcs.valo2.ValorizadorBonosParams;
import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.ValorizadorException;
import cl.bcs.valo2.entidades.Bono;
import cl.bcs.valo2.entidades.Cupon;

public class CalculadoraBonos {
    private static final Logger LOGGER = Logger.getLogger(CalculadoraBonos.class.getName());

    protected final ValorizadorContexto contexto;

    public CalculadoraBonos(
            final ValorizadorContexto contexto) {
        this.contexto = contexto;
    }

    protected BigDecimal calcularPrecio(
            final ValorizadorBonosParams params) {
        try {
            final Cupon cupon = params.getCupones().get(0);
            final double precio = 100 * params.getProporcionCapital()
                    / cupon.calcularSaldoCapitalUnitario();
            return new BigDecimal(precio).setScale(contexto.getConfig().getEscalaPrecios(),
                    RoundingMode.HALF_UP);
        } catch (final Exception ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Al calcular precio: " + params, ex);
            }

            return null;
        }
    }

    protected double calcularProporcionIntereses(
            final ValorizadorBonosParams params,
            final Bono bono) {
        final Cupon cupon = params.getCupones().get(0);
        final double intereses = cupon.calcularInteresesUnitarios();
        final double anosPeriodo = cupon.getPeriodoEnAnos();
        final double anosTranscurridos = cupon.getPeriodoEnAnos() - cupon.getVencimientoEnAnos();

        double proporcionIntereses;
        switch (bono.getTipoCalculoInteresesDevengados()) {
        case TipoInteres.SIMPLE:
            proporcionIntereses = intereses * anosTranscurridos / anosPeriodo;
            break;
        case TipoInteres.COMPUESTO:
            proporcionIntereses = Math.pow(1 + intereses, anosTranscurridos) - 1;
            break;
        default:
            throw new ValorizadorException(
                    "Tipo de calculo de intereses devengados no implementado", bono);
        }

        return proporcionIntereses;
    }

    protected double calcularProporcionTotal(
            final ValorizadorBonosParams params,
            final Bono bono) {
        return params.getProporcionCapital() + params.getProporcionReajuste()
                + params.getProporcionIntereses();
    }

    protected BigDecimal calcularTasa(
            final ValorizadorBonosParams params,
            final Bono bono) {
        try {
            final double cantidadPagosAnuales = bono.isMercadoLocal() ? 1
                    : bono.calcularCantidadPagosAnuales();
            final double tasa = 100 * CalculadoraFinanciera.calcularTIR(params.getCupones(),
                    params.getProporcionTotal() - params.getProporcionReajuste(),
                    cantidadPagosAnuales);
            return new BigDecimal(tasa).setScale(contexto.getConfig().getEscalaTasas(),
                    RoundingMode.HALF_UP);
        } catch (final Exception ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Al calcular TIR: " + params, ex);
            }

            return null;
        }
    }

}
