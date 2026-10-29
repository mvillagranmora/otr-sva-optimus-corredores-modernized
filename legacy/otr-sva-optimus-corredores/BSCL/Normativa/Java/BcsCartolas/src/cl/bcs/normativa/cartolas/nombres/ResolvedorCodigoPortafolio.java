package cl.bcs.normativa.cartolas.nombres;

import java.sql.SQLException;

import cl.bcs.normativa.cartolas.dal.ComunService;
import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;

public final class ResolvedorCodigoPortafolio extends ResolvedorNombre {

    @Override
    public String resolver(
            final ContextoCartolas contexto,
            final Cartola cartola)
                    throws SQLException {
        return ComunService.getCodigoPortafolio(contexto.getConnection(), contexto.getSchema(),
                cartola.getIdPortafolio());
    }

}
