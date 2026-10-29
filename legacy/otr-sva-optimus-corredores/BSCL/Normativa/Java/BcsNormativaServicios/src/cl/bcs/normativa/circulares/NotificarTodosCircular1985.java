package cl.bcs.normativa.circulares;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.circulares.ReeprocesarEventoCircular1985.DetalleEvento;
import cl.bcs.normativa.circulares.ReeprocesarEventoCircular1985.ReeprocesarEventoCircular1985_Entrada;
import cl.bcs.normativa.circulares.ReeprocesarEventoCircular1985.ReeprocesarEventoCircular1985_Salida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.SalidaServicios;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarTodosCircular1985 implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ReeprocesarEventoCircular1985.class);
	
	private final String SQL_CONSULTAR_EVENTOS = loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarTodosCircular1985_ConsultarEventos.sql");
	
	public static class NotificarTodosCircular1985_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public Long p_IdCliente;
	}
	
	public static class ObjEvento {
		public Long IdEvento;
		public Long IdCliente;
		
		 public String toString() {
	            return Utils.printJson(this, false);
	     }
	}
	
	public static class NotificarTodosCircular1985_Salida extends SalidaServicios{
		public Long p_out_TotalRegistros;
	    public Long p_out_TotalNotificados;
	    public Long p_out_TotalSinEvento;  
		public List<MensajeSalida> P_OUT_MENSAJES;
		public NotificarTodosCircular1985_Salida() {
	        super();
	        p_out_TotalRegistros   = 0L;
	        p_out_TotalNotificados = 0L;
	        p_out_TotalSinEvento   = 0L;
	        P_OUT_MENSAJES         = new ArrayList<>();
	    }
		
		public String toString() {
            return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = getGsonBuilder();
		NotificarTodosCircular1985_Entrada in  = gson.fromJson(jin.toString(), NotificarTodosCircular1985_Entrada.class);
        NotificarTodosCircular1985_Salida  out = logica(conn, in);
        JsonParser parser = new JsonParser();
        return parser.parse(gson.toJson(out)).getAsJsonObject();
	}
	
	public NotificarTodosCircular1985_Salida logica(Connection con, NotificarTodosCircular1985_Entrada in) throws Exception {
		SQL sql = new SQL();
		NotificarTodosCircular1985_Salida out =  new NotificarTodosCircular1985_Salida();
		
		//validacion de rango de fechas
		Long parametroDias = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa, "NORMATIVA", "RANGO_CONSULTA_FECHA_CIRCULAR_1985");
		long diasRango = ChronoUnit.DAYS.between(
            LocalDate.parse(in.p_FechaDesde),
            LocalDate.parse(in.p_FechaHasta)
        );
        if (diasRango > parametroDias) {
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                "RANGO_FECHA_EXCEDIDO", new HashMap<>(), con, in.p_IdEmpresa));
            return out;
        }
        
        //consultar eventos segun filtro
        logger.info("INICIO:: consultar eventos");
        HashMap<String, Object> params = new HashMap<>();
        params.put("p_FechaDesde", in.p_FechaDesde);
        params.put("p_FechaHasta", in.p_FechaHasta);
        params.put("p_IdCliente", in.p_IdCliente != null ? in.p_IdCliente : 0L);
        
        List<ObjEvento> listaEventos = sql.query(con, SQL_CONSULTAR_EVENTOS, params, ObjEvento.class);
        logger.info("FIN:: consultar eventos - Total: " + (listaEventos != null ? listaEventos.size() : 0));
        if (listaEventos == null || listaEventos.isEmpty()) {
        	out.P_OUT_ERRORESCONTADOR++;
        	out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "SIN_EVENTOS_PARA_NOTIFICAR", new HashMap<>(), con, in.p_IdEmpresa));
        	return out;
        }
	      
        //armar lista para reprocesar enventos circular 1985
        long totalRegistros   = listaEventos.size();
        long totalSinEvento   = listaEventos.stream().filter(e -> e.IdEvento == null || e.IdEvento == 0).count();
        long totalConEvento   = totalRegistros - totalSinEvento;
        logger.info("Total registros: " + totalRegistros + " - Con evento: " + totalConEvento + " - Sin evento: " + totalSinEvento);

        // Solo los que tienen evento para reprocesar
        List<DetalleEvento> eventosAReprocesar = new ArrayList<>();
        for (ObjEvento evt : listaEventos) {
            if (evt.IdEvento != null && evt.IdEvento > 0) {
                DetalleEvento detalle = new DetalleEvento();
                detalle.p_IdEvento  = evt.IdEvento;
                detalle.p_IdCliente = evt.IdCliente;
                eventosAReprocesar.add(detalle);
            }
        }
        
        out.p_out_TotalRegistros   = totalRegistros;
        out.p_out_TotalSinEvento   = totalSinEvento;
        
    	// Validar que haya eventos a reprocesar
        if (eventosAReprocesar.isEmpty()) {
            logger.info("No hay eventos con IdEvento > 0 para reprocesar");
            out.p_out_TotalNotificados = 0L;
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,
                "SIN_EVENTOS_PARA_NOTIFICAR", new HashMap<>(), con, in.p_IdEmpresa));
            return out;
        }
        
        //llamamos a reprocesar
        logger.info("INICIO:: reprocesar " + eventosAReprocesar.size() + " eventos");
        ReeprocesarEventoCircular1985 reprocesar = new ReeprocesarEventoCircular1985();
        ReeprocesarEventoCircular1985_Entrada entradaReprocesar = new ReeprocesarEventoCircular1985_Entrada();
        entradaReprocesar.p_IdEmpresa = in.p_IdEmpresa;
        entradaReprocesar.p_IdModulo = in.p_IdModulo;
        entradaReprocesar.p_IdUsuario =  in.p_IdUsuario;
        entradaReprocesar.p_Eventos = eventosAReprocesar;
        
        ReeprocesarEventoCircular1985_Salida salidaReprocesar = reprocesar.logica(con, entradaReprocesar);
        logger.info("FIN:: reprocesar - Errores: " + salidaReprocesar.P_OUT_ERRORESCONTADOR);

        out.p_out_TotalNotificados = totalConEvento - salidaReprocesar.P_OUT_ERRORESCONTADOR;
        out.P_OUT_ERRORESCONTADOR = salidaReprocesar.P_OUT_ERRORESCONTADOR;
        out.P_OUT_MENSAJES        = salidaReprocesar.P_OUT_MENSAJES;
        registrarLog(con, in, out.p_out_TotalNotificados, out.p_out_TotalSinEvento);
		return out;
	}
	
	private void registrarLog(Connection con, NotificarTodosCircular1985_Entrada in, long totalNotificados, long totalSinEvento) {
	    try {
	        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(
	            con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema"
	        );

	        InAgregarLog agregarLogEntrada = new InAgregarLog();

	        AgregarLog.InAgregarLog.Log logNotificarTodos = new AgregarLog.InAgregarLog.Log();
	        logNotificarTodos.IdCabeceraLog     = in.p_IdEmpresa;
	        logNotificarTodos.IdTipoLog         = "INFORMATIVO";
	        logNotificarTodos.IdModulo          = in.p_IdModulo;
	        logNotificarTodos.IdUsuario         = in.p_IdUsuario;
	        logNotificarTodos.IdEmpresa         = in.p_IdEmpresa;
	        logNotificarTodos.IdCliente         = in.p_IdCliente != null ? in.p_IdCliente : 0L;
	        logNotificarTodos.IdUsuarioAtendido = in.p_IdUsuario;
	        logNotificarTodos.IdTipoRegistro    = "CIR1985_NOTIFICACION_MASIVA";
	        logNotificarTodos.NombreProceso     = "NotificarTodosCircular1985";
	        logNotificarTodos.FechaSistemaLog   = fechaSistema;
	        agregarLogEntrada.p_Log.add(logNotificarTodos);

	        agregarLogEntrada.agregarDetalleLogBasico(
	            in.p_IdEmpresa, Long.valueOf(0L), "NotificacionesEmitidas", "", String.valueOf(totalNotificados), "RESULTADO"
	        );
	        agregarLogEntrada.agregarDetalleLogBasico(
	            in.p_IdEmpresa, Long.valueOf(0L), "SinEvento", "", String.valueOf(totalSinEvento), "RESULTADO"
	        );
	        agregarLogEntrada.agregarDetalleLogBasico(
	            in.p_IdEmpresa, Long.valueOf(0L), "FechaDesde", "", in.p_FechaDesde, "FILTRO"
	        );
	        agregarLogEntrada.agregarDetalleLogBasico(
	            in.p_IdEmpresa, Long.valueOf(0L), "FechaHasta", "", in.p_FechaHasta, "FILTRO"
	        );

	        new AgregarLog().logica(con, agregarLogEntrada);
	        logger.info("Log registrado correctamente");

	    } catch (Exception e) {
	        logger.error("Error registrando log: " + e.getMessage());
	    }
	}
	
	
	//METODOS PARA PRUEBAS LOCALES
    public static void main(String[] args) {
        System.out.println("== TEST NotificarTodosCircular1985 ==");
        try (Connection con = PlataformaUtils.getDESAConnection()) {
            NotificarTodosCircular1985 sp = new NotificarTodosCircular1985();
            NotificarTodosCircular1985_Entrada in = new NotificarTodosCircular1985_Entrada();
            in.p_IdEmpresa  = 1L;
            in.p_IdUsuario  = 1L;
            in.p_IdModulo   = "NOR";
            in.p_FechaDesde = "2026-05-01";
            in.p_FechaHasta = "2026-05-22";
            in.p_IdCliente  = null; // null = todos
            NotificarTodosCircular1985_Salida out = sp.logica(con, in);
            con.rollback();
            System.out.println(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== FIN ==");
    }
}
