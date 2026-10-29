package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.SalidaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.Moneda;
import cl.bcs.fondomutuo.pojo.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.TipoMovimientoFondoMutuo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearSolicitudRescatePorComision implements JavaCall{
	
	private final Log log = new Log("FM.CrearSolicitudRescatePorComision");
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectTipoMovimientoFondoMutuo.sql");
	private final String SQL_SELECT_MONEDA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectMoneda.sql");
	private final String SQL_INSERT_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_InsertSolicitud.sql");
	private final String SQL_SELECT_REGIMEN = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaCrearSolicitudRescatePorComision{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		
		public List<InSolicitudesRescate> p_SolicitudesRescate;
		
		public EntradaCrearSolicitudRescatePorComision() {
			this.p_SolicitudesRescate = new ArrayList<InSolicitudesRescate>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudesRescate{
		public Long IdInterno;
		public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdSerieFondo;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
    	public String IdMoneda;
    	public Long FolioContrato;
    	public String IdRegimenTributario;
    	public String IngresoAutomatico;
    	public Date FechaValorCuota;
    	
    	public InSolicitudesRescate() {
    		this.IngresoAutomatico = "N";
    	}
    	
    	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearSolicitudRescatePorComision{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearSolicitudRescatePorComision(){
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
		EntradaCrearSolicitudRescatePorComision jin = gson.fromJson(in.toString(), EntradaCrearSolicitudRescatePorComision.class);
		SalidaCrearSolicitudRescatePorComision out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearSolicitudRescatePorComision logica(Connection con, EntradaCrearSolicitudRescatePorComision entrada) throws Exception{
		log.write("INICIO::CrearSolicitudRescatePorComision");
		SalidaCrearSolicitudRescatePorComision out = new SalidaCrearSolicitudRescatePorComision();
		SQL sql=new SQL();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_SolicitudesRescate.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_RESCATE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InSolicitudesRescate mov: entrada.p_SolicitudesRescate) {
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
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String respetaValutasCargaMasiva = _c.NO;
		try {
			respetaValutasCargaMasiva = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "RespetaValutasCargaMasiva");
		}catch(Exception e) {
			log.write("ADVERTENCIA::CrearSolicitudAporteRescateNormal::No se encuentra parametro RespetaValutasCargaMasiva");
		}
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		List<String> series = new ArrayList<String>();
		List<Long> clientes = new ArrayList<Long>();
		List<Long> portafolios = new ArrayList<Long>();
		List<String> monedas = new ArrayList<String>();
		List<String> regimen = new ArrayList<String>();
		List<String> idTipoMovimientos = new ArrayList<String>();
		idTipoMovimientos.add(_c.RescateComision);
		
		for(InSolicitudesRescate mov : entrada.p_SolicitudesRescate){
			if(!series.contains(mov.IdSerieFondo)) series.add(mov.IdSerieFondo);
			if(!clientes.contains(mov.IdCliente)) clientes.add(mov.IdCliente);
			if(!portafolios.contains(mov.IdPortafolio)) portafolios.add(mov.IdPortafolio);
			if(!regimen.contains(mov.IdRegimenTributario)) regimen.add(mov.IdRegimenTributario);
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
		
		for(SerieFondoSolicitudes serie : seriesSolicitudes){
			if(!monedas.contains(serie.IdMoneda)) monedas.add(serie.IdMoneda);
		}
		params.put("IdMoneda", monedas);
		List<Moneda> monedasFondo = sql.query(con, SQL_SELECT_MONEDA, params, Moneda.class);
		params.clear();
		
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		HashMap<Long,Object> hclientes = Utils.List2Hash(clientesSolicitudes, "IdPortafolio");
		HashMap<String,Object> htipoMovimiento =  Utils.List2Hash(tipoMovimientos, "IdTipoMovimientoFondoMutuo");
		HashMap<String,Object> hmonedasFondo =  Utils.List2Hash(monedasFondo, "IdMoneda");
		HashMap<String,Object> hseriesRegimen =  Utils.List2Hash(regimenesSeries, "IdRegimenFondoMutuoSerieFondo");
		
		CalcularFechaSolicitudAporteRescate calculoFechas = new CalcularFechaSolicitudAporteRescate();
		EntradaCalcularFechaSolicitudAporteRescate entradaFechas = new EntradaCalcularFechaSolicitudAporteRescate();
		entradaFechas.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFechas.p_IdPais = idPaisEmpresa;
		
		for(InSolicitudesRescate solicitud : entrada.p_SolicitudesRescate){
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			CalcularFechaSolicitudAporteRescate.InFechas fecha = new CalcularFechaSolicitudAporteRescate.InFechas();
			if(serie != null){
				fecha.IdInterno = solicitud.IdInterno;
				fecha.IdSerieFondo = solicitud.IdSerieFondo;
				fecha.FechaEmision = Fecha.stringToSqlDate(serie.FechaFondo);
				fecha.EstadoFondo = _c.Abierto;
				fecha.EstadoLiquidacion = _c.Abierto;
				if( _c.NO.equals(respetaValutasCargaMasiva)) {
					fecha.NumeroDiasRetencion = 0L;
					fecha.NumeroValutaValorCuota = 0L;
					fecha.NumeroValutaCertificacion = 0L;
					fecha.NumeroValutaCurse = 0L;
					fecha.SolicitudBatch = _c.SI;
				}else {
					fecha.NumeroDiasRetencion = serie.DiasRetencionesRescate;
					fecha.NumeroValutaValorCuota = serie.DiasValutaValorCuotaRescate;
					fecha.NumeroValutaCertificacion = serie.DiasValutaCertificacionRescate;
					fecha.NumeroValutaCurse = serie.DiasValutaCurseRescate;
					fecha.SolicitudBatch = _c.NO;
				}
				fecha.TipoSolicitud = _c.TipoSolicitudRescate;
				fecha.FondoExtranjero = serie.FondoExtranjero;
				fecha.IdPaisFondo = serie.IdPaisOrigen;
				fecha.IdPaisEmisor = serie.IdPaisEmisor;
				Moneda moneda = (Moneda) hmonedasFondo.get(serie.IdMoneda);
				fecha.IdPaisMoneda = moneda != null ? moneda.IdPais : 0L;
				entradaFechas.p_SolicitudAporteRescate.add(fecha);
			}
		}
		
		SalidaCalcularFechaSolicitudAporteRescate salidaFechas = calculoFechas.logica(con, entradaFechas);
		
		HashMap<Long,Object> hfechas =  Utils.List2Hash(salidaFechas.P_FECHASAPORTERESCATE, "IdInterno");
		
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		for(InSolicitudesRescate solicitud : entrada.p_SolicitudesRescate){
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			ResultadoFecha fecha = (ResultadoFecha) hfechas.get(solicitud.IdInterno);
			serie.IdInterno = solicitud.IdInterno;
			if (solicitud.FechaValorCuota != null) {
				serie.Fecha = Fecha.stringToSqlDate(solicitud.FechaValorCuota);
			} else if (fecha != null) {
				serie.Fecha = Fecha.stringToSqlDate(fecha.FechaAceptacion);
			} else {
				serie.Fecha = fechaSistema;
			}
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = _c.CalculoPorCuota;
		 	serie.Monto = solicitud.Monto;
		 	serie.NumeroCuotas = solicitud.NumeroCuotas;
		 	serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.IdMonedaInversion = solicitud.IdMoneda;
		 	serie.TipoSolicitud =  _c.TipoSolicitudRescate;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = entrada.p_IdEmpresa;
		 	entradaCalculo.p_Series.add(serie);
		}
		
		SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate", Long.valueOf(entrada.p_SolicitudesRescate.size()));
		
		HashMap<Long,Object> valoresCuota = Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno");
		
		ServicioCumpleRequisitosCrearSolicitudAporteRescate cumpleRequisitos = new ServicioCumpleRequisitosCrearSolicitudAporteRescate();
		EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate entradaCumpleRequisitos = new EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate();
		entradaCumpleRequisitos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCumpleRequisitos.p_IdUsuario = entrada.p_IdUsuario;
		
		int i = 0;
		List<ResultadoSolicitud> resultadoTemp = new ArrayList<ResultadoSolicitud>();
		List<SolicitudAporteRescate> solicitudesProcesar = new ArrayList<SolicitudAporteRescate>();
		
		int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, entrada.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
        Timestamp fechaHoraUTC = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(con,entrada.p_IdEmpresa));

		for(InSolicitudesRescate solicitud : entrada.p_SolicitudesRescate){
			ClientePortafolioSolicitudes cliente = (ClientePortafolioSolicitudes) hclientes.get(solicitud.IdPortafolio);
			TipoMovimientoFondoMutuo tipoMovimiento = (TipoMovimientoFondoMutuo) htipoMovimiento.get(_c.RescateComision);
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno);
			RegimenSerieDeFondo r = (RegimenSerieDeFondo) hseriesRegimen.get(solicitud.IdSerieFondo + "_" +solicitud.IdRegimenTributario);
			ResultadoFecha fecha = (ResultadoFecha) hfechas.get(solicitud.IdInterno);
			
			SolicitudAporteRescate sol = new SolicitudAporteRescate();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes solVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio portVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio();
			
			//SE COMPLETA EL MOVIMIENTO
			sol.IdInterno = solicitud.IdInterno;
			sol.IdSolicitudAporteRescate = secuencias.get(i);
		 	sol.IdCliente = solicitud.IdCliente;
		 	sol.IdPortafolio = solicitud.IdPortafolio;
		 	sol.TipoSolicitud = _c.TipoSolicitudRescate;
		 	sol.IdSerieFondo = serie != null ? serie.IdSerieFondo : "";
		 	sol.IdTipoMovimientoFondoMutuo = _c.RescateComision;
		 	sol.IdDividendo = 0L;
			sol.IdSerieFondoDestino = "";
			sol.IdEmisorDestino = "";
			sol.IdClienteDestino = 0L;
			sol.IdPortafolioDestino = 0L;
			sol.IndicadorMLT = _c.NO;
			sol.Overnight = _c.NO;
			sol.IdSerieFondoOrigen = "";
			sol.IdSolicitudAporteRescateRelacionado =0L;
			sol.TipoCarteraNormativa = "";
			sol.IdTipoCartera = "";
			sol.CalculoPorMontoCuota = _c.CalculoPorMonto;
			sol.FechaEmision = Fecha.toDate(fechaSistema);
			sol.FechaOriginalInversion = Fecha.toDate("0001-01-01");
			//sol.FechaIngresoRegistro = Fecha.toTimeStamp(FechaDB.ObtenerFechaHoraSistemaUTC(con, entrada.p_IdEmpresa));
			sol.FechaIngresoRegistro = fechaHoraUTC;
			sol.IdMoneda = solicitud.IdMoneda;
			sol.Estado =  _c.EstadoIngresado;
			sol.IdMedioDePago = _c.CuentaInversion;
			sol.Glosa = "";
			sol.IdEmpresa = entrada.p_IdEmpresa;
			sol.IdUsuario = entrada.p_IdUsuario;
			sol.IdModulo = entrada.p_IdModulo;
			sol.IdEmisorOrigen = "";
			sol.MontoOrigen = BigDecimal.ZERO;
			sol.SolicitudBatch = _c.SI;
			sol.OrigenOperacion = _c.OrigenManual;
			sol.IdAutorizadoOrigen = 0L;
			sol.IdEmpleadoEjecutivo = cliente != null && cliente.IdEmpleadoEjecutivo != null  ? cliente.IdEmpleadoEjecutivo : 0L;
			sol.IdRegimenTributario = solicitud.IdRegimenTributario;
			sol.IngresoAutomatico = solicitud.IngresoAutomatico;
			sol.EsPorMontoLiquido = _c.NO;
			sol.IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso != null ? entrada.p_IdTipoCanalIngreso : "";
			sol.FolioContrato = solicitud.FolioContrato >0 ? solicitud.FolioContrato : 0L;
			sol.IdPersona = cliente != null ? cliente.IdPersona : 0L;
			sol.IdTipoInstrumento = serie != null ? serie.IdTipoInstrumento : "";
			sol.IdInstrumento = serie != null ? serie.IdInstrumento : 0L;
			sol.Visado = _c.SI; 
			sol.IdPreferenciaPagoPortafolio = 0L;
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
			    sol.FechaValorCuota = solicitud.FechaValorCuota != null ? solicitud.FechaValorCuota : fecha.FechaValorCuota;
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
			solicitudesProcesar.add(sol);
			
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
				solVal.EstadoFondo = _c.Abierto;
				solVal.EstadoBloqueo = serie.BloqueoAporte;
				solVal.EstadoLiquidez = _c.Abierto;
				solVal.EsFondoValido = _c.SI;
				if(!sol.IdMoneda.equalsIgnoreCase(serie.IdMoneda) && _c.NO.equalsIgnoreCase(serie.Diferido)){
					solVal.EsDiferido = _c.NO;
				}
			}
			
			if(r == null){
				solVal.EsRegimenValido = _c.NO;
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
				solicitudSalida.IdEmisorDestino = serie.IdEmisor;
			}
			solicitudSalida.IdSerieFondoOrigen = "";
			solicitudSalida.IdEmisorOrigen = "";
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
		
		List<SolicitudAporteRescate> solicitudesInsertar = new ArrayList<SolicitudAporteRescate>();
		
		if(!_c.SI.equalsIgnoreCase(entrada.p_SimularOperacion)){
			//VALIDAMOS SOLICITUDES CORRECTAS A INSERTAR
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
					solicitudesInsertar.add(sol);
					out.P_OUT_RESULTADO.add((ResultadoSolicitud) hSolicitudSalida.get(sol.IdInterno));
				}
			}
			
			//INSERTAMOS LAS SIN ERRORES
			if(solicitudesInsertar.size() > 0){
				sql.batch(con, SQL_INSERT_SOLICITUD, solicitudesInsertar, SolicitudAporteRescate.class);
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
						log.IdTipoRegistro = "SOLICITUD_RESCATE_COMISION";
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
			//VALIDAMOS SOLICITUDES CORRECTAS A SALIR
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
					out.P_OUT_RESULTADO.add((ResultadoSolicitud) hSolicitudSalida.get(sol.IdInterno));
				}
			}
		}
		
		log.write("FIN::CrearSolicitudRescatePorComision");
		return out;
	}

}
