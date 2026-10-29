package cl.bcs.valo2.cupones;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.entidades.Bono;
import cl.bcs.valo2.entidades.Cupon;

public class ProveedorCuponesCall extends ProveedorCuponesVariable {

    public ProveedorCuponesCall(
            final ValorizadorContexto contexto) throws SQLException {
        super(contexto);
    }

    public int buscarIndiceCuponCall(
            final List<Cupon> cupones,
            final Date fecha) {
        for (int i = 0; i < cupones.size(); i++) {
            final Cupon cupon = cupones.get(i);
            if (cupon.isCall() && cupon.getFecha().compareTo(fecha) >= 0) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public List<Cupon> obtener(
            final Bono bono,
            final Date fecha) {
        final List<Cupon> cupones = super.obtener(bono, fecha);
        procesarCall(cupones, fecha);
        return cupones;
    }

    public void procesarCall(
            final List<Cupon> cupones,
            final Date fecha) {
        final int x = buscarIndiceCuponCall(cupones, fecha);
        if (x >= 0) {
            for (int i = cupones.size() - 1; i > x; i--) {
                cupones.remove(i);
            }

            final Cupon cuponCall = cupones.get(x);
            cuponCall.setAmortizacion(
                    cuponCall.getAmortizacion().add(cuponCall.getSaldoPorAmortizar()));
            cuponCall.setSaldoPorAmortizar(BigDecimal.ZERO);
        }
    }
}
