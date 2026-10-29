package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;
import cl.bcs.tesoreria.CrearComprobanteJava;
import cl.bcs.tesoreria.CrearComprobanteJava.ComprobanteGenerar_Entrada;
import cl.bcs.tesoreria.CrearComprobanteJava.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearComprobanteJava.IngresoEgresoGenerar_Entrada;



public class CrearComprobante  implements JavaCall
{

	// Log
	static final Logger logger = LogManager.getLogger(CrearComprobante.class);

	
	// Queries
	private final String SQL_INSERT_IngresoEgresoGenerar = "INSERT INTO \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar\"(\"NumeroReferencia\", \"IdIngresoEgreso\", \"TipoComprobante\") VALUES(:NumeroReferencia, :IdIngresoEgreso, :TipoComprobante)";	
	private final String SQL_INSERT_ComprobanteGenerar = "INSERT INTO \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar\"(\"NumeroReferencia\", \"IdComprobante\", \"TipoComprobante\") VALUES(:NumeroReferencia, :IdComprobante, :TipoComprobante)";
	private final String SQL_INSERT_FormaDePago = loadResourceToString("cl/bcs/tesoreria/sql/CrearComprobante_GBL_InsertFormaDePago.sql");
	private final String SQL_SELECT_ResultadoProceso = "SELECT \"Campo\",\"Id\",\"Servicio\",\"Resultado\" FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso\"";
	private final String SQL_SELECT_InformacionDetalle = "SELECT \"Codigo\",\"Descripcion\" FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_InformacionDetalle\"";
	private final String SQL_SELECT_AdvertenciasDetalle = "SELECT \"Codigo\",\"RequiereAutorizacion\",\"Descripcion\" FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle\"";
	private final String SQL_SELECT_ErroresDetalle = "SELECT \"Codigo\",\"Descripcion\" FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle\"";
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
    
