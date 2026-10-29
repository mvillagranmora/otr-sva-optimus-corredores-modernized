package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Salida;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapital_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.facturacion.CrearMovimientoComprobante.Comprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CerrarEventoCapitalEmisionLiberada implements JavaCall{
	
	private final Log log = new Log("CUS.CerrarEventoCapitalEmisionLiberada");
	
	private final String SQL_SELECT_EVENTO_CAPITAL					 		= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalEmisionLiberada_SelectEventoCapital.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE					= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalEmisionLiberada_SelectEventoCapitalCliente.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL 							= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalEmisionLiberada_UpdateEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE 					= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalEmisionLiberada_UpdateEventoCapitalCliente.sql");
	
	
	public static class CerrarEventoCapitalEmisionLiberadaEntrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String	p_IdModulo;
		public Long 	p_IdEventoCapital;
		public String 	p_IdTipoEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	
	public static class CerrarEventoCapitalEmisionLiberadaSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public CerrarEventoCapitalEmisionLiberadaSalida (){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR=0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	};
	
	

	public static class EventoCapital{
		public Long					IdEventoCapital;
		public Long 				IdEmpresa;
		public String				IdTipoEventoCapital;
		public Long 				Secuencia;
		public String				Descripcion;
		public String				EsInscrito;
		public String				IdClasificacionTipoInstrumento;
		public String				IdTipoInstrumento;
		public Long					IdInstrumento;
		public String				Nemo;
		public BigDecimal			Factor;
		public BigDecimal			Valor;
		public String				IdMoneda;
		public Long 				IdInstrumentoNuevo;
		public String				NemoNuevo;
		public BigDecimal			FactorNuevo;
		public java.sql.Date 		FechaActualizacion;
		public java.sql.Date 		FechaLimite;
		public java.sql.Date 		FechaPago;
		public Long					NumeroDividendo;
		public String				IdTipoDividendo;
		public BigDecimal			PrecioAnterior;
		public java.sql.Timestamp 	FechaHoraIngresoRegistro;
		public java.sql.Timestamp 	FechaHoraUltimaActualizacion;
		public String				TipoIngreso;
		public Long					IdUsuario;
		public String				IdEstado;
		public BigDecimal			CuentaDepositanteSorteoLetras;
		public BigDecimal			MonedaSorteoLetras;
		public String				GlosaMonedaSorteoLetras;
		public BigDecimal			EmisorPagadorSorteoLetras;
		public BigDecimal			EmisorSorteoLetras;
		public BigDecimal			InstrumentoSorteoLetras;
		public String				GlosaInstrumentoSorteoLetras;
		public BigDecimal			BloqueoSorteoLetras;
		public BigDecimal			SorteadoSorteoLetras;
		public BigDecimal			APagarSorteoLetras;
		public BigDecimal			APagarPesosSorteoLetras;
		public String				IdEmisor;
		public String				IdMonedaLiquidacion;
		public BigDecimal			Paridad;
		public String				IdEmisorNuevo;
		public String				EstadoNotificacion;
		public Long					IdInstrumentoAnterior;
		public Long					Indice;
		public BigDecimal			Impuesto;
		public BigDecimal			CantidadCuotas;
		public BigDecimal			Monto;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	

	
	
	public static class EventoCapitalCliente 
	{
		public Long					IdEventoCapital;
		public Long					IdEventoCapitalCliente;		
		public String				IdClasificacionTipoInstrumento;
		public String				IdTipoInstrumento;
		public Long					IdInstrumento;
		public Long					IdPortafolio;
		public String				IdBolsa;
		public BigDecimal			CantidadNueva;
		public BigDecimal			CantidadDerechoArchivo;
		public String				IdMoneda;
		public String				IdBoveda;
		public String               TipoCartera;
		public String				Nemo;
		public java.sql.Date		FechaPago;
		public Long					IdCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ActualizarEventoCapitalCliente {
		public Long IdEventoCapitalCliente;
		public Long IdEventoCapital;
		public Long IdMovimientoComprobanteFac;
		public Long IdComprobanteFac;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//------------------------------------------------
	// LOGICA
	//------------------------------------------------
	
	public CerrarEventoCapitalEmisionLiberadaSalida logica(Connection con, CerrarEventoCapitalEmisionLiberadaEntrada in) throws Exception {
		
		CerrarEventoCapitalEmisionLiberadaSalida out = new CerrarEventoCapitalEmisionLiberadaSalida();		
		SQL sql = new SQL();		
		BigDecimal vPrecioInstrumento = new BigDecimal(1);		
		HashMap<Long, Object> mapPreciosTasas  = new HashMap<Long, Object>();
		
		log.write("PARAMETROS DE ENTRADA > ", in);
		
		
		// ---------------------------------------------------------------
		// VALIDACIONES INICIALES
					
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);

		out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
	
		if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
			return out;
		}		
		
		
		
		// ---------------------------------------------------------------
		// CONSULTA DATOS EVENTO CAPITAL A CERRAR
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("IdEventoCapital", in.p_IdEventoCapital);
		
		List<EventoCapital> lstEventoCapital = sql.query(con, SQL_SELECT_EVENTO_CAPITAL, params, EventoCapital.class);
		
		
		// ---------------------------------------------------------------
		// VALIDACIONES DE NEGOCIO
		
		//Valida que exista el EC
		if (lstEventoCapital.size() == 0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_EVENTO_CAPITAL_NO_EXISTE", new HashMap<String,String>() , con, in.p_IdEmpresa));
			return out;
		}
		
		
		//Valida que el EC se encuentre procesado o notificado para poder cerrarlo
		for (EventoCapital EveCap: lstEventoCapital)
		{	
			Set<String> SetEstado = new HashSet<>(Arrays.asList("PRO", "NOT"));
			
			if (!SetEstado.contains(EveCap.IdEstado))
			{
				out.P_OUT_MENSAJES.add(new MensajeSalida(EveCap.IdEventoCapital, MensajeSalida.MENSAJE_ERROR, "UI_EVENTO_CAPITAL_NO_PROCESADO", new HashMap<String,String>() , con, in.p_IdEmpresa));
				return out; 
			}
		}
		

		// ---------------------------------------------------------------
		// CONSULTA DATOS EVENTO CAPITAL CLIENTE		
		
		List<EventoCapitalCliente> lstEventoCapitalCliente = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_CLIENTE, params, EventoCapitalCliente.class);
		
		if (lstEventoCapitalCliente.size() == 0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTEN_CLIENTES_CON_DERECHO", new HashMap<String,String>() , con, in.p_IdEmpresa));
			return out;
		}
		
		

		// ---------------------------------------------------------------
		// OBTIENE PRECIOS Y TASAS			
		
		String fechaSistema 		= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String localizacionSistema 	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		EntregarPreciosTasas_Entrada entregarPreciosTasasEntrada = new EntregarPreciosTasas_Entrada();				
		entregarPreciosTasasEntrada.p_IdEmpresa = in.p_IdEmpresa;
		entregarPreciosTasasEntrada.p_IdModulo 	= in.p_IdModulo;
		entregarPreciosTasasEntrada.p_IdPais 	= idPaisEmpresa;
		entregarPreciosTasasEntrada.p_IdUsuario = in.p_IdUsuario;
		
		for (EventoCapital eventoCapital : lstEventoCapital) 
			
		{
			EntregarPreciosTasas_Entrada_DatosInstrumentos entradaDatosInstrumentos = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
			entradaDatosInstrumentos.IdRegistro 					= eventoCapital.IdEventoCapital;
			entradaDatosInstrumentos.TipoCartera 					= "0";
			entradaDatosInstrumentos.IdUso 							= "CARTERA";
			entradaDatosInstrumentos.IdTipoCriterioTasa 			= "0";
			entradaDatosInstrumentos.IdClasificacionTipoInstrumento = eventoCapital.IdClasificacionTipoInstrumento;
			entradaDatosInstrumentos.IdTipoInstrumento 				= eventoCapital.IdTipoInstrumento;
			entradaDatosInstrumentos.IdInstrumento 					= eventoCapital.IdInstrumento;
			entradaDatosInstrumentos.Nemotecnico 					= eventoCapital.Nemo;
			entradaDatosInstrumentos.IdMoneda 						= eventoCapital.IdMoneda;  
			entradaDatosInstrumentos.FechaConsulta 					= eventoCapital.FechaPago;
			entradaDatosInstrumentos.PlazoResidual 					= 0L;
			entregarPreciosTasasEntrada.p_DatosInstrumentos.add(entradaDatosInstrumentos);
		}
				
		
		if (entregarPreciosTasasEntrada.p_DatosInstrumentos.size() > 0) 
		{
			EntregarPreciosTasas entregarPreciosTasasService = new EntregarPreciosTasas();
			EntregarPreciosTasas_Salida entregarPreciosTasasSalida = entregarPreciosTasasService.logica(con, entregarPreciosTasasEntrada);
			mapPreciosTasas = Utils.List2Hash(entregarPreciosTasasSalida.P_OUT_PRECIOSTASAS, "IdRegistro");
		}
		
		
		
		// ---------------------------------------------------------------
		// GENERA MOVIMIENTOS DE FACTURACION			
		
		
		CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada InMovimientoComprobante = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
		InMovimientoComprobante.p_IdEmpresa 			= in.p_IdEmpresa;
		InMovimientoComprobante.p_IdUsuario 			= in.p_IdUsuario;
		InMovimientoComprobante.p_IdModulo 				= in.p_IdModulo;
		InMovimientoComprobante.p_simularOperacion		= "N";
		InMovimientoComprobante.p_UsaReglasNegocio		= "S";
		InMovimientoComprobante.p_EsIngresoAutomatico	= "S";
		
		for (EventoCapitalCliente eventoCapitalCliente : lstEventoCapitalCliente){	
			vPrecioInstrumento = new BigDecimal(1);
			
			EntregarPreciosTasas_Salida_PreciosTasas registroPrecio = (EntregarPreciosTasas_Salida_PreciosTasas) mapPreciosTasas.get(eventoCapitalCliente.IdEventoCapital);
			if (registroPrecio != null && registroPrecio.Precio != null && registroPrecio.Precio.doubleValue() > 0) 
			{
				vPrecioInstrumento = registroPrecio.Precio;
			}
			
			
			CrearMovimientoComprobante.MovimientoComprobante_Entrada movFac = new CrearMovimientoComprobante.MovimientoComprobante_Entrada();
			movFac.NumeroMvtoCmbte							= eventoCapitalCliente.IdEventoCapitalCliente;   //Numero referencial
			movFac.IdEmpresa								= in.p_IdEmpresa;
			movFac.IdConcepto								= "EMILIB";
			movFac.SecuenciaAgrupacion						= "";
			movFac.IdCliente								= eventoCapitalCliente.IdCliente;
			movFac.IdPortafolio								= eventoCapitalCliente.IdPortafolio;			
			movFac.ModPrefFact								= "";			
			movFac.IdEmpleadoDigitador						= 0L; ///// FALTA. ver si se puede sacar
			movFac.IdInstrumento							= eventoCapitalCliente.IdInstrumento;
			movFac.IdInstrumentoRelacionado					= 0L;
			movFac.IdInstrumentoSubyacente 					= 0L;
			movFac.IdTipoProducto							= "INAC";			
			movFac.IdMoneda									= eventoCapitalCliente.IdMoneda;			
			movFac.IdMonedaSubyacente						= "";
			movFac.IdMonedaLiquidacion 						= eventoCapitalCliente.IdMoneda;
			movFac.IdTipoComprobante						= "FEE";
			movFac.IdBoveda									= eventoCapitalCliente.IdBoveda;
			movFac.IdCorredoraContraparte					= 0L;
			movFac.IdCondicionLiquidacion					= "T0";
			movFac.IdTipoOperacion							= eventoCapitalCliente.TipoCartera == "GAR" ? "EMLIGA" : "EMILIB";
			movFac.FechaLiquidacion							= eventoCapitalCliente.FechaPago.toString();
			movFac.IdBolsa									= eventoCapitalCliente.IdBolsa;
			movFac.IdTipoReajuste							= "CLP";
			movFac.IdTipoConvenio							= "SCV";
			movFac.IdFormaCustodia							= "CUS";
			movFac.IdTipoCanalIngreso						= "AGE";
			movFac.FolioTransaccion							= "";
			movFac.FechaTransaccion							= "0001-01-01";

			if (localizacionSistema.equalsIgnoreCase("PE"))
			{
				movFac.Cantidad								= eventoCapitalCliente.CantidadDerechoArchivo;
			}
			else
			{
				movFac.Cantidad								= eventoCapitalCliente.CantidadNueva;
			}
			
			movFac.IdOrden									= 0L;
			movFac.IdAsignacion								= 0L;
			movFac.IdAsignacionRef							= 0L;
			movFac.FechaOrden								= "0001-01-01";
			movFac.IdTransaccion							= 0L;
			movFac.FechaVencimiento							= "0001-01-01";
			movFac.PrecioAPlazo								= BigDecimal.ZERO;
			movFac.MontoAPlazo								= BigDecimal.ZERO;
			movFac.FechaParidad								= "0001-01-01";
			movFac.ValorParidad								= BigDecimal.ZERO;				
			movFac.FechaTipoCambio							= "0001-01-01"; 
			movFac.ValorTipoCambio							= BigDecimal.ZERO;
			movFac.ValorCaptacionMonedaOrigen				= BigDecimal.ZERO;
			movFac.TasaOperacion							= BigDecimal.ZERO;
			movFac.MontoDescuento							= BigDecimal.ZERO; 
			movFac.MontoInteres								= BigDecimal.ZERO;
			movFac.Glosa 									= "Emisión Liberada N°: " + eventoCapitalCliente.IdEventoCapital.toString() + " " + eventoCapitalCliente.Nemo + " Fecha: " + eventoCapitalCliente.FechaPago.toString();
			movFac.ValorRescate								= BigDecimal.ZERO;
			movFac.TasaAgente								= BigDecimal.ZERO;
			movFac.Spread									= BigDecimal.ZERO;
			movFac.DiasPlazo								= BigDecimal.ZERO;			
			movFac.Precio									= BigDecimal.ZERO;
			movFac.Monto									= BigDecimal.ZERO;
			movFac.MontoMovimientoComprobante				= BigDecimal.ZERO;
			movFac.MontoLiquidacion							= BigDecimal.ZERO;			
			movFac.EsComprobIndividual						= "N";
			movFac.EsCuentaPropia							= "N"; 
			movFac.IndicadorOrdenDirecta					= ""; 
			movFac.EsVentaCorta								= "N"; 
			movFac.EsPrecioPrima							= "N";
			movFac.EsEnRueda								= "N"; 
			movFac.EsRuteo									= "N";
			movFac.EsAPlazo									= "N"; 
			movFac.EsFungible								= "N"; 
			movFac.ActualizarCustodia						= "S";
			movFac.ActualizarCuentaInversion				= "N";
			movFac.ActualizarTesoreria						= "N";
			movFac.ActualizarCustodiaRelacionado			= "N";
			movFac.CrearComprobanteAutomatico				= "S";
			movFac.FechaMovimientoComprobante				= eventoCapitalCliente.FechaPago.toString(); 				
			movFac.FolioCartera								= 0L;
			movFac.FechaEmision								= fechaSistema;
			movFac.TasaEmision								= BigDecimal.ZERO;
			movFac.Tasa										= BigDecimal.ZERO;
			movFac.IdUsuario								= in.p_IdUsuario;
			
			InMovimientoComprobante.p_Movimientos.add(movFac);
		
		}
		
		CrearMovimientoComprobante ComprobanteFacturacion = new CrearMovimientoComprobante();
		CrearMovimientoComprobante_Salida OutMovimientoComprobante = ComprobanteFacturacion.logica(con,InMovimientoComprobante);
		
		HashMap<Long,Object> hMovimientos = Utils.List2Hash(OutMovimientoComprobante.p_out_Movimientos, "NumeroMvtoCmbte");
		HashMap<Long,Object> hComprobante = Utils.List2Hash(OutMovimientoComprobante.p_out_Comprobantes, "IdMovimientoComprobante");
		
		List<ActualizarEventoCapitalCliente> actualizarECCliente = lstEventoCapitalCliente.stream().map(data -> {
			ActualizarEventoCapitalCliente ecc = new ActualizarEventoCapitalCliente();
			ecc.IdEventoCapital            = data.IdEventoCapital;
			ecc.IdEventoCapitalCliente     = data.IdEventoCapitalCliente;
			
			MovimientoComprobante_Salida datosMovimiento  = (MovimientoComprobante_Salida) hMovimientos.get(data.IdEventoCapitalCliente);
			Comprobante_Salida           datosFacturacion = (Comprobante_Salida) hComprobante.get(datosMovimiento.IdMovimientoComprobante);
			
			ecc.IdMovimientoComprobanteFac = datosMovimiento.IdMovimientoComprobante;
			ecc.IdComprobanteFac           = datosFacturacion.IdComprobante;
			return ecc;
		}).collect(Collectors.toList());
		
		if (OutMovimientoComprobante.P_OUT_ERRORESCONTADOR > 0) 
		{	
			out.P_OUT_MENSAJES = OutMovimientoComprobante.P_OUT_MENSAJES;
			return out;
		}
		
		
		
		
		// --------------------------------------------------
		// ACTUALIZAR ESTADO DE EC y ECC
		
		EventoCapital eventoActualizar = new EventoCapital();
		eventoActualizar.IdEventoCapital = in.p_IdEventoCapital;
		
		List<EventoCapital> actualizar = new ArrayList<EventoCapital>();
		actualizar.add(eventoActualizar);
		
		sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, actualizar, EventoCapital.class);
		sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, actualizarECCliente, ActualizarEventoCapitalCliente.class);

		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_OPERACION_PROCESAR_EXITO",new HashMap<String,String>(0),con,in.p_IdEmpresa));
		
		log.write("SALIDA > ", out.P_OUT_MENSAJES);
		
		// crear objeto de tipo noticacion
		
		if(out.P_OUT_ERRORESCONTADOR==0) {
			NotificarEventoCapital notificarEventoCierre = new NotificarEventoCapital();	
			NotificarEventoCapital_Entrada NotifIN = new NotificarEventoCapital_Entrada();
			NotifIN.p_IdUsuario = in.p_IdUsuario;
			NotifIN.p_IdEmpresa = in.p_IdEmpresa;
			NotifIN.p_IdModulo = in.p_IdModulo;
			NotifIN.p_EstadoEventoCapital = "CER";
			ObjEventoCapital_Entrada objIdsCapital = new ObjEventoCapital_Entrada();
			objIdsCapital.p_IdEventoCapital=in.p_IdEventoCapital;
			NotifIN.p_IdsEventoCapital.add(objIdsCapital);
			
			try {
				NotificarEventoCapital_Salida ObjOut= notificarEventoCierre.logica(con, NotifIN);
				List<MensajeSalida> mensajeAdvertencia = ObjOut.P_OUT_MENSAJES.stream().filter(s -> s.tipo.equalsIgnoreCase("ADVERTENCIA")).collect(Collectors.toList());	
				if(ObjOut.P_OUT_ERRORESCONTADOR>0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,"UI_NOTIFICACION_EVENTO_CAPITAL_NO_SE_REALIZO",new HashMap<String,String>(),con,in.p_IdEmpresa) );								
				}else if(mensajeAdvertencia.size()>0) {
					out.P_OUT_MENSAJES=ObjOut.P_OUT_MENSAJES.stream().filter(s -> s.tipo.equalsIgnoreCase("ADVERTENCIA")).collect(Collectors.toList());	
				}
			}
			catch(Exception e) {				
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,"UI_NOTIFICACION_EVENTO_CAPITAL_NO_SE_REALIZO",new HashMap<String,String>(),con,in.p_IdEmpresa) );				
			}
		}
		
		return out;
	}
	
	
	//-------------------------------------------------
	// CALL
	//-------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		//Gson gson = new Gson();		
		Gson gson = Utils.getGsonBuilder();
		
		CerrarEventoCapitalEmisionLiberadaEntrada jin=gson.fromJson(in.toString() ,CerrarEventoCapitalEmisionLiberadaEntrada.class);		
		CerrarEventoCapitalEmisionLiberadaSalida out=logica(conn,jin);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) 
		{
			try 
			{
				///Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				Gson gson = Utils.getGsonBuilder();
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":366298,\"p_IdTipoEventoCapital\":\"EL\"}";
				
				CerrarEventoCapitalEmisionLiberadaEntrada Objin = gson.fromJson(json, CerrarEventoCapitalEmisionLiberadaEntrada.class);
								
				CerrarEventoCapitalEmisionLiberada  kk = new CerrarEventoCapitalEmisionLiberada();
				kk.logica(con, Objin);
				//con.commit();
				
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
