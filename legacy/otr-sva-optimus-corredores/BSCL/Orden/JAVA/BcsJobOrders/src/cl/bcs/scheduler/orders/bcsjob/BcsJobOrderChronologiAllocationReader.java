package cl.bcs.scheduler.orders.bcsjob;
 
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.scheduler.orders.pojo.ParametrosDefault;
import cl.bcs.scheduler.orders.util.connectorsorders.ConnectorsOrders;
 

public class BcsJobOrderChronologiAllocationReader extends BcsJob {

	private ConnectorsOrders conn = new ConnectorsOrders();
	private DataSource datasource ;
	public Connection con;
 	public Connection getConnection() {
		return con;
	}
	public void setConnection(Connection con) {
		this.con = con;
	}
	public DataSource getDatasource() {
		return datasource;
	}
	public void setDatasource(DataSource datasource) {
		this.datasource = datasource;
	}
	public BcsJobOrderChronologiAllocationReader() 
	{
		super();
		   
	}
	@Override
	public void transaction() throws InterruptedException, Throwable
	{		
		datasource = super.getDataSource(); 
		ParametrosDefault param = new ParametrosDefault();
		param.setIdEmpresa(1);
		param.setIdUsuario(3);
		param.setModulo("ORD");
		
		try( Connection con = datasource.getConnection();   ){
			
			
		try{
 		 
		  
 		 conn.addLog(super.getJobId(), "ACT", "Verificando Asignacion Automatica en Ejecucion",con);
 		 String libroOrdenes = super.getParam("IdLibroOrdenes")!=null?super.getParam("IdLibroOrdenes"):"";
   		 if(libroOrdenes.trim().length()>0)
  		 {
 		 if(conn.bookIsActive(libroOrdenes, con))
 		 {
 		int executionExists = conn.existAssignAutomaticActive(Integer.parseInt(libroOrdenes),"ASIG_CRONO",con);
 		if(executionExists==0)             
		{
			
			int resultado  = conn.getLastExecutionAssignCount(libroOrdenes,con,param);
 			conn.addLog(super.getJobId(), "ACT", "Obteniendo la ultima ejecucion de un palo",con);
			boolean lastTransacction = false;
		
			if(resultado==0||resultado==1)
			{
				lastTransacction = true;
			}else if(resultado==-1)
			{
				lastTransacction = false;
			}
 			if(lastTransacction)
			{
				final int seconds = conn.getFechaInSeconds(con);
 				conn.isActiveExecutionAssignAutomatic(super.getJobId(),seconds,con);
 				con.commit();
			}
			else
			{
				conn.addLog(super.getJobId(), "ACT", "No existe un programa de asignacion cronologica para su ejecucion",con);
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,"No existe Asignacion Automatica que deba ejecutarse.");
			}
			 
		}
 		 }
  		 }else{				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,"Problemas al obtener libro");
}
		}catch(Exception e)
		{
			con.rollback();
			e.printStackTrace();
			Logger.getLogger(this.getClass().getName()).log(Level.INFO,e.getMessage(),e.getCause());

		}
		}
	}

	@Override
	public void onError(Throwable t) {
 		try {
 			getConnection().rollback();
			conn.addLog(super.getJobId(), "ACT", t.getMessage(),con);
			t.printStackTrace();
			 
 		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(this.getClass().getName()).log(Level.INFO,e.getMessage(),e.getCause());

		}
		 
 		
	}
	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	

}
