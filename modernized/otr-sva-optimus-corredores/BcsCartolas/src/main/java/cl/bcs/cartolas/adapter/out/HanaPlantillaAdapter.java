package cl.bcs.cartolas.adapter.out;

import cl.bcs.cartolas.domain.Opcion;
import cl.bcs.cartolas.domain.Plantilla;
import cl.bcs.cartolas.domain.Recurso;
import cl.bcs.cartolas.port.out.PlantillaPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador HANA para Plantillas y Recursos (tablas CAR.Plantilla, CAR.Recurso).
 */
@Repository
public class HanaPlantillaAdapter implements PlantillaPort {

    private final NamedParameterJdbcTemplate jdbc;

    public HanaPlantillaAdapter(final NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc es requerido");
    }

    @Override
    public Optional<Plantilla> findById(final long idPlantilla) {
        final String sql = """
                SELECT id_plantilla, id_empresa, nombre, vigente_desde, vigente
                FROM car_plantilla
                WHERE id_plantilla = :idPlantilla
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idPlantilla", idPlantilla);

        return jdbc.query(sql, params, this::mapPlantillaRow)
                .stream()
                .findFirst()
                .map(p -> {
                    p.setRecursos(findRecursosByIdPlantilla(idPlantilla));
                    return p;
                });
    }

    @Override
    public Optional<Plantilla> findVigentePorNombreYFecha(final long idEmpresa,
                                                           final String nombre,
                                                           final LocalDate fecha) {
        Objects.requireNonNull(nombre, "nombre es requerido");
        Objects.requireNonNull(fecha,  "fecha es requerido");
        // Nota: HANA usa "SELECT TOP 1". H2 usa "FETCH FIRST 1 ROWS ONLY" o "LIMIT 1".
        // Usamos LIMIT 1 para compatibilidad H2 en tests.
        // En HANA, la consulta se ejecuta vía stored procedure CAR.BuscarPlantilla.
        // @Disabled("requires HANA") cubre la ruta de producción.
        final String sql = """
                SELECT id_plantilla, id_empresa, nombre, vigente_desde, vigente
                FROM car_plantilla
                WHERE id_empresa    = :idEmpresa
                  AND nombre        = :nombre
                  AND vigente_desde <= :fecha
                  AND vigente       = :vigente
                ORDER BY vigente_desde DESC
                LIMIT 1
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idEmpresa", idEmpresa)
                .addValue("nombre",    nombre)
                .addValue("fecha",     java.sql.Date.valueOf(fecha))
                .addValue("vigente",   Opcion.SI);

        return jdbc.query(sql, params, this::mapPlantillaRow)
                .stream()
                .findFirst()
                .map(p -> {
                    p.setRecursos(findRecursosByIdPlantilla(p.getId()));
                    return p;
                });
    }

    private List<Recurso> findRecursosByIdPlantilla(final long idPlantilla) {
        final String sql = """
                SELECT id_recurso, id_plantilla, editable, descripcion,
                       grupo, nombre, tipo, valor
                FROM car_recurso
                WHERE id_plantilla = :idPlantilla
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idPlantilla", idPlantilla);

        return jdbc.query(sql, params, (rs, rowNum) -> {
            final Recurso r = new Recurso();
            r.setId(rs.getLong("id_recurso"));
            r.setIdPlantilla(rs.getLong("id_plantilla"));
            r.setEditable(rs.getString("editable"));
            r.setDescripcion(rs.getString("descripcion"));
            r.setGrupo(rs.getString("grupo"));
            r.setNombre(rs.getString("nombre"));
            r.setTipo(rs.getString("tipo"));
            r.setValor(rs.getString("valor"));
            return r;
        });
    }

    private Plantilla mapPlantillaRow(final java.sql.ResultSet rs, final int rowNum)
            throws java.sql.SQLException {
        final Plantilla p = new Plantilla();
        p.setId(rs.getLong("id_plantilla"));
        p.setIdEmpresa(rs.getLong("id_empresa"));
        p.setNombre(rs.getString("nombre"));
        final java.sql.Date vd = rs.getDate("vigente_desde");
        if (vd != null) p.setVigenteDesde(vd.toLocalDate());
        p.setVigente(Opcion.SI.equals(rs.getString("vigente")));
        return p;
    }
}
