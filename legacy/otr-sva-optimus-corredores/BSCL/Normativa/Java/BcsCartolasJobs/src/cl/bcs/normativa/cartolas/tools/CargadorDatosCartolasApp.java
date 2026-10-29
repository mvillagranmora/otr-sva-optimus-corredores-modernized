package cl.bcs.normativa.cartolas.tools;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import cl.bcs.normativa.cartolas.procesos.ProcesoCargaDatos;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.sql.utils.SqlConnectionBuilder;
import cl.bcs.sql.utils.SqlConnectionSettings;
import cl.bcs.utils.PropertiesUtil;

public final class CargadorDatosCartolasApp {

    public static void main(
            final String[] args)
                    throws IOException,
                    SQLException {
        final Properties connProps = PropertiesUtil.newProperties("connection.properties");
        final SqlConnectionSettings connSettings = new SqlConnectionSettings(connProps, "source");
        try (final Connection conn = SqlConnectionBuilder.getConnection(connSettings)) {
            final Properties properties = PropertiesUtil.newProperties("cartolas.properties");
            final Proceso proceso = ProcesoCargaDatos.newInstance(conn, properties, 0);
            proceso.procesar();
        }
    }

    private CargadorDatosCartolasApp() {
    }

}
