package cl.bcs.portafolio;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class EliminarSinTransacciones implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(EliminarSinTransacciones.class);
	

	private final String SQL_SELECT_TRANSACCIONES_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/EliminarSinTransacciones_SelectTransaccionesPortafolio.sql");
	private final String SQL_SELECT_PORTAFOLIO 				 = loadResourceToString("cl/bcs/portafolio/sql/EliminarSinTransacciones_SelectPortafolio.sql");
	private final String SQL_UPDATE_PORTAFOLIO 			     = loadResourceToString("cl/bcs/portafolio/sql/EliminarSinTransacciones_ModificarPortafolio.sql");
	
	public static class EntradaEliminarSinTransacciones extends BcsPojo{
		// Entradas fijas servicios publicos
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_UsaReglasNegocio;
		
		// Entradas del servicio
		public Long	    p_IdCliente;
		public Long     p_IdPortafolio;
		public String   p_Comentario;
	}
	
	public static class SalidaEliminarSinTransacciones extends BcsPojo{
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public SalidaEliminarSinTransacciones(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;;
		}	
	}
	public static class Portafolio extends BcsPojo{
		public Long IdPortafolio;
		public Date FechaModificacion;
		public Long IdPortafolioDefecto;
		public String ComentarioBloqueo;
		public String CodigoPortafolio;
	}
	
	
	public static class TransaccionesPortafolio extends BcsPojo{
		public Long Cantidad;
		public String Tabla;
	}

	/*
	 * Metodo de consulta general
	*/
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarSinTransacciones jin = gson.fromJson(in.toString(), EntradaEliminarSinTransacciones.class);
		SalidaEliminarSinTransacciones salida = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}
	

	/*
	 * Logica
	*/
	public SalidaEliminarSinTransacciones logica(Connection con, EntradaEliminarSinTransacciones entrada) throws Exception {
		logger.info("INICIO::EliminarSinTransacciones");
		long inicioServicio = System.currentTimeMillis();
		SalidaEliminarSinTransacciones salida = new SalidaEliminarSinTransacciones();
		List<Portafolio> portafolios = this.obtenerPortafolioCliente(con, entrada);
		// Validamos el portafolio
		boolean portafolioExiste = this.validarPortafolioExiste(portafolios, entrada, salida);
		if (!portafolioExiste) {
			return salida;
		}
		
		// Validamos las transacciones
		List<TransaccionesPortafolio> listadoTransacciones = this.obtenerTrasaccionesPortafolio(con, entrada);
		boolean tieneTransacciones = validarListadoTransacciones(listadoTransacciones, entrada, salida); 
		if (tieneTransacciones) {
			return salida;
		}

		// Modificamos el portafolio
		this.modificarPortafolio(con, entrada, salida, portafolios);

		// Escribimos en el log
		this.escribirLog(con, entrada, salida, portafolios);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarSinTransacciones", finServicio - inicioServicio);
		return salida;
	}
	
	
	
	private void escribirLog(Connection con, EntradaEliminarSinTransacciones entrada, SalidaEliminarSinTransacciones salida, List<Portafolio> portafolios) throws Exception {
		logger.debug("INICIO::EliminarSinTransacciones.escribirLog");
		long inicio = System.currentTimeMillis();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdEmpresa= entrada.p_IdEmpresa;
		entradaAgregarLog.p_IdModulo = "SOC";
		entradaAgregarLog.p_IdUsuario= entrada.p_IdUsuario;		
		entradaAgregarLog.p_UsaReglasNegocio= "N";
		entradaAgregarLog.p_IdEstadoAutorizacion="";
		
		// Generamos el log
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.IdCabeceraLog = entrada.p_IdPortafolio;
		log.IdTipoLog = "BORRADO";
		log.IdModulo = entrada.p_IdModulo;
		log.IdUsuario = entrada.p_IdUsuario;
		log.IdEmpresa = entrada.p_IdEmpresa;
		log.FechaSistemaLog = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
		log.IdUsuarioAtendido = entrada.p_IdUsuario;
		log.IdCliente = entrada.p_IdCliente;
		log.IdPortafolio = entrada.p_IdPortafolio;
		log.IdTipoRegistro = "PORTAFOLIO";
		log.IdProceso = 0L;
		entradaAgregarLog.p_Log.add(log);

		// Detalle log
		InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();
		detLog.IdCabeceraLog = entrada.p_IdPortafolio;
		detLog.IdTrabajo=0L;
		detLog.SubRegistro = "PORTAFOLIO";
		detLog.CampoRegistro = "Borrado";
		detLog.ValorAntiguo = "N";
		detLog.ValorNuevo = "S";
		entradaAgregarLog.p_LogDetalle.add(detLog);

		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarSinTransacciones.escribirLog", fin - inicio);
	}

	private void modificarPortafolio(Connection con, EntradaEliminarSinTransacciones entrada, SalidaEliminarSinTransacciones salida, List<Portafolio> portafolios) throws Exception {
		logger.debug("INICIO::EliminarSinTransacciones.modificarPortafolio");
		long inicio = System.currentTimeMillis();
		Portafolio portafolioBorrar = new Portafolio();
		for (Portafolio portafolio : portafolios) {
			if (portafolio.IdPortafolio.equals(entrada.p_IdPortafolio)) {
				portafolioBorrar = portafolio;
				break;
			}
		}
		Portafolio portafolioParams = new Portafolio();
		portafolioParams.IdPortafolio = entrada.p_IdPortafolio;
		portafolioParams.FechaModificacion = Fecha.toDate(Utils.getFechaHoraSistema(con, Utils.getSchema(), entrada.p_IdEmpresa));
		portafolioParams.ComentarioBloqueo = entrada.p_Comentario;
		portafolioParams.CodigoPortafolio = portafolioBorrar.CodigoPortafolio+"-BORRADO";
		SQL sql = new SQL();
		int[] result = sql.batch(con, SQL_UPDATE_PORTAFOLIO, Arrays.asList(portafolioParams), Portafolio.class);
		if(result != null && result.length > 0 && result[0] == 1){
			salida.P_OUT_MENSAJES.add(new MensajeSalida(entrada.p_IdEmpresa, MensajeSalida.MENSAJE_OK,"EXITO_ELIMINAR_PORTAFOLIO" ,"UI_EXITO_ELIMINAR_PORTAFOLIO"));
			
		}else{
			salida.P_OUT_MENSAJES.add(new MensajeSalida(entrada.p_IdEmpresa, MensajeSalida.MENSAJE_ERROR,"ERROR_ELIMINAR_PORTAFOLIO" ,"UI_ERROR_ELIMINAR_PORTAFOLIO"));
		}
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarSinTransacciones.modificarPortafolio", fin - inicio);
	}
	
	private boolean validarListadoTransacciones(List<TransaccionesPortafolio> listadoTransacciones,EntradaEliminarSinTransacciones entrada, SalidaEliminarSinTransacciones salida) {
		logger.trace("INICIO::EliminarSinTransacciones.validarListadoTransacciones");
		long inicio = System.currentTimeMillis();
		boolean tieneTransacciones = false;
		HashMap<String, Long > cantidadPortafolios = new HashMap<String, Long >();
		listadoTransacciones.stream().forEach(transaccion -> {
			if (transaccion.Cantidad == 0) {
				return;
	        }
			if (cantidadPortafolios.containsKey(transaccion.Tabla)) {
				cantidadPortafolios.put(transaccion.Tabla, cantidadPortafolios.get(transaccion.Tabla) + 1);
			} else {
				cantidadPortafolios.put(transaccion.Tabla, 1L);
			}
		});

		for (String tabla : cantidadPortafolios.keySet()) {
			if (cantidadPortafolios.get(tabla) > 0) {
				tieneTransacciones = true;
				String mensajeError = "UI_ERROR_PORTAFOLIO_OPERACIONES_" + tabla;
				MensajeSalida mensaje = new MensajeSalida(MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) + 1, MensajeSalida.MENSAJE_ERROR,"ERROR_ELIMINAR_PORTAFOLIO" ,mensajeError);
				salida.P_OUT_MENSAJES.add(mensaje);
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EliminarSinTransacciones.validarListadoTransacciones", fin - inicio);
		return tieneTransacciones;
	}

	private boolean validarPortafolioExiste(List<Portafolio> portafolios, EntradaEliminarSinTransacciones entrada, SalidaEliminarSinTransacciones salida) throws Exception {
		logger.trace("INICIO::EliminarSinTransacciones.validarPortafolioExiste");
		boolean existe = false;
		for (Portafolio portafolio : portafolios) {
			if (portafolio.IdPortafolio.equals(entrada.p_IdPortafolio)) {				
				existe = true;
				return existe;
			}
		}
		salida.P_OUT_MENSAJES.add(new MensajeSalida(entrada.p_IdEmpresa, MensajeSalida.MENSAJE_ERROR,"ERROR_ELIMINAR_PORTAFOLIO" ,"UI_ERROR_PORTAFOLIO_NO_EXISTE"));
		salida.P_OUT_ERRORESCONTADOR = 1L;
		logger.debug("FIN::EliminarSinTransacciones.validarPortafolioExiste");
		return existe;
	}	
	
	private List<Portafolio> obtenerPortafolioCliente(Connection con, EntradaEliminarSinTransacciones entrada) throws Exception {
		logger.debug("INICIO::EliminarSinTransacciones.obtenerPortafolioCliente");
		long inicio = System.currentTimeMillis();
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdCliente",entrada.p_IdCliente);
		SQL sql = new SQL();
		List<Portafolio> portafolios = sql.query(con, SQL_SELECT_PORTAFOLIO, params, Portafolio.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarSinTransacciones.obtenerPortafolioCliente", fin - inicio);
		return portafolios;
	}
	
	
	private List<TransaccionesPortafolio> obtenerTrasaccionesPortafolio(Connection con, EntradaEliminarSinTransacciones entrada) throws Exception {
		logger.debug("INICIO::EliminarSinTransacciones.obtenerTrasaccionesPortafolio");
		long inicio = System.currentTimeMillis();
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPortafolio",entrada.p_IdPortafolio);
		SQL sql = new SQL();
		List<TransaccionesPortafolio> ListadoIdPortafoliosMesa = sql.query(con, SQL_SELECT_TRANSACCIONES_PORTAFOLIO, params, TransaccionesPortafolio.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarSinTransacciones.obtenerTrasaccionesPortafolio", fin - inicio);
		return ListadoIdPortafoliosMesa;
	}
	
	
	public static void main(String[] args) throws Exception { 
		logger.info("INICIO::EliminarSinTransacciones.main");
	try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","C:/GIT/optimus-corredores/connections.properties")) {
			
			String entradaTxt = "{\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 97,\r\n"
					+ "  \"p_IdModulo\": \"SOC\",\r\n"
					+ "  \"p_UsaReglasNegocio\": \"S\",\r\n"
					+ "  \"p_IdPortafolio\": \"267\",\r\n"
					+ "  \"p_IdCliente\": 5631,\r\n"
					+ "  \"p_Comentario\": \"PRUEBA\"\r\n"
					+ "}";
			Gson gson = Utils.getGsonBuilder();
			EntradaEliminarSinTransacciones entrada = gson.fromJson(entradaTxt, EntradaEliminarSinTransacciones.class);			
			EliminarSinTransacciones demo = new EliminarSinTransacciones();
			try {
				SalidaEliminarSinTransacciones salida = demo.logica(con, entrada);
				System.out.println(salida);
				con.rollback();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
		logger.info("FIN::EliminarSinTransacciones.main"); 
	}
	
}