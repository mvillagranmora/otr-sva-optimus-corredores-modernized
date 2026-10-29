package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileWriter;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.interfaces.GenerarInterfazC08InformacionContable.DatoCorredor; 
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;


public class GenerarInterfazC40InformacionContable implements JavaCall{ 

	private final Log log =new Log("GenerarInterfazC40");
	private String SQL_CONSULTAR_CORREDOR = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC08InformacionContable_SelectInformacionCorredor.sql");//Consultar datos del corredor
	private final String SQL_SELECT_INTERFAZ_C40 = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC40InformacionContable_SelectInformacionContable.sql");//Consultar configuracion y saldos contables

	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGenerarInterfazC40{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String	p_Fecha;
		public String	p_Ruta;
	}
	
	public static class OutGenerarInterfazC40{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
	}
	
	public static class CuentasContables{
		public String CuentaContabilidad;
		public String NombreCuenta;
		public String TipoCuenta;
		public String CodigoSBIF;
		public String C40Imprime;
		public String C40Codigo;
		public String C40Banda;
		public BigDecimal Saldo;
		
		public String getCuentaContabilidad() {
		    return CuentaContabilidad;
		 }

		public BigDecimal getSaldo() {
		    return Saldo;
		 }
	}
	
	public static class LogEjecucion{
		public Long IdInterfaz;
		public Long NumeroRegistros;
		public Long NumeroRegistrosCorrectos;
		public Long NumeroRegistrosIncorrectos;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InGenerarInterfazC40 in=gson.fromJson(jin.toString() ,InGenerarInterfazC40.class);
		OutGenerarInterfazC40 out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	

	/**********************
	 * logica del programa
	 **********************/
	
	@SuppressWarnings("resource")
	public  OutGenerarInterfazC40 logica(Connection con, InGenerarInterfazC40 entrada) throws Exception {
		OutGenerarInterfazC40 out = new OutGenerarInterfazC40();
		//-----------------------------------------------------------------------------------------------------------------------------------
		//Formato de fechas
		SimpleDateFormat sdfyyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd"); 
		SimpleDateFormat sdfyyyyMMdd = new SimpleDateFormat("yyyyMMdd");
		 SimpleDateFormat sdfyyMMdd = new SimpleDateFormat("yyMMdd"); 	
        SimpleDateFormat sdfddMMyyyy = new SimpleDateFormat("dd/MM/YYYY"); 	// Para salida de interfaz Fecha de Listado Detalle DD/MM/YYYY
        SimpleDateFormat sdfyyyyMM = new SimpleDateFormat("YYYY-MM"); 		// Para salida de interfaz Fecha de Listado Detalle YYYY-MM
        SimpleDateFormat sdfHHmmss = new SimpleDateFormat("HH:mm:ss"); 		// Para salida de interfaz Hora de Listado Detalle HH:mm:ss        
        Date hoy = new Date();
		if (entrada.p_Fecha == null) {
			entrada.p_Fecha = sdfyyyy_MM_dd.format(hoy);
		}
		String fechaSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
		SimpleDateFormat formatFechaDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date fechaDB = formatFechaDB.parse(fechaSistema);
		SimpleDateFormat fm=new SimpleDateFormat("yyyyMMdd");
		String fecha = fm.format(fechaDB);		
		Date pFecha = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha);
		String fechayyyymmdd = sdfyyyyMMdd.format(pFecha);
		String fechayymmdd = sdfyyMMdd.format(pFecha);
		String fechaddmmyyyy  = sdfddMMyyyy.format(pFecha);
		String fechayyyymm  = sdfyyyyMM.format(pFecha);
		String horaHHmmss  = sdfHHmmss.format(hoy); 
		Calendar dia = Calendar.getInstance();
		dia.setTime(pFecha);
		//-----------------------------------------------------------------------------------------------------------------------------------

		out.p_out_NombreArchivo = "C40CBC" + fechayymmdd + ".txt";
		out.p_out_NombreArchivoLog = "C40CBC" + fechayymmdd + "DET.log";	
		out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivo = new File(out.p_out_RutaArchivo);
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		
		BufferedWriter bwArchivo;//*
		bwArchivo = new BufferedWriter(new FileWriter(archivo));
		
