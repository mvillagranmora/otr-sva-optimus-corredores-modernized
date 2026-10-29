package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
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

import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NetearCustodiaNegativa implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(NetearCustodiaNegativa.class);
	private final String SQL_UPDATE_CARTERA_NEGATIVA_PROCESO = loadResourceToString("cl/bcs/custodia/sql/NetearCustodiaNegativa_UpdateCarteraNegativaProceso.sql");
	private final String SQL_SELECT_CARTERAS = loadResourceToString("cl/bcs/custodia/sql/NetearCustodiaNegativa_SelectCartera.sql");
	private final String SQL_SELECT_CARTERA_NEGATIVA = loadResourceToString("cl/bcs/custodia/sql/NetearCustodiaNegativa_SelectCarteraNegativa.sql");
	private final String SQL_UPDATE_CARTERA_NEGATIVA_LIQUIDACION = loadResourceToString("cl/bcs/custodia/sql/NetearCustodiaNegativa_UpdateCarteraNegativaLiquidacion.sql");
	private final String SQL_UPDATE_CARTERA_NEGATIVA_VIGENTE = loadResourceToString("cl/bcs/custodia/sql/NetearCustodiaNegativa_UpdateCarteraNegativaVigente.sql");
	
	public static class EntradaNetearCustodiaNegativa extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public List<PortafolioInstrumento> p_PortafoliosInstrumento;
		
		public EntradaNetearCustodiaNegativa() {
			this.p_PortafoliosInstrumento = new ArrayList<PortafolioInstrumento>();
		}
	}
	
	public static class PortafolioInstrumento extends BcsPojo {
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String NemoSVS;
		
		public PortafolioInstrumento() {
			this.NemoSVS = "";
		}
	}
	
	public static class SalidaNetearCustodiaNegativa extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaNetearCustodiaNegativa() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class Cartera extends BcsPojo {
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String Nemotecnico;
		public String NemoSVS;
		public String IdBoveda;
		public java.sql.Date FechaLiquidacion;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
	}
	
	public static class CarteraNegativa extends BcsPojo {
		public Long IdCarteraNegativa;
		public java.sql.Date FechaOperacion;
		public String IdTipoOperacion;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal CantidadRestante;
		public BigDecimal ValorRescate;
		public String IdConcepto;
		public Long IdCuentaCustodioPortafolio;
		public String IdCondicionLiquidacion;
		public java.sql.Date FechaLiquidacion;
		public String CodigoIFRS;
		public Long IdMesa;
		public String Nemotecnico;
		public String NemoSVS;
		public String IdEmisor;
		public java.sql.Date FechaVencimientoOperacion;
		public String EsFungible;
		public String IdTipoReajuste;
		public Long IdOperacionMesa;
		public String IdTipoComprobante;
		public Long FolioComprobante;
		public Long IdMovimientoComprobante;
		public BigDecimal PrecioOperacion;
		public BigDecimal MontoOperacion;
		public BigDecimal TasaOperacion;
		public String IdBoveda;
		public String IdTipoProducto;
		public Long IdCorredoraContraparte;
		public String FolioTransaccion;
		public java.sql.Date FechaTransaccion;
		public String IdBolsa;
		public Long IdOperacion;
		public Long IdAgente;
		public String IdMonedaInstrumento;
		public String IdMonedaEmision;
		public BigDecimal MontoCompraMonedaInstrumento;
		public String IdTipoCanalIngreso;
		public BigDecimal CorteMinimo;
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaNetearCustodiaNegativa jin = gson.fromJson(in.toString(), EntradaNetearCustodiaNegativa.class);
		SalidaNetearCustodiaNegativa out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaNetearCustodiaNegativa logica(Connection con, EntradaNetearCustodiaNegativa entrada) throws Exception {
		logger.info("INICIO::NetearCustodiaNegativa");
		
		SalidaNetearCustodiaNegativa out = new SalidaNetearCustodiaNegativa();
		
		actualizarCarteraProceso(con, entrada.p_PortafoliosInstrumento);
		List<CarteraNegativa> carterasNegativas = obtenerCarterasNegativa(con, entrada);
		if(carterasNegativas.isEmpty()) {
			actualizarCarteraNegativaVigente(con, entrada.p_PortafoliosInstrumento);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_NO_EXISTEN_CARTERAS_PARA_NETEAR", new HashMap<String, String>(1), con, entrada.p_IdEmpresa));
			return out;
		}
		
		IngresoCustodia.IngresoCustodiaSalida salidaCrearMovimientoTransito = crearMovimientoTransito(con, entrada, carterasNegativas);
		if(salidaCrearMovimientoTransito.P_OUT_ERRORESCONTADOR > 0) {
			actualizarCarteraNegativaVigente(con, entrada.p_PortafoliosInstrumento);
			out.P_OUT_ERRORESCONTADOR = salidaCrearMovimientoTransito.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES = salidaCrearMovimientoTransito.P_OUT_MENSAJES;
			return out;
		}
		
		out = certificarMovimientoTransito(con, entrada, salidaCrearMovimientoTransito.P_OUT_DATOSSALIDA);
		
		if(out.P_OUT_ERRORESCONTADOR == 0) {
			actualizarCarteraNegativaLiquidacion(con, carterasNegativas);
			HashMap<String, String> parametros = new HashMap<String, String>(2);
			parametros.put("NumeroCarteras", String.valueOf(carterasNegativas.size()));
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UI_SE_NETEARON_CARTERAS", parametros, con, entrada.p_IdEmpresa));
		}
		
		actualizarCarteraNegativaVigente(con, entrada.p_PortafoliosInstrumento);
		
		logger.info("FIN::NetearCustodiaNegativa");
		return out;
	}
	
	private void actualizarCarteraProceso(Connection con, List<PortafolioInstrumento> portafoliosInstrumento) throws Exception {
		logger.trace("INICIO::NetearCustodiaNegativa.actualizarCarteraProceso");
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_CARTERA_NEGATIVA_PROCESO, portafoliosInstrumento, PortafolioInstrumento.class);
		
		logger.trace("FIN::NetearCustodiaNegativa.actualizarCarteraProceso");
	}
	
	private List<CarteraNegativa> obtenerCarterasNegativa(Connection con, EntradaNetearCustodiaNegativa entrada) throws Exception {
		logger.trace("INICIO::NetearCustodiaNegativa.obtenerCarterasNegativa");
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		java.sql.Date fechaSistemaDate = Fecha.toDate(fechaSistema);
		List<Long> idPortafolios = entrada.p_PortafoliosInstrumento
				.stream()
				.map(portafolioInstrumento->portafolioInstrumento.IdPortafolio)
				.distinct()
				.collect(Collectors.toList());
		
		List<Long> idInstrumentos = entrada.p_PortafoliosInstrumento
				.stream()
				.map(portafolioInstrumento->portafolioInstrumento.IdInstrumento)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(3);
		parametros.put("IdPortafolio", idPortafolios);
		parametros.put("IdInstrumento", idInstrumentos);
		
		SQL sql = new SQL();
		List<CarteraNegativa> carterasNegativas = sql.query(con, SQL_SELECT_CARTERA_NEGATIVA, parametros, CarteraNegativa.class);
		
		List<CarteraNegativa> carterasNeteo = new ArrayList<CarteraNegativa>();
		if(carterasNegativas.size() > 0) {
			List<Cartera> carteras = sql.query(con, SQL_SELECT_CARTERAS, parametros, Cartera.class);
			
			Map<String, List<Cartera>> carterasPorPortafolioInstrumento = carteras
					.stream()
					.collect(Collectors.groupingBy(cartera->cartera.IdPortafolio + "_" + cartera.IdInstrumento));
			
			Map<String, List<CarteraNegativa>> carterasNegativasPorPortafolioInstrumento = carterasNegativas
					.stream()
					.collect(Collectors.groupingBy(carteraNegativa->carteraNegativa.IdPortafolio + "_" + carteraNegativa.IdInstrumento));
			
			List<PortafolioInstrumento> portafoliosInstrumentoProceso = new ArrayList<PortafolioInstrumento>();
			HashMap<String, Boolean> portafoliosInstrumentosAgregados = new HashMap<String, Boolean>(entrada.p_PortafoliosInstrumento.size() + 1);
			for(PortafolioInstrumento portafolioInstrumento : entrada.p_PortafoliosInstrumento) {
				String key = portafolioInstrumento.IdInstrumento + "_" + portafolioInstrumento.IdPortafolio + "_" + portafolioInstrumento.NemoSVS;
				if(portafoliosInstrumentosAgregados.get(key) == null) {
					portafoliosInstrumentoProceso.add(portafolioInstrumento);
					portafoliosInstrumentosAgregados.put(key, true);
				}
			}
			
			for(PortafolioInstrumento portafolioInstrumento : portafoliosInstrumentoProceso) {
				List<Cartera> carterasPortafolioInstrumento = carterasPorPortafolioInstrumento.get(portafolioInstrumento.IdPortafolio + "_" + portafolioInstrumento.IdInstrumento);
				List<CarteraNegativa> carterasNegativasPortafolioInstrumento = carterasNegativasPorPortafolioInstrumento.get(portafolioInstrumento.IdPortafolio + "_" + portafolioInstrumento.IdInstrumento);
				
				if(carterasPortafolioInstrumento != null && carterasNegativasPortafolioInstrumento != null) {
					for(CarteraNegativa carteraNegativa : carterasNegativasPortafolioInstrumento) {
						if(carteraNegativa.CantidadRestante.compareTo(BigDecimal.ZERO) != 1) continue;
						for(Cartera cartera : carteras) {
							if(esCarteraValida(cartera, carteraNegativa, fechaSistemaDate)) {
								BigDecimal cantidad = cartera.Cantidad;
								cartera.Cantidad = cartera.Cantidad.subtract(carteraNegativa.CantidadRestante, MathContext.DECIMAL128);
								carteraNegativa.CantidadRestante = carteraNegativa.CantidadRestante.subtract(cantidad, MathContext.DECIMAL128);
							}
						}
						if(carteraNegativa.Cantidad.compareTo(carteraNegativa.CantidadRestante) != 0) {
							carterasNeteo.add(carteraNegativa);
						}
					}
				}
			}
		}
		
		logger.trace("FIN::NetearCustodiaNegativa.obtenerCarterasNegativa");
		return carterasNeteo;
	}
	
	private Boolean esCarteraValida(Cartera cartera, CarteraNegativa carteraNegativa, java.sql.Date fechaSistema) {
		logger.trace("INICIO::NetearCustodiaNegativa.esCarteraValida");
		Boolean esCarteraValida = cartera.Cantidad.compareTo(BigDecimal.ZERO) == 1 && cartera.IdBoveda.equals(carteraNegativa.IdBoveda) && (cartera.FechaLiquidacion.compareTo(carteraNegativa.FechaLiquidacion) != 1 || cartera.FechaLiquidacion.compareTo(fechaSistema) != 1);
		logger.trace("FIN::NetearCustodiaNegativa.esCarteraValida");
		return esCarteraValida;
	}
	
	private IngresoCustodia.IngresoCustodiaSalida crearMovimientoTransito(Connection con, EntradaNetearCustodiaNegativa entrada, List<CarteraNegativa> carterasNegativas) throws Exception {
		logger.trace("INICIO::NetearCustodiaNegativa.crearMovimientoTransito");
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		IngresoCustodia.IngresoCustodiaEntrada entradaIngresoCustodia = new IngresoCustodia.IngresoCustodiaEntrada();
		entradaIngresoCustodia.p_IdEmpresa = entrada.p_IdEmpresa;		
		entradaIngresoCustodia.p_IdModulo = entrada.p_IdModulo;
		entradaIngresoCustodia.p_IdUsuario = entrada.p_IdUsuario;
		entradaIngresoCustodia.p_SimularOperacion = "N";
		entradaIngresoCustodia.p_TicketAtencion = 0L;
		entradaIngresoCustodia.p_ValorizacionAutomatica = "N";
		entradaIngresoCustodia.p_DatosEntrada = carterasNegativas
				.stream()
				.map(carteraNegativa->{
					IngresoCustodia.ObjDatosEntrada movimientoTransito = new IngresoCustodia.ObjDatosEntrada();
					movimientoTransito.IdGenerarMovimientoCustodia 	= carteraNegativa.IdCarteraNegativa;
					movimientoTransito.TablaAfectada = "TRANSITO";
					movimientoTransito.AccionOperacion	= "RETIRO";
					movimientoTransito.EsRegistroNuevo = "SI";
					movimientoTransito.FechaOperacion = Fecha.toDate(fechaSistema);
					movimientoTransito.IdCliente = carteraNegativa.IdCliente; 
					movimientoTransito.IdPortafolio = carteraNegativa.IdPortafolio;
					movimientoTransito.IdTipoProducto = carteraNegativa.IdTipoProducto;
					movimientoTransito.IdConcepto = carteraNegativa.IdConcepto;
					movimientoTransito.IdTipoOperacion = carteraNegativa.IdTipoOperacion;
					movimientoTransito.IdOperacion = carteraNegativa.IdOperacion;
					movimientoTransito.IdTipoComprobante = carteraNegativa.IdTipoComprobante;
					movimientoTransito.FolioComprobante = carteraNegativa.FolioComprobante;
					movimientoTransito.IdBolsa = carteraNegativa.IdBolsa;
					movimientoTransito.IdAgente = carteraNegativa.IdAgente;
					movimientoTransito.FechaLiquidacion = carteraNegativa.FechaLiquidacion;
					movimientoTransito.IdUsuario = entrada.p_IdUsuario;
					movimientoTransito.IdInstrumento = carteraNegativa.IdInstrumento;
					movimientoTransito.IdEmisor = carteraNegativa.IdEmisor;
					movimientoTransito.IdMonedaInstrumento = carteraNegativa.IdMonedaInstrumento;
					movimientoTransito.IdBoveda = carteraNegativa.IdBoveda;
					movimientoTransito.Cantidad = carteraNegativa.Cantidad;
					movimientoTransito.ValorRescate = carteraNegativa.ValorRescate;
					movimientoTransito.PrecioCompra = carteraNegativa.PrecioOperacion;
					movimientoTransito.MontoCompra 	= carteraNegativa.MontoOperacion;
					movimientoTransito.MontoCompraMonedaInstrumento = carteraNegativa.MontoCompraMonedaInstrumento;
					movimientoTransito.IdTipoReajusteInstrumento = carteraNegativa.IdTipoReajuste;
					movimientoTransito.IdTipoCanalIngreso = carteraNegativa.IdTipoCanalIngreso;
					movimientoTransito.CorteMinimo = carteraNegativa.CorteMinimo;
					movimientoTransito.CodigoIFRS = carteraNegativa.CodigoIFRS;
					movimientoTransito.IdCuentaCustodioPortafolio = carteraNegativa.IdCuentaCustodioPortafolio;
					movimientoTransito.IdCondicionLiquidacion = carteraNegativa.IdCondicionLiquidacion;
					movimientoTransito.IdMesa = carteraNegativa.IdMesa;
					movimientoTransito.Nemotecnico = carteraNegativa.Nemotecnico;
					movimientoTransito.FechaVencimientoOperacion = carteraNegativa.FechaVencimientoOperacion;
					movimientoTransito.FechaVencimientoInstrumento = carteraNegativa.FechaVencimientoOperacion;
					movimientoTransito.EsFungible = carteraNegativa.EsFungible;
					movimientoTransito.IdMovimientoComprobante = carteraNegativa.IdMovimientoComprobante;
					movimientoTransito.TasaCompra = carteraNegativa.TasaOperacion;
					movimientoTransito.IdCorredora = carteraNegativa.IdCorredoraContraparte;
					movimientoTransito.NemoSVS = carteraNegativa.NemoSVS;
					movimientoTransito.IdMonedaEmision = carteraNegativa.IdMonedaEmision;
					return movimientoTransito;
				})
				.collect(Collectors.toList());
		
		IngresoCustodia ingresoCustodia = new IngresoCustodia();
		IngresoCustodia.IngresoCustodiaSalida salidaIngresoCustodia = ingresoCustodia.logica(con, entradaIngresoCustodia);
		
		logger.trace("FIN::NetearCustodiaNegativa.crearMovimientoTransito");
		return salidaIngresoCustodia;
	}
	
	private SalidaNetearCustodiaNegativa certificarMovimientoTransito(Connection con, EntradaNetearCustodiaNegativa entrada, List<IngresoCustodiaSalida.ObjDatosSalida> carterasTransito) throws Exception {
		logger.trace("INICIO::NetearCustodiaNegativa.certificarMovimientoTransito");
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<Long> idMovimientosTransito = carterasTransito
				.stream()
				.map(carteraTransito->carteraTransito.IdCustodia)
				.collect(Collectors.toList());
		
		List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> movimientosEnTransito = GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(con, idMovimientosTransito);
		
		CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoEntrada entradaCertificarMovimientosEnTransito = new CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoEntrada();
		entradaCertificarMovimientosEnTransito.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCertificarMovimientosEnTransito.p_IdUsuario = entrada.p_IdUsuario;
		entradaCertificarMovimientosEnTransito.p_IdModulo = entrada.p_IdModulo;
		entradaCertificarMovimientosEnTransito.p_FechaOperacion = fechaSistema;
		entradaCertificarMovimientosEnTransito.P_MOVIMIENTOSCERTIFICAR = movimientosEnTransito
				.stream()
				.map(movimientoEnTrasito->{
					CertificarMovimientosEnTransito.ObjMovimientos movimientoCertificar = new CertificarMovimientosEnTransito.ObjMovimientos();
					movimientoCertificar.IdMovimientoCarteraEnTransito = movimientoEnTrasito.IdMovimientoCarteraEnTransito;
					movimientoCertificar.IdTipoOperacion = movimientoEnTrasito.IdTipoOperacion;
					return movimientoCertificar;
				})
				.collect(Collectors.toList());
		
		CertificarMovimientosEnTransito certificarMovimientosEnTransito = new CertificarMovimientosEnTransito();
		
		CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida salidaCertificarMovimientosEnTrasito = certificarMovimientosEnTransito.logica(con, entradaCertificarMovimientosEnTransito);
		
		SalidaNetearCustodiaNegativa out = new SalidaNetearCustodiaNegativa();
		out.P_OUT_ERRORESCONTADOR = salidaCertificarMovimientosEnTrasito.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaCertificarMovimientosEnTrasito.P_OUT_MENSAJES);
		
		logger.trace("FIN::NetearCustodiaNegativa.certificarMovimientoTransito");
		return out;
	}
	
	private void actualizarCarteraNegativaLiquidacion(Connection con, List<CarteraNegativa> carterasNegativas) throws Exception {
		logger.trace("INICIO::NetearCustodiaNegativa.actualizarCarteraNegativaLiquidacion");
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_CARTERA_NEGATIVA_LIQUIDACION, carterasNegativas, CarteraNegativa.class);
		
		logger.trace("FIN::NetearCustodiaNegativa.actualizarCarteraNegativaLiquidacion");
	}
	
	private void actualizarCarteraNegativaVigente(Connection con, List<PortafolioInstrumento> portafoliosInstrumento) throws Exception {
		logger.trace("INICIO::NetearCustodiaNegativa.actualizarCarteraNegativaVigente");
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_CARTERA_NEGATIVA_VIGENTE, portafoliosInstrumento, PortafolioInstrumento.class);
		
		logger.trace("FIN::NetearCustodiaNegativa.actualizarCarteraNegativaVigente");
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\": 1, \"p_IdUsuario\": 9, \"p_IdModulo\": \"CUS\", \"p_UsaReglasNegocio\": \"S\", \"p_SimularOperacion\": \"N\", \"p_PortafoliosInstrumento\": [{\"IdPortafolio\": 308818, \"IdInstrumento\": 2223, \"NemoSVS\": \"\"}]}";
				EntradaNetearCustodiaNegativa entrada = gson.fromJson(json, EntradaNetearCustodiaNegativa.class);
								
				NetearCustodiaNegativa demo = new NetearCustodiaNegativa();
				SalidaNetearCustodiaNegativa salida = demo.logica(con, entrada);
				System.out.println(salida);
				con.commit();
				
		    } catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}

}
