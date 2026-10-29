package cl.bcs.normativa.dal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import cl.bcs.normativa.esquema.Esquema;
import cl.bcs.normativa.esquema.RangoFechas;
import cl.bcs.utils.JdbcUtil;

/**
 * Implementa mapeo para producir {@link Esquema}.
 *
 * @author fpino@email.com
 *
 */
final class EsquemaMapper {

    /**
     * Obtiene una nueva instancia de {@link Esquema} a partir de
     * {@link ResultSet}.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @return Una nueva instancia de {@link Esquema} a partir de
     *         {@link ResultSet}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Esquema map(final ResultSet rs) throws SQLException {
        final Esquema esquema = new Esquema();
        esquema.setNombre(JdbcUtil.getString(rs, "Esquema"));
        esquema.setModulo(JdbcUtil.getString(rs, "Modulo"));
        esquema.setFechaPresentacionLimite(JdbcUtil.getDate(rs, "FechaPresentacionLimite"));
        esquema.setIdEntidadReguladora(JdbcUtil.getString(rs, "IdEntidadReguladora"));
        esquema.setObjetivo(JdbcUtil.getString(rs, "Objetivo"));
        esquema.setPeriodicidad(JdbcUtil.getString(rs, "Periodicidad"));
        esquema.setPeriodoInicio(JdbcUtil.getDate(rs, "PeriodoInicio"));
        esquema.setVersion(JdbcUtil.getString(rs, "Version"));
        esquema.setCodigoVersionSII(JdbcUtil.getInteger(rs, "CodigoVersionSII"));

        final Date fechaDesde = JdbcUtil.getDate(rs, "FechaDatosDesde");
        final Date fechaHasta = JdbcUtil.getDate(rs, "FechaDatosHasta");
        esquema.setRangoFechas(new RangoFechas(fechaDesde, fechaHasta));

        return esquema;
    }

    private EsquemaMapper() {
    }
}
