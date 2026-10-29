package cl.bcs.valo2;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import cl.bcs.sql.datasource.DataSourceProvider;
import cl.bcs.sql.datasource.DataSourceSettings;
import cl.bcs.valo2.data.consultas.Consulta;
import cl.bcs.valo2.data.consultas.ConsultaBuilder;

public class ValorizadorContexto implements Serializable {
    private static final long serialVersionUID = 1L;

    private ValorizadorConfig config;
    private DataSource dataSource;

    public ValorizadorContexto(
            final DataSource dataSource) throws SQLException {
        initialize(dataSource);
    }

    public ValorizadorContexto(
            final Properties props) throws SQLException {
        final String dataSourceType = props.getProperty("datasource.type");
        if ("JDBC".equalsIgnoreCase(dataSourceType)) {
            final DataSourceSettings settings = new DataSourceSettings(props);
            initialize(DataSourceProvider.getInstance(settings));
        }

        this.config = new ValorizadorConfig(props);
    }

    public ValorizadorConfig getConfig() {
        return config;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void initialize(
            final DataSource dataSource)
                    throws SQLException {
        this.dataSource = dataSource;
        this.config = new ValorizadorConfig(newProperties(dataSource));
    }

    private Properties newProperties(
            final DataSource dataSource)
                    throws SQLException {
        final Consulta<String, Properties> consulta = ConsultaBuilder.newConsultaConfig(dataSource);
        return consulta.buscar(null);
    }

    @Override
    public String toString() {
        return "Contexto [config=" + config + "]";
    }
}
