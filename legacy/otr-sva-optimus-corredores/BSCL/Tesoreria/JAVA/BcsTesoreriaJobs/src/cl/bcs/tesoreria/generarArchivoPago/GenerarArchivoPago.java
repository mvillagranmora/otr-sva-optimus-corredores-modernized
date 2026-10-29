package cl.bcs.tesoreria.generarArchivoPago;

/*
 * 
 *  Proposito: Job para generar arhivos físico de pagos a entidades bancarias
 *             - El formato de cada archivo está configurado en "TES.FormatoArchivo" según Banco, Moneda, Medio Pago
 *			   - Cada formato define un SP de consulta que entrega la data a incluir en el archivo	              
 *  Autor: JVILLAGRAN - BCS
 *  Fecha Creacion: 17/10/2017
 *
 */
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.tesoreria.generarArchivoPago.GenerarArchivoPagos.EntradaGenerarArchivoPagos;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarArchivoPago  extends BcsJob implements JavaCall {
	//Para manejo de instancias del jobmanager
	AtomicBoolean running = new AtomicBoolean(false); 

	// Log
	private final Log log = new Log("TES.GENERAR_ARCHIVO_PAGO");
	
	
	// Queries
	private final String SQL_UPDATE_ProgramaJobManager = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida WHERE \"IdPrograma\" = :IdPrograma";
	
	
	
	
	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS DE PROCESAMIENTO
	//-------------------------------------------------------------------------------------------	
	
	public static class ActualizarPrograma{
		public String RutaSalida;
		public Long IdPrograma;
		
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	
	public static class ContenidoArchivo
	{
		public Integer IdInterno;
		public Integer TipoRegistro;
		public String  Linea;
		
		public ContenidoArchivo(Integer idInterno, Integer tipoRegistro, String linea)
		{
			this.IdInterno = idInterno;
			this.TipoRegistro = tipoRegistro;
			this.Linea = linea;
		}
		
	}
	
	//--------------------------------------------
	// METODO PRINCIPAL INVOCADO DESDE JOBMANAGER
	//--------------------------------------------	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		logica(false, conn);
		return true;
	}
	
	
	
	//--------------------------------------------
	// LOGICA DEL JOB
	//--------------------------------------------
	

	
	public void logica(boolean debugMode, Connection conn) throws InterruptedException, Throwable 
	{	
		log.write("== INICIO JOB ==");
		SQL sql = new SQL();
		try 
		{
			running.set(true);
			
	        PreparedStatement pstmt = null;
	        CallableStatement sp = null;
			Long idEmpresa = null;
			Long idNominaTransferencia = null;
			Long idBanco = null;
			String idMoneda = null;
			String idMedioDePago = null;
			
			if (debugMode) 
			{	//Si est en debug, se asignan parámetros de entrada en duro para pruebas
				idEmpresa = 1L;
				idNominaTransferencia =	48L;
				idBanco =  10L;         //166002L;		// 166002: Banco Bcp
				idMoneda = "CLP";        //"PEN"; 		// CLP
				idMedioDePago =  "VVE"; //"TBC"; 	// TRA
			}
			else 
			{	//Si NO esta en debug, recibe parametros de entrada desde JobManager
				idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
				idNominaTransferencia = Long.parseLong(this.getParam("IdNominaTransferencia"));
				idBanco = Long.parseLong(this.getParam("IdBanco"));
				idMoneda = String.valueOf(this.getParam("IdMoneda"));
				idMedioDePago = String.valueOf(this.getParam("IdMedioDePago"));
			}
			
            // Obtiene formato del archivo y ruta del servicio para consultar data
            FormatoArchivo formatoArchivo = new FormatoArchivo(conn,idEmpresa,idNominaTransferencia,idBanco,idMoneda,idMedioDePago);
            formatoArchivo.setIdEmpresa(idEmpresa);
            formatoArchivo.setIdNominaTransferencia(idNominaTransferencia);
            formatoArchivo.setIdBanco(idBanco);
            formatoArchivo.setIdMoneda(idMoneda);
            formatoArchivo.setIdMedioDePago(idMedioDePago);
            log.write("PARAMETROS DE ENTRADA > " + formatoArchivo.toString());
            
            //Ruta para pruebas locales (solo en modo debug)
            if (debugMode)
            {
            	formatoArchivo.setRutaCompleta("C:\\__ARCHIVO_PRUEBA\\");
            }
            
            
        	if (formatoArchivo.getCantidadDeTiposDeRegistro() == 0)
        	{
				log.write("NO EXISTE FORMATO CONFIGURADO PARA GENERAR EL ARCHIVO");
				running.set(false);
				return;	            		
        	}
        	// Configura archivo a crear
            Archivo archivo = new Archivo(formatoArchivo, conn); 
            
            try 
            {
            	  if (formatoArchivo.getCodigoFormatoArchivo().equalsIgnoreCase("BANCO_ESTADO_CLP_TRADVP")) 
           	     {
                 	GenerarArchivoPagos archivoBancoEstado = new GenerarArchivoPagos();
                 	EntradaGenerarArchivoPagos entradaArchivoBancoEstado = new EntradaGenerarArchivoPagos();
                 	entradaArchivoBancoEstado.p_Fecha = Utils.getFechaSistema(conn, "BSCL",  Long.parseLong(this.getParam("IdEmpresa")));
                 	entradaArchivoBancoEstado.p_IdEmpresa = idEmpresa;
                 	entradaArchivoBancoEstado.p_IdModulo = "TES";
                 	entradaArchivoBancoEstado.p_IdNominaTransferencia = idNominaTransferencia;
                 	entradaArchivoBancoEstado.p_Archivo = archivo;
                 	archivoBancoEstado.logica(conn, entradaArchivoBancoEstado);
                 	
           	     }else {
            	
                    // Prepara llamada al servicio de consulta de data del archivo. Los resulset de salida(tipos de registros) varian de acuerdo a la data y formato configurado
	            	String paramSp = "?,?,?,?,?";
	            	for (int i=0; i<formatoArchivo.getCantidadDeTiposDeRegistro(); i++)
	            	{	
	            		paramSp = paramSp + ",?";
	            	}
                    String callSp = String.format("CALL \"%s\"(%s)", formatoArchivo.getServicio(), paramSp);
                    
                    sp = conn.prepareCall(callSp);
                    sp.setLong(1, formatoArchivo.getIdEmpresa());
                    sp.setLong(2, formatoArchivo.getIdNominaTransferencia());
                    sp.setLong(3, formatoArchivo.getIdFormatoArchivo());
                    sp.setString(4, formatoArchivo.getIdMoneda());
                    sp.setString(5, formatoArchivo.getIdMedioDePago());
                    
                    //Ejecuta el sp que entrega data para generar el archivo 
                    boolean results = sp.execute();

                    if (results)
                    {	
                    	 log.write("OBTIENE DATA PARA GENERAR ARCHIVO DESDE > " + formatoArchivo.getServicio());
                    	 archivo.crear(); // Crea archivo

                   	     if (formatoArchivo.getCodigoFormatoArchivo().equalsIgnoreCase("BCP_PEN_TBC_TELECREDITO"))
                    	 {  //Genera archivo telecrédito para banco BCP de PERU
                   	    	generarArchivo_BCP_PEN_TBC_TELECREDITO(sp, formatoArchivo,archivo);
                    	 }
                    	 else
                    	 {   //Genera otros archivos
                    		 generarArchivo_GENERICO(sp, formatoArchivo, archivo);
                    	 }
                        
                         sp.close();
                        
                        archivo.cerrar(); // Cierra archivo creado
                        log.write("ARCHIVO CREADO EXITOSAMENTE EN > " + formatoArchivo.getRutaCompleta() + archivo.getNombreArchivo());
                    }
                    else
                    {
        				log.write("NO EXISTE DATA PARA GENERAR EL ARCHIVO");
        				running.set(false);
        				return;
                    }
                    
           	     }
                    //Actualiza la nomnina relacionada al archivo creado 
                    //-----------------------------------------------------------------------------------                    
                    formatoArchivo.actualizarTransferencia(conn, log, archivo.getNombreArchivo());
                    
                    //Actualiza el programa del jobManager con la ruta donde se creó el arhivo 
                    //-----------------------------------------------------------------------------------                    
                    if (!debugMode)
                    {	
                    	//Solo la ruta tesoreria ya que la ruta completa la pone el jobman al descargar el archivo
                    	String RutaTesoreria  = formatoArchivo.getRutaTesoreria() + archivo.getNombreArchivo(); 
                    	
        				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
        				actualizar.add(new ActualizarPrograma(RutaTesoreria, this.getJobId()));
        				sql.batch(conn, SQL_UPDATE_ProgramaJobManager , actualizar, ActualizarPrograma.class);
        				log.write("ACTUALIZA EL PROGRAMA JOBMAN CON LA RUTA DEL ARCHIVO GENERADO");
                    }
                 
                    conn.commit();
            } 
            catch (Exception ex) 
            {	
               	log.write("ERROR AL GENERAR ARCHIVO > " + ex.getMessage());
                ex.printStackTrace();
                running.set(false);
                throw new Exception(ex);
    			
            }
            finally 
            {
                try 
                {
                    if (pstmt != null)
                        pstmt.close();
                } 
                catch (SQLException e) 
                {
                    e.printStackTrace();
                }
            }            
         
			
			log.write("== FIN JOB ==");
			running.set(false);
		}
		catch(Exception e) 
		{
			log.write("ERROR GENERAL > " + e.getMessage());
			running.set(false);
			throw new Exception(e);
		}
		return;
	}
	
	

	//Generación de archivos
	private void generarArchivo_GENERICO(CallableStatement sp, FormatoArchivo formatoArchivo, Archivo archivo)  throws InterruptedException, Throwable
	{
		Boolean results = true;
		
        //Recorre los resulset disponibles 
        while (results) 
        {
             ResultSet rs = sp.getResultSet();
             ResultSetMetaData rsmeta = rs.getMetaData();
         	
             int numcols = rsmeta.getColumnCount();  // Cantidad de columnas del RecordSet (varía por cada  tipo de registro y formato)
             
             while (rs.next())  //Recorre el resulset  
             {
                 String[] lineaCSV = new String[numcols]; // Para CSV necesita los datos en un arreglo
                 String lineaTXT = ""; 
                 int tipoRegistroOptimus = 0;
                 for (int i = 1; i <= numcols; ++i) 
                 {
                 	String nombreColumna = rsmeta.getColumnName(i);
                 	if (nombreColumna.equalsIgnoreCase("TipoRegistroOptimus"))
                 	{	//obtiene el tipo de registro a formatear
                 		tipoRegistroOptimus = Integer.parseInt(rs.getString(i));
                 	}
                 	else
                 	{
                        if ((formatoArchivo.getFormatoSalida().compareTo("TXT") == 0 || formatoArchivo.getFormatoSalida().compareTo("txt") == 0)|| formatoArchivo.getFormatoSalida().compareTo("DAT") == 0)
                        {	// Para TXT formatea y arma la línea(de acuerdo al tipo registro + nombre campo). Agrega separador de campos si es que se requiere
                        	lineaTXT = lineaTXT + formatoArchivo.formatear(tipoRegistroOptimus, nombreColumna, rs.getString(i)) + formatoArchivo.getSeparadorCampos();
                        }
                        else if (formatoArchivo.getFormatoSalida().compareTo("CSV") == 0)
                        {	// Para CSV mete los datos a un arreglo
                        	lineaCSV[i - 2] = formatoArchivo.formatear(tipoRegistroOptimus, nombreColumna, rs.getString(i));
                        }
                 	}
                 }

                // Inserta los datos formateados al archivo                                  
				if ((formatoArchivo.getFormatoSalida().compareTo("TXT") == 0 || formatoArchivo.getFormatoSalida().compareTo("txt") == 0) || formatoArchivo.getFormatoSalida().compareTo("DAT") == 0)
                     archivo.escribir(lineaTXT,formatoArchivo.getTipoSaltoLinea());
                 else if (formatoArchivo.getFormatoSalida().compareTo("CSV") == 0)
                     archivo.escribir(lineaCSV);                            	 
                 
             }
             
             rs.close();
             results = sp.getMoreResults(); //Avanza al siguiente resultset
             
        }
	}
	
	//Generacion de archivo telecrédito del banco BCP PERU
	private void generarArchivo_BCP_PEN_TBC_TELECREDITO(CallableStatement sp, FormatoArchivo formatoArchivo, Archivo archivo)  throws InterruptedException, Throwable
	{
		Boolean results = true;
		List<ContenidoArchivo> contenidoArchivo = new ArrayList<ContenidoArchivo>();
		
        //Recorre los resulset disponibles 
        while (results) 
        {
             ResultSet rs = sp.getResultSet();
             ResultSetMetaData rsmeta = rs.getMetaData();
         	
             int numcols = rsmeta.getColumnCount();  // Cantidad de columnas del RecordSet (varía por cada  tipo de registro y formato)
             
             while (rs.next())  //Recorre el resulset  
             {
                 String[] lineaCSV = new String[numcols]; // Para CSV necesita los datos en un arreglo
                 String lineaTXT = "";
                 int tipoRegistroOptimus = 0;
                 int idInterno = 0;
                 for (int i = 1; i <= numcols; ++i)
                 {
                 	String nombreColumna = rsmeta.getColumnName(i);
                 	if (nombreColumna.equalsIgnoreCase("TipoRegistroOptimus") || nombreColumna.equalsIgnoreCase("IdInterno"))
                 	{
                 		if (nombreColumna.equalsIgnoreCase("TipoRegistroOptimus")) tipoRegistroOptimus = Integer.parseInt(rs.getString(i)); //Tipo de registro a formatear
                 		if (nombreColumna.equalsIgnoreCase("IdInterno")) idInterno = Integer.parseInt(rs.getString(i)); //IdInterno para relacionar detalle(2) con documento(3)
                 	}
                 	else
                 	{
                 	    // Para TXT formatea y arma la línea(de acuerdo al tipo registro + nombre campo). Agrega separador de campos si es que se requiere
                        lineaTXT = lineaTXT + formatoArchivo.formatear(tipoRegistroOptimus, nombreColumna, rs.getString(i)) + formatoArchivo.getSeparadorCampos();
                 	}
                 }
                 
                 //Inserta la linea del archivo en una lista(para cuando arme el archivo intercalar registros)
                 ContenidoArchivo _contenidoArchivo = new ContenidoArchivo(idInterno, tipoRegistroOptimus, lineaTXT);
                 contenidoArchivo.add(_contenidoArchivo);
                 
             }
             
             rs.close();
             results = sp.getMoreResults(); //Avanza al siguiente resultset             
        }
        
        if (contenidoArchivo.size() > 0)
        {
        	for (ContenidoArchivo contenido : contenidoArchivo)
        	{
        		if (contenido.TipoRegistro.compareTo(1) == 0)   // Si es tipo registro 1(encabezado), lo inserta (solo 1 registro)
        		{	
        			archivo.escribir(contenido.Linea);
        		}
        		
        		if (contenido.TipoRegistro.compareTo(2) == 0) 	// Si es tipo registro 2(detalle), lo inserta
        		{
        			archivo.escribir(contenido.Linea);
        			
        			for (ContenidoArchivo contenidoL3 : contenidoArchivo)
                	{
        				if (contenidoL3.TipoRegistro.compareTo(3) == 0 && contenido.IdInterno.compareTo(contenidoL3.IdInterno) == 0) //Si es tipo registro 3  
        				{
        					archivo.escribir(contenidoL3.Linea);
        				}
                	}
        		}        		
        	}
        }
        
	}

	@Override
    public void onError(Throwable t) {
        log.write(t.getMessage());  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	//--------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//--------------------------------------------
	public static void main(String[] args) throws InterruptedException, Throwable{
	
		Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties");

		//Connection con = PlataformaUtils.getDESAPEConnection();
		//Connection con = PlataformaUtils.getQAPEConnection();		
		GenerarArchivoPago generarArchivoPago = new GenerarArchivoPago();
		generarArchivoPago.logica(true, con);
		//con.commit();
		con.rollback();
	}

}