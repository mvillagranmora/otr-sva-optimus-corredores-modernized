package cl.bcs.facturacion.afp;
import java.sql.Connection;
import java.sql.SQLException;
import java.math.BigDecimal;

public interface InterfaceAFP {
	public String obtenerData(Long idAFP, String fecha, String esJPM, Connection con) throws SQLException ;
	
	/**
	 * 
	 * @param label String para eliminar todo menos numeros
	 * @return retorna un string con solo numeros
	 */
	default String onlyNumbers(BigDecimal label) {
		return label.toString().replaceAll("[^0-9]", "");
	}

}
