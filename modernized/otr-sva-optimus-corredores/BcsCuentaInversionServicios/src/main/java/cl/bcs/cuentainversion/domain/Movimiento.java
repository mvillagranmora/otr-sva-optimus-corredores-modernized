package cl.bcs.cuentainversion.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

// Result record for movement queries (ConsultarMovimientoCuentaInversion, ConsultarCartolaDeMovimiento)
public record Movimiento(
        Long idCargoAbonoMovimiento,
        Long idPortafolio,
        String idMoneda,
        String idEstado,
        String idTipoMovimiento,
        String idConcepto,
        String descripcion,
        LocalDate fechaEfectiva,
        LocalDate fechaSistema,
        BigDecimal monto,
        BigDecimal saldo,
        String idModulo,
        Long idCarga,
        String nemotecnicoOriginal,
        String nombreConcepto
) {}
