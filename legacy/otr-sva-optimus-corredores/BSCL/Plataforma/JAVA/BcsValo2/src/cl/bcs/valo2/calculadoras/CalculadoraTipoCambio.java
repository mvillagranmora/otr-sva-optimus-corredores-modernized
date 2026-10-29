package cl.bcs.valo2.calculadoras;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Date;

import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.fechas.Fecha;
import cl.bcs.valo2.repositorios.RepositorioParidades;

public class CalculadoraTipoCambio {
    private final ValorizadorContexto contexto;
    private final RepositorioParidades repositorioParidades;

    public CalculadoraTipoCambio(
            final ValorizadorContexto contexto) throws SQLException {
        this.contexto = contexto;
        this.repositorioParidades = new RepositorioParidades(contexto);
    }

    public BigDecimal convertir(
            final String monedaOrigen,
            final String monedaDestino,
            final Date fecha,
            final BigDecimal montoOrigen) {
        if (monedaOrigen == null || monedaOrigen.isEmpty()) {
            return null;
        }

        if (monedaDestino == null || monedaDestino.isEmpty()) {
            return null;
        }

        BigDecimal montoDestino = null;
        final BigDecimal tipoCambioOrigen = getTipoCambio(monedaOrigen, fecha);
        final BigDecimal tipoCambioDestino = getTipoCambio(monedaDestino, fecha);
        if (tipoCambioOrigen != null && tipoCambioDestino != null) {
            final double monto = montoOrigen.doubleValue() * tipoCambioOrigen.doubleValue()
                    / tipoCambioDestino.doubleValue();
            montoDestino = new BigDecimal(monto).setScale(contexto.getConfig().getEscalaMontos(),
                    RoundingMode.HALF_UP);
        }

        return montoDestino;
    }

    private BigDecimal getTipoCambio(
            final String moneda,
            final Date fecha) {
        final String monedaSistema = contexto.getConfig().getMonedaSistema();
        if (monedaSistema == null || monedaSistema.isEmpty()) {
            return null;
        }

        if (moneda.equals(monedaSistema)) {
            return BigDecimal.ONE;
        }

        return repositorioParidades
                .buscar(moneda, Fecha.getInstance(fecha), contexto.getConfig().getDiasBusqueda())
                .getValor();
    }
}
