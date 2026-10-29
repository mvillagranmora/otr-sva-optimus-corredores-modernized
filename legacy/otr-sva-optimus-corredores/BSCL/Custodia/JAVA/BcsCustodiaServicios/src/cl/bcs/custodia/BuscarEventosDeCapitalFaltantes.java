package cl.bcs.custodia;

/********************************
 * 
 * Objetivo : Compara los eventos capital cargados en ruebas y con los de optimus para obtener
 *            aquellos que no se encuentran cargados en optimus
 * Aiutor   : - BCS
 * Fecha    : 20/11/2020
 ********************************/


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.FileInputStream;
import java.io.InputStream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.custodia.CargarEventoCapitalDesdeBursatil.RegistroAInsertar;


public class BuscarEventosDeCapitalFaltantes implements JavaCall {	
		
	static final Logger logger = LogManager.getLogger(BuscarEventosDeCapitalFaltantes.class.getName());
	final String SQL_SELECT_EVENTOS_BURSATIL 			= loadResourceToString("cl/bcs/custodia/sql/CargarEventoCapitalDesdeBursatil_SelectEventosBursatil.sql");
	final String SQL_INSERT_EVENTOS_BURSATIL_TABLA_PASO = loadResourceToString("cl/bcs/custodia/sql/CargarEventoCapitalDesdeBursatil_InsertEventosBursatil.sql");
	final String SQL_SELECT_EVENTOS_FALTANTES 			= loadResourceToString("cl/bcs/custodia/sql/CargarEventoCapitalDesdeBursatil_SelectEventosACargar.sql");
	private final String SQL_SELECT_TIPOS_EVENTOS_CAPITAL_ACTIVOS = loadResourceToString("cl/bcs/custodia/sql/BuscarEventosDeCapitalFaltantes_SelectTiposEventoCapitalActivo.sql");
	
	private final String c_SI = "S";
	private final String c_NO = "N";

	public static class Entrada {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_FechaConsulta;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Salida {		
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;		
		public List<EventosFaltantes>	P_OUT_EVENTOSFALTANTES;
		
		
		public Salida(){
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_EVENTOSFALTANTES = new ArrayList<EventosFaltantes>();	
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	
	public static class EventosFaltantes {		
		public String 		TipoEventoCapital; 	// ACF_TIPO_VC;			
		public String 		Nemotecnico; 		// ACF_NEMO;		
		public Long			Secuencia; 			// ACF_SEC_VC;
		public String		Descripcion; 		// ACF_DESCRIP_VC;		
		public String		FechaLimite; 		// ACF_FEC_LIM;
		public String		FechaPago; 			// ACF_FEC_PAGO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class TiposEventosCapital extends BcsPojo {
		public String IdTipoEventoCapital;
	}

	
	//-------------------------------------------
	// CALL
	//-------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {			
		Gson gson = new Gson();		
		cl.bcs.custodia.CargarEventoCapitalDesdeBursatil.Entrada in=gson.fromJson(jin.toString() ,cl.bcs.custodia.CargarEventoCapitalDesdeBursatil.Entrada.class);
		Salida out=logica(con,in);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}
		
	//-------------------------------------------
	// LOGICA
	//-------------------------------------------
	public Salida logica(Connection con, CargarEventoCapitalDesdeBursatil.Entrada entrada) throws Exception {
		
		logger.info("INICIO :: BuscarEventosDeCapitalFaltantes");
		
		Salida out = new Salida();
		List<RegistroAInsertar> eventosFaltantes = new ArrayList<>();
		logger.info("ENTRADA: " + entrada.toString());
		
		eventosFaltantes = obtenerEventosRuebas(con,entrada);
		
		
		eventosFaltantes.forEach(EveRuebas -> {
			EventosFaltantes objEvento = new EventosFaltantes();
			
			objEvento.TipoEventoCapital 	= EveRuebas.ACF_TIPO_VC;		
			objEvento.Nemotecnico 			= EveRuebas.ACF_NEMO;	
			objEvento.Secuencia 			= EveRuebas.ACF_SEC_VC;
			objEvento.Descripcion 			= EveRuebas.ACF_DESCRIP_VC;	
			objEvento.FechaLimite 			= EveRuebas.ACF_FEC_LIM;
			objEvento.FechaPago 			= EveRuebas.ACF_FEC_PAGO;
			
			out.P_OUT_EVENTOSFALTANTES.add(objEvento);
		});
				

		

		logger.info("SALIDA: " + out.toString());
		logger.info("TERMINO :: BuscarEventosDeCapitalFaltantes");
		
		return out;
	}
	
