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
import cl.bcs.valo2.entidades.Indicador;
import cl.bcs.valo2.entidades.Indicador.Clave;

public class ConsultaIndicadoresSH extends Consulta<Clave, Indicador>
        implements ProveedorDatos<Clave, Indicador> {
    private static final Logger LOGGER = Logger.getLogger(ConsultaIndicadoresSH.class.getName());
    private static final String NAME = "BSCL.Plataforma.BD::MER.ParametroFinanciero";

    public ConsultaIndicadoresSH(
            final DataSource dataSource) throws SQLException {
        super(dataSource);
    }

    @Override
    public Indicador buscar(
            final Connection conn,
            final Clave clave)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(NAME);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("IdParametroFinanciero", clave.getNombre());
        select.getWhere().put("Fecha", clave.getFecha().toDate());

        final String sql = sqlGenerator.getSqlText(select);
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(sql);
        }

        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    final Indicador indicador = new Indicador();
                    indicador.setClave(clave);
                    indicador.setValor(rs.getBigDecimal("Valor"));
                    return indicador;
                }
            }
        }

        return null;
    }

}
