package cl.bcs.cuentainversion.port.out;

import java.math.BigDecimal;

public interface CrearMovimientoComprobantePort {
    void crearMovimientoComprobante(Long idEmpresa, Long idUsuario, String concepto,
            String tipoVoucher, Long idPortafolio, BigDecimal monto,
            String idCondicion, String idMoneda);
}
