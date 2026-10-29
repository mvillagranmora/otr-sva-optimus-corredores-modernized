package cl.bcs.plataforma.mer;

import java.sql.Connection;
import java.sql.SQLException;
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

import cl.bcs.plataforma.mer.GenerarLogDeterioroEmisor.CopiaDeterioro;
import cl.bcs.plataforma.mer.GenerarLogDeterioroEmisor.GenerarLogDeterioroEmisor_Entrada;
import cl.bcs.plataforma.mer.pojo.DeterioroEmisor;
import cl.bcs.plataforma.mer.pojo.Validacion;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarDeterioroEmisor implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(ModificarDeterioroEmisor.class);

	private static final FileLoader LOADER = new FileLoader();

	private static final String SQL_UPDATE_DETERIORO_EMISOR = LOADER.loadResourceToString("cl/bcs/plataforma/mer/sql/ModificarDeterioroEmisor_UpdateDeterioroEmisor.sql");
	private static final String SQL_SELECT_DETERIORO_EMISOR = LOADER.loadResourceToString("cl/bcs/plataforma/mer/sql/ModificarDeterioroEmisor_SelectDeterioroEmisor.sql");

	public static class ModificarDeterioroEmisor_Entrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
	    public String p_IdModulo;
	    public String p_UsaReglasNegocio;
	    public List<DeterioroEmisor> p_DeteriorosEmisores;

		public ModificarDeterioroEmisor_Entrada() {
			this.p_DeteriorosEmisores = new ArrayList<DeterioroEmisor>();
		}
	}
	
	public static class ModificarDeterioroEmisor_Salida extends BcsPojo {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public ModificarDeterioroEmisor_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public void agregarValidacionesSalida(List<Validacion> validaciones) {
			for(Validacion validacion : validaciones) {
				agregarValidacionSalida(validacion);
			}
		}
		
		public void agregarValidacionSalida(Validacion validacion) {
			if(validacion.EsError) {
				this.P_OUT_ERRORESCONTADOR += 1L;
				this.P_OUT_MENSAJES.add(validacion.MensajeError);
			}
		}	
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ModificarDeterioroEmisor_Entrada in = gson.fromJson(jin.toString(),ModificarDeterioroEmisor_Entrada.class);
		ModificarDeterioroEmisor_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ModificarDeterioroEmisor_Salida logica(Connection con, ModificarDeterioroEmisor_Entrada entrada) throws Exception {
		
		logger.info("INICIO::ModificarDeterioroEmisor");
		Long inicioServicio = System.currentTimeMillis();
		
		ModificarDeterioroEmisor_Salida out = new ModificarDeterioroEmisor_Salida();
		
		try {

			Map<String,CopiaDeterioro> mapRegistrosOriginales = getMapRegistrosOriginales(con, entrada.p_DeteriorosEmisores);

			List<DeterioroEmisor> registros = obtenerRegistros(con, entrada,mapRegistrosOriginales);
			out.agregarValidacionesSalida(ValidarDeterioroEmisor.validarRegistroDeterioroEmisor(con, registros, entrada.p_IdEmpresa));
			
			if(out.P_OUT_ERRORESCONTADOR > 0) return out;

			int resultadoModificacion[] = modificarDeterioros(con, registros);
			
			if(ValidarDeterioroEmisor.validarResultadoSQL(resultadoModificacion)) {
				GenerarLogDeterioroEmisor_Entrada entradaLog = new GenerarLogDeterioroEmisor_Entrada();
				GenerarLogDeterioroEmisor log = new GenerarLogDeterioroEmisor();
				
				entradaLog.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaLog.p_IdModulo = entrada.p_IdModulo;
				entradaLog.p_IdUsuario = entrada.p_IdUsuario;
				entradaLog.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				entradaLog.registros = registros;
				entradaLog.mapRegistrosOriginales = mapRegistrosOriginales;
				entradaLog.TipoLog = "MODIFICACION";
				
				log.logica(con, entradaLog);

			}
			

		}catch(Exception e) {
			e.printStackTrace();
			MensajeSalida msg = new MensajeSalida();
			msg.id = 1L;
			msg.tipo = "ERROR";
			msg.codigo = "ERROR";
			msg.descripcion = e.getMessage();
			out.P_OUT_MENSAJES.add(msg);
			out.P_OUT_ERRORESCONTADOR = 1L;

		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDeterioroEmisor", finServicio - inicioServicio);
		return out;
	}
	
	private List<DeterioroEmisor> obtenerRegistros(Connection con, ModificarDeterioroEmisor_Entrada entrada, Map<String,CopiaDeterioro> mapRegistrosOriginales) throws Exception {

		logger.trace("INICIO::ModificarDeterioroEmisor.obtenerRegistros");
		Long inicioServicio = System.currentTimeMillis();
		List<DeterioroEmisor> registrosModificar = new ArrayList<DeterioroEmisor>();

		for (DeterioroEmisor deterioro : entrada.p_DeteriorosEmisores) {
			if(mapRegistrosOriginales.containsKey(deterioro.Emisor)) {
				deterioro.IdDeterioroEmisor = mapRegistrosOriginales.get(deterioro.Emisor).IdDeterioroEmisor;
				deterioro.Emisor = deterioro.Emisor.toUpperCase();
				deterioro.Accion = "MODIFICAR";
				deterioro.FlagDeterioro = deterioro.FlagDeterioro;
				deterioro.PorcentajeDeterioro = deterioro.PorcentajeDeterioro;
				deterioro.Fase = deterioro.Fase;
				deterioro.IdEstado = (deterioro.IdEstado == null || !"INA".equals(deterioro.IdEstado)) ? "ACT" : "INA";
				registrosModificar.add(deterioro);
			}
			
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.trace("FIN::ModificarDeterioroEmisor.obtenerRegistros", finServicio - inicioServicio);
		return registrosModificar;

	}
	
	private int [] modificarDeterioros(Connection con, List<DeterioroEmisor> registros) throws Exception{
		
		logger.debug("INICIO::ModificarDeterioroEmisor.modificarDeterioros");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		int [] resultado = sql.batch(con, SQL_UPDATE_DETERIORO_EMISOR, registros, DeterioroEmisor.class);
		
		Long finServicio = System.currentTimeMillis();
		logger.debug("FIN::CrearModificarDeterioroEmisor.modificarDeterioros", finServicio - inicioServicio);
		return resultado;
		
	}

	private List<CopiaDeterioro> getRegistrosOriginales(Connection con, List<DeterioroEmisor> registros) throws SQLException {
		
		logger.debug("INICIO::ModificarDeterioroEmisor.getRegistrosOriginales");
		Long inicioMetodo = System.currentTimeMillis();
			
		List<String> emisores = new ArrayList<String>();
		
		for(DeterioroEmisor deterioro : registros)
			emisores.add(deterioro.Emisor);
		
		HashMap<String, Object> params = new HashMap<>(1);
		params.put("Emisor", emisores);
		SQL sql = new SQL();
		
		List<CopiaDeterioro> registrosOriginales = sql.query(con, SQL_SELECT_DETERIORO_EMISOR, params, CopiaDeterioro.class);
		
		Long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::ModificarDeterioroEmisor.getRegistrosOriginales :", finMetodo - inicioMetodo);
		return registrosOriginales;

	}
	
	private Map<String,CopiaDeterioro> getMapRegistrosOriginales(Connection con, List<DeterioroEmisor> registros) throws Exception{
		
		logger.debug("INICIO::ModificarDeterioroEmisor.crearLogModificacion");
		Long inicioServicio = System.currentTimeMillis();
				
		Map<String,CopiaDeterioro> mapRegistrosOriginales = getRegistrosOriginales(con, registros).stream()
											.collect(Collectors.toMap(registro -> registro.Emisor, registro -> registro));
		Long finServicio = System.currentTimeMillis();
		logger.debug("FIN::ModificarDeterioroEmisor.crearLogModificacion", finServicio - inicioServicio);
		
		return mapRegistrosOriginales;
	}

	public static void main(String[] args) throws Exception {
		// Prueba de la clase
		try {
			Connection con = PlataformaUtils.getPropertiesConnection("DESA", "connections.properties");
			try {
				Gson gson = Utils.getGsonBuilder();

				String jsonCrear = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\" ,"
						+ " \"p_DeteriorosEmisores\": [{\"IdDeterioroEmisor\":0, \"Emisor\": \"AGUAS\", \"FlagDeterioro\": \"N\", \"PorcentajeDeterioro\": \"0.67\", \"Fase\": \"1\", \"Accion\": \"\", \"IdEstado\": \"INA\"},"
						+ "{\"IdDeterioroEmisor\": 10, \"Emisor\": \"BTU\", \"FlagDeterioro\": \"S\", \"PorcentajeDeterioro\": \"99.9999\", \"Fase\": \"3\", \"Accion\": \"\"}]}";
				ModificarDeterioroEmisor_Entrada entradaCrear = gson.fromJson(jsonCrear,ModificarDeterioroEmisor_Entrada.class);

				ModificarDeterioroEmisor demo = new ModificarDeterioroEmisor();

				ModificarDeterioroEmisor_Salida salidaModificar = demo.logica(con, entradaCrear);
				System.out.println(salidaModificar.toString());

				con.rollback();
				//con.commit();

			} catch (Exception e) {
				con.rollback();
				e.printStackTrace();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("== END ==");
	}


}
