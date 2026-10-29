package cl.bcs.normativa.circulares;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.plataforma.evt.AgregarEvento.ObjIDEVENTO;

public class NotificarNCG365FFMM implements JavaCall {


	private static final Logger logger = LogManager.getLogger(NotificarNCG365FFMM.class);
	
	private final String SQL_SELECT_SERIES 					= loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarNCG365FFMM_SelectSeries.sql");
	private final String SQL_SELECT_SECUENCIAS 				= loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarNCG365FFMM_SelectSecuencias.sql");
	private final String SQL_SELECT_INFO_OPERACIONES_FFMM 	= loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarNCG365FFMM_SelectInfoMovimiento.sql");
	private final String SQL_SELECT_ARCHIVOS_NCG365 		= loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarNCG365FFMM_SelectArchivoNCG365.sql");
	private final String SQL_SELECT_CAMPOS_DEFECTO_NOTIFICACION 	= loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarNCG365FFMM_SelectCamposDefectoNotificacionNCG365.sql");
	private final String SQL_SELECT_PERSONA_REMITENTE 	= loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarNCG365FFMM_SelectPersonaRemitente.sql");
	private final String SQL_SELECT_CONTROL_PROCESO 	= loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarNCG365FFMM_SelectConfiguracionEventoControlProceso.sql");
	private final String SQL_SELECT_PROGRAMA_TERMINADO  = loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarNCG365FFMM_SelectProgramaTerminado.sql");
	
	public static class NotificarNCG365FFMM_Entrada {
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_FechaCertificacion = "0001-01-01";  //Fecha de certificacion de las operaciones a incluir en el correo. Si viene 0001-01-01  toma fecha de sistema
		public String		p_EsAgenda = "N";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Orden {
		public Long IdOrden;
		
	}
	
	public static class ObjSeries {
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdPersona;
		public String  DocumentoIdentidad;
		public String IdSerieFondo;
		public String NombreFondo;
		public Long    DiasComision;
		public BigDecimal PorcentajeComision;
		public BigDecimal PorcentajeRemuneracion;
		public BigDecimal PorcentajeGastoOpera;
		public BigDecimal PorcentajeCostoAdm;
		
		
		private String Tabla;
		
		public String getTabla() {
			return Tabla;
		}

		public void setTabla(String tabla) {
			Tabla = tabla;
		}
		
		private String Row;
		
		
		public void setRow(String row) {
			Row = row;
		}
		public String getRow() {
			return Row;
		}

		
	}
	
	
	public static class ObjSecuencias {
		public Long 	IdCliente;
		public Long 	IdPortafolio;
		public Long 	IdPersona;
		public Long 	IdDireccionElectronicaPrincipal;
		public String  	DocumentoIdentidad;
		public String  	NombreDestinatario;
		public String  	RazonCorredora;
		public String  	RutCorredora;
		public String  	Agrupador;
		
		private String Tabla;
		
		public String getTabla() {
			return Tabla;
		}

		public void setTabla(String tabla) {
			Tabla = tabla;
		}
		
	}
	
	public static class ArchivoNCG365 extends BcsPojo {
		public Long			IdCliente;
		public String		NombreArchivo;
		public String		UbicacionZip;
		public String 		FechaCertificacion;
		public byte[]		Archivo;
	}
	
	public static class ArchivoAdjunto extends BcsPojo{
		public Long			IdEventoArchivoAdjunto;
		public Long			IdEvento;
		public String		NombreArchivo;
		public byte[]		Archivo;
		public String 		Contrasenia;
		
		public ArchivoAdjunto () {
			this.IdEventoArchivoAdjunto = 0L;
			this.IdEvento= 0L;
			this.NombreArchivo = "";
			this.Archivo = new byte[0];
			this.Contrasenia ="";
		}
		
		public ArchivoAdjunto (Long	 idEventoArchivoAdjunto, Long idEvento,byte[] archivo, String nombreArchivo, String contrasenia) {
			this.IdEventoArchivoAdjunto = idEventoArchivoAdjunto;
			this.IdEvento= idEvento;
			this.NombreArchivo = nombreArchivo;
			this.Archivo = archivo;
			this.Contrasenia =contrasenia;
		}
	}
	
	public static class ObjInfo {
		
