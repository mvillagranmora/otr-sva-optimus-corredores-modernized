package cl.bcs.tesoreria;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.ConfirmarAutorizacionOrdenes;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarRechazarAnularMovimientosTesoreria implements JavaCall {

	private Logger log = LogManager.getLogger(ConfirmarAutorizacionOrdenes.class);
	private final String SQL_UPDATE_RECHAZAR_ANULAR_MOV_INGRESO_EGRESO = loadResourceToString("cl/bcs/tesoreria/sql/ConfirmarRechazarAnularMovimientos_UpdateRechazarMovimientosIngresoEgreso.sql");
	private final String SQL_UPDATE_RECHAZAR_AUTORIZACION = loadResourceToString("cl/bcs/tesoreria/sql/ConfirmarRechazarAnularMovimientos_UpdateRechazarAutorizacion.sql");	
	
	private final String SQL_SELECT_MOVIMIENTO_RECHAZADOS = loadResourceToString("cl/bcs/tesoreria/sql/ConfirmarRechazarAnularMovimientos_SelectMovimientosRechazados.sql");	
	
	private static final String CREATE_TEMPORARY_TABLE = "CREATE LOCAL TEMPORARY TABLE #RechazarAnularMovimientosTesoreria (\"IdIngresoEgreso\" BIGINT)";
	
	private static final String SQL_INSERT_REGISTROS = "INSERT INTO #RechazarAnularMovimientosTesoreria (\"IdIngresoEgreso\") VALUES (:IdIngresoEgreso)";
	private static final String SQL_DELETE_REGISTROS = "DELETE FROM #RechazarAnularMovimientosTesoreria WHERE 1=1";	
	
	public static class EntradaConfirmarRechazarAnularMovimientosTesoreria {
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		
		public List<MovimientosPorRechazar> p_Movimientos;
		
		public EntradaConfirmarRechazarAnularMovimientosTesoreria() {
			this.p_Movimientos = new ArrayList<MovimientosPorRechazar>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientosPorRechazar {
		public Long 	IdIngresoEgreso;
		
		public MovimientosPorRechazar(Long idIngresoEgreso) {
			IdIngresoEgreso = idIngresoEgreso;
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientosRechazados {
		public Long 	IdIngresoEgreso;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConfirmarRechazarAnularMovimientosTesoreria {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
				
		public SalidaConfirmarRechazarAnularMovimientosTesoreria() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();			
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}		
	
	public SalidaConfirmarRechazarAnularMovimientosTesoreria logica (Connection conn, EntradaConfirmarRechazarAnularMovimientosTesoreria in) throws Exception {
		SalidaConfirmarRechazarAnularMovimientosTesoreria out = new SalidaConfirmarRechazarAnularMovimientosTesoreria();
		try {
			SQL sql = new SQL();
			log.info("INICIO::ConfirmarRechazarAnularMovimientosTesoreria");
			
			SQL.exec(conn, CREATE_TEMPORARY_TABLE);
			sql.batch(conn, SQL_INSERT_REGISTROS, in.p_Movimientos, MovimientosPorRechazar.class);
			
			/**
			 * RECHAZAR ANULACION DE MOVIMIENTOS
			 */
			//Rechaza Autorizacion en tabla de Evento
			sql.batch(conn, SQL_UPDATE_RECHAZAR_AUTORIZACION, Arrays.asList(in), EntradaConfirmarRechazarAnularMovimientosTesoreria.class);
			
			//obtener movimientos a procesar que tengan IdEstado = RZD.
			List<MovimientosRechazados> movimientosRechazados = sql.query(conn, SQL_SELECT_MOVIMIENTO_RECHAZADOS, new HashMap<String, Object>(), MovimientosRechazados.class);
			
			//averiguar si los movimientos de la entrada cumplen con los requisitos para ser rechazados.
			HashMap<String, Object> movimientosValidarRechazar = Utils.List2Hash(movimientosRechazados, "IdIngresoEgreso");
			in.p_Movimientos.forEach(mov -> {
				if(!movimientosValidarRechazar.containsKey(mov.IdIngresoEgreso)) {
					try {
						String mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_ESTADO_INCORRECTO").msg();
						out.P_OUT_MENSAJES.add(new MensajeSalida((long)out.P_OUT_MENSAJES.size()+1, MensajeSalida.MENSAJE_ERROR, "UI_ESTADO_INCORRECTO", mensaje));						
					} catch (Exception e) {
						e.printStackTrace();
					}				
				}
			});

			if(!out.P_OUT_MENSAJES.isEmpty()) {
				//si hay errores, terminar el flujo.
				return out;
			}
			
			log.debug(movimientosValidarRechazar.values());
			
			//Rechaza Anular Movimientos de Ingreso o Egreso
			sql.batch(conn, SQL_UPDATE_RECHAZAR_ANULAR_MOV_INGRESO_EGRESO, Arrays.asList(in), EntradaConfirmarRechazarAnularMovimientosTesoreria.class);
			
			//preparar salida y log de movimientos rechazados para anular.
			AtomicLong contador = new AtomicLong();	
			AgregarLog agregarLog = new AgregarLog();
			InAgregarLog entradaAgregarLog = new InAgregarLog();
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);		
			
			for (MovimientosRechazados mov : movimientosRechazados) {
				String mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_MOVIMIENTO_COMPROBANTE_NO_ANULADO")
								 .add("IdMovimientoComprobante", mov.IdIngresoEgreso)					
								 .msg();
				out.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdIngresoEgreso, MensajeSalida.MENSAJE_OK, "AUTORIZACION_RECHAZADA", mensaje));
				
				entradaAgregarLog.agregarLogBasico(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, mov.IdIngresoEgreso, contador.get(), "ANULAR", "INGRESO/EGRESO", fechaSistema, mensaje);
				entradaAgregarLog.agregarDetalleLogBasico(mov.IdIngresoEgreso, contador.getAndIncrement(), "IdEstado", "INGRESADO", "INGRESADO");
			}
			agregarLog.logica(conn, entradaAgregarLog);
			
			SQL.exec(conn, SQL_DELETE_REGISTROS);
			
		} catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
		}
		log.info("TERMINO::ConfirmarRechazarAnularMovimientosTesoreria");
		return out;
	}	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaConfirmarRechazarAnularMovimientosTesoreria in = gson.fromJson(jin.toString(), EntradaConfirmarRechazarAnularMovimientosTesoreria.class);
		SalidaConfirmarRechazarAnularMovimientosTesoreria out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_Movimientos\":[{\"IdIngresoEgreso\":19014}]}";
								
				EntradaConfirmarRechazarAnularMovimientosTesoreria Objin = gson.fromJson(json, EntradaConfirmarRechazarAnularMovimientosTesoreria.class);				
				ConfirmarRechazarAnularMovimientosTesoreria demo=new ConfirmarRechazarAnularMovimientosTesoreria();				
	 			System.out.println(Objin.toString());
	 			SalidaConfirmarRechazarAnularMovimientosTesoreria ObjOut=demo.logica(con, Objin);	 			
		    	System.out.println(ObjOut.toString());	
		    	con.rollback();
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}		

}
