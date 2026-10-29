package cl.bcs.normativa.cartola.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class SaldoCuentaCliente{
	public Long idCliente;
    public Long idEmpresa;
	public BigDecimal saldoInicial;
	public BigDecimal totalCargosCajaMTD;
	public BigDecimal totalCargosCajaYTD;
	public BigDecimal totalCargosCajaITD;
	public BigDecimal totalIngresoTituloMTD;
	public BigDecimal totalIngresoTituloYTD;
	public BigDecimal totalIngresoTituloITD;
	public BigDecimal cargosCajaMTDRentabilidad;
	public BigDecimal cargosCajaYTDRentabilidad;
	public BigDecimal cargosCajaITDRentabilidad;
	public BigDecimal comprasTitulosMTDRentabilidad;
	public BigDecimal comprasTitulosYTDRentabilidad;
	public BigDecimal comprasTitulosITDRentabilidad;
	public BigDecimal totalAbonosCajaMTD;
	public BigDecimal totalAbonosCajaYTD;
	public BigDecimal totalAbonosCajaITD;
	public BigDecimal totalRetiroTituloMTD;
	public BigDecimal totalRetiroTituloYTD;
	public BigDecimal totalRetiroTituloITD;
	public BigDecimal abonosCajaMTDRentabilidad;
	public BigDecimal abonosCajaYTDRentabilidad;
	public BigDecimal abonosCajaITDRentabilidad;
	public BigDecimal ventasTitulosMTDRentabilidad;
	public BigDecimal ventasTitulosYTDRentabilidad;
	public BigDecimal ventasTitulosITDRentabilidad;
	public BigDecimal totalAbonosCajaMTDPeriodoAnterior;
	public BigDecimal totalAbonosCajaYTDPeriodoAnterior;
	public BigDecimal totalCargosCajaMTDPeriodoAnterior;
	public BigDecimal totalCargosCajaYTDPeriodoAnterior;
	
	public BigDecimal totalVentasCRMTitulosMTD;
	public BigDecimal totalVentasCRMTitulosYTD;
	public BigDecimal totalVentasCRMTitulosITD;
	
	public BigDecimal totalComprasCRMTitulosMTD;
	public BigDecimal totalComprasCRMTitulosYTD;
	public BigDecimal totalComprasCRMTitulosITD;
	
	public BigDecimal totalVentasCRMCajaMTD;
	public BigDecimal totalVentasCRMCajaYTD;
	public BigDecimal totalVentasCRMCajaITD;
	
	public BigDecimal totalComprasCRMCajaMTD;
	public BigDecimal totalComprasCRMCajaYTD;
	public BigDecimal totalComprasCRMCajaITD;
	

	public SaldoCuentaCliente() {
		this.idCliente = 0L;
		this.idEmpresa = 0L;
		this.saldoInicial = BigDecimal.ZERO;
		this.totalCargosCajaMTD = BigDecimal.ZERO;
		this.totalCargosCajaYTD = BigDecimal.ZERO;
		this.totalCargosCajaITD = BigDecimal.ZERO;
		this.totalIngresoTituloMTD = BigDecimal.ZERO;
		this.totalIngresoTituloYTD = BigDecimal.ZERO;
		this.totalIngresoTituloITD = BigDecimal.ZERO;
		this.totalRetiroTituloMTD = BigDecimal.ZERO;
		this.totalRetiroTituloYTD = BigDecimal.ZERO;
		this.totalRetiroTituloITD = BigDecimal.ZERO;
		this.cargosCajaMTDRentabilidad = BigDecimal.ZERO;
		this.cargosCajaYTDRentabilidad = BigDecimal.ZERO;
		this.cargosCajaITDRentabilidad = BigDecimal.ZERO;
		this.comprasTitulosMTDRentabilidad = BigDecimal.ZERO;
		this.comprasTitulosYTDRentabilidad = BigDecimal.ZERO;
		this.comprasTitulosITDRentabilidad = BigDecimal.ZERO;
		this.totalAbonosCajaMTD = BigDecimal.ZERO;
		this.totalAbonosCajaYTD = BigDecimal.ZERO;
		this.totalAbonosCajaITD = BigDecimal.ZERO;
		this.abonosCajaMTDRentabilidad = BigDecimal.ZERO;
		this.abonosCajaYTDRentabilidad = BigDecimal.ZERO;
		this.abonosCajaITDRentabilidad = BigDecimal.ZERO;
		this.totalAbonosCajaMTDPeriodoAnterior = BigDecimal.ZERO;
		this.totalAbonosCajaYTDPeriodoAnterior = BigDecimal.ZERO;
		this.totalCargosCajaMTDPeriodoAnterior = BigDecimal.ZERO;
		this.totalCargosCajaYTDPeriodoAnterior = BigDecimal.ZERO;
		this.ventasTitulosMTDRentabilidad = BigDecimal.ZERO;
		this.ventasTitulosYTDRentabilidad = BigDecimal.ZERO;
		this.ventasTitulosITDRentabilidad = BigDecimal.ZERO;
		
		this.totalVentasCRMTitulosMTD = BigDecimal.ZERO;
		this.totalVentasCRMTitulosYTD = BigDecimal.ZERO;
		this.totalVentasCRMTitulosITD = BigDecimal.ZERO;
		
		this.totalComprasCRMTitulosMTD = BigDecimal.ZERO;
		this.totalComprasCRMTitulosYTD = BigDecimal.ZERO;
		this.totalComprasCRMTitulosITD = BigDecimal.ZERO;
		
		this.totalVentasCRMCajaMTD = BigDecimal.ZERO;
		this.totalVentasCRMCajaYTD = BigDecimal.ZERO;
		this.totalVentasCRMCajaITD = BigDecimal.ZERO;
		
		this.totalComprasCRMCajaMTD = BigDecimal.ZERO;
		this.totalComprasCRMCajaYTD = BigDecimal.ZERO;
		this.totalComprasCRMCajaITD = BigDecimal.ZERO;

	}
	
	public Long getIdEmpresa() {
		return idEmpresa;
	}

	public void setIdEmpresa(Long idEmpresa) {
		this.idEmpresa = idEmpresa;
	}

	public BigDecimal getTotalIngresoTituloMTD() {
		return totalIngresoTituloMTD;
	}
	public void setTotalIngresoTituloMTD(BigDecimal totalIngresoTituloMTD) {
		this.totalIngresoTituloMTD = totalIngresoTituloMTD;
	}
	public BigDecimal getTotalIngresoTituloYTD() {
		return totalIngresoTituloYTD;
	}
	public void setTotalIngresoTituloYTD(BigDecimal totalIngresoTituloYTD) {
		this.totalIngresoTituloYTD = totalIngresoTituloYTD;
	}
	public BigDecimal getTotalIngresoTituloITD() {
		return totalIngresoTituloITD;
	}
	public void setTotalIngresoTituloITD(BigDecimal totalIngresoTituloITD) {
		this.totalIngresoTituloITD = totalIngresoTituloITD;
	}
	public BigDecimal getTotalRetiroTituloMTD() {
		return totalRetiroTituloMTD;
	}
	public void setTotalRetiroTituloMTD(BigDecimal totalRetiroTituloMTD) {
		this.totalRetiroTituloMTD = totalRetiroTituloMTD;
	}
	public BigDecimal getTotalRetiroTituloYTD() {
		return totalRetiroTituloYTD;
	}
	public BigDecimal getComprasTitulosMTDRentabilidad() {
		return comprasTitulosMTDRentabilidad;
	}
	public void setComprasTitulosMTDRentabilidad(BigDecimal comprasTitulosMTDRentabilidad) {
		this.comprasTitulosMTDRentabilidad = comprasTitulosMTDRentabilidad;
	}
	public BigDecimal getComprasTitulosYTDRentabilidad() {
		return comprasTitulosYTDRentabilidad;
	}
	public void setComprasTitulosYTDRentabilidad(BigDecimal comprasTitulosYTDRentabilidad) {
		this.comprasTitulosYTDRentabilidad = comprasTitulosYTDRentabilidad;
	}
	public BigDecimal getComprasTitulosITDRentabilidad() {
		return comprasTitulosITDRentabilidad;
	}
	public void setComprasTitulosITDRentabilidad(BigDecimal comprasTitulosITDRentabilidad) {
		this.comprasTitulosITDRentabilidad = comprasTitulosITDRentabilidad;
	}
	public BigDecimal getVentasTitulosMTDRentabilidad() {
		return ventasTitulosMTDRentabilidad;
	}
	public void setVentasTitulosMTDRentabilidad(BigDecimal ventasTitulosMTDRentabilidad) {
		this.ventasTitulosMTDRentabilidad = ventasTitulosMTDRentabilidad;
	}
	public BigDecimal getVentasTitulosYTDRentabilidad() {
		return ventasTitulosYTDRentabilidad;
	}
	public void setVentasTitulosYTDRentabilidad(BigDecimal ventasTitulosYTDRentabilidad) {
		this.ventasTitulosYTDRentabilidad = ventasTitulosYTDRentabilidad;
	}
	public BigDecimal getVentasTitulosITDRentabilidad() {
		return ventasTitulosITDRentabilidad;
	}
	public void setVentasTitulosITDRentabilidad(BigDecimal ventasTitulosITDRentabilidad) {
		this.ventasTitulosITDRentabilidad = ventasTitulosITDRentabilidad;
	}
	public void setTotalRetiroTituloYTD(BigDecimal totalRetiroTituloYTD) {
		this.totalRetiroTituloYTD = totalRetiroTituloYTD;
	}
	public BigDecimal getTotalRetiroTituloITD() {
		return totalRetiroTituloITD;
	}
	public void setTotalRetiroTituloITD(BigDecimal totalRetiroTituloITD) {
		this.totalRetiroTituloITD = totalRetiroTituloITD;
	}
	public Long getIdCliente() {
		return idCliente;
	}
	public void setIdCliente(Long idCliente) {
		this.idCliente = idCliente;
	}
	public BigDecimal getTotalAbonosCajaMTD() {
		return totalAbonosCajaMTD;
	}
	public void setTotalAbonosCajaMTD(BigDecimal totalAbonosCajaMTD) {
		this.totalAbonosCajaMTD = totalAbonosCajaMTD;
	}
	public BigDecimal getSaldoInicial() {
		return saldoInicial;
	}
	public void setSaldoInicial(BigDecimal saldoInicial) {
		this.saldoInicial = saldoInicial;
	}
	public BigDecimal getTotalAbonosCajaYTD() {
		return totalAbonosCajaYTD;
	}
	public void setTotalAbonosCajaYTD(BigDecimal totalAbonosCajaYTD) {
		this.totalAbonosCajaYTD = totalAbonosCajaYTD;
	}
	public BigDecimal getTotalAbonosCajaITD() {
		return totalAbonosCajaITD;
	}
	public void setTotalAbonosCajaITD(BigDecimal totalAbonosCajaITD) {
		this.totalAbonosCajaITD = totalAbonosCajaITD;
	}
	public BigDecimal getAbonosCajaMTDRentabilidad() {
		return abonosCajaMTDRentabilidad;
	}
	public void setAbonosCajaMTDRentabilidad(BigDecimal abonosCajaMTDRentabilidad) {
		this.abonosCajaMTDRentabilidad = abonosCajaMTDRentabilidad;
	}
	public BigDecimal getAbonosCajaYTDRentabilidad() {
		return abonosCajaYTDRentabilidad;
	}
	public void setAbonosCajaYTDRentabilidad(BigDecimal abonosCajaYTDRentabilidad) {
		this.abonosCajaYTDRentabilidad = abonosCajaYTDRentabilidad;
	}
	public BigDecimal getAbonosCajaITDRentabilidad() {
		return abonosCajaITDRentabilidad;
	}
	public void setAbonosCajaITDRentabilidad(BigDecimal abonosCajaITDRentabilidad) {
		this.abonosCajaITDRentabilidad = abonosCajaITDRentabilidad;
	}
	public BigDecimal getTotalAbonosCajaMTDPeriodoAnterior() {
		return totalAbonosCajaMTDPeriodoAnterior;
	}
	public void setTotalAbonosCajaMTDPeriodoAnterior(BigDecimal totalAbonosCajaMTDPeriodoAnterior) {
		this.totalAbonosCajaMTDPeriodoAnterior = totalAbonosCajaMTDPeriodoAnterior;
	}
	public BigDecimal getTotalAbonosCajaYTDPeriodoAnterior() {
		return totalAbonosCajaYTDPeriodoAnterior;
	}
	public void setTotalAbonosCajaYTDPeriodoAnterior(BigDecimal totalAbonosCajaYTDPeriodoAnterior) {
		this.totalAbonosCajaYTDPeriodoAnterior = totalAbonosCajaYTDPeriodoAnterior;
	}
	public BigDecimal getTotalCargosCajaMTD() {
		return totalCargosCajaMTD;
	}
	public void setTotalCargosCajaMTD(BigDecimal totalCargosCajaMTD) {
		this.totalCargosCajaMTD = totalCargosCajaMTD;
	}
	public BigDecimal getTotalCargosCajaYTD() {
		return totalCargosCajaYTD;
	}
	public void setTotalCargosCajaYTD(BigDecimal totalCargosCajaYTD) {
		this.totalCargosCajaYTD = totalCargosCajaYTD;
	}
	public BigDecimal getTotalCargosCajaITD() {
		return totalCargosCajaITD;
	}
	public void setTotalCargosCajaITD(BigDecimal totalCargosCajaITD) {
		this.totalCargosCajaITD = totalCargosCajaITD;
	}
	public BigDecimal getCargosCajaMTDRentabilidad() {
		return cargosCajaMTDRentabilidad;
	}
	public void setCargosCajaMTDRentabilidad(BigDecimal cargosCajaMTDRentabilidad) {
		this.cargosCajaMTDRentabilidad = cargosCajaMTDRentabilidad;
	}
	public BigDecimal getCargosCajaYTDRentabilidad() {
		return cargosCajaYTDRentabilidad;
	}
	public void setCargosCajaYTDRentabilidad(BigDecimal cargosCajaYTDRentabilidad) {
		this.cargosCajaYTDRentabilidad = cargosCajaYTDRentabilidad;
	}
	public BigDecimal getCargosCajaITDRentabilidad() {
		return cargosCajaITDRentabilidad;
	}
	public void setCargosCajaITDRentabilidad(BigDecimal cargosCajaITDRentabilidad) {
		this.cargosCajaITDRentabilidad = cargosCajaITDRentabilidad;
	}
	public BigDecimal getTotalCargosCajaMTDPeriodoAnterior() {
		return totalCargosCajaMTDPeriodoAnterior;
	}
	public void setTotalCargosCajaMTDPeriodoAnterior(BigDecimal totalCargosCajaMTDPeriodoAnterior) {
		this.totalCargosCajaMTDPeriodoAnterior = totalCargosCajaMTDPeriodoAnterior;
	}
	public BigDecimal getTotalCargosCajaYTDPeriodoAnterior() {
		return totalCargosCajaYTDPeriodoAnterior;
	}
	public void setTotalCargosCajaYTDPeriodoAnterior(BigDecimal totalCargosCajaYTDPeriodoAnterior) {
		this.totalCargosCajaYTDPeriodoAnterior = totalCargosCajaYTDPeriodoAnterior;
	}
	public BigDecimal getTotalVentasCRMTitulosMTD() {
		return totalVentasCRMTitulosMTD;
	}

	public void setTotalVentasCRMTitulosMTD(BigDecimal totalVentasCRMTitulosMTD) {
		this.totalVentasCRMTitulosMTD = totalVentasCRMTitulosMTD;
	}

	public BigDecimal getTotalVentasCRMTitulosYTD() {
		return totalVentasCRMTitulosYTD;
	}

	public void setTotalVentasCRMTitulosYTD(BigDecimal totalVentasCRMTitulosYTD) {
		this.totalVentasCRMTitulosYTD = totalVentasCRMTitulosYTD;
	}

	public BigDecimal getTotalVentasCRMTitulosITD() {
		return totalVentasCRMTitulosITD;
	}

	public void setTotalVentasCRMTitulosITD(BigDecimal totalVentasCRMTitulosITD) {
		this.totalVentasCRMTitulosITD = totalVentasCRMTitulosITD;
	}

	public BigDecimal getTotalComprasCRMTitulosMTD() {
		return totalComprasCRMTitulosMTD;
	}

	public void setTotalComprasCRMTitulosMTD(BigDecimal totalComprasCRMTitulosMTD) {
		this.totalComprasCRMTitulosMTD = totalComprasCRMTitulosMTD;
	}

	public BigDecimal getTotalComprasCRMTitulosYTD() {
		return totalComprasCRMTitulosYTD;
	}

	public void setTotalComprasCRMTitulosYTD(BigDecimal totalComprasCRMTitulosYTD) {
		this.totalComprasCRMTitulosYTD = totalComprasCRMTitulosYTD;
	}

	public BigDecimal getTotalComprasCRMTitulosITD() {
		return totalComprasCRMTitulosITD;
	}

	public void setTotalComprasCRMTitulosITD(BigDecimal totalComprasCRMTitulosITD) {
		this.totalComprasCRMTitulosITD = totalComprasCRMTitulosITD;
	}

	public BigDecimal getTotalVentasCRMCajaMTD() {
		return totalVentasCRMCajaMTD;
	}

	public void setTotalVentasCRMCajaMTD(BigDecimal totalVentasCRMCajaMTD) {
		this.totalVentasCRMCajaMTD = totalVentasCRMCajaMTD;
	}

	public BigDecimal getTotalVentasCRMCajaYTD() {
		return totalVentasCRMCajaYTD;
	}

	public void setTotalVentasCRMCajaYTD(BigDecimal totalVentasCRMCajaYTD) {
		this.totalVentasCRMCajaYTD = totalVentasCRMCajaYTD;
	}

	public BigDecimal getTotalVentasCRMCajaITD() {
		return totalVentasCRMCajaITD;
	}

	public void setTotalVentasCRMCajaITD(BigDecimal totalVentasCRMCajaITD) {
		this.totalVentasCRMCajaITD = totalVentasCRMCajaITD;
	}

	public BigDecimal getTotalComprasCRMCajaMTD() {
		return totalComprasCRMCajaMTD;
	}

	public void setTotalComprasCRMCajaMTD(BigDecimal totalComprasCRMCajaMTD) {
		this.totalComprasCRMCajaMTD = totalComprasCRMCajaMTD;
	}

	public BigDecimal getTotalComprasCRMCajaYTD() {
		return totalComprasCRMCajaYTD;
	}

	public void setTotalComprasCRMCajaYTD(BigDecimal totalComprasCRMCajaYTD) {
		this.totalComprasCRMCajaYTD = totalComprasCRMCajaYTD;
	}

	public BigDecimal getTotalComprasCRMCajaITD() {
		return totalComprasCRMCajaITD;
	}

	public void setTotalComprasCRMCajaITD(BigDecimal totalComprasCRMCajaITD) {
		this.totalComprasCRMCajaITD = totalComprasCRMCajaITD;
	}

	@Override
	public String toString() {
		return Utils.printJson(this, false);
	}
}
