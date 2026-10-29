package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerNemoSVS;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.Instrumento_Entrada;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.Instrumento_Salida;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Entrada;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.plataforma.val.CalcularIntereses;
import cl.bcs.plataforma.val.Valorizar;
import cl.bcs.plataforma.val.Valorizar.OutValorizar;
import cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoPQ;
import cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoWS;
import cl.bcs.plataforma.val.ValorizarSubyacentePacto;
import cl.bcs.plataforma.val.ValorizarSubyacentePacto.EntradaValorizarSubyacentePacto;
import cl.bcs.plataforma.val.ValorizarSubyacentePacto.SalidaValorizarSubyacentePacto;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ValorizarInstrumento implements JavaCall{	
	
	static final Logger logger = LogManager.getLogger(ValorizarInstrumento.class.getName());
	
	private final String SQL_PortafolioCuentaPropia = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_SEL_PortafolioCuentaPropia.sql");
	private final String SQL_TipoValorizacionPorTipoInstrumento = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_SEL_TipoValorizacionPorTipoInstrumento.sql");
	private final String SQL_ObtenerPIBMasMenosSistema = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_SEL_ObtenerPIBMasMenosSistema.sql");
	private final String SQL_Instrumento = loadResourceToString("cl/bcs/custodia/sql/ValorizarInstrumento_SEL_Instrumento.sql");
	
	private final BigDecimal TASA_MAXIMA_VALORIZADOR_RF_DEFAULT = new BigDecimal("998");
	

	
	public static class ObjEntradaInstrumentos {
		public Long					IdValorizacion;
		public String				OrigenProcesamiento;
		public String				FechaValorizacion;
		public String				FechaOperacion;						//Solo para operaciones a plazo
		public String				FechaVencimientoOperacion;			//Solo para operaciones a plazo
		public Long					PlazoEnDiasOperacion;				//Solo para operaciones a plazo		
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento;
		public String				Nemotecnico = "";
		public String				FechaVencimiento;
		public String				FechaEmision;
		public String				IdMonedaInstrumento;
		public String				IdMonedaEmisionInstrumento;
		public String				IdTipoReajusteInstrumento;
		public String				Emisor;								//valorizacionCL
		public String				IdCondicionDeLiquidacion;			//valorizacionCL
		public String				FechaDeLiquidacion;
		
		public BigDecimal			Cantidad; 
		public BigDecimal			ValorRescate; 
		public BigDecimal			PrecioCompra; 
		public BigDecimal			TasaCompra;
		public BigDecimal			MontoCompra;						//en moneda del instrumento		
		public BigDecimal			MontoCompraMonedaSistema;			//en moneda del instrumento
		public BigDecimal			PrecioSubyacente; 
		public BigDecimal			TasaSubyacente; 
		public BigDecimal			PrecioGarantia; 
		public BigDecimal			TasaGarantia; 
		public BigDecimal			TasaTIF;
		public Long					IdPortafolio;
		
		public String				TablaOrigen;		
		public String				IdUso; 
		public String				TipoCartera; 
		public String				IdTipoCriterioTasa;
		
		public String				IdTipoOperacion;
		public String 				IdTipoReajusteCabecera;     // se utiliza para valorizaciones de subyacentes de pactos
		public BigDecimal 			MontoFinalSubyacente; 
		
		public ObjEntradaInstrumentos () {
			this.IdTipoReajusteCabecera="";
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjResultadoValorizacion {
		public Long					IdProcesamiento;
		public Long					IdValorizacion;
		public String				TipoValorizacion; 
		public String				SubTipoValorizacion;
		public String				TipoValorizacionInstrumento;
		public String				IdUso; 
		public String				TipoCartera; 
		public String				IdTipoCriterioTasa;
		public java.sql.Date		FechaValorizacion;
		public String				DimensionTiempo;					// AN - AC - PR 
		public String				DimensionTasa;						// COMP - MERC - GTIA - SUBY - CORR - TIFF - P+CM - P+MC - P+GT - P+SY - P-CM - P-MC - P-GT - P-SY   		
		public java.sql.Date		FechaDimemsion;
		public String				ResultadoProceso;
		public String				MensajeProceso;
		public Long					TicketProcesamiento;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		public java.sql.Date		FechaOperacion;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		public String				IdClasificacionTipoInstrumento;		
		public Long					IdInstrumento; 
		public String				Nemotecnico;
		public String				Emisor;
		public BigDecimal			Cantidad;
		public BigDecimal			ValorRescate;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		
		public String				MensajeErrorFuente;
		public java.sql.Date		FechaPrecioTasa;					//Fecha de la tasa mercado obtenida 
		public String				FuentePrecioTasa;					//Fuente de la tasa mercado obtenida		
		public BigDecimal			Precio;
		public BigDecimal			Tasa;
		public BigDecimal			Tasa360;
		public BigDecimal			PrecioAPlazo;		
		public BigDecimal			TasaAPlazo;	 						//NO ESTAAAAAA
		public String				CalculaIntereses;
		public String				CalculaInteresesAPlazo;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		public String				IdMonedaSistema;
		public BigDecimal			MontoMonedaSistema;								
		public BigDecimal			MontoInteresMonedaSistema;				
		public BigDecimal			MontoInteresAcumuladoMonedaSistema;
		public BigDecimal			MontoReajusteMonedaSistema;				
		public BigDecimal			MontoReajusteAcumuladoMonedaSistema;
		public BigDecimal			MontoDevengadoAMonedaSistema;		
		public BigDecimal			MontoAPlazoMonedaSistema;
		public BigDecimal			MontoInteresAPlazoMonedaSistema;
		public BigDecimal			MontoInteresAcumuladoAPlazoMonedaSistema;
		//++++++++++++++++++++++++++
		public String				IdMonedaInstrumento;
		public BigDecimal			FactorMonedaInstrumento; 
		public java.sql.Date		FechaFactorMonedaInstrumento;
		public BigDecimal			MontoMonedaInstrumento;							//Total (mon + interes y reajuste)		
		public BigDecimal			MontoInteresMonedaInstrumento;					//Interes diario
		public BigDecimal			MontoInteresAcumuladoMonedaInstrumento;			//Interes desde el ingreso
		public BigDecimal			MontoReajusteMonedaInstrumento;
		public BigDecimal			MontoReajusteAcumuladoMonedaInstrumento;
		public BigDecimal			MontoDevengadoAMonedaInstrumento;				//diferencia entre mañana y hoy		
		public BigDecimal			MontoAPlazoMonedaInstrumento;					//desde CalculoInteres
		public BigDecimal			MontoInteresAPlazoMonedaInstrumento;		
		public BigDecimal			MontoInteresAcumuladoAPlazoMonedaInstrumento;
		//++++++++++++++++++++++++++
		public String				IdMonedaAdicional;
		public BigDecimal			FactorMonedaAdicional; 
		public java.sql.Date		FechaFactorMonedaAdicional;
		public BigDecimal			MontoMonedaAdicional;				
		public BigDecimal			MontoInteresMonedaAdicional;
		public BigDecimal			MontoInteresAcumuladoMonedaAdicional;
		public BigDecimal			MontoReajusteMonedaAdicional;
		public BigDecimal			MontoReajusteAcumuladoMonedaAdicional;
		public BigDecimal			MontoDevengadoAMonedaAdicional;
		public BigDecimal			MontoAPlazoMonedaAdicional;
		public BigDecimal			MontoInteresAPlazoMonedaAdicinal;
		public BigDecimal			MontoInteresAcumuladoAPlazoMonedaAdicional;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		public String				TieneCorteDeCupon;
		public java.sql.Date		FechaCorteCupon;
		public java.sql.Date		FechaPagoCorteCupon;
		public BigDecimal			MontoInteresesPagoCorteCupon;
		public Long					NumeroCupon; 
		public String				IdMonedaPagoCupon;		 
		//++++++++++++++++++++++++++
		public BigDecimal			MontoCorteCuponMonedaSistema;				
		public BigDecimal			MontoCapitalCorteCuponMonedaSistema;		
		public BigDecimal			MontoInteresCorteCuponMonedaSistema; 		
		public BigDecimal			MontoReajusteCorteCuponMonedaSistema;		
		//++++++++++++++++++++++++++
		public BigDecimal			MontoCorteCuponMonedaInstrumento;			//Monto amortizacion + interes
		public BigDecimal			MontoCapitalCorteCuponMonedaInstrumento;	//Monto total sin interes
		public BigDecimal			MontoInteresCorteCuponMonedaInstrumento;	//Interes periodo corte
		public BigDecimal			MontoReajusteCorteCuponMonedaInstrumento;	//Bono reajustado según la inflacion
		//++++++++++++++++++++++++++
		public BigDecimal			MontoCorteCuponMonedaAdicional; 
		public BigDecimal			MontoCapitalCorteCuponMonedaAdicional; 
		public BigDecimal			MontoInteresCorteCuponMonedaAdicional; 
		public BigDecimal			MontoReajusteCorteCuponMonedaAdicional;
		//++++++++++++++++++++++++++
		public BigDecimal			Duration;
		public BigDecimal			Convexidad;
		public BigDecimal			PlazoValorizador;
		public String				ValorTieneCall;
		public BigDecimal			TasaEmision;
		public String				ValorClasificacionRiesgo;

		public String				IdTipoReajuste;
		public BigDecimal			ValorTipoReajuste;		
		public String				NemoOriginal;
		
		
		public Long					PlazoEnDiasOperacion;
		public BigDecimal			MontoCompra;
		public BigDecimal			ValorPar;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	public static class ObjPortafolioCuentaPropia {
		public Long					IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjPIBMasMenosSistema {		
		public BigDecimal			PibMas; 
		public BigDecimal			PibMenos; 
		public String				PibTipoTasa; 

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjInstrumento {		
		public Long					IdInstrumento;
		public String				IdClasificacionTipoInstrumento;
		public String				IdTipoInstrumento;
		public String				Nemotecnico;
		public String 				ISIN;
		public String				IdEmisor;
		public String				IdTipoReajuste;
		public java.sql.Date		FechaEmisionInstrumento;
		public java.sql.Date		FechaVencimientoInstrumento;
		public String				IdMoneda;
		public BigDecimal			CorteMinimo;
		public Long					Tamano;
		public String				IndArt104;
		public BigDecimal			TasaFiscal;
		public BigDecimal			TasaEmision;
		public String				ValorizaPor;
		public String				TipoValorizacionInstrumento;
		public String				TieneCupon;
		public Integer				NumeroCupones;
		public String				CallConfirmado;
		public String				CuponeraVariable;
		public Integer				TipoCalculoInteresesCupon;
		public String				ValorClasificacionRiesgo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjTipoValorizacionPorTipoInstrumento {
		public Long					IdTipoValorizacionPorTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public String				UsaTasaCompra; 
		public String				UsaTasaMercado; 
		public String				UsaTasaCorredora; 
		public String				UsaTasaTIF; 
		public String				UsaPibMasMenos; 
		public String				ValorizaAnterior; 
		public String				ValorizaActual; 
		public String				ValorizaProximo; 
		public String				CalculaIntereses;
		public String				CalculaInteresesAPlazo;
		public String				TipoCalculoInteres;
		public Long					BaseCalculoDeInteres;
		public String				TipoValorizacion;
		public String				SubTipoValorizacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	};	
	
	public static class ObjProcesamiento {
		public Long					IdProcesamiento; 
		public Long					IdValorizacion;		//Dato de entrada 
		public String				DimTasa; 
		public String				DimTiempo; 
		public String				FechaCalculada; 
		public String				TipoPrecioTasa; 
		public String				ValorizaPor; 
		public String				IdClasificacionTipoInstrumento; 
		public String				ValorClasificacionRiesgo;
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento; 
		public String				Nemotecnico; 		
		public String				FechaEmision; 
		public BigDecimal			TasaEmision;
		public String				FechaVencimiento; 
		public String				IdMonedaInstrumento;
		public String				IdMonedaEmisionInstrumento;
		public String				IdTipoReajusteInstrumento;
		public String				Emisor;
		public String				IdCondicionDeLiquidacion;
		public Long					Tamano;
		public BigDecimal			Cantidad; 
		public BigDecimal			ValorRescate; 
		public BigDecimal			Precio;
		public java.sql.Date		FechaPrecioTasa; 
		public String				FuentePrecioTasa;
		public String				MensajeErrorFuente;
		public BigDecimal			Tasa;
		public BigDecimal			Monto;
		public BigDecimal			PrecioSubyacente; 
		public BigDecimal			TasaSubyacente; 
		public BigDecimal			PrecioGarantia; 
		public BigDecimal			TasaGarantia; 
		public BigDecimal			TasaTIF; 		
		public String				IdUso; 
		public String				TipoCartera; 
		public String				IdTipoCriterioTasa; 
		public String				EsCuentaPropia;
		public String				IndicadorUsoTasa; 
		public String				TipoValorizacion;
		public String				TipoValorizacionInstrumento; 
		public String				SubTipoValorizacion;
		public String				CalculaIntereses;
		public String				CalculaInteresesAPlazo; 
		public String				ValorizaCorteDeCupon = "N";
		public String 				EsBonoCero;
		public Integer				TipoCalculoInteresesCupon;
		public Long					PlazoResidual;
		public String				TablaOrigen;
		
		public String				NemoOriginal;		// Cuando hay cambio de nemo por corte cupon (& y *)
		public String				OrigenValorizacion;	// Para la valorizacion de instrumentos por la UI de la mesa
		public BigDecimal			MontoUI;			// Para la valorizacion de instrumentos por la UI de la mesa
		public BigDecimal			MontoMonedaSistemaOriginal;
		public String 				EsPacto;
		public String               IdReajusteCabecera;
		
		public ObjProcesamiento () {
			this.EsPacto= "N";
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	};
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static class ObjValorizarInstrumentosPQ {
		public Long					IdValorizacion; 
		public String				FechaValorizacion;
		public Long					IdInstrumento; 
		public String				Nemotecnico; 
		public String				IdMonedaInstrumento; 
		public BigDecimal			Cantidad; 
		public BigDecimal			Precio;
		public BigDecimal			Monto;
	
		public String toString() {
			return Utils.printJson(this, true);
		}
	};
	
	public static class ObjValorizarInstrumentosWS {
		public Long					IdValorizacion; 
		public String				FechaValorizacion;
		public Long					IdInstrumento; 
		public String				Nemotecnico; 
		public String				FechaEmision; 
		public String				FechaVencimiento; 
		public String				IdMonedaInstrumento; 
		public BigDecimal			Cantidad; 
		public BigDecimal			ValorRescate; 
		public BigDecimal			Precio; 
		public BigDecimal			Tasa; 
		public BigDecimal			Monto;
		public String				TipoValorizacion;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	};
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static class ValorizarInstrumentoEntrada{
		public Long								p_IdEmpresa;		
		public Long								p_IdUsuario;
		public String							p_IdModulo;
		public Long								p_IdPais = -1L;
		public String							p_IdMonedaAdicional = null;
		public List<ObjEntradaInstrumentos>		p_Instrumentos;
		public String							p_OrigenProcesamiento = "";
		
		public ValorizarInstrumentoEntrada(){
			p_Instrumentos	= new ArrayList<ObjEntradaInstrumentos>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ValorizarInstrumentoSalida{
		public List<ObjResultadoValorizacion>	P_OUT_INSTRUMENTOSVALORIZADOS;
		public List<MensajeSalida>				P_OUT_MENSAJES;		
		public List<ObjProcesamiento>           P_OUT_ERRORVALORIZACIONES;
		public Long								P_OUT_ERRORESCONTADOR;
		
		public ValorizarInstrumentoSalida(){
			P_OUT_MENSAJES 					= new ArrayList<MensajeSalida>();
			P_OUT_INSTRUMENTOSVALORIZADOS	= new ArrayList<ObjResultadoValorizacion>();
			P_OUT_ERRORVALORIZACIONES 		= new ArrayList<ObjProcesamiento>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	@Trace
	public ValorizarInstrumentoSalida logica(Connection con, ValorizarInstrumentoEntrada in) throws Exception {
		logger.info("Inicio ValorizarInstrumento");
		ValorizarInstrumentoSalida out = new ValorizarInstrumentoSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new SQL();

		try {
			String FechaSistema			= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");
			String MonedaSistema		= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaSistema");		
			String MonedaAdicional		= in.p_IdMonedaAdicional == null?ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaAdicional"):in.p_IdMonedaAdicional;
			String localizacionSistema 	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.LocalizacionSistema");
			BigDecimal tasaMaximaValorizadorRF = obtenerTasaMaximaValorizadorRF(con, in.p_IdEmpresa);
			String NemoSVS;
			
			HashMap<String,Object> paramsPortafolioCuentaPropia = new HashMap<>();
			paramsPortafolioCuentaPropia.put("IdEmpresa",in.p_IdEmpresa);
			List<ObjPortafolioCuentaPropia> PCP = sql.query(con, SQL_PortafolioCuentaPropia, paramsPortafolioCuentaPropia, ObjPortafolioCuentaPropia.class);	
			HashMap<Long,Object> PortafoliosCuentaPropia = Utils.List2Hash(PCP, "IdPortafolio");
						
			HashMap<String,Object> paramsTipoValorizacionPorTipoInstrumento = new HashMap<>();
			paramsTipoValorizacionPorTipoInstrumento.put("IdEmpresa",in.p_IdEmpresa);
			List<ObjTipoValorizacionPorTipoInstrumento> TVTI = sql.query(con, SQL_TipoValorizacionPorTipoInstrumento, paramsTipoValorizacionPorTipoInstrumento, ObjTipoValorizacionPorTipoInstrumento.class);	
			HashMap<String,Object> TipoValorizacionPorTipoInstrumento = Utils.List2Hash(TVTI, "IdTipoInstrumento");
			
			HashMap<String,Object> paramsObtenerPIBMasMenosSistema = new HashMap<>();
			paramsObtenerPIBMasMenosSistema.put("IdEmpresa",in.p_IdEmpresa);
			List<ObjPIBMasMenosSistema> PIBMasMenosSistema = sql.query(con, SQL_ObtenerPIBMasMenosSistema, paramsObtenerPIBMasMenosSistema, ObjPIBMasMenosSistema.class);
			
			HashMap<String,Object> paramsInstrumento = new HashMap<>();
			List<Long> IdInstrumento = in.p_Instrumentos.parallelStream().filter(t -> t.IdInstrumento != null).map(t -> t.IdInstrumento).distinct().collect(Collectors.toList());
			paramsInstrumento.put("IdInstrumento", IdInstrumento);
			List<ObjInstrumento> INS = sql.query(con, SQL_Instrumento, paramsInstrumento, ObjInstrumento.class);
			HashMap<Long,Object> InfoInstrumentos = Utils.List2Hash(INS, "IdInstrumento");
			
			
			
			
			//**************************************************************************************************************************************************
			//****************************** Obtener fechas de Valorizacion ************************************************************************************
			//**************************************************************************************************************************************************
			List<InObtenerDiaAnterior.FechasIn> FechasDA = new ArrayList<InObtenerDiaAnterior.FechasIn>();
			List<InObtenerDiaSiguiente.FechasIn> FechasDS = new ArrayList<InObtenerDiaSiguiente.FechasIn>();
			for (ObjEntradaInstrumentos Instrumento: in.p_Instrumentos) {
				
				if (in.p_OrigenProcesamiento != null && !in.p_OrigenProcesamiento.equals("")) {
					Instrumento.OrigenProcesamiento = in.p_OrigenProcesamiento;
				}
				
				if (Instrumento.OrigenProcesamiento == null || Instrumento.OrigenProcesamiento.equalsIgnoreCase("")) {
					Instrumento.OrigenProcesamiento = "MOVIMIENTOCUSTODIA";	
				}
				
				
				InObtenerDiaAnterior.FechasIn FDA = new InObtenerDiaAnterior.FechasIn();
				InObtenerDiaSiguiente.FechasIn FDS = new InObtenerDiaSiguiente.FechasIn();
				
				FDA.fechaInicial = Instrumento.FechaValorizacion;
				FDA.numeroDias	= 1L;
				FechasDA.add(FDA);
				
				FDS.fechaInicial = Instrumento.FechaValorizacion;
				FDS.numeroDias	= 1L;
				FechasDS.add(FDS);
			};
			logger.debug("Fin Loop ObtenerDia");
			
			InObtenerDiaAnterior ObtenerDiaAnterior = new InObtenerDiaAnterior();
			ObtenerDiaAnterior.idPais = in.p_IdPais;
			ObtenerDiaAnterior.diaHabil = "S";
			ObtenerDiaAnterior.fechas = FechasDA;
			
			InObtenerDiaSiguiente ObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			ObtenerDiaSiguiente.p_idPais = in.p_IdPais;
			ObtenerDiaSiguiente.p_diaHabil = "S";
			ObtenerDiaSiguiente.p_fechas = FechasDS;
			
			ObtenerDiaAnterior dant = new ObtenerDiaAnterior();
			ObtenerDiaAnterior.OutObtenerDiaAnterior dantout = dant.logica(con, ObtenerDiaAnterior);
			logger.debug("Fin ObtenerDiaAnterior");
			ObtenerDiaSiguiente dsig = new ObtenerDiaSiguiente();
			ObtenerDiaSiguiente.OutObtenerDiaSiguiente dsigout = dsig.logica(con, ObtenerDiaSiguiente);
			logger.debug("Fin ObtenerDiaSiguiente");

			
			
			//**************************************************************************************************************************************************
			//****************************** NEMO'S SVS PARA LOS IF'S  ************************************************************************************
			//**************************************************************************************************************************************************
			
			// * MSH
			// * Para rutinas de tasas publicadas se debe utilizar NemoSVS
			// * ---------------------------------------------------------
			List<Instrumento_Entrada> ConsultaNemoSVS = new ArrayList<Instrumento_Entrada>();			
			for (ObjEntradaInstrumentos Instrumento: in.p_Instrumentos) {
				if ( "IF".equals(Instrumento.IdClasificacionTipoInstrumento) && Instrumento.Emisor != null && Instrumento.FechaVencimiento != null ) {
					Instrumento_Entrada EntradaNemoSVS = new Instrumento_Entrada();				
					EntradaNemoSVS.IdInterno = Instrumento.IdValorizacion;
					EntradaNemoSVS.IdEmisor = Instrumento.Emisor;
					EntradaNemoSVS.FechaVencimiento = Instrumento.FechaVencimiento;
					EntradaNemoSVS.Plazo = (Long) ((Fecha.toDate(Instrumento.FechaVencimiento).getTime()-Fecha.toDate(Instrumento.FechaValorizacion).getTime())/86400000);; 
					EntradaNemoSVS.Nemotecnico = Instrumento.Nemotecnico;
					EntradaNemoSVS.IdTipoReajuste = Instrumento.IdTipoReajusteInstrumento;
					EntradaNemoSVS.PagaderoEn = Instrumento.IdMonedaInstrumento;
					ConsultaNemoSVS.add(EntradaNemoSVS);				
				}
			};
		
			HashMap<Long,Object> NemosSVS = new HashMap<Long,Object>();		
			if (ConsultaNemoSVS.size() > 0 ) {
				ObtenerNemoSVS_Entrada NemoEntrada = new ObtenerNemoSVS_Entrada();			
				ObtenerNemoSVS TraeNemoSVS_Service = new ObtenerNemoSVS();
				
				NemoEntrada.p_Instrumentos = ConsultaNemoSVS;
				ObtenerNemoSVS_Salida NemoSVS_Salida = TraeNemoSVS_Service.logica(con, NemoEntrada);
				NemosSVS = Utils.List2Hash(NemoSVS_Salida.p_out_Instrumento, "IdInterno");
				
			}
			
			
			
			//**************************************************************************************************************************************************
			//****************************** Obtencion de Secuencias *******************************************************************************************
			//**************************************************************************************************************************************************			
			logger.debug("Inicio Secuencias");
			
			//List<Long> SecProcesamientoValorizaCustodia = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodia", new Long( (35*in.p_Instrumentos.size()) ));
			List<Long> SecProcesamientoValorizaCustodia = new ArrayList<>();
			for (long i = 10000 ; i<(10000 + 36*in.p_Instrumentos.size()) ; i++) {
				SecProcesamientoValorizaCustodia.add(i);
			}
			logger.debug("Fin Secuencias");
			List<EntregarPreciosTasas_Entrada_DatosInstrumentos> ConsultarPreciosTasas = new ArrayList<EntregarPreciosTasas_Entrada_DatosInstrumentos>();			

			//**************************************************************************************************************************************************
			//*********************************** PROCESAR INSTRUMENTOS  ***************************************************************************************
			//**************************************************************************************************************************************************
			Integer i = 0;	
			List<ObjProcesamiento> ValorizacionDatosEntrada = new ArrayList<ObjProcesamiento>();
			
			logger.debug("Inicio MegaLoop ValorizarInstrumento");
			for (ObjEntradaInstrumentos Instrumento: in.p_Instrumentos ) {
				ObjTipoValorizacionPorTipoInstrumento TipoValorizacion = (ObjTipoValorizacionPorTipoInstrumento) TipoValorizacionPorTipoInstrumento.get(Instrumento.IdTipoInstrumento);
				ObjInstrumento InformacionAdicionalInstrumento = (ObjInstrumento) InfoInstrumentos.get(Instrumento.IdInstrumento);				
				if (TipoValorizacion == null || InformacionAdicionalInstrumento == null) continue;
				
				/*************************/
				if (Instrumento.Nemotecnico != null && !Instrumento.Nemotecnico.equals("") && !InformacionAdicionalInstrumento.Nemotecnico.equalsIgnoreCase(Instrumento.Nemotecnico)){
					InformacionAdicionalInstrumento.Nemotecnico = Instrumento.Nemotecnico;
				}					
				if (Instrumento.Nemotecnico != null && (Instrumento.Nemotecnico.equalsIgnoreCase("PRT") || Instrumento.Nemotecnico.equalsIgnoreCase("PRF"))){
					InformacionAdicionalInstrumento.Nemotecnico = "PAGARE R";
				}
				/***********************/
				ObjPortafolioCuentaPropia PortafolioCuentaPropia = (ObjPortafolioCuentaPropia) PortafoliosCuentaPropia.get(Instrumento.IdPortafolio);

				java.sql.Date FSistema 		= Fecha.toDate(FechaSistema);
				java.sql.Date FVencimiento	= Instrumento.FechaVencimientoOperacion != null ? Fecha.toDate(Instrumento.FechaVencimientoOperacion) : (Instrumento.FechaVencimiento != null? Fecha.toDate(Instrumento.FechaVencimiento) : Fecha.toDate(FechaSistema));
				Long PlazoResidualActual	= (Long) ((FVencimiento.getTime()-FSistema.getTime())/86400000);
				Long PlazoResidualAnterior	= 0L;
				Long PlazoResidualProximo	= 0L;
				String FechaAnterior		= "";
				String FechaSiguiente		= "";
				
				
				for ( OutObtenerDiaAnterior.FechasOut DiaAnt : dantout.fechas){
					if (Instrumento.FechaValorizacion.equalsIgnoreCase(DiaAnt.fechaInicial) && DiaAnt.numeroDiasInicial.equals(1L) ){	
						FechaAnterior = DiaAnt.fechaCalculada;
						PlazoResidualAnterior = (Long) ((Fecha.toDate(DiaAnt.fechaCalculada).getTime()-FSistema.getTime())/86400000);
						break;
					};
				};
				for ( OutObtenerDiaSiguiente.FechasOut DiaSig : dsigout.p_out_fechas ){
					if (Instrumento.FechaValorizacion.equalsIgnoreCase(DiaSig.fechaInicial) && DiaSig.numeroDiasInicial.equals(1L) ){
						FechaSiguiente = DiaSig.fechaCalculada;
						PlazoResidualProximo = (Long) ((Fecha.toDate(DiaSig.fechaCalculada).getTime()-FSistema.getTime())/86400000);
						break;
					};
				};
				
				if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
					Instrumento.IdMonedaEmisionInstrumento = StringUtils.isBlank(Instrumento.IdMonedaEmisionInstrumento) ? Instrumento.IdMonedaInstrumento : Instrumento.IdMonedaEmisionInstrumento;
				}

				// MSH
				// ---------------------------------------------------------
				Long PlazoResidualActualInstrumento	= 0L;
				Long PlazoResidualAnteriorInstrumento= 0L;
				Long PlazoResidualProximoInstrumento= 0L;
				Long PlazoResidualActualOperacion	= 0L;
				Long PlazoResidualAnteriorOperacion	= 0L;
				Long PlazoResidualProximoOperacion	= 0L;
				
				// Plazo Residual respecto del INSTRUMENTO
				if (Instrumento.FechaVencimiento != null) {
					PlazoResidualActualInstrumento = (Long) ((Fecha.toDate(Instrumento.FechaVencimiento).getTime()-Fecha.toDate(Instrumento.FechaValorizacion).getTime())/86400000);
					PlazoResidualAnteriorInstrumento = PlazoResidualActualInstrumento - PlazoResidualAnterior;
					PlazoResidualProximoInstrumento = PlazoResidualActualInstrumento - PlazoResidualProximo;
				}
				
				// Plazo Residual respecto de la OPERACION
				if (Instrumento.FechaVencimientoOperacion != null) {
					PlazoResidualActualOperacion = (Long) ((Fecha.toDate(Instrumento.FechaVencimientoOperacion).getTime()-Fecha.toDate(Instrumento.FechaValorizacion).getTime())/86400000);
					PlazoResidualAnteriorOperacion = PlazoResidualActualOperacion - PlazoResidualAnterior;
					PlazoResidualProximoOperacion = PlazoResidualActualOperacion - PlazoResidualProximo;
				}

				NemoSVS = null;
				if ( Instrumento.IdClasificacionTipoInstrumento.equals("IF") ) {
					// TASAS PUBLICADAS PREGUNTAN POR NEMOSVS
					Instrumento_Salida Nemo = (Instrumento_Salida) NemosSVS.get(Instrumento.IdValorizacion);
					NemoSVS = ( Nemo == null ? null : Nemo.NemoSVS );
				}
				
				
				// ---------------------------------------------------------
				
				
				//++++++++++++++++++++++++++++++++++
				String OmitevalorizacionPorOrigenProcesamiento = "";
				OmitevalorizacionPorOrigenProcesamiento = Instrumento.OrigenProcesamiento.equals("FLUJOCUSTODIA") || Instrumento.OrigenProcesamiento.equals("CUSTODIA_UI") || 
						Instrumento.OrigenProcesamiento.equals("ORDEN_UI") || Instrumento.OrigenProcesamiento.equals("MESA_UI") || Instrumento.OrigenProcesamiento.equals("FACTURACION") ? "S" : "N" ;
				
				
				//++++++++++++++++++++++++++++++++++
				String ValorizarPor = "";
				ValorizarPor = InformacionAdicionalInstrumento.ValorizaPor != null && !InformacionAdicionalInstrumento.ValorizaPor.equals("") ? InformacionAdicionalInstrumento.ValorizaPor : "P";
				//++++++++++++++++++++++++++++++++++
				
				String IdUso = ""; 
				String TipoCartera = "";
				String IdTipoCriterioTasa = "";
				String EsCuentaPropia = "";
				
				if (PortafolioCuentaPropia == null ) EsCuentaPropia = "N"; 
				else EsCuentaPropia = "S";
				
				if (Instrumento.IdTipoCriterioTasa == null) IdTipoCriterioTasa = "NORMATIVO";
				else IdTipoCriterioTasa = Instrumento.IdTipoCriterioTasa;
				
				if (Instrumento.IdUso == null){
					if (Instrumento.TablaOrigen == null || Instrumento.TablaOrigen.equals("") || Instrumento.TablaOrigen.equalsIgnoreCase("TRANSITO") || Instrumento.TablaOrigen.equalsIgnoreCase("NEGATIVA")){
						IdUso = "CARTERA";
					} else {
						IdUso = Instrumento.TablaOrigen;
					}
				} else 
					IdUso = Instrumento.IdUso;
				
				if (Instrumento.TipoCartera == null){
					if (PortafolioCuentaPropia != null){
						TipoCartera = "PROPIA";
					} else 
						TipoCartera = "TERCEROS";
				} else 
					TipoCartera = Instrumento.TipoCartera;
				
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//VALORIZAR A COMPRA ANTERIOR				
				if	(TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && TipoValorizacion.ValorizaAnterior.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "COMP"; 
					PROC_WS.DimTiempo				= "AN";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;										
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";	
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;					
					PROC_WS.ValorizaPor				= ValorizarPor;									
					PROC_WS.Tasa					= ValorizarPor.equals("T") ? Instrumento.TasaCompra : (Instrumento.PrecioCompra == null ? Instrumento.TasaCompra : null);
					PROC_WS.Precio					= ValorizarPor.equals("P") ? Instrumento.PrecioCompra : (Instrumento.TasaCompra == null ? Instrumento.PrecioCompra : null);
					
					if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
						PROC_WS.IdMonedaEmisionInstrumento = StringUtils.isBlank(Instrumento.IdMonedaEmisionInstrumento) ? Instrumento.IdMonedaInstrumento : Instrumento.IdMonedaEmisionInstrumento;
					}
				
										
					if (PROC_WS.SubTipoValorizacion.equals("PAGARE") && PROC_WS.Tasa == null && InformacionAdicionalInstrumento.TasaEmision != null ){
						PROC_WS.Tasa 				= PROC_WS.Tasa == null && PROC_WS.Precio != null ? ( (PROC_WS.Precio.multiply(InformacionAdicionalInstrumento.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : PROC_WS.Tasa;
					};					
					PROC_WS.Monto					= (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? Instrumento.MontoCompra : null;
									
					if (Fecha.toDate(Instrumento.FechaValorizacion).compareTo( Fecha.toDate(Instrumento.FechaOperacion)) == 0 ){
						PROC_WS.FechaCalculada 		= Instrumento.FechaValorizacion;
						PROC_WS.PlazoResidual		= 0L;
					} 
					else {
						PROC_WS.FechaCalculada 	= FechaAnterior;
						// MSH
						// PROC_WS.PlazoResidual	= PlazoResidualAnterior;
						PROC_WS.PlazoResidual	= Instrumento.FechaVencimiento != null ? PlazoResidualAnteriorInstrumento : PlazoResidualAnteriorOperacion;
					};
					
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//VALORIZAR A COMPRA ACTUAL
				i++;
				if	(TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") ){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "COMP"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.ValorizaCorteDeCupon	= InformacionAdicionalInstrumento.NumeroCupones > 0 ? "S" : "N";
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					
					if ("ANTEMP".equalsIgnoreCase(Instrumento.IdTipoOperacion) || "ANTPRT".equalsIgnoreCase(Instrumento.IdTipoOperacion) ||  "EMPRES".equalsIgnoreCase(Instrumento.IdTipoOperacion) || "PRESTA".equalsIgnoreCase(Instrumento.IdTipoOperacion)) {
						PROC_WS.FechaCalculada 		= Instrumento.FechaOperacion;
					}
					
					// MSH
					// PROC_WS.PlazoResidual			= PlazoResidualActual;
					PROC_WS.PlazoResidual			= Instrumento.FechaVencimiento != null ? PlazoResidualActualInstrumento : PlazoResidualActualOperacion;					
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;					
					PROC_WS.OrigenValorizacion		= in.p_OrigenProcesamiento;

					PROC_WS.Tasa					= ValorizarPor.equals("T") ? Instrumento.TasaCompra : (Instrumento.PrecioCompra == null ? Instrumento.TasaCompra : null);
					PROC_WS.Precio					= ValorizarPor.equals("P") ? Instrumento.PrecioCompra : (Instrumento.TasaCompra == null ? Instrumento.PrecioCompra : null);
					
					if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
						PROC_WS.IdMonedaEmisionInstrumento = StringUtils.isBlank(Instrumento.IdMonedaEmisionInstrumento) ? Instrumento.IdMonedaInstrumento : Instrumento.IdMonedaEmisionInstrumento;
					}
					
					// MSH
					// Para valorización en linea de IF/RF con 3 posibles combinaciones de incognitas, debe respetar lo que le llega desde la UI 
					if ( ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") || InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) 
						 || in.p_OrigenProcesamiento.equals("MESA_UI")) {
						
						PROC_WS.Cantidad			= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ? Instrumento.Cantidad : null;
						
						if("VALORIZACION_MASIVA".equalsIgnoreCase(in.p_OrigenProcesamiento) || "CONSULTA_CARTERAS".equalsIgnoreCase(in.p_OrigenProcesamiento) || "VALORIZACION_CARTERARFIF".equalsIgnoreCase(in.p_OrigenProcesamiento)) {
							PROC_WS.Monto				=  
									(PROC_WS.Tasa == null && PROC_WS.Precio == null) ? 
											Instrumento.MontoCompraMonedaSistema != null ?	Instrumento.MontoCompraMonedaSistema:Instrumento.MontoCompra : null;
						}
						//Valorizacion RF por pauta
						else if ("VALORIZACION_CARTERARFIF".equalsIgnoreCase(Instrumento.OrigenProcesamiento) && "RF".equalsIgnoreCase(InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento)){
							PROC_WS.Monto				=  
									(PROC_WS.Tasa == null && PROC_WS.Precio == null) ? 
											Instrumento.MontoCompraMonedaSistema != null ?	Instrumento.MontoCompraMonedaSistema:Instrumento.MontoCompra : null;
						}
						else {
							PROC_WS.Monto			=  Instrumento.MontoCompraMonedaSistema != null ?	Instrumento.MontoCompraMonedaSistema:Instrumento.MontoCompra;
						}				
					
						PROC_WS.MontoUI				= null;
						
						if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF")) {
							PROC_WS.Tasa			= Instrumento.TasaCompra;
							PROC_WS.Precio			= null;
						}
						
					} else {
						PROC_WS.Cantidad			= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
						
						// MSH - Cantidad null si es IF
						PROC_WS.Cantidad			= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? null : PROC_WS.Cantidad;
						PROC_WS.Monto				= (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? Instrumento.MontoCompra : null;
					
						if (PROC_WS.SubTipoValorizacion.equals("PAGARE") && PROC_WS.Tasa == null && InformacionAdicionalInstrumento.TasaEmision != null ){
							PROC_WS.Tasa 				= PROC_WS.Tasa == null && PROC_WS.Precio != null ? ( (PROC_WS.Precio.multiply(InformacionAdicionalInstrumento.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : PROC_WS.Tasa;
						};

						// MSH
						if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RV")) {
							// MSH : RV el plazo residual debe ser contra la f. de vencimiento de  la operacion (SIM, PRT, EMP)
							if ( (PROC_WS.FechaCalculada != null && !"0001-01-01".equals(PROC_WS.FechaCalculada)) && (Instrumento.FechaVencimientoOperacion != null && !"0001-01-01".equals(Instrumento.FechaVencimientoOperacion)) ) {
								PROC_WS.PlazoResidual = (Long) ((Fecha.toDate(Instrumento.FechaVencimientoOperacion).getTime() - Fecha.toDate(PROC_WS.FechaCalculada).getTime())/86400000);
								if(PROC_WS.PlazoResidual < 0) PROC_WS.PlazoResidual = 0L;
							}
						} else {
							// La fecha de liquidación es menor a la fecha de valorización ? => debe respetar el monto valorizado de la cartera
							if ( ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") || InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") )
								 && Instrumento.FechaVencimientoOperacion != null 
								 && Fecha.toDate(Instrumento.FechaValorizacion).compareTo(Fecha.toDate(Instrumento.FechaVencimientoOperacion)) < 1 ) {
								PROC_WS.MontoMonedaSistemaOriginal = Instrumento.MontoCompraMonedaSistema;
							}
						}
					}
					
					ValorizacionDatosEntrada.add(PROC_WS);					
				};
				
				//VALORIZAR A COMPRA PROXIMO
				i++;
				if (TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && TipoValorizacion.ValorizaProximo.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "COMP"; 
					PROC_WS.DimTiempo				= "PR";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;					
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.FechaCalculada 			= FechaSiguiente;
					// MSH
					// PROC_WS.PlazoResidual			= PlazoResidualProximo;
					PROC_WS.PlazoResidual			= Instrumento.FechaVencimiento != null ? PlazoResidualProximoInstrumento : PlazoResidualProximoOperacion;					
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.Tasa					= ValorizarPor.equals("T") ? Instrumento.TasaCompra : (Instrumento.PrecioCompra == null ? Instrumento.TasaCompra : null);
					PROC_WS.Precio					= ValorizarPor.equals("P") ? Instrumento.PrecioCompra : (Instrumento.TasaCompra == null ? Instrumento.PrecioCompra : null);
					
					
					if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
						PROC_WS.IdMonedaEmisionInstrumento = StringUtils.isBlank(Instrumento.IdMonedaEmisionInstrumento) ? Instrumento.IdMonedaInstrumento : Instrumento.IdMonedaEmisionInstrumento;
					}
				
					
					if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RV")) {
						// MSH : RV el plazo residual debe ser contra la f. de vencimiento de  la operacion (SIM, PRT, EMP)
						if ( (PROC_WS.FechaCalculada != null && !"0001-01-01".equals(PROC_WS.FechaCalculada)) && (Instrumento.FechaVencimientoOperacion != null && !"0001-01-01".equals(Instrumento.FechaVencimientoOperacion)) ) {
							PROC_WS.PlazoResidual = (Long) ((Fecha.toDate(Instrumento.FechaVencimientoOperacion).getTime() - Fecha.toDate(PROC_WS.FechaCalculada).getTime())/86400000);
							if(PROC_WS.PlazoResidual < 0) PROC_WS.PlazoResidual = 0L;
						}
					} else {
						// MSH - Cantidad null si es IF
						PROC_WS.Cantidad			= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? null : PROC_WS.Cantidad;
						if (PROC_WS.SubTipoValorizacion.equals("PAGARE") && PROC_WS.Tasa == null && InformacionAdicionalInstrumento.TasaEmision != null ){
							PROC_WS.Tasa 				= PROC_WS.Tasa == null && PROC_WS.Precio != null ? ( (PROC_WS.Precio.multiply(InformacionAdicionalInstrumento.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : PROC_WS.Tasa;
							}
					};
					PROC_WS.Monto					= (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? Instrumento.MontoCompra : null;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					
					// Si es 1er dia habil + Letra Hipotecaria => puede que cambie de Nemo.
					if ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") 
						 && InformacionAdicionalInstrumento.IdTipoInstrumento.equals("LTH") ) {
						 		
						
						Calendar cal = Calendar.getInstance();
				        cal.setTimeInMillis(Fecha.toDate(Instrumento.FechaValorizacion).getTime());
				        int MesAnt = cal.get(Calendar.MONTH);
						
				        cal.setTimeInMillis(Fecha.toDate(FechaSiguiente).getTime());
				        int MesSgt = cal.get(Calendar.MONTH);

						if ( MesAnt != MesSgt ) {
							PROC_WS.NemoOriginal = PROC_WS.Nemotecnico;
							PROC_WS.Nemotecnico =  DeterminaNuevoNemo.NuevoNemo(PROC_WS.Nemotecnico, FechaSiguiente);
						}
						
					}
					
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//VALORIZAR A COMPRA ORIGINAL
				i++;
				if	(TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "COMP"; 
					PROC_WS.DimTiempo				= "ACUM";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.Tasa					= ValorizarPor.equals("T") ? Instrumento.TasaCompra : (Instrumento.PrecioCompra == null ? Instrumento.TasaCompra : null);
					PROC_WS.Precio					= ValorizarPor.equals("P") ? Instrumento.PrecioCompra : (Instrumento.TasaCompra == null ? Instrumento.PrecioCompra : null);			
					PROC_WS.FechaCalculada 			= Instrumento.FechaOperacion;
					
					if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
						PROC_WS.IdMonedaEmisionInstrumento = StringUtils.isBlank(Instrumento.IdMonedaEmisionInstrumento) ? Instrumento.IdMonedaInstrumento : Instrumento.IdMonedaEmisionInstrumento;
					}
					
					//Para SIM (operaciones a plazo) se debe usar PlazoResidualActual ya que este se calcula en base a la fecha de vencimiento de la operacion
					PROC_WS.PlazoResidual			= InformacionAdicionalInstrumento.IdTipoInstrumento.equals("SIM") ? PlazoResidualActual : (Long) ((Fecha.toDate(Instrumento.FechaValorizacion).getTime()-Fecha.toDate(Instrumento.FechaOperacion).getTime())/86400000);
					
					
					// MSH - Cantidad null si es IF
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? null : PROC_WS.Cantidad;
					if (PROC_WS.Tasa == null && PROC_WS.SubTipoValorizacion.equals("PAGARE") && InformacionAdicionalInstrumento.TasaEmision != null ){
						PROC_WS.Tasa 				= PROC_WS.Tasa == null && PROC_WS.Precio != null ? ( (PROC_WS.Precio.multiply(InformacionAdicionalInstrumento.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : PROC_WS.Tasa;
					};
					PROC_WS.Monto					= (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? Instrumento.MontoCompra : null;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//VALORIZAR A MERCADO ANTERIOR
				i++;
				if (TipoValorizacion.UsaTasaMercado.equalsIgnoreCase("S") && TipoValorizacion.ValorizaAnterior.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "MERC"; 
					PROC_WS.DimTiempo				= "AN";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					// MSH - Cantidad null si es IF
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? null : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.Tasa					= new BigDecimal(0);
					PROC_WS.Precio					= new BigDecimal(0);
					PROC_WS.Monto					= null;
					
					if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
						PROC_WS.IdMonedaEmisionInstrumento = StringUtils.isBlank(Instrumento.IdMonedaEmisionInstrumento) ? Instrumento.IdMonedaInstrumento : Instrumento.IdMonedaEmisionInstrumento;
					}
					
					if (Fecha.toDate(Instrumento.FechaValorizacion).compareTo( Fecha.toDate(Instrumento.FechaOperacion)) == 0){
						PROC_WS.PlazoResidual	= PlazoResidualAnteriorInstrumento;//0L				 		
						PROC_WS.FechaCalculada 	= Instrumento.FechaValorizacion;
					} 
					else {
						// MSH
						// PROC_WS.PlazoResidual= PlazoResidualAnterior;
						PROC_WS.PlazoResidual	= Instrumento.FechaVencimiento != null ? PlazoResidualAnteriorInstrumento : PlazoResidualAnteriorOperacion;
						PROC_WS.FechaCalculada 	= FechaAnterior;						
					};
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					ValorizacionDatosEntrada.add(PROC_WS);
					
					//Para busqueda de precio y tasa a mercado
					EntregarPreciosTasas_Entrada_DatosInstrumentos PT_Mercado = creaPojoEntregarPrecioTasa(PROC_WS);
					PT_Mercado.IdRegistro 		= PROC_WS.IdProcesamiento;					
					// MSH La fecha de consulta tiene que ser la del dia anterior
					// PT_Mercado.FechaConsulta	= Fecha.toDate(PROC_WS.FechaCalculada);
					// PT_Mercado.IdMoneda			= PROC_WS.IdMonedaInstrumento;
					PT_Mercado.FechaConsulta	= Fecha.toDate(FechaAnterior);
					if (PROC_WS.IdTipoInstrumento.equalsIgnoreCase("PAC")) {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaInstrumento;
					}
					else {					  
						// JK
						PT_Mercado.IdMoneda			= StringUtils.isBlank(PROC_WS.IdMonedaEmisionInstrumento) ? PROC_WS.IdMonedaInstrumento:PROC_WS.IdMonedaEmisionInstrumento;
					}				
					PT_Mercado.PlazoResidual 	= PROC_WS.PlazoResidual;
					PT_Mercado.NemoSVS 			= NemoSVS;
					ConsultarPreciosTasas.add(PT_Mercado);
				};
				
				//VALORIZAR A MERCADO ACTUAL
				i++;				
				if (TipoValorizacion.UsaTasaMercado.equalsIgnoreCase("S") && TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") 
						
						){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "MERC"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					// MSH
					// PROC_WS.PlazoResidual		= PlazoResidualActual;
					PROC_WS.PlazoResidual			= Instrumento.FechaVencimiento != null ? PlazoResidualActualInstrumento : PlazoResidualActualOperacion;					
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= new BigDecimal(0);
					PROC_WS.Precio					= new BigDecimal(0);
					PROC_WS.Monto					= null;

					if(TipoValorizacion.IdTipoInstrumento.equalsIgnoreCase("DEP") || TipoValorizacion.IdTipoInstrumento.equalsIgnoreCase("AUT")){
						PROC_WS.Tasa				= ValorizarPor.equals("T") ? Instrumento.TasaCompra : (Instrumento.PrecioCompra == null ? Instrumento.TasaCompra : null);
						PROC_WS.Precio			= ValorizarPor.equals("P") ? Instrumento.PrecioCompra : (Instrumento.TasaCompra == null ? Instrumento.PrecioCompra : null);
						PROC_WS.Monto				= (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? Instrumento.MontoCompra : null;
					
						if (PROC_WS.SubTipoValorizacion.equals("PAGARE") && PROC_WS.Tasa == null && InformacionAdicionalInstrumento.TasaEmision != null ){
							PROC_WS.Tasa 				= PROC_WS.Tasa == null && PROC_WS.Precio != null ? ( (PROC_WS.Precio.multiply(InformacionAdicionalInstrumento.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : PROC_WS.Tasa;
						};
					}		
					
					if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
						PROC_WS.IdMonedaEmisionInstrumento = StringUtils.isBlank(Instrumento.IdMonedaEmisionInstrumento) ? Instrumento.IdMonedaInstrumento : Instrumento.IdMonedaEmisionInstrumento;
					}
					
					// MSH - Para intereses y reajustes
					PROC_WS.ValorizaCorteDeCupon	= InformacionAdicionalInstrumento.NumeroCupones > 0 ? "S" : "N";


					// MSH
					// Para valorización en linea de IF/RF con 3 posibles combinaciones de incognitas, debe respetar lo que le llega desde la UI 
					if ( in.p_OrigenProcesamiento.equals("MESA_UI") ) {
						// Si esta Valorizando por Tasa + Monto Valorizado => Busca Valor Rescate => "Arreglo" data para la valorizacion a Mercado.	
						if ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ) {
							// Deberia ser el corte minimo ?
							PROC_WS.OrigenValorizacion	= in.p_OrigenProcesamiento;
							PROC_WS.ValorRescate 		= PROC_WS.ValorRescate == null ? Instrumento.MontoCompra : PROC_WS.ValorRescate;
							// Cuando en la UI se busca la tasa (Regla3), no aplica en la tasa mercado enviar el monto valorizado.
							if ( PROC_WS.ValorRescate == null ) {
								PROC_WS.MontoUI			= Instrumento.MontoCompraMonedaSistema;
							}
						}
					}

					ValorizacionDatosEntrada.add(PROC_WS);
					
					
					//Para busqueda de precio y tasa a mercado
					EntregarPreciosTasas_Entrada_DatosInstrumentos PT_Mercado = creaPojoEntregarPrecioTasa(PROC_WS);
					PT_Mercado.IdRegistro 		= PROC_WS.IdProcesamiento;
					// MSH 
					// PT_Mercado.IdMoneda			= PROC_WS.IdMonedaInstrumento;
					if (PROC_WS.IdTipoInstrumento.equalsIgnoreCase("PAC")) {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaInstrumento;
					}
					else {						
						PT_Mercado.IdMoneda			= StringUtils.isBlank(PROC_WS.IdMonedaEmisionInstrumento) ? PROC_WS.IdMonedaInstrumento:PROC_WS.IdMonedaEmisionInstrumento;
					}

					PT_Mercado.FechaConsulta	= Fecha.toDate(PROC_WS.FechaCalculada);
					PT_Mercado.PlazoResidual 	= PROC_WS.PlazoResidual;
					PT_Mercado.NemoSVS 			= NemoSVS;
										
					ConsultarPreciosTasas.add(PT_Mercado);
					
				};
				
				//VALORIZAR A MERCADO PROXIMO
				i++;
				if (TipoValorizacion.UsaTasaMercado.equalsIgnoreCase("S") && TipoValorizacion.ValorizaProximo.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "MERC"; 
					PROC_WS.DimTiempo				= "PR";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					// MSH - Cantidad null si es IF
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? null : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.FechaCalculada 			= FechaSiguiente;
					// MSH
					// PROC_WS.PlazoResidual		= PlazoResidualProximo;
					PROC_WS.PlazoResidual			= Instrumento.FechaVencimiento != null ? PlazoResidualProximoInstrumento : PlazoResidualProximoOperacion;					
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= new BigDecimal(0);
					PROC_WS.Precio					= new BigDecimal(0);
					PROC_WS.Monto					= null;
					
					if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
						PROC_WS.IdMonedaEmisionInstrumento = StringUtils.isBlank(Instrumento.IdMonedaEmisionInstrumento) ? Instrumento.IdMonedaInstrumento : Instrumento.IdMonedaEmisionInstrumento;
					}
					
					// Si es 1er dia habil + Letra Hipotecaria => puede que cambie de Nemo.
					if ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") 
						 && InformacionAdicionalInstrumento.IdTipoInstrumento.equals("LTH") ) {
						 						
						int MesAnt = Fecha.toDate(Instrumento.FechaValorizacion).getMonth()+1;				
						int MesSgt = Fecha.toDate(FechaSiguiente).getMonth()+1;
						if ( MesAnt < MesSgt ) {
							PROC_WS.NemoOriginal = PROC_WS.Nemotecnico;
							PROC_WS.Nemotecnico =  DeterminaNuevoNemo.NuevoNemo(PROC_WS.Nemotecnico, FechaSiguiente);
						}
						
					}
					
					ValorizacionDatosEntrada.add(PROC_WS);
					
					
					//Para busqueda de precio y tasa a mercado
					EntregarPreciosTasas_Entrada_DatosInstrumentos PT_Mercado = creaPojoEntregarPrecioTasa(PROC_WS);
					PT_Mercado.IdRegistro 		= PROC_WS.IdProcesamiento;					
					
					// MSH La fecha de consulta tiene que ser la del dia anterior
					// PT_Mercado.FechaConsulta	= Fecha.toDate(PROC_WS.FechaCalculada);
					// PT_Mercado.IdMoneda			= PROC_WS.IdMonedaInstrumento;
					PT_Mercado.FechaConsulta	= Fecha.toDate(FechaSiguiente);
					if (PROC_WS.IdTipoInstrumento.equalsIgnoreCase("PAC")) {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaInstrumento;
					}
					else {
						PT_Mercado.IdMoneda			= StringUtils.isBlank(PROC_WS.IdMonedaEmisionInstrumento) ? PROC_WS.IdMonedaInstrumento:PROC_WS.IdMonedaEmisionInstrumento;
					}				
					PT_Mercado.PlazoResidual 	= PROC_WS.PlazoResidual;
					PT_Mercado.NemoSVS 			= NemoSVS;
					ConsultarPreciosTasas.add(PT_Mercado);
				};
				
				//VALORIZAR A MERCADO ORIGINAL
				i++;				
				if (TipoValorizacion.UsaTasaMercado.equalsIgnoreCase("S") && TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "MERC"; 
					PROC_WS.DimTiempo				= "ACUM";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					// MSH - Cantidad null si es IF
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? null : PROC_WS.Cantidad;					
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.FechaCalculada 			= Instrumento.FechaOperacion;
					PROC_WS.PlazoResidual			= (Long) ((Fecha.toDate(Instrumento.FechaValorizacion).getTime()-Fecha.toDate(Instrumento.FechaOperacion).getTime())/86400000);
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= new BigDecimal(0);
					PROC_WS.Precio					= new BigDecimal(0);
					PROC_WS.Monto					= null;
					ValorizacionDatosEntrada.add(PROC_WS);
					
					if (InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
						PROC_WS.IdMonedaEmisionInstrumento = StringUtils.isBlank(Instrumento.IdMonedaEmisionInstrumento) ? Instrumento.IdMonedaInstrumento : Instrumento.IdMonedaEmisionInstrumento;
					}
					
					//Para busqueda de precio y tasa a mercado
					EntregarPreciosTasas_Entrada_DatosInstrumentos PT_Mercado = creaPojoEntregarPrecioTasa(PROC_WS);
					PT_Mercado.IdRegistro 		= PROC_WS.IdProcesamiento;					
					if (PROC_WS.IdTipoInstrumento.equalsIgnoreCase("PAC")) {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaInstrumento;
					}
					else {
						PT_Mercado.IdMoneda			= StringUtils.isBlank(PROC_WS.IdMonedaEmisionInstrumento) ? PROC_WS.IdMonedaInstrumento:PROC_WS.IdMonedaEmisionInstrumento;
					}
					PT_Mercado.FechaConsulta	= Fecha.toDate(PROC_WS.FechaCalculada);
					PT_Mercado.PlazoResidual 	= PROC_WS.PlazoResidual;
					PT_Mercado.NemoSVS 			= NemoSVS;
					ConsultarPreciosTasas.add(PT_Mercado);
				};
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//VALORIZAR A SUBYACENTE ANTERIOR
				i++;
				if (Instrumento.PrecioSubyacente  != null && Instrumento.TasaSubyacente != null && TipoValorizacion.ValorizaAnterior.equalsIgnoreCase("S") && (OmitevalorizacionPorOrigenProcesamiento.equals("N") || Instrumento.OrigenProcesamiento.equalsIgnoreCase("VALORIZACION_CARTERAPACTOS"))
					// MSH - WS Valorizador HT (IF/RF), por el momento no soporta tasa cero
					&& ( ( ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") || InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) 
							 && ValorizarPor.equals("T") && Instrumento.TasaSubyacente.compareTo(BigDecimal.ZERO) == 1 ) 
					   ||  ( !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") && !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) ) ) {					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "SUBY"; 
					PROC_WS.DimTiempo				= "AN";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.Tasa					= ValorizarPor.equals("T") ? Instrumento.TasaSubyacente : (Instrumento.PrecioSubyacente == null ? Instrumento.TasaSubyacente : null);
					PROC_WS.Precio					= ValorizarPor.equals("P") ? Instrumento.PrecioSubyacente : (Instrumento.TasaSubyacente == null ? Instrumento.PrecioSubyacente : null);
					

					if (PROC_WS.SubTipoValorizacion.equals("PAGARE") && PROC_WS.Tasa == null && InformacionAdicionalInstrumento.TasaEmision != null ){
						PROC_WS.Tasa 				= PROC_WS.Tasa == null && PROC_WS.Precio != null ? ( (PROC_WS.Precio.multiply(InformacionAdicionalInstrumento.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : PROC_WS.Tasa;
					};
					PROC_WS.Monto					= (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? Instrumento.MontoCompra : null;
					
					if ( Fecha.toDate(Instrumento.FechaValorizacion).compareTo( Fecha.toDate(Instrumento.FechaOperacion)) == 0){
						PROC_WS.FechaCalculada 	= Instrumento.FechaValorizacion;
						PROC_WS.PlazoResidual	= 0L;
					} 
					else {
						PROC_WS.FechaCalculada	= FechaAnterior;
						PROC_WS.PlazoResidual	= PlazoResidualAnterior;	
					};
					
					if (Instrumento.IdTipoOperacion != null && (Instrumento.IdTipoOperacion.equals("COMPAC") || Instrumento.IdTipoOperacion.equals("VENPAC"))) {						
						PROC_WS.EsPacto="S";
						PROC_WS.IdReajusteCabecera =  Instrumento.IdTipoReajusteCabecera;
						PROC_WS.FechaEmision = Instrumento.FechaOperacion;
						PROC_WS.Monto= 	Instrumento.MontoCompra;
					};
					
					
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//VALORIZAR A SUBYACENTE ACTUAL 
				i++;
				if (Instrumento.PrecioSubyacente  != null && Instrumento.TasaSubyacente != null && TipoValorizacion.ValorizaActual.equalsIgnoreCase("S")
					// MSH - WS Valorizador HT (IF/RF), por el momento no soporta tasa cero
					&& ( ( ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") || InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) 
							 && ValorizarPor.equals("T") && Instrumento.TasaSubyacente.compareTo(BigDecimal.ZERO) == 1 ) 
					   ||  ( !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") && !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) ) ) {					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "SUBY"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.PlazoResidual			= PlazoResidualActual;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.Tasa					= ValorizarPor.equals("T") ? Instrumento.TasaSubyacente : (Instrumento.PrecioSubyacente == null ? Instrumento.TasaSubyacente : null);
					PROC_WS.Precio					= ValorizarPor.equals("P") ? Instrumento.PrecioSubyacente : (Instrumento.TasaSubyacente == null ? Instrumento.PrecioSubyacente : null);
					
					
					
					if (PROC_WS.SubTipoValorizacion.equals("PAGARE") && PROC_WS.Tasa == null && InformacionAdicionalInstrumento.TasaEmision != null ){
						PROC_WS.Tasa 				= PROC_WS.Tasa == null && PROC_WS.Precio != null ? ( (PROC_WS.Precio.multiply(InformacionAdicionalInstrumento.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : PROC_WS.Tasa;
					};
					
					PROC_WS.Monto					= (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? Instrumento.MontoCompra : null;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					
					if (Instrumento.IdTipoOperacion != null && (Instrumento.IdTipoOperacion.equals("COMPAC") || Instrumento.IdTipoOperacion.equals("VENPAC"))) {						
						PROC_WS.EsPacto="S";
						PROC_WS.IdReajusteCabecera =  Instrumento.IdTipoReajusteCabecera;
						PROC_WS.FechaEmision = Instrumento.FechaOperacion;
						PROC_WS.Monto= 	Instrumento.MontoCompra;
					};
					ValorizacionDatosEntrada.add(PROC_WS);				
				};
				
				//VALORIZAR A SUBYACENTE PROXIMO
				i++;
				if (Instrumento.PrecioSubyacente != null && Instrumento.TasaSubyacente != null && TipoValorizacion.ValorizaProximo.equalsIgnoreCase("S") && (OmitevalorizacionPorOrigenProcesamiento.equals("N") || Instrumento.OrigenProcesamiento.equalsIgnoreCase("VALORIZACION_CARTERAPACTOS"))
					// MSH - WS Valorizador HT (IF/RF), por el momento no soporta tasa cero
					&& ( ( ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") || InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) 
							 && ValorizarPor.equals("T") && Instrumento.TasaSubyacente.compareTo(BigDecimal.ZERO) == 1 ) 
					   ||  ( !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") && !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) ) ) {					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "SUBY"; 
					PROC_WS.DimTiempo				= "PR";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.FechaCalculada 			= FechaSiguiente;
					PROC_WS.PlazoResidual			= PlazoResidualProximo;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.Tasa					= ValorizarPor.equals("T") ? Instrumento.TasaSubyacente : (Instrumento.PrecioSubyacente == null ? Instrumento.TasaSubyacente : null);
					PROC_WS.Precio					= ValorizarPor.equals("P") ? Instrumento.PrecioSubyacente : (Instrumento.TasaSubyacente == null ? Instrumento.PrecioSubyacente : null);
					
					
					
					if (PROC_WS.SubTipoValorizacion.equals("PAGARE") && PROC_WS.Tasa == null && InformacionAdicionalInstrumento.TasaEmision != null ){
						PROC_WS.Tasa 				= PROC_WS.Tasa == null && PROC_WS.Precio != null ? ( (PROC_WS.Precio.multiply(InformacionAdicionalInstrumento.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : PROC_WS.Tasa;
					};
					PROC_WS.Monto					= (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? Instrumento.MontoCompra : null;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					
					if (Instrumento.IdTipoOperacion != null && (Instrumento.IdTipoOperacion.equals("COMPAC") || Instrumento.IdTipoOperacion.equals("VENPAC"))) {						
						PROC_WS.EsPacto="S";
						PROC_WS.IdReajusteCabecera =  Instrumento.IdTipoReajusteCabecera;
						PROC_WS.FechaEmision = Instrumento.FechaOperacion;
						PROC_WS.Monto= 	Instrumento.MontoCompra;
					};
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//VALORIZAR A SUBYACENTE ORIGINAL
				i++;
				if (Instrumento.PrecioSubyacente  != null && Instrumento.TasaSubyacente != null && TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") && (OmitevalorizacionPorOrigenProcesamiento.equals("N") || Instrumento.OrigenProcesamiento.equalsIgnoreCase("VALORIZACION_CARTERAPACTOS"))
					// MSH - WS Valorizador HT (IF/RF), por el momento no soporta tasa cero
					&& ( ( ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") || InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) 
							 && ValorizarPor.equals("T") && Instrumento.TasaSubyacente.compareTo(BigDecimal.ZERO) == 1 ) 
					   ||  ( !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") && !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) ) ) {					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "SUBY"; 
					PROC_WS.DimTiempo				= "ACUM";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.FechaCalculada 			= Instrumento.FechaOperacion;
					PROC_WS.PlazoResidual			= (Long) ((Fecha.toDate(Instrumento.FechaValorizacion).getTime()-Fecha.toDate(Instrumento.FechaOperacion).getTime())/86400000);
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.Tasa					= ValorizarPor.equals("T") ? Instrumento.TasaSubyacente : (Instrumento.PrecioSubyacente == null ? Instrumento.TasaSubyacente : null);
					PROC_WS.Precio					= ValorizarPor.equals("P") ? Instrumento.PrecioSubyacente : (Instrumento.TasaSubyacente == null ? Instrumento.PrecioSubyacente : null);					
					

				
					
					if (PROC_WS.SubTipoValorizacion.equals("PAGARE") && PROC_WS.Tasa == null && InformacionAdicionalInstrumento.TasaEmision != null ){
						PROC_WS.Tasa 				= PROC_WS.Tasa == null && PROC_WS.Precio != null ? ( (PROC_WS.Precio.multiply(InformacionAdicionalInstrumento.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : PROC_WS.Tasa;
					};
					PROC_WS.Monto					= (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? Instrumento.MontoCompra : null;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					ValorizacionDatosEntrada.add(PROC_WS);				
				};
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//VALORIZAR A GARANTIA ANTERIOR
				i++;
				if (Instrumento.PrecioGarantia  != null && Instrumento.TasaGarantia != null && TipoValorizacion.ValorizaAnterior.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")
					// MSH - WS Valorizador HT (IF/RF), por el momento no soporta tasa cero
					&& ( ( ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") || InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) 
							 && ValorizarPor.equals("T") && Instrumento.TasaGarantia.compareTo(BigDecimal.ZERO) == 1 ) 
					   ||  ( !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") && !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) ) ) {					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "GTIA"; 
					PROC_WS.DimTiempo				= "AN";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.Tasa					= ValorizarPor.equals("T") ? Instrumento.TasaGarantia : (Instrumento.PrecioGarantia == null ? Instrumento.TasaGarantia : null);
					PROC_WS.Precio					= ValorizarPor.equals("P") ? Instrumento.PrecioGarantia : (Instrumento.TasaGarantia == null ? Instrumento.PrecioGarantia : null);
					

					
					if (PROC_WS.SubTipoValorizacion.equals("PAGARE") && PROC_WS.Tasa == null && InformacionAdicionalInstrumento.TasaEmision != null ){
						PROC_WS.Tasa 				= PROC_WS.Tasa == null && PROC_WS.Precio != null ? ( (PROC_WS.Precio.multiply(InformacionAdicionalInstrumento.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : PROC_WS.Tasa;
					};
					PROC_WS.Monto					= (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? Instrumento.MontoCompra : null;
					
					if ( Fecha.toDate(Instrumento.FechaValorizacion).compareTo(Fecha.toDate(Instrumento.FechaOperacion)) == 0){
						PROC_WS.FechaCalculada 	= Instrumento.FechaValorizacion;
						PROC_WS.PlazoResidual	= 0L;
					} 
					else {
						PROC_WS.FechaCalculada 	= FechaAnterior;
						PROC_WS.PlazoResidual	= PlazoResidualAnterior;
					};
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//VALORIZAR A GARANTIA ACTUAL
				i++;
				if (Instrumento.PrecioGarantia  != null && Instrumento.TasaGarantia != null && TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") && !Instrumento.OrigenProcesamiento.equalsIgnoreCase("VALORIZACION_CARTERAPACTOS")
					// MSH - WS Valorizador HT (IF/RF), por el momento no soporta tasa cero
					&& ( ( ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") || InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) 
							 && ValorizarPor.equals("T") && Instrumento.TasaGarantia.compareTo(BigDecimal.ZERO) == 1 ) 
					   ||  ( !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") && !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) ) ) {					
						
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "GTIA"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.PlazoResidual			= PlazoResidualActual;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.Tasa					= ValorizarPor.equals("T") ? Instrumento.TasaGarantia : (Instrumento.PrecioGarantia == null ? Instrumento.TasaGarantia : null);
					PROC_WS.Precio					= ValorizarPor.equals("P") ? Instrumento.PrecioGarantia : (Instrumento.TasaGarantia == null ? Instrumento.PrecioGarantia : null);					
					

					
					if (PROC_WS.SubTipoValorizacion.equals("PAGARE") && PROC_WS.Tasa == null && InformacionAdicionalInstrumento.TasaEmision != null ){
						PROC_WS.Tasa 				= PROC_WS.Tasa == null && PROC_WS.Precio != null ? ( (PROC_WS.Precio.multiply(InformacionAdicionalInstrumento.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : PROC_WS.Tasa;
					};
					PROC_WS.Monto					= (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? Instrumento.MontoCompra : null;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					ValorizacionDatosEntrada.add(PROC_WS);				
				};
				
				//VALORIZAR A GARANTIA PROXIMO
				i++;
 				if (Instrumento.PrecioGarantia != null && Instrumento.TasaGarantia != null && TipoValorizacion.ValorizaProximo.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")
					// MSH - WS Valorizador HT (IF/RF), por el momento no soporta tasa cero
					&& ( ( ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") || InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) 
							 && ValorizarPor.equals("T") && Instrumento.TasaGarantia.compareTo(BigDecimal.ZERO) == 1 ) 
					   ||  ( !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") && !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) ) ) {					
 						
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "GTIA"; 
					PROC_WS.DimTiempo				= "PR";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.FechaCalculada 			= FechaSiguiente;
					PROC_WS.PlazoResidual			= PlazoResidualProximo;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.Tasa					= ValorizarPor.equals("T") ? Instrumento.TasaGarantia : (Instrumento.PrecioGarantia == null ? Instrumento.TasaGarantia : null);
					PROC_WS.Precio					= ValorizarPor.equals("P") ? Instrumento.PrecioGarantia : (Instrumento.TasaGarantia == null ? Instrumento.PrecioGarantia : null);					
					
					
					if (PROC_WS.SubTipoValorizacion.equals("PAGARE") && PROC_WS.Tasa == null && InformacionAdicionalInstrumento.TasaEmision != null ){
						PROC_WS.Tasa 				= PROC_WS.Tasa == null && PROC_WS.Precio != null ? ( (PROC_WS.Precio.multiply(InformacionAdicionalInstrumento.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : PROC_WS.Tasa;
					};
					PROC_WS.Monto					= (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? Instrumento.MontoCompra : null;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//VALORIZAR A GARANTIA ORIGINAL
				i++;
				if (Instrumento.PrecioGarantia  != null && Instrumento.TasaGarantia != null && TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")
					// MSH - WS Valorizador HT (IF/RF), por el momento no soporta tasa cero
					&& ( ( ( InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") || InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) 
							 && ValorizarPor.equals("T") && Instrumento.TasaGarantia.compareTo(BigDecimal.ZERO) == 1 ) 
					   ||  ( !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") && !InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") ) ) ) {					
						
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "GTIA"; 
					PROC_WS.DimTiempo				= "ACUM";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.FechaCalculada 			= Instrumento.FechaOperacion;
					PROC_WS.PlazoResidual			= (Long) ((Fecha.toDate(Instrumento.FechaValorizacion).getTime()-Fecha.toDate(Instrumento.FechaOperacion).getTime())/86400000);
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.Tasa					= ValorizarPor.equals("T") ? Instrumento.TasaGarantia : (Instrumento.PrecioGarantia == null ? Instrumento.TasaGarantia : null);
					PROC_WS.Precio					= ValorizarPor.equals("P") ? Instrumento.PrecioGarantia : (Instrumento.TasaGarantia == null ? Instrumento.PrecioGarantia : null);					
					

					
					if (PROC_WS.SubTipoValorizacion.equals("PAGARE") && PROC_WS.Tasa == null && InformacionAdicionalInstrumento.TasaEmision != null ){
						PROC_WS.Tasa 				= PROC_WS.Tasa == null && PROC_WS.Precio != null ? ( (PROC_WS.Precio.multiply(InformacionAdicionalInstrumento.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : PROC_WS.Tasa;
					};
					PROC_WS.Monto					= (PROC_WS.Tasa == null && PROC_WS.Precio == null) ? Instrumento.MontoCompra : null;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					ValorizacionDatosEntrada.add(PROC_WS);				
				};
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//VALORIZAR A CORREDORA ACTUAL
				i++;
				if (TipoValorizacion.UsaTasaCorredora.equalsIgnoreCase("S") && TipoValorizacion.ValorizaActual.equalsIgnoreCase("S")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					//TODO: obtener tasa de la corredora
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "CORR"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.PlazoResidual			= PlazoResidualActual;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= null; //Tasa no definida de donde se obtiene
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;
					

					ValorizacionDatosEntrada.add(PROC_WS);					
				};
				
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//VALORIZAR A TIF ACTUAL
				i++;
				if (TipoValorizacion.UsaTasaTIF.equalsIgnoreCase("S") && TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") && Instrumento.TasaTIF != null){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "TIF"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.PlazoResidual			= PlazoResidualActual;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= Instrumento.TasaTIF;					
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;
					

					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//VALORIZAR A PIB MAS COMPRA ANTERIOR
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && TipoValorizacion.ValorizaAnterior.equalsIgnoreCase("S") && 
						OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P+CM"; 
					PROC_WS.DimTiempo				= "AN";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.Tasa					= ValorizarPor.equals("T") && Instrumento.TasaCompra != null? Instrumento.TasaCompra.subtract(PIBMasMenosSistema.get(0).PibMenos) : null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;
					

										
					if (Fecha.toDate(Instrumento.FechaValorizacion).compareTo(Fecha.toDate(Instrumento.FechaOperacion)) == 0){
						PROC_WS.FechaCalculada 	= Instrumento.FechaValorizacion;
						PROC_WS.PlazoResidual	= 0L;
					} 
					else {
						PROC_WS.FechaCalculada 	= FechaAnterior;
						// MSH
						// PROC_WS.PlazoResidual	= PlazoResidualAnterior;
						PROC_WS.PlazoResidual			= Instrumento.FechaVencimiento != null ? PlazoResidualProximoInstrumento : PlazoResidualProximoOperacion;						
					};			
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//VALORIZAR A PIB MAS COMPRA ACTUAL
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") && 
						OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P+CM"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.PlazoResidual			= PlazoResidualActual;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= ValorizarPor.equals("T") && Instrumento.TasaCompra != null? Instrumento.TasaCompra.subtract(PIBMasMenosSistema.get(0).PibMenos) : null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;
					

				
					
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//VALORIZAR A PIB MAS MERCADO ANTERIOR
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaMercado.equalsIgnoreCase("S") && TipoValorizacion.ValorizaAnterior.equalsIgnoreCase("S") && 
						OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P+MC";
					PROC_WS.DimTiempo				= "AN";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;
					

					
					
					if (Fecha.toDate(Instrumento.FechaValorizacion).compareTo(Fecha.toDate(Instrumento.FechaOperacion)) == 0){
						PROC_WS.FechaCalculada 	= Instrumento.FechaValorizacion;
						PROC_WS.PlazoResidual	= 0L;
					} 
					else {
						PROC_WS.FechaCalculada 	= FechaAnterior;
						PROC_WS.PlazoResidual	= PlazoResidualAnterior;
					};					
					ValorizacionDatosEntrada.add(PROC_WS);
					
					
					//Para busqueda de precio y tasa a mercado
					EntregarPreciosTasas_Entrada_DatosInstrumentos PT_Mercado = creaPojoEntregarPrecioTasa(PROC_WS);
					PT_Mercado.IdRegistro 		= PROC_WS.IdProcesamiento;					
					if (PROC_WS.IdTipoInstrumento.equalsIgnoreCase("PAC")) {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaInstrumento;
					}
					else {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaEmisionInstrumento;	
					}
					PT_Mercado.FechaConsulta	= Fecha.toDate(PROC_WS.FechaCalculada);
					PT_Mercado.PlazoResidual 	= PROC_WS.PlazoResidual;
					PT_Mercado.NemoSVS 			= NemoSVS;
					ConsultarPreciosTasas.add(PT_Mercado);
				};
				
				//VALORIZAR A PIB MAS MERCADO ACTUAL
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaMercado.equalsIgnoreCase("S") && TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") && 
						OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P+MC"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.PlazoResidual			= PlazoResidualActual;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;
					ValorizacionDatosEntrada.add(PROC_WS);					

					
					//Para busqueda de precio y tasa a mercado
					EntregarPreciosTasas_Entrada_DatosInstrumentos PT_Mercado = creaPojoEntregarPrecioTasa(PROC_WS);
					PT_Mercado.IdRegistro 		= PROC_WS.IdProcesamiento;					
					if (PROC_WS.IdTipoInstrumento.equalsIgnoreCase("PAC")) {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaInstrumento;
					}
					else {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaEmisionInstrumento;	
					}
					PT_Mercado.FechaConsulta	= Fecha.toDate(PROC_WS.FechaCalculada);
					PT_Mercado.PlazoResidual 	= PROC_WS.PlazoResidual;
					PT_Mercado.NemoSVS 			= NemoSVS;					
					ConsultarPreciosTasas.add(PT_Mercado);
				};
				
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//VALORIZAR A PIB MAS SUBYACENTE ANTERIOR
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && Instrumento.TasaSubyacente != null && 
						TipoValorizacion.ValorizaAnterior.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P+SY"; 
					PROC_WS.DimTiempo				= "AN";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= ValorizarPor.equals("T") ? Instrumento.TasaSubyacente.subtract(PIBMasMenosSistema.get(0).PibMenos) : null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;
					
										
					if (Fecha.toDate(Instrumento.FechaValorizacion).compareTo(Fecha.toDate(Instrumento.FechaOperacion)) == 0){
						PROC_WS.FechaCalculada 	= Instrumento.FechaValorizacion;
						PROC_WS.PlazoResidual	= 0L;
					} 
					else {
						PROC_WS.FechaCalculada 	= FechaAnterior;
						PROC_WS.PlazoResidual	= PlazoResidualAnterior;
					};			
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//VALORIZAR A PIB MAS SUBYACENTE ACTUAL
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && Instrumento.TasaSubyacente != null && 
						TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P+SY"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.PlazoResidual			= PlazoResidualActual;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= ValorizarPor.equals("T") && Instrumento.TasaSubyacente != null? Instrumento.TasaSubyacente.subtract(PIBMasMenosSistema.get(0).PibMenos) : null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;
					

					
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//VALORIZAR A PIB MAS GARANTIA ANTERIOR
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && Instrumento.TasaGarantia != null && 
						TipoValorizacion.ValorizaAnterior.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P+GT"; 
					PROC_WS.DimTiempo				= "AN";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= ValorizarPor.equals("T") && Instrumento.TasaGarantia != null? Instrumento.TasaGarantia.subtract(PIBMasMenosSistema.get(0).PibMenos) : null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;					

										
					if (Fecha.toDate(Instrumento.FechaValorizacion).compareTo(Fecha.toDate(Instrumento.FechaOperacion)) == 0){
						PROC_WS.FechaCalculada 	= Instrumento.FechaValorizacion;
						PROC_WS.PlazoResidual	= 0L;
					} 
					else {
						PROC_WS.FechaCalculada 	= FechaAnterior;
						PROC_WS.PlazoResidual	= PlazoResidualAnterior;
					};			
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//VALORIZAR A PIB MAS GARANTIA ACTUAL
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && Instrumento.TasaGarantia != null && 
						TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P+GT"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.PlazoResidual			= PlazoResidualActual;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= ValorizarPor.equals("T") && Instrumento.TasaGarantia != null? Instrumento.TasaGarantia.subtract(PIBMasMenosSistema.get(0).PibMenos) : null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;

					
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++				
				//VALORIZAR A PIB MENOS COMPRA ANTERIOR
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && 
						TipoValorizacion.ValorizaAnterior.equalsIgnoreCase("S") && PIBMasMenosSistema.get(0).PibTipoTasa.equalsIgnoreCase("COMPRA") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P-CM"; 
					PROC_WS.DimTiempo				= "AN";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && PROC_WS.FechaCalculada!=null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= ValorizarPor.equals("T") && Instrumento.TasaCompra != null? Instrumento.TasaCompra.add(PIBMasMenosSistema.get(0).PibMas) : null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;

					
					
					if (Fecha.toDate(Instrumento.FechaValorizacion).compareTo(Fecha.toDate(Instrumento.FechaOperacion)) == 0){
						PROC_WS.FechaCalculada 	= Instrumento.FechaValorizacion;
						PROC_WS.PlazoResidual	= 0L;
					} 
					else {
						PROC_WS.FechaCalculada 	= FechaAnterior;
						PROC_WS.PlazoResidual	= PlazoResidualAnterior;
					};					
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//VALORIZAR A PIB MENOS COMPRA ACTUAL
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && 
						TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") && PIBMasMenosSistema.get(0).PibTipoTasa.equalsIgnoreCase("COMPRA") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P-CM"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.PlazoResidual			= PlazoResidualActual;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= ValorizarPor.equals("T") && Instrumento.TasaCompra != null? Instrumento.TasaCompra.add(PIBMasMenosSistema.get(0).PibMas) : null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;

					
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//VALORIZAR A PIB MENOS MERCADO ANTERIOR
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaMercado.equalsIgnoreCase("S") && 
						TipoValorizacion.ValorizaAnterior.equalsIgnoreCase("S") && PIBMasMenosSistema.get(0).PibTipoTasa.equalsIgnoreCase("MERCADO") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P-MC"; 
					PROC_WS.DimTiempo				= "AN";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;

					
					if (Fecha.toDate(Instrumento.FechaValorizacion).compareTo(Fecha.toDate(Instrumento.FechaOperacion)) == 0){
						PROC_WS.FechaCalculada 	= Instrumento.FechaValorizacion;
						PROC_WS.PlazoResidual	= 0L;
					} 
					else {
						PROC_WS.FechaCalculada 	= FechaAnterior;
						PROC_WS.PlazoResidual	= PlazoResidualAnterior;
					};				
					ValorizacionDatosEntrada.add(PROC_WS);
					
					
					//Para busqueda de precio y tasa a mercado
					EntregarPreciosTasas_Entrada_DatosInstrumentos PT_Mercado = creaPojoEntregarPrecioTasa(PROC_WS);
					PT_Mercado.IdRegistro 		= PROC_WS.IdProcesamiento;					
					if (PROC_WS.IdTipoInstrumento.equalsIgnoreCase("PAC")) {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaInstrumento;
					}
					else {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaEmisionInstrumento;	
					}
					PT_Mercado.FechaConsulta	= Fecha.toDate(PROC_WS.FechaCalculada);
					PT_Mercado.PlazoResidual 	= PROC_WS.PlazoResidual;
					PT_Mercado.NemoSVS 			= NemoSVS;
					ConsultarPreciosTasas.add(PT_Mercado);
				};
				
				//VALORIZAR A PIB MENOS MERCADO ACTUAL
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaMercado.equalsIgnoreCase("S") && 
						TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") && PIBMasMenosSistema.get(0).PibTipoTasa.equalsIgnoreCase("MERCADO") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P-MC"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.PlazoResidual			= PlazoResidualActual;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= null;					
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;	

					
					ValorizacionDatosEntrada.add(PROC_WS);
					
					
					//Para busqueda de precio y tasa a mercado
					EntregarPreciosTasas_Entrada_DatosInstrumentos PT_Mercado = creaPojoEntregarPrecioTasa(PROC_WS);
					PT_Mercado.IdRegistro 		= PROC_WS.IdProcesamiento;					
					if (PROC_WS.IdTipoInstrumento.equalsIgnoreCase("PAC")) {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaInstrumento;
					}
					else {
						PT_Mercado.IdMoneda			= PROC_WS.IdMonedaEmisionInstrumento;	
					}
					PT_Mercado.FechaConsulta	= Fecha.toDate(PROC_WS.FechaCalculada);
					PT_Mercado.PlazoResidual 	= PROC_WS.PlazoResidual;
					PT_Mercado.NemoSVS 			= NemoSVS;
					ConsultarPreciosTasas.add(PT_Mercado);
				};
				
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//VALORIZAR A PIB MENOS SUBYACENTE ANTERIOR
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && Instrumento.TasaSubyacente != null && 
						TipoValorizacion.ValorizaAnterior.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P+SY"; 
					PROC_WS.DimTiempo				= "AN";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= ValorizarPor.equals("T") && Instrumento.TasaSubyacente != null ? Instrumento.TasaSubyacente.add(PIBMasMenosSistema.get(0).PibMenos) : null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;
					

										
					if (Fecha.toDate(Instrumento.FechaValorizacion).compareTo(Fecha.toDate(Instrumento.FechaOperacion)) == 0){
						PROC_WS.FechaCalculada 	= Instrumento.FechaValorizacion;
						PROC_WS.PlazoResidual	= 0L;
					} 
					else {
						PROC_WS.FechaCalculada 	= FechaAnterior;
						PROC_WS.PlazoResidual	= PlazoResidualAnterior;
					};			
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//VALORIZAR A PIB MENOS SUBYACENTE ACTUAL
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && Instrumento.TasaSubyacente != null && 
						TipoValorizacion.ValorizaActual.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P+SY"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.PlazoResidual			= PlazoResidualActual;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= ValorizarPor.equals("T") && Instrumento.TasaSubyacente != null ? Instrumento.TasaSubyacente.add(PIBMasMenosSistema.get(0).PibMenos) : null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;

					
					
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//VALORIZAR A PIB MENOS GARANTIA ANTERIOR
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && Instrumento.TasaGarantia != null && 
						TipoValorizacion.ValorizaAnterior.equalsIgnoreCase("S") && OmitevalorizacionPorOrigenProcesamiento.equals("N")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P+GT"; 
					PROC_WS.DimTiempo				= "AN";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= ValorizarPor.equals("T") && Instrumento.TasaGarantia != null? Instrumento.TasaGarantia.add(PIBMasMenosSistema.get(0).PibMenos) : null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;

					
										
					if (Fecha.toDate(Instrumento.FechaValorizacion).compareTo(Fecha.toDate(Instrumento.FechaOperacion)) == 0){
						PROC_WS.FechaCalculada 	= Instrumento.FechaValorizacion;
						PROC_WS.PlazoResidual	= 0L;
					} 
					else {
						PROC_WS.FechaCalculada 	= FechaAnterior;
						PROC_WS.PlazoResidual	= PlazoResidualAnterior;
					};			
					ValorizacionDatosEntrada.add(PROC_WS);
				};
				
				//VALORIZAR A PIB MENOS GARANTIA ACTUAL
				i++;
				if (TipoValorizacion.UsaPibMasMenos.equalsIgnoreCase("S") && TipoValorizacion.UsaTasaCompra.equalsIgnoreCase("S") && Instrumento.TasaGarantia != null && 
						TipoValorizacion.ValorizaActual.equalsIgnoreCase("S")){					
					ObjProcesamiento PROC_WS = creaPojoValorizacion(Instrumento,TipoValorizacion,InformacionAdicionalInstrumento);
					
					PROC_WS.IdProcesamiento			= SecProcesamientoValorizaCustodia.get(i);
					PROC_WS.DimTasa					= "P+GT"; 
					PROC_WS.DimTiempo				= "AC";
					PROC_WS.IdUso					= IdUso;
					PROC_WS.TipoCartera				= TipoCartera;
					PROC_WS.IdTipoCriterioTasa		= IdTipoCriterioTasa;
					PROC_WS.EsCuentaPropia			= EsCuentaPropia;
					PROC_WS.EsBonoCero				= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("RF") && InformacionAdicionalInstrumento.NumeroCupones.equals(1) ? "S" : "N";
					PROC_WS.SubTipoValorizacion		= InformacionAdicionalInstrumento.IdClasificacionTipoInstrumento.equals("IF") ? "PAGARE" : PROC_WS.SubTipoValorizacion;
					PROC_WS.FechaCalculada			= Instrumento.FechaValorizacion;
					PROC_WS.Cantidad				= InformacionAdicionalInstrumento.Tamano > 0L && PROC_WS.Cantidad != null ? PROC_WS.Cantidad.multiply(new BigDecimal(InformacionAdicionalInstrumento.Tamano)) : PROC_WS.Cantidad;
					PROC_WS.ValorRescate			= PROC_WS.SubTipoValorizacion.equals("PAGARE") ?  PROC_WS.ValorRescate : null;
					PROC_WS.PlazoResidual			= PlazoResidualActual;
					PROC_WS.ValorizaPor				= ValorizarPor;
					PROC_WS.CalculaIntereses		= Instrumento.FechaDeLiquidacion != null && (Fecha.toDate(PROC_WS.FechaCalculada).compareTo( Fecha.toDate(Instrumento.FechaDeLiquidacion)) > 0 ) ? TipoValorizacion.CalculaIntereses : "N";
					PROC_WS.Tasa					= ValorizarPor.equals("T") && Instrumento.TasaGarantia != null? Instrumento.TasaGarantia.add(PIBMasMenosSistema.get(0).PibMenos) : null;
					PROC_WS.Precio					= null;
					PROC_WS.Monto					= null;	

					
					ValorizacionDatosEntrada.add(PROC_WS);
				};
									
			};
			
			
			logger.debug("Fin megaloop");
			
			//**************************************************************************************************************************************************
			//*********************************** OBTENER TASAS Y PRECIOS DE MERCADO ***************************************************************************
			//**************************************************************************************************************************************************						
			EntregarPreciosTasas_Entrada entregarPreciosTasasEntrada = new EntregarPreciosTasas_Entrada();
			
			entregarPreciosTasasEntrada.p_IdEmpresa 				= in.p_IdEmpresa;
			entregarPreciosTasasEntrada.p_IdUsuario  				= in.p_IdUsuario;			
			entregarPreciosTasasEntrada.p_IdModulo 					= in.p_IdModulo;			
			entregarPreciosTasasEntrada.p_IdPais 					= in.p_IdPais;
			entregarPreciosTasasEntrada.p_DatosInstrumentos         = ConsultarPreciosTasas;
			
			if (entregarPreciosTasasEntrada.p_DatosInstrumentos.size() > 0) {
				EntregarPreciosTasas entregarPreciosTasasService = new EntregarPreciosTasas();
				logger.debug("Inicio EntregarPrecioTasa");
				EntregarPreciosTasas_Salida entregarPreciosTasasSalida = entregarPreciosTasasService.logica(con, entregarPreciosTasasEntrada);
				logger.debug("Fin EntregarPrecioTasa");
				HashMap<Long, Object> precios = Utils.List2Hash(entregarPreciosTasasSalida.P_OUT_PRECIOSTASAS,"IdRegistro");
				
				for (ObjProcesamiento VDE : ValorizacionDatosEntrada){
										
					if (VDE.DimTasa.equalsIgnoreCase("COMP") || VDE.DimTasa.equalsIgnoreCase("GTIA")) {
						continue;
					}
					
					EntregarPreciosTasas_Salida_PreciosTasas PrecioTasaMercado = (EntregarPreciosTasas_Salida_PreciosTasas) precios.get(VDE.IdProcesamiento);
					if ((PrecioTasaMercado == null || PrecioTasaMercado.MensajeError != null)) {
						VDE.MensajeErrorFuente = PrecioTasaMercado != null ? PrecioTasaMercado.MensajeError +"-"+ PrecioTasaMercado.CodigoFuente: Diccionario.getMensaje(con, in.p_IdEmpresa, "ERROR_ENTREGA_PRECIO/TASA_MERCADO").add("nemotecnico", VDE.Nemotecnico).msg();;
						continue;
					}
					
					VDE.FechaPrecioTasa = Fecha.toDate(Fecha.toStringDate(PrecioTasaMercado.FechaFuente));
					VDE.FuentePrecioTasa = PrecioTasaMercado.CodigoFuente;
					
					if ( VDE.DimTasa.equals("MERC") ){
						
						// MSH : Si no hay precio o es cero debe ir por tasa
						if ( VDE.IdClasificacionTipoInstrumento.equals("IF") || VDE.IdClasificacionTipoInstrumento.equals("RF") ) {
							VDE.Precio	= PrecioTasaMercado.Precio;
							VDE.Tasa	= PrecioTasaMercado.Tasa == null ? BigDecimal.ZERO : PrecioTasaMercado.Tasa;
							if ( VDE.ValorizaPor.equals("P") ) {
								if ( VDE.Precio == null || VDE.Precio.compareTo(BigDecimal.ZERO) == 0 ) {
									VDE.Precio	= null;
								} else { 
									VDE.Tasa	= null;
								}
							} else {
								if ( PrecioTasaMercado.Tasa != null ) {
									VDE.Precio	= null;
								} else { 
									VDE.Tasa	= VDE.Precio == null || VDE.Precio.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : null ;
								}
								
							}
						} else {
							VDE.Tasa	= VDE.ValorizaPor.equals("T") ? PrecioTasaMercado.Tasa : (PrecioTasaMercado.Precio == null ? PrecioTasaMercado.Tasa : null);
							VDE.Precio	= VDE.ValorizaPor.equals("P") ? PrecioTasaMercado.Precio : (PrecioTasaMercado.Tasa == null ? PrecioTasaMercado.Precio : null);
						}
												
						if (VDE.SubTipoValorizacion.equals("PAGARE") && VDE.Tasa == null && VDE.TasaEmision != null ){
							VDE.Tasa 	= VDE.Tasa == null && VDE.Precio != null ? ( (VDE.Precio.multiply(VDE.TasaEmision)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP) ) : VDE.Tasa;
							//VDE.Precio 	= VDE.Precio == null && VDE.Tasa != null ? ( (VDE.Tasa.multiply(new BigDecimal(100))).divide(VDE.TasaEmision,20,RoundingMode.HALF_UP) ) : VDE.Precio;
						};
					};					
					if (VDE.DimTasa.equals("P-MC")){
						VDE.Tasa = PrecioTasaMercado.Tasa.subtract(PIBMasMenosSistema.get(0).PibMas);
					};
					if (VDE.DimTasa.equals("P-MC")){
						VDE.Tasa = PrecioTasaMercado.Tasa.add(PIBMasMenosSistema.get(0).PibMas);
					};
					if("RF".equals(VDE.IdClasificacionTipoInstrumento) && VDE.Tasa.compareTo(tasaMaximaValorizadorRF) == 1) {
						VDE.Tasa = tasaMaximaValorizadorRF;
					}
				};
			};
			
			for (ObjProcesamiento VDE : ValorizacionDatosEntrada){    
			    if(VDE.MensajeErrorFuente!= null && (VDE.IdClasificacionTipoInstrumento.equals("IF") || VDE.IdClasificacionTipoInstrumento.equals("RF")|| VDE.IdClasificacionTipoInstrumento.equals("RV"))) {
			    	out.P_OUT_ERRORVALORIZACIONES.add(VDE);  
			    }     
		   }
			
			Map<Long, List<ObjProcesamiento>> hashValorizacionDatosEntrada = ValorizacionDatosEntrada.stream().collect(Collectors.groupingBy(t -> t.IdValorizacion));	
			//**************************************************************************************************************************************************
			//*********************************** CALCULAR INTERES *********************************************************************************************
			//**************************************************************************************************************************************************
			
			HashMap<Long, Object> ResultCalculoInteres = this.calcularInteres(con, in, hashValorizacionDatosEntrada, TipoValorizacionPorTipoInstrumento);
			//**************************************************************************************************************************************************
			//************************************ VALORIZAR CARTERAS ******************************************************************************************
			//**************************************************************************************************************************************************
			Valorizar.InValorizar EntradaValorizador = new Valorizar.InValorizar();
			EntradaValorizador.p_IdEmpresa 				= in.p_IdEmpresa;
			EntradaValorizador.p_IdUsuario 				= in.p_IdUsuario;
			EntradaValorizador.p_IdModulo 				= in.p_IdModulo;
			EntradaValorizador.p_IdPais 				= in.p_IdPais;			
			EntradaValorizador.p_IdMonedaAdicional		= in.p_IdMonedaAdicional;
			
			List<Valorizar.InValorizar.InstrumentoWS> ListaValorizacionSubyacentePacto = new ArrayList<Valorizar.InValorizar.InstrumentoWS>();
			
			for (ObjProcesamiento VDE : ValorizacionDatosEntrada){
				if (VDE.Precio == null && VDE.Tasa == null && VDE.Monto == null) continue;
				ObjTipoValorizacionPorTipoInstrumento TipoValorizacion = (ObjTipoValorizacionPorTipoInstrumento) TipoValorizacionPorTipoInstrumento.get(VDE.IdTipoInstrumento);
				
				if ( VDE.TipoValorizacion.endsWith("PQ") ){
					Valorizar.InValorizar.InstrumentoPQ InsPQ = new Valorizar.InValorizar.InstrumentoPQ();
					
					InsPQ.IdValorizacion = VDE.IdValorizacion; 
					InsPQ.IdInstrumento = VDE.IdInstrumento; 
					InsPQ.Nemotecnico = VDE.Nemotecnico; 
					InsPQ.IdMonedaInstrumento = VDE.IdMonedaInstrumento; 
					InsPQ.Cantidad = VDE.Cantidad; 
					InsPQ.Precio = VDE.Precio; 
					InsPQ.Monto = VDE.Monto;
					InsPQ.Tasa = VDE.Tasa;
					
					InsPQ.IdValorizacion	= VDE.IdProcesamiento;
					InsPQ.FechaValorizacion	= VDE.FechaCalculada.substring(0,10);
					InsPQ.PlazoActual = new BigDecimal(VDE.PlazoResidual == null ? 0L : VDE.PlazoResidual);
					EntradaValorizador.p_InstrumentosPQ.add(InsPQ);
				};
				
				if ( VDE.TipoValorizacion.endsWith("WS") ){
					Valorizar.InValorizar.InstrumentoWS InsWS = new Valorizar.InValorizar.InstrumentoWS();
					
					InsWS.IdValorizacion = VDE.IdValorizacion; 
					InsWS.IdInstrumento = VDE.IdInstrumento; 
					InsWS.Nemotecnico = VDE.Nemotecnico; 
					InsWS.FechaEmision = VDE.FechaEmision; 					
					InsWS.FechaVencimiento = VDE.FechaVencimiento; 
					InsWS.IdMonedaInstrumento = VDE.IdMonedaInstrumento;
					InsWS.IdMonedaEmisionInstrumento = VDE.IdMonedaEmisionInstrumento;
					InsWS.IdTipoReajusteInstrumento = VDE.IdTipoReajusteInstrumento;
					InsWS.Cantidad = VDE.Cantidad; 
					InsWS.ValorRescate = VDE.ValorRescate; 
					InsWS.Precio = VDE.Precio; 
					InsWS.Tasa = VDE.Tasa; 
					InsWS.Monto = VDE.Monto; 
					InsWS.IdClasificacionTipoInstrumento = VDE.IdClasificacionTipoInstrumento;
					InsWS.Emisor = VDE.Emisor;
					InsWS.IdCondicionDeLiquidacion = VDE.IdCondicionDeLiquidacion;
					InsWS.TipoValorizacionInstrumento = VDE.TipoValorizacionInstrumento;
					InsWS.SubTipoValorizacion = VDE.SubTipoValorizacion;
					InsWS.TipoCalculoInteresesCupon = VDE.TipoCalculoInteresesCupon;
					InsWS.ValorizaCorteDeCupon = VDE.ValorizaCorteDeCupon;
					InsWS.OrigenValorizacion = VDE.OrigenValorizacion;
					InsWS.MontoUI = VDE.MontoUI;
					
					InsWS.TipoValorizacionInstrumento	= localizacionSistema.equals("CL") && (VDE.TipoValorizacionInstrumento == null || VDE.TipoValorizacionInstrumento.equals("")) ? null : VDE.TipoValorizacionInstrumento; //Se usa en valorizacion Chile y se obtiene desde bursatil
					InsWS.IdValorizacion				= VDE.IdProcesamiento;
					InsWS.FechaValorizacion				= VDE.FechaCalculada.substring(0,10);
					InsWS.Base							= TipoValorizacion.BaseCalculoDeInteres.intValue();
					InsWS.IdReajusteCabecera 			= VDE.IdReajusteCabecera;
					
					//Filtra Valorizacion Subyacente Pacto
					if("S".equals(VDE.EsPacto)) {
						ListaValorizacionSubyacentePacto.add(InsWS);
					}else {
						EntradaValorizador.p_InstrumentosWS.add(InsWS);
					}
					
				};
			};					
			
			Valorizar Valorizador = new Valorizar();			
			ValorizarSubyacentePacto ValorizacionDetallePacto = new ValorizarSubyacentePacto();
			EntradaValorizarSubyacentePacto EntradaValorizarSubyacentePacto = new EntradaValorizarSubyacentePacto();
			SalidaValorizarSubyacentePacto SalidaValorizarSubyacentePacto = new SalidaValorizarSubyacentePacto();
			
			if(!ListaValorizacionSubyacentePacto.isEmpty()) {
				EntradaValorizarSubyacentePacto.p_IdEmpresa 	= in.p_IdEmpresa;
				EntradaValorizarSubyacentePacto.p_IdUsuario 	= in.p_IdUsuario;
				EntradaValorizarSubyacentePacto.p_IdModulo 		= in.p_IdModulo;
				EntradaValorizarSubyacentePacto.p_InstrumentosWS = ListaValorizacionSubyacentePacto;	
				
				SalidaValorizarSubyacentePacto = ValorizacionDetallePacto.logica(con, EntradaValorizarSubyacentePacto);
				
			}
			
			
			Valorizar.OutValorizar ResultValorizador = Valorizador.logica(con, EntradaValorizador);
			logger.debug("Fin Valorizador");
			
			//agregar valorizacion subyacente			
			ResultValorizador.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS.putAll(SalidaValorizarSubyacentePacto.P_RESULTADOS_VALORIZACION);
			
			
			//**************************************************************************************************************************************************
			//************************************* PREPARAR SALIDA ********************************************************************************************
			//**************************************************************************************************************************************************
			
			ValorizarInstrumentoSalida salida =this.ordenarResultadoaValorizacion(con, in, ResultValorizador, hashValorizacionDatosEntrada, ResultCalculoInteres, InfoInstrumentos, FechaSistema);			
			out.P_OUT_INSTRUMENTOSVALORIZADOS= salida.P_OUT_INSTRUMENTOSVALORIZADOS;
			
			
			//Salida Valorizador
			for (ObjResultadoValorizacion resultVal : out.P_OUT_INSTRUMENTOSVALORIZADOS){
				if (resultVal.ResultadoProceso.equals("ERROR")){
					out.P_OUT_MENSAJES.add(new MensajeSalida(resultVal.IdProcesamiento ,MensajeSalida.MENSAJE_ERROR, resultVal.MensajeProceso, new HashMap<>() ,con,in.p_IdEmpresa));
					out.P_OUT_ERRORESCONTADOR++;
				} else if (resultVal.MensajeErrorFuente != null){
					out.P_OUT_MENSAJES.add(new MensajeSalida(resultVal.IdProcesamiento ,MensajeSalida.MENSAJE_ADVERTENCIA, resultVal.MensajeErrorFuente, new HashMap<>() ,con,in.p_IdEmpresa));
				};
			};
			logger.debug("fin diccionario salida");
		}
		catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		return out;
	};
	
	
	public HashMap<Long,Object> calcularInteres(Connection con,ValorizarInstrumentoEntrada in,Map<Long, List<ObjProcesamiento>> hashValorizacionDatosEntrada,HashMap<String,Object> TipoValorizacionPorTipoInstrumento) throws Exception {
		
		CalcularIntereses.CalcularInteresesEntrada CalcularInteres = new CalcularIntereses.CalcularInteresesEntrada();
		for (ObjEntradaInstrumentos InInstrumento : in.p_Instrumentos ) {
			
			if (InInstrumento.OrigenProcesamiento.equalsIgnoreCase("FLUJOCUSTODIA")) {
				continue;
			}
			
			ObjTipoValorizacionPorTipoInstrumento TipoValorizacion = (ObjTipoValorizacionPorTipoInstrumento) TipoValorizacionPorTipoInstrumento.get(InInstrumento.IdTipoInstrumento);											
			
			List<ObjProcesamiento> datosEntrada = hashValorizacionDatosEntrada.get(InInstrumento.IdValorizacion);
			
			if (datosEntrada == null) {
				//en el caso que no este configurada la valorizacion para este tipo de instrumento
				continue;
			}
		
			for (ObjProcesamiento VDE: datosEntrada){
				if ( !VDE.IdValorizacion.equals( InInstrumento.IdValorizacion) ) continue;
				if (TipoValorizacion.CalculaInteresesAPlazo.equalsIgnoreCase("S") && InInstrumento.TasaCompra != null){						
					CalcularIntereses.ObjDatosEntrada RI = new CalcularIntereses.ObjDatosEntrada();
					RI.IdTrabajo			= VDE.IdProcesamiento;
					RI.FechaCalculo			= VDE.FechaCalculada;
					RI.Plazo				= InInstrumento.PlazoEnDiasOperacion;
					RI.PlazoResidual 		= VDE.PlazoResidual != null || VDE.PlazoResidual >0 ? VDE.PlazoResidual : 0L;
					RI.IdMonedaConsulta		= InInstrumento.IdMonedaInstrumento;
					RI.MontoMonedaConsulta	= InInstrumento.MontoCompra;
					RI.Tasa					= InInstrumento.TasaCompra;
					RI.Base					= TipoValorizacion.BaseCalculoDeInteres;
					RI.TipoInteres			= TipoValorizacion.TipoCalculoInteres;
					RI.Cantidad				= InInstrumento.Cantidad;
					RI.PrecioCompra			= InInstrumento.PrecioCompra;
					CalcularInteres.DatosEntrada.add(RI);
				};
			};
		};
		
		CalcularIntereses CI = new CalcularIntereses();		
		CalcularInteres.IdEmpresa = in.p_IdEmpresa;
		HashMap<Long, Object> ResultCalculoInteres = new HashMap<>();
		if (CalcularInteres.DatosEntrada.size() > 0) {
			CalcularIntereses.CalcularInteresesSalida RCI = CI.logica(con, CalcularInteres);
			ResultCalculoInteres = Utils.List2Hash(RCI.ResultadoCalculoInteres,"IdTrabajo");
		}
		
		return ResultCalculoInteres;
		
	}
	
	
	private EntregarPreciosTasas_Entrada_DatosInstrumentos creaPojoEntregarPrecioTasa(ObjProcesamiento PROC_WS) {
		EntregarPreciosTasas_Entrada_DatosInstrumentos d = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
		
		//d.IdRegistro = PROC_WS.IdRegistro;
		d.TipoCartera = PROC_WS.TipoCartera;
		d.IdUso = PROC_WS.IdUso;
		d.IdTipoCriterioTasa = PROC_WS.IdTipoCriterioTasa;
		d.IdClasificacionTipoInstrumento = PROC_WS.IdClasificacionTipoInstrumento;
		d.IdTipoInstrumento = PROC_WS.IdTipoInstrumento;
		d.IdInstrumento = PROC_WS.IdInstrumento;
		d.Nemotecnico = PROC_WS.Nemotecnico;
		//d.IdMoneda = PROC_WS.IdMoneda;
		//d.FechaConsulta = PROC_WS.FechaConsulta;
		d.PlazoResidual = PROC_WS.PlazoResidual;
		//d.NemoSVS = PROC_WS.NemoSVS;
		
		return d;
	}

	private ObjProcesamiento creaPojoValorizacion(ObjEntradaInstrumentos instrumento,
			ObjTipoValorizacionPorTipoInstrumento tipoValorizacion, ObjInstrumento informacionAdicionalInstrumento) {
		
		ObjProcesamiento PROC_WS = new ObjProcesamiento();
		
		
		
		PROC_WS.IdValorizacion = instrumento.IdValorizacion;
		/*PROC_WS.OrigenProcesamiento = instrumento.OrigenProcesamiento;
		PROC_WS.FechaValorizacion = instrumento.FechaValorizacion;
		PROC_WS.FechaOperacion = instrumento.FechaOperacion;
		PROC_WS.FechaVencimientoOperacion = instrumento.FechaVencimientoOperacion;
		PROC_WS.PlazoEnDiasOperacion = instrumento.PlazoEnDiasOperacion;*/
		PROC_WS.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento;
		PROC_WS.IdTipoInstrumento = instrumento.IdTipoInstrumento;
		PROC_WS.IdInstrumento = instrumento.IdInstrumento;
		PROC_WS.Nemotecnico = instrumento.Nemotecnico;
		PROC_WS.FechaVencimiento = instrumento.FechaVencimiento;
		PROC_WS.FechaEmision = instrumento.FechaEmision;
		PROC_WS.IdMonedaInstrumento = instrumento.IdMonedaInstrumento;
		PROC_WS.IdMonedaEmisionInstrumento = instrumento.IdMonedaEmisionInstrumento;
		PROC_WS.IdTipoReajusteInstrumento = instrumento.IdTipoReajusteInstrumento;
		PROC_WS.Emisor = instrumento.Emisor;
		PROC_WS.IdCondicionDeLiquidacion = instrumento.IdCondicionDeLiquidacion;
		//PROC_WS.FechaDeLiquidacion = instrumento.FechaDeLiquidacion;
		PROC_WS.Cantidad = instrumento.Cantidad;
		PROC_WS.ValorRescate = instrumento.ValorRescate;
		/*PROC_WS.PrecioCompra = instrumento.PrecioCompra;
		PROC_WS.TasaCompra = instrumento.TasaCompra;
		PROC_WS.MontoCompra = instrumento.MontoCompra;
		PROC_WS.MontoCompraMonedaSistema = instrumento.MontoCompraMonedaSistema;*/
		PROC_WS.PrecioSubyacente = instrumento.PrecioSubyacente;
		PROC_WS.TasaSubyacente = instrumento.TasaSubyacente;
		PROC_WS.PrecioGarantia = instrumento.PrecioGarantia;
		PROC_WS.TasaGarantia = instrumento.TasaGarantia;
		PROC_WS.TasaTIF = instrumento.TasaTIF;
		//PROC_WS.IdPortafolio = instrumento.IdPortafolio;
		PROC_WS.TablaOrigen = instrumento.TablaOrigen;	
		PROC_WS.IdUso = instrumento.IdUso;
		PROC_WS.TipoCartera = instrumento.TipoCartera;
		PROC_WS.IdTipoCriterioTasa = instrumento.IdTipoCriterioTasa;
		
		
		
		//PROC_WS.IdTipoValorizacionPorTipoInstrumento = tipoValorizacion.IdTipoValorizacionPorTipoInstrumento;
		PROC_WS.IdTipoInstrumento = tipoValorizacion.IdTipoInstrumento; 
		/*PROC_WS.UsaTasaCompra = tipoValorizacion.UsaTasaCompra; 
		PROC_WS.UsaTasaMercado = tipoValorizacion.UsaTasaMercado;
		PROC_WS.UsaTasaCorredora = tipoValorizacion.UsaTasaCorredora;
		PROC_WS.UsaTasaTIF = tipoValorizacion.UsaTasaTIF; 
		PROC_WS.UsaPibMasMenos = tipoValorizacion.UsaPibMasMenos;
		PROC_WS.ValorizaAnterior = tipoValorizacion.ValorizaAnterior; 
		PROC_WS.ValorizaActual = tipoValorizacion.ValorizaActual;
		PROC_WS.ValorizaProximo = tipoValorizacion.ValorizaProximo;*/
		PROC_WS.CalculaIntereses = tipoValorizacion.CalculaIntereses;
		PROC_WS.CalculaInteresesAPlazo = tipoValorizacion.CalculaInteresesAPlazo;
		/*PROC_WS.TipoCalculoInteres = tipoValorizacion.TipoCalculoInteres;
		PROC_WS.BaseCalculoDeInteres = tipoValorizacion.BaseCalculoDeInteres;*/
		PROC_WS.TipoValorizacion = tipoValorizacion.TipoValorizacion;
		PROC_WS.SubTipoValorizacion = tipoValorizacion.SubTipoValorizacion;
		
		PROC_WS.IdInstrumento = informacionAdicionalInstrumento.IdInstrumento;
		PROC_WS.IdClasificacionTipoInstrumento = informacionAdicionalInstrumento.IdClasificacionTipoInstrumento;
		PROC_WS.IdTipoInstrumento = informacionAdicionalInstrumento.IdTipoInstrumento;
		PROC_WS.Nemotecnico = informacionAdicionalInstrumento.Nemotecnico;
		/*PROC_WS.ISIN = informacionAdicionalInstrumento.ISIN;
		PROC_WS.IdEmisor = informacionAdicionalInstrumento.IdEmisor;
		PROC_WS.IdTipoReajuste = informacionAdicionalInstrumento.IdTipoReajuste;
		PROC_WS.FechaEmisionInstrumento = informacionAdicionalInstrumento.FechaEmisionInstrumento;
		PROC_WS.FechaVencimientoInstrumento = informacionAdicionalInstrumento.FechaVencimientoInstrumento;
		PROC_WS.IdMoneda = informacionAdicionalInstrumento.IdMoneda;
		PROC_WS.CorteMinimo = informacionAdicionalInstrumento.CorteMinimo;*/
		PROC_WS.Tamano = informacionAdicionalInstrumento.Tamano;
		/*PROC_WS.IndArt104 = informacionAdicionalInstrumento.IndArt104;
		PROC_WS.TasaFiscal = informacionAdicionalInstrumento.TasaFiscal;*/
		PROC_WS.TasaEmision = informacionAdicionalInstrumento.TasaEmision;
		PROC_WS.ValorizaPor = informacionAdicionalInstrumento.ValorizaPor;
		PROC_WS.TipoValorizacionInstrumento = informacionAdicionalInstrumento.TipoValorizacionInstrumento;
		/*PROC_WS.TieneCupon = informacionAdicionalInstrumento.TieneCupon;
		PROC_WS.NumeroCupones = informacionAdicionalInstrumento.NumeroCupones;
		PROC_WS.CallConfirmado = informacionAdicionalInstrumento.CallConfirmado;
		PROC_WS.CuponeraVariable = informacionAdicionalInstrumento.CuponeraVariable;*/
		PROC_WS.TipoCalculoInteresesCupon = informacionAdicionalInstrumento.TipoCalculoInteresesCupon;
		PROC_WS.ValorClasificacionRiesgo = informacionAdicionalInstrumento.ValorClasificacionRiesgo;
		
		return PROC_WS;
	}
	
	private BigDecimal obtenerTasaMaximaValorizadorRF(Connection con, Long idEmpresa) {
		try {
			String tasa = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa,"PLATAFORMA", "PLA.TasaMaximaValorizadorRF");
			return new BigDecimal(tasa);
		} catch (Exception e) {
			logger.warn("obtenerTasaMaximaValorizadorRF:: No existe parametro PLA.TasaMaximaValorizadorRF, se toma valor por defecto");
			return TASA_MAXIMA_VALORIZADOR_RF_DEFAULT;
		}
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ValorizarInstrumentoEntrada jin=gson.fromJson(in.toString() ,ValorizarInstrumentoEntrada.class);
		ValorizarInstrumentoSalida out=logica(conn,jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public ValorizarInstrumentoSalida ordenarResultadoaValorizacion (Connection con,ValorizarInstrumentoEntrada in,Valorizar.OutValorizar ResultValorizador
			, Map<Long, List<ObjProcesamiento>> hashValorizacionDatosEntrada,HashMap<Long, Object> ResultCalculoInteres ,HashMap<Long,Object> InfoInstrumentos
		, String FechaSistema) throws Exception {		
		logger.debug("inicio actualizacion valorizacion");
		ValorizarInstrumentoSalida out = new ValorizarInstrumentoSalida();
		String MonedaSistema		= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaSistema");	
		String MonedaAdicional		= in.p_IdMonedaAdicional == null?ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaAdicional"):in.p_IdMonedaAdicional;
		
		HashMap<Long, InstrumentoPQ> ResultValorizacionPQ = ResultValorizador.P_OUT_HASH_INSTRUMENTOSVALORIZADOSPQ;
		HashMap<Long, InstrumentoWS> ResultValorizacionWS = ResultValorizador.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS;
				
		
				
		for (ObjEntradaInstrumentos InInstrumento : in.p_Instrumentos ) {
			
			List<ObjProcesamiento> datosEntrada = hashValorizacionDatosEntrada.get(InInstrumento.IdValorizacion);
			
			if (datosEntrada == null) {
				continue;
			}
			
			for (ObjProcesamiento PQ_WS: datosEntrada){					
				if ( !PQ_WS.IdValorizacion.equals( InInstrumento.IdValorizacion) ) continue;							
				ObjInstrumento Instrumento = (ObjInstrumento) InfoInstrumentos.get(PQ_WS.IdInstrumento);
				
				//Informacion del proceso
				ObjResultadoValorizacion Resultado = new ObjResultadoValorizacion();
				
				Resultado.IdProcesamiento = PQ_WS.IdProcesamiento;
				Resultado.IdValorizacion = PQ_WS.IdValorizacion;
				Resultado.TipoValorizacion = PQ_WS.TipoValorizacion; 
				Resultado.SubTipoValorizacion = PQ_WS.SubTipoValorizacion;
				Resultado.TipoValorizacionInstrumento = PQ_WS.TipoValorizacionInstrumento;
				Resultado.IdUso = PQ_WS.IdUso; 
				Resultado.TipoCartera = PQ_WS.TipoCartera; 
				Resultado.IdTipoCriterioTasa = PQ_WS.IdTipoCriterioTasa;
				Resultado.IdClasificacionTipoInstrumento = PQ_WS.IdClasificacionTipoInstrumento;		
				Resultado.IdInstrumento = PQ_WS.IdInstrumento; 
				Resultado.Nemotecnico = PQ_WS.Nemotecnico;
				Resultado.Emisor = PQ_WS.Emisor;
				Resultado.Cantidad = PQ_WS.Cantidad;
				Resultado.ValorRescate = PQ_WS.ValorRescate;	
				Resultado.MensajeErrorFuente = PQ_WS.MensajeErrorFuente;
				Resultado.FechaPrecioTasa = PQ_WS.FechaPrecioTasa;					//Fecha de la tasa mercado obtenida 
				Resultado.FuentePrecioTasa = PQ_WS.FuentePrecioTasa;					//Fuente de la tasa mercado obtenida		
				Resultado.Precio = PQ_WS.Precio;
				Resultado.Tasa = PQ_WS.Tasa;
				Resultado.CalculaIntereses = PQ_WS.CalculaIntereses;
				Resultado.CalculaInteresesAPlazo = PQ_WS.CalculaInteresesAPlazo;
				Resultado.IdMonedaInstrumento = PQ_WS.IdMonedaInstrumento;
				Resultado.TasaEmision = PQ_WS.TasaEmision;
				Resultado.ValorClasificacionRiesgo = PQ_WS.ValorClasificacionRiesgo;
				Resultado.NemoOriginal = PQ_WS.NemoOriginal;				
				Resultado.DimensionTiempo					= PQ_WS.DimTiempo;
				Resultado.DimensionTasa						= PQ_WS.DimTasa;					
				Resultado.FechaDimemsion					= Fecha.toDate(PQ_WS.FechaCalculada);
				Resultado.ResultadoProceso					= "OK";
				Resultado.MensajeProceso					= "";
				
				Resultado.IdClasificacionTipoInstrumento	= InInstrumento.IdClasificacionTipoInstrumento;
				Resultado.IdMonedaSistema					= MonedaSistema;
				Resultado.IdMonedaAdicional					= MonedaAdicional;
				Resultado.FechaOperacion					= Fecha.toDate(InInstrumento.FechaOperacion);
				
				// Se utiliza este dato cuando la cartera todavía no es materializada => debe conservar el monto original, no debe valorizar...
				Resultado.MontoDevengadoAMonedaSistema		= PQ_WS.MontoMonedaSistemaOriginal;
				
				Resultado.PlazoEnDiasOperacion 				= InInstrumento.PlazoEnDiasOperacion;
				Resultado.MontoCompra 						= InInstrumento.MontoCompra;
				
				//Informacion de valorizacion RV
				Valorizar.OutValorizar.InstrumentoPQ ResultadoValorizacionPQ = (Valorizar.OutValorizar.InstrumentoPQ) ResultValorizacionPQ.get(PQ_WS.IdProcesamiento);
				if (ResultadoValorizacionPQ != null && PQ_WS.TipoValorizacion.equals("PQ")){
					
					Resultado.IdValorizacion = ResultadoValorizacionPQ.IdValorizacion;
					Resultado.FechaValorizacion = ResultadoValorizacionPQ.FechaValorizacion;
					Resultado.IdInstrumento = ResultadoValorizacionPQ.IdInstrumento;
					Resultado.Nemotecnico = ResultadoValorizacionPQ.Nemotecnico;
					Resultado.IdMonedaInstrumento  = ResultadoValorizacionPQ.IdMonedaInstrumento;
					Resultado.Cantidad = ResultadoValorizacionPQ.Cantidad;
					Resultado.Precio = ResultadoValorizacionPQ.Precio;
					Resultado.MontoMonedaSistema = ResultadoValorizacionPQ.MontoMonedaSistema;
					Resultado.MontoMonedaInstrumento = ResultadoValorizacionPQ.MontoMonedaInstrumento; 
					Resultado.MontoMonedaAdicional = ResultadoValorizacionPQ.MontoMonedaAdicional;
					Resultado.FactorMonedaInstrumento = ResultadoValorizacionPQ.FactorMonedaInstrumento; 
					Resultado.FechaFactorMonedaInstrumento = ResultadoValorizacionPQ.FechaFactorMonedaInstrumento;
					Resultado.FactorMonedaAdicional = ResultadoValorizacionPQ.FactorMonedaAdicional;
					Resultado.FechaFactorMonedaAdicional = ResultadoValorizacionPQ.FechaFactorMonedaAdicional; 
					Resultado.ResultadoProceso = ResultadoValorizacionPQ.ResultadoProceso;
					Resultado.Tasa = ResultadoValorizacionPQ.Tasa;
					
					Resultado.MensajeProceso = ResultadoValorizacionPQ.DescripcionProceso;
					Resultado.Cantidad = PQ_WS.Tamano > 0L && Resultado.Cantidad != null? Resultado.Cantidad.divide( new BigDecimal(PQ_WS.Tamano),20,RoundingMode.HALF_UP)  : Resultado.Cantidad;
				};
				
				//Informacion de valorizacion por WebService
				Valorizar.OutValorizar.InstrumentoWS ResultadoValorizacionWS = (Valorizar.OutValorizar.InstrumentoWS) ResultValorizacionWS.get(PQ_WS.IdProcesamiento);
				if (ResultadoValorizacionWS != null && PQ_WS.TipoValorizacion.equals("WS")){
					
					Resultado.IdValorizacion = ResultadoValorizacionWS.IdValorizacion;
					Resultado.FechaValorizacion = ResultadoValorizacionWS.FechaValorizacion;
					Resultado.IdInstrumento = ResultadoValorizacionWS.IdInstrumento;
					Resultado.Nemotecnico = ResultadoValorizacionWS.Nemotecnico;
					Resultado.IdMonedaInstrumento = ResultadoValorizacionWS.IdMonedaInstrumento;
					Resultado.Cantidad = ResultadoValorizacionWS.Cantidad;
					Resultado.ValorRescate = ResultadoValorizacionWS.ValorRescate;
					Resultado.Precio = ResultadoValorizacionWS.Precio;
					Resultado.Tasa = ResultadoValorizacionWS.Tasa;
					Resultado.Tasa360 = ResultadoValorizacionWS.Tasa360;
					Resultado.MontoMonedaSistema = ResultadoValorizacionWS.MontoMonedaSistema; 
					Resultado.MontoMonedaInstrumento = ResultadoValorizacionWS.MontoMonedaInstrumento; 
					Resultado.MontoMonedaAdicional = ResultadoValorizacionWS.MontoMonedaAdicional; 
					Resultado.TipoValorizacionInstrumento = ResultadoValorizacionWS.TipoValorizacionInstrumento;
					Resultado.MontoInteresMonedaSistema = ResultadoValorizacionWS.MontoInteresMonedaSistema; 
					Resultado.MontoInteresMonedaInstrumento = ResultadoValorizacionWS.MontoInteresMonedaInstrumento; 
					Resultado.MontoInteresMonedaAdicional = ResultadoValorizacionWS.MontoInteresMonedaAdicional;  
					Resultado.MontoReajusteMonedaSistema = ResultadoValorizacionWS.MontoReajusteMonedaSistema; 
					Resultado.MontoReajusteMonedaInstrumento = ResultadoValorizacionWS.MontoReajusteMonedaInstrumento; 
					Resultado.MontoReajusteMonedaAdicional = ResultadoValorizacionWS.MontoReajusteMonedaAdicional;  
					Resultado.FactorMonedaInstrumento = ResultadoValorizacionWS.FactorMonedaInstrumento; 
					Resultado.FechaFactorMonedaInstrumento = ResultadoValorizacionWS.FechaFactorMonedaInstrumento; 
					Resultado.FactorMonedaAdicional = ResultadoValorizacionWS.FactorMonedaAdicional; 
					Resultado.FechaFactorMonedaAdicional = ResultadoValorizacionWS.FechaFactorMonedaAdicional;
					Resultado.Duration = ResultadoValorizacionWS.Duration; 			
					Resultado.Convexidad = ResultadoValorizacionWS.Convexidad; 
					Resultado.PlazoValorizador = ResultadoValorizacionWS.PlazoValorizador; 
					Resultado.IdClasificacionTipoInstrumento = ResultadoValorizacionWS.IdClasificacionTipoInstrumento;
					Resultado.Emisor = ResultadoValorizacionWS.Emisor;
					Resultado.ResultadoProceso = ResultadoValorizacionWS.ResultadoProceso; 
					Resultado.ValorTipoReajuste = ResultadoValorizacionWS.ValorTipoReajuste;
					Resultado.ValorPar = ResultadoValorizacionWS.valorPar;
					
					Resultado.MensajeProceso = ResultadoValorizacionWS.DescripcionProceso;
					Resultado.Cantidad = PQ_WS.Tamano > 0L && Resultado.Cantidad != null? Resultado.Cantidad.divide( new BigDecimal(PQ_WS.Tamano),20,RoundingMode.HALF_UP)  : Resultado.Cantidad;
					
					Resultado.MontoMonedaSistema						= ResultadoValorizacionWS.MontoTotalMonedaSistema == null ? BigDecimal.ZERO : ResultadoValorizacionWS.MontoTotalMonedaSistema;		
					Resultado.MontoInteresMonedaSistema					= BigDecimal.ZERO;
					Resultado.MontoInteresAcumuladoMonedaSistema		= BigDecimal.ZERO;
					Resultado.MontoReajusteMonedaSistema				= BigDecimal.ZERO;
					Resultado.MontoReajusteAcumuladoMonedaSistema		= BigDecimal.ZERO;
					//+++++++++++++++++++++++++++++++++++++++++++++++++
					Resultado.MontoMonedaInstrumento					= ResultadoValorizacionWS.MontoTotalMonedaInstrumento == null ? BigDecimal.ZERO : ResultadoValorizacionWS.MontoTotalMonedaInstrumento;						
					Resultado.MontoInteresMonedaInstrumento				= BigDecimal.ZERO;
					Resultado.MontoInteresAcumuladoMonedaInstrumento	= BigDecimal.ZERO;
					Resultado.MontoReajusteMonedaInstrumento			= BigDecimal.ZERO;
					Resultado.MontoReajusteAcumuladoMonedaInstrumento	= BigDecimal.ZERO;
					//+++++++++++++++++++++++++++++++++++++++++++++++++
					Resultado.MontoMonedaAdicional						= ResultadoValorizacionWS.MontoTotalMonedaAdicional == null ? BigDecimal.ZERO : ResultadoValorizacionWS.MontoTotalMonedaAdicional;						
					Resultado.MontoInteresMonedaAdicional				= BigDecimal.ZERO;
					Resultado.MontoInteresAcumuladoMonedaAdicional		= BigDecimal.ZERO;
					Resultado.MontoReajusteMonedaAdicional				= BigDecimal.ZERO;
					Resultado.MontoReajusteAcumuladoMonedaAdicional		= BigDecimal.ZERO;
					//+++++++++++++++++++++++++++++++++++++++++++++++++
					Resultado.ValorTieneCall							= Instrumento.CallConfirmado;
					
					// MSH
					Resultado.IdTipoReajuste = ResultadoValorizacionWS.IdTipoReajusteInstrumento;
					Resultado.ValorTipoReajuste	= ResultadoValorizacionWS.ValorTipoReajuste;
					
					if (PQ_WS.ValorizaCorteDeCupon.equals("S") ){
						Long Diff = Long.MAX_VALUE;
						
						Resultado.IdMonedaPagoCupon = (ResultadoValorizacionWS.IdMonedaInstrumento != null 
								&& ! (ResultadoValorizacionWS.IdMonedaInstrumento.trim().equals("") ) )
								? ResultadoValorizacionWS.IdMonedaInstrumento : MonedaSistema ;
						
						boolean primeraVez = true;
						Valorizar.ObjCupon CuponAUsar = new Valorizar.ObjCupon();
						for ( Valorizar.ObjCupon Cupon : ResultadoValorizacionWS.Cupones ){
							if (primeraVez) {
								CuponAUsar = Cupon;								
								primeraVez = false;
							}
							
							Long Diff_ = Cupon.fecha.getTime() - ResultadoValorizacionWS.FechaValorizacion.getTime();							
							if (!primeraVez &&  Diff_ < Diff){
								CuponAUsar = Cupon;
								Diff = Diff_;
							}
						};
													
						if (CuponAUsar.numero != null){
							
							/*valor de reajuste se debe actualizar segun casuistica de fechacupon - fechaSistema <= 1 
							en caso de cumplirse usaremos reajuste de paridades de cupon sino
							usaremos el reajuste que viene desde valorizador*/
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
							Date fechaSistema = sdf.parse(FechaSistema);
					        Date fechacupon=  sdf.parse(CuponAUsar.fecha+"");
					        
					        long startTime = fechaSistema.getTime();
					        long endTime = fechacupon.getTime();
					        long diasDesde = (long) Math.floor(startTime / (1000*60*60*24)); // convertimos a dias, para que no afecten cambios de hora 
					        long diasHasta = (long) Math.floor(endTime / (1000*60*60*24)); // convertimos a dias, para que no afecten cambios de hora
					        
					        long diferencia = diasHasta - diasDesde;
					        
							
							CalcularParidad par = new CalcularParidad();
							CalcularParidad.CalcularParidadEntrada parin = new CalcularParidad.CalcularParidadEntrada();
							parin.p_IdTrabajo = (long)Math.random()*10000;
							parin.p_IdEmpresa = in.p_IdEmpresa;
							//Reajustes para cupones a la fecha de vencimiento del instrumento
							 	
							
							CalcularParidad.CalculoEntrada auxmon = new CalcularParidad.CalculoEntrada();
									
							auxmon.IdInterno 		= ResultadoValorizacionWS.IdInstrumento;
							auxmon.IdTrabajo 		= parin.p_IdTrabajo;
							auxmon.IdUso 			= "VALORIZADOR";
							auxmon.Fecha 			= Fecha.toStringDate(CuponAUsar.fecha);
							auxmon.IdMonedaOrigen 	= ResultadoValorizacionWS.IdTipoReajusteInstrumento!=null? ResultadoValorizacionWS.IdTipoReajusteInstrumento:MonedaSistema;
							auxmon.MontoOrigen 		= BigDecimal.ONE;
							auxmon.ValorCambioLocalModificado = BigDecimal.ZERO;
							auxmon.IdMonedaDestino 	= ResultadoValorizacionWS.IdMonedaInstrumento!=null?ResultadoValorizacionWS.IdMonedaInstrumento:MonedaAdicional;
							parin.p_CalculosEntrada.add(auxmon);			
										
						
							CalcularParidad.CalcularParidadSalida parout2 = par.logica(con, parin);
							
					        Resultado.ValorTipoReajuste	= diferencia <= 1 ? parout2.p_out_CalculosSalida.get(0).ValorParametroFinanciero :ResultadoValorizacionWS.ValorTipoReajuste;  
							
							Resultado.NumeroCupon					= (long) CuponAUsar.numero;
							Resultado.FechaCorteCupon				= CuponAUsar.fechaAnterior;
							Resultado.FechaPagoCorteCupon			= CuponAUsar.fecha;
							
							
							
							//Interes
							Resultado.MontoInteresesPagoCorteCupon 	= Resultado.ValorTipoReajuste.multiply(CuponAUsar.intereses.multiply( new BigDecimal(100)).multiply(Resultado.Cantidad.divide( new BigDecimal(100),20,RoundingMode.HALF_UP ))).setScale(0, RoundingMode.HALF_UP);
							
							//Total
							Resultado.MontoCorteCuponMonedaSistema 			= Resultado.ValorTipoReajuste.multiply(((CuponAUsar.intereses.multiply( new BigDecimal(100)).multiply(Resultado.Cantidad.divide( new BigDecimal(100),20,RoundingMode.HALF_UP ))).add(CuponAUsar.amortizacion.multiply(new BigDecimal(100)).multiply(ResultadoValorizacionWS.Cantidad.divide(new BigDecimal(100),20,RoundingMode.HALF_UP)))));
							Resultado.MontoCapitalCorteCuponMonedaSistema 	= Resultado.ValorTipoReajuste.multiply(CuponAUsar.amortizacion.multiply(new BigDecimal(100)).multiply(ResultadoValorizacionWS.Cantidad.divide(new BigDecimal(100),20,RoundingMode.HALF_UP)));
							Resultado.MontoInteresCorteCuponMonedaSistema 	= Resultado.ValorTipoReajuste.multiply(CuponAUsar.intereses.multiply( new BigDecimal(100)).multiply(Resultado.Cantidad.divide( new BigDecimal(100),20,RoundingMode.HALF_UP )));
							if(!"CLP".equals(ResultadoValorizacionWS.IdMonedaInstrumento)) {
								Resultado.MontoCorteCuponMonedaInstrumento 			= Resultado.MontoCorteCuponMonedaSistema != null && Resultado.ValorTipoReajuste != null  ? Resultado.MontoCorteCuponMonedaSistema.divide(ResultadoValorizacionWS.FactorMonedaInstrumento,MathContext.DECIMAL128).setScale(4, RoundingMode.UP) : BigDecimal.ZERO;
								Resultado.MontoCapitalCorteCuponMonedaInstrumento 	= Resultado.MontoCapitalCorteCuponMonedaSistema != null && Resultado.ValorTipoReajuste != null  ? Resultado.MontoCapitalCorteCuponMonedaSistema.divide(ResultadoValorizacionWS.FactorMonedaInstrumento,MathContext.DECIMAL128).setScale(4, RoundingMode.UP) : BigDecimal.ZERO;
								Resultado.MontoInteresCorteCuponMonedaInstrumento 	= Resultado.MontoInteresCorteCuponMonedaSistema != null && Resultado.ValorTipoReajuste != null  ? Resultado.MontoInteresCorteCuponMonedaSistema.divide(ResultadoValorizacionWS.FactorMonedaInstrumento,MathContext.DECIMAL128).setScale(4, RoundingMode.UP) : BigDecimal.ZERO;
							}
							else {
								Resultado.MontoCorteCuponMonedaInstrumento			= Resultado.MontoCorteCuponMonedaSistema != null && Resultado.ValorTipoReajuste != null  ? Resultado.MontoCorteCuponMonedaSistema.setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
								Resultado.MontoCapitalCorteCuponMonedaInstrumento	= Resultado.MontoCapitalCorteCuponMonedaSistema != null && Resultado.ValorTipoReajuste != null  ? Resultado.MontoCapitalCorteCuponMonedaSistema.setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
								Resultado.MontoInteresCorteCuponMonedaInstrumento	= Resultado.MontoInteresCorteCuponMonedaSistema != null && Resultado.ValorTipoReajuste != null  ? Resultado.MontoInteresCorteCuponMonedaSistema.setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
							}

							Resultado.MontoCorteCuponMonedaSistema				= Resultado.MontoCorteCuponMonedaSistema.setScale(0, RoundingMode.HALF_UP);
							Resultado.MontoCapitalCorteCuponMonedaSistema		= Resultado.MontoCapitalCorteCuponMonedaSistema.setScale(0, RoundingMode.HALF_UP);
							Resultado.MontoInteresCorteCuponMonedaSistema 		= Resultado.MontoInteresCorteCuponMonedaSistema.setScale(0, RoundingMode.HALF_UP);
							Resultado.MontoReajusteCorteCuponMonedaInstrumento	= ResultadoValorizacionWS.MontoReajusteMonedaInstrumento;
							Resultado.MontoReajusteCorteCuponMonedaSistema		= ResultadoValorizacionWS.MontoReajusteMonedaSistema; //Reajuste Periodo del corte
							//++++++++++++++++++++++++++
							Resultado.MontoCorteCuponMonedaAdicional			= Resultado.MontoCorteCuponMonedaSistema != null && ResultadoValorizacionWS.FactorMonedaAdicional != null ? Resultado.MontoCorteCuponMonedaSistema.multiply(ResultadoValorizacionWS.FactorMonedaAdicional).setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
							Resultado.MontoCapitalCorteCuponMonedaAdicional		= Resultado.MontoCapitalCorteCuponMonedaInstrumento != null && ResultadoValorizacionWS.FactorMonedaAdicional != null ? Resultado.MontoCapitalCorteCuponMonedaInstrumento.multiply(ResultadoValorizacionWS.FactorMonedaAdicional) : BigDecimal.ZERO;
							Resultado.MontoInteresCorteCuponMonedaAdicional		= ResultadoValorizacionWS.MontoInteresMonedaAdicional;
							Resultado.MontoReajusteCorteCuponMonedaAdicional	= ResultadoValorizacionWS.MontoReajusteMonedaAdicional;
						};
						
						if (PQ_WS.EsBonoCero.equals("S")){
							
							Resultado.FechaCorteCupon							= CuponAUsar.fecha;
							Resultado.FechaPagoCorteCupon						= CuponAUsar.fecha;
							
							Resultado.MontoCorteCuponMonedaInstrumento			= ResultadoValorizacionWS.Cantidad;
							Resultado.MontoCapitalCorteCuponMonedaInstrumento	= ResultadoValorizacionWS.Cantidad;
							Resultado.MontoInteresCorteCuponMonedaInstrumento	= BigDecimal.ZERO;
							Resultado.MontoReajusteCorteCuponMonedaInstrumento	= BigDecimal.ZERO;
							
							Resultado.MontoCorteCuponMonedaSistema				= ResultadoValorizacionWS.Cantidad;
							Resultado.MontoCapitalCorteCuponMonedaSistema		= ResultadoValorizacionWS.Cantidad;
							Resultado.MontoInteresCorteCuponMonedaSistema		= BigDecimal.ZERO;
							Resultado.MontoReajusteCorteCuponMonedaSistema		= BigDecimal.ZERO;
							
							Resultado.MontoCorteCuponMonedaAdicional			= ResultadoValorizacionWS.Cantidad;
							Resultado.MontoCapitalCorteCuponMonedaAdicional		= ResultadoValorizacionWS.Cantidad;
							Resultado.MontoInteresCorteCuponMonedaAdicional		= BigDecimal.ZERO;
							Resultado.MontoReajusteCorteCuponMonedaAdicional	= BigDecimal.ZERO;
						};
					};
				};
				
				//Informacion Calculo de interes
				CalcularIntereses.ObjResultado ResultadoCalculoInteres = (CalcularIntereses.ObjResultado) ResultCalculoInteres.get(PQ_WS.IdProcesamiento);
				if (ResultadoCalculoInteres != null){
					BigDecimal PrecioPlazo = InInstrumento.PrecioCompra.subtract(new BigDecimal(1)).multiply(InInstrumento.TasaCompra).multiply(InInstrumento.PrecioCompra);
					Resultado.PrecioAPlazo									= PrecioPlazo.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					
					Resultado.MontoAPlazoMonedaSistema						= ResultadoCalculoInteres.MontoMonedaConsulta;
					Resultado.MontoInteresAPlazoMonedaSistema				= ResultadoCalculoInteres.InteresDiarioMonedaSistema;
					Resultado.MontoInteresAcumuladoAPlazoMonedaSistema		= ResultadoCalculoInteres.InteresAcumuladoMonedaConsulta;
					
					Resultado.MontoAPlazoMonedaInstrumento					= ResultadoCalculoInteres.MontoMonedaConsulta;
					Resultado.MontoInteresAPlazoMonedaInstrumento			= ResultadoCalculoInteres.InteresDiarioMonedaConsulta;
					Resultado.MontoInteresAcumuladoAPlazoMonedaInstrumento	= ResultadoCalculoInteres.InteresAcumuladoMonedaSistema;
					
					Resultado.MontoAPlazoMonedaAdicional					= ResultadoCalculoInteres.MontoMonedaAdicional;
					Resultado.MontoInteresAPlazoMonedaAdicinal				= ResultadoCalculoInteres.InteresDiarioMonedaAdicional;
					Resultado.MontoInteresAcumuladoAPlazoMonedaAdicional	= ResultadoCalculoInteres.InteresAcumuladoMonedaSistema;
					Resultado.MontoMonedaSistema                            = ResultadoCalculoInteres.ValorPresenteMonedaConsulta;
				};
				
				Resultado.IdValorizacion = PQ_WS.IdValorizacion; 

				//SI ES IF SE APLICA formula para duration y convexidad
				if("IF".equalsIgnoreCase(Resultado.IdClasificacionTipoInstrumento) ){
					
					long startTime = Resultado.FechaValorizacion != null? Resultado.FechaValorizacion.getTime(): Fecha.toDate(FechaSistema).getTime();					long endTime = 0;
					
					for (ObjEntradaInstrumentos element : in.p_Instrumentos) {
						if(element.IdInstrumento.equals( Resultado.IdInstrumento) ){
							long endTime2 = Fecha.toDate(element.FechaVencimiento).getTime();
							endTime =endTime2;
						}
					};
					
					
					
				     long diasDesde = (long) Math.floor(startTime / (1000*60*60*24)); // convertimos a dias, para que no afecten cambios de hora 
				     long diasHasta = (long) Math.floor(endTime / (1000*60*60*24)); // convertimos a dias, para que no afecten cambios de hora
				     long TEnDias = diasHasta - diasDesde;
				     BigDecimal TEnDiasSobre360 = new BigDecimal(TEnDias).divide(new BigDecimal(360L),20,RoundingMode.HALF_UP);
				     BigDecimal tasaAnualizada = Resultado.Tasa!= null ? Resultado.Tasa.divide(new BigDecimal(12),20,RoundingMode.HALF_UP): BigDecimal.ZERO;
				     //si es pagare nr o pdbc se anualiza la tasa nemotecnico = PAGARE NR
				     BigDecimal Tasa = Resultado.Nemotecnico.equalsIgnoreCase("PAGARE NR") ||  Resultado.Nemotecnico.equalsIgnoreCase("PDBC") ?  tasaAnualizada : Resultado.Tasa != null ?Resultado.Tasa  : BigDecimal.ZERO ;
				    BigDecimal TasaSobre100 = Tasa.divide(new BigDecimal(100), 20 , RoundingMode.HALF_UP) ;
				    
				    
				    BigDecimal divisorDuration =  TEnDiasSobre360;
				    BigDecimal dividendoDuration =  TasaSobre100.multiply(TEnDiasSobre360).add(BigDecimal.ONE);
				    
				    BigDecimal DosporTasaAlCuadrado = new BigDecimal(2L).multiply((TEnDiasSobre360.pow(2))	); 
				    	
				    BigDecimal CuadradoDeUnoMasTasaSobreCienPorTasaSobretrecientossesenta = (BigDecimal.ONE.add(TasaSobre100.multiply(TEnDiasSobre360))).pow(2);
				    
				    
				    Resultado.Duration = divisorDuration.divide(dividendoDuration,20,RoundingMode.HALF_UP);
				    Resultado.Convexidad = DosporTasaAlCuadrado.divide(CuadradoDeUnoMasTasaSobreCienPorTasaSobretrecientossesenta,20,RoundingMode.HALF_UP);

				}
				
				
				out.P_OUT_INSTRUMENTOSVALORIZADOS.add(Resultado);
			};
			
		};
		logger.debug("Fin actualizacion valorizacion");
		
		//Calculo interes diario
		
		
		// MSH
		// REVIZAR!!!!!!!
		// Se comenta cruce entre las distintas valorizaciones y se reemplaza por asignación simple.
		Map<Long, List<ObjResultadoValorizacion>> hashOutInstrumentosValorizados = out.P_OUT_INSTRUMENTOSVALORIZADOS.stream().collect(Collectors.groupingBy(t -> t.IdValorizacion));
		for (ObjResultadoValorizacion resultActualizar : out.P_OUT_INSTRUMENTOSVALORIZADOS) {
			
			if (!resultActualizar.CalculaIntereses.equals("S")) continue;

			List<ObjResultadoValorizacion> instrumentosValorizados = hashOutInstrumentosValorizados.get(resultActualizar.IdValorizacion);
			if (instrumentosValorizados == null) {
				continue;
			}
			
			if (resultActualizar.IdClasificacionTipoInstrumento.equals("IF") || resultActualizar.IdClasificacionTipoInstrumento.equals("RF")) {
				// Intereses son con respecto a las diferencias de valorizaciones entre días

				for (ObjResultadoValorizacion result : instrumentosValorizados) {
					if (!resultActualizar.DimensionTasa.equals(result.DimensionTasa)) continue;
				
					if (resultActualizar.DimensionTasa.equals("COMP") || resultActualizar.DimensionTasa.equals("MERC") || resultActualizar.DimensionTasa.equals("SUBY") ){
						// -------------------------------------------------------------------------------------------							
						// INTERESES COMPRA AYER + INTERES COMPRA ACUMULADO 
						// -------------------------------------------------------------------------------------------
						if ( ( resultActualizar.DimensionTiempo.equals( "AN" ) && result.DimensionTiempo.equals( "AC" )) || 
							 ( resultActualizar.DimensionTiempo.equals("ACUM") && result.DimensionTiempo.equals( "AC" ))) {
							// Valor Hoy - Valor Ayer
							resultActualizar.MontoInteresMonedaSistema 		= resultActualizar.MontoMonedaSistema != null && result.MontoMonedaSistema != null ? result.MontoMonedaSistema.subtract(resultActualizar.MontoMonedaSistema) : BigDecimal.ZERO;
							resultActualizar.MontoInteresMonedaInstrumento 	= resultActualizar.MontoMonedaInstrumento != null && result.MontoMonedaInstrumento != null ? result.MontoMonedaInstrumento.subtract(resultActualizar.MontoMonedaInstrumento) : BigDecimal.ZERO;
							resultActualizar.MontoInteresMonedaAdicional 	= resultActualizar.MontoMonedaAdicional != null && result.MontoMonedaAdicional != null ? result.MontoMonedaAdicional.subtract(resultActualizar.MontoMonedaAdicional) : BigDecimal.ZERO;

							
							if (result.ValorTipoReajuste != null && resultActualizar.ValorTipoReajuste != null &&
								result.IdTipoReajuste != null && result.IdMonedaInstrumento != null &&
								( !result.IdTipoReajuste.equals("CLP") || !result.IdMonedaInstrumento.equals("CLP") ) &&
								resultActualizar.ValorTipoReajuste.compareTo(BigDecimal.ONE) == 1 && 
								((resultActualizar.ValorTipoReajuste.subtract(result.ValorTipoReajuste)).compareTo(BigDecimal.ZERO) != 0) &&
								(!result.IdTipoReajuste.equals(result.IdMonedaInstrumento))){
								
								Boolean EsDepoAUT = false;
								EsDepoAUT = resultActualizar.Nemotecnico.equalsIgnoreCase("DEPOSITO") || resultActualizar.Nemotecnico.equalsIgnoreCase("AUTODESEMBOLSO") ? true : false;
						 		
								resultActualizar.MontoReajusteMonedaSistema	= EsDepoAUT ? BigDecimal.ZERO : result.MontoMonedaSistema.multiply( (result.ValorTipoReajuste.subtract(resultActualizar.ValorTipoReajuste)).divide(resultActualizar.ValorTipoReajuste, 20,RoundingMode.HALF_UP) );
								resultActualizar.MontoReajusteMonedaInstrumento	= result.MontoMonedaInstrumento.multiply( (result.ValorTipoReajuste.subtract(resultActualizar.ValorTipoReajuste)).divide(resultActualizar.ValorTipoReajuste, 20,RoundingMode.HALF_UP) );
								resultActualizar.MontoInteresMonedaSistema = resultActualizar.MontoInteresMonedaSistema.subtract(resultActualizar.MontoReajusteMonedaSistema);
								resultActualizar.MontoInteresMonedaInstrumento = resultActualizar.MontoInteresMonedaInstrumento.subtract(resultActualizar.MontoReajusteMonedaInstrumento); 
							}								
							else {
								resultActualizar.MontoReajusteMonedaSistema	= BigDecimal.ZERO;
								resultActualizar.MontoReajusteMonedaInstrumento	= BigDecimal.ZERO;
								// resultActualizar.MontoInteresMonedaSistema = BigDecimal.ZERO;
								// resultActualizar.MontoInteresMonedaInstrumento = BigDecimal.ZERO;
							}
							
							if (result.FactorMonedaAdicional != null && resultActualizar.FactorMonedaAdicional != null && result.MontoMonedaAdicional != null &&
								resultActualizar.FactorMonedaAdicional.compareTo(BigDecimal.ONE) == 1 && 
								((resultActualizar.FactorMonedaAdicional.subtract(result.FactorMonedaAdicional)).compareTo(BigDecimal.ZERO) != 0) ){
								// MSH A los intereses se le resta el reajuste de la moneda
								// resultActualizar.MontoReajusteMonedaAdicional	= result.MontoMonedaInstrumento.divide( (result.FactorMonedaAdicional.subtract(resultActualizar.FactorMonedaAdicional) ), 20,RoundingMode.HALF_UP);
								resultActualizar.MontoReajusteMonedaAdicional	= result.MontoMonedaAdicional.multiply( result.FactorMonedaAdicional.subtract(resultActualizar.FactorMonedaAdicional) );
								resultActualizar.MontoInteresMonedaAdicional 	= resultActualizar.MontoInteresMonedaAdicional.subtract(resultActualizar.MontoReajusteMonedaAdicional);									
							}
							
						}

						// -------------------------------------------------------------------------------------------							
						// INTERESES MAÑANA - COMPRA/MERCADO
						// -------------------------------------------------------------------------------------------
						if (resultActualizar.DimensionTiempo.equals( "PR" ) && result.DimensionTiempo.equals( "AC" )){
							
							// MSH
							// Si esta valorizando en t0 o la Cartera todavía no esta materializada
							// debe respetar el monto valorizado en el ingreso de la operacion
							if ( result.DimensionTasa.equals("COMP") && result.MontoDevengadoAMonedaSistema != null ) {
								result.MontoMonedaSistema = result.MontoDevengadoAMonedaSistema;
								result.MontoDevengadoAMonedaSistema = null;
							}
							
							// MSH 
							// El instrumento vence mañana
							if( resultActualizar.ResultadoProceso.equals("INST.VENCIDO") ) {
								resultActualizar.MontoInteresMonedaSistema 		= BigDecimal.ZERO;
								resultActualizar.MontoInteresMonedaInstrumento 	= BigDecimal.ZERO;
								resultActualizar.MontoInteresMonedaAdicional 	= BigDecimal.ZERO;
								resultActualizar.MontoReajusteMonedaSistema		= BigDecimal.ZERO;
								resultActualizar.MontoReajusteMonedaAdicional	= BigDecimal.ZERO;
								resultActualizar.MontoReajusteMonedaInstrumento	= BigDecimal.ZERO;
								
							} else {
								
								if (result.FactorMonedaAdicional != null && resultActualizar.FactorMonedaAdicional != null && result.MontoMonedaAdicional != null &&
									resultActualizar.FactorMonedaAdicional.compareTo(BigDecimal.ONE) == 1 && 
									((resultActualizar.FactorMonedaAdicional.subtract(result.FactorMonedaAdicional)).compareTo(BigDecimal.ZERO) != 0) ){
									// MSH A los intereses se le resta el reajuste de la moneda
									resultActualizar.MontoReajusteMonedaAdicional	= result.MontoMonedaAdicional.multiply( resultActualizar.FactorMonedaAdicional.subtract(result.FactorMonedaAdicional) );
									resultActualizar.MontoInteresMonedaAdicional 	= resultActualizar.MontoInteresMonedaAdicional.subtract(resultActualizar.MontoReajusteMonedaAdicional);									
									
								}
								
								// MSH
								// AC.F.Valorizacion == PRX.F.Corte.Cupon ???
								if ( result.FechaCorteCupon != null && resultActualizar.FechaValorizacion!=null &&
									 resultActualizar.FechaValorizacion.equals(result.FechaCorteCupon) ) {

									// Al valor Proximo se le debe sumar el corte cupon, a excepción de las LH que cambian nemo, en ese caso viene todo listo.
									if ( ( resultActualizar.NemoOriginal == null ) 
										|| ( !resultActualizar.NemoOriginal.equals(result.Nemotecnico) ) ) {
									resultActualizar.MontoMonedaSistema = resultActualizar.MontoMonedaSistema.add(result.MontoCorteCuponMonedaSistema);
									resultActualizar.MontoMonedaInstrumento = resultActualizar.MontoMonedaInstrumento.add(result.MontoCorteCuponMonedaInstrumento);
									resultActualizar.MontoMonedaAdicional = resultActualizar.MontoMonedaAdicional.add(result.MontoCorteCuponMonedaAdicional);
									}
								}
								
								// Valor Proximo - Valor Hoy
								resultActualizar.MontoInteresMonedaSistema 		= resultActualizar.MontoMonedaSistema != null && result.MontoMonedaSistema != null ? resultActualizar.MontoMonedaSistema.subtract(result.MontoMonedaSistema) : BigDecimal.ZERO;
								resultActualizar.MontoInteresMonedaInstrumento 	= resultActualizar.MontoMonedaInstrumento != null && result.MontoMonedaInstrumento != null ? resultActualizar.MontoMonedaInstrumento.subtract(result.MontoMonedaInstrumento) : BigDecimal.ZERO;
								resultActualizar.MontoInteresMonedaAdicional 	= resultActualizar.MontoMonedaAdicional != null && result.MontoMonedaAdicional != null ? resultActualizar.MontoMonedaAdicional.subtract(result.MontoMonedaAdicional) : BigDecimal.ZERO;

								if (result.ValorTipoReajuste != null && resultActualizar.ValorTipoReajuste != null &&
									result.IdTipoReajuste != null && result.IdMonedaInstrumento != null &&
									( !result.IdTipoReajuste.equals("CLP") || !result.IdMonedaInstrumento.equals("CLP") ) &&
									resultActualizar.ValorTipoReajuste.compareTo(BigDecimal.ONE) == 1 && 
									((resultActualizar.ValorTipoReajuste.subtract(result.ValorTipoReajuste)).compareTo(BigDecimal.ZERO) != 0) &&
									(!result.IdTipoReajuste.equals(result.IdMonedaInstrumento))){
									// MSH A los intereses se le resta el reajuste de la moneda
									// resultActualizar.MontoReajusteMonedaInstrumento	= result.MontoMonedaInstrumento.multiply( resultActualizar.FactorMonedaInstrumento.subtract(result.FactorMonedaInstrumento) );
									// resultActualizar.MontoReajusteMonedaInstrumento	= resultActualizar.MontoReajusteMonedaInstrumento;
									
									Boolean EsDepoAUT = false;
									EsDepoAUT = resultActualizar.Nemotecnico.equalsIgnoreCase("DEPOSITO") || resultActualizar.Nemotecnico.equalsIgnoreCase("AUTODESEMBOLSO") ? true : false;
						 		
									resultActualizar.MontoReajusteMonedaSistema	= EsDepoAUT ? BigDecimal.ZERO : result.MontoMonedaSistema.multiply( (resultActualizar.ValorTipoReajuste.subtract(result.ValorTipoReajuste)).divide(result.ValorTipoReajuste, 20,RoundingMode.HALF_UP) );
									resultActualizar.MontoReajusteMonedaInstrumento	= result.MontoMonedaInstrumento.multiply( (resultActualizar.ValorTipoReajuste.subtract(result.ValorTipoReajuste)).divide(result.ValorTipoReajuste, 20,RoundingMode.HALF_UP) );
									resultActualizar.MontoInteresMonedaSistema = resultActualizar.MontoInteresMonedaSistema.subtract(resultActualizar.MontoReajusteMonedaSistema);
									resultActualizar.MontoInteresMonedaInstrumento = resultActualizar.MontoInteresMonedaInstrumento.subtract(resultActualizar.MontoReajusteMonedaInstrumento); 
									
									//Para pactos se recalcula el interes diario (valor final - valor inicial) / Dias operacion
									if ((resultActualizar.Nemotecnico.equalsIgnoreCase("PACTO COMPRA") || resultActualizar.Nemotecnico.equalsIgnoreCase("PACTO VENTA")) && resultActualizar.IdTipoReajuste.equalsIgnoreCase("UF"))
									{	
										
										if(resultActualizar.PlazoEnDiasOperacion != 0L) {
											resultActualizar.MontoInteresMonedaInstrumento = (resultActualizar.ValorRescate.subtract(resultActualizar.MontoCompra.multiply(result.ValorTipoReajuste)).abs()).divide(new BigDecimal(resultActualizar.PlazoEnDiasOperacion), 20,RoundingMode.HALF_UP);
										}else {
											resultActualizar.MontoInteresMonedaInstrumento = BigDecimal.ZERO;
										}
										
									}
								}
									
								if (result.FactorMonedaAdicional != null && resultActualizar.FactorMonedaAdicional != null && result.MontoMonedaAdicional != null &&
									resultActualizar.FactorMonedaAdicional.compareTo(BigDecimal.ONE) == 1 && 
									((resultActualizar.FactorMonedaAdicional.subtract(result.FactorMonedaAdicional)).compareTo(BigDecimal.ZERO) != 0) ){
									// MSH A los intereses se le resta el reajuste de la moneda
									resultActualizar.MontoReajusteMonedaAdicional	= result.MontoMonedaAdicional.multiply( resultActualizar.FactorMonedaAdicional.subtract(result.FactorMonedaAdicional) );
									resultActualizar.MontoInteresMonedaAdicional 	= resultActualizar.MontoInteresMonedaAdicional.subtract(resultActualizar.MontoReajusteMonedaAdicional);									
								}
							}
						};

					}
				}
					
			} else {
				
				// Actualizo el monto valorizado, En las si
				//resultActualizar.MontoMonedaSistema = resultActualizar.MontoInteresAcumuladoAPlazoMonedaSistema != null ? (resultActualizar.MontoMonedaSistema != null ? resultActualizar.MontoMonedaSistema.add(resultActualizar.MontoInteresAcumuladoAPlazoMonedaSistema): resultActualizar.MontoInteresAcumuladoAPlazoMonedaSistema) : resultActualizar.MontoMonedaSistema;
				resultActualizar.MontoMonedaSistema = resultActualizar.MontoMonedaSistema;
				resultActualizar.MontoMonedaInstrumento = resultActualizar.MontoInteresAcumuladoAPlazoMonedaInstrumento != null ? (resultActualizar.MontoMonedaInstrumento != null ? resultActualizar.MontoMonedaInstrumento.add(resultActualizar.MontoInteresAcumuladoAPlazoMonedaInstrumento): resultActualizar.MontoInteresAcumuladoAPlazoMonedaInstrumento) : resultActualizar.MontoMonedaInstrumento;
				resultActualizar.MontoMonedaAdicional = resultActualizar.MontoInteresAcumuladoAPlazoMonedaAdicional != null ? (resultActualizar.MontoMonedaAdicional != null ? resultActualizar.MontoMonedaAdicional.add(resultActualizar.MontoInteresAcumuladoAPlazoMonedaAdicional):resultActualizar.MontoInteresAcumuladoAPlazoMonedaAdicional) : resultActualizar.MontoMonedaAdicional;
				
				// Interes diario
				resultActualizar.MontoInteresMonedaSistema 		= resultActualizar.MontoInteresAPlazoMonedaSistema != null ? resultActualizar.MontoInteresAPlazoMonedaSistema : BigDecimal.ZERO;
				resultActualizar.MontoInteresMonedaInstrumento 	= resultActualizar.MontoInteresAPlazoMonedaInstrumento != null ? resultActualizar.MontoInteresAPlazoMonedaInstrumento : BigDecimal.ZERO;
				resultActualizar.MontoInteresMonedaAdicional 	= resultActualizar.MontoInteresAPlazoMonedaAdicinal != null ? resultActualizar.MontoInteresAPlazoMonedaAdicinal : BigDecimal.ZERO;														
			
				// Interes acumulado
				resultActualizar.MontoInteresAcumuladoMonedaSistema 	= resultActualizar.MontoInteresAcumuladoAPlazoMonedaSistema != null ? resultActualizar.MontoInteresAcumuladoAPlazoMonedaSistema : BigDecimal.ZERO;
				resultActualizar.MontoInteresAcumuladoMonedaInstrumento = resultActualizar.MontoInteresAcumuladoAPlazoMonedaInstrumento != null ? resultActualizar.MontoInteresAcumuladoAPlazoMonedaInstrumento : BigDecimal.ZERO;
				resultActualizar.MontoInteresAcumuladoMonedaAdicional 	= resultActualizar.MontoInteresAcumuladoAPlazoMonedaAdicional != null ? resultActualizar.MontoInteresAcumuladoAPlazoMonedaAdicional : BigDecimal.ZERO;
											
				if (resultActualizar.FactorMonedaInstrumento != null && resultActualizar.FactorMonedaInstrumento.compareTo(BigDecimal.ZERO) != 0 && resultActualizar.FactorMonedaInstrumento.compareTo(BigDecimal.ONE) != 1 ){
					resultActualizar.MontoReajusteAcumuladoMonedaInstrumento = resultActualizar.MontoMonedaInstrumento.divide( (resultActualizar.FactorMonedaInstrumento ), 20,RoundingMode.HALF_UP);
					resultActualizar.MontoReajusteMonedaInstrumento	= resultActualizar.MontoMonedaInstrumento.divide( (resultActualizar.FactorMonedaInstrumento ), 20,RoundingMode.HALF_UP);						
				}
				else {
					resultActualizar.MontoReajusteAcumuladoMonedaInstrumento = BigDecimal.ZERO;
					resultActualizar.MontoReajusteMonedaInstrumento = BigDecimal.ZERO;
				}
				
				if (resultActualizar.FactorMonedaAdicional != null && resultActualizar.FactorMonedaAdicional.compareTo(BigDecimal.ZERO) != 0 && resultActualizar.FactorMonedaAdicional.compareTo(BigDecimal.ONE) != 1 ){
					resultActualizar.MontoReajusteAcumuladoMonedaAdicional	= resultActualizar.MontoMonedaInstrumento.divide( (resultActualizar.FactorMonedaAdicional ), 20,RoundingMode.HALF_UP);
					resultActualizar.MontoReajusteAcumuladoMonedaInstrumento = resultActualizar.MontoMonedaInstrumento.divide( (resultActualizar.FactorMonedaAdicional ), 20,RoundingMode.HALF_UP);						
				}
				else {
					resultActualizar.MontoReajusteAcumuladoMonedaAdicional = BigDecimal.ZERO;
					resultActualizar.MontoReajusteAcumuladoMonedaInstrumento  = BigDecimal.ZERO;
				}
			};
		};
		logger.debug("fin calculo interes diario");
		
		
		return out;
	}
	

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getQAConnection()) {
			try {
				System.out.println("COMIENZA");
				Gson gson = Utils.getGsonBuilder();
				String jsonIngresoRFUSD = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":64,\"p_IdModulo\":\"CUS\",\"p_IdPais\":44,\"p_OrigenProcesamiento\":\"CUSTODIA_UI\",\"p_IdMonedaAdicional\":\"USD\",\"p_Instrumentos\":[{\"IdValorizacion\":1,\"FechaValorizacion\":\"2023-03-28\",\"FechaOperacion\":\"2023-03-28\",\"PlazoEnDiasOperacion\":null,\"IdClasificacionTipoInstrumento\":\"IF\",\"IdTipoInstrumento\":\"PAG\",\"IdInstrumento\":2223,\"FechaVencimiento\":\"2023-03-28\",\"FechaEmision\":\"0001-01-01 00:00:00.000\",\"IdMonedaInstrumento\":\"USD\",\"IdTipoReajusteInstrumento\":\"USD\",\"Cantidad\":null,\"ValorRescate\":2001.72,\"PrecioCompra\":null,\"TasaCompra\":3.1,\"MontoCompra\":null,\"PrecioSubyacente\":null,\"TasaSubyacente\":null,\"PrecioGarantia\":null,\"TasaGarantia\":null,\"TasaTIF\":null,\"IdPortafolio\":null,\"TablaOrigen\":\"CARTERA\",\"IdUso\":null,\"TipoCartera\":null,\"IdTipoCriterioTasa\":null,\"Nemotecnico\":\"PAGARE R\",\"IdMonedaEmisionInstrumento\":\"USD\",\"Emisor\":\"AGUAS MANQ\",\"IdCondicionDeLiquidacion\":\"T0\"}]}";
				ValorizarInstrumentoEntrada Objin = gson.fromJson(jsonIngresoRFUSD, ValorizarInstrumentoEntrada.class);
								
				ValorizarInstrumento  kk = new ValorizarInstrumento();
				ValorizarInstrumentoSalida asdf = kk.logica(con, Objin);
				con.rollback();
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
		    	}		    	
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("TERMINA");
	};
}

