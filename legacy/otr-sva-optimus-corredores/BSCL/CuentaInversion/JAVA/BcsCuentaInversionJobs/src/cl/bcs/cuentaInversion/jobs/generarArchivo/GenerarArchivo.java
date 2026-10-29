package cl.bcs.cuentaInversion.jobs.generarArchivo;

/* 
 *  Proposito: Job para generar arhivos físicos de Cuenta Inversion
 *             - El formato de cada archivo está configurado en "CTA.FormatoArchivo"
 *			   - Cada formato tiene un SP asociado que proporciona la data para generar el archivo	              
 *  Autor: JVILLAGRAN - BCS
 *  Fecha Creacion: 12/01/2018
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
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarArchivo  extends BcsJob implements JavaCall {
	//Para manejo de instancias del jobmanager
	AtomicBoolean running = new AtomicBoolean(false); 

	// Log
	private final Log log = new Log("CTA.GENERAR_ARCHIVO");
	
	
	// Queries
	private final String SQL_UPDATE_ProgramaJobManager = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida WHERE \"IdPrograma\" = :IdPrograma";
	
	
	
	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS DE PROCESAMIENTO
	//-------------------------------------------------------------------------------------------	
	
	public static class ActualizarPrograma{
		public String RutaSalida;
		public Long   IdPrograma;
		
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
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
			Long idUsuario = null;
			Long idPrograma = null;
			
			String codigoFormatoArchivo = null;
			
			if (debugMode) 
			{	//Si está en debug, se asignan parámetros de entrada en duro para pruebas
				idEmpresa = 1L;
				idUsuario = 69L;
				codigoFormatoArchivo = "ANEXO13_DETALLE_SALDOS_CLIENTES";
				idPrograma = 1064343L;
			}
			else 
			{	//Si NO está en debug, recibe parámetros de entrada desde JobManager
				idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
				idUsuario = Long.parseLong(this.getParam("IdUsuario"));
				codigoFormatoArchivo = this.getParam("CodigoFormatoArchivo");
				idPrograma = this.getJobId(); //Id del job
			}
			
			log.write("PARAMETROS DE ENTRADA > Empresa: " + idEmpresa.toString() +" codigoFormatoArchivo: "+ codigoFormatoArchivo + " idPrograma:" +idPrograma.toString() + " idUsuario:"+idUsuario.toString());
			

            
            // Obtiene formato del archivo y ruta del servicio para consultar data
            FormatoArchivo formatoArchivo = new FormatoArchivo(conn,idEmpresa,codigoFormatoArchivo);
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
        	else
        	{
        		log.write("OBTIENE FORMATO Y RUTA > " + formatoArchivo.toString());
        	}
        	
            // Configura archivo a crear
            Archivo archivo = new Archivo(formatoArchivo, conn); 
            

            try 
            {
                    // Prepara llamada al servicio de consulta des datos para el archivo. Los resulset de salida(tipos de registros) varian de acuerdo a la data y formato configurado
            		
	            	String paramSp = "?,?,?,?";  // Estos 4 parametros son fijos para todos los SP de Consulta de data(p_IdEmpresa, p_IdUsuario, p_CodigoFormatoArchivo, p_IdPrograma)
	            	for (int i=0; i<formatoArchivo.getCantidadDeTiposDeRegistro(); i++)
	            	{	
	            		paramSp = paramSp + ",?";    // parametros de salida del SP (Tablas de variables. Dependen de la cantidad de tipos de ragistros que tenga configurado el archivo)
	            	}	            	
                    String callSp = String.format("CALL \"%s\"(%s)", formatoArchivo.getServicio(), paramSp);
                    
                    log.write("CALL A EJECUTAR > " + callSp);
                    
                    sp = conn.prepareCall(callSp);
                    
                    //Parámetros fijos para todos los SP(estos parámetros deben ir en cada SP)
                    sp.setLong("p_IdEmpresa", idEmpresa);
                    sp.setLong("p_IdUsuario", idUsuario);
                    sp.setString("p_CodigoFormatoArchivo", codigoFormatoArchivo);                    
                    sp.setLong("p_IdPrograma", idPrograma); //Lo usa para obtener el resto de los parámetros del SP
    				                    
                    //Ejecuta el SP que entrega data para generar el archivo 
                    boolean results = sp.execute();

                    if (results)
                    {	
                    	log.write("DATA OBTENIDA OK PARA GENERAR ARCHIVO. DESDE > " + formatoArchivo.getServicio());
                    	archivo.crear(); // Crea archivo
                    	
                        //Recorre los resulset disponibles 
                        while (results) 
                        {
                             ResultSet rs = sp.getResultSet();
                             ResultSetMetaData rsmeta = rs.getMetaData();
                             int numcols = rsmeta.getColumnCount();  // Cantidad de columnas del RecordSet (varía por cada  tipo de registro y formato)
                             int contSet = 0;
                             while (rs.next())  //Recorre el resulset  
                             {
                                 String[] lineaCSV = new String[numcols]; // Para CSV necesita los datos en un arreglo
                                 String lineaTXT = ""; 
                                 int tipoRegistroOptimus = 0;
                                 for (int i = 1; i <= numcols; ++i) 
                                 {
                                 	String nombreColumna = rsmeta.getColumnName(i);
                                 	if(contSet == 0
                                 		&& nombreColumna.compareTo("TipoRegistroOptimus") != 0 
                                 			&& formatoArchivo.getAgregoNombreColumna().compareTo("S") == 0
                                 				&& formatoArchivo.getFormatoSalida().compareTo("CSV") == 0) {
                                 		lineaCSV[i - 2] = nombreColumna;	
                                 	}else{
                                 		if (nombreColumna.equalsIgnoreCase("TipoRegistroOptimus"))
                                     	{	//obtiene el tipo de registro a formatear(Ej.: 1: cabecera  2:detalle)
                                     		tipoRegistroOptimus = Integer.parseInt(rs.getString(i));
                                     	}
                                     	else
                                     	{
    	                                    if (formatoArchivo.getFormatoSalida().compareTo("TXT") == 0)
    	                                    {	// Para TXT formatea y arma la línea(de acuerdo al tipo registro + nombre campo). Agrega separador de campos si es que se requiere
    	                                    	lineaTXT = lineaTXT + formatoArchivo.formatear(tipoRegistroOptimus, nombreColumna, rs.getString(i)) + formatoArchivo.getSeparadorCampos();
    	                                    }
    	                                    else if (formatoArchivo.getFormatoSalida().compareTo("CSV") == 0)
    	                                    {	// Para CSV mete los datos a un arreglo
    	                                    	lineaCSV[i - 2] = formatoArchivo.formatear(tipoRegistroOptimus, nombreColumna, rs.getString(i));
    	                                    }
                                     	}	
                                 	}
                                 }

                                // Inserta los datos formateados al archivo
								if (formatoArchivo.getFormatoSalida().compareTo("TXT") == 0)
                                     archivo.escribir(lineaTXT);
                                 else if (formatoArchivo.getFormatoSalida().compareTo("CSV") == 0)
                                     archivo.escribir(lineaCSV);                            	 
								
								contSet++;
                             }
                             rs.close();
                          results = sp.getMoreResults(); //Avanza al siguiente result set 
                        } 
                        sp.close();
                        
                        archivo.cerrar(); // Cierra archivo creado
                        log.write("ARCHIVO CREADO EXITOSAMENTE EN > " + formatoArchivo.getRutaCompleta() + archivo.getNombreArchivo());                        
                        
                    } // if (results)
                    else
                    {
        				log.write("NO EXISTE DATA PARA GENERAR EL ARCHIVO");
        				running.set(false);
        				return;
                    }
        
                    
                    //Actualiza el programa del jobManager con la ruta donde se creó el arhivo 
                    //-----------------------------------------------------------------------------------                    
                    if (!debugMode)
                    {	
                    	//Solo la ruta del Modulo(cta) ya que la ruta completa la pone el jobman al descargar el archivo
                    	String RutaModulo  = formatoArchivo.getRutaModulo() + archivo.getNombreArchivo();
                    	
        				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
        				actualizar.add(new ActualizarPrograma(RutaModulo, this.getJobId()));
        				sql.batch(conn, SQL_UPDATE_ProgramaJobManager , actualizar, ActualizarPrograma.class);
        				log.write("ACTUALIZA EL PROGRAMA JOBMAN CON LA RUTA DEL ARCHIVO GENERADO");
                    }
                    
                    conn.commit();
            } 
            catch (Exception ex) 
            {	
               	log.write("ERROR AL GENERAR ARCHIVO > " + ex.getStackTrace());
                ex.printStackTrace();
    			running.set(false);
    			return;
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
			log.write("ERROR GENERAL > " + e.getStackTrace());
			running.set(false);
			throw e;
		}
		return;
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
	
		//Connection con = PlataformaUtils.getDESAConnection(); // DESA CHILE
		Connection con = PlataformaUtils.getDESAPEConnection(); // DESA PERU
		//Connection con = PlataformaUtils.getQAPEConnection(); // QA PERU
		GenerarArchivo GenerarArchivo = new GenerarArchivo();
		GenerarArchivo.logica(true, con);
		//con.commit();
		con.rollback();
	}

}