	public List<RegistroAInsertar> obtenerEventosRuebas(Connection con,cl.bcs.custodia.CargarEventoCapitalDesdeBursatil.Entrada entrada) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		SQL _sql = new SQL();
		List<RegistroAInsertar> eventosRuebas = new ArrayList<>();
		List<RegistroAInsertar> eventosFaltantes = new ArrayList<>();
		// ---------------------------------------------------------------------------------------------
		try {
			String DBdriver, DBuser, DBpass, DBurl, DBprop;

			String RutaArchivosConfiguracion = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa,
					"PLA.RutaArchivosConfiguracion");
			Properties prop = new Properties();
			InputStream input = null;
			try {
				input = new FileInputStream(RutaArchivosConfiguracion + "ruebas.properties");
			}
			catch(Exception e) {
				input = new FileInputStream("ruebas.properties");
			}
			prop.load(input);
			DBdriver = prop.getProperty("DBdriver");
			DBuser = prop.getProperty("DBuser");
			DBpass = prop.getProperty("DBpass");
			DBurl = prop.getProperty("DBurl");
			DBprop = prop.getProperty("DBprop");
			input.close();

			logger.info("Configuracion cargada: " + DBurl.toString());

			// ---------------------------------------------------------------------------------------------
			// Obtiene DS conexion a la BD RUEBAS
			BasicDataSource dsTarget = new BasicDataSource();
			try {

				// Obtiene propiedades de conexion a la BD RUEBAS (desde archivo
				// ruebas.properties alojado en el servidor)

				// Conexion para pruebas locales, apuntando a Ruebas QA Sebra(para esto se se
				// debe tener referenciada la libreria jtds en el proyecto)
//        	DBdriver	= "net.sourceforge.jtds.jdbc.Driver";
//    		DBuser		= "consorcio";
//    		DBpass		= "consorcio";
//    		DBurl		= "jdbc:jtds:sqlserver://161.238.92.5:1433/RUEBAS";  //QA SEBRA CB
//    		DBprop		= "SocketTimeout=120;LoginTimeout=30;";


				dsTarget.setDriverClassName(DBdriver);
				dsTarget.setUsername(DBuser);
				dsTarget.setPassword(DBpass);
				dsTarget.setUrl(DBurl);
				dsTarget.setConnectionProperties(DBprop);
				dsTarget.setDefaultAutoCommit(true);

				logger.info("Conectado a RUEBAS");
			} catch (Exception e) {
				logger.info("ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB");
				logger.info(e);
			}

			try (Connection conRU = dsTarget.getConnection()) {

				// ---------------------------------------------------------------------------------------------
				// Obtiene los eventos de capital desde Ruebas
				Long idPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
				
				Long EventoCapitalDiasBursatilRetroactivos = 30L;
				try {
					EventoCapitalDiasBursatilRetroactivos = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa,"CUSTODIA", "EventoCapitalDiasBursatilRetroactivos");
				} catch (Exception e) {
					logger.warn("ADVERTENCIA::Parametro EventoCapitalDiasBursatilRetroactivos no existe, se toma valor por defecto de " + EventoCapitalDiasBursatilRetroactivos + " dias.");
				}
				
				Long EventoCapitalDiasBursatilFuturos = 5L;
				try {
					EventoCapitalDiasBursatilFuturos = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa,"CUSTODIA", "EventoCapitalDiasBursatilFuturos");
				} catch (Exception e) {
					logger.warn("ADVERTENCIA::Parametro EventoCapitalDiasBursatilFuturos no existe, se toma valor por defecto de " + EventoCapitalDiasBursatilFuturos + " dias.");
				}

