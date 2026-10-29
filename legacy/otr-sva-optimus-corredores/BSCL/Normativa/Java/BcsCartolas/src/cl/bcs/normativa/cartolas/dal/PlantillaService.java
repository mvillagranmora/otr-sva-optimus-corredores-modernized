package cl.bcs.normativa.cartolas.dal;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.entidades.Opcion;
import cl.bcs.normativa.cartolas.entidades.Plantilla;
import cl.bcs.normativa.cartolas.entidades.Recurso;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.normativa.dal.RecordNotFoundException;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.sql.utils.SqlUtil;

public final class PlantillaService {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();
    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PlantillaService.class.getName());

    public static Plantilla lookupByIdPlantilla(
            final Connection connection,
            final String schema,
            final long idPlantilla)
                    throws SQLException {
        final SqlSelect select = new SqlSelect(SqlMetadata.newTable(schema, Nombres.TB_PLANTILLA));
        select.getWhere().put("IdPlantilla", idPlantilla);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    final Plantilla plantilla = Plantilla.newInstance(rs);
                    final List<Recurso> recursos = RecursoService.lookupByIdPlantilla(connection,
                            schema, idPlantilla);
                    plantilla.setRecursos(recursos);
                    return plantilla;
                }
            }
        }

        throw new RecordNotFoundException("Plantilla no encontrada id: " + idPlantilla);
    }

    public static Plantilla lookupByNombreMasFecha(
            final Connection connection,
            final String schema,
            final long idEmpresa,
            final String nombre,
            final Date fecha)
                    throws SQLException {

        final String sql = String.format(
                "SELECT TOP 1 * FROM %s.%s WHERE %s=? AND %s=? AND %s<=? AND %s='%s 'ORDER BY %s DESC",
                SqlUtil.toQuotedString(schema), SqlUtil.toQuotedString(Nombres.TB_PLANTILLA),
                SqlUtil.toQuotedString("IdEmpresa"), SqlUtil.toQuotedString("Nombre"),
                SqlUtil.toQuotedString("VigenteDesde"), SqlUtil.toQuotedString("Vigente"),
                Opcion.SI, SqlUtil.toQuotedString("VigenteDesde"));
        LOGGER.info(sql);

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, idEmpresa);
            pstmt.setString(2, nombre);
            pstmt.setDate(3, fecha);
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    final Plantilla plantilla = Plantilla.newInstance(rs);
                    final List<Recurso> recursos = RecursoService.lookupByIdPlantilla(connection,
                            schema, plantilla.getId());
                    plantilla.setRecursos(recursos);
                    return plantilla;
                }
            }
        }

        final String msg = String.format("Plantilla no encontrada: nombre=%s, fecha=%s", nombre,
                fecha);
        throw new RecordNotFoundException(msg);
    }

    public static Plantilla lookupByNombreMasFecha(
            final ContextoCartolas contexto,
            final long idEmpresa,
            final String nombre,
            final Date fecha)
                    throws SQLException {
        return lookupByNombreMasFecha(contexto.getConnection(), contexto.getSchema(), idEmpresa,
                nombre, fecha);
    }

    public static void save(
            final ContextoCartolas contexto,
            final Plantilla plantilla)
                    throws SQLException {

        final StringBuilder sb = new StringBuilder(128);
        sb.append("{CALL ").append(SqlUtil.toQuotedString(contexto.getSchema())).append('.')
                .append(SqlUtil.toQuotedString(Nombres.SP_GRABAR_PLANTILLA))
                .append("(?,?,?,?,?,?,?,?,?,?,?,?)}");
        final String sql = sb.toString();
        LOGGER.info(sql);

        try (final CallableStatement cstmt = contexto.getConnection().prepareCall(sql)) {
            cstmt.setLong(1, contexto.getEmpresa().getId());
            cstmt.setLong(2, contexto.getIdUsuario());
            cstmt.setString(3, contexto.getModulo());
            cstmt.setLong(4, plantilla.getId());
            cstmt.setString(5, plantilla.getNombre());
            cstmt.setString(6, plantilla.isVigente() ? Opcion.SI : Opcion.NO);
            cstmt.setDate(7, plantilla.getVigenteDesde());
            cstmt.registerOutParameter(8, Types.INTEGER);
            cstmt.registerOutParameter(9, Types.INTEGER);
            cstmt.registerOutParameter(10, Types.INTEGER);
            cstmt.execute();
            plantilla.setId(cstmt.getLong(8));
        }
    }

    private PlantillaService() {
    }

}
