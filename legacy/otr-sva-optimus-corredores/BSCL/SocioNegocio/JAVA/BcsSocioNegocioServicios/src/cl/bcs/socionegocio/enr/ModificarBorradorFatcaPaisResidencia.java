package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
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
import cl.bcs.socionegocio.enr.pojo.FatcaPaisResidencia;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarBorradorFatcaPaisResidencia implements JavaCall{
	static final Logger logger = LogManager.getLogger(ModificarBorradorFatcaPaisResidencia.class);

	private String SQL_UPDATE_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorFatcaPaisResidencia_UpdateFatcaPaisResidencia.sql");
	private String SQL_UPDATE_FATCA_PAIS_RESIDENCIA_PRINCIPAL = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorFatcaPaisResidencia_UpdateFatcaPaisResidenciaPrincipal.sql");
	private String SQL_SELECT_FATCA_PAIS_RESIDENCIA_NO_PRINCIPAL = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorFatcaPaisResidencia_SelectFatcaPaisResidenciaNoPrincipal.sql");
	private String SQL_DELETE_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorFatcaPaisResidencia_DeleteFatcaPaisResidencia.sql");

	public static class EntradaModificarBorradorFatcaPaisResidencia extends EntradaAdministrarBorrador {
		public List<FatcaPaisResidencia> 	p_FatcaPaisResidencia;
		
		
		public EntradaModificarBorradorFatcaPaisResidencia(){
			this.p_FatcaPaisResidencia 		= new ArrayList<FatcaPaisResidencia>();			
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarBorradorFatcaPaisResidencia jin = gson.fromJson(in.toString(), EntradaModificarBorradorFatcaPaisResidencia.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorFatcaPaisResidencia entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorFatcaPaisResidencia");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();

		prepararPaisesResidenciaEntrantes(con, entrada);
		salidaAdministrarBorrador = entrada.validarCamposObligatoriosEntrada(con, entrada.p_FatcaPaisResidencia, FatcaPaisResidencia.class, salidaAdministrarBorrador);

		String msg ="";
		
		try {			
			FatcaPaisResidencia fatcaPaisResidencia  = entrada.p_FatcaPaisResidencia.parallelStream().findFirst().get();

			if("S".equals(fatcaPaisResidencia.PaisResidenciaPrincipal)) {				
				actualizarPaisResidenciaPrincipal(con,entrada, fatcaPaisResidencia);
			}else {
				actualizarPaisResidenciaSecundario(con, entrada);
			}			
		}catch(Exception e) {
			msg = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_ERROR_MODIFICAR_PAIS_RESIDENCIA").msg();
			salidaAdministrarBorrador.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR",msg));
			salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaAdministrarBorrador.P_OUT_MENSAJES);
			e.printStackTrace();
		}
		
		salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaAdministrarBorrador.P_OUT_MENSAJES);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorFatcaPaisResidencia", finServicio - inicioServicio);
		return salidaAdministrarBorrador;
	}

	private void prepararPaisesResidenciaEntrantes(Connection con, EntradaModificarBorradorFatcaPaisResidencia entrada) {
		logger.debug("INICIO::ModificarBorradorFatcaPaisResidencia.prepararPaisesResidenciaEntrantes");
		long inicio = System.currentTimeMillis();

		for (FatcaPaisResidencia paisResidencia : entrada.p_FatcaPaisResidencia) {
			paisResidencia.EstaInsertada = "S";
			paisResidencia.TieneTin = StringUtils.isNullOrBlank(paisResidencia.TIN) ? "N" : "S";
			paisResidencia.IdCausaNoTin = StringUtils.isNullOrBlank(paisResidencia.IdCausaNoTin) ? "" : paisResidencia.IdCausaNoTin;
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorFatcaPaisResidencia.prepararPaisesResidenciaEntrantes {}", fin - inicio);
	}
	
	public SalidaAdministrarBorrador actualizarPaisResidenciaPrincipal(Connection con,EntradaModificarBorradorFatcaPaisResidencia entrada, FatcaPaisResidencia fatcaPaisResidencia) throws Exception {
		logger.info("INICIO::ModificarBorradorFatcaPaisResidencia.actualizarPaisResidenciaPrincipal");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		String msg = "";
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		HashMap<String, Object> paramsSelect = new HashMap<String, Object>();
		paramsSelect.put("IdEnrolamiento", fatcaPaisResidencia.IdEnrolamiento);
		List<FatcaPaisResidencia> fatcasPaisResidenciaNoPrincipales = sql.query(con, SQL_SELECT_FATCA_PAIS_RESIDENCIA_NO_PRINCIPAL, paramsSelect, FatcaPaisResidencia.class);
		
		List<FatcaPaisResidencia> fatcaPaisResidenciaRepetidos = fatcasPaisResidenciaNoPrincipales.parallelStream()
				.filter(paisResidencia-> paisResidencia.IdPaisResidencia.equals(fatcaPaisResidencia.IdPaisResidencia) 
						&& paisResidencia.IdEnrolamiento.equals(fatcaPaisResidencia.IdEnrolamiento)).collect(Collectors.toList());
		
		if(!fatcaPaisResidenciaRepetidos.isEmpty()) {
			sql.batch(con, SQL_DELETE_FATCA_PAIS_RESIDENCIA, fatcaPaisResidenciaRepetidos, FatcaPaisResidencia.class);
		}			
		
		sql.batch(con, SQL_UPDATE_FATCA_PAIS_RESIDENCIA_PRINCIPAL, entrada.p_FatcaPaisResidencia, FatcaPaisResidencia.class);

		msg = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PAIS_RESIDENCIA_MODIFICADO").msg();
		salidaAdministrarBorrador.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO",msg));
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorFatcaPaisResidencia.actualizarPaisResidenciaPrincipal", finServicio - inicioServicio);
		return salidaAdministrarBorrador;
	}
	
	public SalidaAdministrarBorrador actualizarPaisResidenciaSecundario(Connection con, EntradaModificarBorradorFatcaPaisResidencia entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorFatcaPaisResidencia.actualizarPaisResidenciaSecundario");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		String msg = "";
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		sql.batch(con, SQL_UPDATE_FATCA_PAIS_RESIDENCIA, entrada.p_FatcaPaisResidencia, FatcaPaisResidencia.class);
		
		msg = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PAIS_RESIDENCIA_MODIFICADO").msg();
		salidaAdministrarBorrador.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO",msg));
	
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorFatcaPaisResidencia.actualizarPaisResidenciaSecundario", finServicio - inicioServicio);
		return salidaAdministrarBorrador;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"FATCA_PAIS_RESIDENCIA_FULL_JURIDICA\",\"p_IdTipoCanal\":\"OPT\",\"p_FatcaPaisResidencia\":[{\"IdEnrolamiento\":456,\"IdPaisResidencia\":0,\"TIN\":\"\",\"IdCausaNoTin\":\"TNSR\",\"Observacion\":\"\",\"PaisResidenciaPrincipal\":\"N\"}]}\n";
					
			EntradaModificarBorradorFatcaPaisResidencia Objin = gson.fromJson(json, EntradaModificarBorradorFatcaPaisResidencia.class);
			
			ModificarBorradorFatcaPaisResidencia demo = new ModificarBorradorFatcaPaisResidencia();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 		
 			try {
 				SalidaAdministrarBorrador ObjOut = demo.logica(con, Objin);
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
