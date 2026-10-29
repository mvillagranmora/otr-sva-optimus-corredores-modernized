package cl.bcs.tesoreria;


import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cl.bcs.utils.BcsPojo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoGenerado;
import cl.bcs.ordenes.pojo.IndicadoresPortafolio;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.EntradaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.PortafolioAConsultar;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.SalidaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Entrada;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Salida;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.CuentaBanco_Entrada;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;

public class LiquidarComprobante  implements JavaCall   // En SQL se llama LiquidarComprobantes
{

	// Log
	private static final Logger logger = LogManager.getLogger(LiquidarComprobante.class);
	
	// Queries	
	private final String SQL_INSERT_COMPROBANTES_A_LIQUIDAR 	= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_ComprobantesALiquidar.sql");
	private final String SQL_INSERT_COMPROBANTE             	= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_InsertComprobante.sql");
	private final String SQL_SELECT_COMPROBANTE             	= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_SelectComprobante.sql");	
	private final String SQL_INSERT_COMPROBANTE_DETALLE     	= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_InsertComprobanteDetalle.sql");
	private final String SQL_SELECT_COMPROBANTE_DETALLE     	= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_SelectComprobanteDetalle.sql");
	private final String SQL_INSERT_FORMASDEPAGO_A_LIQUIDAR 	= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_InsertFormasDepagoALiquidar.sql");
	private final String SQL_INSERT_COMPROBANTE_FORMA_DE_PAGO 	= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_InsertComprobanteFormaDepago.sql");	
	private final String SQL_SELECT_COMPROBANTE_FORMA_DE_PAGO   = loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_SelectComprobanteFormaDePago.sql");
	private final String SQL_LIQUIDAR_FORMAS_DE_PAGO 			= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_LiquidarFormasDepago.sql");
	private final String SQL_LIQUIDAR_COMPROBANTE 				= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_LiquidarComprobante.sql");	
	private final String SQL_LIQUIDAR_COMPROBANTE_DETALLE		= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_LiquidarComprobanteDetalle.sql");
	private final String SQL_INSERT_COMPROBANTES_MUEVEN_CTA_INVERSION    = loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_InsertComprobantesMuevenCuentaInversion.sql");
	private final String SQL_SELECT_CARGO_ABONO_A_GENERAR       = loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_SelectCargoAbonoAGenerar.sql");
	private final String SQL_SELECT_CUENTA_BANCO_A_ACTUALIZAR   = loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_SelectCuentaBancoAActualizar.sql");
	private final String SQL_UPDATE_COMPROBANTE_REQUIERE_AUTORIZAR		 = loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_UpdateComprobanteRequiereAutorizar.sql");
	private final String SQL_DELETE_COMPROBANTE					= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_DeleteComprobante.sql");
	private final String SQL_DELETE_COMPROBANTE_DETALLE			= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_DeleteComprobanteDetalle.sql");
	private final String SQL_DELETE_COMPROBANTE_FORMA_DE_PAGO	= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_DeleteComprobanteFormaDePago.sql");
	private final String SQL_DELETE_COMPROBANTES_A_LIQUIDAR		= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_DeleteComprobantesALiquidar.sql");
	private final String SQL_DELETE_FORMASDEPAGO_A_LIQUIDAR		= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_DeleteFormasDepagoALiquidar.sql");
	private final String SQL_SELECT_DATOS_PORTAFOLIO			= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_SelectDatosPortafolio.sql");
	private final String SQL_SELECT_CONCEPTO_TESORERIA			= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_SelectConceptoTesoreria.sql");
	private final String SQL_SELECT_REQUISITO_OPERATIVO			= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_SelectRequisitoOperativo.sql");
	private final String SQL_DELETE_COMPROBANTES_MUEVEN_CI 		= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_DeleteComprobantesMuevenCuentaInversion.sql");

	
	//Para revisar contenido de tabla global que mueve cuenta inversion
	private final String SQL_SELECT_ComprobantesMuevenCuentaInversion	= "select * from \"BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion\"";
	
	private final String SQL_SELECT_MONEDA_COMPROBANTE_DETALLE   = "SELECT \"IdComprobante\",\"IdMoneda\" FROM \"BSCL.Tesoreria.BD::TES.ComprobanteDetalle\" WHERE \"IdComprobante\" in (:IdComprobante);";//loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_SelectMoneda.sql");
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO 				= loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectIndicadoresPortafolio.sql");

	List<String> listAutorizaciones = new ArrayList<String>();
	
    //-------------------------------------------------------------------------------------------
	// CLASES DE ENTRADA
	//-------------------------------------------------------------------------------------------
   
	public static class LiquidarComprobante_Entrada 
	{	
		public Long		p_IdEmpresa;  				 
		public Long		p_IdUsuario; 				   
		public String 	p_IdModulo; 				
		public String 	p_SimularOperacion = "N"; 	 
		public String 	p_UsaReglasNegocio;			
	    public String 	p_FechaMovimiento; 	// Corresponde a Fecha Sistema	
	    
	    //Uso exclusivo Aporte CSC
	    public String	FolioCSC = "";
	    // En SQL se llama "p_TablaComprobante"
	    public List<ComprobanteLiquidar_Entrada> p_ComprobanteLiquidar;  // Comprobantes a Liquidar
	     
	    // En SQL se llama "p_TablaComprobanteFormaDePago" 
	    public List<FormaDePagoLiquidar_Entrada> p_FormaDePagoLiquidar;  // Formas de Pago a Liquidar > Si no especifica la FP a liquidar, se asume que se deben liquidar todas las FP de ese comprobante 
	    		
	    public LiquidarComprobante_Entrada()
	    {
	    	p_SimularOperacion    = "N";
	    	p_ComprobanteLiquidar = new ArrayList<ComprobanteLiquidar_Entrada>();
	    	p_FormaDePagoLiquidar = new ArrayList<FormaDePagoLiquidar_Entrada>();
	    }
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	//Comprobantes a Liquidar
	public static class ComprobanteLiquidar_Entrada
	{
		public Long 	IdComprobante;  		
		public String 	IdTipoMovimiento;  		// MOVING / MOVEGR
		public String 	AfectaCuentaInversion;  // CLI: Cliente / COR: Corredora / VACIO: Ambos
	}
	
	// Formas de Pago a Liquidar - Si no especifica la FP a liquidar para un comprobante, se asume que se deben liquidar todas las FP de ese comprobante	
	public static class FormaDePagoLiquidar_Entrada
	{
		public Long 	IdComprobante;
		public Long 	IdComprobanteFormaDePago;
	}	
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES DE SALIDA
	//-------------------------------------------------------------------------------------------
	
	public static class LiquidarComprobante_Salida 
	{	
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida>	P_OUT_RESULTADOPROCESO;
		
