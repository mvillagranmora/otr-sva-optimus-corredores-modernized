package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.nuam.StoredProcedureException;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ModificarPersonaDatosGenerales.EntidadLogGenerarContrato;
import cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarFatcaPaisResidencia implements JavaCall{
	static final Logger logger = LogManager.getLogger(ModificarFatcaPaisResidencia.class);

	private String SQL_UPDATE_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarFatcaPaisResidencia_UpdateFatcaPaisResidencia.sql");
	private String SQL_UPDATE_FATCA_PAIS_RESIDENCIA_PRINCIPAL = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarFatcaPaisResidencia_UpdateFatcaPaisResidenciaPrincipal.sql");
	private String SQL_SELECT_FATCA_PAIS_RESIDENCIA_NO_PRINCIPAL = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarFatcaPaisResidencia_SelectFatcaPaisResidenciaNoPrincipal.sql");
	private String SQL_DELETE_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarFatcaPaisResidencia_DeleteFatcaPaisResidencia.sql");
	public  String SQL_INSERT_LOG_GENERAR_DOCUMENTOS	= loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarFatcaPaisResidencia_InsertLogGenerarContratos.sql");
	public  String SQL_SELECT_VALORES_ANTIGUOS	= loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarFatcaPaisResidencia_SelectPaisResidenciaAnterior.sql");	
	
	public static class EntradaModificarFatcaPaisResidencia extends BcsPojo {
		public Long 						p_IdEmpresa;
		public Long 						p_IdUsuario;
		public List<FatcaPaisResidencia> 	p_FatcaPaisResidencia;
		
		public EntradaModificarFatcaPaisResidencia(){
			this.p_FatcaPaisResidencia 		= new ArrayList<FatcaPaisResidencia>();			
		}
	}
	
	public static class SalidaModificarFatcaPaisResidencia extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;		
		
		public SalidaModificarFatcaPaisResidencia(){
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();			
		}
	}
	
	
    public static class EntidadLogGenerarContrato extends BcsPojo{
    	public Long Idlog;
    	public String CodigoFormulario;
    	public String CampoBackend;
    	public Long IdEmpresa;
    	public Long IdCliente;
    	public Long IdPersona;
    	public Long IdUsuario;
    	public String FechaSistema;
    	public Long IdPrograma;
    	public String Estado;
    	public String Mensaje;
    }

    public static class EntidadFatcaPaisResidenciaValoresAntiguos extends BcsPojo{
    	public String TIN;
    	public String IdCausaNoTin;
    	public String Observacion;
    	public String PaisResidenciaPrincipal;
    }
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarFatcaPaisResidencia jin = gson.fromJson(in.toString(), EntradaModificarFatcaPaisResidencia.class);
		SalidaModificarFatcaPaisResidencia out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarFatcaPaisResidencia logica(Connection con, EntradaModificarFatcaPaisResidencia entrada) throws Exception {
		logger.info("INICIO::ModificarFatcaPaisResidencia");
		long inicioServicio = System.currentTimeMillis();
		SalidaModificarFatcaPaisResidencia out = new SalidaModificarFatcaPaisResidencia();		
		String msg ="";
		
		try {	
			FatcaPaisResidencia fatcaPaisResidencia  = entrada.p_FatcaPaisResidencia.parallelStream().findFirst().get();
			insertTablaLogGenerarContratos(con,entrada);

			if("S".equals(fatcaPaisResidencia.PaisResidenciaPrincipal)) {				
				out = actualizarPaisResidenciaPrincipal(con,entrada, fatcaPaisResidencia);
			}else {
				out = actualizarPaisResidenciaSecundario(con, entrada);
				out = actualizarPaisResidenciaSecundario(con, entrada);
			}			

		}catch(Exception e) {
			msg = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_ERROR_MODIFICAR_PAIS_RESIDENCIA").msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR",msg));
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			e.printStackTrace();
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		
		
		String campos[] = {"IdPaisResidencia","TIN","IdCausaNoTin" };	
		
		if(out.P_OUT_ERRORESCONTADOR==0 && entrada.p_FatcaPaisResidencia.size() > 0) {
			List<EntidadLogGenerarContrato> params = new ArrayList<EntidadLogGenerarContrato>();
			
			List<Long> idSecuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", (long) entrada.p_FatcaPaisResidencia.size());
			for(int i=0;i< campos.length ;i++) {
				EntidadLogGenerarContrato logGenerarContrato = new EntidadLogGenerarContrato();
				logGenerarContrato.Idlog = idSecuencias.get(0).longValue();
				logGenerarContrato.CodigoFormulario = "FATCA_PAIS_RESIDENCIA_";
				logGenerarContrato.CampoBackend = campos[i];
				logGenerarContrato.IdEmpresa = entrada.p_IdEmpresa;
				logGenerarContrato.IdCliente = 0L;
				logGenerarContrato.IdPersona = entrada.p_FatcaPaisResidencia.get(0).IdPersona;
				logGenerarContrato.IdUsuario = entrada.p_IdUsuario;
				logGenerarContrato.FechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
				logGenerarContrato.IdPrograma = 0L;
				logGenerarContrato.Estado = "PEN";
				logGenerarContrato.Mensaje = "Persona - ModificarFatcaPaisResidencia.java";
				params.add(logGenerarContrato);				
			}				
			SQL sql = new SQL();
			
			System.out.println(params);
			
			sql.batch(con, SQL_INSERT_LOG_GENERAR_DOCUMENTOS, params,EntidadLogGenerarContrato.class);
		}			
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarFatcaPaisResidencia", finServicio - inicioServicio);
		return out;
	}


	public void insertTablaLogGenerarContratos(Connection con, EntradaModificarFatcaPaisResidencia entrada) throws SQLException, StoredProcedureException {				
		logger.debug("INICIO::ModificarFatcaPaisResidencia.insertTablaLogGenerarContratos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		HashMap<String, Object> paramsSelect = new HashMap<String, Object>(2);
		FatcaPaisResidencia fatcaPaisResidencia = entrada.p_FatcaPaisResidencia.get(0);
		paramsSelect.put("IdPersona", fatcaPaisResidencia.IdPersona);
		paramsSelect.put("IdPaisResidencia", fatcaPaisResidencia.IdPaisResidencia);
		
		//SE AGREGA CONDICION PARA EVITAR QUE SE CAIGA AL NO TRAER NADA CON LA CONSULTA SQL_SELECT_VALORES_ANTIGUOS
		List<EntidadFatcaPaisResidenciaValoresAntiguos> listaFatcaPaisResidenciaValoresAntiguos = sql.query(con, SQL_SELECT_VALORES_ANTIGUOS, paramsSelect, EntidadFatcaPaisResidenciaValoresAntiguos.class);
		if(!listaFatcaPaisResidenciaValoresAntiguos.isEmpty()) {
			EntidadFatcaPaisResidenciaValoresAntiguos fatcaPaisResidenciaValoresAntiguos = listaFatcaPaisResidenciaValoresAntiguos.get(0);
			FatcaPaisResidencia fatcaPaisResidenciaValoresNuevos = entrada.p_FatcaPaisResidencia.get(0);
	    	
			
			List<String> atributosCambiados = new ArrayList<String>();
			if(!fatcaPaisResidenciaValoresAntiguos.TIN.equals(fatcaPaisResidenciaValoresNuevos.TIN)) atributosCambiados.add("TIN");
			if(!fatcaPaisResidenciaValoresAntiguos.IdCausaNoTin.equals(fatcaPaisResidenciaValoresNuevos.IdCausaNoTin)) atributosCambiados.add("IdCausaNoTin");
			if(!fatcaPaisResidenciaValoresAntiguos.Observacion.equals(fatcaPaisResidenciaValoresNuevos.Observacion)) atributosCambiados.add("Observacion");
			if(!fatcaPaisResidenciaValoresAntiguos.PaisResidenciaPrincipal.equals(fatcaPaisResidenciaValoresNuevos.PaisResidenciaPrincipal)) atributosCambiados.add("PaisResidenciaPrincipal");
			
			List<EntidadLogGenerarContrato> params = new ArrayList<EntidadLogGenerarContrato>();
	    	List<Long> idSecuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", (long) atributosCambiados.size());
	    	String fechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
	    	
	    	for(String atributo: atributosCambiados) {
	    		EntidadLogGenerarContrato logGenerarContrato = new EntidadLogGenerarContrato();
	    		logGenerarContrato.Idlog = idSecuencias.remove(0);
	    		logGenerarContrato.CodigoFormulario = "FATCA_PAIS_RESIDENCIA_";
	    		logGenerarContrato.CampoBackend = atributo;
	    		logGenerarContrato.IdEmpresa = entrada.p_IdEmpresa;
	    		logGenerarContrato.IdCliente = 0L;
	    		logGenerarContrato.IdPersona = fatcaPaisResidenciaValoresNuevos.IdPersona;
	    		logGenerarContrato.IdUsuario = entrada.p_IdUsuario;
	    		logGenerarContrato.FechaSistema = fechaSistema;
	    		logGenerarContrato.IdPrograma = 0L;
	    		logGenerarContrato.Estado = "PEN";
	    		logGenerarContrato.Mensaje = "Cliente - ConfirmarModificarClienteDatosGeneralesMasivo.java";
	    		params.add(logGenerarContrato);
	    		
	    	}
			sql.batch(con, SQL_INSERT_LOG_GENERAR_DOCUMENTOS, params,EntidadLogGenerarContrato.class);
			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarFatcaPaisResidencia.insertTablaLogGenerarContratos", fin - inicio);
	}

	public SalidaModificarFatcaPaisResidencia actualizarPaisResidenciaPrincipal(Connection con,EntradaModificarFatcaPaisResidencia entrada, FatcaPaisResidencia fatcaPaisResidencia) throws Exception {
		logger.info("INICIO::ModificarFatcaPaisResidencia.actualizarPaisResidenciaPrincipal");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		String msg = "";
		SalidaModificarFatcaPaisResidencia salidaModificarFatcaPaisResidenciaPrincipal = new SalidaModificarFatcaPaisResidencia();
		
		HashMap<String, Object> paramsSelect = new HashMap<String, Object>();
		paramsSelect.put("IdPersona", fatcaPaisResidencia.IdPersona);
		List<FatcaPaisResidencia> fatcasPaisResidenciaNoPrincipales = sql.query(con, SQL_SELECT_FATCA_PAIS_RESIDENCIA_NO_PRINCIPAL, paramsSelect, FatcaPaisResidencia.class);
		
		List<FatcaPaisResidencia> fatcaPaisResidenciaRepetidos = fatcasPaisResidenciaNoPrincipales.parallelStream()
				.filter(paisResidencia-> paisResidencia.IdPaisResidencia.equals(fatcaPaisResidencia.IdPaisResidencia) 
						&& paisResidencia.IdPersona.equals(fatcaPaisResidencia.IdPersona)).collect(Collectors.toList());
		
		if(!fatcaPaisResidenciaRepetidos.isEmpty()) {
			sql.batch(con, SQL_DELETE_FATCA_PAIS_RESIDENCIA, fatcaPaisResidenciaRepetidos, FatcaPaisResidencia.class);
		}			
		
		sql.batch(con, SQL_UPDATE_FATCA_PAIS_RESIDENCIA_PRINCIPAL, entrada.p_FatcaPaisResidencia, FatcaPaisResidencia.class);

		msg = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PAIS_RESIDENCIA_MODIFICADO").msg();
		salidaModificarFatcaPaisResidenciaPrincipal.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO",msg));
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarFatcaPaisResidencia.actualizarPaisResidenciaPrincipal", finServicio - inicioServicio);
		return salidaModificarFatcaPaisResidenciaPrincipal;
	}
	
	public SalidaModificarFatcaPaisResidencia actualizarPaisResidenciaSecundario(Connection con, EntradaModificarFatcaPaisResidencia entrada) throws Exception {
		logger.info("INICIO::ModificarFatcaPaisResidencia.actualizarPaisResidenciaSecundario");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		String msg = "";
		SalidaModificarFatcaPaisResidencia salidaModificarFatcaPaisResidencia = new SalidaModificarFatcaPaisResidencia();
		
		sql.batch(con, SQL_UPDATE_FATCA_PAIS_RESIDENCIA, entrada.p_FatcaPaisResidencia, FatcaPaisResidencia.class);
		
		msg = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PAIS_RESIDENCIA_MODIFICADO").msg();
		salidaModificarFatcaPaisResidencia.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO",msg));
	
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarFatcaPaisResidencia.actualizarPaisResidenciaSecundario", finServicio - inicioServicio);
		return salidaModificarFatcaPaisResidencia;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_FatcaPaisResidencia\":[{\"IdPersona\":1790842,\"IdPaisResidencia\":223,\"TIN\":\"NADA\",\"IdCausaNoTin\":\"\",\"Observacion\":\"\",\"PaisResidenciaPrincipal\":\"S\"}]}";
					
			EntradaModificarFatcaPaisResidencia Objin = gson.fromJson(json, EntradaModificarFatcaPaisResidencia.class);
			
			ModificarFatcaPaisResidencia demo = new ModificarFatcaPaisResidencia();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 		
 			try {
 				SalidaModificarFatcaPaisResidencia ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}
