package cl.bcs.normativa.svc.domain.cartola;

import java.math.BigDecimal;

/**
 * Representa los movimientos de caja/títulos de un cliente para el cálculo de rentabilidad.
 * Mapea los datos de la cartola consolidada histórica.
 *
 * MTD = Month to Date, YTD = Year to Date, ITD = Inception to Date
 */
public class SaldoCuentaCliente {

    public Long idCliente;
    public Long idEmpresa;

    // Movimientos de caja - abonos (aportes)
    public BigDecimal totalAbonosCajaMTD = BigDecimal.ZERO;
    public BigDecimal totalAbonosCajaYTD = BigDecimal.ZERO;
    public BigDecimal totalAbonosCajaITD = BigDecimal.ZERO;

    // Movimientos de caja - cargos (rescates)
    public BigDecimal totalCargosCajaMTD = BigDecimal.ZERO;
    public BigDecimal totalCargosCajaYTD = BigDecimal.ZERO;
    public BigDecimal totalCargosCajaITD = BigDecimal.ZERO;

    // Títulos - ingresos
    public BigDecimal totalIngresoTituloMTD = BigDecimal.ZERO;
    public BigDecimal totalIngresoTituloYTD = BigDecimal.ZERO;
    public BigDecimal totalIngresoTituloITD = BigDecimal.ZERO;

    // Títulos - retiros
    public BigDecimal totalRetiroTituloMTD = BigDecimal.ZERO;
    public BigDecimal totalRetiroTituloYTD = BigDecimal.ZERO;
    public BigDecimal totalRetiroTituloITD = BigDecimal.ZERO;

    // Aportes período para cálculo de rentabilidad
    public BigDecimal abonosCajaMTDRentabilidad = BigDecimal.ZERO;
    public BigDecimal abonosCajaYTDRentabilidad = BigDecimal.ZERO;
    public BigDecimal abonosCajaITDRentabilidad = BigDecimal.ZERO;

    // Rescates período para cálculo de rentabilidad
    public BigDecimal cargosCajaMTDRentabilidad = BigDecimal.ZERO;
    public BigDecimal cargosCajaYTDRentabilidad = BigDecimal.ZERO;
    public BigDecimal cargosCajaITDRentabilidad = BigDecimal.ZERO;

    // Ventas de títulos período
    public BigDecimal ventasTitulosMTDRentabilidad = BigDecimal.ZERO;
    public BigDecimal ventasTitulosYTDRentabilidad = BigDecimal.ZERO;
    public BigDecimal ventasTitulosITDRentabilidad = BigDecimal.ZERO;

    // Compras de títulos período
    public BigDecimal comprasTitulosMTDRentabilidad = BigDecimal.ZERO;
    public BigDecimal comprasTitulosYTDRentabilidad = BigDecimal.ZERO;
    public BigDecimal comprasTitulosITDRentabilidad = BigDecimal.ZERO;

    public SaldoCuentaCliente() {}
}
