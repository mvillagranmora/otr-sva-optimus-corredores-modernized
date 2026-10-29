package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarInformacionEnvioFacturacionMasivo implements JavaCall {

	private final Log log = new Log("ORD.ConsultarInformacionEnvioFacturacionMasivo");

	private final String SQL_SELECT_ASIGNACIONES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarInformacionEnvioFacturacionMasivo_SelectAsignaciones.sql");
	private final String SQL_SELECT_ASIGNACIONES_ID_ASIGNACION_MASIVA = loadResourceToString("cl/bcs/ordenes/sql/ConsultarInformacionEnvioFacturacionMasivo_SelectAsignacionesPorIdAsignacionMasiva.sql");

	public static class ConsultarInformacionEnvioFacturacionMasivo_Entrada extends BcsPojo {

		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		
		public String 	p_HoraHasta;
		public String 	p_IdClasificacionTipoInstrumento;
		public String 	p_IdTipoInstrumento;
		public Long 	p_IdLibroOrdenes;
		public String 	p_IdAccionOrden;
		public Long		p_IdInstrumento;
		public String 	p_IdCondicionDeLiquidacion;
		public Long		p_IdAsignacion;
		public String	p_FolioTransaccion;
		public Long		p_IdOrden;
		public String 	p_IdBolsa;
		public Long 	p_IdAsignacionMasiva;
		public String   p_MarcaAsignacion;
		public String   p_IdTipoCanalIngreso;
	}
	
	public static class Asignacion_Salida extends BcsPojo {
		public Long 				IdTransaccion;
		public Long 				IdOrden;
		public Long 				IdAsignacion;
		public String 				FolioTransaccion;
		public BigDecimal 			CantidadAsignada;
		public BigDecimal 			ValorRescateAsignada;
		public String 				IdClasificacionTipoInstrumento;
		public String 				IdTipoInstrumento;
		public String 				AccionOrden;
		public String 				IdBolsa;
		public String 				IdMonedaInstrumento;
		public String 				IdMonedaOperacion;
		public String 				NombreLibroOrdenes;
		public String 				Instrumento;
		public String 				EsAPlazo;
		public String 				EsAPlazoUI;
		public Long				    IdAsignacionMasiva;
	}
		
	public static class ConsultarInformacionEnvioFacturacionMasivo_Salida extends BcsPojo {
		
		public List<Asignacion_Salida>	P_OUT_ASIGNACIONES;
		public List<MensajeSalida>		P_OUT_MENSAJES;
		
		public ConsultarInformacionEnvioFacturacionMasivo_Salida() {
			P_OUT_ASIGNACIONES = new ArrayList<>();
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarInformacionEnvioFacturacionMasivo_Entrada in = gson.fromJson(jin.toString(), ConsultarInformacionEnvioFacturacionMasivo_Entrada.class);
		ConsultarInformacionEnvioFacturacionMasivo_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public ConsultarInformacionEnvioFacturacionMasivo_Salida logica(Connection con, ConsultarInformacionEnvioFacturacionMasivo_Entrada in) throws Exception {
		ConsultarInformacionEnvioFacturacionMasivo_Salida out = new ConsultarInformacionEnvioFacturacionMasivo_Salida();
		SQL _sql = new SQL();

		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if (in.p_IdAsignacionMasiva == null && in.p_HoraHasta == null) { 
			String codigo = "UI_FALTA_HORA_HASTA";
			String desc = Diccionario.getMensaje(con, in.p_IdEmpresa, codigo).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return out;
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		Long localOffset = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
		
		Fecha.setLocalOffSet(localOffset.intValue());
		
		java.sql.Timestamp date1 = Fecha.toDateTime(fechaSistema + " 00:00:00");
		String fechaDesde = Fecha.toStringDatetime(Fecha.toUTCTimeStamp(date1));
		
		
		java.sql.Timestamp date2 = Fecha.toDateTime(fechaSistema + " " + in.p_HoraHasta);
		String fechaHasta = Fecha.toStringDatetime(Fecha.toUTCTimeStamp(date2));
		
		HashMap<String,Object> params = new HashMap<>(14);
		
		if(in.p_IdAsignacionMasiva != null) {
			params.put("p_IdAsignacionMasiva", in.p_IdAsignacionMasiva);

			out.P_OUT_ASIGNACIONES = _sql.query(con, SQL_SELECT_ASIGNACIONES_ID_ASIGNACION_MASIVA, params, Asignacion_Salida.class);
			return out;
		}

		params.put("v_FechaAsignacionDesde", fechaDesde);
		params.put("v_FechaAsignacionHasta", fechaHasta);
		params.put("p_IdLibroOrdenes", in.p_IdLibroOrdenes);
		params.put("p_IdClasificacionTipoInstrumento", in.p_IdClasificacionTipoInstrumento);
		params.put("p_IdTipoInstrumento", in.p_IdTipoInstrumento);
		params.put("p_IdAccionOrden", in.p_IdAccionOrden);
		params.put("p_IdCondicionDeLiquidacion", in.p_IdCondicionDeLiquidacion);
		params.put("p_IdBolsa", in.p_IdBolsa);
		params.put("p_IdInstrumento", in.p_IdInstrumento);
		params.put("p_IdAsignacion", in.p_IdAsignacion);
		params.put("p_IdOrden", in.p_IdOrden);
		params.put("p_IdBolsa", in.p_IdBolsa);
		params.put("p_FolioTransaccion", in.p_FolioTransaccion);
		params.put("p_IdTipoCanalIngreso", in.p_IdTipoCanalIngreso);
		params.put("p_MarcaAsignacion", in.p_MarcaAsignacion);
		
		out.P_OUT_ASIGNACIONES = _sql.query(con, SQL_SELECT_ASIGNACIONES, params, Asignacion_Salida.class);		
		return out;

	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2","C:\\Users\\lportales_ext\\OPTIMUS\\workspace\\optimus-corredores-dev-workspace\\OptimusDevJava\\application.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9677,\"p_IdModulo\":\"ORD\",\"p_HoraHasta\":\"14:31:52\",\"p_MarcaAsignacion\":\"TEST\"}";
			ConsultarInformacionEnvioFacturacionMasivo_Entrada adf = gson.fromJson(json, ConsultarInformacionEnvioFacturacionMasivo_Entrada.class);
			ConsultarInformacionEnvioFacturacionMasivo demo=new ConsultarInformacionEnvioFacturacionMasivo();
			ConsultarInformacionEnvioFacturacionMasivo_Salida dfasflj = demo.logica(con, adf);
			System.out.println(dfasflj.toString());
 			//con.commit();
 			
	    	}
		System.out.println("== END ==");
	}
}
