package cl.bcs.normativa.cartolas.tools;

import java.beans.PropertyVetoException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Properties;

import javax.sql.DataSource;

import cl.bcs.normativa.cartolas.master.CartolaMaster;
import cl.bcs.normativa.cartolas.master.GeneradorMaster;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.sql.utils.SqlConnectionSettings;
import cl.bcs.sql.utils.SqlDataSource;
import cl.bcs.sql.utils.SqlUtil;
import cl.bcs.utils.PropertiesUtil;
import net.sf.jasperreports.engine.JRException;

public final class EmisorOnlineApp {

    public static void main(
            final String[] args)
                    throws IOException,
                    SQLException,
                    JRException,
                    PropertyVetoException,
                    ParseException {
        final Properties connectionProps = PropertiesUtil.newProperties("connection.properties");
        final SqlConnectionSettings settings = new SqlConnectionSettings(connectionProps, "source");
        final DataSource dataSource = SqlDataSource.getInstance(settings);

        try (final Connection conn = dataSource.getConnection()) {
            final Properties cartolasProps = PropertiesUtil.newProperties("cartolas.properties");
            final ContextoCartolas contexto = new ContextoCartolas(conn, cartolasProps, 0);
            final GeneradorMaster generador = new GeneradorMaster(contexto, dataSource);

            final long idCliente = 395174;
            final long idPortafolio = 343710;
            final Date fechaDesde = SqlUtil.getDate("2016-09-01");
            final Date fechaHasta = SqlUtil.getDate("2016-09-30");
            final CartolaMaster cartolaMaster = generador.generarOnline(idCliente, idPortafolio,
                    fechaDesde, fechaHasta);
            try (final OutputStream out = new FileOutputStream(
                    "C:/Users/v_fpino/Documents/Proyectos/bcs/Cartolas/Salidas/"
                            + cartolaMaster.getNombreArchivo())) {
                out.write(cartolaMaster.getBytes());
            }
        }
    }

    private EmisorOnlineApp() {
    }

}
