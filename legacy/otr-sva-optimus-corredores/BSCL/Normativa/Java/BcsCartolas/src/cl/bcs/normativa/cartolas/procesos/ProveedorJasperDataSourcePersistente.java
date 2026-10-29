package cl.bcs.normativa.cartolas.procesos;

import java.sql.Connection;
import java.sql.SQLException;

import cl.bcs.normativa.cartolas.dal.DatosService;
import cl.bcs.normativa.cartolas.entidades.Cartola;

public class ProveedorJasperDataSourcePersistente extends ProveedorJasperDataSource {

    @Override
    public void load(
            final Connection connection,
            final String schema,
            final Cartola cartola)
                    throws SQLException {
        this.datosCartola = DatosService.lookupByIdCartola(connection, schema, cartola.getId());
    }

}
