package cl.bcs.normativa.cartolas.tools;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.normativa.cartolas.dal.NominaService;
import cl.bcs.normativa.cartolas.dal.ParametroService;
import cl.bcs.normativa.cartolas.entidades.Nomina;
import cl.bcs.normativa.cartolas.entidades.NominaException;
import cl.bcs.normativa.cartolas.entidades.TipoProceso;
import cl.bcs.normativa.cartolas.master.ProcesoEmisionMaster;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.normativa.cartolas.procesos.ProcesoCargaDatos;
import cl.bcs.normativa.procesos.ProcesoException;
import cl.bcs.sql.utils.SqlConnectionSettings;
import cl.bcs.sql.utils.SqlDataSource;
import cl.bcs.utils.PropertiesUtil;

public class ProcesoCartolasApp {

    private static final Logger LOGGER = Logger.getLogger(ProcesoCartolasApp.class.getName());

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
            procesar(dataSource, cartolasProps, 0);
        }

    }

    public static void procesar(
            final DataSource dataSource,
            final Properties properties,
            final long jobId) {
        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            final ContextoCartolas contexto = new ContextoCartolas(connection, properties, jobId);
            final Nomina nomina = contexto.getNomina();

            if (nomina.isCertificada()) {
                throw new NominaException("La nomina ya esta certificada: id=" + nomina.getId());
            }

            nomina.abrir(contexto);

            try {
                final String tipoProceso = contexto.getTipoProceso();
                if (TipoProceso.PROGRAMADO.equals(tipoProceso)) {
                    ParametroService.save(contexto, properties);
                } else if (TipoProceso.NORMAL.equals(tipoProceso)
                        || TipoProceso.RECURRENTE.equals(tipoProceso)) {
                    ParametroService.save(contexto, properties);
                    NominaService.poblar(contexto, contexto.getIdNomina());
                    ProcesoCargaDatos.newInstance(contexto).procesar();
                    ProcesoEmisionMaster.newInstance(contexto, dataSource).procesar();
                } else if (TipoProceso.REPROCESO.equals(tipoProceso)) {
                    ProcesoCargaDatos.newInstance(contexto).procesar();
                    ProcesoEmisionMaster.newInstance(contexto, dataSource).procesar();
                } else if (TipoProceso.REEMPAQUETA.equals(tipoProceso)) {
                    ProcesoEmisionMaster.newInstance(contexto, dataSource).procesar();
                }

                connection.setAutoCommit(true);
                nomina.cerrar(contexto);
            } catch (Exception ex) {
                nomina.cerrar(contexto, ex);
                throw ex;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al procesar cartolas", ex);
            throw new ProcesoException("Error al procesar cartolas: " + ex.getMessage(), ex);
        }
    }

    private ProcesoCartolasApp() {
    }

}
