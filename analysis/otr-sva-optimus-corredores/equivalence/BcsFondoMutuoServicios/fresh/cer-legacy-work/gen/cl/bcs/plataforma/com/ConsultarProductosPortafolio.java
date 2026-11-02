// GENERATED DOUBLE of the legacy cl.bcs.plataforma.com.ConsultarProductosPortafolio by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarProductosPortafolio implements JavaCall {
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { return cl.bcs.oracle.Doubles.answer("ConsultarProductosPortafolio", "call", new Object[]{conn, in}, JsonObject.class); }
	
	public static class DetallePerfilProductoProceso {
		public Long IdPortafolio;
		public String IdPerfilProductos;
		public String IdEstadoPerfilProducto;
		public String IdEstadoDetallePerfilProducto;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String EsPerfilPersonalizado;
		public String PermiteRuteo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Trace
	public ConsultarProductosPortafolio_Salida logica(Connection conn,ConsultarProductosPortafolio_Entrada entrada) throws Exception { return cl.bcs.oracle.Doubles.answer("ConsultarProductosPortafolio", "logica", new Object[]{conn, entrada}, ConsultarProductosPortafolio_Salida.class); }
	
	public static class ConsultarProductosPortafolio_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<PortafolioEntrada> p_portafolios;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ConsultarProductosPortafolio_Entrada() {
			p_portafolios = new ArrayList<PortafolioEntrada>();
		}
	}
	
	public static class PortafolioEntrada {
		public Long IdPortafolio;
		
		@Override
		public boolean equals(Object o) {
			PortafolioEntrada e = (PortafolioEntrada)o;
			return this.IdPortafolio.equals(e.IdPortafolio);
		}
		
		@Override
		public int hashCode() {
			return this.IdPortafolio.hashCode();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetallePerfilProducto {
		public Long IdPortafolio;
		public String IdPerfilProductos;
		public String IdEstadoPerfilProducto;
		public String IdEstadoDetallePerfilProducto;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String EsPerfilPersonalizado;
		public String PermiteRuteo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarProductosPortafolio_Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> p_out_Mensajes;
		public List<DetallePerfilProducto> p_out_PortafolioProductos;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ConsultarProductosPortafolio_Salida () {
			p_out_Mensajes = new ArrayList<MensajeSalida>();
			p_out_PortafolioProductos = new ArrayList<DetallePerfilProducto>();
		}
		
	
	}
	
	public static void main(String[] args) throws Exception { throw new UnsupportedOperationException("oracle double ConsultarProductosPortafolio.main: not part of the double"); }
	

}