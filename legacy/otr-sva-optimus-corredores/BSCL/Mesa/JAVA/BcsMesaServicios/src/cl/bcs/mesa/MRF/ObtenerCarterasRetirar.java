package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ObtenerCarterasRetirar implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ObtenerCarterasRetirar.class);
	private final String SQL_SELECT_CARTERAS_RF = loadResourceToString("cl/bcs/mesa/MRF/sql/ObtenerCarterasRetirar_SelectCarterasRF.sql");
	private final String SQL_SELECT_CARTERAS_IF = loadResourceToString("cl/bcs/mesa/MRF/sql/ObtenerCarterasRetirar_SelectCarterasIF.sql");
	
	public static class EntradaObtenerCarterasRetirar extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Consulta> p_Consultas;
		
		public EntradaObtenerCarterasRetirar() {
			this.p_Consultas = new ArrayList<Consulta>();
		}
	}
	
	public static class Consulta extends BcsPojo {
		public Long IdInterno;
		public Long IdPortafolio;
		public String IdClasificacionTipoInstrumento;
		public String Nemotecnico;
		public String NemoSVS;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public String IdBoveda;
	}
	
	public static class Cartera extends BcsPojo {
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdPortafolio;
		public String Nemo;
		public String IdBoveda;
		public BigDecimal Saldo;
	}
		
	public static class CarteraRetiro extends BcsPojo {
		public Long IdCartera;
		public Long FolioCartera;
		public BigDecimal SaldoRetiro;
	}
	
	public static class Resultado extends BcsPojo {
		public Long IdInterno;
		public List<CarteraRetiro> CarterasRetiro;
		public BigDecimal SaldoRestante;
		public String IdEstado;
		
		public Resultado() {
			this.CarterasRetiro = new ArrayList<CarteraRetiro>();
		}
	}
	
	public static class SalidaObtenerCarterasRetirar extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Resultado> P_OUT_RESULTADO;
		
		public SalidaObtenerCarterasRetirar() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<Resultado>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaObtenerCarterasRetirar jin = gson.fromJson(in.toString(), EntradaObtenerCarterasRetirar.class);
		SalidaObtenerCarterasRetirar out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaObtenerCarterasRetirar logica(Connection con, EntradaObtenerCarterasRetirar entrada) throws Exception{
		logger.info("INICIO::ObtenerCarterasRetirar");
		SalidaObtenerCarterasRetirar out = new SalidaObtenerCarterasRetirar();
		
		out = validarEntrada(con, entrada);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		out.P_OUT_RESULTADO = obtenerResultado(con, entrada);
		
		logger.info("FIN::ObtenerCarterasRetirar");
		return out;
	}
	
	private SalidaObtenerCarterasRetirar validarEntrada(Connection con, EntradaObtenerCarterasRetirar entrada) throws Exception {
		logger.trace("INICIO::ObtenerCarterasRetirar.validarEntrada");
		SalidaObtenerCarterasRetirar out = new SalidaObtenerCarterasRetirar();
		Map<Long, List<Consulta>> consultasAgrupadasPorIdInterno = entrada.p_Consultas.stream().collect(Collectors.groupingBy(consulta->consulta.IdInterno));
		for(Entry<Long, List<Consulta>> consulta : consultasAgrupadasPorIdInterno.entrySet()) {
			if(consulta.getValue().size() > 1) {
				HashMap<String, String> parametrosMensaje = new HashMap<String, String>(2);
				parametrosMensaje.put("IdInterno", String.valueOf(consulta.getKey()));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OBTENER_CARTERAS_RETIRAR_MESA_ID_INTERNO_REPETIDO", parametrosMensaje, con, entrada.p_IdEmpresa));
			}
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		logger.trace("FIN::ObtenerCarterasRetirar.validarEntrada");
		return out;
	}
	
	private List<Resultado> obtenerResultado(Connection con, EntradaObtenerCarterasRetirar entrada) throws Exception {
		logger.trace("INICIO::ObtenerCarterasRetirar.obtenerResultado");
		List<Resultado> resultados = new ArrayList<Resultado>();
		
		List<Cartera> carterasRF = obtenerCarterasRF(con, entrada);
		List<Cartera> carterasIF = obtenerCarterasIF(con, entrada);
		
		Map<String, List<Cartera>> carterasRFAgrupadasPorNemotecnicoPortafolio = carterasRF.stream().collect(Collectors.groupingBy(carteraRF->obtenerLlaveNemoPortafolio(carteraRF.Nemo, carteraRF.IdPortafolio)));
		Map<String, List<Cartera>> carterasIFAgrupadasPorNemoSVSPortafolio = carterasIF.stream().collect(Collectors.groupingBy(carteraIF->obtenerLlaveNemoPortafolio(carteraIF.Nemo, carteraIF.IdPortafolio)));
		
		for(Consulta consulta : entrada.p_Consultas) {
			Resultado resultado = new Resultado();
			resultado.IdInterno = consulta.IdInterno;
			resultado.IdEstado = "ERR";
			List<Cartera> carterasEncontradas = null;
			
			if("RF".equals(consulta.IdClasificacionTipoInstrumento)) {
				resultado.SaldoRestante = consulta.Cantidad;
				carterasEncontradas = carterasRFAgrupadasPorNemotecnicoPortafolio.get(obtenerLlaveNemoPortafolio(consulta.Nemotecnico, consulta.IdPortafolio));
			}
			
			if("IF".equals(consulta.IdClasificacionTipoInstrumento)) {
				resultado.SaldoRestante = consulta.ValorRescate;
				carterasEncontradas = carterasIFAgrupadasPorNemoSVSPortafolio.get(obtenerLlaveNemoPortafolio(consulta.NemoSVS, consulta.IdPortafolio));
			}
			
			if(carterasEncontradas != null) {
				for(Cartera cartera : carterasEncontradas) {
					if(esBovedaCorrecta(consulta, cartera) && cartera.Saldo.compareTo(BigDecimal.ZERO) == 1) {
						CarteraRetiro carteraRetiro = new CarteraRetiro();
						carteraRetiro.IdCartera = cartera.IdCartera;
						carteraRetiro.FolioCartera = cartera.FolioCartera;
						if(cartera.Saldo.compareTo(resultado.SaldoRestante) == 1) {
							carteraRetiro.SaldoRetiro = resultado.SaldoRestante;
						} else {
							carteraRetiro.SaldoRetiro = cartera.Saldo;
						}
						resultado.CarterasRetiro.add(carteraRetiro);
						
						resultado.SaldoRestante = resultado.SaldoRestante.subtract(carteraRetiro.SaldoRetiro, MathContext.DECIMAL128);
						cartera.Saldo = cartera.Saldo.subtract(carteraRetiro.SaldoRetiro, MathContext.DECIMAL128);
						
						if(resultado.SaldoRestante.compareTo(BigDecimal.ZERO) == 0) {
							resultado.IdEstado = "OK";
							break;
						}
					}
				}
			}
			
			resultados.add(resultado);
		}
		
		logger.trace("FIN::ObtenerCarterasRetirar.obtenerResultado");
		return resultados;
	}
	
	private List<Cartera> obtenerCarterasRF(Connection con, EntradaObtenerCarterasRetirar entrada) throws Exception {
		logger.trace("INICIO::ObtenerCarterasRetirar.obtenerCarterasRF");
		List<Consulta> consultaRF = entrada.p_Consultas
				.stream()
				.filter(consulta->"RF".equals(consulta.IdClasificacionTipoInstrumento))
				.collect(Collectors.toList());
		
		List<String> nemotecnicos = consultaRF.stream().map(consulta->consulta.Nemotecnico).distinct().collect(Collectors.toList());
		List<Long> idPortafolios = consultaRF.stream().map(consulta->consulta.IdPortafolio).distinct().collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(4);
		parametros.put("IdEmpresa", entrada.p_IdEmpresa);
		parametros.put("Nemotecnico", nemotecnicos);
		parametros.put("IdPortafolio", idPortafolios);
		
		SQL sql = new SQL();
		List<Cartera> carterasRF = sql.query(con, SQL_SELECT_CARTERAS_RF, parametros, Cartera.class);
		
		logger.trace("FIN::ObtenerCarterasRetirar.obtenerCarterasRF");
		return carterasRF;
	}
	
	private List<Cartera> obtenerCarterasIF(Connection con, EntradaObtenerCarterasRetirar entrada) throws Exception {
		logger.trace("INICIO::ObtenerCarterasRetirar.obtenerCarterasIF");
		List<Consulta> consultaIF = entrada.p_Consultas
				.stream()
				.filter(consulta->"IF".equals(consulta.IdClasificacionTipoInstrumento))
				.collect(Collectors.toList());
		
		List<String> nemosSVS = consultaIF.stream().map(consulta->consulta.NemoSVS).distinct().collect(Collectors.toList());
		List<Long> idPortafolios = consultaIF.stream().map(consulta->consulta.IdPortafolio).distinct().collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(4);
		parametros.put("IdEmpresa", entrada.p_IdEmpresa);
		parametros.put("NemoSVS", nemosSVS);
		parametros.put("IdPortafolio", idPortafolios);
		
		SQL sql = new SQL();
		List<Cartera> carterasIF = sql.query(con, SQL_SELECT_CARTERAS_IF, parametros, Cartera.class);
		
		logger.trace("FIN::ObtenerCarterasRetirar.obtenerCarterasIF");
		return carterasIF;
	}
	
	private String obtenerLlaveNemoPortafolio(String nemo, Long idPortafolio) {
		logger.trace("INICIO::ObtenerCarterasRetirar.obtenerLlaveNemoPortafolio");
		String llaveNemoPortafolio = nemo + "_" + idPortafolio;
		logger.trace("FIN::ObtenerCarterasRetirar.obtenerLlaveNemoPortafolio");
		return llaveNemoPortafolio;
	}
	
	private Boolean esBovedaCorrecta(Consulta consulta, Cartera cartera) {
		logger.trace("INICIO::ObtenerCarterasRetirar.esBovedaCorrecta");
		Boolean esBovedaCorrecta = consulta.IdBoveda == null || consulta.IdBoveda.isEmpty() || cartera.IdBoveda.equals(consulta.IdBoveda);
		logger.trace("FIN::ObtenerCarterasRetirar.esBovedaCorrecta");
		return esBovedaCorrecta;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		EntradaObtenerCarterasRetirar entrada = new EntradaObtenerCarterasRetirar();
		entrada.p_IdEmpresa = 1L;
		entrada.p_IdUsuario = 22L;
		entrada.p_IdModulo = "MRF";
		
		Consulta consulta = new Consulta();
		consulta.IdInterno = 1L;
		consulta.IdPortafolio = 512339L;
		consulta.IdClasificacionTipoInstrumento = "IF";
		consulta.NemoSVS = "FUSTD-050623";
		consulta.ValorRescate = new BigDecimal("50000");
		
		entrada.p_Consultas.add(consulta);
		
		ObtenerCarterasRetirar demo = new ObtenerCarterasRetirar();				
		try {
			SalidaObtenerCarterasRetirar salida = demo.logica(con, entrada);
			con.rollback();
			System.out.println(salida.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	};
		System.out.println("== END ==");
	}

}
