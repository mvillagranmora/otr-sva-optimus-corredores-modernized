package cl.bcs.cuentainversion.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SaldoFlujoCajaPort {
    BigDecimal consultarSaldo(Long idPortafolio, String idMoneda, Long idEmpresa, Long idUsuario, LocalDate fecha);
}
