package cl.bcs.plataforma.evt.pojo;

import java.sql.Timestamp;

import cl.bcs.utils.Utils;

//EVENTO DE NOTIFICACION
public class Evento {
	public Long 		IdEmpresa;
	public Long 		IdEvento;
	public String		CodigoTipoEvento;
	public Long 		IdUsuario;
	public Timestamp 	FechaCreacion;
	public String 		Procesado;
	
	public String 		TipoEntidadOrigen;
	public Long 		IdEntidadOrigen;
	public String 		TipoEntidadDestino;
	public String 		MensajeBody;
	public Long 		IdEntidadDestino;
	public String 		MensajeRecordatorio;
	public Long 		IdPersonaDestinatario;
	public Long 		IdPersonaRemitente;
	public Long 		IdPortafolio;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
	
}
