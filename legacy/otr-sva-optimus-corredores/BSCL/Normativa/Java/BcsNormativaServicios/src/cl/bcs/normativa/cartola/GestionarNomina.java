
package cl.bcs.normativa.cartola;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.Calendar;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bat.AgregarAgenda;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.SalidaServicios.ErrorDetalle;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.Feriados;


public class GestionarNomina implements JavaCall{
	
	private boolean DEBUG = false;
	private static final Logger logger = LogManager.getLogger(GestionarNomina.class);
	
	private final String SQL_INSERT_NOMINA = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarNomina_InsertNomina.sql");
	private final String SQL_UPDATE_NOMINA = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarNomina_UpdateNomina.sql");
	private final String SQL_UPDATE_PORTAFOLIO = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarNomina_UpdatePortafolio.sql");
	private final String SQL_SELECT_NOMINA = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarNomina_SelectNomina.sql");
	private final String SQL_SELECT_PLANTILLA = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarNomina_SelectPlantilla.sql");
	private final String SQL_SELECT_USUARIO = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarNomina_SelectUsuario.sql");
	private final String SQL_SELECT_NOMINA_REPROCESO = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarNomina_SelectNominaReproceso.sql");
	private final String SQL_SELECT_PORTAFOLIOS_NOMINA = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarNomina_SelectPortafoliosNomina.sql");
	private final String SQL_INSERT_CARTOLA = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarNomina_InsertCartola.sql");
	private final String SQL_UPDATE_CARTOLA_PADRE = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarNomina_UpdateCartolaPadre.sql");
	private final String SQL_SELECT_CARTOLA = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarNomina_SelectCartola.sql");
	private final String SQL_SELECT_ANOTACION = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarNomina_SelectAnotacionPdf.sql");
	
	public static class GestionarNominaEntrada {
	    public Long						p_IdEmpresa;
	    public Long						p_IdUsuario;
	    public String					p_IdModulo;
	    public String					p_AccionNomina;			//Ingresar, Certificar, Cancelar, Reprocesar, etc
	    public Long						p_IdNomina = 0L;
	    public String					p_TipoProceso;    	
	    public String					p_Periodo;
	    public String					p_FechaDesde;
	    public String					p_FechaHasta;
	    public Long						p_SegundosEsperaProceso;
	    public String					p_Comentario;
	    public String					p_Certificada;
	    public String					p_NotaCertificacion;
	    public String 					p_IncluirPortafoliosConSaldo;
	    public String					p_IncluirPortafoliosConCustodia;
	    public String					p_IncluirPortafoliosConMovimientoCuentaInversion;
	    public String 					p_IncluirCategoriaInversionista;
	    public String 					p_CategoriaInversionista;
	    public String 					p_IncluirTipoPersona;
	    public String 					p_TipoPersona;
	    public String					p_IncluirTipoCliente;
	    public String					p_TipoCliente;
	    public String					p_IncluirPortafoliosExcluidos;
	    public String					p_EnvioAutomatico;
	    public String					p_ExcluirPortafoliosIncluidosOtraNomina;
	    public String					p_ExcluirOperadoresDirectos;
	    public String					p_MensajeProceso;
	    public String 					p_OpcionProceso;
	    public String					p_CartolaConsolidada;
	    public Long						p_IdCartola; 			//Solo generacion OFFLINE
    	public List<ObjCliente>			p_Clientes;				//Solo generacion ONLINE
    	//Parametros programar nomina
    	public String 					p_Habilitado;
    	public String 					p_DiaEjecucion;
    	public String 					p_HoraEjecucion;
    	
    	//Parametros para descarga de anotaciones
    	public Long 					p_IdAnotacion;
    	public Long						p_IdCliente;
    	public byte[]					p_Archivo;
    	public String 					p_NombreArchivo;
    	
    	public GestionarNominaEntrada(){
    		this.p_Clientes = new ArrayList<>();
    		this.p_IncluirPortafoliosConSaldo = "N";
    		this.p_IncluirPortafoliosConCustodia = "N";
    		this.p_IncluirPortafoliosConMovimientoCuentaInversion = "N";
    		this.p_IncluirCategoriaInversionista = "N";
    		this.p_CategoriaInversionista = "";
    		this.p_IncluirTipoPersona = "N";
    		this.p_TipoPersona = "";
    		this.p_IncluirTipoCliente = "N";
    		this.p_TipoCliente = "";
    		this.p_IncluirPortafoliosExcluidos = "N";
    		this.p_EnvioAutomatico = "N";
    		this.p_ExcluirPortafoliosIncluidosOtraNomina = "N";
    		this.p_ExcluirOperadoresDirectos="N";
    		this.p_OpcionProceso = "";
    		this.p_Habilitado = "";
    		this.p_DiaEjecucion = "";
    		this.p_HoraEjecucion = "";
    		
    	};
    	public static class ObjFiltrosProceso {
    		public String 	NombreParametro;
    		public String 	Valor;
    	}
    	public static class ObjCliente {
		    public Long	 	IdCliente;
		    public Long	 	IdPortafolio;		    
	    }
	    public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class GestionarNominaSalida {
		public Long						P_OUT_IDNOMINA;
		public Long						P_OUT_IDPROGRAMA;
		public Long 					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public List<ObjCartolaSalida>	P_OUT_CARTOLAS;		//Solo generacion ONLINE
		public Long P_OUT_IDAGENDA;
		
		public GestionarNominaSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_CARTOLAS = new ArrayList<ObjCartolaSalida>();
		}
		public static class ObjCartolaSalida {
			public String 	FechaDesde;
		    public String 	FechaHasta;
		    public Long	 	IdCliente;
		    public Long	 	IdPortafolio;
		    public String	CartolaEncoded;
			public String 	NombreArchivo;
			
