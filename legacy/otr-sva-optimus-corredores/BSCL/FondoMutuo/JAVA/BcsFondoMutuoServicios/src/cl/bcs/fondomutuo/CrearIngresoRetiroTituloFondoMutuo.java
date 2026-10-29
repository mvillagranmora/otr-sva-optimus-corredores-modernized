package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.EntradaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.CrearSolicitudAGF.EntradaCrearSolicitudAGF;
import cl.bcs.fondomutuo.CrearSolicitudAGF.SalidaCrearSolicitudAGF;
import cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo.EntradaProcesarCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo.SalidaMovimientoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo.SalidaProcesarCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.pojo.ConfiguracionPropositoPortafolio;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.MovimientoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.pojo.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.TipoMovimientoFondoMutuo;
import cl.bcs.fondomutuo.utils.FondoMutuoUtils;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearIngresoRetiroTituloFondoMutuo implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearIngresoRetiroTituloFondoMutuo.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectTipoMovimientoFondoMutuo.sql");
	private final String SQL_SELECT_REGIMEN = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");
	private final String SQL_INSERT_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_InsertSolicitud.sql");
	private final String SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectConfiguracionPropositoPortafolio.sql");
	
	public static class EntradaCrearIngresoRetiroTituloFondoMutuo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<InSolicitudesTitulo> p_SolicitudesTitulo;
		
		public EntradaCrearIngresoRetiroTituloFondoMutuo() {
			this.p_SolicitudesTitulo = new ArrayList<InSolicitudesTitulo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudesTitulo{
		
		public Long IdInterno;
		public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdTipoMovimientoFondoMutuo;
    	public String IdSerieFondo;
		public BigDecimal NumeroCuotas;
		public String IdRegimenTributario;
		public String Glosa;
    	public String SolicitudBatch;
    	public String OrigenOperacion;
    	public Long IdAutorizadoOrigen;
		public String IngresoAutomatico;
		public Date FechaValorCuota;
		
		public InSolicitudesTitulo() {
			this.IngresoAutomatico = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearIngresoRetiroTituloFondoMutuo{
		
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearIngresoRetiroTituloFondoMutuo(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCrearIngresoRetiroTituloFondoMutuo jin = gson.fromJson(in.toString(), EntradaCrearIngresoRetiroTituloFondoMutuo.class);
		SalidaCrearIngresoRetiroTituloFondoMutuo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearIngresoRetiroTituloFondoMutuo logica(Connection con, EntradaCrearIngresoRetiroTituloFondoMutuo entrada) throws Exception{
		logger.info("INICIO::CrearIngresoRetiroTituloFondoMutuo");
		long inicioServicio = System.currentTimeMillis();
		SQL sql=new SQL();
		SalidaCrearIngresoRetiroTituloFondoMutuo out = new SalidaCrearIngresoRetiroTituloFondoMutuo();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_SolicitudesTitulo.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InSolicitudesTitulo titulos: entrada.p_SolicitudesTitulo) {
			if(titulos.IdInterno.longValue() == 0L){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if (correlativos.contains(titulos.IdInterno)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} else {
				correlativos.add(titulos.IdInterno);
			}
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		List<String> series = new ArrayList<String>();
		List<Long> clientes = new ArrayList<Long>();
		List<Long> portafolios = new ArrayList<Long>();
		List<String> regimen = new ArrayList<String>();
		List<String> idTipoMovimientos = new ArrayList<String>();
		idTipoMovimientos.add(_c.IngresoTitulo);
		idTipoMovimientos.add(_c.RetiroTitulo);
		for(InSolicitudesTitulo titulos : entrada.p_SolicitudesTitulo){
			if(!series.contains(titulos.IdSerieFondo)) series.add(titulos.IdSerieFondo);
			if(!clientes.contains(titulos.IdCliente)) clientes.add(titulos.IdCliente);
			if(!portafolios.contains(titulos.IdPortafolio)) portafolios.add(titulos.IdPortafolio);
			if(!regimen.contains(titulos.IdRegimenTributario)) regimen.add(titulos.IdRegimenTributario);
			
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		params.clear();
		params.put("IdPortafolio", portafolios);
		List<ClientePortafolioSolicitudes> clientesSolicitudes = sql.query(con, SQL_SELECT_PORTAFOLIO, params, ClientePortafolioSolicitudes.class);
		params.clear();
		params.put("IdTipoMovimientoFondoMutuo", idTipoMovimientos);
		List<TipoMovimientoFondoMutuo> tipoMovimientos = sql.query(con, SQL_SELECT_TIPO_MOVIMIENTO, params, TipoMovimientoFondoMutuo.class);
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		params.put("IdRegimenTributario", regimen);
		List<RegimenSerieDeFondo> regimenesSeries = sql.query(con, SQL_SELECT_REGIMEN, params, RegimenSerieDeFondo.class);
		params.clear();
		params.put("IdRegimenFondoMutuo", regimen);
		List<ConfiguracionPropositoPortafolio> configuracionesRegimenPortafolio = sql.query(con, SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO, params, ConfiguracionPropositoPortafolio.class);
		params.clear();
		
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		HashMap<Long,Object> hclientes = Utils.List2Hash(clientesSolicitudes, "IdPortafolio");
		HashMap<String,Object> htipoMovimiento =  Utils.List2Hash(tipoMovimientos, "IdTipoMovimientoFondoMutuo");
		HashMap<String,Object> hseriesRegimen =  Utils.List2Hash(regimenesSeries, "IdRegimenFondoMutuoSerieFondo");
		HashMap<String,Object> hconfRegimenPortafolio =  Utils.List2Hash(configuracionesRegimenPortafolio, "Id");
		
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		for(InSolicitudesTitulo solicitud : entrada.p_SolicitudesTitulo){
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			serie.IdInterno = solicitud.IdInterno;
			if (solicitud.FechaValorCuota != null) {
				serie.Fecha = Fecha.stringToSqlDate(solicitud.FechaValorCuota);
			} else if (serieFondo != null) {
				serie.Fecha = Fecha.stringToSqlDate(serieFondo.FechaFondo);
			} else {
				serie.Fecha = fechaSistema;
			}
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = _c.CalculoPorCuota;
		 	serie.Monto = BigDecimal.ZERO;
		 	serie.NumeroCuotas = solicitud.NumeroCuotas;
		 	serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.IdMonedaInversion = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.TipoSolicitud = _c.IngresoTitulo.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo) ? _c.TipoSolicitudAporte : _c.TipoSolicitudRescate;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = entrada.p_IdEmpresa;
		 	entradaCalculo.p_Series.add(serie);
		}
		
		SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate", Long.valueOf(entrada.p_SolicitudesTitulo.size()));
		
		HashMap<Long,Object> valoresCuota = Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno");
		
		List<SolicitudAporteRescate> solicitudesProcesar = new ArrayList<SolicitudAporteRescate>();
		
		ServicioCumpleRequisitosCrearSolicitudAporteRescate cumpleRequisitos = new ServicioCumpleRequisitosCrearSolicitudAporteRescate();
		EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate entradaCumpleRequisitos = new EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate();
		entradaCumpleRequisitos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCumpleRequisitos.p_IdUsuario = entrada.p_IdUsuario;
		
		int i = 0;
		
		List<ResultadoSolicitud> resultadoTemp = new ArrayList<ResultadoSolicitud>();
		
		List<Long> folioContratoSolicitudes = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato", (long) entrada.p_SolicitudesTitulo.size());
		
		int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, entrada.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
        Timestamp fechaHoraUTC = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(con,entrada.p_IdEmpresa));

		for(InSolicitudesTitulo solicitud : entrada.p_SolicitudesTitulo){
			ClientePortafolioSolicitudes cliente = (ClientePortafolioSolicitudes) hclientes.get(solicitud.IdPortafolio);
			TipoMovimientoFondoMutuo tipoMovimiento = (TipoMovimientoFondoMutuo) htipoMovimiento.get(solicitud.IdTipoMovimientoFondoMutuo);
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno);
			RegimenSerieDeFondo r = (RegimenSerieDeFondo) hseriesRegimen.get(solicitud.IdSerieFondo + "_" +solicitud.IdRegimenTributario);
			
			SolicitudAporteRescate sol = new SolicitudAporteRescate();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes solVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio portVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio();
			ConfiguracionPropositoPortafolio confPropositoPortafolio = null;
			if(cliente != null) confPropositoPortafolio = (ConfiguracionPropositoPortafolio) hconfRegimenPortafolio.get(solicitud.IdRegimenTributario + "_" + cliente.Proposito);
			//SE COMPLETA EL MOVIMIENTO
			sol.IdInterno = solicitud.IdInterno;
			sol.IdSolicitudAporteRescate = secuencias.get(i);
		 	sol.IdCliente = solicitud.IdCliente;
		 	sol.IdPortafolio = solicitud.IdPortafolio;
		 	sol.TipoSolicitud = _c.IngresoTitulo.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo) ? _c.TipoSolicitudAporte : _c.TipoSolicitudRescate;
		 	sol.IdSerieFondo = serie != null ? serie.IdSerieFondo : "";
		 	sol.IdInstrumento =  serie != null ? serie.IdInstrumento  :  0L;
		 	sol.IdTipoMovimientoFondoMutuo = solicitud.IdTipoMovimientoFondoMutuo;
		 	sol.IdDividendo = 0L;
			sol.IdSerieFondoDestino = "";
			sol.IdEmisorDestino = "";
			sol.IdClienteDestino = 0L;
			sol.IdPortafolioDestino = 0L;
			sol.IndicadorMLT = _c.NO;
			sol.Overnight = _c.NO;
			sol.IdSolicitudAporteRescateRelacionado = 0L;
			sol.TipoCarteraNormativa = _c.IngresoTitulo.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) && r != null ? r.TipoCarteraNormativa : "";
			sol.IdTipoCartera = "";
			sol.CalculoPorMontoCuota = _c.CalculoPorCuota;
			sol.FechaEmision = Fecha.toDate(fechaSistema);
			sol.FechaOriginalInversion = Fecha.toDate("0001-01-01");
			sol.FechaAceptacion = sol.FechaEmision;
			sol.FechaValorCuota = solicitud.FechaValorCuota != null ? solicitud.FechaValorCuota : sol.FechaEmision;
		    sol.FechaCertificacion = sol.FechaEmision;
		    sol.FechaLiquidacion = sol.FechaEmision;
			//sol.FechaIngresoRegistro = Fecha.toTimeStamp(FechaDB.ObtenerFechaHoraSistemaUTC(con, entrada.p_IdEmpresa));
		    sol.FechaIngresoRegistro = fechaHoraUTC;
			sol.IdMoneda = serie != null ? serie.IdMoneda : "";
			sol.MontoComision = BigDecimal.ZERO;
			sol.MontoComisionLocal = BigDecimal.ZERO;
			sol.Estado =  _c.EstadoCertificado;
			sol.IdMedioDePago = "";
			sol.Glosa = solicitud.Glosa;
			sol.FolioContrato = folioContratoSolicitudes.get(entrada.p_SolicitudesTitulo.indexOf(solicitud));
			sol.IdEmpresa = entrada.p_IdEmpresa;
			sol.IdUsuario = entrada.p_IdUsuario;
			sol.IdModulo = entrada.p_IdModulo;
			sol.IdEmisorOrigen = serie != null ? serie.IdEmisor : "";
			sol.SolicitudBatch = solicitud.SolicitudBatch;
			sol.OrigenOperacion = solicitud.OrigenOperacion;
			sol.IdAutorizadoOrigen = solicitud.IdAutorizadoOrigen;
			sol.IdEmpleadoEjecutivo = cliente != null && cliente.IdEmpleadoEjecutivo != null ? cliente.IdEmpleadoEjecutivo : 0L;
			sol.IdRegimenTributario = solicitud.IdRegimenTributario;
			sol.IngresoAutomatico = solicitud.IngresoAutomatico;
			sol.Monto = BigDecimal.ZERO;
			sol.NumeroCuotas = BigDecimal.ZERO;
			sol.ValorCuota = BigDecimal.ZERO;
			sol.CostoUF = BigDecimal.ZERO;
		    sol.CostoMonedaLocal = BigDecimal.ZERO; 
			sol.ValorUFFechaInversion = BigDecimal.ZERO; 
			sol.ParidadFechaInversion = BigDecimal.ZERO;
			sol.IdMonedaSistema = "";
			sol.MontoMonedaSistema = BigDecimal.ZERO;
			sol.MontoPagoLocal = BigDecimal.ZERO; 
			sol.MontoPago = BigDecimal.ZERO;
			if(vc != null){
				sol.Monto = vc.Monto;
				sol.NumeroCuotas = vc.NumeroCuotas;
				sol.ValorCuota = vc.ValorCuota;
				sol.CostoUF = vc.CostoEnFactorAjuste;
			    sol.CostoMonedaLocal = vc.MontoDestino; 
				sol.ValorUFFechaInversion = vc.ValorFactorAjuste; 
				sol.ParidadFechaInversion = vc.ValorParidad;
				sol.IdMonedaSistema = vc.IdMonedaSistema;
				sol.MontoMonedaSistema = vc.MontoMonedaSistema;
				sol.MontoPagoLocal = vc.MontoDestino; 
				sol.MontoPago = vc.Monto;
				sol.MontoInversion = vc.Monto;
				sol.CostoMonedaLocalInversion = vc.MontoDestino;
				sol.ValorCuotaInversion = vc.ValorCuota;
				sol.ParidadFechaCertificacion = vc.ValorParidad;
				sol.NumeroCuotasInversion = vc.NumeroCuotas;
			}
			if(r != null){
				switch(r.GrupoRegimen){
					case "APV":
						sol.Tributacion = _c.TributacionAPV;
						break;
					case "ART108":
						sol.Tributacion = _c.TributacionART108;
						break;
					case "ART107":
						sol.Tributacion = _c.TributacionART107;
						break;
					case "57BIS":
						sol.Tributacion = _c.Tributacion57BIS;
						break;
					default:
						sol.Tributacion = _c.TributacionDefecto;
						break;
				}
			}
			sol.EstadoProceso = _c.EstadoPendiente;
			solicitudesProcesar.add(sol);
			
			//VALIDACIONES
			solVal.IdInterno = sol.IdInterno;
			solVal.IdCliente = sol.IdCliente;
			solVal.IdPortafolio = sol.IdPortafolio;
			solVal.IdSerieFondo = sol.IdSerieFondo;
			solVal.IdTipoMovimientoFondoMutuo = tipoMovimiento != null ? tipoMovimiento.IdTipoMovimientoFondoMutuo : "";
			solVal.Monto = BigDecimal.ONE;
			solVal.NumeroCuotas = sol.NumeroCuotas;
			solVal.IdEstadoSerie = "";
			solVal.EsFechaValidaAporte = _c.SI;
			solVal.EsFechaValidaRescate = _c.SI;
			solVal.EsDiferido = _c.SI;
			solVal.EsMontoMaximo = _c.SI;
			solVal.EsMontoMinimo = _c.SI;
			solVal.ValorCuota = sol.ValorCuota;
			solVal.Overnight = _c.NO;
			solVal.EstadoLiquidez = "";
			solVal.EsMoneyMarket = _c.SI;
			solVal.EsFamiliaEquivalente = _c.SI;
			solVal.EsPortafolioRegimenValido = _c.SI;
			solVal.EsRegimenValido = _c.SI;
			solVal.PermiteRescateMandato = _c.SI;
			solVal.PermiteCambioGrupo = _c.SI;
			solVal.EsPortafolioValido = _c.SI;
			solVal.EsCuentaInversionValido = _c.SI;
			solVal.PermiteAporte = _c.SI;
			solVal.EsRegimenDestinoValido = _c.SI;
			if(serie != null){
				solVal.IdEstadoSerie = serie.IdEstado;
				solVal.EstadoFondo = _c.Abierto;
				solVal.EstadoBloqueo = serie.BloqueoRescate;
				solVal.EsFondoValido = _c.SI;
			}
			
			if(r == null){
				solVal.EsRegimenValido = _c.NO;
			}else {
				solVal.PermiteAporte = _c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) ? r.PermiteAporte : _c.SI;
			}
			
			if(_c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) && confPropositoPortafolio == null) solVal.EsPortafolioRegimenValido = _c.NO;
			
			if(cliente != null){
				portVal.IdInterno = sol.IdInterno;
				portVal.IdPortafolio = sol.IdPortafolio;
				portVal.IdPortafolioReal = cliente.IdPortafolio;
				portVal.IdEstadoPortafolio = cliente.IdEstadoPortafolio;
				portVal.IdCliente = sol.IdCliente;
				portVal.IdClienteReal = cliente.IdCliente;
				portVal.IdEstadoCliente = cliente.IdEstadoCliente;
				portVal.DocumentoIdentidad = cliente.DocumentoIdentidad;
				portVal.IdEmpleadoPortafolio = cliente.IdEmpleadoEjecutivo;
				portVal.IdClienteDestino = sol.IdClienteDestino;
				portVal.IdClienteRealDestino = 0L;
				portVal.IdPortafolioDestino = sol.IdPortafolio;
				portVal.IdPortafolioRealDestino = 0L;
				portVal.IdEstadoClienteDestino = "";
				portVal.IdEstadoPortafolioDestino = "";
			}
			
			solVal.IdTipoOperacion=_c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) ? _c.TipoOperacionAporteFondoMutuo : _c.TipoOperacionRescateFondoMutuo;
			solVal.IdTipoProducto=_c.TipoOperacion;
			
			entradaCumpleRequisitos.p_Portafolios.add(portVal);
			entradaCumpleRequisitos.p_SolicitudesAporteRescate.add(solVal);
			
			//SE PREPARA SALIDA
			ResultadoSolicitud solicitudSalida = new ResultadoSolicitud();
			solicitudSalida = Utils.copyPojo(sol, solicitudSalida);
			if(serie != null){
				solicitudSalida.IdFondo = serie.IdFondo;
				solicitudSalida.Nemotecnico = serie.Nemotecnico;
				solicitudSalida.NombreFondo = serie.NombreFondo;
				solicitudSalida.NombreCorto = serie.NombreCortoTipoFondo;
				solicitudSalida.NombreSerieFondo = serie.NombreSerieFondo;
				solicitudSalida.CodigoSerie = serie.CodigoSerie;
				solicitudSalida.IdEmisorOrigen = serie.IdEmisor;
			}
			if(cliente != null){
				solicitudSalida.DocumentoIdentidad = cliente.DocumentoIdentidad;
				solicitudSalida.CodigoPortafolio = cliente.CodigoPortafolio;
				solicitudSalida.NombreCliente = cliente.NombreCompleto;
			}
			solicitudSalida.MontoTransferencia = sol.Monto;
			solicitudSalida.NumeroCuotaTransferencia = sol.NumeroCuotas;
			solicitudSalida.FechaAceptacion = Fecha.stringToSqlDate(sol.FechaAceptacion);
			solicitudSalida.FechaOriginalInversion = Fecha.stringToSqlDate(sol.FechaOriginalInversion);
			solicitudSalida.FechaIngresoRegistro = Fecha.toStringTimeStamp(sol.FechaIngresoRegistro);
			solicitudSalida.FechaLiquidacion = Fecha.stringToSqlDate(sol.FechaLiquidacion);
			solicitudSalida.FechaCertificacion = Fecha.stringToSqlDate(sol.FechaCertificacion);
			solicitudSalida.FechaValorCuota = Fecha.stringToSqlDate(sol.FechaValorCuota);
			resultadoTemp.add(solicitudSalida);
			
			i++;
		}
		
		HashMap<Long,Object> hSolicitudSalida = Utils.List2Hash(resultadoTemp, "IdInterno");
		
		Long registrosPorProcesar = 0L;
		SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate salidaCumpleRequisitos = cumpleRequisitos.logica(con, entradaCumpleRequisitos);
		for(MensajeSalida mje : salidaCumpleRequisitos.P_OUT_MENSAJES){
			out.P_OUT_MENSAJES.add(mje);
			if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
				out.P_OUT_ERRORESCONTADOR++;
				for(SolicitudAporteRescate sol : solicitudesProcesar){
					if(sol.IdInterno.equals(Math.abs(mje.id))){
						sol.EstadoProceso = _c.EstadoRechazado;
						break;
					}
				}
			}
		}
		
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
				registrosPorProcesar++;
			}
		}
		
		//VALIDAMOS CUSTODIA
		ConsultarFlujoCustodiaFondoMutuo flujo = new ConsultarFlujoCustodiaFondoMutuo();
		EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo = new EntradaConsultarFlujoCustodiaFondoMutuo();
		entradaFlujo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFlujo.p_IdUsuario = entrada.p_IdUsuario;
		entradaFlujo.p_IdModulo = entrada.p_IdModulo;
		entradaFlujo.p_IdPais = idPaisEmpresa;
		entradaFlujo.p_FechaSaldo = fechaSistema;
		entradaFlujo.p_DiasProyeccion = 0;
		List<Long> fPortafolio = new ArrayList<Long>();
		HashMap<String,Long> fRegimenHashMap = new HashMap<String,Long>();
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso) && _c.RetiroTitulo.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo)){
				SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(sol.IdSerieFondo);
				if(!fPortafolio.contains(sol.IdPortafolio)){
					fPortafolio.add(sol.IdPortafolio);
					ConsultarFlujoCustodiaFondoMutuo.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuo.InCarteras();
					cartera.IdCliente = sol.IdCliente;
					cartera.IdPortafolio = sol.IdPortafolio;
					entradaFlujo.p_Carteras.add(cartera);
				}
				String key = sol.IdRegimenTributario+serie.IdInstrumento+sol.IdCliente+"";
				if(fRegimenHashMap.get(key) == null) {
					fRegimenHashMap.put(key,serie.IdInstrumento);
					ConsultarFlujoCustodiaFondoMutuo.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuo.InInstrumento();
					instrumento.IdInstrumento = serie.IdInstrumento;
					instrumento.IdRegimenFondoMutuo = sol.IdRegimenTributario;
					entradaFlujo.p_Instrumentos.add(instrumento);
				}
			}
		}
		registrosPorProcesar = solicitudesProcesar.parallelStream().filter(sol->_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)).count();
		SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo = new SalidaConsultarFlujoCustodiaFondoMutuo();
		
		if(registrosPorProcesar.equals(0L)){
			return out;
		}

		if(entradaFlujo.p_Carteras.size() > 0){
			salidaFlujo = flujo.logica(con, entradaFlujo);
		}

		//CUSTODIA
		ProcesarCustodiaFondoMutuo procesarCustodia = new ProcesarCustodiaFondoMutuo();
		EntradaProcesarCustodiaFondoMutuo entradaCustodia = new EntradaProcesarCustodiaFondoMutuo();
		//AGF
		CrearSolicitudAGF crearSolicitudAGF = new CrearSolicitudAGF();
		EntradaCrearSolicitudAGF entradaCrearAGF = new EntradaCrearSolicitudAGF();
		
		//SE VALIDA CUSTODIA, SI HAY SE INSERTA Y MUEVE CUSTODIA
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			if(_c.EstadoRechazado.equalsIgnoreCase(sol.EstadoProceso)) continue;
			
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(sol.IdSerieFondo);
			BigDecimal custodia = BigDecimal.ZERO;
			
			for(ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado flujoCustodia : salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO){
				if(FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso(flujoCustodia, sol)){
					custodia = flujoCustodia.CantidadDisponible;
					break;
				}
			}
			
			//VALIDAMOS CUSTODIA, SI NO HAY ERROR Y SALIMOS. SI HAY INSERTAMOS Y MOVEMOS CUSTODIA 
			if(sol.NumeroCuotas.compareTo(custodia) == 1 && _c.RetiroTitulo.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo)){
				sol.EstadoProceso = _c.EstadoRechazado;
				out.P_OUT_ERRORESCONTADOR++;
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
				parametrosMensaje.put("Nemotecnico", sol.IdSerieFondo);
				parametrosMensaje.put("CantidadRetiro", sol.NumeroCuotas.toPlainString());
				parametrosMensaje.put("CantidadCustodia",custodia.toPlainString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FM_CLIENTE_SIN_CUSTODIA",parametrosMensaje,con,entrada.p_IdEmpresa));
				continue;
			}
			//Ingreso o Retiro
			MovimientoCustodiaFondoMutuo movCustodia = new MovimientoCustodiaFondoMutuo();
			movCustodia.IdGenerarMovimientoCustodia = sol.IdInterno;
			movCustodia.FechaOperacion = sol.FechaEmision;
			movCustodia.IdCliente = sol.IdCliente;
			movCustodia.IdPortafolio = sol.IdPortafolio;
			movCustodia.IdTipoProducto = _c.TipoOperacion;
			movCustodia.IdConcepto = _c.IngresoTitulo.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) ? _c.ConceptoIngresoTitulo : _c.ConceptoRetiroTitulo;
			movCustodia.IdTipoOperacion = _c.IngresoTitulo.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) ? _c.TipoOperacionAporteFondoMutuo : _c.TipoOperacionRescateFondoMutuo;
			movCustodia.IdOperacion = sol.IdSolicitudAporteRescate;
			movCustodia.IdTipoComprobante = _c.TipoComprobanteCustodia;
			movCustodia.FolioComprobante = sol.IdSolicitudAporteRescate;
			movCustodia.FechaComprobante = sol.FechaCertificacion;
			movCustodia.IdBolsa = _c.BCS;
			movCustodia.IdAgente = sol.IdEmpleadoEjecutivo;
			movCustodia.FechaLiquidacion = sol.FechaLiquidacion;
			movCustodia.IdUsuario = entrada.p_IdUsuario;
			movCustodia.TipoCarteraNormativa = sol.TipoCarteraNormativa;
			movCustodia.FechaCurse = sol.FechaAceptacion;
			movCustodia.TipoFactorAjuste = _c.UF;
			movCustodia.ValorFactorAjusteaFechaCurse = sol.ValorUFFechaInversion;
			movCustodia.CostoEnFactorAjuste = sol.CostoUF;
			movCustodia.IdInstrumento = serie.IdInstrumento;
			movCustodia.IdEmisor = serie.IdEmisor;
			movCustodia.IdMonedaInstrumento = serie.IdMoneda;
			movCustodia.IdBoveda = _c.COR;
			movCustodia.Cantidad = sol.NumeroCuotas;
			movCustodia.ValorRescate = BigDecimal.ZERO;
			movCustodia.PrecioCompra = sol.ValorCuota;
			movCustodia.MontoCompra = sol.MontoMonedaSistema;
			movCustodia.MontoCompraMonedaInstrumento = sol.Monto;
			movCustodia.IdTipoCanalIngreso = "";
			movCustodia.IdRegimenTributario = sol.IdRegimenTributario;
			movCustodia.FechaOrigenCompra = sol.FechaValorCuota;
			entradaCustodia.p_Movimientos.add(movCustodia);
		}
		
		List<SolicitudAporteRescate> solicitudesInsertar = new ArrayList<SolicitudAporteRescate>();
		if(!_c.SI.equalsIgnoreCase(entrada.p_SimularOperacion) && entradaCustodia.p_Movimientos.size() > 0){
			entradaCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCustodia.p_IdUsuario = entrada.p_IdUsuario;
			entradaCustodia.p_IdModulo = entrada.p_IdModulo;
			entradaCustodia.p_simularOperacion = _c.NO;
			entradaCustodia. p_UsaReglasNegocio = _c.SI;

			SalidaProcesarCustodiaFondoMutuo salidaCustodia = procesarCustodia.logica(con, entradaCustodia);
			out.P_OUT_ERRORESCONTADOR += salidaCustodia.P_OUT_ERRORESCONTADOR;
			for(MensajeSalida mje : salidaCustodia.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
					Long id = Math.abs(mje.id);
					out.P_OUT_MENSAJES.add(new MensajeSalida(id, mje.tipo, mje.codigo, mje.descripcion));
					for(SolicitudAporteRescate sol : solicitudesProcesar){
						if(id.longValue() == sol.IdInterno.longValue()){
							sol.EstadoProceso = _c.EstadoRechazado;
							break;
						}
					}
				}
			}
			
			Map<Long,List<SalidaMovimientoCustodiaFondoMutuo>> movimientosAgrupadosPorSolicitud = salidaCustodia.P_OUT_MOVIMIENTOS.parallelStream().collect(Collectors.groupingBy(solicitud->solicitud.IdGenerarMovimientoCustodia));
			List<Date> fechaCertificaciones = solicitudesProcesar.parallelStream().filter(solicitud->_c.TipoSolicitudRescate.equals(solicitud.TipoSolicitud)).map(solicitud->solicitud.FechaCertificacion).distinct().collect(Collectors.toList());
			HashMap<Date,Long> mapeoIdInternoFecha = new HashMap<Date,Long>();
			
			CalcularParidad calcularParidad = new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 1L;
			
			CalculoEntrada calculoEntradaUTM = new CalculoEntrada();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Long idInterno = 1L;
			
			for(Date fechaCertificacion : fechaCertificaciones) {
				calculoEntradaUTM.IdTrabajo = 1L;
				calculoEntradaUTM.IdInterno = idInterno;
				calculoEntradaUTM.Fecha = dateFormat.format(fechaCertificacion);
				calculoEntradaUTM.IdMonedaOrigen = "UTM";
				calculoEntradaUTM.IdMonedaDestino = "CLP";
				calculoEntradaUTM.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntradaUTM.MontoOrigen = BigDecimal.ONE;
				calculoEntradaUTM.IdUso = "";
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaUTM);
				mapeoIdInternoFecha.put(fechaCertificacion, idInterno);
				idInterno++;
			}
			
			CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
			HashMap<Long,Object> hCalculoUTM = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
			
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				if(_c.EstadoRechazado.equalsIgnoreCase(sol.EstadoProceso)) continue;
				
				if(_c.TipoSolicitudAporte.equals(sol.TipoSolicitud)) {
					SalidaMovimientoCustodiaFondoMutuo carteraSolicitud = salidaCustodia.P_OUT_MOVIMIENTOS.parallelStream().filter(cartera->cartera.IdGenerarMovimientoCustodia.longValue() == sol.IdInterno.longValue()).findFirst().orElse(null);
					if(carteraSolicitud != null) {
						sol.IdCartera = carteraSolicitud.IdCustodia;
						sol.RentabilidadReal = carteraSolicitud.MontoCompraFactorOriginal;
					}
				}
				
				if(_c.TipoSolicitudRescate.equals(sol.TipoSolicitud)) {
					BigDecimal costoUF = BigDecimal.ZERO;
					BigDecimal rentabilidad = BigDecimal.ZERO;
					List<SalidaMovimientoCustodiaFondoMutuo> movimientosCustodia = movimientosAgrupadosPorSolicitud.get(sol.IdInterno);
					if(movimientosCustodia != null) {
						for(SalidaMovimientoCustodiaFondoMutuo movimientoCustodia : movimientosCustodia) {
							costoUF = costoUF.add(movimientoCustodia.CostoInversionEnFactorAjusteARetirar, MathContext.DECIMAL128);
							rentabilidad = rentabilidad.add(movimientoCustodia.MontoCompraFactorARetirar, MathContext.DECIMAL128);
						}
					}
					
					Long id = mapeoIdInternoFecha.get(sol.FechaCertificacion);
					CalculoSalida calculoValorUTM = (CalculoSalida) hCalculoUTM.get(id);
					BigDecimal valorUTM = calculoValorUTM !=null && "OK".equals(calculoValorUTM.Resultado) ? calculoValorUTM.MontoDestino : BigDecimal.ONE;
					sol.RentabilidadReal = ((sol.MontoMonedaSistema.divide(valorUTM, MathContext.DECIMAL128)).subtract(rentabilidad, MathContext.DECIMAL128));
					sol.Spread = sol.CostoUF.subtract(costoUF, MathContext.DECIMAL128);
					sol.CostoUF = costoUF;
				}
			}
			
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
					if(_c.NO.equalsIgnoreCase(sol.SolicitudBatch)){
						//AGF
						CrearSolicitudAGF.Solicitudes solAGF = new CrearSolicitudAGF.Solicitudes();
						solAGF.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
						entradaCrearAGF.p_Solicitudes.add(solAGF);
					}
					solicitudesInsertar.add(sol);
					out.P_OUT_RESULTADO.add((ResultadoSolicitud) hSolicitudSalida.get(sol.IdInterno));
				}
			}
			
			//INSERTAMOS LAS SIN ERRORES
			if(solicitudesInsertar.size() > 0){
				sql.batch(con, SQL_INSERT_SOLICITUD, solicitudesInsertar, SolicitudAporteRescate.class);
				//AGF
				if(entradaCrearAGF.p_Solicitudes.size() > 0){
					entradaCrearAGF.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaCrearAGF.p_IdUsuario = entrada.p_IdUsuario;
					entradaCrearAGF.p_IdModulo = entrada.p_IdModulo;
					entradaCrearAGF.p_EsManual = _c.NO;
					SalidaCrearSolicitudAGF salidaAGF = crearSolicitudAGF.logica(con, entradaCrearAGF);
					out.P_OUT_ERRORESCONTADOR += salidaAGF.P_OUT_ERRORESCONTADOR;
					out.P_OUT_MENSAJES.addAll(salidaAGF.P_OUT_MENSAJES);
				}

				//AGREGAR LOG
				{
					AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
					for (SolicitudAporteRescate sol : solicitudesInsertar) {
						ClientePortafolioSolicitudes cliente = (ClientePortafolioSolicitudes) hclientes.get(sol.IdPortafolio);
						AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
						log.IdCabeceraLog = sol.IdSolicitudAporteRescate;
						log.IdTipoLog = "INGRESO";
						log.IdModulo = _c.ModuloFondoMutuo;
						log.IdUsuario = entrada.p_IdUsuario;
						log.IdEmpresa = entrada.p_IdEmpresa;
						log.FechaSistemaLog = fechaSistema;
						log.IdUsuarioAtendido = entrada.p_IdUsuario;
						log.IdCliente = sol.IdCliente;
						log.EstadoCliente = cliente.IdEstadoCliente;
						log.NombreCliente = cliente.NombreCompleto;
						log.IdTipoDocumentoIdentidad = cliente.TipoDocumentoIdentidad;
						log.IdDocumentoIdentidad = cliente.DocumentoIdentidad;
						log.IdPortafolio = sol.IdPortafolio;
						log.EstadoPortafolio = cliente.IdEstadoPortafolio;
						log.IdTipoRegistro = _c.IngresoTitulo.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) ? "INGRESO_TITULO_FFMM" : "RETIRO_TITULO_FFMM";
						log.EstadoRegistro = sol.Estado;
						log.IdTipoOperacion = sol.IdTipoMovimientoFondoMutuo;
						log.Folio = sol.IdSolicitudAporteRescate;
						log.Cantidad = sol.NumeroCuotas;
						log.PrecioTasa = sol.ValorCuota;
						log.Monto = sol.Monto;
						log.IdMonedaOrigen = sol.IdMoneda;
						log.IdMonedaLiquidacion = sol.IdMoneda;
						log.TipoCambio = sol.ParidadFechaInversion.doubleValue()+"";
						log.Nemo = sol.IdSerieFondo;
						log.IdProceso = 0L;
						log.EstadoProceso = "";
						log.NombreProceso = "";						
						entradaAgregarLog.p_Log.add(log);
					}
					AgregarLog agregarLogService = new AgregarLog();
					agregarLogService.logica(con, entradaAgregarLog);
				}
			}
		}else{
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
					out.P_OUT_RESULTADO.add((ResultadoSolicitud) hSolicitudSalida.get(sol.IdInterno));
				}
			}
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearIngresoRetiroTituloFondoMutuo", finServicio - inicioServicio);
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_SolicitudesTitulo\":[{\"IdInterno\":1,\"IdCliente\":524891,\"IdPortafolio\":1450068,\"IdTipoMovimientoFondoMutuo\":\"RETTIT\",\"IdSerieFondo\":\"FMLIQUIDEZ_A\",\"NumeroCuotas\":10,\"IdRegimenFondoMutuo\":\"APV-A\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"IdRegimenTributario\":\"APV-A\",\"Glosa\":\"\",\"IdAutorizadoOrigen\":0}]}";		
		EntradaCrearIngresoRetiroTituloFondoMutuo Objin = gson.fromJson(jsonRF, EntradaCrearIngresoRetiroTituloFondoMutuo.class);
		CrearIngresoRetiroTituloFondoMutuo  demo=new CrearIngresoRetiroTituloFondoMutuo();				
		try {
			SalidaCrearIngresoRetiroTituloFondoMutuo ObjOut=demo.logica(con, Objin);
			//con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}

}
