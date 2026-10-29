package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

//import com.businessobjects.report.htmlrender.e;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.custodia.evc.CerrarEventoCapitalCorteCupon.EventoCapital;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Salida;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapital_Entrada;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Feriados;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;

import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;


import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CerrarEventoCapitalCorteCupon implements JavaCall{
	
	private final Log log = new Log("CUS.CerrarEventoCapitalCorteCupon");

	
	private final String SQL_SELECT_EVENTO_CAPITAL					 		= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCorteCupon_SelectEventoCapital.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_CONTRAPARTE  			= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCorteCupon_SelectEventoCapitalContraparte.sql");	
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE   			    = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCorteCupon_SelectEventoCapitalCliente.sql");		
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE_FORMA_DE_PAGO    = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCorteCupon_SelectEventoCapitalClienteFormaDePago.sql");	
	private final String SQL_SELECT_INSTRUMENTO_ART_104						= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCorteCupon_SelectInstrumentoArt104.sql");	
	private final String SQL_SELECT_PARAMETRO_ART_104						= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCorteCupon_SelectParametroArt104.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL 							= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCorteCupon_UpdateEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE 					= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCorteCupon_UpdateEventoCapitalCliente.sql");
	private final String SQL_SELECT_INFO_ECC_CONTRAPARTE 					= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCorteCupon_SelectECCInfoContraparte.sql");
	
	
	//-----------------------------------------------------------------
	// CLASES DE ENTRADA
	//-----------------------------------------------------------------
	public static class CerrarEventoCapitalCorteCuponEntrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String	p_IdModulo;
		public Long 	p_IdEventoCapital;
		public String 	p_IdTipoEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	
	
	//-----------------------------------------------------------------
	// CLASES DE SALIDA
	//-----------------------------------------------------------------	
	public static class CerrarEventoCapitalCorteCuponSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public CerrarEventoCapitalCorteCuponSalida (){
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	};
	
	
	
	//-----------------------------------------------------------------
	// CLASES COMPLEMENTARIAS DE APOYO
	//-----------------------------------------------------------------
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
	

	
	public static class EventoCapitalContraparte
	{	
		public Long   			IdInterno; 
		public Long   			IdPersonaSocioNegocio;
		public Long   			IdPortafolioSocioNegocio;
		public Long   			IdPersonaPagoCobro;
		public Long   			IdPortafolioPagoCobro;		
		public String 			IdMoneda;
		public java.sql.Date 	FechaCierre;
		public Long   			IdInstrumento; 				
		public String 			IdTipoInstrumento;			
		public String 			IdClasificacionTipoInstrumento;
		public String 			IdBoveda;
		public BigDecimal 		Monto;
		public BigDecimal 		MontoInteres;
		public BigDecimal 		Retencion;
		public Long   			IdEventoCapital = 0L; 
		public Long   			IdEventoCapitalCliente = 0L;
		
		public String toString(){
			return Utils.printJson(this, false); 
		}
	}		
		
	      
	public static class EventoCapitalCliente
	{	
		public Long   			IdEventoCapital; 
		public Long   			IdEventoCapitalCliente;
		public Long   			IdPersona;
		public Long   			IdPortafolio;	
		public String 			IdMoneda;
		public java.sql.Date 	FechaCierre;
		public Long   			IdInstrumento; 				
		public String 			IdTipoInstrumento;			
		public String 			IdClasificacionTipoInstrumento;
		public String 			IdBoveda;
		public BigDecimal 		Monto;
		public BigDecimal 		MontoInteres;
		public BigDecimal       Amortizacion;
		public BigDecimal 		Retencion;
		public String 			PagaClienteCorredor;
		public String 			IdMedioDePago;
		
		public String toString(){
			return Utils.printJson(this, false); 
		}
	}	


	public static class EventoCapitalClienteFormaDePago {
	
		public Long				IdEventoCapitalCliente;		    	
		public String			IdMedioDePago;
		public String			IdModalidadDePago;
		public String			IdMoneda;						
		public BigDecimal		Monto;
		public BigDecimal 		MontoInteres;
		public BigDecimal 		Retencion;				        
		public Long				IdCuentaBancariaSocio;
		public Long				IdBancoSocio;
		public String			NombreCuentaBancariaSocio;
		public String			NumeroCuentaBancariaSocio;
		public String			IdTipoCuentaBancariaSocio;
		public String			NombreTipoCuentaBancariaSocio;
		public String			IdTipoTarjetaBancariaSocio;
		public String			NumeroTarjetaBancariaSocio;
	
		public String toString(){
			return Utils.printJson(this, false); 
		}		
	}
	
	public static class InstrumentoEventoCapital
	{	
		public Long   			IdInstrumento; 	
		public String 			Nemotecnico;				
		public String 			AfectoArt104;			
		
		public String toString(){
			return Utils.printJson(this, false); 
		}
	}

	public static class ParametroArt104EventoCapital
	{					
		public String 			Valor;			
		
		public String toString(){
			return Utils.printJson(this, false); 
		}
	}
	
	
	
	//-----------------------------------------------------------------
	// LOGICA
	//-----------------------------------------------------------------
	
	public CerrarEventoCapitalCorteCuponSalida logica(Connection con, CerrarEventoCapitalCorteCuponEntrada in) throws Exception {
		
		CerrarEventoCapitalCorteCuponSalida out = new CerrarEventoCapitalCorteCuponSalida();		
		SQL sql = new SQL();		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		HashMap<Long,Object> mapEventoCapital = new HashMap<Long,Object>();
		HashMap<Long,Object> mapSalidaIngresoEgresoGenerado = new HashMap<Long,Object>();
				
		log.write("PARAMETROS DE ENTRADA > ", in);

		String fechaSistema 		= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String localizacionSistema 	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate fechaSistemaDate = LocalDate.parse(fechaSistema, formatter);
		
		
		// ---------------------------------------------------------------
		// VALIDACIONES BASICAS

		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();		
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);

		out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
	
		if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0)
		{
			return out;
		}		
		
		

		
		
		// ---------------------------------------------------------------
		// CONSULTA EVENTO CAPITAL A CERRAR
		
		
		params.put("IdEventoCapital", in.p_IdEventoCapital);	
		params.put("TipoEventoCapital", in.p_IdTipoEventoCapital);
		
		List<EventoCapital> lstEventoCapital = sql.query(con, SQL_SELECT_EVENTO_CAPITAL, params, EventoCapital.class);

		if (lstEventoCapital.size() == 0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_EVENTO_CAPITAL_NO_EXISTE", new HashMap<String,String>() , con, in.p_IdEmpresa));
			return out;
		}
		else
		{	
			mapEventoCapital = Utils.List2Hash(lstEventoCapital, "IdEventoCapital");   // Transforma la lista lstEventoCapital en un hash
		}
				

		
		
		// ---------------------------------------------------------------
		// CONSULTA DATOS EVENTO CAPITAL CONTRAPARTE		
		params.clear();
		params.put("IdEventoCapital", in.p_IdEventoCapital);
		params.put("IdTipoEventoCapital", in.p_IdTipoEventoCapital);
		params.put("LocalizacionSistema", localizacionSistema);
			
		List<EventoCapitalContraparte> lstEventoCapitalContraparte = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_CONTRAPARTE, params, EventoCapitalContraparte.class);
		

				
		// ---------------------------------------------------------------
		// CONSULTA DATOS EVENTO CAPITAL CLIENTE		
		params.clear();
		params.put("IdEventoCapital", in.p_IdEventoCapital);
		params.put("IdTipoEventoCapital", in.p_IdTipoEventoCapital);
		params.put("LocalizacionSistema", localizacionSistema);			
		List<EventoCapitalCliente> lstEventoCapitalCliente = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_CLIENTE, params, EventoCapitalCliente.class);
		
				
		
		// ---------------------------------------------------------------
		// CONSULTA FORMAS DE PAGO DE EVENTO CAPITAL CLIENTE
		
		List<EventoCapitalClienteFormaDePago> lstEventoCapitalClienteFormaDePago = new ArrayList<EventoCapitalClienteFormaDePago>();
		
		if (lstEventoCapitalCliente.size() > 0)
		{
			params.clear();
			params.put("IdEventoCapital", in.p_IdEventoCapital);
			params.put("IdTipoEventoCapital", in.p_IdTipoEventoCapital);
			params.put("LocalizacionSistema", localizacionSistema);
						
			lstEventoCapitalClienteFormaDePago = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_CLIENTE_FORMA_DE_PAGO, params, EventoCapitalClienteFormaDePago.class);
		}
		
		
		// ---------------------------------------------------------------
		// VALIDA DIA HABIL

		String paisSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		List<FechasIn> fechasEntrada = new ArrayList<FechasIn>();
		HashMap<String, Object> fechasCalculadas = new HashMap<String,Object>();
		
		for(Entry<Long, Object> entry : mapEventoCapital.entrySet()) {
			EventoCapital evento = (EventoCapital)entry.getValue();
			boolean fechaLimiteEsDiaHabil = Feriados.esDiaHabil(con, evento.FechaPago.toString(), Long.parseLong(paisSistema));

			if(!fechaLimiteEsDiaHabil) {

				FechasIn fechasIn = new FechasIn();
				
				fechasIn.fechaInicial = evento.FechaPago.toString();
				fechasIn.numeroDias = 1L;
				fechasEntrada.add(fechasIn);
			}


		}

		if(fechasEntrada.size() !=0) {
			ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_idPais = Long.parseLong(paisSistema);
			inObtenerDiaSiguiente.p_diaHabil = "S";
			inObtenerDiaSiguiente.p_soloCaeDiaHabil = "S";
			inObtenerDiaSiguiente.p_fechas = fechasEntrada;
			
			OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
			fechasCalculadas = Utils.List2Hash(outObtenerDiaSiguiente.p_out_fechas, "fechaInicial");

		}
		
		//INFORMACION INSTRUMENTO DE EVENTO CAPITAL PARA VERIFICAR SI ESTÁ AFECTO AL ART. 104
		List<ParametroArt104EventoCapital> lstParametroArt104EventoCapital = new ArrayList<ParametroArt104EventoCapital>();
		List<InstrumentoEventoCapital> lstInstrumentoEventoCapital  = new ArrayList<InstrumentoEventoCapital>();
		String afectoArt104 = "";
		int valorParametroArt104 = 0;

		for (EventoCapital ecc: lstEventoCapital)
		{
			params.clear();
			params.put("IdInstrumento", ecc.IdInstrumento);
			lstInstrumentoEventoCapital = sql.query(con, SQL_SELECT_INSTRUMENTO_ART_104, params, InstrumentoEventoCapital.class);
		}

		for(InstrumentoEventoCapital ins: lstInstrumentoEventoCapital){
			//Verificamos si instrumento está afecto al ART.104, si es así consultamos parametro para modificar monto del movimiento 
			afectoArt104 = ins.AfectoArt104;
		}

		if("S".equalsIgnoreCase(afectoArt104)){
				params.clear();
				lstParametroArt104EventoCapital = sql.query(con, SQL_SELECT_PARAMETRO_ART_104, params, ParametroArt104EventoCapital.class);
				for(ParametroArt104EventoCapital param104: lstParametroArt104EventoCapital){
					valorParametroArt104 = Integer.parseInt(param104.Valor);
				}
				// Convertir el valor entero a BigDecimal y dividir por 100
				BigDecimal porcentaje = BigDecimal.valueOf(valorParametroArt104).divide(BigDecimal.valueOf(100));
				//ACTUALIZAMOS MONTOS DE EVENTO CAPITAL CONTRAPARTE, CLIENTE Y FORMA DE PAGO
				for (EventoCapitalContraparte ecc: lstEventoCapitalContraparte)
					{ 
						ecc.Monto = ecc.Monto.subtract(ecc.MontoInteres.multiply(porcentaje));
						ecc.Retencion =  ecc.MontoInteres.multiply(porcentaje);
					}
				for (EventoCapitalCliente ecc: lstEventoCapitalCliente)
					{ 
						ecc.Monto = ecc.Monto.subtract(ecc.MontoInteres.multiply(porcentaje));
						ecc.Retencion =  ecc.MontoInteres.multiply(porcentaje);

					}
				for (EventoCapitalClienteFormaDePago eccfp: lstEventoCapitalClienteFormaDePago)
					{
						eccfp.Monto = eccfp.Monto.subtract(eccfp.MontoInteres.multiply(porcentaje)); 
						eccfp.Retencion =  eccfp.MontoInteres.multiply(porcentaje);
					}
		}		

		
		// ---------------------------------------------------------------
		// GENERA MOVIMIENTOS DE TESORERIA PARA CLIENTES Y CONTRAPARTES (Cuando la Forma de pago no es cuenta inversiÃ³n)
		
		CrearIngresoEgreso servicioCrearIngresoEgreso = new CrearIngresoEgreso();
		CrearIngresoEgreso_Entrada entradaCrearIngresoEgreso = new CrearIngresoEgreso_Entrada();


		entradaCrearIngresoEgreso.p_IdEmpresa = in.p_IdEmpresa;
		entradaCrearIngresoEgreso.p_IdUsuario = in.p_IdUsuario;
		entradaCrearIngresoEgreso.p_IdModulo = in.p_IdModulo;
		entradaCrearIngresoEgreso.p_SimularOperacion = "N";
		entradaCrearIngresoEgreso.p_UsaReglasNegocio = "S";		
		entradaCrearIngresoEgreso.p_EsIngresoAutomatico = "S";
		entradaCrearIngresoEgreso.p_FechaMovimiento = fechaSistema;
		
		
		//Crear movimiento de tesoreria para CONTRAPARTES
		for (EventoCapitalContraparte ecc: lstEventoCapitalContraparte)
		{
			IngresoEgreso_Entrada entradaIngresoEgreso = new IngresoEgreso_Entrada();			
			LocalDate fechaCierre = ecc.FechaCierre.toLocalDate();
			String FechaPago = calcularFechaPago(fechaSistemaDate,fechaCierre,fechaSistema,fechasCalculadas,out,con,in);
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
			    return out; 
			}			
			entradaIngresoEgreso.NumeroReferencia 			= ecc.IdInterno;   // Es un Id autogenerado ya que es producto de una agrupaciÃ³n
			entradaIngresoEgreso.IdTipoMovimiento 			= "MOVING";
			entradaIngresoEgreso.IdTipoPersona 				= "CNT";
			entradaIngresoEgreso.IdPersona 					= ecc.IdPersonaPagoCobro;
			entradaIngresoEgreso.IdPortafolio 				= ecc.IdPortafolioPagoCobro; 
		   	entradaIngresoEgreso.IdMoneda 					= ecc.IdMoneda;
		   	entradaIngresoEgreso.Monto 						= ecc.Monto.compareTo(new BigDecimal(0)) == -1 ? ecc.Monto.multiply(new BigDecimal(-1)) : ecc.Monto;
		   	entradaIngresoEgreso.FechaLiquidacion 			= FechaPago;                                                                      
		   	entradaIngresoEgreso.IdConcepto 				= "CIECCC"; // Cierre evento capital Corte cupÃ³n
		   	entradaIngresoEgreso.IdTipoPersonaTitularPago 	= "CNT";		   	
		   	entradaIngresoEgreso.IdPersonaTitularPago 		= ecc.IdPersonaPagoCobro;
		   	entradaIngresoEgreso.IdPortafolioTitularPago 	= ecc.IdPortafolioPagoCobro;
		   	entradaIngresoEgreso.IdCaja 					= 0L;
		   	entradaIngresoEgreso.SecuenciaAgrupacion 		= "";
		   	entradaIngresoEgreso.SecuenciaRelacionada 		= 0L;		   	
		   	entradaIngresoEgreso.Glosa 						= "CORTE CUPON CONTRAPARTE";									   	
		   	//ParÃ¡metros Referenciales(Opcionales)
		   	entradaIngresoEgreso.IdEmpleadoDigitador 		= 0L;
		   	entradaIngresoEgreso.IdSucursalEmpleadoDigitador = 0L;
		   	entradaIngresoEgreso.IdTipoProducto 			= "";
		   	entradaIngresoEgreso.IdTipoOperacion 			= "CORCUP";									    
		   	entradaIngresoEgreso.IdTipoComprobante 			= "";
		   	entradaIngresoEgreso.IdComprobante 				= 0L;		   	
		   	entradaIngresoEgreso.FechaComprobante 			= FechaPago;
		   	entradaIngresoEgreso.FechaLiquidacionComprobante = FechaPago;
		   	entradaIngresoEgreso.IdOperacion 				= 0L;			
		   	entradaIngresoEgreso.FechaOperacion 			= "0001-01-01"; 
		    entradaIngresoEgreso.IdCondicionLiquidacion 	= "";
		    entradaIngresoEgreso.IdBolsa 					= "";
		    entradaIngresoEgreso.IdMonedaOperacion 			= "";
		    entradaIngresoEgreso.MontoOperacion 			= BigDecimal.ZERO;
		    entradaIngresoEgreso.IdTipoReajuste 			= "";
		    entradaIngresoEgreso.FechaParidad 				= "0001-01-01"; 
		    entradaIngresoEgreso.ValorParidad				= BigDecimal.ZERO;
		    entradaIngresoEgreso.FolioCartera 				= 0L;
		    entradaIngresoEgreso.IdInstrumento 				= ecc.IdInstrumento;
		    entradaIngresoEgreso.IdTipoInstrumento 			= ecc.IdTipoInstrumento;
		    entradaIngresoEgreso.IdClasificacionTipoInstrumento = ecc.IdClasificacionTipoInstrumento;
		    entradaIngresoEgreso.Bilateral 					= "N";
		    entradaIngresoEgreso.GeneraComprobante 			= "N";
		    entradaIngresoEgreso.AgruparEnComprobante 		= "N";
		    entradaIngresoEgreso.MueveCuentaInversion 		= "N";  	// Este campo no se usa en tesoreria(deprecado)
		    entradaIngresoEgreso.TitularPago 				= "S";
		    entradaIngresoEgreso.EsTraspaso 				= "N";		 
		    
		    entradaCrearIngresoEgreso.p_IngresoEgresoGenerar.add(entradaIngresoEgreso);
		}
			

		//Crear movimiento de tesoreria para CLIENTES
		for (EventoCapitalCliente ecc: lstEventoCapitalCliente)
		{
			IngresoEgreso_Entrada entradaIngresoEgreso = new IngresoEgreso_Entrada();
			
			EventoCapital infoEventoCapital = (EventoCapital) mapEventoCapital.get(ecc.IdEventoCapital);
			
			//Si pago es  cuenta inversion no genera mov tesoreria
			if (ecc.IdMedioDePago.equalsIgnoreCase("CIN"))
			{
				continue;
			}


			LocalDate fechaCierre = ecc.FechaCierre.toLocalDate();
			String FechaPago = calcularFechaPago(fechaSistemaDate,fechaCierre,fechaSistema,fechasCalculadas,out,con,in);
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
			    return out;
			}
			
			entradaIngresoEgreso.NumeroReferencia 			= ecc.IdEventoCapitalCliente;
			entradaIngresoEgreso.IdTipoMovimiento 			= "MOVEGR";
			entradaIngresoEgreso.IdTipoPersona 				= "CLI";			
			entradaIngresoEgreso.IdPersona 					= ecc.IdPersona;			
			entradaIngresoEgreso.IdPortafolio 				= ecc.IdPortafolio; 
		   	entradaIngresoEgreso.IdMoneda 					= ecc.IdMoneda;
		   	entradaIngresoEgreso.Monto 						= ecc.Monto;
		   	entradaIngresoEgreso.FechaLiquidacion 			= FechaPago;
		   	entradaIngresoEgreso.IdConcepto 				= "CIECCC"; // Cierre evento capital Corte cupon
		   	entradaIngresoEgreso.IdTipoPersonaTitularPago 	= "CLI";		  
		   	entradaIngresoEgreso.IdPersonaTitularPago 		= ecc.IdPersona;
		   	entradaIngresoEgreso.IdPortafolioTitularPago 	= ecc.IdPortafolio;
		   	entradaIngresoEgreso.IdCaja 					= 0L;
		   	entradaIngresoEgreso.SecuenciaAgrupacion 		= "";
		   	entradaIngresoEgreso.SecuenciaRelacionada 		= 0L;		   	
		   	entradaIngresoEgreso.Glosa 						= "Egreso por Corte de Cupon, Instrumento : " + infoEventoCapital.Nemo;
		   	//ParÃ¡metros Referenciales(Opcionales)
		   	entradaIngresoEgreso.IdEmpleadoDigitador 		= 0L;
		   	entradaIngresoEgreso.IdSucursalEmpleadoDigitador = 0L;
		   	entradaIngresoEgreso.IdTipoProducto 			= "";
		   	entradaIngresoEgreso.IdTipoOperacion 			= "CORCUP";									    
		   	entradaIngresoEgreso.IdTipoComprobante 			= "";
		   	entradaIngresoEgreso.IdComprobante 				= 0L;		   	
		   	entradaIngresoEgreso.FechaComprobante 			= FechaPago;
		   	entradaIngresoEgreso.FechaLiquidacionComprobante = FechaPago;
		
		   	entradaIngresoEgreso.IdOperacion 				= ecc.IdEventoCapitalCliente;			
		   	entradaIngresoEgreso.FechaOperacion 			= "0001-01-01"; 
		    entradaIngresoEgreso.IdCondicionLiquidacion 	= "T0";
		    entradaIngresoEgreso.IdBolsa 					= "";
		    entradaIngresoEgreso.IdMonedaOperacion 			= ecc.IdMoneda;
		    entradaIngresoEgreso.MontoOperacion 			= ecc.Monto;
		    entradaIngresoEgreso.IdTipoReajuste 			= "";
		    entradaIngresoEgreso.FechaParidad 				= "0001-01-01"; 
		    entradaIngresoEgreso.ValorParidad				= BigDecimal.ZERO;
		    entradaIngresoEgreso.FolioCartera 				= 0L;
		    entradaIngresoEgreso.IdInstrumento 				= ecc.IdInstrumento;
		    entradaIngresoEgreso.IdTipoInstrumento 			= ecc.IdTipoInstrumento;
		    entradaIngresoEgreso.IdClasificacionTipoInstrumento = ecc.IdClasificacionTipoInstrumento;
		    entradaIngresoEgreso.Bilateral 					= "N";
		    entradaIngresoEgreso.GeneraComprobante 			= "N";
		    entradaIngresoEgreso.AgruparEnComprobante 		= "N";
		    entradaIngresoEgreso.MueveCuentaInversion 		= "N";  	// Este campo no se usa en tesoreria(deprecado)
		    entradaIngresoEgreso.TitularPago 				= "S";
		    entradaIngresoEgreso.EsTraspaso 				= "N";
	    
		    
		    entradaCrearIngresoEgreso.p_IngresoEgresoGenerar.add(entradaIngresoEgreso);			
	
			}


		//Generar Formas de pago para CLIENTES
		Long countFormnaDePago = 0L;
		
		for (EventoCapitalClienteFormaDePago eccfp: lstEventoCapitalClienteFormaDePago)
		{	
			FormaDePago_Entrada entradaFormaDePago = new FormaDePago_Entrada();
			
			//Si pago es  cuenta inversion no genera mov tesoreria
			if (eccfp.IdMedioDePago.equalsIgnoreCase("CIN"))
			{
				continue;
			}
			
			entradaFormaDePago.NumeroReferencia				= eccfp.IdEventoCapitalCliente;
			entradaFormaDePago.NumeroReferenciaFormaDePago 	= countFormnaDePago;
			entradaFormaDePago.IdMedioDePago 				= eccfp.IdMedioDePago;
			entradaFormaDePago.IdModalidadDePago 			= eccfp.IdModalidadDePago;
			entradaFormaDePago.IdMoneda 					= eccfp.IdMoneda;
			entradaFormaDePago.Monto 						= eccfp.Monto;
			entradaFormaDePago.IdCuentaBancariaCorredora 	= 0L;
			entradaFormaDePago.IdTarjetaBancariaCorredora 	= 0L;
			entradaFormaDePago.IdChequera 					= 0L;
			entradaFormaDePago.NumeroDocumentoCorredora 	= 0L;
			entradaFormaDePago.FechaDocumentoPagoCorredora 	= "0001-01-01";
			entradaFormaDePago.IdCuentaBancariaSocio 		= eccfp.IdCuentaBancariaSocio;
			entradaFormaDePago.IdBancoSocio 				= eccfp.IdBancoSocio;
			entradaFormaDePago.NombreCuentaBancariaSocio 	= eccfp.NombreCuentaBancariaSocio;
			entradaFormaDePago.NumeroCuentaBancariaSocio 	= eccfp.NumeroCuentaBancariaSocio;
			entradaFormaDePago.IdTipoCuentaBancariaSocio 	= eccfp.IdTipoCuentaBancariaSocio;
			entradaFormaDePago.NombreTipoCuentaBancariaSocio = eccfp.IdTipoCuentaBancariaSocio;
			entradaFormaDePago.IdTarjetaBancariaSocio 		= 0L;
			entradaFormaDePago.IdTipoTarjetaBancariaSocio 	= eccfp.IdTipoTarjetaBancariaSocio;
			entradaFormaDePago.NombreTipoTarjetaBancariaSocio = "";
			entradaFormaDePago.NumeroTarjetaBancariaSocio 	= eccfp.NumeroTarjetaBancariaSocio;			
			entradaFormaDePago.NumeroSerieSocio 			= "";
			entradaFormaDePago.NumeroDocumentoSocio 		= 0L;
			entradaFormaDePago.FechaDocumentoPagoSocio 		= "0001-01-01";
			
			entradaCrearIngresoEgreso.p_FormaDePago.add(entradaFormaDePago);
			countFormnaDePago++;
			
		}		
		
		if (entradaCrearIngresoEgreso.p_IngresoEgresoGenerar.size() > 0)
		{
			CrearIngresoEgreso_Salida salidaCrearIngresoEgreso = servicioCrearIngresoEgreso.logica(con, entradaCrearIngresoEgreso);		
			
			if (salidaCrearIngresoEgreso.P_OUT_ERRORESCONTADOR != null)
			if (salidaCrearIngresoEgreso.P_OUT_ERRORESCONTADOR > 0)
			{
				out.P_OUT_MENSAJES.addAll(salidaCrearIngresoEgreso.P_OUT_MENSAJES);
				return out;
			}
			
			
			
			//Obtiene los movimientos generdos para Clientes
			List<IngresoEgresoGenerado_Salida> lstSalidaIngresoEgresoGenerado = new ArrayList<IngresoEgresoGenerado_Salida>();
			
			for (CrearIngresoEgreso.IngresoEgresoGenerado_Salida result : salidaCrearIngresoEgreso.P_OUT_INGRESOEGRESOGENERADO)
			{
				lstSalidaIngresoEgresoGenerado.add(result);
			}
			
			if (lstSalidaIngresoEgresoGenerado.size() > 0)
			{	
				mapSalidaIngresoEgresoGenerado = Utils.List2Hash(lstSalidaIngresoEgresoGenerado, "NumeroReferencia");  // NumeroReferencia contiene el IdEventoCapitalCliente
			}
			
		}
		
		
		// ---------------------------------------------------------------
		// GENERA MOVIMIENTOS EN CUENTA INVERSION

		//ART.104 restauramos valor original del monto para el movimiento en cuenta de inversión

		//Verificamos si instrumento está afecto al ART.104, si es así consultamos parametro para modificar monto del movimiento 

		if ("S".equalsIgnoreCase(afectoArt104)) {
			params.clear();
			BigDecimal porcentaje = BigDecimal.valueOf(valorParametroArt104).divide(BigDecimal.valueOf(100));
			for (EventoCapitalCliente ecc : lstEventoCapitalCliente) {
				// Restauramos el valor original del Monto sumando Retencion
				ecc.Monto = ecc.Monto.add(ecc.Retencion);
    		}
		}
		
		CrearCargoAbono servicioCrearCargoAbono = new CrearCargoAbono();		
		CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
		
		entradaCrearCargoAbono.p_IdEmpresa = in.p_IdEmpresa;
		entradaCrearCargoAbono.p_IdUsuario = in.p_IdUsuario;
		entradaCrearCargoAbono.p_IdModulo = in.p_IdModulo;
		entradaCrearCargoAbono.p_simularOperacion = "N";
		entradaCrearCargoAbono.p_UsaReglasNegocio = "S";
		entradaCrearCargoAbono.p_FechaSistema = fechaSistema;
		entradaCrearCargoAbono.p_EsIngresoManual	= "N";
		

		for (EventoCapitalCliente ecc: lstEventoCapitalCliente)
		{	
			
			if(ecc.Monto.compareTo(BigDecimal.ZERO)>0){
				
				
				
			ObjCargoAbonoAGenerar cargoAbonoAGenerarInteres = new ObjCargoAbonoAGenerar();
			EventoCapital infoEventoCapital = (EventoCapital) mapEventoCapital.get(ecc.IdEventoCapital);
			CrearIngresoEgreso.IngresoEgresoGenerado_Salida infoIngresoEgresoGenerado = (CrearIngresoEgreso.IngresoEgresoGenerado_Salida) mapSalidaIngresoEgresoGenerado.get(ecc.IdEventoCapitalCliente);


		
			LocalDate fechaCierre = ecc.FechaCierre.toLocalDate();
			String FechaPago = calcularFechaPago(fechaSistemaDate,fechaCierre,fechaSistema,fechasCalculadas,out,con,in);
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
			    return out;
			}
			Random random= new Random();
			if(ecc.MontoInteres.compareTo(BigDecimal.ZERO)>0){

			cargoAbonoAGenerarInteres .NumeroCargoAbono 			= ecc.IdEventoCapitalCliente *-2; 
			cargoAbonoAGenerarInteres .IdPortafolio 				= ecc.IdPortafolio; 
			cargoAbonoAGenerarInteres .IdMoneda 					= ecc.IdMoneda; 
			cargoAbonoAGenerarInteres .IdTipoMovimiento 			= "MOVABN"; 
			cargoAbonoAGenerarInteres .IdConcepto 					= "CICCMI";
			cargoAbonoAGenerarInteres .Descripcion 					= "Abono interes por Corte de Cupon, Instrumento : " + infoEventoCapital.Nemo;		
			cargoAbonoAGenerarInteres .IdCondicionDeLiquidacion 	= "T0"; 
			cargoAbonoAGenerarInteres .FechaCondicionDeLiquidacion 	= FechaPago;		
			cargoAbonoAGenerarInteres .Monto					    = ecc.MontoInteres;
			cargoAbonoAGenerarInteres .IdEjecutivo 					= 0L; 
			cargoAbonoAGenerarInteres .IdSucursal 					= 0L; 
			cargoAbonoAGenerarInteres .IdTipoProducto 				= ""; 
			cargoAbonoAGenerarInteres .IdTipoOperacion 				= "";
			cargoAbonoAGenerarInteres .IdOperacion 					= ecc.IdEventoCapitalCliente; 
			cargoAbonoAGenerarInteres .FechaOperacion 				= "0001-01-01"; 
			cargoAbonoAGenerarInteres .FechaLiquidacionOperacion 	= FechaPago; 
			cargoAbonoAGenerarInteres .FechaVencimientoOperacion 	= "0001-01-01";
			cargoAbonoAGenerarInteres .IdMonedaOperacion 			= ecc.IdMoneda; 
			cargoAbonoAGenerarInteres .MontoOperacion    			= ecc.Monto; 
			cargoAbonoAGenerarInteres .TipoCambioOperacion 			= BigDecimal.ZERO; 
			cargoAbonoAGenerarInteres .IdContraparte 				= 0L;		
			cargoAbonoAGenerarInteres .IdTipoComprobante 			= ""; 
			cargoAbonoAGenerarInteres .IdComprobante 				= 0L; 
			cargoAbonoAGenerarInteres .FolioComprobante 			= ""; 
			cargoAbonoAGenerarInteres .IdComprobanteDetalle 		= 0L; 
			cargoAbonoAGenerarInteres .IdComprobanteFormaDePago 	= 0L; 
			cargoAbonoAGenerarInteres .IdTipoMovimientoAsociado 	= infoIngresoEgresoGenerado != null ? infoIngresoEgresoGenerado.IdTipoMovimiento : ""; 
			cargoAbonoAGenerarInteres .IdMovimientoAsociado 		= infoIngresoEgresoGenerado != null ? infoIngresoEgresoGenerado.IdIngresoEgreso : 0L;
			cargoAbonoAGenerarInteres .FechaComprobante 			= "0001-01-01"; 
			cargoAbonoAGenerarInteres .IdPersonaPagoCobro 			= 0L;
			cargoAbonoAGenerarInteres .IdInstrumento 				= ecc.IdInstrumento; 
			cargoAbonoAGenerarInteres .IdTipoInstrumento 			= ecc.IdTipoInstrumento;
			cargoAbonoAGenerarInteres .Cantidad 					= BigDecimal.ZERO;; 
			cargoAbonoAGenerarInteres .Nemo 						= infoEventoCapital.Nemo; 
			cargoAbonoAGenerarInteres .EsIndicadorBilateral 		= ""; 
			cargoAbonoAGenerarInteres .EsCargoAbonoPorTraspaso 		= "N"; 
			cargoAbonoAGenerarInteres .IdPortafolioTraspaso 		= 0L; 
			cargoAbonoAGenerarInteres .GeneroContraparteEnTesoreria = "S";
			cargoAbonoAGenerarInteres .CertificadoAutomatico 		= "S";
			
			
			entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cargoAbonoAGenerarInteres );
			
			}
			
		   if( ecc.Amortizacion.compareTo(BigDecimal.ZERO)>0) {
			
			
			ObjCargoAbonoAGenerar cargoAbonoAGenerarAmortizacion = new ObjCargoAbonoAGenerar();

			cargoAbonoAGenerarAmortizacion.NumeroCargoAbono 			= ecc.IdEventoCapitalCliente; 
			cargoAbonoAGenerarAmortizacion.IdPortafolio 				= ecc.IdPortafolio; 
			cargoAbonoAGenerarAmortizacion.IdMoneda 					= ecc.IdMoneda; 
			cargoAbonoAGenerarAmortizacion.IdTipoMovimiento 			= "MOVABN"; 
			cargoAbonoAGenerarAmortizacion.IdConcepto 					= "CICCAM";
			cargoAbonoAGenerarAmortizacion.Descripcion 					= "Abono Amortizacion por Corte de Cupon, Instrumento : " + infoEventoCapital.Nemo;		
			cargoAbonoAGenerarAmortizacion.IdCondicionDeLiquidacion 	= "T0"; 
			cargoAbonoAGenerarAmortizacion.FechaCondicionDeLiquidacion 	= FechaPago;		
			cargoAbonoAGenerarAmortizacion.Monto         				= ecc.Amortizacion;
			cargoAbonoAGenerarAmortizacion.IdEjecutivo 					= 0L; 
			cargoAbonoAGenerarAmortizacion.IdSucursal 					= 0L; 
			cargoAbonoAGenerarAmortizacion.IdTipoProducto 				= ""; 
			cargoAbonoAGenerarAmortizacion.IdTipoOperacion 				= "";
			cargoAbonoAGenerarAmortizacion.IdOperacion 					= ecc.IdEventoCapitalCliente; 
			cargoAbonoAGenerarAmortizacion.FechaOperacion 				= "0001-01-01"; 
			cargoAbonoAGenerarAmortizacion.FechaLiquidacionOperacion 	= FechaPago; 
			cargoAbonoAGenerarAmortizacion.FechaVencimientoOperacion 	= "0001-01-01";
			cargoAbonoAGenerarAmortizacion.IdMonedaOperacion 			= ecc.IdMoneda; 
			cargoAbonoAGenerarAmortizacion.MontoOperacion    			= ecc.Monto; 
			cargoAbonoAGenerarAmortizacion.TipoCambioOperacion 			= BigDecimal.ZERO; 
			cargoAbonoAGenerarAmortizacion.IdContraparte 				= 0L;		
			cargoAbonoAGenerarAmortizacion.IdTipoComprobante 			= ""; 
			cargoAbonoAGenerarAmortizacion.IdComprobante 				= 0L; 
			cargoAbonoAGenerarAmortizacion.FolioComprobante 			= ""; 
			cargoAbonoAGenerarAmortizacion.IdComprobanteDetalle 		= 0L; 
			cargoAbonoAGenerarAmortizacion.IdComprobanteFormaDePago 	= 0L; 
			cargoAbonoAGenerarAmortizacion.IdTipoMovimientoAsociado 	= infoIngresoEgresoGenerado != null ? infoIngresoEgresoGenerado.IdTipoMovimiento : ""; 
			cargoAbonoAGenerarAmortizacion.IdMovimientoAsociado 		= infoIngresoEgresoGenerado != null ? infoIngresoEgresoGenerado.IdIngresoEgreso : 0L;
			cargoAbonoAGenerarAmortizacion.FechaComprobante 			= "0001-01-01"; 
			cargoAbonoAGenerarAmortizacion.IdPersonaPagoCobro 			= 0L;
			cargoAbonoAGenerarAmortizacion.IdInstrumento 				= ecc.IdInstrumento; 
			cargoAbonoAGenerarAmortizacion.IdTipoInstrumento 			= ecc.IdTipoInstrumento;
			cargoAbonoAGenerarAmortizacion.Cantidad 					= BigDecimal.ZERO;; 
			cargoAbonoAGenerarAmortizacion.Nemo 						= infoEventoCapital.Nemo; 
			cargoAbonoAGenerarAmortizacion.EsIndicadorBilateral 		= ""; 
			cargoAbonoAGenerarAmortizacion.EsCargoAbonoPorTraspaso 		= "N"; 
			cargoAbonoAGenerarAmortizacion.IdPortafolioTraspaso 		= 0L; 
			cargoAbonoAGenerarAmortizacion.GeneroContraparteEnTesoreria = "S";
			cargoAbonoAGenerarAmortizacion.CertificadoAutomatico 		= "S";
			
			entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cargoAbonoAGenerarAmortizacion);
			
		   }
		}
		}
		
		

		if("S".equalsIgnoreCase(afectoArt104))
		{	
			//ART.104: Verificamos si instrumento está afecto al ART.104, si es así significa que hubo descuento del 4% y debemos generar el movimiento de cargo en cuenta de inversion
			//Si Existe retención del Art104 se debe generar un movimiento en la cuenta de inversión por el valor de la retención 
			for (EventoCapitalCliente ecc: lstEventoCapitalCliente)
			{	
				ObjCargoAbonoAGenerar cargoAbonoArt104AGenerar = new ObjCargoAbonoAGenerar();
				EventoCapital infoEventoCapital = (EventoCapital) mapEventoCapital.get(ecc.IdEventoCapital);
				CrearIngresoEgreso.IngresoEgresoGenerado_Salida infoIngresoEgresoGenerado = (CrearIngresoEgreso.IngresoEgresoGenerado_Salida) mapSalidaIngresoEgresoGenerado.get(ecc.IdEventoCapitalCliente);



				LocalDate fechaCierre = ecc.FechaCierre.toLocalDate();
				String FechaPago = calcularFechaPago(fechaSistemaDate,fechaCierre,fechaSistema,fechasCalculadas,out,con,in);
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
				    return out;
				}
				
						cargoAbonoArt104AGenerar.NumeroCargoAbono 				= ecc.IdEventoCapitalCliente; 
						cargoAbonoArt104AGenerar.IdPortafolio 					= ecc.IdPortafolio; 
						cargoAbonoArt104AGenerar.IdMoneda 						= ecc.IdMoneda; 
						cargoAbonoArt104AGenerar.IdTipoMovimiento 				= "MOVCAR"; 
						cargoAbonoArt104AGenerar.IdConcepto 					= "RET104";
						cargoAbonoArt104AGenerar.Descripcion 					= "Retención Art.104, Instrumento : " + infoEventoCapital.Nemo;		
						cargoAbonoArt104AGenerar.IdCondicionDeLiquidacion 		= "T0"; 
						cargoAbonoArt104AGenerar.FechaCondicionDeLiquidacion 	= FechaPago;		
						cargoAbonoArt104AGenerar.Monto 							= ecc.Retencion;
						cargoAbonoArt104AGenerar.IdEjecutivo 					= 0L; 
						cargoAbonoArt104AGenerar.IdSucursal 					= 0L; 
						cargoAbonoArt104AGenerar.IdTipoProducto 				= ""; 
						cargoAbonoArt104AGenerar.IdTipoOperacion 				= "";
						cargoAbonoArt104AGenerar.IdOperacion 					= ecc.IdEventoCapitalCliente; 
						cargoAbonoArt104AGenerar.FechaOperacion 				= "0001-01-01"; 
						cargoAbonoArt104AGenerar.FechaLiquidacionOperacion 		= FechaPago; 
						cargoAbonoArt104AGenerar.FechaVencimientoOperacion 		= "0001-01-01";
						cargoAbonoArt104AGenerar.IdMonedaOperacion 				= ecc.IdMoneda; 
						cargoAbonoArt104AGenerar.MontoOperacion 				= ecc.Monto; 
						cargoAbonoArt104AGenerar.TipoCambioOperacion 			= BigDecimal.ZERO; 
						cargoAbonoArt104AGenerar.IdContraparte 					= 0L;		
						cargoAbonoArt104AGenerar.IdTipoComprobante 				= ""; 
						cargoAbonoArt104AGenerar.IdComprobante 					= 0L; 
						cargoAbonoArt104AGenerar.FolioComprobante 				= ""; 
						cargoAbonoArt104AGenerar.IdComprobanteDetalle 			= 0L; 
						cargoAbonoArt104AGenerar.IdComprobanteFormaDePago 		= 0L; 
						cargoAbonoArt104AGenerar.IdTipoMovimientoAsociado 		= infoIngresoEgresoGenerado != null ? infoIngresoEgresoGenerado.IdTipoMovimiento : ""; 
						cargoAbonoArt104AGenerar.IdMovimientoAsociado 			= infoIngresoEgresoGenerado != null ? infoIngresoEgresoGenerado.IdIngresoEgreso : 0L;
						cargoAbonoArt104AGenerar.FechaComprobante 				= "0001-01-01"; 
						cargoAbonoArt104AGenerar.IdPersonaPagoCobro 			= 0L;
						cargoAbonoArt104AGenerar.IdInstrumento 					= ecc.IdInstrumento; 
						cargoAbonoArt104AGenerar.IdTipoInstrumento 				= ecc.IdTipoInstrumento;
						cargoAbonoArt104AGenerar.Cantidad 						= BigDecimal.ZERO;; 
						cargoAbonoArt104AGenerar.Nemo 							= infoEventoCapital.Nemo; 
						cargoAbonoArt104AGenerar.EsIndicadorBilateral 			= ""; 
						cargoAbonoArt104AGenerar.EsCargoAbonoPorTraspaso 		= "N"; 
						cargoAbonoArt104AGenerar.IdPortafolioTraspaso 			= 0L; 
						cargoAbonoArt104AGenerar.GeneroContraparteEnTesoreria 	= "S";
						cargoAbonoArt104AGenerar.CertificadoAutomatico 			= "S";
								
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cargoAbonoArt104AGenerar);

					}}				


		// CARGO A GENERAR EN EL CASO DE LOS CLIENTES "CUENTA MANDANTE" QUE TENGAN CONFIGURADO QUE EL PAGO
		// DE LOS DIVIDENDOS SE REALIZARÃ� DIRECTAMENTE AL CLIENTE Y NO MEDIANTE LA CORREDORA. JIRA "SAP-5857"		
		for (EventoCapitalCliente ecc: lstEventoCapitalCliente)
		{	
			ObjCargoAbonoAGenerar cargoAbonoAGenerar = new ObjCargoAbonoAGenerar();
			EventoCapital infoEventoCapital = (EventoCapital) mapEventoCapital.get(ecc.IdEventoCapital);
			CrearIngresoEgreso.IngresoEgresoGenerado_Salida infoIngresoEgresoGenerado = (CrearIngresoEgreso.IngresoEgresoGenerado_Salida) mapSalidaIngresoEgresoGenerado.get(ecc.IdEventoCapitalCliente);
			
			//Sino no paga Cliente no genera mov en cta inversiÃ³n
			if (ecc.PagaClienteCorredor != "CL" )
			{
				continue;
			}

			LocalDate fechaCierre = ecc.FechaCierre.toLocalDate();
			String FechaPago = calcularFechaPago(fechaSistemaDate,fechaCierre,fechaSistema,fechasCalculadas,out,con,in);
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
			    return out;
			}
			
			cargoAbonoAGenerar.NumeroCargoAbono 			= ecc.IdEventoCapitalCliente; 
			cargoAbonoAGenerar.IdPortafolio 				= ecc.IdPortafolio; 
			cargoAbonoAGenerar.IdMoneda 					= ecc.IdMoneda; 
			cargoAbonoAGenerar.IdTipoMovimiento 			= "MOVCAR"; 
			cargoAbonoAGenerar.IdConcepto 					= "CIECCC";
			cargoAbonoAGenerar.Descripcion 					= "Cargo - Pago por Emisor - Corte de Cupon, Instrumento : " + infoEventoCapital.Nemo;		
			cargoAbonoAGenerar.IdCondicionDeLiquidacion 	= "T0"; 
			cargoAbonoAGenerar.FechaCondicionDeLiquidacion 	= FechaPago;		
			cargoAbonoAGenerar.Monto 						= ecc.Monto;
			cargoAbonoAGenerar.IdEjecutivo 					= 0L; 
			cargoAbonoAGenerar.IdSucursal 					= 0L; 
			cargoAbonoAGenerar.IdTipoProducto 				= ""; 
			cargoAbonoAGenerar.IdTipoOperacion 				= "";
			cargoAbonoAGenerar.IdOperacion 					= ecc.IdEventoCapitalCliente; 
			cargoAbonoAGenerar.FechaOperacion 				= "0001-01-01"; 
			cargoAbonoAGenerar.FechaLiquidacionOperacion 	= FechaPago; 
			cargoAbonoAGenerar.FechaVencimientoOperacion 	= "0001-01-01";
			cargoAbonoAGenerar.IdMonedaOperacion 			= ecc.IdMoneda; 
			cargoAbonoAGenerar.MontoOperacion 				= ecc.Monto; 
			cargoAbonoAGenerar.TipoCambioOperacion 			= BigDecimal.ZERO; 
			cargoAbonoAGenerar.IdContraparte 				= 0L;		
			cargoAbonoAGenerar.IdTipoComprobante 			= ""; 
			cargoAbonoAGenerar.IdComprobante 				= 0L; 
			cargoAbonoAGenerar.FolioComprobante 			= ""; 
			cargoAbonoAGenerar.IdComprobanteDetalle 		= 0L; 
			cargoAbonoAGenerar.IdComprobanteFormaDePago 	= 0L; 
			cargoAbonoAGenerar.IdTipoMovimientoAsociado 	= infoIngresoEgresoGenerado != null ? infoIngresoEgresoGenerado.IdTipoMovimiento : ""; 
			cargoAbonoAGenerar.IdMovimientoAsociado 		= infoIngresoEgresoGenerado != null ? infoIngresoEgresoGenerado.IdIngresoEgreso : 0L;
			cargoAbonoAGenerar.FechaComprobante 			= "0001-01-01"; 
			cargoAbonoAGenerar.IdPersonaPagoCobro 			= 0L;
			cargoAbonoAGenerar.IdInstrumento 				= ecc.IdInstrumento; 
			cargoAbonoAGenerar.IdTipoInstrumento 			= ecc.IdTipoInstrumento;
			cargoAbonoAGenerar.Cantidad 					= BigDecimal.ZERO;; 
			cargoAbonoAGenerar.Nemo 						= infoEventoCapital.Nemo; 
			cargoAbonoAGenerar.EsIndicadorBilateral 		= ""; 
			cargoAbonoAGenerar.EsCargoAbonoPorTraspaso 		= "N"; 
			cargoAbonoAGenerar.IdPortafolioTraspaso 		= 0L; 
			cargoAbonoAGenerar.GeneroContraparteEnTesoreria = "S";
			cargoAbonoAGenerar.CertificadoAutomatico 		= "S";
			
			entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cargoAbonoAGenerar);
			
		}		

		//CELLINCECT-809 : Genera movimiento de contrapartes en cuenta inversion
		// CONSULTA DATOS CONTRAPARTE		
		params.clear();
		params.put("IdEventoCapital", in.p_IdEventoCapital);
		params.put("IdTipoEventoCapital", in.p_IdTipoEventoCapital);
		params.put("LocalizacionSistema", localizacionSistema);
			
		List<EventoCapitalContraparte> lstContraparte = sql.query(con, SQL_SELECT_INFO_ECC_CONTRAPARTE, params, EventoCapitalContraparte.class);

		for (EventoCapitalContraparte ecc: lstContraparte)
		{	
			ObjCargoAbonoAGenerar cargoAbonoAGenerar = new ObjCargoAbonoAGenerar();
			EventoCapital infoEventoCapital = (EventoCapital) mapEventoCapital.get(ecc.IdEventoCapital);
			CrearIngresoEgreso.IngresoEgresoGenerado_Salida infoIngresoEgresoGenerado = (CrearIngresoEgreso.IngresoEgresoGenerado_Salida) mapSalidaIngresoEgresoGenerado.get(ecc.IdEventoCapitalCliente);

			LocalDate fechaCierre = ecc.FechaCierre.toLocalDate();
			String FechaPago = calcularFechaPago(fechaSistemaDate,fechaCierre,fechaSistema,fechasCalculadas,out,con,in);
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
			    return out;
			}	
			
			if(ecc.IdPortafolioSocioNegocio > 0) {
				
			cargoAbonoAGenerar.NumeroCargoAbono 			= ecc.IdInterno; 
			cargoAbonoAGenerar.IdPortafolio 				= ecc.IdPortafolioSocioNegocio; 
			cargoAbonoAGenerar.IdMoneda 					= ecc.IdMoneda; 
			cargoAbonoAGenerar.IdTipoMovimiento 			= "MOVCAR"; 
			cargoAbonoAGenerar.IdConcepto 					= "CIECCC";
			cargoAbonoAGenerar.Descripcion 					= "CORTE CUPON CONTRAPARTE";		
			cargoAbonoAGenerar.IdCondicionDeLiquidacion 	= "T0"; 
			cargoAbonoAGenerar.FechaCondicionDeLiquidacion 	= FechaPago;		
			cargoAbonoAGenerar.Monto 						= ecc.Monto.abs();
			cargoAbonoAGenerar.IdEjecutivo 					= 0L; 
			cargoAbonoAGenerar.IdSucursal 					= 0L; 
			cargoAbonoAGenerar.IdTipoProducto 				= ""; 
			cargoAbonoAGenerar.IdTipoOperacion 				= "";
			cargoAbonoAGenerar.IdOperacion 					= ecc.IdEventoCapitalCliente; 
			cargoAbonoAGenerar.FechaOperacion 				= "0001-01-01"; 
			cargoAbonoAGenerar.FechaLiquidacionOperacion 	= FechaPago; 
			cargoAbonoAGenerar.FechaVencimientoOperacion 	= "0001-01-01";
			cargoAbonoAGenerar.IdMonedaOperacion 			= ecc.IdMoneda; 
			cargoAbonoAGenerar.MontoOperacion 				= ecc.Monto; 
			cargoAbonoAGenerar.TipoCambioOperacion 			= BigDecimal.ZERO; 
			cargoAbonoAGenerar.IdContraparte 				= 0L;		
			cargoAbonoAGenerar.IdTipoComprobante 			= ""; 
			cargoAbonoAGenerar.IdComprobante 				= 0L; 
			cargoAbonoAGenerar.FolioComprobante 			= ""; 
			cargoAbonoAGenerar.IdComprobanteDetalle 		= 0L; 
			cargoAbonoAGenerar.IdComprobanteFormaDePago 	= 0L; 
			cargoAbonoAGenerar.IdTipoMovimientoAsociado 	= infoIngresoEgresoGenerado != null ? infoIngresoEgresoGenerado.IdTipoMovimiento : ""; 
			cargoAbonoAGenerar.IdMovimientoAsociado 		= infoIngresoEgresoGenerado != null ? infoIngresoEgresoGenerado.IdIngresoEgreso : 0L;
			cargoAbonoAGenerar.FechaComprobante 			= "0001-01-01"; 
			cargoAbonoAGenerar.IdPersonaPagoCobro 			= 0L;
			cargoAbonoAGenerar.IdInstrumento 				= ecc.IdInstrumento; 
			cargoAbonoAGenerar.IdTipoInstrumento 			= ecc.IdTipoInstrumento;
			cargoAbonoAGenerar.Cantidad 					= BigDecimal.ZERO;; 
			cargoAbonoAGenerar.Nemo 						= infoEventoCapital.Nemo; 
			cargoAbonoAGenerar.EsIndicadorBilateral 		= ""; 
			cargoAbonoAGenerar.EsCargoAbonoPorTraspaso 		= "N"; 
			cargoAbonoAGenerar.IdPortafolioTraspaso 		= 0L;  
			cargoAbonoAGenerar.GeneroContraparteEnTesoreria = "S";
			cargoAbonoAGenerar.CertificadoAutomatico 		= "S";
			
			entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cargoAbonoAGenerar);
			
			}
		}

		if (entradaCrearCargoAbono.p_CargoAbonoAGenerar.size() > 0)
		{	
			CrearCargoAbonoSalida salidaCrearCargoAbono = servicioCrearCargoAbono.logica(con, entradaCrearCargoAbono);
			
			if (salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR > 0)
			{		
				out.P_OUT_MENSAJES.addAll(salidaCrearCargoAbono.P_OUT_MENSAJES);
				return out;
			}
		}

		
		// --------------------------------------------------
		// ACTUALIZAR ESTADO EVENTO CAPITAL Y ECC
		
		EventoCapital eventoActualizar = new EventoCapital();
		eventoActualizar.IdEventoCapital = in.p_IdEventoCapital;
		
		List<EventoCapital> actualizar = new ArrayList<EventoCapital>();
		actualizar.add(eventoActualizar);
		
		sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, actualizar, EventoCapital.class);		
		sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, actualizar, EventoCapital.class);

		
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_OPERACION_PROCESAR_EXITO",new HashMap<String,String>(), con, in.p_IdEmpresa));
		
		// crear objeto de tipo noticacion
		if (out.P_OUT_ERRORESCONTADOR==0) {
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
		
		log.write("SALIDA > ", out.P_OUT_MENSAJES);
		return out;
	}

	
	
	
	//-----------------------------------------------------------------
	// CALL
	//-----------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		///CerrarEventoCapitalCorteCupon
		CerrarEventoCapitalCorteCuponEntrada jin = gson.fromJson(in.toString(), CerrarEventoCapitalCorteCuponEntrada.class);		
		CerrarEventoCapitalCorteCuponSalida out = logica(conn,jin);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	
	public String calcularFechaPago(LocalDate fechaSistemaDate, LocalDate fechaCierre,String fechaSistema, Map<String, Object> fechasCalculadas,
			CerrarEventoCapitalCorteCuponSalida out, Connection con, CerrarEventoCapitalCorteCuponEntrada in) throws Exception {
	    String fechaPago = null;

	    FechasOut fechaNueva = (FechasOut) fechasCalculadas.get(fechaSistema);

	    if (!fechaSistemaDate.isBefore(fechaCierre)) {
	        if (fechaNueva != null) {
	            fechaPago = fechaNueva.fechaCalculada;
	        } else {
	            fechaPago = fechaSistema;
	             
	        }
	    } else {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO",new HashMap<String, String>(),con,in.p_IdEmpresa));
            System.out.println(out.P_OUT_MENSAJES);
        }

        return fechaPago;
	}
	
	//-----------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-----------------------------------------------------------------
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("master02","connections.properties")) 
		{
			try 
			{	
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":46936,\"p_IdTipoEventoCapital\":\"CC\"}";
				CerrarEventoCapitalCorteCuponEntrada Objin = gson.fromJson(json, CerrarEventoCapitalCorteCuponEntrada.class);
								
				CerrarEventoCapitalCorteCupon  kk = new CerrarEventoCapitalCorteCupon();
				kk.logica(con, Objin);
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