			//Parametros para descarga de anotaciones
			public Long	    IdAnotacion;
			public byte[]	Archivo;
			public String	ArchivoEncoded;
			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class Nomina{
		public Long IdNomina;
	    public Long IdEmpresa;
	    public Long IdUsuario;
	    public Long IdPlantilla;
		public String Periodo;
	    public Date FechaDesde;
	    public Date FechaHasta;
	    public String Comentario;
	    public String Certificada;
	    public Date FechaCertificacion;
	    public Long IdUsuarioCertificador;
	    public String NombreUsuarioCertificador;
	    public String NotaCertificacion;
	    public String TipoProceso;
	    public String EstadoProceso;
	    public String FechaProgramadaProceso;
	    public Timestamp FechaInicioProceso;
	    public Timestamp FechaTerminoProceso;
	    public String MensajeProceso;
	    public String Ano;
	    public String Mes;
	    public String IncluirPortafoliosConSaldo;
	    public String IncluirPortafoliosConCustodia;
	    public String IncluirPortafoliosConMovimientoCuentaInversion;
	    public String IncluirCategoriaInversionista;
	    public String CategoriaInversionista;
	    public String IncluirTipoPersona;
	    public String TipoPersona;
	    public String IncluirTipoCliente;
	    public String TipoCliente;
	    public String IncluirPortafoliosExcluidos;
	    public String EnvioAutomatico;
	    public String ExcluirPortafoliosIncluidosOtraNomina;
	    public String ExcluirOperadoresDirectos;
	    public String OpcionProceso;
	    public Long   IdNominaPadre;
	    public String CartolaConsolidada;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PlanillaNomina{
		
		public Long IdPlantilla;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Usuario{
		public String CodigoUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClientePortafolio{
		public Long IdCliente;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartola{
		public Long		IdNomina;
		public Long		IdCartola;
		public Long		IdCliente;
		public Long		IdPortafolio;
		public String 	MensajeProceso;
		public String 	EstadoProceso;
		public String 	Notificar;
		public String 	Reprocesar;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PortafolioCliente{
		public String FechaCertificacion;
		public Long IdNomina;
	}
			
	public GestionarNominaSalida logica(Connection con, GestionarNominaEntrada in) throws Exception {
		GestionarNominaSalida out = new GestionarNominaSalida();
			
		out.P_OUT_ERRORESCONTADOR = 0L;
		
		switch (in.p_AccionNomina) {		 
	        case "INGRESAR": 
	        	IngresarNomina(in, out, con); 
	        	break;	 
	        case "CERTIFICAR":
	        	CertificarNomina(in, out, con);
	        	break;	        
	        case "CANCELAR":
	        	CancelarNomina(in, out, con);
	        	break;
	        case "REPROCESAR":
	        	ReprocesarNomina(in, out, con);
	        	break;
	        case "PROGRAMADO":
	        	ProgramarNomina(in, out, con);
	        	break;
	        case "RECURRENTE":
	        	NominaRecurrente(in, out, con);
	        	break;
	        case "REEMPAQUETA":
	        	ReempaquetaNomina(in, out, con);
	        	break;
	        case "ONLINE":
	        	NominaOnline(in, out, con);
	        	break;
	        case "OFFLINE":
	        	NominaOffline(in, out, con);
	        	break;
	        case "INICIAR":
	        	InicioEjecucionNomina(in, out, con);
	        	break;
	        case "TERMINAR":
	        	TerminoEjecucionNomina(in, out, con);
	        	break;
	        case "ERROR":
	        	ErrorEjecucionNomina(in, out, con);
	        	break;
	        case "ANN":
	        	NominaOfflineAnn(in, out, con);
	        	break;
		}
		
		return out;		
	};
	
	
	private boolean IngresarNomina (GestionarNominaEntrada in, GestionarNominaSalida out, Connection con) throws Exception{		
		try {
			logger.info("Inicio del proceso ingresar nomina");
			//00 VARIABLES DE SISTEMA
			String FechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");

			//01 GRABAR NOMINA
			out.P_OUT_IDNOMINA = GrabarNomina(con, in); 
								
			//02 INSCRIBIR PROGRAMAS
			List<AgregarPrograma.InAgregarPrograma.Parametro> paramsParametros = new ArrayList<AgregarPrograma.InAgregarPrograma.Parametro>();
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramIdEmpresa = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramIdEmpresa.NombreParametro 	= "IdEmpresa";
			paramIdEmpresa.Valor			= in.p_IdEmpresa.toString();
			paramsParametros.add(paramIdEmpresa);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramIdUsuario = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramIdUsuario.NombreParametro 	= "IdUsuario";
			paramIdUsuario.Valor			= in.p_IdUsuario.toString();
			paramsParametros.add(paramIdUsuario);
			
			AgregarPrograma.InAgregarPrograma.Parametro	paramIdNomina = new AgregarPrograma.InAgregarPrograma.Parametro();
			paramIdNomina.NombreParametro 	= "IdNomina";
			paramIdNomina.Valor				= out.P_OUT_IDNOMINA.toString();
			paramsParametros.add(paramIdNomina);
			
			AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
			paramsIn.p_IdEmpresa		= in.p_IdEmpresa;
			paramsIn.p_IdModulo			= in.p_IdModulo;
			paramsIn.p_IdUsuario		= in.p_IdUsuario;
			paramsIn.p_IdTipoPrograma	= "ProcesoCartolas";
			paramsIn.p_Segundos			= in.p_SegundosEsperaProceso;
			paramsIn.p_FechaSistema		= FechaSistema; 
			paramsIn.p_Parametros		= paramsParametros;
			AgregarPrograma AP = new AgregarPrograma();
			AgregarPrograma.OutAgregarPrograma result = AP.logica(con, paramsIn);
			
			out.P_OUT_IDPROGRAMA		= result.P_OUT_IDPROGRAMA;
			out.P_OUT_ERRORESCONTADOR 	= result.P_OUT_ERRORESCONTADOR;
			if ( out.P_OUT_ERRORESCONTADOR.equals(1L) ){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, result.P_OUT_MENSAJE, new HashMap<>() ,con, in.p_IdEmpresa));
			} else {
				
				out.P_OUT_MENSAJES.add(new MensajeSalida(out.P_OUT_IDNOMINA ,MensajeSalida.MENSAJE_INFORMACION, "NOMINA_INGRESADA", new HashMap<String,String>() ,con,in.p_IdEmpresa));
			};
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.info("== ERROR ==",e);
			throw e;
		}		
		return true;
	};
	
	private boolean CertificarNomina (GestionarNominaEntrada in, GestionarNominaSalida out, Connection con) throws Exception{
		try {
			logger.info("Inicio del proceso certificar nomina");
			SQL sql = new SQL();
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			List<Nomina> nominas = new ArrayList<Nomina>();
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdNomina", in.p_IdNomina);
			List<Nomina> nominasExistentes = sql.query(con, SQL_SELECT_NOMINA, params, Nomina.class);
			params.clear();
			params.put("IdUsuario", in.p_IdUsuario);
			List<Usuario> usuarios = sql.query(con, SQL_SELECT_USUARIO, params, Usuario.class);
			if(nominasExistentes.size() > 0){
				Nomina nomina = nominasExistentes.get(0);
				nomina.Certificada = "S";
				nomina.IdUsuarioCertificador = in.p_IdUsuario;
				nomina.NombreUsuarioCertificador = usuarios.size() > 0 ? usuarios.get(0).CodigoUsuario : "";
				nomina.NotaCertificacion = in.p_NotaCertificacion;
				nomina.FechaCertificacion = Fecha.toDate(fechaSistema);
				nominas.add(nomina);
				sql.batch(con, SQL_UPDATE_NOMINA, nominas, Nomina.class);
				
				//UPDATE PORTAFOLIO
				sql.batch(con, SQL_UPDATE_PORTAFOLIO, nominas, Nomina.class);
			}
			
			out.P_OUT_MENSAJES.add(new MensajeSalida(out.P_OUT_IDNOMINA ,MensajeSalida.MENSAJE_INFORMACION, "NOMINA_CERTIFICADA", new HashMap<String,String>() ,con,in.p_IdEmpresa));
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("== ERROR ==",e);
			throw e;
		}
		return true;
	};
	
	private boolean CancelarNomina (GestionarNominaEntrada in, GestionarNominaSalida out, Connection con) throws Exception{
		try {
			logger.info("Inicio del proceso cancelar nomina");
			SQL sql = new SQL();
			List<Nomina> nominas = new ArrayList<Nomina>();
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdNomina", in.p_IdNomina);
			List<Nomina> nominasExistentes = sql.query(con, SQL_SELECT_NOMINA, params, Nomina.class);
			params.clear();
			if(nominasExistentes.size() > 0){
				Nomina nomina = nominasExistentes.get(0);
				nomina.EstadoProceso = "CAN";
				nominas.add(nomina);
				sql.batch(con, SQL_UPDATE_NOMINA, nominas, Nomina.class);
			}
			
			out.P_OUT_MENSAJES.add(new MensajeSalida(out.P_OUT_IDNOMINA ,MensajeSalida.MENSAJE_INFORMACION, "NOMINA_CANCELADA", new HashMap<String,String>() ,con,in.p_IdEmpresa));
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("== ERROR ==",e);
			throw e;
		}
		return true;
	};
	
	
	public String validarFecha(Connection con, Date fechaOperacion,Long idEmpresa) throws Exception {
		ConsultarEmpresa consultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa infoEmpresa = consultarEmpresa.logica(con, idEmpresa);
		String UltimoDiaDelMes = obtenerUltimoDiaDelMesString(fechaOperacion.toString());
		Date UltimoDiaDelMesDate = Fecha.toDate(UltimoDiaDelMes);
		Boolean FechaHastaEsDiaHabil = Feriados.esDiaHabil(con, fechaOperacion.toString(), infoEmpresa.IdPais);
		
		if (FechaHastaEsDiaHabil) {
			return fechaOperacion.toString();
		}
		else if(FechaHastaEsDiaHabil == false && fechaOperacion.compareTo(UltimoDiaDelMesDate) == 0){
			return fechaOperacion.toString();
		}
		
		ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
		InObtenerDiaAnterior entradaObtenerDiaAnterior = new InObtenerDiaAnterior();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		entradaObtenerDiaAnterior.soloCaeDiaHabil = "S";
		entradaObtenerDiaAnterior.diaHabil = "S";
		entradaObtenerDiaAnterior.idPais = infoEmpresa.IdPais;

		InObtenerDiaAnterior.FechasIn fecha = new InObtenerDiaAnterior.FechasIn();
		fecha.fechaInicial = dateFormat.format(fechaOperacion);
		fecha.numeroDias = 1L;
		
		entradaObtenerDiaAnterior.fechas.add(fecha);
		
		OutObtenerDiaAnterior salidaObtenerDiaAnterior = obtenerDiaAnterior.logica(con, entradaObtenerDiaAnterior);
		String fechaHabilAnterior = salidaObtenerDiaAnterior.fechas.get(0).fechaCalculada;
		
		return fechaHabilAnterior;
	}
	public LocalDate obtenerUltimoDiaDelMes(LocalDate fecha) {	        
	        LocalDate ultimoDiaDelMes = fecha.withDayOfMonth(fecha.lengthOfMonth());
	        return ultimoDiaDelMes;
	}
	 
	public String obtenerUltimoDiaDelMesString(String fecha) {	  
		 
		    String [] FechaCalculada = fecha.split("-");	
		 	Integer Agno = Integer.parseInt(FechaCalculada[0]);
			Integer Mes = Integer.parseInt(FechaCalculada[1]);
			Integer Dia = Integer.parseInt(FechaCalculada[2]);
			
			LocalDate ultimoDia =this.obtenerUltimoDiaDelMes(LocalDate.of( Agno, Mes, Dia));
	        return ultimoDia.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); 
	 }

	
	private boolean ReprocesarNomina (GestionarNominaEntrada in, GestionarNominaSalida out, Connection con) throws Exception{
		try {
			logger.debug("Inicio del proceso reprocesar nomina");
			//00 VARIABLES DE SISTEMA
			String FechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");
			SQL sql = new SQL();
			List<Nomina> nominas = new ArrayList<Nomina>();
			List<Cartola> cartolas = new ArrayList<Cartola>();
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdNomina", in.p_IdNomina);
			List<Nomina> nominasExistentes = sql.query(con, SQL_SELECT_NOMINA_REPROCESO, params, Nomina.class);
			List<ClientePortafolio> portafoliosValidos = sql.query(con, SQL_SELECT_PORTAFOLIOS_NOMINA, params, ClientePortafolio.class);
			params.clear();
			List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Normativa.BD.Secuencias::CAR.Global", 1L);
			if(nominasExistentes.size() > 0){
				//01 COPIA NOMINA
				Nomina nomina = nominasExistentes.get(0);
				nomina.IdNomina = secuencias.get(0);
				nomina.IdEmpresa = in.p_IdEmpresa;
				nomina.IdUsuario = in.p_IdUsuario;
				nomina.Comentario = in.p_Comentario;
				nomina.TipoProceso = "REPROCESO";
				nomina.Certificada = "N";
				nomina.IdUsuarioCertificador = 0L;
				nomina.NombreUsuarioCertificador = "";
				nomina.NotaCertificacion = "";
				nomina.EstadoProceso = "ING";
				nomina.FechaProgramadaProceso = in.p_SegundosEsperaProceso != null ? Fecha.toStringDatetime(Fecha.addSeconds(FechaDB.ObtenerFechaHoraTs(con, in.p_IdEmpresa),in.p_SegundosEsperaProceso.intValue())) : null;
				nomina.FechaInicioProceso = Fecha.toTimeStamp("0001-01-01 00:00:00");
				nomina.FechaTerminoProceso = Fecha.toTimeStamp("0001-01-01 00:00:00");
				nomina.MensajeProceso = "";
				nomina.IdNominaPadre = in.p_IdNomina;
				nomina.CartolaConsolidada = in.p_CartolaConsolidada;
				nominas.add(nomina);
				
				sql.batch(con, SQL_INSERT_NOMINA, nominas, Nomina.class);
				
				for(ClientePortafolio portafolio : portafoliosValidos){
					Cartola cartola = new Cartola();
					cartola.IdNomina		= nomina.IdNomina;
					cartola.IdCliente		= portafolio.IdCliente;
					cartola.IdPortafolio 	= portafolio.IdPortafolio;
					cartola.EstadoProceso	= "PEN";
					cartola.MensajeProceso 	= "";
					cartolas.add(cartola);
				}
				
				sql.batch(con, SQL_INSERT_CARTOLA, cartolas, Cartola.class);
				sql.batch(con, SQL_UPDATE_CARTOLA_PADRE, nominas, Nomina.class);
				
				out.P_OUT_IDNOMINA = nomina.IdNomina;
				
				//02 INSCRIBIR PROGRAMA
				List<AgregarPrograma.InAgregarPrograma.Parametro> paramsParametros = new ArrayList<AgregarPrograma.InAgregarPrograma.Parametro>();
				
				AgregarPrograma.InAgregarPrograma.Parametro	paramIdEmpresa = new AgregarPrograma.InAgregarPrograma.Parametro();
				paramIdEmpresa.NombreParametro 	= "IdEmpresa";
				paramIdEmpresa.Valor			= in.p_IdEmpresa.toString();
				paramsParametros.add(paramIdEmpresa);
				
				AgregarPrograma.InAgregarPrograma.Parametro	paramIdUsuario = new AgregarPrograma.InAgregarPrograma.Parametro();
				paramIdUsuario.NombreParametro 	= "IdUsuario";
				paramIdUsuario.Valor			= in.p_IdUsuario.toString();
				paramsParametros.add(paramIdUsuario);
				
				AgregarPrograma.InAgregarPrograma.Parametro	paramIdNomina = new AgregarPrograma.InAgregarPrograma.Parametro();
				paramIdNomina.NombreParametro 	= "IdNomina";
				paramIdNomina.Valor				= out.P_OUT_IDNOMINA.toString();
				paramsParametros.add(paramIdNomina);
				
				AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
				paramsIn.p_IdEmpresa		= in.p_IdEmpresa;
				paramsIn.p_IdModulo			= in.p_IdModulo;
				paramsIn.p_IdUsuario		= in.p_IdUsuario;
				paramsIn.p_IdTipoPrograma	= "ProcesoCartolas";
				paramsIn.p_Segundos			= in.p_SegundosEsperaProceso;
				paramsIn.p_FechaSistema		= FechaSistema; 
				paramsIn.p_Parametros		= paramsParametros;
				AgregarPrograma AP = new AgregarPrograma();
				AgregarPrograma.OutAgregarPrograma result = AP.logica(con, paramsIn);
				
				out.P_OUT_IDPROGRAMA		= result.P_OUT_IDPROGRAMA;
				out.P_OUT_ERRORESCONTADOR 	= result.P_OUT_ERRORESCONTADOR;
				if ( out.P_OUT_ERRORESCONTADOR.equals(1L) ){
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, result.P_OUT_MENSAJE, new HashMap<>() ,con, in.p_IdEmpresa));
				} else {
					out.P_OUT_MENSAJES.add(new MensajeSalida(out.P_OUT_IDNOMINA ,MensajeSalida.MENSAJE_INFORMACION, "NOMINA_REPROCESADA", new HashMap<String,String>() ,con,in.p_IdEmpresa));
				};
			}
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("== ERROR ==",e);
			throw e;
		}
		return true;
	};
	
