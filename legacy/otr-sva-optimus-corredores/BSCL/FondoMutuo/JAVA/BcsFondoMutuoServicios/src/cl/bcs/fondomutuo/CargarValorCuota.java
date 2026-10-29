package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarValorCuota implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CargarValorCuota.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_SERIE_FONDO_ISIN = loadResourceToString("cl/bcs/fondomutuo/sql/CargarValorCuota_SelectSerieFondoISIN.sql");
	private final String SQL_SELECT_VALOR_CUOTA_EXISTENTE_FECHA = loadResourceToString("cl/bcs/fondomutuo/sql/CargarValorCuota_SelectValorCuotaExistenteFecha.sql");
	private final String SQL_SELECT_VALOR_CUOTA_DIA_ANTERIOR_FECHA_CARGA = loadResourceToString("cl/bcs/fondomutuo/sql/CargarValorCuota_SelectValorCuotaDiaAnteriorFechaCarga.sql");
	private final String SQL_UPDATE_VALOR_CUOTA_ULTIMA_CARGA = loadResourceToString("cl/bcs/fondomutuo/sql/CargarValorCuota_UpdateValorCuotaUltimaCarga.sql");
	private final String SQL_INSERT_CARGA_VALOR_CUOTA_HISTORICO = loadResourceToString("cl/bcs/fondomutuo/sql/CargarValorCuota_InsertValorCuotaHistorico.sql");
	private final String SQL_DELETE_VALOR_CUOTA = loadResourceToString("cl/bcs/fondomutuo/sql/CargarValorCuota_DeleteValorCuota.sql");
	private final String SQL_INSERT_VALOR_CUOTA = loadResourceToString("cl/bcs/fondomutuo/sql/CargarValorCuota_InsertValorCuota.sql");
	
	public static class EntradaCargarValorCuota extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public Long	p_IdPais = -1L;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public String p_AllFunds;
		public List<ValorCuota> p_ValorCuota;
		
		public EntradaCargarValorCuota() {
			this.p_ValorCuota = new ArrayList<ValorCuota>();
		}
		
	}
	
	public static class ValorCuota extends BcsPojo{
		public String IDSERIEFONDO;
		public String ISIN;
		public String IDMONEDA;
		public String FECHACIERRE;
		public BigDecimal VALORCUOTA;
		public String OBSERVACION;
		public String MENSAJEVALIDACION;
		public String FECHACALCULO;
		public Long REGISTRO;
		
		public ValorCuota() {
			this.FECHACALCULO = "";
			this.ISIN = "";
			this.REGISTRO = 0L;
		}

	}
	
	public static class ResultadoCargaValorCuota extends BcsPojo{
		public String IdSerieFondo;
		public String IdMoneda;
		public String ISIN;
		public Date FechaVigencia;
		public BigDecimal ValorCuota;
		public BigDecimal ValorCuotaFechaAnterior;
		public String Observacion;
		public Long DecimalesValorCuota;
		public Date FechaCalculo;
		public String Estado;
		public String DescEstado;
		public Long IdEmpresa;
		public String EmisorValorCuota;
		public BigDecimal ParidadValorCuota;
		public BigDecimal ValorCuotaCalculado;
		public BigDecimal VariacionValorCuota;
		
		public ResultadoCargaValorCuota() {
			this.ValorCuota = BigDecimal.ZERO;
			this.ValorCuotaFechaAnterior = BigDecimal.ZERO;
			this.ParidadValorCuota = BigDecimal.ONE;
			this.ValorCuotaCalculado = BigDecimal.ZERO;
			this.VariacionValorCuota = BigDecimal.ZERO;
			this.EmisorValorCuota = null;
		}		
	}
	
	public static class EstadoCargaValorCuota extends BcsPojo{
		public String Estado;
		public String DescEstado;
	}
	
	public static class SerieFondoISIN extends BcsPojo{
		public String IdSerieFondo;
		public String ISIN;
		
		public SerieFondoISIN() {
			this.IdSerieFondo = "";
			this.ISIN = "";
		}
	}
	
	public static class ValorCuotaVigente extends BcsPojo{
		public String IdSerieFondo;
		public Date FechaVigencia;
		public BigDecimal ValorCuotaVigente;
		
		public ValorCuotaVigente() {
			ValorCuotaVigente = BigDecimal.ZERO;
		}

		@Override
		public int hashCode() {
			return 31 * this.IdSerieFondo.hashCode() + this.FechaVigencia.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
			    return false;
			if (getClass() != obj.getClass())
			    return false;
			ValorCuotaVigente valorCuotaVigente = (ValorCuotaVigente) obj;
			return this.IdSerieFondo.equals(valorCuotaVigente.IdSerieFondo) && this.FechaVigencia.equals(valorCuotaVigente.FechaVigencia);
		}
	}
	
	public static class SalidaCargarValorCuota extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<ResultadoCargaValorCuota> P_OUT_RESULTADO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCargarValorCuota() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCargaValorCuota>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCargarValorCuota jin = gson.fromJson(in.toString(), EntradaCargarValorCuota.class);
		SalidaCargarValorCuota out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCargarValorCuota logica(Connection con, EntradaCargarValorCuota entrada) throws Exception{
		logger.info("INICIO::CargarValorCuota");
		SalidaCargarValorCuota out = new SalidaCargarValorCuota();
		
		out = validaRequisitosBasicos(con, entrada, out);
		out = validaCantidadValoresCuota(con, entrada, out);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		//Conversion ISIN a IdSerieFondo
		if("S".equalsIgnoreCase(entrada.p_AllFunds)) {
			SQL sql = new SQL();
			List<String> seriesEntrada = entrada.p_ValorCuota.stream().map(valorCuota->valorCuota.IDSERIEFONDO).distinct().collect(Collectors.toList());
			HashMap<String,Object> parametrosISIN = new HashMap<String,Object>();
			parametrosISIN.put("ISIN", seriesEntrada);
			List<SerieFondoISIN> FondosConISIN = sql.query(con, SQL_SELECT_SERIE_FONDO_ISIN, parametrosISIN, SerieFondoISIN.class);
			
			List<ValorCuota> newValorCuota = new ArrayList<>();
			Map<String,ValorCuota> valoresCuotaAgrupadosPorISIN = entrada.p_ValorCuota.stream().collect(Collectors.toMap(valorcuota -> valorcuota.IDSERIEFONDO, valorcuota -> valorcuota));
			
			for(SerieFondoISIN fondoISIN : FondosConISIN ) {
				ValorCuota valorCuotaFondo = new ValorCuota();
				
				ValorCuota valorCuotaBusquedaPorISIN = valoresCuotaAgrupadosPorISIN.get(fondoISIN.ISIN);
				
				valorCuotaFondo.FECHACALCULO = valorCuotaBusquedaPorISIN.FECHACALCULO;
				valorCuotaFondo.FECHACIERRE = valorCuotaBusquedaPorISIN.FECHACIERRE;
				valorCuotaFondo.IDMONEDA = valorCuotaBusquedaPorISIN.IDMONEDA;
				valorCuotaFondo.IDSERIEFONDO = fondoISIN.IdSerieFondo;
				valorCuotaFondo.ISIN = fondoISIN.ISIN;
				valorCuotaFondo.MENSAJEVALIDACION = valorCuotaBusquedaPorISIN.MENSAJEVALIDACION;
				valorCuotaFondo.OBSERVACION = valorCuotaBusquedaPorISIN.OBSERVACION;
				valorCuotaFondo.REGISTRO = valorCuotaBusquedaPorISIN.REGISTRO;
				valorCuotaFondo.VALORCUOTA = valorCuotaBusquedaPorISIN.VALORCUOTA;
				
				newValorCuota.add(valorCuotaFondo);
				
			}
			
			entrada.p_ValorCuota = newValorCuota;
		}

		out.P_OUT_RESULTADO = obtenerResultado(con, entrada);
		
		if(_c.NO.equals(entrada.p_SimularOperacion)) {
			List<ResultadoCargaValorCuota> valoresCuotaHistoricoInsertar = obtenerValoresCuotaHistoricosInsertar(out.P_OUT_RESULTADO);
			List<ResultadoCargaValorCuota> valoresCuotaInsertar = obtenerValoresCuotaInsertar(out.P_OUT_RESULTADO);
			actualizarUltimaCarga(con, valoresCuotaHistoricoInsertar);
			insertarValorCuotaHistorico(con, valoresCuotaHistoricoInsertar);
			eliminarValoresCuotas(con, valoresCuotaInsertar);
			insertarValorCuota(con, valoresCuotaInsertar);
			agregarLog(con, entrada, valoresCuotaHistoricoInsertar);
			
		}
		
		out.P_OUT_MENSAJES = obtenerMensajesSalida(con, out.P_OUT_RESULTADO);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		logger.info("FIN::CargarValorCuota");
		return out;
	}
	
	private SalidaCargarValorCuota validaRequisitosBasicos(Connection con, EntradaCargarValorCuota entrada, SalidaCargarValorCuota out) throws Exception {
		logger.trace("INICIO::CargarValorCuota.validaRequisitosBasicos");
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida validacionRequisitosBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(validacionRequisitosBasicos.p_out_Mensajes);
		if (out.P_OUT_ERRORESCONTADOR > 0) {
			out.P_OUT_MENSAJES.addAll(validacionRequisitosBasicos.p_out_Mensajes);
		}
		logger.trace("FIN::CargarValorCuota.validaRequisitosBasicos");
		return out;
	}
	
	private SalidaCargarValorCuota validaCantidadValoresCuota(Connection con, EntradaCargarValorCuota entrada, SalidaCargarValorCuota out) throws Exception {
		logger.trace("INICIO::CargarValorCuota.validaCantidadValoresCuota");
		if(entrada.p_ValorCuota.size() == 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_VALORES_CUOTA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		logger.trace("FIN::CargarValorCuota.validaCantidadValoresCuota");
		return out;
	}
	
	private List<ResultadoCargaValorCuota> obtenerResultado(Connection con, EntradaCargarValorCuota entrada) throws Exception{
		logger.trace("INICIO::CargarValorCuota.obtenerResultado");
		String fechaSistema = obtenerFechaSistema(con, entrada);
		Map<ValorCuotaVigente, Object> seriesDuplicadas = obtenerSeriesDuplicadas(entrada);
		Map<String, SerieFondoSolicitudes> seriesFondos = obtenerSeriesFondos(con, entrada);
		Map<String,ValorCuotaVigente> valoresCuotaExistentes = obtenerValorCuotaVigenteFecha(con, entrada);
		Map<String,ValorCuotaVigente> valoresCuotaExistenteDiaAnterior = obtenerValorCuotaVigenteFechaAnterior(con, entrada);
		
		
		List<ResultadoCargaValorCuota> resultados = entrada.p_ValorCuota.stream().map(valorCuota->{
			EstadoCargaValorCuota estado = obtenerEstadoCargaValorCuota(valorCuota, seriesDuplicadas, seriesFondos, valoresCuotaExistentes, fechaSistema, entrada.p_AllFunds);
			Long decimalesValorCuota = obtenerDecimalesValorCuota(valorCuota,seriesFondos);
			ResultadoCargaValorCuota resultado = new ResultadoCargaValorCuota();
			resultado.IdSerieFondo = valorCuota.IDSERIEFONDO;
			resultado.ISIN = valorCuota.ISIN;
			resultado.IdMoneda = valorCuota.IDMONEDA;
			resultado.FechaVigencia = obtenerFechaCierre(valorCuota, estado);
			resultado.Observacion = valorCuota.OBSERVACION;
			resultado.DecimalesValorCuota = decimalesValorCuota;
			resultado.ValorCuota = obtenerValorCuota(resultado.ValorCuota, decimalesValorCuota, estado);
			resultado.FechaCalculo = Fecha.toDate(fechaSistema);
			resultado.Estado = estado.Estado;
			resultado.DescEstado = estado.DescEstado;
			resultado.ValorCuota = valorCuota.VALORCUOTA;
			resultado.ValorCuotaFechaAnterior = valoresCuotaExistenteDiaAnterior.containsKey(valorCuota.IDSERIEFONDO) ? valoresCuotaExistenteDiaAnterior.get(valorCuota.IDSERIEFONDO).ValorCuotaVigente : BigDecimal.ZERO;
			resultado.IdEmpresa = entrada.p_IdEmpresa;
			resultado.EmisorValorCuota = obtenerEmisor(valorCuota,seriesFondos, estado);
			if("S".equalsIgnoreCase(entrada.p_AllFunds)) {
				resultado.FechaCalculo = obtenerFechaCalculoAllFunds(valorCuota, estado);
			}
			if(resultado.ValorCuota.compareTo(BigDecimal.ZERO) != 0 ) {
				resultado.ValorCuotaCalculado = resultado.ValorCuota;
				if(resultado.ValorCuotaFechaAnterior.compareTo(BigDecimal.ZERO) != 0) {
					resultado.VariacionValorCuota = (((resultado.ValorCuota.multiply(resultado.ParidadValorCuota))
														.subtract(resultado.ValorCuotaFechaAnterior))
														.divide(resultado.ValorCuotaFechaAnterior, 3, RoundingMode.HALF_EVEN)
														).multiply(new BigDecimal(100)).abs();
				}	
			}
			return resultado;
		}).collect(Collectors.toList());
		
		logger.trace("FIN::CargarValorCuota.obtenerResultado");
		return resultados;
	}
	
	private String obtenerFechaSistema(Connection con, EntradaCargarValorCuota entrada) throws Exception {
		logger.trace("INICIO::CargarValorCuota.obtenerFechaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		logger.trace("FIN::CargarValorCuota.obtenerFechaSistema");
		return fechaSistema;
	}
	
	private Map<ValorCuotaVigente, Object> obtenerSeriesDuplicadas(EntradaCargarValorCuota entrada){
		logger.trace("INICIO::CargarValorCuota.obtenerSeriesDuplicadas");
		Map<ValorCuotaVigente, Object> seriesDuplicadas = new HashMap<ValorCuotaVigente, Object>();
		Map<ValorCuotaVigente, Boolean> seriesProcesadas = new HashMap<ValorCuotaVigente, Boolean>();
		for(ValorCuota valorCuota : entrada.p_ValorCuota) {
			ValorCuotaVigente valorCuotaEntrada = new ValorCuotaVigente();
			valorCuotaEntrada.IdSerieFondo = valorCuota.IDSERIEFONDO;
			valorCuotaEntrada.FechaVigencia = Fecha.toDate(valorCuota.FECHACIERRE);
			if(seriesProcesadas.get(valorCuotaEntrada) != null) {
				seriesDuplicadas.put(valorCuotaEntrada, new Object());
			}
			seriesProcesadas.put(valorCuotaEntrada, true);
		}
		logger.trace("FIN::CargarValorCuota.obtenerSeriesDuplicadas");
		return seriesDuplicadas;
	}
	
	private Map<String, SerieFondoSolicitudes> obtenerSeriesFondos(Connection con, EntradaCargarValorCuota entrada) throws Exception{
		logger.trace("INICIO::CargarValorCuota.obtenerSerieFondos");
		SQL sql = new SQL();
		List<String> seriesEntrada = entrada.p_ValorCuota.stream().map(valorCuota->valorCuota.IDSERIEFONDO).distinct().collect(Collectors.toList());
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdEmpresa", entrada.p_IdEmpresa);
		parametros.put("IdSerieFondo", seriesEntrada);
		List<SerieFondoSolicitudes> seriesAlmacenadas = sql.query(con, SQL_SELECT_SERIE_FONDO, parametros, SerieFondoSolicitudes.class);
		Map<String, SerieFondoSolicitudes> seriesFondo = seriesAlmacenadas.stream().collect(Collectors.toMap(serieFondo->serieFondo.IdSerieFondo, serieFondo->serieFondo));
		logger.trace("FIN::CargarValorCuota.obtenerSerieFondos");
		return seriesFondo;
	}
	
	private Map<String,ValorCuotaVigente> obtenerValorCuotaVigenteFecha(Connection con, EntradaCargarValorCuota entrada) throws Exception{
		logger.trace("INICIO::CargarValorCuota.obtenerValorCuotaVigenteFecha");
		SQL sql = new SQL();
		List<String> seriesEntrada = entrada.p_ValorCuota.stream().map(valorCuota->valorCuota.IDSERIEFONDO).distinct().collect(Collectors.toList());
		List<String> fechasVigenciaEntrada = entrada.p_ValorCuota.stream().map(valorCuota->valorCuota.FECHACIERRE).distinct().collect(Collectors.toList());
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdSerieFondo", seriesEntrada);
		parametros.put("FechaVigencia", fechasVigenciaEntrada);
		List<ValorCuotaVigente> valoresCuotasVigentesAlmacenadas = sql.query(con, SQL_SELECT_VALOR_CUOTA_EXISTENTE_FECHA, parametros, ValorCuotaVigente.class);
		Map<String, ValorCuotaVigente> valoresCuotasVigentesFecha = valoresCuotasVigentesAlmacenadas.stream().collect(Collectors.toMap(valorCuota->valorCuota.IdSerieFondo + "_" + valorCuota.FechaVigencia, valorCuota->valorCuota));
		logger.trace("FIN::CargarValorCuota.obtenerValorCuotaVigenteFecha");
		return valoresCuotasVigentesFecha;
	}
	
	private Map<String,ValorCuotaVigente> obtenerValorCuotaVigenteFechaAnterior(Connection con, EntradaCargarValorCuota entrada) throws Exception{
 		logger.trace("INICIO::CargarValorCuota.obtenerValorCuotaVigenteFechaAnterior");
		SQL sql = new SQL();
		List<String> seriesEntrada = entrada.p_ValorCuota.stream().map(valorCuota->valorCuota.IDSERIEFONDO).distinct().collect(Collectors.toList());
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		List<InObtenerDiaAnterior.FechasIn> FechasDAValorCuota = new ArrayList<InObtenerDiaAnterior.FechasIn>();
		InObtenerDiaAnterior.FechasIn FDA = new InObtenerDiaAnterior.FechasIn();
		for(ValorCuota fecha : entrada.p_ValorCuota) {
			FDA.fechaInicial = fecha.FECHACIERRE;
			FDA.numeroDias	= 1L;
			FechasDAValorCuota.add(FDA);

		}
		
		InObtenerDiaAnterior ObtenerDiaAnterior = new InObtenerDiaAnterior();
		ObtenerDiaAnterior.idPais = entrada.p_IdPais;
		//cambiar dia habil para RF ,IF ,PAC ,SIM ?			
		ObtenerDiaAnterior.diaHabil = "N";
		ObtenerDiaAnterior.fechas = FechasDAValorCuota;
		
		ObtenerDiaAnterior dant = new ObtenerDiaAnterior();
		ObtenerDiaAnterior.OutObtenerDiaAnterior dantout = dant.logica(con, ObtenerDiaAnterior);
		
		List<String> fechasVigenciaEntrada = dantout.fechas.stream().map(fechaCalculada->fechaCalculada.fechaCalculada).distinct().collect(Collectors.toList());
		parametros.put("IdSerieFondo", seriesEntrada);
		parametros.put("FechaCierre", fechasVigenciaEntrada);
		List<ValorCuotaVigente> valoresCuotasVigentesAlmacenadas = sql.query(con, SQL_SELECT_VALOR_CUOTA_DIA_ANTERIOR_FECHA_CARGA, parametros, ValorCuotaVigente.class);
		Map<String, ValorCuotaVigente> valoresCuotasVigentesFecha = valoresCuotasVigentesAlmacenadas.stream().collect(Collectors.toMap(valorCuota->valorCuota.IdSerieFondo, valorCuota->valorCuota));
		logger.trace("FIN::CargarValorCuota.obtenerValorCuotaVigenteFechaAnterior");
		return valoresCuotasVigentesFecha;
	}
	
	private EstadoCargaValorCuota obtenerEstadoCargaValorCuota(ValorCuota valorCuota, Map<ValorCuotaVigente, Object> seriesDuplicadas, Map<String, SerieFondoSolicitudes> seriesFondos, Map<String ,ValorCuotaVigente> valorCuotaExistentes, String fechaSistema,String allFunds) {
		logger.trace("INICIO::CargarValorCuota.obtenerEstadoCargaValorCuota");
		ValorCuotaVigente valorCuotaEntrada = new ValorCuotaVigente();
		valorCuotaEntrada.IdSerieFondo = valorCuota.IDSERIEFONDO;
		valorCuotaEntrada.FechaVigencia = Fecha.toDate(valorCuota.FECHACIERRE);
		Object serieDuplicada = seriesDuplicadas.get(valorCuotaEntrada);
		SerieFondoSolicitudes serieFondo = seriesFondos.get(valorCuota.IDSERIEFONDO);
		ValorCuotaVigente valorCuotaExistente = valorCuotaExistentes.get(valorCuotaEntrada.IdSerieFondo);
		EstadoCargaValorCuota estadoCargaValorCuota = new EstadoCargaValorCuota();
		estadoCargaValorCuota.Estado = _c.OK;
		estadoCargaValorCuota.DescEstado = _c.OK;
		
		if(!_c.OK.equals(valorCuota.MENSAJEVALIDACION)) {
			estadoCargaValorCuota.Estado = _c.EstadoError;
			estadoCargaValorCuota.DescEstado = "FORMATO_INVALIDO";
			return estadoCargaValorCuota;
		}
		if(serieDuplicada != null) {
			estadoCargaValorCuota.Estado = _c.EstadoError;
			estadoCargaValorCuota.DescEstado = "VALOR_CUOTA_DUPLICADO";
			return estadoCargaValorCuota;
		}
		if(serieFondo == null) {
			estadoCargaValorCuota.Estado = _c.EstadoError;
			estadoCargaValorCuota.DescEstado = "SERIE_NO_EXISTE";
			return estadoCargaValorCuota;
		}
		if(valorCuota.VALORCUOTA.compareTo(BigDecimal.ZERO) == 0) {
			estadoCargaValorCuota.Estado = _c.EstadoError;
			estadoCargaValorCuota.DescEstado = "VALOR_CUOTA_INVALIDO";
			return estadoCargaValorCuota;
		}
		if("N".equals(allFunds) && !valorCuota.IDMONEDA.equals(serieFondo.IdMoneda)){
			estadoCargaValorCuota.Estado = _c.EstadoError;
			estadoCargaValorCuota.DescEstado = "MONEDA_INCORRECTA";
			return estadoCargaValorCuota;
		}
		Date fechaCierre = Fecha.toDate(valorCuota.FECHACIERRE);
		Date fechaSistemaComparacion = Fecha.toDate(fechaSistema);
		if(fechaCierre.compareTo(fechaSistemaComparacion) == 1) {
			estadoCargaValorCuota.Estado = _c.EstadoError;
			estadoCargaValorCuota.DescEstado = "FECHA_VALOR_CUOTA_INCORRECTA";
			return estadoCargaValorCuota;
		}
		if(valorCuotaExistente != null) {
			estadoCargaValorCuota.Estado = _c.EstadoAdvertencia;
			estadoCargaValorCuota.DescEstado = "VALOR_EXISTE_FECHA";
			return estadoCargaValorCuota;
		}
		
		logger.trace("FIN::CargarValorCuota.obtenerEstadoCargaValorCuota");
		return estadoCargaValorCuota;
		
	}
	
	private Date obtenerFechaCierre(ValorCuota valorCuota, EstadoCargaValorCuota estado) {
		logger.trace("INICIO::CargarValorCuota.obtenerFechaCierre");
		Date fechaCierre = Fecha.toDate("0001-01-01");
		if(esEstadoCorrecto(estado.Estado)) {
			fechaCierre = Fecha.toDate(valorCuota.FECHACIERRE);
		}
		logger.trace("FIN::CargarValorCuota.obtenerFechaCierre");
		return fechaCierre;
	}
	
	private Date obtenerFechaCalculoAllFunds(ValorCuota valorCuota, EstadoCargaValorCuota estado) {
		logger.trace("INICIO::CargarValorCuota.obtenerFechaCalculoAllFunds");
		Date fechaCalculo = Fecha.toDate("0001-01-01");
		if(esEstadoCorrecto(estado.Estado)) {
			fechaCalculo = Fecha.toDate(valorCuota.FECHACALCULO);
		}
		logger.trace("FIN::CargarValorCuota.obtenerFechaCalculoAllFunds");
		return fechaCalculo;
	}
	
	private Boolean esEstadoCorrecto(String estado) {
		logger.trace("INICIO::CargarValorCuota.esEstadoCorrecto");
		Boolean esEstadoCorrecto = !_c.EstadoError.equals(estado);
		logger.trace("FIN::CargarValorCuota.esEstadoCorrecto");
		return esEstadoCorrecto;
	}
	
	private Long obtenerDecimalesValorCuota(ValorCuota valorCuota, Map<String, SerieFondoSolicitudes> seriesFondos){
		logger.trace("INICIO::CargarValorCuota.obtenerDecimalesValorCuota");
		SerieFondoSolicitudes serieFondo = seriesFondos.get(valorCuota.IDSERIEFONDO);
		Long decimalesValorCuota = serieFondo != null ? serieFondo.DecimalesValorCuota : 0L;
		logger.trace("FIN::CargarValorCuota.obtenerDecimalesValorCuota");
		return decimalesValorCuota;
	}
	
	private String obtenerEmisor(ValorCuota valorCuota, Map<String, SerieFondoSolicitudes> seriesFondos, EstadoCargaValorCuota estado) {
		logger.trace("INICIO::CargarValorCuota.obtenerEmisor");
		String emisor = "";
		if(esEstadoCorrecto(estado.Estado)) {
			SerieFondoSolicitudes serieFondo = seriesFondos.get(valorCuota.IDSERIEFONDO);
			emisor = serieFondo.IdEmisor;
		}
		logger.trace("FIN::CargarValorCuota.obtenerEmisor");
		return emisor;
	}
	
	private BigDecimal obtenerValorCuota(BigDecimal valorCuotaOriginal, Long decimalesValorCuota, EstadoCargaValorCuota estado){
		logger.trace("INICIO::CargarValorCuota.obtenerValorCuota");
		BigDecimal valorCuotaCalculado = BigDecimal.ZERO;
		if(esEstadoCorrecto(estado.Estado)) {
			valorCuotaCalculado = valorCuotaOriginal.setScale(decimalesValorCuota.intValue(), RoundingMode.FLOOR);
		}
		logger.trace("FIN::CargarValorCuota.obtenerValorCuota");
		return valorCuotaCalculado;
	}
	
	private List<MensajeSalida> obtenerMensajesSalida(Connection con, List<ResultadoCargaValorCuota> resultados) throws Exception {
		logger.trace("INICIO::CargarValorCuota.obtenerValorCuota");
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		for(ResultadoCargaValorCuota resultado : resultados) {
			if(!esEstadoCorrecto(resultado.Estado)) {
				mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,resultado.DescEstado,new HashMap<String,String>(),con,resultado.IdEmpresa));
			}
		}
		logger.trace("FIN::CargarValorCuota.obtenerValorCuota");
		return mensajes;
	}
	
	private List<ResultadoCargaValorCuota> obtenerValoresCuotaHistoricosInsertar(List<ResultadoCargaValorCuota> valoresCuotas){
		logger.trace("INICIO::CargarValorCuota.obtenerValoresCuotaHistoricosInsertar");
		List<ResultadoCargaValorCuota> valoresCuotasHistoricoInsertar = valoresCuotas.stream().filter(valorCuota->esEstadoCorrecto(valorCuota.Estado)).collect(Collectors.toList());
		logger.trace("FIN::CargarValorCuota.obtenerValoresCuotaHistoricosInsertar");
		return valoresCuotasHistoricoInsertar;
	}
	
	private List<ResultadoCargaValorCuota> obtenerValoresCuotaInsertar(List<ResultadoCargaValorCuota> valoresCuotas){
		logger.trace("INICIO::CargarValorCuota.obtenerValoresCuotaInsertar");
		List<ResultadoCargaValorCuota> valoresCuotasCorrectas = valoresCuotas.stream().filter(valorCuota->esEstadoCorrecto(valorCuota.Estado)).collect(Collectors.toList());
		Map<String, List<ResultadoCargaValorCuota>> valoresCuotaPorIdSerieFondo = valoresCuotasCorrectas.stream().collect(Collectors.groupingBy(valorCuota->valorCuota.IdSerieFondo));
		
		List<ResultadoCargaValorCuota> valoresCuotasInsertar = new ArrayList<ResultadoCargaValorCuota>();
		for(Entry<String, List<ResultadoCargaValorCuota>> valorCuotaIdSerieFondo : valoresCuotaPorIdSerieFondo.entrySet()) {
			List<ResultadoCargaValorCuota> valoresCuotasIdSerieFondo = valorCuotaIdSerieFondo.getValue();
			Comparator<ResultadoCargaValorCuota> comparadorFechas = obtenerComparadorFecha();
			List<ResultadoCargaValorCuota> valoresCuotaOrdenados = valoresCuotasIdSerieFondo.stream().sorted(comparadorFechas).collect(Collectors.toList());
			valoresCuotasInsertar.add(valoresCuotaOrdenados.get(0));
		}
		
		logger.trace("FIN::CargarValorCuota.obtenerValoresCuotaInsertar");
		return valoresCuotasInsertar;
	}
	
	private Comparator<ResultadoCargaValorCuota> obtenerComparadorFecha() {
		logger.trace("INICIO::CargarValorCuota.obtenerComparadorFecha");
		Comparator<ResultadoCargaValorCuota> comparadorFechas = new Comparator<ResultadoCargaValorCuota>() {
		    @Override
		    public int compare(ResultadoCargaValorCuota valorCuota1, ResultadoCargaValorCuota valorCuota2) {
		        if(valorCuota1.FechaVigencia.compareTo(valorCuota2.FechaVigencia) == 0){
		            return 0;
		        }
		        return valorCuota2.FechaVigencia.compareTo(valorCuota1.FechaVigencia) == 1 ? 1 : -1;
		     }
		};
		logger.trace("FIN::CargarValorCuota.obtenerComparadorFecha");
		return comparadorFechas;
	}
	
	private void actualizarUltimaCarga(Connection con, List<ResultadoCargaValorCuota> valoresCuotas) throws Exception{
		logger.trace("INICIO::CargarValorCuota.actualizarUltimaCarga");
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_VALOR_CUOTA_ULTIMA_CARGA, valoresCuotas, ResultadoCargaValorCuota.class);
		logger.trace("FIN::CargarValorCuota.actualizarUltimaCarga");
	}
	
	private void insertarValorCuotaHistorico(Connection con, List<ResultadoCargaValorCuota> valoresCuotas) throws Exception{
		logger.trace("INICIO::CargarValorCuota.insertarValorCuotaHistorico");
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_CARGA_VALOR_CUOTA_HISTORICO, valoresCuotas, ResultadoCargaValorCuota.class);
		logger.trace("FIN::CargarValorCuota.insertarValorCuotaHistorico");
	}
	
	private void eliminarValoresCuotas(Connection con, List<ResultadoCargaValorCuota> valoresCuotas) throws Exception{
		logger.trace("INICIO::CargarValorCuota.eliminarValoresCuotas");
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_VALOR_CUOTA, valoresCuotas, ResultadoCargaValorCuota.class);
		logger.trace("FIN::CargarValorCuota.eliminarValoresCuotas");
	}
	
	private void insertarValorCuota(Connection con, List<ResultadoCargaValorCuota> valoresCuotas) throws Exception{
		logger.trace("INICIO::CargarValorCuota.insertarValorCuota");
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_VALOR_CUOTA, valoresCuotas, ResultadoCargaValorCuota.class);
		logger.trace("FIN::CargarValorCuota.insertarValorCuota");
	}
	
	private void agregarLog(Connection con, EntradaCargarValorCuota entrada, List<ResultadoCargaValorCuota> valoresCuotas) throws Exception {
		logger.trace("INICIO::CargarValorCuota.agregarLog");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_Log = valoresCuotas.stream().map(valorCuota->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = 159L;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = _c.ModuloFondoMutuo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "VALOR_CUOTA_FMM";
			log.EstadoRegistro = "";
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.Nemo = "";
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "Cargar Valor Cuota";
			if (valorCuota != null && valorCuota.FechaVigencia != null && Fecha.toStringDate(valorCuota.FechaCalculo) != null) {
				log.FechaSistemaLog = Fecha.toStringDate(valorCuota.FechaCalculo);
			} else { 
				log.FechaSistemaLog = "";
			}						
			entradaAgregarLog.p_Log.add(log);
			
			//detalle log 
			AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
			detLog.IdCabeceraLog = 159L;
			detLog.IdTrabajo = 0L;
			detLog.SubRegistro = valorCuota.IdSerieFondo != null ? valorCuota.IdSerieFondo : "";
			detLog.ValorAntiguo = "";
			
			if (valorCuota != null && valorCuota.FechaVigencia != null && Fecha.toStringDate(valorCuota.FechaVigencia) != null) {
				detLog.CampoRegistro = "VALOR_CUOTA_FMM " + Fecha.toStringDate(valorCuota.FechaVigencia);
			} else { 
				detLog.CampoRegistro = "VALOR_CUOTA_FMM ";
			}
			
			if (valorCuota != null && valorCuota.ValorCuota != null && valorCuota.ValorCuota.toString() != null) {
			    detLog.ValorNuevo = valorCuota.ValorCuota.toString();
			} else { 
				detLog.ValorNuevo = "";
			}
			entradaAgregarLog.p_LogDetalle.add(detLog);
			
			return log;
		}).collect(Collectors.toList());
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		logger.trace("FIN::CargarValorCuota.agregarLog");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_UsaReglasNegocio\":\"N\",\"p_SimularOperacion\":\"N\",\"p_ValorCuota\":[{\"IDSERIEFONDO\":\"FMLIQUIDEZ_A\",\"FECHACIERRE\":\"2020-06-29\",\"VALORCUOTA\":1693.8561,\"IDMONEDA\":\"CLP\",\"OBSERVACION\":\"NINGUNA\",\"MENSAJEVALIDACION\":\"OK\"},{\"IDSERIEFONDO\":\"FMLIQUIDEZ_A\",\"FECHACIERRE\":\"2020-05-29\",\"VALORCUOTA\":5.6109,\"IDMONEDA\":\"CLP\",\"OBSERVACION\":\"NINGUNA\",\"MENSAJEVALIDACION\":\"OK\"}]}";
		EntradaCargarValorCuota Objin = gson.fromJson(json, EntradaCargarValorCuota.class);
		CargarValorCuota demo = new CargarValorCuota();				
		try {
			SalidaCargarValorCuota ObjOut=demo.logica(con, Objin);
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