		public LiquidarComprobante_Salida (){
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			P_OUT_RESULTADOPROCESO 		= new ArrayList<ResultadoProceso_Salida>();
		};
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	public static class ResultadoProceso_Salida
	{
		public String 	Campo;        // VARCHAR(40)   -- Ej: "IdComprobante"
		public Long   	Id;           // BIGINT        -- IdComprobante liquidado  Ej: 9999
		public String 	Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes"
		public String	Resultado;    // VARCHAR(40)   -- Ej: "OK"
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	
	
	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS  DE PROCESAMIENTO
	//-------------------------------------------------------------------------------------------

	public static class Comprobante
	{
		public Long   		IdInterno;
		public Long   		IdComprobanteInformado;
		public String 		IdTipoMovimientoInformado;
		public Long   		IdComprobante;
		public String 		IdTipoMovimiento;	
		public BigDecimal	MontoComprobante;
		public Long   		CantidadFormaDePago;
		public BigDecimal   MontoTotalFormaDePago;
	    public String 		IdEstadoComprobante;
	    public String 		AfectaCuentaInversion;
	   	public Long   		IdPortafolioTitularPago;
	   	public String 		IdConcepto;
	   	public String		FechaMovimiento;    //Fecha de sistema en que se liquida 
	   	public Long   		IdPersona;	
	   	public String 		NombrePersona;
		public String 		TitularPago;
		public String 		IdTipoPersonaTitularPago;
		public Long   		IdPersonaTitularPago;
		public String 		FechaLiquidacion;
		public Long   		IdCaja;
		
		//Para validaciones
		public long 				errores 		= 0;
		public long 				advertencias 	= 0;
		public long 				autorizaciones 	= 0;
		public long 				ok 				= 0;	
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}

	public static class ComprobanteRequiereAutorizar
	{
		public Long   	IdComprobante;
		public String 	IdTipoMovimiento;	
		public String 	AfectaCuentaInversion;
		public Long   	IdComprobanteFormaDePago;
		public Long		IdUsuario;
		public String 	UsaReglasNegocio;
		public String	FechaMovimiento;
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class RequisitosOperativosLiquidacion{
		public String IdCondicionOperativa;
		public String IdTipoEtapa;
		public String IdTipoCanalIngreso;
		public String IdTipoComportamiento;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		   	
	public static class ComprobanteDetalle
	{
		public Long   		IdInterno;
		public Long   		IdComprobanteInformado;
		public Long   		IdComprobanteDetalle;
	   	public String 		IdTipoMovimiento;
	   	public String 		IdTipoComprobanteOrigen;
	   	public Long   		SecuenciaRelacionada;
	   	public int   		PendienteDeLiquidar;
	   	public String 		IdConcepto;
	   	public BigDecimal	Monto;	   	
	 
		//Para validaciones
		public long 				errores 		= 0;
		public long 				advertencias 	= 0;
		public long 				autorizaciones 	= 0;
		public long 				ok 				= 0;	
		
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}

	public static class ComprobanteDetalleMoneda
	{
		public Long   		IdComprobante;
	   	public String 		IdMoneda;

		//Para validaciones
		public long 				errores 		= 0;
		public long 				advertencias 	= 0;
		public long 				autorizaciones 	= 0;
		public long 				ok 				= 0;	
		
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class ComprobanteFormaDePago
	{
	   	public Long   		IdInterno;
	   	public Long   		IdComprobanteInformado;
	   	public Long   		IdComprobanteFormaDePagoInformado;
	   	public Long   		IdComprobanteFormaDePago;
	   	public Long   		IdCuentaBancariaCorredora;
		public String 		IdEstadoFormaDePago;
		public String 		IdEstadoComprobante;
		public String 		TipoMedioDePago;
		public String 		LiquidacionParcialEnviaACTA;   //es un parametro de tesoreria
		public String 		EstadosPosibleTipoMovimientoMedioDePago;
		public String 		AfectaSaldoEnEstado;
		public String 		IdMoneda;
		public BigDecimal	Monto;
		public Long   		IdDeposito;
		public String 		IdEstadoDepositoDetalle;
		public java.sql.Date FechaLiquidacion; 
		public Long   		DiasRetencion;
		public String 		IdMedioDePago;
		public Long   		IdCaja;
		public String		FechaMovimiento;  //Fecha de sistema en que se liquida 
		public Long   		IdUsuario;
		
	
		public String		NumeroCuentaBancariaCorredora;
		public String		IdTipoMovimiento;
		public String		IdModalidadDePago;		
		public Long         IdCuentaBancariaSocio;
		public String		NumeroCuentaBancariaSocio;
	
		public Long         IdBancoSocio;
		
		public String		TipoComprobante;
		
		public String		ValidarFormaDePagoSinCertificar = "S";   // Para recudacion APV no se valida	 		
		public String		ValidarFormaDePagoSinDepositar  = "S";   // Para recudacion APV no se valida
		
		//Para validaciones
		public long 				errores 		= 0;
		public long 				advertencias 	= 0;
		public long 				autorizaciones 	= 0;
		public long 				ok 				= 0;	

		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ParametrosConfiguracion
	{	

		public String LiquidacionParcialEnviaACTA;
		
		public String toString(){
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
	

	public static class ComprobantesMuevenCuentaInversion
	{	

		public Long   	IdComprobante;
		public Long   	IdComprobanteDetalle;	 		
		public Long   	IdComprobanteFormaDePago;		
		public String 	AfectaCuentaInversion;
		public String 	FechaMovimiento;
		public String 	IdConcepto;
		public Long   	IdPersona;
		public String 	NombrePersona;
		public String 	TitularPago;
		public String 	IdTipoPersonaTitularPago;
		public Long   	IdPersonaTitularPago;	    
		public String 	IdMoneda;			    
		public BigDecimal Monto;		  
		public Long   	IdPortafolio;
		public String 	IdTipoMovimientoCtaInvCliente;   
		public String 	IdTipoMovimientoCtaInvCorredora;		 
		public String 	IdTipoComprobante;
		public String 	IdTipoMovimientoAsociado;
		public Long   	IdMovimientoAsociado; 

		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConceptoTesoreria extends BcsPojo {
		public String IdConcepto;
		public String IndicadorNoValidaSaldoCaja;

		public ConceptoTesoreria() {
			IndicadorNoValidaSaldoCaja = "N";
		}
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODOS UTILITARIOS
	//-------------------------------------------------------------------------------------------	

	private HashMap<String,RequisitosOperativosLiquidacion> ObtieneRequisitosOperativos(Connection con, Long idEmpresa, String IdCondicionOperativa)
			throws Exception {
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa", idEmpresa);
		List<RequisitosOperativosLiquidacion> requisitosOperativos = sql.query(con, SQL_SELECT_REQUISITO_OPERATIVO, params, RequisitosOperativosLiquidacion.class);
		HashMap<String,RequisitosOperativosLiquidacion> requisitosOperativosHash = new HashMap<>();
		for (RequisitosOperativosLiquidacion requisito : requisitosOperativos) {
			requisitosOperativosHash.put(requisito.IdCondicionOperativa, requisito);
		}
		
		return requisitosOperativosHash;
	}

	private void ProcesaRequisitoComprobante(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, Comprobante com, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("IdComprobante", com.IdComprobanteInformado)
					.add("Monto", com.MontoComprobante)
				    .msg();
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(com.IdComprobanteInformado,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				com.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(com.IdComprobanteInformado,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				listAutorizaciones.add(codMsg);
				com.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(com.IdComprobanteInformado,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				com.advertencias++;
			}
			else 
			{
				com.ok++;
			}
		}
		else 
		{
			com.ok++;
		}		
	}
	
	

	private void ProcesaRequisitoComprobanteFormaDePago(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, ComprobanteFormaDePago comFP, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("IdComprobante", comFP.IdComprobanteInformado)
					.add("IdComprobanteFormaDePago", comFP.IdMedioDePago)	
				    .msg();
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(comFP.IdComprobanteInformado,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				comFP.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(comFP.IdComprobanteInformado,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				comFP.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(comFP.IdComprobanteInformado,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				comFP.advertencias++;
			}
			else 
			{
				comFP.ok++;
			}
		}
		else 
		{
			comFP.ok++;
		}		
	}
	
	
	
	private void ProcesaRequisitoComprobanteDetalle(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, ComprobanteDetalle comDet, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("IdComprobante", comDet.IdComprobanteInformado)
				    .msg();
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(comDet.IdComprobanteInformado,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				comDet.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(comDet.IdComprobanteInformado,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				comDet.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(comDet.IdComprobanteInformado,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				comDet.advertencias++;
			}
			else 
			{
				comDet.ok++;
			}
		}
		else 
		{
			comDet.ok++;
		}		
	}
	
	private void ProcesaRequisitoCargoAbono(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, ObjCargoAbonoAGenerar cargoAbono, DatoPortafolio datosPortafolio, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("IdComprobante", cargoAbono.NumeroCargoAbono)
					.add("Monto", cargoAbono.Monto)
					.add("IdMoneda", cargoAbono.IdMoneda)
					.add("DocumentoIdentidad", datosPortafolio.DocumentoIdentidad)
					.add("NombreCompleto","IdPortafolio " +datosPortafolio.IdPortafolio+" " + datosPortafolio.NombreCompleto )
				    .msg();
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(cargoAbono.NumeroCargoAbono,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
			}
		}	
	}
	
	public static class DatoPortafolio{
		public Long IdPortafolio;
		public String NombreCompleto;
		public String DocumentoIdentidad;
	}
	
	
	
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------
	
	public LiquidarComprobante_Salida logica(Connection con, LiquidarComprobante_Entrada in) throws Exception {
				
		logger.info("== INICIO LIQUIDAR COMPROBANTE ==");
		
		SQL sql = new SQL();
		LiquidarComprobante_Salida out = new LiquidarComprobante_Salida();
		HashMap<String,Object> param = new HashMap<>();
		List<Comprobante> comprobantesAProcesar = in.p_ComprobanteLiquidar.parallelStream().map(comprobanteEntrada->{
			Comprobante comprobante = new Comprobante();
			comprobante.IdComprobante = comprobanteEntrada.IdComprobante;
			return comprobante;
		}).collect(Collectors.toList());
		
		
		// --------------------------------------------------------------------------------------------------
		// OBTIENE DE PARAMETROS DE CONFIGURACION DE LIQUIDACION	
		

		//Obtiene parametro "LIQUIDACION PARCIAL DE COMPROBANTE ENVIA A CUENTA INVERSION"
		String liquidacionParcialEnviaACTA = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "TESORERIA","LIQUIDACIONPARCIALENVIAACTA");
		String permiteCuentaBancariaSocioNegocio = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "TESORERIA","PERMITECUENTABANCARSOCIONEGOCIO");

		//Obtiene requisito operativo para liquidacion en fecha diferente a la de sistema
		HashMap<String,RequisitosOperativosLiquidacion> requisitosOperativosHash = ObtieneRequisitosOperativos(con, in.p_IdEmpresa, "LIQUIDACOMPTES");
		HashMap<String,String> requisitosComportamiento = new HashMap<String,String>();
		String requiereAutorizacion = "";
		if (requisitosOperativosHash.get("LIQFECHADIS")!=null) {
			requiereAutorizacion = (requisitosOperativosHash.get("LIQFECHADIS").IdEstado).equals("ACT")?requisitosOperativosHash.get("LIQFECHADIS").IdTipoComportamiento:"";
			requisitosComportamiento.put("LIQFECHADIS", requiereAutorizacion);	
		}
		if (requisitosOperativosHash.get("LIQSALDONEG")!=null) {
			requiereAutorizacion = (requisitosOperativosHash.get("LIQSALDONEG").IdEstado).equals("ACT")?requisitosOperativosHash.get("LIQSALDONEG").IdTipoComportamiento:"";
			requisitosComportamiento.put("LIQSALDONEG", requiereAutorizacion);
		}

		try {
			// --------------------------------------------------------------------------------------------------
			// OBTIENE DATOS DE COMPROBANTES A LIQUIDAR		
		
			//Guarda en tabla global datos de entrada de comprobantes a liquidar
			sql.batch(con, SQL_INSERT_COMPROBANTES_A_LIQUIDAR, in.p_ComprobanteLiquidar, ComprobanteLiquidar_Entrada.class);
			
			//Obtiene y Guarda en tabla global mas datos de los comprobantes a liquidar
			List<Comprobante> lstComprobante = new ArrayList<Comprobante>();
			Comprobante objComprobante = new Comprobante();
			objComprobante.FechaMovimiento = in.p_FechaMovimiento;
			lstComprobante.add(objComprobante);
			sql.batch(con, SQL_INSERT_COMPROBANTE, lstComprobante, Comprobante.class);
			
			//Visualizar los datos de los comprobantes a liquidar
			List<Comprobante> comprobante = sql.query(con, SQL_SELECT_COMPROBANTE, param, Comprobante.class);
			
			//Agregar Log Generar Comprobante TESORERIA
			AgregarLog agregarLog = new AgregarLog();
			InAgregarLog entradaLog = new InAgregarLog();

			if (comprobante != null && !comprobante.isEmpty()) {		
				for(Comprobante datosLog : comprobante) {

					//Construir entradas log
					InAgregarLog.Log headLog = new InAgregarLog.Log();
					headLog.NombreProceso = "Liquidar Comprobante Tesoreria";
					headLog.IdCabeceraLog = datosLog.IdComprobante != null ? datosLog.IdComprobante : 0L;
					headLog.IdTipoLog = "INGRESO";
					headLog.IdModulo = in.p_IdModulo;
					headLog.IdTipoRegistro = "LIQUIDACION_TESORERIA";
					headLog.IdUsuario = in.p_IdUsuario;
					headLog.IdEmpresa = in.p_IdEmpresa;
					headLog.FechaSistemaLog = datosLog.FechaLiquidacion!= null ? datosLog.FechaLiquidacion : "";
					headLog.IdUsuarioAtendido = in.p_IdUsuario;
					headLog.IdPersona = datosLog.IdPersonaTitularPago != null ? datosLog.IdPersonaTitularPago : 0L;
					headLog.IdPortafolio = datosLog.IdPortafolioTitularPago != null ? datosLog.IdPortafolioTitularPago : 0L;
					headLog.Folio = datosLog.IdComprobante != null ? datosLog.IdComprobante : 0L;
					headLog.Monto = datosLog.MontoComprobante != null ? datosLog.MontoComprobante : BigDecimal.ZERO;
					
					entradaLog.p_Log.add(headLog);
				}	
				//llamar al log
				agregarLog.logica(con, entradaLog);
			}

			// --------------------------------------------------------------------------------------------------
			// OBTIENE DATOS DE COMPROBANTES DETALLE		
			
			//Obtiene y Guarda en tabla global datos de los comprobantes detalles a liquidar
			List<ComprobanteDetalle> lstComprobanteDetalle = new ArrayList<ComprobanteDetalle>();		
			ComprobanteDetalle objComprobanteDetalle = new ComprobanteDetalle();
			objComprobanteDetalle.IdInterno = 1L; //solo para que entre un parametro sino no hace insert
			lstComprobanteDetalle.add(objComprobanteDetalle);
			sql.batch(con, SQL_INSERT_COMPROBANTE_DETALLE, lstComprobanteDetalle, ComprobanteDetalle.class);
			

			//Visualizar los datos de los comprobantes detalles a liquidar
			List<ComprobanteDetalle> comprobanteDetalle = sql.query(con, SQL_SELECT_COMPROBANTE_DETALLE, param, ComprobanteDetalle.class);
			
			
			
			// --------------------------------------------------------------------------------------------------
			// OBTIENE DATOS DE COMPROBANTE FORMAS DE PAGO
			
			//Guarda en tabla global datos de entrada de formas de pago a liquidar
			sql.batch(con, SQL_INSERT_FORMASDEPAGO_A_LIQUIDAR, in.p_FormaDePagoLiquidar, FormaDePagoLiquidar_Entrada.class);

			
			//Obtiene y Guarda en tabla global mas datos de comprobante formas de pago
			List<ComprobanteFormaDePago> lstComprobanteFormaDePago = new ArrayList<ComprobanteFormaDePago>();
			ComprobanteFormaDePago objComprobanteFormaDePago = new ComprobanteFormaDePago();
			objComprobanteFormaDePago.FechaMovimiento = in.p_FechaMovimiento;
			lstComprobanteFormaDePago.add(objComprobanteFormaDePago);
			sql.batch(con, SQL_INSERT_COMPROBANTE_FORMA_DE_PAGO, lstComprobanteFormaDePago, ComprobanteFormaDePago.class);
			
			
			//Visualizar los datos de las formas de pago de los comprobantes a liquidar
			param.clear();
			param.put("IdEmpresa", in.p_IdEmpresa);
			List<ComprobanteFormaDePago> comprobanteFormaDePago = sql.query(con, SQL_SELECT_COMPROBANTE_FORMA_DE_PAGO, param, ComprobanteFormaDePago.class);
			

			HashMap<String,Object> inDatos = new HashMap<String,Object>();
			Map<Long, List<String>> autorizacionesMap = new HashMap<Long, List<String>>();
			for (Comprobante com: comprobante) {
				inDatos.put("IdComprobante", com.IdComprobante);
			}
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			List<ComprobanteDetalleMoneda> moneda = sql.query(con, SQL_SELECT_MONEDA_COMPROBANTE_DETALLE, inDatos, ComprobanteDetalleMoneda.class);
			
			HashMap<Long,String> monedas = new HashMap<Long,String>();
			for(ComprobanteDetalleMoneda monedaSaldo : moneda){
				monedas.put(monedaSaldo.IdComprobante, monedaSaldo.IdMoneda);
			}
			
			List<PortafolioAConsultar> portafoliosAConsultar = new ArrayList();
			for(Comprobante comprobarSaldo : comprobante){
				
				if (comprobarSaldo.IdPortafolioTitularPago != null && comprobarSaldo.IdPortafolioTitularPago != 0) {
					PortafolioAConsultar portafolio = new PortafolioAConsultar();
					portafolio.IdMoneda = monedas.get(comprobarSaldo.IdComprobante);
					portafolio.IdPortafolio = comprobarSaldo.IdPortafolioTitularPago;
					portafoliosAConsultar.add(portafolio);
				}
	
			}
			
			HashMap<Long,BigDecimal> saldos = new HashMap<Long,BigDecimal>();
			
			if (portafoliosAConsultar.size() > 0) {
				
				ConsultarFlujoCaja llamadoFlujoCajaSaldo = new ConsultarFlujoCaja();
				EntradaConsultarFlujoCaja entradaFlujoCajaSaldo = new EntradaConsultarFlujoCaja();
				entradaFlujoCajaSaldo.p_IdEmpresa = in.p_IdEmpresa;
				entradaFlujoCajaSaldo.p_IdUsuario = in.p_IdUsuario;
				entradaFlujoCajaSaldo.p_IdModulo  = in.p_IdModulo;
				entradaFlujoCajaSaldo.p_UsaReglasNegocio="S";
				entradaFlujoCajaSaldo.p_FechaDesde = fechaSistema;
				entradaFlujoCajaSaldo.p_FechaHasta = fechaSistema;
				entradaFlujoCajaSaldo.p_Uso="EGRESOS_TESORERIA";
				entradaFlujoCajaSaldo.p_PortafolioAConsultar =portafoliosAConsultar;
				
				logger.info("Inicio Consultar flujo Caja: " + entradaFlujoCajaSaldo.toString());
				SalidaConsultarFlujoCaja salidaFlujoCajaSaldo = llamadoFlujoCajaSaldo.logica(con, entradaFlujoCajaSaldo);
				List<ObjFLUJOCAJA> flujoCajaSaldo = salidaFlujoCajaSaldo.P_OUT_FLUJOCAJA;
				flujoCajaSaldo = flujoCajaSaldo.stream()
						.filter(s -> s.NombreUso.equals("EGRESOS_TESORERIA") )
						.collect(Collectors.toList());
				
				for (ObjFLUJOCAJA flujo : flujoCajaSaldo) {
					saldos.put(flujo.IdPortafolio,flujo.Saldo);
					
				}
			}
			
			HashMap<Long,String> indicadorCuentaInv = new HashMap<Long,String>();
			List<IndicadoresPortafolio> _indicadoresPortafolios = new ArrayList<IndicadoresPortafolio>();
			HashMap<String,Object> paramsPortafolios = new HashMap<>();
			
			List<Long> portafoliosCI = new ArrayList<Long>();
			for(Comprobante indicadores : comprobante){
				portafoliosCI.add(indicadores.IdPortafolioTitularPago);
				
			}
			
			if (portafoliosCI.size() > 0) {
				paramsPortafolios.put("idsPortafolio", portafoliosCI);
				_indicadoresPortafolios = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO, paramsPortafolios, IndicadoresPortafolio.class);
				for (IndicadoresPortafolio indicador : _indicadoresPortafolios) {
					indicadorCuentaInv.put(indicador.IdPortafolio,indicador.OperaCuentaInversion);
				}
			}
			// --------------------------------------------------------------------------------------------------
			// VALIDACIONES		
			logger.debug("Inicio validaciones basicas");
			if (in.p_UsaReglasNegocio.equalsIgnoreCase("S"))
			{
				
				//Validaciones basicas
				
				RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
				RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
				if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
					logger.debug("NO CUMPLE REQUISITOS BASICOS");
					out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
					//simular
					return out;
				}			
				

				//Validaciones a nivel de Comprobante
				for (Comprobante com: comprobante)
				{
					ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (com.IdComprobante == null || com.IdComprobante == 0), "COMPROBANTE_TESORERIA_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, com, out.P_OUT_MENSAJES);
					ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (com.IdEstadoComprobante.equalsIgnoreCase("BLO")), "COMPROBANTE_TESORERIA_BLOQUEADO", MensajeSalida.MENSAJE_ERROR, com, out.P_OUT_MENSAJES);
					ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (com.IdEstadoComprobante.equalsIgnoreCase("ANU")), "COMPROBANTE_TESORERIA_ANULADO", MensajeSalida.MENSAJE_ERROR, com, out.P_OUT_MENSAJES);
					ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (com.IdEstadoComprobante.equalsIgnoreCase("MOD")), "COMPROBANTE_TESORERIA_MODIFICADO", MensajeSalida.MENSAJE_ERROR, com, out.P_OUT_MENSAJES);				
					ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (com.IdEstadoComprobante.equalsIgnoreCase("LIQ")), "COMPROBANTE_LIQUIDADO_TESORERIA", MensajeSalida.MENSAJE_ERROR, com, out.P_OUT_MENSAJES);				
					ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (com.CantidadFormaDePago.compareTo(0L)==0 && (com.IdEstadoComprobante.equalsIgnoreCase("ING") ||com.IdEstadoComprobante.equalsIgnoreCase("LIP") ||com.IdEstadoComprobante.equalsIgnoreCase("GIP") ||com.IdEstadoComprobante.equalsIgnoreCase("GIR")) ), "COMPROBANTE_TESORERIA_SIN_FORMA_PAGO", MensajeSalida.MENSAJE_ERROR, com, out.P_OUT_MENSAJES);
					ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (com.MontoTotalFormaDePago.compareTo(com.MontoComprobante)==-1 && (com.IdEstadoComprobante.equalsIgnoreCase("ING") ||com.IdEstadoComprobante.equalsIgnoreCase("LIP") ||com.IdEstadoComprobante.equalsIgnoreCase("GIP") ||com.IdEstadoComprobante.equalsIgnoreCase("GIR")) ), "MONTO_FORMA_PAGO_MENOR_MONTO_COMPROBANTE", MensajeSalida.MENSAJE_ERROR, com, out.P_OUT_MENSAJES);
					ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (com.MontoTotalFormaDePago.compareTo(com.MontoComprobante)==1 && (com.IdEstadoComprobante.equalsIgnoreCase("ING") ||com.IdEstadoComprobante.equalsIgnoreCase("LIP") ||com.IdEstadoComprobante.equalsIgnoreCase("GIP") ||com.IdEstadoComprobante.equalsIgnoreCase("GIR")) ), "MONTO_FORMA_PAGO_MAYOR_MONTO_COMPROBANTE", MensajeSalida.MENSAJE_ERROR, com, out.P_OUT_MENSAJES);

					// Requisito operativo: Liquidacion fecha distinta a la del sistema
					if (requisitosComportamiento.get("LIQFECHADIS")!=null) {
						switch(requisitosComportamiento.get("LIQFECHADIS")) {
							case "AUTORIZACION":
								if(com.IdTipoMovimiento.equalsIgnoreCase("MOVEGR") && !com.IdEstadoComprobante.equalsIgnoreCase("PDA")) {
									ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (!com.FechaLiquidacion.equals(fechaSistema)), "COMPROBANTE_TESORERIA_AUTORIZAR_FECHA", MensajeSalida.MENSAJE_AUTORIZACION, com, out.P_OUT_MENSAJES);
								}
							break;
							case "ADVERTENCIA":
								if(com.IdTipoMovimiento.equalsIgnoreCase("MOVEGR") && !com.IdEstadoComprobante.equalsIgnoreCase("PDA")) {
									ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (!com.FechaLiquidacion.equals(fechaSistema)), "COMPROBANTE_TESORERIA_ADVERTIR_FECHA", MensajeSalida.MENSAJE_ADVERTENCIA, com, out.P_OUT_MENSAJES);
								}
							break;
							case "RECHAZO":
								if(com.IdTipoMovimiento.equalsIgnoreCase("MOVEGR") && !com.IdEstadoComprobante.equalsIgnoreCase("PDA")) {
									ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (!com.FechaLiquidacion.equals(fechaSistema)), "COMPROBANTE_TESORERIA_ADVERTIR_FECHA", MensajeSalida.MENSAJE_ERROR, com, out.P_OUT_MENSAJES);
								}
								break;
							default: break;
						}
					}
					if (requisitosComportamiento.get("LIQSALDONEG")!=null) {
						if((com.IdConcepto.equalsIgnoreCase("CIECDV") ||com.IdConcepto.equalsIgnoreCase("REPDIN")) && com.IdTipoPersonaTitularPago.equalsIgnoreCase("CLI")  && com.IdPortafolioTitularPago !=0) {
							if(indicadorCuentaInv.get(com.IdPortafolioTitularPago).equalsIgnoreCase("S")) {							
								switch(requisitosComportamiento.get("LIQSALDONEG")) {
									case "AUTORIZACION":
										if(com.IdTipoMovimiento.equalsIgnoreCase("MOVEGR")&& (com.IdConcepto.equalsIgnoreCase("CIECDV") ||com.IdConcepto.equalsIgnoreCase("REPDIN")) && !com.IdEstadoComprobante.equalsIgnoreCase("PDA")) {
											ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (saldos.get(com.IdPortafolioTitularPago).compareTo(BigDecimal.ZERO)<0), "COMPROBANTE_TESORERIA_AUTORIZAR_SALDO", MensajeSalida.MENSAJE_AUTORIZACION, com, out.P_OUT_MENSAJES);
										}
									break;
									case "ADVERTENCIA":
										if(com.IdTipoMovimiento.equalsIgnoreCase("MOVEGR")&& (com.IdConcepto.equalsIgnoreCase("CIECDV") ||com.IdConcepto.equalsIgnoreCase("REPDIN")) && !com.IdEstadoComprobante.equalsIgnoreCase("PDA")) {
											ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (saldos.get(com.IdPortafolioTitularPago).compareTo(BigDecimal.ZERO)<0), "COMPROBANTE_TESORERIA_ADVERTIR_SALDO", MensajeSalida.MENSAJE_ADVERTENCIA, com, out.P_OUT_MENSAJES);
										}
									break;
									case "RECHAZO":
										if(com.IdTipoMovimiento.equalsIgnoreCase("MOVEGR") && (com.IdConcepto.equalsIgnoreCase("CIECDV") ||com.IdConcepto.equalsIgnoreCase("REPDIN"))&& !com.IdEstadoComprobante.equalsIgnoreCase("PDA")) {
											ProcesaRequisitoComprobante(con, in.p_IdEmpresa, (saldos.get(com.IdPortafolioTitularPago).compareTo(BigDecimal.ZERO)<0), "COMPROBANTE_TESORERIA_ADVERTIR_SALDO", MensajeSalida.MENSAJE_ERROR, com, out.P_OUT_MENSAJES);
										}
										break;
									default: break;
								}
							}
						}
					}
										
