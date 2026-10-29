package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;



import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion1N.EntradaCrearSolicitudRescateReinversion1N;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.InSolicitudesAporte;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.InSolicitudesRescate;

import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion1N.SalidaCrearSolicitudRescateReinversion1N;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen.EntradaBuscarCantidadesPorRegimen;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen.InClientes;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen.ResultadoBuscarCantidadesPorRegimen;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen.SalidaBuscarCantidadesPorRegimen;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearSolicitudReinversionPorCliente implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearSolicitudReinversionPorCliente.class);
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaCrearSolicitudReinversionPorCliente{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public BigDecimal p_TtlRescate;
		public List<InSolicitudRescateCliente> p_SolicitudesRescate;
		public List<InSolicitudAporteCliente> p_SolicitudesAporte;
		
		public EntradaCrearSolicitudReinversionPorCliente() {
			this.p_SolicitudesRescate = new ArrayList<InSolicitudRescateCliente>();
			this.p_SolicitudesAporte = new ArrayList<InSolicitudAporteCliente>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearSolicitudReinversionPorCliente{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearSolicitudReinversionPorCliente(){
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
		public Long FolioContrato; //1
		public Long IdCliente;
		public Long IdPortafolio;
    	public String IdSerieFondo;
    	public String CalculoPorMontoCuota;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
    	public String IdMoneda;
    	public String IdGrupoRegimen;
    	public String IdRegimenTributario;
    	public String Glosa;
    	public BigDecimal MontoPatrimonio;
    	public String IngresoAutomatico;
    	
    	public InSolicitudRescateCliente() {
    		this.MontoPatrimonio = BigDecimal.ZERO;
    		this.IngresoAutomatico = "N";
    	}
    	
    	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudAporteCliente{
		public Long IdInterno;
		public Long FolioContrato; //1 //1 //1
		public String IdSerieFondo;
		public String IdMoneda;
		public BigDecimal Porcentaje;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCrearSolicitudReinversionPorCliente jin = gson.fromJson(in.toString(), EntradaCrearSolicitudReinversionPorCliente.class);
		SalidaCrearSolicitudReinversionPorCliente out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearSolicitudReinversionPorCliente logica(Connection con, EntradaCrearSolicitudReinversionPorCliente entrada) throws Exception{
		logger.info("INICIO::CrearSolicitudReinversionPorCliente");
		SalidaCrearSolicitudReinversionPorCliente out = new SalidaCrearSolicitudReinversionPorCliente();

		List<String> series = new ArrayList<String>();
		List<Long> idInternos = new ArrayList<Long>();
		for(InSolicitudRescateCliente solicitud : entrada.p_SolicitudesRescate){
			if((solicitud.IdSerieFondo == null || solicitud.IdSerieFondo.isEmpty()) || ((solicitud.IdCliente == null || solicitud.IdCliente.equals(0L)) && (solicitud.IdPortafolio == null || solicitud.IdPortafolio.equals(0L)))) {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTRADA_INVALIDA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if(!idInternos.contains(solicitud.IdInterno)) {
				idInternos.add(solicitud.IdInterno);
				series.add(solicitud.IdSerieFondo);
			} else {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			
		}
		
		String fechaSistema = obtenerFechaSistema(con, entrada.p_IdEmpresa);
		
		List<SerieFondoSolicitudes> seriesSolicitudes =  obtenerSeriesFondosSolicitudes(con, entrada.p_IdEmpresa, series);
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		
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
		
		//SE LLAMA A CREAR REINVERSION FFMM
		{
			HashMap<Long,Long> idsInternoMapeo = new HashMap<Long,Long>();
			Long idInterno = 1L;
			Long idAgrupador = 1L;
			
		
			EntradaCrearSolicitudRescateReinversion1N entradaCrearSolicitudRescateReinversion = new EntradaCrearSolicitudRescateReinversion1N();
			
			entradaCrearSolicitudRescateReinversion.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearSolicitudRescateReinversion.p_IdModulo = entrada.p_IdModulo;
			entradaCrearSolicitudRescateReinversion.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearSolicitudRescateReinversion.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
			entradaCrearSolicitudRescateReinversion.p_SimularOperacion = entrada.p_SimularOperacion;
			for(ResultadoBuscarCantidadesPorRegimen resultado : salidaBuscarCantidadesPorRegimen.P_OUT_RESULTADO) {
				//En caso que el resultado sea Bonificacion Fiscal y si esta posee 0.0 de custodia disponible no se crea la solicitud
				if(resultado.Cantidad.compareTo(BigDecimal.ZERO)== 0 && resultado.IdRegimenFondoMutuo.equalsIgnoreCase("BF")) {
					continue;
				}
				InSolicitudRescateCliente inSolicitudRescateCliente = (InSolicitudRescateCliente) solEntrada.get(resultado.IdInterno);
				for(InSolicitudAporteCliente aporte : entrada.p_SolicitudesAporte) {
					if(aporte.IdInterno.equals(inSolicitudRescateCliente.IdInterno)) {
						InSolicitudesRescate inSolicitudesRescate = new InSolicitudesRescate();
						inSolicitudesRescate.IdInterno = idInterno;
						inSolicitudesRescate.IdCliente = resultado.IdCliente;
						inSolicitudesRescate.IdPortafolio = resultado.IdPortafolio;
						inSolicitudesRescate.IdSerieFondo = resultado.IdSerieFondo;
						inSolicitudesRescate.CalculoPorMontoCuota = inSolicitudRescateCliente.CalculoPorMontoCuota;
						inSolicitudesRescate.Monto = BigDecimal.ZERO;
						inSolicitudesRescate.NumeroCuotas = resultado.Cantidad;
						inSolicitudesRescate.IdMoneda = inSolicitudRescateCliente.IdMoneda;
						inSolicitudesRescate.IdMedioDePago = _c.CuentaInversion;
						inSolicitudesRescate.SolicitudBatch = _c.NO;
						inSolicitudesRescate.OrigenOperacion = _c.OrigenManual;
						inSolicitudesRescate.Glosa = inSolicitudRescateCliente.Glosa;
						inSolicitudesRescate.IdAutorizadoOrigen = 0L;
						inSolicitudesRescate.Porcentaje = resultado.Porcentaje.multiply(BigDecimal.valueOf(100L), MathContext.DECIMAL128);
						inSolicitudesRescate.Porcentaje = MonedaFormato.formatear(con, inSolicitudesRescate.Porcentaje , MonedaFormato.MONTO, "CLP").valor;
						inSolicitudesRescate.FolioContrato = inSolicitudRescateCliente.FolioContrato;
						inSolicitudesRescate.IdAgrupador = idAgrupador;
						inSolicitudesRescate.IdRegimenTributario = resultado.IdRegimenFondoMutuo;
						inSolicitudesRescate.MontoPatrimonio = inSolicitudRescateCliente.MontoPatrimonio;
						inSolicitudesRescate.IngresoAutomatico = inSolicitudRescateCliente.IngresoAutomatico;
						entradaCrearSolicitudRescateReinversion.p_SolicitudesRescate.add(inSolicitudesRescate);
						
						InSolicitudesAporte inSolicitudesAporte = new InSolicitudesAporte();
						inSolicitudesAporte.IdInterno = idInterno;
						inSolicitudesAporte.IdCliente = resultado.IdCliente;
						inSolicitudesAporte.IdPortafolio = resultado.IdPortafolio;
						inSolicitudesAporte.IdSerieFondo = aporte.IdSerieFondo;
						inSolicitudesAporte.IdEmisor = "";
						inSolicitudesAporte.Monto = BigDecimal.ZERO;
						inSolicitudesAporte.NumeroCuotas = resultado.Cantidad.multiply(aporte.Porcentaje.divide(new BigDecimal("100",MathContext.DECIMAL128)), MathContext.DECIMAL128);
						inSolicitudesAporte.IndicadorMLT = _c.NO;
						inSolicitudesAporte.IdMoneda = aporte.IdMoneda;
						inSolicitudesAporte.IdMedioDePago = _c.CuentaInversion;
						inSolicitudesAporte.IdRegimenTributario = resultado.IdRegimenFondoMutuo;
						inSolicitudesAporte.Glosa = inSolicitudRescateCliente.Glosa;
						inSolicitudesAporte.FolioContrato = inSolicitudRescateCliente.FolioContrato;
						inSolicitudesAporte.Porcentaje = aporte.Porcentaje;
						inSolicitudesAporte.IdAgrupador = idAgrupador;
						entradaCrearSolicitudRescateReinversion.p_SolicitudesAporte.add(inSolicitudesAporte);
						idsInternoMapeo.put(idInterno, resultado.IdInterno);
						idInterno++;
					}
				}
				idAgrupador++;
			}
			
			for(InSolicitudRescateCliente inSolicitudRescateCliente : solicitudesPorPortafolio) {
				CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(inSolicitudRescateCliente.IdInterno);
				for(InSolicitudAporteCliente aporte : entrada.p_SolicitudesAporte) {
					if(aporte.IdInterno.equals(inSolicitudRescateCliente.IdInterno)) {
						InSolicitudesRescate inSolicitudesRescate = new InSolicitudesRescate();
						inSolicitudesRescate.IdInterno = idInterno;
						inSolicitudesRescate.IdCliente = inSolicitudRescateCliente.IdCliente;
						inSolicitudesRescate.IdPortafolio = inSolicitudRescateCliente.IdPortafolio;
						inSolicitudesRescate.IdSerieFondo = inSolicitudRescateCliente.IdSerieFondo;
						inSolicitudesRescate.CalculoPorMontoCuota = _c.CalculoPorCuota;
						inSolicitudesRescate.Monto = BigDecimal.ZERO;
						inSolicitudesRescate.NumeroCuotas = vc.NumeroCuotas;
						inSolicitudesRescate.IdMoneda = inSolicitudRescateCliente.IdMoneda;
						inSolicitudesRescate.IdMedioDePago = _c.CuentaInversion;
						inSolicitudesRescate.SolicitudBatch = _c.NO;
						inSolicitudesRescate.OrigenOperacion = _c.OrigenManual;
						inSolicitudesRescate.Glosa = inSolicitudRescateCliente.Glosa;
						inSolicitudesRescate.IdAutorizadoOrigen = 0L;
						inSolicitudesRescate.FolioContrato = inSolicitudRescateCliente.FolioContrato;
						inSolicitudesRescate.IdAgrupador = idAgrupador;
						inSolicitudesRescate.IdRegimenTributario = inSolicitudRescateCliente.IdRegimenTributario;
						inSolicitudesRescate.MontoPatrimonio = inSolicitudRescateCliente.MontoPatrimonio;
						inSolicitudesRescate.IngresoAutomatico = inSolicitudRescateCliente.IngresoAutomatico;
						entradaCrearSolicitudRescateReinversion.p_SolicitudesRescate.add(inSolicitudesRescate);
						
						InSolicitudesAporte inSolicitudesAporte = new InSolicitudesAporte();
						inSolicitudesAporte.IdInterno = idInterno;
						inSolicitudesAporte.IdCliente = inSolicitudRescateCliente.IdCliente;
						inSolicitudesAporte.IdPortafolio = inSolicitudRescateCliente.IdPortafolio;
						inSolicitudesAporte.IdSerieFondo = aporte.IdSerieFondo;
						inSolicitudesAporte.IdEmisor = "";
						inSolicitudesAporte.Monto = BigDecimal.ZERO;
						inSolicitudesAporte.NumeroCuotas = vc.NumeroCuotas.multiply(aporte.Porcentaje.divide(new BigDecimal("100",MathContext.DECIMAL128)), MathContext.DECIMAL128);
						inSolicitudesAporte.IndicadorMLT = _c.NO;
						inSolicitudesAporte.IdMoneda = aporte.IdMoneda;
						inSolicitudesAporte.IdMedioDePago = _c.CuentaInversion;
						inSolicitudesAporte.IdRegimenTributario = inSolicitudRescateCliente.IdRegimenTributario;
						inSolicitudesAporte.Glosa = inSolicitudRescateCliente.Glosa;
						inSolicitudesAporte.FolioContrato = inSolicitudRescateCliente.FolioContrato;
						inSolicitudesAporte.Porcentaje = aporte.Porcentaje;
						
						inSolicitudesAporte.IdAgrupador = idAgrupador;
						entradaCrearSolicitudRescateReinversion.p_SolicitudesAporte.add(inSolicitudesAporte);
						idsInternoMapeo.put(idInterno, inSolicitudRescateCliente.IdInterno);
						idInterno++;
					}
				}
				idAgrupador++;
			}
			
			
			CrearSolicitudRescateReinversion1N crearSolicitudRescateReinversion = new CrearSolicitudRescateReinversion1N();
			
			SalidaCrearSolicitudRescateReinversion1N salidaCrearSolicitudRescateReinversion = crearSolicitudRescateReinversion.logica(con, entradaCrearSolicitudRescateReinversion);
			
			out.P_OUT_ERRORESCONTADOR = salidaCrearSolicitudRescateReinversion.P_OUT_ERRORESCONTADOR;
			
			for(MensajeSalida mje : salidaCrearSolicitudRescateReinversion.P_OUT_MENSAJES) {
				if(idsInternoMapeo.get(mje.id) != null) {
					mje.id = idsInternoMapeo.get(mje.id);
				}
				out.P_OUT_MENSAJES.add(mje);
			}
			
			for(ResultadoSolicitud resultado : salidaCrearSolicitudRescateReinversion.P_OUT_RESULTADO) {
				if(idsInternoMapeo.get(resultado.IdInterno) != null) {
					resultado.IdInterno = idsInternoMapeo.get(resultado.IdInterno);
				}
				out.P_OUT_RESULTADO.add(resultado);
			}
		}
		
		
		logger.info("FIN::CrearSolicitudReinversionPorCliente");
		return out;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::CrearSolicitudReinversionPorCliente.obtenerFechaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		logger.debug("FIN::CrearSolicitudReinversionPorCliente.obtenerFechaSistema");
		return fechaSistema;
	}
	
	private List<SerieFondoSolicitudes> obtenerSeriesFondosSolicitudes(Connection con, Long idEmpresa, List<String> series) throws Exception{
		logger.debug("INICIO::CrearSolicitudReinversionPorCliente.obtenerSeriesFondos");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", idEmpresa);
		params.put("IdSerieFondo", series);
		logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
		List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
		logger.debug("FIN::CrearSolicitudReinversionPorCliente.obtenerSeriesFondos");
		return seriesSolicitudes;
	}
	
	private List<CalcularMontoNumeroCuota.Resultado> obtenerMontoNumeroCuotas(Connection con, Long idEmpresa, List<InSolicitudRescateCliente> solicitudes, String fechaSistema, HashMap<String,Object> hseries) throws Exception{
		logger.debug("INICIO::CrearSolicitudReinversionPorCliente.obtenerMontoNumeroCuotas");
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
		logger.debug("FIN::CrearSolicitudReinversionPorCliente.obtenerMontoNumeroCuotas");
		return salidaCalculo.P_OUT_RESULTADO;
	}
	
	private List<InSolicitudRescateCliente> obtenerSolicitudesPorPortafolio(List<InSolicitudRescateCliente> solicitudes) {
		logger.debug("INICIO::CrearSolicitudReinversionPorCliente.obtenerSolicitudesPorPortafolio");
		List<InSolicitudRescateCliente> solicitudesPorPortafolio = solicitudes.parallelStream().filter(solicitud-> solicitud.IdPortafolio > 0).collect(Collectors.toList()); 
		logger.debug("FIN::CrearSolicitudReinversionPorCliente.obtenerSolicitudesPorPortafolio");
		return solicitudesPorPortafolio;
	}
	
	private List<InSolicitudRescateCliente> obtenerSolicitudesPorCliente(List<InSolicitudRescateCliente> solicitudes) {
		logger.debug("INICIO::CrearSolicitudReinversionPorCliente.obtenerSolicitudesPorCliente");
		List<InSolicitudRescateCliente> solicitudesPorCliente = solicitudes.parallelStream().filter(solicitud-> solicitud.IdPortafolio.equals(0L)).collect(Collectors.toList()); 
		logger.debug("FIN::CrearSolicitudReinversionPorCliente.obtenerSolicitudesPorCliente");
		return solicitudesPorCliente;
	}
	
	private SalidaBuscarCantidadesPorRegimen obtenerCantidadesPorRegimen(Connection con, EntradaCrearSolicitudReinversionPorCliente entrada, List<InSolicitudRescateCliente> solicitudes, HashMap<Long,Object> valoresCuota, HashMap<String,Object> hseries) throws Exception {
		logger.debug("INICIO::CrearSolicitudReinversionPorCliente.obtenerCantidadesPorRegimen");
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
		logger.debug("FIN::CrearSolicitudReinversionPorCliente.obtenerCantidadesPorRegimen");
		
		return salidaBuscarCantidadesPorRegimen;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			EntradaCrearSolicitudReinversionPorCliente entrada = new EntradaCrearSolicitudReinversionPorCliente();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "FMM";
			entrada.p_IdUsuario = 21L;
			entrada.p_IdTipoCanalIngreso = "WS";
			entrada.p_UsaReglasNegocio = "S";
			entrada.p_SimularOperacion = "S";
			InSolicitudRescateCliente solicitud = new InSolicitudRescateCliente();
			solicitud.FolioContrato = 1L;
			solicitud.IdInterno = 1L;
			solicitud.IdCliente = 524891L;
			solicitud.IdPortafolio = 1451287L;
			solicitud.IdSerieFondo = "FMLIQUIDEZ_A";
			solicitud.IdGrupoRegimen = "APV";
			solicitud.NumeroCuotas = new BigDecimal(1000);
			solicitud.CalculoPorMontoCuota = "C";
			solicitud.Monto = BigDecimal.ZERO;
			solicitud.IdMoneda = "CLP";
			solicitud.IdGrupoRegimen = "APV";
	    	solicitud.Glosa = "";
			entrada.p_SolicitudesRescate.add(solicitud);
			InSolicitudAporteCliente solicitudAporte = new InSolicitudAporteCliente();
			solicitudAporte.FolioContrato = 1L;
			solicitudAporte.IdInterno = 1L;
			solicitudAporte.IdMoneda = "CLP";
			solicitudAporte.IdSerieFondo = "FM_CUSTODIA_A";
			solicitudAporte.Porcentaje = new BigDecimal("100");
			entrada.p_SolicitudesAporte.add(solicitudAporte);
			
			CrearSolicitudReinversionPorCliente demo = new CrearSolicitudReinversionPorCliente();
			SalidaCrearSolicitudReinversionPorCliente salida = demo.logica(con, entrada);
			System.out.println(salida);
		}
		System.out.println("== END ==");
	}

}
