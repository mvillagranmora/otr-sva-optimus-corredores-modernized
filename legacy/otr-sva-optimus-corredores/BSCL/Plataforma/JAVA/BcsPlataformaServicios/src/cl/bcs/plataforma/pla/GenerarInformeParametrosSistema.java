package cl.bcs.plataforma.pla;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.NamedParameterStatement;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import com.opencsv.CSVWriter;


/***************************************
 * @version 	1.0
 * @author 		Sebastian Hechenleitner
 * 06-06-2017	SAP-6614 Mantenedor Consulta de Parametros
**************************************/

public class GenerarInformeParametrosSistema implements JavaCall {
	private final Log log = new Log("PLA.GenerarInformeParametrosSistema");
 	private FileOutputStream os;
    private CSVWriter cw;   //para crear archivo CSV
	
	private final String SQL_CONSULTA_TABLAS_USO_SISTEMA = 	"SELECT Columns.\"TABLE_NAME\", Columns.\"COLUMN_NAME\", UsoSistema.\"DescripcionUso\" FROM sys.table_columns AS Columns , \"BSCL.Plataforma.BD::PLA.UsoTablaSistema\" AS UsoSistema "+
															" WHERE schema_name ='BSCL' "+
															" AND Columns.\"TABLE_NAME\" = UsoSistema.\"NombreTabla\" "+
															" AND table_name IN (select \"NombreTabla\" FROM \"BSCL.Plataforma.BD::PLA.UsoTablaSistema\" WHERE \"EsConfiguracion\" = 'S' AND \"EsMantenedorParametroModulo\" = 'N'  ) "+															
															" ORDER BY \"TABLE_NAME\", \"POSITION\" ";

	private final String SQL_CONSULTA_TABLAS_PARAMETROS = loadResourceToString("cl/bcs/plataforma/pla/sql/GenerarInformeParametrosSistema_SelectParametros.sql");
		
	public static class EstructuraTablas {
		public String TABLE_NAME;
		public String COLUMN_NAME;
		public String DescripcionUso;
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}		
	
	public static class DatosTabla {
		public Long			p_IdEmpresa;
		public String		p_IdModulo;
		public String		p_Nombretabla;
		public String		DescripcionUso;

		public List<String>	ColumnasTabla;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}	
	
	public static class Parametros {
		public String Modulo;
		public Long   IdEmpresa;
		public String CodigoParametro;
		public String Valor;
		public String Descripcion;
		public String Uso;
		public String ValorDefecto;
		public String ValoresPosibles;
		public String ModificablePorUsuario;
		public String ModificablePostProduccion;
		public String CreadorParametro;
		public java.sql.Timestamp FechaCreacion;
		public String ModificadorParametro;
		public java.sql.Timestamp FechaModificacion;
		public String Observaciones;
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGeneraInformeParametros{
		
		public Long    p_IdEmpresa;
		public Long    p_IdUsuario;
		public String  p_IdModulo;
		public String  p_Ruta;
		
	}
	
	public static class OutGeneraInformeParametros{
		
