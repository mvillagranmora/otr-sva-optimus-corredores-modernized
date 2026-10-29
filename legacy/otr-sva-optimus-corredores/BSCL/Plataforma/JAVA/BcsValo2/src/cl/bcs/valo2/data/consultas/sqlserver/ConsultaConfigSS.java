package cl.bcs.valo2.data.consultas.sqlserver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.data.consultas.Consulta;

public class ConsultaConfigSS extends Consulta<String, Properties>
        implements ProveedorDatos<String, Properties> {

    public ConsultaConfigSS(
            final DataSource dataSource) throws SQLException {
        super(dataSource);
    }

    @Override
    public Properties buscar(
            final Connection conn,
            final String key)
                    throws SQLException {
        // No esta la configuracion en SQL Server.
        // Se asumira valores por defecto.
        return new Properties();
    }

}
