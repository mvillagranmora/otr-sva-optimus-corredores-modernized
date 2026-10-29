package cl.bcs.tesoreria.javacall.contrato;

import cl.bcs.utils.Utils;

public class AutorizadoEtapaEvento {
	public Long			IdAutorizadoEtapaEvento; 
	public Long			IdEtapa; 
	public Long			IdEtapaEvento; 
	public Long			IdUsuario; 
	public Long			IdRol; 
	public Long			IdUsuarioReemplaza; 
	public java.sql.Date			ReemplazaDesde; 
	public java.sql.Date			ReemplazaHasta; 
	public String					Observaciones; 
	public java.sql.Timestamp		FechaHoraCambioEstado; 
	public String					IdEstado; 
	public String toString() {
		return Utils.printJson(this, false);
	}			
}