package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import cl.bcs.plataforma.pau.ValorizarGarantiasRVRFIF;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Log;
import cl.bcs.plataforma.pau.LogValorizadorPauta;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaValorizarGarantiasRVRFIF implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaValorizarGarantiasRVRFIF"); 
	
	private static final String CODIGO_PROCESO = "VALORIZAR_GARANTIAS_RV_RF_IF";
	private static final String MERCADO = "GARANTIAS_RV_RF_IF";
	
    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaValorizarGarantiasRVRFIF *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	LogValorizadorPauta logValorizadorPauta = new LogValorizadorPauta();
		Long idLogValorizadorPauta = null;
    	try {
	    	String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
	    	Long idEjecucion = logValorizadorPauta.obtenerIdEjecucionPauta(con,	(Long) parameters.get("IdEmpresa"), confFechaSistema, CODIGO_PROCESO);
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			logValorizadorPauta.limpiarDetalleLogPautaAntiguo(con, confFechaSistema, CODIGO_PROCESO);
			idLogValorizadorPauta = logValorizadorPauta.crearCabeceraLogPauta(con, idEjecucion, CODIGO_PROCESO, MERCADO);
			ValorizarGarantiasRVRFIF.ValorizarCarteraRVRFIFEntrada valorizarCarteraRVRFIFParametros = new ValorizarGarantiasRVRFIF.ValorizarCarteraRVRFIFEntrada(); 
	
			valorizarCarteraRVRFIFParametros.p_FechaSistema = confFechaSistema;
			valorizarCarteraRVRFIFParametros.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			valorizarCarteraRVRFIFParametros.p_IdPais = empresa.IdPais;
			valorizarCarteraRVRFIFParametros.p_IdUsuario = (Long)parameters.get("IdUsuario");
			valorizarCarteraRVRFIFParametros.p_SimularOperacion = "N";
			valorizarCarteraRVRFIFParametros.p_EsPauta = "S";
			valorizarCarteraRVRFIFParametros.p_IdLogValorizadorPauta = idLogValorizadorPauta;
			
			ValorizarGarantiasRVRFIF valorizarGarantiasRVRFIF = new ValorizarGarantiasRVRFIF();
			ValorizarGarantiasRVRFIF.ValorizarCarteraRVRFIFSalida ValorizarCarteraRVRFIFOut = new ValorizarGarantiasRVRFIF.ValorizarCarteraRVRFIFSalida();
			
			ValorizarCarteraRVRFIFOut = valorizarGarantiasRVRFIF.logica(con, valorizarCarteraRVRFIFParametros);
			salida.P_OUT_MENSAJE = ValorizarCarteraRVRFIFOut.P_OUT_MENSAJE;
			if (!Long.valueOf(0L).equals(ValorizarCarteraRVRFIFOut.P_OUT_ERRORESCONTADOR) || (salida.P_OUT_MENSAJE != null && salida.P_OUT_MENSAJE.toLowerCase().contains("error"))) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
			} else {
				salida.P_OUT_ERRORESCONTADOR = 0L;
			}
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			//log.write("Salida: ");
			//String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			//for (String mensaje: mensajes) log.write(mensaje);
			
			logValorizadorPauta.cerrarCabeceraLogPauta(con, idLogValorizadorPauta);

	    	boolean isDebug = false;
			if (isDebug) {
				String rutaLogDebug = "C:/temp/logs";
				logValorizadorPauta.generarArchivoLogValorizadorPauta(con, (Long) parameters.get("IdEmpresa"), idLogValorizadorPauta, rutaLogDebug);
			} else {
				logValorizadorPauta.generarArchivoLogValorizadorPauta(con, (Long) parameters.get("IdEmpresa"), idLogValorizadorPauta);
			}
			
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaValorizarGarantiasRVRFIF *****");
    		throw e;
    	} finally {
			try {
				logValorizadorPauta.cerrarCabeceraLogPauta(con, idLogValorizadorPauta);
			} catch (Exception e) {
				log.write("Error cerrando cabecera LogValorizadorPauta");
				log.write(e);
			}

			log.write("***** Fin PautaValorizarGarantiasRVRFIF *****");
		}

		return salida;
	}
    
    public static void main(String[] args) throws Throwable {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")){
			PautaValorizarGarantiasRVRFIF ipb = new PautaValorizarGarantiasRVRFIF();
			
		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put("IdEmpresa",1L);
		    params.put("IdUsuario",0L);
			ipb.transaction(con,params );
		    //con.rollback();
		    con.commit();
		  }
		catch(Exception e) {
			e.printStackTrace();
		}
		  
	}

    
}