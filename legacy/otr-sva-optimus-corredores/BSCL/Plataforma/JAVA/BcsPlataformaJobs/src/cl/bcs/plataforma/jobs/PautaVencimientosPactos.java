package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;

import cl.bcs.custodia.EjecutarVencimiento;
import cl.bcs.mesa.MRF.EjecutarVencimientoPactos;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaVencimientosPactos implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaVencimientosPactos");

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaVencimientosPactos *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
	    	String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
	    	
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			EjecutarVencimientoPactos ejevenc = new EjecutarVencimientoPactos();
			EjecutarVencimientoPactos.Entrada inejevenc = new EjecutarVencimientoPactos.Entrada();
			inejevenc.p_IdEmpresa = (Long)parameters.get("IdEmpresa");;
			inejevenc.p_IdModulo = "PLA";
			inejevenc.p_IdUsuario =  (Long)parameters.get("IdUsuario");;
			inejevenc.p_Fecha = confFechaSistema;
			inejevenc.p_IdPais = empresa.IdPais;
			inejevenc.p_SimularOperacion = "N";
			inejevenc.p_IdTipoVencimiento = "PAC";
			
			EjecutarVencimientoPactos.Salida outejevenc = ejevenc.logica(con, inejevenc);
			salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(outejevenc.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR = outejevenc.P_OUT_ERRORESCONTADOR;
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			//log.write("Salida: ");
			//String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			//for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaVencimientosPactos *****");
    		throw e;
    	}
		log.write("***** Fin PautaVencimientosPactos *****");
		return salida;
    }

   
}