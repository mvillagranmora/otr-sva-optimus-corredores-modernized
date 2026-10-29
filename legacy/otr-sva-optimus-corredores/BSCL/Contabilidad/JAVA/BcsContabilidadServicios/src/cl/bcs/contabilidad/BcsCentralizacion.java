package cl.bcs.contabilidad;

import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import cl.bcs.contabilidad.pojo.ResultadoCentralizacion;
import cl.bcs.contabilidad.pojo.TabDatosEvento;
import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Utils;

public interface BcsCentralizacion {
	
	public abstract SalidaCentralizacion centralizar(Connection con,EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception;
	
	public static class SalidaCentralizacion {
		public List<MensajeSalida>	P_OUT_MENSAJES;	//DETALLES
		public Long					P_OUT_ERRORESCONTADOR = 0L;
		public List<ResultadoCentralizacion> P_OUT_RESULTADO;
		List<TabMovimientos> P_TABMOVIMIENTOS;
		List<TabEventos> P_TABEVENTOS;
		List<TabDatosMovimiento> P_TABDATOSMOVIMIENTO;
		List<TabDatosEvento> P_TABDATOSEVENTO;
		
		public SalidaCentralizacion() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCentralizacion>();
			this.P_TABMOVIMIENTOS = new ArrayList<TabMovimientos>();
			this.P_TABEVENTOS = new ArrayList<TabEventos>();
			this.P_TABDATOSMOVIMIENTO = new ArrayList<TabDatosMovimiento>();
			this.P_TABDATOSEVENTO = new ArrayList<TabDatosEvento>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public String toString(boolean pretty) {
			return Utils.printJson(this, pretty);
		}
	}
	
	public static class EntradaCentralizacion {
		public Long				p_IdEmpresa;
		public Long				p_IdUsuario;
		public String			p_IdModulo;
		public Long 			p_IdTipoCentralizacion;
		public String			p_FechaContable;
		public String			p_IdInforme;
		public Long				p_Ejercicio;
		public String			p_SimulaOperacion;
		
		public EntradaCentralizacion(){
			this.p_SimulaOperacion = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntradaCentralizacionInfoAdicional{
		public String			p_FechaSistema;
		public InformacionEmpresa p_Empresa;
		public String			p_LocalizacionSistema;
		public String			p_IdMonedaSistema;
		
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
	
	default List<ResultadoCentralizacion> llenarResultado(List<TabMovimientos> movimientos) throws Exception{
		List<ResultadoCentralizacion> resultados = new ArrayList<ResultadoCentralizacion>();
		for(TabMovimientos mov : movimientos){
			ResultadoCentralizacion resultado = new ResultadoCentralizacion();
			resultado = Utils.copyPojo(mov, resultado);
			resultado.Ejercicio = String.valueOf(mov.Ejercicio);
			resultados.add(resultado);
		}
		
		return resultados;
		
	} 
	
}
