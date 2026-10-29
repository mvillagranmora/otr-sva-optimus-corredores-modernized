package cl.bcs.socionegocio.enr;

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
import cl.bcs.socionegocio.enr.pojo.FatcaPaisResidencia;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarBorradorFatcaPaisResidencia implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EliminarBorradorFatcaPaisResidencia.class);
	
	private final String SQL_EXISTE_FATCA_PAIS_RESIDENCIA_PERSONA = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorFatcaPaisResidencia_SelectPaisResidenciaPersona.sql");
	private final String SQL_DELETE_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorFatcaPaisResidencia_DeleteFatcaPaisResidencia.sql");
	
	public static class EntradaEliminarBorradorFatcaPaisResidencia extends BcsPojo{
		public Long p_IdEmpresa;
		public List<FatcaPaisResidencia> p_FatcaPaisResidencia;
		
		public EntradaEliminarBorradorFatcaPaisResidencia() {
			this.p_FatcaPaisResidencia = new ArrayList<FatcaPaisResidencia>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaEliminarBorradorFatcaPaisResidencia jin = gson.fromJson(in.toString(), EntradaEliminarBorradorFatcaPaisResidencia.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaEliminarBorradorFatcaPaisResidencia entrada) throws Exception {
		logger.info("INICIO::EliminarBorradorFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		String msg = "";
		
		List<Validacion> validacionExistenciaFatcaPaisResidenciaPersona = validarExistenciaFatcaPaisResidenciaPersona(con, entrada);
		out.agregarValidacionesSalida(validacionExistenciaFatcaPaisResidenciaPersona);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		sql.batch(con, SQL_DELETE_FATCA_PAIS_RESIDENCIA, entrada.p_FatcaPaisResidencia, FatcaPaisResidencia.class);			
		
		msg = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PAIS_RESIDENCIA_ELIMINADO").msg();
		out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO",msg));			
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::EliminarBorradorFatcaPaisResidencia", fin - inicio);
		return out;
	}
	
	private List<Validacion> validarExistenciaFatcaPaisResidenciaPersona(Connection con, EntradaEliminarBorradorFatcaPaisResidencia entrada) throws Exception {
		logger.debug("INICIO::EliminarBorradorFatcaPaisResidencia.validarExistenciaFatcaPaisResidenciaPersona");
		long inicio = System.currentTimeMillis();
		List<Validacion> validaciones = new ArrayList<Validacion>();
		Validacion validacion = new Validacion();
		
		FatcaPaisResidencia fatcaPaisResidencia = entrada.p_FatcaPaisResidencia.parallelStream().findFirst().orElse(null);
		
		if(fatcaPaisResidencia != null) {
			HashMap<String,Object> parametros = new HashMap<String,Object>(3);
			parametros.put("IdEnrolamiento", fatcaPaisResidencia.IdEnrolamiento);
			parametros.put("IdPaisResidencia", fatcaPaisResidencia.IdPaisResidencia);
		
			SQL sql = new SQL();
			Long count = sql.countQuery(con, SQL_EXISTE_FATCA_PAIS_RESIDENCIA_PERSONA, parametros);
		
			if(count > 0) {
				validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_PAIS_RESIDENCIA_ASOCIADO_GENERAL", new HashMap<String,String>(), con, entrada.p_IdEmpresa);
			}
		}
		
		validaciones.add(validacion);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarBorradorFatcaPaisResidencia.validarExistenciaFatcaPaisResidenciaPersona", fin - inicio);
		return validaciones;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\"p_IdEmpresa\":1,\"p_FatcaPaisResidencia\":[{\"IdEnrolamiento\":30,\"IdPaisResidencia\":2}]}";
			EntradaEliminarBorradorFatcaPaisResidencia Objin = gson.fromJson(json, EntradaEliminarBorradorFatcaPaisResidencia.class);
			EliminarBorradorFatcaPaisResidencia demo = new EliminarBorradorFatcaPaisResidencia();				
			try {
				SalidaAdministrarBorrador ObjOut=demo.logica(con, Objin);
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
