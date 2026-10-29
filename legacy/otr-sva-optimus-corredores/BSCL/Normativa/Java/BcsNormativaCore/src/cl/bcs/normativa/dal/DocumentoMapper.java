package cl.bcs.normativa.dal;

import java.sql.ResultSet;
import java.sql.SQLException;

import cl.bcs.normativa.esquema.Documento;
import cl.bcs.utils.JdbcUtil;

/**
 * Implementa mapeo para producir {@link Documento}.
 *
 * @author fpino@email.com
 *
 */
final class DocumentoMapper {

    /**
     * Obtiene una nueva instancia de {@link Documento} a partir de
     * {@link ResultSet}.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @return Una nueva instancia de {@link Documento} a partir de
     *         {@link ResultSet}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Documento map(final ResultSet rs) throws SQLException {
        final Documento documento = new Documento();
        documento.setId(JdbcUtil.getLong(rs, "IdDocumento"));
        documento.setIdTipoDocumento(JdbcUtil.getString(rs, "IdTipoDocumento"));
        documento.setNombreEsquema(JdbcUtil.getString(rs, "Esquema"));
        documento.setVersionEsquema(JdbcUtil.getString(rs, "Version"));
        documento.setIdEmpresa(JdbcUtil.getLong(rs, "IdEmpresa"));
        documento.setFechaLimite(JdbcUtil.getDate(rs, "FechaLimite"));
        documento.setIdEstadoDocumento(JdbcUtil.getString(rs, "IdEstadoDocumento"));
        documento.setFechaCreacion(JdbcUtil.getDate(rs, "FechaCreacion"));
        documento.setFechaPresentacion(JdbcUtil.getDate(rs, "FechaPresentacion"));
        documento.setRutDeclarante(JdbcUtil.getString(rs, "DocumentoIdentidadDeclarante"));
        documento.setRutRepresentante(JdbcUtil.getString(rs, "DocumentoIdentidadRepresentante"));
        documento.setEmailDeclaracion(JdbcUtil.getString(rs, "DireccionElectronicaDeclaracion"));
        documento.setDireccionDeclaracion(JdbcUtil.getString(rs, "DireccionFisicaDeclaracion"));
        documento.setComunaDeclaracion(JdbcUtil.getString(rs, "ComunaDeclaracion"));
        documento.setTelefonoDeclaracion(JdbcUtil.getString(rs, "TelefonoDeclaracion"));
        documento.setFolioDeclaracion(JdbcUtil.getString(rs, "FolioDeclaracion"));
        documento.setEnProceso(JdbcUtil.getStringAsBoolean(rs, "EnProceso"));
        documento.setFechaInicioUltimoProceso(JdbcUtil.getDate(rs, "FechaInicioUltimoProceso"));
        return documento;
    }

    private DocumentoMapper() {
    }
}
