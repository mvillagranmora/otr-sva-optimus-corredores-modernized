package cl.bcs.plataforma.jobs.ejemplojob;
import java.math.BigDecimal;
import java.sql.Connection;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Utils;

/***********************************
 * @version 	1.0
 * @author 		jgarces
 *
***********************************/
public class EjemploJob extends BcsJob {
    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
    	System.out.println(Utils.getCurrentTimeStamp() +":EJEMPLO:  ****** OBTENER DATASOURCE  *******");
     
        System.out.println(Utils.getCurrentTimeStamp() +":EJEMPLO:  ****** INICIO  *******");
        try
        {
            String sgrupo=super.getParam("IdGrupoEjemploJob");
            BigDecimal IdGrupo=new BigDecimal(sgrupo);
            /*try(Statement cs1 = con.createStatement();) {
                String query="select * FROM \"BSCL.Plataforma.BD::EJ.TareaEjemploJob\" WHERE \"IdEstado\" = 'PEN' and  \"IdGrupoEjemploJob\"="+IdGrupo;
                try( ResultSet rset1 = cs1.executeQuery(query);) {
                	while(rset1.next()){
	                	Integer idTarea = (rset1.getObject(rset1.findColumn("IdTarea"))==null)?null:rset1.getInt(rset1.findColumn("IdTarea"));
	                	Integer idGrupoEjemploJob = (rset1.getObject(rset1.findColumn("IdGrupoEjemploJob"))==null)?null:rset1.getInt(rset1.findColumn("IdGrupoEjemploJob"));
	                    String idEstado = rset1.getString(rset1.findColumn("IdEstado"));
	                    String datoEjemplo = rset1.getString(rset1.findColumn("DatoEjemplo"));
	                    System.out.println(DateTools.getCurrentTimeStamp() +":EJEMPLO:  ****** Datos obtenidos:  *******  \n"
	                    		+ "IdTarea: " + idTarea + "\n"
	                    		+ "IdGrupoEjemploJob: " + idGrupoEjemploJob + "\n"
	                    		+ "IdEstado: " + idEstado + "\n"
	                    		+ "DatoEjemplo: " + datoEjemplo + "\n");
                	}
                }//close rset1
           	}//close cs1*/
            System.out.println(Utils.getCurrentTimeStamp() +":EJEMPLO:  ****** FIN TAREA *******");
            return true;//commit
        }catch(Exception er) {
        	System.out.println(":EJEMPLO:  ****** EXCEPTION *******");
        	throw er;//rollback
        }        
    }
    @Override
    public void onError(Throwable t) {
        System.out.println(Utils.getCurrentTimeStamp() +":EJEMPLO:  ****** ERROR  *******");
        t.printStackTrace();
    }
	@Override
	public void pause() {
		//TODO: 
	}
}