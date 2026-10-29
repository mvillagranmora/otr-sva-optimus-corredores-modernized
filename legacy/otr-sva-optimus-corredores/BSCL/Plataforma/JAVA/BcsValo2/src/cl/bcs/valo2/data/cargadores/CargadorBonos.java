package cl.bcs.valo2.data.cargadores;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import cl.bcs.sql.utils.SqlBulk;
import cl.bcs.sql.utils.SqlUtil;
import cl.bcs.valo2.entidades.Bono;

public class CargadorBonos extends SqlBulk {
    private static final String INSERT_COMMAND;

    static {
        final StringBuilder sb = new StringBuilder(256);
        sb.append("INSERT INTO ")
                .append(SqlUtil.toQuotedString("BSCL.Plataforma.BD::INS.InstrumentoBono"))
                .append('(').append(SqlUtil.toQuotedString("IdInstrumento")).append(",")
                .append(SqlUtil.toQuotedString("FechaDevengo")).append(",")
                .append(SqlUtil.toQuotedString("AjusteFinDeMes")).append(",")
                .append(SqlUtil.toQuotedString("CallConfirmado")).append(",")
                .append(SqlUtil.toQuotedString("ConvencionDias")).append(",")
                .append(SqlUtil.toQuotedString("CuponeraVariable")).append(",")
                .append(SqlUtil.toQuotedString("TasaEmision")).append(",")
                .append(SqlUtil.toQuotedString("IdTipoInflacion")).append(",")
                .append(SqlUtil.toQuotedString("TenorInflacion")).append(",")
                .append(SqlUtil.toQuotedString("TipoLibor")).append(",")
                .append(SqlUtil.toQuotedString("TipoCalculoInteresesCupon")).append(",")
                .append(SqlUtil.toQuotedString("TipoCalculoInteresesDevengados")).append(",")
                .append(SqlUtil.toQuotedString("FechaPrepago")).append(",")
                .append(SqlUtil.toQuotedString("CUSIP")).append(",")
                .append(SqlUtil.toQuotedString("COMMON")).append(",")
                .append(SqlUtil.toQuotedString("SEDOL")).append(",")
                .append(SqlUtil.toQuotedString("TickerBloomberg")).append(",")
                .append(SqlUtil.toQuotedString("MercadoLocal")).append(",")
                .append(SqlUtil.toQuotedString("MercadoExtrabursatil")).append(",")
                .append(SqlUtil.toQuotedString("MercadoExtranjero")).append(",")
                .append(SqlUtil.toQuotedString("PresenciaBursatil")).append(",")
                .append(SqlUtil.toQuotedString("IdPaisTransaccion")).append(",")
                .append(SqlUtil.toQuotedString("IdCalendario")).append(",")
                .append(SqlUtil.toQuotedString("ValorBolsa")).append(",")
                .append(SqlUtil.toQuotedString("ValorBloomberg")).append(",")
                .append(SqlUtil.toQuotedString("DurationBolsa")).append(",")
                .append(SqlUtil.toQuotedString("DurationBloomberg")).append(",")
                .append(SqlUtil.toQuotedString("FechaValorizacion")).append(",")
                .append(SqlUtil.toQuotedString("ValorNominal")).append(",")
                .append(SqlUtil.toQuotedString("PrecioValorizacion"))
                .append(") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        INSERT_COMMAND = sb.toString();
    }

    public CargadorBonos(
            final Connection conn,
            final int batchSize) throws SQLException {
        super(conn, INSERT_COMMAND, batchSize);
    }

    public void cargar(
            final Collection<Bono> bonos,
            final long startId)
                    throws SQLException {
        long id = startId;
        for (final Bono bono : bonos) {
            grabar(++id, bono);
        }

        endBatch();
    }

    private void grabar(
            final long id,
            final Bono bono)
                    throws SQLException {
        final PreparedStatement bulkStatement = getBulkStatement();
        bulkStatement.clearParameters();
        bulkStatement.setLong(1, id);
        bulkStatement.setDate(2, new java.sql.Date(bono.getFechaDevengo().getTime()));
        bulkStatement.setString(3, bono.isAjusteFinDeMes() ? "S" : "N");
        bulkStatement.setString(4, bono.isCallConfirmado() ? "S" : "N");
        bulkStatement.setString(5, bono.getConvencionDias());
        bulkStatement.setString(6, bono.isCuponeraVariable() ? "S" : "N");
        bulkStatement.setBigDecimal(7, bono.getTasaEmision());
        bulkStatement.setString(8, bono.getTipoInflacion());
        bulkStatement.setString(9, bono.getTenorInflacion());
        bulkStatement.setString(10, bono.getTipoLibor());
        bulkStatement.setInt(11, bono.getTipoCalculoInteresesCupon());
        bulkStatement.setInt(12, bono.getTipoCalculoInteresesDevengados());
        bulkStatement.setDate(13, null);
        bulkStatement.setString(14, null);
        bulkStatement.setString(15, null);
        bulkStatement.setString(16, null);
        bulkStatement.setString(17, null);
        bulkStatement.setString(18, "N");
        bulkStatement.setString(19, "N");
        bulkStatement.setString(20, "N");
        bulkStatement.setString(21, "N");
        bulkStatement.setInt(22, 0);
        bulkStatement.setString(23, bono.getCalendario());
        bulkStatement.setBigDecimal(24, BigDecimal.ZERO);
        bulkStatement.setBigDecimal(25, BigDecimal.ZERO);
        bulkStatement.setBigDecimal(26, BigDecimal.ZERO);
        bulkStatement.setBigDecimal(27, BigDecimal.ZERO);
        bulkStatement.setDate(28, null);
        bulkStatement.setBigDecimal(29, BigDecimal.ZERO);
        bulkStatement.setBigDecimal(30, BigDecimal.ZERO);
        addBatch();
    }

}
