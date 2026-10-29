package cl.bcs.plataforma.jobs;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import java.util.concurrent.atomic.AtomicBoolean;

import cl.bcs.scheduler.BcsJob;


public class GenerarArchivoTasas extends BcsJob{
	
	

	public boolean transaction(Connection conexion) throws InterruptedException, Throwable {
    	       
		PreparedStatement pstmt	= null;
		CallableStatement sp	= null;
		
		File file		= null;
		FileWriter fw	= null;
		
		try {
			System.out.println("GENERAR ARCHIVO TASAS :: Inicio");
			
			//Se obtienen parametros de entrada del JOB 
			Long idEmpresa	= Long.parseLong(this.getParam("IdEmpresa"));
			String fecha	= String.valueOf(this.getParam("Fecha"));
			String fuente	= String.valueOf(this.getParam("Fuente"));
			String ruta		= String.valueOf(this.getParam("RutaArchivo"));
			String nombre	= String.valueOf(this.getParam("NombreArchivo"));
			String servicio	= String.valueOf(this.getParam("Servicio"));
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		factory.setExpandEntityReferences(false);
        	
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder builder        = factory.newDocumentBuilder();
        	Document doc                   = builder.newDocument();
        	Element results = doc.createElement("ENTRADA");
        	doc.appendChild(results);
						
    		//Se prepara llamada al servicio
    		String callSp = String.format("CALL \"%s\"(?,?,?,?,?)", servicio);
    		sp = conexion.prepareCall(callSp);
    		
    		//Se asignan parametros de entrada
    		sp.setLong(1, idEmpresa);	
    		sp.setString(2, fecha);
    		sp.setString(3, fuente);
    		
    		//Se ejecuta SP
    		boolean isResultSet = sp.execute();
    		
    		System.out.println("GENERAR ARCHIVO TASAS :: Ejecucion servicio : " + servicio + " : " + isResultSet);
    		
			if(isResultSet){
	        	//Se obtiene salida del SP
				ResultSet rs = sp.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
				int cantidadColumnas = rsmd.getColumnCount(); 
				
				while(rs.next()){
					Element row = doc.createElement("CABECERA");
					results.appendChild(row);
				    for(int i = 1; i <= cantidadColumnas; ++i){
				    	Element node = doc.createElement(rsmd.getColumnName(i));
				    	node.appendChild(doc.createTextNode(rs.getObject(i).toString()));
				    	row.appendChild(node);
				    }
				}
				rs.close();
				
				isResultSet = sp.getMoreResults();
				if(isResultSet){
					rs = sp.getResultSet();
					rsmd = rs.getMetaData();
					cantidadColumnas = rsmd.getColumnCount();
					
					while(rs.next()){
						Element row = doc.createElement("REGISTRO");
						results.appendChild(row);
					    for(int i = 1; i <= cantidadColumnas; ++i){
					    	Element node = doc.createElement(rsmd.getColumnName(i));
					    	if("NEMO".equals(rsmd.getColumnName(i)))
					    		node.appendChild(doc.createTextNode(rs.getString(i)));
					    	else
					    		node.appendChild(doc.createTextNode(String.valueOf(rs.getLong(i))));
					    	row.appendChild(node);
					    }
					}
					rs.close();
				}
			}
			
			StringWriter writer = new StringWriter();
	        OutputFormat format = new OutputFormat("xml","ISO-8859-1",true);
	        XMLSerializer serializer = new XMLSerializer(writer, format);
	        serializer.serialize(doc);
	        
	        //Se crea Archivo. Se valida exista el directorio donde se creara el archivo
        	if (!new File(ruta).exists()){
        		File theDir = new File(ruta);
        		theDir.mkdir();
        	}
        	System.out.println("GENERAR ARCHIVO TASAS :: ruta archivo generado " + ruta + nombre);
			file = new File(ruta + nombre);
        	fw = new FileWriter(file);
			fw.write(writer.getBuffer().toString());
			
			conexion.commit();
		} 
		catch(SQLException e){
			System.out.println("GenerarArchivo.main(): ERROR AL OBTENER DB CONNECTION");
			System.out.println(e.getMessage());
		}
		catch(Exception ex){
    		System.out.println("ERROR AL GENERAR ARCHIVO");
    		ex.printStackTrace();
    	}
		finally{
			try{
				if (fw != null) fw.close();
				if (pstmt != null) pstmt.close();
			}
			catch(Exception e){
			}
		}		
		return true;
	}
	
	@Override
    public void onError(Throwable t) {
        System.out.println(":GENERARARCHIVOTASAS:  ****** ERROR  *******");  
        t.printStackTrace();
    }
	
	@Override
	public void pause() {}
}