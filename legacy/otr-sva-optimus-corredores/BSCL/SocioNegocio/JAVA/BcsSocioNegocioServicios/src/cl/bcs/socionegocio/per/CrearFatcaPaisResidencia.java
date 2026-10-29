package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ModificarFatcaPaisResidencia.EntidadLogGenerarContrato;
import cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearFatcaPaisResidencia implements JavaCall{
	static final Logger logger = LogManager.getLogger(CrearFatcaPaisResidencia.class);
	
	private String SQL_SELECT_EXISTE_PERSONA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearFatcaPaisResidencia_Select_Existe_FatcaPaisResidencia.sql");
	private String SQL_INSERT_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearFatcaPaisResidencia_Insert_FatcaPaisResidencia.sql");
	public  String SQL_INSERT_LOG_GENERAR_DOCUMENTOS	= loadResourceToString("cl/bcs/socionegocio/per/sql/CrearFatcaPaisResidencia_InsertLogGenerarContratos.sql");
	
	public static class EntradaCrearFatcaPaisResidencia extends BcsPojo{
		public Long							p_IdEmpresa; 
		public Long 						p_IdUsuario;
		public List<FatcaPaisResidencia> 	p_FatcaPaisResidencia;
		
		public EntradaCrearFatcaPaisResidencia(){
			this.p_FatcaPaisResidencia = new ArrayList<FatcaPaisResidencia>();
		}	
	}
	
	public static class SalidaCrearFatcaPaisResidencia extends BcsPojo{
		public Long					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public SalidaCrearFatcaPaisResidencia(){
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	}
	
	public static class Validacion extends BcsPojo{
		public Boolean EsError;
		public MensajeSalida MensajeError;
		
		public Validacion() {
			this.EsError = false;
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
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearFatcaPaisResidencia in = gson.fromJson(jin.toString(), EntradaCrearFatcaPaisResidencia.class);
		SalidaCrearFatcaPaisResidencia out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearFatcaPaisResidencia logica(Connection con, EntradaCrearFatcaPaisResidencia entrada) throws Exception {
		logger.info("INICIO::CrearFatcaPaisResidencia");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearFatcaPaisResidencia out = new SalidaCrearFatcaPaisResidencia();
		String msg ="";
		SQL sql = new SQL();
		
		Validacion validacionExistenciaFatcaPaisResidencia = validarExistenciaFatcaPaisResidencia(con, entrada);
		out = agregarValidacionSalida(out, validacionExistenciaFatcaPaisResidencia);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		sql.batch(con, SQL_INSERT_FATCA_PAIS_RESIDENCIA, entrada.p_FatcaPaisResidencia, FatcaPaisResidencia.class);		
		
		msg = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PAIS_RESIDENCIA_AGREGADO").msg();
		out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO", msg));
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
				logGenerarContrato.Mensaje = "Persona - CrearFatcaPaisResidencia.java";
				params.add(logGenerarContrato);				
			}				
			SQL sqlCampos = new SQL();
			sqlCampos.batch(con, SQL_INSERT_LOG_GENERAR_DOCUMENTOS, params,EntidadLogGenerarContrato.class);
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearFatcaPaisResidencia", finServicio - inicioServicio);
		return out;
	}
	
	private Validacion validarExistenciaFatcaPaisResidencia(Connection con, EntradaCrearFatcaPaisResidencia entrada) throws Exception {
		logger.debug("INICIO::CrearFatcaPaisResidencia.validarExistenciaFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		Validacion validacion = new Validacion();
		
		FatcaPaisResidencia fatcaPaisResidencia = entrada.p_FatcaPaisResidencia.parallelStream().findFirst().orElse(null);
		
		if(fatcaPaisResidencia != null) {
			HashMap<String,Object> parametros = new HashMap<String,Object>(3);
			parametros.put("IdPersona", fatcaPaisResidencia.IdPersona);
			parametros.put("IdPaisResidencia", fatcaPaisResidencia.IdPaisResidencia);
		
			SQL sql = new SQL();
			Long count = sql.countQuery(con, SQL_SELECT_EXISTE_PERSONA_PAIS_RESIDENCIA, parametros);
		
			if(count > 0) {
				validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_EXISTE_PAIS_RESIDENCIA", new HashMap<String,String>(), con, entrada.p_IdEmpresa);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearFatcaPaisResidencia.validarExistenciaFatcaPaisResidencia", fin - inicio);
		return validacion;
	}
	
	private SalidaCrearFatcaPaisResidencia agregarValidacionSalida(SalidaCrearFatcaPaisResidencia salida, Validacion validacion) {
		logger.trace("INICIO::CrearFatcaPaisResidencia.agregarValidacionSalida");
		long inicio = System.currentTimeMillis();
		if(validacion.EsError) {
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add(validacion.MensajeError);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearFatcaPaisResidencia.agregarValidacionSalida", fin - inicio);
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"IdPersona\":1752703,\"IdPaisResidencia\":\"5\",\"TIN\":\"1111111\",\"IdCausaNoTin\":\"\",\"Observacion\":\"\",\"PaisResidenciaPrincipal\":\"N\"}";
			EntradaCrearFatcaPaisResidencia Objin = gson.fromJson(json, EntradaCrearFatcaPaisResidencia.class);
			
			CrearFatcaPaisResidencia demo=new CrearFatcaPaisResidencia();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaCrearFatcaPaisResidencia ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}finally {
				con.rollback();	 			
			}
	    }
		System.out.println("== END ==");
	}

}
