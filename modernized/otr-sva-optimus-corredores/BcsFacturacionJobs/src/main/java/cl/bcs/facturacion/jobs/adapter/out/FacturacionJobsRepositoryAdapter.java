package cl.bcs.facturacion.jobs.adapter.out;

import cl.bcs.facturacion.jobs.domain.ResumenFacturacionCliente;
import cl.bcs.facturacion.jobs.port.out.FacturacionJobsDataPort;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * JDBC adapter for {@link FacturacionJobsDataPort}.
 *
 * <p>HANA stored-procedure operations ({@link #emitirComprobantesMasivos} and
 * {@link #generarPrefacturasMasivas}) throw {@link UnsupportedOperationException}
 * because they require a live SAP HANA environment with the corresponding
 * stored procedures installed.</p>
 */
@Repository
public class FacturacionJobsRepositoryAdapter implements FacturacionJobsDataPort {

    private final NamedParameterJdbcTemplate jdbc;

    public FacturacionJobsRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ResumenFacturacionCliente> consultarResumenFacturas(Long idEmpresa,
                                                                     String fechaDesde,
                                                                     String fechaHasta) {
        String sql = """
                SELECT ID_CLIENTE, ID_COMPROBANTE, MONTO_TOTAL, FECHA_COMPROBANTE
                FROM COMPROBANTES_T
                WHERE ID_EMPRESA = :idEmpresa
                  AND FECHA_COMPROBANTE BETWEEN :fechaDesde AND :fechaHasta
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idEmpresa", idEmpresa)
                .addValue("fechaDesde", LocalDate.parse(fechaDesde))
                .addValue("fechaHasta", LocalDate.parse(fechaHasta));

        return jdbc.query(sql, params, new DataClassRowMapper<>(ResumenFacturacionCliente.class));
    }

    @Override
    public String obtenerFechaSistema(Long idEmpresa) {
        String sql = """
                SELECT VALOR FROM CONFIGURACION_T
                WHERE ID_EMPRESA = :idEmpresa
                  AND CLAVE = 'PLA.FechaSistema'
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idEmpresa", idEmpresa);

        return jdbc.queryForObject(sql, params, String.class);
    }

    @Override
    public void emitirComprobantesMasivos(Long idEmpresa, Long idUsuario) {
        throw new UnsupportedOperationException(
                "requires HANA SP: SP_EMISION_MASIVA_COMPROBANTE");
    }

    @Override
    public void generarPrefacturasMasivas(Long idEmpresa, Long idUsuario) {
        throw new UnsupportedOperationException(
                "requires HANA SP: SP_GENERAR_PREFACTURAS_MASIVAS");
    }
}
