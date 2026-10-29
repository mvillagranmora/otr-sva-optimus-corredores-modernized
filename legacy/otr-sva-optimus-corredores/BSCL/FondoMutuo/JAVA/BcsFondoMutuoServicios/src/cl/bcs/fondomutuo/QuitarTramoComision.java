package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.AdministrarComisiones.SalidaAdministrarComision;
import cl.bcs.fondomutuo.AdministrarComisiones.TramoComision;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class QuitarTramoComision implements JavaCall{

	private final String SQL_UPDATE_TRAMO = loadResourceToString("cl/bcs/fondomutuo/sql/QuitarTramoComision_UpdateTramo.sql");
	static final Logger logger = LogManager.getLogger(QuitarTramoComision.class);
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SalidaAdministrarComision logica(Connection conn, List<TramoComision> tramosComision)  throws Exception{
		logger.info("INICIO::QuitarTramoComision");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaAdministrarComision salida = new SalidaAdministrarComision();
		
		quitarTramos(conn, tramosComision);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::QuitarTramoComision", finServicio - inicioServicio);
		return salida;
	}
	
	private void quitarTramos(Connection conn, List<TramoComision> tramosAQuitar) throws Exception {
		logger.info("INICIO::QuitarTramoComision.quitarTramos");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(conn, SQL_UPDATE_TRAMO, tramosAQuitar, TramoComision.class);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::QuitarTramoComision.quitarTramos", finServicio - inicioServicio);
	}
}
