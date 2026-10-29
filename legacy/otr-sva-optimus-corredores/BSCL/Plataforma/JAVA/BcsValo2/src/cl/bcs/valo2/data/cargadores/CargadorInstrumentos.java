package cl.bcs.valo2.data.cargadores;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;

import cl.bcs.sql.utils.SqlBulk;
import cl.bcs.sql.utils.SqlUtil;
import cl.bcs.utils.DateUtil;
import cl.bcs.valo2.entidades.Bono;

public class CargadorInstrumentos extends SqlBulk {
    private static final String INSERT_COMMAND;

    static {
        final StringBuilder sb = new StringBuilder(256);
        sb.append("INSERT INTO ")
                .append(SqlUtil.toQuotedString("BSCL.Plataforma.BD::INS.Instrumento")).append('(')
                .append(SqlUtil.toQuotedString("IdInstrumento")).append(",")
                .append(SqlUtil.toQuotedString("Nemotecnico")).append(",")
                .append(SqlUtil.toQuotedString("ISIN")).append(",")
                .append(SqlUtil.toQuotedString("CorteMinimo")).append(",")
                .append(SqlUtil.toQuotedString("EstaInscrito")).append(",")
                .append(SqlUtil.toQuotedString("EsDivisible")).append(",")
                .append(SqlUtil.toQuotedString("RazonSocial")).append(",")
                .append(SqlUtil.toQuotedString("Lista")).append(",")
                .append(SqlUtil.toQuotedString("FechaEmision")).append(",")
                .append(SqlUtil.toQuotedString("FechaVencimiento")).append(",")
                .append(SqlUtil.toQuotedString("IdInstrumentoRelacionado")).append(",")
                .append(SqlUtil.toQuotedString("IdEmisor")).append(",")
                .append(SqlUtil.toQuotedString("IdTipoReajuste")).append(",")
                .append(SqlUtil.toQuotedString("Reajustable")).append(",")
                .append(SqlUtil.toQuotedString("IdMoneda")).append(",")
                .append(SqlUtil.toQuotedString("IdTipoInstrumento")).append(",")
                .append(SqlUtil.toQuotedString("IdEstado")).append(",")
                .append(SqlUtil.toQuotedString("DecimalesPrecio")).append(",")
                .append(SqlUtil.toQuotedString("Tamano")).append(",")
                .append(SqlUtil.toQuotedString("FechaInicioVigencia")).append(",")
                .append(SqlUtil.toQuotedString("TasaFiscal")).append(",")
                .append(SqlUtil.toQuotedString("IndArt104")).append(",")
                .append(SqlUtil.toQuotedString("IdEntidadClasificadoraRiesgo")).append(",")
                .append(SqlUtil.toQuotedString("ValorClasificacionRiesgo")).append(",")
                .append(SqlUtil.toQuotedString("TipoValorizacion")).append(",")
                .append(SqlUtil.toQuotedString("Tasa")).append(",")
                .append(SqlUtil.toQuotedString("Precio")).append(",")
                .append(SqlUtil.toQuotedString("ValorizaPor")).append(",")
                .append(SqlUtil.toQuotedString("TieneCupon")).append(",")
                .append(SqlUtil.toQuotedString("NumeroAccionesCirculacion")).append(",")
                .append(SqlUtil.toQuotedString("NumeroCuotasCirculacionMesActual")).append(",")
                .append(SqlUtil.toQuotedString("IdPaisEmisor")).append(",")
                .append(SqlUtil.toQuotedString("IndicadorLiquidaDCV")).append(",")
                .append(SqlUtil.toQuotedString("Serie")).append(",")
                .append(SqlUtil.toQuotedString("IndicadorBolson")).append(",")
                .append(SqlUtil.toQuotedString("FrecuenciaCupon")).append(",")
                .append(SqlUtil.toQuotedString("PrecioReferencia")).append(",")
                .append(SqlUtil.toQuotedString("FechaReferencia")).append(",")
                .append(SqlUtil.toQuotedString("NumeroCupones"))
                .append(") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        INSERT_COMMAND = sb.toString();
    }

    public CargadorInstrumentos(
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
        bulkStatement.setString(2, bono.getNemo());
        bulkStatement.setString(3, bono.getIsin());
        bulkStatement.setBigDecimal(4, BigDecimal.ZERO);
        bulkStatement.setString(5, "N");
        bulkStatement.setString(6, "N");
        bulkStatement.setString(7, bono.getNemo());
        bulkStatement.setString(8, "");
        bulkStatement.setDate(9, new java.sql.Date(bono.getFechaDevengo().getTime()));
        bulkStatement.setDate(10, new java.sql.Date(bono.getFechaVencimiento().getTime()));
        bulkStatement.setInt(11, 0);
        bulkStatement.setString(12, "");
        bulkStatement.setString(13, null);
        bulkStatement.setString(14, "N");
        bulkStatement.setString(15, bono.getMonedaEmision());
        bulkStatement.setString(16, "BON");
        bulkStatement.setString(17, "ACT");
        bulkStatement.setInt(18, 0);
        bulkStatement.setInt(19, 0);
        bulkStatement.setDate(20, new java.sql.Date(bono.getFechaDevengo().getTime()));
        bulkStatement.setBigDecimal(21, BigDecimal.ZERO);
        bulkStatement.setString(22, "N");
        bulkStatement.setString(23, "");
        bulkStatement.setString(24, "");
        bulkStatement.setString(25, "INTER");
        bulkStatement.setBigDecimal(26, BigDecimal.ZERO);
        bulkStatement.setBigDecimal(27, new BigDecimal(100));
        bulkStatement.setString(28, "P");
        bulkStatement.setString(29, "S");
        bulkStatement.setInt(30, 0);
        bulkStatement.setInt(31, 0);
        bulkStatement.setInt(32, 0);
        bulkStatement.setString(33, "N");
        bulkStatement.setString(34, "");
        bulkStatement.setString(35, "N");
        bulkStatement.setBigDecimal(36, new BigDecimal(bono.getPeriodoPago()));
        bulkStatement.setBigDecimal(37, BigDecimal.ZERO);
        bulkStatement.setDate(38,
                new java.sql.Date(DateUtil.getDate(1, Calendar.JANUARY, 1).getTime()));
        bulkStatement.setInt(39, bono.getCantidadCupones());
        addBatch();
    }

}
