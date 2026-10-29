package cl.bcs.custodia.pojo;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import cl.bcs.utils.Utils;

public class EventoCapitalClienteFormaDePago {

	public Long IdEventoCapitalClienteFormaDePago;
	public Long IdEventoCapitalCliente;
	public Long IdEventoCapital;
	public Long IdPreferenciaPagoPortafolio;
	public String IdTipoMovimiento;
	public String IdModalidadPago;
	public String IdMedioDePago;
	public String NombreMedioDePago;
	public Long IdBanco;
	public String NombreBanco;
	public String IdTipoCuentaBancaria;
	public String NombreTipoCuentaBancaria;
	public Long IdCuentaBancaria;
	public String NombreCuentaBancaria;
	public String NumeroCuentaBancaria;
	public String IdTipoTarjetaBancaria;
	public String NumeroTarjetaBancaria;
	public String NumeroMedioDePago;
	public String Sucursal;
	public Timestamp FechaVctoDocumento;
	public BigDecimal Monto;
	public Timestamp FechaHoraIngresoRegistro;
	public Long IdUsuarioIngreso;
	public String IdEstado;
	public String IdMoneda;

	public String toString() {
		return Utils.printJson(this, false);
	};
}
