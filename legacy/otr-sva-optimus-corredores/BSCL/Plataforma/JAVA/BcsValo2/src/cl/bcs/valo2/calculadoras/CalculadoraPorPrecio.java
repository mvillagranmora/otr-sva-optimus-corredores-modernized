package cl.bcs.valo2.calculadoras;

import java.math.BigDecimal;

import cl.bcs.valo2.ValorizadorBonosParams;
import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.entidades.Bono;
import cl.bcs.valo2.entidades.Cupon;

public class CalculadoraPorPrecio extends CalculadoraBonos implements Calculadora {
    public CalculadoraPorPrecio(
            final ValorizadorContexto contexto) {
        super(contexto);
    }

    @Override
    public void calcular(
            final ValorizadorBonosParams params,
            final Bono bono) {
        if (bono.isEnFechaVencimiento(params.getFechaValorizacion())) {
            params.setPrecio(new BigDecimal(100));
        }

        params.setProporcionCapital(calcularProporcionCapital(params));
        params.setProporcionIntereses(calcularProporcionIntereses(params, bono));
        params.setProporcionTotal(calcularProporcionTotal(params, bono));
        params.setTasa(calcularTasa(params, bono));
    }

    private double calcularProporcionCapital(
            final ValorizadorBonosParams params) {
        final Cupon cupon = params.getCupones().get(0);
        return cupon.calcularSaldoCapitalUnitario() * params.calcularPrecioUnitario();
    }

}
