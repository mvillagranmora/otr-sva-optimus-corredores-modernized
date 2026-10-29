package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.val.CuponesWs;
import cl.bcs.plataforma.val.TipificarWs;
import cl.bcs.plataforma.val.ValorizarWs;
import cl.bcs.plataforma.val.util.ValoUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/***************************************
 * @version 	1.0
 * @author 		cstange
 *
 * @version 	1.1
 * @author 		jgarces
 * 
 * @version 	1.2
 * @author 		jgarces
 * Se migra procedimientos java.
 *
**************************************/

public class ValorizaJob extends BcsJob {
	private final Log log = new Log("ValorizaJob");
	
	 @Override
	 public boolean transaction(Connection con) throws InterruptedException, Throwable {
		 return logica(con,false);
	 }
	
    public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
		SQL sql=new SQL();
		String sgrupo = "";
		if (debug) {
			sgrupo = "211478";
		}
		else {
			sgrupo=super.getParam("IdGrupo");
		}
        
        String auxmsj = "";
        Long IdGrupo=new Long(sgrupo);
        List<ValoUtils.TareaValorizacion> arrTarea = null;
    	try{
    		HashMap<String,Object> parmsConsultaTarea =new HashMap<String,Object>();
    		parmsConsultaTarea.put("IdGrupo", IdGrupo);	
    		arrTarea = sql.query(con, ValoUtils.SQL_SelectTareaValorizacion,parmsConsultaTarea,ValoUtils.TareaValorizacion.class);
    		log.writeAsJson("DATOS OBTENIDOS DE LA TAREA: ",arrTarea);
    		if(arrTarea.size()==0){
    			auxmsj = "Error al obtener datos de tabla TareaValorizacion";
    			log.write(auxmsj);
    			throw new InterruptedException(auxmsj);
    		}
    		for(ValoUtils.TareaValorizacion item : arrTarea) item.IdEstado = "ERR"; 
    		String tipoTarea = arrTarea.get(0).TipoTarea;
    		if(tipoTarea.equals("TIP")){
    			TipificarWs tip = new TipificarWs();
    			TipificarWs.InTipificarWs inTip = new TipificarWs.InTipificarWs();
    			inTip.idGrupo = IdGrupo;
    			inTip.instrumentos = arrTarea;
    			inTip.rutinaValorizador = arrTarea.get(0).RutinaValorizacion;
    			TipificarWs.OutTipificarWs outTip = tip.logica(con, inTip);
    			if(outTip.erroresContador>0){
    				auxmsj = "Error TipificarWs: "+outTip.descripcion;
    				log.write(auxmsj);
    				throw new InterruptedException(auxmsj);
    			}
    			arrTarea = outTip.instrumentos;
    		}else if(tipoTarea.equals("VAL")){
    			ValorizarWs val = new ValorizarWs();
    			ValorizarWs.InValorizarWs inVal = new ValorizarWs.InValorizarWs();
    			inVal.idGrupo = IdGrupo;
    			inVal.instrumentos = arrTarea;
    			inVal.rutinaValorizador = arrTarea.get(0).RutinaValorizacion;
    			String iniEje = Utils.getCurrentTimeStamp();
    			ValorizarWs.OutValorizarWs outVal = val.logica(con, inVal);
    			log.write("Inicio ejecucion: "+iniEje+" - Fin Ejecucion: "+Utils.getCurrentTimeStamp());
    			if(outVal.erroresContador>0){
    				auxmsj = "Error TipificarWs: "+outVal.descripcion;
    				log.write(auxmsj);
    				throw new InterruptedException(auxmsj);
    			}
    			arrTarea = outVal.instrumentos;
    		}else if(tipoTarea.equals("CUP")){
    			CuponesWs cup = new CuponesWs();
    			CuponesWs.InCuponesWs inCup = new CuponesWs.InCuponesWs();
    			inCup.idGrupo = IdGrupo;
    			inCup.cupones = arrTarea;
    			inCup.rutinaValorizador = arrTarea.get(0).RutinaValorizacion;
    			CuponesWs.OutCuponesWs outCup = cup.logica(con, inCup);
    			if(outCup.erroresContador>0){
    				auxmsj = "Error CuponesWs: "+outCup.descripcion;
    				log.write(auxmsj);
    				throw new InterruptedException(auxmsj);
    			}
    			arrTarea = outCup.cupones;
    		}else{
    			auxmsj = "TipoTarea "+tipoTarea+"no existe o no implementada";
    			log.write(auxmsj);
    			throw new InterruptedException(auxmsj);
    		}
    		//actualizar
    		sql.batch(con,ValoUtils.SQL_UpdateTareaValorizacion, arrTarea, ValoUtils.TareaValorizacion.class);
        	log.write("***** FIN TAREA *****");
        	return true;
    	} catch(Exception e){
    		log.write("***** ERROR TAREA *****");
    		log.write(e);
    		e.printStackTrace();
    		if(arrTarea!=null)sql.batch(con,ValoUtils.SQL_UpdateTareaValorizacion, arrTarea, ValoUtils.TareaValorizacion.class);
    		throw e;
    	}
    }

    @Override
    public void onError(Throwable t) {
    	log.write(t.getMessage());
        log.write("****** ERROR TAREA  *******");
        t.printStackTrace();
    }

	@Override
	public void pause() {
	}
	
	public static void main (String[] args) throws InterruptedException, Throwable {
		
		Connection asdf = PlataformaUtils.getMBConnection();
		ValorizaJob val = new ValorizaJob();
		
		val.logica(asdf,true);
		
		
		
	}
	
	
	
	
	
}