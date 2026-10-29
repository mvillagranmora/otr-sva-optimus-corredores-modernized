package cl.bcs.contabilidad.jobs;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.contabilidad.CentralizaMultiSeleccion;
import cl.bcs.contabilidad.CentralizaMultiSeleccion.EntradaCentralizaMultiSeleccion;
import cl.bcs.contabilidad.CentralizaMultiSeleccion.SalidaCentralizaMultiSeleccion;
import cl.bcs.contabilidad.CentralizaMultiSeleccion.InTiposCentralizacion;                  

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class CentralizaMultiSeleccionJob extends BcsJob implements JavaCall {
	private final static Logger logger = LogManager.getLogger(CentralizaMultiSeleccionJob.class);
	
	@Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
    	logger.debug("INICIO JOB");
    	CentralizaMultiSeleccion centralizaMultiSeleccion = new CentralizaMultiSeleccion();
    	EntradaCentralizaMultiSeleccion entradaCentralizaMultiSeleccion = new EntradaCentralizaMultiSeleccion(); 

        Long 	idEmpresa;
        Long 	idUsuario;
        String  idModulo;
        String  fechaContable;
        String  fechaConsulta;
        Long    ejercicio;
	    String  idsTipoCentralizacion;
	    
		try{
	    
        if (debug) 
        {
        	idEmpresa 					= 1L;       
        	idUsuario					= 21L;
        	idModulo                    = "CON";
        	fechaContable 				= "2026-05-05";
        	ejercicio                   = 2026L;
        	idsTipoCentralizacion       = "200;201;210";
        	fechaConsulta 				= "2026-05-05";
        }
        else
        {
         	idEmpresa					= Long.parseLong(this.getParam("IdEmpresa"));
        	idUsuario					= Long.parseLong(this.getParam("IdUsuario"));
        	idModulo                    = this.getParam("IdModulo");
        	fechaContable 				= this.getParam("FechaContable");
        	ejercicio                   = Long.parseLong(this.getParam("Ejercicio"));
        	idsTipoCentralizacion       = this.getParam("IdsTipoCentralizacion");
        	fechaConsulta 				= this.getParam("fechaConsulta");
        }
     
        String str[] = idsTipoCentralizacion.split(";");
        List<String> listTiposCentralizacion = new ArrayList<String>();
        listTiposCentralizacion = Arrays.asList(str);
    	
    	List<InTiposCentralizacion> listaTiposCentralizacionEntrada = new ArrayList<InTiposCentralizacion>();
    	
		for (String tipoCentralizacion : listTiposCentralizacion){
			InTiposCentralizacion tipo_centralizacion = new InTiposCentralizacion();
			tipo_centralizacion.IdTipoCentralizacion = Long.parseLong(tipoCentralizacion);
			listaTiposCentralizacionEntrada.add(tipo_centralizacion);
		}
		
		entradaCentralizaMultiSeleccion.p_IdEmpresa = idEmpresa;
		entradaCentralizaMultiSeleccion.p_IdUsuario = idUsuario;
		entradaCentralizaMultiSeleccion.p_IdModulo = idModulo;
		entradaCentralizaMultiSeleccion.p_FechaContable = fechaContable;
		entradaCentralizaMultiSeleccion.p_FechaConsulta = fechaConsulta;
		entradaCentralizaMultiSeleccion.p_Ejercicio = ejercicio;
		entradaCentralizaMultiSeleccion.p_in_TiposCentralizacion.addAll(listaTiposCentralizacionEntrada);
	    
		    SalidaCentralizaMultiSeleccion salida = new SalidaCentralizaMultiSeleccion();
			salida = centralizaMultiSeleccion.logica(con, entradaCentralizaMultiSeleccion);	
			
			con.commit();
			return true;
		
		
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();
			throw e;
		}
	
    }

    // @Override
    public void onError(Throwable t) {
    	logger.debug("****** ERROR  *******");
        logger.debug(t);
        t.printStackTrace();
    }

    // @Override
    public void pause() {

    }
    
    @Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

    
    public static void main(String[] args) throws IOException { 
    	
    	System.out.println("== INICIO PRUEBA ==");
    	
        try {
        	Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties");
        	CentralizaMultiSeleccionJob demo = new CentralizaMultiSeleccionJob();
        	demo.logica(con,true);
        
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("== FIN PRUEBA ==");
    }
}
