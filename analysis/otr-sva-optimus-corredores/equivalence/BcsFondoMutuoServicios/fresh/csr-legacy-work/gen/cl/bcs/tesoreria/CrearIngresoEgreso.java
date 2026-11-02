// GENERATED DOUBLE of the legacy cl.bcs.tesoreria.CrearIngresoEgreso by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


import java.util.Date;

import com.google.gson.JsonObject;

import cl.bcs.utils.BcsPojo;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;



public class CrearIngresoEgreso implements JavaCall {

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


	

	//Quita del proceso los movimientos que no son validos(y las formas de pago asociadas)
	public void limpiarIngresosEgresos(List<IngresoEgreso_Proceso> ingresoEgresoProceso, List<FormaDePago_Proceso> formaDePago_Proceso, List<IngresoEgreso_Proceso> ingresoEgresoConErrores) { throw new UnsupportedOperationException("oracle double CrearIngresoEgreso.limpiarIngresosEgresos: not part of the double"); }
	
	
	//Quita del proceso las formas de pago que tuvieron problemas en la validación(y los movimientos asociados)
	public void limpiarFormasDePago(List<FormaDePago_Proceso> formaDePagoProceso, List<IngresoEgreso_Proceso> ingresoEgresoProceso ,List<FormaDePago_Proceso> formaDePagoConErrores) throws Exception 
	{ throw new UnsupportedOperationException("oracle double CrearIngresoEgreso.limpiarFormasDePago: not part of the double"); }	

	public CrearIngresoEgreso_Salida LimpiarSalida(CrearIngresoEgreso_Salida out ) { throw new UnsupportedOperationException("oracle double CrearIngresoEgreso.LimpiarSalida: not part of the double"); }

	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception { return cl.bcs.oracle.Doubles.answer("CrearIngresoEgreso", "call", new Object[]{con, jin}, JsonObject.class); }
	
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------
	//@Trace
	public CrearIngresoEgreso_Salida logica(Connection con, CrearIngresoEgreso_Entrada in) throws Exception { return cl.bcs.oracle.Doubles.answer("CrearIngresoEgreso", "logica", new Object[]{con, in}, CrearIngresoEgreso_Salida.class); }

	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------	
	
	public static void main(String[] args) throws Exception 
	{ throw new UnsupportedOperationException("oracle double CrearIngresoEgreso.main: not part of the double"); }

}