package cl.bcs.valo2.cupones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cl.bcs.valo2.TipoInteres;
import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.entidades.Bono;
import cl.bcs.valo2.entidades.Cupon;
import cl.bcs.valo2.fechas.Fecha;

public class ProveedorCuponesBullet implements ProveedorCupones {

    private static BigDecimal calcularAmortizacion(
            final Bono bono,
            final int numero) {
        return numero == bono.getCantidadCupones() ? new BigDecimal(100) : BigDecimal.ZERO;
    }

    private static Date calcularFechaAnterior(
            final Bono bono,
            final int numero,
            final Date fechaCupon) {
        if (numero == 1 && bono.getFechaDevengo() != null) {
            return bono.getFechaDevengo();
        }

        final Fecha jd = Fecha.getInstance(fechaCupon).restaMeses(bono.getPeriodoPago());
        return jd.toDate();
    }

    private final ValorizadorContexto contexto;

    public ProveedorCuponesBullet(
            final ValorizadorContexto contexto) {
        this.contexto = contexto;
    }

    private BigDecimal calcularIntereses(
            final Bono bono) {
        final double intereses;
        switch (bono.getTipoCalculoInteresesCupon()) {
        case TipoInteres.SIMPLE:
            intereses = calcularInteresesSimple(bono);
            break;
        case TipoInteres.COMPUESTO:
            intereses = calcularInteresesCompuesto(bono);
            break;
        default:
            throw new ProveedorCuponesException(
                    "Tipo de calculo de intereses cupon no implementado", bono);
        }

        return new BigDecimal(intereses).setScale(contexto.getConfig().getEscalaIntereses(),
                RoundingMode.HALF_UP);
    }

    public double calcularInteresesCompuesto(
            final Bono bono) {
        return 100 * (Math.pow(1 + bono.getTasaEmision().doubleValue() / 100,
                1 / bono.calcularCantidadPagosAnuales()) - 1);
    }

    public double calcularInteresesSimple(
            final Bono bono) {
        return bono.getTasaEmision().doubleValue() / bono.calcularCantidadPagosAnuales();
    }

    public BigDecimal calcularSaldoPorAmortizar(
            final Bono bono,
            final int numero) {
        return numero == bono.getCantidadCupones() ? BigDecimal.ZERO : new BigDecimal(100);
    }

    @Override
    public List<Cupon> obtener(
            final Bono bono,
            final Date fecha) {
        final List<Cupon> list = new ArrayList<Cupon>();
        final BigDecimal intereses = calcularIntereses(bono);
        Date fechaCupon = bono.getFechaVencimiento();
        for (int numero = bono.getCantidadCupones(); numero > 0; numero--) {
            final Date fechaAnterior = calcularFechaAnterior(bono, numero, fechaCupon);
            final Cupon cupon = new Cupon();
            cupon.setNumero(numero);
            cupon.setFecha(fechaCupon);
            cupon.setFechaAnterior(fechaAnterior);
            cupon.setIntereses(intereses);
            cupon.setAmortizacion(calcularAmortizacion(bono, numero));
            cupon.setSaldoPorAmortizar(calcularSaldoPorAmortizar(bono, numero));
            list.add(cupon);

            if (fechaAnterior.compareTo(fecha) <= 0) {
                break;
            }

            fechaCupon = fechaAnterior;
        }

        Collections.reverse(list);
        return list;
    }

}
