package cl.bcs.facturacion.afp;

import java.sql.Connection;

import com.google.gson.JsonObject;

import cl.bcs.utils.JavaCall;

public class FactoryAFP implements JavaCall{

	String SQL_CONSULTAR_TRANSACCIONES_CAPITAL = loadResourceToString(
			"cl/bcs/facturacion/sql/FactoryAFP_ConsultarTransaccionAFP_Capital.sql");
	String SQL_CONSULTAR_TRANSACCIONES_PROVIDA = loadResourceToString(
			"cl/bcs/facturacion/sql/FactoryAFP_ConsultarTransaccionAFP_Provida.sql");
	String SQL_CONSULTAR_TRANSACCIONES_HABITAT = loadResourceToString(
			"cl/bcs/facturacion/sql/FactoryAFP_ConsultarTransaccionAFP_Habitat.sql");

	public InterfaceAFP getDAO(String AFP) {
		if (AFP != null && !AFP.equals("")) {
			if (AFP.equalsIgnoreCase("CAPITAL")) {
				return new AFPCapital(SQL_CONSULTAR_TRANSACCIONES_CAPITAL);
			}else if (AFP.equalsIgnoreCase("PROVIDA")) {
				return new AFPProvida(SQL_CONSULTAR_TRANSACCIONES_PROVIDA);
			}else if (AFP.equalsIgnoreCase("HABITAT")) {
				return new AFPHabitat(SQL_CONSULTAR_TRANSACCIONES_HABITAT);
			}
		}
		return null;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
