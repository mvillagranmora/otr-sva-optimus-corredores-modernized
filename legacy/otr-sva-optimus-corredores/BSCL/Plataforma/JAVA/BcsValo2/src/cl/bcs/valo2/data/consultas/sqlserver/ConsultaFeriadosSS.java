package cl.bcs.valo2.data.consultas.sqlserver;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.data.consultas.Consulta;
import cl.bcs.valo2.fechas.Fecha;

public class ConsultaFeriadosSS extends Consulta<String, Set<Fecha>>
        implements ProveedorDatos<String, Set<Fecha>> {
    private static final Logger LOGGER = Logger.getLogger(ConsultaFeriadosSS.class.getName());
    private static final String NAME = "TBBEFECHAS";

    public ConsultaFeriadosSS(
            final DataSource dataSource) throws SQLException {
        super(dataSource);
    }

    @Override
    public Set<Fecha> buscar(
            final Connection conn,
            final String calendario)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(NAME);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("PAIS", calendario);
        select.getWhere().put("TIPO_FEC", "F");

        final String sql = sqlGenerator.getSqlText(select);
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(sql);
        }

        final Set<Fecha> set = new HashSet<Fecha>();
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                rs.setFetchSize(1000);
                while (rs.next()) {
                    final Date fecha = rs.getDate("DIA");
                    final Fecha julianDate = Fecha.getInstance(fecha);
                    set.add(julianDate);
                }
            }
        }

        return set;
    }

}
