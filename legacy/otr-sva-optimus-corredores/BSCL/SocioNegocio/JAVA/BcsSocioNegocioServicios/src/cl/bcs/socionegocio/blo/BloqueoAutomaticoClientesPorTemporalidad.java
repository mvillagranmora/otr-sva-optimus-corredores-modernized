package cl.bcs.socionegocio.blo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BloqueoAutomaticoClientesPorTemporalidad implements JavaCall {	
	private final Log log = new Log("SOC_BLOQUEOAUTOMATICOCLIENTES_TEMPORALIDAD");	

	// QUERIES
	private final String SQL_SELECT_DATOSPROCESAR = loadResourceToString("cl/bcs/socionegocio/blo/sql/BloqueoAutomaticoClientesPorTemporalidad_SelectDatosProcesar.sql");
	private final String SQL_UPDATE_CLIENTE = loadResourceToString("cl/bcs/socionegocio/blo/sql/BloqueoAutomaticoClientesPorTemporalidad_UpdateCliente.sql");		
	
	/*******************
	 * pojos
	 *******************/
	public static class DatosConsulta{
		public Long 				IdCliente;
		public java.sql.Timestamp	FechaDesbloqueo;
		public java.sql.Timestamp	FechaDesbloqueoTemporal;
	}
	
	public static class DatosActualizar{
		public Long 	IdCliente;
		public String 	IdEstado;
		public String 	IdTipoBloqueo;
		public String	ComentarioBloqueo;
	}
	
	/*******************
	 * clases in/out
	 *******************/
	public static class InBloqueoAutomaticoClientesPorTemporalidad {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_SimularOperacion;
	}
	
	public static class OutBloqueoAutomaticoClientesPorTemporalidad{
		public Long 			P_OUT_ERRORESCONTADOR;
		public List<Registro> 	P_OUT_LISTA_CLIENTES;
		public OutBloqueoAutomaticoClientesPorTemporalidad(){
			P_OUT_LISTA_CLIENTES = new ArrayList<Registro>();
		}
		public static class Registro{
			public Long 	IdCliente;
			public Registro(){}
			public Registro(Long IdCliente){
				this.IdCliente = IdCliente;
			}
		}
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InBloqueoAutomaticoClientesPorTemporalidad in=gson.fromJson(jin.toString() ,InBloqueoAutomaticoClientesPorTemporalidad.class);
		OutBloqueoAutomaticoClientesPorTemporalidad out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception{
		
        try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {        	
			InBloqueoAutomaticoClientesPorTemporalidad entrada = new InBloqueoAutomaticoClientesPorTemporalidad();
			entrada.p_IdEmpresa=1L;
			entrada.p_IdUsuario=37L;
			entrada.p_IdModulo="SOC";
			entrada.p_SimularOperacion="N";
 			JsonParser parser = new JsonParser();
	        JsonObject dataJsonIn = parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        BloqueoAutomaticoClientesPorTemporalidad demo=new BloqueoAutomaticoClientesPorTemporalidad();		
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
	public OutBloqueoAutomaticoClientesPorTemporalidad logica(Connection con, InBloqueoAutomaticoClientesPorTemporalidad entrada) throws Exception {
		boolean debug = true;
		log.write("[INI]PROCESAMIENTO CAMBIO ESTADO CLIENTE/PORTAFOLIO, DEBUG="+debug);
		OutBloqueoAutomaticoClientesPorTemporalidad salida = new OutBloqueoAutomaticoClientesPorTemporalidad();
		salida.P_OUT_ERRORESCONTADOR = 1L;
		SQL sql=new SQL();
		String fechaHoraSistemaStr = Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa);
		java.sql.Timestamp fechaHoraSistemaTs = Fecha.toTimeStamp(fechaHoraSistemaStr);
		log.write("Fecha del sistema: "+fechaHoraSistemaStr);
		HashMap<String,Object> parmsconsulta =new HashMap<String,Object>();
		parmsconsulta.put("IdEmpresa", entrada.p_IdEmpresa);
		parmsconsulta.put("FechaSistema", fechaHoraSistemaTs);
		List<DatosConsulta> datosConsulta = sql.query(con, SQL_SELECT_DATOSPROCESAR,parmsconsulta,DatosConsulta.class);
		log.write("prepara set de datos de salida y datos para el update");
		List<DatosActualizar> dacli=new ArrayList<DatosActualizar>();
		//procesa datos
		for(DatosConsulta itemcons:datosConsulta){
			DatosActualizar aux = new DatosActualizar();
			aux.IdCliente=itemcons.IdCliente;
			aux.IdEstado="BLO";
			aux.IdTipoBloqueo="BAUTOM";
			aux.ComentarioBloqueo="Finaliza desbloqueo temporal";
			dacli.add(aux);
		}
		// realiza update
		if(dacli.size()>0){
			if(entrada.p_SimularOperacion.equalsIgnoreCase("N")){
				sql.batch(con,SQL_UPDATE_CLIENTE, dacli,DatosActualizar.class);
			}
			//realizado el update, devolver listado IdClientes procesados
			for(DatosActualizar item:dacli){
				salida.P_OUT_LISTA_CLIENTES.add(new OutBloqueoAutomaticoClientesPorTemporalidad.Registro(item.IdCliente));
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