				logger.info("Inicia obtencion de eventos de capital de Ruebas");
				ObtenerDiaAnterior obtenerDiaAnt = new ObtenerDiaAnterior();
				InObtenerDiaAnterior inObtenerDiaAnt = new InObtenerDiaAnterior();
				InObtenerDiaAnterior.FechasIn fechasIn = new InObtenerDiaAnterior.FechasIn();
				fechasIn.fechaInicial = entrada.p_FechaSistema;
				fechasIn.numeroDias = EventoCapitalDiasBursatilRetroactivos;
				inObtenerDiaAnt.fechas.add(fechasIn);
				inObtenerDiaAnt.idPais = idPaisSistema;
				inObtenerDiaAnt.diaHabil = c_NO;
				inObtenerDiaAnt.soloCaeDiaHabil = c_SI;

				ObtenerDiaSiguiente obtenerDiaSig = new ObtenerDiaSiguiente();
				InObtenerDiaSiguiente inObtenerDiaSig = new InObtenerDiaSiguiente();
				InObtenerDiaSiguiente.FechasIn fechasInSig = new InObtenerDiaSiguiente.FechasIn();
				fechasInSig.fechaInicial = entrada.p_FechaSistema;
				fechasInSig.numeroDias = EventoCapitalDiasBursatilFuturos;
				fechasInSig.pais = idPaisSistema;
				inObtenerDiaSig.p_fechas.add(fechasInSig);
				inObtenerDiaSig.p_soloCaeDiaHabil = c_SI;
				inObtenerDiaSig.p_diaHabil = c_SI;

				ObtenerDiaAnterior obtenerDiaAntHabil = new ObtenerDiaAnterior();
				InObtenerDiaAnterior inObtenerDiaAntHabil = new InObtenerDiaAnterior();
				InObtenerDiaAnterior.FechasIn fechasInHabil = new InObtenerDiaAnterior.FechasIn();
				fechasInHabil.fechaInicial = entrada.p_FechaSistema;
				fechasInHabil.numeroDias = 1L;
				inObtenerDiaAntHabil.fechas.add(fechasInHabil);
				inObtenerDiaAntHabil.idPais = idPaisSistema;
				inObtenerDiaAntHabil.diaHabil = c_SI;
				inObtenerDiaAntHabil.soloCaeDiaHabil = c_SI;
								
				OutObtenerDiaAnterior outObtenerDiaAnteriorHabil = obtenerDiaAntHabil.logica(con, inObtenerDiaAntHabil);
				String fechaAnteriorHabil = outObtenerDiaAnteriorHabil.fechas.get(0).fechaCalculada;
				
				ObtenerDiaSiguiente obtenerDiaSigNoHabil = new ObtenerDiaSiguiente();
				InObtenerDiaSiguiente inObtenerDiaSigNoHabil = new InObtenerDiaSiguiente();
				InObtenerDiaSiguiente.FechasIn fechasInSigNoHabil = new InObtenerDiaSiguiente.FechasIn();
				fechasInSigNoHabil.fechaInicial = fechaAnteriorHabil;
				fechasInSigNoHabil.numeroDias = 1L;
				fechasInSigNoHabil.pais = idPaisSistema;
				inObtenerDiaSigNoHabil.p_fechas.add(fechasInSigNoHabil);
				inObtenerDiaSigNoHabil.p_soloCaeDiaHabil = c_NO;
				inObtenerDiaSigNoHabil.p_diaHabil = c_NO;
				
				OutObtenerDiaSiguiente outObtenerDiaSiguienteNoHabil = obtenerDiaSigNoHabil.logica(con, inObtenerDiaSigNoHabil);
				String fechaInicialNoHabil = outObtenerDiaSiguienteNoHabil.p_out_fechas.get(0).fechaCalculada;

				OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSig.logica(con, inObtenerDiaSig);
				String fechaFinal = outObtenerDiaSiguiente.p_out_fechas.get(0).fechaCalculada;

				OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnt.logica(con, inObtenerDiaAnt);
				String fechaInicio = outObtenerDiaAnterior.fechas.get(0).fechaCalculada;

				params.put("p_Fecha_inicio", fechaInicio);
				params.put("p_Fecha_fin", fechaFinal);

