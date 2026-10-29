package cl.bcs.fondomutuo;


import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CalculaCustodiaRescate.EntradaCalculaCustodiaRescate;
import cl.bcs.fondomutuo.CalculaCustodiaRescate.ResultadoCalculaCustodiaRescate;
import cl.bcs.fondomutuo.CalculaCustodiaRescate.SalidaCalculaCustodiaRescate;
import cl.bcs.fondomutuo.CalcularComisiones.EntradaCalcularComisiones;
import cl.bcs.fondomutuo.CalcularComisiones.ResultadoCalcularComision;
import cl.bcs.fondomutuo.CalcularComisiones.SalidaCalcularComisiones;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.EntradaCertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.Estado;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.InSolicitudes;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.SolicitudesProcesoCertificacion;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InFormaPagoSpot;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo.EntradaProcesarCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo.SalidaMovimientoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ProcesarCustodiaFondoMutuo.SalidaProcesarCustodiaFondoMutuo;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.MovimientoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.pojo.PreferenciaPago;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.utils.BuscarCantidadesPorRegimen.ResultadoBuscarCantidadesPorRegimen;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimulaCalculoSpreadRescate implements JavaCall {
	

	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_SelectSolicitudes.sql");
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	static final Logger logger = LogManager.getLogger(SimulaCalculoSpreadRescate.class);


	public static class EntradaSimulaCalculoSpreadRescate{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public Long p_NumeroDividendo;
		public List<InSolicitudesAporteRescate> p_SolicitudesAporteRescate;
		public List<InFormaPagoSpot> p_FormaPagoSpot;
		public String p_IdOrigenProceso;  // REC: Recaudacion    PAC: Pago automatico  BONIF: Proceso Gestion Bonificacion 
		
		public EntradaSimulaCalculoSpreadRescate() {
			p_IdTipoCanalIngreso = "OPT";
			p_SolicitudesAporteRescate = new ArrayList<InSolicitudesAporteRescate>();
			p_FormaPagoSpot = new ArrayList<InFormaPagoSpot>();
			p_NumeroDividendo = 0L;
			p_IdOrigenProceso = "OPT";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudesRescate{
    	public Long IdInterno;
    	public Long IdCliente;
    	public Long IdPortafolio;
    	public String TipoSolicitud;
    	public String IdSerieFondo;
    	public String CalculoPorMontoCuota;
    	public String EsPorMontoLiquido;
    	public BigDecimal Monto;
    	public BigDecimal NumeroCuotas;
    	public String IdMoneda;
    	public String IdMedioDePago;
    	public Long IdPreferenciaPagoPortafolio;
    	public String Visado;
    	public String SolicitudBatch;
    	public String OrigenOperacion;
    	public String FechaOperacion;
    	public String IdRegimenTributario;
    	public String Glosa;
    	public String IndicadorMLT;
    	public String IdSerieFondoDestino;
    	public String IdEmisorDestino;
    	public Long IdAutorizadoOrigen;
    	public BigDecimal Porcentaje;
    	public Long FolioContrato;
    	public String IdMonedaPago;
    	public BigDecimal MontoPatrimonio;
    	public String IdMotivoRetiro;
    	public String IngresoAutomatico;
    	public String ForzarSpot; //Parametro para forzar spot independiente si tiene saldo en dolar, para distribucion y CSC
    	public String TipoContratoOperacion;
    	public String FolioContratoOperacion;
    	public String EsTraspaso;
    	public String Canal;
    	public Long IdSolicitudAporteRescateRelacionado;//Para solicitudes BF
    	public InSolicitudesRescate(){
    		this.IdMonedaPago = "";
    		this.IdEmisorDestino = "";
    		this.MontoPatrimonio = BigDecimal.ZERO;
    		this.IdMotivoRetiro="";
    		this.IngresoAutomatico = "N";
    		this.ForzarSpot = "N";
    		this.TipoContratoOperacion = "";
    		this.FolioContratoOperacion = "";
    		this.EsTraspaso = "N";
    		this.Canal = "";
    		this.IdSolicitudAporteRescateRelacionado = 0L;
    	}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InFormaPagoSpot{
		
    	public Long IdInternoPagoOperacion;
    	public Long IdComprobantePagoOperacion;
    	public String IdTipoMovimiento;
    	public String IdFormaDePago;
    	public String IdMoneda;
    	public String IdMonedaLiquidacion;
    	public Long IdOperacion;
    	public String RecibePagaPortafolio;
    	public BigDecimal MontoFormaDePago;
    	public String AfectarTesoreria;
    
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaSimulaCalculoSpreadRescate{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List<SolicitudesProcesoCertificacion> P_OUT_RESULTADO;
		
		public SalidaSimulaCalculoSpreadRescate(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<SolicitudesProcesoCertificacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio{
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaSimulaCalculoSpreadRescate jin = gson.fromJson(in.toString(), EntradaSimulaCalculoSpreadRescate.class);
		SalidaSimulaCalculoSpreadRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaSimulaCalculoSpreadRescate logica(Connection con, EntradaSimulaCalculoSpreadRescate entrada) throws Exception{
		
		logger.info("INICIO::SimulaCalculoSpreadRescate");
		SalidaSimulaCalculoSpreadRescate out = new SalidaSimulaCalculoSpreadRescate();
		SQL sql=new SQL();
		
		
		EntradaCrearSolicitudAporteRescateNormal entradaCrearSolicitudAporteRescateNormal = new EntradaCrearSolicitudAporteRescateNormal();
		entradaCrearSolicitudAporteRescateNormal.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearSolicitudAporteRescateNormal.p_IdModulo = entrada.p_IdModulo;
		entradaCrearSolicitudAporteRescateNormal.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearSolicitudAporteRescateNormal.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaCrearSolicitudAporteRescateNormal.p_SimularOperacion = _c.NO;
		InSolicitudesAporteRescate inSolicitudesAporteRescate = new InSolicitudesAporteRescate();
		
		for(InSolicitudesAporteRescate resultado : entrada.p_SolicitudesAporteRescate) {
			inSolicitudesAporteRescate.IdInterno = resultado.IdInterno;
			inSolicitudesAporteRescate.IdCliente = resultado.IdCliente;
			inSolicitudesAporteRescate.IdPortafolio = resultado.IdPortafolio;
			inSolicitudesAporteRescate.TipoSolicitud = resultado.TipoSolicitud;
			inSolicitudesAporteRescate.IdSerieFondo = resultado.IdSerieFondo;
			inSolicitudesAporteRescate.CalculoPorMontoCuota = resultado.CalculoPorMontoCuota;
			inSolicitudesAporteRescate.EsPorMontoLiquido = resultado.EsPorMontoLiquido;
			inSolicitudesAporteRescate.Monto = resultado.Monto;
			inSolicitudesAporteRescate.NumeroCuotas = resultado.NumeroCuotas;
			inSolicitudesAporteRescate.IdMoneda = resultado.IdMoneda;
			inSolicitudesAporteRescate.IdMedioDePago = resultado.IdMedioDePago;
			inSolicitudesAporteRescate.IdPreferenciaPagoPortafolio = resultado.IdPreferenciaPagoPortafolio;
			inSolicitudesAporteRescate.Visado = resultado.Visado;
			inSolicitudesAporteRescate.SolicitudBatch = resultado.SolicitudBatch;
			inSolicitudesAporteRescate.OrigenOperacion = resultado.OrigenOperacion;
			inSolicitudesAporteRescate.FechaOperacion = resultado.FechaOperacion;
			inSolicitudesAporteRescate.IdRegimenTributario = resultado.IdRegimenTributario;
			inSolicitudesAporteRescate.Glosa = resultado.Glosa;
			inSolicitudesAporteRescate.IndicadorMLT = resultado.IndicadorMLT;
			inSolicitudesAporteRescate.IdSerieFondoDestino = resultado.IdSerieFondoDestino;
			inSolicitudesAporteRescate.IdEmisorDestino = resultado.IdEmisorDestino;
			inSolicitudesAporteRescate.IdAutorizadoOrigen =resultado.IdAutorizadoOrigen;
			inSolicitudesAporteRescate.Porcentaje = resultado.Porcentaje;
			inSolicitudesAporteRescate.FolioContrato = resultado.FolioContrato;
			inSolicitudesAporteRescate.IdMonedaPago = resultado.IdMonedaPago;
			entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(inSolicitudesAporteRescate);
		}
		
		CrearSolicitudAporteRescateNormal crearSolicitudAporteRescateNormal = new CrearSolicitudAporteRescateNormal();
		
		SalidaCrearSolicitudAporteRescateNormal salidaCrearSolicitudAporteRescateNormal = crearSolicitudAporteRescateNormal.logica(con, entradaCrearSolicitudAporteRescateNormal);
		
		
		
		if (MensajeSalida.getNumeroErrores(salidaCrearSolicitudAporteRescateNormal.P_OUT_MENSAJES) > 0) {
			out.P_OUT_ERRORESCONTADOR = salidaCrearSolicitudAporteRescateNormal.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaCrearSolicitudAporteRescateNormal.P_OUT_MENSAJES);
			return out;
		}
		
		List<SolicitudesProcesoCertificacion> solicitudes = obtenerSolicitudes(con,entrada.p_IdEmpresa,salidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO.get(0).IdSolicitudAporteRescate);
		
		//ACTUALIZAMOS MONTOS
		HashMap<Long, Object> hcalculo = obtenerCalculoMontoCuotas(con, entrada.p_IdEmpresa, solicitudes);
		actualizarMontosSolicitudes(solicitudes, hcalculo);
				
		String rescateGeneraVentaSpotDolar = obtenerRescateGeneraMovimientoSpot(con, entrada.p_IdEmpresa);
		String calcularComisionCertificacion = obtenerCalculaComision(con, entrada.p_IdEmpresa);
		Long porcentajeRetencion = obtenerPorcentajeRetencion(con, entrada.p_IdEmpresa);
		
		
		List<SolicitudesProcesoCertificacion> rescates = obtenerSolicitudesPorTipoOperacionOrdenadasAscendiente(solicitudes, _c.TipoSolicitudRescate);
		
		if(rescates.size() > 0){
			//RESCATE
			HashMap<Long,Object> hcalculaCustodiaRescate = calculaCustodiaRescate(con,entrada.p_IdEmpresa,rescates);
			cambiarMontosRescatesPorCantidadCustodia(con, entrada.p_IdEmpresa, rescates, hcalculaCustodiaRescate, rescateGeneraVentaSpotDolar);
			
			marcarRescatesConRetencion(rescates, porcentajeRetencion);
			
			//CALCULO COMISIONES SI EL PARAMETRO ESTA ENCENDIDO
			if(_c.SI.equals(calcularComisionCertificacion)){
				HashMap<Long,Object> hcomisiones = calcularComisiones(con, rescates, entrada);
				cambiarMontosPorComisiones(rescates, hcomisiones);
			}
			formatearMontos(con,rescates);
			esMontoRetencionValido(rescates);
		}
		
		if(rescates.size() > 0){
			//CUSTODIA
			List<SolicitudesProcesoCertificacion> rescatesConRegimen = obtenerRescatesConRegimen(rescates);
			List<SolicitudesProcesoCertificacion> rescatesSinRegimen = obtenerRescatesSinRegimen(rescates);
			SalidaProcesarCustodiaFondoMutuo salidaCustodiaRescateConRegimen = ingresoRetiroCustodia(con, rescatesConRegimen, entrada);
			out.P_OUT_ERRORESCONTADOR += salidaCustodiaRescateConRegimen.P_OUT_ERRORESCONTADOR;
			
			SalidaProcesarCustodiaFondoMutuo salidaCustodiaRescateSinRegimen = ingresoRetiroCustodia(con, rescatesSinRegimen, entrada);
			out.P_OUT_ERRORESCONTADOR += salidaCustodiaRescateSinRegimen.P_OUT_ERRORESCONTADOR;
			
			List<MensajeSalida> mensajeSalidaCustodia = obtenerMensajeSalidaRescateCustodia(salidaCustodiaRescateConRegimen, salidaCustodiaRescateSinRegimen);
			
			for(MensajeSalida mje : mensajeSalidaCustodia){
				if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
					Long id = Math.abs(mje.id);
					out.P_OUT_MENSAJES.add(new MensajeSalida(id, mje.tipo, mje.codigo, mje.descripcion));
					for(SolicitudesProcesoCertificacion sol : rescates){
						if(id.longValue() == sol.IdSolicitudAporteRescate.longValue()){
							sol.EstadoProceso = _c.EstadoRechazado;
							sol.Codigo = mje.codigo;
							sol.DescripcionCodigo = mje.descripcion;
							break;
						}
					}
				}
			}
			
			List<SalidaMovimientoCustodiaFondoMutuo> carterasRetiradas = obtenerCarterasRetiradas(salidaCustodiaRescateConRegimen, salidaCustodiaRescateSinRegimen);
			
			validarRetiroCarteras(rescates,carterasRetiradas);
			calcularCostoUFRentabilidadRescates(con,rescates,carterasRetiradas,entrada.p_IdEmpresa);
			out.P_OUT_RESULTADO.addAll(rescates);	
	
		}
		
		
		if(_c.SI.equals(entrada.p_SimularOperacion)){
			out.P_OUT_ERRORESCONTADOR=1L;
		}
		
		logger.info("FIN::SimulaCalculoSpreadRescate");
		
		return out;
	
	}
	
	private List<SolicitudesProcesoCertificacion> obtenerSolicitudes(Connection con,Long idEmpresa, Long idSolicitudes) throws Exception{
		logger.trace("INICIO::SimulaCalculoSpreadRescate.obtenerSolicitudes");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", idEmpresa);
		params.put("IdSolicitudAporteRescate", idSolicitudes);
		List<SolicitudesProcesoCertificacion> solicitudes = sql.query(con, SQL_SELECT_SOLICITUDES, params, SolicitudesProcesoCertificacion.class);
		logger.trace("FIN::SimulaCalculoSpreadRescate.obtenerSolicitudes");
		return solicitudes;
	}
	

	private String obtenerRescateGeneraMovimientoSpot(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.obtenerRescateGeneraMovimientoSpot");
		String rescateGeneraMovimientoSpot = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "RESCATE_GENERA_VENTA_SPOT_DOLAR");
		logger.trace("FIN::SimulaCalculoSpreadRescate.obtenerRescateGeneraMovimientoSpot");
		return rescateGeneraMovimientoSpot;
	}
	

	private String obtenerCalculaComision(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.obtenerCalculaComision");
		String calculaComision = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "CALCULAR_COMISION_CERTIFICACION");
		logger.trace("FIN::SimulaCalculoSpreadRescate.obtenerCalculaComision");
		return calculaComision;
	}
	
	private Long obtenerPorcentajeRetencion(Connection con, Long idEmpresa) {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.obtenerPorcentajeRetencion");
		Long porcentajeRetencion = 0L;
		try {
			porcentajeRetencion= ConfiguracionSistema.getParametroConfiguracionLong(con,idEmpresa, "PLATAFORMA","APV.PorcentajeRetencion");
		}catch(Exception e) {
			logger.error("ERROR::SimulaCalculoSpreadRescate.obtenerPorcentajeRetencion:: Al encontrar parametro APV.PorcentajeRetencion");
		}
		logger.trace("FIN::SimulaCalculoSpreadRescate.obtenerPorcentajeRetencion");
		return porcentajeRetencion;
	}
	
	private List<SolicitudesProcesoCertificacion> obtenerSolicitudesPorTipoOperacionOrdenadasAscendiente(List<SolicitudesProcesoCertificacion> solicitudes, String tipoSolicitud){
		logger.trace("INICIO::SimulaCalculoSpreadRescate.obtenerSolicitudesPorTipoOperacionOrdenadasAscendiente");
		List<SolicitudesProcesoCertificacion> solicitudesTipoOperacionDesordenada = filtrarSolicitudesPendientesYAdvertencias(solicitudes).filter(solicitud->tipoSolicitud.equals(solicitud.TipoSolicitud)).collect(Collectors.toList());
		Comparator<SolicitudesProcesoCertificacion> comparadorAscendiente = obtenerComparadorSolicitudesAscendiente();
		List<SolicitudesProcesoCertificacion> solicitudesTipoOperacionOrdenada = solicitudesTipoOperacionDesordenada.parallelStream().sorted(comparadorAscendiente).collect(Collectors.toList());
		logger.trace("FIN::SimulaCalculoSpreadRescate.obtenerSolicitudesPorTipoOperacionOrdenadasAscendiente");
		return solicitudesTipoOperacionOrdenada;
	}
	
	private Stream<SolicitudesProcesoCertificacion> filtrarSolicitudesPendientesYAdvertencias(List<SolicitudesProcesoCertificacion> solicitudes){
		logger.trace("INICIO::SimulaCalculoSpreadRescate.filtarSolicitudes");
		Stream<SolicitudesProcesoCertificacion> solicitudesFiltradas = solicitudes.parallelStream().filter(solicitud->_c.EstadoPendiente.equals(solicitud.EstadoProceso) || _c.EstadoAdvertencia.equals(solicitud.EstadoProceso));
		logger.trace("FIN::SimulaCalculoSpreadRescate.filtarSolicitudes");
		return solicitudesFiltradas;
	}
	
	private Comparator<SolicitudesProcesoCertificacion> obtenerComparadorSolicitudesAscendiente() {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.obtenerComparadorSolicitudesAscendiente");
		Comparator<SolicitudesProcesoCertificacion> comparadorSolicitudesAscendiente = new Comparator<SolicitudesProcesoCertificacion>() {
		    @Override
		    public int compare(SolicitudesProcesoCertificacion primeraSolicitud, SolicitudesProcesoCertificacion segundaSolicitud) {
		        if(primeraSolicitud.IdSolicitudAporteRescate.compareTo(segundaSolicitud.IdSolicitudAporteRescate) == 0){
		            return 0;
		        }
		        return primeraSolicitud.IdSolicitudAporteRescate.compareTo(segundaSolicitud.IdSolicitudAporteRescate) == 1 ? 1 : -1;
		     }
		};
		logger.trace("FIN::SimulaCalculoSpreadRescate.obtenerComparadorSolicitudesAscendiente");
		return comparadorSolicitudesAscendiente;
	}
	
	private HashMap<Long,Object> calculaCustodiaRescate(Connection con, Long idEmpresa, List<SolicitudesProcesoCertificacion> solicitudes) throws Exception {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.calculaCustodiaRescate");
		CalculaCustodiaRescate calculaCustodiaRescate = new CalculaCustodiaRescate();
		EntradaCalculaCustodiaRescate entradacalculaCustodiaRescate = new EntradaCalculaCustodiaRescate();
		entradacalculaCustodiaRescate.p_IdEmpresa = idEmpresa;
		for(SolicitudesProcesoCertificacion sol : solicitudes){
			CalculaCustodiaRescate.InSolicitud solCus = new CalculaCustodiaRescate.InSolicitud();
			//solCus = Utils.copyPojo(sol, solCus);
			solCus = getPojoSolCus(sol,solCus);
			entradacalculaCustodiaRescate.p_Solicitudes.add(solCus);
		}
		
		SalidaCalculaCustodiaRescate salidaCalculaCustodiaRescate = calculaCustodiaRescate.logica(con, entradacalculaCustodiaRescate);
		HashMap<Long,Object> hcalculaCustodiaRescate = Utils.List2Hash(salidaCalculaCustodiaRescate.P_OUT_RESULTADO, "IdSolicitudAporteRescate");
		logger.trace("FIN::SimulaCalculoSpreadRescate.calculaCustodiaRescate");
		return hcalculaCustodiaRescate;
	}
	
	private void marcarRescatesConRetencion(List<SolicitudesProcesoCertificacion> rescates, Long porcentajeRetencion) {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.marcarRescatesConRetencion");
		rescates.parallelStream().forEach(sol->{
			if(_c.RescateFondoMutuo.equals(sol.IdTipoMovimientoFondoMutuo) 
					&&(_c.APV_AN.equals(sol.IdRegimenTributarioAPV) || _c.APV_B.equals(sol.IdRegimenTributarioAPV)) 
					&& !_c.CuentaInversion.equals(sol.IdMedioDePago) 
					&& !_c.SI.equals(sol.RelacionTraspaso) 
					&& !_c.SI.equals(sol.GeneraSpot) 
					&& porcentajeRetencion.compareTo(0L) != 0) {
				sol.TieneMontoRetencion = _c.SI;
				sol.MontoRetencion = sol.Monto.multiply(BigDecimal.valueOf(porcentajeRetencion)).divide(BigDecimal.valueOf(100));
				sol.MontoRetencionLocal = sol.CostoMonedaLocal.multiply(BigDecimal.valueOf(porcentajeRetencion)).divide(BigDecimal.valueOf(100));
			}
		});
		logger.trace("FIN::SimulaCalculoSpreadRescate.marcarRescatesConRetencion");
	}
	
	private void cambiarMontosRescatesPorCantidadCustodia(Connection con, Long idEmpresa, List<SolicitudesProcesoCertificacion> rescates, HashMap<Long,Object> hcalculaCustodiaRescate, String rescateGeneraVentaSpotDolar) {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.cambiarMontosRescatesPorCantidadCustodia");
		rescates.parallelStream().forEach(sol->{
			ResultadoCalculaCustodiaRescate custodia = (ResultadoCalculaCustodiaRescate) hcalculaCustodiaRescate.get(sol.IdSolicitudAporteRescate);
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
			parametrosMensaje.put("DocumentoIdentidad", sol.DocumentoIdentidad);
			parametrosMensaje.put("CodigoPortafolio", sol.CodigoPortafolio);
			parametrosMensaje.put("Nemotecnico", sol.IdSerieFondo);
			if((custodia == null || _c.EstadoRechazado.equals(custodia.Estado)) || (_c.EstadoAdvertencia.equals(custodia.Estado))){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION";
				parametrosMensaje.put("CantidadRetiro", sol.NumeroCuotas.toPlainString());
				parametrosMensaje.put("CantidadCustodia",custodia.CantidadDisponible.toPlainString());
				sol.DescripcionCodigo = obtenerDescripcionMensajeSalida(con, idEmpresa, sol.Codigo, parametrosMensaje);
			}else {
				if(_c.EstadoAdvertencia.equals(custodia.Estado)){
					sol.EstadoProceso = _c.EstadoAdvertencia;
					sol.Codigo = "UI_ERROR_FM_SALDO_CUSTODIA_INSUFICIENTE_CERTIFICACION";
					parametrosMensaje.put("CantidadRetiroInicial", sol.NumeroCuotas.toPlainString());
					parametrosMensaje.put("CantidadRetiroFinal", custodia.NumeroCuotas.toPlainString());
					parametrosMensaje.put("CantidadCustodia",custodia.CantidadDisponible.toPlainString());
					sol.DescripcionCodigo = obtenerDescripcionMensajeSalida(con, idEmpresa, sol.Codigo, parametrosMensaje);
				}
				if(_c.EstadoAdvertencia.equals(custodia.Estado) || (_c.CalculoPorTotal.equals(sol.CalculoPorMontoCuota))){
					sol.CostoMonedaLocal = custodia.MontoDestino;
					sol.Monto = custodia.Monto;
					sol.ValorCuota = custodia.ValorCuota;
					sol.NumeroCuotas = custodia.NumeroCuotas;
					sol.ParidadFechaCertificacion = custodia.ValorParidad;
					sol.CostoUF = custodia.CostoEnFactorAjuste;
					sol.ValorUFFechaInversion = custodia.ValorFactorAjuste;
					sol.MontoMonedaSistema = custodia.MontoMonedaSistema;
					sol.MontoPago = custodia.Monto;
					sol.MontoPagoLocal = custodia.MontoDestino;
				}
				
				if(_c.SI.equalsIgnoreCase(rescateGeneraVentaSpotDolar) && _c.Dolar.equalsIgnoreCase(sol.IdMoneda) && _c.Pesos.equalsIgnoreCase(sol.IdMonedaPago) ){
					sol.GeneraSpot=_c.SI;
				}else{
					sol.GeneraSpot=_c.NO;
				}
			}
		});
		logger.trace("FIN::SimulaCalculoSpreadRescate.cambiarMontosRescatesPorCantidadCustodia");
	}
	
	
	private String obtenerDescripcionMensajeSalida(Connection con, Long idEmpresa, String codigo, HashMap<String,String> parametrosMensaje) {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.obtenerDescripcionMensajeSalida");
		String descripcion = "";
		try {
			descripcion = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,parametrosMensaje,con,idEmpresa).descripcion;
		}catch(Exception e) {
			logger.trace("ADVERTENCIA::SimulaCalculoSpreadRescate.obtenerDescripcionMensajeSalida:: Mensaje no encontrado para codigo: " + descripcion);
		}
		logger.trace("FIN::SimulaCalculoSpreadRescate.obtenerDescripcionMensajeSalida");
		return descripcion;
	}
	
	private CalculaCustodiaRescate.InSolicitud getPojoSolCus(SolicitudesProcesoCertificacion sol, CalculaCustodiaRescate.InSolicitud solCus){
		logger.trace("INICIO::CertificarSolicitudAporteRescate.getPojoSolCus");
		solCus.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
		solCus.IdSerieFondo = sol.IdSerieFondo;
		solCus.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
		solCus.IdMoneda = sol.IdMoneda;
		solCus.IdMonedaFondo = sol.IdMonedaFondo;
		solCus.Monto = sol.Monto;
		solCus.NumeroCuotas = sol.NumeroCuotas;
		solCus.ValorCuota = sol.ValorCuota;
		solCus.TipoSolicitud = sol.TipoSolicitud;
		solCus.IdInstrumento = sol.IdInstrumento;
		solCus.IdCliente = sol.IdCliente;
		solCus.IdPortafolio = sol.IdPortafolio;
		solCus.FechaValorCuota = sol.FechaValorCuota;
		solCus.IdRegimenTributario = sol.IdRegimenTributario;
		solCus.IdOmnibus = sol.IdOmnibus;
		logger.trace("FIN::SimulaCalculoSpreadRescate.getPojoSolCus");
		return solCus;
	}
	
	
	private void cambiarMontosPorComisiones(List<SolicitudesProcesoCertificacion> rescates, HashMap<Long,Object> hcomisiones) {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.cambiarMontosPorComisiones");
		rescates.parallelStream().forEach(solicitud->{
			ResultadoCalcularComision comision = (ResultadoCalcularComision) hcomisiones.get(solicitud.IdSolicitudAporteRescate);
			if(comision != null){
				solicitud.MontoComision = comision.Comision;
				solicitud.MontoComisionLocal = comision.ComisionLocal;
				solicitud.MontoPagoLocal = solicitud.CostoMonedaLocal.subtract(solicitud.MontoComisionLocal, MathContext.DECIMAL128);
				solicitud.MontoPago = solicitud.Monto.subtract(solicitud.MontoComision, MathContext.DECIMAL128);
			}
		});
		logger.trace("FIN::SimulaCalculoSpreadRescate.cambiarMontosPorComisiones");
	}
	
	private HashMap<Long, Object> calcularComisiones(Connection con, List<SolicitudesProcesoCertificacion> solicitudes, EntradaSimulaCalculoSpreadRescate entrada) throws Exception{
		logger.trace("INICIO::SimulaCalculoSpreadRescate.calcularComisiones");
		HashMap<Long,Object> hcomisiones = new HashMap<Long,Object>();
		CalcularComisiones calculoComisiones = new CalcularComisiones();
		EntradaCalcularComisiones entradaCalcularComisiones = new EntradaCalcularComisiones();
		entradaCalcularComisiones.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCalcularComisiones.p_IdUsuario = entrada.p_IdUsuario;
		entradaCalcularComisiones.p_IdUsuario = entrada.p_IdUsuario;
		entradaCalcularComisiones.p_IdModulo = entrada.p_IdModulo;
		entradaCalcularComisiones.p_SimularOperacion = _c.NO;
		entradaCalcularComisiones.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaCalcularComisiones.p_DatosCustodia = solicitudes.parallelStream()
				.filter(solicitud->!_c.EstadoRechazado.equals(solicitud.EstadoProceso))
				.map(solicitud->{
					CalcularComisiones.InCustodia inCustodia =  new CalcularComisiones.InCustodia();
					inCustodia.IdInterno = solicitud.IdSolicitudAporteRescate;
					inCustodia.IdSerieFondo = solicitud.IdSerieFondo;
					inCustodia.IdInstrumento = solicitud.IdInstrumento;
					inCustodia.IdCliente = solicitud.IdCliente;
					inCustodia.IdPortafolio = solicitud.IdPortafolio;
					inCustodia.Cantidad = solicitud.NumeroCuotas;
					inCustodia.IdMonedaFondo = solicitud.IdMonedaFondo;
					inCustodia.IdMonedaOperacion = solicitud.IdMoneda;
					inCustodia.TipoSolicitud = solicitud.TipoSolicitud;
					inCustodia.IdTipoMovimientoFondoMutuo = solicitud.IdTipoMovimientoFondoMutuo;
					inCustodia.FechaValorCuota = Fecha.toStringDate(solicitud.FechaValorCuota);
					inCustodia.EsTraspaso = solicitud.RelacionTraspaso;
					return inCustodia;
				}).collect(Collectors.toList());
		
		if(entradaCalcularComisiones.p_DatosCustodia.size() > 0){
			SalidaCalcularComisiones salidaCalculoComisiones = calculoComisiones.logica(con, entradaCalcularComisiones);
			hcomisiones = Utils.List2Hash(salidaCalculoComisiones.P_OUT_RESULTADO, "IdInterno");
		}
		logger.trace("FIN::SimulaCalculoSpreadRescate.calcularComisiones");
		return hcomisiones;
	}
	
	
	private void formatearMontos(Connection con, List<SolicitudesProcesoCertificacion> solicitudes) throws Exception {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.formatearMontos");
		for(SolicitudesProcesoCertificacion solicitud : solicitudes) {
			solicitud.Monto = MonedaFormato.formatear(con, solicitud.Monto, MonedaFormato.MONTO, solicitud.IdMonedaFondo).valor;
		    solicitud.MontoComision = MonedaFormato.formatear(con, solicitud.MontoComision, MonedaFormato.MONTO, solicitud.IdMonedaFondo).valor;
		    solicitud.MontoPago = MonedaFormato.formatear(con, solicitud.MontoPago, MonedaFormato.MONTO, solicitud.IdMonedaFondo).valor;
		    solicitud.MontoLiquido = MonedaFormato.formatear(con, solicitud.MontoLiquido, MonedaFormato.MONTO, solicitud.IdMonedaFondo).valor;
		    solicitud.MontoRetencion = MonedaFormato.formatear(con, solicitud.MontoRetencion, MonedaFormato.MONTO, solicitud.IdMonedaFondo).valor;
		    
		    solicitud.CostoMonedaLocal = MonedaFormato.formatear(con, solicitud.CostoMonedaLocal, MonedaFormato.MONTO, solicitud.IdMoneda).valor;
		    solicitud.MontoComisionLocal = MonedaFormato.formatear(con, solicitud.MontoComisionLocal, MonedaFormato.MONTO, solicitud.IdMoneda).valor;
		    solicitud.MontoPagoLocal = MonedaFormato.formatear(con, solicitud.MontoPagoLocal, MonedaFormato.MONTO, solicitud.IdMoneda).valor;
		    solicitud.MontoLiquidoLocal = MonedaFormato.formatear(con, solicitud.MontoLiquidoLocal, MonedaFormato.MONTO, solicitud.IdMoneda).valor;
		    solicitud.MontoRetencionLocal = MonedaFormato.formatear(con, solicitud.MontoRetencionLocal, MonedaFormato.MONTO, solicitud.IdMoneda).valor;
		    
		    solicitud.MontoMonedaSistema = MonedaFormato.formatear(con, solicitud.MontoMonedaSistema, MonedaFormato.MONTO, solicitud.IdMonedaSistema).valor;;

		}
		logger.trace("FIN::SimulaCalculoSpreadRescate.formatearMontos");
	}
	
	
	private void esMontoRetencionValido(List<SolicitudesProcesoCertificacion> rescates) {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.esMontoRetencionValido");
		rescates.parallelStream().filter(rescate->_c.SI.equals(rescate.TieneMontoRetencion)).forEach(rescate->{
			if(rescate.MontoRetencion.compareTo(BigDecimal.ZERO) == 0) {
				rescate.TieneMontoRetencion = _c.NO;
			}
		});
		logger.trace("FIN::SimulaCalculoSpreadRescate.esMontoRetencionValido");
	}
	
	
	private List<SolicitudesProcesoCertificacion> obtenerRescatesConRegimen(List<SolicitudesProcesoCertificacion> rescates){
		logger.trace("INICIO::SimulaCalculoSpreadRescate.obtenerRescatesParciales");
		List<SolicitudesProcesoCertificacion> rescateParciales = rescates.parallelStream().filter(rescate->!_c.EstadoRechazado.equals(rescate.EstadoProceso) && !rescate.IdRegimenTributario.isEmpty()).collect(Collectors.toList());
		logger.trace("FIN::SimulaCalculoSpreadRescate.obtenerRescatesParciales");
		return rescateParciales;
	}
	
	private List<SolicitudesProcesoCertificacion> obtenerRescatesSinRegimen(List<SolicitudesProcesoCertificacion> rescates){
		logger.trace("INICIO::SimulaCalculoSpreadRescate.obtenerRescatesTotales");
		List<SolicitudesProcesoCertificacion> rescateTotales = rescates.parallelStream().filter(rescate->!_c.EstadoRechazado.equals(rescate.EstadoProceso) && rescate.IdRegimenTributario.isEmpty()).collect(Collectors.toList());
		logger.trace("FIN::SimulaCalculoSpreadRescate.obtenerRescatesTotales");
		return rescateTotales;
	}
	

	private SalidaProcesarCustodiaFondoMutuo ingresoRetiroCustodia(Connection con, List<SolicitudesProcesoCertificacion> solicitudes,  EntradaSimulaCalculoSpreadRescate entrada) throws Exception{
		logger.trace("INICIO::SimulaCalculoSpreadRescate.ingresoRetiroCustodia");
		ProcesarCustodiaFondoMutuo procesarCustodia = new ProcesarCustodiaFondoMutuo();
		EntradaProcesarCustodiaFondoMutuo entradaCustodia = new EntradaProcesarCustodiaFondoMutuo();
		SalidaProcesarCustodiaFondoMutuo salidaCustodia = new SalidaProcesarCustodiaFondoMutuo();
		for(SolicitudesProcesoCertificacion sol : solicitudes){
			if(_c.EstadoRechazado.equals(sol.EstadoProceso)) continue;
			MovimientoCustodiaFondoMutuo movCustodia = new MovimientoCustodiaFondoMutuo();
			movCustodia.IdGenerarMovimientoCustodia = sol.IdSolicitudAporteRescate;
			movCustodia.FechaOperacion = sol.FechaCertificacion;
			movCustodia.IdCliente = sol.IdCliente;
			movCustodia.IdPortafolio = sol.IdPortafolio;
			movCustodia.IdTipoProducto = _c.TipoOperacion;
			movCustodia.IdConcepto = sol.IdConcepto ;
			movCustodia.IdTipoOperacion = _c.TipoSolicitudAporte.equals(sol.TipoSolicitud) ? _c.TipoOperacionAporteFondoMutuo :  _c.TipoOperacionRescateFondoMutuo;
			movCustodia.IdOperacion = sol.IdSolicitudAporteRescate;
			movCustodia.IdTipoComprobante = _c.TipoComprobanteCustodia;
			movCustodia.FolioComprobante = sol.IdSolicitudAporteRescate;
			movCustodia.FechaComprobante = sol.FechaCertificacion;
			movCustodia.IdBolsa = _c.BCS;
			movCustodia.IdAgente = sol.IdEmpleadoEjecutivo;
			movCustodia.FechaLiquidacion = sol.FechaCertificacion;
			movCustodia.IdUsuario = entrada.p_IdUsuario;
			movCustodia.TipoCarteraNormativa = sol.TipoCarteraNormativa;
			movCustodia.FechaCurse = sol.FechaAceptacion;
			movCustodia.TipoFactorAjuste = _c.UF;
			movCustodia.ValorFactorAjusteaFechaCurse = sol.ValorUFFechaInversion;
			movCustodia.CostoEnFactorAjuste = sol.CostoUF;
			movCustodia.IdInstrumento = sol.IdInstrumento;
			movCustodia.IdEmisor = sol.IdEmisor;
			movCustodia.IdMonedaInstrumento = sol.IdMonedaFondo;
			movCustodia.IdBoveda = _c.COR;
			movCustodia.Cantidad = sol.NumeroCuotas;
			movCustodia.ValorRescate = BigDecimal.ZERO;
			movCustodia.PrecioCompra = sol.ValorCuota;
			movCustodia.MontoCompra = sol.MontoMonedaSistema;
			movCustodia.MontoCompraMonedaInstrumento = sol.Monto;
			movCustodia.IdTipoCanalIngreso = "";
			movCustodia.IdRegimenTributario = sol.IdRegimenTributario;
			movCustodia.FechaOrigenCompra = _c.AporteReinversion.equals(sol.IdTipoMovimientoFondoMutuo) && !"0001-01-01".equals(Fecha.stringToSqlDate(sol.FechaOriginalInversion)) ? sol.FechaOriginalInversion : sol.FechaValorCuota;
			entradaCustodia.p_Movimientos.add(movCustodia);
		}
		
		if(entradaCustodia.p_Movimientos.size() > 0){
			entradaCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCustodia.p_IdUsuario = entrada.p_IdUsuario;
			entradaCustodia.p_IdModulo = entrada.p_IdModulo;
			entradaCustodia.p_simularOperacion = _c.NO;
			entradaCustodia. p_UsaReglasNegocio = _c.SI;
			logger.debug("Se envia a custodia",entradaCustodia);
			salidaCustodia = procesarCustodia.logica(con, entradaCustodia);
		}
		logger.trace("FIN::SimulaCalculoSpreadRescate.ingresoRetiroCustodia");
		return salidaCustodia;
		
	}
	
	private List<MensajeSalida> obtenerMensajeSalidaRescateCustodia(SalidaProcesarCustodiaFondoMutuo salidaCustodiaRescateParciales, SalidaProcesarCustodiaFondoMutuo salidaCustodiaRescateTotales){
		logger.trace("INICIO::SimulaCalculoSpreadRescate.obtenerMensajeSalidaRescateCustodia");
		List<MensajeSalida> mensajeSalidaCustodia = new ArrayList<MensajeSalida>();
		mensajeSalidaCustodia.addAll(salidaCustodiaRescateParciales.P_OUT_MENSAJES);
		mensajeSalidaCustodia.addAll(salidaCustodiaRescateTotales.P_OUT_MENSAJES);
		logger.trace("FIN::SimulaCalculoSpreadRescate.obtenerMensajeSalidaRescateCustodia");
		return mensajeSalidaCustodia;
	}
	
	private void validarRetiroCarteras(List<SolicitudesProcesoCertificacion> rescates, List<SalidaMovimientoCustodiaFondoMutuo> carterasRetiradas) {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.validarRetiroCarteras");
		rescates.parallelStream().filter(rescate->!_c.EstadoRechazado.equals(rescate.EstadoProceso)).forEach(rescate->{
			int cantidadCarterasRetiradas = carterasRetiradas.parallelStream().filter(cartera->cartera.IdGenerarMovimientoCustodia.equals(rescate.IdSolicitudAporteRescate)).collect(Collectors.toList()).size();
			if(cantidadCarterasRetiradas == 0) {
				rescate.EstadoProceso = _c.EstadoRechazado;
				rescate.Codigo = "UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO";
			}
		});
		logger.trace("FIN::SimulaCalculoSpreadRescate.validarRetiroCarteras");
	}
	
	public List<SalidaMovimientoCustodiaFondoMutuo> obtenerCarterasRetiradas(SalidaProcesarCustodiaFondoMutuo salidaCustodiaRescateParciales, SalidaProcesarCustodiaFondoMutuo salidaCustodiaRescateTotales){
		logger.trace("INICIO::SimulaCalculoSpreadRescate.obtenerCarterasRetiradas");
		List<SalidaMovimientoCustodiaFondoMutuo> carterasRetiradas = new ArrayList<SalidaMovimientoCustodiaFondoMutuo>();
		carterasRetiradas.addAll(salidaCustodiaRescateParciales.P_OUT_MOVIMIENTOS);
		carterasRetiradas.addAll(salidaCustodiaRescateTotales.P_OUT_MOVIMIENTOS);
		logger.trace("INICIO::SimulaCalculoSpreadRescate.obtenerCarterasRetiradas");
		return carterasRetiradas;
	}
	
	private void calcularCostoUFRentabilidadRescates(Connection con, List<SolicitudesProcesoCertificacion> rescates, List<SalidaMovimientoCustodiaFondoMutuo> carterasRetiradas, Long idEmpresa) throws Exception {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.calcularCostoUFRentabilidadRescates");
		
		List<Date> fechaCertificaciones = rescates.parallelStream().map(solicitud->solicitud.FechaCertificacion).distinct().collect(Collectors.toList());
		Map<Long,List<SalidaMovimientoCustodiaFondoMutuo>> movimientosAgrupadosPorSolicitud = carterasRetiradas.parallelStream().collect(Collectors.groupingBy(solicitud->solicitud.IdGenerarMovimientoCustodia));
		HashMap<Date,Long> mapeoIdInternoFecha = new HashMap<Date,Long>();
		
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = idEmpresa;
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
		HashMap<Long,Object> hCalculo = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
		
		
		for(SolicitudesProcesoCertificacion sol : rescates) {
			BigDecimal costoUF = BigDecimal.ZERO;
			BigDecimal rentabilidad = BigDecimal.ZERO;
			
			List<SalidaMovimientoCustodiaFondoMutuo> movimientosCustodia = movimientosAgrupadosPorSolicitud.get(sol.IdSolicitudAporteRescate);
			if(movimientosCustodia != null) {
				for(SalidaMovimientoCustodiaFondoMutuo movimientoCustodia : movimientosCustodia) {
					costoUF = costoUF.add(movimientoCustodia.CostoInversionEnFactorAjusteARetirar, MathContext.DECIMAL128);
					rentabilidad = rentabilidad.add(movimientoCustodia.MontoCompraFactorARetirar, MathContext.DECIMAL128);
				}
			}
			
			Long id = mapeoIdInternoFecha.get(sol.FechaCertificacion);
			CalculoSalida calculoValorUTM = (CalculoSalida) hCalculo.get(id);
			BigDecimal valorUTM = calculoValorUTM !=null && "OK".equals(calculoValorUTM.Resultado) ? calculoValorUTM.MontoDestino : BigDecimal.ONE;
			sol.RentabilidadReal = ((sol.MontoMonedaSistema.divide(valorUTM, MathContext.DECIMAL128)).subtract(rentabilidad, MathContext.DECIMAL128));
			sol.Spread = sol.CostoUF.subtract(costoUF, MathContext.DECIMAL128);
			sol.CostoUF = costoUF;
		}
		logger.trace("FIN::SimulaCalculoSpreadRescate.calcularCostoUFRentabilidadRescates");
	}
	
	private HashMap<Long, Object> obtenerCalculoMontoCuotas(Connection con, Long idEmpresa, List<SolicitudesProcesoCertificacion> solicitudes) throws Exception{
		logger.trace("INICIO::SimulaCalculoSpreadRescate.obtenerCalculoMontoCuotas");
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = idEmpresa;
		
		entradaCalculo.p_Series = filtrarSolicitudesPendientesYAdvertencias(solicitudes).map(solicitud->{
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			serie.IdInterno = solicitud.IdSolicitudAporteRescate;
		
			serie.Fecha =  Fecha.stringToSqlDate(solicitud.FechaValorCuota);
			
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
		 	serie.Monto = solicitud.CostoMonedaLocal;
		 	serie.NumeroCuotas = solicitud.NumeroCuotas;
		 	serie.IdMonedaFondo = solicitud.IdMonedaFondo;
		 	serie.IdMonedaInversion = solicitud.IdMoneda;
		 	serie.TipoSolicitud =  solicitud.TipoSolicitud;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = idEmpresa;
		 	return serie;
		}).collect(Collectors.toList());
		
		
		SalidaCalcularMontoNumeroCuota salidaCalculo = new SalidaCalcularMontoNumeroCuota();
		if(entradaCalculo.p_Series.size() > 0){
			salidaCalculo = calculo.logica(con, entradaCalculo);
		}
		
		HashMap<Long, Object> hcalculo = Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno");
		logger.trace("FIN::SimulaCalculoSpreadRescate.obtenerCalculoMontoCuotas");
		return hcalculo;
	}
	
	private void actualizarMontosSolicitudes(List<SolicitudesProcesoCertificacion> solicitudes, HashMap<Long, Object> hcalculo) {
		logger.trace("INICIO::SimulaCalculoSpreadRescate.actualizarMontosSolicitudes");
		filtrarSolicitudesPendientesYAdvertencias(solicitudes).forEach(solicitud->{
			CalcularMontoNumeroCuota.Resultado r = (CalcularMontoNumeroCuota.Resultado) hcalculo.get(solicitud.IdSolicitudAporteRescate);
			if(r == null || !_c.OK.equals(r.Resultado)){
				solicitud.EstadoProceso = _c.EstadoRechazado;
				solicitud.Codigo = "ERROR_CALCULO_MONTO_CUOTA";
			}else {
				solicitud.CostoMonedaLocal = r.MontoDestino;
				solicitud.Monto = r.Monto;
				solicitud.ValorCuota = r.ValorCuota;
				solicitud.NumeroCuotas = r.NumeroCuotas;
				solicitud.ParidadFechaCertificacion = r.ValorParidad;
				solicitud.CostoUF = (_c.AporteReinversion.equals(solicitud.IdTipoMovimientoFondoMutuo)) ? solicitud.CostoUF : r.CostoEnFactorAjuste;
				solicitud.ValorUFFechaInversion = (_c.AporteReinversion.equals(solicitud.IdTipoMovimientoFondoMutuo)) ? solicitud.ValorUFFechaInversion : r.ValorFactorAjuste;
				solicitud.IdMonedaSistema = r.IdMonedaSistema;
				solicitud.MontoMonedaSistema = _c.TipoSolicitudAporte.equals(solicitud.TipoSolicitud) && solicitud.IdOperacionSpot != null && !solicitud.IdOperacionSpot.equals(0L) ? solicitud.MontoMonedaSistema : r.MontoMonedaSistema; //CUANDO ES APORTE Y TIENE SPOT SE DEBE RESPETAR EL MONTO DEL SPOT
				solicitud.MontoPago = r.Monto;
				solicitud.MontoPagoLocal = r.MontoDestino;
			}
		});
		logger.trace("FIN::SimulaCalculoSpreadRescate.actualizarMontosSolicitudes");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		
		String Json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"S\",\"p_UsaReglasNegocio\":\"S\",\"p_SolicitudesAporteRescate\":[{\"IdInterno\":1,\"IdCliente\":1424229,\"IdPortafolio\":1451828,\"TipoSolicitud\":\"RES\",\"IdSerieFondo\":\"FMLIQUIDEZ\",\"CalculoPorMontoCuota\":\"M\",\"EsPorMontoLiquido\":\"N\",\"Monto\":15000,\"NumeroCuotas\":21,\"IdMoneda\":\"CLP\",\"IdMedioDePago\":\"CIN\",\"IdPreferenciaPagoPortafolio\":0,\"Visado\":\"N\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"FechaOperacion\":\"2020-08-21\",\"IdRegimenTributario\":\"APV-A\",\"Glosa\":\"\",\"IndicadorMLT\":\"N\",\"IdSerieFondoDestino\":\"\",\"IdEmisorDestino\":\"\",\"IdAutorizadoOrigen\":0,\"Porcentaje\":0,\"FolioContrato\":0,\"IdMonedaPago\":\"\"}]}";		
		EntradaSimulaCalculoSpreadRescate Objin = gson.fromJson(Json2, EntradaSimulaCalculoSpreadRescate.class);
		SimulaCalculoSpreadRescate  demo=new SimulaCalculoSpreadRescate();				
		try {
			SalidaSimulaCalculoSpreadRescate ObjOut=demo.logica(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	}
		System.out.println("== END ==");
	}
}