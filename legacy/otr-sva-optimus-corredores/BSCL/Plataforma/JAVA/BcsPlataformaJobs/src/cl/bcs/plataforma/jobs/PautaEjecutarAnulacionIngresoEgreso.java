package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.tesoreria.EjecutarAnulacionComprobantes;
import cl.bcs.tesoreria.EjecutarAnulacionIngresoEgreso;
import cl.bcs.utils.Log;

/**********************************
 * @version 	1.0
 * @author 		
 **********************************/
public class PautaEjecutarAnulacionIngresoEgreso implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaEjecutarAnulacionIngresoEgreso");

	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaEjecutarAnulacionIngresoEgreso *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
	    	String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
	    	String anularComprobantes = ConfiguracionSistema.getParametroConfiguracionString(con, (Long)parameters.get("IdEmpresa"), "TESORERIA", "ANULARCOMPROBANTES");

	    	// Entra dependiendo del valor del parametro
	    	if ("S".equals(anularComprobantes)) {
	    		EjecutarAnulacionComprobantes ejeAnulacionComprobantes = new EjecutarAnulacionComprobantes();
		    	EjecutarAnulacionComprobantes.Entrada paramEjeAnulacionComprobantes = new EjecutarAnulacionComprobantes.Entrada();
		    	paramEjeAnulacionComprobantes.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
		    	paramEjeAnulacionComprobantes.p_IdUsuario = (Long)parameters.get("IdUsuario");
				paramEjeAnulacionComprobantes.p_FechaMovimiento = confFechaSistema;
				
				EjecutarAnulacionComprobantes.Salida outEjeAnuCom = ejeAnulacionComprobantes.logica(con, paramEjeAnulacionComprobantes);
				salida.P_OUT_MENSAJE = outEjeAnuCom.P_OUT_DESCRIPCION;
				salida.P_OUT_ERRORESCONTADOR = outEjeAnuCom.P_OUT_ERRORESCONTADOR;
	    	}
			
			EjecutarAnulacionIngresoEgreso ejeAnulacionIngresosEgresos = new EjecutarAnulacionIngresoEgreso();
			EjecutarAnulacionIngresoEgreso.Entrada paramEjeAnulacionIngresosEgresos = new EjecutarAnulacionIngresoEgreso.Entrada();
			paramEjeAnulacionIngresosEgresos.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			paramEjeAnulacionIngresosEgresos.p_IdUsuario =  (Long)parameters.get("IdUsuario");
			paramEjeAnulacionIngresosEgresos.p_IdModulo = "PLA";			
			paramEjeAnulacionIngresosEgresos.p_Fecha = confFechaSistema;
			paramEjeAnulacionIngresosEgresos.p_SimularOperacion = "N";
			
			EjecutarAnulacionIngresoEgreso.Salida outejevenc = ejeAnulacionIngresosEgresos.logica(con, paramEjeAnulacionIngresosEgresos);
			salida.P_OUT_MENSAJE = Objects.isNull(salida.P_OUT_MENSAJE)? "" : salida.P_OUT_MENSAJE + PlataformaUtils.concatenarMensajesSalida(outejevenc.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR += outejevenc.P_OUT_ERRORESCONTADOR;
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaEjecutarAnulacionIngresoEgreso *****");
    		throw e;
    	}
		log.write("***** Fin PautaEjecutarAnulacionIngresoEgreso *****");
		return salida;
    }
	
	//solo para test
	public static void main(String[] args) throws Throwable {
		System.out.println("== START ==");
		Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties");
		PautaEjecutarAnulacionIngresoEgreso apf = new PautaEjecutarAnulacionIngresoEgreso();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("IdEmpresa", 1L);
		parameters.put("IdUsuario", 1L);
		System.out.println(apf.transaction(con,parameters));	      
		//con.commit();
		con.rollback();
		System.out.println("== END ==");
	}    
}