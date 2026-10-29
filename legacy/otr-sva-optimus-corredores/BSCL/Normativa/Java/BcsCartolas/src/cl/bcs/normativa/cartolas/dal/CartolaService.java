package cl.bcs.normativa.cartolas.dal;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.entidades.Opcion;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.normativa.dal.RecordNotFoundException;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.sql.utils.SqlUtil;

public final class CartolaService {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CartolaService.class.getName());

    public static Cartola lookupByIdCartola(
            final Connection connection,
            final String schema,
            final long idCartola)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.TB_CARTOLA);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("IdCartola", idCartola);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Cartola.newInstance(rs);
                }
            }
        }

        throw new RecordNotFoundException("Cartola no encontrada id: " + idCartola);
    }

    public static List<Cartola> lookupByIdNomina(
            final Connection connection,
            final String schema,
            final long idNomina,
            final boolean reproceso)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.TB_CARTOLA);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("IdNomina", idNomina);

        if (reproceso) {
            select.getWhere().put("Reprocesar", Opcion.SI);
        }

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        final List<Cartola> list = new ArrayList<>();
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(Cartola.newInstance(rs));
                }
            }
        }

        return list;
    }

    public static List<Cartola> lookupByIdNomina(
            final ContextoCartolas contexto,
            final long idNomina,
            boolean reproceso)
                    throws SQLException {
        return lookupByIdNomina(contexto.getConnection(), contexto.getSchema(), idNomina,
                reproceso);
    }

    public static void save(
            final ContextoCartolas contexto,
            final Cartola cartola)
                    throws SQLException {

        final StringBuilder sb = new StringBuilder(128);
        sb.append("{CALL ").append(SqlUtil.toQuotedString(contexto.getSchema())).append('.')
                .append(SqlUtil.toQuotedString(Nombres.SP_GRABAR_CARTOLA))
                .append("(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
        final String sql = sb.toString();
        LOGGER.info(sql);

        try (final CallableStatement cstmt = contexto.getConnection().prepareCall(sql)) {
            cstmt.setLong(1, contexto.getEmpresa().getId());
            cstmt.setLong(2, contexto.getIdUsuario());
            cstmt.setString(3, contexto.getModulo());
            cstmt.setLong(4, cartola.getId());
            cstmt.setLong(5, cartola.getIdNomina());
            cstmt.setLong(6, cartola.getIdCliente());
            cstmt.setLong(7, cartola.getIdPortafolio());
            cstmt.setString(8, cartola.getReprocesar());
            cstmt.setString(9, cartola.getNotificar());
            cstmt.setString(10, cartola.getEstadoProceso());
            cstmt.setString(11, cartola.getMensajeProceso());
            cstmt.registerOutParameter(12, Types.INTEGER);
            cstmt.registerOutParameter(13, Types.INTEGER);
            cstmt.registerOutParameter(14, Types.INTEGER);
            cstmt.execute();
            cartola.setId(cstmt.getLong(12));
        }
    }

    private CartolaService() {
    }

}
