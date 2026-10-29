package cl.bcs.plataforma.ins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Salida;
import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.Resultado_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaArchivoPresenciaBursatil implements JavaCall{
	
	private final Log log = new Log("CargaArchivoPresenciaBursatil");
	
	private final String SQL_INSERT_CARGA_PRESENCIA_BURSATIL = loadResourceToString("cl/bcs/plataforma/ins/sql/CargaArchivoPresenciaBursatil_InsertCargaArchivoPresenciaBursatil.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/plataforma/ins/sql/CargaArchivoPresenciaBursatil_SelectInstrumentos.sql");
	
	
	public static class PresenciaBursatil{
		public String		CodigoIsin;
		public Long         IdInstrumento;
		public String 		Nemotecnico;
		public String 		Empresa;
		public String 		FechaCalculo;
		public String 		FechaVigencia;
		public Long         Presencia;
		public BigDecimal	Factor;
		public Long     	FormadorMercado;
		public String		FechaInscripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Instrumentos{
		public Long		IdInstrumento;
		public String	IdMoneda;
		public String	Nemotecnico;
		public String   IdClasificacionTipoInstrumento;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InCargaArchivoPresenciaBursatil{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_Archivo;
		public String 		p_Ruta;
	}
	
	public static class OutCargaArchivoPresenciaBursatil{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaArchivoPresenciaBursatil in=gson.fromJson(jin.toString() ,InCargaArchivoPresenciaBursatil.class);
		OutCargaArchivoPresenciaBursatil out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCargaArchivoPresenciaBursatil logica(Connection con, InCargaArchivoPresenciaBursatil entrada) throws Exception {
		OutCargaArchivoPresenciaBursatil out = new OutCargaArchivoPresenciaBursatil();
		
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_ErroresContador = 1L;
		out.p_out_NombreArchivoLog="ord/CARGA_PRESENCIA_BURSATIL_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		File folder = new File(entrada.p_Ruta + "/ord");
		if(!folder.exists()){
			folder.mkdirs();
		}
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			
			String archivoDecoded = new String(Base64.getDecoder().decode(entrada.p_Archivo));
			
			SimpleDateFormat formatoArchivo = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat formatoSP = new SimpleDateFormat("yyyy-MM-dd");
			JsonParser parser     = new JsonParser();
	        JsonObject dataJsonIn  = parser.parse(archivoDecoded).getAsJsonObject();
			
			List<PresenciaBursatil> operaciones = new ArrayList<PresenciaBursatil>();
			List<PresenciaBursatil> operacionesInsert = new ArrayList<PresenciaBursatil>();
			JsonArray registros= dataJsonIn.getAsJsonArray("p_contenido");
			Long i = 0L;
			List<String> nemotecnicos = new ArrayList<String>();
			
			// ---------------------------------------------------------------------------------------------
			// BUSCA INFORMACION RELACIONADA AL REGISTRO						
			HashMap<String,Object> params = new HashMap<>();
			
			// ---------------------------------------------------------------------------------------------
			// RECORRE REGISTROS DEL ARCHIVO Y METE DATA EN LISTA ("operaciones") PARA SU PROCESAMIENTO
			// ---------------------------------------------------------------------------------------------
			for(JsonElement registro : registros){
				numeroRegistros++;
				PresenciaBursatil operacion = new PresenciaBursatil();
				JsonObject elemento = registro.getAsJsonObject();
				String validacion = elemento.get("MENSAJEVALIDACION").getAsString();
				if("OK".equalsIgnoreCase(validacion)){
					try{
    					operacion.CodigoIsin = elemento.get("CIPBUR_CODIGOISIN").getAsString();
    					
    					
						operacion.Nemotecnico = elemento.get("CIPBUR_NEMOTECNICO").getAsString();
						operacion.Empresa = elemento.get("CIPBUR_EMPRESA").getAsString();
						operacion.FechaCalculo = elemento.get("CIPBUR_FECHACALCULO").getAsString();
						operacion.FechaVigencia = elemento.get("CIPBUR_FECHAVIGENCIA").getAsString();
						operacion.Presencia = elemento.get("CIPBUR_PRESENCIA").getAsLong();
						operacion.Factor = elemento.get("CIPBUR_FACTOR").getAsBigDecimal();
						operacion.FormadorMercado = elemento.get("CIPBUR_FORMADORMERCADO").getAsLong();
						operacion.FechaInscripcion = elemento.get("CIPBUR_FECHAINSCRIPCION").getAsString();
						if(!nemotecnicos.contains(operacion.Nemotecnico)) nemotecnicos.add(operacion.Nemotecnico);
						i++;
						
						
						operaciones.add(operacion);
					 }catch(Exception e){
						numeroRegistrosErroneos++;
						bwArchivoLog.write("Error al parsear el registro:" + elemento);
						bwArchivoLog.newLine();
					}
				}else{
					numeroRegistrosErroneos++;
					bwArchivoLog.write("Registro con Error:" + validacion + ",registro:" + elemento);
					bwArchivoLog.newLine();
				}
			}
			
			params.put("Nemotecnicos", nemotecnicos);
			List<Instrumentos> idInstrumentos = sql.query(con, SQL_SELECT_INSTRUMENTOS, params, Instrumentos.class);
			
			
			for (PresenciaBursatil operacion : operaciones){
				PresenciaBursatil operacionVal = new PresenciaBursatil();
				operacionVal.Nemotecnico = operacion.Nemotecnico;
				operacionVal.Empresa = operacion.Empresa;
				operacionVal.FechaCalculo = operacion.FechaCalculo;
				operacionVal.FechaVigencia = operacion.FechaVigencia;
				operacionVal.Presencia = operacion.Presencia;
				operacionVal.Factor = operacion.Factor;
				operacionVal.FormadorMercado = operacion.FormadorMercado;
				operacionVal.FechaInscripcion = operacion.FechaInscripcion;
				for (Instrumentos ins : idInstrumentos){
					if (ins.Nemotecnico.equalsIgnoreCase(operacion.Nemotecnico)){
						operacionVal.IdInstrumento = ins.IdInstrumento;
					}
				}
				if (operacionVal.IdInstrumento == null) throw new Exception ("Nemotecnico " + operacionVal.Nemotecnico + " no existe.");
				operacionesInsert.add(operacionVal);
			}
			
			
						
			sql.batch(con, SQL_INSERT_CARGA_PRESENCIA_BURSATIL, operacionesInsert, PresenciaBursatil.class);
			
			
			//evaluar salida
			long endTime = System.currentTimeMillis();
			String fechaFin = fm.format(new Date());
			bwArchivoLog.write("Fecha Hora Inicio:" + fecha);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Fecha Hora Termino:" + fechaFin);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Tiempo de ejecución:" + (endTime - startTime));
			bwArchivoLog.newLine();
			bwArchivoLog.write("Total Registros:" + numeroRegistros);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Registros Correctos:" + numeroRegistrosCorrecto);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Registros Erroneos:" + numeroRegistrosErroneos);
			out.p_out_ErroresContador = new Long(numeroRegistrosErroneos);
			
		}catch(Exception e){
			log.write(e);
		}finally{
			log.writeAsJson("SALIDA",out);
			if(bwArchivoLog != null){
				bwArchivoLog.close();
			}
		}
		
		return out;
		
	}
	
	public static void main (String [] args) {
		
		//Settings by file
        /*String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "JGARCES";
        String pass   = "Bolsa199";*/
		
		String dbURL  = "jdbc:sap://161.238.34.163:30015/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "desarrollo";
        String pass   = "Bolsa123";
        
        
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
  				
  				InCargaArchivoPresenciaBursatil entrada= new InCargaArchivoPresenciaBursatil();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "ORD";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				entrada.p_Archivo		= "eyJwX2NvbnRlbmlkbyI6W3siQ0lQQlVSX0NPRElHT0lTSU4iOiJQRVAyMTQwMDEwMDUiLCJDSVBCVVJfTkVNT1RFQ05JQ08iOiJBTElDT1JDMSIsIkNJUEJVUl9FTVBSRVNBIjoiQUxJQ09SUCBTLkEuQS4iLCJDSVBCVVJfRkVDSEFDQUxDVUxPIjoiMjAxNzExMjgiLCJDSVBCVVJfRkVDSEFWSUdFTkNJQSI6IjIwMTcxMTI5IiwiQ0lQQlVSX1BSRVNFTkNJQSI6MSwiQ0lQQlVSX0ZBQ1RPUiI6OTkuNDQsIkNJUEJVUl9GT1JNQURPUk1FUkNBRE8iOjAsIkNJUEJVUl9GRUNIQUlOU0NSSVBDSU9OIjoiMTk5NTA5MTUiLCJNRU5TQUpFVkFMSURBQ0lPTiI6Ik9LIn0seyJDSVBCVVJfQ09ESUdPSVNJTiI6IlVTNDY1NTZNQUw2MyIsIkNJUEJVUl9ORU1PVEVDTklDTyI6IkFBUEwiLCJDSVBCVVJfRU1QUkVTQSI6IkFBUEwiLCJDSVBCVVJfRkVDSEFDQUxDVUxPIjoiMjAxNzExMjgiLCJDSVBCVVJfRkVDSEFWSUdFTkNJQSI6IjIwMTcxMTI5IiwiQ0lQQlVSX1BSRVNFTkNJQSI6MSwiQ0lQQlVSX0ZBQ1RPUiI6NzIuNzgsIkNJUEJVUl9GT1JNQURPUk1FUkNBRE8iOjAsIkNJUEJVUl9GRUNIQUlOU0NSSVBDSU9OIjoiMjAwNzAyMTMiLCJNRU5TQUpFVkFMSURBQ0lPTiI6Ik9LIn0seyJDSVBCVVJfQ09ESUdPSVNJTiI6IkE1NFNENjVBUzREIiwiQ0lQQlVSX05FTU9URUNOSUNPIjoiQkFQLlJQUyIsIkNJUEJVUl9FTVBSRVNBIjoiQ1JFRElDT1JQIExURC4iLCJDSVBCVVJfRkVDSEFDQUxDVUxPIjoiMjAxNzExMjgiLCJDSVBCVVJfRkVDSEFWSUdFTkNJQSI6IjIwMTcxMTI5IiwiQ0lQQlVSX1BSRVNFTkNJQSI6MSwiQ0lQQlVSX0ZBQ1RPUiI6IjIwIiwiQ0lQQlVSX0ZPUk1BRE9STUVSQ0FETyI6MCwiQ0lQQlVSX0ZFQ0hBSU5TQ1JJUENJT04iOiIxOTk3MDUwMiIsIk1FTlNBSkVWQUxJREFDSU9OIjoiT0sifV19";  				
  				
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				CargaArchivoPresenciaBursatil demo= new CargaArchivoPresenciaBursatil();
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
