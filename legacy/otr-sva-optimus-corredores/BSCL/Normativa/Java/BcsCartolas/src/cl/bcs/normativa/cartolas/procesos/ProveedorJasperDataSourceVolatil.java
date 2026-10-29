package cl.bcs.normativa.cartolas.procesos;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import cl.bcs.normativa.cartolas.caller.CallParams;
import cl.bcs.normativa.cartolas.caller.CallTable;
import cl.bcs.normativa.cartolas.caller.ProcedureCaller;
import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.entidades.Nomina;
import cl.bcs.normativa.cartolas.entidades.Recurso;
import cl.bcs.normativa.cartolas.entidades.TipoRecurso;

public class ProveedorJasperDataSourceVolatil extends ProveedorJasperDataSource {

    private final Nomina nomina;
    private final List<Cartola> cartolas;
    private final CargadorDatos cargador;

    public ProveedorJasperDataSourceVolatil(
            final Nomina nomina,
            final List<Cartola> cartolas) {
        this.nomina = nomina;
        this.cartolas = cartolas;
        this.cargador = new CargadorDatosVolatiles(cartolas, this);
    }
    
    public ProveedorJasperDataSourceVolatil(
            final Nomina nomina,
            final List<Cartola> cartolas,
            final CargadorDatosVolatiles cargador) {
        this.nomina = nomina;
        this.cartolas = cartolas;
        this.cargador = cargador;
    }

    @Override
    public void load(
            final Connection connection,
            final String schema,
            final Cartola cartola)
                    throws SQLException,
                    IOException,
                    ParseException {
        final CallParams params = new CallParams(nomina, cartolas, 0, 0);
        final CallTable table = CallTable.newInstance(connection);
        table.load(params);

        try {
            for (final Recurso recurso : nomina.getPlantilla().getRecursos()) {
                if (recurso.getTipo().compareTo(TipoRecurso.PROCEDIMIENTO) == 0) {
                    final String dataSourceName = recurso.getNombre();
                    final String procedureName = recurso.getValor();
                    ProcedureCaller.call(connection, schema, ContextoCartolas.MODULO_CARTOLAS,
                            procedureName, params, cargador, dataSourceName, table.getName());
                }
            }
        } finally {
            table.drop();
        }
    }

}
