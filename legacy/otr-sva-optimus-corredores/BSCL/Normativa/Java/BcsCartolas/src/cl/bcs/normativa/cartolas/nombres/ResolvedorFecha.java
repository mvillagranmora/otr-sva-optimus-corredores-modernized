package cl.bcs.normativa.cartolas.nombres;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;

public final class ResolvedorFecha extends ResolvedorNombre {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Override
    public String resolver(
            final ContextoCartolas contexto,
            final Cartola cartola) {
        return DATE_FORMAT.format(new Date());
    }

}
