package cl.bcs.normativa.cartolas.procesos;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.entidades.Datos;
import cl.bcs.normativa.cartolas.entidades.FormatoDatos;

public class CargadorDatosVolatiles extends CargadorDatos {

    private final ProveedorJasperDataSource proveedor;
    private final List<Cartola> cartolas;

    public CargadorDatosVolatiles(
            final List<Cartola> cartolas,
            final ProveedorJasperDataSource proveedor) {
        this.cartolas = cartolas;
        this.proveedor = proveedor;
    }

    @Override
    public void load(
            final String nombre,
            final ResultSet resultSet)
                    throws SQLException,
                    IOException {
        final Map<Long, List<String[]>> map = buildMap(resultSet);
        for (final Long idPortafolio : map.keySet()) {
            final Cartola cartola = Cartola.buscarCartolaPorIdPortafolio(cartolas, idPortafolio);
            final List<String[]> list = map.get(idPortafolio);
            final String valor = serialize(list);
            final Datos datos = Datos.newInstance(cartola, nombre, FormatoDatos.CSV, valor);
            proveedor.put(datos);
        }
    }

}
