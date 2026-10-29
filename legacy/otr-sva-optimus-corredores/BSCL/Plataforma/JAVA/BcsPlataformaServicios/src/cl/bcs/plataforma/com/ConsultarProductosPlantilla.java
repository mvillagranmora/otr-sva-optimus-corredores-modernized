package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarProductosPlantilla implements JavaCall {
	
	private final String SQL_CONSULTAR_PERFIL_PRODUCTO_BASE = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarProductosPlantilla_ConsultarDetallePerfilProducto.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static class DetallePerfilProductoProceso {
		public String IdPerfilProductos;
		public String IdEstadoPerfilProducto;
		public String IdEstadoDetallePerfilProducto;
		public String IdTipoProducto;
		public String NombreTipoProducto;
		public String IdTipoOperacion;
		public String NombreTipoOperacion;
		public String IdTipoCanalIngreso;
		public String NombreTipoCanalIngreso;
		public String EsPerfilPersonalizado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ConsultarProductosPlantilla_Salida logica(Connection conn,ConsultarProductosPlantilla_Entrada entrada) throws Exception {
		ConsultarProductosPlantilla_Salida salida = new ConsultarProductosPlantilla_Salida();
		
		SQL _sql = new SQL();
		
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		ArrayList<String> idsPerfilProductos = new ArrayList<String>();
		for (PerfilProductoEntrada perfil : entrada.p_Perfiles) {
			idsPerfilProductos.add(perfil.IdPerfilProductos);
		}
		params1.put("idsPerfilProductos", idsPerfilProductos);
		List<DetallePerfilProductoProceso> base = _sql.query(conn, SQL_CONSULTAR_PERFIL_PRODUCTO_BASE, params1, DetallePerfilProductoProceso.class);
		
		for (DetallePerfilProductoProceso det : base) {
			DetallePerfilProducto det1 = new DetallePerfilProducto();
			Utils.copyPojo(det, det1);
			salida.p_out_Productos.add(det1);
		}
		
		return salida;
	}
	
	public static class ConsultarProductosPlantilla_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<PerfilProductoEntrada> p_Perfiles;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ConsultarProductosPlantilla_Entrada() {
			p_Perfiles = new ArrayList<PerfilProductoEntrada>();
		}
	}
	
	public static class PerfilProductoEntrada {
		public String IdPerfilProductos;
	}
	
	public static class DetallePerfilProducto {
		public String IdPerfilProductos;
		public String IdEstadoPerfilProducto;
		public String IdEstadoDetallePerfilProducto;
		public String IdTipoProducto;
		public String NombreTipoProducto;
		public String IdTipoOperacion;
		public String NombreTipoOperacion;
		public String IdTipoCanalIngreso;
		public String NombreTipoCanalIngreso;
		public String EsPerfilPersonalizado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarProductosPlantilla_Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> p_out_Mensajes;
		public List<DetallePerfilProducto> p_out_Productos;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ConsultarProductosPlantilla_Salida () {
			p_out_Mensajes = new ArrayList<MensajeSalida>();
			p_out_Productos = new ArrayList<DetallePerfilProducto>();
		}
		
	
	}
	
	public static void main(String[] args) throws Exception {
		Connection con = PlataformaUtils.getDESAConnection();
		ConsultarProductosPlantilla p = new ConsultarProductosPlantilla();
		ConsultarProductosPlantilla_Entrada entrada = new ConsultarProductosPlantilla_Entrada();
		entrada.p_IdEmpresa = 1L;
		entrada.p_IdModulo = "ORD";
		entrada.p_IdUsuario = 4L;
		PerfilProductoEntrada ppp = new PerfilProductoEntrada();
		ppp.IdPerfilProductos = "PRODUCTOS ARRIESGADO BCS";
		entrada.p_Perfiles.add(ppp);
		
		ConsultarProductosPlantilla_Salida adf = p.logica(con, entrada);
		System.out.println(adf);
	}
	

}