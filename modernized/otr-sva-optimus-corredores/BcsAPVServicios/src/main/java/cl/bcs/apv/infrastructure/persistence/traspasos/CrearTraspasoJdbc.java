package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.domain.port.out.traspasos.CrearTraspasoRepositorio;

/** JDBC adapter of {@link CrearTraspasoRepositorio}; the SQL files are the legacy ones, byte for byte. */
@Repository
public class CrearTraspasoJdbc implements CrearTraspasoRepositorio {

    private static final String SQL_TRASPASO = SqlTraspasos.recurso("Traspaso_Insert");
    private static final String SQL_DETALLE = SqlTraspasos.recurso("DetalleOrigenTraspaso_Insert");

    private final SqlTraspasos sql;

    public CrearTraspasoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    @Override
    public int[] insertarTraspaso(List<TraspasoFila> filas) throws SQLException {
        // LEGACY DEFECT kept (see README APV-B-15): Traspaso_Insert.sql binds :FechaSuscripcion, a field the legacy Traspaso does not have
        verificarCampos(SQL_TRASPASO, filas, "cl.bcs.APV.CrearTraspaso$Traspaso");
        return sql.loteConConteos(SQL_TRASPASO, filas);
    }

    @Override
    public int[] insertarDetallesOrigen(List<DetalleOrigenFila> filas) throws SQLException {
        verificarCampos(SQL_DETALLE, filas, "cl.bcs.APV.CrearTraspaso$DetalleOrigenTraspaso");
        return sql.loteConConteos(SQL_DETALLE, filas);
    }

    @Override
    public void revertir() throws SQLException {
        sql.revertir();
    }

    /**
     * Legacy {@code SQL.batch}: a named parameter with no public field in the element class fails with "Map Object failed &lt;field&gt;" and the
     * {@code rethrow} suffix " Query: &lt;raw sql&gt; Parameters: [&lt;class&gt;@&lt;hash&gt;]" (the hash is normalised by the tests).
     */
    private static void verificarCampos(String texto, List<?> filas, String nombreLegacy) throws SQLException {
        ParsedSql parsed = NamedParameterUtils.parseSqlStatement(SqlTraspasos.sinComentarios(texto));
        for (Object fila : filas) {
            for (org.springframework.jdbc.core.SqlParameter parametro : NamedParameterUtils.buildSqlParameterList(parsed, new org.springframework.jdbc.core.namedparam.MapSqlParameterSource())) {
                String nombre = parametro.getName();
                try {
                    Field f = fila.getClass().getField(nombre);
                    if (f == null) {
                        throw new NoSuchFieldException(nombre);
                    }
                } catch (NoSuchFieldException e) {
                    StringBuilder params = new StringBuilder("[");
                    for (int i = 0; i < filas.size(); i++) {
                        params.append(i == 0 ? "" : ", ").append(nombreLegacy).append('@').append(Integer.toHexString(System.identityHashCode(filas.get(i))));
                    }
                    params.append(']');
                    throw new SQLException("Map Object failed " + nombre + " Query: " + texto + " Parameters: " + params);
                }
            }
        }
    }
}
