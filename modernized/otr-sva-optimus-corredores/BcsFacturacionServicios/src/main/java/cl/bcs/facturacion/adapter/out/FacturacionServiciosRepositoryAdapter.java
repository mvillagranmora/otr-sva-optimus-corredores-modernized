package cl.bcs.facturacion.adapter.out;

import cl.bcs.facturacion.domain.Comprobante;
import cl.bcs.facturacion.port.out.FacturacionServiciosDataPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JDBC adapter for FacturacionServiciosDataPort.
 *
 * obtenerFechaSistema and consultarComprobantes use NamedParameterJdbcTemplate
 * against CONFIGURACION_T and COMPROBANTES_T respectively.
 *
 * procesarFacturacionCustodia, crearComprobante and procesarGananciaCapital
 * require full SAP HANA stored procedures and throw UnsupportedOperationException.
 * They are marked @Disabled in integration tests and return 503 via
 * GlobalExceptionHandler when called via REST.
 */
@Repository
public class FacturacionServiciosRepositoryAdapter implements FacturacionServiciosDataPort {

    private final NamedParameterJdbcTemplate jdbc;

    public FacturacionServiciosRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String obtenerFechaSistema(Long idEmpresa) {
        String sql = """
                SELECT VALOR
                FROM CONFIGURACION_T
                WHERE ID_EMPRESA = :idEmpresa
                  AND ID_CONFIGURACION = 'FechaSistema'
                """;
        var params = new MapSqlParameterSource("idEmpresa", idEmpresa);
        return jdbc.queryForObject(sql, params, String.class);
    }

    @Override
    public List<Comprobante> consultarComprobantes(Long idEmpresa, String estado) {
        String sql = """
                SELECT ID_COMPROBANTE, ID_EMPRESA, ID_TIPO_COMPROBANTE, ESTADO, FECHA_COMPROBANTE
                FROM COMPROBANTES_T
                WHERE ID_EMPRESA = :idEmpresa
                  AND ESTADO = :estado
                """;
        var params = new MapSqlParameterSource()
                .addValue("idEmpresa", idEmpresa)
                .addValue("estado", estado);
        return jdbc.query(sql, params, (rs, rowNum) -> new Comprobante(
                rs.getLong("ID_COMPROBANTE"),
                rs.getLong("ID_EMPRESA"),
                rs.getString("ID_TIPO_COMPROBANTE"),
                rs.getString("ESTADO"),
                rs.getString("FECHA_COMPROBANTE")
        ));
    }

    @Override
    public void procesarFacturacionCustodia(Long idEmpresa, Long idPortafolio) {
        throw new UnsupportedOperationException("requires HANA");
    }

    @Override
    public void crearComprobante(Long idEmpresa, String tipoComprobante) {
        throw new UnsupportedOperationException("requires HANA");
    }

    @Override
    public void procesarGananciaCapital(Long idEmpresa, Long idComprobante) {
        throw new UnsupportedOperationException("requires HANA");
    }
}
