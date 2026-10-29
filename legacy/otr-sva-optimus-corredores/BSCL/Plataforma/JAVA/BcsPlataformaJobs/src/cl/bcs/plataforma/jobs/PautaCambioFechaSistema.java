package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cl.bcs.plataforma.pla.CambiarFechaSistema;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pau.utils.PautaUtils;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		jgarces
 **********************************/
public class PautaCambioFechaSistema implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaCambioFechaSistema"); 

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
    	log.write("***** Inicio PautaCambioFechaSistema *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
	    	PautaUtils pautaUtil = new PautaUtils();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
	        String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
			ObtenerDiaSiguiente dsig = new ObtenerDiaSiguiente();
			ObtenerDiaSiguiente.InObtenerDiaSiguiente dsigin = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
			//validacion si es fin de mes 
			String esFinMesInhabil = pautaUtil.ejecutarDiaInhabil(con,confFechaSistema,(Long)parameters.get("IdEmpresa"))?"S":"N";						
			dsigin.p_idPais = empresa.IdPais;
			dsigin.p_diaHabil = esFinMesInhabil;
			dsigin.p_soloCaeDiaHabil = "S";
			
			ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn itemFec = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
			itemFec.fechaInicial = confFechaSistema;
			itemFec.numeroDias = 1L;
			dsigin.p_fechas.add(itemFec);
			ObtenerDiaSiguiente.OutObtenerDiaSiguiente dsigout = dsig.logica(con, dsigin);
			
			String fechaSistemaNueva = dsigout.p_out_fechas.get(0).fechaCalculada;			
			if ("S".equals(esFinMesInhabil)) {
				fechaSistemaNueva=pautaUtil.obtenerUltimoDiaDelMesString(confFechaSistema);
			}else {
				fechaSistemaNueva = dsigout.p_out_fechas.get(0).fechaCalculada;
			}
			
			log.write("Datos obtenidos para cambio fecha=> fecha actual: "+confFechaSistema+", fecha siguiente: "+fechaSistemaNueva);
			CambiarFechaSistema cf = new CambiarFechaSistema();
			CambiarFechaSistema.CambiarFechaSistemaEntrada cfin = new CambiarFechaSistema.CambiarFechaSistemaEntrada();
			cfin.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			cfin.p_FechaSistema = fechaSistemaNueva;
			cfin.p_FechaSistemaActual = confFechaSistema;
			log.write("Ejecuta el cambio de fecha");
			CambiarFechaSistema.CambiarFechaSistemaSalida cfout = cf.logica(con, cfin);
			ConfiguracionSistema.limpiarParametros();
			if (cfout.P_OUT_ERRORESCONTADOR == 0) {
				if ("S".equals(esFinMesInhabil)) {
					salida.P_OUT_MENSAJE = "El proceso CambioFechaSistema ha cambiado al último día inhábil del mes "+fechaSistemaNueva+", la fecha cambiara nuevamente una vez terminado el proceso de día inhábil";
					
				}else {					
					salida.P_OUT_MENSAJE = "El proceso CambioFechaSistema se ha ejecutado correctamente "+fechaSistemaNueva;
				}
				
				
			} else {
				salida.P_OUT_MENSAJE = "Ha ocurrido un error al ejecutar el proceso CambioFechaSistema";
			}
	    	salida.P_OUT_ERRORESCONTADOR = cfout.P_OUT_ERRORESCONTADOR;
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			log.write("Salida: ");
	    	String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaCambioFechaSistema *****");
    		throw e;
    	}
    	log.write("***** Fin PautaCambioFechaSistema *****");
		return salida;
    }
    
    public static void main(String [] args) throws Throwable {
    	PautaCambioFechaSistema cambio = new PautaCambioFechaSistema();
		Long offset = -10800L;
		//System.out.println("obtenerFechaHora:" + pauta.obtenerFechaHora("Fecha",offset));
		//System.out.println("obtenerFechaHora:" + pauta.obtenerFechaHora("Hora",offset));
		//System.out.println("obtenerFechaHora:" + pauta.obtenerFechaHora("Ambos",offset));
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa02","c:/archivos/connections.properties")) {
			
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("IdEmpresa", 1L);
			
			cambio.transaction(conn, parameters);
			conn.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}