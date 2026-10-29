package cl.bcs.cuentaInversion.jobs.generarArchivo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.opencsv.CSVWriter;

public class Archivo 
{
	private String nombreArchivo;
	private String extension;
	private String ruta;
	private FileWriter fw;  //para crear archivo TXT
    private CSVWriter cw;   //para crear archivo CSV
        
	

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public String getRuta() {
		return ruta;
	}

	public String getExtension() {
		return extension;
	}
	
	/**
	 * Constructor, setea variables 
	 * @param formatoArchivo
	 */
	public Archivo(final FormatoArchivo formatoArchivo, Connection conexion) 
	{	
		this.extension = formatoArchivo.getFormatoSalida();		
		this.nombreArchivo = generarNombreArchivo(formatoArchivo);		
		this.ruta = formatoArchivo.getRutaCompleta();
	}
	

	/**
	 * Genera el nombre de un arhivo físico, con el formato:	   
	 * <nombreArhivoSalida>_<fechaHora>.<extension>
	 */
	private String generarNombreArchivo(final FormatoArchivo formatoArchivo)
	{
		String nombreArchivo = "";
		
		try 
		{	
			Date fechaActual = new Date();
			String nombreArchivoSalida  = String.valueOf(formatoArchivo.getNombreArchivoSalida());
			
			DateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String fechaHora = formatoFecha.format(fechaActual);
			
			nombreArchivo = nombreArchivoSalida + "_"+ fechaHora +"."+ this.extension;
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return nombreArchivo;
	}
	
	
	

	/**
	 * Permite crear un archivo TXT ó CSV en la ruta indicada
	 */
	public void crear()
	{	
        try
        {	
        	String rutaCompleta = this.ruta + this.nombreArchivo;
    		File archivo = new File(rutaCompleta);
    		
    		//Valida que exista el directorio donde se creará el archivo
        	if (!new File(this.ruta).exists())
        	{
        		//throw new IOException();
        		File theDir = new File(this.ruta);
        		theDir.mkdir();
        	}
        	
    		this.fw = new FileWriter(archivo);    	    
    		if (this.extension.compareTo("CSV") == 0)
    		{	
    			this.cw = new CSVWriter(this.fw,';');
    		}
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }
	}

	/**
	 * Permite cerrar el archivo TXT ó CSV creado
	 */
	public void cerrar()
	{	
        try
        {
        	this.fw.flush();
        	this.fw.close();
    		
        	if (this.extension.compareTo("CSV") == 0)
    		{	
    			this.cw.close();
    		}
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
	}
	

	/**
	 * Inserta una nueva línea en un archico TXT 
	 * @param linea
	 */
	public void escribir(String linea)
	{	
        try
        {
    		this.fw.write(linea);
    		this.fw.write(System.lineSeparator());
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
	}		
	

	/**
	 * Inserta una nueva línea en un archico CSV 
	 * @param linea
	 */
	public void escribir(String[] linea)
	{	
        try
        {	    
			 this.cw.writeNext(linea, false); 
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
	}
}
