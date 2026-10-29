package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import cl.bcs.custodia.ValorizarRespaldoCustodia;
import cl.bcs.custodia.RespaldarCustodia;
import cl.bcs.custodia.RespaldarCustodia.Salida;
import cl.bcs.plataforma.jobs.BcsProcesoPauta.SalidaProcesoPauta;
import cl.bcs.plataforma.pau.ValorizarCarteraPactos;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaRespaldarCustodia implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaRespaldarCustodia");

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaRespaldarCustodia *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
	    	String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
	    	
            ValorizarRespaldoCustodia valorizarRespaldo = new ValorizarRespaldoCustodia();
            ValorizarRespaldoCustodia.Entrada params = new ValorizarRespaldoCustodia.Entrada();
            params.p_FechaSistema = confFechaSistema;
            params.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
            params.p_IdPais = empresa.IdPais;
            params.p_IdUsuario = (Long)parameters.get("IdUsuario");
            params.p_SimularOperacion = "N";
            ValorizarRespaldoCustodia.Salida valorizarRespaldoSalida = new ValorizarRespaldoCustodia.Salida();
            valorizarRespaldoSalida = valorizarRespaldo.logica(con, params);
            
			RespaldarCustodia respaldarCustodia = new RespaldarCustodia();
			RespaldarCustodia.Entrada entradaRespaldar = new RespaldarCustodia.Entrada();
			entradaRespaldar.p_FechaRespaldo = confFechaSistema;
			System.out.println("entradaRespaldar.p_FechaRespaldo" + entradaRespaldar.p_FechaRespaldo);
			Salida salidaRespaldar = respaldarCustodia.logica(con, entradaRespaldar);

			if (salidaRespaldar.P_OUT_ERRORESCONTADOR == 0) {
				salida.P_OUT_MENSAJE = "Se han respaldado: \nRegistros de tabla CARTERA: " + salidaRespaldar.P_OUT_COUNTCARTERAS + "\nRegistros de tabla SUBYACENTE: " + salidaRespaldar.P_OUT_COUNTSUBYACENTES 
						+ "\nRegistros de tabla RESERVA: " + salidaRespaldar.P_OUT_COUNTRESERVAS + "\nRegistros de tabla GARANTIA: " + salidaRespaldar.P_OUT_COUNTGARANTIAS + "\n"+ valorizarRespaldoSalida.P_OUT_MENSAJE;
			} else {
				salida.P_OUT_MENSAJE = "Ha ocurrido un error al respaldar Custodia";
			}

			salida.P_OUT_ERRORESCONTADOR = salidaRespaldar.P_OUT_ERRORESCONTADOR;
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			log.write("Salida: ");
			String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			for (String mensaje: mensajes) log.write(mensaje);

    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaRespaldarCustodia *****");
    		throw e;
    	}
		log.write("***** Fin PautaRespaldarCustodia *****");
		return salida;
    		 
    }
    
    public static void main(String[] args) throws Throwable {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("fynsa","connections.properties")) {  
			Map<String, Object> parameters = new HashMap<>();
	        parameters.put("IdEmpresa", 1L);
	        parameters.put("IdUsuario", 4L);
	        
	        try {
	        	PautaRespaldarCustodia pauta = new PautaRespaldarCustodia();
		        SalidaProcesoPauta salida = pauta.transaction(con, parameters);

		        System.out.println("P_OUT_MENSAJE: " + salida.P_OUT_MENSAJE);
		        System.out.println("P_OUT_ERRORESCONTADOR: " + salida.P_OUT_ERRORESCONTADOR);
		        con.commit();
		        
	        } catch (Exception er) {
	        	er.printStackTrace();
	        	con.rollback();
	        }
		}
		
        System.out.println("== END ==");
	}

    
}