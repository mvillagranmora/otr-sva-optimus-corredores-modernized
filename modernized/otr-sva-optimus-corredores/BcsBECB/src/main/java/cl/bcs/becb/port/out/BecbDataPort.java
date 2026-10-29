package cl.bcs.becb.port.out;

import cl.bcs.becb.domain.DataDetalleRiesgo;
import cl.bcs.becb.domain.DataRiesgoFinanciero;

import java.math.BigDecimal;
import java.util.List;

public interface BecbDataPort {
    Long obtenerIdCliente();
    BigDecimal obtenerValorUF(String fechaSistema);
    List<DataRiesgoFinanciero> consultarRiesgoFinanciero(Long idCliente, BigDecimal valorUF, String fechaInforme, String tabla);
    List<DataDetalleRiesgo> consultarDetalleRiesgoFinanciero(Long idCliente, BigDecimal valorUF, String fechaInforme, String tabla);
}