		public Long 	  P_OUT_ERRORESCONTADOR;
		public String 	  P_OUT_NOMBRE_ARCHIVO;
		public String	  P_OUT_RUTA_NOMBRE_ARCHIVO;
		
	}
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InGeneraInformeParametros in=gson.fromJson(jin.toString() ,InGeneraInformeParametros.class);
		OutGeneraInformeParametros out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
    public OutGeneraInformeParametros logica(Connection con, InGeneraInformeParametros entrada) throws Exception {
    	OutGeneraInformeParametros salida = new OutGeneraInformeParametros();
    	salida.P_OUT_ERRORESCONTADOR = new Long(1);
    	
		SQL sql = new SQL();
		int  SegundosZonaHoraria=FechaDB.ObtenerSegundosZonaHoraria(con,entrada.p_IdEmpresa);
		Fecha.setLocalOffSet(SegundosZonaHoraria);		

        //Obtiene las tablas maestras del sistema
    	log.write("Obteniendo Tablas Maestras Optimus");
    	HashMap<String,Object> param =new HashMap<String,Object>();
		List<EstructuraTablas> tablasMaestras = sql.query(con, SQL_CONSULTA_TABLAS_USO_SISTEMA, param, EstructuraTablas.class);
		DatosTabla datos = new DatosTabla();
		List<String> columnas = new ArrayList<>();

		//Ruta y archivos de trabajo
		if (!new File(entrada.p_Ruta).exists())
    	{
    		File theDir = new File(entrada.p_Ruta);
    		theDir.mkdir();
    	}				
		String fileZip=entrada.p_Ruta+"/"+generarNombreArchivo("ARCHIVO_PARAMETROS_MAESTROS","ZIP");
		String subfileParmametros = entrada.p_Ruta+"/"+generarNombreArchivo("PARAMETROS","CSV");
		String subfileMaestros = entrada.p_Ruta+"/"+generarNombreArchivo("MAESTROS","CSV");
				
		//Genera el archivo maestros dinamicamente
		log.write("Generando archivo paso de maestros...");
		crearArchivoMaestros(subfileMaestros);		
		String T_NAME_NEXT = "";
		for (int i = 0; i< tablasMaestras.size(); i++){
			String T_NAME = tablasMaestras.get(i).TABLE_NAME;
			String T_DESCRIPCION = tablasMaestras.get(i).DescripcionUso;
			try{
				T_NAME_NEXT = tablasMaestras.get(i+1).TABLE_NAME;
			}catch(Exception e){T_NAME_NEXT = "";}
			
			if(datos.p_IdEmpresa == null){
				datos = AgregaDatosTabla (datos, T_NAME, T_DESCRIPCION, entrada);
				columnas = addColumn(columnas, tablasMaestras.get(i).COLUMN_NAME);
				if (!T_NAME.equals(T_NAME_NEXT)){
					datos.ColumnasTabla = columnas;
					GeneraDatoParametroDinamicamente(datos, T_NAME, con);		
					
					//REINICIO DE LOS OBJETOS
					datos = new DatosTabla();
					columnas = new ArrayList<>();
				}
				continue;
			}
			
			if (T_NAME.equals(T_NAME_NEXT)){
				columnas = addColumn(columnas, tablasMaestras.get(i).COLUMN_NAME);
			}
			else{
				datos = AgregaDatosTabla (datos, T_NAME, T_DESCRIPCION, entrada);
				columnas = addColumn(columnas, tablasMaestras.get(i).COLUMN_NAME);
				datos.ColumnasTabla = columnas;
				GeneraDatoParametroDinamicamente(datos, T_NAME, con);		
				
				//REINICIO DE LOS OBJETOS
				datos = new DatosTabla();
				columnas = new ArrayList<>();
			}
		}
		cerrarArchivoMaestro();
		
		//Genera el archivo de parametros de los modulos
		log.write("Generando archivo paso de parametros...");
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<Parametros> lParamtros = sql.query(con, SQL_CONSULTA_TABLAS_PARAMETROS, params, Parametros.class);
		try (FileOutputStream mos=new FileOutputStream(subfileParmametros);
				 CSVWriter mcw=new CSVWriter(new OutputStreamWriter(mos, StandardCharsets.ISO_8859_1),';')) 
		{
			//cabecera
			String[] lineaC=new String[13];
			lineaC[0]="Modulo";
			lineaC[1]="Codigo";
			lineaC[2]="Descripcion";
			lineaC[3]="Valor";
			lineaC[4]="Uso";
			lineaC[5]="Valor Defecto";
			lineaC[6]="Valores Posibles";
			lineaC[7]="Ambito";
			lineaC[8]="Creador";
			lineaC[9]="F.Creacion";
			lineaC[10]="Modificador";
			lineaC[11]="F.Modificacion";
			lineaC[12]="Observaciones";
			mcw.writeNext(lineaC,false); 
			//Valores
			for (Parametros p:lParamtros) {
				String[] linea=new String[13];
				linea[0]=p.Modulo;
				linea[1]=p.CodigoParametro;
				linea[2]=p.Descripcion;
				linea[3]=p.Valor;
				linea[4]=p.Uso;
				linea[5]=p.ValorDefecto;
				linea[6]=p.ValoresPosibles;
				if (p.ModificablePorUsuario!=null && p.ModificablePorUsuario.equals("N")) {
					linea[7]="CORREDOR";
				}
				else {
					linea[7]="BCS";
					linea[2]="******";
					
				}
				linea[8]=p.CreadorParametro;
				if(p.FechaCreacion != null) {
					linea[9]=Fecha.toLocalFormat(p.FechaCreacion,"yyyy-MM-dd HH:mm:ss"); 
				}else {	
					linea[9]=Fecha.toLocalFormat(transformarFechasNull(),"yyyy-MM-dd HH:mm:ss"); 
				}
				linea[10]=p.ModificadorParametro;
				if(p.FechaModificacion != null) {
					linea[11]=Fecha.toLocalFormat(p.FechaModificacion,"yyyy-MM-dd HH:mm:ss");
				}else {			
					linea[11]=Fecha.toLocalFormat(transformarFechasNull(),"yyyy-MM-dd HH:mm:ss");
				}
				linea[12]=p.Observaciones;				
				mcw.writeNext(linea,false); 				
			}
		}
		
		
		//Genera el zip para enviar	
		log.write("Uniendo archivos en un zip...");
		try (FileOutputStream fos = new FileOutputStream(fileZip);) {				
			try (ZipOutputStream zos = new ZipOutputStream(fos);) {
				//Parametros
				{
		    		File file = new File(subfileParmametros);
		    		try (FileInputStream fis = new FileInputStream(file);) {
			    		ZipEntry zipEntry = new ZipEntry(file.getName());
			    		zos.putNextEntry(zipEntry);
			    		byte[] bytes = new byte[1024];
			    		int length;
			    		while ((length = fis.read(bytes)) >= 0) {
			    			zos.write(bytes, 0, length);
			    		}
		    		}
		    		file.delete();
				}		    		
	    		//Maestros
				{
		    		File file = new File(subfileMaestros);
		    		try (FileInputStream fis = new FileInputStream(file);) {
			    		ZipEntry zipEntry = new ZipEntry(file.getName());
			    		zos.putNextEntry(zipEntry);
			    		byte[] bytes = new byte[1024];
			    		int length;
			    		while ((length = fis.read(bytes)) >= 0) {
			    			zos.write(bytes, 0, length);
			    		}
		    		}
		    		file.delete();
				}	    				
    		}
		}			
		
		//Salida
		File file = new File(fileZip);
		salida.P_OUT_NOMBRE_ARCHIVO = file.getName();
		salida.P_OUT_RUTA_NOMBRE_ARCHIVO = file.getAbsolutePath();		
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		log.writeAsJson("SALIDA:",salida);
        return salida;    	    	
    }
    
