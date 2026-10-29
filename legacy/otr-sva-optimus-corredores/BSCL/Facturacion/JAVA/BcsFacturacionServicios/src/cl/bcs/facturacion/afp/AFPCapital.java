package cl.bcs.facturacion.afp;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AFPCapital implements InterfaceAFP {

	private String cosultarTransaccionesCapitalSQL;

	public static class TransaccionesCapital {
		public String CodigoCorredor;
		public String Fecha;
		public String CompraVenta;
		public String Fondo;
		public String Nemotecnico;
		public String FolioBolsa;
		public String OrdenDirecta;
		public BigDecimal Nominales;
		public BigDecimal Precio;
		public BigDecimal Monto;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public AFPCapital(String consultaSQL) {
		this.cosultarTransaccionesCapitalSQL = consultaSQL;
	}

	@Override
	public String obtenerData(Long idAFP, String fecha, String esJPM, Connection con) throws SQLException {
		SQL sql = new SQL();
		HashMap<String, Object> parametrosConsulta = new HashMap<>();
		parametrosConsulta.put("IdAFP", idAFP);
		parametrosConsulta.put("Fecha", fecha);
		List<TransaccionesCapital> transaccionesAFP = sql.query(con, cosultarTransaccionesCapitalSQL,
				parametrosConsulta, TransaccionesCapital.class);
		return formatoCapital(transaccionesAFP, esJPM);
	}

	/**
	 * Metodo que retorna el texto en el formato de AFP Capital dependiendo del estado del parametro JPM
	 * @param transaccionesAFP Listado de transacciones
	 * @param esJPM Parametro JP Morgan
	 * @return Texto con formato correspondiente
	 */
	private String formatoCapital(List<TransaccionesCapital> transaccionesAFP, String esJPM) {
		StringBuilder archivo = new StringBuilder();
		String vacio = "No existen transacciones";
		if (transaccionesAFP.isEmpty()) {
			System.out.println("========================");
			System.out.println(vacio);
			System.out.println("========================");
			return vacio;
		} else {
			if (esJPM.equalsIgnoreCase("S")) {
				System.out.println("===========:: PARAMETRO ON ::===========");
				for (TransaccionesCapital transaccion : transaccionesAFP) {
					if (transaccion.OrdenDirecta.equalsIgnoreCase("N")) {
						archivo.append(String.format("%-4s", transaccion.CodigoCorredor).replace(' ', ' '));
						archivo.append(transaccion.Fecha);
						archivo.append(transaccion.CompraVenta);
						archivo.append(transaccion.Fondo);
						archivo.append(String.format("%-15s", transaccion.Nemotecnico).replace(' ', ' '));
						archivo.append(String.format("%9s", transaccion.FolioBolsa).replace(' ', '0'));
						archivo.append(transaccion.OrdenDirecta);
						archivo.append(String.format("%13s", onlyNumbers(transaccion.Nominales)).replace(' ', '0'));
						archivo.append(String.format("%13s", onlyNumbers(transaccion.Precio)).replace(' ', '0'));
						archivo.append(String.format("%13s", onlyNumbers(transaccion.Monto)).replace(' ', '0'));
						archivo.append("\n");
					}
				}
			} else {
				System.out.println("===========:: PARAMETRO OFF ::===========");
				for (TransaccionesCapital transaccion : transaccionesAFP) {
					archivo.append(String.format("%-4s", transaccion.CodigoCorredor).replace(' ', ' '));
					archivo.append(transaccion.Fecha);
					archivo.append(transaccion.CompraVenta);
					archivo.append(transaccion.Fondo);
					archivo.append(String.format("%-15s", transaccion.Nemotecnico).replace(' ', ' '));
					archivo.append(String.format("%9s", transaccion.FolioBolsa).replace(' ', '0'));
					archivo.append(transaccion.OrdenDirecta);
					archivo.append(String.format("%13s", onlyNumbers(transaccion.Nominales)).replace(' ', '0'));
					archivo.append(String.format("%13s", onlyNumbers(transaccion.Precio)).replace(' ', '0'));
					archivo.append(String.format("%13s", onlyNumbers(transaccion.Monto)).replace(' ', '0'));
					archivo.append("\n");
				}
			}
			archivo.setLength(archivo.length() - 1);
			System.out.println("==============================================================================");
			System.out.println(archivo.toString());
			System.out.println("==============================================================================");
			return archivo.toString();
		}
	}
}
