package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class RetornoCartera implements JavaCall{
	static final Logger logger = LogManager.getLogger(RetornoCartera.class);
	private final String SQL_SelectCartera = loadResourceToString("cl/bcs/custodia/sql/RetornoCartera_SEL_Cartera.sql");
	
	public static class RetornoCarteraEntrada {
	    public Long						p_IdEmpresa;
	    public Long						p_TicketAtencion;
	    public List<ObjDatosEntrada>	p_DatosEntrada;
		public String p_ValorizacionAutomatica;			
	    	    
	    public String toString() {
			return Utils.printJson(this, false);
		};
		public RetornoCarteraEntrada(){
	    	p_DatosEntrada = new ArrayList<ObjDatosEntrada>();
	    };
	};
	
	public static class RetornoCarteraSalida {		
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public List<ObjDatosSalida>		P_OUT_DATOSSALIDA;
		
		public static class ObjDatosSalida {
			public Long		IdGenerarMovimientoCustodia;
			public String	TablaOrigen;			
			public Long		IdCustodia;
			public Long		FolioCustodia;
			public String	RegistroConProblemas;
			public String	Mensaje;
		};
		public String toString() {
			return Utils.printJson(this, false);
		};
		public RetornoCarteraSalida (){
			P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			P_OUT_DATOSSALIDA	= new ArrayList<ObjDatosSalida>();
		};
	};
	
	public static class ObjDatosEntrada{
		public Long				IdGenerarMovimientoCustodia;
		public java.sql.Date	FechaOperacion;
		public String			TablaDestino;
		public String			IdConcepto;
		public Long				IdCustodiaDestino;			//REQUERIDO
		public Long				FolioCustodiaDestino;		//REQUERIDO
		
		public String			TablaOrigen;
		public String			IdTipoOperacionOrigen;		//REQUERIDO
		public Long				IdOperacionOrigen;			//OPCIONAL
		public Long				FolioCustodiaOrigen;		//REQUERIDO
		public Long				IdCustodiaOrigen;			//OPCIONAL
		
		public BigDecimal		PrecioARetornar;			//REQUERIDO
		public BigDecimal		TasaARetornar;				//REQUERIDO
		public BigDecimal		CantidadARetornar;
		public BigDecimal		ValorRescateARetornar;
		public java.sql.Date	FechaLiquidacionARetornar;
		public String 			EsFungible;
		
		public Long				IdUsuario;
		public String			IdModulo;
		
		//EME
		public String 			IdTipoReajuste;
		public String 			IdMonedaInstrumento;
		public String 			IdmonedaEmision;
		public Long 			IdMovimientoCarteraEnTransitoOrigen = 0L;
		
		//circular 514
		public BigDecimal 		PrecioReservaPacto;
		public BigDecimal		TasaReservaPacto;
		public String 			GeneraCarteraNuevaTasaInstrumentoPacto;
		
		public ObjDatosEntrada() {
			super();
			EsFungible = "N";
			GeneraCarteraNuevaTasaInstrumentoPacto = "N";
			this.PrecioARetornar= BigDecimal.ZERO;
			this.CantidadARetornar=BigDecimal.ZERO;
		}


		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ObjTablaCartera{
		public Long 		IdCartera;
		public Long 		IdCliente;
		public Long 		IdPortafolio;
		public Long 		FolioCartera;
		public String 		IdEstado;
		public Long			IdInstrumento;
		public BigDecimal 	INGCantidad;
		public BigDecimal 	INGValorRescate;
		public BigDecimal 	INGPrecioCompra;
		public BigDecimal 	INGTasaCompra;
		public BigDecimal	INGMontoATasaCompraActual;
		public BigDecimal	INGMontoCompraActualMonedaInstrumento;
		public BigDecimal	EGRCantidad;
		public BigDecimal 	EGRValorRescate;
		public String		NemoSVS;
		public Long 		IdCustodiaDestino;
		public String		IdClasificacionTipoInstrumento;
		public Date 		FechaVencimientoInstrumento;
		public String		IdRegimenFondoMutuo;
		public String		Procesada;
		public String 		IdEmisor;
		
		public ObjTablaCartera() {
			this.Procesada = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ObjCustodiaFolio{
		public Long 		IdCustodia;
		public Long 		FolioCustodia;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class TotalCartera { 
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		
		public TotalCartera() {
			this.Cantidad = BigDecimal.ZERO;
			this.ValorRescate = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	
	////////********************************************************************************************************************************************////////
	@Trace
	public RetornoCarteraSalida logica(Connection con, RetornoCarteraEntrada in) throws Exception {
		logger.info("INICIO::RetornoCarteraSalida");
		long inicio = System.currentTimeMillis();
		RetornoCarteraSalida out = new RetornoCarteraSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;		
		SQL sql = new SQL();
		
		
		try {
			//00 - INFORMACION DEL SISTEMA
			List<InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia> ConfiguracionCUS = InformacionConfiguracionCustodia.informacionConfiguracion(con, in.p_IdEmpresa);
			HashMap<String,Object> ConfigCUS = Utils.List2Hash(ConfiguracionCUS, "IdTipoOperacion");
			
			
			//01 - RECUPERAR INFORMACION DE REGISTROS EXISTENTES
			List<Long> FolioCarteras = new ArrayList<Long>();
			
			for (ObjDatosEntrada DE: in.p_DatosEntrada){
				FolioCarteras.add(DE.FolioCustodiaDestino);				
			};							
						
			HashMap<String,Object> paramsCartera = new HashMap<>();
			List<ObjTablaCartera> Carteras = new ArrayList <ObjTablaCartera>();
			
			FolioCarteras = FolioCarteras.stream().distinct().collect(Collectors.toList());
			
			if (FolioCarteras.size() > 0)
			{   //Consultamos carteras
				paramsCartera.clear(); 
				paramsCartera.put("IdEmpresa", in.p_IdEmpresa);
				paramsCartera.put("FolioCartera", FolioCarteras);			
				Carteras = sql.query(con, SQL_SelectCartera, paramsCartera, ObjTablaCartera.class);
			}
									
			//02 - PREPARAR DATA A INGRESAR O RETIRAR
			List<IngresoCustodia.ObjDatosEntrada> IngresosCustodia = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
			List<RetiroCustodia.ObjDatosEntrada> RetirosCustodia = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
			
			Map<Long, ObjTablaCartera> carterasAgrupadasPorFolio = Carteras.stream().collect(Collectors.toMap(custodia->custodia.FolioCartera, custodia->custodia));
			Map<Long, List<ObjDatosEntrada>> entradaAgrupadaPorFolioDestino = in.p_DatosEntrada.stream().collect(Collectors.groupingBy(entrada->entrada.FolioCustodiaDestino));
			
			Long idInternoIngresos = 1L;
			List<Long> ingresosALiquidar = new ArrayList<>();
			Map<Long, Long> indInternosPorIdGenerarMovimientoCustodia = new HashMap<Long, Long>();
			
			for(Entry<Long, List<ObjDatosEntrada>> folioDestino : entradaAgrupadaPorFolioDestino.entrySet()) {
				ObjTablaCartera car = carterasAgrupadasPorFolio.get(folioDestino.getKey());
				List<ObjDatosEntrada> entradas = folioDestino.getValue();
				TotalCartera totalCartera = new TotalCartera();
				Long ultimoIngreso = null;
				
				for(ObjDatosEntrada ent: entradas) {
					
					if(car != null) {
						ent.CantidadARetornar = ent.CantidadARetornar!=null ? ent.CantidadARetornar : BigDecimal.ZERO;
						ent.PrecioARetornar = ent.PrecioARetornar!=null ? ent.PrecioARetornar : BigDecimal.ZERO;
						
						InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia ConfiguracionMovimientoCustodia = (InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia) ConfigCUS.get(ent.IdTipoOperacionOrigen);	
					    
						// Verifica si se debe generar una nueva cartera
						boolean generaCarteraNuevaConfiguracionMovimientoCustodia = ("S").equals(ConfiguracionMovimientoCustodia.GeneraCarteraNueva); //se obtiene desde la configuracion de movimientos de custodia
						boolean generaCarteraNuevaTasaInstrumentoPacto = ("S").equals(ent.GeneraCarteraNuevaTasaInstrumentoPacto); //se obtiene desde la tabla de configuracion de mesa
					    
						boolean debeGenerarCarteraNueva = generaCarteraNuevaTasaInstrumentoPacto || generaCarteraNuevaConfiguracionMovimientoCustodia;
					    
						if (debeGenerarCarteraNueva){
							//Crea y configura la entrada de un nuevo ingreso de custodia
							IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada(); 
							ObjEntrada.IdGenerarMovimientoCustodia	= ent.IdGenerarMovimientoCustodia;
							ObjEntrada.AccionOperacion 				= "INGRESO";
							ObjEntrada.TablaAfectada 				= ent.TablaDestino;
							ObjEntrada.EsRegistroNuevo				= "N";
							ObjEntrada.GenerarNuevoFolioCustodia	= "S";
							ObjEntrada.IdCustodia					= ent.IdCustodiaDestino;
							ObjEntrada.IdCliente 					= car.IdCliente;
							ObjEntrada.IdPortafolio 				= car.IdPortafolio;
							ObjEntrada.FolioCarteraOrigen			= car.FolioCartera;
							ObjEntrada.FolioGarantiaOrigen			= ent.FolioCustodiaOrigen;
							ObjEntrada.IdInstrumento				= car.IdInstrumento;
							ObjEntrada.Cantidad						= ent.CantidadARetornar;
							ObjEntrada.ValorRescate					= ent.ValorRescateARetornar;
							ObjEntrada.NemoSVS						= car.NemoSVS;
							//DECIDIMOS PRECIOCOMPRA Y TASACOMPRA
							ObjEntrada.PrecioCompra					= (ConfiguracionMovimientoCustodia.RetornaCarteraAlPrecioTasa.equals("COMP")) ? car.INGPrecioCompra : ent.PrecioARetornar;
							ObjEntrada.TasaCompra					= (ConfiguracionMovimientoCustodia.RetornaCarteraAlPrecioTasa.equals("COMP")) ? car.INGTasaCompra : ent.TasaARetornar;
							//Circular 514
							if(generaCarteraNuevaTasaInstrumentoPacto) {
								ObjEntrada.PrecioCompra = ent.PrecioReservaPacto;
								ObjEntrada.TasaCompra	= ent.TasaReservaPacto;
								ObjEntrada.UsaTasaRecompraPacto = "S";
							}
							ObjEntrada.FechaVencimientoInstrumento  = car.FechaVencimientoInstrumento;
							ObjEntrada.IdRegimenFondoMutuo			= car.IdRegimenFondoMutuo;
							ObjEntrada.IdTipoOperacion				=  (ConfiguracionMovimientoCustodia.IdTipoOperacionRetornoCartera != null && !ConfiguracionMovimientoCustodia.IdTipoOperacionRetornoCartera.equals("")) ? ConfiguracionMovimientoCustodia.IdTipoOperacionRetornoCartera : ent.IdTipoOperacionOrigen;
							ObjEntrada.FechaOperacion				= ent.FechaOperacion;
							ObjEntrada.FechaLiquidacion				= ent.FechaLiquidacionARetornar;
							ObjEntrada.IdConcepto					= ent.IdConcepto;
							ObjEntrada.EsFungible        			= ent.EsFungible;
							
							
							IngresosCustodia.add(ObjEntrada);
						}
						
						if (!debeGenerarCarteraNueva){
							if("N".equals(car.Procesada) && "VIG".equals(car.IdEstado)) {
								// Configura la entrada para un retiro de custodia
								RetiroCustodia.ObjDatosEntrada ObjEntradaRetiro = new RetiroCustodia.ObjDatosEntrada();
								ObjEntradaRetiro.IdGenerarMovimientoCustodia	= ent.IdGenerarMovimientoCustodia;
								ObjEntradaRetiro.AccionOperacion 				= "RETIRO";
								ObjEntradaRetiro.IdCustodia						= car.IdCartera;						
								ObjEntradaRetiro.FolioCustodia					= car.FolioCartera;
								ObjEntradaRetiro.TablaAfectada 					= ent.TablaDestino;
								ObjEntradaRetiro.IdInstrumento					= car.IdInstrumento;
								ObjEntradaRetiro.IdCliente 						= car.IdCliente;
								ObjEntradaRetiro.IdPortafolio 					= car.IdPortafolio;
								ObjEntradaRetiro.Cantidad						= car.INGCantidad;
								ObjEntradaRetiro.ValorRescate					= car.INGValorRescate; 
								ObjEntradaRetiro.PrecioCompra					= ent.PrecioARetornar; 
								ObjEntradaRetiro.TasaCompra						= ent.TasaARetornar;
								ObjEntradaRetiro.FolioComprobante				= ent.FolioCustodiaOrigen;
								ObjEntradaRetiro.FechaOperacion					= ent.FechaOperacion;
								ObjEntradaRetiro.EsFungible						= ent.EsFungible;
								ObjEntradaRetiro.IdTipoOperacion				= ent.IdTipoOperacionOrigen;
								ObjEntradaRetiro.IdOperacion					= ent.IdOperacionOrigen;
								ObjEntradaRetiro.IdConcepto						= ent.IdConcepto;
								ObjEntradaRetiro.IdModulo						= ent.IdModulo;
								ObjEntradaRetiro.IdUsuario						= ent.IdUsuario; 
								ObjEntradaRetiro.FechaLiquidacion				= ent.FechaLiquidacionARetornar;
								ObjEntradaRetiro.EsRetiroPorFusion				= "S";
								ObjEntradaRetiro.IdEmisor						= car.IdEmisor;
								
								
								//EME
								ObjEntradaRetiro.IdTipoReajusteInstrumento      = ent.IdTipoReajuste;
								ObjEntradaRetiro.IdMonedaEmision				= ent.IdmonedaEmision;
								ObjEntradaRetiro.IdMonedaInstrumento            = ent.IdMonedaInstrumento;
								ObjEntradaRetiro.FechaVencimientoInstrumento    = car.FechaVencimientoInstrumento;
								ObjEntradaRetiro.MontoARecibirAnticipo			= car.INGCantidad.multiply(ent.PrecioARetornar, MathContext.DECIMAL128);
								if("RETGTA".equals(ObjEntradaRetiro.IdTipoOperacion)){
									ObjEntradaRetiro.IdMovimientoCarteraEnTransitoOrigen = ent.IdMovimientoCarteraEnTransitoOrigen;
								}
								RetirosCustodia.add(ObjEntradaRetiro);
								
								car.Procesada = "S";
								
								
								totalCartera.Cantidad = "IF".equals(car.IdClasificacionTipoInstrumento) && "S".equals(ent.EsFungible) ? BigDecimal.ONE : totalCartera.Cantidad.add(car.INGCantidad, MathContext.DECIMAL128);
								totalCartera.ValorRescate = "IF".equals(car.IdClasificacionTipoInstrumento) ? totalCartera.ValorRescate.add(car.INGValorRescate, MathContext.DECIMAL128) : BigDecimal.ZERO;
							}
							
							
							totalCartera.Cantidad = "IF".equals(car.IdClasificacionTipoInstrumento) && "S".equals(ent.EsFungible)  ? BigDecimal.ONE : totalCartera.Cantidad.add(ent.CantidadARetornar, MathContext.DECIMAL128);
							totalCartera.ValorRescate = "IF".equals(car.IdClasificacionTipoInstrumento) ? totalCartera.ValorRescate.add(ent.ValorRescateARetornar, MathContext.DECIMAL128) : BigDecimal.ZERO;
							
							if(ultimoIngreso != null) {
								ingresosALiquidar.add(ultimoIngreso);
							}
							
							//Ingreso
							IngresoCustodia.ObjDatosEntrada	ObjEntradaIngreso = new IngresoCustodia.ObjDatosEntrada(); 
								ObjEntradaIngreso.IdGenerarMovimientoCustodia	= idInternoIngresos;
								ObjEntradaIngreso.AccionOperacion 				= "INGRESO";
								ObjEntradaIngreso.TablaAfectada 				= ent.TablaDestino;
								ObjEntradaIngreso.EsRegistroNuevo				= "N";
								ObjEntradaIngreso.GenerarNuevoFolioCustodia		= "N";
								ObjEntradaIngreso.IdCustodia					= ent.IdCustodiaDestino;
								ObjEntradaIngreso.IdCliente 					= car.IdCliente;
								ObjEntradaIngreso.IdPortafolio 					= car.IdPortafolio;
								ObjEntradaIngreso.FechaOperacion				= ent.FechaOperacion;
								ObjEntradaIngreso.NemoSVS						= car.NemoSVS;
								ObjEntradaIngreso.IdCarteraOrigen				= ent.TablaOrigen.equals("CARTERA") ? ent.IdCustodiaOrigen : null;							
								ObjEntradaIngreso.IdGarantiaOrigen				= ent.TablaOrigen.equals("GARANTIA") ? ent.IdCustodiaOrigen : null;
								ObjEntradaIngreso.FolioGarantiaOrigen			= ent.TablaOrigen.equals("GARANTIA") ? ent.FolioCustodiaOrigen : null;	
								ObjEntradaIngreso.FolioCarteraOrigen			= car.FolioCartera;
								ObjEntradaIngreso.IdInstrumento					= car.IdInstrumento;
								ObjEntradaIngreso.Cantidad						= totalCartera.Cantidad;
								ObjEntradaIngreso.ValorRescate					= totalCartera.ValorRescate;							
								ObjEntradaIngreso.PrecioCompra					= ConfiguracionMovimientoCustodia.RetornaCarteraAlPrecioTasa.equals("COMP") ? car.INGPrecioCompra : ent.PrecioARetornar;
								ObjEntradaIngreso.TasaCompra					= ConfiguracionMovimientoCustodia.RetornaCarteraAlPrecioTasa.equals("COMP") ? car.INGTasaCompra : ent.TasaARetornar;		
								ObjEntradaIngreso.FechaVencimientoInstrumento   = car.FechaVencimientoInstrumento;
								ObjEntradaIngreso.IdRegimenFondoMutuo			= car.IdRegimenFondoMutuo;
								ObjEntradaIngreso.IdTipoOperacion				= ent.IdTipoOperacionOrigen;
								ObjEntradaIngreso.FechaLiquidacion				= ent.FechaLiquidacionARetornar;
								ObjEntradaIngreso.MontoARecibirAnticipo			= ObjEntradaIngreso.Cantidad.multiply(ent.PrecioARetornar, MathContext.DECIMAL128);
								ObjEntradaIngreso.IdConcepto					= ent.IdConcepto;
								ObjEntradaIngreso.IdEmisor				        = car.IdEmisor;
								ObjEntradaIngreso.IdOperacion        			= ent.IdOperacionOrigen;
								ObjEntradaIngreso.EsFungible        			= ent.EsFungible;
								ObjEntradaIngreso.IdMovimientoCarteraEnTransitoOrigen = ent.IdMovimientoCarteraEnTransitoOrigen;
								
								IngresosCustodia.add(ObjEntradaIngreso);
								indInternosPorIdGenerarMovimientoCustodia.put(idInternoIngresos, ent.IdGenerarMovimientoCustodia);
								ultimoIngreso = idInternoIngresos;
								idInternoIngresos++;
							
							
							
						}
					}
				}
			}
			
			//REALIZAR RETIROS
			if ( RetirosCustodia.size() > 0 ){
				RetiroCustodia retiroCustodia = new RetiroCustodia();
				RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSP.p_IdEmpresa			= in.p_IdEmpresa;	
				paramInSP.p_SimularOperacion	= "N";
				paramInSP.p_ValorizacionAutomatica = in.p_ValorizacionAutomatica;
				paramInSP.p_TicketAtencion		= in.p_TicketAtencion;
				paramInSP.p_DatosEntrada		= RetirosCustodia;				
				RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(con, paramInSP);
				if (!resultRetiros.P_OUT_ERRORESCONTADOR.equals(0L)){						
					out.P_OUT_MENSAJES = resultRetiros.P_OUT_MENSAJES;					
					out.P_OUT_ERRORESCONTADOR = 1L;
					return out;											
				};
				for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida result: resultRetiros.P_OUT_DATOSSALIDA){
					RetornoCarteraSalida.ObjDatosSalida i = new RetornoCarteraSalida.ObjDatosSalida(); 
					Utils.copyPojo(result, i);					
					out.P_OUT_DATOSSALIDA.add(i);
				};
			};
			
			
			//REALIZAR INGRESOS
			if ( IngresosCustodia.size() > 0  && out.P_OUT_ERRORESCONTADOR.equals(0L) ){
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInSP.p_IdEmpresa			= in.p_IdEmpresa;		
				paramInSP.p_IdModulo			= "CUS";
				paramInSP.p_SimularOperacion	= "N";
				paramInSP.p_ValorizacionAutomatica = in.p_ValorizacionAutomatica;
				paramInSP.p_TicketAtencion		= in.p_TicketAtencion;
				paramInSP.p_DatosEntrada		= IngresosCustodia;				
				IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(con, paramInSP);
				if (!resultIngresos.P_OUT_ERRORESCONTADOR.equals(0L)){
					out.P_OUT_MENSAJES = resultIngresos.P_OUT_MENSAJES;
					out.P_OUT_ERRORESCONTADOR = 1L;
					return out;											
				};
				
				//RETIROS DE CARTERAS CON SALDO INTERMEDIO.
				RetirosCustodia.clear();
				Map<Long, IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida> resultadosAgrupadosPorId = resultIngresos.P_OUT_DATOSSALIDA.stream().collect(Collectors.toMap(custodia->custodia.IdGenerarMovimientoCustodia, custodia->custodia));
				Map<Long, IngresoCustodia.ObjDatosEntrada> ingresosAgrupadosPorId = IngresosCustodia.stream().collect(Collectors.toMap(custodia->custodia.IdGenerarMovimientoCustodia, custodia->custodia));
				
				for(Long id : ingresosALiquidar) {
					IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida resultado = resultadosAgrupadosPorId.get(id);
					IngresoCustodia.ObjDatosEntrada ingreso = ingresosAgrupadosPorId.get(id);
					
					if(resultado != null && ingreso != null) {
						RetiroCustodia.ObjDatosEntrada ObjEntradaRetiro = new RetiroCustodia.ObjDatosEntrada();
						ObjEntradaRetiro.IdGenerarMovimientoCustodia	= ingreso.IdGenerarMovimientoCustodia;
						ObjEntradaRetiro.AccionOperacion 				= "RETIRO";
						ObjEntradaRetiro.IdCustodia						= resultado.IdCustodia;						
						ObjEntradaRetiro.FolioCustodia					= resultado.FolioCustodia;
						ObjEntradaRetiro.TablaAfectada 					= resultado.TablaOrigen;
						ObjEntradaRetiro.IdInstrumento					= ingreso.IdInstrumento;
						ObjEntradaRetiro.IdCliente 						= ingreso.IdCliente;
						ObjEntradaRetiro.IdPortafolio 					= ingreso.IdPortafolio;
						ObjEntradaRetiro.Cantidad						= ingreso.Cantidad;
						ObjEntradaRetiro.ValorRescate					= ingreso.ValorRescate; 
						ObjEntradaRetiro.PrecioCompra					= ingreso.PrecioCompra; 
						ObjEntradaRetiro.TasaCompra						= ingreso.TasaCompra;
						ObjEntradaRetiro.FolioComprobante				= ingreso.FolioComprobante;
						ObjEntradaRetiro.FechaOperacion					= ingreso.FechaOperacion;
						ObjEntradaRetiro.EsFungible						= ingreso.EsFungible;
						ObjEntradaRetiro.IdTipoOperacion				= ingreso.IdTipoOperacion;
						ObjEntradaRetiro.IdOperacion					= ingreso.IdOperacion;
						ObjEntradaRetiro.IdConcepto						= ingreso.IdConcepto;
						ObjEntradaRetiro.IdModulo						= "CUS";
						ObjEntradaRetiro.IdUsuario						= ingreso.IdUsuario; 
						ObjEntradaRetiro.FechaLiquidacion				= ingreso.FechaLiquidacion;
						ObjEntradaRetiro.EsRetiroPorFusion				= "S";
						ObjEntradaRetiro.IdEmisor						= ingreso.IdEmisor;
						ObjEntradaRetiro.Nemotecnico					= ingreso.Nemotecnico;
					    ObjEntradaRetiro.NemoSVS						= ingreso.NemoSVS;
						
						//EME
						ObjEntradaRetiro.IdTipoReajusteInstrumento      = ingreso.IdTipoReajusteInstrumento;
						ObjEntradaRetiro.IdMonedaEmision				= ingreso.IdMonedaEmision;
						ObjEntradaRetiro.IdMonedaInstrumento            = ingreso.IdMonedaInstrumento;
						ObjEntradaRetiro.FechaVencimientoInstrumento    = ingreso.FechaVencimientoInstrumento;
						ObjEntradaRetiro.MontoARecibirAnticipo			= ingreso.Cantidad.multiply(ingreso.PrecioCompra, MathContext.DECIMAL128);
						
						RetirosCustodia.add(ObjEntradaRetiro);
					}
				}
				
				for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida result: resultIngresos.P_OUT_DATOSSALIDA){
					Long id = indInternosPorIdGenerarMovimientoCustodia.get(result.IdGenerarMovimientoCustodia);
					if(id != null) {
						result.IdGenerarMovimientoCustodia = id;
					}
					RetornoCarteraSalida.ObjDatosSalida j = new RetornoCarteraSalida.ObjDatosSalida(); 
					Utils.copyPojo(result, j);					
					out.P_OUT_DATOSSALIDA.add(j);
				};
				
				//REALIZAR RETIROS SALDOS INTERMEDIO
				if ( RetirosCustodia.size() > 0 ){
					RetiroCustodia retiroCustodia = new RetiroCustodia();
					RetiroCustodia.RetiroCustodiaEntrada paramRescate = new RetiroCustodia.RetiroCustodiaEntrada();
					paramRescate.p_IdEmpresa			= in.p_IdEmpresa;	
					paramRescate.p_SimularOperacion	= "N";
					paramRescate.p_ValorizacionAutomatica = in.p_ValorizacionAutomatica;
					paramRescate.p_TicketAtencion		= in.p_TicketAtencion;
					paramRescate.p_DatosEntrada		= RetirosCustodia;				
					RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(con, paramRescate);
					if (!resultRetiros.P_OUT_ERRORESCONTADOR.equals(0L)){						
						out.P_OUT_MENSAJES = resultRetiros.P_OUT_MENSAJES;					
						out.P_OUT_ERRORESCONTADOR = 1L;
						return out;											
					};
					for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida result: resultRetiros.P_OUT_DATOSSALIDA){
						Long id = indInternosPorIdGenerarMovimientoCustodia.get(result.IdGenerarMovimientoCustodia);
						if(id != null) {
							result.IdGenerarMovimientoCustodia = id;
						}
						RetornoCarteraSalida.ObjDatosSalida i = new RetornoCarteraSalida.ObjDatosSalida(); 
						Utils.copyPojo(result, i);					
						out.P_OUT_DATOSSALIDA.add(i);
					};
				};
			};
			Carteras = sql.query(con, SQL_SelectCartera, paramsCartera, ObjTablaCartera.class);
			System.out.println(Carteras);
		}	
		
		catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("ERROR::RetornoCarteraSalida",e);
			throw e;
		}
		long fin = System.currentTimeMillis();
		logger.info("FIN::RetornoCarteraSalida", fin - inicio);
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		RetornoCarteraEntrada jin=gson.fromJson(in.toString() ,RetornoCarteraEntrada.class);
		RetornoCarteraSalida out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_TicketAtencion\":19403,\"p_DatosEntrada\":[{\"IdGenerarMovimientoCustodia\":1988,\"FechaOperacion\":\"2018-10-12 00:00:00.000\",\"TablaDestino\":\"CARTERA\",\"IdConcepto\":\"ANPCME\",\"IdCustodiaDestino\":1526091,\"TablaOrigen\":\"SUBYACENTE\",\"IdTipoOperacionOrigen\":\"COMPAC\",\"IdOperacionOrigen\":null,\"FolioCustodiaOrigen\":255398,\"IdCustodiaOrigen\":1988,\"PrecioARetornar\":null,\"TasaARetornar\":null,\"CantidadARetornar\":1500.00000000,\"ValorRescateARetornar\":0E-8,\"FechaLiquidacionARetornar\":\"2018-10-12 00:00:00.000\",\"EsFungible\":\"N\",\"IdUsuario\":null,\"IdModulo\":null,\"IdTipoReajuste\":\"UF\",\"IdMonedaInstrumento\":\"CLP\",\"IdmonedaEmision\":\"UF\"}],\"p_ValorizacionAutomatica\":\"S\"}";				
				RetornoCarteraEntrada Objin = gson.fromJson(json, RetornoCarteraEntrada.class);
							
				RetornoCartera  kk = new RetornoCartera();
				kk.logica(con, Objin);				
				//con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}

}
