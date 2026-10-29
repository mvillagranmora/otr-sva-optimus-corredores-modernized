package cl.bcs.ordenes.adapter.out;

import cl.bcs.ordenes.port.out.OrdenDataPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class OrdenRepositoryAdapter implements OrdenDataPort {

    private final NamedParameterJdbcTemplate jdbc;

    public OrdenRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void registrarOrden(String idPortafolio, String idTipoOperacion) {
        throw new UnsupportedOperationException(
            "requires HANA SP: SP_CREAR_ORDEN");
    }

    @Override
    public String consultarEstadoModulo(String idModulo) {
        String sql = "SELECT ESTADO FROM MODULOS_T WHERE ID_MODULO = :idModulo";
        List<String> result = jdbc.query(sql, Map.of("idModulo", idModulo),
            (rs, row) -> rs.getString("ESTADO"));
        return result.isEmpty() ? null : result.get(0);
    }
}
