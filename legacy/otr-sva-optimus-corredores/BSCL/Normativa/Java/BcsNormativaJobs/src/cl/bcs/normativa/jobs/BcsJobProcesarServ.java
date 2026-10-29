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

import cl.bcs.plataforma.pau.AgregarControlPauta;
import cl.bcs.plataforma.pau.AgregarControlPauta.AgregarControlPautaEntrada;

import javax.sql.DataSource;

import cl.bcs.scheduler.BcsJob;

public class BcsJobProcesarServ extends BcsJob{
	
	AtomicBoolean running=new AtomicBoolean(false); 
	private Long idTrabajoLog = new Long(0);



	public boolean transaction(Connection dbcon) throws InterruptedException, Throwable {
    	running.set(true);
        DataSource datasource = super.getDataSource();
    	String confFechaSistema = "2019-01-01";//ConfiguracionSistema.getParametroConfiguracionString(con,parIdEmpresa,"PLATAFORMA","PLA.FechaSistema");

		String ejecucion = "PROGRAMADA";
		
        Connection dbConnection = null;
		PreparedStatement pstmt = null;
		CallableStatement sp = null;
	
		final String callSp = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::NOR.ProcesarTarea\"(?, ?, ?, ?)";
		final String IdTipoTarea = this.getParam("IdTipoTarea");			
		try {
			dbConnection = datasource.getConnection();
			final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			final long IdSolicitudCircular = Long.parseLong(this.getParam("IdSolicitudCircular"));
			
			logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"PEN","Inicio del proceso");
			
			Integer numColumnas = 0;
			long total = System.currentTimeMillis();
        	try{
    			sp = dbConnection.prepareCall(callSp);
        		sp.setLong(1, idEmpresa);
        		sp.setString(2, IdTipoTarea);
        		sp.setLong(3, IdSolicitudCircular);
        		sp.setLong(4, getJobId());
        		long milis = System.currentTimeMillis();
				
				logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"PEN","En Ejecución");

        		sp.execute();
        		dbConnection.commit();
        		logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"TER","Proceso finalizado con éxito");
        	}catch(Exception ex){
				logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"ERR","Excepción al Ejecutar Procedimiento, revisar SP ,conexión y parámetros base IdEmpresa: "+idEmpresa+" IdTipoTarea: " +IdTipoTarea+" IdSolicitudCircular: " +IdSolicitudCircular);
        		System.out.println("BcsJobProcesarServ: EXCEPTION AL EJECUTAR: NOR.ProcesarTarea");
        		ex.printStackTrace();
        	}
        	
        	
		} catch (Exception e) {
			//logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"ERR","Error de Conexión");

			System.out.println("BcsJobProcesarServ(): ERROR AL OBTENER DB CONNECTION");
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
        System.out.println(":BcsJobProcesarServ:  ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
}
