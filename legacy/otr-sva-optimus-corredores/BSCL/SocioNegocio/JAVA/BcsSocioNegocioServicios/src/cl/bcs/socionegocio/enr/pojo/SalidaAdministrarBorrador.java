package cl.bcs.socionegocio.enr.pojo;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;

public class SalidaAdministrarBorrador extends BcsPojo {
	public Long	P_OUT_ERRORESCONTADOR;
	public List<MensajeSalida> P_OUT_MENSAJES;
	public List<ResultadoAdministrarBorradorEntidad> P_OUT_REFID;
	
	public SalidaAdministrarBorrador() {
		this.P_OUT_ERRORESCONTADOR = 0L;
		this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		this.P_OUT_REFID = new ArrayList<ResultadoAdministrarBorradorEntidad>();
	}
	
	public void agregarValidacionesSalida(List<Validacion> validaciones) {
		for(Validacion validacion : validaciones) {
			agregarValidacionSalida(validacion);
		}
	}
	
	public void agregarValidacionSalida(Validacion validacion) {
		if(validacion.EsError) {
			this.P_OUT_ERRORESCONTADOR += 1L;
			this.P_OUT_MENSAJES.add(validacion.MensajeError);
		}
	}
	
	public void agregarErroresMensajesSalida(SalidaAdministrarBorrador salidaAdministrarBorrador) {
		this.P_OUT_ERRORESCONTADOR += salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR;
		this.P_OUT_MENSAJES.addAll(salidaAdministrarBorrador.P_OUT_MENSAJES);
	}
}
