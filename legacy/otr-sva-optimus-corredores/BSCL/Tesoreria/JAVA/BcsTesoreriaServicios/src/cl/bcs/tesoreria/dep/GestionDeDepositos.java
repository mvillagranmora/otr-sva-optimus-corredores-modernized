package cl.bcs.tesoreria.dep;

/**
 * Proposito: Gestionar nominas de depositos o el detalle de estas:
 *  	EDP: Enviar a deposito		
 *  	LIQ: Liquidar nomina de deposito o detalle de este	
 *  	ANU: Anular nomina de deposito  o detalle de este	
 */

import java.sql.Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.LiquidarComprobante;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.tesoreria.LiquidarComprobante;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.ComprobanteLiquidar_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.FormaDePagoLiquidar_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Salida;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Entrada;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.CuentaBanco_Entrada;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Salida;

public class GestionDeDepositos implements JavaCall {

	// Log
	private static final Logger logger = LogManager.getLogger(GestionDeDepositos.class);
	
	private final String SQL_INSERT_DEPOSITO_PROCESO   							= loadResourceToString("cl/bcs/tesoreria/sql/GestionDeDepositos_InsertDepositoProceso.sql");	
	private final String SQL_SELECT_DEPOSITO_PROCESO   							= loadResourceToString("cl/bcs/tesoreria/sql/GestionDeDepositos_SelectDepositoProceso.sql");
	private final String SQL_UPDATE_DEPOSITO_DETALLE_ENTIDAD_DEPOSITO   		= loadResourceToString("cl/bcs/tesoreria/sql/GestionDeDepositos_UpdateDepositoDetalle_EntidadDeposito.sql");	
	private final String SQL_UPDATE_DEPOSITO_ENTIDAD_DEPOSITO   				= loadResourceToString("cl/bcs/tesoreria/sql/GestionDeDepositos_UpdateDeposito_EntidadDeposito.sql");	
	private final String SQL_UPDATE_DEPOSITO_DETALLE_ENTIDAD_DEPOSITODETALLE   	= loadResourceToString("cl/bcs/tesoreria/sql/GestionDeDepositos_UpdateDepositoDetalle_EntidadDepositoDetalle.sql");	
	private final String SQL_UPDATE_DEPOSITO_ENTIDAD_DEPOSITODETALLE   			= loadResourceToString("cl/bcs/tesoreria/sql/GestionDeDepositos_UpdateDeposito_EntidadDepositoDetalle.sql");                                          
	private final String SQL_LIBERAR_COMPROBANTE_FORMA_DE_PAGO   				= loadResourceToString("cl/bcs/tesoreria/sql/GestionDeDepositos_LiberarComprobanteFormaDePago.sql");
	private final String SQL_SELECT_COMPROBANTE_FORMA_DE_PAGO   				= loadResourceToString("cl/bcs/tesoreria/sql/GestionDeDepositos_SelectComprobanteFormaDePago.sql");
	private final String SQL_UPDATE_COMPROBANTE_FORMA_DE_PAGO   				= loadResourceToString("cl/bcs/tesoreria/sql/GestionDeDepositos_UpdateComprobanteFormaDePago.sql");

	
    //-------------------------------------------------------------------------------------------
	// CLASES PARA ENTRADA
	//-------------------------------------------------------------------------------------------

	public static class GestionDeDepositos_Entrada 
	{		
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String		p_IdModulo;
		public String		p_simularOperacion;
		public String		p_UsaReglasNegocio;				
		public String		p_FechaMovimiento;		// Corresponde a Fecha Sistema
		public String		p_TipoEjecucion;		// EDP: Enviar a deposito		LIQ: Nomina liquidada   	ANU: Nomina Anulada	
		
		public List<RegistroDeposito> p_Registros;