		public Long IdSolicitudAporteRescate;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdPersona;
		public String TipoSolicitud;
		public String IdSerieFondo;
		public String IdTipoMovimientoFondoMutuo;
		public Date FechaCertificacion;
		public Date FechaLiquidacion;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
		public BigDecimal ValorCuota;
		public BigDecimal CostoUF;
		public String IdMoneda;
		public Long IdEmpresa;
		public String EstadoSolicitud;
		public Long IdInstrumento;
		public String NombreSerieFondo;
		public String CodigoSerie;
		public String IdFondo;
		public String IdEmisor;
		public String Nemotecnico;
		public String NombreFondo;
		public String DescripcionTipoSolicitud;
		public String DescripcionTipoMovimiento;
		public Date FechaCertificacionRegistro;
		public String IdRegimenTributario;
		public String DescripcionRegimenTributario;
		public String NombreDestinatario;
		public String DocumentoIdentidad;
		
		public String RutEmisor;
		public String FechaCertificacionRegistroStr;
		public String HoraCertificacionRegistro;
		public String NombreMoneda;
		
		public String DiaCertificacionRegistro;
		public String MesCertificacionRegistro;
		public String AgnoCertificacionRegistro;
		
		
		
		
		private String Row;
		
		
		public void setRow(String row) {
			Row = row;
		}
		public String getRow() {
			return Row;
		}

		
	
		
	}	
	
	public static class ObjDirs {
		public Long 	  IdCliente;
		public Integer 	IdDireccion;
		public String 	  TipoDestinatario;
		@Override
		public String toString() {
			return "ObjDirs [IdCliente=" + IdCliente + ", IdDireccion=" + IdDireccion + ", TipoDestinatario="
					+ TipoDestinatario + "]";
		}	
	}
	
	public static class NotificarNCG365FFMM_Salida {
		 public Long P_OUT_ERRORESCONTADOR;
	}

	public static class CampoDefectoNotificacion extends BcsPojo {
		public String CodigoTipoEvento;
		public String NombreCampo;
		public String Valor;
		
		public CampoDefectoNotificacion() {
			this.CodigoTipoEvento = "";
			this.NombreCampo = "";
			this.Valor = "";
		}
	}
	public static class PersonaRemitente extends BcsPojo{
		public Long IdPersona;
		public Long IdDireccionElectronica;
		public PersonaRemitente() {
			this.IdPersona = 0L;
			this.IdDireccionElectronica = 0L;
		}
	}
	
	public static class ControlProceso extends BcsPojo{
		public String ControlProceso;
		
		public ControlProceso () {
			this.ControlProceso = "S";
		}
	}
	
	public static class ProgramaEjecutado extends BcsPojo{
		public Long IdPrograma;
	}
	
	public void logicaAsync(Connection con,NotificarNCG365FFMM_Entrada in) throws Exception {
		Gson gson = getGsonBuilder();	
		JsonParser parser 	= new JsonParser();
        JsonObject dataObj	= parser.parse(gson.toJson(in)).getAsJsonObject();		
        String CallBatchURL=ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.CallBatchURL");
        String CallBatchToken=ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.CallBatchToken");
		Utils.CallBatchAsync(CallBatchURL, CallBatchToken, "PLUGIN_NOR.jar@cl.bcs.normativa.circulares.NotificarNCG365FFMM", dataObj);
	}
	
	
	// ---------------------------------------------------
	// LOGICA
	// ---------------------------------------------------
	
	public NotificarNCG365FFMM_Salida logica(Connection con, NotificarNCG365FFMM_Entrada in) throws Exception {
		SQL sql = new SQL();
		
		logger.info(":: INICIO NotificarNCG365FFMM ::");
		
		NotificarNCG365FFMM_Salida out = new NotificarNCG365FFMM_Salida();
		
        int SegundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, in.p_IdEmpresa);
        Fecha.setLocalOffSet(SegundosZonaHoraria);        
        String 	tFechaHoraSistema 	= FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
		Timestamp vFechaHoraSistema = Fecha.toTimeStamp(tFechaHoraSistema);  
		Long IdEmpleadoUsuarioSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa, "PLATAFORMA", "ACC.IdUsuarioBolsa");
		
		HashMap<String, Object> paramsConf = new HashMap<String, Object>(1);
		paramsConf.put("IdEmpleado", IdEmpleadoUsuarioSistema);
		PersonaRemitente personaRemitente = sql.query(con, SQL_SELECT_PERSONA_REMITENTE, paramsConf, PersonaRemitente.class).stream().findFirst().orElse(new PersonaRemitente());
		
		paramsConf.clear();
		paramsConf.put("IdEmpresa", in.p_IdEmpresa);
		ControlProceso controlProceso = sql.query(con, SQL_SELECT_CONTROL_PROCESO, paramsConf,ControlProceso.class).stream().findFirst().orElse(new ControlProceso());
		
