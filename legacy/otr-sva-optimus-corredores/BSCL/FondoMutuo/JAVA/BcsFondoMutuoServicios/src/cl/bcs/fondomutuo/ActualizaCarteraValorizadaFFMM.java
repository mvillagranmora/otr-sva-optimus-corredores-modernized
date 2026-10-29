package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.fondomutuo.ActualizarCarterasFondosMutuos.Cartera;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizaCarteraValorizadaFFMM implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ActualizaCarteraValorizadaFFMM.class);
	
	private final String SQL_UPDATE_CARTERAS = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCarterasFondosMutuos_UpdateCarteras.sql");
	
	public static class EntradaActulizaCarteraValorizadaFFMM {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public List<Cartera> carteras;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaActulizaCarteraValorizadaFFMM {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_CARTERAS_VALORIZADAS;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaActulizaCarteraValorizadaFFMM() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_CARTERAS_VALORIZADAS=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaActulizaCarteraValorizadaFFMM jin = gson.fromJson(in.toString(), EntradaActulizaCarteraValorizadaFFMM.class);
		SalidaActulizaCarteraValorizadaFFMM out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaActulizaCarteraValorizadaFFMM logica(Connection con, EntradaActulizaCarteraValorizadaFFMM entrada) throws Exception {
		logger.info("Iniciar ActualizaCarteraValorizadaFFMM");
		SalidaActulizaCarteraValorizadaFFMM out = new SalidaActulizaCarteraValorizadaFFMM();
		String idMonedaSistema = obtenerMonedaSistema(con,entrada.p_IdEmpresa);
		String idMonedaAdicional = obtenerMonedaAdicional(con,entrada.p_IdEmpresa);
		
		ValorizaCarteraFFMM valorizar = new ValorizaCarteraFFMM();
		ValorizaCarteraFFMM.EntradaValorizaCarteraFFMM inValorizarFFMM = new ValorizaCarteraFFMM.EntradaValorizaCarteraFFMM();
		ValorizaCarteraFFMM.SalidaValorizaCarteraFFMM outValorizarFFMM = new ValorizaCarteraFFMM.SalidaValorizaCarteraFFMM();
		inValorizarFFMM.p_IdEmpresa=entrada.p_IdEmpresa;
		inValorizarFFMM.p_IdModulo=entrada.p_IdModulo;
		inValorizarFFMM.p_IdUsuario=entrada.p_IdUsuario;
		inValorizarFFMM.carteras=entrada.carteras;		
		outValorizarFFMM = valorizar.logica(con, inValorizarFFMM);
		
		List<Cartera> listaValorizada = outValorizarFFMM.P_OUT_CARTERAS_VALORIZADAS;
		listaValorizada = filtrarCarterasCorrectas(listaValorizada);
		guardarValorizacionCartera(con,listaValorizada,out,entrada.p_IdEmpresa,idMonedaSistema,idMonedaAdicional);		
		logger.info("Fin ActualizaCarteraValorizadaFFMM");
		return out;
	}
	private void guardarValorizacionCartera(Connection con, List<Cartera> carteras, SalidaActulizaCarteraValorizadaFFMM out, Long idEmpresa, String idMonedaSistema, String idMonedaAdicional) throws Exception {
		logger.trace("INICIO::ActualizaCarteraValorizadaFFMM.guardarValorizacionCartera");		
		try {
			CalcularParidadSalida salidaCalcularParidad = obtenerMontosMonedaSistemaYMontosMonedaAdicional(con,carteras,idEmpresa,idMonedaSistema,idMonedaAdicional);
			actualizarMontosCarteras(carteras, salidaCalcularParidad.p_out_CalculosSalida);
			SQL sql=new SQL();
			sql.batch(con, SQL_UPDATE_CARTERAS, carteras, Cartera.class);
			out.P_OUT_CARTERAS_VALORIZADAS += carteras.size();
		}catch(Exception e) {
			agregarError(out,e);
			logger.info("ERROR::ActualizaCarteraValorizadaFFMM.guardarValorizacionCartera",e);
			throw e;
		}
		logger.trace("FIN::ActualizaCarteraValorizadaFFMM.guardarValorizacionCartera");
	}
	
	private void actualizarMontosCarteras(List<Cartera> carteras, List<CalculoSalida> montosParidades) {
		logger.trace("INICIO::ActualizaCarteraValorizadaFFMM.actualizarMontosCarteras");
		Map<Long,CalculoSalida> mapMontosParidades = montosParidades.parallelStream().collect(Collectors.toMap(montoParidad->montoParidad.IdInterno, montoParidad->montoParidad));
		carteras.parallelStream().forEach(cartera->{
			{
				CalculoSalida montoParidadMonedaSistema = mapMontosParidades.get((cartera.IdCartera * 8) + 1L);
				CalculoSalida montoParidadMonedaAdicional = mapMontosParidades.get((cartera.IdCartera * 8) + 2L);
				CalculoSalida montoParidadMonedaInstrumento = mapMontosParidades.get((cartera.IdCartera * 8) + 3L);
				
				
				if(montoParidadMonedaSistema != null && montoParidadMonedaSistema.MontoDestino != null) {
					cartera.INGMontoATasaMercadoActual = montoParidadMonedaSistema.MontoDestino;
				}
				if(montoParidadMonedaAdicional != null && montoParidadMonedaAdicional.MontoDestino != null) {
					cartera.INGMontoMercadoActualMonedaAdicional = montoParidadMonedaAdicional.MontoDestino;
				}
				if(montoParidadMonedaInstrumento != null && montoParidadMonedaInstrumento.MontoDestino != null) {
					cartera.INGMontoATasaCompraActual = montoParidadMonedaInstrumento.MontoDestino;
				}
			}
			
			{
				CalculoSalida montoParidadMonedaSistema = mapMontosParidades.get((cartera.IdCartera * 8) + 4L);
				CalculoSalida montoParidadMonedaAdicional = mapMontosParidades.get((cartera.IdCartera * 8) + 5L);
				CalculoSalida montoVentaParidadMonedaSistema = mapMontosParidades.get((cartera.IdCartera * 8) + 6L);
				CalculoSalida montoVentaParidadMonedaAdicional = mapMontosParidades.get((cartera.IdCartera * 8) + 7L);
				
				if(montoParidadMonedaSistema != null && montoParidadMonedaSistema.MontoDestino != null) {
					cartera.EGRMontoATasaMercado = montoParidadMonedaSistema.MontoDestino;
				}
				if(montoParidadMonedaAdicional != null && montoParidadMonedaAdicional.MontoDestino != null) {
					cartera.EGRMontoMercadoMonedaAdicional = montoParidadMonedaAdicional.MontoDestino;
				}
				if(montoVentaParidadMonedaSistema != null && montoVentaParidadMonedaSistema.MontoDestino != null) {
					cartera.EGRMonto = montoVentaParidadMonedaSistema.MontoDestino;
				}
				if(montoVentaParidadMonedaAdicional != null && montoVentaParidadMonedaAdicional.MontoDestino != null) {
					cartera.EGRMontoMonedaAdicional = montoVentaParidadMonedaAdicional.MontoDestino;
				}
			}
			
			
		});
		logger.trace("FIN::ActualizaCarteraValorizadaFFMM.actualizarMontosCarteras");
	}
	
	private CalcularParidadSalida obtenerMontosMonedaSistemaYMontosMonedaAdicional(Connection con,List<Cartera> carteras, Long idEmpresa, String idMonedaSistema, String idMonedaAdicional) throws Exception {
		logger.trace("INICIO::ActualizaCarteraValorizadaFFMM.obtenerMontosMonedaSistemaYMontosMonedaAdicional");
	
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		CalcularParidadEntrada entradaCalcularParidad = new CalcularParidadEntrada();
		entradaCalcularParidad.p_IdEmpresa = 1L;
		entradaCalcularParidad.p_IdTrabajo = 0L;
		entradaCalcularParidad.p_CalculosEntrada = new ArrayList<CalculoEntrada>();
		entradaCalcularParidad.p_CalculosEntrada.addAll(obtenerCarterasIngresosFondosInternacionalesParaMonedaSistema(carteras, idMonedaSistema));
		entradaCalcularParidad.p_CalculosEntrada.addAll(obtenerCarterasIngresosFondosInternacionalesParaMonedaAdicional(carteras, idMonedaAdicional));
		entradaCalcularParidad.p_CalculosEntrada.addAll(obtenerCarterasIngresosFondosInternacionalesParaMonedaInstrumento(carteras, idMonedaSistema, fechaSistema));
		entradaCalcularParidad.p_CalculosEntrada.addAll(obtenerCarterasRetirosFondosInternacionalesParaMonedaSistema(carteras, idMonedaSistema));
		entradaCalcularParidad.p_CalculosEntrada.addAll(obtenerCarterasRetirosFondosInternacionalesParaMonedaAdicional(carteras, idMonedaAdicional));
		entradaCalcularParidad.p_CalculosEntrada.addAll(obtenerCarterasRetirosPrecioVentaFondosInternacionalesParaMonedaSistema(carteras, idMonedaSistema));
		entradaCalcularParidad.p_CalculosEntrada.addAll(obtenerCarterasRetirosPrecioVentaFondosInternacionalesParaMonedaAdicional(carteras, idMonedaAdicional));
		
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadSalida salidaCalcularParidad = calcularParidad.logica(con,entradaCalcularParidad);
		logger.trace("FIN::ActualizaCarteraValorizadaFFMM.obtenerMontosMonedaSistemaYMontosMonedaAdicional");
		return salidaCalcularParidad;
	}
	
	private List<CalculoEntrada> obtenerCarterasIngresosFondosInternacionalesParaMonedaSistema(List<Cartera> carteras, String idMonedaSistema) {
		logger.trace("INICIO::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaSistema");
		List<CalculoEntrada> carteraParidad = carteras.parallelStream().filter(cartera->!cartera.IdMoneda.equals(idMonedaSistema) && esCarteraIngreso(cartera)).map(cartera->{
			CalculoEntrada calculo = new CalculoEntrada();
			calculo.Fecha = cartera.INGFechaPrecioTasaMercadoActual;
			calculo.IdInterno = (cartera.IdCartera * 8) + 1L;
			calculo.IdMonedaOrigen = cartera.IdMoneda;
			calculo.IdMonedaDestino = idMonedaSistema;
			calculo.IdTrabajo = 0L;
			calculo.IdUso = "";
			calculo.MontoOrigen = cartera.INGMontoMercadoActualMonedaInstrumento;
			calculo.ValorCambioLocalModificado = BigDecimal.ZERO;
			return calculo;
		}).collect(Collectors.toList());
		logger.trace("FIN::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaSistema");
		return carteraParidad;
	}
	
	private List<CalculoEntrada> obtenerCarterasIngresosFondosInternacionalesParaMonedaAdicional(List<Cartera> carteras, String idMonedaAdicional) {
		logger.trace("INICIO::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaAdicional");
		List<CalculoEntrada> carteraParidad = carteras.parallelStream().filter(cartera->!cartera.IdMoneda.equals(idMonedaAdicional) && esCarteraIngreso(cartera)).map(cartera->{
			CalculoEntrada calculo = new CalculoEntrada();
			calculo.Fecha = cartera.INGFechaPrecioTasaMercadoActual;
			calculo.IdInterno = (cartera.IdCartera * 8) + 2L;
			calculo.IdMonedaOrigen = cartera.IdMoneda;
			calculo.IdMonedaDestino = idMonedaAdicional;
			calculo.IdTrabajo = 0L;
			calculo.IdUso = "";
			calculo.MontoOrigen = cartera.INGMontoMercadoActualMonedaInstrumento;
			calculo.ValorCambioLocalModificado = BigDecimal.ZERO;
			return calculo;
		}).collect(Collectors.toList());
		logger.trace("FIN::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaAdicional");
		return carteraParidad;
	}
	
	
	

	private List<CalculoEntrada> obtenerCarterasIngresosFondosInternacionalesParaMonedaInstrumento(List<Cartera> carteras, String idMonedaSistema,String fechaSistema) {
		logger.trace("INICIO::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaInstrumento");
		List<CalculoEntrada> carteraParidad = carteras.parallelStream().filter(cartera->!cartera.IdMoneda.equals(idMonedaSistema) && esCarteraIngreso(cartera)).map(cartera->{
			CalculoEntrada calculo = new CalculoEntrada();
			calculo.Fecha = fechaSistema; /////cartera.INGFechaPrecioTasaMercadoActual;
			calculo.IdInterno = (cartera.IdCartera * 8) + 3L;
			calculo.IdMonedaOrigen = cartera.IdMoneda;   // Corresponde a la moneda del fondo
			calculo.IdMonedaDestino = idMonedaSistema;
			calculo.IdTrabajo = 0L;
			calculo.IdUso = "";			
			calculo.MontoOrigen = cartera.INGPrecioCompra.multiply(cartera.INGCantidad);			
			calculo.ValorCambioLocalModificado = BigDecimal.ZERO;
			return calculo;
		}).collect(Collectors.toList());
		logger.trace("FIN::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaInstrumento");
		return carteraParidad;
	}

	private List<CalculoEntrada> obtenerCarterasRetirosFondosInternacionalesParaMonedaSistema(List<Cartera> carteras, String idMonedaSistema) {
		logger.trace("INICIO::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaSistema");
		List<CalculoEntrada> carteraParidad = carteras.parallelStream().filter(cartera->!cartera.IdMoneda.equals(idMonedaSistema) && esCarteraRetiro(cartera)).map(cartera->{
			CalculoEntrada calculo = new CalculoEntrada();
			calculo.Fecha = cartera.EGRFechaPrecioTasaMercado;
			calculo.IdInterno = (cartera.IdCartera * 8) + 4L;
			calculo.IdMonedaOrigen = cartera.IdMoneda;
			calculo.IdMonedaDestino = idMonedaSistema;
			calculo.IdTrabajo = 0L;
			calculo.IdUso = "";
			calculo.MontoOrigen = cartera.EGRMontoMercadoMonedaInstrumento;
			calculo.ValorCambioLocalModificado = BigDecimal.ZERO;
			return calculo;
		}).collect(Collectors.toList());
		logger.trace("FIN::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaSistema");
		return carteraParidad;
	}
	
	private List<CalculoEntrada> obtenerCarterasRetirosFondosInternacionalesParaMonedaAdicional(List<Cartera> carteras, String idMonedaAdicional) {
		logger.trace("INICIO::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaAdicional");
		List<CalculoEntrada> carteraParidad = carteras.parallelStream().filter(cartera->!cartera.IdMoneda.equals(idMonedaAdicional) && esCarteraRetiro(cartera)).map(cartera->{
			CalculoEntrada calculo = new CalculoEntrada();
			calculo.Fecha = cartera.EGRFechaPrecioTasaMercado;
			calculo.IdInterno = (cartera.IdCartera * 8) + 5L;
			calculo.IdMonedaOrigen = cartera.IdMoneda;
			calculo.IdMonedaDestino = idMonedaAdicional;
			calculo.IdTrabajo = 0L;
			calculo.IdUso = "";
			calculo.MontoOrigen = cartera.EGRMontoMercadoMonedaInstrumento;
			calculo.ValorCambioLocalModificado = BigDecimal.ZERO;
			return calculo;
		}).collect(Collectors.toList());
		logger.trace("FIN::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaAdicional");
		return carteraParidad;
	}
	
	private List<CalculoEntrada> obtenerCarterasRetirosPrecioVentaFondosInternacionalesParaMonedaSistema(List<Cartera> carteras, String idMonedaSistema) {
		logger.trace("INICIO::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaSistema");
		List<CalculoEntrada> carteraParidad = carteras.parallelStream().filter(cartera->!cartera.IdMoneda.equals(idMonedaSistema) && esCarteraRetiro(cartera)).map(cartera->{
			CalculoEntrada calculo = new CalculoEntrada();
			calculo.Fecha = cartera.EGRFechaPrecioTasaMercado;
			calculo.IdInterno = (cartera.IdCartera * 8) + 6L;
			calculo.IdMonedaOrigen = cartera.IdMoneda;
			calculo.IdMonedaDestino = idMonedaSistema;
			calculo.IdTrabajo = 0L;
			calculo.IdUso = "";
			calculo.MontoOrigen = cartera.EGRMontoMonedaInstrumento;
			calculo.ValorCambioLocalModificado = BigDecimal.ZERO;
			return calculo;
		}).collect(Collectors.toList());
		logger.trace("FIN::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaSistema");
		return carteraParidad;
	}
	
	private List<CalculoEntrada> obtenerCarterasRetirosPrecioVentaFondosInternacionalesParaMonedaAdicional(List<Cartera> carteras, String idMonedaAdicional) {
		logger.trace("INICIO::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaAdicional");
		List<CalculoEntrada> carteraParidad = carteras.parallelStream().filter(cartera->!cartera.IdMoneda.equals(idMonedaAdicional) && esCarteraRetiro(cartera)).map(cartera->{
			CalculoEntrada calculo = new CalculoEntrada();
			calculo.Fecha = cartera.EGRFechaPrecioTasaMercado;
			calculo.IdInterno = (cartera.IdCartera * 8) + 7L;
			calculo.IdMonedaOrigen = cartera.IdMoneda;
			calculo.IdMonedaDestino = idMonedaAdicional;
			calculo.IdTrabajo = 0L;
			calculo.IdUso = "";
			calculo.MontoOrigen = cartera.EGRMontoMonedaInstrumento;
			calculo.ValorCambioLocalModificado = BigDecimal.ZERO;
			return calculo;
		}).collect(Collectors.toList());
		logger.trace("FIN::ActualizaCarteraValorizadaFFMM.obtenerCarterasFondosInternacionalesParaMonedaAdicional");
		return carteraParidad;
	}
	
	private void agregarError(SalidaActulizaCarteraValorizadaFFMM out, Exception e) {
		logger.trace("INICIO::ActualizaCarteraValorizadaFFMM.agregarError");
		out.P_OUT_ERRORESCONTADOR++;
		out.P_OUT_MENSAJES.add((new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_VALORIZACION",e.toString())));
		logger.trace("FIN::ActualizaCarteraValorizadaFFMM.agregarError");
	}
	
	private List<Cartera> filtrarCarterasCorrectas(List<Cartera> carteras){
		logger.trace("INICIO::ActualizaCarteraValorizadaFFMM.filtrarCarterasCorrectas");
		carteras = carteras.parallelStream().filter(cartera->esPrecioValido(cartera.INGPrecioMercadoActual) || esPrecioValido(cartera.EGRPrecioMercado)).collect(Collectors.toList());
		logger.trace("FIN::ActualizaCarteraValorizadaFFMM.filtrarCarterasCorrectas");
		return carteras;
	}
	
	private String obtenerMonedaAdicional(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ActualizarCarterasFondosMutuos.obtenerMonedaAdicional");
		String idMonedaAdicional = ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA", "PLA.MonedaAdicional");
		logger.trace("FIN::ActualizarCarterasFondosMutuos.obtenerMonedaAdicional");
		return idMonedaAdicional;
	}
	
	private String obtenerMonedaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ActualizarCarterasFondosMutuos.obtenerMonedaSistema");
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA", "PLA.MonedaSistema");
		logger.trace("FIN::ActualizarCarterasFondosMutuos.obtenerMonedaSistema");
		return idMonedaSistema;
	}
	
	private Boolean esCarteraIngreso(Cartera cartera) {
		logger.trace("INICIO::ActualizarCarterasFondosMutuos.esCarteraIngreso");
		Boolean esCarteraIngreso = cartera.INGCantidad != null;
		logger.trace("FIN::ActualizarCarterasFondosMutuos.esCarteraIngreso");
		return esCarteraIngreso;
	}
	
	private Boolean esCarteraRetiro(Cartera cartera) {
		logger.trace("INICIO::ActualizarCarterasFondosMutuos.esCarteraRetiro");
		Boolean esCarteraRetiro = cartera.EGRCantidad != null;
		logger.trace("FIN::ActualizarCarterasFondosMutuos.esCarteraRetiro");
		return esCarteraRetiro;
	}
	
	private Boolean esPrecioValido(BigDecimal precio) {
		logger.trace("INICIO::ActualizarCarterasFondosMutuos.esPrecioValido");
		Boolean esPrecioValido = precio != null && precio.compareTo(BigDecimal.ZERO) == 1;
		logger.trace("FIN::ActualizarCarterasFondosMutuos.esPrecioValido");
		return esPrecioValido;
	}
}
