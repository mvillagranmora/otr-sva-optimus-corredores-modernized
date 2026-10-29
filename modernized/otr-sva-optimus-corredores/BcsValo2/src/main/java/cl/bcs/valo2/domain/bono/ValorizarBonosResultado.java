package cl.bcs.valo2.domain.bono;

import cl.bcs.valo2.domain.cupon.Cupon;
import java.math.BigDecimal;
import java.util.List;

public record ValorizarBonosResultado(
        BigDecimal precio,
        BigDecimal tasa,
        BigDecimal tasa360,
        BigDecimal duration,
        BigDecimal convexidad,
        BigDecimal montoCapital,
        BigDecimal montoIntereses,
        BigDecimal montoReajuste,
        BigDecimal montoTotal,
        BigDecimal montoTotalPago,
        double proporcionCapital,
        double proporcionIntereses,
        double proporcionReajuste,
        double proporcionTotal,
        List<Cupon> cupones,
        int codigoRetorno,
        String mensaje
) {}
