package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.SalidaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.Resultado;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.EntradaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.CrearSolicitudAGF.EntradaCrearSolicitudAGF;
import cl.bcs.fondomutuo.CrearSolicitudAGF.SalidaCrearSolicitudAGF;
import cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.SalidaObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria.EntradaProcesarIngresoEgresoTesoreria;
import cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria.SalidaProcesarIngresoEgresoTesoreria;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.ValidaMontoInversion.EntradaValidaMontoInversion;
import cl.bcs.fondomutuo.ValidaMontoInversion.SalidaValidaMontoInversion;
import cl.bcs.fondomutuo.ValidaPatrimonioEmisor.EntradaValidaPatrimonioEmisor;
import cl.bcs.fondomutuo.ValidaPatrimonioEmisor.SalidaValidaPatrimonioEmisor;
import cl.bcs.fondomutuo.pojo.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.pojo.ConfiguracionPropositoPortafolio;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.IndicadoresPortafolio;
import cl.bcs.fondomutuo.pojo.IndicadoresPortafolioFondoMutuo;
import cl.bcs.fondomutuo.pojo.Moneda;
import cl.bcs.fondomutuo.pojo.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.TipoMovimientoFondoMutuo;
import cl.bcs.fondomutuo.utils.FondoMutuoUtils;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso.ResultadoProceso_Salida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearSolicitudAporteOvernight implements JavaCall{
	
	private final Log log = new Log("FM.CrearSolicitudAporteOvernight");
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectTipoMovimientoFondoMutuo.sql");
	private final String SQL_SELECT_REGIMEN = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectIndicadoresPortafolio.sql");
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO_FFMM = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectIndicadoresPortafolioFFMM.sql");
	private final String SQL_SELECT_MONEDA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectMoneda.sql");
	private final String SQL_INSERT_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_InsertSolicitud.sql");
	private final String SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectConfiguracionPropositoPortafolio.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaCrearSolicitudAporteOvernight{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public List<InSolicitudesAporte> p_SolicitudesAporte;
		
		public EntradaCrearSolicitudAporteOvernight() {
			p_SolicitudesAporte = new ArrayList<InSolicitudesAporte>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudesAporte{
		
		public Long IdInterno;
		public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdSerieFondo;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
    	public String IdMoneda;
    	public String IdMedioDePago;
    	public String IdMedioDePagoRescate;
    	public Long IdPreferenciaPagoPortafolioRescate;
    	public String Visado;
    	public String SolicitudBatch;
    	public String OrigenOperacion;
    	public String IdRegimenTributario;
    	public String AdministracionLiquidez;
    	public String Glosa;
    	public Long IdAutorizadoOrigen;
    	public Long FolioContrato;
		public BigDecimal MontoPatrimonio;
		public String IngresoAutomatico;
		
		public InSolicitudesAporte() {
			this.MontoPatrimonio = BigDecimal.ZERO;
			this.IngresoAutomatico = "N";
			this.AdministracionLiquidez = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearSolicitudAporteOvernight{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearSolicitudAporteOvernight(){
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
		EntradaCrearSolicitudAporteOvernight jin = gson.fromJson(in.toString(), EntradaCrearSolicitudAporteOvernight.class);
		SalidaCrearSolicitudAporteOvernight out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearSolicitudAporteOvernight logica(Connection con, EntradaCrearSolicitudAporteOvernight entrada) throws Exception{
		log.write("INICIO::CrearSolicitudAporteOvernight");		
		SalidaCrearSolicitudAporteOvernight out = new SalidaCrearSolicitudAporteOvernight();
		SQL sql=new SQL();
		Boolean origenAdministracionLiquidez = false;
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_SolicitudesAporte.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 9999L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"NO_SE_INGRESARON_SOLICITUDES_APORTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InSolicitudesAporte mov: entrada.p_SolicitudesAporte) {
			if(mov.IdInterno.longValue() == 0L){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if (correlativos.contains(mov.IdInterno)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} else {
				correlativos.add(mov.IdInterno);
			}
			
			
			if("S".equals(mov.AdministracionLiquidez)) {
				origenAdministracionLiquidez = true;
			}
		}
		
		int countTransaccionesBatch = 0;
		
		List<String> series = new ArrayList<String>();
		List<Long> clientes = new ArrayList<Long>();
		List<String> regimen = new ArrayList<String>();
		List<Long> portafolios = new ArrayList<Long>();
		List<String> monedas = new ArrayList<String>();
		List<String> idTipoMovimientos = new ArrayList<String>();
		idTipoMovimientos.add(_c.AporteOvernight);
		
		for(InSolicitudesAporte mov : entrada.p_SolicitudesAporte){
			if(_c.SI.equalsIgnoreCase(mov.SolicitudBatch)){
				countTransaccionesBatch++;
			}
			if(!series.contains(mov.IdSerieFondo)) series.add(mov.IdSerieFondo);
			if(!clientes.contains(mov.IdCliente)) clientes.add(mov.IdCliente);
			if(!portafolios.contains(mov.IdPortafolio)) portafolios.add(mov.IdPortafolio);
			if(!regimen.contains(mov.IdRegimenTributario)) regimen.add(mov.IdRegimenTributario);
		}
		
		ObtenerPerfilFondoMutuo obtenerPerfilFondoMutuo = new ObtenerPerfilFondoMutuo();
		EntradaObtenerPerfilFondoMutuo entradaObtenerPerfilFondoMutuo = new EntradaObtenerPerfilFondoMutuo();
		entradaObtenerPerfilFondoMutuo.p_IdUsuario = entrada.p_IdUsuario;
		SalidaObtenerPerfilFondoMutuo salidaObtenerPerfilFondoMutuo = obtenerPerfilFondoMutuo.logica(con, entradaObtenerPerfilFondoMutuo);
		
		if(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo == null && countTransaccionesBatch != entrada.p_SolicitudesAporte.size()){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PERFIL_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
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
		params.put("IdPortafolio", portafolios);
		params.put("CodigoTipoIndicador", _c.UsaCuentaInversion);
		List<IndicadoresPortafolio> indicadoresPortafolio = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO, params, IndicadoresPortafolio.class);
		params.clear();
		params.put("IdPortafolio", portafolios);
		List<IndicadoresPortafolioFondoMutuo> indicadoresPortafolioFFMM = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO_FFMM, params, IndicadoresPortafolioFondoMutuo.class);
		params.clear();
		params.put("IdRegimenFondoMutuo", regimen);
		List<ConfiguracionPropositoPortafolio> configuracionesRegimenPortafolio = sql.query(con, SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO, params, ConfiguracionPropositoPortafolio.class);
		params.clear();
		
		for(SerieFondoSolicitudes serie : seriesSolicitudes){
			if(!monedas.contains(serie.IdMoneda)) monedas.add(serie.IdMoneda);
		}
		params.put("IdMoneda", monedas);
		List<Moneda> monedasFondo = sql.query(con, SQL_SELECT_MONEDA, params, Moneda.class);
		params.clear();
		
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		HashMap<Long,Object> hclientes = Utils.List2Hash(clientesSolicitudes, "IdPortafolio");
		HashMap<String,Object> htipoMovimiento =  Utils.List2Hash(tipoMovimientos, "IdTipoMovimientoFondoMutuo");
		HashMap<String,Object> hseriesRegimen =  Utils.List2Hash(regimenesSeries, "IdRegimenFondoMutuoSerieFondo");
		HashMap<Long,Object> hindicadoresPortafolio =  Utils.List2Hash(indicadoresPortafolio, "IdPortafolio");
		HashMap<Long,Object> hindicadoresPortafolioFFMM =  Utils.List2Hash(indicadoresPortafolioFFMM, "IdPortafolio");
		HashMap<String,Object> hmonedasFondo =  Utils.List2Hash(monedasFondo, "IdMoneda");
		HashMap<String,Object> hconfRegimenPortafolio =  Utils.List2Hash(configuracionesRegimenPortafolio, "Id");
		
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		ObtenerDiaSiguiente obFechaSiguiente = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente inFechaSiguiente = new InObtenerDiaSiguiente();
		inFechaSiguiente.p_idPais = idPaisEmpresa;
		inFechaSiguiente.p_diaHabil = _c.SI;
		inFechaSiguiente.p_soloCaeDiaHabil = _c.SI;
		for(InSolicitudesAporte mov : entrada.p_SolicitudesAporte){
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(mov.IdSerieFondo);
			FechasIn fechaIN = new FechasIn();
			fechaIN.fechaInicial = serie != null ? Fecha.stringToSqlDate(serie.FechaFondo) : fechaSistema;
			fechaIN.numeroDias = 1L;
			inFechaSiguiente.p_fechas.add(fechaIN);
		}
		
		OutObtenerDiaSiguiente salidaFechaSiguiente = obFechaSiguiente.logica(con, inFechaSiguiente);
		
		CalcularFechaSolicitudAporteRescate calculoFechas = new CalcularFechaSolicitudAporteRescate();
		EntradaCalcularFechaSolicitudAporteRescate entradaFechas = new EntradaCalcularFechaSolicitudAporteRescate();
		entradaFechas.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFechas.p_IdPais = idPaisEmpresa;
		
		for(InSolicitudesAporte solicitud : entrada.p_SolicitudesAporte){
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			CalcularFechaSolicitudAporteRescate.InFechas fecha = new CalcularFechaSolicitudAporteRescate.InFechas();
			CalcularFechaSolicitudAporteRescate.InFechas fechaRescate = new CalcularFechaSolicitudAporteRescate.InFechas();
			if(serie != null){
				//Aporte
				fecha.IdInterno = solicitud.IdInterno;
				fecha.IdSerieFondo = solicitud.IdSerieFondo;
				fecha.FechaEmision = Fecha.stringToSqlDate(serie.FechaFondo);
				if(_c.SI.equalsIgnoreCase(solicitud.SolicitudBatch)){
					fecha.EstadoFondo = _c.Abierto;
					fecha.EstadoLiquidacion = _c.Abierto;
					fecha.NumeroDiasRetencion = 0L;
					fecha.NumeroValutaValorCuota = 0L;
					fecha.NumeroValutaCertificacion = 0L;
					fecha.NumeroValutaCurse = 0L;
				}else{
					fecha.EstadoFondo = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoAporteComercial : serie.EstadoAporteBackoffice;
					fecha.NumeroDiasRetencion = serie.DiasRetencionesAporte;
					fecha.NumeroValutaValorCuota = serie.DiasValutaValorCuotaAporte;
					fecha.NumeroValutaCertificacion = serie.DiasValutaCertificacionAporte;
					fecha.NumeroValutaCurse = serie.DiasValutaCurseAporte;
					fecha.EstadoLiquidacion = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
				}
				fecha.SolicitudBatch = solicitud.SolicitudBatch;
				fecha.TipoSolicitud = _c.TipoSolicitudAporte;
				fecha.FondoExtranjero = serie.FondoExtranjero;
				fecha.IdPaisFondo = serie.IdPaisOrigen;
				fecha.IdPaisEmisor = serie.IdPaisEmisor;
				Moneda moneda = (Moneda) hmonedasFondo.get(serie.IdMoneda);
				fecha.IdPaisMoneda = moneda != null ? moneda.IdPais : 0L;
				//Rescate
				fechaRescate = Utils.copyPojo(fecha, fechaRescate);
				fechaRescate.IdInterno = solicitud.IdInterno * -1;
				String fechaFondo = Fecha.stringToSqlDate(serie.FechaFondo);
				for(OutObtenerDiaSiguiente.FechasOut f : salidaFechaSiguiente.p_out_fechas){
					if(fechaFondo.equalsIgnoreCase(f.fechaInicial)){
						fechaRescate.FechaEmision = f.fechaCalculada;
						break;
					}
				}
				if(_c.NO.equalsIgnoreCase(solicitud.SolicitudBatch)){
					fechaRescate.EstadoFondo = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoRescateComercial : serie.EstadoRescateBackoffice;
					fechaRescate.NumeroDiasRetencion = serie.DiasRetencionesRescate;
					fechaRescate.NumeroValutaValorCuota = serie.DiasValutaValorCuotaRescate;
					fechaRescate.NumeroValutaCertificacion = serie.DiasValutaCertificacionRescate;
					fechaRescate.NumeroValutaCurse = serie.DiasValutaCurseRescate;
				}
				fechaRescate.TipoSolicitud = _c.TipoSolicitudRescate;
				entradaFechas.p_SolicitudAporteRescate.add(fecha);
				entradaFechas.p_SolicitudAporteRescate.add(fechaRescate);
			}
		}
		
		SalidaCalcularFechaSolicitudAporteRescate salidaFechas = calculoFechas.logica(con, entradaFechas);
		
		HashMap<Long,Object> hfechas =  Utils.List2Hash(salidaFechas.P_FECHASAPORTERESCATE, "IdInterno");
		
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		for(InSolicitudesAporte solicitud : entrada.p_SolicitudesAporte){
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			ResultadoFecha fecha = (ResultadoFecha) hfechas.get(solicitud.IdInterno);
			serie.IdInterno = solicitud.IdInterno;
			serie.Fecha = fecha != null ? Fecha.stringToSqlDate(fecha.FechaAceptacion) : fechaSistema;
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = _c.CalculoPorMonto;
		 	serie.Monto = solicitud.Monto;
		 	serie.NumeroCuotas = solicitud.NumeroCuotas;
		 	serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.IdMonedaInversion = solicitud.IdMoneda;
		 	serie.TipoSolicitud =  _c.TipoSolicitudAporte;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = entrada.p_IdEmpresa;
		 	entradaCalculo.p_Series.add(serie);
		}
		
		SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);
		
		if (salidaCalculo.P_OUT_RESULTADO.size() == 1) {
			Resultado resultadoCalculo = salidaCalculo.P_OUT_RESULTADO.get(0);
			if ("ERROR_VALORCUOTA".equalsIgnoreCase(resultadoCalculo.CodigoMensaje)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, resultadoCalculo.CodigoMensaje, resultadoCalculo.Resultado));
				return out;
			}
		}
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate", Long.valueOf(entrada.p_SolicitudesAporte.size()*2));
		
		HashMap<Long,Object> valoresCuota = Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno");
		
		ServicioCumpleRequisitosCrearSolicitudAporteRescate cumpleRequisitos = new ServicioCumpleRequisitosCrearSolicitudAporteRescate();
		EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate entradaCumpleRequisitos = new EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate();
		entradaCumpleRequisitos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCumpleRequisitos.p_IdUsuario = entrada.p_IdUsuario;
		ValidaPatrimonioEmisor validaPatrimonioEmisor = new ValidaPatrimonioEmisor();
		EntradaValidaPatrimonioEmisor entradaValidaPatrimonioEmisor = new EntradaValidaPatrimonioEmisor();
		entradaValidaPatrimonioEmisor.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidaPatrimonioEmisor.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidaPatrimonioEmisor.p_Fecha = fechaSistema;
		int i = 0;
		List<ResultadoSolicitud> resultadoTemp = new ArrayList<ResultadoSolicitud>();
		List<SolicitudAporteRescate> solicitudesProcesar = new ArrayList<SolicitudAporteRescate>();
		List<SolicitudAporteRescate> solicitudesRescates = new ArrayList<SolicitudAporteRescate>();
		
		HashMap<String,BigDecimal> hashMontosPortafolioEmisor = new HashMap<String,BigDecimal>();
		
		List<Long> folioContratoSolicitudes = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato", (long) entrada.p_SolicitudesAporte.size());
		
		int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, entrada.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
        Timestamp fechaHoraUTC = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(con,entrada.p_IdEmpresa));

        // RescateAporteTotalAdmLiquidez
		String rescateAporteTotalAdmLiquidez = "";
		try	{
			rescateAporteTotalAdmLiquidez = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "RescateAporteTotalAdmLiquidez");
		}
		catch (Exception e)	{
			rescateAporteTotalAdmLiquidez = "N";
		}
		SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujoADL = new SalidaConsultarFlujoCustodiaFondoMutuo();
		HashMap<String,Object> hcustodia = new HashMap<String,Object>();
		HashMap<String,Long> hportafolioInstrumentoCustodia = new HashMap<String,Long>();
		if ("S".equals(rescateAporteTotalAdmLiquidez)) {
			EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo = new EntradaConsultarFlujoCustodiaFondoMutuo();
			ConsultarFlujoCustodiaFondoMutuo flujo = new ConsultarFlujoCustodiaFondoMutuo();
			
			List<Long> fPortafolio = new ArrayList<Long>();
			List<Long> fInstrumento = new ArrayList<Long>();
			for(InSolicitudesAporte sol : entrada.p_SolicitudesAporte){
				if ("S".equals(sol.AdministracionLiquidez)) {
					SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(sol.IdSerieFondo);
					if(!fPortafolio.contains(sol.IdPortafolio)){
						fPortafolio.add(sol.IdPortafolio);
						ConsultarFlujoCustodiaFondoMutuo.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuo.InCarteras();
						cartera.IdCliente = sol.IdCliente;
						cartera.IdPortafolio = sol.IdPortafolio;
						entradaFlujo.p_Carteras.add(cartera);
					}
					if(!fInstrumento.contains(serie.IdInstrumento)){
						fInstrumento.add(serie.IdInstrumento);
						ConsultarFlujoCustodiaFondoMutuo.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuo.InInstrumento();
						instrumento.IdInstrumento = serie.IdInstrumento;
						instrumento.IdRegimenFondoMutuo = sol.IdRegimenTributario;
						entradaFlujo.p_Instrumentos.add(instrumento);
					}
				}
			}
			if(entradaFlujo.p_Carteras.size() > 0 && entradaFlujo.p_Instrumentos.size()>0){
				entradaFlujo.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaFlujo.p_IdUsuario = entrada.p_IdUsuario;
				entradaFlujo.p_IdModulo = entrada.p_IdModulo;
				entradaFlujo.p_IdPais = idPaisEmpresa;
				entradaFlujo.p_FechaSaldo = fechaSistema;
				entradaFlujo.p_DiasProyeccion = 0;
				salidaFlujoADL = flujo.logica(con, entradaFlujo);
				hcustodia = Utils.List2Hash(salidaFlujoADL.P_OUT_FLUJOFONDOMUTUOTOTALIZADO, "IdInterno");
				salidaFlujoADL.P_OUT_FLUJOFONDOMUTUOTOTALIZADO.stream().forEach((c)->{
					hportafolioInstrumentoCustodia.put(c.IdPortafolio+"_"+c.IdInstrumento,c.IdInterno);
				});
			}
		}
        
		for(InSolicitudesAporte solicitud : entrada.p_SolicitudesAporte){
			ClientePortafolioSolicitudes cliente = (ClientePortafolioSolicitudes) hclientes.get(solicitud.IdPortafolio);
			TipoMovimientoFondoMutuo tipoMovimiento = (TipoMovimientoFondoMutuo) htipoMovimiento.get(_c.AporteOvernight);
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno);
			RegimenSerieDeFondo r = (RegimenSerieDeFondo) hseriesRegimen.get(solicitud.IdSerieFondo + "_" +solicitud.IdRegimenTributario);
			ConfiguracionPropositoPortafolio confPropositoPortafolio = null;
			if(cliente != null) confPropositoPortafolio = (ConfiguracionPropositoPortafolio) hconfRegimenPortafolio.get(solicitud.IdRegimenTributario + "_" + cliente.Proposito);
			
			ResultadoFecha fecha = (ResultadoFecha) hfechas.get(solicitud.IdInterno);
			ResultadoFecha fechaRest = (ResultadoFecha) hfechas.get(solicitud.IdInterno * -1);
			IndicadoresPortafolio indicadorPortafolio = (IndicadoresPortafolio) hindicadoresPortafolio.get(solicitud.IdPortafolio);
			
			SolicitudAporteRescate sol = new SolicitudAporteRescate(); //APORTE
			SolicitudAporteRescate solRest = new SolicitudAporteRescate(); //RESCATE
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes solVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio portVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio();
			
			//SE COMPLETA EL MOVIMIENTO
			sol.IdInterno = solicitud.IdInterno;
			sol.IdSolicitudAporteRescate = secuencias.get(i);
		 	sol.IdCliente = solicitud.IdCliente;
		 	sol.IdPortafolio = solicitud.IdPortafolio;
		 	sol.TipoSolicitud = _c.TipoSolicitudAporte;
		 	sol.IdSerieFondo = serie != null ? serie.IdSerieFondo : "";
		 	sol.IdTipoMovimientoFondoMutuo = _c.AporteOvernight;
		 	sol.IdDividendo = 0L;
			sol.IdSerieFondoDestino = "";
			sol.IdEmisorDestino = "";
			sol.IdClienteDestino = 0L;
			sol.IdPortafolioDestino = 0L;
			sol.IndicadorMLT = _c.NO;
			sol.Overnight = _c.SI;
			sol.IdSolicitudAporteRescateRelacionado = 0L;
			sol.TipoCarteraNormativa = r != null ? r.TipoCarteraNormativa : "";
			sol.IdTipoCartera = "";
			sol.CalculoPorMontoCuota = _c.CalculoPorMonto;
			sol.FechaEmision = Fecha.toDate(fechaSistema);
			sol.FechaOriginalInversion = Fecha.toDate("0001-01-01");
			//sol.FechaIngresoRegistro = Fecha.toTimeStamp(FechaDB.ObtenerFechaHoraSistemaUTC(con, entrada.p_IdEmpresa));
			sol.FechaIngresoRegistro = fechaHoraUTC;
			sol.IdMoneda = solicitud.IdMoneda;
			sol.Estado =  _c.EstadoIngresado;
			sol.IdMedioDePago = solicitud.IdMedioDePago;
			sol.Glosa = solicitud.Glosa;
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
			sol.EsPorMontoLiquido = _c.NO;
			sol.IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso != null ? entrada.p_IdTipoCanalIngreso : "";
			sol.FolioContrato = solicitud.FolioContrato > 0 ? solicitud.FolioContrato : folioContratoSolicitudes.get(entrada.p_SolicitudesAporte.indexOf(solicitud));
			sol.IdPersona = cliente != null ? cliente.IdPersona : 0L;
			sol.IdTipoInstrumento = serie != null ? serie.IdTipoInstrumento : "";
			sol.IdInstrumento = serie != null ? serie.IdInstrumento : 0L;
			sol.Visado = _c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) && _c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago) ? _c.NO : solicitud.Visado; 
			sol.IdPreferenciaPagoPortafolio = 0L;
			sol.AdministracionLiquidez = solicitud.AdministracionLiquidez;
			sol.EstadoProceso = _c.EstadoPendiente;
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
			sol.MontoComision = BigDecimal.ZERO;
			sol.MontoComisionLocal = BigDecimal.ZERO;
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
				sol.MontoComision = BigDecimal.ZERO;
				sol.MontoComisionLocal = BigDecimal.ZERO;
				sol.MontoPagoLocal = vc.MontoDestino;
				sol.MontoPago = vc.Monto;
				sol.MontoInversion = vc.Monto;
				sol.CostoMonedaLocalInversion = vc.MontoDestino;
				sol.ValorCuotaInversion = vc.ValorCuota;
				sol.ParidadFechaCertificacion = vc.ValorParidad;
				sol.NumeroCuotasInversion = vc.NumeroCuotas;
			}
			if(fecha != null){
				sol.FechaAceptacion = fecha.FechaAceptacion;
			    sol.FechaValorCuota = fecha.FechaValorCuota;
			    sol.FechaCertificacion = fecha.FechaCertificacion;
			    sol.FechaLiquidacion = fecha.FechaLiquidacion;
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
			
			i++;
			
			//RESCATE, COPIAMOS EL APORTE y CAMBIAMOS LO NECESARIO
			solRest = Utils.copyPojo(sol, solRest);
			solRest.IdInterno =  solicitud.IdInterno * -1;
			solRest.IdSolicitudAporteRescate = secuencias.get(i);
			solRest.TipoSolicitud = _c.TipoSolicitudRescate;
			solRest.IdTipoMovimientoFondoMutuo = _c.RescateFondoMutuo;
			solRest.CalculoPorMontoCuota = _c.CalculoPorCuota;
			solRest.IdPreferenciaPagoPortafolio = solicitud.IdPreferenciaPagoPortafolioRescate;
			solRest.FolioContrato = solicitud.FolioContrato >0 ? solicitud.FolioContrato : 0L;
			solRest.IdMedioDePago = solicitud.IdMedioDePagoRescate;
			if(fechaRest != null){
				solRest.FechaAceptacion = fechaRest.FechaAceptacion;
				solRest.FechaValorCuota = fechaRest.FechaValorCuota;
				solRest.FechaCertificacion = fechaRest.FechaCertificacion;
				solRest.FechaLiquidacion = fechaRest.FechaLiquidacion;
			}
			if("S".equals(rescateAporteTotalAdmLiquidez) && "S".equals(solicitud.AdministracionLiquidez)){
				Long IdInternoFlujoCustodia = (Long)hportafolioInstrumentoCustodia.get(sol.IdPortafolio+"_"+sol.IdInstrumento);
				FlujoFondoMutuoTotalizado flujoCustodia = (FlujoFondoMutuoTotalizado)hcustodia.get(IdInternoFlujoCustodia);
				if (flujoCustodia.CantidadDisponible.compareTo(BigDecimal.ZERO)>0) {
					BigDecimal TC = BigDecimal.ONE;
					if (!solRest.IdMoneda.equals(solRest.IdMonedaSistema)) {
						TC = solRest.MontoMonedaSistema.divide(solRest.Monto,MathContext.DECIMAL128);
					}
					solRest.Monto = solRest.Monto.add(flujoCustodia.MontoDisponible);
					solRest.NumeroCuotas = solRest.NumeroCuotas.add(flujoCustodia.CantidadDisponible);
					solRest.CostoMonedaLocal = solRest.CostoMonedaLocal.add(flujoCustodia.MontoDisponible);
					solRest.MontoMonedaSistema = solRest.MontoMonedaSistema.add(flujoCustodia.MontoDisponible.multiply(TC));
					solRest.MontoPagoLocal = solRest.MontoPagoLocal.add(flujoCustodia.MontoDisponible);
					solRest.MontoPago = solRest.MontoPago.add(flujoCustodia.MontoDisponible);
					solRest.MontoInversion = solRest.MontoInversion.add(flujoCustodia.MontoDisponible);
					solRest.CostoMonedaLocalInversion = solRest.CostoMonedaLocalInversion.add(flujoCustodia.MontoDisponible);
					solRest.NumeroCuotasInversion = solRest.NumeroCuotasInversion.add(flujoCustodia.CantidadDisponible);
				}
			}
			//ENLAZAMOS LOS MOVIMIENTOS
			sol.IdSolicitudAporteRescateRelacionado = solRest.IdSolicitudAporteRescate;
			solRest.IdSolicitudAporteRescateRelacionado = sol.IdSolicitudAporteRescate;
			
			solicitudesProcesar.add(sol);
			solicitudesRescates.add(solRest);
			
			//VALIDACIONES
			solVal.IdInterno = sol.IdInterno;
			solVal.IdCliente = sol.IdCliente;
			solVal.IdPortafolio = sol.IdPortafolio;
			solVal.IdSerieFondo = sol.IdSerieFondo;
			solVal.IdTipoMovimientoFondoMutuo = tipoMovimiento != null ? tipoMovimiento.IdTipoMovimientoFondoMutuo : "";
			solVal.Monto = sol.Monto;
			solVal.NumeroCuotas = sol.NumeroCuotas;
			solVal.IdEstadoSerie = "";
			solVal.EsDiferido = _c.SI;
			solVal.EsMontoMaximo = _c.SI;
			solVal.EsMontoMinimo = _c.SI;
			solVal.PermiteCambioGrupo = _c.SI;
			solVal.EsPortafolioValido = _c.SI;
			solVal.PermiteRescateMandato = _c.SI;
			solVal.EsCuentaInversionValido = _c.SI;
			solVal.EsFechaValidaAporte = _c.SI;
			solVal.EsFechaValidaRescate = _c.SI;
			solVal.ValorCuota = sol.ValorCuota;
			solVal.Overnight = _c.SI;
			solVal.EsMoneyMarket = _c.SI;
			solVal.EsFamiliaEquivalente = _c.SI;
			solVal.EsPortafolioRegimenValido = _c.SI;
			solVal.EsRegimenValido = _c.SI;
			solVal.EsCuentaInversionValido = _c.SI;
			solVal.PermiteAporte = _c.SI;
			solVal.EsRegimenDestinoValido = _c.SI;
			if(serie != null){
				solVal.IdEstadoSerie = serie.IdEstado;
				solVal.EstadoFondo = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoAporteComercial : serie.EstadoAporteBackoffice;
				solVal.EstadoBloqueo = _c.NO.equalsIgnoreCase(serie.BloqueoAporte) && _c.NO.equalsIgnoreCase(serie.BloqueoRescate) ? _c.NO : _c.SI;
				solVal.EsMontoMaximo = serie.MontoMaximo.compareTo(BigDecimal.ZERO) != 0 && serie.MontoMaximo.compareTo(sol.Monto) == -1 ? _c.NO : _c.SI;
				if(r != null && _c.SI.equals(r.ValidaMontoMinimoInstrumento)) {
					solVal.EsMontoMinimo = serie.MontoMinimo.compareTo(BigDecimal.ZERO) != 0 && serie.MontoMinimo.compareTo(sol.Monto) == 1 ? _c.NO : _c.SI;
				}
				solVal.EstadoLiquidez = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
				solVal.EsFondoValido = _c.SI;
				if(!sol.IdMoneda.equalsIgnoreCase(serie.IdMoneda) && _c.NO.equalsIgnoreCase(serie.Diferido)){
					solVal.EsDiferido = _c.NO;
				}
				solVal.EsMoneyMarket = serie.MoneyMarket;
				BigDecimal montoPortafolioPorEmisor = hashMontosPortafolioEmisor.get(sol.IdCliente + "_" + serie.IdEmisor);
				if(montoPortafolioPorEmisor == null) montoPortafolioPorEmisor = BigDecimal.ZERO;
				montoPortafolioPorEmisor = montoPortafolioPorEmisor.add(sol.MontoMonedaSistema, MathContext.DECIMAL128);
				hashMontosPortafolioEmisor.put(sol.IdCliente + "_" + serie.IdEmisor, montoPortafolioPorEmisor);
				if(_c.SI.equals(serie.PatrimonioAltos)) {
					ValidaPatrimonioEmisor.Solicitud valPatrimonio = new ValidaPatrimonioEmisor.Solicitud();
					valPatrimonio.IdInterno = sol.IdInterno;
					valPatrimonio.IdCliente = sol.IdCliente;
					valPatrimonio.IdSerieFondo = serie.IdSerieFondo;
					valPatrimonio.IdEmisor = serie.IdEmisor;
					valPatrimonio.MontoPatrimonio = solicitud.MontoPatrimonio;
					valPatrimonio.ValorCuota = sol.ValorCuota;
					entradaValidaPatrimonioEmisor.p_Solicitudes.add(valPatrimonio);
				}
			}
			
			if(r == null){
				solVal.EsRegimenValido = _c.NO;
			}else {
				solVal.PermiteAporte = r.PermiteAporte;
			}
			
			if(confPropositoPortafolio == null) solVal.EsPortafolioRegimenValido = _c.NO;
			
			if(_c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago) && _c.NO.equalsIgnoreCase(indicadorPortafolio.ValorIndicador)){
				solVal.EsCuentaInversionValido = _c.NO;
			}
			
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
			
			solVal.IdTipoOperacion=_c.TipoOperacionAporteFondoMutuo;
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
		HashMap<Long,Object> hSolicitudRescates = Utils.List2Hash(solicitudesRescates, "IdInterno");
		
		int registrosPorProcesar = 0;
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
		
		if(registrosPorProcesar == 0){
			return out;
		}
		
		//VALIDACION INVERSIONES
		ValidaMontoInversion validaMontoInversion = new ValidaMontoInversion();
		EntradaValidaMontoInversion entradaValidaMontoInversion  = new EntradaValidaMontoInversion();
		entradaValidaMontoInversion.p_IdEmpresa=entrada.p_IdEmpresa;
		entradaValidaMontoInversion.p_IdUsuario=entrada.p_IdUsuario;
		entradaValidaMontoInversion.p_Solicitudes= new ArrayList<ValidaMontoInversion.Solicitud>();
		
		entradaValidaMontoInversion.p_Solicitudes = solicitudesProcesar.parallelStream().map(solicitud->{
			SerieFondoSolicitudes serieFondoSolicitud = (SerieFondoSolicitudes)hseries.get(solicitud.IdSerieFondo);
			RegimenSerieDeFondo regimenSerieFondo = (RegimenSerieDeFondo) hseriesRegimen.get(solicitud.IdSerieFondo + "_" +solicitud.IdRegimenTributario);
			ValidaMontoInversion.Solicitud solicitudAValidar = new ValidaMontoInversion.Solicitud();
			solicitudAValidar.IdInterno = solicitud.IdInterno;
			solicitudAValidar.FolioContrato = solicitud.FolioContrato;
			solicitudAValidar.AportesAltos = serieFondoSolicitud.AportesAltos;
			solicitudAValidar.Monto = solicitud.MontoMonedaSistema;
			solicitudAValidar.ValidaMontoMinimo = regimenSerieFondo != null ? regimenSerieFondo.ValidaMontoMinimoInstrumento : _c.SI;
			return solicitudAValidar;
		}).collect(Collectors.toList());
		
		SalidaValidaMontoInversion salidaValidaMontoInversion = validaMontoInversion.logica(con, entradaValidaMontoInversion);
		
		for(MensajeSalida mje : salidaValidaMontoInversion.P_OUT_MENSAJES){
			out.P_OUT_MENSAJES.add(mje);
			if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
				out.P_OUT_ERRORESCONTADOR++;
				for(SolicitudAporteRescate sol : solicitudesProcesar){
					if(sol.IdInterno.equals(mje.id)){
						sol.EstadoProceso = _c.EstadoRechazado;
						break;
					}
				}
			}
		}
			
		Date fechaMaximaCertificacion = Fecha.toDate("0001-01-01");
		
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso) && sol.FechaCertificacion.compareTo(fechaMaximaCertificacion) == 1){
				fechaMaximaCertificacion = sol.FechaCertificacion;
			}
		}
		
		//PATROMINO EMISOR
		if(entradaValidaPatrimonioEmisor.p_Solicitudes.size() > 0) {
			entradaValidaPatrimonioEmisor.p_Solicitudes.parallelStream().forEach(sol -> {
				BigDecimal montoPorEmisor = hashMontosPortafolioEmisor.get(sol.IdCliente + "_" + sol.IdEmisor);
				if(montoPorEmisor != null) sol.MontoPatrimonio = sol.MontoPatrimonio.add(montoPorEmisor, MathContext.DECIMAL128);
			});
			SalidaValidaPatrimonioEmisor salidaValidaPatrimonioEmisor = validaPatrimonioEmisor.logica(con, entradaValidaPatrimonioEmisor);
			for(MensajeSalida mje : salidaValidaPatrimonioEmisor.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(mje);
					for(SolicitudAporteRescate sol : solicitudesProcesar){
						if(sol.IdInterno.equals(mje.id)){
							sol.EstadoProceso = _c.EstadoRechazado;
							break;
						}
					}
				}
			}
		}
		
		//LLAMAMOS FLUJO CAJA
		List<String> portMonedaFlujo = new ArrayList<String>();
		HashMap<String,BigDecimal> portTotalesMontos = new HashMap<String,BigDecimal>();
		SaldoFlujoCaja saldoFlujoCaja = new SaldoFlujoCaja();
		SaldoFlujoCaja.ServiceEntrada entradaSaldoFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
		entradaSaldoFlujoCaja.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaSaldoFlujoCaja.P_FECHADESDE = fechaSistema;
		entradaSaldoFlujoCaja.P_FECHAHASTA = Fecha.stringToSqlDate(fechaMaximaCertificacion);
		entradaSaldoFlujoCaja.P_USO = origenAdministracionLiquidez ? "ADMINISTRACION_LIQUIDEZ" : _c.UsoFlujoCajaIngreso;
		entradaSaldoFlujoCaja.P_DESPLEGARENUI = _c.NO;
		entradaSaldoFlujoCaja.P_AGRUPARPORORIGEN = _c.NO;
		entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR>();
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			IndicadoresPortafolioFondoMutuo ind = (IndicadoresPortafolioFondoMutuo) hindicadoresPortafolioFFMM.get(sol.IdPortafolio);
			String sobregiro = ind != null ? ind.OperaLineaSobregiro : _c.NO;
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso) && _c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago) 
			   && _c.NO.equalsIgnoreCase(sobregiro)){
				String id = sol.IdPortafolio.toString() + "_" + sol.IdMoneda;
				BigDecimal montoAcum = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
				portTotalesMontos.put(id, montoAcum.add(sol.CostoMonedaLocal, MathContext.DECIMAL128));
				if(!portMonedaFlujo.contains(sol.IdPortafolio+"_"+sol.IdMoneda)){
					SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR saldo = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
					saldo.IdPortafolio = sol.IdPortafolio;
					saldo.IdMoneda = sol.IdMoneda;
					entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR.add(saldo);
					if(!portMonedaFlujo.contains(sol.IdPortafolio+"_"+sol.IdMoneda)) portMonedaFlujo.add(sol.IdPortafolio+"_"+sol.IdMoneda);
				}
			}
		}
		
		SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = new SaldoFlujoCaja.ServiceSalida();
		salidaFlujoCaja.P_OUT_FLUJOCAJA = new ArrayList<SaldoFlujoCaja.ObjFLUJOCAJA>();
		if(entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR.size() > 0){
			salidaFlujoCaja = saldoFlujoCaja.logica(con, entradaSaldoFlujoCaja);
		}
		
		//LLAMAMOS FLUJO CUSTODIA
		
		ConsultarFlujoCustodiaFondoMutuo flujo = new ConsultarFlujoCustodiaFondoMutuo();
		EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo = new EntradaConsultarFlujoCustodiaFondoMutuo();
		entradaFlujo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFlujo.p_IdUsuario = entrada.p_IdUsuario;
		entradaFlujo.p_IdModulo = entrada.p_IdModulo;
		entradaFlujo.p_IdPais = idPaisEmpresa;
		entradaFlujo.p_FechaSaldo = fechaSistema;
		entradaFlujo.p_DiasProyeccion = 0;
		List<Long> fPortafolio = new ArrayList<Long>();
		List<Long> fInstrumento = new ArrayList<Long>();
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(sol.IdSerieFondo);
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso) && serie.PatrimonioMinimo.compareTo(BigDecimal.ZERO) == 1){
				if(!fPortafolio.contains(sol.IdPortafolio)){
					fPortafolio.add(sol.IdPortafolio);
					ConsultarFlujoCustodiaFondoMutuo.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuo.InCarteras();
					cartera.IdCliente = sol.IdCliente;
					cartera.IdPortafolio = sol.IdPortafolio;
					entradaFlujo.p_Carteras.add(cartera);
				}
				if(!fInstrumento.contains(serie.IdInstrumento)){
					fInstrumento.add(serie.IdInstrumento);
					ConsultarFlujoCustodiaFondoMutuo.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuo.InInstrumento();
					instrumento.IdInstrumento = serie.IdInstrumento;
					instrumento.IdRegimenFondoMutuo = sol.IdRegimenTributario;
					entradaFlujo.p_Instrumentos.add(instrumento);
				}
			}
		}
		
		SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo = new SalidaConsultarFlujoCustodiaFondoMutuo();
		
		if(entradaFlujo.p_Carteras.size() > 0){
			salidaFlujo = flujo.logica(con, entradaFlujo);
		}
		
		//TESORERIA
		ProcesarIngresoEgresoTesoreria procesarTesoreria = new ProcesarIngresoEgresoTesoreria();
		EntradaProcesarIngresoEgresoTesoreria entradaprocesarTesoreria = new EntradaProcesarIngresoEgresoTesoreria();
		//AGF
		CrearSolicitudAGF crearSolicitudAGF = new CrearSolicitudAGF();
		EntradaCrearSolicitudAGF entradaCrearAGF = new EntradaCrearSolicitudAGF();
		
		//SE VALIDA CUSTODIA Y FLUJO CAJA, SI HAY SE INSERTA
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			if(_c.EstadoRechazado.equalsIgnoreCase(sol.EstadoProceso)) continue;
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(sol.IdSerieFondo);
			IndicadoresPortafolioFondoMutuo ind = (IndicadoresPortafolioFondoMutuo) hindicadoresPortafolioFFMM.get(sol.IdPortafolio);
			String sobregiro = ind != null ? ind.OperaLineaSobregiro : _c.NO;
			BigDecimal monto =  BigDecimal.ZERO;
			for(ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado flujoCustodia : salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO){
				if(FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso(flujoCustodia, sol)){
					monto = flujoCustodia.CantidadDisponible.multiply(sol.ValorCuota, MathContext.DECIMAL128);
					break;
				}
			}

			if(serie.PatrimonioMinimo.compareTo(sol.Monto.add(monto, MathContext.DECIMAL128)) == 1){
				sol.EstadoProceso = _c.EstadoRechazado;
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_PATRIMONIO_MINIMO_INSUFICIENTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				continue;
			}
			
			//VALIDAMOS CUENTA INVERSION, SI NO HAY ERROR SALIMOS
			if(_c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago) && _c.NO.equalsIgnoreCase(sobregiro)){
				String id = sol.IdPortafolio.toString() + "_" + sol.IdMoneda;
				BigDecimal saldo = BigDecimal.ZERO;
				BigDecimal total = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
				String fecha = Fecha.stringToSqlDate(sol.FechaCertificacion);
				for(SaldoFlujoCaja.ObjFLUJOCAJA f : salidaFlujoCaja.P_OUT_FLUJOCAJA){
					if(f.IdPortafolio.longValue() == sol.IdPortafolio && f.IdMoneda.equalsIgnoreCase(sol.IdMoneda) && f.FechaSaldo.equalsIgnoreCase(fecha)){
						saldo = f.Saldo;
					}
				}
				if(total.compareTo(saldo) == 1){
					sol.EstadoProceso = _c.EstadoRechazado;
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_SALDO_INSUFICIENTE_FLUJO_CAJA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
			}
			
			//MUEVE TESORERIA
			if(!_c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago)){
				entradaprocesarTesoreria.p_Solicitudes.add(sol);
			}
			
		}
		
		List<SolicitudAporteRescate> solicitudesInsertar = new ArrayList<SolicitudAporteRescate>();
		
		if(!_c.SI.equalsIgnoreCase(entrada.p_SimularOperacion)){
			if(entradaprocesarTesoreria.p_Solicitudes.size() > 0){
				entradaprocesarTesoreria.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaprocesarTesoreria.p_IdUsuario = entrada.p_IdUsuario;
				entradaprocesarTesoreria.p_IdModulo = entrada.p_IdModulo;
				entradaprocesarTesoreria.p_SimularOperacion = entrada.p_SimularOperacion;
				entradaprocesarTesoreria.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;		
				entradaprocesarTesoreria.p_EsIngresoAutomatico = _c.NO;
				SalidaProcesarIngresoEgresoTesoreria salidaTesoreria = procesarTesoreria.logica(con, entradaprocesarTesoreria);
				for(ResultadoProceso_Salida rs : salidaTesoreria.P_OUT_RESULTADOPROCESO){
					if(_c.EstadoRechazadoTesoreria.equalsIgnoreCase(rs.Resultado)){
						for(SolicitudAporteRescate sol : solicitudesProcesar){
							if(rs.Id.longValue() == sol.IdInterno.longValue() && rs.Campo.equalsIgnoreCase(_c.NumeroReferenciaTesoreria)){
								sol.EstadoProceso = _c.EstadoRechazado;
								break;
							}
						}
					}
				}
			}
			//VALIDAMOS SOLICITUDES CORRECTAS A INSERTAR
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
					
					solicitudesInsertar.add(sol);
					out.P_OUT_RESULTADO.add((ResultadoSolicitud) hSolicitudSalida.get(sol.IdInterno));
					SolicitudAporteRescate solRest = (SolicitudAporteRescate) hSolicitudRescates.get(sol.IdInterno * -1);
					solicitudesInsertar.add(solRest);
					if(_c.NO.equalsIgnoreCase(sol.SolicitudBatch) || _c.SI.equalsIgnoreCase(sol.AdministracionLiquidez)){
						//AGF
						CrearSolicitudAGF.Solicitudes solAGF = new CrearSolicitudAGF.Solicitudes();
						solAGF.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
						entradaCrearAGF.p_Solicitudes.add(solAGF);
						CrearSolicitudAGF.Solicitudes solAGFR = new CrearSolicitudAGF.Solicitudes();
						solAGFR.IdSolicitudAporteRescate = solRest.IdSolicitudAporteRescate;
						entradaCrearAGF.p_Solicitudes.add(solAGFR);
					}
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
						log.IdTipoRegistro = _c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) ? "SOLICITUD_APORTE_OVERNIGHT" : "SOLICITUD_RESCATE_OVERNIGHT";
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
		
		log.write("FIN::CrearSolicitudAporteOvernight");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_SolicitudesAporte\":[{\"IdInterno\":1,\"IdCliente\":1423323,\"IdPortafolio\":1450339,\"IdSerieFondo\":\"LIQUIDEZAB\",\"Monto\":5000,\"NumeroCuotas\":5,\"IdMoneda\":\"CLP\",\"IdMedioDePago\":\"CIN\",\"IdMedioDePagoRescate\":\"CIN\",\"IdPreferenciaPagoPortafolioRescate\":0,\"Visado\":\"N\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"IdRegimenTributario\":\"APV-A\",\"AdministracionLiquidez\":\"N\",\"Glosa\":\"\",\"IdAutorizadoOrigen\":0,\"FolioContrato\":0}]}";		
		EntradaCrearSolicitudAporteOvernight Objin = gson.fromJson(jsonRF, EntradaCrearSolicitudAporteOvernight.class);
		CrearSolicitudAporteOvernight  demo=new CrearSolicitudAporteOvernight();				
		try {
			SalidaCrearSolicitudAporteOvernight ObjOut=demo.logica(con, Objin);
			con.commit();
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
