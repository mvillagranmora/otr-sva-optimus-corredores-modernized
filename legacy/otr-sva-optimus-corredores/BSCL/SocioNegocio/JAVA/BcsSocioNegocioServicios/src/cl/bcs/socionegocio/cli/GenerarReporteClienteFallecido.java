package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

import com.opencsv.CSVWriter;


public class GenerarReporteClienteFallecido implements JavaCall {
	private final Log log = new Log("GenerarReporteClienteFallecido");
	private final String SQL_SELECT_CONSULTA_CLIENTE_FALLECIDO = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConsultaClienteFallecido_Select.sql");
	
	/*******************
	 * pojos
	 *******************/
	public static class ConsultaClienteFallecido{
		
		public Long			IdPersona;
		public String   	NombreCompleto;
		public String     	DocumentoIdentidad;
		public Date			FechaDefuncion;
		public String       NombreEstado;
		public String 		NombreTipoBloqueoCliente;
		public Timestamp 	FechaBloqueo;
		public String 		ComentarioBloqueo;
		public String 		NombreTipoBloqueo;
		
	}
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InConsultaClientes{
		
		public Long    p_IdEmpresa;
		public Long    p_IdUsuario;
		public String  p_IdModulo;
		public String  p_Ruta;
		
	}
	
	public static class OutConsultaClientes{
		
		public Long 	  P_OUT_ERRORESCONTADOR;
		public String 	  P_OUT_NOMBRE_ARCHIVO;
		public String	  P_OUT_RUTA_NOMBRE_ARCHIVO;
		
	}
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InConsultaClientes in=gson.fromJson(jin.toString() ,InConsultaClientes.class);
		OutConsultaClientes out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	
	public OutConsultaClientes logica(Connection con, InConsultaClientes entrada) throws Exception {
		
		OutConsultaClientes salida= new OutConsultaClientes();
		salida.P_OUT_ERRORESCONTADOR = new Long(1);
		
		SQL sql=new SQL();
		HashMap<String,Object> paramsConsultaCliente = new HashMap<String,Object>();
		paramsConsultaCliente.put("IdEmpresa", entrada.p_IdEmpresa);
		List<ConsultaClienteFallecido> clientes = sql.query(con,SQL_SELECT_CONSULTA_CLIENTE_FALLECIDO,paramsConsultaCliente,ConsultaClienteFallecido.class);
	    salida.P_OUT_NOMBRE_ARCHIVO = "CLI/ReporteClienteFallecido" + Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(con, entrada.p_IdEmpresa),"yyyyMMddHHmmss") + ".csv";
		salida.P_OUT_RUTA_NOMBRE_ARCHIVO = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
		FileOutputStream os = new FileOutputStream(salida.P_OUT_RUTA_NOMBRE_ARCHIVO);
		os.write(0xef);
		os.write(0xbb);
		os.write(0xbf);
		CSVWriter EscribeCSV=new CSVWriter(new OutputStreamWriter(os,"UTF-8"),';');

		try{
			String [] FilaEscribeClienteFallecido= new String[]{
					//"Id Persona",
					"Nombre Completo",
					"Documento Identidad",
					"Fecha Defuncion",
					"Estado",
					"Tipo Bloqueo Cliente",
					"Fecha Bloqueo Cliente",
					"Comentario Bloqueo",
					//"Tipo Bloqueo Persona"
					};
						
			EscribeCSV.writeNext(FilaEscribeClienteFallecido);
			log.write("Numero clientes fallecidos",clientes.size());
			int  SegundosZonaHoraria=FechaDB.ObtenerSegundosZonaHoraria(con,entrada.p_IdEmpresa);
			Fecha.setLocalOffSet(SegundosZonaHoraria);
			for(ConsultaClienteFallecido cliente : clientes){
				
				FilaEscribeClienteFallecido = new String[]{
						
						cliente.NombreCompleto,
						cliente.DocumentoIdentidad.toString(),
						Fecha.toLocalFormat(new java.sql.Timestamp(cliente.FechaDefuncion.getTime()),"dd/MM/yyyy"),
						cliente.NombreEstado,
						cliente.NombreTipoBloqueo,
						cliente.FechaBloqueo != null ? Fecha.toLocalFormat(cliente.FechaBloqueo,"dd/MM/yyyy") : "",
						cliente.ComentarioBloqueo
								
						};
				EscribeCSV.writeNext(FilaEscribeClienteFallecido);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{
				EscribeCSV.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
		}
		
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		log.writeAsJson("SALIDA:",salida);
		return salida;
		
	}
	
	public static void main(String[] args) {
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
			try{
				
				InConsultaClientes entrada = new InConsultaClientes();
				entrada.p_IdEmpresa = new Long(1);
				entrada.p_IdUsuario = new Long(21);
				entrada.p_IdModulo = "PLA";
				entrada.p_Ruta     = "C:/reporte";
				
				
	 			JsonParser parser 		= new JsonParser();
		        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
		        GenerarReporteClienteFallecido demo=new GenerarReporteClienteFallecido();		
		    	System.out.println(demo.call(con, dataJsonIn).toString());	
		    	con.commit();
			}catch(Exception e){
				con.rollback();
				e.printStackTrace();
			}finally{
				con.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
	
	
}