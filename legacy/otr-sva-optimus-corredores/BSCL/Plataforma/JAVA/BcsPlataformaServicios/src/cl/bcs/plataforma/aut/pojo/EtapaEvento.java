package cl.bcs.plataforma.aut.pojo;

import cl.bcs.utils.Utils;

public class EtapaEvento {
	public Long						IdEtapaEvento; 
	public Long						IdEvento; 
	public Long						IdEtapa; 
	public String					Codigo; 
	public String					Nombre; 
	public Integer					Orden; 
	public String					TipoAutorizacion; 
	public Integer					MinimoAutorizan; 
	public Long						IdUsuarioCambioEstado; 
	public Long						IdRolCambioEstado; 
	public java.sql.Timestamp		FechaHoraCambioEstado; 
	public String					IdEstado; 
	public String toString() {
		return Utils.printJson(this, false);
	}		
}