					if(listAutorizaciones.size()>0) {
						List<String> copyOfListAutorizaciones = listAutorizaciones.stream().collect(Collectors.toList());
						autorizacionesMap.put(com.IdComprobante, copyOfListAutorizaciones);
						listAutorizaciones.clear();
					}
				}
						
				
			//Validaciones a nivel de Comprobante Detalle				
			for (ComprobanteDetalle comDet: comprobanteDetalle)	
			{	
				ProcesaRequisitoComprobanteDetalle(con, in.p_IdEmpresa, (comDet.IdTipoMovimiento.equalsIgnoreCase("MOVEGR") && comDet.PendienteDeLiquidar > 0), "SIN_LIQUIDAR_MOVIMIENTOS_RELACIONADOS", MensajeSalida.MENSAJE_ERROR, comDet, out.P_OUT_MENSAJES);
			}
						

			//Validaciones a nivel de Comprobante Forma de Pago
			for (ComprobanteFormaDePago comFP: comprobanteFormaDePago)	
			{
				ProcesaRequisitoComprobanteFormaDePago(con, in.p_IdEmpresa, (!comFP.IdEstadoComprobante.equalsIgnoreCase("ANU") && (comFP.IdComprobanteFormaDePago == null || comFP.IdComprobanteFormaDePago.compareTo(0L) == 0)), "FORMA_PAGO_COMPROBANTE_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, comFP, out.P_OUT_MENSAJES);
				ProcesaRequisitoComprobanteFormaDePago(con, in.p_IdEmpresa, (comFP.IdEstadoComprobante.equalsIgnoreCase("ANU")), "FORMA_PAGO_COMPROBANTE_ANULADA", MensajeSalida.MENSAJE_ERROR, comFP, out.P_OUT_MENSAJES);
				ProcesaRequisitoComprobanteFormaDePago(con, in.p_IdEmpresa, (comFP.IdEstadoComprobante.equalsIgnoreCase("LIQ")), "FORMA_PAGO_COMPROBANTE_LIQUIDADA", MensajeSalida.MENSAJE_ERROR, comFP, out.P_OUT_MENSAJES);
				ProcesaRequisitoComprobanteFormaDePago(con, in.p_IdEmpresa, (comFP.IdEstadoFormaDePago.equalsIgnoreCase("NDP") && !comFP.IdEstadoDepositoDetalle.equalsIgnoreCase("LIQ")), "FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO", MensajeSalida.MENSAJE_ERROR, comFP, out.P_OUT_MENSAJES);
				ProcesaRequisitoComprobanteFormaDePago(con, in.p_IdEmpresa, (!comFP.IdEstadoComprobante.equalsIgnoreCase("ANU") && (comFP.IdCuentaBancariaCorredora.compareTo(0L) == 0 || comFP.NumeroCuentaBancariaCorredora.equals("")) &&  !comFP.IdMedioDePago.equalsIgnoreCase("EFE") ), "FORMA_PAGO_COMPROBANTE_SIN_CUENTA_BANCARIA_CORREDORA", MensajeSalida.MENSAJE_ERROR, comFP, out.P_OUT_MENSAJES);
				
				if(permiteCuentaBancariaSocioNegocio.equals("S")) {
					ProcesaRequisitoComprobanteFormaDePago(con, in.p_IdEmpresa, (!comFP.IdEstadoComprobante.equalsIgnoreCase("ANU") && (comFP.IdTipoMovimiento.equalsIgnoreCase("MOVEGR") && !comFP.TipoComprobante.equalsIgnoreCase("PAREGR") && comFP.IdModalidadDePago.equalsIgnoreCase("MODDEP") && (comFP.IdBancoSocio.compareTo(0L) == 0 || comFP.NumeroCuentaBancariaSocio.equals(""))) ), "FORMA_PAGO_COMPROBANTE_SIN_CUENTA_BANCARIA_SOCIO", MensajeSalida.MENSAJE_ERROR, comFP, out.P_OUT_MENSAJES);
				}
				
				ProcesaRequisitoComprobanteFormaDePago(con, in.p_IdEmpresa, (comFP.ValidarFormaDePagoSinDepositar.equalsIgnoreCase("S")  && !comFP.IdEstadoComprobante.equalsIgnoreCase("ANU") && (comFP.IdMedioDePago.equalsIgnoreCase("CHE") || comFP.IdMedioDePago.equalsIgnoreCase("VVI")) && comFP.EstadosPosibleTipoMovimientoMedioDePago.contains("NDP") && !comFP.IdEstadoFormaDePago.equalsIgnoreCase("NDP")), "FORMA_PAGO_COMPROBANTE_SIN_DEPOSITAR", MensajeSalida.MENSAJE_ERROR, comFP, out.P_OUT_MENSAJES);				
				
				
				ProcesaRequisitoComprobanteFormaDePago(con, in.p_IdEmpresa, (comFP.ValidarFormaDePagoSinCertificar.equalsIgnoreCase("S") && !comFP.IdEstadoComprobante.equalsIgnoreCase("ANU") &&  comFP.EstadosPosibleTipoMovimientoMedioDePago.contains("CRT") && (!comFP.IdEstadoFormaDePago.equalsIgnoreCase("CRT") && !comFP.IdEstadoFormaDePago.equalsIgnoreCase("EBV") && !comFP.IdEstadoFormaDePago.equalsIgnoreCase("NDP") )), "FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR", MensajeSalida.MENSAJE_ERROR, comFP, out.P_OUT_MENSAJES);				
			}
			logger.debug("Fin validaciones");
				
				
			//Quitar Registros invalidos
			List<Comprobante> lstComprobantesConErrores = new ArrayList<Comprobante>();   
			Comprobante objComprobantesConErrores;
			List<ComprobanteRequiereAutorizar> lstComprobantesRequiereAutorizar = new ArrayList<ComprobanteRequiereAutorizar>();
			ComprobanteRequiereAutorizar objComprobantesRequiereAutorizar;
			
			for (Comprobante com: comprobante)
			{
				if (com.errores > 0)
				{	
					objComprobantesConErrores = new Comprobante();
					objComprobantesConErrores.IdComprobante = com.IdComprobante;
					if (!lstComprobantesConErrores.contains(objComprobantesConErrores))
					{
						lstComprobantesConErrores.add(objComprobantesConErrores);	
					}
				}
			}
				
			
			//Quitar Registros invalidos(segun validaciones de formas de pago)
			for (ComprobanteFormaDePago comFP: comprobanteFormaDePago)	
			{
				if (comFP.errores > 0)
				{		
					objComprobantesConErrores = new Comprobante();
					objComprobantesConErrores.IdComprobante = comFP.IdComprobanteInformado;
					if (!lstComprobantesConErrores.contains(objComprobantesConErrores))
					{
						lstComprobantesConErrores.add(objComprobantesConErrores);	
					}	
				}
			}
				
			//Quitar Registros invalidos(segun validaciones de comprobante detalle)
			for (ComprobanteDetalle comDet: comprobanteDetalle)	
			{	
				if (comDet.errores > 0)
				{		
					objComprobantesConErrores = new Comprobante();
					objComprobantesConErrores.IdComprobante = comDet.IdComprobanteInformado;
					if (!lstComprobantesConErrores.contains(objComprobantesConErrores))
					{
						lstComprobantesConErrores.add(objComprobantesConErrores);	
					}	
				}	
			}
				
			
				
			if (lstComprobantesConErrores.size() > 0)
			{	
				sql.batch(con, SQL_DELETE_COMPROBANTE, lstComprobantesConErrores, Comprobante.class);
				sql.batch(con, SQL_DELETE_COMPROBANTE_DETALLE, lstComprobantesConErrores, Comprobante.class);
				sql.batch(con, SQL_DELETE_COMPROBANTE_FORMA_DE_PAGO, lstComprobantesConErrores, Comprobante.class);
			}


			//Comprobantes que requieren autorizacion
			param.clear();
			List<Comprobante> comprobanteVal = sql.query(con, SQL_SELECT_COMPROBANTE, param, Comprobante.class);
			HashMap<Long,Comprobante> comprobanteHash = new HashMap<>();
			for (Comprobante com: comprobanteVal){
				comprobanteHash.put(com.IdComprobante,com);
			}
			
			List<MensajeSalida> resultado = new ArrayList<>(out.P_OUT_MENSAJES);
				List<MensajeSalida> resultadoError = new ArrayList<>();
				List<MensajeSalida> resultadoAdvertencia = new ArrayList<>();
				List<MensajeSalida> resultadoAutorizacion = new ArrayList<>();
				resultadoError = resultado.stream()
						.filter(s -> s.tipo.equals(MensajeSalida.MENSAJE_ERROR) )
						.collect(Collectors.toList());
				resultadoAdvertencia = resultado.stream()
						.filter(s -> s.tipo.equals(MensajeSalida.MENSAJE_ADVERTENCIA) )
						.collect(Collectors.toList());
				resultadoAutorizacion = resultado.stream()
						.filter(s -> s.tipo.equals(MensajeSalida.MENSAJE_AUTORIZACION) )
						.collect(Collectors.toList());
				if(resultadoError.size()>0 && (resultadoAdvertencia.size()>0 ||resultadoAutorizacion.size()>0 )) {
					for (MensajeSalida mensaje: out.P_OUT_MENSAJES)
					{
							if (mensaje.tipo.equals(MensajeSalida.MENSAJE_ADVERTENCIA) ||mensaje.tipo.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
								int idx = resultado.indexOf(mensaje);
								resultado.remove(idx);

							} 
						
					}
					out.P_OUT_MENSAJES = resultado;
				}
			
			for (Comprobante com: comprobante)
			{
				if (com.autorizaciones > 0 && comprobanteHash.get(com.IdComprobante)!=null)
				{	
					objComprobantesRequiereAutorizar = new ComprobanteRequiereAutorizar();
					objComprobantesRequiereAutorizar.IdComprobante = com.IdComprobante;
					objComprobantesRequiereAutorizar.IdTipoMovimiento = com.IdTipoMovimiento;
					objComprobantesRequiereAutorizar.AfectaCuentaInversion = com.AfectaCuentaInversion;
					objComprobantesRequiereAutorizar.IdUsuario = in.p_IdUsuario;
					objComprobantesRequiereAutorizar.UsaReglasNegocio = in.p_UsaReglasNegocio;
					objComprobantesRequiereAutorizar.FechaMovimiento = com.FechaMovimiento;
					for(FormaDePagoLiquidar_Entrada fp: in.p_FormaDePagoLiquidar ) {
						if(fp.IdComprobante.equals(objComprobantesRequiereAutorizar.IdComprobante))
							objComprobantesRequiereAutorizar.IdComprobanteFormaDePago = fp.IdComprobanteFormaDePago;
					}
					if (!lstComprobantesRequiereAutorizar.contains(objComprobantesRequiereAutorizar))
					{
						lstComprobantesRequiereAutorizar.add(objComprobantesRequiereAutorizar);	
					}
				}
			}

			if (lstComprobantesRequiereAutorizar.size() > 0) {
				System.out.println("== Generar evento de autorizacion ==");
					
				CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
				
				CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
				entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = in.p_IdEmpresa;
				entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = in.p_IdUsuario;
				entradaCrearEventoAutorizacionMasivo.P_IDMODULO = in.p_IdModulo;
				entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
				entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
					
				Long lngEvento = 0L;
				for(ComprobanteRequiereAutorizar com:lstComprobantesRequiereAutorizar){
					for (String aut: autorizacionesMap.get(com.IdComprobante)) {
						if(aut.equalsIgnoreCase("COMPROBANTE_TESORERIA_AUTORIZAR_FECHA")) {
							lngEvento++;
							CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
							evento.IdTipoEvento = "ALFDDS";
							evento.IdUnion = lngEvento;
							evento.IdModuloSolicita = in.p_IdModulo;
							evento.IdUsuarioSolicita = in.p_IdUsuario;
							evento.Fecha = in.p_FechaMovimiento;
							evento.IdConcepto = "ALFDDS";
							evento.NumeroOperacionConcepto = com.IdComprobante.toString();
							evento.FechaOperacionConcepto = in.p_FechaMovimiento;
							evento.Glosa = com.IdComprobante + " / " + com.IdTipoMovimiento + " / La fecha de liquidación del comprobante no coincide con la fecha de sistema";
								
							entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS.add(evento);

							CrearEventoAutorizacionMasivo.ObjTABLADETALLE eventoDetalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
							eventoDetalle.IdUnion = evento.IdUnion;
							eventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
							eventoDetalle.NombreCampo="IdComprobante";
							eventoDetalle.TipoDato = "LONG";
							eventoDetalle.Valor = com.IdComprobante.toString();
							entradaCrearEventoAutorizacionMasivo.P_DETALLE.add(eventoDetalle);

								eventoDetalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
								eventoDetalle.IdUnion = evento.IdUnion;
								eventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
								eventoDetalle.NombreCampo="IdTipoMovimiento";
								eventoDetalle.TipoDato = "LONG";
								eventoDetalle.Valor = com.IdTipoMovimiento.toString();
								entradaCrearEventoAutorizacionMasivo.P_DETALLE.add(eventoDetalle);
									
								eventoDetalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
								eventoDetalle.IdUnion = evento.IdUnion;
								eventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
								eventoDetalle.NombreCampo="AfectaCuentaInversion";
								eventoDetalle.TipoDato = "STRING";
								eventoDetalle.Valor = com.AfectaCuentaInversion.toString();
								entradaCrearEventoAutorizacionMasivo.P_DETALLE.add(eventoDetalle);
									
								eventoDetalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
								eventoDetalle.IdUnion = evento.IdUnion;
								eventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
								eventoDetalle.NombreCampo="IdUsuario";
								eventoDetalle.TipoDato = "LONG";
								eventoDetalle.Valor = com.IdUsuario.toString();
								entradaCrearEventoAutorizacionMasivo.P_DETALLE.add(eventoDetalle);
									
								eventoDetalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
								eventoDetalle.IdUnion = evento.IdUnion;
								eventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
								eventoDetalle.NombreCampo="UsaReglasNegocio";
								eventoDetalle.TipoDato = "STRING";
								eventoDetalle.Valor = com.UsaReglasNegocio.toString();
									entradaCrearEventoAutorizacionMasivo.P_DETALLE.add(eventoDetalle);
									
								eventoDetalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
								eventoDetalle.IdUnion = evento.IdUnion;
								eventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
								eventoDetalle.NombreCampo="FechaMovimiento";
								eventoDetalle.TipoDato = "STRING";
								eventoDetalle.Valor = com.FechaMovimiento.toString();
								entradaCrearEventoAutorizacionMasivo.P_DETALLE.add(eventoDetalle);
							}
							if(aut.equalsIgnoreCase("COMPROBANTE_TESORERIA_AUTORIZAR_SALDO")) {
						    	  lngEvento++;
						    	  CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
								  evento.IdTipoEvento = "ALFSTN";
								  evento.IdUnion = lngEvento;
								  evento.IdModuloSolicita = in.p_IdModulo;
								  evento.IdUsuarioSolicita = in.p_IdUsuario;
								  evento.Fecha = in.p_FechaMovimiento;
								  evento.IdConcepto = "ALFSTN";
							      evento.NumeroOperacionConcepto = com.IdComprobante.toString();
								  evento.FechaOperacionConcepto = in.p_FechaMovimiento;
								  evento.Glosa = com.IdComprobante + " / " + com.IdTipoMovimiento + " / El flujo de caja es negativo";
										
								  entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS.add(evento);

								  CrearEventoAutorizacionMasivo.ObjTABLADETALLE eventoDetalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
								  eventoDetalle.IdUnion = evento.IdUnion;
							      eventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
								  eventoDetalle.NombreCampo="IdComprobante";
								  eventoDetalle.TipoDato = "LONG";
								  eventoDetalle.Valor = com.IdComprobante.toString();
								  entradaCrearEventoAutorizacionMasivo.P_DETALLE.add(eventoDetalle);

								  eventoDetalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
								  eventoDetalle.IdUnion = evento.IdUnion;
								  eventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
							      eventoDetalle.NombreCampo="IdTipoMovimiento";
								  eventoDetalle.TipoDato = "LONG";
								  eventoDetalle.Valor = com.IdTipoMovimiento.toString();
								  entradaCrearEventoAutorizacionMasivo.P_DETALLE.add(eventoDetalle);
										
								  eventoDetalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
								  eventoDetalle.IdUnion = evento.IdUnion;
								  eventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
								  eventoDetalle.NombreCampo="AfectaCuentaInversion";
								  eventoDetalle.TipoDato = "STRING";
								  eventoDetalle.Valor = com.AfectaCuentaInversion.toString();
								  entradaCrearEventoAutorizacionMasivo.P_DETALLE.add(eventoDetalle);
										
								  eventoDetalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
								  eventoDetalle.IdUnion = evento.IdUnion;
								  eventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
								  eventoDetalle.NombreCampo="IdUsuario";
								  eventoDetalle.TipoDato = "LONG";
								  eventoDetalle.Valor = com.IdUsuario.toString();
								  entradaCrearEventoAutorizacionMasivo.P_DETALLE.add(eventoDetalle);
										
								  eventoDetalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
								  eventoDetalle.IdUnion = evento.IdUnion;
								  eventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
								  eventoDetalle.NombreCampo="UsaReglasNegocio";
								  eventoDetalle.TipoDato = "STRING";
								  eventoDetalle.Valor = com.UsaReglasNegocio.toString();
								  entradaCrearEventoAutorizacionMasivo.P_DETALLE.add(eventoDetalle);
										
								  eventoDetalle = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
								  eventoDetalle.IdUnion = evento.IdUnion;
								  eventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
								  eventoDetalle.NombreCampo="FechaMovimiento";
							      eventoDetalle.TipoDato = "STRING";
								  eventoDetalle.Valor = com.FechaMovimiento.toString();
								  entradaCrearEventoAutorizacionMasivo.P_DETALLE.add(eventoDetalle);
							}
						}
					}
					
					CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
					List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
					List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
					
					out.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
					out.P_OUT_MENSAJES.addAll(mensajesErrores);
					
					if (mensajesAdvertencia.size() > 0) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION", new HashMap<String, String>(), con, in.p_IdEmpresa));
					}
					if (mensajesErrores.size() > 0) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_CREAR_AUTORIZACION", new HashMap<String, String>(), con, in.p_IdEmpresa));
					}
					for(ComprobanteRequiereAutorizar com:lstComprobantesRequiereAutorizar){
						ResultadoProceso_Salida objResultadoProceso = new ResultadoProceso_Salida();
						objResultadoProceso.Campo 		= "IdComprobante";
						objResultadoProceso.Id 			= com.IdComprobante;
						objResultadoProceso.Servicio 	= "cl.bcs.tesoreria.LiquidarComprobante";
						objResultadoProceso.Resultado 	= "PDA";
						out.P_OUT_RESULTADOPROCESO.add(objResultadoProceso);
					}
					
