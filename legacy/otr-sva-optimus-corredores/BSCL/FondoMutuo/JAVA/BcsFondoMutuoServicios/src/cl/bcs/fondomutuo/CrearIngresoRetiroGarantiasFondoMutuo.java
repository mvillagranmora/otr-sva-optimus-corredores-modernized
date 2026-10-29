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

import cl.bcs.custodia.IngresarGarantiaJava;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaEntrada;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaSalida;
import cl.bcs.custodia.IngresarGarantiaJava.ObjGarantiaCustodia;
import cl.bcs.custodia.RetirarGarantiaJava;
import cl.bcs.custodia.RetirarGarantiaJava.ObjGarantiasARetirar;
import cl.bcs.custodia.RetirarGarantiaJava.RetirarGarantiaEntrada;
import cl.bcs.custodia.RetirarGarantiaJava.RetirarGarantiaSalida;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.EntradaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.CrearSolicitudAGF.EntradaCrearSolicitudAGF;
import cl.bcs.fondomutuo.CrearSolicitudAGF.SalidaCrearSolicitudAGF;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
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

public class CrearIngresoRetiroGarantiasFondoMutuo implements JavaCall{
	
	private final Log log = new Log("FM.CrearIngresoRetiroGarantiasFondoMutuo");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectTipoMovimientoFondoMutuo.sql");
	private final String SQL_SELECT_REGIMEN = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");
	private final String SQL_INSERT_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_InsertSolicitud.sql");
	
	public static class EntradaCrearIngresoRetiroGarantiasFondoMutuo{
		public Long p_IdEmpresa;
	    public Long p_IdUsuario;
	    public String p_IdModulo;
	    public String p_SimularOperacion;
	    public String p_UsaReglasNegocio;
	    public List<InIngresoRetiroGarantiasFondoMutuo> p_SolicitudesGarantias;
	    
	    public EntradaCrearIngresoRetiroGarantiasFondoMutuo() {
	    	this.p_SolicitudesGarantias = new ArrayList<InIngresoRetiroGarantiasFondoMutuo>();
	    }
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	}
	
	public static class InIngresoRetiroGarantiasFondoMutuo{
		public Long IdInterno;
		public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdTipoMovimientoFondoMutuo;
    	public String IdSerieFondo;
		public BigDecimal NumeroCuotas;
		public Long IdContraparteGarantia;
		public Long IdInstrumentoGarantizado;
		public Long IdGarantia;
		public String IdConcepto;
		public String Glosa;
    	public String SolicitudBatch;
    	public String OrigenOperacion;
    	public Long IdAutorizadoOrigen;
    	public String IdRegimenTributario;
    	public String IngresoAutomatico;
    	
