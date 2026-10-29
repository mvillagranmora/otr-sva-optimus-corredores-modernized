package cl.bcs.filters.adapter.out.jdbc;

import cl.bcs.filters.port.out.ServicioRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class ServicioJdbcAdapter implements ServicioRepository {

    private final JdbcTemplate jdbc;

    ServicioJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<String> getValidaAcceso(String idServicio) {
        try {
            String val = jdbc.queryForObject(
                    "SELECT VALIDA_ACCESO_SERVICIO FROM PLA_SERVICIO WHERE ID_SERVICIO = ?",
                    String.class, idServicio);
            return Optional.ofNullable(val);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public int countAccesoServicio(String token, String idServicio) {
        // Simplified: checks a direct token→service grant table.
        // The legacy used a full role→profile→access tree; an equivalent migration
        // query targeting HANA would be @Disabled("requires HANA").
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(1) FROM ACC_ACCESO_SERVICIO WHERE ID_TOKEN_SESION = ? AND ID_SERVICIO = ?",
                Integer.class, token, idServicio);
        return count != null ? count : 0;
    }

    @Override
    public Optional<String> getConfiguracion(String codigoParametro) {
        try {
            String val = jdbc.queryForObject(
                    "SELECT VALOR FROM PLA_CONFIGURACION WHERE CODIGO_PARAMETRO = ?",
                    String.class, codigoParametro);
            return Optional.ofNullable(val);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public String getCodigoUsuario(String token) {
        return jdbc.queryForObject(
                """
                SELECT u.CODIGO_USUARIO
                FROM ACC_USUARIO u
                JOIN ACC_SESION s ON s.ID_USUARIO = u.ID_USUARIO
                WHERE s.ID_TOKEN_SESION = ?
                """,
                String.class, token);
    }
}
