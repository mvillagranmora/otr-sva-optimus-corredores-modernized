package cl.bcs.normativa.cartola;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;

import cl.bcs.utils.Utils;

public interface BcsCartola {
	
	public  abstract SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception;
	
	public static class EntradaCartola{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Date p_FechaDesde;
		public Date p_FechaHasta;
		public Date  p_FechaHastaOrigen;
		public String p_AgrupaMovimientos;
		public String p_FiltroSoloMonedaSistema;
		public String p_FiltroUbicacionBoveda;
		public String p_Moneda;
		public String p_IdTipoCanal;
		public List<Dato> p_Datos;
		public Long p_IdCliente;
		public Long p_IdTrabajo;
		
		public EntradaCartola(){
			this.p_FiltroSoloMonedaSistema = "S";
			this.p_AgrupaMovimientos = "N";
			this.p_FiltroUbicacionBoveda = "";
			this.p_Datos = new ArrayList<Dato>();
			this.p_IdTrabajo= 0L;
		}
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Dato{
		public Long IdCliente;
		public Long IdPortafolio;
		public Dato() {
		}
		public Dato(Long idCliente, Long idPortafolio ) 
		{
			this.IdCliente = idCliente;
			this.IdPortafolio = idPortafolio; 
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCartola{
		public HashMap<String,Object> P_OUT_SALIDA;
		
		public SalidaCartola(){
			this.P_OUT_SALIDA = new HashMap<String,Object>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/**
	 * Load resources from path package
	 * @param path
	 * @return
	 */
	default String loadResourceToString(final String path){		
	    final InputStream stream = this.getClass().getClassLoader().getResourceAsStream(path);
	    if (stream==null) {
	    	throw new IllegalStateException("can't load resource:"+path);
	    }
	    try{
	        return IOUtils.toString(stream);
	    } catch(final Exception e){
	        throw new IllegalStateException(e);
	    } finally{
	        IOUtils.closeQuietly(stream);
	    }
	}
}
