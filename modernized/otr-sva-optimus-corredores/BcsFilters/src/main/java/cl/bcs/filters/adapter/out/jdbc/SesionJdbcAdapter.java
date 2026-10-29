package cl.bcs.filters.adapter.out.jdbc;

import cl.bcs.filters.domain.Sesion;
import cl.bcs.filters.port.out.SesionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class SesionJdbcAdapter implements SesionRepository {

    private final JdbcTemplate jdbc;

    SesionJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Sesion> findByToken(String token) {
        String sql = """
                SELECT ID_TOKEN_SESION, TOKEN_VALIDO, ID_USUARIO, CANAL, FECHA_CREACION
                FROM ACC_SESION
                WHERE ID_TOKEN_SESION = ?
                """;
        List<Sesion> rows = jdbc.query(sql, (rs, n) -> new Sesion(
                rs.getString("ID_TOKEN_SESION"),
                "S".equals(rs.getString("TOKEN_VALIDO")),
                rs.getLong("ID_USUARIO"),
                rs.getString("CANAL"),
                rs.getTimestamp("FECHA_CREACION").toLocalDateTime()
        ), token);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public String create(Long idUsuario, String canal) {
        String token = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO ACC_SESION (ID_TOKEN_SESION, TOKEN_VALIDO, ID_USUARIO, CANAL, FECHA_CREACION)
                VALUES (?, 'S', ?, ?, ?)
                """, token, idUsuario, canal, LocalDateTime.now());
        return token;
    }
}
