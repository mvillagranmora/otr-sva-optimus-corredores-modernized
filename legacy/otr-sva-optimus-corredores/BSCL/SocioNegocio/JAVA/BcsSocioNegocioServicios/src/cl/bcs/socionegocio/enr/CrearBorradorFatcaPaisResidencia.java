package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Fatca;
import cl.bcs.socionegocio.enr.pojo.FatcaPaisResidencia;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorFatcaPaisResidencia implements JavaCall{
	static final Logger logger = LogManager.getLogger(CrearBorradorFatcaPaisResidencia.class);
	
	private String SQL_SELECT_EXISTE_PERSONA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorFatcaPaisResidencia_SelectFatcaPaisResidencia.sql");
	private String SQL_INSERT_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorFatcaPaisResidencia_InsertFatcaPaisResidencia.sql");
	
	public static class EntradaCrearBorradorFatcaPaisResidencia extends EntradaAdministrarBorrador{
		public List<FatcaPaisResidencia> 	p_FatcaPaisResidencia;
		
		public EntradaCrearBorradorFatcaPaisResidencia(){
			this.p_FatcaPaisResidencia = new ArrayList<FatcaPaisResidencia>();
		}	
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearBorradorFatcaPaisResidencia in = gson.fromJson(jin.toString(), EntradaCrearBorradorFatcaPaisResidencia.class);
		SalidaAdministrarBorrador out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorFatcaPaisResidencia entrada) throws Exception {
		logger.info("INICIO::CrearBorradorFatcaPaisResidencia");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		prepararPaisesResidenciaEntrantes(entrada);

		String msg = "";
		SQL sql = new SQL();

		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_FatcaPaisResidencia, FatcaPaisResidencia.class, out);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		
		List<Validacion> validacionExistenciaFatcaPaisResidencia = validarExistenciaFatcaPaisResidencia(con, entrada);
		out.agregarValidacionesSalida(validacionExistenciaFatcaPaisResidencia);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		sql.batch(con, SQL_INSERT_FATCA_PAIS_RESIDENCIA, entrada.p_FatcaPaisResidencia, FatcaPaisResidencia.class);		
		
		msg = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PAIS_RESIDENCIA_AGREGADO").msg();
		out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO", msg));
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorFatcaPaisResidencia", finServicio - inicioServicio);
		return out;
	}

	private void prepararPaisesResidenciaEntrantes(EntradaCrearBorradorFatcaPaisResidencia entrada) {
		logger.debug("INICIO::CrearBorradorFatcaPaisResidencia.prepararPaisesResidenciaEntrantes");
		long inicio = System.currentTimeMillis();

		for (FatcaPaisResidencia paisResidencia : entrada.p_FatcaPaisResidencia) {
			paisResidencia.EstaInsertada = "N";
			paisResidencia.TieneTin = StringUtils.isNullOrBlank(paisResidencia.TIN) ? "N" : "S";
			paisResidencia.IdCausaNoTin = StringUtils.isNullOrBlank(paisResidencia.IdCausaNoTin) ? "" : paisResidencia.IdCausaNoTin;
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorFatcaPaisResidencia.prepararPaisesResidenciaEntrantes {}", fin - inicio);
	}

	private List<Validacion> validarExistenciaFatcaPaisResidencia(Connection con, EntradaCrearBorradorFatcaPaisResidencia entrada) throws Exception {
		logger.debug("INICIO::CrearBorradorFatcaPaisResidencia.validarExistenciaFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		List<Validacion> validaciones = new ArrayList<Validacion>();
		Validacion validacion = new Validacion();
		
		FatcaPaisResidencia fatcaPaisResidencia = entrada.p_FatcaPaisResidencia.parallelStream().findFirst().orElse(null);

		if(objetoVacio(fatcaPaisResidencia)) fatcaPaisResidencia = null;
		if(fatcaPaisResidencia != null) {
			HashMap<String,Object> parametros = new HashMap<String,Object>(3);
			parametros.put("IdEnrolamiento", fatcaPaisResidencia.IdEnrolamiento);
			parametros.put("IdPaisResidencia", fatcaPaisResidencia.IdPaisResidencia);
		
			SQL sql = new SQL();
			Long count = sql.countQuery(con, SQL_SELECT_EXISTE_PERSONA_PAIS_RESIDENCIA, parametros);
		
			if(count > 0) {
				validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_EXISTE_PAIS_RESIDENCIA", new HashMap<String,String>(), con, entrada.p_IdEmpresa);
			}
		}
		
		validaciones.add(validacion);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorFatcaPaisResidencia.validarExistenciaFatcaPaisResidencia", fin - inicio);
		return validaciones;
	}
	
	private boolean objetoVacio(FatcaPaisResidencia fatcaPaisResidencia) {
	    if(fatcaPaisResidencia==null)return true;
	    boolean noTienePais =fatcaPaisResidencia.IdPaisResidencia == 0;
	    boolean noTieneTin =fatcaPaisResidencia.TIN.isEmpty();
	    boolean noTieneCausaNoTin =fatcaPaisResidencia.IdCausaNoTin.isEmpty();
	    boolean noTieneObservacion =fatcaPaisResidencia.Observacion.isEmpty();
	    return noTienePais && noTieneTin && noTieneCausaNoTin && noTieneObservacion;
	}

	public static void main(String[] args) throws Exception {

		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"FATCA_PAIS_RESIDENCIA_FULL_JURIDICA\",\"p_IdTipoCanal\":\"OPT\",\"p_FatcaPaisResidencia\":[{\"IdEnrolamiento\":456,\"IdPaisResidencia\":5,\"TIN\":\"ASDASDSAD\",\"IdCausaNoTin\":\"\",\"Observacion\":\"\",\"PaisResidenciaPrincipal\":\"N\"}]}\n";
			EntradaCrearBorradorFatcaPaisResidencia Objin = gson.fromJson(json, EntradaCrearBorradorFatcaPaisResidencia.class);
			
			CrearBorradorFatcaPaisResidencia demo=new CrearBorradorFatcaPaisResidencia();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaAdministrarBorrador ObjOut = demo.logica(con, Objin);
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
