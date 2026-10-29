package cl.bcs.custodia;


import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarCarteraDerivados implements JavaCall{
	
	//Consultas SQL para opciones
	private final String SELECT_CARTERA_OPCIONES = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivados_ObtenerCarteraOpciones.sql");
	private final String SELECT_CARTERA_OPCIONES_BY_PORTAFOLIO = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivados_ObtenerCarteraOpcionesByPortafolio.sql");
	
	private final String SELECT_EJERCIDOS_SORTEADOS_OPCIONES = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivados_SelectEjercidosSorteados.sql");
	
	//Consultas SQL para futuro
	private final String SELECT_CARTERA_FUTURO = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivados_ObtenerCarteraFuturo.sql");
	private final String SELECT_CARTERA_FUTURO_BY_PORTAFOLIO = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivados_ObtenerCarteraFuturoByPortafolio.sql");
	
	
	
	//private final String SELECT_CARTERA_OPCIONES = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivados_ObtenerCarteraOpciones.sql");
	
	
	public static class Entrada_ConsultarCustodiaDerivados{
		public Integer p_IdEmpresa;
		public Integer p_IdUsuario;
		public String p_IdModulo;		
		public Date p_FechaConsulta;
		public String p_IdClasificacionTipoInstrumento;
		public String p_IdTipoInstrumento;
		public Integer p_IdInstrumento;		
		public Integer p_IdCliente;
		public String p_TipoDerivado;
		public Long p_IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class CustodiaDerivados{
		public String DocumentoIdentidad;
		public String SocioNegocio;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public Long FolioCartera;
		public String Instrumento;
		public String Tipo;
		public BigDecimal Contratos;
		public BigDecimal ContratosCCLV;
		public String TipoOpcion;
		public String Mercado;
		public String Familia;
		public String Subyacente;
		public String Estilo;
		public String ModalidadLiquidacion;
		public Date FechaVencimiento;
		public BigDecimal PrecioEjercicio;
		public Integer PrecioSubyacente;
		public Date FechaInver;
		public Long ContratosEjercidos;
		public BigDecimal ContratosEjercidosCCLV;
		public BigDecimal ContratosSorteados;
		public String TipoPosicion;
		public String Cuadratura;
		
		public CustodiaDerivados() {			
			PrecioSubyacente = 0;
			ContratosEjercidosCCLV = BigDecimal.ZERO;
			ContratosSorteados = BigDecimal.ZERO;
			Cuadratura = "PEN";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	
	public static class CustodiaDerivadosFuturo{
		public String DocumentoIdentidad;
		public String RazonSocial;
		public String CodigoPortafolio;
		public Long FolioCartera;
		public String Instrumento;
		public String TipoInstrumento;
		public String Tipo;
		public BigDecimal Cantidad;
		public Date FechaOperacion;
		public Date FechaVencimientoOperacion;
		public BigDecimal PrecioCompra;
		public BigDecimal TasaCompra;
		public BigDecimal TasaMercado;
		public BigDecimal MontoCompra;
		public BigDecimal ValorMercado;
		public String FolioTransaccion;
		public Date FechaLiquidacion;
		public String Agente;
		public String NombreSucursal;
		public String BolsaOrigen;
		public String Documento;
		public Long FolioComprobante;
		public BigDecimal PrecioBaseFuturos;	
		public String Liquidacion;
		public BigDecimal MargenExigido;
		public BigDecimal ResultadoDiario;
		public BigDecimal ResultadoAcumulado;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class EjercidosSorteados{
		public BigDecimal ContratosAbiertosDia;
		public BigDecimal ContratosEjercidos;
		public BigDecimal ContratosSorteados;		
		public String Sorteado; 
		
		public EjercidosSorteados() {
			ContratosAbiertosDia = BigDecimal.ZERO;
			ContratosEjercidos = BigDecimal.ZERO;
			ContratosSorteados = BigDecimal.ZERO;
			Sorteado = "NO";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public static class Salida_ConsultarCustodiaDerivados{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public List<CustodiaDerivados> P_OUT_CARTERA;		
		public List<CustodiaDerivadosFuturo> P_OUT_CARTERA_FUTURO;	
		
		public Salida_ConsultarCustodiaDerivados() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			
			this.P_OUT_CARTERA = new ArrayList<CustodiaDerivados>();			
			this.P_OUT_CARTERA_FUTURO = new ArrayList<CustodiaDerivadosFuturo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		Entrada_ConsultarCustodiaDerivados in=gson.fromJson(jin.toString(),Entrada_ConsultarCustodiaDerivados.class);
		Salida_ConsultarCustodiaDerivados out=logica(con,in);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private List<CustodiaDerivados> obtenerCarteraOpciones(Connection con, Entrada_ConsultarCustodiaDerivados in) throws Exception {
		SQL sql = new SQL();
		List<CustodiaDerivados> custodias = new ArrayList<CustodiaDerivados>();
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		String fechaAnterior = obtenerDiaAnteriorHabil(con, in.p_FechaConsulta);
		
		params.put("p_FechaOperacion", in.p_FechaConsulta);
		if(in.p_IdPortafolio == null) {
			custodias = sql.query(con, SELECT_CARTERA_OPCIONES, params, CustodiaDerivados.class);
		}
		else {
			params.put("p_IdPortafolio", in.p_IdPortafolio);
			custodias = sql.query(con, SELECT_CARTERA_OPCIONES_BY_PORTAFOLIO, params, CustodiaDerivados.class);
		}
		for(CustodiaDerivados custodia: custodias) {
			if(custodia.TipoPosicion.equals("POSICION")) {
				EjercidosSorteados ejercidosSorteados = obtenerEjercidosSorteados(con, fechaAnterior, custodia.Instrumento, custodia.IdPortafolio);					
				custodia.ContratosCCLV = ejercidosSorteados.ContratosAbiertosDia;
				custodia.ContratosEjercidosCCLV = ejercidosSorteados.ContratosEjercidos;
				custodia.ContratosSorteados = ejercidosSorteados.ContratosSorteados;
				if(ejercidosSorteados.Sorteado.equals("NO")) {
					custodia.Cuadratura = "PEN";
				}
				else if(ejercidosSorteados.Sorteado.equals("SI")) {
					if(new BigDecimal(custodia.ContratosEjercidos).compareTo(custodia.ContratosEjercidosCCLV) != 0) {
						custodia.Cuadratura = "DIFERENCIA EN CONTRATOS EJERCIDOS";
					}
					else if(custodia.Contratos.compareTo(custodia.ContratosCCLV) != 0) {
						custodia.Cuadratura = "DIFERENCIA EN CONTRATOS";
					}
					else {
						custodia.Cuadratura = "OK";
					}
				}
			}
		}
		return custodias;
	}		
	
	private EjercidosSorteados obtenerEjercidosSorteados(Connection con, String FechaOperacion, String Nemotecnico, Long IdPortafolio) throws Exception {
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("p_Nemotecnico", Nemotecnico);
		params.put("p_FechaOperacion", FechaOperacion);
		params.put("p_IdPortafolio", IdPortafolio);
		List<EjercidosSorteados> ejercidosSorteados = new ArrayList<EjercidosSorteados>();
		ejercidosSorteados = sql.query(con, SELECT_EJERCIDOS_SORTEADOS_OPCIONES, params, EjercidosSorteados.class);
		if(ejercidosSorteados.isEmpty()) {
			return new EjercidosSorteados();
		}
		return ejercidosSorteados.get(0);
	}
	
	
	private List<CustodiaDerivadosFuturo> ObtenerCarteraFuturo(Connection con, Entrada_ConsultarCustodiaDerivados in) throws Exception {
		SQL sql = new SQL();
		List<CustodiaDerivadosFuturo> custodias = new ArrayList<CustodiaDerivadosFuturo>();
		HashMap<String, Object> params = new HashMap<String, Object>();		
		params.put("p_FechaOperacion", in.p_FechaConsulta);
		if(in.p_IdPortafolio == null) {
			custodias = sql.query(con, SELECT_CARTERA_FUTURO, params, CustodiaDerivadosFuturo.class);
		}
		else {
			params.put("p_IdPortafolio", in.p_IdPortafolio);
			custodias = sql.query(con, SELECT_CARTERA_FUTURO_BY_PORTAFOLIO, params, CustodiaDerivadosFuturo.class);
		}				
		return custodias;
	}
	
	private String obtenerDiaAnteriorHabil(Connection con, Date fechaOperacion) throws Exception {
		ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
		InObtenerDiaAnterior entradaObtenerDiaAnterior = new InObtenerDiaAnterior();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		entradaObtenerDiaAnterior.soloCaeDiaHabil = "S";
		entradaObtenerDiaAnterior.diaHabil = "S";
		//entradaObtenerDiaAnterior.
		InObtenerDiaAnterior.FechasIn fecha = new InObtenerDiaAnterior.FechasIn();
		fecha.fechaInicial = dateFormat.format(fechaOperacion);
		fecha.numeroDias = 1L;
		//fecha.pais = 44L;
		entradaObtenerDiaAnterior.fechas.add(fecha);
		OutObtenerDiaAnterior salidaObtenerDiaAnterior = obtenerDiaAnterior.logica(con, entradaObtenerDiaAnterior);
		return salidaObtenerDiaAnterior.fechas.get(0).fechaCalculada;
	}
	
	
	public Salida_ConsultarCustodiaDerivados logica(Connection con, Entrada_ConsultarCustodiaDerivados entrada) throws Exception{
		Salida_ConsultarCustodiaDerivados custodias = new Salida_ConsultarCustodiaDerivados();	
				
		if(entrada.p_TipoDerivado.equals("OP")) {			
			List<CustodiaDerivados> derivadosPosiciones = obtenerCarteraOpciones(con, entrada);				
			custodias.P_OUT_CARTERA.addAll(derivadosPosiciones);
		}		
		
		if(entrada.p_TipoDerivado.equals("FU")) {			
			List<CustodiaDerivadosFuturo> derivadosFuturo = ObtenerCarteraFuturo(con, entrada);	
			custodias.P_OUT_CARTERA_FUTURO.addAll(derivadosFuturo);
		}
		return custodias;
	}
	
		
	
	public static void main(String[] args) { 
		System.out.println("=== BEGIN TEST ===");
		try (Connection con =  PlataformaUtils.getDESAConnection()){
			Gson gson = Utils.getGsonBuilder();				
			
			String json = "{\r\n" + 
					"  \"p_IdEmpresa\": 1,\r\n" + 
					"  \"p_IdUsuario\": 6,\r\n" + 
					"  \"p_IdModulo\": \"CUS\",\r\n" + 					
					"  \"p_FechaConsulta\": \"2020-04-01\",\r\n" + 
					"  \"p_IdClasificacionTipoInstrumento\": \"\",\r\n" + 
					"  \"p_IdTipoInstrumento\": \"\",\r\n" + 
					"  \"p_IdInstrumento\": 0,\r\n" + 					
					"  \"p_IdCliente\": 123569,\r\n" +
					"  \"p_TipoDerivado\": \"FU\"\r\n" +
					//"  \"p_IdPortafolio\": 27499\r\n" + OP
					"}";
			
			
			Entrada_ConsultarCustodiaDerivados entrada = gson.fromJson(json, Entrada_ConsultarCustodiaDerivados.class);
			ConsultarCarteraDerivados demo = new ConsultarCarteraDerivados();
			long init = System.currentTimeMillis();
			Salida_ConsultarCustodiaDerivados salida = demo.logica(con, entrada);			
			System.out.println(String.format("TERMINO EN: %d", (System.currentTimeMillis()-init)/1000));			
			System.out.println(salida);			
			con.rollback();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("=== END TEST ===");
	}

}
