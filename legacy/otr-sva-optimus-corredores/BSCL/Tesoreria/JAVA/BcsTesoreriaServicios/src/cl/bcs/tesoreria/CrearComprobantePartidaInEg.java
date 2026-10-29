package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class CrearComprobantePartidaInEg  implements JavaCall
{ 

	// Log
	static final Logger logger = LogManager.getLogger(CrearComprobante.class);
	// Queries
	private static final String CREATE_TEMPORARY_TABLE = "CREATE LOCAL TEMPORARY TABLE #ComprobanteIngresEgresoAProcesar (\"NumeroReferencia\" BIGINT,\"IdIngresoEgreso\" BIGINT, \"TipoComprobante\" NVARCHAR)";
	private static final String SQL_INSERT_REGISTROS = "INSERT INTO #ComprobanteIngresEgresoAProcesar (\"NumeroReferencia\",\"IdIngresoEgreso\",\"TipoComprobante\") VALUES (:NumeroReferencia,:IdIngresoEgreso,:TipoComprobante)";
	private static final String SQL_DELETE_REGISTROS = "DELETE FROM #ComprobanteIngresEgresoAProcesar WHERE 1=1";

	private final String SQL_INSERT_COMPROBANTES 			= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobanteJava_InsertComprobante.sql");
	private final String SQL_INSERT_COMPROBANTES_DETALLE    = loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobanteJava_InsertComprobanteDetalle.sql");
	private final String SQL_INSERT_COMPROBANTES_FORMA_PAGO = loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobanteJava_InsertComprobanteFormaPago.sql");	
	//private final String SQL_INSERT_COMPROBANTES_ORIGEN     = loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobanteJava_InsertComprobanteOrigen.sql");

	private final String SQL_SELECT_INGRESOEGRESO = loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobanteJava_SelectIngresoEgreso.sql");
	private final String SQL_SELECT_FORMA_PAGO   = loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_SelectFormaPago.sql");

	private final String SQL_SELECT_ResultadoProceso = "SELECT \"Campo\",\"Id\",\"Servicio\",\"Resultado\" FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso\"";
	private final String SQL_SELECT_InformacionDetalle = "SELECT \"Codigo\",\"Descripcion\" FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_InformacionDetalle\"";
	private final String SQL_SELECT_AdvertenciasDetalle = "SELECT \"Codigo\",\"RequiereAutorizacion\",\"Descripcion\" FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle\"";
	private final String SQL_SELECT_ErroresDetalle = "SELECT \"Codigo\",\"Descripcion\" FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle\"";
	private final String SQL_CALL_CrearComprobante = "call \"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearComprobantes\" ("
	   	     + "p_FechaMovimiento 		=> :p_IdEmpresa,"  
	   	     + "p_IdCaja 		=> :p_IdUsuario,"
	   	     + "p_GenerarComprobanteDesdeMovimientos  		=> :p_IdModulo,"
	   	     + "p_GenerarComprobanteDesdeComprobantes 	=> :p_SimularOperacion," 
	   	     + "p_ValidaComprobanteGenerado 	=> :p_SimularOperacion)"; 
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS IN
	//-------------------------------------------------------------------------------------------

	public static class CrearComprobante_Entrada
	{
		public Long		p_IdEmpresa; 									//	BIGINT  
		public Long		p_IdUsuario; 									//	BIGINT 
		public String 	p_IdModulo; 									//	NVARCHAR(3)    
		public String 	p_SimularOperacion = "N"; 						//	NVARCHAR(1)
		public String 	p_UsaReglasNegocio = "S"; 						//	NVARCHAR(1)      
		public String 	p_FechaMovimiento;								//  NVARCHAR(10)  
		public Long		p_IdCaja = 0L;   								//	BIGINT			-- Caja donde se procesaran los comprobantes		
		public String 	p_GenerarComprobanteDesdeMovimientos  = "N";	//	NVARCHAR(1)		-- S: debe llenar p_IngresoEgresoGenerar	
		public String 	p_GenerarComprobanteDesdeComprobantes = "S";	//  NVARCHAR(1)		-- S: debe llenar p_ComprobanteGenerar	
		public String 	p_EsIngresoAutomatico = "N";					//  NVARCHAR(1) 	-- S: Es ingreso automatico, N: Es ingreso Manual
		
		public List<IngresoEgresoGenerar_Entrada>	p_IngresoEgresoGenerar; //Listado de movimientos para generar comprobantes
		public List<ComprobanteGenerar_Entrada>		p_ComprobanteGenerar;   //Listado de comprobantes para generar comprobantes
		public List<FormaDePago_Entrada>			p_FormaDePago;			//Formas de pago para PARING/PAREGR, para comprobantes normales los toma desde el origen
		
		public CrearComprobante_Entrada()
		{
			this.p_SimularOperacion = "N";
			this.p_UsaReglasNegocio = "S";
			this.p_IdCaja = 0L;		
			this.p_GenerarComprobanteDesdeMovimientos  = "N";	// S: debe llenar p_IngresoEgresoGenerar
			this.p_GenerarComprobanteDesdeComprobantes = "S";	// S: debe llenar p_ComprobanteGenerar
			this.p_EsIngresoAutomatico = "N";
			this.p_IngresoEgresoGenerar = new ArrayList<IngresoEgresoGenerar_Entrada>();
			this.p_ComprobanteGenerar = new ArrayList<ComprobanteGenerar_Entrada>();
			this.p_FormaDePago = new ArrayList<FormaDePago_Entrada>();
		}
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	
	}

	
	//Generar comprobantes desde movimientos de ingreso/egreso
	public static class IngresoEgresoGenerar_Entrada
	{
		public Long 	NumeroReferencia;   // BIGINT 		-- Correlativo para relacionar los Mov que van a formar el comprobante
		public long 	IdIngresoEgreso;	// BIGINT		-- Movimientos que conformarán el comprobante
		public String 	TipoComprobante;	// NVARCHAR(6) 	-- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//Generar comprobantes desde otros comprobantes	
	public static class ComprobanteGenerar_Entrada
	{
		public Long 	NumeroReferencia;   // BIGINT 		 -- Correlativo para relacionar los comprobantes que van a formar un nuevo comprobante
		public Long 	IdComprobante;   	// BIGINT        -- Comprobantes que conformará el comprobante 
		public String 	TipoComprobante;   	// NVARVCHAR(6)  -- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//Formas de pago para PARING/PAREGR, para comprobantes normales los toma desde el origen
	public static class FormaDePago_Entrada implements Cloneable 
	{
		public Long 		IdComprobanteFormaDePago;
		public Long	        IdUsuarioLiquidacion;
		public Long			IdEmpresa; 					//	BIGINT  
		public Long 		IdComprobante;
		public String 		IdTipoMovimiento;
		public String 		NombreMedioDePago;
		public Long 		NumeroReferencia;   			// BIGINT
		public String 		IdMedioDePago;  				// NVARCHAR(3)
		public String 		IdModalidadDePago;  			// NVARCHAR(6)
		public String 		NombreModalidadDePago;
		public BigDecimal	Monto;   						// DECIMAL(26,8)
		public Long 		IdCuentaBancariaCorredora;   	// BIGINT
		public String 		NombreCuentaBancariaCorredora;
		public String		IdTipoCuentaBancariaCorredora;
		public String		NombreTipoCuentaBancariaCorredora;
		public Long 		IdTarjetaBancariaCorredora;   	// BIGINT
		public String 		IdTipoTarjetaBancariaCorredora;
		public String 		NombreTipoTarjetaBancariaCorredora;
		public String 		NumeroTarjetaBancariaCorredora;
		public Long 		IdChequera;   					// BIGINT
		public Long 		NumeroDocumentoCorredora;   	// BIGINT
		public Date 		FechaDocumentoPagoCorredora;	// NVARCHAR(40)
		public Long 		IdCuentaBancariaSocio;   		// BIGINT
		public Long 		IdBancoSocio;   				// BIGINT
		public String 		NombreCuentaBancariaSocio; 		// NVARCHAR(100)
		public String 		NumeroCuentaBancariaSocio; 		// NVARCHAR(100)
		public String 		IdTipoCuentaBancariaSocio; 		// NVARCHAR(4)
		public String 		NombreTipoCuentaBancariaSocio; 	// NVARCHAR(100)
		public Long 		IdTarjetaBancariaSocio;  		// BIGINT
		public String 		IdTipoTarjetaBancariaSocio; 	// NVARCHAR(40)
		public String 		NombreTipoTarjetaBancariaSocio; // NVARCHAR(100)
		public String 		NumeroTarjetaBancariaSocio; 	// NVARCHAR(100)
		public String 		NumeroSerieSocio; 				// NVARCHAR(40)
		public Long 		NumeroDocumentoSocio;   		// BIGINT
		public Date 		FechaDocumentoPagoSocio; 		// NVARCHAR(40)
		public String 		IdMoneda;
		public BigDecimal 	SaldoDisponible;
		public BigDecimal	SaldoContable;
		public Date 		FechaIngresoRegistro;
		public String 		FechaAfectaSaldo;
		public Long 		IdUsuarioGiro;
		public Date 		FechaGiro;
		public Long			IdComprobantePartida;
		public Long			IdNominaTransferencia;
		public Long			IdDeposito;
		public String 		IdEstado;
		public Long 		DiasRetencion;
		public Long			IdPlaza;
		public String 		Depositado; 
		public Long			IdUsuarioCertificacion;
		public Date 		FechaHoraCertificacion; 
		public String 		ClaveDCV; 
		public String 		NumeroCuentaBancariaCorredora;
		public Object clone(){ Object obj=null; try{ obj=super.clone(); }catch(CloneNotSupportedException ex){ System.out.println(" no se puede duplicar"); } return obj; }
		
	}

	public static class Comprobante
	{
		public Long		IdComprobante; 				//	BIGINT  
		public Long		IdEmpresa; 					//	BIGINT  
		public Long		IdUsuario; 					//	BIGINT 
		public String 	TipoComprobante; 				//	NVARCHAR(6)    
		public String 	IdTipoMovimiento; 			//	NVARCHAR(6)   
		public String 	IdConcepto; 					//	NVARCHAR(6)   
		public Long 	IdCaja; 						//	BIGINT 
		public String 	IdMoneda;                     //	NVARCHAR(3) 
		public BigDecimal 	Monto; 						//	DECIMAL   
		public Date 	FechaLiquidacion; 			//	DATE   
		public Date 	FechaIngresoRegistro; 		//	TIMESTAMP   
		public Date 	FechaMovimiento; 				//	DATE   
		public String 	IdTipoPersona; 				//	NVARCHAR(3)   
		public Long 	IdPersona; 					//	BIGINT    
		public String 	NombrePersona; 				//	NVARCHAR(3)   
		public String 	IdTipoDocumentoIdentidad; 	//	NVARCHAR(3)   
		public String 	DocumentoIdentidad; 			//	NVARCHAR(3)   
		public String 	TitularPago; 					//	NVARCHAR(3)   
		public String 	IdTipoPersonaTitularPago; 	//	NVARCHAR(3)   
		public Long 	IdPersonaTitularPago; 		//	BIGINT   
		public String 	NombrePersonaTitularPago; 	//	NVARCHAR(300)   
		public Long 	IdPortafolioTitularPago; 	    //	BIGINT   
		public String 	SecuenciaAgrupacion; 			//	NVARCHAR(3)   
		public String 	EsBilateral; 					//	NVARCHAR(3)   
		public String 	ComprobanteNeteado; 			//	NVARCHAR(1)   
		public String 	SeparadoPorConcepto; 			//	NVARCHAR(1)   
		public String 	GeneradoDesdeMovimientos; 	//	NVARCHAR(1)   
		public Long 	IdUsuarioBloqueo; 			//	BIGINT  
		public Long 	IdCarga; 						//	BIGINT 
		public Long 	IdEvento; 					//	BIGINT 
		public String 	IdEstado; 					//	NVARCHAR(3) 
		public String 	MueveCuentaInversion; 		//	NVARCHAR(1)   
		public String 	ComprobanteAgrupado; 			//	NVARCHAR(1)   
		public Long 	IdUsuarioAnulacion; 			//	BIGINT  
		////////////////
		public Long	IdComprobanteDetalle; 			//	BIGINT  
		public int			NroLinea; 						//	BIGINT 
		public Long	IdEmpleadoDigitador; 			//	BIGINT 
		public Long	IdSucursalEmpleadoDigitador; 	//	BIGINT 
		public String		IdTipoComprobanteOrigen; 		//	BIGINT 
		public Long	IdComprobanteOrigen; 			//	BIGINT 
		public Date			FechaComprobanteOrigen; 		//	BIGINT 
		public Date			FechaLiquidacionComprobanteOrigen; 		//	BIGINT 
		public String		IdTipoProducto; 				//	BIGINT 
		public String		IdTipoOperacion; 				//	BIGINT 
		public String		IdBolsa; 						//	BIGINT 
		public String		IdMonedaOperacion; 				//	BIGINT 
		public BigDecimal	MontoOperacion; 				//	BIGINT 
		public Date		    FechaParidad; 					//	BIGINT 
		public String		IdTipoReajuste; 				//	BIGINT 
		public BigDecimal	ValorParidad; 					//	BIGINT 
		public Long	FolioCartera; 					//	BIGINT 
		public Long	IdInstrumento; 					//	BIGINT 
		public String		IdTipoInstrumento; 				//	BIGINT 
		public String		IdClasificacionTipoInstrumento; 	//	BIGINT 
		public Long	SecuenciaRelacionada; 			//	BIGINT 
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	
	}
	
	public static class ComprobanteDetalle
	{
		
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
		
	
	}
	public static class ComprobanteDetalleOrigen
	{
		public Long		IdComprobanteDetalleOrigen; 	//	BIGINT  
		public Long		IdComprobanteDetalle; 			//	BIGINT  
		public Long		IdIngresoEgreso; 				//	BIGINT 
		public Long		IdComprobante; 					//	BIGINT 
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	
	}

	/**
	INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen"
	("IdComprobanteDetalleOrigen", "IdComprobanteDetalle", "IdIngresoEgreso", "IdComprobante")
	VALUES(0, 0, 0, 0);*/
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT	
	//-------------------------------------------------------------------------------------------	

	public static class CrearComprobante_Salida
	{
		public List<MensajeSalida>			 P_OUT_MENSAJES;
		public Long							 P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida> P_OUT_RESULTADOPROCESO;
		
		public CrearComprobante_Salida()
		{
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADOPROCESO =  new ArrayList<ResultadoProceso_Salida>();
		}
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}


	public static class ResultadoProceso_Salida
	{
		public String 	Campo;        // VARCHAR(40)   -- Ej: "IdComprobante"
		public Long   	Id;           // BIGINT        -- IdComprobante generado  Ej: 9999
		public String 	Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::CrearComprobante"
		public String	Resultado;    // VARCHAR(40)   -- Ej: "OK"
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}	
	
	
	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS DE PROCESAMIENTO
	//-------------------------------------------------------------------------------------------
	
	public static class InformacionDetalle 
	{	 
		public String		Codigo; 
		public String		Descripcion; 
	}


	public static class AdvertenciasDetalle 
	{	
		public String		Codigo; 
		public String		RequiereAutorizacion; 
		public String		Descripcion; 
	}

	public static class ErroresDetalle 
	{
		public String		Codigo; 
		public String		Descripcion; 
	}


	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//------------------------------------------------------------------------------------------
	@Trace
	public CrearComprobante_Salida logica(Connection con, CrearComprobante_Entrada in) throws Exception {
		SQL sql = new SQL();
		CrearComprobante_Salida out = new CrearComprobante_Salida();
		
		logger.info("== INICIO CREAR COMPROBANTE ==");
		try {
			// Insertar Escalares de entrada
			HashMap<String, Object> parmsIN = new HashMap<String, Object>();
			parmsIN.put("p_FechaMovimiento", in.p_FechaMovimiento);
			parmsIN.put("p_IdCaja", in.p_IdCaja);
			parmsIN.put("p_GenerarComprobanteDesdeMovimientos", in.p_GenerarComprobanteDesdeMovimientos);
			parmsIN.put("p_GenerarComprobanteDesdeComprobantes", in.p_GenerarComprobanteDesdeComprobantes);
			parmsIN.put("p_ValidaComprobanteGenerado", 'S');

			HashMap<String, Class> parmsOUT = new HashMap<String, Class>();
		    
			 // Ejecuta el SP (call)
			logger.debug("EJECUTA CALL \"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearComprobantes\" ");
		    HashMap<String, Object> respuestaCC = sql.call(con, SQL_CALL_CrearComprobante, parmsIN, parmsOUT);
			logger.debug("SERVICIO EJECUTADO OK");
	
			HashMap<String,Object> parmsVacio = new HashMap<String,Object>();
			
			// Insertar Escalares de entrada
			SQL.exec(con, CREATE_TEMPORARY_TABLE);
			sql.batch(con, SQL_INSERT_REGISTROS, in.p_IngresoEgresoGenerar, IngresoEgresoGenerar_Entrada.class);
			
			HashMap<String,Object> params = new HashMap<>();
			//params.clear();
			params.put("p_IdEmpresa", in.p_IdEmpresa);
			List<Comprobante> infoIngresoEgreso = sql.query(con, SQL_SELECT_INGRESOEGRESO, params, Comprobante.class); //TODO 
			params.clear();
			params.put("p_IdEmpresa", in.p_IdEmpresa);
			List<FormaDePago_Entrada> infoFormaPago = sql.query(con, SQL_SELECT_FORMA_PAGO, params, FormaDePago_Entrada.class); 
			
			String v_SeparadoPorConcepto = "S";
			String v_GeneradoDesdeMovimientos = "S";
			String v_IdEstado = "ING";
			String ComprobanteAgrupado = "N";
			if (in.p_GenerarComprobanteDesdeMovimientos.equals("S")) { 				
				v_SeparadoPorConcepto = "S";
				v_GeneradoDesdeMovimientos= "S";
				v_IdEstado = "ING";
				ComprobanteAgrupado = "N";
			}
			//*****************************************************************************************************
			if (in.p_GenerarComprobanteDesdeComprobantes.equals("S")) {
				v_SeparadoPorConcepto = "S";
				v_GeneradoDesdeMovimientos= "N";
				v_IdEstado = "GIR";
				ComprobanteAgrupado = "N";
			}
			//Comprobantes
			List<Comprobante> parmsIgresoCom                    = new ArrayList<Comprobante>();
			for(Comprobante movimientos :infoIngresoEgreso) {
				Comprobante parmsIgre = new Comprobante();
				long idComprobante = Utils.getSecuencia(con, "BSCL.Tesoreria.BD.Secuencias::Comprobante", (long) 1).get(0);
				parmsIgre.IdComprobante = idComprobante;
				parmsIgre.IdEmpresa = in.p_IdEmpresa;
				parmsIgre.IdUsuario = in.p_IdUsuario;
				parmsIgre.TipoComprobante = movimientos.TipoComprobante;
				parmsIgre.IdTipoMovimiento =  movimientos.IdTipoMovimiento;
				parmsIgre.IdConcepto =  movimientos.IdConcepto;
				parmsIgre.IdCaja = movimientos.IdCaja;
				parmsIgre.IdMoneda =  movimientos.IdMoneda;//8
				parmsIgre.Monto = movimientos.Monto;
				parmsIgre.FechaLiquidacion = movimientos.FechaLiquidacion;
				parmsIgre.FechaIngresoRegistro = movimientos.FechaIngresoRegistro;
				parmsIgre.FechaMovimiento = movimientos.FechaMovimiento;			
				parmsIgre.IdTipoPersona = movimientos.IdTipoPersona;
				parmsIgre.IdPersona = movimientos.IdPersona;
				parmsIgre.NombrePersona = movimientos.NombrePersona;		
				parmsIgre.IdTipoDocumentoIdentidad = movimientos.IdTipoDocumentoIdentidad;//16
				parmsIgre.DocumentoIdentidad = movimientos.DocumentoIdentidad;
				parmsIgre.TitularPago = movimientos.TitularPago;
				parmsIgre.IdTipoPersonaTitularPago = movimientos.IdTipoPersonaTitularPago;
				parmsIgre.IdPersonaTitularPago =  movimientos.IdPersonaTitularPago;
				parmsIgre.NombrePersonaTitularPago =  movimientos.NombrePersonaTitularPago;
				parmsIgre.IdPortafolioTitularPago = movimientos.IdPortafolioTitularPago;
				parmsIgre.SecuenciaAgrupacion =  movimientos.SecuenciaAgrupacion;
				parmsIgre.EsBilateral = movimientos.EsBilateral;
				parmsIgre.ComprobanteNeteado =  movimientos.ComprobanteNeteado;
				parmsIgre.SeparadoPorConcepto = v_SeparadoPorConcepto; 
				parmsIgre.GeneradoDesdeMovimientos = v_GeneradoDesdeMovimientos; 
				parmsIgre.IdUsuarioBloqueo =  movimientos.IdUsuarioBloqueo;
				parmsIgre.IdCarga =  movimientos.IdCarga;
				parmsIgre.IdEvento =  movimientos.IdEvento;
				parmsIgre.IdEstado = v_IdEstado;
				parmsIgre.MueveCuentaInversion = movimientos.MueveCuentaInversion;
				parmsIgre.ComprobanteAgrupado = ComprobanteAgrupado; 
				parmsIgre.IdUsuarioAnulacion =  movimientos.IdUsuarioAnulacion;
				///////////////////////////// 
				parmsIgre.NroLinea = 1;
				parmsIgre.IdEmpleadoDigitador = movimientos.IdEmpleadoDigitador;
				parmsIgre.IdSucursalEmpleadoDigitador = movimientos.IdSucursalEmpleadoDigitador;
				parmsIgre.IdTipoComprobanteOrigen = movimientos.IdTipoComprobanteOrigen;
				parmsIgre.IdComprobanteOrigen = movimientos.IdComprobanteOrigen;
				parmsIgre.FechaComprobanteOrigen = movimientos.FechaComprobanteOrigen;
				parmsIgre.FechaLiquidacionComprobanteOrigen = movimientos.FechaLiquidacionComprobanteOrigen;
				parmsIgre.IdTipoProducto = movimientos.IdTipoProducto;
				parmsIgre.IdTipoOperacion =movimientos.IdTipoOperacion;
				parmsIgre.IdBolsa = movimientos.IdBolsa;
				parmsIgre.IdMonedaOperacion = movimientos.IdMonedaOperacion;
				parmsIgre.MontoOperacion = movimientos.MontoOperacion;
				parmsIgre.FechaParidad = movimientos.FechaParidad;
				parmsIgre.IdTipoReajuste = movimientos.IdTipoReajuste;
				parmsIgre.ValorParidad = movimientos.ValorParidad;
				parmsIgre.FolioCartera =  movimientos.FolioCartera;
				parmsIgre.IdInstrumento = movimientos.IdInstrumento;
				parmsIgre.IdTipoInstrumento = movimientos.IdTipoInstrumento;
				parmsIgre.IdClasificacionTipoInstrumento = movimientos.IdClasificacionTipoInstrumento;
				parmsIgre.SecuenciaRelacionada =  movimientos.SecuenciaRelacionada;
				parmsIgresoCom.add(parmsIgre); 
			}
			
			//Forma de pago
			List<FormaDePago_Entrada> parmsFormaPago = new ArrayList<FormaDePago_Entrada>();
			for(FormaDePago_Entrada formaPago :infoFormaPago) {
				FormaDePago_Entrada parmsIgre = new FormaDePago_Entrada();
				long idComprobanteFPago = Utils.getSecuencia(con, "BSCL.Tesoreria.BD.Secuencias::ComprobanteFormaDePago", (long) 1).get(0);
				parmsIgre.IdComprobanteFormaDePago = idComprobanteFPago;
				parmsIgre.IdComprobante = 1L;//formaPago.IdComprobante;
				parmsIgre.IdTipoMovimiento = formaPago.IdTipoMovimiento;
				parmsIgre.IdMedioDePago = formaPago.IdMedioDePago;
				parmsIgre.NombreMedioDePago = formaPago.NombreMedioDePago;
				parmsIgre.IdModalidadDePago = formaPago.IdModalidadDePago;
				parmsIgre.NombreModalidadDePago = formaPago.NombreModalidadDePago;
				parmsIgre.IdCuentaBancariaCorredora = formaPago.IdCuentaBancariaCorredora;
				parmsIgre.NombreCuentaBancariaCorredora = formaPago.NombreCuentaBancariaCorredora;
				parmsIgre.NumeroCuentaBancariaCorredora = "00001";//formaPago.NumeroCuentaBancariaCorredora;
				parmsIgre.IdTipoCuentaBancariaCorredora = "1"; //formaPago.IdTipoCuentaBancariaCorredora;
				parmsIgre.NombreTipoCuentaBancariaCorredora = formaPago.NombreCuentaBancariaCorredora;
				parmsIgre.IdTarjetaBancariaCorredora = formaPago.IdTarjetaBancariaCorredora;
				parmsIgre.IdTipoTarjetaBancariaCorredora = formaPago.IdTipoTarjetaBancariaCorredora;
				parmsIgre.NombreTipoTarjetaBancariaCorredora = formaPago.NombreTipoTarjetaBancariaCorredora;
				parmsIgre.NumeroTarjetaBancariaCorredora = formaPago.NumeroTarjetaBancariaCorredora;
				parmsIgre.IdChequera = formaPago.IdChequera;
				parmsIgre.NumeroDocumentoCorredora = formaPago.NumeroDocumentoCorredora;
				parmsIgre.FechaDocumentoPagoCorredora = formaPago.FechaDocumentoPagoCorredora;
				parmsIgre.IdCuentaBancariaSocio = formaPago.IdCuentaBancariaSocio;
				parmsIgre.IdBancoSocio = formaPago.IdBancoSocio;
				parmsIgre.NombreCuentaBancariaSocio = formaPago.NombreCuentaBancariaSocio;
				parmsIgre.NumeroCuentaBancariaSocio = formaPago.NumeroCuentaBancariaSocio;
				parmsIgre.IdTipoCuentaBancariaSocio = formaPago.IdTipoCuentaBancariaSocio;
				parmsIgre.NombreTipoCuentaBancariaSocio = formaPago.NombreTipoCuentaBancariaSocio;
				parmsIgre.IdTarjetaBancariaSocio = formaPago.IdTarjetaBancariaSocio;
				parmsIgre.IdTipoTarjetaBancariaSocio = formaPago.IdTipoTarjetaBancariaSocio;
				parmsIgre.NumeroTarjetaBancariaSocio = formaPago.NumeroTarjetaBancariaSocio;
				parmsIgre.NumeroSerieSocio = formaPago.NumeroSerieSocio;
				parmsIgre.NumeroDocumentoSocio = formaPago.NumeroDocumentoSocio;
				parmsIgre.FechaDocumentoPagoSocio = formaPago.FechaDocumentoPagoSocio;
				parmsIgre.IdMoneda = formaPago.IdMoneda;
				parmsIgre.Monto = formaPago.Monto;
				parmsIgre.SaldoDisponible = formaPago.SaldoDisponible;
				parmsIgre.SaldoContable = formaPago.SaldoContable;
				parmsIgre.FechaIngresoRegistro = formaPago.FechaIngresoRegistro;
				parmsIgre.FechaAfectaSaldo = formaPago.FechaAfectaSaldo;
				parmsIgre.IdUsuarioGiro = formaPago.IdUsuarioGiro;
				parmsIgre.FechaGiro = formaPago.FechaGiro;
				parmsIgre.IdComprobantePartida = formaPago.IdComprobantePartida;
				parmsIgre.IdNominaTransferencia = formaPago.IdNominaTransferencia;
				parmsIgre.IdDeposito = formaPago.IdDeposito;
				parmsIgre.IdEstado = formaPago.IdEstado;
				parmsIgre.DiasRetencion = 1L;//formaPago.DiasRetencion;
				parmsIgre.IdPlaza = 1L;//formaPago.IdPlaza;
				parmsIgre.Depositado = formaPago.Depositado;
				parmsIgre.IdUsuarioCertificacion = formaPago.IdUsuarioCertificacion;
				parmsIgre.FechaHoraCertificacion = formaPago.FechaHoraCertificacion;
				parmsIgre.ClaveDCV = formaPago.ClaveDCV;
				parmsIgre.IdUsuarioLiquidacion = formaPago.IdUsuarioLiquidacion;
				parmsFormaPago.add(parmsIgre); 
			}

			//Comprobantes detalle Origen
			List<ComprobanteDetalleOrigen> parmsIgresoComDetOr = new ArrayList<ComprobanteDetalleOrigen>();
			ComprobanteDetalleOrigen parmsIgreDetOr = new ComprobanteDetalleOrigen();
			parmsIgreDetOr.IdComprobante = 0L;
			parmsIgreDetOr.IdComprobanteDetalle =  0L;
			parmsIgreDetOr.IdComprobanteDetalleOrigen = 0L;
			parmsIgreDetOr.IdIngresoEgreso =  0L;
			parmsIgresoComDetOr.add(parmsIgreDetOr); 
			
			

			//*****************************************************************************************************
			//  VALIDA RESULTADO PROCESO ANTES DE INSERTAR  *
			//*****************************************************************************************************
			sql.batch(con, SQL_INSERT_COMPROBANTES, parmsIgresoCom, Comprobante.class);
			sql.batch(con, SQL_INSERT_COMPROBANTES_DETALLE, parmsIgresoCom, Comprobante.class);
			sql.batch(con, SQL_INSERT_COMPROBANTES_FORMA_PAGO, parmsFormaPago, FormaDePago_Entrada.class);
			if (in.p_SimularOperacion.equals("N")) {
				
			}
			
			
			// Lee datos de tablas temporales globales de salida
			List<ResultadoProceso_Salida> p_out_ResultadoProceso = sql.query(con, SQL_SELECT_ResultadoProceso,parmsVacio,ResultadoProceso_Salida.class);
			List<InformacionDetalle> p_out_InformacionDetalle = sql.query(con, SQL_SELECT_InformacionDetalle,parmsVacio,InformacionDetalle.class);
			List<AdvertenciasDetalle> p_out_AdvertenciasDetalle = sql.query(con, SQL_SELECT_AdvertenciasDetalle,parmsVacio,AdvertenciasDetalle.class);
			List<ErroresDetalle> p_out_ErroresDetalle = sql.query(con, SQL_SELECT_ErroresDetalle,parmsVacio,ErroresDetalle.class);
			
			// Asigna Salida
			out.P_OUT_RESULTADOPROCESO = p_out_ResultadoProceso;
			
			for (InformacionDetalle Informacion: p_out_InformacionDetalle){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK, Informacion.Codigo, Informacion.Descripcion));
			};
			
			for (AdvertenciasDetalle  Advertencia: p_out_AdvertenciasDetalle){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA, Advertencia.Codigo, Advertencia.Descripcion));
			};
			
			for (ErroresDetalle  Errores: p_out_ErroresDetalle){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, Errores.Codigo, Errores.Descripcion));
			};
			
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
			SQL.exec(con, SQL_DELETE_REGISTROS);//TODO  MOVER
			try {
				SQL.exec(con, "DROP TABLE #ComprobanteIngresEgresoAProcesar");
				SQL.exec(con, "DROP TABLE #ComprobanteAProcesar");
			} catch (Exception er) {
			}		
		} 
		finally 
		{	

		}
		logger.info("== TERMINO CREAR COMPROBANTE ==");
		return out;

	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearComprobante_Entrada jin = gson.fromJson(in.toString(), CrearComprobante_Entrada.class);
		CrearComprobante_Salida out = logica(conn, jin);
		JsonParser parser = new JsonParser();
		JsonObject dataOut = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataOut;
	};


	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			System.out.println("== INICIO PRUEBA LOCAL ==");
			try {
				CrearComprobante_Entrada entrada = new CrearComprobante_Entrada();
				
				entrada.p_IdEmpresa         					= 1L; 			// BIGINT  
				entrada.p_IdUsuario         					= 5L; 			// BIGINT   
				entrada.p_IdModulo          					= "TES";		// NVARCHAR(3)
				entrada.p_SimularOperacion  					= "N"; 			// NVARCHAR(1) 
				entrada.p_UsaReglasNegocio  					= "S"; 			// NVARCHAR(1)
				entrada.p_FechaMovimiento   					= "2017-08-25"; // NVARCHAR(10) -- Fecha Sistema
				entrada.p_IdCaja  								= 0L; 			// BIGINT		-- Caja donde se procesaran los comprobantes
				entrada.p_GenerarComprobanteDesdeMovimientos   	= "S"; 			// NVARCHAR(1)  -- S: debe llenar p_IngresoEgresoGenerar
				entrada.p_GenerarComprobanteDesdeComprobantes  	= "N"; 			// NVARCHAR(1)  -- S: debe llenar p_ComprobanteGenerar
				entrada.p_EsIngresoAutomatico  					= "N"; 			// NVARCHAR(1)
				
				
				//Movimientos de I/E para generar comprobantes
				List<IngresoEgresoGenerar_Entrada> IngresoEgresoGenerar = new ArrayList<>();				
				IngresoEgresoGenerar_Entrada ingresoEgresoGenerar1 = new IngresoEgresoGenerar_Entrada(); 
				
				ingresoEgresoGenerar1.NumeroReferencia = 5488L;
				ingresoEgresoGenerar1.IdIngresoEgreso  = 18145L;   // BIGINT
				ingresoEgresoGenerar1.TipoComprobante  = "";       // NVARCHAR(6)   -- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
				IngresoEgresoGenerar.add(ingresoEgresoGenerar1);
				IngresoEgresoGenerar_Entrada ingresoEgresoGenerar2 = new IngresoEgresoGenerar_Entrada(); 
				ingresoEgresoGenerar2.NumeroReferencia = 5489L;
				ingresoEgresoGenerar2.IdIngresoEgreso  = 18146L;   // BIGINT
				ingresoEgresoGenerar2.TipoComprobante  = "";    
				IngresoEgresoGenerar.add(ingresoEgresoGenerar2);
				entrada.p_IngresoEgresoGenerar = IngresoEgresoGenerar;
				
				//Comprobantes para generar comprobantes
				//List<ObjComprobanteGenerar> ComprobanteGenerar = new ArrayList<>();				
				//ObjComprobanteGenerar _comprobanteGenerar = new ObjComprobanteGenerar();
				//_comprobanteGenerar.IdComprobante = 999L;
				
				//Forma de Pago del Comprobante (si no se especifica toma los de los movimientos de origen)
				List<FormaDePago_Entrada> FormaDePago = new ArrayList<>();				
				FormaDePago_Entrada formaDePago = new FormaDePago_Entrada();
				formaDePago.IdMedioDePago = "TRA";				
				formaDePago.Monto = BigDecimal.valueOf(2000000);
				formaDePago.IdCuentaBancariaCorredora = 21L;
				formaDePago.IdModalidadDePago = "MODDEP";
				formaDePago.IdMoneda = "CLP";
				FormaDePago.add(formaDePago);
				entrada.p_FormaDePago = FormaDePago;
				
				
				System.out.println("ENTRADA SERVICIO JAVA:" + entrada.toString());

				
				CrearComprobantePartidaInEg crearComprobante = new CrearComprobantePartidaInEg();
				CrearComprobante_Salida Objout = crearComprobante.logica(con, entrada);
				
				System.out.println("SALIDA: " + Objout.toString());
				con.commit();
				
		    	}
				catch(Exception er) 
				{
					er.printStackTrace();	
					con.rollback();
				}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();			
		}	
		System.out.println("== FIN PRUEBA LOCAL ==");
	};
}