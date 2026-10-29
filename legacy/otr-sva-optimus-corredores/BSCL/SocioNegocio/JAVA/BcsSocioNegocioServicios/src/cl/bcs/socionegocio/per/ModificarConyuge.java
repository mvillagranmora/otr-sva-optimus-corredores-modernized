package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.EnrolamientoUtils;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.per.CrearPersona.EntradaServicioCrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.SalidaServicioCrearPersona;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarConyuge implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarConyuge.class);
	
	public String SQL_SELECT_CONYUGE 			= loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarConyuge_SelectConyuge.sql");
	public String SQL_SELECT_VALIDA_CONYUGE 	= loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarConyuge_SelectValidaConyuge.sql");
	public String SQL_UPDATE_CONYUGE 			= loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarConyuge_UpdateConyuge.sql");
	
	public static class EntradaModificarConyuge extends BcsPojo{
		
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public EntradaConyuge p_Conyuges;
		
		public EntradaModificarConyuge() {
			this.p_Conyuges = new EntradaConyuge();
		}
		
	}
	
	public static class EntradaConyuge extends BcsPojo{
		
		public String Accion;
		public Long IdPersona;
		public Long IdPersonaConyuge;
		public String EstadoCivil;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String IdEstado;
	}
		
	public static class Conyuge extends BcsPojo{
		
		public Long IdPersona;
		public Long IdPersonaConyuge;
		public String DocumentoIdentidadPersona;
		public String DocumentoIdentidadConyuge;
		
	}
	
	public class UpdateConyuge extends BcsPojo {
		
		public Long IdPersona;
		public Long IdPersonaConyuge;
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarConyuge jin = gson.fromJson(in.toString(), EntradaModificarConyuge.class);
		SalidaWBS out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaWBS logica(Connection con, EntradaModificarConyuge entrada) throws Exception {
		
		logger.info("INICIO::ModificarConyuge");
		
		long inicioServicio 			= System.currentTimeMillis();
		SalidaWBS salida 				= new SalidaWBS();
		EntidadManipulada entidadCreada = new EntidadManipulada();
	    entidadCreada.IdEntidad 		= entrada.p_Conyuges.IdPersonaConyuge;
	    entidadCreada.TipoEntidad 		= "CONYUGE";
	    
		if ( "CA".equalsIgnoreCase(entrada.p_Conyuges.EstadoCivil) ||  "CC".equalsIgnoreCase(entrada.p_Conyuges.EstadoCivil) ) {
			
			List<Conyuge> conyuge = obtenerConyuge(con, entrada.p_Conyuges);
			
			if ( conyuge.size() > 0 ) {
				
				entidadCreada.Mensajes.addAll(validarConyuge(con, entrada, conyuge.get(0)));
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(entidadCreada.Mensajes);
				if (salida.P_OUT_ERRORESCONTADOR > 0)
					return salida;
			}
			if ("MODIFICAR".equalsIgnoreCase( entrada.p_Conyuges.Accion ) ) {
				modificarConyuge(con, entrada.p_Conyuges);
				if (entrada.p_Conyuges.IdPersonaConyuge != null && entrada.p_Conyuges.IdPersonaConyuge == 0) { 
					// desde WS solo llega aca con idpersonaconyuge = 0 pero puede que desde otro servicio no y eso podria generar errores
					entidadCreada.Mensajes.addAll( agregarConyuge(con, entrada));
				}				
			}
			else if(  "BORRAR".equalsIgnoreCase( entrada.p_Conyuges.Accion ) ) {
				modificarConyuge(con, entrada.p_Conyuges);
			}	
			else if( "AGREGAR".equalsIgnoreCase( entrada.p_Conyuges.Accion ) ) {
				entidadCreada.Mensajes.addAll( agregarConyuge(con, entrada));
			}
			
		} else {
			
			EntradaConyuge modificarConyuge = new EntradaConyuge();
			modificarConyuge.Accion 		= "BORRAR";
			modificarConyuge.IdPersona 		= entrada.p_Conyuges.IdPersona;
			modificarConyuge(con, modificarConyuge);
			
		}
		
		if ( MensajeSalida.getNumeroErrores(entidadCreada.Mensajes) > 0 ) {
			
			salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(entidadCreada.Mensajes);
			
		}		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarConyuge", finServicio - inicioServicio);
		return salida;
		
	}
	
	private List<MensajeSalida> modificarConyuge(Connection con, EntradaConyuge entradaConyuges) throws Exception {
		
		logger.debug("INICIO::ModificarConyuge.modificarConyuge");
		long inicio 						= System.currentTimeMillis();
		List<MensajeSalida> validaciones 	= new ArrayList<MensajeSalida>();
		SQL sql 							= new SQL();
		
		List<EntradaConyuge> params 		=  new ArrayList<EntradaConyuge>();
		
		EntradaConyuge modificarConyuge 	= new EntradaConyuge();
		modificarConyuge.IdPersona 			= entradaConyuges.IdPersona;
		modificarConyuge.IdPersonaConyuge 	= ("BORRAR".equalsIgnoreCase(entradaConyuges.Accion)) ? 0L : entradaConyuges.IdPersonaConyuge;
		params.add(modificarConyuge);
		
		sql.batch(con, SQL_UPDATE_CONYUGE, params, EntradaConyuge.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarConyuge.modificarConyuge", fin - inicio);
		return validaciones;
	}
	
	private List<MensajeSalida> agregarConyuge(Connection con, EntradaModificarConyuge entrada) throws Exception {
		
		logger.debug("INICIO::ModificarConyuge.modificarConyuge");
		long inicio 						= System.currentTimeMillis();
		List<MensajeSalida> salida 			= new ArrayList<MensajeSalida>();
		SQL sql 							= new SQL();
		
		
		List<MensajeSalida> salidaValidaConyuge = this.validaConyugeEnPersona(con, entrada);
		salida.addAll(salidaValidaConyuge);
		
		if ( MensajeSalida.getNumeroErrores(salidaValidaConyuge) > 0 )
			return salida;
		
		Enrolamiento persona = crearEntidadEnrolamientoPersona(entrada);		
		EntradaServicioCrearPersona entradaServicioCrearPersona = new EntradaServicioCrearPersona();
		entradaServicioCrearPersona.p_IdEmpresa 				= entrada.p_IdEmpresa;
		entradaServicioCrearPersona.p_IdUsuario 				= entrada.p_IdUsuario;
		entradaServicioCrearPersona.p_IdModulo 					= "SOC";
		entradaServicioCrearPersona.p_UsaReglasNegocio 			= "S";
		entradaServicioCrearPersona.p_ConsultarEnrolamiento 	= Arrays.asList(persona);
		
		CrearPersona crearPersona = new CrearPersona();
		SalidaServicioCrearPersona salidaServicioCrearPersona = crearPersona.logica(con, entradaServicioCrearPersona); 
		salida.addAll( EnrolamientoUtils.obtenerMensajesSalidaAdvertencia(salidaServicioCrearPersona.P_OUT_ADVERTENCIASDETALLE, 0L) );
		salida.addAll( EnrolamientoUtils.obtenerMensajesSalidaError(salidaServicioCrearPersona.P_OUT_ERRORESDETALLE, 0L) );
		
		Long IdPersona = salidaServicioCrearPersona.P_OUT_REFID;
		
		if( IdPersona > 0 || salidaServicioCrearPersona.P_OUT_ERRORESCONTADOR == 0 ) {
			
			List<EntradaConyuge> params 		=  new ArrayList<EntradaConyuge>();
			EntradaConyuge modificarConyuge 	= new EntradaConyuge();
			modificarConyuge.IdPersona 			= entrada.p_Conyuges.IdPersona;
			modificarConyuge.IdPersonaConyuge 	= IdPersona;
			params.add(modificarConyuge);
			sql.batch(con, SQL_UPDATE_CONYUGE, params, EntradaConyuge.class);
			
		}
			
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarConyuge.modificarConyuge", fin - inicio);
		return salida;
	}
	
	private List<MensajeSalida> validaConyugeEnPersona(Connection con, EntradaModificarConyuge entrada) throws Exception {
		
		logger.debug("INICIO::ModificarConyuge.validaConyuge");
		long inicio 						= System.currentTimeMillis();
		List<MensajeSalida> salida 			= new ArrayList<MensajeSalida>();
		SQL sql 							= new SQL();
		
		HashMap<String, Object> params 	= new HashMap<String, Object>(2);
		
		params.put("IdPersona", entrada.p_Conyuges.IdPersona);
		List<Conyuge> conyuge = sql.query(con, SQL_SELECT_VALIDA_CONYUGE, params, Conyuge.class);
		
		if ( conyuge.get(0).IdPersonaConyuge > 0 ) {
			
			salida.add( new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_PERSONA_YA_TIENE_UN_CONYUGE", "La persona ya tiene un conyuge asignado.") );

		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarConyuge.validaConyuge", fin - inicio);
		return salida;
	}

	private Enrolamiento crearEntidadEnrolamientoPersona(EntradaModificarConyuge entrada){
		logger.trace("INICIO::ModificarConyuge.crearEntidadEnrolamientoPersona");
		long inicio = System.currentTimeMillis();
		
		EntradaConyuge datosConyuge = entrada.p_Conyuges;
		Enrolamiento persona 		= new Enrolamiento();
		
		persona.IdEnrolamiento						= 0L;
		persona.IdEmpresa							= entrada.p_IdEmpresa;
		persona.IdUsuario							= entrada.p_IdUsuario;
		persona.IdTipoEntidad						= "PERSONA";
		persona.Tipo								= "N";
		persona.IdTipoDocumentoIdentidadAsociado 	= "";
		persona.Nombres 							= datosConyuge.Nombres;
		persona.ApellidoPaterno	 					= datosConyuge.ApellidoPaterno;
		persona.ApellidoMaterno 					= datosConyuge.ApellidoMaterno;
		persona.IdEntidadEnrolada 					= 0L;
		persona.IdTipoDocumentoIdentidad 			= datosConyuge.TipoDocumentoIdentidad;
		persona.DocumentoIdentidad					= datosConyuge.DocumentoIdentidad;
		persona.RegimenConyugal						= "";
		persona.IdTipoTelefono						= "OTRO";
		persona.EMP_FechaInicioCargo				= java.sql.Date.valueOf("0001-01-01");
		persona.EMP_FechaTerminoCargo				= java.sql.Date.valueOf("0001-01-01");
		persona.PRO_IdAreaProveedor					= 0L;
		persona.EsRutInterno						= "N";
		persona.IdPersonaConyuge					= 0L;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarConyuge.crearEntidadEnrolamientoPersona", fin - inicio);
		return persona;
	}
	
	private List<Conyuge> obtenerConyuge(Connection con, EntradaConyuge entradaConyuges) throws Exception {
		
		logger.debug("INICIO::ModificarConyuge.obtenerConyuges");
		long inicio 					= System.currentTimeMillis();
		SQL sql 						= new SQL();
		HashMap<String, Object> params 	= new HashMap<String, Object>(2);
		
		params.put("IdPersonaConyuge", entradaConyuges.IdPersonaConyuge);
		List<Conyuge> conyuges = sql.query(con, SQL_SELECT_CONYUGE, params, Conyuge.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarConyuge.obtenerConyuges", fin - inicio);
		return conyuges;
		
	}
	
	private List<MensajeSalida> validarConyuge(Connection con, EntradaModificarConyuge entrada, Conyuge conyuge) throws Exception {
		
		logger.debug("INICIO::ModificarConyuge.validarConyuges");
		long inicio 						= System.currentTimeMillis();
		List<MensajeSalida> validaciones 	= new ArrayList<MensajeSalida>();
		
		if( esConyungeExistenteEnOtraPersona(entrada.p_Conyuges, conyuge) ) {
			
			HashMap<String,String> parametros = new HashMap<String,String>(3);
			parametros.put("DocumentoIdentidadConyuge", conyuge.DocumentoIdentidadConyuge);
			parametros.put("DocumentoIdentidadPersona", conyuge.DocumentoIdentidadPersona);
			MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_PERSONA_CONYUGE_YA_EXISTE_EN_OTRA_PERSONA", parametros, con, entrada.p_IdEmpresa);
			validaciones.add(mensajeSalida);
			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarConyuge.validarConyuges", fin - inicio);
		return validaciones;
		
	}
	
	private Boolean esConyungeExistenteEnOtraPersona(EntradaConyuge entradaConyuge, Conyuge conyuge) {
		logger.trace("INICIO::ModificarConyuge.esConyungeExistenteEnOtraPersona");
		long inicio = System.currentTimeMillis();
		
		Boolean esConyungeExistenteEnOtraPersona = conyuge.IdPersonaConyuge.equals(entradaConyuge.IdPersonaConyuge) && !conyuge.IdPersona.equals(entradaConyuge.IdPersona);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarConyuge.esConyungeExistenteEnOtraPersona", fin - inicio);
		return esConyungeExistenteEnOtraPersona;
	}
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			EntradaModificarConyuge entradaValidarConyuge = new EntradaModificarConyuge();
			entradaValidarConyuge.p_IdEmpresa = 1L;
			entradaValidarConyuge.p_IdUsuario = 21L;
			EntradaConyuge entradaConyuge = new EntradaConyuge();
			entradaConyuge.IdPersona = 1773904L;
			entradaConyuge.IdPersonaConyuge = 1775121L;

			entradaValidarConyuge.p_Conyuges = entradaConyuge;
			ModificarConyuge validarConyuge = new ModificarConyuge();
			try {
				SalidaWBS salidaModificarConyuge = validarConyuge.logica(con, entradaValidarConyuge);
				System.out.println(salidaModificarConyuge.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			} finally {
				con.rollback();
			}

    	}
		System.out.println("== END ==");
	}

}
