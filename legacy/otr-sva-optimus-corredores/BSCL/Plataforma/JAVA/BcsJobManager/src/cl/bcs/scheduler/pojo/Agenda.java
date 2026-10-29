package cl.bcs.scheduler.pojo;

import cl.bcs.utils.Utils;

public class Agenda {
	int idAgenda;
	String idTipoPrograma;
	String esDemonio;
	String diaDeMes;
	String mes;
	String hora;
	String minuto;
	String soloDiasHabiles;
	String soloFinDeMes;
	String habilitado;
	String nombreClasePrograma;
	String tipoClasePrograma;
	String siguienteFechaEjecucion;
	String ultimaFechaEjecucion;

	public String toString() {
		return Utils.printJson(this, false);
	}
	
	public String getUltimaFechaEjecucion() {
		return ultimaFechaEjecucion;
	}

	public void setUltimaFechaEjecucion(String ultimaFechaEjecucion) {
		this.ultimaFechaEjecucion = ultimaFechaEjecucion;
	}

	public Agenda() {
		super();
	}

	public String getSiguienteFechaEjecucion() {
		return siguienteFechaEjecucion;
	}

	public void setSiguienteFechaEjecucion(String siguienteFechaEjecucion) {
		this.siguienteFechaEjecucion = siguienteFechaEjecucion;
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

	public String getEsDemonio() {
		return esDemonio;
	}

	public void setEsDemonio(String esDemonio) {
		this.esDemonio = esDemonio;
	}

	public String getDiaDeMes() {
		return diaDeMes;
	}

	public void setDiaDeMes(String diaDeMes) {
		this.diaDeMes = diaDeMes;
	}

	public String getMes() {
		return mes;
	}

	public void setMes(String mes) {
		this.mes = mes;
	}

	public String getHora() {
		return hora;
	}

	public void setHora(String hora) {
		this.hora = hora;
	}

	public String getMinuto() {
		return minuto;
	}

	public void setMinuto(String minuto) {
		this.minuto = minuto;
	}

	public String getSoloDiasHabiles() {
		return soloDiasHabiles;
	}

	public void setSoloDiasHabiles(String soloDiasHabiles) {
		this.soloDiasHabiles = soloDiasHabiles;
	}

	public String getSoloFinDeMes() {
		return soloFinDeMes;
	}

	public void setSoloFinDeMes(String soloFinDeMes) {
		this.soloFinDeMes = soloFinDeMes;
	}

	public String getHabilitado() {
		return habilitado;
	}

	public void setHabilitado(String habilitado) {
		this.habilitado = habilitado;
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
