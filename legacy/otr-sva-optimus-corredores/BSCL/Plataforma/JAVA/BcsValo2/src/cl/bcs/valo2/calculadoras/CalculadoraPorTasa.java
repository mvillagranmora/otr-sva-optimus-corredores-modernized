package cl.bcs.valo2.calculadoras;

import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.valo2.ValorizadorBonosParams;
import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.entidades.Bono;

public final class CalculadoraPorTasa extends CalculadoraBonos implements Calculadora {
    private static final Logger LOGGER = Logger.getLogger(CalculadoraPorTasa.class.getName());

    public CalculadoraPorTasa(
            final ValorizadorContexto contexto) {
        super(contexto);
    }

    @Override
    public void calcular(
            final ValorizadorBonosParams params,
            final Bono bono) {
        params.setProporcionTotal(calcularProporcionTotal(params, bono));
        params.setProporcionIntereses(calcularProporcionIntereses(params, bono));
        params.setProporcionCapital(calcularProporcionCapital(params));
        params.setPrecio(calcularPrecio(params));
    }

    protected double calcularProporcionCapital(
            final ValorizadorBonosParams params) {
        return params.getProporcionTotal() - params.getProporcionIntereses();
    }

    @Override
    protected double calcularProporcionTotal(
            final ValorizadorBonosParams params,
            final Bono bono) {
        double proporcionTotal;
        try {
            proporcionTotal = CalculadoraFinanciera.calcularValorPresente(params.getCupones(),
                    params.calcularTasaUnitaria(), bono.calcularCantidadPagosAnuales());
        } catch (final Exception ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Al calcular valor presente: " + params, ex);
            }

            proporcionTotal = Double.NaN;
        }

        return proporcionTotal;
    }

}
