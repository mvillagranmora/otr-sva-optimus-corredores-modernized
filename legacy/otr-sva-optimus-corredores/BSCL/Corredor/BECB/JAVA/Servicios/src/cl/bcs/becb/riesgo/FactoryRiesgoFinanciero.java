package cl.bcs.becb.riesgo;

import java.sql.Connection;

import com.google.gson.JsonObject;

import cl.bcs.becb.riesgo.InformeRiesgoFinanciero;
import cl.bcs.becb.riesgo.DetalleRiesgoFinanciero;
import cl.bcs.becb.riesgo.InterfaceRiesgoFinanciero;
import cl.bcs.utils.JavaCall;

/**
 * 
 * @author Carlos Herrera
 *
 */

public class FactoryRiesgoFinanciero implements JavaCall {

	String SQL_CONSULTAR_RIESGO_FINANCIERO = loadResourceToString(
			"cl/bcs/becb/sql/FactoryRiesgoFinanciero_ConsultarInformeRiesgoFinanciero.sql");
	String SQL_CONSULTAR_DETALLE_RIESGO_FINANCIERO = loadResourceToString(
			"cl/bcs/becb/sql/FactoryRiesgoFinanciero_ConsultarDetalleRiesgoFinanciero.sql");
	String SQL_CONSULTAR_RIESGO_FINANCIERO_HIS = loadResourceToString(
			"cl/bcs/becb/sql/FactoryRiesgoFinanciero_ConsultarInformeRiesgoFinancieroHis.sql");
	String SQL_CONSULTAR_DETALLE_RIESGO_FINANCIERO_HIS = loadResourceToString(
			"cl/bcs/becb/sql/FactoryRiesgoFinanciero_ConsultarDetalleRiesgoFinancieroHis.sql");

	public InterfaceRiesgoFinanciero getDAO(String codigo) {
		if (codigo != null && !codigo.equals("")) {
			if (codigo.equalsIgnoreCase("RIFI")) {
				return new InformeRiesgoFinanciero(SQL_CONSULTAR_RIESGO_FINANCIERO,
						SQL_CONSULTAR_RIESGO_FINANCIERO_HIS);
			} else if (codigo.equalsIgnoreCase("DIFI")) {
				return new DetalleRiesgoFinanciero(SQL_CONSULTAR_DETALLE_RIESGO_FINANCIERO,
						SQL_CONSULTAR_DETALLE_RIESGO_FINANCIERO_HIS);
			} else if (codigo.equalsIgnoreCase("ALL")) {
				return new RiesgoFinancieroCompleto(SQL_CONSULTAR_RIESGO_FINANCIERO,
						SQL_CONSULTAR_RIESGO_FINANCIERO_HIS, SQL_CONSULTAR_DETALLE_RIESGO_FINANCIERO,
						SQL_CONSULTAR_DETALLE_RIESGO_FINANCIERO_HIS);
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
