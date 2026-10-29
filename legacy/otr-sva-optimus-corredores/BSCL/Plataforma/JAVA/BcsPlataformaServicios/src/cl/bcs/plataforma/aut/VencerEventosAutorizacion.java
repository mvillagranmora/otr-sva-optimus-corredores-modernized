package cl.bcs.plataforma.aut;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class VencerEventosAutorizacion implements JavaCall   {
	
	/*
	 * El log
	 */
	private final Log log=new Log("AUT.VencerEventosAutorizacion");
	
	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	
	private final String SQL_ConsultaEventoVencido = loadResourceToString("cl/bcs/plataforma/aut/sql/ConsultaEventoVencido.sql");
	
	
	/*
	 * Clases IN/OUT
	 */	
	public static class ServicioEntrada {
		public Long 						p_IdEmpresa;
		public Long 						p_IdUsuario;
		public String 						p_IdModulo;	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ServicioSalida {
		public Long 								P_OUT_ERRORESCONTADOR;		
		public Long 								P_OUT_ADVERTENCIASCONTADOR;
		public List<ServicioSalidaAdvertencia>  	P_OUT_ADVERTENCIADETALLE;
		public List<ServicioSalidaError>  			P_OUT_ERRORESDETALLE;
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}			
	
	public static class ServicioSalidaAdvertencia {
		public String 						Codigo;
		public String 						RequiereAutorizacion;
		public String 						Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		}			
	}

	public static class ServicioSalidaError {
		public String 						Codigo;
		public String 						RequiereAutorizacion;
		public String 						Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		}			
	}
	
	/*
	 * Objetos de procesamiento
	 */
	public static class EventoPrc extends cl.bcs.plataforma.aut.pojo.Evento  {
		public String toString() {
			return Utils.printJson(this, false);
		}			
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();		
		ServicioEntrada in=gson.fromJson(jin.toString() ,ServicioEntrada.class);
		ServicioSalida out=logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	
	/*
	 * Autoriza etpas o eventos de autorizacion
	 * 
	 * 	 
	 */
	public ServicioSalida logica(Connection con, ServicioEntrada in) throws Exception {	
		log.writeAsJson("Inicio ",in);
		SQL sql=new SQL();
		int  SegundosZonaHoraria=FechaDB.ObtenerSegundosZonaHoraria(con,in.p_IdEmpresa);
		Fecha.setLocalOffSet(SegundosZonaHoraria);		
		ServicioSalida out=new ServicioSalida();
		Timestamp vFechaHoraSistema =  FechaDB.ObtenerFechaHoraSistemaTs(con, in.p_IdEmpresa);
		out.P_OUT_ERRORESCONTADOR=new Long(0);		
		out.P_OUT_ADVERTENCIASCONTADOR=new Long(0);
		out.P_OUT_ADVERTENCIADETALLE=new ArrayList<ServicioSalidaAdvertencia>();
		out.P_OUT_ERRORESDETALLE=new ArrayList<ServicioSalidaError>();
		
		//Eventos a autorizar
		HashMap<String,Object> parmsEvts= new HashMap<String,Object>();
		parmsEvts.put("IdEmpresa",in.p_IdEmpresa);
		parmsEvts.put("FechaHoraVigencia",vFechaHoraSistema);	
				
		List<EventoPrc> listaEvtVencidos = sql.query(con, SQL_ConsultaEventoVencido,parmsEvts,EventoPrc.class);
		if (listaEvtVencidos.size()==0) {
			log.writeAsJson("Sin eventos de autorizacion por vencer");
		}
		else {
			//log.writeAsJson("Por vencer",listaEvtVencidos);
			ModificarEstadoEventoAutorizacionMasivo modEvento=new ModificarEstadoEventoAutorizacionMasivo();
			ModificarEstadoEventoAutorizacionMasivo.ServicioEntrada inModAut=new ModificarEstadoEventoAutorizacionMasivo.ServicioEntrada();
			inModAut.p_IdEmpresa=in.p_IdEmpresa;
			inModAut.p_IdUsuario=in.p_IdUsuario;
			inModAut.p_IdModulo=in.p_IdModulo;
			inModAut.p_SimularOperacion="N";
			inModAut.p_UsaReglasNegocio="N";
			inModAut.p_EventoEtapa="EVENTO";
			inModAut.p_IdEstado="VCD";
			inModAut.p_Observaciones="MODIFICADO AUTOMATICAMENTE POR VENCIMIENTO";
			inModAut.p_Etapas=new ArrayList<>();
			inModAut.p_Eventos=new ArrayList<>();
			for (EventoPrc evt:listaEvtVencidos) {
				ModificarEstadoEventoAutorizacionMasivo.ServicioEntradaEvento newevt=new ModificarEstadoEventoAutorizacionMasivo.ServicioEntradaEvento();
				newevt.IdEvento = evt.IdEvento;
				inModAut.p_Eventos.add(newevt);				
			}
			ModificarEstadoEventoAutorizacionMasivo.ServicioSalida outModAut=modEvento.logica(con,inModAut);	
			log.writeAsJson("Resultado de vencidos",outModAut);
			out.P_OUT_ADVERTENCIASCONTADOR = outModAut.P_OUT_ADVERTENCIASCONTADOR;
			out.P_OUT_ERRORESCONTADOR=outModAut.P_OUT_ERRORESCONTADOR;
		}		
		//arma salida de respuesta
		log.write("Termino");		
		return out;
	}
	
	public static void main(String[] args) {
		//Settings by file
		String dbURL = "jdbc:sap://161.238.15.14:30415/?"; 
        String dbprop = "currentschema=BSCL;";
        String dbdrv = "com.sap.db.jdbc.Driver";
        String dbqry = "select 1 from dummy";
        String user = "SOPORTE";
        String pass = "Randall99";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        
        //Example
        System.out.println("== START ==");
		try {
			Connection con = ds.getConnection();
			con.setAutoCommit(false);
			SQL.exec(con,"set schema BSCL");
			SQL.exec(con,"SET TRANSACTION AUTOCOMMIT DDL OFF");
			//---------------------------------
			try {
 			JsonParser parser 		= new JsonParser(); 
	        JsonObject dataJsonIn	= parser.parse("{\"p_IdEmpresa\":1,\"p_IdUsuario\":2,\"p_IdModulo\":\"AUT\" }").getAsJsonObject();
	        VencerEventosAutorizacion demo=new VencerEventosAutorizacion();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	      
	    	con.commit();
			}catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
        System.out.println("== END ==");
	}		
}
