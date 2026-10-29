package cl.bcs.valo2.data.consultas.sqlserver;

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
import cl.bcs.valo2.data.consultas.sqlserver.ConsultaInstrumentoSS.Instrumento;

public class ConsultaInstrumentoSS extends Consulta<String, Instrumento>
        implements ProveedorDatos<String, Instrumento> {

    public static class Instrumento {
        private String calendario;
        private String codigoSerie;
        private Date fechaEmision;
        private Date fechaVencimiento;
        private String nemo;

        public String getCalendario() {
            return calendario;
        }

        public String getCodigoSerie() {
            return codigoSerie;
        }

        public Date getFechaEmision() {
            return fechaEmision;
        }

        public Date getFechaVencimiento() {
            return fechaVencimiento;
        }

        public String getNemo() {
            return nemo;
        }

        public void setCalendario(
                final String calendario) {
            this.calendario = calendario;
        }

        public void setCodigoSerie(
                final String codigoSerie) {
            this.codigoSerie = codigoSerie;
        }

        public void setFechaEmision(
                final Date fechaEmision) {
            this.fechaEmision = fechaEmision;
        }

        public void setFechaVencimiento(
                final Date fechaVencimiento) {
            this.fechaVencimiento = fechaVencimiento;
        }

        public void setNemo(
                final String nemo) {
            this.nemo = nemo;
        }

    }

    private static final Logger LOGGER = Logger.getLogger(ConsultaInstrumentoSS.class.getName());
    public static final String NAME = "TBBEMI";

    private static Instrumento newInstrumento(
            final ResultSet rs)
                    throws SQLException {
        final Instrumento instrumento = new Instrumento();
        instrumento.setNemo(rs.getString("NEMO"));
        instrumento.setCalendario(rs.getString("RAZON_SOCIAL"));
        instrumento.setCodigoSerie(rs.getString("COD_SERIE"));
        instrumento.setFechaEmision(rs.getDate("FEC_EMI"));
        instrumento.setFechaVencimiento(rs.getDate("FEC_VCTO"));
        return instrumento;
    }

    public ConsultaInstrumentoSS(
            final DataSource dataSource) throws SQLException {
        super(dataSource);
    }

    @Override
    public Instrumento buscar(
            final Connection conn,
            final String nemo)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(NAME);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("NEMO", nemo);

        final String sql = sqlGenerator.getSqlText(select);
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(sql);
        }

        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return newInstrumento(rs);
                }
            }
        }

        return null;
    }

}
