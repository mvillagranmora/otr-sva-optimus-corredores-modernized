package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.SalidaServicios;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.ordenes.NotificarEstadoOrden.NotificarEstadoOrden_Salida;

public class VencimientoOrdenes implements JavaCall {
	
	private final String SQL_SELECT_ORDENES= loadResourceToString("cl/bcs/ordenes/sql/VencimientoOrdenes_Select.sql");
	private final String SQL_UPDATE_ORDENES= loadResourceToString("cl/bcs/ordenes/sql/VencimientoOrdenes_Update.sql");
	private final String SQL_INSERT_CONTROL_PROCESO = loadResourceToString("cl/bcs/ordenes/sql/VencimientoOrdenes_InsertControlProceso.sql");

	public static class EntradaVencimientoOrdenes{

		public Long p_IdEmpresa;
		public String p_IdModulo;
		public long p_IdUsuario;
		public String p_FechaVencimiento;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaVencimientoOrdenes extends SalidaServicios{
		public Long P_CANTIDAD_ORDENES_VENCIDAS;
		public String P_RESULTADO_CIERRE_DIARIO;
		public Map<String, Long> P_ORDENES_VENCIDAS_POR_TIPO_VIGENCIA;
		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public SalidaVencimientoOrdenes() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			////super();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		
	}

	public static class VencerOrdenes{
		public Long IdOrden;
		public String IdEstado;
		public String IdTipoVigenciaOrden;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class ControlProceso
	{	
		public Long   IdEmpresa;
		public Long   IdUsuario;
		public String FechaSistema; 
		public String ResultadoProceso;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}	
	public SalidaVencimientoOrdenes logica(Connection con, EntradaVencimientoOrdenes in) {
		SalidaVencimientoOrdenes salida = new SalidaVencimientoOrdenes();
		String resultadoProcesoTexto = "OK";
		
		try {
			SQL sql = new SQL();
			//Obtener Ordenes a vencer
			HashMap<String, Object> parmsDirs= new HashMap<String, Object>();
			parmsDirs.put("p_FechaVencimiento", in.p_FechaVencimiento);
			parmsDirs.put("p_IdEmpresa", in.p_IdEmpresa);
			List<VencerOrdenes> ordenesAVencer  = sql.query(con, SQL_SELECT_ORDENES, parmsDirs, VencerOrdenes.class);
			
			//actualizar ordenes
			int[] res = sql.batch(con, SQL_UPDATE_ORDENES, ordenesAVencer, VencerOrdenes.class);
			salida.P_CANTIDAD_ORDENES_VENCIDAS = new Long(0);
			
			// Se inicializa la lista de Ordenes Vencidas por Tipo Vigencia
			salida.P_ORDENES_VENCIDAS_POR_TIPO_VIGENCIA = new HashMap<String, Long>();
			String[] IdsTiposVigenciaOrden = {"FF", "OP", "SH", "SE"};
			for (String tipoVigencia : IdsTiposVigenciaOrden) {
				salida.P_ORDENES_VENCIDAS_POR_TIPO_VIGENCIA.put(tipoVigencia, 0L);
			}
			
			for (int i = 0; i < res.length; i++) {
				if(res[i]>0) {
					salida.P_CANTIDAD_ORDENES_VENCIDAS++;
					
					String tipoVigenciaActual = ordenesAVencer.get(i).IdTipoVigenciaOrden;
					if(tipoVigenciaActual != null) {
						Long valorActual = salida.P_ORDENES_VENCIDAS_POR_TIPO_VIGENCIA.get(tipoVigenciaActual);
						salida.P_ORDENES_VENCIDAS_POR_TIPO_VIGENCIA.put(tipoVigenciaActual, valorActual+1);
					}
				}
			}
			
			if(salida.P_CANTIDAD_ORDENES_VENCIDAS>0){
				NotificarEstadoOrden notificar = new NotificarEstadoOrden();
				NotificarEstadoOrden.NotificarEstadoOrden_Entrada entradaNotificaar = new NotificarEstadoOrden.NotificarEstadoOrden_Entrada();
				entradaNotificaar.p_IdEmpresa = in.p_IdEmpresa;
				entradaNotificaar.p_IdUsuario = in.p_IdUsuario;
				entradaNotificaar.p_IdModulo = in.p_IdModulo;
				List<NotificarEstadoOrden.Orden> ordenes = new ArrayList<NotificarEstadoOrden.Orden>();
				for (VencerOrdenes orden : ordenesAVencer) {
					ordenes.add(new NotificarEstadoOrden.Orden(orden.IdOrden));
				}
				entradaNotificaar.p_Ordenes = ordenes;
				
				
				
				
				NotificarEstadoOrden_Salida  salidaNotificar  = notificar.logica(con, entradaNotificaar);
				
				if (salidaNotificar.P_OUT_MENSAJES.size() > 0)
				{	
					salida.P_OUT_MENSAJES = salidaNotificar.P_OUT_MENSAJES;
				}
				
				for(String clave : salida.P_ORDENES_VENCIDAS_POR_TIPO_VIGENCIA.keySet()) {
					HashMap<String, String> params = new HashMap<>(2);
					params.put("OrdenesVencidas", salida.P_ORDENES_VENCIDAS_POR_TIPO_VIGENCIA.get(clave).toString());
					
					if(clave == "SH")
						params.put("TipoVigenciaOrden", "SOLO HOY");
					else if (clave == "OP")
						params.put("TipoVigenciaOrden", "PERMANENTE");
					else if (clave == "SE")
						params.put("TipoVigenciaOrden", "SEMANAL");
					else if (clave == "FF")
						params.put("TipoVigenciaOrden", "FECHA FIJA");
					
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_ORDENES_VENCIDAS_TIPO_VIGENCIA",params,con,in.p_IdEmpresa));
				}
				
			}
			else {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_NO_SE_VENCIERON_ORDENES",new HashMap<String,String>(),con,in.p_IdEmpresa));
			}
			//Marca el proceso VENCIMIENTO_ORDEN como ejecutado
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			List<ControlProceso> lstControlProceso = new ArrayList<ControlProceso>();			
			ControlProceso controlProceso = new ControlProceso();
			
			controlProceso.FechaSistema  = fechaSistema;
			controlProceso.IdEmpresa     = in.p_IdEmpresa;
			controlProceso.IdUsuario     = in.p_IdUsuario;
			controlProceso.ResultadoProceso = "OK";
			
			lstControlProceso.add(controlProceso);
			
			sql.batch(con, SQL_INSERT_CONTROL_PROCESO, lstControlProceso, ControlProceso.class);			
			salida.P_RESULTADO_CIERRE_DIARIO = "OK+";
		}catch(Exception e) {
			salida.addError("UI_ERROR_VENCER_ORDENES", "UI_ERROR_VENCER_ORDENES");
			salida.P_RESULTADO_CIERRE_DIARIO = "NOK";
			e.printStackTrace();
		}
		
		return salida;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		EntradaVencimientoOrdenes in=gson.fromJson(jin.toString() ,EntradaVencimientoOrdenes.class);
		SalidaVencimientoOrdenes out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	

	public static void main(String[] args){
		
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdUsuario\":69,\"p_IdEmpresa\":1,\"p_IdModulo\":\"ORD\",\"p_FechaVencimiento\":\"2017-07-26 00:00:00.0\"}";
			
			EntradaVencimientoOrdenes Objin = gson.fromJson(json, EntradaVencimientoOrdenes.class);			
			VencimientoOrdenes sp = new VencimientoOrdenes();

			
			System.out.println(sp.logica(con, Objin));
			con.rollback();
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}

}