    	public InIngresoRetiroGarantiasFondoMutuo() {
    		this.IngresoAutomatico = "N";
    	}
    	
    	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearIngresoRetiroGarantiasFondoMutuo{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearIngresoRetiroGarantiasFondoMutuo(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Contraparte{
		public Long IdContraparte;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCrearIngresoRetiroGarantiasFondoMutuo jin = gson.fromJson(in.toString(), EntradaCrearIngresoRetiroGarantiasFondoMutuo.class);
		SalidaCrearIngresoRetiroGarantiasFondoMutuo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearIngresoRetiroGarantiasFondoMutuo logica(Connection con, EntradaCrearIngresoRetiroGarantiasFondoMutuo entrada) throws Exception{
		log.write("INICIO::CrearIngresoRetiroGarantiasFondoMutuo");
		SQL sql=new SQL();
		SalidaCrearIngresoRetiroGarantiasFondoMutuo out = new SalidaCrearIngresoRetiroGarantiasFondoMutuo();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_SolicitudesGarantias.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InIngresoRetiroGarantiasFondoMutuo garantia: entrada.p_SolicitudesGarantias) {
			if(garantia.IdInterno.longValue() == 0L){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if (correlativos.contains(garantia.IdInterno)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} else {
				correlativos.add(garantia.IdInterno);
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
		idTipoMovimientos.add(_c.IngresoGarantia);
		idTipoMovimientos.add(_c.RetiroGarantia);
		for(InIngresoRetiroGarantiasFondoMutuo garantia : entrada.p_SolicitudesGarantias){
			if(!series.contains(garantia.IdSerieFondo)) series.add(garantia.IdSerieFondo);
			if(!clientes.contains(garantia.IdCliente)) clientes.add(garantia.IdCliente);
			if(!portafolios.contains(garantia.IdPortafolio)) portafolios.add(garantia.IdPortafolio);
			if(!regimen.contains(garantia.IdRegimenTributario)) regimen.add(garantia.IdRegimenTributario);
			
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
		
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		HashMap<Long,Object> hclientes = Utils.List2Hash(clientesSolicitudes, "IdPortafolio");
		HashMap<String,Object> htipoMovimiento =  Utils.List2Hash(tipoMovimientos, "IdTipoMovimientoFondoMutuo");
		HashMap<String,Object> hseriesRegimen =  Utils.List2Hash(regimenesSeries, "IdRegimenFondoMutuoSerieFondo");
		HashMap<Long,Object> hentrada = Utils.List2Hash(entrada.p_SolicitudesGarantias, "IdInterno");
		
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		
		for(InIngresoRetiroGarantiasFondoMutuo solicitud : entrada.p_SolicitudesGarantias){
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			serie.IdInterno = solicitud.IdInterno;
			serie.Fecha = serieFondo != null ? Fecha.stringToSqlDate(serieFondo.FechaFondo) : fechaSistema;
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = _c.CalculoPorCuota;
		 	serie.Monto = BigDecimal.ZERO;
		 	serie.NumeroCuotas = solicitud.NumeroCuotas;
		 	serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.IdMonedaInversion = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.TipoSolicitud = _c.IngresoGarantia.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo) ? _c.TipoSolicitudAporte : _c.TipoSolicitudRescate;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = entrada.p_IdEmpresa;
		 	serie.IdTipoMovimientoFondoMutuo = solicitud.IdTipoMovimientoFondoMutuo;
		 	entradaCalculo.p_Series.add(serie);
		}
		
		SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate", Long.valueOf(entrada.p_SolicitudesGarantias.size()));
		
		HashMap<Long,Object> valoresCuota = Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno");
		
		List<SolicitudAporteRescate> solicitudesProcesar = new ArrayList<SolicitudAporteRescate>();
		
		ServicioCumpleRequisitosCrearSolicitudAporteRescate cumpleRequisitos = new ServicioCumpleRequisitosCrearSolicitudAporteRescate();
		EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate entradaCumpleRequisitos = new EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate();
		entradaCumpleRequisitos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCumpleRequisitos.p_IdUsuario = entrada.p_IdUsuario;
		
		int i = 0;
		
		List<ResultadoSolicitud> resultadoTemp = new ArrayList<ResultadoSolicitud>();
		
		List<Long> folioContratoSolicitudes = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato", (long) entrada.p_SolicitudesGarantias.size());
		
		int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, entrada.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
        Timestamp fechaHoraUTC = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(con,entrada.p_IdEmpresa));

		for(InIngresoRetiroGarantiasFondoMutuo solicitud : entrada.p_SolicitudesGarantias){
			ClientePortafolioSolicitudes cliente = (ClientePortafolioSolicitudes) hclientes.get(solicitud.IdPortafolio);
			TipoMovimientoFondoMutuo tipoMovimiento = (TipoMovimientoFondoMutuo) htipoMovimiento.get(solicitud.IdTipoMovimientoFondoMutuo);
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno);
			RegimenSerieDeFondo r = (RegimenSerieDeFondo) hseriesRegimen.get(solicitud.IdSerieFondo + "_" +solicitud.IdRegimenTributario);
			
			SolicitudAporteRescate sol = new SolicitudAporteRescate();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes solVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes();
			ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio portVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio();
			
			//SE COMPLETA EL MOVIMIENTO
			sol.IdInterno = solicitud.IdInterno;
			sol.IdSolicitudAporteRescate = secuencias.get(i);
		 	sol.IdCliente = solicitud.IdCliente;
		 	sol.IdPortafolio = solicitud.IdPortafolio;
		 	sol.TipoSolicitud = _c.IngresoGarantia.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo) ? _c.TipoSolicitudAporte : _c.TipoSolicitudRescate;
		 	sol.IdSerieFondo = serie != null ? serie.IdSerieFondo : "";
		 	sol.IdInstrumento =  serie != null ? serie.IdInstrumento  : null;
		 	sol.IdTipoMovimientoFondoMutuo = solicitud.IdTipoMovimientoFondoMutuo;
		 	sol.IdDividendo = 0L;
			sol.IdSerieFondoDestino = "";
			sol.IdEmisorDestino = "";
			sol.IdClienteDestino = 0L;
			sol.IdPortafolioDestino = 0L;
			sol.IndicadorMLT = _c.NO;
			sol.Overnight = _c.NO;
			sol.IdSolicitudAporteRescateRelacionado = 0L;
			sol.TipoCarteraNormativa = _c.IngresoGarantia.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) && r != null ? r.TipoCarteraNormativa : "";
			sol.IdTipoCartera = "";
			sol.CalculoPorMontoCuota = _c.CalculoPorCuota;
			sol.FechaEmision = Fecha.toDate(fechaSistema);
			sol.FechaOriginalInversion = Fecha.toDate("0001-01-01");
			sol.FechaAceptacion = sol.FechaEmision;
		    sol.FechaValorCuota = sol.FechaEmision;
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
			sol.FolioContrato = folioContratoSolicitudes.get(entrada.p_SolicitudesGarantias.indexOf(solicitud));
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
			if(_c.RetiroGarantia.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo) && r != null){
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
				solVal.EsFondoValido = _c.SI;
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
			if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso) && _c.IngresoGarantia.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo)){
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
		
		SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo = new SalidaConsultarFlujoCustodiaFondoMutuo();
		
		if(entradaFlujo.p_Carteras.size() > 0){
			salidaFlujo = flujo.logica(con, entradaFlujo);
		}
		
		//AGF
		CrearSolicitudAGF crearSolicitudAGF = new CrearSolicitudAGF();
		EntradaCrearSolicitudAGF entradaCrearAGF = new EntradaCrearSolicitudAGF();
		
		IngresarGarantiaJava ingresarGarantia = new IngresarGarantiaJava();
		RetirarGarantiaJava retirarGarantia = new RetirarGarantiaJava();
		
		IngresarGarantiaEntrada ingresarGarantiaEntrada = new IngresarGarantiaEntrada();
		RetirarGarantiaEntrada retirarGarantiaEntrada = new RetirarGarantiaEntrada();
		ingresarGarantiaEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		ingresarGarantiaEntrada.p_IdUsuario = entrada.p_IdUsuario;
		ingresarGarantiaEntrada.p_IdModulo = entrada.p_IdModulo;
		ingresarGarantiaEntrada.p_IdEmpleado = 0L;
		ingresarGarantiaEntrada.p_UsaReglasNegocio = _c.SI;
		ingresarGarantiaEntrada.p_SimulaOperacion = _c.NO;
		ingresarGarantiaEntrada.p_Certifica = _c.NO;
		ingresarGarantiaEntrada.p_FechaMovimiento = fechaSistema;
		
		retirarGarantiaEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		retirarGarantiaEntrada.p_IdUsuario = entrada.p_IdUsuario;
		retirarGarantiaEntrada.p_IdModulo = entrada.p_IdModulo;
		retirarGarantiaEntrada.p_IdEmpleado = 0L;
		retirarGarantiaEntrada.p_UsaReglasNegocio = _c.SI;
		retirarGarantiaEntrada.p_SimulaOperacion =  _c.NO;
		retirarGarantiaEntrada.p_FechaMovimiento = fechaSistema;
		retirarGarantiaEntrada.p_IdTipoOperacion = _c.RetiroGarantiaCustodia;
		retirarGarantiaEntrada.p_IdContraparteGarantia = 0L;
		
		//SE VALIDA CUSTODIA, SI HAY SE INSERTA Y MUEVE GARANTIA
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
			if(sol.NumeroCuotas.compareTo(custodia) == 1 && _c.IngresoGarantia.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo)){
				sol.EstadoProceso = _c.EstadoRechazado;
				out.P_OUT_ERRORESCONTADOR++;
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
				parametrosMensaje.put("Nemotecnico", sol.IdSerieFondo);
				parametrosMensaje.put("CantidadRetiro", sol.NumeroCuotas.toPlainString());
				parametrosMensaje.put("CantidadCustodia",custodia.toPlainString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FM_CLIENTE_SIN_CUSTODIA",parametrosMensaje,con,entrada.p_IdEmpresa));
				continue;
			}
			InIngresoRetiroGarantiasFondoMutuo in = (InIngresoRetiroGarantiasFondoMutuo) hentrada.get(sol.IdInterno);
			if(_c.IngresoGarantia.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo)) {
				ObjGarantiaCustodia obj = new ObjGarantiaCustodia();
				obj.IdInstrumentoGarantizado = in.IdInstrumentoGarantizado;
				obj.IdContraparteGarantia = in.IdContraparteGarantia;
				obj.IdCliente = in.IdCliente;
				obj.IdPortafolio = in.IdPortafolio;
				obj.IdConcepto = in.IdConcepto;
				obj.IdTipoOperacion = _c.IngresoGarantiaCustodia;
				obj.IdBolsa = _c.COR;
				obj.IdClasificacionTipoInstrumento = _c.FM;
				obj.IdTipoInstrumento = _c.TipoInstrumento;
				obj.IdInstrumento = serie.IdInstrumento;
				obj.IdEmisor = serie.IdEmisor;
				obj.EsFungible = _c.NO;
				obj.Cantidad = sol.NumeroCuotas;
				obj.Precio = sol.ValorCuota;
				obj.ValorRescate = sol.Monto;
				obj.MontoMonedaInstrumento = sol.Monto;
				obj.MontoMonedaSistema = sol.MontoMonedaSistema;
				obj.IdTipoComprobante = _c.TipoComprobanteCustodia;
				obj.FolioComprobante = sol.IdSolicitudAporteRescate;
				obj.FechaComprobante = sol.FechaLiquidacion;
				obj.FechaVencimientoInstrumento = _c.FechaDefecto;
				obj.IdMoneda = sol.IdMoneda;
				obj.IdMonedaEmisionInstrumento = sol.IdMoneda;
				ingresarGarantiaEntrada.p_GarantiaCustodia.add(obj);
			}else {
				ObjGarantiasARetirar obj = new ObjGarantiasARetirar();
				obj.IdConcepto = in.IdConcepto;
				obj.Cantidad = sol.NumeroCuotas;
				obj.ValorRescate = sol.Monto;
				obj.MontoMonedaInstrumento = sol.Monto;
				obj.IdGarantia = in.IdGarantia;
				obj.ConCertificadoAutomatico = _c.SI;
				retirarGarantiaEntrada.p_tablaRetiro.add(obj);
			}
		}
		
		
		
