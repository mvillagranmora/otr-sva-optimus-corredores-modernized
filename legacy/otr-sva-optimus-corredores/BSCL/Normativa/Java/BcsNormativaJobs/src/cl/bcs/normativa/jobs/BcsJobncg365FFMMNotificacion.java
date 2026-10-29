package cl.bcs.normativa.jobs;
  

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.text.SimpleDateFormat;
import java.util.Date;

import cl.bcs.normativa.circulares.NotificarNCG365FFMM;
import cl.bcs.normativa.circulares.NotificarNCG365FFMM.NotificarNCG365FFMM_Entrada;
import cl.bcs.plataforma.pau.AgregarControlPauta;
import cl.bcs.plataforma.pau.AgregarControlPauta.AgregarControlPautaEntrada;

import javax.sql.DataSource;

import cl.bcs.scheduler.BcsJob;

public class BcsJobncg365FFMMNotificacion extends BcsJob{
	
	AtomicBoolean running=new AtomicBoolean(false); 
	private Long idTrabajoLog = new Long(0);



	public boolean transaction(Connection dbcon) throws InterruptedException, Throwable {
    	running.set(true);
        DataSource datasource = super.getDataSource();
    	//String confFechaSistema = "2019-01-01";//ConfiguracionSistema.getParametroConfiguracionString(con,parIdEmpresa,"PLATAFORMA","PLA.FechaSistema");

    	//Fecha actual
    	SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
    	java.util.Date fecha = new Date();
    	String confFechaSistema =format1.format(fecha); 
		
		String ejecucion = "PROGRAMADA";
		
        Connection dbConnection = null;
		PreparedStatement pstmt = null;
	
		//final String callSp = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::NOR.ProcesarTareaPauta\"(?, ?, ?)";
		final String IdTipoTarea = this.getParam("IdTipoTarea");			
		try {
			dbConnection = datasource.getConnection();
			dbConnection.setAutoCommit(false);
			final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
						
			logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"PEN","Notificar NCG 365: Inicio del proceso");
			
			Integer numColumnas = 0;
			long total = System.currentTimeMillis();
        	try{
    			long milis = System.currentTimeMillis();
				
				logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"PEN","Notificar NCG 365:En Ejecución");

        		NotificarNCG365FFMM sp = new NotificarNCG365FFMM();
    			NotificarNCG365FFMM_Entrada in = new NotificarNCG365FFMM_Entrada();
    			in.p_IdEmpresa = idEmpresa;
    			in.p_IdModulo = "NOR";
    			in.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));//Long.parseLong("6");//
    			in.p_FechaCertificacion = (this.getParam("FechaCertificacion") != null ? this.getParam("FechaCertificacion") : "0001-01-01");
    			in.p_EsAgenda = (this.getParam("EsAgenda") != null ? this.getParam("EsAgenda") : "N");
    			
    			sp.logica(dbConnection, in);
        		
        		logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"TER","Notificar NCG 365: finalizado con éxito");
        	}catch(Exception ex){
				logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"ERR","Excepción al Ejecutar NCG 365 FFMM IdEmpresa: "+idEmpresa+" IdTipoTarea: " +IdTipoTarea);
        		System.out.println("BcsJobProcesarPautaServ: EXCEPTION AL EJECUTAR: Notificar NCG 365");
        		ex.printStackTrace();
        	}
        	
        	
		} catch (Exception e) {
			//logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"ERR","Error de Conexión");

			System.out.println("BcsJobProcesarPautaServ(): ERROR AL OBTENER DB CONNECTION");
			System.out.println(e.getMessage());
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (dbConnection != null) {
					dbConnection.commit();
					dbConnection.close();
				}
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
        System.out.println(":BcsJobProcesarPautaServ:  ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
}
