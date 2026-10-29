package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.EntradaGenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.SalidaGenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.ClienteConfig;
import cl.bcs.normativa.interfaces.ClienteConfigFactory;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF_Scotia;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PautaGenerarInterfazP40SBIF_Scotia extends BcsJob implements BcsProcesoPauta {

	private static FileLoader loader = new FileLoader();
	static final Logger logger = LogManager.getLogger(PautaGenerarInterfazP40SBIF_Scotia.class);
	
	private final String SQL_CODIGO_CORREDORA = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarInterfazP40_SelectCorredoraCodigos.sql");
	private String SUBCARPETA = "NOR/"; 
	
	public static class DatoCodigoCorredora {
		public String 		CodigoCorredora;
	}
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		SQL sql = new SQL();
		logger.info("***** Inicio PautaGenerarInterfazP40SBIF_Scotia *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			HashMap<String, Object> paramsRut = new HashMap<String, Object>();
			paramsRut.put("IdEmpresa", (Long)parameters.get("IdEmpresa"));
			salida.P_OUT_ERRORESCONTADOR = 0L;
			List<DatoCodigoCorredora> datoCodigo = sql.query(con, SQL_CODIGO_CORREDORA, paramsRut, DatoCodigoCorredora.class);
			
            String fechaSistema = (String)parameters.get("FechaSistema");
			EntradaGenerarInterfazP40SBIF entrada = new EntradaGenerarInterfazP40SBIF();
			String rutaArchivo = Utils.getConfiguracion(con, Utils.getSchema(), (Long)parameters.get("IdEmpresa"), "PLA.RutaBaseArchivos");
			SUBCARPETA = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"NORMATIVA","SubRutaArchivosModulo"); 
			//Se configura una subcarpeta para alojar los archivos.
			rutaArchivo = rutaArchivo.concat(SUBCARPETA);
			
			//Nota: la clase ClienteConfig configura a la entradap40 según el código de corredora que se envía, no se necesitan todos los parametros de entrada
			ClienteConfig clienteConfig = ClienteConfigFactory.getClienteConfig(datoCodigo.get(0).CodigoCorredora);
			clienteConfig.configurarParametros(entrada, parameters.get("IdEmpresa").toString(), parameters.get("IdUsuario").toString(), rutaArchivo);			
    		entrada.p_Fecha = fechaSistema;
			GenerarInterfazP40SBIF_Scotia generarInterfazP40SBIF_Scotia = new GenerarInterfazP40SBIF_Scotia();
			SalidaGenerarInterfazP40SBIF resp = generarInterfazP40SBIF_Scotia.logica(con, entrada);
			salida.P_OUT_ERRORESCONTADOR += resp.P_OUT_ERRORESCONTADOR;

		} catch (Exception e) {
			salida.P_OUT_ERRORESCONTADOR += 1;
			logger.error(e.getMessage());
			logger.info("***** Error PautaGenerarInterfazP40SBIF_Scotia *****");
    		throw e;
		}finally {
			if(salida.P_OUT_ERRORESCONTADOR == 0) {				
				salida.P_OUT_MENSAJE = "El proceso PautaGenerarInterfazP40SBIF_Scotia se ha ejecutado correctamente";
			}else {
				salida.P_OUT_MENSAJE = "El proceso PautaGenerarInterfazP40SBIF_Scotia ha finalizado con errores";
			}
			logger.info(salida.P_OUT_MENSAJE);
			logger.info("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			logger.info("***** Fin PautaGenerarInterfazP40SBIF_Scotia *****\n");
		}
		return salida;
	};
	

	public static void main(String[] args) throws Exception {
		try (Connection con =  PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) {
			Map<String,Object> params = new HashMap<>();
			params.put("IdEmpresa",1L);
			 params.put("IdUsuario",1L);
			 params.put("FechaSistema", "2024-11-13");
			PautaGenerarInterfazP40SBIF_Scotia pgn = new PautaGenerarInterfazP40SBIF_Scotia();
			try {
				pgn.transaction(con, params);
//				con.commit();
				con.rollback();
			} catch (Throwable e) {
				e.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}	

	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		logger.info("Inicia ejecucion del job");
		SQL sql = new SQL();
		try {
			HashMap<String, Object> paramsRut = new HashMap<String, Object>();
			paramsRut.put("IdEmpresa", Long.parseLong(this.getParam("IdEmpresa")));
			List<DatoCodigoCorredora> datoCodigo = sql.query(con, SQL_CODIGO_CORREDORA, paramsRut, DatoCodigoCorredora.class);
			
			String fechaSistema = this.getParam("FechaEjecucion") == null ? "" : this.getParam("FechaEjecucion");
			if(fechaSistema.isEmpty()) {
				fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,
						Long.parseLong(this.getParam("IdEmpresa")), "PLATAFORMA", "PLA.FechaSistema");
			}
			EntradaGenerarInterfazP40SBIF entrada = new EntradaGenerarInterfazP40SBIF();
			String rutaArchivo = Utils.getConfiguracion(con, Utils.getSchema(), Long.parseLong(this.getParam("IdEmpresa")), "PLA.RutaBaseArchivos");
			SUBCARPETA = ConfiguracionSistema.getParametroConfiguracionString(con,Long.parseLong(this.getParam("IdEmpresa")),"NORMATIVA","SubRutaArchivosModulo"); 
			//Se configura una subcarpeta para alojar los archivos.
			rutaArchivo = rutaArchivo.concat(SUBCARPETA);
			
			//Nota: la clase ClienteConfig configura a la entradap40 según el código de corredora que se envía, no se necesitan todos los parametros de entrada
			ClienteConfig clienteConfig = ClienteConfigFactory.getClienteConfig(datoCodigo.get(0).CodigoCorredora);
			clienteConfig.configurarParametros(entrada, this.getParam("IdEmpresa"), this.getParam("IdUsuario"), rutaArchivo);			
    		entrada.p_Fecha = fechaSistema;
			GenerarInterfazP40SBIF_Scotia generarInterfazP40SBIF_Scotia = new GenerarInterfazP40SBIF_Scotia();
			SalidaGenerarInterfazP40SBIF resp = generarInterfazP40SBIF_Scotia.logica(con, entrada);
			if(resp.P_OUT_ERRORESCONTADOR > 0) {
				logger.error("El proceso PautaGenerarInterfazP40SBIF_Scotia ha finalizado con errores");
				return false;
			}
			logger.info("El proceso PautaGenerarInterfazP40SBIF_Scotia se ha ejecutado correctamente");
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage());
    		return false;
		}
	}
	
	@Override
    public void onError(Throwable t) {
        logger.error(t);
    }

	@Override
	public void pause() {

	}
	
}
