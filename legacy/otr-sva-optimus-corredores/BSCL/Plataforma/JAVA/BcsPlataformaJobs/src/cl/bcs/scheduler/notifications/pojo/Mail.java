package cl.bcs.scheduler.notifications.pojo;

public class Mail
{
	public Mail() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Mail(int idMail, String fechaCreacion, String remitente, String asunto, String mensajeCuerpo, int idDestinatarios,
			int idArchivosAdjuntos, String estadoEnviado, String fechaEnviado) {
		super();
		this.idMail = idMail;
		this.fechaCreacion = fechaCreacion;
		this.remitente = remitente;
		this.asunto = asunto;
		this.mensajeCuerpo = mensajeCuerpo;
		this.idDestinatarios = idDestinatarios;
		this.idArchivosAdjuntos = idArchivosAdjuntos;
		this.estadoEnviado = estadoEnviado;
		this.fechaEnviado = fechaEnviado;
	}
	public int idMail;
	public String fechaCreacion;
	public String remitente;
	public String asunto;
	public String mensajeCuerpo;
	public int idDestinatarios;
	public int idArchivosAdjuntos;
	public String estadoEnviado;
	public String fechaEnviado;
	
	public int getIdMail() {
		return idMail;
	}
	public void setIdMail(int idMail) {
		this.idMail = idMail;
	}
	public String getFechaCreacion() {
		return fechaCreacion;
	}
	public void setFechaCreacion(String fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}
	public String getRemitente() {
		return remitente;
	}
	public void setRemitente(String remitente) {
		this.remitente = remitente;
	}
	public String getAsunto() {
		return asunto;
	}
	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}
	public String getMensajeCuerpo() {
		return mensajeCuerpo;
	}
	public void setMensajeCuerpo(String mensajeCuerpo) {
		this.mensajeCuerpo = mensajeCuerpo;
	}
	public int getIdDestinatarios() {
		return idDestinatarios;
	}
	public void setIdDestinatarios(int idDestinatarios) {
		this.idDestinatarios = idDestinatarios;
	}
	public int getIdArchivosAdjuntos() {
		return idArchivosAdjuntos;
	}
	public void setIdArchivosAdjuntos(int idArchivosAdjuntos) {
		this.idArchivosAdjuntos = idArchivosAdjuntos;
	}
	public String getEstadoEnviado() {
		return estadoEnviado;
	}
	public void setEstadoEnviado(String estadoEnviado) {
		this.estadoEnviado = estadoEnviado;
	}
	public String getFechaEnviado() {
		return fechaEnviado;
	}
	public void setFechaEnviado(String fechaEnviado) {
		this.fechaEnviado = fechaEnviado;
	}
	public String toString(){
		return "{idMail: " +  idMail+
				", fechaCreacion: " +  fechaCreacion+
				", remitente: " +  remitente+
				", asunto: " +  asunto+
				", mensajeCuerpo: " +  mensajeCuerpo+
				", idDestinatarios: " +  idDestinatarios+
				", idArchivosAdjuntos : " +  idArchivosAdjuntos+
				", estadoEnviado: " +  estadoEnviado+
				", fechaEnviado: " +  fechaEnviado+"}";
	}
}
