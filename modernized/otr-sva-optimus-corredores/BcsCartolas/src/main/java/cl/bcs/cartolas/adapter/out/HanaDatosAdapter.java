package cl.bcs.cartolas.adapter.out;

import cl.bcs.cartolas.domain.Datos;
import cl.bcs.cartolas.port.out.DatosPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador HANA para datos CSV de cartola (tabla CAR.Datos).
 */
@Repository
public class HanaDatosAdapter implements DatosPort {

    private final NamedParameterJdbcTemplate jdbc;

    public HanaDatosAdapter(final NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc es requerido");
    }

    @Override
    public Optional<Datos> findByIdCartolaAndNombre(final long idCartola, final String nombre) {
        Objects.requireNonNull(nombre, "nombre es requerido");
        final String sql = """
                SELECT id_datos, id_cartola, nombre, formato, valor
                FROM car_datos
                WHERE id_cartola = :idCartola
                  AND nombre     = :nombre
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCartola", idCartola)
                .addValue("nombre",    nombre);

        return jdbc.query(sql, params, this::mapRow).stream().findFirst();
    }

    @Override
    public List<Datos> findByIdCartola(final long idCartola) {
        final String sql = """
                SELECT id_datos, id_cartola, nombre, formato, valor
                FROM car_datos
                WHERE id_cartola = :idCartola
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCartola", idCartola);

        return jdbc.query(sql, params, this::mapRow);
    }

    @Override
    public void save(final Datos datos) {
        Objects.requireNonNull(datos, "datos es requerido");
        final String sql = """
                INSERT INTO car_datos (id_cartola, nombre, formato, valor)
                VALUES (:idCartola, :nombre, :formato, :valor)
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCartola", datos.getIdCartola())
                .addValue("nombre",    datos.getNombre())
                .addValue("formato",   datos.getFormato())
                .addValue("valor",     datos.getValor());

        jdbc.update(sql, params);
    }

    @Override
    public void deleteByIdCartola(final long idCartola) {
        final String sql = "DELETE FROM car_datos WHERE id_cartola = :idCartola";
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCartola", idCartola);
        jdbc.update(sql, params);
    }

    private Datos mapRow(final java.sql.ResultSet rs, final int rowNum) throws java.sql.SQLException {
        final Datos d = new Datos();
        d.setId(rs.getLong("id_datos"));
        d.setIdCartola(rs.getLong("id_cartola"));
        d.setNombre(rs.getString("nombre"));
        d.setFormato(rs.getString("formato"));
        d.setValor(rs.getString("valor"));
        return d;
    }
}
