package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
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

import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.EntradaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.InCarteras;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.InInstrumento;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo.EntradaConsultarValorDolarFondoMutuo;
import cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo.SalidaConsultarValorDolarFondoMutuo;
import cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo.SerieFondoParidad;
// Rescate 
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
// Reinversion APV
import cl.bcs.fondomutuo.CrearSolicitudReinversionPorCliente.EntradaCrearSolicitudReinversionPorCliente;
import cl.bcs.fondomutuo.CrearSolicitudReinversionPorCliente.InSolicitudAporteCliente; // AKI modificar?
import cl.bcs.fondomutuo.CrearSolicitudReinversionPorCliente.SalidaCrearSolicitudReinversionPorCliente;
// Rescate APV
import cl.bcs.fondomutuo.CrearSolicitudRescateNormalPorCliente.EntradaCrearSolicitudRescateNormalPorCliente;
import cl.bcs.fondomutuo.CrearSolicitudRescateNormalPorCliente.InSolicitudRescateCliente;
import cl.bcs.fondomutuo.CrearSolicitudRescateNormalPorCliente.SalidaCrearSolicitudRescateNormalPorCliente;
// Reinversion NO APV

import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.InSolicitudesAporte;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.InSolicitudesRescate;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.GrupoRegimen;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion1N.EntradaCrearSolicitudRescateReinversion1N;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion1N.SalidaCrearSolicitudRescateReinversion1N;

