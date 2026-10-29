package cl.bcs.plataforma.ctr.impl;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

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

public class ControlProcesoRevisionCierreDeMercado implements JavaCall, ControlProceso {
	
	private final String SQL_ESTADO_MERCADO = loadResourceToString("cl/bcs/plataforma/ctr/sql/EjecutarControlProceso_SelectRevisionEstadoMercado.sql");
	private final String SQL_REVISION_MERCADOS = loadResourceToString("cl/bcs/plataforma/ctr/sql/EjecutarControlProceso_SelectRevisionMercado.sql");
	
	public static class Mercado{
		public String Nombre;
		public String FechaInicio;
		public String CodigoControlProcesoTipo;
	}
	
	public static class Estado{
		public String Estado;
	}
	
	@Override
	public SalidaControlProceso ejecutar(Connection con, EntradaControlProceso entrada) {
		SQL sql = new SQL();
		SalidaControlProceso out = new SalidaControlProceso();
		try {
			if (entrada.p_Fecha==null || entrada.p_Fecha.equals("")) {
				entrada.p_Fecha = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.FechaSistema");
			}

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("Fecha", entrada.p_Fecha);
			List<Estado> estadoMercado = sql.query(con, SQL_ESTADO_MERCADO, params, Estado.class);
			params.clear();
			String estado = "no hay data para la fecha";
			String idEstado = "ERR";
			
			if(!estadoMercado.isEmpty()) {
				if("NEWS: Mercado Cerrado".equalsIgnoreCase(estadoMercado.get(0).Estado)) {
					estado = "cerrado";
					idEstado = "COM";
				}else {
					estado = "abierto";
					idEstado = "ADV";
				}
			}
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			List<Mercado> mercados = sql.query(con, SQL_REVISION_MERCADOS, params, Mercado.class);

			for (Mercado mercado : mercados) {
				SalidaControlProcesoDetalle insert = new SalidaControlProcesoDetalle();
				insert.IdEmpresa = entrada.p_IdEmpresa;
				insert.FechaSistema = entrada.p_Fecha;
				insert.FechaInicio = mercado.FechaInicio;
				insert.IdEstado = idEstado;
				insert.CantidadTotal = 1L;
				insert.CantidadEjecutada = (insert.IdEstado.equals("COM"))? 1L : 0L;
				insert.CodigoControlProcesoTipo = mercado.CodigoControlProcesoTipo;
				insert.Mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PROCESO_REVISION_MERCADO_ESTADO")
		 		 		 		 .add("modulo", mercado.Nombre.toLowerCase())	// Mercado ::modulo:: ::estado::
		 		 		 		 .add("estado", estado).msg();
				out.P_OUT_RESULTADO.add(insert);
			}
			out.P_OUT_ERRORESCONTADOR = 0L;
			
		} catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			e.printStackTrace();
		}
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaControlProceso entrada = gson.fromJson(in.toString(), EntradaControlProceso.class);
		SalidaControlProceso salida = ejecutar(conn, entrada);
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		Connection conn = null;
		Gson gson = Utils.getGsonBuilder();
		
		System.out.println("== INICIO ==");
		conn = PlataformaUtils.getDESAConnection();
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"NOR\",\"p_Fecha\":\"2020-05-01\"}";
		
		EntradaControlProceso entrada = gson.fromJson(json, EntradaControlProceso.class);
		ControlProcesoRevisionCierreDeMercado demo = new ControlProcesoRevisionCierreDeMercado();
		
		SalidaControlProceso ObjOut = demo.ejecutar(conn, entrada);
		conn.commit();
		conn.close();
		//logger.info(ObjOut.toString());
		System.out.println(ObjOut.toString());
	}

}
