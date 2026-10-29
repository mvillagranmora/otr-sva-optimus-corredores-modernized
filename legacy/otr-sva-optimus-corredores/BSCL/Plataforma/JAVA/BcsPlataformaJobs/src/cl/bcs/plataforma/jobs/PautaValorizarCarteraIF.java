package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import cl.bcs.plataforma.pau.ValorizarCarteraIF;
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
public class PautaValorizarCarteraIF implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaValorizarCarteraIF"); 
	
	private static final String CODIGO_PROCESO = "VALORIZAR_CARTERA_IF";
	private static final String MERCADO = "IF";
	
    @Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaValorizarCarteraIF *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	LogValorizadorPauta logValorizadorPauta = new LogValorizadorPauta();
		Long idLogValorizadorPauta = null;
    	try {
	    	String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
	    	
	    	Long idEjecucion = logValorizadorPauta.obtenerIdEjecucionPauta(con,	(Long) parameters.get("IdEmpresa"),	confFechaSistema, CODIGO_PROCESO);
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			logValorizadorPauta.limpiarDetalleLogPautaAntiguo(con, confFechaSistema, CODIGO_PROCESO);
			idLogValorizadorPauta = logValorizadorPauta.crearCabeceraLogPauta(con, idEjecucion, CODIGO_PROCESO,	MERCADO);
			
			ValorizarCarteraIF.ValorizarCarteraIFEntrada valorizarCarteraParametros = new ValorizarCarteraIF.ValorizarCarteraIFEntrada();
	    	valorizarCarteraParametros.p_FechaSistema = confFechaSistema;
			valorizarCarteraParametros.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			valorizarCarteraParametros.p_IdPais = empresa.IdPais;
			valorizarCarteraParametros.p_IdUsuario = (Long)parameters.get("IdUsuario");
			valorizarCarteraParametros.p_SimularOperacion = "N";
			valorizarCarteraParametros.p_EsPauta = "S";
			valorizarCarteraParametros.p_IdLogValorizadorPauta = idLogValorizadorPauta;
			
			ValorizarCarteraIF valorizarCartera = new ValorizarCarteraIF();
			ValorizarCarteraIF.ValorizarCarteraIFSalida valorizarCarteraRFIFSalidaOut = new ValorizarCarteraIF.ValorizarCarteraIFSalida(); 
			
			valorizarCarteraRFIFSalidaOut = valorizarCartera.logica(con, valorizarCarteraParametros);
			salida.P_OUT_MENSAJE = valorizarCarteraRFIFSalidaOut.P_OUT_MENSAJE;
			if (!Long.valueOf(0L).equals(valorizarCarteraRFIFSalidaOut.P_OUT_ERRORESCONTADOR) || (salida.P_OUT_MENSAJE != null && salida.P_OUT_MENSAJE.toLowerCase().contains("error"))) {
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
    		log.write("***** Error PautaValorizarCarteraIF *****");
    		throw e;
    	} finally {
			try {
				logValorizadorPauta.cerrarCabeceraLogPauta(con, idLogValorizadorPauta);
			} catch (Exception e) {
				log.write("Error cerrando cabecera LogValorizadorPauta");
				log.write(e);
			}
			log.write("***** Fin PautaValorizarCarteraIF *****");
		}
		return salida;
	}
    
    public static void main(String[] args) throws Throwable {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")){
			PautaValorizarCarteraIF ipb = new PautaValorizarCarteraIF();
			
		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put("IdEmpresa",1L);
		    params.put("IdUsuario",0L);
			ipb.transaction(con,params );
		    con.commit();
			//con.rollback();
		  }
		catch(Exception e) {
			e.printStackTrace();
		}
		  
	}
    
}