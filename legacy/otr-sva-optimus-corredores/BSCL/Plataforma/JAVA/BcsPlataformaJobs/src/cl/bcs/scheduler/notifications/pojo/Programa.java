package cl.bcs.scheduler.notifications.pojo;

 
public class Programa {
	int idPrograma;
	int idGrupoParametro;
	int idAgenda;
	String idTipoPrograma;
	int intento;
	String idUsuario;
	int maximoIntentos;
	String nombreClasePrograma;
	String tipoClasePrograma;
	String descripcion;
	String creaParametros;
	public String getCreaParametros() {
		return creaParametros;
	}

	public void setCreaParametros(String creaParametros) {
		this.creaParametros = creaParametros;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getTipoClasePrograma() {
		return tipoClasePrograma;
	}

	public void setTipoClasePrograma(String tipoClasePrograma) {
		this.tipoClasePrograma = tipoClasePrograma;
	}

	String idEstado;
	String resultado;
	String fechaCreacion;
	int fechaEjecucion;
	String fechaFin;

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

	public String getIdEstado() {
		return idEstado;
	}

	public void setIdEstado(String idEstado) {
		this.idEstado = idEstado;
	}

	public Programa() {
		super();
	}

	public int getIdPrograma() {
		return idPrograma;
	}

	public void setIdPrograma(int idPrograma) {
		this.idPrograma = idPrograma;
	}

	public int getIdGrupoParametro() {
		return idGrupoParametro;
	}

	public void setIdGrupoParametro(int idGrupoParametro) {
		this.idGrupoParametro = idGrupoParametro;
	}

	public int getIdAgenda() {
		return idAgenda;
	}

	public void setIdAgenda(int idAgenda) {
		this.idAgenda = idAgenda;
	}

	public String getIdTipoPrograma() {
		return idTipoPrograma;
	}

	public void setIdTipoPrograma(String idTipoPrograma) {
		this.idTipoPrograma = idTipoPrograma;
	}

	public int getIntento() {
		return intento;
	}

	public void setIntento(int intento) {
		this.intento = intento;
	}

	public String getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(String idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getResultado() {
		return resultado;
	}

	public void setResultado(String resultado) {
		this.resultado = resultado;
	}

	public String getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(String fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public int getFechaEjecucion() {
		return fechaEjecucion;
	}

	public void setFechaEjecucion(int fechaEjecucion) {
		this.fechaEjecucion = fechaEjecucion;
	}

	public String getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(String fechaFin) {
		this.fechaFin = fechaFin;
	}

}

