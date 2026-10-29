package cl.bcs.tesoreria;

import java.sql.Connection;

import com.google.gson.JsonObject;

import cl.bcs.oracle.Hooks;

/**
 * ORACLE FAKE of legacy cl.bcs.tesoreria.ActivarMandatosArchivoCargo.logica(Connection, Long): records {"valor": diaDescuento}
 * (the convention of the Tesoreria family B goldens for a scalar parameter); "colaboradores.ActivarMandatosArchivoCargo.lanza" scripts a failure.
 */
public class ActivarMandatosArchivoCargo {

	public void logica(Connection conn, Long diasDescuento) throws Exception {
		JsonObject o = new JsonObject();
		o.addProperty("valor", diasDescuento);
		Hooks.registrarJson("ActivarMandatosArchivoCargo", o);
		Hooks.lanzaSiCorresponde("ActivarMandatosArchivoCargo.lanza");
	}
}