        out.P_OUT_ERRORESCONTADOR=0L;
        
        HashMap<String, Object> paramsCamposDefecto = new HashMap<String, Object>(2);
        paramsCamposDefecto.put("CodigoTipoEvento", "NOTIFICAR_NCG365FFMM_CLIENTE");
        paramsCamposDefecto.put("IdEmpresa", in.p_IdEmpresa);
        Map<String, CampoDefectoNotificacion> camposDefecto = sql.query(con, SQL_SELECT_CAMPOS_DEFECTO_NOTIFICACION, paramsCamposDefecto, CampoDefectoNotificacion.class).stream().collect(Collectors.toMap(cmpDefecto -> cmpDefecto.NombreCampo, cmpDefecto -> cmpDefecto));
        
        String fechaSistema   = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        String fechaCertificacion = (!in.p_FechaCertificacion.equalsIgnoreCase("0001-01-01") && !in.p_FechaCertificacion.equalsIgnoreCase("")) ? in.p_FechaCertificacion : fechaSistema;
        Long diasAtrasoNotificacionNCG365 = 0L;
        if(("S").equals(in.p_EsAgenda)) {
        	if(programaYaEjecutadoEnElDia(con, sql, fechaSistema)) {
        		logger.info("NotificarNCG365FFMM :: Programa ya ejecutado durante el día");
        		return out;
        	}
        	diasAtrasoNotificacionNCG365 = obtenerParametrOdiasAtrasoNotificacionNCG365(con, in.p_IdEmpresa);
        	diasAtrasoNotificacionNCG365++;
        	final long diasAtraso = diasAtrasoNotificacionNCG365;
        	ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores entradaObtenerDiasHabilesAnteriores = new ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores();
            entradaObtenerDiasHabilesAnteriores.idPais = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
    		entradaObtenerDiasHabilesAnteriores.diaHabil = "S";
    		entradaObtenerDiasHabilesAnteriores.soloCaeDiaHabil = "S";
    		FechasIn fecha = new FechasIn(fechaSistema, diasAtrasoNotificacionNCG365);
    		entradaObtenerDiasHabilesAnteriores.fechas.add(fecha);
    		ObtenerDiasHabilesAnteriores obtenerDiasHabilesAnteriores = new ObtenerDiasHabilesAnteriores();
    		OutObtenerDiasHabilesAnteriores salidaObtenerDiaHabilAnterior = obtenerDiasHabilesAnteriores.logica(con, entradaObtenerDiasHabilesAnteriores);
    		fechaCertificacion = salidaObtenerDiaHabilAnterior.fechas.stream().filter(u -> u.numeroDiasInicial == diasAtraso && fechaSistema.equals(u.fechaInicial)).map(u -> u.fechaCalculada).findFirst().orElse(fechaCertificacion);
        }
        
        
        String DiaCertificacionRegistro=""; 
        String AgnoCertificacionRegistro=""; 
        String MesCertificacionRegistro="";
         
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdEmpresa", in.p_IdEmpresa);		
		params.put("FechaCertificacion", fechaCertificacion);
		logger.info("NotificarNCG365FFMM :: Fecha Certificacion: "+ fechaCertificacion);
		
		
		// ------------------------------------------------------------------------------------
		// Obtiene datos de los destinatarios para la notificar (Nombre, Direccion) **
		List<ObjSecuencias> listaSecuencias;
		listaSecuencias= sql.query(con, SQL_SELECT_SECUENCIAS, params, ObjSecuencias.class);
		

		
		// ------------------------------------------------------------------------------------
		// Obtiene datos de operaciones de FFMM  (para armar parametro Tabla1)
		List<ObjInfo> listaInfo= sql.query(con, SQL_SELECT_INFO_OPERACIONES_FFMM, params, ObjInfo.class);
		
		
		
		// ------------------------------------------------------------------------------------
		// Obtiene datos de operaciones de FFMM con Serie (para armar parametro Tabla2)
		List<ObjSeries> listaSeries;
		listaSeries= sql.query(con, SQL_SELECT_SERIES, params, ObjSeries.class);	
		logger.info("NotificarNCG365FFMM :: Cantidad Notificaciones: "+ listaSecuencias.size());
		
		// ------------------------------------------------------------------------------------
		// Obtiene archivos generados para la NCG365
		HashMap<String, Object> paramsSelectArchivos = new HashMap<String, Object>();
		paramsSelectArchivos.put("FechaCertificacion", params.get("FechaCertificacion").toString());
		Map<Long, ArchivoNCG365> mapArchivos = sql.query(con, SQL_SELECT_ARCHIVOS_NCG365, paramsSelectArchivos, ArchivoNCG365.class).stream().collect(Collectors.toMap(archivo -> archivo.IdCliente, archivo -> archivo));
		logger.info("NotificarNCG365FFMM :: Cantidad de Archivos: "+ mapArchivos.size());
		
