package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.cua.ProcesarCuadraturaCustodiaRFIF;
import cl.bcs.plataforma.cua.ProcesarCuadraturaCustodiaRFIF.EntradaProcesarCuadraturaCustodiaRFIF;
import cl.bcs.plataforma.cua.ProcesarCuadraturaCustodiaRFIF.SalidaProcesarCuadraturaCustodia;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class ValidaSaldosMovimientos extends BcsJob implements JavaCall {	
	private final Log log = new Log("ValidaSaldosMovimientos");
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {	
		
		ProcesarCuadraturaCustodiaRFIF generarConsulta = new ProcesarCuadraturaCustodiaRFIF();
		EntradaProcesarCuadraturaCustodiaRFIF entradaConsulta = new EntradaProcesarCuadraturaCustodiaRFIF();
		
		try{
			entradaConsulta.p_FechaInicio = this.getParam("p_FechaInicio");
			}
		catch (Exception er) {entradaConsulta.p_FechaInicio = "";}
		try{
			entradaConsulta.p_FechaFin = this.getParam("p_FechaFin");
			}
		catch (Exception er) {entradaConsulta.p_FechaFin = "";}			
		entradaConsulta.p_IdModulo  					= this.getParam("p_IdModulo");
		entradaConsulta.p_IdEmpresa 					= Long.parseLong(this.getParam("p_IdEmpresa"));
		entradaConsulta.p_IdUsuario 					= Long.parseLong(this.getParam("p_IdUsuario"));
		
		String fechaFin = "";
		String fechaInicio = "";
		Boolean resultado = new Boolean(false);
		try{			
			Gson gson = Utils.getGsonBuilder();
			JSONObject myObject = new JSONObject();    	    
			
			if("".equals(entradaConsulta.p_FechaFin)) {		
				fechaFin = ConfiguracionSistema.getParametroConfiguracionString(con,1L,"PLATAFORMA","PLA.FechaSistema");
			}else {
				fechaFin = entradaConsulta.p_FechaFin;
			}
			if("".equals(entradaConsulta.p_FechaInicio)) {		
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate localDate = LocalDate.parse(fechaFin, formatter);
				LocalDate lessADay = localDate.minusDays(1);  
				fechaInicio=lessADay.toString();
			}else {
				fechaInicio=entradaConsulta.p_FechaInicio;
			}
			
			 myObject.put("p_FechaInicio", fechaInicio );
		     myObject.put("p_FechaFin", fechaFin);	
		     myObject.put("p_IdModulo", entradaConsulta.p_IdModulo );
		     myObject.put("p_IdEmpresa", entradaConsulta.p_IdEmpresa);	
		     myObject.put("p_IdUsuario", entradaConsulta.p_IdUsuario);	
		     			
			EntradaProcesarCuadraturaCustodiaRFIF entrada = gson.fromJson(myObject.toString(), EntradaProcesarCuadraturaCustodiaRFIF.class);
		
		    SalidaProcesarCuadraturaCustodia ObjOut=generarConsulta.logica(con, entrada);
		    con.commit();
		    if(ObjOut.P_OUT_ERRORESCONTADOR==0) {
		    	log.write(ObjOut.P_OUT_ERRORESDETALLE);
		    	resultado = new Boolean(true);
		    }else {
		    	resultado = new Boolean(false);
		    }			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return resultado;			
	}

	@Override
	public void onError(Throwable t) {
		log.write("****** ERROR *******");
        log.write(t);
        t.printStackTrace();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
