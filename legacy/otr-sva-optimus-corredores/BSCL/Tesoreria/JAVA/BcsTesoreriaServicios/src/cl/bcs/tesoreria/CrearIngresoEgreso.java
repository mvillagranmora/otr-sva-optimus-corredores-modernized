package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.utils.BcsPojo;
import java.util.stream.Collectors;

import cl.bcs.tesoreria.pojo.IngresoEgreso;
import cl.bcs.tesoreria.pojo.IngresoEgresoFormaDePago;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.tesoreria.CrearComprobante.CrearComprobante_Entrada; 
import cl.bcs.tesoreria.CrearComprobante.CrearComprobante_Salida;
import cl.bcs.tesoreria.CrearComprobante.IngresoEgresoGenerar_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.ComprobanteLiquidar_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Salida;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;



public class CrearIngresoEgreso implements JavaCall {
	
	// Log
	private static final Logger logger = LogManager.getLogger(CrearIngresoEgreso.class);
	
	
	// Queries
	private final String SQL_INSERT_IngresoEgreso 					 = loadResourceToString("cl/bcs/tesoreria/sql/CrearIngresoEgreso_InsertIngresoEgreso.sql");	
	private final String SQL_INSERT_IngresoEgresoFormaDePago 		 = loadResourceToString("cl/bcs/tesoreria/sql/CrearIngresoEgreso_InsertFormaDePago.sql");
	private final String SQL_SELECT_PreferenciasNegocioTesoreria 	 = loadResourceToString("cl/bcs/tesoreria/sql/Globales_SelectPreferenciasNegocioTesoreria.sql");
	private final String SQL_INSERT_IngresoEgreso_Proceso 			 = loadResourceToString("cl/bcs/tesoreria/sql/CrearIngresoEgreso_InsertIngresoEgresoProceso.sql");
	private final String SQL_SELECT_IngresoEgreso_Proceso 			 = loadResourceToString("cl/bcs/tesoreria/sql/CrearIngresoEgreso_SelectIngresoEgresoProceso.sql");	
	private final String SQL_INSERT_IngresoEgresoFormaDePago_Proceso = loadResourceToString("cl/bcs/tesoreria/sql/CrearIngresoEgreso_InsertIngresoEgresoFormaDePagoProceso.sql");
	private final String SQL_SELECT_IngresoEgresoFormaDePago_Proceso = loadResourceToString("cl/bcs/tesoreria/sql/CrearIngresoEgreso_SelectIngresoEgresoFormaDePagoProceso.sql");
	private final String SQL_SELECT_COMPROBANTES 					 = loadResourceToString("cl/bcs/tesoreria/sql/CrearIngresoEgreso_SelectComprobantes.sql");
	private final String SQL_SELECT_Corredora 						 = loadResourceToString("cl/bcs/tesoreria/sql/CrearIngresoEgreso_SelectCorredora.sql");
	private final String SQL_SELECT_INSTRUMENTO                      = loadResourceToString("cl/bcs/tesoreria/sql/CrearIngresoEgreso_SelectInstrumento.sql");
	private final String SQL_SELECT_EMPLEADO_DIGITADOR		 		 = loadResourceToString("cl/bcs/tesoreria/sql/CrearIngresoEgreso_SelectCodigoEjecutivoDigitador.sql");

	private final String SQL_DELETE_IngresoEgreso_Proceso 			 = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso\";\r\n"+"";
	private final String SQL_DELETE_IngresoEgresoFormaDePago_Proceso = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearIngresoEgresoFormaDePago_Proceso\";";
	 
	private final ConstantesOrdenes _c = new ConstantesOrdenes();

    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS IN
	//-------------------------------------------------------------------------------------------

	public static class CrearIngresoEgreso_Entrada 
	{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_SimularOperacion = "N";
		public String						p_UsaReglasNegocio;		
		public String						p_IdOrigenProceso;
		public String                       p_EsIngresoAutomatico = "S";        //S: Ingreso automático   N: Ingreso Manual
		public String						p_EsIngresoUI = "N";
		public String						p_FechaMovimiento = "0001-01-01";   // Fecha de Sistema(si no viene le asigna fecha sistema)  //////// nuevo jv
		public List<IngresoEgreso_Entrada>	p_IngresoEgresoGenerar;
		public List<FormaDePago_Entrada>	p_FormaDePago;
		

		public CrearIngresoEgreso_Entrada()
		{
			this.p_EsIngresoAutomatico 	= "S";
			this.p_EsIngresoUI			= "N";
			this.p_SimularOperacion     = "N";
			this.p_FechaMovimiento 		= "0001-01-01";
			this.p_IngresoEgresoGenerar = new ArrayList<IngresoEgreso_Entrada>();
			this.p_FormaDePago 			= new ArrayList<FormaDePago_Entrada>();
		}		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	
	//Entrada para movimientos de ingreso/egreso
	public static class IngresoEgreso_Entrada implements Cloneable
	{
		public Long      	NumeroReferencia;  				//	BIGINT 
		public String    	IdTipoMovimiento;  		 		//	NVARCHAR(6)    -- MOVING, MOVEGR
		public String    	IdTipoPersona;  		 		//	NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
	   	public Long      	IdPersona;  		 			//	BIGINT
	   	public Long      	IdPortafolio;  		 			//	BIGINT 
	   	public String    	IdMoneda;  		 				//	NVARCHAR(3)
	   	public BigDecimal 	Monto 				= BigDecimal.ZERO;  		 				//	DECIMAL(26,8)
	   	public String   	FechaLiquidacion;  		 		//	NVARCHAR(10)                                                                      
	   	public String    	IdConcepto;  		 			//	NVARCHAR(6)
	   	public String    	IdTipoPersonaTitularPago;  		//	NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
	   	public Long      	IdPersonaTitularPago;  		 	//	BIGINT
	   	public Long      	IdPortafolioTitularPago;  		//	BIGINT
	   	public Long      	IdCaja;  		 				//	BIGINT
	   	public String    	SecuenciaAgrupacion = ""; 		//	NVARCHAR(40)  -- Movimientos con una misma secuencia se agrupan en un mismo comprobante(también influyen parámetros de agrupación)
	   	public Long      	SecuenciaRelacionada = 0L;	 	//	BIGINT		  -- Permite relacionar distintos mov y generar una orden de liquidación entre ellos
	   	public String    	Glosa;  		 				//	NVARCHAR(260)
		public String    	RequiereAutorizacion = "N";  	//	NVARCHAR(260)	
										   	
	   	//Parámetros Referenciales(Opcionales)
	   	public Long      	IdEmpleadoDigitador;  		 	//	BIGINT
	   	public Long      	IdSucursalEmpleadoDigitador;  	//	BIGINT
	   	public String   	IdTipoProducto;  		 		//	NVARCHAR(4)
	   	public String    	IdTipoOperacion;  		 		//	NVARCHAR(6)									    
	   	public String    	IdTipoComprobante;  		 	//	NVARCHAR(3)
	   	public Long      	IdComprobante;  		 		//	BIGINT
	   	public String    	FechaComprobante;  		 		//	NVARCHAR(40)
	   	public String    	FechaLiquidacionComprobante;  	//	NVARCHAR(40)
										    
	   	public Long     	IdOperacion;  		 			//	BIGINT			
	    public String    	FechaOperacion;  		 		//	NVARCHAR(40)
	    public String   	IdCondicionLiquidacion;  		//	NVARCHAR(2)
	    public String    	IdBolsa;  		 				//	NVARCHAR(3)
	    public String    	IdMonedaOperacion;  		 	//	NVARCHAR(3)
	    public BigDecimal 	MontoOperacion	= BigDecimal.ZERO;   				//	DECIMAL(26,8)
	    public String    	IdTipoReajuste;  		 		//	NVARCHAR(3)
	    public String    	FechaParidad;  		 			//	NVARCHAR(40)
	    public BigDecimal 	ValorParidad	= BigDecimal.ZERO;   				// 	DECIMAL(26,8)
	    public Long     	FolioCartera;  		 			//	BIGINT
	    public Long      	IdInstrumento;  		 		//	BIGINT
	    public String    	IdTipoInstrumento;  		 	//	NVARCHAR(3)
	    public String    	IdClasificacionTipoInstrumento; //	NVARCHAR(3)

	    //Indicadores
	    public String    	Bilateral = "N";		        //	NVARCHAR(1)  -- Liquidación bilateral entre corredores, no pasa por el CCLV
	    public String    	GeneraComprobante = "N"; 		//	NVARCHAR(1)  -- S/N: genera comprobante automático
	    public String    	LiquidarComprobante = "N"; 		//	NVARCHAR(1)  -- S/N: genera comprobante automático
	    public String    	AgruparEnComprobante = "N"; 	//	NVARCHAR(1)  -- S/N: permite agrupar varios mov. en un comprobante
	    public String    	MueveCuentaInversion;  			//	NVARCHAR(1)  -- S/N: mueve cuenta inversion // *** No se usa el campo, se debe borrar ? ***
	    public String    	TitularPago;  		 			//	NVARCHAR(1)  -- S/N: Socio de negocio que genera el movimiento es quién cobra o paga
	    public String		EsTraspaso = "N";				//  NVARCHAR(1)  -- S/N: Si es traspaso y se esta generando el ingreso y egreso
	    public String		IdTipoCanalIngreso = "OPT"; 
	    public String		CodigoEjecutivoDigitador = "";
	    
	    //Uso exclusivo Aporte CSC
	    public String		FolioCSC = "";
	    
		public IngresoEgreso_Entrada()
		{
			this.Bilateral 	          = "N";    //Valores por defecto
			this.GeneraComprobante    = "N";
			this.LiquidarComprobante  = "N";
			this.AgruparEnComprobante = "N";
			this.SecuenciaAgrupacion  = "";
			this.SecuenciaRelacionada = 0L;
			this.EsTraspaso = "N";
		}	
		public String toString(){
			return Utils.printJson(this, false);
		}
		public Object clone(){ Object obj=null; try{ obj=super.clone(); }catch(CloneNotSupportedException ex){ System.out.println(" no se puede duplicar"); } return obj; }
		
	}

	 
	//Entrada para formas de pago de los movimientos	
	public static class FormaDePago_Entrada implements Cloneable 
	{
		public Long 		NumeroReferencia;				//  BIGINT        Id Referencial (para unir el movimiento con su forma de pago)
		public Long 		NumeroReferenciaFormaDePago;	//  BIGINT        Id Referencia para FP (1,2,3,..) debe ser único por FP aunque sean de distintos movimientos
		public String		IdMedioDePago;          		//	NVARCHAR(3)   Ej.: CHE, EFE, TRA, VVI, CIN
		public String		IdModalidadDePago;      		//	NVARCHAR(6)   Ej.: MODCAJ: Por Caja   MODDEP: Depósito
		public String		IdMoneda;               		//	NVARCHAR(3)
		public BigDecimal   Monto; 							//	DECIMAL(26,8)
		public Long 		IdCuentaBancariaCorredora; 		//	BIGINT
		public Long 		IdTarjetaBancariaCorredora; 	//	BIGINT
		public Long 		IdChequera; 					//	BIGINT
		public Long 		NumeroDocumentoCorredora; 		//	BIGINT
		public String		FechaDocumentoPagoCorredora;	//	NVARCHAR(40)
		public Long 		IdCuentaBancariaSocio; 			//	BIGINT
		public Long 		IdBancoSocio; 					//	BIGINT
		public String		NombreCuentaBancariaSocio;		//	NVARCHAR(100)
		public String		NumeroCuentaBancariaSocio;		//	NVARCHAR(100)
		public String		IdTipoCuentaBancariaSocio;		//	NVARCHAR(4)
		public String		NombreTipoCuentaBancariaSocio; 	//  NVARCHAR(100)
		public Long 		IdTarjetaBancariaSocio;			//	BIGINT
		public String		IdTipoTarjetaBancariaSocio;  	//	NVARCHAR(40)
		public String		NombreTipoTarjetaBancariaSocio; //	NVARCHAR(100)
		public String		NumeroTarjetaBancariaSocio;  	//	NVARCHAR(100)
		public String		NumeroSerieSocio;  				//	NVARCHAR(40)
		public Long 		NumeroDocumentoSocio;			//	BIGINT
		public String		FechaDocumentoPagoSocio;  		//	NVARCHAR(40)	
		
		public String toString(){
			return Utils.printJson(this, false);
		}

		public Object clone(){ Object obj=null; try{ obj=super.clone(); }catch(CloneNotSupportedException ex){ System.out.println(" no se puede duplicar"); } return obj; }
		
	}

	
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT	
	//-------------------------------------------------------------------------------------------
	
	public static class CrearIngresoEgreso_Salida {

		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<IngresoEgresoGenerado_Salida>	P_OUT_INGRESOEGRESOGENERADO;
		public List<ResultadoProceso_Salida>        P_OUT_RESULTADOPROCESO;
		