		public GestionDeDepositos_Entrada()
		{
			this.p_Registros = new ArrayList<RegistroDeposito>();	
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	public static class RegistroDeposito 
	{
		public Long			Id;
		public String		Entidad;  		// NVARCHAR(300)
		public String		IdConcepto; 	// NVARCHAR(6)
		public String		Descripcion;	// NVARCHAR(300)
		   
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	
    //-------------------------------------------------------------------------------------------
	// CLASES PARA SALIDA
	//-------------------------------------------------------------------------------------------
	
	public static class GestionDeDepositos_Salida {

		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;		
		
		public GestionDeDepositos_Salida()
		{	
			this.P_OUT_MENSAJES 	= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	


	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------------------------------------------------

	
	public static class RegistroDeposito_Proceso
	{
		
		public Long			IdInterno;
		public String		TipoEjecucion;	 		
		public Long			Id;
		public String		Entidad;  		// NVARCHAR(300)
		public String		IdConcepto; 	// NVARCHAR(6)
		public String		Descripcion;	// NVARCHAR(300)
		public Long			IdDeposito;
		public String		IdEstadoDeposito;
		public Long			IdDepositoDetalle; 		
		public String		IdEstadoDepositoDetalle;		
		
		public Long			IdUsuario;
		public String		FechaMovimiento;
		
		//Para validaciones
		public long 		errores = 0;
		public long 		advertencias = 0;
		public long 		autorizaciones = 0;
		public long 		ok = 0;
		
		

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

        
	public static class ComprobanteFormaPago
	{
		public Long 	IdComprobante;
		public String 	IdTipoMovimiento;
		public Long 	IdComprobanteFormaDePago;
		public Long 	IdCuentaBancariaCorredora;		
		public String 	IdEstadoDepositoDetalle;
		public String 	IdEstadoComprobanteFormaDePago;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		GestionDeDepositos_Entrada in = gson.fromJson(jin.toString(), GestionDeDepositos_Entrada.class);
		GestionDeDepositos_Salida out = logica(con, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODOS COMPLEMANTARIOS
	//-------------------------------------------------------------------------------------------
	
	private void ProcesaRequisito(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, RegistroDeposito_Proceso depositoProceso, List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		if (esError) {
			String msg=Diccionario.getMensaje(con,idEmpresa,codMsg)				
				  .add("Id", depositoProceso.Id)
				  .msg();
			
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(depositoProceso.Id,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				depositoProceso.errores++;
			}else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(depositoProceso.Id,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				depositoProceso.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(depositoProceso.Id,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				depositoProceso.advertencias++;
			}
			else {
				depositoProceso.ok++;
			}
		}else {
			depositoProceso.ok++;
		}		
	}

	
	public void limpiarDepositos(List<RegistroDeposito_Proceso> depositoProceso, List<RegistroDeposito_Proceso> depositoConErrores) {
		for (RegistroDeposito_Proceso deposito : depositoProceso) {
			if (deposito.errores > 0) {
				depositoConErrores.add(deposito);
			}
		}
		
		for (RegistroDeposito_Proceso depositoErr : depositoConErrores) {
			if (depositoProceso.contains(depositoErr))
				depositoProceso.remove(depositoErr);
		}
	}
	
	
    
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------
	
	public GestionDeDepositos_Salida logica(Connection con, GestionDeDepositos_Entrada in) throws Exception {
		
		logger.info(":: INICIO GestionDeDepositos ::");	
		logger.info("ENTRADA > " + in.toString());
		
		SQL _sql = new SQL();
		GestionDeDepositos_Salida out = new GestionDeDepositos_Salida();
		HashMap<String, Object> param = new HashMap<String, Object>();
		
		
		
		//Si usa regla de negocio realiza validaciones
		if ("S".equalsIgnoreCase(in.p_UsaReglasNegocio))
		{
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				logger.info("NO CUMPLE REQUISITOS BASICOS");
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
						
			if (in.p_Registros.isEmpty()) {				
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"DEPOSITO_NO_EXISTE",null,con, in.p_IdEmpresa));
				return out;
			}
		}
		
		 
		//Inserta parametros de entrada a tabla global
		_sql.batch(con, SQL_INSERT_DEPOSITO_PROCESO, in.p_Registros , RegistroDeposito.class);
	
		
		//Obtiene datos de proceso
		List<RegistroDeposito_Proceso> registroDepositoProceso = new ArrayList<RegistroDeposito_Proceso>();
		List<RegistroDeposito_Proceso> registroDepositoConErrores = new ArrayList<RegistroDeposito_Proceso>();		
		param.clear();
		param.put("TipoEjecucion", in.p_TipoEjecucion);		
		param.put("IdUsuario", in.p_IdUsuario);
		param.put("FechaMovimiento", in.p_FechaMovimiento);

		
		registroDepositoProceso = _sql.query(con, SQL_SELECT_DEPOSITO_PROCESO, param, RegistroDeposito_Proceso.class);
		
		
		//---------------------------------------------------------------------------------------------
		//VALIDACIONES
		
		for (RegistroDeposito_Proceso deposito: registroDepositoProceso)
		{
			ProcesaRequisito(con, in.p_IdEmpresa, (deposito.TipoEjecucion.equalsIgnoreCase("ANU") && deposito.IdEstadoDeposito.equalsIgnoreCase("EDP")), "NOMINA_DEPOSITADA", MensajeSalida.MENSAJE_ERROR, deposito, out.P_OUT_MENSAJES);
			ProcesaRequisito(con, in.p_IdEmpresa, (deposito.TipoEjecucion.equalsIgnoreCase("LIQ") && deposito.IdEstadoDeposito.equalsIgnoreCase("ING")), "NOMINA_SIN_DEPOSITAR", MensajeSalida.MENSAJE_ERROR, deposito, out.P_OUT_MENSAJES);
		
			// Validaciones para entidad TES.Deposito
			ProcesaRequisito(con, in.p_IdEmpresa, (deposito.Entidad.equalsIgnoreCase("TES.Deposito") && deposito.IdDeposito.compareTo(0L)==0), "DEPOSITO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, deposito, out.P_OUT_MENSAJES);
			ProcesaRequisito(con, in.p_IdEmpresa, (deposito.Entidad.equalsIgnoreCase("TES.Deposito") && deposito.IdEstadoDeposito.equalsIgnoreCase("ANU")), "DEPOSITO_ANULADO", MensajeSalida.MENSAJE_ERROR, deposito, out.P_OUT_MENSAJES);
			ProcesaRequisito(con, in.p_IdEmpresa, (deposito.Entidad.equalsIgnoreCase("TES.Deposito") && deposito.IdEstadoDeposito.equalsIgnoreCase("LIQ")), "DEPOSITO_LIQUIDADO", MensajeSalida.MENSAJE_ERROR, deposito, out.P_OUT_MENSAJES);
			
			// Validaciones para entidad TES.DepositoDetalle
			ProcesaRequisito(con, in.p_IdEmpresa, (deposito.Entidad.equalsIgnoreCase("TES.DepositoDetalle") && deposito.IdDepositoDetalle.compareTo(0L)==0), "DETALLE_DEPOSITO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, deposito, out.P_OUT_MENSAJES);
			ProcesaRequisito(con, in.p_IdEmpresa, (deposito.Entidad.equalsIgnoreCase("TES.DepositoDetalle") && deposito.IdEstadoDepositoDetalle.equalsIgnoreCase("LIQ")), "DETALLE_DEPOSITO_LIQUIDADO", MensajeSalida.MENSAJE_ERROR, deposito, out.P_OUT_MENSAJES);
			ProcesaRequisito(con, in.p_IdEmpresa, (deposito.Entidad.equalsIgnoreCase("TES.DepositoDetalle") && deposito.IdEstadoDepositoDetalle.equalsIgnoreCase("ANU")), "DETALLE_DEPOSITO_ANULADO", MensajeSalida.MENSAJE_ERROR, deposito, out.P_OUT_MENSAJES);

		}
	

							
		limpiarDepositos(registroDepositoProceso, registroDepositoConErrores);
		
		if (registroDepositoProceso.size() == 0) 
		{
			logger.debug("DEPOSITOS INVALIDALIDOS > " + registroDepositoConErrores.toString());
			return out;
		}

		

		
	 	// -----------------------------------------------------------------------------------------
		// ACTUALIZACIONES DE DEPOSITO Y DEPOSITO DETALLE CUANDO ENTIDAD ES "TES.Deposito"
		
		// Actualiza estado a DepositoDetalle cuando entidad es TES.Deposito
		_sql.batch(con, SQL_UPDATE_DEPOSITO_DETALLE_ENTIDAD_DEPOSITO, registroDepositoProceso , RegistroDeposito_Proceso.class);
		
		// Actualiza estado y datos de liquidacion a Deposito cuando entidad es TES.Deposito
		_sql.batch(con, SQL_UPDATE_DEPOSITO_ENTIDAD_DEPOSITO, registroDepositoProceso , RegistroDeposito_Proceso.class);
		
		
	 	// -----------------------------------------------------------------------------------------
		// ACTUALIZACIONES DE DEPOSITO Y DEPOSITO DETALLE CUANDO ENTIDAD ES "TES.DepositoDetalle"
		
		// Actualizacion de estado y otros a DepositoDetalle cuando entidad es 'TES.DepositoDetalle'
		_sql.batch(con, SQL_UPDATE_DEPOSITO_DETALLE_ENTIDAD_DEPOSITODETALLE, registroDepositoProceso , RegistroDeposito_Proceso.class);	
		
		//Actualizacio de estado y otros a Deposito cuando entidad es 'TES.DepositoDetalle'
		_sql.batch(con, SQL_UPDATE_DEPOSITO_ENTIDAD_DEPOSITODETALLE, registroDepositoProceso , RegistroDeposito_Proceso.class);
		

		
	 	// -----------------------------------------------------------------------------------------
		// ANULACION - Al anular el deposito se libera la forma de pago del comprobante asociada a ese deposito	
		if (in.p_TipoEjecucion.equalsIgnoreCase("ANU"))
		{			
			_sql.batch(con, SQL_LIBERAR_COMPROBANTE_FORMA_DE_PAGO, registroDepositoProceso , RegistroDeposito_Proceso.class);
		}
		
		
		
	 	// -----------------------------------------------------------------------------------------
		// LIQUIDACION
		if (in.p_TipoEjecucion.equalsIgnoreCase("LIQ"))
		{	
			LiquidarComprobante servicioLiquidarComprobante = new LiquidarComprobante();
			LiquidarComprobante_Entrada entradaLiquidarComprobante = new LiquidarComprobante_Entrada();
			List<ComprobanteFormaPago> lstComprobanteALiquidar = new ArrayList<ComprobanteFormaPago>();
			
			
			
			List<Long> idsDeposito = new ArrayList<Long>();
			List<Long> idsDepositoDetalle = new ArrayList<Long>();
			idsDeposito.add(0L);
			idsDepositoDetalle.add(0L);
			for (RegistroDeposito_Proceso deposito: registroDepositoProceso)
			{
				if (deposito.Entidad.equalsIgnoreCase("TES.Deposito"))				
					idsDeposito.add(deposito.Id);
				else if (deposito.Entidad.equalsIgnoreCase(" TES.DepositoDetalle"))
					idsDepositoDetalle.add(deposito.Id);				
			}
			
			
			// Obtiene datos de las formas de pago del comprobante asociadas a los depositos;
			param.clear();			
			param.put("IdsDeposito", idsDeposito);
			param.put("IdsDetalle", idsDepositoDetalle);
						
			List<ComprobanteFormaPago> lstComprobanteFormaPago = _sql.query(con, SQL_SELECT_COMPROBANTE_FORMA_DE_PAGO, param, ComprobanteFormaPago.class);
			
					
					
			for (ComprobanteFormaPago cfp: lstComprobanteFormaPago)
			{
				if (cfp.IdEstadoDepositoDetalle.equalsIgnoreCase("LIQ") && !cfp.IdEstadoComprobanteFormaDePago.equalsIgnoreCase("LIQ") && cfp.IdComprobante.compareTo(0L) != 0)	
				{
					lstComprobanteALiquidar.add(cfp);
				}
			}
	
			
			if (lstComprobanteALiquidar.size() > 0)
			{
				ComprobanteLiquidar_Entrada objComprobanteLiquidar;	
				FormaDePagoLiquidar_Entrada objFormaDePagoLiquidar;
				
				entradaLiquidarComprobante.p_IdEmpresa 			= in.p_IdEmpresa;	 
				entradaLiquidarComprobante.p_IdUsuario 			= in.p_IdUsuario;			   
				entradaLiquidarComprobante.p_IdModulo 			= in.p_IdModulo;			
				entradaLiquidarComprobante.p_SimularOperacion 	= "N";
				entradaLiquidarComprobante.p_UsaReglasNegocio 	= "S";
				entradaLiquidarComprobante.p_FechaMovimiento 	= in.p_FechaMovimiento;  	// Corresponde a Fecha Sistema	
			    
				
				for (ComprobanteFormaPago cfp : lstComprobanteALiquidar)
				{
					//Comprobantes a liquidar
					objComprobanteLiquidar = new ComprobanteLiquidar_Entrada();
					objComprobanteLiquidar.IdComprobante 		 = cfp.IdComprobante;
					objComprobanteLiquidar.IdTipoMovimiento 	 = cfp.IdTipoMovimiento;
					objComprobanteLiquidar.AfectaCuentaInversion = "CLI";
					
					if (!entradaLiquidarComprobante.p_ComprobanteLiquidar.contains(objComprobanteLiquidar))
						entradaLiquidarComprobante.p_ComprobanteLiquidar.add(objComprobanteLiquidar);
					
					//Formas de pago a liquidar					
					objFormaDePagoLiquidar = new FormaDePagoLiquidar_Entrada();					
					objFormaDePagoLiquidar.IdComprobante = cfp.IdComprobante;
					objFormaDePagoLiquidar.IdComprobanteFormaDePago = cfp.IdComprobanteFormaDePago;
					entradaLiquidarComprobante.p_FormaDePagoLiquidar.add(objFormaDePagoLiquidar);
									
				}
				
					
				
				
				LiquidarComprobante_Salida salidaLiquidarComprobante = servicioLiquidarComprobante.logica(con, entradaLiquidarComprobante);
				
				if (salidaLiquidarComprobante.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1)
				{
					logger.debug("LIQUIDAR COMPROBANTES CON ERRORES: " + salidaLiquidarComprobante.P_OUT_MENSAJES);
					out.P_OUT_MENSAJES.addAll(salidaLiquidarComprobante.P_OUT_MENSAJES);
					return out;
				}
				
		
			}
			else
			{
				//Marca los comprobantes forma de pago como depositados
				_sql.batch(con, SQL_UPDATE_COMPROBANTE_FORMA_DE_PAGO, registroDepositoProceso , RegistroDeposito_Proceso.class);
				
				//Actualizar saldo cuentas bancarias
				CuentaBanco_Entrada objCuentaBanco;
				List<CuentaBanco_Entrada> lstCuentaBanco = new ArrayList<CuentaBanco_Entrada>();
				
				for (ComprobanteFormaPago cfp : lstComprobanteFormaPago)
				{
					
					 if (cfp.IdCuentaBancariaCorredora.compareTo(0L) != 0)
					 { 
						 objCuentaBanco = new CuentaBanco_Entrada();	
						 objCuentaBanco.IdCuentaBanco = cfp.IdCuentaBancariaCorredora;
						 						 
						 if (!lstCuentaBanco.contains(objCuentaBanco))						
						    lstCuentaBanco.add(objCuentaBanco);
					 }
				}

				if (!lstCuentaBanco.isEmpty())
				{
					ActualizarSaldosCuentaBanco servicioActualizarSaldosCuentaBanco = new ActualizarSaldosCuentaBanco();
					ActualizarSaldosCuentaBanco_Entrada entradaActualizarSaldosCuentaBanco = new ActualizarSaldosCuentaBanco_Entrada();
					
					entradaActualizarSaldosCuentaBanco.p_FechaMovimiento = in.p_FechaMovimiento;
					entradaActualizarSaldosCuentaBanco.p_CuentaBanco = lstCuentaBanco;
					
					servicioActualizarSaldosCuentaBanco.logica(con, entradaActualizarSaldosCuentaBanco);
				}
			}
			
		}

	
		// -------------------------------------------------------------------------------------
		// PREPARA MENSAJES DE SALIDA
		String codigoMensajeExito = "";
		for (RegistroDeposito_Proceso deposito: registroDepositoProceso)
		{
			if (in.p_TipoEjecucion.equalsIgnoreCase("EDP"))
			{
				codigoMensajeExito = "ENVIO_NOMINA_A_DEPOSITAR_EXITO";
			}
			
			if (in.p_TipoEjecucion.equalsIgnoreCase("LIQ"))
			{
				if (deposito.Entidad.equalsIgnoreCase("TES.Deposito"))					
					codigoMensajeExito = "DEPOSITO_LIQUIDADO_EXITO";
				else						
					codigoMensajeExito = "DEPOSITO_DETALLE_LIQUIDADO_EXITO";
			}
			
			if (in.p_TipoEjecucion.equalsIgnoreCase("ANU"))
			{
				if (deposito.Entidad.equalsIgnoreCase("TES.Deposito"))					
					codigoMensajeExito = "DEPOSITO_ANULADO_EXITO";
				else					
					codigoMensajeExito = "DEPOSITO_DETALLE_ANULADO_EXITO";
			}


			String mensajeExito = Diccionario.getMensaje(con, in.p_IdEmpresa, codigoMensajeExito).add("Id", deposito.Id).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(deposito.Id, MensajeSalida.MENSAJE_OK, codigoMensajeExito, mensajeExito));
					
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		logger.info("SALIDA > " + out);	
		logger.info(":: TERMINO GestionDeDepositos ::");	
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
			// ENVIO A DEPOSITO
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2020-06-15\",\"p_TipoEjecucion\":\"EDP\",\"p_Registros\":[{\"Id\":194,\"Entidad\":\"TES.Deposito\",\"IdConcepto\":\"\",\"Descripcion\":\"\"}]}";			
						
			// ANULACION
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2020-06-15\",\"p_TipoEjecucion\":\"ANU\",\"p_Registros\":[{\"Id\":194,\"Entidad\":\"TES.Deposito\",\"IdConcepto\":\"ANUCDH\",\"Descripcion\":\"comengtario prueba\"}]}";
			
			// LIQUIDACION
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2020-06-15\",\"p_TipoEjecucion\":\"LIQ\",\"p_Registros\":[{\"Id\":194,\"Entidad\":\"TES.Deposito\",\"IdConcepto\":\"\",\"Descripcion\":\"\"}]}";
			
			GestionDeDepositos_Entrada Objin = gson.fromJson(json, GestionDeDepositos_Entrada.class);
			GestionDeDepositos demo = new GestionDeDepositos();
			
 			System.out.println("ENTRADA: " + Objin.toString());
 			
 			try 
 			{
 				GestionDeDepositos_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				con.commit();
 				//con.rollback();
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


