package cl.bcs.normativa.jobs;
  

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cl.bcs.normativa.AgendaGenerarNCG365Masivo;
import cl.bcs.normativa.AgendaGenerarNCG365Masivo.SalidaJobNCG365MasivoAgenda;
import cl.bcs.plataforma.pau.AgregarControlPauta;
import cl.bcs.plataforma.pau.AgregarControlPauta.AgregarControlPautaEntrada;

import javax.sql.DataSource;

import cl.bcs.scheduler.BcsJob;

public class BcsJobncg365FFMMMasivoAgenda extends BcsJob{
	
	AtomicBoolean running=new AtomicBoolean(false); 
	private Long idTrabajoLog = new Long(0);



	public boolean transaction(Connection dbcon) throws InterruptedException, Throwable {
    	running.set(true);
        DataSource datasource = super.getDataSource();
    	

    	//Fecha actual
    	SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
    	java.util.Date fecha = new Date();
    	String confFechaSistema =format1.format(fecha); 
		
		String ejecucion = "PROGRAMADA";
		
        Connection dbConnection = null;
		PreparedStatement pstmt = null;
	
		
		final String IdTipoTarea = this.getParam("IdTipoTarea");			
		try {
			dbConnection = datasource.getConnection();
			final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
						
			logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"PEN","Generar NCG 365: Inicio del proceso");
			
			Integer numColumnas = 0;
			long total = System.currentTimeMillis();
        	try{
    			long milis = System.currentTimeMillis();
				
				logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"PEN","Generar NCG 365:En Ejecución");

        		
				AgendaGenerarNCG365Masivo sp = new AgendaGenerarNCG365Masivo();
				SalidaJobNCG365MasivoAgenda salida = new SalidaJobNCG365MasivoAgenda();
				
    			
    			 Map<String, Object> in = new HashMap<>();
    			 
    			 in.put("IdEmpresa", 1L);
    			 in.put("IdUsuario", 0L);

    		        
    		        salida = sp.transaction(dbConnection, in);
    		        
    		        if(salida.P_OUT_ERRORESCONTADOR == 0) {
    		        	dbConnection.commit();
    		        }
	    			
    		        else {
    		        	dbConnection.rollback();
    		        }
	
        		logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"TER","Generar NCG 365: finalizado con éxito");
        	}catch(Exception ex){
				logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"ERR","Excepción al Ejecutar NCG 365 FFMM IdEmpresa: "+idEmpresa+" IdTipoTarea: " +IdTipoTarea);
        		System.out.println("BcsJobNCG365MasivoAgenda: EXCEPTION AL EJECUTAR: Generer NCG 365");
        		ex.printStackTrace();
        	}
        	
        	
		} catch (Exception e) {
			

			System.out.println("BcsJobNCG365MasivoAgenda(): ERROR AL OBTENER DB CONNECTION");
			System.out.println(e.getMessage());
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (dbConnection != null)
				
				

					dbConnection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		running.set(false);
		return true;
	}
	
	
	   public void logPauta(Connection con, Long idPrograma, String idTipoPrograma, String ejecucion, String fechaSistema, String estado, String mensaje) throws Exception{
    	idTrabajoLog++;
    	AgregarControlPauta cnpt = new AgregarControlPauta();
    	AgregarControlPauta.AgregarControlPautaEntrada cnptIn = new AgregarControlPauta.AgregarControlPautaEntrada();
    	cnptIn.p_IdTrabajo = idTrabajoLog;
		List<AgregarControlPautaEntrada.Registro> registros = new ArrayList<AgregarControlPautaEntrada.Registro>();
		AgregarControlPautaEntrada.Registro item = new AgregarControlPautaEntrada.Registro();
		item.IdTrabajo = idTrabajoLog;
		item.IdPrograma = idPrograma;
		item.IdTipoPrograma = idTipoPrograma;
		item.Ejecucion = ejecucion; 
		item.FechaSistema = fechaSistema;
		item.IdEstado = estado;	// PEN, TER, ERR
		item.Mensaje = mensaje;	
		registros.add(item);	
		cnptIn.p_Registros = registros;
		cnpt.logica(con, cnptIn);
    }
	
	
	@Override
    public void onError(Throwable t) {
        System.out.println(":BcsJobNCG365MasivoAgenda:  ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
}
