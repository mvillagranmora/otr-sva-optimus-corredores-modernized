package cl.bcs.normativa.dal;

import java.sql.ResultSet;
import java.sql.SQLException;

import cl.bcs.normativa.esquema.Campo;
import cl.bcs.utils.JdbcUtil;

/**
 * Implementa mapeo para producir {@link Campo}.
 *
 * @author fpino@email.com
 *
 */
final class CampoMapper {

    /**
     * Obtiene una nueva instancia de {@link Campo} a partir de
     * {@link ResultSet}.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @return Una nueva instancia de {@link Campo} a partir de
     *         {@link ResultSet}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Campo map(final ResultSet rs) throws SQLException {
        final Campo campo = new Campo();
        campo.setNombreEsquema(JdbcUtil.getString(rs, "Esquema"));
        campo.setVersionEsquema(JdbcUtil.getString(rs, "Version"));
        campo.setTipoDiccionario(JdbcUtil.getString(rs, "TipoDiccionario"));
        campo.setOrden(JdbcUtil.getShort(rs, "Orden"));
        campo.setNombre(JdbcUtil.getString(rs, "Nombre"));
        campo.setCodigoSII(JdbcUtil.getString(rs, "CodigoSII"));
        campo.setTitulo(JdbcUtil.getString(rs, "Titulo"));
        campo.setTipoDato(JdbcUtil.getString(rs, "TipoDato"));
        campo.setLargo(JdbcUtil.getShort(rs, "Largo"));
        campo.setValorDefecto(JdbcUtil.getString(rs, "ValorDefecto"));
        campo.setObligatorio(JdbcUtil.getStringAsBoolean(rs, "Obligatorio"));
        campo.setEditable(JdbcUtil.getStringAsBoolean(rs, "Editable"));
        campo.setModoMasivo(JdbcUtil.getStringAsBoolean(rs, "ModoMasivo"));
        campo.setExportable(JdbcUtil.getStringAsBoolean(rs, "Exportable"));
        campo.setOcultoEnGrilla(JdbcUtil.getStringAsBoolean(rs, "OcultoEnGrilla"));
        campo.setOcultoEnMantenedor(JdbcUtil.getStringAsBoolean(rs, "OcultoEnMantenedor"));
        campo.setComun(JdbcUtil.getStringAsBoolean(rs, "Comun"));
        campo.setGrupo(JdbcUtil.getString(rs, "Grupo"));
        campo.setClave(JdbcUtil.getStringAsBoolean(rs, "Clave"));
        campo.setOrdenCargaExterna(JdbcUtil.getShort(rs, "OrdenCargaExterna"));
        campo.setCantidadDecimales(JdbcUtil.getShort(rs, "CantidadDecimales"));
        return campo;
    }

    private CampoMapper() {
    }
}
