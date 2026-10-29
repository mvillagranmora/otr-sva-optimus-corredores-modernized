package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.BcsPojo;
import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import com.google.gson.JsonObject;
import java.util.Date;

/**
 * ORACLE FAKE of legacy cl.bcs.tesoreria.CrearIngresoEgreso (cl/bcs/tesoreria/CrearIngresoEgreso.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Used by ProcesarClearingFinanciero, RecaudacionBCP, CargaMasivaIngresoEgresoDAPFFMM, ProcesarDistribucion, ProcesarCargaMasiva.
 */
public class CrearIngresoEgreso implements JavaCall {

	// legacy cl/bcs/tesoreria/CrearIngresoEgreso.java:83-111 (verbatim)
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

	// legacy cl/bcs/tesoreria/CrearIngresoEgreso.java:116-189 (verbatim)
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

	// legacy cl/bcs/tesoreria/CrearIngresoEgreso.java:193-226 (verbatim)
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

	// legacy cl/bcs/tesoreria/CrearIngresoEgreso.java:234-251 (verbatim)
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

	// legacy cl/bcs/tesoreria/CrearIngresoEgreso.java:254-263 (verbatim)
	public static class IngresoEgresoGenerado_Salida 
	{
		public Long 	NumeroReferencia;			// BIGINT
		public Long 	IdIngresoEgreso; 			// BIGINT
		public String 	IdTipoMovimiento;			// NVARCHAR(6)
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/tesoreria/CrearIngresoEgreso.java:266-277 (verbatim)
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

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }


	/** FAKE of the legacy logica: records its input (Hooks.registrar) and answers the script colaboradores.<Servicio> of the case (see oracle/Hooks.java); nothing of the legacy behaviour is reproduced. */
	public CrearIngresoEgreso_Salida logica(Connection con, CrearIngresoEgreso_Entrada in) throws Exception {
		Hooks.registrar("CrearIngresoEgreso", in);
		return Hooks.salida("CrearIngresoEgreso", CrearIngresoEgreso_Salida.class);
	}

}
