package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.EjecutarDesasignacion.EjecutarDesasignacionEntrada;
import cl.bcs.ordenes.EjecutarDesasignacion.EjecutarDesasignacionSalida;
import cl.bcs.ordenes.utils.OrdenUtils;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.AdvertenciaDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.ErrorDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.InformacionDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarDemanda implements JavaCall {
	
	private final String SQL_SELECT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarDemanda_SelectOrdenes.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		ConsultarDemanda_Entrada in=gson.fromJson(jin.toString() ,ConsultarDemanda_Entrada.class);
		ConsultarDemanda_Salida out=logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		for (MensajeSalida msje : out.P_OUT_MENSAJES) {
			msje.descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, msje.codigo, new HashMap());
		}
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class Demanda_Proceso {
		public String AccionOrden;
		public String NombreTipoInstrumento;
		public String Nemotecnico;
		public String IdTipoPrecio;
		public String NombreTipoPrecio;
		public BigDecimal Precio;
		public BigDecimal Cantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ConsultarDemanda_Salida logica(Connection conn,ConsultarDemanda_Entrada entrada) throws Exception {
		SQL _sql = new SQL();
		ConsultarDemanda_Salida salida = new ConsultarDemanda_Salida();
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdInstrumento", entrada.p_IdInstrumento);
		params.put("EsVentaCorta", entrada.p_EsVentaCorta);
		params.put("IdTipoInstrumento", entrada.p_IdTipoInstrumento);
		params.put("FechaConsulta", Fecha.toDate(entrada.p_FechaConsulta));
		
		List<Demanda_Proceso> demanda = _sql.query(conn, SQL_SELECT_ORDENES, params , Demanda_Proceso.class);
		
		for (Demanda_Proceso dma : demanda) {
			
			Demanda_Salida dm = new Demanda_Salida();
			Utils.copyPojo(dma, dm);
			dm.IdMoneda = idMonedaSistema;
			salida.P_OUT_DEMANDA.add(dm);
			
		}
		
		return salida;
	}
	
	
	public static class Demanda_Salida {
		public String NombreTipoInstrumento;
		public String IdMoneda;
		public String Nemotecnico;
		
		public String AccionOrden;
		public BigDecimal Cantidad;
		public String NombreTipoPrecio;
		public BigDecimal Precio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarDemanda_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public String p_FechaConsulta;
		public Long p_IdInstrumento;
		public String p_EsVentaCorta;
		public String p_IdTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//SALIDAS
	public static class ConsultarDemanda_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<Demanda_Salida> P_OUT_DEMANDA;
		
		public ConsultarDemanda_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_DEMANDA = new ArrayList<>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	
	public static void main(String[]args) throws Exception{
		try (Connection con = PlataformaUtils.getDESAConnection()){
			
			ConsultarDemanda pos = new ConsultarDemanda();
			ConsultarDemanda_Entrada ent = new ConsultarDemanda_Entrada();
			ent.p_IdEmpresa = 1L;
			ent.p_IdUsuario = 4L;
			ent.p_IdModulo  = "ORD";
			ent.p_FechaConsulta = "2017-09-08";
			ent.p_IdInstrumento = 502L;
			ent.p_EsVentaCorta = "N";
			ent.p_IdTipoInstrumento = "SIM";
			
			ConsultarDemanda_Salida asdf = pos.logica(con, ent);
			System.out.println(asdf.toString());
			
			

		}	
	}
}
	