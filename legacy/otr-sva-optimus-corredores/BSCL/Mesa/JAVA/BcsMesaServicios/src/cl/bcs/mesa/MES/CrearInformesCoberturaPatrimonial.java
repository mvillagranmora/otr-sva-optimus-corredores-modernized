package cl.bcs.mesa.MES;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;

import cl.bcs.mesa.MRF.ConsultarValorizacionPatrimonioMRF;
import cl.bcs.mesa.MRF.ConsultarValorizacionPatrimonioMRF.ValorizarPatrimonio_IN;
import cl.bcs.mesa.MRF.ConsultarValorizacionPatrimonioMRF.ValorizarPatrimonio_OUT;
import cl.bcs.nuam.StoredProcedure;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrearInformesCoberturaPatrimonial implements JavaCall {

	private final Log log = new Log("CrearInformesCoberturaPatrimonial");
	static final Logger logger = LogManager.getLogger(CrearInformesCoberturaPatrimonial.class);
	private final Long plazoDias = 3L;
	private final String SQL_SELECT_REPORTE = loadResourceToString("cl/bcs/mesa/sql/CrearInformesCoberturaPatrimonial_Select_TemplateCoberturaPatrimonial.sql");
	private final String SQL_CALL_COBERTURA_PATRIMONIAL = loadResourceToString("cl/bcs/mesa/sql/CrearInformesCoberturaPatrimonial_Call_SPCoberturaPatrimonial.sql");
	private final String SP_ARCHIVO_PA = "BSCL.Mesa.BD.Servicios::MRF.GenerarArchivoPAccmmdd";
	private final String SP_ARCHIVO_CC = "BSCL.Mesa.BD.Servicios::MRF.GenerarArchivoCCccmmdd";
	
	/********************************************************************
	 * 								CLASES								*
	 ********************************************************************/
	
	public static class Entrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_FechaOperacion;
		public String p_ValorizaAl;
		public String p_NombreMesa;
		public String p_FechaHoraSistema;
		public String p_IdModulo;
		public Long p_IdPortafolio;
		public Long p_IdCliente;
		public DataSource p_Datasource;
		public Long p_IdPais;
		public String p_Ruta;
		
		public Entrada () {
			this.p_IdPortafolio = 0L;
			this.p_IdCliente = 0L;
			this.p_IdModulo = "MES";
			this.p_NombreMesa = "TODOS";
		}
	}
	
	public static class Reporte{
		public Long CompanyId;
		public String Id;
		public String ReportName;
	}
	
	public static class Salida{
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}
	
	public static class FormatoArchivoDetalle{
		public Long 	IdFormatoArchivoDetalle;
		public Integer TipoRegistro;	
		public String 	Campo;	
		public String 	TipoDato;	
		public Integer Largo;	
		public Integer Decimales;	
		public String 	ValorDefecto;	
		public Integer Orden;	
		public String 	Descripcion;
	}
	
	
	/********************************************************************
	 * 								LOGICA								*
	 ********************************************************************/
	public Salida logica(Connection con, Entrada entrada) throws Exception {
		
		SQL sql=new SQL();	
		List<String> archivos = new ArrayList<String>();
		Salida salida= new Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		salida.P_OUT_MENSAJE = "";
		CallableStatement spexe = null;
		String fechaSistema = "";
		//INFORME DE PATRIMONIO
		try{
			
			if (!new File(entrada.p_Ruta).exists()){
        		File theDir = new File(entrada.p_Ruta);
        		theDir.mkdir();
        	}
			
			fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			List<Reporte> infoReporte = sql.query(con, SQL_SELECT_REPORTE, new HashMap<String,Object>(), Reporte.class);
			Reporte datosReporte = infoReporte.get(0);
			
			ReportDocument report = new ReportDocument(Utils.getSchema(),entrada.p_Datasource);			
			
			if(!report.readDocument(datosReporte.Id)){
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_MENSAJE = "Reporte no existe";
				log.write(salida);
				return salida;
			}
			
			ValorizarPatrimonio_IN valorizarPatrimonioEntrada = new ValorizarPatrimonio_IN();
			valorizarPatrimonioEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
    		valorizarPatrimonioEntrada.p_FechaOperacion = entrada.p_FechaOperacion;
    		valorizarPatrimonioEntrada.p_plazoDias = plazoDias.intValue();
    		valorizarPatrimonioEntrada.p_IdPortafolio = entrada.p_IdPortafolio;
    		valorizarPatrimonioEntrada.p_ValorizaAl = entrada.p_FechaHoraSistema;
    		valorizarPatrimonioEntrada.p_NombreMesa = entrada.p_NombreMesa;
    		valorizarPatrimonioEntrada.p_FechaHoraSistema = entrada.p_FechaHoraSistema;
    		valorizarPatrimonioEntrada.p_IdUsuario = entrada.p_IdUsuario;
    		valorizarPatrimonioEntrada.p_IdModulo = entrada.p_IdModulo;
    		valorizarPatrimonioEntrada.p_IdCliente = entrada.p_IdCliente;
    		valorizarPatrimonioEntrada.p_IdPais = entrada.p_IdPais;
    		
    		ConsultarValorizacionPatrimonioMRF valPatrimonio = new ConsultarValorizacionPatrimonioMRF();
			
			for(int i=1;i<plazoDias;i++ ){
				for(int j=1;j<plazoDias;j++){
					valorizarPatrimonioEntrada.p_opcion = i;
					valorizarPatrimonioEntrada.p_TipoValor = j;
					
					ValorizarPatrimonio_OUT salidaValPatrimonio = valPatrimonio.logica(con, valorizarPatrimonioEntrada);
					
					try{
						spexe = con.prepareCall(SQL_CALL_COBERTURA_PATRIMONIAL);
						spexe.setString(1, salidaValPatrimonio.LlaveReporte);
						spexe.setLong(2, entrada.p_IdCliente);
						spexe.setLong(3, i);
						spexe.setLong(4, j);
						spexe.setString(5, entrada.p_NombreMesa);	
						spexe.setLong(6, plazoDias);
						spexe.setLong(7, entrada.p_IdEmpresa);
						spexe.setDate(8, Fecha.toDate(entrada.p_FechaHoraSistema));
						spexe.registerOutParameter(9, java.sql.Types.REF_CURSOR); 
						spexe.registerOutParameter(10, java.sql.Types.INTEGER);
						boolean isResultSet =spexe.execute();
						if(isResultSet){
							ResultSet rsSP = spexe.getResultSet();
							log.write("INFORME EJECUCION: Servicio Crystal Resulset  ->  " + isResultSet);
							
							report.setDocumentResultSet(rsSP);
							
							log.write("INFORME EJECUCION: modifica Resulset en crystal");
							log.write("**  MODIFICACION RESULSET PARA CRYSTAL REPORT / OK");
							
							//exportar el reporte en formato pdf, se guarda fisicamente en <<ruta>>.
							try(InputStream inStreamRpt = report.exportDocument("pdf")){
								
								String NombreArchivo = infoReporte.get(0).ReportName+"_"+fechaSistema+"_"+i+j+".pdf";
								
								FileUtils.copyInputStreamToFile(inStreamRpt, new File(entrada.p_Ruta, NombreArchivo));
								IOUtils.closeQuietly(inStreamRpt);
								
								//se guardar las rutas de cada archivo para luego zipearlos
								archivos.add(entrada.p_Ruta+NombreArchivo);
								
								log.write("INFORME EJECUCION: ARCHIVO CREADO ->  " + entrada.p_Ruta+NombreArchivo);
								log.write("**  ARCHIVO CREADO :  " + entrada.p_Ruta+NombreArchivo);
							}
							
						}
					}finally{
						if(spexe != null){
							spexe.close();
						}
					}
				}
				
			}
			
			
			
			//Zip
			//Nombre y ruta ZIP
			String zipName = "CoberturaPatrimonial_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+ ".zip";
			String zip = entrada.p_Ruta  + zipName;
			log.write("INFORME EJECUCION: ZIPFILE -> " + zip);
			log.write("ARCHIVO ZIP : " + zip);
			//Crear ZIP
			
			try (OutputStream os = new FileOutputStream(zip)) {
				try (ZipOutputStream zos = new ZipOutputStream(os)) {
					for (String archivo : archivos) {
						//obtener cada archivo a zipear
						String fileName = FilenameUtils.getName(archivo);
						log.write("COMPRESSFILE: " + fileName);
						
						try (InputStream fis = new FileInputStream(archivo)) {
							zos.putNextEntry(new ZipEntry(fileName));
							//escribie un archivo en el zip
							int length;
							byte[] buffer = new byte[1024];
							while ((length = fis.read(buffer)) > 0) {
								zos.write(buffer, 0, length);
							}
							zos.closeEntry();
							log.write("COMPRESSFILE: " + archivo+ " DONE");
							log.write("COMPRIMIDO ZIP : " + archivo+ " AGREGADO");
						}
						FileUtils.forceDelete(new File(archivo));//Borrar cada arhivo incluido en el zip al terminar.
					}
				}
			}	
		}
		catch (Exception e){
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJE = "Error al procesar archivo Cobertura patrimonial";
			log.write("Error",e);			
		}
		
		log.write( ":GENERAR INFORME EJECUCION DE COBERTURA PATRIMONIAL MASIVO:  ****** FIN  *******");
		
		JsonObject callArchivo =new JsonObject();
		callArchivo.addProperty("p_IdEmpresa", entrada.p_IdEmpresa);
		callArchivo.addProperty("p_IdUsuario", entrada.p_IdUsuario);
		callArchivo.addProperty("p_IdModulo", "MRF");
		callArchivo.addProperty("p_UsaReglasNegocio", "S");
		callArchivo.addProperty("p_Fecha", fechaSistema);
		callArchivo.addProperty("p_Formato", "TXT");
		
		log.write( ":INICIO INSERTAR TAREA ARCHIVO PA:  ****** INICIO  *******");
		
		try{
			StoredProcedure plog=new StoredProcedure(SP_ARCHIVO_PA);
			log.write("Se envia a generar archivo pa:",callArchivo);
			//Salida
			JsonObject callArchivoSalida=plog.call(con,callArchivo);
			salida.P_OUT_ERRORESCONTADOR =  callArchivoSalida.get("P_OUT_ERRORESCONTADOR").getAsLong();
			log.write("Resultado de pa:",salida.P_OUT_ERRORESCONTADOR);
		}catch(Exception e){
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJE = "Error al procesar archivo PA";
			log.write("Error",e);
		}
		
		log.write( ":TERMINA INSERTAR TAREA ARCHIVO PA:  ****** FIN  *******");
		
		log.write( ":INICIO INSERTAR TAREA ARCHIVO CC:  ****** INICIO  *******");
		
		try{
			StoredProcedure plog=new StoredProcedure(SP_ARCHIVO_CC);
			log.write("Se envia a generar archivo cc:",callArchivo);
			//Salida
			JsonObject callArchivoSalida=plog.call(con,callArchivo);
			salida.P_OUT_ERRORESCONTADOR =  callArchivoSalida.get("P_OUT_ERRORESCONTADOR").getAsLong();
			log.write("Resultado de cc:",salida.P_OUT_ERRORESCONTADOR);
		}catch(Exception e){
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJE = "Error al procesar archivo PA";
			log.write("Error",e);
		}
		
		log.write( ":TERMINA INSERTAR TAREA ARCHIVO CC:  ****** FIN  *******");
		
		log.write("****** FIN TAREA *******");
				
		return salida;
		
	}
	
	/********************************************************************
	 * 								CALL								*
	 ********************************************************************/
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
		
	public static void main(String[] args) throws Exception,SQLException {
		
		Connection conn = null;
		String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "ABERROCAL";
        String pass   = "Diciembre2017";	
		BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
		
		try {
			System.out.println("== BEGIN ==");
			conn = ds.getConnection();
			CrearInformesCoberturaPatrimonial test = new CrearInformesCoberturaPatrimonial();	
			
			//datos entrada
			Entrada entrada = new Entrada();
			entrada.p_IdEmpresa=new Long(1);
			entrada.p_Datasource = ds;
			entrada.p_Ruta = "C:\\BCS\\logs\\nor\\";
			test.logica(conn, entrada);
			
			conn.rollback();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");
	}
		
}