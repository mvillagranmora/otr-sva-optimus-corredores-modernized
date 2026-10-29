import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import javax.swing.text.Position;

public class Tools {
	
	private static java.sql.Connection connection = null;
    
    public static void main(String[] args) throws SQLException, IOException {
		
		ResultSet rset =null;
		ResultSet rsetCol =null;
		ResultSet rsetData =null;
		ResultSet rsetDef =null;
				
		String definition="";
		int posDefinition=0;
		String tableName="";
		String fileName="";
		String fileNameN="";
		
		Locale.setDefault(new Locale("en", "EN"));
			
	
		
		{
			File newfile = new File("SCRIPT_UPDATE_MAYUS.sql");													
			FileWriter out = new FileWriter(newfile,false);	
			out.write("SET SCHEMA \"BSCL\"; \r\n \r\n");			
			rset = getTableList(true);
			while ( rset.next()){
				String SQLUPDATE="";
				try{
					posDefinition= rset.findColumn("TABLE_NAME");
					tableName =rset.getString(posDefinition);											
					//Tablas a excluir
					if (tableName.equalsIgnoreCase("BSCL.Plataforma.BD::PLA.Configuracion"						))  continue;
					if (tableName.equalsIgnoreCase("BSCL.Plataforma.BD::PLA.Servicio"							))  continue;	
					if (tableName.equalsIgnoreCase("BSCL.CuentaInversion.BD::CTA.EstadoCuentaInversion"			))  continue;	
					if (tableName.equalsIgnoreCase("BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"		))  continue;	
					if (tableName.equalsIgnoreCase("BSCL.Tesoreria.BD::TES.EstadoTesoreria"						))  continue;	
					if (tableName.equalsIgnoreCase("BSCL.Tesoreria.BD::TES.ConceptoTesoreria"					))  continue;	
					if (tableName.equalsIgnoreCase("BSCL.Facturacion.BD::FAC.EstadoFacturacion"					))  continue;	
					if (tableName.equalsIgnoreCase("BSCL.Facturacion.BD::FAC.ConceptoFacturacion"				))  continue;	
					if (tableName.equalsIgnoreCase("BSCL.Plataforma.BD::PLA.TablaConversion"					))  continue;
					if (tableName.equalsIgnoreCase("BSCL.Plataforma.BD::ACC.Acceso"								))  continue;
					if (tableName.equalsIgnoreCase("BSCL.Custodia.BD::CUS.GenerarMovimientoCustodiaLog"			))  continue;
					
					
					if (tableName.startsWith(	   "BSCL.Normativa.BD::"										))  continue;	
					
					
					
					//Columnas
					rsetCol=getTableColoumnList(tableName);
					while ( rsetCol.next()){
						String cname = rsetCol.getString(rsetCol.findColumn("COLUMN_NAME"));
						String tname = rsetCol.getString(rsetCol.findColumn("DATA_TYPE_NAME"));
						//Campos
						if (tname.equalsIgnoreCase("BIGINT")) {}
						else if (tname.equalsIgnoreCase("INT")) {}
						else if (tname.equalsIgnoreCase("INTEGER")) {}
						else if (tname.equalsIgnoreCase("TIME")) {}
						else if (tname.equalsIgnoreCase("DECIMAL")) {}
						else if (tname.equalsIgnoreCase("BLOB")) {}
						else if (tname.equalsIgnoreCase("TIMESTAMP")) {}
						else if (tname.equalsIgnoreCase("SMALLINT")) {}
						else if (tname.equalsIgnoreCase("DATE")) {}
						else if (tname.equalsIgnoreCase("TEXT")) {}
						else if (tname.equalsIgnoreCase("NCLOB")) {}
						else if (tname.equalsIgnoreCase("SECONDDATE")) {}
						else if (tname.equalsIgnoreCase("NVARCHAR") || tname.equalsIgnoreCase("VARCHAR") || tname.equalsIgnoreCase("CHAR")   || tname.equalsIgnoreCase("TEXT")   ) {	
							//revisa si tiene un caracter en minuscula
							if (!getColumnIsFullUpper(tableName,cname)) {
								SQLUPDATE+="UPDATE BSCL.\""+tableName+"\"  SET \""+cname+"\" = UPPER(\""+cname+"\") ; --"+tname+"\r\n";
							}
							else {
								System.out.println("COLUMNA -> "+tableName+" --- "+cname+" --- "+tname+"  EN  MAYUSCULAS");	
							}
						}						
						else {
							System.out.println("TIPO!! -> "+tableName+" --- "+cname+" --- "+tname);							
						}						
					}
					rsetCol.close();
				}
				catch(Exception e){
					e.printStackTrace();
				}			
				if (SQLUPDATE.length()>0) {
					out.append(SQLUPDATE+"\r\n");	
					out.flush();				
				}				
			}
			out.close();
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		System.exit(0);			
		
		
		//--------------------------------------------------------------------
		//SCRIPTS TABLAS - FK - INDEX - TYPES
		//--------------------------------------------------------------------
		{
			File newfile = new File("SCRIPT_TABLES_FULL.sql");													
			FileWriter out = new FileWriter(newfile,false);	
			out.write("SET SCHEMA \"BSCL\"; \r\n \r\n");
			rset = getTableList(false);
			fileNameN="";
			String FKs="";
			while ( rset.next()){
				try{
					
					posDefinition= rset.findColumn("TABLE_NAME");
					tableName =rset.getString(posDefinition);				
					System.out.println("Procesando Tabla:"+tableName);
					rsetDef = getTableDefinition(tableName);
					if (rset!=null){						
						posDefinition= rsetDef.findColumn("OBJECT_CREATION_STATEMENT");
						definition= rsetDef.getString(posDefinition);							
						int posk=definition.indexOf("ALTER TABLE ");
						if (posk>0) {
							FKs+=definition.substring(posk).replace("\n",";\n");							
							definition=definition.substring(0,posk-1);
						}
						definition = definition.replaceAll("\" \\(", "\" \\(\n ");
						definition = definition.replaceAll(", \"", ",\n \"");
						definition = definition.replace("UNLOAD PRIORITY 5  AUTO MERGE","");
						definition = definition.replace("\"BSCL\".","");																						
						out.append(definition+";\r\n");	
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
			out2.write("SET SCHEMA \"BSCL\"; \r\n \r\n");			
			out2.append(FKs+"\r\n");	
			out2.close();					
		}			
		//--------------------------------------------------------------------
		//SCRIPTS SEQUENCES
		//--------------------------------------------------------------------
		{
			File newfile = new File("SCRIPT_SEQS_FULL.sql");													
			FileWriter out = new FileWriter(newfile,false);	
			out.write("SET SCHEMA \"BSCL\"; \r\n \r\n");
			rset = getSequences();
			while ( rset.next()){
				try{
					String SEQUENCE_NAME =rset.getString(rset.findColumn("SEQUENCE_NAME"));		
					String START_NUMBER = getSequenceVal(SEQUENCE_NAME)+"";		
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
		System.exit(0);			
	    //--------------------------------------------------------------------
		//dfsfsdf
		//--------------------------------------------------------------------			
		rset = getDependencias();
		System.out.println("Dependencias:");
		while ( rset.next()){
			String p1= rset.getString(rset.findColumn("OBJETO"));
			String p2=rset.getString(rset.findColumn("OBJECTO_TIPO"));
			String p3=rset.getString(rset.findColumn("REQUIERE"));		
			String p4=rset.getString(rset.findColumn("REQUIERE_TIPO"));					
			System.out.println(p1+"\t"+p2+"\t"+p3+"\t"+p4);
		}
		System.out.println("FIN");
		System.exit(0);		
		
	    //--------------------------------------------------------------------
		//genera script de  insert de datos
		//--------------------------------------------------------------------	
		rset = getRepoDataSQL("BSCL.");
		System.out.println("Nombre de servicios incorrectos:");
		while ( rset.next()){
			String pkg= rset.getString(rset.findColumn("PACKAGE_ID"));
			String bdta=rset.getString(rset.findColumn("BDATA"));
			String cdta=rset.getString(rset.findColumn("CDATA"));		
			String name=rset.getString(rset.findColumn("OBJECT_NAME"));					
			try{
				//System.out.println("Procesando "+pkg+ " "+name);				
				if (bdta==null || bdta=="") bdta=cdta;
				String lns[]=bdta.split("\n");				
				for (int ii=0;ii<lns.length && ii<50;ii++) {					
					String ln=lns[ii];
					if (ln.toLowerCase().contains("drop")) {
						String elem=ln;
						while (elem.indexOf("  ")>0) elem=elem.replace("  "," ");
						elem=elem.split(" ")[2];
						elem=elem.replace("\"BSCL\".","");
						elem=elem.replace("\r","");	
						elem=elem.replace(";","");	
						elem=elem.replace("\"","");			
						String elems[]=elem.replace("::",".").split("\\.");
						String nelem=elems[elems.length-1];
						String subspack[]=pkg.split("\\.");
						if (subspack.length<4) break;
						String norm_pkg=subspack[0]+"."+subspack[1]+"."+subspack[2]+"."+subspack[3];
						if (subspack.length==5) norm_pkg+="::"+subspack[4]+"."+name;												
						else  norm_pkg+="::"+name;						
						if (!(subspack[3].equals("Servicios") || 
								subspack[3].equals("Reglas")  ||
								subspack[3].equals("Vistas")  ||
								subspack[3].equals("Utilitarios")
						)) {
							System.out.println("'"+norm_pkg+"' != '"+elem+"' *** NO CORRESPONDE ESTRUCTURA (4)");	
						}
						else if (!norm_pkg.trim().equals(elem.trim())) {
							System.out.println("'"+norm_pkg+"' != '"+elem+"' *** NO CORRESPONDE RUTA CON NOMBRE SERVICIO");							
						}
						else if (!name.trim().equals(nelem.trim())) {	
							System.out.println("'"+name+"' != '"+nelem+"' *** NO CORRESPONDE NOMBRE ARCHIVO CON NOMBRE SERVICIO");								
						}						
						else if (!elem.contains(norm_pkg)) {
							//No corresponde la ruta al nombre del servicio
							System.out.println("'"+elem+"' != '"+pkg+"' *** NO CUMPLE FORMATO");
						}						
						break;
					}
				}
				
			
				//SET SCHEMA "BSCL";

				//DROP PROCEDURE  "BSCL.Cliente.BD.Reglas::ClienteExiste";
			
				
			}
			catch(Exception e){
				System.out.println("Procesando "+pkg+ " "+name);	
				e.printStackTrace();
			}				
		}
		System.out.println("FIN");
		System.exit(0);
			
		
		/*
		//--------------------------------------------------------------------
		//genera script de  insert de datos
		//--------------------------------------------------------------------	
		rset = getTableList();
		while ( rset.next()){
			try{
				posDefinition= rset.findColumn("TABLE_NAME");
				tableName =rset.getString(posDefinition);				
				System.out.println("Procesando INSERT:"+tableName);
				
				String columnas="";
				String tipos="";
				int    ncols=0;
				rsetCol=getTableColoumnList(tableName);
				while ( rsetCol.next()){
					String cname = rsetCol.getString(rsetCol.findColumn("COLUMN_NAME"));
					String tname = rsetCol.getString(rsetCol.findColumn("DATA_TYPE_NAME"));
					columnas += "\""+cname +"\",";
					tipos += "\""+tname +"\",";	
					ncols++;
				}	
				columnas=columnas.substring(0,columnas.length()-1);
				String[] coltipos = tipos.split(",");
				
				String[] parts = tableName.split("::");
				if (parts.length>0) {	
					String modulo = parts[0].split("\\.")[1]; //Cliente
					String prefijo = parts[1].split("\\.")[0]; //CLI
					String tipo = "Inserts";  //Script Base datos
					String servicio = parts[1].split("\\.")[1]; //tabla	
					
					
					File dirModulo = new File(modulo);
					if (!dirModulo.exists()) dirModulo.mkdir();
					
					File dirModuloTipo = new File(modulo+"\\"+tipo);
					if (!dirModuloTipo.exists()) dirModuloTipo.mkdir();
					
					File newfile = new File(dirModuloTipo.getAbsolutePath()+"\\"+prefijo+"_"+servicio+".sql");													
					out = new FileWriter(newfile);				
					
					out.write("SET SCHEMA \"BSCL\"; \r\n \r\n");				
					out.append("DELETE FROM  \""+tableName+"\"; \r\n \r\n");
					
					//loop over datos				
					rsetData=getTableData(tableName,columnas);
					while (rsetData.next()){
						out.append("INSERT INTO \""+tableName+"\" ( "+columnas+" )  \r\nVALUES(");
						for (int i=0;i<ncols;i++) {
							String val=rsetData.getString(i+1);
							if (coltipos[i].contains("CHAR"))  out.append("'"+val+"'");
							else if (coltipos[i].contains("DATE"))  out.append("'"+val+"'");
							else if (coltipos[i].contains("INT")) out.append(val);
							else if (coltipos[i].contains("DECIMAL")) out.append(val);
							else out.append("'"+val+"'");						
							if (i<ncols-1) out.append(",");
						}	
						out.append(");\r\n");
					}				
					//end file
					out.append("\r\n\r\n");
					out.append("commit;\r\n");
					out.close();				
				}	
			}
			catch(Exception e){
				e.printStackTrace();
			}				
		}*/

		
		//--------------------------------------------------------------------		
		//obtiene listado de vistas
		//--------------------------------------------------------------------		
		/*
		rset = getViewList();
		fileNameN="";
		while ( rset.next()){
			try{
				String name = rset.getString(rset.findColumn("VIEW_NAME"));
				String view = rset.getString(rset.findColumn("DEFINITION"));				
				//BSCL.Cliente.BD::CLI.Servicios.ConsultarCliente
				//BSCL.CuentaInversion.BD.Servicios::ConsultarCuentaInversionPortafolioCLiente		
				System.out.println("Procesando Vista:"+name);
				String[] parts = name.split("::");
				if (parts.length>0) {										
					String modulo = parts[0].split("\\.")[1]; //Cliente
					String prefijo = "";  //CLI
					String tipo = "";     //Servicios					
					String servicio = ""; //ConsultarCliente										
					if (parts[1].split("\\.").length==1) {
						 prefijo = parts[0].split("\\.")[3]; //NA
						 tipo = parts[0].split("\\.")[3];  //Servicios
						 servicio = parts[1].split("\\.")[0]; //ConsultarCuentaInversionPortafolioCLiente							
					}else {
						 prefijo = parts[1].split("\\.")[0]; //CLI
						 tipo = parts[1].split("\\.")[1];  //Servicios
						 servicio = parts[1].split("\\.")[2]; //ConsultarCliente						
					}
															
					File dirModulo = new File(modulo);
					if (!dirModulo.exists()) dirModulo.mkdir();
					
					File dirModuloTipo = new File(modulo+"\\"+tipo);
					if (!dirModuloTipo.exists()) dirModuloTipo.mkdir();					
										
					
					File newfile = new File(dirModuloTipo.getAbsolutePath()+"\\"+servicio+".sql");
					out = new FileWriter(newfile);
					
					
					out.write("SET SCHEMA \"BSCL\"; \r\n \r\n");
					out.append("DROP VIEW \""+name+"\"; \r\n \r\n");
					out.append("CREATE VIEW \""+name+"\"  AS \r\n");					
					out.append(view+";");
					out.append("\r\n\r\n");					
					out.append("GRANT SELECT ON \""+name+"\" TO  BCS_DESARROLLADOR WITH GRANT OPTION; \r\n");										
					out.close();
			
				}			
			} 
			catch(Exception e){
				e.printStackTrace();
			}
		}	*/
		
		
		//--------------------------------------------------------------------
		//obtiene listado de secuencias
		//--------------------------------------------------------------------
		
		
		

		
	}
    
    
	//--------------------------------------------------------------------		
	//utilidades
	//--------------------------------------------------------------------	    
	
	private static void guardarFile(FileWriter out, String string) throws IOException {
		System.out.println(string); 
		out.append(string);
		out.append("\n");
		
	}
	
	public static ResultSet getSequences(){
		ResultSet Rset=null; 
		try {
            connection = getConnectionHana();
            if (connection != null) {
                Statement cs = connection.createStatement() ;
                Rset = cs.executeQuery("select * from SYS.SEQUENCES where schema_name = 'BSCL'");                
                if (Rset !=null){
                	return Rset;
                }
            }
           
	    } catch (Exception e) {
	            e.printStackTrace();
	    }
		return Rset;
	}	
	
	public static BigDecimal getSequenceVal(String seq){
		ResultSet Rset=null; 
		try {
            connection = getConnectionHana();
            if (connection != null) {
                Statement cs = connection.createStatement() ;
                Rset = cs.executeQuery("select BSCL.\""+seq+"\".NEXTVAL as VAL from dummy");                
                if (Rset !=null){
                	if (Rset.next()) { 
                		return Rset.getBigDecimal("VAL");
                	}
                }
            }
           
	    } catch (Exception e) {
	            e.printStackTrace();
	    }
		return new BigDecimal(1);
	}	
	
	public static ResultSet getTableDefinition(String tabla){
		ResultSet Rset=null; 
		try {
            connection = getConnectionHana();
            if (connection != null) {
                Statement cs = connection.createStatement() ;
                Rset = cs.executeQuery("CALL \"SYS\".\"GET_OBJECT_DEFINITION\"('BSCL', '\""+tabla+"\"')");
                Rset.next();
                if (Rset !=null){
                	return Rset;
                }
           }
           
	    } catch (Exception e) {
	            e.printStackTrace();
	    }
    return Rset;
	}
	public static ResultSet getViewList(){
		ResultSet Rset=null; 
		try {
            connection = getConnectionHana();
            if (connection != null) {
                Statement cs = connection.createStatement() ;
                Rset = cs.executeQuery("select view_name,definition from sys.VIEWS where schema_name = 'BSCL' order  by view_name");
                
                if (Rset !=null){
                	return Rset;
                }
            }
           
	    } catch (Exception e) {
	            e.printStackTrace();
	    }
		return Rset;
	}	
	public static ResultSet getSecuenceList(){
		ResultSet Rset=null; 
		try {
            connection = getConnectionHana();
            if (connection != null) {
                Statement cs = connection.createStatement() ;
                Rset = cs.executeQuery("select view_name,definition from sys.OBJECTS  where schema_name = 'BSCL' and object_type = 'SECUENCE' order  by view_name");
                
                if (Rset !=null){
                	return Rset;
                }
            }
           
	    } catch (Exception e) {
	            e.printStackTrace();
	    }
		return Rset;
	}
	
	public static boolean getColumnIsFullUpper(String table,String column){		
		try {
            connection = getConnectionHana();           
        	ResultSet Rset=null; 
        	Statement cs = connection.createStatement() ;  //   LOCATE("+column+",'u') > 0 OR             
        	String where="";
        	for (int i=97;i<=122;i++) {
        		String l=Character.toString((char)i);
        		where+=" LOCATE(\""+column+"\",'"+l+"') > 0 OR ";
        	}
        	where+=" 1=2 ";
        	
            Rset = cs.executeQuery("SELECT count(1) FROM BSCL.\""+table+"\" WHERE  "+where);
            int count=0;
            if (Rset.next()) count=Rset.getInt(1);
            Rset.close();
            cs.close();  
            if (count==0) return true;
            else return false;
     
	    } catch (Exception e) {
	          e.printStackTrace();
	          return false;
	    }
	}
	
	
	
	public static ResultSet getTableList(Boolean excluyeTypes){
		ResultSet Rset=null; 
		try {
            connection = getConnectionHana();
            if (connection != null) {
                Statement cs = connection.createStatement() ;
                String types=" AND IS_USER_DEFINED_TYPE <> 'TRUE' ";
                if (excluyeTypes==false) types="  ";
                Rset = cs.executeQuery("SELECT TABLE_NAME FROM SYS.TABLES WHERE SCHEMA_NAME = 'BSCL' "+types+"  AND  IS_TEMPORARY = 'FALSE'  ORDER BY TABLE_NAME ASC");
                
                if (Rset !=null){
                	return Rset;
                }
            }
           
	    } catch (Exception e) {
	            e.printStackTrace();
	    }
		return Rset;
	}
	
	public static ResultSet getRepoDataSQL(String modulo){
		ResultSet Rset=null; 
		try {
            connection = getConnectionHana();
            if (connection != null) {
                Statement cs = connection.createStatement() ;                               
                Rset = cs.executeQuery("SELECT package_id,OBJECT_NAME,cast(BINTOSTR(cast(BDATA as binary)) as varchar(500)) as BDATA,cast(BINTOSTR(cast(CDATA as binary)) as varchar(500)) as CDATA  FROM _SYS_REPO.ACTIVE_OBJECT WHERE  package_id like 'BSCL%' and OBJECT_SUFFIX = 'sql' and PACKAGE_ID not like '%Scripts%'  and PACKAGE_ID like '%"+modulo+"%' order by PACKAGE_ID  ");                
                if (Rset !=null){
                	return Rset;
                }
            }
           
	    } catch (Exception e) {
	            e.printStackTrace();
	    }
    return Rset;
	}
	
	
	public static ResultSet getDependencias(){
		ResultSet Rset=null; 
		try {
            connection = getConnectionHana();
            if (connection != null) {
                Statement cs = connection.createStatement() ;                                  
                Rset = cs.executeQuery("select dependent_object_name as \"OBJETO\",dependent_object_type as \"OBJECTO_TIPO\",base_object_name as \"REQUIERE\",base_object_type as \"REQUIERE_TIPO\"  from \"SYS\".\"OBJECT_DEPENDENCIES\" where base_schema_name = 'BSCL' AND base_object_type not in ('TABLE','SEQUENCE') and dependent_object_type not in ('SYNONYM') order by dependent_object_name ");                
                if (Rset !=null){
                	return Rset;
                }
            }
           
	    } catch (Exception e) {
	            e.printStackTrace();
	    }
    return Rset;
	}
		
	
	public static ResultSet getTableColoumnList(String table){
		ResultSet Rset=null; 
		try {
            connection = getConnectionHana();
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
	public static ResultSet getTableData(String table,String columnas){
		ResultSet Rset=null; 
		try {
            connection = getConnectionHana();
            if (connection != null) {
                Statement cs = connection.createStatement() ;
                Rset = cs.executeQuery("select "+columnas+"  from \""+table+"\"");                
                if (Rset !=null){
                	return Rset;
                }
            }
           
	    } catch (Exception e) {
	            e.printStackTrace();
	    }
		return Rset;
	}		

	
	public static Connection getConnectionHana() {
        try {
        	if (connection!=null && !connection.isClosed()) 
        		return connection; 
        	String cnx = "jdbc:sap://161.238.37.7:30415/?autocommit=false";
        	//String cnx = "jdbc:sap://161.238.34.246:30215/?autocommit=false";
        	connection = java.sql.DriverManager.getConnection(cnx,"SOPORTE", "Bolsa123");            
        } catch (Exception e) {
                e.printStackTrace();
                connection=null;                
        }
        return connection;
	}

    
}