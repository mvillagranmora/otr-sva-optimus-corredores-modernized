package cl.bcs.cuentainversion.infrastructure.adapter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cl.bcs.cuentainversion.port.out.SecuenciasPort;

/** HANA implementation of {@link SecuenciasPort}: one NEXTVAL per row of a generated series. */
@Component
public class SecuenciasJdbc implements SecuenciasPort {

    private final NamedParameterJdbcTemplate jdbc;

    public SecuenciasJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Long> siguientes(String nombre, Long cantidad) {
        List<Long> salida = new ArrayList<>();
        if (cantidad == null || cantidad <= 0) {
            return salida;
        }
        // the sequence name is a code constant of the callers, never user input
        String sql = "SELECT \"" + nombre + "\".NEXTVAL AS \"Id\" FROM SERIES_GENERATE_BIGINT(1, 0, :cantidad)";
        jdbc.query(sql, new MapSqlParameterSource("cantidad", cantidad), rs -> {
            long id = rs.getLong("Id");
            if (!rs.wasNull()) {
                salida.add(id);
            }
        });
        return salida;
    }
}
