package cl.bcs.socionegocio.wbs;

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

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarEmpleados implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConsultarEmpleados.class);
	
	private final String SQL_SELECT_EMPLEADOS = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarEmpleados_SelectEmpleados.sql");
	private final String SQL_SELECT_ESTADOS_EMPLEADO = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarEmpleados_SelectEstadosEmpleado.sql");
	
	public static class EntradaConsultarEmpleados extends BcsPojo {
		public Long p_IdEmpresa;
		public String p_IdUsuario;
		public String p_ClaveUsuario;
		public List<FiltroEmpleado> p_Empleados;
		
		public EntradaConsultarEmpleados () {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = "";
			this.p_ClaveUsuario = "";
			this.p_Empleados = new ArrayList<FiltroEmpleado>();
		}
		
	}
	
	public static class FiltroEmpleado extends BcsPojo {
		public Long IdInterno;
		public String Filtro;
		public String Valor;
	}
	
	public static class SalidaConsultarEmpleados extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Empleado> P_OUT_RESULTADO;
		
		public SalidaConsultarEmpleados () {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<Empleado>();
		}
	}

	public static class Empleado extends BcsPojo {
		public Long IdEmpleado;
		public String DocumentoIdentidad;
		public String CodigoEmpleado;
		public String NombreEmpleado;
		public String IdEstadoEmpleado;
		public String EstadoEmpleado;
		public String EsEjecutivo;
		public String IdEstadoEjecutivo;
		public String EstadoEjecutivo;
		public String NombreSupervisor;
		public String CodigoSupervisor;
		public String NombreEjecutivoReemplazo;
		public String FechaInicioDeReemplazo;
		public String FechaFinDeReemplazo;
		
		public Empleado() {
			this.IdEmpleado = 0L;
			this.DocumentoIdentidad = "";
			this.CodigoEmpleado = "";
			this.NombreEmpleado = "";
			this.IdEstadoEmpleado = "";
			this.EstadoEmpleado = "";
			this.EsEjecutivo = "";
			this.IdEstadoEjecutivo = "";
			this.EstadoEjecutivo = "";
			this.NombreSupervisor = "";
			this.CodigoSupervisor = "";
			this.NombreEjecutivoReemplazo = "";
			this.FechaInicioDeReemplazo = "0001-01-01";
			this.FechaFinDeReemplazo = "0001-01-01";
		}
	}
	
	public static class ValorValidoFiltro extends BcsPojo{
		public String TipoFiltro;
		public String ValorFiltro;
		
		public ValorValidoFiltro() {
			this.TipoFiltro = "";
			this.ValorFiltro = "";
		}
		
	}
	
	public SalidaConsultarEmpleados logica(Connection con, EntradaConsultarEmpleados entrada) throws Exception {
		logger.info("INICIO::ConsultarEmpleados");
		long inicio = System.currentTimeMillis();
		
		SalidaConsultarEmpleados salida = new SalidaConsultarEmpleados();
		
		List<String> documentosIdentidad = entrada.p_Empleados.parallelStream()
				.filter(Filtro -> "DocumentoIdentidad".equals(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor).collect(Collectors.toList());
		
		List<String> esEjecutivo = entrada.p_Empleados.parallelStream()
				.filter(Filtro -> "EsEjecutivo".equalsIgnoreCase(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor.toUpperCase())
				.collect(Collectors.toList());

		List<String> idsEstadoEmpleado = entrada.p_Empleados.parallelStream()
				.filter(Filtro -> "EstadoEmpleado".equalsIgnoreCase(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor.toUpperCase())
				.collect(Collectors.toList());

		List<String> idsEstadoEjecutivo = entrada.p_Empleados.parallelStream()
				.filter(Filtro -> "EstadoEjecutivo".equalsIgnoreCase(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor.toUpperCase())
				.collect(Collectors.toList());

		List<String> codigosEmpleado = entrada.p_Empleados.parallelStream()
				.filter(Filtro -> "CodigoEmpleado".equalsIgnoreCase(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor.toUpperCase())
				.collect(Collectors.toList());
		
		List<String> codigosSupervisor = entrada.p_Empleados.parallelStream()
				.filter(Filtro -> "CodigoSupervisor".equalsIgnoreCase(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor.toUpperCase())
				.collect(Collectors.toList());
		
		salida.P_OUT_MENSAJES.addAll(verificarFiltrosEntrada(con,entrada.p_IdEmpresa,documentosIdentidad, esEjecutivo,idsEstadoEmpleado, idsEstadoEjecutivo));
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		
		if(salida.P_OUT_ERRORESCONTADOR > 0L) {
			long fin = System.currentTimeMillis();
			logger.info("FIN::ConsultarEmpleados", fin - inicio);
			return salida;
		}
		
		SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(6);
        parametros.put("documentosIdentidad", documentosIdentidad);
        parametros.put("esEjecutivo", esEjecutivo);
        parametros.put("idsEstadoEmpleado", idsEstadoEmpleado);
        parametros.put("idsEstadoEjecutivo", idsEstadoEjecutivo);
        parametros.put("codigosEmpleado", codigosEmpleado);
        parametros.put("codigosSupervisor", codigosSupervisor);
		
        List<Empleado> empleados = sql.query(con, SQL_SELECT_EMPLEADOS, parametros, Empleado.class); 
        
        salida.P_OUT_RESULTADO = empleados;
        
		if(salida.P_OUT_RESULTADO.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_SIN_EMPLEADOS",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
		} else {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_CONSULTA_EMPLEADOS_OK",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarEmpleados", fin - inicio);
		return salida;
	}
	
	public List<MensajeSalida> verificarFiltrosEntrada(Connection con, Long idEmpresa, List<String> documentosIdentidad, List<String> esEjecutivo, List<String> idsEstadoEmpleado, List<String> idsEstadoEjecutivo) throws Exception {
		logger.info("INICIO::ConsultarEmpleados.verificarFiltrosEntrada");
		long inicio = System.currentTimeMillis();
		
		List<MensajeSalida> mensajes = new ArrayList<>();
        HashMap<String,String> params = new HashMap<String,String>(1);
        
        List<ValorValidoFiltro> valoresValidosFiltros = obtenerValoresValidosFiltros(con);
        
		for(String ejecutivo : esEjecutivo) {
        	if(!("S".equalsIgnoreCase(ejecutivo) || "N".equalsIgnoreCase(ejecutivo))) {
        		params.put("EsEjecutivo",ejecutivo);
        		mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ES_EJECUTIVO_NO_ES_VALIDO",params,con, idEmpresa));
        		params.clear();
        	}
        }
		
		for(String estado : idsEstadoEmpleado) {
			if(!valoresValidosFiltros.parallelStream().anyMatch(val -> "Estado".equalsIgnoreCase(val.TipoFiltro) && estado.equalsIgnoreCase(val.ValorFiltro))) {
				params.put("Estado",estado);
        		mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ESTADO_EMPLEADO_NO_ES_VALIDO",params,con, idEmpresa));
        		params.clear();
			}
		}

		for(String estado : idsEstadoEjecutivo) {
			if(!valoresValidosFiltros.parallelStream().anyMatch(val -> "Estado".equalsIgnoreCase(val.TipoFiltro) && estado.equalsIgnoreCase(val.ValorFiltro))) {
				params.put("Estado",estado);
        		mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ESTADO_EJECUTIVO_NO_ES_VALIDO",params,con, idEmpresa));
        		params.clear();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarEmpleados.verificarFiltrosEntrada", fin - inicio);
		return mensajes;
	}

	public List<ValorValidoFiltro> obtenerValoresValidosFiltros(Connection con) throws Exception {
		logger.info("INICIO::ConsultarEmpleados.obtenerValoresValidosFiltros");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<ValorValidoFiltro> filtroEstados = sql.query(con, SQL_SELECT_ESTADOS_EMPLEADO, new HashMap<String,Object>(0), ValorValidoFiltro.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarEmpleados.obtenerValoresValidosFiltros", fin - inicio);
		return filtroEstados;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarEmpleados jin=gson.fromJson(in.toString() ,EntradaConsultarEmpleados.class);
		SalidaConsultarEmpleados out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa02")) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd")
						.serializeNulls().create();

				String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_ClaveUsuario\":\"\",\"p_Empleados\":["
						+ "{ \"IdInterno\": 0, \"Filtro\": \"EsEjecutivo\", \"Valor\": \"S\" }]}";
				EntradaConsultarEmpleados in = gson.fromJson(jsonCL, EntradaConsultarEmpleados.class);

				ConsultarEmpleados demo = new ConsultarEmpleados();
				System.out.println("ENTRADA");
				System.out.println(in);
				
				try {
					SalidaConsultarEmpleados out = demo.logica(con, in);
					con.rollback();
					System.out.println("SALIDA");
					System.out.println(out);
				} catch (Exception er) {
					er.printStackTrace();
					con.rollback();
				}
				
			}catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}

	}

}