    public Timestamp transformarFechasNull() throws ParseException {
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
	    Date parsedDate = df.parse("0001-01-01 00:00:00.0");
	    Timestamp fecha = new java.sql.Timestamp(parsedDate.getTime());
	    
	    return fecha;
    }
    
    public List<String> addColumn(List<String> array, String columnName){
    	switch (columnName){
			case "IdEmpresa":
			case "EsModificableUsuario": 
				break;
			default:
				array.add(columnName);
				break;
		}
    	return array;
    }
	
    public static void main(String[] args) {
        System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getDESAConnection();
            // ---------------------------------
            try {
            	InGeneraInformeParametros entrada = new InGeneraInformeParametros();
				entrada.p_IdEmpresa = new Long(1);
				entrada.p_IdUsuario = new Long(21);
				entrada.p_IdModulo = "PLA";
				entrada.p_Ruta     = "C:/tmp/";
				
            	JsonParser parser = new JsonParser();
            	JsonObject dataJsonIn = parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
            	
            	GenerarInformeParametrosSistema demo = new GenerarInformeParametrosSistema();
            	System.out.println(demo.call(con, dataJsonIn).toString());
                //con.commit();
            	con.rollback();
            } catch (Throwable er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");       
    }
    
	public ArrayList<String[]> GeneraDatoParametroDinamicamente (DatosTabla datos, String T_NAME, Connection con) throws SQLException, IOException
	{
		String sql = GeneraQuery(datos, T_NAME);	
		//System.out.println(sql);
        NamedParameterStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String[]> result = new ArrayList<>();
        try {
        	String sqlWcomments = sql.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)","");
        	String DataType = "";
        	String ResultValue = "";
        	
        	stmt = new NamedParameterStatement(con,sqlWcomments); 	
            rs=stmt.executeQuery();

            if (rs.next()) { 
            	ResultSetMetaData rsmd = rs.getMetaData();
            	int cols=rsmd.getColumnCount();
            	
            	String[] VACIO = {""};
            	String[] MODULO = {"MODULO",datos.p_IdModulo};
                String[] TABLA = {"TABLA",datos.p_Nombretabla};
                String[] USO = {"USO",datos.DescripcionUso};
            	String[] TAGS = datos.ColumnasTabla.toArray(new String[cols]);
            	
            	escribirCSV(VACIO);
            	escribirCSV(MODULO);
            	escribirCSV(TABLA);
            	escribirCSV(USO);
            	escribirCSV(TAGS);
            	
            	String[] StringArrayResult = new String[cols];
                do  {
                	for (int i=1;i<=cols;i++) {
	            		Object val=rs.getObject(i);
	            		if (val != null){
	            			DataType = val.getClass().getSimpleName();
	            			switch(DataType){ //EN CASO DE TENER QUE DAR FORMATO A LA SALIDA
		            			default:
		            				ResultValue = val.toString();
		            				break;
	            			}
							StringArrayResult[i-1] = ResultValue;
	            		}
                	}
                	result.add(StringArrayResult);
                	escribirCSV(StringArrayResult);
                }while (rs.next());            	
            }
            
        } catch (SQLException e) {
            this.rethrow(e, sql);
        } finally {
            closeQuietly(rs);
        }
        return result;
	}
	
