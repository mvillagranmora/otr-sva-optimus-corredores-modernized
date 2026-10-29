package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.custodia.CalcularCoberturaWrapper.CalcularCoberturaWrapperEntrada;
import cl.bcs.custodia.CalcularCoberturaWrapper.CalcularCoberturaWrapperSalida;
import cl.bcs.custodia.CalcularCoberturaWrapper.ObjCalcularCobertura;
import cl.bcs.custodia.CalcularCoberturaWrapper.ObjTableCoberturaSimulacion;

import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodia;
import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut;
import cl.bcs.plataforma.pla.ObtenerPlazo.InObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.tas.EntregarPreciosTasasUI;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasasUI.EntregarPreciosTasasUI_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasasUI.EntregarPreciosTasasUI_Entrada_DatosInstrumentos;

public class ConsultarCustodiaParaGarantia implements JavaCall {

	private final Log log = new Log("CUS.ConsultarCustodiaParaGarantia");
	private final String SQL_ConsultarInstrumento = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaParaGarantia_ConsultaInstrumento.sql");
	private final String SQL_ConsultarInstrumentoGarantizable = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaParaGarantia_ConsultaInstrumentoGarantizable.sql");
	private final String SQL_ConsultarInstrumentoReportable = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaParaGarantia_ConsultaInstrumentoReportable.sql");
	private final String SQL_ConsultarInstrumentosSubyacente = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaParaGarantia_ConsultaInstrumentosSubyacentes.sql");
	
