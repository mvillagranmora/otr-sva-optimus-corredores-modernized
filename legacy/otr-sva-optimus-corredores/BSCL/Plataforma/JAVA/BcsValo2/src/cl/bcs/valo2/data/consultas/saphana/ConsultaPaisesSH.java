package cl.bcs.valo2.data.consultas.saphana;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.data.consultas.Consulta;

public class ConsultaPaisesSH extends Consulta<String, Integer>
        implements ProveedorDatos<String, Integer> {
    private static final Logger LOGGER = Logger.getLogger(ConsultaPaisesSH.class.getName());
    public static final String NAME = "BSCL.Plataforma.BD::REG.Pais";

    public ConsultaPaisesSH(
            final DataSource dataSource) throws SQLException {
        super(dataSource);
    }

    @Override
    public Integer buscar(
            final Connection conn,
            final String codigoPais)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(NAME);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("CodigoPais", codigoPais);

        final String sql = sqlGenerator.getSqlText(select);
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(sql);
        }

        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("IdPais");
                }
            }
        }

        return 0;
    }

}
