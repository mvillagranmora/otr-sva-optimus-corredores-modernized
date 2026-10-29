package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarFondoIndustria  implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ActualizarFondoIndustria.class);
	private final String SQL_INSERT_FONDOS_INDUSTRIA = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarFondoIndustria_FondoIndustria_Insert.sql");
	private final String SQL_UPDATE_FONDOS_INDUSTRIA = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarFondoIndustria_FondoIndustria_Update.sql");
	private final String SQL_SELECT_FONDO_INDUSTRIA = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarFondoIndustria_FondoIndustria_Select.sql");

	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaActualizarFondoInsdustria extends BcsPojo{
		
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Accion;
		public List<FondoIndustria> p_FondosIndustria;
		
		public EntradaActualizarFondoInsdustria() {
			this.p_FondosIndustria = new ArrayList<FondoIndustria>();
		}		
	}
	
	public static class SalidaActualizarFondoInsdustria extends BcsPojo{
		
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public SalidaActualizarFondoInsdustria() {
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class FondoIndustria  extends BcsPojo{
		public Long		IdFondoIndustria;
		public String  RunFFMM;
		public String  NombreFFMM;
		public String  SociedadAdmFFMM;
		public String  RutSociedadAdmFFMM;
		public String  IdEstado;
		public String	IdMoneda;
		
		public FondoIndustria () {
			this.IdFondoIndustria = 0L;
			this.IdEstado = "INA";
			this.IdMoneda = "CLP";
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaActualizarFondoInsdustria jin = gson.fromJson(in.toString(), EntradaActualizarFondoInsdustria.class);
		SalidaActualizarFondoInsdustria out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	private SalidaActualizarFondoInsdustria logica(Connection conn, EntradaActualizarFondoInsdustria in) throws Exception {
		logger.info("INICIO::ActualizarFondoInsdustria ");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaActualizarFondoInsdustria salida = new SalidaActualizarFondoInsdustria ();
		
		if(in.p_Accion.equals("ADD")) {
			salida = creaFondosIndustria(conn, in);
			if(salida.P_OUT_ERRORESCONTADOR > 0L) {
				return salida;
			}
		}
		if(in.p_Accion.equals("MOD")) {
			salida = modificaFondosIndustria(conn, in);
			if(salida.P_OUT_ERRORESCONTADOR > 0L) {
				return salida;
			}
		}		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ActualizarFondoInsdustria ",finServicio - inicioServicio);
		return salida;
	}
	
	
	private SalidaActualizarFondoInsdustria creaFondosIndustria(Connection con, EntradaActualizarFondoInsdustria in) throws Exception {
		logger.debug("INICIO::ActualizarFondoInsdustria.creacionFondosIndustria");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaActualizarFondoInsdustria salidaControlada = new SalidaActualizarFondoInsdustria();
		
		List<FondoIndustria> fondosExistentes = consultarFondosIndustriaExistente(con, in.p_FondosIndustria);
		
		List<FondoIndustria> fondosNuevos = new ArrayList<FondoIndustria>();
		List<FondoIndustria> fondosInactivos = new ArrayList<FondoIndustria>();
		
		if(fondosExistentes.size() > 0) {
			HashMap<String,String> paramsError = new HashMap<String,String>();
			for(FondoIndustria fondoEntrada : in.p_FondosIndustria) {
				if(fondosExistentes.stream().anyMatch(fondo-> fondo.RunFFMM.equals(fondoEntrada.RunFFMM) && fondo.IdMoneda.equals(fondoEntrada.IdMoneda) && (_c.EstadoActivo).equals(fondo.IdEstado))) {
					paramsError.put("RunFFMM",fondoEntrada.RunFFMM);
					paramsError.put("IdMoneda",fondoEntrada.IdMoneda);
					salidaControlada.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FONDO_INDUSTRIA_EXISTE",paramsError,con,in.p_IdEmpresa));
					paramsError.clear();
					continue;
				}
				
				if(!fondosExistentes.stream().anyMatch(fondo-> fondo.RunFFMM.equals(fondoEntrada.RunFFMM) && fondo.IdMoneda.equals(fondoEntrada.IdMoneda))) {
					fondosNuevos.add(fondoEntrada);
				} else if(fondosExistentes.stream().anyMatch(fondo-> fondo.RunFFMM.equals(fondoEntrada.RunFFMM) && fondo.IdMoneda.equals(fondoEntrada.IdMoneda) && !(_c.EstadoActivo).equals(fondo.IdEstado))) {
					fondoEntrada.IdFondoIndustria = fondosExistentes.stream().filter(fondo-> fondo.RunFFMM.equals(fondoEntrada.RunFFMM) && fondo.IdMoneda.equals(fondoEntrada.IdMoneda) && !(_c.EstadoActivo).equals(fondo.IdEstado)).findFirst().get().IdFondoIndustria;
					fondosInactivos.add(fondoEntrada);
				}
			}
			salidaControlada.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaControlada.P_OUT_MENSAJES);
		} else {
			fondosNuevos.addAll(in.p_FondosIndustria);
		}

		if(salidaControlada.P_OUT_ERRORESCONTADOR > 0L) {
			return salidaControlada;
		}
			
		if(!fondosInactivos.isEmpty()) {
			EntradaActualizarFondoInsdustria entradaModificar = new EntradaActualizarFondoInsdustria();
			entradaModificar.p_FondosIndustria = fondosInactivos;
			
			this.editarFondoInsutria(con, entradaModificar, salidaControlada);
		}
		
		if(!fondosNuevos.isEmpty()) {
			EntradaActualizarFondoInsdustria entradaCrear = new EntradaActualizarFondoInsdustria();

			List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FondosIndustria", Long.valueOf(fondosNuevos.size()));
			AtomicInteger indice = new AtomicInteger();
			fondosNuevos.stream().forEach(fondo -> {
				fondo.IdFondoIndustria = secuencias.get(indice.getAndIncrement());
			});
			
			entradaCrear.p_FondosIndustria = fondosNuevos;
			this.agregarFondoInsutria(con, entradaCrear, salidaControlada);
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ActualizarFondoInsdustria.creacionFondosIndustria ",finServicio - inicioServicio);
		return salidaControlada;
	}
	
	private SalidaActualizarFondoInsdustria modificaFondosIndustria(Connection con, EntradaActualizarFondoInsdustria in) throws Exception {
		logger.debug("INICIO::ActualizarFondoInsdustria.modificaFondosIndustria");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaActualizarFondoInsdustria salidaControlada = new SalidaActualizarFondoInsdustria();
		
		List<FondoIndustria> fondosExistentes = consultarFondosIndustriaExistente(con, in.p_FondosIndustria);
		List<FondoIndustria> fondosActualizables = new ArrayList<FondoIndustria>();
		
		if(fondosExistentes.size() > 0) {
			HashMap<String,String> paramsError = new HashMap<String,String>();
			for(FondoIndustria fondoEntrada : in.p_FondosIndustria) {
				if(fondosExistentes.stream().anyMatch(fondo-> fondo.RunFFMM.equals(fondoEntrada.RunFFMM) && fondo.IdMoneda.equals(fondoEntrada.IdMoneda) && (_c.EstadoActivo).equals(fondo.IdEstado) && !fondoEntrada.IdFondoIndustria.equals(fondo.IdFondoIndustria))) {
					paramsError.put("RunFFMM",fondoEntrada.RunFFMM);
					paramsError.put("IdMoneda",fondoEntrada.IdMoneda);
					salidaControlada.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FONDO_INDUSTRIA_EXISTE",paramsError,con,in.p_IdEmpresa));
					paramsError.clear();	
					continue;
				}
				
				fondosActualizables.add(fondoEntrada);
			}
		}
		
		if(salidaControlada.P_OUT_ERRORESCONTADOR > 0L) {
			return salidaControlada;
		}
		
		if(!fondosActualizables.isEmpty()) {
			EntradaActualizarFondoInsdustria entradaModificar = new EntradaActualizarFondoInsdustria();
			entradaModificar.p_FondosIndustria = fondosActualizables;
			
			this.editarFondoInsutria(con, entradaModificar, salidaControlada);
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ActualizarFondoInsdustria.modificaFondosIndustria ",finServicio - inicioServicio);
		return salidaControlada;
	}
	
	private List<FondoIndustria> consultarFondosIndustriaExistente(Connection conn, List<FondoIndustria> p_FondosIndustria) throws Exception {
		logger.debug("INICIO::ActualizarFondoInsdustria.consultarFondosIndustriaExistente");
		long inicioServicio = System.currentTimeMillis();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		List<String> fondos = p_FondosIndustria.stream().map(fondo -> {
			return fondo.RunFFMM;
		}).collect(Collectors.toList());
		
		params.put("RunFFMM", fondos);
		
		SQL sql=new SQL();
		List<FondoIndustria> fondosExistentes = sql.query(conn, SQL_SELECT_FONDO_INDUSTRIA, params, FondoIndustria.class);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ActualizarFondoInsdustria.consultarFondosIndustriaExistente ",finServicio - inicioServicio);
		return fondosExistentes;
	}

	public void agregarFondoInsutria (Connection con, EntradaActualizarFondoInsdustria in, SalidaActualizarFondoInsdustria out)  throws Exception {
		logger.debug("INICIO::ActualizarFondoInsdustria.agregarFondoInsutria");
		long inicioServicio = System.currentTimeMillis();		
		SQL sql = new SQL();	
		
		int[] res = sql.batch(con, SQL_INSERT_FONDOS_INDUSTRIA, in.p_FondosIndustria, FondoIndustria.class);
		if(res==null || res.length==0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"ERROR_INGRESO_FONDOS_INSUDTRIA","Error al intentar ingresar el fondo nuevo"));
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);			
		}else {
			out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_OK,"FONDO_CREADO","El fondo se creado correctamente"));
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ActualizarFondoInsdustria.agregarFondoInsutria ",finServicio - inicioServicio);	
		
	}
	
	public void editarFondoInsutria (Connection con, EntradaActualizarFondoInsdustria in, SalidaActualizarFondoInsdustria out)throws Exception {
		logger.debug("INICIO::ActualizarFondoInsdustria.agregarFondoInsutria");
		long inicioServicio = System.currentTimeMillis();		
		SQL sql = new SQL();	
		
		int[] res = sql.batch(con, SQL_UPDATE_FONDOS_INDUSTRIA, in.p_FondosIndustria, FondoIndustria.class);
		if(res==null || res.length==0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"ERROR_ACTUALIZAR_FONDOS_INSUDTRIA","Error al intentar actualizar fondo"));
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);			
		}else {
			out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_OK,"FONDO_ACTUALIZADO","El fondo se actualizo correctamente"));
		}

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ActualizarFondoInsdustria.agregarFondoInsutria ",finServicio - inicioServicio);	
	}
	
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			ActualizarFondoIndustria demo = new ActualizarFondoIndustria();
			try {
				EntradaActualizarFondoInsdustria Objin = new EntradaActualizarFondoInsdustria();

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				
				String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_Accion\":\"MOD\",\"p_FondosIndustria\":[{\"RunFFMM\":\"9994-5\",\"NombreFFMM\":\"AMERIS LIQUIDEZ FONDO MUTUO\",\"SociedadAdmFFMM\":\"AMERIS CAPITAL ADMINISTRADORA GENERAL DE FONDOS S.A. TEST\",\"RutSociedadAdmFFMM\":\"76503273-3\",\"IdEstado\":\"ACT\"}]}"; 
				
				Objin = gson.fromJson(json, EntradaActualizarFondoInsdustria.class);				

				try {

					SalidaActualizarFondoInsdustria ObjOut = demo.logica(con, Objin);
					System.out.println(ObjOut);
					con.rollback(); 
					//con.commit();
				} catch (Exception er) {
					er.printStackTrace();
					con.rollback(); 
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("== END ==");
		};
	}

}
