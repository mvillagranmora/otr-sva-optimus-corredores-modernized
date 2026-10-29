package cl.bcs.mesa.jobs;  

import java.io.File;
import java.io.FileWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.opencsv.CSVWriter;

import cl.bcs.mesa.jobs.utils.Archivo;
import cl.bcs.mesa.jobs.utils.FormatoArchivo;
import cl.bcs.scheduler.BcsJob;


public class GenerarArchivoAnexoIX extends BcsJob{
	
	AtomicBoolean running=new AtomicBoolean(false); 

	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
    	running.set(true);
    	
        DataSource datasource	= super.getDataSource();        
        Connection conexion		= null;
		PreparedStatement pstmt	= null;
		CallableStatement sp	= null;
		
		File file		= null;
		FileWriter fw	= null;
		CSVWriter    cw = null;
		try {
			//Se obtienen parametros de entrada del JOB 
			Long idEmpresa	    = Long.parseLong(this.getParam("IdEmpresa"));
			String fechaDesde  	= String.valueOf(this.getParam("FechaDesde"));
			String fechaHasta  	= String.valueOf(this.getParam("FechaHasta"));
			String ruta		    = String.valueOf(this.getParam("RutaArchivo"));
			String nombre	    = String.valueOf(this.getParam("NombreArchivo"));
			String formato	    = String.valueOf(this.getParam("FormatoArchivo"));
			String servicio	    = String.valueOf(this.getParam("Servicio"));
/*
	    	System.out.println("idEmpresa "+idEmpresa);
	    	System.out.println("fecha "+fecha);
	    	System.out.println("codigoComercio "+codigoComercio);
	    	System.out.println("ruta "+ruta);
	    	System.out.println("nombre "+nombre);
	    	System.out.println("servicio "+servicio);
*/
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        	factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        	factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        	factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        	factory.setExpandEntityReferences(false);
        	DocumentBuilder builder        = factory.newDocumentBuilder();
        	Document doc                   = builder.newDocument();
        	Element results = doc.createElement("ENTRADA");
        	doc.appendChild(results);
			
        	//Se obtiene conexion
			conexion = datasource.getConnection();
			FormatoArchivo Form = new FormatoArchivo(this, conexion);
			Form.cargarFormatoArchivoDetalle(conexion);

    		//Se prepara llamada al servicio
    		String callSp = String.format("CALL \"%s\"(?,?,?,?,?)", servicio);
    		sp = conexion.prepareCall(callSp);

    		//Se asignan parametros de entrada
    		sp.setLong(1, idEmpresa);	
    		sp.setString(2, fechaDesde);
    		sp.setString(3, fechaHasta);

    		//Se ejecuta SP
    		boolean isResultSet = sp.execute();

			if(isResultSet){
	        	//Se obtiene salida del SP
				ResultSet rs = sp.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
        		final FormatoArchivo formatoArchivo = new FormatoArchivo(this, conexion);
        		final Archivo archivo = new Archivo(formatoArchivo, conexion); //Configura archivo a crear
				int cantidadColumnas = rsmd.getColumnCount(); 

				String[] datosCSV = new String[cantidadColumnas]; //Para csv necesita datos en arreglo
				String   datosTXT = "";  

		        //Se crea Archivo. Se valida exista el directorio donde se creara el archivo
	        	if (!new File(ruta).exists()){
	        		File theDir = new File(ruta);
	        		theDir.mkdir();
	        	}

				file = new File(ruta + nombre);
	        	fw = new FileWriter(file);

			    if (formato.compareTo("CSV") == 0){
	    			cw = new CSVWriter(fw,';');
			    }

			    System.out.println("lala"); 
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
			    
				isResultSet = sp.getMoreResults();
				if(isResultSet){
					rs = sp.getResultSet();
					rsmd = rs.getMetaData();
					cantidadColumnas = rsmd.getColumnCount();
					while(rs.next()){
						datosCSV = new String[cantidadColumnas]; //Para csv necesita datos en arreglo
						datosTXT = "";  
						String   valor = "";  
					    for(int i = 1; i <= cantidadColumnas; ++i){
					    	if (isNumeric(rs.getString(i))){
					    		valor = rs.getString(i).replace('.', ',');
					    	}else{
					    		valor = rs.getString(i);
					    	}
					    	
					    	
   					    	if (formato.compareTo("TXT") == 0){
   					    		if (formatoArchivo.formatear(rsmd.getColumnName(i), rs.getString(i)) != ""){
   	   					    		datosTXT = datosTXT + formatoArchivo.formatear(rsmd.getColumnName(i), rs.getString(i));
   					    		}else{
   	   					    		datosTXT = datosTXT + valor;
   					    		}
   					    	}
   					    	else if (formato.compareTo("CSV") == 0)			    	
   					    		datosCSV[i-1] = valor;  
   					    	
   					    	//System.out.println(rs.getString(i).replace('.', ','));      					    
   					    		
					    }

					    if (formato.compareTo("TXT") == 0){
					    	fw.write(datosTXT); 
				    		fw.write(System.lineSeparator());
					    }
					    else if (formato.compareTo("CSV") == 0){
							cw.writeNext(datosCSV, false); 
					    	
					    }
					}
					//fw.write(writer.getBuffer().toString());
					
					conexion.commit();
				    if (formato.compareTo("TXT") == 0)
					rs.close();
				    else
	    			cw.close();
				}
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
				if (conexion != null) conexion.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		// TODO aca debiese devolver la ruta del archivo creado para que se abra en la UI ?
		
		running.set(false);
		return true;
	}
	
	public static boolean isNumeric(String str)
	{
	  return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
	@Override
    public void onError(Throwable t) {
        System.out.println(":GenerarArchivoAnexoIX:  ****** ERROR  *******");  
        t.printStackTrace();
    }
	
	@Override
	public void pause() {}
}