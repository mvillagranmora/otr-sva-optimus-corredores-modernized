package cl.bcs.plataforma.acc;
import static cl.bcs.plataforma.pla.utils.PlataformaUtils.loadResourceToString;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.LiquidarMovimientoAutomatico.AdvertenciasDetalle;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearAutorizadoOrden implements JavaCall {
	
	private static final Logger logger 						= LogManager.getLogger(CrearAutorizadoOrden.class);
	private final String SQL_CALL_CREAR_AUTORIZADO_ORDEN 	= loadResourceToString("cl/bcs/plataforma/acc/sql/CrearAutorizadoOrden_CallCrearAutorizadoOrden.sql");
	private final String SQL_VERIFICAR_EVENTO_AUTORIZACION 	= loadResourceToString("cl/bcs/plataforma/acc/sql/CrearAutorizadoOrden_VerificarEventoAutorizacion.sql");
	private final String SQL_UPDATE_ESTADO_CONFIRMADO 		= loadResourceToString("cl/bcs/plataforma/acc/sql/CrearAutorizadoOrden_UpdateEstadoConfirmado.sql");

	
	public static class CrearAutorizadoOrden_Entrada extends BcsPojo {
		
		public 	Long 	p_IdEmpresa;
		public	Long 	p_IdUsuario;
		public 	String 	p_IdModulo;
		public  String 	p_IdEstado; 
		public  String 	p_UsaReglasNegocio;
		public	Long 	p_IdPersona;
		public 	Long 	p_IdPortafolio;
		public  String 	p_IdTipoAutorizado; 
		public  String 	p_FirmaOptimusCliente;
		// PERU
		public  String 	p_NumeroRegistroPublico;
		public  String 	p_Asiento;
		public  String 	p_FichaPartida;
		public  String 	p_TipoFirma;
		
		public CrearAutorizadoOrden_Entrada( ) {
			
			this.p_NumeroRegistroPublico = "";
			this.p_Asiento = "";
			this.p_FichaPartida = "";
			this.p_TipoFirma = "INDIVIDUAL";

		}

	}
	
	public static class CrearAutorizadoOrden_Salida extends BcsPojo {

		public List<MensajeSalida> 	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public CrearAutorizadoOrden_Salida () {	
			
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR 		 = 0L;
			
		}
		
	}
	
	public static class AdvertenciasDetalle extends BcsPojo {
		
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

	}
	
	public static class ErrorDetalle extends BcsPojo {
		
		public String Codigo;
		public String Descripcion;

	}
	
	public static class EventoAutorizacion extends BcsPojo {
		
		public Long CantidadEventos;

	}
	
	public static class UpdateEstadoConfirmado extends BcsPojo {
		
		public Long IdAutorizado;
		public String IdEstado;
		public String IdEstadoAutorizacion;

	}
	
	@SuppressWarnings("unchecked")
	public CrearAutorizadoOrden_Salida logica(Connection conn, CrearAutorizadoOrden_Entrada in) throws Exception {
		
		logger.info("INICIO::CrearAutorizadoOrden");
		
		Long init 							= System.currentTimeMillis();
		CrearAutorizadoOrden_Salida salida 	= new CrearAutorizadoOrden_Salida();
		SQL sql 							= new SQL();

		HashMap<String, Object> saldidaCallCrearAutorizardoOrden = CallCrearAutorizardoOrden(conn, sql, in);
		
		if ( (Long)saldidaCallCrearAutorizardoOrden.get("p_out_ErroresContador") > 0L ) {
			
			logger.debug("SP CREAR AUTORIZACION ORDENES CON ERRORES");
			
			salida.P_OUT_ERRORESCONTADOR 	= (Long)saldidaCallCrearAutorizardoOrden.get("p_out_ErroresContador");
			List<ErrorDetalle> errores 		= (List<ErrorDetalle>) saldidaCallCrearAutorizardoOrden.get("p_out_ErroresDetalle");
			
			for( ErrorDetalle err: errores ) {
				
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,err.Codigo,err.Descripcion));
				
			}
			
			return salida;
			
		} else if ( (Long)saldidaCallCrearAutorizardoOrden.get("p_out_AdvertenciasContador") > 0L ) {
			
			logger.debug("SP CREAR AUTORIZACION ORDENES CON ADVERTENCIAS");
			
			List<AdvertenciasDetalle> advertencias = (List<AdvertenciasDetalle>) saldidaCallCrearAutorizardoOrden.get("p_out_AdvertenciasDetalle");
			
			boolean requiereAutorizacion = false;
			
			for( AdvertenciasDetalle advertencia: advertencias ) {
				
				// Verificar si la advertencia indica que requiere autorización
				if ("REQUIERE_AUTORIZACION".equals(advertencia.Codigo)) {
					requiereAutorizacion = true;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,advertencia.Codigo,advertencia.Descripcion));
				} else {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,advertencia.Codigo,advertencia.Descripcion));
				}
				
			}
			
		} else {
			// Sin errores ni advertencias → Verificar si se creó evento de autorización
			Long idAutorizado = (Long)saldidaCallCrearAutorizardoOrden.get("p_out_RefId");
			
			if (idAutorizado != null && idAutorizado > 0) {
				// Consultar si existe evento NAUTOR para este autorizado
				HashMap<String, Object> paramsEvento = new HashMap<>();
				paramsEvento.put("IdAutorizado", String.valueOf(idAutorizado));
				paramsEvento.put("IdEmpresa", in.p_IdEmpresa);
				
				List<EventoAutorizacion> eventos = sql.query(conn, SQL_VERIFICAR_EVENTO_AUTORIZACION, paramsEvento, EventoAutorizacion.class);
				
				if (!eventos.isEmpty() && eventos.get(0).CantidadEventos > 0) {
					// Se creó evento de autorización
					logger.debug("Evento de autorización NAUTOR creado para IdAutorizado: {}", idAutorizado);
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),conn,in.p_IdEmpresa));
				} else {
					// No se creó evento → Confirmado directo → Actualizar IdEstado de PEN a ACT
					logger.debug("Autorizado creado sin evento de autorización: {} - Actualizando IdEstado a ACT", idAutorizado);
					
					// Crear objeto para UPDATE
					UpdateEstadoConfirmado updateEstado = new UpdateEstadoConfirmado();
					updateEstado.IdAutorizado = idAutorizado;
					updateEstado.IdEstado = "ACT";
					updateEstado.IdEstadoAutorizacion = "ING";
					
					sql.batch(conn, SQL_UPDATE_ESTADO_CONFIRMADO, java.util.Arrays.asList(updateEstado), UpdateEstadoConfirmado.class);
				}
			}
		}
	
		logger.info("FIN::CrearAutorizadoOrden, " + (System.currentTimeMillis() - init));
		return salida;		
	}
	
	private HashMap<String, Object> CallCrearAutorizardoOrden(Connection con, SQL sql, CrearAutorizadoOrden_Entrada in) throws SQLException {
		
		logger.info("INICIO::CrearAutorizadoOrden.CallCrearAutorizardoOrden");
		
		Long init 									= System.currentTimeMillis();
		HashMap<String, Object> parametrosEntrada 	= new HashMap<String, Object>();
		
		parametrosEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
		parametrosEntrada.put("p_IdUsuario", in.p_IdUsuario);
		parametrosEntrada.put("p_IdModulo", in.p_IdModulo);
		parametrosEntrada.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
		parametrosEntrada.put("p_IdEstado", in.p_IdEstado);
		parametrosEntrada.put("p_IdPersona", in.p_IdPersona);
		parametrosEntrada.put("p_IdPortafolio", in.p_IdPortafolio);
		parametrosEntrada.put("p_IdTipoAutorizado", in.p_IdTipoAutorizado);
		parametrosEntrada.put("p_FirmaOptimusCliente", in.p_FirmaOptimusCliente);
		parametrosEntrada.put("p_NumeroRegistroPublico", in.p_NumeroRegistroPublico);
		parametrosEntrada.put("p_Asiento", in.p_Asiento);
		parametrosEntrada.put("p_FichaPartida", in.p_FichaPartida);
		parametrosEntrada.put("p_TipoFirma", in.p_TipoFirma);


		HashMap<String, Class> parametrosSalida = new HashMap<String, Class>();
		parametrosSalida.put("p_out_RefId", Long.class);
		parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
		parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class);
	    parametrosSalida.put("p_out_ErroresContador", Long.class);
	    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
	    
	    
		HashMap<String, Object> respuestaCallCrearAutorizardoOrden;
		try {
			
			respuestaCallCrearAutorizardoOrden = sql.call(con, SQL_CALL_CREAR_AUTORIZADO_ORDEN, parametrosEntrada, parametrosSalida);
	    	
		} catch(Exception e) {
			
	    	throw e;
	    	
	    } finally {
	    	
	    	
	    }
		
		logger.info("FIN::CrearAutorizadoOrden.CallCrearAutorizardoOrden, " + (System.currentTimeMillis() - init));
		
    	return respuestaCallCrearAutorizardoOrden;
	    
	}
	
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearAutorizadoOrden_Entrada in = gson.fromJson(jin.toString(), CrearAutorizadoOrden_Entrada.class);
		CrearAutorizadoOrden_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args)
	{
		System.out.println("== INICIO PRUEBA LOCAL CrearAutorizadoOrden ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties"))
		{
			try {
				SQL.exec(con, "set schema BSCL");
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_IdEstado\": \"ACT\",\"p_UsaReglasNegocio\": \"S\",\"p_IdPersona\":45982,\"p_IdPortafolio\":315756,\"p_IdTipoAutorizado\":\"ICLIENTE-OPERAR\",\"p_FirmaOptimusCliente\":\"S\"}";
		
				CrearAutorizadoOrden_Entrada Objin = gson.fromJson(json, CrearAutorizadoOrden_Entrada.class);

				CrearAutorizadoOrden demo = new CrearAutorizadoOrden();
				System.out.println("ENTRADA");
				System.out.println(Objin.toString());
				
				CrearAutorizadoOrden_Salida ObjOut = demo.logica(con, Objin);
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
				
				con.rollback();
				
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}