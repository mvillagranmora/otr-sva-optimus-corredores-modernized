package cl.bcs.normativa.exportacionserv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.crystaldecisions.sdk.occa.report.lib.ReportSDKException;
import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.normativa.circulares.GenerarNCG380Custodia;
import cl.bcs.normativa.circulares.GenerarNCG380Custodia.EntradaGenerarNCG380Custodia;
import cl.bcs.normativa.circulares.GenerarNCG380Custodia.SalidaGenerarNCG380Custodia;
import cl.bcs.normativa.circulares.GenerarNCG380Custodia.SalidaNCG380Custodia;
import cl.bcs.normativa.dal.LocalizadorArchivo;
import cl.bcs.normativa.data.Registro;

import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.ResumenGeneracion;
import cl.bcs.normativa.exportacion.ProcesoExportacion;
import cl.bcs.normativa.generacion.ProcesoGeneracion;
import cl.bcs.normativa.generacion.ResumenGeneracionBuilder;

import cl.bcs.normativa.lectores.Lector;
import cl.bcs.normativa.lectores.LectorBuilder;
import cl.bcs.normativa.procesos.ControladorProceso;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.normativa.procesos.ProcesoException;
import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.PropertiesUtil;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;



public class ProcesoExportacionServ extends BcsJob implements Proceso, JavaCall {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ProcesoGeneracion.class.getName());
    private boolean DEBUG = false;
    
    private static DateFormat FORMATOFECHA;
    private String SEPARADORDECIMAL;
    private String SEPARADORLINEAS;
    private String SEPARADORLINEASARCHIVO;
    private static Long IDSOLICITUDCIRCULAR;
    private static Long IDPROCESO;
    private static Locale LOCALE;
    private static int NUMFILASLOG = 0;
    private static int NUMFILASSEBRA = 0;
    
    private String SQL_CONSULTAIDREPORTE = loadResourceToString("cl/bcs/normativa/exportacionserv/sql/ProcesoExportacionServ_SelectConsultarReporte.sql");
    private String SQL_SELECTPARAMETROSCIRCULAR = loadResourceToString("cl/bcs/normativa/exportacionserv/sql/ProcesoExportacionServ_SelectParametrosCircular.sql");
    private String SQL_SELECTPARAMETROSCIRCULAR380 = loadResourceToString("cl/bcs/normativa/exportacionserv/sql/ProcesoExportacionServ_SelectParametrosCircular380.sql");
    private String SQL_SELECTPARAMETROSINFTRABCS = loadResourceToString("cl/bcs/normativa/exportacionserv/sql/ProcesoExportacionServ_SelectParametrosINFTRABCS.sql");
    private final String SQL_EXEC_SEBRAINFTRABCS = loadResourceToString("cl/bcs/normativa/exportacionserv/sql/ProcesoExportacionServ_ExecSebraINFTRABCS.sql");
    private final String SQL_SELECT_SEBRAINFTRABCS = loadResourceToString("cl/bcs/normativa/exportacionserv/sql/ProcesoExportacionServ_SelectSebraINFTRABCS.sql");
    private final String SQL_SELECT_DOCI = "SELECT DI.\"DocumentoIdentidad\" FROM \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" DI INNER JOIN \"BSCL.SocioNegocio.BD::PER.Persona\" PE ON PE.\"IdDocumentoIdentidad\" = DI.\"IdDocumentoIdentidad\" INNER JOIN \"BSCL.SocioNegocio.BD::CLI.Cliente\" CLI ON PE.\"IdPersona\"=CLI.\"IdPersona\" WHERE CLI.\"IdCliente\" =:IDCLI";
    private final String SQL_UPDATE_FEC = "UPDATE \"BSCL.Normativa.BD::NOR.SolicitudParametros\" SET \"Valor\"=:fechaGoLive WHERE \"TipoParametro\"='FINI' AND \"IdSolicitudCircular\" =:IdSolicitudParametros";
    private final String SQL_SELECT_SOLICITUDPARAMETROS_NOMSAL = loadResourceToString("cl/bcs/normativa/exportacionserv/sql/ProcesoExportacionServ_SelectSolicitudParametrosNomsal.sql");
    private final String SQL_SELECT_VERSION = loadResourceToString("cl/bcs/normativa/exportacionserv/sql/ProcesoExportacionServ_SelectVersion.sql");
    private final String SQL_CAMPO_VERSION = loadResourceToString("cl/bcs/normativa/exportacionserv/sql/ProcesoExportacionServ_SelectCampoVersion.sql");
    private final String SQL_SELECT_PARAMETROS = loadResourceToString("cl/bcs/normativa/exportacionserv/sql/ProcesoExportacionServ_SelectParametros.sql");
    		
    public static class ObjReporte {
    	public String IdReporte;
    }
    
    public static class ParametrosCircular{
    	public Long IdSolicitudParametros;
    	public String TipoParametro;
    	public String Valor;
    	public String toString() {
			return Utils.printJson(this, false);
		}
    }
    public static class ParametrosINFTRABCS {
        public String FechaIni;
        public String FechaFin;
        public String Usuario;
        public String DocIde;
        public String Nemo;
        public String Formulario;
        public int TipoAnexo;
    }
	public static class LParametrosINFTRABCS {
	        public String Data;
	}
	public static class ClienteDI {
	        public String DocumentoIdentidad;
	}
	public static class FechaAux {
	        public Long IdSolicitudParametros;
	        public String fechaGoLive;
	}
	public static class ParametroNCG380 {
		public String Valor;
		public String TipoParametro;
	}
	public static class ObjVersion {
        public String P_OUT_VERSION;
	}
    public static class ObjCampoAuto {
        public long CampoAutorizado;
    }
    
    public static class ParametrosCircularLog extends BcsPojo{
    	public Long 	IdSolicitudCircular;
    	public String	TipoParametro;
    	public String	Nombre;
    	public String	Valor;
    }
    
    private void GenerarLogSistema(SQL sql, Long idSolicitudCircular, Long cantidadRegistros, ContextoExportacionServ contexto, String tipoDocumento) throws SQLException {
    	Connection conn = contexto.getConnection();
    	
		conn.setAutoCommit(false);

    	List<ParametrosCircularLog> parametrosCircular = new ArrayList<>();
    	HashMap<String, Object> param = new HashMap<>(1);
		param.put("IdSolicitudCircular", idSolicitudCircular);
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		Long idEmpresa	= contexto.getDocumento().getIdEmpresa();
		String fechaSistema = "";
		Long usuario = contexto.getUsuario().getId();
		
		try {
			 fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		} catch (Exception e1) {
			conn.setAutoCommit(true);
			LOGGER.log(Level.SEVERE, "Error al obtener fecha sistema", e1);
		}
		
		try {
			parametrosCircular = sql.query(conn, SQL_SELECT_PARAMETROS, param, ParametrosCircularLog.class);
		} catch (SQLException e2) {
			conn.setAutoCommit(true);
			LOGGER.log(Level.SEVERE, "Error al obtener parametros de la circular para generar log", e2);
		}
		
		if(!parametrosCircular.isEmpty()) {
			AgregarLog agregarLogService = new AgregarLog();
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			List<AgregarLog.InAgregarLog.Log> logList = new ArrayList<>();
			List<AgregarLog.InAgregarLog.DetalleLog> detalleLogList = new ArrayList<>();
			
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdTipoLog		= "INFORMATIVO";
			log.IdTipoRegistro	= "SOLICITUDCIRCULAR";
			log.IdCabeceraLog	= idSolicitudCircular;
			log.IdTrabajo		= idTrabajo;
			log.IdModulo		= "NOR";
			log.IdEmpresa		= idEmpresa;
			log.FechaSistemaLog	= fechaSistema;
			log.NombreProceso	= "cl.bcs.normativa.ProcesoExportacionServ";
			log.IdUsuario		= usuario;
			
			logList.add(log);
			entradaAgregarLog.p_Log = logList;
			
			for(ParametrosCircularLog parametro : parametrosCircular) {
				AgregarLog.InAgregarLog.DetalleLog logDetalleParametros = new AgregarLog.InAgregarLog.DetalleLog();
				logDetalleParametros.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
				logDetalleParametros.IdCabeceraLog	= idSolicitudCircular; 
				logDetalleParametros.SubRegistro	= "SOLICITUDCIRCULAR"; 
				logDetalleParametros.CampoRegistro	= parametro.Nombre; 
				logDetalleParametros.ValorAntiguo	= ""; 
				logDetalleParametros.ValorNuevo		= parametro.Valor;
				detalleLogList.add(logDetalleParametros);
			}
			
			AgregarLog.InAgregarLog.DetalleLog logDetalleCantidadRegistros = new AgregarLog.InAgregarLog.DetalleLog();
			logDetalleCantidadRegistros.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
			logDetalleCantidadRegistros.IdCabeceraLog	= idSolicitudCircular; 
			logDetalleCantidadRegistros.SubRegistro		= "SOLICITUDCIRCULAR"; 
			logDetalleCantidadRegistros.CampoRegistro	= "Cantidad Registros"; 
			logDetalleCantidadRegistros.ValorAntiguo		= ""; 
			logDetalleCantidadRegistros.ValorNuevo		= cantidadRegistros.toString();
			detalleLogList.add(logDetalleCantidadRegistros);
			
			if(!"".equalsIgnoreCase(tipoDocumento.trim())) {
				AgregarLog.InAgregarLog.DetalleLog logDetalleTipoDocumento = new AgregarLog.InAgregarLog.DetalleLog();
				logDetalleTipoDocumento.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
				logDetalleTipoDocumento.IdCabeceraLog	= idSolicitudCircular; 
				logDetalleTipoDocumento.SubRegistro		= "SOLICITUDCIRCULAR"; 
				logDetalleTipoDocumento.CampoRegistro	= "TipoDocumento"; 
				logDetalleTipoDocumento.ValorAntiguo	= ""; 
				logDetalleTipoDocumento.ValorNuevo		= tipoDocumento;
				detalleLogList.add(logDetalleTipoDocumento);
			}
			
			entradaAgregarLog.p_LogDetalle = detalleLogList;
			
			try {
				agregarLogService.logica(conn, entradaAgregarLog);
			} catch (Exception e3) {
				LOGGER.log(Level.SEVERE, "Error al generar log", e3);
			}finally {
				conn.setAutoCommit(true);
			}
		}
    }

    private static String ActualizarSolicitud(long p_IdDocumento, String p_NombreArchivo,
            String p_RutaArchivo, final long p_NumRegistros,String p_RutaArchivoLog,final long p_NumRegistrosLog, ContextoExportacionServ contexto) {

        String sqlCall = "";

        String PROCEDURE_NAME_ACTUALIZAR = "BSCL.Normativa.BD.Servicios.NOR::ModificarEstadoSolicitudCircular";
        p_NombreArchivo = "'" + p_NombreArchivo + "'";
        p_RutaArchivo = "'" + p_RutaArchivo + "'";
        p_RutaArchivoLog = "'" + p_RutaArchivoLog + "'";
        sqlCall = String.format("{CALL %s.%s(%s,%s,%s,%s,%s,%s,%s)}", getQuotedString("BSCL"),
                getQuotedString(PROCEDURE_NAME_ACTUALIZAR), p_IdDocumento, p_NombreArchivo,
                p_RutaArchivo, p_NumRegistros, contexto.getIdProceso(), p_RutaArchivoLog,NUMFILASLOG);
        System.out.println(sqlCall);
        Connection conn = contexto.getConnection();

        try (CallableStatement cstmt = conn.prepareCall(sqlCall)) {
            //conn.setAutoCommit(false);

            if (cstmt.execute()) {
               // conn.commit();

                try (final ResultSet rs = cstmt.getResultSet()) {
                    if (rs.next()) {
                        LOGGER.log(Level.SEVERE, "Proceso de Actualizacion de Solicitud exitoso");
                        return "OK";
                    } else {
                        LOGGER.log(Level.SEVERE,
                                "Error durante proceso de Actualizacion de Solicitud, no RS");
                        return "NOK";
                    }

                } catch (Exception ex) {

                    LOGGER.log(Level.SEVERE, "Error durante proceso de Actualizacion de Solicitud",
                            ex);
                    return "NOK";
                }

            } else {

                LOGGER.log(Level.SEVERE, "Error no se pudo ejecutar servicio actualizacion");
                return "NOK";
            }

        } catch (Exception ex) {

            LOGGER.log(Level.SEVERE, "Error durante proceso de Actualizacion de Solicitud", ex);
            return "NOK";
        }

    }

    /**
     * Obtiene instancia de {@link ProcesoExportacion}.
     */

    private static String formatoCampo(Comparable<?> val, final Campo campo) {

        String formatoCampo = "";

        final String tipoDato = campo.getTipoDato();
        if (tipoDato.equals("DATE_FILTER") || tipoDato.equals("DATE")) {
            formatoCampo = FORMATOFECHA.format(val);
        } else
            if (tipoDato.equals("AMOUNT") || tipoDato.equals("DECIMAL") || tipoDato.equals("QTY")) {
            formatoCampo = String.format(LOCALE, "%." + campo.getCantidadDecimales() + "f",
                    ((Number) val).doubleValue());
        } else {
            formatoCampo = val.toString();
        }
        return formatoCampo;
    }
    
    private static String formatoCampo(Object val, final Campo campo) {

        String formatoCampo = "";

        final String tipoDato = campo.getTipoDato();
        if (tipoDato.equals("DATE_FILTER") || tipoDato.equals("DATE")) {
            formatoCampo = FORMATOFECHA.format(val);
        } else
            if (tipoDato.equals("AMOUNT") || tipoDato.equals("DECIMAL") || tipoDato.equals("QTY")) {
            formatoCampo = String.format(LOCALE, "%." + campo.getCantidadDecimales() + "f",
                    ((Number) val).doubleValue());
        } else {
            formatoCampo = val.toString();
        }
        return formatoCampo;
    }

    private static String getQuotedString(final String name) {
        return "\"" + name + "\"";
    }
    private static String getComillasString(final String name) {
        return "\'" + name + "\'";
    }

    /**
     * Obtiene instancia de {@link ProcesoExportacion}.
     */
    
    
    public static Proceso newInstance(final Connection conn, final Map<String, String> map,
            final long idProceso, final DataSource ds) throws SQLException {
        final Properties props = PropertiesUtil.newProperties(map);
        return new ProcesoExportacionServ(conn, props, idProceso, ds);
        // return newInstance(conn, props, idProceso);
    }

    /**
     * Contexto de exportaci&oacute;n de datos.
     */
    private ContextoExportacionServ contexto;
    
    /**
     * Constructor.
     */
    public ProcesoExportacionServ(final Connection conn, final Properties props,
            final long idProceso, final DataSource ds) throws SQLException {
        this.contexto = new ContextoExportacionServ(conn, props, idProceso, ds);
    }
    
    private void exportarServicio() throws SQLException, IOException {

        List<Campo> campos = contexto.getEsquema().getCampos();
        String nombreArchivo = "",nombreArchivoLog = "",rutaNombreResultado ="", resultadoActualizacion = "";
        
        long ParticionNombre =0,cantidadLineasParticion=0; 
        long CantParticiones =1;///Siempre debe partir en uno
        
        /*Parametros*/
        List<String> archivos = new ArrayList<String>();//Lista de archivos para ZIP
        String ParNombre 		= PropertiesUtil.getRequiredString(contexto.getParametros(),"NOMSAL");    //Nombre desde la solicitud
        String ParExtencion 	= PropertiesUtil.getRequiredString(contexto.getParametros(),"EXTSAL");    //Extencion desde la solicitud
        String ParComprimido 	= PropertiesUtil.getRequiredString(contexto.getParametros(),"COMSAL");    //Comprime el archivo en ZIP 
        String ParTitulos 		= PropertiesUtil.getRequiredString(contexto.getParametros(),"NOMCOLSAL"); //Incluye la linea de titulos en el archivo 
        long ParLineasPar 		= PropertiesUtil.getRequiredLong(contexto.getParametros(),"LINPARSAL");   // Genera particiones del archivo en base a cantidad de Lineas
        String ParCamposDesc	= PropertiesUtil.getRequiredString(contexto.getParametros(),"DESCCOL"); // Usa nombre (NOM) o Titulos (TIT) en la descripcion del campo
        String ParEnvioTandem="";
        String ParEnvioTandemCUS="";
    	try {
    		ParEnvioTandem= PropertiesUtil.getRequiredString(contexto.getParametros(),"NOMBRE_ENVIO_TANDEM_SVS"); // Usa este nombre para saber si realiza una copia en directorio TANDEM
    	}catch(Exception e) {
    		ParEnvioTandem="";
    	}
    	try {
    		ParEnvioTandemCUS= PropertiesUtil.getRequiredString(contexto.getParametros(),"NOMBRE_ENVIO_TANDEM_CUS"); // Usa este nombre para saber si realiza una copia en directorio TANDEM
    	}catch(Exception e) {
    		ParEnvioTandemCUS="";
    	}      	
        
        String IdSoliCircular	= PropertiesUtil.getRequiredString(contexto.getParametros(),"IdSolicitudCircular"); 
        String TipoDocumento =  contexto.getDocumento().getIdTipoDocumento();
        IDSOLICITUDCIRCULAR = PropertiesUtil.getRequiredLong(contexto.getParametros(),"IdSolicitudCircular");
        IDPROCESO 		= contexto.getIdProceso();
        Long IdProceso = contexto.getIdProceso();
        String rutaRaiz = LocalizadorArchivo.buscarRutaRaiz(contexto.getConnection(), contexto.getSchema(),contexto.getDocumento().getIdEmpresa());
        String rutaTandem= rutaRaiz.replace("/nor", "/TANDEM/SVSDAT/");
        String rutaTandemCUS= rutaRaiz.replace("/nor", "/TANDEM/CUSDAT/");
        
        String sqlCallExportacionNcg380Pauta = null;
        
        String tipoDoc = contexto.getDocumento().getIdTipoDocumento().toString();
        Connection conn = contexto.getConnection();
        SQL sql=new SQL();
        
        /* Caracteristicas de salida */
        if (ParCamposDesc.length()==0){ParCamposDesc="NOM";}else{ParCamposDesc="TIT";} ;
        if (tipoDoc != null && tipoDoc.toUpperCase().contains("NCG380"))
        {	
    		HashMap<String, Object> params = new HashMap<>();
    		params.put("IdSolicitudCircular", IdSoliCircular);
    		List<ParametroNCG380> parametrosSolicitud = sql.query(conn, SQL_SELECT_SOLICITUDPARAMETROS_NOMSAL, params, ParametroNCG380.class);
    		HashMap<String, String> mapParams = new HashMap<String, String>();
    		for (ParametroNCG380 p:  parametrosSolicitud) {
    			mapParams.put(p.TipoParametro, p.Valor);
    		}
    		String nomsal = mapParams.get("NOMSAL");
    		String numeroNcg380Pauta = mapParams.get("PAUTA_NCG380_NUM");
    		if (nomsal != null && numeroNcg380Pauta != null) {
    			String nombreArchivoTemp = LocalizadorArchivo.buscarNombreArchivo(
    	    	contexto.getDocumento().getIdTipoDocumento().toString(), contexto.getIdProceso());
    	    	String rutaNombreResultadoTemp = LocalizadorArchivo.buscarRutaArchivo(contexto.getConnection(),
    	    	contexto.getSchema(),contexto.getDocumento().getIdTipoDocumento().toString(),
    	    	contexto.getDocumento(), contexto.getIdProceso());
    		
    			String idString = IdProceso.toString();    			
    		    String rellenoId = rellenar(10, idString);
    		    
    		    nombreArchivo = nomsal.replace("[IdProc]", rellenoId + idString);
    		    nombreArchivo = nombreArchivo + "." +nombreArchivoTemp.split("\\.",0)[1];
   
    		    String rellenoNumero = rellenar(3, numeroNcg380Pauta);
    		    nombreArchivo = nombreArchivo.replace("[NUM]", rellenoNumero + numeroNcg380Pauta);

    		    rutaNombreResultado = rutaNombreResultadoTemp.replace(nombreArchivoTemp, nombreArchivo);
    		    	
    		    // Construye llamada a SP ExportarCircular
    		    sqlCallExportacionNcg380Pauta = String.format("{CALL %s.%s(%s,%s,%s)}", 
    		      	getQuotedString("BSCL"),
	    		    getQuotedString("BSCL.Normativa.BD.Servicios.NOR::ExportarCircular"),
	    		    IdSoliCircular,
	    		    getComillasString(tipoDoc),
	    		    contexto.getIdProceso());
    		    
            }else if(ParNombre.length()==0){
            	nombreArchivo = LocalizadorArchivo.buscarNombreArchivo(
                contexto.getDocumento().getIdTipoDocumento().toString(), contexto.getIdProceso());
            	rutaNombreResultado = LocalizadorArchivo.buscarRutaArchivo(contexto.getConnection(),
                contexto.getSchema(),contexto.getDocumento().getIdTipoDocumento().toString() ,
                contexto.getDocumento(), contexto.getIdProceso());
            }else{
            	nombreArchivo=ParNombre;
            	if (ParExtencion.length()==0){ParExtencion= ".txt";};
            	rutaNombreResultado =  rutaRaiz + File.separator + FilenameUtils.getName(nombreArchivo) + "." + ParExtencion ;
            	nombreArchivo=FilenameUtils.getName(nombreArchivo) + "." + ParExtencion ;
            }
        }else if(ParNombre.length()==0){
        	nombreArchivo = LocalizadorArchivo.buscarNombreArchivo(
            contexto.getDocumento().getIdTipoDocumento().toString(), contexto.getIdProceso());
        	rutaNombreResultado = LocalizadorArchivo.buscarRutaArchivo(contexto.getConnection(),
            contexto.getSchema(),contexto.getDocumento().getIdTipoDocumento().toString() ,
            contexto.getDocumento(), contexto.getIdProceso());
        }else{
        	nombreArchivo=ParNombre;
        	if (ParExtencion.length()==0){ParExtencion= "txt";}
        	rutaNombreResultado =  rutaRaiz + File.separator + FilenameUtils.getName(nombreArchivo) + extensionArchivo(PropertiesUtil.getRequiredString(contexto.getParametros(),"EXTSAL"), tipoDoc);
        	nombreArchivo=FilenameUtils.getName(nombreArchivo) + extensionArchivo(PropertiesUtil.getRequiredString(contexto.getParametros(),"EXTSAL"), tipoDoc) ;
        };
        nombreArchivoLog = "LOGNOR"+contexto.getIdProceso()+".csv";
        rutaNombreResultado = rutaNombreResultado.replace("\\", File.separator);
        String rutaNombreResultadoLOG =  String.format(rutaRaiz+"/"+nombreArchivoLog);
       
        FORMATOFECHA = new SimpleDateFormat(
                PropertiesUtil.getRequiredString(contexto.getParametros(), "simboloFecha"));
        SEPARADORLINEAS = PropertiesUtil.getRequiredString(contexto.getParametros(),
                "separadorListas");
        SEPARADORLINEASARCHIVO= PropertiesUtil.getRequiredString(contexto.getParametros(),
                "separadorListas");
        SEPARADORDECIMAL = PropertiesUtil.getRequiredString(contexto.getParametros(),
                "simboloDecimal");
        if (SEPARADORLINEAS.isEmpty())
            SEPARADORLINEAS = ";";
        if (SEPARADORDECIMAL.isEmpty())
            SEPARADORDECIMAL = ",";
        
        LOCALE = (SEPARADORDECIMAL.equals(".")) ? new Locale("EN") : new Locale("ES");
        


        ResumenGeneracion resumen;
        resumen = ResumenGeneracionBuilder.crearResumen(contexto, rutaNombreResultado, 0);

               
        
        String fechaGoLive = "";
        List<ParametrosCircular> ParamINFTRABCS = null;
        String FINI = "";
            String FTER = "";
            String NEMO = "";
            String IDCLI = "";
            String DocI = "";
            DateTimeFormatter formatoIn = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            final boolean autoCommit = conn.getAutoCommit();
        try {
                        fechaGoLive = ConfiguracionSistema.getParametroConfiguracionString(conn, contexto.getDocumento().getIdEmpresa(), "PLATAFORMA", "PLA.FechaGoLive");
                } catch (Exception e2) {
                        // TODO Auto-generated catch block
                        e2.printStackTrace();
                }
				
        if(TipoDocumento != null && (TipoDocumento.equalsIgnoreCase("INFTRASEIL") || TipoDocumento.equalsIgnoreCase("INFTRABCS"))) {                
                try {
                    HashMap<String, Object> paramsSoliParam = new HashMap<String, Object>();
                    paramsSoliParam.put("IdSolicitudCircular", IDSOLICITUDCIRCULAR);;
                    ParamINFTRABCS = obtenerFormatoINFTRABCS(conn, paramsSoliParam);
            }catch(Exception e) {
                    e.printStackTrace();
            }                                        
            for(ParametrosCircular paramCirt : ParamINFTRABCS) {
                        if(paramCirt.Valor!= null && paramCirt.TipoParametro.equalsIgnoreCase("FINI")) FINI = paramCirt.Valor;
                        if(paramCirt.Valor!= null && paramCirt.TipoParametro.equalsIgnoreCase("FTER")) FTER = paramCirt.Valor;
                        if(paramCirt.Valor!= null && paramCirt.TipoParametro.equalsIgnoreCase("NEMO")) NEMO = paramCirt.Valor;
                        if(paramCirt.Valor!= null && paramCirt.TipoParametro.equalsIgnoreCase("IDCLI")) IDCLI = paramCirt.Valor;
                }
            
            LocalDate fechaGL = LocalDate.parse(fechaGoLive, formatoIn);
            LocalDate fechaINI = LocalDate.parse(FINI, formatoIn);
            if (fechaGL.isAfter(fechaINI)) {
                    //La fecha de inicio de la consulta en optimus se modifica a la fechaGoLive para que no traiga data anterior
                    List<FechaAux> paramsUpt = new ArrayList<>();
                    FechaAux argU = new FechaAux();
                    argU.fechaGoLive = fechaGoLive;
                    argU.IdSolicitudParametros = IDSOLICITUDCIRCULAR;
                    paramsUpt.add(argU);
                    sql.batch(conn, SQL_UPDATE_FEC, paramsUpt, FechaAux.class);
            }
        }
       
        String nombreproc = "BSCL.Normativa.BD.Servicios.NOR::ExportarCircular";
        String sqlCall = String.format("{CALL %s.%s(%s,%s,%s)}", getQuotedString("BSCL"),
        getQuotedString(nombreproc),IdSoliCircular,getComillasString(contexto.getDocumento().getIdTipoDocumento()),contexto.getIdProceso());
        if (sqlCallExportacionNcg380Pauta != null) {
        	sqlCall = sqlCallExportacionNcg380Pauta;
        }

        System.out.println(sqlCall);
        LOGGER.log(Level.SEVERE, sqlCall);  
        
        
        HashMap<String, String> hFormatoCircular = new HashMap<String, String>();
        ///Determinar formato CIR1518
        if(TipoDocumento != null && (TipoDocumento.equalsIgnoreCase("CIR1518") == true) &&  IdSoliCircular != null && IdSoliCircular.length() != 0) {
        	Long IdSolicitudCircular = 0L;
        	try {
        		IdSolicitudCircular = Long.parseLong(IdSoliCircular);
        		if(IdSolicitudCircular.compareTo(0L) != 0) {
                	HashMap<String, Object> paramsSoliParam = new HashMap<String, Object>();
                	paramsSoliParam.put("IdSolicitudCircular", IdSolicitudCircular);
                	hFormatoCircular = obtenerFormatoSalida(conn, paramsSoliParam);
                }
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        	
        	List<Campo> listaFormato = new ArrayList<Campo>();
        	if(!hFormatoCircular.isEmpty()) {
            	for(Campo camp : campos ) {
            		if(hFormatoCircular.containsKey(camp.getNombre())) {
            			listaFormato.add(camp);
            		}
            	}
            }
        	else {
        		HashMap<String, String> hasParaEliminarEnCampo = new HashMap<String, String>();
        		hasParaEliminarEnCampo.put("SorteoLetras", "SorteoLetras"); 
        		hasParaEliminarEnCampo.put("CustodiaFisica", "CustodiaFisica");
        		for(Campo camp : campos ) {
            		if(!hasParaEliminarEnCampo.containsKey(camp.getNombre())) {
            			listaFormato.add(camp);
            		}
            	}
        	}
        	campos.clear();
        	campos = listaFormato;
        }
        
        
        ///Determinar formato NCG380CLI
		List<ParametrosCircular> Cli_Oficial = null;
        if(TipoDocumento != null && (TipoDocumento.equalsIgnoreCase("NCG380CLI") == true) &&  IdSoliCircular != null && IdSoliCircular.length() != 0) {
        	Long IdSolicitudCircular = 0L;
        	try {
        		IdSolicitudCircular = Long.parseLong(IdSoliCircular);
        		HashMap<String, Object> paramsSoliParam = new HashMap<String, Object>();
        		paramsSoliParam.put("IdSolicitudCircular", IdSolicitudCircular);;
        		Cli_Oficial = obtenerFormatoNCG380cli(conn, paramsSoliParam);

        	}catch(Exception e) {
        		e.printStackTrace();

        	}
        	List<Campo> TipoNCG380CLI = new ArrayList<Campo>();
        	if( Cli_Oficial.get(0).Valor.equalsIgnoreCase("S")) {
        		HashMap<String, String> hasParaEliminarEnCampo = new HashMap<String, String>();
        		hasParaEliminarEnCampo.put("TipoPersona", "TipoPersona"); 
        		hasParaEliminarEnCampo.put("TipoCliente", "TipoCliente");
        		hasParaEliminarEnCampo.put("FechaEnrolamiento", "FechaEnrolamiento");
        		hasParaEliminarEnCampo.put("ContratoFirmadoNCG30", "ContratoFirmadoNCG30");
        		hasParaEliminarEnCampo.put("FechaSuscripcion", "FechaSuscripcion");
        		hasParaEliminarEnCampo.put("ConfirmadoPor", "ConfirmadoPor");
        		hasParaEliminarEnCampo.put("EstadoCliente", "EstadoCliente");
        		hasParaEliminarEnCampo.put("ComentarioBloqueo", "ComentarioBloqueo");
        		hasParaEliminarEnCampo.put("ContratoCustodia", "ContratoCustodia");
        		hasParaEliminarEnCampo.put("ConflictoInteres", "ConflictoInteres");
        		hasParaEliminarEnCampo.put("InfoPrivilegiadaArt171", "InfoPrivilegiadaArt171");
        		hasParaEliminarEnCampo.put("CargoSVS171", "CargoSVS171");
        		hasParaEliminarEnCampo.put("RelCorredoraArt100", "RelCorredoraArt100");
        		hasParaEliminarEnCampo.put("RelCorredora_NCG380", "RelCorredora_NCG380");
        		hasParaEliminarEnCampo.put("RelCorredora_CIRC1064", "RelCorredora_CIRC1064");
        		hasParaEliminarEnCampo.put("Cargo_NCG_68_69", "Cargo_NCG_68_69");
        		hasParaEliminarEnCampo.put("PersonaExpuestaPoliticamente", "PersonaExpuestaPoliticamente");
        		hasParaEliminarEnCampo.put("NumeroContrato", "NumeroContrato");
        		for(Campo camp : campos ) {
            		if(!hasParaEliminarEnCampo.containsKey(camp.getNombre())) {
            			TipoNCG380CLI.add(camp);
            		}
            	}
            	campos.clear();
            	campos = TipoNCG380CLI;
            }



        }

        HashMap<String,Object> parmsConsultaReporte =new HashMap<String,Object>();
        parmsConsultaReporte.put("IdTipoDocumento", contexto.getDocumento().getIdTipoDocumento());
        List<ObjReporte> objReporte = sql.query(conn,SQL_CONSULTAIDREPORTE,parmsConsultaReporte,ObjReporte.class);
        if (objReporte.get(0).IdReporte != null && !objReporte.get(0).IdReporte.equalsIgnoreCase("")){
    		ReportDocument report;
			try {
				report = new ReportDocument(Utils.getSchema(),contexto.getDS());
				if(report.readDocument(objReporte.get(0).IdReporte)){
		    		CallableStatement spexe = null;
		    		
					spexe = conn.prepareCall("CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios.NOR::InformeMensualInversionistasRV\"(?)");
					spexe.setLong(1, IDSOLICITUDCIRCULAR);
					boolean isResultSet =spexe.execute();
					
					if(isResultSet){
						ResultSet rsSP = spexe.getResultSet();
						report.setDocumentResultSet(rsSP);
						
						try (InputStream inStreamRpt = report.exportDocument("excel")) {
							FileUtils.copyInputStreamToFile(inStreamRpt, new File(rutaRaiz, nombreArchivo));
							IOUtils.closeQuietly(inStreamRpt);
						}
					}
					resultadoActualizacion = ActualizarSolicitud(
	                    	IDSOLICITUDCIRCULAR, nombreArchivo, rutaNombreResultado,
	                           1,nombreArchivoLog,NUMFILASLOG, contexto);
	    		} else {
	    			
	    		}
			} catch (ReportSDKException e1) {
				e1.printStackTrace();
			}
    		    		
    	
    	} else if(TipoDocumento != null && "NCG380CUS".equalsIgnoreCase(TipoDocumento)){
    		Long IdSolicitudCircular = 0L;
    		Long IdEmpresa	= contexto.getDocumento().getIdEmpresa();
    		
    		if(IdSoliCircular != null && IdSoliCircular.length() != 0) {
    			IdSolicitudCircular = Long.parseLong(IdSoliCircular);
    			
    			GenerarNCG380Custodia generarNCG380Custodia = new GenerarNCG380Custodia();
    			EntradaGenerarNCG380Custodia entradaNCG380Custodia = new EntradaGenerarNCG380Custodia();
    			entradaNCG380Custodia.IdEmpresa = IdEmpresa;
    			entradaNCG380Custodia.IdSolicitudCircular = IdSolicitudCircular;
    			
    			try {
    				
    				conn.setAutoCommit(false);
    				
					SalidaGenerarNCG380Custodia salidaNCG380Custodia = generarNCG380Custodia.logica(conn, entradaNCG380Custodia);
					
					CSVWriter writer =null;
					 
					writer = new CSVWriter(
                            new OutputStreamWriter(
                            		  new FileOutputStream(rutaNombreResultado), StandardCharsets.ISO_8859_1), SEPARADORLINEAS.toCharArray()[0]);
					
					boolean withHeader = true;
                    if ("S".equals(ParTitulos))
                    {withHeader = true;
                    }else
                    {withHeader = false;}
                    
                    Long cantidadRegistrosLeidos = 0L;
					
					if(salidaNCG380Custodia.P_OUT_ERRORESCONTADOR == 0L && salidaNCG380Custodia.P_OUT_TABLA.size() > 0) {
						
                        HashMap<String,Campo> hashCampos = new HashMap<>();
                        
                        //Titulos
                        if (withHeader) {
                            final String[] entries = new String[campos.size()];
                            for (int i = 0; i < campos.size(); i++) {
                            	hashCampos.put(campos.get(i).getNombre(), campos.get(i));
                            	if ("TIT".equals(ParCamposDesc)){
                            		entries[i] = campos.get(i).getTitulo().replaceAll("\"", "");
                            	}else{
                            		entries[i] = campos.get(i).getNombre();
                            	};
                            }

                            writer.writeNext(entries);
                            withHeader = false;
                        }
                        
                        for(SalidaNCG380Custodia registro: salidaNCG380Custodia.P_OUT_TABLA) {
                        	
                            Class<?> clase = registro.getClass();
                            Field[] atributos = clase.getDeclaredFields();
                            List<Field> arrayAtributos = Arrays.asList(atributos);
                            final String[] entries = new String[campos.size()];
                            final String[] regSinDelimitador= new String[1];
                            
                            for (int i = 0; i < campos.size(); i++) {
                            	Campo campoActual = campos.get(i);
                            	Field val = arrayAtributos.stream().filter(atributo -> atributo.getName().equalsIgnoreCase(campoActual.getNombre())).findFirst().orElse(null);
                            	if(val != null) {
                            		Object valor = val.get(registro);
                            		entries[i] = formatoCampo(valor, campos.get(i)).replace("'", "");
                            		if (i==0){
                                		regSinDelimitador[0]= formatoCampo(valor, campos.get(i));
                                	}else{
                                		regSinDelimitador[0]=regSinDelimitador[0] + formatoCampo(valor, campos.get(i));
                                	};
                            	}
                            } 
                                                        
                            if (SEPARADORLINEASARCHIVO.isEmpty())
                            {
                            	writer.writeNext(regSinDelimitador, false);
                            }else{
                            	writer.writeNext(entries, false);
                            };
                            
                            cantidadLineasParticion=cantidadLineasParticion+1;
                            
                            if (ParLineasPar>0 && (cantidadLineasParticion > ParLineasPar && registro != null))
                            {
                            	writer.close();
                            	archivos.add(rutaNombreResultado);//Lista ZIP
                            	
                             	//Nombre Particion
                            	ParticionNombre=CantParticiones+1;
                            	rutaNombreResultado =  rutaRaiz + File.separator + FilenameUtils.getName(nombreArchivo) + "_Part" + ParticionNombre + "." + ParExtencion ;
                            	
                            	writer = new CSVWriter(
                                          new OutputStreamWriter(
                                          		  new FileOutputStream(rutaNombreResultado), StandardCharsets.ISO_8859_1),
                                          SEPARADORLINEAS.toCharArray()[0]); 
                            	  
                                 CantParticiones=CantParticiones + 1;
                                 cantidadLineasParticion=0;
                            };
   
                        }
                        
                        cantidadRegistrosLeidos = (long)salidaNCG380Custodia.P_OUT_TABLA.size();

					}
					
					writer.close();
                    NUMFILASSEBRA=(int)cantidadLineasParticion;
                    
                    archivos.add(rutaNombreResultado);
                    LOGGER.info(String.format("%d registros exportados", NUMFILASSEBRA));
                    
                    if("S".equals(ParComprimido)) {
                    	
    					String zipName = FilenameUtils.getName(nombreArchivo) + ".zip";
    					String zip = rutaRaiz + File.separator +  zipName;

    					try (OutputStream os = new FileOutputStream(zip)) {
    						try (ZipOutputStream zos = new ZipOutputStream(os)) {
    							for (String archivo : archivos) {
    								String fileName = FilenameUtils.getName(archivo);
    								
    								try (InputStream fis = new FileInputStream(archivo)) {
    									zos.putNextEntry(new ZipEntry(fileName));

    									int length;
    									byte[] buffer = new byte[1024];
    									while ((length = fis.read(buffer)) > 0) {
    										zos.write(buffer, 0, length);
    									}
    									zos.closeEntry();
    								}
    								FileUtils.forceDelete(new File(archivo));
    							}
    						}
    					}                              
                               
    					resultadoActualizacion = ActualizarSolicitud(IDSOLICITUDCIRCULAR, zipName, zip, cantidadRegistrosLeidos, nombreArchivoLog, NUMFILASLOG, contexto);
    					
                    }else{
                    	
                    	if (ParEnvioTandem.length()!=0) {
                    		String CopiaTandem=rutaTandem + ParEnvioTandem;
                    		 CopiarArchivo(rutaNombreResultado,CopiaTandem);		                        		
                        }
                    	
                    	if (ParEnvioTandemCUS.length()!=0) {
                    		String CopiaTandemCUS=rutaTandemCUS + ParEnvioTandemCUS;
                    		 CopiarArchivo(rutaNombreResultado,CopiaTandemCUS);
                    	}
                    	
                    	resultadoActualizacion = ActualizarSolicitud(Long.parseLong(IdSoliCircular), nombreArchivo, rutaNombreResultado, cantidadRegistrosLeidos, nombreArchivoLog, NUMFILASLOG, contexto);
                    };
                    
                    conn.setAutoCommit(true);
                    
                    GenerarLogSistema(sql, IdSolicitudCircular, cantidadRegistrosLeidos, contexto, TipoDocumento);
                    
                    if ("OK".equalsIgnoreCase(resultadoActualizacion)) {
                        resumen.setGlosa("Finalizada");
                        resumen.setTotalRegistrosGenerados(cantidadRegistrosLeidos);
                    } else if ("NOK".equalsIgnoreCase(resultadoActualizacion)) {
                        resumen.setGlosa("Actualizacion Solicitud NOK");
                    } else if ("NOKSOL".equalsIgnoreCase(resultadoActualizacion)) {
                        resumen.setGlosa("El proceso no se encuentra asociado a una solicitud");
                    }   
				} catch (Exception e) {
					 conn.setAutoCommit(true);
					// TODO Auto-generated catch block
					resumen.setGlosa("Interrumpida");
                    LOGGER.log(Level.SEVERE, "Error durante proceso de exportacion, :ExportarCircular", e);
                    LOGGER.log(Level.SEVERE, e.toString());
                    LOGGER.log(Level.SEVERE,rutaNombreResultado);
	                LOGGER.log(Level.SEVERE, e.getMessage());
	               
				}
    		}else {
    			ExportarLogNormativo(1, nombreArchivoLog, rutaNombreResultadoLOG, contexto);
                resultadoActualizacion = ActualizarSolicitud(IDSOLICITUDCIRCULAR, nombreArchivo, rutaNombreResultado, 0, nombreArchivoLog, NUMFILASLOG, contexto);
                if ("OK".equalsIgnoreCase(resultadoActualizacion)) {
                    resumen.setGlosa("Finalizada");
                } else if ("NOK".equalsIgnoreCase(resultadoActualizacion)) {
                    resumen.setGlosa("Actualizacion Solicitud NOK");
                } else if ("NOKSOL".equalsIgnoreCase(resultadoActualizacion)) {
                    resumen.setGlosa("El proceso no se encuentra asociado a una solicitud");
                }
    		}
    	}
    	else {
	        try (CallableStatement cstmt = conn.prepareCall(sqlCall)) {
	
	            if (cstmt.execute()) {
	
	                CSVWriter writer =null;	
	                try (final ResultSet rs = cstmt.getResultSet()) {
	                    try (Lector lector = LectorBuilder.newLectorServicio(contexto, sqlCall, rs); ) {
	                    	/*
	                    	final FileWriter fw = new FileWriter(rutaNombreResultado);
	                        if (SEPARADORLINEASARCHIVO.isEmpty())
	                        {	
		                        writer = new CSVWriter(
		                                new OutputStreamWriter(
		                                		  new FileOutputStream(rutaNombreResultado), "UTF-8"));
	                        }else{
	                        	  writer = new CSVWriter(
	                                      new OutputStreamWriter(
	                                      		  new FileOutputStream(rutaNombreResultado), "UTF-8"),
	                                      SEPARADORLINEASARCHIVO.toCharArray()[0]);
	                        	
	                        }; 
	                        */
	                    	 writer = new CSVWriter(
	                                 new OutputStreamWriter(
	                                 		  new FileOutputStream(rutaNombreResultado), StandardCharsets.ISO_8859_1),
	                                 SEPARADORLINEAS.toCharArray()[0]);
	                    	  
	                        Registro registro = lector.siguiente();
	                        
	                        boolean withHeader = true;
	                        if (ParTitulos.equals("S"))
	                        {withHeader = true;
	                        }else
	                        {withHeader = false;}
	                        
	                        while (registro != null)
	                        {
	                            if (withHeader) {
	                                final String[] entries = new String[campos.size()];
	                                for (int i = 0; i < campos.size(); i++) {
	                                	if (ParCamposDesc.equals("TIT")){
	                                		if(TipoDocumento != null && (TipoDocumento.equalsIgnoreCase("CIR1518") == true) && hFormatoCircular.containsKey(campos.get(i).getNombre())) {
	                                			entries[i] = hFormatoCircular.get(campos.get(i).getNombre()).replaceAll("\"", "");
	                                		}else {
	                                			entries[i] = campos.get(i).getTitulo().replaceAll("\"", "");
	                                		}

	                                	}else{
	                                		entries[i] = campos.get(i).getNombre();
	                                	};
	                                }
	
	                                writer.writeNext(entries);
	                                withHeader = false;
	                            }
	
	                            final String[] entries = new String[campos.size()];
	                            final String[] regSinDelimitador= new String[1];
	                            for (int i = 0; i < campos.size(); i++) {
	                                Comparable<?> val = registro.get(campos.get(i).getNombre());
	                                //entries[i] = val == null ? "" : formatoCampo(val, campos.get(i));
	                                if (val != null) {
	                                	entries[i] = formatoCampo(val, campos.get(i)).replace("'", "");
	                                                  	
	                                	if (i==0){
	                                		regSinDelimitador[0]= formatoCampo(val, campos.get(i));
	                                	}else{
	                                		regSinDelimitador[0]=regSinDelimitador[0] + formatoCampo(val, campos.get(i));
	                                	};
	                                }; 
	                            }
	                            
	                            if (SEPARADORLINEASARCHIVO.isEmpty())
	                            {
	                            	writer.writeNext(regSinDelimitador, false);
	                            }else{
	                            	writer.writeNext(entries, false);
	                            };
	                            
	                            if (CantParticiones==1)
	                            {
	                            	cantidadLineasParticion=lector.getCantidadRegistrosLeidos();
	                            }else{
	                            	cantidadLineasParticion=cantidadLineasParticion+1;
	                            };
	                            
	                            registro = lector.siguiente();
	                                                        
	                            //INICIO  : PARTICIONES  X LINEAS
	                           
	                            if (ParLineasPar>0 && (cantidadLineasParticion > ParLineasPar && registro != null))
	                            {
	                            	writer.close();
	                            	archivos.add(rutaNombreResultado);//Lista ZIP
	                            	
	                             	//Nombre Particion
	                            	ParticionNombre=CantParticiones+1;
	                            	rutaNombreResultado =  rutaRaiz + File.separator + FilenameUtils.getName(nombreArchivo) + "_Part" + ParticionNombre + extensionArchivo(PropertiesUtil.getRequiredString(contexto.getParametros(),"EXTSAL"), tipoDoc) ;
	                            	
	                            	writer = new CSVWriter(
	                                          new OutputStreamWriter(
	                                          		  new FileOutputStream(rutaNombreResultado), StandardCharsets.ISO_8859_1),
	                                          SEPARADORLINEAS.toCharArray()[0]); 
	                            	  
	                                 CantParticiones=CantParticiones + 1;
	                                 cantidadLineasParticion=0;
	                            };
	                            //TERMINO : PARTICIONES
	
	                           
	                        }///fin while
	                        
	                        writer.close();
	                        NUMFILASSEBRA=(int)cantidadLineasParticion;
	                        if(TipoDocumento != null && (TipoDocumento.equalsIgnoreCase("INFTRASEIL") || TipoDocumento.equalsIgnoreCase("INFTRABCS"))) {
                                FileWriter fichero = null;
                                
                                HashMap<String,Object> paramsCliR = null;
                                
                                LOGGER.info(String.format("Validación fecha GoLive para consulta de registros en sebra"));
                                
                                LocalDate fechaGL = LocalDate.parse(fechaGoLive, formatoIn);
                                LocalDate fechaINI = LocalDate.parse(FINI, formatoIn);
                                LocalDate fechaFIN = LocalDate.parse(FTER, formatoIn);
                                if (fechaGL.isAfter(fechaINI)) {                                        
                                String DBdriver = "", DBuser= "", DBpass= "", DBurl= "", DBprop= "", anio= "";
                            SQL _sql = new SQL();
                            try {                                            
                                    Properties prop = new Properties();
                                    InputStream input;
                                        if( DEBUG ) {
                                    input = new FileInputStream("C:\\GIT\\Properties\\seil.properties");
                                        } else {
                                            Long p_IdEmpresa = (Long)contexto.getDocumento().getIdEmpresa();
                                        String RutaArchivosConfiguracion = Utils.getConfiguracion(conn,  Utils.getSchema(), p_IdEmpresa, "PLA.RutaArchivosConfiguracion");
                                        LOGGER.info("Configuracion seil.properties "+RutaArchivosConfiguracion+"seil.properties");
                                        input = new FileInputStream(RutaArchivosConfiguracion+"seil.properties");
                                        }                                                
                                prop.load(input);
                                DBdriver=prop.getProperty("DBdriver");
                                DBuser=prop.getProperty("DBuser");
                                DBpass=prop.getProperty("DBpass");
                                DBurl=prop.getProperty("DBurl");
                                DBprop=prop.getProperty("DBprop");
                                input.close();
                                LOGGER.info("Configuracion seil.properties cargada");                                        
                            } catch(Exception e) {                                            
                                    LOGGER.log(Level.SEVERE, "ERROR NO SE PUEDE LEER seil.properties", e);
                                    LOGGER.log(Level.SEVERE, e.toString());
                            }          
                            BasicDataSource dsTarget = new BasicDataSource();            
                                try {                                                
                                dsTarget.setDriverClassName(DBdriver);
                                dsTarget.setUsername(DBuser);
                                dsTarget.setPassword(DBpass);
                                dsTarget.setUrl(DBurl);          
                                dsTarget.setConnectionProperties(DBprop);
                                dsTarget.setDefaultAutoCommit(true);
                                LOGGER.info("Conectado a Sebra");
                                
                                } catch( Exception e ){
                                        LOGGER.log(Level.SEVERE, "ERROR NO SE PUEDE OBTENER CONEXION SEBRA DBr", e);
                                    LOGGER.log(Level.SEVERE, e.toString());                                            
                            }
                                try( Connection conRU = dsTarget.getConnection() ) {                                                  
                                        paramsCliR = new HashMap<>();                
                                        paramsCliR.put("IDCLI", IDCLI);
                                        LOGGER.info(String.format("Ejecución de seil en sebra"));
                                        List<ParametrosINFTRABCS> paramsProceso = new ArrayList<>();
                                        ParametrosINFTRABCS arg1 = new ParametrosINFTRABCS();
                                        arg1.FechaIni = FINI;
                                        if (fechaGL.isAfter(fechaFIN)) {
                                                arg1.FechaFin = FTER;                                                
                                        }else {
                                                arg1.FechaFin = fechaGL.minusDays(1).toString();
                                        }
                                        arg1.Nemo = NEMO;
                                        List<ClienteDI> ClienteRutD = null;
                                        if(IDCLI.equalsIgnoreCase("0")) {
                                        	DocI="0";
                                        }else if(IDCLI!="") {
                                        ClienteRutD=sql.query(conn, SQL_SELECT_DOCI, paramsCliR, ClienteDI.class);
                                        for (ClienteDI rut : ClienteRutD) {
                                                DocI=rut.DocumentoIdentidad;
                                        }
                                        }
                                        
                                        arg1.DocIde=DocI;
                                        arg1.Usuario = contexto.getUsuario().getCodigo();
                                        switch (TipoDocumento) {
                                        case "INFTRASEIL":
                                                arg1.Formulario = "CtlGenSEILHisto";
                                                arg1.TipoAnexo=  0;
                                         break;
                                        case "INFTRABCS":
                                                arg1.Formulario = "CtlGenBCSHisto";
                                                arg1.TipoAnexo=  1;
                                         break;
                                        }
                                        long CampoSeil = 0;
                                        
                                        if(TipoDocumento.equals("INFTRASEIL")) {                                
                                            HashMap<String,Object> parmsConsultaVersion =new HashMap<String,Object>();
                                            parmsConsultaVersion.put("IdTipoDocumento", TipoDocumento);
                                            parmsConsultaVersion.put("v_FechaVersion",  FINI);
                                            List<ObjVersion> objVersion = sql.query(conn,SQL_SELECT_VERSION,parmsConsultaVersion,ObjVersion.class);  
                                            String VSeil = objVersion.get(0).P_OUT_VERSION;
                                            HashMap<String,Object> parmsCampoAut =new HashMap<String,Object>();
                                            parmsCampoAut.put("VSeil",  VSeil);
                                            parmsCampoAut.put("V_esquema",  TipoDocumento);
                                            List<ObjCampoAuto> objCampo = sql.query(conn,SQL_CAMPO_VERSION,parmsCampoAut,ObjCampoAuto.class);  
                                            CampoSeil = objCampo.get(0).CampoAutorizado;
                                        }
                                        if(TipoDocumento.equals("INFTRASEIL") && CampoSeil>0L) {   
                                        	arg1.TipoAnexo=  1;
                                        }
                                        paramsProceso.add(arg1);
                                        LOGGER.info(arg1.FechaIni+','+arg1.FechaFin+','+arg1.DocIde+','+arg1.Nemo+','+arg1.Formulario);
                                        _sql.batch(conRU, SQL_EXEC_SEBRAINFTRABCS, paramsProceso, ParametrosINFTRABCS.class);
                            } catch( Exception er ) {
                                    //LOGGER.log(Level.SEVERE, "ERROR NO SE PUEDE EJECUTAR SEIL EN SEBRA", er);
                                    //LOGGER.log(Level.SEVERE, er.toString());                                                    
                            }
                                try( Connection conRU = dsTarget.getConnection() ) { 
                                		
                                        List<LParametrosINFTRABCS> ListaSebra = new ArrayList<>();
                                        LOGGER.info(String.format("Consulta de data generada en sebra"));
                                        HashMap<String,Object> paramsConsSebra = new HashMap<String,Object>();
                                        paramsConsSebra.put("Usuario", contexto.getUsuario().getCodigo());
                                        switch (TipoDocumento) {
                                        case "INFTRASEIL":
                                                paramsConsSebra.put("Formulario", "CtlGenSEILHisto");
                                         break;
                                        case "INFTRABCS":
                                                paramsConsSebra.put("Formulario", "CtlGenBCSHisto");
                                         break;
                                        }
                                        ListaSebra = _sql.query(conRU, SQL_SELECT_SEBRAINFTRABCS, paramsConsSebra, LParametrosINFTRABCS.class);
                                        NUMFILASSEBRA+=ListaSebra.size();                                        
                                        fichero = new FileWriter(rutaNombreResultado, true);
                                        for(LParametrosINFTRABCS lista : ListaSebra) {
                                                fichero.write(lista.Data + "\n");
                                        }
                                        fichero.close();
                            } catch( Exception er ) {
                                    LOGGER.log(Level.SEVERE, "ERROR NO SE PUEDE CONSULTAR SEIL EN SEBRA", er);
                                    LOGGER.log(Level.SEVERE, er.toString());                                                    
                            }
                                
                                }
                        }
	                       	archivos.add(rutaNombreResultado);//Lista ZIP
	                                    
	                        if(TipoDocumento.equalsIgnoreCase("INFTRASEIL") == false && TipoDocumento.equalsIgnoreCase("INFTRABCS") == false) {
                                LOGGER.info(String.format("%d registros exportados",
                                        lector.getCantidadRegistrosLeidos()));
                              
                                       }
	                        else {
	                        	LOGGER.info(String.format("%d registros exportados",
                                        NUMFILASSEBRA));
	                        }
	                       
	                        if(ParComprimido.equals("S"))
	                        {
	                        	// INICIO : ZIP
		            			//Nombre y ruta ZIP
		    					String zipName = FilenameUtils.getName(nombreArchivo) + ".zip";
		    					String zip = rutaRaiz + File.separator +  zipName;
		    					System.out.println("ZIPFILE: " + zip);
		    					//Crear ZIP
		    					try (OutputStream os = new FileOutputStream(zip)) {
		    						try (ZipOutputStream zos = new ZipOutputStream(os)) {
		    							for (String archivo : archivos) {
		    								//obtener cada archivo a zipear
		    								String fileName = FilenameUtils.getName(archivo);
		    								System.out.println("COMPRESSFILE: " + fileName);
		    								
		    								try (InputStream fis = new FileInputStream(archivo)) {
		    									zos.putNextEntry(new ZipEntry(fileName));
		    									//escribie un archivo en el zip
		    									int length;
		    									byte[] buffer = new byte[1024];
		    									while ((length = fis.read(buffer)) > 0) {
		    										zos.write(buffer, 0, length);
		    									}
		    									zos.closeEntry();
		    									System.out.println("COMPRESSFILE: " + archivo+ " DONE");
		    								}
		    								FileUtils.forceDelete(new File(archivo));//Borrar cada arhivo incluido en el zip al terminar.
		    							}
		    						}
		    					}
		    					LocalDate fechaGL = LocalDate.parse(fechaGoLive, formatoIn);                                
                                                    if(TipoDocumento != null && (TipoDocumento.equalsIgnoreCase("INFTRASEIL") || TipoDocumento.equalsIgnoreCase("INFTRABCS"))) {  
													LocalDate fechaINI = LocalDate.parse(FINI, formatoIn);
													if(fechaGL.isAfter(fechaINI)){
														List<FechaAux> paramsUpt = new ArrayList<>();
                                                        FechaAux argU = new FechaAux();
                                                        argU.fechaGoLive = FINI;
                                                        argU.IdSolicitudParametros = IDSOLICITUDCIRCULAR;
                                                        paramsUpt.add(argU);
                                                        sql.batch(conn, SQL_UPDATE_FEC, paramsUpt, FechaAux.class);
                                                        resultadoActualizacion = ActualizarSolicitud(
                                                        		IDSOLICITUDCIRCULAR, zipName, zip,
                                                        		NUMFILASSEBRA,nombreArchivoLog,NUMFILASLOG, contexto);    
														}	
													else {
		    					 resultadoActualizacion = ActualizarSolicitud(
		                         		IDSOLICITUDCIRCULAR, zipName, zip,
		                                 lector.getCantidadRegistrosLeidos(),nombreArchivoLog,NUMFILASLOG, contexto);
                                                    }														
                                                    }
                                                    else {
		    					 resultadoActualizacion = ActualizarSolicitud(
		                         		IDSOLICITUDCIRCULAR, zipName, zip,
		                                 lector.getCantidadRegistrosLeidos(),nombreArchivoLog,NUMFILASLOG, contexto);
                                                    }
		    					//TERMINO : ZIP
	                        }else{
	                        	/*Genera copia TANDEM sin extencion y solo de Archivos no comprimidos*/
	                        	if (ParEnvioTandem.length()!=0)
	                            {
	                        		String CopiaTandem=rutaTandem + ParEnvioTandem;
	                        		System.out.println("ENVIANDO A TANDEM SVS: " + ParEnvioTandem);
	                        		System.out.println("Origen : " + rutaNombreResultado);
	                        		System.out.println("Destino : " + CopiaTandem);
	                        		 CopiarArchivo(rutaNombreResultado,CopiaTandem);
	                        		
	                        			                        		
	                            }
	                        	
	                        	/*Genera copia TANDEM sin extencion y solo de Archivos no comprimidos*/
	                        	if (ParEnvioTandemCUS.length()!=0)
	                            {
	                        		String CopiaTandemCUS=rutaTandemCUS + ParEnvioTandemCUS;
	                        		System.out.println("ENVIANDO A TANDEM CUS: " + ParEnvioTandemCUS);
	                        		System.out.println("Origen : " + rutaNombreResultado);
	                        		System.out.println("Destino : " + CopiaTandemCUS);
	                        		 CopiarArchivo(rutaNombreResultado,CopiaTandemCUS);
	                        		
	                        			                        		
	                            }
	                        	
	                        	 resultadoActualizacion = ActualizarSolicitud(Long.parseLong(IdSoliCircular)
	                        			 , nombreArchivo, rutaNombreResultado,
	                                   lector.getCantidadRegistrosLeidos(),nombreArchivoLog,NUMFILASLOG, contexto);
	                        };
	                        
	                        
	                        //ExportarLogNormativo(1, nombreArchivoLog, rutaNombreResultadoLOG, contexto);
	                        String TipoDocumentoAuxiliar = TipoDocumento != null ? TipoDocumento : "";
	                        GenerarLogSistema(sql, IDSOLICITUDCIRCULAR, lector.getCantidadRegistrosLeidos(), contexto, TipoDocumentoAuxiliar);
	
	                        if (resultadoActualizacion == "OK") {
	                            resumen.setGlosa("Finalizada");
	                            resumen.setTotalRegistrosGenerados(lector.getCantidadRegistrosLeidos());
	                        } else if (resultadoActualizacion == "NOK") {
	                            resumen.setGlosa("Actualizacion Solicitud NOK");
	
	                        } else if (resultadoActualizacion == "NOKSOL") {
	                            resumen.setGlosa("El proceso no se encuentra asociado a una solicitud");
	                        }
	
	                    } catch (Exception ex) {
	                        resumen.setGlosa("Interrumpida");
	                        LOGGER.log(Level.SEVERE, "Error durante proceso de exportacion, :ExportarCircular", ex);
	                        LOGGER.log(Level.SEVERE, ex.toString());
	                        LOGGER.log(Level.SEVERE,rutaNombreResultado);
	                    } finally {
	                       
	                    	
	                    }
	                }
	            } else {
	                ExportarLogNormativo(1, nombreArchivoLog, rutaNombreResultadoLOG, contexto);
	                resultadoActualizacion = ActualizarSolicitud(IDSOLICITUDCIRCULAR,
	                		nombreArchivo, rutaNombreResultado, 0,nombreArchivoLog,NUMFILASLOG, contexto);
	                if (resultadoActualizacion == "OK") {
	                    resumen.setGlosa("Finalizada");
	                } else if (resultadoActualizacion == "NOK") {
	                    resumen.setGlosa("Actualizacion Solicitud NOK");
	                } else if (resultadoActualizacion == "NOKSOL") {
	                    resumen.setGlosa("El proceso no se encuentra asociado a una solicitud");
	                }
	
	            } // fin if (cstmt.execute())
	        } finally {
	           // conn.setAutoCommit(autoCommit);
	        }
    	}
    }
    
    private static void CopiarArchivo(String origenArchivo, String destinoArchivo) {
        try {
            Path origenPath = Paths.get(origenArchivo);
            Path destinoPath = Paths.get(destinoArchivo);
            //sobreescribir el fichero de destino si existe y lo copia
            Files.copy(origenPath, destinoPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("ENVIO TANDEM OKEY: " + destinoArchivo);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            System.out.println("ENVIO TANDEM ERROR: " + ex.getMessage());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            System.out.println("ENVIO TANDEM ERROR: " + ex.getMessage());
        }
    }
    
    private HashMap<String, String> obtenerFormatoSalida (Connection conn, HashMap<String, Object> parametros){
    	
    	SQL sql = new SQL();
    	HashMap<String, String> paramSalida = new HashMap<String, String>();
    	List<ParametrosCircular> listaParametros = new ArrayList<ParametrosCircular>(); 
    	
    	try{
    		 listaParametros = sql.query(conn,SQL_SELECTPARAMETROSCIRCULAR,parametros,ParametrosCircular.class);
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    	}
    	
    	if(!listaParametros.isEmpty()) {
    		int largoLista = listaParametros.size();
    		String RV = "RV"; 
    		String RF = "RF"; 
    		String IF = "IF"; 
    		boolean isRV = false;
    		boolean isRF = false;
    		boolean isIF = false;
    		
    		for(ParametrosCircular paramCirt : listaParametros) {
    			if(paramCirt.Valor!= null && paramCirt.Valor.equalsIgnoreCase(RV) == true) isRV = true;
    			if(paramCirt.Valor!= null && paramCirt.Valor.equalsIgnoreCase(RF) == true) isRF = true;
    			if(paramCirt.Valor!= null && paramCirt.Valor.equalsIgnoreCase(IF) == true) isIF = true;
    		}
    		//Condiciones para formato salida
    		if(isRV == true && largoLista == 1) paramSalida = FormatoRV();
    		if(isRF == true && largoLista == 1) paramSalida = FormatoRFeIF();
    		if(isIF == true && largoLista == 1) paramSalida = FormatoRFeIF();
    		if((isRF == true && isIF == true)  && largoLista == 2) paramSalida = FormatoRFeIF();
    	}
 
    	return paramSalida;
    }    
    
    private List<ParametrosCircular> obtenerFormatoNCG380cli (Connection conn, HashMap<String, Object> parametros){
    	
    	SQL sql = new SQL();

    	List<ParametrosCircular> listaParametros = new ArrayList<ParametrosCircular>(); 
    	
    	try{
    		 listaParametros = sql.query(conn,SQL_SELECTPARAMETROSCIRCULAR380,parametros,ParametrosCircular.class);
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    	}
    	
    	return listaParametros;
    }
    
    private List<ParametrosCircular> obtenerFormatoINFTRABCS (Connection conn, HashMap<String, Object> parametros){            
        SQL sql = new SQL();
        List<ParametrosCircular> listaParametros = new ArrayList<ParametrosCircular>();
        
        try{
                 listaParametros = sql.query(conn,SQL_SELECTPARAMETROSINFTRABCS,parametros,ParametrosCircular.class);
        }catch(Exception e) {                    
                e.printStackTrace();
        }            
        return listaParametros;

    }
    private HashMap<String, String> FormatoRV(){
    	
    	HashMap<String, String> paramRV = new HashMap<String, String>();
    	paramRV.put("FechaCuadraturaCustodia", "FechaCuadraturaCustodia");
    	paramRV.put("Nemotecnico", "Instrumento");
    	paramRV.put("CantidadCustodia", "Saldo Segun Registro Custodia (N acciones)");
    	paramRV.put("CantidadGarantia", "Acciones en Garantias (N acciones)");
    	paramRV.put("CantidadPlazoVenta", "Venta a Plazo (N acciones)");
    	paramRV.put("CantidadPlazoCompra", "Compra a Plazo (N acciones)");
    	paramRV.put("CantidadTotal", "Total Custodia (N acciones)");
    	paramRV.put("CantidadDCVRelacionado", "DCV Terceros (N acciones)");
    	paramRV.put("CantidadDCVTerceros", "DCV Relacionados (N acciones)");
    	paramRV.put("CantidadDCVCuentaPropia", "DCV Cta. Propia (N acciones)");
    	paramRV.put("CantidadBCS", "Saldo BCS (N acciones)");
    	paramRV.put("CantidadBEC", "Saldo BEC (N acciones)");
    	paramRV.put("CantidadBOVALPO", "Saldo BOVALPO (N acciones)");
    	paramRV.put("CantidadEmisores", "Saldo Emisores (N acciones)");
    	paramRV.put("CantidadComprasEnTransito", "Compras en Transito (N acciones)");
    	paramRV.put("CantidadVentasEnTransito", "Venta en Transito (N acciones)");
    	paramRV.put("CantidadTransitoIngreso", "Ingreso de custodia (N acciones)");
    	paramRV.put("CantidadTransitoEgreso", "Egreso de custodia (N acciones)");
    	paramRV.put("TotalCustodiosyEmisores", "Total Custodios y Emisores (N acciones)");
    	paramRV.put("Diferencia", "Diferencia (N acciones)");
    	paramRV.put("PrecioCierre", "Precio de Cierre al ultimo dia de la semana");
    	paramRV.put("Valorizacion", "Valorizacion saldo de custodia (19*17)");
    	paramRV.put("DiferenciaValorizada", "Diferencia valorizada (19*18)");
    	paramRV.put("ExplicacionDescuadratura", "Explicacion diferencias");
    	paramRV.put("Mercado", "Mercado");
    	paramRV.put("TipoInstrumento", "TipoInstrumento");
    	return paramRV;
    }
    
    private HashMap<String, String> FormatoRFeIF(){
    	
    	HashMap<String, String> paramRF_IF = new HashMap<String, String>();
    	paramRF_IF.put("FechaCuadraturaCustodia", "FechaCuadraturaCustodia");
    	paramRF_IF.put("Nemotecnico", "Instrumento");
    	paramRF_IF.put("CantidadCustodia", "Saldo Segun Registro Custodia");
    	paramRF_IF.put("CantidadGarantia", "Instrumentos en Garantias (N unidades)");
    	paramRF_IF.put("CantidadPlazoVenta", "Compromisos Ventas (N Unidades)");
    	paramRF_IF.put("CantidadPlazoCompra", "Compromisos Compras (N unidades)");
    	paramRF_IF.put("CantidadTotal", "Total Custodia (N unidades)");
    	paramRF_IF.put("CantidadDCVRelacionado", "DCV Terceros (N unidades)");
    	paramRF_IF.put("CantidadDCVTerceros", "DCV Relacionados (N unidades)");
    	paramRF_IF.put("CantidadDCVCuentaPropia", "DCV Cta. Propia (N unidades)");
    	paramRF_IF.put("CantidadBCS", "Saldo BCS (N unidades)");
    	paramRF_IF.put("CantidadBEC", "Saldo BEC (N unidades)");
    	paramRF_IF.put("CantidadBOVALPO", "Saldo BOVALPO (N unidades)");
    	paramRF_IF.put("SorteoLetras", "SorteoLetras");
    	paramRF_IF.put("CantidadEmisores", "Custodia Fisica en Corredor (N unidades)");
    	paramRF_IF.put("CantidadComprasEnTransito", "Compras en Transito (N unidades)");
    	paramRF_IF.put("CantidadVentasEnTransito", "Ventas en Transito (N unidades)");
    	paramRF_IF.put("CantidadTransitoIngreso", "Ingreso de Custodia (N unidades)");
    	paramRF_IF.put("CantidadTransitoEgreso", "Egreso de Custodia (N unidades)");
    	paramRF_IF.put("TotalCustodiosyEmisores", "Total Custodios y Emisores (N unidades)");
    	paramRF_IF.put("Diferencia", "Diferencia (N unidades)");
    	paramRF_IF.put("PrecioCierre", "Precio de Cierre al Ultimo dia de la semana / Tasa");
    	paramRF_IF.put("Valorizacion", "Valorizacion Saldo de custodia");
    	paramRF_IF.put("DiferenciaValorizada", "Diferencia Valorizada");
    	paramRF_IF.put("ExplicacionDescuadratura", "Explicacion Diferencia");
    	paramRF_IF.put("TipoInstrumento", "TipoInstrumento");
    	return paramRF_IF;
    }
    
    private static String extensionArchivo(String parExtension, String idTipoDocumento) {
    	String extension = (parExtension.length() == 0) ? ".txt" : "." + parExtension ;
    	if (idTipoDocumento.toUpperCase().contains("PACVIGEBOX") && parExtension.length() == 0) { 
    		extension = "";
    	}
    	return extension;
    }
    
    private String ExportarLogNormativo(final long p_IdEmpresa,String NombreArchivoLOG,String rutaNombreResultadoLOG,ContextoExportacionServ contexto) {

        String sqlCall = "";
        

        Connection conn = contexto.getConnection();
        
        String PROCEDURE_NAME_CONSULTARLOGNOR = "BSCL.Normativa.BD.Servicios.NOR::ConsultarLogNormativo";
        NombreArchivoLOG = "'" + NombreArchivoLOG + "'";
        
        sqlCall = String.format("{CALL %s.%s(%s,%s)}", getQuotedString("BSCL"),getQuotedString(PROCEDURE_NAME_CONSULTARLOGNOR),0,IDSOLICITUDCIRCULAR);
        System.out.println(sqlCall);
        

        try (CallableStatement cstmt = conn.prepareCall(sqlCall)) {
            if (cstmt.execute()) {
               try (final ResultSet rs = cstmt.getResultSet()) {
            	   
            	   //FileWriter fw = new FileWriter(rutaNombreResultadoLOG);
                   CSVWriter writer = new CSVWriter(
                           new OutputStreamWriter(
                                   new FileOutputStream(rutaNombreResultadoLOG), "UTF-8"),
                           SEPARADORLINEAS.toCharArray()[0]);

                   //utilizo cachedrowset para almacenar resulset en memoria (log nunca serán tantos registros) ya que Hana no permite recorrer por scrolling el resultset
                   CachedRowSet crs = null ;
                   crs=(CachedRowSet)Class.forName("com.sun.rowset.CachedRowSetImpl").newInstance();
                   crs.populate(rs);
                   NUMFILASLOG = resultSetCount(crs);
                   crs.beforeFirst();
                   writer.writeAll(crs, true);
            	   writer.close();
               }
               return "OK";
           } else {

        	   	return "NOK";

           } // fin if (cstmt.execute())

        } catch (Exception ex) {

            LOGGER.log(Level.SEVERE, "Error durante proceso de creacion LOGNOR", ex);
            return "NOK";
        }

        }

    @Override
    public void procesar() throws ProcesoException {
        LOGGER.info(String.format("Se recibio solicitud de proceso de exportacion."));

        try (final ControladorProceso controlador = new ControladorProceso(contexto)) {
            try {
                exportarServicio(); 
            } catch (SQLException ex) {
                final String msg = "Error durante proceso de exportacion";
                
                LOGGER.log(Level.SEVERE, msg, ex);
                throw new ProcesoException(msg, ex);
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    private static String rellenar(int largo, String contenido) {
    	String relleno = "";
	    for (int i=0; i<largo-contenido.length(); i++){
	    	relleno = relleno + "0";
	    }
    	return relleno;
	}
        
    private static int resultSetCount(ResultSet resultSet) throws SQLException{

            int i = 0;
            while (resultSet.next()) {
                i++;
        }
        return i;
    }

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
