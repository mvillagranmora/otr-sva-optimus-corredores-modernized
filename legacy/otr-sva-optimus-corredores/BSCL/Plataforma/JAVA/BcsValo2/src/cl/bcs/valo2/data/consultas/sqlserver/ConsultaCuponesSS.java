package cl.bcs.valo2.data.consultas.sqlserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlOrderByCondition;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.data.consultas.Consulta;
import cl.bcs.valo2.data.consultas.sqlserver.ConsultaInstrumentoSS.Instrumento;
import cl.bcs.valo2.entidades.Cupon;

public class ConsultaCuponesSS extends Consulta<String, List<Cupon>>
        implements ProveedorDatos<String, List<Cupon>> {
    private static final Logger LOGGER = Logger.getLogger(ConsultaCuponesSS.class.getName());
    private static final String NAME = "TBBETD";

    private static Cupon newCupon(
            final ResultSet rs,
            final Instrumento instrumento)
                    throws SQLException {
        final Cupon cupon = new Cupon();
        cupon.setNemo(instrumento.getNemo());
        cupon.setNumero(rs.getInt("NUM_CUPON"));
        cupon.setFecha(rs.getDate("FEC_PAGO"));
        cupon.setCall(false);
        cupon.setAmortizacion(rs.getBigDecimal("AMORTIZ"));
        cupon.setIntereses(rs.getBigDecimal("INTERES"));
        cupon.setSaldoPorAmortizar(rs.getBigDecimal("SALDO_AMORT"));
        return cupon;
    }

    private final ConsultaInstrumentoSS consultaInstrumento;

    public ConsultaCuponesSS(
            final DataSource dataSource) throws SQLException {
        super(dataSource);
        this.consultaInstrumento = new ConsultaInstrumentoSS(dataSource);
    }

    @Override
    public List<Cupon> buscar(
            final Connection conn,
            final String nemo)
                    throws SQLException {
        final List<Cupon> list = new ArrayList<Cupon>();

        final Instrumento instrumento = consultaInstrumento.buscar(conn, nemo);
        if (instrumento != null) {
            final SqlTable table = SqlMetadata.newTable(NAME);
            final SqlSelect select = new SqlSelect(table);
            select.getWhere().put("COD_SERIE", instrumento.getCodigoSerie());
            select.getOrderBy().add(new SqlOrderByCondition("FEC_PAGO", "ASC"));

            final String sql = sqlGenerator.getSqlText(select);
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(sql);
            }

            try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
                try (final ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(newCupon(rs, instrumento));
                    }
                }
            }
        }

        return list;
    }

}
