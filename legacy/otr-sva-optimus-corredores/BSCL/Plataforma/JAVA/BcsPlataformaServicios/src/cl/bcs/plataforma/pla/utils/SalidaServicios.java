package cl.bcs.plataforma.pla.utils;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Utils;

public abstract class SalidaServicios{

	public static class AdvertenciaDetalle {
		public AdvertenciaDetalle() {}
		public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion){
			this.Codigo = codigo;
			this.RequiereAutorizacion = requiereAutorizacion;
			this.Descripcion = descripcion;
		}
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ErrorDetalle {
		public ErrorDetalle() {}
		public ErrorDetalle(String codigo, String descripcion){
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
		public String Codigo;
		public String Descripcion;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaServicios() {
		this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
		this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		this.P_OUT_ADVERTENCIASCONTADOR = 0L;
		this.P_OUT_ERRORESCONTADOR = 0L;
	}
	
	public Long P_OUT_ADVERTENCIASCONTADOR;
	public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
	public Long P_OUT_ERRORESCONTADOR;
	public List<ErrorDetalle> P_OUT_ERRORESDETALLE;
	
	public void addAdvertencia(String codigo, String descripcion){
		this.P_OUT_ADVERTENCIASCONTADOR++;
		this.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(codigo, "N", descripcion));
	}
	public void addError(String codigo, String descripcion){
		this.P_OUT_ERRORESCONTADOR++;
		this.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(codigo, descripcion));
	}
	
	public void parseMensajeSalida(List<MensajeSalida> mensajesSalida) {
		mensajesSalida.forEach(msj ->{
			if(msj.tipo.equals(MensajeSalida.MENSAJE_ERROR)) {
				this.addError(msj.codigo, msj.descripcion);
			}else {
				this.addAdvertencia(msj.codigo, msj.descripcion);
			}
		});
	}
}
