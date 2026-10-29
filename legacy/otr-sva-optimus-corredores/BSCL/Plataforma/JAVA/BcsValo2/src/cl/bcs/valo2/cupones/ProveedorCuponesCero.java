package cl.bcs.valo2.cupones;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cl.bcs.valo2.entidades.Bono;
import cl.bcs.valo2.entidades.Cupon;

public class ProveedorCuponesCero implements ProveedorCupones {

	@Override
	public List<Cupon> obtener(Bono bono, Date fecha) {
		
		//solo si esta dentro del rasngo de valorizacion
		
		List<Cupon> list = new ArrayList<Cupon>();
		list.add(getCupon(bono));
		return list;
	}

	private Cupon getCupon(Bono bono) {
		Cupon cupon = new Cupon();
		cupon.setAmortizacion(new BigDecimal(100));
		cupon.setCall(false);
		cupon.setFecha(bono.getFechaVencimiento());
		cupon.setFechaAnterior(bono.getFechaDevengo());
		cupon.setNumero(1);
		cupon.setSaldoPorAmortizar(BigDecimal.ZERO);
		return cupon;
	}

}