    private final String SQL_DELETE_IngresoEgresoGenerar = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar\";";
    private final String SQL_DELETE_ComprobanteGenerar = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar\";";
    private final String SQL_DELETE_FormaDePago = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_FormaDePago\";";
	private final String SQL_DELETE_ResultadoProceso = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso\";";
	private final String SQL_DELETE_InformacionDetalle = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_InformacionDetalle\";";
	private final String SQL_DELETE_AdvertenciasDetalle = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle\";";
	private final String SQL_DELETE_SalidaErroresDetalle = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle\";";
	
	
	private final String SQL_DELETE_Comprobante = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante\";";
	private final String SQL_DELETE_ComprobanteDetalle = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalle\";";
	private final String SQL_DELETE_ComprobanteFormaDePago = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago\";";
	private final String SQL_DELETE_ComprobanteDetalleOrigen = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalleOrigen\";";
	private final String SQL_DELETE__TablaCodigosMensajes = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes\";";

	
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
		public Long 	IdIngresoEgreso;	// BIGINT		-- Movimientos que conformarán el comprobante
		public String 	TipoComprobante;	// NVARCHAR(6) 	-- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
	}
	
	//Generar comprobantes desde otros comprobantes	
	public static class ComprobanteGenerar_Entrada
	{
		public Long 	NumeroReferencia;   // BIGINT 		 -- Correlativo para relacionar los comprobantes que van a formar un nuevo comprobante
		public Long 	IdComprobante;   	// BIGINT        -- Comprobantes que conformará el comprobante 
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

	
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT	
	//-------------------------------------------------------------------------------------------	

	public static class CrearComprobante_Salida extends BcsPojo
	{
		public List<MensajeSalida>			 P_OUT_MENSAJES;
		public Long							 P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida> P_OUT_RESULTADOPROCESO;
		
		public CrearComprobante_Salida()
		{
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADOPROCESO =  new ArrayList<ResultadoProceso_Salida>();
		}
		
	}


	public static class ResultadoProceso_Salida extends BcsPojo
	{
		public String 	Campo;        // VARCHAR(40)   -- Ej: "IdComprobante"
		public Long   	Id;           // BIGINT        -- IdComprobante generado  Ej: 9999
		public String 	Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::CrearComprobante"
		public String	Resultado;    // VARCHAR(40)   -- Ej: "OK"
		public Long NumeroReferencia;
		
		public ResultadoProceso_Salida(){
			this.NumeroReferencia = 0L;
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
	// LOGICA DEL SERVICIO
	//------------------------------------------------------------------------------------------
	@Trace
	public CrearComprobante_Salida logica(Connection con, CrearComprobante_Entrada in) throws Exception {
		SQL sql = new SQL();
		CrearComprobante_Salida out = new CrearComprobante_Salida();
		HashMap<String,Object> parmsVacio = new HashMap<String,Object>();
		
		logger.info("== INICIO CREAR COMPROBANTE TESORERIA JAVA==");

			
		String usarCrearComprobanteJava = "N";
		try 
		{				
			usarCrearComprobanteJava = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "TESORERIA", "USARCREARCOMPROBANTEJAVA");
		}
		catch(Exception er) 
		{
			usarCrearComprobanteJava  = "N";
		}
			
			
			
		//*************************************
		//*** LLAMA A CREAR COMPROBANTE SQL ***
		//*************************************
		if (usarCrearComprobanteJava.equalsIgnoreCase("N"))
		{
			logger.debug("Inicio CrearComprobante seccion SQL");
			/// Crea las tablas temporales (No aplica para este sp)
			
			
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
				sql.batch(con,SQL_INSERT_IngresoEgresoGenerar, ingresoEgresoGenerar, IngresoEgresoGenerar_Entrada.class);
			}
		    
			if (in.p_ComprobanteGenerar.size() > 0)
			{
				ArrayList<ComprobanteGenerar_Entrada> comprobanteGenerar = (ArrayList<ComprobanteGenerar_Entrada>)in.p_ComprobanteGenerar;
				sql.batch(con,SQL_INSERT_ComprobanteGenerar, comprobanteGenerar, ComprobanteGenerar_Entrada.class);
			}
			
			if (in.p_FormaDePago.size() > 0)
			{
				ArrayList<FormaDePago_Entrada> formaDePago = (ArrayList<FormaDePago_Entrada>)in.p_FormaDePago;
				sql.batch(con,SQL_INSERT_FormaDePago, formaDePago, FormaDePago_Entrada.class);				
			}			
			
			

		    // Ejecuta el SP (call)
			logger.debug("EJECUTA CALL \"BSCL.Tesoreria.BD.Servicios::CrearComprobante\" ");
		    HashMap<String, Object> respuestaCC = sql.call(con, SQL_CALL_CrearComprobante, parmsIN, parmsOUT);
			logger.debug("SERVICIO EJECUTADO OK");
			
			
			/// Lee scalar de salida  (no aplica para este sp)			
			///out.p_out_ErroresContador      = (Long) respuestaLC.get("p_out_ErroresContador");
			

			
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
			
		} // if (usarCrearComprobanteJava.equalsIgnoreCase("N"))
			
			
		
		
			
		//****************************************
		//*** LLAMADA A CREAR COMPROBANTE JAVA ***
		//****************************************
		if (usarCrearComprobanteJava.equalsIgnoreCase("S"))
		{
				logger.debug("Inicio CrearComprobante seccion Java");
				CrearComprobanteJava servicioCrearComprobanteJava = new CrearComprobanteJava();				
				cl.bcs.tesoreria.CrearComprobanteJava.CrearComprobante_Entrada entradaCrearComprobanteJava = new cl.bcs.tesoreria.CrearComprobanteJava.CrearComprobante_Entrada();
				cl.bcs.tesoreria.CrearComprobanteJava.CrearComprobante_Salida salidaCrearComprobanteJava = new cl.bcs.tesoreria.CrearComprobanteJava.CrearComprobante_Salida();
				
				
				entradaCrearComprobanteJava.p_IdEmpresa = in.p_IdEmpresa;
				entradaCrearComprobanteJava.p_IdUsuario = in.p_IdUsuario;
				entradaCrearComprobanteJava.p_IdModulo = in.p_IdModulo;   
				entradaCrearComprobanteJava.p_SimularOperacion = in.p_SimularOperacion;
				entradaCrearComprobanteJava.p_UsaReglasNegocio = in.p_UsaReglasNegocio;     
				entradaCrearComprobanteJava.p_FechaMovimiento = in.p_FechaMovimiento; 
				entradaCrearComprobanteJava.p_IdCaja = in.p_IdCaja;	
				entradaCrearComprobanteJava.p_GenerarComprobanteDesdeMovimientos = in.p_GenerarComprobanteDesdeMovimientos;
				entradaCrearComprobanteJava.p_GenerarComprobanteDesdeComprobantes = in.p_GenerarComprobanteDesdeComprobantes;
				entradaCrearComprobanteJava.p_EsIngresoAutomatico = in.p_EsIngresoAutomatico;
				

				List<cl.bcs.tesoreria.CrearComprobanteJava.IngresoEgresoGenerar_Entrada> lstIngresoEgresoGenerar = new ArrayList<cl.bcs.tesoreria.CrearComprobanteJava.IngresoEgresoGenerar_Entrada>();
				cl.bcs.tesoreria.CrearComprobanteJava.IngresoEgresoGenerar_Entrada objIngresoEgresoGenerar;				
				for (IngresoEgresoGenerar_Entrada ieg: in.p_IngresoEgresoGenerar)
				{	
					objIngresoEgresoGenerar = new cl.bcs.tesoreria.CrearComprobanteJava.IngresoEgresoGenerar_Entrada();
					objIngresoEgresoGenerar.NumeroReferencia 	= ieg.NumeroReferencia;
					objIngresoEgresoGenerar.IdIngresoEgreso 	= ieg.IdIngresoEgreso;
					objIngresoEgresoGenerar.TipoComprobante  	= ieg.TipoComprobante;	
					lstIngresoEgresoGenerar.add(objIngresoEgresoGenerar);
				}
				

				List<cl.bcs.tesoreria.CrearComprobanteJava.ComprobanteGenerar_Entrada> lstComprobanteGenerar = new ArrayList<cl.bcs.tesoreria.CrearComprobanteJava.ComprobanteGenerar_Entrada>();
				cl.bcs.tesoreria.CrearComprobanteJava.ComprobanteGenerar_Entrada objComprobanteGenerar;				
				for (ComprobanteGenerar_Entrada cge: in.p_ComprobanteGenerar)
				{
					objComprobanteGenerar = new cl.bcs.tesoreria.CrearComprobanteJava.ComprobanteGenerar_Entrada();
					objComprobanteGenerar.NumeroReferencia 	= cge.NumeroReferencia;
					objComprobanteGenerar.IdComprobante 	= cge.IdComprobante;
					objComprobanteGenerar.TipoComprobante  	= cge.TipoComprobante;	
					lstComprobanteGenerar.add(objComprobanteGenerar);
				}
				

				List<cl.bcs.tesoreria.CrearComprobanteJava.FormaDePago_Entrada> lstFormaDePagoGenerar = new ArrayList<cl.bcs.tesoreria.CrearComprobanteJava.FormaDePago_Entrada>();
				cl.bcs.tesoreria.CrearComprobanteJava.FormaDePago_Entrada objFormaDePagoGenerar;				
				for (FormaDePago_Entrada fp: in.p_FormaDePago)
				{
					objFormaDePagoGenerar = new cl.bcs.tesoreria.CrearComprobanteJava.FormaDePago_Entrada();								
					objFormaDePagoGenerar.NumeroReferencia = fp.NumeroReferencia;					
					objFormaDePagoGenerar.IdMedioDePago = fp.IdMedioDePago;
					objFormaDePagoGenerar.IdModalidadDePago = fp.IdModalidadDePago;
					objFormaDePagoGenerar.IdMoneda = fp.IdMoneda;
					objFormaDePagoGenerar.Monto = fp.Monto;
					objFormaDePagoGenerar.IdCuentaBancariaCorredora = fp.IdCuentaBancariaCorredora;	
					objFormaDePagoGenerar.IdTarjetaBancariaCorredora = fp.IdTarjetaBancariaCorredora;
					objFormaDePagoGenerar.IdChequera = fp.IdChequera;
					objFormaDePagoGenerar.NumeroDocumentoCorredora = fp.NumeroDocumentoCorredora;
					objFormaDePagoGenerar.FechaDocumentoPagoCorredora = fp.FechaDocumentoPagoCorredora;	
					objFormaDePagoGenerar.IdCuentaBancariaSocio = fp.IdCuentaBancariaSocio;
					objFormaDePagoGenerar.IdBancoSocio = fp.IdBancoSocio;
					objFormaDePagoGenerar.NombreCuentaBancariaSocio = fp.NombreCuentaBancariaSocio;
					objFormaDePagoGenerar.NumeroCuentaBancariaSocio = fp.NumeroCuentaBancariaSocio;
					objFormaDePagoGenerar.IdTipoCuentaBancariaSocio = fp.IdTipoCuentaBancariaSocio;		
					objFormaDePagoGenerar.NombreTipoCuentaBancariaSocio = fp.NombreTipoCuentaBancariaSocio;
					objFormaDePagoGenerar.IdTarjetaBancariaSocio = fp.IdTarjetaBancariaSocio;
					objFormaDePagoGenerar.IdTipoTarjetaBancariaSocio = fp.IdTipoTarjetaBancariaSocio;
					objFormaDePagoGenerar.NombreTipoTarjetaBancariaSocio = fp.NombreTipoTarjetaBancariaSocio;		
					objFormaDePagoGenerar.NumeroTarjetaBancariaSocio = fp.NumeroTarjetaBancariaSocio;
					objFormaDePagoGenerar.NumeroSerieSocio = fp.NumeroSerieSocio;
					objFormaDePagoGenerar.NumeroDocumentoSocio = fp.NumeroDocumentoSocio;
					objFormaDePagoGenerar.FechaDocumentoPagoSocio = fp.FechaDocumentoPagoSocio;					
					lstFormaDePagoGenerar.add(objFormaDePagoGenerar);					
				}
							
				entradaCrearComprobanteJava.p_IngresoEgresoGenerar = lstIngresoEgresoGenerar;
				entradaCrearComprobanteJava.p_ComprobanteGenerar = lstComprobanteGenerar;
				entradaCrearComprobanteJava.p_FormaDePago = lstFormaDePagoGenerar;
				
				salidaCrearComprobanteJava = servicioCrearComprobanteJava.logica(con, entradaCrearComprobanteJava);
				
				// Asigna Salida				
				out.P_OUT_MENSAJES = salidaCrearComprobanteJava.P_OUT_MENSAJES;
				

				
				List<ResultadoProceso_Salida> lstResultadoProceso = new ArrayList<ResultadoProceso_Salida>();
				ResultadoProceso_Salida objResultadoProceso;
				
				for (cl.bcs.tesoreria.CrearComprobanteJava.ResultadoProceso_Salida resultado: salidaCrearComprobanteJava.P_OUT_RESULTADOPROCESO)
				{	
					objResultadoProceso = new ResultadoProceso_Salida();
					objResultadoProceso.Campo = resultado.Campo;
					objResultadoProceso.Id = resultado.Id;
					objResultadoProceso.Servicio = resultado.Servicio;
					objResultadoProceso.Resultado = resultado.Resultado;
					objResultadoProceso.NumeroReferencia = resultado.NumeroReferencia;
					lstResultadoProceso.add(objResultadoProceso);
				}
				
				out.P_OUT_RESULTADOPROCESO = lstResultadoProceso;
				
		} // if (usarCrearComprobanteJava.equalsIgnoreCase("S"))

			
			
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);

		logger.info("== TERMINO CREAR COMPROBANTE TESORERIA JAVA ==");
		

		
		return out;

	}
	
	
	
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
				IngresoEgresoGenerar_Entrada _ingresoEgresoGenerar = new IngresoEgresoGenerar_Entrada(); 
				
				_ingresoEgresoGenerar.NumeroReferencia = 1L;
				_ingresoEgresoGenerar.IdIngresoEgreso  = 10438L;   // BIGINT
				_ingresoEgresoGenerar.TipoComprobante  = "";       // NVARCHAR(6)   -- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
				
				IngresoEgresoGenerar.add(_ingresoEgresoGenerar);
				entrada.p_IngresoEgresoGenerar = IngresoEgresoGenerar;
				
				//Comprobantes para generar comprobantes
				//List<ObjComprobanteGenerar> ComprobanteGenerar = new ArrayList<>();				
				//ObjComprobanteGenerar _comprobanteGenerar = new ObjComprobanteGenerar();
				//_comprobanteGenerar.IdComprobante = 999L;
				
				//Forma de Pago del Comprobante (si no se especifica toma los de los movimientos de origen)
				List<FormaDePago_Entrada> FormaDePago = new ArrayList<>();				
				FormaDePago_Entrada _formaDePago = new FormaDePago_Entrada();
				_formaDePago.IdMedioDePago = "TRA";				
				_formaDePago.Monto = BigDecimal.valueOf(2000000);
				_formaDePago.IdCuentaBancariaCorredora = 21L;
				_formaDePago.IdModalidadDePago = "MODDEP";
				_formaDePago.IdMoneda = "CLP";
				FormaDePago.add(_formaDePago);
				entrada.p_FormaDePago = FormaDePago;
				
				
				System.out.println("ENTRADA SERVICIO JAVA:" + entrada.toString());

				
				CrearComprobante crearComprobante = new CrearComprobante();
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
