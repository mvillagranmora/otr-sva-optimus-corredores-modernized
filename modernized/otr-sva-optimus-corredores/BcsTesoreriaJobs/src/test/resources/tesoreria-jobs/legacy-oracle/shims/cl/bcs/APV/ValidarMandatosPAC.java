package cl.bcs.APV;

import java.sql.Connection;

import com.google.gson.JsonObject;

import cl.bcs.oracle.Hooks;
import cl.bcs.tesoreria.ProcesarDistribucion.SalidaRecaudacionTraspaso;
import cl.bcs.utils.Utils;

/**
 * ORACLE FAKE of legacy cl.bcs.APV.ValidarMandatosPAC.logica(con, SalidaRecaudacionTraspaso, IdEmpresa, IdUsuario)
 * (APV/.../ValidarMandatosPAC.java:67). Records {salida, idEmpresa, idUsuario}; "colaboradores.ValidarMandatosPAC.lanza" scripts a failure.
 */
public class ValidarMandatosPAC {

	public void logica(Connection con, SalidaRecaudacionTraspaso entrada, Long IdEmpresa, Long IdUsuario) throws Exception {
		JsonObject o = new JsonObject();
		o.add("salida", Utils.getGsonBuilder().toJsonTree(entrada));
		o.add("idEmpresa", Utils.getGsonBuilder().toJsonTree(IdEmpresa));
		o.add("idUsuario", Utils.getGsonBuilder().toJsonTree(IdUsuario));
		Hooks.registrarJson("ValidarMandatosPAC", o);
		Hooks.lanzaSiCorresponde("ValidarMandatosPAC.lanza");
	}
}
