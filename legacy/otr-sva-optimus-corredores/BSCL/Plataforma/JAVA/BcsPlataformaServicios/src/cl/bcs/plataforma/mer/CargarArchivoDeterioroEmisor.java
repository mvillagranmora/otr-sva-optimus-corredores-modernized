package cl.bcs.plataforma.mer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CrearDeterioroEmisor.CrearDeterioroEmisor_Entrada;
import cl.bcs.plataforma.mer.CrearDeterioroEmisor.CrearDeterioroEmisor_Salida;
import cl.bcs.plataforma.mer.ModificarDeterioroEmisor.ModificarDeterioroEmisor_Entrada;
import cl.bcs.plataforma.mer.ModificarDeterioroEmisor.ModificarDeterioroEmisor_Salida;
import cl.bcs.plataforma.mer.pojo.DeterioroEmisor;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CargarArchivoDeterioroEmisor implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(CargarArchivoDeterioroEmisor.class);

	private static final FileLoader LOADER = new FileLoader();
	
	private static final String SQL_SELECT_DETERIORO_EMISOR = LOADER.loadResourceToString("cl/bcs/plataforma/mer/sql/CargarArchivoDeterioroEmisor_SelectDeteriorosEmisores.sql");

	public static class CargarArchivoDeterioroEmisor_Entrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
	    public String p_IdModulo;
	    public String p_UsaReglasNegocio;
	    public List<DeterioroEmisor> p_DeteriorosEmisores;

		public CargarArchivoDeterioroEmisor_Entrada() {
			this.p_DeteriorosEmisores = new ArrayList<DeterioroEmisor>();
		}
	}
	
	public static class CargarArchivoDeterioroEmisor_Salida extends BcsPojo {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public CargarArchivoDeterioroEmisor_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}


	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		CargarArchivoDeterioroEmisor_Entrada in = gson.fromJson(jin.toString(),CargarArchivoDeterioroEmisor_Entrada.class);
		CargarArchivoDeterioroEmisor_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public CargarArchivoDeterioroEmisor_Salida logica(Connection con, CargarArchivoDeterioroEmisor_Entrada entrada) throws Exception {
		
		logger.info("INICIO::CargarArchivoDeterioroEmisor");
		Long inicioServicio = System.currentTimeMillis();
		
		CargarArchivoDeterioroEmisor_Salida out = new CargarArchivoDeterioroEmisor_Salida();
		
		try {
			
			List<DeterioroEmisor> registrosInsertar = new ArrayList<DeterioroEmisor>();
			List<DeterioroEmisor> registrosModificar = new ArrayList<DeterioroEmisor>();
			
			if(entrada.p_DeteriorosEmisores == null || entrada.p_DeteriorosEmisores.size() == 0) {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_DETERIORO_EMISOR_SIN_REGISTROS", new HashMap<String,String>() ,con,entrada.p_IdEmpresa));
				return out;
			}
			
			Boolean contieneDuplicados = false;
			Map<String, String> registrosDuplicados = new HashMap<>();
			
			Map<String,DeterioroEmisor> mapRegistros = getMapDeterioroEmisores(con);
			
			for(DeterioroEmisor deterioro : entrada.p_DeteriorosEmisores) {
				
				if(registrosDuplicados.containsKey(deterioro.Emisor)) {
					contieneDuplicados = true;
					break;
				}else{
					registrosDuplicados.put(deterioro.Emisor, deterioro.Emisor);
				}
				
				if(mapRegistros.containsKey(deterioro.Emisor)) {
					registrosModificar.add(deterioro);
				}else{
					registrosInsertar.add(deterioro);
				}
			}
			
			if(contieneDuplicados) {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_CARGA_DETERIORO_EMISOR_REPETIDO", new HashMap<String,String>() ,con,entrada.p_IdEmpresa));
				return out;
			}
			
			CrearDeterioroEmisor_Salida salidaInsercion = new CrearDeterioroEmisor_Salida();
			if(registrosInsertar.size() > 0) {
				
				CrearDeterioroEmisor crearDeterioroEmisor = new CrearDeterioroEmisor();
				CrearDeterioroEmisor_Entrada entradaInsercion =  new CrearDeterioroEmisor_Entrada();
				entradaInsercion.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaInsercion.p_IdUsuario = entrada.p_IdUsuario;
				entradaInsercion.p_IdModulo = entrada.p_IdModulo;
				entradaInsercion.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				entradaInsercion.p_DeteriorosEmisores = registrosInsertar;
				salidaInsercion = crearDeterioroEmisor.logica(con, entradaInsercion);
				
				out.P_OUT_ERRORESCONTADOR = salidaInsercion.P_OUT_ERRORESCONTADOR;
				if(out.P_OUT_ERRORESCONTADOR > 0) return out;
				
			}
			
			ModificarDeterioroEmisor_Salida salidaModificacion = new ModificarDeterioroEmisor_Salida();
			if(registrosModificar.size() > 0) {
				
				ModificarDeterioroEmisor modificarDeterioroEmisor = new ModificarDeterioroEmisor();
				ModificarDeterioroEmisor_Entrada entradaModificacion = new ModificarDeterioroEmisor_Entrada();
				entradaModificacion.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaModificacion.p_IdUsuario = entrada.p_IdUsuario;
				entradaModificacion.p_IdModulo = entrada.p_IdModulo;
				entradaModificacion.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				entradaModificacion.p_DeteriorosEmisores = registrosModificar;
				salidaModificacion = modificarDeterioroEmisor.logica(con, entradaModificacion);
				
				out.P_OUT_ERRORESCONTADOR = salidaModificacion.P_OUT_ERRORESCONTADOR;
				if(out.P_OUT_ERRORESCONTADOR > 0) return out;

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
			con.rollback();

		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::CargarArchivoDeterioroEmisor", finServicio - inicioServicio);
		return out;
	}

	private Map<String, DeterioroEmisor> getMapDeterioroEmisores(Connection con) throws SQLException {
		
		logger.debug("INICIO::CargarArchivoDeterioroEmisor.getMapDeterioroEmisores");
		Long inicioMetodo = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(1);
		SQL sql = new SQL();
		
		Map<String, DeterioroEmisor> map = sql.query(con, SQL_SELECT_DETERIORO_EMISOR, params, DeterioroEmisor.class).stream()
														.collect(Collectors.toMap(registro -> registro.Emisor, registro -> registro));
		
		Long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::CargarArchivoDeterioroEmisor.getMapDeterioroEmisores :", finMetodo - inicioMetodo);
		return map;

	}
	
	public static void main(String[] args) throws Exception {
		try {
			Connection con = PlataformaUtils.getPropertiesConnection("DESA", "connections.properties");
			try {
				Gson gson = Utils.getGsonBuilder();

				String jsonCrear = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\" ,"
						+ " \"p_DeteriorosEmisores\": ["
						+ "{\"IdDeterioroEmisor\":0, \"Emisor\": \"BCI\", \"FlagDeterioro\": \"S\", \"PorcentajeDeterioro\": \"11\", \"Fase\": \"2\", \"Accion\": \"\"},"
						+ "{\"IdDeterioroEmisor\": 10, \"Emisor\": \"BCI\", \"FlagDeterioro\": \"S\", \"PorcentajeDeterioro\": \"25.8\", \"Fase\": \"3\", \"Accion\": \"\"}"
						+ "]}";
				
				CargarArchivoDeterioroEmisor_Entrada entradaCrear = gson.fromJson(jsonCrear,CargarArchivoDeterioroEmisor_Entrada.class);

				CargarArchivoDeterioroEmisor demo = new CargarArchivoDeterioroEmisor();

				CargarArchivoDeterioroEmisor_Salida salidaCargaArchivoDeterioroEmisor = demo.logica(con, entradaCrear);
				System.out.println(salidaCargaArchivoDeterioroEmisor.toString());

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
