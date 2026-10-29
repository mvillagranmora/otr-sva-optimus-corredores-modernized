package cl.bcs.custodia.jobs;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import cl.bcs.custodia.ConsultarCarteraDerivadosNeteo;
import cl.bcs.custodia.ConsultarCarteraDerivadosNeteo.Entrada_ConsultarCarteraDerivados;
import cl.bcs.custodia.ConsultarCarteraDerivadosNeteo.Salida_ConsultarCustodiaDerivados;
import cl.bcs.plataforma.jobs.BcsProcesoPauta;
import cl.bcs.plataforma.jobs.BcsProcesoPauta.SalidaProcesoPauta;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;



public class DerivadosNeteo implements BcsProcesoPauta{
	
	private final Log log = new Log("CUS.DerivadosNeteo");

	//DEBUG
	public static void main(String[] args) {
		// Example
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			con.setAutoCommit(false);
			SQL.exec(con, "set schema BSCL");
			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				DerivadosNeteo derivadosNeteo = new DerivadosNeteo();
				HashMap<String, Object> params = new HashMap<String, Object>();
				derivadosNeteo.transaction(con, params);
				con.rollback();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println("== END ==");
	}

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		ConsultarCarteraDerivadosNeteo consultarDerivadosNeteo = new ConsultarCarteraDerivadosNeteo();
		Entrada_ConsultarCarteraDerivados entrada = new Entrada_ConsultarCarteraDerivados();	
		log.write("***** Inicio DerivadosNeteo *****");
		try {		
		
			entrada.p_IdEmpresa = 1L;		
			entrada.p_IdModulo = "CUS";	
		
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
			entrada.p_FechaConsulta = new SimpleDateFormat("yyyy-MM-dd").parse(fechaSistema);
		
			Salida_ConsultarCustodiaDerivados salidaDerivadosNeteo = consultarDerivadosNeteo.logica(con, entrada);
			salida.P_OUT_MENSAJE = "Se ha procesado correctamente";
			System.out.println(salidaDerivadosNeteo);
			System.out.println(salida);
		} catch (Exception e) {
    		log.write(e);
    		log.write("***** DerivadosNeteo *****");
    		throw e;
    	}
		log.write("***** Fin DerivadosNeteo *****");
		return salida;
	}

	

}