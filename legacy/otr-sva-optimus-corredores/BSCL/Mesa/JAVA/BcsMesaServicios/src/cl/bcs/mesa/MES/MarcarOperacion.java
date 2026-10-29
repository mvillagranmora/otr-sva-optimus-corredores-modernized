package cl.bcs.mesa.MES;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MES.MarcarOperacion.MarcarOperacion_OUT;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class MarcarOperacion implements JavaCall{

	private static final Logger logger = LogManager.getLogger(MarcarOperacion.class);
	
	private final String SQL_SELECT_OPERACIONES = loadResourceToString("cl/bcs/mesa/MES/sql/MarcarOperaciones_SelectOperaciones.sql");
	private final String SQL_UPDATE_MARCAR_OPERACIONES = loadResourceToString("cl/bcs/mesa/MES/sql/MarcarOperaciones_UpdateOperaciones.sql");
	private final String SQL_UPDATE_BLOQUEO_USUARIO_EDITOR = loadResourceToString("cl/bcs/mesa/MES/sql/MarcarOperaciones_UpdateBloqueoUsuarioEditor.sql");
	private final String SQL_SELECT_AUTORIZACION_AGENTE_OPERADOR_MESA = loadResourceToString("cl/bcs/mesa/MES/sql/MarcarOperaciones_SelectAutorizacionAgenteOperadorMesa.sql");
	private final String SQL_SELECT_CODIGO_NOMBRE_USUARIO = loadResourceToString("cl/bcs/mesa/MES/sql/MarcarOperaciones_SelectCodigoNombreUsuario.sql");
	private final String SQL_SELECT_ES_SUPERVISOR = loadResourceToString("cl/bcs/mesa/MES/sql/MarcarOperaciones_SelectEsSupervisor.sql");

	public static class MarcarOperacionObjIn {
		public String Accion;
		public Long IdOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class MarcarOperacion_IN {
		public Long p_IdEmpresa;
	    public Long p_IdUsuario;
	    public String p_IdModulo;
	    public List<MarcarOperacionObjIn> p_Operaciones;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class MarcarOperacionObjOut {
		public Long IdOperacion;
		public Timestamp FechaEdicion;
		public Long IdUsuarioEdicion;
		public String EstadoEdicion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class MarcarOperacion_OUT {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<MarcarOperacionObjOut>		P_OUT_OPERACIONES;
		public MarcarOperacion_OUT() {
			this.P_OUT_OPERACIONES 		= new ArrayList<MarcarOperacionObjOut>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Operacion {
		public Long IdOperacion;
		public Long FolioOperacion;
		public Date FechaOperacion;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public Long IdMesaOrigen;
        public Long IdUsuarioCreacion;
        public Long IdUsuarioEdicion;
        public String IdEstado;
        public Timestamp FechaEdicion;
        public String BloqueoPorEdicion;
        public String CodigoUsuario;
        public String CodigoUsuarioBloqueoEdicion;
        public Timestamp FechaHoraInicioEdicion;

        public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class AutorizacionAgenteOperadorMesa {
		public String ModificaOperacionOtroUsuario;
	}

	public static class CodigoNombreUsuario {
		public String CodigoUsuario;
		public String NombreUsuarioCreacion;
	}

	public static class SupervisorObj {
		public Long IdEmpleadoSupervisor;
	}

	private boolean esPactoObjetivo(Operacion ope) {
		return "MERF".equals(ope.IdTipoProducto)
				&& ("COMPAC".equals(ope.IdTipoOperacion) || "VENPAC".equals(ope.IdTipoOperacion));
	}

	private boolean consultarAutorizacionAgenteOperadorMesa(Connection conn, Long idEmpresa, Long idUsuario, Long idMesaOrigen) throws Exception {
		SQL sqlAuth = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdUsuario", idUsuario);
		params.put("IdEmpresa", idEmpresa);
		params.put("IdMesaOrigen", idMesaOrigen != null ? idMesaOrigen : -1L);
		List<AutorizacionAgenteOperadorMesa> resultado = sqlAuth.query(conn, SQL_SELECT_AUTORIZACION_AGENTE_OPERADOR_MESA, params, AutorizacionAgenteOperadorMesa.class);
		return !resultado.isEmpty() && "S".equals(resultado.get(0).ModificaOperacionOtroUsuario);
	}

	private CodigoNombreUsuario consultarCodigoNombreUsuario(Connection conn, Long idUsuario) throws Exception {
		if (idUsuario == null) {
			return null;
		}
		SQL sqlUsuario = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdUsuario", idUsuario);
		List<CodigoNombreUsuario> resultado = sqlUsuario.query(conn, SQL_SELECT_CODIGO_NOMBRE_USUARIO, params, CodigoNombreUsuario.class);
		return resultado.isEmpty() ? null : resultado.get(0);
	}

	private String consultarNombreUsuarioCreacion(Connection conn, Long idUsuarioCreacion) throws Exception {
		CodigoNombreUsuario datos = consultarCodigoNombreUsuario(conn, idUsuarioCreacion);
		return (datos != null && datos.NombreUsuarioCreacion != null) ? datos.NombreUsuarioCreacion : "";
	}

	private String consultarCodigoUsuarioEditor(Connection conn, Long idUsuario) throws Exception {
		CodigoNombreUsuario datos = consultarCodigoNombreUsuario(conn, idUsuario);
		return (datos != null && datos.CodigoUsuario != null) ? datos.CodigoUsuario : "";
	}

	private boolean consultarEsSupervisor(Connection conn, Long idUsuario) throws Exception {
		SQL sqlSup = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdUsuario", idUsuario);
		List<SupervisorObj> resultado = sqlSup.query(conn, SQL_SELECT_ES_SUPERVISOR, params, SupervisorObj.class);
		return !resultado.isEmpty();
	}

	public MarcarOperacion_OUT logica (Connection conn, MarcarOperacion_IN in) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		SQL _sql = new SQL();
		MarcarOperacion_OUT out = new MarcarOperacion_OUT();
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		logger.info("Inicio servicio MES.MarcarOperacion");
		
		try {
			String v_FechaOperacionSecondate = FechaDB.ObtenerFechaHoraSistemaUTC(conn,in.p_IdEmpresa);
			List<Operacion> v_TblUpdate = new ArrayList<>();
			List<Operacion> v_TblUpdateBloqueo = new ArrayList<>();
			List<Operacion> v_TblUpdateModificacion = new ArrayList<>();

			logger.debug("[MarcarOperacion] Inicio MARCA1");
			startTime = System.nanoTime();
			List<Long> idOperaciones= new ArrayList<>();
			for (MarcarOperacionObjIn op : in.p_Operaciones){
				idOperaciones.add(op.IdOperacion);
			}
			if (idOperaciones.size() <= 0){
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_ORDEN",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}

			logger.debug("[MarcarOperacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin MARCA1");
			logger.debug("[MarcarOperacion] Inicio SQL_SELECT_OPERACIONES");
			startTime = System.nanoTime();
			HashMap<String,Object> paramsSelectOperaciones = new HashMap<>();
			paramsSelectOperaciones.put("IdOperacion", idOperaciones);
			List<Operacion> operaciones = _sql.query(conn, SQL_SELECT_OPERACIONES, paramsSelectOperaciones, Operacion.class);
			logger.debug("[MarcarOperacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_OPERACIONES");
			logger.debug("[MarcarOperacion] Inicio MARCA2");
			startTime = System.nanoTime();
			HashMap<Object, Object> operacionesHash = Utils.List2Hash(operaciones,"IdOperacion");

			String codigoUsuarioActual = null;
			Boolean esUsuarioSupervisor = null;

			for (MarcarOperacionObjIn op: in.p_Operaciones){
				HashMap<String,String> mapMsgOp = new HashMap<>();
				Operacion ope = (Operacion) operacionesHash.get(op.IdOperacion);

				if (ope == null){
					mapMsgOp.put("IdOperacion", op.IdOperacion.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"ORDEN_NO_EXISTE",mapMsgOp,conn,in.p_IdEmpresa));
					Id++;
					continue;
				}

				if ("L".equals(op.Accion)){
					Operacion eLiberar = new Operacion();
					eLiberar.IdOperacion = ope.IdOperacion;
					eLiberar.BloqueoPorEdicion = "N";
					eLiberar.CodigoUsuarioBloqueoEdicion = "";
					eLiberar.FechaHoraInicioEdicion = ope.FechaHoraInicioEdicion;
					v_TblUpdateBloqueo.add(eLiberar);
					v_TblUpdate.add(eLiberar);
					continue;
				}

				if ("M".equals(op.Accion)){
					Operacion eModificar = new Operacion();
					eModificar.IdOperacion = ope.IdOperacion;
					eModificar.IdUsuarioEdicion = in.p_IdUsuario;
					eModificar.FechaEdicion = Timestamp.valueOf(v_FechaOperacionSecondate);
					v_TblUpdateModificacion.add(eModificar);
					v_TblUpdate.add(eModificar);
					continue;
				}

				boolean esPactoObjetivo = esPactoObjetivo(ope);

				if ("MERF".equals(ope.IdTipoProducto) && !esPactoObjetivo){
					continue;
				}

				boolean yaBloqueadoPorMismoUsuario = false;
				if (esPactoObjetivo && "S".equals(ope.BloqueoPorEdicion)){
					if (codigoUsuarioActual == null){
						codigoUsuarioActual = consultarCodigoUsuarioEditor(conn, in.p_IdUsuario);
					}
					yaBloqueadoPorMismoUsuario = codigoUsuarioActual.equals(ope.CodigoUsuarioBloqueoEdicion);
				}

				if (esPactoObjetivo && !yaBloqueadoPorMismoUsuario){
					String fechaSistemaSoloFecha = v_FechaOperacionSecondate.substring(0,10);
					String fechaOperacionSoloFecha = ope.FechaOperacion != null ? ope.FechaOperacion.toString() : null;
					if (fechaOperacionSoloFecha == null || !fechaOperacionSoloFecha.equals(fechaSistemaSoloFecha)){
						mapMsgOp.put("NumeroFolio", ope.FolioOperacion != null ? ope.FolioOperacion.toString() : "");
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"OPERACION_FECHA_ANTERIOR",mapMsgOp,conn,in.p_IdEmpresa));
						Id++;
						continue;
					}
				}

				boolean esPropietario = in.p_IdUsuario.equals(ope.IdUsuarioCreacion);
				if (esUsuarioSupervisor == null){
					esUsuarioSupervisor = consultarEsSupervisor(conn, in.p_IdUsuario);
				}
				boolean esSupervisor  = esUsuarioSupervisor;
				boolean puedeModificarOtros = false;

				if (!esPropietario && !esSupervisor && esPactoObjetivo && !yaBloqueadoPorMismoUsuario){
					puedeModificarOtros = consultarAutorizacionAgenteOperadorMesa(conn, in.p_IdEmpresa, in.p_IdUsuario, ope.IdMesaOrigen);
				}

				boolean autorizado = esPropietario || esSupervisor || puedeModificarOtros || yaBloqueadoPorMismoUsuario;

				if (!autorizado && esPactoObjetivo){
					mapMsgOp.put("IdOperacion", ope.IdOperacion.toString());
					mapMsgOp.put("NombreUser", ope.CodigoUsuario);
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"USUARIO_NO_AUTORIZADO_A_MODIFICAR_PACTO",mapMsgOp,conn,in.p_IdEmpresa));
					Id++;
				}
				else if (!autorizado){
					mapMsgOp.put("IdOperacion", ope.IdOperacion.toString());
					mapMsgOp.put("NombreUser", ope.CodigoUsuario);
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"USUARIO_NO_AUTORIZADO_A_MODIFICAR_OPERACION",mapMsgOp,conn,in.p_IdEmpresa));
					Id++;
				}
				else {
					if (codigoUsuarioActual == null){
						codigoUsuarioActual = consultarCodigoUsuarioEditor(conn, in.p_IdUsuario);
					}
					if ("S".equals(ope.BloqueoPorEdicion) && !codigoUsuarioActual.equals(ope.CodigoUsuarioBloqueoEdicion)){
						mapMsgOp.put("IdOperacion", ope.IdOperacion.toString());
						mapMsgOp.put("CodigoUsuarioBloqueoEdicion", ope.CodigoUsuarioBloqueoEdicion != null ? ope.CodigoUsuarioBloqueoEdicion : "");
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"OPERACION_BLOQUEADA_POR_MODIFICACION",mapMsgOp,conn,in.p_IdEmpresa));
						Id++;
					}
					else {
						Operacion eBloquear = new Operacion();
						eBloquear.IdOperacion = ope.IdOperacion;
						eBloquear.BloqueoPorEdicion = "S";
						eBloquear.CodigoUsuarioBloqueoEdicion = codigoUsuarioActual;
						eBloquear.FechaHoraInicioEdicion = yaBloqueadoPorMismoUsuario
								? ope.FechaHoraInicioEdicion
								: Timestamp.valueOf(v_FechaOperacionSecondate);
						v_TblUpdateBloqueo.add(eBloquear);
						v_TblUpdate.add(eBloquear);
					}
				}
			}
			Long errTot = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (!errTot.equals(0L) ){
				return out;
			}
			logger.debug("[MarcarOperacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin MARCA2");

			logger.debug("[MarcarOperacion] Inicio SQL_UPDATE_MARCAR_OPERACIONES");
			startTime = System.nanoTime();
			
			if (!v_TblUpdateBloqueo.isEmpty()){
				_sql.batch(conn, SQL_UPDATE_BLOQUEO_USUARIO_EDITOR, v_TblUpdateBloqueo, Operacion.class);
			}
			if (!v_TblUpdateModificacion.isEmpty()){
				_sql.batch(conn, SQL_UPDATE_MARCAR_OPERACIONES, v_TblUpdateModificacion, Operacion.class);
			}
			
			logger.debug("[MarcarOperacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_UPDATE_MARCAR_OPERACIONES");
			logger.debug("[MarcarOperacion] Inicio MARCA3");
			startTime = System.nanoTime();
			mapMsg.put("Cantidad", ""+v_TblUpdate.size());
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_OK, "BLOQUEO_OPERACION_ACTUALIZADA", mapMsg, conn, in.p_IdEmpresa));
			logger.debug("[MarcarOperacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin MARCA3");
			
			//Salida operaciones			
			for(Operacion op : v_TblUpdate){
				MarcarOperacionObjOut outOp = new MarcarOperacionObjOut();
				outOp.IdOperacion 		= op.IdOperacion;
				outOp.FechaEdicion 		= op.FechaEdicion;
				outOp.IdUsuarioEdicion 	= op.IdUsuarioEdicion;
				outOp.EstadoEdicion		= op.IdEstado;
				out.P_OUT_OPERACIONES.add(outOp);
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
		logger.info(" Termino del servicio MES.MarcarOperacion, tiempo: " + (System.currentTimeMillis() - init) + " ms - " + (System.currentTimeMillis() - init)/1000 + " seg");
		
		return out;
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		MarcarOperacion_IN jin = gson.fromJson(in.toString(), MarcarOperacion_IN.class);
		MarcarOperacion_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPropertiesConnection("qa02","connections.properties");
			
			//datos entrada
			String json1 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":2326,\"p_IdModulo\":\"MRF\",\"p_Operaciones\":[{\"Accion\":\"B\",\"IdOperacion\":202021}]}";
			String json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9853,\"p_IdModulo\":\"MRF\",\"p_Operaciones\":[{\"Accion\":\"B\",\"IdOperacion\":202021}]}";
			String json3 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":206,\"p_IdModulo\":\"MRF\",\"p_Operaciones\":[{\"Accion\":\"B\",\"IdOperacion\":202026}]}";		
			
			MarcarOperacion_IN objIn = gson.fromJson(json3,MarcarOperacion_IN.class);
			MarcarOperacion demo = new MarcarOperacion();
			MarcarOperacion_OUT ObjOut=demo.logica(conn, objIn);
				
			
			System.out.println(ObjOut.toString());
			conn.rollback();
			//conn.commit();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}
