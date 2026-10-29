package cl.bcs.custodia.jobs;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.configuration.ConfigurationException;

import cl.bcs.custodia.ProcesaCargaMasivaCustodia;
import cl.bcs.custodia.ProcesaCargaMasivaCustodia.ProcesaCargaMasivaCustodiaSalida;
import cl.bcs.custodia.ProcesaCargaMasivaCustodia.ProcesarCargaMasivaCustodiaEntrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;

public class BcsJobProcesaCargaMasivaCustodia extends BcsJob {

    private final Log log = new Log("CUS.BcsJobProcesaCargaMasivaCustodia");
    
    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con, false);
	}
    
    public boolean logica(Connection con, boolean debug) throws InterruptedException, Throwable {
        log.write("INICIO JOB BcsJobProcesaCargaMasivaCustodia");
       
        try {
        	Long cargaMasivaProcesar;
        	Long idEmpresa;
            Long idUsuario;
        	String fechaProceso;
            String usaCarterasSistema;
            
        	if (!debug) {
        		cargaMasivaProcesar = Long.parseLong(this.getParam("CargaMasivaProcesar"));
        		idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
                idUsuario = Long.parseLong(this.getParam("IdUsuario"));
                fechaProceso = this.getParam("FechaProceso");
                usaCarterasSistema = this.getParam("UsaCarterasSistema");
        	} else {
                cargaMasivaProcesar = 2681L;
        		idEmpresa = 1L;
                idUsuario = 97L;
                fechaProceso = "2025-10-16";
                usaCarterasSistema = "S";
        	}
            
        	ProcesaCargaMasivaCustodia procesaCargaMasivaCustodia = new ProcesaCargaMasivaCustodia();
        	ProcesarCargaMasivaCustodiaEntrada entradaProcesaCargaMasivaCustodia = new ProcesarCargaMasivaCustodiaEntrada();
        	
        	log.write( "*PROCESA_CARGA_MASIVA_CUSTODIA* Inicio de llamada a ProcesaCargaMasivaCustodia");
        	
        	entradaProcesaCargaMasivaCustodia.p_IdEmpresa = idEmpresa;
        	entradaProcesaCargaMasivaCustodia.p_IdUsuario = idUsuario;
        	entradaProcesaCargaMasivaCustodia.p_IdModulo = "CUS";
        	entradaProcesaCargaMasivaCustodia.p_UsaReglasNegocio = "S";
        	entradaProcesaCargaMasivaCustodia.p_FechaProceso = fechaProceso;
        	entradaProcesaCargaMasivaCustodia.p_CargaMasivaProcesar = cargaMasivaProcesar;
        	entradaProcesaCargaMasivaCustodia.p_UsaCarterasSistema = usaCarterasSistema;
        	
        	ProcesaCargaMasivaCustodiaSalida salidaProcesaCargaMasivaCustodia = procesaCargaMasivaCustodia.logica(con, entradaProcesaCargaMasivaCustodia);

        	log.write( "*PROCESA_CARGA_MASIVA_CUSTODIA* Fin de llamada a ProcesaCargaMasivaCustodia");
        	
        	if (salidaProcesaCargaMasivaCustodia.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
        		return true;
        	} else {
        		String error = "";
        		
				for (MensajeSalida s : salidaProcesaCargaMasivaCustodia.P_OUT_MENSAJES) {
					if (MensajeSalida.MENSAJE_ERROR.equals(s.tipo)) {
						error = s.descripcion;
					}
				}
				
				if (!error.isEmpty()) {
					throw new Exception(error);
				} else {
					throw new Exception("Error al procesar carga masiva de custodia");
				}
        	}       	
        } catch (Exception e) {
            log.write("Error", e);
            throw e;
        } finally {
        	log.write("FIN JOB BcsJobProcesaCargaMasivaCustodia");
        }
    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public void pause() {
    }

    public static void main(String[] args) throws SQLException, ConfigurationException {
    	Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties");
    	BcsJobProcesaCargaMasivaCustodia c = new BcsJobProcesaCargaMasivaCustodia();
        try {
            c.logica(con, true);
            //con.commit();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }
}
