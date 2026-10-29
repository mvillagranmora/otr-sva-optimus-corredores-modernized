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

public class CrearTramoComision implements JavaCall{

	private final String SQL_INSERT_TRAMO = loadResourceToString("cl/bcs/fondomutuo/sql/CrearTramoComision_InsertTramo.sql");
	static final Logger logger = LogManager.getLogger(CrearTramoComision.class);
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SalidaAdministrarComision logica(Connection conn, List<TramoComision> tramosACrear)  throws Exception{
		logger.info("INICIO::CrearTramoComision");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaAdministrarComision salida = new SalidaAdministrarComision();
		
		insertarTramos(conn, tramosACrear);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearTramoComision", finServicio - inicioServicio);
		return salida;
	}

	private void insertarTramos(Connection conn, List<TramoComision> tramosACrear) throws Exception {
		logger.info("INICIO::CrearTramoComision.insertarTramos");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(conn, SQL_INSERT_TRAMO, tramosACrear, TramoComision.class);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearTramoComision.insertarTramos", finServicio - inicioServicio);
	}
}