	private boolean ProgramarNomina (GestionarNominaEntrada in, GestionarNominaSalida out, Connection con) throws Exception{
		try {
			//01 VARIABLES DE SISTEMA
			//02 INSCRIBIR AGENDA
			List<AgregarAgenda.TablaParametros> paramsAgenda = new ArrayList<>();
			
			//agregar parametros de la nomina
			AgregarAgenda.TablaParametros param = new AgregarAgenda.TablaParametros();
			param.NombreParametro 	= "IdEmpresa";
			param.Valor			= in.p_IdEmpresa.toString();
			paramsAgenda.add(param);
			param = new AgregarAgenda.TablaParametros();
			param.NombreParametro 	= "IdUsuario";
			param.Valor			= in.p_IdUsuario.toString();
			paramsAgenda.add(param);
			param = new AgregarAgenda.TablaParametros();
			param.NombreParametro 	= "IdModulo";
			param.Valor			= in.p_IdModulo.toString();
			paramsAgenda.add(param);
			
			param = new AgregarAgenda.TablaParametros();
			param.NombreParametro 	= "ConfiguracionNomina";
			param.Valor			= in.toString();
			paramsAgenda.add(param);
			
			//obtener hora de ejecucion
			LocalTime horaEjecucion = LocalTime.parse(in.p_HoraEjecucion, DateTimeFormatter.ofPattern("HH:mm"));
			
			//agendar generacion de cartolas
			AgregarAgenda.EntradaAgregarAgenda paramsIn = new AgregarAgenda.EntradaAgregarAgenda(); 
			paramsIn.p_IdEmpresa		= in.p_IdEmpresa;
			paramsIn.p_IdModulo			= in.p_IdModulo;
			paramsIn.p_IdUsuario		= in.p_IdUsuario;

			paramsIn.p_IdTipoTarea = "PROGRAMAR_CARTOLAS";
			paramsIn.p_EsDemonio    = "N";
			paramsIn.p_DiaDeMes = in.p_DiaEjecucion;
			paramsIn.p_DiaDeSemana = "*";
			paramsIn.p_Mes = "**";
			paramsIn.p_Hora = horaEjecucion.format(DateTimeFormatter.ofPattern("HH"));
			paramsIn.p_Minuto = horaEjecucion.format(DateTimeFormatter.ofPattern("mm"));
			paramsIn.p_SoloDiasHabiales = "S";
			paramsIn.p_SoloPrincipioMes = "N";
			paramsIn.p_SoloFinDeMes = "N";
			paramsIn.p_habilitado = "S";
			
			paramsIn.p_TablaParametros		= paramsAgenda;
			AgregarAgenda agregarAgenda = new AgregarAgenda();
			AgregarAgenda.SalidaAgregarAgenda result = agregarAgenda.logica(con, paramsIn);
			
			out.P_OUT_IDAGENDA 			= result.p_IdAgenda;
			out.P_OUT_IDPROGRAMA		= result.p_IdPrograma;
			out.P_OUT_ERRORESCONTADOR 	= result.P_OUT_ERRORESCONTADOR;
			
			if ( !out.P_OUT_ERRORESCONTADOR.equals(0L) ){
				for(ErrorDetalle error : result.P_OUT_ERRORESDETALLE) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L ,MensajeSalida.MENSAJE_ERROR, error.Descripcion, new HashMap<>() ,con, in.p_IdEmpresa));	
				}
			} else {				
				out.P_OUT_MENSAJES.add(new MensajeSalida(out.P_OUT_IDNOMINA ,MensajeSalida.MENSAJE_INFORMACION, "NOMINA_PROGRAMADA", new HashMap<String,String>() ,con,in.p_IdEmpresa));
			}
			System.out.println(out);
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("== ERROR ==",e);
			throw e;
		}
		return true;
	};
	
	private boolean NominaRecurrente (GestionarNominaEntrada in, GestionarNominaSalida out, Connection con) throws Exception{
		try {
			//TODO: REVISAR Q HACE
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("== ERROR ==",e);
			throw e;
		}
		return true;
	};
	
	private boolean ReempaquetaNomina (GestionarNominaEntrada in, GestionarNominaSalida out, Connection con) throws Exception{
		try {
			//TODO: REVISAR Q HACE
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("== ERROR ==",e);
			throw e;
		}
		return true;
	};
	
	private boolean NominaOnline (GestionarNominaEntrada in, GestionarNominaSalida out, Connection con) throws Exception{
		try {
			in.p_IdNomina = GrabarNomina(con, in);
			
			List<GenerarCartola.GenerarCartolaEntrada.objDatosEntrada> datosEntrada = new ArrayList<GenerarCartola.GenerarCartolaEntrada.objDatosEntrada>();
			for (GestionarNomina.GestionarNominaEntrada.ObjCliente inCli : in.p_Clientes){
				GenerarCartola.GenerarCartolaEntrada.objDatosEntrada datos = new GenerarCartola.GenerarCartolaEntrada.objDatosEntrada();
				Utils.copyPojo(inCli, datos);
				datosEntrada.add(datos);
			};
			
			String fechaHastaFix = validarFecha(con,parseFecha(in.p_FechaHasta), in.p_IdEmpresa);
					
			GenerarCartola.GenerarCartolaEntrada VIE = new GenerarCartola.GenerarCartolaEntrada();
			VIE.p_IdEmpresa		= in.p_IdEmpresa;
			VIE.p_IdUsuario		= in.p_IdUsuario;
			VIE.p_IdModulo		= in.p_IdModulo;
			VIE.p_FechaDesde	= in.p_FechaDesde;
			VIE.p_FechaHasta	= fechaHastaFix;
			VIE.p_IdNomina		= in.p_IdNomina;
			VIE.p_TipoProceso   = in.p_TipoProceso;
			VIE.p_CartolaConsolidada	= in.p_CartolaConsolidada;
			VIE.p_Clientes		= datosEntrada;
			VIE.p_FechaHastaOrigen		= fechaHastaFix;
			GenerarCartola GCE = new GenerarCartola();
			GenerarCartola.GenerarCartolaSalida VIS = GCE.logica(con, VIE);			
			
			if ( VIS.P_OUT_ERRORESCONTADOR.equals(0L) ){
				for (GenerarCartola.ObjCartolaSalida infoCartola : VIS.P_OUT_CARTOLAS){
					GestionarNomina.GestionarNominaSalida.ObjCartolaSalida cartola = new GestionarNomina.GestionarNominaSalida.ObjCartolaSalida();
					Utils.copyPojo(infoCartola, cartola);
					out.P_OUT_CARTOLAS.add(cartola);
				};

				TerminoEjecucionNomina(in, out, con);
			} else {
				ErrorEjecucionNomina(in, out, con);
			};
			
			out.P_OUT_IDNOMINA = in.p_IdNomina;
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("== ERROR ==",e);
			throw e;
		}
		return true;
	};
	
	
	
	
	  public static Date parseFecha(String fechaString) {
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	        LocalDate fecha = LocalDate.parse(fechaString, formatter);
	        return Date.valueOf(fecha);
	    }
	
	
	private boolean NominaOffline(GestionarNominaEntrada in, GestionarNominaSalida out, Connection con) throws Exception{
		try{
			//Se realiza de esta manera dado que el SQL query no aguanta Blob
			PreparedStatement st = con.prepareStatement(SQL_SELECT_CARTOLA);
			st.setLong(1, in.p_IdCartola);
			if(st.execute())
			{
				ResultSet rs = st.getResultSet();
				while (rs.next()) {
					GestionarNominaSalida.ObjCartolaSalida cartola = new GestionarNominaSalida.ObjCartolaSalida();
					cartola.FechaDesde = rs.getString("FechaDesde");
					cartola.FechaHasta = rs.getString("FechaHasta");
					cartola.IdCliente = rs.getLong("IdCliente");
					cartola.IdPortafolio = rs.getLong("IdPortafolio");
					cartola.NombreArchivo = rs.getString("NombreArchivo");
					String origen = rs.getString("Origen");
					
					if(origen!=null && origen.equalsIgnoreCase("sebra")) {
						String rutaBase = (DEBUG)? "C:\\BCS\\CAR_HIS\\" : ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos")+"PLA/CAR_HIS/";
						String rutaCompleta = rutaBase+cartola.NombreArchivo;
						System.out.println(rutaCompleta);
						File file = FileUtils.getFile(rutaCompleta);
						if(file.exists()) {
							cartola.CartolaEncoded = new String(Base64.encodeBase64( FileUtils.readFileToByteArray(file) ));	
						}else {
							throw new IOException("Archivo no existe");
						}
					} else if(origen!=null && origen.equalsIgnoreCase("sebraOnline")) {
						try {
						cartola.CartolaEncoded = obtenerCartolaDesdeSebra(con, in.p_IdEmpresa, cartola);
						}
						catch (Exception e) {
							throw new Exception("Error al obtener cartola de cliente");
						}
						
					} else {
						// Descomprimir y cifrar CartolaEncoded
					    byte[] cartolaInflater = descomprimir(rs.getBytes("CartolaEncoded"));
						cartola.CartolaEncoded = new String(Base64.encodeBase64(cartolaInflater));
					}
					
					out.P_OUT_CARTOLAS.add(cartola);
				}
			}
			out.P_OUT_IDNOMINA = in.p_IdNomina;
		}
		catch(Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("== ERROR ==",e);
			throw e;
		}
		return true;
	}
	
	
	private String obtenerCartolaDesdeSebra(Connection con, long IdEmpresa,GestionarNominaSalida.ObjCartolaSalida cartola) throws Exception {

	    // (DEBUG) Usar localhost o ruta real según entorno
	    //String rutaBase = "http://localhost:8045/BcsCartolaSebraCapital/CartolaServlet";
	    String rutaBase = ConfiguracionSistema.getParametroConfiguracionString(con, IdEmpresa, "PLATAFORMA", "PLA.URLServletCartolas");;

	    String[] partes = cartola.NombreArchivo.split("_");
	    if (partes.length < 3) {
	        throw new IllegalArgumentException(
	            "Formato de NombreArchivo inválido: " + cartola.NombreArchivo
	        );
	    }
	    String rut     = partes[0];
	    String cuenta  = partes[1];
	    String periodo = partes[2];

	    HttpURLConnection conUrl = null;
	    try {
	        String queryString = "?rut="    + URLEncoder.encode(rut,     "UTF-8")
	                           + "&cuenta=" + URLEncoder.encode(cuenta,  "UTF-8")
	                           + "&codPer=" + URLEncoder.encode(periodo, "UTF-8");

	        URL url = new URL(rutaBase + queryString);
	        conUrl = (HttpURLConnection) url.openConnection();
	        conUrl.setRequestMethod("GET");
	        conUrl.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

	        conUrl.setConnectTimeout(5_000);   // 5 segundos para conectar
	        conUrl.setReadTimeout(15_000);     // 15 segundos para leer respuesta

	        int codigoRespuesta = conUrl.getResponseCode();

	        if (codigoRespuesta == HttpURLConnection.HTTP_OK) {
	            try (BufferedInputStream inputStream = new BufferedInputStream(conUrl.getInputStream());
	                 ByteArrayOutputStream bufferSalida = new ByteArrayOutputStream()) {

	                byte[] dataBuffer = new byte[4096];
	                int bytesLeidos;
	                while ((bytesLeidos = inputStream.read(dataBuffer, 0, dataBuffer.length)) != -1) {
	                    bufferSalida.write(dataBuffer, 0, bytesLeidos);
	                }

	                return new String(Base64.encodeBase64(bufferSalida.toByteArray()));
	            }
	        } else {
	            throw new IOException("El servidor respondió con código HTTP: " + codigoRespuesta);
	        }

	    } catch (IOException e) {
	        throw new IOException("Error al obtener archivo desde servidor legacy: " + e.getMessage(), e);
	    } finally {
	        if (conUrl != null) {
	            conUrl.disconnect();
	        }
	    }
	}
	
	//prueba para anotaciones
	private boolean NominaOfflineAnn(GestionarNominaEntrada in, GestionarNominaSalida out, Connection con) throws Exception{
		try{
			//Se realiza de esta manera dado que el SQL query no aguanta Blob
			PreparedStatement st = con.prepareStatement(SQL_SELECT_ANOTACION);
			st.setLong(1, in.p_IdAnotacion);
			if(st.execute())
			{
				ResultSet rs = st.getResultSet();
				while (rs.next()) {
					GestionarNominaSalida.ObjCartolaSalida anotacion = new GestionarNominaSalida.ObjCartolaSalida();
					anotacion.IdAnotacion = rs.getLong("IdAnotacion");
					anotacion.IdCliente = rs.getLong("IdCliente");
					anotacion.Archivo = rs.getBytes("Archivo");
					anotacion.NombreArchivo = rs.getString("NombreArchivo");
				    anotacion.ArchivoEncoded = new String(Base64.encodeBase64(anotacion.Archivo));		
					
					out.P_OUT_CARTOLAS.add(anotacion);				
				}
			}
		}
		catch(Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.debug("== ERROR ==",e);
			throw e;
		}
		return true;
	}
	
	private byte[] descomprimir (byte[] data) throws IOException, DataFormatException {
		Inflater inflater = new Inflater();
		inflater.setInput(data);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		byte[] buffer = new byte [1024];
		while(!inflater.finished()) {
			int count = inflater.inflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		
		return outputStream.toByteArray();
	}
	
	private boolean InicioEjecucionNomina (GestionarNominaEntrada in, GestionarNominaSalida out, Connection con) throws Exception{
		try {
			logger.debug("Inicio del proceso de la nomina, cambio de estado de iniciar a en proceso");
			SQL sql = new SQL();
			List<Nomina> nominas = new ArrayList<Nomina>();
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdNomina", in.p_IdNomina);
			List<Nomina> nominasExistentes = sql.query(con, SQL_SELECT_NOMINA, params, Nomina.class);
			if(nominasExistentes.size() > 0){
				Nomina nomina = nominasExistentes.get(0);
				nomina.EstadoProceso = "EPR";
				nomina.FechaInicioProceso = FechaDB.ObtenerFechaHoraTs(con, in.p_IdEmpresa);
				nomina.MensajeProceso = in.p_MensajeProceso;
				nominas.add(nomina);
				sql.batch(con, SQL_UPDATE_NOMINA, nominas, Nomina.class);
			}
			
			out.P_OUT_MENSAJES.add(new MensajeSalida(out.P_OUT_IDNOMINA ,MensajeSalida.MENSAJE_INFORMACION, Diccionario.getMensaje(con, in.p_IdEmpresa, "NOMINA_INGRESADA").add("IdNomina", in.p_IdNomina.toString()).msg(), new HashMap<>() ,con,in.p_IdEmpresa));
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("== ERROR ==",e);
			throw e;
		}
		return true;
	} 
	
	private boolean TerminoEjecucionNomina (GestionarNominaEntrada in, GestionarNominaSalida out, Connection con) throws Exception{
		try {
			logger.debug("inicio del proceso terminar");
			SQL sql = new SQL();
			List<Nomina> nominas = new ArrayList<Nomina>();
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdNomina", in.p_IdNomina);
			List<Nomina> nominasExistentes = sql.query(con, SQL_SELECT_NOMINA, params, Nomina.class);
			if(nominasExistentes.size() > 0){
				Nomina nomina = nominasExistentes.get(0);
				nomina.EstadoProceso = "TER";
				nomina.FechaTerminoProceso = FechaDB.ObtenerFechaHoraTs(con, in.p_IdEmpresa);
				nomina.MensajeProceso = in.p_MensajeProceso;
				nominas.add(nomina);
				sql.batch(con, SQL_UPDATE_NOMINA, nominas, Nomina.class);
				logger.debug("ACTUALIZAR LOS PORTAFOLIOS DE LOS CLIENTES CON LA ULTIMA CARTOLA GENERADA y SU ID");
				//ACTUALIZAR LOS PORTAFOLIOS DE LOS CLIENTES CON LA ULTIMA CARTOLA GENERADA y SU ID
				PortafolioCliente portafolio = new PortafolioCliente();
				portafolio.FechaCertificacion = Utils.getFechaSistema(con, Utils.getSchema(), in.p_IdEmpresa);
				portafolio.IdNomina = nomina.IdNomina;
				sql.batch(con, SQL_UPDATE_PORTAFOLIO, Arrays.asList(portafolio), PortafolioCliente.class);
				
			}
			
			out.P_OUT_MENSAJES.add(new MensajeSalida(out.P_OUT_IDNOMINA ,MensajeSalida.MENSAJE_INFORMACION, Diccionario.getMensaje(con, in.p_IdEmpresa, "NOMINA_TERMINADA").add("IdNomina", in.p_IdNomina.toString()).msg(), new HashMap<>() ,con,in.p_IdEmpresa));
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("== ERROR ==",e);
			throw e;
		}
		return true;
	}
	
	private boolean ErrorEjecucionNomina (GestionarNominaEntrada in, GestionarNominaSalida out, Connection con) throws Exception{
		try {
			logger.debug("inicio del proceso de ErrorejecucionNomina");
			SQL sql = new SQL();
			List<Nomina> nominas = new ArrayList<Nomina>();
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdNomina", in.p_IdNomina);
			List<Nomina> nominasExistentes = sql.query(con, SQL_SELECT_NOMINA, params, Nomina.class);
			if(nominasExistentes.size() > 0){
				Nomina nomina = nominasExistentes.get(0);
				nomina.EstadoProceso = "ERR";
				nomina.FechaTerminoProceso = FechaDB.ObtenerFechaHoraTs(con, in.p_IdEmpresa);
				nomina.MensajeProceso = in.p_MensajeProceso;
				nominas.add(nomina);
				sql.batch(con, SQL_UPDATE_NOMINA, nominas, Nomina.class);
			}
			
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(out.P_OUT_IDNOMINA ,MensajeSalida.MENSAJE_ERROR, Diccionario.getMensaje(con, in.p_IdEmpresa, "NOMINA_ERROR").add("IdNomina", in.p_IdNomina.toString()).msg(), new HashMap<>() ,con,in.p_IdEmpresa));
		}
		catch(Exception e) {			
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("== ERROR ==",e);
			throw e;
		}
		return true;
	};
	
	private Long GrabarNomina(Connection con, GestionarNominaEntrada in) throws Exception{
		SQL sql = new SQL();
		logger.debug("Inicio del proceso grabar Nomina");
		String fechaDesde = in.p_FechaDesde;
		String fechaHasta = in.p_FechaHasta;
		if (in.p_Periodo != null){
			Calendar c = Calendar.getInstance();   // this takes current date
		    c.set(Calendar.YEAR,  Integer.parseInt(in.p_Periodo.substring(0, 4)) );
		    c.set(Calendar.MONTH, Integer.parseInt(in.p_Periodo.substring(5, 7))-1 );
		    
		    c.set(Calendar.DAY_OF_MONTH, 1);
		    java.sql.Date primero = new java.sql.Date(c.getTime().getTime());		    
		    fechaDesde = primero.toString();
		    
		    c.add(Calendar.MONTH, 1);
		    c.add(Calendar.DAY_OF_MONTH, -1);
		    java.sql.Date ultimo = new java.sql.Date(c.getTime().getTime());
		    fechaHasta = ultimo.toString();
		};

		List<Nomina> nominas = new ArrayList<Nomina>();
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Normativa.BD.Secuencias::CAR.Global", 1L);
		List<PlanillaNomina> plantillas = sql.query(con, SQL_SELECT_PLANTILLA, new HashMap<String,Object>(), PlanillaNomina.class);
		Nomina nomina = new Nomina();
		nomina.IdNomina = secuencias.get(0);
		nomina.IdEmpresa = in.p_IdEmpresa;
		nomina.IdUsuario = in.p_IdUsuario;
		nomina.IdPlantilla = plantillas.size() > 0 ? plantillas.get(0).IdPlantilla : 0L;
		nomina.Periodo = in.p_Periodo;
		nomina.FechaDesde = Fecha.toDate(fechaDesde);
		nomina.FechaHasta = Fecha.toDate(fechaHasta);
		nomina.Comentario = in.p_Comentario;
		nomina.Certificada = "N";
		nomina.IdUsuarioCertificador = 0L;
		nomina.NombreUsuarioCertificador = "";
		nomina.NotaCertificacion = "";
		nomina.TipoProceso = in.p_TipoProceso;
		nomina.EstadoProceso = "ING";
		nomina.FechaProgramadaProceso = in.p_SegundosEsperaProceso != null ? Fecha.toStringDatetime(Fecha.addSeconds(FechaDB.ObtenerFechaHoraTs(con, in.p_IdEmpresa),in.p_SegundosEsperaProceso.intValue())) : null;
		nomina.FechaInicioProceso = Fecha.toTimeStamp("0001-01-01 00:00:00");
		nomina.FechaTerminoProceso = Fecha.toTimeStamp("0001-01-01 00:00:00");
		nomina.MensajeProceso = "";
		nomina.Ano = in.p_Periodo != null ? in.p_Periodo.substring(0, 4) : "";
		nomina.Mes = in.p_Periodo != null ? in.p_Periodo.substring(5, 7) : "";
		nomina.IncluirPortafoliosConSaldo = in.p_IncluirPortafoliosConSaldo;
		nomina.IncluirPortafoliosConCustodia = in.p_IncluirPortafoliosConCustodia;
		nomina.IncluirPortafoliosConMovimientoCuentaInversion = in.p_IncluirPortafoliosConMovimientoCuentaInversion;
		nomina.IncluirCategoriaInversionista = in.p_IncluirCategoriaInversionista;
		nomina.CategoriaInversionista = in.p_CategoriaInversionista;
		nomina.IncluirTipoPersona = in.p_IncluirTipoPersona;
		nomina.TipoPersona = in.p_TipoPersona;
		nomina.IncluirTipoCliente = in.p_IncluirTipoCliente;
		nomina.TipoCliente = in.p_TipoCliente;
		nomina.IncluirPortafoliosExcluidos = in.p_IncluirPortafoliosExcluidos;
		nomina.EnvioAutomatico = in.p_EnvioAutomatico;
		nomina.ExcluirPortafoliosIncluidosOtraNomina = in.p_ExcluirPortafoliosIncluidosOtraNomina;
		nomina.ExcluirOperadoresDirectos = in.p_ExcluirOperadoresDirectos;
		nomina.OpcionProceso = in.p_OpcionProceso;
		nomina.IdNominaPadre = 0L;
		nomina.CartolaConsolidada = in.p_CartolaConsolidada;
		nominas.add(nomina);
		
		sql.batch(con, SQL_INSERT_NOMINA, nominas, Nomina.class);
		
		return nomina.IdNomina;
			
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();	
		GestionarNominaEntrada jin=gson.fromJson(in.toString() ,GestionarNominaEntrada.class);
		GestionarNominaSalida out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {

		try (Connection con = PlataformaUtils.getPropertiesConnection("scotiapre","connections.properties")) {

			try {
				Gson gson = Utils.getGsonBuilder();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6408,\"p_IdCartola\":1100483,\"p_AccionNomina\":\"OFFLINE\"}";				
			    GestionarNominaEntrada Objin = gson.fromJson(json, GestionarNominaEntrada.class);
				GestionarNomina  kk = new GestionarNomina();
				kk.DEBUG = true;
				System.out.println(kk.logica(con, Objin));
				con.rollback();				
				
				System.out.println("---- FIN GestionarNomina ------");
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();					
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
}
