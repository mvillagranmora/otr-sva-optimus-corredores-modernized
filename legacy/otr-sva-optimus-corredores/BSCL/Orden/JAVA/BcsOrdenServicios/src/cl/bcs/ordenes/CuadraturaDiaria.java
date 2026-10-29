package cl.bcs.ordenes;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
 
public class CuadraturaDiaria implements JavaCall {

	private final Log log = new Log("ORD.CuadraturaDiaria");
	private static final Logger logger = LogManager.getLogger(CuadraturaDiaria.class);	

	private final String SQL_SELECT_CUADRATURADIARIAOPERACIONES 		= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_TRANSACCIONES 						= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransacciones.sql");
	private final String SQL_SELECT_TRANSACCIONESOTC					= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesOTC.sql");
	private final String SQL_SELECT_ORDENESPIVOT  						= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_ConsultarConfiguracionOrdenesPivot.sql");
	private final String SQL_DELETE_CUADRATURADIARIAOPERACIONES 		= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_DeleteCuadraturaDiariaOperaciones.sql");
	private final String SQL_INSERT_CUADRATURADIARIAOPERACIONES 		= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_InsertCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_DETALLE_CUADRATURADIARIAOPERACIONES = loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectJSONCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_ASIGNACIONES						= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectAsignaciones.sql");
	private final String SQL_SELECT_ASIGNACIONESOTC						= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectAsignacionesOTC.sql");
	private final String SQL_SELECT_MOVIMIENTOS_FACTURAR				= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectMovimientoComprobante.sql");
	private final String SQL_SELECT_MOVIMIENTOS_FACTURAROTC				= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectMovimientoComprobanteOTC.sql");
	private final String SQL_SELECT_CUADRATURA_CAVALI					= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectCuadraturaCavali.sql");
	private final String SQL_SELECT_TRANSACCIONES_FERIADO				= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesFeriado.sql");
	private final String SQL_SELECT_EVENTOS_BURSATIL_RV 				= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesRuebasRV.sql");
	private final String SQL_SELECT_EVENTOS_BURSATIL_IF					= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesRuebasIF.sql");
	private final String SQL_SELECT_EVENTOS_BURSATIL_RF					= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesRuebasRF.sql");
	private final String SQL_SELECT_CODIGOCORREDORA						= loadResourceToString("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectCodigoCorredora.sql");
	
