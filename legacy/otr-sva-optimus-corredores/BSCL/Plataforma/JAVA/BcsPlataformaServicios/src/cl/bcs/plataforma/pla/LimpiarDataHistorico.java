package cl.bcs.plataforma.pla;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class LimpiarDataHistorico implements JavaCall {
	
	static final Logger logger 	= LogManager.getLogger(LimpiarDataHistorico.class.getName());	
	FileLoader loader 			= new FileLoader();
	
	//private final String SQL_INSERT_TABLA_FECHAS_HABILES  = "insert into \"BSCL.Custodia.BD::GBL.FechasHabiles_Entrada_Entrada\"(\"Fecha\")values (:fechaCalculada)";
	private final String SQL_LIMPIAR_DATA_HISTORICA_GENERICO = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/LimpiarDataHistorica_Delete_Data_Historica.sql");
	private final String SQL_LIMPIAR_DATA_HISTORICA_FUSION_DETALLE = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/LimpiarDataHistorica_Delete_FusionDetalle.sql");
	private final String SQL_SELECT_REGISTROS_COMPARATIVA_HANA_GENERICO = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/LimpiarDataHistorica_Select_Cantidad_A_Borrar.sql");
	private final String SQL_SELECT_REGISTROS_COMPARATIVA_SQL_GENERICO = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/LimpiarDataHistorica_Select_Registros_Previos.sql");
	private final String SQL_SELECT_REGISTROS_COMPARATIVA_HANA_FUSIONDETALLE = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/LimpiarDataHistorica_Select_Count_HisFusionDetalle_Hana.sql");
	private final String SQL_SELECT_REGISTROS_COMPARATIVA_SQL_FUSIONDETALLE = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/LimpiarDataHistorica_Select_Count_HisFusionDetalle_SQLServer.sql");
	private static String RutaArchivoConfiguracion;
	
	public static class FiltrosLimpieza{
    	public String Tabla;
    	public String Filtro;
		public String Desde;
		public String Hasta;		
	}
    
    public static class DataHistorica{
		public String CodigoTabla;
		public String CampoFiltro;
		public Long TamanoTabla;
		public Long MaximoRegitros;
		public String LimpiaTabla;
		public String MigraTabla;
	}    
    
    public static class LimpiarDataHistoricoEntrada {
    	public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_Tabla;
    	public String p_Filtro;
		public String p_Desde;
		public String p_Hasta;
		public String p_SoloLimpia;
	}
	
	public static class LimpiarDataHistoricoSalida  {
		public Long	P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public LimpiarDataHistoricoSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		LimpiarDataHistoricoEntrada in = gson.fromJson(jin.toString(), LimpiarDataHistoricoEntrada.class);
		LimpiarDataHistoricoSalida out = logica(conn, in);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
    
	public LimpiarDataHistoricoSalida  logica(Connection conn, LimpiarDataHistoricoEntrada entrada) throws Exception {
		
		System.out.println("Inicio servicio LimpiarDataHistorica");

		LimpiarDataHistoricoSalida out = new LimpiarDataHistoricoSalida();		
		SQL sql = new SQL();   
		
		logger.info("***** Inicio Proceso de Limpieza *****");		      
    		
		List<FiltrosLimpieza> rangoFechas = new ArrayList<>();
		FiltrosLimpieza parametrosLimpieza = new FiltrosLimpieza();
		HashMap<String, Object> selectCount = new HashMap<>();

		parametrosLimpieza.Tabla = entrada.p_Tabla;
		parametrosLimpieza.Filtro = entrada.p_Filtro;
		parametrosLimpieza.Desde = entrada.p_Desde;
		parametrosLimpieza.Hasta = entrada.p_Hasta;
		rangoFechas.add(parametrosLimpieza);

		String limpiaDataHistorica = "";
		String RegistrosEnFechaLimpiezaHana = "";
		String RegistrosEnFechaLimpiezaSQL = "";
		if (entrada.p_Tabla.equals("\"BSCL.Custodia.BD::HIS.FusionCarteraDetalle\"")) {
			limpiaDataHistorica = SQL_LIMPIAR_DATA_HISTORICA_FUSION_DETALLE.replace(":p_Tabla",
					parametrosLimpieza.Tabla);
			limpiaDataHistorica = limpiaDataHistorica.replace(":p_Filtro", parametrosLimpieza.Filtro);
			limpiaDataHistorica = limpiaDataHistorica.replace(":p_Hasta", "'" + entrada.p_Hasta + "'");
			limpiaDataHistorica = limpiaDataHistorica.replace(":p_Desde", "'" + entrada.p_Desde + "'");

			RegistrosEnFechaLimpiezaHana = SQL_SELECT_REGISTROS_COMPARATIVA_HANA_FUSIONDETALLE.replace(":p_Tabla",
					parametrosLimpieza.Tabla);
			RegistrosEnFechaLimpiezaHana = RegistrosEnFechaLimpiezaHana.replace(":p_Filtro", parametrosLimpieza.Filtro);
			RegistrosEnFechaLimpiezaHana = RegistrosEnFechaLimpiezaHana.replace(":p_Hasta",
					"'" + entrada.p_Hasta + "'");
			RegistrosEnFechaLimpiezaHana = RegistrosEnFechaLimpiezaHana.replace(":p_Desde",
					"'" + entrada.p_Desde + "'");

			RegistrosEnFechaLimpiezaSQL = SQL_SELECT_REGISTROS_COMPARATIVA_SQL_FUSIONDETALLE.replace(":p_Tabla",
					parametrosLimpieza.Tabla);
			RegistrosEnFechaLimpiezaSQL = RegistrosEnFechaLimpiezaSQL.replace(":p_Filtro", parametrosLimpieza.Filtro);
			RegistrosEnFechaLimpiezaSQL = RegistrosEnFechaLimpiezaSQL.replace(":p_Hasta", "'" + entrada.p_Hasta + "'");
			RegistrosEnFechaLimpiezaSQL = RegistrosEnFechaLimpiezaSQL.replace(":p_Desde", "'" + entrada.p_Desde + "'");
		} else {
			limpiaDataHistorica = SQL_LIMPIAR_DATA_HISTORICA_GENERICO.replace(":p_Tabla", parametrosLimpieza.Tabla);
			limpiaDataHistorica = limpiaDataHistorica.replace(":p_Filtro", parametrosLimpieza.Filtro);
			limpiaDataHistorica = limpiaDataHistorica.replace(":p_Hasta", "'" + entrada.p_Hasta + "'");
			limpiaDataHistorica = limpiaDataHistorica.replace(":p_Desde", "'" + entrada.p_Desde + "'");

			RegistrosEnFechaLimpiezaHana = SQL_SELECT_REGISTROS_COMPARATIVA_HANA_GENERICO.replace(":p_Tabla",
					parametrosLimpieza.Tabla);
			RegistrosEnFechaLimpiezaHana = RegistrosEnFechaLimpiezaHana.replace(":p_Filtro", parametrosLimpieza.Filtro);
			RegistrosEnFechaLimpiezaHana = RegistrosEnFechaLimpiezaHana.replace(":p_Hasta",
					"'" + entrada.p_Hasta + "'");
			RegistrosEnFechaLimpiezaHana = RegistrosEnFechaLimpiezaHana.replace(":p_Desde",
					"'" + entrada.p_Desde + "'");

			RegistrosEnFechaLimpiezaSQL = SQL_SELECT_REGISTROS_COMPARATIVA_SQL_GENERICO.replace(":p_Tabla",
					parametrosLimpieza.Tabla);
			RegistrosEnFechaLimpiezaSQL = RegistrosEnFechaLimpiezaSQL.replace(":p_Filtro", parametrosLimpieza.Filtro);
			RegistrosEnFechaLimpiezaSQL = RegistrosEnFechaLimpiezaSQL.replace(":p_Hasta", "'" + entrada.p_Hasta + "'");
			RegistrosEnFechaLimpiezaSQL = RegistrosEnFechaLimpiezaSQL.replace(":p_Desde", "'" + entrada.p_Desde + "'");
		}

		Long cantidadRegistrosABorrar = sql.countQuery(conn, RegistrosEnFechaLimpiezaHana, selectCount);
		Long cantidadRegistrosSQL = -1L;
		Long cantidadRegistrosPostLimpieza = -1L;
		
		
			if (RutaArchivoConfiguracion == null) {
				RutaArchivoConfiguracion = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.RutaArchivosConfiguracion");
			}

			if (RutaArchivoConfiguracion == null) {
				throw new Exception(
						"Inicializacion LimpiarDataHistorica: RutaArchivosConfiguracion no definida. Revise parametro PLA.RutaArchivosConfiguracion");
			}
			InputStream input = null;
			String archivoProperties = "";
			try {
				archivoProperties = "historico.properties";
				input = new FileInputStream("historico.properties");
			} catch (FileNotFoundException fnfe) {
				archivoProperties = RutaArchivoConfiguracion + "historico.properties";
			}

			CompositeConfiguration prop = new CompositeConfiguration();
			prop.addConfiguration(new SystemConfiguration());
			prop.addConfiguration(new PropertiesConfiguration(archivoProperties));

			String dbURL = prop.getString("sqlserver.dbURL");
			String dbprop = prop.getString("sqlserver.dbprop");
			String dbdrv = prop.getString("sqlserver.dbdrv");
			String dbqry = prop.getString("sqlserver.dbquery");
			String user = prop.getString("sqlserver.dbuser");
			String pass = prop.getString("sqlserver.dbpass");

			// Datasource
			BasicDataSource dsTarget = new BasicDataSource();
			dsTarget.setDriverClassName(dbdrv);
			dsTarget.setUsername(user);
			dsTarget.setPassword(pass);
			dsTarget.setUrl(dbURL);
			dsTarget.setValidationQuery(dbqry);
			dsTarget.setConnectionProperties(dbprop);
			dsTarget.setDefaultAutoCommit(true);
			try (Connection conSQLServer = dsTarget.getConnection(); Statement stmtComparativa = conSQLServer.createStatement(); ResultSet result = stmtComparativa.executeQuery(RegistrosEnFechaLimpiezaSQL);) {

			if (cantidadRegistrosABorrar.compareTo(0L) != 0) {
				result.next();
				cantidadRegistrosSQL = result.getLong(1);

				if (cantidadRegistrosSQL.compareTo(cantidadRegistrosABorrar) == 0 || entrada.p_SoloLimpia.equals("S")) {
					// BORRAMOS HANA
					sql.batch(conn, limpiaDataHistorica, rangoFechas, FiltrosLimpieza.class);

					// CONTEO REGISTROS POST BORRADO
					cantidadRegistrosPostLimpieza = sql.countQuery(conn, RegistrosEnFechaLimpiezaHana, selectCount);
					if (cantidadRegistrosPostLimpieza == 0) {
						logger.info("Proceso de limpieza de" +  entrada.p_Tabla + " finalizo correctamente entre " + parametrosLimpieza.Desde + " y " + parametrosLimpieza.Hasta);
						logger.info("***** Fin Proceso de Limpieza *****");
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "Proceso de limpieza de " +  entrada.p_Tabla +" finalizo correctamente entre " + parametrosLimpieza.Desde + " y " + parametrosLimpieza.Hasta, new HashMap<String, String>(1), conn, entrada.p_IdEmpresa));
					} else {
						logger.error("ERROR en proceso de limpieza detectado, no se realizo limpieza de: "
								+ parametrosLimpieza.Tabla + ".");
						out.P_OUT_ERRORESCONTADOR = 1L;
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR en proceso de limpieza detectado, no se realizo limpieza de: " + parametrosLimpieza.Tabla 
								+ ".", new HashMap<String, String>(1), conn, entrada.p_IdEmpresa));
					}
				} else {
					logger.info("Proceso de limpieza de: " + parametrosLimpieza.Tabla + " no se pudo realizar ya que no hay data respaldada  entre " + parametrosLimpieza.Desde + " y " + parametrosLimpieza.Hasta + ".");
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "Proceso de limpieza de: " + parametrosLimpieza.Tabla + " no se pudo realizar ya que no hay data respaldada  entre "
							+ parametrosLimpieza.Desde + " y " + parametrosLimpieza.Hasta + ".", new HashMap<String, String>(1), conn, entrada.p_IdEmpresa));
				}

			} else {
				logger.info("No se realizo eliminacion de data ya que no existe data a eliminar en hana.");
				logger.info("***** Fin Proceso de Limpieza *****");
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK,
						"Proceso de limpieza no encontro data a eliminar en " + entrada.p_Tabla + " entre " + parametrosLimpieza.Desde + " y " + parametrosLimpieza.Hasta + ".", new HashMap<String, String>(1), conn, entrada.p_IdEmpresa));
			}

		} catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR Proceso de limpieza.", new HashMap<String, String>(1), conn, entrada.p_IdEmpresa));
			logger.error(e);
			logger.error("***** Error  Proceso de Limpieza *****");
		}

		return out;

	}
	
	/*
	public List<InObtenerDiaAnterior.FechasIn> obtenerUltimodiaHabil(Date fechaInicio, Date fechaFin){
		
		List<InObtenerDiaAnterior.FechasIn> listaFechas = new ArrayList<>();
		SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

		Calendar fechaDesde         = Calendar.getInstance();
		Calendar fechaHasta         = Calendar.getInstance();
		
		fechaDesde.setTime(fechaInicio);
		fechaHasta.setTime(fechaFin);
		
		while(fechaDesde.before(fechaHasta)){

			fechaDesde.set(Calendar.DAY_OF_MONTH, 1);
			fechaDesde.add(Calendar.MONTH, 1);

        	String dateCartera   = formato.format(fechaDesde.getTime()).toUpperCase();

        	InObtenerDiaAnterior.FechasIn  FH = new InObtenerDiaAnterior.FechasIn();
        	FH.fechaInicial = dateCartera;
        	FH.numeroDias	= 1L;
        	listaFechas.add(FH);

        }
		return listaFechas;
	}
	
		
		public  ObtenerDiaAnterior.OutObtenerDiaAnterior diaAnteriorHabil (Connection conn, List<InObtenerDiaAnterior.FechasIn> listaFechas , Long idPais) throws Exception{
			logger.trace("****** Inicio Proceso Obtener Dia habil ******");
			InObtenerDiaAnterior ObtenerDiaAnterior = new InObtenerDiaAnterior();
    		ObtenerDiaAnterior.idPais =idPais;
					
    		ObtenerDiaAnterior.diaHabil = "S";
    		ObtenerDiaAnterior.fechas = listaFechas;
    		
    		ObtenerDiaAnterior Habiles = new ObtenerDiaAnterior();
			ObtenerDiaAnterior.OutObtenerDiaAnterior Habil = Habiles.logica(conn, ObtenerDiaAnterior);
    		
			logger.trace("****** Fin Proceso Obtener Dia habil ******");
			return Habil;
			
		}
		
		public static Date ObtenerFechasParametro(Date fecha,Long dias,Long IdPais, Connection conn) throws Exception{
			
    	logger.trace("INICIO::ObtenerFechasParametro.crearMovimiendoAfacturar");
    	
		//SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ObtenerDiasHabilesAnteriores ObtenerDiasHabilesAnteriores = new ObtenerDiasHabilesAnteriores();
		InObtenerDiasHabilesAnteriores inObtenerDiaAnterior       = new InObtenerDiasHabilesAnteriores();
	    InObtenerDiasHabilesAnteriores.FechasIn fechaIn           = new InObtenerDiasHabilesAnteriores.FechasIn();
	   
		fechaIn.fechaInicial = fecha.toString();
		inObtenerDiaAnterior.idPais =IdPais;
		inObtenerDiaAnterior.diaHabil = "S";
		fechaIn.numeroDias = dias;
		inObtenerDiaAnterior.fechas.add(fechaIn);

		String fechaDefinitiva ="0000-01-01";
		
		OutObtenerDiasHabilesAnteriores diaAnteriorCarteraOut = ObtenerDiasHabilesAnteriores.logica(conn, inObtenerDiaAnterior);
		for (OutObtenerDiasHabilesAnteriores.FechasOut diasCalculador : diaAnteriorCarteraOut.fechas) {
			fechaDefinitiva = diasCalculador.fechaCalculada;
		}	
		
		
		Date FechaReturn  =  Fecha.toDate(fechaDefinitiva);
		
		logger.trace("****** Inicio Proceso Obtener Fecha habiles  ******");
        return FechaReturn;
        
		}
	
     
	    public static Date quitarMesesFecha(Date fecha,Long meses){
	    	
	    	int mes = (int) (long)meses;
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(fecha);
	        cal.add(Calendar.MONTH, -mes);
	        return cal.getTime();
	    }

	    */
	    public static void main(String[] args) throws Exception {
			System.out.println("== TEST ==");
			
			System.out.println("== END ==");
		}

	 
	}