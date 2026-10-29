package cl.bcs.valo2.data.consultas.saphana;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.data.consultas.Consulta;

public class ConsultaConfigSH extends Consulta<String, Properties>
        implements ProveedorDatos<String, Properties> {
    private static final Logger LOGGER = Logger.getLogger(ConsultaConfigSH.class.getName());
    private static final String NAME = "BSCL.Plataforma.BD::VAL.Config";

    public ConsultaConfigSH(
            final DataSource dataSource) throws SQLException {
        super(dataSource);
    }

    @Override
    public Properties buscar(
            final Connection conn,
            final String key)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(NAME);
        final SqlSelect select = new SqlSelect(table);

        final String sql = sqlGenerator.getSqlText(select);
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(sql);
        }

        final Properties props = new Properties();
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    final String nombre = rs.getString("Nombre");
                    final String valor = rs.getString("Valor");
                    props.setProperty(nombre, valor);
                }
            }
        }

        return props;
    }

}
