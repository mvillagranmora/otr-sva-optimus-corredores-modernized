package cl.bcs.socionegocio.wbs.pojo;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;

public class SalidaAdministrarWS extends BcsPojo {
	public Long	P_OUT_ERRORESCONTADOR;
	public List<MensajeSalida> P_OUT_MENSAJES;
	public List<ResultadoAdministrarEntidadWS> P_OUT_REFID;
	
	public SalidaAdministrarWS() {
		this.P_OUT_ERRORESCONTADOR = 0L;
		this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		this.P_OUT_REFID = new ArrayList<ResultadoAdministrarEntidadWS>();
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
	
	public void agregarErroresMensajesSalida(SalidaAdministrarWS salidaAdministrarWS) {
		this.P_OUT_ERRORESCONTADOR += salidaAdministrarWS.P_OUT_ERRORESCONTADOR;
		this.P_OUT_MENSAJES.addAll(salidaAdministrarWS.P_OUT_MENSAJES);
	}
}
