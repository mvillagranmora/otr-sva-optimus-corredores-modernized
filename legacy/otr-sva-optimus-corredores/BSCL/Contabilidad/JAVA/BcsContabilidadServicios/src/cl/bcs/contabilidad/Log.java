package cl.bcs.contabilidad;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

public class Log {

	private final File fileInfo;
	
	public Log(String  path) 
	{
       
		//String pathBase=properties.getProperty("pathLog");
		String pathBase = path; 
		SimpleDateFormat fm=new SimpleDateFormat("yyyyMMdd");		
		fileInfo=new File(pathBase+"logs/LOG_ServiceBROAD_"+fm.format(new Date())+".log");
	}
	
	public void EscribirLog(String tipoLog, String message)
	{
		String msg="";		
		try
		{
			msg = tipoLog+ " - " +  message +"\r\n";
			FileUtils.writeStringToFile(fileInfo,msg,true);
		}
		catch (Exception e)
		{
			
		}

	}
}
