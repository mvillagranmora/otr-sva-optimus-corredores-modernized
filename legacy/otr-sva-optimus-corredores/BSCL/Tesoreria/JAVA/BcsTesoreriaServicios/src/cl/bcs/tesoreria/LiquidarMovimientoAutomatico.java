package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearComprobante.CrearComprobante_Entrada;
import cl.bcs.tesoreria.CrearComprobante.CrearComprobante_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.PreferenciaTesoreria;
import cl.bcs.tesoreria.CrearComprobante.IngresoEgresoGenerar_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.ResultadoProceso_Salida;
import cl.bcs.tesoreria.EdicionMultiple.FormaDePago;
import cl.bcs.tesoreria.EdicionMultiple.RegistroAProcesar;
import cl.bcs.tesoreria.LiquidarComprobante.ComprobanteLiquidar_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Salida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class LiquidarMovimientoAutomatico  implements JavaCall
{

	private static final Logger logger 							= LogManager.getLogger(LiquidarMovimientoAutomatico.class);
	private final String SQL_SELECT_INGRESO 					= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarMovimientoAutomatico_SelectIngresoEgreso.sql");
	private final String SQL_SELECT_COMPROBANTES 				= loadResourceToString("cl/bcs/tesoreria/sql/CrearIngresoEgreso_SelectComprobantes.sql");
	private final String SQL_CALL_MODIFICAR_INGRESO_EGRESO 		= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarMovimientoAutomatico_CallModificarIngresoEgreso.sql");
	private final String SQL_CREATE_TEMP_TABLE_FORMA_PAGO 		= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarMovimientoAutomatico_CreateTempTableFormaPago.sql");
	private final String SQL_CREATE_TEMP_TABLE_INGRESO_EGRESO 	= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarMovimientoAutomatico_CreateTempTableIngresoEgreso.sql");
	private final String SQL_DROP_TEMP_TABLE_FORMA_PAGO 		= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarMovimientoAutomatico_DropTempTableFormaPago.sql");
	private final String SQL_DROP_TEMP_TABLE_INGRESO_EGRESO 	= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarMovimientoAutomatico_DropTempTableIngresoEgreso.sql");
	private final String SQL_INSERT_TEMP_TABLE_FORMA_PAGO 		= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarMovimientoAutomatico_InsertTempTableFormaPago.sql");
	private final String SQL_INSERT_TEMP_TABLE_INGRESO_EGRESO 	= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarMovimientoAutomatico_InsertTempTableIngresoEgreso.sql");
	
	public static class LiquidarMovimientoAutomatico_Entrada extends BcsPojo {
		
		public Long		p_IdEmpresa;  				 
		public Long		p_IdUsuario; 				   
		public String 	p_IdModulo; 				
		public String 	p_SimularOperacion; 	 
		public String 	p_UsaReglasNegocio;			
	    public String 	p_FechaMovimiento;
	    public String 	p_Canal;

	    public List<IngresoEgresoGenerar_Entrada> 	p_IngresoEgreso;
	    public List<FormaDePago_Entrada> 			p_FormaDePago;  
	    
	    public LiquidarMovimientoAutomatico_Entrada() {
	    	
	    	p_Canal 			= "WEB";
	    	p_SimularOperacion  = "N";
	    	p_UsaReglasNegocio	= "S";
	    	p_IngresoEgreso		= new ArrayList<IngresoEgresoGenerar_Entrada>();
	    	p_FormaDePago 		= new ArrayList<FormaDePago_Entrada>();
	    }
	    
	}	
	
	public static class LiquidarMovimientoAutomatico_Salida extends BcsPojo {
		
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida>	P_OUT_RESULTADOPROCESO;
		public List<AdvertenciasDetalle>		P_OUT_ADVERTENCIASDETALLE;
		public Long								P_OUT_ADVERTENCIASCONTADOR;

		public LiquidarMovimientoAutomatico_Salida () {
			
			P_OUT_ERRORESCONTADOR		= 0L;
			P_OUT_ADVERTENCIASCONTADOR	= 0L;
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			P_OUT_RESULTADOPROCESO 		= new ArrayList<ResultadoProceso_Salida>();
			P_OUT_ADVERTENCIASDETALLE 	= new ArrayList<AdvertenciasDetalle>();
			
		};
		
	}
	
	public static class ResultadoProceso_Salida extends BcsPojo {
		
		public String 	Campo;        
		public Long   	Id;           
		public String 	Servicio;
		public String	Resultado;
		
	}
	
	public static class IngresoEgreso_Entrada extends BcsPojo {
		
		public Long      	NumeroReferencia;  				
		public String    	IdTipoMovimiento;  		 		
		public String    	IdTipoPersona;  		 		
	   	public Long      	IdPersona;  		 			
	   	public Long      	IdPortafolio;  		 			
	   	public String    	IdMoneda;  		 				
	   	public BigDecimal 	Monto; 		 					
	   	public String   	FechaLiquidacion;  		 		
	   	public String    	IdConcepto;  		 			
	   	public String    	IdTipoPersonaTitularPago;  		
	   	public Long      	IdPersonaTitularPago;  		 	
	   	public Long      	IdPortafolioTitularPago;  		
	   	public Long      	IdCaja;  		 				
	   	public String    	SecuenciaAgrupacion = ""; 		
	   	public Long      	SecuenciaRelacionada = 0L;	 	
	   	public String    	Glosa;  		 				
	   	public Long      	IdEmpleadoDigitador;  		 	
	   	public Long      	IdSucursalEmpleadoDigitador;  	
	   	public String   	IdTipoProducto;  		 		
	   	public String    	IdTipoOperacion;  		 		
	   	public String    	IdTipoComprobante;  		 	
	   	public Long      	IdComprobante;  		 		
	   	public String    	FechaComprobante;  		 		
	   	public String    	FechaLiquidacionComprobante;  								    
	   	public Long     	IdOperacion;  		 			
	    public String    	FechaOperacion;  		 		
	    public String   	IdCondicionLiquidacion;  		
	    public String    	IdBolsa;  		 				
	    public String    	IdMonedaOperacion;  		 	
	    public BigDecimal 	MontoOperacion	= BigDecimal.ZERO;   				
	    public String    	IdTipoReajuste;  		 		
	    public String    	FechaParidad;  		 			
	    public BigDecimal 	ValorParidad	= BigDecimal.ZERO;   				
	    public Long     	FolioCartera;  		 			
	    public Long      	IdInstrumento;  		 		
	    public String    	IdTipoInstrumento;  		 	
	    public String    	IdClasificacionTipoInstrumento; 
	    public String    	Bilateral = "N";		        
	    public String    	GeneraComprobante = "N"; 		
	    public String    	AgruparEnComprobante = "N"; 	
	    public String    	MueveCuentaInversion;  			
	    public String    	TitularPago = "";  		 		
	    
		public IngresoEgreso_Entrada()
		{
			this.Bilateral 	          			= "N";
			this.GeneraComprobante    			= "N";
			this.AgruparEnComprobante 			= "N";
			this.SecuenciaAgrupacion  			= "";
			this.SecuenciaRelacionada 			= 0L;
			this.IdTipoComprobante 	  			= "";
			this.IdComprobante 		  			= 0L;
			this.FechaComprobante				= "0001-01-01";
			this.FechaLiquidacionComprobante 	= "0001-01-01";
		}	
			
	} 
	
	public static class FormaDePago_Entrada extends BcsPojo {
		
		public Long 		NumeroReferencia;				
		public String		IdMedioDePago;          		
		public String		IdModalidadDePago;      		
		public String		IdMoneda;               		
		public BigDecimal   Monto; 							
		public Long 		IdCuentaBancariaCorredora; 		
		public Long 		IdTarjetaBancariaCorredora; 	
		public Long 		IdChequera; 					
		public Long 		NumeroDocumentoCorredora; 		
		public String		FechaDocumentoPagoCorredora;	
		public Long 		IdCuentaBancariaSocio; 			
		public Long 		IdBancoSocio; 					
		public String		NombreCuentaBancariaSocio;		
		public String		NumeroCuentaBancariaSocio;		
		public String		IdTipoCuentaBancariaSocio;		
		public String		NombreTipoCuentaBancariaSocio; 	
		public Long 		IdTarjetaBancariaSocio;			
		public String		IdTipoTarjetaBancariaSocio;  	
		public String		NombreTipoTarjetaBancariaSocio; 
		public String		NumeroTarjetaBancariaSocio;  	
		public String		NumeroSerieSocio;  				
		public Long 		NumeroDocumentoSocio;			
		public String		FechaDocumentoPagoSocio;	

	}
	
	public static class IngresoEgresoModificado extends BcsPojo {
		
		public Long NumeroReferencia;
		public String IdTipoMovimiento;

	}
	
	public static class ResultadoProceso extends BcsPojo {
		
		public String Campo;
		public Long Id;
		public String Servicio;
		public String Resultado;
		
	}
	
	public static class AdvertenciasDetalle extends BcsPojo {
		
		public Long NumeroReferencia;
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

	}
	
	public static class ErrorDetalle extends BcsPojo {
		
		public Long NumeroReferencia;
		public String Codigo;
		public String Descripcion;

	}
	
	public static class ModificarIngresoEgreso extends BcsPojo {
		
		List<IngresoEgresoModificado> IngresoEgresoModificado;
		List<ResultadoProceso> ResultadoProceso;
		List<AdvertenciasDetalle> AdvertenciasDetalle;
		List<ErrorDetalle> ErrorDetalle;
		
		public ModificarIngresoEgreso() {
	    	
			IngresoEgresoModificado  	= new ArrayList<IngresoEgresoModificado>();
			ResultadoProceso			= new ArrayList<ResultadoProceso>();
			AdvertenciasDetalle			= new ArrayList<AdvertenciasDetalle>();
			ErrorDetalle 				= new ArrayList<ErrorDetalle>();
	    }
		
	}

	@SuppressWarnings("unchecked")
	public LiquidarMovimientoAutomatico_Salida logica(Connection con, LiquidarMovimientoAutomatico_Entrada in) throws Exception {
		
		logger.info("INICIO::LiquidarMovimientoAutomatico");
		
		Long init 									= System.currentTimeMillis();
		SQL sql 									= new SQL();
		LiquidarMovimientoAutomatico_Salida out 	= new LiquidarMovimientoAutomatico_Salida();
		String fechaSistema 						= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String FechaMovimiento 						= "";
		
		if (in.p_IngresoEgreso.size() == 0) 
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_MOVIMIENTOS_DE_INGRESO_EGRESO",null,con,in.p_IdEmpresa));
			return out;
		}
		
		if ( in.p_FechaMovimiento == null || in.p_FechaMovimiento.isEmpty() || in.p_FechaMovimiento.equalsIgnoreCase("0001-01-01") )
			FechaMovimiento = fechaSistema;
		else
			FechaMovimiento = in.p_FechaMovimiento;
		
		// Se obtienen los ids de ingresos/egresos a liquidar (todos) y los que necesitan actualizar sus formas de pago
		List<Long> IngresosEgresosALiquidar 			= new ArrayList<Long>();
		List<Long> IngresosEgresosActualizarFormaPago 	= new ArrayList<Long>();

		for ( IngresoEgresoGenerar_Entrada IngresoEgreso: in.p_IngresoEgreso ) {
			
			IngresosEgresosALiquidar.add(IngresoEgreso.IdIngresoEgreso);
			
			if ( in.p_FormaDePago.stream()
					.filter(item -> item.NumeroReferencia.equals(IngresoEgreso.NumeroReferencia))
					.collect(Collectors.toList()).size() > 0
				) IngresosEgresosActualizarFormaPago.add(IngresoEgreso.IdIngresoEgreso);
		}
		
		// Se obtienen los Ingresos/Egresos a actualizar desde la BD
		HashMap<String,Object> ParamIngresosEgresos = new HashMap<>();
		ParamIngresosEgresos.put("IdsIngresosEgresos", IngresosEgresosActualizarFormaPago);
		List<IngresoEgreso_Entrada> IngresosEgresosActualizar = sql.query(con, SQL_SELECT_INGRESO, ParamIngresosEgresos, IngresoEgreso_Entrada.class);
		List<FormaDePago_Entrada> FormasDePagoActualizar = new ArrayList<FormaDePago_Entrada>();
		
		// Se preparan las formas de pago actualizando el numero de referencia por el IdIngresoEgreso para enviar al SP
		HashMap<Long,Object> HashInformacionIngresoEntrada = Utils.List2Hash(in.p_IngresoEgreso, "NumeroReferencia");
		for ( FormaDePago_Entrada formasDePago: in.p_FormaDePago ) {
			
			IngresoEgresoGenerar_Entrada fp = (IngresoEgresoGenerar_Entrada) HashInformacionIngresoEntrada.get(formasDePago.NumeroReferencia);
			formasDePago.NumeroReferencia = fp.IdIngresoEgreso;
			FormasDePagoActualizar.add(formasDePago);
			
		}	
		
		// Se actualizan las formas de pago
		HashMap<String, Object> SalidaActualizarFormasDePago = this.actualizarFormasDePago(con, sql, in, IngresosEgresosActualizar, FormasDePagoActualizar);
		
		if ( (Long)SalidaActualizarFormasDePago.get("p_out_ErroresContador") > 0L ) {
			
			logger.debug("ACTUALIZACION DE FORMAS DE PAGO CON ERRORES");
			out.P_OUT_ERRORESCONTADOR = (Long)SalidaActualizarFormasDePago.get("p_out_ErroresContador");
			
			List<ErrorDetalle> errores = (List<ErrorDetalle>) SalidaActualizarFormasDePago.get("p_out_ErroresDetalle");
			
			for( ErrorDetalle err: errores ) {
				
				out.P_OUT_MENSAJES.add(new MensajeSalida(err.NumeroReferencia,MensajeSalida.MENSAJE_ERROR,err.Codigo,err.Descripcion));
				
			}
			
			return out;
			
		} else {
			
			logger.debug("ACTUALIZACION DE FORMAS DE PAGO OK");
			out.P_OUT_RESULTADOPROCESO.addAll((Collection<? extends ResultadoProceso_Salida>) SalidaActualizarFormasDePago.get("p_out_ResultadoProceso"));
			out.P_OUT_ADVERTENCIASDETALLE.addAll((Collection<? extends AdvertenciasDetalle>) SalidaActualizarFormasDePago.get("p_out_AdvertenciasDetalle"));
			out.P_OUT_ADVERTENCIASCONTADOR = (Long)SalidaActualizarFormasDePago.get("p_out_AdvertenciasContador");
			
			// Si viene de webservice lo considera como error		
			if ( in.p_Canal.equalsIgnoreCase("WEB") &&
				 out.P_OUT_ADVERTENCIASDETALLE
					.stream()
					.filter(item -> item.Codigo.equals("MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO"))
					.collect(Collectors.toList()).size() > 0 
				) 
				return out;
			
		}
		
		// Se mandan a crear los comprobantes
		CrearComprobante_Salida crearComprobante_Salida = this.creaComprobante(con, sql, in, FechaMovimiento);
		
		if (MensajeSalida.getNumeroErrores(crearComprobante_Salida.P_OUT_MENSAJES) > 0) {
			
			logger.debug("GENERACION DE COMPROBANTES CON ERRORES: " + crearComprobante_Salida.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES.addAll(crearComprobante_Salida.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;
			
		} else {
			
			logger.debug("GENERACION DE COMPROBANTES OK: " + crearComprobante_Salida.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES.addAll(crearComprobante_Salida.P_OUT_MENSAJES);
			
			//Obtiene y agrega a la salida el resultado del proceso de generar comprobante 
			for (CrearComprobante.ResultadoProceso_Salida resultadoProcesoCOM: crearComprobante_Salida.P_OUT_RESULTADOPROCESO) {
				
				ResultadoProceso_Salida resultadoProcesoIE = new ResultadoProceso_Salida();
				Utils.copyPojo(resultadoProcesoCOM, resultadoProcesoIE);
				out.P_OUT_RESULTADOPROCESO.add(resultadoProcesoIE);
				
			}
			
		}
		
		// Se mandan a liquidar los comprobantes
		LiquidarComprobante_Salida liquidarComprobanteSalida = this.liquidaComprobante(con, sql, in, IngresosEgresosALiquidar, FechaMovimiento);
		
		if (MensajeSalida.getNumeroErrores(liquidarComprobanteSalida.P_OUT_MENSAJES) > 0) {
			
			logger.debug("LIQUIDACION DE COMPROBANTES CON ERRORES: " + liquidarComprobanteSalida.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES.addAll(liquidarComprobanteSalida.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;

		} else {
			
			logger.debug("LIQUIDACION DE COMPROBANTES OK: " + liquidarComprobanteSalida.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES.addAll(liquidarComprobanteSalida.P_OUT_MENSAJES);
			
			//Obtiene y agrega a la salida el resultado del proceso de liquidar comprobantes 
			for ( LiquidarComprobante.ResultadoProceso_Salida resultadoProcesoLiq: liquidarComprobanteSalida.P_OUT_RESULTADOPROCESO ) {
				
				ResultadoProceso_Salida resultadoProcesoIE = new ResultadoProceso_Salida();
				Utils.copyPojo(resultadoProcesoLiq, resultadoProcesoIE);
				out.P_OUT_RESULTADOPROCESO.add(resultadoProcesoIE);
				
			}
			
		}
		
		logger.info("FIN::LiquidarMovimientoAutomatico, " + (System.currentTimeMillis() - init));
		return out;

	}
	
	private HashMap<String, Object> actualizarFormasDePago(Connection con, SQL sql, LiquidarMovimientoAutomatico_Entrada in, List<IngresoEgreso_Entrada> IngresosEgresosActualizar, List<FormaDePago_Entrada> IngresoEgresoFormasDePagoActualizar) throws SQLException {
		
		logger.info("INICIO::LiquidarMovimientoAutomatico.actualizarFormasDePago");
		
		Long init 									= System.currentTimeMillis();
		HashMap<String, Object> parametrosEntrada 	= new HashMap<String, Object>();
		
		parametrosEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
		parametrosEntrada.put("p_IdUsuario", in.p_IdUsuario);
		parametrosEntrada.put("p_IdModulo", in.p_IdModulo);
		parametrosEntrada.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
		parametrosEntrada.put("p_SimularOperacion", in.p_SimularOperacion);
		parametrosEntrada.put("p_FormaDePago", IngresoEgresoFormasDePagoActualizar);
		parametrosEntrada.put("p_IngresoEgresoModificar", IngresosEgresosActualizar);
		
		HashMap<String, Class> parametrosSalida = new HashMap<String, Class>();
		parametrosSalida.put("p_out_IngresoEgresoModificado", IngresoEgresoModificado.class);
		parametrosSalida.put("p_out_ResultadoProceso", ResultadoProceso.class);
		parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
	    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class); 
	    parametrosSalida.put("p_out_ErroresContador", Long.class);
	    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
	    
	    System.out.println("IngresosEgresosActualizar:" + IngresosEgresosActualizar);
		System.out.println("IngresoEgresoFormasDePagoActualizar:" + IngresoEgresoFormasDePagoActualizar);
	    
		HashMap<String, Object> respuestaModificarIngresoEgreso;
		try {
			
			SQL.exec( con, SQL_CREATE_TEMP_TABLE_FORMA_PAGO );
	    	SQL.exec( con, SQL_CREATE_TEMP_TABLE_INGRESO_EGRESO );
	    	sql.batch(con, SQL_INSERT_TEMP_TABLE_FORMA_PAGO, IngresoEgresoFormasDePagoActualizar, FormaDePago_Entrada.class);
	    	sql.batch(con, SQL_INSERT_TEMP_TABLE_INGRESO_EGRESO, IngresosEgresosActualizar, IngresoEgreso_Entrada.class);
	    	
	    	respuestaModificarIngresoEgreso = sql.call(con, SQL_CALL_MODIFICAR_INGRESO_EGRESO, parametrosEntrada, parametrosSalida);
	    	
		} catch(Exception e) {
			
	    	throw e;
	    	
	    } finally {
	    	
	    	SQL.exec(con, SQL_DROP_TEMP_TABLE_FORMA_PAGO);
	    	SQL.exec(con, SQL_DROP_TEMP_TABLE_INGRESO_EGRESO);
	    	
	    }
		
		logger.info("FIN::LiquidarMovimientoAutomatico.actualizarFormasDePago, " + (System.currentTimeMillis() - init));
		
    	return respuestaModificarIngresoEgreso;
	    
	}

	private CrearComprobante_Salida creaComprobante( Connection con, SQL sql, LiquidarMovimientoAutomatico_Entrada in, String FechaMovimiento ) throws Exception {

		logger.info("INICIO::LiquidarMovimientoAutomatico.creaComprobante");
		
		Long init 														= System.currentTimeMillis();
		
		CrearComprobante_Entrada crearComprobanteEntrada 				= new CrearComprobante_Entrada();
		crearComprobanteEntrada.p_IdEmpresa 							= in.p_IdEmpresa;  
		crearComprobanteEntrada.p_IdUsuario 							= in.p_IdUsuario; 
		crearComprobanteEntrada.p_IdModulo 								= in.p_IdModulo;
		crearComprobanteEntrada.p_SimularOperacion 						= in.p_SimularOperacion;
		crearComprobanteEntrada.p_UsaReglasNegocio 						= "N";
		crearComprobanteEntrada.p_FechaMovimiento 						= FechaMovimiento;				
		crearComprobanteEntrada.p_IdCaja 								= 0L;		
		crearComprobanteEntrada.p_GenerarComprobanteDesdeMovimientos  	= "S";
		crearComprobanteEntrada.p_GenerarComprobanteDesdeComprobantes 	= "N";	
		crearComprobanteEntrada.p_EsIngresoAutomatico 					= "S";
		crearComprobanteEntrada.p_IngresoEgresoGenerar.addAll(in.p_IngresoEgreso);
		
		CrearComprobante crearComprobante = new CrearComprobante();
		CrearComprobante_Salida crearComprobante_Salida = crearComprobante.logica(con, crearComprobanteEntrada);
		
		logger.info("FIN::LiquidarMovimientoAutomatico.creaComprobante, " + (System.currentTimeMillis() - init));

		return crearComprobante_Salida;
		
	}
	
	private LiquidarComprobante_Salida liquidaComprobante ( Connection con, SQL sql, LiquidarMovimientoAutomatico_Entrada in, List<Long> IngresoEgresoLiquidar, String FechaMovimiento ) throws Exception {
		
		logger.info("INICIO::LiquidarMovimientoAutomatico.liquidaComprobante");
		
		Long init 									= System.currentTimeMillis();

		HashMap<String,Object> paramsComprobantes 	= new HashMap<>();
		paramsComprobantes.put("IdsIngresoEgreso", IngresoEgresoLiquidar);
		
		List<ComprobanteLiquidar_Entrada> comprobantesLiquidar = sql.query(con, SQL_SELECT_COMPROBANTES, paramsComprobantes , ComprobanteLiquidar_Entrada.class);
		
		LiquidarComprobante_Entrada liquidarComprobanteEntrada = new LiquidarComprobante_Entrada();
		
		liquidarComprobanteEntrada.p_IdEmpresa 				= in.p_IdEmpresa;  
		liquidarComprobanteEntrada.p_IdUsuario 				= in.p_IdUsuario; 
		liquidarComprobanteEntrada.p_IdModulo 				= in.p_IdModulo;
		liquidarComprobanteEntrada.p_SimularOperacion 		= in.p_SimularOperacion;
		liquidarComprobanteEntrada.p_UsaReglasNegocio 		= in.p_UsaReglasNegocio;
		liquidarComprobanteEntrada.p_FechaMovimiento 		= FechaMovimiento;
		liquidarComprobanteEntrada.p_ComprobanteLiquidar 	= comprobantesLiquidar;
		
		LiquidarComprobante liquidarComprobante 				= new LiquidarComprobante();
		LiquidarComprobante_Salida liquidarComprobanteSalida 	= liquidarComprobante.logica(con, liquidarComprobanteEntrada);
		
		logger.info("FIN::LiquidarMovimientoAutomatico.liquidaComprobante, " + (System.currentTimeMillis() - init));

		return liquidarComprobanteSalida;
		
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		
		Gson gson = getGsonBuilder();
		
		LiquidarMovimientoAutomatico_Entrada in = gson.fromJson(jin.toString(), LiquidarMovimientoAutomatico_Entrada.class);
		LiquidarMovimientoAutomatico_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		if(in.p_SimularOperacion.equalsIgnoreCase("S")) {
			con.rollback();
		}
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
		
	}

	public static void main(String[] args) throws Exception {
		
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			Gson gson = Utils.getGsonBuilder();

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FormaDePago\":[{\"IdMedioDePago\":\"BDE\",\"IdModalidadDePago\":\"MODDEP\",\"IdMoneda\":\"CLP\",\"Monto\":10000,\"IdCuentaBancariaCorredora\":109,\"IdTarjetaBancariaCorredora\":0,\"IdChequera\":0,\"NumeroDocumentoCorredora\":0,\"FechaDocumentoPagoCorredora\":\"0001-01-01\",\"IdCuentaBancariaSocio\":0,\"IdBancoSocio\":0,\"NombreCuentaBancariaSocio\":\"\",\"NumeroCuentaBancariaSocio\":\"\",\"IdTipoCuentaBancariaSocio\":\"\",\"NombreTipoCuentaBancariaSocio\":\"\",\"IdTarjetaBancariaSocio\":0,\"IdTipoTarjetaBancariaSocio\":\"\",\"NombreTipoTarjetaBancariaSocio\":\"\",\"NumeroTarjetaBancariaSocio\":\"\",\"NumeroSerieSocio\":\"\",\"NumeroDocumentoSocio\":123123123,\"FechaDocumentoPagoSocio\":\"2021-08-17\",\"NumeroReferencia\":0,\"NumeroReferenciaFormaDePago\":0},{\"IdMedioDePago\":\"BDE\",\"IdModalidadDePago\":\"MODDEP\",\"IdMoneda\":\"CLP\",\"Monto\":30000,\"IdCuentaBancariaCorredora\":110,\"IdTarjetaBancariaCorredora\":0,\"IdChequera\":0,\"NumeroDocumentoCorredora\":0,\"FechaDocumentoPagoCorredora\":\"0001-01-01\",\"IdCuentaBancariaSocio\":0,\"IdBancoSocio\":0,\"NombreCuentaBancariaSocio\":\"\",\"NumeroCuentaBancariaSocio\":\"\",\"IdTipoCuentaBancariaSocio\":\"\",\"NombreTipoCuentaBancariaSocio\":\"\",\"IdTarjetaBancariaSocio\":0,\"IdTipoTarjetaBancariaSocio\":\"\",\"NombreTipoTarjetaBancariaSocio\":\"\",\"NumeroTarjetaBancariaSocio\":\"\",\"NumeroSerieSocio\":\"\",\"NumeroDocumentoSocio\":1123121,\"FechaDocumentoPagoSocio\":\"2021-08-17\",\"NumeroReferencia\":0,\"NumeroReferenciaFormaDePago\":1}],\"p_IngresoEgreso\":[{\"NumeroReferencia\":0,\"IdIngresoEgreso\":19780,\"TipoComprobante\":\"\"},{\"NumeroReferencia\":1,\"IdIngresoEgreso\":19781,\"TipoComprobante\":\"\"}],\"p_Canal\":\"WEB\"}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FormaDePago\":[],\"p_IngresoEgreso\":[{\"NumeroReferencia\":0,\"IdIngresoEgreso\":19780,\"TipoComprobante\":\"\"}]}";
			LiquidarMovimientoAutomatico_Entrada Objin = gson.fromJson(json, LiquidarMovimientoAutomatico_Entrada.class);
			LiquidarMovimientoAutomatico demo = new LiquidarMovimientoAutomatico();
			
 			System.out.println("ENTRADA:");
 			System.out.println(Objin.toString());
 			
 			try {
 				
 				LiquidarMovimientoAutomatico_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				con.rollback();
 				//con.commit();
 				
 			} catch(Exception er) {
 				
				er.printStackTrace();	
				//con.commit();
				
			}
 			
 			con.close();
 			
	    }
		catch (Exception e) {
			
			e.printStackTrace();	
			
		}
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
	
	
	
}
