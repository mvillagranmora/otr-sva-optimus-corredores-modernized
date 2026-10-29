package cl.bcs.facturacion.afp;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AFPHabitat implements InterfaceAFP {

	private String cosultarTransaccionesHabitatSQL;
	
	public static class TransaccionesProvida {
		public String FechaTransaccion;
		public String FolioTransaccion;
		public String CodigoCorredor;
		public String Fondo;
		public String CompraVenta;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public String HoraTransaccion;
		public String Nemotecnico;
		public BigDecimal TotalTransado;
		public String IndicadorDCV;
		public String IndicadorOrdenDirecta;
		public String Bolsa;
		public String CondicionPago;
		public String IdentificadorOperador;
		public String OrdenDirecta;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public AFPHabitat(String consultaSQL) {
		this.cosultarTransaccionesHabitatSQL = consultaSQL;
	}

	@Override
	public String obtenerData(Long idAFP, String fecha, String esJPM, Connection con) throws SQLException {
		SQL sql = new SQL();
		HashMap<String, Object> parametrosConsulta = new HashMap<>();
		parametrosConsulta.put("IdAFP", idAFP);
		parametrosConsulta.put("Fecha", fecha);
		List<TransaccionesProvida> transaccionesAFP = sql.query(con, cosultarTransaccionesHabitatSQL,
				parametrosConsulta, TransaccionesProvida.class);
		return formatoProvida(transaccionesAFP, esJPM);
	}

	/**
	 * Metodo que retorna el texto en el formato de AFP Provida dependiendo del estado del parametro JPM
	 * @param transaccionesAFP Listado de transacciones
	 * @param esJPM Parametro JP Morgan
	 * @return Texto con formato correspondiente
	 */
	private String formatoProvida(List<TransaccionesProvida> transaccionesAFP, String esJPM) {
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
				for (TransaccionesProvida transaccion : transaccionesAFP) {
					if (transaccion.OrdenDirecta.equalsIgnoreCase("N")) {
						archivo.append(String.format("%-8s", transaccion.FechaTransaccion).replace(' ', ' '));
						archivo.append(String.format("%-6s", transaccion.FolioTransaccion).replace(' ', ' '));
						archivo.append(String.format("%-3s", transaccion.CodigoCorredor).replace(' ', ' '));
						archivo.append(String.format("%-3s", transaccion.Fondo).replace(' ', ' '));
						archivo.append(String.format("%-1s", transaccion.CompraVenta).replace(' ', ' '));
						archivo.append(String.format("%13s", onlyNumbers(transaccion.Cantidad)).replace(' ', '0'));
						archivo.append(String.format("%13s", onlyNumbers(transaccion.Precio)).replace(' ', '0'));
						archivo.append(String.format("%-6s", transaccion.HoraTransaccion).replace(' ', ' '));
						archivo.append(String.format("%-10s", transaccion.Nemotecnico).replace(' ', ' '));
						archivo.append(String.format("%15s", onlyNumbers(transaccion.TotalTransado)).replace(' ', '0'));
						archivo.append(String.format("%-1s", transaccion.IndicadorDCV).replace(' ', ' '));
						archivo.append(String.format("%-2s", transaccion.IndicadorOrdenDirecta).replace(' ', ' '));
						archivo.append(String.format("%-1s", transaccion.Bolsa).replace(' ', ' '));
						archivo.append(String.format("%-2s", transaccion.CondicionPago).replace(' ', ' '));
						archivo.append(String.format("%-1s", transaccion.IdentificadorOperador).replace(' ', ' '));
						archivo.append("\n");
					}
				}
			} else {
				System.out.println("===========:: PARAMETRO OFF ::===========");
				for (TransaccionesProvida transaccion : transaccionesAFP) {
					archivo.append(String.format("%-8s", transaccion.FechaTransaccion).replace(' ', ' '));
					archivo.append(String.format("%-6s", transaccion.FolioTransaccion).replace(' ', ' '));
					archivo.append(String.format("%-3s", transaccion.CodigoCorredor).replace(' ', ' '));
					archivo.append(String.format("%-3s", transaccion.Fondo).replace(' ', ' '));
					archivo.append(String.format("%-1s", transaccion.CompraVenta).replace(' ', ' '));
					archivo.append(String.format("%13s", onlyNumbers(transaccion.Cantidad)).replace(' ', '0'));
					archivo.append(String.format("%13s", onlyNumbers(transaccion.Precio)).replace(' ', '0'));
					archivo.append(String.format("%-6s", transaccion.HoraTransaccion).replace(' ', ' '));
					archivo.append(String.format("%-10s", transaccion.Nemotecnico).replace(' ', ' '));
					archivo.append(String.format("%15s", onlyNumbers(transaccion.TotalTransado)).replace(' ', '0'));
					archivo.append(String.format("%-1s", transaccion.IndicadorDCV).replace(' ', ' '));
					archivo.append(String.format("%-2s", transaccion.IndicadorOrdenDirecta).replace(' ', ' '));
					archivo.append(String.format("%-1s", transaccion.Bolsa).replace(' ', ' '));
					archivo.append(String.format("%-2s", transaccion.CondicionPago).replace(' ', ' '));
					archivo.append(String.format("%-1s", transaccion.IdentificadorOperador).replace(' ', ' '));
					archivo.append("\n");
				}
			}
			archivo.setLength(archivo.length() - 1);
			System.out.println("=====================================================================================");
			System.out.println(archivo.toString());
			System.out.println("=====================================================================================");
			return archivo.toString();
		}
	}
}
