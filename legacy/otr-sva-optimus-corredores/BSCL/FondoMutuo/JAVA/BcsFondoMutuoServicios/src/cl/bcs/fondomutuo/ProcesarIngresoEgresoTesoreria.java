package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ModalidadPago;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.ResultadoProceso_Salida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ProcesarIngresoEgresoTesoreria implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(ProcesarIngresoEgresoTesoreria.class);
	private final String SQL_SELECT_MODALIDAD_PAGO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectModalidadPago.sql");
	private final String SQL_SELECT_Empleado = "SELECT COALESCE(\"IdEntidad\",0) AS \"IdEmpleado\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"TipoEntidad\" = 'EMP' AND \"IdUsuario\" = :IdUsuario";
	
	private final String SQL_SELECT_PreferenciaPagoPortafolio = "SELECT \"IdPreferenciaPagoPortafolio\", \"IdCuentaBancaria\", \"IdBancoCuentaBancaria\", \"NumeroCuentaBancaria\", \"IdTipoCuentaBancaria\", \"NombreTipoCuentaBancaria\" FROM \"BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio\" WHERE \"IdPreferenciaPagoPortafolio\" IN (:IdPreferenciaPagoPortafolio)";
	

	private final String SQL_SELECT_PreferenciaPagoCorredor = "SELECT \"IdTipoMovimiento\", \"IdConcepto\", \"IdMedioDePago\", \"IdCuentaBanco\", \"IdMoneda\" FROM \"BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor\" AS PRE WHERE \"IdConcepto\" = 'RESCFM' AND \"NombreEntidad\" = 'FFM.RescatePorTraspaso' AND \"IdEstado\" = 'ACT' AND \"IdEmpresa\" = :IdEmpresa";
	
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaProcesarIngresoEgresoTesoreria{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_SimularOperacion;
		public String						p_UsaReglasNegocio;		
		public String                       p_EsIngresoAutomatico;
		List<SolicitudAporteRescate> p_Solicitudes;
		
		public EntradaProcesarIngresoEgresoTesoreria(){
			this.p_Solicitudes = new ArrayList<SolicitudAporteRescate>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaProcesarIngresoEgresoTesoreria {

		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<IngresoEgresoGenerado_Salida>	P_OUT_INGRESOEGRESOGENERADO;
		public List<ResultadoProceso_Salida>        P_OUT_RESULTADOPROCESO;
		
		public SalidaProcesarIngresoEgresoTesoreria()
		{	
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_INGRESOEGRESOGENERADO = new ArrayList<IngresoEgresoGenerado_Salida>();
			this.P_OUT_RESULTADOPROCESO		 = new ArrayList<ResultadoProceso_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	public static class Empleado{
		public Long		IdEmpleado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	


	public static class PreferenciaPagoCorredor{		
		public String		IdTipoMovimiento;
		public String		IdConcepto;
		public String		IdMedioDePago;
		public Long			IdCuentaBanco;
		public String		IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	
	public static class PreferenciaPagoPortafolio{
		
		public Long    IdPreferenciaPagoPortafolio;
		public Long    IdCuentaBancaria;
		public Long    IdBancoCuentaBancaria;
		public String  NumeroCuentaBancaria;
		public String  IdTipoCuentaBancaria;
		public String  NombreTipoCuentaBancaria;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaProcesarIngresoEgresoTesoreria jin = gson.fromJson(in.toString(), EntradaProcesarIngresoEgresoTesoreria.class);
		SalidaProcesarIngresoEgresoTesoreria out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	@Trace
	public SalidaProcesarIngresoEgresoTesoreria logica(Connection con, EntradaProcesarIngresoEgresoTesoreria entrada) throws Exception{
		SQL sql = new SQL();
		Long IdEmpleadoDigitador = 0L;
		
		logger.info("INICIO::ProcesarIngresoEgresoTesoreria");
		SalidaProcesarIngresoEgresoTesoreria out = new SalidaProcesarIngresoEgresoTesoreria();
		CrearIngresoEgreso crearIngresoEgreso = new CrearIngresoEgreso();
		CrearIngresoEgreso_Entrada entradacrearIngresoEgreso = new CrearIngresoEgreso_Entrada();
		
		//Obtiene empleado digitador a partir de IdUsuario
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdUsuario", entrada.p_IdUsuario);
		logger.debug("INICIO::SQL_SELECT_Empleado");
		List<Empleado> empleadoDigitador = sql.query(con, SQL_SELECT_Empleado, params, Empleado.class);
		logger.debug("FIN::SQL_SELECT_Empleado");
		if (empleadoDigitador.size() > 0)
		{
			IdEmpleadoDigitador = empleadoDigitador.get(0).IdEmpleado;
		}
		logger.debug("INICIO::SQL_SELECT_MODALIDAD_PAGO");
		List<ModalidadPago> listaModalidadPago = sql.query(con, SQL_SELECT_MODALIDAD_PAGO, new HashMap<String,Object>(), ModalidadPago.class);
		logger.debug("FIN::SQL_SELECT_MODALIDAD_PAGO");
		Map<String,ModalidadPago> hashModalidadPago = listaModalidadPago.parallelStream().collect(Collectors.toMap(modalidad->modalidad.IdMedioDePago, modalidad->modalidad));
		
		//Obtiene datos de la preferencia de pago del portafolio
		params.clear();
		List<Long> lstIdPreferenciaPagoPortafolio = new ArrayList<Long>();
		HashMap<Long, Object> hashPreferenciaPagoPortafolio = new HashMap<Long, Object>(); 
		
		HashMap<Long, Object> hashPreferenciaPagoCorredor = new HashMap<Long, Object>(); 
		
		for(SolicitudAporteRescate sol : entrada.p_Solicitudes)
		{
			if (sol.IdPreferenciaPagoPortafolio != null)
			lstIdPreferenciaPagoPortafolio.add(sol.IdPreferenciaPagoPortafolio);
		}
		
		if (lstIdPreferenciaPagoPortafolio.size() > 0)
		{
			params.put("IdPreferenciaPagoPortafolio", lstIdPreferenciaPagoPortafolio);
			logger.debug("INICIO::SQL_SELECT_PreferenciaPagoPortafolio");
			List<PreferenciaPagoPortafolio> lstPreferenciaPagoPortafolio = sql.query(con, SQL_SELECT_PreferenciaPagoPortafolio, params, PreferenciaPagoPortafolio.class);
			logger.debug("FIN::SQL_SELECT_PreferenciaPagoPortafolio");
			hashPreferenciaPagoPortafolio = Utils.List2Hash(lstPreferenciaPagoPortafolio, "IdPreferenciaPagoPortafolio");
		}
		
		
	
		//Preferencias de pago corredor
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		logger.debug("INICIO::SQL_SELECT_PreferenciaPagoCorredor");
		List<PreferenciaPagoCorredor> lstPreferenciaPagoCorredor = sql.query(con, SQL_SELECT_PreferenciaPagoCorredor, params, PreferenciaPagoCorredor.class);
		logger.debug("FIN::SQL_SELECT_PreferenciaPagoCorredor");
		
		if(lstPreferenciaPagoCorredor.size() > 0)
		{
			hashPreferenciaPagoCorredor = Utils.List2Hash(lstPreferenciaPagoCorredor, "IdMoneda");			
		}
	
		for(SolicitudAporteRescate sol : entrada.p_Solicitudes){
		
			PreferenciaPagoPortafolio infoPreferenciaPagoPortafolio = (PreferenciaPagoPortafolio) hashPreferenciaPagoPortafolio.get(sol.IdPreferenciaPagoPortafolio != null ? sol.IdPreferenciaPagoPortafolio : 0);
			IngresoEgreso_Entrada ie = new IngresoEgreso_Entrada();
			String IdConcepto;
			if ( _c.SI.equalsIgnoreCase(sol.GeneraSpot)){
				IdConcepto = _c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) ? _c.ConceptoAporteNormalDolar : _c.TipoSolicitudRescate.equalsIgnoreCase(sol.TipoSolicitud) ? _c.ConceptoRescateNormalDolar : "";
			}else{
				IdConcepto = _c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) ? _c.ConceptoAporteNormal : (_c.TipoSolicitudRescate.equalsIgnoreCase(sol.TipoSolicitud) ? (_c.RescateTraspaso.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) ? _c.ConceptoRescateTraspaso : _c.ConceptoRescateNormal) : "");
			};
			
			ie.NumeroReferencia = sol.IdInterno;
			ie.IdTipoMovimiento = _c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) ? _c.IngresoTesoreria : _c.TipoSolicitudRescate.equalsIgnoreCase(sol.TipoSolicitud) ? _c.EgresoTesoreria : "";
			
			ie.IdMoneda = _c.SI.equalsIgnoreCase(sol.GeneraSpot)? sol.IdMonedaSistema  :sol.IdMoneda;;
			ie.Monto    = _c.SI.equalsIgnoreCase(sol.GeneraSpot)? sol.MontoMonedaSistema  :sol.MontoPagoLocal;
			ie.FechaLiquidacion = Fecha.toStringDate(sol.FechaEmision);
			

			ie.IdConcepto = (!sol.IdConcepto.equalsIgnoreCase("")) ? sol.IdConcepto : IdConcepto;
			
			if(_c.SI.equals(sol.RelacionTraspaso)) {
				ie.IdTipoPersona =  _c.TipoPersonaContraparte;
				ie.IdPortafolio = 0L;
				ie.IdPersona = sol.IdPersonaTraspaso;
			}else {
				ie.IdTipoPersona =  _c.TipoPersonaCliente;
				ie.IdPortafolio = sol.IdPortafolio;
				ie.IdPersona = sol.IdPersona;
			}
			ie.IdPortafolioTitularPago = sol.IdPortafolio;
			ie.IdTipoPersonaTitularPago = _c.TipoPersonaCliente;
			ie.IdPersonaTitularPago =   sol.IdPersona; 
			ie.IdPortafolioTitularPago = sol.IdPortafolio;  
			ie.IdCaja = 0L;
			ie.SecuenciaAgrupacion = "";
			ie.SecuenciaRelacionada = (sol.SecuenciaRelacionada != null) ? sol.SecuenciaRelacionada :0L;
			ie.Glosa = "";
			ie.IdEmpleadoDigitador = IdEmpleadoDigitador;
			ie.IdSucursalEmpleadoDigitador = 0L;
			ie.IdTipoProducto = _c.TipoProducto;
			ie.IdTipoOperacion = _c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) ? _c.TipoOperacionAporteFondoMutuo : _c.TipoSolicitudRescate.equalsIgnoreCase(sol.TipoSolicitud) ? _c.TipoOperacionRescateFondoMutuo : "";
			ie.IdTipoComprobante = _c.TipoComprobanteTesoreria;
			ie.IdComprobante = sol.IdSolicitudAporteRescate;
			ie.FechaComprobante = Fecha.stringToSqlDate(sol.FechaAceptacion);
			ie.FechaLiquidacionComprobante = "0001-01-01";
			ie.IdOperacion = sol.IdSolicitudAporteRescate;
			ie.FechaOperacion = Fecha.stringToSqlDate(sol.FechaEmision);
			ie.IdCondicionLiquidacion = _c.T0;
			ie.IdBolsa = _c.BCS;
			ie.IdMonedaOperacion = sol.IdMoneda;
			ie.MontoOperacion = sol.MontoPagoLocal;
			ie.IdTipoReajuste = "";
			ie.FechaParidad = Fecha.stringToSqlDate(sol.FechaEmision);
			ie.ValorParidad = sol.ParidadFechaInversion;
			ie.FolioCartera = 0L;
			ie.IdInstrumento = sol.IdInstrumento;
			ie.IdTipoInstrumento = sol.IdTipoInstrumento;
			ie.IdClasificacionTipoInstrumento = "";
			ie.Bilateral = _c.NO;
			ie.GeneraComprobante = _c.SI;
			ie.AgruparEnComprobante = _c.NO;
			ie.MueveCuentaInversion = _c.SI;				
			ie.TitularPago = sol.IdPersona.equals(ie.IdPersonaTitularPago) ? _c.NO : _c.SI; 		 
		
			FormaDePago_Entrada fp = new FormaDePago_Entrada();
			fp.NumeroReferencia = sol.IdInterno;
			fp.NumeroReferenciaFormaDePago = sol.IdInterno;
			fp.IdMedioDePago = _c.SI.equalsIgnoreCase(sol.GeneraSpot)? _c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago)? _c.MedioPagoTransferencia :sol.IdMedioDePago :sol.IdMedioDePago;
			fp.IdModalidadDePago = hashModalidadPago.get(fp.IdMedioDePago) != null ? hashModalidadPago.get(fp.IdMedioDePago).IdModalidadDePago:_c.MODCAJ;
			fp.IdMoneda = _c.SI.equalsIgnoreCase(sol.GeneraSpot)? sol.IdMonedaSistema  :sol.IdMoneda;
			fp.Monto = _c.SI.equalsIgnoreCase(sol.GeneraSpot)? sol.MontoMonedaSistema  :sol.MontoPagoLocal;
			
			
			if (sol.SecuenciaRelacionada != null)
			{
				PreferenciaPagoCorredor infoPreferenciaPagoCorredor = (PreferenciaPagoCorredor) hashPreferenciaPagoCorredor.get(_c.SI.equalsIgnoreCase(sol.GeneraSpot)? sol.IdMonedaSistema :sol.IdMoneda);
				fp.IdCuentaBancariaCorredora = infoPreferenciaPagoCorredor == null || infoPreferenciaPagoCorredor.IdCuentaBanco == null ? 0L : infoPreferenciaPagoCorredor.IdCuentaBanco;
			}
			else
			{
				fp.IdCuentaBancariaCorredora = 0L;  	
			}

			fp.IdTarjetaBancariaCorredora = 0L;
			fp.IdChequera = 0L;
			fp.NumeroDocumentoCorredora = 0L;
			fp.FechaDocumentoPagoCorredora = "0001-01-01";
			
			if(!_c.SI.equals(sol.RelacionTraspaso)) {
				fp.IdCuentaBancariaSocio = infoPreferenciaPagoPortafolio != null ? infoPreferenciaPagoPortafolio.IdCuentaBancaria : 0L;
				fp.IdBancoSocio = infoPreferenciaPagoPortafolio != null ? infoPreferenciaPagoPortafolio.IdBancoCuentaBancaria : 0L;
				fp.NombreCuentaBancariaSocio = "";
				fp.NumeroCuentaBancariaSocio = infoPreferenciaPagoPortafolio != null ? infoPreferenciaPagoPortafolio.NumeroCuentaBancaria : "";
				fp.IdTipoCuentaBancariaSocio = infoPreferenciaPagoPortafolio != null ? infoPreferenciaPagoPortafolio.IdTipoCuentaBancaria : "";
				fp.NombreTipoCuentaBancariaSocio = infoPreferenciaPagoPortafolio != null ? infoPreferenciaPagoPortafolio.NombreTipoCuentaBancaria : "";
			}else {
				fp.IdCuentaBancariaSocio = sol.IdCuentaBancaria;
				fp.IdBancoSocio = sol.IdBanco;
				fp.NombreCuentaBancariaSocio = "";
				fp.NumeroCuentaBancariaSocio = sol.NumeroCuentaBancaria;
				fp.IdTipoCuentaBancariaSocio = sol.IdTipoCuentaBancaria;
				fp.NombreTipoCuentaBancariaSocio = sol.NombreTipoCuentaBancaria;
			}
			
			fp.IdTarjetaBancariaSocio = 0L;
			fp.IdTipoTarjetaBancariaSocio = "";
			fp.NombreTipoTarjetaBancariaSocio = "";
			fp.NumeroTarjetaBancariaSocio = "";
			fp.NumeroSerieSocio = "";
			fp.NumeroDocumentoSocio = 0L;
			fp.FechaDocumentoPagoSocio = "0001-01-01";
			
			entradacrearIngresoEgreso.p_IngresoEgresoGenerar.add(ie);
			entradacrearIngresoEgreso.p_FormaDePago.add(fp);
		}
		entradacrearIngresoEgreso.p_IdEmpresa = entrada.p_IdEmpresa;
		entradacrearIngresoEgreso.p_IdUsuario = entrada.p_IdUsuario;
		entradacrearIngresoEgreso.p_IdModulo = entrada.p_IdModulo;
		entradacrearIngresoEgreso.p_SimularOperacion = entrada.p_SimularOperacion == null ? "N" : entrada.p_SimularOperacion;
		entradacrearIngresoEgreso.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;		
		entradacrearIngresoEgreso.p_EsIngresoAutomatico = entrada.p_EsIngresoAutomatico;
		CrearIngresoEgreso_Salida salida = crearIngresoEgreso.logica(con, entradacrearIngresoEgreso);
		out.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
		out.P_OUT_INGRESOEGRESOGENERADO = salida.P_OUT_INGRESOEGRESOGENERADO;
		out.P_OUT_RESULTADOPROCESO = salida.P_OUT_RESULTADOPROCESO;
		out.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
		
		logger.info("FIN::ProcesarIngresoEgresoTesoreria");
		return out;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES

	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getQAConnection()) 
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String test = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"TES\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"S\"}";
			
			
			EntradaProcesarIngresoEgresoTesoreria Objin = gson.fromJson(test, EntradaProcesarIngresoEgresoTesoreria.class);
			ProcesarIngresoEgresoTesoreria demo = new ProcesarIngresoEgresoTesoreria();
			
 			System.out.println("ENTRADA:");
 			System.out.println(Objin.toString());
 			
 			try 
 			{	
 				SalidaProcesarIngresoEgresoTesoreria ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				//con.commit();
 			}
 			catch(Exception er) 
 			{
				//er.printStackTrace();	
				con.commit();
			}
 			
 			
 			con.close();
 			
	    	}
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
	
}
