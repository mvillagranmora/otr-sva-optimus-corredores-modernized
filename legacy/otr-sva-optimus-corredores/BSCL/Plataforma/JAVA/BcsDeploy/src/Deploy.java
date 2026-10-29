import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class Deploy {
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	
	public static HashMap<String,String> getSVNFileProperties(String filePath) throws IOException {
		
		HashMap<String, String> res = new HashMap<String,String>();
		
		Process p = Runtime.getRuntime().exec("svn info " + filePath);
		
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;
        while ((line = in.readLine()) != null) {
            //System.out.println(line);
            //System.out.flush();
            String[] tok = line.split(": ");
            if (tok.length != 2)
            	continue;
            res.put(tok[0], tok[1]);
        }
        
        return res;
		
	}
	
	public static void main(String[] args) throws SQLException, IOException,Exception {
		
		Locale.setDefault(new Locale("en", "EN"));				
		Properties prop = new Properties();
		InputStream input = new FileInputStream("config.properties");
		prop.load(input);
		//GENERAL
		String Silent=prop.getProperty("Silent")==null?"":prop.getProperty("Silent"); 
		//DESTINO:PRODUCCION
		String cnxPRO = prop.getProperty("DatabasePRO"); 
		String userPRO=prop.getProperty("UserPRO");
		String passPRO=prop.getProperty("PassPRO");
    	String PathRepoPRO=prop.getProperty("PathRepoPRO");
		//ORIGEN:DESARROLLO
		String cnxDEV = prop.getProperty("DatabaseDEV"); 
		String userDEV=prop.getProperty("UserDEV");
		String passDEV=prop.getProperty("PassDEV");		
    	String vBasePath=prop.getProperty("PathBase");  
    	String PathRepoDEV=prop.getProperty("PathRepoDEV");
    	String MasterTables=prop.getProperty("MasterTables");
    	//WEB
    	String WebHost=prop.getProperty("WebHost");
    	String WebPort=prop.getProperty("WebPort");
    	String WebUser=prop.getProperty("WebUser");
    	String WebPass=prop.getProperty("WebPass");
    	String WebRSA=prop.getProperty("WebRSA");
    	String WebPath=prop.getProperty("WebPath");
    	
    	//FIORI
    	String FioriHost=prop.getProperty("FioriHost");
    	String FioriPort=prop.getProperty("FioriPort");
    	String FioriUser=prop.getProperty("FioriUser");
    	String FioriPass=prop.getProperty("FioriPass");
    	String FioriRSA=prop.getProperty("FioriRSA");
    	String FioriPath=prop.getProperty("FioriPath");
    	String FioriMask=prop.getProperty("FioriMask");
    	
    	//WEB SERVICES
    	String WebServicesHost=prop.getProperty("WebServicesHost");
    	String WebServicesPort=prop.getProperty("WebServicesPort");
    	String WebServicesUser=prop.getProperty("WebServicesUser");
    	String WebServicesPass=prop.getProperty("WebServicesPass");
    	String WebServicesRSA=prop.getProperty("WebServicesRSA");
    	String WebServicesPath=prop.getProperty("WebServicesPath");
    	String WebServicesMask=prop.getProperty("WebServicesMask");
    	
    	String IClienteHost=prop.getProperty("IClienteHost");
    	String IClientePort=prop.getProperty("IClientePort");
    	String IClienteUser=prop.getProperty("IClienteUser");
    	String IClientePass=prop.getProperty("IClientePass");
    	String IClienteRSA=prop.getProperty("IClienteRSA");
    	String IClientePath=prop.getProperty("IClientePath");
    	String IClienteMask=prop.getProperty("IClienteMask");
    	
    	//OPCION
    	for (int i=0;i<args.length;i++) {
    		if (args[i].equals("--silent")) Silent="Y";
    		WriteLog(i+":"+args[i]); 
    	}    	
    	String option=args[0];    	
     	WriteLog("+++++++++++++++++++++++++++++++");      
     	WriteLog("*** FECHA: "+(new SimpleDateFormat("yyyyMMdd.HHmm")).format(new Date()));
     	WriteLog("*** OPTION: "+option);      	
    	WriteLog("*** DATABASE TARGET: "+cnxPRO);   
    	WriteLog("*** DATABASE SOURCE: "+cnxDEV);   
    	WriteLog("*** PATH BASE: "+vBasePath);   
     	WriteLog("+++++++++++++++++++++++++++++++");      	
     	if(!Silent.equals("Y")) {
         	System.out.print("Esta seguro de continuar, los cambios son irreversibles [Y]:");
         	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
     		String confirm = br.readLine();
     		if (!confirm.equals("Y") && !confirm.equals("y"))  System.exit(-1);  
     	}
		WriteLog("*** Obteniendo conexion a BD TARGET");		    	
    	java.sql.Connection cnnPRO = java.sql.DriverManager.getConnection(cnxPRO,userPRO, passPRO);   
    	WriteLog("*** Go!");
    	WriteLog("*** SET SCHEMA BSCL: "+ExecuteSQL(cnnPRO,"SET SCHEMA BSCL"));
		WriteLog("*** Obteniendo conexion a BD SOURCE");		    	
    	java.sql.Connection cnnDEV = java.sql.DriverManager.getConnection(cnxDEV,userDEV, passDEV);   
    	WriteLog("*** Go!");
    	WriteLog("*** SET SCHEMA BSCL: "+ExecuteSQL(cnnDEV,"SET SCHEMA BSCL"));
    	WriteLog("*** Obteniendo conexion SSH");
    	Channel channelWeb = null;
    	Channel channelWebServices = null;
    	Channel channelFiori = null;
    	Channel channelICliente = null;
    	if (WebHost!= null) {
	        JSch ssh = new JSch();		        
	        ssh.addIdentity(WebRSA, WebPass);
	        Session session = ssh.getSession(WebUser,WebHost, Integer.parseInt(WebPort));
	        java.util.Properties config = new java.util.Properties(); 
	        config.put("StrictHostKeyChecking", "no");
	        session.setConfig(config);		        
	        session.connect();
	        channelWeb = session.openChannel("sftp");
	        channelWeb.connect();
	        WriteLog("*** SSH WEB OK");
    	}
    	if (FioriHost!= null) {
	        JSch ssh = new JSch();		        
	        ssh.addIdentity(FioriRSA, FioriPass);
	        Session session = ssh.getSession(FioriUser,FioriHost, Integer.parseInt(FioriPort));
	        java.util.Properties config = new java.util.Properties(); 
	        config.put("StrictHostKeyChecking", "no");
	        session.setConfig(config);		        
	        session.connect();
	        channelFiori = session.openChannel("sftp");
	        channelFiori.connect();
	        WriteLog("*** SSH FIORI OK");
    	}
    	if (WebServicesHost!= null) {
	        JSch ssh = new JSch();		        
	        ssh.addIdentity(WebServicesRSA, WebServicesPass);
	        Session session = ssh.getSession(WebServicesUser,WebServicesHost, Integer.parseInt(WebServicesPort));
	        java.util.Properties config = new java.util.Properties(); 
	        config.put("StrictHostKeyChecking", "no");
	        session.setConfig(config);		        
	        session.connect();
	        channelWebServices = session.openChannel("sftp");
	        channelWebServices.connect();
	        WriteLog("*** SSH WEB SERVICES OK");
    	}
    	if (IClienteHost!= null) {
	        JSch ssh = new JSch();		        
	        ssh.addIdentity(IClienteRSA, IClientePass);
	        Session session = ssh.getSession(IClienteUser,IClienteHost, Integer.parseInt(IClientePort));
	        java.util.Properties config = new java.util.Properties(); 
	        config.put("StrictHostKeyChecking", "no");
	        session.setConfig(config);		        
	        session.connect();
	        channelICliente = session.openChannel("sftp");
	        channelICliente.connect();
	        WriteLog("*** SSH ICLIENTE OK");
    	}
        WriteLog("*** Go!");
    	//*******************************************
    	//hdbsql
    	//********************************************  
        if (option.equals("--sftp")){
        	WriteLog("*** Subiendo archivo SFTP "+args[1]+" "+args[2]);
			if (channelWeb!=null) {				
				try {				
					ChannelSftp sftp=(ChannelSftp)channelWeb;
					sftp.put(args[1], args[2]);
					WriteLog("SFTP: OK+");
			    } catch (SftpException e) {
			    	WriteLog("ERROR:"+e.getMessage());
			    }
			} else {
		    	WriteLog("ERROR: NO CONECTADO A SFTP");
			}
		}        	
    	//*******************************************
    	//hdbsql
    	//********************************************  
		else if (option.equals("--hdbsql")){
        	WriteLog("*** Ejecutando script de base datos "+args[1]);
        	WriteLog("*** Usando separador de instruccion como GO ");
        	WriteLog("*** Recuerde usar set schema bscl$");
        	Runtime rt = Runtime.getRuntime();
        	String ipport=cnxPRO.split("/")[2];
        	String process="hdbsql.exe -n "+ipport+" -u "+userPRO+" -p "+passPRO+" -r -z -m -c GO -f -I "+args[1]+" -o "+args[2];        	
        	try {
        	  Process pr = rt.exec(process);        		
    		  Worker worker = new Worker(pr);
    		  worker.start();
    		  try {
    		    worker.join(5*60* 1000);
    		    if (worker.exit != null) {
    		    	WriteLog("*** Terminado exit="+worker.exit);
    		    }
    		    else {
    		    	WriteLog("*** Terminado timeout");
    		    	throw new TimeoutException();
    		    }
    		  } catch(InterruptedException ex) {
    		    worker.interrupt();
    		    Thread.currentThread().interrupt();
    		    throw ex;
    		  } finally {
    		    pr.destroy();
    		  }        		
        	}catch(Exception er ) {
        		er.printStackTrace();
        		WriteLog(er.getMessage());
        	}        	
        }        
    	//*******************************************
    	//CLEAN REPO
    	//********************************************    
        else if (option.equals("--clean-repo")){
    		try {
    			WriteLog("*** Limpia todos los objetos que no se usan en el destino vs origen ");
    			
    			//Buscamos todo los ACTIVE_OBJECT en el ORIGEN
    			HashMap<String,Boolean> O_ACT_OBJ=new HashMap<String,Boolean>(); 
                Statement cs1 = cnnDEV.createStatement() ;
                String query=" SELECT  PACKAGE_ID||'|'||OBJECT_NAME||'|'||OBJECT_SUFFIX||'|'  as TKEY FROM _SYS_REPO.ACTIVE_OBJECT WHERE  package_id like 'BSCL.%' "; 
                ResultSet rset1 = cs1.executeQuery(query);  
                while(rset1.next()){
                	String tkey=rset1.getString(rset1.findColumn("TKEY"));
                	if (!O_ACT_OBJ.containsKey(tkey)) O_ACT_OBJ.put(tkey, true);
                }
                rset1.close();  
                cs1.close();
                rset1=null;
                cs1=null;
                
                                
    			//Buscamos todo los PACKAGE_CATALOG en el ORIGEN
    			HashMap<String,Boolean> O_PKG_CTLOG=new HashMap<String,Boolean>(); 
                Statement cs2 = cnnDEV.createStatement() ;
                String query2=" SELECT  PACKAGE_ID as TKEY FROM _SYS_REPO.PACKAGE_CATALOG WHERE  package_id like 'BSCL.%' "; 
                ResultSet rset2 = cs2.executeQuery(query2);  
                while(rset2.next()){
                	String tkey=rset2.getString(rset2.findColumn("TKEY"));
                	if (!O_PKG_CTLOG.containsKey(tkey)) O_PKG_CTLOG.put(tkey, true);
                }
                rset2.close();  
                cs2.close();
                rset2=null;
                cs2=null;
                
                WriteLog("---------------------------------------");                
                WriteLog("OBJECTOS  EN ORIGEN: "+O_ACT_OBJ.size());
                WriteLog("CATALOGOS EN ORIGEN: "+O_PKG_CTLOG.size());                    			
                WriteLog("---------------------------------------");
                
    			//Revisa cada uno de los ACTIVE_OBJECT destino vs Origen para borrar
                Statement cs3 = cnnPRO.createStatement() ;
                String query3=" SELECT  PACKAGE_ID||'|'||OBJECT_NAME||'|'||OBJECT_SUFFIX||'|' as TKEY,PACKAGE_ID,OBJECT_NAME,OBJECT_SUFFIX   FROM _SYS_REPO.ACTIVE_OBJECT WHERE  package_id like 'BSCL.%' "; 
                ResultSet rset3 = cs3.executeQuery(query3);  
                while(rset3.next()){
                	String tkey=rset3.getString(rset3.findColumn("TKEY"));
                	String pkg=rset3.getString(rset3.findColumn("PACKAGE_ID"));
                	String obj=rset3.getString(rset3.findColumn("OBJECT_NAME"));
                	String sfx=rset3.getString(rset3.findColumn("OBJECT_SUFFIX"));
                	if (!O_ACT_OBJ.containsKey(tkey)) {
                		WriteLog("QUITANDO OBJECT: "+tkey);                		
            			Statement cs4 = cnnPRO.createStatement() ;
            			String query4="DELETE FROM _SYS_REPO.ACTIVE_OBJECT WHERE  PACKAGE_ID = '"+pkg+"' AND OBJECT_NAME = '"+obj+"' AND OBJECT_SUFFIX = '"+sfx+"'";
            			cs4.executeUpdate(query4);            			
            			cs4.close();                        
                	}
                }
                rset3.close();  
                cs3.close();
                rset3=null;
                cs3=null;               

    			//Revisa cada uno de los PACKAGE_CATALOG destino vs Origen para borrar
                Statement cs4 = cnnPRO.createStatement() ;
                String query4=" SELECT  PACKAGE_ID as TKEY, PACKAGE_ID FROM _SYS_REPO.PACKAGE_CATALOG WHERE  package_id like 'BSCL.%' "; 
                ResultSet rset4 = cs4.executeQuery(query4);  
                while(rset4.next()){
                	String tkey=rset4.getString(rset4.findColumn("TKEY"));
                	String pkg=rset4.getString(rset4.findColumn("PACKAGE_ID"));
                	if (!O_PKG_CTLOG.containsKey(tkey)) {
                		WriteLog("QUITANDO CATALOGO: "+tkey);                		
            			Statement cs5 = cnnPRO.createStatement() ;
            			String query5="DELETE FROM _SYS_REPO.PACKAGE_CATALOG WHERE PACKAGE_ID = '"+pkg+"' ";
            			cs5.executeUpdate(query5);
            			cs5.close();                        
                	}
                }
                rset4.close();  
                cs4.close();
                rset4=null;
                cs4=null;               	
			}catch(Exception er) {
				WriteLog("CLEAN REPO: ERR"+er.getMessage());
			};
    	}       
    	else if (option.equals("--clean-database")){
    		try {
              	WriteLog("*** Eliminando SP/vv/FN en destino");
            	EliminarObjetosBD(cnnPRO); 
            	
			}catch(Exception er) {
				WriteLog("CLEAN DATABASE: ERR"+er.getMessage());
			};
    	}           	
       	//*************************************
    	//REVISA SALUD DE DESA/PRO
    	//*************************************
    	else if (option.equals("--check-dev")){
    		checkServicios(cnnDEV);    	    
    	}
    	else if (option.equals("--check-pro")){
    		checkServicios(cnnPRO);      		
    	} 
       	//*************************************
    	//SALVA DATOS
    	//*************************************
    	else if (option.equals("--save-schema-dev")){
    		saveSchema(cnnDEV,"RUEBAS");    		
    	}     	    	
    	else if (option.equals("--save-schema-pro")){
    		saveSchema(cnnPRO,"PECO");    		
    	}   
    	else if (option.startsWith("--export-data-master-table")){    	
    		java.sql.Connection conn=null;
    		if (option.startsWith("--export-data-master-table-dev")) conn= cnnDEV;
    		else conn= cnnPRO;
    		List<String> tablas=new ArrayList<String>();
			try (Statement csMA = conn.createStatement() ;) {
				try (ResultSet rsMA = csMA.executeQuery("select *  from \"BSCL.Plataforma.BD::PLA.UsoTablaSistema\" where  \"UsoTabla\" = 'MAESTRO' and  \"Estado\" = 'ACT' ORDER BY \"NombreTabla\" ");) {
					while (rsMA.next()) {
						tablas.add(rsMA.getString("NombreTabla"));
					}
				}
			}					
			for (int k=0;k<tablas.size();k++) {    
				String tabla=tablas.get(k);
				OutputStreamWriter fileMD=null;
				try {
					WriteLog("TABLA: "+tabla);
					String fOut=tabla.replace('.', '_').replace("::", "-");
					fileMD=new OutputStreamWriter(new FileOutputStream(new File("MT_DATA_"+fOut+".csv"), false), "windows-1252");
					try (Statement csLO = conn.createStatement() ;) {
						try (ResultSet rsLO = csLO.executeQuery("select * from \""+ tabla  +"\"")) {
							ResultSetMetaData md=rsLO.getMetaData();
							int nc=md.getColumnCount();
							String cols="";
							for (int i=0;i<nc;i++) {
								cols+="\""+md.getColumnName(i+1)+"\",";
							}
							fileMD.write(cols+"\r\n");
							while (nc>0 && rsLO.next()) {								
								String vals="";
								for (int i=0;i<nc;i++) {
									String tname=md.getColumnTypeName(i+1);
									String cname=md.getColumnName(i+1);
									if (tname.equalsIgnoreCase("BIGINT") || tname.equalsIgnoreCase("INT") || tname.equalsIgnoreCase("INTEGER") || tname.equalsIgnoreCase("SMALLINT")) {								
										vals+=rsLO.getLong(cname)+ ",";
									} else if (tname.equalsIgnoreCase("DECIMAL")){
										vals+=rsLO.getBigDecimal(cname)+ ",";
									} else if (tname.equalsIgnoreCase("DATE") || tname.equalsIgnoreCase("SECONDDATE") || tname.equalsIgnoreCase("TIME") || tname.equalsIgnoreCase("TIMESTAMP") ) {
										if (rsLO.getString(cname)==null) vals+="null,";
										else vals+=""+rsLO.getString(cname)+ ",";
									} else if (tname.equalsIgnoreCase("NVARCHAR") || tname.equalsIgnoreCase("VARCHAR") || tname.equalsIgnoreCase("CHAR") || tname.equalsIgnoreCase("TEXT")) {
										if (rsLO.getString(cname)==null) vals+="null,";
										else vals+="\""+rsLO.getString(cname).replace("\"", "\"\"")+ "\",";
									} else {
										if (rsLO.getObject(cname)==null) vals+="null,";
										else vals+=Base64.getEncoder().encode(rsLO.getBytes(cname))+",";										
									}
								}							
								fileMD.write(vals+"\r\n");
							}//while rows		
						}
					}
				}catch(Exception er) {
					WriteLog(" ERR: "+er.getMessage());
				}finally {
					if (fileMD!=null) fileMD.close();
					
				}
			}
    	}      	
    	else if (option.equals("--save-master-data-pro")){
    		//saveSchema(cnnPRO);    		
    	}  
    	else if (option.equals("--create-static-files")){
    		
    		File f = new File("WEB-STATIC.zip");
    		if (f.exists()) f.delete();
    		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
    		FileWriter fileWEB = new FileWriter(new File("WEB_FILES.TXT"),false);	
    		File dir = new File(vBasePath);
    		List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    		for (File file : files) {
    			String sfile=file.getCanonicalPath().replace(vBasePath, "").replace("\\", "/");
    			if (sfile.toLowerCase().contains(fixPath("/BD/Script/").toLowerCase())) continue;
	        	if (sfile.toLowerCase().contains(fixPath("/BD/Scripts/").toLowerCase())) continue;
	        	if (sfile.toLowerCase().contains(fixPath("/Pruebas/").toLowerCase())) continue;
	        	if (sfile.toLowerCase().contains("test") || sfile.toLowerCase().contains("prueba") ) continue;    	        	
	        	if (sfile.toLowerCase().endsWith("sql")) continue;	
	        	if (sfile.toLowerCase().startsWith("bscl")==false) continue;
	        	if (sfile.contains("/UI/")==false) continue;
    			fileWEB.write(sfile+"\n");
        		ZipEntry e = new ZipEntry(sfile);
        		out.putNextEntry(e);        		
        		Path path = Paths.get(file.getCanonicalPath());
        		byte[] data = Files.readAllBytes(path);
        		out.write(data, 0, data.length);
        		out.closeEntry();    			
    		}
    		out.close();
    		fileWEB.close();
    	}     	
       	//*************************************
    	//COMPARA LAS TABLAS,FK, SECUENCIAS BD
    	//*************************************
    	else if (option.equals("--compare-data-tables")){     	
    		WriteLog("\n************************************");
    		WriteLog("   GENERANDO DIFERENCIA DATOS MAESTROS");
    		WriteLog("**************************************");         		
    		compareMasterTables(cnnDEV,cnnPRO,MasterTables);    	
    	} else if (option.equals("--compare-db")){    		
            //COMPARA TABLAS
    		WriteLog("\n************************************");
    		WriteLog("   Comparando tablas de Base datos");
    		WriteLog("**************************************");
			FileWriter fileCompara = new FileWriter(new File("SCRIPT_COMPARACION.sql"),false);	
			fileCompara.write("SET SCHEMA BSCL;\n");
    		HashMap<String,ArrayList<String>> tablasMsgs=new HashMap<String,ArrayList<String>> ();
            HashMap<String,Deploy.objTable> tablasDEV=new HashMap<String,Deploy.objTable> ();            
            HashMap<String,Deploy.objTable> tablasPRO=new HashMap<String,Deploy.objTable> (); 
            HashMap<String,String> tablasFaltan=new HashMap<String,String> (); 
        	getMetaDataTable(cnnDEV, tablasDEV);  
        	getMetaDataTable(cnnPRO, tablasPRO);    
        	for (Map.Entry<String, Deploy.objTable> entry : tablasDEV.entrySet()) {
        	    String keyTaba = entry.getKey();        	    
        	    if (!tablasMsgs.containsKey(keyTaba)) 
        	    	tablasMsgs.put(keyTaba, new ArrayList<String>());
        	    if (tablasPRO.containsKey(keyTaba)) {
        	    	//Compara columnas  ta con tb
        	    	Deploy.objTable ta=tablasDEV.get(keyTaba);
        	    	Deploy.objTable tb=tablasPRO.get(keyTaba);
        	    	for (Map.Entry<String, Deploy.objColumn> entryCol : ta.objColumns.entrySet()) {
        	    		 String keyColta= entryCol.getKey();
        	    		if (tb.objColumns.containsKey(keyColta)) {
        	    			Deploy.objColumn ca= ta.objColumns.get(keyColta);
        	    			Deploy.objColumn cb= tb.objColumns.get(keyColta);
        	    			boolean tienecambios=false;
        	    			//Tipos especiales temp globales
	    	    			if (ta.is_gtemp==true && tb.is_gtemp==true && ta.is_delete!=tb.is_delete) {
	    	    				tablasMsgs.get(keyTaba).add("TABLA TEMP GLOBAL  en "+keyColta+" debe ser ON COMMIT DELETE ROWS|PRESERVE");
        	    				fileCompara.write("DROP TABLE \""+keyTaba+"\";\n");
        	    				Statement cs2 = cnnDEV.createStatement() ;
        						ResultSet rsetDef = cs2.executeQuery("CALL \"SYS\".\"GET_OBJECT_DEFINITION\"('BSCL', '\""+keyTaba+"\"')");
        						if (rsetDef.next()){						        							
        							String ssql=rsetDef.getString("OBJECT_CREATION_STATEMENT")+";\n";
        							if (ta.is_gtemp && ta.is_delete) { 
        								ssql= ssql.substring(0,ssql.indexOf(";")) + " ON COMMIT DELETE ROWS "+ ssql.substring(ssql.indexOf(";")); 
        								
        							}
        							fileCompara.write(ssql);
        						}
        						rsetDef.close();
        						cs2.close();	    	    					    	    			
	    	    				break;
	    	    			}   
        	    			//Valores columnas
        	    			if (!ca.DATA_TYPE_NAME.equals(cb.DATA_TYPE_NAME)) 	{
        	    				tablasMsgs.get(keyTaba).add("DATA_TYPE_NAME  "+cb.DATA_TYPE_NAME+" incorrecto  en "+keyColta+" en tabla en destino.Debe ser "+ca.DATA_TYPE_NAME);
        	    				tienecambios=true;
        	    			}
        	    			if (ca.LENGTH!=cb.LENGTH) 	{						
        	    				tablasMsgs.get(keyTaba).add("LENGTH  "+cb.LENGTH+" incorrecto  en "+keyColta+" en tabla  destino.Debe ser "+ca.LENGTH);
        	    				tienecambios=true;        	    				
        	    			}
        	    			if (ca.SCALE!=cb.SCALE && ca.DATA_TYPE_NAME.contains("DECIMAL")) {							
        	    				tablasMsgs.get(keyTaba).add("SCALE  "+cb.SCALE+" incorrecto  en "+keyColta+" en tabla destino.Debe ser "+ca.SCALE);
        	    				tienecambios=true; 
        	    			}
        	    			if (!ca.IS_NULLABLE.equals(cb.IS_NULLABLE)) { 				
        	    				tablasMsgs.get(keyTaba).add("IS_NULLABLE  "+cb.IS_NULLABLE+" incorrecto  en "+keyColta+" en tabla destino.Debe ser "+ca.IS_NULLABLE);
        	    				tienecambios=true; 
        	    			}        	    			
        	    			if (!(ca.DEFAULT_VALUE+"").equals(cb.DEFAULT_VALUE+"")) { 			
        	    				tablasMsgs.get(keyTaba).add("DEFAULT_VALUE  "+cb.DEFAULT_VALUE+" incorrecto  en "+keyColta+" en tabla en destino.Debe ser "+ca.DEFAULT_VALUE);
        	    				tienecambios=true;
        	    			}
        	    			if (tienecambios) {
        	    				String vnull="NULL";  if (ca.IS_NULLABLE.equals("FALSE")) vnull="NOT NULL"; 
    	    					String defaul="DEFAULT NULL";  if (ca.DEFAULT_VALUE!=null) defaul="DEFAULT '"+ca.DEFAULT_VALUE+"'";        	    					
    	    	    			//hubo cambios y es de tipo type
    	    	    			if (ta.is_type==true) {
            	    				fileCompara.write("DROP TYPE \""+keyTaba+"\";\n");
            	    				Statement cs2 = cnnDEV.createStatement() ;
            						ResultSet rsetDef = cs2.executeQuery("CALL \"SYS\".\"GET_OBJECT_DEFINITION\"('BSCL', '\""+keyTaba+"\"')");
            						if (rsetDef.next()){						
            							fileCompara.write(rsetDef.getString("OBJECT_CREATION_STATEMENT")+";\n");
            						}
            						rsetDef.close();
            						cs2.close();
            						break;	    	    				
    	    	    			}    	    					
    	    					//cambios que hay que generar
    	    	    			if (!ca.DATA_TYPE_NAME.equals(cb.DATA_TYPE_NAME))  {
    	    	    				fileCompara.write("ALTER TABLE \""+keyTaba+"\" DROP (\""+keyColta+"\");\n");
    	        	    			String tipos=ca.DATA_TYPE_NAME+ " ";
    	        	    			if (ca.DATA_TYPE_NAME.equals("NVARCHAR") || ca.DATA_TYPE_NAME.equals("VARCHAR")) tipos+=" (" + ca.LENGTH + " ) ";
    	        	    			else if (ca.DATA_TYPE_NAME.equals("DECIMAL")) tipos+=" (" + ca.LENGTH + "," + ca.SCALE +"  ) ";        	    			
    	        	    			if (ca.IS_NULLABLE.equals("FALSE")) tipos+=" not null ";
    	        	    			if (ca.DEFAULT_VALUE != null) tipos+=" default '"+ca.DEFAULT_VALUE +"' ";        	    			        	    			
    	        	    			fileCompara.write("ALTER TABLE \""+keyTaba+"\" ADD (\""+keyColta+"\" "+tipos+" );\n");      	    	    				
    	    	    			}
    	    	    			else if (ca.DATA_TYPE_NAME.contains("CHAR")) {        	    					
        	    					fileCompara.write("alter table \""+keyTaba+"\"  alter ( \""+keyColta+"\"  "+ca.DATA_TYPE_NAME+" ("+ca.LENGTH+") "+vnull+" "+defaul+"   ) ;\n");
        	    				}
        	    				else if (ca.DATA_TYPE_NAME.contains("DATE") || ca.DATA_TYPE_NAME.contains("TIME")) {
        	    					fileCompara.write("alter table \""+keyTaba+"\"  alter ( \""+keyColta+"\"  "+ca.DATA_TYPE_NAME+" "+vnull+" "+defaul+"   ) ;\n");
        	    				}
           	    				else if (ca.DATA_TYPE_NAME.contains("DECIMAL")) {
        	    					fileCompara.write("alter table \""+keyTaba+"\"  alter ( \""+keyColta+"\"  "+ca.DATA_TYPE_NAME+" ("+ca.LENGTH+","+ca.SCALE+")  "+vnull+" "+defaul+"   ) ;\n");
        	    				}        
           	    				else if (ca.DATA_TYPE_NAME.contains("BIGINT")||ca.DATA_TYPE_NAME.contains("INT")) {
        	    					fileCompara.write("alter table \""+keyTaba+"\"  alter ( \""+keyColta+"\"  "+ca.DATA_TYPE_NAME+"  "+vnull+" "+defaul+"   ) ;\n");
        	    				}          	    				
        	    				else {
        	    					WriteLog("TIPO NO IMPLEMENTADO "+ca.DATA_TYPE_NAME);
        	    				}
        	    			}
        	    		}else {      
        	    			Deploy.objColumn ca= ta.objColumns.get(keyColta);
        	    			tablasMsgs.get(keyTaba).add("! Falta columna "+keyColta+" en tabla en destino");
        	    			if (ta.is_type==false) {
	        	    			String tipos=ca.DATA_TYPE_NAME+ " ";
	        	    			if (ca.DATA_TYPE_NAME.equals("NVARCHAR") || ca.DATA_TYPE_NAME.equals("VARCHAR")) tipos+=" (" + ca.LENGTH + " ) ";
	        	    			else if (ca.DATA_TYPE_NAME.equals("DECIMAL")) tipos+=" (" + ca.LENGTH + "," + ca.SCALE +"  ) ";        	    			
	        	    			if (ca.IS_NULLABLE.equals("FALSE")) tipos+=" not null ";
	        	    			if (ca.DEFAULT_VALUE != null) tipos+=" default '"+ca.DEFAULT_VALUE +"' ";        	    			        	    			
	        	    			fileCompara.write("ALTER TABLE \""+keyTaba+"\" ADD (\""+keyColta+"\" "+tipos+" );\n");  
        	    			}else {
        	    				fileCompara.write("DROP TYPE \""+keyTaba+"\";\n");
        	    				Statement cs2 = cnnDEV.createStatement() ;
        						ResultSet rsetDef = cs2.executeQuery("CALL \"SYS\".\"GET_OBJECT_DEFINITION\"('BSCL', '\""+keyTaba+"\"')");
        						if (rsetDef.next()){						
        							fileCompara.write(rsetDef.getString("OBJECT_CREATION_STATEMENT")+";\n");
        						}
        						rsetDef.close();
        						cs2.close();
        						break;
        	    			}
        	    		}
        	    	}//fin loop compara columnas 1..n
        	    	//Compara columnas  que faltan quitar en tb con ta  
        	    	tb=tablasDEV.get(keyTaba);
        	    	ta=tablasPRO.get(keyTaba);
        	    	for (Map.Entry<String, Deploy.objColumn> entryCol : ta.objColumns.entrySet()) {
        	    		 String keyColta= entryCol.getKey();
        	    		if (!tb.objColumns.containsKey(keyColta)) {        	    			
        	    			tablasMsgs.get(keyTaba).add("! Falta eliminar columna "+keyColta+" en tabla en destino");
        	    			fileCompara.write("ALTER TABLE \""+keyTaba+"\" DROP (\""+keyColta+"\");\n");
        	    		}
        	    	}        	    	        	    	
        	    }else {
        	    	Deploy.objTable ta=tablasDEV.get(keyTaba);
        	    	tablasMsgs.get(keyTaba).add("! Falta tabla en destino");
        	    	if (!tablasFaltan.containsKey(tablasFaltan)) tablasFaltan.put(keyTaba, "!");
					Statement cs2 = cnnDEV.createStatement() ;
					ResultSet rsetDef = cs2.executeQuery("CALL \"SYS\".\"GET_OBJECT_DEFINITION\"('BSCL', '\""+keyTaba+"\"')");
					if (rsetDef.next()){				
						String ssql=rsetDef.getString("OBJECT_CREATION_STATEMENT")+";\n";
						if (ta.is_gtemp && ta.is_delete) { 
							ssql= ssql.substring(0,ssql.indexOf(";")) + " ON COMMIT DELETE ROWS "+ ssql.substring(ssql.indexOf(";")); 
							
						}
						fileCompara.write(ssql);
					}
					rsetDef.close();
					cs2.close();
        	    }
        	    fileCompara.flush();
        	}  
        	SortedSet<String> keys = new TreeSet<String>(tablasMsgs.keySet());
        	for (String key: keys) {
        		if (tablasMsgs.get(key).size()==0) continue;
        		WriteLog("\nTABLA: "+key);
        		for (String v:tablasMsgs.get(key)) {
        			WriteLog("    "+v);        			
        		}        		
        	}        	
        	//COMPARA PK, UNIQUE
        	{
        		WriteLog("\n************************************");
        		WriteLog("   Buscando PK/UK diferentes");
        		WriteLog("**************************************");        		
	    		HashMap<String,String> tablasPKMsgs=new HashMap<String,String> ();
	            HashMap<String,Deploy.objPKUK> inxDEV=new HashMap<String,Deploy.objPKUK> ();            
	            HashMap<String,Deploy.objPKUK> inxPRO=new HashMap<String,Deploy.objPKUK> ();            
	        	getMetaDataPKUK(cnnDEV, inxDEV);  
	        	getMetaDataPKUK(cnnPRO, inxPRO);          	
	        	for (Map.Entry<String, Deploy.objPKUK> entry : inxDEV.entrySet()) {//Lo que no esta en B
	        	    String keySQa = entry.getKey();
	        	    if (!inxPRO.containsKey(keySQa) && !tablasFaltan.containsKey(entry.getValue().TABLE_NAME)) {	
	        	    	String tabname=entry.getValue().TABLE_NAME;	        	    		        	    	
	        	    	if (!tablasPKMsgs.containsKey(tabname)) {
	        	    		tablasPKMsgs.put(tabname, tabname);
	        	    	}	        	    		        	    	
	        	    }
	        	}
	        	SortedSet<String> pkkeys = new TreeSet<String>(tablasPKMsgs.keySet());
	        	for (String key: pkkeys) {
	        		WriteLog("    "+key);     		
	        	}	        		        	
        	}            	
        	//COMPARA INDICES
        	{
        		
        		WriteLog("\n************************************");
        		WriteLog("   Comparando INDEX diferentes");
        		WriteLog("**************************************");            		
	            HashMap<String,Deploy.objINX> inxDEV=new HashMap<String,Deploy.objINX> ();            
	            HashMap<String,Deploy.objINX> inxPRO=new HashMap<String,Deploy.objINX> ();            
	        	getMetaDataINX(cnnDEV, inxDEV);  
	        	getMetaDataINX(cnnPRO, inxPRO);          	
	        	for (Map.Entry<String, Deploy.objINX> entry : inxDEV.entrySet()) {//Lo que no esta en B
	        	    String keySQa = entry.getKey();
	        	    if (!inxPRO.containsKey(keySQa)) {		    			
	        	    	WriteLog("    ! Falta INDEX "+keySQa+" en destino");
	        	    }
	        	}
	        	for (Map.Entry<String, Deploy.objINX> entry : inxPRO.entrySet()) {//Lo que esta en B pero no en A
	        	    String keySQa = entry.getKey();
	        	    if (!inxDEV.containsKey(keySQa)) {	    			
	        	    	WriteLog("    INDEX "+keySQa+" se encuentra en destino pero no en origen");
	        	    }
	        	}    
        	}             	
        	//COMPARA FK
        	{
        		WriteLog("\n************************************");
        		WriteLog("   Comparando FKs diferentes");
        		WriteLog("**************************************");            		
	            HashMap<String,Deploy.objFK> fkDEV=new HashMap<String,Deploy.objFK> ();            
	            HashMap<String,Deploy.objFK> fkPRO=new HashMap<String,Deploy.objFK> ();            
	        	getMetaDataFK(cnnDEV, fkDEV);  
	        	getMetaDataFK(cnnPRO, fkPRO);          	
	        	for (Map.Entry<String, Deploy.objFK> entry : fkDEV.entrySet()) {//Lo que no esta en B
	        	    String keyFKa = entry.getKey();
	        	    if (fkPRO.containsKey(keyFKa)) {
		    			Deploy.objFK fka= fkDEV.get(keyFKa);
		    			Deploy.objFK fkb= fkPRO.get(keyFKa);	    			
		    			if (!fka.TABLE_NAME.equals(fkb.TABLE_NAME)) WriteLog("  SCHEMA: FK "+keyFKa+" distinta TABLE_NAME en destino PRO");
		    			if (!fka.COLUMN_NAME.equals(fkb.COLUMN_NAME)) WriteLog("  SCHEMA: FK "+keyFKa+" distinta  COLUMN_NAME en destino PRO");
		    			if (!fka.REFERENCED_TABLE_NAME.equals(fkb.REFERENCED_TABLE_NAME)) WriteLog("  SCHEMA: FK "+keyFKa+" distinta REFERENCED_TABLE_NAME en destino PRO");
		    			if (!fka.REFERENCED_COLUMN_NAME.equals(fkb.REFERENCED_COLUMN_NAME)) WriteLog("  SCHEMA: FK "+keyFKa+" distinta REFERENCED_COLUMN_NAME en destino PRO");
	        	    }else {
	        	    	WriteLog("  SCHEMA: Falta FK "+keyFKa+" en destino PRO");
	        	    }
	        	}
	        	for (Map.Entry<String, Deploy.objFK> entry : fkPRO.entrySet()) {//Lo que esta en B pero no en A
	        	    String keyFKa = entry.getKey();
	        	    if (!fkDEV.containsKey(keyFKa)) {	    			
	        	    	WriteLog("  SCHEMA: FK "+keyFKa+" se encuentra en destino PRO pero no DEV");
	        	    }
	        	}
        	}        	                       		
        	//COMPARA SECUENCES
        	{
        		WriteLog("\n************************************");
        		WriteLog("   Comparando SECUENCES diferentes");
        		WriteLog("**************************************");    	            
	            HashMap<String,Deploy.objSQ> sqDEV=new HashMap<String,Deploy.objSQ> ();            
	            HashMap<String,Deploy.objSQ> sqPRO=new HashMap<String,Deploy.objSQ> ();            
	        	getMetaDataSQ(cnnDEV, sqDEV);  
	        	getMetaDataSQ(cnnPRO, sqPRO);          	
	        	for (Map.Entry<String, Deploy.objSQ> entry : sqDEV.entrySet()) {//Lo que no esta en B
	        	    String keySQa = entry.getKey();
	        	    if (!sqPRO.containsKey(keySQa)) {		    			
	        	    	WriteLog("    Falta SEQUENCE "+keySQa+" en destino");
	        	    	fileCompara.write("CREATE SEQUENCE \""+keySQa+"\" START     WITH 1   INCREMENT BY 1   MINVALUE  1;\n");	        	    		        	    	
	        	    }
	        	}
	        	for (Map.Entry<String, Deploy.objSQ> entry : sqPRO.entrySet()) {//Lo que esta en B pero no en A
	        	    String keySQa = entry.getKey();
	        	    if (!sqDEV.containsKey(keySQa)) {	    			
	        	    	WriteLog("    SEQUENCE "+keySQa+" se encuentra en destino pero no en origen");
	        	    	fileCompara.write("DROP SEQUENCE \""+keySQa+"\";\n");	        	    		        	    		        	    	
	        	    }
	        	}    
        	}  
        	//Fin Archivo
        	fileCompara.flush();
        	fileCompara.close();
    	}        	    	
    	//*******************************************
    	//SCRIPTS PARA ACTULIZAR CON ULTIMOS CAMBIOS
    	//********************************************    	 
    	else if (option.equals("--install-multi-file")){
    		try {
    			WriteLog("*** install-multi-file ****");
		    	String fileread=args[1];
		    	ArrayList<String> archivosSta=new ArrayList<String>();
		    	ArrayList<String> serviciosDesSQL=new ArrayList<String>();
		    	HashMap<String,String> servArchivo=new HashMap<String,String>();
		    	try (BufferedReader br = new BufferedReader(new FileReader(fileread))) {
		    	    String sfile;
		    	    while ((sfile = br.readLine()) != null) {
		    	    	
			    		if (sfile.length()>5) {
			    			sfile=sfile.replace("\\","/");
			    			sfile=sfile.trim();
			    			sfile=sfile.replace("/BSCL/Desarrollo/","");
			    			sfile=sfile.replace("/BSCL/QA/","");
			    			sfile=sfile.replace("/BSCL/PreProduccion/","");
			    			sfile=sfile.replace("/BSCL/Produccion/","");			    			
				        	if (sfile.toLowerCase().contains("test") 
				        		|| sfile.toLowerCase().contains("prueba")  
				        		|| sfile.toLowerCase().contains("/Scripts/")){
				        		WriteLog("Linea omitida:"+sfile);
				        		continue;
				        	}
				        	//comtabilidad para workspace desarrollo
				        	if (sfile.startsWith("Optimus/"))  sfile=sfile.replace("Optimus/","BSCL/");
				        	if (sfile.startsWith("/Optimus/"))  sfile=sfile.replace("/Optimus/","BSCL/");
				        	//Asi debe ir
				        	sfile=sfile.replace("/BSCL/","BSCL/");
				        	WriteLog("Linea a procesar:"+sfile);
				        	String spath=fixPath(vBasePath+sfile);
				        	//valida archivo
				        	File fi=new File(spath);
				        	if (fi.isDirectory()) continue; 
				        	if (!fi.exists()) {
				        		WriteLog("Linea omitida - FILE NOT FOUND:"+sfile);
				        		WriteLog("SE REQUIERE REPROCESAR:\t"+sfile);
				        		continue;
				        	}				        	
				        	//caso archivo
				        	if (sfile.toLowerCase().endsWith("sql")) {
				        		//valida script con 
				        		boolean validSQL=false;
				        		boolean badnames=false;
				        		String lineSQL1="";
				        		int numInstr=0;
				        		try {
				        			String vScript[]=readFileScript(spath);
				        			if (vScript.length>2) {
				        				if (vScript[2].length()>80) lineSQL1=vScript[2].substring(0, 80);
				        				else lineSQL1=vScript[2].substring(0, vScript[2].length());
				        			}
				        			numInstr=vScript.length;
				        			if ((vScript.length==3 || vScript.length==4) 
				        					&& vScript[1].toUpperCase().contains("DROP")
				        					&& vScript[2].toUpperCase().contains("CREATE")) {
				        				String sqlDropName=getNombreBDScript(vScript[1]);
				        				String sqlCreaName=getNombreBDScript(vScript[2]);
				        				String sqlName=sqlCreaName.replace("::", ".")+".SQL";
				        				String filename=sfile.replace("/",".").replace("\\", ".");
				        				lineSQL1=sqlName;
				        				if (!sqlCreaName.equals(sqlDropName)) badnames=true;				        				
				        				if (sqlCreaName.equals(sqlDropName) && sqlName.equalsIgnoreCase(filename)) {
				        					validSQL=true;
				        				}
				        			}
				        		}catch(Exception er) { 
				        			WriteLog("Linea omitida - EXCEPTION: "+sfile+" => "+er.getMessage());
				        			WriteLog("SE REQUIERE REPROCESAR:\t"+sfile);
				        		}
				        		if (validSQL) {
					        		String srv=fileName2Srv(sfile);
					        		if (!servArchivo.containsKey(srv)) servArchivo.put(srv, sfile);
					        		serviciosDesSQL.add(srv);				        			
				        		}else {
				        			if (numInstr==3 || numInstr == 4)  {
				        				if (badnames)  WriteLog("Linea omitida - SP != FILE: "+sfile+" DROP!=CREATE ");
				        				else WriteLog("Linea omitida - SP != FILE: "+sfile+" != "+lineSQL1);				        				
				        			}
				        			else WriteLog("Linea omitida - SP != FILE: "+sfile+" != "+lineSQL1 + " SE ESPERAN 3 o 4 instrucciones set, drop, create y [grant]");				        			
				        			WriteLog("SE REQUIERE REPROCESAR:\t"+sfile);				        			
				        		}
				        	}else {
				        		archivosSta.add(sfile);
				        	}
			    		}
		    	    }
		    	    WriteLog(" ARCHIVOS SQL:"+serviciosDesSQL.size());
		    	    WriteLog(" ARCHIVOS ESTATICOS:"+archivosSta.size());		    	    
		    	    //Ordena jerarquicamente servicios
		    	    ArrayList<String> serviciosOrdSQL=OrdenaJerarquicamente(cnnDEV,serviciosDesSQL);
		    	    //Primero van piezas SQL
		    	    ArrayList<String> archivos=new ArrayList<String>();
		    	    for (int i=0;i<serviciosOrdSQL.size();i++)  {
		    	    	archivos.add(servArchivo.get(serviciosOrdSQL.get(i)));
		    	    }
		    	    //Luego van piezas no SQL
		    	    for (String file:archivosSta) { 
		    	    	archivos.add(file);	
		    	    }
		    	    //Inicia el despliegue
		    	    int numproceso=0;
		    	    HashMap<String, String> svnProps = null;
		    	    for (String script:archivos) {
		    	    	numproceso++;
		    			WriteLog("*** ["+numproceso+"/"+archivos.size()+"] Actualizando archivo:"+script);
		    			boolean error=true;
		    			try {
		    				
		    				svnProps = getSVNFileProperties(vBasePath+script);
		    				String URL = svnProps.get("URL");
		    				Channel channel;
		    				String path;
		    				if (URL != null && URL.contains("/" + WebServicesMask + "/")) {
		    					path = WebServicesPath;
		    					channel = channelWebServices;
		    				}
		    				else if (URL != null && URL.contains("/" + FioriMask + "/")) {
		    					path = FioriPath;
		    					channel = channelFiori;
		    				}
		    				else if (URL != null && URL.contains("/" + IClienteMask + "/")) {
		    					path = IClientePath;
		    					channel = channelICliente;
		    				}
		    				else {
		    					path = WebPath;
		    					channel = channelWeb;
		    				}
		    				
		    				error=!deployFile(vBasePath, cnnPRO, script,(ChannelSftp) channel,path);	
		    				
		    			}catch(Exception er) {
		    				System.out.println(er.toString());
		    				WriteLog("SE REQUIERE REPROCESAR:\t"+er.toString());
		    			}
		    			if (error==true) {
		    				WriteLog("SE REQUIERE REPROCESAR:\t"+script);
		    			}
		    	    }
		    	    SpInValidate(cnnPRO);
		    	    updateVersion(cnnPRO);
		    	}
    		}catch(Exception er) {
    			WriteLog("ERROR"+er.getMessage());
    			er.printStackTrace();
    			System.exit(-1);
    		}
    	}        	
    	else if (option.equals("--diff-list")){
			FileWriter filediff = new FileWriter(new File("DIFF_LIST.TXT"),false);							
            String query2="SELECT PACKAGE_ID,OBJECT_NAME,OBJECT_SUFFIX,BDATA,CDATA  FROM _SYS_REPO.ACTIVE_OBJECT WHERE  PACKAGE_ID = ? AND OBJECT_NAME = ? AND OBJECT_SUFFIX = ?  ";            
            PreparedStatement cspro=cnnPRO.prepareStatement(query2) ;             
    		//String query="SELECT PACKAGE_ID,OBJECT_NAME,OBJECT_SUFFIX,BDATA,CDATA  FROM _SYS_REPO.ACTIVE_OBJECT WHERE  package_id like 'BSCL%' and object_suffix not in ('xshttpdest','xsprivileges')  and OBJECT_NAME like '%bcsapp'  ";
    		String query="SELECT PACKAGE_ID,OBJECT_NAME,OBJECT_SUFFIX,BDATA,CDATA  FROM _SYS_REPO.ACTIVE_OBJECT WHERE  package_id like 'BSCL%' and object_suffix not in ('xshttpdest','xsprivileges','project')  ";
    		Statement cs1=cnnDEV.createStatement() ;    
            ResultSet rset1 = cs1.executeQuery(query);                         
            while(rset1.next()){ 
               	String pkg=rset1.getString(rset1.findColumn("PACKAGE_ID"));
            	String name=rset1.getString(rset1.findColumn("OBJECT_NAME"));
            	String suffix=rset1.getString(rset1.findColumn("OBJECT_SUFFIX"));           	
            	String spath= pkg.replace(".", "/");
            	String sfile = fixPath(spath + "/" + name + "."+suffix);            	
            	if (sfile.toLowerCase().contains(fixPath("/BD/Script/").toLowerCase())) continue;
	        	if (sfile.toLowerCase().contains(fixPath("/BD/Scripts/").toLowerCase())) continue;
	        	if (sfile.toLowerCase().contains(fixPath("/Pruebas/").toLowerCase())) continue;    
	        	//Valida archivo de origen se encuentre en  carpeta
	        	String fullFile= fixPath(vBasePath+"/"+sfile);
	        	File ffullfile=new File(fullFile);
	        	if (!ffullfile.isFile()) continue;
            	//---------------
	        	System.out.println("->"+sfile);
            	byte[] bdata= rset1.getBytes(rset1.findColumn("BDATA"));
            	String cdata= rset1.getString(rset1.findColumn("CDATA"));      
    			if (bdata==null) bdata=cdata.getBytes("UTF-8");            	
            	//---------------
            	cspro.setString(1, pkg);
            	cspro.setString(2, name);
            	cspro.setString(3, suffix);            	
                ResultSet rset2 = cspro.executeQuery();   
                boolean modificado=true;
                if (rset2.next()) {
                	byte[] bdataP= rset2.getBytes(rset2.findColumn("BDATA"));
                	String cdataP= rset2.getString(rset2.findColumn("CDATA"));      
        			if (bdataP==null) bdataP=cdataP.getBytes("UTF-8");   
        			if (suffix.equalsIgnoreCase("sql")||suffix.equalsIgnoreCase("html")||suffix.equalsIgnoreCase("js")||suffix.equalsIgnoreCase("java")) {
        				String tx1=new String(bdata,"UTF-8");
        				String tx2=new String(bdataP,"UTF-8");        				
            			if (tx1.equals(tx2)==true||Arrays.equals(bdata, bdataP) ==true) {  
            				WriteLog("*** IGUAL:"+sfile); modificado=false;     
            				}
            			else {
            				WriteLog("*** DIFF:"+sfile+"\t"+tx1.length() +"!="+tx2.length());             				
            				}        				
        			}else {
            			if (Arrays.equals(bdata, bdataP)) { 
            				WriteLog("*** IGUAL:"+sfile); modificado=false;     
            				}
            			else {
            				WriteLog("*** DIFF:"+sfile+"\t"+bdata.length +"!="+bdataP.length);      
            				}        				        				
        			}
                }else {
                	WriteLog("*** DIFF:"+sfile+"\t"+bdata.length +" NUEVO "); 
                }
                rset2.close();   
                if (modificado) {
                	filediff.write(sfile+"\r\n");
                	filediff.flush();
                }
            }
            cspro.close();
            rset1.close();
            cs1.close();        
            filediff.close();    	    		    		    	                     
    	}else {
    		WriteLog("*** COMANDO INCORRECTO");
    		System.exit(-1);
    	}
    	//---------------------------------------------
    	WriteLog("*** PROGRAMA TERMINADO "); 
    	System.exit(0);
	}
	
	public static String getNombreBDScript(String script) {
		String scriptwc=script;
		scriptwc = scriptwc.replaceAll("(?:--.*)","");		
		String xname=scriptwc.replaceFirst(" \"BSCL\".", " ");
		xname=xname.replaceFirst(" ", " ");
		xname=xname.replaceFirst(" ", " ");
		int posI=xname.indexOf("\""); 
		int posF=xname.indexOf("\"",posI+1); 
		return xname.substring(posI+1, posF);		
	}
	
	public static boolean checkServicios(java.sql.Connection cnnDEV) throws SQLException, IOException {
		int ninv=0;	
		{
			WriteLog("\n*** Revisando SP de la tabla Servicio se encuentren mal compilados");
			Statement csInv = cnnDEV.createStatement() ;
			ResultSet rsInv = csInv.executeQuery("select srv.\"RutaServicio\",ifnull(is_valid,'FALSE') as IS_VALID from \"BSCL.Plataforma.BD::PLA.Servicio\"  as srv left join sys.procedures as proc on srv.\"RutaServicio\" = proc.procedure_name and proc.schema_name = 'BSCL' where srv.\"TipoServicio\"='P' ORDER BY srv.\"RutaServicio\" ");			
			while(rsInv.next()){
				if (rsInv.getString(rsInv.findColumn("IS_VALID")).equalsIgnoreCase("FALSE")) {
			    	WriteLog("     "+rsInv.getString(rsInv.findColumn("RutaServicio")));
			    	ninv++;
				}
			}
			rsInv.close();
			csInv.close();
		}
		{
    		WriteLog("\n*** Revisando objetos en BD que se encuentran Invalidos");
    		Statement csInv = cnnDEV.createStatement() ;
            ResultSet rsInv = csInv.executeQuery("select function_name as SERVICIO from sys.functions where is_valid = 'FALSE' and schema_name = 'BSCL' union select procedure_name as SERVICIO from sys.procedures where is_valid = 'FALSE' and schema_name = 'BSCL' union select view_name as SERVICIO from sys.views  where is_valid = 'FALSE' and schema_name = 'BSCL' order by SERVICIO");           
            while(rsInv.next()){
            	WriteLog("     "+rsInv.getString(rsInv.findColumn("SERVICIO")));
            	ninv++;
            }
            rsInv.close();
            csInv.close();   
		}		
		return true;
	}


	public static String quitarAcentos(String string) {
		if(string == null)return null;
	    return Normalizer.normalize(string, Form.NFD)
	        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
	
	public static void compareMasterTables(java.sql.Connection cnnDEV,java.sql.Connection cnnPRO,String MasterTables) throws SQLException, IOException {		
		WriteLog("\n*** Tablas para comparar  que se replican A --> B");
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream("TABLAS_MAESTRAS.ZIP"));
		{
			ArrayList<String> tablasReplica=new ArrayList<String>();			
			HashMap<String,OutputStreamWriter>  filesCmp= new HashMap<String,OutputStreamWriter>();
			
			try (Statement csMA = cnnDEV.createStatement() ;) {
				try (ResultSet rsMA = csMA.executeQuery("select * from \"BSCL.Plataforma.BD::PLA.UsoTablaSistema\" where  \"UsoTabla\" = 'MAESTRO' and  \"Estado\" = 'ACT' AND \"SoloReplicar\" = 'S' ORDER BY \"NombreTabla\" ");) {
					while (rsMA.next()) {
						tablasReplica.add(rsMA.getString("NombreTabla"));
					}
				}
			}					
			for (int k=0;k<tablasReplica.size();k++) {
				String tabla=tablasReplica.get(k);
				try (Statement csLO = cnnDEV.createStatement() ;) {
					try (ResultSet rsLO = csLO.executeQuery("select * from \""+ tabla  +"\"")) {
						ResultSetMetaData md=rsLO.getMetaData();
						int nc=md.getColumnCount();
						WriteLog("      -> "+tabla+"  COLS "+nc);
						
						String fOut=tabla.substring(0,tabla.lastIndexOf(".")).replace('.', '_').replace("::", "-");
						if (!filesCmp.containsKey(fOut)) {
							filesCmp.put(fOut, new OutputStreamWriter(new FileOutputStream(new File("MT_REP_"+fOut+".sql"), false), "windows-1252"));
							filesCmp.get(fOut).write("SET SCHEMA \"BSCL\"; \r\n \r\n");
						}						
						filesCmp.get(fOut).write("DO BEGIN AUTONOMOUS TRANSACTION \r\n");
						filesCmp.get(fOut).write("  DELETE FROM \""+ tabla  +"\"; \r\n ");														
						while (nc>0 && rsLO.next()) {								
							filesCmp.get(fOut).write("  INSERT INTO \""+ tabla  +"\"  ( ");
							//columnas
							String cols="";
							for (int i=0;i<nc;i++) {
								String tname=md.getColumnTypeName(i+1);
								String cname=md.getColumnName(i+1);
								if (tname.equalsIgnoreCase("BIGINT") || tname.equalsIgnoreCase("INT") || tname.equalsIgnoreCase("INTEGER") || tname.equalsIgnoreCase("SMALLINT")) {
									cols+="\""+cname+"\",";
								} else if (tname.equalsIgnoreCase("DECIMAL")){
									cols+="\""+cname+"\",";
								} else if (tname.equalsIgnoreCase("DATE") || tname.equalsIgnoreCase("SECONDDATE") || tname.equalsIgnoreCase("TIME") || tname.equalsIgnoreCase("TIMESTAMP") ) {
									cols+="\""+cname+"\",";
								} else if (tname.equalsIgnoreCase("NVARCHAR") || tname.equalsIgnoreCase("VARCHAR") || tname.equalsIgnoreCase("CHAR") || tname.equalsIgnoreCase("TEXT")) {
									cols+="\""+cname+"\",";
								} 
							}
							filesCmp.get(fOut).write(cols.substring(0,cols.length()-1)+ ") VALUES ( ");						
							//valores
							String vals="";
							for (int i=0;i<nc;i++) {
								String tname=md.getColumnTypeName(i+1);
								String cname=md.getColumnName(i+1);
								if (tname.equalsIgnoreCase("BIGINT") || tname.equalsIgnoreCase("INT") || tname.equalsIgnoreCase("INTEGER") || tname.equalsIgnoreCase("SMALLINT")) {								
									vals+=rsLO.getLong(cname)+ ",";
								} else if (tname.equalsIgnoreCase("DECIMAL")){
									vals+=rsLO.getBigDecimal(cname)+ ",";
								} else if (tname.equalsIgnoreCase("DATE") || tname.equalsIgnoreCase("SECONDDATE") || tname.equalsIgnoreCase("TIME") || tname.equalsIgnoreCase("TIMESTAMP") ) {
									if (rsLO.getString(cname)==null) vals+="null,";
									else vals+="'"+rsLO.getString(cname)+ "',";
								} else if (tname.equalsIgnoreCase("NVARCHAR") || tname.equalsIgnoreCase("VARCHAR") || tname.equalsIgnoreCase("CHAR") || tname.equalsIgnoreCase("TEXT")) {
									if (rsLO.getString(cname)==null) vals+="null,";
									else vals+="'"+rsLO.getString(cname).replace("'", "''")+ "',";
								} 
							}							
							filesCmp.get(fOut).write(vals.substring(0,vals.length()-1)+"); \r\n ");
						}//while rows
						filesCmp.get(fOut).write("COMMIT; \r\n END; \r\n \r\n \r\n");
						filesCmp.get(fOut).flush();							
					}
				}catch(Exception er) {
					WriteLog("ERROR: "+er.getMessage());
					er.printStackTrace();
				}				
			}//while tablas
			
			//close files && ZIP
			for (Map.Entry<String,OutputStreamWriter> entry : filesCmp.entrySet()) {
				entry.getValue().close(); 
				File file=new File("MT_REP_"+entry.getKey()+".sql");
        		ZipEntry e = new ZipEntry(file.getName());
        		zout.putNextEntry(e);        		
        		Path path = Paths.get(file.getCanonicalPath());
        		byte[] data = Files.readAllBytes(path);
        		zout.write(data, 0, data.length);
        		zout.closeEntry();   
        		file.delete();
			}		
		}
			
					
					
		WriteLog("\n*** Tablas para comparar  registros nuevos A* --> B");
		{
			ArrayList<String> tablasNuevas=new ArrayList<String>();
			HashMap<String,String> mapa=new HashMap<String,String>();			
			HashMap<String,OutputStreamWriter>  filesCmp= new HashMap<String,OutputStreamWriter>();
			
			try (Statement csMA = cnnDEV.createStatement() ;) {
				try (ResultSet rsMA = csMA.executeQuery("select * from \"BSCL.Plataforma.BD::PLA.UsoTablaSistema\" where  \"UsoTabla\" = 'MAESTRO' and  \"Estado\" = 'ACT' AND \"SoloReplicar\" = 'N' AND \"SoloNuevos\" = 'S'  AND \"SoloCambios\" = 'N'  ORDER BY \"NombreTabla\" ");) {
					while (rsMA.next()) {
						String tabla=rsMA.getString("NombreTabla");
						String llaves=rsMA.getString("Llaves");
						if (llaves==null || llaves.length()<3) {
							llaves="";
							try (Statement csMAK = cnnDEV.createStatement() ;) {
								try (ResultSet rsMAK = csMAK.executeQuery("SELECT COLUMN_NAME FROM SYS.CONSTRAINTS WHERE SCHEMA_NAME = 'BSCL' AND IS_PRIMARY_KEY = 'TRUE' AND TABLE_NAME = '"+tabla+"' ");) {
									while (rsMAK.next()) {										
									 llaves+=rsMAK.getString("COLUMN_NAME")+";";
									}
								}
							}
							WriteLog("Usando llave de BD->"+llaves);
						}
						tablasNuevas.add(tabla);
						mapa.put(tabla, llaves);
					}
				}
			}		
			WriteLog("  Tablas: "+tablasNuevas.size());
			for (int k=0;k<tablasNuevas.size();k++) {
				String tabla=tablasNuevas.get(k);
				String llave=mapa.get(tabla);
				if (llave==null || llave.length()==0) {
					WriteLog("ERROR: Tabla no tiene llave -> "+tabla);
					continue;
				}					
				try {					
					//Prepara llave primaria
					boolean xEmpresas=false;
					String IdEmpresa="";
					String PK="";
					try (Statement csMA = cnnDEV.createStatement() ;) {
						try (ResultSet rsMA = csMA.executeQuery("SELECT TOP 1 *  FROM \""+tabla+"\" ");) {
							ResultSetMetaData md=rsMA.getMetaData();
							int nc=md.getColumnCount();																						
							for (int i=0;i<nc;i++) {
								String cname=md.getColumnName(i+1);
								if (cname.equalsIgnoreCase("idempresa")) {
									xEmpresas=true;
									IdEmpresa=cname;	
									break;
								}
							}
							WriteLog("   Carga   -> "+tabla+"  COLS "+nc + " IdEmpresa "+xEmpresas);
							if (rsMA.next()) {
								//columnas
								String cols="";								
								String[] keys=llave.split(";");
								for (int j=0;j<keys.length;j++) {
									String pk=keys[j];
									if (pk.length()==0) continue;
									if (pk.equalsIgnoreCase("idempresa")) {
										xEmpresas=true;
										IdEmpresa=pk;
										continue;
									}									
									for (int i=0;i<nc;i++) {
										String tname=md.getColumnTypeName(i+1);
										String cname=md.getColumnName(i+1);
										if (pk.trim().equals(cname)) {
											if (tname.equalsIgnoreCase("BIGINT") || tname.equalsIgnoreCase("INT") || tname.equalsIgnoreCase("INTEGER") || tname.equalsIgnoreCase("SMALLINT")) {
												cols+="\""+cname+"\" ||";
											} else if (tname.equalsIgnoreCase("DECIMAL")){
												cols+="\""+cname+"\" ||";
											} else if (tname.equalsIgnoreCase("DATE") || tname.equalsIgnoreCase("SECONDDATE") || tname.equalsIgnoreCase("TIME") || tname.equalsIgnoreCase("TIMESTAMP") ) {
												cols+="\""+cname+"\" ||";
											} else if (tname.equalsIgnoreCase("NVARCHAR") || tname.equalsIgnoreCase("VARCHAR") || tname.equalsIgnoreCase("CHAR") || tname.equalsIgnoreCase("TEXT")) {
												cols+="\""+cname+"\" ||";
											}
											break;
										}
									}																			
								}
								if (cols.length()==0) {
									WriteLog("ERROR: Tabla no pudo armar llave -> "+tabla+" : "+llave);
									continue;									
								}
								PK=cols.substring(0, cols.length()-2);
								WriteLog("   Llave ->  "+tabla+"  KEY  "+PK+" Itera empresas "+xEmpresas);
							}							
						}
					}					
					//Elimina tabla temporales
					try (Statement csTT = cnnDEV.createStatement(); ) {
						csTT.execute("DROP TABLE #TMP_DEPLOY_CMP;");										
					}catch(Exception er) { };						
					//Creamos una table de paso en el origen
					try (Statement csTT = cnnDEV.createStatement(); ) {
						csTT.execute("CREATE LOCAL TEMPORARY COLUMN TABLE #TMP_DEPLOY_CMP AS (SELECT * FROM \""+tabla+"\") WITH NO DATA;");										
					}
					//cargamos la data del destino en el origen
					try (Statement csMA = cnnPRO.createStatement() ; Statement csBATCH = cnnDEV.createStatement();) {
						try (ResultSet rsMA = csMA.executeQuery("SELECT *  FROM \""+tabla+"\" ");) {
							ResultSetMetaData md=rsMA.getMetaData();
							int nc=md.getColumnCount();							
							int nrs=0;
							while (rsMA.next()) {
								nrs++;
								String insertTxt="";
								insertTxt="  INSERT INTO #TMP_DEPLOY_CMP  ( ";
								//columnas
								String cols="";
								for (int i=0;i<nc;i++) {
									String tname=md.getColumnTypeName(i+1);
									String cname=md.getColumnName(i+1);
									if (tname.equalsIgnoreCase("BIGINT") || tname.equalsIgnoreCase("INT") || tname.equalsIgnoreCase("INTEGER") || tname.equalsIgnoreCase("SMALLINT")) {
										cols+="\""+cname+"\",";
									} else if (tname.equalsIgnoreCase("DECIMAL")){
										cols+="\""+cname+"\",";
									} else if (tname.equalsIgnoreCase("DATE") || tname.equalsIgnoreCase("SECONDDATE") || tname.equalsIgnoreCase("TIME") || tname.equalsIgnoreCase("TIMESTAMP") ) {
										cols+="\""+cname+"\",";
									} else if (tname.equalsIgnoreCase("NVARCHAR") || tname.equalsIgnoreCase("VARCHAR") || tname.equalsIgnoreCase("CHAR") || tname.equalsIgnoreCase("TEXT")) {
										cols+="\""+cname+"\",";
									} 
								}
								insertTxt+=cols.substring(0,cols.length()-1)+ ") VALUES ( ";						
								//valores
								String vals="";
								for (int i=0;i<nc;i++) {
									String tname=md.getColumnTypeName(i+1);
									String cname=md.getColumnName(i+1);
									if (tname.equalsIgnoreCase("BIGINT") || tname.equalsIgnoreCase("INT") || tname.equalsIgnoreCase("INTEGER") || tname.equalsIgnoreCase("SMALLINT")) {								
										vals+=rsMA.getLong(cname)+ ",";
									} else if (tname.equalsIgnoreCase("DECIMAL")){
										vals+=rsMA.getBigDecimal(cname)+ ",";
									} else if (tname.equalsIgnoreCase("DATE") || tname.equalsIgnoreCase("SECONDDATE") || tname.equalsIgnoreCase("TIME") || tname.equalsIgnoreCase("TIMESTAMP") ) {
										if (rsMA.getString(cname)==null) vals+="null,";
										else vals+="'"+rsMA.getString(cname)+ "',";
									} else if (tname.equalsIgnoreCase("NVARCHAR") || tname.equalsIgnoreCase("VARCHAR") || tname.equalsIgnoreCase("CHAR") || tname.equalsIgnoreCase("TEXT")) {
										if (rsMA.getString(cname)==null) vals+="null,";
										else vals+="'"+rsMA.getString(cname).replace("'", "''")+ "',";
									} 
								}							
								insertTxt+=vals.substring(0,vals.length()-1)+");";									
								csBATCH.addBatch(insertTxt);
								if (nrs%200==0) {
									csBATCH.executeBatch();								
								}
							}//while data
							csBATCH.executeBatch();							
						}
					}	
					
					//si tiene idempresa iteramos por esa
					List<Integer> lempresas=new ArrayList<Integer>();
					if (xEmpresas) {
						try (Statement csMA = cnnPRO.createStatement() ;) {
							try (ResultSet rsMA = csMA.executeQuery("SELECT DISTINCT \""+IdEmpresa+"\"  FROM \""+tabla+"\" ");) {																					
								while (rsMA.next()) {
									lempresas.add(rsMA.getInt(IdEmpresa));
								}
							}
						}
					}
					
					
					//comparamos toda la data en el origen y enviamos a archivo
					do {
						try (Statement csLO = cnnDEV.createStatement() ;) {
							String query="SELECT * FROM \""+tabla+"\" WHERE "+PK+" NOT IN  (SELECT "+PK+" FROM #TMP_DEPLOY_CMP)";
							int numE=-1;
							if (lempresas.size()>0) {
								numE=lempresas.get(0);
								lempresas.remove(0);
								query="SELECT * FROM \""+tabla+"\" WHERE \""+IdEmpresa+"\" = 1 AND  "+PK+" NOT IN  (SELECT "+PK+" FROM #TMP_DEPLOY_CMP where \""+IdEmpresa+"\" = "+numE+" )";									
							}							
							try (ResultSet rsLO = csLO.executeQuery(query)) {
								ResultSetMetaData md=rsLO.getMetaData();
								int nc=md.getColumnCount();
								WriteLog("   Compara -> "+tabla+"  COLS "+nc);
								
								String fOut=tabla.substring(0,tabla.lastIndexOf(".")).replace('.', '_').replace("::", "-");
								if (!filesCmp.containsKey(fOut)) {
									filesCmp.put(fOut, new OutputStreamWriter(new FileOutputStream(new File("MT_NEW_"+fOut+".sql"), false), "windows-1252"));
									filesCmp.get(fOut).write("SET SCHEMA \"BSCL\"; \r\n \r\n");
								}	
								if (xEmpresas) filesCmp.get(fOut).write("-- EMPRESA: "+numE+" \r\n");
								filesCmp.get(fOut).write("DO BEGIN AUTONOMOUS TRANSACTION \r\n");												
								while (nc>0 && rsLO.next()) {								
									filesCmp.get(fOut).write("  INSERT INTO \""+ tabla  +"\"  ( ");
									//columnas
									String cols="";
									for (int i=0;i<nc;i++) {
										String tname=md.getColumnTypeName(i+1);
										String cname=md.getColumnName(i+1);
										if (tname.equalsIgnoreCase("BIGINT") || tname.equalsIgnoreCase("INT") || tname.equalsIgnoreCase("INTEGER") || tname.equalsIgnoreCase("SMALLINT")) {
											cols+="\""+cname+"\",";
										} else if (tname.equalsIgnoreCase("DECIMAL")){
											cols+="\""+cname+"\",";
										} else if (tname.equalsIgnoreCase("DATE") || tname.equalsIgnoreCase("SECONDDATE") || tname.equalsIgnoreCase("TIME") || tname.equalsIgnoreCase("TIMESTAMP") ) {
											cols+="\""+cname+"\",";
										} else if (tname.equalsIgnoreCase("NVARCHAR") || tname.equalsIgnoreCase("VARCHAR") || tname.equalsIgnoreCase("CHAR") || tname.equalsIgnoreCase("TEXT")) {
											cols+="\""+cname+"\",";
										} 
									}
									filesCmp.get(fOut).write(cols.substring(0,cols.length()-1)+ ") VALUES ( ");						
									//valores
									String vals="";
									for (int i=0;i<nc;i++) {
										String tname=md.getColumnTypeName(i+1);
										String cname=md.getColumnName(i+1);
										if (xEmpresas && numE>0 && cname.equals(IdEmpresa)) {
											vals+=numE+ ",";
										} else if (tname.equalsIgnoreCase("BIGINT") || tname.equalsIgnoreCase("INT") || tname.equalsIgnoreCase("INTEGER") || tname.equalsIgnoreCase("SMALLINT")) {								
											vals+=rsLO.getLong(cname)+ ",";
										} else if (tname.equalsIgnoreCase("DECIMAL")){
											vals+=rsLO.getBigDecimal(cname)+ ",";
										} else if (tname.equalsIgnoreCase("DATE") || tname.equalsIgnoreCase("SECONDDATE") || tname.equalsIgnoreCase("TIME") || tname.equalsIgnoreCase("TIMESTAMP") ) {
											if (rsLO.getString(cname)==null) vals+="null,";
											else vals+="'"+rsLO.getString(cname)+ "',";
										} else if (tname.equalsIgnoreCase("NVARCHAR") || tname.equalsIgnoreCase("VARCHAR") || tname.equalsIgnoreCase("CHAR") || tname.equalsIgnoreCase("TEXT")) {
											if (rsLO.getString(cname)==null) vals+="null,";
											else vals+="'"+rsLO.getString(cname).replace("'", "''")+ "',";
										} 
									}							
									filesCmp.get(fOut).write(vals.substring(0,vals.length()-1)+"); \r\n ");
								}//while rows
								filesCmp.get(fOut).write("COMMIT; \r\n END; \r\n \r\n \r\n");
								filesCmp.get(fOut).flush();							
							}
						}
					}while (lempresas.size()>0);					
				}catch(Exception er) {
					WriteLog("ERROR: "+er.getMessage());
					er.printStackTrace();					
				}
			}//while tablas
			//close files
			for (Map.Entry<String,OutputStreamWriter> entry : filesCmp.entrySet()) {
				entry.getValue().close();
				File file=new File("MT_NEW_"+entry.getKey()+".sql");
        		ZipEntry e = new ZipEntry(file.getName());
        		zout.putNextEntry(e);        		
        		Path path = Paths.get(file.getCanonicalPath());
        		byte[] data = Files.readAllBytes(path);
        		zout.write(data, 0, data.length);
        		zout.closeEntry();   
        		file.delete();				
			}	
		}
		

		WriteLog("\n*** Tablas para comparar  registros modificados A! --> B");
		{
			ArrayList<String> tablasModificadas=new ArrayList<String>();
			HashMap<String,String> mapa=new HashMap<String,String>();
			HashMap<String,OutputStreamWriter>  filesCmp= new HashMap<String,OutputStreamWriter>();
			
			try (Statement csMA = cnnDEV.createStatement() ;) {
				try (ResultSet rsMA = csMA.executeQuery("select * from \"BSCL.Plataforma.BD::PLA.UsoTablaSistema\" where  \"UsoTabla\" = 'MAESTRO' and  \"Estado\" = 'ACT' AND \"SoloReplicar\" = 'N' AND \"SoloNuevos\" = 'N'  AND \"SoloCambios\" = 'S'  ORDER BY \"NombreTabla\" ");) {
					while (rsMA.next()) {
						String tabla=rsMA.getString("NombreTabla");
						String llaves=rsMA.getString("Llaves");
						tablasModificadas.add(tabla);
						mapa.put(tabla, llaves);
					}
				}
			}					
			for (int k=0;k<tablasModificadas.size();k++) {							
				String tabla=tablasModificadas.get(k);
				String llave=mapa.get(tabla);
				WriteLog("ERROR: ESTA FUNCION NO ESTA IMPLEMENTADA");

				String fOut=tabla.substring(0,tabla.lastIndexOf(".")).replace('.', '_').replace("::", "-");
				if (!filesCmp.containsKey(fOut)) {
					filesCmp.put(fOut, new OutputStreamWriter(new FileOutputStream(new File("MT_MOD_"+fOut+".sql"), false), "windows-1252"));
					filesCmp.get(fOut).write("SET SCHEMA \"BSCL\"; \r\n \r\n");
				}	
				filesCmp.get(fOut).write("ESTA FUNCION NO ESTA IMPLEMENTADA \r\n");
				break;
//				if (llave==null || llave.length()==0) {
//					WriteLog("ERROR: Tabla no tiene llave -> "+tabla);
//					continue;
//				}					
//				try {	
//					
//							
//				}catch(Exception er) {
//					WriteLog("ERROR: "+er.getMessage());
//					er.printStackTrace();
//				}
			}//while tablas
			//close files
			for (Map.Entry<String,OutputStreamWriter> entry : filesCmp.entrySet()) {
				entry.getValue().close();
				File file=new File("MT_MOD_"+entry.getKey()+".sql");
        		ZipEntry e = new ZipEntry(file.getName());
        		zout.putNextEntry(e);        		
        		Path path = Paths.get(file.getCanonicalPath());
        		byte[] data = Files.readAllBytes(path);
        		zout.write(data, 0, data.length);
        		zout.closeEntry();   
        		file.delete();					
			}	
		}
		zout.close();
	}
	public static HashMap<String,String> getValidObjects(java.sql.Connection cnn) throws SQLException {
		HashMap<String,String> map=new HashMap<String,String>();
		Statement csInv = cnn.createStatement() ;
        ResultSet rsInv = csInv.executeQuery("select function_name as SERVICIO from sys.functions where is_valid = 'TRUE' and schema_name = 'BSCL' union select procedure_name as SERVICIO from sys.procedures where is_valid = 'TRUE' and schema_name = 'BSCL' union select view_name as SERVICIO from sys.views  where is_valid = 'TRUE' and schema_name = 'BSCL' order by SERVICIO");           
        while(rsInv.next()){
        	String srv=rsInv.getString(rsInv.findColumn("SERVICIO")).replace("::", ".");
        	if (!map.containsKey(srv)) map.put(srv, "OK");
        	//caso que se uso _ como .
        	if (srv.indexOf("_")>0) {
        		srv=rsInv.getString(rsInv.findColumn("SERVICIO")).replace("_", ".");
        		if (!map.containsKey(srv)) map.put(srv, "OK");
        	}
        }
        rsInv.close();
        csInv.close();   
        return map;
	}
	

	public static void getMetaDataTable(java.sql.Connection cnnDEV,
			HashMap<String, Deploy.objTable> tablasDEV) throws SQLException {		
		
		//datos columnas
		String query="select TC.TABLE_NAME,TC.POSITION,TC.COLUMN_NAME,TC.DATA_TYPE_NAME,TC.LENGTH,TC.SCALE,TC.IS_NULLABLE,TC.DEFAULT_VALUE,TT.IS_USER_DEFINED_TYPE,TT.IS_TEMPORARY,TT.TEMPORARY_TABLE_TYPE,TT.COMMIT_ACTION from SYS.TABLES as TT JOIN SYS.TABLE_COLUMNS AS TC ON TC.TABLE_NAME=TT.TABLE_NAME WHERE TC.SCHEMA_NAME = 'BSCL' ORDER BY TC.TABLE_NAME,TC.POSITION";  
		Statement csInv = cnnDEV.createStatement() ;
		ResultSet rsInv = csInv.executeQuery(query);
		while(rsInv.next()){
			String tname=rsInv.getString(rsInv.findColumn("TABLE_NAME"));
			Deploy.objTable table;
			if (tablasDEV.containsKey(tname)) 
			{
				table=tablasDEV.get(tname);
			}else {
				table=new Deploy.objTable();
				table.is_type=rsInv.getString(rsInv.findColumn("IS_USER_DEFINED_TYPE")).equals("TRUE")?true:false;	
				if (rsInv.getString(rsInv.findColumn("IS_TEMPORARY")).equals("TRUE")) {
					table.is_gtemp=rsInv.getString(rsInv.findColumn("TEMPORARY_TABLE_TYPE")).equals("GLOBAL")?true:false;		
					table.is_delete=rsInv.getString(rsInv.findColumn("COMMIT_ACTION")).equals("DELETE")?true:false;
				}				
				tablasDEV.put(tname, table);
			}
			 Deploy.objColumn col=new  Deploy.objColumn();
			 col.TABLE_NAME=tname;            	 
			 col.COLUMN_NAME=rsInv.getString(rsInv.findColumn("COLUMN_NAME"));
			 col.DATA_TYPE_NAME=rsInv.getString(rsInv.findColumn("COLUMN_NAME"));
			 col.POSITION=rsInv.getInt(rsInv.findColumn("POSITION"));
			 col.DATA_TYPE_NAME=rsInv.getString(rsInv.findColumn("DATA_TYPE_NAME"));
			 col.LENGTH=rsInv.getInt(rsInv.findColumn("LENGTH"));
			 col.SCALE=rsInv.getInt(rsInv.findColumn("SCALE"));
			 col.IS_NULLABLE=rsInv.getString(rsInv.findColumn("IS_NULLABLE"));
			 if (col.IS_NULLABLE==null) col.IS_NULLABLE="TRUE";
			 col.DEFAULT_VALUE=rsInv.getString(rsInv.findColumn("DEFAULT_VALUE"));
			 table.objColumns.put(col.COLUMN_NAME,col);
		}
		rsInv.close();
		csInv.close();
	}
	public static void getMetaDataFK(java.sql.Connection cnnDEV,
			HashMap<String, Deploy.objFK> fkDEV) throws Exception {
		String query="SELECT TABLE_NAME,COLUMN_NAME,CONSTRAINT_NAME,REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME FROM SYS.REFERENTIAL_CONSTRAINTS WHERE SCHEMA_NAME = 'BSCL' ORDER BY TABLE_NAME ";  
		Statement csInv = cnnDEV.createStatement() ;
		ResultSet rsInv = csInv.executeQuery(query);
		while(rsInv.next()){
			String fkname=rsInv.getString(rsInv.findColumn("TABLE_NAME"))+"_"+rsInv.getString(rsInv.findColumn("COLUMN_NAME"))+"_"+rsInv.getString(rsInv.findColumn("CONSTRAINT_NAME"));
			Deploy.objFK fk;
			if (fkDEV.containsKey(fkname)) 
			{
				throw new Exception("FK con el mismo nombre:"+rsInv.getString(rsInv.findColumn("CONSTRAINT_NAME")));
			}
			fk=new Deploy.objFK();
			fkDEV.put(fkname, fk);
			fk.TABLE_NAME=rsInv.getString(rsInv.findColumn("TABLE_NAME"));
			fk.COLUMN_NAME=rsInv.getString(rsInv.findColumn("COLUMN_NAME"));
			fk.CONSTRAINT_NAME=rsInv.getString(rsInv.findColumn("CONSTRAINT_NAME"));
			fk.REFERENCED_TABLE_NAME=rsInv.getString(rsInv.findColumn("REFERENCED_TABLE_NAME"));
			fk.REFERENCED_COLUMN_NAME=rsInv.getString(rsInv.findColumn("REFERENCED_COLUMN_NAME"));			
		}
		rsInv.close();
		csInv.close();
	}	
	
	
	public static void getMetaDataPKUK(java.sql.Connection cnnDEV,
		HashMap<String, Deploy.objPKUK> fkDEV) throws Exception {
		String query="SELECT TABLE_NAME,COLUMN_NAME,IS_PRIMARY_KEY,IS_UNIQUE_KEY FROM SYS.CONSTRAINTS WHERE SCHEMA_NAME = 'BSCL' ORDER BY  TABLE_NAME ";  
		Statement csInv = cnnDEV.createStatement() ;
		ResultSet rsInv = csInv.executeQuery(query);
		while(rsInv.next()){
			String KEYname=rsInv.getString(rsInv.findColumn("TABLE_NAME"))+"_"+rsInv.getString(rsInv.findColumn("COLUMN_NAME"))+"_"+rsInv.getString(rsInv.findColumn("IS_PRIMARY_KEY"))+"_"+rsInv.getString(rsInv.findColumn("IS_UNIQUE_KEY"));
			Deploy.objPKUK pk;
			if (fkDEV.containsKey(KEYname)) 
			{
				throw new Exception("PK-UK repetida:"+rsInv.getString(rsInv.findColumn("TABLE_NAME")));
			}
			pk=new Deploy.objPKUK();
			fkDEV.put(KEYname, pk);
			pk.TABLE_NAME=rsInv.getString(rsInv.findColumn("TABLE_NAME"));
			pk.COLUMN_NAME=rsInv.getString(rsInv.findColumn("COLUMN_NAME"));
			pk.IS_PRIMARY_KEY=rsInv.getString(rsInv.findColumn("IS_PRIMARY_KEY"));
			pk.IS_UNIQUE_KEY=rsInv.getString(rsInv.findColumn("IS_UNIQUE_KEY"));
		}
		rsInv.close();
		csInv.close();
	}	
	
	public static void getMetaDataSQ(java.sql.Connection cnnDEV,
			HashMap<String, Deploy.objSQ> fkDEV) throws Exception {
			String query="select OBJECT_NAME from sys.objects where schema_name = 'BSCL' and object_type = 'SEQUENCE' and OBJECT_NAME NOT LIKE '_SYS_%' ";  
			Statement csInv = cnnDEV.createStatement() ;
			ResultSet rsInv = csInv.executeQuery(query);
			while(rsInv.next()){
				String KEYname=rsInv.getString(rsInv.findColumn("OBJECT_NAME"));
				Deploy.objSQ pk;
				if (fkDEV.containsKey(KEYname)) 
				{
					throw new Exception("SEQUENCE repetida:"+rsInv.getString(rsInv.findColumn("OBJECT_NAME")));
				}
				pk=new Deploy.objSQ();
				fkDEV.put(KEYname, pk);
				pk.OBJECT_NAME=rsInv.getString(rsInv.findColumn("OBJECT_NAME"));
			}
			rsInv.close();
			csInv.close();
		}	
	
	public static void getMetaDataINX(java.sql.Connection cnnDEV,
			HashMap<String, Deploy.objINX> fkDEV) throws Exception {
			String query="select OBJECT_NAME from sys.objects where schema_name = 'BSCL' and object_type = 'INDEX' and object_name not like '_SYS_TREE%' and  object_name not like '%FULLTEXT%'   ";  
			Statement csInv = cnnDEV.createStatement() ;
			ResultSet rsInv = csInv.executeQuery(query);
			while(rsInv.next()){
				String KEYname=rsInv.getString(rsInv.findColumn("OBJECT_NAME"));
				Deploy.objINX pk;
				if (fkDEV.containsKey(KEYname)) 
				{
					throw new Exception("INDEX repetido:"+rsInv.getString(rsInv.findColumn("OBJECT_NAME")));
				}
				pk=new Deploy.objINX();
				fkDEV.put(KEYname, pk);
				pk.OBJECT_NAME=rsInv.getString(rsInv.findColumn("OBJECT_NAME"));
			}
			rsInv.close();
			csInv.close();
		}		
	
	public static String fixPath(String path) {
		if (File.separator.equals("\\")) {
			return path.replace("/", "\\");
		}
		return path.replace("\\", "/");
	}
	public static boolean deployFile(String vBasePath, java.sql.Connection cnnPRO,String repo,ChannelSftp sftp,String WebPath) throws IOException {
		repo=repo.replace(vBasePath, "");
		String sname=vBasePath+repo;
		File fname=new File(sname);
		byte[] script = Files.readAllBytes(Paths.get(sname));
		String tname = fname.getName();
		String suffix=tname.substring(tname.lastIndexOf('.')+1);
		String name=tname.replace("."+suffix,"");			
		String pkg=fileName2Srv(fixPath(repo));
		if (suffix.equalsIgnoreCase("sql")) pkg+="."+suffix;
		pkg=pkg.replace("."+tname,"");
		String resp = "OK+";
		if (pkg.startsWith("FioriAgente")) {
			
		}
		else {
			resp=ExecuteREPO(cnnPRO,pkg,name,suffix, script);	
		}
		    			
		WriteLog("REPO:"+repo+" "+resp); 
		boolean resp2=true;
		String vfilePath = fixPath(fname.getAbsolutePath().substring(0,fname.getAbsolutePath().lastIndexOf(File.separator))+File.separator);
		String vFileName = fname.getName();
		if (suffix.toLowerCase().endsWith("sql")) {
			//Archivo SQL
			resp2=EjecutarScriptFile(cnnPRO, vfilePath,vFileName);    				
		}else {
			//Archivo Estatico
			if (sftp!=null) {
				String serverfile= WebPath+repo.replace('\\', '/');
				try {				
					try {
						//intenta subir el archivo
						sftp.put(vfilePath+vFileName, serverfile);	
						WriteLog("WEB:"+repo+" OK+");
						resp2=true;						
					}catch (SftpException e) {
						//crea primero directorios
						String[] folders = serverfile.split( "/" );
						sftp.cd("/");
						for (int i=0;i<folders.length-1;i++) {
							String folder=folders[i];
						    if ( folder.length() > 0 ) {
						        try {
						            sftp.cd( folder );
						        }
						        catch ( SftpException pe ) {
						            sftp.mkdir( folder );
						            sftp.cd( folder );
						        }
						    }
						}
						//envia nuevamente archivo
						sftp.put(vfilePath+vFileName, serverfile);
						WriteLog("WEB:"+repo+" OK+");
						resp2=true;				
				    }
			    } catch (SftpException e) {
			    	WriteLog("WEB:"+repo+" ERROR:"+e.getMessage());
			    	resp2=false;
			    }
			} else {
		    	WriteLog("WEB:"+repo+" ERROR: NO CONECTADO A SFTP");
		    	resp2=false;				
			}
		}
		if (resp.equalsIgnoreCase("OK+") && resp2==true) 
			return true;
		return false;
	}
	
	public static boolean deployRepoFile(String vBasePath, java.sql.Connection cnnDEV, java.sql.Connection cnnPRO,String repo) throws IOException {
		String repon =repo.replace("/", File.separator).replace("\\", File.separator);		
		String suffix=repon.substring(repon.lastIndexOf('.')+1);
		String pkg=repon.substring(0,repon.lastIndexOf(File.separator)).replace(File.separator, ".");
		String obj=repon.substring(repon.lastIndexOf(File.separator)+1,repon.lastIndexOf('.'));
		byte[] bdata=null;
		try {
	        String query="SELECT BDATA,CDATA FROM _SYS_REPO.ACTIVE_OBJECT WHERE  package_id like 'BSCL%'  and PACKAGE_ID = '"+pkg+"' AND OBJECT_NAME = '"+obj+"' AND OBJECT_SUFFIX = '"+suffix+"'  "; 
			Statement cs1 = cnnDEV.createStatement() ;
	        ResultSet rset1 = cs1.executeQuery(query);  
	        if (!rset1.next()){
	        	WriteLog("DEPLOY: Archivo no encontrado en REPO"); 
	        	return false;        	
	        }
	    	bdata       = rset1.getBytes(rset1.findColumn("BDATA"));
	    	String cdata= rset1.getString(rset1.findColumn("CDATA"));   
			if (bdata==null) bdata=cdata.getBytes("UTF-8");	        
		}catch(Exception er) {
        	WriteLog("DEPLOY: ERROR"+er.getMessage()); 
        	return false;        				
		}
		if (bdata.length==0) {
        	WriteLog("DEPLOY: ERROR DATA LEN=0"); 
        	return false;        							
		}
		String resp=ExecuteREPO(cnnPRO,pkg,obj,suffix, bdata);    			
		WriteLog("DEPLOY:"+repon+" "+resp); 
		if (suffix.toLowerCase().endsWith("sql")) { 	
			String tempfile=obj+".tmp";
			Files.write(Paths.get(tempfile), bdata);
			File fname=new File(tempfile);			
			String vfilePath = fixPath(fname.getAbsolutePath().substring(0,fname.getAbsolutePath().lastIndexOf(File.separator))+File.separator);
			String vFileName = fname.getName();
			EjecutarScriptFile(cnnPRO, vfilePath,vFileName);
			Files.deleteIfExists(Paths.get(tempfile));
		}
		return true;
	}	
	
	public static boolean updateVersion(java.sql.Connection cnnPRO) throws IOException, SQLException {
    	//Version de sistema
    	Statement cs5 = cnnPRO.createStatement() ;
    	SimpleDateFormat dtV = new SimpleDateFormat("yyyyMMdd.HHmm");
		String query5="update  \"BSCL.Plataforma.BD::PLA.Configuracion\" set  \"Valor\" = '"+dtV.format(new Date())+"'  where \"CodigoParametro\"='PLA.Version' ";
		cs5.executeUpdate(query5);
		cs5.close();    
		return true;
	}	
	
	public static void SpInValidate(java.sql.Connection cnnPRO) throws IOException, SQLException {
    	Statement cs5 = cnnPRO.createStatement() ;
		String query5="select  procedure_name as SP,'do begin execute immediate ''alter procedure bscl.\"'||procedure_name||'\" recompile with plan ''; end;' AS RECOM\r\n" + 
				"  from sys.procedures where is_valid = 'FALSE' and schema_name = 'BSCL' and procedure_name not like 'BSCL.Homologacion%'";
		ResultSet rs=cs5.executeQuery(query5);
		WriteLog("Revisando SP invalidados:");
		String recompile="\r\n\r\nPara recompilar:\r\n\r\n";
		while (rs.next()) {
			recompile+=rs.getString("RECOM")+"\r\n";
			WriteLog("\""+rs.getString("SP")+"\"");
		}		
		WriteLog(recompile);
		cs5.close();    
	}		

	public static void BuscaLocalFiles(ArrayList<String> repo,String vBasePath,File root)
	{
	    File[] files = root.listFiles(); 
	    for (File file : files) {
	        if (file.isFile()) {       	
	        	repo.add(file.getAbsolutePath().replace(vBasePath, ""));	        		           	            
	        } else if (file.isDirectory()) {
	        	BuscaLocalFiles(repo,vBasePath,file);
	        }
	    }
	}	
		
	public static boolean EjecutarScriptFile(java.sql.Connection cnn, String vPath,String vScriptFile )
			throws IOException {
		WriteLog("  SCRIPT "+vScriptFile);
		String vScript[]=readFileScript(vPath+vScriptFile);
		//caso catalan (alter)
		//0: SET SCHEMA
		//1: DROP
		//2: CREATE
		if ((vScript.length==3 || vScript.length==4) && vScript[2].toUpperCase().contains("CREATE")) {	
			//Ejecutamos el Set schema
			String resp=ExecuteSQL(cnn,vScript[0]);
			if (!resp.equals("OK+")) {
				String errmsg=vScript[0].replace("\n", "[n]");
				if (errmsg.length()>80) errmsg=errmsg.substring(0, 76)+"...";
				WriteLog("  SCRIPT: "+errmsg); 
				WriteLog("  ERROR DESCRI: "+resp);
				return false;				
			}
			//valida si el script es el mismo en la base datos (no ejecuta)
			String sqlName="\""+getNombreBDScript(vScript[2])+"\"";			
			String bkp=GetObjectDefinition(cnn,"BSCL",sqlName);
			if (bkp!=null && bkp.length()>0) {
				//VALIDA SI PROCEDIMIENTO ES VALIDO
				String cmpbkp=bkp.trim();
				String cmpScr=vScript[2].trim();
				//vistas remueve columnas
				String tipo=cmpbkp;
				while (tipo.indexOf("  ")>0) tipo=tipo.replace("  "," ").replace("\r", "").replace("\n", "").toUpperCase();
				if (tipo.contains("CREATE VIEW")) tipo="VIEW";
				else if (tipo.contains("CREATE PROCEDURE")) tipo="PROCEDURE";
				else if (tipo.contains("CREATE FUNCTION")) tipo="FUNCTION";
				else tipo = "???";
				if (tipo.equals("VIEW")) {
					String cmpbkpMinus=cmpbkp.toUpperCase();
					String cmpScrMinus=cmpScr.toUpperCase();
					if (cmpbkpMinus.indexOf(" AS ") > -1) {
						cmpbkp=cmpbkp.substring(cmpbkpMinus.indexOf(" AS "),cmpbkp.length()).replace("\r", "").replace("\n", "").trim();	
					}
					
					if (cmpScrMinus.indexOf(" AS ") > -1) {
						cmpScr=cmpScr.substring(cmpScrMinus.indexOf(" AS "),cmpScr.length()).replace("\r", "").replace("\n", "").trim();	
					}
										
				}				
				if (cmpbkp.equals(cmpScr)) {
					if (isCompiledObject(cnn,sqlName.replace("\"", ""))) {
						WriteLog("  MENSAJE: Script "+tipo+" igual en BD no se compila");
						return true;
					}else {
						WriteLog("  MENSAJE: Script "+tipo+" igual pero invalidado en BD");
					}
				}
			}			
			//ejecutamos el create por alter
			String vScript2=vScript[2];
			if (vScript2.contains("CREATE")) vScript2=vScript2.replaceFirst("CREATE", "ALTER");			
			if (vScript2.contains("create")) vScript2=vScript2.replaceFirst("create", "alter");
			resp=ExecuteSQL(cnn,vScript2);
			if (!resp.equals("OK+")) {
				int pi=resp.indexOf("[");
				int pf=resp.indexOf("]",pi+1);
				String codeResp=resp.substring(pi,pf+1);
				//Maneja caso que no exista en la base datos
				//ERROR DESCRI: SAP DBTech JDBC: [321] (at 12): invalid view name: BSCL.Orden.BD.Vistas::ConsultarAsignacion2: line 1 col 13 (at pos 12)
				//ERROR DESCRI: SAP DBTech JDBC: [328] (at 17): invalid name of function or procedure: BSCL.Custodia.BD.Servicios::ConsultarFlujoCustodia2: line 1 col 18 (at pos 17)
				//ERROR DESCRI: SAP DBTech JDBC: [328] (at 8553): invalid name of function or procedure: BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguienteMasivoXXXX: line 171 col 7 (at pos 8553)
				//-----------------------------------------------
				//Maneja caso que la firma es diferente (DROP/CREATE)
				//ERROR DESCRI: SAP DBTech JDBC: [7]: feature not supported: number of parameters cannot be changed with ALTER command: expected 16, but 17 provided: line 1 col 2 (at pos 1)
				if ((resp.contains("[7]")) || 
					((resp.contains("[321]") || resp.contains("[328]")) && (resp.contains(sqlName.replace("\"", ""))))) 
				{		
					resp=ExecuteSQL(cnn,vScript[1]);//DROP
					
					resp=ExecuteSQL(cnn,vScript[2]);//CREATE
					if (!resp.equals("OK+")) {	
						String respBKP="---  SIN BACKUP ---";
						if (bkp.length()>1) respBKP=ExecuteSQL(cnn,bkp);//backup
						WriteLog("  SCRIPT: "+sqlName); 
						WriteLog("  MENSAJE RESTAURACION: "+respBKP); 		
						WriteLog("  ERROR DESCRI: "+resp); 							
						return false;						
					}
					
					if (vScript.length==4) { 
						resp=ExecuteSQL(cnn,vScript[3]);//GRANTT
						WriteLog("  MENSAJE GRANT: "+resp); 		
					}
					if (codeResp.equals("[7]")) WriteLog("  OK: NEW HEADER");
					else WriteLog("  OK: NEW OBJECT "+codeResp);
					return true;
				}					
				//Error en el SP/VISTA
				String errmsg=vScript[2].replace("\n", "[n]");
				if (errmsg.length()>80) errmsg=errmsg.substring(0, 76)+"...";
				WriteLog("  SCRIPT ALTER: "+errmsg); 
				WriteLog("  ERROR DESCRI: "+resp);
				return false;				
			}
			WriteLog("  OK: ALTER EXECUTED");
			return true;
		}else {		
			//caso raro no definido por desarrollo
			int ok=0;
			int er=0;
			for (int i=0;i<vScript.length;i++) {			
				String resp=ExecuteSQL(cnn,vScript[i]);
				if (!resp.equals("OK+")) {	
					er++;
					String errmsg=vScript[i].replace("\n", "[n]");
					if (errmsg.length()>80) errmsg=errmsg.substring(0, 76)+"...";
					WriteLog("  SCRIPT: "+errmsg); 
					WriteLog("  ERROR DESCRI: "+resp); 				
				}else {
					ok++;
				}
			}
			WriteLog("  OK: "+ok+" ERR:"+er+"  *** SCRIPT PERSONALIZADO! ***");
			if (er==0) return true;
			return false;
		}
	}

	public static String[] readFileScript(String filename) throws IOException
	{
		String data=readFile(filename);			
		String lines[]=data.split("\\r?\\n");
		ArrayList<String> res=new ArrayList<String>();
		
		///Revisa si viene delimitador GO
		boolean isGO=false;
		for (int i=0;i<lines.length;i++) {
			String ln=lines[i];
			if (ln.trim().toUpperCase().equals("GO")) {
				isGO=true;
				break;
			}
		}
		if (isGO) {		
			//Delimitador GO
			String script="";
			for (int i=0;i<lines.length;i++) {
				String ln=lines[i];
				if (ln.trim().toUpperCase().equals("GO")) {
					res.add(script);
					script="";
				}else {
					script+=ln+"\r\n";
				}
			}			
			if (script.length()>0) res.add(script+"\r\n");					
		}else {
			//Antiguo Delimitador ;			
			String tsigue="";
			int comentario=0;
			int index=0;
			int procedure=0;
			for (int i=0;i<lines.length;i++) {
				String ln=lines[i].replace("\t", " ").trim();					
				//Extrae Comentarios /*  */
				if (procedure==0) {
					if (ln.indexOf("/*")>=0) {
						if (ln.lastIndexOf("*/")>=0) {
							//segunda parte
							index=ln.lastIndexOf("*/")+2;
							if (index>ln.length()) index=ln.length();					
							ln=ln.substring(index);
							//Primera Parte
							index=ln.indexOf("/*");
							if (index<0) index=0;
							ln=ln.substring(0,index);						
							comentario=0;
						}else {
							comentario=1;
							index=ln.indexOf("/*");
							if (index<0) index=0;
							ln=ln.substring(0,index);					
						}
					}
					else if (comentario==1 && ln.lastIndexOf("*/")>=0) {
						index=ln.lastIndexOf("*/")+2;
						if (index>ln.length()) index=ln.length();					
						ln=ln.substring(index);
						comentario=0;				
					}
					else if (comentario==1) {
						continue;
					}
					//Extrae Comentarios --
					if (ln.indexOf("--")>=0) {
						index=ln.indexOf("--");
						if (index<0) index=0;
						ln=ln.substring(0,index);					
					}	
				}
				//
				if (ln.length()==0) continue;
				if (procedure==0 && ln.endsWith(";")) {		
					//Alguien olvido cerrar el anterior
					if (tsigue.length()>1 && (
							ln.toLowerCase().startsWith("commit") ||
							ln.toLowerCase().startsWith("drop") ||
							ln.toLowerCase().startsWith("create") ||
							ln.toLowerCase().startsWith("alter"))) {
						res.add(tsigue);
						res.add(ln.substring(0,ln.length()-1));
						
					}else {
						tsigue+=" "+ln.substring(0,ln.length()-1);
						res.add(tsigue);
					}
					tsigue="";
				}else {
					if (ln.toLowerCase().contains("create") && ln.toLowerCase().contains("procedure")) procedure=1;
					if (ln.toLowerCase().contains("create") && ln.toLowerCase().contains("function")) procedure=1;
					if (procedure==1) tsigue+=" "+ln+"\r\n";
					else tsigue+=" "+ln;
				}
			}	
			//Alguien olvido cerrar el ultimo
			if (tsigue.length()>1) res.add(tsigue);
		}
		//FIN
		return res.toArray(new String[res.size()]);			
	}

	public static String readFile(String filename) throws IOException
	{
	  byte[] encoded = Files.readAllBytes(Paths.get(filename));
	  return new String(encoded, StandardCharsets.UTF_8);
	}	
	
	public static  ArrayList<String> OrdenaJerarquicamente(java.sql.Connection cnnDEV,ArrayList<String> listDesordenada) throws SQLException {
		if (listDesordenada.size() == 0) {
			return listDesordenada;
		}
       	//WriteLog("*** Obteniendo arbol de dependencias");
    	String filesDeps[]=getDependencias(cnnDEV);
    	//WriteLog("*** Obteniendo objetos de base datos");
    	ArrayList<String> srvList=new ArrayList<String>();
    	Statement csInv = cnnDEV.createStatement() ;
		ResultSet rsInv = csInv.executeQuery("select function_name as SERVICIO from sys.functions where schema_name = 'BSCL' union select procedure_name as SERVICIO from sys.procedures where schema_name = 'BSCL' union select view_name as SERVICIO from sys.views  where schema_name = 'BSCL' ORDER BY SERVICIO ");			
		while(rsInv.next()){    
			srvList.add(rsInv.getString(rsInv.findColumn("SERVICIO")).replace("::", "."));
		}
		rsInv.close();
		csInv.close();        	
		//WriteLog("*** Reconstruyendo mapa");
		HashMap<String,Deploy.Objdb> mapa=new HashMap<String, Deploy.Objdb>();
		for (int i=0;i<filesDeps.length;i++) {
			String str=filesDeps[i].replace("\r", "").trim();
			if (str.length()==0) continue;
			if (str.startsWith("#")) continue;
			String param[]=filesDeps[i].split("\t");
			String srv=param[0].replace("::", ".");
			if (!mapa.containsKey(srv)) {
				Objdb obj=new Objdb();
				obj.tipo=param[1];
				mapa.put(srv, obj);
			}
			Deploy.Objdb obj=mapa.get(srv);
			obj.objRequeridos.add(param[2].replace("::", "."));
		}  	
		//WriteLog("*** Priorizando");
		HashMap<String,String> priori_h=new HashMap<String,String>();
		String[] priori=new String[srvList.size()];
		int priori_c=0;
		//Si no esta en mapa no requiere de nada, va al inicio
		while (true) {
			boolean found=false;
			for (int i=0;i<srvList.size();i++) {				
				if (!mapa.containsKey(srvList.get(i))) {						
					priori[priori_c++]=srvList.get(i);
					priori_h.put(srvList.get(i), "");
					srvList.remove(i);	
					found=true;
					break;
				}
			}		
			if (found==false) break;
		}
		//Si esta en mapa, requiere que este primero en la lista
		while (srvList.size()>0) {
			for (int i=0;i<srvList.size();i++) {					
				ArrayList<String> reqs=mapa.get(srvList.get(i)).objRequeridos;
				boolean insert=true;
				for (int j=0;j<reqs.size();j++) {
					if (!priori_h.containsKey(reqs.get(j))) {
						insert=false;
						break;
					}
				}
				if (insert) {
					priori[priori_c++]=srvList.get(i);
					priori_h.put(srvList.get(i), "");
					srvList.remove(i);
					break;								
				}
			}				
		}
		//reordena la lista para los que tienen mapa
		ArrayList<String> listOrdenada=new ArrayList<String>();
		for (int i=0;i<priori_c;i++) {
			for (int j=0;j<listDesordenada.size();j++) {
				if (priori[i].equals(listDesordenada.get(j))) {
					listOrdenada.add(priori[i]);
				}
			}
		}
		//Elementos que no se encontraron
		for(String itemDes:listDesordenada) {
			boolean found=false;
			for(String itemORD:listOrdenada) {
				if (itemDes.equals(itemORD)) {
					found=true;
					break;
				}
			}					
			if (found==false) {
				WriteLog(" Sin referencia:"+itemDes);
				listOrdenada.add(itemDes);
			}
		}		
		return listOrdenada;
	}
	public static void EjecutarDependencias(java.sql.Connection cnn, String vPath,HashMap<String,Deploy.Objdb> mapa,String srv,boolean soloInvalidos,HashMap<String,String> validob) throws IOException {
		Objdb obj=mapa.get(srv);    	
		for (int k=0;k<obj.objRequeridos.size();k++) {
			String srvdep=obj.objRequeridos.get(k);
			if (mapa.containsKey(srvdep)) {
				Objdb obj2=mapa.get(srvdep);  
				if (obj2.ejecutado==false) {								
					EjecutarDependencias(cnn,vPath,mapa,srvdep,soloInvalidos,validob);
					if (soloInvalidos==true && obj2.ejecutado==false) {
						if (validob.containsKey(srvdep)) obj2.ejecutado=true;	
					}
					if (obj2.ejecutado==false) {
						obj2.ejecutado=true;
						File fname=new File(vPath+Srv2filename(srvdep));
		    			String vfilePath = fixPath(fname.getAbsolutePath().substring(0,fname.getAbsolutePath().lastIndexOf(File.separator))+"/");
		    			String vFileName = fname.getName();
		    			EjecutarScriptFile(cnn, vfilePath,vFileName);   						
					}
				}
			}
		}
		if (soloInvalidos==true && obj.ejecutado==false) {
			if (validob.containsKey(srv)) obj.ejecutado=true;	
		}		
		if (obj.ejecutado==false) {
			obj.ejecutado=true;
			File fname=new File(vPath+Srv2filename(srv));
			String vfilePath = fixPath(fname.getAbsolutePath().substring(0,fname.getAbsolutePath().lastIndexOf(File.separator))+"/");
			String vFileName = fname.getName();
			EjecutarScriptFile(cnn, vfilePath,vFileName);   
		}
	}
	public static int CalcularDependencias(HashMap<String,Deploy.Objdb> mapa,String srv,HashMap<String,int[]> files,int deps,Boolean loops,int nivel) throws IOException {
		Objdb obj=mapa.get(srv);    
		if (obj==null) {
			WriteLog("ERROR: No se encuentra en deps:"+srv);
			return 0;			
		}
		for (int k=0;k<obj.objRequeridos.size();k++) {
			String srvdep=obj.objRequeridos.get(k);
			if (mapa.containsKey(srvdep)) {
				Objdb objdep=mapa.get(srvdep);
				boolean loopdep=false;
				int calldep=0;
				if (!files.containsKey(srvdep)) {
					int data[]=files.get(srv);
					calldep=data[0];
					if (data[1]>0) loopdep=true;
				}				
				WriteLog(new String(new char[nivel+1]).replace("\0"," ")+srvdep+" subs="+objdep.objRequeridos.size()+" calls="+calldep+" loop="+loopdep);
				return CalcularDependencias(mapa,srvdep,files,deps,loops,nivel+1);
			}
		}
		if (!files.containsKey(srv)) {
			WriteLog("ERROR: No se encuentra en deps:"+srv);
			return 0;
		}
		int data[]=files.get(srv);
		if (data[1]>0) loops=true;
		return deps+data[0];
	}	
	public static String Srv2filename(String srv) {
		return fixPath(srv.replace(".", "/")+".sql");
	}	
	public static String fileName2Srv(String filename) {
		return fixPath(filename).replace(File.separator, ".").replace(".sql","").replace(".SQL","");
	}

	
	public static void EliminarObjetosBD(java.sql.Connection cnn) throws SQLException{
		ArrayList<String> todo=new ArrayList<String>();
        Statement cs = cnn.createStatement() ;              
        String query=""; 
        query+="select VIEW_NAME      AS OBJETO,'VISTA' as TIPO from sys.views      where schema_name ='BSCL' union "; 
        query+="select PROCEDURE_NAME AS OBJETO,'SP'    as TIPO from sys.procedures where schema_name ='BSCL' union ";
        query+="select FUNCTION_NAME  AS OBJETO,'FN'    as TIPO from sys.functions  where schema_name ='BSCL' ";
        ResultSet rset = cs.executeQuery(query);
		while (rset.next()){
			String obj= rset.getString(rset.findColumn("OBJETO"));
			String tipo=rset.getString(rset.findColumn("TIPO"));
			String cmd="";
			if (tipo.equals("VISTA")) cmd="drop view      BSCL.\""+obj+"\"";
			if (tipo.equals("SP"))    cmd="drop procedure BSCL.\""+obj+"\"";
			if (tipo.equals("FN"))    cmd="drop function  BSCL.\""+obj+"\"";
			todo.add(cmd);
		}
		rset.close();
		cs.close();
		for (String cmd:todo) {
			System.out.println("Ejecutando: "+cmd);
			Statement st = cnn.createStatement() ;
			st.executeUpdate(cmd);
			st.close();
		}		
	}
	
	public static boolean isCompiledObject(java.sql.Connection cnn,String srv) {
		try {
	        Statement cs = cnn.createStatement() ;   
	        ResultSet rset = cs.executeQuery("select function_name as SERVICIO from sys.functions where is_valid = 'TRUE' and schema_name = 'BSCL' and function_name = '"+srv+"' union select procedure_name as SERVICIO from sys.procedures where is_valid = 'TRUE' and schema_name = 'BSCL'  and procedure_name = '"+srv+"'  union select view_name as SERVICIO from sys.views  where is_valid = 'TRUE' and schema_name = 'BSCL'  and view_name = '"+srv+"'  ");
			boolean found=false;
	        if (rset.next()){
				found=true;
			}
			rset.close();
			cs.close();    
			return found;
		}catch (Exception er) {
			return false;
		}
	}	
	
	public static String[] getDependencias(java.sql.Connection cnn) throws SQLException{
		ArrayList<String> deps=new ArrayList<String>();
        Statement cs = cnn.createStatement() ;                                  
        ResultSet rset = cs.executeQuery("select dependent_object_name as \"OBJETO\",dependent_object_type as \"OBJECTO_TIPO\",base_object_name as \"REQUIERE\",base_object_type as \"REQUIERE_TIPO\"  from \"SYS\".\"OBJECT_DEPENDENCIES\" where base_schema_name = 'BSCL' AND base_object_type not in ('TABLE','SEQUENCE') and dependent_object_type not in ('SYNONYM') order by dependent_object_name ");
		while (rset.next()){
			String p1= rset.getString(rset.findColumn("OBJETO"));
			String p2=rset.getString(rset.findColumn("OBJECTO_TIPO"));
			String p3=rset.getString(rset.findColumn("REQUIERE"));		
			String p4=rset.getString(rset.findColumn("REQUIERE_TIPO"));	
			deps.add(p1+"\t"+p2+"\t"+p3+"\t"+p4);
		}
		rset.close();
		cs.close();    
		return deps.toArray(new String[deps.size()]);
	}
	
	public static HashMap<String,Deploy.Objdb> creaEstructuraObjetos(String vBasePath,String filesDeps[],String filesTargets[]) throws IOException {
		HashMap<String,Deploy.Objdb> est=new HashMap<String, Deploy.Objdb>();
		//Definidos en DEPs
		for (int i=0;i<filesDeps.length;i++) {
			String str=filesDeps[i].replace("\r", "").trim();
			if (str.length()==0) continue;
			if (str.startsWith("#")) continue;
			String param[]=filesDeps[i].split("\t");
			String srv=param[0].replace("::", ".");
			if (!est.containsKey(srv)) {
				Objdb obj=new Objdb();
				obj.ejecutado=false;
				obj.tipo=param[1];
				est.put(srv, obj);
			}
			Deploy.Objdb obj=est.get(srv);
			obj.objRequeridos.add(param[2].replace("::", "."));
		}  		
		//Los que ya nos indicadron
		for (int i=0;i<filesTargets.length;i++) {
			String str=filesTargets[i].replace("\r", "").trim();
			if (str.length()==0) continue;
			if (str.startsWith("#")) continue;	
			String srv=fileName2Srv(str);
			if (!est.containsKey(srv)) {
				Objdb obj=new Objdb();
				obj.ejecutado=false;
				obj.tipo="SCRIPT";
				est.put(srv, obj);
			}				
		}
		return est;
 	}
	
	static class  Objdb {
		public ArrayList<String> objRequeridos=new ArrayList<String>();
		public String tipo="";
		public boolean ejecutado=false;
		public Objdb() {			
		}
	}

	static class objColumn {
		String TABLE_NAME;
		int    POSITION;
		String COLUMN_NAME;
		String DATA_TYPE_NAME;
		int    LENGTH;
		int    SCALE;
		String    IS_NULLABLE;
		String    DEFAULT_VALUE;
		public objColumn() {			
		}		
	}

	static class objFK {
		String CONSTRAINT_NAME;
				
		String TABLE_NAME;
		String COLUMN_NAME;		
		
		String REFERENCED_TABLE_NAME;
		String REFERENCED_COLUMN_NAME;		
		public objFK() {			
		}		
	}
	
	static class objPKUK {
		String TABLE_NAME;
		String COLUMN_NAME;		
		String IS_PRIMARY_KEY;
		String IS_UNIQUE_KEY;		
		public objPKUK() {			
		}		
	}	
	
	static class objSQ {
		String OBJECT_NAME;				
		public objSQ() {			
		}		
	}	
	
	static class objINX {
		String OBJECT_NAME;				
		public objINX() {			
		}		
	}	
	
	
	static class objTable {
		String TABLE_NAME;	
		boolean is_type=false;
		boolean is_gtemp=false;
		boolean is_delete=false;
		public HashMap<String,objColumn> objColumns=new HashMap<String,objColumn>();
		public objTable() {			
		}		
	}
	
	private static byte[] convertIntToByteArray(int val, ByteOrder bo) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(bo);
		buffer.putInt(val);
		return buffer.array();
	}
	
	private static int convertByteArrayToInt(byte[] bytebuf, ByteOrder bo) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(bo);
		buffer.put(bytebuf);
		return buffer.getInt(0);
	}
	
	private static byte[] encodeRequest(String strJSON, byte[] binContent) throws UnsupportedEncodingException {
		
		String strContent="";
		byte[] result = null;
		byte[] bArrJSON = strJSON.getBytes("UTF-8");
		byte[] bRepoV1 = "repoV1".getBytes("UTF-8");
		byte[] bArrStrCont = new byte[0];
		byte[] reqAsByteArr;		
		
		if (binContent!=null)
			strContent=  new String(binContent, "UTF-8"); 
		
		byte nr_attachments = 1;
		if (strContent != null) {
			bArrStrCont = strContent.getBytes("UTF-8");
			nr_attachments = (byte) (nr_attachments + 1);
			if (binContent == null) {
				binContent = new byte[0];
			}
			if (binContent.length != 0) {
				nr_attachments = (byte) (nr_attachments + 1);
			}
		}
		byte[] bNoAttachm = { nr_attachments };
		byte[] bLenAttachm1 = convertIntToByteArray(bArrJSON.length,
				ByteOrder.LITTLE_ENDIAN);

		if (nr_attachments == 1) {
			result = new byte[bRepoV1.length + bNoAttachm.length
					+ bLenAttachm1.length + bArrJSON.length];

			int pos = 0;
			System.arraycopy(bRepoV1, 0, result, 0, bRepoV1.length);
			pos += bRepoV1.length;
			System.arraycopy(bNoAttachm, 0, result, pos, bNoAttachm.length);
			pos += bNoAttachm.length;
			System.arraycopy(bLenAttachm1, 0, result, pos, bLenAttachm1.length);
			pos += bLenAttachm1.length;
			System.arraycopy(bArrJSON, 0, result, pos, bArrJSON.length);
		} else {
			byte[] bLenAttachm2 = convertIntToByteArray(bArrStrCont.length,
					ByteOrder.LITTLE_ENDIAN);
			byte[] bLenAttachm3 = convertIntToByteArray(binContent.length,
					ByteOrder.LITTLE_ENDIAN);
			result = new byte[bRepoV1.length + bNoAttachm.length
					+ bLenAttachm1.length + bArrJSON.length
					+ bLenAttachm2.length + bArrStrCont.length
					+ bLenAttachm3.length + binContent.length];
			int pos = 0;
			System.arraycopy(bRepoV1, 0, result, 0, bRepoV1.length);
			pos += bRepoV1.length;
			System.arraycopy(bNoAttachm, 0, result, pos, bNoAttachm.length);
			pos += bNoAttachm.length;
			System.arraycopy(bLenAttachm1, 0, result, pos, bLenAttachm1.length);
			pos += bLenAttachm1.length;
			System.arraycopy(bArrJSON, 0, result, pos, bArrJSON.length);
			pos += bArrJSON.length;

			System.arraycopy(bLenAttachm2, 0, result, pos, bLenAttachm2.length);
			pos += bLenAttachm2.length;

			System.arraycopy(bArrStrCont, 0, result, pos, bArrStrCont.length);
			pos += bArrStrCont.length;

			System.arraycopy(bLenAttachm3, 0, result, pos, bLenAttachm3.length);
			pos += bLenAttachm3.length;

			System.arraycopy(binContent, 0, result, pos, binContent.length);
			pos += binContent.length;
		}

		reqAsByteArr = result;
		return ((byte[])reqAsByteArr.clone());
	}
	
	 private static String decodeResponse(byte[] respAsByteArr) {
		

		String result = "";

		String strAttachment = "";
		byte[] binAttachment = null;
		String respAsString = null;

		int nr_attachments = respAsByteArr[6];
		byte[] bLen = new byte[4];
		System.arraycopy(respAsByteArr, 7, bLen, 0, 4);
		int iLength = convertByteArrayToInt(bLen, ByteOrder.LITTLE_ENDIAN);

		int pos = 11;
		try {
			result = new String(respAsByteArr, pos, iLength, "UTF-8");
		} catch (UnsupportedEncodingException localUnsupportedEncodingException1) {
		}

		pos += iLength;

		if (nr_attachments > 1) {
			System.arraycopy(respAsByteArr, pos, bLen, 0, 4);
			iLength = convertByteArrayToInt(bLen, ByteOrder.LITTLE_ENDIAN);
			pos += 4;
			try {
				strAttachment = new String(respAsByteArr, pos, iLength, "UTF-8");
				//textAndBinaryContent.setTextData(strAttachment);
			} catch (UnsupportedEncodingException localUnsupportedEncodingException2) {
			}
			pos += iLength;
		}

		if (nr_attachments > 2) {
			System.arraycopy(respAsByteArr, pos, bLen, 0, 4);
			pos += 4;
			iLength = convertByteArrayToInt(bLen, ByteOrder.LITTLE_ENDIAN);
			binAttachment = new byte[iLength];
			//textAndBinaryContent.setBinaryData(binAttachment);
			System.arraycopy(respAsByteArr, pos, binAttachment, 0, iLength);
		}
		//fileContent.add(textAndBinaryContent);
		respAsString = result;
		return respAsString;
	}
	 
	private static void RepoActivate(java.sql.Connection connection,String pkg,String name,String suffix) throws Exception {
		CallableStatement cs = null;
		byte[] reqAsBArr = null;
		byte[] respAsByteArr = null;		
		String json="";
		String resp="";		
		json="{\"action\":\"activate\",\"what\":\"objects\",\"session\":{\"sessionType\":\"2\",\"workspace\":\"\"},\"objList\":[{\"package\":\""+pkg+"\",\"name\":\""+name+"\",\"tenant\":\"\",\"suffix\":\""+suffix+"\"}],\"activationMode\":\"0\"}";
		reqAsBArr=encodeRequest(json,null);
		cs = connection.prepareCall("{ call SYS.REPOSITORY_REST (?,?) }");
		cs.setBytes(1, reqAsBArr);
		cs.execute();
		respAsByteArr = cs.getBytes(2);
		resp=decodeResponse(respAsByteArr); 
		cs.close();
		JSONObject jObject=(JSONObject)JSONValue.parse(resp);
		if (!resp.contains("\"error-msg\": \"No error\"")) 
			throw new Exception("RepoActivate "+jObject.get("error-msg"));
	}
	
	private static void RepoRevert(java.sql.Connection connection,String pkg,String name,String suffix) throws Exception {
		CallableStatement cs = null;
		byte[] reqAsBArr = null;
		byte[] respAsByteArr = null;		
		String json="";
		String resp="";		
		json="	{\"what\":\"revertObjects\",\"session\":{\"workspace\":\"\",\"sessionType\":\"2\",\"timestamp\":\"2015-12-05 18:53:33.217\"},\"action\":\"delete\",\"objList\":[{\"package\":\""+pkg+"\",\"name\":\""+name+"\",\"suffix\":\""+suffix+"\",\"tenant\":\"\"}]}";
		reqAsBArr=encodeRequest(json,null);
		cs = connection.prepareCall("{ call SYS.REPOSITORY_REST (?,?) }");
		cs.setBytes(1, reqAsBArr);
		cs.execute();
		respAsByteArr = cs.getBytes(2);
		resp=decodeResponse(respAsByteArr); 
		cs.close();
		JSONObject jObject=(JSONObject)JSONValue.parse(resp);
		if (!resp.contains("\"error-msg\": \"No error\"")) 
			throw new Exception("RepoRevert "+jObject.get("error-msg"));
	}
		
	private static void RepoWrite(java.sql.Connection connection,String pkg,String name,String suffix,int version,byte[] script) throws Exception {
		CallableStatement cs = null;
		byte[] reqAsBArr = null;
		byte[] respAsByteArr = null;		
		String json="";
		String resp="";		
		json="{\"action\":\"write\",\"what\":\"object\",\"session\":{\"sessionType\":\"2\",\"workspace\":\"\"},\"object\":{\"package\":\""+pkg+"\",\"name\":\""+name+"\",\"tenant\":\"\",\"suffix\":\""+suffix+"\"},\"determineReferences\":\"0\",\"createPackage\":\"1\",\"metadata\":{\"version_id\":\""+version+"\",\"type\":\"1\"}}";
		reqAsBArr=encodeRequest(json,script);
		cs = connection.prepareCall("{ call SYS.REPOSITORY_REST (?,?) }");
		cs.setBytes(1, reqAsBArr);
		cs.execute();		
		respAsByteArr = cs.getBytes(2);
		resp=decodeResponse(respAsByteArr);
		cs.close();
		JSONObject jObject=(JSONObject)JSONValue.parse(resp);
		//"error-msg":"Repository: Version mismatch;inactive version exists already
		if (jObject.containsKey("error-code") && jObject.get("error-code").equals("40124"))  {
			RepoRevert(connection, pkg, name, suffix);	
			int verisonnew=RepoVersion(connection,pkg, name, suffix)+0;
			RepoWrite(connection, pkg, name, suffix, verisonnew, script);		
		}
		else if (!resp.contains("\"error-msg\": \"No error\"")) 			
			throw new Exception("RepoWrite "+jObject.get("error-msg"));
	}	
	
	private static int RepoVersion(java.sql.Connection connection,String pkg,String name,String suffix) throws SQLException, UnsupportedEncodingException {
		CallableStatement cs = null;
		byte[] reqAsBArr = null;
		byte[] respAsByteArr = null;		
		String json="";
		String resp="";		
		json="{\"what\":\"object\",\"session\":{\"workspace\":\"\",\"sessionType\":\"1\"},\"action\":\"read\",\"lang\":\"es_CL\",\"object\":{\"package\":\""+pkg+"\",\"name\":\""+name+"\",\"suffix\":\""+suffix+"\",\"tenant\":\"\"}}";
		reqAsBArr=encodeRequest(json,null);
		cs = connection.prepareCall("{ call SYS.REPOSITORY_REST (?,?) }");
		cs.setBytes(1, reqAsBArr);
		cs.execute();		
		respAsByteArr = cs.getBytes(2);
		resp=decodeResponse(respAsByteArr);
		cs.close();
		JSONObject jObject=(JSONObject)JSONValue.parse(resp);
		if (jObject.containsKey("error-code")) return 0;
		if (!jObject.containsKey("metadata")) return 0;
		JSONObject jMetaData=(JSONObject)jObject.get("metadata");
		if (!jMetaData.containsKey("version_id")) return 0;
		return Integer.parseInt((String) jMetaData.get("version_id"));
	}	
	

	
	public static String ExecuteREPO(java.sql.Connection connection,String pkg,String name,String suffix,  byte[] script){
		try {
			int version=RepoVersion(connection,pkg, name, suffix);
			RepoWrite(connection,pkg, name, suffix,version,script);
			RepoActivate(connection,pkg, name, suffix);			
			return "OK+";
	    } catch (Exception e) {	    	 
	    	return e.getMessage();
	    }		
	}	
	
	
	public static String ExecuteSQL(java.sql.Connection connection,String script){
		try {
	        Statement cs = connection.createStatement();
	        //Arregla algunas cosas del script
	        if (script.toUpperCase().contains("GRANT")) {
	        	script=script.replace("BCS_DESARROLLADOR", "BCS");
	        	script=script.replace("bcs_desarrollador", "BCS");	        	
	        }
	        cs.execute(script); 
	        return "OK+";
	    } catch (Exception e) {
	    	if (script.trim().toLowerCase().startsWith("drop") && e.getMessage().contains("invalid table name"))
	    		return "OK+";
	    	if (script.trim().toLowerCase().startsWith("drop") && e.getMessage().contains("invalid user-defined type name"))
	    		return "OK+";
	    	if (script.trim().toLowerCase().startsWith("drop") && e.getMessage().contains("invalid view name"))
	    		return "OK+";	 
	    	if (script.trim().toLowerCase().startsWith("drop") && e.getMessage().contains("invalid name of function or procedure"))
	    		return "OK+";	 	    	
	    	if (script.trim().toLowerCase().startsWith("drop") && e.getMessage().contains("invalid sequence"))
	    		return "OK+";	 	    	
	    	return e.getMessage();
	    }		
	}	
	
	public static PrintWriter logout=null;
	public static void WriteLog(String dato) {
		if (logout==null) {
			Date d = Calendar.getInstance().getTime(); 
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HHmm"); 
			String logname = "";
			if (System.getenv("LOG_DEPLOY")!= null && System.getenv("LOG_DEPLOY").length()>0) {
				logname = "LOG_"+System.getenv("LOG_DEPLOY")+".txt";
			}
			else {
				logname = "LOG_"+sdf.format(d)+".txt";
			}
			try {
				logout = new PrintWriter(new BufferedWriter(new FileWriter(logname, true)));
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		 try {
			System.out.println(dato);
			if (logout!=null) {
				logout.println(dato);  logout.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	
	public static BigDecimal getSequenceVal(java.sql.Connection connection,String seq){
		ResultSet Rset=null; 
		try {
            Statement cs = connection.createStatement() ;
            Rset = cs.executeQuery("select BSCL.\""+seq+"\".NEXTVAL as VAL from dummy");                
            if (Rset !=null){
            	if (Rset.next()) { 
            		return Rset.getBigDecimal("VAL");
            	}
            }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		return new BigDecimal(1);
	}	
	
	public static ResultSet getTableColoumnList(java.sql.Connection connection,String table){
		ResultSet Rset=null;
		try {
            if (connection != null) {
                Statement cs = connection.createStatement() ;
                Rset = cs.executeQuery("select COLUMN_NAME,DATA_TYPE_NAME  from SYS.TABLE_COLUMNS where SCHEMA_NAME = 'BSCL' and TABLE_NAME ='"+table+"' order by POSITION");                
                if (Rset !=null){
                	return Rset;
                }
            }
	    } catch (Exception e) {
	           e.printStackTrace();
	    }
		return Rset;
	}
	
	public static ResultSet getTableData(java.sql.Connection connection,String table) {
		ResultSet Rset=null;
		try {
            if (connection != null) {
                Statement cs = connection.createStatement();
                Rset = cs.executeQuery("select * from \""+table+"\"");
                if (Rset !=null){
                	return Rset;
                }
            }
           
	    } catch (Exception e) {
	          e.printStackTrace();
	    }
		return Rset;
	}
	
	
	public static String GetObjectDefinition(java.sql.Connection connection,String schema,String object) {
		ResultSet rsetDef=null;
		Statement cs2=null;
		try{			
			cs2 = connection.createStatement() ;
			rsetDef = cs2.executeQuery("CALL \"SYS\".\"GET_OBJECT_DEFINITION\"('"+schema+"', '"+object+"')");
			if (rsetDef.next()) return rsetDef.getString("OBJECT_CREATION_STATEMENT");	
			return "";
		} 		
		catch(Exception e){
			return "";
		}
		finally {
			try {
				if (rsetDef!= null) rsetDef.close();
				if (cs2!= null) cs2.close();
			}catch(Exception er) {
			}			
		}
		
	}
	public static void saveSchema(java.sql.Connection connection,String schema) throws IOException, SQLException {
		ResultSet rset;
		ResultSet rsetDef;
		String definition;
		int posDefinition;
		String tableName;
		String fileNameN;		
		{
			File newfile = new File("SCRIPT_TABLES_FULL.sql");													
			FileWriter out = new FileWriter(newfile,false);	
			out.write("SET SCHEMA \""+schema+"\"; \r\n \r\n");
			Statement cs = connection.createStatement() ;
			rset = cs.executeQuery("SELECT TABLE_NAME FROM SYS.TABLES WHERE SCHEMA_NAME = '"+schema+"'  ORDER BY TABLE_TYPE, TABLE_NAME ASC");
			fileNameN="";
			String FKs="";
			while ( rset.next()){
				try{
					
					posDefinition= rset.findColumn("TABLE_NAME");
					tableName =rset.getString(posDefinition);				
					System.out.println("Procesando Tabla:"+tableName);
					Statement cs2 = connection.createStatement() ;
					rsetDef = cs2.executeQuery("CALL \"SYS\".\"GET_OBJECT_DEFINITION\"('"+schema+"', '\""+tableName+"\"')");
					if (rsetDef.next()){						
						posDefinition= rsetDef.findColumn("OBJECT_CREATION_STATEMENT");
						definition= rsetDef.getString(posDefinition);							
						int posk=definition.indexOf("ALTER TABLE ");
						if (posk>0) {
							String nelem=definition.substring(posk)+";\n";
							nelem = nelem.replace("\n;\n",";\n");
							nelem = nelem.replace(";;",";");							
							FKs+=nelem;
							definition=definition.substring(0,posk-1);
						}
						definition = definition.replaceAll("\" \\(", "\" \\(\n ");
						definition = definition.replaceAll(", \"", ",\n \"");
						definition = definition.replace("UNLOAD PRIORITY 5  AUTO MERGE","");
						definition = definition.replace("\""+schema+"\".","");
						definition = definition+";\r\n \r\n";
						definition = definition.replace(";;",";");
						System.out.println(definition);
						out.append(definition);	
						out.flush();
					}																															
				} 		
				catch(Exception e){
					e.printStackTrace();
				}
			}
			out.close();
			//FK INDEX
			File newfile2 = new File("SCRIPT_FK_INDEX_FULL.sql");													
			FileWriter out2 = new FileWriter(newfile2,false);	
			out2.write("SET SCHEMA \""+schema+"\"; \r\n \r\n");			
			out2.append(FKs+"\r\n");	
			out2.close();					
		}			
		//--------------------------------------------------------------------
		//SCRIPTS SEQUENCES
		//--------------------------------------------------------------------
		{
			File newfile = new File("SCRIPT_SEQS_FULL.sql");													
			FileWriter out = new FileWriter(newfile,false);	
			out.write("SET SCHEMA \""+schema+"\"; \r\n \r\n");
			Statement cs = connection.createStatement() ;
			rset = cs.executeQuery("select * from SYS.SEQUENCES where schema_name = '"+schema+"'");
			while ( rset.next()){
				try{
					String SEQUENCE_NAME =rset.getString(rset.findColumn("SEQUENCE_NAME"));		
					String START_NUMBER = getSequenceVal(connection,SEQUENCE_NAME)+"";		
					String MIN_VALUE =rset.getBigDecimal(rset.findColumn("MIN_VALUE"))+"";		
					String MAX_VALUE =rset.getBigDecimal(rset.findColumn("MAX_VALUE"))+"";		
					String INCREMENT_BY =rset.getBigDecimal(rset.findColumn("INCREMENT_BY"))+"";		
					String IS_CYCLED =rset.getString(rset.findColumn("IS_CYCLED"));		
					System.out.println("Procesando SEQUENCE_NAME:"+SEQUENCE_NAME);	
					String q="CREATE SEQUENCE \""+SEQUENCE_NAME+"\" START WITH "+START_NUMBER+" INCREMENT BY "+INCREMENT_BY+"  MINVALUE "+MIN_VALUE+" MAXVALUE "+MAX_VALUE+ " ";
					if (IS_CYCLED.equalsIgnoreCase("TRUE")) q+=" CYCLE";
					else  q+=" NO CYCLE";
					out.append(q+";\r\n");	
					out.flush();					
				} 		
				catch(Exception e){
					e.printStackTrace();
				}
			}
			out.close();				
		}
	}	
	
	private static class Worker extends Thread {
		  private final Process process;
		  private Integer exit;
		  private Worker(Process process) {
		    this.process = process;
		  }
		  public void run() {
		    try { 
		      exit = process.waitFor();
		    } catch (InterruptedException ignore) {
		      return;
		    }
		  }  
	}	
}