		BufferedWriter bwArchivoLog;
	    bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));		
		
		try{
			SQL sql=new SQL();
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("IdEmpresa", entrada.p_IdEmpresa);
		        List<CuentasContables> cuentas = sql.query(con, SQL_SELECT_INTERFAZ_C40,new HashMap<>(), CuentasContables.class);	
		        List<DatoCorredor> datoCorredor = sql.query(con, SQL_CONSULTAR_CORREDOR, params, DatoCorredor.class);

		        Map<String, BigDecimal> cuenta = cuentas.stream().collect(Collectors.toMap(CuentasContables::getCuentaContabilidad, CuentasContables::getSaldo));
		        Map<String, BigDecimal> capital2 = CalcularCapital(cuenta);
		        
		        bwArchivoLog.write("==================== Pagina 1 =================================================================");
		        bwArchivoLog.write("\r\n");
				bwArchivoLog.write(String.format("%-17s", datoCorredor.get(0).Nombre) + " CORREDORES DE BOLSA GENERACION CONTABLE GESTION C40        FECHA  : " +  String.format("%10s", fechaddmmyyyy));		
		        bwArchivoLog.write("\r\n");
				bwArchivoLog.write(" " + String.format("%-11s", datoCorredor.get(0).Rut) + "                          ===============================        HORA   : " +  String.format("%10s", horaHHmmss));		
		        bwArchivoLog.write("\r\n");
				bwArchivoLog.write("CODIGO : C40CBC                          PERIODO : " + fechayyyymm + "                   PAGINA : " + String.format("%10s", "1"));
		        bwArchivoLog.write("\r\n");
				bwArchivoLog.write("                                         ARCHIVO : " +  out.p_out_NombreArchivoLog);
		        bwArchivoLog.write("\r\n");
				bwArchivoLog.write("TIPO   CUENTA       DESCRIPCION                                                          CAPITAL");
		        bwArchivoLog.write("\r\n");
				bwArchivoLog.write("------------------------------------------------------------------------------------------------");
		        bwArchivoLog.write("\r\n");
		        bwArchivoLog.write("\r\n");
	 	        String tipoCuenta = "ACTIVO";
		        for (CuentasContables oact : cuentas) {

		        	StringBuilder linea = new StringBuilder(); 
		        	StringBuilder lineaLog = new StringBuilder();  
		        	
		        	if ( oact.CuentaContabilidad.substring(0,1).equals("1") ) {
		        		tipoCuenta = "ACTIVO";
					}else if ( oact.CuentaContabilidad.substring(0,1).equals("2") ) {
						tipoCuenta = "PASIVO";
					}
		        	 
		        	if ("S".equalsIgnoreCase(oact.C40Imprime)) {
		        		
		        		BigDecimal capital = capital2.get(oact.CuentaContabilidad);
		        		if (capital == null) {
		        			 capital = oact.getSaldo() ;
		                } 
			        	linea.append("MDD");		           							// APLI 3
			        	linea.append("1");		                   						// ITASA 1
			        	linea.append(oact.CodigoSBIF);		                       		// PARTIDA-SBIF (4)
			        	linea.append(oact.C40Banda);	 	                          	// PLAZO-BANDA (2)
			        	linea.append("999");		                     				// MONEDA (3)
			        	linea.append(String.format("%014d", capital.toBigInteger()));	//CAPITAL  
			        	linea.append(String.format("%014d", 0));    					// INTERESES (14)
			        	linea.append(String.format("%014d", 0));    					// INTPROY (14)
			        	linea.append(" ");	                          					// ESPACIO (1)
			        	linea.append(String.format("%014d", 0));	   					// REDDEV (14)
			        	linea.append(" ");	                          					// ESPACIO (14)
			        	linea.append(String.format("%014d", 0));	    				// READROY (14)
			        	linea.append(String.format("%014d", 0));	    				// COMISION (14)
			        	linea.append("1");                          					// TCOMPUTO (1)
			        	linea.append(String.format("%.7s", oact.CuentaContabilidad)); 	// CUENTA CAP (7)
			        	linea.append(oact.CuentaContabilidad.substring(oact.CuentaContabilidad.length() - 5));  // CUENTA COMIS (5)
			        	linea.append("  ");                         					// ESPACIO (2)
			        	linea.append(fechayyyymmdd);   	               					// FECHAINF (8)
			        	linea.append("02");   					   						// LIBRO 2
			        	linea.append("    ");  // BLANCO 4			        	
			        	//-----------------------------------------------------------------------------------------------------------------
			        	lineaLog.append(String.format("%-6s", tipoCuenta));
			        	lineaLog.append(" ");
			        	lineaLog.append(oact.CuentaContabilidad);
			        	lineaLog.append(" ");
			        	lineaLog.append(String.format("%-50s", oact.NombreCuenta));
			        	lineaLog.append(" ");
			        	lineaLog.append(String.format("%26s", String.format(Locale.ROOT, "%,d\r\n", capital.toBigInteger())));
						
			        	bwArchivo.write(linea.toString());
			        	bwArchivoLog.write(lineaLog.toString());
			        	bwArchivo.write("\r\n");
			        	bwArchivoLog.write("\r\n");
		        	}
		        }
		}catch(Exception e){
			log.write(e);
			out.p_out_Comentario= "Archivo no pudo ser Generado";
		}finally{
			log.writeAsJson("SALIDA",out);
			// Cierra los archivos
			if(bwArchivo !=null){
				bwArchivo.close();
			}
			if(bwArchivoLog !=null){
				bwArchivoLog.close();
			}
			 out.p_out_Comentario = "Archivo generado exitosamente";
		}
		
		return out;
	}
	
	 Map<String, BigDecimal> CalcularCapital( Map<String, BigDecimal> cuentas){
		 HashMap<String, BigDecimal> sumatoria = new HashMap<String,BigDecimal>();
		 BigDecimal saldo;
		 BigDecimal disponible = BigDecimal.ZERO;
		 BigDecimal monto;
		if(cuentas.containsKey("110100000000")) {			
			disponible = cuentas.get("110100000000");              //suma todos los saldos
			if(disponible.compareTo(BigDecimal.ZERO) < 0) { 
				sumatoria.put("110100000000",BigDecimal.ZERO);
			}else {
				sumatoria.put("210400000000",disponible);
			}
		}
		if(cuentas.containsKey("210500000000")) {			
			monto = cuentas.get("210500000000").add(disponible);
			sumatoria.put("210500000000",monto);
		}
		if( cuentas.containsKey("110404000000")|| cuentas.containsKey("110406000000") || cuentas.containsKey("110410000000") ) {			 
			saldo = cuentas.get("110404000000").add(cuentas.get("110404000000")).add(cuentas.get("110410000000"));
			sumatoria.put("110404000000",saldo);
		 }
		if(cuentas.containsKey("110701000000") || cuentas.containsKey("110702000000") || cuentas.containsKey("110790000000")) { 
			saldo = cuentas.get("110701000000").add(cuentas.get("110702000000")).add(cuentas.get("110790000000"));              //suma todos los saldos
			if(saldo.compareTo(BigDecimal.ZERO) < 0) {                                                                          //si saldo es negativo
				disponible = saldo.multiply(new BigDecimal(-1));
				saldo = cuentas.get("210400000000").add(disponible);                                                            ///acumula el valor positivo de la cuenta (110701000000 + 210400000000)
				sumatoria.put("110701000000",BigDecimal.ZERO);                                                                  /// asigma 0 a la cuenta (110701000000)
				sumatoria.put("210400000000",saldo);
			}	        		
    	}
		if(cuentas.containsKey("110801010000") || cuentas.containsKey("110801010000") || cuentas.containsKey("110801050000") || cuentas.containsKey("110801100000") || cuentas.containsKey("110801110000") || cuentas.containsKey("110802010000")) {
			saldo = cuentas.get("110801010000").add(cuentas.get("110801010000")).add(cuentas.get("110801050000")).add(cuentas.get("110801100000")).add(cuentas.get("110801110000")).add(cuentas.get("110802010000"));
			sumatoria.put("110801010000",saldo);	        		
   	    }
		if(cuentas.containsKey("210206000000") || cuentas.containsKey("210207000000") || cuentas.containsKey("210208000000")) {
			saldo = cuentas.get("210206000000").add(cuentas.get("210207000000")).add(cuentas.get("210208000000"));
			sumatoria.put("210206000000",saldo);	        		
   	    }
		if(cuentas.containsKey("210601010000") || cuentas.containsKey("210601020000") || cuentas.containsKey("210601030000") || cuentas.containsKey("210601050000")) {
			saldo = cuentas.get("210601010000").add(cuentas.get("210601020000")).add(cuentas.get("210601030000")).add(cuentas.get("210601050000"));
			sumatoria.put("210601010000",saldo);	        		
  	    }if(cuentas.containsKey("210601040000") || cuentas.containsKey("210601060000") || cuentas.containsKey("210601900000") || cuentas.containsKey("210602010000")) {
			saldo = cuentas.get("210601040000").add(cuentas.get("210601060000")).add(cuentas.get("210601900000")).add(cuentas.get("210602010000"));
			sumatoria.put("210601040000",saldo);	        		
 	    }		
		return sumatoria;		 
	 }
	

	public static void main (String [] args) {
		
        //prueba de la clase
  		try {
  			Connection con = PlataformaUtils.getDESAConnection();
  			try{  				
  				InGenerarInterfazC40 entrada= new InGenerarInterfazC40();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_Fecha	        = "2017-01-30";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazC40InformacionContable demo = new GenerarInterfazC40InformacionContable();
  				System.out.println(demo.call(con, dataJsonIn).toString());
  				
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
