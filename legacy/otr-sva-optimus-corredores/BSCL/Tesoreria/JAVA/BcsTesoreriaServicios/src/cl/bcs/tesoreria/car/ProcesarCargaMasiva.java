package cl.bcs.tesoreria.car;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.map.MultiKeyMap;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearComprobante;
import cl.bcs.tesoreria.CrearComprobante.CrearComprobante_Entrada;
import cl.bcs.tesoreria.CrearComprobante.CrearComprobante_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ProcesarCargaMasiva implements JavaCall {

	private final Log log = new Log("TES.ProcesarCargaMasiva");
	

	private final String SQL_INSERT_CARGA_MASIVA_PROCESO   = loadResourceToString("cl/bcs/tesoreria/sql/ProcesarCargaMasiva_InsertCargaMasivaProceso.sql");
	private final String SQL_SELECT_CARGA_MASIVA_PROCESO   = loadResourceToString("cl/bcs/tesoreria/sql/ProcesarCargaMasiva_SelectCargaMasivaProceso.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO_CARGA = loadResourceToString("cl/bcs/tesoreria/sql/ProcesarCargaMasiva_SelectPreferenciaPagoCarga.sql");
	private final String SQL_SELECT_CARGA_MASIVA_DETALLE   = loadResourceToString("cl/bcs/tesoreria/sql/ProcesarCargaMasiva_SelectCargaMasivaDetalle.sql");	
	private final String SQL_UPDATE_CARGA_MASIVA           = loadResourceToString("cl/bcs/tesoreria/sql/ProcesarCargaMasiva_UpdateCargaMasiva.sql");
	private final String SQL_UPDATE_CARGA_MASIVA_DETALLE   = loadResourceToString("cl/bcs/tesoreria/sql/ProcesarCargaMasiva_UpdateCargaMasivaDetalle.sql");

	
    //-------------------------------------------------------------------------------------------
	// CLASES PARA ENTRADA
	//-------------------------------------------------------------------------------------------
	
	public static class ProcesarCargaMasiva_Entrada 
	{		
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String		p_IdModulo;
		public String		p_UsaReglasNegocio;
		public String		p_FechaProceso;  // Fecha de sistema
		public List<CargaMasivaProcesar> p_CargaMasivaProcesar;

		public ProcesarCargaMasiva_Entrada()
		{
			this.p_CargaMasivaProcesar = new ArrayList<CargaMasivaProcesar>();	
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CargaMasivaProcesar 
	{
		public Long			IdCargaMasiva;
		public String		CodigoTipoCargaMasiva;		   // Ej: CARGA_RECAUDACION_SERVIPAG / CARGA_GENERICA_MOVIMIENTOS
		public String		IdMoneda;
		public String		IdConcepto;
		public String		IdMedioDePago;
		public Long			IdCuentaBanco;	 	  
	 	  
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	
    //-------------------------------------------------------------------------------------------
	// CLASES PARA SALIDA
	//-------------------------------------------------------------------------------------------
	
	public static class ProcesarCargaMasiva_Salida {

		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;		
		
		public ProcesarCargaMasiva_Salida()
		{	
			this.P_OUT_MENSAJES 			 	= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	


	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------------------------------------------------

	
	public static class CargaMasiva_Proceso
	{
		public Long			IdCargaMasiva;
		public String		CodigoTipoCargaMasiva;
		public String		IdMoneda;
		public String		IdConcepto;
		public String		IdMedioDePago;
		public Long			IdCuentaBanco;		
		
		public Long			IdCargaMasiva_Real;
		public String		CodigoTipoCargaMasiva_Real;
		public String		IdEstado;	
		public String		FechaProceso;
		
		//Para validaciones
		public long 		errores = 0;
		public long 		advertencias = 0;
		public long 		autorizaciones = 0;
		public long 		ok = 0;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	public static class PreferenciaPagoCarga
	{
		public Long			IdCargaMasiva;
		public String		CodigoTipoCargaMasiva;
		public Long			IdPersonaEntidadRecaudadora;		
		public String		NombreCompletoEntidadRecaudadora;
		public String		DocumentoIdentidadEntidadRecaudadora;
		public String		TipoDocumentoIdentidadEntidadRecaudadora;
		public String		IdMoneda;
		public String		IdConcepto;
		public String		IdMedioDePago;
		public String		IdModalidadDePago;
		public Long			IdCuentaBanco;
		public String		ProfundidadProceso;
		public String		GeneraPartida;    // Indicador si se genera partida de ingreso/egreso	

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	public static class CargaMasivaDetalle
	{
		public Long			IdCargaMasiva;
		public Long			IdCargaMasivaDetalle;
		public String		CodigoTipoCargaMasiva;
		public String		IdTipoMovimiento;
	    public String		DocumentoIdentidad;
		public BigDecimal   Monto;
		public Long			IdPersona;
		public Long			IdPortafolio;
		public String		Glosa;
		public Long			IdIngresoEgreso = 0L;
	
		
		// Datos para carga "CONCILIACION_RECAUDACION_BCP" (Peru)
		public String		BCP_CON_FechaTransaccion;
		public Long			BCP_CON_NumeroOperacion;			
		public String		ExisteMovimiento;
		public Long			IdMovimientoExistente;

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PartidaIngresoEgreso
	{	
		public Long			IdPartidaReferencial = 0L;		
		public Long			IdCargaMasiva;
		public String		IdTipoMovimiento;
		public String		IdMoneda;
		public Long			IdIngresoEgreso;
		public String		IdMedioDePago;
		public String		IdModalidadDePago;
		public BigDecimal   Monto;
		public Long			IdCuentaBanco;
	
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
		
		ProcesarCargaMasiva_Entrada in = gson.fromJson(jin.toString(), ProcesarCargaMasiva_Entrada.class);
		ProcesarCargaMasiva_Salida out = logica(con, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODOS COMPLEMANTARIOS
	//-------------------------------------------------------------------------------------------
	
	private void ProcesaRequisito(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, CargaMasiva_Proceso cargaProceso, List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		if (esError) {
			String msg=Diccionario.getMensaje(con,idEmpresa,codMsg)
				  .add("IdCargaMasiva", cargaProceso.IdCargaMasiva)
				  .msg();
			
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cargaProceso.IdCargaMasiva,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				cargaProceso.errores++;
			}else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cargaProceso.IdCargaMasiva,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				cargaProceso.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cargaProceso.IdCargaMasiva,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				cargaProceso.advertencias++;
			}
			else {
				cargaProceso.ok++;
			}
		}else {
			cargaProceso.ok++;
		}		
	}

	
	public void limpiarCargas(List<CargaMasiva_Proceso> cargaProceso, List<CargaMasiva_Proceso> cargaConErrores) {
		for (CargaMasiva_Proceso carga : cargaProceso) {
			if (carga.errores > 0) {
				cargaConErrores.add(carga);
			}
		}
		
		for (CargaMasiva_Proceso cargaErr : cargaConErrores) {
			if (cargaProceso.contains(cargaErr))
				cargaProceso.remove(cargaErr);
		}
	}
	
	
    
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------
	
	public ProcesarCargaMasiva_Salida logica(Connection con, ProcesarCargaMasiva_Entrada in) throws Exception {
		
		log.write("== INICIO PROCESAR CARGA MASIVA  ==");		
		log.write("ENTRADA > " + in.toString());
		
		SQL _sql = new SQL();
		ProcesarCargaMasiva_Salida out = new ProcesarCargaMasiva_Salida();
		
		List<CargaMasiva_Proceso> cargaMasiva_Proceso = new ArrayList<>();
		List<CargaMasiva_Proceso> cargaMasiva_ConErrores = new ArrayList<>();		
		HashMap<String,Object> params = new HashMap<>();
		HashMap<Long, Object> hashIngresoEgresoGenerado = new HashMap<Long, Object>();
		
		
		//Si usa regla de negocio realiza validaciones basicas
		if ("S".equalsIgnoreCase(in.p_UsaReglasNegocio))
		{
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				log.write("NO CUMPLE REQUISITOS BASICOS");
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
						
			if (in.p_CargaMasivaProcesar.isEmpty()) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_CARGAS",null,con, in.p_IdEmpresa));
				return out;
			}
		}
			
		//Inserta parametros de entrada a tabla global
		_sql.batch(con, SQL_INSERT_CARGA_MASIVA_PROCESO, in.p_CargaMasivaProcesar , CargaMasivaProcesar.class);
		
		
		//Obtiene datos de carga masiva para proceso
		params.clear();
		for (CargaMasivaProcesar carga : in.p_CargaMasivaProcesar) {
			params.put("IdCargaMasiva", carga.IdCargaMasiva);
		}	
			
		cargaMasiva_Proceso = _sql.query(con, SQL_SELECT_CARGA_MASIVA_PROCESO, params, CargaMasiva_Proceso.class);
		log.write("DATOS PROCESO > " + cargaMasiva_Proceso);
		

		// --------------------------------------------------------------------------------
		// OBTIENE PREFERENCIAS DE PAGO / COMPLEMENTO
		// Para las preferencias de pago Toma los parametros de entrada(que provienen de la UI complemento) 
		// Si estos vienen vacios, toma la preferencia de pago configurada por carga		

		List<PreferenciaPagoCarga> lstPreferenciaPagoCarga = new ArrayList<PreferenciaPagoCarga>();
		
		params.put("IdEmpresa", in.p_IdEmpresa);		
		lstPreferenciaPagoCarga = _sql.query(con, SQL_SELECT_PREFERENCIA_PAGO_CARGA, params, PreferenciaPagoCarga.class);
		log.write("PREFERENCIA PAGO > " + lstPreferenciaPagoCarga);
		
		HashMap<Long,Object> hashPreferenciaPagoCarga = Utils.List2Hash(lstPreferenciaPagoCarga, "IdCargaMasiva");
		
		
		//Si usa regla de negocio realiza validaciones de negocio
		if ("S".equalsIgnoreCase(in.p_UsaReglasNegocio))
		{
			for (CargaMasiva_Proceso carga : cargaMasiva_Proceso) 
			{				
				PreferenciaPagoCarga infoPreferenciaPago = (PreferenciaPagoCarga) hashPreferenciaPagoCarga.get(carga.IdCargaMasiva);
				
				ProcesaRequisito(con, in.p_IdEmpresa,(carga.IdCargaMasiva_Real.compareTo(0L) == 0), "CARGA_MASIVA_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, carga, out.P_OUT_MENSAJES);				
				ProcesaRequisito(con, in.p_IdEmpresa,(carga.CodigoTipoCargaMasiva_Real.equalsIgnoreCase("")), "TIPO_CARGA_MASIVA_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, carga, out.P_OUT_MENSAJES);				
				ProcesaRequisito(con, in.p_IdEmpresa,("PRO".equalsIgnoreCase(carga.IdEstado)), "CARGA_MASIVA_PROCESADA", MensajeSalida.MENSAJE_ERROR, carga, out.P_OUT_MENSAJES);
				ProcesaRequisito(con, in.p_IdEmpresa,("INV".equalsIgnoreCase(carga.IdEstado)), "CARGA_MASIVA_INVALIDA", MensajeSalida.MENSAJE_ERROR, carga, out.P_OUT_MENSAJES);
			
				if (carga.CodigoTipoCargaMasiva_Real.equalsIgnoreCase("CARGA_RECAUDACION_SERVIPAG") || carga.CodigoTipoCargaMasiva_Real.equalsIgnoreCase("CONCILIACION_RECAUDACION_BCP") || carga.CodigoTipoCargaMasiva_Real.equalsIgnoreCase("EGRESOS_POR_ABONOS_BCP_PAC"))
				{
					ProcesaRequisito(con, in.p_IdEmpresa,(infoPreferenciaPago.CodigoTipoCargaMasiva.equalsIgnoreCase("")), "TIPO_CARGA_MASIVA_SIN_CONFIGURACION", MensajeSalida.MENSAJE_ERROR, carga, out.P_OUT_MENSAJES);				
					ProcesaRequisito(con, in.p_IdEmpresa,(carga.CodigoTipoCargaMasiva_Real.equalsIgnoreCase("CARGA_RECAUDACION_SERVIPAG") && infoPreferenciaPago.IdPersonaEntidadRecaudadora.compareTo(0L)==0),     "CONFIGURACION_TIPO_CARGA_MASIVA_INVALIDA", MensajeSalida.MENSAJE_ERROR, carga, out.P_OUT_MENSAJES);
					ProcesaRequisito(con, in.p_IdEmpresa,(infoPreferenciaPago.IdConcepto.equalsIgnoreCase("")),    "CONFIGURACION_TIPO_CARGA_MASIVA_INVALIDA", MensajeSalida.MENSAJE_ERROR, carga, out.P_OUT_MENSAJES);
					ProcesaRequisito(con, in.p_IdEmpresa,(infoPreferenciaPago.IdCuentaBanco.compareTo(0L)==0),     "CONFIGURACION_TIPO_CARGA_MASIVA_INVALIDA", MensajeSalida.MENSAJE_ERROR, carga, out.P_OUT_MENSAJES);
					ProcesaRequisito(con, in.p_IdEmpresa,(infoPreferenciaPago.IdMedioDePago.equalsIgnoreCase("")), "CONFIGURACION_TIPO_CARGA_MASIVA_INVALIDA", MensajeSalida.MENSAJE_ERROR, carga, out.P_OUT_MENSAJES);
				}				
			}
				
			limpiarCargas(cargaMasiva_Proceso, cargaMasiva_ConErrores);
			
			if (cargaMasiva_Proceso.isEmpty()) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "CARGAS_MASIVAS_CON_ERRORES", null, con, in.p_IdEmpresa));
				return out;
			}
		}	
		
		

		//obtener cargas sin errores de validacion
		List<Long> idsCargaOk = new ArrayList<>();
		
		for (CargaMasiva_Proceso carga : cargaMasiva_Proceso) {			
			if (!idsCargaOk.contains(carga.IdCargaMasiva)) {
				idsCargaOk.add(carga.IdCargaMasiva);
			}
		}
		
		
		// --------------------------------------------------------------------------------
		// OBTIENE DETALLE DE CARGA MASIVA A PROCESAR
		
		List<CargaMasivaDetalle> CargaMasivaDetalle = new ArrayList<CargaMasivaDetalle>();
		
		params.clear();
		params.put("IdCargaMasiva", idsCargaOk);
		
		CargaMasivaDetalle = _sql.query(con, SQL_SELECT_CARGA_MASIVA_DETALLE, params, CargaMasivaDetalle.class);
		log.write("CARGA MASIVA DETALLE > " + CargaMasivaDetalle);
		
		
		// --------------------------------------------------------------------------------
		// GENERA MOVIMIENTOS DE TESORERIA (Para cada cliente)
		
		CrearIngresoEgreso servicioCrearIngresoEgreso = new CrearIngresoEgreso();
		CrearIngresoEgreso_Entrada entradaCrearIngresoEgreso = new CrearIngresoEgreso_Entrada();
		List<IngresoEgreso_Entrada> lstEntradaIngresoEgreso = new ArrayList<IngresoEgreso_Entrada>();
		List<FormaDePago_Entrada>   lstEntradaFormaDePago = new ArrayList<FormaDePago_Entrada>();
		
		
		IngresoEgreso_Entrada objIngresoEgreso;
		FormaDePago_Entrada   objFormaDePago;
		List<Long> idsCargaMasivaDetalleEnviadosTesoreria = new ArrayList<>();
		
		for (CargaMasivaDetalle cargaDetalle : CargaMasivaDetalle)
		{

			objIngresoEgreso = new IngresoEgreso_Entrada();				
			PreferenciaPagoCarga infoPreferenciaPago = (PreferenciaPagoCarga) hashPreferenciaPagoCarga.get(cargaDetalle.IdCargaMasiva);

			if (cargaDetalle.ExisteMovimiento.equalsIgnoreCase("N") && !infoPreferenciaPago.IdMoneda.equalsIgnoreCase(""))
			{				
				objIngresoEgreso.NumeroReferencia 		= cargaDetalle.IdCargaMasivaDetalle;
				objIngresoEgreso.IdTipoMovimiento 		= cargaDetalle.IdTipoMovimiento;   	// MOVING, MOVEGR
				objIngresoEgreso.IdTipoPersona 			= "CLI";  		 					// Corresponde al IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
			   	objIngresoEgreso.IdPersona 				= cargaDetalle.IdPersona;   		 			
			   	objIngresoEgreso.IdPortafolio 			= cargaDetalle.IdPortafolio;   		 		
			   	objIngresoEgreso.IdMoneda 				= infoPreferenciaPago.IdMoneda;  		 			
			   	objIngresoEgreso.Monto 					= cargaDetalle.Monto;		 				
			   	objIngresoEgreso.FechaLiquidacion 		= in.p_FechaProceso;  		 		                                                                     
			   	objIngresoEgreso.IdConcepto 			= infoPreferenciaPago.IdConcepto;		   	
			   	objIngresoEgreso.IdTipoPersonaTitularPago = "CLI";  		// Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
			   	objIngresoEgreso.IdPersonaTitularPago 	= cargaDetalle.IdPersona;
			   	objIngresoEgreso.IdPortafolioTitularPago = cargaDetalle.IdPortafolio;
			   	objIngresoEgreso.IdCaja 				= 0L;
			   	objIngresoEgreso.SecuenciaAgrupacion 	= "";
			   	objIngresoEgreso.SecuenciaRelacionada 	= null;
			   	objIngresoEgreso.Glosa 					= cargaDetalle.Glosa;  		 			
			   	
			    //Parametros Referenciales(Opcionales)
			   	objIngresoEgreso.IdEmpleadoDigitador 	= 0L;
			   	objIngresoEgreso.IdSucursalEmpleadoDigitador = 0L;
			   	objIngresoEgreso.IdTipoProducto    		= "";
			   	objIngresoEgreso.IdTipoOperacion   		= "";		
			   	objIngresoEgreso.IdTipoComprobante 		= (cargaDetalle.CodigoTipoCargaMasiva.contentEquals("CONCILIACION_RECAUDACION_BCP") ? "BAT" : "");
			   	objIngresoEgreso.IdComprobante     		= (cargaDetalle.CodigoTipoCargaMasiva.contentEquals("CONCILIACION_RECAUDACION_BCP") ? cargaDetalle.BCP_CON_NumeroOperacion : 0L);
			   	objIngresoEgreso.FechaComprobante  		= (cargaDetalle.CodigoTipoCargaMasiva.contentEquals("CONCILIACION_RECAUDACION_BCP") ? cargaDetalle.BCP_CON_FechaTransaccion : "");			   
			   	objIngresoEgreso.FechaLiquidacionComprobante = "0001-01-01";								    
			   	objIngresoEgreso.IdOperacion			= 0L; 		
			   	objIngresoEgreso.FechaOperacion 		= "0001-01-01";
			   	objIngresoEgreso.IdCondicionLiquidacion = "";
			   	objIngresoEgreso.IdBolsa 				= "";
			   	objIngresoEgreso.IdMonedaOperacion 		= "";
			   	objIngresoEgreso.MontoOperacion			= BigDecimal.ZERO;
			   	objIngresoEgreso.IdTipoReajuste 		= "";
			   	objIngresoEgreso.FechaParidad 			= "0001-01-01";
			   	objIngresoEgreso.ValorParidad			= BigDecimal.ZERO;
			   	objIngresoEgreso.FolioCartera			= 0L;		    
			   	objIngresoEgreso.IdInstrumento 			= 0L;
			   	objIngresoEgreso.IdTipoInstrumento 		= "";
			   	objIngresoEgreso.IdClasificacionTipoInstrumento = "";		   	
			   	objIngresoEgreso.Bilateral 				= "N";	
			   	objIngresoEgreso.MueveCuentaInversion	= "S";	
			   	objIngresoEgreso.GeneraComprobante 		= ((infoPreferenciaPago.ProfundidadProceso.equalsIgnoreCase("COM") || infoPreferenciaPago.ProfundidadProceso.equalsIgnoreCase("LIQ")) ? "S" : "N");
			    objIngresoEgreso.AgruparEnComprobante 	= "N";
			    objIngresoEgreso.TitularPago 			= "N";	
		
			    lstEntradaIngresoEgreso.add(objIngresoEgreso);
			    

			    if (!infoPreferenciaPago.IdMedioDePago.equalsIgnoreCase(""))
			    {
				    objFormaDePago = new FormaDePago_Entrada();
				    objFormaDePago.NumeroReferencia				= cargaDetalle.IdCargaMasivaDetalle;
				    objFormaDePago.NumeroReferenciaFormaDePago	= cargaDetalle.IdCargaMasivaDetalle;
				    objFormaDePago.IdMedioDePago				= infoPreferenciaPago.IdMedioDePago;
				    objFormaDePago.IdModalidadDePago			= infoPreferenciaPago.IdModalidadDePago;
				    objFormaDePago.IdMoneda						= infoPreferenciaPago.IdMoneda;
				    objFormaDePago.Monto						= cargaDetalle.Monto;
				    objFormaDePago.IdCuentaBancariaCorredora	= infoPreferenciaPago.IdCuentaBanco;
				    objFormaDePago.IdTarjetaBancariaCorredora 	= 0L;
				    objFormaDePago.IdChequera 					= 0L;
				    objFormaDePago.NumeroDocumentoCorredora 	= 0L;
				    objFormaDePago.FechaDocumentoPagoCorredora	= "0001-01-01";
				    objFormaDePago.IdCuentaBancariaSocio 		= 0L;
				    objFormaDePago.IdBancoSocio 				= 0L;
				    objFormaDePago.NombreCuentaBancariaSocio 	= "";
				    objFormaDePago.NumeroCuentaBancariaSocio 	= "";
				    objFormaDePago.IdTipoCuentaBancariaSocio 	= "";
				    objFormaDePago.NombreTipoCuentaBancariaSocio = "";
				    objFormaDePago.IdTarjetaBancariaSocio 		= 0L;
				    objFormaDePago.IdTipoTarjetaBancariaSocio 	= "";
				    objFormaDePago.NombreTipoTarjetaBancariaSocio = "";
				    objFormaDePago.NumeroTarjetaBancariaSocio 	= "";
				    objFormaDePago.NumeroSerieSocio 			= "";
				    objFormaDePago.NumeroDocumentoSocio 		= 0L;
				    objFormaDePago.FechaDocumentoPagoSocio 		= "";
								
				    lstEntradaFormaDePago.add(objFormaDePago);
				    idsCargaMasivaDetalleEnviadosTesoreria.add(cargaDetalle.IdCargaMasivaDetalle);
			    }  // if (!infoPreferenciaPago.IdMedioDePago.equalsIgnoreCase(""))
		
			    
			}  // if (cargaDetalle.ExisteMovimiento.equalsIgnoreCase("N") && !infoPreferenciaPago.IdMoneda.equalsIgnoreCase(""))
		}
		
		
		if (!lstEntradaIngresoEgreso.isEmpty())
		{
			entradaCrearIngresoEgreso.p_IdEmpresa 			 = in.p_IdEmpresa;
			entradaCrearIngresoEgreso.p_IdUsuario 			 = in.p_IdUsuario;
			entradaCrearIngresoEgreso.p_IdModulo 			 = in.p_IdModulo;
			entradaCrearIngresoEgreso.p_SimularOperacion 	 = "N";
			entradaCrearIngresoEgreso.p_UsaReglasNegocio 	 = in.p_UsaReglasNegocio;
			entradaCrearIngresoEgreso.p_EsIngresoAutomatico  = "S";
			entradaCrearIngresoEgreso.p_FechaMovimiento 	 = in.p_FechaProceso; 
			entradaCrearIngresoEgreso.p_IngresoEgresoGenerar = lstEntradaIngresoEgreso;
			entradaCrearIngresoEgreso.p_FormaDePago 		 = lstEntradaFormaDePago;
						
			CrearIngresoEgreso_Salida salidaCrearIngresoEgreso = servicioCrearIngresoEgreso.logica(con, entradaCrearIngresoEgreso);
			out.P_OUT_MENSAJES.addAll(salidaCrearIngresoEgreso.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
			if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) > 0)
			{
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "ERROR_AL_GENERAR_MOVIMIENTO_TESORERIA", null, con, in.p_IdEmpresa));
				return out;
			}
			
		
			//Obtiene los Id de los movimientos generados
			List<IngresoEgresoGenerado_Salida> salidaIngresoEgresoGenerado = salidaCrearIngresoEgreso.P_OUT_INGRESOEGRESOGENERADO;			
			hashIngresoEgresoGenerado =  Utils.List2Hash(salidaIngresoEgresoGenerado, "NumeroReferencia");
		}
		

		
		// --------------------------------------------------------------------------------
		// GENERA PARTIDAS DE INGRESO/EGRESO (solo para algunos tipos de carga)
		
		List<PartidaIngresoEgreso> 	lstPartidaDetalle 			= new ArrayList<PartidaIngresoEgreso>();
		CrearComprobante 			servicioCrearComprobante 	= new CrearComprobante();		
		CrearComprobante_Entrada 	entradaCrearComprobante 	= new CrearComprobante_Entrada();
		
		List<cl.bcs.tesoreria.CrearComprobante.IngresoEgresoGenerar_Entrada>	lstEntradaComprobanteIngresoEgresoGenerar = new ArrayList<cl.bcs.tesoreria.CrearComprobante.IngresoEgresoGenerar_Entrada>();
		List<cl.bcs.tesoreria.CrearComprobante.ComprobanteGenerar_Entrada>		lstEntradaComprobanteGenerar = new ArrayList<cl.bcs.tesoreria.CrearComprobante.ComprobanteGenerar_Entrada>();
		List<cl.bcs.tesoreria.CrearComprobante.FormaDePago_Entrada>				lstEntradaComprobanteFormaDePago = new ArrayList<cl.bcs.tesoreria.CrearComprobante.FormaDePago_Entrada>();
	
		
		
		for (CargaMasivaDetalle cargaDetalle : CargaMasivaDetalle)
		{
			PreferenciaPagoCarga infoPreferenciaPago = (PreferenciaPagoCarga) hashPreferenciaPagoCarga.get(cargaDetalle.IdCargaMasiva);
			IngresoEgresoGenerado_Salida infoIngresoEgresoGenerado = (IngresoEgresoGenerado_Salida) hashIngresoEgresoGenerado.get(cargaDetalle.IdCargaMasivaDetalle);
						
			if (infoPreferenciaPago.GeneraPartida.equalsIgnoreCase("S") && infoIngresoEgresoGenerado != null)	
			{
				PartidaIngresoEgreso objPartidaDetalle = new PartidaIngresoEgreso();	
				objPartidaDetalle.IdCargaMasiva = cargaDetalle.IdCargaMasiva;
				objPartidaDetalle.IdTipoMovimiento = cargaDetalle.IdTipoMovimiento;
				objPartidaDetalle.IdMoneda = infoPreferenciaPago.IdMoneda;
				objPartidaDetalle.IdIngresoEgreso = infoIngresoEgresoGenerado.IdIngresoEgreso;
				objPartidaDetalle.IdMedioDePago = infoPreferenciaPago.IdMedioDePago;
				objPartidaDetalle.IdModalidadDePago = infoPreferenciaPago.IdModalidadDePago;
				objPartidaDetalle.Monto = cargaDetalle.Monto;
				objPartidaDetalle.IdCuentaBanco = infoPreferenciaPago.IdCuentaBanco;
				
				lstPartidaDetalle.add(objPartidaDetalle);
			}
		}
			
		if (!lstPartidaDetalle.isEmpty())
		{	
			ArrayList<PartidaIngresoEgreso> lstPartida = new ArrayList<PartidaIngresoEgreso>();
			MultiKeyMap mapPartida = new MultiKeyMap();
			
			for (PartidaIngresoEgreso partidaDetalle : lstPartidaDetalle)
			{
				PartidaIngresoEgreso infoPartida = (PartidaIngresoEgreso) mapPartida.get(partidaDetalle.IdCargaMasiva, partidaDetalle.IdTipoMovimiento, partidaDetalle.IdMoneda, partidaDetalle.IdMedioDePago,partidaDetalle.IdCuentaBanco);	
				
				if (infoPartida == null) //Si no existe, lo agregamos
				{
					lstPartida.add(partidaDetalle);					
					mapPartida.put(partidaDetalle.IdCargaMasiva, partidaDetalle.IdTipoMovimiento,partidaDetalle.IdMoneda, partidaDetalle.IdMedioDePago,partidaDetalle.IdCuentaBanco,partidaDetalle);
				}
				else
				{
					PartidaIngresoEgreso newPartida = (PartidaIngresoEgreso) mapPartida.get(partidaDetalle.IdCargaMasiva, partidaDetalle.IdTipoMovimiento,partidaDetalle.IdMoneda, partidaDetalle.IdMedioDePago,partidaDetalle.IdCuentaBanco);
					newPartida.Monto	= newPartida.Monto.add(partidaDetalle.Monto);
				}
				
			}

		
			// Actualizamos partida con el monto total que corresponde
			Long idPartidaReferencial = 1L;
			for(PartidaIngresoEgreso partida : lstPartida){				
				PartidaIngresoEgreso infoPartida = (PartidaIngresoEgreso) mapPartida.get (partida.IdCargaMasiva, partida.IdTipoMovimiento,partida.IdMoneda, partida.IdMedioDePago, partida.IdCuentaBanco); 				
				partida.IdPartidaReferencial = idPartidaReferencial;
				partida.Monto 	= infoPartida.Monto;
				idPartidaReferencial++;
			}
			

			for (PartidaIngresoEgreso partidaDetalle : lstPartidaDetalle)
			{
				PartidaIngresoEgreso infoPartida = (PartidaIngresoEgreso) mapPartida.get (partidaDetalle.IdCargaMasiva, partidaDetalle.IdTipoMovimiento,partidaDetalle.IdMoneda, partidaDetalle.IdMedioDePago, partidaDetalle.IdCuentaBanco); 	
			
				cl.bcs.tesoreria.CrearComprobante.IngresoEgresoGenerar_Entrada	objComprobanteIngresoEgresoGenerar = new cl.bcs.tesoreria.CrearComprobante.IngresoEgresoGenerar_Entrada();
				
				objComprobanteIngresoEgresoGenerar.NumeroReferencia = infoPartida.IdPartidaReferencial;
				objComprobanteIngresoEgresoGenerar.TipoComprobante  = (partidaDetalle.IdTipoMovimiento.equalsIgnoreCase("MOVING") ? "PARING" : "PAREGR");
				objComprobanteIngresoEgresoGenerar.IdIngresoEgreso  = partidaDetalle.IdIngresoEgreso;
				
				lstEntradaComprobanteIngresoEgresoGenerar.add(objComprobanteIngresoEgresoGenerar);
			}
			
			for(PartidaIngresoEgreso partida : lstPartida)
			{		
				cl.bcs.tesoreria.CrearComprobante.FormaDePago_Entrada	objComprobanteFormaDePago = new cl.bcs.tesoreria.CrearComprobante.FormaDePago_Entrada();
			
				objComprobanteFormaDePago.NumeroReferencia = partida.IdPartidaReferencial;
				objComprobanteFormaDePago.IdMedioDePago = partida.IdMedioDePago; 
				objComprobanteFormaDePago.IdModalidadDePago = partida.IdModalidadDePago; 
				objComprobanteFormaDePago.IdMoneda = partida.IdMoneda; 
				objComprobanteFormaDePago.Monto = partida.Monto; 
				objComprobanteFormaDePago.IdCuentaBancariaCorredora = partida.IdCuentaBanco;				
				objComprobanteFormaDePago.IdTarjetaBancariaCorredora = 0L;
				objComprobanteFormaDePago.IdChequera = 0L;
				objComprobanteFormaDePago.NumeroDocumentoCorredora = 0L;
				objComprobanteFormaDePago.FechaDocumentoPagoCorredora = "0001-01-01";
				objComprobanteFormaDePago.IdCuentaBancariaSocio = 0L;
				objComprobanteFormaDePago.IdBancoSocio = 0L;
				objComprobanteFormaDePago.NombreCuentaBancariaSocio = "";
				objComprobanteFormaDePago.NumeroCuentaBancariaSocio = "";
				objComprobanteFormaDePago.IdTipoCuentaBancariaSocio = "";
				objComprobanteFormaDePago.NombreTipoCuentaBancariaSocio = "";
				objComprobanteFormaDePago.IdTarjetaBancariaSocio = 0L;
				objComprobanteFormaDePago.IdTipoTarjetaBancariaSocio = "";
				objComprobanteFormaDePago.NombreTipoTarjetaBancariaSocio = "";
				objComprobanteFormaDePago.NumeroTarjetaBancariaSocio = "";
				objComprobanteFormaDePago.NumeroSerieSocio = "";
				objComprobanteFormaDePago.NumeroDocumentoSocio = 0L;
				objComprobanteFormaDePago.FechaDocumentoPagoSocio = "0001-01-01";
				
				lstEntradaComprobanteFormaDePago.add(objComprobanteFormaDePago);
			}
			

			
			if (!lstEntradaComprobanteIngresoEgresoGenerar.isEmpty())
			{
				entradaCrearComprobante.p_IdEmpresa 		= in.p_IdEmpresa;  
				entradaCrearComprobante.p_IdUsuario 		= in.p_IdUsuario;
				entradaCrearComprobante.p_IdModulo			= in.p_IdModulo;   
				entradaCrearComprobante.p_SimularOperacion 	= "N";
				entradaCrearComprobante.p_UsaReglasNegocio 	= "S";    
				entradaCrearComprobante.p_FechaMovimiento 	= in.p_FechaProceso;			
				entradaCrearComprobante.p_IdCaja 			= 0L;	
				entradaCrearComprobante.p_GenerarComprobanteDesdeMovimientos  = "S";	
				entradaCrearComprobante.p_GenerarComprobanteDesdeComprobantes = "N";
				entradaCrearComprobante.p_EsIngresoAutomatico 	= "S";			
				entradaCrearComprobante.p_IngresoEgresoGenerar 	= lstEntradaComprobanteIngresoEgresoGenerar;
				entradaCrearComprobante.p_ComprobanteGenerar 	= lstEntradaComprobanteGenerar;
				entradaCrearComprobante.p_FormaDePago 			= lstEntradaComprobanteFormaDePago;	
				
				CrearComprobante_Salida salidaCrearComprobante = servicioCrearComprobante.logica(con, entradaCrearComprobante);
				out.P_OUT_MENSAJES.addAll(salidaCrearComprobante.P_OUT_MENSAJES);
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				
				if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) > 0)
				{
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "ERROR_AL_GENERAR_PARTIDA_INGRESO_EGRESO", null, con, in.p_IdEmpresa));
					return out;
				}
			}
			
		} // if (!lstPartidaDetalle.isEmpty())

		
		
		// --------------------------------------------------------------------------------
		// ACTUALIZA CARGA MASIVA		

		List<CargaMasiva_Proceso> lstCargaActualizar = new ArrayList<CargaMasiva_Proceso>();
		
		for (Long idCarga: idsCargaOk)
		{
			PreferenciaPagoCarga infoPreferenciaPago = (PreferenciaPagoCarga) hashPreferenciaPagoCarga.get(idCarga);
				
			CargaMasiva_Proceso objCarga = new CargaMasiva_Proceso();
			objCarga.IdCargaMasiva 	= idCarga;
			objCarga.IdMoneda 		= infoPreferenciaPago.IdMoneda;
			objCarga.IdConcepto 	= infoPreferenciaPago.IdConcepto;
			objCarga.FechaProceso 	= in.p_FechaProceso;
			lstCargaActualizar.add(objCarga);
		}
		
		if (!lstCargaActualizar.isEmpty())
		{
			_sql.batch(con, SQL_UPDATE_CARGA_MASIVA, lstCargaActualizar , CargaMasiva_Proceso.class);	
		}
		
		
		// --------------------------------------------------------------------------------
		// ACTUALIZA CARGA MASIVA DETALLE
		
		for (CargaMasivaDetalle cargaDetalle : CargaMasivaDetalle)
		{			
			IngresoEgresoGenerado_Salida infoIngresoEgresoGenerado = (IngresoEgresoGenerado_Salida) hashIngresoEgresoGenerado.get(cargaDetalle.IdCargaMasivaDetalle);
			cargaDetalle.IdIngresoEgreso = infoIngresoEgresoGenerado != null ? infoIngresoEgresoGenerado.IdIngresoEgreso : 0L;
		}
		
		if (!CargaMasivaDetalle.isEmpty())
		{
			_sql.batch(con, SQL_UPDATE_CARGA_MASIVA_DETALLE, CargaMasivaDetalle , CargaMasivaDetalle.class);
		}
		
	
		
		log.write("SALIDA > " + out);
		
		log.write("== TERMINO PROCESAR CARGA MASIVA ==");
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
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":14,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_CargaMasivaProcesar\":[{\"IdCargaMasiva\":58,\"CodigoTipoCargaMasiva\":\"CARGA_GENERICA_MOVIMIENTOS\",\"IdMoneda\":\"CLP\",\"IdConcepto\":\"ABLFDI\",\"IdMedioDePago\":\"TRA\",\"IdCuentaBanco\":2982}],\"p_FechaProceso\":\"2020-04-02\"}";			

			ProcesarCargaMasiva_Entrada Objin = gson.fromJson(json, ProcesarCargaMasiva_Entrada.class);
			ProcesarCargaMasiva demo = new ProcesarCargaMasiva();
			
 			System.out.println("ENTRADA: " + Objin.toString());
 			
 			try 
 			{
 				ProcesarCargaMasiva_Salida ObjOut = demo.logica(con, Objin);
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

