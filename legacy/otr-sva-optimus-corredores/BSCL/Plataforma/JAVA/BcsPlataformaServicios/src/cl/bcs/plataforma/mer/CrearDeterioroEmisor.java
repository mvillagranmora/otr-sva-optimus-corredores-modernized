package cl.bcs.plataforma.mer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrearDeterioroEmisor implements JavaCall {

	private static final Logger logger = LogManager.getLogger(CrearDeterioroEmisor.class);

	private static final FileLoader LOADER = new FileLoader();

	private static final String SQL_INSERT_DETERIORO_EMISOR = LOADER.loadResourceToString("cl/bcs/plataforma/mer/sql/CrearDeterioroEmisor_InsertDeterioroEmisor.sql");

	public static class CrearDeterioroEmisor_Entrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
	    public String p_IdModulo;
	    public String p_UsaReglasNegocio;
	    public List<DeterioroEmisor> p_DeteriorosEmisores;

		public CrearDeterioroEmisor_Entrada() {
			this.p_DeteriorosEmisores = new ArrayList<DeterioroEmisor>();
		}
	}
	
	public static class CrearDeterioroEmisor_Salida extends BcsPojo {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public CrearDeterioroEmisor_Salida() {
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
		CrearDeterioroEmisor_Entrada in = gson.fromJson(jin.toString(),CrearDeterioroEmisor_Entrada.class);
		CrearDeterioroEmisor_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public CrearDeterioroEmisor_Salida logica(Connection con, CrearDeterioroEmisor_Entrada entrada) throws Exception {
		
		logger.info("INICIO::CrearDeterioroEmisor");
		Long inicioServicio = System.currentTimeMillis();
		
		CrearDeterioroEmisor_Salida out = new CrearDeterioroEmisor_Salida();
		
		try {
			
			List<DeterioroEmisor> registrosInsertar = obtenerRegistros(con, entrada);
			
			out.agregarValidacionesSalida(ValidarDeterioroEmisor.validarRegistroDeterioroEmisor(con, registrosInsertar, entrada.p_IdEmpresa));
			if(out.P_OUT_ERRORESCONTADOR > 0) return out;

			int resultadoInsercion [] = insertarDeterioros(con, registrosInsertar);
			
			if (ValidarDeterioroEmisor.validarResultadoSQL(resultadoInsercion)) {
				crearLogInsercion(con, entrada, registrosInsertar);
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
		logger.info("FIN::CrearDeterioroEmisor", finServicio - inicioServicio);
		return out;
	}
	
	private List<DeterioroEmisor> obtenerRegistros(Connection con, CrearDeterioroEmisor_Entrada entrada) throws Exception {

		logger.trace("INICIO::CrearDeterioroEmisor.obtenerRegistros");
		Long inicioServicio = System.currentTimeMillis();

		List<Long> secuenciasDeterioroEmisor = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::MER.IdDeterioroEmisor", (long)entrada.p_DeteriorosEmisores.size());

		List<DeterioroEmisor> registrosInsertar = new ArrayList<DeterioroEmisor>();
		Integer i = 0;
		
		for (DeterioroEmisor deterioro : entrada.p_DeteriorosEmisores) {
				DeterioroEmisor deterioroEmisor = new DeterioroEmisor();
				deterioroEmisor.IdDeterioroEmisor = secuenciasDeterioroEmisor.get(i);
				deterioroEmisor.Emisor = deterioro.Emisor.toUpperCase();
				deterioroEmisor.FlagDeterioro = deterioro.FlagDeterioro;
				deterioroEmisor.PorcentajeDeterioro = deterioro.PorcentajeDeterioro;
				deterioroEmisor.Fase = deterioro.Fase;
				deterioroEmisor.Accion = "CREAR";
				registrosInsertar.add(deterioroEmisor);
				i++;
		}

		Long finServicio = System.currentTimeMillis();
		logger.trace("FIN::CrearDeterioroEmisor.obtenerRegistros", finServicio - inicioServicio);
		return registrosInsertar;

	}
	
	private int [] insertarDeterioros(Connection con, List<DeterioroEmisor> registrosInsertar) throws Exception{
		
		logger.debug("INICIO::CrearDeterioroEmisor.insertarDeterioros");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		int [] resultado = sql.batch(con, SQL_INSERT_DETERIORO_EMISOR, registrosInsertar, DeterioroEmisor.class);
		
		Long finServicio = System.currentTimeMillis();
		logger.debug("FIN::CrearDeterioroEmisor.insertarDeterioros", finServicio - inicioServicio);
		return resultado;
		
	}
	
	private void crearLogInsercion(Connection con, CrearDeterioroEmisor_Entrada entrada, List<DeterioroEmisor> registros) throws Exception {
		
		logger.debug("INICIO::CrearDeterioroEmisor.crearLogInsercion");
		Long inicioServicio = System.currentTimeMillis();
		
		GenerarLogDeterioroEmisor_Entrada entradaLog = new GenerarLogDeterioroEmisor_Entrada();
		GenerarLogDeterioroEmisor log = new GenerarLogDeterioroEmisor();
		
		entradaLog.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaLog.p_IdModulo = entrada.p_IdModulo;
		entradaLog.p_IdUsuario = entrada.p_IdUsuario;
		entradaLog.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaLog.registros = registros;
		entradaLog.TipoLog = "CREACION";
		
		log.logica(con, entradaLog);
		
		Long finServicio = System.currentTimeMillis();
		logger.debug("FIN::CrearDeterioroEmisor.crearLogInsercion", finServicio - inicioServicio);
		
	}

	public static void main(String[] args) throws Exception {

		try {
			Connection con = PlataformaUtils.getPropertiesConnection("DESA", "connections.properties");
			try {
				Gson gson = Utils.getGsonBuilder();

				String jsonCrear = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\" ,"
						+ " \"p_DeteriorosEmisores\": ["
						+ "{\"IdDeterioroEmisor\":0, \"Emisor\":\"AGUAS\", \"FlagDeterioro\": \"S\", \"PorcentajeDeterioro\": \"0.1231\", \"Fase\": \"1\", \"Accion\": \"MODIFICAR\",\"IdEstado\": \"e\"},"
						+ "{\"IdDeterioroEmisor\": 0, \"Emisor\": \"BTU\", \"FlagDeterioro\": \"S\", \"PorcentajeDeterioro\": \"27.8\", \"Fase\": \"3\", \"Accion\": \"MODIFICAR\", \"IdEstado\": \"e\"}]}";
				CrearDeterioroEmisor_Entrada entradaCrear = gson.fromJson(jsonCrear,CrearDeterioroEmisor_Entrada.class);

				CrearDeterioroEmisor demo = new CrearDeterioroEmisor();

				CrearDeterioroEmisor_Salida salidaModificar = demo.logica(con, entradaCrear);
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
