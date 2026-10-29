package cl.bcs.valo2.data.consultas.sqlserver;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.data.consultas.Consulta;
import cl.bcs.valo2.data.consultas.sqlserver.ConsultaSerieSS.Serie;

public class ConsultaSerieSS extends Consulta<String, Serie>
        implements ProveedorDatos<String, Serie> {
    public static class Serie {

        private int cantidadCupones;
        private String codigoMoneda;
        private String codigoSerie;
        private int orden;
        private int periodoPago;
        private BigDecimal tasaEmision;
        private String tipoAmortizacion;
        private Date fechaPrepago;

        public int getCantidadCupones() {
            return cantidadCupones;
        }

        public String getCodigoMoneda() {
            return codigoMoneda;
        }

        public String getCodigoSerie() {
            return codigoSerie;
        }

        public Date getFechaPrepago() {
            return fechaPrepago;
        }

        public int getOrden() {
            return orden;
        }

        public int getPeriodoPago() {
            return periodoPago;
        }

        public BigDecimal getTasaEmision() {
            return tasaEmision;
        }

        public String getTipoAmortizacion() {
            return tipoAmortizacion;
        }

        public void setCantidadCupones(
                final int cantidadCupones) {
            this.cantidadCupones = cantidadCupones;

        }

        public void setCodigoMoneda(
                final String codigoMoneda) {
            this.codigoMoneda = codigoMoneda;

        }

        public void setCodigoSerie(
                final String codigoSerie) {
            this.codigoSerie = codigoSerie;
        }

        public void setFechaPrepago(
                final Date fechaPrepago) {
            this.fechaPrepago = fechaPrepago;
        }

        public void setOrden(
                final int orden) {
            this.orden = orden;

        }

        public void setPeriodoPago(
                final int periodoPago) {
            this.periodoPago = periodoPago;

        }

        public void setTasaEmision(
                final BigDecimal tasaEmision) {
            this.tasaEmision = tasaEmision;

        }

        public void setTipoAmortizacion(
                final String tipoAmortizacion) {
            this.tipoAmortizacion = tipoAmortizacion;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ConsultaSerieSS.class.getName());
    public static final String NAME = "TBBESE";

    private static Serie newSerie(
            final ResultSet rs)
                    throws SQLException {
        final Serie serie = new Serie();
        serie.setTasaEmision(rs.getBigDecimal("TASA_NOMI_EMI"));
        serie.setPeriodoPago(rs.getInt("PERI_PAGO_CUPON"));
        serie.setCantidadCupones(rs.getInt("NUM_CUPON"));
        serie.setCodigoMoneda(rs.getString("MONEDA"));
        serie.setCodigoSerie(rs.getString("COD_SERIE"));
        serie.setOrden(rs.getInt("ORDEN"));
        serie.setTipoAmortizacion(rs.getString("TIPO_AMORT"));
        serie.setFechaPrepago(rs.getDate("FEC_COM_PPAG"));
        return serie;
    }

    public ConsultaSerieSS(
            final DataSource dataSource) throws SQLException {
        super(dataSource);
    }

    @Override
    public Serie buscar(
            final Connection conn,
            final String codigoSerie)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(NAME);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("COD_SERIE", codigoSerie);

        final String sql = sqlGenerator.getSqlText(select);
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(sql);
        }

        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return newSerie(rs);
                }
            }
        }

        return null;
    }

}
