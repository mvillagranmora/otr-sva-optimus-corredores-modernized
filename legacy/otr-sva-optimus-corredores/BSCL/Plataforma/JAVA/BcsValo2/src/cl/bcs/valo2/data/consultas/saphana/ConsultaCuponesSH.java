package cl.bcs.valo2.data.consultas.saphana;

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
import cl.bcs.valo2.entidades.Cupon;

public class ConsultaCuponesSH extends Consulta<String, List<Cupon>>
        implements ProveedorDatos<String, List<Cupon>> {
    private static final Logger LOGGER = Logger.getLogger(ConsultaCuponesSH.class.getName());
    private static final String NAME = "BSCL.Plataforma.BD::INS.Cupon";

    private static Cupon newCupon(
            final ResultSet rs)
                    throws SQLException {
        final Cupon cupon = new Cupon();
        cupon.setNemo(rs.getString("Nemotecnico"));
        cupon.setNumero(rs.getInt("Numero"));
        cupon.setFecha(rs.getDate("Fecha"));
        cupon.setCall("S".equalsIgnoreCase(rs.getString("Call")));
        cupon.setAmortizacion(rs.getBigDecimal("Amortizacion"));
        cupon.setIntereses(rs.getBigDecimal("Intereses"));
        cupon.setSaldoPorAmortizar(rs.getBigDecimal("SaldoPorAmortizar"));
        return cupon;
    }

    public ConsultaCuponesSH(
            final DataSource dataSource) throws SQLException {
        super(dataSource);
    }

    @Override
    public List<Cupon> buscar(
            final Connection conn,
            final String nemo)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(NAME);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("Nemotecnico", nemo);
        select.getOrderBy().add(new SqlOrderByCondition("Fecha", "ASC"));

        final String sql = sqlGenerator.getSqlText(select);
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(sql);
        }

        final List<Cupon> list = new ArrayList<Cupon>();
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(newCupon(rs));
                }
            }
        }

        return list;
    }

}
