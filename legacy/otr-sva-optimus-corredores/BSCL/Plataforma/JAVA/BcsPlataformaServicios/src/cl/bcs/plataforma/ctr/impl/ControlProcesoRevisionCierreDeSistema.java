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

public class ControlProcesoRevisionCierreDeSistema implements JavaCall, ControlProceso {
	
	private final String SQL_SELECT_REVISION_MODULOS = loadResourceToString("cl/bcs/plataforma/ctr/sql/EjecutarControlProceso_SelectRevisionModulosCerrados.sql");
	
	public static class Modulo{
		public String Estado;
		public String FechaInicio;
		public String NombreModulo;
		public String CodigoControlProcesoTipo;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaControlProceso entrada = gson.fromJson(in.toString(), EntradaControlProceso.class);
		SalidaControlProceso salida = ejecutar(conn, entrada);
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
	    JsonObject dataout = parser.parse(gson.toJson(salida)).getAsJsonObject();
	    return dataout;
	}
	
	public SalidaControlProceso ejecutar(Connection con, EntradaControlProceso entrada) {
		SQL sql = new SQL();
		SalidaControlProceso out = new SalidaControlProceso();
		try {
			if (entrada.p_Fecha==null || entrada.p_Fecha.equals("")) {
				entrada.p_Fecha = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.FechaSistema");
			}
			
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			List<Modulo> modulos = sql.query(con, SQL_SELECT_REVISION_MODULOS, params, Modulo.class);
			
			for(Modulo modulo: modulos) {
				SalidaControlProcesoDetalle insert = new SalidaControlProcesoDetalle();	

				insert.IdEstado = ("CERRADO".equalsIgnoreCase(modulo.Estado))? "COM" : "ERR";
				insert.IdEmpresa = entrada.p_IdEmpresa;
				insert.FechaSistema = entrada.p_Fecha;
				insert.FechaInicio = modulo.FechaInicio;
				insert.CantidadTotal = 1L;
				insert.CantidadEjecutada = (insert.IdEstado.equals("COM"))? 1L : 0L;
				insert.CodigoControlProcesoTipo = modulo.CodigoControlProcesoTipo;
				insert.Mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PROCESO_REVISION_MODULO_ESTADO")
				 		 		 .add("modulo", modulo.NombreModulo.toLowerCase())	// Modulo ::modulo:: ::estado::
				 		 		 .add("estado", modulo.Estado.toLowerCase()).msg();
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
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"NOR\",\"p_Fecha\":\"2020-05-01\"}";
			System.out.println("ENTRADA");
			//System.out.println(Objin.toString());
			
			try {
				System.out.println("SALIDA");
				EntradaControlProceso entrada = gson.fromJson(json, EntradaControlProceso.class);
				ControlProcesoRevisionCierreDeSistema demo = new ControlProcesoRevisionCierreDeSistema();
				SalidaControlProceso resp = demo.ejecutar(con, entrada);
				con.rollback();
				con.commit();
				con.close();
				System.out.println(resp);
			} catch (Exception e) {
				e.printStackTrace();
				con.rollback();
			}
			
			System.out.println("== END ==");
		}
	}
	
}
