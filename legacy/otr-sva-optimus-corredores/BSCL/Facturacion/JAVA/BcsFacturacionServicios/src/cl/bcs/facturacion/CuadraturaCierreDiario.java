package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

public class CuadraturaCierreDiario implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CuadraturaCierreDiario.class);
	
	private final String SQL_SELECT_CUADRATURADIARIAOPERACIONES		 	= loadResourceToString("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE 				= loadResourceToString("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectMovimientoComprobante.sql");
	private final String SQL_DELETE_CUADRATURADIARIAOPERACIONES 		= loadResourceToString("cl/bcs/facturacion/sql/CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql");
	private final String SQL_INSERT_CUADRATURADIARIAOPERACIONES 		= loadResourceToString("cl/bcs/facturacion/sql/CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_DETALLE_CUADRATURADIARIAOPERACIONES = loadResourceToString("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectJSONCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_CUADRATURAS_FACTURACION				= loadResourceToString("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectCuadraturasFacturacion.sql");
	//CUADTRATURA TRANSACCIONES - FACTURAS
	private final String SQL_SELECT_CUADRATURA_TRANSACCION_FACTURACION  = loadResourceToString("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectTransaccionFacturacion.sql");
	private final String SQL_SELECT_CUADRATURA_SPOT_FACTURACION  		= loadResourceToString("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectSpotFacturacion.sql");
	private final String SQL_SELECT_CUADRATURA_SUSCRIPCION_FACTURACION  = loadResourceToString("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectSuscripcionFacturacion.sql");
	private final String SQL_SELECT_CUADRATURA_PREPAGO_BONO_FACTURACION = loadResourceToString("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectPrepagoBonoFacturacion.sql");
	private final String SQL_SELECT_CUADRATURA_TRANSACCION_FACTURACION_PENDIENTE 	= loadResourceToString("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectTransaccionFacturacionPendiente.sql");
	private final String SQL_SELECT_CUADRATURA_TRANSACCION_FACTURACION_PENDIENTE_DTE = loadResourceToString("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectTransaccionFacturacionPendiente_dte.sql");
	private final String SQL_SELECT_CUADRATURA_PRESTAMOS_PENDIENTES 	= loadResourceToString("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectPrestamoFacturacionPendiente.sql");
	//
	
	public static class CuadraturaDiariaObj{
		public String IdCuadraturaDiariaOperaciones;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuadraturaCierreDiarioEntrada {
		public Long p_IdUsuario;
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Date p_FechaInicio;
		public List<CuadraturaDiariaObj> p_IdProceso;
		public String p_TipoLlamado;
		
		public CuadraturaCierreDiarioEntrada() {
			this.p_IdProceso = new ArrayList<CuadraturaDiariaObj>();
		}

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
	
	public static class MovimientoComprobante{
		public Long 		IdMovimientoComprobante;
		public String 		IdTipoComprobante;
		public String 		NombreTipoComprobante;
		public String 		EstadoFacturacion;
		public String 		NombreEstadoFacturacion;
		public String 		IdConceptoMovimiento;
		public String 		NombreConceptoMovimiento;
		public String 		DocumentoIdentidad;
		public String 		TipoDocumentoIdentidad;
		public String 		NombreLargoCliente;
		public String 		Descripcion;
		public String 		IdEstadoFacturacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TransaccionFacturacion{
		public Long IdTransaccion;
		public String FolioTransaccion;
		public Date FechaTransaccion;
		public Timestamp FechaHoraTransaccion;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreBolsa;
		public String TipoOperacion;
		public String IdCondicionLiquidacion;
		public String IdMoneda;
		public String NombreTipoInstrumento;
		public String NemotecnicoInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal Monto;
		public BigDecimal CantidadAsignada;
		public BigDecimal CantidadPorAsignar;
		public BigDecimal CantidadEnviada;
		public BigDecimal CantidadPorEnviar;
		public BigDecimal CantidadFacturada;
		public BigDecimal CantidadPorFacturar;
		public BigDecimal MontoFacturado;
		public BigDecimal MontoPorFacturar;
		
		public String Descripcion;
		public String TipoRegistro;
		public String IdEstadoRegistro;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class SpotFacturacion{
		public String 		IdTipoOperacion;
		public String 		TipoOperacion;
		public Long 		FolioTransaccion;
		public Long 		IdOperacionSpotVigente;
		public Date 		FechaHoraTransaccion;
		public String 		TipoComprobanteFacturacion;
		public Long			IdComprobanteFacturacion;
		public String		EstadoSpot;
		public String		EstadoFacturacion;
		public String 		Descripcion;
		
		public String       NombreBolsa;
		public String       IdMoneda;
		public String       NemotecnicoInstrumento;
		public BigDecimal   Cantidad;
		public BigDecimal   Precio;
		public BigDecimal   Monto;
		public BigDecimal   MontoFacturado;
		public BigDecimal   MontoPorFacturar;
		public BigDecimal   CantidadAsignada;
		public BigDecimal   CantidadPorAsignar;
		public BigDecimal   CantidadEnviada;
		public BigDecimal	CantidadFacturada;
		public BigDecimal   CantidadPorEnviar;
		public BigDecimal   CantidadPorFacturar;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	public static class SuscripcionFacturacion{
		public Long IdTransaccion;
		public Long FolioTransaccion;
		public Date FechaTransaccion;
		public Timestamp FechaHoraTransaccion;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreBolsa;
		public String TipoOperacion;
		public String IdCondicionLiquidacion;
		public String IdMoneda;
		public String NombreTipoInstrumento;
		public String NemotecnicoInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal Monto;
		public BigDecimal CantidadAsignada;
		public BigDecimal CantidadPorAsignar;
		public BigDecimal CantidadEnviada;
		public BigDecimal CantidadPorEnviar;
		public BigDecimal CantidadFacturada;
		public BigDecimal CantidadPorFacturar;
		public BigDecimal MontoFacturado;
		public BigDecimal MontoPorFacturar;
		public String Descripcion;	
		//Los Requiero
		public String		EstadoEventoCapital;
		public String		EstadoFacturacion;

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class PrepagoBonoFacturacion{
		public Long IdTransaccion;
		public Long FolioTransaccion;
		public Date FechaTransaccion;
		public Timestamp FechaHoraTransaccion;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreBolsa;
		public String TipoOperacion;
		public String IdCondicionLiquidacion;
		public String IdMoneda;
		public String NombreTipoInstrumento;
		public String NemotecnicoInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal Monto;
		public BigDecimal CantidadAsignada;
		public BigDecimal CantidadPorAsignar;
		public BigDecimal CantidadEnviada;
		public BigDecimal CantidadPorEnviar;
		public BigDecimal CantidadFacturada;
		public BigDecimal CantidadPorFacturar;
		public BigDecimal MontoFacturado;
		public BigDecimal MontoPorFacturar;
		public String Descripcion;	
		//Los Requiero
		public String		EstadoEventoCapital;
		public String		EstadoFacturacion;

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class PrestamosFacturacion{
	    public Long IdMovimientoComprobante;
	    public Long IdTransaccion;
	    public String FolioTransaccion; 
	    public Timestamp FechaHoraTransaccion;  
	    public Timestamp FechaHoraMovimientoComprobante; 
	    public String IdClasificacionTipoInstrumento;
	    public String IdTipoInstrumento ; 
	    public String NombreBolsa; 
	    public String IdTipoOperacion;  
	    public String IdCondicionLiquidacion;  
	    public String IdMoneda;  
	    public String NombreTipoInstrumento;  
	    public String NemotecnicoInstrumento; 
	    public BigDecimal Cantidad;  
	    public BigDecimal Precio;  
	    public BigDecimal Monto ; 
	    public String EstadoFacturacion;
	    public BigDecimal CantidadAsignada;
	    public BigDecimal CantidadPorAsignar;
	    public BigDecimal CantidadEnviada;
	    public BigDecimal CantidadFacturada;
	    public BigDecimal CantidadPorEnviar;
	    public BigDecimal CantidadPorFacturar;
	    public BigDecimal MontoFacturado;
	    public BigDecimal MontoPorFacturar;
	    
		public String Descripcion;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
		
	public static class objCuadraturaDiariaOperaciones{
		public Long IdEmpresa; 
		public String IdCuadraturaDiariaOperaciones;
		public Date FechaEjecucion;
		public Long TotalOperaciones; 
		public Long OperacionesConProblemas;
		public String Resultado;
		public String DetalleCuadraturaJson;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuadraturaCierreDiarioSalida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public String						P_OUT_DETALLE_CUADRATURA;
		
		public CuadraturaCierreDiarioSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TipoCuadratura{
		public String IdCuadraturaDiariaOperaciones;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class objPendienteDTE{
		public Long IdTransaccion;
		public Long FolioTransaccion;
		public Date FechaTransaccion;
		public Timestamp FechaHoraTransaccion;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreBolsa;
		public String TipoOperacion;
		public String IdCondicionLiquidacion;
		public String IdMoneda;
		public String NombreTipoInstrumento;
		public String NemotecnicoInstrumento;
		public Long Cantidad;
		public BigDecimal Precio;
		public BigDecimal Monto;
		public BigDecimal CantidadAsignada;
		public BigDecimal CantidadPorAsignar;
		public BigDecimal CantidadEnviada;
		public BigDecimal CantidadPorEnviar;
		public BigDecimal CantidadFacturada;
		public BigDecimal CantidadPorFacturar;
		public BigDecimal MontoFacturado;
		public BigDecimal MontoPorFacturar;
		
		public String Descripcion;
		public String TipoRegistro;
		public String IdEstadoRegistro;
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
		CuadraturaCierreDiarioEntrada in = gson.fromJson(jin.toString(), CuadraturaCierreDiarioEntrada.class);
		CuadraturaCierreDiarioSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public CuadraturaCierreDiarioSalida logica(Connection conn, CuadraturaCierreDiarioEntrada in) throws Exception {
		logger.info("INICIO::CuadraturaCierreDiario");
		long start = System.currentTimeMillis();
		SQL _sql = new SQL();
		CuadraturaCierreDiarioSalida out = new CuadraturaCierreDiarioSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		
		List<String> idsModuloCuadraturaOrden = new ArrayList<String>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String fechaInicio = sdf.format(in.p_FechaInicio);
			
			//Modo Proceso
			if (in.p_TipoLlamado.equals("Proceso")){
				Long Id = 0L;
				HashMap<String,String> mapMsg = new HashMap<>();
				mapMsg.put("fechaEjecucion", in.p_FechaInicio.toString());
				List<String> idOper = new ArrayList<>();
				for (CuadraturaDiariaObj obj: in.p_IdProceso){
					idOper.add(obj.IdCuadraturaDiariaOperaciones);
				}
				HashMap<String,Object> regResumen = new HashMap<>();
				regResumen.put("IdEmpresa", in.p_IdEmpresa);
				List<TipoCuadratura> rsCuadraturas = _sql.query(conn, SQL_SELECT_CUADRATURAS_FACTURACION, regResumen, TipoCuadratura.class);
				for(TipoCuadratura tp : rsCuadraturas){
					idsModuloCuadraturaOrden.add(tp.IdCuadraturaDiariaOperaciones);
				}
				regResumen.clear();
				regResumen.put("IdEmpresa", in.p_IdEmpresa);
				regResumen.put("IdCuadraturaDiariaOperaciones", idOper);
				List<Resumen> rsResumen = _sql.query(conn, SQL_SELECT_CUADRATURADIARIAOPERACIONES, regResumen, Resumen.class);
				
				List<MovimientoComprobante> certificacionesPendientes = new ArrayList<MovimientoComprobante>();
				List<MovimientoComprobante> movFact = new ArrayList<MovimientoComprobante>();
				List<objCuadraturaDiariaOperaciones> parametrosparaBorrar = new ArrayList<>();
				
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				long posicion = 0;
				for (CuadraturaDiariaObj objProc: in.p_IdProceso){
					if(objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACCOM") || objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACVEN")){

						for (Resumen recoResumen: rsResumen){
							recoResumen.IdUsuario = in.p_IdUsuario;
							if (recoResumen.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
								
								HashMap<String, Object> params = new HashMap<>();
								params.put("FechaInicio", in.p_FechaInicio);
								params.put("IdTipoOperacion", recoResumen.TipoOperacion);
								movFact = _sql.query(conn, SQL_SELECT_MOVIMIENTO_COMPROBANTE, params, MovimientoComprobante.class);
								String vResultado = new String();
								
								certificacionesPendientes = cuadraturaMovimientoFac(movFact);
								
								if (movFact.size
										()==0){
									vResultado = "UI_SIN_OPERACIONES";
								}
								else if(certificacionesPendientes.size() == 0){
									vResultado = "UI_CUADRADO";
								}else {
									vResultado = "UI_DESCUADRADO";
								}
								
								recoResumen.FechaEjecucion = in.p_FechaInicio;
								recoResumen.TotalOperaciones = (long)movFact.size(); 
								recoResumen.OperacionesConProblemas = (long)certificacionesPendientes.size();
								recoResumen.Resultado = vResultado;
								recoResumen.DetalleCuadraturaJSON = gson.toJson(certificacionesPendientes);
								recoResumen.Orden = posicion;
								posicion++;
								
								objCuadraturaDiariaOperaciones oDelete = new objCuadraturaDiariaOperaciones();	
								oDelete.IdCuadraturaDiariaOperaciones = objProc.IdCuadraturaDiariaOperaciones;
								oDelete.FechaEjecucion = in.p_FechaInicio;
								parametrosparaBorrar.add(oDelete);
							}
						}
					}else if(idsModuloCuadraturaOrden.contains(objProc.IdCuadraturaDiariaOperaciones)){
						for (Resumen recoResumen: rsResumen){
							recoResumen.IdUsuario = in.p_IdUsuario;
							if (recoResumen.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
								List<TransaccionFacturacion> transacciones			= new ArrayList<>();
								List<SpotFacturacion> 		 spot					= new ArrayList<>();
								List<SuscripcionFacturacion> suscripcion			= new ArrayList<>();	
								List<TransaccionFacturacion> transaccionesProblemas = new ArrayList<>();
								List<SpotFacturacion>        spotProblemas			= new ArrayList<>();
								List<SuscripcionFacturacion> suscripcionProblemas	= new ArrayList<>();
								List<PrepagoBonoFacturacion> prepagoBono			= new ArrayList<>();
								List<PrepagoBonoFacturacion> prepagoBonoProblemas	= new ArrayList<>();
								List<TransaccionFacturacion> transaccionesPendientes= new ArrayList<>();
								List<TransaccionFacturacion> transaccionesPendientesProblemas= new ArrayList<>();
								List<PrestamosFacturacion> prestamosPendientes	= new ArrayList<>();
								List<PrestamosFacturacion> prestamosPendientesProblemas= new ArrayList<>();
								//PrepagoBonoFacturacion
								
								if(!(objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACCOMSPT") || objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACVENSPT") || objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACSUSOSA") || objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACVEPRBO") || objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACTRANPEN") || objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACPRESTA"))){
									transacciones						= cuadraturaTransaccionesFacturadas(conn,recoResumen.TipoOperacion,fechaInicio);
									transaccionesProblemas				= transacciones.stream().map( trx -> (trx.Descripcion.equalsIgnoreCase("UI_CUADRADO")) ? null : trx ).filter(Objects::nonNull).collect(Collectors.toList());
									
									recoResumen.TotalOperaciones		= (long) transacciones.size(); 
									recoResumen.OperacionesConProblemas	= (long) transaccionesProblemas.size();
									recoResumen.Resultado 				= (transaccionesProblemas.isEmpty()) ? "UI_CUADRADO" : "UI_DESCUADRADO";
									recoResumen.DetalleCuadraturaJSON	= gson.toJson(transaccionesProblemas);
								}else if (objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACCOMSPT") || objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACVENSPT")){
									spot 								= cuadraturaSpotFacturadas(conn,recoResumen.TipoOperacion,fechaInicio);
									spotProblemas						= spot.stream().map( trx -> (trx.Descripcion.equalsIgnoreCase("UI_CUADRADO")) ? null : trx ).filter(Objects::nonNull).collect(Collectors.toList());
									
									recoResumen.TotalOperaciones 		= (long) spot.size(); 
									recoResumen.OperacionesConProblemas = (long) spotProblemas.size();
									recoResumen.Resultado 				= (spotProblemas.isEmpty()) ? "UI_CUADRADO" : "UI_DESCUADRADO";
									recoResumen.DetalleCuadraturaJSON 	= gson.toJson(spotProblemas);
								}else if(objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACSUSOSA")){
									suscripcion 								= cuadraturaSuscripcionFacturadas(conn,recoResumen.TipoOperacion,fechaInicio);
									suscripcionProblemas						= suscripcion.stream().map( trx -> (trx.Descripcion.equalsIgnoreCase("UI_CUADRADO")) ? null : trx ).filter(Objects::nonNull).collect(Collectors.toList());
									
									recoResumen.TotalOperaciones 		= (long) suscripcion.size(); 
									recoResumen.OperacionesConProblemas = (long) suscripcionProblemas.size();
									recoResumen.Resultado 				= (suscripcionProblemas.isEmpty()) ? "UI_CUADRADO" : "UI_DESCUADRADO";
									recoResumen.DetalleCuadraturaJSON 	= gson.toJson(suscripcionProblemas);
								}
								else if(objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACVEPRBO")){
									prepagoBono 								= cuadraturaPrepagoBonoFacturadas(conn,recoResumen.TipoOperacion,fechaInicio);
									prepagoBonoProblemas						= prepagoBono.stream().map( trx -> (trx.Descripcion.equalsIgnoreCase("UI_CUADRADO")) ? null : trx ).filter(Objects::nonNull).collect(Collectors.toList());
									
									recoResumen.TotalOperaciones 		= (long) prepagoBono.size(); 
									recoResumen.OperacionesConProblemas = (long) prepagoBonoProblemas.size();
									recoResumen.Resultado 				= (prepagoBonoProblemas.isEmpty()) ? "UI_CUADRADO" : "UI_DESCUADRADO";
									recoResumen.DetalleCuadraturaJSON 	= gson.toJson(prepagoBonoProblemas);
								}
								else if(objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACTRANPEN")) {
									transaccionesPendientes				= cuadraturaTransaccionesFacturadasPendientes(conn,fechaInicio);
									transaccionesPendientesProblemas	= transaccionesPendientes.stream().filter(trx->!trx.Descripcion.equalsIgnoreCase("UI_CUADRADO")).collect(Collectors.toList());
									recoResumen.TotalOperaciones		= (long) transaccionesPendientes.size();
									recoResumen.OperacionesConProblemas	= (long) transaccionesPendientesProblemas.size();
									recoResumen.Resultado 				= (transaccionesPendientesProblemas.isEmpty()) ? "UI_CUADRADO" : "UI_DESCUADRADO";
									recoResumen.DetalleCuadraturaJSON	= gson.toJson(transaccionesPendientesProblemas);
								}
								else if(objProc.IdCuadraturaDiariaOperaciones.equalsIgnoreCase("CUAMOVFACPRESTA")) {
									prestamosPendientes				= cuadraturaPrestamosPendientes(conn,recoResumen.TipoOperacion,fechaInicio);
									prestamosPendientesProblemas	= prestamosPendientes.stream().map( trx -> (trx.Descripcion.equalsIgnoreCase("UI_CUADRADO")) ? null : trx ).filter(Objects::nonNull).collect(Collectors.toList());
									
									recoResumen.TotalOperaciones		= (long) prestamosPendientes.size();
									recoResumen.OperacionesConProblemas	= (long) prestamosPendientesProblemas.size();
									recoResumen.Resultado 				= (prestamosPendientesProblemas.isEmpty()) ? "UI_CUADRADO" : "UI_DESCUADRADO";
									recoResumen.DetalleCuadraturaJSON	= gson.toJson(prestamosPendientesProblemas);
								};
								
								recoResumen.FechaEjecucion = in.p_FechaInicio;

								recoResumen.Orden = posicion;
								posicion++;
							}
						}
						objCuadraturaDiariaOperaciones oDelete = new objCuadraturaDiariaOperaciones();	
						oDelete.IdCuadraturaDiariaOperaciones = objProc.IdCuadraturaDiariaOperaciones;
						oDelete.FechaEjecucion = in.p_FechaInicio;
						parametrosparaBorrar.add(oDelete);
					}
				}
				//CONTAR AQUELLOS QUE ESTEN PENDIENTES DE ENVIAR A DTE 
				List<objPendienteDTE> transaccionesPendientesDTE = cuadraturaTransaccionesFacturadasPendientesDTE(conn,fechaInicio);
				
				List<objPendienteDTE> transaccionesPendientesDTEProblemas = transaccionesPendientesDTE.stream().map( trx -> (trx.Descripcion.equalsIgnoreCase("UI_CUADRADO")) ? null : trx ).filter(Objects::nonNull).collect(Collectors.toList());
					Resumen recoResumen = new Resumen();
					
					recoResumen.TotalOperaciones		= (long) transaccionesPendientesDTE.size();
					recoResumen.OperacionesConProblemas	= (long) transaccionesPendientesDTEProblemas.size();
					recoResumen.Resultado 				= (transaccionesPendientesDTEProblemas.isEmpty()) ? "UI_CUADRADO" : "UI_DESCUADRADO";
					recoResumen.DetalleCuadraturaJSON	= gson.toJson(transaccionesPendientesDTEProblemas);
					recoResumen.FechaEjecucion = in.p_FechaInicio;
					recoResumen.IdCuadraturaDiariaOperaciones="CUAMOVFACDTENPEN";
					recoResumen.TipoOperacion="COMADT";
					recoResumen.IdUsuario = in.p_IdUsuario;
					recoResumen.Orden = (long) (rsResumen.size()+1);
					recoResumen.OperacionCuadraturaDiariaOperaciones = "UI_CUADRATURA_MOVIMIENTO_COMPROBANTE_DTE_PENDIENTE";
					recoResumen.IdEmpresa=in.p_IdEmpresa;
						rsResumen.add(recoResumen); 
				
				if (parametrosparaBorrar.size() > 0){ 
					_sql.batch(conn, SQL_DELETE_CUADRATURADIARIAOPERACIONES, parametrosparaBorrar, objCuadraturaDiariaOperaciones.class);
					_sql.batch(conn, SQL_INSERT_CUADRATURADIARIAOPERACIONES, rsResumen, Resumen.class);
				}
				
				if(rsResumen.size() > 0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO",mapMsg,conn,in.p_IdEmpresa));
				}else {
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_CERO_OPERACIONES_CUADRATURA" ,mapMsg,conn,in.p_IdEmpresa));
				}
				
				AgregarLog(conn, in);
				
			}else if (in.p_TipoLlamado.equalsIgnoreCase("consulta")){
				List<String> idOper = new ArrayList<>();
				for (CuadraturaDiariaObj obj: in.p_IdProceso){
					idOper.add(obj.IdCuadraturaDiariaOperaciones);
				}
				
				HashMap<String, Object> params = new HashMap<>();
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("IdCuadraturaDiariaOperaciones", idOper);
				params.put("FechaInicio", in.p_FechaInicio);
				List<DetalleResumenJSON> rsJson = _sql.query(conn, SQL_SELECT_DETALLE_CUADRATURADIARIAOPERACIONES, params, DetalleResumenJSON.class);
				
				if (rsJson.size() != 0) {
					out.P_OUT_DETALLE_CUADRATURA = (rsJson).get(0).DetalleCuadraturaJSON;					
				}
			}
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("Error",e);
			throw e;
		}
		long end =  System.currentTimeMillis() - start;
		logger.info("FIN::CuadraturaCierreDiario - " + end);
		
		return out;
	}
	
	private List<TransaccionFacturacion> cuadraturaTransaccionesFacturadas(Connection conn, String idTipoOperacion, String fechaCuadratura) throws Exception{
		logger.info("INICIO::CuadraturaCierreDiario.cuadraturaTransaccionesFacturadas");
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaInicio", fechaCuadratura);
		params.put("IdTipoOperacion", idTipoOperacion);
		List<TransaccionFacturacion> transacciones = sql.query(conn, SQL_SELECT_CUADRATURA_TRANSACCION_FACTURACION, params, TransaccionFacturacion.class);
		transacciones.parallelStream().forEach(transaccion->{
			try {
				transaccion.MontoPorFacturar = MonedaFormato.formatear(conn, transaccion.MontoPorFacturar, MonedaFormato.MONTO, transaccion.IdMoneda).valor;
				transaccion.Monto = MonedaFormato.formatear(conn, transaccion.Monto, MonedaFormato.MONTO, transaccion.IdMoneda).valor;
				transaccion.MontoFacturado = MonedaFormato.formatear(conn, transaccion.MontoFacturado, MonedaFormato.MONTO, transaccion.IdMoneda).valor;
				
				if(transaccion.CantidadPorFacturar.compareTo(BigDecimal.ZERO) != 0){
					transaccion.Descripcion = (transaccion.CantidadPorFacturar.compareTo(BigDecimal.ZERO) < 0) ? "UI_SOBRA_CANTIDAD_FATURADA" : "UI_FALTA_CANTIDAD_FACTURADA";
				}else if(transaccion.MontoPorFacturar.compareTo(BigDecimal.ZERO)!=0) {
					transaccion.Descripcion = (transaccion.MontoPorFacturar.compareTo(BigDecimal.ZERO) < 0) ? "UI_SOBRA_MONTO_FACTURADO" : "UI_FALTA_MONTO_FACTURADO";
				}else {
					transaccion.Descripcion = "UI_CUADRADO";
				}
			} catch (Exception e) {
				logger.error("ERROR",e);
				transaccion.Descripcion = "UI_ERROR";
			}
		});
		
		logger.info("FIN::CuadraturaCierreDiario.cuadraturaTransaccionesFacturadas");
		
		return transacciones;
	}
	
	private List<SpotFacturacion> cuadraturaSpotFacturadas(Connection conn, String idTipoOperacion, String fechaCuadratura) throws SQLException{
		logger.info("INICIO::CuadraturaCierreDiario.cuadraturaSpotFacturadas");
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaInicio", fechaCuadratura);
		params.put("IdOperacion", idTipoOperacion);
		List<SpotFacturacion> operacionesSpot = sql.query(conn, SQL_SELECT_CUADRATURA_SPOT_FACTURACION, params, SpotFacturacion.class);

		operacionesSpot.parallelStream().forEach( operacionSpot -> {
			if(operacionSpot.EstadoSpot.equals("VIG") && operacionSpot.EstadoFacturacion.equals("ING")){
				operacionSpot.Descripcion = (operacionSpot.EstadoSpot.equals("VIG") && operacionSpot.EstadoFacturacion.equals("ING")) ? "UI_OPERACION_SPOT_NO_FACTURADA" : "";
			}else if(operacionSpot.EstadoSpot.equals("VIG") && operacionSpot.EstadoFacturacion.equals("FAC")) {
				operacionSpot.Descripcion = (operacionSpot.EstadoSpot.equals("VIG") && operacionSpot.EstadoFacturacion.equals("FAC")) ? "UI_CUADRADO" : "";
			}else if(operacionSpot.EstadoSpot.equals("VIG") && operacionSpot.EstadoFacturacion.equals("ANU")) {
				operacionSpot.Descripcion = (operacionSpot.EstadoSpot.equals("VIG") && operacionSpot.EstadoFacturacion.equals("ANU")) ? "UI_SPOT_VIGENTE_COMPROBANTE_ANULADO" : "";
			}else if(operacionSpot.EstadoSpot.equals("VIG") && operacionSpot.EstadoFacturacion.equals("BLO")) {
				operacionSpot.Descripcion = (operacionSpot.EstadoSpot.equals("VIG") && operacionSpot.EstadoFacturacion.equals("BLO")) ? "UI_SPOT_VIGENTE_COMPROBANTE_BLOQUEADO" : "";
			}else if(operacionSpot.EstadoSpot.equals("ANU") && operacionSpot.EstadoFacturacion.equals("ING")) {
				operacionSpot.Descripcion = (operacionSpot.EstadoSpot.equals("ANU") && operacionSpot.EstadoFacturacion.equals("ING")) ? "UI_SPOT_ANULADO_COMPROBANTE_VIGENTE" : "";
			}else if(operacionSpot.EstadoSpot.equals("ANU") && operacionSpot.EstadoFacturacion.equals("FAC")) {
				operacionSpot.Descripcion = (operacionSpot.EstadoSpot.equals("ANU") && operacionSpot.EstadoFacturacion.equals("FAC")) ? "UI_SPOT_ANULADO_COMPROBANTE_FACTURADO" : "";
			}else if(operacionSpot.EstadoSpot.equals("ANU") && operacionSpot.EstadoFacturacion.equals("ANU")) {
				operacionSpot.Descripcion = (operacionSpot.EstadoSpot.equals("ANU") && operacionSpot.EstadoFacturacion.equals("ANU")) ? "UI_CUADRADO" : "";
			}else if(operacionSpot.EstadoSpot.equals("ANU") && operacionSpot.EstadoFacturacion.equals("BLO")) {
				operacionSpot.Descripcion = (operacionSpot.EstadoSpot.equals("ANU") && operacionSpot.EstadoFacturacion.equals("BLO")) ? "UI_SPOT_ANULADO_COMPROBANTE_BLOQUEADO" : "";
			}else {
				operacionSpot.Descripcion = "UI_CUADRADO";
			}
		});
		
		logger.info("FIN::CuadraturaCierreDiario.cuadraturaSpotFacturadas");
		return operacionesSpot;
	};
	
	private List<SuscripcionFacturacion> cuadraturaSuscripcionFacturadas(Connection conn, String idTipoOperacion, String fechaCuadratura) throws SQLException{
		logger.info("INICIO::CuadraturaCierreDiario.cuadraturaSuscripcionFacturadas");
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaInicio", fechaCuadratura);
		params.put("IdOperacion", idTipoOperacion);
		List<SuscripcionFacturacion> operacionesSuscripcion = sql.query(conn, SQL_SELECT_CUADRATURA_SUSCRIPCION_FACTURACION, params, SuscripcionFacturacion.class);

		operacionesSuscripcion.parallelStream().forEach( operacionSuscripcion -> {
			if(operacionSuscripcion.EstadoEventoCapital.equals("VIG") && operacionSuscripcion.EstadoFacturacion.equals("ING")){
				operacionSuscripcion.Descripcion = (operacionSuscripcion.EstadoEventoCapital.equals("VIG") && operacionSuscripcion.EstadoFacturacion.equals("ING")) ? "UI_SUSCRIPCION_NO_FACTURADA" : "";
			}else if(operacionSuscripcion.EstadoEventoCapital.equals("VIG") && operacionSuscripcion.EstadoFacturacion.equals("FAC")) {
				operacionSuscripcion.Descripcion = (operacionSuscripcion.EstadoEventoCapital.equals("VIG") && operacionSuscripcion.EstadoFacturacion.equals("FAC")) ? "UI_CUADRADO" : "";
			}else if(operacionSuscripcion.EstadoEventoCapital.equals("VIG") && operacionSuscripcion.EstadoFacturacion.equals("ANU")) {
				operacionSuscripcion.Descripcion = (operacionSuscripcion.EstadoEventoCapital.equals("VIG") && operacionSuscripcion.EstadoFacturacion.equals("ANU")) ? "UI_SUSCRIPCION_VIGENTE_COMPROBANTE_ANULADO" : "";
			}else if(operacionSuscripcion.EstadoEventoCapital.equals("VIG") && operacionSuscripcion.EstadoFacturacion.equals("BLO")) {
				operacionSuscripcion.Descripcion = (operacionSuscripcion.EstadoEventoCapital.equals("VIG") && operacionSuscripcion.EstadoFacturacion.equals("BLO")) ? "UI_SUSCRIPCION_VIGENTE_COMPROBANTE_BLOQUEADO" : "";
			}else if(operacionSuscripcion.EstadoEventoCapital.equals("ANU") && operacionSuscripcion.EstadoFacturacion.equals("ING")) {
				operacionSuscripcion.Descripcion = (operacionSuscripcion.EstadoEventoCapital.equals("ANU") && operacionSuscripcion.EstadoFacturacion.equals("ING")) ? "UI_SUSCRIPCION_ANULADA_COMPROBANTE_VIGENTE" : "";
			}else if(operacionSuscripcion.EstadoEventoCapital.equals("ANU") && operacionSuscripcion.EstadoFacturacion.equals("FAC")) {
				operacionSuscripcion.Descripcion = (operacionSuscripcion.EstadoEventoCapital.equals("ANU") && operacionSuscripcion.EstadoFacturacion.equals("FAC")) ? "UI_SUSCRIPCION_ANULADA_COMPROBANTE_FACTURADO" : "";
			}else if(operacionSuscripcion.EstadoEventoCapital.equals("ANU") && operacionSuscripcion.EstadoFacturacion.equals("ANU")) {
				operacionSuscripcion.Descripcion = (operacionSuscripcion.EstadoEventoCapital.equals("ANU") && operacionSuscripcion.EstadoFacturacion.equals("ANU")) ? "UI_CUADRADO" : "";
			}else if(operacionSuscripcion.EstadoEventoCapital.equals("ANU") && operacionSuscripcion.EstadoFacturacion.equals("BLO")) {
				operacionSuscripcion.Descripcion = (operacionSuscripcion.EstadoEventoCapital.equals("ANU") && operacionSuscripcion.EstadoFacturacion.equals("BLO")) ? "UI_SUSCRIPCION_ANULADA_COMPROBANTE_BLOQUEADO" : "";
			}else {
				operacionSuscripcion.Descripcion = "UI_CUADRADO";
			}
		});
		logger.info("FIN::CuadraturaCierreDiario.cuadraturaSuscripcionFacturadas");
		return operacionesSuscripcion;
	};
	
	private List<PrepagoBonoFacturacion> cuadraturaPrepagoBonoFacturadas(Connection conn, String idTipoOperacion, String fechaCuadratura) throws SQLException{
		logger.info("INICIO::CuadraturaCierreDiario.cuadraturaPrepagoBonoFacturadas");
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaInicio", fechaCuadratura);
		params.put("IdOperacion", idTipoOperacion);
		List<PrepagoBonoFacturacion> operacionesPrepago = sql.query(conn, SQL_SELECT_CUADRATURA_PREPAGO_BONO_FACTURACION, params, PrepagoBonoFacturacion.class);

		operacionesPrepago.parallelStream().forEach( operacionPrepago -> {
			if(operacionPrepago.EstadoEventoCapital.equals("VIG") && operacionPrepago.EstadoFacturacion.equals("ING")){
				operacionPrepago.Descripcion = (operacionPrepago.EstadoEventoCapital.equals("VIG") && operacionPrepago.EstadoFacturacion.equals("ING")) ? "UI_PREPAGOBONO_NO_FACTURADA" : "";
			}else if(operacionPrepago.EstadoEventoCapital.equals("VIG") && operacionPrepago.EstadoFacturacion.equals("FAC")) {
				operacionPrepago.Descripcion = (operacionPrepago.EstadoEventoCapital.equals("VIG") && operacionPrepago.EstadoFacturacion.equals("FAC")) ? "UI_CUADRADO" : "";
			}else if(operacionPrepago.EstadoEventoCapital.equals("VIG") && operacionPrepago.EstadoFacturacion.equals("ANU")) {
				operacionPrepago.Descripcion = (operacionPrepago.EstadoEventoCapital.equals("VIG") && operacionPrepago.EstadoFacturacion.equals("ANU")) ? "UI_PREPAGOBONO_VIGENTE_COMPROBANTE_ANULADO" : "";
			}else if(operacionPrepago.EstadoEventoCapital.equals("VIG") && operacionPrepago.EstadoFacturacion.equals("BLO")) {
				operacionPrepago.Descripcion = (operacionPrepago.EstadoEventoCapital.equals("VIG") && operacionPrepago.EstadoFacturacion.equals("BLO")) ? "UI_PREPAGOBONO_VIGENTE_COMPROBANTE_BLOQUEADO" : "";
			}else if(operacionPrepago.EstadoEventoCapital.equals("ANU") && operacionPrepago.EstadoFacturacion.equals("ING")) {
				operacionPrepago.Descripcion = (operacionPrepago.EstadoEventoCapital.equals("ANU") && operacionPrepago.EstadoFacturacion.equals("ING")) ? "UI_PREPAGOBONO_ANULADA_COMPROBANTE_VIGENTE" : "";
			}else if(operacionPrepago.EstadoEventoCapital.equals("ANU") && operacionPrepago.EstadoFacturacion.equals("FAC")) {
				operacionPrepago.Descripcion = (operacionPrepago.EstadoEventoCapital.equals("ANU") && operacionPrepago.EstadoFacturacion.equals("FAC")) ? "UI_PREPAGOBONO_ANULADA_COMPROBANTE_FACTURADO" : "";
			}else if(operacionPrepago.EstadoEventoCapital.equals("ANU") && operacionPrepago.EstadoFacturacion.equals("ANU")) {
				operacionPrepago.Descripcion = (operacionPrepago.EstadoEventoCapital.equals("ANU") && operacionPrepago.EstadoFacturacion.equals("ANU")) ? "UI_CUADRADO" : "";
			}else if(operacionPrepago.EstadoEventoCapital.equals("ANU") && operacionPrepago.EstadoFacturacion.equals("BLO")) {
				operacionPrepago.Descripcion = (operacionPrepago.EstadoEventoCapital.equals("ANU") && operacionPrepago.EstadoFacturacion.equals("BLO")) ? "UI_PREPAGOBONO_ANULADA_COMPROBANTE_BLOQUEADO" : "";
			}else {
				operacionPrepago.Descripcion = "UI_CUADRADO";
			}
		});
		logger.info("FIN::CuadraturaCierreDiario.cuadraturaPrepagoBonoFacturadas");
		return operacionesPrepago;
	};
	
	private List<TransaccionFacturacion> cuadraturaTransaccionesFacturadasPendientes(Connection conn,String fechaCuadratura) throws Exception{
		logger.info("INICIO::CuadraturaCierreDiario.cuadraturaTransaccionesFacturadasPendientes");
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("FechaInicio", fechaCuadratura);
		
		List<TransaccionFacturacion> transaccionesPendientes = sql.query(conn, SQL_SELECT_CUADRATURA_TRANSACCION_FACTURACION_PENDIENTE, params, TransaccionFacturacion.class);
		
		transaccionesPendientes.parallelStream().forEach(transaccionPendiente->{
			if("PEN".equals(transaccionPendiente.IdEstadoRegistro)) {
				if("MOD".equals(transaccionPendiente.TipoRegistro)) {
					transaccionPendiente.Descripcion = "UI_TRANSACCION_MODIFICADA_NO_PROCESADA";
				}else if("ANU".equals(transaccionPendiente.TipoRegistro)){
					transaccionPendiente.Descripcion = "UI_TRANSACCION_ANULADA_NO_PROCESADA";
				}else if("ING".equals(transaccionPendiente.TipoRegistro)){
					transaccionPendiente.Descripcion = "UI_TRANSACCION_INGRESADA_NO_PROCESADA";
				}
			}else {
				transaccionPendiente.Descripcion = "UI_CUADRADO";
			}
		});
		logger.info("FIN::CuadraturaCierreDiario.cuadraturaTransaccionesFacturadasPendientes");
		return transaccionesPendientes;
	}
	
	private List<objPendienteDTE> cuadraturaTransaccionesFacturadasPendientesDTE(Connection conn,String fechaCuadratura) throws Exception{
		logger.info("INICIO::CuadraturaCierreDiario.cuadraturaTransaccionesFacturadasPendientesDTE");
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("FechaInicio", fechaCuadratura);
		
		List<objPendienteDTE> transaccionesPendientes = sql.query(conn, SQL_SELECT_CUADRATURA_TRANSACCION_FACTURACION_PENDIENTE_DTE, params, objPendienteDTE.class);
		
		transaccionesPendientes.parallelStream().forEach(transaccionPendiente->{
			if("PEN".equals(transaccionPendiente.IdEstadoRegistro)) {
					transaccionPendiente.Descripcion = "UI_TRANSACCION_INGRESADA_NO_PROCESADA";
			}else {
				transaccionPendiente.Descripcion = "UI_CUADRADO";
			}
		});
		logger.info("FIN::CuadraturaCierreDiario.cuadraturaTransaccionesFacturadasPendientesDTE");
		return transaccionesPendientes;
	}
	
	private List<PrestamosFacturacion> cuadraturaPrestamosPendientes(Connection conn,String idTipoOperacion,String fechaCuadratura) throws Exception{
		logger.info("INICIO::CuadraturaCierreDiario.cuadraturaPrestamosPendientes");
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("FechaInicio", fechaCuadratura);
		params.put("IdOperacion", idTipoOperacion);
		
		List<PrestamosFacturacion> prestamosPendientes = sql.query(conn, SQL_SELECT_CUADRATURA_PRESTAMOS_PENDIENTES, params, PrestamosFacturacion.class);
        prestamosPendientes.parallelStream().forEach(  prestamoPendiente -> {
			if(    prestamoPendiente.EstadoFacturacion.equals("ING")){
				 prestamoPendiente.Descripcion = (    prestamoPendiente.EstadoFacturacion.equals("ING")) ? "UI_PRESTAMO_NO_FACTURADO" : "";
			}else if(    prestamoPendiente.EstadoFacturacion.equals("FAC")) {
				 prestamoPendiente.Descripcion = (    prestamoPendiente.EstadoFacturacion.equals("FAC")) ? "UI_CUADRADO" : "";
			}else if(    prestamoPendiente.EstadoFacturacion.equals("ANU")) {
				 prestamoPendiente.Descripcion = (    prestamoPendiente.EstadoFacturacion.equals("ANU")) ? "UI_PRESTAMO_COMPROBANTE_ANULADO" : "";
			}else if(    prestamoPendiente.EstadoFacturacion.equals("BLO")) {
				 prestamoPendiente.Descripcion = (    prestamoPendiente.EstadoFacturacion.equals("BLO")) ? "UI_PRESTAMO_COMPROBANTE_BLOQUEADO" : "";
			}
			else {
				 prestamoPendiente.Descripcion = "UI_CUADRADO";
			}
        });

		logger.info("FIN::CuadraturaCierreDiario.cuadraturaPrestamosPendientes");
		return prestamosPendientes;
	}
	
	private List<MovimientoComprobante> cuadraturaMovimientoFac(List<MovimientoComprobante> movFact){
		logger.info("INICIO::CuadraturaCierreDiario.cuadraturaMovimientoFac");
		List<MovimientoComprobante> descuadres = new ArrayList<MovimientoComprobante>();
		for(MovimientoComprobante mov : movFact){
			if(!"FAC".equalsIgnoreCase(mov.IdEstadoFacturacion)){
				mov.Descripcion = mov.NombreEstadoFacturacion;
				descuadres.add(mov);
			}
		}
		logger.info("FIN::CuadraturaCierreDiario.cuadraturaMovimientoFac");
		return descuadres;
	}
	
	public void AgregarLog(Connection con, CuadraturaCierreDiarioEntrada in) {
		try {
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			Long IdCabeceraLog =  in.p_IdUsuario;		
			//CABECERA
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				
			log.IdCabeceraLog = IdCabeceraLog;
			log.IdModulo = "FAC";
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
	
	//TODO: Main
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String json = "{"p_IdUsuario":14,"p_IdEmpresa":1,"p_IdModulo":"FMM","p_FechaInicio":"2020-03-31","p_IdProceso":[{"IdCuadraturaDiariaOperaciones":"CUAMOVFACEMILIB"}],"p_TipoLlamado":"Proceso"}";
			//String jsonConsulta = "{\"p_IdUsuario\":164,\"p_IdEmpresa\":1,\"p_IdModulo\":\"FMM\",\"p_FechaInicio\":\"2023-05-24\",\"p_IdProceso\":[{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACANCACN\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACANCRF\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACANCSPT\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACANSIMC\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACANSIMV\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACANTPRT\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACANVACN\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACCOMACN\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACCOMACX\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACCOMETN\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACCOMETX\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACCOMIIF\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACCOMMIL\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACCOMOPA\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACCOMOTC\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACCOMRF\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACCOMSIM\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACCOMSPT\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACDIVACC\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACEMILIB\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACPRESTA\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACSUSOSA\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACVENACN\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACVENACX\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACVENEMP\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACVENIIF\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACVENMIL\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACVENOTC\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACVENPRT\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACVENRF\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACVENSIM\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACVENSPT\"}],\"p_TipoLlamado\":\"Proceso\"}"; 
			String jsonConsulta = "{\"p_IdUsuario\":0,\"p_IdEmpresa\":1,\"p_IdModulo\":\"FMM\",\"p_FechaInicio\":\"2024-07-09\",\"p_IdProceso\":[{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVFACDTENPEN\"}],\"p_TipoLlamado\":\"Proceso\"}";
			CuadraturaCierreDiarioEntrada Objin = gson.fromJson(jsonConsulta, CuadraturaCierreDiarioEntrada.class);
			
			CuadraturaCierreDiario demo=new CuadraturaCierreDiario();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CuadraturaCierreDiarioSalida ObjOut= demo.logica(con, Objin);
 				//con.commit();
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			} 			
    	}
		 	
		System.out.println("== END ==");
	}
	

}