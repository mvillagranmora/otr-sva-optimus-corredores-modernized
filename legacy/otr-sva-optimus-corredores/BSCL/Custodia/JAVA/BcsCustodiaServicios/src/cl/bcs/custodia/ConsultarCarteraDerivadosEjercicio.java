package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarCarteraDerivadosEjercicio implements JavaCall{
	private final static Logger logger = LogManager.getLogger(ConsultarCarteraDerivadosEjercicio.class);
	private final String SELECT_EJERCICIOS = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivadosEjercicio_ObtenerEjercicios.sql");
	private final String SELECT_EJERCICIOS_BY_PORTAFOLIO = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivadosEjercicio_ObtenerEjerciciosByPortafolio.sql");
	private final String SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivadosEjercicio_ObtenerInstrumentos.sql");
	private final String SELECT_INSTRUMENTOS_BY_PORTAFOLIO = loadResourceToString("cl/bcs/custodia/sql/ConsultarCarteraDerivadosEjercicio_ObtenerInstrumentosByPortafolio.sql");
	
	public static class Entrada_ConsultarEjercicio{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public Long p_IdPais;
		public String p_IdModulo;
		public String p_TipoFecha;
		public java.sql.Date p_FechaConsulta;
		public String p_IdClasificacionTipoInstrumento;
		public String p_IdTipoInstrumento;
		public Integer p_IdInstrumento;
		public Integer p_FolioCartera;
		public String p_IdMoneda;
		public Integer p_IdProductoAgrupador;
		public Integer p_IdCliente;
		public String p_TipoDerivado;
		public Long p_IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class ConsultarEjercicio{
		public Long IdCartera;
		public String DocumentoIdentidad;
		public String SocioNegocio;
		public String CodigoPortafolio;
		public Long IdInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String Serie;
		public String Subyacente;
		public String TipoOpcion;
		public String Estilo;
		public String ModalidadLiquidacion;
		public String ModalidadEjercicio;
		public Date FechaVencimiento;
		public BigDecimal PrecioEjercicio;
		public BigDecimal PrecioSubyacente;
		public String Moneyness;
		public BigDecimal SaldoFinanciero;
		public BigDecimal ContratosEjercibles;
		public Long ContratosEjercidos;	
		public String Bloqueado;
		public Boolean Editado;
		public String EstadoIntencion;
		public String IdEstado;
		public String RazonSocial; 
		public Long IdPortafolio;
		public Long IdCliente;
		public Long FolioCartera;
		public Long Tamano;
		
		public ConsultarEjercicio() {		
			
			Bloqueado = "NO";
			Editado = false;
			EstadoIntencion = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class Instrumento{		
		public Long IdInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String Nemotecnico;
		public Long IdSubyacente;
		public String NemotecnicoSubyacente;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	
	public static class Salida_ConsultarEjercicio{
		public List<ConsultarEjercicio> P_OUT_EJERCICIOS;
		//public List<ConsultarEjercicio> P_OUT_EJERCICIO_AUTOMATICO;	
		
		public Salida_ConsultarEjercicio() {
			P_OUT_EJERCICIOS = new ArrayList<ConsultarEjercicio>();
			//P_OUT_EJERCICIO_AUTOMATICO = new ArrayList<ConsultarEjercicio>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	private String calculcarMoneyness(ConsultarEjercicio ejercicio) {
		String moneyness = "";
		BigDecimal precio = new BigDecimal(0);
		BigDecimal zero = new BigDecimal(0);
		if(ejercicio.TipoOpcion.contentEquals("CALL")){
			precio = ejercicio.PrecioSubyacente.subtract(ejercicio.PrecioEjercicio);
			if(precio.compareTo(zero) == 0) // =
				moneyness = "AT THE MONEY";
			if(precio.compareTo(zero) == 1) // >
				moneyness = "IN THE MONEY";
			if(precio.compareTo(zero) == -1) // <
				moneyness = "OUT OF THE MONEY";
		}
		if(ejercicio.TipoOpcion.contentEquals("PUT")){
			precio = ejercicio.PrecioEjercicio.subtract(ejercicio.PrecioSubyacente);
			if(precio.compareTo(zero) == 0) // =
				moneyness = "AT THE MONEY";
			if(precio.compareTo(zero) == 1) // >
				moneyness = "IN THE MONEY";
			if(precio.compareTo(zero) == -1) // <
				moneyness = "OUT OF THE MONEY";
		}
		return moneyness;
	}
	
	private List<ConsultarEjercicio> obtenerEjercicios(Connection con, Entrada_ConsultarEjercicio entrada, String modalidad) throws Exception{
		List<ConsultarEjercicio> ejercicios = new ArrayList<ConsultarEjercicio>();			
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();		
		params.put("p_FechaConsulta", entrada.p_FechaConsulta);
		if(modalidad.contentEquals("A"))
			params.put("p_FechaActual", entrada.p_FechaConsulta);
		else if(modalidad.contentEquals("M"))
			params.put("p_FechaActual", ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema"));
		params.put("p_Modalidad", modalidad);		
		if(entrada.p_IdPortafolio != null) {
			params.put("p_IdPortafolio", entrada.p_IdPortafolio);
			ejercicios = sql.query(con, SELECT_EJERCICIOS_BY_PORTAFOLIO, params, ConsultarEjercicio.class);
		}
		else if(entrada.p_IdPortafolio == null) {
			ejercicios = sql.query(con, SELECT_EJERCICIOS, params, ConsultarEjercicio.class);
		}		
		return ejercicios;
	}
	
	private List<Instrumento> obtenerInstrumentos(Connection con, Entrada_ConsultarEjercicio entrada) throws SQLException{
		List<Instrumento> instrumentos = new ArrayList<Instrumento>();	
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();		
		params.put("p_FechaConsulta", entrada.p_FechaConsulta);
		if(entrada.p_IdPortafolio != null) {
			params.put("p_IdPortafolio", entrada.p_IdPortafolio);
			instrumentos = sql.query(con, SELECT_INSTRUMENTOS_BY_PORTAFOLIO, params, Instrumento.class);
		}
		else if(entrada.p_IdPortafolio == null) {
			instrumentos = sql.query(con, SELECT_INSTRUMENTOS, params, Instrumento.class);
		}	
		return instrumentos;
	}
	
	private List<EntregarPreciosTasas_Salida_PreciosTasas> obtenerPrecioSubyacente(Connection con, Entrada_ConsultarEjercicio entrada, List<Instrumento> instrumentos) throws Exception {
		EntregarPreciosTasas entregarPreciosTasas = new EntregarPreciosTasas();
		EntregarPreciosTasas_Entrada entradaEntregarPreciosTasas = new EntregarPreciosTasas_Entrada();
		entradaEntregarPreciosTasas.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEntregarPreciosTasas.p_IdUsuario = entrada.p_IdUsuario;
		entradaEntregarPreciosTasas.p_IdModulo = "PLA";
		entradaEntregarPreciosTasas.p_IdPais = entrada.p_IdPais;
		//entradaEntregarPreciosTasas.
		
		for(Instrumento instrumento: instrumentos) {
			EntregarPreciosTasas_Entrada_DatosInstrumentos datosInstrumento = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
			datosInstrumento.FechaConsulta = entrada.p_FechaConsulta;
			datosInstrumento.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento;
			datosInstrumento.IdInstrumento = instrumento.IdSubyacente;
			datosInstrumento.IdTipoInstrumento = instrumento.IdTipoInstrumento;
			datosInstrumento.Nemotecnico = instrumento.NemotecnicoSubyacente;
			datosInstrumento.TipoCartera = "TERCEROS";
			datosInstrumento.IdUso = "SUBYACENTE";
			datosInstrumento.IdTipoCriterioTasa = "NORMATIVO";
			entradaEntregarPreciosTasas.p_DatosInstrumentos.add(datosInstrumento);
		}
		EntregarPreciosTasas_Salida salidaEntregarPreciosTasas = entregarPreciosTasas.logica(con, entradaEntregarPreciosTasas);		
		return salidaEntregarPreciosTasas.P_OUT_PRECIOSTASAS;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		Entrada_ConsultarEjercicio in=gson.fromJson(jin.toString(),Entrada_ConsultarEjercicio.class);
		Salida_ConsultarEjercicio out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/* ObtenerPrecioBase para ACN, MILA, para Dolar, indicador TipoCambioIndice */
	public Salida_ConsultarEjercicio logica(Connection con, Entrada_ConsultarEjercicio entrada) throws Exception{
		logger.debug("INICIO::ConsultarEjercicio");
		long inicioServicio = System.currentTimeMillis();
		Salida_ConsultarEjercicio salidaEjercicios = new Salida_ConsultarEjercicio();		
		List<ConsultarEjercicio> ejerciciosAutomaticos = obtenerEjercicios(con, entrada, "A");		
		List<ConsultarEjercicio> ejerciciosManuales = obtenerEjercicios(con, entrada, "M");
		List<ConsultarEjercicio> ejercicios = new ArrayList<ConsultarEjercicio>();
		
		ejercicios.addAll(ejerciciosAutomaticos);
		ejercicios.addAll(ejerciciosManuales);
		List<Instrumento> instrumentos = obtenerInstrumentos(con, entrada);
		List<EntregarPreciosTasas_Salida_PreciosTasas> preciosSubyacentes = obtenerPrecioSubyacente(con, entrada, instrumentos);		
		
		for(ConsultarEjercicio ejercicio: ejercicios) {
			
			EntregarPreciosTasas_Salida_PreciosTasas precioSubyacente = preciosSubyacentes.stream()
																.filter(preciosSubyacente -> ejercicio.Subyacente.equals(preciosSubyacente.Nemotecnico))
																.findFirst()
																.orElse(null);
			ejercicio.PrecioSubyacente = precioSubyacente != null && precioSubyacente.Precio != null ? precioSubyacente.Precio : BigDecimal.ZERO;			
			ejercicio.Moneyness = calculcarMoneyness(ejercicio);
			if(ejercicio.ModalidadLiquidacion.equals("FISICA")) {
				ejercicio.SaldoFinanciero = BigDecimal.ZERO;
			}
			else if(ejercicio.ModalidadLiquidacion.equals("FINANCIERA") && ejercicio.ModalidadEjercicio.equals("AUTOMATICA") && ejercicio.Moneyness.equals("IN THE MONEY")) {
				ejercicio.ContratosEjercidos = ejercicio.ContratosEjercibles.longValue();
				ejercicio.SaldoFinanciero = ejercicio.PrecioEjercicio.subtract(ejercicio.PrecioSubyacente).multiply(new BigDecimal(ejercicio.ContratosEjercidos));
			}
			else {
				ejercicio.SaldoFinanciero = BigDecimal.ZERO;
			}		
		}
		salidaEjercicios.P_OUT_EJERCICIOS = ejercicios;
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarEjercicio", finServicio - inicioServicio);
		return salidaEjercicios;
		
	}
	
	public static void main(String[] args) { 
		System.out.println("=== BEGIN TEST ===");
		
		try (Connection con =  PlataformaUtils.getDESAConnection()){
			Gson gson = Utils.getGsonBuilder();			
			String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"CUS\",\"p_FechaConsulta\":\"2020-02-26\",\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_TipoDerivado\":\"OP\"}";
/*			String json = "{\r\n" + 
					"  \"p_IdEmpresa\": 1,\r\n" + 
					"  \"p_IdUsuario\": 21,\r\n" + 
					"  \"p_IdPais\": 44,\r\n" + 
					"  \"p_IdModulo\": \"CUS\",\r\n" + 
					"  \"p_tipoFecha\": \"ADQ\",\r\n" + 
					"  \"p_FechaConsulta\": \"2020-02-26\",\r\n" + 
					"  \"p_IdClasificacionTipoInstrumento\": \"\",\r\n" + 
					"  \"p_IdTipoInstrumento\": \"\",\r\n" + 
					"  \"p_IdInstrumento\": 0,\r\n" + 
					"  \"p_FolioCartera\": 0,\r\n" + 
					"  \"p_IdMoneda\": \"\",\r\n" + 
					"  \"p_IdEstado\": \"\",\r\n" + 
					"  \"p_IdProductoAgrupador\": 0,\r\n" + 
					"  \"p_IdCliente\": 67760,\r\n" +
					"  \"p_TipoDerivado\": \"OP\",\r\n" +
					"  \"p_IdPortafolio\": 412766\r\n" +
					"}";*/
			
			Entrada_ConsultarEjercicio entrada = gson.fromJson(json, Entrada_ConsultarEjercicio.class);
			ConsultarCarteraDerivadosEjercicio demo = new ConsultarCarteraDerivadosEjercicio();
			long init = System.currentTimeMillis();
			Salida_ConsultarEjercicio salida = demo.logica(con, entrada);			
			System.out.println(String.format("TERMINO EN: %d", (System.currentTimeMillis()-init)/1000));			
			System.out.println(salida);			
			con.rollback();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("=== END TEST ===");
	}

}
