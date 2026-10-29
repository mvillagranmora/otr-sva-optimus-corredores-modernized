package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesSalida;
import cl.bcs.ordenes.RutearOrdenes.SalidaResumen;
import cl.bcs.ordenes.pojo.FormaDePagoOrden;
import cl.bcs.ordenes.pojo.Orden;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarInformacionAsignacionPropCrono implements JavaCall {

	private final Log log = new Log("ORD.ConsultarInformacionAsignacionPropCrono");

	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarInformacionAsignacionPropCrono_SelectTransacciones.sql");
	private final String SQL_SELECT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarInformacionAsignacionPropCrono_SelectOrdenes.sql");
	
	public static class ConsultarInformacionAsignacionPropCrono_Entrada {

		public Long 	p_IdEmpresa;
		public String 	p_HoraHasta;
		public Long 	p_IdLibroOrdenes;
		public String 	p_IdClasificacionTipoInstrumento;
		public String 	p_IdCondicionDeLiquidacion;
		public String 	p_IdTipoInstrumento;
		public String 	p_IdAccionOrden;
		public String 	p_IdBolsa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_CanalIngreso;
		public String	p_MarcaAsignacion;
		
		public Long 	p_IdInstrumento;
		public String 	p_IdMonedaNegociacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Transaccion_Salida {
		public Long 				IdTransaccion;
		public BigDecimal 			CantidadDisponibleTransaccion;
		public BigDecimal 			ValorRescateDisponibleTransaccion;
		public BigDecimal			PrecioTransaccion;
		public java.sql.Timestamp 	FechaOperacionTransaccion;
		public Long 				IdLibroOrdenes;
		public String 				IdClasificacionTipoInstrumento;
		public String 				IdCondicionDeLiquidacion;
		public String 				IdTipoInstrumento;
		public String 				AccionOrden;
		public String 				IdBolsa;
		public String 				FolioTransaccion;
		public String 				IdMonedaInstrumento;
		public String 				NombreLibroOrdenes;
		public String 				Instrumento;
		public String				IdTipoReajuste;
		public String 				Reservado;
		public Long 				IdUsuarioReserva;
		public Long					IdInstrumento;
	}
	
	public static class Orden_Salida {
		public Long 				IdOrden;
		public java.sql.Timestamp 	FechaOperacionOrden;
		public Long 				IdLibroOrdenes;
		public String 				IdClasificacionTipoInstrumento;
		public String 				IdCondicionDeLiquidacion;
		public String 				IdTipoInstrumento;
		public String 				AccionOrden;
		public String 				IdBolsa;
		public String 				TipoPrecio;
		public BigDecimal 			CantidadDisponibleOrden;
		public BigDecimal 			PrecioOrden;
		public String 				NombreLibroOrdenes;
		public String 				IdMonedaOrden;
		public String 				Instrumento;
		public Long					IdInstrumento;
	}
	
	public static class ConsultarInformacionAsignacionPropCrono_Salida {
		
		public List<Transaccion_Salida> P_OUT_TRANSACCIONES;
		public List<Orden_Salida> 		P_OUT_ORDENES;
		public List<MensajeSalida>		P_OUT_MENSAJES;
		
		public ConsultarInformacionAsignacionPropCrono_Salida() {
			P_OUT_TRANSACCIONES = new ArrayList<>();
			P_OUT_ORDENES = new ArrayList<>();
			P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarInformacionAsignacionPropCrono_Entrada in = gson.fromJson(jin.toString(), ConsultarInformacionAsignacionPropCrono_Entrada.class);
		ConsultarInformacionAsignacionPropCrono_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public ConsultarInformacionAsignacionPropCrono_Salida logica(Connection con, ConsultarInformacionAsignacionPropCrono_Entrada in) throws Exception {
		ConsultarInformacionAsignacionPropCrono_Salida out = new ConsultarInformacionAsignacionPropCrono_Salida();
		SQL _sql = new SQL();

		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if (in.p_HoraHasta == null) {
			String codigo = "";
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
		
		HashMap<String,Object> params = new HashMap<>();
		
		params.put("v_FechaOperacionHasta", fechaHasta);
		params.put("p_IdLibroOrdenes", in.p_IdLibroOrdenes);
		params.put("p_IdClasificacionTipoInstrumento", in.p_IdClasificacionTipoInstrumento);
		params.put("p_IdTipoInstrumento", in.p_IdTipoInstrumento);
		params.put("p_IdAccionOrden", in.p_IdAccionOrden);
		params.put("p_IdCondicionDeLiquidacion", in.p_IdCondicionDeLiquidacion);
		params.put("p_IdBolsa", in.p_IdBolsa);
		params.put("p_IdEmpresa", in.p_IdEmpresa);
		params.put("p_IdTipoCanalIngreso", in.p_CanalIngreso);
		params.put("p_IdInstrumento", in.p_IdInstrumento);
		params.put("p_IdMonedaNegociacion", in.p_IdMonedaNegociacion);
		
		out.P_OUT_ORDENES = _sql.query(con, SQL_SELECT_ORDENES, params, Orden_Salida.class);
		
		params.remove("p_IdTipoCanalIngreso");
		
		
		
		params.put("v_FechaOperacionDesde", fechaDesde);
		List<Transaccion_Salida> transacciones = _sql.query(con, SQL_SELECT_TRANSACCIONES, params, Transaccion_Salida.class);
		for (Transaccion_Salida tran : transacciones) {
			if (tran.Reservado.equalsIgnoreCase("S") && tran.IdUsuarioReserva!=in.p_IdUsuario) {
				//no lo agrego porque esta reservado por otro usuario
			}
			else {
				out.P_OUT_TRANSACCIONES.add(tran);
			}
		}
		
		return out;

	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPRESURAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":208,\"p_IdModulo\":\"ORD\",\"p_HoraHasta\":\"20:59:44\"}";
			ConsultarInformacionAsignacionPropCrono_Entrada adf = gson.fromJson(json, ConsultarInformacionAsignacionPropCrono_Entrada.class);
			ConsultarInformacionAsignacionPropCrono demo=new ConsultarInformacionAsignacionPropCrono();
			ConsultarInformacionAsignacionPropCrono_Salida dfasflj = demo.logica(con, adf);
			System.out.println(dfasflj.toString());
 			//con.commit();
 			
	    	}
		System.out.println("== END ==");
	}
}
