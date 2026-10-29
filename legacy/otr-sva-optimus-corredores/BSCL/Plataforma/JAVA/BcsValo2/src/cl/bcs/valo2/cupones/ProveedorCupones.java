package cl.bcs.valo2.cupones;

import java.util.Date;
import java.util.List;

import cl.bcs.valo2.entidades.Bono;
import cl.bcs.valo2.entidades.Cupon;

public interface ProveedorCupones {

    List<Cupon> obtener(
            final Bono bono,
            final Date fecha);

}
