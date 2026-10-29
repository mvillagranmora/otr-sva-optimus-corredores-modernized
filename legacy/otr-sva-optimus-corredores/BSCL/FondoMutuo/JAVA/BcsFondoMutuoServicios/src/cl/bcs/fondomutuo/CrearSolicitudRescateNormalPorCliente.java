package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.PreferenciaPago;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen.EntradaBuscarCantidadesPorRegimen;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen.InClientes;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen.ResultadoBuscarCantidadesPorRegimen;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen.SalidaBuscarCantidadesPorRegimen;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearSolicitudRescateNormalPorCliente implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearSolicitudRescateNormalPorCliente.class);
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPreferenciaPago.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudRescateNormalPorCliente_SelectPortafolio.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaCrearSolicitudRescateNormalPorCliente{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public List<InSolicitudRescateCliente> p_SolicitudesRescate;
		
		public EntradaCrearSolicitudRescateNormalPorCliente() {
			this.p_SolicitudesRescate = new ArrayList<InSolicitudRescateCliente>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearSolicitudRescateNormalPorCliente{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearSolicitudRescateNormalPorCliente(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudRescateCliente{
		public Long IdInterno;
		public Long FolioContrato;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdSerieFondo;
		public String CalculoPorMontoCuota;
		public BigDecimal Monto;
    	public BigDecimal NumeroCuotas;
    	public String IdMoneda;
    	public String IdMonedaPago;
    	public String IdMedioDePago;
    	public Long IdCuentaBancaria;
    	public String IdGrupoRegimen;
    	public String Glosa;
    	public BigDecimal MontoPatrimonio;
    	public String IngresoAutomatico;
    	public Long IdPreferenciaPagoPortafolio;
    	public InSolicitudRescateCliente() {
    		this.MontoPatrimonio = BigDecimal.ZERO;
    		this.IdCuentaBancaria = 0L;
    		this.IngresoAutomatico = "N";
    		this.IdPreferenciaPagoPortafolio = -1L;
    	}
    	
    	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio{
		public Long IdPortafolio;
		public String CodigoPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCrearSolicitudRescateNormalPorCliente jin = gson.fromJson(in.toString(), EntradaCrearSolicitudRescateNormalPorCliente.class);
		SalidaCrearSolicitudRescateNormalPorCliente out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearSolicitudRescateNormalPorCliente logica(Connection con, EntradaCrearSolicitudRescateNormalPorCliente entrada) throws Exception{
		logger.info("INICIO::CrearSolicitudRescateNormalPorCliente");
		logger.debug("ENTRADA::CrearSolicitudRescateNormalPorCliente::" + entrada);
		SalidaCrearSolicitudRescateNormalPorCliente out = new SalidaCrearSolicitudRescateNormalPorCliente();
		
		List<String> series = new ArrayList<String>();
		List<Long> idsInternos = new ArrayList<Long>();
		List<String> idMonedas = new ArrayList<String>();
		List<Long> idCuentasBancarias = new ArrayList<Long>();
		List<Long> idClientes = new ArrayList<Long>();
		List<String> idMediosPago = new ArrayList<String>();
		
		for(InSolicitudRescateCliente solicitud : entrada.p_SolicitudesRescate){
			if((solicitud.IdSerieFondo == null || solicitud.IdSerieFondo.isEmpty()) || ((solicitud.IdCliente == null || solicitud.IdCliente.equals(0L)) && (solicitud.IdPortafolio == null || solicitud.IdPortafolio.equals(0L)))) {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTRADA_INVALIDA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if(!idsInternos.contains(solicitud.IdInterno)) {
				idsInternos.add(solicitud.IdInterno);
				series.add(solicitud.IdSerieFondo);
				idClientes.add(solicitud.IdCliente);
				idMonedas.add(solicitud.IdMonedaPago);
				idCuentasBancarias.add(solicitud.IdCuentaBancaria);
				idMediosPago.add(solicitud.IdMedioDePago);
			} else {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
		}
		
		String fechaSistema = obtenerFechaSistema(con,entrada.p_IdEmpresa);
		
		List<SerieFondoSolicitudes> seriesSolicitudes = obtenerSeriesFondosSolicitudes(con, entrada.p_IdEmpresa, series);
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		List<PreferenciaPago> preferenciasPago = obtenerPreferenciaPago(con, entrada.p_IdEmpresa, idMonedas, idCuentasBancarias, idMediosPago, idClientes);
		
		
		HashMap<String,Object> hashPreferenciasPago = Utils.List2Hash(preferenciasPago, "Id");
		
		List<CalcularMontoNumeroCuota.Resultado> resultadoCalculo = obtenerMontoNumeroCuotas(con, entrada.p_IdEmpresa, entrada.p_SolicitudesRescate, fechaSistema, hseries);
		
		HashMap<Long,Object> valoresCuota = Utils.List2Hash(resultadoCalculo, "IdInterno");
		HashMap<Long,Object> solEntrada = Utils.List2Hash(entrada.p_SolicitudesRescate, "IdInterno");
		
		List<InSolicitudRescateCliente> solicitudesPorPortafolio = obtenerSolicitudesPorPortafolio(entrada.p_SolicitudesRescate);
		List<InSolicitudRescateCliente> solicitudesPorCliente = obtenerSolicitudesPorCliente(entrada.p_SolicitudesRescate);
		
		SalidaBuscarCantidadesPorRegimen salidaBuscarCantidadesPorRegimen = obtenerCantidadesPorRegimen(con, entrada, solicitudesPorCliente, valoresCuota, hseries);
		
		if(salidaBuscarCantidadesPorRegimen.P_OUT_ERRORESCONTADOR.longValue() > 0) {
			out.P_OUT_ERRORESCONTADOR = salidaBuscarCantidadesPorRegimen.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaBuscarCantidadesPorRegimen.P_OUT_MENSAJES);
			return out;
		}
		
		List<Portafolio> portafolios = obtenerPortafolio(con, solicitudesPorPortafolio);
		Map<Long,Portafolio> hashPortafolio = portafolios.parallelStream().collect(Collectors.toMap(portafolio->portafolio.IdPortafolio, portafolio->portafolio));
		
		//SE LLAMA A CREAR RESCATE FFMM
		{
			Long i = 1L;
			HashMap<Long,Long> idsFolioContrato = new HashMap<Long,Long>();
			EntradaCrearSolicitudAporteRescateNormal entradaCrearSolicitudAporteRescateNormal = new EntradaCrearSolicitudAporteRescateNormal();
			entradaCrearSolicitudAporteRescateNormal.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearSolicitudAporteRescateNormal.p_IdModulo = entrada.p_IdModulo;
			entradaCrearSolicitudAporteRescateNormal.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearSolicitudAporteRescateNormal.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
			entradaCrearSolicitudAporteRescateNormal.p_SimularOperacion = entrada.p_SimularOperacion;
			for(ResultadoBuscarCantidadesPorRegimen resultado : salidaBuscarCantidadesPorRegimen.P_OUT_RESULTADO) {
				InSolicitudRescateCliente inSolicitudRescateCliente = (InSolicitudRescateCliente) solEntrada.get(resultado.IdInterno);
				InSolicitudesAporteRescate inSolicitudesAporteRescate = new InSolicitudesAporteRescate();
				String id = resultado.IdPortafolio + "_" + inSolicitudRescateCliente.IdMoneda + "_" + inSolicitudRescateCliente.IdMedioDePago + "_" + inSolicitudRescateCliente.IdCuentaBancaria;
				PreferenciaPago preferenciaPago = (PreferenciaPago) hashPreferenciasPago.get(id);
				inSolicitudesAporteRescate.IdInterno = i;
				inSolicitudesAporteRescate.IdCliente = resultado.IdCliente;
				inSolicitudesAporteRescate.IdPortafolio = resultado.IdPortafolio;
				inSolicitudesAporteRescate.TipoSolicitud = _c.TipoSolicitudRescate;
				inSolicitudesAporteRescate.IdSerieFondo = resultado.IdSerieFondo;
				inSolicitudesAporteRescate.CalculoPorMontoCuota = _c.CalculoPorCuota;
				inSolicitudesAporteRescate.EsPorMontoLiquido = _c.NO;
				inSolicitudesAporteRescate.Monto = BigDecimal.ZERO;
				inSolicitudesAporteRescate.NumeroCuotas = resultado.Cantidad;
				inSolicitudesAporteRescate.IdMoneda = inSolicitudRescateCliente.IdMoneda;
				inSolicitudesAporteRescate.IdMedioDePago = inSolicitudRescateCliente.IdMedioDePago;
				if(inSolicitudRescateCliente.IdPreferenciaPagoPortafolio > 0) {
					inSolicitudesAporteRescate.IdPreferenciaPagoPortafolio = inSolicitudRescateCliente.IdPreferenciaPagoPortafolio;
				}else {
					inSolicitudesAporteRescate.IdPreferenciaPagoPortafolio = preferenciaPago == null ? 0 : preferenciaPago.IdPreferenciaPagoPortafolio;
				}
				logger.debug("PREFERENCIA PAGO PORTAFOLIO::CrearSolicitudAporteRescateNormal::" + inSolicitudesAporteRescate.IdPreferenciaPagoPortafolio);
				inSolicitudesAporteRescate.Visado = _c.SI;
				inSolicitudesAporteRescate.SolicitudBatch = _c.NO;
				inSolicitudesAporteRescate.OrigenOperacion = _c.OrigenManual;
				inSolicitudesAporteRescate.FechaOperacion = fechaSistema;
				inSolicitudesAporteRescate.IdRegimenTributario = resultado.IdRegimenFondoMutuo;
				inSolicitudesAporteRescate.Glosa = inSolicitudRescateCliente.Glosa;
				inSolicitudesAporteRescate.IndicadorMLT = _c.NO;
				inSolicitudesAporteRescate.IdSerieFondoDestino = "";
				inSolicitudesAporteRescate.IdEmisorDestino = "";
				inSolicitudesAporteRescate.IdAutorizadoOrigen = 0L;
				inSolicitudesAporteRescate.Porcentaje = BigDecimal.ZERO;
				inSolicitudesAporteRescate.FolioContrato = inSolicitudRescateCliente.FolioContrato;
				inSolicitudesAporteRescate.IdMonedaPago = inSolicitudRescateCliente.IdMonedaPago;
				inSolicitudesAporteRescate.MontoPatrimonio = inSolicitudRescateCliente.MontoPatrimonio;
				inSolicitudesAporteRescate.IngresoAutomatico = inSolicitudRescateCliente.IngresoAutomatico;
				entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(inSolicitudesAporteRescate);
				idsFolioContrato.put(i, resultado.IdInterno);
				i++;
			}
			
			for(InSolicitudRescateCliente inSolicitudRescateCliente : solicitudesPorPortafolio) {
				InSolicitudesAporteRescate inSolicitudesAporteRescate = new InSolicitudesAporteRescate();
				String id = inSolicitudRescateCliente.IdPortafolio + "_" + inSolicitudRescateCliente.IdMoneda + "_" + inSolicitudRescateCliente.IdMedioDePago + "_" + inSolicitudRescateCliente.IdCuentaBancaria;
				CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(inSolicitudRescateCliente.IdInterno);
				Portafolio portafolio = hashPortafolio.get(inSolicitudRescateCliente.IdPortafolio);
				PreferenciaPago preferenciaPago = (PreferenciaPago) hashPreferenciasPago.get(id);
				inSolicitudesAporteRescate.IdInterno = i;
				inSolicitudesAporteRescate.IdCliente = inSolicitudRescateCliente.IdCliente;
				inSolicitudesAporteRescate.IdPortafolio = inSolicitudRescateCliente.IdPortafolio;
				inSolicitudesAporteRescate.TipoSolicitud = _c.TipoSolicitudRescate;
				inSolicitudesAporteRescate.IdSerieFondo = inSolicitudRescateCliente.IdSerieFondo;
				inSolicitudesAporteRescate.CalculoPorMontoCuota = inSolicitudRescateCliente.CalculoPorMontoCuota.equalsIgnoreCase("T") ?  inSolicitudRescateCliente.CalculoPorMontoCuota :_c.CalculoPorCuota ;
				inSolicitudesAporteRescate.EsPorMontoLiquido = _c.NO;
				inSolicitudesAporteRescate.Monto = BigDecimal.ZERO;
				inSolicitudesAporteRescate.NumeroCuotas = vc.NumeroCuotas;
				inSolicitudesAporteRescate.IdMoneda = inSolicitudRescateCliente.IdMoneda;
				inSolicitudesAporteRescate.IdMedioDePago = inSolicitudRescateCliente.IdMedioDePago;
				if(inSolicitudRescateCliente.IdPreferenciaPagoPortafolio > 0) {
					inSolicitudesAporteRescate.IdPreferenciaPagoPortafolio = inSolicitudRescateCliente.IdPreferenciaPagoPortafolio;
				}else {
					inSolicitudesAporteRescate.IdPreferenciaPagoPortafolio = preferenciaPago == null ? 0 : preferenciaPago.IdPreferenciaPagoPortafolio;
				}
				logger.debug("PREFERENCIA PAGO PORTAFOLIO::CrearSolicitudAporteRescateNormal::" + inSolicitudesAporteRescate.IdPreferenciaPagoPortafolio);
				inSolicitudesAporteRescate.Visado = _c.SI;
				inSolicitudesAporteRescate.SolicitudBatch = _c.NO;
				inSolicitudesAporteRescate.OrigenOperacion = _c.OrigenManual;
				inSolicitudesAporteRescate.FechaOperacion = fechaSistema;
				inSolicitudesAporteRescate.IdRegimenTributario = portafolio != null ? portafolio.CodigoPortafolio : "";
				inSolicitudesAporteRescate.Glosa = inSolicitudRescateCliente.Glosa;
				inSolicitudesAporteRescate.IndicadorMLT = _c.NO;
				inSolicitudesAporteRescate.IdSerieFondoDestino = "";
				inSolicitudesAporteRescate.IdEmisorDestino = "";
				inSolicitudesAporteRescate.IdAutorizadoOrigen = 0L;
				inSolicitudesAporteRescate.Porcentaje = BigDecimal.ZERO;
				inSolicitudesAporteRescate.FolioContrato = inSolicitudRescateCliente.FolioContrato;
				inSolicitudesAporteRescate.IdMonedaPago = inSolicitudRescateCliente.IdMonedaPago;
				inSolicitudesAporteRescate.MontoPatrimonio = inSolicitudRescateCliente.MontoPatrimonio;
				inSolicitudesAporteRescate.IngresoAutomatico = inSolicitudRescateCliente.IngresoAutomatico;
				entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(inSolicitudesAporteRescate);
				idsFolioContrato.put(i, inSolicitudesAporteRescate.IdInterno);
				i++;
			}
			
			CrearSolicitudAporteRescateNormal crearSolicitudAporteRescateNormal = new CrearSolicitudAporteRescateNormal();
			
			SalidaCrearSolicitudAporteRescateNormal salidaCrearSolicitudAporteRescateNormal = crearSolicitudAporteRescateNormal.logica(con, entradaCrearSolicitudAporteRescateNormal);
			
			out.P_OUT_ERRORESCONTADOR = salidaCrearSolicitudAporteRescateNormal.P_OUT_ERRORESCONTADOR;
			
			for(MensajeSalida mje : salidaCrearSolicitudAporteRescateNormal.P_OUT_MENSAJES) {
				if(idsFolioContrato.get(mje.id) != null) {
					mje.id = idsFolioContrato.get(mje.id);
				}
				out.P_OUT_MENSAJES.add(mje);
			}
			
			for(ResultadoSolicitud resultado : salidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO) {
				if(idsFolioContrato.get(resultado.IdInterno) != null) {
					resultado.IdInterno = idsFolioContrato.get(resultado.IdInterno);
				}
				out.P_OUT_RESULTADO.add(resultado);
			}
		}
		
		logger.debug("SALIDA::CrearSolicitudRescateNormalPorCliente" + out);
		logger.info("FIN::CrearSolicitudRescateNormalPorCliente");
		return out;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::CrearSolicitudRescateNormalPorCliente.obtenerFechaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		logger.debug("FIN::CrearSolicitudRescateNormalPorCliente.obtenerFechaSistema");
		return fechaSistema;
	}
	
	private List<SerieFondoSolicitudes> obtenerSeriesFondosSolicitudes(Connection con, Long idEmpresa, List<String> series) throws Exception{
		logger.debug("INICIO::CrearSolicitudRescateNormalPorCliente.obtenerSeriesFondos");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", idEmpresa);
		params.put("IdSerieFondo", series);
		logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
		List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
		logger.debug("FIN::CrearSolicitudRescateNormalPorCliente.obtenerSeriesFondos");
		return seriesSolicitudes;
	}
	
	private List<PreferenciaPago> obtenerPreferenciaPago(Connection con, Long idEmpresa, List<String> idMonedas, List<Long> idCuentasBancarias, List<String> idMediosPago, List<Long> idClientes) throws Exception{
		logger.debug("INICIO::CrearSolicitudRescateNormalPorCliente.obtenerPreferenciaPago");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", idEmpresa);
		params.put("IdMoneda", idMonedas);
		params.put("IdCuentaBancaria", idCuentasBancarias);
		params.put("IdMedioDePago", idMediosPago);
		params.put("IdCliente", idClientes);
		
		logger.debug("INIT CONSULTA PREF PAGO PORTAFOLIO");
		logger.debug("IdEmpresa: " + idEmpresa);
		logger.debug("IdMoneda: " + idMonedas);
		logger.debug("IdCuentaBancaria: " + idCuentasBancarias);
		logger.debug("IdMedioDePago: " + idMediosPago);
		logger.debug("IdCliente: " +idClientes);
		logger.debug("FIN CONSULTA PREF PAGO PORTAFOLIO");
		logger.debug("INICIO::SQL_SELECT_PREFERENCIA_PAGO");
		List<PreferenciaPago> preferenciasPago = sql.query(con, SQL_SELECT_PREFERENCIA_PAGO, params, PreferenciaPago.class);
		logger.debug("FIN::SQL_SELECT_PREFERENCIA_PAGO");
		logger.debug("FIN::CrearSolicitudRescateNormalPorCliente.obtenerPreferenciaPago");
		logger.debug("FIN CONSULTA PREF PAGO PORTAFOLIO");
		return preferenciasPago;
	}
	
	private List<CalcularMontoNumeroCuota.Resultado> obtenerMontoNumeroCuotas(Connection con, Long idEmpresa, List<InSolicitudRescateCliente> solicitudes, String fechaSistema, HashMap<String,Object> hseries) throws Exception{
		logger.debug("INICIO::CrearSolicitudRescateNormalPorCliente.obtenerMontoNumeroCuotas");
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = idEmpresa;
		for(InSolicitudRescateCliente solicitud : solicitudes){
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			serie.IdInterno = solicitud.IdInterno;
			serie.Fecha = fechaSistema;
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
		 	serie.Monto = solicitud.Monto;
		 	serie.NumeroCuotas = solicitud.NumeroCuotas;
		 	serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.IdMonedaInversion = solicitud.IdMoneda;
		 	serie.TipoSolicitud =  _c.TipoSolicitudRescate;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = idEmpresa;
		 	entradaCalculo.p_Series.add(serie);
		}
		
		SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);
		logger.debug("FIN::CrearSolicitudRescateNormalPorCliente.obtenerMontoNumeroCuotas");
		return salidaCalculo.P_OUT_RESULTADO;
	}
	
	private SalidaBuscarCantidadesPorRegimen obtenerCantidadesPorRegimen(Connection con, EntradaCrearSolicitudRescateNormalPorCliente entrada, List<InSolicitudRescateCliente> solicitudes, HashMap<Long,Object> valoresCuota, HashMap<String,Object> hseries) throws Exception {
		logger.debug("INICIO::CrearSolicitudRescateNormalPorCliente.obtenerCantidadesPorRegimen");
		EntradaBuscarCantidadesPorRegimen entradaBuscarCantidadesPorRegimen = new EntradaBuscarCantidadesPorRegimen();
		entradaBuscarCantidadesPorRegimen.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaBuscarCantidadesPorRegimen.p_IdModulo = entrada.p_IdModulo;
		entradaBuscarCantidadesPorRegimen.p_IdUsuario = entrada.p_IdUsuario;
		
		for(InSolicitudRescateCliente solicitud : solicitudes) {
			CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno);
			SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
			InClientes cliente = new InClientes();
			cliente.IdInterno = solicitud.IdInterno;
			cliente.IdCliente = solicitud.IdCliente;
			cliente.IdInstrumento = serieFondo.IdInstrumento;
			if(vc != null) {
				cliente.Cantidad = vc.NumeroCuotas;
			}
			cliente.IdGrupoRegimen = solicitud.IdGrupoRegimen;
			cliente.IdSerieFondo = solicitud.IdSerieFondo;
			entradaBuscarCantidadesPorRegimen.p_Clientes.add(cliente);
		}
		
		BuscarCantidadesPorRegimen buscarCantidadesPorRegimen = new BuscarCantidadesPorRegimen();
		
		SalidaBuscarCantidadesPorRegimen salidaBuscarCantidadesPorRegimen = buscarCantidadesPorRegimen.logica(con, entradaBuscarCantidadesPorRegimen);
		logger.debug("FIN::CrearSolicitudRescateNormalPorCliente.obtenerCantidadesPorRegimen");
		
		return salidaBuscarCantidadesPorRegimen;
	}
	
	private List<InSolicitudRescateCliente> obtenerSolicitudesPorPortafolio(List<InSolicitudRescateCliente> solicitudes) {
		logger.debug("INICIO::CrearSolicitudRescateNormalPorCliente.obtenerSolicitudesPorPortafolio");
		List<InSolicitudRescateCliente> solicitudesPorPortafolio = solicitudes.parallelStream().filter(solicitud-> solicitud.IdPortafolio > 0).collect(Collectors.toList()); 
		logger.debug("FIN::CrearSolicitudRescateNormalPorCliente.obtenerSolicitudesPorPortafolio");
		return solicitudesPorPortafolio;
	}
	
	private List<InSolicitudRescateCliente> obtenerSolicitudesPorCliente(List<InSolicitudRescateCliente> solicitudes) {
		logger.debug("INICIO::CrearSolicitudRescateNormalPorCliente.obtenerSolicitudesPorCliente");
		List<InSolicitudRescateCliente> solicitudesPorCliente = solicitudes.parallelStream().filter(solicitud-> solicitud.IdPortafolio.equals(0L)).collect(Collectors.toList()); 
		logger.debug("FIN::CrearSolicitudRescateNormalPorCliente.obtenerSolicitudesPorCliente");
		return solicitudesPorCliente;
	}
	
	private List<Portafolio> obtenerPortafolio(Connection con, List<InSolicitudRescateCliente> solicitudes) throws Exception{
		logger.debug("INICIO::CrearSolicitudRescateNormalPorCliente.obtenerPortafolio");
		SQL sql=new SQL();
		List<Long> idPortafolios = solicitudes.parallelStream().map(solicitud-> solicitud.IdPortafolio).collect(Collectors.toList());
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdPortafolio", idPortafolios);
		logger.debug("INICIO::SQL_SELECT_PORTAFOLIO");
		List<Portafolio> portafolios = sql.query(con, SQL_SELECT_PORTAFOLIO, params, Portafolio.class);
		logger.debug("FIN::SQL_SELECT_PORTAFOLIO");
		logger.debug("FIN::CrearSolicitudRescateNormalPorCliente.obtenerPortafolio");
		return portafolios;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPRESURAConnection()) {
			EntradaCrearSolicitudRescateNormalPorCliente entrada = new EntradaCrearSolicitudRescateNormalPorCliente();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "FMM";
			entrada.p_IdUsuario = 15L;
			entrada.p_IdTipoCanalIngreso = "WS";
			entrada.p_UsaReglasNegocio = "S";
			entrada.p_SimularOperacion = "N";
			InSolicitudRescateCliente solicitud = new InSolicitudRescateCliente();
			solicitud.IdInterno = 1L;
			solicitud.FolioContrato = 1L;
			solicitud.IdCliente = 67062L;
			solicitud.IdPortafolio = 407831L;
			solicitud.IdSerieFondo = "00001B-APVCLP";
			solicitud.IdGrupoRegimen = "APV";
			solicitud.NumeroCuotas = new BigDecimal(1000);
			solicitud.CalculoPorMontoCuota = "C";
			solicitud.Monto = BigDecimal.ZERO;
			solicitud.IdMoneda = "CLP";
			solicitud.IdMonedaPago = "CLP";
			solicitud.IdMedioDePago = "CIN";
			solicitud.IdCuentaBancaria = 0L;
	    	solicitud.Glosa = "";
			
			entrada.p_SolicitudesRescate.add(solicitud);
			
			CrearSolicitudRescateNormalPorCliente crearSolicitudRescateNormalPorCliente = new CrearSolicitudRescateNormalPorCliente();
			SalidaCrearSolicitudRescateNormalPorCliente salida = crearSolicitudRescateNormalPorCliente.logica(con, entrada);
			System.out.println(salida);
			con.commit();
			
		}
		
		System.out.println("== END ==");
	}
	
}
