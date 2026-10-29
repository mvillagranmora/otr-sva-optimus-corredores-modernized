package cl.bcs.valo2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import cl.bcs.valo2.calculadoras.CalculadoraPagares;
import cl.bcs.valo2.calculadoras.CalculadoraTipoCambio;
import cl.bcs.valo2.fechas.Fecha;
import cl.bcs.valo2.fechas.contadores.ContadorDias;
import cl.bcs.valo2.fechas.contadores.ContadorDiasParams;

public class ValorizadorPagares {
    private final CalculadoraTipoCambio calculadoraTipoCambio;
    private final ContadorDias contadorDias;
    private final ValorizadorContexto contexto;

    public ValorizadorPagares(
            final DataSource dataSource) throws SQLException {
        this(new ValorizadorContexto(dataSource));
    }

    public ValorizadorPagares(
            final ValorizadorContexto contexto) throws SQLException {
        this.contexto = contexto;
        this.contadorDias = new ContadorDias(contexto);
        this.calculadoraTipoCambio = new CalculadoraTipoCambio(contexto);
    }

    private int calcularDiasVencimiento(
            final Fecha fechaCaptacion,
            final Fecha fechaVencimiento) {
        final ContadorDiasParams params = new ContadorDiasParams();
        params.setAjusteEOM(false);
        params.setCalendario(null);
        params.setConvencionDias("ACT/365");
        params.setFechaVencimiento(fechaVencimiento);
        return contadorDias.calcularDiasCorridos(fechaCaptacion, fechaVencimiento, params);
    }

    private int calcularDiasVencimiento(
            final ValorizadorPagaresParams params) {
        return calcularDiasVencimiento(Fecha.getInstance(params.getFechaCaptacion()),
                Fecha.getInstance(params.getFechaVencimiento()));
    }

    private Date calcularFechaVencimiento(
            final ValorizadorPagaresParams params) {
        return Fecha.getInstance(params.getFechaCaptacion()).sumarDias(params.getDiasVencimiento())
                .toDate();
    }

    private BigDecimal calcularTasa(
            final ValorizadorPagaresParams params) {
        final double tasa = 100 * CalculadoraPagares.calcularTasa(params.getTipoInteres(),
                params.getDiasAno(), params.getDiasVencimiento(),
                params.getValorCaptacion().doubleValue(), params.getValorRescate().doubleValue());
        return new BigDecimal(tasa).setScale(contexto.getConfig().getEscalaTasas(),
                RoundingMode.HALF_UP);
    }

    private BigDecimal calcularValorCaptacion(
            final ValorizadorPagaresParams params) {
        final double valorCaptacion = CalculadoraPagares.calcularCaptacion(params.getTipoInteres(),
                params.getDiasAno(), params.getDiasVencimiento(),
                params.getValorRescate().doubleValue(), params.getTasa().doubleValue() / 100);
        return new BigDecimal(valorCaptacion).setScale(contexto.getConfig().getEscalaMontos(),
                RoundingMode.HALF_UP);
    }

    private BigDecimal calcularValorCaptacionPago(
            final ValorizadorPagaresParams params) {
        return calculadoraTipoCambio.convertir(params.getMoneda(), params.getMonedaPago(),
                params.getFechaCaptacion(), params.getValorCaptacion());
    }

    private BigDecimal calcularValorRescate(
            final ValorizadorPagaresParams params) {
        final double valorRescate = CalculadoraPagares.calcularRescate(params.getTipoInteres(),
                params.getDiasAno(), params.getDiasVencimiento(),
                params.getValorCaptacion().doubleValue(), params.getTasa().doubleValue() / 100);
        return new BigDecimal(valorRescate).setScale(contexto.getConfig().getEscalaMontos(),
                RoundingMode.HALF_UP);
    }

    private BigDecimal calcularValorRescatePago(
            final ValorizadorPagaresParams params) {
        return calculadoraTipoCambio.convertir(params.getMoneda(), params.getMonedaPago(),
                params.getFechaVencimiento(), params.getValorRescate());
    }

    public void valorizar(
            final ValorizadorPagaresParams params) {
        if (params.getDiasVencimiento() == 0) {
            params.setDiasVencimiento(calcularDiasVencimiento(params));
        } else {
            params.setFechaVencimiento(calcularFechaVencimiento(params));
        }

        if (params.getTasa() == null) {
            params.setTasa(calcularTasa(params));
        } else if (params.getValorCaptacion() == null) {
            params.setValorCaptacion(calcularValorCaptacion(params));
        } else {
            params.setValorRescate(calcularValorRescate(params));
        }

        params.setValorCaptacionPago(calcularValorCaptacionPago(params));
        params.setValorRescatePago(calcularValorRescatePago(params));
        
        if(params.getFechaVencimiento() != null && params.getFechaCaptacion() != null) {
			params.setDuration(new BigDecimal((params.getFechaVencimiento().getTime()-params.getFechaCaptacion().getTime())/86400000));
        }
    }
}