		public CrearIngresoEgreso_Salida()
		{	
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_INGRESOEGRESOGENERADO = new ArrayList<IngresoEgresoGenerado_Salida>();
			this.P_OUT_RESULTADOPROCESO		 = new ArrayList<ResultadoProceso_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	
	public static class IngresoEgresoGenerado_Salida 
	{
		public Long 	NumeroReferencia;			// BIGINT
		public Long 	IdIngresoEgreso; 			// BIGINT
		public String 	IdTipoMovimiento;			// NVARCHAR(6)
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ResultadoProceso_Salida extends BcsPojo
	{
		public String 	Campo;        // VARCHAR(40)   -- Ej: "IdIngresoEgreso"
		public Long   	Id;           // BIGINT        -- IdIngresoEgreso generado  Ej: 9999
		public String 	Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso"
		public String	Resultado;    // VARCHAR(40)   -- Ej: "ING"
		public Long NumeroReferencia;
		
		public ResultadoProceso_Salida (){
			this.NumeroReferencia = 0L;
		}
	}
	


	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS DE PROCESAMIENTO
	//-------------------------------------------------------------------------------------------
	
	public static class IngresoEgreso_Usuario {
		
		public Long IdPersona;
		public Long IdDireccionElectronica;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class IngresoEgreso_DireccionNotificacion {
		public Long IdPersona;
		public Long IdDireccionElectronicaPrincipal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IngresoEgreso_Proceso
	{
		public Long 	    		IdIngresoEgreso;
		public Long					IdEmpresa; 
		public String				IdModulo; 
		public Long					IdUsuario;
		public java.sql.Date        FechaMovimiento;
		public String				NombrePersona; 
		public String				IdTipoDocumentoIdentidad; 
		public String				DocumentoIdentidad;
		public String				NombrePersonaTitularPago;
		public String				IdTipoDocumentoIdentidadTitularPago; 
		public String				DocumentoIdentidadTitularPago;
		public String               EsIngresoAutomatico;            // S/N
		public String               NeteaComprobante;               // S/N   preferencias del portafolio
		public String               IdEstado;
		public String 				RequiereAutorizacion = "N";
		
		//Notificacion
		public String				Tipo;
		public String				Nombre;
		public String				NumeroTel;
		public String				NombreConcepto;
		public String				Corredora;
		public String				Correo;
		public String				DI;
		public String				TipoDoc;
		
		public Long      			NumeroReferencia;  				//	BIGINT
		public String    			IdTipoMovimiento;  		 		//	NVARCHAR(6)    -- MOVING, MOVEGR
		public String    			IdTipoPersona;  		 		//	NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
	   	public Long      			IdPersona;  		 			//	BIGINT
	   	public Long      			IdPortafolio;  		 			//	BIGINT 
	   	public String    			IdMoneda;  		 				//	NVARCHAR(3)
	   	public BigDecimal 			Monto;  		 				//	DECIMAL(26,8)
	   	public java.sql.Date    	FechaLiquidacion;  		                                                                       
	   	public String    			IdConcepto;  		 			//	NVARCHAR(6)
	   	public String    			IdTipoPersonaTitularPago;  		//	NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
	   	public Long      			IdPersonaTitularPago;  		 	//	BIGINT
	   	public Long      			IdPortafolioTitularPago;  		//	BIGINT
	   	public Long      			IdCaja;  		 				//	BIGINT
	   	public String    			SecuenciaAgrupacion;  			//	NVARCHAR(40)  -- Aquellos mov con la misma secuencia no son mezclados en distintos comprobante
	   	public Long      			SecuenciaRelacionada;  		 	//	BIGINT		  -- Permite relacionar distintos mov y generar una orden de liquidación entre ellos
	   	public String    			Glosa;  		 				//	NVARCHAR(260)
										   	
	   	//Parámetros Opcionales(Referenciales)
	   	public Long      			IdEmpleadoDigitador;  		 	//	BIGINT
	   	public Long      			IdSucursalEmpleadoDigitador;  	//	BIGINT
	   	public String   			IdTipoProducto;  		 		//	NVARCHAR(4)
	   	public String    			IdTipoOperacion;  		 		//	NVARCHAR(6)
		public String				IdTipoComprobanteOrigen; 		//	NVARCHAR(3)
		public Long					IdComprobanteOrigen; 			//	BIGINT
		public java.sql.Timestamp	FechaComprobanteOrigen;			//	NVARCHAR(40)
		public java.sql.Timestamp	FechaLiquidacionComprobanteOrigen;//NVARCHAR(40)
	   	public Long     			IdOperacion;  		 			//	BIGINT			
	    public java.sql.Timestamp   FechaOperacion;  		 		//	NVARCHAR(40)
	    public String   			IdCondicionLiquidacion;  		//	NVARCHAR(2)
	    public String    			IdBolsa;  		 				//	NVARCHAR(3)
	    public String    			IdMonedaOperacion;  		 	//	NVARCHAR(3)
	    public BigDecimal 			MontoOperacion;   				//	DECIMAL(26,8)
	    public String    			IdTipoReajuste;  		 		//	NVARCHAR(3)
	    public java.sql.Timestamp   FechaParidad;  		 			//	NVARCHAR(40)
	    public BigDecimal 			ValorParidad;   				// 	DECIMAL(26,8)
	    public Long     			FolioCartera;  		 			//	BIGINT
	    public Long      			IdInstrumento;  		 		//	BIGINT
	    public String    			IdTipoInstrumento;  		 	//	NVARCHAR(3)
	    public String    			IdClasificacionTipoInstrumento; //	NVARCHAR(3)
										  
	    //Indicadores
	    public String    			EsBilateral;  		 			//	NVARCHAR(1)  -- Liquidación bilateral entre corredores, no pasa por el CCLV
	    public String    			GeneraComprobante;  			//	NVARCHAR(1)  -- S/N: genera comprobante automático
	    public String    			LiquidarComprobante; 			//	NVARCHAR(1)  -- S/N: liquida comprobante automático
	    public String    			AgrupaComprobante;  			//	NVARCHAR(1)  -- S/N: permite agrupar varios mov. en un comprobante
	    public String    			MueveCuentaInversion;  			//	NVARCHAR(1)  -- S/N: mueve cuenta inversion // *** No se usa el campo, se debe borrar ? ***
	    public String    			TitularPago;  		 			//	NVARCHAR(1)  -- S/N: Socio de negocio que genera el movimiento es quién cobra o paga
	    public String				EsTrapaso = "N";						//  NVARCHAR(1)  -- S/N: Si es traspaso y se esta generando el ingreso y egreso
	    
		//Para validaciones
		public long 				errores 		= 0;
		public long 				advertencias 	= 0;
		public long 				autorizaciones 	= 0;
		public long 				ok 				= 0;
		public String 				IdTipoMovimiento_Real;
		public String 				IdConcepto_Real;
		public String 				IdMoneda_Real;
		public String 				IdEstadoMoneda_Real;
		public Long   				IdCaja_Real;
		public String 				IdMonedaTesoreria_Real;
		public Long  				IdPersona_Real;
	    public String 				IdEstado_PersonaReal;
	    public Long  				IdPersonaRelacionada_Real;
	    public String 				IdEstado_RelacionadoReal;	    
	    public String 				IdCondicionLiquidacion_Real;	
	    public String 				IdTipoProducto_Real;
	    public String 				IdTipoOperacion_Real;
	    public Long 				CountSec;
	    	
	    
	    public String 				BloqueadoPorCumplimiento = "N";
	    public String				EstadoPortafolio;
	    public String				CodigoPortafolio;
	    public String				EstadoPortafolioTitularPago;
	    public String				CodigoPortafolioTitularPago;
	    
	    //para saldo caja
		public BigDecimal			TotalMontoPortafolio = BigDecimal.ZERO;
		public BigDecimal 			SaldoCajaPortafolio = BigDecimal.ZERO;
		
		// Canal de origen
		public String				IdTipoCanalIngreso = "OPT"; 
	    
		//Uso exclusivo Aporte CSC
		public String				FolioCSC = "";
		public String				GeneraComprobanteAutomaticamente = "N";
		
		public String				CodigoEjecutivoDigitador = "";
	    
		public String toString() {
			return Utils.printJson(this, false);
		}	    
	}

	
	public static class FormaDePago_Proceso
	{	
		public Long 				NumeroReferencia;					//  BIGINT        Id Referencial (para unir el movimiento con su forma de pago)
		public Long 				NumeroReferenciaFormaDePago;        //  BIGINT        Id Referencia para FP (1,2,3,..) debe ser único por FP aunque sean de distintos movimientos
		public String				IdMedioDePago;          			//	NVARCHAR(3)   Ej.: CHE, EFE, TRA, VVI, CIN
		public String				IdModalidadDePago;      			//	NVARCHAR(6)   Ej.: MODCAJ: Por Caja   MODDEP: Depósito
		public String				NombreModalidadDePago;         		//	NVARCHAR(100)		
		public String				NombreMedioDePago;					//	NVARCHAR(100)
		public String				IdMoneda;               			//	NVARCHAR(3)
		public BigDecimal   		Monto; 								//	DECIMAL(26,8)
		public Long 				IdCuentaBancariaCorredora; 			//	BIGINT
		public Long 				IdTarjetaBancariaCorredora; 		//	BIGINT
		public Long 				IdChequera; 						//	BIGINT
		public Long 				NumeroDocumentoCorredora; 			//	BIGINT
		public java.sql.Timestamp	FechaDocumentoPagoCorredora;		//	SECONDATE(19)
		public Long 				IdCuentaBancariaSocio; 				//	BIGINT
		public Long 				IdBancoSocio; 						//	BIGINT
		public String				NombreCuentaBancariaSocio;			//	NVARCHAR(100)
		public String				NumeroCuentaBancariaSocio;			//	NVARCHAR(100)
		public String				IdTipoCuentaBancariaSocio;			//	NVARCHAR(4)
		public String				NombreTipoCuentaBancariaSocio; 		//  NVARCHAR(100)
		public Long 				IdTarjetaBancariaSocio;				//	BIGINT,
		public String				IdTipoTarjetaBancariaSocio;  		//	NVARCHAR(40)
		public String				NombreTipoTarjetaBancariaSocio; 	//	NVARCHAR(100)
		public String				NumeroTarjetaBancariaSocio;  		//	NVARCHAR(100)
		public String				NumeroSerieSocio;  					//	NVARCHAR(40)
		public Long 				NumeroDocumentoSocio;				//	BIGINT
		public java.sql.Timestamp	FechaDocumentoPagoSocio;  			//	SECONDATE(19)
		public Long 	    		IdIngresoEgresoFormaDePago;			//	BIGINT
		public Long 	    		IdIngresoEgreso;					//	BIGINT
		public String				IdTipoMovimiento;					//	NVARCHAR(6)
		public String				IdTipoCuentaBancariaCorredora;   	//	NVARCHAR(4)
		public String				NombreTipoCuentaBancariaCorredora;	//	NVARCHAR(100)
		public String				NombreCuentaBancariaCorredora;		//	NVARCHAR(100)
		public String               NumeroCuentaBancariaCorredora;    	//	NVARCHAR(100)
		public String               IdTipoTarjetaBancariaCorredora;  	//	NVARCHAR(40)
		public String				NombreTipoTarjetaBancariaCorredora;	//	NVARCHAR(100)
		public String				NumeroTarjetaBancariaCorredora;		//	NVARCHAR(100)
		public java.sql.Timestamp  	FechaVctoTarjetaBancariaSocio; 		//  SECONDATE(19)
		public String				IdEstado;							//	NVARCHAR(3)
		
		//Para validaciones
		public long 				errores 		= 0;
		public long 				advertencias 	= 0;
		public long 				autorizaciones 	= 0;
		public long 				ok 				= 0;		
		public String				IdMedioDePago_Real;
		public String				IdModalidadDePago_Real;
		public String				IdMoneda_Real;
		public String				IdEstadoMoneda_Real;
		public Long   				IdCuentaBancariaCorredora_Real;
		public Long					IdCuentaBancariaSocio_Real;
		public Long					IdTarjetaCorredoraBancaria_Real;
		public Long					IdTarjetaSocioBancaria_Real;
		public Integer 				ExisteDescuadreEnMontos;
		public Integer 				ExisteDiferenciaMoneda;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}	    		
	}	
	
	
	//Preferencias de negocio de tesorería, por portafolio
	public static class PreferenciaTesoreria 
	{	
		public Long   			IdPortafolio;
		public Long   			IdCliente;
		public String   		IdTipoCliente;
		public String   		TES_NIE;   //Indicador que Netea Ingresos y Egresos
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	//Datos de entrada de movimientos de acuerdo a los indices de entrada
	public static class InformacionIngresoEgreso	
	{
		public Long   			NumeroReferencia;
		public String 			IdTipoMovimiento_Real;
		public String 			IdConcepto_Real;
		public String 			IdMoneda_Real;
		public String 			IdEstadoMoneda_Real;
		public Long   			IdCaja_Real;
		public String 			IdMonedaTesoreria_Real;
		public String 			IdCondicionLiquidacion_Real;	
		public String 			IdTipoProducto_Real;
		public String 			IdTipoOperacion_Real;		
		public Long 			CountSec;
		
		public String			Tipo;
		public String			Nombre;
		public String			NumeroTel;
		public String			NombreConcepto;
		public String			Corredora;
		public String			Correo;
		public String			DI;
		public String			TipoDoc;

		
		public String			BloqueadoPorCumplimiento = "N";
		public String			GeneraComprobanteAutomaticamente = "N";
		public String			EstadoPortafolio;
		public String			CodigoPortafolio;
		public String			EstadoPortafolioTitularPago;
	    public String			CodigoPortafolioTitularPago;

		//Datos de la persona
		public String			NombrePersona; 
		public String			IdTipoDocumentoIdentidad; 
		public String			DocumentoIdentidad;
		public Long  			IdPersona_Real;
	    public String 			IdEstado_PersonaReal;
	    
	    
		//Datos del titular pago
		public String			NombrePersonaTitularPago;
		public String			IdTipoDocumentoIdentidadTitularPago; 
		public String			DocumentoIdentidadTitularPago;
	    public Long  			IdPersonaRelacionada_Real;
	    public String 			IdEstado_RelacionadoReal;
	}
	

	//Datos de entrada de formas de pago de acuerdo a los indices de entrada
	public static class InformacionFormaDePago	
	{
		public Long   			NumeroReferencia;
		public Long   			NumeroReferenciaFormaDePago;
		public String			IdMedioDePago_Real;
		public String			IdModalidadDePago_Real;
		public String			IdMoneda_Real;
		public String			IdEstadoMoneda_Real;
		public Long   			IdCuentaBancariaCorredora_Real;
		public Long				IdCuentaBancariaSocio_Real;
		public Long				IdTarjetaCorredoraBancaria_Real;
		public Long				IdTarjetaSocioBancaria_Real;
		public String			NombreMedioDePago;
		public String			NombreModalidadDePago;
		public String			IdTipoCuentaBancariaCorredora;
		public String			NombreTipoCuentaBancariaCorredora;
		public String			NombreCuentaBancariaCorredora;
		public String			NumeroCuentaBancariaCorredora;
		public String			IdTipoCuentaBancariaSocio;
		public String			NombreTipoCuentaBancariaSocio;
		public String			NumeroCuentaBancariaSocio;
		public Long				IdBancoSocio;
		public Integer			ExisteDescuadreEnMontos;
		public Integer			ExisteDiferenciaMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}

  
	public static class FlujoCajaElem {
		String IdMoneda;
		String Fecha;
		Long IdPortafolio;
		
		public FlujoCajaElem(String moneda,String fecha,Long idPortafolio) {
			this.IdMoneda = moneda;
			this.Fecha = fecha;
			this.IdPortafolio = idPortafolio;
		}
		
		@Override
		public boolean equals(Object o) {
			FlujoCajaElem e = (FlujoCajaElem)o;
			return this.IdMoneda.equalsIgnoreCase(e.IdMoneda) && this.Fecha.equalsIgnoreCase(e.Fecha) && this.IdPortafolio.equals(e.IdPortafolio);
		}
		
		@Override
		public int hashCode() {
			return (this.IdMoneda + this.Fecha + this.IdPortafolio).hashCode();
		}
		
	}
	
	
	public static class InformacionCorredora extends BcsPojo{
	    public String 	Nombre;
	    public Long 	IdPersona;
	    public String	DocumentoIdentidad;
	}
	
	public static class ObjInstrumento extends BcsPojo{
		public Long 	IdInstrumento;
		public String 	Nemotecnico;
	}
	
	public static class Empleado extends BcsPojo{
		public Long 	IdEmpleadoDigitador;
		public String 	CodigoEmpleado;
	}
	
	//-------------------------------------------------------------------------------------------
	// METODOS UTILITARIOS PARA PROCESAMIENTO EN LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------	
	 
	//Procesa salida de validaciones
	private void ProcesaRequisitoIngresoEgreso(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, IngresoEgreso_Proceso iep, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("NumeroReferencia", iep.NumeroReferencia)
					.add("IdConcepto", iep.IdConcepto)
					.add("Monto", iep.Monto)
					.add("IdPersona", iep.IdPersona)
					.add("IdPersonaTitularPago", iep.IdPersonaTitularPago)
					.add("NombrePersona", iep.NombrePersona)
					.add("NombrePersonaTitularPago", iep.NombrePersonaTitularPago)
					.add("IdSecuencia", iep.SecuenciaAgrupacion)
					.add("IdMoneda", iep.IdMoneda)
					.add("IdTipoProducto", iep.IdTipoProducto)
					.add("IdTipoOperacion", iep.IdTipoOperacion)
					.add("IdCondicionDeLiquidacion", iep.IdCondicionLiquidacion)
					
					
					.add("NombreCliente", iep.NombrePersona)
					.add("DocumentoIdentidad", iep.DocumentoIdentidad)
					.add("CodigoPortafolio", iep.CodigoPortafolio)
					.add("DocumentoIdentidadTitularPago", iep.DocumentoIdentidadTitularPago)
					.add("CodigoPortafolioTitularPago", iep.CodigoPortafolioTitularPago)
					.add("CodigoEjecutivoDigitador", iep.CodigoEjecutivoDigitador)

				    .msg();			
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(iep.NumeroReferencia,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				iep.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(iep.NumeroReferencia,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				iep.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(iep.NumeroReferencia,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				iep.advertencias++;
			}
			else 
			{
				iep.ok++;
			}
		}
		else 
		{
			iep.ok++;
		}		
	}
	
	
	
	private void ProcesaRequisitoFormaDePago(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, FormaDePago_Proceso fpp, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
			.add("IdMedioDePago", fpp.IdMedioDePago)
			.add("IdModalidadDePago", fpp.IdModalidadDePago)
			.add("IdMoneda", fpp.IdMoneda)
			.add("IdEstadoMoneda", fpp.IdEstadoMoneda_Real)
			.add("IdCuentaBanco", fpp.IdCuentaBancariaCorredora)
			.add("IdTarjetaBancaria", fpp.IdTarjetaBancariaCorredora)
			.add("IdCuentaBancaria", fpp.IdCuentaBancariaSocio)
			.add("IdTarjetaBancaria", fpp.IdTarjetaBancariaSocio)
		    .msg();  
					  
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(fpp.NumeroReferenciaFormaDePago,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				fpp.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(fpp.NumeroReferenciaFormaDePago,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				fpp.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(fpp.NumeroReferenciaFormaDePago,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				fpp.advertencias++;
			}
			else 
			{
				fpp.ok++;
			}
		}
		else 
		{
			fpp.ok++;
		}		
	}	


	

	//Quita del proceso los movimientos que no son validos(y las formas de pago asociadas)
	public void limpiarIngresosEgresos(List<IngresoEgreso_Proceso> ingresoEgresoProceso, List<FormaDePago_Proceso> formaDePago_Proceso, List<IngresoEgreso_Proceso> ingresoEgresoConErrores) {
		List<FormaDePago_Proceso> formasDePagoEliminadas = new ArrayList<FormaDePago_Proceso>();
		for (IngresoEgreso_Proceso iep : ingresoEgresoProceso) {
			if (iep.errores > 0) {
				ingresoEgresoConErrores.add(iep);
			}
		}
		
		for (IngresoEgreso_Proceso iep : ingresoEgresoConErrores) {
			//Remueve el movimiento
			if (ingresoEgresoProceso.contains(iep)) {
				ingresoEgresoProceso.remove(iep);
			}
				

			//Remueve las formas de pago asociadas al movimiento
			for (FormaDePago_Proceso fpp : formaDePago_Proceso) {	
				if (iep.NumeroReferencia.equals(fpp.NumeroReferencia)) {
					if (formaDePago_Proceso.contains(fpp)) {
						formasDePagoEliminadas.add(fpp);
					}
						
				}
			}
			
		}
		
		for (FormaDePago_Proceso fpce : formasDePagoEliminadas) {
			if(formaDePago_Proceso.contains(fpce)) {
				formaDePago_Proceso.remove(fpce);
			}
		}
	}
	
	
	//Quita del proceso las formas de pago que tuvieron problemas en la validación(y los movimientos asociados)
	public void limpiarFormasDePago(List<FormaDePago_Proceso> formaDePagoProceso, List<IngresoEgreso_Proceso> ingresoEgresoProceso ,List<FormaDePago_Proceso> formaDePagoConErrores) throws Exception 
	{
		for (FormaDePago_Proceso fpp : formaDePagoProceso) 
		{
			if (fpp.errores > 0) {
				formaDePagoConErrores.add(fpp);
			}
		}
		
		HashMap<Long,Object> IngresoEgresoProceso = Utils.List2Hash(ingresoEgresoProceso, "NumeroReferencia");   // Transforma la lista ingresoEgresoProceso en un hash
		
		for (FormaDePago_Proceso fpp : formaDePagoConErrores)
		{
			//Remueve la forma de pago con error
			if (formaDePagoProceso.contains(fpp))
				formaDePagoProceso.remove(fpp);
			
			//Remueve el movimiento asociado a la forma de pago con error
			IngresoEgreso_Proceso ingresoEgresoProcesoConError = (IngresoEgreso_Proceso) IngresoEgresoProceso.get(fpp.NumeroReferencia);
			if (ingresoEgresoProceso.contains(ingresoEgresoProcesoConError))
				ingresoEgresoProceso.remove(ingresoEgresoProcesoConError);
			
		}
	}	

	public CrearIngresoEgreso_Salida LimpiarSalida(CrearIngresoEgreso_Salida out ) {
		
		if (out.P_OUT_ERRORESCONTADOR > 0){
			for ( int i = 0; i < out.P_OUT_MENSAJES.size() && i >= 0  ; i++ ){ //MensajeSalida mensaje : out.P_OUT_MENSAJES
				if (out.P_OUT_MENSAJES.get(i).tipo == "OK" ){
					out.P_OUT_MENSAJES.remove( i ) ;
					i--;
				}
			}
		}
		
		if (out.P_OUT_MENSAJES.size() > 0) {
			out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream()		
												    .collect(Collectors.toMap(
												        mensaje -> mensaje.descripcion,
												        mensaje -> mensaje,
												        (mensaje1, mensaje2) -> mensaje1
												    ))
												    .values()
												    .stream()
												    .collect(Collectors.toCollection(ArrayList::new));
		}
		
		return out;

	}

	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		CrearIngresoEgreso_Entrada in = gson.fromJson(jin.toString(), CrearIngresoEgreso_Entrada.class);
		CrearIngresoEgreso_Salida out = logica(con, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------
	//@Trace
	public CrearIngresoEgreso_Salida logica(Connection con, CrearIngresoEgreso_Entrada in) throws Exception {
		SQL _sql = new SQL();
		CrearIngresoEgreso_Salida out = new CrearIngresoEgreso_Salida();
		logger.info("== INICIO CREAR INGRESO/EGRESO ==");

		try {
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			String pValidaFlujoCajaEgresoNegocio = "N";
			try {
				if (!"PAC".equalsIgnoreCase(in.p_IdOrigenProceso)) {
					pValidaFlujoCajaEgresoNegocio = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "TESORERIA", "VALIDAFLUJOCAJAEGRESONEGOCIO");
				}
			} catch(Exception er) {
				pValidaFlujoCajaEgresoNegocio  = "N";
			}
			
			String pValidaFlujoCajaEgresoManual  = "N";
			try {
				if (!"PAC".equalsIgnoreCase(in.p_IdOrigenProceso)) {
					pValidaFlujoCajaEgresoManual = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "TESORERIA", "VALIDAFLUJOCAJAEGRESOMANUAL");
				}
			} catch(Exception er) {
				pValidaFlujoCajaEgresoManual  = "N";
			}

			if (in.p_IngresoEgresoGenerar.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_DE_INGRESO_EGRESO",new HashMap<String,String>(0),con,in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return LimpiarSalida(out);
			}
			
			if (in.p_FormaDePago.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"NO_SE_INGRESARON_FORMAS_DE_PAGO",new HashMap<String,String>(0),con,in.p_IdEmpresa));
			}
			
			//---------------------------------------------------------------------------------------------
			//VALIDA SI CUMPLE REQUISITOS BASICOS
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				logger.debug("NO CUMPLE REQUISITOS BASICOS");
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return LimpiarSalida(out);
			}

			String valorParametro = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "TESORERIA", "SEPARACOMPROBANTECOMSPT");
			if(valorParametro.equals("S")) {
				for (IngresoEgreso_Entrada ingresoEgresoEntrada : in.p_IngresoEgresoGenerar) {
					if(ingresoEgresoEntrada.IdTipoCanalIngreso.equals("FMM") && ingresoEgresoEntrada.IdTipoOperacion.equals("COMSPT")) {
						ingresoEgresoEntrada.AgruparEnComprobante = "N";
					}
				}
			}
			
			ArrayList<IngresoEgreso_Proceso> ingresoEgresoConErrores = new ArrayList<>();
			ArrayList<FormaDePago_Proceso> formaDePagoConErrores = new ArrayList<>();
			
			List<IngresoEgreso_Proceso> IngresoEgreso_Proceso  = new ArrayList<IngresoEgreso_Proceso>();
			
			//---------------------------------------------------------------------------------------------
			// OBTIENE DATOS DE ENTRADA DE INGRESOS/EGRESO DE ACUERDO A INDICES(para validaciones e insert)
			_sql.batch(con, SQL_INSERT_IngresoEgreso_Proceso, in.p_IngresoEgresoGenerar, IngresoEgreso_Entrada.class); //Inserta indices de entrada en tabla procesos
			
			HashMap<String,Object> paramsInfoIngresoEgreso = new HashMap<>();
			paramsInfoIngresoEgreso.put("IdEmpresa", in.p_IdEmpresa);
			logger.debug("INICIO::SQL_SELECT_IngresoEgreso_Proceso");
			List<InformacionIngresoEgreso> _infoIngresoEgreso = _sql.query(con, SQL_SELECT_IngresoEgreso_Proceso, paramsInfoIngresoEgreso ,InformacionIngresoEgreso.class); //Obtiene datos de entrada de acuerdo a índices de entrada  
			logger.debug("FIN::SQL_SELECT_IngresoEgreso_Proceso");
			HashMap<Long,Object> InformacionIngresoEgreso = Utils.List2Hash(_infoIngresoEgreso, "NumeroReferencia");   // Transforma la lista _infoIngresoEgreso en un hash    
			
			//---------------------------------------------------------------------------------------------
			// INCORPORAR DATOS DE MOVIMIENTOS(ENTRADA) A DATOS PROCESO
			HashMap<String,Object> parmsVacio = new HashMap<String,Object>();
			logger.debug("INICIO::SQL_SELECT_Corredora");
			List<InformacionCorredora> resultCosultaCorredora = _sql.query(con, SQL_SELECT_Corredora, parmsVacio, InformacionCorredora.class);
			logger.debug("FIN::SQL_SELECT_Corredora");
			
			//---------------------------------------------------------------------------------------------
			// OBTIENE EL ID DEL EMPLEADO USUARIO
			SocioNegocioUtils.Empleado empleadoUsuario = SocioNegocioUtils.obtenerEmpleadoUsuario(con, in.p_IdEmpresa, in.p_IdUsuario).orElse(null);
			
			//---------------------------------------------------------------------------------------------
			// OBTIENE DATOS DE LOS EMPLEADOS POR CODIGO EJECUTIVO DIGITADOR
			HashMap<String,Object> paramsCodigoEmpleado = new HashMap<>();
			ArrayList<String> codigoEjecutivoDigitador = new ArrayList<>();
			for (IngresoEgreso_Entrada ingresoEgresoProceso: in.p_IngresoEgresoGenerar) {    
				codigoEjecutivoDigitador.add(ingresoEgresoProceso.CodigoEjecutivoDigitador);
			}
			paramsCodigoEmpleado.put("CodigoEjecutivoDigitador", codigoEjecutivoDigitador);
			logger.debug("INICIO::SQL_SELECT_EMPLEADO_DIGITADOR");
			List<Empleado> _infoEmpleado = _sql.query(con, SQL_SELECT_EMPLEADO_DIGITADOR, paramsCodigoEmpleado, Empleado.class);
			logger.debug("FIN::SQL_SELECT_EMPLEADO_DIGITADOR");
			HashMap<String,Object> InformacionEmpleado = Utils.List2Hash(_infoEmpleado, "CodigoEmpleado");

			for (IngresoEgreso_Entrada ingresoEgresoEntrada : in.p_IngresoEgresoGenerar) {
				InformacionIngresoEgreso infoIngresoEgreso = (InformacionIngresoEgreso) InformacionIngresoEgreso.get(ingresoEgresoEntrada.NumeroReferencia);
				IngresoEgreso_Proceso ingresoEgresoProceso = new IngresoEgreso_Proceso();
				Utils.copyPojo(ingresoEgresoEntrada, ingresoEgresoProceso);
				
				ingresoEgresoProceso.IdEmpresa = in.p_IdEmpresa; 
				ingresoEgresoProceso.IdModulo = in.p_IdModulo; 
				ingresoEgresoProceso.IdUsuario = in.p_IdUsuario;
				ingresoEgresoProceso.EsIngresoAutomatico = in.p_EsIngresoAutomatico;
				ingresoEgresoProceso.RequiereAutorizacion = ingresoEgresoEntrada.RequiereAutorizacion;
				
				//Datos de la persona
				ingresoEgresoProceso.NombrePersona = infoIngresoEgreso.NombrePersona; 
				ingresoEgresoProceso.IdTipoDocumentoIdentidad = infoIngresoEgreso.IdTipoDocumentoIdentidad; 
				ingresoEgresoProceso.DocumentoIdentidad = infoIngresoEgreso.DocumentoIdentidad;
			
				//Datos del titular pago
				ingresoEgresoProceso.NombrePersonaTitularPago = infoIngresoEgreso.NombrePersonaTitularPago;
				ingresoEgresoProceso.IdTipoDocumentoIdentidadTitularPago = infoIngresoEgreso.IdTipoDocumentoIdentidadTitularPago; 
				ingresoEgresoProceso.DocumentoIdentidadTitularPago = infoIngresoEgreso.DocumentoIdentidadTitularPago;
				
				if (ingresoEgresoEntrada.FechaLiquidacion == null || ingresoEgresoEntrada.FechaLiquidacion.isEmpty() || ingresoEgresoEntrada.FechaLiquidacion.equalsIgnoreCase("0001-01-01"))
					ingresoEgresoProceso.FechaLiquidacion = Fecha.toDate(fechaSistema); // Si es null, le asigna la fecha de sistema
				else
					ingresoEgresoProceso.FechaLiquidacion = Fecha.toDate(ingresoEgresoEntrada.FechaLiquidacion);
				
				
				if (ingresoEgresoEntrada.SecuenciaRelacionada == null)
					ingresoEgresoProceso.SecuenciaRelacionada = 0L;
				else
					ingresoEgresoProceso.SecuenciaRelacionada = ingresoEgresoEntrada.SecuenciaRelacionada;
				
				ingresoEgresoProceso.IdTipoComprobanteOrigen = ingresoEgresoEntrada.IdTipoComprobante; 
				ingresoEgresoProceso.IdComprobanteOrigen = ingresoEgresoEntrada.IdComprobante;   

				if (ingresoEgresoEntrada.FechaComprobante == null || ingresoEgresoEntrada.FechaComprobante.isEmpty() || ingresoEgresoEntrada.FechaComprobante.equalsIgnoreCase("0001-01-01")) {
					ingresoEgresoProceso.FechaComprobanteOrigen = Fecha.toDateTime("0001-01-01 00:00:00");
				} else {
					if (ingresoEgresoEntrada.FechaComprobante.length() == 10) {
						ingresoEgresoProceso.FechaComprobanteOrigen = Fecha.toDateTime(ingresoEgresoEntrada.FechaComprobante + " 00:00:00");
					} else {
						ingresoEgresoProceso.FechaComprobanteOrigen = Fecha.toDateTime(ingresoEgresoEntrada.FechaComprobante);	
					}
					
				}
				
				if (ingresoEgresoEntrada.IdTipoComprobante == "CSC") {
					ingresoEgresoProceso.FolioCSC = ingresoEgresoEntrada.FolioCSC;
				} else {
					ingresoEgresoProceso.FolioCSC = "";
				}
				
				if (ingresoEgresoEntrada.FechaLiquidacionComprobante == null || ingresoEgresoEntrada.FechaLiquidacionComprobante.isEmpty() || ingresoEgresoEntrada.FechaLiquidacionComprobante.equalsIgnoreCase("0001-01-01")) {
					ingresoEgresoProceso.FechaLiquidacionComprobanteOrigen = Fecha.toDateTime("0001-01-01 00:00:00");
				} else {
					if (ingresoEgresoEntrada.FechaLiquidacionComprobante.length() == 10) {
						ingresoEgresoProceso.FechaLiquidacionComprobanteOrigen = Fecha.toDateTime(ingresoEgresoEntrada.FechaLiquidacionComprobante + " 00:00:00");
					} else {
						ingresoEgresoProceso.FechaLiquidacionComprobanteOrigen = Fecha.toDateTime(ingresoEgresoEntrada.FechaLiquidacionComprobante);	
					}
					
				}
				

				if (in.p_FechaMovimiento == null || in.p_FechaMovimiento.isEmpty() || in.p_FechaMovimiento.equalsIgnoreCase("0001-01-01"))
					ingresoEgresoProceso.FechaMovimiento = Fecha.toDate(fechaSistema);     //Si no llega FechaMovimiento como parámetro de entrada, se le asigna fecha de sistema
				else
					ingresoEgresoProceso.FechaMovimiento = Fecha.toDate(in.p_FechaMovimiento);
				
				ingresoEgresoProceso.IdOperacion = ingresoEgresoEntrada.IdOperacion;			

				if (ingresoEgresoEntrada.FechaOperacion == null || ingresoEgresoEntrada.FechaOperacion.isEmpty() || ingresoEgresoEntrada.FechaOperacion.equalsIgnoreCase("0001-01-01"))
					ingresoEgresoProceso.FechaOperacion = Fecha.toDateTime("0001-01-01 00:00:00");
				else{
					if (ingresoEgresoEntrada.FechaOperacion.length() == 10) {
						ingresoEgresoProceso.FechaOperacion = Fecha.toDateTime(ingresoEgresoEntrada.FechaOperacion + " 00:00:00");
					} else {
						ingresoEgresoProceso.FechaOperacion = Fecha.toDateTime(ingresoEgresoEntrada.FechaOperacion);	
					}
				}
				
				if (ingresoEgresoEntrada.FechaParidad == null || ingresoEgresoEntrada.FechaParidad.isEmpty() || ingresoEgresoEntrada.FechaParidad.equalsIgnoreCase("0001-01-01"))
					ingresoEgresoProceso.FechaParidad = Fecha.toDateTime("0001-01-01 00:00:00");
				else{
					if (ingresoEgresoEntrada.FechaParidad.length() == 10) {
						ingresoEgresoProceso.FechaParidad = Fecha.toDateTime(ingresoEgresoEntrada.FechaParidad + " 00:00:00");
					} else {
						ingresoEgresoProceso.FechaParidad = Fecha.toDateTime(ingresoEgresoEntrada.FechaParidad);	
					}
				}
				
				ingresoEgresoProceso.NeteaComprobante = "N";    //Se repasa más abajo según las preferencias de negocio del portafolio 

			    //Indicadores
				ingresoEgresoProceso.EsBilateral = ingresoEgresoEntrada.Bilateral; 
				ingresoEgresoProceso.GeneraComprobante = ingresoEgresoEntrada.GeneraComprobante;
				ingresoEgresoProceso.GeneraComprobanteAutomaticamente = infoIngresoEgreso.GeneraComprobanteAutomaticamente;
				ingresoEgresoProceso.AgrupaComprobante = ingresoEgresoEntrada.AgruparEnComprobante;
				ingresoEgresoProceso.MueveCuentaInversion = ingresoEgresoEntrada.MueveCuentaInversion;   //campo obsoleto en BD
				
				for (InformacionCorredora  Informacion: resultCosultaCorredora){
					if(Informacion.DocumentoIdentidad.equalsIgnoreCase(ingresoEgresoProceso.DocumentoIdentidad)) {
						ingresoEgresoProceso.BloqueadoPorCumplimiento = "N";					
					} else {
						ingresoEgresoProceso.BloqueadoPorCumplimiento = infoIngresoEgreso.BloqueadoPorCumplimiento;					
					}
				}
				
				//Datos para validaciones
				ingresoEgresoProceso.IdTipoMovimiento_Real = infoIngresoEgreso.IdTipoMovimiento_Real;
				ingresoEgresoProceso.Tipo = infoIngresoEgreso.Tipo;
				ingresoEgresoProceso.Nombre = infoIngresoEgreso.Nombre;
				ingresoEgresoProceso.NumeroTel= infoIngresoEgreso.NumeroTel;
				ingresoEgresoProceso.DI= infoIngresoEgreso.DI;
				ingresoEgresoProceso.TipoDoc= infoIngresoEgreso.TipoDoc;
				ingresoEgresoProceso.Correo= infoIngresoEgreso.Correo;
				ingresoEgresoProceso.Corredora= infoIngresoEgreso.Corredora;
				ingresoEgresoProceso.NombreConcepto= infoIngresoEgreso.NombreConcepto;
				ingresoEgresoProceso.IdConcepto_Real = infoIngresoEgreso.IdConcepto_Real;
				ingresoEgresoProceso.IdMoneda_Real = infoIngresoEgreso.IdMoneda_Real;
				ingresoEgresoProceso.IdEstadoMoneda_Real = infoIngresoEgreso.IdEstadoMoneda_Real;
				ingresoEgresoProceso.IdCaja_Real = infoIngresoEgreso.IdCaja_Real;
				ingresoEgresoProceso.IdMonedaTesoreria_Real = infoIngresoEgreso.IdMonedaTesoreria_Real;
				ingresoEgresoProceso.IdPersona_Real = infoIngresoEgreso.IdPersona_Real;
				ingresoEgresoProceso.IdEstado_PersonaReal = infoIngresoEgreso.IdEstado_PersonaReal;
				ingresoEgresoProceso.IdPersonaRelacionada_Real = infoIngresoEgreso.IdPersonaRelacionada_Real;
				ingresoEgresoProceso.IdEstado_RelacionadoReal = infoIngresoEgreso.IdEstado_RelacionadoReal;
				ingresoEgresoProceso.IdCondicionLiquidacion_Real = infoIngresoEgreso.IdCondicionLiquidacion_Real;	
				ingresoEgresoProceso.IdTipoProducto_Real = infoIngresoEgreso.IdTipoProducto_Real;
				ingresoEgresoProceso.IdTipoOperacion_Real = infoIngresoEgreso.IdTipoOperacion_Real;		
				ingresoEgresoProceso.CountSec = infoIngresoEgreso.CountSec;
				ingresoEgresoProceso.IdTipoCanalIngreso = ingresoEgresoEntrada.IdTipoCanalIngreso;
				ingresoEgresoProceso.EstadoPortafolio = infoIngresoEgreso.EstadoPortafolio;
				ingresoEgresoProceso.CodigoPortafolio = infoIngresoEgreso.CodigoPortafolio;
				ingresoEgresoProceso.EstadoPortafolioTitularPago = infoIngresoEgreso.EstadoPortafolioTitularPago;
				ingresoEgresoProceso.CodigoPortafolioTitularPago = infoIngresoEgreso.CodigoPortafolioTitularPago;
				
				if("".equalsIgnoreCase(ingresoEgresoEntrada.CodigoEjecutivoDigitador)) {
					ingresoEgresoProceso.IdEmpleadoDigitador = empleadoUsuario.IdEmpleado;
				} else {
					Empleado emp = (Empleado) InformacionEmpleado.get(ingresoEgresoEntrada.CodigoEjecutivoDigitador);
					ingresoEgresoProceso.IdEmpleadoDigitador = emp != null ? emp.IdEmpleadoDigitador : 0L;
				}
								
				IngresoEgreso_Proceso.add(ingresoEgresoProceso);
			}
			//---------------------------------------------------------------------------------------------
			//CONSULTAR Y ACTUALIZA PREFERENCIAS DE NEGOCIO TESORERIA
			
			//Consultar preferencias de negocio de tesorería
			logger.debug("INICIO CONSULTAR Y ACTUALIZA PREFERENCIAS DE NEGOCIO TESORERIA");
			HashMap<Long, PreferenciaTesoreria> preferenciaNegocioTesoreria = new HashMap<>();
			{	
				HashMap<String,Object> parametrosConsultaPNT = new HashMap<>();
				ArrayList<Long> idsPortafolio = new ArrayList<>();
				
				for (IngresoEgreso_Proceso ingresoEgresoProceso: IngresoEgreso_Proceso) {	
					idsPortafolio.add(ingresoEgresoProceso.IdPortafolioTitularPago);
				}
				parametrosConsultaPNT.put("idsPortafolio", idsPortafolio);
				
				logger.debug("INICIO::SQL_SELECT_PreferenciasNegocioTesoreria");
				List<PreferenciaTesoreria> resultCosultaPNT = _sql.query(con, SQL_SELECT_PreferenciasNegocioTesoreria, parametrosConsultaPNT, PreferenciaTesoreria.class);
				logger.debug("FIN::SQL_SELECT_PreferenciasNegocioTesoreria");
				HashMap<Object, Object>__preferenciaTesoreria = Utils.List2Hash(resultCosultaPNT, "IdPortafolio");
				for(Object key: __preferenciaTesoreria.keySet()) {
					preferenciaNegocioTesoreria.put((Long)key,(PreferenciaTesoreria) __preferenciaTesoreria.get(key));
				}
				
				if (preferenciaNegocioTesoreria.size() > 0) {
					for (IngresoEgreso_Proceso iep: IngresoEgreso_Proceso) {	
						if (preferenciaNegocioTesoreria.get(iep.IdPortafolioTitularPago) == null || preferenciaNegocioTesoreria.get(iep.IdPortafolioTitularPago).TES_NIE == null || preferenciaNegocioTesoreria.get(iep.IdPortafolioTitularPago).TES_NIE == "")
							iep.NeteaComprobante = "N";
						else
							iep.NeteaComprobante = preferenciaNegocioTesoreria.get(iep.IdPortafolioTitularPago).TES_NIE;
					}
					
					logger.debug("FIN CONSULTAR Y ACTUALIZA PREFERENCIAS DE NEGOCIO TESORERIA");
				} else {
					logger.debug("NO SE ENCONTRARON PREFERENCIAS DE NEGOCIO TESORERIA PARA LOS PORTAFOLIOS > " + idsPortafolio);
				}	
				
			}
			//---------------------------------------------------------------------------------------------
			// OBTIENE DATOS DE ENTRADA DE FORMAS DE PAGO DE ACUERDO A INDICES(para validaciones e insert)
			_sql.batch(con, SQL_INSERT_IngresoEgresoFormaDePago_Proceso, in.p_FormaDePago, FormaDePago_Entrada.class); //Inserta indices de entrada en tabla proceso
			
			HashMap<String,Object> paramsInfoFormaDePago = new HashMap<>();
			logger.debug("INICIO::SQL_SELECT_IngresoEgresoFormaDePago_Proceso");
			List<InformacionFormaDePago> _informacionFormaDePago = _sql.query(con, SQL_SELECT_IngresoEgresoFormaDePago_Proceso, paramsInfoFormaDePago ,InformacionFormaDePago.class); //Obtiene datos de entrada de acuerdo a índices de entrada
			logger.debug("FIN::SQL_SELECT_IngresoEgresoFormaDePago_Proceso");
			HashMap<Long,Object> InformacionFormaDePago = Utils.List2Hash(_informacionFormaDePago, "NumeroReferenciaFormaDePago");   // Transforma la lista _informacionFormaDePago en un hash
			
			//---------------------------------------------------------------------------------------------
			// INCORPORAR DATOS DE FORMA DE PAGO(ENTRADA) A DATOS PROCESO
			HashMap<Long,Object> _ingresoEgresoProceso = Utils.List2Hash(IngresoEgreso_Proceso, "NumeroReferencia");   //transforma la lista IngresoEgresoProceso en un hash				
			List<FormaDePago_Proceso> FormaDePago_Proceso  = new ArrayList<FormaDePago_Proceso>();
			logger.debug("SE PROCESAN FORMAS DE PAGO");
			
			//si existen forma de pago, las procesa
			if (in.p_FormaDePago.size() > 0) {
				for (FormaDePago_Entrada formaDePagoEntrada : in.p_FormaDePago) {	
					InformacionFormaDePago infoFormaDePago = (InformacionFormaDePago) InformacionFormaDePago.get(formaDePagoEntrada.NumeroReferenciaFormaDePago);
					
					//Copia los datos de entrada de la FP a objeto local					
					FormaDePago_Proceso formaDePagoProceso = new FormaDePago_Proceso();
					Utils.copyPojo(formaDePagoEntrada, formaDePagoProceso);  
					
					//Obtiene datos del movimiento de IE(encabezado) y los asigna al objeto local
					IngresoEgreso_Proceso ingresoEgresoProceso = (IngresoEgreso_Proceso) _ingresoEgresoProceso.get(formaDePagoEntrada.NumeroReferencia);					

					formaDePagoProceso.IdTipoMovimiento = ingresoEgresoProceso.IdTipoMovimiento;

					formaDePagoProceso.Monto = formaDePagoEntrada.Monto;
					
					formaDePagoProceso.FechaVctoTarjetaBancariaSocio = Fecha.toDateTime("0001-01-01 00:00:00"); //No se está registrando por ahora
					formaDePagoProceso.NombreMedioDePago     = infoFormaDePago.NombreMedioDePago;
					formaDePagoProceso.NombreModalidadDePago = infoFormaDePago.NombreModalidadDePago;
					formaDePagoProceso.IdTipoCuentaBancariaCorredora = infoFormaDePago.IdTipoCuentaBancariaCorredora;
					formaDePagoProceso.NombreTipoCuentaBancariaCorredora = infoFormaDePago.NombreTipoCuentaBancariaCorredora;
					formaDePagoProceso.NombreCuentaBancariaCorredora = infoFormaDePago.NombreCuentaBancariaCorredora;
					formaDePagoProceso.NumeroCuentaBancariaCorredora = infoFormaDePago.NumeroCuentaBancariaCorredora;					
					formaDePagoProceso.IdTipoTarjetaBancariaCorredora = "";
					formaDePagoProceso.NombreTipoTarjetaBancariaCorredora = "";
					formaDePagoProceso.NumeroTarjetaBancariaCorredora = "";
					formaDePagoProceso.IdTipoCuentaBancariaSocio = (formaDePagoProceso.IdTipoCuentaBancariaSocio == null || formaDePagoProceso.IdTipoCuentaBancariaSocio.equalsIgnoreCase("")) ? infoFormaDePago.IdTipoCuentaBancariaSocio : formaDePagoProceso.IdTipoCuentaBancariaSocio;
					formaDePagoProceso.NombreTipoCuentaBancariaSocio = (formaDePagoProceso.NombreTipoCuentaBancariaSocio == null || formaDePagoProceso.NombreTipoCuentaBancariaSocio.equalsIgnoreCase(""))  ? infoFormaDePago.NombreTipoCuentaBancariaSocio : formaDePagoProceso.NombreTipoCuentaBancariaSocio;
					formaDePagoProceso.NumeroCuentaBancariaSocio = (formaDePagoProceso.NumeroCuentaBancariaSocio == null || formaDePagoProceso.NumeroCuentaBancariaSocio.equalsIgnoreCase("")) ? infoFormaDePago.NumeroCuentaBancariaSocio : formaDePagoProceso.NumeroCuentaBancariaSocio;					
					formaDePagoProceso.IdBancoSocio =  (formaDePagoProceso.IdBancoSocio == null || formaDePagoProceso.IdBancoSocio.compareTo(0L) == 0) ? infoFormaDePago.IdBancoSocio : formaDePagoProceso.IdBancoSocio;
					
					//Para validaciones
					formaDePagoProceso.IdMedioDePago_Real = infoFormaDePago.IdMedioDePago_Real;
					formaDePagoProceso.IdModalidadDePago_Real = infoFormaDePago.IdModalidadDePago_Real;
					formaDePagoProceso.IdMoneda_Real = infoFormaDePago.IdMoneda_Real;
					formaDePagoProceso.IdEstadoMoneda_Real = infoFormaDePago.IdEstadoMoneda_Real;
					formaDePagoProceso.IdCuentaBancariaCorredora_Real = infoFormaDePago.IdCuentaBancariaCorredora_Real;
					formaDePagoProceso.IdCuentaBancariaSocio_Real = infoFormaDePago.IdCuentaBancariaSocio_Real;
					formaDePagoProceso.IdTarjetaCorredoraBancaria_Real = infoFormaDePago.IdTarjetaCorredoraBancaria_Real;
					formaDePagoProceso.IdTarjetaSocioBancaria_Real = infoFormaDePago.IdTarjetaSocioBancaria_Real;
					formaDePagoProceso.ExisteDescuadreEnMontos  = infoFormaDePago.ExisteDescuadreEnMontos;
					formaDePagoProceso.ExisteDiferenciaMoneda  = infoFormaDePago.ExisteDiferenciaMoneda;
					
					
					if (formaDePagoEntrada.FechaDocumentoPagoCorredora == null || formaDePagoEntrada.FechaDocumentoPagoCorredora == "" || formaDePagoEntrada.FechaDocumentoPagoCorredora.equalsIgnoreCase("0001-01-01"))
						formaDePagoProceso.FechaDocumentoPagoCorredora = Fecha.toDateTime("0001-01-01 00:00:00");
					else{
						if (formaDePagoEntrada.FechaDocumentoPagoCorredora.length() == 10) {
							formaDePagoProceso.FechaDocumentoPagoCorredora = Fecha.toDateTime(formaDePagoEntrada.FechaDocumentoPagoCorredora + " 00:00:00");
						} else {
							formaDePagoProceso.FechaDocumentoPagoCorredora = Fecha.toDateTime(formaDePagoEntrada.FechaDocumentoPagoCorredora);	
						}
					}
					
					if (formaDePagoEntrada.FechaDocumentoPagoSocio == null || formaDePagoEntrada.FechaDocumentoPagoSocio == "" || formaDePagoEntrada.FechaDocumentoPagoSocio.equalsIgnoreCase("0001-01-01"))
						formaDePagoProceso.FechaDocumentoPagoSocio = Fecha.toDateTime("0001-01-01 00:00:00");
					else{
						if (formaDePagoEntrada.FechaDocumentoPagoSocio.length() == 10) {
							formaDePagoProceso.FechaDocumentoPagoSocio = Fecha.toDateTime(formaDePagoEntrada.FechaDocumentoPagoSocio + " 00:00:00");
						} else {
							formaDePagoProceso.FechaDocumentoPagoSocio = Fecha.toDateTime(formaDePagoEntrada.FechaDocumentoPagoSocio);	
						}
					}
					
					if (formaDePagoEntrada.FechaDocumentoPagoCorredora == null || formaDePagoEntrada.FechaDocumentoPagoCorredora == "" || formaDePagoEntrada.FechaDocumentoPagoCorredora.equalsIgnoreCase("0001-01-01"))
						formaDePagoProceso.FechaDocumentoPagoCorredora = Fecha.toDateTime("0001-01-01 00:00:00");
					else{
						if (formaDePagoEntrada.FechaDocumentoPagoCorredora.length() == 10) {
							formaDePagoProceso.FechaDocumentoPagoCorredora = Fecha.toDateTime(formaDePagoEntrada.FechaDocumentoPagoCorredora + " 00:00:00");
						} else {
							formaDePagoProceso.FechaDocumentoPagoCorredora = Fecha.toDateTime(formaDePagoEntrada.FechaDocumentoPagoCorredora);	
						}
					}
					FormaDePago_Proceso.add(formaDePagoProceso);
				}
			}
			
			//---------------------------------------------------------------------------------------------
			//VALIDACIONES DE DATOS DE MOVIMIENTOS 
			{	
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date fechaVacia = sdf.parse("0001-01-01");
				SimpleDateFormat sdfh = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss");
				Date fechaHoraVacia = sdfh.parse("0001-01-01 00:00:00.000");
				
				for (IngresoEgreso_Proceso iep: IngresoEgreso_Proceso) {
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdTipoMovimiento == null || iep.IdTipoMovimiento.length() == 0),"SIN_ESPECIFICAR_TIPO_MOVIMIENTO_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdConcepto == null || iep.IdConcepto.length() == 0),"SIN_ESPECIFICAR_CONCEPTO_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdMoneda == null || iep.IdMoneda.length() == 0),"SIN_ESPECIFICAR_MONEDA_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.Monto == null || iep.Monto.compareTo(new BigDecimal(0)) == 0),"SIN_ESPECIFICAR_MONTO_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.FechaLiquidacion == null || iep.FechaLiquidacion.compareTo(fechaVacia) == 0|| iep.FechaLiquidacion.compareTo(fechaHoraVacia) == 0),"SIN_ESPECIFICAR_FECHA_LIQUIDACION_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdPersona == null || iep.IdPersona == 0),"SIN_ESPECIFICAR_PERSONA_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, ("CLI".equalsIgnoreCase(iep.IdTipoPersonaTitularPago) && (iep.IdPortafolioTitularPago == null || iep.IdPortafolioTitularPago == 0)),"SIN_ESPECIFICAR_PORTAFOLIO_PERSONA_TITULAR_PAGO_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.EsBilateral == null || iep.EsBilateral.length() == 0),"SIN_ESPECIFICAR_INDICADOR_BILATERAL_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.GeneraComprobante == null || iep.GeneraComprobante.length() == 0),"SIN_ESPECIFICAR_INDICADOR_GENERA_COMPROBANTE_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.AgrupaComprobante == null || iep.AgrupaComprobante.length() == 0),"SIN_ESPECIFICAR_INDICADOR_AGRUPA_EN_COMPROBANTE_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.TitularPago == null || iep.TitularPago.length() == 0),"SIN_ESPECIFICAR_INDICADOR_TITULAR_PAGO_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdTipoMovimiento_Real == null),"SIN_DEFINIR_TIPO_MOVIMIENTO_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);					
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdConcepto_Real == null),"SIN_DEFINIR_CONCEPTO_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdMoneda_Real == null),"MONEDA_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdEstadoMoneda_Real == null),"MONEDA_NO_ACTIVA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdCaja_Real == null && iep.IdCaja != null && iep.IdCaja != 0 ),"CAJA_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdMonedaTesoreria_Real == null),"MONEDA_NO_HABILITADA_PARA_OPERAR_EN_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.Monto.compareTo(new BigDecimal(0)) < 0),"MONTO_MENOR_IGUAL_A_CERO",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdPersona_Real == null || iep.IdPersona_Real == 0),"PERSONA_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);					
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, ("INA".equalsIgnoreCase(iep.IdEstado_PersonaReal)),"PERSONA_NO_ACTIVA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdPersonaRelacionada_Real == null || iep.IdPersonaRelacionada_Real == 0),"PERSONA_RELACIONADA_NO_EXISTE_CIE",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);					
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, ("INA".equalsIgnoreCase(iep.IdEstado_RelacionadoReal)),"PERSONA_RELACIONADA_NO_ACTIVA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.SecuenciaAgrupacion != null && iep.SecuenciaAgrupacion.length() > 0 && iep.CountSec > 0),"SECUENCIA_AGRUPACION_UTILIZADA_TESORERIA",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdTipoProducto != null && iep.IdTipoProducto.length() > 0 && (iep.IdTipoProducto_Real == null || iep.IdTipoProducto_Real.length() == 0)),"TIPO_PRODUCTO_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdTipoOperacion != null && iep.IdTipoOperacion.length() > 0 && (iep.IdTipoOperacion_Real == null || iep.IdTipoOperacion_Real.length() == 0)),"TIPO_OPERACION_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.IdCondicionLiquidacion != null && iep.IdCondicionLiquidacion.length() > 0 && (iep.IdCondicionLiquidacion_Real == null || iep.IdCondicionLiquidacion_Real.length() == 0)),"CONDICION_DE_LIQUIDACION_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, ("MOVING".equalsIgnoreCase(iep.IdTipoMovimiento) && "S".equalsIgnoreCase(iep.BloqueadoPorCumplimiento) ),"CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, ("CLI".equalsIgnoreCase(iep.IdTipoPersona) && iep.EstadoPortafolio != null && !"ACT".equalsIgnoreCase(iep.EstadoPortafolio)),"PORTAFOLIO_BLOQUEADO",MensajeSalida.MENSAJE_ADVERTENCIA,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, ("CLI".equalsIgnoreCase(iep.IdTipoPersonaTitularPago) && iep.EstadoPortafolioTitularPago != null && iep.EstadoPortafolio != null && iep.IdPortafolio.compareTo(iep.IdPortafolioTitularPago) != 0 && !"ACT".equalsIgnoreCase(iep.EstadoPortafolioTitularPago)),"PORTAFOLIO_BLOQUEADO_TITULAR_PAGO",MensajeSalida.MENSAJE_ADVERTENCIA,iep,out.P_OUT_MENSAJES);
					ProcesaRequisitoIngresoEgreso(con, in.p_IdEmpresa, (iep.CodigoEjecutivoDigitador != null && !iep.CodigoEjecutivoDigitador.trim().isEmpty() && (iep.IdEmpleadoDigitador == null || iep.IdEmpleadoDigitador == 0L)),"CODIGO_EJECUTIVO_DIGITADOR_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
				}
								
				limpiarIngresosEgresos(IngresoEgreso_Proceso, FormaDePago_Proceso, ingresoEgresoConErrores);
				if (IngresoEgreso_Proceso.size() == 0) {
					logger.debug("DATOS DE MOVIMIENTOS INVALIDALIDOS > " + ingresoEgresoConErrores.toString());
					out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					return LimpiarSalida(out);
				} else {	
					logger.debug("VALIDACION DE DATOS DE MOVIMIENTOS REALIZADA");
				}
			}
			

			//---------------------------------------------------------------------------------------------
			//VALIDACIONES DE DATOS DE FORMA DE PAGO 
			{	
				for (FormaDePago_Proceso fpp: FormaDePago_Proceso) {	
					ProcesaRequisitoFormaDePago(con, in.p_IdEmpresa, (fpp.IdMedioDePago_Real == null || fpp.IdMedioDePago_Real.length() == 0),"MEDIO_DE_PAGO_INVALIDO",MensajeSalida.MENSAJE_ERROR,fpp,out.P_OUT_MENSAJES);
					ProcesaRequisitoFormaDePago(con, in.p_IdEmpresa, ("CIN".equalsIgnoreCase(fpp.IdMedioDePago_Real)),"MEDIO_DE_PAGO_INVALIDO",MensajeSalida.MENSAJE_ERROR,fpp,out.P_OUT_MENSAJES); // Cta Inv NO es un medio de pago valido para tesoreria ya que es da cuando el cliente opera con cta. inv.
					ProcesaRequisitoFormaDePago(con, in.p_IdEmpresa, (!"CIN".equalsIgnoreCase(fpp.IdMedioDePago) && (fpp.IdModalidadDePago_Real == null || fpp.IdModalidadDePago_Real.length() == 0)),"MODALIDAD_DE_PAGO_INVALIDO",MensajeSalida.MENSAJE_ERROR,fpp,out.P_OUT_MENSAJES);				
					ProcesaRequisitoFormaDePago(con, in.p_IdEmpresa, (fpp.IdMoneda_Real == null || fpp.IdMoneda_Real.length() == 0),"MONEDA_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,fpp,out.P_OUT_MENSAJES);					
					ProcesaRequisitoFormaDePago(con, in.p_IdEmpresa, ("INA".equalsIgnoreCase(fpp.IdEstadoMoneda_Real)),"MONEDA_NO_ACTIVA",MensajeSalida.MENSAJE_ERROR,fpp,out.P_OUT_MENSAJES);					
					ProcesaRequisitoFormaDePago(con, in.p_IdEmpresa, (fpp.Monto.compareTo(new BigDecimal(0)) == 0 || fpp.Monto.compareTo(new BigDecimal(0)) < 0),"MONTO_MENOR_IGUAL_A_CERO",MensajeSalida.MENSAJE_ERROR,fpp,out.P_OUT_MENSAJES);					
					ProcesaRequisitoFormaDePago(con, in.p_IdEmpresa, (fpp.IdCuentaBancariaSocio > 0 && fpp.IdCuentaBancariaSocio_Real == 0),"CUENTA_BANCARIA_SOCIO_INVALIDA",MensajeSalida.MENSAJE_ERROR,fpp,out.P_OUT_MENSAJES); 					
					ProcesaRequisitoFormaDePago(con, in.p_IdEmpresa, (fpp.IdCuentaBancariaCorredora > 0 && fpp.IdCuentaBancariaCorredora_Real == 0),"CUENTA_BANCARIA_CORREDORA_INVALIDA",MensajeSalida.MENSAJE_ERROR,fpp,out.P_OUT_MENSAJES);
					ProcesaRequisitoFormaDePago(con, in.p_IdEmpresa, (fpp.IdTarjetaBancariaCorredora > 0 && fpp.IdTarjetaCorredoraBancaria_Real == 0),"TARJETA_BANCARIA_CORREDORA_INVALIDA",MensajeSalida.MENSAJE_ERROR,fpp,out.P_OUT_MENSAJES);
					ProcesaRequisitoFormaDePago(con, in.p_IdEmpresa, (fpp.IdTarjetaBancariaSocio > 0 && fpp.IdTarjetaSocioBancaria_Real == 0),"TARJETA_BANCARIA_SOCIO_INVALIDA",MensajeSalida.MENSAJE_ERROR,fpp,out.P_OUT_MENSAJES);
					ProcesaRequisitoFormaDePago(con, in.p_IdEmpresa, (fpp.ExisteDiferenciaMoneda != null && fpp.ExisteDiferenciaMoneda > 0),"DIFERENTE_MONEDA_ENTRE_MOVIMIENTO_Y_FORMA_PAGO",MensajeSalida.MENSAJE_ERROR,fpp,out.P_OUT_MENSAJES);						
					ProcesaRequisitoFormaDePago(con, in.p_IdEmpresa, ("TES".equalsIgnoreCase(in.p_IdModulo) && fpp.ExisteDescuadreEnMontos > 0 ),"MONTO_MOVIMIENTO_NO_CUADRA_FORMAS_DE_PAGO",MensajeSalida.MENSAJE_ERROR,fpp,out.P_OUT_MENSAJES);
				}
				
				limpiarFormasDePago(FormaDePago_Proceso, IngresoEgreso_Proceso, formaDePagoConErrores);
				
				if (FormaDePago_Proceso.size() == 0) {
					logger.debug("DATOS DE FOMA DE PAGO INVALIDADOS > " + formaDePagoConErrores.toString());
				} else {	
					logger.debug("VALIDACION DE DATOS DE FORMA DE PAGO REALIZADA");
				}
			}			

			if (IngresoEgreso_Proceso.size() == 0) {
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return LimpiarSalida(out);
			}
			
			//FLUJO CAJA
			{
				if (pValidaFlujoCajaEgresoNegocio.equalsIgnoreCase(_c.SI) || (pValidaFlujoCajaEgresoManual.equalsIgnoreCase(_c.SI) && "TES".equalsIgnoreCase(in.p_IdModulo) && "N".equalsIgnoreCase(in.p_EsIngresoAutomatico)))	{
					SaldoFlujoCaja.ServiceEntrada entradaFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
					java.sql.Date fechaLiquidacionMinima = IngresoEgreso_Proceso.get(0).FechaLiquidacion;
					java.sql.Date fechaLiquidacionMaxima = IngresoEgreso_Proceso.get(0).FechaLiquidacion;
					
					HashSet<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafoliosFlujoCaja = new HashSet<>();
					HashMap<FlujoCajaElem,BigDecimal> totalEgresosPortafolio = new HashMap<>();
					HashMap<FlujoCajaElem,BigDecimal> totalFlujoCajaPortafolio = new HashMap<>();
					
					
					for (	IngresoEgreso_Proceso iep: IngresoEgreso_Proceso) {
						// jvillagran ... cambio 111
						if(iep.IdPortafolio.compareTo(0L) != 0 && iep.IdTipoMovimiento.equalsIgnoreCase("MOVEGR")) {
							java.sql.Date fechaLiquidacion = iep.FechaLiquidacion;
							
							if (fechaLiquidacion.before(fechaLiquidacionMinima)) {
								fechaLiquidacionMinima = fechaLiquidacion;
							}
							
							if (fechaLiquidacion.after(fechaLiquidacionMaxima)) {
								fechaLiquidacionMaxima = fechaLiquidacion;
							}
							
							FlujoCajaElem llave = new FlujoCajaElem(iep.IdMoneda, Fecha.toStringDate(iep.FechaLiquidacion),iep.IdPortafolio);
							if (!totalEgresosPortafolio.containsKey(llave)) {
								totalEgresosPortafolio.put(llave, BigDecimal.ZERO);
							}
							BigDecimal totalEgresos = totalEgresosPortafolio.get(llave);
							totalEgresos = totalEgresos.add(iep.Monto);
							totalEgresosPortafolio.put(llave, totalEgresos);
							
							SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR port = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
							port.IdMoneda = iep.IdMoneda;
							port.IdPortafolio = iep.IdPortafolio;
							portafoliosFlujoCaja.add(port);
							
						}
					}
					
					if (portafoliosFlujoCaja.size() > 0) {
				    	
				    	entradaFlujoCaja.P_IDEMPRESA = in.p_IdEmpresa;
						entradaFlujoCaja.P_FECHADESDE = Fecha.toStringDate(fechaLiquidacionMinima);
						entradaFlujoCaja.P_FECHAHASTA = Fecha.toStringDate(fechaLiquidacionMaxima);
						
						// Si los movimientos son manuales usa flujo EGRESOS_TESORERIA(no incluye linea sobregiro), si provienen desde el negocio usa flujo CUENTA_INVERSION (que incluye linea de sobregiro)
						entradaFlujoCaja.P_USO = ("TES".equalsIgnoreCase(in.p_IdModulo) && "N".equalsIgnoreCase(in.p_EsIngresoAutomatico))  ? "EGRESOS_TESORERIA" : "CUENTA_INVERSION";
						entradaFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<ObjPORTAFOLIOACONSULTAR>(portafoliosFlujoCaja);
						
						SaldoFlujoCaja saldoFlujoCajaService = new SaldoFlujoCaja();
						SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = saldoFlujoCajaService.logica(con,entradaFlujoCaja);
						
						//TODO MANEJO DE ERRORES
						List<ObjFLUJOCAJA> asdf = salidaFlujoCaja.P_OUT_FLUJOCAJA;
						for (ObjFLUJOCAJA flj : asdf) {
							FlujoCajaElem llave = new FlujoCajaElem(flj.IdMoneda, flj.FechaSaldo,flj.IdPortafolio);
							if (!totalFlujoCajaPortafolio.containsKey(llave)) {
								totalFlujoCajaPortafolio.put(llave, BigDecimal.ZERO);
							}
							BigDecimal totalFlujoCaja = totalFlujoCajaPortafolio.get(llave);
							totalFlujoCaja = totalFlujoCaja.add(flj.Saldo);
							totalFlujoCajaPortafolio.put(llave, totalFlujoCaja);
						}
				    }
				    //actualizamos Total Monto Portafolio con total por grupo (portafolio,moneda de liquidacion,fecha liquidacion)

			        for (IngresoEgreso_Proceso iep: IngresoEgreso_Proceso) {
			        	FlujoCajaElem llave = new FlujoCajaElem(iep.IdMoneda, Fecha.toStringDate(iep.FechaLiquidacion),iep.IdPortafolio);			        	
			        	BigDecimal totalEgresos = totalEgresosPortafolio.get(llave);
			        	BigDecimal totalFlujoCaja = totalFlujoCajaPortafolio.get(llave);
			        	
			        	if (totalEgresos != null)			        	
			        		iep.TotalMontoPortafolio = totalEgresos;
			        	
			        	if (totalFlujoCaja != null)
			        		iep.SaldoCajaPortafolio = totalFlujoCaja;			        	

			        } 
				}
			}
			
			//Validaciones restantes de los Ingresos/Egresos, Caso Flujo Caja
			{	
				if(pValidaFlujoCajaEgresoNegocio.equalsIgnoreCase(_c.SI) || (pValidaFlujoCajaEgresoManual.equalsIgnoreCase(_c.SI) && "TES".equalsIgnoreCase(in.p_IdModulo) && "N".equalsIgnoreCase(in.p_EsIngresoAutomatico))) {
					for (IngresoEgreso_Proceso iep: IngresoEgreso_Proceso) {
						//valida solo los movimientos de tipo egreso
						ProcesaRequisitoIngresoEgreso(con,in.p_IdEmpresa,( iep.IdTipoMovimiento.equalsIgnoreCase("MOVEGR") &&  iep.TotalMontoPortafolio.doubleValue() > iep.SaldoCajaPortafolio.doubleValue()),"FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO",MensajeSalida.MENSAJE_ERROR,iep,out.P_OUT_MENSAJES);
					}
					
					limpiarIngresosEgresos(IngresoEgreso_Proceso, FormaDePago_Proceso, ingresoEgresoConErrores);
					if (IngresoEgreso_Proceso.size() == 0) {
						logger.debug("DATOS DE MOVIMIENTOS INVALIDALIDOS > " + ingresoEgresoConErrores.toString());
						out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
						return LimpiarSalida(out);
					} else {	
						logger.debug("VALIDACION DE DATOS DE MOVIMIENTOS REALIZADA");
					}
				}
			}
			//---------------------------------------------------------------------------------------------			
			//INSERTAR MOVIMIENTOS DE INGRESO/EGRESO
			
			//Genera las secuencias de los nuevos movimientos de I/E
			List<Long> secuenciasIngresoEgreso = Utils.getSecuencia(con, "BSCL.Tesoreria.BD.Secuencias::IngresoEgreso", new Long(IngresoEgreso_Proceso.size()));

			//Obtiene datos de I/E finales (Actualiza últimos datos antes de crear movimientos)
			ArrayList<IngresoEgreso> IngresoEgresoFinales = new ArrayList<>();
			{	
				for (IngresoEgreso_Proceso ingresoEgresoProceso: IngresoEgreso_Proceso) {
					IngresoEgreso ie = new IngresoEgreso();
					Utils.copyPojo(ingresoEgresoProceso, ie);
					
					ie.FechaIngresoRegistro=ie.FechaIngresoRegistro==null?ingresoEgresoProceso.FechaOperacion:ie.FechaIngresoRegistro;
					ie.IdIngresoEgreso = secuenciasIngresoEgreso.get(IngresoEgreso_Proceso.indexOf(ingresoEgresoProceso));
					ingresoEgresoProceso.IdIngresoEgreso = ie.IdIngresoEgreso; //se usa mas abajo para generar la salida de sp y generar comprobante
					ie.IdUsuarioEmision = ingresoEgresoProceso.IdUsuario;
					ie.IdEstado =  (ingresoEgresoProceso.RequiereAutorizacion).equals("S") ? "PDA" : "ING";     // Se crea con estado INGRESADO 
					
					IngresoEgresoFinales.add(ie);
				}
			}
			
			//Inserta los movimientos(cabecera)
			if (in.p_SimularOperacion.equalsIgnoreCase("N")) { 
				if (IngresoEgresoFinales.size() > 0) {
					_sql.batch(con, SQL_INSERT_IngresoEgreso, IngresoEgresoFinales , IngresoEgreso.class);
					logger.debug("INSERCION DE MOVIMIENTO I/E OK");
				}
			}	
			
			//Agregar Log ingreso TESORERIA
			AgregarLog agregarLog = new AgregarLog();
			InAgregarLog entradaLog = new InAgregarLog();

			if (IngresoEgresoFinales != null && !IngresoEgresoFinales.isEmpty()) {

				List<Long> lstIngresoEgresoFinales = IngresoEgresoFinales.stream().map(procesamiento ->{
					return procesamiento.IdInstrumento;
				}).distinct().collect(Collectors.toList());
			
				List<ObjInstrumento> listaInstrumento = new ArrayList();
				HashMap<Long,Object> hashInstrumento = new HashMap();
			
				if (!lstIngresoEgresoFinales.isEmpty()) {
					HashMap<String,Object> parmsIn2 = new HashMap<String,Object>();
					parmsIn2.put("IdInstrumento",lstIngresoEgresoFinales);
					listaInstrumento = _sql.query(con, SQL_SELECT_INSTRUMENTO, parmsIn2, ObjInstrumento.class);
					hashInstrumento = Utils.List2Hash(listaInstrumento, "IdInstrumento");
				}
				
				for(IngresoEgreso datosLog : IngresoEgresoFinales) {
					ObjInstrumento instrumento = new ObjInstrumento();
					if(hashInstrumento.size()>0) {
						instrumento = (ObjInstrumento) hashInstrumento.get(datosLog.IdInstrumento);
					}
				
					//Construir entradas log
					InAgregarLog.Log headLog = new InAgregarLog.Log();
					headLog.NombreProceso = "Crear Ingreso Egreso Tesoreria";
					headLog.IdCabeceraLog = datosLog.IdOperacion != null ? datosLog.IdOperacion : 0L;
					headLog.IdTipoLog = "INGRESO";
					headLog.IdModulo = in.p_IdModulo;
					headLog.IdTipoRegistro = "INGRESO_TESORERIA";
					headLog.IdUsuario = in.p_IdUsuario;
					headLog.IdEmpresa = in.p_IdEmpresa;
					headLog.FechaSistemaLog = fechaSistema != null ? fechaSistema : "";
					headLog.IdUsuarioAtendido = in.p_IdUsuario;
					headLog.IdPersona = datosLog.IdPersona != null ? datosLog.IdPersona : 0L;
					headLog.IdPortafolio = datosLog.IdPortafolio != null ? datosLog.IdPortafolio : 0L;
					headLog.IdTipoOperacion = datosLog.IdTipoOperacion != null ? datosLog.IdTipoOperacion : "";
					headLog.Folio = datosLog.IdOperacion != null ? datosLog.IdOperacion : 0L;
					headLog.Monto = datosLog.Monto != null ? datosLog.Monto : BigDecimal.ZERO;
					headLog.IdMonedaOrigen = datosLog.IdMoneda != null ? datosLog.IdMoneda : "";
					headLog.Nemo = (instrumento != null && instrumento.Nemotecnico != null) ? instrumento.Nemotecnico : "";
					
					entradaLog.p_Log.add(headLog);
				}	
				//llamar al log
				agregarLog.logica(con, entradaLog);
			}	
			//---------------------------------------------------------------------------------------------			
			//INSERTAR FORMAS DE PAGO DE LOS MOVIMIENTOS
			
			//Genera secuencias para Formas de Pago
			List<Long> secuenciasFormaDePago = Utils.getSecuencia(con, "BSCL.Tesoreria.BD.Secuencias::IngresoEgresoFormaDePago", new Long(FormaDePago_Proceso.size()));
			HashMap<Long,Object> _ingresoEgresoProcesoValido = Utils.List2Hash(IngresoEgreso_Proceso, "NumeroReferencia");   //transforma la lista IngresoEgresoProceso en un hash
			
			//Obtiene formas de pagos finales	
			ArrayList<IngresoEgresoFormaDePago> formaDePagoFinales = new ArrayList<>();
			{	
				for (FormaDePago_Proceso formaDePagoProceso: FormaDePago_Proceso) {	
					IngresoEgresoFormaDePago formaDePago = new IngresoEgresoFormaDePago();
					Utils.copyPojo(formaDePagoProceso, formaDePago);
					
					//Obtiene datos del movimiento de IE(encabezado) y los asigna al objeto local // OJO ACA //
					IngresoEgreso_Proceso ingresoEgresoProcesoValido = (IngresoEgreso_Proceso)  _ingresoEgresoProcesoValido.get(formaDePagoProceso.NumeroReferencia);						
					formaDePago.IdIngresoEgreso = ingresoEgresoProcesoValido.IdIngresoEgreso;
					formaDePago.IdIngresoEgresoFormaDePago = secuenciasFormaDePago.get(FormaDePago_Proceso.indexOf(formaDePagoProceso));
					formaDePagoProceso.IdIngresoEgresoFormaDePago = formaDePago.IdIngresoEgresoFormaDePago;
					formaDePago.IdEstado = "ING";   // Se crea con estado "Ingresado"
					formaDePagoFinales.add(formaDePago);						
				}
		    }
			
			//Insertar Formas de Pago de los movimientos			
			if (in.p_SimularOperacion.equalsIgnoreCase("N")) {	
				if (formaDePagoFinales.size() > 0 && (IngresoEgresoFinales.size() > 0)) {
					_sql.batch(con, SQL_INSERT_IngresoEgresoFormaDePago, formaDePagoFinales , IngresoEgresoFormaDePago.class);
					logger.debug("SE INSERTAN FORMAS DE PAGO OK");
				}
			}			
			
			//---------------------------------------------------------------------------------------------
			//PREPARA SALIDAS DEL SERVICIO (PARA EL INGRESO DE LOS MOVIMIENTOS)
			
			for (IngresoEgreso_Proceso ingresoEgresoProceso: IngresoEgreso_Proceso) {
				IngresoEgresoGenerado_Salida ingresoEgresoGenerado = new IngresoEgresoGenerado_Salida();
				ResultadoProceso_Salida resultadoProceso = new ResultadoProceso_Salida();
				Utils.copyPojo(ingresoEgresoProceso, ingresoEgresoGenerado);
				
				resultadoProceso.Campo 		= "IdIngresoEgreso";
				resultadoProceso.Id 		= ingresoEgresoProceso.IdIngresoEgreso;
				resultadoProceso.Servicio 	= "cl.bcs.tesoreria.CrearIngresoEgreso";
				resultadoProceso.Resultado 	= "ING";
				resultadoProceso.NumeroReferencia	= ingresoEgresoProceso.NumeroReferencia;
				
				out.P_OUT_INGRESOEGRESOGENERADO.add(ingresoEgresoGenerado);
				out.P_OUT_RESULTADOPROCESO.add(resultadoProceso);
				
				//Mensaje de éxito creación de movimientos 
				String codigoMensajeExito = "";
				if (ingresoEgresoGenerado.IdTipoMovimiento.equalsIgnoreCase("MOVING"))
					codigoMensajeExito = "UI_CREADO_MOVIMIENTO_INGRESO";
				else if(ingresoEgresoGenerado.IdTipoMovimiento.equalsIgnoreCase("MOVEGR"))
					codigoMensajeExito = "UI_CREADO_MOVIMIENTO_EGRESO";
				
				String mensajeExito = Diccionario.getMensaje(con, in.p_IdEmpresa, codigoMensajeExito).add("IdIngresoEgreso", ingresoEgresoGenerado.IdIngresoEgreso).msg();				
				out.P_OUT_MENSAJES.add(new MensajeSalida(ingresoEgresoGenerado.NumeroReferencia,MensajeSalida.MENSAJE_OK,codigoMensajeExito,mensajeExito));
			}
			
			    
			
			//---------------------------------------------------------------------------------------------
			// GENERAR COMPROBANTES DE LOS MOVIMIENTOS Y LIQUIDA COMPROBANTES DE SER NECESARIO
			
			//Obtiene los movimientos que deben generar comprobante y comprobantes a liquidar
			List<IngresoEgresoGenerar_Entrada> IngresoEgresoGenerar = new ArrayList<IngresoEgresoGenerar_Entrada>();
			List<Long> IngresoEgresoLiquidar = new ArrayList<Long>();
			{
				for (IngresoEgreso_Proceso ingresoEgresoProceso: IngresoEgreso_Proceso) {
					if (ingresoEgresoProceso.GeneraComprobante.equalsIgnoreCase("S") || ("N".equals(in.p_EsIngresoUI) && "S".equals(ingresoEgresoProceso.GeneraComprobanteAutomaticamente))) {
						IngresoEgresoGenerar_Entrada ingresoEgresoGenerar = new IngresoEgresoGenerar_Entrada();
						Utils.copyPojo(ingresoEgresoProceso, ingresoEgresoGenerar);
						ingresoEgresoGenerar.TipoComprobante = "";       // PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
						IngresoEgresoGenerar.add(ingresoEgresoGenerar);
						
						if (ingresoEgresoProceso.LiquidarComprobante.equalsIgnoreCase("S") ) {
							IngresoEgresoLiquidar.add(ingresoEgresoProceso.IdIngresoEgreso);
						}
					}
				}
			}
			
			String FechaMovimiento = "";
			if (in.p_FechaMovimiento == null || in.p_FechaMovimiento.isEmpty() || in.p_FechaMovimiento.equalsIgnoreCase("0001-01-01"))
				FechaMovimiento = fechaSistema;
			else
				FechaMovimiento = in.p_FechaMovimiento;
			
			
			if (IngresoEgresoGenerar.size() > 0) {
				logger.debug("SE INVOCA SERVICIO CREAR COMPROBANTE TESORERIA");
				CrearComprobante_Entrada crearComprobanteEntrada = new CrearComprobante_Entrada();
				
				crearComprobanteEntrada.p_IdEmpresa = in.p_IdEmpresa;  
				crearComprobanteEntrada.p_IdUsuario = in.p_IdUsuario; 
				crearComprobanteEntrada.p_IdModulo = in.p_IdModulo;
				crearComprobanteEntrada.p_SimularOperacion = (IngresoEgreso_Proceso.get(0).RequiereAutorizacion).toString().equals("S") ? "S" : in.p_SimularOperacion;
				crearComprobanteEntrada.p_UsaReglasNegocio = "N";
				crearComprobanteEntrada.p_FechaMovimiento = FechaMovimiento;				
				crearComprobanteEntrada.p_IdCaja = 0L;		
				crearComprobanteEntrada.p_GenerarComprobanteDesdeMovimientos  = (IngresoEgreso_Proceso.get(0).RequiereAutorizacion).toString().equals("S") ? "N" : "S";
				crearComprobanteEntrada.p_GenerarComprobanteDesdeComprobantes = "N";	
				crearComprobanteEntrada.p_EsIngresoAutomatico = "S";
				crearComprobanteEntrada.p_IngresoEgresoGenerar.addAll(IngresoEgresoGenerar); //Generar comprobantes desde movimientos
				
				
				CrearComprobante crearComprobante = new CrearComprobante();
				CrearComprobante_Salida crearComprobante_Salida = crearComprobante.logica(con, crearComprobanteEntrada);
				
				if (MensajeSalida.getNumeroErrores(crearComprobante_Salida.P_OUT_MENSAJES) > 0) {
					logger.debug("GENERACION DE COMPROBANTES CON ERRORES: " + crearComprobante_Salida.P_OUT_MENSAJES);
					out.P_OUT_MENSAJES.addAll(crearComprobante_Salida.P_OUT_MENSAJES);
					out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					return LimpiarSalida(out);
				} else {
					logger.debug("GENERACION DE COMPROBANTES OK: " + crearComprobante_Salida.P_OUT_MENSAJES);
					out.P_OUT_MENSAJES.addAll(crearComprobante_Salida.P_OUT_MENSAJES);
				}
				//Obtiene y agrega a la salida el resultado del proceso de generar comprobante 
				for (CrearComprobante.ResultadoProceso_Salida resultadoProcesoCOM: crearComprobante_Salida.P_OUT_RESULTADOPROCESO) {
					ResultadoProceso_Salida resultadoProcesoIE = new ResultadoProceso_Salida();
					Utils.copyPojo(resultadoProcesoCOM, resultadoProcesoIE);
					out.P_OUT_RESULTADOPROCESO.add(resultadoProcesoIE);
				}
			}
			
			if ( IngresoEgresoLiquidar.size() > 0 ) {
				
				HashMap<String,Object> paramsComprobantes = new HashMap<>();
				paramsComprobantes.put("IdsIngresoEgreso", IngresoEgresoLiquidar);
				
				List<ComprobanteLiquidar_Entrada> comprobantesLiquidar = _sql.query(con, SQL_SELECT_COMPROBANTES, paramsComprobantes , ComprobanteLiquidar_Entrada.class);
				
				LiquidarComprobante_Entrada liquidarComprobanteEntrada = new LiquidarComprobante_Entrada();
				
				liquidarComprobanteEntrada.p_IdEmpresa = in.p_IdEmpresa;  
				liquidarComprobanteEntrada.p_IdUsuario = in.p_IdUsuario; 
				liquidarComprobanteEntrada.p_IdModulo = in.p_IdModulo;
				liquidarComprobanteEntrada.p_SimularOperacion = in.p_SimularOperacion;
				liquidarComprobanteEntrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				liquidarComprobanteEntrada.p_FechaMovimiento = FechaMovimiento;
				liquidarComprobanteEntrada.p_ComprobanteLiquidar = comprobantesLiquidar;
				liquidarComprobanteEntrada.FolioCSC = in.p_IngresoEgresoGenerar.get(0).FolioCSC;
				
				LiquidarComprobante liquidarComprobante = new LiquidarComprobante();
				LiquidarComprobante_Salida liquidarComprobanteSalida = liquidarComprobante.logica(con, liquidarComprobanteEntrada);
				
				if (MensajeSalida.getNumeroErrores(liquidarComprobanteSalida.P_OUT_MENSAJES) > 0) {
					logger.debug("LIQUIDACION DE COMPROBANTES CON ERRORES: " + liquidarComprobanteSalida.P_OUT_MENSAJES);
					out.P_OUT_MENSAJES.addAll(liquidarComprobanteSalida.P_OUT_MENSAJES);
					out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					return LimpiarSalida(out);
		
				} else {
					logger.debug("LIQUIDACION DE COMPROBANTES OK: " + liquidarComprobanteSalida.P_OUT_MENSAJES);
					out.P_OUT_MENSAJES.addAll(liquidarComprobanteSalida.P_OUT_MENSAJES);
				}
				
				//Obtiene y agrega a la salida el resultado del proceso de liquidar comprobantes 
				for ( LiquidarComprobante.ResultadoProceso_Salida resultadoProcesoLiq: liquidarComprobanteSalida.P_OUT_RESULTADOPROCESO ) {
					ResultadoProceso_Salida resultadoProcesoIE = new ResultadoProceso_Salida();
					Utils.copyPojo(resultadoProcesoLiq, resultadoProcesoIE);
					out.P_OUT_RESULTADOPROCESO.add(resultadoProcesoIE);
				}
			}
			
		}catch (Exception er)  {
			er.printStackTrace();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_INGRESO_EGRESO",null,con,in.p_IdEmpresa));
		}finally {
			SQL.exec(con,SQL_DELETE_IngresoEgreso_Proceso);
			SQL.exec(con,SQL_DELETE_IngresoEgresoFormaDePago_Proceso);
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		logger.info("== TERMINO CREAR INGRESO/EGRESO ==");
		
		return LimpiarSalida(out);
		
	}

	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------	
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) 
		{
			Gson gson = Utils.getGsonBuilder();

			String test = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FormaDePago\":[],\"p_IngresoEgresoGenerar\":[{\"IdTipoMovimiento\":\"MOVEGR\",\"IdTipoPersona\":\"CLI\",\"IdPersona\":5632,\"IdPortafolio\":403,\"IdMoneda\":\"CLP\",\"Monto\":10000,\"FechaLiquidacion\":\"2025-06-17\",\"IdConcepto\":\"FCOT00\",\"IdTipoPersonaTitularPago\":\"CLI\",\"IdPersonaTitularPago\":72932,\"IdPortafolioTitularPago\":12161,\"IdCaja\":0,\"SecuenciaAgrupacion\":\"\",\"SecuenciaRelacionada\":null,\"Glosa\":\"\",\"IdEmpleadoDigitador\":108,\"IdSucursalEmpleadoDigitador\":0,\"IdTipoComprobante\":\"\",\"IdComprobante\":0,\"FechaComprobante\":\"0001-01-01\",\"FechaLiquidacionComprobante\":\"0001-01-01\",\"FolioCartera\":0,\"IdInstrumento\":0,\"IdTipoInstrumento\":\"\",\"IdClasificacionTipoInstrumento\":\"\",\"IdTipoProducto\":\"\",\"IdTipoOperacion\":\"\",\"IdOperacion\":0,\"FechaOperacion\":\"2025-06-17\",\"IdCondicionLiquidacion\":\"\",\"IdBolsa\":\"\",\"IdMonedaOperacion\":\"\",\"MontoOperacion\":0,\"IdTipoReajuste\":\"\",\"FechaParidad\":\"0001-01-01\",\"ValorParidad\":0,\"Bilateral\":\"N\",\"GeneraComprobante\":\"N\",\"AgruparEnComprobante\":\"N\",\"MueveCuentaInversion\":\"S\",\"TitularPago\":\"S\",\"NumeroReferencia\":1}],\"p_EsIngresoAutomatico\":\"N\",\"p_EsIngresoUI\":\"S\"}";
			String test2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FormaDePago\":[],\"p_IngresoEgresoGenerar\":[{\"IdTipoMovimiento\":\"MOVING\",\"IdTipoPersona\":\"CLI\",\"IdPersona\":29705,\"IdPortafolio\":7009,\"IdMoneda\":\"CLP\",\"Monto\":10000,\"FechaLiquidacion\":\"2025-07-28\",\"IdConcepto\":\"FCOT00\",\"IdTipoPersonaTitularPago\":\"CLI\",\"IdPersonaTitularPago\":29705,\"IdPortafolioTitularPago\":7009,\"IdCaja\":0,\"SecuenciaAgrupacion\":\"\",\"SecuenciaRelacionada\":null,\"Glosa\":\"\",\"IdEmpleadoDigitador\":108,\"IdSucursalEmpleadoDigitador\":0,\"IdTipoComprobante\":\"\",\"IdComprobante\":0,\"FechaComprobante\":\"0001-01-01\",\"FechaLiquidacionComprobante\":\"0001-01-01\",\"FolioCartera\":0,\"IdInstrumento\":0,\"IdTipoInstrumento\":\"\",\"IdClasificacionTipoInstrumento\":\"\",\"IdTipoProducto\":\"\",\"IdTipoOperacion\":\"\",\"IdOperacion\":0,\"FechaOperacion\":\"2025-07-28\",\"IdCondicionLiquidacion\":\"\",\"IdBolsa\":\"\",\"IdMonedaOperacion\":\"\",\"MontoOperacion\":0,\"IdTipoReajuste\":\"\",\"FechaParidad\":\"0001-01-01\",\"ValorParidad\":0,\"Bilateral\":\"N\",\"GeneraComprobante\":\"N\",\"AgruparEnComprobante\":\"N\",\"MueveCuentaInversion\":\"S\",\"TitularPago\":\"N\",\"NumeroReferencia\":1}],\"p_EsIngresoAutomatico\":\"N\",\"p_EsIngresoUI\":\"S\"}";
			
			CrearIngresoEgreso_Entrada Objin = gson.fromJson(test2, CrearIngresoEgreso_Entrada.class);
			CrearIngresoEgreso demo = new CrearIngresoEgreso();
			
 			System.out.println("ENTRADA:");
 			System.out.println(Objin.toString());
 			
 			try 
 			{	
 				CrearIngresoEgreso_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				con.rollback();
 				//con.commit();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();	
				//con.commit();
			}
 			
 			
 			con.close();
 			
	    	}
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}