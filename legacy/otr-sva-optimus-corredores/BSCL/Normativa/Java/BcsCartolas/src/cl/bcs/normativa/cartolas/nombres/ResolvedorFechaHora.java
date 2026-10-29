package cl.bcs.normativa.cartolas.nombres;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;

public final class ResolvedorFechaHora extends ResolvedorNombre {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    @Override
    public String resolver(
            final ContextoCartolas contexto,
            final Cartola cartola) {
        return DATE_FORMAT.format(new Date());
    }

}