	public static class CuadraturaDiariaObj{
		public String IdCuadraturaDiariaOperaciones;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class CuadraturaDiariaEntrada {
		public Long p_IdUsuario;
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Date p_FechaInicio;
		public List<CuadraturaDiariaObj> p_IdProceso;
		public String p_TipoLlamado;
		
		public CuadraturaDiariaEntrada() {
			this.p_IdProceso = new ArrayList<CuadraturaDiariaObj>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Pivote{
		public Long ValorPivot;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleResumenJSON{
		public String IdCuadraturaDiariaOperaciones;
		public String DetalleCuadraturaJSON;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class Resumen{
		public String IdCuadraturaDiariaOperaciones;
		public Long IdEmpresa;
		public String IdClasificacionTipoInstrumento;
		public String OperacionCuadraturaDiariaOperaciones;
		public Date FechaEjecucion;
		public Long TotalOperaciones;
		public Long OperacionesConProblemas;
		public String TipoOperacion;
		public String Resultado;
		public Long Orden;
		public String DetalleCuadraturaJSON;
		public Long IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Transacciones{
		public String TipoRegistro;
		public String IdClasificacionTipoInstrumento; 
		public String FechaOperacionTransaccion;
		public Date FechaVencimiento; 
		public String FolioTransaccion;     
		public String IdBolsa;     
		public String AccionOrden;
		public String Nemotecnico;
		public String CodigoOperadorCorredor;
		public BigDecimal CantidadTransaccion;
		public BigDecimal CantidadAsignadaTransaccion;
		public BigDecimal MontoTransaccion;
		public BigDecimal MontoAsignadoTransaccion;
		public String IdMonedaTransaccion;
		public String EsFungible;
		public String Descripcion;
		public String IdTipoInstrumento;
		public BigDecimal ValorRescateTransaccion;
		public BigDecimal ValorRescateAsignadoTransaccion;
		public String IndicadorOrdenDirecta;
		public String IdTipoOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Asignaciones{
		public Long IdMovimientoComprobante ;
		public String EstadoFacturacion;
		public String NombreEstadoFacturacion;
		public Long IdAsignacion;
		public String FolioTransaccion;
		public Timestamp FechaAsignacion;
		public String IdEstadoAsignacion;
		public String EstadoAsignacion;
		public String AccionOrden;
		public String NombreTipoOperacion;
		public String InstrumentoUI;
		public String IdBolsa;
		public String NombreBolsa;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public Long IdCliente;
		public String DocumentoIdentidad;
		public String NombreCompleto;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobante{
		public Long IdMovimientoComprobante;
		public String IdTipoComprobante;
		public String NombreTipoComprobante;
		public String EstadoFacturacion;
		public String NombreEstadoFacturacion;
		public String IdConceptoMovimiento;
		public String NombreConceptoMovimiento;
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public String NombreLargoCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuadraturaCavali{
		public Long NumeroOperacion;
		public java.sql.Date FechaOperacion;
		public String FolioTransaccion;
		public BigDecimal CantidadOperacion;
		public BigDecimal MontoEfectivo;
		public String DescripcionNegocio;
		public BigDecimal PrecioOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class objCuadraturaDiariaOperaciones{
		public Long IdEmpresa; 
		public String IdCuadraturaDiariaOperaciones;
		public String FechaEjecucion;
		public Long TotalOperaciones; 
		public Long OperacionesConProblemas;
		public String Resultado;
		public String DetalleCuadraturaJson;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PalosRuebas{
		public String NEMO;
		public String HORA;
		public BigDecimal FOLIO;
		public BigDecimal CANTIDAD;
		public String FECHA;
		public String FEC_LIQ;
		public BigDecimal MONTO;
		public BigDecimal PRECIO;
		public String RUEDA;
		
		public String toString() {
			return Utils.printJson(this,false);
		}
	}
	
	public static class Corredora{
		public String CodigoCorredora;
		public String Codigo;
		public Long IdCorredora;    
       
		public String toString() {
			return Utils.printJson(this,false);
		}
	}
	
	public static class CuadraturaDiariaSalida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public String						P_OUT_DETALLE_CUADRATURA;
		
		public CuadraturaDiariaSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*
	 * Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CuadraturaDiariaEntrada in = gson.fromJson(jin.toString(), CuadraturaDiariaEntrada.class);
		CuadraturaDiariaSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public CuadraturaDiariaSalida logica(Connection conn, CuadraturaDiariaEntrada in) throws Exception {
		SQL _sql = new SQL();
		CuadraturaDiariaSalida out = new CuadraturaDiariaSalida();
		String bolsaPorDefecto = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.BolsaDefectoSistema");
		log.write("INICIO");
		try {
			//TODO : Modo Proceso
			if (in.p_TipoLlamado.equals("Proceso")){
				//variables
				Long Id = 0L;
				HashMap<String,String> mapMsg = new HashMap<>();
				
				//Obtenemos los registros completos de cada resumen
				List<String> idOper = new ArrayList<>();
				for (CuadraturaDiariaObj obj: in.p_IdProceso){
					//necesito los codigos para RV, RF, DV, Reportes o simultaneas
					idOper.add(obj.IdCuadraturaDiariaOperaciones);
				}
				
				HashMap<String,Object> regResumen = new HashMap<>();
				regResumen.put("IdEmpresa", in.p_IdEmpresa);
				regResumen.put("IdCuadraturaDiariaOperaciones", idOper);
				List<Resumen> rsResumen = _sql.query(conn, SQL_SELECT_CUADRATURADIARIAOPERACIONES, regResumen, Resumen.class);

				List<Transacciones> transaccionesDescuadradas = new ArrayList<Transacciones>();
				List<Asignaciones> asignacionesSinMovFac = new ArrayList<Asignaciones>();
				List<MovimientoComprobante> movFacturarSinFacturar = new ArrayList<MovimientoComprobante>();
				List<CuadraturaCavali> movCuadraturaCavali = new ArrayList<CuadraturaCavali>();
				List<objCuadraturaDiariaOperaciones> parametrosparaBorrar = new ArrayList<objCuadraturaDiariaOperaciones>();

				List<Transacciones> trans = new ArrayList<Transacciones>();
				List<Transacciones> transaccionesDuplicadas = new ArrayList<Transacciones>();
				List<Transacciones> transaccionesFeriados = new ArrayList<Transacciones>();
				List<Asignaciones> asigna = new ArrayList<Asignaciones>();
				List<MovimientoComprobante> movtoFact = new ArrayList<MovimientoComprobante>();
				List<CuadraturaCavali> cuadraCavali = new ArrayList<CuadraturaCavali>();
				Long conteoDescuadres = 0L;
				Long conteoTotal = 0L;
				String jsonDetalle = "";
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				
				List<Corredora>codCorredora =consultaCorredora(conn);
				
				BasicDataSource dsTarget = new BasicDataSource();
				try {
					String DBdriver, DBuser, DBpass, DBurl, DBprop;

					String RutaArchivosConfiguracion = Utils.getConfiguracion(conn, Utils.getSchema(), in.p_IdEmpresa,
							"PLA.RutaArchivosConfiguracion");
					Properties prop = new Properties();
					InputStream input = new FileInputStream(RutaArchivosConfiguracion + "ruebas.properties");
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
					
					try {

						// Obtiene propiedades de conexion a la BD RUEBAS (desde archivo
						// ruebas.properties alojado en el servidor)

						// Conexion para pruebas locales, apuntando a Ruebas QA Sebra(para esto se se
						// debe tener referenciada la libreria jtds en el proyecto)
						/*
			        	DBdriver	= "net.sourceforge.jtds.jdbc.Driver";
			    		DBuser		= "consorcio";
			    		DBpass		= "consorcio";
			    		DBurl		= "jdbc:jtds:sqlserver://161.238.92.5:1433/RUEBAS";  //QA SEBRA CB
			    		DBprop		= "SocketTimeout=120;LoginTimeout=30;";
						*/

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
				}catch(Exception e) {
					log.write(e);
					throw e;
				}
				
				for (CuadraturaDiariaObj objProc: in.p_IdProceso){
					
					List<String> idCla = new ArrayList<>();
					String vResultado = new String();
					switch (objProc.IdCuadraturaDiariaOperaciones) {
					case "CUAASIGRV": 
					case "CUAASIGRF": 
					case "CUAASIGRE":
					case "CUAASIGOT":

					case "CUAASIGIF":

						
						//Cargar el monto maximo de tolerancia entre el monto nominal vs monto asignado.
						HashMap<String,Object> paramsPivote = new HashMap<>();
						paramsPivote.put("IdEmpresa", in.p_IdEmpresa);
						List<Pivote> vPivot = _sql.query(conn, SQL_SELECT_ORDENESPIVOT, paramsPivote, Pivote.class);
						
						for (Resumen obj: rsResumen){
							if (obj.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
								idCla.add(obj.IdClasificacionTipoInstrumento);
						
							}
							obj.IdUsuario = in.p_IdUsuario;
						}
						
						if (objProc.IdCuadraturaDiariaOperaciones.equals("CUAASIGOT")){
							trans = consultaTransaccionesOTC (conn, in.p_FechaInicio, idCla, bolsaPorDefecto);
						}else{
							trans = consultaTransacciones (conn, in.p_FechaInicio, idCla); //se quita bolsa por defecto ya que no considera transacciones de otras bolsas
							transaccionesFeriados = consultaTransaccionesFeriado (conn,in.p_FechaInicio);
							trans.addAll(transaccionesFeriados);
							
						}
						 
						transaccionesDescuadradas = cuadraturaTransacciones(trans, vPivot);
						
						if (trans.size()==0){
							vResultado = "UI_SIN_OPERACIONES";
						}
						else if(transaccionesDescuadradas.size() == 0){
							vResultado = "UI_CUADRADO";
						}else {
							vResultado = "UI_DESCUADRADO";
						}
						
						//cambio de mensajes segun diccionario.
						for (Transacciones msje : transaccionesDescuadradas) {
							msje.Descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, msje.Descripcion, new HashMap<String,String>());
						}	
						
						conteoTotal = (long)trans.size();						
						conteoDescuadres = (long)transaccionesDescuadradas.size();
						jsonDetalle = gson.toJson(transaccionesDescuadradas);
						break;
					case "CUAASIMOVFACRV":
					case "CUAASIMOVFACRF":

					case "CUAASIMOVFACIF":	
					case "CUAASIMOVFACRE":
					case "CUAASIMOVFACOT":
						for (Resumen obj: rsResumen){
							if (obj.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
								idCla.add(obj.IdClasificacionTipoInstrumento);
							}
							obj.IdUsuario = in.p_IdUsuario;
						}
						
						if (objProc.IdCuadraturaDiariaOperaciones.equals("CUAMOVFACOT")){
							asigna = consultaAsignacionesOTC (conn, in.p_FechaInicio, idCla, bolsaPorDefecto);
						}else{
							asigna = consultaAsignaciones (conn, in.p_FechaInicio, idCla);// se quita bolsa por defecto ya que no considera asignaciones de otras bolsas
						}
						 
						asignacionesSinMovFac = cuadraturaAsignaciones(asigna);
						
						if (asigna.size()==0){
							vResultado = "UI_SIN_OPERACIONES";
						}
						else if(asignacionesSinMovFac.size() == 0){
							vResultado = "UI_CUADRADO";
						}else {
							vResultado = "UI_DESCUADRADO";
						}
						
						//cambio de mensajes segun diccionario.
						for (Asignaciones msje : asignacionesSinMovFac) {
							msje.Descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, msje.Descripcion, new HashMap<String,String>());
						}
						
						conteoTotal = (long)asigna.size();
						conteoDescuadres = (long)asignacionesSinMovFac.size();
						jsonDetalle = gson.toJson(asignacionesSinMovFac);
						break;
					case "CUAMOVFACRV": 
					case "CUAMOVFACRF": 
					case "CUAMOVFACRE":
					case "CUAMOVFACOT":
						for (Resumen obj: rsResumen){
							if (obj.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
								idCla.add(obj.IdClasificacionTipoInstrumento);
							}
							obj.IdUsuario = in.p_IdUsuario;
						}
						
						if (objProc.IdCuadraturaDiariaOperaciones.equals("CUAMOVFACOT")){
							movtoFact = consultaMovimientosFacturarOTC (conn, in.p_FechaInicio, idCla, bolsaPorDefecto);
						}else{
							movtoFact = consultaMovimientosFacturar (conn, in.p_FechaInicio, idCla, bolsaPorDefecto);
						}
						 
						movFacturarSinFacturar = cuadraturaMovimientoFac(movtoFact);
						
						if (movtoFact.size()==0){
							vResultado = "UI_SIN_OPERACIONES";
						}
						else if(movFacturarSinFacturar.size() == 0){
							vResultado = "UI_CUADRADO";
						}else {
							vResultado = "UI_DESCUADRADO";
						}
						
						conteoTotal = (long)movtoFact.size();
						conteoDescuadres = (long)movFacturarSinFacturar.size();
						jsonDetalle = gson.toJson(movFacturarSinFacturar);
						break;
					case "CUACAVALI":
						for (Resumen obj: rsResumen){
							if (obj.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
								idCla.add(obj.IdClasificacionTipoInstrumento);
							}
							obj.IdUsuario = in.p_IdUsuario;
						}
						
						cuadraCavali = consultaCuadraturaCavali (conn, in.p_FechaInicio);
						
						movCuadraturaCavali = cuadraturaMovCavali(cuadraCavali);
						
						if (cuadraCavali.size()==0){
							vResultado = "UI_SIN_OPERACIONES";
						}
						else if(movCuadraturaCavali.size() == 0){
							vResultado = "UI_CUADRADO";
						}else {
							vResultado = "UI_DESCUADRADO";
						}
						
						conteoTotal = (long)cuadraCavali.size();
						conteoDescuadres = (long)movCuadraturaCavali.size();
						jsonDetalle = gson.toJson(movCuadraturaCavali);
						
						break;
					//casos comparasion a ruebas
					case "CUARUETRANRV":
						List<PalosRuebas> transRuebasRV = new ArrayList<PalosRuebas>();
						try (Connection conRU = dsTarget.getConnection()) {
							transRuebasRV=consultaTransaccionesRuebasRV(conRU,in.p_FechaInicio,codCorredora);
							
						} catch (Exception e) {
							log.write(e);
							throw e;
						}
						for (Resumen obj: rsResumen){
							if (obj.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
								idCla.add(obj.IdClasificacionTipoInstrumento);
						
							}
							obj.IdUsuario = in.p_IdUsuario;
						}
						trans = consultaTransacciones (conn, in.p_FechaInicio, idCla); //se quita bolsa por defecto ya que no considera transacciones de otras bolsas
						transRuebasRV = compararTransaccionesRuebasVsOptimus(trans,transRuebasRV);
						if (trans.size()==0){
							vResultado = "UI_SIN_OPERACIONES";
						}
						else if(transRuebasRV.size() == 0){
							vResultado = "UI_CUADRADO";
						}else {
							vResultado = "UI_DESCUADRADO";
						}
						conteoTotal = (long)trans.size();
						conteoDescuadres= (long)transRuebasRV.size();
						jsonDetalle = gson.toJson(castRuebasAResumen(transRuebasRV));
						break;
					case "CUARUETRANIF":
						List<PalosRuebas> transRuebasIF = new ArrayList<PalosRuebas>();
						try (Connection conRU = dsTarget.getConnection()) {
							transRuebasIF=consultaTransaccionesRuebasIF(conRU,in.p_FechaInicio,codCorredora);
						} catch (Exception e) {
							log.write(e);
							throw e;
						}
						for (Resumen obj: rsResumen){
							if (obj.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
								idCla.add(obj.IdClasificacionTipoInstrumento);
						
							}
							obj.IdUsuario = in.p_IdUsuario;
						}
						trans = consultaTransacciones (conn, in.p_FechaInicio, idCla); //se quita bolsa por defecto ya que no considera transacciones de otras bolsas
						transRuebasIF = compararTransaccionesRuebasVsOptimus(trans,transRuebasIF);
						if (trans.size()==0){
							vResultado = "UI_SIN_OPERACIONES";
						}
						else if(transRuebasIF.size() == 0){
							vResultado = "UI_CUADRADO";
						}else {
							vResultado = "UI_DESCUADRADO";
						}
						conteoTotal = (long)trans.size();
						conteoDescuadres= (long)transRuebasIF.size();
						jsonDetalle = gson.toJson(castRuebasAResumen(transRuebasIF));
						break;
					case "CUARUETRANRF":
						List<PalosRuebas> transRuebasRF = new ArrayList<PalosRuebas>();
						try (Connection conRU = dsTarget.getConnection()) {
							transRuebasRF = consultaTransaccionesRuebasRF(conRU,in.p_FechaInicio,codCorredora);
							
						} catch (Exception e) {
							log.write(e);
							throw e;
						}
						for (Resumen obj: rsResumen){
							if (obj.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
								idCla.add(obj.IdClasificacionTipoInstrumento);
						
							}
							obj.IdUsuario = in.p_IdUsuario;
						}
						trans = consultaTransacciones (conn, in.p_FechaInicio, idCla); //se quita bolsa por defecto ya que no considera transacciones de otras bolsas
						transRuebasRF = compararTransaccionesRuebasVsOptimus(trans,transRuebasRF);
						if (trans.size()==0){
							vResultado = "UI_SIN_OPERACIONES";
						}
						else if(transRuebasRF.size() == 0){
							vResultado = "UI_CUADRADO";
						}else {
							vResultado = "UI_DESCUADRADO";
						}
						conteoTotal = (long)trans.size();
						conteoDescuadres= (long)transRuebasRF.size();
						jsonDetalle = gson.toJson(castRuebasAResumen(transRuebasRF));
						break;
					case "CUATRANDUPLIRV":
						for (Resumen obj: rsResumen){
							if (obj.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
								idCla.add(obj.IdClasificacionTipoInstrumento);
						
							}
							obj.IdUsuario = in.p_IdUsuario;
						}
						trans = consultaTransacciones (conn, in.p_FechaInicio, idCla);
						transaccionesDuplicadas = consultaTransaccionesDuplicadas(conn, in.p_FechaInicio,trans);
						
						if (trans.size()==0){
							vResultado = "UI_SIN_OPERACIONES";
						}
						else if (transaccionesDuplicadas.size()==0){
							vResultado = "UI_CUADRADO";
						}
						else {
							vResultado = "UI_DESCUADRADO";
						}
						
						//cambio de mensajes segun diccionario.
						for (Transacciones msje : transaccionesDuplicadas) {
							msje.Descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, msje.Descripcion, new HashMap<String,String>());
						}	
						
						conteoTotal = (long)trans.size();						
						conteoDescuadres = (long)transaccionesDuplicadas.size();
						jsonDetalle = gson.toJson(transaccionesDuplicadas);
						break;
					case "CUATRANDUPLIRF":
						for (Resumen obj: rsResumen){
							if (obj.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
								idCla.add(obj.IdClasificacionTipoInstrumento);
						
							}
							obj.IdUsuario = in.p_IdUsuario;
						}
						trans = consultaTransacciones (conn, in.p_FechaInicio, idCla);
						transaccionesDuplicadas = consultaTransaccionesDuplicadas(conn, in.p_FechaInicio,trans);
						
						if (trans.size()==0){
							vResultado = "UI_SIN_OPERACIONES";
						}
						else if (transaccionesDuplicadas.size()==0){
							vResultado = "UI_CUADRADO";
						}
						else {
							vResultado = "UI_DESCUADRADO";
						}
						
						//cambio de mensajes segun diccionario.
						for (Transacciones msje : transaccionesDuplicadas) {
							msje.Descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, msje.Descripcion, new HashMap<String,String>());
						}	
						
						conteoTotal = (long)trans.size();						
						conteoDescuadres = (long)transaccionesDuplicadas.size();
						jsonDetalle = gson.toJson(transaccionesDuplicadas);
						break;
					case "CUATRANDUPLIIF":
						for (Resumen obj: rsResumen){
							if (obj.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
								idCla.add(obj.IdClasificacionTipoInstrumento);
						
							}
							obj.IdUsuario = in.p_IdUsuario;
						}
						trans = consultaTransacciones (conn, in.p_FechaInicio, idCla);
						transaccionesDuplicadas = consultaTransaccionesDuplicadas(conn, in.p_FechaInicio,trans);
						
						if (trans.size()==0){
							vResultado = "UI_SIN_OPERACIONES";
						}
						else if (transaccionesDuplicadas.size()==0){
							vResultado = "UI_CUADRADO";
						}
						else {
							vResultado = "UI_DESCUADRADO";
						}
						
						//cambio de mensajes segun diccionario.
						for (Transacciones msje : transaccionesDuplicadas) {
							msje.Descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, msje.Descripcion, new HashMap<String,String>());
						}	
						
						conteoTotal = (long)trans.size();						
						conteoDescuadres = (long)transaccionesDuplicadas.size();
						jsonDetalle = gson.toJson(transaccionesDuplicadas);
						break;
					default:
						break;
					}
					
					for (Resumen recoResumen: rsResumen){
						if (recoResumen.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
							recoResumen.FechaEjecucion 			= in.p_FechaInicio;
							recoResumen.TotalOperaciones 		= conteoTotal; 
							recoResumen.OperacionesConProblemas = conteoDescuadres;
							recoResumen.Resultado 				= vResultado;
							recoResumen.DetalleCuadraturaJSON 	= jsonDetalle;
							recoResumen.IdUsuario = in.p_IdUsuario;
						}
					}
					
					// preparamos parámetros para el borrado
					objCuadraturaDiariaOperaciones oDelete = new objCuadraturaDiariaOperaciones();
					oDelete.IdCuadraturaDiariaOperaciones = objProc.IdCuadraturaDiariaOperaciones;
					
					parametrosparaBorrar.add(oDelete);
				}
				
				// actualizamos tabla : Primero eliminamos los registros y los reinsertamos con el json correspondiente, esto debido a que
				// la tabla tiene una columna del tipo TEXT, las cuales no se pueden actualizar (UPDATE).
				if (parametrosparaBorrar.size() > 0){ 
					_sql.batch(conn, SQL_DELETE_CUADRATURADIARIAOPERACIONES, parametrosparaBorrar, objCuadraturaDiariaOperaciones.class);
					_sql.batch(conn, SQL_INSERT_CUADRATURADIARIAOPERACIONES, rsResumen, Resumen.class);
				}
				
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO",mapMsg,conn,in.p_IdEmpresa));
				AgregarLog(conn, in);
				
			}else if (in.p_TipoLlamado.equals("Consulta")){
				//TODO : Modo consulta
				List<String> idOper = new ArrayList<>();
				for (CuadraturaDiariaObj obj: in.p_IdProceso){
					//necesito los codigos para RV, RF, DV, Reportes o simultaneas
					idOper.add(obj.IdCuadraturaDiariaOperaciones);
				}
				HashMap<String, Object> params = new HashMap<>();
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("IdCuadraturaDiariaOperaciones", idOper);
				List<DetalleResumenJSON> rsJson = _sql.query(conn, SQL_SELECT_DETALLE_CUADRATURADIARIAOPERACIONES, params, DetalleResumenJSON.class);
				
				if (rsJson.size() != 0) {
					out.P_OUT_DETALLE_CUADRATURA = (rsJson).get(0).DetalleCuadraturaJSON;					
				}
			}
		}
		catch(Exception e) {
			log.write(e);
			throw e;
		}
		finally {
			//conn.close();
		}
		log.write("TERMINO");
		return out;
	} 
	
	private List<Transacciones> castRuebasAResumen(List<PalosRuebas> paloRuebas) {
	    List<Transacciones> auxiliar = new ArrayList<Transacciones>();
	    
	    for(PalosRuebas palo : paloRuebas) {
	        Transacciones transaccion = new Transacciones();
	        
	        if (palo.FOLIO != null) {
	            transaccion.FolioTransaccion = palo.FOLIO.toString();
	        } else {
	            transaccion.FolioTransaccion = "";
	        }
	        
	        transaccion.FechaOperacionTransaccion = palo.FECHA != null ? palo.FECHA : "";
	        
	        transaccion.Nemotecnico = palo.NEMO != null ? palo.NEMO : "";
	        transaccion.CantidadTransaccion = palo.CANTIDAD != null ? palo.CANTIDAD.setScale(8,RoundingMode.DOWN): BigDecimal.ZERO;
	        transaccion.MontoTransaccion = palo.MONTO != null ? palo.MONTO.setScale(8,RoundingMode.DOWN) : BigDecimal.ZERO;
	        transaccion.Descripcion = "Operación no encontrada en Optimus";
	        transaccion.ValorRescateAsignadoTransaccion =BigDecimal.ZERO ;
	        transaccion.ValorRescateTransaccion = BigDecimal.ZERO;
	        transaccion.MontoAsignadoTransaccion =BigDecimal.ZERO;
	        transaccion.CantidadAsignadaTransaccion = BigDecimal.ZERO;
	        transaccion.IdMonedaTransaccion = "CLP";
	        
	        auxiliar.add(transaccion);
	    }
	    
	    return auxiliar;
	}
	
	private List<PalosRuebas> compararTransaccionesRuebasVsOptimus(List<Transacciones> transOpt,List<PalosRuebas>transRuebas) {
		   Set<String> foliosOpt = transOpt.stream()
		       .map(trans -> trans.FolioTransaccion)
		       .filter(folio -> folio != null && !folio.trim().isEmpty())
		       .collect(Collectors.toSet());

		   transRuebas.removeIf(rueba -> {
		       if (rueba.FOLIO == null) {
		           return false;
		       }
		       String folioRuebas = rueba.FOLIO.toString();
		       return foliosOpt.contains(folioRuebas);
		   });
		return transRuebas;
	}

	private List<Transacciones> consultaTransaccionesFeriado(Connection conn, Date p_FechaInicio) throws SQLException {
		logger.trace("INICIO::CuadraturaDiaria.consultaTransaccionesFeriado");
		long inicioMetodo = System.currentTimeMillis();
		SQL _sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaInicio", p_FechaInicio);
		List<Transacciones> trans = _sql.query(conn, SQL_SELECT_TRANSACCIONES_FERIADO, params, Transacciones.class);
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::CuadraturaDiaria.crearMovimientosEnTransito :" , finMetodo-inicioMetodo);
		
		return trans;		
	}

	public List<Transacciones> cuadraturaTransacciones(List<Transacciones> trans, List<Pivote> vPivot){
		List<Transacciones> transDescuadradasRVRF = new ArrayList<>();
		BigDecimal pivot = vPivot.get(0) != null ? new BigDecimal(vPivot.get(0).ValorPivot) : BigDecimal.ZERO;
		//Almacenar el total de transacciones descuadradas para el resumen.
		for(Transacciones transDescuadradas: trans){
			if("HISTORICO".equals(transDescuadradas.TipoRegistro)) {
				transDescuadradasRVRF.add(transDescuadradas);
			}else if (transDescuadradas.CantidadTransaccion.compareTo(transDescuadradas.CantidadAsignadaTransaccion)!= 0){
				// agrego a los descuadrados
				transDescuadradasRVRF.add(transDescuadradas);
			}else{
				switch (transDescuadradas.Descripcion) {
				case "UI_FALTA_MONTO_ASIGNACION":
					if ((transDescuadradas.MontoTransaccion.subtract(transDescuadradas.MontoAsignadoTransaccion)).compareTo(pivot) > 0){ 
						// agrego a los descuadrados
						transDescuadradasRVRF.add(transDescuadradas);
					}
					break;
				case "UI_SOBRA_MONTO_ASIGNACION":
					if ((transDescuadradas.MontoAsignadoTransaccion.subtract(transDescuadradas.MontoTransaccion)).compareTo(pivot) > 0 ){
						// agrego a los descuadrados
						transDescuadradasRVRF.add(transDescuadradas);
					}
					break;
				default:
					break;
				}
			}
		}
		return transDescuadradasRVRF;
	}

	public List<Asignaciones> cuadraturaAsignaciones(List<Asignaciones> asing){
		List<Asignaciones> asignDescuadradas = new ArrayList<>();
		for(Asignaciones asignDesc: asing){
			if (!asignDesc.Descripcion.equals("")){
				asignDescuadradas.add(asignDesc);
			}
		}
		return asignDescuadradas;
	}
	
	public List<MovimientoComprobante> cuadraturaMovimientoFac(List<MovimientoComprobante> movFac){
		List<MovimientoComprobante> movFactDescuadradas = new ArrayList<>();
		for(MovimientoComprobante movFacDesc: movFac){
			if (!movFacDesc.EstadoFacturacion.equals("FAC")){
				movFactDescuadradas.add(movFacDesc);
			}
		}
		return movFactDescuadradas;
	}
	
	public List<CuadraturaCavali> cuadraturaMovCavali(List<CuadraturaCavali> movCav){
		List<CuadraturaCavali> movCavaliDescuadradas = new ArrayList<>();
		for(CuadraturaCavali movCavDesc: movCav){
			if (movCavDesc.FolioTransaccion.equals("")){
				movCavaliDescuadradas.add(movCavDesc);
			}
		}
		return movCavaliDescuadradas;
	}
	
	public List<Transacciones> consultaTransacciones(Connection conn, Date inFechaInicio, List<String> idCla) throws SQLException{
		SQL _sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaInicio", inFechaInicio);
		params.put("IdClasificacionTipoInstrumento", idCla);
		//params.put("IdBolsa", bolsaPorDefecto);
		List<Transacciones> trans = _sql.query(conn, SQL_SELECT_TRANSACCIONES, params, Transacciones.class);
		
		return trans;
	}
	
	public List<Transacciones> consultaTransaccionesDuplicadas(Connection conn, Date inFechaInicio,List<Transacciones> transacciones) throws SQLException{
		List<Transacciones> duplicadas =
			    transacciones.stream()
			        .filter(t -> !"OD".equals(t.IndicadorOrdenDirecta))
			        .filter(t -> !"OE".equals(t.IndicadorOrdenDirecta))
			        .filter(t -> !"COMSIM".equals(t.IdTipoOperacion))
			        .filter(t -> !"VENSIM".equals(t.IdTipoOperacion))
			        .collect(Collectors.groupingBy(t ->
			            t.FolioTransaccion
			        ))
			        .values()
			        .stream()
			        .filter(lista -> lista.size() > 1)
			        .flatMap(List::stream)
			        .collect(Collectors.toList());
		
		return duplicadas;
	}
	
	public List<Transacciones> consultaTransaccionesOTC(Connection conn, Date inFechaInicio, List<String> idCla, String bolsaPorDefecto) throws SQLException{
		SQL _sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaInicio", inFechaInicio);
		params.put("IdBolsa", bolsaPorDefecto);
		List<Transacciones> trans = _sql.query(conn, SQL_SELECT_TRANSACCIONESOTC, params, Transacciones.class);
		
		return trans;
	}
	
	public List<PalosRuebas> consultaTransaccionesRuebasRF(Connection conn, Date inFechaInicio, List<Corredora> codCorredora) throws SQLException{
		SQL _sql = new SQL();
		List<PalosRuebas> trans = new ArrayList<PalosRuebas>();
		if(codCorredora.isEmpty()) return trans;
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaConsulta", inFechaInicio);
		params.put("CodCorredora",codCorredora.get(0).CodigoCorredora);
		trans = _sql.query(conn, SQL_SELECT_EVENTOS_BURSATIL_RF, params, PalosRuebas.class);
		return trans;
	}
	
	public List<PalosRuebas> consultaTransaccionesRuebasIF(Connection conn, Date inFechaInicio, List<Corredora> codCorredora) throws SQLException{
		SQL _sql = new SQL();
		List<PalosRuebas> trans = new ArrayList<PalosRuebas>();
		if(codCorredora.isEmpty()) return trans;
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaConsulta", inFechaInicio);
		params.put("CodCorredora",codCorredora.get(0).CodigoCorredora);
		trans = _sql.query(conn, SQL_SELECT_EVENTOS_BURSATIL_IF, params, PalosRuebas.class);
		return trans;
	}
	
	public List<PalosRuebas> consultaTransaccionesRuebasRV(Connection conn, Date inFechaInicio, List<Corredora> codCorredora) throws SQLException{
		SQL _sql = new SQL();
		List<PalosRuebas> trans = new ArrayList<PalosRuebas>();
		if(codCorredora.isEmpty()) return trans;
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaConsulta", inFechaInicio);
		params.put("CodCorredora",codCorredora.get(0).CodigoCorredora);
		trans = _sql.query(conn, SQL_SELECT_EVENTOS_BURSATIL_RV, params, PalosRuebas.class);
		return trans;
	}
	
	
	public List<Asignaciones> consultaAsignaciones(Connection conn, Date inFechaInicio, List<String> idCla) throws SQLException{
		SQL _sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaInicio", inFechaInicio);
		params.put("IdClasificacionTipoInstrumento", idCla);
		//params.put("IdBolsa", bolsaPorDefecto);
		List<Asignaciones> asign = _sql.query(conn, SQL_SELECT_ASIGNACIONES, params, Asignaciones.class);
		
		return asign;
	}
	
	public List<Asignaciones> consultaAsignacionesOTC(Connection conn, Date inFechaInicio, List<String> idCla, String bolsaPorDefecto) throws SQLException{
		SQL _sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaInicio", inFechaInicio);
		params.put("IdBolsa", bolsaPorDefecto);
		List<Asignaciones> asign = _sql.query(conn, SQL_SELECT_ASIGNACIONESOTC, params, Asignaciones.class);
		
		return asign;
	}
	
	public List<MovimientoComprobante> consultaMovimientosFacturar(Connection conn, Date inFechaInicio, List<String> idCla, String bolsaPorDefecto) throws SQLException{
		SQL _sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaInicio", inFechaInicio);
		params.put("IdClasificacionTipoInstrumento", idCla);
		params.put("IdBolsa", bolsaPorDefecto);
		List<MovimientoComprobante> asign = _sql.query(conn, SQL_SELECT_MOVIMIENTOS_FACTURAR, params, MovimientoComprobante.class);
		
		return asign;
	}
	
	public List<MovimientoComprobante> consultaMovimientosFacturarOTC(Connection conn, Date inFechaInicio, List<String> idCla, String bolsaPorDefecto) throws SQLException{
		SQL _sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaInicio", inFechaInicio);
		params.put("IdBolsa", bolsaPorDefecto);
		List<MovimientoComprobante> asign = _sql.query(conn, SQL_SELECT_MOVIMIENTOS_FACTURAROTC, params, MovimientoComprobante.class);
		
		return asign;
	}
	
	public List<CuadraturaCavali> consultaCuadraturaCavali (Connection conn, Date inFechaInicio) throws SQLException{
		SQL _sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaOperacion", inFechaInicio);
		List<CuadraturaCavali> asign = _sql.query(conn, SQL_SELECT_CUADRATURA_CAVALI, params, CuadraturaCavali.class);
		
		return asign;
	}
	
	private List<Corredora> consultaCorredora(Connection conn) throws SQLException{
		SQL _sql = new SQL(); 
		HashMap<String, Object> params = new HashMap<>();
		List<Corredora> codCorerdora = _sql.query(conn, SQL_SELECT_CODIGOCORREDORA,params, Corredora.class);
		return codCorerdora;
	}
	
	public void AgregarLog(Connection con, CuadraturaDiariaEntrada in) {
		try {
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			Long IdCabeceraLog =  in.p_IdUsuario;
			//CABECERA
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				
			log.IdCabeceraLog = IdCabeceraLog;
			log.IdModulo = "ORD"; 
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = FechaSistema;
			log.IdUsuarioAtendido = 0L; 
			log.IdCliente = 0L; 
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "CUADRATURACIERREDIARIO";
			log.EstadoRegistro = "";
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.Nemo = "";
			log.IdProceso = 0L;
			log.EstadoProceso = "ING";
			log.IdPersona = 0L;
			log.IdTipoLog = "INFORMATIVO"; 
			log.NombreProceso = ""; 
			//AÑADIR AL LOG
			entradaAgregarLog.p_Log.add(log);
			//REGISTRO
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
//		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdUsuario\":97,\"p_IdEmpresa\":1,\"p_IdModulo\":\"ORD\",\"p_FechaInicio\":\"2025-10-09\",\"p_IdProceso\":[{\"IdCuadraturaDiariaOperaciones\":\"CUARUETRANRV\"}],\"p_TipoLlamado\":\"Proceso\"}";
			CuadraturaDiariaEntrada Objin = gson.fromJson(json, CuadraturaDiariaEntrada.class);
			
			CuadraturaDiaria demo=new CuadraturaDiaria();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CuadraturaDiariaSalida ObjOut=demo.logica(con, Objin);
 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
	    	}
		System.out.println("== END ==");
	}

}