		List<SolicitudAporteRescate> solicitudesInsertar = new ArrayList<SolicitudAporteRescate>();
		if(!_c.SI.equalsIgnoreCase(entrada.p_SimularOperacion) && (retirarGarantiaEntrada.p_tablaRetiro.size() > 0 || ingresarGarantiaEntrada.p_GarantiaCustodia.size() > 0)){
			if(ingresarGarantiaEntrada.p_GarantiaCustodia.size() > 0) {
				IngresarGarantiaSalida ingresarGarantiaSalida = ingresarGarantia.logica(con, ingresarGarantiaEntrada);
				out.P_OUT_ERRORESCONTADOR += ingresarGarantiaSalida.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.addAll(ingresarGarantiaSalida.P_OUT_MENSAJES);
			}
			
			if(retirarGarantiaEntrada.p_tablaRetiro.size() > 0) {
				RetirarGarantiaSalida retirarGarantiaSalida = retirarGarantia.logica(con, retirarGarantiaEntrada);
				out.P_OUT_ERRORESCONTADOR += retirarGarantiaSalida.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.addAll(retirarGarantiaSalida.P_OUT_MENSAJES);
			}
			
			if(out.P_OUT_ERRORESCONTADOR.equals(0L)) {
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
			}
		}else{
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
					out.P_OUT_RESULTADO.add((ResultadoSolicitud) hSolicitudSalida.get(sol.IdInterno));
				}
			}
		}
		
		log.write("FIN::CrearIngresoRetiroGarantiasFondoMutuo");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPRESURAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		
		String Json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1006,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_SolicitudesGarantias\":[{\"IdInterno\":1,\"IdCliente\":44078,\"IdPortafolio\":126528,\"IdTipoMovimientoFondoMutuo\":\"RETGTA\",\"IdSerieFondo\":\"00001CCLP\",\"NumeroCuotas\":1000,\"IdContraparteGarantia\":0,\"IdInstrumentoGarantizado\":0,\"IdRegimenTributario\":\"18T\",\"IdGarantia\":1704,\"IdConcepto\":\"FALLEC\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"Glosa\":\"\",\"IdAutorizadoOrigen\":0}]}";
		
		EntradaCrearIngresoRetiroGarantiasFondoMutuo Objin = gson.fromJson(Json, EntradaCrearIngresoRetiroGarantiasFondoMutuo.class);
		CrearIngresoRetiroGarantiasFondoMutuo  demo=new CrearIngresoRetiroGarantiasFondoMutuo();				
		try {
			SalidaCrearIngresoRetiroGarantiasFondoMutuo ObjOut=demo.logica(con, Objin);
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
