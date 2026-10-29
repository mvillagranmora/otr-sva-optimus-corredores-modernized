package cl.bcs.cuentaInversion.jobs;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono;
import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono.ActualizarSaldoCargoAbonoEntrada;
import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono.ActualizarSaldoCargoAbonoSalida;
import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono.CuentaInversion;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ActualizarSaldoCargoAbonoJob extends BcsJob implements JavaCall {
	private final static Logger logger = LogManager.getLogger(ActualizarSaldoCargoAbonoJob.class);
	
	private final String SQL_UPDATE_TIPO_PROGRAMA_EJECUCION = loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldoCargoAbonoJob_Update_TipoProgramaEjecucion.sql");

	
	public static class ParamsTipoProgramaEjecucion_Update {
		public String p_IdTipoPrograma;
		public String p_IdEstado;
		
	}
	
	@Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
    	logger.debug("INICIO JOB");
    	ActualizarSaldoCargoAbono actualizarSaldoCargoAbono = new ActualizarSaldoCargoAbono();
	    ActualizarSaldoCargoAbonoEntrada entradaActualizarSaldoCargoAbonoEntrada = new ActualizarSaldoCargoAbonoEntrada(); 

		SQL sql = new SQL();
		
        Long 	idEmpresa;
        Long 	idUsuario;
    	String 	fechaSistema;
	    String 	cuentasInversion;  	    
	    
	    //inserta/actualizar tabla ejecucion	    
	    ParamsTipoProgramaEjecucion_Update paramsTipoProgramaEjecucion =new ParamsTipoProgramaEjecucion_Update();
	    paramsTipoProgramaEjecucion.p_IdTipoPrograma = "ACTUALIZAR_SALDO_CARGO_ABONO";
	    paramsTipoProgramaEjecucion.p_IdEstado = "EJEC";
	    
		sql.batch(con, SQL_UPDATE_TIPO_PROGRAMA_EJECUCION,Arrays.asList(paramsTipoProgramaEjecucion),ParamsTipoProgramaEjecucion_Update.class);
		

		try{
	    
        if (debug) 
        {
        	idEmpresa 					= 1L;       
        	idUsuario					= 21L; 
        	fechaSistema 				= "2020-01-10";
        	cuentasInversion 			= "3205988"; 		    
        }
        else
        {
         	idEmpresa					= Long.parseLong(this.getParam("IdEmpresa"));
        	idUsuario					= Long.parseLong(this.getParam("IdUsuario"));
        	fechaSistema 				= this.getParam("FechaSistema");
        	cuentasInversion 			= this.getParam("CuentasInversion"); 	
        }
        //decodificar la cuenta de b64
       // String cuentasInversionDecoded =  Base64.getDecoder().decode(cuentasInversion.getBytes()).toString();
		
        String str[] = cuentasInversion.split(";");
    	List<String> listCuentasInversion = new ArrayList<String>();
    	listCuentasInversion = Arrays.asList(str);
    	
    	List<CuentaInversion> listaCuentasInversionEntrada = new ArrayList<CuentaInversion>();
    	
		for (String cuentaInversion : listCuentasInversion){
			CuentaInversion cuenta_inversion = new CuentaInversion();
			cuenta_inversion.IdCuentaInversion = Long.parseLong(cuentaInversion);
			listaCuentasInversionEntrada.add(cuenta_inversion);
		}

        entradaActualizarSaldoCargoAbonoEntrada.p_IdEmpresa = idEmpresa;
	    entradaActualizarSaldoCargoAbonoEntrada.p_IdUsuario = idUsuario;
	    entradaActualizarSaldoCargoAbonoEntrada.p_FechaMovimiento = fechaSistema;  
	    entradaActualizarSaldoCargoAbonoEntrada.p_CuentaInversion.addAll(listaCuentasInversionEntrada);
	    
	    
			ActualizarSaldoCargoAbonoSalida salida = new ActualizarSaldoCargoAbonoSalida();
			salida = actualizarSaldoCargoAbono.logica(con, entradaActualizarSaldoCargoAbonoEntrada);	
			
			if (salida.P_OUT_ERRORESCONTADOR.equals(0L)) {
				con.commit();
				return true;
			}
			else {
				con.rollback();
				return false;
			}
		
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();
			throw e;
		}
		finally {
			paramsTipoProgramaEjecucion =new ParamsTipoProgramaEjecucion_Update();
		    paramsTipoProgramaEjecucion.p_IdTipoPrograma = "ACTUALIZAR_SALDO_CARGO_ABONO";
		    paramsTipoProgramaEjecucion.p_IdEstado = "";
		    
			sql.batch(con, SQL_UPDATE_TIPO_PROGRAMA_EJECUCION,Arrays.asList(paramsTipoProgramaEjecucion),ParamsTipoProgramaEjecucion_Update.class);
			

			con.commit();
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
        	Connection con = PlataformaUtils.getDESAConnection();
        	ActualizarSaldoCargoAbonoJob demo = new ActualizarSaldoCargoAbonoJob();
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
