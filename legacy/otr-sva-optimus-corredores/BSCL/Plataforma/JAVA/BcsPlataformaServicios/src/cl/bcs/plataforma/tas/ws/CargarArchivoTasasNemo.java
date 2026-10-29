package cl.bcs.plataforma.tas.ws;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarArchivoTasasNemo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CargarArchivoTasasNemo.class);
	private final String SQL_OBTENER_FUENTE_ACTIVA = loadResourceToString("cl/bcs/plataforma/tas/sql/SolicitudGenerarTasas_SelectCodigoFuenteActiva.sql");
	private final String SQL_OBTENER_MERCADO_ACTIVO = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasasNemo_SelectMercadoActivo.sql");
	private final String SQL_OBTENER_TIPO_TASA_ACTIVA = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasasNemo_SelectTipoTasaActiva.sql");
	private final String SQL_OBTENER_INSTRUMENTOS_ACTIVOS_MERCADO = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasasNemo_SelectInstrumentosActivosPorMercado.sql");

	public static class EntradaCargarArchivoTasasNemo extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public String p_Fuente = "";
		public String p_Mercado;
		public String p_TipoRegistro;
		public String p_Oficializar;
		public List<Tasa> p_Tasas;
		
		public EntradaCargarArchivoTasasNemo() {
			this.p_Tasas = new ArrayList<Tasa>();
		}
		
	}
	
	public static class Tasa extends BcsPojo{
		public String Nemo;
		public BigDecimal Tasa;
	}
	
	public static class Instrumento extends BcsPojo{
		public String Nemotecnico;
	}
	
	public static class Resultado extends BcsPojo{
		public Long CantidadCorrectos;
		public Long CantidadIncorrectos;
		public List<Instrumento> NemotecnicosErroneos;
		
		public Resultado() {
			this.NemotecnicosErroneos = new ArrayList<Instrumento>();
		}
	}

	public static class SalidaCargarArchivoTasasNemo extends BcsPojo{
		public Long P_OUT_ID_PROGRAMA;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Resultado> P_OUT_RESULTADOS;
		
		public SalidaCargarArchivoTasasNemo() {
			this.P_OUT_ID_PROGRAMA = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADOS = new ArrayList<Resultado>();
		}
	}
	
	public static class CodigoFuente extends BcsPojo{
		public String Codigo;
		public String Descripcion;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCargarArchivoTasasNemo jin = gson.fromJson(in.toString(), EntradaCargarArchivoTasasNemo.class);
		SalidaCargarArchivoTasasNemo out = logica(conn,jin);
		out.P_OUT_ERRORESCONTADOR =  MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCargarArchivoTasasNemo logica(Connection con, EntradaCargarArchivoTasasNemo entrada) throws Exception {
		logger.info("INICIO::CargarArchivoTasasNemo.logica");
		long inicio = System.currentTimeMillis();
		
		SalidaCargarArchivoTasasNemo out = new SalidaCargarArchivoTasasNemo();
		
		entrada.p_Fuente = obtenerCodigoFuenteActiva(con, entrada);
		
		if(entrada.p_Fuente.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_FUENTE_NO_VALIDA",new HashMap<String,String>(0),con,entrada.p_IdEmpresa));
		}	
		
		if(!obtenerMercadoActivo(con, entrada)) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_MERCADO_NO_VALIDO",new HashMap<String,String>(0),con,entrada.p_IdEmpresa));
		}
		
		if(!obtenerTipoTasaActiva(con, entrada)) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_TIPO_REGISTRO_NO_VALIDO",new HashMap<String,String>(0),con,entrada.p_IdEmpresa));
		}
		
		if(MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;
		}
		
		entrada.p_Tasas = quitarNemosRepetidos(entrada.p_Tasas);
					
		List<Instrumento> nemosEncontrados = obtenerInstrumentosActivosPorMercado(con, entrada);
		
		List<Tasa> tasasCorrectasFiltradas = entrada.p_Tasas.stream().filter(tasa -> nemosEncontrados.stream()
                .anyMatch(nemo -> nemo.Nemotecnico.equals(tasa.Nemo))).collect(Collectors.toList());
		
		List<Instrumento> nemosNoEncontrados = entrada.p_Tasas.stream().filter(tasa -> nemosEncontrados.stream()
				.noneMatch(nemo -> tasa.Nemo.equals(nemo.Nemotecnico)))
				.map(tasa -> {
                	 Instrumento instrumento = new Instrumento();
                     instrumento.Nemotecnico = tasa.Nemo;
                     return instrumento;	
                }).collect(Collectors.toList());

		Resultado resultado = new Resultado();
		resultado.CantidadCorrectos = (long) tasasCorrectasFiltradas.size();
		resultado.CantidadIncorrectos= (long) nemosNoEncontrados.size();
		resultado.NemotecnicosErroneos.addAll(nemosNoEncontrados);
		out.P_OUT_RESULTADOS.add(resultado);
		
		if(resultado.CantidadCorrectos > 0) {
			entrada.p_Tasas = tasasCorrectasFiltradas;

			AgregarPrograma.OutAgregarPrograma salidaAgendarCarga = agendarCarga(con, entrada);
			
			if(salidaAgendarCarga.P_OUT_ERRORESCONTADOR > 0) {	
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AGENDAR_CARGA_TASAS", salidaAgendarCarga.P_OUT_MENSAJE));
				return out;
			}
			
			out.P_OUT_ID_PROGRAMA = salidaAgendarCarga.P_OUT_IDPROGRAMA;
			HashMap<String,String> parametros = new HashMap<String,String>(1);
			parametros.put("IdPrograma", String.valueOf(out.P_OUT_ID_PROGRAMA));
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CARGA_TASAS_AGENDADA_CORRECTAMENTE",parametros,con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargarArchivoTasasNemo.logica", fin - inicio);	
		
		return out;
	}
	
	private AgregarPrograma.OutAgregarPrograma agendarCarga(Connection con, EntradaCargarArchivoTasasNemo entrada) throws Exception {
		logger.trace("INICIO::CargarArchivoTasasNemo.agendarCarga");
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<AgregarPrograma.InAgregarPrograma.Parametro> paramsParametros = new ArrayList<AgregarPrograma.InAgregarPrograma.Parametro>();
		AgregarPrograma.InAgregarPrograma.Parametro	paramIdEmpresa = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramIdUsuario = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramIdModulo = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramUsaReglasNegocio = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramSimularOperacion = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramFuente = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramMercado = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramTipoRegistro = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramOficializar = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramTasas = new AgregarPrograma.InAgregarPrograma.Parametro();
		
		paramIdEmpresa.NombreParametro = "IdEmpresa";
		paramIdEmpresa.Valor = entrada.p_IdEmpresa.toString();
		paramsParametros.add(paramIdEmpresa);
		
		paramIdUsuario.NombreParametro = "IdUsuario";
		paramIdUsuario.Valor = entrada.p_IdUsuario.toString();
		paramsParametros.add(paramIdUsuario);
		
		paramIdModulo.NombreParametro = "IdModulo";
		paramIdModulo.Valor = entrada.p_IdModulo.toString();
		paramsParametros.add(paramIdModulo);
		
		paramUsaReglasNegocio.NombreParametro = "UsaReglasNegocio";
		paramUsaReglasNegocio.Valor = entrada.p_UsaReglasNegocio.toString();
		paramsParametros.add(paramUsaReglasNegocio);
		
		paramSimularOperacion.NombreParametro = "SimularOperacion";
		paramSimularOperacion.Valor = entrada.p_SimularOperacion.toString();
		paramsParametros.add(paramSimularOperacion);
		
		paramFuente.NombreParametro = "Fuente";
		paramFuente.Valor = entrada.p_Fuente.toString();
		paramsParametros.add(paramFuente);
		
		paramMercado.NombreParametro = "Mercado";
		paramMercado.Valor = entrada.p_Mercado.toString();
		paramsParametros.add(paramMercado);
		
		paramTipoRegistro.NombreParametro = "TipoRegistro";
		paramTipoRegistro.Valor = entrada.p_TipoRegistro.toString();
		paramsParametros.add(paramTipoRegistro);
		
		paramOficializar.NombreParametro = "Oficializar";
		paramOficializar.Valor = entrada.p_Oficializar.toString();
		paramsParametros.add(paramOficializar);
		
		paramTasas.NombreParametro = "Tasas";
		paramTasas.Valor = "";
		paramTasas.ValorBlob = entrada.toString();
		paramsParametros.add(paramTasas);
		
		AgregarPrograma.InAgregarPrograma paramsAgregarPrograma = new AgregarPrograma.InAgregarPrograma(); 
		paramsAgregarPrograma.p_IdEmpresa = entrada.p_IdEmpresa;
		paramsAgregarPrograma.p_IdModulo = entrada.p_IdModulo;
		paramsAgregarPrograma.p_IdUsuario = entrada.p_IdUsuario;
		paramsAgregarPrograma.p_IdTipoPrograma	= "CARGA_ARCHIVO_TASAS_NEMO";
		paramsAgregarPrograma.p_Segundos = 0L;
		paramsAgregarPrograma.p_FechaSistema = fechaSistema;
		paramsAgregarPrograma.p_Parametros = paramsParametros;
		AgregarPrograma agregarPrograma = new AgregarPrograma();
		AgregarPrograma.OutAgregarPrograma salidaAgregarPrograma = agregarPrograma.logica(con, paramsAgregarPrograma);
		
		logger.trace("FIN::CargarArchivoTasasNemo.agendarCarga");
		return salidaAgregarPrograma;
	}
	
	private List<Tasa> quitarNemosRepetidos(List<Tasa> tasas){
		logger.info("INICIO::CargarArchivoTasasNemo.quitarNemosRepetidos");
		long inicio = System.currentTimeMillis();
		
		List<Tasa> listaSinRepetidos = new ArrayList<Tasa>();
		
		listaSinRepetidos = tasas.stream()
                .collect(Collectors.groupingBy(tasa -> tasa.Nemo, LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .map(subList -> subList.get(subList.size() - 1))
                .collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargarArchivoTasasNemo.quitarNemosRepetidos", fin - inicio);
		
		return listaSinRepetidos;
	}
	
	private String obtenerCodigoFuenteActiva(Connection con, EntradaCargarArchivoTasasNemo entrada) throws Exception{
		logger.info("INICIO::CargarArchivoTasasNemo.obtenerCodigoFuenteActiva");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>(2);
		
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("Fuente", entrada.p_Fuente);
		
		List<CodigoFuente> codigos = sql.query(con, SQL_OBTENER_FUENTE_ACTIVA, params, CodigoFuente.class);
		
		String descripcion = codigos.parallelStream().findFirst().map(x -> x.Descripcion).orElse("");
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargarArchivoTasasNemo.obtenerCodigoFuenteActiva", fin - inicio);
		
		return descripcion;
	}
	
	private boolean obtenerMercadoActivo(Connection con, EntradaCargarArchivoTasasNemo entrada) throws Exception{
		logger.info("INICIO::CargarArchivoTasasNemo.obtenerMercadoActivo");
		long inicio = System.currentTimeMillis();
		
		boolean existe = false;
		SQL sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>(1);
		params.put("Mercado", entrada.p_Mercado);
		
		Long contadorMercado = sql.countQuery(con, SQL_OBTENER_MERCADO_ACTIVO, params);
		
		existe = contadorMercado.equals(0L) ? false: true;
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargarArchivoTasasNemo.obtenerMercadoActivo", fin - inicio);
		
		return existe;
	}
	
	private boolean obtenerTipoTasaActiva(Connection con, EntradaCargarArchivoTasasNemo entrada) throws Exception{
		logger.info("INICIO::CargarArchivoTasasNemo.obtenerTipoTasaActiva");
		long inicio = System.currentTimeMillis();
		
		boolean existe = false;
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>(2);
		
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("TipoRegistro", entrada.p_TipoRegistro);
		
		Long contadorTipoTasa = sql.countQuery(con, SQL_OBTENER_TIPO_TASA_ACTIVA, params);
		
		existe = contadorTipoTasa.equals(0L) ? false : true;
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargarArchivoTasasNemo.obtenerTipoTasaActiva", fin - inicio);
		
		return existe;
	}
	
	private List<Instrumento> obtenerInstrumentosActivosPorMercado(Connection con, EntradaCargarArchivoTasasNemo entrada) throws Exception{
		logger.info("INICIO::CargarArchivoTasasNemo.obtenerInstrumentosActivosPorMercado");
		long inicio = System.currentTimeMillis();
		
		List<Instrumento> nemosEncontrados = new ArrayList<Instrumento>();
		
		SQL sql = new SQL();
		
		List<String> nemos = entrada.p_Tasas.parallelStream().map(x -> x.Nemo).collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("Mercado", entrada.p_Mercado);
		params.put("Nemo", nemos);
		
		nemosEncontrados = sql.query(con, SQL_OBTENER_INSTRUMENTOS_ACTIVOS_MERCADO, params, Instrumento.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargarArchivoTasasNemo.obtenerInstrumentosActivosPorMercado", fin - inicio);
		
		return nemosEncontrados;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa","connections.properties")) {
		Gson gson = Utils.getGsonBuilder();			
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"S\",\"p_SimularOperacion\":\"N\",\"p_Fuente\":\"RISK - TASASMERCADO\",\"p_Mercado\":\"RF\",\"p_TipoRegistro\": \"TASA\",\"p_Oficializar\": \"N\",\"p_Tasas\": [{\"Nemo\":\"BACEN-A1\",\"Tasa\":11},{\"Nemo\":\"BACEN-A1\",\"Tasa\":12},{\"Nemo\":\"BACEN-A1\",\"Tasa\":13},{\"Nemo\":\"BACEN-A1\",\"Tasa\":14}]}";
		
		EntradaCargarArchivoTasasNemo Objin = gson.fromJson(json, EntradaCargarArchivoTasasNemo.class);
		CargarArchivoTasasNemo demo = new CargarArchivoTasasNemo();				
		try {
			SalidaCargarArchivoTasasNemo ObjOut = demo.logica(con, Objin);
			//con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	};
		System.out.println("== END ==");
	}

}
