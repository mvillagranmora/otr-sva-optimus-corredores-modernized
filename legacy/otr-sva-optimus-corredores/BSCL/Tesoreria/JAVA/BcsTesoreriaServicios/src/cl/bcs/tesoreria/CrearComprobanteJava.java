package cl.bcs.tesoreria;

/**
 * Proposito: Crear comprobantes de tesoreria a partir de:
 *				1-	Movimientos de tesoreria
 *				2-	Comprobantes de tesoreria (Ingreso, Egresos o Ambos)
 *			   Permite generar Partidas de Ingreso y Partida de Egresos	
 */
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.utils.BcsPojo;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Entrada;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Salida;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.CuentaBanco_Entrada;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class CrearComprobanteJava  implements JavaCall
{
	private static final Logger logger = LogManager.getLogger(CrearComprobanteJava.class);

	
	private final String SQL_INSERT_CuentaBancariaCorredora			= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertCuentaBancariaCorredora.sql");
	private final String SQL_SELECT_ConfiguracionTesoreria			= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_SelectConfiguracionTesoreria.sql");
	private final String SQL_INSERT_EntradaIngresoEgresoGenerar 	= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertEntradaIngresoEgresoGenerar.sql");
	private final String SQL_INSERT_IngresoEgresoGenerarProceso 	= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertIngresoEgresoGenerarProceso.sql");
	private final String SQL_SELECT_IngresoEgresoGenerarProceso 	= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_SelectIngresoEgresoGenerarProceso.sql");
	private final String SQL_DELETE_IngresoEgresoGenerarProceso 	= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_DeleteIngresoEgresoGenerarProceso.sql");
	private final String SQL_INSERT_EntradaFormaDePago 				= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertEntradaFormaDePago.sql");
	private final String SQL_INSERT_FormaDePagoProceso 				= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertFormaDePagoProceso.sql");
	private final String SQL_SELECT_FormaDePagoProceso 				= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_SelectFormaDePagoProceso.sql");
	private final String SQL_INSERT_GruposDeComprobantes 			= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertGruposDeComprobantes.sql");
	private final String SQL_INSERT_MovimientoDetalle   			= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertMovimientoDetalle.sql");
	private final String SQL_INSERT_GlobalComprobante   			= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertGlobalComprobante.sql");
	private final String SQL_INSERT_GlobalComprobanteDetalle   	  	= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertGlobalComprobanteDetalle.sql");
	private final String SQL_INSERT_GlobalComprobanteDetalleOrigen 	= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertGlobalComprobanteDetalleOrigen.sql");
	private final String SQL_INSERT_GlobalComprobanteFormaDePago	= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertGlobalComprobanteFormaDePago.sql");
	private final String SQL_UPDATE_GlobalComprobanteFormaDePago	= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_UpdateGlobalComprobanteFormaDePago.sql");
	private final String SQL_INSERT_Comprobante   					= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertComprobante.sql");
	private final String SQL_INSERT_ComprobanteDetalle   	  		= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertComprobanteDetalle.sql");
	private final String SQL_INSERT_ComprobanteDetalleOrigen 		= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertComprobanteDetalleOrigen.sql");
	private final String SQL_INSERT_ComprobanteFormaDePago			= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_InsertComprobanteFormaDePago.sql");	
	private final String SQL_UDATE_IngresoEgreso					= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_UpdateIngresoEgreso.sql");	
	private final String SQL_UDATE_IngresoEgresoFormaDePago			= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_UpdateIngresoEgresoFormaDePago.sql");	
	private final String SQL_SELECT_CuentaBancariaActualizarSaldo	= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_SelectCuentaBancariaActualizarSaldo.sql");
	private final String SQL_SELECT_ResultadoProceso				= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_SelectResultadoProceso.sql");
	private final String SQL_SELECT_DataValidacionComprobante		= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_SelectDataValidacionComprobante.sql");	
	private final String SQL_SELECT_INSTRUMENTO						= loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_SelectInstrumento.sql");
	
	//*** INICIO: Quitar toda esta seccion cuando se migre la generacion de comprobantes de comprobantes ***
	private final String SQL_INSERT_IngresoEgresoGenerar = "INSERT INTO \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar\"(\"NumeroReferencia\", \"IdIngresoEgreso\", \"TipoComprobante\") VALUES(:NumeroReferencia, :IdIngresoEgreso, :TipoComprobante)";	
	private final String SQL_INSERT_ComprobanteGenerar = "INSERT INTO \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar\"(\"NumeroReferencia\", \"IdComprobante\", \"TipoComprobante\") VALUES(:NumeroReferencia, :IdComprobante, :TipoComprobante)";
	private final String SQL_INSERT_FormaDePago = loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_GBL_InsertFormaDePago.sql");
	private final String SQL_SELECT_ResultadoProcesoSql = "SELECT \"Campo\",\"Id\",\"Servicio\",\"Resultado\" FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso\"";
	private final String SQL_SELECT_InformacionDetalle = "SELECT \"Codigo\",\"Descripcion\" FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_InformacionDetalle\"";
	private final String SQL_SELECT_AdvertenciasDetalle = "SELECT \"Codigo\",\"RequiereAutorizacion\",\"Descripcion\" FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle\"";
	private final String SQL_SELECT_ErroresDetalle = "SELECT \"Codigo\",\"Descripcion\" FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle\"";
	private final String SQL_UPDATE_ESTADO = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteIngreso_UpdateComprobantesEstado.sql");
	
    private final String SQL_CALL_CrearComprobante = "call \"BSCL.Tesoreria.BD.Servicios::CrearComprobante\" ("
   	     + "p_IdEmpresa 		=> :p_IdEmpresa,"  
   	     + "p_IdUsuario 		=> :p_IdUsuario,"
   	     + "p_IdModulo  		=> :p_IdModulo,"
   	     + "p_SimularOperacion 	=> :p_SimularOperacion," 
   	     + "p_UsaReglasNegocio 	=> :p_UsaReglasNegocio,"
   	     + "p_FechaMovimiento  	=> :p_FechaMovimiento,"   	     
		 + "p_IdCaja  			=> :p_IdCaja,"
		 + "p_GenerarComprobanteDesdeMovimientos  	=> :p_GenerarComprobanteDesdeMovimientos,"
		 + "p_GenerarComprobanteDesdeComprobantes 	=> :p_GenerarComprobanteDesdeComprobantes,"
		 + "p_EsIngresoAutomatico  					=> :p_EsIngresoAutomatico,"
   	     + "p_out_InformacionContador  				=> @p_out_InformacionContador,"
   	     + "p_out_AdvertenciasContador 				=> @p_out_AdvertenciasContador,"
   	     + "p_out_ErroresContador      				=> @p_out_ErroresContador)";
    

    private final String SQL_DELETE_ComprobanteGenerar = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar\";";
    private final String SQL_DELETE_ResultadoProceso = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso\";";
	private final String SQL_DELETE_InformacionDetalle = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_InformacionDetalle\";";
	private final String SQL_DELETE_AdvertenciasDetalle = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle\";";
	private final String SQL_DELETE_SalidaErroresDetalle = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle\";";
	private final String SQL_DELETE__TablaCodigosMensajes = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes\";";
	

	//*** FIN: Quitar toda esta seccion cuando se migre la generacion de comprobantes de comprobantes ***
	
	private final String SQL_DELETE_FormaDePago = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_FormaDePago\";";
    private final String SQL_DELETE_IngresoEgresoGenerar = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar\";";
	private final String SQL_DELETE_Comprobante = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante\";";
	private final String SQL_DELETE_ComprobanteDetalle = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalle\";";
	private final String SQL_DELETE_ComprobanteFormaDePago = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago\";";
	private final String SQL_DELETE_ComprobanteDetalleOrigen = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalleOrigen\";";

	private final String SQL_DELETE_IngresoEgresoGenerarComprobante_Proceso = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.IngresoEgresoGenerarComprobante_Proceso\";";
	private final String SQL_DELETE_FormaDePagoGenerarComprobante_Proceso = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.FormaDePagoGenerarComprobante_Proceso\";";
	private final String SQL_DELETE_CrearComprobante_GruposDeComprobantes = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes\";";
	private final String SQL_DELETE_MovimientoDetalleGenerarComprobante = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.MovimientoDetalleGenerarComprobante\";";
	private final String SQL_DELETE_CuentaBancariaCorredoraGenerarComprobante = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CuentaBancariaCorredoraGenerarComprobante\";";
	

	

	
    //-------------------------------------------------------------------------------------------
	// CLASES DE ENTRADA
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
		public String 	p_GenerarComprobanteDesdeComprobantes = "N";	//  NVARCHAR(1)		-- S: debe llenar p_ComprobanteGenerar	
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
			this.p_GenerarComprobanteDesdeComprobantes = "N";	// S: debe llenar p_ComprobanteGenerar
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
		public Long 	IdIngresoEgreso;	// BIGINT		-- Movimientos que conformaran el comprobante
		public String 	TipoComprobante;	// NVARCHAR(6) 	-- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
	}
	
	//Generar comprobantes desde otros comprobantes	
	public static class ComprobanteGenerar_Entrada
	{
		public Long 	NumeroReferencia;   // BIGINT 		 -- Correlativo para relacionar los comprobantes que van a formar un nuevo comprobante
		public Long 	IdComprobante;   	// BIGINT        -- Comprobantes que conformaran el comprobante 
		public String 	TipoComprobante;   	// NVARVCHAR(6)  -- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
	}

	
	
	//Formas de pago para PARING/PAREGR, para comprobantes normales los toma desde el origen
	public static class FormaDePago_Entrada implements Cloneable 
	{
		public Long 		NumeroReferencia;   			// BIGINT
		public String 		IdMedioDePago;  				// NVARCHAR(3)
		public String 		IdModalidadDePago;  			// NVARCHAR(6)
		public String 		IdMoneda;  						// NVARCHAR(3)
		public BigDecimal	Monto;   						// DECIMAL(26,8)
		public Long 		IdCuentaBancariaCorredora;   	// BIGINT		
		public Long 		IdTarjetaBancariaCorredora;   	// BIGINT
		public Long 		IdChequera;   					// BIGINT
		public Long 		NumeroDocumentoCorredora;   	// BIGINT
		public String 		FechaDocumentoPagoCorredora;	// NVARCHAR(40)		
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
		public String 		FechaDocumentoPagoSocio; 		// NVARCHAR(40)

		public Object clone(){ Object obj=null; try{ obj=super.clone(); }catch(CloneNotSupportedException ex){ System.out.println(" no se puede duplicar"); } return obj; }
		
	}

	public static class ObjInstrumento extends BcsPojo
	{
		public Long 					IdInstrumento;
		public String 					Nemotecnico;
	}	
	
    //-------------------------------------------------------------------------------------------
	// CLASES DE SALIDA	
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


	public static class ResultadoProceso_Salida extends BcsPojo
	{
		public String 	Campo;        // VARCHAR(40)   -- Ej: "IdComprobante"
		public Long   	Id;           // BIGINT        -- IdComprobante generado  Ej: 9999
		public String 	Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::CrearComprobante"
		public String	Resultado;    // VARCHAR(40)   -- Ej: "OK"
		public Long		NumeroReferencia;
		
		public ResultadoProceso_Salida(){
			this.NumeroReferencia = 0L;
		}
	}	
	
	
	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS PARA PROCESAMIENTO
	//-------------------------------------------------------------------------------------------
	
	public static class IngresoEgresoGenerar_Proceso 
	{	
		public Long			NumeroReferencia;  			// BIGINT
		public Long			IdIngresoEgresoInformado;  	// BIGINT
		public String		TipoComprobante;  			// NVARCHAR(6)
		public Long			IdIngresoEgreso;  			// BIGINT
		public String		IdTipoMovimiento;  			// NVARCHAR(6)
		public String		IdConcepto;  				// NVARCHAR(6)
		public Long			IdCaja;  					// BIGINT
		public String		IdMoneda;  					// NVARCHAR(3)
		public BigDecimal 	Monto;  					// DECIMAL(26,8)
		public String		FechaLiquidacion;  			// NVARCHAR(10)
		public String		FechaIngresoRegistro;  		// NVARCHAR(40)
		public String		FechaMovimiento;  			// NVARCHAR(10)
		public String		IdTipoPersona;  			// NVARCHAR(3)
		public Long			IdPersona;  				// BIGINT
		public String		IdTipoDocumentoIdentidad;  	// NVARCHAR(3)
		public String		IdTipoPersonaTitularPago;  	// NVARCHAR(3)
		public Long			IdPersonaTitularPago;  		// BIGINT
		public Long			IdPortafolioTitularPago;  	// BIGINT
		public String		SecuenciaAgrupacion;  		// NVARCHAR(40)
		public Long			SecuenciaRelacionada;  		// BIGINT
		public String 		Glosa;  					// NVARCHAR(260)
		public Long			IdEmpleadoDigitador;  		// BIGINT
		public Long			IdSucursalEmpleadoDigitador;// BIGINT,
		public String		IdTipoComprobanteOrigen;  	// NVARCHAR(3),
		public Long			IdComprobanteOrigen;  		// BIGINT,
		public String		FechaComprobanteOrigen;  	// NVARCHAR(40),
		public String		FechaLiquidacionComprobanteOrigen;  //  NVARCHAR(40),
		public String		IdTipoProducto;  			// NVARCHAR(4),
		public String		IdTipoOperacion;  			// NVARCHAR(6),
		public String		IdBolsa;  					// NVARCHAR(3),
		public String		IdMonedaOperacion;  		// NVARCHAR(3),
		public BigDecimal 	MontoOperacion;  			// DECIMAL(26,8),
		public String		FechaParidad;  				// NVARCHAR(40),
		public String		IdTipoReajuste;  			// NVARCHAR(3),
		public BigDecimal 	ValorParidad;  				// DECIMAL(26,8),
		public Long			FolioCartera;  				// BIGINT,
		public Long			IdInstrumento;  			// BIGINT,
		public String		IdTipoInstrumento;  		// NVARCHAR(3),
		public String		IdClasificacionTipoInstrumento;  // NVARCHAR(3),
		public String		EsBilateral;  				// NVARCHAR(1)
		public String		TitularPago;  				// NVARCHAR(1)
		public String		EsIngresoAutomatico;  		// NVARCHAR(1)
		public String		NeteaComprobante;  			// NVARCHAR(1)
		public String		AgrupaComprobante;  		// NVARCHAR(1)
		public String		MueveCuentaInversion;  		// NVARCHAR(1)
		public String		IdEstado;  					// NVARCHAR(3)
		public String 		IdCondicionDeLiquidacion;	// NVARCHAR(10)		
		public Long			IdEmpresa;
		public Long			IdUsuario;
		public Long			IdCajaComprobante;
		public int 			CantidadSecuenciaMovimiento;  //Para validar si falta incluir movimientos segun la secuencia agrupacion comprobante
		public int 			CantidadSecuenciaComprobante; //Para validar que la secuencia de comprobante no este utilizada
		public String		NombrePersona;
		public String		DocumentoIdentidad;
		public String		NombrePersonaTitularPago;
		
		//Para validaciones
		public long 		errores = 0;
		public long 		advertencias = 0;
		public long 		autorizaciones = 0;
		public long 		ok = 0;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}

	public static class FormaDePago_Proceso 
	{
		public Long			NumeroReferencia;      			// BIGINT
		public String		IdMedioDePago;      			// NVARCHAR(3)
		public String		IdModalidadDePago;      		// NVARCHAR(6)
		public String		IdMoneda;      					// NVARCHAR(3)
		public BigDecimal	Monto;      					// DECIMAL(26,8)
		public Long			IdCuentaBancariaCorredora;      // BIGINT		
		public Long			IdTarjetaBancariaCorredora;     // BIGINT
		public Long			IdChequera;      				// BIGINT
		public Long			NumeroDocumentoCorredora;      	// BIGINT
		public String		FechaDocumentoPagoCorredora;    // NVARCHAR(40)		
		public Long			IdCuentaBancariaSocio;      	// BIGINT
		public Long			IdBancoSocio;      				// BIGINT
		public String		NombreCuentaBancariaSocio;      // NVARCHAR(100)
		public String		NumeroCuentaBancariaSocio;      // NVARCHAR(100)
		public String		IdTipoCuentaBancariaSocio;      // NVARCHAR(4)		
		public String		NombreTipoCuentaBancariaSocio;  // NVARCHAR(100)
		public Long			IdTarjetaBancariaSocio;      	// BIGINT
		public String		IdTipoTarjetaBancariaSocio;     // NVARCHAR(40)
		public String		NombreTipoTarjetaBancariaSocio; // NVARCHAR(100)		
		public String		NumeroTarjetaBancariaSocio;     // NVARCHAR(100)
		public String		NumeroSerieSocio;      			// NVARCHAR(40)
		public Long			NumeroDocumentoSocio;      		// BIGINT
		public String		FechaDocumentoPagoSocio;      	// NVARCHAR(40)	
		public String		NombreMedioDePago;      		// NVARCHAR(100)
		public String		NombreModalidadDePago;      	// NVARCHAR(100)
		public Long			IdPersona;      				// BIGINT
		public String		NombreCompleto;      			// NVARCHAR(300)
		public String		DocumentoIdentidadDefecto;      // NVARCHAR(40)
		public String		CodigoTipoPersona;      		// NVARCHAR(2)
		public Long			IdCuentaBanco;      			// BIGINT
		public Long			IdBanco;      					// BIGINT
		public Long			IdPlaza;      					// BIGINT
		public Long			PlazoRetencionMismoBanco;      	// BIGINT
		public Long			PlazoRetencionOtroBanco;      	// BIGINT
		public String		NombreCuentaBancaria;      		// NVARCHAR(100)
		public String		NumeroCuentaBancaria;      		// NVARCHAR(100)
		public String		IdTipoCuentaBancaria;      		// NVARCHAR(4)
		public String		NombreTipoCuentaBancaria;      	// NVARCHAR(100)
		public String		IdTipoTarjetaBancaria;      	// NVARCHAR(4)
		public String		NombreTipoTarjetaBancaria;      // NVARCHAR(40)
		public String		NumeroTarjetaBancaria;      	// NVARCHAR(100)		
		public Long			IdEmpresa;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}

	

	public static class ConfiguracionTesoreria
	{
		public String 	SEPARAPORCONCEPTO;
		public String 	SEPARAPORAGENTE;
		public String 	SEPARAPORSUCURSAL;
		public String 	SEPARAPORCORREDORACONTRAPARTE;
		public String 	SEPARAPORTIPOCOMPROBANTE;
		public String 	SEPARAPORTIPOPRODUCTO;
		public String 	SEPARAPORTIPOOPERACION;
		public String 	SEPARAPORBOLSA;
		public String	SEPARAPORCONDICIONLIQUIDACION;
		    
		public String toString(){
			return Utils.printJson(this, false);
		}
	}	
	
	public static class ValidacionComprobante
	{
		public Long			NumeroReferencia;
		public Long			IdComprobante;
		public Long			IdCaja;
		public String 		IdMoneda;
		public String 		IdTipoPersona;
		public String 		IdMonedaFormaDePago;
		public String		GeneraComprobanteAutomaticamente;
		public Long 		CantidadTipoComprobante;
		public Long 		CantidadMoneda;
		public Long 		CantidadFechaLiquidacion; 		
		public Long 		CantidadFormasDePago;
		public Long 		CantidadMonedaFormaDePago;
		public Long 		CantidadConModalidadCaja;
    	public BigDecimal 	TotalMonto;
    	public BigDecimal	TotalMontoFormaDePago;

		//Para validaciones
		public long 		errores = 0;
		public long 		advertencias = 0;
		public long 		autorizaciones = 0;
		public long 		ok = 0;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}	

	
	public static class ResultadoProceso extends BcsPojo
	{
		public String 	Campo;
		public Long   	Id;
		public String 	Servicio;
		public String	Resultado;
		public String	IdTipoMovimiento;
		public String	CodigoMensaje;
		public Long 	NumeroReferencia;
		
		public ResultadoProceso(){
			this.NumeroReferencia = 0L;
		}
	}	

	//*** INICIO: Quitar toda esta seccion cuando se migre la generacion de comprobantes de comprobantes ***
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
	//*** FIN: Quitar toda esta seccion cuando se migre la generacion de comprobantes de comprobantes ***
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		
		CrearComprobante_Entrada in = gson.fromJson(jin.toString(), CrearComprobante_Entrada.class);
		CrearComprobante_Salida out = logica(con, in);		

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	
	//-------------------------------------------------------------------------------------------
	// METODOS COMPLEMANTARIOS
	//-------------------------------------------------------------------------------------------
	
	private void ProcesaRequisitoIngresoEgresoGenerar(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, IngresoEgresoGenerar_Proceso ieProceso, List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		if (esError) {
			String msg=Diccionario.getMensaje(con,idEmpresa,codMsg)
				  .add("Id", ieProceso.IdIngresoEgresoInformado)
				  .msg();
			
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(ieProceso.IdIngresoEgresoInformado,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				ieProceso.errores++;
			}else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(ieProceso.IdIngresoEgresoInformado,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				ieProceso.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(ieProceso.IdIngresoEgresoInformado,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				ieProceso.advertencias++;
			}
			else {
				ieProceso.ok++;
			}
		}else {
			ieProceso.ok++;
		}		
	}
	
	
	private void ProcesaRequisitoNewComprobante(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, ValidacionComprobante newComprobante, List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		if (esError) {
			String msg=Diccionario.getMensaje(con,idEmpresa,codMsg)
				  .add("IdComprobante", newComprobante.IdComprobante)				
				  .msg();
			
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(newComprobante.IdComprobante,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				newComprobante.errores++;
			}else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(newComprobante.IdComprobante,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				newComprobante.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(newComprobante.IdComprobante,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				newComprobante.advertencias++;
			}
			else {
				newComprobante.ok++;
			}
		}else {
			newComprobante.ok++;
		}		
	}
	
	
	public void limpiarIngresoEgresoGenerar(List<IngresoEgresoGenerar_Proceso> ieProceso, List<IngresoEgresoGenerar_Proceso> ieConErrores) {
		for (IngresoEgresoGenerar_Proceso iePro : ieProceso) {
			if (iePro.errores > 0) {
				ieConErrores.add(iePro);
			}
		}
		
		for (IngresoEgresoGenerar_Proceso ieErr : ieConErrores) {
			if (ieProceso.contains(ieErr))
				ieProceso.remove(ieErr);
		}
	}
	
	public void limpiarFormaDePago(List<IngresoEgresoGenerar_Proceso> ieProceso, List<FormaDePago_Proceso> fpProceso) {	
		for (IngresoEgresoGenerar_Proceso iePro : ieProceso) {
			if (iePro.errores > 0) {
				for (FormaDePago_Proceso fp : fpProceso) {
					if (fp.NumeroReferencia.compareTo(iePro.NumeroReferencia) == 0)
					{
						fpProceso.remove(fp);
					}
				}
			}
		}		
	}
	

	public void limpiarNewComprobante(List<ValidacionComprobante> newComprobante, List<ValidacionComprobante> newComprobanteConErrores) {
		for (ValidacionComprobante newCom : newComprobante) {
			if (newCom.errores > 0) {
				newComprobanteConErrores.add(newCom);
			}
		}
		
		for (ValidacionComprobante newComErr : newComprobanteConErrores) {
			if (newComprobante.contains(newComErr))
				newComprobante.remove(newComErr);
		}
	}




	
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//------------------------------------------------------------------------------------------
	@Trace
	public CrearComprobante_Salida logica(Connection con, CrearComprobante_Entrada in) throws Exception {
		
		logger.info("== INICIO CREAR COMPROBANTE TESORERIA JAVA JAVA ==");		
		
		SQL _sql = new SQL();		
		CrearComprobante_Salida out = new CrearComprobante_Salida();		
		HashMap<String,Object> param = new HashMap<>();
		

	
		//*** INICIO: Quitar toda esta seccion cuando se migre la generacion de comprobantes de comprobantes ***
		if (in.p_GenerarComprobanteDesdeComprobantes.equalsIgnoreCase("S"))
		{
			// Insertar Escalares de entrada
			HashMap<String, Object> parmsIN = new HashMap<String, Object>();
			parmsIN.put("p_IdEmpresa", in.p_IdEmpresa);
			parmsIN.put("p_IdUsuario", in.p_IdUsuario);
			parmsIN.put("p_IdModulo", in.p_IdModulo);
			parmsIN.put("p_SimularOperacion", in.p_SimularOperacion);
			parmsIN.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
			parmsIN.put("p_FechaMovimiento", in.p_FechaMovimiento);
			parmsIN.put("p_IdCaja", in.p_IdCaja);
			parmsIN.put("p_GenerarComprobanteDesdeMovimientos", in.p_GenerarComprobanteDesdeMovimientos);
			parmsIN.put("p_GenerarComprobanteDesdeComprobantes", in.p_GenerarComprobanteDesdeComprobantes);
			parmsIN.put("p_EsIngresoAutomatico", in.p_EsIngresoAutomatico);

			
			HashMap<String, Class> parmsOUT = new HashMap<String, Class>();
			parmsOUT.put("p_out_InformacionContador", Long.class);
		    parmsOUT.put("p_out_AdvertenciasContador", Long.class);
		    parmsOUT.put("p_out_ErroresContador", Long.class);
		    
		    
		    
			// Inserta tablas globales temporales de entrada
			if (in.p_IngresoEgresoGenerar.size() > 0)
			{
				ArrayList<IngresoEgresoGenerar_Entrada> ingresoEgresoGenerar = (ArrayList<IngresoEgresoGenerar_Entrada>)in.p_IngresoEgresoGenerar;
				_sql.batch(con,SQL_INSERT_IngresoEgresoGenerar, ingresoEgresoGenerar, IngresoEgresoGenerar_Entrada.class);
			}
		    
			if (in.p_ComprobanteGenerar.size() > 0)
			{
				ArrayList<ComprobanteGenerar_Entrada> comprobanteGenerar = (ArrayList<ComprobanteGenerar_Entrada>)in.p_ComprobanteGenerar;
				_sql.batch(con,SQL_INSERT_ComprobanteGenerar, comprobanteGenerar, ComprobanteGenerar_Entrada.class);
			}
			
			if (in.p_FormaDePago.size() > 0)
			{
				ArrayList<FormaDePago_Entrada> formaDePago = (ArrayList<FormaDePago_Entrada>)in.p_FormaDePago;
				_sql.batch(con,SQL_INSERT_FormaDePago, formaDePago, FormaDePago_Entrada.class);				
			}			
			
			

		    // Ejecuta el SP (call)
			logger.debug("EJECUTA CALL \"BSCL.Tesoreria.BD.Servicios::CrearComprobante\" ");
		    HashMap<String, Object> respuestaCC = _sql.call(con, SQL_CALL_CrearComprobante, parmsIN, parmsOUT);
		    logger.debug("SERVICIO EJECUTADO OK");
			
			// Lee datos de tablas temporales globales de salida
			List<ResultadoProceso_Salida> p_out_ResultadoProceso = _sql.query(con, SQL_SELECT_ResultadoProcesoSql,param,ResultadoProceso_Salida.class);
			List<InformacionDetalle> p_out_InformacionDetalle = _sql.query(con, SQL_SELECT_InformacionDetalle,param,InformacionDetalle.class);
			List<AdvertenciasDetalle> p_out_AdvertenciasDetalle = _sql.query(con, SQL_SELECT_AdvertenciasDetalle,param,AdvertenciasDetalle.class);
			List<ErroresDetalle> p_out_ErroresDetalle = _sql.query(con, SQL_SELECT_ErroresDetalle,param,ErroresDetalle.class);
			
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
	
			// LIMPIA TABLAS GLOBALES 
			SQL.exec(con,SQL_DELETE_IngresoEgresoGenerar);
			SQL.exec(con,SQL_DELETE_ComprobanteGenerar);
			SQL.exec(con,SQL_DELETE_FormaDePago);
			SQL.exec(con,SQL_DELETE_ResultadoProceso);
			SQL.exec(con,SQL_DELETE_InformacionDetalle);
			SQL.exec(con,SQL_DELETE_AdvertenciasDetalle);
			SQL.exec(con,SQL_DELETE_SalidaErroresDetalle);
			SQL.exec(con,SQL_DELETE_Comprobante);
			SQL.exec(con,SQL_DELETE_ComprobanteDetalle);
			SQL.exec(con,SQL_DELETE_ComprobanteFormaDePago);
			SQL.exec(con,SQL_DELETE_ComprobanteDetalleOrigen);
			SQL.exec(con,SQL_DELETE__TablaCodigosMensajes);
		} //if (in.p_GenerarComprobanteDesdeComprobantes.equalsIgnoreCase("S"))
		//*** FIN: Quitar toda esta seccion cuando se migre la generacion de comprobantes de comprobantes ***
		else // este else debe desaparecer cuando se migre la generacion de comprobantes de comprobantes
		{
			logger.debug("Inicio proceso crear comprobante");
			List<IngresoEgresoGenerar_Proceso> ingresoEgresoGenerar_ConErrores = new ArrayList<IngresoEgresoGenerar_Proceso>();
			
			//Inserta en tabla global parametros de entrada de movimientos de ingreso/egreso con los cuales generar comprobantes
			_sql.batch(con, SQL_INSERT_EntradaIngresoEgresoGenerar, in.p_IngresoEgresoGenerar , IngresoEgresoGenerar_Entrada.class);
			
			//Inserta en tabla global parametros de entrada de forma de pago
			_sql.batch(con, SQL_INSERT_EntradaFormaDePago, in.p_FormaDePago , FormaDePago_Entrada.class);
		
			
			// ----------------------------------------------------------------------------------------------------------
			// OBTIENE DATOS DE LAS CUENTAS BANCARIAS DE LA CORREDORA
			List<IngresoEgresoGenerar_Proceso> lstEntradaCuentaBancariaCorredora = new ArrayList<IngresoEgresoGenerar_Proceso>();
			IngresoEgresoGenerar_Proceso objEntradaCuentaBancariaCorredora = new IngresoEgresoGenerar_Proceso();
			objEntradaCuentaBancariaCorredora.IdEmpresa         = in.p_IdEmpresa;		
			lstEntradaCuentaBancariaCorredora.add(objEntradaCuentaBancariaCorredora);
			logger.debug("Inserta cuentas bancarias corredora");
			_sql.batch(con, SQL_INSERT_CuentaBancariaCorredora, lstEntradaCuentaBancariaCorredora , IngresoEgresoGenerar_Proceso.class);
			
		
			
			// ----------------------------------------------------------------------------------------------------------
			// OBTIENE CONFIGURACION DE TESORERIA PARA AGRUPACION COMPROBANTE	
			param.clear();	
			param.put("IdEmpresa", in.p_IdEmpresa);
			List<ConfiguracionTesoreria> lstConfiguracionTesoreria =  new ArrayList <ConfiguracionTesoreria>();
			//Solo debe salir 1 registro con los parametros de configuracion
			lstConfiguracionTesoreria = _sql.query(con, SQL_SELECT_ConfiguracionTesoreria, param, ConfiguracionTesoreria.class);
			logger.debug("CONFIGURACION TESORERIA > " + lstConfiguracionTesoreria);
			
			
			
			// ----------------------------------------------------------------------------------------------------------
			// OBTIENE DATOS DE PROCESO PARA INGRESOS EGRESOS A GENERAR COMROBANTE
			List<IngresoEgresoGenerar_Proceso> lstEntradaIngresoEgresoProceso = new ArrayList<IngresoEgresoGenerar_Proceso>();
			IngresoEgresoGenerar_Proceso objEntradaIngresoEgresoProceso = new IngresoEgresoGenerar_Proceso();
			objEntradaIngresoEgresoProceso.IdEmpresa         = in.p_IdEmpresa;
			objEntradaIngresoEgresoProceso.IdCajaComprobante = in.p_IdCaja;
			lstEntradaIngresoEgresoProceso.add(objEntradaIngresoEgresoProceso);	
			_sql.batch(con, SQL_INSERT_IngresoEgresoGenerarProceso, lstEntradaIngresoEgresoProceso , IngresoEgresoGenerar_Proceso.class);
			
			param.clear();
			List<IngresoEgresoGenerar_Proceso> ingresoEgresoGenerar_Proceso = new ArrayList<IngresoEgresoGenerar_Proceso>();
			ingresoEgresoGenerar_Proceso = _sql.query(con, SQL_SELECT_IngresoEgresoGenerarProceso, param, IngresoEgresoGenerar_Proceso.class);
			logger.trace("INGRESO EGRESO GENERAR PROCESO > " + ingresoEgresoGenerar_Proceso);

			//Agregar Log Generar Comprobante TESORERIA
			AgregarLog agregarLog = new AgregarLog();
			InAgregarLog entradaLog = new InAgregarLog();

			if (ingresoEgresoGenerar_Proceso != null && !ingresoEgresoGenerar_Proceso.isEmpty()) {

				List<Long> lstIngresoEgresoGenerar = ingresoEgresoGenerar_Proceso.stream().map(procesamiento ->{
					return procesamiento.IdInstrumento;
				}).distinct().collect(Collectors.toList());
			
				List<ObjInstrumento> listaInstrumento = new ArrayList();
				HashMap<Long,Object> hashInstrumento = new HashMap();
			
				if (!lstIngresoEgresoGenerar.isEmpty()) {
					HashMap<String,Object> parmsIn2 = new HashMap<String,Object>();
					parmsIn2.put("IdInstrumento",lstIngresoEgresoGenerar);
					listaInstrumento = _sql.query(con, SQL_SELECT_INSTRUMENTO, parmsIn2, ObjInstrumento.class);	
					hashInstrumento = Utils.List2Hash(listaInstrumento, "IdInstrumento");		
				}
				
				for(IngresoEgresoGenerar_Proceso datosLog : ingresoEgresoGenerar_Proceso) {
					ObjInstrumento instrumento = new ObjInstrumento();
					if(hashInstrumento.size()>0) {
						instrumento = (ObjInstrumento) hashInstrumento.get(datosLog.IdInstrumento);
					}
				
					//Construir entradas log
					InAgregarLog.Log headLog = new InAgregarLog.Log();
					headLog.NombreProceso = "Crear Comprobante Tesoreria";
					headLog.IdCabeceraLog = datosLog.IdComprobanteOrigen != null ? datosLog.IdComprobanteOrigen : 0L;
					headLog.IdTipoLog = "INGRESO";
					headLog.IdModulo = in.p_IdModulo;
					headLog.IdTipoRegistro = "COMPROBANTE_TESORERIA";
					headLog.IdUsuario = in.p_IdUsuario;
					headLog.IdEmpresa = in.p_IdEmpresa;
					headLog.FechaSistemaLog = in.p_FechaMovimiento != null ? in.p_FechaMovimiento : "";
					headLog.IdUsuarioAtendido = in.p_IdUsuario;
					headLog.IdPersona = datosLog.IdPersona != null ? datosLog.IdPersona : 0L;
					headLog.IdPortafolio = datosLog.IdPortafolioTitularPago != null ? datosLog.IdPortafolioTitularPago : 0L;
					headLog.IdTipoOperacion = datosLog.IdTipoOperacion != null ? datosLog.IdTipoOperacion : "";
					headLog.Folio = datosLog.IdComprobanteOrigen != null ? datosLog.IdComprobanteOrigen : 0L;
					headLog.Monto = datosLog.Monto != null ? datosLog.Monto : BigDecimal.ZERO;
					headLog.IdMonedaOrigen = datosLog.IdMoneda != null ? datosLog.IdMoneda : "";
					headLog.Nemo = (instrumento != null && instrumento.Nemotecnico != null) ? instrumento.Nemotecnico : "";
					
					entradaLog.p_Log.add(headLog);
				}	
				//llamar al log
				agregarLog.logica(con, entradaLog);
			}
			
			// ----------------------------------------------------------------------------------------------------------
			// OBTIENE DATOS DE PROCESO PARA FORMAS DE PAGO	
			List<FormaDePago_Proceso> lstEntradaFormaDePagoProceso = new ArrayList<FormaDePago_Proceso>();
			FormaDePago_Proceso objEntradaFormaDePagoProceso = new FormaDePago_Proceso();
			objEntradaFormaDePagoProceso.IdEmpresa         = in.p_IdEmpresa;		
			lstEntradaFormaDePagoProceso.add(objEntradaFormaDePagoProceso);
			_sql.batch(con, SQL_INSERT_FormaDePagoProceso, lstEntradaFormaDePagoProceso , FormaDePago_Proceso.class);		
			
			param.clear();	
			List<FormaDePago_Proceso> formaDePago_Proceso = new ArrayList<FormaDePago_Proceso>();
			formaDePago_Proceso = _sql.query(con, SQL_SELECT_FormaDePagoProceso, param, FormaDePago_Proceso.class);
			logger.debug("FORMA PAGO PROCESO > " + formaDePago_Proceso);
			
			
			// ----------------------------------------------------------------------------------------------------------
			// VALIDACIONES DE NEGOCIO
			if (in.p_UsaReglasNegocio.contentEquals("S"))
			{
				if (in.p_EsIngresoAutomatico.equalsIgnoreCase("N"))
				{
					RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
					RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
					
					if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
						logger.debug("NO CUMPLE REQUISITOS BASICOS");
						out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
						out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
						return out;
					}	
				}
				
				if ((in.p_GenerarComprobanteDesdeMovimientos.equalsIgnoreCase("S") && in.p_IngresoEgresoGenerar.isEmpty()) || (in.p_GenerarComprobanteDesdeComprobantes.equalsIgnoreCase("S") && in.p_ComprobanteGenerar.isEmpty()))
				{
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_VIENEN_MOVIMIENTOS",null,con, in.p_IdEmpresa));
					out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					return out;	
				}
				
				if (in.p_GenerarComprobanteDesdeMovimientos.equalsIgnoreCase("S"))
				{
					for (IngresoEgresoGenerar_Proceso ieProceso : ingresoEgresoGenerar_Proceso) 
					{					
						ProcesaRequisitoIngresoEgresoGenerar(con, in.p_IdEmpresa,(ieProceso.IdIngresoEgreso.compareTo(0L) == 0), "MOVIMIENTO_TESORERIA_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, ieProceso, out.P_OUT_MENSAJES);				
						ProcesaRequisitoIngresoEgresoGenerar(con, in.p_IdEmpresa,("EMI".equalsIgnoreCase(ieProceso.IdEstado)), "MOVIMIENTO_TESORERIA_EMITIDO", MensajeSalida.MENSAJE_ERROR, ieProceso, out.P_OUT_MENSAJES);				
						ProcesaRequisitoIngresoEgresoGenerar(con, in.p_IdEmpresa,("BLO".equalsIgnoreCase(ieProceso.IdEstado)), "MOVIMIENTO_TESORERIA_BLOQUEADO", MensajeSalida.MENSAJE_ERROR, ieProceso, out.P_OUT_MENSAJES);				
						ProcesaRequisitoIngresoEgresoGenerar(con, in.p_IdEmpresa,("ANU".equalsIgnoreCase(ieProceso.IdEstado)), "MOVIMIENTO_TESORERIA_ANULADO", MensajeSalida.MENSAJE_ERROR, ieProceso, out.P_OUT_MENSAJES);	
						ProcesaRequisitoIngresoEgresoGenerar(con, in.p_IdEmpresa,("PDA".equalsIgnoreCase(ieProceso.IdEstado)), "MOVIMIENTO_TESORERIA_PENDIENTE_AUTORIZACION", MensajeSalida.MENSAJE_ERROR, ieProceso, out.P_OUT_MENSAJES);
						ProcesaRequisitoIngresoEgresoGenerar(con, in.p_IdEmpresa,(ieProceso.CantidadSecuenciaComprobante > 1), "COMPROBANTE_TESORERIA_SECUENCIA_UTILIZADA", MensajeSalida.MENSAJE_ERROR, ieProceso, out.P_OUT_MENSAJES);				
						ProcesaRequisitoIngresoEgresoGenerar(con, in.p_IdEmpresa,(ieProceso.CantidadSecuenciaMovimiento > 1),  "FALTA_INCLUIR_MOVIMIENTOS_SECUENCIA_AGRUPACION_COMPROBANTE", MensajeSalida.MENSAJE_ERROR, ieProceso, out.P_OUT_MENSAJES);				
					}	

					limpiarIngresoEgresoGenerar(ingresoEgresoGenerar_Proceso, ingresoEgresoGenerar_ConErrores);
					
					limpiarFormaDePago(ingresoEgresoGenerar_Proceso, formaDePago_Proceso);
				
					
					if (!ingresoEgresoGenerar_ConErrores.isEmpty())
					{
						// Borra de la tabla proceso global los registron invalidos
						_sql.batch(con, SQL_DELETE_IngresoEgresoGenerarProceso, ingresoEgresoGenerar_ConErrores, IngresoEgresoGenerar_Proceso.class);
					}
					
					if (in.p_GenerarComprobanteDesdeMovimientos.equalsIgnoreCase("S") && ingresoEgresoGenerar_Proceso.isEmpty())
					{
						out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
						return out;					
					}
				} // if (in.p_GenerarComprobanteDesdeMovimientos.equalsIgnoreCase("S"))
				
		  	
			} //if (in.p_UsaReglasNegocio.contentEquals("S"))
			
			
			// ----------------------------------------------------------------------------------------------------------
			// GENERAR COMPROBANTES DESDE MOVIMIENTOS
			if (in.p_GenerarComprobanteDesdeMovimientos.equalsIgnoreCase("S"))
			{
				
				// Relaciona los movimientos con la configuracion de agrupacion, para generar los grupos de comprobantes			
				_sql.batch(con, SQL_INSERT_GruposDeComprobantes, lstConfiguracionTesoreria , ConfiguracionTesoreria.class);	
				
				// Obtiene detalle de movimientos para generar comprobante (movimiento unico por comprobante y agrupados por comprobante)
				List<IngresoEgresoGenerar_Proceso> lstMovimientoDetalle = new ArrayList<IngresoEgresoGenerar_Proceso>();
				IngresoEgresoGenerar_Proceso objMovimientoDetalle = new IngresoEgresoGenerar_Proceso();
				objMovimientoDetalle.IdEmpresa      	= in.p_IdEmpresa;
				objMovimientoDetalle.IdUsuario 			= in.p_IdUsuario;
				objMovimientoDetalle.FechaMovimiento 	= in.p_FechaMovimiento;
				objMovimientoDetalle.IdCajaComprobante 	= in.p_IdCaja;
				lstMovimientoDetalle.add(objMovimientoDetalle);			
			
				_sql.batch(con, SQL_INSERT_MovimientoDetalle, lstMovimientoDetalle , IngresoEgresoGenerar_Proceso.class);
				
				
			
				
				// Inserta datos de comprobantes en tablas globales
				List<IngresoEgresoGenerar_Proceso> lstInsertGlobales = new ArrayList<IngresoEgresoGenerar_Proceso>();
				IngresoEgresoGenerar_Proceso objInsertGlobales = new IngresoEgresoGenerar_Proceso();		
				objInsertGlobales.IdEmpresa 	= in.p_IdEmpresa;			
				lstInsertGlobales.add(objInsertGlobales);
				
				//Inserta en global datos para comprobante
				_sql.batch(con, SQL_INSERT_GlobalComprobante, lstInsertGlobales , IngresoEgresoGenerar_Proceso.class);	
				
				//Inserta en global datos para comprobante detalle
				_sql.batch(con, SQL_INSERT_GlobalComprobanteDetalle, lstInsertGlobales , IngresoEgresoGenerar_Proceso.class);
				
				//Inserta en global datos para comprobante detalle origen
				_sql.batch(con, SQL_INSERT_GlobalComprobanteDetalleOrigen, lstInsertGlobales , IngresoEgresoGenerar_Proceso.class);
				
				//Inserta en global datos para comprobante forma de pago
				_sql.batch(con, SQL_INSERT_GlobalComprobanteFormaDePago, lstInsertGlobales , IngresoEgresoGenerar_Proceso.class);
				
				

				
				// Actualizar Global de Formas de Pago(DiasRetencion y FechaAfectaSaldo)
				List<IngresoEgresoGenerar_Proceso> lstActualizarFP = new ArrayList<IngresoEgresoGenerar_Proceso>();
				IngresoEgresoGenerar_Proceso objActualizarFP = new IngresoEgresoGenerar_Proceso();		
				objActualizarFP.FechaMovimiento 	= in.p_FechaMovimiento;			
				lstActualizarFP.add(objActualizarFP);			
			
				_sql.batch(con, SQL_UPDATE_GlobalComprobanteFormaDePago, lstActualizarFP , IngresoEgresoGenerar_Proceso.class);			

				
			} // if (in.p_GenerarComprobanteDesdeMovimientos.equalsIgnoreCase("S"))
			
			if (in.p_GenerarComprobanteDesdeComprobantes.equalsIgnoreCase("S"))
			{
				// TODO :  aca se debe migrar la generacion de comprobantes desde comprobantes
				//         Actualmente se esta baypaseando al SQL para estos casos(en la parte superior de este servicio)
			}

			
			
			// ----------------------------------------------------------------------------------------------------------
			// VALIDA RESULTADO PROCESO ANTES DE INSERTAR (Valida data de nuevos comprobantes a crear)	
			
			List<ValidacionComprobante> validacionNewComprobante = new ArrayList<ValidacionComprobante>();
			List<ValidacionComprobante> validacionNewComprobante_ConErrores = new ArrayList<ValidacionComprobante>();
			param.clear();
			validacionNewComprobante = _sql.query(con, SQL_SELECT_DataValidacionComprobante, param, ValidacionComprobante.class);;
			
			for (ValidacionComprobante newComprobante : validacionNewComprobante) 
			{
				ProcesaRequisitoNewComprobante(con, in.p_IdEmpresa,newComprobante.CantidadTipoComprobante.compareTo(1L) == 1, "COMPROBANTE_CON_MEZCLA_DE_TIPO_COMPROBANTE", MensajeSalida.MENSAJE_ERROR, newComprobante, out.P_OUT_MENSAJES);	
				ProcesaRequisitoNewComprobante(con, in.p_IdEmpresa,newComprobante.CantidadMoneda.compareTo(1L) == 1, "COMPROBANTE_CON_MEZCLA_DE_MONEDAS", MensajeSalida.MENSAJE_ERROR, newComprobante, out.P_OUT_MENSAJES);	
				ProcesaRequisitoNewComprobante(con, in.p_IdEmpresa,newComprobante.CantidadFechaLiquidacion.compareTo(1L) == 1, "COMPROBANTE_CON_FECHAS_DISTINTAS_LIQUIDACION", MensajeSalida.MENSAJE_ERROR, newComprobante, out.P_OUT_MENSAJES);	
				ProcesaRequisitoNewComprobante(con, in.p_IdEmpresa,(newComprobante.CantidadFormasDePago.compareTo(0L) == 0 && newComprobante.IdTipoPersona.equalsIgnoreCase("CLI") && "N".equals(newComprobante.GeneraComprobanteAutomaticamente)), "NO_VIENEN_FORMAS_DE_PAGO", MensajeSalida.MENSAJE_ERROR, newComprobante, out.P_OUT_MENSAJES);	
				ProcesaRequisitoNewComprobante(con, in.p_IdEmpresa,(newComprobante.CantidadFormasDePago.compareTo(0L) > 0 && newComprobante.CantidadMonedaFormaDePago > 1), "FORMA_PAGO_CON_MEZCLA_DE_MONEDAS", MensajeSalida.MENSAJE_ERROR, newComprobante, out.P_OUT_MENSAJES);	
				ProcesaRequisitoNewComprobante(con, in.p_IdEmpresa,(newComprobante.CantidadFormasDePago.compareTo(0L) > 0 && !newComprobante.IdMoneda.equalsIgnoreCase(newComprobante.IdMonedaFormaDePago)), "DIFERENTE_MONEDA_ENTRE_COMPROBANTE_Y_FORMA_PAGO", MensajeSalida.MENSAJE_ERROR, newComprobante, out.P_OUT_MENSAJES);	
				ProcesaRequisitoNewComprobante(con, in.p_IdEmpresa,(newComprobante.CantidadFormasDePago.compareTo(0L) > 0 && ((newComprobante.TotalMonto.abs()).subtract(newComprobante.TotalMontoFormaDePago)).compareTo(BigDecimal.ZERO)<0), "MONTO_FORMA_PAGO_MAYOR_A_COMPROBANTE", MensajeSalida.MENSAJE_ERROR, newComprobante, out.P_OUT_MENSAJES);			
				ProcesaRequisitoNewComprobante(con, in.p_IdEmpresa,(newComprobante.CantidadFormasDePago.compareTo(0L) > 0 && ((newComprobante.TotalMonto.setScale(2, RoundingMode.FLOOR).abs()).subtract(newComprobante.TotalMontoFormaDePago.setScale(2, RoundingMode.FLOOR))).compareTo(BigDecimal.ZERO) > 0), "MONTO_FORMA_PAGO_MENOR_A_COMPROBANTE", MensajeSalida.MENSAJE_ERROR, newComprobante, out.P_OUT_MENSAJES);	
				ProcesaRequisitoNewComprobante(con, in.p_IdEmpresa,(newComprobante.CantidadConModalidadCaja.compareTo(0L) > 0 && newComprobante.IdCaja.compareTo(0L) == 0), "COMPROBANTE_SIN_CAJA_ASIGNADA", MensajeSalida.MENSAJE_ERROR, newComprobante, out.P_OUT_MENSAJES);	
			}
		
	
			limpiarNewComprobante(validacionNewComprobante, validacionNewComprobante_ConErrores);
			
			if (validacionNewComprobante_ConErrores.size() > 0)
			{
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return out;		
			}

			
			if (in.p_SimularOperacion.equalsIgnoreCase("N"))
			{
				logger.debug("Inserta comprobantes");
				// Inserta datos de los nuevos comprobantes
				List<IngresoEgresoGenerar_Proceso> lstInsertComprobante = new ArrayList<IngresoEgresoGenerar_Proceso>();
				IngresoEgresoGenerar_Proceso objInsertComprobante = new IngresoEgresoGenerar_Proceso();		
				objInsertComprobante.IdEmpresa 	= in.p_IdEmpresa;
				objInsertComprobante.IdUsuario  = in.p_IdUsuario;
				objInsertComprobante.FechaMovimiento = in.p_FechaMovimiento;
				lstInsertComprobante.add(objInsertComprobante);
				
				//Inserta datos de comprobantes
				logger.debug("Insert Comprobante");
				_sql.batch(con, SQL_INSERT_Comprobante, lstInsertComprobante , IngresoEgresoGenerar_Proceso.class);	
				
				logger.debug("Insert Comprobante Detalle");
				//Inserta datos de comprobante detalle
				_sql.batch(con, SQL_INSERT_ComprobanteDetalle, lstInsertComprobante , IngresoEgresoGenerar_Proceso.class);
				
				logger.debug("Insert Comprobante Forma de Pago");
				//Inserta datos de comprobante forma de pago
				_sql.batch(con, SQL_INSERT_ComprobanteFormaDePago, lstInsertComprobante , IngresoEgresoGenerar_Proceso.class);
				
				logger.debug("Insert Comprobante Detalle Origen");
				//Inserta datos de comprobante detalle origen
				_sql.batch(con, SQL_INSERT_ComprobanteDetalleOrigen, lstInsertComprobante , IngresoEgresoGenerar_Proceso.class);
				
				
				if (in.p_GenerarComprobanteDesdeMovimientos.equalsIgnoreCase("S"))
				{
					// Actualiza estado de los movimientos de ingreso egreso
					_sql.batch(con, SQL_UDATE_IngresoEgreso, lstInsertComprobante , IngresoEgresoGenerar_Proceso.class);
					// Actualiza estado de formas de pago de los movimientos de ingreso egreso
					_sql.batch(con, SQL_UDATE_IngresoEgresoFormaDePago, lstInsertComprobante , IngresoEgresoGenerar_Proceso.class);								
				}


				// --------------------------------------------------------------------------
				// ACTUALIZAR SALDOS DE CUENTAS BANCARIAS DE LA CORRECORA			
				ActualizarSaldosCuentaBanco servicioActualizarSaldosCuentaBanco = new ActualizarSaldosCuentaBanco();
				ActualizarSaldosCuentaBanco_Entrada entradaActualizarSaldosCuentaBanco = new ActualizarSaldosCuentaBanco_Entrada();
							
				param.clear();			
				List<CuentaBanco_Entrada> lstEntradaCuentaBanco = _sql.query(con, SQL_SELECT_CuentaBancariaActualizarSaldo, param, CuentaBanco_Entrada.class);
				
				if (!lstEntradaCuentaBanco.isEmpty())
				{
					entradaActualizarSaldosCuentaBanco.p_FechaMovimiento = in.p_FechaMovimiento;
					entradaActualizarSaldosCuentaBanco.p_CuentaBanco = lstEntradaCuentaBanco;
					
					ActualizarSaldosCuentaBanco_Salida salidaActualizarSaldosCuentaBanco = servicioActualizarSaldosCuentaBanco.logica(con, entradaActualizarSaldosCuentaBanco);
				}
			
			} // if (in.p_SimularOperacion.equalsIgnoreCase("N"))
			
			
			// --------------------------------------------------------------------------
			// PREPARA RESULTADO DEL PROCESO
			param.clear();
			List<ResultadoProceso> lstResultadoProceso = _sql.query(con, SQL_SELECT_ResultadoProceso, param, ResultadoProceso.class);		
			
			HashMap<String, String> valorMensaje = new HashMap<>();
			for (ResultadoProceso resul : lstResultadoProceso)
			{
				ResultadoProceso_Salida salidaResultadoProceso = new ResultadoProceso_Salida();			
				salidaResultadoProceso.Campo = resul.Campo;			
				salidaResultadoProceso.Id = resul.Id;
				salidaResultadoProceso.Servicio = resul.Servicio;
				salidaResultadoProceso.Resultado = resul.Resultado;
				salidaResultadoProceso.NumeroReferencia = resul.NumeroReferencia;
				out.P_OUT_RESULTADOPROCESO.add(salidaResultadoProceso);
				
				
				//Despliega mensaje de exito por cada comprobante generado
				if (!resul.CodigoMensaje.equalsIgnoreCase("") && resul.Campo.equalsIgnoreCase("IdComprobante") && resul.Resultado.equalsIgnoreCase("ING"))
				{
					valorMensaje.clear();
					valorMensaje.put("IdComprobante", resul.Id.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(resul.Id,MensajeSalida.MENSAJE_OK, resul.CodigoMensaje, valorMensaje, con, in.p_IdEmpresa));
				}
			}
			
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
		}  //  este else debe desaparecer cuando se migre la generacion de comprobantes de comprobantes
		
		// LIMPIA TABLAS GLOBALES
		logger.debug("Limpia tablas globales");
		SQL.exec(con,SQL_DELETE_FormaDePago);
		SQL.exec(con,SQL_DELETE_IngresoEgresoGenerar);
		SQL.exec(con,SQL_DELETE_Comprobante);
		SQL.exec(con,SQL_DELETE_ComprobanteDetalle);
		SQL.exec(con,SQL_DELETE_ComprobanteFormaDePago);
		SQL.exec(con,SQL_DELETE_ComprobanteDetalleOrigen);	
		SQL.exec(con,SQL_DELETE_IngresoEgresoGenerarComprobante_Proceso);
		SQL.exec(con,SQL_DELETE_FormaDePagoGenerarComprobante_Proceso);
		SQL.exec(con,SQL_DELETE_CrearComprobante_GruposDeComprobantes);
		SQL.exec(con,SQL_DELETE_MovimientoDetalleGenerarComprobante);
		SQL.exec(con,SQL_DELETE_CuentaBancariaCorredoraGenerarComprobante);
		
		logger.info("== TERMINO CREAR COMPROBANTE JAVA JAVA ==");
		return out;
		

	}
	
	
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------	
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = Utils.getGsonBuilder();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2020-03-19\",\"p_GenerarComprobanteDesdeMovimientos\":\"S\",\"p_IngresoEgresoGenerar\":[{\"NumeroReferencia\":0,\"IdIngresoEgreso\":18397,\"TipoComprobante\":\"\"}],\"p_ComprobanteGenerar\":[],\"p_FormasDePago\":[]}";
			CrearComprobante_Entrada Objin = gson.fromJson(json, CrearComprobante_Entrada.class);
			CrearComprobanteJava demo = new CrearComprobanteJava();
			
 			System.out.println("ENTRADA: " + Objin.toString());
 			
 			try 
 			{
 				CrearComprobante_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				//con.commit();
 				con.rollback();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();
			}
 			con.close();			
	    }	 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
	
}


