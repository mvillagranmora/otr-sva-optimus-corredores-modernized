package cl.bcs.normativa.cartolas.dal;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.entidades.Nomina;
import cl.bcs.normativa.cartolas.entidades.Opcion;
import cl.bcs.normativa.cartolas.entidades.Plantilla;
import cl.bcs.normativa.cartolas.entidades.TipoProceso;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.normativa.dal.RecordNotFoundException;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.sql.utils.SqlUtil;

public final class NominaService {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(NominaService.class.getName());

    private static void actualizarReproceso(
            final ContextoCartolas contexto,
            final String lista,
            final String valor)
                    throws SQLException {

        final StringBuilder sb = new StringBuilder(128);
        sb.append("{CALL ").append(SqlUtil.toQuotedString(contexto.getSchema())).append('.')
                .append(SqlUtil.toQuotedString(Nombres.SP_ACTUALIZAR_REPROCESO))
                .append("(?,?,?,?,?,?,?,?,?)}");
        final String sql = sb.toString();
        LOGGER.info(sql);

        try (final CallableStatement cstmt = contexto.getConnection().prepareCall(sql)) {
            cstmt.setLong(1, contexto.getEmpresa().getId());
            cstmt.setLong(2, contexto.getIdUsuario());
            cstmt.setString(3, contexto.getModulo());
            cstmt.setString(4, lista);
            cstmt.setString(5, valor);
            cstmt.registerOutParameter(6, Types.INTEGER);
            cstmt.registerOutParameter(7, Types.INTEGER);
            cstmt.execute();
        }
    }

    public static Nomina buscarRecurrente(
            final Connection conn,
            final String schema,
            final Nomina nomina)
                    throws SQLException {
        final SqlSelect select = new SqlSelect(SqlMetadata.newTable(schema, Nombres.TB_NOMINA));
        select.getWhere().put("IdEmpresa", nomina.getIdEmpresa());
        select.getWhere().put("TipoProceso", TipoProceso.RECURRENTE);
        select.getWhere().put("Periodo", nomina.getPeriodo());

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Nomina.newInstance(rs);
                }
            }
        }

        return null;
    }

    public static void desmarcarReproceso(
            final ContextoCartolas contexto,
            final List<Cartola> cartolas)
                    throws SQLException {

        String listaIds = "";
        for (final Cartola cartola : cartolas) {
            if (Opcion.SI.equals(cartola.getReprocesar())) {
                if (!listaIds.isEmpty()) {
                    listaIds += ",";
                }

                listaIds += cartola.getId();
            }
        }

        if (!listaIds.isEmpty()) {
            actualizarReproceso(contexto, listaIds, Opcion.NO);
        }
    }

    public static List<Nomina> getNominasProgramadas(
            final Connection connection,
            final String schema)
                    throws SQLException {
        final SqlSelect select = new SqlSelect(SqlMetadata.newTable(schema, Nombres.TB_NOMINA));
        select.getWhere().put("TipoProceso", TipoProceso.PROGRAMADO);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        final List<Nomina> list = new ArrayList();
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    final Nomina nomina = Nomina.newInstance(rs);
                    list.add(nomina);
                }
            }
        }

        return list;
    }

    public static Nomina lookupByIdNomina(
            final Connection connection,
            final String schema,
            final long idNomina)
                    throws SQLException {
        final SqlSelect select = new SqlSelect(SqlMetadata.newTable(schema, Nombres.TB_NOMINA));
        select.getWhere().put("IdNomina", idNomina);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    final Nomina nomina = Nomina.newInstance(rs);
                    final Plantilla plantilla = PlantillaService.lookupByIdPlantilla(connection,
                            schema, nomina.getIdPlantilla());
                    nomina.setPlantilla(plantilla);
                    return nomina;
                }
            }
        }

        throw new RecordNotFoundException("Nomina no encontrada id: " + idNomina);
    }

    public static Nomina lookupByIdNomina(
            final ContextoCartolas contexto,
            final long idNomina)
                    throws SQLException {
        return lookupByIdNomina(contexto.getConnection(), contexto.getSchema(), idNomina);
    }

    public static void poblar(
            final ContextoCartolas contexto,
            final long idNomina)
                    throws SQLException {

        final StringBuilder sb = new StringBuilder(128);
        sb.append("{CALL ").append(SqlUtil.toQuotedString(contexto.getSchema())).append('.')
                .append(SqlUtil.toQuotedString(Nombres.SP_POBLAR_NOMINA))
                .append("(?,?,?,?,?,?,?,?)}");
        final String sql = sb.toString();
        LOGGER.info(sql);

        try (final CallableStatement cstmt = contexto.getConnection().prepareCall(sql)) {
            cstmt.setLong(1, contexto.getEmpresa().getId());
            cstmt.setLong(2, contexto.getIdUsuario());
            cstmt.setString(3, contexto.getModulo());
            cstmt.setLong(4, idNomina);
            cstmt.registerOutParameter(5, Types.INTEGER);
            cstmt.registerOutParameter(6, Types.INTEGER);
            cstmt.execute();
        }
    }

    public static void save(
            final ContextoCartolas contexto,
            final Nomina nomina)
                    throws SQLException,
                    ParseException {

        final StringBuilder sb = new StringBuilder(128);
        sb.append("{CALL ").append(SqlUtil.toQuotedString(contexto.getSchema())).append('.')
                .append(SqlUtil.toQuotedString(Nombres.SP_GRABAR_NOMINA))
                .append("(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
        final String sql = sb.toString();
        LOGGER.info(sql);

        try (final CallableStatement cstmt = contexto.getConnection().prepareCall(sql)) {
            cstmt.setLong(1, contexto.getEmpresa().getId());
            cstmt.setLong(2, contexto.getIdUsuario());
            cstmt.setString(3, contexto.getModulo());

            cstmt.setLong(4, nomina.getId());

            cstmt.setString(5, nomina.getPeriodo());
            cstmt.setString(6, SqlUtil.format(nomina.getFechaDesde()));
            cstmt.setString(7, SqlUtil.format(nomina.getFechaHasta()));
            cstmt.setString(8, nomina.getComentario());

            cstmt.setString(9, nomina.getCertificada());
            cstmt.setString(10, SqlUtil.format(nomina.getFechaCertificacion()));
            cstmt.setLong(11, nomina.getIdUsuarioCertificador());
            cstmt.setString(12, nomina.getNombreUsuarioCertificador());
            cstmt.setString(13, nomina.getNotaCertificacion());

            cstmt.setString(14, nomina.getTipoProceso());
            cstmt.setString(15, nomina.getEstadoProceso());
            // La fecha/hora de proceso se envia como cantidad de segundos,
            // respecto de la hora actual del servidor.
            cstmt.setLong(16, 0l);
            cstmt.setString(17, nomina.getMensajeProceso());

            cstmt.registerOutParameter(18, Types.INTEGER);
            cstmt.registerOutParameter(19, Types.INTEGER);
            cstmt.registerOutParameter(20, Types.INTEGER);
            cstmt.execute();

            nomina.setId(cstmt.getLong(18));
        }
    }

    private NominaService() {
    }

}
