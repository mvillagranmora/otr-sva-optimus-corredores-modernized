package cl.bcs.becb.adapter.out;

import cl.bcs.becb.domain.DataDetalleRiesgo;
import cl.bcs.becb.domain.DataRiesgoFinanciero;
import cl.bcs.becb.port.out.BecbDataPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public class BecbRepositoryAdapter implements BecbDataPort {

    private final NamedParameterJdbcTemplate jdbc;

    public BecbRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long obtenerIdCliente() {
        List<Long> result = jdbc.query(
            "SELECT ID_CLIENTE FROM CLIENTES_T LIMIT 1",
            Map.of(),
            (rs, row) -> rs.getLong("ID_CLIENTE"));
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public BigDecimal obtenerValorUF(String fechaSistema) {
        List<BigDecimal> result = jdbc.query(
            "SELECT VALOR FROM INDICADORES_FINANCIEROS_T " +
            "WHERE FECHA_SISTEMA = :fecha AND CODIGO_INDICADOR = 'UF'",
            Map.of("fecha", fechaSistema),
            (rs, row) -> rs.getBigDecimal("VALOR"));
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<DataRiesgoFinanciero> consultarRiesgoFinanciero(
            Long idCliente, BigDecimal valorUF, String fechaInforme, String tabla) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.RIESGO_FINANCIERO (ConsultarInformeRiesgoFinanciero.sql) — query HANA-específica no reproducible en H2");
    }

    @Override
    public List<DataDetalleRiesgo> consultarDetalleRiesgoFinanciero(
            Long idCliente, BigDecimal valorUF, String fechaInforme, String tabla) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.DETALLE_RIESGO_FINANCIERO (ConsultarDetalleRiesgoFinanciero.sql) — query HANA-específica no reproducible en H2");
    }
}
