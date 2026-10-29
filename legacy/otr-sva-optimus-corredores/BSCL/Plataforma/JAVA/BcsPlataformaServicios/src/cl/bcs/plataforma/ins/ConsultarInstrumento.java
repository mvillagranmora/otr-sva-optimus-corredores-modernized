package cl.bcs.plataforma.ins;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarFlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaEntrada;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaSalida;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoEsAgrupadoPor;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoFiltrosBusqueda;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoUso;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarInstrumento implements JavaCall{
	private static final Logger logger = LogManager.getLogger(ConsultarInstrumento.class);

	private String SQL_SELECT_CONSULTAR_INSTRUMENTO_UI = loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultarInstrumento_ConsultarInstrumento.sql");
	private String SQL_SELECT_CONSULTAR_INSTRUMENTO_UI_CUS = loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultarInstrumento_ConsultarInstrumentoCUS.sql");

	public static class ConsultarInstrumentoEntrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_FechaConsulta;
		public String 	p_UsoTipoOperacion;//"FLJORD";
		public String	p_BusquedaEspecifica;// 	= "FLUJO_ORDEN"
		public String 	p_TipoOperacion; // C, V (Compra, Venta)
		public Integer	p_DiasProyeccion;

		//COnsultarInstrumento.sql ((IdClasificacionTipoInstrumento eq 'RV' or IdClasificacionTipoInstrumento eq 'DV') and IdEstado eq 'ACT')
		//ConsultarFlujoCustodia.java {"p_IdEmpresa":1,"p_IdUsuario":9,"p_IdModulo":"PLA","p_FechaSaldo":"2019-07-08","p_IdPais":44,"p_DiasProyeccion":5,"p_DiaHabil":"S","p_ValorizarFlujo":"S","p_Uso":[{"UsoTipoOperacion":"FLJORD","IdCliente":null,"IdPortafolio":27499,"BusquedaEspecifica":"FLUJO_ORDEN"}],"p_FiltrosBusqueda":[{"IdClasificacionTipoInstrumento":"RV","IdInstrumento":null,"IdEmisor":null,"FechaVencimiento":null,"IdTipoReajuste":null,"EsFungible":null,"IdBoveda":"DCV"},{"IdClasificacionTipoInstrumento":"DV","IdInstrumento":null,"IdEmisor":null,"FechaVencimiento":null,"IdTipoReajuste":null,"EsFungible":null,"IdBoveda":"DCV"}],"p_EsAgrupadoPor":[{"ClasificacionTipoInstrumento":null,"TipoInstrumento":null,"MercadoLocal":null,"Instrumento":"S","Emisor":null,"Nemotecnico":null,"NemoSVS":null,"FechaEmisionInstrumento":null,"FechaVencimientoInstrumento":null,"FechaLiquidacionInstrumento":null,"TipoReajuste":null,"EsFungible":null,"CodigoIFRS":null,"Boveda":"S","Bolsa":null,"TipoOperacion":null,"Mesa":null,"FolioRegistro":null,"ItemOrigen":null}]}
		public ArrayList<DatosIn> p_Datos;
		
		public ConsultarInstrumentoEntrada() {
			this.p_Datos = new ArrayList<DatosIn>();

		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class DatosIn {
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String IdBoveda;
		public String Nemotecnico;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ConsultarInstrumentoSalida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<Instrumento>  			P_OUT_INSTRUMENTO;
		
		public ConsultarInstrumentoSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_INSTRUMENTO = new ArrayList<Instrumento>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Instrumento {
		public Long		IdInstrumento;
		public String	Nemotecnico;
		public String	ISIN;
		public BigDecimal	CorteMinimo;
		public String 	EstaInscrito;
		public String 	EsDivisible;
		public String 	RazonSocial;
		public Date 	FechaEmision;
		public Date 	FechaVencimiento;
		public Long 	IdInstrumentoRelacionado;
		public String 	IdEmisor;
		public String 	DocumentoIdentidad;
		public String 	IdTipoReajuste;
		public String 	Reajustable;
		public String 	IdMoneda;
		public String 	IdMonedaEmision;
		public String 	IdTipoInstrumento;
		public String 	IdEstado;
		public Integer 	DecimalesPrecio;
		public String 	NombreTipoInstrumento;
		public String 	IdClasificacionTipoInstrumento; 
		public String 	NombreClasificacionTipoInstrumento;
		public String 	NombreCompletoEmisor;
		public Long 	Tamano;
		public String 	TipoValorizacion;
		public String 	IndicadorBolson;
		public BigDecimal 	Tasa;
		public BigDecimal 	Precio;
		public String 	ValorizaPor;
		public String 	TieneCupon;
		public Integer 	Cantidad;
		public BigDecimal 	PorcentajeCastigo;
		public BigDecimal 	FrecuenciaCupon;
		public BigDecimal 	PrecioStrike;
		public String 	InstrumentoAcreditado;
		public String 	CallConfirmado;
		public BigDecimal 	TasaEmision;
		public String 	MercadoLocal;
		public String 	ValorClasificacionRiesgo;
		public String 	EsPutEsCall;
		public String 	IdTipoInstrumentoOrigen;
		
		public Long		IdPortafolio;
		public String 	IdBoveda;
		public Date 	FechaSaldo;
		public Long		Dias;
		public String IdMonedaInstrumento;
		public BigDecimal	PrecioMercado;
		public BigDecimal	MontoAMercadoMonedaSistema;
		public BigDecimal	MontoAMercadoMonedaInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Trace
	public ConsultarInstrumentoSalida logica(Connection con, ConsultarInstrumentoEntrada in) throws Exception {
		SQL sql = new SQL();
		Long Id = 0L;
		ConsultarInstrumentoSalida salida = new ConsultarInstrumentoSalida();
		//COnsultarInstrumento.sql ((IdClasificacionTipoInstrumento eq 'RV' or IdClasificacionTipoInstrumento eq 'DV') and IdEstado eq 'ACT')
		//ConsultarFlujoCustodia.java {"p_IdEmpresa":1,"p_IdUsuario":9,"p_IdModulo":"PLA","p_FechaSaldo":"2019-07-08","p_IdPais":44,"p_DiasProyeccion":5,"p_DiaHabil":"S","p_ValorizarFlujo":"S","p_Uso":[{"UsoTipoOperacion":"FLJORD","IdCliente":null,"IdPortafolio":27499,"BusquedaEspecifica":"FLUJO_ORDEN"}],"p_FiltrosBusqueda":[{"IdClasificacionTipoInstrumento":"RV","IdInstrumento":null,"IdEmisor":null,"FechaVencimiento":null,"IdTipoReajuste":null,"EsFungible":null,"IdBoveda":"DCV"},{"IdClasificacionTipoInstrumento":"DV","IdInstrumento":null,"IdEmisor":null,"FechaVencimiento":null,"IdTipoReajuste":null,"EsFungible":null,"IdBoveda":"DCV"}],"p_EsAgrupadoPor":[{"ClasificacionTipoInstrumento":null,"TipoInstrumento":null,"MercadoLocal":null,"Instrumento":"S","Emisor":null,"Nemotecnico":null,"NemoSVS":null,"FechaEmisionInstrumento":null,"FechaVencimientoInstrumento":null,"FechaLiquidacionInstrumento":null,"TipoReajuste":null,"EsFungible":null,"CodigoIFRS":null,"Boveda":"S","Bolsa":null,"TipoOperacion":null,"Mesa":null,"FolioRegistro":null,"ItemOrigen":null}]}
		ArrayList<String> ClasificacionTipoInstrumento = new ArrayList<String>();
		ArrayList<String> Nemotecnicos = new ArrayList<String>();
		ArrayList<String> IdTipoInstrumento = new ArrayList<String>();
		ArrayList<Long> IdInstrumento = new ArrayList<Long>();
		ArrayList<String> Boveda = new ArrayList<String>();
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		//consulto Flujo Custodia
		ConsultarFlujoCustodia flujo = new ConsultarFlujoCustodia();
		FlujoCustodiaSalida flujoSalida = new FlujoCustodiaSalida();
		//params.put("p_Uso":[{"UsoTipoOperacion":"FLJORD","IdCliente":null,"IdPortafolio":27499,"BusquedaEspecifica":"FLUJO_ORDEN"}]
		//	,"p_FiltrosBusqueda":[{"IdClasificacionTipoInstrumento":"RV","IdInstrumento":null,"IdEmisor":null,"FechaVencimiento":null,"IdTipoReajuste":null,"EsFungible":null,"IdBoveda":"DCV"},{"IdClasificacionTipoInstrumento":"DV","IdInstrumento":null,"IdEmisor":null,"FechaVencimiento":null,"IdTipoReajuste":null,"EsFungible":null,"IdBoveda":"DCV"}]
		//	,"p_EsAgrupadoPor":[{"ClasificacionTipoInstrumento":null,"TipoInstrumento":null,"MercadoLocal":null,"Instrumento":"S","Emisor":null,"Nemotecnico":null,"NemoSVS":null,"FechaEmisionInstrumento":null,"FechaVencimientoInstrumento":null,"FechaLiquidacionInstrumento":null,"TipoReajuste":null,"EsFungible":null,"CodigoIFRS":null,"Boveda":"S","Bolsa":null,"TipoOperacion":null,"Mesa":null,"FolioRegistro":null,"ItemOrigen":null}]}
		
		//validaciones
		if (in.p_Datos.size() == 0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_DATOS",null,con,in.p_IdEmpresa));
			Id++;			
		}
		if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) !=  0) {
			return salida;
		}
		FlujoCustodiaEntrada flujoIn = new FlujoCustodiaEntrada();
		flujoIn.p_IdEmpresa = in.p_IdEmpresa;
		flujoIn.p_IdUsuario = in.p_IdUsuario;
		flujoIn.p_IdModulo = in.p_IdModulo;
		flujoIn.p_FechaSaldo = in.p_FechaConsulta;
		flujoIn.p_IdPais = idPaisEmpresa;
		flujoIn.p_DiasProyeccion = in.p_DiasProyeccion; //Integer.valueOf(5);
		flujoIn.p_DiaHabil = "S";
		flujoIn.p_ValorizarFlujo = "S";
		
		for (DatosIn dat: in.p_Datos) {
			if (ClasificacionTipoInstrumento != null) {
				ClasificacionTipoInstrumento.add(dat.IdClasificacionTipoInstrumento);
			}
			if (dat.Nemotecnico != null) {
				Nemotecnicos.add(dat.Nemotecnico);
			}
			if (dat.IdBoveda != null) {
				Boveda.add(dat.IdBoveda);
			}
			if (dat.IdTipoInstrumento != null) {
				IdTipoInstrumento.add(dat.IdTipoInstrumento);
			}
			
			FlujoUso uso = new FlujoUso();
			uso.UsoTipoOperacion 	= in.p_UsoTipoOperacion;
			uso.IdCliente 			= dat.IdCliente;
			uso.IdPortafolio 		= dat.IdPortafolio;
			uso.BusquedaEspecifica 	= in.p_BusquedaEspecifica;
			uso.IdTipoInstrumentoEspecifico = null;
			
			FlujoFiltrosBusqueda bus = new FlujoFiltrosBusqueda();
			bus.IdClasificacionTipoInstrumento = dat.IdClasificacionTipoInstrumento;
			bus.IdTipoInstrumento 	= dat.IdTipoInstrumento;
			bus.IdInstrumento 		= null;
			bus.IdEmisor 			= null;
			bus.FechaVencimiento 	= null;
			bus.IdTipoReajuste 		= null;
			bus.EsFungible 			= null;
			bus.IdBoveda			= dat.IdBoveda; 
			bus.IdRegimenFondoMutuo = null;
						
			if ("V".equalsIgnoreCase(in.p_TipoOperacion) && uso.IdPortafolio != null) {
				flujoIn.p_Uso.add(uso);
				
				flujoIn.p_FiltrosBusqueda.add(bus);
			}
		}
		if (flujoIn.p_Uso.size() > 0) {
			logger.debug("Inicio FLUJO_CUSTODIA");
			FlujoEsAgrupadoPor agr = new FlujoEsAgrupadoPor();
			agr.ClasificacionTipoInstrumento = null;
			agr.TipoInstrumento = null;
			agr.Instrumento = "S";
			agr.Emisor = null;
			agr.Nemotecnico = null;
			agr.NemoSVS = null;
			agr.FechaEmisionInstrumento = null;
			agr.FechaVencimientoInstrumento = null;
			agr.FechaLiquidacionInstrumento = null;
			agr.TipoReajuste = null;
			agr.EsFungible = null;
			agr.CodigoIFRS = null;
			agr.Boveda = "S";
			agr.Bolsa = null;
			agr.TipoOperacion = null;
			agr.Mesa = null;
			agr.FolioRegistro = null;
			agr.ItemOrigen = null;
			agr.IdRegimenFondoMutuo = null;
			agr.GrupoRegimen = null;
			agr.MonedaInstrumento = null;
			flujoIn.p_EsAgrupadoPor.add(agr);
			
			flujoSalida = flujo.logica(con, flujoIn);
			
			salida.P_OUT_MENSAJES.addAll(flujoSalida.P_OUT_MENSAJES);
			
			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) ==  0 && flujoSalida.p_out_FlujoDetallado.size() > 0) {
				HashMap<String, Object> params = new HashMap<String, Object>();
				//BUSCAR POR DIAS (0)LOS INTRUMENTO PAR AQUE NO SE REPITA Y COTEJAR CON LO DE LA BUSQUEDA.
				//AGERGAR LA CONDICON DE LIQYUIDACION PARA QUE 
				//Nemotecnicos = null;
				IdInstrumento.clear();
				for (FlujoCustodia fl: flujoSalida.p_out_FlujoTotalizado) {
					IdInstrumento.add(fl.IdInstrumento);
				}
				//params.put("IdClasificacionTipoInstrumento", ClasificacionTipoInstrumento);
				//params.put("Nemotecnico", Nemotecnicos);
				//params.put("IdTipoInstrumento", IdTipoInstrumento);
				params.put("IdInstrumento", IdInstrumento);
				params.put("IdEstado", "ACT");
				logger.debug("Inicio SQL_SELECT_CONSULTAR_INSTRUMENTO_UI");
				List<Instrumento> instrumentos = sql.query(con, SQL_SELECT_CONSULTAR_INSTRUMENTO_UI_CUS, params, Instrumento.class);
				Map<String, Object> instrumentosMap = Utils.List2Hash(instrumentos, "IdInstrumento");
				for (FlujoCustodia fl: flujoSalida.p_out_FlujoTotalizado) {
					Instrumento i = (Instrumento) instrumentosMap.get(fl.IdInstrumento);
					if (i != null && IdTipoInstrumento.contains(i.IdTipoInstrumento)) {
						Instrumento ins = new Instrumento();
						ins.IdInstrumento 			= i.IdInstrumento;
						ins.Nemotecnico				= i.Nemotecnico;
						ins.ISIN					= i.ISIN;
						ins.CorteMinimo				= i.CorteMinimo;
						ins.EstaInscrito			= i.EstaInscrito;
						ins.EsDivisible				= i.EsDivisible;
						ins.RazonSocial				= i.RazonSocial;
						ins.FechaEmision			= i.FechaEmision;
						ins.FechaVencimiento		= i.FechaVencimiento;
						ins.IdInstrumentoRelacionado= i.IdInstrumentoRelacionado;
						ins.IdEmisor				= i.IdEmisor;
						ins.DocumentoIdentidad		= i.DocumentoIdentidad;
						ins.IdTipoReajuste			= i.IdTipoReajuste;
						ins.Reajustable				= i.Reajustable;
						ins.IdMoneda				= i.IdMoneda;
						ins.IdMonedaEmision			= i.IdMonedaEmision;
						ins.IdTipoInstrumento		= i.IdTipoInstrumento;
						ins.IdEstado				= i.IdEstado;
						ins.DecimalesPrecio			= i.DecimalesPrecio;
						ins.NombreTipoInstrumento	= i.NombreTipoInstrumento;
						ins.IdClasificacionTipoInstrumento		= i.IdClasificacionTipoInstrumento; 
						ins.NombreClasificacionTipoInstrumento	= i.NombreClasificacionTipoInstrumento;
						ins.NombreCompletoEmisor	= i.NombreCompletoEmisor;
						ins.Tamano					= i.Tamano;
						ins.TipoValorizacion		= i.TipoValorizacion;
						ins.IndicadorBolson			= i.IndicadorBolson;
						ins.Tasa					= i.Tasa;
						ins.Precio					= i.Precio;
						ins.ValorizaPor				= i.ValorizaPor;
						ins.TieneCupon				= i.TieneCupon;
						ins.Cantidad				= i.Cantidad;
						ins.PorcentajeCastigo		= i.PorcentajeCastigo;
						ins.FrecuenciaCupon			= i.FrecuenciaCupon;
						ins.PrecioStrike			= i.PrecioStrike;
						ins.InstrumentoAcreditado	= i.InstrumentoAcreditado;
						ins.CallConfirmado			= i.CallConfirmado;
						ins.TasaEmision				= i.TasaEmision;
						ins.MercadoLocal			= i.MercadoLocal;
						ins.ValorClasificacionRiesgo= i.ValorClasificacionRiesgo;
						ins.EsPutEsCall				= i.EsPutEsCall;
						ins.IdTipoInstrumentoOrigen	 = i.IdTipoInstrumentoOrigen;
						
						ins.IdPortafolio 			= fl.IdPortafolio;
						ins.IdBoveda				= fl.IdBoveda;
						ins.FechaSaldo				= fl.FechaSaldo;
						ins.Dias					= fl.Dias;
						ins.IdMonedaInstrumento 			= fl.IdMonedaInstrumento;
						ins.PrecioMercado					= fl.PrecioMercado;
						ins.MontoAMercadoMonedaSistema		= fl.MontoAMercadoMonedaSistema;
						ins.MontoAMercadoMonedaInstrumento 	= fl.MontoAMercadoMonedaInstrumento;
						
						
						salida.P_OUT_INSTRUMENTO.add(ins);
					}
				}
			}
			else {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"CLIENTE_SIN_CUSTODIA",null,con,in.p_IdEmpresa));
				Id++;
			}
		}
		else {//el caso de compra
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("FiltroIdClasificacionTipoInstrumento", 0);
			params.put("FiltroNemotecnico", 0);
			params.put("FiltroIdTipoInstrumento", 0);
			if (ClasificacionTipoInstrumento.size() == 0) {
				//ClasificacionTipoInstrumento.add(null); 
				params.put("FiltroIdClasificacionTipoInstrumento", 1);
			}
			if (Nemotecnicos.size() == 0) {
				//Nemotecnicos.add(null); 
				params.put("FiltroNemotecnico", 1);
			}
			if (IdTipoInstrumento.size() == 0) {
				//IdTipoInstrumento.add(null); 
				params.put("FiltroIdTipoInstrumento", 1);
			}
			params.put("IdClasificacionTipoInstrumento", ClasificacionTipoInstrumento);
			params.put("Nemotecnico", Nemotecnicos);
			params.put("IdTipoInstrumento", IdTipoInstrumento);
			//params.put("IdInstrumento", IdInstrumento);
			params.put("IdEstado", "ACT");
			logger.debug("Inicio SQL_SELECT_CONSULTAR_INSTRUMENTO_UI");
			List<Instrumento> instrumentos = sql.query(con, SQL_SELECT_CONSULTAR_INSTRUMENTO_UI, params, Instrumento.class);
			
			salida.P_OUT_INSTRUMENTO.addAll(instrumentos);
		}
		
		
		
		
		
		
		
		/*
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdClasificacionTipoInstrumento", ClasificacionTipoInstrumento);
		params.put("Nemotecnico", Nemotecnicos);
		params.put("IdEstado", "ACT");
		logger.debug("Inicio SQL_SELECT_CONSULTAR_INSTRUMENTO_UI");
		List<Instrumento> instrumentos = sql.query(con, SQL_SELECT_CONSULTAR_INSTRUMENTO_UI, params, Instrumento.class);
		
		
		
		if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) ==  0) {
//			HashMap<Long, Object> fl = Utils.List2Hash(flujoSalida.p_out_FlujoTotalizado, "IdInstrumento");
			HashMap<Long, Object> fl = Utils.List2Hash(instrumentos, "IdInstrumento");
			for (FlujoCustodia ins: flujoSalida.p_out_FlujoTotalizado) {
//			for (Instrumento ins: instrumentos) {
//				FlujoCustodia f = (FlujoCustodia) fl.get(ins.IdInstrumento);
				Instrumento f = (Instrumento) fl.get(ins.IdInstrumento);
				if (f != null) {
					/ *ins.IdPortafolio 	= f.IdPortafolio;
					ins.IdBoveda		= f.IdBoveda;
					ins.FechaSaldo		= f.FechaSaldo;
					ins.Dias			= f.Dias;
					ins.IdMonedaInstrumento 			= f.IdMonedaInstrumento;
					ins.PrecioMercado					= f.PrecioMercado;
					ins.MontoAMercadoMonedaSistema		= f.MontoAMercadoMonedaSistema;
					ins.MontoAMercadoMonedaInstrumento 	= f.MontoAMercadoMonedaInstrumento;
					* /
					f.IdPortafolio 	= ins.IdPortafolio;
					f.IdBoveda		= ins.IdBoveda;
					f.FechaSaldo		= ins.FechaSaldo;
					f.Dias			= ins.Dias;
					f.IdMonedaInstrumento 			= ins.IdMonedaInstrumento;
					f.PrecioMercado					= ins.PrecioMercado;
					f.MontoAMercadoMonedaSistema		= ins.MontoAMercadoMonedaSistema;
					f.MontoAMercadoMonedaInstrumento 	= ins.MontoAMercadoMonedaInstrumento;
					salida.P_OUT_INSTRUMENTO.add(f);
				}
				
			}
		}
		if ("C".equalsIgnoreCase(in.p_TipoOperacion)) {
			salida.P_OUT_INSTRUMENTO.addAll(instrumentos);
		}*/
		return salida;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = getGsonBuilder();
		ConsultarInstrumentoEntrada inn = gson.fromJson(in.toString(), ConsultarInstrumentoEntrada.class);
		ConsultarInstrumentoSalida out = logica(conn, inn);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try (Connection con = PlataformaUtils.getQAConnection()) {
			try {
				/*
 		public Long IdCliente;
		public Long IdPortafolio;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String Nemotecnico;
				 */
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();					
				String jsonDE = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_FechaConsulta\":\"2019-07-10\",\"p_UsoTipoOperacion\":\"FLJORD\",\"p_BusquedaEspecifica\":\"FLUJO_ORDEN\",\"p_TipoOperacion\": \"C\", \"p_DiasProyeccion\": 0"
						+ ",\"p_Datos\":[{\"IdClasificacionTipoInstrumento\":\"RV\", \"IdCliente\": 100059, \"IdPortafolio\": 100069,\"IdTipoInstrumento\": \"OSA\"},{\"IdClasificacionTipoInstrumento\":\"RV\", \"IdCliente\": 100059, \"IdPortafolio\": 100069,\"IdTipoInstrumento\": \"ACN\"}] }";
				ConsultarInstrumentoEntrada Objin = gson.fromJson(jsonDE, ConsultarInstrumentoEntrada.class);
							
				ConsultarInstrumento kk = new ConsultarInstrumento();
				ConsultarInstrumentoSalida Objout = kk.logica(con, Objin);
				System.out.println(Objout.toString());
				con.rollback();
				
	    	}catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
}
