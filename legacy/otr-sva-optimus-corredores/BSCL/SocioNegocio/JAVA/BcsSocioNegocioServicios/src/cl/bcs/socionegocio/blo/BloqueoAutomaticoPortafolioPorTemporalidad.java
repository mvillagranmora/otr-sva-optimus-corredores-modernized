package cl.bcs.socionegocio.blo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BloqueoAutomaticoPortafolioPorTemporalidad implements JavaCall {	
	private final Log log = new Log("SOC_BLOQUEOAUTOMATICOPORTAFOLIOS_TEMPORALIDAD");
	
	//QUERIES
	private final String SQL_SELECT_DATOSPROCESAR_PORTAFOLIOACTIVO = loadResourceToString("cl/bcs/socionegocio/blo/sql/BloqueoDesbloqueoPortafolio_SelectPortafoliosProcesarActivos.sql");
	private final String SQL_UPDATE_PORTAFOLIOBLOQUEO = loadResourceToString("cl/bcs/socionegocio/blo/sql/BloqueoDesbloqueoPortafolio_UpdatePortafoliosProcesarBloquear.sql");
	
	/*******************
	 * pojos
	 *******************/
	public static class DatosConsulta{
		public String 				CodigoPortafolio;
		public Long 				IdCliente;
		public Long 				IdPortafolio; 
		public java.sql.Timestamp	FechaDesbloqueo;
		public java.sql.Timestamp	FechaDesbloqueoTemporal;
	}
	
	public static class DatosActualizar{
		public Long 	IdPortafolio;
		public String 	IdEstado;
		public String 	IdTipoBloqueo;
		public String	ComentarioBloqueo;
	}
	
	/*******************
	 * clases in/out
	 *******************/
	public static class InBloqueoAutomaticoPortafoliosPorTemporalidad {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_SimularOperacion;
	}
	
	public static class OutBloqueoAutomaticoPortafoliosPorTemporalidad{
		public Long 			P_OUT_ERRORESCONTADOR;
		public List<Registro> 	P_OUT_LISTA_PORTAFOLIO;
		public OutBloqueoAutomaticoPortafoliosPorTemporalidad(){
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
		InBloqueoAutomaticoPortafoliosPorTemporalidad in=gson.fromJson(jin.toString() ,InBloqueoAutomaticoPortafoliosPorTemporalidad.class);
		OutBloqueoAutomaticoPortafoliosPorTemporalidad out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/*public static void main(String[] args) {
		//Settings by file
        String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "JGARCES";
        String pass   = "Bolsa199";		
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        //prueba de la clase
		try {
			Connection con = ds.getConnection();
			InBloqueoAutomaticoPortafoliosPorTemporalidad entrada = new InBloqueoAutomaticoPortafoliosPorTemporalidad();
			entrada.p_IdEmpresa=1L;
			entrada.p_IdUsuario=37L;
			entrada.p_IdModulo="SOC";
			entrada.p_SimularOperacion="N";
 			JsonParser parser = new JsonParser();
	        JsonObject dataJsonIn = parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        BloqueoAutomaticoPortafolioPorTemporalidad demo=new BloqueoAutomaticoPortafolioPorTemporalidad();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	con.close();
		}catch (Exception e){e.printStackTrace();}
        System.out.println("== END ==");
	}*/
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutBloqueoAutomaticoPortafoliosPorTemporalidad logica(Connection con, InBloqueoAutomaticoPortafoliosPorTemporalidad entrada) throws Exception {
		boolean debug = true;
		log.write("[INI]PROCESAMIENTO CAMBIO ESTADO PORTAFOLIO, DEBUG="+debug);
		OutBloqueoAutomaticoPortafoliosPorTemporalidad salida = new OutBloqueoAutomaticoPortafoliosPorTemporalidad();
		salida.P_OUT_ERRORESCONTADOR = 1L;
		SQL sql=new SQL();
		String fechaHoraSistemaStr = Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa);
		java.sql.Timestamp fechaHoraSistemaTs = Fecha.toTimeStamp(fechaHoraSistemaStr);
		log.write("Fecha del sistema: "+fechaHoraSistemaStr);
		HashMap<String,Object> parmsconsulta =new HashMap<String,Object>();
		//parmsconsulta.put("IdEmpresa", entrada.p_IdEmpresa);
		parmsconsulta.put("FechaSistema", fechaHoraSistemaTs);
		List<DatosConsulta> datosConsulta = sql.query(con, SQL_SELECT_DATOSPROCESAR_PORTAFOLIOACTIVO,parmsconsulta,DatosConsulta.class);
		log.write("prepara set de datos de salida y datos para el update");
		List<DatosActualizar> dacli=new ArrayList<DatosActualizar>();
		//procesa datos
		for(DatosConsulta itemcons:datosConsulta){
			DatosActualizar aux = new DatosActualizar();
			aux.IdPortafolio =itemcons.IdPortafolio;
			aux.IdEstado="BLO";
			aux.IdTipoBloqueo="BLOAUTOM";
			aux.ComentarioBloqueo="Finaliza desbloqueo temporal";
			dacli.add(aux);
		}
		// realiza update
		if(dacli.size()>0){
			if(entrada.p_SimularOperacion.equalsIgnoreCase("N")){
				sql.batch(con,SQL_UPDATE_PORTAFOLIOBLOQUEO, dacli,DatosActualizar.class);
			}
			//realizado el update, devolver listado IdClientes procesados
			for(DatosActualizar item:dacli){
				salida.P_OUT_LISTA_PORTAFOLIO.add(new OutBloqueoAutomaticoPortafoliosPorTemporalidad.Registro(item.IdPortafolio));
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
