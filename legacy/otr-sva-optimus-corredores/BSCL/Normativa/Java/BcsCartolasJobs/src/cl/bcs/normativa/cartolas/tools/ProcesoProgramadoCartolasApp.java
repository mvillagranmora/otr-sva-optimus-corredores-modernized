package cl.bcs.normativa.cartolas.tools;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import cl.bcs.normativa.cartolas.procesos.ProcesoProgramacionCartolas;
import cl.bcs.sql.utils.SqlConnectionSettings;
import cl.bcs.sql.utils.SqlDataSource;
import cl.bcs.utils.PropertiesUtil;

public class ProcesoProgramadoCartolasApp {

    public static void main(
            final String[] args)
                    throws IOException,
                    SQLException,
                    PropertyVetoException {

        final Properties connectionProps = PropertiesUtil.newProperties("connection.properties");
        final SqlConnectionSettings settings = new SqlConnectionSettings(connectionProps, "source");
        final DataSource dataSource = SqlDataSource.getInstance(settings);

        try (final Connection conn = dataSource.getConnection()) {
            final Properties cartolasProps = PropertiesUtil.newProperties("cartolas.properties");
            new ProcesoProgramacionCartolas(conn, cartolasProps).procesar();
        }

    }

}
