package cl.bcs.socionegocio.blo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class DesbloqueoAutomaticoPortafolioPorTemporalidad implements JavaCall {	
	private final Log log = new Log("SOC_DESBLOQUEOAUTOMATICOPORTAFOLIO_TEMPORALIDAD");
	
	//QUERIES
	private final String SQL_SELECT_DATOSPROCESAR_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/blo/sql/BloqueoDesbloqueoPortafolio_SelectPortafoliosProcesar.sql");
	private final String SQL_UPDATE_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/blo/sql/BloqueoDesbloqueoPortafolio_UpdatePortafoliosProcesar.sql");
	
	/*******************
	 * pojos
	 *******************/
	public static class DatosConsulta{
		public String 				CodigoPortafolio;
		public Long 				IdCliente;
		public Long 				IdPortafolio; 
		public java.sql.Timestamp	FechaBloqueo;
		public java.sql.Timestamp	FechaBloqueoTemporal;
	}
	
	public static class DatosActualizar{
		public Long 	IdPortafolio;
		public String 	IdEstado;
		public String 	IdTipoDesbloqueo;
		public String	ComentarioDesbloqueo;
	}
	
	/*******************
	 * clases in/out
	 *******************/
	public static class InDesbloqueoAutomaticoPortafoliosPorTemporalidad {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_SimularOperacion;
	}
	
	public static class OutDesbloqueoAutomaticoPortafoliosPorTemporalidad{
		public Long 			P_OUT_ERRORESCONTADOR;
		public List<Registro> 	P_OUT_LISTA_PORTAFOLIO;
		public OutDesbloqueoAutomaticoPortafoliosPorTemporalidad(){
			P_OUT_LISTA_PORTAFOLIO = new ArrayList<Registro>();
		}
		public static class Registro{
			public Long 	IdPortafolio;
			public Registro(){}
			public Registro(Long IdPortafolio){
				this.IdPortafolio = IdPortafolio;
			}
		}
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InDesbloqueoAutomaticoPortafoliosPorTemporalidad in=gson.fromJson(jin.toString() ,InDesbloqueoAutomaticoPortafoliosPorTemporalidad.class);
		OutDesbloqueoAutomaticoPortafoliosPorTemporalidad out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		
        //prueba de la clase
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa2","connections.properties")){
			InDesbloqueoAutomaticoPortafoliosPorTemporalidad entrada = new InDesbloqueoAutomaticoPortafoliosPorTemporalidad();
			entrada.p_IdEmpresa=1L;
			entrada.p_IdUsuario=37L;
			entrada.p_IdModulo="SOC";
			entrada.p_SimularOperacion="S";
 			JsonParser parser = new JsonParser();
	        JsonObject dataJsonIn = parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        DesbloqueoAutomaticoPortafolioPorTemporalidad demo=new DesbloqueoAutomaticoPortafolioPorTemporalidad();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	//con.commit();
	    	con.rollback();
	    	con.close();
		}catch (Exception e){e.printStackTrace();}
        System.out.println("== END ==");
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutDesbloqueoAutomaticoPortafoliosPorTemporalidad logica(Connection con, InDesbloqueoAutomaticoPortafoliosPorTemporalidad entrada) throws Exception {
		boolean debug = true;
		log.write("[INI]PROCESAMIENTO CAMBIO ESTADO PORTAFOLIO, DEBUG="+debug);
		OutDesbloqueoAutomaticoPortafoliosPorTemporalidad salida = new OutDesbloqueoAutomaticoPortafoliosPorTemporalidad();
		salida.P_OUT_ERRORESCONTADOR = 1L;
		SQL sql=new SQL();
		String fechaHoraSistemaStr = Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa);
		java.sql.Timestamp fechaHoraSistemaTs = Fecha.toTimeStamp(fechaHoraSistemaStr);
		HashMap<String,Object> parmsconsulta =new HashMap<String,Object>();
		//parmsconsulta.put("IdEmpresa", entrada.p_IdEmpresa);
		parmsconsulta.put("FechaSistema", fechaHoraSistemaTs);
		List<DatosConsulta> datosConsulta = sql.query(con, SQL_SELECT_DATOSPROCESAR_PORTAFOLIO,parmsconsulta,DatosConsulta.class);
		log.write("prepara set de datos de salida y datos para el update");
		List<DatosActualizar> dacli=new ArrayList<DatosActualizar>();
		//procesa datos
		for(DatosConsulta itemcons:datosConsulta){
			DatosActualizar aux = new DatosActualizar();
			aux.IdPortafolio=itemcons.IdPortafolio;
			aux.IdEstado="ACT";
			aux.IdTipoDesbloqueo="DESBAUTOM";
			aux.ComentarioDesbloqueo="Finaliza bloqueo temporal";
			dacli.add(aux);
		}
		// realiza update
		if(dacli.size()>0){
			if(entrada.p_SimularOperacion.equalsIgnoreCase("N")){
				sql.batch(con,SQL_UPDATE_PORTAFOLIO, dacli,DatosActualizar.class);
			}
			//realizado el update, devolver listado IdClientes procesados
			for(DatosActualizar item:dacli){
				salida.P_OUT_LISTA_PORTAFOLIO.add(new OutDesbloqueoAutomaticoPortafoliosPorTemporalidad.Registro(item.IdPortafolio));
			}
		}else{
			//no hay datos para bloquear.
		}
		salida.P_OUT_ERRORESCONTADOR=0L;
		log.writeAsJson("ENTRADA:",entrada);
		log.writeAsJson("SALIDA:",salida);
		log.write("****************** FIN ****************");
		return salida;
	}
}
