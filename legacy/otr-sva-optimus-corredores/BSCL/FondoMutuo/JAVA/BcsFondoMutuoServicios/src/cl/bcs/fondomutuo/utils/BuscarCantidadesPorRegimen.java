package cl.bcs.fondomutuo.utils;

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

import cl.bcs.custodia.ConsultarFlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaEntrada;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaSalida;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoEsAgrupadoPor;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoFiltrosBusqueda;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoUso;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class BuscarCantidadesPorRegimen implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(BuscarCantidadesPorRegimen.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaBuscarCantidadesPorRegimen{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public List<InClientes> p_Clientes;
		
		public EntradaBuscarCantidadesPorRegimen() {
			this.p_Clientes = new ArrayList<InClientes>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InClientes{
		public Long IdInterno;
		public Long IdCliente;
		public BigDecimal Cantidad;
		public String IdSerieFondo;
		public Long IdInstrumento;
		public String IdGrupoRegimen;
		public String IdRegimenFondoMutuo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaBuscarCantidadesPorRegimen{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoBuscarCantidadesPorRegimen> P_OUT_RESULTADO;
		
		public SalidaBuscarCantidadesPorRegimen() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoBuscarCantidadesPorRegimen>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoBuscarCantidadesPorRegimen{
		public Long IdInterno;
		public Long IdCliente;
		public Long IdPortafolio;
		public BigDecimal Cantidad;
		public BigDecimal CantidadTotalCustodia;
		public BigDecimal Porcentaje;
		public String IdSerieFondo;
		public String IdRegimenFondoMutuo;
		public String IdGrupoRegimen;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CarteraMovimiento{
		public Long IdCliente;
		public Long IdPortafolio;
		public BigDecimal Cantidad;
		public String IdRegimenFondoMutuo;
		public String IdGrupoRegimen;
		public String IdSerieFondo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClienteGrupoRegimenFondo{
		public Long IdCliente;
		public Long IdInstrumento;
		public String IdSerieFondo;
		public String GrupoRegimen;
		public BigDecimal Total;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InstrumentoRegimenFondoMutuo{
		public Long IdIntrumento;
		public String IdRegimenFondoMutuo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaBuscarCantidadesPorRegimen jin = gson.fromJson(in.toString(), EntradaBuscarCantidadesPorRegimen.class);
		SalidaBuscarCantidadesPorRegimen out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaBuscarCantidadesPorRegimen logica(Connection con, EntradaBuscarCantidadesPorRegimen entrada) throws Exception{
		logger.info("INICIO::BuscarCantidadesPorRegimen");
		SalidaBuscarCantidadesPorRegimen out = new SalidaBuscarCantidadesPorRegimen();
		
		List<Long> idCliente = new ArrayList<Long>();
		List<InstrumentoRegimenFondoMutuo> idsInstrumentoRegimenFondoMutuo = new ArrayList<InstrumentoRegimenFondoMutuo>();
		for(InClientes cliente : entrada.p_Clientes) {
			if(cliente.Cantidad == null || cliente.Cantidad.compareTo(BigDecimal.ZERO) != 1) {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(cliente.IdInterno,MensajeSalida.MENSAJE_ERROR,"NUMERO_CUOTA_VALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}
			else{
				idCliente.add(cliente.IdCliente);
				InstrumentoRegimenFondoMutuo instrumentoRegimenFondoMutuo = new InstrumentoRegimenFondoMutuo();
				instrumentoRegimenFondoMutuo.IdIntrumento = cliente.IdInstrumento;
				instrumentoRegimenFondoMutuo.IdRegimenFondoMutuo = cliente.IdRegimenFondoMutuo;
				idsInstrumentoRegimenFondoMutuo.add(instrumentoRegimenFondoMutuo);
			}
		}
		
		String fechaSistema = obtenerFechaSistema(con, entrada.p_IdEmpresa);
		
		InformacionEmpresa infoEmpresa = obtenerInformacionEmpresa(con, entrada.p_IdEmpresa);
		
		List<FlujoCustodia> custodias = obtenerCustodia(con,entrada,fechaSistema,infoEmpresa.IdPais,idCliente,idsInstrumentoRegimenFondoMutuo);
		
		HashMap<String,ClienteGrupoRegimenFondo> hashClienteGrupoRegimenFondo = obtenerTotalesPorClienteGrupoRegimenFondo(custodias);
		
		Map<String,List<FlujoCustodia>> hashFlujoCustodiaPorClienteGrupoRegimenFondo = obtenerHashFlujoCustodiaPorClienteGrupoRegimenFondo(custodias);
		
		List<ResultadoBuscarCantidadesPorRegimen> resultadosBuscarCantidadesPorRegimen = obtenerCantidadesPorRegimen(entrada.p_Clientes,hashClienteGrupoRegimenFondo,hashFlujoCustodiaPorClienteGrupoRegimenFondo);
		
		out.P_OUT_MENSAJES = obtenerMensajesSalidaError(con, entrada.p_IdEmpresa, resultadosBuscarCantidadesPorRegimen);
		
		out.P_OUT_RESULTADO = obtenerResultadoCorrecto(resultadosBuscarCantidadesPorRegimen);
		
		out.P_OUT_ERRORESCONTADOR = new Long(out.P_OUT_MENSAJES.size());
		
		logger.info("FIN::BuscarCantidadesPorRegimen");
		return out;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.info("INICIO::BuscarCantidadesPorRegimen.obtenerFechaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		logger.info("FIN::BuscarCantidadesPorRegimen.obtenerFechaSistema");
		return fechaSistema;
	}
	
	private InformacionEmpresa obtenerInformacionEmpresa(Connection con, Long idEmpresa) throws Exception {
		logger.info("INICIO::BuscarCantidadesPorRegimen.obtenerInformacionEmpresa");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa informacionEmpresa = consultarEmpresaService.logica(con, idEmpresa);
		logger.info("FIN::BuscarCantidadesPorRegimen.obtenerInformacionEmpresa");
		return informacionEmpresa;
	}
	
	private List<FlujoCustodia> obtenerCustodia(Connection con, EntradaBuscarCantidadesPorRegimen entrada, String fechaSistema, Long idPais, List<Long> idCliente, List<InstrumentoRegimenFondoMutuo> idsInstrumentoRegimenFondoMutuo) throws Exception{
		logger.info("INICIO::BuscarCantidadesPorRegimen.obtenerCustodia");
		ConsultarFlujoCustodia flujoCustodia = new ConsultarFlujoCustodia();
		FlujoCustodiaEntrada flujoCustodiaEntrada = new FlujoCustodiaEntrada();
		flujoCustodiaEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		flujoCustodiaEntrada.p_IdUsuario = entrada.p_IdUsuario;
		flujoCustodiaEntrada.p_IdModulo = entrada.p_IdModulo;
		flujoCustodiaEntrada.p_FechaSaldo = fechaSistema;
		flujoCustodiaEntrada.p_IdPais = idPais;
		flujoCustodiaEntrada.p_DiasProyeccion = 0;
		flujoCustodiaEntrada.p_DiaHabil = "S";
		flujoCustodiaEntrada.p_ValorizarFlujo = "N";
		
		flujoCustodiaEntrada.p_Uso = idCliente.parallelStream().map(cliente->{
			FlujoUso uso = new FlujoUso();
			uso.UsoTipoOperacion = "RESCFM";
			uso.IdCliente = cliente;
			uso.IdPortafolio = null;
			uso.BusquedaEspecifica = "";
			uso.IdTipoInstrumentoEspecifico = "";
			return uso;
		}).collect(Collectors.toList());
		
		flujoCustodiaEntrada.p_FiltrosBusqueda = idsInstrumentoRegimenFondoMutuo.parallelStream().map(instrumentoRegimenFondomutuo->{
			FlujoFiltrosBusqueda busqueda = new FlujoFiltrosBusqueda();
			busqueda.IdClasificacionTipoInstrumento = "FM";
			busqueda.IdInstrumento = instrumentoRegimenFondomutuo.IdIntrumento;
			busqueda.IdRegimenFondoMutuo = instrumentoRegimenFondomutuo.IdRegimenFondoMutuo;
			return busqueda;
		}).collect(Collectors.toList());
		
		FlujoEsAgrupadoPor agrupador = new FlujoEsAgrupadoPor();
		agrupador.ClasificacionTipoInstrumento = "S";
		agrupador.TipoInstrumento = "S";
		agrupador.Instrumento = "S";
		agrupador.IdRegimenFondoMutuo = "S";
		agrupador.GrupoRegimen = "S";
		agrupador.Emisor = "N";
		agrupador.Nemotecnico = "N";
		agrupador.NemoSVS = "N";
		agrupador.FechaEmisionInstrumento = "N";
		agrupador.FechaVencimientoInstrumento = "N";
		agrupador.FechaLiquidacionInstrumento = "N";
		agrupador.TipoReajuste = "N";
		agrupador.EsFungible = "N";
		agrupador.CodigoIFRS = "N";
		agrupador.Boveda = "N";
		agrupador.Bolsa = "N";
		agrupador.TipoOperacion = "N";
		agrupador.Mesa = "N";
		agrupador.FolioRegistro = "N";
		agrupador.ItemOrigen = "N";
		flujoCustodiaEntrada.p_EsAgrupadoPor.add(agrupador);
		
		FlujoCustodiaSalida salida = flujoCustodia.logica(con, flujoCustodiaEntrada);
		List<FlujoCustodia> custodias = salida.p_out_FlujoTotalizado.parallelStream().filter(custodia->custodia.Cantidad != null && custodia.Cantidad.compareTo(BigDecimal.ZERO) != -1).collect(Collectors.toList());
		logger.info("FIN::BuscarCantidadesPorRegimen.obtenerCustodia");
		
		return custodias;
	}
	
	private HashMap<String,ClienteGrupoRegimenFondo> obtenerTotalesPorClienteGrupoRegimenFondo(List<FlujoCustodia> custodias){
		logger.info("INICIO::BuscarCantidadesPorRegimen.obtenerTotalesPorClienteGrupoRegimenFondo");
		HashMap<String,ClienteGrupoRegimenFondo> resultado = new HashMap<String,ClienteGrupoRegimenFondo>();
		custodias.stream().forEach(custodia->{
			String id = custodia.IdCliente + "_" + custodia.IdInstrumento + "_" + custodia.GrupoRegimen;
			ClienteGrupoRegimenFondo clienteGrupoRegimenFondo = resultado.get(id);
			if(clienteGrupoRegimenFondo == null) {
				clienteGrupoRegimenFondo = new ClienteGrupoRegimenFondo();
				clienteGrupoRegimenFondo.IdCliente = custodia.IdCliente;
				clienteGrupoRegimenFondo.IdInstrumento = custodia.IdInstrumento;
				clienteGrupoRegimenFondo.IdSerieFondo = custodia.Nemotecnico;
				clienteGrupoRegimenFondo.Total = custodia.Cantidad;
			}else {
				clienteGrupoRegimenFondo.Total = clienteGrupoRegimenFondo.Total.add(custodia.Cantidad, MathContext.DECIMAL128);
			}
			resultado.put(id, clienteGrupoRegimenFondo);
		});
		logger.info("FIN::BuscarCantidadesPorRegimen.obtenerTotalesPorClienteGrupoRegimenFondo");
		return resultado;
	}
	
	private Map<String,List<FlujoCustodia>> obtenerHashFlujoCustodiaPorClienteGrupoRegimenFondo(List<FlujoCustodia> custodias){
		logger.info("INICIO::BuscarCantidadesPorRegimen.obtenerHashFlujoCustodiaPorClienteGrupoRegimenFondo");
		Map<String,List<FlujoCustodia>> hashFlujoCustodiaPorClienteGrupoRegimenFondo = custodias.parallelStream().collect(Collectors.groupingBy(custodia->custodia.IdCliente + "_" + custodia.IdInstrumento + "_" + custodia.GrupoRegimen));
		logger.info("FIN::BuscarCantidadesPorRegimen.obtenerHashFlujoCustodiaPorClienteGrupoRegimenFondo");
		return hashFlujoCustodiaPorClienteGrupoRegimenFondo;
	}
	
	private List<ResultadoBuscarCantidadesPorRegimen> obtenerCantidadesPorRegimen(List<InClientes> clientes, HashMap<String,ClienteGrupoRegimenFondo> hashClienteGrupoRegimenFondo, Map<String,List<FlujoCustodia>> hashFlujoCustodiaPorClienteGrupoRegimenFondo){
		logger.info("INICIO::BuscarCantidadesPorRegimen.obtenerCantidadesPorRegimen");
		List<ResultadoBuscarCantidadesPorRegimen> resultadosBuscarCantidadesPorRegimen = new ArrayList<ResultadoBuscarCantidadesPorRegimen>();
		clientes.stream().forEach(cliente->{
			String id = cliente.IdCliente + "_" + cliente.IdInstrumento + "_" + cliente.IdGrupoRegimen;
			ClienteGrupoRegimenFondo clienteGrupoRegimenFondo = hashClienteGrupoRegimenFondo.get(id);
			List<FlujoCustodia> listCustodiaPorClienteGrupoRegimenFondo = hashFlujoCustodiaPorClienteGrupoRegimenFondo.get(id);
			BigDecimal porcentaje = null;
			
			if(clienteGrupoRegimenFondo != null) {
				porcentaje = obtenerPorcentaje(cliente.Cantidad, clienteGrupoRegimenFondo.Total);
			}
			
			if(porcentaje == null || porcentaje.compareTo(BigDecimal.ONE) == 1 || listCustodiaPorClienteGrupoRegimenFondo == null) {
				ResultadoBuscarCantidadesPorRegimen resultadoBuscarCantidadesPorRegimen = new ResultadoBuscarCantidadesPorRegimen();
				resultadoBuscarCantidadesPorRegimen.IdInterno = cliente.IdInterno;
				resultadoBuscarCantidadesPorRegimen.IdCliente = cliente.IdCliente;
				resultadoBuscarCantidadesPorRegimen.IdPortafolio = 0L;
				resultadoBuscarCantidadesPorRegimen.Cantidad = cliente.Cantidad == null ? BigDecimal.ZERO : cliente.Cantidad;
				resultadoBuscarCantidadesPorRegimen.CantidadTotalCustodia = clienteGrupoRegimenFondo == null || clienteGrupoRegimenFondo.Total == null ? BigDecimal.ZERO : clienteGrupoRegimenFondo.Total;
				resultadoBuscarCantidadesPorRegimen.Porcentaje = porcentaje;
				resultadoBuscarCantidadesPorRegimen.IdSerieFondo = cliente.IdSerieFondo;
				resultadoBuscarCantidadesPorRegimen.IdRegimenFondoMutuo = "";
				resultadoBuscarCantidadesPorRegimen.IdGrupoRegimen = cliente.IdGrupoRegimen;
				resultadoBuscarCantidadesPorRegimen.IdEstado = "ERR";
				resultadosBuscarCantidadesPorRegimen.add(resultadoBuscarCantidadesPorRegimen);
			}else {
				resultadosBuscarCantidadesPorRegimen.addAll(obtenerCantidadesCliente(cliente,listCustodiaPorClienteGrupoRegimenFondo,porcentaje));
			}
		});
		logger.info("FIN::BuscarCantidadesPorRegimen.obtenerCantidadesPorRegimen");
		
		return resultadosBuscarCantidadesPorRegimen;
	}
	
	private BigDecimal obtenerPorcentaje(BigDecimal cantidadRetirar, BigDecimal total) {
		logger.info("INICIO::BuscarCantidadesPorRegimen.obtenerPorcentaje");
		
		BigDecimal porcentaje;
		
		if (total.compareTo(BigDecimal.ZERO)>0) {
			 porcentaje = cantidadRetirar.divide(total, MathContext.DECIMAL128);
		}else {
			
			porcentaje = null;
		}
		
		logger.info("FIN::BuscarCantidadesPorRegimen.obtenerPorcentaje");
		return porcentaje;
	}
	
	private List<ResultadoBuscarCantidadesPorRegimen> obtenerCantidadesCliente(InClientes cliente, List<FlujoCustodia> listCustodiaPorClienteGrupoRegimenFondo, BigDecimal porcentaje){
		logger.info("INICIO::BuscarCantidadesPorRegimen.obtenerCantidadesCliente");
		List<ResultadoBuscarCantidadesPorRegimen> resultado = listCustodiaPorClienteGrupoRegimenFondo.parallelStream().map(custodia->{
			ResultadoBuscarCantidadesPorRegimen resultadoBuscarCantidadesPorRegimen = new ResultadoBuscarCantidadesPorRegimen();
			resultadoBuscarCantidadesPorRegimen.IdInterno = cliente.IdInterno;
			resultadoBuscarCantidadesPorRegimen.IdCliente = cliente.IdCliente;
			resultadoBuscarCantidadesPorRegimen.IdPortafolio = custodia.IdPortafolio;
			resultadoBuscarCantidadesPorRegimen.Cantidad = custodia.Cantidad.multiply(porcentaje, MathContext.DECIMAL128);
			resultadoBuscarCantidadesPorRegimen.CantidadTotalCustodia = custodia.Cantidad;
			resultadoBuscarCantidadesPorRegimen.Porcentaje = porcentaje;
			resultadoBuscarCantidadesPorRegimen.IdSerieFondo = cliente.IdSerieFondo;
			resultadoBuscarCantidadesPorRegimen.IdRegimenFondoMutuo = custodia.IdRegimenFondoMutuo;
			resultadoBuscarCantidadesPorRegimen.IdGrupoRegimen = cliente.IdGrupoRegimen;
			resultadoBuscarCantidadesPorRegimen.IdEstado = "OK";
			return resultadoBuscarCantidadesPorRegimen;
		}).collect(Collectors.toList());
		logger.info("FIN::BuscarCantidadesPorRegimen.obtenerCantidadesCliente");
		return resultado;
	}
	
	private List<MensajeSalida> obtenerMensajesSalidaError(Connection con, Long idEmpresa, List<ResultadoBuscarCantidadesPorRegimen> resultadosBuscarCantidadesPorRegimen) throws Exception{
		logger.info("INICIO::BuscarCantidadesPorRegimen.obtenerMensajesSalidaError");
		List<MensajeSalida> mensajesError = new ArrayList<MensajeSalida>();
		List<ResultadoBuscarCantidadesPorRegimen> resultadosErrores = resultadosBuscarCantidadesPorRegimen.parallelStream().filter(resultado->_c.EstadoError.equals(resultado.IdEstado)).collect(Collectors.toList());
		for(ResultadoBuscarCantidadesPorRegimen resultadoError : resultadosErrores) {
			HashMap<String,String> parametros = new HashMap<String,String>();
			parametros.put("Nemotecnico", resultadoError.IdSerieFondo);
			parametros.put("CantidadRetiro", resultadoError.Cantidad.toPlainString());
			parametros.put("CantidadCustodia",resultadoError.CantidadTotalCustodia.toPlainString());
			MensajeSalida mensajeSalidaError = new MensajeSalida(resultadoError.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FM_CLIENTE_SIN_CUSTODIA",parametros,con,idEmpresa);
			mensajesError.add(mensajeSalidaError);
		}
		
		logger.info("FIN::BuscarCantidadesPorRegimen.obtenerMensajesSalidaError");
		return mensajesError;
	}
	
	private List<ResultadoBuscarCantidadesPorRegimen> obtenerResultadoCorrecto(List<ResultadoBuscarCantidadesPorRegimen> resultadosBuscarCantidadesPorRegimen){
		logger.info("INICIO::BuscarCantidadesPorRegimen.obtenerResultadoCorrecto");
		List<ResultadoBuscarCantidadesPorRegimen> resultadoCorrecto = resultadosBuscarCantidadesPorRegimen.parallelStream().filter(resultado->"OK".equals(resultado.IdEstado)).collect(Collectors.toList());
		logger.info("FIN::BuscarCantidadesPorRegimen.obtenerResultadoCorrecto");
		return resultadoCorrecto;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			EntradaBuscarCantidadesPorRegimen entrada = new EntradaBuscarCantidadesPorRegimen();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "FMM";
			entrada.p_IdUsuario = 21L;
			
			InClientes cliente = new InClientes();
			cliente.IdInterno = 1L;
			cliente.IdCliente = 524891L;
			cliente.IdSerieFondo = "FMLIQUIDEZ_A";
			cliente.IdInstrumento = 346394L;
			cliente.IdGrupoRegimen = "APV";
			cliente.Cantidad = new BigDecimal(150);
			
			entrada.p_Clientes.add(cliente);
			
			BuscarCantidadesPorRegimen buscarCantidadesPorRegimen = new BuscarCantidadesPorRegimen();
			SalidaBuscarCantidadesPorRegimen salida = buscarCantidadesPorRegimen.logica(con, entrada);
			System.out.println(salida);
			
		}
		
		System.out.println("== END ==");
	}

}
