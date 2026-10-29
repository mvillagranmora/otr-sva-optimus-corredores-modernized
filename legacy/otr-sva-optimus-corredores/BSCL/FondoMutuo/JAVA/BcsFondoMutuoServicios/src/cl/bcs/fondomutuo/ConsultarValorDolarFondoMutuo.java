package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarValorDolarFondoMutuo implements JavaCall{
	
	private final String SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarValorDolarFondoMutuo_SelectValorCuotaTipoCambio.sql");
	
	static final Logger logger = LogManager.getLogger(ConsultarValorDolarFondoMutuo.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaConsultarValorDolarFondoMutuo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<SerieFondoParidad> p_Series;
		
		public EntradaConsultarValorDolarFondoMutuo() {
			this.p_Series = new ArrayList<SerieFondoParidad>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SerieFondoParidad{
		public String IdSerieFondo;
		public String IdTipoSolicitud;
		public String Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarValorDolarFondoMutuo{
		public Long P_OUT_ERRORESCONTADOR;
		public List <ResultadoValorDolarFondoMutuo> P_OUT_RESULTADO;
		
		public SalidaConsultarValorDolarFondoMutuo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_RESULTADO = new ArrayList<ResultadoValorDolarFondoMutuo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoValorDolarFondoMutuo{
		public BigDecimal ValorParidad;
		public String IdSerieFondo;
		public String Fecha;
		public String IdTipoSolicitud;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValorCuotaTipoParidad{
		public String IdSerieFondo;
		public BigDecimal ValorDolarAporte;
		public BigDecimal ValorDolarRescate;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarValorDolarFondoMutuo jin = gson.fromJson(in.toString(), EntradaConsultarValorDolarFondoMutuo.class);
		SalidaConsultarValorDolarFondoMutuo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarValorDolarFondoMutuo logica (Connection con, EntradaConsultarValorDolarFondoMutuo entrada) throws Exception {
		logger.info("INICIO::ConsultarValorDolarFondoMutuo");
		SalidaConsultarValorDolarFondoMutuo out = new SalidaConsultarValorDolarFondoMutuo();
		SQL sql=new SQL();
		
		String usaDolarObservado = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "UsaDolarObservado");
		String idMoneda = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		if(entrada.p_Series.size() > 0) {
			if(_c.SI.equals(usaDolarObservado)) {
				HashMap<String,Boolean> fechasRepetidas = new HashMap<String,Boolean>();
				HashMap<String,Long> fechasIdInterno = new HashMap<String,Long>();
				CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
				Long i = 0L;
				CalcularParidad calcularParidad = new CalcularParidad();
				calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
				calcularParidadEntrada.p_IdTrabajo = 1L;
				calcularParidadEntrada.p_CalculosEntrada = entrada.p_Series.stream().filter(serie->{
					if(fechasRepetidas.get(serie.Fecha) != null) {
						return false;
					}else {
						fechasRepetidas.put(serie.Fecha, true);
						return true;
					}
				}).map(serie ->{
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.IdTrabajo = 1L;
					calculoEntrada.IdInterno = i;
					calculoEntrada.Fecha = serie.Fecha;
					calculoEntrada.IdMonedaOrigen = "USD";
					calculoEntrada.IdMonedaDestino = idMoneda;
					calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
					calculoEntrada.MontoOrigen = BigDecimal.ONE;
					calculoEntrada.IdUso = "";
					fechasIdInterno.put(serie.Fecha, i);
					return calculoEntrada;
				}).collect(Collectors.toList());
				
				CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
				HashMap<Long,Object> paridades = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
				
				out.P_OUT_RESULTADO = entrada.p_Series.stream().map(serie->{
					ResultadoValorDolarFondoMutuo resultado = new ResultadoValorDolarFondoMutuo();
					resultado.IdSerieFondo = serie.IdSerieFondo;
					resultado.Fecha = serie.Fecha;
					resultado.IdTipoSolicitud = serie.IdTipoSolicitud;
					Long id = fechasIdInterno.get(serie.Fecha);
					CalculoSalida calcularParidadSalida = (CalculoSalida) paridades.get(id);
					resultado.ValorParidad = (calcularParidadSalida != null) ? calcularParidadSalida.MontoDestino : BigDecimal.ZERO;
					return resultado;
				}).collect(Collectors.toList());
				
			}else {
				List<String> series = entrada.p_Series.stream().map(serie->{
					return serie.IdSerieFondo;
				}).collect(Collectors.toList());
				
				HashMap<String,Object> params = new HashMap<String,Object>();
				params.put("IdSerieFondo", series);
				params.put("IdEmpresa",entrada.p_IdEmpresa);
				logger.debug("INICIO::SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO");
				List<ValorCuotaTipoParidad> valorCuotaTipoParidad = sql.query(con, SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO, params, ValorCuotaTipoParidad.class);
				logger.debug("FIN::SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO");
				HashMap<String,Object> paridades = Utils.List2Hash(valorCuotaTipoParidad, "IdSerieFondo");
				out.P_OUT_RESULTADO = entrada.p_Series.stream().map(serie->{
					ResultadoValorDolarFondoMutuo resultado = new ResultadoValorDolarFondoMutuo();
					resultado.IdSerieFondo = serie.IdSerieFondo;
					resultado.Fecha = serie.Fecha;
					resultado.IdTipoSolicitud = serie.IdTipoSolicitud;
					ValorCuotaTipoParidad calcularParidadSalida = (ValorCuotaTipoParidad) paridades.get(serie.IdSerieFondo);
					resultado.ValorParidad = (calcularParidadSalida != null) ? _c.TipoSolicitudAporte.equals(serie.IdTipoSolicitud) ? calcularParidadSalida.ValorDolarAporte : calcularParidadSalida.ValorDolarRescate : BigDecimal.ZERO;
					return resultado;
				}).collect(Collectors.toList());
			}
		}
		
		
		logger.info("FIN::ConsultarValorDolarFondoMutuo");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_Series\":[{\"IdSerieFondo\":\"FMOPTIMUS_A\",\"IdTipoSolicitud\":\"APO\",\"Fecha\":\"2018-10-23\"}]}";
		EntradaConsultarValorDolarFondoMutuo Objin = gson.fromJson(jsonRF, EntradaConsultarValorDolarFondoMutuo.class);
		ConsultarValorDolarFondoMutuo  demo=new ConsultarValorDolarFondoMutuo();				
		try {
			SalidaConsultarValorDolarFondoMutuo ObjOut=demo.logica(con, Objin);
			//con.rollback();
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
	
	
}
