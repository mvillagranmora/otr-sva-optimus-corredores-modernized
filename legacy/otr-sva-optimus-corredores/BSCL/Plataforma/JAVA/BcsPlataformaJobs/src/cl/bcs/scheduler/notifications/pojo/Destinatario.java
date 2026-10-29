package cl.bcs.scheduler.notifications.pojo;

public class Destinatario
{
	public int idDestinatario;
	public int idSecuenciaDestinatario;
	public Destinatario() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Destinatario(int idSecuenciaDestinatario,int idDestinatario, int idEmail, String tipoDestinatario, String destinatario) {
		super();
		this.idSecuenciaDestinatario = idSecuenciaDestinatario;
		this.idDestinatario = idDestinatario;
		this.idEmail = idEmail;
		this.tipoDestinatario = tipoDestinatario;
		this.destinatario = destinatario;
	}
	public int idEmail;
	public String tipoDestinatario;
	public String destinatario;
	
	public int getIdDestinatario() {
		return idDestinatario;
	}
	public void setIdDestinatario(int idDestinatario) {
		this.idDestinatario = idDestinatario;
	}
	public int getIdEmail() {
		return idEmail;
	}
	public void setIdEmail(int idEmail) {
		this.idEmail = idEmail;
	}
	public String getTipoDestinatario() {
		return tipoDestinatario;
	}
	public void setTipoDestinatario(String tipoDestinatario) {
		this.tipoDestinatario = tipoDestinatario;
	}
	public String getDestinatario() {
		return destinatario;
	}
	public void setDestinatario(String destinatario) {
		this.destinatario = destinatario;
	}
	public int getIdSecuenciaDestinatario() {
		return idSecuenciaDestinatario;
	}
	public void setIdSecuenciaDestinatario(int idSecuenciaDestinatario) {
		this.idSecuenciaDestinatario = idSecuenciaDestinatario;
	}
	
	public String toString(){
		return "{idSecuenciaDestinatario: "+ this.idSecuenciaDestinatario+
				", idDestinatario: " + idDestinatario+
				", idEmail : " +  idEmail+
				", tipoDestinatario : " +  tipoDestinatario+
				", destinatario : " +  destinatario+"}";
	}

}
