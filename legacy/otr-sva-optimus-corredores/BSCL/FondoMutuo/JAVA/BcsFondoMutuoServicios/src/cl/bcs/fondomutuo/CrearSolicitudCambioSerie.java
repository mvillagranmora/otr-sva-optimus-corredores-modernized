package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.SalidaObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo.EntradaProcesarCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo.SalidaMovimientoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo.SalidaProcesarCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.pojo.ConfiguracionPropositoPortafolio;
import cl.bcs.fondomutuo.pojo.ConfiguracionRegimenFondoMutuo;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.MovimientoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.pojo.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.TipoMovimientoFondoMutuo;
import cl.bcs.fondomutuo.utils.FondoMutuoUtils;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearSolicitudCambioSerie implements JavaCall{
	
	private final Log log = new Log("FM.CrearSolicitudCambioSerie");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectTipoMovimientoFondoMutuo.sql");
	private final String SQL_INSERT_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_InsertSolicitud.sql");
	private final String SQL_SELECT_REGIMEN = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");
	private final String SQL_CONFIGURACION_REGIMEN = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectConfiguracionRegimenFondoMutuo.sql");
	private final String SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectConfiguracionPropositoPortafolio.sql");
	
	public static class EntradaCrearSolicitudCambioSerie{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public List<InSolicitudesRescateCambioSerie> p_SolicitudesRescate;
		public List<InSolicitudesAporteCambioSerie> p_SolicitudesAporte;
		
		public EntradaCrearSolicitudCambioSerie() {
			this.p_SolicitudesRescate = new ArrayList<InSolicitudesRescateCambioSerie>();
			this.p_SolicitudesAporte = new ArrayList<InSolicitudesAporteCambioSerie>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudesRescateCambioSerie{
		public Long IdInterno;
		public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdSerieFondo;
    	public String CalculoPorMontoCuota;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
    	public String IdMoneda;
    	public String IdMedioDePago;
    	public String SolicitudBatch;
    	public String OrigenOperacion;
    	public String Glosa;
    	public Long IdAutorizadoOrigen;
    	public String Switch;
    	public Long FolioContrato;
    	public String IdRegimenTributario;
    	public String IngresoAutomatico;
    	
    	public InSolicitudesRescateCambioSerie() {
    		this.IngresoAutomatico = "N";
    	}
    	
    	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudesAporteCambioSerie{
		public Long IdInterno;
		public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdSerieFondo;
    	public String IdEmisor;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
    	public String IdMoneda;
    	public String IdMedioDePago;
    	public String Glosa;
    	public String Switch;
    	public Long FolioContrato;
    	public String IdRegimenTributario;
    	
    	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearSolicitudCambioSerie{
		
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearSolicitudCambioSerie(){
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
		EntradaCrearSolicitudCambioSerie jin = gson.fromJson(in.toString(), EntradaCrearSolicitudCambioSerie.class);
		SalidaCrearSolicitudCambioSerie out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearSolicitudCambioSerie logica(Connection con, EntradaCrearSolicitudCambioSerie entrada) throws Exception{
		log.write("INICIO CrearSolicitudCambioSerie");
		SQL sql=new SQL();
		
		SalidaCrearSolicitudCambioSerie out = new SalidaCrearSolicitudCambioSerie();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_SolicitudesRescate.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_APORTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InSolicitudesRescateCambioSerie cambioserie: entrada.p_SolicitudesRescate) {
			if(cambioserie.IdInterno.longValue() == 0L){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if (correlativos.contains(cambioserie.IdInterno)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} else {
				correlativos.add(cambioserie.IdInterno);
			}
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		int countTransaccionesBatch = 0;
		
		List<String> series = new ArrayList<String>();
		List<Long> clientes = new ArrayList<Long>();
		List<Long> portafolios = new ArrayList<Long>();
		List<String> idTipoMovimientos = new ArrayList<String>();
		List<String> regimen = new ArrayList<String>();
		idTipoMovimientos.add(_c.CambioSerie);
		for(InSolicitudesRescateCambioSerie rescates : entrada.p_SolicitudesRescate){
			if(_c.SI.equalsIgnoreCase(rescates.SolicitudBatch)){
				countTransaccionesBatch++;
			}
			if(!series.contains(rescates.IdSerieFondo)) series.add(rescates.IdSerieFondo);
			if(!clientes.contains(rescates.IdCliente)) clientes.add(rescates.IdCliente);
			if(!portafolios.contains(rescates.IdPortafolio)) portafolios.add(rescates.IdPortafolio);
			if(!regimen.contains(rescates.IdRegimenTributario)) regimen.add(rescates.IdRegimenTributario);
		}
		
		for(InSolicitudesAporteCambioSerie aportes : entrada.p_SolicitudesAporte){
			if(!series.contains(aportes.IdSerieFondo)) series.add(aportes.IdSerieFondo);
			if(!regimen.contains(aportes.IdRegimenTributario)) regimen.add(aportes.IdRegimenTributario);
		}
		
		ObtenerPerfilFondoMutuo obtenerPerfilFondoMutuo = new ObtenerPerfilFondoMutuo();
		EntradaObtenerPerfilFondoMutuo entradaObtenerPerfilFondoMutuo = new EntradaObtenerPerfilFondoMutuo();
		entradaObtenerPerfilFondoMutuo.p_IdUsuario = entrada.p_IdUsuario;
		SalidaObtenerPerfilFondoMutuo salidaObtenerPerfilFondoMutuo = obtenerPerfilFondoMutuo.logica(con, entradaObtenerPerfilFondoMutuo);
		
		if(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo == null && countTransaccionesBatch != entrada.p_SolicitudesRescate.size()){
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
		params.put("IdRegimenFondoMutuoOrigen", regimen);
		List<ConfiguracionRegimenFondoMutuo> configuracionesRegimenFFMM = sql.query(con, SQL_CONFIGURACION_REGIMEN, params, ConfiguracionRegimenFondoMutuo.class);
		params.clear();
		params.put("IdRegimenFondoMutuo", regimen);
		List<ConfiguracionPropositoPortafolio> configuracionesRegimenPortafolio = sql.query(con, SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO, params, ConfiguracionPropositoPortafolio.class);
		params.clear();
		
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		HashMap<Long,Object> hclientes = Utils.List2Hash(clientesSolicitudes, "IdPortafolio");
		HashMap<String,Object> htipoMovimiento =  Utils.List2Hash(tipoMovimientos, "IdTipoMovimientoFondoMutuo");
		HashMap<String,Object> hseriesRegimen =  Utils.List2Hash(regimenesSeries, "IdRegimenFondoMutuoSerieFondo");
		HashMap<String,Object> hregimenConfiguraciones =  Utils.List2Hash(configuracionesRegimenFFMM, "Id");
		HashMap<String,Object> hconfRegimenPortafolio =  Utils.List2Hash(configuracionesRegimenPortafolio, "Id");
		
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		for(InSolicitudesRescateCambioSerie solicitud : entrada.p_SolicitudesRescate){
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			serie.IdInterno = solicitud.IdInterno;
			serie.Fecha = serieFondo != null ? Fecha.stringToSqlDate(serieFondo.FechaFondo) : fechaSistema;
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
		 	serie.Monto = solicitud.Monto;
		 	serie.NumeroCuotas = solicitud.NumeroCuotas;
		 	serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.IdMonedaInversion = solicitud.IdMoneda;
		 	serie.TipoSolicitud = _c.TipoSolicitudRescate;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = entrada.p_IdEmpresa;
		 	entradaCalculo.p_Series.add(serie);
		}
		HashMap<Long,Object> hrescates = Utils.List2Hash(entrada.p_SolicitudesRescate, "IdInterno");
		HashMap<Long,Object> haportes = Utils.List2Hash(entrada.p_SolicitudesAporte, "IdInterno");
		for(InSolicitudesAporteCambioSerie solicitud : entrada.p_SolicitudesAporte){
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			InSolicitudesRescateCambioSerie res = (InSolicitudesRescateCambioSerie) hrescates.get(solicitud.IdInterno);
			SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			serie.IdInterno = solicitud.IdInterno * -1;
			serie.Fecha = serieFondo != null ? Fecha.stringToSqlDate(serieFondo.FechaFondo) : fechaSistema;
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = res.CalculoPorMontoCuota;
		 	serie.Monto = solicitud.Monto;
		 	serie.NumeroCuotas = solicitud.NumeroCuotas;
		 	serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.IdMonedaInversion = solicitud.IdMoneda;
		 	serie.TipoSolicitud = _c.TipoSolicitudAporte;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = entrada.p_IdEmpresa;
		 	entradaCalculo.p_Series.add(serie);
		}
		
		SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate", Long.valueOf(entrada.p_SolicitudesRescate.size()));
		
		HashMap<Long,Object> valoresCuota = Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno");
		
		List<SolicitudAporteRescate> solicitudesProcesar = new ArrayList<SolicitudAporteRescate>();
		
		ServicioCumpleRequisitosCrearSolicitudAporteRescate cumpleRequisitos = new ServicioCumpleRequisitosCrearSolicitudAporteRescate();
		EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate entradaCumpleRequisitos = new EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate();
		entradaCumpleRequisitos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCumpleRequisitos.p_IdUsuario = entrada.p_IdUsuario;
		
		int i = 0;
		
		List<ResultadoSolicitud> resultadoTemp = new ArrayList<ResultadoSolicitud>();
		
		List<Long> folioContratoSolicitudes = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato", (long) entrada.p_SolicitudesRescate.size());
		
		int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, entrada.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
        Timestamp fechaHoraUTC = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(con,entrada.p_IdEmpresa));

		for(InSolicitudesRescateCambioSerie solicitud : entrada.p_SolicitudesRescate){
			InSolicitudesAporteCambioSerie apor = (InSolicitudesAporteCambioSerie) haportes.get(solicitud.IdInterno);
			ClientePortafolioSolicitudes cliente = (ClientePortafolioSolicitudes) hclientes.get(solicitud.IdPortafolio);
			TipoMovimientoFondoMutuo tipoMovimiento = (TipoMovimientoFondoMutuo) htipoMovimiento.get(_c.CambioSerie);
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			SerieFondoSolicitudes serieDestino = (SerieFondoSolicitudes) hseries.get(apor.IdSerieFondo);
			
			CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno);
			CalcularMontoNumeroCuota.Resultado vcApor = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno * -1);
			RegimenSerieDeFondo rRescate = (RegimenSerieDeFondo) hseriesRegimen.get(solicitud.IdSerieFondo + "_" +solicitud.IdRegimenTributario);
			RegimenSerieDeFondo rAporte = (RegimenSerieDeFondo) hseriesRegimen.get(apor.IdSerieFondo + "_" +apor.IdRegimenTributario);
			ConfiguracionRegimenFondoMutuo confRegimen = (ConfiguracionRegimenFondoMutuo) hregimenConfiguraciones.get(solicitud.IdRegimenTributario + "_" + apor.IdRegimenTributario);
			ConfiguracionPropositoPortafolio confPropositoPortafolio = null;
			if(cliente != null) confPropositoPortafolio = (ConfiguracionPropositoPortafolio) hconfRegimenPortafolio.get(apor.IdRegimenTributario + "_" + cliente.Proposito);
			SolicitudAporteRescate sol = new SolicitudAporteRescate();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes solVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio portVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio();
			
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes solValApor = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio portValApor = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio();
			
			//SE COMPLETA EL MOVIMIENTO
			sol.IdInterno = solicitud.IdInterno;
			sol.IdSolicitudAporteRescate = secuencias.get(i);
		 	sol.IdCliente = solicitud.IdCliente;
		 	sol.IdPortafolio = solicitud.IdPortafolio;
		 	sol.TipoSolicitud = _c.TipoSolicitudRescate;
		 	sol.IdInstrumento = serie != null ? serie.IdInstrumento : 0L;
		 	sol.IdSerieFondo = serie != null ? serie.IdSerieFondo : "";
			sol.IdTipoMovimientoFondoMutuo = _c.CambioSerie;
			sol.IdDividendo = 0L;
			sol.IdSerieFondoDestino = apor.IdSerieFondo;
			sol.IdEmisorDestino = apor.IdEmisor;
			sol.IdClienteDestino = 0L;
			sol.IdPortafolioDestino = 0L;
			sol.IndicadorMLT = _c.NO;
			sol.Overnight = _c.NO;
			sol.IdSolicitudAporteRescateRelacionado = 0L;
			sol.IdTipoCartera = "";
			sol.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
			sol.FechaEmision = Fecha.toDate(fechaSistema);
			sol.FechaOriginalInversion = Fecha.toDate("0001-01-01");
			sol.FechaAceptacion = sol.FechaEmision;
		    sol.FechaValorCuota = sol.FechaEmision;
		    sol.FechaCertificacion = sol.FechaEmision;
		    sol.FechaLiquidacion = sol.FechaEmision;
			//sol.FechaIngresoRegistro = Fecha.toTimeStamp(FechaDB.ObtenerFechaHoraSistemaUTC(con, entrada.p_IdEmpresa));
		    sol.FechaIngresoRegistro = fechaHoraUTC;
			sol.IdMoneda = solicitud.IdMoneda;
			sol.MontoComision = BigDecimal.ZERO;
			sol.MontoComisionLocal = BigDecimal.ZERO;
			sol.Estado =  _c.EstadoCertificado;
			sol.IdMedioDePago = solicitud.IdMedioDePago;
			sol.Glosa = solicitud.Glosa;
			sol.IdEmpresa = entrada.p_IdEmpresa;
			sol.IdUsuario = entrada.p_IdUsuario;
			sol.IdModulo = entrada.p_IdModulo;
			sol.IdEmisorOrigen = serie != null ? serie.IdEmisor : "";
			sol.SolicitudBatch = solicitud.SolicitudBatch;
			sol.OrigenOperacion = solicitud.OrigenOperacion;
			sol.IdAutorizadoOrigen = solicitud.IdAutorizadoOrigen;
			sol.Switch = solicitud.Switch;
			sol.FolioContrato = sol.FolioContrato = solicitud.FolioContrato > 0 ? solicitud.FolioContrato : folioContratoSolicitudes.get(entrada.p_SolicitudesRescate.indexOf(solicitud));
			sol.IdEmpleadoEjecutivo = cliente != null && cliente.IdEmpleadoEjecutivo != null ? cliente.IdEmpleadoEjecutivo : 0L;
		    sol.IdEmpleadoEjecutivoDestino = 0L;
		    sol.EstadoProceso = _c.EstadoPendiente;
		    sol.IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso != null ? entrada.p_IdTipoCanalIngreso : "";
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
			if(rRescate != null){
				switch(rRescate.GrupoRegimen){
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
		    solicitudesProcesar.add(sol);
		    
		    //VALIDACIONES RESCATE
		    solVal.IdInterno = sol.IdInterno;
			solVal.IdCliente = sol.IdCliente;
			solVal.IdPortafolio = sol.IdPortafolio;
			solVal.IdSerieFondo = sol.IdSerieFondo;
			solVal.IdTipoMovimientoFondoMutuo = tipoMovimiento != null ? tipoMovimiento.IdTipoMovimientoFondoMutuo : "";
			solVal.Monto = sol.Monto;
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
				solVal.EsFondoValido = serie.IdFondo.equalsIgnoreCase(serieDestino.IdFondo) ? _c.SI : _c.NO;
			}
			
			if(rRescate == null){
				solVal.EsRegimenValido = _c.NO;
			}
			
			if(!solicitud.IdRegimenTributario.equals(apor.IdRegimenTributario) && confRegimen == null) {
					solVal.EsRegimenDestinoValido = _c.NO;
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
			
			solVal.IdTipoOperacion=_c.TipoOperacionRescateFondoMutuo;
			solVal.IdTipoProducto=_c.TipoOperacion;
			
			entradaCumpleRequisitos.p_Portafolios.add(portVal);
			entradaCumpleRequisitos.p_SolicitudesAporteRescate.add(solVal);
			
			//VALIDACIONES APORTE
			solValApor.IdInterno = sol.IdInterno * -1;
			solValApor.IdCliente = sol.IdCliente;
			solValApor.IdPortafolio = sol.IdPortafolio;
			solValApor.IdSerieFondo = serieDestino != null ? serieDestino.IdSerieFondo : "";
			solValApor.IdTipoMovimientoFondoMutuo = tipoMovimiento != null ? tipoMovimiento.IdTipoMovimientoFondoMutuo : "";
			solValApor.Monto = BigDecimal.ZERO;
			solValApor.NumeroCuotas = BigDecimal.ZERO;
			solValApor.ValorCuota = BigDecimal.ZERO;
			solValApor.IdEstadoSerie = "";
			solValApor.EsFechaValidaAporte = _c.SI;
			solValApor.EsFechaValidaRescate = _c.SI;
			solValApor.EsDiferido = _c.SI;
			solValApor.Overnight = _c.NO;
			solValApor.EstadoLiquidez = "";
			solValApor.EsMoneyMarket = _c.SI;
			solValApor.EsFamiliaEquivalente = _c.SI;
			solValApor.EsPortafolioRegimenValido = _c.SI;
			solValApor.EsRegimenValido = _c.SI;
			solValApor.PermiteRescateMandato = _c.SI;
			solValApor.PermiteCambioGrupo = _c.SI;
			solValApor.EsPortafolioValido = _c.SI;
			solValApor.EsCuentaInversionValido = _c.SI;
			solValApor.PermiteAporte = _c.SI;
			solValApor.EsRegimenDestinoValido = _c.SI;
			if(vcApor != null){
				solValApor.Monto = vcApor.Monto;
				solValApor.NumeroCuotas = vcApor.NumeroCuotas;
				solValApor.ValorCuota = vcApor.ValorCuota;
			}
			if(serieDestino != null){
				solValApor.IdEstadoSerie = serieDestino.IdEstado;
				solValApor.EstadoFondo = _c.Abierto;
				solValApor.EstadoBloqueo = serieDestino.BloqueoRescate;
				solValApor.EsFondoValido = serieDestino.IdFondo.equalsIgnoreCase(serieDestino.IdFondo) ? _c.SI : _c.NO;
				solValApor.EsMontoMaximo = vcApor == null || (serieDestino.MontoMaximo.compareTo(BigDecimal.ZERO) != 0 && vcApor.Monto.compareTo(serieDestino.MontoMaximo) == 1) ? _c.NO : _c.SI;
				solValApor.EsMontoMinimo = vcApor == null || (serieDestino.MontoMinimo.compareTo(BigDecimal.ZERO) != 0 && vcApor.Monto.compareTo(serieDestino.MontoMinimo) == -1) ? _c.NO : _c.SI;
				
			}
			if(rAporte == null){
				solValApor.EsRegimenValido = _c.NO;
			}else {
				solValApor.PermiteAporte = rAporte.PermiteAporte;
			}
			
			if(confPropositoPortafolio == null) solValApor.EsPortafolioRegimenValido = _c.NO;
			
			if(cliente != null){
				portValApor.IdInterno = sol.IdInterno * -1;
				portValApor.IdPortafolio = sol.IdPortafolio;
				portValApor.IdPortafolioReal = cliente.IdPortafolio;
				portValApor.IdEstadoPortafolio = cliente.IdEstadoPortafolio;
				portValApor.IdCliente = sol.IdCliente;
				portValApor.IdClienteReal = cliente.IdCliente;
				portValApor.IdEstadoCliente = cliente.IdEstadoCliente;
				portValApor.DocumentoIdentidad = cliente.DocumentoIdentidad;
				portValApor.IdEmpleadoPortafolio = cliente.IdEmpleadoEjecutivo;
				portValApor.IdClienteDestino = sol.IdClienteDestino;
				portValApor.IdClienteRealDestino = 0L;
				portValApor.IdPortafolioDestino = sol.IdPortafolio;
				portValApor.IdPortafolioRealDestino = 0L;
				portValApor.IdEstadoClienteDestino = "";
				portValApor.IdEstadoPortafolioDestino = "";
			}
			
			
			solValApor.IdTipoOperacion=_c.TipoOperacionAporteFondoMutuo;
			solValApor.IdTipoProducto=_c.TipoOperacion;
			entradaCumpleRequisitos.p_Portafolios.add(portValApor);
			entradaCumpleRequisitos.p_SolicitudesAporteRescate.add(solValApor);
			
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
		List<Long> fInstrumento = new ArrayList<Long>();
		for(SolicitudAporteRescate sol : solicitudesProcesar){
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
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
				
		SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo = flujo.logica(con, entradaFlujo);
		
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
			InSolicitudesAporteCambioSerie apor = (InSolicitudesAporteCambioSerie) haportes.get(sol.IdInterno);
			SerieFondoSolicitudes serieDestino = (SerieFondoSolicitudes) hseries.get(apor.IdSerieFondo);
			CalcularMontoNumeroCuota.Resultado vcApor = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(sol.IdInterno * -1);
			BigDecimal custodia = BigDecimal.ZERO;
			
			for(ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado flujoCustodia : salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO){
				if(FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso(flujoCustodia, sol)){
					custodia = flujoCustodia.CantidadDisponible;
					break;
				}
			}
			
			//VALIDAMOS CUSTODIA, SI NO HAY ERROR Y SALIMOS. SI HAY INSERTAMOS Y MOVEMOS CUSTODIA 
			if(sol.NumeroCuotas.compareTo(custodia) == 1){
				sol.EstadoProceso = _c.EstadoRechazado;
				out.P_OUT_ERRORESCONTADOR++;
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
				parametrosMensaje.put("Nemotecnico", sol.IdSerieFondo);
				parametrosMensaje.put("CantidadRetiro", sol.NumeroCuotas.toPlainString());
				parametrosMensaje.put("CantidadCustodia",custodia.toPlainString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FM_CLIENTE_SIN_CUSTODIA",parametrosMensaje,con,entrada.p_IdEmpresa));
				continue;
			}
			
			//Retiro
			MovimientoCustodiaFondoMutuo movCustodiaRetiro = new MovimientoCustodiaFondoMutuo();
			movCustodiaRetiro.IdGenerarMovimientoCustodia = sol.IdInterno;
			movCustodiaRetiro.FechaOperacion = sol.FechaEmision;
			movCustodiaRetiro.IdCliente = sol.IdCliente;
			movCustodiaRetiro.IdPortafolio = sol.IdPortafolio;
			movCustodiaRetiro.IdTipoProducto = _c.TipoOperacion;
			movCustodiaRetiro.IdConcepto = _c.ConceptoRescateCambioSerie;
			movCustodiaRetiro.IdTipoOperacion = _c.TipoOperacionRescateFondoMutuo;
			movCustodiaRetiro.IdOperacion = sol.IdSolicitudAporteRescate;
			movCustodiaRetiro.IdTipoComprobante = _c.TipoComprobanteCustodia;
			movCustodiaRetiro.FolioComprobante = sol.IdSolicitudAporteRescate;
			movCustodiaRetiro.FechaComprobante = sol.FechaCertificacion;
			movCustodiaRetiro.IdBolsa = _c.BCS;
			movCustodiaRetiro.IdAgente = sol.IdEmpleadoEjecutivo;
			movCustodiaRetiro.FechaLiquidacion = sol.FechaLiquidacion;
			movCustodiaRetiro.IdUsuario = entrada.p_IdUsuario;
			movCustodiaRetiro.TipoCarteraNormativa = sol.TipoCarteraNormativa;
			movCustodiaRetiro.FechaCurse = sol.FechaAceptacion;
			movCustodiaRetiro.TipoFactorAjuste = _c.UF;
			movCustodiaRetiro.ValorFactorAjusteaFechaCurse = sol.ValorUFFechaInversion;
			movCustodiaRetiro.CostoEnFactorAjuste = sol.CostoUF;
			movCustodiaRetiro.IdInstrumento = serie.IdInstrumento;
			movCustodiaRetiro.IdEmisor = serie.IdEmisor;
			movCustodiaRetiro.IdMonedaInstrumento = serie.IdMoneda;
			movCustodiaRetiro.IdBoveda = _c.COR;
			movCustodiaRetiro.Cantidad = sol.NumeroCuotas;
			movCustodiaRetiro.ValorRescate = BigDecimal.ZERO;
			movCustodiaRetiro.PrecioCompra = sol.ValorCuota;
			movCustodiaRetiro.MontoCompra = sol.MontoMonedaSistema;
			movCustodiaRetiro.MontoCompraMonedaInstrumento = sol.Monto;
			movCustodiaRetiro.IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
			movCustodiaRetiro.IdRegimenTributario = sol.IdRegimenTributario;
			entradaCustodia.p_Movimientos.add(movCustodiaRetiro);
			
			//INGRESO
			MovimientoCustodiaFondoMutuo movCustodiaIngreso = new MovimientoCustodiaFondoMutuo();
			movCustodiaIngreso.IdGenerarMovimientoCustodia = sol.IdInterno * -1;
			movCustodiaIngreso.FechaOperacion = sol.FechaEmision;
			movCustodiaIngreso.IdCliente = sol.IdCliente;
			movCustodiaIngreso.IdPortafolio = sol.IdPortafolio;
			movCustodiaIngreso.IdTipoProducto = _c.TipoOperacion;
			movCustodiaIngreso.IdConcepto = _c.ConceptoAporteCambioSerie;
			movCustodiaIngreso.IdTipoOperacion = _c.TipoOperacionAporteFondoMutuo;
			movCustodiaIngreso.IdOperacion = sol.IdSolicitudAporteRescate;
			movCustodiaIngreso.IdTipoComprobante = _c.TipoComprobanteCustodia;
			movCustodiaIngreso.FolioComprobante = sol.IdSolicitudAporteRescate;
			movCustodiaIngreso.FechaComprobante = sol.FechaCertificacion;
			movCustodiaIngreso.IdBolsa = _c.BCS;
			movCustodiaIngreso.IdAgente = sol.IdEmpleadoEjecutivo;
			movCustodiaIngreso.FechaLiquidacion = sol.FechaLiquidacion;
			movCustodiaIngreso.IdUsuario = entrada.p_IdUsuario;
			movCustodiaIngreso.TipoCarteraNormativa = sol.TipoCarteraNormativa;
			movCustodiaIngreso.FechaCurse = sol.FechaAceptacion;
			movCustodiaIngreso.TipoFactorAjuste = _c.UF;
			movCustodiaIngreso.ValorFactorAjusteaFechaCurse = sol.ValorUFFechaInversion;
			movCustodiaIngreso.CostoEnFactorAjuste = sol.CostoUF;
			movCustodiaIngreso.IdInstrumento = serieDestino.IdInstrumento;
			movCustodiaIngreso.IdEmisor = serieDestino.IdEmisor;
			movCustodiaIngreso.IdMonedaInstrumento = serieDestino.IdMoneda;
			movCustodiaIngreso.IdBoveda = _c.COR;
			movCustodiaIngreso.Cantidad = vcApor.NumeroCuotas;
			movCustodiaIngreso.ValorRescate = BigDecimal.ZERO;
			movCustodiaIngreso.PrecioCompra = vcApor.ValorCuota;
			movCustodiaIngreso.MontoCompra = vcApor.MontoMonedaSistema;
			movCustodiaIngreso.MontoCompraMonedaInstrumento =vcApor.Monto;
			movCustodiaIngreso.IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
			movCustodiaIngreso.IdRegimenTributario = apor.IdRegimenTributario;
			movCustodiaIngreso.FechaOrigenCompra = sol.FechaValorCuota;
			entradaCustodia.p_Movimientos.add(movCustodiaIngreso);
		}
		
		List<SolicitudAporteRescate> solicitudesInsertar = new ArrayList<SolicitudAporteRescate>();
		if(!_c.SI.equalsIgnoreCase(entrada.p_SimularOperacion) && entradaCustodia.p_Movimientos.size() > 0){
			
			entradaCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCustodia.p_IdUsuario = entrada.p_IdUsuario;
			entradaCustodia.p_IdModulo = entrada.p_IdModulo;
			entradaCustodia.p_simularOperacion = _c.NO;
			entradaCustodia. p_UsaReglasNegocio = _c.SI;
			log.write("Se envia a custodia",entradaCustodia);
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
			
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				if(_c.EstadoRechazado.equalsIgnoreCase(sol.EstadoProceso)) continue;
				long id = sol.IdInterno.longValue() * -1;
				for(SalidaMovimientoCustodiaFondoMutuo result : salidaCustodia.P_OUT_MOVIMIENTOS){
					if(id == result.IdGenerarMovimientoCustodia.longValue()){
						sol.IdCartera = result.IdCustodia;
						break;
					}
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
						log.IdTipoRegistro = "CAMBIO_SERIE";
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
				
		log.write("FIN CrearSolicitudCambioSerie");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMBConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_SolicitudesRescate\":[{\"IdInterno\":1,\"IdCliente\":150555,\"IdPortafolio\":189184,\"IdSerieFondo\":\"PESO_B\",\"CalculoPorMontoCuota\":\"M\",\"Monto\":26000,\"NumeroCuotas\":16.0991,\"IdMoneda\":\"CLP\",\"IdMedioDePago\":\"CIN\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"Glosa\":\"\",\"IdAutorizadoOrigen\":0,\"Switch\":\"N\",\"FolioContrato\":0}],\"p_SolicitudesAporte\":[{\"IdInterno\":1,\"IdCliente\":150555,\"IdPortafolio\":189184,\"IdSerieFondo\":\"PESO_A\",\"IdEmisor\":\"\",\"Monto\":26000,\"NumeroCuotas\":16.3317,\"IdMoneda\":\"CLP\",\"IdMedioDePago\":\"CIN\",\"Glosa\":\"\",\"Switch\":\"N\",\"FolioContrato\":0}]}";		
		EntradaCrearSolicitudCambioSerie Objin = gson.fromJson(jsonRF, EntradaCrearSolicitudCambioSerie.class);
		CrearSolicitudCambioSerie  demo=new CrearSolicitudCambioSerie();				
		try {
			SalidaCrearSolicitudCambioSerie ObjOut=demo.logica(con, Objin);
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
