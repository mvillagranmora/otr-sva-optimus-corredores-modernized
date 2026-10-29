package cl.bcs.mesa.MES;

import java.math.BigDecimal;
import java.sql.Connection;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.ValorizarInforme;
import cl.bcs.custodia.utils.ValorizarInforme.ObjEntradaInstrumentos;
import cl.bcs.custodia.utils.ValorizarInforme.ValorizarInformeEntrada;
import cl.bcs.custodia.utils.ValorizarInforme.ValorizarInformeSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log;
import cl.bcs.plataforma.pla.AgregarLog.OutAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarCarteraPactosValorizada implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConsultarCarteraPactosValorizada.class);
	
	private final String SQL_SELECT_CLIENTE_SOCIONEGOCIO = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectClienteSocioNegocio.sql");
	private final String SQL_SELECT_CLIENTE_MESA = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectClienteMesa.sql");
	private final String SQL_SELECT_PORTAFOLIOS_MESA = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectPortafoliosMesa.sql");
	
	private final String SQL_SELECT_RESUMEN_HISTORICO = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectCarteraHistorica_Reserva.sql");
	private final String SQL_SELECT_RESUMEN = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectReserva.sql");
	
	private final String SQL_SELECT_TASA_RECOMPRA_PACTO = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectTasaRecompraPactos.sql");
	
	private final String SQL_SELECT_COMPRAS_PACTOS = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectComprasPacto.sql");
	private final String SQL_SELECT_VENTAS_PACTOS = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectVentasPacto.sql");
	private final String SQL_SELECT_VENTAS_PACTOS_COMPROMETIDAS = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectVentasPactoComprometidas.sql");
	
	private final String SQL_SELECT_COMPRAS_PACTOS_HISTORICO = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectComprasPactoHistorico.sql");
	private final String SQL_SELECT_VENTAS_PACTOS_HISTORICO = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectVentasPactoHistorico.sql");
	private final String SQL_SELECT_VENTAS_PACTOS_COMPROMETIDAS_HISTORICO = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectVentasPactoComprometidasHistorico.sql");
	
	private final String SQL_SELECT_FECHA_ULTIMO_RESPALDO = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectFechaUltimoRespaldo.sql");
	
	public static class EntradaConsultarCarteraPactosValorizada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaConsulta;
		public String p_FechaValorizacion;
		public String p_TipoValorizacion;
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public Long p_IdSocioNegocio;
		public String p_TipoReporte;
		public Long p_IdPais;
		public List<EntradaPortafolios> p_Portafolios;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntradaPortafolios {
		public Long p_IdPortafolio;
		public Long p_IdEmpresa;
	}
	
	public static class SalidaConsultarCarteraPactosValorizada {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ResumenCartera> P_OUT_CARTERA_RESUMEN;
		public String P_OUT_LLAVE_REPORTE;
		
		public SalidaConsultarCarteraPactosValorizada() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_LLAVE_REPORTE = "";
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_CARTERA_RESUMEN = new ArrayList<ResumenCartera>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cliente {
		public Long IdCliente;
		
		public Cliente() {
			this.IdCliente = 0L;
		}
	}
	
	public static class ResumenCartera {
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public String IdMonedaInstrumento;
		public BigDecimal MontoValorActualMayorCD;
		public BigDecimal MontoValorActualMenorCD;
		public BigDecimal MontoValorActualMayorCP;
		public BigDecimal MontoValorActualMenorCP;
	}
	
	public static class DetalleCartera {
		public Long IdValorizacion = 0L;
		public Long IdCartera = 0L;
		public Long FolioCartera = 0L;
		public Long FolioOperacion = 0L;
		public Long IdOperacion = 0L;
		public String IdTipoOperacion;
		public Long IdCliente = 0L;
		public Long IdPortafolio = 0L;
		public String Diferenciador;
		public String FechaOperacion;
		public String FechaVencimientoOperacion;
		public String FechaLiquidacion;
		public String IdCondicionLiquidacion;
		public String IdTipoReajuste;
		public String CodigoIFRS;
		public String Nemotecnico;
		public String EsFungible;
		public BigDecimal Cantidad = BigDecimal.ZERO;
		public String Instrumento;
		public String Emisor;
		public Long IdInstrumento = 0L;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdMonedaInstrumento;
		public String IdMonedaEmisionInstrumento;
		public String IdTipoReajusteInstrumento;
		public String FechaEmisionInstrumento;
		public String FechaVencimientoInstrumento;
		public BigDecimal MontoCompraMonedaSistema = BigDecimal.ZERO;
		public BigDecimal MontoOperacion = BigDecimal.ZERO;
		public BigDecimal MontoPactado = BigDecimal.ZERO;
		public BigDecimal ValorRescate = BigDecimal.ZERO;
		public BigDecimal PrecioCompra = BigDecimal.ZERO;
		public BigDecimal TasaActual = BigDecimal.ZERO;
		public BigDecimal TasaCompra = BigDecimal.ZERO;
		public String IdConcepto;
		public String IdBolsa;
		public String IdBoveda;
		public String IdTipoProducto;
		public String FechaUltimoRespaldo;
	}
	
	public static class CarteraValorizada {
		public Date FechaConsulta ;
		public Date FechaValorizacion;
		public String TipoValorizacion;
		public String TablaAfecta;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdCartera;
		public Long FolioCartera;
		public String IdTipoOperacion;
		public Date FechaOperacion;
		public Date FechaVencimientoOperacion;
		public Long IdOperacion;
		public String IdTipoComprobante;
		public Long FolioComprobante;
		public Date FechaComprobante;
		public String IdCondicionLiquidacion;
		public Date FechaLiquidacion;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public Long IdInstrumentoAnterior;
		public String Nemotecnico;
		public String IdEmisor;
		public String NemoSVS;
		public String IdTipoReajuste;
		public Date FechaVencimientoInstrumento;
		public Date FechaEmisionInstrumento;
		public String IdBoveda;
		public String CodigoIFRS;
		public String IdMonedaInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public BigDecimal Precio;
		public BigDecimal Tasa;
		public BigDecimal TasaTir;
		public BigDecimal MontoValorizadoDesde;
		public BigDecimal MontoValorizadoDesdeMonedaInstrumento;
		public BigDecimal MontoValorizadoDesdeMonedaAdicional;
		public BigDecimal MontoValorizadoHasta;
		public BigDecimal MontoValorizadoHastaMonedaInstrumento;
		public BigDecimal MontoValorizadoHastaMonedaAdicional;
		public BigDecimal MontoInteres;
		public BigDecimal MontoInteresMonedaInstrumento;
		public BigDecimal MontoInteresMonedaAdicional;
		public BigDecimal MontoReajuste;
		public BigDecimal MontoReajusteMonedaInstrumento;
		public BigDecimal MontoReajusteMonedaAdicional;
		public BigDecimal MontoDevengo;
		public BigDecimal MontoDevengoMonedaInstrumento;
		public BigDecimal MontoDevengoMonedaAdicional;
		public BigDecimal Duration;
		public BigDecimal Convexidad;
		public Long IdEmpresa;
		public Long IdUsuario;
		public Timestamp FechaIngresoRegistro;
	}
	
	public static class TasaRecompraPacto {
		public String UsaTasaRecompraPacto;
		
		public TasaRecompraPacto() {
			this.UsaTasaRecompraPacto = "N";
		}
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarCarteraPactosValorizada jin = gson.fromJson(in.toString(), EntradaConsultarCarteraPactosValorizada.class);
		SalidaConsultarCarteraPactosValorizada out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public boolean ValidacionIds(Long id) {
		return id == null || id.equals(0L);
	}
	
	public String convertirTipoValorizacion(String tipoValorizacion) {
		if (tipoValorizacion.equals("MERC") ||tipoValorizacion.equals("COMP") ||tipoValorizacion.equals("PAC")) {
			return tipoValorizacion.equals("MERC") ? "M" : tipoValorizacion.equals("COMP") ? "C" : "P";
		}
		return tipoValorizacion;		
	}
	
	public SalidaConsultarCarteraPactosValorizada logica(Connection conn, EntradaConsultarCarteraPactosValorizada entrada) throws Exception{
		
		logger.info("INICIO::ConsultarCarteraPactosValorizada ");
		SalidaConsultarCarteraPactosValorizada out = new SalidaConsultarCarteraPactosValorizada();
		SQL sql=new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");		
		InformacionEmpresa empresa = new ConsultarEmpresa().logica(conn, entrada.p_IdEmpresa);
		HashMap<String,Object> params = new HashMap<String,Object>(3);
		
		//VALIDACION DE VALORES FECHAS
		if(entrada.p_FechaConsulta == null || entrada.p_FechaValorizacion == null) {
			out.P_OUT_ERRORESCONTADOR++;
			String codigoFechaInvalida = "FECHA_INVALIDA";
			MensajeSalida error = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,codigoFechaInvalida, Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigoFechaInvalida).msg());
			out.P_OUT_MENSAJES.add(error);
			return out;
		}		
			
		LocalDate parsedFechaConsulta = LocalDate.parse(entrada.p_FechaConsulta);
		LocalDate parsedFechaValorizacion = LocalDate.parse(entrada.p_FechaValorizacion);
		LocalDate parsedFechaSistema = LocalDate.parse(fechaSistema);
		
		//VALIDACION DE FECHACONSULTA VS FECHAVALORIZACION
		if(parsedFechaValorizacion.isBefore(parsedFechaConsulta)) {
			out.P_OUT_ERRORESCONTADOR++;
			String codigoFechaValorizacion = "FECHA_VALORIZACION_INVALIDA";
			MensajeSalida error = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,codigoFechaValorizacion, Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigoFechaValorizacion).msg());
			out.P_OUT_MENSAJES.add(error);
			return out;
		}	
		
		//VALIDACION DE IDS NULOS O 0
		if(ValidacionIds(entrada.p_IdCliente) && ValidacionIds(entrada.p_IdSocioNegocio) && ValidacionIds(entrada.p_IdEmpresa)) {
			out.P_OUT_ERRORESCONTADOR++;
			String codigoIdsNulos = "IDS_NULOS";
			MensajeSalida error = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,codigoIdsNulos, Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigoIdsNulos).msg());
			out.P_OUT_MENSAJES.add(error);
			return out;
		}		
		
		Long IdCliente = 0L;
		
		//TODO BUSCAR FORMA DE SACAR IF ANIDADO
		if (entrada.p_IdCliente.equals(0L)) {
			List<Cliente> cliente = new ArrayList<>();
			if (!entrada.p_IdSocioNegocio.equals(0L)) {
				params.put("IdSocioNegocio", entrada.p_IdSocioNegocio);
				cliente = sql.query(conn, SQL_SELECT_CLIENTE_SOCIONEGOCIO, params, Cliente.class);				
			} else {
				params.put("IdEmpresa", entrada.p_IdEmpresa);
				cliente = sql.query(conn, SQL_SELECT_CLIENTE_MESA, params, Cliente.class);				
			}
			if (!cliente.isEmpty()) {
				IdCliente = cliente.stream().findFirst().orElse(new Cliente()).IdCliente;
			}
		} else {
			IdCliente = entrada.p_IdCliente;
		}
		
		//VALIDACION CLIENTE INEXISTENTE
		if (IdCliente.equals(0L)) {
			out.P_OUT_ERRORESCONTADOR++;
			String codigoIdCliente = "ID_CLIENTE_INVALIDO";
			MensajeSalida error = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,codigoIdCliente, Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigoIdCliente).msg());
			out.P_OUT_MENSAJES.add(error);
			return out;
		}
		
		Boolean esCuentaPropia = empresa.IdCliente.equals(IdCliente);
		
		//VALIDACION CUENTA PROPIA
		if (!esCuentaPropia) {
			out.P_OUT_ERRORESCONTADOR++;
			String codigoCuentaPropia = "ERROR_CUENTA_PROPIA";
			MensajeSalida error = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,codigoCuentaPropia, Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigoCuentaPropia).msg());
			out.P_OUT_MENSAJES.add(error);
			return out;
		}
		
		params.clear();
		
		String tipoValorizacion = convertirTipoValorizacion(entrada.p_TipoValorizacion);

		params.put("TipoValorizacion", tipoValorizacion);
		params.put("IdCliente", IdCliente);
		params.put("IdPortafolio", entrada.p_IdPortafolio);
		params.put("Fecha", entrada.p_FechaConsulta);
		params.put("FechaSistema", fechaSistema);
		
		
		if (parsedFechaConsulta.isBefore(parsedFechaSistema)) {
			out.P_OUT_CARTERA_RESUMEN.addAll(sql.query(conn, SQL_SELECT_RESUMEN_HISTORICO, params, ResumenCartera.class));
		} else {
			out.P_OUT_CARTERA_RESUMEN.addAll(sql.query(conn, SQL_SELECT_RESUMEN, params, ResumenCartera.class));
		} 
		
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		
		TasaRecompraPacto tasaRecompraPacto = sql.query(conn, SQL_SELECT_TASA_RECOMPRA_PACTO, params, TasaRecompraPacto.class)
				.stream()
				.findFirst()
				.orElse(new TasaRecompraPacto());
		
		params.clear();
		params.put("UsaTasaRecompraPacto", tasaRecompraPacto.UsaTasaRecompraPacto);
		params.put("TipoValorizacion", tipoValorizacion);
		params.put("IdCliente", IdCliente);
		params.put("IdPortafolio", entrada.p_IdPortafolio);
		params.put("Fecha", entrada.p_FechaConsulta);
		params.put("FechaSistema", fechaSistema);
		
		List<DetalleCartera> detalleCartera = new ArrayList<DetalleCartera>();
		
		if (parsedFechaConsulta.isBefore(parsedFechaSistema)) {
			HashMap<String, Object> paramRespaldo = new HashMap<>(1);
			paramRespaldo.put("p_Fecha", entrada.p_FechaConsulta);
			String fechaRespaldo = sql.query(conn, SQL_SELECT_FECHA_ULTIMO_RESPALDO, paramRespaldo, DetalleCartera.class).stream().findFirst().get().FechaUltimoRespaldo;
			params.put("Fecha", fechaRespaldo);
			detalleCartera.addAll(sql.query(conn, SQL_SELECT_COMPRAS_PACTOS_HISTORICO, params, DetalleCartera.class));
			detalleCartera.addAll(sql.query(conn, SQL_SELECT_VENTAS_PACTOS_HISTORICO, params, DetalleCartera.class));
			detalleCartera.addAll(sql.query(conn, SQL_SELECT_VENTAS_PACTOS_COMPROMETIDAS_HISTORICO, params, DetalleCartera.class));
		} else {
			detalleCartera.addAll(sql.query(conn, SQL_SELECT_COMPRAS_PACTOS, params, DetalleCartera.class));
			detalleCartera.addAll(sql.query(conn, SQL_SELECT_VENTAS_PACTOS, params, DetalleCartera.class));
			detalleCartera.addAll(sql.query(conn, SQL_SELECT_VENTAS_PACTOS_COMPROMETIDAS, params, DetalleCartera.class));
		}
		
		if (detalleCartera.isEmpty()) {
			logger.debug("No hay pactos para valorizar.");				
			out.P_OUT_ERRORESCONTADOR++;
			String codigoSinPactos = "SIN_PACTOS_PARA_VALORIZAR";
			MensajeSalida errorValorizacion = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigoSinPactos, Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigoSinPactos).msg());
			out.P_OUT_MENSAJES.add(errorValorizacion);
			return out;
		}
		
		List<Long> IdsPortafolio = new ArrayList<Long>();
		
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		
		if(entrada.p_IdPortafolio == null || entrada.p_IdPortafolio.equals(0L)) {
			List<Portafolio> portafoliosMesa = sql.query(conn, SQL_SELECT_PORTAFOLIOS_MESA, params, Portafolio.class);
			for (Portafolio port : portafoliosMesa) {
				IdsPortafolio.add(port.IdPortafolio);
			}
		} else {
			IdsPortafolio.add(entrada.p_IdPortafolio);
		}
		params.clear();
		
		if (esCuentaPropia && !detalleCartera.isEmpty()) {
 			
			//SERVICIO DE LOGS
 			AgregarLog agregarLogService = new AgregarLog();
			InAgregarLog agregarLogEntrada = new InAgregarLog();
			
			agregarLogEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			agregarLogEntrada.p_IdUsuario = entrada.p_IdUsuario;
			agregarLogEntrada.p_IdModulo = entrada.p_IdModulo;
			agregarLogEntrada.p_UsaReglasNegocio = "Si";
			agregarLogEntrada.p_IdEstadoAutorizacion = "S";
			agregarLogEntrada.p_IdTrabajo = 0L;
			
			String tipoLog = "INFORMATIVO";
			String subregistroLog = "";
			String valorAntiguo = "";
			String valorNuevo = "";			
			
			//VALORIZA
 			ValorizarInformeEntrada valorizarInformeEntrada = new ValorizarInformeEntrada();
 			valorizarInformeEntrada.p_IdEmpresa 					= entrada.p_IdEmpresa;
 			valorizarInformeEntrada.p_IdUsuario 					= entrada.p_IdUsuario;
 			valorizarInformeEntrada.p_IdModulo 						= entrada.p_IdModulo;
 			valorizarInformeEntrada.p_IdPais 						= entrada.p_IdPais;
 			valorizarInformeEntrada.p_IdMonedaAdicional 			= "USD";
 			valorizarInformeEntrada.p_IdsPortafolio 				= IdsPortafolio;
 			valorizarInformeEntrada.p_FechaInicial					= entrada.p_FechaConsulta;
 			valorizarInformeEntrada.p_FechaFinal					= entrada.p_FechaValorizacion;
 			valorizarInformeEntrada.p_OrigenProcesamiento 			= "CARTERA.PACTO"; 
 			valorizarInformeEntrada.p_IdTipoTasaValorizacion		= entrada.p_TipoValorizacion;
 			valorizarInformeEntrada.p_IdFechaHoraSistema			= FechaDB.ObtenerFechaHoraSistemaLocal(conn,entrada.p_IdEmpresa);
 			valorizarInformeEntrada.p_IdClienteSolicitante          = IdCliente;
 			
 			ValorizarInformeSalida valorizarInformeSalidaGeneral = new ValorizarInformeSalida();
 			
 			for (int i = 0; i < detalleCartera.size() ; i++ ){
 				DetalleCartera detalle = detalleCartera.get(i);
 				ValorizarInforme.ObjEntradaInstrumentos entradaInstrumento = new ObjEntradaInstrumentos();
 				entradaInstrumento.IdValorizacion                   = detalle.IdValorizacion;
 				entradaInstrumento.TablaOrigen                      = "SUBYACENTE";
 				entradaInstrumento.IdCartera                        = detalle.IdCartera;
 				entradaInstrumento.FolioCartera		                = detalle.FolioCartera;
 				entradaInstrumento.FolioOperacion					= detalle.FolioOperacion;
 				entradaInstrumento.IdCliente                        = detalle.IdCliente;
 				entradaInstrumento.IdPortafolio                     = detalle.IdPortafolio;
 				entradaInstrumento.IdClasificacionTipoInstrumento   = detalle.IdClasificacionTipoInstrumento;
 				entradaInstrumento.IdTipoInstrumento                = detalle.IdTipoInstrumento;
 				entradaInstrumento.IdInstrumento                    = detalle.IdInstrumento;
 				entradaInstrumento.FechaVencimientoInstrumento      = detalle.FechaVencimientoInstrumento;
 				entradaInstrumento.FechaVencimientoOperacion        = detalle.FechaVencimientoOperacion;
 				entradaInstrumento.FechaLiquidacion                 = detalle.FechaLiquidacion;
 				entradaInstrumento.IdOperacion						= detalle.IdOperacion;
 				entradaInstrumento.FechaEmision                     = detalle.FechaEmisionInstrumento;
 				entradaInstrumento.FechaOperacion                   = detalle.FechaOperacion;
 				entradaInstrumento.IdMonedaInstrumento              = detalle.IdMonedaInstrumento;
 				entradaInstrumento.IdTipoReajusteInstrumento        = detalle.IdTipoReajusteInstrumento;
 				entradaInstrumento.IdTipoReajusteCabecera           = detalle.IdTipoReajuste;
 				entradaInstrumento.IdMonedaEmisionInstrumento       = detalle.IdMonedaEmisionInstrumento;
 				entradaInstrumento.IdCondicionLiquidacion           = detalle.IdCondicionLiquidacion;
 				entradaInstrumento.IdTipoOperacion             		= detalle.IdTipoOperacion;
 				entradaInstrumento.Emisor                           = detalle.Emisor;
 				entradaInstrumento.EsFungible                       = detalle.EsFungible;
 				entradaInstrumento.CodigoIFRS                       = detalle.CodigoIFRS;
 				entradaInstrumento.Nemotecnico                      = detalle.Nemotecnico;
 				entradaInstrumento.NemoSVS                          = detalle.Instrumento;
 				entradaInstrumento.Diferenciador					= detalle.Diferenciador;
 				entradaInstrumento.Cantidad                         = detalle.Cantidad;
				entradaInstrumento.IdConcepto                       = detalle.IdConcepto;
 				entradaInstrumento.MontoCompra                      = detalle.MontoOperacion;
 				entradaInstrumento.MontoCompraMonedaSistema			= detalle.MontoCompraMonedaSistema;
 				entradaInstrumento.ValorRescate                     = detalle.ValorRescate;
 				entradaInstrumento.PrecioCompra                     = ("COMP".equalsIgnoreCase(entrada.p_TipoValorizacion)) ? null : detalle.PrecioCompra;
 				entradaInstrumento.TasaCompra                       = ("MERC".equalsIgnoreCase(entrada.p_TipoValorizacion)) ? null : detalle.TasaActual;				
 				entradaInstrumento.MontoPactado						= detalle.MontoPactado;
 				entradaInstrumento.TasaCompraValorizacionFechaOperacion = detalle.TasaCompra;
 				entradaInstrumento.OrigenProcesamiento              = "CARTERA.PACTO";
 				entradaInstrumento.IdBolsa							= detalle.IdBolsa;
 				entradaInstrumento.IdBoveda							= detalle.IdBoveda;
 				entradaInstrumento.IdTipoProducto					= detalle.IdTipoProducto;
 				
				valorizarInformeEntrada.p_Instrumentos.add(entradaInstrumento); 
 				
 				//AL FINAL O CADA 5000
 				if ((i!= 0 && i%5000 == 0) || i == detalleCartera.size() - 1) {
 					logger.info("Valorizando hasta " + i);
 					ValorizarInforme ValorizarPapeles = new ValorizarInforme();
 					ValorizarInformeSalida Salida = new ValorizarInformeSalida();
 					try {
 						Salida = ValorizarPapeles.logica(conn, valorizarInformeEntrada);
 					} catch(Exception err) {
 						logger.error(err);
 						err.printStackTrace();
 						valorNuevo = err.toString();
 						Salida.P_OUT_ERRORESCONTADOR++;
 					} finally {
 						valorizarInformeSalidaGeneral.P_OUT_ERRORESCONTADOR += Salida.P_OUT_ERRORESCONTADOR;
 	 					valorizarInformeSalidaGeneral.P_OUT_ERRORVALORIZACIONES.addAll(Salida.P_OUT_ERRORVALORIZACIONES);
 	 					valorizarInformeSalidaGeneral.P_OUT_INSTRUMENTOSVALORIZADOS.addAll(Salida.P_OUT_INSTRUMENTOSVALORIZADOS);
 	 					valorizarInformeSalidaGeneral.P_OUT_MENSAJES.addAll(Salida.P_OUT_MENSAJES);
 	 					valorizarInformeSalidaGeneral.LlaveReporte = Salida.LlaveReporte;
 	 					//LIMPIA LOS INSTRUMENTOS ANTERIORERMENTE VALORIZADOS PARA LA SIGUIENTE TANDA
 	 					valorizarInformeEntrada.p_Instrumentos.clear();
 					}
 				}
 			} 			
 			
 			out.P_OUT_LLAVE_REPORTE = valorizarInformeSalidaGeneral.LlaveReporte;			
				
			if (valorizarInformeSalidaGeneral.P_OUT_ERRORESCONTADOR > 0) {
				logger.debug("Valorizacion erronea");				
				out.P_OUT_ERRORESCONTADOR++;
				String codigoError = "VALORIZACION_PACTOS_ERRONEA";
				MensajeSalida errorValorizacion = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigoError, Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigoError).msg());
				out.P_OUT_MENSAJES.add(errorValorizacion);
				tipoLog = "ERROR";
				subregistroLog = "ERROR";
			}
			
			Long ultimaValorizacion = valorizarInformeSalidaGeneral.P_OUT_INSTRUMENTOSVALORIZADOS.size() != 0 ?
					valorizarInformeSalidaGeneral.P_OUT_INSTRUMENTOSVALORIZADOS
					.stream()
					.max(Comparator.comparingLong(inst -> inst.IdValorizacion))
					.get().IdValorizacion : (long) (Math.random())*100000;
			
			Log logValorizacion = new Log();			
			logValorizacion.IdCabeceraLog = ultimaValorizacion; 
			logValorizacion.IdTipoLog = tipoLog; 
			logValorizacion.IdModulo = entrada.p_IdModulo; 
			logValorizacion.IdUsuario = entrada.p_IdUsuario; 
			logValorizacion.IdEmpresa = entrada.p_IdEmpresa; 
			logValorizacion.FechaSistemaLog = fechaSistema; 
			logValorizacion.IdCliente = IdCliente;
			logValorizacion.NombreCliente = empresa.NombreCompleto;
			logValorizacion.IdTipoRegistro = "INFORME_CARTERA_PACTOS_VALORIZADA";
			logValorizacion.NombreProceso = "Valorizacion Informe de Cartera Propia Pactos"; 		
			
			agregarLogEntrada.p_Log.add(logValorizacion);	
			agregarLogEntrada.agregarDetalleLogBasico(ultimaValorizacion, 0L, "VALORIZACION", valorAntiguo, valorNuevo, subregistroLog);
			
			OutAgregarLog agregarLogSalida = agregarLogService.logica(conn, agregarLogEntrada);
			
			if(agregarLogSalida.P_OUT_ERRORESCONTADOR > 0L) {
				//SI SE AGREGALOG TIENE ERRORES, SE ENVIA MENSAJE DE ADVERTENCIA 				
				logger.debug("No se pudo agregar log");
				String codigoError = "ERROR_AGREGAR_LOG";
				MensajeSalida errorValorizacion = new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, codigoError, Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigoError).msg());
				out.P_OUT_MENSAJES.add(errorValorizacion);				
			}
		}
		
		//VALIDACION LLAVE REPORTE INVALIDA
		if ( out.P_OUT_LLAVE_REPORTE == null || out.P_OUT_LLAVE_REPORTE.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR++;
			String codigoLlaveInvalida = "LLAVE_REPORTE_INVALIDA";
			MensajeSalida errorValorizacion = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigoLlaveInvalida, Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigoLlaveInvalida).msg());
			out.P_OUT_MENSAJES.add(errorValorizacion);	
		}
		
		return out;
	}
	
	public static void main(String[] args) throws Exception,SQLException {
		// TODO Auto-generated method stub
		System.out.println("== TEST ==");
		try (Connection con =  PlataformaUtils.getPropertiesConnection("mbi", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			
			String Json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6703,\"p_IdModulo\":\"MRF\",\"p_Fecha\":\"2026-05-06T04:00:00.000Z\",\"p_FechaConsulta\":\"2026-05-06\",\"p_FechaValorizacion\":\"2026-05-07\",\"p_TipoValorizacion\":\"PAC\",\"p_IdCliente\":0,\"p_IdPortafolio\":0,\"p_IdSocioNegocio\":0,\"p_TipoReporte\":\"CAR_PAC\",\"p_EjecutaValorizacion\":true,\"p_IdPais\":44}";
			EntradaConsultarCarteraPactosValorizada ObjIn = gson.fromJson(Json,EntradaConsultarCarteraPactosValorizada.class);
			
			ConsultarCarteraPactosValorizada demo = new ConsultarCarteraPactosValorizada();
			
			try {
				SalidaConsultarCarteraPactosValorizada ObjOut=demo.logica(con, ObjIn);
				System.out.println(ObjOut.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
			} finally { 
				con.commit();
				//con.commit();
			}
						
    	}
		System.out.println("== END ==");
	}
}