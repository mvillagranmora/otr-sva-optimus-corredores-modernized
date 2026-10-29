package cl.bcs.plataforma.bat;

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

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarTipoProgramaEnProceso implements JavaCall {	
	static final Logger logger = LogManager.getLogger(ConsultarTipoProgramaEnProceso.class);
	//QUERIES
	private final String SQL_SELECT_TIPO_PROGRAMA 				= loadResourceToString("cl/bcs/plataforma/bat/sql/ConsultarTipoProgramaEnProceso_SelectTipoPrograma.sql");
	private final String SQL_SELECT_TIPO_PROGRAMA_EN_PROCESO 	= loadResourceToString("cl/bcs/plataforma/bat/sql/ConsultarTipoProgramaEnProceso_SelectTipoProgramaEnProceso.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	/*******************
	 * clases in/out
	 *******************/
	public static class TipoPrograma extends BcsPojo {
		public String	IdTipoPrograma;
		public String	Existe;
		
		public TipoPrograma(){
			this.IdTipoPrograma = "";
			this.Existe = "N";
		}
	}
	
	public static class Programa extends BcsPojo {
		public Long		IdPrograma;
		public String	IdTipoPrograma;
		public String	IdUsuario;
		public String	CodigoUsuario;
		public String	IdEstado;
		
		public Programa () {
			this.IdPrograma = 0L;
			this.IdTipoPrograma = "";
			this.IdUsuario = "";
			this.CodigoUsuario = "";
			this.IdEstado = "";
		}
	}
	
	public static class EntradaConsultarTipoProgramaEnProceso extends BcsPojo {
		public Long 				p_IdEmpresa;
		public Long 				p_IdUsuario;
		public String				p_IdModulo;
		public List<TipoPrograma>	p_TipoProgramas;
		
		public EntradaConsultarTipoProgramaEnProceso(){
			this.p_IdEmpresa = 1L;
			this.p_IdModulo = "PLA";
			this.p_IdUsuario = 0L;
			this.p_TipoProgramas = new ArrayList<TipoPrograma>();
		}
	}
	
	public static class SalidaConsultarTipoProgramaEnProceso extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public SalidaConsultarTipoProgramaEnProceso() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();		
		EntradaConsultarTipoProgramaEnProceso in=gson.fromJson(jin.toString() ,EntradaConsultarTipoProgramaEnProceso.class);
		SalidaConsultarTipoProgramaEnProceso out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/**********************
	 * lógica del programa
	 **********************/
	@Trace
	public SalidaConsultarTipoProgramaEnProceso logica(Connection con, EntradaConsultarTipoProgramaEnProceso entrada) throws Exception {
		logger.debug("INICIO::ConsultarTipoProgramaEnProceso"); 
		Long inicio = System.currentTimeMillis();
		
		SalidaConsultarTipoProgramaEnProceso salida = new SalidaConsultarTipoProgramaEnProceso();
		
		List<MensajeSalida> salidaExistencia = consultarExistenciaTipoProgramas(con, entrada);
		salida.P_OUT_MENSAJES.addAll(salidaExistencia);
		
		List<MensajeSalida> salidaEnProcesamiento = consultarTipoProgramasEnProceso(con, entrada);
		
		salida.P_OUT_MENSAJES.addAll(salidaEnProcesamiento);
		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		
		if(salida.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_EXITO",new HashMap<String,String>(0),con,entrada.p_IdEmpresa));
		}
		
		Long fin = System.currentTimeMillis();
		logger.debug("INICIO::ConsultarTipoProgramaEnProceso{}", fin - inicio);
		return salida;
	}
	
	private List<MensajeSalida> consultarTipoProgramasEnProceso(Connection con, EntradaConsultarTipoProgramaEnProceso entrada) throws Exception {
		logger.debug("INICIO::ConsultarTipoProgramaEnProceso.consultarTipoProgramasEnProceso"); 
		Long inicio = System.currentTimeMillis();
		
		List<MensajeSalida> salida = new ArrayList<MensajeSalida>();
		
		List<String> tipoProgramasEntrada = entrada.p_TipoProgramas.stream().filter(tp -> (_c.SI).equals(tp.Existe)).map(tp -> {return tp.IdTipoPrograma;}).collect(Collectors.toList());
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("idTipoProgramas", tipoProgramasEntrada);
		
		SQL sql = new SQL();
		
		List<Programa> programasEnProceso = sql.query(con, SQL_SELECT_TIPO_PROGRAMA_EN_PROCESO, params, Programa.class);
		
		for(Programa programa : programasEnProceso) {
			HashMap<String,String> paramsError = new HashMap<String,String>(1);
			paramsError.put("IdTipoPrograma", programa.IdTipoPrograma);
			paramsError.put("CodigoUsuario", programa.CodigoUsuario);
			
			salida.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_TIPO_PROGRAMA_EN_PROCESO",paramsError,con,entrada.p_IdEmpresa));
			paramsError.clear();
		}
		
		Long fin = System.currentTimeMillis();
		logger.debug("INICIO::ConsultarTipoProgramaEnProceso.consultarTipoProgramasEnProceso{}", fin - inicio);
		return salida;
	}

	private List<MensajeSalida> consultarExistenciaTipoProgramas(Connection con, EntradaConsultarTipoProgramaEnProceso entrada) throws Exception {
		logger.debug("INICIO::ConsultarTipoProgramaEnProceso.consultarExistenciaTipoProgramas"); 
		Long inicio = System.currentTimeMillis();
		
		List<String> tipoProgramasEntrada = entrada.p_TipoProgramas.stream().map(tp -> {return tp.IdTipoPrograma;}).collect(Collectors.toList());
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("idTipoProgramas", tipoProgramasEntrada);
		
		SQL sql = new SQL();
		Map<String , String> tipoProgramasMap = sql.query(con, SQL_SELECT_TIPO_PROGRAMA, params, TipoPrograma.class).parallelStream()
				.collect(Collectors.toMap(tp -> tp.IdTipoPrograma, tp -> "S"));
		
		entrada.p_TipoProgramas.stream().forEach(tp -> {
			tp.Existe = tipoProgramasMap.containsKey(tp.IdTipoPrograma) ? tipoProgramasMap.get(tp.IdTipoPrograma) : tp.Existe;
		});
		
		List<MensajeSalida> salida = new ArrayList<MensajeSalida>();
		
		for(TipoPrograma tipoPrograma : entrada.p_TipoProgramas) {
			if(_c.NO.equals(tipoPrograma.Existe)) {
				HashMap<String,String> paramsError = new HashMap<String,String>(2);
				paramsError.put("IdTipoPrograma", tipoPrograma.IdTipoPrograma);
				salida.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_TIPO_PROGRAMA_NO_EXISTE",paramsError,con,entrada.p_IdEmpresa));
				paramsError.clear();
			}
		}
		
		Long fin = System.currentTimeMillis();
		logger.debug("INICIO::ConsultarTipoProgramaEnProceso.consultarExistenciaTipoProgramas{}", fin - inicio);
		return salida;
	}

	public static void main(String[] args) {
		try {
			Connection con =  PlataformaUtils.getDESAConnection();
			
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":3,\"p_IdModulo\":\"PLA\",\"p_TipoProgramas\":[{\"IdTipoPrograma\":\"CARGA_MASIVA_PREVIRED\"}]}"; 
			EntradaConsultarTipoProgramaEnProceso objin = gson.fromJson(json, EntradaConsultarTipoProgramaEnProceso.class);
	
			ConsultarTipoProgramaEnProceso demo = new ConsultarTipoProgramaEnProceso();
			SalidaConsultarTipoProgramaEnProceso salida = demo.logica(con, objin);
	        con.rollback();
	    	con.close();
	    	System.out.println("Mensajes= "+salida.P_OUT_MENSAJES);	
	    	System.out.println("fin");
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
}