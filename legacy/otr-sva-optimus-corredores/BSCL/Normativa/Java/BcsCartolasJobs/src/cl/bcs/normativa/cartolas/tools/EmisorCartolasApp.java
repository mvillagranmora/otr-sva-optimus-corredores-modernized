package cl.bcs.normativa.cartolas.tools;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import cl.bcs.normativa.cartolas.master.ProcesoEmisionMaster;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.sql.utils.SqlConnectionSettings;
import cl.bcs.sql.utils.SqlDataSource;
import cl.bcs.utils.PropertiesUtil;

public final class EmisorCartolasApp {

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
            final Proceso proceso = ProcesoEmisionMaster.newInstance(conn, cartolasProps, 0,
                    dataSource);

            final long start = System.currentTimeMillis();
            proceso.procesar();
            final long elapsed = (System.currentTimeMillis() - start) / 1000;

            final String msg = String.format("Total time: %s", elapsed);
            System.out.println(msg);
        }
    }

    private EmisorCartolasApp() {
    }

}
