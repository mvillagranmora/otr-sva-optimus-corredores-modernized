package cl.bcs.filters.adapter.out.jdbc;

import cl.bcs.filters.domain.Usuario;
import cl.bcs.filters.port.out.UsuarioRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class UsuarioJdbcAdapter implements UsuarioRepository {

    private final JdbcTemplate jdbc;

    UsuarioJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_COLS =
            "ID_USUARIO, CODIGO_USUARIO, ESTADO, FECHA_VIGENCIA, CLAVE, INTENTOS";

    private Usuario mapRow(java.sql.ResultSet rs, int n) throws java.sql.SQLException {
        var fv = rs.getDate("FECHA_VIGENCIA");
        return new Usuario(
                rs.getLong("ID_USUARIO"),
                rs.getString("CODIGO_USUARIO"),
                rs.getString("ESTADO"),
                fv != null ? fv.toLocalDate() : null,
                rs.getString("CLAVE"),
                rs.getInt("INTENTOS")
        );
    }

    @Override
    public Optional<Usuario> findByCodigo(String codigoUsuario) {
        String sql = "SELECT " + SELECT_COLS + " FROM ACC_USUARIO WHERE CODIGO_USUARIO = ?";
        List<Usuario> rows = jdbc.query(sql, this::mapRow, codigoUsuario);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public Optional<Usuario> findByToken(String token) {
        String sql = """
                SELECT u.ID_USUARIO, u.CODIGO_USUARIO, u.ESTADO, u.FECHA_VIGENCIA, u.CLAVE, u.INTENTOS
                FROM ACC_USUARIO u
                JOIN ACC_SESION s ON s.ID_USUARIO = u.ID_USUARIO
                WHERE s.ID_TOKEN_SESION = ?
                """;
        List<Usuario> rows = jdbc.query(sql, this::mapRow, token);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public void incrementarIntentos(String codigoUsuario) {
        jdbc.update("""
                UPDATE ACC_USUARIO SET INTENTOS = INTENTOS + 1
                WHERE CODIGO_USUARIO = ?
                """, codigoUsuario);
    }

    @Override
    public void resetearIntentos(String codigoUsuario) {
        jdbc.update("""
                UPDATE ACC_USUARIO SET INTENTOS = 0
                WHERE CODIGO_USUARIO = ?
                """, codigoUsuario);
    }
}
