package cl.bcs.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Log {
	
	private final File fileInfo;
	private final String relativePath; //path relativo a RutaBaseArchivos
	private final SimpleDateFormat dfm=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	/**
	 * 
	 * @param name
	 */
	public Log(String name,String pathBase,String modulo,String mask) {
		SimpleDateFormat fm=new SimpleDateFormat(mask);
		if (modulo.length() > 0) {
			relativePath = modulo +"/LOG_"+name+"_"+fm.format(new Date())+".log";
		}
		else {
			relativePath = "/LOG_"+name+"_"+fm.format(new Date())+".log";
		}
		
		fileInfo=new File(pathBase+"/" + relativePath);
		
	}
	
	public Log(String name) {
		this(name,System.getProperty("user.home"),"","yyyyMMdd");
	}
	
	public Log(String name,String pathBase,String modulo) {
		this(name,pathBase,modulo,"yyyyMMdd");
	}
	
	public String getAbsolutePath() {
		return fileInfo.getAbsolutePath();
	}
	
	public String getRelativePath() {
		return relativePath;
	}
	
	/**
	 * 
	 * @param msg
	 */
	public void write(Object... params) {
		try {
			String msg="";
		    if (params == null) msg="[]";
		    else msg=Arrays.deepToString(params);
			String smsg=dfm.format(new Date())+" "+msg+"\r\n";
			FileUtils.writeStringToFile(fileInfo,smsg,true);
			System.out.print(smsg);
		} catch (IOException e) {
		}		
	}	
	
	
	/**
	 * 
	 * @param msg
	 */
	public void writeAsJson(String tmsg,Object obj) {		
		try {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").setPrettyPrinting().serializeNulls().create(); 			
			String msg="";
		    if (obj == null) msg=tmsg+" []";
		    else msg=tmsg+" "+gson.toJson(obj);
			String smsg=dfm.format(new Date())+" "+msg+"\r\n";
			FileUtils.writeStringToFile(fileInfo,smsg,true);
			System.out.print(smsg);
		} catch (IOException e) {
		}		
	}	
	
	/**
	 * 
	 * @param msg
	 */
	public void writeAsJson(Object obj) {
		try {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").setPrettyPrinting().serializeNulls().create(); 			
			String msg="";
		    if (obj == null) msg="[]";
		    else msg=gson.toJson(obj);
			String smsg=dfm.format(new Date())+" "+msg+"\r\n";
			FileUtils.writeStringToFile(fileInfo,smsg,true);
			System.out.print(smsg);
		} catch (IOException e) {
		}		
	}
	
	/**
	 * 
	 * @param msg
	 */
	public void write(String msg) {
		try {
			String smsg=dfm.format(new Date())+" "+msg+"\r\n";
			FileUtils.writeStringToFile(fileInfo,smsg,true);
			System.out.print(smsg);
		} catch (IOException e) {
			System.out.println(e);
		}		
	}
	

	
	/**
	 * 
	 * @param msg
	 * @param er
	 */
	public void write(String msg,Exception er) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		er.printStackTrace(ps);
		ps.close();
		write(msg+ " " +baos.toString()+"\r\n");		
	}		
	/**
	 * 
	 * @param er
	 */
	public void write(Exception er) {
		write(er.getMessage(),er);	
	}	
}
