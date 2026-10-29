package cl.bcs.plataforma.ctr.impl;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ctr.ControlProceso;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ControlProcesoRevisionCentralizaciones implements JavaCall, ControlProceso {
	
	private final String SQL_SELECT_REVISION_CENTRALIZACIONES = loadResourceToString("cl/bcs/plataforma/ctr/sql/EjecutarControlProceso_SelectRevisionCentralizaciones.sql");
	
	public static class SalidaCentralizaciones{
		public String Estado;
		public String Mensaje;
		public String FechaInicio;
		public Long TotalOperaciones;
		public Long OperacionesConProblemas;
		public String CodigoControlProcesoTipo;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaControlProceso entrada = gson.fromJson(in.toString(), EntradaControlProceso.class);
		SalidaControlProceso salida = ejecutar(conn, entrada);
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
        JsonParser parser     = new JsonParser();
        JsonObject dataout    = parser.parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}
	
	@Override
	public SalidaControlProceso ejecutar(Connection con, EntradaControlProceso entrada){
		SalidaControlProceso out = new SalidaControlProceso();
		try {
			if (entrada.p_Fecha == null || entrada.p_Fecha.equals("")) {
				entrada.p_Fecha = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.FechaSistema");
			}
			
			SQL sql = new SQL();
	        HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("DiaAnterior", entrada.p_FechaAnterior);
			List<SalidaCentralizaciones> centralizaciones = sql.query(con, SQL_SELECT_REVISION_CENTRALIZACIONES, params, SalidaCentralizaciones.class);
			
			for (SalidaCentralizaciones centralizacion : centralizaciones) {
				SalidaControlProcesoDetalle insert = new SalidaControlProcesoDetalle();
				insert.IdEmpresa = entrada.p_IdEmpresa;
				insert.FechaSistema = entrada.p_Fecha;
				insert.FechaInicio = centralizacion.FechaInicio;
				insert.CodigoControlProcesoTipo = centralizacion.CodigoControlProcesoTipo;
				insert.Mensaje = centralizacion.Mensaje;
				insert.IdEstado = centralizacion.Estado;
				insert.CantidadEjecutada = centralizacion.TotalOperaciones - centralizacion.OperacionesConProblemas;
				insert.CantidadTotal = centralizacion.TotalOperaciones;
				out.P_OUT_RESULTADO.add(insert);
			}
			out.P_OUT_ERRORESCONTADOR = 0L;
			
		} catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			e.printStackTrace();
		}

		return out;
	}
	
	public static void main(String[] args) throws Exception {
		Connection conn = null;
		Gson gson = Utils.getGsonBuilder();
		try {
			System.out.println("== INICIO ==");
			conn = PlataformaUtils.getDESAConnection();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"NOR\",\"p_Fecha\":\"2020-05-01\"}";
			EntradaControlProceso entrada = gson.fromJson(json, EntradaControlProceso.class);
			ControlProcesoRevisionCentralizaciones demo = new ControlProcesoRevisionCentralizaciones();
			SalidaControlProceso ObjOut = demo.ejecutar(conn, entrada);
			// conn.commit();

			// logger.info(ObjOut.toString());
			System.out.println(ObjOut.toString());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== FIN ==");
	}
}
