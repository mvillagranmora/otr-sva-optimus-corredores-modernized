package cl.bcs.valo2.data.cargadores;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import cl.bcs.sql.datasource.DataSourceProvider;
import cl.bcs.sql.datasource.DataSourceSettings;
import cl.bcs.sql.utils.SqlBulk;
import cl.bcs.sql.utils.SqlUtil;
import cl.bcs.utils.PropertiesUtil;
import cl.bcs.valo2.data.archivos.ArchivoCupones;
import cl.bcs.valo2.entidades.Cupon;

public class CargadorCupones extends SqlBulk {
    private static final String INSERT_COMMAND;

    static {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("INSERT INTO ").append(SqlUtil.toQuotedString("BSCL.Plataforma.BD::INS.Cupon"))
                .append('(').append(SqlUtil.toQuotedString("IdCupon")).append(",")
                .append(SqlUtil.toQuotedString("Nemotecnico")).append(",")
                .append(SqlUtil.toQuotedString("Numero")).append(",")
                .append(SqlUtil.toQuotedString("Fecha")).append(",")
                .append(SqlUtil.toQuotedString("Call")).append(",")
                .append(SqlUtil.toQuotedString("Intereses")).append(",")
                .append(SqlUtil.toQuotedString("Amortizacion")).append(",")
                .append(SqlUtil.toQuotedString("SaldoPorAmortizar"))
                .append(") VALUES(?,?,?,?,?,?,?,?)");
        INSERT_COMMAND = sb.toString();
    }

    public static void main(
            final String[] args)
                    throws IOException,
                    SQLException {
        final ArchivoCupones archivoCupones = new ArchivoCupones("cupones.csv");
        final Map<String, List<Cupon>> mapCupones = archivoCupones.getCupones();
        final Properties props = PropertiesUtil.newProperties("valorizador.properties");
        final DataSourceSettings settings = new DataSourceSettings(props);
        final DataSource dataSource = DataSourceProvider.getInstance(settings);
        try (final Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            final CargadorCupones cargadorCupones = new CargadorCupones(conn, 100);
            cargadorCupones.cargar(mapCupones, 100000);
        }
    }

    public CargadorCupones(
            final Connection conn,
            final int batchSize) throws SQLException {
        super(conn, INSERT_COMMAND, batchSize);
    }

    public void cargar(
            final Map<String, List<Cupon>> mapCupones,
            final long startId)
                    throws SQLException {
        long id = startId;
        for (final String nemo : mapCupones.keySet()) {
            final List<Cupon> cupones = mapCupones.get(nemo);
            for (final Cupon cupon : cupones) {
                grabar(++id, cupon);
            }
        }

        endBatch();
    }

    private void grabar(
            final long id,
            final Cupon cupon)
                    throws SQLException {
        final PreparedStatement bulkStatement = getBulkStatement();
        bulkStatement.clearParameters();
        bulkStatement.setLong(1, id);
        bulkStatement.setString(2, cupon.getNemo());
        bulkStatement.setInt(3, cupon.getNumero());
        bulkStatement.setDate(4, new java.sql.Date(cupon.getFecha().getTime()));
        bulkStatement.setString(5, cupon.isCall() ? "S" : "N");
        bulkStatement.setBigDecimal(6, cupon.getIntereses());
        bulkStatement.setBigDecimal(7, cupon.getAmortizacion());
        bulkStatement.setBigDecimal(8, cupon.getSaldoPorAmortizar());
        addBatch();
    }

}
