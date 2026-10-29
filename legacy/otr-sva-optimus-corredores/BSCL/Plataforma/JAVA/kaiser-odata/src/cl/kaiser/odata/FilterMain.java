package cl.kaiser.odata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.dbcp.BasicDataSource;

import cl.kaiser.odata.gen.OdataFilterLexer;
import cl.kaiser.odata.gen.OdataFilterParser;

public class FilterMain {

	public static void main(String[] args) throws SQLException, IOException {		
		hanaExample();		
	}
	
	public static void postgreExample() throws SQLException, IOException {
		  Connection c = null;
		  try {
		     Class.forName("org.postgresql.Driver");
		     c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/dvdrental","demo", "demo");
		  } catch (Exception e) {
		     e.printStackTrace();
		     System.err.println(e.getClass().getName()+": "+e.getMessage());
		     System.exit(0);
		  }
		  System.out.println("Opened database successfully");		
		
		  long mseg=System.currentTimeMillis();
		  OdataImpSql odata=new OdataImpSql();	  	  
		  OutputStream out = new FileOutputStream(new File("demo.json"));
		  odata.QueryJsonObject(c, out, null,null, "rental","rental_id ne datetime'2016-06-06T00:00:00' AND ",null,15000,-1,true);
		  out.close();	  
		  System.out.println("\n\nDURA:"+(System.currentTimeMillis()-mseg));		
	}
	
	
	public static void hanaExample() throws SQLException, IOException {
		//Settings by file
        String dbURL  = "jdbc:sap://161.238.34.39:30215/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "SYSTEM";
        String pass   = "B0lsa123";		
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
			 Connection c = ds.getConnection();
			 System.out.println("Opened database successfully");					
			 long mseg=System.currentTimeMillis();
			 OdataImpSql odata=new OdataImpSql();	  	  
			 OutputStream out = new FileOutputStream(new File("demo.json"));
			 odata.QueryJsonObject(c, out, null,null,"\"BSCL.Orden.BD.Vistas::ConsultarOrden\"","\"PrecioOrden\" ge -1.1234 and \"FechaRecepcion\" ge datetime'2010-06-06T00:00:00'",null,15000,-1,true);
			 out.close();	  
			 System.out.println("\n\nDURA:"+(System.currentTimeMillis()-mseg));	        	        			
		} catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");		
	}
	
	public static void simpleParser(String filter) {
		System.out.println("---------------------");
		System.out.println("FILTER INPUT="+filter);
		ANTLRInputStream input = new ANTLRInputStream(filter);
		OdataFilterLexer lexer = new OdataFilterLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		OdataFilterParser parser = new OdataFilterParser(tokens);
		ParseTree tree = parser.prog();
		//OdataFilterSql eval = new OdataFilterSql();
		//System.out.println("QUERY WHERE ="+eval.visit(tree));		
	}

}
