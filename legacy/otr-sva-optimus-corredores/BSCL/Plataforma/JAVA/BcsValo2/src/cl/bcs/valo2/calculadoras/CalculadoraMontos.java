package cl.bcs.valo2.calculadoras;

import java.math.BigDecimal;
import java.math.RoundingMode;

import cl.bcs.valo2.ValorizadorBonosParams;
import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.entidades.Bono;

public final class CalculadoraMontos implements Calculadora {
    private final ValorizadorContexto contexto;

    public CalculadoraMontos(
            final ValorizadorContexto contexto) {
        this.contexto = contexto;
    }

    @Override
    public void calcular(
            final ValorizadorBonosParams params,
            final Bono bono) {
        params.setMoneda(bono.getMonedaEmision());
        params.setMontoCapital(calcularMontoCapital(params));
        params.setMontoReajuste(calcularMontoReajuste(params));
        params.setMontoIntereses(calcularMontoIntereses(params));
        params.setMontoTotal(calcularMontoTotal(params));
    }

    private BigDecimal calcularMonto(
            final BigDecimal valor,
            final double proporcion) {
        final double monto = valor.doubleValue() * proporcion;
        return new BigDecimal(monto).setScale(contexto.getConfig().getEscalaMontos(),
                RoundingMode.HALF_UP);
    }

    private BigDecimal calcularMontoCapital(
            final ValorizadorBonosParams params) {
        return calcularMonto(params.getValorNominal(), params.getProporcionCapital());
    }

    private BigDecimal calcularMontoIntereses(
            final ValorizadorBonosParams params) {
        return calcularMonto(params.getValorNominal().add(params.getMontoReajuste()),
                params.getProporcionIntereses());
    }

    private BigDecimal calcularMontoReajuste(
            final ValorizadorBonosParams params) {
        return calcularMonto(params.getValorNominal(), params.getProporcionReajuste());
    }

    private BigDecimal calcularMontoTotal(
            final ValorizadorBonosParams params) {
        return params.getMontoCapital().add(params.getMontoReajuste())
                .add(params.getMontoIntereses());
    }

}
