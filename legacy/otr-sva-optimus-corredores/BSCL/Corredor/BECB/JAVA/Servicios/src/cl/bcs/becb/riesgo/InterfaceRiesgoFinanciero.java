package cl.bcs.becb.riesgo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * 
 * @author Carlos Herrera
 *
 */

public interface InterfaceRiesgoFinanciero {
	public boolean obtenerData(String codigo, String fechaInforme, Long IdCliente, BigDecimal valorUF, String ruta, String tabla, Connection con) throws SQLException, FileNotFoundException, IOException;
}