					sql.batch(con, SQL_UPDATE_COMPROBANTE_REQUIERE_AUTORIZAR, lstComprobantesRequiereAutorizar, ComprobanteRequiereAutorizar.class);
					sql.batch(con, SQL_DELETE_COMPROBANTE, lstComprobantesRequiereAutorizar, ComprobanteRequiereAutorizar.class);
				}
			}


			//Verifica si despues de las validaciones hay registros validos para seguir con el proceso
			param.clear();
			List<Comprobante> comprobantesOK = sql.query(con, SQL_SELECT_COMPROBANTE, param, Comprobante.class);				
			
			if (comprobantesOK.size() == 0)
			{
				//simular
				logger.info("Fin Liquidar Comprobante Simulacion");
				return out;
			}

				
			// --------------------------------------------------------------------------------------------------
			// PROCESO DE LIQUIDACION			
			
			//Liquidar las formas de pago de los comprobantes	
			List<ComprobanteFormaDePago> lstLiquidarFormaDePago = new ArrayList<ComprobanteFormaDePago>();
			ComprobanteFormaDePago objLiquidarFormaDePago = new ComprobanteFormaDePago();
			objLiquidarFormaDePago.FechaMovimiento = in.p_FechaMovimiento;
			objLiquidarFormaDePago.IdUsuario       = in.p_IdUsuario;		
			lstLiquidarFormaDePago.add(objLiquidarFormaDePago);		
			sql.batch(con, SQL_LIQUIDAR_FORMAS_DE_PAGO, lstLiquidarFormaDePago, ComprobanteFormaDePago.class);
			
			//Liquidar los comprobantes
			List<Comprobante> lstLiquidarComprobante = new ArrayList<Comprobante>();
			Comprobante objLiquidarComprobante = new Comprobante();
			objLiquidarComprobante.FechaMovimiento = in.p_FechaMovimiento;
			lstLiquidarComprobante.add(objLiquidarComprobante);		
			sql.batch(con, SQL_LIQUIDAR_COMPROBANTE, lstLiquidarComprobante, Comprobante.class);	
			
			
			//Liquidar comprobantes detalles
			List<ComprobanteDetalle> lstLiquidarComprobanteDetalle = new ArrayList<ComprobanteDetalle>();
			ComprobanteDetalle objLiquidarComprobanteDetalle = new ComprobanteDetalle();
			objLiquidarComprobanteDetalle.IdInterno = 1L; //solo para que entre un parametro sino no hace update
			lstLiquidarComprobanteDetalle.add(objLiquidarComprobanteDetalle);
			sql.batch(con, SQL_LIQUIDAR_COMPROBANTE_DETALLE, lstLiquidarComprobanteDetalle, ComprobanteDetalle.class);
			
			
			
			
			// --------------------------------------------------------------------------------------------------
			// ACTUALIZA SALDOS DE CUENTAS BANCARIAS		
			
			ActualizarSaldosCuentaBanco servicioActualizarSaldosCuentaBanco = new ActualizarSaldosCuentaBanco();
			ActualizarSaldosCuentaBanco_Entrada entradaActualizarSaldosCuentaBanco = new ActualizarSaldosCuentaBanco_Entrada();		
			

			//Obtien datos de las cuentas bancarias de la corredora a actualizar el saldo
			param.clear();
			logger.debug("Actualizar bancos");
			List<CuentaBanco_Entrada> lstCuentaBanco = sql.query(con, SQL_SELECT_CUENTA_BANCO_A_ACTUALIZAR, param, CuentaBanco_Entrada.class);
			
			if (lstCuentaBanco.size() > 0)
			{
				entradaActualizarSaldosCuentaBanco.p_FechaMovimiento = in.p_FechaMovimiento;
				entradaActualizarSaldosCuentaBanco.p_CuentaBanco = lstCuentaBanco;
			
				ActualizarSaldosCuentaBanco_Salida salidaActualizarSaldosCuentaBanco = servicioActualizarSaldosCuentaBanco.logica(con, entradaActualizarSaldosCuentaBanco);
				
				out.P_OUT_MENSAJES.addAll(salidaActualizarSaldosCuentaBanco.P_OUT_MENSAJES);			
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				
				if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1) {
					//simular
					return out;
				}
				
			}
			
			
			// --------------------------------------------------------------------------------------------------
			// GENERA RESULTADO PROCESO DE LIQUIDACION
			
			for (Comprobante com: comprobante)
			{
				if (com.ok > 0 && com.IdComprobante.compareTo(0L) == 1)
				{
					ResultadoProceso_Salida objResultadoProceso = new ResultadoProceso_Salida();
					objResultadoProceso.Campo 		= "IdComprobante";
					objResultadoProceso.Id 			= com.IdComprobante;
					objResultadoProceso.Servicio 	= "cl.bcs.tesoreria.LiquidarComprobante";
					objResultadoProceso.Resultado 	= "LIQ";
					out.P_OUT_RESULTADOPROCESO.add(objResultadoProceso);
				}
			}

			

			// --------------------------------------------------------------------------------------------------
			// GENERA CARGO/ABONO EN CUENTA INVERSION (para cliente, corredora segun parametro de entrada)
			
			//Obtiene y guarda en tabla global datos de comprobantes que moveran cuenta inversion
			List<ParametrosConfiguracion> lstParametrosConfiguracion = new ArrayList<ParametrosConfiguracion>();
			ParametrosConfiguracion objParametrosConfiguracion = new ParametrosConfiguracion();	
			objParametrosConfiguracion.LiquidacionParcialEnviaACTA = liquidacionParcialEnviaACTA;
			lstParametrosConfiguracion.add(objParametrosConfiguracion);
			sql.batch(con, SQL_INSERT_COMPROBANTES_MUEVEN_CTA_INVERSION, lstParametrosConfiguracion, ParametrosConfiguracion.class);
			
			//Para revisar contenido de tabla global que mueve cuenta inversion			
			//List<ComprobantesMuevenCuentaInversion> comprobantesMuevenCuentaInversion = sql.query(con, SQL_SELECT_ComprobantesMuevenCuentaInversion, param, ComprobantesMuevenCuentaInversion.class);
			
			// Obtiene y prepara los datos para el envio a cuenta inversion
			param.clear();		
			param.put("IdEmpresa", in.p_IdEmpresa);		
			param.put("FechaMovimiento", in.p_FechaMovimiento);
			List<ObjCargoAbonoAGenerar> lstCargoAbonoAGenerar = sql.query(con, SQL_SELECT_CARGO_ABONO_A_GENERAR, param, ObjCargoAbonoAGenerar.class);
			
			
			
			if (lstCargoAbonoAGenerar.size() > 0)
			{
				String pValidaFlujoCaja = "N";
				try {	
					pValidaFlujoCaja = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUENTAINVERSION", "VALIDAFLUJOCAJAENCREARCARGOABONO");
				}
				catch(Exception er) {
					pValidaFlujoCaja  = "N";
				}

				Set<String> conceptosIgnoranValidacionFlujoCaja = obtenerConceptosIgnoranValidacionFlujoCaja(con, in.p_IdEmpresa, lstCargoAbonoAGenerar);
				List<ObjCargoAbonoAGenerar> cargosAbonosValidanFlujoCaja = lstCargoAbonoAGenerar
						.stream()
						.filter(cargoAbono -> conceptosIgnoranValidacionFlujoCaja.contains(cargoAbono.IdConcepto) == false)
						.collect(Collectors.toList());

				if("S".equals(pValidaFlujoCaja) && cargosAbonosValidanFlujoCaja.size() > 0) {
					SaldoFlujoCaja.ServiceEntrada entradaFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
					java.sql.Date fechaLiquidacionMinima = Fecha.toDate(cargosAbonosValidanFlujoCaja.get(0).FechaCondicionDeLiquidacion);
					java.sql.Date fechaLiquidacionMaxima = Fecha.toDate(cargosAbonosValidanFlujoCaja.get(0).FechaCondicionDeLiquidacion);
					
					HashSet<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafoliosFlujoCaja = new HashSet<>();
					HashMap<FlujoCajaElem,BigDecimal> totalEgresosPortafolio = new HashMap<>();
					
				
					HashMap<FlujoCajaElem,BigDecimal> totalFlujoCajaPortafolio = new HashMap<>();
					
					for(ObjCargoAbonoAGenerar cargoAbono : cargosAbonosValidanFlujoCaja) {
						if("MOVCAR".equals(cargoAbono.IdTipoMovimiento)) {
							java.sql.Date fechaLiquidacion = Fecha.toDate(cargoAbono.FechaCondicionDeLiquidacion);
							
							if (fechaLiquidacion.before(fechaLiquidacionMinima)) {
								fechaLiquidacionMinima = fechaLiquidacion;
							}
							
							if (fechaLiquidacion.after(fechaLiquidacionMaxima)) {
								fechaLiquidacionMaxima = fechaLiquidacion;
							}
							
							FlujoCajaElem llave = new FlujoCajaElem(cargoAbono.IdMoneda, cargoAbono.FechaCondicionDeLiquidacion,cargoAbono.IdPortafolio);
							if (!totalEgresosPortafolio.containsKey(llave)) {
								totalEgresosPortafolio.put(llave, BigDecimal.ZERO);
							}
							
							BigDecimal totalEgresos = totalEgresosPortafolio.get(llave);
							totalEgresos = totalEgresos.add(cargoAbono.Monto);
							totalEgresosPortafolio.put(llave, totalEgresos);
							
							SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR port = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
							port.IdMoneda = cargoAbono.IdMoneda;
							port.IdPortafolio = cargoAbono.IdPortafolio;
							portafoliosFlujoCaja.add(port);
						}
					}
					
					if (portafoliosFlujoCaja.size() > 0) {
				    	
				    	entradaFlujoCaja.P_IDEMPRESA = in.p_IdEmpresa;
						entradaFlujoCaja.P_FECHADESDE = Fecha.toStringDate(fechaLiquidacionMinima);
						entradaFlujoCaja.P_FECHAHASTA = Fecha.toStringDate(fechaLiquidacionMaxima);
						entradaFlujoCaja.P_USO = "LIQUIDACION_TESORERIA";
						entradaFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<ObjPORTAFOLIOACONSULTAR>(portafoliosFlujoCaja);
						
						SaldoFlujoCaja saldoFlujoCajaService = new SaldoFlujoCaja();
						SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = saldoFlujoCajaService.logica(con,entradaFlujoCaja);
						
						List<ObjFLUJOCAJA> resultadoFlujoCaja = salidaFlujoCaja.P_OUT_FLUJOCAJA;
						for (ObjFLUJOCAJA flujoCaja : resultadoFlujoCaja) {
							FlujoCajaElem llave = new FlujoCajaElem(flujoCaja.IdMoneda, flujoCaja.FechaSaldo, flujoCaja.IdPortafolio);
							if (!totalFlujoCajaPortafolio.containsKey(llave)) {
								totalFlujoCajaPortafolio.put(llave, BigDecimal.ZERO);
							}
							BigDecimal totalFlujoCaja = totalFlujoCajaPortafolio.get(llave);
							totalFlujoCaja = totalFlujoCaja.add(flujoCaja.Saldo);
							totalFlujoCajaPortafolio.put(llave, totalFlujoCaja);
						}
						
						List<Long> idsPortafolio = cargosAbonosValidanFlujoCaja
								.stream()
								.map(cargoAbono->cargoAbono.IdPortafolio)
								.collect(Collectors.toList());
						HashMap<String, Object> parametros = new HashMap<String, Object>(2);
						parametros.put("IdPortafolio", idsPortafolio);
						List<DatoPortafolio> datosPortafolios = sql.query(con, SQL_SELECT_DATOS_PORTAFOLIO, parametros, DatoPortafolio.class);
						Map<Long, DatoPortafolio> datosPortafoliosPorIdPortafolio = datosPortafolios.stream().collect(Collectors.toMap(portafolio->portafolio.IdPortafolio, portafolio->portafolio));
						
						for(ObjCargoAbonoAGenerar cargoAbono : cargosAbonosValidanFlujoCaja) {
							if("MOVCAR".equals(cargoAbono.IdTipoMovimiento)) {
								FlujoCajaElem llave = new FlujoCajaElem(cargoAbono.IdMoneda, cargoAbono.FechaCondicionDeLiquidacion, cargoAbono.IdPortafolio);			        	
					        	BigDecimal totalEgresos = totalEgresosPortafolio.get(llave);
					        	BigDecimal totalFlujoCaja = totalFlujoCajaPortafolio.get(llave);
					        	
					            Boolean ValidaSaldos = IndicadorNoValidaSaldoCaja(con, in.p_IdEmpresa, lstCargoAbonoAGenerar);
							if (ValidaSaldos) {
					            if(totalEgresos.compareTo(totalFlujoCaja) == 1) {
					        		DatoPortafolio datoPortafolio = datosPortafoliosPorIdPortafolio.get(cargoAbono.IdPortafolio);
					        		ProcesaRequisitoCargoAbono(con, in.p_IdEmpresa, true, "SALDO_CAJA_INSUFICIENTE", MensajeSalida.MENSAJE_ERROR, cargoAbono, datoPortafolio, out.P_OUT_MENSAJES);
					              	}
					        	}
							}
						}
						
						out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
						if(out.P_OUT_ERRORESCONTADOR > 0) {
							return out;
						}
				    	
				    }
				}

				Long numeroCargoAbono = 0L;
				for(ObjCargoAbonoAGenerar objCargAbo: lstCargoAbonoAGenerar)
				{
					numeroCargoAbono++;
					objCargAbo.NumeroCargoAbono = numeroCargoAbono;			
				}
				
				CrearCargoAbono serviceCrearCargoAbono = new CrearCargoAbono();
				CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();

				entradaCrearCargoAbono.p_IdEmpresa 		    = in.p_IdEmpresa;			
				entradaCrearCargoAbono.p_IdUsuario 		    = in.p_IdUsuario;
				entradaCrearCargoAbono.p_IdModulo 		    = "TES";
				//entradaCrearCargoAbono.p_simularOperacion   = "N";
				entradaCrearCargoAbono.p_simularOperacion   = in.p_SimularOperacion;
				entradaCrearCargoAbono.p_UsaReglasNegocio   = in.p_UsaReglasNegocio;
				entradaCrearCargoAbono.p_FechaSistema 	    = in.p_FechaMovimiento;
				entradaCrearCargoAbono.p_EsIngresoManual    = "N";
				
				entradaCrearCargoAbono.p_CargoAbonoAGenerar = lstCargoAbonoAGenerar;
				entradaCrearCargoAbono.p_CargoAbonoAGenerar.get(0).FolioComprobante = in.FolioCSC;
				CrearCargoAbonoSalida salidaCrearCargoAbono = serviceCrearCargoAbono.logica(con, entradaCrearCargoAbono);
				
				out.P_OUT_MENSAJES.addAll(salidaCrearCargoAbono.P_OUT_MENSAJES);
				
				//Genera Resultado proceso de movimientos generados en cuenta inversion(se anexa al resultado de la liquidacion) 
				if (salidaCrearCargoAbono.P_OUT_CARGOABONOGENERADO.size() > 0)
				{
					for(ObjCargoAbonoGenerado result : salidaCrearCargoAbono.P_OUT_CARGOABONOGENERADO)
					{
						ResultadoProceso_Salida objResultadoProceso = new ResultadoProceso_Salida();
						objResultadoProceso.Campo 		= "IdCargoAbonoMovimiento";
						objResultadoProceso.Id 			= result.IdCargoAbonoMovimiento;
						objResultadoProceso.Servicio 	= "cl.bcs.cuentaInversion.CrearCargoAbono";
						objResultadoProceso.Resultado 	= "CRT";
						out.P_OUT_RESULTADOPROCESO.add(objResultadoProceso);
					}
					
				}	
				
				
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);					
				if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1) {
					//simular
					return out;
				}
				
				
			} // FIN   if (lstCargoAbonoAGenerar.size() > 0)
		} catch(Exception e) {
			throw e;
		} finally {
			sql.batch(con, SQL_DELETE_COMPROBANTE, comprobantesAProcesar, Comprobante.class);
			sql.batch(con, SQL_DELETE_COMPROBANTE_DETALLE, comprobantesAProcesar, Comprobante.class);
			sql.batch(con, SQL_DELETE_COMPROBANTE_FORMA_DE_PAGO, comprobantesAProcesar, Comprobante.class);
			sql.batch(con, SQL_DELETE_COMPROBANTES_A_LIQUIDAR, comprobantesAProcesar, Comprobante.class);
			sql.batch(con, SQL_DELETE_FORMASDEPAGO_A_LIQUIDAR, comprobantesAProcesar, Comprobante.class);
			sql.batch(con, SQL_DELETE_COMPROBANTES_MUEVEN_CI, comprobantesAProcesar, Comprobante.class);
		}
		
		// --------------------------------------------------------------------------------------------------
		// GENERAR EVENTO DE NOTIFICACION : TODO
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		logger.info("== TERMINO LIQUIDAR COMPROBANTE ==");
		
		
		
		return out;

	}
	
	private Set<String> obtenerConceptosIgnoranValidacionFlujoCaja(Connection conn, Long idEmpresa, List<ObjCargoAbonoAGenerar> cargosAbonos) throws SQLException {
		logger.debug("INICIO::LiquidarComprobante.obtenerConceptosIgnoranValidacionFlujoCaja");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("ConceptoIds", cargosAbonos.stream().map(u -> u.IdConcepto).collect(Collectors.toList()));

		Map<String, String> indicadorNoValidaSaldoCajaPorIdConcepto = new SQL()
				.query(conn, SQL_SELECT_CONCEPTO_TESORERIA, params, ConceptoTesoreria.class)
				.stream()
				.collect(Collectors.toMap(u -> u.IdConcepto, u -> u.IndicadorNoValidaSaldoCaja));

		Set<String> conceptosHabilitanConfiguracionIndicador;
		try {
			conceptosHabilitanConfiguracionIndicador = new HashSet<>(ConfiguracionSistema.getParametroConfiguracionSet(conn, idEmpresa, "PLATAFORMA", "PLA.ConceptosCheckSinValidacionFlujoCaja", ";"));
		} catch (Exception e) {
			conceptosHabilitanConfiguracionIndicador = Collections.emptySet();
		}

		Set<String> conceptosIgnoranValidacionFlujoCaja = conceptosHabilitanConfiguracionIndicador
				.stream()
				.filter(concepto -> "S".equals(indicadorNoValidaSaldoCajaPorIdConcepto.get(concepto)))
				.collect(Collectors.toSet());

		long fin = System.currentTimeMillis();
		logger.debug("FIN::LiquidarComprobante.obtenerConceptosIgnoranValidacionFlujoCaja {}", fin - inicio);

		return conceptosIgnoranValidacionFlujoCaja;
	}
	
	private Boolean IndicadorNoValidaSaldoCaja(Connection conn, Long idEmpresa, List<ObjCargoAbonoAGenerar> cargosAbonos) throws SQLException {
		logger.debug("INICIO::LiquidarComprobante.IgnorarSaldosNegativos");
		long inicio = System.currentTimeMillis();
		Boolean Respuesta = true;

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("ConceptoIds", cargosAbonos.stream().map(u -> u.IdConcepto).collect(Collectors.toList()));

			
		Map<String, String> indicadorNoValidaSaldoCajaPorIdConcepto = new SQL()
				.query(conn, SQL_SELECT_CONCEPTO_TESORERIA, params, ConceptoTesoreria.class)
				.stream().filter (u -> u.IndicadorNoValidaSaldoCaja.equals("S") ).collect(Collectors.toMap(u -> u.IdConcepto, u -> u.IndicadorNoValidaSaldoCaja));
        if (indicadorNoValidaSaldoCajaPorIdConcepto.size() >0) {
        	Respuesta = false;
        } else {
        	Respuesta =true;
        }
			
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::LiquidarComprobante.IgnorarSaldosNegativos", fin - inicio);
		
		return Respuesta;
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		
		LiquidarComprobante_Entrada in = gson.fromJson(jin.toString(), LiquidarComprobante_Entrada.class);
		LiquidarComprobante_Salida out = logica(con, in);
		//LiquidarComprobante_Salida out = new LiquidarComprobante_Salida();
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		//out.P_OUT_MENSAJES=(List<MensajeSalida>) in;
		if(in.p_SimularOperacion.equalsIgnoreCase("S")) {
			con.rollback();
		}
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
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

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2020-08-26\",\"p_ComprobanteLiquidar\":[{\"IdComprobante\":6579,\"IdTipoMovimiento\":\"MOVEGR\",\"AfectaCuentaInversion\":\"CLI\"}],\"p_FormaDePagoLiquidar\":[]}";

			
			LiquidarComprobante_Entrada Objin = gson.fromJson(json, LiquidarComprobante_Entrada.class);
			LiquidarComprobante demo = new LiquidarComprobante();
			
 			System.out.println("ENTRADA:");
 			System.out.println(Objin.toString());
 			
 			try 
 			{
 				LiquidarComprobante_Salida ObjOut = demo.logica(con, Objin);
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
