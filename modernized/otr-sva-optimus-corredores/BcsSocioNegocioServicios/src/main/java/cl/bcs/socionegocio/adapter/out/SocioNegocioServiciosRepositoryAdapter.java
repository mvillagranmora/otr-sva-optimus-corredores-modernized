package cl.bcs.socionegocio.adapter.out;

import cl.bcs.socionegocio.domain.SocioNegocio;
import cl.bcs.socionegocio.port.out.SocioNegocioServiciosDataPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SocioNegocioServiciosRepositoryAdapter implements SocioNegocioServiciosDataPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SocioNegocioServiciosRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String obtenerFechaSistema(Long idEmpresa) {
        String sql = "SELECT VALOR FROM CONFIGURACION_T WHERE ID_EMPRESA = :idEmpresa AND ID_CONFIGURACION = 'FechaSistema'";
        MapSqlParameterSource params = new MapSqlParameterSource("idEmpresa", idEmpresa);
        return jdbcTemplate.queryForObject(sql, params, String.class);
    }

    @Override
    public List<SocioNegocio> consultarSociosNegocio(Long idEmpresa) {
        String sql = "SELECT ID_CLIENTE, DOCUMENTO_IDENTIDAD, ID_TIPO_DOCUMENTO, ESTADO FROM SOCIOS_NEGOCIO_T";
        return jdbcTemplate.query(sql, new MapSqlParameterSource(), (rs, rowNum) -> new SocioNegocio(
                rs.getLong("ID_CLIENTE"),
                rs.getString("DOCUMENTO_IDENTIDAD"),
                rs.getString("ID_TIPO_DOCUMENTO"),
                rs.getString("ESTADO")
        ));
    }

    @Override
    public void crearSocioNegocio(Long idEmpresa, String documentoIdentidad) {
        throw new UnsupportedOperationException("requires HANA SP");
    }

    @Override
    public void modificarSocioNegocio(Long idCliente, String estado) {
        throw new UnsupportedOperationException("requires HANA SP");
    }
}
