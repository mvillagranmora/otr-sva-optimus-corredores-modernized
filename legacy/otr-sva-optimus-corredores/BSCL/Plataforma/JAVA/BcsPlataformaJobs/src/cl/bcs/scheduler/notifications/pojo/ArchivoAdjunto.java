package cl.bcs.scheduler.notifications.pojo;

 
public class ArchivoAdjunto {
	
	public int IdSecuenciaArchivoAdjunto;
	public int IdArchivoAdjunto;
	public int IdEmail;
	public String Nombre;
	public byte[] Archivo;
	public String ArchivoNoProcesado;
	public String getArchivoNoProcesado() {
		return ArchivoNoProcesado;
	}
	public void setArchivoNoProcesado(String archivoNoProcesado) {
		ArchivoNoProcesado = archivoNoProcesado;
	}
	public ArchivoAdjunto(int idSecuenciaArchivoAdjunto, int idArchivoAdjunto, int idEmail, String nombre,
			byte[] archivo) {
		super();
		IdSecuenciaArchivoAdjunto = idSecuenciaArchivoAdjunto;
		IdArchivoAdjunto = idArchivoAdjunto;
		IdEmail = idEmail;
		Nombre = nombre;
		Archivo = archivo;
	}
	public ArchivoAdjunto() {
		super();
		// TODO Auto-generated constructor stub
	}
	public int getIdSecuenciaArchivoAdjunto() {
		return IdSecuenciaArchivoAdjunto;
	}
	public void setIdSecuenciaArchivoAdjunto(int idSecuenciaArchivoAdjunto) {
		IdSecuenciaArchivoAdjunto = idSecuenciaArchivoAdjunto;
	}
	public int getIdArchivoAdjunto() {
		return IdArchivoAdjunto;
	}
	public void setIdArchivoAdjunto(int idArchivoAdjunto) {
		IdArchivoAdjunto = idArchivoAdjunto;
	}
	public int getIdEmail() {
		return IdEmail;
	}
	public void setIdEmail(int idEmail) {
		IdEmail = idEmail;
	}
	public String getNombre() {
		return Nombre;
	}
	public void setNombre(String nombre) {
		Nombre = nombre;
	}
	public byte[] getArchivo() {
		return Archivo;
	}
	public void setArchivo(byte[] archivo) {
		Archivo = archivo;
	}

}
