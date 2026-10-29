package cl.bcs.normativa.svc.domain.declaracion;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Valor del IPC (Índice de Precios al Consumidor) para un mes determinado.
 * Usado en el reajuste de valores de declaraciones juradas.
 */
public class ValorIPC {

    /** Año en formato yyyy */
    public String agno;

    /** Año-mes en formato yyyyMM */
    public String agnoMes;

    /** Año tributario */
    public String agnoTributario;

    public String descMes;
    public LocalDate fecha;

    /** Factor de ajuste acumulado */
    public BigDecimal factor;

    /** Valor IPC del mes */
    public BigDecimal valorIpc;

    public ValorIPC() {}
}