	public static class ObjConsultaInstrumento{
		public Long					IdInstrumento;
		public String 				Instrumento;
		public String				ISIN;
		public BigDecimal			CorteMinimo;
		public String				EstaInscrito;
		public String				EsDivisible;
		public String				RazonSocial;
		public String				Lista;
		public java.sql.Date		FechaEmision;
		public java.sql.Date		FechaVencimiento;
		public Long					IdInstrumentoRelacionado;
		public String				IdEmisor;
		public String				IdTipoReajuste;
		public String				Reajustable;
		public String				IdMoneda;
		public String				IdEstado;
		public Integer				DecimalesPrecio;
		public String 				IdTipoInstrumento;
		public String 				CodigoTipoInstrumento;
		public String 				NombreTipoInstrumento;
		public String   			IdClasificacionTipoInstrumento;
		public String  				CodigoClasificacionTipoInstrumento;
		public String  				NombreClasificacionTipoInstrumento;
		public String				DocumentoIdentidadEmisor;
		public String				NombreCompletoEmisor;
		public String				IdTipoOperacion;
		public String				Accion;
		public String				NombreTipoOperacion;
		public Long					Tamano;
		public String				IdEntidadClasificadoraRiesgo;
		public String				ValorClasificacionRiesgo;
		public String				IdTipoClasificacionRiesgo;
		public String				TipoValorizacion;
		public BigDecimal			Tasa;
		public BigDecimal			Precio;
		public String				ValorizaPor;
		public String				TieneCupon;
		public Integer				NumeroCupones;
		public String				IndicadorBolson;
		public String				EsGarantizable;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class ObjTipoInstrumentoDerivados{
		public String					IdClasificacionTipoInstrumento;
		public String 				IdTipoInstrumento;
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ConsultarCustodiaParaGarantiaEntrada {
		public Long					p_IdEmpresa; 
		public Long					p_IdUsuario;
		public String				p_IdModulo;
		public String				p_UsaReglasNegocio;
		public Long					p_IdPais;
		public Integer				p_PlazoDias;
		public Long					p_IdCliente; 
		public Long					p_IdPortafolio;
		public String				p_IdTipoOperacion;
		public String				p_IdTipoInstrumentoAGarantizar;
		public Long					p_IdInstrumentoAGarantizar;
		public String				p_IdTipoInstrumentoBusqueda;
		public Long					p_IdInstrumentoBusqueda;
		public Long					p_IdEntidadCobertura;
		public String				p_ValorizarFlujo;
		public String				p_IdBolsa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class Obj_p_out_CustodiaGarantia {
		public Long 			IdInstrumento;
		public String			Instrumento;
		public String			ISIN;
		public String			IdTipoInstrumento;
		public String			NombreTipoInstrumento;
		public String			IdClasificacionTipoInstrumento;
		public BigDecimal		ValorNominal;
		public BigDecimal		CantidadDisponibleGarantia;
		public BigDecimal		MontoDisponibleGarantia;
		public BigDecimal		MontoDisponibleMonedaLocalGarantia;
		public BigDecimal		PrecioMercado;
		public BigDecimal		TasaMercado;
		public BigDecimal		MontoAMercado;
		public BigDecimal		PrecioVector;
		public BigDecimal		MontoVector;
		public String 			IdMonedaInstrumento;
		public String 			IdMonedaEmisionInstrumento;
		public Date				FechaLiquidacion;
		public String			IdEmisor;
		public Date				FechaEmisionInstrumento;
		public Date				FechaVencimientoInstrumento;
		public BigDecimal		CorteMinimo;
		public String			IdTipoReajuste;
		public Long				PlazoEnDias;
		public String			EsFungible;
		public BigDecimal		PorcentajeCoberturaEntidad;
		public BigDecimal		MontoCoberturaEntidad;
		public BigDecimal		PorcentajeCoberturaCorredora;
		public BigDecimal		MontoCoberturaCorredora;
		public BigDecimal		MontoCobertura;
		public BigDecimal		Precio;
		public BigDecimal		PrecioVaR;
		public String			ValorClasificacionRiesgo;
		public BigDecimal		CantidadAIngresar;
		public BigDecimal		MontoAGarantizar;
		public BigDecimal		ValorRescate;
		public BigDecimal		ValorRescateDisponibleGarantia;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class Obj_p_out_InformacionDetalle {
		public String	Codigo;
		public String	Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class Obj_p_out_ErroresDetalle {
		public String	Codigo;
		public String	Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class Obj_p_out_AdvertenciasDetalle {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class TipoInstrumento {
		public String IdTipoInstrumento;
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ConsultarCustodiaParaGarantiaSalida {
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public List<Obj_p_out_CustodiaGarantia> 	p_out_CustodiaGarantia;
		public Long									p_out_InformacionContador;
		public List<Obj_p_out_InformacionDetalle>	p_out_InformacionDetalle;
		public Long									p_out_AdvertenciasContador;
		public List<Obj_p_out_AdvertenciasDetalle>	p_out_AdvertenciasDetalle;
		public Long									p_out_ErroresContador;
		public List<Obj_p_out_ErroresDetalle>		p_out_ErroresDetalle;
		
		public ConsultarCustodiaParaGarantiaSalida (){
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			p_out_CustodiaGarantia		= new ArrayList<Obj_p_out_CustodiaGarantia>();
			p_out_InformacionDetalle	= new ArrayList<Obj_p_out_InformacionDetalle>();
			p_out_AdvertenciasDetalle	= new ArrayList<Obj_p_out_AdvertenciasDetalle>();
			p_out_ErroresDetalle		= new ArrayList<Obj_p_out_ErroresDetalle>();
			p_out_ErroresContador 		= 0L;
			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	};
	
	public static class listaInstrumentoProcesado {
		public Long 			IdReferencial;
		public Long 			IdInstrumento;
		public String 			Nemotecnico;
		public String 			NemoSVS;
		public BigDecimal		CorteMinimo;
		public String 			IdTipoInstrumento; // L = 3
		public String 			IdClasificacionTipoInstrumento; // L = 3
		public BigDecimal		Cantidad = new BigDecimal(0L);
		//public BigDecimal 		MontoACompra = new BigDecimal(0L);
		//public BigDecimal 		MontoACompraMonedaLocal = new BigDecimal(0L);
		public BigDecimal 		ValorRescate = new BigDecimal(0L);
		public BigDecimal 		PrecioMercado = new BigDecimal(0L);
		public BigDecimal 		TasaMercado = new BigDecimal(0L);
		public BigDecimal 		MontoAMercadoMonedaSistema = new BigDecimal(0L);
		public BigDecimal 		MontoAMercadoMonedaInstrumento = new BigDecimal(0L);		
		//public BigDecimal 		PrecioVector = new BigDecimal(0L);
		//public BigDecimal 		MontoVector = new BigDecimal(0L);
		public String 			IdMonedaInstrumento;
		public String 			IdMonedaEmisionInstrumento;
		public java.sql.Date 	FechaLiquidacionInstrumento;
		public String 			IdEmisor; // L = 40
		public java.sql.Date 	FechaEmisionInstrumento;
		public java.sql.Date 	FechaVencimientoInstrumento;
		public Long 			PlazoEnDias;
		public String 			IdTipoReajuste; // L = 3
		public String 			EsFungible; // L = 1
		public String 			IdTipoInstrumentoGarantizado; // L = 3
		public String 			IdBolsa; // L = 3
		public Long 			PlazoEnDías; // L = 3
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public ConsultarCustodiaParaGarantiaSalida logica(Connection con, ConsultarCustodiaParaGarantiaEntrada in) throws Exception {		
		ConsultarCustodiaParaGarantiaSalida out = new ConsultarCustodiaParaGarantiaSalida();
		SQL sql=new SQL();
		
		CalcularCoberturaWrapper calculoCobertura = new CalcularCoberturaWrapper();
		try {			
			
			//******************************************************************************************************************
			//												VALIDACIONES INICIALES
			//******************************************************************************************************************			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();			
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			List<listaInstrumentoProcesado> instrumentoProcesado = new ArrayList<listaInstrumentoProcesado>();
			List<ObjTableCoberturaSimulacion> registroCalcCobertura = new ArrayList<ObjTableCoberturaSimulacion>();
			
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			};
		
			if (out.P_OUT_MENSAJES.size() > 0){
				return out;
			};
			
			//******************************************************************************************************************
			//													LOGICA
			//******************************************************************************************************************			
			// obtener fecha del sistema
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String pais = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");

			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
			
			//Llamado a Flujo de Custodia
			List<ConsultarFlujoCustodia.FlujoUso> dato_p_uso = new ArrayList<ConsultarFlujoCustodia.FlujoUso>();

			ConsultarFlujoCustodia.FlujoUso datosFlujoUso = new ConsultarFlujoCustodia.FlujoUso();
			datosFlujoUso.UsoTipoOperacion				= in.p_IdTipoOperacion;
			datosFlujoUso.IdCliente						= in.p_IdCliente;
			datosFlujoUso.IdPortafolio					= in.p_IdPortafolio;
			datosFlujoUso.BusquedaEspecifica			= "SOLO_GARANTIZABLE";
			datosFlujoUso.IdTipoInstrumentoEspecifico	= in.p_IdTipoInstrumentoAGarantizar;
			dato_p_uso.add(datosFlujoUso);
			
			List<ConsultarFlujoCustodia.FlujoFiltrosBusqueda> dato_p_FiltrosBusqueda = new ArrayList<ConsultarFlujoCustodia.FlujoFiltrosBusqueda>();
			ConsultarFlujoCustodia.FlujoFiltrosBusqueda datosFlujoFiltrosBusqueda = new ConsultarFlujoCustodia.FlujoFiltrosBusqueda();
			datosFlujoFiltrosBusqueda.IdClasificacionTipoInstrumento	= null;
			datosFlujoFiltrosBusqueda.IdTipoInstrumento					= in.p_IdTipoInstrumentoBusqueda == null ? null : in.p_IdTipoInstrumentoBusqueda;
			datosFlujoFiltrosBusqueda.IdInstrumento						= in.p_IdInstrumentoBusqueda == null ? null : in.p_IdInstrumentoBusqueda;
			datosFlujoFiltrosBusqueda.IdEmisor							= null;
			datosFlujoFiltrosBusqueda.FechaVencimiento					= null;
			datosFlujoFiltrosBusqueda.IdTipoReajuste					= null;
			datosFlujoFiltrosBusqueda.EsFungible						= null;
			datosFlujoFiltrosBusqueda.IdBoveda							= null;
			dato_p_FiltrosBusqueda.add(datosFlujoFiltrosBusqueda);
			
			List<ConsultarFlujoCustodia.FlujoEsAgrupadoPor> dato_p_EsAgrupadoPor = new ArrayList<ConsultarFlujoCustodia.FlujoEsAgrupadoPor>();
			ConsultarFlujoCustodia.FlujoEsAgrupadoPor datosFlujoEsAgrupadoPor = new ConsultarFlujoCustodia.FlujoEsAgrupadoPor();
			datosFlujoEsAgrupadoPor.ClasificacionTipoInstrumento	= "S";
			datosFlujoEsAgrupadoPor.TipoInstrumento					= "S";
			datosFlujoEsAgrupadoPor.Instrumento						= "S";
			datosFlujoEsAgrupadoPor.Emisor							= "S";
			datosFlujoEsAgrupadoPor.Nemotecnico						= "S";
			datosFlujoEsAgrupadoPor.NemoSVS							= "S";
			datosFlujoEsAgrupadoPor.FechaEmisionInstrumento			= "S";
			datosFlujoEsAgrupadoPor.FechaVencimientoInstrumento		= "S";
			datosFlujoEsAgrupadoPor.FechaLiquidacionInstrumento		= "S";
			datosFlujoEsAgrupadoPor.TipoReajuste					= "S";
			datosFlujoEsAgrupadoPor.EsFungible						= "S";
			datosFlujoEsAgrupadoPor.CodigoIFRS						= null;
			datosFlujoEsAgrupadoPor.Boveda							= null;
			datosFlujoEsAgrupadoPor.Bolsa							= "S";
			datosFlujoEsAgrupadoPor.TipoOperacion					= null;
			datosFlujoEsAgrupadoPor.Mesa							= null;
			datosFlujoEsAgrupadoPor.FolioRegistro					= null;
			datosFlujoEsAgrupadoPor.ItemOrigen						= null;
			dato_p_EsAgrupadoPor.add(datosFlujoEsAgrupadoPor);

			
			ConsultarFlujoCustodia llamadoFlujoCustodia = new ConsultarFlujoCustodia();
			ConsultarFlujoCustodia.FlujoCustodiaEntrada paramInSP = new ConsultarFlujoCustodia.FlujoCustodiaEntrada(); 
			paramInSP.p_IdEmpresa			= in.p_IdEmpresa;
			paramInSP.p_IdUsuario			= in.p_IdUsuario;
			paramInSP.p_IdModulo			= in.p_IdModulo;
			paramInSP.p_FechaSaldo			= fechaSistema;
			paramInSP.p_IdPais				= in.p_IdPais;
			paramInSP.p_DiasProyeccion		= in.p_PlazoDias;
			paramInSP.p_DiaHabil			= "S";
			paramInSP.p_ValorizarFlujo		= in.p_ValorizarFlujo;
			paramInSP.p_Uso					= dato_p_uso;
			paramInSP.p_FiltrosBusqueda		= dato_p_FiltrosBusqueda;
			paramInSP.p_EsAgrupadoPor		= dato_p_EsAgrupadoPor;

			ConsultarFlujoCustodia.FlujoCustodiaSalida salidaFlujo = llamadoFlujoCustodia.logica(con, paramInSP);

			if (salidaFlujo.p_out_FlujoTotalizado != null){
//			if (salidaFlujo.P_OUT_ERRORESCONTADOR == null || salidaFlujo.P_OUT_ERRORESCONTADOR.equals(0L) ){
				
				ArrayList<Long> instProc = new ArrayList<>();
				CalcularCoberturaWrapperEntrada SimulacionCobertura = new CalcularCoberturaWrapperEntrada(); 
				
				Long NumeroReferencia = 0L;
				for (FlujoCustodia  registroFT : salidaFlujo.p_out_FlujoTotalizado ){
					if ("DV".equals(registroFT.IdClasificacionTipoInstrumento)) continue;
					if ((registroFT.Cantidad.compareTo(BigDecimal.ZERO) > 0) && (registroFT.FechaLiquidacionInstrumento.compareTo(Fecha.toDate(fechaSistema))==-1 || registroFT.FechaLiquidacionInstrumento.compareTo(Fecha.toDate(fechaSistema))==0)){
						if (!instProc.contains(registroFT.IdInstrumento) || "IF".equals(registroFT.IdClasificacionTipoInstrumento)) {
							listaInstrumentoProcesado instrumentoProc = new listaInstrumentoProcesado();
							Utils.copyPojo( registroFT, instrumentoProc);
							instrumentoProc.IdReferencial = NumeroReferencia ++;
							instrumentoProcesado.add(instrumentoProc);
							instProc.add(registroFT.IdInstrumento);
						} else {
							int posicion = instProc.indexOf(registroFT.IdInstrumento);
							instrumentoProcesado.get(posicion).Cantidad							= instrumentoProcesado.get(posicion).Cantidad.add(registroFT.Cantidad);
							instrumentoProcesado.get(posicion).ValorRescate						= instrumentoProcesado.get(posicion).ValorRescate.add(registroFT.ValorRescate);
							instrumentoProcesado.get(posicion).MontoAMercadoMonedaSistema		= instrumentoProcesado.get(posicion).MontoAMercadoMonedaSistema != null && registroFT.MontoAMercadoMonedaSistema != null ? instrumentoProcesado.get(posicion).MontoAMercadoMonedaSistema.add(registroFT.MontoAMercadoMonedaSistema) : BigDecimal.ZERO;
							instrumentoProcesado.get(posicion).MontoAMercadoMonedaInstrumento	= instrumentoProcesado.get(posicion).MontoAMercadoMonedaInstrumento != null && registroFT.MontoAMercadoMonedaInstrumento != null ? instrumentoProcesado.get(posicion).MontoAMercadoMonedaInstrumento.add(registroFT.MontoAMercadoMonedaInstrumento) : BigDecimal.ZERO;
						}
					}
				};
				
				//**********************************************************************************
				//                            CALCULO DE COBERTURAS
				//**********************************************************************************				
				if (in.p_IdTipoInstrumentoAGarantizar != null && in.p_IdInstrumentoAGarantizar != null){
					SimulacionCobertura.p_IdEmpresa = in.p_IdEmpresa;
					SimulacionCobertura.p_IdUsuario = in.p_IdUsuario;
					SimulacionCobertura.p_IdModulo = in.p_IdModulo;
					SimulacionCobertura.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
					SimulacionCobertura.p_IdPortafolio = in.p_IdPortafolio;
					SimulacionCobertura.p_IdEntidadDestinoGarantia = in.p_IdEntidadCobertura;
					SimulacionCobertura.p_IdTipoInstrumentoExigeGarantia = in.p_IdTipoInstrumentoAGarantizar;
					SimulacionCobertura.p_IdInstrumentoExigeGarantia = in.p_IdInstrumentoAGarantizar;
					for (listaInstrumentoProcesado ForSimulacionCobertura : instrumentoProcesado){
						ObjCalcularCobertura parametroCobertura = new ObjCalcularCobertura();
						Utils.copyPojo( ForSimulacionCobertura, parametroCobertura);

						parametroCobertura.PrecioMercado = ForSimulacionCobertura.PrecioMercado;
		            	parametroCobertura.Monto 		 = new BigDecimal(0);
						SimulacionCobertura.p_Simulacion.add(parametroCobertura);
					}
					// LLAMAMOS AL WRAPPER DE COBERTURAS
					CalcularCoberturaWrapperSalida resultadoCalculoCobertura = calculoCobertura.logica(con, SimulacionCobertura);
					registroCalcCobertura = resultadoCalculoCobertura.p_out_TableCoberturaSimulacion;
				}
				
				
				
				//**********************************************************************************
				//                            SE PREPARA SALIDA
				//**********************************************************************************
				HashMap<String,Object> paramsInstrumento = new HashMap<>();
				long contadorOcurrencia = 0L;
				if (pais.equals("PE")) {
					List<TipoInstrumento> idsTipoInstrumento = sql.query(con, SQL_ConsultarInstrumentosSubyacente, paramsInstrumento, TipoInstrumento.class);
					contadorOcurrencia = idsTipoInstrumento.stream().filter(s -> s.IdTipoInstrumento.equals(in.p_IdTipoInstrumentoAGarantizar)).count();
				}
				List<Long> IdInstrumento = new ArrayList<Long>();
				for (listaInstrumentoProcesado ConsInstrumento: instrumentoProcesado){
					IdInstrumento.add( ConsInstrumento.IdInstrumento );
				};
				paramsInstrumento.put("IdInstrumento", IdInstrumento);
				List<ObjConsultaInstrumento> INS;
								
				if (contadorOcurrencia > 0) {
					INS = sql.query(con, SQL_ConsultarInstrumentoReportable, paramsInstrumento, ObjConsultaInstrumento.class);
				} else if (in.p_IdTipoInstrumentoAGarantizar == null){
					INS = sql.query(con, SQL_ConsultarInstrumento, paramsInstrumento, ObjConsultaInstrumento.class);
				} else {
					paramsInstrumento.put("IdBolsa", in.p_IdBolsa);
					paramsInstrumento.put("IdTipoInstrumento", in.p_IdTipoInstrumentoAGarantizar);
					INS = sql.query(con, SQL_ConsultarInstrumentoGarantizable, paramsInstrumento, ObjConsultaInstrumento.class);
				}
				HashMap<Long,Object> Instrumentos = Utils.List2Hash(INS, "IdInstrumento");
				
				//Se obtiene Precio Var
				EntregarPreciosTasasUI consultarPrecioVector = new EntregarPreciosTasasUI();
				EntregarPreciosTasasUI_Entrada entrada_consultarPrecioVector = new EntregarPreciosTasasUI_Entrada();
				
				entrada_consultarPrecioVector.p_IdEmpresa =  in.p_IdEmpresa;
				entrada_consultarPrecioVector.p_IdModulo = "CUS";
				entrada_consultarPrecioVector.p_IdPais = empresa.IdPais;
				entrada_consultarPrecioVector.p_IdUsuario = in.p_IdUsuario;
				
				for ( ObjConsultaInstrumento resp : INS){
					EntregarPreciosTasasUI_Entrada_DatosInstrumentos entradaPrecioOrden = new EntregarPreciosTasasUI_Entrada_DatosInstrumentos();
					entradaPrecioOrden.FechaConsulta = fechaSistema;
					entradaPrecioOrden.IdRegistro = 0L;
					entradaPrecioOrden.TipoCartera = "0";
					entradaPrecioOrden.IdUso = "ORDEN";
					entradaPrecioOrden.IdTipoCriterioTasa = "0";
					entradaPrecioOrden.IdClasificacionTipoInstrumento = resp.IdClasificacionTipoInstrumento;
					entradaPrecioOrden.IdTipoInstrumento = resp.IdTipoInstrumento;
					entradaPrecioOrden.Nemotecnico = resp.Instrumento;
					entradaPrecioOrden.IdInstrumento = resp.IdInstrumento;
					entradaPrecioOrden.IdMoneda = resp.IdMoneda;
					entradaPrecioOrden.FechaConsulta = fechaSistema;
					entradaPrecioOrden.PlazoResidual = 0L;
					entradaPrecioOrden.IdTrabajo = 0L;	
					entrada_consultarPrecioVector.p_DatosInstrumentos.add(entradaPrecioOrden);
					
					EntregarPreciosTasasUI_Entrada_DatosInstrumentos entradaPrecioGarantia = new EntregarPreciosTasasUI_Entrada_DatosInstrumentos();
					entradaPrecioGarantia.FechaConsulta = fechaSistema;
					entradaPrecioGarantia.IdRegistro = 1L;
					entradaPrecioGarantia.TipoCartera = "0";
					entradaPrecioGarantia.IdUso = "GARANTIA";
					entradaPrecioGarantia.IdTipoCriterioTasa = "0";
					entradaPrecioGarantia.IdClasificacionTipoInstrumento = resp.IdClasificacionTipoInstrumento;
					entradaPrecioGarantia.IdTipoInstrumento = resp.IdTipoInstrumento;
					entradaPrecioGarantia.Nemotecnico = resp.Instrumento;
					entradaPrecioGarantia.IdInstrumento = resp.IdInstrumento;
					entradaPrecioGarantia.IdMoneda = resp.IdMoneda;
					entradaPrecioGarantia.FechaConsulta = fechaSistema;
					entradaPrecioGarantia.PlazoResidual = 0L;
					entradaPrecioGarantia.IdTrabajo = 1L;	
					entrada_consultarPrecioVector.p_DatosInstrumentos.add(entradaPrecioGarantia);
				}
				
				
				EntregarPreciosTasas_Salida salidaPrecio = consultarPrecioVector.logica(con, entrada_consultarPrecioVector);
				HashMap<String, Object> hashDatosPreciosVar = new HashMap<String, Object>();
				
				List<MensajeSalida> mensajesErrores = salidaPrecio.P_OUT_MENSAJES.parallelStream().filter(mensaje->MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).collect(Collectors.toList());
				out.P_OUT_MENSAJES.addAll(mensajesErrores);
				out.p_out_ErroresContador = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				
				
				if (out.p_out_ErroresContador == 0) {
					for (EntregarPreciosTasas_Salida_PreciosTasas sal: salidaPrecio.P_OUT_PRECIOSTASAS) {
						if (sal.IdRegistro == 1L) {
							 hashDatosPreciosVar.put(sal.IdInstrumento.toString(), sal);
						 }
					}
				
				//Fin precio var
	
					for ( listaInstrumentoProcesado PrepararSalida : instrumentoProcesado){
						Obj_p_out_CustodiaGarantia RegistroProcesado = new Obj_p_out_CustodiaGarantia();										
						ObjConsultaInstrumento Instrumento = (ObjConsultaInstrumento) Instrumentos.get(PrepararSalida.IdInstrumento);
						if(Instrumento != null && ("S".equalsIgnoreCase(Instrumento.EsGarantizable) || in.p_IdInstrumentoAGarantizar == null) && !(Instrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("DV") || Instrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("FM"))) {
							EntregarPreciosTasas_Salida_PreciosTasas infoPrecioVar = (EntregarPreciosTasas_Salida_PreciosTasas) hashDatosPreciosVar.get(PrepararSalida.IdInstrumento.toString());
							Utils.copyPojo(PrepararSalida, RegistroProcesado);
							
							RegistroProcesado.FechaEmisionInstrumento 			= PrepararSalida.FechaEmisionInstrumento;
							RegistroProcesado.FechaVencimientoInstrumento 		= PrepararSalida.FechaVencimientoInstrumento;
							RegistroProcesado.FechaLiquidacion 					= PrepararSalida.FechaLiquidacionInstrumento;									
							RegistroProcesado.CantidadDisponibleGarantia 		= "IF".equalsIgnoreCase(Instrumento.IdClasificacionTipoInstrumento) ? BigDecimal.ZERO : PrepararSalida.Cantidad;
							RegistroProcesado.ValorRescateDisponibleGarantia	= "IF".equalsIgnoreCase(Instrumento.IdClasificacionTipoInstrumento) ? PrepararSalida.ValorRescate : BigDecimal.ZERO;
							RegistroProcesado.Precio							= PrepararSalida.PrecioMercado;
							RegistroProcesado.PrecioVaR							= infoPrecioVar.Precio;
							RegistroProcesado.MontoDisponibleGarantia 			= PrepararSalida.MontoAMercadoMonedaInstrumento;
							RegistroProcesado.MontoDisponibleMonedaLocalGarantia= PrepararSalida.MontoAMercadoMonedaSistema;
							
							RegistroProcesado.NombreTipoInstrumento 			= Instrumento.NombreTipoInstrumento;
							RegistroProcesado.ISIN 								= Instrumento.ISIN;
							RegistroProcesado.CorteMinimo 						= PrepararSalida.CorteMinimo;
							RegistroProcesado.ValorClasificacionRiesgo 			= Instrumento.ValorClasificacionRiesgo;
							RegistroProcesado.IdClasificacionTipoInstrumento 	= Instrumento.IdClasificacionTipoInstrumento;
						
							
							{	
								InObtenerPlazo entradaObtenerPlazo = new InObtenerPlazo();
								entradaObtenerPlazo.p_IdPais = empresa.IdPais;
								
								ObtenerPlazo.FechasIn f = new ObtenerPlazo.FechasIn();
								f.FechaDesde 	= Fecha.toStringDate(PrepararSalida.FechaEmisionInstrumento);
								f.FechaHasta 	= Fecha.toStringDate(PrepararSalida.FechaVencimientoInstrumento);
								f.IdInterno 	= PrepararSalida.IdReferencial;
								f.SoloDiasHabiles = "N";
								entradaObtenerPlazo.p_Fechas.add(f);
								
								if (entradaObtenerPlazo.p_Fechas.size() > 0) {
									ObtenerPlazo serviceObtenerPlazo = new ObtenerPlazo();
									OutObtenerPlazo salidaObtenerPlazo = serviceObtenerPlazo.logica(con, entradaObtenerPlazo);
									List<FechasOut> Plazos = salidaObtenerPlazo.p_out_Fechas;
									RegistroProcesado.PlazoEnDias = Plazos.size() > 0? Plazos.get(0).Plazo : 0L;
								}
							}
							
							if (Instrumento.CorteMinimo == null){
								RegistroProcesado.ValorNominal = PrepararSalida.Cantidad;
							}else{
								if (Instrumento.CorteMinimo.compareTo(BigDecimal.ZERO) == 0){
									RegistroProcesado.ValorNominal = PrepararSalida.Cantidad;
								}else{
									if (PrepararSalida.Cantidad.equals(0L)){
										PrepararSalida.Cantidad = BigDecimal.ONE;
									}
									RegistroProcesado.ValorNominal = (Instrumento.CorteMinimo.multiply(PrepararSalida.Cantidad));
								}
							};
							
							
							for (ObjTableCoberturaSimulacion RegistroCobertura : registroCalcCobertura){
								if (RegistroCobertura.IdReferencial.equals(PrepararSalida.IdReferencial)){
									RegistroProcesado.PorcentajeCoberturaEntidad 	= RegistroCobertura.PorcentajeCoberturaEntidad;
									RegistroProcesado.MontoCoberturaEntidad 		= RegistroCobertura.MontoCoberturaEntidad;
									RegistroProcesado.PorcentajeCoberturaCorredora	= RegistroCobertura.PorcentajeCoberturaCorredora;
									RegistroProcesado.MontoCoberturaCorredora 		= RegistroCobertura.MontoCoberturaCorredora;
									if (RegistroProcesado.MontoCoberturaCorredora.compareTo(RegistroProcesado.MontoCoberturaEntidad) > 0 ){
										RegistroProcesado.MontoCobertura = RegistroCobertura.PorcentajeCoberturaCorredora;
									}else{
										RegistroProcesado.MontoCobertura = RegistroCobertura.MontoCoberturaEntidad;
									}
								}
							}
							
							if (Instrumento.IdClasificacionTipoInstrumento.equals("IF")){
								RegistroProcesado.Instrumento = PrepararSalida.NemoSVS;
							}else{
								RegistroProcesado.Instrumento = PrepararSalida.Nemotecnico;
							}
							
							RegistroProcesado.CantidadAIngresar	= BigDecimal.ZERO;
							RegistroProcesado.MontoAGarantizar 	= BigDecimal.ZERO;
							out.p_out_CustodiaGarantia.add(RegistroProcesado);
						}
					};
					//log.write(out.P_OUT_MENSAJES);
					
				} // FIN if (salidaFlujo.P_OUT_ERRORESCONTADOR == null || salidaFlujo.P_OUT_ERRORESCONTADOR.equals(0L) ){
			
			}
			
			for (MensajeSalida  AdveError: salidaFlujo.P_OUT_MENSAJES){
				out.P_OUT_MENSAJES.add(AdveError);
			};								
			out.p_out_ErroresContador 	= MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);			
			log.write(out.P_OUT_MENSAJES);
			log.write(instrumentoProcesado);
		}
		finally {
			//algo
		}
		// SALIDA
		log.write("SALIDA CONSULTARFLUJOCUSTODIA : " + out);
		return out;
	};

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConsultarCustodiaParaGarantiaEntrada jin=gson.fromJson(in.toString() ,ConsultarCustodiaParaGarantiaEntrada.class);
		ConsultarCustodiaParaGarantiaSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	// MAIN
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdPais\":44,\"p_PlazoDias\":0,\"p_IdCliente\":123569,\"p_IdPortafolio\":27499,\"p_IdTipoOperacion\":\"INGGTA\",\"p_IdTipoInstrumentoAGarantizar\":\"DER\",\"p_IdInstrumentoAGarantizar\":null,\"p_IdEntidadCobertura\":1,\"p_ValorizarFlujo\":\"S\",\"p_IdBolsa\":\"BCS\"}";
				//String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdPais\":44,\"p_PlazoDias\":0,\"p_IdCliente\":123569,\"p_IdPortafolio\":27499,\"p_IdTipoOperacion\":\"INGGPR\",\"p_IdTipoInstrumentoAGarantizar\":null,\"p_IdInstrumentoAGarantizar\":null,\"p_IdEntidadCobertura\":1,\"p_ValorizarFlujo\":\"N\"}";
				ConsultarCustodiaParaGarantiaEntrada Objin = gson.fromJson(jsonDesa, ConsultarCustodiaParaGarantiaEntrada.class);
								
				ConsultarCustodiaParaGarantia  kk = new ConsultarCustodiaParaGarantia();
				ConsultarCustodiaParaGarantiaSalida Objout = kk.logica(con, Objin);
				System.out.println(Objout);
				con.rollback();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
	
};