package cl.bcs.tesoreria.javacall.contrato;

import java.math.BigDecimal;
import java.sql.Timestamp;

import cl.bcs.utils.Utils;

public class Evento {
	public Long			IdEvento; 
	public String		IdTipoEvento; 
	public Long			IdEmpresa; 
	public String		IdModulo; 
	public Long			IdUsuario; 
	public java.sql.Date			Fecha; 
	public java.sql.Timestamp			FechaHoraVigencia; 
	public java.sql.Timestamp			FechaHoraRegistro; 
	public Long			IdUsuarioSuperAutorizador; 
	public Long			IdRolSuperAutorizador; 
	public String		EtapasSecuenciales; 
	public String		IdTipoOperacion; 
	public String		IdConcepto; 
	public String		NumeroOperacionConcepto; 
	public java.sql.Timestamp			FechaOperacionConcepto; 
	public Long			IdCliente; 
	public Long			IdFuncionario; 
	public Long			IdProveedor; 
	public String		IdTipoProducto; 
	public String		IdClasificacionTipoInstrumento; 
	public String		IdTipoInstrumento; 
	public Long			IdInstrumento; 
	public String		Nemo; 
	public Long			Cantidad; 
	public BigDecimal	Monto; 
	public BigDecimal	Precio; 
	public BigDecimal	Tasa; 
	public String		IdMoneda; 
	public String		Glosa; 
	public Long			IdUsuarioCambioEstado; 
	public Long			IdRolCambioEstado; 
	public java.sql.Timestamp			FechaHoraCambioEstado; 
	public String		IdEstado; 
	public Long			IdGrupo;
	public String       IdCanal;
	public String toString() {
		return Utils.printJson(this, false);
	}
	
	public Evento() {
		this.IdCanal="OPT";
	}
}
