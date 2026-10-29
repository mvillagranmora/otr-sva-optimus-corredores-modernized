package cl.bcs.mesa.jobs;  

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;


public class GenerarArchivoOMA extends BcsJob implements JavaCall{
	private final Log log = new Log("MES.GenerarArchivoOMA");
	AtomicBoolean running=new AtomicBoolean(false); 

	
	
	@Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	

	
	
	public boolean logica(Connection conn,boolean debug) throws InterruptedException, Throwable {
    	running.set(true);
    	
      
		PreparedStatement pstmt	= null;
		CallableStatement sp	= null;
		
		File file		= null;
		FileWriter fw	= null;
		CSVWriter    cw = null;
		try {
			

			Long idEmpresa;
			String fecha;
			Long codigoComercio;
			String ruta;
			String nombre;
			String formato;
			String servicio;
			String fechaHoraDesde;
			String fechaHoraHasta;			
	        if (debug) 
	        {   //Paramatros para Debug
	        	
				idEmpresa		= 1L;
				fecha			= "2020-04-27";
				codigoComercio	= 0L;
				ruta			= "C:\\__ARCHIVO_PRUEBA\\";   // /mnt/optimus/sura/oma/
				nombre			= "OMA_Prueba.TXT";
				formato			= "TXT"; // TXT  CSV (se debe cmbir extencion del nombre de archivo)
				servicio		= "BSCL.Mesa.BD.Servicios::ConsultaOMA"; 
				fechaHoraDesde 	= "2022-01-05 01:30:00";
				fechaHoraHasta 	= "2022-01-05 01:30:00";
	        } 
	        else
	        { 
	        	//Se obtienen parametros de entrada del JOB 
				idEmpresa		= Long.parseLong(this.getParam("IdEmpresa"));
				fecha			= String.valueOf(this.getParam("Fecha"));
				codigoComercio	= Long.parseLong(this.getParam("CodigoComercio"));
				ruta			= String.valueOf(this.getParam("RutaArchivo"));
				nombre			= String.valueOf(this.getParam("NombreArchivo"));
				formato			= String.valueOf(this.getParam("FormatoArchivo"));
				servicio		= String.valueOf(this.getParam("Servicio"));  
				fechaHoraDesde  = String.valueOf(this.getParam("FechaHoraDesde"));
				fechaHoraHasta  = String.valueOf(this.getParam("FechaHoraHasta"));
	        }



        	

			
			//-----------------------------------------------
    		// Se prepara llamada al servicio
			//-----------------------------------------------
    		String callSp = String.format("CALL \"%s\"(?,?,?,?,?,?,?,?)", servicio);
    
    		sp = conn.prepareCall(callSp);
    		
    		//Se asignan parametros de entrada al servicio
    		sp.setLong(1, idEmpresa);	
    		sp.setString(2, fecha);
    		sp.setLong(3, codigoComercio);
    		sp.setString(4, fechaHoraDesde);
    		sp.setString(5, fechaHoraHasta);
    		//Se ejecuta SP
    		boolean isResultSet = sp.execute();

			if(isResultSet){
	        	//Se obtiene salida del SP
				ResultSet rs = sp.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
				int cantidadColumnas = rsmd.getColumnCount(); 
        		

				String[] datosCSV = new String[cantidadColumnas]; //Para csv necesita datos en arreglo
				String   datosTXT = "";  

		        //Se crea Archivo. Se valida exista el directorio donde se creara el archivo
	        	if (!new File(ruta).exists()){
	        		File theDir = new File(ruta);
	        		theDir.mkdir();
	    			System.out.println("GenerarArchivoOMA: Se crea directorio");
	        	}

				file = new File(ruta + nombre);
	        	fw = new FileWriter(file);

			    if (formato.compareTo("CSV") == 0){
	    			cw = new CSVWriter(fw,';');
			    }

			    
			    
			    // -------------------------------------------------------------
			    // GENERA REGISTRO 00 (Registro de encabezamiento del Archivo)
			    // -------------------------------------------------------------
			   
				while(rs.next()){
				    for(int i = 1; i <= cantidadColumnas; ++i){
				    	
					    	if (formato.compareTo("TXT") == 0)
					    		datosTXT = datosTXT + rs.getString(i);
					    	else if (formato.compareTo("CSV") == 0)			    	
					    		datosCSV[i-1] = rs.getString(i);  
				    }
				}
			    if (formato.compareTo("TXT") == 0){
			    	fw.write(datosTXT); 
		    		fw.write(System.lineSeparator());
			    }
			    else if (formato.compareTo("CSV") == 0){
					cw.writeNext(datosCSV, false);			    	
			    }

			    
			    // -------------------------------------------------------------
			    // GENERA REGISTRO 10
			    // -------------------------------------------------------------    		
				isResultSet = sp.getMoreResults();
				
				if(isResultSet){
					rs = sp.getResultSet();
					rsmd = rs.getMetaData();
					cantidadColumnas = rsmd.getColumnCount();
					
					// Genera linea de encabezado para CSV
					if (formato.compareTo("CSV") == 0)
					{
						String[] encabezadoCSV = new String[cantidadColumnas]; //Para csv necesita datos en arreglo
					
						encabezadoCSV[0]  = "Tipo Registro";
						encabezadoCSV[1]  = "Folio Planilla";
						encabezadoCSV[2]  = "Rut Cliente";				
						encabezadoCSV[3]  = "Nombre Cliente";
						encabezadoCSV[4]  = "Tipo Documento";
						encabezadoCSV[5]  = "CodigoComercio";
						encabezadoCSV[6]  = "Pais Operacion";
						encabezadoCSV[7]  = "Moneda Operacion";
						encabezadoCSV[8]  = "Monto Moneda Origen";
						encabezadoCSV[9]  = "Monto Dolar";
						encabezadoCSV[10] = "Tipo Cambio";
						encabezadoCSV[11] = "Monto Pesos";			
						encabezadoCSV[12] = "Afecto Derivados";
						encabezadoCSV[13] = "Operacion Zona Franca";
						encabezadoCSV[14] = "Tipo Autorizacion";
						encabezadoCSV[15] = "Num Autorizacion";
						encabezadoCSV[16] = "Fecha Autorizacion";
						encabezadoCSV[17] = "Codigo Entidad";
						encabezadoCSV[18] = "Feccha Planilla";
						encabezadoCSV[19] = "Numero Planilla";
						encabezadoCSV[20] = "Dolar Observado";

						cw.writeNext(encabezadoCSV, false); 
					}
					
					

					while(rs.next()){
						datosCSV = new String[cantidadColumnas]; //Para csv necesita datos en arreglo
						datosTXT = "";  
						String   valor = "";  
						String columna = "";
						
					    for(int i = 1; i <= cantidadColumnas; ++i){

					    	valor = rs.getString(i);
					    	columna = rsmd.getColumnLabel(i);
					    	
   					    	if (formato.compareTo("TXT") == 0 && !"Glosa".equals(columna))
   					    		datosTXT = datosTXT + valor;
   					    	else if (formato.compareTo("CSV") == 0)			    	
   					    		datosCSV[i-1] = valor;   					    
   					    		
					    }

					    if (formato.compareTo("TXT") == 0){
					    	fw.write(datosTXT); 
				    		fw.write(System.lineSeparator());

							if(!rs.getString("Glosa").trim().isEmpty())
							{
								fw.write(String.format("%-200s", rs.getString("Glosa")));
								fw.write(System.lineSeparator());
							}
							
					    }
					    else if (formato.compareTo("CSV") == 0){
							cw.writeNext(datosCSV, false); 
					    	
					    }
					}

				}
				
				

			    // -------------------------------------------------------------
			    // GENERA REGISTRO 99
			    // ------------------------------------------------------------- 			
				isResultSet = sp.getMoreResults();
				
				if(isResultSet)
				{
					rs = sp.getResultSet();
					rsmd = rs.getMetaData();
					cantidadColumnas = rsmd.getColumnCount();
					
	
					while(rs.next()){
						datosCSV = new String[cantidadColumnas]; //Para csv necesita datos en arreglo
						datosTXT = "";  
						String   valor = "";  
						
					    for(int i = 1; i <= cantidadColumnas; ++i){
					    	valor = rs.getString(i);
					    	
					    	if (formato.compareTo("TXT") == 0)
					    		datosTXT = datosTXT + valor;
					    	else if (formato.compareTo("CSV") == 0)			    	
					    		datosCSV[i-1] = valor;   					    
						    		
					    }
	
					    if (formato.compareTo("TXT") == 0){
					    	fw.write(datosTXT); 
				    		//fw.write(System.lineSeparator());
					    }
					    else if (formato.compareTo("CSV") == 0){
							cw.writeNext(datosCSV, false); 
					    	
					    }
					}
				}

				
				
				
    			conn.commit();
			    if (formato.compareTo("TXT") == 0)
			    	rs.close();
			    else
			    	cw.close();

			}
		} 
		catch(Exception ex){
    		System.out.println("ERROR AL GENERAR ARCHIVO");
    		ex.printStackTrace();
			throw new Throwable(ex);
    	}
		finally{
			try{
				if (fw != null) fw.close();
				if (pstmt != null) pstmt.close();
				if (conn != null) conn.close();
				
			}
			catch(Exception e){
				e.printStackTrace();
				throw new Throwable(e);
			}
		}
		
		// TODO aca debiese devolver la ruta del archivo creado para que se abra en la UI ?
		
		running.set(false);
		
		return true;
	}
	
	

	
	
	@Override
    public void onError(Throwable t) {
		log.write(":GenerarArchivoOMA:  ****** ERROR  *******");  
        log.write(t);        
        t.printStackTrace();

    }
	
	
	@Override
	public void pause() {}
	
    @Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}



    public static void main(String[] args) throws IOException { 
    	
    	System.out.println("== INICIO PRUEBA ==");
    	
        try {
        	Connection con = PlataformaUtils.getDESAConnection();
        	GenerarArchivoOMA demo = new GenerarArchivoOMA();
        	demo.logica(con,true);
        
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("== FIN PRUEBA ==");
    }
    
	
	
}