		ConsultarImagen consultarImagen = new ConsultarImagen();
		ConsultarImagenEntrada consultarImagenIn = new ConsultarImagenEntrada();
		consultarImagenIn.p_IdEmpresa = in.p_IdEmpresa;
		consultarImagenIn.p_imagenNombres.add("LOGO_CORREDOR_BANNER");
		consultarImagenIn.p_imagenNombres.add("NCG365_LOGO_INVESTMENTS");
		ConsultarImagenSalida consultarImagenOut = consultarImagen.logica(con, consultarImagenIn);
		// ------------------------------------------------------------------------------------
		//Agrega notificacion
		AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
        notifIn.P_IDEMPRESA 		= in.p_IdEmpresa;
        notifIn.P_IDUSUARIO 		= in.p_IdUsuario;
        notifIn.P_IDMODULO 			= in.p_IdModulo;
        notifIn.P_EVENTOS 			= new ArrayList<>();
        notifIn.P_VALORES 			= new ArrayList<>();
        notifIn.P_ARCHIVOSADJUNTOS 	= new ArrayList<>();
        notifIn.P_IDDIRECCIONES 	= new ArrayList<>(); 
        notifIn.P_CONTROLPROCESO	= controlProceso.ControlProceso;
        // Evento de notificacion
        for (ObjSecuencias ord:listaSecuencias) {        	
        	logger.info("Armando Evento > IdCliente:" + ord.IdCliente + " IdDireccionElectronicaPrincipal:" + ord.IdDireccionElectronicaPrincipal);
        	
            AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
            evt.CodigoTipoEvento 		= "NOTIFICAR_NCG365FFMM_CLIENTE";
            evt.IdSecuencia 			= ord.IdCliente;
            evt.FechaCreacionSistema 	= Fecha.toStringDate(vFechaHoraSistema);            
            evt.IdPersonaRemitente 		= personaRemitente.IdPersona;
            evt.IdPersonaDestinatario 	= ord.IdPersona;            
            evt.IdPortafolio 			= 0L;
            notifIn.P_EVENTOS.add(evt);
            
            notifIn.addValor(ord.IdCliente, "NombreDestinatario", ord.NombreDestinatario);
            notifIn.addValor(ord.IdCliente, "RazonCorredora", camposDefecto.get("RazonCorredora") != null ? camposDefecto.get("RazonCorredora").Valor : camposDefecto.get("RazonCorredoraDefecto").Valor);
            notifIn.addValor(ord.IdCliente, "NumeroCallCenter", camposDefecto.get("NumeroCallCenter") != null ? camposDefecto.get("NumeroCallCenter").Valor : camposDefecto.get("NumeroCallCenterDefecto").Valor);
            notifIn.addValor(ord.IdCliente, "LogoCorredorBanner", consultarImagenOut.OUT_IMAGENES.stream().filter(imagen -> "LOGO_CORREDOR_BANNER".equalsIgnoreCase(imagen.NombreImagen)).count() > 0 ? consultarImagenOut.OUT_IMAGENES.stream().filter(imagen -> "LOGO_CORREDOR_BANNER".equalsIgnoreCase(imagen.NombreImagen)).collect(Collectors.toList()).get(0).RutaPublica : "" );
        	notifIn.addValor(ord.IdCliente, "NCG365LogoInvestments", consultarImagenOut.OUT_IMAGENES.stream().filter(imagen -> "NCG365_LOGO_INVESTMENTS".equalsIgnoreCase(imagen.NombreImagen)).count() > 0 ? consultarImagenOut.OUT_IMAGENES.stream().filter(imagen -> "NCG365_LOGO_INVESTMENTS".equalsIgnoreCase(imagen.NombreImagen)).collect(Collectors.toList()).get(0).RutaPublica : "" );
         }
        // Agregar direcciones al evento
        for (ObjSecuencias dir:listaSecuencias) { 
        	if (dir.IdDireccionElectronicaPrincipal==null || dir.IdDireccionElectronicaPrincipal==0) continue;
        	logger.info("Armando Direcciones > IdCliente:" + dir.IdCliente + " IdDireccionElectronicaPrincipal:" + dir.IdDireccionElectronicaPrincipal);
        	
            AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
            ndir.IdSecuencia 		= dir.IdCliente;
            ndir.TipoDestinatario 	= "TO";
            ndir.IdDireccion 		= new Long(dir.IdDireccionElectronicaPrincipal);
            notifIn.P_IDDIRECCIONES.add(ndir);
        }
        
