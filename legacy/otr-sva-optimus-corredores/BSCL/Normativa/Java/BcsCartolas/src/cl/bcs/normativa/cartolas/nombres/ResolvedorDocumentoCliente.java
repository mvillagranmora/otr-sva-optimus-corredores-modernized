package cl.bcs.normativa.cartolas.nombres;

import java.sql.SQLException;

import cl.bcs.normativa.cartolas.dal.ComunService;
import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.entidades.Cliente;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;

public final class ResolvedorDocumentoCliente extends ResolvedorNombre {

    @Override
    public String resolver(
            final ContextoCartolas contexto,
            final Cartola cartola)
                    throws SQLException {
        Cliente cliente = ComunService.getCliente(contexto.getConnection(),
                contexto.getSchema(), cartola.getIdCliente());
        return cliente.getDocumentoIdentidad();
    }

}
