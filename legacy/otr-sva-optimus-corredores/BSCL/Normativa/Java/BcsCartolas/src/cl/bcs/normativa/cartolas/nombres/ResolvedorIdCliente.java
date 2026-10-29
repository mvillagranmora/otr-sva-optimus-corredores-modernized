package cl.bcs.normativa.cartolas.nombres;

import java.sql.SQLException;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;

public final class ResolvedorIdCliente extends ResolvedorNombre {

    @Override
    public String resolver(
            final ContextoCartolas contexto,
            final Cartola cartola)
                    throws SQLException {
        return String.format("%d", cartola.getIdCliente());
    }

}
