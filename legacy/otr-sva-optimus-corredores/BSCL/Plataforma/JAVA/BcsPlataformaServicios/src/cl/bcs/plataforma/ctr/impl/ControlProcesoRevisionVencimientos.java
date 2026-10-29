package cl.bcs.plataforma.ctr.impl;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ctr.ControlProceso;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ControlProcesoRevisionVencimientos implements JavaCall, ControlProceso {
	
	static final Logger logger = LogManager.getLogger(ControlProcesoRevisionVencimientos.class);
	
	private final String SQL_SELECT_REVISION_VENCIMIENTOS = loadResourceToString("cl/bcs/plataforma/ctr/sql/EjecutarControlProceso_SelectRevisionVencimientos.sql");
	
	public static class SalidaRevisionVencimientos {
		public String 	Fecha;
		public String 	FechaInicio;
		public Long 	CantidadTotal;
		public Long 	CantidadEjecutada;		
		public String 	CodigoControlProcesoTipo;
	}
	
	@Override
	public SalidaControlProceso ejecutar(Connection con, EntradaControlProceso entrada) {
		logger.info("=== INICIO REVISION VENCIMIENTOS ===");
		SalidaControlProceso salida = new SalidaControlProceso();
		
		try {
			if (entrada.p_Fecha==null || entrada.p_Fecha.equals("")) {
				entrada.p_Fecha = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.FechaSistema");
			}
			
			SQL sql = new SQL();
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("p_IdEmpresa", entrada.p_IdEmpresa);
			params.put("p_Fecha", entrada.p_Fecha);
			params.put("p_FechaDiaAnterior", entrada.p_FechaAnterior);			
			List<SalidaRevisionVencimientos> revision = sql.query(con, SQL_SELECT_REVISION_VENCIMIENTOS, params, SalidaRevisionVencimientos.class);
			
			for (SalidaRevisionVencimientos salidaRevision: revision) {
				
				SalidaControlProcesoDetalle salidaRevisionDetalle = new SalidaControlProcesoDetalle();
				salidaRevisionDetalle.IdEmpresa = entrada.p_IdEmpresa;
				salidaRevisionDetalle.CodigoControlProcesoTipo = salidaRevision.CodigoControlProcesoTipo;
				salidaRevisionDetalle.FechaSistema = entrada.p_Fecha;
				salidaRevisionDetalle.FechaInicio = salidaRevision.FechaInicio;
				salidaRevisionDetalle.CantidadTotal = salidaRevision.CantidadTotal;
				salidaRevisionDetalle.CantidadEjecutada = salidaRevision.CantidadEjecutada;
				
				if (salidaRevision.CantidadTotal==0) {
					salidaRevisionDetalle.IdEstado = "ADV";	//No se encontraron registros ::tipo:: para la fecha ::fecha::
					salidaRevisionDetalle.Mensaje  = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PROCESO_REVISION_MENSAJE_ADVERTENCIA")
							 						 .add("fecha", salidaRevision.Fecha)
							 						 .add("tipo", "vencidos").msg();
					
				} else if (salidaRevision.CantidadTotal==salidaRevision.CantidadEjecutada){
					salidaRevisionDetalle.IdEstado = "COM";	//100% de la cartera de ::cantidad:: registro(s) fueron ::tipo::(s) para la fecha ::fecha::
					salidaRevisionDetalle.Mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PROCESO_REVISION_MENSAJE_COMPLETADO")
													.add("cantidad", salidaRevision.CantidadEjecutada)
													.add("fecha", salidaRevision.Fecha)
													.add("tipo", "vencidos").msg();
					
				} else {
					salidaRevisionDetalle.IdEstado = "ERR";	//Cartera de ::cantidadEjecutada:: registro(s) ::tipo::(s) de ::cantidadTotal:: en total para la fecha ::fecha::
					salidaRevisionDetalle.Mensaje  = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PROCESO_REVISION_MENSAJE_ERROR")
													 .add("cantidadEjecutada", salidaRevision.CantidadEjecutada)
													 .add("cantidadTotal", salidaRevision.CantidadTotal)
													 .add("fecha", salidaRevision.Fecha)
													 .add("tipo", "vencidos").msg();
				} 
				salida.P_OUT_RESULTADO.add(salidaRevisionDetalle);
			}
			salida.P_OUT_ERRORESCONTADOR = 0L;
			
		} catch (Exception e) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			logger.info("=== ERROR REVISION VENCIMIENTOS ===");
			System.out.println(e.getMessage());
		} 
		logger.info("=== FIN REVISION VENCIMIENTOS ===");
		
		return salida;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaControlProceso entrada = gson.fromJson(jin.toString(), EntradaControlProceso.class);
		SalidaControlProceso salida = ejecutar(conn, entrada);
		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		Connection conn = null;
		Gson gson = Utils.getGsonBuilder();
		try {
			System.out.println("== INICIO ==");
			conn = PlataformaUtils.getDESAConnection();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"NOR\",\"p_Fecha\":\"\",\"p_FechaAnterior\":\"2020-05-04\"}";
			EntradaControlProceso entrada = gson.fromJson(json, EntradaControlProceso.class);
			ControlProcesoRevisionVencimientos demo = new ControlProcesoRevisionVencimientos();
			SalidaControlProceso ObjOut = demo.ejecutar(conn, entrada);
			conn.commit();
			
			logger.info(ObjOut.toString());
			System.out.println(ObjOut.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== FIN ==");		
	}
}
