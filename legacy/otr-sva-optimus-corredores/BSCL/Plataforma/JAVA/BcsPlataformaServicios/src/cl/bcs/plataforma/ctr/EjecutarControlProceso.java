package cl.bcs.plataforma.ctr;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ctr.ControlProceso.EntradaControlProceso;
import cl.bcs.plataforma.ctr.ControlProceso.SalidaControlProceso;
import cl.bcs.plataforma.ctr.ControlProceso.SalidaControlProcesoDetalle;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EjecutarControlProceso implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EjecutarControlProceso.class);

	private final String SQL_INSERT_CONTROL_PROCESO_DETALLE = loadResourceToString("cl/bcs/plataforma/ctr/sql/EjecutarControlProceso_Insert.sql");
	private final String SQL_SELECT_CONTROL_PROCESO_REVISION = loadResourceToString("cl/bcs/plataforma/ctr/sql/EjecutarControlProceso_SelectRevisiones.sql");
	
	public static class EntradaEjecutarControlProceso {
		public Long				p_IdEmpresa;
		public Long				p_IdUsuario;
		public String			p_IdModulo;
		public String			p_Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaEjecutarControlProceso {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long                 P_OUT_ERRORESCONTADOR;
		public List<String>			P_OUT_RESULTADO;
		
		public SalidaEjecutarControlProceso() {
			P_OUT_RESULTADO = new ArrayList<String>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntradaInsertControlProcesoDetalle {
		public Long 	p_IdControlProcesoDetalle;
		public Long 	p_IdEmpresa;
		public String 	p_CodigoControlProcesoTipo;
		public String   p_FechaSistema;
		public String 	p_FechaInicio;
		public String 	p_FechaTermino;
		public Long 	p_CantidadTotal;
		public Long 	p_CantidadEjecutada;		
		public String 	p_IdEstado;
		public String 	p_Mensaje;
		
		public EntradaInsertControlProcesoDetalle() {
			p_CantidadTotal = 0L;
			p_CantidadEjecutada = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaRevision {
		public String NombreClase;
		public String DescripcionProceso;
	}
	
	SalidaEjecutarControlProceso logica (Connection con, EntradaEjecutarControlProceso entrada) throws Exception {
		logger.info("=== INICIO EJECUTAR REVISIONES ===");
		SalidaEjecutarControlProceso salida = new SalidaEjecutarControlProceso();
		
		try {
			if (entrada.p_Fecha == null || entrada.p_Fecha.equals("")) {
				entrada.p_Fecha = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.FechaSistema");
			}
			String fechaAnterior = obtenerDiaAnteriorHabil(con, entrada.p_IdEmpresa, entrada.p_Fecha);
			
			SQL sql = new SQL();
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("p_IdEmpresa", entrada.p_IdEmpresa);
			List<SalidaRevision> revision = sql.query(con, SQL_SELECT_CONTROL_PROCESO_REVISION, params, SalidaRevision.class);
			
			for (SalidaRevision salidaRevision: revision) {
				try {
					Class<?> clazz = Class.forName(salidaRevision.NombreClase);
					ControlProceso controlProceso = (ControlProceso) clazz.newInstance();

					EntradaControlProceso entradaControlProceso = new EntradaControlProceso();
					entradaControlProceso.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaControlProceso.p_IdUsuario = entrada.p_IdUsuario;
					entradaControlProceso.p_IdModulo = entrada.p_IdModulo;
					entradaControlProceso.p_Fecha = entrada.p_Fecha;
					entradaControlProceso.p_FechaAnterior = fechaAnterior;
					
					SalidaControlProceso salidaControlProceso = controlProceso.ejecutar(con, entradaControlProceso);
					if (salidaControlProceso.P_OUT_RESULTADO.isEmpty())  {
						salida.P_OUT_ERRORESCONTADOR++;
						salida.P_OUT_RESULTADO.add(Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PROCESO_SIN_REGISTROS")
                                			  .add("proceso", salidaRevision.DescripcionProceso).msg());
					} else {
						for(SalidaControlProcesoDetalle insert: salidaControlProceso.P_OUT_RESULTADO) {
							insertControlProcesoDetalle(con, insert);
						}
						salida.P_OUT_RESULTADO.add(Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PROCESO_EJECUTADO_SATISFACTORIAMENTE")
                  			  				  .add("proceso", salidaRevision.DescripcionProceso).msg());
					}

				} catch (Exception e) {
					salida.P_OUT_RESULTADO.add(Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PROCESO_ERROR")
			  				  			  .add("proceso", salidaRevision.DescripcionProceso)
			  				  			  .add("mensajeError", e).msg());
				} 
			}
			
		} catch (Exception e) {
			salida.P_OUT_ERRORESCONTADOR++;
			logger.info(e.getMessage());			
			logger.info("=== ERROR EJECUTAR REVISIONES ===");
		}
		
		logger.info("=== FIN EJECUTAR REVISIONES ===");
		return salida;
	}
	
	public void insertControlProcesoDetalle (Connection con, SalidaControlProcesoDetalle insert) throws SQLException {
		SQL sql = new SQL();
		List<SalidaControlProcesoDetalle> params = new ArrayList<>();
		params.add(insert);
		sql.batch(con, SQL_INSERT_CONTROL_PROCESO_DETALLE, params, SalidaControlProcesoDetalle.class);
	}
	
	private String obtenerDiaAnteriorHabil(Connection con, Long idEmpresa, String fecha) throws Exception {
		ConsultarEmpresa consultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa infoEmpresa = consultarEmpresa.logica(con, idEmpresa);

		InObtenerDiaAnterior.FechasIn fechaDiaAnterior = new InObtenerDiaAnterior.FechasIn();
		fechaDiaAnterior.fechaInicial = fecha;
		fechaDiaAnterior.numeroDias = 1L;
		
		InObtenerDiaAnterior entradaObtenerDiaAnterior = new InObtenerDiaAnterior();
		entradaObtenerDiaAnterior.soloCaeDiaHabil = "S";
		entradaObtenerDiaAnterior.diaHabil = "S";
		entradaObtenerDiaAnterior.idPais = infoEmpresa.IdPais;
		entradaObtenerDiaAnterior.fechas.add(fechaDiaAnterior);
		
		ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
		OutObtenerDiaAnterior salidaObtenerDiaAnterior = obtenerDiaAnterior.logica(con, entradaObtenerDiaAnterior);
		return salidaObtenerDiaAnterior.fechas.get(0).fechaCalculada;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaEjecutarControlProceso entrada = gson.fromJson(jin.toString(), EntradaEjecutarControlProceso.class);
		SalidaEjecutarControlProceso salida = logica(conn, entrada);
		
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
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"NOR\",\"p_Fecha\":\"2020-06-01\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"NOR\",\"p_Fecha\":\"\"}";
			EntradaEjecutarControlProceso entrada = gson.fromJson(json, EntradaEjecutarControlProceso.class);
			EjecutarControlProceso demo = new EjecutarControlProceso();
			SalidaEjecutarControlProceso ObjOut = demo.logica(conn, entrada);
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
