package cl.bcs.normativa.cartolas.dal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.entidades.Plantilla;
import cl.bcs.normativa.cartolas.entidades.Recurso;
import cl.bcs.normativa.cartolas.entidades.TipoRecurso;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.sql.utils.SqlUtil;

public final class RecursoService {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(RecursoService.class.getName());

    public static void createImage(
            final ContextoCartolas contexto,
            final Plantilla plantilla,
            final String grupo,
            final String nombre,
            final String descripcion,
            final InputStream is)
                    throws IOException,
                    SQLException {
        final String valor = Recurso.getEncodedBytes(is);
        Recurso.getBufferedImage(valor);
        final Recurso recurso = Recurso.newInstance(plantilla, grupo, nombre, TipoRecurso.IMAGEN,
                valor);
        recurso.setDescripcion(descripcion);
        RecursoService.save(contexto, recurso);
    }

    public static List<Recurso> lookupByIdPlantilla(
            final Connection connection,
            final String schema,
            final long idPlantilla)
                    throws SQLException {
        final SqlTable table = SqlMetadata.newTable(schema, Nombres.TB_RECURSO);
        final SqlSelect select = new SqlSelect(table);
        select.getWhere().put("IdPlantilla", idPlantilla);

        final String sql = GENERATOR.getSqlText(select);
        LOGGER.info(sql);

        final List<Recurso> list = new ArrayList();
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                rs.setFetchSize(1000);
                while (rs.next()) {
                    list.add(Recurso.newInstance(rs));
                }
            }
        }

        return list;
    }

    public static List<Recurso> lookupByIdPlantilla(
            final ContextoCartolas contexto,
            final long idPlantilla)
                    throws SQLException {
        return lookupByIdPlantilla(contexto.getConnection(), contexto.getSchema(), idPlantilla);
    }

    public static void save(
            final ContextoCartolas contexto,
            final Recurso recurso)
                    throws SQLException {

        final String sql = String.format("{CALL %s.%s(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}",
                SqlUtil.toQuotedString(contexto.getSchema()),
                SqlUtil.toQuotedString(Nombres.SP_GRABAR_RECURSO));

        try (final CallableStatement cstmt = contexto.getConnection().prepareCall(sql)) {
            cstmt.setLong(1, contexto.getEmpresa().getId());
            cstmt.setLong(2, contexto.getIdUsuario());
            cstmt.setString(3, contexto.getModulo());
            cstmt.setLong(4, recurso.getId());
            cstmt.setLong(5, recurso.getIdPlantilla());
            cstmt.setString(6, recurso.getEditable());
            cstmt.setString(7, recurso.getDescripcion());
            cstmt.setString(8, recurso.getGrupo());
            cstmt.setString(9, recurso.getNombre());
            cstmt.setString(10, recurso.getTipo());
            cstmt.setString(11, recurso.getValor());
            cstmt.registerOutParameter(12, Types.INTEGER);
            cstmt.registerOutParameter(13, Types.INTEGER);
            cstmt.registerOutParameter(14, Types.INTEGER);
            cstmt.execute();
            recurso.setId(cstmt.getLong(12));
        }
    }

    public static void updateClob(
            final ContextoCartolas contexto,
            final Recurso recurso)
                    throws SQLException {

        final String sql = String.format("{CALL %s.%s(?,?,?,?,?,?,?,?,?)}",
                SqlUtil.toQuotedString(contexto.getSchema()),
                SqlUtil.toQuotedString(Nombres.SP_ACTUALIZAR_VALOR_RECURSO_CLOB));

        try (final CallableStatement cstmt = contexto.getConnection().prepareCall(sql)) {
            cstmt.setLong(1, contexto.getEmpresa().getId());
            cstmt.setLong(2, contexto.getIdUsuario());
            cstmt.setString(3, contexto.getModulo());
            cstmt.setLong(4, recurso.getId());
            cstmt.setString(5, recurso.getValor());
            cstmt.registerOutParameter(6, Types.INTEGER);
            cstmt.registerOutParameter(7, Types.INTEGER);
            cstmt.execute();
        }
    }

    public static void updateImage(
            final ContextoCartolas contexto,
            final long idRecurso,
            final InputStream is)
                    throws IOException,
                    SQLException {
        final String valor = Recurso.getEncodedBytes(is);
        Recurso.getBufferedImage(valor);
        final Recurso recurso = new Recurso();
        recurso.setId(idRecurso);
        recurso.setValor(valor);
        RecursoService.updateClob(contexto, recurso);
    }

    private RecursoService() {
    }

}
