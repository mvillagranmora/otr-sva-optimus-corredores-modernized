package cl.bcs.scheduler.pojo;

public class TipoTarea {
	String idTipoPrograma;
	String descripcion;
	int maximoIntentos;
	String nombreClasePrograma;
	String tipoClasePrograma;

	public TipoTarea() {
		super();
	}

	public String getIdTipoPrograma() {
		return idTipoPrograma;
	}

	public void setIdTipoPrograma(String idTipoPrograma) {
		this.idTipoPrograma = idTipoPrograma;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public int getMaximoIntentos() {
		return maximoIntentos;
	}

	public void setMaximoIntentos(int maximoIntentos) {
		this.maximoIntentos = maximoIntentos;
	}

	public String getNombreClasePrograma() {
		return nombreClasePrograma;
	}

	public void setNombreClasePrograma(String nombreClasePrograma) {
		this.nombreClasePrograma = nombreClasePrograma;
	}

	public String getTipoClasePrograma() {
		return tipoClasePrograma;
	}

	public void setTipoClasePrograma(String tipoClasePrograma) {
		this.tipoClasePrograma = tipoClasePrograma;
	}
}
