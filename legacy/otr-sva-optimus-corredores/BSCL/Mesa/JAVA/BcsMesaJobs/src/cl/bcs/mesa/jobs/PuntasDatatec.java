package cl.bcs.mesa.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.mesa.SPT.CrearPuntasSpot;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class PuntasDatatec extends BcsJob implements JavaCall {
	String IdTipoPrograma = "PUNTASDATATEC";
	
	private final String SQL_SELECT_PUNTAS_DATATEC = loadResourceToString("cl/bcs/mesa/jobs/sql/PuntasDatatec_SelectPuntasDatatec.sql");;     
	private final String SQL_SELECT_PUNTAS_TRAMO = loadResourceToString("cl/bcs/mesa/jobs/sql/PuntasDatatec_SelectPuntasTramo.sql");
	private final String SQL_SELECT_PUNTAS = loadResourceToString("cl/bcs/mesa/jobs/sql/PuntasDatatec_SelectPuntas.sql"); 
	
	private static final Logger logger = LogManager.getLogger(PuntasDatatec.class);
	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return logica(conn,false);
	}
	
	public boolean logica(Connection conn, boolean debug) throws Exception {
		logger.info("Inicio Puntas Datatec");
		
		try {
			SQL _sql = new SQL();

			Long IdEmpresa = !debug ? Long.parseLong(this.getParam("IdEmpresa")) : 1L;
			String IdUsuario = !debug ? this.getParam("IdUsuario") : "1";
			Long IdPrograma = !debug ? this.getJobId() : 324234343L;
			
			String activaPuntasDatatec = ConfiguracionSistema.getParametroConfiguracionString(conn, IdEmpresa, "PLATAFORMA", "PLA.HabilitarPuntasDatatec");
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			if ("N".equalsIgnoreCase(activaPuntasDatatec)) return true;
			
			CrearPuntasSpot.PuntasEntrada in = new CrearPuntasSpot.PuntasEntrada();
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdMoneda", "USD");
			CrearPuntasSpot.Puntas puntasDatatec = _sql.query(conn, SQL_SELECT_PUNTAS_DATATEC, new HashMap<>(), CrearPuntasSpot.Puntas.class).stream().findFirst().orElse(new CrearPuntasSpot.Puntas());
			CrearPuntasSpot.Puntas puntas = _sql.query(conn, SQL_SELECT_PUNTAS, params, CrearPuntasSpot.Puntas.class).stream().findFirst().orElse(new CrearPuntasSpot.Puntas());;
			if (puntas.PuntaCompra.equals(puntasDatatec.PuntaCompra) && puntas.PuntaVenta.equals(puntasDatatec.PuntaVenta)) return true;
			puntasDatatec.AperturaPrecio = puntasDatatec.PuntaVenta.subtract(puntasDatatec.PuntaCompra).multiply(new BigDecimal(100));
			puntasDatatec.IdEmpresa = IdEmpresa;
			puntasDatatec.IdUsuario = IdUsuario;
			puntasDatatec.Origen = "DATATEC";
			puntasDatatec.Estado = "VIG"; 
			params.clear();
			params.put("IdPuntas", puntas.IdPuntas);
			in.p_DatosPuntasTramo = _sql.query(conn, SQL_SELECT_PUNTAS_TRAMO, params, CrearPuntasSpot.PuntasTramo.class);
			CrearPuntasSpot puntasSpot = new CrearPuntasSpot();
			LocalDateTime fechaPunta = puntasDatatec.Fecha.toLocalDateTime();
	        String horaString = FechaDB.ObtenerHoraSistemaLocal(conn, IdEmpresa);
	        String[] partes = horaString.split(":");
	        fechaPunta = fechaPunta.withHour(Integer.parseInt(partes[0]))
                    .withMinute(Integer.parseInt(partes[1]))
                    .withSecond(Integer.parseInt(partes[2]));

			in.p_IdMoneda = "USD";
			in.p_fecha = Timestamp.valueOf(fechaPunta);
			in.p_IdEmpresa = IdEmpresa;
			
			puntasDatatec.IdPuntas = puntas.IdPuntas;
			puntasDatatec.CheckPrecioPorTramo = puntas.CheckPrecioPorTramo;
			
			logger.info("PuntasDatatec.logica - INICIO AGREGAR LOGs");
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			entradaAgregarLog.agregarLogBasico(IdEmpresa, Long.parseLong(IdUsuario), "PLA", IdPrograma, IdPrograma, "PUNTAS", "PUNTAS", FechaSistema, "");
			entradaAgregarLog.agregarDetalleLogBasico(IdPrograma, IdPrograma, "Punta Compra", puntas.PuntaCompra.toString(), puntasDatatec.PuntaCompra.toString(), "PUNTAS");
			entradaAgregarLog.agregarDetalleLogBasico(IdPrograma, IdPrograma, "Punta Venta", puntas.PuntaVenta.toString(), puntasDatatec.PuntaVenta.toString(), "PUNTAS");
			AgregarLog log = new AgregarLog();
			AgregarLog.OutAgregarLog outLog = log.logica(conn, entradaAgregarLog);
			logger.info("PuntasDatatec.logica - FIN AGREGAR LOG", outLog.toString());
			
			List<CrearPuntasSpot.Puntas> puntasArray = new ArrayList<>();
			puntasArray.add(puntasDatatec);
			
			in.p_DatosPuntas = puntasArray;

			CrearPuntasSpot.PuntasSalida out =  puntasSpot.logica(conn, in);
			logger.info("PuntasDatatec.logica - FIN AGREGAR LOG", out.toString());
		} catch(Exception e) {
			logger.error("ERROR", e.getMessage());
			e.printStackTrace();
			throw e;
		}
		logger.info("Inicio Puntas Datatec");
		return true;
	}
	
	@Override
    public void onError(Throwable t) {
        logger.error(IdTipoPrograma + " ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public static void main(String[] args) throws SQLException {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			PuntasDatatec demo = new PuntasDatatec();
 			try {
 				Boolean ObjOut = demo.logica(con, true);
				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				con.rollback();
			}
			finally {
				con.close();
			}
	    }
		System.out.println("== END ==");
	}
}
