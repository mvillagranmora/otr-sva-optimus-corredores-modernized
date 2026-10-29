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

public class ModificarTramoComision implements JavaCall{

	private final String SQL_UPDATE_TRAMO = loadResourceToString("cl/bcs/fondomutuo/sql/ModificarTramoComision_UpdateTramo.sql");
	static final Logger logger = LogManager.getLogger(ModificarTramoComision.class);
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SalidaAdministrarComision logica(Connection conn, List<TramoComision> tramosComision)  throws Exception{
		logger.info("INICIO::ModificarTramoComision");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaAdministrarComision salida = new SalidaAdministrarComision();
		
		modificarTramos(conn, tramosComision);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTramoComision", finServicio - inicioServicio);
		return salida;
	}
	
	private void modificarTramos(Connection conn, List<TramoComision> tramosAModificar) throws Exception {
		logger.info("INICIO::ModificarTramoComision.modificarTramos");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(conn, SQL_UPDATE_TRAMO, tramosAModificar, TramoComision.class);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTramoComision.modificarTramos", finServicio - inicioServicio);
	}
}
