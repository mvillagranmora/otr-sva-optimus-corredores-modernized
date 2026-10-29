package cl.bcs.valo2.calculadoras;

import cl.bcs.valo2.ValorizadorBonosParams;
import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.entidades.Bono;

public class CalculadoraPorMonto extends CalculadoraBonos implements Calculadora {

    public CalculadoraPorMonto(
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
        params.setTasa(calcularTasa(params, bono));
        params.setPrecio(calcularPrecio(params));
    }

    private double calcularProporcionCapital(
            final ValorizadorBonosParams params) {
        return params.getProporcionTotal() - params.getProporcionReajuste()
                - params.getProporcionIntereses();
    }

    @Override
    protected double calcularProporcionTotal(
            final ValorizadorBonosParams params,
            final Bono bono) {
        return params.getMontoTotal().doubleValue() / params.getValorNominal().doubleValue();
    }

}
