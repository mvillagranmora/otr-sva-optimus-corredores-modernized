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

public class DesbloqueoAutomaticoClientesPorTemporalidad implements JavaCall {	
	private final Log log = new Log("SOC_DESBLOQUEOAUTOMATICOCLIENTES_TEMPORALIDAD");
	
	//QUERIES
	private final String SQL_SELECT_DATOSPROCESAR="select \"IdCliente\",\"FechaBloqueo\",\"FechaBloqueoTemporal\" "
			+ "from \"BSCL.SocioNegocio.BD::CLI.Cliente\" "
			+ "where \"IdEstado\" = 'BLO' and \"IdTipoBloqueo\"='BTEMPO' and \"IdEmpresa\" = :IdEmpresa and \"FechaBloqueoTemporal\"<=:FechaSistema;";
	
	private final String SQL_UPDATE_CLIENTE = "update \"BSCL.SocioNegocio.BD::CLI.Cliente\" "
			+ "set \"IdEstado\"=:IdEstado,\"IdTipoDesbloqueo\"=:IdTipoDesbloqueo, \"ComentarioDesbloqueo\"=:ComentarioDesbloqueo, "
			+ "\"FechaDesbloqueo\"=CURRENT_UTCTIMESTAMP,\"FechaModificacion\"=CURRENT_UTCTIMESTAMP "
			+ "where \"IdCliente\"=:IdCliente;";
	
	/*******************
	 * pojos
	 *******************/
	public static class DatosConsulta{
		public Long 				IdCliente;
		public java.sql.Timestamp	FechaBloqueo;
		public java.sql.Timestamp	FechaBloqueoTemporal;
	}
	
	public static class DatosActualizar{
		public Long 	IdCliente;
		public String 	IdEstado;
		public String 	IdTipoDesbloqueo;
		public String	ComentarioDesbloqueo;
	}
	
	/*******************
	 * clases in/out
	 *******************/
	public static class InDesbloqueoAutomaticoClientesPorTemporalidad {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_SimularOperacion;
	}
	
	public static class OutDesbloqueoAutomaticoClientesPorTemporalidad{
		public Long 			P_OUT_ERRORESCONTADOR;
		public List<Registro> 	P_OUT_LISTA_CLIENTES;
		public OutDesbloqueoAutomaticoClientesPorTemporalidad(){
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
		InDesbloqueoAutomaticoClientesPorTemporalidad in=gson.fromJson(jin.toString() ,InDesbloqueoAutomaticoClientesPorTemporalidad.class);
		OutDesbloqueoAutomaticoClientesPorTemporalidad out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		
        //prueba de la clase
		try (Connection con = PlataformaUtils.getMBConnection();){
			InDesbloqueoAutomaticoClientesPorTemporalidad entrada = new InDesbloqueoAutomaticoClientesPorTemporalidad();
			entrada.p_IdEmpresa=1L;
			entrada.p_IdUsuario=37L;
			entrada.p_IdModulo="SOC";
			entrada.p_SimularOperacion="S";
 			JsonParser parser = new JsonParser();
	        JsonObject dataJsonIn = parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        DesbloqueoAutomaticoClientesPorTemporalidad demo=new DesbloqueoAutomaticoClientesPorTemporalidad();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	con.close();
		}catch (Exception e){e.printStackTrace();}
        System.out.println("== END ==");
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutDesbloqueoAutomaticoClientesPorTemporalidad logica(Connection con, InDesbloqueoAutomaticoClientesPorTemporalidad entrada) throws Exception {
		boolean debug = true;
		log.write("[INI]PROCESAMIENTO CAMBIO ESTADO CLIENTE/PORTAFOLIO, DEBUG="+debug);
		OutDesbloqueoAutomaticoClientesPorTemporalidad salida = new OutDesbloqueoAutomaticoClientesPorTemporalidad();
		salida.P_OUT_ERRORESCONTADOR = 1L;
		SQL sql=new SQL();
		String fechaHoraSistemaStr = Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa);
		java.sql.Timestamp fechaHoraSistemaTs = Fecha.toTimeStamp(fechaHoraSistemaStr);
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
			aux.IdEstado="ACT";
			aux.IdTipoDesbloqueo="AAUTOM";
			aux.ComentarioDesbloqueo="Finaliza bloqueo temporal";
			dacli.add(aux);
		}
		// realiza update
		if(dacli.size()>0){
			if(entrada.p_SimularOperacion.equalsIgnoreCase("N")){
				sql.batch(con,SQL_UPDATE_CLIENTE, dacli,DatosActualizar.class);
			}
			//realizado el update, devolver listado IdClientes procesados
			for(DatosActualizar item:dacli){
				salida.P_OUT_LISTA_CLIENTES.add(new OutDesbloqueoAutomaticoClientesPorTemporalidad.Registro(item.IdCliente));
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
