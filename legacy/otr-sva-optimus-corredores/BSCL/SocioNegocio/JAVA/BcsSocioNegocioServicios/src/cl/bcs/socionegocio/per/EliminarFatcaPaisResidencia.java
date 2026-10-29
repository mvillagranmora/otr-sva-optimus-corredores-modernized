package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarFatcaPaisResidencia implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EliminarFatcaPaisResidencia.class);
	
	private final String SQL_EXISTE_FATCA_PAIS_RESIDENCIA_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarFatcaPaisResidencia_Select_PaisResidencia_Persona.sql");
	private final String SQL_DELETE_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarFatcaPaisResidencia_Delete_FatcaPaisResidencia.sql");
	
	public static class EntradaEliminarFatcaPaisResidencia extends BcsPojo{
		public Long p_IdEmpresa;
		public List<FatcaPaisResidencia> p_FatcaPaisResidencia;
		
		public EntradaEliminarFatcaPaisResidencia() {
			this.p_FatcaPaisResidencia = new ArrayList<FatcaPaisResidencia>();
		}
	}
	
	public static class FatcaPaisResidencia extends BcsPojo{
		public Long IdPersona;
		public Long IdPaisResidencia;
	}
	
	public static class BuscaPaisResidencia extends BcsPojo{
	    public Long	IdPersona;
	}
	
	public static class SalidaEliminarFatcaPaisResidencia extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaEliminarFatcaPaisResidencia() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new  ArrayList<MensajeSalida>();
		}
	}
	
	public static class Validacion extends BcsPojo{
		public Boolean EsError;
		public MensajeSalida MensajeError;
		
		public Validacion() {
			this.EsError = false;
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaEliminarFatcaPaisResidencia jin = gson.fromJson(in.toString(), EntradaEliminarFatcaPaisResidencia.class);
		SalidaEliminarFatcaPaisResidencia out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaEliminarFatcaPaisResidencia logica(Connection con, EntradaEliminarFatcaPaisResidencia entrada) throws Exception {
		logger.info("INICIO::EliminarFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaEliminarFatcaPaisResidencia out = new SalidaEliminarFatcaPaisResidencia();
		String msg = "";
		
		Validacion validacionExistenciaFatcaPaisResidenciaPersona = validarExistenciaFatcaPaisResidenciaPersona(con, entrada);
		out = agregarValidacionSalida(out, validacionExistenciaFatcaPaisResidenciaPersona);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
					
		sql.batch(con, SQL_DELETE_FATCA_PAIS_RESIDENCIA, entrada.p_FatcaPaisResidencia, FatcaPaisResidencia.class);			
		
		msg = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PAIS_RESIDENCIA_ELIMINADO").msg();
		out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO",msg));				
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		long fin = System.currentTimeMillis();
		logger.info("FIN::EliminarFatcaPaisResidencia", fin - inicio);
		return out;
	}
	
	private Validacion validarExistenciaFatcaPaisResidenciaPersona(Connection con, EntradaEliminarFatcaPaisResidencia entrada) throws Exception {
		logger.debug("INICIO::EliminarFatcaPaisResidencia.validarExistenciaFatcaPaisResidenciaPersona");
		long inicio = System.currentTimeMillis();
		Validacion validacion = new Validacion();
		
		FatcaPaisResidencia fatcaPaisResidencia = entrada.p_FatcaPaisResidencia.parallelStream().findFirst().orElse(null);
		
		if(fatcaPaisResidencia != null) {
			HashMap<String,Object> parametros = new HashMap<String,Object>(3);
			parametros.put("IdPersona", fatcaPaisResidencia.IdPersona);
			parametros.put("IdPaisResidencia", fatcaPaisResidencia.IdPaisResidencia);
		
			SQL sql = new SQL();
			Long count = sql.countQuery(con, SQL_EXISTE_FATCA_PAIS_RESIDENCIA_PERSONA, parametros);
		
			if(count > 0) {
				validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_PAIS_RESIDENCIA_ASOCIADO_GENERAL", new HashMap<String,String>(), con, entrada.p_IdEmpresa);
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarFatcaPaisResidencia.validarExistenciaFatcaPaisResidenciaPersona", fin - inicio);
		return validacion;
	}
	
	private SalidaEliminarFatcaPaisResidencia agregarValidacionSalida(SalidaEliminarFatcaPaisResidencia salida, Validacion validacion) {
		logger.trace("INICIO::EliminarFatcaPaisResidencia.agregarValidacionSalida");
		long inicio = System.currentTimeMillis();
		if(validacion.EsError) {
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add(validacion.MensajeError);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EliminarFatcaPaisResidencia.agregarValidacionSalida", fin - inicio);
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\"p_IdEmpresa\":1,\"p_FatcaPaisResidencia\":[{\"IdPersona\":1749606,\"IdPaisResidencia\":44}]}";
			EntradaEliminarFatcaPaisResidencia Objin = gson.fromJson(json, EntradaEliminarFatcaPaisResidencia.class);
			EliminarFatcaPaisResidencia demo=new EliminarFatcaPaisResidencia();				
			try {
				SalidaEliminarFatcaPaisResidencia ObjOut=demo.logica(con, Objin);
				System.out.println(ObjOut.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback(); 
			}
			finally {
				con.rollback(); 
				con.close();
	    	}
			System.out.println("== END ==");
		}
	}
	
}
