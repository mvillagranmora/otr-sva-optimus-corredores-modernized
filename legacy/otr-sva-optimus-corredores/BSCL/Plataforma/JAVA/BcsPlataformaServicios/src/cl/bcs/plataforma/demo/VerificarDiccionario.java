package cl.bcs.plataforma.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class VerificarDiccionario implements JavaCall {
	private final Log log=new Log("VerificarDiccionario");
	private final Log logEspecifico=new Log("VerificarDiccionario_NoEstanEnDB");
	private final Log logEspecifico2=new Log("VerificarDiccionario_NoEstanEnDB_detalle");
	private final String SQL_DICCIONARIO = "select \"CodDiccionario\",\"Descripcion\" from \"BSCL.Plataforma.BD::PLA.Diccionario\" where \"CodLenguaje\"='CL'";
	
	public static class Diccionario {
		public String CodDiccionario;
		public String Descripcion;
	}
	
	public static class Entrada{
		public String PathProyecto;
	}
	
	public static class Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public List<String>  CodDiccionarioNoExisteDB;
		public List<DiccNoExiste>  DiccionarioNoExisteDB;
		public List<String>  CodDiccionarioSiExisteDB;
		public List<DiccionarioFile>  DiccionarioArchivos;
		public List<Diccionario>  DiccionarioDB;
		public Salida(){
			CodDiccionarioNoExisteDB = new ArrayList<String>();
			CodDiccionarioSiExisteDB = new ArrayList<String>();
			DiccionarioArchivos = new ArrayList<DiccionarioFile>();
			DiccionarioDB = new ArrayList<Diccionario>();
			DiccionarioNoExisteDB = new ArrayList<DiccNoExiste>();
		}
	}	
	
	public static class DiccionarioFile{
		public String codDiccionario;
		public String nombreArchivo;
		public String pathArchivo;
		public Integer linea;
		public Integer posicion;
	}
	
	public static class DiccNoExiste{
		public String codDiccionario;
		public String nombreArchivos;
	}

	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		Entrada in=gson.fromJson(jin.toString() ,Entrada.class);
		Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	

	public static void main(String[] args) {
		//Settings by file
        String dbURL  = "jdbc:sap://161.238.37.7:30415/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "SOPORTE";
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
        
        //Example
		try {
			Connection con = ds.getConnection();
			//---------------------------------
			Entrada entrada = new Entrada();
			entrada.PathProyecto = "C:\\BCS\\OPTIMUS_SVN\\BSCL\\PreProduccion\\BSCL\\"; 
			Gson gson = new Gson();
 			JsonParser parser = new JsonParser();
	        JsonObject dataJsonIn = parser.parse(gson.toJson(entrada)).getAsJsonObject();
	        VerificarDiccionario demo = new VerificarDiccionario();	
	        demo.call(con, dataJsonIn).toString();    	        			
		} catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}

	public static void getDirectoryContent(File dirEntrada, List<File> dirResultado) {
		File[] files = dirEntrada.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				getDirectoryContent(file,dirResultado);
			} else {
				dirResultado.add(file);
			}
		}
	}
	
	public Salida logica(Connection con, Entrada entrada) throws Exception {
		SQL sql=new SQL();
		Salida out=new Salida();
		out.P_OUT_ERRORESCONTADOR=new Long(0);
		HashMap<String,Object> parmsVacio = new HashMap<String,Object>();	
		out.DiccionarioDB = sql.query(con, SQL_DICCIONARIO,parmsVacio,Diccionario.class);
		File directory = new File(entrada.PathProyecto);
		//File[] allFiles = directory.listFiles();
		List<File> allFiles = new ArrayList<File>();
		getDirectoryContent(directory,allFiles);
		//log.writeAsJson("Lista de Archivos", allFiles);
		Pattern p1 = Pattern.compile("UI_([A-Z,_])*", Pattern.CASE_INSENSITIVE);	
		Pattern p2 = Pattern.compile("'([A-Z,_])*'");
		Pattern p3 = Pattern.compile("\"([A-Z,_])*\"");	
		for(File item : allFiles){
			if (item.getAbsolutePath().toLowerCase().contains("\\homologacion\\")) continue;
			if (item.getAbsolutePath().toLowerCase().contains("\\scripts\\")) continue;
			if (item.getAbsolutePath().toLowerCase().contains("\\script\\")) continue;
			if (item.getAbsolutePath().toLowerCase().contains("\\pruebas\\")) continue;
			if (item.getAbsolutePath().toLowerCase().contains("\\swg\\")) continue;
			if (item.getAbsolutePath().contains("Plataforma\\UI\\libs")) continue;
			if(item.getName().toUpperCase().endsWith(".JAVA") || item.getName().toUpperCase().endsWith(".JS") || item.getName().toUpperCase().endsWith(".HTML") || item.getName().toUpperCase().endsWith(".SQL")){
				System.out.println(item.getAbsolutePath());
				String line;
				int linecount = 0;
				BufferedReader bf = new BufferedReader(new FileReader(item.getAbsolutePath()));
				String codDiccionario = "";
				String mem="";
				while ((line = bf.readLine()) != null) {
				    linecount++;
				    //Patron tipico UI_
					 {
					    Matcher m = p1.matcher(line);
					    while (m.find()) {
					    	codDiccionario = m.group().replaceAll("\\\"", "").replaceAll("'", "");
					    	if(mem.contains("*"+codDiccionario+"*")==false && existe(out.DiccionarioArchivos,codDiccionario,item.getName())==false){
					    		DiccionarioFile aux = new DiccionarioFile();
						    	aux.codDiccionario = codDiccionario;
						    	aux.nombreArchivo = item.getName();
						    	aux.pathArchivo = item.getAbsolutePath();
						    	aux.linea = linecount;
						    	aux.posicion =  m.start();
						    	out.DiccionarioArchivos.add(aux);
						    	mem+="*"+codDiccionario+"* ";
					    	}
					    }
				    }
				    //Patron ' '
					if(!item.getName().toUpperCase().endsWith(".JAVA")) {
					    Matcher m = p2.matcher(line);
					    while (m.find()) {
					    	codDiccionario =  m.group().replaceAll("\\\"", "").replaceAll("'", "");
					    	if (codDiccionario.length()<=5) continue;
					     	if (!codDiccionario.contains("_")) continue;
					    	if(mem.contains("*"+codDiccionario+"*")==false && existe(out.DiccionarioArchivos,codDiccionario,item.getName())==false){
					    		DiccionarioFile aux = new DiccionarioFile();
						    	aux.codDiccionario = codDiccionario;
						    	aux.nombreArchivo = item.getName();
						    	aux.pathArchivo = item.getAbsolutePath();
						    	aux.linea = linecount;
						    	aux.posicion =  m.start();
						    	out.DiccionarioArchivos.add(aux);
						    	mem+="*"+codDiccionario+"* ";
					    	}
					    }
				    }
				    //Patron " "
					if(!item.getName().toUpperCase().endsWith(".SQL")) {
					    Matcher m = p2.matcher(line);
					    while (m.find()) {
					    	codDiccionario =  m.group().replaceAll("\\\"", "").replaceAll("'", "");
					    	if (codDiccionario.length()<=5) continue;
					     	if (!codDiccionario.contains("_")) continue;	
					    	if(mem.contains("*"+codDiccionario+"*")==false && existe(out.DiccionarioArchivos,codDiccionario,item.getName())==false){
					    		DiccionarioFile aux = new DiccionarioFile();
						    	aux.codDiccionario = codDiccionario;
						    	aux.nombreArchivo = item.getName();
						    	aux.pathArchivo = item.getAbsolutePath();
						    	aux.linea = linecount;
						    	aux.posicion =  m.start();
						    	out.DiccionarioArchivos.add(aux);
						    	mem+="*"+codDiccionario+"* ";
					    	}
					    }
				    }					
				    
				} bf.close();
			}
		}
		for(DiccionarioFile itemFile : out.DiccionarioArchivos){
			boolean match = false;
			for(Diccionario itemDB : out.DiccionarioDB){
				if(itemFile.codDiccionario.equals(itemDB.CodDiccionario)){
					match=true;
					break;
				}
			}
			if(match) out.CodDiccionarioSiExisteDB.add(itemFile.codDiccionario);
			else {
				boolean exste = false;
				for(String cod : out.CodDiccionarioNoExisteDB){
					if(cod.equals(itemFile.codDiccionario)){
						exste = true;
						break;
					}
				}
				if(!exste) out.CodDiccionarioNoExisteDB.add(itemFile.codDiccionario);
				//agrego todo el detalle para determinar el archivo donde está el item.
				DiccNoExiste aux = new DiccNoExiste();
				aux.codDiccionario = itemFile.codDiccionario;
				aux.nombreArchivos = itemFile.pathArchivo;
				out.DiccionarioNoExisteDB.add(aux);
			}
		}
		//log.writeAsJson("Salida Proceso", out);
		logEspecifico.writeAsJson("Salida Codigos diccionario no existen en BD",out.CodDiccionarioNoExisteDB);
		logEspecifico2.writeAsJson("Detalle Salida Codigos diccionario no existen en BD:",out.DiccionarioNoExisteDB);
		log.write("Fin");
		return out;
	}
	
	public boolean existe(List<DiccionarioFile> l, String n,String fn){
		for(DiccionarioFile item:l){
			if(item.codDiccionario.equals(n) && item.nombreArchivo.equals(fn)){
				return true;
			}
		}
		return false;
	}
}
