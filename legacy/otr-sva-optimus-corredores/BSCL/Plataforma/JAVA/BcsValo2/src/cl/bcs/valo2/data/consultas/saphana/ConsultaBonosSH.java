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
import cl.bcs.valo2.entidades.Bono;

public class ConsultaBonosSH extends Consulta<String, Bono>implements ProveedorDatos<String, Bono> {
    private static final Logger LOGGER = Logger.getLogger(ConsultaBonosSH.class.getName());
    public static final String NAME = "BSCL.Plataforma.BD.Vistas::VAL.ConsultarBonos";

    private static Bono newBono(
            final ResultSet rs)
                    throws SQLException {
        final Bono bono = new Bono();
        bono.setNemo(rs.getString("Nemotecnico"));
        bono.setTasaEmision(rs.getBigDecimal("TasaEmision"));
        bono.setPeriodoPago(rs.getInt("PeriodoPago"));
        bono.setCantidadCupones(rs.getInt("NumeroCupones"));
        bono.setMonedaEmision(rs.getString("IdTipoReajuste"));
        bono.setMonedaPago(rs.getString("IdMoneda"));
        bono.setCuponeraVariable("S".equalsIgnoreCase(rs.getString("CuponeraVariable")));
        bono.setCallConfirmado("S".equalsIgnoreCase(rs.getString("CallConfirmado")));
        bono.setTipoCalculoInteresesCupon(rs.getInt("TipoCalculoInteresesCupon"));
        bono.setTipoCalculoInteresesDevengados(rs.getInt("TipoCalculoInteresesDevengados"));
        bono.setTipoInflacion(rs.getString("IdTipoInflacion"));
        bono.setTenorInflacion(rs.getString("TenorInflacion"));
        bono.setTipoLibor(rs.getString("TipoLibor"));
        bono.setFechaDevengo(rs.getDate("FechaDevengo"));
        bono.setFechaVencimiento(rs.getDate("FechaVencimiento"));
        bono.setCalendario(rs.getString("IdCalendario"));
        bono.setConvencionDias(rs.getString("ConvencionDias"));
        bono.setAjusteFinDeMes("S".equalsIgnoreCase(rs.getString("AjusteFinDeMes")));
        bono.setMercadoLocal("S".equalsIgnoreCase(rs.getString("MercadoLocal")));
        return bono;
    }

    public ConsultaBonosSH(
            final DataSource dataSource) throws SQLException {
        super(dataSource);
    }

    @Override
    public Bono buscar(
            final Connection conn,
            final String nemo)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(NAME);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("Nemotecnico", nemo);

        final String sql = sqlGenerator.getSqlText(select);
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(sql);
        }

        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    final Bono bono = newBono(rs);
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info(bono.toString());
                    }

                    return bono;
                }
            }
        }

        return null;
    }

}
