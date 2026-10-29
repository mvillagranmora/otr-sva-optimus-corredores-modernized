package cl.bcs.valo2.calculadoras;

import java.math.BigDecimal;
import java.sql.SQLException;

import cl.bcs.valo2.ValorizadorBonosParams;
import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.entidades.Bono;

public class CalculadoraPagos implements Calculadora {
    private final CalculadoraTipoCambio calculadoraTipoCambio;

    public CalculadoraPagos(
            final ValorizadorContexto contexto) throws SQLException {
        this.calculadoraTipoCambio = new CalculadoraTipoCambio(contexto);
    }

    @Override
    public void calcular(
            final ValorizadorBonosParams params,
            final Bono bono) {
        final String monedaEmision = bono.getMonedaEmision();
        final String monedaPago = bono.getMonedaPago();
        final BigDecimal montoTotalPago = calculadoraTipoCambio.convertir(monedaEmision, monedaPago,
                params.getFechaValorizacion(), params.getMontoTotal());

        params.setMonedaPago(monedaPago);
        params.setMontoTotalPago(montoTotalPago);
    }

}
