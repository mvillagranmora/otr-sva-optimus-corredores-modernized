package cl.bcs.normativa.svc.domain.declaracion;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Detalle de una declaración jurada DJ1871 con sus valores a ajustar por IPC.
 * DJ1871: Ahorro Previsional Voluntario (APV).
 */
public class DetalleIPCDJ1871 {

    public Long idDocumento;
    public Long id;
    public Long numero;
    public String fuenteDatos;
    public LocalDate fechaOperacion;

    /** Año-mes en formato yyyyMM */
    public String agnoMes;

    // Ahorros del trabajador
    public BigDecimal ahorrosTrabajador = BigDecimal.ZERO;
    public BigDecimal ahorrosTrabajadorActualizado = BigDecimal.ZERO;

    // Ahorros por depósito
    public BigDecimal ahorrosDeposito = BigDecimal.ZERO;
    public BigDecimal ahorrosDepositoActualizado = BigDecimal.ZERO;

    // Ahorros por cotizaciones
    public BigDecimal ahorrosCotizaciones = BigDecimal.ZERO;
    public BigDecimal ahorrosCotizacionesActualizado = BigDecimal.ZERO;

    // Retiros del trabajador
    public BigDecimal retirosTrabajador = BigDecimal.ZERO;
    public BigDecimal retirosTrabajadorActualizado = BigDecimal.ZERO;

    // Retiros por depósito
    public BigDecimal retirosDeposito = BigDecimal.ZERO;
    public BigDecimal retirosDepositoActualizado = BigDecimal.ZERO;

    // Retiros por cotizaciones
    public BigDecimal retirosCotizaciones = BigDecimal.ZERO;
    public BigDecimal retirosCotizacionesActualizado = BigDecimal.ZERO;

    public DetalleIPCDJ1871() {}
}
