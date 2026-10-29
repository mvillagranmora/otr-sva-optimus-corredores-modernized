package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.pojo.Asignacion;
import cl.bcs.ordenes.utils.OrdenUtils;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CambiarEstadoAsignaciones implements JavaCall {
	
	private final String SQL_UPDATE_ESTADO_ASIGNACION = loadResourceToString("cl/bcs/ordenes/sql/CambiarEstadoAsignaciones_UpdateEstadoAsignacion.sql");	
	private final String SQL_SELECT_ASIGNACIONES = loadResourceToString("cl/bcs/ordenes/sql/CambiarEstadoAsignaciones_SelectAsignaciones.sql");	
	
	@Trace
	public CambiarEstadoAsignaciones_Salida logica(Connection conn,CambiarEstadoAsignaciones_Entrada entrada) throws Exception {
		SQL _sql = new SQL();
		CambiarEstadoAsignaciones_Salida salida = new CambiarEstadoAsignaciones_Salida();
		
		HashMap<Long,Asignacion> hashAsignaciones = new HashMap<Long,Asignacion>();
		{
			HashMap<String,Object> params = new HashMap<>();
			ArrayList<Long> idsAsignacion = new ArrayList<>();
			for (Asignacion_Entrada asig : entrada.p_Asignaciones) {
				idsAsignacion.add(asig.IdAsignacion);
			}
			params.put("idsAsignacion", idsAsignacion);
			List<Asignacion> listaAsignaciones = _sql.query(conn, SQL_SELECT_ASIGNACIONES, params, Asignacion.class);
			for (Asignacion asig : listaAsignaciones) {
				hashAsignaciones.put(asig.IdAsignacion, asig);
			}
		}
		
		List<Asignacion> asignaciones = new ArrayList<>();
		List<Long> asignacionesConErrores = new ArrayList<>();
		for (Asignacion_Entrada asig : entrada.p_Asignaciones) {
			if (hashAsignaciones.get(asig.IdAsignacion) == null) {
				String codigo = "ASIGNACION_NO_EXISTE";
				String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdAsignacion, MensajeSalida.MENSAJE_ERROR, codigo, desc));
				asignacionesConErrores.add(asig.IdAsignacion);
			}
			
			if (hashAsignaciones.get(asig.IdAsignacion) != null && 
					hashAsignaciones.get(asig.IdAsignacion).IdEstadoAsignacion.equalsIgnoreCase("ANU") &&
					(asig.IdEstadoAsignacion.equalsIgnoreCase("EAF") || asig.IdEstadoAsignacion.equalsIgnoreCase("NEF") || asig.IdEstadoAsignacion.equalsIgnoreCase("REF") )
					) {
				String codigo = "ASIGNACION_ESTADO_ANULADO";
				String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdAsignacion, MensajeSalida.MENSAJE_ERROR, codigo, desc));
				asignacionesConErrores.add(asig.IdAsignacion);
			}
			
			else if (hashAsignaciones.get(asig.IdAsignacion) != null && 
					hashAsignaciones.get(asig.IdAsignacion).IdEstadoAsignacion.equalsIgnoreCase("EAF") &&
					asig.IdEstadoAsignacion.equalsIgnoreCase("ANU") 
				) {
				String codigo = "ASIGNACION_ENVIADA_FACTURAR";
				String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdAsignacion, MensajeSalida.MENSAJE_ERROR, codigo, desc));
				asignacionesConErrores.add(asig.IdAsignacion);
			}
			
				
			if (asignacionesConErrores.contains(asig))
				continue;
			Asignacion asigPojo = new Asignacion();
			Utils.copyPojo(asig, asigPojo);
			asignaciones.add(asigPojo);
			
		}
		
		if (asignaciones.size() > 0) {
			_sql.batch(conn, SQL_UPDATE_ESTADO_ASIGNACION, asignaciones, Asignacion.class);
			for (Asignacion asig : asignaciones) {
				String codigo = "CAMBIO_DE_ESTADO_REALIZADO";
				String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdAsignacion,MensajeSalida.MENSAJE_OK,codigo,desc));
			}
		}
		
		return salida;
	}

	public static class Asignacion_Entrada {
		public Long IdAsignacion;
		public String IdEstadoAsignacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class CambiarEstadoAsignaciones_Entrada {
		public List<Asignacion_Entrada> p_Asignaciones;
		public Long p_IdEmpresa;
		
		public CambiarEstadoAsignaciones_Entrada() {
			p_Asignaciones = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//SALIDAS
	public static class CambiarEstadoAsignaciones_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public CambiarEstadoAsignaciones_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static void main(String[]args) throws Exception{
		try (Connection con = PlataformaUtils.getDESAConnection()){
			
		}	
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		throw new Exception("call no implementado");
	}
}
	