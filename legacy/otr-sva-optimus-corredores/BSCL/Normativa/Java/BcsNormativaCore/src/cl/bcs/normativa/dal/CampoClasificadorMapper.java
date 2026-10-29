package cl.bcs.normativa.dal;

import java.sql.ResultSet;
import java.sql.SQLException;

import cl.bcs.normativa.esquema.CampoClasificador;
import cl.bcs.utils.JdbcUtil;

/**
 * Implementa mapeo para producir {@link CampoClasificador}.
 *
 * @author fpino@email.com
 *
 */
public final class CampoClasificadorMapper {

    /**
     * Obtiene una nueva instancia de {@link CampoClasificador} a partir de
     * {@link ResultSet}.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @return Una nueva instancia de {@link CampoClasificador} a partir de
     *         {@link ResultSet}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static CampoClasificador map(final ResultSet rs) throws SQLException {
        final CampoClasificador campoClasificador = new CampoClasificador();
        campoClasificador.setNombreEsquema(JdbcUtil.getString(rs, "Esquema"));
        campoClasificador.setVersionEsquema(JdbcUtil.getString(rs, "Version"));
        campoClasificador.setNombreCampo(JdbcUtil.getString(rs, "NombreCampo"));
        campoClasificador.setTipoClasificador(JdbcUtil.getString(rs, "TipoClasificador"));
        campoClasificador.setValorClasificador(JdbcUtil.getString(rs, "ValorClasificador"));
        return campoClasificador;
    }

    private CampoClasificadorMapper() {
    }

}