	public DatosTabla AgregaDatosTabla (DatosTabla datos, String T_NAME, String T_DESCRIPCION, InGeneraInformeParametros entrada) {
		try{
			//datos.p_IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			datos.p_IdEmpresa = entrada.p_IdEmpresa;
			datos.p_IdModulo = T_NAME.substring(T_NAME.indexOf(".")+1, T_NAME.indexOf(".",T_NAME.indexOf(".")+1));
			datos.p_Nombretabla = T_NAME.substring(T_NAME.lastIndexOf(".")+1, T_NAME.length());
			
			datos.DescripcionUso = T_DESCRIPCION; //FALTA AÑADIR COLUMNA Descripcion A LA TABLA UsoTablaSistema
		}
		catch(Exception e){
			log.write("Tabla " + T_NAME + " no cumple formato de nombre.");
		}
		return datos;
	}
	
	public String GeneraQuery (DatosTabla datos, String T_NAME) {
		ArrayList<String> condicion = new ArrayList<>();
		String QUERY = "SELECT ";
		for (int i=0; i<datos.ColumnasTabla.size(); i++){
			switch(datos.ColumnasTabla.get(i)){
				case "IdEmpresa":
				case "EsModificableUsuario":
					condicion.add(datos.ColumnasTabla.get(i));
					continue;
			}
			QUERY = QUERY.concat("\"" + datos.ColumnasTabla.get(i) + "\", ");
		}
		QUERY = QUERY.substring(0, QUERY.lastIndexOf(','));
		QUERY = QUERY.concat(" FROM \"" + T_NAME+ "\"");
		for (int i=0; i<condicion.size(); i++){
			if(i==0) QUERY = QUERY.concat(" WHERE ");
			else QUERY = QUERY.concat(" AND ");
			QUERY = QUERY.concat("\"" + condicion.get(i) + "\" = ");
			switch(condicion.get(i)){
				case "IdEmpresa":
					QUERY = QUERY.concat(datos.p_IdEmpresa.toString());
					break;
				case "EsModificableUsuario":
					QUERY = QUERY.concat("'S'");
					break;
			}
		}
		return QUERY;
	}
	
	private String generarNombreArchivo(String prefix,String ext)
	{	
		String nombreArchivo = "";
		
		try 
		{	
			Date fechaActual = new Date();		
			
			DateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String fechaHora = formatoFecha.format(fechaActual);
			
			nombreArchivo = prefix+"_"+ fechaHora +"."+ext;			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return nombreArchivo;
	}
	
	
	
	public void crearArchivoMaestros(String nombreArchivo)
	{	
        try
        {	
        	this.os = new FileOutputStream(nombreArchivo);
    		this.cw = new CSVWriter(new OutputStreamWriter(this.os, StandardCharsets.ISO_8859_1),';');
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }
	}
	
	public void escribirCSV(String[] linea)
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
	
	public void cerrarArchivoMaestro()
	{	
        try
        {
        	this.cw.close();
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
	}
        
    protected void rethrow(SQLException cause, String sql)throws SQLException {
        String causeMessage = cause.getMessage();
        if (cause.getCause() != null)
        	causeMessage = causeMessage  + " " + cause.getCause().getMessage();   
        StringBuffer msg = new StringBuffer(causeMessage);

        msg.append(" Query: ");
        msg.append(sql);
        msg.append(" Parameters: ");

        SQLException e = new SQLException(msg.toString(), cause.getSQLState(),cause.getErrorCode());
        e.setNextException(cause);
		
        throw e;
    }	
	
    public static void closeQuietly(ResultSet rs) {
        try {
			if (rs!=null) {
				rs.close();
			}
        } catch (SQLException e) { 
            // quiet
        }
    }
}