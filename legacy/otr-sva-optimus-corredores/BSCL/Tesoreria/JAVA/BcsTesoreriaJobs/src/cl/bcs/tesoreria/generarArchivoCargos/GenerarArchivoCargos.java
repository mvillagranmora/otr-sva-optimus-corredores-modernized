package cl.bcs.tesoreria.generarArchivoCargos;

/*
 * 
 *  Proposito: Job para generar arhivos fisico de pagos a entidades bancarias
 *             - El formato de cada archivo esta configurado en "TES.FormatoArchivo" segun Banco, Moneda, Medio Pago
 *			   - Cada formato define un SP de consulta que entrega la data a incluir en el archivo	              
 *  Autor: AES - BCS
 *  Fecha Creacion: 08/01/2019
 *
 */
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


import com.google.gson.JsonObject;

import cl.bcs.plataforma.ftp.FileManager.Cabecera;
import cl.bcs.plataforma.ftp.FileManager.Detalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.tesoreria.ActivarMandatosArchivoCargo;
import cl.bcs.tesoreria.ConsultarFormatoArchivo;
import cl.bcs.tesoreria.ConsultarFormatoArchivo.EntradaFormatoArchivo;
import cl.bcs.tesoreria.ConsultarFormatoArchivo.SalidaFormatoArchivo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class GenerarArchivoCargos  extends BcsJob implements JavaCall {
	
	AtomicBoolean running = new AtomicBoolean(false); 
	
	private final Log log = new Log("TES.GENERAR_ARCHIVO_PAGO");	
	
	private final String SQL_UPDATE_ProgramaJobManager = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida WHERE \"IdPrograma\" = :IdPrograma";
	private final String SQL_INSERT_CABECERA = loadResourceToString("cl/bcs/plataforma/ftp/sql/FTP_InsertTableCabecera.sql");
	private final String SQL_INSERT_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/FTP_InsertTableDetalle.sql");
		
	public static class ActualizarPrograma{
		public String RutaSalida;
		public Long IdPrograma;
		
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}	

	private CallableStatement getService(SalidaFormatoArchivo formatoArchivo, Long discountDay, String fechaCargo, Connection conn) throws Exception {
		String params = "?,?,?,?";
		Boolean results = false;
    	for (int i=0; i<formatoArchivo.CantidadDeTiposDeRegistro; i++)   	    		
    		params = params + ",?";    	
        String query = String.format("CALL BSCL.\"%s\"(%s)", formatoArchivo.Servicio, params);
        CallableStatement storedProcedure = conn.prepareCall(query);       
        storedProcedure.setLong(1, formatoArchivo.IdEmpresa);        
        storedProcedure.setLong(2, formatoArchivo.IdFormatoArchivo);
        storedProcedure.setLong(3, discountDay);
        storedProcedure.setString(4, fechaCargo);
        results = storedProcedure.execute();
        if(results) {        	
        	return storedProcedure;
        }
        return null;
	}
	
	private void upgradeJobManager(Archivo file, SalidaFormatoArchivo formatoArchivo, Connection conn) throws SQLException {
		SQL sql = new SQL();
		String RutaTesoreria  = formatoArchivo.RutaTesoreria + file.getFilename();    	
		List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
		actualizar.add(new ActualizarPrograma(RutaTesoreria, this.getJobId()));		
		log.write(SQL_UPDATE_ProgramaJobManager+" - "+ RutaTesoreria+" - "+this.getJobId());
		sql.batch(conn, SQL_UPDATE_ProgramaJobManager , actualizar, ActualizarPrograma.class);		
		log.write("ACTUALIZA EL PROGRAMA JOBMAN CON LA RUTA DEL ARCHIVO GENERADO");		
	}	
	
	private void uploadToFTP(Archivo file, SalidaFormatoArchivo formatoArchivo, Connection conn) throws Exception {
		SQL sql = new SQL();
		
		List<Long> secuenciasDetalle = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Detalle", file.getDetails()); 
		List<Long> secuenciasCabecera = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Cabecera", 1L);  				
  		List<Cabecera> cabeceras = new ArrayList<Cabecera>();
  		
  		Cabecera cabecera = new Cabecera();  		
  		cabecera.NombreArchivo = file.getFilename();
  		cabecera.IdCabecera = secuenciasCabecera.get(0);
  		cabecera.IdCodigoTipo = formatoArchivo.FormatoSalida.equals("CSV") ? "CARCSV": "CARTXT";  		
  		cabecera.IdEstado = "PEN";
  		cabecera.FechaCreacion = new Timestamp(System.currentTimeMillis());
  		cabecera.IdUsuario = formatoArchivo.IdUsuario;
  		cabeceras.add(cabecera);  		
  		//Detalle
		List<Detalle> detalles = new ArrayList<Detalle>();
		for(int i=0; i<file.getLines().size(); i++) {
			Detalle detalle = new Detalle();
			detalle.IdDetalle = secuenciasDetalle.get(i);
			detalle.IdCabecera = cabecera.IdCabecera;
			detalle.Linea = file.getLines().get(i);
			detalles.add(detalle);
		}		
  		sql.batch(conn, SQL_INSERT_CABECERA, cabeceras, Cabecera.class);
		sql.batch(conn, SQL_INSERT_DETALLE, detalles, Detalle.class);
	}
	
	private Archivo generateFile(CallableStatement storedProcedure, SalidaFormatoArchivo formatoArchivo, FormatoArchivo formato,	Boolean results) throws Exception {
		
		Archivo file = new Archivo(formatoArchivo);		
		Integer typeRecordOptimus = 0;		
		file.open();
		while(results) {
			ResultSet resultSet = storedProcedure.getResultSet();
			ResultSetMetaData metaData = resultSet.getMetaData();
			while(resultSet.next()) {	
				List<String> valuesFormatted = new ArrayList<String>();
				for (int i=1; i<=metaData.getColumnCount(); ++i) {
					if (metaData.getColumnName(i).equals("TipoRegistroOptimus"))
						typeRecordOptimus = Integer.parseInt(resultSet.getString(i));             	
					else 										
						valuesFormatted.add(formato.formatear(typeRecordOptimus, metaData.getColumnName(i), resultSet.getString(i), formatoArchivo.RellenarCampo.equals("S")));
				}			
				file.addLine(valuesFormatted);				
			}
			resultSet.close();
            results = storedProcedure.getMoreResults();
		}		
		file.close();
		return file;
		
	}
	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		logica(false, conn);
		return true;
	}
	
	public void logica(boolean debugMode, Connection conn) throws Exception {		
		log.write("== BEGIN JOB ==");
		running.set(true);
		ConsultarFormatoArchivo consultarFormatoArchivo = new ConsultarFormatoArchivo();
		EntradaFormatoArchivo entradaFormatoArchivo = new EntradaFormatoArchivo();
		SalidaFormatoArchivo formatoArchivo = new SalidaFormatoArchivo();	
		ActivarMandatosArchivoCargo activarMandatos = new ActivarMandatosArchivoCargo();
				
		Long discountDay;
		String fechaCargo;
		
		if(debugMode) {
			entradaFormatoArchivo.IdFormatoArchivo = 8L;
			entradaFormatoArchivo.IdEmpresa = 1L;
			entradaFormatoArchivo.IdUsuario = 1L;
			discountDay = 0L;
			fechaCargo = "2020-01-01";
		}		
		else {
			entradaFormatoArchivo.IdFormatoArchivo = 8L;
			entradaFormatoArchivo.IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			entradaFormatoArchivo.IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
			discountDay = Long.parseLong(this.getParam("DiaDescuento"));
			fechaCargo = this.getParam("FechaCargo");
		}
		
		
		formatoArchivo = consultarFormatoArchivo.logica(conn, entradaFormatoArchivo).get(0);
		FormatoArchivo formato = new FormatoArchivo(formatoArchivo);	
		if(debugMode)
			formatoArchivo.RutaCompleta = "C:\\__ARCHIVO_PRUEBA\\";
		if(formatoArchivo.CantidadDeTiposDeRegistro == 0) {
			log.write("NO EXISTE FORMATO CONFIGURADO PARA GENERAR EL ARCHIVO");
			running.set(false);
			return;
		}
		CallableStatement storedProcedure = getService(formatoArchivo, discountDay, fechaCargo, conn);
		Boolean results = storedProcedure.execute();		
		if(!results){
			log.write("NO EXISTE DATA PARA GENERAR EL ARCHIVO");
    		running.set(false);
    		return;
    	}
    	log.write("OBTIENE DATA PARA GENERAR ARCHIVO DESDE > " + formatoArchivo.Servicio);
    	Archivo file = generateFile(storedProcedure, formatoArchivo, formato, results);
    	uploadToFTP(file, formatoArchivo, conn);
		log.write("ARCHIVO CREADO EXITOSAMENTE EN > " + file.getFullpath());
		
		/**********************************************
		 * Anular Mandatos con Dia Descuento distinto *
		 **********************************************/
		if (discountDay > 0) {
			activarMandatos.logica(conn, discountDay);
		}
		/*********************************************/
		
		if(!debugMode)
			upgradeJobManager(file, formatoArchivo, conn);
		conn.commit();
		storedProcedure.close();
		log.write("== END JOB ==");
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
	
		//Connection con = PlataformaUtils.getDESAConnection();
		
		Connection con = PlataformaUtils.getDESAConnection();
		//Connection con = PlataformaUtils.getQAPEConnection();		
		GenerarArchivoCargos generarArchivoCargo = new GenerarArchivoCargos();
		generarArchivoCargo.logica(true, con);
		//con.commit();
		con.rollback();
	}

}
