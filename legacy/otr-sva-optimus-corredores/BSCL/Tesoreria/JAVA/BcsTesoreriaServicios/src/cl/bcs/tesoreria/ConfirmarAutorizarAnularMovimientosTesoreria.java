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

public class ConfirmarAutorizarAnularMovimientosTesoreria implements JavaCall {

	private Logger log = LogManager.getLogger(ConfirmarAutorizacionOrdenes.class);
	private final String SQL_SELECT_MOVIMIENTO_AUTORIZADOS 	= loadResourceToString("cl/bcs/tesoreria/sql/ConfirmarAutorizarAnularMovimientos_SelectMovimientosAutorizados.sql");
	private final String SQL_UPDATE_ANULAR_MOV_INGRESO_EGRESO = loadResourceToString("cl/bcs/tesoreria/sql/ConfirmarAutorizarAnularMovimientos_UpdateAnularMovimientosIngresoEgreso.sql");	
	private final String SQL_UPDATE_ANULAR_FORMA_PAGO_MOVIMIENTO = loadResourceToString("cl/bcs/tesoreria/sql/ConfirmarAutorizarAnularMovimientos_UpdateAnularFormaDePagoMovimientos.sql");
	
	private static final String CREATE_TEMPORARY_TABLE = "CREATE LOCAL TEMPORARY TABLE #AutorizarAnularMovimientosTesoreria (\"IdIngresoEgreso\" BIGINT)";
	
	private static final String SQL_INSERT_REGISTROS = "INSERT INTO #AutorizarAnularMovimientosTesoreria (\"IdIngresoEgreso\") VALUES (:IdIngresoEgreso)";
	private static final String SQL_DELETE_REGISTROS = "DELETE FROM #AutorizarAnularMovimientosTesoreria WHERE 1=1";	
	
	public static class EntradaConfirmarAutorizarAnularMovimientosTesoreria {
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		
		public List<MovimientosPorAutorizar> p_Movimientos;
		
		public EntradaConfirmarAutorizarAnularMovimientosTesoreria() {
			this.p_Movimientos = new ArrayList<MovimientosPorAutorizar>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientosPorAutorizar {
		public Long 	IdIngresoEgreso;
		
		public MovimientosPorAutorizar(Long idIngresoEgreso) {
			IdIngresoEgreso = idIngresoEgreso;
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientosAutorizados {
		public Long 	IdIngresoEgreso;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConfirmarAutorizarAnularMovimientosTesoreria {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
				
		public SalidaConfirmarAutorizarAnularMovimientosTesoreria() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();			
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}		
	
	public SalidaConfirmarAutorizarAnularMovimientosTesoreria logica (Connection conn, EntradaConfirmarAutorizarAnularMovimientosTesoreria in) throws Exception {
		SalidaConfirmarAutorizarAnularMovimientosTesoreria out = new SalidaConfirmarAutorizarAnularMovimientosTesoreria();
		SQL sql = new SQL();
		log.info("INICIO::ConfirmarAutorizarAnularMovimientosTesoreria");
		
		SQL.exec(conn, CREATE_TEMPORARY_TABLE);
		sql.batch(conn, SQL_INSERT_REGISTROS, in.p_Movimientos, MovimientosPorAutorizar.class);
		
		/**
		 * AUTORIZAR ANULACION DE MOVIMIENTOS
		 */
		//Anula Movimientos de Ingreso o Egreso
		sql.batch(conn, SQL_UPDATE_ANULAR_MOV_INGRESO_EGRESO, Arrays.asList(in), EntradaConfirmarAutorizarAnularMovimientosTesoreria.class);
		
		//Anula la Forma de Pago de los movimientos anulados
		sql.batch(conn, SQL_UPDATE_ANULAR_FORMA_PAGO_MOVIMIENTO, Arrays.asList(in), EntradaConfirmarAutorizarAnularMovimientosTesoreria.class);
		
		//obtener movimientos a procesar que tengan IdEstado = ANU.
		List<MovimientosAutorizados> movimientosAutorizados = sql.query(conn, SQL_SELECT_MOVIMIENTO_AUTORIZADOS, new HashMap<String, Object>(), MovimientosAutorizados.class);
		
		//averiguar si los movimientos de la entrada cumplen con los requisitos para ser autorizados.
		HashMap<String, Object> movimientosValidarAutorizar = Utils.List2Hash(movimientosAutorizados, "IdIngresoEgreso");
		in.p_Movimientos.forEach(mov -> {
			if(!movimientosValidarAutorizar.containsKey(mov.IdIngresoEgreso)) {
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
		
		log.debug(movimientosValidarAutorizar.values());
		
		//preparar salida y log de movimientos anulados.
		AtomicLong contador = new AtomicLong();
		AgregarLog agregarLog = new AgregarLog();
		InAgregarLog entradaAgregarLog = new InAgregarLog();
		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);		
		
		for (MovimientosAutorizados mov : movimientosAutorizados) {
			String mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_MOVIMIENTO_COMPROBANTE_ANULADO")
							 .add("IdMovimientoComprobante", mov.IdIngresoEgreso)
							 .msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdIngresoEgreso, MensajeSalida.MENSAJE_OK, "UI_MOVIMIENTO_COMPROBANTE_ANULADO", mensaje));
			
			entradaAgregarLog.agregarLogBasico(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, mov.IdIngresoEgreso, contador.get(), "ANULAR", "INGRESO/EGRESO", fechaSistema, mensaje);
			entradaAgregarLog.agregarDetalleLogBasico(mov.IdIngresoEgreso, contador.getAndIncrement(), "IdEstado", "INGRESADO", "ANULADO");
		}
		agregarLog.logica(conn, entradaAgregarLog);
		
		SQL.exec(conn, SQL_DELETE_REGISTROS);
		
		log.info("TERMINO::ConfirmarAutorizarAnularMovimientosTesoreria");
		return out;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaConfirmarAutorizarAnularMovimientosTesoreria in = gson.fromJson(jin.toString(), EntradaConfirmarAutorizarAnularMovimientosTesoreria.class);
		SalidaConfirmarAutorizarAnularMovimientosTesoreria out = logica(con, in);
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
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_Movimientos\":[{\"IdIngresoEgreso\":12853}]}";
								
				EntradaConfirmarAutorizarAnularMovimientosTesoreria Objin = gson.fromJson(json, EntradaConfirmarAutorizarAnularMovimientosTesoreria.class);				
				ConfirmarAutorizarAnularMovimientosTesoreria demo=new ConfirmarAutorizarAnularMovimientosTesoreria();				
	 			System.out.println(Objin.toString());
	 			SalidaConfirmarAutorizarAnularMovimientosTesoreria ObjOut=demo.logica(con, Objin);	 			
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
