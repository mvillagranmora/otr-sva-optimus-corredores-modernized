package cl.bcs.plataforma.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.CallRestService;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;


public class ActualizarPreciosVar extends BcsJob implements JavaCall{
	
	private static final Logger logger 				= LogManager.getLogger(ActualizarPreciosVar.class);
	private final String SQL_UPSERT_PRECIO_VAR 		= loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizaPreciosVar_UpsertPreciosVar.sql");
	private final String SQL_SELECT_NEMOTECNICOS 	= loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizaPreciosVar_SelectNemotecnicos.sql");
	
	public static class Nemotecnicos extends BcsPojo {
		
		public Long IdInstrumento;
		public String Nemotecnico;

	}
	
	public class SalidaWebService extends BcsPojo {
		
		public CabeceraResultadoWebService cabecera;
		
	}
	
	public class CabeceraResultadoWebService extends BcsPojo {
		
		List<ResultadoWebService> consultaVectorPrecioPTResult;
		
	}
	
	public class ResultadoWebService extends BcsPojo {
		
		public Long IdInstrumento;
		public String FechaVector;
		public String nemo;
		public BigDecimal precioBase;
		public Long secLista;
		public String tipo;
		public String valor;
		public BigDecimal valor01;
		public BigDecimal valor02;
		public BigDecimal valor03;
		public BigDecimal valor04;
		public BigDecimal valor05;
		public BigDecimal valor95;
		public BigDecimal valor98;

	}

	@Override
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		long idEmpresa = Long.parseLong(this.getOptionalParam("IdEmpresa").orElse("1"));
		return logica(conn,false, "", idEmpresa);
	}
	
	public boolean logica(Connection conn, boolean debug, String fechaSistemaDebug, long idEmpresa) throws InterruptedException, Throwable {
		
		logger.info("INICIO::ActualizarPrecioVar");
		
		SQL sql 	= new SQL();
		Long init 	= System.currentTimeMillis();

		List<Nemotecnicos> listadoNemotecnicosBaseDeDatos = sql.query(conn, SQL_SELECT_NEMOTECNICOS, new HashMap<String,Object>(), Nemotecnicos.class);
		
		String fechaSistema 	= (debug) ? fechaSistemaDebug : ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String servidor 		= ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "MER.ServidorTec");
		String urlWebservice 	= ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "MER.UrlServicioPrecios");
		String user 			= ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "MER.UserPreciosVar");
		String passwd 			= ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "MER.PassPreciosVar");
		boolean auth 			= true;
		
		JsonArray entradaWebservice = new JsonArray();
		JsonObject entrada 			= new JsonObject();
		entrada.addProperty("nemo", "");
		entradaWebservice.add(entrada);
		
		try {
			
			CallRestService call = new CallRestService(servidor, urlWebservice, user, passwd, auth, debug);
			String salidaWebservice = call.callRestPost(entradaWebservice.toString());
			Gson gson = getGsonBuilder();
			SalidaWebService[] salidaWebService = gson.fromJson(salidaWebservice, SalidaWebService[].class);
	
			List<ResultadoWebService> listadoNemotecnicosWS = this.ProcesarSalidaWebService(conn, salidaWebService);
			this.UpsertPreciosVar(conn, listadoNemotecnicosWS, listadoNemotecnicosBaseDeDatos, fechaSistema);
			
		} catch (ParseException e) {
			
			e.printStackTrace();
			return false;
			
		}
		
		logger.info("FIN::ActualizarPrecioVar, " + (System.currentTimeMillis() - init));
		return true;	
	}

	private List<ResultadoWebService> ProcesarSalidaWebService(Connection conn, SalidaWebService[] salidaWebService) {
		
		logger.info("INICIO::ActualizarPrecioVar.ProcesarSalidaWebService");	
		
		List<ResultadoWebService> listadoNemotecnicos = new ArrayList<ResultadoWebService>();
		
		for ( SalidaWebService salidaWS : salidaWebService ) {
			
			listadoNemotecnicos.addAll(salidaWS.cabecera.consultaVectorPrecioPTResult);

		}
		
		logger.info("FIN::ActualizarPrecioVar.ProcesarSalidaWebService");	
		return listadoNemotecnicos;
	}
	
	private void UpsertPreciosVar(Connection conn, List<ResultadoWebService> listadoNemotecnicosWS, List<Nemotecnicos> listadoNemotecnicosBaseDeDatos, String fechaSistema) throws SQLException {

		logger.info("INICIO::ActualizarPrecioVar.UpsertPreciosVar");	
		
		SQL sql = new SQL();

		List<ResultadoWebService> descartados = new ArrayList<ResultadoWebService>();
		
		for (int i = listadoNemotecnicosWS.size() - 1; i >= 0; i--) {
			 
			String nemotecnico 							= listadoNemotecnicosWS.get(i).nemo.trim();
			listadoNemotecnicosWS.get(i).nemo 			= nemotecnico;
			listadoNemotecnicosWS.get(i).FechaVector 	= fechaSistema;
			List<Nemotecnicos> result 					= listadoNemotecnicosBaseDeDatos
														 	.stream()
														 	.filter( item -> item.Nemotecnico.equals(nemotecnico) )
														 	.collect(Collectors.toList()); 
			
			if ( result.size() > 0 ) {
				listadoNemotecnicosWS.get(i).IdInstrumento = result.get(0).IdInstrumento;
			}
			else {
				descartados.add(listadoNemotecnicosWS.get(i));
				listadoNemotecnicosWS.remove(i);
			}
			  
		}
		
		sql.batch(conn, SQL_UPSERT_PRECIO_VAR, listadoNemotecnicosWS, ResultadoWebService.class);

		logger.info("FIN::ActualizarPrecioVar.UpsertPreciosVar");	

	}

	@Override
	public void onError(Throwable t) {
		logger.info(":ActualizarPrecioVar:  ****** ERROR  *******");
		t.printStackTrace();
	}

	@Override
	public void pause() {

	}
		
	public static void main(String[] args) throws Throwable {
		
		System.out.println("== INIT ==");
		try {
			
			Connection con = PlataformaUtils.getDESAConnection();
			ActualizarPreciosVar apf = new ActualizarPreciosVar();
			apf.logica(con,false, "", 1L);
	    	con.rollback();
	    	
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
        System.out.println("== END ==");
	}
	
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}


