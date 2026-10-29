package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.MigrarDataHistorico;
import cl.bcs.plataforma.pla.MigrarDataHistorico.MigrarDataHistoricoEntrada;
import cl.bcs.plataforma.pla.MigrarDataHistorico.MigrarDataHistoricoSalida;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn;
import cl.bcs.plataforma.pla.LimpiarDataHistorico;
import cl.bcs.plataforma.pla.LimpiarDataHistorico.LimpiarDataHistoricoEntrada;
import cl.bcs.plataforma.pla.LimpiarDataHistorico.LimpiarDataHistoricoSalida;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GestionDataHistorica implements JavaCall {
	
	static final Logger logger 	= LogManager.getLogger(GestionDataHistorica.class.getName());	
	FileLoader loader 			= new FileLoader();
	
	private final String SQL_SELECT_TABLAS_MIGRAR        = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/GestionDataHistorica_Select_Tablas_Migrar.sql");
	private final String SQL_SELECT_TABLAS_LIMPIAR       = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/GestionDataHistorica_Select_Tablas_Limpiar.sql");
	
    public static class FiltrosLimpieza{
    	public String Tabla;
    	public String Filtro;
		public String Desde;
		public String Hasta;		
	}
    
    public static class TablaDataHistorica{
		public String CodigoTabla;
		public String CampoFiltro;
		public Long DiasEnLinea;
		public Long Desfase;
		public String SoloLimpia;
	}
    
    public static class GestionDataHistoricaEntrada {
    	public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_Fecha;
	}
    
    public static class GestionDataHistoricaSalida  {
		public Long	P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public GestionDataHistoricaSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
    
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		GestionDataHistoricaEntrada in = gson.fromJson(jin.toString(), GestionDataHistoricaEntrada.class);
		GestionDataHistoricaSalida out = logica(conn, in);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
    
	public String ObtenerDiaAnterior (Connection conn, Long idEmpresa, String fechaInicial, Long diasDesfase, String contarSoloDiaHabil) throws Exception{	
		long idPais = Long.valueOf(ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.PaisSistema"));
		
		ObtenerDiaAnterior.InObtenerDiaAnterior inObtenerDiaAnterior = new ObtenerDiaAnterior.InObtenerDiaAnterior();
		inObtenerDiaAnterior.idPais = idPais;
		inObtenerDiaAnterior.diaHabil = contarSoloDiaHabil;
		inObtenerDiaAnterior.soloCaeDiaHabil = "S";
		inObtenerDiaAnterior.excluirConsultarDiaFeriado = false;
		inObtenerDiaAnterior.fechas.add(new cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn(fechaInicial, diasDesfase));

		ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
		ObtenerDiaAnterior.OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior.logica(conn, inObtenerDiaAnterior);
		String diaAnteriorHabil = outObtenerDiaAnterior.fechas.get(0).fechaCalculada;
		
		return diaAnteriorHabil;
		
	}
	
	public Date ObtenerDiaSiguienteIncluyendoNoHabiles (Connection conn, Long idEmpresa, String fechaInicial, String caeSoloDiaHabil) throws Exception{	
		long idPais = Long.valueOf(ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.PaisSistema"));
		
		ObtenerDiaSiguiente.InObtenerDiaSiguiente inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
		inObtenerDiaSiguiente.p_idPais = idPais;
		inObtenerDiaSiguiente.p_diaHabil = "S";
		inObtenerDiaSiguiente.p_soloCaeDiaHabil = caeSoloDiaHabil;
		inObtenerDiaSiguiente.p_fechas.add(new cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn(fechaInicial, 1L, idPais));
		
		ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
		ObtenerDiaSiguiente.OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(conn, inObtenerDiaSiguiente);
		String diaSiguienteHabil = outObtenerDiaSiguiente.p_out_fechas.get(0).fechaCalculada;
		
		Date diaSiguienteHabilDate = Fecha.toDate(diaSiguienteHabil);
		LocalDate diaSiguienteHabilLocalDate = Instant.ofEpochMilli(diaSiguienteHabilDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
		diaSiguienteHabilLocalDate = diaSiguienteHabilLocalDate.minusDays(1L);
		
		Date diaSiguenteIncluyendoNoHabiles = Date.from(diaSiguienteHabilLocalDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		return diaSiguenteIncluyendoNoHabiles;
	}
	
	public GestionDataHistoricaSalida  logica(Connection conn, GestionDataHistoricaEntrada entrada) throws Exception {		
		logger.info("***** Inicio servicio GestionDataHistorica*****");
		
		GestionDataHistoricaSalida out = new GestionDataHistoricaSalida();
		SQL sql = new SQL();		
		SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");		
		
		HashMap<String, Object> parametroDataHistorica = new HashMap<>();
		parametroDataHistorica.put("p_IdEmpresa", entrada.p_IdEmpresa);

		// Migracion de data
		List<TablaDataHistorica> tablasDataHistoricaMigrar = sql.query(conn, SQL_SELECT_TABLAS_MIGRAR, parametroDataHistorica, TablaDataHistorica.class);
		MigrarDataHistorico migrarDataHis = new MigrarDataHistorico();
		MigrarDataHistoricoEntrada entradaMig = new MigrarDataHistoricoEntrada();

		for (TablaDataHistorica tablaDataHistorica : tablasDataHistoricaMigrar) {

			Long diasAMantener = tablaDataHistorica.DiasEnLinea;

			String fechaDesdeMigrado = ObtenerDiaAnterior(conn, entrada.p_IdEmpresa, entrada.p_Fecha, diasAMantener, "S");
			Date fechaDesdeMigradoDate =  Fecha.toDate(fechaDesdeMigrado);
			Date fechaHastaMigradoDate = ObtenerDiaSiguienteIncluyendoNoHabiles(conn, entrada.p_IdEmpresa, fechaDesdeMigrado, "S");

			entradaMig.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaMig.p_IdUsuario = entrada.p_IdUsuario;
			entradaMig.p_Tabla = tablaDataHistorica.CodigoTabla;
			entradaMig.p_Filtro = tablaDataHistorica.CampoFiltro;
			entradaMig.p_Desde = formato.format(fechaDesdeMigradoDate);
			entradaMig.p_Hasta = formato.format(fechaHastaMigradoDate);

			MigrarDataHistoricoSalida salida = migrarDataHis.logica(conn, entradaMig);
			if (salida.P_OUT_ERRORESCONTADOR == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, entradaMig.p_Tabla + " respaldada entre " + entradaMig.p_Desde + " y " + entradaMig.p_Hasta + ".", new HashMap<String, String>(1), conn, entrada.p_IdEmpresa));
				logger.info(salida.P_OUT_MENSAJES.get(0));
			} else {
				out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
						"ERROR en proceso de migracion detectado, no se realizo migracion de: " + tablaDataHistorica.CodigoTabla + ".", new HashMap<String, String>(1), conn, entrada.p_IdEmpresa));
				logger.error("ERROR en proceso de migracion detectado, no se realizo migracion de: " + tablaDataHistorica.CodigoTabla + ".");
				throw new Exception();
			}
		}

		// Borrado de data
		List<TablaDataHistorica> tablaDataHistoricaLimpiar = sql.query(conn, SQL_SELECT_TABLAS_LIMPIAR, parametroDataHistorica, TablaDataHistorica.class);
		LimpiarDataHistorico limpiarDataHis = new LimpiarDataHistorico();
		LimpiarDataHistoricoEntrada entradaLimpieza = new LimpiarDataHistoricoEntrada();

		for (TablaDataHistorica tablaDataHistorica : tablaDataHistoricaLimpiar) {
			
			Long desfase = tablaDataHistorica.Desfase + tablaDataHistorica.DiasEnLinea;		
			
			String fechaDesdeBorrado = ObtenerDiaAnterior(conn, entrada.p_IdEmpresa, entrada.p_Fecha, desfase, "S"); 
			Date fechaDesdeBorradoDate = Fecha.toDate(fechaDesdeBorrado);
			Date fechaHastaBorradoDate = ObtenerDiaSiguienteIncluyendoNoHabiles(conn, entrada.p_IdEmpresa, fechaDesdeBorrado, "S");

			entradaLimpieza.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaLimpieza.p_IdUsuario = entrada.p_IdUsuario;
			entradaLimpieza.p_Tabla = tablaDataHistorica.CodigoTabla;
			entradaLimpieza.p_Filtro = tablaDataHistorica.CampoFiltro;
			entradaLimpieza.p_Desde = formato.format(fechaDesdeBorradoDate);
			entradaLimpieza.p_Hasta = formato.format(fechaHastaBorradoDate);
			entradaLimpieza.p_SoloLimpia = tablaDataHistorica.SoloLimpia;

			LimpiarDataHistoricoSalida salida = limpiarDataHis.logica(conn, entradaLimpieza);
			if (salida.P_OUT_ERRORESCONTADOR == 0) {
				out.P_OUT_MENSAJES.add(salida.P_OUT_MENSAJES.get(0));
				logger.info(salida.P_OUT_MENSAJES.get(0));
			} else {
				out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
						"ERROR en proceso de limpieza detectado, no se realizo limpieza de: " + tablaDataHistorica.CodigoTabla + ".", new HashMap<String, String>(1), conn, entrada.p_IdEmpresa));
				logger.error("ERROR en proceso de limpieza detectado, no se realizo limpieza de: " + tablaDataHistorica.CodigoTabla + ".");
				throw new Exception();
			}
		}
					
    	
    	logger.info("***** Fin servicio GestionDataHistorica *****");
    	if(out.P_OUT_ERRORESCONTADOR == 0) {
    		out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "GestionDataHistorica ejecutado correctamente.", new HashMap<String, String>(1), conn, entrada.p_IdEmpresa));
    	}   			
    	
		return out;
	
		}

	    public static void main(String[] args) throws Exception {
			System.out.println("== TEST ==");
			try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
				GestionDataHistoricaEntrada GestionDataHistoricaEntrada = new GestionDataHistoricaEntrada();
				GestionDataHistoricaEntrada.p_IdEmpresa = 1L;
				GestionDataHistoricaEntrada.p_IdUsuario = 21L;
				GestionDataHistoricaEntrada.p_Fecha = "2024-09-30";
				
				GestionDataHistorica GestionDataHistorica = new GestionDataHistorica();
				try {
					GestionDataHistoricaSalida GestionDataHistoricaSalida = GestionDataHistorica.logica(con, GestionDataHistoricaEntrada);
					con.commit();
					System.out.println(GestionDataHistoricaSalida);
				} catch (Exception er) {
					er.printStackTrace();
					con.rollback();
				}
							
	    	}
			System.out.println("== END ==");
		}

	 
	}



