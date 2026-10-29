package cl.bcs.APV;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;

import cl.bcs.APV.AsignarContratoARecaudacion.AsignacionContratoRecaudacion;
import cl.bcs.APV.AsignarContratoARecaudacion.ParamsAsignacion_Insert;
import cl.bcs.APV.AsignarContratoARecaudacion.ParamsAsignacion_Update;
import cl.bcs.APV.BusquedaContratoSAAPVRecaudacion.AsignarContratoRecaudacion;
import cl.bcs.APV.BusquedaContratoSAAPVRecaudacion.EntradaBusquedaContratoSAAPVRecaudacion;
import cl.bcs.APV.BusquedaContratoSAAPVRecaudacion.SalidaBusquedaContratoSAAPVRecaudacion;
import cl.bcs.APV.pojo.DatosRecaudaciones;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ValidarYAsignarContratosARecaudaciones  implements JavaCall {
	static final Logger logger = LogManager.getLogger(ValidarYAsignarContratosARecaudaciones.class);
	
	private final String SQL_CREATE_TABLA_TEMPORAL = loadResourceToString("cl/bcs/APV/sql/ValidarYAsignarContratosARecaudaciones_CrearTablaTemporal.sql");
	private final String SQL_DELETE_TABLA_TEMPORAL = loadResourceToString("cl/bcs/APV/sql/ValidarYAsignarContratosARecaudaciones_BorrarTablaTemporal.sql");
	private final String SQL_DROP_TABLA_TEMPORAL = loadResourceToString("cl/bcs/APV/sql/ValidarYAsignarContratosARecaudaciones_DropTablaTemporal.sql");
	private final String SQL_INSERT_TABLA_TEMPORAL = loadResourceToString("cl/bcs/APV/sql/ValidarYAsignarContratosARecaudaciones_InsertarATablaTemporal.sql");
	private final String SQL_SELECT_TABLA_TEMPORAL = loadResourceToString("cl/bcs/APV/sql/ValidarYAsignarContratosARecaudaciones_ConsultarIdsContratosSAAPVFiltrandoPorTablaTemporal.sql");
	private final String SQL_SELECT_RECAUDACIONES_REZAGO = loadResourceToString("cl/bcs/APV/sql/ValidarYAsignarContratosARecaudaciones_ConsultarDatosRecaudacionesEnRezago.sql");
	private final String SQL_SELECT_ASIGNACION_CONTRATO_RECAUDACION = loadResourceToString("cl/bcs/APV/sql/AsignacionContratoRecaudacion_Select.sql");
	private final String SQL_INSERT_ASIGNACION_CONTRATO_RECAUDACION  = loadResourceToString("cl/bcs/APV/sql/AsignacionContratoRecaudacion_Insert.sql");
	private final String SQL_UPDATE_ASIGNACION_CONTRATO_RECAUDACION  = loadResourceToString("cl/bcs/APV/sql/AsignacionContratoRecaudacion_Update.sql");
	public String mensajesAdvertencia = "";
	
	public static class EntradaValidarYAsignarContratosARecaudaciones extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Filtros p_FiltrosRecaudacion;
		
		public EntradaValidarYAsignarContratosARecaudaciones() {
			this.p_FiltrosRecaudacion= new Filtros();
		}
	}
	
	public static class Filtros extends BcsPojo {
		public String p_FechaDesde;
		public String p_FechaHasta;
		public String p_RutCliente;
		public String p_RutEmpleador;
		public Long p_FolioRecaudacion;
		
		public Filtros() {
			this.p_FechaDesde="0001-01-01";
			this.p_FechaHasta="0001-01-01";
			this.p_RutCliente="";
			this.p_RutEmpleador="";
			this.p_FolioRecaudacion=0L;
		}
	}
	
	public static class SalidaValidarYAsignarContratosARecaudaciones extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_ADVERTENCIAS;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		
		public SalidaValidarYAsignarContratosARecaudaciones() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIAS = new ArrayList<MensajeSalida>();
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
		}
	}
	
	public static class ParametrosAsignacionContratoRecaudacion extends BcsPojo {
		public Long IdRecaudacionCliente;
		public Long IdContratoSAAPV;
		public Long FolioRecaudacion;
		public String FolioContratoSAAPV;
		
		public ParametrosAsignacionContratoRecaudacion() {
			this.IdRecaudacionCliente = 0L;
			this.IdContratoSAAPV = 0L;
			this.FolioRecaudacion = 0L;
			this.FolioContratoSAAPV = "";
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaValidarYAsignarContratosARecaudaciones jin = gson.fromJson(in.toString(), EntradaValidarYAsignarContratosARecaudaciones.class);
		SalidaValidarYAsignarContratosARecaudaciones out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}


	private SalidaValidarYAsignarContratosARecaudaciones logica(Connection conn, EntradaValidarYAsignarContratosARecaudaciones entrada) throws Exception {
		logger.debug("INICIO::ValidarYAsignarContratosARecaudaciones.logica");
	    long inicio = System.currentTimeMillis();
		
		SalidaValidarYAsignarContratosARecaudaciones salida = new SalidaValidarYAsignarContratosARecaudaciones();
		
		List<DatosRecaudaciones> recaudaciones  = obtenerEntidadesRecaudacionesYContratos(conn,entrada);
		
		List<ParametrosAsignacionContratoRecaudacion> parametrosAsignacion = new ArrayList<ParametrosAsignacionContratoRecaudacion>();
		
		List<DatosRecaudaciones> recaudacionesConContratoAAsignar = recaudaciones.parallelStream().filter(recaudacion -> recaudacion.IdContratoSAAPV > 0).collect(Collectors.toList());
		
		if(!recaudacionesConContratoAAsignar.isEmpty()) {
			for(DatosRecaudaciones conContrato : recaudacionesConContratoAAsignar) {
				ParametrosAsignacionContratoRecaudacion parametro = new ParametrosAsignacionContratoRecaudacion();
				parametro.IdRecaudacionCliente = conContrato.IdRecaudacionCliente;
				parametro.IdContratoSAAPV = conContrato.IdContratoSAAPV;
				parametro.FolioRecaudacion = conContrato.FolioRecaudacion;
				parametro.FolioContratoSAAPV = conContrato.FolioContratoSAAPV;
				
				parametrosAsignacion.add(parametro);
			}
		}
		
		if(!parametrosAsignacion.isEmpty()) {
			asignarContratosARecaudacion(conn, parametrosAsignacion);
			
			for(ParametrosAsignacionContratoRecaudacion asginacion : parametrosAsignacion) {
				HashMap<String,String> parametros = new HashMap<String,String>();
	    		parametros.put("IdRecaudacionCliente", String.valueOf(asginacion.IdRecaudacionCliente));
				parametros.put("IdContratoSAAPV", String.valueOf(asginacion.IdContratoSAAPV));
				parametros.put("FolioRecaudacion", String.valueOf(asginacion.FolioRecaudacion));
				parametros.put("FolioContratoSAAPV", String.valueOf(asginacion.FolioContratoSAAPV));

				MensajeSalida msg = new MensajeSalida(asginacion.IdRecaudacionCliente, MensajeSalida.MENSAJE_OK, "UI_ASIGNAR_CONTRATO_A_RECAUDACION", parametros, conn, entrada.p_IdEmpresa);
				salida.P_OUT_MENSAJES.add(msg);
			}
			
		}
		
		List<DatosRecaudaciones> recaudacionesSinContratoAAsignar = recaudaciones.stream().filter(recaudacion -> recaudacion.IdContratoSAAPV == 0 ).collect(Collectors.toList());
		
		if(!recaudacionesSinContratoAAsignar.isEmpty()) {
			for(DatosRecaudaciones sinContrato : recaudacionesSinContratoAAsignar) {
				HashMap<String,String> parametros = new HashMap<String,String>();
	    		parametros.put("IdRecaudacionCliente", String.valueOf(sinContrato.IdRecaudacionCliente));
	    		parametros.put("FolioRecaudacion", String.valueOf(sinContrato.FolioRecaudacion));

	    		MensajeSalida mensajeSalida = new MensajeSalida(sinContrato.IdRecaudacionCliente, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ERROR_ASIGNAR_CONTRATO_RECAUDACION", parametros, conn, entrada.p_IdEmpresa);
				salida.P_OUT_ADVERTENCIAS.add(mensajeSalida);
			}	
		}
		salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIAS.stream().count(); 
		
		long fin = System.currentTimeMillis();
	    logger.debug("FIN::ValidarYAsignarContratosARecaudaciones.logica", fin - inicio);
		return salida;
	}

	private void asignarContratosARecaudacion(Connection conn, List<ParametrosAsignacionContratoRecaudacion> parametrosAsignacion) throws Exception {
		logger.debug("INICIO::ValidarYAsignarContratosARecaudaciones.asignarContratosARecaudacion");
	    long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		Long time = System.currentTimeMillis();
		java.sql.Timestamp FechaHora = new java.sql.Timestamp(time);
		
		List<ParamsAsignacion_Insert> insertsAsignacion = new ArrayList<ParamsAsignacion_Insert>();
		List<ParamsAsignacion_Update> updatesAsignacion = new ArrayList<ParamsAsignacion_Update>();
		
		for (ParametrosAsignacionContratoRecaudacion fila : parametrosAsignacion) {
			
			
			HashMap<String,Object> Select_Asignacion = new HashMap<String,Object>();
			Select_Asignacion.put("IdRecaudacionCliente", fila.IdRecaudacionCliente);
			
			List<AsignacionContratoRecaudacion> asignacionContratoRecaudacion = sql.query(conn, SQL_SELECT_ASIGNACION_CONTRATO_RECAUDACION, Select_Asignacion, AsignacionContratoRecaudacion.class);

			if(asignacionContratoRecaudacion.size() > 0){
				ParamsAsignacion_Update Param_Asignacion = new ParamsAsignacion_Update();
				Param_Asignacion.IdRecaudacionCliente = fila.IdRecaudacionCliente;
				Param_Asignacion.IdContratoSAAPV = fila.IdContratoSAAPV;
				Param_Asignacion.FechaModificacion = FechaHora;
				updatesAsignacion.add(Param_Asignacion);
			}else {
				ParamsAsignacion_Insert Param_Asignacion = new ParamsAsignacion_Insert();
				Param_Asignacion.IdRecaudacionCliente = fila.IdRecaudacionCliente;
				Param_Asignacion.IdContratoSAAPV = fila.IdContratoSAAPV;
				Param_Asignacion.FechaCreacion = FechaHora; 
				Param_Asignacion.FechaModificacion = FechaHora;
				insertsAsignacion.add(Param_Asignacion);
			}
		}
		
		if(updatesAsignacion.size()>0) {
			sql.batch(conn,SQL_UPDATE_ASIGNACION_CONTRATO_RECAUDACION,updatesAsignacion,ParamsAsignacion_Update.class);	
		}
		
		if(insertsAsignacion.size()>0) {
			sql.batch(conn,SQL_INSERT_ASIGNACION_CONTRATO_RECAUDACION,insertsAsignacion,ParamsAsignacion_Insert.class);
		}
		
		long fin = System.currentTimeMillis();
	    logger.debug("FIN::ValidarYAsignarContratosARecaudaciones.asignarContratosARecaudacion", fin - inicio);
	}


	private List<DatosRecaudaciones> obtenerEntidadesRecaudacionesYContratos(Connection conn, EntradaValidarYAsignarContratosARecaudaciones entrada) throws Exception {
		logger.debug("INICIO::ValidarYAsignarContratosARecaudaciones.obtenerRecaudacionesYContratos");
	    long inicio = System.currentTimeMillis();
	    
	    List<DatosRecaudaciones> todasRecaudacionesRezago = obtenerRecaudacionesRezago(conn, entrada.p_FiltrosRecaudacion);
	    
	    generarTablaTemporal(conn, todasRecaudacionesRezago);
	    List<AsignarContratoRecaudacion> contratos =  obtenerIdsContratos(conn,todasRecaudacionesRezago,entrada);
	    
	    for(DatosRecaudaciones recaudacion : todasRecaudacionesRezago) {
	    	
	    	AsignarContratoRecaudacion busquedaContrato = contratos
		    												.parallelStream()
		    												.filter(con -> recaudacion.IdRecaudacionCliente.equals(con.IdRecaudacionCliente))
		    												.findFirst()
		    												.orElse(new AsignarContratoRecaudacion());
	    	
	    	recaudacion.IdContratoSAAPV = busquedaContrato.IdContratoSAAPV;
	    	recaudacion.FolioContratoSAAPV = busquedaContrato.FolioContratoSAAPV;
	    }
	    
	    long fin = System.currentTimeMillis();
	    logger.debug("FIN::ValidarYAsignarContratosARecaudaciones.obtenerRecaudacionesYContratos", fin - inicio);
		return todasRecaudacionesRezago;
	}

	private List<DatosRecaudaciones> obtenerRecaudacionesRezago(Connection conn, Filtros filtros) throws Exception {
		logger.debug("INICIO::ValidarYAsignarContratosARecaudaciones.obtenerRecaudacionesRezago");
	    long inicio = System.currentTimeMillis();
	    
		SQL sql = new SQL();
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("p_FechaDesde", filtros.p_FechaDesde);
		parametros.put("p_FechaHasta", filtros.p_FechaHasta);
		List<DatosRecaudaciones> rezagos = sql.query(conn, SQL_SELECT_RECAUDACIONES_REZAGO, parametros, DatosRecaudaciones.class);
		
		rezagos = filtrarRezagos(rezagos, filtros);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarYAsignarContratosARecaudaciones.obtenerRecaudacionesRezago", fin - inicio);
		return rezagos;
	}


	private List<DatosRecaudaciones> filtrarRezagos(List<DatosRecaudaciones> rezagos, Filtros filtros) {
		logger.debug("INICIO::ValidarYAsignarContratosARecaudaciones.filtrarRezagos");
	    long inicio = System.currentTimeMillis();
		
		if(filtros.p_FolioRecaudacion > 0) {
			rezagos = rezagos.parallelStream().filter(rec -> rec.FolioRecaudacion.equals(filtros.p_FolioRecaudacion)).collect(Collectors.toList());
		}
		
		if(!filtros.p_RutCliente.equals("")) {
			rezagos = rezagos.parallelStream().filter(rec -> rec.RutCliente.equals(filtros.p_RutCliente)).collect(Collectors.toList());
		}
		
		if(!filtros.p_RutEmpleador.equals("")) {
			rezagos = rezagos.parallelStream().filter(rec -> rec.RutEmpleador.equals(filtros.p_RutEmpleador)).collect(Collectors.toList());
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarYAsignarContratosARecaudaciones.filtrarRezagos", fin - inicio);
		return rezagos;
	}


	private List<AsignarContratoRecaudacion> obtenerIdsContratos(Connection conn, List<DatosRecaudaciones> recaudacionesEntrada, EntradaValidarYAsignarContratosARecaudaciones entrada) throws Exception {
		logger.debug("INICIO::ValidarYAsignarContratosARecaudaciones.obtenerIdsContratos");
	    long inicio = System.currentTimeMillis();
	   
	    SQL sql = new SQL();
	    
	    HashMap<String, Object> parametros = new HashMap<String, Object>();
		
	    List<Long> idsRecaudaciones = recaudacionesEntrada
	    								.stream()
	    								.map( rec -> rec.FolioRecaudacion)
	    								.collect(Collectors.toList());
	    
	    //List<DatosRecaudaciones> recaudaciones = sql.query(conn, SQL_SELECT_TABLA_TEMPORAL, parametros, DatosRecaudaciones.class);
		
	    BusquedaContratoSAAPVRecaudacion busquedaContratoSAAPVRecaudacion = new BusquedaContratoSAAPVRecaudacion();
		EntradaBusquedaContratoSAAPVRecaudacion entradaBusquedaContratoSAAPVRecaudacion = new EntradaBusquedaContratoSAAPVRecaudacion();
		entradaBusquedaContratoSAAPVRecaudacion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaBusquedaContratoSAAPVRecaudacion.p_IdUsuario = entrada.p_IdUsuario;
		entradaBusquedaContratoSAAPVRecaudacion.p_IdModulo	= entrada.p_IdModulo;
		entradaBusquedaContratoSAAPVRecaudacion.p_IdsRecaudacion = idsRecaudaciones;
		SalidaBusquedaContratoSAAPVRecaudacion salidaBusquedaContratoSAAPVRecaudacion = busquedaContratoSAAPVRecaudacion.logica(conn, entradaBusquedaContratoSAAPVRecaudacion);
	    
		sql.batch(conn, SQL_DROP_TABLA_TEMPORAL, recaudacionesEntrada, DatosRecaudaciones.class);
		
		long fin = System.currentTimeMillis();
	    logger.debug("FIN::ValidarYAsignarContratosARecaudaciones.obtenerIdsContratos", fin - inicio);
		return salidaBusquedaContratoSAAPVRecaudacion.P_OUT_RESULTADO;
	}


	private void generarTablaTemporal(Connection conn, List<DatosRecaudaciones> recaudacionesEntrada) throws Exception {
		logger.info("INICIO::ValidarYAsignarContratosARecaudaciones.generarTablaTemporal");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		SQL.exec(conn, SQL_CREATE_TABLA_TEMPORAL);
		
		sql.batch(conn, SQL_DELETE_TABLA_TEMPORAL, recaudacionesEntrada, DatosRecaudaciones.class);
		
		sql.batch(conn, SQL_INSERT_TABLA_TEMPORAL, recaudacionesEntrada, DatosRecaudaciones.class);
			
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ValidarYAsignarContratosARecaudaciones.generarTablaTemporal", finServicio - inicioServicio);
	}


	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		try (Connection conn = PlataformaUtils.getBOLSAConnection()){
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String entrada2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"APV\", \"p_FiltrosRecaudacion\": { \"p_FechaDesde\":\"2023-03-16\",\"p_FechaHasta\":\"2023-03-16\", \"p_FolioRecaudacion\":0} }";
			
			EntradaValidarYAsignarContratosARecaudaciones Objin = new EntradaValidarYAsignarContratosARecaudaciones();
			
			Objin = gson.fromJson(entrada2, EntradaValidarYAsignarContratosARecaudaciones.class);
			
			ValidarYAsignarContratosARecaudaciones demo = new ValidarYAsignarContratosARecaudaciones();
			
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaValidarYAsignarContratosARecaudaciones ObjOut = demo.logica( conn, Objin);
				
				conn.rollback();
//				con.commit();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				conn.rollback();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
		
	}
}