				eventosRuebas = _sql.query(conRU, SQL_SELECT_EVENTOS_BURSATIL, params, RegistroAInsertar.class);
				
				params.clear();
				params.put("p_Fecha_inicio", fechaInicio);
				params.put("p_Fecha_fin", entrada.p_FechaSistema);

			} catch (Exception er) {

				logger.info("ERROR NO SE PUEDEN OBTENER LOS EVENTOS DE CAPITAL DESDE RUEBAS");
				logger.info(er);
			}
			try {
				logger.info(" Eventos capital obtenidos de Ruebas : " + eventosRuebas.size());
				logger.info("Insertan en tabla Global los EVC obtenidos desde bursatil");
				_sql.batch(con, SQL_INSERT_EVENTOS_BURSATIL_TABLA_PASO, eventosRuebas, RegistroAInsertar.class);

				// ---------------------------------------------------------------------------------------------
				// Compara Optimus con Ruebas para obtener los eventos faltantes

				logger.info("Obtener los eventos faltantes");

				params.put("p_IdEmpresa", entrada.p_IdEmpresa);

				eventosFaltantes = _sql.query(con, SQL_SELECT_EVENTOS_FALTANTES, params, RegistroAInsertar.class);
				
				List<TiposEventosCapital> tiposEventosCapitalActivos = this.obtenerTiposEventoCapitalActivos(con);
				
				Set<String> listaTiposEventosCapitalActivos = tiposEventosCapitalActivos.stream()
						.filter(Objects::nonNull)
						.map(tipoevento ->{
							return tipoevento.IdTipoEventoCapital.toUpperCase();
						}).collect(Collectors.toSet());
				
				if(!eventosFaltantes.isEmpty() && !listaTiposEventosCapitalActivos.isEmpty()) {
					eventosFaltantes = eventosFaltantes.stream()
							.filter(evento -> (evento.ECV_EVENTO_CAPITAL != null || (evento.ECV_EVENTO_CAPITAL ==null &&  listaTiposEventosCapitalActivos.contains(evento.ACF_TIPO_VC.toUpperCase())) ))
							.collect(Collectors.toList());
				}

				logger.info("Eventos Faltantes: " + eventosFaltantes.size());
			} catch (Exception e) {
				logger.info("ERROR NO SE PUEDE LEER ruebas.properties");
				logger.info(e);
			}
		} catch (Exception e) {

			logger.info("ERROR NO SE PUEDE LEER ruebas.properties");
			logger.info(e);
		}
		return eventosFaltantes;

	}
	
	private List<TiposEventosCapital> obtenerTiposEventoCapitalActivos (Connection con) throws SQLException {
		logger.debug("INICIO::CargarEventoCapitalDesdeBursatil.obtenerTiposEventoCapitalActivos");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		HashMap<String, Object> params = new HashMap<>(0);
		
		List<TiposEventosCapital> tiposEventosCapitalActivos = _sql.query(con, SQL_SELECT_TIPOS_EVENTOS_CAPITAL_ACTIVOS, params, TiposEventosCapital.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarEventoCapitalDesdeBursatil.obtenerTiposEventoCapitalActivos tiempo: " + (fin - inicio));
		return tiposEventosCapitalActivos;
	}
	
	
	//-------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------
	public static void main(String[] args) {
        
		try {
			System.out.println("== INICIO ==");
			Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties");
			cl.bcs.custodia.CargarEventoCapitalDesdeBursatil.Entrada entrada = new cl.bcs.custodia.CargarEventoCapitalDesdeBursatil.Entrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 0L;
			entrada.p_IdModulo = "CUS";
			entrada.p_FechaSistema = "2026-03-13";
			System.out.println(entrada.toString());

			Gson gson = new Gson();
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(gson.toJson(entrada)).getAsJsonObject();
	        BuscarEventosDeCapitalFaltantes demo=new BuscarEventosDeCapitalFaltantes();			
	        Salida resp=gson.fromJson(demo.call(con, dataJsonIn),Salida.class);
	    	System.out.println(resp);	

	    	//con.commit();
	    	con.rollback();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}

}