package cl.bcs.normativa.adapter.out;

import cl.bcs.normativa.domain.Campo;
import cl.bcs.normativa.domain.Esquema;
import cl.bcs.normativa.domain.RangoFechas;
import cl.bcs.normativa.port.out.CampoPort;
import cl.bcs.normativa.port.out.EsquemaPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC para Esquema y Campo normativos.
 * HANA tables:
 *   "BSCL.Normativa.BD::DOC.Esquema" (alias ESQUEMA en H2)
 *   "BSCL.Normativa.BD::DOC.Campo"   (alias CAMPO en H2)
 */
@Repository
public class EsquemaJdbcAdapter implements EsquemaPort, CampoPort {

    private final NamedParameterJdbcTemplate jdbc;

    public EsquemaJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Esquema> buscarPorClave(String nombre, String version) {
        var sql = """
                SELECT Esquema, Version, Modulo, FechaPresentacionLimite,
                       IdEntidadReguladora, Objetivo, Periodicidad, PeriodoInicio,
                       CodigoVersionSII, FechaDatosDesde, FechaDatosHasta
                FROM ESQUEMA
                WHERE Esquema = :nombre AND Version = :version
                """;
        List<Esquema> result = jdbc.query(sql,
                new MapSqlParameterSource("nombre", nombre).addValue("version", version),
                this::mapEsquema);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<Campo> buscarPorEsquema(String nombreEsquema, String versionEsquema) {
        var sql = """
                SELECT Esquema, Version, Nombre, CodigoSII, Titulo, TipoDato,
                       Largo, CantidadDecimales, ValorDefecto, Obligatorio,
                       Editable, ModoMasivo, Exportable, OcultoEnGrilla,
                       OcultoEnMantenedor, Comun, Grupo, Clave,
                       Orden, OrdenCargaExterna, TipoDiccionario
                FROM CAMPO
                WHERE Esquema = :esquema AND Version = :version
                ORDER BY Orden ASC
                """;
        return jdbc.query(sql,
                new MapSqlParameterSource("esquema", nombreEsquema).addValue("version", versionEsquema),
                this::mapCampo);
    }

    private Esquema mapEsquema(ResultSet rs, int row) throws SQLException {
        var e = new Esquema();
        e.setNombre(rs.getString("Esquema"));
        e.setVersion(rs.getString("Version"));
        e.setModulo(rs.getString("Modulo"));
        e.setFechaPresentacionLimite(rs.getDate("FechaPresentacionLimite"));
        e.setIdEntidadReguladora(rs.getString("IdEntidadReguladora"));
        e.setObjetivo(rs.getString("Objetivo"));
        e.setPeriodicidad(rs.getString("Periodicidad"));
        e.setPeriodoInicio(rs.getDate("PeriodoInicio"));
        e.setCodigoVersionSII(rs.getInt("CodigoVersionSII"));
        Date desde = rs.getDate("FechaDatosDesde");
        Date hasta = rs.getDate("FechaDatosHasta");
        e.setRangoFechas(new RangoFechas(desde, hasta));
        return e;
    }

    private Campo mapCampo(ResultSet rs, int row) throws SQLException {
        var c = new Campo();
        c.setNombreEsquema(rs.getString("Esquema"));
        c.setVersionEsquema(rs.getString("Version"));
        c.setNombre(rs.getString("Nombre"));
        c.setCodigoSII(rs.getString("CodigoSII"));
        c.setTitulo(rs.getString("Titulo"));
        c.setTipoDato(rs.getString("TipoDato"));
        c.setLargo(rs.getShort("Largo"));
        c.setCantidadDecimales(rs.getShort("CantidadDecimales"));
        c.setValorDefecto(rs.getString("ValorDefecto"));
        c.setObligatorio("S".equals(rs.getString("Obligatorio")));
        c.setEditable("S".equals(rs.getString("Editable")));
        c.setModoMasivo("S".equals(rs.getString("ModoMasivo")));
        c.setExportable("S".equals(rs.getString("Exportable")));
        c.setOcultoEnGrilla("S".equals(rs.getString("OcultoEnGrilla")));
        c.setOcultoEnMantenedor("S".equals(rs.getString("OcultoEnMantenedor")));
        c.setComun("S".equals(rs.getString("Comun")));
        c.setGrupo(rs.getString("Grupo"));
        c.setClave("S".equals(rs.getString("Clave")));
        c.setOrden(rs.getShort("Orden"));
        c.setOrdenCargaExterna(rs.getShort("OrdenCargaExterna"));
        c.setTipoDiccionario(rs.getString("TipoDiccionario"));
        return c;
    }
}
