package cl.bcs.socionegocio.pojo;

import java.util.List;

public abstract class SalidaEstandar {

	public long P_OUT_ADVERTENCIASCONTADOR;
	public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
	public long P_OUT_ERRORESCONTADOR;
	public List<ErrorDetalle> P_OUT_ERRORESDETALLE;
	
	public void addAdvertencia(String codigo, String descripcion){
		P_OUT_ADVERTENCIASCONTADOR++;
		P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(codigo, "N", descripcion));
	}
	public void addError(String codigo, String descripcion){
		P_OUT_ERRORESCONTADOR++;
		P_OUT_ERRORESDETALLE.add(new ErrorDetalle(codigo, descripcion));
	}
}