        if(!mapArchivos.isEmpty()) {
        	// Agregar archivos adjuntos
            for (ObjSecuencias dir:listaSecuencias) {
            	AgregarEvento.ObjARCHIVOSADJUNTOS adj = new AgregarEvento.ObjARCHIVOSADJUNTOS();
            	adj.IdSecuencia = dir.IdCliente;
            	adj.Nombre = mapArchivos.get(dir.IdCliente).NombreArchivo;
            	adj.Contrasenia = "";
            	adj.ArchivoAdjunto = mapArchivos.get(dir.IdCliente).Archivo;
            	logger.info("Armando Archivos Adjuntos > IdCliente:" + adj.IdSecuencia + " Nombre Archivo:" + adj.Nombre);
            	notifIn.P_ARCHIVOSADJUNTOS.add(adj);
            	
            }
		}

        if (notifIn.P_EVENTOS.size() > 0) {
        	
        	logger.info("Hay "+ notifIn.P_EVENTOS.size() +" notificaciones para enviar");
        	logger.info("Se Llama a Servicio AgregarEvento");
        	
            AgregarEvento notif = new AgregarEvento();
            AgregarEvento.ServiceSalida notifOut = notif.logica(con, notifIn);

            for (ObjIDEVENTO eve: notifOut.P_OUT_IDEVENTO) 
            {            	
            	logger.info("Secuencia de Evento Creada > " + eve.IdEvento);
            }
            logger.info("Finaliza Llamada a Servicio AgregarEvento");
            
        }
   

        
        logger.info(":: TERMINO NotificarNCG365FFMM ::");
        
        
		return out;
	}
	
	
	
	// ---------------------------------------------------
	// CALL
	// ---------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		NotificarNCG365FFMM_Entrada in=gson.fromJson(jin.toString() ,NotificarNCG365FFMM_Entrada.class);
		NotificarNCG365FFMM_Salida out=logica(con,in);		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	

	private Long obtenerParametrOdiasAtrasoNotificacionNCG365(Connection dbcon, Long idEmpresa) {
		Long diasAtrasoNotificacionNCG365 = 0L;
		try {
			diasAtrasoNotificacionNCG365 = ConfiguracionSistema.getParametroConfiguracionLong(dbcon, idEmpresa, "FONDOMUTUO", "DiasAtrasoNotificacionNCG365");
		}catch(Exception e) {
			logger.info("ADVERTENCIA::BcsJobncg365FFMMMasivo::No se encuentra parametro DiasAtrasoNotificacionNCG365");
		}
		return diasAtrasoNotificacionNCG365;
	}
	
	private Boolean programaYaEjecutadoEnElDia(Connection con, SQL sql, String fechaSistema) {
		logger.trace("INICIO::NotificarNCG365FFMM.programaYaEjecutadoEnElDia");
		long inicio = System.currentTimeMillis();
		
		Boolean programaEjecutado = false;
		HashMap<String, Object> paramSelectPrograma = new HashMap<String, Object>();
    	paramSelectPrograma.put("FechaSistema", fechaSistema);
    	
    	try {
			List<ProgramaEjecutado> listaProgramaEjecutado = sql.query(con, SQL_SELECT_PROGRAMA_TERMINADO, paramSelectPrograma, ProgramaEjecutado.class);
			if(listaProgramaEjecutado.size() > 0) {
				programaEjecutado = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    			
		long fin = System.currentTimeMillis();
		logger.trace("FIN::NotificarNCG365FFMM.programaYaEjecutadoEnElDia, tiempo: " + (fin - inicio));
		return programaEjecutado;
	}
	
	// ---------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// ---------------------------------------------------
	public static void main(String[] args){
		
		System.out.println("== TEST ==");
		
		try (Connection con =  PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
			NotificarNCG365FFMM sp = new NotificarNCG365FFMM();
			NotificarNCG365FFMM_Entrada in = new NotificarNCG365FFMM_Entrada();
			in.p_IdEmpresa 			= 1L;
			in.p_IdModulo 			= "NOR";
			in.p_IdUsuario 			= 0L;
			in.p_FechaCertificacion = "0001-01-01"; // "2020-08-03";  // Si va en 0001-01-01  toma la fecha de sistema
			in.p_EsAgenda = "S";
			
			sp.logica(con, in);
//			con.commit();
			con.rollback();
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}

}