//Resultado
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen.EntradaBuscarCantidadesPorRegimen;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen.InClientes;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen.SalidaBuscarCantidadesPorRegimen;
import cl.bcs.fondomutuo.utils.FondoMutuoUtils;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen.ClienteGrupoRegimenFondo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.portafolio.IngresaPreferenciaPagoPortafolio;
//Preferencia de Pago Portafolio
import cl.bcs.portafolio.IngresaPreferenciaPagoPortafolio.IngresaPreferenciaPagoPortafolio_Entrada;
import cl.bcs.portafolio.IngresaPreferenciaPagoPortafolio.IngresaPreferenciaPagoPortafolio_Salida;
import cl.bcs.portafolio.IngresaPreferenciaPagoPortafolio.PreferenciaPagoPortafolio;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarReinversionRescate implements JavaCall
{
	private final Logger logger = LogManager.getLogger(ProcesarReinversionRescate.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO 			= loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarReinversionRescate_SelectPreferenciaPagoPortafolio.sql");
	private final String SQL_SELECT_CONSULTA_PREFERENCIA_PAGO_PORTAFOLIO	= loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarReinversionRescate_ConsultaPreferenciaPagoPortafolio.sql");
	private final String SQL_SELECT_CONSULTA_PREFERENCIA_PAGO				= loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarReinversionRescate_ConsultaPreferenciaPago.sql");
	private final String SQL_SELECT_PORTAFOLIOS								= loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarReinversionRescate_SelectPortafolio.sql");
	private final String SQL_SELECT_SERIE_FONDO 						= loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_GRUPO_REGIMEN_SERIE_FONDO 				= loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectGrupoRegimenSerieDeFondo.sql");
	private final String SQL_SELECT_REGIMEN				= loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarReinversionRescate_SelectRegimen.sql");
	private final String SQL_SELECT_CONSULTACUENTABANCARIA	= loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarReinversionRescate_ConsultarCuentaBancaria.sql");
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	// Entrada que permite recibir cualquier tipo de operacion (Rescate, Reinversion APV y No APV)
	public static class EntradaProcesarReinversionRescate extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_ClaveUsuario;
		public String p_TipoProceso; //RC: Rescate, RCA: Rescate APV, RE: Reinversion, REA: Reinversion APV
		public List<InEntradaReinversion> p_EntradaReinversion;
		public List<InEntradaReinversionAPV> p_EntradaReinversionAPV;
		public List<InEntradaRescate> p_EntradaRescate;
		public List<InEntradaRescateAPV> p_EntradaRescateAPV;
		
				
		public EntradaProcesarReinversionRescate() {
			p_EntradaReinversion = new ArrayList<InEntradaReinversion>();
			p_EntradaReinversionAPV = new ArrayList<InEntradaReinversionAPV>();
			p_EntradaRescate = new ArrayList<InEntradaRescate>();
			p_EntradaRescateAPV = new ArrayList<InEntradaRescateAPV>();
		}
	}
	

	/* ************************************************** */
	/* *********** Entrada para la Reinversion ********** */
	/* ************************************************** */
	public static class InEntradaReinversion extends BcsPojo {
		public Long			IdCliente			= 0L;
		public Long			IdPortafolio		= 0L;
		public String		Glosa				= "";
		public String		Simula				= "";
		public String		IndicadorMLT		= "";
		public BigDecimal	MontoAGF			= BigDecimal.ZERO;
		public String       Canal           	= "";
		public String		IdBloqueReInversion = "";
		public List<InDetalleRescateReinversion> p_DetalleRescateReinversion;
		public List<InDetalleAporteReinversion> p_DetalleAporteReinversion;
		
		public InEntradaReinversion() {
			p_DetalleRescateReinversion = new ArrayList<InDetalleRescateReinversion>();
			p_DetalleAporteReinversion = new ArrayList<InDetalleAporteReinversion>();
		}
	}
	
	// Entrada Rescate Reinversion
	public static class InDetalleRescateReinversion extends BcsPojo {
		public String		IdSerieFondo		= "";
		public String		IdRegimenTributario	= "";
		public BigDecimal	Monto				= BigDecimal.ZERO;
		public BigDecimal	NumeroCuotas		= BigDecimal.ZERO;
		public String       RescateTotal    	= "N";
	}
	
	// Entrada Aporte Reinversion
	public static class InDetalleAporteReinversion extends BcsPojo {
		public String		IdSerieFondo		= "";
		public BigDecimal	Porcentaje			= BigDecimal.ZERO;
		public String		IdRegimenTributario	= "";
		public String		Switch				= "";
		public String		GeneraSpot			= "";
	}
	
	
	/* ************************************************** */
	/* ********* Entrada para la Reinversion APV ******** */
	/* ************************************************** */
	public static class InEntradaReinversionAPV extends BcsPojo {
		public Long			IdCliente		= 0L;
		public String		IdGrupoRegimen	= "";
		public String		Glosa			= "";
		public String		Simula			= "";
		public BigDecimal	MontoAGF		= BigDecimal.ZERO;
		public String       Canal           = "";
		public String		IdBloqueReInversion = "";
		public List<InDetalleRescateReinversionAPV> p_DetalleRescateReinversionAPV;
		public List<InDetalleAporteReinversion> p_DetalleAporteReinversionAPV;
		
		public InEntradaReinversionAPV() {
			p_DetalleRescateReinversionAPV = new ArrayList<InDetalleRescateReinversionAPV>();
			p_DetalleAporteReinversionAPV = new ArrayList<InDetalleAporteReinversion>();
		}
	}
	
	// Entrada Rescate Reinversion APV
	public static class InDetalleRescateReinversionAPV extends BcsPojo {
		public Long			IdPortafolio	= 0L;
		public String		IdSerieFondo	= "";
		public BigDecimal	Monto			= BigDecimal.ZERO;
		public BigDecimal	NumeroCuotas	= BigDecimal.ZERO;
		public String       RescateTotal    = "N";
	}
	
	// Entrada Aporte Reinversion APV
	public static class InDetalleAporteReinversionAPV extends BcsPojo {
		public String		IdSerieFondo	= "";
		public BigDecimal	Porcentaje		= BigDecimal.ZERO;
	}
	
	
	/* ************************************************** */
	/* ************* Entrada para el Rescate ************ */
	/* ************************************************** */
	public static class InEntradaRescate extends BcsPojo {
		public Long IdInterno;
		public String Canal;
		public Long IdCliente;
		public Long IdPortafolio;
		public String Simula;
		public String FormaPago;
		public Long IdCuentaBancaria;
		public Long IdPreferenciaPagoPortafolio;
		public List<InDetalleRescate> p_DetalleRescate;
		
		public InEntradaRescate() {
			p_DetalleRescate 			= new ArrayList<InDetalleRescate>();
			IdPreferenciaPagoPortafolio = -1L; //valor por defecto
			FormaPago 					= "";  //valor por defecto
			IdCuentaBancaria			= 0L; //valor por defecto
			IdPortafolio				= 0L;
		}
	}
	
	// Entrada Rescate 
	public static class InDetalleRescate extends BcsPojo {
		public String IdSerieFondo;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
		public String Glosa;
		public String IdRegimenTributario;
		public String RescateTotal;
		
		public InDetalleRescate() {
			
			Monto 		  =  BigDecimal.ZERO; //valor por defecto
			RescateTotal  = "N";  //valor por defecto
			NumeroCuotas  =  BigDecimal.ZERO;; //valor por defecto
		}
	}
	
	/* ************************************************** */
	/* *********** Entrada para el Rescate APV ********** */
	/* ************************************************** */
	public static class InEntradaRescateAPV extends BcsPojo {
		public Long IdInterno;
		public String Canal;
		public Long IdCliente;
		public String IdGrupoRegimen;
		public String Simula;
		public String FormaPago;
		public Long IdCuentaBancaria;
		public Long IdPreferenciaPagoPortafolio;
		public List<InDetalleRescateAPV> p_DetalleRescateAPV;
		
		public InEntradaRescateAPV() {
			p_DetalleRescateAPV = new ArrayList<InDetalleRescateAPV>();
			IdPreferenciaPagoPortafolio = -1L; //valor por defecto
			FormaPago 					= "";  //valor por defecto
			IdCuentaBancaria			= 0L; //valor por defecto
		}
	}
	
	// Entrada Rescate APV
	public static class InDetalleRescateAPV extends BcsPojo {
		public Long IdPortafolio	= 0L;
		public String IdSerieFondo;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
		public String RescateTotal;
		public String Glosa; 
		
		public InDetalleRescateAPV() {
			
			Monto 		  =  BigDecimal.ZERO; //valor por defecto
			RescateTotal  = "N";  //valor por defecto
			NumeroCuotas  =  BigDecimal.ZERO;; //valor por defecto
		}
	}
	
	
	public static class Regimen extends BcsPojo {
		public String IdSerieFondo;
		public String IdRegimenFondoMutuo;
		public String DescripcionRegimen;
		public String PermiteMLT;
		public String FondoExtranjero;
	}
	
	public static class RegimenMandato extends BcsPojo {
		public String RescateConMandato;
	}
	
	public static class IdCuentaBancaria extends BcsPojo {
		public Long IdCuentaBancaria;
	}

	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class SalidaProcesarReinversionRescate extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<ResultadoOperacion> P_OUT_RESULTADO;
		public List<ResultadoPortafolio> P_OUT_RESULTADO_PORTAFOLIO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaProcesarReinversionRescate(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_RESULTADO = new ArrayList<>();
			this.P_OUT_RESULTADO_PORTAFOLIO = new ArrayList<>();
			this.P_OUT_MENSAJES = new ArrayList<>();
		}

		public void agregarValidacion(Validacion validacion) {
			if (validacion.EsError) {
				P_OUT_ERRORESCONTADOR++;
			}
			P_OUT_MENSAJES.add(validacion.Mensaje);
		}

		public void agregarValidaciones(List<Validacion> validaciones) {
			for (Validacion validacion : validaciones) {
				agregarValidacion(validacion);
			}

		}
	}

	public static class Validacion extends BcsPojo {
		public Boolean EsError;
		public MensajeSalida Mensaje;

		public Validacion() {

		}

		public Validacion(Boolean esError, MensajeSalida mensajeSalida) {
			EsError = esError;
			Mensaje = mensajeSalida;
		}
	}

	public static class ResultadoOperacion extends BcsPojo {
		public Long IdInterno;
		public Long	FolioContrato;
		public Long IdSolicitud;
		public Long IdPortafolio;
		public String IdSerieFondo;
		public BigDecimal MontoComisionMonedaFondo;
		public BigDecimal MontoComisionMonedaSistema;
		public BigDecimal ValorPivoteEnMonedaSistema;

		public List<MensajesErrores> Mensajes;

		public ResultadoOperacion() {
			Mensajes = new ArrayList<>();
			MontoComisionMonedaFondo = BigDecimal.ZERO;
			MontoComisionMonedaSistema = BigDecimal.ZERO;
		}
	}

	public static class ResultadoPortafolio extends BcsPojo
	{
		public Long IdPortafolio;
		public BigDecimal TotalComisionesMonedaSistema;

		public ResultadoPortafolio() {
			TotalComisionesMonedaSistema = BigDecimal.ZERO;
		}

		public ResultadoPortafolio(Long idPortafolio) {
			IdPortafolio = idPortafolio;
			TotalComisionesMonedaSistema = BigDecimal.ZERO;
		}
	}
	
	public static class MensajesErrores extends BcsPojo {
		public Long		IdInterno;
		public String	TipoMensaje;
		public String	Codigo;
		public String	Mensaje;
	}
	
	public static class BloqueoReinversion extends BcsPojo{
		public String		IdBloqueaReinversion;
		public Long			IdPortafolio;
		public Long			IdCliente;
		public String		IdRegimenTributario;
		public String 		IdGrupoRegimen;
		public String		IdSerieFondo;	
		public BigDecimal	Monto;
		public BigDecimal	NumeroCuotas;
		public String		RescateTotal;
		public Date			FechaSistema;
		
		public BloqueoReinversion() {
			this.IdPortafolio = 0L;
			this.IdCliente = 0L;
			this.IdRegimenTributario = "";
			this.IdGrupoRegimen = "";
		}
	}
	
	public static class DesbloquearReinversion extends BcsPojo{
		public String		IdBloqueaReinversion;
		
	}
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class ClaseUtilitaria extends BcsPojo {
        public Long		FolioContrato;
        public Long		IdInterno;
	}
	//  inicio JALA al 14-05-2019
	
	public static class UtiPreferenciaPagoPortafolio extends BcsPojo {
		public Long IdPortafolio;
		public Long IdPreferenciaPagoPortafolio;
		
		public UtiPreferenciaPagoPortafolio() {
			this.IdPreferenciaPagoPortafolio = 0L;
		}
	}
	
	public static class UtiPreferenciaPago extends BcsPojo {
		public String IdPreferenciaPago = "";
	}

	public static class UtiPortafolio extends BcsPojo {
		public Long IdPortafolio = 0L;
	}
	//  fin JALA al 14-05-2019
	
	public static class PreferenciaPagoVerificar extends BcsPojo {
		Long IdEmpresa;
		Long IdUsuario;
		Long IdCliente;
		Long IdPortafolio; 
		Long IdPreferenciaPagoPortafolio;
		String IdTipoOperacion; 
		String IdMedioDePago; 
		String IdMoneda;
		Long IdCuentaBancaria;
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public SalidaProcesarReinversionRescate logica(Connection con, EntradaProcesarReinversionRescate entrada) throws Exception
	{
		logger.info("INICIO::ProcesarReinversionRescate");
		SalidaProcesarReinversionRescate out = new SalidaProcesarReinversionRescate();
		SQL sql=new SQL();
		
		Date fechaSistemaLocal = Fecha.toDate("0001-01-01");
		fechaSistemaLocal= Fecha.toDate(FechaDB.ObtenerFechaHoraSistemaLocal(con,entrada.p_IdEmpresa).toString().split(" ")[0]);
		String FechaHoraSistema = Utils.getFechaHoraSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
		String Fecha = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		Long IdInterno = 0L;
		Long IdInternoSecundario = 0L;
		Long ErroresContador = 0L;
		Long ErroresGlobal = 0L;
		Long lngIdPreferenciaPagoPortafolio = 0L;  //JALA al 14-05-2019
		Map<Long, Long> IdInternoGrupoPorIdInternoDetalle = new HashMap<>();
		
		List<ResultadoOperacion> listaResultadoOperacion = new ArrayList<ResultadoOperacion>();
		List<ClaseUtilitaria> listaClaseUtilitaria = new ArrayList<ClaseUtilitaria>();
		
		//Se debe llamar servicio para corroborar si es un rescate total o parcial
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		
		//********************************************************************
		//RC: Rescate, RCA: Rescate APV, RE: Reinversion, REA: Reinversion APV
		//********************************************************************
		
		//***********************************
		//SI ES UN RESCATE
		//***********************************
		if (entrada.p_TipoProceso.trim().equals("RC"))
		{
			out.agregarValidaciones(validarIdsInternos(con, entrada.p_IdEmpresa, entrada.p_EntradaRescate.stream().map(u -> u.IdInterno).collect(Collectors.toList())));
			if (out.P_OUT_ERRORESCONTADOR > 0) {
				return out;
			}
			
			List<String> series = new ArrayList<String>();
			 
	        for(InEntradaRescate inEntradaRescate : entrada.p_EntradaRescate){
	        	if(inEntradaRescate.p_DetalleRescate.size() > 0) {
	        		for(InDetalleRescate inDetalleRescate : inEntradaRescate.p_DetalleRescate) {
	        			series.add(inDetalleRescate.IdSerieFondo);
	        		}
	        	}
	        }
	        
			HashMap<String,Object> paramsSerieFondo = new HashMap<String,Object>(2);
			paramsSerieFondo.put("IdEmpresa", entrada.p_IdEmpresa);
			paramsSerieFondo.put("IdSerieFondo", series);
			logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
			List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, paramsSerieFondo, SerieFondoSolicitudes.class);
			logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
			HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
			
			boolean validaFormaPago = false;
			// 1: Si la entrada viene con info
			if(entrada.p_EntradaRescate.size() > 0)
			{	
				Long cantidadMonedaCLP = entrada.p_EntradaRescate.get(0).p_DetalleRescate.stream().filter(detalle->idMonedaSistema.equals(obtenerMonedaRescate((SerieFondoSolicitudes) hseries.get(detalle.IdSerieFondo), idMonedaSistema, idPaisEmpresa))).count();
				
				if(cantidadMonedaCLP > 0) {
					validaFormaPago = true;
				}
				
				if(validaFormaPago) {
					if (entrada.p_EntradaRescate.get(0).FormaPago != null &&
						    !entrada.p_EntradaRescate.get(0).FormaPago.isEmpty() &&
						    "TRA".equalsIgnoreCase(entrada.p_EntradaRescate.get(0).FormaPago))
					{
						// validamos si la cuenta bancaria es del cliente
						HashMap<String,Object> params =  new HashMap<String,Object>();
						params.put("IdCuentaBanco", entrada.p_EntradaRescate.get(0).IdCuentaBancaria);
						params.put("IdCliente",entrada.p_EntradaRescate.get(0).IdCliente);
						logger.debug("INICIO::SQL_SELECT_CONSULTACUENTABANCARIA");
						List<IdCuentaBancaria>  IdCuentaBancaria = sql.query(con, SQL_SELECT_CONSULTACUENTABANCARIA, params, IdCuentaBancaria.class);
						logger.debug("FIN::SQL_SELECT_CONSULTACUENTABANCARIA");
						if(IdCuentaBancaria.size()==0) {
							// Se crea la lista para cargarla y luego pasarla al proceso
							List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
						
							// Instancia los mensajes de errores
							MensajesErrores mensajesErrores = new MensajesErrores();
						
							// Instancia objeto de salida
							ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
						
							objResultadoOperacion.FolioContrato = 0L;
							objResultadoOperacion.IdSolicitud = 0L;
						
							mensajesErrores.IdInterno = 0L;
							mensajesErrores.TipoMensaje = "ERROR";
							mensajesErrores.Codigo = "ERROR_CUENTA_BANCARIA";
							mensajesErrores.Mensaje = "La Cuenta Bancaria no existe" ;
							
						
							listaMensajesErrores.add(mensajesErrores);
						
							objResultadoOperacion.Mensajes = listaMensajesErrores;
							out.P_OUT_ERRORESCONTADOR++;
							listaResultadoOperacion.add(objResultadoOperacion);
							out.P_OUT_RESULTADO = listaResultadoOperacion;
							return out;
						}
					}
					
					
					
					
					//si viene con datos la entrada del rescate validamos el IdPreferenciaDePagoPortafolio que se envia, que se asume es unico para
					//todos los items de rescate 

					PreferenciaPagoVerificar preferenciaPagoVerificar = new PreferenciaPagoVerificar();
					preferenciaPagoVerificar.IdEmpresa = entrada.p_IdEmpresa;
					preferenciaPagoVerificar.IdUsuario = entrada.p_IdUsuario;
					preferenciaPagoVerificar.IdCliente = entrada.p_EntradaRescate.get(0).IdCliente;
					preferenciaPagoVerificar.IdPortafolio = entrada.p_EntradaRescate.get(0).IdPortafolio;
					preferenciaPagoVerificar.IdPreferenciaPagoPortafolio = entrada.p_EntradaRescate.get(0).IdPreferenciaPagoPortafolio;
					preferenciaPagoVerificar.IdTipoOperacion = _c.TipoOperacionRescateFondoMutuo;
					preferenciaPagoVerificar.IdMedioDePago = entrada.p_EntradaRescate.get(0).FormaPago;
					preferenciaPagoVerificar.IdMoneda = idMonedaSistema;
					preferenciaPagoVerificar.IdCuentaBancaria = entrada.p_EntradaRescate.get(0).IdCuentaBancaria;
					lngIdPreferenciaPagoPortafolio = verificaPreferenciaPago(con, preferenciaPagoVerificar, listaResultadoOperacion);

					//incrementa el error si es que no logra obtener un IdPreferenciaPagoPortafolio
					ErroresContador += lngIdPreferenciaPagoPortafolio >0 ? 0 : 1;
				}
				
			} // Fin 1: Si la entrada viene con info
			
			// Si la entrada viene con info
			// si no logra determinarse el IdPreferenciaPortafolio no puede seguir con el proceso de Rescate
			if(entrada.p_EntradaRescate.size() > 0 && (lngIdPreferenciaPagoPortafolio>0 || !validaFormaPago))
			{
				EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo = new EntradaConsultarFlujoCustodiaFondoMutuo();
				List<Long> fPortafolio = new ArrayList<Long>();
		        for(InEntradaRescate inEntradaRescate : entrada.p_EntradaRescate){
		        	if(inEntradaRescate.p_DetalleRescate.size() > 0) {
		        		for(InDetalleRescate inDetalleRescate : inEntradaRescate.p_DetalleRescate) {
		        			
		        			if (inDetalleRescate.RescateTotal.equalsIgnoreCase("S")) {
								if(!fPortafolio.contains(inEntradaRescate.IdPortafolio)){
										fPortafolio.add(inEntradaRescate.IdPortafolio);
										ConsultarFlujoCustodiaFondoMutuo.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuo.InCarteras();
										cartera.IdCliente = inEntradaRescate.IdCliente;
										cartera.IdPortafolio = inEntradaRescate.IdPortafolio;
										entradaFlujo.p_Carteras.add(cartera);
									}
								
								SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(inDetalleRescate.IdSerieFondo);
								ConsultarFlujoCustodiaFondoMutuo.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuo.InInstrumento();
								instrumento.IdInstrumento = serieFondo.IdInstrumento;
								instrumento.IdRegimenFondoMutuo = esRegimenValido(inDetalleRescate.IdRegimenTributario) ? inDetalleRescate.IdRegimenTributario : null;
								entradaFlujo.p_Instrumentos.add(instrumento);
								}
			        		}
						}
		        	}
		        
		        SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo = new SalidaConsultarFlujoCustodiaFondoMutuo();
		        
				 if(entradaFlujo.p_Carteras.size() > 0){
		        	salidaFlujo = obtenerCustodiaPortafolio(con,entrada, Fecha, idPaisEmpresa, entradaFlujo.p_Carteras, entradaFlujo.p_Instrumentos); 
					
				}
		        
				
				// Instancia la clase principal
				CrearSolicitudAporteRescateNormal objInRescateNormal = new CrearSolicitudAporteRescateNormal();
				
				// Instancia la entrada del rescate
				EntradaCrearSolicitudAporteRescateNormal objInEntradaRescateNormal = new EntradaCrearSolicitudAporteRescateNormal();
				
				// Se crea la lista para cargarla y luego pasarla al proceso
				List<InSolicitudesAporteRescate> listaSolicitudesAporteRescate = new ArrayList<InSolicitudesAporteRescate>();

				Map<Long, InEntradaRescate> entradaRescatePorIdInterno = new HashMap<>(entrada.p_EntradaRescate.size());
				// Recorro el encabezado
				for(InEntradaRescate inEntradaRescate : entrada.p_EntradaRescate)
				{
					entradaRescatePorIdInterno.put(inEntradaRescate.IdInterno, inEntradaRescate);

					objInEntradaRescateNormal.p_IdEmpresa = entrada.p_IdEmpresa;
					objInEntradaRescateNormal.p_IdModulo = "FMM";
					objInEntradaRescateNormal.p_IdUsuario = entrada.p_IdUsuario; 
					objInEntradaRescateNormal.p_SimularOperacion = inEntradaRescate.Simula;
					objInEntradaRescateNormal.p_UsaReglasNegocio = "N";
					objInEntradaRescateNormal.p_IdTipoCanalIngreso = inEntradaRescate.Canal;
					
					// Si la entrada viene con info
					if(inEntradaRescate.p_DetalleRescate.size() > 0)
					{
						// Llamar al proceso de invocar la secuencia que es quien relfeja que una operacion es de un mismo grupo
						List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato" , 1L);
						
						// Por cada Rescate llamo al servicio de crear solicitudes FFMM
						for(InDetalleRescate inDetalleRescate : inEntradaRescate.p_DetalleRescate )
						{
							SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(inDetalleRescate.IdSerieFondo);
							
							// Setea el Id Interno que es el que permite agrupar las operaciones
							IdInterno++;
							IdInternoGrupoPorIdInternoDetalle.put(IdInterno, inEntradaRescate.IdInterno);
							
							//Valida si es rescate total busca el monto total a rescatar
							if (inDetalleRescate.RescateTotal.equalsIgnoreCase("S")) {
							
								for(ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado salida : salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO){
									if(salida.IdPortafolio.equals(inEntradaRescate.IdPortafolio) && salida.IdInstrumento.equals(serieFondo.IdInstrumento) && (FondoMutuoUtils.esRegimenTributarioVacio(inDetalleRescate.IdRegimenTributario) || inDetalleRescate.IdRegimenTributario.equals(salida.IdRegimenTributario))){
										if (salida.CantidadDisponible.compareTo(BigDecimal.ZERO) == 1) {
											inDetalleRescate.Monto = BigDecimal.ZERO;
											inDetalleRescate.NumeroCuotas = salida.CantidadDisponible;
										}else {
											
											List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
											MensajesErrores mensajesErrores = new MensajesErrores();
											ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
											objResultadoOperacion.FolioContrato = 0L;
											objResultadoOperacion.IdSolicitud = 0L;
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = "UI_CLIENTE_SIN_CUSTODIA";
											mensajesErrores.Mensaje = "Cliente sin custodia para instrumento : " + salida.Nemotecnico + " Cantidad : " + salida.CantidadDisponible;
											listaMensajesErrores.add(mensajesErrores);
											objResultadoOperacion.Mensajes = listaMensajesErrores;
											out.P_OUT_ERRORESCONTADOR++;
											listaResultadoOperacion.add(objResultadoOperacion);
											out.P_OUT_RESULTADO = listaResultadoOperacion;
											return out;
											
										}
									}
								}
							}
								

							// Instancia el detalle del rescate
							InSolicitudesAporteRescate objInSolicitudRescate = new InSolicitudesAporteRescate();
							ClaseUtilitaria claseUtilitaria = new ClaseUtilitaria();
							
							objInSolicitudRescate.EsPorMontoLiquido = "N";
							objInSolicitudRescate.FechaOperacion = FechaHoraSistema;
							objInSolicitudRescate.FolioContrato = secuencias.get(0);
							objInSolicitudRescate.Glosa = "INGRESO RESCATE";
							objInSolicitudRescate.IdAutorizadoOrigen = 0L;
							objInSolicitudRescate.IdCliente = inEntradaRescate.IdCliente;
							objInSolicitudRescate.IdEmisorDestino = "";
							objInSolicitudRescate.IdInterno = IdInterno;
							objInSolicitudRescate.IdMonedaPago = obtenerMonedaRescate(serieFondo, idMonedaSistema, idPaisEmpresa);
							objInSolicitudRescate.IdMedioDePago = idMonedaSistema.equals(objInSolicitudRescate.IdMonedaPago) ? inEntradaRescate.FormaPago : _c.CuentaInversion;
							objInSolicitudRescate.IdMoneda = serieFondo.IdMoneda;
							objInSolicitudRescate.IdPortafolio = inEntradaRescate.IdPortafolio;
							objInSolicitudRescate.IdPreferenciaPagoPortafolio = idMonedaSistema.equals(objInSolicitudRescate.IdMonedaPago) ? lngIdPreferenciaPagoPortafolio : 0L;
							objInSolicitudRescate.IdRegimenTributario = inDetalleRescate.IdRegimenTributario == null ? "": inDetalleRescate.IdRegimenTributario ;
							objInSolicitudRescate.IdSerieFondo = inDetalleRescate.IdSerieFondo;
							objInSolicitudRescate.IdSerieFondoDestino = ""; 
							objInSolicitudRescate.IndicadorMLT = "N"; // Ojo
							objInSolicitudRescate.NumeroCuotas = inDetalleRescate.NumeroCuotas;
							objInSolicitudRescate.OrigenOperacion = "M"; 
							objInSolicitudRescate.Porcentaje = BigDecimal.ZERO;
							objInSolicitudRescate.SolicitudBatch = "N";
							objInSolicitudRescate.TipoSolicitud = "RES";
							objInSolicitudRescate.Visado = "N";
							objInSolicitudRescate.Monto = (inDetalleRescate.Monto != null) ? inDetalleRescate.Monto : BigDecimal.ZERO;
							objInSolicitudRescate.CalculoPorMontoCuota =inDetalleRescate.RescateTotal.equalsIgnoreCase("S") && noEsRegimenValido(inDetalleRescate.IdRegimenTributario)  ? "T" : (objInSolicitudRescate.Monto.compareTo(BigDecimal.ZERO) == 0) ? "C" : "M";
							
							if(_c.Dolar.equals(serieFondo.IdMoneda) && !_c.Dolar.equals(objInSolicitudRescate.IdMonedaPago)){
								EntradaConsultarValorDolarFondoMutuo objInValorDolar = new EntradaConsultarValorDolarFondoMutuo();
								ConsultarValorDolarFondoMutuo ConsultarValorDolar = new ConsultarValorDolarFondoMutuo();
								List<SerieFondoParidad> listaSerie = new ArrayList<SerieFondoParidad>();
								SerieFondoParidad objInSerieFondoParidad = new SerieFondoParidad();

								objInSerieFondoParidad.Fecha = Fecha;
								objInSerieFondoParidad.IdSerieFondo= serieFondo.IdSerieFondo;
								objInSerieFondoParidad.IdTipoSolicitud = "";
								listaSerie.add(objInSerieFondoParidad);
								
								objInValorDolar.p_IdEmpresa = entrada.p_IdEmpresa;
								objInValorDolar.p_IdModulo = "FMM";
								objInValorDolar.p_IdUsuario = entrada.p_IdUsuario;
								objInValorDolar.p_Series = listaSerie;
								
								SalidaConsultarValorDolarFondoMutuo objOutSalidaValorDolar = ConsultarValorDolar.logica(con, objInValorDolar);
 								BigDecimal valorDolar  = objOutSalidaValorDolar.P_OUT_RESULTADO.get(0).ValorParidad;
 								objInSolicitudRescate.Monto = objInSolicitudRescate.Monto.divide(valorDolar, MathContext.DECIMAL128);
							}
							
							claseUtilitaria.FolioContrato = objInSolicitudRescate.FolioContrato;
							claseUtilitaria.IdInterno = IdInterno;
							
							// Se llena la lista
							listaSolicitudesAporteRescate.add(objInSolicitudRescate);
							listaClaseUtilitaria.add(claseUtilitaria);
						}
						// Fin for detalle rescate
					}
					// Fin Si validacion con datos detalle
				}
				// fin for Encabezado 
				
				// Se le pasa por parametro la lista recien agregada
				objInEntradaRescateNormal.p_SolicitudesAporteRescate = listaSolicitudesAporteRescate;

				// Invoca a la creacion de solicitudes de rescate
				SalidaCrearSolicitudAporteRescateNormal objOutSalidaSolicitudRescate = objInRescateNormal.logica(con, objInEntradaRescateNormal);
				
				if(objOutSalidaSolicitudRescate.P_OUT_ERRORESCONTADOR > 0)
				{
					for(ClaseUtilitaria claseUtilitaria : listaClaseUtilitaria)
					{
						for(MensajeSalida errorDetalle : objOutSalidaSolicitudRescate.P_OUT_MENSAJES)
						{
							if(claseUtilitaria.IdInterno == errorDetalle.id  || ErroresGlobal == errorDetalle.id || errorDetalle.id < 0 )
							{
								ErroresGlobal++;
								
								// Se crea la lista para cargarla y luego pasarla al proceso
								List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
								
								// Instancia los mensajes de errores
								MensajesErrores mensajesErrores = new MensajesErrores();
								
								// Instancia objeto de salida
								ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
								
								objResultadoOperacion.FolioContrato = claseUtilitaria.FolioContrato;
								objResultadoOperacion.IdSolicitud = claseUtilitaria.IdInterno;
								
								mensajesErrores.IdInterno = errorDetalle.id;
								mensajesErrores.TipoMensaje = errorDetalle.tipo;
								mensajesErrores.Codigo = errorDetalle.codigo;
								mensajesErrores.Mensaje = errorDetalle.descripcion;
								
								listaMensajesErrores.add(mensajesErrores);
								
								objResultadoOperacion.Mensajes = listaMensajesErrores;
								
								listaResultadoOperacion.add(objResultadoOperacion);
								
								if(MensajeSalida.MENSAJE_ERROR.equals(mensajesErrores.TipoMensaje)) {
									ErroresContador++;
								}
							}
						}
					}

				}
				else
				{
					for (ResultadoSolicitud resultadoSolicitud : objOutSalidaSolicitudRescate.P_OUT_RESULTADO)
					{
						ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
						objResultadoOperacion.IdInterno = IdInternoGrupoPorIdInternoDetalle.getOrDefault(resultadoSolicitud.IdInterno, -1L);
						objResultadoOperacion.IdSerieFondo = resultadoSolicitud.IdSerieFondo;
						objResultadoOperacion.FolioContrato = resultadoSolicitud.FolioContrato;
						objResultadoOperacion.IdSolicitud = resultadoSolicitud.IdSolicitudAporteRescate;
						objResultadoOperacion.IdPortafolio = resultadoSolicitud.IdPortafolio;
						objResultadoOperacion.MontoComisionMonedaFondo = resultadoSolicitud.MontoComision;
						objResultadoOperacion.MontoComisionMonedaSistema = resultadoSolicitud.MontoComisionMonedaSistema;
						objResultadoOperacion.ValorPivoteEnMonedaSistema = resultadoSolicitud.ValorPivoteSistemaConversionSistema;

						InEntradaRescate entradaRescate = entradaRescatePorIdInterno.getOrDefault(objResultadoOperacion.IdInterno, new InEntradaRescate());
						MensajesErrores mensajesErrores = new MensajesErrores();
						mensajesErrores.IdInterno = resultadoSolicitud.IdInterno;
						mensajesErrores.TipoMensaje = MensajeSalida.MENSAJE_INFORMACION;
						mensajesErrores.Codigo = _c.SI.equals(entradaRescate.Simula) ? "SIMULACION_RESCATE_EXITOSA" : "CREACION_RESCATE_EXITOSA";
						mensajesErrores.Mensaje = obtenerDescripcionDiccionario(con, entrada.p_IdEmpresa, mensajesErrores.Codigo, new HashMap<>());
						objResultadoOperacion.Mensajes.add(mensajesErrores);

						listaResultadoOperacion.add(objResultadoOperacion);
					}
				}
			}
			// Fin validacion con datos encabezado y IdPreferenciaPagoPortafolio
		}
		// Fin validacion si es un Rescate
		
		
		//***********************************
		//SI ES UN RESCATE APV
		//***********************************
		if (entrada.p_TipoProceso.trim().equals("RCA"))
		{
			out.agregarValidaciones(validarIdsInternos(con, entrada.p_IdEmpresa, entrada.p_EntradaRescateAPV.stream().map(u -> u.IdInterno).collect(Collectors.toList())));
			if (out.P_OUT_ERRORESCONTADOR > 0) {
				return out;
			}

			//JALA al 14-05-2019 inicio
			// 1: Si la entrada viene con info
			if(entrada.p_EntradaRescateAPV.size() > 0 && entrada.p_EntradaRescateAPV.get(0).p_DetalleRescateAPV.size()>0)
			{
				if (entrada.p_EntradaRescateAPV.get(0).FormaPago != null &&
					    !entrada.p_EntradaRescateAPV.get(0).FormaPago.trim().isEmpty() &&
					    "TRA".equalsIgnoreCase(entrada.p_EntradaRescateAPV.get(0).FormaPago)) 
				{
					// validamos si la cuenta bancaria es del cliente
					HashMap<String,Object> params =  new HashMap<>();
					params.put("IdCuentaBanco", entrada.p_EntradaRescateAPV.get(0).IdCuentaBancaria);
					params.put("IdCliente",entrada.p_EntradaRescateAPV.get(0).IdCliente);
					logger.debug("INICIO::SQL_SELECT_CONSULTACUENTABANCARIA");
					List<IdCuentaBancaria>  IdCuentaBancaria = sql.query(con, SQL_SELECT_CONSULTACUENTABANCARIA, params, IdCuentaBancaria.class);
					logger.debug("FIN::SQL_SELECT_CONSULTACUENTABANCARIA");
					
					if(IdCuentaBancaria.size()==0) {
						// Se crea la lista para cargarla y luego pasarla al proceso
						List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
					
						// Instancia los mensajes de errores
						MensajesErrores mensajesErrores = new MensajesErrores();
					
						// Instancia objeto de salida
						ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
					
						objResultadoOperacion.FolioContrato = 0L;
						objResultadoOperacion.IdSolicitud = 0L;
					
						mensajesErrores.IdInterno = 0L;
						mensajesErrores.TipoMensaje = "ERROR";
						mensajesErrores.Codigo = "ERROR_CUENTA_BANCARIA";
						mensajesErrores.Mensaje = "La Cuenta Bancaria no existe" ;
						
					
						listaMensajesErrores.add(mensajesErrores);
					
						objResultadoOperacion.Mensajes = listaMensajesErrores;
						out.P_OUT_ERRORESCONTADOR++;
						listaResultadoOperacion.add(objResultadoOperacion);
						out.P_OUT_RESULTADO = listaResultadoOperacion;
						return out;
					}
				}
				
				
				//si viene con datos la entrada del rescate validamos el IdPreferenciaDePagoPortafolio que se envia, que se asume es unico para
				//todos los items de rescate 

				//JALA al 14-04-2019
				PreferenciaPagoVerificar preferenciaPagoVerificar = new PreferenciaPagoVerificar();
				preferenciaPagoVerificar.IdEmpresa = entrada.p_IdEmpresa;
				preferenciaPagoVerificar.IdUsuario = entrada.p_IdUsuario;
				preferenciaPagoVerificar.IdCliente = entrada.p_EntradaRescateAPV.get(0).IdCliente;
				preferenciaPagoVerificar.IdPortafolio = entrada.p_EntradaRescateAPV.get(0).p_DetalleRescateAPV.get(0).IdPortafolio;
				preferenciaPagoVerificar.IdPreferenciaPagoPortafolio =  entrada.p_EntradaRescateAPV.get(0).IdPreferenciaPagoPortafolio;
				preferenciaPagoVerificar.IdTipoOperacion = _c.TipoOperacionRescateFondoMutuo;
				preferenciaPagoVerificar.IdMedioDePago = entrada.p_EntradaRescateAPV.get(0).FormaPago;
				preferenciaPagoVerificar.IdMoneda = idMonedaSistema;
				preferenciaPagoVerificar.IdCuentaBancaria = entrada.p_EntradaRescateAPV.get(0).IdCuentaBancaria;
				lngIdPreferenciaPagoPortafolio = verificaPreferenciaPago(con, preferenciaPagoVerificar, listaResultadoOperacion);

				//incrementa el error si es que no logra obtener un IdPreferenciaPagoPortafolio
				ErroresContador += lngIdPreferenciaPagoPortafolio >0 ? 0 : 1;  
				
				
			} // Fin 1: Si la entrada viene con info
			//JALA al 14-05-2019 Fin
			
			
			// Si la entrada viene con info
			// JALA AL 14-05-2019, si no logra determinarse el IdPreferenciaPortafolio no puede seguir con el proceso de Rescate
			if(entrada.p_EntradaRescateAPV.size() > 0 && lngIdPreferenciaPagoPortafolio > 0)
			{
				EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo = new EntradaConsultarFlujoCustodiaFondoMutuo();
				List<Long> fPortafolio = new ArrayList<Long>();
				List<String> series = new ArrayList<String>();
				 
		        for(InEntradaRescateAPV inEntradaRescateAPV : entrada.p_EntradaRescateAPV){
		        	if(inEntradaRescateAPV.p_DetalleRescateAPV.size() > 0) {
		        		for(InDetalleRescateAPV inDetalleRescateAPV : inEntradaRescateAPV.p_DetalleRescateAPV) {
		        			series.add(inDetalleRescateAPV.IdSerieFondo);
		        		}
		        	}
		        }
		        
				HashMap<String,Object> params = new HashMap<String,Object>();
				params.put("IdEmpresa", entrada.p_IdEmpresa);
				params.put("IdSerieFondo", series);
				logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
				List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
				logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
				HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");

		        for(InEntradaRescateAPV inEntradaRescateAPV : entrada.p_EntradaRescateAPV){
		        	if(inEntradaRescateAPV.p_DetalleRescateAPV.size() > 0) {
		        		for(InDetalleRescateAPV inDetalleRescateAPV : inEntradaRescateAPV.p_DetalleRescateAPV) {
		        			
		        			if (inDetalleRescateAPV.RescateTotal.equalsIgnoreCase("S")) {
								if(!fPortafolio.contains(inDetalleRescateAPV.IdPortafolio)){
										fPortafolio.add(inDetalleRescateAPV.IdPortafolio);
										ConsultarFlujoCustodiaFondoMutuo.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuo.InCarteras();
										cartera.IdCliente = inEntradaRescateAPV.IdCliente;
										cartera.IdPortafolio = inDetalleRescateAPV.IdPortafolio;
										entradaFlujo.p_Carteras.add(cartera);
									}
								
								SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(inDetalleRescateAPV.IdSerieFondo);
								ConsultarFlujoCustodiaFondoMutuo.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuo.InInstrumento();
								instrumento.IdInstrumento = serieFondo.IdInstrumento;
								instrumento.IdRegimenFondoMutuo = null;
								entradaFlujo.p_Instrumentos.add(instrumento);
								}
			        		}
						}
		        	}
		        
		        SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo = new SalidaConsultarFlujoCustodiaFondoMutuo();
		        
				 if(entradaFlujo.p_Carteras.size() > 0){
		        	salidaFlujo = obtenerCustodiaPortafolio(con,entrada, Fecha, idPaisEmpresa, entradaFlujo.p_Carteras, entradaFlujo.p_Instrumentos); 
		        	
				}
				
				 
				// Instancia la clase principal
				CrearSolicitudRescateNormalPorCliente objInRescateNormalCliente = new CrearSolicitudRescateNormalPorCliente();
				
				// Instancia la entrada del rescate
				EntradaCrearSolicitudRescateNormalPorCliente objInEntradaRescateNormalCliente = new EntradaCrearSolicitudRescateNormalPorCliente();

				Map<Long, InEntradaRescateAPV> entradaRescateAPVPorIdInterno = new HashMap<>(entrada.p_EntradaRescateAPV.size());
				// Recorro el encabezado
				for(InEntradaRescateAPV inEntradaRescateAPV : entrada.p_EntradaRescateAPV)
				{
					entradaRescateAPVPorIdInterno.put(inEntradaRescateAPV.IdInterno, inEntradaRescateAPV);

					objInEntradaRescateNormalCliente.p_IdEmpresa = entrada.p_IdEmpresa;
					objInEntradaRescateNormalCliente.p_IdModulo = "FMM";
					objInEntradaRescateNormalCliente.p_IdUsuario = entrada.p_IdUsuario; 
					objInEntradaRescateNormalCliente.p_SimularOperacion = inEntradaRescateAPV.Simula;
					objInEntradaRescateNormalCliente.p_UsaReglasNegocio = "N";
					objInEntradaRescateNormalCliente.p_IdTipoCanalIngreso = inEntradaRescateAPV.Canal;
					
					// Llamar al proceso de invocar la secuencia que es quien relfeja que una operacion es de un mismo grupo
					List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato" , 1L);

					// Si la entrada viene con info
					if(inEntradaRescateAPV.p_DetalleRescateAPV.size() > 0)
					{
						// Se crea la lista para cargarla y luego pasarla al proceso
						List<InSolicitudRescateCliente> listaSolicitudRescateCliente = new ArrayList<InSolicitudRescateCliente>();
						
						// Por cada Rescate llamo al servicio de crear solicitudes FFMM
						for(InDetalleRescateAPV inDetalleRescateAPV : inEntradaRescateAPV.p_DetalleRescateAPV)
						{
							SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(inDetalleRescateAPV.IdSerieFondo);
							
							// Setea el Id Interno que es el que permite agrupar las operaciones
							IdInterno++;
							IdInternoGrupoPorIdInternoDetalle.put(IdInterno, inEntradaRescateAPV.IdInterno);
							
							if (inDetalleRescateAPV.RescateTotal.equalsIgnoreCase("S")) {
							
								for(ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado salida : salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO){
									if(salida.IdPortafolio.equals(inDetalleRescateAPV.IdPortafolio) && salida.IdInstrumento.equals(serieFondo.IdInstrumento)){
										
										if (salida.CantidadDisponible.compareTo(BigDecimal.ZERO) == 1) {
											inDetalleRescateAPV.Monto = salida.MontoDisponible;
											inDetalleRescateAPV.NumeroCuotas = salida.CantidadDisponible;
										}else {
											List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
											MensajesErrores mensajesErrores = new MensajesErrores();
											ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
											objResultadoOperacion.FolioContrato = 0L;
											objResultadoOperacion.IdSolicitud = 0L;
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = "UI_CLIENTE_SIN_CUSTODIA";
											mensajesErrores.Mensaje = "Cliente sin custodia para instrumento : " + salida.Nemotecnico + " Cantidad : " + salida.CantidadDisponible;
											listaMensajesErrores.add(mensajesErrores);
											objResultadoOperacion.Mensajes = listaMensajesErrores;
											out.P_OUT_ERRORESCONTADOR++;
											listaResultadoOperacion.add(objResultadoOperacion);
											out.P_OUT_RESULTADO = listaResultadoOperacion;
											return out;
											
										}
									}
								}
							}
								

							// Instancia el detalle del rescate
							InSolicitudRescateCliente objInSolicitudRescateAPV = new InSolicitudRescateCliente();
							ClaseUtilitaria claseUtilitaria = new ClaseUtilitaria();
							
							objInSolicitudRescateAPV.IdInterno = IdInterno;
							objInSolicitudRescateAPV.FolioContrato = secuencias.get(0); //Ojo
							objInSolicitudRescateAPV.IdCliente = inEntradaRescateAPV.IdCliente;
							objInSolicitudRescateAPV.IdPortafolio = inDetalleRescateAPV.IdPortafolio;
							objInSolicitudRescateAPV.IdSerieFondo = inDetalleRescateAPV.IdSerieFondo;
							objInSolicitudRescateAPV.NumeroCuotas = inDetalleRescateAPV.NumeroCuotas;
							objInSolicitudRescateAPV.IdMoneda = serieFondo.IdMoneda;
							objInSolicitudRescateAPV.IdMonedaPago = idMonedaSistema;// "N";
							objInSolicitudRescateAPV.IdMedioDePago = inEntradaRescateAPV.FormaPago; // "CIN";
							objInSolicitudRescateAPV.IdCuentaBancaria = inEntradaRescateAPV.IdCuentaBancaria; //inEntradaRescateAPV. lngIdPreferenciaPagoPortafolio; //JALA al 14-05-2019, antes estaba esto: inEntradaRescateAPV.IdPreferenciaPagoPortafolio;
							objInSolicitudRescateAPV.IdGrupoRegimen = inEntradaRescateAPV.IdGrupoRegimen;
							objInSolicitudRescateAPV.Glosa = "INGRESO RESCATE APV";
							objInSolicitudRescateAPV.Monto = (inDetalleRescateAPV.Monto != null) ? inDetalleRescateAPV.Monto : BigDecimal.ZERO;
							objInSolicitudRescateAPV.CalculoPorMontoCuota = inDetalleRescateAPV.RescateTotal.equalsIgnoreCase("S") ? "T" : (objInSolicitudRescateAPV.Monto.compareTo(BigDecimal.ZERO) == 0) ? "C" : "M";
							objInSolicitudRescateAPV.IdPreferenciaPagoPortafolio = lngIdPreferenciaPagoPortafolio;
							
							if(_c.Dolar.equals(serieFondo.IdMoneda)){
								EntradaConsultarValorDolarFondoMutuo objInValorDolar = new EntradaConsultarValorDolarFondoMutuo();
								ConsultarValorDolarFondoMutuo ConsultarValorDolar = new ConsultarValorDolarFondoMutuo();
								List<SerieFondoParidad> listaSerie = new ArrayList<SerieFondoParidad>();
								SerieFondoParidad objInSerieFondoParidad = new SerieFondoParidad();

								objInSerieFondoParidad.Fecha = Fecha;
								objInSerieFondoParidad.IdSerieFondo= serieFondo.IdSerieFondo;
								objInSerieFondoParidad.IdTipoSolicitud = "";
								listaSerie.add(objInSerieFondoParidad);
								
								objInValorDolar.p_IdEmpresa = entrada.p_IdEmpresa;
								objInValorDolar.p_IdModulo = "FMM";
								objInValorDolar.p_IdUsuario = entrada.p_IdUsuario;
								objInValorDolar.p_Series = listaSerie;
								
								SalidaConsultarValorDolarFondoMutuo objOutSalidaValorDolar = ConsultarValorDolar.logica(con, objInValorDolar);
 								BigDecimal valorDolar  = objOutSalidaValorDolar.P_OUT_RESULTADO.get(0).ValorParidad;
 								objInSolicitudRescateAPV.Monto = objInSolicitudRescateAPV.Monto.divide(valorDolar, MathContext.DECIMAL128);
							}
							
							claseUtilitaria.FolioContrato = objInSolicitudRescateAPV.FolioContrato;
							claseUtilitaria.IdInterno = IdInterno;
							
							// Se llena la lista
							listaSolicitudRescateCliente.add(objInSolicitudRescateAPV);
							listaClaseUtilitaria.add(claseUtilitaria);
							
							// Se le pasa por parametro la lista recien agregada
							objInEntradaRescateNormalCliente.p_SolicitudesRescate.add(objInSolicitudRescateAPV) ;
						} 
						// Fin for detalle rescate APV
						

					} 
					// Fin Si validacion con datos detalle
				} 
				// fin for Encabezado 

				// Invoca a la creacion de solicitudes de rescate por Cliente
				SalidaCrearSolicitudRescateNormalPorCliente objOutSolicitudRescateCliente = objInRescateNormalCliente.logica(con, objInEntradaRescateNormalCliente);
				
				if(objOutSolicitudRescateCliente.P_OUT_ERRORESCONTADOR > 0)
				{
					for(ClaseUtilitaria claseUtilitaria : listaClaseUtilitaria)
					{
						for(MensajeSalida errorDetalle : objOutSolicitudRescateCliente.P_OUT_MENSAJES)
						{
							if(claseUtilitaria.IdInterno == errorDetalle.id  || ErroresGlobal == errorDetalle.id || errorDetalle.id < 0 )
							{
								ErroresGlobal++;
								
								// Se crea la lista para cargarla y luego pasarla al proceso
								List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
								
								// Instancia los mensajes de errores
								MensajesErrores mensajesErrores = new MensajesErrores();
								
								// Instancia objeto de salida
								ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
								
								objResultadoOperacion.FolioContrato = claseUtilitaria.FolioContrato;
								objResultadoOperacion.IdSolicitud = claseUtilitaria.IdInterno;
								
								mensajesErrores.IdInterno = errorDetalle.id;
								mensajesErrores.TipoMensaje = errorDetalle.tipo;
								mensajesErrores.Codigo = errorDetalle.codigo;
								mensajesErrores.Mensaje = errorDetalle.descripcion;
								
								listaMensajesErrores.add(mensajesErrores);
								
								objResultadoOperacion.Mensajes = listaMensajesErrores;
								
								listaResultadoOperacion.add(objResultadoOperacion);
								
								if(MensajeSalida.MENSAJE_ERROR.equals(errorDetalle.tipo)) {
									ErroresContador++;
								}
							}
						}
					}

				}
				else
				{
					for (ResultadoSolicitud resultadoSolicitud : objOutSolicitudRescateCliente.P_OUT_RESULTADO)
					{
						ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
						objResultadoOperacion.IdInterno = IdInternoGrupoPorIdInternoDetalle.getOrDefault(resultadoSolicitud.IdInterno, -1L);
						objResultadoOperacion.IdSerieFondo = resultadoSolicitud.IdSerieFondo;
						objResultadoOperacion.FolioContrato = resultadoSolicitud.FolioContrato;
						objResultadoOperacion.IdSolicitud = resultadoSolicitud.IdSolicitudAporteRescate;
						objResultadoOperacion.IdPortafolio = resultadoSolicitud.IdPortafolio;
						objResultadoOperacion.MontoComisionMonedaFondo = resultadoSolicitud.MontoComision;
						objResultadoOperacion.MontoComisionMonedaSistema = resultadoSolicitud.MontoComisionMonedaSistema;
						objResultadoOperacion.ValorPivoteEnMonedaSistema = resultadoSolicitud.ValorPivoteSistemaConversionSistema;

						InEntradaRescateAPV entradaRescateAPV = entradaRescateAPVPorIdInterno.getOrDefault(objResultadoOperacion.IdInterno, new InEntradaRescateAPV());
						MensajesErrores mensajesErrores = new MensajesErrores();
						mensajesErrores.IdInterno = resultadoSolicitud.IdInterno;
						mensajesErrores.TipoMensaje = MensajeSalida.MENSAJE_INFORMACION;
						mensajesErrores.Codigo = _c.SI.equals(entradaRescateAPV.Simula) ? "UI_SIMULACION_RESCATE_EXITOSA" : "CREACION_RESCATE_EXITOSA";
						mensajesErrores.Mensaje = obtenerDescripcionDiccionario(con, entrada.p_IdEmpresa, mensajesErrores.Codigo, new HashMap<>());
						objResultadoOperacion.Mensajes.add(mensajesErrores);

						listaResultadoOperacion.add(objResultadoOperacion);
					}
				}
			} 
			// Fin validacion con datos encabezado 
		}
		// Fin validacion si es un Rescate APV
		
		
		//***********************************
		//SI ES UNA REINVERSION NO APV
		//***********************************
		if (entrada.p_TipoProceso.trim().equals("RE"))
		{
			// Si la entrada viene con info
			if(entrada.p_EntradaReinversion.size() > 0)
			{
				// Instancia la clase principal
				CrearSolicitudRescateReinversion1N objInReinversion1N = new CrearSolicitudRescateReinversion1N();
				
				// Instancia la entrada del rescate
				
				EntradaCrearSolicitudRescateReinversion1N objInEntradaReinversion = new EntradaCrearSolicitudRescateReinversion1N();
				
				// Instancia Busqueda de cantidad por Grupo Regimen
				BuscarCantidadesPorRegimen buscarCantidadesPorRegimen = new BuscarCantidadesPorRegimen();
				
				// Llamar al proceso de invocar la secuencia que es quien relfeja que una operacion es de un mismo grupo
				List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato" , 1L);
				List<String> series = new ArrayList<String>();
			    EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo = new EntradaConsultarFlujoCustodiaFondoMutuo();
				
				List<Long> fPortafolio = new ArrayList<Long>();
				List<String> seriesres = new ArrayList<String>();
				
				for(InEntradaReinversion inEntradaReinversion : entrada.p_EntradaReinversion){
					// Si la entrada de rescate viene con info
					if(inEntradaReinversion.p_DetalleRescateReinversion.size() > 0){
						

						if(out.P_OUT_ERRORESCONTADOR>0) {
							return out;
						}					
						
						for(InDetalleRescateReinversion  inDetalleRescateReinversion : inEntradaReinversion.p_DetalleRescateReinversion) {
							seriesres.add(inDetalleRescateReinversion.IdSerieFondo);
				        };
					}
				}	
				
				HashMap<String,Object> paramsreinv = new HashMap<String,Object>();
				paramsreinv.put("IdEmpresa", entrada.p_IdEmpresa);
				paramsreinv.put("IdSerieFondo", seriesres);
				logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
				List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, paramsreinv, SerieFondoSolicitudes.class);
				logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
				HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
				
				for(InEntradaReinversion inEntradaReinversion : entrada.p_EntradaReinversion){
						for(InDetalleRescateReinversion  inDetalleRescateReinversion : inEntradaReinversion.p_DetalleRescateReinversion){
				        	if (inDetalleRescateReinversion.RescateTotal.equalsIgnoreCase("S")) {
									if(!fPortafolio.contains(inEntradaReinversion.IdPortafolio)){
											fPortafolio.add(inEntradaReinversion.IdPortafolio);
											ConsultarFlujoCustodiaFondoMutuo.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuo.InCarteras();
											cartera.IdCliente = inEntradaReinversion.IdCliente;
											cartera.IdPortafolio = inEntradaReinversion.IdPortafolio;
											entradaFlujo.p_Carteras.add(cartera);
										}
									
									SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(inDetalleRescateReinversion.IdSerieFondo);
									ConsultarFlujoCustodiaFondoMutuo.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuo.InInstrumento();
									instrumento.IdInstrumento = serieFondo.IdInstrumento;
									instrumento.IdRegimenFondoMutuo = esRegimenValido(inDetalleRescateReinversion.IdRegimenTributario) ? inDetalleRescateReinversion.IdRegimenTributario : null;
									entradaFlujo.p_Instrumentos.add(instrumento);
							}
					        		
						}
						
					}
				
				 SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo = new SalidaConsultarFlujoCustodiaFondoMutuo();
		        
				 if(entradaFlujo.p_Carteras.size() > 0){
		        	salidaFlujo = obtenerCustodiaPortafolio(con,entrada, Fecha, idPaisEmpresa, entradaFlujo.p_Carteras, entradaFlujo.p_Instrumentos); 
					
				}
				
				// Recorro el encabezado
				for(InEntradaReinversion inEntradaReinversion : entrada.p_EntradaReinversion)
				{
					objInEntradaReinversion.p_IdEmpresa = entrada.p_IdEmpresa;
					objInEntradaReinversion.p_IdModulo = "FMM";
					objInEntradaReinversion.p_IdUsuario = entrada.p_IdUsuario; 
					objInEntradaReinversion.p_SimularOperacion = inEntradaReinversion.Simula;
					objInEntradaReinversion.p_UsaReglasNegocio = "N";
					objInEntradaReinversion.p_IdTipoCanalIngreso = inEntradaReinversion.Canal;
					
					BigDecimal MontoReinversion = BigDecimal.ZERO;
					BigDecimal NumeroCuotasReinversion = BigDecimal.ZERO;
					
					List<SerieFondoSolicitudes> seriesSolicitudesRe = new ArrayList<SerieFondoSolicitudes>();
					List<GrupoRegimen> grupoRegimenSolicitudesRe = new ArrayList<GrupoRegimen>();
					List<SerieFondoSolicitudes> seriesSolicitudesAp = new ArrayList<SerieFondoSolicitudes>();
					
					String SerieRe = "";
					BigDecimal sumaPorcentaje = BigDecimal.ZERO;
					
					// Si la entrada de rescate viene con info
					if(inEntradaReinversion.p_DetalleRescateReinversion.size() > 0)
					{
						// Se crea la lista para cargarla y luego pasarla al proceso
						List<InSolicitudesRescate> listaSolicitudRescateReinversion = new ArrayList<InSolicitudesRescate>();
						
						int count = 0;
  						for(Long Id = 1L; Id <= inEntradaReinversion.p_DetalleAporteReinversion.size(); Id ++)
						{
							sumaPorcentaje = sumaPorcentaje.add(inEntradaReinversion.p_DetalleAporteReinversion.get(count).Porcentaje);
							// Por cada Rescate llamo al servicio rescate reinversion de crear solicitudes FFMM
							for(InDetalleRescateReinversion  inDetalleRescateReinversion : inEntradaReinversion.p_DetalleRescateReinversion)
							{
								
								//Valida si es rescate total busca el monto total a rescatar
								if (inDetalleRescateReinversion.RescateTotal.equalsIgnoreCase("S")) {
									
									SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(inDetalleRescateReinversion.IdSerieFondo);
									
									for(ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado salida : salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO){
										if(salida.IdPortafolio.equals(inEntradaReinversion.IdPortafolio) && salida.IdInstrumento.equals(serieFondo.IdInstrumento) && (FondoMutuoUtils.esRegimenTributarioVacio(inDetalleRescateReinversion.IdRegimenTributario) || inDetalleRescateReinversion.IdRegimenTributario.equals(salida.IdRegimenTributario))){
											MontoReinversion = salida.MontoDisponible;
											NumeroCuotasReinversion = salida.CantidadDisponible;
										}
									}
								}else {
									MontoReinversion = inDetalleRescateReinversion.Monto;
									NumeroCuotasReinversion = inDetalleRescateReinversion.NumeroCuotas;
								}
																
								series.clear();
								if(!series.contains(inDetalleRescateReinversion.IdSerieFondo)) series.add(inDetalleRescateReinversion.IdSerieFondo);
								
								
								HashMap<String,Object> params = new HashMap<String,Object>();
								params.put("IdEmpresa", entrada.p_IdEmpresa);
								params.put("IdSerieFondo", series);
								logger.debug("INICIO::SQL_SELECT_GRUPO_REGIMEN_SERIE_FONDO");
								grupoRegimenSolicitudesRe = sql.query(con, SQL_SELECT_GRUPO_REGIMEN_SERIE_FONDO, params, GrupoRegimen.class);
								logger.debug("FIN::SQL_SELECT_GRUPO_REGIMEN_SERIE_FONDO");
								logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
								seriesSolicitudesRe = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
								logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
								params.clear();
								
								EntradaBuscarCantidadesPorRegimen entradaBuscarCantidadesPorRegimen = new EntradaBuscarCantidadesPorRegimen();
								entradaBuscarCantidadesPorRegimen.p_IdEmpresa = entrada.p_IdEmpresa;
								entradaBuscarCantidadesPorRegimen.p_IdModulo = "FMM";
								entradaBuscarCantidadesPorRegimen.p_IdUsuario = entrada.p_IdUsuario;
								
								InClientes cliente = new InClientes();
								cliente.IdInterno = Id;
								cliente.IdCliente = inEntradaReinversion.IdCliente;
								cliente.IdInstrumento = seriesSolicitudesRe != null && seriesSolicitudesRe.size() > 0 ? seriesSolicitudesRe.get(0).IdInstrumento : 0L;
								cliente.Cantidad = NumeroCuotasReinversion;
								cliente.IdGrupoRegimen = grupoRegimenSolicitudesRe != null && grupoRegimenSolicitudesRe.size() > 0 ? grupoRegimenSolicitudesRe.get(0).GrupoRegimen : "";
								cliente.IdSerieFondo = inDetalleRescateReinversion.IdSerieFondo;
								cliente.IdRegimenFondoMutuo = esRegimenValido(inDetalleRescateReinversion.IdRegimenTributario) ? inDetalleRescateReinversion.IdRegimenTributario : null;
								entradaBuscarCantidadesPorRegimen.p_Clientes.add(cliente);
								SalidaBuscarCantidadesPorRegimen salidaBuscarCantidadesPorRegimen = buscarCantidadesPorRegimen.logica(con, entradaBuscarCantidadesPorRegimen);
								
								if(salidaBuscarCantidadesPorRegimen.P_OUT_ERRORESCONTADOR > 0L) {
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
									
									for(MensajeSalida mje : salidaBuscarCantidadesPorRegimen.P_OUT_MENSAJES) {
										// Instancia los mensajes de errores
										MensajesErrores mensajesErrores = new MensajesErrores();
										
										// Instancia objeto de salida
										ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
									
									
										mensajesErrores.IdInterno = 0L;
										mensajesErrores.TipoMensaje = mje.tipo;
										mensajesErrores.Codigo = mje.codigo;
										mensajesErrores.Mensaje = mje.descripcion;
									
										listaMensajesErrores.add(mensajesErrores);
									
										objResultadoOperacion.Mensajes = listaMensajesErrores;
										out.P_OUT_ERRORESCONTADOR++;
										listaResultadoOperacion.add(objResultadoOperacion);
										out.P_OUT_RESULTADO = listaResultadoOperacion;
									}
									return out;
								}
						      //validamos si MLT es en S
								if (!entrada.p_EntradaReinversion.isEmpty() && 
									    entrada.p_EntradaReinversion.get(0).IndicadorMLT != null &&
									    "S".equalsIgnoreCase(entrada.p_EntradaReinversion.get(0).IndicadorMLT))
								{
						        
						        	params.put("IdSerieFondo", series);
									params.put("IdRegimenFondoMutuo",inDetalleRescateReinversion.IdRegimenTributario);
									logger.debug("INICIO::SQL_SELECT_REGIMEN");
									List<Regimen>  regimenes = sql.query(con, SQL_SELECT_REGIMEN, params, Regimen.class);
									logger.debug("FIN::SQL_SELECT_REGIMEN");
									
									// validamos si es fondo extranjero , en el caso que lo sea el indicador mlt sera N
									if (!regimenes.isEmpty() && 
										    ("S".equals(regimenes.get(0).FondoExtranjero) || "N".equals(regimenes.get(0).PermiteMLT)))
									{
										// Se crea la lista para cargarla y luego pasarla al proceso
										List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
									
										// Instancia los mensajes de errores
										MensajesErrores mensajesErrores = new MensajesErrores();
									
										// Instancia objeto de salida
										ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
									
										String codigo = "UI_ERROR_INDICADOR_MLT";
										String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).add("IdSerieFondo", regimenes.get(0).IdSerieFondo).add("IdRegimenFondoMutuo",inDetalleRescateReinversion.IdRegimenTributario).msg();										
										
										mensajesErrores.IdInterno = 0L;
										mensajesErrores.TipoMensaje = MensajeSalida.MENSAJE_ERROR;
										mensajesErrores.Codigo = codigo;
										mensajesErrores.Mensaje = descripcion;
									
										listaMensajesErrores.add(mensajesErrores);
									
										objResultadoOperacion.Mensajes = listaMensajesErrores;
										
										out.P_OUT_ERRORESCONTADOR++;
										listaResultadoOperacion.add(objResultadoOperacion);
										out.P_OUT_RESULTADO = listaResultadoOperacion;
										return out;
									}
						        }
						        
								
								// Instancia el detalle del rescate
								InSolicitudesRescate objInSolicitudRescateReinversion = new InSolicitudesRescate();
								ClaseUtilitaria claseUtilitaria = new ClaseUtilitaria();
								
								objInSolicitudRescateReinversion.IdInterno = Id;
								objInSolicitudRescateReinversion.IdCliente = inEntradaReinversion.IdCliente;
								objInSolicitudRescateReinversion.IdPortafolio = inEntradaReinversion.IdPortafolio;
								objInSolicitudRescateReinversion.IdSerieFondo = inDetalleRescateReinversion.IdSerieFondo;
								objInSolicitudRescateReinversion.CalculoPorMontoCuota = inDetalleRescateReinversion.RescateTotal.equalsIgnoreCase("S") ? "T" : (MontoReinversion.compareTo(BigDecimal.ZERO) == 0) ? "C" : "M";
								objInSolicitudRescateReinversion.Monto = MontoReinversion;
								objInSolicitudRescateReinversion.NumeroCuotas = NumeroCuotasReinversion;
								objInSolicitudRescateReinversion.IdMoneda = seriesSolicitudesRe != null && seriesSolicitudesRe.size() > 0 ? seriesSolicitudesRe.get(0).IdMoneda : ""; // Ojo  
								objInSolicitudRescateReinversion.IdMedioDePago = "CIN";
								objInSolicitudRescateReinversion.SolicitudBatch = "N";
								objInSolicitudRescateReinversion.OrigenOperacion = "M";
								objInSolicitudRescateReinversion.Glosa = "INGRESO RESCATE REINVERSION";
								objInSolicitudRescateReinversion.IdAutorizadoOrigen = 0L;
								objInSolicitudRescateReinversion.FolioContrato = secuencias.get(0); //Ojo
								objInSolicitudRescateReinversion.IdAgrupador = 1L;
								objInSolicitudRescateReinversion.IdRegimenTributario = inDetalleRescateReinversion.IdRegimenTributario;
								objInSolicitudRescateReinversion.MontoPatrimonio = inEntradaReinversion.MontoAGF;
								//rescate porcentaje original
								objInSolicitudRescateReinversion.Porcentaje = salidaBuscarCantidadesPorRegimen.P_OUT_RESULTADO.get(0).Porcentaje.multiply(BigDecimal.valueOf(100L), MathContext.DECIMAL128);
								objInSolicitudRescateReinversion.Porcentaje = MonedaFormato.formatear(con, objInSolicitudRescateReinversion.Porcentaje , MonedaFormato.MONTO, objInSolicitudRescateReinversion.IdMoneda).valor;

								claseUtilitaria.FolioContrato = objInSolicitudRescateReinversion.FolioContrato;
								claseUtilitaria.IdInterno = Id;
								
								// Se llena la lista
								listaSolicitudRescateReinversion.add(objInSolicitudRescateReinversion);
								listaClaseUtilitaria.add(claseUtilitaria);
								
								SerieRe = inDetalleRescateReinversion.IdSerieFondo;
							}
							// Fin for detalle reinversion 
							count++;
						}
						
						
						// Se le pasa por parametro la lista recien agregada
 						objInEntradaReinversion.p_SolicitudesRescate = listaSolicitudRescateReinversion;
					}
					// Fin Si validacion con datos detalle
					
 					if(!sumaPorcentaje.equals(new BigDecimal(100))){
						for(ClaseUtilitaria claseUtilitaria : listaClaseUtilitaria){
							// Se crea la lista para cargarla y luego pasarla al proceso
							List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
						
							// Instancia los mensajes de errores
							MensajesErrores mensajesErrores = new MensajesErrores();
						
							// Instancia objeto de salida
							ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
						
							objResultadoOperacion.FolioContrato = claseUtilitaria.FolioContrato;
							objResultadoOperacion.IdSolicitud = claseUtilitaria.IdInterno;
						
							mensajesErrores.IdInterno = claseUtilitaria.IdInterno;
							mensajesErrores.TipoMensaje = "ERROR";
							mensajesErrores.Codigo = "UI_PORCENTAJE_DISTRIBUCION_DIFERENTE_A_MONTO";
							mensajesErrores.Mensaje = "UI_PORCENTAJE_DISTRIBUCION_DIFERENTE_A_MONTO";
						
							listaMensajesErrores.add(mensajesErrores);
						
							objResultadoOperacion.Mensajes = listaMensajesErrores;
							out.P_OUT_ERRORESCONTADOR++;
							listaResultadoOperacion.add(objResultadoOperacion);
							out.P_OUT_RESULTADO = listaResultadoOperacion;
							return out;
						} 
					}
					
					// Si la entrada de aporte viene con info
 					if(inEntradaReinversion.p_DetalleAporteReinversion.size() > 0)
					{
						// Se crea la lista para cargarla y luego pasarla al proceso
 						List<InSolicitudesAporte> listaSolicitudAporteReinversion = new ArrayList<InSolicitudesAporte>();
						
						int secuencia = 0;
						// Por cada Aporte llamo al servicio rescate reinversion de crear solicitudes FFMM
						for(InDetalleAporteReinversion inDetalleAporteReinversion : inEntradaReinversion.p_DetalleAporteReinversion)
						{
							// Setea el Id Interno que es el que permite agrupar las operaciones
							IdInternoSecundario ++;
							series.clear();
							if(!series.contains(inDetalleAporteReinversion.IdSerieFondo)) series.add(inDetalleAporteReinversion.IdSerieFondo); //aki
							
							HashMap<String,Object> params = new HashMap<String,Object>();
							params.put("IdEmpresa", entrada.p_IdEmpresa);
							params.put("IdSerieFondo", series);
							logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
							seriesSolicitudesAp = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
							logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
					        params.clear();
					        
					        //validamos si MLT es en S
					        if (!entrada.p_EntradaReinversion.isEmpty() && 
					        	    entrada.p_EntradaReinversion.get(0).IndicadorMLT != null &&
					        	    "S".equalsIgnoreCase(entrada.p_EntradaReinversion.get(0).IndicadorMLT))
					        {
					        	
					        	params.put("IdSerieFondo", series);
								params.put("IdRegimenFondoMutuo",inDetalleAporteReinversion.IdRegimenTributario);
								logger.debug("INICIO::SQL_SELECT_REGIMEN");
								List<Regimen>  regimenes = sql.query(con, SQL_SELECT_REGIMEN, params, Regimen.class);
								logger.debug("FIN::SQL_SELECT_REGIMEN");
								
								// validamos si es fondo extranjero , en el caso que lo sea el indicador mlt sera N
								if (!regimenes.isEmpty() && 
									    ("S".equals(regimenes.get(0).FondoExtranjero) || "N".equals(regimenes.get(0).PermiteMLT)))
								{
									// Se crea la lista para cargarla y luego pasarla al proceso
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
								
									// Instancia los mensajes de errores
									MensajesErrores mensajesErrores = new MensajesErrores();
								
									// Instancia objeto de salida
									ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
								
									String codigo = "UI_ERROR_INDICADOR_MLT";
									String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).add("IdSerieFondo", regimenes.get(0).IdSerieFondo).add("IdRegimenFondoMutuo",inDetalleAporteReinversion.IdRegimenTributario).msg();										
									
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = MensajeSalida.MENSAJE_ERROR;
									mensajesErrores.Codigo = codigo;
									mensajesErrores.Mensaje = descripcion;
								
									listaMensajesErrores.add(mensajesErrores);
								
									objResultadoOperacion.Mensajes = listaMensajesErrores;
									out.P_OUT_ERRORESCONTADOR++;
									listaResultadoOperacion.add(objResultadoOperacion);
									out.P_OUT_RESULTADO = listaResultadoOperacion;
									return out;
								}
					        }
							
						
							
							
					        // Instancia el detalle del aporte
							InSolicitudesAporte objInSolicitudAporteReinversion = new InSolicitudesAporte();
							BigDecimal porcentaje  = entrada.p_EntradaReinversion.get(0).p_DetalleAporteReinversion.get(secuencia).Porcentaje;
							
							objInSolicitudAporteReinversion.IdInterno = IdInternoSecundario;
							objInSolicitudAporteReinversion.IdCliente = inEntradaReinversion.IdCliente;
							objInSolicitudAporteReinversion.IdPortafolio = inEntradaReinversion.IdPortafolio;
							objInSolicitudAporteReinversion.IdSerieFondo = inDetalleAporteReinversion.IdSerieFondo;
							objInSolicitudAporteReinversion.IdEmisor = ""; // Ojo se obtiene del SerieFondo
							objInSolicitudAporteReinversion.Monto =  MontoReinversion.multiply(porcentaje.divide(new BigDecimal(100)));
							
							//valor del dolar
							EntradaConsultarValorDolarFondoMutuo objInValorDolar = new EntradaConsultarValorDolarFondoMutuo();
							ConsultarValorDolarFondoMutuo ConsultarValorDolar = new ConsultarValorDolarFondoMutuo();
							List<SerieFondoParidad> listaSerie = new ArrayList<SerieFondoParidad>();
							SerieFondoParidad objInSerieFondoParidad = new SerieFondoParidad();

							objInSerieFondoParidad.Fecha = Fecha;
							objInSerieFondoParidad.IdSerieFondo= seriesSolicitudesAp.get(0).IdSerieFondo;
							objInSerieFondoParidad.IdTipoSolicitud = "";
						
							listaSerie.add(objInSerieFondoParidad);
							
							objInValorDolar.p_IdEmpresa = entrada.p_IdEmpresa;
							objInValorDolar.p_IdModulo = "FMM";
							objInValorDolar.p_IdUsuario = entrada.p_IdUsuario;
							objInValorDolar.p_Series = listaSerie;
							
							SalidaConsultarValorDolarFondoMutuo objOutSalidaValorDolar = ConsultarValorDolar.logica(con, objInValorDolar);
							//fin valor dolar
								
							//peso a dolar
							if (!seriesSolicitudesRe.isEmpty() && !seriesSolicitudesAp.isEmpty() &&
								    seriesSolicitudesRe.get(0).IdMoneda != null && 
								    seriesSolicitudesAp.get(0).IdMoneda != null &&
								    seriesSolicitudesRe.get(0).IdMoneda.equals(idMonedaSistema) && 
								    seriesSolicitudesAp.get(0).IdMoneda.equals(_c.Dolar))
							{
								BigDecimal valorDolar  = objOutSalidaValorDolar.P_OUT_RESULTADO.get(0).ValorParidad;
								 porcentaje  = entrada.p_EntradaReinversion.get(0).p_DetalleAporteReinversion.get(secuencia).Porcentaje;
								//BigDecimal MontoCalculado = MontoReinversion.divide(valorDolar, MathContext.DECIMAL128)
								BigDecimal montoCalculado = MontoReinversion.multiply(porcentaje.divide(new BigDecimal(100)).divide(valorDolar, MathContext.DECIMAL128));
										
								objInSolicitudAporteReinversion.Monto = montoCalculado;
								secuencia++;
							}
							
							if (!seriesSolicitudesRe.isEmpty() && !seriesSolicitudesAp.isEmpty() &&
								    seriesSolicitudesRe.get(0).IdMoneda != null && 
								    seriesSolicitudesAp.get(0).IdMoneda != null &&
								    seriesSolicitudesRe.get(0).IdMoneda.equals(_c.Dolar) && 
								    seriesSolicitudesAp.get(0).IdMoneda.equals(idMonedaSistema))
							{
								BigDecimal valorDolar  = objOutSalidaValorDolar.P_OUT_RESULTADO.get(0).ValorParidad;
								 porcentaje  = entrada.p_EntradaReinversion.get(0).p_DetalleAporteReinversion.get(secuencia).Porcentaje;
								//BigDecimal MontoCalculado = MontoReinversion.divide(valorDolar, MathContext.DECIMAL128)
								BigDecimal montoCalculado = MontoReinversion.multiply(porcentaje.divide(new BigDecimal(100)).multiply(valorDolar, MathContext.DECIMAL128));
								
								objInSolicitudAporteReinversion.Monto = montoCalculado;
								secuencia++;
							}
							
							objInSolicitudAporteReinversion.NumeroCuotas = NumeroCuotasReinversion; //este debe cambiar
							objInSolicitudAporteReinversion.IndicadorMLT = inEntradaReinversion.IndicadorMLT;
							

							if (inDetalleAporteReinversion.GeneraSpot  == "N")
							{
	                            HashMap<String,Object> hSeries = Utils.List2Hash(seriesSolicitudesRe, "IdSerieFondo");
	                            SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hSeries.get(SerieRe);
								objInSolicitudAporteReinversion.IdMoneda = serie.IdMoneda;
								
							}
							else 
							{
	                            HashMap<String,Object> hSeries = Utils.List2Hash(seriesSolicitudesAp, "IdSerieFondo");
	                            SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hSeries.get(inDetalleAporteReinversion.IdSerieFondo);
								objInSolicitudAporteReinversion.IdMoneda = serie.IdMoneda; 
							}
							
							objInSolicitudAporteReinversion.IdMedioDePago = "CIN";
							objInSolicitudAporteReinversion.IdRegimenTributario = inDetalleAporteReinversion.IdRegimenTributario; //Ojo
							objInSolicitudAporteReinversion.Glosa = "INGRESO APORTE REINVERSION";
							objInSolicitudAporteReinversion.FolioContrato = 0L; //Ojo
							objInSolicitudAporteReinversion.Porcentaje = inDetalleAporteReinversion.Porcentaje;
							objInSolicitudAporteReinversion.IdAgrupador = 1L;

							listaSolicitudAporteReinversion.add(objInSolicitudAporteReinversion);
						}
						
						// Fin for detalle reinversion 

						// Se le pasa por parametro la lista recien agregada
						objInEntradaReinversion.p_SolicitudesAporte = listaSolicitudAporteReinversion;
					}
					// Fin Si validacion con datos detalle
				} 
				// fin for Encabezado 
				
				// Invoca a la creacion de solicitudes de rescate por Cliente
								
				SalidaCrearSolicitudRescateReinversion1N objOutSolicitudAporteReinversion = objInReinversion1N.logica(con, objInEntradaReinversion);
						
				if(objOutSolicitudAporteReinversion.P_OUT_ERRORESCONTADOR > 0)
				{
					Long errorDetalleId = 0L; 
					for(ClaseUtilitaria claseUtilitaria : listaClaseUtilitaria)
					{
						for(MensajeSalida errorDetalle : objOutSolicitudAporteReinversion.P_OUT_MENSAJES)
						{
							//revisa si IdInterno es negativo
							if( errorDetalle.id < 0) {
								errorDetalleId = errorDetalle.id * -1;
							}else {
								errorDetalleId = errorDetalle.id;
							}
							
							if(claseUtilitaria.IdInterno == errorDetalleId)
							{
								ErroresGlobal++;
								
							
								// Se crea la lista para cargarla y luego pasarla al proceso
								List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
								
								// Instancia los mensajes de errores
								MensajesErrores mensajesErrores = new MensajesErrores();
								
								// Instancia objeto de salida
								ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
								
								objResultadoOperacion.FolioContrato = claseUtilitaria.FolioContrato;
								objResultadoOperacion.IdSolicitud = claseUtilitaria.IdInterno;
								
								mensajesErrores.IdInterno = claseUtilitaria.IdInterno;
								mensajesErrores.TipoMensaje = errorDetalle.tipo;
								mensajesErrores.Codigo = errorDetalle.codigo;
								mensajesErrores.Mensaje = errorDetalle.descripcion;
								
								listaMensajesErrores.add(mensajesErrores);
								
								objResultadoOperacion.Mensajes = listaMensajesErrores;
								
								listaResultadoOperacion.add(objResultadoOperacion);
								
								if(MensajeSalida.MENSAJE_ERROR.equals(mensajesErrores.TipoMensaje)) {
									ErroresContador++;
								}
							}
						}
					}
					
				}
				else
				{
					for (ResultadoSolicitud resultadoSolicitud : objOutSolicitudAporteReinversion.P_OUT_RESULTADO)
					{
						// Se crea la lista para cargarla y luego pasarla al proceso
						List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
						
						// Instancia los mensajes de errores
						MensajesErrores mensajesErrores = new MensajesErrores();
						
						// Instancia objeto de salida
						ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

						objResultadoOperacion.FolioContrato = resultadoSolicitud.FolioContrato;
						objResultadoOperacion.IdSolicitud = resultadoSolicitud.IdSolicitudAporteRescate;
						
						mensajesErrores.IdInterno = 0L;
						mensajesErrores.TipoMensaje = "INFORMACION";
						mensajesErrores.Codigo = "CREACION_REINVERSION_EXITOSA";
						mensajesErrores.Mensaje = "Creacion Reinversion Exitosa";
						
						listaMensajesErrores.add(mensajesErrores);
						
						objResultadoOperacion.Mensajes = listaMensajesErrores;
						
						listaResultadoOperacion.add(objResultadoOperacion);
					}
				}
			} 
			// Fin validacion con datos encabezado 
		} 
		// Fin validacion si es una Reinversion 
		
		
		//***********************************
		//SI ES UNA REINVERSION APV
		//***********************************
		if (entrada.p_TipoProceso.trim().equals("REA"))
		{
			// Si la entrada viene con info
			if(entrada.p_EntradaReinversionAPV.size() > 0)
			{
				// Instancia la clase principal
				CrearSolicitudReinversionPorCliente objInReinversionCliente = new CrearSolicitudReinversionPorCliente();
				
				// Instancia la entrada del rescate
				EntradaCrearSolicitudReinversionPorCliente objInEntradaReinversionCliente = new EntradaCrearSolicitudReinversionPorCliente();

				// Llamar al proceso de invocar la secuencia que es quien relfeja que una operacion es de un mismo grupo
				List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato" , 1L);
				List<String> seriesres = new ArrayList<String>();
				 
				HashMap<String,String> valores = new HashMap<String,String>();
				int secuenciaReinversion = 0;
				
				
				for(InEntradaReinversionAPV inEntradaReinversionAPV : entrada.p_EntradaReinversionAPV) {	
					if(out.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1) {
						return out;
					}
					
					// Si la entrada de rescate viene con info
					if(inEntradaReinversionAPV.p_DetalleRescateReinversionAPV.size() > 0){
						
						for(InDetalleRescateReinversionAPV  inDetalleRescateReinversionAPV : inEntradaReinversionAPV.p_DetalleRescateReinversionAPV) {
							seriesres.add(inDetalleRescateReinversionAPV.IdSerieFondo);
				        }
					}
					secuenciaReinversion++;
				}	
				
				HashMap<String,Object> paramsreinv = new HashMap<String,Object>();
				paramsreinv.put("IdEmpresa", entrada.p_IdEmpresa);
				paramsreinv.put("IdSerieFondo", seriesres);
				logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
				List<SerieFondoSolicitudes> seriesSolicitudesReinv = sql.query(con, SQL_SELECT_SERIE_FONDO, paramsreinv, SerieFondoSolicitudes.class);
				logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
				HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudesReinv, "IdSerieFondo");
				
				
				// Recorro el encabezado
				for(InEntradaReinversionAPV inEntradaReinversionAPV : entrada.p_EntradaReinversionAPV)
				{
					objInEntradaReinversionCliente.p_IdEmpresa = entrada.p_IdEmpresa;
					objInEntradaReinversionCliente.p_IdModulo = "FMM";
					objInEntradaReinversionCliente.p_IdUsuario = entrada.p_IdUsuario; 
					objInEntradaReinversionCliente.p_SimularOperacion = inEntradaReinversionAPV.Simula;
					objInEntradaReinversionCliente.p_UsaReglasNegocio = "N";
					objInEntradaReinversionCliente.p_IdTipoCanalIngreso = inEntradaReinversionAPV.Canal;
					objInEntradaReinversionCliente.p_TtlRescate = inEntradaReinversionAPV.p_DetalleRescateReinversionAPV.get(0).Monto;
					BigDecimal MontoReinversionAPV = BigDecimal.ZERO;
					BigDecimal NumeroCuotasReinversionAPV = BigDecimal.ZERO;
					
					List<String> series = inEntradaReinversionAPV.p_DetalleRescateReinversionAPV.parallelStream().map(solicitud->solicitud.IdSerieFondo).collect(Collectors.toList());
					series.addAll(inEntradaReinversionAPV.p_DetalleAporteReinversionAPV.parallelStream().map(solicitud->solicitud.IdSerieFondo).collect(Collectors.toList()));
					HashMap<String,Object> params = new HashMap<String,Object>();
					params.put("IdEmpresa", entrada.p_IdEmpresa);
					params.put("IdSerieFondo", series);
					logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
					List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
					logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
					Map<String,SerieFondoSolicitudes> mapSeriesSolicitudes = seriesSolicitudes.parallelStream().collect(Collectors.toMap(serie->serie.IdSerieFondo, serie->serie));
					
					
					// Si la entrada de rescate viene con info
					if(inEntradaReinversionAPV.p_DetalleRescateReinversionAPV.size() > 0)
					{
						// Se crea la lista para cargarla y luego pasarla al proceso
						List<cl.bcs.fondomutuo.CrearSolicitudReinversionPorCliente.InSolicitudRescateCliente> listaSolicitudRescateReinversionCliente;
						listaSolicitudRescateReinversionCliente = new ArrayList<cl.bcs.fondomutuo.CrearSolicitudReinversionPorCliente.InSolicitudRescateCliente>();
						
						// Por cada Rescate llamo al servicio rescate reinversion de crear solicitudes FFMM
						for(InDetalleRescateReinversionAPV  inDetalleRescateReinversionAPV : inEntradaReinversionAPV.p_DetalleRescateReinversionAPV)
						{
							// Setea el Id Interno que es el que permite agrupar las operaciones
							IdInterno ++;
							
							//Valida si es rescate total busca el monto total a rescatar
							if (inDetalleRescateReinversionAPV.RescateTotal.equalsIgnoreCase("S")) {
								
								SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(inDetalleRescateReinversionAPV.IdSerieFondo);
									
								// se busca el saldo por grupo regimen
								List<Long> idCliente = new ArrayList<Long>();
								List<Long> idInstrumento = new ArrayList<Long>();
								
								idCliente.add(inEntradaReinversionAPV.IdCliente);
								idInstrumento.add(serieFondo.IdInstrumento);
								
								List<FlujoCustodia> custodias = obtenerCustodia(con,entrada,Fecha,idPaisEmpresa,idCliente,idInstrumento);
								HashMap<String,ClienteGrupoRegimenFondo> hashClienteGrupoRegimenFondo = obtenerTotalesPorClienteGrupoRegimenFondo(custodias);
								String id = "";
								if(custodias.size() == 1) {
									if (!custodias.isEmpty() && custodias.get(0).IdRegimenFondoMutuo == null)
									{
										id = inEntradaReinversionAPV.IdCliente + "_" + serieFondo.IdInstrumento + "_";
									} else {
										id = inEntradaReinversionAPV.IdCliente + "_" + serieFondo.IdInstrumento + "_" + inEntradaReinversionAPV.IdGrupoRegimen;
									}
								} else {
									id = inEntradaReinversionAPV.IdCliente + "_" + serieFondo.IdInstrumento + "_" + inEntradaReinversionAPV.IdGrupoRegimen;
								}
								ClienteGrupoRegimenFondo clienteGrupoRegimenFondo = hashClienteGrupoRegimenFondo.get(id);
								if (clienteGrupoRegimenFondo != null) {
									NumeroCuotasReinversionAPV = clienteGrupoRegimenFondo.Total;
								} else {
									NumeroCuotasReinversionAPV = BigDecimal.ZERO;
								}
												
							}else {
								MontoReinversionAPV = inDetalleRescateReinversionAPV.Monto;
								NumeroCuotasReinversionAPV = inDetalleRescateReinversionAPV.NumeroCuotas;
							}
							
							
							// Instancia el detalle del rescate
							cl.bcs.fondomutuo.CrearSolicitudReinversionPorCliente.InSolicitudRescateCliente objInSolicitudRescateReinversionCliente = new cl.bcs.fondomutuo.CrearSolicitudReinversionPorCliente.InSolicitudRescateCliente();
							ClaseUtilitaria claseUtilitaria = new ClaseUtilitaria();
							SerieFondoSolicitudes serie = mapSeriesSolicitudes.get(inDetalleRescateReinversionAPV.IdSerieFondo);
							objInSolicitudRescateReinversionCliente.IdInterno = IdInterno;
							objInSolicitudRescateReinversionCliente.FolioContrato = secuencias.get(0);
							objInSolicitudRescateReinversionCliente.IdCliente = inEntradaReinversionAPV.IdCliente;
							objInSolicitudRescateReinversionCliente.IdPortafolio = inDetalleRescateReinversionAPV.IdPortafolio;
							objInSolicitudRescateReinversionCliente.IdSerieFondo = inDetalleRescateReinversionAPV.IdSerieFondo;
							objInSolicitudRescateReinversionCliente.CalculoPorMontoCuota = inDetalleRescateReinversionAPV.RescateTotal.equalsIgnoreCase("S") ? "T" : (MontoReinversionAPV.compareTo(BigDecimal.ZERO) == 0) ? "C" : "M";
							objInSolicitudRescateReinversionCliente.Monto = MontoReinversionAPV;
							objInSolicitudRescateReinversionCliente.NumeroCuotas = NumeroCuotasReinversionAPV;
							objInSolicitudRescateReinversionCliente.IdMoneda = serie != null ? serie.IdMoneda : "";
							objInSolicitudRescateReinversionCliente.IdGrupoRegimen = inEntradaReinversionAPV.IdGrupoRegimen;
							objInSolicitudRescateReinversionCliente.Glosa = "INGRESO RESCATE REINVERSION APV";
							objInSolicitudRescateReinversionCliente.MontoPatrimonio = inEntradaReinversionAPV.MontoAGF;
							
							claseUtilitaria.FolioContrato = objInSolicitudRescateReinversionCliente.FolioContrato;
							claseUtilitaria.IdInterno = IdInterno;

							// Se llena el arreglo
							listaSolicitudRescateReinversionCliente.add(objInSolicitudRescateReinversionCliente);
							listaClaseUtilitaria.add(claseUtilitaria);
						}
						// Fin for detalle reinversion 
						
						// Se le pasa por parametro la lista recien agregada
						objInEntradaReinversionCliente.p_SolicitudesRescate = listaSolicitudRescateReinversionCliente;
					} 
					// Fin Si validacion con datos detalle

					// Si la entrada de aporte viene con info
					if(inEntradaReinversionAPV.p_DetalleAporteReinversionAPV.size() > 0)
					{
						// Se crea la lista para cargarla y luego pasarla al proceso
						List<InSolicitudAporteCliente> listaSolicitudAporteReinversionCliente;
						listaSolicitudAporteReinversionCliente = new ArrayList<InSolicitudAporteCliente>();
						
						// Por cada Aporte llamo al servicio rescate reinversion de crear solicitudes FFMM
						for(InDetalleAporteReinversion  inDetalleAporteReinversionAPV : inEntradaReinversionAPV.p_DetalleAporteReinversionAPV)
						{
							// Setea el Id Interno que es el que permite agrupar las operaciones
							IdInternoSecundario ++;

							// Instancia el detalle del aporte
							InSolicitudAporteCliente objInSolicitudAporteReinversionCliente = new InSolicitudAporteCliente(); //aki vuelve
							ClaseUtilitaria claseUtilitaria = new ClaseUtilitaria();
							SerieFondoSolicitudes serie = mapSeriesSolicitudes.get(inDetalleAporteReinversionAPV.IdSerieFondo);
							objInSolicitudAporteReinversionCliente.IdInterno = IdInterno;
							objInSolicitudAporteReinversionCliente.FolioContrato = 0L; //Ojo
							objInSolicitudAporteReinversionCliente.IdSerieFondo = inDetalleAporteReinversionAPV.IdSerieFondo;
							objInSolicitudAporteReinversionCliente.IdMoneda = serie != null ? serie.IdMoneda : ""; //Ojo
							objInSolicitudAporteReinversionCliente.Porcentaje = inDetalleAporteReinversionAPV.Porcentaje;
							//objInSolicitudAporteReinversionCliente.Montottl = (inDetalleAporteReinversionAPV.Porcentaje.multiply(MontoReinversionAPV)).divide((new BigDecimal(100)), MathContext.DECIMAL128).setScale(0, RoundingMode.HALF_DOWN); // aki ejemplo decalculo total 
							
							claseUtilitaria.FolioContrato = 0L;
							claseUtilitaria.IdInterno = IdInternoSecundario;
							
							// Se llena la lista
							listaSolicitudAporteReinversionCliente.add(objInSolicitudAporteReinversionCliente);
							listaClaseUtilitaria.add(claseUtilitaria);
						} 
						// Fin for detalle reinversion APV
						
						// Se le pasa por parametro la lista recien agregada
						objInEntradaReinversionCliente.p_SolicitudesAporte = listaSolicitudAporteReinversionCliente;
					} 
					// Fin Si validacion con datos detalle
				} 
				// fin for Encabezado 

				// Invoca a la creacion de solicitudes de rescate por Cliente
				SalidaCrearSolicitudReinversionPorCliente objOutSolicitudAporteReinversionCliente = objInReinversionCliente.logica(con, objInEntradaReinversionCliente);
				
				if(objOutSolicitudAporteReinversionCliente.P_OUT_ERRORESCONTADOR > 0)
				{
					Long errorDetalleId = 0L;
					for(ClaseUtilitaria claseUtilitaria : listaClaseUtilitaria)
					{
						for(MensajeSalida errorDetalle : objOutSolicitudAporteReinversionCliente.P_OUT_MENSAJES)
						{
							//revisa si idinterno es negativo
							if(errorDetalle.id < 0) {
								errorDetalleId = errorDetalle.id *-1;
							}else {
								errorDetalleId = errorDetalle.id;
							}
							
							if(claseUtilitaria.IdInterno == errorDetalleId ||errorDetalleId.equals(0L) )
							{
								
								// Se crea la lista para cargarla y luego pasarla al proceso
								List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
								
								// Instancia los mensajes de errores
								MensajesErrores mensajesErrores = new MensajesErrores();
								
								// Instancia objeto de salida
								ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
								
								objResultadoOperacion.FolioContrato = claseUtilitaria.FolioContrato;
								objResultadoOperacion.IdSolicitud = claseUtilitaria.IdInterno;
								
								mensajesErrores.IdInterno = claseUtilitaria.IdInterno;
								mensajesErrores.TipoMensaje = errorDetalle.tipo;
								mensajesErrores.Codigo = errorDetalle.codigo;
								mensajesErrores.Mensaje = errorDetalle.descripcion;
								
								listaMensajesErrores.add(mensajesErrores);
								
								objResultadoOperacion.Mensajes = listaMensajesErrores;
								
								listaResultadoOperacion.add(objResultadoOperacion);
								
								if(MensajeSalida.MENSAJE_ERROR.equals(mensajesErrores.TipoMensaje)) {
									ErroresContador++;
								}
								
							}
						}
					}
				}
				else
				{
					for (ResultadoSolicitud resultadoSolicitud : objOutSolicitudAporteReinversionCliente.P_OUT_RESULTADO)
					{
						// Se crea la lista para cargarla y luego pasarla al proceso
						List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
						
						// Instancia los mensajes de errores
						MensajesErrores mensajesErrores = new MensajesErrores();
						
						// Instancia objeto de salida
						ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

						objResultadoOperacion.FolioContrato = resultadoSolicitud.FolioContrato;
						objResultadoOperacion.IdSolicitud = resultadoSolicitud.IdSolicitudAporteRescate;
						
						mensajesErrores.IdInterno = 0L;
						mensajesErrores.TipoMensaje = "INFORMACION";
						mensajesErrores.Codigo = "CREACION_REINVERSION_EXITOSA";
						mensajesErrores.Mensaje = "Creacion Reinversion Exitosa";
						
						listaMensajesErrores.add(mensajesErrores);
						
						objResultadoOperacion.Mensajes = listaMensajesErrores;
						
						listaResultadoOperacion.add(objResultadoOperacion);
					}
				}
			} 
			// Fin validacion con datos encabezado 
		} 
		// Fin validacion si es una Reinversion APV
		
		out.P_OUT_ERRORESCONTADOR = ErroresContador;
		out.P_OUT_RESULTADO = listaResultadoOperacion;
		if ("RC".equals(entrada.p_TipoProceso) || "RCA".equals(entrada.p_TipoProceso)) {
			out.P_OUT_RESULTADO_PORTAFOLIO = obtenerResultadosPortafolios(out.P_OUT_RESULTADO);
		}

		logger.info("FIN::ProcesarReinversionRescate");
		return out;
	}


	private List<ResultadoPortafolio> obtenerResultadosPortafolios(List<ResultadoOperacion> resultadosOperaciones) {
		logger.trace("INICIO:: ProcesarReinversionRescate.obtenerResultadosPortafolios");
		long inicio = System.currentTimeMillis();

		Map<Long, ResultadoPortafolio> resultadoPortafolioPorIdPortafolio = new HashMap<>();
		for (ResultadoOperacion resultadoOperacion : resultadosOperaciones) {
			if (resultadoOperacion.IdPortafolio != null && resultadoOperacion.MontoComisionMonedaSistema != null) {
				ResultadoPortafolio resultadoPortafolio = resultadoPortafolioPorIdPortafolio.computeIfAbsent(resultadoOperacion.IdPortafolio, ResultadoPortafolio::new);

				resultadoPortafolio.TotalComisionesMonedaSistema = resultadoPortafolio.TotalComisionesMonedaSistema.add(resultadoOperacion.MontoComisionMonedaSistema);
			}
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarReinversionRescate.obtenerResultadosPortafolios {}", fin - inicio);

		return new ArrayList<>(resultadoPortafolioPorIdPortafolio.values());
	}


	private HashMap<String, ClienteGrupoRegimenFondo> obtenerTotalesPorClienteGrupoRegimenFondo(
			List<FlujoCustodia> custodias) {
		logger.info("INICIO::BuscarCantidadesPorRegimen.obtenerTotalesPorClienteGrupoRegimenFondo");
		HashMap<String,ClienteGrupoRegimenFondo> resultado = new HashMap<String,ClienteGrupoRegimenFondo>();
		custodias.stream().forEach(custodia->{
			String id = custodia.IdCliente + "_" + custodia.IdInstrumento + "_" + custodia.GrupoRegimen;
			ClienteGrupoRegimenFondo clienteGrupoRegimenFondo = resultado.get(id);
			if(clienteGrupoRegimenFondo == null) {
				clienteGrupoRegimenFondo = new ClienteGrupoRegimenFondo();
				clienteGrupoRegimenFondo.IdCliente = custodia.IdCliente;
				clienteGrupoRegimenFondo.IdInstrumento = custodia.IdInstrumento;
				clienteGrupoRegimenFondo.IdSerieFondo = custodia.Nemotecnico;
				clienteGrupoRegimenFondo.Total = custodia.Cantidad;
			}else {
				clienteGrupoRegimenFondo.Total = clienteGrupoRegimenFondo.Total.add(custodia.Cantidad, MathContext.DECIMAL128);
			}
			resultado.put(id, clienteGrupoRegimenFondo);
		});
		logger.info("FIN::BuscarCantidadesPorRegimen.obtenerTotalesPorClienteGrupoRegimenFondo");
		return resultado;
	}
	
	
	/*flujo custodia por gruporegimen*/
	private List<FlujoCustodia> obtenerCustodia(Connection con, EntradaProcesarReinversionRescate entrada, String fecha,
			Long idPaisEmpresa,  List<Long> idCliente,  List<Long> idInstrumento) throws Exception{
		// TODO Auto-generated method stub
		ConsultarFlujoCustodia flujoCustodia = new ConsultarFlujoCustodia();
		FlujoCustodiaEntrada flujoCustodiaEntrada = new FlujoCustodiaEntrada();
		flujoCustodiaEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		flujoCustodiaEntrada.p_IdUsuario = entrada.p_IdUsuario;
		flujoCustodiaEntrada.p_IdModulo = "FMM";
		flujoCustodiaEntrada.p_FechaSaldo = fecha;
		flujoCustodiaEntrada.p_IdPais = idPaisEmpresa;
		flujoCustodiaEntrada.p_DiasProyeccion = 0;
		flujoCustodiaEntrada.p_DiaHabil = "S";
		flujoCustodiaEntrada.p_ValorizarFlujo = "N";
		
		flujoCustodiaEntrada.p_Uso = idCliente.parallelStream().map(cliente->{
			FlujoUso uso = new FlujoUso();
			uso.UsoTipoOperacion = "RESCFM";
			uso.IdCliente = cliente;
			uso.IdPortafolio = null;
			uso.BusquedaEspecifica = "";
			uso.IdTipoInstrumentoEspecifico = "";
			return uso;
		}).collect(Collectors.toList());
		
		flujoCustodiaEntrada.p_FiltrosBusqueda = idInstrumento.parallelStream().map(instrumento->{
			FlujoFiltrosBusqueda busqueda = new FlujoFiltrosBusqueda();
			busqueda.IdClasificacionTipoInstrumento = "FM";
			busqueda.IdInstrumento = instrumento;
			return busqueda;
		}).collect(Collectors.toList());
		
		FlujoEsAgrupadoPor agrupador = new FlujoEsAgrupadoPor();
		agrupador.ClasificacionTipoInstrumento = "S";
		agrupador.TipoInstrumento = "S";
		agrupador.Instrumento = "S";
		agrupador.IdRegimenFondoMutuo = "S";
		agrupador.GrupoRegimen = "S";
		agrupador.Emisor = "N";
		agrupador.Nemotecnico = "N";
		agrupador.NemoSVS = "N";
		agrupador.FechaEmisionInstrumento = "N";
		agrupador.FechaVencimientoInstrumento = "N";
		agrupador.FechaLiquidacionInstrumento = "N";
		agrupador.TipoReajuste = "N";
		agrupador.EsFungible = "N";
		agrupador.CodigoIFRS = "N";
		agrupador.Boveda = "N";
		agrupador.Bolsa = "N";
		agrupador.TipoOperacion = "N";
		agrupador.Mesa = "N";
		agrupador.FolioRegistro = "N";
		agrupador.ItemOrigen = "N";
		flujoCustodiaEntrada.p_EsAgrupadoPor.add(agrupador);
		
		FlujoCustodiaSalida salida = flujoCustodia.logica(con, flujoCustodiaEntrada);
		logger.info("FIN::BuscarCantidadesPorRegimen.obtenerCustodia");
		
		return salida.p_out_FlujoTotalizado;
	}
	
	private SalidaConsultarFlujoCustodiaFondoMutuo obtenerCustodiaPortafolio(Connection con, EntradaProcesarReinversionRescate entrada, String fecha,
			Long idPaisEmpresa,  List<InCarteras> p_Carteras, List<InInstrumento> p_Instrumentos ) throws Exception{
		
		ConsultarFlujoCustodiaFondoMutuo flujo = new ConsultarFlujoCustodiaFondoMutuo();
		EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo = new EntradaConsultarFlujoCustodiaFondoMutuo();
		entradaFlujo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFlujo.p_IdUsuario = entrada.p_IdUsuario;
		entradaFlujo.p_IdModulo = "FMM";
		entradaFlujo.p_IdPais = idPaisEmpresa;
		entradaFlujo.p_FechaSaldo = fecha;
		entradaFlujo.p_DiasProyeccion = 0;
		entradaFlujo.p_Carteras.addAll(p_Carteras);
		entradaFlujo.p_Instrumentos.addAll(p_Instrumentos);
		
	    SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo1 = flujo.logica(con, entradaFlujo);
		return salidaFlujo1;
	}
	
	/*
	 * Va en busqueda de un ID de preferencia de pago registrada en la tabla de PreferenciadePagoPortafolio
	 * JALA al 14-05-2019
	 */
	public Long verificaPreferenciaPago(Connection conn,
			PreferenciaPagoVerificar preferenciaPagoVerificar,
			List<ResultadoOperacion> listaResultadoOperacion) throws Exception {
		
		Long idPreferenciaPagoEncontrada = 0L;
		
		//se envio el parametro de IdPreferencia de Pago
		if(preferenciaPagoVerificar.IdPreferenciaPagoPortafolio > 0L) {
			idPreferenciaPagoEncontrada = buscarPreferenciaPagoPortafolioPorId(conn, preferenciaPagoVerificar.IdCliente, preferenciaPagoVerificar.IdPreferenciaPagoPortafolio, listaResultadoOperacion);
		}
		else { //si no viene el valor lo consulta por otros campos, sino lo crea
			
			//consulta todos los portafolio asociados al cliente, o el que se paso por parametro
			//esto permite ver por cada portafolio si esta creada la preferencia de pago que cumple con las caracterosticas
			//pasadas por parametro
			List<UtiPortafolio> portafoliosCliente = buscarPortafoliosCliente(conn, preferenciaPagoVerificar.IdCliente, preferenciaPagoVerificar.IdPortafolio);
		
			//encontro portafolio
			if(portafoliosCliente.size()>0) {
				
				Map<Long, List<UtiPreferenciaPagoPortafolio>> preferenciasPagoAgrupadaPorPortafolio = buscarPreferenciaPagoAgrupadasPorPortafolio(conn, preferenciaPagoVerificar, portafoliosCliente);
				List<UtiPortafolio> idPortafoliosCreacionPreferenciaPago = new ArrayList<UtiPortafolio>();
				
				for(UtiPortafolio portafolioCliente : portafoliosCliente) {
					List<UtiPreferenciaPagoPortafolio> preferenciasPagoPortafolio = preferenciasPagoAgrupadaPorPortafolio.get(portafolioCliente.IdPortafolio);
					//encontro una preferenia de pago de acuerdo a las otras caracteristicas pasadas
					if(preferenciasPagoPortafolio != null) {
						//guardo el ultimo IdPreferenciaPagoPortafolio encontrado para que retorne la funcion
						idPreferenciaPagoEncontrada = preferenciasPagoPortafolio.get(0).IdPreferenciaPagoPortafolio;
					}
					else {
						idPortafoliosCreacionPreferenciaPago.add(portafolioCliente);
					}//FIN else : preferenciasPagoPortafolio.size
		
				}//FIN for (recorre portafolios asociados al cliente)
				if(idPortafoliosCreacionPreferenciaPago.size() > 0) {
					idPreferenciaPagoEncontrada = crearPreferenciaPagoPortafolios(conn, preferenciaPagoVerificar, idPortafoliosCreacionPreferenciaPago, listaResultadoOperacion);
				}
			} //FIN If Portafolios encontrados
			else 
			{ //no existe Portafolio para el cliente-portafolio indicado
			
				GeneraElementosError(0L,
						"ERROR",
						"PORTAFOLIO_NO_EXISTE",
						"No se encontraron portafolios para el cliente ingresado o IdPortafolio no pertenece a Cliente", 
						listaResultadoOperacion);
				
			
			}//
				
			
		}
			
		return idPreferenciaPagoEncontrada;
	}
	
	private Long buscarPreferenciaPagoPortafolioPorId(Connection conn, Long idCliente, Long idPreferenciaPagoPortafolio, List<ResultadoOperacion> listaResultadoOperacion) throws Exception {
		logger.info("INICIO::ProcesarReinversionRescate.buscarPreferenciaPagoPortafolioPorId");
		SQL sql = new SQL();
		Long idPreferenciaPagoEncontrada = 0L;
		//se valida que exista en las tablas de preferencia de pago
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("p_IdCliente",idCliente);
		parametros.put("p_IdPreferenciaPagoPortafolio", idPreferenciaPagoPortafolio);
			
		try {
			logger.debug("INICIO::SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO");
			List<UtiPreferenciaPagoPortafolio> preferenciasPagoPortafolio = sql.query(conn, SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO, parametros, UtiPreferenciaPagoPortafolio.class);
			logger.debug("FIN::SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO");
			if(preferenciasPagoPortafolio.size()>0) {
				idPreferenciaPagoEncontrada = preferenciasPagoPortafolio.get(0).IdPreferenciaPagoPortafolio;
			}
			else {
				GeneraElementosError(0L,
						"ERROR",
						"ID_PREFERENCIA_PAGO_PORTAFOLIO_NO_EXISTE",
						"El valor dado a IdPreferenciaPagoPortafolio no esta registrado", 
						listaResultadoOperacion);
			}
			
		}catch (Exception e) {
			logger.error("ERROR::CrearSolicitudRescateReinversion.verificaPreferenciaPago", e);
			throw e;
		}
		
		logger.info("FIN::ProcesarReinversionRescate.buscarPreferenciaPagoPortafolioPorId");
		return idPreferenciaPagoEncontrada;
	}
	
	private List<UtiPortafolio> buscarPortafoliosCliente(Connection conn, Long idCliente, Long idPortafolio) throws SQLException{
		logger.info("INICIO::ProcesarReinversionRescate.buscarPortafoliosCliente");
		SQL sql = new SQL();
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("p_IdCliente",idCliente);
		parametros.put("p_IdPortafolio", idPortafolio);
		logger.debug("INICIO::SQL_SELECT_PORTAFOLIOS");
		List<UtiPortafolio> lstPortafolios = sql.query(conn, SQL_SELECT_PORTAFOLIOS, parametros, UtiPortafolio.class);
		logger.debug("FIN::SQL_SELECT_PORTAFOLIOS");
		logger.info("FIN::ProcesarReinversionRescate.buscarPortafoliosCliente");
		
		return lstPortafolios;
	}
	
	private Map<Long,List<UtiPreferenciaPagoPortafolio>> buscarPreferenciaPagoAgrupadasPorPortafolio(Connection conn, PreferenciaPagoVerificar preferenciaPagoVerificar, List<UtiPortafolio> portafoliosCliente) throws SQLException{
		logger.info("INICIO::ProcesarReinversionRescate.buscarPreferenciaPagoAgrupadasPorPortafolio");
		
		SQL sql = new SQL();
		List<Long> idPortafoliosBusqueda = portafoliosCliente.parallelStream().map(portafolio->portafolio.IdPortafolio).collect(Collectors.toList());
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("p_IdCliente",preferenciaPagoVerificar.IdCliente);
		parametros.put("p_IdPortafolio", idPortafoliosBusqueda);
		parametros.put("p_IdTipoOperacion", preferenciaPagoVerificar.IdTipoOperacion);
		parametros.put("p_IdMedioDePago", preferenciaPagoVerificar.IdMedioDePago);
		parametros.put("p_IdMoneda", preferenciaPagoVerificar.IdMoneda);
		parametros.put("p_IdCuentaBancaria", preferenciaPagoVerificar.IdCuentaBancaria);
		parametros.put("p_IdEmpresa", preferenciaPagoVerificar.IdEmpresa);
		logger.debug("INICIO::SQL_SELECT_CONSULTA_PREFERENCIA_PAGO_PORTAFOLIO");
		List<UtiPreferenciaPagoPortafolio> preferenciasPagoPortafolio = sql.query(conn, SQL_SELECT_CONSULTA_PREFERENCIA_PAGO_PORTAFOLIO, parametros, UtiPreferenciaPagoPortafolio.class);
		logger.debug("FIN::SQL_SELECT_CONSULTA_PREFERENCIA_PAGO_PORTAFOLIO");
		Map<Long,List<UtiPreferenciaPagoPortafolio>> preferenciasPagoPortafolioAgrupadas = preferenciasPagoPortafolio.parallelStream().collect(Collectors.groupingBy(preferenciaPago->preferenciaPago.IdPortafolio));
		
		logger.info("FIN::ProcesarReinversionRescate.buscarPreferenciaPagoAgrupadasPorPortafolio");
		
		return preferenciasPagoPortafolioAgrupadas;
		
	}
	
	private Long crearPreferenciaPagoPortafolios(Connection conn, PreferenciaPagoVerificar preferenciaPagoVerificar, List<UtiPortafolio> portafolios, List<ResultadoOperacion> listaResultadoOperacion) throws Exception {
		logger.info("INICIO::ProcesarReinversionRescate.crearPreferenciaPagoPortafolios");
		Long idPreferenciaPagoEncontrada = 0L;
		List<UtiPreferenciaPago> preferenciasPago = buscarPreferenciasPago(conn, preferenciaPagoVerificar);
		//como no la encontro la intenta crear con el servicio determinado
		//busca que exista la combinacion de dada por parametro configurada en las preferencia de pago
		//encontro una preferencia de pago configurada?
		if(preferenciasPago.size()>0) { 
			//va a crear una preferencia de pago de portafolio con el codigo de preferencia encontrado en la configuracion
			//defino datos de entrada 
			IngresaPreferenciaPagoPortafolio_Entrada entradaIngresaPreferenciaPagoPortafolio = new IngresaPreferenciaPagoPortafolio_Entrada();
			entradaIngresaPreferenciaPagoPortafolio.p_IdEmpresa = preferenciaPagoVerificar.IdEmpresa;
			entradaIngresaPreferenciaPagoPortafolio.p_IdModulo = "SOC";
			entradaIngresaPreferenciaPagoPortafolio.p_IdUsuario = preferenciaPagoVerificar.IdUsuario; 
			entradaIngresaPreferenciaPagoPortafolio.p_UsaReglasNegocio = "N";
			
			entradaIngresaPreferenciaPagoPortafolio.p_PreferenciaPagoPortafolioIng = portafolios.parallelStream().map(portafolio->{
				PreferenciaPagoPortafolio preferenciaPagoPortafolioCrear = new PreferenciaPagoPortafolio();
				
				preferenciaPagoPortafolioCrear.IdPortafolio = portafolio.IdPortafolio; //IdPortafolio;
				preferenciaPagoPortafolioCrear.IdPreferenciaPago = preferenciasPago.get(0).IdPreferenciaPago;
				preferenciaPagoPortafolioCrear.IdCuentaBancaria = preferenciaPagoVerificar.IdCuentaBancaria;
				preferenciaPagoPortafolioCrear.IdEstado = "ACT";
				preferenciaPagoPortafolioCrear.IdTarjetaBancaria = 0L;
				preferenciaPagoPortafolioCrear.PorDefecto = "N";
				
				return preferenciaPagoPortafolioCrear;
			}).collect(Collectors.toList());
			
			//defino el objeto de la clase que contiene el metodo logica
			IngresaPreferenciaPagoPortafolio ingresaPreferenciaPagoPortafolio = new IngresaPreferenciaPagoPortafolio();

			//ejecuto el metodo logica y obtengo el objeto de salida
			IngresaPreferenciaPagoPortafolio_Salida salidaIngresaPreferenciaPagoPortafolio = ingresaPreferenciaPagoPortafolio.logica(conn, entradaIngresaPreferenciaPagoPortafolio);
			
			//el proceso fue exitoso
			if (salidaIngresaPreferenciaPagoPortafolio.P_OUT_ERRORESCONTADOR==0L) {
				
				//entonces va a recuperar el Id de Preferencia de Pago generado
				
				if(salidaIngresaPreferenciaPagoPortafolio.P_OUT_RESULTADOPROCESO.size()>0) {
					//guardo el ultimo IdPreferenciaPagoPortafolio encontrado para que retorne la funcion
					idPreferenciaPagoEncontrada = salidaIngresaPreferenciaPagoPortafolio.P_OUT_RESULTADOPROCESO.get(0).RefId;
				}
				else 
				{
					//indica tambien que no se pudo crear la preferencia de pago de portafolio
					GeneraElementosError(0L,
							"ERROR",
							"ID_PREFERENCIA_PAGO_PORTAFOLIO_NO_FUE_POSIBLE_DE_CREAR_ID",
							"Se intento crear la Preferencia de Pago de Portafolio pero el servicio especifico no retorno el ID", 
							listaResultadoOperacion);
				}
			}
			else
			{
				//tiene que indicar que hubo error al intentar crear la preferencia de pago portafolio
				GeneraElementosError(0L,
						"ERROR",
						"ID_PREFERENCIA_PAGO_PORTAFOLIO_NO_FUE_POSIBLE_DE_CREAR",
						"Se intento crear la Preferencia de Pago de Portafolio pero el servicio especifico arrojo error", 
						listaResultadoOperacion);
			}
			
			
		}
		else
		{
			//reporta el error que no puede crear una preferencia de pago de portafolio
			GeneraElementosError(0L,
					"ERROR",
					"ID_PREFERENCIA_PAGO_PORTAFOLIO_PREFERENCIA_NO_CONFIGURADA",
					"Se busca alguna preferencia de pago para crear una preferencia de pago de portafolio pero no hay configuracion", 
					listaResultadoOperacion);
			
			
		}
		
		logger.info("FIN::ProcesarReinversionRescate.crearPreferenciaPagoPortafolios");
		
		return idPreferenciaPagoEncontrada;
	}
	
	private List<UtiPreferenciaPago> buscarPreferenciasPago(Connection conn, PreferenciaPagoVerificar preferenciaPagoVerificar) throws SQLException{
		logger.info("INICIO::ProcesarReinversionRescate.buscarPreferenciasPago");
		
		SQL sql = new SQL();
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("p_IdTipoOperacion", preferenciaPagoVerificar.IdTipoOperacion);
		parametros.put("p_IdMedioDePago", preferenciaPagoVerificar.IdMedioDePago);
		parametros.put("p_IdMoneda", preferenciaPagoVerificar.IdMoneda);
		parametros.put("p_IdEmpresa", preferenciaPagoVerificar.IdEmpresa);
		logger.debug("INICIO::SQL_SELECT_CONSULTA_PREFERENCIA_PAGO");
		List<UtiPreferenciaPago> preferenciasPago = sql.query(conn, SQL_SELECT_CONSULTA_PREFERENCIA_PAGO, parametros, UtiPreferenciaPago.class);
		logger.debug("FIN::SQL_SELECT_CONSULTA_PREFERENCIA_PAGO");
		logger.info("FIN::ProcesarReinversionRescate.buscarPreferenciasPago");
		
		return preferenciasPago;
	}
	
	private Boolean esRegimenValido(String idRegimenTributario) {
		logger.info("INICIO::ProcesarReinversionRescate.esRegimenValido");
		Boolean esRegimenValido = idRegimenTributario != null && !idRegimenTributario.isEmpty();
		logger.info("INICIO::ProcesarReinversionRescate.esRegimenValido");
		return esRegimenValido;
	}
	
	private Boolean noEsRegimenValido(String idRegimenTributario) {
		logger.info("INICIO::ProcesarReinversionRescate.noEsRegimenValido");
		Boolean noEsRegimenValido = idRegimenTributario == null || idRegimenTributario.isEmpty();
		logger.info("INICIO::ProcesarReinversionRescate.noEsRegimenValido");
		return noEsRegimenValido;
	}
	
	/*
	 * Permite generar objeto de mensajes de error
	 * JALA al 14-05-2019
	 */
	private void GeneraElementosError(Long p_IdInterno, String p_TipoMensaje, String p_Codigo, String p_Mensaje, List<ResultadoOperacion> listaResultadoOperacion) throws Exception {
		
		// Se crea la lista para cargarla y luego pasarla al proceso
		List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
		
		// Instancia los mensajes de errores
		MensajesErrores mensajesErrores = new MensajesErrores();
		
		// Instancia objeto de salida
		ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
		
		mensajesErrores.IdInterno = p_IdInterno;
		mensajesErrores.TipoMensaje = p_TipoMensaje;
		mensajesErrores.Codigo = p_Codigo;
		mensajesErrores.Mensaje = p_Mensaje;
		
		listaMensajesErrores.add(mensajesErrores);
		
		objResultadoOperacion.Mensajes = listaMensajesErrores;
		
		listaResultadoOperacion.add(objResultadoOperacion);
		
	}

	private List<Validacion> validarIdsInternos(Connection conn, Long idEmpresa, List<Long> idsInternos) throws Exception {
		List<Validacion> validaciones = new ArrayList<>(idsInternos.size());
		Set<Long> idsInternosUsados = new HashSet<>(idsInternos.size());

		for (Long idInterno : idsInternos) {
			if (idInterno == null) {
				validaciones.add(new Validacion(true, new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ID_INTERNO_NULL", new HashMap<>(0), conn, idEmpresa)));
			}
			if (idsInternosUsados.add(idInterno) == false) {
				validaciones.add(new Validacion(true, new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ID_INTERNO_DUPLICADO", new HashMap<>(0), conn, idEmpresa)));
			}
		}

		return validaciones;
	}

	private String obtenerDescripcionDiccionario(Connection conn, Long idEmpresa, String codigo, HashMap<String, String> parametros) {
		String descripcion;
		try {
			descripcion = Diccionario.getMensaje(conn, idEmpresa, codigo, parametros);
		} catch (Exception ex) {
			descripcion = codigo + " sin traduccion";
		}

		return descripcion;
	}
	
	private String obtenerMonedaRescate(SerieFondoSolicitudes serieFondo, String monedaSistema, Long idPaisEmpresa) {
		String idMoneda = monedaSistema;
		if(serieFondo != null && !_c.Pesos.equals(serieFondo.IdMoneda) && idPaisEmpresa.equals(serieFondo.IdPaisEmisor)) {
			idMoneda = _c.Dolar;
		}
		return idMoneda;
	}
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception
	{
		Gson gson = new Gson();		
		EntradaProcesarReinversionRescate jin = gson.fromJson(in.toString(), EntradaProcesarReinversionRescate.class);
		SalidaProcesarReinversionRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args) throws Exception
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties"))
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"25\",\"p_ClaveUsuario\":\"\",\"p_TipoProceso\":\"RE\",\"p_EntradaReinversion\":[{\"IdPortafolio\":421026,\"Glosa\":\"PruebaReinver\",\"Simula\":\"S\",\"IndicadorMLT\":\"N\",\"MontoAGF\":5000,\"IdCliente\":67968,\"p_DetalleRescateReinversion\":[{\"IdSerieFondo\":\"00229ACLP\",\"IdRegimenTributario\":\"18Q\",\"Monto\":5000,\"NumeroCuotas\":0}],\"p_DetalleAporteReinversion\":[{\"IdSerieFondo\":\"00069ACLP\",\"Porcentaje\":50,\"IdRegimenTributario\":\"18Q\",\"Switch\":\"N\",\"GeneraSpot\":\"N\"},{\"IdSerieFondo\":\"BTCHIL-ACA\",\"Porcentaje\":50,\"IdRegimenTributario\":\"18T\",\"Switch\":\"N\",\"GeneraSpot\":\"N\"}]}]}";
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"25\",\"p_ClaveUsuario\":\"\",\"p_TipoProceso\":\"RE\",\"p_EntradaReinversion\":[{\"IdPortafolio\":421026,\"Glosa\":\"PruebaReinver\",\"Simula\":\"S\",\"IndicadorMLT\":\"N\",\"MontoAGF\":0,\"IdCliente\":67968,\"p_DetalleRescateReinversion\":[{\"IdSerieFondo\":\"00229ACLP\",\"IdRegimenTributario\":\"18Q\",\"Monto\":5000,\"NumeroCuotas\":0}],\"p_DetalleAporteReinversion\":[{\"IdSerieFondo\":\"00069ACLP\",\"Porcentaje\":100,\"IdRegimenTributario\":\"18Q\",\"Switch\":\"N\",\"GeneraSpot\":\"N\"}]}]}";
			
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"21\",\"p_ClaveUsuario\":\"\",\"p_TipoProceso\":\"REA\",\"p_EntradaReinversionAPV\":[{\"IdGrupoRegimen\":\"APV\",\"Glosa\":\"Prueba\",\"Simula\":\"S\",\"MontoAGF\":0,\"IdCliente\":17781,\"p_DetalleRescateReinversionAPV\":[{\"IdPortafolio\":0,\"IdSerieFondo\":\"00038CCLP\",\"Monto\":10000}],\"p_DetalleAporteReinversionAPV\":[{\"IdSerieFondo\":\"00093HCLP\",\"Porcentaje\":100}]}]}";
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"15\",\"p_ClaveUsuario\":\"\",\"p_TipoProceso\":\"RCA\",\"p_EntradaRescateAPV\":[{\"IdInterno\":0,\"Canal\":\"WEB\",\"Simula\":\"S\",\"FormaPago\":\"TRA\",\"IdCuentaBancaria\":41593,\"IdCliente\":59681,\"p_DetalleRescateAPV\":[{\"IdPortafolio\":76787,\"IdSerieFondo\":\"00132AUSD\",\"Monto\":25,\"Glosa\":\"Simulacion\"}]}]}";			
			String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"1\",\"p_ClaveUsuario\":\"6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b\",\"p_TipoProceso\":\"RE\",\"p_EntradaReinversion\":[{\"IdPortafolio\":191454,\"Glosa\":\"\",\"Simula\":\"N\",\"IndicadorMLT\":\"N\",\"MontoAGF\":0,\"Canal\":\"EJE\",\"IdCliente\":52451,\"p_DetalleRescateReinversion\":[{\"IdSerieFondo\":\"00041BCLP\",\"Monto\":18354,\"NumeroCuotas\":10,\"RescateTotal\":\"N\",\"IdRegimenTributario\":\"18T\"}],\"p_DetalleAporteReinversion\":[{\"IdSerieFondo\":\"00038ACLP\",\"Porcentaje\":100,\"IdRegimenTributario\":\"18Q\",\"Switch\":\"N\",\"GeneraSpot\":\"N\"}]}]}";
			EntradaProcesarReinversionRescate Objin = gson.fromJson(jsonCL, EntradaProcesarReinversionRescate.class);

			ProcesarReinversionRescate demo = new ProcesarReinversionRescate();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaProcesarReinversionRescate ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}