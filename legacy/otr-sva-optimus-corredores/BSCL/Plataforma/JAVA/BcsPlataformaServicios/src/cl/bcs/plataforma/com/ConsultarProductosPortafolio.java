package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.com.ConsultarProductosPortafolio.PortafolioEntrada;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarProductosPortafolio implements JavaCall {
	
	private final String SQL_CONSULTAR_PERFIL_PRODUCTO_BASE = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarProductosPortafolio_ConsultarDetallePerfilProducto.sql");
	private final String SQL_CONSULTAR_PERFIL_PRODUCTO_EXTRA = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarProductosPortafolio_ConsultarDetallePerfilProductoExtra.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
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
	public ConsultarProductosPortafolio_Salida logica(Connection conn,ConsultarProductosPortafolio_Entrada entrada) throws Exception {
		ConsultarProductosPortafolio_Salida salida = new ConsultarProductosPortafolio_Salida();
		
		SQL _sql = new SQL();
		
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		ArrayList<Long> idsPortafolios = new ArrayList<Long>();
		for (PortafolioEntrada portafolio : entrada.p_portafolios) {
			if (!idsPortafolios.contains(portafolio.IdPortafolio)) {
				idsPortafolios.add(portafolio.IdPortafolio);
			}
		}
		params1.put("idsPortafolios", idsPortafolios);
		List<DetallePerfilProductoProceso> base = _sql.query(conn, SQL_CONSULTAR_PERFIL_PRODUCTO_BASE, params1, DetallePerfilProductoProceso.class);
		
		//productos base
		for (DetallePerfilProductoProceso det : base) {
			DetallePerfilProducto det1 = new DetallePerfilProducto();
			Utils.copyPojo(det, det1);
			salida.p_out_PortafolioProductos.add(det1);
			
		}
		
		//productos extra
		List<DetallePerfilProductoProceso> extras = _sql.query(conn, SQL_CONSULTAR_PERFIL_PRODUCTO_EXTRA, params1, DetallePerfilProductoProceso.class);
		for (DetallePerfilProductoProceso det : extras) {
			DetallePerfilProducto det1 = new DetallePerfilProducto();
			Utils.copyPojo(det, det1);
			salida.p_out_PortafolioProductos.add(det1);
		}
		return salida;
	}
	
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
	
	public static void main(String[] args) throws Exception {
		Connection con = PlataformaUtils.getDESAConnection();
		ConsultarProductosPortafolio p = new ConsultarProductosPortafolio();
		ConsultarProductosPortafolio_Entrada entrada = new ConsultarProductosPortafolio_Entrada();
		entrada.p_IdEmpresa = 1L;
		entrada.p_IdModulo = "ORD";
		entrada.p_IdUsuario = 4L;
		PortafolioEntrada portafolio = new PortafolioEntrada();
		portafolio.IdPortafolio = 343710L;
		entrada.p_portafolios.add(portafolio);
		
		p.logica(con, entrada);
				
	}
	

}